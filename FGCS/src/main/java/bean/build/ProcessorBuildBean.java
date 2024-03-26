package bean.build;

import bean.entity.MEC;
import bean.entity.Processor;
import bean.entity.UE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcessorBuildBean {
    /**
     * 终端、服务器初始化
     * @param processor_number 处理器数量
     * @return 服务器列表
     */
    public static List<Processor> exe(int processor_number){
        Random random = new Random();
        List<Processor> givenProcessorList = new ArrayList<Processor>();

        UE UE = new UE("MEC_0",0.1,2.0,0.05,0.0015);
        givenProcessorList.add(UE);

        //初始化除终端外的所有MEC参数
        for (int j = 1; j < processor_number; j++) {

            double s = 3.1 - (0.1 * j);
            double w = 2.9 + (0.1 * j);
            double beta =  2.1 - (0.1 * j);
            double theta = 0.5 + (0.1 *j);
            double sigma = random.nextDouble() * 0.49 + 0.50;
            double lambda = 0.0015 + (0.0002 * j);


            Processor p = new MEC("MEC_" + j, s, w, beta,lambda, sigma,theta);
            givenProcessorList.add(p);
        }

        return givenProcessorList;
    }
}
