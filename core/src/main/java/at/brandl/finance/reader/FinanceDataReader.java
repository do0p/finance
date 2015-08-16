package at.brandl.finance.reader;

import java.io.InputStream;
import java.util.Iterator;

import at.brandl.finance.common.Line;

public interface FinanceDataReader {

	String DATE = "Valutadatum";
	String AMOUNT = "Betrag";
	String TEXT = "Buchungstext";
	String REASON = "Zahlungsgrund";
	String LABEL = "Kategorie";
	
	void parse(InputStream is);

	Iterator<Line> getLines();

}