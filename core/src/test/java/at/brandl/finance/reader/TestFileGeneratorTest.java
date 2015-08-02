package at.brandl.finance.reader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.utils.Constants;

public class TestFileGeneratorTest {
	private static final String FILENAME = Constants.DIR + "reader\\test.csv";
	private InputStream inputStream;
	private FinanceDataReader reader;

	@Before
	public void setUp() throws FileNotFoundException {
		inputStream = new FileInputStream(FILENAME);
		reader = new CsvReader();	
	}
	
	@Test
	public void csvReader() throws IOException, InterruptedException {
		reader.parse(inputStream);
		
		OutputStream outExpenses = new ByteArrayOutputStream();
		OutputStream outIncome = new ByteArrayOutputStream();
		
		TestFileGenerator generator = new TestFileGenerator(outExpenses , outIncome );
		generator.generate(reader);
		
		System.out.println("expenses:");
		System.out.println(outExpenses.toString());
		
		System.out.println("income:");
		System.out.println(outIncome.toString());
		
	}
}
