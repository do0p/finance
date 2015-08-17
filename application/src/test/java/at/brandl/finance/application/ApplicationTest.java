package at.brandl.finance.application;

import static at.brandl.finance.application.TestProperties.getTestFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.application.error.NoProjectSelectedException;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.common.Line;

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
		application.selectProject(PROJECT_NAME, false);
		Line line = createLine();
		application.predict(line);
	}

	@Test
	public void train() throws IOException {

		application.createProject(PROJECT_NAME);
		application.selectProject(PROJECT_NAME, false);
		application.loadData(getTestFile("test.csv"));
		for(Line line : application.getLines(null)) {
			line.setTrained(true);
		}
		application.train();
	}

	@Test
	public void predict() throws IOException, InterruptedException,
			ExecutionException {

		assertCalledAfterTrain(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				Line line = createLine();
				Prediction predict = application.predict(line);
				Assert.assertNotNull(predict.getLabel());
				Assert.assertFalse(predict.getConfidence() <= 0);

				return true;
			}
		});

	}

	@Test
	public void serialize() throws IOException, InterruptedException,
			ExecutionException {

		assertCalledAfterTrain(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				String saveFile = getTestFile("save.fdt");
				application.saveToFile(saveFile);

				application = new Application();
				application.readFromFile(saveFile, false);

				Line line = createLine();
				Prediction predict = application.predict(line);
				Assert.assertNotNull(predict.getLabel());
				Assert.assertFalse(predict.getConfidence() <= 0);

				return true;

			}
		});

	}

	private void assertCalledAfterTrain(Callable<Boolean> test)
			throws IOException, InterruptedException, ExecutionException {
		
		FutureTask<Boolean> futureTask = new FutureTask<Boolean>(test);

		application.addTrainListener(new Application.TrainingListener() {

			@Override
			public void onTrainingFinished() {

				application.removeTrainListener(this);
				futureTask.run();
			}
		});

		train();

		Assert.assertTrue(futureTask.get());
	}

	private Line createLine() {

		Line line = new Line();
		line.setAmount(new BigDecimal(-20));
		line.addWord("AUTOMAT");
		line.addWord("05310014");
		line.addWord("K3");
		line.addWord("UM");
		return line;
	}

}
