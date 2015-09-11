package at.brandl.finance.gui.components;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import at.brandl.finance.gui.LocalizationUtil;

public class StatusBar extends Composite {

	private Label statusField;
	private Label sumField;

	public StatusBar(Composite parent) {
		super(parent, SWT.NONE);

		RowLayout layout = new RowLayout();
		layout.spacing = 12;
		setLayout(layout);

		Label sumLabel = new Label(this, SWT.NONE);
		sumLabel.setText(LocalizationUtil.getLocalized("Sum") + ":");
		
		sumField = new Label(this, SWT.SHADOW_IN | SWT.RIGHT);
		sumField.setText(" ");
		sumField.pack();

		Label statusLabel = new Label(this, SWT.NONE);
		statusLabel.setText(LocalizationUtil.getLocalized("Status") + ":");
		
		statusField = new Label(this, SWT.SHADOW_IN | SWT.RIGHT);
		statusField.setText(" ");
		statusField.pack();

		pack();
	}

	public void setStatus(String status) {
		statusField.setText(status);
		statusField.pack();
		pack();
	}

	public void setSum(BigDecimal sum) {
		sumField.setText(String.format("%,.2f", sum.doubleValue()));
		sumField.pack();
		pack();
	}

}
