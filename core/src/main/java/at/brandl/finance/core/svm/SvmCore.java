package at.brandl.finance.core.svm;

import java.util.List;

import libsvm.svm;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import at.brandl.finance.common.Data;
import at.brandl.finance.common.Node;
import at.brandl.finance.common.NodeSet;
import at.brandl.finance.core.Core;

public class SvmCore implements Core<SvmModel> {

	@Override
	public SvmModel train(Data data) {

		svm_problem problem = createProblem(data);
		svm_parameter params = createParams();
		svm.svm_check_parameter(problem, params);
		return new SvmModel(svm.svm_train(problem, params));
	}

	@Override
	public double[] predict(SvmModel model, NodeSet data) {

		return new double[] { svm.svm_predict(model.getSvmModel(),
				convertNodes(data)) };
	}

	private svm_problem createProblem(Data data) {

		svm_problem problem = new svm_problem();
		problem.x = convertNodeSets(data.getNodeSets());
		problem.y = convertLabels(data.getLabels());
		problem.l = data.getSize();
		return problem;
	}

	private double[] convertLabels(List<Double> labels) {

		int numLabels = labels.size();
		double[] result = new double[numLabels];
		for (int i = 0; i < numLabels; i++) {
			result[i] = labels.get(i);
		}
		return result;
	}

	private svm_node[][] convertNodeSets(List<NodeSet> nodeSets) {

		int numSets = nodeSets.size();
		svm_node[][] result = new svm_node[numSets][];
		for (int i = 0; i < numSets; i++) {
			result[i] = convertNodes(nodeSets.get(i));
		}
		return result;
	}

	private svm_node[] convertNodes(NodeSet nodeSet) {

		List<Node> nodes = nodeSet.getNodes();
		int numNodes = nodes.size();
		svm_node[] result = new svm_node[numNodes];
		for (int i = 0; i < numNodes; i++) {
			result[i] = convertNode(nodes.get(i));
		}
		return result;
	}

	private svm_node convertNode(Node node) {

		svm_node svmNode = new svm_node();
		svmNode.index = node.getIndex();
		svmNode.value = node.getValue();
		return svmNode;
	}

	private svm_parameter createParams() {

		svm_parameter params = new svm_parameter();
		params.C = 10;
		params.kernel_type = 0;
		params.svm_type = 0;
		return params;
	}
}
