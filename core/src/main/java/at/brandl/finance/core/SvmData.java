package at.brandl.finance.core;

import libsvm.svm_node;
import libsvm.svm_problem;

public class SvmData {

	private final svm_node[][] nodes;
	private final double[] labels;
	private final int numLines;
	private int lineNo;

	public SvmData(int numLines, int numFeatures) {
		this.numLines = numLines;
		labels = new double[numLines];
		nodes = new svm_node[numLines][numFeatures];
	}

	public void setLabel(double value) {
		labels[lineNo] = value;
	}

	public void setLine(int lineNo) {
		this.lineNo = lineNo;
	}

	public void addNode(svm_node node) {
		nodes[lineNo][node.index - 1] = node;
	}

	public int getSize() {
		return numLines;
	}

	public svm_problem toProblem() {
		svm_problem problem = new svm_problem();
		problem.x = nodes;
		problem.y = labels;
		problem.l = numLines;
		return problem;
	}

}
