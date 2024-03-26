package alg.ECLLTROS_H;

import bean.AlgorithmBean;
import bean.entity.Processor;
import bean.entity.Strategy;
import bean.entity.Task;

import java.util.List;

public class ECLLTROS {
    /**
     *
     * @param layer 层级数
     * @param layers 不同层级任务数
     * @param limitE_G 应用程序能量约束
     * @param ELimitList 不同层级能量约束
     * @param givenProcessorList 服务器列表
     * @param givenTaskList 任务列表
     * @param ratioE 副本用能量比
     * @param K 剩余能量切片数量
     */
    public static Strategy[] exe(int layer , int[] layers , double limitE_G , double[] ELimitList, List<Processor> givenProcessorList,
                                 List<Task> givenTaskList,int ratioE, int K, double eta, int h) {
        Strategy[] strategyList = new Strategy[layer];
        Processor[] processors = givenProcessorList.toArray(new Processor[0]);

        //算法1-3行
        for (int i = 0; i < layer; i++) {
            Task[] tasks = AlgorithmBean.tasksFind(i,layers,givenTaskList);
            strategyList[i] = ECTROS.exe(tasks,processors,ELimitList[i],ratioE,eta,h);
//            System.out.print(" 第"+i+"层任务列表：");
//            for (Task task : tasks) {System.out.print(task.getName() + " ");} System.out.println();
//            System.out.println(" 第"+i+"层分配结果："+ Arrays.toString(strategyList[i].getTaskExeProcessor()));
//            System.out.println(" 第"+i+"层目标值："+ strategyList[i]);
//            System.out.println("____________________________");
        }

        //算法4-5
        double limitE_Levels = AlgorithmBean.limit_ESumCompute(layer,ELimitList);
        double remainingE = limitE_G - limitE_Levels;
        double E_edge = remainingE / K;
//        System.out.println("初始总能量："+limitE_Levels+"   剩余能量:"+remainingE);

        return deltaEOptimize_Re(E_edge,remainingE,processors,strategyList,ratioE,eta,h);
    }
    private static Strategy[] deltaEOptimize_Re(double E_edge , double remainingE, Processor[] processors, Strategy[] strategyList,int ratioE, double eta, int h) {

//        System.out.println("-------------------------现在开始剩余能量分配-------------------------");
        /*
         * 在这里要实现确定deltaE的分配归属---> 判优条件:提升更高
         */
        double deltaE;
        //算法6-16
        while (remainingE > 0) {
            if (remainingE <= E_edge) deltaE = remainingE;
            else deltaE = AlgorithmBean.randomGamma() * E_edge;

            Strategy strategyChange = ECTROS.exe(strategyList[0].getTasks(),processors,strategyList[0].getEnergy() + deltaE,
                    ratioE,eta,h);

            //判优条件初值
            double point =  strategyList[0].getFinalTime() - strategyChange.getFinalTime();  //获取初值策略的执行时间的优化值
            int value = 0, layer = 1;

            do{
                Strategy newStrategy = ECTROS.exe(strategyList[layer].getTasks(),processors,strategyList[layer].getEnergy() + deltaE ,
                        ratioE,eta,h);
                double timeGap =  strategyList[layer].getFinalTime() - newStrategy.getFinalTime();
                if (timeGap > point) {
                    strategyChange = newStrategy;
                    value = layer;
                }
                layer++;
            }while (layer < strategyList.length);

            strategyList[value] = strategyChange;
            remainingE -= deltaE;

//            System.out.println("本次分配结束，最终决定分配给第" + value + "层,此时当前层级能量为"+strategyList[value].getEnergy());
//            System.out.println("剩余能量： " + remainingE);
//            System.out.println("____________________________");
        }
        return strategyList;
    }

}
