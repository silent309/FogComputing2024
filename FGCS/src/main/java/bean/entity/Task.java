package bean.entity;

public class Task {

    private String name;
    private double r;
    private double d;
    private double rd;  //用于启发式H排序

    public Task() {
    }

    public Task(String name, double r, double d, double rd) {
        this.name = name;
        this.r = r;
        this.d = d;
        this.rd = rd;
    }



    public void setName(String name) {
        this.name = name;
    }

    public double getRd() {
        return rd;
    }

    public void setRd(double rd) {
        this.rd = rd;
    }

    public Task(String name) {
        this.name = name;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", r=" + r +
                ", d=" + d +
                ", rd=" + rd +
                '}';
    }
}
