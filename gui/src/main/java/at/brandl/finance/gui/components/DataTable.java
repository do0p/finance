package at.brandl.finance.gui.components;
import static at.brandl.finance.gui.LocalizationUtil.getLocalized;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import at.brandl.finance.application.Application;
import at.brandl.finance.application.Journal;
import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class DataTable extends Composite {

	private static final String COLUMN_NAME = "name";
	private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private static final String[] TITLES = { " ", Journal.LABEL, Journal.CONFIDENCE, Journal.CONFIRMED, Journal.DATE,
			Journal.AMOUNT, Journal.TEXT, Journal.REASON };
	private static final String[] SORTABLE = { Journal.CONFIDENCE, Journal.DATE, Journal.AMOUNT };
	private Application application;
	private FilterComposite filtersComposite;
	private Table table;

	public DataTable(Shell parent, Application application) {
		super(parent, SWT.NONE);
		this.application = application;
		
		setLayout(new GridLayout(1, false));
		
		this.filtersComposite = new FilterComposite(this);
		filtersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.table = new Table(this, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

		filtersComposite.addListener(createRefreshListener());
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

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

		for (int i = 0; i < TITLES.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(getLocalized(TITLES[i]));
			column.setData(COLUMN_NAME, TITLES[i]);
			if (Journal.AMOUNT.equals(column.getText()) || Journal.CONFIDENCE.equals(column.getText())) {
				column.setAlignment(SWT.RIGHT);
			} else {
				column.setAlignment(SWT.LEFT);
			}

			if (Arrays.asList(SORTABLE).contains(column.getText())) {
				column.addListener(SWT.Selection, listener);
			}
			column.pack();
		}
	}

	public void refresh() {

		filtersComposite.setLabels(application.getLabels());

		table.removeAll();

		for (Line line : application.getLines(filtersComposite.getFilters())) {
			createItem(line);
		}

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

		List<Line> lines = new ArrayList<>();
		for (TableItem item : table.getSelection()) {
			lines.add((Line) item.getData());
		}
		return lines;
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

	public void addTableSelectionListener(Listener listener) {

		table.addListener(SWT.Selection, listener);
	}

}
