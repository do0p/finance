package at.brandl.finance.gui.components;
import static at.brandl.finance.gui.LocalizationUtil.getLocalized;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import at.brandl.finance.application.Journal.Filter;
import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class FilterComposite extends Composite {

	private class LabelFilter implements Filter {

		@Override
		public boolean accept(Line line) {

			if (StringUtils.isBlank(labelSelection.getText()) || labelSelection.getText().startsWith("-")) {
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
			return text != null && text.toLowerCase().contains(searchText.toLowerCase());
		}

	};

	private final Collection<Filter> filters = new ArrayList<Filter>();
	private Button confirmedCheck, unconfirmedCheck, expensesCheck, incomeCheck, trainingDataCheck;
	private Text searchField;
	private Combo labelSelection;

	public FilterComposite(Composite parent) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout(7, false);
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

		searchField = new Text(this, SWT.SINGLE);
		GridData searchLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		searchLayoutData.widthHint = 180;
		searchField.setLayoutData(searchLayoutData);
		searchField.setMessage(getLocalized("SearchText"));
		searchField.pack();
		
		filters.add(new LabelFilter());
		filters.add(new ExpensesFilter());
		filters.add(new ConfirmedFilter());
		filters.add(new TextFilter());

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
		labelSelection.addListener(SWT.Selection , listener);
		confirmedCheck.addListener(SWT.Selection, listener);
		unconfirmedCheck.addListener(SWT.Selection, listener);
		expensesCheck.addListener(SWT.Selection, listener);
		incomeCheck.addListener(SWT.Selection, listener);
		trainingDataCheck.addListener(SWT.Selection, listener);
		searchField.addListener(SWT.Modify, listener);
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
		if(StringUtils.isNotBlank(labelFilterText)) {
			labelSelection.setText(labelFilterText);
		} else {
			labelSelection.setText(getLocalized("SelectLabel"));
		}
	}

}
