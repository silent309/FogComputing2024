package bean.entity;

public class MEC extends Processor {
    private String name;
    private double s;       //计算速度
    private double w;       //信道带宽
    private double beta;    //任务传输中各干扰因素的总和
    private double lambda;
    private double sigma;   //信道衰落系数均值
    private double theta;   //信噪比阈值(若实际接收信噪比低于此值，则判定传输失败)

    public MEC() {
    }

    public MEC(String name, double s, double w, double beta, double lambda, double sigma, double theta) {
        this.name = name;
        this.s = s;
        this.w = w;
        this.beta = beta;
        this.lambda = lambda;
        this.sigma = sigma;
        this.theta = theta;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double getSigma(){
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getTheta(){
        return theta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getS() {
        return s;
    }

    public void setS(double s) {
        this.s = s;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    @Override
    public String toString() {
        return "MEC{" +
                "name='" + name + '\'' +
                ", s=" + s +
                ", w=" + w +
                ", beta=" + beta +
                ", lambda=" + lambda +
                ", sigma=" + sigma +
                ", theta=" + theta +
                '}';
    }
}
