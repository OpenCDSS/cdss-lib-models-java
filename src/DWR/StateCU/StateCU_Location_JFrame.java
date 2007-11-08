//------------------------------------------------------------------------------
// StateCU_Location_JFrame - dialog to display location info
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2003-07-14	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-22	JTS, RTi		Revised following SAM's review. 
// 2004-02-28	Steven A. Malers, RTi	Moved some methods from StateCU_Data to
//					StateCU_Util.
// 2005-01-17	JTS, RTi		Changed getOriginalRow() to 
//					getOriginalRowNumber().
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------

package DWR.StateCU;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import DWR.StateMod.StateMod_DiversionRight;
import RTi.GRTS.TSViewJFrame;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a GUI for displaying location data.
*/
public class StateCU_Location_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Button labels.
*/
private final String 
	__BUTTON_FIND_NEXT = "Find Next",
	__BUTTON_ID = "ID",
	__BUTTON_NAME = "Name",
	__BUTTON_HELP = "Help",
	__BUTTON_CLOSE = "Close",
	__BUTTON_APPLY = "Apply",
	__BUTTON_GRAPH = "Graph",
	__BUTTON_TABLE = "Table",
	__BUTTON_SUMMARY = "Summary",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_GRAPH_ALL = "Graph All Time Series";

/**
Boolean specifying whether the form is editable or not.
*/
private boolean __editable = false;

/**
Button group for selecting what to search the jworksheet for.
*/
private ButtonGroup __searchCriteriaGroup;

/**
Array of the elements of __textDisables that should never be editable.
*/
private int[] __textUneditables;

/**
Index of the currently-selected location.
*/
private int __currentLocationIndex;
/**
Index of the previously-selected location.
*/
private int __lastLocationIndex;

/**
GUI buttons.
*/
private JButton __findNextLocation;

/**
Checkboxes for selecting the kind of time series to graph.
*/
private JCheckBox
	__cropPatternCheckBox,
	__irrigationPracticeCheckBox,
	__diversionCheckBox,
	__precipitationCheckBox,
	__temperatureCheckBox,
	__frostDatesCheckBox;

/**
Array of JComponents that should be disabled if nothing is selected.
*/
private JComponent[] __disables;

/**
Radio buttons for selecting the field on which to search through the
JWorksheet.
*/
private JRadioButton
	__searchIDJRadioButton,
	__searchNameJRadioButton;

/**
Array of JTextComponents that should be disabled if nothing is selected.
*/
private JTextComponent[] __textDisables;

/**
GUI text fields.
*/
private JTextField 
	__searchID,
	__searchName,
	__locationIDJTextField,
	__nameJTextField,
	__latitudeJTextField,
	__elevationJTextField,
	__region1JTextField,
	__region2JTextField, 
	__awcJTextField;

/**
Worksheet for displaying location information.
*/
private JWorksheet __worksheet;

/**
Dataset containing the location data.
*/
private StateCU_DataSet __dataset;

/**
DataSetComponent containing location data.
*/
private DataSetComponent __locationComponent;

/**
Vector of locations data in the DataSetComponent.
*/
private Vector __locationsVector;

private JWorksheet __delayWorksheet;
private StateCU_Location_TableModel __delayModel;
private DataSetComponent __delaysComponent;
private Vector __delaysVector;

private JWorksheet __stationWorksheet;
private StateCU_Location_TableModel __stationModel;
private DataSetComponent __stationsComponent;
private Vector __stationsVector;

private JWorksheet __rightsWorksheet;
private StateCU_Location_TableModel __rightsModel;
private DataSetComponent __rightsComponent;

private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Constructor.
@param dataset dataset containing data to display
@param editable whether the display should be editable or not
*/
public StateCU_Location_JFrame(StateCU_DataSet dataset, boolean editable) {
	super(dataset.getBaseName() + " - StateCU GUI - Locations");
	__currentLocationIndex = -1;
	__editable = editable;

	__dataset = dataset;
	__locationComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CU_LOCATIONS);
	__locationsVector = (Vector)__locationComponent.getData();

	__delaysComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY);
	__delaysVector = (Vector)__delaysComponent.getData();

	__stationsComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CLIMATE_STATIONS);
	__stationsVector = (Vector)__stationsComponent.getData();

	__rightsComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_DIVERSION_RIGHTS);

	setupGUI(0);
}

/**
Constructor.
@param location StateCU_Location object to display
@param editable whether the display should be editable or not.
*/
public StateCU_Location_JFrame(StateCU_DataSet dataset, 
StateCU_Location location, boolean editable) {
	super(dataset.getBaseName() + " - StateCU GUI - Locations");
	__dataset = dataset;
	__currentLocationIndex = -1;	
	__locationsVector = new Vector();

	__locationComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CU_LOCATIONS);
	__locationsVector = (Vector)__locationComponent.getData();	

	String id = location.getID();
	int index = StateCU_Util.indexOf(__locationsVector, id);

	__delaysComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY);
	__delaysVector = (Vector)__delaysComponent.getData();

	__stationsComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CLIMATE_STATIONS);
	__stationsVector = (Vector)__stationsComponent.getData();

	__rightsComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_DIVERSION_RIGHTS);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) { 

	try {
	String action = e.getActionCommand();
	Object source = e.getSource();

	if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-06-10
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		saveCurrentRecord();
		dispose();
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		dispose();
	}
	else if (action.equals(__BUTTON_GRAPH) || action.equals(__BUTTON_TABLE)
		|| action.equals(__BUTTON_SUMMARY)
		|| action.equals(__BUTTON_GRAPH_ALL)) {
		displayTSViewJFrame(action);
	}
	else if (source == __findNextLocation) {
		searchWorksheet(__worksheet.getSelectedRow() + 1);
	}
	else if (source == __searchID || source == __searchName) {	
		searchWorksheet();
	}		
	else if (source == __searchNameJRadioButton) {
		__searchName.setEditable(true);
		__searchID.setEditable(false);
	}
	else if (source == __searchIDJRadioButton) {
		__searchName.setEditable(false);
		__searchID.setEditable(true);	
	}

	}
	catch (Exception ex) {
		ex.printStackTrace();
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
	String label = "The following error" + plural + "encountered "
		+ "trying to save the record:\n";
	for (int i = 0; i < errorCount; i++) {
		label += errors.elementAt(i) + "\n";
	}
	// TODO SAM 2007-03-01 Why not using Message/Logging?
	new ResponseJDialog(this, 
		"Errors encountered", label, ResponseJDialog.OK);
	return false;
}

private void displayTSViewJFrame(String action) {
	String routine = "displayTSViewJFrame";

	Vector tslist = new Vector();

	boolean graphAll = false;
	if (action.equals(__BUTTON_GRAPH_ALL)) {
		graphAll = true;
	}

	if (__precipitationCheckBox.isSelected() || graphAll) {
//		tslist.add(
	}
	if (__temperatureCheckBox.isSelected() || graphAll) {
//		tslist.add(
	}
	if (__frostDatesCheckBox.isSelected() || graphAll) {
//		tslist.add(
	}
	if (__cropPatternCheckBox.isSelected() || graphAll) {
//		tslist.add(
	}
	if (__irrigationPracticeCheckBox.isSelected() || graphAll) {
//		tslist.add(
	}
	if (__diversionCheckBox.isSelected() || graphAll) {
//		tslist.add(
	}

	PropList graphProps = new PropList("TSView");
	if (action.equals(__BUTTON_GRAPH) || graphAll) {
		graphProps.set("InitialView", "Graph");
	}
	else if (action.equals(__BUTTON_TABLE)) {
		graphProps.set("InitialView", "Table");
	}
	else if (action.equals(__BUTTON_SUMMARY)) {
		graphProps.set("InitialView", "Summary");
	}
	// graphProps.set("HelpKey", "TSTool.ExportMenu");
	graphProps.set("TotalWidth", "600");
	graphProps.set("TotalHeight", "400");
	graphProps.set("Title", "Demand");
	graphProps.set("DisplayFont", "Courier");
	graphProps.set("DisplaySize", "11");
	graphProps.set("PrintFont", "Courier");
	graphProps.set("PrintSize", "7");
	graphProps.set("PageLength", "100");

	try {
		new TSViewJFrame(tslist, graphProps);
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error displays TSViewJFrame");
		Message.printWarning(2, routine, e);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__searchCriteriaGroup = null;
	__textUneditables = null;
	__findNextLocation = null;
	__disables = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__textDisables = null;
	__searchID = null;
	__searchName = null;
	__locationIDJTextField = null;
	__nameJTextField = null;
	__latitudeJTextField = null;
	__elevationJTextField = null;
	__region1JTextField = null;
	__region2JTextField = null;
	__worksheet = null;
	__dataset = null;
	__locationComponent = null;
	__locationsVector = null;
	__cropPatternCheckBox = null;
	__irrigationPracticeCheckBox = null;
	__diversionCheckBox = null;
	__precipitationCheckBox = null;
	__temperatureCheckBox = null;
	__frostDatesCheckBox = null;
	super.finalize();
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; calls 'processTableSelection' with the 
newly-selected index in the table.
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
Responds to mouse released events; calls 'processTableSelection' with the 
newly-selected index in the table.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	processTableSelection(__worksheet.getSelectedRow());
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it 
on the form.
@param index the index of the reservoir to display on the form.
*/
private void processTableSelection(int index) {
	__lastLocationIndex = __currentLocationIndex;
	__currentLocationIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		nothingSelected();
		return;
	}

	somethingSelected();

	StateCU_Location location = (StateCU_Location)
		__locationsVector.elementAt(__currentLocationIndex);

	__locationIDJTextField.setText(location.getID());
	__nameJTextField.setText(location.getName());
	StateCU_Util.checkAndSet(location.getLatitude(), __latitudeJTextField);
	StateCU_Util.checkAndSet(location.getElevation(),__elevationJTextField);
	__region1JTextField.setText(location.getRegion1());
	__region2JTextField.setText(location.getRegion2());
	StateCU_Util.checkAndSet(location.getAwc(), __awcJTextField);

	int dindex = StateCU_Util.indexOf(__delaysVector, location.getID());
	if (dindex == -1 || __delaysVector == null) {
		__delayModel.setDelay(location, null);
	}
	else {
		__delayModel.setDelay(location, (StateCU_DelayTableAssignment)
			__delaysVector.elementAt(dindex));
	}

	__stationModel.setStations(location, __stationsVector);

	Vector v = (Vector)__rightsComponent.getData();
	String did = null;
	String sdid = null;
	String id = location.getID();
	int j = 0;
	Vector rights = new Vector();
	StateMod_DiversionRight right = null;
	for (int i = 0; i < v.size(); i++) {
		right = (StateMod_DiversionRight)v.elementAt(i);
		did = right.getID();
		j = did.indexOf(".");
		if (j == -1) {
			if (id.equals(did)) {
				rights.add(right);
			}
		}
		else {
			sdid = did.substring(0, j);
			if (id.equals(sdid)) {
				rights.add(right);
			}
		}
	}

	__rightsModel.setRights(location, rights);
}

/**
Saves the prior record selected in the table; called when moving to a new 
record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastLocationIndex);
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentLocationIndex);
}

/**
Saves the information associated with the currently-selected location.
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

	StateCU_Location location = 
		(StateCU_Location)__locationsVector.elementAt(record);

	location.setName(__nameJTextField.getText());
	location.setID(__locationIDJTextField.getText());
	location.setLatitude(
		new Double(__latitudeJTextField.getText()).doubleValue());
	location.setElevation(
		new Double(__elevationJTextField.getText()).doubleValue());
	location.setRegion1(__region1JTextField.getText());
	location.setRegion2(__region2JTextField.getText());
	location.setAwc(
		new Double(__awcJTextField.getText()).doubleValue());
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
		col = 1;
	}
	else {
		searchFor = __searchName.getText().trim();
		col = 2;
	}
	int index = __worksheet.find(searchFor, col, row, 
		JWorksheet.FIND_EQUAL_TO | JWorksheet.FIND_CONTAINS |
		JWorksheet.FIND_CASE_INSENSITIVE | JWorksheet.FIND_WRAPAROUND);
	if (index != -1) {
		selectTableIndex(index);
	}
}

/**
Selects the desired index in the table, but also displays the appropriate data
in the remainder of the window.
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
@param index the index to select
*/
private void setupGUI(int index) {
	String routine = "setupGUI";

	addWindowListener(this);

	JPanel p1 = new JPanel();	// first 6 months' effeciency
	//JPanel p2 = new JPanel();	// last 6 months' effeciency
	JPanel p3 = new JPanel();	// div sta id -> switch for diversion
	JPanel p4 = new JPanel();	// user name -> data type switch

	JPanel left_panel = new JPanel();	// multilist and search area
	JPanel right_panel = new JPanel();	// everything else

	__locationIDJTextField = new JTextField(12);
	__nameJTextField = new JTextField(24);
	__latitudeJTextField = new JTextField(12);
	__elevationJTextField = new JTextField(12);
	__region1JTextField = new JTextField(12);
	__region2JTextField = new JTextField(12);
	__awcJTextField = new JTextField(12);
	
	__searchID = new JTextField(10);
	__searchName = new JTextField(10);
	__searchName.setEditable(false);
	__findNextLocation = new JButton(__BUTTON_FIND_NEXT);
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton(__BUTTON_ID, true);
	__searchNameJRadioButton = new JRadioButton(__BUTTON_NAME, false);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	__searchCriteriaGroup.add(__searchNameJRadioButton);

	JButton applyJButton = new JButton(__BUTTON_APPLY);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton helpJButton = new JButton(__BUTTON_HELP);
	helpJButton.setEnabled(false);
	JButton closeJButton = new JButton(__BUTTON_CLOSE);

	GridBagLayout gb = new GridBagLayout();
	JPanel mainJPanel = new JPanel();
	mainJPanel.setLayout(gb);
	p1.setLayout(new GridLayout(4, 6, 2, 0));
	p3.setLayout(gb);
	p4.setLayout(gb);
	right_panel.setLayout(gb);
	left_panel.setLayout(gb);

	int y;

	PropList p = 
		new PropList("StateCU_Location_JFrame.JWorksheet");

	p.add("JWorksheet.CellFont=Courier");
	p.add("JWorksheet.CellStyle=Plain");
	p.add("JWorksheet.CellSize=11");
	p.add("JWorksheet.HeaderFont=Arial");
	p.add("JWorksheet.HeaderStyle=Plain");
	p.add("JWorksheet.HeaderSize=11");
	p.add("JWorksheet.HeaderBackground=LightGray");
	p.add("JWorksheet.RowColumnPresent=false");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	try {
		StateCU_Location_TableModel tmw = new
			StateCU_Location_TableModel(__locationsVector);
		StateCU_Location_CellRenderer crw = new
			StateCU_Location_CellRenderer(tmw);
	
		__worksheet = new JWorksheet(crw, tmw, p);

		__worksheet.removeColumn(3);
		__worksheet.removeColumn(4);
		__worksheet.removeColumn(5);
		__worksheet.removeColumn(6);
		__worksheet.removeColumn(7);
		__worksheet.removeColumn(8);
		__worksheet.removeColumn(9);
		__worksheet.removeColumn(10);
		__worksheet.removeColumn(11);
		__worksheet.removeColumn(12);
		__worksheet.removeColumn(13);		

		widths = crw.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		__worksheet = new JWorksheet(0, 0, p);
		e.printStackTrace();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);

	JGUIUtil.addComponent(left_panel, new JScrollPane(__worksheet),
		0, 0, 6, 9, 1, 1, 
		0, 0, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	y = 0;
	JGUIUtil.addComponent(p3, new JLabel("Location ID:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __locationIDJTextField,
		1, y, 1, 1, 1, 1,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__locationIDJTextField.setEditable(false);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Name:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __nameJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Latitude (Dec. Deg.):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __latitudeJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Elevation (Feet):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __elevationJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Region 1:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __region1JTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Region 2:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __region2JTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(p3, new JLabel("Available Water Content:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __awcJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	y = 0;

	// two top panels of info
	JGUIUtil.addComponent(right_panel, p3, 
		0, 0, 2, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	int[] widths2 = null;
	try {
		__stationModel = new
			StateCU_Location_TableModel(new Vector());
		StateCU_Location_CellRenderer crw = new
			StateCU_Location_CellRenderer(__stationModel);
	
		__stationWorksheet = new JWorksheet(crw, __stationModel, p);

		__stationWorksheet.removeColumn(1);
		__stationWorksheet.removeColumn(2);
		__stationWorksheet.removeColumn(3);
		__stationWorksheet.removeColumn(4);
		__stationWorksheet.removeColumn(9);
		__stationWorksheet.removeColumn(10);
		__stationWorksheet.removeColumn(11);
		__stationWorksheet.removeColumn(12);
		__stationWorksheet.removeColumn(13);		

		widths2 = crw.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		__stationWorksheet = new JWorksheet(0, 0, p);
		e.printStackTrace();
	}
	__stationWorksheet.setPreferredScrollableViewportSize(null);
	__stationWorksheet.setHourglassJFrame(this);
	JScrollPane jsp2 = new JScrollPane(__stationWorksheet);
	jsp2.setBorder(BorderFactory.createTitledBorder(jsp2.getBorder(),
		"Climate Stations"));
	y = 1;
	JGUIUtil.addComponent(right_panel, jsp2,
		0, y++, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	
	int[] widths3 = null;
	if (__dataset.getDataSetType() >= 
		StateCU_DataSet.TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS) {
		try {
			__rightsModel = new
				StateCU_Location_TableModel(new Vector());
			StateCU_Location_CellRenderer crw = new
				StateCU_Location_CellRenderer(__rightsModel);
		
			__rightsWorksheet = new JWorksheet(crw, __rightsModel, 
				p);
	
			__rightsWorksheet.removeColumn(1);
			__rightsWorksheet.removeColumn(2);
			__rightsWorksheet.removeColumn(3);
			__rightsWorksheet.removeColumn(4);
			__rightsWorksheet.removeColumn(5);
			__rightsWorksheet.removeColumn(6);
			__rightsWorksheet.removeColumn(7);
			__rightsWorksheet.removeColumn(8);
	
			widths3 = crw.getColumnWidths();
		}
		catch (Exception e) {
			Message.printWarning(2, routine, e);
			__rightsWorksheet = new JWorksheet(0, 0, p);
			e.printStackTrace();
		}
		__rightsWorksheet.setPreferredScrollableViewportSize(null);
		__rightsWorksheet.setHourglassJFrame(this);
		JScrollPane jsp3 = new JScrollPane(__rightsWorksheet);
		jsp3.setBorder(BorderFactory.createTitledBorder(
			jsp3.getBorder(), "Diversion Rights"));
		JGUIUtil.addComponent(right_panel, jsp3,
			0, y++, 1, 1, 1, 1, 
			0, 0, 0, 0,
			GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	}

	JPanel graphPanel = new JPanel();
	graphPanel.setLayout(gb);
	graphPanel.setBorder(BorderFactory.createTitledBorder(
		"Time Series"));
	int yy = 0;
	__precipitationCheckBox = new JCheckBox("Precipitation (Monthly)");
	__temperatureCheckBox = new JCheckBox("Temperature (Monthly)");
	__frostDatesCheckBox = new JCheckBox("Frost Dates (Yearly)");
	__cropPatternCheckBox = new JCheckBox("Crop Pattern (Yearly)");
	__irrigationPracticeCheckBox = new JCheckBox("Irrigation Practice "
		+ "(Yearly)");
	__diversionCheckBox = new JCheckBox("Diversion (Monthly)");
	
	JGUIUtil.addComponent(graphPanel, __precipitationCheckBox,
		0, yy++, 3, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(graphPanel, __temperatureCheckBox,
		0, yy++, 3, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(graphPanel, __frostDatesCheckBox,
		0, yy++, 3, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(graphPanel, __cropPatternCheckBox,
		0, yy++, 3, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	if (__dataset.getDataSetType() >= 
		StateCU_DataSet.TYPE_WATER_SUPPLY_LIMITED) {
		JGUIUtil.addComponent(graphPanel, __irrigationPracticeCheckBox,
			0, yy++, 3, 1, 1, 0, 
			0, 0, 0, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	}
	if (__dataset.getDataSetType() >=
		StateCU_DataSet.TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS) {
		JGUIUtil.addComponent(graphPanel, __diversionCheckBox,
			0, yy++, 3, 1, 1, 0, 
			0, 0, 0, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	}

	JButton graphButton = new JButton(__BUTTON_GRAPH);
	JButton tableButton = new JButton(__BUTTON_TABLE);
	JButton summaryButton = new JButton(__BUTTON_SUMMARY);
	JButton graphAllButton = new JButton(__BUTTON_GRAPH_ALL);
	
	JGUIUtil.addComponent(graphPanel, graphButton,
		0, yy, 1, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(graphPanel, tableButton,
		1, yy, 1, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	JGUIUtil.addComponent(graphPanel, summaryButton,
		2, yy++, 1, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(graphPanel, graphAllButton,
		0, yy, 2, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(right_panel, graphPanel,
		0, y++, 1, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST);	

	int[] widths4 = null;
	if (__dataset.getDataSetType() >= 
		StateCU_DataSet.TYPE_RIVER_DEPLETION) {	
		try {
			__delayModel = new
				StateCU_Location_TableModel(new Vector());
			StateCU_Location_CellRenderer crw = new
				StateCU_Location_CellRenderer(__delayModel);
		
			__delayWorksheet = new JWorksheet(crw, __delayModel, p);
	
			__delayWorksheet.removeColumn(1);
			__delayWorksheet.removeColumn(2);
			__delayWorksheet.removeColumn(5);
			__delayWorksheet.removeColumn(6);
			__delayWorksheet.removeColumn(7);
			__delayWorksheet.removeColumn(8);
			__delayWorksheet.removeColumn(9);
			__delayWorksheet.removeColumn(10);
			__delayWorksheet.removeColumn(11);
			__delayWorksheet.removeColumn(12);
			__delayWorksheet.removeColumn(13);
	
			widths4 = crw.getColumnWidths();
		}
		catch (Exception e) {
			Message.printWarning(2, routine, e);
			__delayWorksheet = new JWorksheet(0, 0, p);
			e.printStackTrace();
		}
		__delayWorksheet.setPreferredScrollableViewportSize(null);
		__delayWorksheet.setHourglassJFrame(this);
		JScrollPane jsp4 = new JScrollPane(__delayWorksheet);
		jsp4.setBorder(BorderFactory.createTitledBorder(
			jsp4.getBorder(), "Delay Table Assignment"));
		JGUIUtil.addComponent(right_panel, jsp4,
			0, y++, 1, 1, 1, 1, 
			0, 0, 0, 0,
			GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);	
	}
	
	// add search areas
	y = 10;
	JPanel searchPanel = new JPanel();
	searchPanel.setLayout(gb);
	searchPanel.setBorder(BorderFactory.createTitledBorder(
		"Search above list for:     "));
		
	JGUIUtil.addComponent(left_panel, searchPanel,
		0, y, 4, 1, 0, 0, 
		10, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	int y2 = 0;
	JGUIUtil.addComponent(searchPanel, __searchIDJRadioButton,
		0, y2, 1, 1, 0, 0, 
		5, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchIDJRadioButton.addActionListener(this);
	JGUIUtil.addComponent(searchPanel, __searchID,
		1, y2, 1, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__searchID.addActionListener(this);
	
	y2++;
	JGUIUtil.addComponent(searchPanel, __searchNameJRadioButton,
		0, y2, 1, 1, 0, 0, 
		5, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchNameJRadioButton.addActionListener(this);
	JGUIUtil.addComponent(searchPanel, __searchName,
		1, y2, 1, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__searchName.addActionListener(this);
	
	y2++;
	JGUIUtil.addComponent(searchPanel, __findNextLocation,
		0, y2, 4, 1, 0, 0, 
		20, 10, 20, 10,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextLocation.addActionListener(this);
	// add buttons which lead to location
	// direct flow demand, and return flow information
	y = 6;
	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
	JPanel p5 = new JPanel();
	p5.setLayout(new GridLayout(5, 2));

	// add help and close buttons
	y = 10;
	JPanel p6 = new JPanel();
	p6.setLayout(fl);
	if (__editable) {
		p6.add(applyJButton);
		p6.add(cancelJButton);
	}
	p6.add(helpJButton);
	p6.add(closeJButton);
	JGUIUtil.addComponent(right_panel, p6,
		GridBagConstraints.RELATIVE, y, 4, 1, 1, 0, 
		30, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.SOUTH);
	applyJButton.addActionListener(this);
	cancelJButton.addActionListener(this);
	helpJButton.addActionListener(this);
	closeJButton.addActionListener(this);
	
	JGUIUtil.addComponent(mainJPanel, left_panel,
		0, 0, 4, 10, 1, 1, 
		10, 10, 10, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainJPanel, right_panel,
		GridBagConstraints.RELATIVE, 0, 4, 10, .4, 1, 
		10, 10, 10, 10,
		GridBagConstraints.BOTH, GridBagConstraints.EAST);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);

	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 0, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__statusJTextField = new JTextField(5);
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __statusJTextField,
		7, 0, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottomJPanel);	

	getContentPane().add(mainJPanel);

	initializeDisables();

//	JGUIUtil.center(this);
	pack();
	setSize(800,820);
	selectTableIndex(index);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
	if (widths2 != null) {
		__stationWorksheet.setColumnWidths(widths2);
	}		
	if (widths3 != null) {
		__rightsWorksheet.setColumnWidths(widths3);
	}
	if (widths4 != null) {
		__delayWorksheet.setColumnWidths(widths4);
	}		
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
in StateCU_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	saveCurrentRecord();
}

/**
Responds to Window deactivated events; saves the current information.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {
	saveCurrentRecord();
}
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

private void initializeDisables() {
	__disables = new JComponent[0];

	__textDisables = new JTextComponent[7];
	__textDisables[0] = __locationIDJTextField;
	__textDisables[1] = __nameJTextField;
	__textDisables[2] = __latitudeJTextField;
	__textDisables[3] = __elevationJTextField;
	__textDisables[4] = __region1JTextField;
	__textDisables[5] = __region2JTextField;
	__textDisables[6] = __awcJTextField;

	__textUneditables = new int[1];
	__textUneditables[0] = 0;
}

private void somethingSelected() {
	if (!__editable) {
		for (int i = 0; i < __disables.length; i++) {
			__disables[i].setEnabled(false);
		}
		for (int i = 0; i < __textDisables.length; i++) {
			__textDisables[i].setEditable(false);
		}
	}
	else {
		for (int i = 0; i < __disables.length; i++) {
			__disables[i].setEnabled(true);
		}
		for (int i = 0; i < __textDisables.length; i++) {
			__textDisables[i].setEditable(true);
		}
		for (int i = 0; i < __textUneditables.length; i++) {
			__textDisables[__textUneditables[i]].setEditable(false);
		}
	}
}

/**
Disables everything (in response to nothing being selected)
*/
private void nothingSelected() {
	for (int i = 0; i < __disables.length; i++) {
		__disables[i].setEnabled(false);
	}
	for (int i = 0; i < __textDisables.length; i++) {
		__textDisables[i].setText("");
		__textDisables[i].setEditable(false);
	}
	if (__delayWorksheet != null) {
		__delayWorksheet.clear();
	}
	if (__stationWorksheet != null) {
		__stationWorksheet.clear();
	}
	if (__rightsWorksheet != null) {
		__rightsWorksheet.clear();
	}
	
}

}
