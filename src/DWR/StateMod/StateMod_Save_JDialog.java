// StateMod_Save_JDialog - dialog to manage response file in a worksheet format

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

//------------------------------------------------------------------------------
// StateMod_Save_JDialog - dialog to manage response file in a worksheet 
//	format
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2003-09-10	J. Thomas Sapienza, RTi	Initial version.
// 2003-10-15	Steven A. Malers, RTi	* Enable the save code.
// 					* Select all files initially.
// 2004-01-22	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-08-13	JTS, RTi		Added code for saving StateMod data
// 					to appropriate files.
// 2004-08-25	JTS, RTi		Updated the GUI.
// 2006-03-04	SAM, RTi		* Change so that "Carry forward old
//					  comments?" checkbox is selected by
//					  default - users should not
//					  accidentally discard old comments.
//					* Allow a cancel when saving the
//					  response file.
//					* Don't actually save files that have
//					  no name - this is a side-effect of
//					  opening old data sets and removing
//					  "dum" files from the response file.
// 2006-08-22	SAM, RTi		* Add plans when writing.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import RTi.TS.TS;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.YearType;

/**
This dialog displays a list of all the data set components that have been changed
and prompts the user to select the ones that should be saved.
*/
@SuppressWarnings("serial")
public class StateMod_Save_JDialog extends JDialog
implements ActionListener, WindowListener {

/**
Labels for the buttons on the gui.
*/
private final String 
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_HELP = "Help",
	__BUTTON_SAVE = "Save Selected Files";

/**
Checkbox for choosing to overwrite or update files to be saved.
*/
private JCheckBox __updateCheckbox;

/**
The JFrame on which this dialog is displayed.
*/
private JFrame __parent;

/**
The worksheet displayed on the gui.
*/
private JWorksheet __worksheet;

/**
The buttons on the gui.
*/
private SimpleJButton 	
	__cancelButton,
	__helpButton,
	__saveButton;

/**
The dataset for which to prompt to save data set components.
*/
private StateMod_DataSet __dataset = null;

/**
The dataset window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm = null;

/**
The table model to hold the JWorksheet's information.
*/
private StateMod_Save_TableModel __tableModel;

/** 
Constructor.
@param parent the JFrame on which to display this dialog.  Must not be null.
@param dataset the dataset that is being worked with.
@param datasetWindowManager the window manager that is managing this window.
*/
public StateMod_Save_JDialog ( JFrame parent, StateMod_DataSet dataset,
				StateMod_DataSet_WindowManager datasetWindowManager )
{	super(parent, "Save Modified StateMod Files", true);

	__dataset = dataset;
	__dataset_wm = datasetWindowManager;
	__parent = parent;

	setupGUI();
}

/**
Responds to action performed events.
@param ae the ActionEvent that occurred.
*/
public void actionPerformed(ActionEvent ae) {
	String action = ae.getActionCommand();

	if (action.equals(__BUTTON_CANCEL)) {
		closeWindow();
	}
	else if (action.equals(__BUTTON_SAVE)) {
		if ( saveData () ) {
			if ( __dataset_wm != null ) {
				__dataset_wm.updateWindowStatus ( StateMod_DataSet_WindowManager.WINDOW_MAIN );
			}
			closeWindow();
		}
	}
	else if (action.equals(__BUTTON_HELP)) {
		// TODO HELP (JTS - 2003-09-10)
	}
}

/**
Checks the data for validity before saving.
@return 0 if the text fields are okay, 1 if fatal errors exist, and -1 if only non-fatal errors exist.
*/
private int checkInput()
{	String routine = "StateMod_Save_JDialog.checkInput";
	
	String warning = "";
	int fatal_count = 0;

	/*
	// TODO - need to make sure that if a component file name has changed
	// that the response file is forced to be saved???  This may require
	// that the data set maintain an original copy of itself after creation

	if ( ... ) {
	warning += "\n" + comp.getComponentName() +
		" has data but no file name is specified.";
		++fatal_count;
	}
	*/

	if ( warning.length() > 0 ) {
		warning = "\nResponse file:  " + warning + "\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, warning );
		if ( fatal_count > 0 ) {
			// Fatal errors...
			return 1;
		}
		else {
			// Nonfatal errors...
			return -1;
		}
	}
	else {
		// No errors...
		return 0;
	}
}

/**
Disposes of the JDialog and closes it.
*/
private void closeWindow() {
	dispose();
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__parent = null;
	__worksheet = null;
	__cancelButton = null;
	__helpButton = null;
	__saveButton = null;
	__dataset = null;
	__tableModel = null;
	super.finalize();
}

/**
Save the data.
@return true if the save was successful, false if not.
*/
private boolean saveData()
{	String routine = "StateMod_Save_JDialog.saveData";
	if ( checkInput() == 1 ) {
		return false;
	}

	// Else save the data...

	int[] selectedRows = __worksheet.getSelectedRows();
	int comp_type;
	DataSetComponent comp = null;
	int error_count = 0;	// Counter for save errors.
	String newFilename0 = null;	// Used within path
	String newFilename = null;
	String oldFilename = null;
	List<String> comments = new Vector<String>();
	// TODO - add a checkbox to the display.
	//if ( __add_revision_comments_JCheckBox.isSelected() ) {
	comments.add("Modification to data made interactively by user with " + IOUtil.getProgramName() + " " 
		+ IOUtil.getProgramVersion());
	if (__updateCheckbox.isSelected()) {
		comments.add("Updated by StateModGUI");
	}
	//}
	for (int i = 0; i < selectedRows.length; i++) {	
		try {
			comp_type = __tableModel.getRowComponentNum(i);
		
			comp = (DataSetComponent)__dataset.getComponentForComponentType( comp_type );
			newFilename0 = comp.getDataFileName();
			newFilename = __dataset.getDataFilePathAbsolute(comp);

			if (__updateCheckbox.isSelected()) {
				oldFilename = newFilename;
			}
			else {
				oldFilename = null;
			}

			if ( comp_type == StateMod_DataSet.COMP_RESPONSE ) {
				// TODO - need to track the original file name...
				StateMod_DataSet.writeStateModFile ( __dataset, oldFilename, newFilename, comments );
				// Mark the component clean...
				comp.setDirty ( false );
			}
			else if ( newFilename0.length() == 0 ) {
				// SAM 2006-03-04...
				// Just set to not dirty.  If someone sets a filename to blank and actually makes
				// changes, they don't know what they are doing.
				comp.setDirty ( false );
			}
			else if ( newFilename0.length() > 0 ) {
				try {
					saveComponent(comp, oldFilename, newFilename, comments);
				}
				catch (Exception e) {
					Message.printWarning(1, routine, "Error saving file \"" + newFilename + "\"");
					Message.printWarning(2, routine, e);
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error saving " + comp.getComponentName() +
			" to \"" + newFilename + "\"" );
			Message.printWarning ( 2, routine, e );
			++error_count;
		}
	}

	if ( error_count > 0 ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "StateMod_Save_JDialog.setupGUI";

	addWindowListener(this);

	PropList p = new PropList("StateMod_Save_JDialog.JWorksheet");
	/*
	p.add("JWorksheet.CellFont=Courier");
	p.add("JWorksheet.CellStyle=Plain");
	p.add("JWorksheet.CellSize=11");
	p.add("JWorksheet.HeaderFont=Arial");
	p.add("JWorksheet.HeaderStyle=Plain");
	p.add("JWorksheet.HeaderSize=11");
	p.add("JWorksheet.HeaderBackground=LightGray");
	p.add("JWorksheet.RowColumnPresent=false");
	*/
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=MultipleDiscontinuousRowSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		__tableModel = new StateMod_Save_TableModel(__dataset);
		StateMod_Save_CellRenderer crr = new StateMod_Save_CellRenderer(__tableModel);
	
		jsw = new JScrollWorksheet(crr, __tableModel, p);
		__worksheet = jsw.getJWorksheet();

		widths = crr.getColumnWidths();
		// Select all the rows initially...
		__worksheet.selectAllRows();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(__parent);

	__helpButton = new SimpleJButton(__BUTTON_HELP, this);
	__helpButton.setEnabled(false);
	__saveButton = new SimpleJButton(__BUTTON_SAVE, this);
	__saveButton.setToolTipText("Save data to file(s).");
	__cancelButton = new SimpleJButton(__BUTTON_CANCEL, this);
	__cancelButton.setToolTipText("Cancel without saving data to file(s).");

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	JGUIUtil.addComponent(panel, new JLabel("Data from the following files have been modified."),
		0, 0, 2, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(panel, 
		new JLabel("Select files to be saved and press the \"Save Selected Files\" button."),
		0, 1, 2, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(panel,
		new JLabel("To change the filenames, use the Data...Control...Response menu"),
		0, 2, 2, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(panel, new JLabel( "Data set base name (from *.rsp):  "),
		0, 3, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
	JGUIUtil.addComponent(panel, new JLabel(__dataset.getBaseName()),
		1, 3, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(panel, new JLabel("Data set directory:  "),
		0, 4, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
	JGUIUtil.addComponent(panel, new JLabel(__dataset.getDataSetDirectory()),
		1, 4, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);		

	__updateCheckbox = new JCheckBox((String)null, true);
	JGUIUtil.addComponent(panel, __updateCheckbox,
		0, 6, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);		
	JGUIUtil.addComponent(panel, new JLabel("Carry forward old file comments?"),
		1, 6, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);		

	getContentPane().add("North", panel);
	getContentPane().add("Center", jsw);

	JPanel button_panel = new JPanel();
	button_panel.add(__saveButton);
	button_panel.add(__cancelButton);
//	button_panel.add(__helpButton);

	JPanel bottom_panel = new JPanel();
	bottom_panel.setLayout(new BorderLayout());
	getContentPane().add("South", bottom_panel);
	bottom_panel.add("South", button_panel);

	pack();
	setSize(700, 500);

	JGUIUtil.center(this);

	if (widths != null) {
		__worksheet.setColumnWidths(widths, __parent.getGraphics());
	}

	setVisible(true);
}

/**
Responds to Window Activated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowActivated(WindowEvent e) {}

/**
Responds to Window closed events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowClosed(WindowEvent e) {}

/**
Responds to Window closing events; closes the window and marks it closed in StateMod_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	closeWindow();
}

/**
Responds to Window deactivated events; saves the current information.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to Window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to Window iconified events; saves the current information.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {}

/**
Responds to Window opened events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpened(WindowEvent e) {}

/**
Responds to Window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e) {}

private void saveComponent(DataSetComponent comp, String oldFilename,String newFilename, List<String> comments) 
throws Exception {
	boolean daily = false;
	int type = comp.getComponentType();
	Object data = comp.getData();
	String name = null;

	switch (type) {
	////////////////////////////////////////////////////////
	// StateMod_* classes
		case StateMod_DataSet.COMP_CONTROL:
			StateMod_DataSet.writeStateModControlFile(__dataset, oldFilename, newFilename, comments);
			name = "Control";
			break;
		case StateMod_DataSet.COMP_DELAY_TABLES_DAILY:
			@SuppressWarnings("unchecked")
			List<StateMod_DelayTable> delayTablesDaily = (List<StateMod_DelayTable>)data;
			StateMod_DelayTable.writeStateModFile(oldFilename,
				newFilename, delayTablesDaily, comments, __dataset.getInterv(), -1);
			name = "Delay Tables Daily";
			break;
		case StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY:
			@SuppressWarnings("unchecked")
			List<StateMod_DelayTable> delayTablesMonthly = (List<StateMod_DelayTable>)data;
			StateMod_DelayTable.writeStateModFile(oldFilename,
				newFilename, delayTablesMonthly, comments, __dataset.getInterv(), -1);
			name = "Delay Tables Monthly";
			break;
		case StateMod_DataSet.COMP_DIVERSION_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_Diversion> diversionStations = (List<StateMod_Diversion>)data;
			StateMod_Diversion.writeStateModFile(oldFilename, newFilename, diversionStations, comments, daily );
			name = "Diversion";
			break;
		case StateMod_DataSet.COMP_DIVERSION_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_DiversionRight> diversionRights = (List<StateMod_DiversionRight>)data;
			StateMod_DiversionRight.writeStateModFile(oldFilename, newFilename, diversionRights, comments, daily);
			name = "Diversion Rights";
			break;
		case StateMod_DataSet.COMP_INSTREAM_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlow> instreamFlow = (List<StateMod_InstreamFlow>)data;
			StateMod_InstreamFlow.writeStateModFile(oldFilename, newFilename, instreamFlow, comments, daily);
			name = "Instream";
			break;
		case StateMod_DataSet.COMP_INSTREAM_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlowRight> instreamFlowRights = (List<StateMod_InstreamFlowRight>)data;
			StateMod_InstreamFlowRight.writeStateModFile( oldFilename, newFilename, instreamFlowRights, comments );
			name = "Instream Rights";
			break;
		case StateMod_DataSet.COMP_OPERATION_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_OperationalRight> operationalRights = (List<StateMod_OperationalRight>)data;
			// 2 is the file version (introduced for StateMod version 12 change)
			StateMod_OperationalRight.writeStateModFile( oldFilename, newFilename, 2, operationalRights, comments,
				__dataset );
			name = "Operational Rights";
			break;
		case StateMod_DataSet.COMP_PLANS:
			@SuppressWarnings("unchecked")
			List<StateMod_Plan> planStations = (List<StateMod_Plan>)data;
			StateMod_Plan.writeStateModFile(oldFilename, newFilename, planStations, comments);
			name = "Plan";
			break;
		case StateMod_DataSet.COMP_RESERVOIR_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_Reservoir> reservoirStations = (List<StateMod_Reservoir>)data;
			StateMod_Reservoir.writeStateModFile(oldFilename, newFilename, reservoirStations, comments, daily);
			name = "Reservoir";
			break;
		case StateMod_DataSet.COMP_RESERVOIR_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_ReservoirRight> reservoirRights = (List<StateMod_ReservoirRight>)data;
			StateMod_ReservoirRight.writeStateModFile(oldFilename, newFilename, reservoirRights, comments);
			name = "Reservoir Rights";
			break;
		case StateMod_DataSet.COMP_RESPONSE:
			StateMod_DataSet.writeStateModFile(__dataset, oldFilename, newFilename, comments );
			name = "Response";
			break;
		case StateMod_DataSet.COMP_RIVER_NETWORK:
			@SuppressWarnings("unchecked")
			List<StateMod_RiverNetworkNode> riverNodes = (List<StateMod_RiverNetworkNode>)data;
			StateMod_RiverNetworkNode.writeStateModFile(oldFilename, newFilename, riverNodes, comments, true);
			name = "River Network";
			break;
		case StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate> streamEstimateStations = (List<StateMod_StreamEstimate>)data;
			StateMod_StreamEstimate.writeStateModFile(oldFilename, newFilename, streamEstimateStations, comments, daily);
			name = "Stream Estimate";
			break;
		case StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS:
			@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate_Coefficients> streamEstimateCoefficients = (List<StateMod_StreamEstimate_Coefficients>)data;
			StateMod_StreamEstimate_Coefficients.writeStateModFile( oldFilename, newFilename, streamEstimateCoefficients, comments );
			name = "Stream Estimate Coefficients";
			break;
		case StateMod_DataSet.COMP_STREAMGAGE_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_StreamGage> streamGageStations = (List<StateMod_StreamGage>)data;
			StateMod_StreamGage.writeStateModFile(oldFilename, newFilename, streamGageStations, comments, daily);
			name = "Streamgage Stations";
			break;
		case StateMod_DataSet.COMP_WELL_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_Well> wellStations = (List<StateMod_Well>)data;
			StateMod_Well.writeStateModFile(oldFilename, newFilename, wellStations, comments);
			name = "Well";
			break;
		case StateMod_DataSet.COMP_WELL_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_WellRight> wellRights = (List<StateMod_WellRight>)data;
			StateMod_WellRight.writeStateModFile(oldFilename, newFilename, wellRights, comments, (PropList)null);
			name = "Well Rights";
			break;

	//////////////////////////////////////////////////////
	// StateMod Time Series
		case StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY:
		case StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY:
		case StateMod_DataSet.COMP_DEMAND_TS_DAILY:
		case StateMod_DataSet.COMP_DEMAND_TS_AVERAGE_MONTHLY:
		case StateMod_DataSet.COMP_DEMAND_TS_MONTHLY:
		case StateMod_DataSet.COMP_DEMAND_TS_OVERRIDE_MONTHLY:
		case StateMod_DataSet.COMP_DIVERSION_TS_DAILY:
		case StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY:
		case StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY:
		case StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY:
		case StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_DAILY:
		case StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY:
		case StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY:
		case StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_DAILY:
		case StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY:
		case StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_DAILY:
		case StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY:
		case StateMod_DataSet.COMP_STREAMESTIMATE_NATURAL_FLOW_TS_DAILY:
		case StateMod_DataSet.COMP_STREAMESTIMATE_NATURAL_FLOW_TS_MONTHLY:
		case StateMod_DataSet.COMP_STREAMGAGE_NATURAL_FLOW_TS_DAILY:
		case StateMod_DataSet.COMP_STREAMGAGE_NATURAL_FLOW_TS_MONTHLY:
		case StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_DAILY:
		case StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY:
		case StateMod_DataSet.COMP_WELL_DEMAND_TS_DAILY:
		case StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY:
		case StateMod_DataSet.COMP_WELL_PUMPING_TS_DAILY:
		case StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY:
			double missing = -999.0;
			YearType yearType = null;
			if (__dataset.getCyrl() == YearType.CALENDAR) {
				yearType = YearType.CALENDAR;
			}
			else if (__dataset.getCyrl() == YearType.WATER) {
				yearType = YearType.WATER;
			}
			else if (__dataset.getCyrl() == YearType.NOV_TO_OCT) {
				yearType = YearType.NOV_TO_OCT;
			}
			int precision = 2;

			// Do the following to avoid warnings
			List<TS> tslist = null;
			if ( data != null ) {
				@SuppressWarnings("unchecked")
				List<TS> tslist0 = (List<TS>)data;
				if ( tslist0.size() > 0) {
					TS ts = tslist0.get(0);
					missing = ts.getMissing();
				}
				tslist = tslist0;
			}
			
			StateMod_TS.writeTimeSeriesList(oldFilename,
				newFilename, comments, tslist, null, null, yearType, missing, precision);
			name = "TS (" + type + ")";
			break;

		default:
			name = "(something: " + type + ")";
			break;
	}
	comp.setDirty(false);
	Message.printStatus(1, "", "Component '" + name + "' written");
}

// TODO SAM 2006-08-22 Why are these here?
/*
public static final int   COMP_OUTPUT_REQUEST = 3;
public final static int   COMP_SOIL_MOISTURE = 26;
public final static int   COMP_NETWORK = 58;
public final static int   COMP_SANJUAN_RIP = 62;
public final static int   COMP_GEOVIEW = 64;
*/

//public final static int   COMP_RESPONSE = 1;
//public final static int   COMP_CONTROL = 2;
//public final static int   COMP_STREAMGAGE_STATIONS = 5;
//public final static int   COMP_DELAY_TABLES_MONTHLY = 11;
//public final static int   COMP_DELAY_TABLES_DAILY = 13;
//public final static int   COMP_DIVERSION_STATIONS = 15;
//public final static int   COMP_DIVERSION_RIGHTS = 16;
//public final static int   COMP_RESERVOIR_STATIONS = 32;
//public final static int   COMP_RESERVOIR_RIGHTS = 33;
//public final static int   COMP_INSTREAM_STATIONS = 39;
//public final static int   COMP_INSTREAM_RIGHTS = 40;
//public final static int   COMP_WELL_STATIONS = 45;
//public final static int   COMP_WELL_RIGHTS = 46;
//public final static int   COMP_STREAMESTIMATE_STATIONS = 52;
//public final static int   COMP_STREAMESTIMATE_COEFFICIENTS = 53;
//public final static int   COMP_RIVER_NETWORK = 57;
//public final static int   COMP_OPERATION_RIGHTS = 60;

/* 
Time series
public final static int   COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY = 6;
public final static int   COMP_STREAMGAGE_HISTORICAL_TS_DAILY = 7;
public final static int   COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY = 8;
public final static int   COMP_STREAMGAGE_BASEFLOW_TS_DAILY = 9;
public final static int   COMP_DIVERSION_TS_MONTHLY = 17;
public final static int   COMP_DIVERSION_TS_DAILY = 18;
public final static int   COMP_DEMAND_TS_MONTHLY = 19;
public final static int   COMP_DEMAND_TS_OVERRIDE_MONTHLY = 20;
public final static int   COMP_DEMAND_TS_AVERAGE_MONTHLY = 21;
public final static int   COMP_DEMAND_TS_DAILY = 22;
public final static int   COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY = 24;
public final static int   COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY = 25;
public final static int   COMP_PRECIPITATION_TS_MONTHLY = 28;
public final static int   COMP_EVAPORATION_TS_MONTHLY = 30;
public final static int   COMP_RESERVOIR_CONTENT_TS_MONTHLY = 34;
public final static int   COMP_RESERVOIR_CONTENT_TS_DAILY = 35;
public final static int   COMP_RESERVOIR_TARGET_TS_MONTHLY = 36;
public final static int   COMP_RESERVOIR_TARGET_TS_DAILY = 37;
public final static int   COMP_INSTREAM_DEMAND_TS_MONTHLY = 41;
public final static int   COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY = 42;
public final static int   COMP_INSTREAM_DEMAND_TS_DAILY = 43;
public final static int   COMP_WELL_PUMPING_TS_MONTHLY = 47;
public final static int   COMP_WELL_PUMPING_TS_DAILY = 48;
public final static int   COMP_WELL_DEMAND_TS_MONTHLY = 49;
public final static int   COMP_WELL_DEMAND_TS_DAILY = 50;
public final static int   COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY = 54;
public final static int   COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY = 55;
*/

}
