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

import at.brandl.finance.common.Line;

public class CsvReader implements FinanceDataReader {

	private static final String SEPARATOR = "\\s*;\\s*";

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
					&& (fieldEnd || character == '\n' || character == '\r');

			if (fieldEnd || lineEnd) {

				setValue(buffer.toString(), columns[colNo], line);

				if (lineEnd) {
					lines.add(line);
					while (character != '\n' && reader.ready()) {
						character = (char) reader.read();
					}
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

			column = column.trim();
			data = cleanUp(data);
			switch (column) {

			case DATE:

				parseDate(data, line);
				break;

			case AMOUNT:

				parseAmount(data, line);
				break;

			case TEXT:
			case REASON:
				
				line.putText(column, data);
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

	private String cleanUp(String column) {
		
		column = column.trim();
		if (column.startsWith("\"") && column.endsWith("\"")) {
			column = column.substring(1, column.length() - 1);
		}
		return column;
	}

	private void parseLabel(String data, Line line) {
		
		line.setLabel(data);
	}

	private void parseText(String data, Line line) {
		
		StringTokenizer tokenizer = new StringTokenizer(data, " \"\t\n\r\f.,:/");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().toLowerCase();
			if (token.matches("[-a-zäöüß]{2,}")) {
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

		line.setDate(date);
//		line.setDay(calendar.get(Calendar.DAY_OF_MONTH));
//		line.setWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
//		line.setMonth(calendar.get(Calendar.MONTH));
	}

	@Override
	public Iterator<Line> getLines() {

		return lines.iterator();
	}

}
