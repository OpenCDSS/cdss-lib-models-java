//------------------------------------------------------------------------------
// StateMod_StreamEstimate_JFrame - JFrame to edit the stream estimate stations
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Jan 1998	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 17 Feb 1998	CEN, RTi		Move help/close to bottom.
// 31 Aug 1998	CEN, RTi		added check for areTSRead.
// 22 Sep 1998	CEN, RTi		Changed list to multilist.
// 08 Mar 2000	CEN, RTi		Added radio buttons to search.
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 04 May 2001	SAM, RTi		Change "Baseflow Results" to
//					"Baseflow Time Series" and change the
//					MonthTS title to be more friendly.
// 2002-09-12	SAM, RTi		Move baseflow time series to the .ris
//					(River Stations)window.  Change the
//					title to say Baseflow Coefficients.
//------------------------------------------------------------------------------
// 2003-08-20	J. Thomas Sapienza, RTi	Initial swing version adapted from 
//					the old baseflow coefficients window.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-08-27	JTS, RTi		Added selectID() to select an ID 
//					on the worksheet from outside the GUI.
// 2003-09-02	JTS, RTi		* Added a border around the search area
//					  components.
//					* TS Graph/Summary/Table selection is
//					  now done through check boxes.
// 2003-09-04	JTS, RTi		Added cancel and apply buttons.
// 2003-09-04	SAM, RTi		Enable graph features.
// 2003-09-05	JTS, RTi		Class is now an item listener in 
//					order to enable/disable graph buttons
//					based on selected checkboxes.
// 2003-09-06	SAM, RTi		Fix problem with graph sizing.
// 2003-09-08	JTS, RTi		* Added checkTimeSeriesButtonsStates()
//					  to enable/disable the time series
//					  display buttons according to how
//					  the time series checkboxes are 
//					  selected.
//					* Adjusted layout.
// 2003-09-11	SAM, RTi		* Rename the class from
//					  StateMod_BaseFlowStation_JFrame to
//					  StateMod_StreamEstimate_JFrame and
//					  make changes accordingly.
//					* Remove findBaseflowCoefficients()
//					  since StateMod_Util.indexOf() will
//					  work.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2004-01-22	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
// 					* windowDeactivated() no longer saves
//					  data as it was causing problems with
//					  the cancel code.
// 2006-01-19	JTS, RTi		* Now implements JWorksheet_SortListener
//					* Reselects the record that was selected
//					  when the worksheet is sorted.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------

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
import javax.swing.JTextField;

import RTi.GRTS.TSProduct;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_SortListener;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a GUI for displaying stream estimate station data and the
associated coefficient data.
*/
public class StateMod_StreamEstimate_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener, JWorksheet_SortListener {

/**
Whether the data on the GUI is editable or not.
*/
private boolean __editable = true;

/**
Button group for the search radio buttons.
*/
private ButtonGroup __searchCriteriaGroup;

/**
The data set component containing base flow coefficient data.
*/
private DataSetComponent __coefficientsComp;

/**
The data set component containing base flow station data.
*/
private DataSetComponent __stationsComp;

/**
The index in __disables[] of textfields that should NEVER be made
editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
Indices of the currently- and last-selected station.
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
GUI buttons.
*/
private JButton
	__applyJButton,
	__cancelJButton,
	__findNextButton,
	__helpJButton,
	__closeJButton,
	__graph_JButton,
	__table_JButton,
	__summary_JButton;

private JCheckBox
	__ts_streamflow_base_monthly_JCheckBox,
	__ts_streamflow_base_daily_JCheckBox;

/**
Array of JComponents that should be disabled when nothing is selected from the list.
*/
private JComponent[] __disables;

/**
Search radio buttons.
*/
private JRadioButton	
	__searchIDJRadioButton,
	__searchNameJRadioButton;

/**
GUI text fields.
*/
private JTextField 	
	__nameJTextField,
	__idJTextField,
	__prorationFactorJTextField,
	__searchIDJTextField,
	__searchNameJTextField;

/**
Worksheets for displaying data.  __worksheetR is on the right and displays
stream estimate coefficients.  __worksheetL is on the left and displays station 
ids and names.
*/
private JWorksheet 
	__worksheetL,
	__worksheetR;

/**
The data set containing the data to display.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
GUI strings.
*/
private final String
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_FIND_NEXT = "Find Next",
	__BUTTON_HELP = "Help";

/**
The table model for displaying data in the right worksheet.
*/
private StateMod_StreamEstimate_Coefficients_TableModel __tableModelR;

/**
The stream estimate stations data.
*/
private Vector __stationsVector;

/**
The coefficients data.
*/
private Vector __coefficientsVector;

/**
Constructor.
@param dataset the dataset containing the stream estimate stations data.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param editable whether the data on the gui is editable or not
*/
public StateMod_StreamEstimate_JFrame (	StateMod_DataSet dataset,
					StateMod_DataSet_WindowManager
					dataset_wm, boolean editable )
{	
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	StateMod_GUIUtil.setTitle(this, dataset, "Stream Estimate Stations", null);

	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__stationsComp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS);
	__stationsVector = (Vector)__stationsComp.getData();
	int size = __stationsVector.size();
	StateMod_StreamEstimate s = null;
	for (int i = 0; i < size; i++) {
		s = (StateMod_StreamEstimate)__stationsVector.elementAt(i);
		s.createBackup();
	}

	__coefficientsComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS);
	__coefficientsVector = (Vector)__coefficientsComp.getData();	
	size = __coefficientsVector.size();
	StateMod_StreamEstimate_Coefficients c = null;
	for (int i = 0; i < size; i++) {
		c = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(i);
		c.createBackup();
	}
		
	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the dataset containing the baseflow data.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param station the station to preselect on the GUI.
@param editable whether the data on the gui is editable or not
*/
public StateMod_StreamEstimate_JFrame (	StateMod_DataSet dataset,
					StateMod_DataSet_WindowManager
					dataset_wm,
					StateMod_StreamEstimate station,
					boolean editable )
{	
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	StateMod_GUIUtil.setTitle(this, dataset, "Stream Estimate Stations", null);

	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__stationsComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS);
	__stationsVector = (Vector)__stationsComp.getData();
	int size = __stationsVector.size();
	StateMod_StreamEstimate s = null;
	for (int i = 0; i < size; i++) {
		s = (StateMod_StreamEstimate)__stationsVector.elementAt(i);
		s.createBackup();
	}

	__coefficientsComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS);
	__coefficientsVector = (Vector)__coefficientsComp.getData();
	size = __coefficientsVector.size();
	StateMod_StreamEstimate_Coefficients c = null;
	for (int i = 0; i < size; i++) {
		c = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(i);
		c.createBackup();
	}

	String id = station.getID();
	int index = StateMod_Util.indexOf(__stationsVector, id);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();
	Object o = e.getSource();

	if (o == __searchIDJRadioButton) {
		__searchIDJTextField.setEditable(true);
		__searchNameJTextField.setEditable(false);
	}
	else if (o == __searchNameJRadioButton) {
		__searchIDJTextField.setEditable(false);
		__searchNameJTextField.setEditable(true);
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
		int size = __stationsVector.size();
		StateMod_StreamEstimate s = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			s = (StateMod_StreamEstimate)__stationsVector.elementAt(i);
			if (!changed && s.changed()) {
				changed = true;
			}
			s.createBackup();
		}	
		if (changed) {
			__dataset.setDirty(	StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS,true);
		}							
		size = __coefficientsVector.size();
		StateMod_StreamEstimate_Coefficients c = null;
		changed = false;
		for (int i = 0; i < size; i++) {
			c = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(i);
			if (!changed && c.changed()) {
				changed = true;
			}
			c.createBackup();
		}	
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
		}	
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		int size = __stationsVector.size();
		StateMod_StreamEstimate s = null;
		for (int i = 0; i < size; i++) {
			s = (StateMod_StreamEstimate)__stationsVector.elementAt(i);
			s.restoreOriginal();
		}		
		size = __coefficientsVector.size();
		StateMod_StreamEstimate_Coefficients c = null;
		for (int i = 0; i < size; i++) {
			c = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(i);
			c.restoreOriginal();
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_STREAMESTIMATE );
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		saveCurrentRecord();
		int size = __stationsVector.size();
		StateMod_StreamEstimate s = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			s = (StateMod_StreamEstimate)__stationsVector.elementAt(i);
			if (!changed && s.changed()) {
				changed = true;
			}
			s.acceptChanges();
		}		
		if (changed) {
			__dataset.setDirty(	StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS,true);
		}						
		size = __coefficientsVector.size();
		StateMod_StreamEstimate_Coefficients c = null;
		changed = false;
		for (int i = 0; i < size; i++) {
			c = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(i);
			if (!changed && c.changed()) {
				changed = true;
			}
			c.acceptChanges();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_STREAMESTIMATE );
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_HELP)) {
		// TODO Enable HELP (JTS - 2003-08-20)
	}	
	else if (action.equals(__BUTTON_FIND_NEXT)) {
		searchLWorksheet(__worksheetL.getSelectedRow() + 1);
	}
	else if ((o == __searchIDJTextField) || (o == __searchNameJTextField)) {
		searchLWorksheet(0);
	}	
	else if ( (o == __graph_JButton) || (o == __table_JButton) ||
		(o == __summary_JButton) ) {
		displayTSViewJFrame(o);
	}	
}

/**
Checks the text fields for validity before they are saved back into the
data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	Vector errors = new Vector();
	int errorCount = 0;

	// for each field, check if it contains valid input.  If not,
	// create a string of the format "fieldname -- reason why it
	// is not correct" and add it to the errors vector.  also
	// increment error count
	
	if (errorCount == 0) {
		return true;
	}

	String plural = " was ";
	if (errorCount > 1) {
		plural = "s were ";
	}
	String label = "The following error" + plural + "encountered trying to save the record:\n";
	for (int i = 0; i < errorCount; i++) {
		label += errors.elementAt(i) + "\n";
	}
	new ResponseJDialog(this, "Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Enables or disables the time series display buttons according to how the 
time series JCheckBoxes are selected.
*/
private void checkTimeSeriesButtonsStates() {
	boolean enabled = false;

	if (
		(__ts_streamflow_base_monthly_JCheckBox.isSelected()
			&& __ts_streamflow_base_monthly_JCheckBox.isEnabled())
		|| (__ts_streamflow_base_daily_JCheckBox.isSelected()
			&& __ts_streamflow_base_daily_JCheckBox.isEnabled())
	) {
		enabled = true;
	}
	
	__graph_JButton.setEnabled(enabled);
	__table_JButton.setEnabled(enabled);
	__summary_JButton.setEnabled(enabled);
}

/**
Display the time series.
@param action Event action that initiated the display.
*/
private void displayTSViewJFrame(Object o)
{	String routine = "displayTSViewJFrame";

	// Initialize the display...

	PropList display_props = new PropList("StreamEstimate");
	if ( o == __graph_JButton ) {
		display_props.set("InitialView", "Graph");
	}
	else if ( o == __table_JButton ) {
		display_props.set("InitialView", "Table");
	}
	else if ( o == __summary_JButton ) {
		display_props.set("InitialView", "Summary");
	}

	StateMod_StreamEstimate sta = (StateMod_StreamEstimate)__stationsVector.elementAt(__currentStationIndex);

	// display_props.set("HelpKey", "TSTool.ExportMenu");
	display_props.set("TSViewTitleString", StateMod_Util.createDataLabel(sta,true) + " Time Series");
	display_props.set("DisplayFont", "Courier");
	display_props.set("DisplaySize", "11");
	display_props.set("PrintFont", "Courier");
	display_props.set("PrintSize", "7");
	display_props.set("PageLength", "100");

	PropList props = new PropList("StreamEstimate");
	props.set("Product.TotalWidth", "600");
	props.set("Product.TotalHeight", "400");

	Vector tslist = new Vector();

	int sub = 0;
	int its = 0;
	TS ts = null;

	if ( (__ts_streamflow_base_monthly_JCheckBox.isSelected() && (sta.getBaseflowMonthTS() != null) ) ) {
		// Do the monthly graph...
		++sub;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub +
		".SubTitleString=Monthly Data for Stream Estimate Station "
			+ sta.getID() + " (" + sta.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = sta.getBaseflowMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) +	".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	if ( (__ts_streamflow_base_daily_JCheckBox.isSelected() && (sta.getBaseflowDayTS() != null) ) ) {
		// Do the daily graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub +
		".SubTitleString=Daily Data for Stream Estimate Station " + sta.getID() + " (" + sta.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = sta.getBaseflowDayTS();
		if ( ts != null ) {
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
	__searchCriteriaGroup = null;
	__coefficientsComp = null;
	__stationsComp = null;
	__textUneditables = null;
	__findNextButton = null;
	__helpJButton = null;
	__closeJButton = null;
	__disables = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__nameJTextField = null;
	__idJTextField = null;
	__prorationFactorJTextField = null;
	__searchIDJTextField = null;
	__ts_streamflow_base_monthly_JCheckBox = null;
	__ts_streamflow_base_daily_JCheckBox = null;
	__searchNameJTextField = null;
	__worksheetR = null;
	__worksheetL = null;
	__dataset = null;
	__tableModelR = null;
	__stationsVector = null;
	__coefficientsVector = null;
	super.finalize();
}

/**
Finds coefficients in the __coefficientsVector that has the specified id.
@param id the id to match.
@return the matching coefficients object, or null if no matches could be found.
*/
private StateMod_StreamEstimate_Coefficients findCoefficients(String id) {
	StateMod_StreamEstimate_Coefficients coef = null;
	int pos = StateMod_Util.indexOf ( __coefficientsVector, id );
	if ( pos >= 0 ) {;
		coef = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(pos);
	}
	return coef;
}

/**
Responds to item state changed events.
@param e the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent e) {
	checkTimeSeriesButtonsStates();
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to 
selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[8];
	int i = 0;
	__disables[i++] = __nameJTextField;
	__disables[i++] = __idJTextField;
	__disables[i++] = __prorationFactorJTextField;
	__disables[i++] = __graph_JButton;
	__disables[i++] = __table_JButton;
	__disables[i++] = __summary_JButton;
	__disables[i++] = __ts_streamflow_base_monthly_JCheckBox;
	__disables[i++] = __ts_streamflow_base_daily_JCheckBox;

	__textUneditables = new int[2];
	__textUneditables[0] = 0;
	__textUneditables[0] = 1;
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; calls 'processLTableSelection' with the 
newly-selected index in the table.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	processLTableSelection(__worksheetL.getSelectedRow());
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
Responds to mouse released events; calls 'processLTableSelection' with the 
newly-selected index in the table.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	processLTableSelection(__worksheetL.getSelectedRow());
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectLTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it 
on the form.
@param index the index of the reservoir to display on the form.
*/
private void processLTableSelection(int index) {
	__lastStationIndex = __currentStationIndex;
	__currentStationIndex = __worksheetL.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheetL.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__disables, true);
		__tableModelR.setStreamEstimateCoefficients(null);
		__worksheetR.refresh();
		return;
	}

	StateMod_StreamEstimate sta = (StateMod_StreamEstimate)
		__stationsVector.elementAt(__currentStationIndex);

	JGUIUtil.enableComponents(__disables, __textUneditables, __editable);
	checkTimeSeriesButtonsStates();
		
	__idJTextField.setText(sta.getID());
	__nameJTextField.setText(sta.getName());

	StateMod_StreamEstimate_Coefficients coef=findCoefficients(sta.getID());

	if (coef != null) {	
		StateMod_GUIUtil.checkAndSet(coef.getProratnf(), __prorationFactorJTextField);
		__tableModelR.setStreamEstimateCoefficients(coef);
		__worksheetR.refresh();
	}
	else {
		__prorationFactorJTextField.setText("");
		__tableModelR.setStreamEstimateCoefficients(null);
		__worksheetR.refresh();
	}

	// For checkboxes, do not change the state of the checkbox, only
	// whether enabled - that way if the user has picked a combination of
	// parameters it is easy for them to keep the same settings when
	// switching between stations.  Make sure to do the following after
	// the generic enable/disable code is called above!

	if ( sta.getBaseflowMonthTS() != null ) {
		__ts_streamflow_base_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_streamflow_base_monthly_JCheckBox.setEnabled(false);
	}

	if ( sta.getBaseflowDayTS() != null ) {
		__ts_streamflow_base_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_streamflow_base_daily_JCheckBox.setEnabled(false);
	}
}

/**
Saves the prior record selected in the table; called when moving to a new 
record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastStationIndex);
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentStationIndex);
}

/**
Saves the information associated with the currently-selected reservoir.
The user doesn't need to hit the return key for the gui to recognize changes.
The info is saved each time the user selects a differents tation or pressed
the close button.
*/
private void saveInformation(int record) {
	if (!__editable || record == -1) {
		return;
	}

	if (!checkInput()) {
		return;
	}

	StateMod_StreamEstimate sta = (StateMod_StreamEstimate)__stationsVector.elementAt(record);

	sta.setName(__nameJTextField.getText());	
	sta.setID(__idJTextField.getText());	

	StateMod_StreamEstimate_Coefficients coef=findCoefficients(sta.getID());

	if (coef != null) {
		coef.setProratnf(__prorationFactorJTextField.getText());	
	}	
}

/**
Searches through the worksheet for a value, starting at the first row.
If the value is found, the row is selected.
*/
public void searchLWorksheet() {
	searchLWorksheet(0);
}

/**
Selects the desired ID in the left table and displays the appropriate data
in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheetL.getRowCount();
	StateMod_StreamEstimate sta = null;
	for (int i = 0; i < rows; i++) {
		sta = (StateMod_StreamEstimate)__worksheetL.getRowData(i);
		if (sta.getID().trim().equals(id.trim())) {
			selectLTableIndex(i);
			return;
		}
	}
}

/**
Searches through the worksheet for a value, starting at the given row.
If the value is found, the row is selected.
@param row the row to start searching from.
*/
public void searchLWorksheet(int row) {
	String searchFor = null;
	int col = -1;
	if (__searchIDJRadioButton.isSelected()) {
		searchFor = __searchIDJTextField.getText().trim();
		col = 0;
	}
	else {
		searchFor = __searchNameJTextField.getText().trim();
		col = 1;
	}
	int index = __worksheetL.find(searchFor, col, row, 
		JWorksheet.FIND_EQUAL_TO | JWorksheet.FIND_CONTAINS |
		JWorksheet.FIND_CASE_INSENSITIVE | JWorksheet.FIND_WRAPAROUND);
	if (index != -1) {
		selectLTableIndex(index);
	}
}

/**
Selects the desired index in the table, but also displays the appropriate data
in the remainder of the window.
@param index the index to select in the list.
*/
public void selectLTableIndex(int index) {
	int rowCount = __worksheetL.getRowCount();
	if (rowCount == 0) {
		return;
	}
	if (index > (rowCount + 1)) {
		return;
	}
	if (index < 0) {
		return;
	}
	__worksheetL.scrollToRow(index);
	__worksheetL.selectRow(index);

	processLTableSelection(index);
}

/**
Sets up the GUI.
@param index the index of the stream estimate station to preselect from the
list.
*/
private void setupGUI(int index) {
	String routine = "StateMod_StreamEstimate_JFrame.setupGUI";

	addWindowListener(this);

	// AWT portion
	JPanel p1 = new JPanel();	// selection list and grid
	JPanel p2 = new JPanel();	// search widgets
	JPanel pmain = new JPanel();	// everything but close and help buttons

	__nameJTextField = new JTextField(24);
	__nameJTextField.setEditable(false);
	__idJTextField = new JTextField(12);
	__idJTextField.setEditable(false);
	__prorationFactorJTextField = new JTextField(12);

	__searchIDJTextField = new JTextField(12);
	__searchIDJTextField.addActionListener(this);
	__searchNameJTextField = new JTextField(12);
	__searchNameJTextField.addActionListener(this);
	__searchNameJTextField.setEditable(false);
	__findNextButton = new JButton(__BUTTON_FIND_NEXT);
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton("ID", true);
	__searchIDJRadioButton.addActionListener(this);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	__searchNameJRadioButton = new JRadioButton("Name", false);
	__searchNameJRadioButton.addActionListener(this);
	__searchCriteriaGroup.add(__searchNameJRadioButton);

	__applyJButton = new JButton(__BUTTON_APPLY);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__helpJButton = new JButton(__BUTTON_HELP);
	__closeJButton = new JButton(__BUTTON_CLOSE);

	GridBagLayout gb = new GridBagLayout();
	p1.setLayout(gb);
	p2.setLayout(gb);
	pmain.setLayout(gb);

	int y;

	PropList p = new PropList( "StateMod_StreamEstimate_JFrame.JWorksheet");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widthsL = null;
	JScrollWorksheet jswL = null;
	try {
		StateMod_StreamEstimate_TableModel tmr = new StateMod_StreamEstimate_TableModel(__stationsVector);
		StateMod_StreamEstimate_CellRenderer crr = new StateMod_StreamEstimate_CellRenderer(tmr);
	
		jswL = new JScrollWorksheet(crr, tmr, p);
		__worksheetL = jswL.getJWorksheet();

		widthsL = crr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jswL = new JScrollWorksheet(0, 0, p);
		__worksheetL = jswL.getJWorksheet();
	}
	__worksheetL.setPreferredScrollableViewportSize(null);
	__worksheetL.setHourglassJFrame(this);
	__worksheetL.addMouseListener(this);	
	__worksheetL.addKeyListener(this);		

	JGUIUtil.addComponent(pmain, jswL,
		0, 0, 4, 30, 1, 1,
		10, 10, 1, 10,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		p1, new JLabel("ID:"),
		0, 0, 1, 1, 1, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __idJTextField,
		1, 0, 1, 1, 1, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		p1, new JLabel("Name:"),
		0, 1, 1, 1, 1, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __nameJTextField,
		1, 1, 1, 1, 1, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		p1, new JLabel("Proration Factor:"),
		0, 2, 1, 1, 1, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __prorationFactorJTextField,
		1, 2, 1, 1, 1, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(
		pmain, p1,
		5, 0, 4, 4, 0, 0,
		10, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	int[] widthsR = null;
	JScrollWorksheet jswR = null;
	try {
		Vector v = new Vector();
		/*
		for (int i = 0; i < StateMod_RiverBaseFlow.MAX_BASEFLOWS;
			i++) {
			v.add("");
		}
		*/
		__tableModelR = new
			StateMod_StreamEstimate_Coefficients_TableModel(v);
		StateMod_StreamEstimate_Coefficients_CellRenderer crr = new
			StateMod_StreamEstimate_Coefficients_CellRenderer( __tableModelR);
	
		jswR = new JScrollWorksheet(crr, __tableModelR, p);
		__worksheetR = jswR.getJWorksheet();

		widthsR = crr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jswR = new JScrollWorksheet(0, 0, p);
		__worksheetR = jswR.getJWorksheet();
	}
	__worksheetR.setPreferredScrollableViewportSize(null);
	__worksheetR.setHourglassJFrame(this);
	__worksheetR.addMouseListener(this);	
	__worksheetR.addKeyListener(this);		
		
	JPanel worksheetRPanel = new JPanel();
	worksheetRPanel.setLayout(gb);
	
	JGUIUtil.addComponent(
		worksheetRPanel, jswR,
		0, 0, 1, 1, .5, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.SOUTHEAST);

	worksheetRPanel.setBorder(BorderFactory.createTitledBorder("Stream Estimate Coefficients"));
	
	JGUIUtil.addComponent(
		pmain, worksheetRPanel,
		5, 5, 18, 24, .5, 1,
		10, 10, 10, 10,
		GridBagConstraints.BOTH, GridBagConstraints.SOUTHWEST);

	JPanel tsPanel = new JPanel();
	tsPanel.setLayout(gb);

	__ts_streamflow_base_monthly_JCheckBox = new JCheckBox( "Streamflow (Baseflow Monthly)");
	__ts_streamflow_base_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY).hasData()) {
		__ts_streamflow_base_monthly_JCheckBox.setEnabled(false);
	}
	__ts_streamflow_base_daily_JCheckBox = new JCheckBox( "Streamflow (Baseflow Daily)");
	__ts_streamflow_base_daily_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(	
		StateMod_DataSet.COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY).hasData()) {
		__ts_streamflow_base_daily_JCheckBox.setEnabled(false);
	}
	
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_base_monthly_JCheckBox,
		0, 0, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
	JGUIUtil.addComponent(
		tsPanel, __ts_streamflow_base_daily_JCheckBox,
		0, 1, 1, 1, 1, 1, 
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
		0, 4, 1, 1, 1, 1,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	tsPanel.setBorder(BorderFactory.createTitledBorder("Time Series"));

	JGUIUtil.addComponent(
		pmain, tsPanel,
		5, 30, 1, 1, 0, 0,
		10, 10, 10, 10,
		GridBagConstraints.BOTH, GridBagConstraints.SOUTHEAST);		

	//
	// close and help buttons
	//
	JPanel pfinal = new JPanel();
	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	pfinal.setLayout(fl);
	if (__editable) {
		pfinal.add(__applyJButton);
		pfinal.add(__cancelJButton);
	}
	pfinal.add(__closeJButton);
//	pfinal.add(__helpJButton);
	__applyJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	__helpJButton.addActionListener(this);
	__helpJButton.setEnabled(false);
	__closeJButton.addActionListener(this);


	//
	// add search areas
	//
	y=0;	
	p2.setBorder(BorderFactory.createTitledBorder("Search above list for:     "));
	y++;
	JGUIUtil.addComponent(
		p2, __searchIDJRadioButton,
		0, y, 1, 1, 0, 0,
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		p2, __searchIDJTextField,
		1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, 
		GridBagConstraints.EAST);
	__searchIDJTextField.addActionListener(this);
	y++;
	JGUIUtil.addComponent(
		p2, __searchNameJRadioButton,
		0, y, 1, 1, 0, 0,
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		p2, __searchNameJTextField,
		1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, 
		GridBagConstraints.EAST);
	__searchNameJTextField.addActionListener(this);
	y++;
	JGUIUtil.addComponent(
		p2, __findNextButton,
		0, y, 4, 1, 0, 0,
		10, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__findNextButton.addActionListener(this);
	JGUIUtil.addComponent(
		pmain, p2,
		0, GridBagConstraints.RELATIVE, 4, 1, 0, 0,
		5, 10, 20, 10,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);

	getContentPane().add("Center", pmain);
	getContentPane().add("South", pfinal);

	initializeDisables();

	selectLTableIndex(index);
	
	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_STREAMESTIMATE, this );
	}
	pack();
	setSize(770, 500);
	JGUIUtil.center(this);
	setVisible(true);

	if (widthsR != null) {
		__worksheetR.setColumnWidths(widthsR);
	}
	if (widthsL != null) {
		__worksheetL.setColumnWidths(widthsL);
	}
	__graph_JButton.setEnabled(false);
	__table_JButton.setEnabled(false);
	__summary_JButton.setEnabled(false);	

	__worksheetL.addSortListener(this);
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
Responds to Window closing events; closes the window and marks it closed
in StateMod_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	saveCurrentRecord();
	int size = __stationsVector.size();
	StateMod_StreamEstimate s = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		s = (StateMod_StreamEstimate)__stationsVector.elementAt(i);
		if (!changed && s.changed()) {
			changed = true;
		}
		s.acceptChanges();
	}		
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS,
			true);
	}					
	size = __coefficientsVector.size();
	StateMod_StreamEstimate_Coefficients c = null;
	changed = false;
	for (int i = 0; i < size; i++) {
		c = (StateMod_StreamEstimate_Coefficients)__coefficientsVector.elementAt(i);
		if (!changed && c.changed()) {	
			changed = true;
		}
		c.acceptChanges();
	}		
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_STREAMESTIMATE );
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
Responds to Window iconified events; saves the current information.
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

/**
Called just before the worksheet is sorted.  Stores the index of the record
that is selected.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortAboutToChange(JWorksheet worksheet, int sort) {
	__sortSelectedRow = __worksheetL.getOriginalRowNumber( __worksheetL.getSelectedRow());
}

/**
Called when the worksheet is sorted.  Reselects the record that was selected
prior to the sort.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortChanged(JWorksheet worksheet, int sort) {
	__worksheetL.deselectAll();
	__worksheetL.selectRow(__worksheetL.getSortedRowNumber( __sortSelectedRow));
}

}
