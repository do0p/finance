package at.brandl.finance.application;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import at.brandl.finance.application.Journal.Filter;
import at.brandl.finance.common.Line;
import at.brandl.finance.common.RewindableReader;
import at.brandl.finance.core.linear.LinearModel;
import at.brandl.finance.reader.FinanceDataReader;

public class Project implements Serializable {

	private static final long serialVersionUID = 5661898260984383133L;
	
	private final Journal journal = new Journal();
	private final String name;
	private RewindableReader restore;
	private LinearModel model;
	private String[] labels;
	private String[] wordFeatures;
	private boolean changes;

	public Project(String name) {

		if (name == null) {
			throw new IllegalArgumentException("name is null");
		}
		this.name = name;
	}

	public String getName() {

		return name;
	}

	public int readData(FinanceDataReader reader) {

		int count = 0;
		Iterator<Line> linesIterator = reader.getLines();
		while (linesIterator.hasNext()) {
			Line line = linesIterator.next();
			journal.add(line);
			count++;
		}
		changes = count > 0;
		return count;
	}

	public RewindableReader getRestore() {

		return restore;
	}

	public LinearModel getModel() {

		return model;
	}

	public String getLabel(int labelNo) {

		return labels[labelNo];
	}

	public String[] getWordFeatures() {

		return wordFeatures;
	}

	public void setRestore(RewindableReader restore) {

		this.restore = restore;
		changes = true;
	}

	public void setModel(LinearModel model) {

		this.model = model;
		changes = true;
	}

	public String[] getLabels() {

		return labels;
	}

	public void setLabels(String[] labels) {

		this.labels = labels;
		changes = true;
	}

	public void setWordFeatures(String[] wordFeatures) {

		this.wordFeatures = wordFeatures;
		changes = true;
	}

	public List<Line> getLabeledLines() {

		return journal.getLabeled();
	}

	public List<Line> getUnlabeledLines() {

		return journal.getUnlabeled();
	}

	public Line getLine(int index) {

		return journal.getLine(index);
	}

	public List<Line> getConfirmedLines() {

		return journal.getConfirmedLines();
	}

	public List<Line> getUnconfirmedLines() {

		return journal.getUnconfirmedLines();
	}

	public void confirmAllLabeled() {

		journal.confirmAllLabeled();
	}
	
	public boolean hasChanges() {
		
		return changes || journal.hasChanges();
	}

	public void markUnchanged() {
		
		changes = false;
		journal.markUnchanged();
	}
	
	public int getSize() {

		return journal.size();
	}

	public void setSort(String column, boolean up) {

		journal.setSort(column, up);
	}
	
	public void sort() {
		
		journal.sort();
	}
	
	public void addFilter(Filter filter) {
		
		journal.addFilter(filter);
	}
	
	public void removeFilter(Filter filter) {
		
		journal.removeFilter(filter);
	}

	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

	public void release() {
		// TODO Auto-generated method stub
		
	}
}
