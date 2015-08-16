package at.brandl.finance.gui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import at.brandl.finance.application.Application;
import at.brandl.finance.reader.Line;

public class TrainDataPopup {

	public TrainDataPopup(Display display, Application application) {
		this(display, application, application.getUnconfirmedLines());
	}

	public TrainDataPopup(Display display, Application application,
			List<Line> lines) {

		Shell popUp = new Shell(display);
		popUp.setLayout(new RowLayout(SWT.VERTICAL));
		if (lines.isEmpty()) {
			popUp.close();
			return;
		}

		Label lineDesc = new Label(popUp, SWT.HORIZONTAL);
		lineDesc.setText(lines.get(0).toString());

		Combo combo = new Combo(popUp, SWT.DROP_DOWN);

		String[] labels = application.getLabels();
		if (labels != null) {
			for (String label : labels) {
				combo.add(label);
			}
		}

		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String text = combo.getText();
				if (combo.getSelectionIndex() < 0
						&& StringUtils.isNotBlank(text)) {
					combo.add(text);
					popUp.layout();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				String text = combo.getText();
				if (combo.getSelectionIndex() < 0
						&& StringUtils.isNotBlank(text)) {
					combo.add(text);
					popUp.layout();
				}
			}
		});

		Button nextButton = new Button(popUp, SWT.PUSH);
		nextButton.setText("next");
		nextButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if (StringUtils.isBlank(combo.getText())) {
					return;
				}

				if (lines.size() > 1) {
					lines.get(0).setLabel(combo.getText());
					lines.get(0).setConfirmed(true);
					lines.get(0).setConfidence(1);
					lines.remove(0);

					lineDesc.setText(lines.get(0).toString());
					if (lines.get(0).getLabel() != null) {
						combo.setText(lines.get(0).getLabel());
					}
				} else {
					nextButton.setEnabled(false);
				}
			}
		});

		Button trainButton = new Button(popUp, SWT.PUSH);
		trainButton.setText("train");
		trainButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if (StringUtils.isNotBlank(combo.getText())) {

					lines.get(0).setLabel(combo.getText());
					lines.get(0).setConfirmed(true);
				}

				application.train();

				popUp.close();
			}
		});

		popUp.pack();
		popUp.open();
	}

}
