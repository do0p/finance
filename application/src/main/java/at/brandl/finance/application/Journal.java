package at.brandl.finance.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import at.brandl.finance.reader.Line;

public class Journal implements Serializable {

	private static final long serialVersionUID = 1040315008843882813L;
	private final List<Line> lines = new ArrayList<>();
	private boolean changes;

	public int size() {

		return lines.size();
	}

	public void add(Line line) {

		lines.add(line);
		changes = true;
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
				.sorted(new Comparator<Line>() {

					@Override
					public int compare(Line o1, Line o2) {

						return Double.compare(o1.getConfidence(),
								o2.getConfidence());
					}
				}).collect(Collectors.toList());
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

	public boolean hasChanges() {

		return changes || changesInLines();
	}

	public void markUnchanged() {

		changes = false;
		markLinesUnchanged();
	}

	private void markLinesUnchanged() {

		lines.stream().filter(t -> t.hasChanges())
				.forEach(new Consumer<Line>() {

					@Override
					public void accept(Line line) {

						line.markUnchanged();
					}
				});
	}

	private boolean changesInLines() {

		for (Line line : lines) {

			if (line.hasChanges()) {
				return true;
			}
		}
		return false;
	}

}
