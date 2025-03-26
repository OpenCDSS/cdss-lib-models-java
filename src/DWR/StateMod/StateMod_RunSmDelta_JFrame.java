// StateMod_RunSmDelta_JFrame - dialog to create templates for graphing

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Models Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Models Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

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
import java.util.ArrayList;
import java.util.List;
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
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
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
@SuppressWarnings("serial")
public class StateMod_RunSmDelta_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener {

/**
Reference the window manager's number for this window.
*/
public final static int WINDOW_NUM = StateMod_DataSet_WindowManager.WINDOW_RUN_DELPLT;

/**
Filename option for the file browser.
*/
public final static String OPTION_BROWSE = "Browse ...";

/**
 * Name of the program as known to users.
 */
private final String __SmDelta = "SmDelta";

/**
Run mode types.
*/
private final String 
	__DELPLT_RUN_MODE_SINGLE = "Single - One parameter (first given), 1+ stations",
	__DELPLT_RUN_MODE_MULTIPLE = "Multiple - 1+ parameter(s), 1+ station(s)",
	__DELPLT_RUN_MODE_DIFFERENCE = "Diff - 1 parameter difference between runs (assign zero if not found in both runs)",
	__DELPLT_RUN_MODE_DIFFX = "Diffx - 1 parameter difference between runs (ignore if not found in both runs)",
	__DELPLT_RUN_MODE_MERGE = "Merge - merge output from " + __SmDelta + " runs";

/**
Button labels.
*/
private final String
	__BUTTON_ADD_ROW = "Add a Row (Append)",
	__BUTTON_DELETE_ROWS = "Delete Selected Row(s)",
	__BUTTON_REMOVE_ALL_ROWS = "Remove All Rows",
	__BUTTON_LOAD = "Load " + __SmDelta + " Input",
	__BUTTON_RUN = "Run " + __SmDelta,
	__BUTTON_CLOSE = "Close",
	__BUTTON_HELP = "Help",
	__BUTTON_SAVE = "Save " + __SmDelta + " Input";

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
	__runSmDeltaButton;

/**
GUI combo box for choosing the run mode.
*/
private SimpleJComboBox __smdeltaRunModeSimpleJComboBox;

/**
The dataset containing the StateMod data.
*/
private StateMod_DataSet __dataset;

/**
The worksheet table model.
*/
private StateMod_RunSmDelta_TableModel __tableModel;

/**
The default filename filter.
*/
private String __defaultFilenameFilter = __SmDelta;

/**
The SmDelta response file (no leading path).
*/
private String __smdeltaFilename;

/**
The SmDelta response file folder.
*/
private String __smdeltaFolder;

/** 
Constructor.
@param dataset the dataset for which to construct a delta plot run file.
*/
public StateMod_RunSmDelta_JFrame(StateMod_DataSet dataset) {
	StateMod_GUIUtil.setTitle(this, dataset, "Run " + __SmDelta, null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	// Get the StateMod data set basename
	String basename = dataset.getBaseName();

	__dataset = dataset;
	
	__reservoirComp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_RESERVOIR_STATIONS);
	__diversionComp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_DIVERSION_STATIONS);
	__instreamFlowComp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_INSTREAM_STATIONS);
	__wellComp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_WELL_STATIONS);
	__streamGageComp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_STREAMGAGE_STATIONS );
	
	if (!basename.equals("")) {
		setTitle(getTitle()+ " - " + basename);
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
			setMessages("At least the first row must have all values filled out.", "Error");
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
		
		StateMod_GraphNode gnp = (StateMod_GraphNode)__worksheet.getRowData(rows - 1);
		String id = gnp.getID().trim();			
		
		if (autoLineCopy() && id.equals("0 (All)")) {
		// duplicate the values from the above row and copy them into the new row
			__worksheet.addRow(new StateMod_GraphNode());
			__worksheet.setValueAt(gnp.getFileName(), rows, 0);
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
		setMessages(__SmDelta + " input has changed.", "Ready");
	}
	else if (action.equals(__BUTTON_HELP)) {
		List<String> helpVector = fillBPPrelimHelpGUIVector();
		PropList proplist = new PropList("HelpProps");
		proplist.setValue("HelpKey", "SMGUI.BigPicture");
		new HelpJDialog(this, helpVector, proplist);
	}	
	else if (action.equals(__BUTTON_RUN)) {
		runSmDelta();
	}
	else if (action.equals(__BUTTON_SAVE)) {
		saveSmDeltaFile();
	}
	else if (action.equals(__BUTTON_LOAD)) {
		JGUIUtil.setWaitCursor(this, true);
		String directory = JGUIUtil.getLastFileDialogDirectory();
	
		JFileChooser fc = null;
		if (directory != null) {
			fc = new JFileChooser(directory);
		}
		else {
			fc = new JFileChooser();
		}

		fc.setDialogTitle("Select " + __SmDelta + " Input File");
		SimpleFileFilter tpl = new SimpleFileFilter( __defaultFilenameFilter, __SmDelta + " Input Files");
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
	
		__smdeltaFolder = currDir;
	
		if (!currDir.equalsIgnoreCase(directory)) {
			JGUIUtil.setLastFileDialogDirectory(currDir);
		}
		String filename = fc.getSelectedFile().getName();

		__smdeltaFilename = filename;

		JGUIUtil.setWaitCursor(this, true);

		List<StateMod_GraphNode> theGraphNodes = new Vector<StateMod_GraphNode>();
		try {
			StateMod_GraphNode.readStateModDelPltFile(theGraphNodes, currDir + File.separator + filename);
			List<StateMod_GraphNode> nodes = __tableModel.formLoadData(theGraphNodes);
			StateMod_GraphNode gn = null;
			__worksheet.clear();
			for (int i = 0; i < nodes.size(); i++) {
				gn = nodes.get(i);
				__worksheet.addRow(new StateMod_GraphNode());
				__worksheet.setValueAt(gn.getFileName(), i, 0);
				__worksheet.setValueAt(gn.getType(), i, 1);
				__tableModel.setParmIDComboBoxes(i, gn.getType());
				__worksheet.setValueAt(gn.getDtype(), i, 2);
				__worksheet.setValueAt(gn.getYrAve(), i, 3);
				__worksheet.setValueAt(gn.getID(), i, 4);
			}

			__dirty = false;
		}
		catch (Exception e) {
			Message.printWarning(1, routine,
				"Error loading output control file\n\"" + currDir + File.separator + filename + "\"", this);
			Message.printWarning(2, routine, e);
		}

		if (theGraphNodes.size() > 0) {
			StateMod_GraphNode gn = (StateMod_GraphNode)theGraphNodes.get(0);
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
		__runSmDeltaButton.setEnabled(false);
		__saveTemplateButton.setEnabled(false);
	}
	else {
		__clearWorksheetButton.setEnabled(true);
		__runSmDeltaButton.setEnabled(true);
		__saveTemplateButton.setEnabled(true);
	}
}

/**
Closes the window, prompting to save if the user has not already.
*/
protected void closeWindow() {
	if (__dirty) {
		int x = new ResponseJDialog(this, 
			"Save " + __SmDelta + " Input",
			"You have not saved the " + __SmDelta + " input file.\n\n"
			+ "Continue without saving?",
			ResponseJDialog.YES | ResponseJDialog.NO).response();
		if (x == ResponseJDialog.NO) {
			setDefaultCloseOperation ( WindowConstants.DO_NOTHING_ON_CLOSE);		
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
private List<String> fillBPPrelimHelpGUIVector() {
	List<String> helpVector = new Vector<String>(2);
	helpVector.add( "This tool helps you create an input file for the " + __SmDelta);
	helpVector.add( "program, which allows comparisons of different scenarios, years, ");
	helpVector.add( "data types, etc.  Each section in the input file (file, data type, ");
	helpVector.add( "parameters, ID list and year) is constructed in this tool.  Each ");
	helpVector.add( "time you specify a new file name, the entire row must be filled.");
	helpVector.add( "This is a new section in the input file.  If you wish to ");
	helpVector.add( "perform the analysis on every station, set the identifier to \"0\".");
	helpVector.add( "If you wish to specify a list of identifiers, fill in every column ");
	helpVector.add( "on the first row and only the identifier on subsequent rows.");
	helpVector.add("");

	helpVector.add( "The file name should either be one of the binary files (.b43 or .b44");
	helpVector.add( "for example)or the .xdd file for diversions or .xre for ");
	helpVector.add( "reservoirs if full reports have been created.");

	helpVector.add( "StateMod determines which stations to include in the reports based");
	helpVector.add( "on the output control contents.  Therefore, if your results don't");
	helpVector.add( "include stations you wish to compare, look at your output control");
	helpVector.add( "file.  Remember that changes to that file will not be reflected in ");
	helpVector.add( "the BigPicture plot until another StateMod simulation has been run.");
	helpVector.add("");

	helpVector.add( "For example, to perform difference comparisons between average");
	helpVector.add( "River_Outflow and River_Outflow in 1995 of 4 different stations, ");
	helpVector.add( "the table would look like the following:");
	helpVector.add( "");
	helpVector.add( "Stream       River_Outflow   09152500      Ave     whiteH.xdd");
	helpVector.add( "                             09144250");
	helpVector.add( "                             09128000");
	helpVector.add( "                             09149500");
	helpVector.add( "Stream       River_Outflow   09152500      1995    whiteH.xdd");
	helpVector.add( "                             09144250");
	helpVector.add( "                             09128000");
	helpVector.add( "                             09149500");
	helpVector.add( "");
	helpVector.add( "The result is a single value (the difference) for each station");

	helpVector.add( "");
	helpVector.add( "See additional help through the \"More help\" button below.");
	helpVector.add( "Note that the additional help may take some time to display.");
	return helpVector;
}

/**
This method is called by the SMbigPictureGrid getData()method.
REVISIT (JTS - 2003-08-25) 
this should be used instead by whatever method saves the data to a file
@return StateMod_GraphNode.RUNTYPE_*, indicating SmDelta run mode.
*/
public int getRunType() {
	String selected = __smdeltaRunModeSimpleJComboBox.getSelected();
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
Run SmDelta as follows:
<ol>
<li>	Run SmDelta.</li>
<li>	Convert SmDelta .xgr output file to a more general .csv file format.</li>
<li>	Display the .csv file contents as a layer on the map (if requested).</li>
</ol>
*/
public void runSmDelta() {
	String routine = "StateMod_RunSmDelta_JFrame.runSmDelta";

	if (__worksheet.getRowCount() == 0) {
		Message.printWarning(1, routine, "Input for " + __SmDelta + " has not been defined.");
		return;
	}
	
	new ResponseJDialog(this, "Run " + __SmDelta,
		"The " + __SmDelta + " utility program will now be run to\n"
		+ "prepare summary data that can be displayed on the map.",
		ResponseJDialog.OK).response();

	// Check to see if anything in the template has changed since a template was last read in(if any).
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
		new ResponseJDialog(this, "Save SmDelta Input",
			"Changes have been made to the input "
			+ "file used by \"SmDelta\".\nYou will be asked "
			+ "to select an input file name (convention is *.SmDelta).",
			ResponseJDialog.OK).response();
		//}

		// If false is returned, the save was cancelled and no need to continue.
		if (!saveSmDeltaFile()) {
			return;
		}
	}

	// Construct the(*.xgr)output file name created by SmDelta...

	int index = __smdeltaFilename.indexOf(".");
	if (index == -1) {
		Message.printWarning(1, routine, 
			"Don't know how to handle filename from output.  "
			+ "You must include a \".\" in the filename.", this);
		return;
	}
	String xgrOutputFilename = __smdeltaFilename.substring(0, index).toUpperCase() + ".XGR";

	File xgrFile = new File(__smdeltaFolder + File.separator + xgrOutputFilename);

	// Delete the file if it exists so we don't get bogus output...

	if (xgrFile.exists()) {
		Message.printStatus(1, routine, "Deleting existing " + __SmDelta
			+ " output file \"" + __smdeltaFolder + File.separator
			+ xgrOutputFilename + "\"");
		xgrFile.delete();
		xgrFile = null;
	}

	// Also delete the SmDelta log file so if there is an error we can display the correct one...

	// Start with the input file, and strip off the extension and replace with "log".
	String logfileString = __smdeltaFilename.substring(0, index)+ ".log";

	File logfile = new File(logfileString);
	if (logfile.exists()) {
		Message.printStatus(1, routine, "Deleting existing SmDelta log file \"" + logfileString + "\"");
		logfile.delete();
		logfile = null;
	}

	// Run command using GUI...

	PropList props = new PropList("SmDelta");
	// This is modal...
	String [] command_array = new String[2];
	command_array[0] = StateMod_Util.getSmDeltaExecutable();
	command_array[1] = __smdeltaFolder + File.separator + __smdeltaFilename;
	ProcessManager pm = new ProcessManager(command_array);
	ProcessManagerJDialog pmgui = new ProcessManagerJDialog( this,"StateMod SmDelta", pm, props);

	int exitStatus = pmgui.getExitStatus();
	Message.printStatus(1, routine, "exitStatus is " + exitStatus);

	if (exitStatus != 0) {
		// Try checking for the log file.  If it does not exist, then SmDelta is probably not in the path...
		logfile = new File(logfileString);
		if (logfile.exists()) {
			int x = new ResponseJDialog(this, 
				"SmDelta Unsuccessful",
				"SmDelta did not complete successfully.\n\n"
				+ "View SmDelta log file?",
				ResponseJDialog.YES | ResponseJDialog.NO).response();
			if (x == ResponseJDialog.YES) {
				try {	
					String[] command_array2 = new String[2];
					command_array2[0] = StateMod_GUIUtil._editorPreference;
					command_array2[1] = logfileString;
					ProcessManager p = new ProcessManager( command_array2);
					// This will run as a thread until the process is shut down...
					Thread t = new Thread(p);
					t.start();
				}
				catch (Exception e2) {
					Message.printWarning(1, routine, "Unable to view/edit file \"" + logfileString + "\"");
				}
			}
		}
		else {
			// Log file does not exist...
			new ResponseJDialog(this, 
				"SmDelta Unsuccessful",
				"SmDelta did not complete successfully.\n\n" +
				"No SmDelta log file is available.  Verify that SmDelta is in the PATH.",
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
	// file in the directory corresponding to the SmDelta .rsp file
	// directory.  If so, ask user for a .csv filename to save converted
	// information to.  Note that this is different from the StateMod report
	// which puts the output in the directory of the response file for each
	// individual run.  For SmDelta, the output is always in the directory
	// where the SmDelta input file is.
	if (Message.isDebugOn) {
		Message.printDebug(10, routine, "Looking for xgr file: "
			+ __smdeltaFolder + File.separator + xgrOutputFilename);	
	}
	//File xgrOutput = new File(__templateLocation + xgrOutputFilename);
	// Reopen this file to see if output was created...
	xgrFile = new File(__smdeltaFolder + File.separator + xgrOutputFilename);

	// Sometimes need to wait for the file to be created...
	/* TODO take this out - the new ProcessManager seems to be better
		behaved so see if we can do without.
	if (TimeUtil.waitForFile(__templateLocation +
		xgrOutputFilename, 500, 120)) {}
	*/
	
	if (xgrFile.exists()) {
		// Describe to users what is going to happen.  This is done with
		// a dialog because the JFileChooser does not have a way to add more instructions...
		int response = new ResponseJDialog(this,
			"Save SmDelta Output as Text File",
			"SmDelta was successful.\n\nThe output will now "
			+ "be converted from " + xgrOutputFilename
			+ " format to a comma delimited format\n"
			+ "that can be used directly by the StateMod GUI, GIS"
			+ " applications and other software.\n"
			+ "You will be asked to select an output filename "
			+ "(convention is *.csv).\n\n"
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
	
		fc.setDialogTitle("Select Filename for SmDelta Text Output");
		SimpleFileFilter tpl = new SimpleFileFilter("csv","Comma Separated Value (CSV) Files");
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

		__smdeltaFolder = currDir;
	
		if (!currDir.equalsIgnoreCase(directory)) {
			JGUIUtil.setLastFileDialogDirectory(currDir);
		}
		String filename = fc.getSelectedFile().getName();
		filename = IOUtil.enforceFileExtension(filename, "csv");

		StateMod_DeltaPlot smdeltaOutput = new StateMod_DeltaPlot();
		try {
			smdeltaOutput.readStateModDeltaOutputFile( __smdeltaFolder + File.separator + xgrOutputFilename);
			smdeltaOutput.writeArcViewFile(	null, currDir + File.separator + filename, null);

			// Prompt the user if they want to add to the map.
			// Sometimes all they are trying to do is to run SmDelta
			// so they can look at the text output.

			response = new ResponseJDialog(this,
				"Display " + __SmDelta + " Output on Map",
				"Do you want to display the " + __SmDelta + " output as "
				+ "a map layer?\nYou can add to the map later if you answer No.",
				ResponseJDialog.YES|ResponseJDialog.NO).response();
			if (response == ResponseJDialog.NO) {
				pmgui.toFront();
				return;
			}
	/*
	TODO (JTS - 2003-08-25)
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
	else {
		Message.printWarning(1, routine, __smdeltaFolder + xgrOutputFilename + " file never created.", this);
	}
}

/**
Saves the SmDelta plot file.
*/
private boolean saveSmDeltaFile() {
	// TODO (JTS - 2003-08-27)
	// check for formatted cells and disallow saving if there are errors

	String routine = "StateMod_RunDeltaPlot_JFrame.saveSmDeltaFile";

	JGUIUtil.setWaitCursor(this, true);
	String directory = JGUIUtil.getLastFileDialogDirectory();

	JFileChooser fc = null;
	if (directory != null) {
		fc = new JFileChooser(directory);
	}
	else {
		fc = new JFileChooser();
	}

	fc.setDialogTitle("Select " + __SmDelta + " Input File to Save");
	SimpleFileFilter tpl = new SimpleFileFilter(__defaultFilenameFilter, __SmDelta + " Input Files");
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

	__smdeltaFolder = currDir;

	if (!currDir.equalsIgnoreCase(directory)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	String filename = fc.getSelectedFile().getName();
	filename = IOUtil.enforceFileExtension(filename, __SmDelta);

	__smdeltaFilename = filename;

	JGUIUtil.setWaitCursor(this, true);

	@SuppressWarnings("unchecked")
	List<StateMod_GraphNode>theGraphNodes = (List<StateMod_GraphNode>)__tableModel.formSaveData(__worksheet.getAllData());

	StateMod_GraphNode gn = null;
	int runtype = getRunType();
	for (int i = 0; i < theGraphNodes.size(); i++) {
		gn = theGraphNodes.get(i);
		gn.setSwitch(runtype);
	}
	try {	
		StateMod_GraphNode.writeStateModDelPltFile(null, currDir + File.separator + filename, theGraphNodes, null);
			__dirty = false;
		} 
	catch (Exception e) {
		Message.printWarning(1, routine,
			"Error saving output control file\n" + "\"" + currDir + File.separator + filename + "\"", this);
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
TODO (JTS - 2003-08-25) This should actually be called when the values are read in from a file.
@param runType StateMod_GraphNode.RUNTYPE_* values.
*/
public void setRunType(int runType) {
	if (runType == StateMod_GraphNode.RUNTYPE_SINGLE) {
		__smdeltaRunModeSimpleJComboBox.select(__DELPLT_RUN_MODE_SINGLE);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_MULTIPLE) {
		__smdeltaRunModeSimpleJComboBox.select( __DELPLT_RUN_MODE_MULTIPLE);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_DIFFERENCE) {
		__smdeltaRunModeSimpleJComboBox.select( __DELPLT_RUN_MODE_DIFFERENCE);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_DIFFX) {
		__smdeltaRunModeSimpleJComboBox.select(__DELPLT_RUN_MODE_DIFFX);
	}
	else if (runType == StateMod_GraphNode.RUNTYPE_MERGE) {
		__smdeltaRunModeSimpleJComboBox.select(__DELPLT_RUN_MODE_MERGE);
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

	__smdeltaFilename = "";

	__addRowButton = new SimpleJButton(__BUTTON_ADD_ROW, this);
	__deleteRowButton = new SimpleJButton(__BUTTON_DELETE_ROWS, this);
	__saveTemplateButton = new SimpleJButton(__BUTTON_SAVE, this);
	__clearWorksheetButton = new SimpleJButton(__BUTTON_REMOVE_ALL_ROWS, this);
	__selectTemplateButton = new SimpleJButton(__BUTTON_LOAD, this);

	__helpButton = new SimpleJButton(__BUTTON_HELP, this);
	__closeButton = new SimpleJButton(__BUTTON_CLOSE, this);
	__runSmDeltaButton = new SimpleJButton(__BUTTON_RUN, this);

	__smdeltaRunModeSimpleJComboBox = new SimpleJComboBox();
	__smdeltaRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_SINGLE);
	__smdeltaRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_MULTIPLE);
	__smdeltaRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_DIFFERENCE);
	__smdeltaRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_DIFFX);
	__smdeltaRunModeSimpleJComboBox.add(__DELPLT_RUN_MODE_MERGE);
	__smdeltaRunModeSimpleJComboBox.addItemListener(this);
	// Default is to select multiple mode...
	__smdeltaRunModeSimpleJComboBox.select(1);

	__autoLineCopyJCheckBox = new JCheckBox( "Automatically fill initial line content on \"Add a Row\".", true);

	// Make a main panel to be the resizable body of the frame...
	JPanel mainPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	mainPanel.setLayout(gb);

	GridLayout gl = new GridLayout(2, 2, 2, 2);
	JPanel topPanel = new JPanel();
	topPanel.setLayout(gl);

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
		GridBagConstraints.NONE, GridBagConstraints.NORTH);
	y+=3;
	JPanel runTypeJPanel = new JPanel(new FlowLayout());
	runTypeJPanel.add(new JLabel(__SmDelta + " run mode:"));
	runTypeJPanel.add(__smdeltaRunModeSimpleJComboBox);
	JGUIUtil.addComponent(mainPanel, runTypeJPanel,
		0, y, 10, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(1) File:  For reservoirs specify the ASCII "
		+ ".xre or binary .b44 file."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"         For other node types specify the "
		+ "ASCII .xdd or binary .b43 file.  Or, specify"
		+ " blank if continuing list of IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(2) Station type:  diversion, instream, "
		+ "reservoir, stream, streamID (0* gages)"
		+ ", well, or blank if continuing list of IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(3) Parameter:  parameters from StateMod "
		+ "output, or blank if continuing list of IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(4) Year/Ave:  Enter Ave, 4-digit year "
		+ "(e.g., 1989) or year and month (e.g., "
		+ "1989 NOV), or blank if continuing list of"
		+ " IDs."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, new JLabel(
		"(5) ID:  0 (zero) for all or enter a specific "
		+ "identifier."),
		0, ++y, 10, 1, 1, 0, 
		0, 4, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainPanel, __autoLineCopyJCheckBox,
		0, ++y, 1, 1, 0, 0, 
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	PropList p = 
		new PropList("StateMod_RunDeltaPlot_JFrame.JWorksheet");
	p.add("JWorksheet.ShowRowHeader=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.RowColumnBackground=LightGray");
	p.add("JWorksheet.ShowPopupMenu=true");

	File file = new File(__dataset.getDataSetDirectory());
	__smdeltaFolder = __dataset.getDataSetDirectory();
	JGUIUtil.setLastFileDialogDirectory(__dataset.getDataSetDirectory());
	List<String> filters = new Vector<String>();
	filters.add("xre");
	filters.add("b44");
	filters.add("xdd");
	filters.add("b43");
	SimpleFileFilter sff = new SimpleFileFilter(filters, "etc");
	File[] files = file.listFiles(sff);

	List<String> filenames = new Vector<String>();
	filenames.add("");
	filenames.add(OPTION_BROWSE);
	for (int i = 0; i < files.length; i++) {
		filenames.add(files[i].getName());
		Message.printStatus(1, "", "File " + i + ": '" + files[i].getName() + "'");
	}
	
	int[] widths = null;
	JScrollWorksheet jsw = null;
	@SuppressWarnings("unchecked")
	List<StateMod_Diversion> ddsList = (List<StateMod_Diversion>)__diversionComp.getData(); 
	@SuppressWarnings("unchecked")
	List<StateMod_InstreamFlow> ifsList = (List<StateMod_InstreamFlow>)__instreamFlowComp.getData(); 
	@SuppressWarnings("unchecked")
	List<StateMod_Reservoir> resList = (List<StateMod_Reservoir>)__reservoirComp.getData(); 
	@SuppressWarnings("unchecked")
	List<StateMod_StreamGage> risList =	(List<StateMod_StreamGage>)__streamGageComp.getData();
	@SuppressWarnings("unchecked")
	List<StateMod_Well> wellList = (List<StateMod_Well>)__wellComp.getData();
	try {
		__tableModel = new
			StateMod_RunSmDelta_TableModel(this, new ArrayList<StateMod_GraphNode>(),
			resList,
			ddsList,
			ifsList,
			risList,
			wellList);
			
		StateMod_RunSmDelta_CellRenderer crg = new StateMod_RunSmDelta_CellRenderer(__tableModel);
	
		jsw = new JScrollWorksheet(crg, __tableModel, p);
		__worksheet = jsw.getJWorksheet();

		__worksheet.setColumnJComboBoxValues(0, filenames, true);

		List<String> vn = StateMod_Util.arrayToList( StateMod_GraphNode.node_types);
		
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
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(mainPanel, gridPanel, 
		0, ++y, 10, 12, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	y += 11;	// To account for grid height

	// Add bottom buttons - these are alphabetical so be
	// careful if more are added

	finalButtonPanel.add(__runSmDeltaButton);
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
		GridBagConstraints.VERTICAL, GridBagConstraints.SOUTH);

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
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(message_JPanel, __statusTextField, 
		9, 0, 1, 1, 
		0, 0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH);

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
