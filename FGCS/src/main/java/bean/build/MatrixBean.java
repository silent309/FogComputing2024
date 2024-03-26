package bean.build;



import bean.entity.*;

import java.util.Arrays;

public class MatrixBean {

	boolean[][] PriorityConsMatrix;

	Double[][] compMatrix ;

	Task[][] tasksRD;

	Double[][] commMatrix;

	public MatrixBean() {
	}

	public MatrixBean(Double[][] commMatrix) {
		this.commMatrix = commMatrix;
	}

	public MatrixBean(Double[][] compMatrix, Double[][] commMatrix) {
		super();
		this.compMatrix = compMatrix;
		this.commMatrix = commMatrix;
	}


	public boolean[][] getPriorityConsMatrix() {
		return PriorityConsMatrix;
	}

	public void setPriorityConsMatrix(boolean[][] priorityConsMatrix) {
		PriorityConsMatrix = priorityConsMatrix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(commMatrix);
		result = prime * result + Arrays.hashCode(compMatrix);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatrixBean other = (MatrixBean) obj;
		if (!Arrays.deepEquals(commMatrix, other.commMatrix))
			return false;
		if (!Arrays.deepEquals(compMatrix, other.compMatrix))
			return false;
		return true;
	}

	public Double[][] getCompMatrix() {
		return compMatrix;
	}

	public void setCompMatrix(Double[][] compMatrix) {
		this.compMatrix = compMatrix;
	}

	public Double[][] getCommMatrix() {
		return commMatrix;
	}

	public void setCommMatrix(Double[][] commMatrix) {
		this.commMatrix = commMatrix;
	}

	public Task[][] getTasksRD() {
		return tasksRD;
	}

	public void setTasksRD(Task[][] tasksRD) {
		this.tasksRD = tasksRD;
	}
}
