/**
 * 
 */
package org.ecsoya.lib60870.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.IConnection;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public abstract class BaseApplication implements IConnection {

	private String title;

	private Text traceText;

	private Text consoleText;

	private Display display;
	private Shell shell;

	private boolean isStarted = false;

	public BaseApplication(String title) {
		this.title = title;
	}

	public Composite createContent(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		SashForm top = new SashForm(sashForm, SWT.HORIZONTAL);

		Group settingGroup = new Group(top, SWT.NONE);
		settingGroup.setText("Settings");
		settingGroup.setLayout(new FillLayout());
		createSettingGroup(settingGroup);

		Group runningGroup = new Group(top, SWT.NONE);
		runningGroup.setText("Run");
		runningGroup.setLayout(new FillLayout());
		createRunningGroup(runningGroup);

		top.setSashWidth(2);
		top.setWeights(new int[] { 2, 1 });

		SashForm bottom = new SashForm(sashForm, SWT.VERTICAL);

		Group asduGroup = new Group(bottom, SWT.NONE);
		asduGroup.setText("ASDU");
		asduGroup.setLayout(new FillLayout());
		createAsduGroup(asduGroup);

		Group traceGroup = new Group(bottom, SWT.NONE);
		traceGroup.setText("Trace");
		traceGroup.setLayout(new FillLayout());
		createTraceGroup(traceGroup);

		sashForm.setSashWidth(2);
		sashForm.setWeights(new int[] { 1, 3 });

		return sashForm;
	}

	protected Composite createSettingGroup(Composite parent) {

		return null;
	}

	protected Composite createRunningGroup(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(2, true));

		Label label = new Label(control, SWT.NONE);
		label.setText("Port");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 1, 1));

		Text text = new Text(control, SWT.BORDER);
		text.setMessage("2404");
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		Button startButton = new Button(control, SWT.NONE);
		startButton.setText("Start");
		startButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		startButton.setEnabled(!isStarted);
		parent.getShell().setDefaultButton(startButton);

		Button stopButton = new Button(control, SWT.NONE);
		stopButton.setText("Stop");
		stopButton.addListener(SWT.Selection, (event) -> {
			try {
				stop();

				isStarted = true;

				startButton.setEnabled(true);
				stopButton.setEnabled(false);
			} catch (ConnectionException e) {
				console(e);
			}
		});
		stopButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		stopButton.setEnabled(isStarted);

		startButton.addListener(SWT.Selection, (event) -> {
			try {
				start();
				isStarted = true;
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
			} catch (ConnectionException e) {
				console(e);
			}
		});

		return control;
	}

	protected Composite createAsduGroup(Composite parent) {

		return null;
	}

	protected Composite createTraceGroup(Composite parent) {
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);

		traceText = new Text(form, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		display.asyncExec(() -> {
			traceText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			traceText.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		});

		Menu menu = new Menu(traceText);
		MenuItem clearItem = new MenuItem(menu, SWT.NONE);
		clearItem.setText("Clear");
		clearItem.addListener(SWT.Selection, (e) -> traceText.setText(""));
		traceText.setMenu(menu);

		consoleText = new Text(form, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		display.asyncExec(() -> {
			consoleText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			consoleText.setForeground(display.getSystemColor(SWT.COLOR_RED));
		});
		menu = new Menu(consoleText);
		clearItem = new MenuItem(menu, SWT.NONE);
		clearItem.setText("Clear");
		clearItem.addListener(SWT.Selection, (e) -> consoleText.setText(""));
		form.setWeights(new int[] { 1, 1 });
		return form;
	}

	protected void trace(String msg) {
		if (msg == null) {
			return;
		}
		if (traceText != null && !traceText.isDisposed()) {
			traceText.getDisplay().asyncExec(() -> {
				traceText.append(msg + "\n");
			});
		}
	}

	protected void console(String message) {
		if (message == null || consoleText == null || consoleText.isDisposed()) {
			return;
		}
		consoleText.getDisplay().asyncExec(() -> {
			consoleText.append(message + "\n");
		});
	}

	protected void console(Exception e) {
		if (e == null) {
			return;
		}
		console(e.getMessage());
	}

	public final void open() {
		display = new Display();

		shell = new Shell(display);
		shell.setLayout(new FillLayout());

		shell.setMenuBar(createMenuBar(shell));

		createContent(shell);

		shell.setText(getTitle());

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	protected Menu createMenuBar(Shell shell) {
		Menu menuBar = new Menu(shell, SWT.BAR);

		MenuItem fileMenu = new MenuItem(menuBar, SWT.CASCADE);
		fileMenu.setText("&File");

		Menu fileDropDown = new Menu(shell, SWT.DROP_DOWN);
		MenuItem exitMenu = new MenuItem(fileDropDown, SWT.NONE);
		exitMenu.setText("&Exit");
		exitMenu.addListener(SWT.Selection, (event) -> shell.close());
		fileMenu.setMenu(fileDropDown);

		return menuBar;
	}

	protected String getTitle() {
		return title;
	}

}
