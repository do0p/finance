package at.brandl.finance.application;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import at.brandl.finance.common.Line;

public class Journal implements Serializable {

	public static interface Filter {

		boolean accept(Line line);
	}

	public static final String CONFIDENCE = "Confidence";
	public static final String AMOUNT = "Amount";
	public static final String LABEL = "Label";
	public static final String CONFIRMED = "Confirmed";
	public static final String DATE = "Date";
	public static final String TEXT = "Text";
	public static final String REASON = "Reason";

	private static final long serialVersionUID = 1040315008843882813L;

	private final List<Line> lines;

	private transient List<Filter> filters;
	private transient String column;
	private transient boolean up;
	private transient boolean changes;

	public Journal() {
		lines = new ArrayList<Line>();
		filters = new ArrayList<>();
		column = DATE;
		up = true;
	}

	public int size() {

		return getFilteredLines().size();
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

		List<Line> filteredLines = getFilteredLines();

		if (index >= filteredLines.size()) {
			throw new IllegalArgumentException("no line with index " + index);
		}
		return filteredLines.get(index);
	}

	private List<Line> getFilteredLines() {

		return lines.stream().filter(t -> filter(t))
				.collect(Collectors.toList());
	}

	private boolean filter(Line line) {

		for (Filter filter : filters) {
			if (!filter.accept(line)) {
				return false;
			}
		}

		return true;
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

	public void setSort(String column, boolean up) {

		this.column = column;
		this.up = up;
		sort();
	}

	public void sort() {

		Collections.sort(lines, createComparator());
	}

	public void addFilter(Filter filter) {

		filters.add(filter);
	}

	public void removeFilter(Filter filter) {

		filters.remove(filter);
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

	private Comparator<Line> createComparator() {

		return new Comparator<Line>() {

			@Override
			public int compare(Line line1, Line line2) {

				int result;
				switch (column) {

				case DATE:
					result = line1.getDate().compareTo(line2.getDate());
					break;

				case CONFIDENCE:
					result = Double.compare(line1.getConfidence(),
							line2.getConfidence());
					break;

				case AMOUNT:
					result = line1.getAmount().compareTo(line2.getAmount());
					break;

				default:
					result = 0;
				}
				return result * (up ? 1 : -1);
			}
		};
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		in.defaultReadObject();
		filters = new ArrayList<>();
		column = DATE;
		up = true;
	}

}
