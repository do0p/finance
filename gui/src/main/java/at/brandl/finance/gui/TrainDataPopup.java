package at.brandl.finance.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import at.brandl.finance.application.Application;
import at.brandl.finance.common.Line;
import at.brandl.finance.reader.FinanceDataReader;

public class TrainDataPopup extends Dialog {

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
	private List<Line> lines;
	private Application application;

	public TrainDataPopup(Shell parent, Application application) {

		this(parent, application, application.getUnconfirmedLines());
	
	}

	public TrainDataPopup(Shell parent, Application application, List<Line> lines) {
		super(parent, 0);
		this.lines = lines;
		this.application = application;
	}

	public Object open() {
		
		Shell parent = getParent();
		Shell popUp = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		popUp.setText(getText());
		GridLayout layout = new GridLayout(2, false);
		popUp.setLayout(layout);
		popUp.setText("Train Data");
		if (lines.isEmpty()) {
			popUp.close();
			return null;
		}

		Label dateLabel = new Label(popUp, SWT.RIGHT);
		dateLabel.setText("Date:");
		dateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		date = new Label(popUp, SWT.HORIZONTAL);

		Label amountLabel = new Label(popUp, SWT.RIGHT);
		amountLabel.setText("Amount:");
		amountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		amount = new Label(popUp, SWT.HORIZONTAL);

		Label textLabel = new Label(popUp, SWT.RIGHT);
		textLabel.setText("Text:");
		textLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		text = new Label(popUp, SWT.WRAP );
		GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		textLayoutData.widthHint = 300;
		text.setLayoutData(textLayoutData);

		Label reasonLabel = new Label(popUp, SWT.RIGHT);
		reasonLabel.setText("Reason:");
		reasonLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		reason = new Label(popUp, SWT.HORIZONTAL);
		GridData reasonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		reasonLayoutData.widthHint = 300;
		reason.setLayoutData(reasonLayoutData);

		Label confidenceLabel = new Label(popUp, SWT.RIGHT);
		confidenceLabel.setText("Confidence:");
		confidenceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		confidence = new Label(popUp, SWT.HORIZONTAL);

		Label labelLabel = new Label(popUp, SWT.RIGHT);
		labelLabel.setText("Label:");
		labelLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		combo = new Combo(popUp, SWT.DROP_DOWN);

		if (lines.size() > 1) {
			nextButton = new Button(popUp, SWT.PUSH);
			nextButton.setText("next");
			nextButton.addListener(SWT.Selection, createNextButtonListener(lines));
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
		trainButton.addListener(SWT.Selection, createTrainButtonListener(application, popUp));

		popUp.pack();
		popUp.open();
		Display display = parent.getDisplay();
		while (!popUp.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return null;
	}

	private Listener createLabelSelectionListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				String text = combo.getText();
				if (combo.getSelectionIndex() < 0 && StringUtils.isNotBlank(text)) {
					combo.add(text);
					combo.pack();
				}
			}
		};
	}

	private Listener createTrainButtonListener(Application application, Shell popUp) {
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
		confidence.setText(Integer.toString((int) (line.getConfidence() * 100)) + "%");

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
		line.setTrained(true);
		line.setConfidence(1);
	}

}
