package at.brandl.finance.gui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import at.brandl.finance.application.Application;
import at.brandl.finance.application.Application.TrainingListener;
import at.brandl.finance.application.Journal;
import at.brandl.finance.application.Journal.Filter;
import at.brandl.finance.application.Prediction;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class Gui implements TrainingListener {

	private static final String[] TITLES = { " ", Journal.LABEL,
			Journal.CONFIDENCE, Journal.CONFIRMED, Journal.DATE,
			Journal.AMOUNT, Journal.TEXT, Journal.REASON };
	private static final String[] SORTABLE = { Journal.CONFIDENCE,
			Journal.DATE, Journal.AMOUNT };

	private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private final Collection<Filter> filters = new ArrayList<Filter>();

	private final Display display;
	private final Shell shell;
	private final Application application;
	private final Table table;

	private MenuItem fileNewItem, fileOpenItem, fileSaveItem, fileExitItem,
			projectLoadItem, projectTrainItem, projectExportCsv;
	private ToolItem confirmAll, refreshData;
	private Button confirmedCheck, unconfirmedCheck, expensesCheck,
			incomeCheck, trainingDataCheck;
	private Combo combo;
	private Composite filtersComposite;

	public static void main(String[] args) {

		new Gui();
	}

	public Gui() {

		display = new Display();
		shell = createShell();

		createMenuBar();
		createToolBar();
		createFilters();
		table = createTable();

		application = new Application();
		application.addTrainListener(this);

		table.addListener(SWT.Selection, createTableSelectionListener());

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
		// trainData.addListener(SWT.Selection, trainDataListener);
		projectTrainItem.addListener(SWT.Selection, trainDataListener);

		projectExportCsv.addListener(SWT.Selection, createExportCsvListener());

		confirmAll.addListener(SWT.Selection, createConfirmAllListener());

		refreshData.addListener(SWT.Selection, createRefreshListener());

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		exit();
	}

	private Listener createExportCsvListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				FileDialog dialog = createFileDialog(SWT.SAVE, "CSV", ".csv");
				dialog.setFileName(application.getProjectName() + ".csv");

				String open = dialog.open();
				if (StringUtils.isNotBlank(open)) {
					application.exportCsvToFile(open);
				}

			}
		};
	}

	private Listener createConfirmAllListener() {

		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				for (Line line : application.getLines(filters)) {

					line.setConfirmed(true);
					line.setConfidence(1d);
				}
				refresh();
			}
		};
	}

	private void createFilters() {

		Filter labelFilter = new Filter() {

			@Override
			public boolean accept(Line line) {

				if (StringUtils.isBlank(combo.getText())) {
					return true;
				}

				return combo.getText().equals(line.getLabel());
			}

		};

		Filter confirmedFilter = new Filter() {

			@Override
			public boolean accept(Line line) {

				boolean confirmed = confirmedCheck.getSelection();
				boolean unconfirmed = unconfirmedCheck.getSelection();
				boolean trainingData = trainingDataCheck.getSelection();

				if (trainingData) {
					return line.isTrained();
				}

				if (confirmed && unconfirmed) {
					return true;
				}

				if (!confirmed && !unconfirmed) {
					return false;
				}

				return line.isConfirmed() == confirmed;

			}
		};

		Filter expensesFilter = new Filter() {

			@Override
			public boolean accept(Line line) {

				boolean expenses = expensesCheck.getSelection();
				boolean income = incomeCheck.getSelection();

				if (expenses && income) {
					return true;
				}

				if (!expenses && !income) {
					return false;
				}

				return line.isExpense() == expenses;

			}
		};

		filters.add(labelFilter);
		filters.add(confirmedFilter);
		filters.add(expensesFilter);

		filtersComposite = new Composite(shell, SWT.NONE);
		filtersComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		combo = new Combo(filtersComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.pack();

		confirmedCheck = new Button(filtersComposite, SWT.CHECK);
		confirmedCheck.setText("confirmed");
		confirmedCheck.setSelection(true);
		confirmedCheck.pack();

		unconfirmedCheck = new Button(filtersComposite, SWT.CHECK);
		unconfirmedCheck.setText("unconfirmed");
		unconfirmedCheck.setSelection(true);
		unconfirmedCheck.pack();

		expensesCheck = new Button(filtersComposite, SWT.CHECK);
		expensesCheck.setText("expenses");
		expensesCheck.setSelection(true);
		expensesCheck.pack();

		incomeCheck = new Button(filtersComposite, SWT.CHECK);
		incomeCheck.setText("income");
		incomeCheck.setSelection(true);
		incomeCheck.pack();

		trainingDataCheck = new Button(filtersComposite, SWT.CHECK);
		trainingDataCheck.setText("training set");
		trainingDataCheck.setSelection(false);
		trainingDataCheck.pack();

		combo.addListener(SWT.Selection, createRefreshListener());
		confirmedCheck.addListener(SWT.Selection, createRefreshListener());
		unconfirmedCheck.addListener(SWT.Selection, createRefreshListener());
		expensesCheck.addListener(SWT.Selection, createRefreshListener());
		incomeCheck.addListener(SWT.Selection, createRefreshListener());
		trainingDataCheck.addListener(SWT.Selection, createRefreshListener());

		filtersComposite.pack();

	}

	private Listener createExitListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				exit();
			}

		};
	}

	private void exit() {
		display.dispose();
		System.exit(0);
	}

	private Listener createFileSaveSelectionListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				FileDialog dialog = createFileDialog(SWT.SAVE,  "Finance Files", "*.fdt");
				dialog.setFileName(application.getProjectName() + ".fdt");

				String open = dialog.open();
				if (StringUtils.isNotBlank(open)) {
					application.saveToFile(open);
				}

			}
		};
	}

	private Listener createFileOpenSelectionListener() {

		return new Listener() {
			public void handleEvent(Event event) {

				FileDialog dialog = createFileDialog(SWT.OPEN,  "Finance Files", "*.fdt");


				String open = dialog.open();
				if (StringUtils.isNotBlank(open)) {
					application.readFromFile(open, false);

					refresh();
				}
			}
		};
	}

	@Override
	public void onTrainingFinished() {

		for (Line line : application.getUnconfirmedLines()) {
			Prediction prediction = application.predict(line);
			line.setLabel(prediction.getLabel());
			line.setConfidence(prediction.getConfidence());
		}
		application.sort();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refresh();
			}
		});

	}

	private void createItem(Line line) {
		TableItem item = new TableItem(table, SWT.NONE);
		boolean labeled = line.getLabel() != null;
		item.setText(1, labeled ? line.getLabel() : "");
		item.setText(2, String.format("%.1f%%", line.getConfidence() * 100));
		item.setText(3, Boolean.toString(line.isConfirmed()));
		item.setText(4, dateFormat.format(line.getDate()));
		item.setText(5, String.format("%,.2f", line.getAmount().doubleValue()));
		item.setText(6, line.getText(FinanceDataReader.TEXT));
		item.setText(7, line.getText(FinanceDataReader.REASON));
		item.setData(line);
	}

	private Listener createTableSelectionListener() {
		return new Listener() {
			public void handleEvent(Event e) {

				TableItem[] selection = table.getSelection();
				List<Line> lines = new ArrayList<>();
				for (TableItem item : selection) {
					lines.add((Line) item.getData());
				}
				new TrainDataPopup(display, application, lines);
			}
		};
	}

	private Table createTable() {

		Table table = new Table(shell, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 600;
		gridData.widthHint = 800;
		table.setLayoutData(gridData);
		for (int i = 0; i < TITLES.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(TITLES[i]);
			if (Journal.AMOUNT.equals(column.getText())
					|| Journal.CONFIDENCE.equals(column.getText())) {
				column.setAlignment(SWT.RIGHT);
			} else {
				column.setAlignment(SWT.LEFT);
			}
			if (Arrays.asList(SORTABLE).contains(column.getText())) {
				column.addListener(SWT.Selection, createTableSortListener());
			}
			column.pack();
		}
		return table;
	}

	private Listener createTableSortListener() {

		return new Listener() {
			@Override
			public void handleEvent(Event e) {
				TableColumn currentColumn = (TableColumn) e.widget;
				String columnName = currentColumn.getText();

				// determine new sort column and direction
				TableColumn sortColumn = table.getSortColumn();
				int dir = table.getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					table.setSortColumn(currentColumn);
					dir = SWT.UP;
				}

				application.setSort(columnName, dir == SWT.UP);

				// update data displayed in table
				table.setSortDirection(dir);
				refresh();
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

		String labelFilterText = combo.getText();

		combo.removeAll();
		combo.add(" ");
		String[] labels = application.getLabels();
		if (labels != null) {
			for (String label : labels) {
				combo.add(label);
			}
		}
		combo.setText(labelFilterText);
		combo.pack();

		filtersComposite.pack();
		
		table.removeAll();
		for (Line line : application.getLines(filters)) {
			createItem(line);
		}

		for (int i = 0; i < 7; i++) {
			table.getColumn(i).pack();
		}
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

				FileDialog dialog = createFileDialog(SWT.OPEN, "CSV", "*.csv");

				String fileName = dialog.open();
				if (StringUtils.isBlank(fileName)) {
					return;
				}
				try {
					application.loadData(fileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// int count = application.getSize();
				// table.setItemCount(count);
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

				application.sort();
				refresh();
			}

			

		};
	}

	private FileDialog createFileDialog(int style, String filterName,
			String extension) {
		FileDialog dialog = new FileDialog(shell, style);
		dialog.setFilterNames(new String[] { filterName });
		dialog.setFilterExtensions(new String[] { extension });
		dialog.setFilterPath("c:\\");
		return dialog;
	}
	
	private Listener createProjectListener() {

		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				new CreateProjectPopup(display, application);
			}
		};
	}

	private Shell createShell() {

		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Finance Data Reader");
		return shell;
	}

	private void createToolBar() {

		ToolBar toolBar = new ToolBar(shell, SWT.BORDER);

		// trainData = new ToolItem(toolBar, SWT.PUSH);
		// trainData.setText("Train Data");

		confirmAll = new ToolItem(toolBar, SWT.PUSH);
		confirmAll.setText("Confirm All");

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

		projectExportCsv = new MenuItem(projectMenu, SWT.PUSH);
		projectExportCsv.setText("&Export CSV");

		return projectMenu;
	}

}
