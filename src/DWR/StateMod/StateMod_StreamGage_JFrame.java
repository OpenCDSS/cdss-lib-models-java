//------------------------------------------------------------------------------
// StateMod_StreamGage_JFrame - dialog to edit the stream gage (.ris) file
//					information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 07 Jan 1998	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 2002-09-12	SAM, RTi		Move the baseflow time series display
//					from the baseflows window to this
//					window.  Actually, display both the
//					baseflow and historic time series if
//					available.
//------------------------------------------------------------------------------
// 2003-08-18	J. Thomas Sapienza, RTi	Initial Swing version.
// 2003-08-20	JTS, RTi		* Added code so that the gui data is
//					  editable.
//					* Cleaned up the GUI.
//					* Objects can now be pre-selected from
//					  the second constructor.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-08-27	JTS, RTi		Added selectID() to select an ID 
//					on the worksheet from outside the GUI.
// 2003-08-96	SAM, RTi		Update for changes in
//					StateMod_RiverStation
// 2003-09-03	JTS, RTi		Removed buttons for selecting time
//					series to view and replaced with
//					JCheckBoxes.
// 2003-09-03	SAM, RTi		* Always show the checkboxes but disable
//					  base on the time series that are
//					  available from the selected station.
//					* JTS had not fully removed buttons in
//					  previous changes.
// 2003-09-04	JTS, RTi		* Added crunidy combo box. 
//					* Added apply and cancel buttons.
//					* Put search widgets into titled panel.
// 2003-09-05	JTS, RTi		Class is now an item listener in 
//					order to enable/disable graph buttons
//					based on selected checkboxes.
// 2003-09-06	SAM, RTi		Fix problem with graphs from products
//					not recognizing size.
// 2003-09-08	JTS, RTi		* Added checkTimeSeriesButtonsStates()
//					  to enable time series display buttons
//					  appropriately.
//					* Adjusted layout.
// 2003-09-11	SAM, RTi		* Rename class from
//					  StateMod_RiverStation_JFrame to
//					  StateMod_StreamGage_JFrame.
//					* Adjust for general change in name from
//					  "Streamflow Station" to "StreamGage
//					  Station".
// 2003-09-18	SAM, RTi		Add estimated historical daily time
//					series.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2004-01-22	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
//					* windowDeactivated() no longer saves
//					  data because it was causing problems
//					  with the cancel code.
// 2006-01-19	JTS, RTi		* Now implements JWorksheet_SortListener
//					* Reselects the record that was selected
//					  when the worksheet is sorted.
// 2006-08-31	SAM, RTi		* Fix problem where checkboxes were
//					  not being checked to determine which
//					  time series should be plotted.
//					* Fix problem where estimated daily
//					  checkboxes were enabled even though
//					  they should have been disabled always
//					  (until software features are enabled).
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import RTi.GRTS.TSProduct;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_SortListener;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Class to display data about stream gage stations.
*/
public class StateMod_StreamGage_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener, JWorksheet_SortListener {

private boolean __editable = true;

/**
Button group for selecting the kind of search to do.
*/
private ButtonGroup __searchCriteriaGroup;

/**
Data set component containing the form data.
*/
private DataSetComponent __streamGageStationComponent;

/**
Indices of the currently-selected and last-selected stations.
*/
private int
	__currentStationIndex = -1,
	__lastStationIndex = -1;
	
/**
Stores the index of the record that was selected before the worksheet is sorted,
in order to reselect it after the sort is complete.
*/
private int __sortSelectedRow = -1;

/**
Buttons for performing operations on the form.
*/
private JButton 
	__applyJButton,
	__findNext,
	__cancelJButton,
	__helpJButton,
	__closeJButton,
	__graph_JButton,
	__table_JButton,
	__summary_JButton;	

/**
Checkboxes to select the time series to view.
*/
private JCheckBox
	__ts_streamflow_hist_monthly_JCheckBox,
	__ts_streamflow_hist_daily_JCheckBox,
	__ts_streamflow_est_hist_daily_JCheckBox,
	__ts_streamflow_base_monthly_JCheckBox,
	__ts_streamflow_base_daily_JCheckBox,
	__ts_streamflow_est_base_daily_JCheckBox;
	
/**
Radio buttons for selecting the kind of search to do.
*/
private JRadioButton 
	__searchIDJRadioButton,
	__searchNameJRadioButton;

/**
GUI TextFields.
*/
private JTextField 
	__idJTextField,
	__nameJTextField,
	__cgotoJTextField;

/**
Text fields for searching through the worksheet.
*/
private JTextField 
	__searchID,
	__searchName;

/**
Worksheet for displaying stream gage station data.
*/
private JWorksheet __worksheet;

/**
Combo box for displaying crunidy data.
*/
private SimpleJComboBox __crunidyComboBox;

/**
Button label strings.
*/
private final String
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_HELP = "Help",
	__BUTTON_FIND_NEXT = "Find Next";

/**
Data set containing the data for the form.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
Vector of stream gage station data to display in the form.
*/
private List __streamGageStationsVector;

/**
The index in __disables[] of textfields and other components that should NEVER be made editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
Array of JComponents that should be disabled when nothing is selected from the list.
*/
private JComponent[] __disables;

/**
Constructor.
@param dataset the dataset containing the data to show in the form.
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param editable Indicates whether the data in the display should be editable.
*/
public StateMod_StreamGage_JFrame (	StateMod_DataSet dataset, StateMod_DataSet_WindowManager dataset_wm,
	boolean editable)
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Stream Gage Stations", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());

	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__streamGageStationComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
	__streamGageStationsVector = (List)__streamGageStationComponent.getData();
	int size = __streamGageStationsVector.size();
	StateMod_StreamGage s = null;
	for (int i = 0; i < size; i++) {
		s =(StateMod_StreamGage)__streamGageStationsVector.get(i);
		s.createBackup();
	}
	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the dataset containing the data to show in the form.
@param station the object to preselect in the gui.
@param editable Indicates whether the data in the display should be editable.
*/
public StateMod_StreamGage_JFrame(StateMod_DataSet dataset,
StateMod_StreamGage station, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, "Stream Gage Stations", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());

	__dataset = dataset;
	__streamGageStationComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
	__streamGageStationsVector = (List)__streamGageStationComponent.getData();
	int size = __streamGageStationsVector.size();
	StateMod_StreamGage s = null;
	for (int i = 0; i < size; i++) {
		s =(StateMod_StreamGage)__streamGageStationsVector.get(i);
		s.createBackup();
	}

	String id = station.getID();
	int index = StateMod_Util.locateIndexFromID(id, __streamGageStationsVector);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine="StateMod_StreamGage_JFrame.actionPerformed"; 
	if (Message.isDebugOn) {
		Message.printDebug(1, routine, "In actionPerformed: " + e.getActionCommand());
	}

	String action = e.getActionCommand();
	Object o = e.getSource();

	if ( (o == __graph_JButton) || (o == __table_JButton) || (o == __summary_JButton) ) {
		displayTSViewJFrame(o);
	}	
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
		int size = __streamGageStationsVector.size();
		StateMod_StreamGage s = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			s = (StateMod_StreamGage)__streamGageStationsVector.get(i);
			if (!changed && s.changed()) {
				changed = true;
			}
			s.createBackup();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_STREAMGAGE_STATIONS,true);
		}		
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		int size = __streamGageStationsVector.size();
		StateMod_StreamGage s = null;
		for (int i = 0; i < size; i++) {
			s = (StateMod_StreamGage)__streamGageStationsVector.get(i);
			s.restoreOriginal();
		}			
		if (__dataset_wm != null) {
			__dataset_wm.closeWindow(StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE);
		}
		else {
			JGUIUtil.close(this);
		}
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		saveCurrentRecord();
		int size = __streamGageStationsVector.size();
		StateMod_StreamGage s = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			s = (StateMod_StreamGage)__streamGageStationsVector.get(i);
			if (!changed && s.changed()) {
				changed = true;
			}
			s.acceptChanges();
		}				
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_STREAMGAGE_STATIONS,true);
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE );
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_HELP)) {
		// TODO HELP (JTS - 2003-08-18)
	}
	else if (e.getSource() == __searchIDJRadioButton) {
		__searchName.setEditable(false);
		__searchID.setEditable(true);
	}
	else if (e.getSource() == __searchNameJRadioButton) {
		__searchName.setEditable(true);
		__searchID.setEditable(false);
	}		
	else if (action.equals(__BUTTON_FIND_NEXT)) {
		searchWorksheet(__worksheet.getSelectedRow() + 1);
	}
	else if (e.getSource() == __searchID || e.getSource() == __searchName) {
		searchWorksheet(0);
	}	
}
 
/**
Checks the text fields for validity before they are saved back into the data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	List errors = new Vector();
	int errorCount = 0;

	// for each field, check if it contains valid input.  If not,
	// create a string of the format "fieldname -- reason why it
	// is not correct" and add it to the errors vector.  Also increment error count
	
	if (errorCount == 0) {
		return true;
	}

	String plural = " was ";
	if (errorCount > 1) {
		plural = "s were ";
	}
	String label = "The following error" + plural + "encountered trying to save the record:\n";
	for (int i = 0; i < errorCount; i++) {
		label += errors.get(i) + "\n";
	}
	new ResponseJDialog(this, "Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Checks whether the time series display buttons need to be enabled or not based on the JCheckBoxes.
*/
private void checkTimeSeriesButtonsStates() {
	boolean enabled = false;

	if (
		(__ts_streamflow_hist_monthly_JCheckBox.isSelected() && __ts_streamflow_hist_monthly_JCheckBox.isEnabled())
		|| (__ts_streamflow_hist_daily_JCheckBox.isSelected() && __ts_streamflow_hist_daily_JCheckBox.isEnabled())
		|| (__ts_streamflow_est_hist_daily_JCheckBox.isSelected() && __ts_streamflow_est_hist_daily_JCheckBox.isEnabled())
		|| (__ts_streamflow_base_monthly_JCheckBox.isSelected() && __ts_streamflow_base_monthly_JCheckBox.isEnabled())
		|| (__ts_streamflow_base_daily_JCheckBox.isSelected() && __ts_streamflow_base_daily_JCheckBox.isEnabled())
		|| (__ts_streamflow_est_base_daily_JCheckBox.isSelected() && __ts_streamflow_est_base_daily_JCheckBox.isEnabled())
	) {
		enabled = true;
	}
	
	__graph_JButton.setEnabled(enabled);
	__table_JButton.setEnabled(enabled);
	__summary_JButton.setEnabled(enabled);
}

/**
Display the time series.  Create two graphs as needed, one with ACFT monthly data, and one with CFS daily data.
@param action Event action that initiated the display.
*/
private void displayTSViewJFrame(Object o)
{	String routine = "displayTSViewJFrame";

	// Initialize the display...

	PropList display_props = new PropList("StreamGage Station");
	if ( o == __graph_JButton ) {
		display_props.set("InitialView", "Graph");
	}
	else if ( o == __table_JButton ) {
		display_props.set("InitialView", "Table");
	}
	else if ( o == __summary_JButton ) {
		display_props.set("InitialView", "Summary");
	}

	StateMod_StreamGage sta = (StateMod_StreamGage)__streamGageStationsVector.get(__currentStationIndex);

	// display_props.set("HelpKey", "TSTool.ExportMenu");
	display_props.set("TSViewTitleString", StateMod_Util.createDataLabel(sta,true) + " Time Series");
	display_props.set("DisplayFont", "Courier");
	display_props.set("DisplaySize", "11");
	display_props.set("PrintFont", "Courier");
	display_props.set("PrintSize", "7");
	display_props.set("PageLength", "100");

	PropList props = new PropList("StreamGage");
	props.set("Product.TotalWidth", "600");
	props.set("Product.TotalHeight", "400");

	List tslist = new Vector();

	int sub = 0;
	int its = 0;
	TS ts = null;

	if ( (__ts_streamflow_hist_monthly_JCheckBox.isSelected() && (sta.getHistoricalMonthTS() != null) ) ||
		(__ts_streamflow_base_monthly_JCheckBox.isSelected() && (sta.getBaseflowMonthTS() != null) ) ) {
		// Do the monthly graph...
		++sub;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Monthly Data for Stream Gage Station "
			+ sta.getID() + " (" + sta.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = sta.getHistoricalMonthTS();
		if ( (ts != null) && __ts_streamflow_hist_monthly_JCheckBox.isSelected() ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
		ts = sta.getBaseflowMonthTS();
		if ( (ts != null) && __ts_streamflow_base_monthly_JCheckBox.isSelected() ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	if ( (__ts_streamflow_hist_daily_JCheckBox.isSelected() && (sta.getHistoricalDayTS() != null) ) ||
		(__ts_streamflow_base_daily_JCheckBox.isSelected() && (sta.getBaseflowDayTS() != null) ) ) {
		// Do the daily graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Daily Data for Stream Gage Station "
			+ sta.getID() + " (" + sta.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = sta.getHistoricalDayTS();
		if ( (ts != null) && __ts_streamflow_hist_daily_JCheckBox.isSelected() ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
		ts = sta.getBaseflowDayTS();
		if ( (ts != null) && __ts_streamflow_base_daily_JCheckBox.isSelected() ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}
	
	try {
		TSProduct tsproduct = new TSProduct ( props, display_props );
		tsproduct.setTSList ( tslist );
		new TSViewJFrame ( tsproduct );
	}
	catch (Exception e) {
		Message.printWarning(1,routine,"Error displaying time series.");
		Message.printWarning(2, routine, e);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__applyJButton = null;
	__cancelJButton = null;
	__searchCriteriaGroup = null;
	__streamGageStationComponent = null;
	__findNext = null;
	__helpJButton = null;
	__closeJButton = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__searchID = null;
	__searchName = null;
	__worksheet = null;
	__dataset = null;
	__crunidyComboBox = null;
	__streamGageStationsVector = null;
	__ts_streamflow_est_base_daily_JCheckBox = null;
	__ts_streamflow_est_hist_daily_JCheckBox = null;
	super.finalize();
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[14];
	int i = 0;
	
	__disables[i++] = __idJTextField;
	__disables[i++] = __nameJTextField;
	__disables[i++] = __cgotoJTextField;
	__disables[i++] = __graph_JButton;
	__disables[i++] = __table_JButton;
	__disables[i++] = __summary_JButton;
	__disables[i++] = __applyJButton;
	__disables[i++] = __ts_streamflow_hist_monthly_JCheckBox;
	__disables[i++] = __ts_streamflow_hist_daily_JCheckBox;
	__disables[i++] = __ts_streamflow_est_hist_daily_JCheckBox;
	__disables[i++] = __ts_streamflow_base_monthly_JCheckBox;
	__disables[i++] = __ts_streamflow_base_daily_JCheckBox;	
	__disables[i++] = __ts_streamflow_est_base_daily_JCheckBox;
	__disables[i++] = __crunidyComboBox;

	__textUneditables = new int[4];
	__textUneditables[0] = 0;
	__textUneditables[1] = 1;
	__textUneditables[2] = 9;
	__textUneditables[3] = 12;
}

/**
Responds to item state changed events.
@param e the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent e) {
	checkTimeSeriesButtonsStates();
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; calls 'processTableSelection' with the newly-selected index in the table.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	processTableSelection(__worksheet.getSelectedRow());
}

/**
Responds to key typed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyTyped(KeyEvent e) {}

/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {}

/**
Responds to mouse entered events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent e) {}

/**
Responds to mouse exited events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseExited(MouseEvent e) {}

/**
Responds to mouse pressed events; does nothing.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {}

/**
Responds to mouse released events; calls 'processTableSelection' with the newly-selected index in the table.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	processTableSelection(__worksheet.getSelectedRow());
}

/**
Fills in the values for the crunidy combo box.
@param id the id of the currently-selected stream gage station, which will be 
excluded from the list of stream gage stations in the crunidy combo box.
*/
private void populateCrunidyComboBox() {
	__crunidyComboBox.removeAllItems();

	__crunidyComboBox.add("0 - Use average daily value from monthly time series");
	__crunidyComboBox.add("3 - Daily time series are supplied");
	__crunidyComboBox.add("4 - Daily time series interpolated from midpoints of monthly data");

	List idNameVector = StateMod_Util.createDataList(__streamGageStationsVector, true);
	int size = idNameVector.size();

	String s = null;
	for (int i = 0; i < size; i++) {
		s = (String)idNameVector.get(i);
		__crunidyComboBox.add(s.trim());
	}
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it on the form.
@param index the index of the reservoir to display on the form.
*/
private void processTableSelection(int index) {
	String routine = "StateMod_StreamGage_JFrame.processTableSelection";

	__lastStationIndex = __currentStationIndex;
	__currentStationIndex = __worksheet.getOriginalRowNumber(index);
	if (__currentStationIndex < 0) {
		JGUIUtil.disableComponents(__disables, true);
		return;
	}

	// If a time series is available, enable the time series button...
	saveLastRecord();

	StateMod_StreamGage r = (StateMod_StreamGage)__streamGageStationsVector.get(__currentStationIndex);

	JGUIUtil.enableComponents(__disables, __textUneditables, __editable);	
	checkTimeSeriesButtonsStates();
	
	// For checkboxes, do not change the state of the checkbox, only
	// whether enabled - that way if the user has picked a combination of
	// parameters it is easy for them to keep the same settings when
	// switching between stations.  Make sure to do the following after
	// the generic enable/disable code is called above!

	if ( r.getHistoricalMonthTS() != null ) {
		__ts_streamflow_hist_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_streamflow_hist_monthly_JCheckBox.setEnabled(false);
	}

	if ( r.getHistoricalDayTS() != null ) {
		__ts_streamflow_hist_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_streamflow_hist_daily_JCheckBox.setEnabled(false);
	}

	if ( r.getBaseflowMonthTS() != null ) {
		__ts_streamflow_base_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_streamflow_base_monthly_JCheckBox.setEnabled(false);
	}

	if ( r.getBaseflowDayTS() != null ) {
		__ts_streamflow_base_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_streamflow_base_daily_JCheckBox.setEnabled(false);
	}

	__idJTextField.setText(r.getID());
	__nameJTextField.setText(r.getName());
	__cgotoJTextField.setText(r.getCgoto());

	if (__lastStationIndex != -1) {
		String s = __crunidyComboBox.getStringAt(3);
		__crunidyComboBox.removeAt(3);
		__crunidyComboBox.addAlpha(s, 3);
	}
	String s = "" + r.getID() + " - " + r.getName();
	__crunidyComboBox.remove(s);
	__crunidyComboBox.addAt(s, 3);	

	String c = r.getCrunidy();
	if (c.trim().equals("")) {
		if (!__crunidyComboBox.setSelectedPrefixItem(r.getID())) {
			Message.printWarning(2, routine, "No Crunidy value matching '" + r.getID() + "' found in combo box.");
			__crunidyComboBox.select(0);
		}
		else {
			setOriginalCrunidy(r, r.getID());
		}
	}
	else {
		if (!__crunidyComboBox.setSelectedPrefixItem(c)) {
			Message.printWarning(2, routine, "No Crunidy value matching '" + c + "' found in combo box.");
			__crunidyComboBox.select(0);
		}		
	}	
}

/**
Saves the prior record selected in the table; called when moving to a new record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastStationIndex);
}

/**
Saves the current record selected in the table; called when the window is closed or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentStationIndex);
}

/**
Saves the information associated with the currently-selected stream gage
station.  The user doesn't need to hit the return key for the gui to recognize
changes.  The info is saved each time the user selects a different station or pressed the close button.
*/
private void saveInformation(int record) {
	if (!__editable || record == -1) {
		return;
	}

	if (!checkInput()) {
		return;
	}

	StateMod_StreamGage r = (StateMod_StreamGage)__streamGageStationsVector.get(record);

	r.setName(__nameJTextField.getText());
	r.setCgoto(__cgotoJTextField.getText());
	String crunidy = __crunidyComboBox.getSelected();
	int index = crunidy.indexOf(" - ");
	if (index > -1) {
		r.setCrunidy(crunidy.substring(0, index));
	}
	else {
		r.setCrunidy("");
	}
}

/**
Searches through the worksheet for a value, starting at the first row.
If the value is found, the row is selected.
*/
public void searchWorksheet() {
	searchWorksheet(0);
}

/**
Searches through the worksheet for a value, starting at the given row.
If the value is found, the row is selected.
@param row the row to start searching from.
*/
public void searchWorksheet(int row) {
	String searchFor = null;
	int col = -1;
	if (__searchIDJRadioButton.isSelected()) {
		searchFor = __searchID.getText().trim();
		col = 0;
	}
	else {
		searchFor = __searchName.getText().trim();
		col = 1;
	}
	int index = __worksheet.find(searchFor, col, row, 
		JWorksheet.FIND_EQUAL_TO | JWorksheet.FIND_CONTAINS |
		JWorksheet.FIND_CASE_INSENSITIVE | JWorksheet.FIND_WRAPAROUND);
	if (index != -1) {
		selectTableIndex(index);
	}
}

/**
Selects the desired ID in the table and displays the appropriate data in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheet.getRowCount();
	StateMod_StreamGage rs = null;
	for (int i = 0; i < rows; i++) {
		rs = (StateMod_StreamGage)__worksheet.getRowData(i);
		if (rs.getID().trim().equals(id.trim())) {
			selectTableIndex(i);
			return;
		}
	}
}

/**
Selects the desired index in the table, but also displays the appropriate data in the remainder of the window.
@param index the index to select in the list.
*/
public void selectTableIndex(int index) {
	int rowCount = __worksheet.getRowCount();
	if (rowCount == 0) {
		return;
	}
	if (index > (rowCount + 1)) {
		return;
	}
	if (index < 0) {
		return;
	}
	__worksheet.scrollToRow(index);
	__worksheet.selectRow(index);
	processTableSelection(index);
}

/**
Sets up the GUI.
@param index the index of the object to preselect on the gui.
*/
private void setupGUI(int index) {
	String routine = "StateMod_StreamGage_JFrame.setupGUI";

	addWindowListener(this);

	JPanel p1 = new JPanel();	// entire top half

	__searchID = new JTextField(10);
	__searchName = new JTextField(10);
	__findNext = new JButton(__BUTTON_FIND_NEXT);
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton("ID", true);
	__searchIDJRadioButton.addActionListener(this);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	__searchNameJRadioButton = new JRadioButton("Name", false);
	__searchNameJRadioButton.addActionListener(this);
	__searchCriteriaGroup.add(__searchNameJRadioButton);

	__idJTextField = new JTextField(12);
	__idJTextField.setEditable(false);
	__nameJTextField = new JTextField(24);
	__nameJTextField.setEditable(false);
	__cgotoJTextField = new JTextField(12);
	__cgotoJTextField.setEditable(false);
	__crunidyComboBox = new SimpleJComboBox();

	__helpJButton = new JButton(__BUTTON_HELP);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__applyJButton = new JButton(__BUTTON_APPLY);

	GridBagLayout gb = new GridBagLayout();
	p1.setLayout(gb);

	int y = 0;
	
	PropList p = new PropList("StateMod_StreamGage_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	try {
		StateMod_StreamGage_TableModel tmr = new
			StateMod_StreamGage_TableModel(__streamGageStationsVector);
		StateMod_StreamGage_CellRenderer crr = new StateMod_StreamGage_CellRenderer(tmr);
	
		__worksheet = new JWorksheet(crr, tmr, p);

		widths = crr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		__worksheet = new JWorksheet(0, 0, p);
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);	
	JPanel param_JPanel = new JPanel();
	param_JPanel.setLayout(gb);
	
	JGUIUtil.addComponent(param_JPanel, new JLabel("Station ID:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __idJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(param_JPanel, new JLabel("Station name:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __nameJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(param_JPanel, new JLabel("River node ID:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __cgotoJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(param_JPanel,new JLabel("Daily data ID:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __crunidyComboBox,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel tsPanel = new JPanel();
	tsPanel.setLayout(gb);

	tsPanel.setBorder(BorderFactory.createTitledBorder("Time series"));

	__ts_streamflow_hist_monthly_JCheckBox = new JCheckBox("Streamflow (Historical Monthly)");
	__ts_streamflow_hist_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY).hasData()) {
		__ts_streamflow_hist_monthly_JCheckBox.setEnabled(false);
	}
	__ts_streamflow_hist_daily_JCheckBox = new JCheckBox("Streamflow (Historical Daily)");		
	__ts_streamflow_hist_daily_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_DAILY).hasData()) {
		__ts_streamflow_hist_daily_JCheckBox.setEnabled(false);
	}

	// TODO SAM 2006-08-31
	// Need to enable - for now always disabled...
	// This checkbox needs to be enabled when the daily identifier
	// indicates using another time series to estimate the daily time series.
	__ts_streamflow_est_hist_daily_JCheckBox = new JCheckBox("Streamflow (Estimated Historical Daily)");
	__ts_streamflow_est_hist_daily_JCheckBox.addItemListener(this);
	__ts_streamflow_est_hist_daily_JCheckBox.setEnabled ( false );

	__ts_streamflow_base_monthly_JCheckBox = new JCheckBox("Streamflow (Baseflow Monthly)");
	__ts_streamflow_base_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY).hasData()) {
		__ts_streamflow_base_monthly_JCheckBox.setEnabled(false);
	}
	__ts_streamflow_base_daily_JCheckBox = new JCheckBox("Streamflow (Baseflow Daily)");
	__ts_streamflow_base_daily_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(	
		StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_DAILY).hasData()) {
		__ts_streamflow_base_daily_JCheckBox.setEnabled(false);
	}
	// TODO SAM 2006-08-31
	// Need to enable - for now always disabled...
	// This checkbox needs to be enabled when the daily identifier
	// indicates using another time series to estimate the daily
	// time series.  Need to support all the StateMod options for computing the time series.
	__ts_streamflow_est_base_daily_JCheckBox = new JCheckBox("Streamflow (Estimated Baseflow Daily)");
	__ts_streamflow_est_base_daily_JCheckBox.addItemListener(this);
	__ts_streamflow_est_base_daily_JCheckBox.setEnabled ( false );

	y = 0;
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_hist_monthly_JCheckBox,
		0, y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_hist_daily_JCheckBox,
		0, ++y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_est_hist_daily_JCheckBox,
		0, ++y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_base_monthly_JCheckBox,
		0, ++y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_base_daily_JCheckBox,
		0, ++y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_est_base_daily_JCheckBox,
		0, ++y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);

	// Buttons for the time series...
	JPanel tsb_JPanel = new JPanel ();
	tsb_JPanel.setLayout ( new FlowLayout() );
	__graph_JButton = new SimpleJButton ( "Graph", this );
	tsb_JPanel.add ( __graph_JButton );
	__table_JButton = new SimpleJButton ( "Table", this );
	tsb_JPanel.add ( __table_JButton );
	__summary_JButton = new SimpleJButton ( "Summary", this );
	tsb_JPanel.add ( __summary_JButton );

	JGUIUtil.addComponent(tsPanel, tsb_JPanel,
		0, ++y, 1, 1, 1, 1,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	// Based on whether there are any components with data for the given checkboxes
	
	//
	// add search areas
	//	

	y = 0;
	
	JPanel searchPanel = new JPanel();
	searchPanel.setLayout(gb);
	searchPanel.setBorder(BorderFactory.createTitledBorder("Search above list for:"));
	JGUIUtil.addComponent(searchPanel, __searchIDJRadioButton,
		0, ++y, 1, 1, 0, 0,
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(searchPanel, __searchID,
		1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
	__searchID.addActionListener(this);
	JGUIUtil.addComponent(searchPanel, __searchNameJRadioButton,
		0, ++y, 1, 1, 0, 0,
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchName.setEditable(false);
	JGUIUtil.addComponent(searchPanel, __searchName,
		1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
	__searchName.addActionListener(this);
	JGUIUtil.addComponent(searchPanel, __findNext,
		0, ++y, 4, 1, 0, 0,
		10, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNext.addActionListener(this);

	JGUIUtil.addComponent(p1, new JScrollPane(__worksheet),
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	JPanel right = new JPanel();
	right.setLayout(gb);

	JGUIUtil.addComponent(p1, right, 
		1, 0, 1, 2, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	JGUIUtil.addComponent(right, param_JPanel,
		0, 0, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(right, tsPanel,
		0, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(p1, searchPanel,
		0, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	//
	// add close and help buttons
	//
	JPanel pfinal = new JPanel();
	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	pfinal.setLayout(fl);
	__helpJButton.setEnabled(false);
	if (__editable) {
		pfinal.add(__applyJButton);
		pfinal.add(__cancelJButton);
	}
	pfinal.add(__closeJButton);
//	pfinal.add(__helpJButton);
	__helpJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	__applyJButton.addActionListener(this);

	getContentPane().add("Center", p1);
	getContentPane().add("South", pfinal);
	
	// this must be done before the first selectTableIndex() call
	populateCrunidyComboBox();
	
	initializeDisables();
	
	selectTableIndex(index);

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen ( StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE, this );
	}

	pack();
	setSize(875, 500);
	JGUIUtil.center(this);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
	__graph_JButton.setEnabled(false);
	__table_JButton.setEnabled(false);
	__summary_JButton.setEnabled(false);

	__worksheet.addSortListener(this);
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
public void windowClosing(WindowEvent e)
{	saveCurrentRecord();
	int size = __streamGageStationsVector.size();
	StateMod_StreamGage s = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		s = (StateMod_StreamGage)__streamGageStationsVector.get(i);
		if (!changed && s.changed()) {
			changed = true;
		}
		s.acceptChanges();
	}				
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_STREAMGAGE_STATIONS,true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE );
	}
	else {
		JGUIUtil.close ( this );
	}
}

/**
Responds to Window deactivated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to Window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to Window iconified events; saves the current record.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {
	saveCurrentRecord();
}

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

private void setOriginalCrunidy(StateMod_StreamGage r, String crunidy) {
	((StateMod_StreamGage)r._original)._crunidy = crunidy;
}

/**
Called just before the worksheet is sorted.  Stores the index of the record that is selected.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortAboutToChange(JWorksheet worksheet, int sort) {
	__sortSelectedRow = __worksheet.getOriginalRowNumber(__worksheet.getSelectedRow());
}

/**
Called when the worksheet is sorted.  Reselects the record that was selected prior to the sort.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortChanged(JWorksheet worksheet, int sort) {
	__worksheet.deselectAll();
	__worksheet.selectRow(__worksheet.getSortedRowNumber(__sortSelectedRow));
}

}