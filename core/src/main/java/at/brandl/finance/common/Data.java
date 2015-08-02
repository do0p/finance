package at.brandl.finance.common;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class Data {

	private final TreeMap<Integer, NodeSet> nodeSets = new TreeMap<Integer, NodeSet>();
	private final TreeMap<Integer, Double> labels = new TreeMap<Integer, Double>();
	private int lineNo = -1;

	public void setLabel(double value) {

		labels.put(lineNo, value);
	}

	public void nextLine() {

		nodeSets.put(++lineNo, new NodeSet());
	}

	public void addNode(Node node) {

		nodeSets.get(lineNo).addNode(node);
	}

	public int getSize() {

		return lineNo + 1;
	}

	public List<NodeSet> getNodeSets() {

		return new ArrayList<NodeSet>(nodeSets.values());
	}

	public List<Double> getLabels() {

		return new ArrayList<Double>(labels.values());
	}

	public int getNumFeatures() {

		if (nodeSets.isEmpty()) {
			throw new IllegalStateException("no data available");
		}
		return getNodeSets().get(0).getNodes().size();
	}

	public Double[] getUniqueLabels() {
		
		TreeSet<Double> uniqueLabels = new TreeSet<Double>(getLabels());
		return uniqueLabels.toArray(new Double[uniqueLabels.size()]);
	}
}
