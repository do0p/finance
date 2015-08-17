package at.brandl.finance.gui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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

	private MenuItem fileNewItem, fileOpenItem, fileSaveItem, fileExitItem;
	private MenuItem projectLoadItem, projectTrainItem;
	private ToolItem trainData, refreshData;
	private Table table;
	private Shell shell;
	private Display display;
	private Application application;
	private Combo combo;
	private Button confirmedCheck;
	private Button unconfirmedCheck;
	private Button expensesCheck;
	private Button incomeCheck;
	private Filter labelFilter;
	private Filter confirmedFilter;
	private Filter expensesFilter;

	public static void main(String[] args) {

		new Gui();
	}

	public Gui() {

		display = new Display();
		createShell();
		createMenuBar();
		createToolBar();
		createFilters();
		createTable();

		application = new Application();
		application.addTrainListener(this);

		application.addFilter(labelFilter);
		application.addFilter(confirmedFilter);
		application.addFilter(expensesFilter);

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
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		exit();
	}

	private void createFilters() {

		labelFilter = new Filter() {

			@Override
			public boolean accept(Line line) {

				if (StringUtils.isBlank(combo.getText())) {
					return true;
				}

				return combo.getText().equals(line.getLabel());
			}

		};

		confirmedFilter = new Filter() {

			@Override
			public boolean accept(Line line) {

				boolean confirmed = confirmedCheck.getSelection();
				boolean unconfirmed = unconfirmedCheck.getSelection();

				if (confirmed && unconfirmed) {
					return true;
				}

				if (!confirmed && !unconfirmed) {
					return false;
				}

				return line.isConfirmed() == confirmed;

			}
		};

		expensesFilter = new Filter() {

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

		// Composite filters = new Composite(shell, SWT.NONE);
		// filters.setLayoutData(new RowData(800, 80));

		combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.pack();

		confirmedCheck = new Button(shell, SWT.CHECK);
		confirmedCheck.setText("confirmed");
		confirmedCheck.setSelection(true);
		confirmedCheck.pack();

		unconfirmedCheck = new Button(shell, SWT.CHECK);
		unconfirmedCheck.setText("unconfirmed");
		unconfirmedCheck.setSelection(true);
		unconfirmedCheck.pack();

		expensesCheck = new Button(shell, SWT.CHECK);
		expensesCheck.setText("expenses");
		expensesCheck.setSelection(true);
		expensesCheck.pack();

		incomeCheck = new Button(shell, SWT.CHECK);
		incomeCheck.setText("income");
		incomeCheck.setSelection(true);
		incomeCheck.pack();

		combo.addListener(SWT.Selection, createRefreshListener());
		confirmedCheck.addListener(SWT.Selection, createRefreshListener());
		unconfirmedCheck.addListener(SWT.Selection, createRefreshListener());
		expensesCheck.addListener(SWT.Selection, createRefreshListener());
		incomeCheck.addListener(SWT.Selection, createRefreshListener());

		// filters.pack();

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

				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterNames(new String[] { "Finance Files" });
				dialog.setFilterExtensions(new String[] { "*.fdt" });
				dialog.setFilterPath("c:\\");
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

				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterNames(new String[] { "Finance Files" });
				dialog.setFilterExtensions(new String[] { "*.fdt" });
				dialog.setFilterPath("c:\\");

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

	private Listener createTableSetDataListener() {
		return new Listener() {

			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = table.indexOf(item);
				Line line = application.getLine(index);
				boolean labeled = line.getLabel() != null;
				item.setText(1, labeled ? line.getLabel() : "");
				item.setText(2,
						String.format("%.1f%%", line.getConfidence() * 100));
				item.setText(3, Boolean.toString(line.isConfirmed()));
				item.setText(4, dateFormat.format(line.getDate()));
				item.setText(5,
						String.format("%,.2f", line.getAmount().doubleValue()));
				item.setText(6, line.getText(FinanceDataReader.TEXT));
				item.setText(7, line.getText(FinanceDataReader.REASON));
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
					line.setConfirmed(false);
					lines.add(line);
				}
				new TrainDataPopup(display, application, lines);
			}
		};
	}

	private void createTable() {

		table = new Table(shell, SWT.VIRTUAL | SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new RowData(800, 600));
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
				table.clearAll();
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

		table.setItemCount(application.getSize());
		table.clearAll();
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

				FileDialog fileDialog = new FileDialog(shell);
				String fileName = fileDialog.open();
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
