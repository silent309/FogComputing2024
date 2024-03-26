package bean.build;

import bean.entity.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskBuildBean {

    /**
     * 任务列表L初始化
     * @param task_number 任务数目
     * @return 任务列表
     */
    public static List<Task> exe(int task_number) {
        Random random = new Random();

        //初始化任务task列表(计算需求)
        List<Task> givenTaskList = new ArrayList<Task>();
        for (int i = 0; i < task_number; i++) {

            double r = random.nextDouble() * 1.5 + 3.5;
            double d = random.nextDouble() * 2.0 + 2.0;
            double rd = r/d;
            Task t = new Task("task_" + (i+1), r, d,rd);
            givenTaskList.add(t);
        }
        return givenTaskList;
    }

    public static List<Task> exeFFT(int rho) {
        Random random = new Random();
        Double task_number1 = (2 * rho - 1 + rho * (Math.log(rho) / Math.log(2))+1 );

        int task_number = task_number1.intValue()-1;

        //初始化任务task列表(计算需求)
        List<Task> givenTaskList = new ArrayList<Task>();
        for (int i = 0; i < task_number; i++) {

            double r = random.nextDouble() * 3.5 + 1.5;
            double d = random.nextDouble() * 2 + 1.0;
            double rd = r/d;

            Task t = new Task("task_" + (i+1), r, d,rd);
            givenTaskList.add(t);
        }
        return givenTaskList;
    }

    public static List<Task> exeGE(int rho) {
        Random random = new Random();
        int task_number1 = (rho * rho + rho - 2) / 2 + 1;
        int task_number = task_number1-1;

        //初始化任务task列表(计算需求)
        List<Task> givenTaskList = new ArrayList<Task>();
        for (int i = 0; i < task_number; i++) {

            double r = random.nextDouble() * 3.5 + 1.5;
            double d = random.nextDouble() * 2 + 1.0;
            double rd = r/d;

            Task t = new Task("task_" + (i+1), r, d,rd);
            givenTaskList.add(t);
        }
        return givenTaskList;
    }

    public static List<Task> exeNormal(int value) {
        Random random = new Random();

        //初始化任务task列表(计算需求)
        List<Task> givenTaskList = new ArrayList<Task>();
        for (int i = 0; i < value; i++) {

            double r = random.nextDouble() * 3.5 + 1.5;
            double d = random.nextDouble() * 2 + 1.0;
            double rd = r/d;

            Task t = new Task("task_" + (i+1), r, d,rd);
            givenTaskList.add(t);
        }
        return givenTaskList;
    }

    /**
     * 根据二维数组与层数获取层内任务数量
     * @param compCommMatrix
     * @param layer
     * @param task_number
     * @return 下标为当前层数，值为最大的任务名
     */
    public static int[] layersFindFFT(MatrixBean compCommMatrix, int layer, int task_number){
        Double[][] commMatrix = compCommMatrix.getCommMatrix();

        int[] layers = new int[layer];
        layers[0] = 1;
//        System.out.println(Arrays.toString(layers));
        int m = 1,num = 0;
        while (m < layers.length){
            for(int n = 0; n < layers[m-1]; n++) {
                for (int i = 0; i < task_number; i++) {
                    if (commMatrix[n][i] == 1.1 && num < i) num = i;
                }
            }
//            System.out.println(m+"  "+num);
            layers[m++] = num+1;
        }
        return layers;
    }

    public static int[] layersFindGE(MatrixBean compCommMatrix, int layer, int task_number){
        Double[][] commMatrix = compCommMatrix.getCommMatrix();

        int[] layers = new int[layer];
        int num = 0;
        for (int i = 0; i < task_number; i++) {
            if( commMatrix[1][i] != 0.0) num+=1;
        }
        int i = 0,j = 1;
        while (num != 0){
            layers[i++] = j;
            layers[i++] = j+ num;
            j+=num+1;
            num-=1;
        }
        return layers;
    }

    //获取不同层次任务总D数据与R
    public static Task[] TaskLevelMatching(int[] layers, int layer, List<Task> givenTaskList){
        int  n = 0;
        Task[] tasksLevelRD = new Task[layer];    //存放层次任务总数据
        for (int i = 0; i < layers.length; i++) {
            double D = 0,R = 0;
            Task LevelTask = new Task("LevelTask_" + (i+1));
            while (n < layers[i]){
                D += givenTaskList.get(n).getD();
                R += givenTaskList.get(n).getR();
                n++;
            }
            n = layers[i];
            LevelTask.setD(D);
            LevelTask.setR(R);
            tasksLevelRD[i] = LevelTask;
        }
        return tasksLevelRD;
    }

    public static Task TaskAllLevelCompute(int layer, Task[] tasksLevelRD){
        Task AllLevelTaskRD = new Task("AllLevelTaskRD");
        double d = 0, r = 0;
        for (int i = 0; i < layer; i++) {
            d += tasksLevelRD[i].getD();
            r += tasksLevelRD[i].getR();
        }
        AllLevelTaskRD.setR(r);
        AllLevelTaskRD.setD(d);

        return AllLevelTaskRD;
    }

    public static Task TaskAllCompute(int task_number, List<Task> givenTaskList){
        Task AllTaskRD = new Task("AllTaskRD");
        double d = 0, r = 0;
        for (int i = 0; i < task_number; i++) {
            d += givenTaskList.get(i).getD();
            r += givenTaskList.get(i).getR();
        }
        AllTaskRD.setR(r);
        AllTaskRD.setD(d);
        return AllTaskRD;
    }



}
