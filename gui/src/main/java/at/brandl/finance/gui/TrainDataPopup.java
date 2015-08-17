package at.brandl.finance.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import at.brandl.finance.application.Application;
import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class TrainDataPopup {

	private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private Line line;
	private Label amount;
	private Label date;
	private Label text;
	private Label reason;
	private Label confidence;
	private Combo combo;
	private Button trainButton;
	private Button nextButton;

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

		date = new Label(popUp, SWT.HORIZONTAL);
		amount = new Label(popUp, SWT.HORIZONTAL);
		text = new Label(popUp, SWT.HORIZONTAL);
		reason = new Label(popUp, SWT.HORIZONTAL);
		confidence = new Label(popUp, SWT.HORIZONTAL);

		combo = new Combo(popUp, SWT.DROP_DOWN);

		if (lines.size() > 1) {
			nextButton = new Button(popUp, SWT.PUSH);
			nextButton.setText("next");
			nextButton.addListener(SWT.Selection,
					createNextButtonListener(lines));
		}

		trainButton = new Button(popUp, SWT.PUSH);
		trainButton.setText("train");

		String[] labels = application.getLabels();
		if (labels != null) {
			for (String label : labels) {
				combo.add(label);
			}
		}

		line = lines.get(0);
		updateGui();

		combo.addListener(SWT.FocusOut, createLabelSelectionListener());
		trainButton.addListener(SWT.Selection,
				createTrainButtonListener(application, popUp));

		popUp.pack();
		popUp.open();
	}

	private Listener createLabelSelectionListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				String text = combo.getText();
				if (combo.getSelectionIndex() < 0
						&& StringUtils.isNotBlank(text)) {
					combo.add(text);
					combo.pack();
				}
			}
		};
	}

	private Listener createTrainButtonListener(Application application,
			Shell popUp) {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				if (StringUtils.isNotBlank(combo.getText())) {
					updateLine();
				}

				application.train();

				popUp.close();
			}
		};
	}

	private Listener createNextButtonListener(List<Line> lines) {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				if (StringUtils.isBlank(combo.getText())) {
					return;
				}

				if (lines.size() > 1) {

					updateLine();
					lines.remove(0);
					line = lines.get(0);
					updateGui();

				} else {

					nextButton.setEnabled(false);
				}
			}

		};
	}

	private void updateGui() {

		long rounded = FastMath.round(line.getAmount().doubleValue() * 100);
		amount.setText(Double.toString(rounded / 100d));
		date.setText(dateFormat.format(line.getDate()));
		text.setText(line.getText(FinanceDataReader.TEXT));
		reason.setText(line.getText(FinanceDataReader.REASON));
		confidence.setText(Integer.toString((int) (line.getConfidence() * 100))
				+ "%");

		date.pack();
		amount.pack();
		text.pack();
		reason.pack();
		confidence.pack();
		
		if (line.getLabel() != null) {
			combo.setText(line.getLabel());
		}
	}

	private void updateLine() {
		line.setLabel(combo.getText());
		line.setConfirmed(true);
		line.setConfidence(1);
	}

}
