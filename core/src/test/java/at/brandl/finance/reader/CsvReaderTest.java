package at.brandl.finance.reader;

import static at.brandl.finance.utils.TestProperties.getTestFile;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.common.Line;

public class CsvReaderTest {
	private static final String FILENAME = getTestFile("test.csv");
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
//		assertEquals(30, line.getDay());
//		assertEquals(2, line.getMonth());
//		assertEquals(2, line.getWeekDay());
		assertTrue(line.isExpense());
		assertEquals(5, line.getMagnitude());
		assertEquals(-52.44, line.getAmount().doubleValue(), 0);
		assertEquals(Arrays.asList("at", "maestro", "pos", "bp",
				"hainfelderstr", "boeheimkirche"), line.getWords());
		assertEquals("AT 52,44 MAESTRO POS 27.03.15 18.29K6 O BP HAINFELDERSTR. BOEHEIMKIRCHE 3071", line.getText("Buchungstext"));
		assertEquals(new Date(1427666400000l), line.getDate());
	}

	@After
	public void tearDown() throws IOException {
		inputStream.close();
	}
}
