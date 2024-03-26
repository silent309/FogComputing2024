package alg.ECLLRS;

import bean.AlgorithmBean;
import bean.entity.*;


public class LRSEC {
    /**
     * @param limitE 当前层次总能量阈值
     * @param processors 所有可用处理器数组
     * @param tasks 所有待计算的任务数组
     * @return 调度策略 + T + R + E
     */
    public static Strategy exe(double limitE, Processor[] processors, Task[] tasks ) {

        //初始化层级总可靠性，总响应时间；
        double level_R = 1; double Final_T = 0;
        Strategy strategy_Algorithm1 = new Strategy();

        double limitE_task = limitE / tasks.length;     //等能量法
        int[] task_Processor = new int[tasks.length];   //存储每个任务(下标)的最终分配处理器(值)

        /*
         * 算法2-4
         */
        double[] Time_P = new double[processors.length]; //存储每个处理器(下标)的总卸载执行时间(值)
        double[] Reliability_P = new double[processors.length]; //存储每个处理器(下标)的总可靠性(值)
        double[][] Re_taskProcessor = new double[tasks.length][processors.length]; //任务-处理器可靠性二维数组
        for (int i = 0; i < processors.length; i++) {
            Reliability_P[i] = 1;
            Time_P[i] = 0;
        }

        /*
         * 算法5-9
         */
        //获取当前层次任务(行)在所有服务器(列)的执行时间二维数组(等能量分配下)
        double[][] taskExeTime = AlgorithmBean.taskExeTimeCompute(limitE_task, processors, tasks);
        double[][] taskCommTime = AlgorithmBean.taskCommTimeCompute(limitE_task,processors,tasks);

        //任务-处理器可靠性二维数组计算
        for (int i = 0; i < tasks.length; i++) {
            for (int j = 0; j < processors.length; j++) {
                if (j == 0) Re_taskProcessor[i][j] = AlgorithmBean.compRealityCompute(taskExeTime[i][j],processors[j].getLambda()) * 1;  //j==0则任务本地执行，不卸载至MEC则传输可靠性为1
                else {
                    MEC mec = (MEC)  processors[j];
                    Re_taskProcessor[i][j] = AlgorithmBean.compRealityCompute(taskExeTime[i][j],processors[j].getLambda())
                                           * AlgorithmBean.commRealityCompute(taskCommTime[i][j],mec,limitE_task);
                }
            }
        }


        for (int i = 0; i < tasks.length; i++) {

            //赋予评估初值，判断循环(得出当前任务的最高可靠性执行结果对应的服务器下标index)
            double compareRE = Re_taskProcessor[i][0]; int index = 0;
            for (int j = 1; j < processors.length; j++) {
                double newRE =  Re_taskProcessor[i][j];
                if (newRE > compareRE && newRE != 1){
                    compareRE = newRE;
                    index = j;
                }
            }
            task_Processor[i] = index;

            //刷新index服务器总执行时间
            Time_P[index] += taskExeTime[i][index] + taskCommTime[i][index];
            Reliability_P[index] *= Re_taskProcessor[i][index];
        }

        //层级可靠性计算(每个服务器的总卸载执行可靠性的乘积)
        for (double v : Reliability_P){
            level_R *= v;
        }
        //层级时间计算(所有服务器的总卸载执行时间的最大值)
        for(double v : Time_P){
            Final_T = Math.max(Final_T, v);
        }


        strategy_Algorithm1.setEnergy(limitE);
        strategy_Algorithm1.setTasks(tasks);
        strategy_Algorithm1.setTaskExeProcessor(task_Processor);
        strategy_Algorithm1.setFinalTime(Final_T);
        strategy_Algorithm1.setReality(level_R);

        return strategy_Algorithm1;
    }
}
