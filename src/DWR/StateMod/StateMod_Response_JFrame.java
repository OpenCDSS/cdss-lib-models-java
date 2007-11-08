//------------------------------------------------------------------------------
// StateMod_Response_JFrame - dialog to manage response file in a worksheet 
//	format
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 19 Oct 1999	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to GUIUtil.  Add finalize().
//					Remove import *.
// 13 Aug 2001	SAM, RTi		Change the numbers in the first column
//					so the control file is the first number.
// 2001-12-11	SAM, RTi		Change help key to "SMGUI.Response"
//					(erroneously had big picture key
//					before).
// 2003-06-27	SAM, RTi		* Change so the explanation at the top
//					  is enabled but not editable(before
//					  was disabled and therefore gray - hard
//					  to read).
//					* Change "Save" button to "OK"
//					  consistent with the reset of the GUI
//					  since files are not actually saved
//					  here.
//------------------------------------------------------------------------------
// 2003-09-10	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMResponseFrame.
// 2003-09-18	JTS, RTi		Data set directory information is now
//					shown at the top of the form.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-13	SAM, RTi		* If a file name changes, set the
//					  response file component to dirty and
//					  also set the component with the file
//					  to dirty.
//					* Comment out the help button.
//					* Use the factory to get the file
//					  chooser because of the Java bug.
//					* Change OK to Close, add Apply, and
//					  enable/disable components depending
//					  on whether something has changed.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-08-16	JTS, RTi		The response file is now marked dirty
//					if any of the other filenames changed.
// 2004-08-25	JTS, RTi		Revised the GUI setup.
// 2006-03-04	SAM, RTi		Fix bug where the filename for the
//					worksheet was being requested using the
//					wrong column.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a GUI that displays a table of all the data set components,
their description and their filename.  Data set components that are dirty
can have their filenames changed.  Others cannot.
*/
public class StateMod_Response_JFrame extends JFrame 
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
String labels for the buttons.
*/
private final String 
	__BUTTON_APPLY = "Apply",
	__BUTTON_BROWSE = "Browse...",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_HELP = "Help",
	__BUTTON_CLOSE = "Close";

/**
The worksheet displayed in the gui.
*/
private JWorksheet __worksheet;

private boolean __something_changed = false;	// Indicates if a file name
						// changed.  This is used to
						// know whether to show the user
						// more information at Close.

/**
GUI buttons.
*/
private SimpleJButton
	__apply_JButton,
	__close_JButton,
	__browse_JButton,
	__cancel_JButton;

/**
The dataset for which to display the data set components in the gui.
*/
private StateMod_DataSet __dataset;

private StateMod_DataSet __dataset_copy;	// This will be edited in the
						// worksheet and will be
						// committed to __dataset only
						// when apply occurs.
/**
The dataset's window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The table model of the JWorksheet in the gui.
*/
private StateMod_Response_TableModel __tableModel;

/** 
Constructor.
@param dataset the dataset that is being worked with.
@param dataset_wm the dataset's window manager
*/
public StateMod_Response_JFrame(StateMod_DataSet dataset,
StateMod_DataSet_WindowManager dataset_wm) {
	StateMod_GUIUtil.setTitle(this, dataset, "Response File Contents",null);

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());

	__dataset = dataset;
	__dataset_copy = new StateMod_DataSet ( dataset, false );
	__dataset_wm = dataset_wm;

	setupGUI();
}

/**
Responds to action performed events.
@param ae the ActionEvent that occurred.
*/
public void actionPerformed(ActionEvent ae) {
	String action = ae.getActionCommand();

	if (action.equals(__BUTTON_BROWSE)) {
		String file = browseForFile();
		if (file.trim().equals("")) {
			return;
		}
		__tableModel.setValueAt(file, __worksheet.getSelectedRow(), 
			StateMod_Response_TableModel.COL_NAME);
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveData ();
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		closeWindow();
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		if ( saveData () ) {
			closeWindow();
		}
	}
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP(JTS - 2003-09-10)
	}
}

private String browseForFile() {
	JGUIUtil.setWaitCursor(this, true);
	String directory = JGUIUtil.getLastFileDialogDirectory();

	JFileChooser fc = JFileChooserFactory.createJFileChooser ( directory );

	String compName = __tableModel.getComponentName(
		__worksheet.getSelectedRow());
	
	String ext = __dataset.getComponentFileExtension(
		__tableModel.getComponentTypeForRow(
		__worksheet.getSelectedRow()));

	fc.setDialogTitle("Select " + compName + " File");
	SimpleFileFilter ff = new SimpleFileFilter(ext, compName + " files");
	fc.addChoosableFileFilter(ff);
	fc.setAcceptAllFileFilterUsed(true);
	fc.setDialogType(JFileChooser.OPEN_DIALOG); 
	fc.setFileFilter(ff);

	JGUIUtil.setWaitCursor(this, false);
	int retVal = fc.showOpenDialog(this);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return "";
	}

	String currDir = (fc.getCurrentDirectory()).toString();

	if (!currDir.equalsIgnoreCase(directory)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	String filename = fc.getSelectedFile().getName();

	return currDir + File.separator + filename;
}

/**
Checks the text fields for validity before they are saved back into the
data object.
@return 0 if the text fields are okay, 1 if fatal errors exist, and -1 if only
non-fatal errors exist.
*/
private int checkInput()
{	String routine = "StateMod_Response_JFrame.checkInput";
	
	String warning = "";
	int fatal_count = 0;

	// Check to make sure that no two files have the same name...

	int size = __worksheet.getModel().getRowCount();
	DataSetComponent comp = null, comp2 = null;
	String file_name, file_name2;
	for ( int i = 0; i < size; i++ ) {
		// Get the component corresponding to the line.
		comp = __dataset_copy.getComponentForComponentType (
			((StateMod_Response_TableModel)__worksheet.getModel()).
			getComponentTypeForRow ( i ) );
		file_name = ((String)
			__worksheet.getValueAt(i,
			StateMod_Response_TableModel.COL_NAME )).trim();
		if (	file_name.equalsIgnoreCase(
			__dataset_copy.BLANK_FILE_NAME) && comp.hasData() ) {
			if (	comp.getComponentType() !=
				StateMod_DataSet.COMP_NETWORK ) {
				warning += "\n" + comp.getComponentName() +
				" has data but no file name is specified.";
				++fatal_count;
			}
		}
		// Check for duplicate file names.  In particular with the new
		// response file format, there is no need for "dum" empty files.
		for ( int j = 0; j < size; j++ ) {
			if ( i == j ) {
				continue;
			}
			comp2 = __dataset_copy.getComponentForComponentType (
				((StateMod_Response_TableModel)
				__worksheet.getModel()).
				getComponentTypeForRow ( j ) );
			file_name2 = ((String)
				__worksheet.getValueAt(j,
				StateMod_Response_TableModel.COL_NAME)).
				trim();
			if (	file_name2.equalsIgnoreCase(file_name) &&
				!file_name2.equalsIgnoreCase(
				__dataset_copy.BLANK_FILE_NAME) ) {
				// Stream gage and stream estimate stations can
				// be the same file name...
				if (	((comp.getComponentType() ==
					StateMod_DataSet.
					COMP_STREAMGAGE_STATIONS) &&
					(comp2.getComponentType() ==
					StateMod_DataSet.
					COMP_STREAMESTIMATE_STATIONS)) ||
					((comp.getComponentType() ==
					StateMod_DataSet.
					COMP_STREAMESTIMATE_STATIONS) &&
					(comp2.getComponentType() ==
					StateMod_DataSet.
					COMP_STREAMGAGE_STATIONS)) ) {
					// REVISIT SAM 2006-03-04
					// Need to finalize how the gage and
					// estimate files are handled.
					//&&
					//!__dataset_copy.isFreeFormat() ) {
					// No need for a warning because the
					// single file is split internally when
					// read...
				}
				else {	warning += "\n" +
					comp.getComponentName() +
					" file name (" + file_name +
					") is the same as another component.";
					++fatal_count;
				}
				// No need to look at more files...
				break;
			}
		}
		// Compare against the original copy...
		// Warn that time series file names cannot be changed from the
		// original because time series were not read in...
		comp2 = __dataset.getComponentForComponentType (
			comp.getComponentType() );
		file_name2 = comp2.getDataFileName();
		if (	!file_name.equals(file_name2) &&
			!__dataset.areTSRead() &&
			__dataset.isDynamicTSComponent(
			comp.getComponentType()) ) {
			warning += "\n" +
			comp.getComponentName() +
			" time series were not read in - " +
			"cannot change file name.";
			++fatal_count;
		}
	}

	if ( warning.length() > 0 ) {
		warning = "\nResponse file:  " +
			warning + "\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, warning, this );
		if ( fatal_count > 0 ) {
			// Fatal errors...
			return 1;
		}
		else {	// Nonfatal errors...
			return -1;
		}
	}
	else {	// No errors...
		return 0;
	}
}

/**
Closes the window, using the data set window manager to handle all the closing
operations.
*/
protected void closeWindow()
{	if ( __something_changed ) {
		new ResponseJDialog(this,
			"Response File Contents",
			"Changing file names here will not actually save " +
			"files.\n" +
			"Use the File...Save menu to save files, which will " +
			"reflect data and file name changes.",
			ResponseJDialog.OK);
	}
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.WINDOW_RESPONSE);
	}
	else {	JGUIUtil.close ( this );
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__worksheet = null;
	__close_JButton = null;
	__cancel_JButton = null;
	__dataset = null;
	__dataset_wm = null;
	__browse_JButton = null;
	__tableModel = null;
	super.finalize();
}

private void checkBrowseButtonState() {
	if (__worksheet.getSelectedRowCount() > 0) {
		__browse_JButton.setEnabled(true);
	}
	else {
		__browse_JButton.setEnabled(false);
	}
}

public void keyPressed(KeyEvent event) {}
public void keyReleased(KeyEvent event) {
	checkBrowseButtonState();
}
public void keyTyped(KeyEvent event) {}

public void mouseEntered(MouseEvent event) {}
public void mouseExited(MouseEvent event) {}
public void mouseReleased(MouseEvent event) {
	checkBrowseButtonState();
}
public void mousePressed(MouseEvent event) {}
public void mouseClicked(MouseEvent event) {}

/**
Save the data, meaning save the file names in the data components.
*/
private boolean saveData ()
{	boolean dirty = false;	// No file name changes detected.
	if ( checkInput() == 1 ) {
		return false;	// No save because of errors.
	}
	// Set the file names back into the components and mark the components
	// dirty if the name has changed.
	int size = __worksheet.getModel().getRowCount();
	DataSetComponent comp2 = null;
	String file_name, file_name2;
	int comp_type;
	for ( int i = 0; i < size; i++ ) {
		// Get the component corresponding to the line...
		comp_type = ((StateMod_Response_TableModel)__worksheet.
				getModel()).getComponentTypeForRow ( i );
		file_name = ((String)
			__worksheet.getValueAt(i,
			StateMod_Response_TableModel.COL_NAME )).trim();
		// Get the component from the original data...
		comp2 =__dataset.getComponentForComponentType ( comp_type);
		file_name2 = comp2.getDataFileName();
		// Compare the old and new and set if different...
		if ( !file_name.equals(file_name2) ) {
			comp2.setDataFileName ( file_name );
			dirty = true;
			comp2.setDirty ( true );
			__something_changed = true;
		}
	}

	if (dirty) {
		__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESPONSE)
			.setDirty(true);
	}
	
	return true;
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "StateMod_Response_JFrame.setupGUI";

	addWindowListener(this);

	PropList p = new PropList("StateMod_Response_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {	// Operate on a copy of the table model...
		__tableModel = new
			StateMod_Response_TableModel(__dataset_copy);
		StateMod_Response_CellRenderer crr = new
			StateMod_Response_CellRenderer(__tableModel);
	
		jsw = new JScrollWorksheet(crr, __tableModel, p);
		__worksheet = jsw.getJWorksheet();

		__tableModel.setWorksheet(__worksheet);

		widths = crr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.addMouseListener(this);
	__worksheet.addKeyListener(this);
	__worksheet.setHourglassJFrame(this);

	/* TODO SAM 2007-03-01 Evaluate logic
	boolean renameAllowed = false;
	if (__tableModel != null) {
		renameAllowed = true;
	}
	*/
	
	JPanel top_panel = new JPanel();
	top_panel.setLayout(new GridBagLayout());
	int y = 0;
	JGUIUtil.addComponent(top_panel, 
		new JLabel("To rename a data set component, select a row and "
			+ "either type a new file name or use the Browse "
			+ "button."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(top_panel, 
		new JLabel("If ARE DATA MODIFIED? is YES, data for the "
			+ "component have been modified byt the file has not "
			+ "been written"),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	JGUIUtil.addComponent(top_panel, 
		new JLabel("Consequently, StateMod will not recognize the "
			+ "changes until the data are saved with File...Save."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(top_panel, 
		new JLabel("The file with the original filename will remain "
			+ "even after the new file is saved."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(top_panel, new JLabel("Data set base name"
		+ " (from *.rsp):  "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
	JGUIUtil.addComponent(top_panel, new JLabel(__dataset.getBaseName()),
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(top_panel, new JLabel("Data set directory:  "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
	JGUIUtil.addComponent(top_panel, 
		new JLabel(__dataset.getDataSetDirectory()),
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	getContentPane().add("North", top_panel);

	getContentPane().add("Center", jsw);

	JPanel button_panel = new JPanel();

	__browse_JButton = new SimpleJButton(__BUTTON_BROWSE, this);
	__browse_JButton.setEnabled(false);
	__browse_JButton.setToolTipText("Select an existing file.");
	button_panel.add(__browse_JButton);
	__apply_JButton = new SimpleJButton(__BUTTON_APPLY, this);
	//__apply_JButton.setEnabled(false);
	__apply_JButton.setToolTipText("Set the file names.  File...Save is "
		+ "still required to save files.");
	button_panel.add(__apply_JButton);
	__cancel_JButton = new SimpleJButton(__BUTTON_CANCEL, this);
	__cancel_JButton.setToolTipText("Cancel file name changes.");
	button_panel.add(__cancel_JButton);
	__close_JButton = new SimpleJButton(__BUTTON_CLOSE, this);
	__close_JButton.setToolTipText("Apply file name changes and close "
		+ "window.  File...Save is still required to save files.");
	//__close_JButton.setEnabled (false);
	button_panel.add(__close_JButton);
	//__help_JButton = new SimpleJButton(__BUTTON_HELP, this);
	//__help_JButton.setEnabled(false);
	//button_panel.add(__help_JButton);

	JPanel bottom_panel = new JPanel();
	bottom_panel.setLayout(new BorderLayout());
	getContentPane().add("South", bottom_panel);
	bottom_panel.add("South", button_panel);

	pack();

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
			StateMod_DataSet_WindowManager.WINDOW_RESPONSE, this );
	}

	setSize(700, 500);
	JGUIUtil.center(this);

	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
}

/**
Responds to Window Activated events; does nothing.
@param event the WindowEvent that happened.
*/
public void windowActivated(WindowEvent event) {
	if (IOUtil.testing()) {
		__dataset_copy = new StateMod_DataSet (__dataset, false);
	try {	
		__tableModel = new
			StateMod_Response_TableModel(__dataset_copy);
		__worksheet.setModel(__tableModel);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	}
}

/**
Responds to Window closed events; does nothing.
@param event the WindowEvent that happened.
*/
public void windowClosed(WindowEvent event) {}

/**
Responds to Window closing events; closes the window and marks it closed
in StateMod_GUIUtil.
@param event the WindowEvent that happened.
*/
public void windowClosing(WindowEvent event) {
	closeWindow();
}

/**
Responds to Window deactivated events; saves the current information.
@param event the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Responds to Window deiconified events; does nothing.
@param event the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Responds to Window iconified events; saves the current information.
@param event the WindowEvent that happened.
*/
public void windowIconified(WindowEvent event) {}

/**
Responds to Window opened events; does nothing.
@param event the WindowEvent that happened.
*/
public void windowOpened(WindowEvent event) {}

/**
Responds to Window opening events; does nothing.
@param event the WindowEvent that happened.
*/
public void windowOpening(WindowEvent event) {}

} // End StateMod_Response_JFrame
