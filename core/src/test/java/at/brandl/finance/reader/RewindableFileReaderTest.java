package at.brandl.finance.reader;

import static at.brandl.finance.utils.TestProperties.getTestFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.common.RewindableFileReader;

public class RewindableFileReaderTest {

	private static final String FILENAME = getTestFile("test.csv");
	private RewindableFileReader reader;
	
	@Before
	public void setUp() throws FileNotFoundException {
		
		reader = new RewindableFileReader(FILENAME);
	}
	
	@Test
	public void serialize() throws IOException, ClassNotFoundException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectOutputStream os = new ObjectOutputStream(out)) {

			os.writeObject(reader);
		}

		RewindableFileReader deserializedReader;
		try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))) {
			
			deserializedReader = (RewindableFileReader) is.readObject();
		}
		
		Assert.assertEquals(reader.readLine(), deserializedReader.readLine());
	}
}
