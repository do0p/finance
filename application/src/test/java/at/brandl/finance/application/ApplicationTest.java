package at.brandl.finance.application;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.reader.Line;

public class ApplicationTest {

	private static final String PROJECT_NAME = "name";
	private Application application;
	
	@Before
	public void setUp() {
		application = new Application();
	}
	
	@Test
	public void project() {
		

		application.createProject(PROJECT_NAME);
		Collection<String> projects = application.getProjectNames();
		
		Assert.assertNotNull(projects);
		Assert.assertEquals(1, projects.size());
		Assert.assertTrue(projects.contains(PROJECT_NAME));
		
	}
	
	@Test(expected = NoProjectSelectedException.class)
	public void predictWithoutSelection() {
		
		Line line = createLine(null);
		application.predict(line);
	}

	@Test(expected = UntrainedProjectException.class)
	public void predictWithoutTraining() {

		application.createProject(PROJECT_NAME);
		application.selectProject(PROJECT_NAME);
		Line line = createLine(null);
		application.predict(line);
	}
	
	
	
	@Test
	public void train() {
		
		application.createProject(PROJECT_NAME);
		application.selectProject(PROJECT_NAME);
		application.train(createLine("bla"));
	}
	
	
	@Test
	public void predict() {
		
		train();
		Line line = createLine(null);
		Prediction predict = application.predict(line);
		Assert.assertEquals("bla", predict.getLabel());
		Assert.assertEquals(1, predict.getConfidence(), 0.00001);
	}
	
	private Line createLine(String label) {

		Line line = new Line();
		line.setLabel(label);
		line.setDay(1);
		line.setMonth(2);
		line.setWeekDay(3);
		line.setAmount(new BigDecimal(35));
		line.addWord("word1");
		line.addWord("word2");
		return line;
	}
	
}
