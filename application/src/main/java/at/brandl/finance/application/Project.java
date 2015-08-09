package at.brandl.finance.application;

import java.util.Iterator;
import java.util.List;

import at.brandl.finance.common.RewindableReader;
import at.brandl.finance.core.linear.LinearModel;
import at.brandl.finance.reader.FinanceDataReader;
import at.brandl.finance.reader.Line;

public class Project {

	private final Journal journal = new Journal();
	private final String name;
	private RewindableReader restore;
	private LinearModel model;
	private String[] labels;
	private String[] wordFeatures;

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
		return count;
	}

	public Journal getJournal() {

		return journal;
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
	}

	public void setModel(LinearModel model) {

		this.model = model;
	}

	public String[] getLabels() {

		return labels;
	}

	public void setLabels(String[] labels) {

		this.labels = labels;
	}

	public void setWordFeatures(String[] wordFeatures) {

		this.wordFeatures = wordFeatures;
	}

	public List<Line> getLabeledLines() {

		return journal.getLabeled();
	}


	public List<Line> getUnlabeledLines() {

		return journal.getUnlabeled();
	}

}
