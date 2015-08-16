package at.brandl.finance.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import at.brandl.finance.application.Application;
import at.brandl.finance.application.Application.TrainingListener;
import at.brandl.finance.application.Prediction;
import at.brandl.finance.application.Project;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.reader.Line;

public class Gui implements TrainingListener {

	static final int COUNT = 1000000;
	private MenuItem fileNewItem, fileOpenItem, fileSaveItem, fileExitItem;
	private MenuItem projectLoadItem, projectTrainItem;
	private ToolItem  trainData, refreshData;
	private Table table;
	private Shell shell;
	private Display display;
	private Application application;

	public static void main(String[] args) {

		new Gui();
	}

	public Gui() {

		display = new Display();
		createShell();
		createMenuBar();
		createToolBar();
		createTable();

		application = new Application();
		application.addTrainListener(this);

		table.addListener(SWT.Selection, createTableSelectionListener());
		table.addListener(SWT.SetData, createTableSetDataListener());

		Listener newProjectListener = createProjectListener();
		fileNewItem.addListener(SWT.Selection, newProjectListener);

		fileSaveItem.addListener(SWT.Selection,
				createFileSaveSelectionListener());
		fileOpenItem.addListener(SWT.Selection,
				createFileOpenSelectionListener());
		fileExitItem.addListener(SWT.Selection, createExitListener());

		Listener loadDataListener = createLoadDataListener();
		projectLoadItem.addListener(SWT.Selection, loadDataListener);

		Listener trainDataListener = createTrainDataListener();
		trainData.addListener(SWT.Selection, trainDataListener);
		projectTrainItem.addListener(SWT.Selection, trainDataListener);

		refreshData.addListener(SWT.Selection, createRefreshListener());

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private Listener createExitListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				display.dispose();
			}
		};
	}

	private Listener createFileSaveSelectionListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterNames(new String[] { "Finance Files" });
				dialog.setFilterExtensions(new String[] { "*.fdt" });
				dialog.setFilterPath("c:\\");
				dialog.setFileName(application.getProjectName() + ".fdt");

				application.saveToFile(dialog.open());

			}
		};
	}

	private Listener createFileOpenSelectionListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterNames(new String[] { "Finance Files" });
				dialog.setFilterExtensions(new String[] { "*.fdt" });
				dialog.setFilterPath("c:\\");

				application.readFromFile(dialog.open(), false);
				
				refresh();
			}
		};
	}

	@Override
	public void finished(Project trainedProject) {

		for (Line line : application.getUnconfirmedLines()) {
			Prediction prediction = application.predict(line);
			line.setLabel(prediction.getLabel());
			line.setConfidence(prediction.getConfidence());
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refresh();
			}
		});

	}

	private Listener createTableSetDataListener() {
		return new Listener() {

			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = table.indexOf(item);
				Line line = application.getLine(index);
				item.setText(line.toString());
				item.setData(line);
			}
		};
	}

	private Listener createTableSelectionListener() {
		return new Listener() {
			public void handleEvent(Event e) {

				TableItem[] selection = table.getSelection();
				List<Line> lines = new ArrayList<>();
				for (TableItem item : selection) {
					Line line = (Line) item.getData();
					line.setLabel(null);
					line.setConfirmed(false);
					line.setConfidence(0);
					lines.add(line);
				}
				new TrainDataPopup(display, application, lines);
			}
		};
	}

	private Listener createRefreshListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				refresh();
			}
		};
	}

	private void refresh() {

		table.clearAll();
		table.setItemCount(application.getNumLines());
	}

	private Listener createTrainDataListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				new TrainDataPopup(display, application);
			}
		};
	}

	private Listener createLoadDataListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				FileDialog fileDialog = new FileDialog(shell);
				String fileName = fileDialog.open();
				try {
					application.loadData(fileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int count = application.getNumLines();
				table.setItemCount(count);
				shell.layout();

				try {
					for (Line line : application.getUnlabeledLines()) {
						Prediction prediction = application.predict(line);
						line.setLabel(prediction.getLabel());
						line.setConfidence(prediction.getConfidence());
					}
				} catch (UntrainedProjectException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private Listener createProjectListener() {

		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				new CreateProjectPopup(display, application);
			}
		};
	}

	private void createShell() {

		shell = new Shell(display);
		shell.setLayout(new RowLayout(SWT.VERTICAL));
	}

	private void createTable() {

		table = new Table(shell, SWT.VIRTUAL | SWT.BORDER | SWT.MULTI);
		table.setLayoutData(new RowData(500, 500));
	}

	private void createToolBar() {

		ToolBar toolBar = new ToolBar(shell, SWT.BORDER);

		trainData = new ToolItem(toolBar, SWT.PUSH);
		trainData.setText("Train Data");

		refreshData = new ToolItem(toolBar, SWT.PUSH);
		refreshData.setText("Refresh");

		Rectangle clientArea = shell.getClientArea();
		toolBar.setLocation(clientArea.x, clientArea.y);
		toolBar.pack();
	}

	private void createMenuBar() {
		Menu menuBar = new Menu(shell, SWT.BAR);

		Menu fileMenu = createFileMenu();
		Menu projectMenu = createProjectMenu();

		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");
		fileMenuHeader.setMenu(fileMenu);

		MenuItem projectMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		projectMenuHeader.setText("&Project");
		projectMenuHeader.setMenu(projectMenu);

		shell.setMenuBar(menuBar);
	}

	private Menu createFileMenu() {

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);

		fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewItem.setText("&New");

		fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open");

		fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText("&Save");

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");

		return fileMenu;
	}

	private Menu createProjectMenu() {

		Menu projectMenu = new Menu(shell, SWT.DROP_DOWN);

		projectLoadItem = new MenuItem(projectMenu, SWT.PUSH);
		projectLoadItem.setText("&Load");

		projectTrainItem = new MenuItem(projectMenu, SWT.PUSH);
		projectTrainItem.setText("&Train");

		return projectMenu;
	}

}
