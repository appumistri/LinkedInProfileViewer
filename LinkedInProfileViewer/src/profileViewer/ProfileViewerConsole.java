package profileViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class ProfileViewerConsole {

	protected Shell shlCesLinkedinProfile;
	private Text LinkedInUsernameTxt;
	private Text passwordTxt;
	private Text leadsExcelPathTxt;
	private Text outputLog;
	private Text sheetNameTxt;
	private Combo columnNumberCmb;

	private Thread viewerProcess;
	private ProfileViewerProcess pvp;

	public static StringBuilder log = new StringBuilder();

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ProfileViewerConsole window = new ProfileViewerConsole();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlCesLinkedinProfile.open();
		shlCesLinkedinProfile.layout();
		while (!shlCesLinkedinProfile.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlCesLinkedinProfile = new Shell();
		shlCesLinkedinProfile.setImage(
				SWTResourceManager.getImage(ProfileViewerConsole.class, "/profileViewer/resources/CES_logo.png"));
		shlCesLinkedinProfile.setSize(635, 460);
		shlCesLinkedinProfile.setText("CES LinkedIn Profile Viewer");

		Group inputGroup = new Group(shlCesLinkedinProfile, SWT.NONE);
		inputGroup.setText("Inputs");
		inputGroup.setBounds(10, 10, 599, 209);

		Label linkedInUsernameLbl = new Label(inputGroup, SWT.NONE);
		linkedInUsernameLbl.setBounds(10, 20, 181, 27);
		linkedInUsernameLbl.setText("LinkedIn Username");

		LinkedInUsernameTxt = new Text(inputGroup, SWT.BORDER);
		LinkedInUsernameTxt.setToolTipText("Username");
		LinkedInUsernameTxt.setBounds(197, 20, 392, 27);

		Label passwordLbl = new Label(inputGroup, SWT.NONE);
		passwordLbl.setText("Password");
		passwordLbl.setBounds(10, 53, 181, 27);

		passwordTxt = new Text(inputGroup, SWT.BORDER | SWT.PASSWORD);
		passwordTxt.setToolTipText("Password");
		passwordTxt.setBounds(197, 53, 392, 27);

		Label leadsExcelLbl = new Label(inputGroup, SWT.NONE);
		leadsExcelLbl.setText("Leads Excel");
		leadsExcelLbl.setBounds(10, 86, 181, 27);

		leadsExcelPathTxt = new Text(inputGroup, SWT.BORDER);
		leadsExcelPathTxt.setToolTipText("Leads Excel Path");
		leadsExcelPathTxt.setBounds(197, 86, 304, 27);

		Button browseLeadsExcel = new Button(inputGroup, SWT.NONE);
		browseLeadsExcel.setBounds(507, 86, 82, 27);
		browseLeadsExcel.setText("Browse");
		browseLeadsExcel.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				String filePath = selectFile();
				if (filePath != null) {
					leadsExcelPathTxt.setText(filePath);
				}
			}
		});

		Label SheetNameLbl = new Label(inputGroup, SWT.NONE);
		SheetNameLbl.setText("Sheet Name and Column number");
		SheetNameLbl.setBounds(10, 119, 181, 27);

		sheetNameTxt = new Text(inputGroup, SWT.BORDER);
		sheetNameTxt.setToolTipText("Sheet Name");
		sheetNameTxt.setBounds(197, 119, 304, 27);

		String[] items = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", };
		columnNumberCmb = new Combo(inputGroup, SWT.NONE);
		columnNumberCmb.setToolTipText("Comumn Number");
		columnNumberCmb.setBounds(507, 119, 82, 27);
		columnNumberCmb.setItems(items);

		Button startBtn = new Button(inputGroup, SWT.NONE);
		startBtn.setBounds(197, 166, 181, 33);
		startBtn.setText("Start");
		startBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				String btnName = startBtn.getText();
				if (btnName.equalsIgnoreCase("start")) {
					startBtn.setText("Stop");
					viewerProcess(true);
				} else {
					viewerProcess(false);
					startBtn.setText("Start");
				}
			}
		});

		Group outputGroup = new Group(shlCesLinkedinProfile, SWT.NONE);
		outputGroup.setText("Logs");
		outputGroup.setBounds(10, 225, 599, 187);

		outputLog = new Text(outputGroup, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		outputLog.setBounds(10, 24, 575, 153);
		outputLog.setEditable(false);
		outputLog.setEnabled(true);
	}

	public String selectFile() {
		FileDialog dialog = new FileDialog(shlCesLinkedinProfile, SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.xlsx", "*.xls" });
		dialog.setFilterPath(System.getProperty("uder.dir"));
		String filePath = dialog.open();
		return filePath;
	}

	public void emptyInputWarning() {
		MessageBox messageBox = new MessageBox(shlCesLinkedinProfile, SWT.ICON_WARNING | SWT.OK);
		messageBox.setText("Warning");
		messageBox.setMessage("Please provide inputs in Username, Password and Excel fields.");
		messageBox.open();
	}

	@SuppressWarnings("deprecation")
	public void viewerProcess(boolean run) {
		if (run) {
			String userName = LinkedInUsernameTxt.getText();
			String password = passwordTxt.getText();
			String leadsExcelPath = leadsExcelPathTxt.getText();
			String sheetName = sheetNameTxt.getText();
			String columnNumber = columnNumberCmb.getText();

			if (userName.trim().isEmpty() || password.trim().isEmpty() || leadsExcelPath.trim().isEmpty()
					|| sheetName.trim().isEmpty() || columnNumber.trim().isEmpty()) {
				emptyInputWarning();
				return;
			}

			pvp = new ProfileViewerProcess();
			pvp.setValues(userName, password, leadsExcelPath, sheetName, columnNumber);
			viewerProcess = new Thread(pvp);
			viewerProcess.start();
		} else {
			pvp.stopViewer();
			outputLog.setText(log.toString()); 
			viewerProcess.stop();
		}
	}
}
