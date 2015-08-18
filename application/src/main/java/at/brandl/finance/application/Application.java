package at.brandl.finance.application;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import at.brandl.finance.application.Journal.Filter;
import at.brandl.finance.application.error.NoConfirmedLinesException;
import at.brandl.finance.application.error.NoProjectSelectedException;
import at.brandl.finance.application.error.NoSuchProjectFoundException;
import at.brandl.finance.application.error.OpenProjectFailedException;
import at.brandl.finance.application.error.ProjectWithUnsafedChangesException;
import at.brandl.finance.application.error.SaveProjectFailedException;
import at.brandl.finance.application.error.TrainingFailedException;
import at.brandl.finance.application.error.UnknownProjectFileFormatException;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.common.Data;
import at.brandl.finance.common.Line;
import at.brandl.finance.common.RewindableReader;
import at.brandl.finance.common.RewindableStringReader;
import at.brandl.finance.core.Core;
import at.brandl.finance.core.Scale;
import at.brandl.finance.core.linear.LinearCore;
import at.brandl.finance.core.linear.LinearModel;
import at.brandl.finance.reader.CsvReader;
import at.brandl.finance.reader.NodeGenerator;

public class Application {

	public static interface ProjectSelectionListener {
		
		void onProjectSelection();
		
	}
	
	public static interface TrainingListener {

		void onTrainingFinished();
	}

	private static final double LOWER = -1;
	private static final double UPPER = 1;
	private final Map<String, Project> projects = new HashMap<>();
	private final Collection<TrainingListener> trainingListeners = new CopyOnWriteArrayList<>();
	private final Collection<ProjectSelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private final Executor executor = Executors.newSingleThreadExecutor();
	private final Core<LinearModel> core = new LinearCore();
	private Project project;

	public void createProject(String projectName) {

		projects.put(projectName, new Project(projectName));
	}

	public Collection<String> getProjectNames() {

		return projects.keySet();
	}

	public void selectProject(String projectName, boolean force) {

		if (project != null) {
			if (!force && project.hasChanges()) {
				throw new ProjectWithUnsafedChangesException();
			}
			project.release();

		}

		project = projects.get(projectName);

		if (project == null) {
			throw new NoSuchProjectFoundException(projectName);
		}

		
		for(ProjectSelectionListener listener : selectionListeners) {
			
			listener.onProjectSelection();
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
			Data data = new Scale(reader, null, restore, LOWER, UPPER).scale();
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

		List<Line> lines = project.getTrainedLines();

		if (lines.isEmpty()) {
			throw new NoConfirmedLinesException();
		}

		try (StringWriter out = new StringWriter()) {

			NodeGenerator nodeGenerator = new NodeGenerator();
			List<String> nodeStrings = nodeGenerator.createNodeStrings(lines);
			BufferedWriter saveFile = new BufferedWriter(out);
			RewindableStringReader reader = createReader(nodeStrings);
			Data data = new Scale(reader, saveFile, null, LOWER, UPPER).scale();

			FutureTask<LinearModel> future = scheduleTraining(data);

			notifyListners(future, nodeGenerator, out);

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

	public int getSize(Collection<Filter> filters) {

		assertProjectSelected();
		return project.getSize(filters);
	}

	public String[] getLabels() {

		assertProjectSelected();
		return project.getLabels();
	}

	public List<Line> getUnconfirmedLines() {

		assertProjectSelected();

		return project.getUnconfirmedLines();
	}

	public void saveToFile(String filename) {

		assertProjectSelected();

		try (OutputStream os = new GZIPOutputStream(new FileOutputStream(
				filename))) {

			project.markUnchanged();
			new ObjectOutputStream(os).writeObject(project);
		} catch (IOException e) {
			throw new SaveProjectFailedException(e);
		}
	}

	
	public void exportCsvToFile(String filename) {
		
		assertProjectSelected();

		try (OutputStream os = new FileOutputStream(
				filename)) {

			new OutputStreamWriter(os).write(project.toCsv());
			
		} catch (IOException e) {
			throw new SaveProjectFailedException(e);
		}
	}
	
	public void readFromFile(String filename, boolean force) {

		Project project;
		try (InputStream is = new GZIPInputStream(new FileInputStream(filename))) {

			project = (Project) new ObjectInputStream(is).readObject();
	
		} catch (FileNotFoundException e) {

			throw new OpenProjectFailedException(e);

		} catch (Exception e) {

			throw new UnknownProjectFileFormatException(e);
		}
		projects.put(project.getName(), project);
		selectProject(project.getName(), false);
	}

	public void addTrainListener(TrainingListener trainingListener) {

		trainingListeners.add(trainingListener);
	}

	public void removeTrainListener(TrainingListener trainingListener) {

		trainingListeners.remove(trainingListener);
	}
	
	public void addSelectionListener(ProjectSelectionListener selectionListener) {

		selectionListeners.add(selectionListener);
	}

	public void removeSelectionListener(ProjectSelectionListener selectionListener) {

		selectionListeners.remove(selectionListener);
	}

	public String getProjectName() {

		assertProjectSelected();
		return project.getName();
	}

	public void setSort(String column, boolean up) {

		assertProjectSelected();
		project.setSort(column, up);
	}

	public void sort() {

		assertProjectSelected();
		project.sort();
	}


	private FutureTask<LinearModel> scheduleTraining(Data data) {
		FutureTask<LinearModel> future = new FutureTask<LinearModel>(
				new Callable<LinearModel>() {

					@Override
					public LinearModel call() throws Exception {

						return core.train(data);
					}
				});

		executor.execute(future);
		return future;
	}

	private void notifyListners(FutureTask<LinearModel> future,
			NodeGenerator nodeGenerator, StringWriter out) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				LinearModel model;
				try {
					model = future.get();
				} catch (Exception e) {

					throw new TrainingFailedException(e);
				}

				project.setModel(model);
				project.setLabels(nodeGenerator.getLabels());
				project.setWordFeatures(nodeGenerator.getWordFeatures());
				project.setRestore(createRestore(out));

				for (TrainingListener listener : trainingListeners) {
					listener.onTrainingFinished();
				}

			}
		}).start();

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

	public List<Line> getLines(Collection<Filter> filters) {

		return project.getLines(filters);
	}



}
