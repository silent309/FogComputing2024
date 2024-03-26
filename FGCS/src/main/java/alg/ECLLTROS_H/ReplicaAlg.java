package alg.ECLLTROS_H;

import bean.entity.REList_copy;

import java.util.List;

public class ReplicaAlg {
    /**
     * 计算当前任务在可牺牲时间中加入的副本与提升的可靠性
     * @param reList
     * @param index_task
     * @param T_taskTransPro
     * @param T_taskExePro
     * @param taskRe
     * @param eta
     * @param TimeDeadLine 当前层级时间上限
     * @param Time_P
     * @return
     */
    public static REList_copy exe(List<REList_copy> reList, int index_task, double[] T_taskTransPro, double[] T_taskExePro,
                                  double taskRe, double eta, double TimeDeadLine, double[] Time_P) {
        double maxCopyTimeGap = 0, finalRe = taskRe;
        int copyNum = 0;
        boolean NeedE = false;
        double Re_fault = 1 - taskRe; //初始错误率

        //reList为时间增序排列, 依次提取非task执行的服务器加入副本执行，直至可用服务器为空或TimeGap不满足或可靠性高于eta
        //返回任务最终可靠性，返回TimeGap_Addition(最大副本时间-任务服务器时间)
        double T_task = T_taskExePro[index_task] + T_taskTransPro[index_task];

        if (index_task == 0) {
            NeedE = true;
            for (REList_copy copy : reList) {
                double newTime_P = Time_P[copy.getMecIndex()] + copy.getT();
                double newRe_Task = 1 - (Re_fault * (1 - copy.getRE()));

                //若此服务器的总时间低于层级时间约束，非task执行服务器的同时，可靠性高于taskRe且任务最终可靠性不高于eta
                if (newTime_P <= TimeDeadLine && copy.getMecIndex() != index_task && newRe_Task > taskRe && finalRe < eta) {
                    /*
                     如果满足上述条件，则认为此服务器可以执行副本，则服务器总时间增加，刷新最大副本执行时间差，副本对应任务可靠性、错误率改变，副本数量+1
                     */
                    Time_P[copy.getMecIndex()] = newTime_P;

                    maxCopyTimeGap = Math.max(copy.getT() - T_task, maxCopyTimeGap);

                    Re_fault *= 1 - copy.getRE();
                    finalRe = 1 - Re_fault;

                    copyNum++;
                }
            }
        }

        else {
            /*
            此时任务初始执行服务器为雾端MEC，则只有在终端执行副本时才需要支付副本能量
            if (copy.getMecIndex() == 0) NeedE = true;
             */
            for (REList_copy copy : reList) {
                double newTime_P = Time_P[copy.getMecIndex()] + copy.getT();
                double newRe_Task = 1 - (Re_fault * (1 - copy.getRE()));

                //若此服务器的总时间低于层级时间约束，非task执行服务器的同时，可靠性高于taskRe且任务最终可靠性不高于eta
                if (newTime_P < TimeDeadLine && copy.getMecIndex() != index_task && newRe_Task > taskRe && finalRe < eta) {
                    /*
                     如果满足上述条件，则认为此服务器可以执行副本，则服务器总时间增加，刷新最大副本执行时间差，副本对应任务可靠性、错误率改变，副本数量+1
                     */
                    if (copy.getMecIndex() == 0) NeedE = true;
                    Time_P[copy.getMecIndex()] = newTime_P;

                    maxCopyTimeGap = Math.min(TimeDeadLine - newTime_P, maxCopyTimeGap);

                    Re_fault *= 1 - copy.getRE();
                    finalRe = 1 - Re_fault;

                    copyNum++;
                }
            }
        }

        //需要返回新的可靠性，副本整体花费的最大时间差, 最大时间的服务器下标
        return new REList_copy(finalRe,maxCopyTimeGap,NeedE, index_task,copyNum,Time_P);
    }

}
