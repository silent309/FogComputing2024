package bean.DAG.realgraph;

import bean.build.MatrixBean;



/**
 * 创造基于FFT的DAG二维示意数组
 */

public class FFTGenerator {
	public static MatrixBean exe(int rho, int processor_number) {

		Double task_number1 = (2 * rho - 1 + rho * (Math.log(rho) / Math.log(2))+1 );

		int task_number = task_number1.intValue();
//		System.out.println(task_number);
//		System.out.println(" The task number of fast Fourier transform is  " + (task_number-1));
//		System.out.println(" The Processor number of fast Fourier transform is  " + (processor_number));

		processor_number = processor_number+1;

		Double[][] compEnergyMatrix = new Double[task_number][processor_number];

		//计算能量矩阵
		for (int i = 0; i < task_number; i++) {
			for (int j = 0; j < processor_number; j++) {

				compEnergyMatrix[i][j] = Math.random();

			}
		}


		Double[][] commMatrix = new Double[task_number][task_number];

		double[][] commMatrix2 = FFT.fft(rho, 1.5, 5);

		//rho为2，节点数显示为5，但通信矩阵中所标示节点仅为4个	(已修改完成)
//		for (int i = 0; i < task_number-1; i++) {
//			for (int j = 0; j < task_number-1; j++) {
//				if (commMatrix2[i][j] != 0.0){
//					System.out.print(1.01 + "    ");
//				}
//				else System.out.print(commMatrix[i][j] + "    ");
//			}
//			System.out.println();
//		}

		for (int i = 0; i < task_number-1; i++) {
			for (int j = 0; j < task_number-1; j++) {

				commMatrix[i][j] = 0.0;
			}
		}

		for (int i = 0; i < task_number - 1; i++) {
			for (int j = 0; j < task_number - 1; j++) {

				commMatrix[i][j] = commMatrix2[i][j];
			}
		}


		MatrixBean TaskCommMatrix = new MatrixBean(commMatrix);

		return TaskCommMatrix;
	}

}
