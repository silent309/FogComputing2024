package bean.entity;

public class REList_copy {

    double RE;
    int maxExeTimes;    //剩余副本执行用能量可分配次数
    int mecIndex;

    public REList_copy(int maxExeTimes, double RE) {
        this.RE = RE;
        this.maxExeTimes = maxExeTimes;
    }

    public REList_copy(double RE, int mecIndex) {
        this.RE = RE;
        this.mecIndex = mecIndex;
    }

    public int getMecIndex() {
        return mecIndex;
    }

    public void setMecIndex(int mecIndex) {
        this.mecIndex = mecIndex;
    }

    public double getRE() {
        return RE;
    }

    public void setRE(double RE) {
        this.RE = RE;
    }

    public int getMaxExeTimes() {
        return maxExeTimes;
    }

    public void setMaxExeTimes(int maxExeTimes) {
        this.maxExeTimes = maxExeTimes;
    }


    //供CopyWithOneTrans使用部分
    double T;
    boolean needE;
    int copyNum;
    double[] Time_P;
    public REList_copy(double RE, double T, int mecIndex) {
        this.RE = RE;
        this.T = T;
        this.mecIndex = mecIndex;
    }
    public REList_copy(double RE, double T, boolean needE, int mecIndex, int copyNum, double[] Time_P) {
        this.RE = RE;
        this.T = T;
        this.needE = needE;
        this.mecIndex = mecIndex;
        this.copyNum = copyNum;
        this.Time_P = Time_P;
    }

    public double[] getTime_P() {
        return Time_P;
    }

    public void setTime_P(double[] time_P) {
        Time_P = time_P;
    }

    public int getCopyNum() {
        return copyNum;
    }

    public void setCopyNum(int copyNum) {
        this.copyNum = copyNum;
    }

    public boolean isNeedE() {
        return needE;
    }

    public void setNeedE(boolean needE) {
        this.needE = needE;
    }

    public double getT() {
        return T;
    }

    public void setT(double t) {
        T = t;
    }
}
