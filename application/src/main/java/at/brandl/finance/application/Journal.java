package at.brandl.finance.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import at.brandl.finance.reader.Line;

public class Journal {

	private final List<Line> lines = new ArrayList<>();

	public int size() {

		return lines.size();
	}

	public void add(Line line) {

		lines.add(line);
	}

	public List<Line> getLabeled() {

		return lines.stream().filter(t -> t.getLabel() != null)
				.collect(Collectors.toList());
	}

	public List<Line> getUnlabeled() {
		
		return lines.stream().filter(t -> t.getLabel() == null)
				.collect(Collectors.toList());
	}

}
