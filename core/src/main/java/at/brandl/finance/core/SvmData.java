package at.brandl.finance.core;

import java.util.Arrays;

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
		nodes = new svm_node[numLines][];
	}

	public void setLabel(double value) {
		labels[lineNo] = value;
	}

	public void setLine(int lineNo) {
		this.lineNo = lineNo;
		nodes[lineNo] = new svm_node[0];
	}

	public void addNode(svm_node node) {
		
		svm_node[] original = nodes[lineNo];
		int length = original.length;
		nodes[lineNo] = Arrays.copyOf(original, length+1);
		nodes[lineNo][length] = node;
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
