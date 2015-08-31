package at.brandl.finance.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateProjectPopup extends Dialog {

	private String result;


	public CreateProjectPopup(Shell parent) {
		super(parent);
	}

	public String open() {

		Shell parent = getParent();
		Shell popUp = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		popUp.setText(getText());
		popUp.setLayout(new GridLayout(1, false));

		Text text = new Text(popUp, SWT.SINGLE);
		GridData gridData = new GridData();
		gridData.widthHint = 200;
		text.setLayoutData(gridData);
		text.setMessage("Project Name");

		Button button = new Button(popUp, SWT.PUSH);
		button.setText("save");

		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				result = text.getText();

				popUp.close();
			}
		});

		popUp.pack();
		popUp.open();

		popUp.open();
		Display display = parent.getDisplay();
		while (!popUp.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

}
