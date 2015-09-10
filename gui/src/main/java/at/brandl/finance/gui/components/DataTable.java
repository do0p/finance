package at.brandl.finance.gui.components;

import static at.brandl.finance.gui.LocalizationUtil.getLocalized;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import at.brandl.finance.application.Application;
import at.brandl.finance.application.Journal;
import at.brandl.finance.common.Line;
import at.brandl.finance.gui.TrainDataPopup;
import at.brandl.finance.reader.FinanceDataReader;

public class DataTable extends Composite {

	private static final String COLUMN_NAME = "name";
	private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private static final String[] TITLES = { Journal.LABEL, Journal.CONFIDENCE,
			Journal.CONFIRMED, Journal.DATE, Journal.AMOUNT, Journal.TEXT,
			Journal.REASON };
	private static final String[] SORTABLE = { Journal.CONFIDENCE,
			Journal.DATE, Journal.AMOUNT };
	private Application application;
	private FilterComposite filtersComposite;
	private Table table;
	private BigDecimal sum = new BigDecimal(0);

	public DataTable(Shell parent, Application application) {
		super(parent, SWT.NONE);
		this.application = application;

		setLayout(new GridLayout(1, false));

		this.filtersComposite = new FilterComposite(this);
		filtersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		this.table = new Table(this, SWT.CHECK | SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);

		filtersComposite.addListener(createRefreshListener());
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		Menu menu = new Menu(this);
		table.setMenu(menu);
		
		MenuItem confirmItem = new MenuItem(menu, SWT.PUSH);
		confirmItem.setText(getLocalized("Confirm"));
		confirmItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				int[] indices = getCheckedItemIndices();
				for(int index : indices) {
					Line line = (Line) table.getItem(index).getData();
					line.setConfirmed(true);
				}
				refresh();
			}
		});

		
		MenuItem uncofirmItem = new MenuItem(menu, SWT.PUSH);
		uncofirmItem.setText(getLocalized("Unconfirm"));
		uncofirmItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				int[] indices = getCheckedItemIndices();
				for(int index : indices) {
					Line line = (Line) table.getItem(index).getData();
					line.setConfirmed(false);
				}
				refresh();
			}
		});
		
		MenuItem trainItem = new MenuItem(menu, SWT.PUSH);
		trainItem.setText(getLocalized("Train"));
		trainItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				TrainDataPopup trainDataPopup = new TrainDataPopup(parent, application, getLines(getCheckedItemIndices()));
				trainDataPopup.open();
			}
		});
		
		MenuItem removeFromTrainItem = new MenuItem(menu, SWT.PUSH);
		removeFromTrainItem.setText(getLocalized("RemoveFromTrain"));
		removeFromTrainItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				application.train();
			}
		});
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 600;
		gridData.widthHint = 800;
		table.setLayoutData(gridData);

		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				TableColumn currentColumn = (TableColumn) e.widget;
				String columnName = (String) currentColumn.getData(COLUMN_NAME);

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

		// column for checkbox
		TableColumn checkColumn = new TableColumn(table, SWT.NONE);
		checkColumn.pack();
		for (int i = 0; i < TITLES.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			String colName = TITLES[i];
			column.setText(getLocalized(colName));
			column.setData(COLUMN_NAME, colName);
			if (Journal.AMOUNT.equals(colName)
					|| Journal.CONFIDENCE.equals(colName)) {
				column.setAlignment(SWT.RIGHT);
			} else {
				column.setAlignment(SWT.LEFT);
			}

			if (Arrays.asList(SORTABLE).contains(colName)) {
				column.addListener(SWT.Selection, listener);
			}
			column.pack();
		}
	}

	public void refresh() {

		filtersComposite.setLabels(application.getLabels());

		table.removeAll();

		BigDecimal tmpSum = new BigDecimal(0);
		for (Line line : application.getLines(filtersComposite.getFilters())) {
			createItem(line);
			tmpSum = tmpSum.add(line.getAmount());
		}
		sum = tmpSum;

		for (int i = 0; i < 7; i++) {
			table.getColumn(i).pack();
		}
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

	private Listener createRefreshListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				refresh();
			}
		};
	}

	public List<Line> getSelection() {

		return getLines(table.getSelectionIndices());
	}

	public void init() {

		filtersComposite.init();

		table.removeAll();

		for (int i = 0; i < 7; i++) {
			table.getColumn(i).pack();
		}
	}

	public Collection<Line> getLines() {

		return application.getLines(filtersComposite.getFilters());
	}

	public void addTableSelectionListener(SelectionListener listener) {

		table.addSelectionListener(listener);
	}

	public int[] getCheckedItemIndices() {
		
		List<Integer> indices = new ArrayList<Integer>();
		for (TableItem item : table.getItems()) {
			if (item.getChecked()) {
				indices.add(table.indexOf(item));
			}
		}

		int[] indexArray = new int[indices.size()];
		for (int i = 0; i < indices.size(); i++) {
			indexArray[i] = indices.get(i);
		}

		return indexArray;
	}
	
	private List<Line> getLines(int[] itemIndices) {
		List<Line> lines = new ArrayList<Line>();
		for(int index : itemIndices) {
			lines.add((Line) table.getItem(index).getData());
		}
		return lines;
	}

	public BigDecimal getSum() {
		
		return sum;
	}
	
}
