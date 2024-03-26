package bean.entity;

public class Strategy {
    Task[] tasks;
    int[] taskExeProcessor; //长度为n的数组，表示任务(下标)所分配的处理器编号(值)

    double FinalExeTime;

    double RemainingCopyE;

    double[] Re_taskExeProcessor;
    double[] T_taskExeProcessor;



    //总结果
    double FinalTime;
    double Reality;
    double Energy;


    public Strategy() {
    }

    public Strategy(Task[] tasks, int[] taskExeProcessor) {
        this.tasks = tasks;
        this.taskExeProcessor = taskExeProcessor;
    }

    public double getRemainingCopyE() {
        return RemainingCopyE;
    }

    public void setRemainingCopyE(double remainingCopyE) {
        RemainingCopyE = remainingCopyE;
    }

    public double getFinalExeTime() {
        return FinalExeTime;
    }

    public void setFinalExeTime(double finalExeTime) {
        FinalExeTime = finalExeTime;
    }

    public double getFinalTime() {
        return FinalTime;
    }

    public void setFinalTime(double finalTime) {
        FinalTime = finalTime;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public void setTasks(Task[] tasks) {
        this.tasks = tasks;
    }

    public double getEnergy() {
        return Energy;
    }

    public void setEnergy(double energy) {
        Energy = energy;
    }


    public int[] getTaskExeProcessor() {
        return taskExeProcessor;
    }

    public void setTaskExeProcessor(int[] taskExeProcessor) {
        this.taskExeProcessor = taskExeProcessor;
    }


    public double getReality() {
        return Reality;
    }

    public void setReality(double reality) {
        Reality = reality;
    }

    public double[] getRe_taskExeProcessor() {
        return Re_taskExeProcessor;
    }

    public void setRe_taskExeProcessor(double[] re_taskExeProcessor) {
        Re_taskExeProcessor = re_taskExeProcessor;
    }

    public double[] getT_taskExeProcessor() {
        return T_taskExeProcessor;
    }

    public void setT_taskExeProcessor(double[] t_taskExeProcessor) {
        T_taskExeProcessor = t_taskExeProcessor;
    }


    //DeadLineSlack使用部分
    double[] Time_P;

    public double[] getTime_P() {
        return Time_P;
    }

    public void setTime_P(double[] time_P) {
        Time_P = time_P;
    }

    @Override
    public String toString() {
        return "Strategy{" +
                "FinalTime=" + FinalTime +
                ", Reality=" + Reality +
                ", Energy=" + Energy +
                '}';
    }

}
