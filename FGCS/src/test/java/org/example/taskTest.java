package org.example;

import alg.ECLLTROS_H.NewAlg_G;
import alg.ECLL_H.ECLL_H;
import bean.AlgorithmBean;
import bean.Heuristic_H;
import bean.build.DAGgenerator;
import bean.build.MatrixBean;
import bean.build.ProcessorBuildBean;
import bean.build.TaskBuildBean;
import bean.entity.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import static exp.DeadLineSlackExperiment.newAlgExe;

public class taskTest {
    public static void main(String[] args) throws Exception {
        test(8,8);
//        NewAlgTest(80,8,0.99);
    }

    public static void test(int processor_number,int init) throws IOException,ClassNotFoundException{

        //初始化服务器数据
        List<Processor> givenProcessorList = ProcessorBuildBean.exe(processor_number);

        //设定能量阈值(上限)
        double E = (4 + 8 * ((1)/10.0)) * init;
//        double E = (4 * 1 - 3) * init;

        //初始化任务列表数据
        List<Task> givenTaskList = TaskBuildBean.exe(1);
        Task task = givenTaskList.get(0);
        /**
         遍历二维数组获得不同层内的实际任务数据
         */
        int[] layers = {1};

        //得出DAG的层数
        int layer = layers.length;

        //获取不同层次任务总D数据与R
        Task[] tasksLevelRD = TaskBuildBean.TaskLevelMatching(layers,layer,givenTaskList);
        Processor[] processors = givenProcessorList.toArray(new Processor[0]);

        /**
         * 通过公式得出各层级所需最低能量-->算法2
         */
        double[] ELimitList = AlgorithmBean.limit_eCompute(layer, tasksLevelRD, givenProcessorList, processor_number);

        double[] taskExeTimeCompute = AlgorithmBean.taskExeTimeCompute(ELimitList[0], processors, task);
        double[] taskCommTimeCompute = AlgorithmBean.taskCommTimeCompute(ELimitList[0], processors, task);

        double[] commRE = new double[processor_number];
        double[] compRE = new double[processor_number];
        double[] AllRE = new double[processor_number];
        double[] AllT  = new double[processor_number];


        System.out.println("服务器名称"+
                "    "+"计算时间"+
                "     "+"传输时间"+
                "      "+"计算可靠性"+
                "      "+"传输可靠性"+
                "      "+"总时间"+
                "      "+"总可靠性");
        DecimalFormat df = new DecimalFormat("0.0000000");
        for (int i = 0; i < processor_number; i++) {
            compRE[i] = AlgorithmBean.compRealityCompute(taskExeTimeCompute[i],processors[i].getLambda());
            if (i != 0) commRE[i] = AlgorithmBean.commRealityCompute(taskCommTimeCompute[i], (MEC) processors[i],E);
            else commRE[i] = 1.00;

            compRE[i] = Double.parseDouble(df.format(compRE[i]));
            commRE[i] = Double.parseDouble(df.format(commRE[i]));
            taskCommTimeCompute[i] = Double.parseDouble(df.format(taskCommTimeCompute[i]));
            taskExeTimeCompute[i] = Double.parseDouble(df.format(taskExeTimeCompute[i]));

            AllRE[i] = Double.parseDouble(df.format(compRE[i]*commRE[i]));
            AllT[i] = Double.parseDouble(df.format(taskExeTimeCompute[i]+taskCommTimeCompute[i]));

            System.out.printf("%7s",processors[i].getName());
            System.out.printf("%15s",taskExeTimeCompute[i]);
            System.out.printf("%12s",taskCommTimeCompute[i]);
            System.out.printf("%15s",compRE[i]);
            System.out.printf("%15s",commRE[i]);
            System.out.printf("%14s",AllT[i]);
            System.out.printf("%13s",AllRE[i]);
            System.out.println();

        }
        System.out.println("-----------------------------------------------------------------------");



    }

    public static void NewAlgTest(int num, int ratioE, double eta) throws Exception {
        exe(num, 8,  200, 16, ratioE, eta, "fileName");
    }

    public static void exe(int task_number, int processor_number,  int K ,int init, int ratioE, double eta, String fileName) throws Exception {

        //初始化服务器数据
        List<Processor> givenProcessorList = ProcessorBuildBean.exe(processor_number);

        //设定能量阈值(上限)
        double E = 24*task_number-72;
        //16m+80
        //30m-90

        System.out.println("总能量: "+E+"   任务数："+task_number+"   服务器数："+processor_number);

        /**
         生成DAG-->得出层数-->获取层数内任务数目


         */
        //生成随机DAG有向无环图的优先约束矩阵
        MatrixBean priorityConsMatrix =	DAGgenerator.random(task_number, 2.0/task_number);

        //初始化任务列表数据
        List<Task> givenTaskList = TaskBuildBean.exe(task_number);

        /**
         遍历二维数组获得不同层内的实际任务数据
         */
        //获取层次所含任务示意数组
        List<Integer> levelList = DAGgenerator.getLevelList(priorityConsMatrix.getPriorityConsMatrix(),task_number);
        int[] layers = new int[levelList.size()];
        int num = 0;
        for (int i = 0; i < levelList.size(); i++) {
            num +=  levelList.get(i);
            layers[i] = num;
        }

        //得出DAG的层数
        int layer = layers.length;

        //获取不同层次任务总D数据与R
        Task[] tasksLevelRD = TaskBuildBean.TaskLevelMatching(layers,layer,givenTaskList);


        //验证上式  (验证完成)
        Task AllLevelTaskRD = TaskBuildBean.TaskAllLevelCompute(layer,tasksLevelRD);
        Task AllTaskRD = TaskBuildBean.TaskAllCompute(task_number,givenTaskList);

        /**
         * 通过公式得出各层级所需最低能量-->算法2
         */
        double[] ELimitList = AlgorithmBean.limit_eCompute(layer, tasksLevelRD, givenProcessorList, processor_number);
        double[] ELimitList_replica = AlgorithmBean.limit_eCompute_replica(layer, tasksLevelRD, givenProcessorList, processor_number,ratioE);

        //输出服务器基本数据与任务列表基本数据
//        System.out.println("————————————————————————————————————————————————");
//
//        for (int i = 0; i < task_number; i++) {
//            System.out.println(givenTaskList.get(i));
//        }
//        System.out.println("————————————————————————————————————————————————");
//
//        for (int i = 0; i < processor_number; i++) {
//            System.out.println(givenProcessorList.get(i));
//        }
//        System.out.println("————————————————————————————————————————————————");
//
//        System.out.println(Arrays.toString(layers));
//        for (int i = 0; i < layer; i++) {
//            System.out.println(tasksLevelRD[i]);
//        }
//        System.out.println("————————————————————————————————————————————————");
//        System.out.println("———————————Verify the above formula—————————————");
//        System.out.println(AllLevelTaskRD);
//        System.out.println(AllTaskRD);
//        System.out.println();

        System.out.println("KeQinL  algorithm");
        for (int h = 0; h < 7; h++){
            Strategy[] strategyList = ECLL_H.exe(layer, layers, E, ELimitList, givenProcessorList, givenTaskList, K, h);
            double[] finalValues = AlgorithmBean.getFinalValues(strategyList);
            System.out.println(Heuristic_H.getName(h)+ "   Energy consumption：" + finalValues[0] + "  Spend time：" + finalValues[1] + " Execution reliability：" + finalValues[2]);

        }

        System.out.println("NewAlg  algorithm");
        for (int h = 0; h < 7; h++) {
            Strategy[] strategyList1 = NewAlg_G.exe(layer, layers, E, ELimitList, givenProcessorList, givenTaskList, ratioE, K, eta, h);
            double[] finalValues1 = AlgorithmBean.getFinalValues(strategyList1);

            System.out.println(Heuristic_H.getName(h) + "   Energy consumption：" + finalValues1[0] + "  Spend time：" + finalValues1[1] + " Execution reliability：" + finalValues1[2]);
        }

    }

}
