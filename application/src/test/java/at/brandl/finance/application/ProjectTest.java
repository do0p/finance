package at.brandl.finance.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.common.Line;
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
		Assert.assertEquals(159,  project.getSize(null));
	}
	
	@Test
	public void changes() throws IOException {
		
		Assert.assertFalse(project.hasChanges());
		
		importData();
		Assert.assertTrue(project.hasChanges());
		
		project.markUnchanged();
		Assert.assertFalse(project.hasChanges());
		
		Line line = project.getLines(null).get(0);
		Assert.assertFalse(project.hasChanges());
		
		line.setLabel("bla");
		Assert.assertTrue(project.hasChanges());
		
		project.markUnchanged();
		Assert.assertFalse(project.hasChanges());
		
		line.setLabel("bla");
		Assert.assertFalse(project.hasChanges());
	}
	
	@Test
	public void serialize() throws IOException, ClassNotFoundException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectOutputStream os = new ObjectOutputStream(out)) {

			os.writeObject(project);
		}

		Project deserializedProject;
		try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))) {
			
			deserializedProject = (Project) is.readObject();
		}
		
		Assert.assertEquals(project.getName(), deserializedProject.getName());
	}
}
