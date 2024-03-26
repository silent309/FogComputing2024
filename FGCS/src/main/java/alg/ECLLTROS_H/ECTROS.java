package alg.ECLLTROS_H;

import alg.ECLL_H.ECLS_H;
import bean.AlgorithmBean;
import bean.Heuristic_H;
import bean.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ECTROS {

    public static Strategy exe(Task[] tasks, Processor[] processors, double levelE, int ratioE, double eta, int h) {

//        System.out.println("----------------进入层内分配阶段-----------------");
        Strategy strategy_newAlg = new Strategy();
        int CopyLevelNum = 0;// 判定为层级副本的总数目
        double FinalT = 0, FinalRE = 1;  //卸载调度策略的总时间、总可靠性


        //算法开始
        //启发式初始化任务列表
        List<Task> level_tasks = Heuristic_H.exe(new ArrayList<>(Arrays.asList(tasks)), h);
        for (int i = 0; i < level_tasks.size(); i++) {tasks[i] = level_tasks.get(i);}

        //计算副本能量/任务能量(设定同一层级内只有ratioE/taskNum个任务可以执行副本，至少为1个)
        int AllTaskAndCopyNum = tasks.length, levelCopyNum = 0;
        if (tasks.length/ratioE == 0){ AllTaskAndCopyNum +=  1; levelCopyNum = 1;}
        else { levelCopyNum = tasks.length / ratioE; AllTaskAndCopyNum += levelCopyNum;}

        //单次副本/任务卸载执行所用能量(等能量法--此时副本与任务单次卸载执行使用的能量是相同的)
        double limitE_task = levelE / AllTaskAndCopyNum;

        //层级总任务能量/层级总副本能量
        double levelE_replica = limitE_task * levelCopyNum;
        double TaskE = levelE - levelE_replica;


        //计算TimeDeadLine/TimeGap
        Strategy strategy_LTSEC = ECTM.MinLevelTimeExe(TaskE, processors, tasks,h);

        double TimeDeadLine = ECLS_H.exe(levelE, processors, tasks).getFinalTime();
        double minLevelTime = strategy_LTSEC.getFinalTime();

//        System.out.println(" 当前层级时间上限："+ TimeDeadLine+" ,时间下限: " + minLevelTime);

        if (TimeDeadLine < minLevelTime){  //若上限小于下限
//            System.out.print(" 不符合副本执行条件，所以仅做时间优化.");
            strategy_newAlg = ECTM.MinLevelTimeExe(levelE, processors, tasks,h);
//            System.out.println("故本层时间优化为："+ strategy_newAlg.getFinalTime());
            return  strategy_newAlg;
        }

//        double TimeGap = TimeDeadLine - minLevelTime;

        //获取当前层级初始分配结果与服务器现有卸载执行时间
        int[] taskExeProcessor = strategy_LTSEC.getTaskExeProcessor();
        double[] Time_P = strategy_LTSEC.getTime_P();

        double[][] Re_taskProcessor = new double[tasks.length][processors.length]; //任务-处理器可靠性二维数组
        double[][] T_taskProcessor = new double[tasks.length][processors.length]; //任务-处理器可靠性二维数组

        //获取当前层次任务(行)在所有服务器(列)的执行时间二维数组(等能量分配下)
        double[][] taskExeTime = AlgorithmBean.taskExeTimeCompute(limitE_task, processors, tasks);
        double[][] taskCommTime = AlgorithmBean.taskCommTimeCompute(limitE_task,processors,tasks);

        //任务-处理器时间、可靠性二维数组计算
        for (int i = 0; i < tasks.length; i++) {
            for (int j = 0; j < processors.length; j++) {
                T_taskProcessor[i][j] = taskExeTime[i][j] + taskCommTime[i][j];

                if (j == 0) Re_taskProcessor[i][j] = AlgorithmBean.compRealityCompute(taskExeTime[i][j],processors[j].getLambda()) * 1;  //j==0则任务本地执行，不卸载至MEC则传输可靠性为1
                else {
                    MEC mec = (MEC)  processors[j];
                    Re_taskProcessor[i][j] = AlgorithmBean.compRealityCompute(taskExeTime[i][j],mec.getLambda())
                            * AlgorithmBean.commRealityCompute(taskCommTime[i][j],mec,limitE_task);
                }
            }
        }

        /*
        算法核心：遍历所有任务，比较任务可靠性与阈值的关系，小于阈值且副本用能量充足时进入副本判定算法，ReplicaAlg执行结束后
        获得任务的新可靠性，副本在各服务器上执行的总时间列表，
         */
        for (int i = 0; i < tasks.length; i++) {

            //当前任务在所有服务器卸载执行的可靠性列表
            List<REList_copy> reList = new ArrayList<>();

            for (int j = 0; j < processors.length; j++) {
                reList.add(new REList_copy(Re_taskProcessor[i][j], T_taskProcessor[i][j], j));
            }

//            //排序服务器队列，以可靠性降序排列，
////            reList.sort(Comparator.comparing(REList_copy::getRE).reversed());
//            reList.sort(Comparator.comparing(REList_copy::getT));


            if (Re_taskProcessor[i][taskExeProcessor[i]] < eta && levelCopyNum > 0 && levelE_replica >= limitE_task){
                /*
                此时确定低于可靠性阈值，且有充足的副本能量
                其余判定(时间是否充足)进入ReplicaAlg判定
                 */

                REList_copy result = ReplicaAlg.exe(reList,taskExeProcessor[i],taskCommTime[i],taskExeTime[i],Re_taskProcessor[i][taskExeProcessor[i]],
                        eta,TimeDeadLine, Time_P);   //需要返回新的可靠性，副本整体花费的最大时间差, 副本执行后的各服务器总时间列表

                CopyLevelNum += result.getCopyNum();

                if (result.isNeedE()) {
                    levelE_replica -= limitE_task;
                    levelCopyNum -= 1;
                }

                taskExeProcessor[i] += 10 * result.getCopyNum();
                FinalRE *= result.getRE();
                Time_P = result.getTime_P();
            }

            else {
                FinalRE *= Re_taskProcessor[i][taskExeProcessor[i]];
            }
        }

        //层级时间计算(所有服务器的总卸载执行时间的最大值)
        for(double v : Time_P){
            FinalT = Math.max(FinalT, v);
        }

//        System.out.println(" 当前层级最终时间："+ FinalT+" ,");
//        if (FinalT > TimeDeadLine) System.out.println(" 超过TimeDeadLine!!!!!!!!!!!!!!!!");

//        if (levelE_replica != 0) System.out.println(levelE_replica);

        strategy_newAlg.setEnergy(levelE);
        strategy_newAlg.setReality(FinalRE);
        strategy_newAlg.setFinalTime(FinalT);
        strategy_newAlg.setTasks(tasks);
        strategy_newAlg.setTaskExeProcessor(taskExeProcessor);


        if (CopyLevelNum == 0){
            strategy_newAlg = ECTM.MinLevelTimeExe(levelE, processors, tasks,h);
        }

        return strategy_newAlg;
    }
}
