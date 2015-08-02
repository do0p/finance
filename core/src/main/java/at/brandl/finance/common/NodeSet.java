package at.brandl.finance.common;

import java.util.ArrayList;
import java.util.List;

public class NodeSet  {

	private final ArrayList<Node> nodes = new ArrayList<Node>();

	public void addNode(Node node) {

		nodes.add(node);
	}

	public List<Node> getNodes() {

		return new ArrayList<Node>(nodes);
	}

	
}
