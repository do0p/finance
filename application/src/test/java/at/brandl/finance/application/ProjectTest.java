package at.brandl.finance.application;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.reader.CsvReader;

public class ProjectTest {

	private static final String FILE_NAME = "test.csv";
	private Project project;

	@Before
	public void setUp() {

		project = new Project("bla");
	}

	@Test
	public void importData() throws IOException {

		try (InputStream is = getClass().getClassLoader().getResourceAsStream(FILE_NAME)) {
			CsvReader reader = new CsvReader();
			reader.parse(is);
			project.readData(reader);
		}
		Journal journal = project.getJournal();
		Assert.assertEquals(159, journal.size());
	}
}
