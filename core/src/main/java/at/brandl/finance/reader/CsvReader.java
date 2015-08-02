package at.brandl.finance.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

public class CsvReader implements FinanceDataReader {

	private static final String SEPARATOR = "\\s*;\\s*";
	private static final String DATE = "Valutadatum";
	private static final String AMOUNT = "Betrag";
	private static final String TEXT = "Buchungstext";
	private static final String REASON = "Zahlungsgrund";
	private static final String LABEL = "Kategorie";
	private final Collection<Line> lines = new ArrayList<>();
	private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private final NumberFormat numberFormat = NumberFormat
			.getNumberInstance(Locale.GERMAN);
	private final Calendar calendar = new GregorianCalendar();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.brandl.finance.reader.FinanceDataReader#parse(java.io.InputStream)
	 */
	@Override
	public void parse(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {

			String header = reader.readLine();
			String[] columns = header.split(SEPARATOR);

			while (reader.ready()) {

				parseLine(reader, columns);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void parseLine(BufferedReader reader, String[] columns)
			throws IOException {
		
		int colNo = 0;
		StringBuffer buffer = new StringBuffer();
		Line line = new Line();

		while (reader.ready()) {

			char character = (char) reader.read();
			boolean fieldEnd = character == ';';
			boolean lineEnd = colNo + 1 == columns.length
					&& character == '\n';

			if (fieldEnd || lineEnd) {

				setValue(buffer.toString(), columns[colNo], line);

				if (lineEnd) {
					lines.add(line);
					break;
				}

				buffer = new StringBuffer();
				colNo++;

			} else {
				
				buffer.append(character);
			}
		}
	}

	private void setValue(String data, String column, Line line) {

		try {

			switch (column) {

			case DATE:

				parseDate(data, line);
				break;

			case AMOUNT:

				parseAmount(data, line);
				break;

			case TEXT:
			case REASON:

				parseText(data, line);
				break;

			case LABEL:

				parseLabel(data, line);
				break;
			}

		} catch (ParseException e) {
			throw new RuntimeException("could not parse " + data + " as "
					+ column);
		}
	}

	private void parseLabel(String data, Line line) {
		line.setLabel(data);
	}

	private void parseText(String data, Line line) {
		StringTokenizer tokenizer = new StringTokenizer(data,
				" \t\n\r\f.,:/");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().toLowerCase();
			if (token.matches("[-a-z����0-9]{2,}")) {
				line.addWord(token);
			}
		}
	}

	private void parseAmount(String data, Line line) throws ParseException {
		Number number = numberFormat.parse(data);
		line.setAmount(new BigDecimal(number.doubleValue()));
	}

	private void parseDate(String data, Line line) throws ParseException {
		Date date = dateFormat.parse(data);
		calendar.setTime(date);

		line.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		line.setWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
		line.setMonth(calendar.get(Calendar.MONTH));
	}

	@Override
	public Iterator<Line> getLines() {

		return lines.iterator();
	}

}
