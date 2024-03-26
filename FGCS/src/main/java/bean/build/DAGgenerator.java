package bean.build;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DAGgenerator {

	/**
	 * 生成随机DAG优先约束矩阵
	 * @param TaskNum	任务总数
	 * @param p	2.0
	 * @return	优先约束矩阵
	 */
	public static MatrixBean random(int TaskNum, double p) {
			
			boolean [][] PriorityConsMatrix=new boolean[TaskNum+1][TaskNum+1];

			//根据参数给定概率随机生成DAG
			for(int i=1;i<=TaskNum;i++) {
				for(int j=i+1;j<=TaskNum;j++) {
					if(new Random().nextDouble() < p) {
						PriorityConsMatrix[i][j] = true;
					}	
				}
			}

			MatrixBean matrixBean = new MatrixBean();
			matrixBean.setPriorityConsMatrix(PriorityConsMatrix);

			return matrixBean;
		}

	/**
	 * 获取随机DAG矩阵所示的任务-层数数组
	 * @param matrix    优先约束矩阵
	 * @param taskNum   任务总数
	 * @return 下标为当前层数，值为最大的任务名
	 */
	public static List<Integer> getLevelList(boolean[][] matrix, int taskNum){
		int[] taskLevelArray = new int[taskNum + 1];
		boolean[] taskBoolArray = new boolean[taskNum + 1];
		for (int i = 1; i <= taskNum; i++) {
			for (int j = i + 1; j <= taskNum; j++) {
				if (matrix[i][j]) {
					taskBoolArray[i] = true;
					taskBoolArray[j] = true;
					taskLevelArray[j] = Math.max(taskLevelArray[j], taskLevelArray[i] + 1);
				}
			}
		}
//        System.out.println(Arrays.toString(taskLevelArray));
//        System.out.println(Arrays.toString(taskBoolArray));
		int[] levelArray = new int[taskNum + 1];
		for (int i = 1; i <= taskNum; i++) {
			if (taskBoolArray[i]) {
				levelArray[taskLevelArray[i]] ++;
			}else {
				levelArray[0] ++;
			}
		}
//        System.out.println(Arrays.toString(levelArray));
		List<Integer> list = new ArrayList<>();
		for (int i : levelArray) {
			if (i != 0) {
				list.add(i);
			}
		}
		return list;
	}

	/**
	 * 获取真实车联网应用实例结构
	 */
	public static List<Integer> getLevelList_realApp(){
		List<Integer> list = new ArrayList<>();
		list.add(12);
		list.add(19);
		return list;
	}

}