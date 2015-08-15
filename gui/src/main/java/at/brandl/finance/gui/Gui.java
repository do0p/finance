package at.brandl.finance.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import at.brandl.finance.application.Application;
import at.brandl.finance.application.Prediction;
import at.brandl.finance.application.error.UntrainedProjectException;
import at.brandl.finance.reader.Line;

public class Gui {

	static final int COUNT = 1000000;

	public Gui() {

		Application application = new Application();
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new RowLayout(SWT.VERTICAL));
		ToolBar bar = new ToolBar(shell, SWT.BORDER);

		ToolItem newProjectItem = new ToolItem(bar, SWT.PUSH);
		newProjectItem.setText("New Project");
		newProjectItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				new CreateProjectPopup(display, application);
			}
		});

		Rectangle clientArea = shell.getClientArea();
		bar.setLocation(clientArea.x, clientArea.y);
		bar.pack();

		final Table table = new Table(shell, SWT.VIRTUAL | SWT.BORDER | SWT.MULTI);
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {

				TableItem[] selection = table.getSelection();
				List<Line> lines = new ArrayList<>();
				for (TableItem item : selection) {
					Line line = (Line) item.getData();
					line.setLabel(null);
					line.setConfirmed(false);
					line.setConfidence(0);
					lines.add(line);
				}
				new TrainDataPopup(display, application, lines);
			}
		});
		table.addListener(SWT.SetData, new Listener() {

			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = table.indexOf(item);
				Line line = application.getLine(index);
				item.setText(line.toString());
				item.setData(line);
			}
		});
		table.setLayoutData(new RowData(500, 500));

		ToolItem loadData = new ToolItem(bar, SWT.PUSH);
		loadData.setText("Load Data");
		loadData.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				FileDialog fileDialog = new FileDialog(shell);
				String fileName = fileDialog.open();
				try {
					application.loadData(fileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int count = application.getNumLines();
				table.setItemCount(count);
				shell.layout();

				try {
					for (Line line : application.getUnconfirmedLines()) {
						Prediction prediction = application.predict(line);
						line.setLabel(prediction.getLabel());
						line.setConfidence(prediction.getConfidence());
					}
				} catch (UntrainedProjectException e) {
					e.printStackTrace();
				}
			}
		});

		ToolItem trainData = new ToolItem(bar, SWT.PUSH);
		trainData.setText("Train Data");
		trainData.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				new TrainDataPopup(display, application);
			}
		});

		ToolItem refreshData = new ToolItem(bar, SWT.PUSH);
		refreshData.setText("Refresh");
		refreshData.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {

				table.clearAll();
				table.setItemCount(application.getNumLines());
			}
		});

		// Button button = new Button(shell, SWT.PUSH);
		// button.setText("Add Items");
		// final Label label = new Label(shell, SWT.NONE);
		// button.addListener(SWT.Selection, new Listener() {
		//
		// public void handleEvent(Event event) {
		// long t1 = System.currentTimeMillis();
		// table.setItemCount(COUNT);
		// long t2 = System.currentTimeMillis();
		// label.setText("Items: " + COUNT + ", Time: " + (t2 - t1)
		// + " (ms)");
		// shell.layout();
		// }
		// });
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static void main(String[] args) {

		new Gui();
	}

}
