package bean.entity;


public class UE extends Processor {
    private String name;

    private double xi ;   //ξ
    private double a; //α
    private double Ps; // 静态功率
    private double lambda;   // 泊松分布表示事件发生次数的均值lambda

    public UE() {
    }

    public UE(String name, double xi, double a, double ps, double lambda) {
        this.name = name;
        this.xi = xi;
        this.a = a;
        Ps = ps;
        this.lambda = lambda;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getXi() {
        return xi;
    }

    public void setXi(double xi) {
        this.xi = xi;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getPs() {
        return Ps;
    }

    public void setPs(double ps) {
        Ps = ps;
    }

    @Override
    public String toString() {
        return "UE{" +
                "name='" + name + '\'' +
                ", xi=" + xi +
                ", a=" + a +
                ", Ps=" + Ps +
                '}';
    }
}
