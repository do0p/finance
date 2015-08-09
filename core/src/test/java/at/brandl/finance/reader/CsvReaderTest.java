package at.brandl.finance.reader;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.utils.Constants;

public class CsvReaderTest {
	private static final String FILENAME = Constants.DIR + "reader\\test.csv";
	private InputStream inputStream;
	private FinanceDataReader reader;

	@Before
	public void setUp() throws FileNotFoundException {
		inputStream = new FileInputStream(FILENAME);
		reader = new CsvReader();	
	}

	@Test
	public void csvReader() {
		reader.parse(inputStream);
		Iterator<Line> lines = reader.getLines();
			Line line = lines.next();
			assertEquals(30, line.getDay());
			assertEquals(2, line.getMonth());
			assertEquals(2, line.getWeekDay());
			assertEquals(-52.44, line.getAmount().doubleValue(), 0);
			assertEquals(Arrays.asList("at", "52", "44", "maestro", "pos", "27", "03", "15", "18", "29k6", "bp", "hainfelderstr", "boeheimkirche", "3071"), line.getWords());
	}

	@After
	public void tearDown() throws IOException {
		inputStream.close();
	}
}
