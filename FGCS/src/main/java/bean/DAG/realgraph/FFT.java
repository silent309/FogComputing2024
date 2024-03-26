package bean.DAG.realgraph;


public class FFT {
	public static double[][] fft(int n, double cost_lower, double cost_upper) {
		int[] values = topCompute(n);
//		System.out.println("top:" + values[0] + "  layer:  "+ values[1] +"   count: " + values[2]);
		int count = values[2];
		double[][] matrix = new double[count][count];

		// 顶部二叉树
		for (int i = 0; i < n - 1; i++) {
			int leftChild = i * 2 + 1;
			int rightChild = i * 2 + 2;
			
			matrix[i][leftChild] = cost_lower + Math.random() * cost_upper;
			matrix[i][rightChild] = cost_lower + Math.random() * cost_upper;
		}

		// layer 层
		cross(matrix, count - n, count, n,cost_lower,cost_upper);

		/**
		将产生的数据清零，仅用此符合FFT结构的二维数组表示DAG的架构
 		*/
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				if (matrix[i][j] != 0.0){
					matrix[i][j] = 1.1;
				}
			}
		}
		return matrix;
	}

	private static void cross(double[][] matrix, int start, int end, int n,double cost_lower,
							  double cost_upper) {
		if (end - start <= 1) {
			return;
		}

		int half = (end - start) / 2;
		// left half
		for (int i = start; i < start + half; i++) {
			matrix[i - n][i] = cost_lower + Math.random() * cost_upper;
			matrix[i - n + half][i] = cost_lower + Math.random() * cost_upper;
		}
		// right half
		for (int i = start + half; i < end; i++) {
			matrix[i - n][i] = cost_lower + Math.random() * cost_upper;
			matrix[i - n - half][i] = cost_lower + Math.random() * cost_upper;
		}

		cross(matrix, start - n, start + half - n, n,cost_lower,cost_upper);
		cross(matrix, start + half - n, end - n, n,cost_lower,cost_upper);
	}

	public static int[] topCompute(int n){
		int power = (int) (Math.log(n) / Math.log(2));
		int product = (int) Math.pow(2, power);
		if (power < 1 || product != n) {
			throw new IllegalArgumentException("n must be 2^p");
		}
		int top = product - 1;
		int layer = power + 1;
		int count = n * layer + top;
		int[] index = new int[]{top,layer,count};
		return index;
	}

	/**
	 * 计算DAG层数
	 * @param value DAG节点与层数参数{top:节点数不同的层的节点总数  layer:节点数相同的层数  count:节点数}
	 * @return	level
	 */
	public static int layerCompute(int value){

		int[] top = FFT.topCompute(value);
		int a = 0,level = 0;
		for (int i = 0; i < value; i++) {
			a += Math.pow(2,level++);
			if (top[0] == a) break;
		}
//        System.out.println("layer:"+layer);
		level += top[1];
//		System.out.println(" The level number of fast Fourier transform DAG is " + level);
		return level;
	}
}
