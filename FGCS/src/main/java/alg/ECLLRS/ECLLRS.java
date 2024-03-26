package alg.ECLLRS;

import bean.AlgorithmBean;
import bean.entity.*;


import java.util.List;

public class ECLLRS {
    public static Strategy[] exe(int layer , int[] layers , double limitE_G , double[] ELimitList , List<Processor> givenProcessorList, List<Task> givenTaskList, int K) {

        Strategy[] strategyList = new Strategy[layer];
        Processor[] processors = givenProcessorList.toArray(new Processor[0]);

        //算法1-3行
        for (int i = 0; i < layer; i++) {
            Task[] tasks = AlgorithmBean.tasksFind(i,layers,givenTaskList);
            strategyList[i] = LRSEC.exe(ELimitList[i],processors,tasks);  //调用LRSEC算法得出每层的调用方案
        }

        //算法4-5
        double limitE_Levels = AlgorithmBean.limit_ESumCompute(layer,ELimitList);
        double remainingE = limitE_G - limitE_Levels;
        double E_edge = remainingE / K, deltaE = 0;
        /**
         * 在这里要实现确定deltaE的分配归属---> 判优条件？ (暂定为可靠性优化幅度最大)
         */

        return deltaEOptimize_Re(E_edge,remainingE,processors,strategyList);
    }

    private static Strategy[] deltaEOptimize_Re(double E_edge , double remainingE, Processor[] processors, Strategy[] strategyList) {

        double deltaE;
        //算法6-16
        while (remainingE > 0) {
            if (remainingE <= E_edge) deltaE = remainingE;
            else deltaE = AlgorithmBean.randomGamma() * E_edge;

            Strategy strategyChange = LRSEC.exe(strategyList[0].getEnergy() + deltaE,processors,strategyList[0].getTasks());

            //判优条件初值
            double point =  strategyChange.getReality() /strategyList[0].getReality();  //获取初值策略的执行时间的优化值
            int value = 0, layer = 1;

            do{
                Strategy newStrategy = LRSEC.exe(strategyList[layer].getEnergy() + deltaE, processors, strategyList[layer].getTasks());
                double reliabilityGap = newStrategy.getReality() / strategyList[layer].getReality()   ;    //新策略可靠性除以原可靠性(优化比值),大于1则存在优化效果

                if (reliabilityGap > point) {
                    strategyChange = newStrategy;
                    value = layer;
                }
                layer++;
            }while (layer < strategyList.length);

            strategyList[value] = strategyChange;
            remainingE -= deltaE;

        }
        return strategyList;
    }

}
