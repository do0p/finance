package at.brandl.finance.common;

public class Node {

	private final int index;
	private final double value;
	
	public Node(int index, double value) {
		
		this.index = index;
		this.value = value;
	}
	
	public int getIndex() {
		
		return index;
	}
	
	public double getValue() {
		
		return value;
	}

	@Override
	public int hashCode() {

		int prime = 31;
		long temp = Double.doubleToLongBits(value);
		int result = prime  + index;
		return prime * result + (int) (temp ^ (temp >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Node)) {
			return false;
		}
		
		Node other = (Node) obj;
		return index == other.index && value == other.value;
	}
	
	@Override
	public String toString() {
	
		return index + ":" + value;
	}
}
