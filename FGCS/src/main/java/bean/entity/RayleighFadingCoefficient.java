package bean.entity;

import java.util.Random;

public class RayleighFadingCoefficient {
    public static double build(){
        Random random = new Random();
        double value = random.nextDouble() ;
        if ( value <= 0.25) value = 0.4;
        else if (value <= 0.5) value = 0.6;
        else if (value <= 0.75) value = 0.8;
        else  value = 1.0;

        return value;
    }

    public static double getMinValue() {
        return 0.4;
    }
}
