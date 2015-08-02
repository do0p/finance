package at.brandl.finance.reader;

import java.io.InputStream;
import java.util.Iterator;

public interface FinanceDataReader {

	void parse(InputStream is);

	Iterator<Line> getLines();

}