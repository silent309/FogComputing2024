package alg.ECLLTS_H;

import alg.ECLLTROS_H.ECTM;
import bean.AlgorithmBean;
import bean.entity.Processor;
import bean.entity.Strategy;
import bean.entity.Task;

import java.util.List;

/*
    废案
 */
public class ECLLTS {
    public static Strategy[] exe(int layer , int[] layers , double limitE_G , double[] ELimitList ,
                                 List<Processor> givenProcessorList, List<Task> givenTaskList, int K, int h) {

        Strategy[] strategyList = new Strategy[layer];
        Processor[] processors = givenProcessorList.toArray(new Processor[0]);

        //算法1-3行
        for (int i = 0; i < layer; i++) {
            Task[] tasks = AlgorithmBean.tasksFind(i,layers,givenTaskList);
            strategyList[i] = ECTM.MinLevelTimeExe(ELimitList[i],processors,tasks,h);  //调用LTSEC算法得出每层的调用方案
        }

        //算法4-5
        double limitE_Levels = AlgorithmBean.limit_ESumCompute(layer,ELimitList);
        double remainingE = limitE_G - limitE_Levels;
        double E_edge = remainingE / K;
        /**
         * 在这里要实现确定deltaE的分配归属---> 判优条件？ (暂定为可靠性优化幅度最大)
         */

        return deltaEOptimize_Re(E_edge,remainingE,processors,strategyList, h);
    }

    private static Strategy[] deltaEOptimize_Re(double E_edge , double remainingE, Processor[] processors, Strategy[] strategyList, int h) {

        double deltaE;
        //算法6-16
        while (remainingE > 0) {
            if (remainingE <= E_edge) deltaE = remainingE;
            else deltaE = AlgorithmBean.randomGamma() * E_edge;

            Strategy strategyChange = ECTM.MinLevelTimeExe(strategyList[0].getEnergy() + deltaE,processors,strategyList[0].getTasks(),h);

            //判优条件初值
            double point = strategyList[0].getFinalTime() - strategyChange.getFinalTime();  //获取初值策略的执行时间的优化值
            int value = 0, layer = 1;

            do{
                Strategy newStrategy = ECTM.MinLevelTimeExe(strategyList[layer].getEnergy() + deltaE, processors, strategyList[layer].getTasks(), h);
                double timeGap =  strategyList[layer].getFinalTime() - newStrategy.getFinalTime();
                if (timeGap > point) {
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
