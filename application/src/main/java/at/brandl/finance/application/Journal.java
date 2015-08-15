package at.brandl.finance.application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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

	public Line getLine(int index) {

		if (index >= lines.size()) {
			throw new IllegalArgumentException("no line with index " + index);
		}
		return lines.get(index);
	}

	public int getNumLines() {

		return lines.size();
	}

	public List<Line> getConfirmedLines() {

		return lines.stream().filter(t -> t.isConfirmed())
				.collect(Collectors.toList());
	}

	public List<Line> getUnconfirmedLines() {

		return lines.stream().filter(t -> !t.isConfirmed())
				.collect(Collectors.toList());
	}

	public void confirmAllLabeled() {

		lines.stream().filter(t -> t.getLabel() != null)
				.forEach(new Consumer<Line>() {

					@Override
					public void accept(Line line) {

						line.setConfirmed(true);
						line.setConfidence(1);
					}
				});
	}

}
