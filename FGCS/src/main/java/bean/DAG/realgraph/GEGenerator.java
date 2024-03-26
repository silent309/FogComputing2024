package bean.DAG.realgraph;


import bean.build.MatrixBean;

import java.util.Random;

public class GEGenerator {
        public static MatrixBean exe(int rho, int processor_number) {
            int task_number = (rho * rho + rho - 2) / 2 + 1;

//            System.out.println(" The task number of Gaussian  is  " + (task_number-1));
//            System.out.println(" The Processor number of Gaussian is  " + (processor_number));

            processor_number= processor_number+1;

            Double[][] compEnergyMatrix = new Double[task_number][processor_number];

            for (int i = 0; i < task_number; i++) {
                for (int j = 0; j < processor_number; j++) {

                    compEnergyMatrix[i][j] = (1.5 + new Random().nextInt(5));

                }
            }

            Double[][] commMatrix = new Double[task_number][task_number];
            Double[][] commMatrix2 = Gaussian.ge(rho, 1.5, 5);

            for (int i = 0; i < task_number; i++) {
                for (int j = 0; j < task_number; j++) {

                    commMatrix[i][j] = 0.0;
                }
            }
            for (int i = 1; i < task_number; i++) {
                for (int j = 1; j < task_number; j++) {

                    commMatrix[i][j] = commMatrix2[i-1][j-1];
                }
            }

            MatrixBean TaskCommMatrix = new MatrixBean(commMatrix);
            return TaskCommMatrix;
        }

    }

