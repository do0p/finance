package at.brandl.finance.gui.components;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StatusBar extends Composite {

	private Label statusField;
	private Label sumField;

	public StatusBar(Composite parent) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		setLayout(layout);

		sumField = new Label(this, SWT.SHADOW_IN | SWT.RIGHT);
		sumField.setText(" ");
		sumField.pack();
		sumField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		statusField = new Label(this, SWT.SHADOW_IN | SWT.RIGHT);
		statusField.setText(" ");
		statusField.pack();
		statusField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		pack();

	}

	public void setStatus(String status) {
		statusField.setText(status);
	}

	public void setSum(BigDecimal sum) {
		sumField.setText(String.format("%,.2f", sum.doubleValue()));
	}

}
