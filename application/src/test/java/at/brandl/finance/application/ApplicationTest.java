package at.brandl.finance.application;

import static at.brandl.finance.application.TestProperties.getTestFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.application.error.NoProjectSelectedException;
import at.brandl.finance.application.error.UntrainedProjectException;
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

		Line line = createLine();
		application.predict(line);
	}

	@Test(expected = UntrainedProjectException.class)
	public void predictWithoutTraining() {

		application.createProject(PROJECT_NAME);
		application.selectProject(PROJECT_NAME);
		Line line = createLine();
		application.predict(line);
	}

	@Test
	public void train() throws IOException {

		application.createProject(PROJECT_NAME);
		application.selectProject(PROJECT_NAME);
		application.loadData(getTestFile("test.csv"));
		application.confirmAllLabeled();
		application.train();
	}

	@Test
	public void predict() throws IOException {

		train();
		Line line = createLine();
		Prediction predict = application.predict(line);
		Assert.assertNotNull(predict.getLabel());
		Assert.assertFalse(predict.getConfidence() <= 0);
	}

	private Line createLine() {

		Line line = new Line();
		line.setDay(6);
		line.setMonth(2);
		line.setWeekDay(4);
		line.setAmount(new BigDecimal(-20));
		line.addWord("AUTOMAT");
		line.addWord("05310014");
		line.addWord("K3");
		line.addWord("UM");
		return line;
	}

}
