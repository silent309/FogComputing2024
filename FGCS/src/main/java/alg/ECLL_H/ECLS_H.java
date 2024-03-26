package alg.ECLL_H;

import bean.AlgorithmBean;
import bean.Heuristic_H;
import bean.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ECLS_H {

    /**
     * 同一层级内的等能量法可靠性优化策略
     * @param limitE    层级能量
     * @param processors    服务器列表
     * @param tasks 任务列表
     * @return 当前层级所示任务列表的卸载执行策略
     */
    public static Strategy exe(double limitE, Processor[] processors, Task[] tasks) {

        Strategy strategy_Algorithm1 = new Strategy();
        double FinalT = 0,FinalRE = 1;  //卸载调度策略的总时间、总可靠性

        //等能量法
        double limitE_task = limitE / tasks.length;
        int[] taskExeProcessor = new int[tasks.length];   //存储每个任务(下标)的最终分配处理器(值)

        double[] Time_P = new double[processors.length]; //存储每个处理器(下标)的总卸载执行时间(值)
        double[] Reliability_P = new double[processors.length]; //存储每个处理器(下标)的总可靠性(值)

        for (int i = 0; i < processors.length; i++) {
            Reliability_P[i] = 1;
            Time_P[i] = 0;
        }

        //获取当前层次任务(行)在所有服务器(列)的执行/通讯时间的二维数组(等能量分配下)
        double[][] taskExeTime = AlgorithmBean.taskExeTimeCompute(limitE_task, processors, tasks);
        double[][] taskCommTime = AlgorithmBean.taskCommTimeCompute(limitE_task, processors, tasks);

//        double[][] allTime = new double[tasks.length][processors.length];
//        for (int i = 0; i < tasks.length; i++) {
//            for (int j = 0; j < processors.length; j++) {
//                allTime[i][j] = taskExeTime[i][j] + taskCommTime[i][j];
//                System.out.print(allTime[i][j] +"  ");
//            }
//            System.out.println();
//        }



        //本层任务数量 <= 总服务器数，直接求依次执行时间的最大值
        //当任务数量 > 服务器数量的时候，先按照顺序将任务卸载至服务器，直至无空闲服务器，剩余任务开始等待，当任意服务器再次进入空闲状态再卸载至当前服务器，直至所有任务完成卸载执行
        int i;
        if (tasks.length <= processors.length){
            for ( i = 0; i < tasks.length; i++) {
                //计算时间
                Time_P[i] += taskExeTime[i][i] + taskCommTime[i][i];

                //计算可靠性(本地执行/雾端执行)
                if (i == 0) {
                    double lambda = ((UE) processors[i]).getLambda();
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],lambda) * 1; //不卸载至MEC则传输可靠性为1
                }
                else {
                    MEC mec = (MEC)  processors[i];
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],mec.getLambda())
                                      * AlgorithmBean.commRealityCompute(taskCommTime[i][i],mec,limitE_task);
                }
                taskExeProcessor[i] = i;    //此时任务下标=服务器下标
            }
        }

        else {
            for ( i = 0; i < processors.length; i++) {  //按照初始化后的任务列表依次将任务发送至空闲MEC执行(仅卸载服务器个数的任务)

                Time_P[i] += taskExeTime[i][i] + taskCommTime[i][i];   //各服务器当前完成任务卸载执行总时间

                if (i == 0) {
                    double lambda = ((UE) processors[i]).getLambda();
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],lambda) * 1;
                }
                else {
                    MEC mec = (MEC)  processors[i];
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],mec.getLambda())
                                     * AlgorithmBean.commRealityCompute(taskCommTime[i][i],mec,limitE_task);
                }
                taskExeProcessor[i] = i;
            }

            double[] RemainingTime_P = new double[processors.length];  //此时时间点为全部任务确定卸载目标，但未开始卸载执行
            System.arraycopy(Time_P, 0, RemainingTime_P, 0, Time_P.length);

            //算法10-25(剩余任务(m-n)卸载调度)
            while(i < tasks.length){

                //赋予评估初值，判断循环(得出最早进入空闲状态服务器下标indexMin)
                double MinTime = RemainingTime_P[0]; int indexMin = 0;
                for (int j = 1; j < processors.length; j++) {
                    if (MinTime > RemainingTime_P[j] ){
                        MinTime = RemainingTime_P[j];
                        indexMin = j ;
                    }
                }

                //算法13-17（时间消去,即所有服务器时间推移至首个空闲服务器出现的时间节点）
                for (int j = 0; j < RemainingTime_P.length; j++) {
                    if (RemainingTime_P[j] != 0) {
                        RemainingTime_P[j] -= MinTime;
                    }
                }


                //将下一任务放入indexMin服务器执行(服务器卸载执行时间刷新)
                Time_P[indexMin] += taskExeTime[i][indexMin] + taskCommTime[i][indexMin];
                RemainingTime_P[indexMin] += taskExeTime[i][indexMin] + taskCommTime[i][indexMin];

                //单一服务器总可靠性刷新
                if (indexMin == 0){
                    double lambda = ((UE) processors[indexMin]).getLambda();
                    Reliability_P[indexMin] *= AlgorithmBean.compRealityCompute(taskExeTime[i][indexMin],lambda) * 1;  //不卸载至MEC则传输可靠性为1
                }
                else {
                    MEC mec = (MEC)  processors[indexMin];
                    Reliability_P[indexMin] *= AlgorithmBean.compRealityCompute(taskExeTime[i][indexMin],mec.getLambda())
                                            * AlgorithmBean.commRealityCompute(taskCommTime[i][indexMin],mec,limitE_task);
                }
                taskExeProcessor[i] = indexMin;
                i++;
            }
        }

        //层级可靠性计算(每个服务器的总卸载执行可靠性的乘积)
        for (double v : Reliability_P){
            FinalRE *= v;
        }

        //层级时间计算(所有服务器的总卸载执行时间的最大值)
        for(double v : Time_P){
            FinalT = Math.max(FinalT, v);
        }

        strategy_Algorithm1.setEnergy(limitE);
        strategy_Algorithm1.setTasks(tasks);
        strategy_Algorithm1.setTaskExeProcessor(taskExeProcessor);
        strategy_Algorithm1.setFinalTime(FinalT);
        strategy_Algorithm1.setReality(FinalRE);

        return strategy_Algorithm1;
    }

    public static Strategy exe(double limitE, Processor[] processors, Task[] tasks, int h) {

        //启发式初始化任务列表
        List<Task> level_tasks = Heuristic_H.exe(new ArrayList<>(Arrays.asList(tasks)), h);
        for (int i = 0; i < level_tasks.size(); i++) {
            tasks[i] = level_tasks.get(i);
        }

        Strategy strategy_Algorithm1 = new Strategy();
        double FinalT = 0,FinalRE = 1;  //卸载调度策略的总时间、总可靠性

        //等能量法
        double limitE_task = limitE / tasks.length;
        int[] taskExeProcessor = new int[tasks.length];   //存储每个任务(下标)的最终分配处理器(值)

        double[] Time_P = new double[processors.length]; //存储每个处理器(下标)的总卸载执行时间(值)
        double[] Reliability_P = new double[processors.length]; //存储每个处理器(下标)的总可靠性(值)

        for (int i = 0; i < processors.length; i++) {
            Reliability_P[i] = 1;
            Time_P[i] = 0;
        }

        //获取当前层次任务(行)在所有服务器(列)的执行/通讯时间的二维数组(等能量分配下)
        double[][] taskExeTime = AlgorithmBean.taskExeTimeCompute(limitE_task, processors, tasks);
        double[][] taskCommTime = AlgorithmBean.taskCommTimeCompute(limitE_task, processors, tasks);

//        double[][] allTime = new double[tasks.length][processors.length];
//        for (int i = 0; i < tasks.length; i++) {
//            for (int j = 0; j < processors.length; j++) {
//                allTime[i][j] = taskExeTime[i][j] + taskCommTime[i][j];
//                System.out.print(allTime[i][j] +"  ");
//            }
//            System.out.println();
//        }



        //本层任务数量 <= 总服务器数，直接求依次执行时间的最大值
        //当任务数量 > 服务器数量的时候，先按照顺序将任务卸载至服务器，直至无空闲服务器，剩余任务开始等待，当任意服务器再次进入空闲状态再卸载至当前服务器，直至所有任务完成卸载执行
        int i;
        if (tasks.length <= processors.length){
            for ( i = 0; i < tasks.length; i++) {
                //计算时间
                Time_P[i] += taskExeTime[i][i] + taskCommTime[i][i];

                //计算可靠性(本地执行/雾端执行)
                if (i == 0) {
                    double lambda = ((UE) processors[i]).getLambda();
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],lambda) * 1; //不卸载至MEC则传输可靠性为1
                }
                else {
                    MEC mec = (MEC)  processors[i];
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],mec.getLambda())
                                      * AlgorithmBean.commRealityCompute(taskCommTime[i][i],mec,limitE_task);
                }
                taskExeProcessor[i] = i;    //此时任务下标=服务器下标
            }
        }

        else {
            for ( i = 0; i < processors.length; i++) {  //按照初始化后的任务列表依次将任务发送至空闲MEC执行(仅卸载服务器个数的任务)

                Time_P[i] += taskExeTime[i][i] + taskCommTime[i][i];   //各服务器当前完成任务卸载执行总时间

                if (i == 0) {
                    double lambda = ((UE) processors[i]).getLambda();
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],lambda) * 1;
                }
                else {
                    MEC mec = (MEC)  processors[i];
                    Reliability_P[i] *= AlgorithmBean.compRealityCompute(taskExeTime[i][i],mec.getLambda())
                                     * AlgorithmBean.commRealityCompute(taskCommTime[i][i],mec,limitE_task);
                }
                taskExeProcessor[i] = i;
            }

            double[] RemainingTime_P = new double[processors.length];  //此时时间点为全部任务确定卸载目标，但未开始卸载执行
            System.arraycopy(Time_P, 0, RemainingTime_P, 0, Time_P.length);

            //算法10-25(剩余任务(m-n)卸载调度)
            while(i < tasks.length){

                //赋予评估初值，判断循环(得出最早进入空闲状态服务器下标indexMin)
                double MinTime = RemainingTime_P[0]; int indexMin = 0;
                for (int j = 1; j < processors.length; j++) {
                    if (MinTime > RemainingTime_P[j] ){
                        MinTime = RemainingTime_P[j];
                        indexMin = j ;
                    }
                }

                //算法13-17（时间消去,即所有服务器时间推移至首个空闲服务器出现的时间节点）
                for (int j = 0; j < RemainingTime_P.length; j++) {
                    if (RemainingTime_P[j] != 0) {
                        RemainingTime_P[j] -= MinTime;
                    }
                }


                //将下一任务放入indexMin服务器执行(服务器卸载执行时间刷新)
                Time_P[indexMin] += taskExeTime[i][indexMin] + taskCommTime[i][indexMin];
                RemainingTime_P[indexMin] += taskExeTime[i][indexMin] + taskCommTime[i][indexMin];

                //单一服务器总可靠性刷新
                if (indexMin == 0){
                    double lambda = ((UE) processors[indexMin]).getLambda();
                    Reliability_P[indexMin] *= AlgorithmBean.compRealityCompute(taskExeTime[i][indexMin],lambda) * 1;  //不卸载至MEC则传输可靠性为1
                }
                else {
                    MEC mec = (MEC)  processors[indexMin];
                    Reliability_P[indexMin] *= AlgorithmBean.compRealityCompute(taskExeTime[i][indexMin],mec.getLambda())
                                            * AlgorithmBean.commRealityCompute(taskCommTime[i][indexMin],mec,limitE_task);
                }
                taskExeProcessor[i] = indexMin;
                i++;
            }
        }

        //层级可靠性计算(每个服务器的总卸载执行可靠性的乘积)
        for (double v : Reliability_P){
            FinalRE *= v;
        }

        //层级时间计算(所有服务器的总卸载执行时间的最大值)
        for(double v : Time_P){
            FinalT = Math.max(FinalT, v);
        }

        strategy_Algorithm1.setEnergy(limitE);
        strategy_Algorithm1.setTasks(tasks);
        strategy_Algorithm1.setTaskExeProcessor(taskExeProcessor);
        strategy_Algorithm1.setFinalTime(FinalT);
        strategy_Algorithm1.setReality(FinalRE);

        return strategy_Algorithm1;
    }
}
