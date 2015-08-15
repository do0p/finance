package at.brandl.finance.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import at.brandl.finance.application.Application;

public class CreateProjectPopup {

	public CreateProjectPopup(Display display, Application application) {
		Shell popUp = new Shell(display);
		popUp.setLayout(new RowLayout(SWT.VERTICAL));

		Text text = new Text(popUp, SWT.SINGLE);

		Button button = new Button(popUp, SWT.PUSH);
		button.setText("save");

		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				application.createProject(text.getText());
				application.selectProject(text.getText());
				popUp.close();
			}
		});

		popUp.pack();
		popUp.open();
	}

}
