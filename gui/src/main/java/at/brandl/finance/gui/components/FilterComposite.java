package at.brandl.finance.gui.components;

import static at.brandl.finance.gui.LocalizationUtil.getLocalized;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import at.brandl.finance.application.Journal.Filter;
import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class FilterComposite extends Composite {

	private final int currentYear = new GregorianCalendar().get(Calendar.YEAR);
	private final int DEFAULT_END_YEAR = currentYear;
	private final int DEFAULT_START_YEAR = currentYear;

	private class LabelFilter implements Filter {

		@Override
		public boolean accept(Line line) {

			if (StringUtils.isBlank(labelSelection.getText())
					|| labelSelection.getText().startsWith("-")) {
				return true;
			}

			return labelSelection.getText().equals(line.getLabel());
		}

	};

	private class ConfirmedFilter implements Filter {

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

	private class ExpensesFilter implements Filter {

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

	private class TextFilter implements Filter {

		@Override
		public boolean accept(Line line) {

			String searchText = searchField.getText();

			if (StringUtils.isBlank(searchText)) {
				return true;
			}

			String text = line.getText(FinanceDataReader.TEXT);
			String reason = line.getText(FinanceDataReader.REASON);

			return matches(text, searchText) || matches(reason, searchText);

		}

		private boolean matches(String text, String searchText) {
			return text != null
					&& text.toLowerCase().contains(searchText.toLowerCase());
		}

	};

	private class DateFilter implements Filter {

		@Override
		public boolean accept(Line line) {

			Date startDate = getDate(startDateField);
			Date endDate = getDate(endDateField);
			Date date = line.getDate();

			return !(date.before(startDate) || date.after(endDate));

		}

		private Date getDate(DateTime dateField) {

			int year = dateField.getYear();
			int month = dateField.getMonth();
			int day = dateField.getDay();

			Calendar cal = new GregorianCalendar();
			cal.clear();
			cal.set(year, month, day);
			return cal.getTime();
		}

	};

	private final Collection<Filter> filters = new ArrayList<Filter>();
	private Button confirmedCheck, unconfirmedCheck, expensesCheck,
			incomeCheck, trainingDataCheck;
	private DateTime startDateField, endDateField;
	private Text searchField;
	private Combo labelSelection;

	public FilterComposite(Composite parent) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout(11, false);
		layout.horizontalSpacing = 7;
		setLayout(layout);

		labelSelection = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData labelLayoutData = new GridData();
		labelLayoutData.widthHint = 180;
		labelSelection.setLayoutData(labelLayoutData);
		labelSelection.pack();

		confirmedCheck = new Button(this, SWT.CHECK);
		confirmedCheck.setText(getLocalized("Confirmed"));
		confirmedCheck.pack();

		unconfirmedCheck = new Button(this, SWT.CHECK);
		unconfirmedCheck.setText(getLocalized("Unconfirmed"));
		unconfirmedCheck.pack();

		expensesCheck = new Button(this, SWT.CHECK);
		expensesCheck.setText(getLocalized("Expenses"));
		expensesCheck.pack();

		incomeCheck = new Button(this, SWT.CHECK);
		incomeCheck.setText(getLocalized("Income"));
		incomeCheck.pack();

		trainingDataCheck = new Button(this, SWT.CHECK);
		trainingDataCheck.setText(getLocalized("TrainingSet"));
		trainingDataCheck.pack();

		Label startDateLabel = new Label(this, SWT.NONE);
		startDateLabel.setText(getLocalized("from"));
		startDateLabel.pack();
		
		startDateField = new DateTime(this, SWT.DATE | SWT.MEDIUM
				| SWT.DROP_DOWN);
		startDateField.setYear(DEFAULT_START_YEAR);
		startDateField.setMonth(0);
		startDateField.setDay(1);
		startDateField.pack();

		Label endDateLabel = new Label(this, SWT.NONE);
		endDateLabel.setText(getLocalized("to"));
		endDateLabel.pack();
		
		endDateField = new DateTime(this, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
		endDateField.setYear(DEFAULT_END_YEAR);
		endDateField.setMonth(11);
		endDateField.setDay(31);
		endDateField.pack();

		searchField = new Text(this, SWT.SINGLE);
		GridData searchLayoutData = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		searchLayoutData.widthHint = 180;
		searchField.setLayoutData(searchLayoutData);
		searchField.setMessage(getLocalized("SearchText"));
		searchField.pack();

		filters.add(new LabelFilter());
		filters.add(new ExpensesFilter());
		filters.add(new ConfirmedFilter());
		filters.add(new TextFilter());
		filters.add(new DateFilter());

		init();

		pack();

	}

	public void init() {

		labelSelection.removeAll();
		confirmedCheck.setSelection(true);
		unconfirmedCheck.setSelection(true);
		expensesCheck.setSelection(true);
		incomeCheck.setSelection(true);
		trainingDataCheck.setSelection(false);
		searchField.setText("");
	}

	public void addListener(Listener listener) {
		labelSelection.addListener(SWT.Selection, listener);
		confirmedCheck.addListener(SWT.Selection, listener);
		unconfirmedCheck.addListener(SWT.Selection, listener);
		expensesCheck.addListener(SWT.Selection, listener);
		incomeCheck.addListener(SWT.Selection, listener);
		trainingDataCheck.addListener(SWT.Selection, listener);
		searchField.addListener(SWT.Modify, listener);
		startDateField.addListener(SWT.Selection, listener);
		endDateField.addListener(SWT.Selection, listener);
	}

	public Collection<Filter> getFilters() {
		return filters;
	}

	public void setLabels(String... labels) {

		String labelFilterText = labelSelection.getText();

		labelSelection.removeAll();
		labelSelection.add(getLocalized("SelectLabel"));

		if (labels != null) {
			for (String label : labels) {
				labelSelection.add(label);
			}
		}
		if (StringUtils.isNotBlank(labelFilterText)) {
			labelSelection.setText(labelFilterText);
		} else {
			labelSelection.setText(getLocalized("SelectLabel"));
		}
	}

}
