package alg.ECLL_H;

import bean.AlgorithmBean;
import bean.entity.*;
import bean.entity.Strategy;


import java.util.List;

public class ECLL_H {
    public static Strategy[] exe(int layer , int[] layers , double limitE_G , double[] ELimitList , List<Processor> givenProcessorList, List<Task> givenTaskList, int K,int h) {

        Strategy[] strategyList = new Strategy[layer];
        Processor[] processors = givenProcessorList.toArray(new Processor[0]);

        //获得每一层数对应任务，求出层级初始卸载策略
        for (int i = 0; i < layer; i++) {
            Task[] tasks = AlgorithmBean.tasksFind(i,layers,givenTaskList);
            strategyList[i] = ECLS_H.exe(ELimitList[i],processors,tasks,h);
//            System.out.print("第"+i+"层任务列表：");
//            for (Task task : tasks) {System.out.print(task.getName() + " ");} System.out.println();
//            System.out.println("第"+i+"层初始结果："+strategyList[i]);
//            System.out.println("____________________________");
        }

        //计算剩余能量remainingE与单次分配能量片上限E_edge
        double limitE_Levels = AlgorithmBean.limit_ESumCompute(layer,ELimitList);
        double remainingE = limitE_G - limitE_Levels;
        double E_edge = remainingE / K;

        //进入能量片分配迭代获取最终的调度结果与能量分配方案
        return deltaEOptimize(E_edge,remainingE,processors,strategyList,h);
    }

    private static Strategy[] deltaEOptimize(double E_edge , double remainingE, Processor[] processors, Strategy[] strategyList, int h) {

        //初始化单次实际分配能量deltaE
        double deltaE ;
        while (remainingE > 0) {
            if (remainingE <= E_edge) deltaE = remainingE;
            else deltaE = AlgorithmBean.randomGamma() * E_edge;

            //先将deltaE放入第一层中求出一次优化值作为比较条件
            Strategy strategyChange = ECLS_H.exe(strategyList[0].getEnergy() + deltaE ,processors,strategyList[0].getTasks(), h);
            double point =strategyList[0].getFinalTime() - strategyChange.getFinalTime();  //deltaE添加前后总卸载执行时间优化值
            int value = 0, layer = 1;

            do{
                Strategy strategy = ECLS_H.exe(strategyList[layer].getEnergy() + deltaE, processors, strategyList[layer].getTasks(), h);
                double timeGap = strategyList[layer].getFinalTime() - strategy.getFinalTime() ;

                if (timeGap > point) {
                    strategyChange = strategy;
                    value = layer;
                }
                layer++;
            }while (layer < strategyList.length);

            strategyList[value] = strategyChange;
            remainingE -= deltaE;

//            System.out.println("本次分配结束，最终决定分配给第" + value + "层");
//            System.out.println("剩余能量： " + remainingE);
//            System.out.println("____________________________");

        }




        return strategyList;
    }
}
