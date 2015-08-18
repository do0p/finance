package at.brandl.finance.application;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class Journal implements Serializable {

	private static final String DATE_FORMAT = "dd.MM.yyyy";
	private static final String SEPERATOR = ";";
	private static final String QUOTE = "\"";
	private static final String NL = System.lineSeparator();

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

	private transient DateFormat dateFormat;
	private transient String column;
	private transient boolean up;
	private transient boolean changes;

	public Journal() {
		lines = new ArrayList<Line>();
		column = DATE;
		up = true;
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}

	public int size(Collection<Filter> filters) {

		return getFilteredLines(filters).size();
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

	public List<Line> getFilteredLines(Collection<Filter> filters) {

		if (filters == null) {
			return lines;
		}

		return lines.stream().filter(t -> filter(t, filters))
				.collect(Collectors.toList());
	}

	private boolean filter(Line line, Collection<Filter> filters) {

		for (Filter filter : filters) {
			if (!filter.accept(line)) {
				return false;
			}
		}

		return true;
	}

	public List<Line> getTrainedLines() {

		return lines.stream().filter(t -> t.isTrained())
				.collect(Collectors.toList());
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
		column = DATE;
		up = true;
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}

	public String toCsv() {

		StringBuilder csv = new StringBuilder();
		csv.append(createCsvHeader());
		for (Line line : lines) {
			csv.append(createCsvLine(line));
		}
		return csv.toString();
	}



	private String createCsvHeader() {

		String header = StringUtils.join(new String[] { LABEL, CONFIDENCE,
				CONFIRMED, DATE, AMOUNT, TEXT, REASON }, quote(SEPERATOR));
		return quote(header) + NL;
	}

	private String quote(String text) {

		return QUOTE + text + QUOTE;
	}

	private String createCsvLine(Line line) {

		String label = quote(line.getLabel());
		String confidence = String.format("%.3f", line.getConfidence() );
		String confirmed = line.isConfirmed() ? line.isTrained() ? "t" : "c" : "u";
		String date = dateFormat.format(line.getDate());
		String amount = String.format("%.3f", line.getAmount() );
		String text = line.getText(FinanceDataReader.TEXT);
		text = text == null ? "" : text;
		String reason = line.getText(FinanceDataReader.REASON);
		reason = reason == null ? "" : reason;
		
		return StringUtils.join(new String[] {label, confidence, confirmed, date, amount, text, reason}, SEPERATOR) + NL;
	}

}
