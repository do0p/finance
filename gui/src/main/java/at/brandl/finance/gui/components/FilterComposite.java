package at.brandl.finance.gui.components;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import at.brandl.finance.application.Journal.Filter;
import at.brandl.finance.common.Line;

public class FilterComposite extends Composite {

	private class LabelFilter implements Filter {

		@Override
		public boolean accept(Line line) {

			if (StringUtils.isBlank(labelSelection.getText())) {
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

	private final Collection<Filter> filters = new ArrayList<Filter>();
	private Button confirmedCheck, unconfirmedCheck, expensesCheck, incomeCheck, trainingDataCheck;
	private Combo labelSelection;

	public FilterComposite(Composite parent) {
		super(parent, SWT.NONE);

		setLayout(new RowLayout(SWT.HORIZONTAL));

		labelSelection = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		labelSelection.pack();

		confirmedCheck = new Button(this, SWT.CHECK);
		confirmedCheck.setText("confirmed");
		confirmedCheck.pack();

		unconfirmedCheck = new Button(this, SWT.CHECK);
		unconfirmedCheck.setText("unconfirmed");
		unconfirmedCheck.pack();

		expensesCheck = new Button(this, SWT.CHECK);
		expensesCheck.setText("expenses");
		expensesCheck.pack();

		incomeCheck = new Button(this, SWT.CHECK);
		incomeCheck.setText("income");
		incomeCheck.pack();

		trainingDataCheck = new Button(this, SWT.CHECK);
		trainingDataCheck.setText("training set");
		trainingDataCheck.pack();

		filters.add(new LabelFilter());
		filters.add(new ExpensesFilter());
		filters.add(new ConfirmedFilter());

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
	}

	public void addListener(int eventType, Listener listener) {
		labelSelection.addListener(eventType, listener);
		confirmedCheck.addListener(eventType, listener);
		unconfirmedCheck.addListener(eventType, listener);
		expensesCheck.addListener(eventType, listener);
		incomeCheck.addListener(eventType, listener);
		trainingDataCheck.addListener(eventType, listener);
	}

	public Collection<Filter> getFilters() {
		return filters;
	}

	public void setLabels(String... labels) {
		
		String labelFilterText = labelSelection.getText();

		labelSelection.removeAll();
		labelSelection.add(" ");
	
		if (labels != null) {
			for (String label : labels) {
				labelSelection.add(label);
			}
		}
		labelSelection.setText(labelFilterText);
		labelSelection.pack();
		pack();
	}

}
