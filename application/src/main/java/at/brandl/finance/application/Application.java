package at.brandl.finance.application;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.brandl.finance.application.error.NoProjectSelectedException;
import at.brandl.finance.application.error.NoSuchProjectFoundException;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.common.Data;
import at.brandl.finance.common.RewindableReader;
import at.brandl.finance.common.RewindableStringReader;
import at.brandl.finance.core.Core;
import at.brandl.finance.core.Scale;
import at.brandl.finance.core.linear.LinearCore;
import at.brandl.finance.core.linear.LinearModel;
import at.brandl.finance.reader.CsvReader;
import at.brandl.finance.reader.Line;
import at.brandl.finance.reader.NodeGenerator;

public class Application {

	private static final double LOWER = -1;
	private static final double UPPER = 1;
	private final Map<String, Project> projects = new HashMap<>();
	private final Scale scale = new Scale();
	private final Core<LinearModel> core = new LinearCore();
	private Project project;

	public void createProject(String projectName) {

		projects.put(projectName, new Project(projectName));
	}

	public Collection<String> getProjectNames() {

		return projects.keySet();
	}

	public void selectProject(String projectName) {

		project = projects.get(projectName);
		if (project == null) {
			throw new NoSuchProjectFoundException(projectName);
		}
	}

	public Prediction predict(Line line) {

		assertProjectSelected();

		String[] labels = project.getLabels();
		String[] wordFeatures = project.getWordFeatures();
		RewindableReader restore = project.getRestore();
		LinearModel model = project.getModel();

		if (labels == null || wordFeatures == null || restore == null
				|| model == null) {
			throw new UntrainedProjectException();
		}

		RewindableReader reader = createReader(line, wordFeatures);
		try {
			Data data = scale.scale(reader, null, restore, LOWER, UPPER);
			double[] predict = core.predict(model, data.getNodeSets().get(0));
			String label = labels[(int) predict[0]];
			double confidence = predict[1];

			return new Prediction(label, confidence);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void train() {

		assertProjectSelected();

		List<Line> labeled = project.getConfirmedLines();

		try (StringWriter out = new StringWriter()) {

			BufferedWriter saveFile = new BufferedWriter(out);
			NodeGenerator nodeGenerator = new NodeGenerator();
			RewindableStringReader reader = createReader(nodeGenerator
					.createNodeStrings(labeled));

			Data data = scale.scale(reader, saveFile, null, LOWER, UPPER);
			LinearModel model = core.train(data);

			project.setModel(model);
			project.setLabels(nodeGenerator.getLabels());
			project.setWordFeatures(nodeGenerator.getWordFeatures());
			project.setRestore(createRestore(out));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int loadData(String fileName) throws IOException {

		assertProjectSelected();

		try (InputStream is = new FileInputStream(fileName)) {
			CsvReader reader = new CsvReader();
			reader.parse(is);
			return project.readData(reader);
		}
	}

	public void confirmAllLabeled() {

		assertProjectSelected();
		
		project.confirmAllLabeled();
	}

	public List<Line> getUnlabeledLines() {

		assertProjectSelected();

		return project.getUnlabeledLines();
	}

	private void assertProjectSelected() {

		if (project == null) {
			throw new NoProjectSelectedException();
		}
	}

	private RewindableStringReader createReader(List<String> nodeStrings) {

		RewindableStringReader reader = new RewindableStringReader();
		for (String nodeString : nodeStrings) {
			reader.addLine(nodeString);
		}
		return reader;
	}

	private RewindableStringReader createRestore(StringWriter out) {

		RewindableStringReader restore = new RewindableStringReader();
		String[] linesStrs = out.getBuffer().toString().split("\n");
		for (String lineStr : linesStrs) {
			restore.addLine(lineStr);
		}
		return restore;
	}

	private RewindableReader createReader(Line line, String[] wordFeatures) {

		RewindableStringReader reader = new RewindableStringReader();
		String nodeStr = new NodeGenerator().createNodeString(line,
				wordFeatures);
		reader.addLine(nodeStr);
		return reader;
	}

	public Line getLine(int index) {

		assertProjectSelected();
		return project.getLine(index);
	}

	public int getNumLines() {

		assertProjectSelected();
		return project.getNumLines();
	}

	public String[] getLabels() {

		return project.getLabels();
	}

	public List<Line> getUnconfirmedLines() {

		assertProjectSelected();

		return project.getUnconfirmedLines();
	}

}
