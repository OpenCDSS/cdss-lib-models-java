//------------------------------------------------------------------------------
// StateMod_RunDeltaPlot_JFrame - dialog to create templates for graphing
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 24 Dec 1997	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 03 Jul 1998	CEN, RTi		Modify to work for graphing
//					or for the output control edit.
// 28 Sep 1998	SAM, RTi		Added ability to specify the
//					period when getting time series
//					so that graph performs better.
// 29 Sep 1998	CEN, RTi		Adding radio buttons to toggle
//					user entered id vs. generated lists.
// 20 Nov 1998	CEN, RTi		Adding big picture stuff.
// 21 Dec 1998	CEN, RTi		Added try/catch to IO routines
// 12 May 1999	CEN, RTi		Changed .att to .txt for big
//					picture output.
// 25 Oct 1999	CEN, RTi		Added _template_location
// 06 Nov 2000	CEN, RTi		Added auto-line copy checkbox preference
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 04 May 2001	SAM, RTi		Verify TSView usage.  Enable all time
//					series defined in
//					TSGraph.GRAPH_TYPE_NAMES.  Remove limit
//					that only monthly time series can be
//					displayed.
// 19 Jul 2001	SAM, RTi		Change so the output file(s)are removed
//					before each run to make sure that new
//					data are used.
// 13 Aug 2001	SAM, RTi		Add wells(xwg).
// 17 Aug 2001	SAM, RTi		Add argument to RunSMOption to wait
//					after the run so that the output file is
//					created.  This may only be necessary on
//					fast machines.
// 23 Sep 2001	SAM, RTi		Change Table to DataTable.  Lengthen the
//					total time in waitForFile()calls to
//					60 seconds total.
// 2001-12-11	SAM, RTi		Update to use NoSwing GeoView classes so
//					parallel Swing development can occur.
// 2002-03-07	SAM, RTi		Use TSProduct to get the graph types.
// 2002-06-20	SAM, RTi		Update to pass well information to big
//					picture grid.
// 2002-07-26	SAM, RTi		Update to support the new GeoView for
//					the big picture plot.
// 2002-08-02	SAM, RTi		Make additional enhancements to
//					streamline running delplt.
//					Add __templateLocation to try to avoid
//					possible side-effects from multiple
//					windows being open at the same time.
// 2002-08-07	SAM, RTi		Figure out why the graph was not working
//					correctly - need to remove old files
//					each time.  Remove StringTokenizer - for
//					now do not handle reservoir accounts.
// 2002-08-26	SAM, RTi		Change constructor to take an integer
//					for the SMMainGUI interface type.
//					Change big picture constructor to take
//					a reference to SMMainGUI to pass
//					information.
// 2002-09-12	SAM, RTi		For graphing, add Baseflow as a data
//					type if the node is a baseflow node with
//					baseflow time series.
//					Also add a button to retrieve the time
//					series before graphing.  This allows
//					different graphs to be shown without
//					rereading.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.  Add a JTextField at the
//					bottom of the window to indicate the
//					status of the GUI.
// 2002-10-11	SAM, RTi		Change ProcessManager* to
//					ProcessManager1* to be allow transition
//					to Java 1.4.x.
// 2002-10-16	SAM, RTi		Move back to ProcessManger since the
//					updated version seems to work well with
//					Java 1.18 and 1.4.0.  Use version that
//					takes command arguments rather than a
//					single string.
// 2003-06-26	SAM, RTi		* Change the call to editFile()to use
//					  local code and start the editor as a
//					  thread.
//					* Change graphTS()to extend the period
//					  by a month on each end if bar graphs
//					  are used to account for the overlap
//					  of the bars on the end.
//------------------------------------------------------------------------------
// 2003-08-21	J. Thomas Sapienza, RTi	Initial swing version.
// 2003-08-25	JTS, RTi		Continued work on initial swing version.
// 2003-09-11	SAM, RTi		Update due to some name changes in the
//					river station components.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2004-01-22	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2006-03-06	JTS, RTi		Removed the help key from the process
//					manager dialog so that the help option
//					no longer displays.
//------------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.File;

import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.SimpleJButton;

import RTi.Util.Help.HelpJDialog;

import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.ProcessManagerJDialog;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This GUI is a class for controlling the run of delta plots.
*/
public class StateMod_RunDeltaPlot_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener {

/**
Reference the window manager's number for this window.
*/
public final static int WINDOW_NUM = 
	StateMod_DataSet_WindowManager.WINDOW_RUN_DELPLT;

/**
Filename option for the file browser.
*/
public final static String OPTION_BROWSE = "Browse ...";

/**
Run mode types.
*/
private final String 
	__DELPLT_RUN_MODE_SINGLE = 
		"Single - One parameter (first given), 1+ stations",
	__DELPLT_RUN_MODE_MULTIPLE = 
		"Multiple - 1+ parameter(s), 1+ station(s)",
	__DELPLT_RUN_MODE_DIFFERENCE = 
		"Diff - 1 parameter difference between runs (assign zero if "
		+ "not found in both runs)",
	__DELPLT_RUN_MODE_DIFFX =
		"Diffx - 1 parameter difference between runs (ignore if "
		+ "not found in both runs)",
	__DELPLT_RUN_MODE_MERGE =
		"Merge - merge output from delplt runs";

/**
Button labels.
*/
private final String
	__BUTTON_ADD_ROW = "Add a Row (Append)",
	__BUTTON_DELETE_ROWS = "Delete Selected Row(s)",
	__BUTTON_REMOVE_ALL_ROWS = "Remove All Rows",
	__BUTTON_LOAD = "Load Delplt Input",
	__BUTTON_RUN = "Run Delplt",
	__BUTTON_CLOSE = "Close",
	__BUTTON_HELP = "Help",
	__BUTTON_SAVE = "Save Delplt Input";

/**
Whether the table is dirty or not.
*/
private boolean __dirty = false;

/**
Specific dataset components that are kept handy.
*/
private DataSetComponent
	__reservoirComp = null,
	__diversionComp = null,
	__instreamFlowComp = null,
	__wellComp = null,
	__streamGageComp = null;

/**
GUI checkbox for whether to copy the above line when adding a new one.
*/
private JCheckBox __autoLineCopyJCheckBox;

/**
GUI status fields.
*/
private JTextField 
	__messageTextField,
	__statusTextField;

/**
The worksheet in which data is displayed.
*/
private JWorksheet __worksheet;

/**
GUI buttons.
*/
private SimpleJButton 
	__addRowButton,
	__deleteRowButton,
	__clearWorksheetButton,
	__selectTemplateButton,
	__saveTemplateButton,
	__helpButton,
	__closeButton,
	__runDelpltButton;

/**
GUI combo box for choosing the run mode.
*/
private SimpleJComboBox __delpltRunModeSimpleJComboBox;

/**
The dataset containing the StateMod data.
*/
private StateMod_DataSet __dataset;

/**
The worksheet table model.
*/
private StateMod_RunDeltaPlot_TableModel __tableModel;

/**
The default filename filter.
*/
private String __defaultFilenameFilter;

/**
The name of the response file without the extension.
*/
private String __basename = "";

/**
The default filename.
*/
private String __defaultFilename;

/**
The template location.
*/
private String __templateLocation;

/** 
Constructor.
@param dataset the dataset for which to construct a delta plot run file.
*/
public StateMod_RunDeltaPlot_JFrame(StateMod_DataSet dataset) {
	StateMod_GUIUtil.setTitle(this, dataset, "Run Delta Plot", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__basename = dataset.getBaseName();

	__dataset = dataset;
	
	__reservoirComp = __dataset.getComponentForComponentType(
		__dataset.COMP_RESERVOIR_STATIONS);
	__diversionComp = __dataset.getComponentForComponentType(
		__dataset.COMP_DIVERSION_STATIONS);
	__instreamFlowComp = __dataset.getComponentForComponentType(
		__dataset.COMP_INSTREAM_STATIONS);
	__wellComp = __dataset.getComponentForComponentType(
		__dataset.COMP_WELL_STATIONS);
	__streamGageComp = __dataset.getComponentForComponentType(
		__dataset.COMP_STREAMGAGE_STATIONS );
			
	__defaultFilenameFilter = "in";
	
	if (!__basename.equals("")) {
		setTitle(getTitle()+ " - " + __basename);
	}

	setupGUI();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String routine = "StateMod_RunDeltaPlot_JFrame.actionPerformed";
	String action = event.getActionCommand();

	if (action.equals(__BUTTON_ADD_ROW)) {
		if (!__tableModel.canAddRow()) {
			setMessages("At least the first row must have all "
				+ "values filled out.", "Error");
			return;
		}
		int rows = __worksheet.getRowCount();

		if (rows == 0) {
			__worksheet.addRow(new StateMod_GraphNode());
			__tableModel.setCellAttributes(0, 0, true);
			__tableModel.setCellAttributes(0, 1, true);
			__tableModel.setCellAttributes(0, 2, true);
			__tableModel.setCellAttributes(0, 3, true);
			__tableModel.setCellAttributes(0, 4, true);
			__dirty = true;
			checkButtonStates();
			setMessages("Add parameters to analyze", "Ready");
			return;
		}
		
		StateMod_GraphNode gnp = (StateMod_GraphNode)
			__worksheet.getRowData(rows - 1);
		String id = gnp.getID().trim();			
		
		if (autoLineCopy() && id.equals("0 (All)")) {
		// duplicate the values from the above row and copy them
		// into the new row
			__worksheet.addRow(new StateMod_GraphNode());
			__worksheet.setValueAt(gnp.getFileName(), 
				rows, 0);
			__worksheet.setValueAt(gnp.getType(), rows, 1);
			__tableModel.setParmIDComboBoxes(rows, gnp.getType());
			__worksheet.setValueAt(gnp.getDtype(), rows, 2);
			__worksheet.setValueAt(gnp.getYrAve(), rows, 3);
			__worksheet.setValueAt(gnp.getID(), rows, 4);
		}
		else {		
		// normal row add
		// mark everything as a duplicate of the row above it, 
		// except the ID, which should have a red border
			__worksheet.addRow(new StateMod_GraphNode());
			__tableModel.copyDownComboBoxes();
			__tableModel.setLastRowAsBlank();
			__tableModel.setCellAttributes(rows, 4, true);
		}
		__dirty = true;
		checkButtonStates();
		setMessages("Add parameters to analyze", "Ready");
	}
	else if (action.equals(__BUTTON_REMOVE_ALL_ROWS)) {
		__worksheet.clear();
		__dirty = false;
		checkButtonStates();
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		closeWindow();
	}
	else if (action.equals(__BUTTON_DELETE_ROWS)) {
		int[] rows = __worksheet.getSelectedRows();
		for (int i = rows.length - 1; i >= 0; i--) {
			__tableModel.preDeleteRow(rows[i]);
//			__worksheet.selectRow(rows[i] - 1);
//			__worksheet.scrollToRow(rows[i] - 1);
		}
		checkButtonStates();
		__dirty = true;
		setMessages("Delplt input has changed.", "Ready");
	}
	else if (action.equals(__BUTTON_HELP)) {
		Vector helpVector = fillBPPrelimHelpGUIVector();
		PropList proplist = new PropList("HelpProps");
		proplist.setValue("HelpKey", "SMGUI.BigPicture");
		new HelpJDialog(this, helpVector, proplist);
	}	
	else if (action.equals(__BUTTON_RUN)) {
		runDelplt();
	}
	else if (action.equals(__BUTTON_SAVE)) {
		saveDelpltFile();
	}
	else if (action.equals(__BUTTON_LOAD)) {
		JGUIUtil.setWaitCursor(this, true);
		String directory = 
			JGUIUtil.getLastFileDialogDirectory();
	
		JFileChooser fc = null;
		if (directory != null) {
			fc = new JFileChooser(directory);
		}
		else {
			fc = new JFileChooser();
		}

		fc.setDialogTitle("Select Delplt Input File");
		SimpleFileFilter tpl = new SimpleFileFilter(
			__defaultFilenameFilter, "Delplt Input Files");
		fc.addChoosableFileFilter(tpl);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(tpl);
		fc.setDialogType(JFileChooser.OPEN_DIALOG); 	

		JGUIUtil.setWaitCursor(this, false);
		int retVal = fc.showOpenDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
	
		String currDir = (fc.getCurrentDirectory()).toString();
	
		__templateLocation = currDir;
	
		if (!currDir.equalsIgnoreCase(directory)) {
			JGUIUtil.setLastFileDialogDirectory(currDir);
		}
		String filename = fc.getSelectedFile().getName();

		__defaultFilename = filename;

		JGUIUtil.setWaitCursor(this, true);

		Vector theGraphNodes = new Vector();
		try {
			StateMod_GraphNode.readStateModDelPltFile(theGraphNodes,
				currDir + File.separator + filename);
			Vector nodes = __tableModel.formLoadData(theGraphNodes);
			StateMod_GraphNode gn = null;
			__worksheet.clear();
			for (int i = 0; i < nodes.size(); i++) {
				gn = (StateMod_GraphNode)nodes.elementAt(i);
				__worksheet.addRow(new StateMod_GraphNode());
				__worksheet.setValueAt(gn.getFileName(), i, 0);
				__worksheet.setValueAt(gn.getType(), i, 1);
				__tableModel.setParmIDComboBoxes(i, 
					gn.getType());
				__worksheet.setValueAt(gn.getDtype(), i, 2);
				__worksheet.setValueAt(gn.getYrAve(), i, 3);
				__worksheet.setValueAt(gn.getID(), i, 4);
			}

			__dirty = false;
		}
		catch (Exception e) {
			Message.printWarning(1, routine,
				"Error loading output control file\n"
				+ "\"" + currDir + File.separator + filename
				+ "\"", this);
			Message.printWarning(2, routine, e);
		}

		if (theGraphNodes.size() > 0) {
			StateMod_GraphNode gn = (StateMod_GraphNode)
				theGraphNodes.elementAt(0);
			setRunType(gn.getSwitch());
		}
	
		JGUIUtil.setWaitCursor(this, false);

		checkButtonStates();
	}
}

/**
Checks to see if the previous line should be automatically copied down to the 
next one when a new line is added.
@return true if the line should be copied, false if not
*/
public boolean autoLineCopy() {
	if (__autoLineCopyJCheckBox.isSelected()) {
		return true;
	}
	else {	
		return false;
	}
}

/**
Checks the state of the GUI and sets the button states appropriately.
*/
private void checkButtonStates() {
	int selRows = __worksheet.getSelectedRowCount();

	if (selRows == 0) {
		__deleteRowButton.setEnabled(false);
	}
	else {
		__deleteRowButton.setEnabled(true);
	}

	int rows = __worksheet.getRowCount();
	if (rows == 0) {
		__clearWorksheetButton.setEnabled(false);
		__runDelpltButton.setEnabled(false);
		__saveTemplateButton.setEnabled(false);
	}
	else {
		__clearWorksheetButton.setEnabled(true);
		__runDelpltButton.setEnabled(true);
		__saveTemplateButton.setEnabled(true);
	}
}

/**
Closes the window, prompting to save if the user has not already.
*/
protected void closeWindow() {
	if (__dirty) {
		int x = new ResponseJDialog(this, 
			"Save Delplt Input",
			"You have not saved the Delplt input file.\n\n"
			+ "Continue without saving?",
			ResponseJDialog.YES | ResponseJDialog.NO).response();
		if (x == ResponseJDialog.NO) {
			setDefaultCloseOperation (
				WindowConstants.DO_NOTHING_ON_CLOSE);		
			return;
		}
	}
	setVisible(false);
	setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
	dispose();			
}


/**
Create help information for the help dialog.
*/
private Vector fillBPPrelimHelpGUIVector() {
	Vector helpVector = new Vector(2);
	helpVector.addElement(
	"This tool is intended to help you create an input file for the ");
	helpVector.addElement(
	"delplt, which allows comparisons of different scenarios, years, ");
	helpVector.addElement(
	"data types, etc.  Each section in the input file (file, data type, ");
	helpVector.addElement(
	"parameters, ID list and year) is constructed in this tool.  Each ");
	helpVector.addElement(
	"time you specify a new file name, the entire row must be filled.");
	helpVector.addElement(
	"This is a new section in the input file.  If you wish to ");
	helpVector.addElement(
	"perform the analysis on every station, set the identifier to \"0\".");
	helpVector.addElement(
	"If you wish to specify a list of identifiers, fill in every column ");
	helpVector.addElement(
	"on the first row and only the identifier on subsequent rows.");
	helpVector.addElement("");

	helpVector.addElement(
	"The file name should either be one of the binary files (.b43 or .b44");
	helpVector.addElement(
	"for example)or the .xdd file for diversions or .xre for ");
	helpVector.addElement(
	"reservoirs if full reports have been created.");

	helpVector.addElement(
	"StateMod determines which stations to include in the reports based");
	helpVector.addElement(
	"on the output control contents.  Therefore, if your results don't");
	helpVector.addElement(
	"include stations you wish to compare, look at your output control");
	helpVector.addElement(
	"file.  Remember that changes to that file will not be reflected in ");
	helpVector.addElement(
	"the BigPicture plot until another StateMod simulation has been run.");
	helpVector.addElement("");

	helpVector.addElement(
	"For example, to perform difference comparisons between average");
	helpVector.addElement(
	"River_Outflow and River_Outflow in 1995 of 4 different stations, ");
	helpVector.addElement(
	"the table would look like the following:");
	helpVector.addElement("");
	helpVector.addElement(
	"Stream       River_Outflow   09152500      Ave     whiteH.xdd");
	helpVector.addElement(
	"                             09144250");
	helpVector.addElement(
	"                             09128000");
	helpVector.addElement(
	"                             09149500");
	helpVector.addElement(
	"Stream       River_Outflow   09152500      1995    whiteH.xdd");
	helpVector.addElement(
	"                             09144250");
	helpVector.addElement(
	"                             09128000");
	helpVector.addElement(
	"                             09149500");
	helpVector.addElement("");
	helpVector.addElement(
	"The result is a single value (the difference) for each station");

	helpVector.addElement("");
	helpVector.addElement(
	"See additional help through the \"More help\" button below.");
	helpVector.addElement(
	"Note that the additional help may take some time to display.");
	return helpVector;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	// REVISIT
	super.finalize();
}

/**
This method is called by the SMbigPictureGrid getData()method.
REVISIT (JTS - 2003-08-25) 
this should be used instead by whatever method saves the data to a file
@return StateMod_GraphNode.RUNTYPE_*, indicating Delplt run mode.
*/
public int getRunType() {
	String selected = __delpltRunModeSimpleJComboBox.getSelected();
	if (selected.equals(__DELPLT_RUN_MODE_SINGLE)) {
		return StateMod_GraphNode.RUNTYPE_SINGLE;
	}
	else if (selected.equals(__DELPLT_RUN_MODE_MULTIPLE)) {
		return StateMod_GraphNode.RUNTYPE_MULTIPLE;
	}
	else if (selected.equals(__DELPLT_RUN_MODE_DIFFERENCE)) {
		return StateMod_GraphNode.RUNTYPE_DIFFERENCE;
	}
	else if (selected.equals(__DELPLT_RUN_MODE_DIFFX)) {
		return StateMod_GraphNode.RUNTYPE_DIFFX;
	}
	else { // if (selected.equals(__DELPLT_RUN_MODE_MERGE)) {
		return StateMod_GraphNode.RUNTYPE_MERGE;
	}
}

/**
Responds when item states have changed.
*/
public void itemStateChanged(ItemEvent event) {
	__dirty = true;
}

/**
Does nothing.
*/
public void keyPressed(KeyEvent event) {}

/**
Responds to key released events; calls 'processTableSelection' with the 
newly-selected index in the table.
@param event the KeyEvent that happened.
*/
public void keyReleased(KeyEvent event) {
	checkButtonStates();
}

/**
Does nothing.
*/
public void keyTyped(KeyEvent event) {}

/**
Does nothing.
*/
public void mouseClicked(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
Does nothing.
*/
public void mousePressed(MouseEvent event) {}

/**
Responds to mouse released events; calls 'processTableSelection' with the 
newly-selected index in the table.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	checkButtonStates();
}

/**
Run delplt.  It is run as follows:
<ol>
<li>	Run delplt.</li>
<li>	Convert delplt .xgr output file to a more general .txt file format.</li>
<li>	Display the .txt file contents as a layer on the map(if
	requested).</li>
</ol>
*/
public void runDelplt() {
	String routine = "StateMod_RunDeltaPlot_JFrame.runDelplt";

	if (__worksheet.getRowCount() == 0) {
		Message.printWarning(1, routine,
			"Input for Delplt has not been defined.");
		return;
	}
	
	new ResponseJDialog(this, "Run Delplt",
		"The Delplt utility program will now be run to\n"
		+ "prepare summary data that can be displayed on the map.",
		ResponseJDialog.OK).response();

	// Check to see if anything in the template has changed
	// since a template was last read in(if any).
	// If so, rewrite template file.

	if (__dirty) {
		// warn users 
		/* Until get OK from Rays, stick with *.in...
		if (IOUtil.testing()) {
			// Use *.delplt for file name...
			new ResponseJDialog(this, "Save Delplt Input",
			"Changes have been made to the input " +
			"file used by \"delplt\".\nYou will be asked to " +
			"select an input file name(convention is *.delplt).",
			ResponseJDialog.OK).response();
		}
		else {*/	// Use *.in for file name...
		new ResponseJDialog(this, "Save Delplt Input",
			"Changes have been made to the input "
			+ "file used by \"delplt\".\nYou will be asked "
			+ "to select an input file name (convention is *.in).",
			ResponseJDialog.OK).response();
		//}

		if (!saveDelpltFile()) {
			return;
		}
	}

	// Construct the(*.xgr)output file name created by delplt...

	int index = __defaultFilename.indexOf(".");
	if (index == -1) {
		Message.printWarning(1, routine, 
			"Don't know how to handle filename output.  "
			+ "You must include a \".\" in your filename.", this);
		return;
	}
	String xgrOutputFilename = 
		__defaultFilename.substring(0, index).toUpperCase() + ".XGR";

	File xgrFile = new File(__templateLocation + File.separator 
		+ xgrOutputFilename);

	// Delete the file if it exists so we don't get bogus output...

	if (xgrFile.exists()) {
		Message.printStatus(1, routine, "Deleting existing delplt "
			+ "output file \"" + __templateLocation + File.separator
			+ xgrOutputFilename + "\"");
		xgrFile.delete();
		xgrFile = null;
	}

	// Also delete the delplt log file so if there is an error we can
	// display the correct one...

	// Start with the input file...
	String logfileString = __defaultFilename.substring(0, index)+ ".log";

	// Strip off the extension and replace with "log".

	File logfile = new File(logfileString);
	if (logfile.exists()) {
		Message.printStatus(1, routine, "Deleting existing delplt "
			+ "log file \"" + logfileString + "\"");
		logfile.delete();
		logfile = null;
	}

	// Run command using GUI...

	PropList props = new PropList("Delplt");
	// This is modal...
	String [] command_array = new String[2];
	command_array[0] = "delplt";
	command_array[1] = __templateLocation + File.separator 
		+ __defaultFilename;
	ProcessManager pm = new ProcessManager(command_array);
	ProcessManagerJDialog pmgui = new ProcessManagerJDialog(
		this,"StateMod Delplt", pm, props);

	int exitStatus = pmgui.getExitStatus();
	Message.printStatus(1, routine, "exitStatus is " + exitStatus);

	if (exitStatus != 0) {
		// Try checking for the log file.  If it does not exist, then
		// delplt is probably not in the path...
		logfile = new File(logfileString);
		if (logfile.exists()) {
			int x = new ResponseJDialog(this, 
				"Delplt Unsuccessful",
				"Delplt did not complete successfully.\n\n"
				+ "View Delplt log file?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.YES) {
				try {	
					String[] command_array2 = new String[2];
					command_array2[0] = 
					    StateMod_GUIUtil._editorPreference;
					command_array2[1] = logfileString;
					ProcessManager p = new ProcessManager(
						command_array2);
					// This will run as a thread until the
					// process is shut down...
					Thread t = new Thread(p);
					t.start();
				}
				catch (Exception e2) {
					Message.printWarning(1, routine,
						"Unable to view/edit file \""
						+ logfileString + "\"");
				}
			}
		}
		else {	// Log file does not exist...
			int x = new ResponseJDialog(this, 
				"Delplt Unsuccessful",
				"Delplt did not complete successfully.\n\n" +
				"No Delplt log file is available.  Verify that"+
				" Delplt is in the PATH.",
				ResponseJDialog.OK).response();
		}
		return;
	}
	else {	
		// Successful execution so no reason to show the...
		// However, because the ProcessManagerJDialog is modal, we don't
		// get to here until the dialog is closed!  For now, require the
		// user to close...
		//pmgui.close();
	}

	// Now - if command was successful, there should be a <filename>.xgr
	// file in the directory corresponding to the delplt .rsp file
	// directory.  If so, ask user for a .txt filename to save converted
	// information to.  Note that this is different from the StateMod report
	// which puts the output in the directory of the response file for each
	// individual run.  For Delplt, the output is always in the directory
	// where the Delplt input file is.
	if (Message.isDebugOn) {
		Message.printDebug(10, routine, "Looking for xgr file: "
			+ __templateLocation + File.separator 
			+ xgrOutputFilename);	
	}
	//File xgrOutput = new File(__templateLocation + xgrOutputFilename);
	// Reopen this file to see if output was created...
	xgrFile = new File(__templateLocation + File.separator 
		+ xgrOutputFilename);

	// Sometimes need to wait for the file to be created...
	/* SAMX take this out - the new ProcessManager seems to be better
		behaved so see if we can do without.
	if (TimeUtil.waitForFile(__templateLocation +
		xgrOutputFilename, 500, 120)) {}
	*/
	
	if (xgrFile.exists()) {
		// Describe to users what is going to happen.  This is done with
		// a dialog because the JFileChooser does not have a way to add
		// more instructions...
		int response = new ResponseJDialog(this,
			"Save Delplt Output as Text File",
			"Delplt was successful.\n\nThe output will now "
			+ "be converted from " + xgrOutputFilename
			+ " format to a comma delimited format\n"
			+ "that can be used directly by the StateMod GUI, GIS"
			+ " applications and other software.\n"
			+ "You will be asked to select an output filename "
			+ "(convention is *.txt).\n\n"
			+ "Select Cancel if no further action is desired.",
			ResponseJDialog.OK | ResponseJDialog.CANCEL).response();
		if (response == ResponseJDialog.CANCEL) {
			pmgui.toFront();
			return;
		}

		JGUIUtil.setWaitCursor(this, true);
		String directory = JGUIUtil.getLastFileDialogDirectory();

		JFileChooser fc = null;
		if (directory != null) {
			fc = new JFileChooser(directory);
		}
		else {
			fc = new JFileChooser();
		}
	
		fc.setDialogTitle("Select Filename for Delplt Text Output");
		SimpleFileFilter tpl = new SimpleFileFilter("txt","Text Files");
		fc.addChoosableFileFilter(tpl);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(tpl);
		fc.setDialogType(JFileChooser.SAVE_DIALOG); 	
	
		JGUIUtil.setWaitCursor(this, false);
		int retVal = fc.showSaveDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
	
		String currDir = (fc.getCurrentDirectory()).toString();

		__templateLocation = currDir;
	
		if (!currDir.equalsIgnoreCase(directory)) {
			JGUIUtil.setLastFileDialogDirectory(currDir);
		}
		String filename = fc.getSelectedFile().getName();

		StateMod_DeltaPlot delpltOutput = new StateMod_DeltaPlot();
		try {
			delpltOutput.readStateModDeltaOutputFile(
				__templateLocation + File.separator 
				+ xgrOutputFilename);
			delpltOutput.writeArcViewFile(
				null, currDir + File.separator + filename, 
				null);

			// Prompt the user if they want to add to the map.
			// Sometimes all they are trying to do is to run Delplt
			// so they can look at the text output.

			response = new ResponseJDialog(this,
				"Display Delplt Output on Map",
				"Do you want to display the Delplt output as "
				+ "a map layer?\nYou can add to the map later "
				+ "if you answer No.",
				ResponseJDialog.YES|ResponseJDialog.NO)
				.response();
			if (response == ResponseJDialog.NO) {
				pmgui.toFront();
				return;
			}
	/*
	REVISIT (JTS - 2003-08-25)
	no 'addSummaryMapLayer' method in the main jframe
			// Create a layer and add it to the geoview...
			((StateModGUI_JFrame)StateMod_GUIUtil.getWindow(
				StateMod_GUIUtil.MAIN_WINDOW))
				.addSummaryMapLayer(currDir + File.separator 
				+ filename);
	*/
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	else {	Message.printWarning(1, routine, 
		__templateLocation + xgrOutputFilename + 
		" file never created.", this);
	}
}

/**
Saves the delta plot file.
*/
private boolean saveDelpltFile() {
	// REVISIT (JTS - 2003-08-27)
	// check for formatted cells and disallow saving if there are
	// errors

	String routine = "StateMod_RunDeltaPlot_JFrame.saveDelpltFile";

	JGUIUtil.setWaitCursor(this, true);
	String directory = 
		JGUIUtil.getLastFileDialogDirectory();

	JFileChooser fc = null;
	if (directory != null) {
		fc = new JFileChooser(directory);
	}
	else {
		fc = new JFileChooser();
	}

	fc.setDialogTitle("Select Delplt Input File to Save");
	SimpleFileFilter tpl = new SimpleFileFilter(
		__defaultFilenameFilter, "Delplt Input Files");
	fc.addChoosableFileFilter(tpl);
	fc.setAcceptAllFileFilterUsed(true);
	fc.setFileFilter(tpl);
	fc.setDialogType(JFileChooser.SAVE_DIALOG); 	

	JGUIUtil.setWaitCursor(this, false);
	int retVal = fc.showSaveDialog(this);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return false;
	}

	String currDir = (fc.getCurrentDirectory()).toString();

	__templateLocation = currDir;

	if (!currDir.equalsIgnoreCase(directory)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	String filename = fc.getSelectedFile().getName();

	__defaultFilename = filename;

	JGUIUtil.setWaitCursor(this, true);

	Vector theGraphNodes = __tableModel.formSaveData(
		__worksheet.getAllData());

	StateMod_GraphNode gn = null;
	int runtype = getRunType();
	for (int i = 0; i < theGraphNodes.size(); i++) {
		gn = (StateMod_GraphNode)theGraphNodes.elementAt(i);
		gn.setSwitch(runtype);
	}
	try {	
		StateMod_GraphNode.writeStateModDelPltFile(null, 
			currDir + File.separator + filename,
			theGraphNodes, null);
			__dirty = false;
		} 
	catch (Exception e) {
		Message.printWarning(1, routine,
			"Error saving output control file\n"
			+ "\"" + currDir + File.separator 
			+ filename + "\"",
			this);
		Message.printWarning(2, routine, e);
		JGUIUtil.setWaitCursor(this, false);		
		return false;
	}
	JGUIUtil.setWaitCursor(this, false);	
	__dirty = false;
	return true;
}

/**
This method is called by the SMbigPictureGrid displayData()method.
REVISIT (JTS - 2003-08-25)
this should actually be called when the values are read in from a file.
@param runType StateMod_GraphNode.RUNTYPE_* values.
*/
public void setRunType(int runType) {
	if (runType == StateMod_GraphNode.RUNTYPE_SINGLE) {
		__delpltRunModeSimpleJComboBox.select(__DELPLT_RUN_MODE_SINGLE);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_MULTIPLE) {
		__delpltRunModeSimpleJComboBox.select(
			__DELPLT_RUN_MODE_MULTIPLE);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_DIFFERENCE) {
		__delpltRunModeSimpleJComboBox.select(
			__DELPLT_RUN_MODE_DIFFERENCE);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_DIFFX) {
		__delpltRunModeSimpleJComboBox.select(__DELPLT_RUN_MODE_DIFFX);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_MERGE) {
		__delpltRunModeSimpleJComboBox.select(__DELPLT_RUN_MODE_MERGE);
	}
}

/**
Set the messages that are visible in the bottom of the window.
@param message General message string.
@param status Status string(e.g., "Ready", "Wait".
*/
private void setMessages(String message, String status) {
	if (message != null) {
		__messageTextField.setText(message);
	}
	if (status != null) {
		__statusTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "StateMod_RunDeltaPlot_JFrame.setupGUI";

	addWindowListener(this);	

	__defaultFilename = "";

	__addRowButton = new SimpleJButton(__BUTTON_ADD_ROW, this);
	__deleteRowButton = new SimpleJButton(__BUTTON_DELETE_ROWS, this);
	__saveTemplateButton = new SimpleJButton(__BUTTON_SAVE, this);
	__clearWorksheetButton = new SimpleJButton(__BUTTON_REMOVE_ALL_ROWS,
		this);
	__selectTemplateButton = new SimpleJButton(__BUTTON_LOAD, this);

	__helpButton = new SimpleJButton(__BUTTON_HELP, this);
	__closeButton = new SimpleJButton(__BUTTON_CLOSE, this);
	__runDelpltButton = new SimpleJButton(__BUTTON_RUN, this);

	__delpltRunModeSimpleJComboBox = new SimpleJComboBox();
	__delpltRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_SINGLE);
	__delpltRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_MULTIPLE);
	__delpltRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_DIFFERENCE);
	__delpltRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_DIFFX);
	__delpltRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_MERGE);
	__delpltRunModeSimpleJComboBox.addItemListener(this);
	// Default is to select multiple mode...
	__delpltRunModeSimpleJComboBox.select(1);

	__autoLineCopyJCheckBox = new JCheckBox(
		"Automatically fill initial line content on \"Add a Row\".", 
		true);

	// Make a main panel to be the resizable body of the frame...
	JPanel mainPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	mainPanel.setLayout(gb);

	GridLayout gl = new GridLayout(2, 2, 2, 2);
	JPanel topPanel = new JPanel();
	topPanel.setLayout(gl);

	GridBagConstraints gbc = new GridBagConstraints();
	GridLayout gl2 = new GridLayout(1, 0, 2, 0);
	JPanel bottomPanel = new JPanel();
	bottomPanel.setLayout(gl2);

	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
	JPanel finalButtonPanel = new JPanel();
	finalButtonPanel.setLayout(fl);

	JPanel gridPanel = new JPanel();
	gridPanel.setLayout(gb);

	// add add a row, delete selected rows, clear spreadsheet,
	// select template, save template buttons
	topPanel.add(__addRowButton);
	topPanel.add(__deleteRowButton);
	topPanel.add(__clearWorksheetButton);
	topPanel.add(__selectTemplateButton);
	int y = 0;
	JGUIUtil.addComponent(mainPanel, topPanel, 
		0, y, 10, 3, 0, 0, 
		10, 10, 10, 10, 
		gbc.NONE, gbc.NORTH);
	y+=3;
	JPanel runTypeJPanel = new JPanel(new FlowLayout());
	runTypeJPanel.add(new JLabel("Delplt run mode:"));
	runTypeJPanel.add(__delpltRunModeSimpleJComboBox);
	JGUIUtil.addComponent(mainPanel, runTypeJPanel,
		0, y, 10, 1, 1, 0, 
		0, 0, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(1) File:  For reservoirs specify the ASCII "
		+ ".xre or binary .b44 file."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"         For other node types specify the "
		+ "ASCII .xdd or binary .b43 file.  Or, specify"
		+ " blank if continuing list of IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(2) Station type:  diversion, instream, "
		+ "reservoir, stream, streamID (0* gages)"
		+ ", well, or blank if continuing list of IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(3) Parameter:  parameters from StateMod "
		+ "output, or blank if continuing list of IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(4) Year/Ave:  Enter Ave, 4-digit year "
		+ "(e.g., 1989) or year and month (e.g., "
		+ "1989 NOV), or blank if continuing list of"
		+ " IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(5) ID:  0 (zero) for all or enter a specific "
		+ "identifier."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(mainPanel, __autoLineCopyJCheckBox,
		0, ++y, 1, 1, 0, 0, 
		0, 10, 0, 0,
		gbc.NONE, gbc.WEST);

	PropList p = 
		new PropList("StateMod_RunDeltaPlot_JFrame.JWorksheet");
	p.add("JWorksheet.ShowRowHeader=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.RowColumnBackground=LightGray");
	p.add("JWorksheet.ShowPopupMenu=true");

	File file = new File(__dataset.getDataSetDirectory());
	__templateLocation = __dataset.getDataSetDirectory();
	JGUIUtil.setLastFileDialogDirectory(__dataset.getDataSetDirectory());
	Vector filters = new Vector();
	filters.add("xre");
	filters.add("b44");
	filters.add("xdd");
	filters.add("b43");
	SimpleFileFilter sff = new SimpleFileFilter(filters, "etc");
	File[] files = file.listFiles(sff);

	Vector filenames = new Vector();
	filenames.add("");
	filenames.add(OPTION_BROWSE);
	for (int i = 0; i < files.length; i++) {
		filenames.add(files[i].getName());
		Message.printStatus(1, "", "File " + i + ": '" 
			+ files[i].getName() + "'");
	}
	
	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		__tableModel = new
			StateMod_RunDeltaPlot_TableModel(this, new Vector(),
			(Vector)__reservoirComp.getData(), 
			(Vector)__diversionComp.getData(),
			(Vector)__instreamFlowComp.getData(),
			(Vector)__wellComp.getData(),
			(Vector)__streamGageComp.getData());
			
		StateMod_RunDeltaPlot_CellRenderer crg = new
			StateMod_RunDeltaPlot_CellRenderer(__tableModel);
	
		jsw = new JScrollWorksheet(crg, __tableModel, p);
		__worksheet = jsw.getJWorksheet();

		Vector v = StateMod_Util.arrayToVector(
			StateMod_GraphNode.node_types);
		__worksheet.setColumnJComboBoxValues(0, filenames, true);

		Vector vn = StateMod_Util.arrayToVector(
			StateMod_GraphNode.node_types);
		
		int index = vn.indexOf("Streamflow");
		vn.add(index + 1, "Stream ID (0* Gages)");
		
		vn.add("");
		
		__worksheet.setColumnJComboBoxValues(1, vn, true);

		__worksheet.setCellSpecificJComboBoxColumn(2);
		__worksheet.setCellSpecificJComboBoxColumn(4, true);

		__tableModel.setWorksheet(__worksheet);

		widths = crg.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);
	__worksheet.addKeyListener(this);

	JGUIUtil.addComponent(gridPanel, jsw,
		0, 0, 1, 1, 1, 1, 
		gbc.BOTH, gbc.CENTER);
	JGUIUtil.addComponent(mainPanel, gridPanel, 
		0, ++y, 10, 12, 1, 1, 
		gbc.BOTH, gbc.CENTER);

	y += 11;	// To account for grid height

	// Add bottom buttons - these are alphabetical so be
	// careful if more are added

	finalButtonPanel.add(__runDelpltButton);
	finalButtonPanel.add(__closeButton);
	finalButtonPanel.add(__helpButton);
	finalButtonPanel.add(__saveTemplateButton);

	// Add the final buttons on the bottom to the bottom panel...
	bottomPanel.add(finalButtonPanel);
	// Add the button panel to the frame...
	JGUIUtil.addComponent(mainPanel, bottomPanel, 
		//0, gbc.RELATIVE, 10, 1, 
		0, ++y, 10, 1, 
		0, 0, 
		gbc.VERTICAL, gbc.SOUTH);

	// Add the main panel as the resizable content...
	getContentPane().add("Center", mainPanel);

	// Add JTextFields for messages...
	JPanel message_JPanel = new JPanel();
	message_JPanel.setLayout(gb);
	__messageTextField = new JTextField();
	__messageTextField.setEditable(false);
	__statusTextField = new JTextField("             ");
	__statusTextField.setEditable(false);
	JGUIUtil.addComponent(message_JPanel, __messageTextField, 
		0, 0, 9, 1, 
		1, 0, 
		gbc.HORIZONTAL, gbc.WEST);
	JGUIUtil.addComponent(message_JPanel, __statusTextField, 
		9, 0, 1, 1, 
		0, 0, 
		gbc.HORIZONTAL, gbc.SOUTH);

	getContentPane().add("South", message_JPanel);

	pack();
	setSize(950, 625);
	setMessages("Add parameters to analyze", "Ready");
	JGUIUtil.center(this);
	setVisible(true);

	checkButtonStates();

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Does nothing.
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
Does nothing.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpening(WindowEvent event) {}

}
