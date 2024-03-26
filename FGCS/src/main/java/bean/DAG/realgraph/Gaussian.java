package bean.DAG.realgraph;

import java.util.Random;

public class Gaussian {
	public static Double[][] ge(Integer m,double cost_lower, int cost_upper) {
		Integer taskNumber = (m * m + m - 2) / 2 ;

		Double[][] communicationMatrix = new Double[taskNumber][taskNumber];

		int end = m - 1;
		int k = 2;
		int level = m - 1;

		for (int i = 0; i < taskNumber; i++) { // Initiate the matrix with 0.0
			for (int j = 0; j < taskNumber; j++) {

				communicationMatrix[i][j] = 0.0;

				if (i == 0 && (j > 0 && j < m))
					communicationMatrix[i][j] = Double.valueOf(new Random().nextInt());

			}
		}

		for (int i = 0; i < taskNumber; i++) {

			for (int j = i; j < taskNumber; j++) {
				if (i == end + 1) {
					for (int a = i + 1; a < i + 1 + m - k; a++) {
						communicationMatrix[i][a] = cost_lower + new Random().nextInt(cost_upper);
					}
					end = end + 1 + (m - k);
					k++;
					level--;
					break;
				}
				if (i > 0 && j == (i + level))
					communicationMatrix[i][j] = cost_lower + new Random().nextInt(cost_upper);
			}
		}

		//printCommunicationMatrix(communicationMatrix);
		return communicationMatrix;

	}

	public static int layerCompute(int value, Double[][] commMatrix) {
		int task_number = (value * value + value - 2) / 2 + 1;
		int num = 0,layer = 0;
		for (int i = 0; i < task_number; i++) {
			if( commMatrix[1][i] != 0.0) num+=1;
		}
		while (num != 0){
			num--;
			layer+=2;
		}
		return layer;
	}

	public static void printCommunicationMatrix(Integer[][] communicationMatrix) {

		boolean flag = true;
		int taskNumber = communicationMatrix.length;
		System.out.println("communicationMatrix = {");

		for (int i = 0; i < taskNumber; i++) {
			boolean f = false;
			System.out.print("{");
			for (int j = 0; j < taskNumber; j++) {
				if (j == taskNumber - 1) {System.out.print(communicationMatrix[i][j]);}
				else {System.out.print(communicationMatrix[i][j] + ",");}
				f = f || !communicationMatrix[i][j].equals(0.0);
			}
			if (i == taskNumber - 1) {
				System.out.print("}");
			} else {
				System.out.print("},");
			}
			System.out.println("");

			if (i < taskNumber - 1)
				flag = flag && f;
		}

		System.out.println("}");
		System.out.println(flag);
	}
}