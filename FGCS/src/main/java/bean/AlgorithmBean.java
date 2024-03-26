package bean;


import bean.entity.*;

import java.util.List;
import java.util.Random;

public class AlgorithmBean {

    /**
     * 随机生成gamma参数用于能量切片分配的比较
     * @return gamma
     */
    public static double randomGamma(){
        return new Random().nextDouble() * 0.5 + 0.5;
    }


    /**
     * 获取下限公式计算所得的每一层的能量下限值
     * @param layer 层数
     * @param taskLevelRD   层数对应的R与D
     * @param givenProcessorList 给定任务列表
     * @param processor_number  处理器数量
     * @return  能量阈值数组
     */
    public static double[] limit_eCompute(int layer, Task[] taskLevelRD, List<Processor> givenProcessorList, int processor_number){
        double[] eLimitList = new double[layer];
        for (int num = 0; num < layer; num++) {

            double R = taskLevelRD[num].getR();
            double D = taskLevelRD[num].getD();
            double minValue = 10000000;

            UE ue = (UE) givenProcessorList.get(0);
            double a = ue.getA();

            for (int j = 1; j < processor_number; j++) {
                MEC mec = (MEC) givenProcessorList.get(j);
                double minRayLeValue = RayleighFadingCoefficient.getMinValue();
                if ( mec.getW() * mec.getBeta() * minRayLeValue < minValue) minValue = mec.getW() * mec.getBeta() * minRayLeValue;
            }
            double EL = R * Math.pow(ue.getPs(), (1 - 1 / a)) * Math.pow(ue.getXi(), (1 - 1 / a))
                    * a / Math.pow(a - 1, (1 - 1 / a)) + D * (Math.log(2) / minValue);
            eLimitList[num] = EL;
        }
        return eLimitList;
    }

    public static double[] limit_eCompute_replica(int layer, Task[] tasksLevelRD, List<Processor> givenProcessorList, int processor_number, double y) {
        double[] eLimitList = limit_eCompute(layer, tasksLevelRD, givenProcessorList, processor_number);
        for (int i = 0; i < eLimitList.length; i++) {
            eLimitList[i] = eLimitList[i]/(y/(y+1));
        }
        return eLimitList;
    }

    /**
     * 获取下限公式计算所得各层能量阈值的总和
     * @param layer 层数
     * @param ELimitList    每层能量阈值的列表
     */
    public static double limit_ESumCompute(int layer, double[] ELimitList){
        double limit_E = 0;
        for (int i = 0; i < layer; i++) {
            limit_E += ELimitList[i];
        }
        return limit_E;
    }


    /**
     * 获取DAG中某一层中的对应的任务列表
     * @param num 指定层
     * @param layers    DAG层数任务数组
     * @param givenTaskList 给定的任务列表
     */
    public static Task[] tasksFind(int num, int[] layers, List<Task> givenTaskList){

        Task[] tasks = new Task[0];
        if(num == 0) {
            tasks = new Task[layers[0]];
            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = givenTaskList.get(i);
            }
        }
        else {
            int n = 0;
            tasks = new Task[layers[num]-layers[num-1]];
            for (int i = layers[num-1]; i <layers[num] ; i++) {
                tasks[n++] = givenTaskList.get(i);
            }
        }
        return tasks;
    }

    /**
     * 获取当前层中每一任务在每一处理器上的处理\传输时间（等能量法）
     * @param limitE_task 当前层级任务的能量阈值
     * @param processors 处理器队列
     * @param tasks 当前层对应的任务队列
     */
    public static double[][] taskExeTimeCompute(double limitE_task, Processor[] processors, Task[] tasks){

        double[][] exeTimeAll = new double[tasks.length][processors.length];

        for (int i = 0; i < tasks.length; i++) {

            UE ue = (UE) processors[0];
            double s0_ue = Math.sqrt(Math.pow(limitE_task / (ue.getXi() * tasks[i].getR() * 2),2) - (ue.getPs()/ue.getXi()))
                    + (limitE_task / (2 * ue.getXi() * tasks[i].getR()));


            //s0的计算直接将alpha的预设值2输入进去才得到的上式，若更改alpha则式子需要调整
            exeTimeAll[i][0] = tasks[i].getR() / s0_ue;

            if (Double.isNaN( exeTimeAll[i][0])) exeTimeAll[i][0] = 1000010; //排除掉NAN情况(仅忽略无处理方式)

            for (int j = 1; j < processors.length; j++) {
                MEC mec = (MEC) processors[j];
                double time = tasks[i].getR()/ mec.getS();
                exeTimeAll[i][j] = time;
            }
        }
        return exeTimeAll;
    }


    //二分法求任务传输速度-->任务传输时间
    public static double[][] taskCommTimeCompute(double limitE_task, Processor[] processors, Task[] tasks) {
        double[][] commTimeAll = new double[tasks.length][processors.length];
        double[][] SNR = new double[tasks.length][processors.length];


        for (int i = 0; i < tasks.length; i++) {
            double commSpeed = 0.0, commData = tasks[i].getD();
            double beta,bandWidth;
//            System.out.println("开始计算"+ tasks[i].getName() +"任务的传输时间列表：");
//            System.out.println("    limitE_task: "+ limitE_task);

            //计算EMC的CommTime
            for (int j = 1; j < processors.length; j++) {
                MEC mec = (MEC) processors[j];
                beta = mec.getBeta(); bandWidth = mec.getW();

                double rayLeValue = RayleighFadingCoefficient.build();
                double Right = 2 * ((limitE_task * beta / commData) - (Math.log(2) / bandWidth)) / Math.pow(Math.log(2) / bandWidth, 2);
                double Left = 0d;

                //二分法求指定能耗约束对应的最佳通信速度
                while (compare(Left, Right) < 0) {
                    commSpeed = (Left + Right) / 2.0;
                    double exeEnergy = ((Math.pow(2d, commSpeed / bandWidth) - 1) * commData) / (beta * rayLeValue * commSpeed);
                    if (compare(exeEnergy, limitE_task) == 0) {
                        break;
                    } else if (compare(exeEnergy, limitE_task) < 0) {
                        Left = commSpeed;
                    } else Right = commSpeed;
                }



                commTimeAll[i][j] = tasks[i].getD() / commSpeed;
                SNR[i][j] = (limitE_task/commTimeAll[i][j])* mec.getBeta() * rayLeValue;

            }
            commTimeAll[i][0] = 0;    //在终端无传输

        }
        return commTimeAll;
    }


    /**
     * 两个个double比较，0代表相等，1代表大于，-1代表小于
     */
    public static Integer compare(Double d1, Double d2) {
        if (Math.abs(d1 - d2) < 0.00000001) return 0;   //result=0表示相等
        else if (d1 > d2) return 1;                     //result=1表示d1>d2
        else return -1;                                 //result=-1表示d1<d2

    }



    public static double[] taskCommTimeCompute(double limitE_task, Processor[] processors, Task task) {
        double[] commTimeAll = new double[processors.length];
        double[] SNR = new double[processors.length];

        double cj = 0.0001;
        //计算EMC的CommTime
        for (int j = 1; j < processors.length; j++) {

            MEC mec = (MEC) processors[j];
//                System.out.println(mec.getName() + "等式右侧初值:" + ((Math.pow(2,cj/mec.getW()))-1)* tasks[i].getD()/(mec.getBeta() * cj));
            double rayLeValue = RayleighFadingCoefficient.build();
            while (limitE_task >= ((Math.pow(2,cj/mec.getW()))-1)* task.getD()/(mec.getBeta() * cj * rayLeValue)){
                cj+=0.0001;
            }
//                System.out.println("结束遍历，cj= "+cj);
            if(cj == 0.0001){    //若cj等于初值则说明不可达，则通讯时间设为最大值
                commTimeAll[j] = Double.MAX_VALUE;
                SNR[j] = 0;
            }
            else{
                commTimeAll[j] = task.getD() / cj;
                SNR[j] = (limitE_task/commTimeAll[j])* mec.getBeta() * rayLeValue;
            }
        }
        commTimeAll[0] = 0;    //在终端无传输
        return commTimeAll;
    }

    public static double[] taskExeTimeCompute(double limitE_task, Processor[] processors, Task task){

        double[] exeTimeAll = new double[processors.length];

        UE ue = (UE) processors[0];
        double s0_ue = Math.sqrt(Math.pow(limitE_task / (ue.getXi() * task.getR() * 2),2) - (ue.getPs()/ue.getXi()))
                + (limitE_task / (2 * ue.getXi() * task.getR()));


        //s0的计算直接将alpha的预设值2输入进去才得到的上式，若更改alpha则式子需要调整
        exeTimeAll[0] = task.getR() / s0_ue;

        if (Double.isNaN( exeTimeAll[0])) exeTimeAll[0] = 1000010; //排除掉NAN情况(仅忽略无处理方式)

        for (int j = 1; j < processors.length; j++) {
            MEC mec = (MEC) processors[j];
            double time = task.getR()/ mec.getS();
            exeTimeAll[j] = time;
        }

        return exeTimeAll;
    }



    /**
     * 任务执行/传输可靠性计算
     */
    public static double compRealityCompute(double exeTime, double lambda){
        if(Double.isNaN(exeTime)) return 0;
        else return Math.pow(Math.E,(-lambda * exeTime));
    }

    public static double commRealityCompute(double commTime, MEC mec, double limitE_task){
        if(Double.isNaN(commTime)) return 0;
        else return Math.pow(Math.E,-(mec.getTheta() * commTime )/ (limitE_task * mec.getBeta() * mec.getSigma()));
    }

    //线性传输可靠性计算
    public static double commRealityCompute(double commTime, double commReFault){
        return 1 - (commTime * commReFault);
    }

    /**
     * 获取最终三指标结果
     * @param strategyList 策略结果
     * @return 三指标数组
     */
    public static double[] getFinalValues(Strategy[] strategyList) {
        double EFinal = 0, TFinal = 0, RFinal = 1;
        for (Strategy strategy : strategyList) {
            EFinal += strategy.getEnergy();
            TFinal += strategy.getFinalTime();
            RFinal *= strategy.getReality();
        }
        return new double[]{EFinal,TFinal,RFinal};
    }

    /**
     * 获取RD综合值与总值
     */
    public static double[] getRDValueList(Task[] tasks, double x, double y) {
        double[] RDValues = new double[tasks.length+1];
        double sum = 0;
        for (int i = 0; i < tasks.length; i++) {
            RDValues[i] = (tasks[i].getR() * x )+ (tasks[i].getD() * y);
            sum += RDValues[i];
        }
        RDValues[tasks.length] = sum;
        return RDValues;
    }

    /**
     * 获取当前任务rd之和占当前层次RD总值的比值
     */
    public static double[] getRDValueList(Task[] tasks) {
        double[] RDValues = new double[tasks.length+1];     //数组长度+1 最后一个为层次RD总和
        double sum = 0;
        for (int i = 0; i < tasks.length; i++) {
            RDValues[i] = (tasks[i].getR() + tasks[i].getD());
            sum += RDValues[i];
        }
        RDValues[tasks.length] = sum;
        return RDValues;
    }


    /**
     * 获取当前层中每一任务在每一处理器上的处理\传输时间（limitE能量加权法）
     * @param limitE_tasks 每个任务的执行下限能量值
     * @param limitE 当前层内总的能量阈值
     * @param processors 处理器队列
     * @param tasks 当前层对应的任务队列
     */
    public static double[][] taskExeTimeCompute_limitE(double[] limitE_tasks, double limitE, Processor[] processors, Task[] tasks) {

        double[][] exeTimeAll = new double[tasks.length][processors.length];
        for (int i = 0; i < tasks.length; i++) {

            UE ue = (UE) processors[0];

            double limitE_task =  (limitE_tasks[0]/limitE) * limitE; //加权方式分配能量

            double s0_ue = Math.sqrt(Math.pow(limitE_task / (ue.getXi() * tasks[i].getR() * 2),2) - (ue.getPs()/ue.getXi())) + (limitE_task / (2 * ue.getXi() * tasks[i].getR()));
            //s0的计算直接将alpha的预设值2输入进去才得到的上式，若更改alpha则式子需要调整

            exeTimeAll[i][0] = tasks[i].getR() / s0_ue;

            //计算EMC的执行时间
            for (int j = 1; j < processors.length; j++) {
                MEC mec = (MEC) processors[j];

                double time = tasks[i].getR()/ mec.getS();
                exeTimeAll[i][j] = time;
            }

        }
        return exeTimeAll;
    }

    public static double[][] taskCommTimeCompute_limitE(double[] limitE_tasks, double limitE, Processor[] processors, Task[] tasks) {

        double[][] commTimeAll = new double[tasks.length][processors.length];

        for (int i = 0; i < tasks.length; i++) {

            double taskLimitE =  (limitE_tasks[i]/limitE) * limitE;  //加权能量分配

            //计算EMC的
            for (int j = 1; j < processors.length; j++) {
                double cj = 0.001;
                MEC mec = (MEC) processors[j];
//                System.out.println(mec.getName() + "等式右侧初值:" + ((Math.pow(2,cj/mec.getW()))-1)* tasks[i].getD()/(mec.getBeta() * cj));

                while (taskLimitE >= ((Math.pow(2,cj/mec.getW()))-1)* tasks[i].getD()/(mec.getBeta() * cj)){
                    cj += 0.001;
                }
//                System.out.println("结束遍历，cj= "+cj);

                if(cj == 0.001){    //若cj等于初值则说明不可达，则通讯时间设为最大值
                    commTimeAll[i][j] = Double.MAX_VALUE;
                }
                else{
                    commTimeAll[i][j] = tasks[i].getD() / cj;
                }

            }
            commTimeAll[i][0] = 0;
        }

        return commTimeAll;
    }

    /**
     * 获取当前层中每一任务在每一处理器上的处理\传输时间（RD加权法）
     * @param RDValueList RD综合值列表
     * @param levelLimitE 当前层内总的能量阈值
     * @param processors 处理器队列
     * @param tasks 当前层对应的任务队列
     */
    public static double[][] taskExeTimeCompute_RD(double[] RDValueList, double levelLimitE, Processor[] processors, Task[] tasks){
        double RDSum = RDValueList[tasks.length];
        double[][] runtimeAll = new double[tasks.length][processors.length];

        for (int i = 0; i < tasks.length; i++) {

            UE ue = (UE) processors[0];

            double limitE_task =  (RDValueList[0]/RDSum) * levelLimitE; //加权方式分配能量

            double s0_ue = Math.sqrt(Math.pow(limitE_task / (ue.getXi() * tasks[i].getR() * 2),2) - (ue.getPs()/ue.getXi())) + (limitE_task / (2 * ue.getXi() * tasks[i].getR()));
            //s0的计算直接将alpha的预设值2输入进去才得到的上式，若更改alpha则式子需要调整

            runtimeAll[i][0] = tasks[i].getR() / s0_ue;

            //计算EMC的执行时间
            for (int j = 1; j < processors.length; j++) {
                MEC mec = (MEC) processors[j];

                double time = tasks[i].getR()/ mec.getS();
                runtimeAll[i][j] = time;
            }

        }

        return runtimeAll;
    }

    public static double[][] taskCommTimeCompute_RD(double[] RDValueList, double levelLimitE, Processor[] processors, Task[] tasks) {
        double RDSum = RDValueList[tasks.length];
        double[][] commTimeAll = new double[tasks.length][processors.length];

        for (int i = 0; i < tasks.length; i++) {

            double taskLimitE =  (RDValueList[i]/RDSum) * levelLimitE;  //加权能量分配

            //计算EMC的
            for (int j = 1; j < processors.length; j++) {
                double cj = 0.001;
                MEC mec = (MEC) processors[j];
//                System.out.println(mec.getName() + "等式右侧初值:" + ((Math.pow(2,cj/mec.getW()))-1)* tasks[i].getD()/(mec.getBeta() * cj));

                while (taskLimitE >= ((Math.pow(2,cj/mec.getW()))-1)* tasks[i].getD()/(mec.getBeta() * cj)){
                    cj += 0.001;
                }
//                System.out.println("结束遍历，cj= "+cj);

                if(cj == 0.001){    //若cj等于初值则说明不可达，则通讯时间设为最大值
                    commTimeAll[i][j] = Double.MAX_VALUE;
                }
                else{
                    commTimeAll[i][j] = tasks[i].getD() / cj;
                }

            }
            commTimeAll[i][0] = 0;
        }

        return commTimeAll;
    }


    /**
     * 获取下限公式计算所得的每一任务的能量下限值
     * @param taskNum 当前层次任务总数
     * @param tasks   下标对应任务的R与D
     * @param ProcessorList 给定任务列表
     * @param processor_number  处理器数量
     * @return  能量阈值数组
     */
    public static double[] limit_eCompute_task(int taskNum, Task[] tasks, Processor[] ProcessorList, int processor_number){
        double[] eLimitList_tasks = new double[taskNum];
        for (int i = 0; i < taskNum; i++) {

            double R = tasks[i].getR();
            double D = tasks[i].getD();
            double minValue_WBeta = 100000;

            UE ue = (UE) ProcessorList[0];
            double a = ue.getA();

            for (int j = 1; j < processor_number; j++) {
                MEC mec = (MEC) ProcessorList[j];
                if ( mec.getW() * mec.getBeta() < minValue_WBeta) minValue_WBeta = mec.getW() * mec.getBeta();
            }
            double EL = R * Math.pow(ue.getPs(), (1 - 1 / a)) * Math.pow(ue.getXi(), (1 - 1 / a))
                    * a / Math.pow(a - 1, (1 - 1 / a)) + D * (Math.log(2) / minValue_WBeta);
            eLimitList_tasks[i] = EL;
        }
        return eLimitList_tasks;
    }



}
