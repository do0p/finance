package at.brandl.finance.gui;

import static at.brandl.finance.gui.LocalizationUtil.getLocalized;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import at.brandl.finance.application.Application;
import at.brandl.finance.application.Application.TrainingListener;
import at.brandl.finance.application.error.NoProjectSelectedException;
import at.brandl.finance.application.error.ProjectWithUnsafedChangesException;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.common.Line;
import at.brandl.finance.gui.components.DataTable;
import at.brandl.finance.gui.components.StatusBar;
import at.brandl.finance.gui.components.DataTable.StatusListener;

public class Gui implements TrainingListener, StatusListener {

	private Shell shell;
	private Application application;
	private DataTable table;
	private StatusBar statusBar;

	public Gui() {

		Display display = new Display();

		createShell(display);
		createApplication();

		createMenuBar();
		createToolBar();

		createTable();

		createStatusBar();

		try {
			run(display);
		} finally {
			display.dispose();
			application.close();
		}
	}

	public static void main(String[] args) {

		new Gui();
	}

	private void trainSelected() {

		TrainDataPopup trainDataPopup = new TrainDataPopup(shell, application,
				table.getSelection());
		trainDataPopup.open();
	}

	private void trainAll() {

		TrainDataPopup trainDataPopup = new TrainDataPopup(shell, application);
		trainDataPopup.open();
	}

	private void export() {

		FileDialog dialog = createFileDialog(SWT.SAVE, "CSV", ".csv");
		dialog.setFileName(application.getProjectName() + ".csv");

		String open = dialog.open();
		if (StringUtils.isNotBlank(open)) {
			application.exportCsvToFile(open);
		}
	}

	private void open() {

		FileDialog dialog = createFileDialog(SWT.OPEN, "Finance Files", "*.fdt");

		String open = dialog.open();
		if (StringUtils.isNotBlank(open)) {
			application.readFromFile(open, false);
			refresh();
		}
	}

	private void confirmAll() {

		for (Line line : table.getLines()) {
			line.setConfirmed(true);
			line.setConfidence(1d);
		}
		refresh();
	}

	private void createProject() {

		CreateProjectPopup createProjectPopup = new CreateProjectPopup(shell);
		createProjectPopup.setText(getLocalized("NewProject"));
		String text = createProjectPopup.open();

		if (StringUtils.isNotBlank(text)) {
			application.createProject(text);
			application.selectProject(text, false);
			table.init();
		}
	}

	private void loadData() {

		FileDialog dialog = createFileDialog(SWT.OPEN, "CSV", "*.csv");
		String fileName = dialog.open();

		if (StringUtils.isNotBlank(fileName)) {
			try {
				application.loadData(fileName);

				for (Line line : application.getUnlabeledLines()) {
					application.predict(line);
				}
				
			} catch (UntrainedProjectException e) {
				// ignore this case
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			refresh();
		}
	}

	private void createNoProjectMessage() {

		MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
		dialog.setText("No Project Selected");
		dialog.setMessage("A project must be open to proceed this action.");
		dialog.open();
	}

	private void createUnsafedProjectMessage() {

		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES
				| SWT.NO);
		dialog.setText("Unsafed Project");
		dialog.setMessage("There are unsafed changes. Save current project?");
		int answer = dialog.open();
		if (answer == SWT.YES) {
			save();
		} else {
			application.releaseProject();
		}
	}

	private void save() {

		FileDialog dialog = createFileDialog(SWT.SAVE, "Finance Files", "*.fdt");
		dialog.setFileName(application.getProjectName() + ".fdt");
		String open = dialog.open();

		if (StringUtils.isNotBlank(open)) {
			application.saveToFile(open);
		}
	}

	private void refresh() {
		
		table.refresh();
	}


	@Override
	public void onTrainingStarted() {
		
		if (application.isTrainingRunning()) {
			statusBar.setStatus(getLocalized("InProgress"));
			statusBar.pack();
		}
	}

	
	@Override
	public void onTrainingFinished() {

		for (Line line : application.getUnconfirmedLines()) {
			application.predict(line);
		}

		application.sort();

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refresh();
			}
		});
	}


	@Override
	public void onStatusChanged() {
	
		statusBar.setSum(table.getSum());
		if (!application.isTrainingRunning()) {
			statusBar.setStatus(" ");
			statusBar.pack();
		}
	}
	
	private FileDialog createFileDialog(int style, String filterName,
			String extension) {

		FileDialog dialog = new FileDialog(shell, style);
		dialog.setFilterNames(new String[] { filterName });
		dialog.setFilterExtensions(new String[] { extension });
		dialog.setFilterPath("c:\\");
		return dialog;
	}

	private void handleUnsafedProject() {

		if (application.hasChanges()) {
			createUnsafedProjectMessage();
		}
	}

	private void createShell(Display display) {

		shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Finance Data Reader");
	}

	private void createApplication() {

		application = new Application();
		application.addTrainListener(this);
	}

	private void createStatusBar() {

		statusBar = new StatusBar(shell);
		statusBar.setStatus(" ");
		statusBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		statusBar.pack();
	}

	private void createTable() {

		table = new DataTable(shell, application);
		table.addTableSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				//
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				trainSelected();
			}
		});
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.addStatusListener(this);
		table.pack();
	}

	private void createMenuBar() {

		Menu menuBar = new Menu(shell, SWT.BAR);
		createMenuItem(menuBar, getLocalized("&File"), createFileMenu());
		createMenuItem(menuBar, getLocalized("&Project"), createProjectMenu());
		shell.setMenuBar(menuBar);
	}

	private void createMenuItem(Menu menuBar, String text, Menu menu) {

		MenuItem menuItem = new MenuItem(menuBar, SWT.CASCADE);
		menuItem.setText(text);
		menuItem.setMenu(menu);
	}

	private Menu createProjectMenu() {

		Menu projectMenu = new Menu(shell, SWT.DROP_DOWN);

		createMenuItem(projectMenu, getLocalized("&Load"), new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				loadData();
			}
		});

		createMenuItem(projectMenu, getLocalized("&Train"), new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				trainAll();
			}
		});

		createMenuItem(projectMenu, getLocalized("&ExportCSV"), new Listener() {
			public void handleEvent(Event event) {
				export();
			}
		});

		return projectMenu;
	}

	private Menu createFileMenu() {

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);

		createMenuItem(fileMenu, getLocalized("&New"), new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				handleUnsafedProject();
				createProject();
			}
		});

		createMenuItem(fileMenu, getLocalized("&Open"), new Listener() {
			public void handleEvent(Event event) {
				handleUnsafedProject();
				open();
			}
		});

		createMenuItem(fileMenu, getLocalized("&Save"), new Listener() {
			public void handleEvent(Event event) {
				save();
			}
		});

		createMenuItem(fileMenu, getLocalized("E&xit"), new Listener() {
			public void handleEvent(Event event) {
				handleUnsafedProject();
				shell.dispose();
			}
		});

		return fileMenu;
	}

	private void createToolBar() {

		ToolBar toolBar = new ToolBar(shell, SWT.BORDER);
		Rectangle clientArea = shell.getClientArea();
		toolBar.setLocation(clientArea.x, clientArea.y);

		createToolItem(toolBar, getLocalized("ConfirmAll"), new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				confirmAll();
			}
		});

		createToolItem(toolBar, getLocalized("Refresh"), new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				refresh();
			}

		});

		toolBar.pack();
	}

	private void createToolItem(ToolBar toolBar, String text, Listener listener) {

		ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
		toolItem.setText(text);
		toolItem.addListener(SWT.Selection, listener);
	}

	private void createMenuItem(Menu menu, String text, Listener listener) {

		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(text);
		menuItem.addListener(SWT.Selection, listener);
	}

	private void run(Display display) {

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (NoProjectSelectedException e) {
				createNoProjectMessage();
				table.init();
			} catch (ProjectWithUnsafedChangesException e) {
				createUnsafedProjectMessage();
			} catch (RuntimeException e) {

				e.printStackTrace(System.err);
				display.dispose();
			}
		}
	}
}
