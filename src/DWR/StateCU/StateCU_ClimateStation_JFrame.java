//------------------------------------------------------------------------------
// StateCU_ClimateStation_JFrame - dialog to display climate station info
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2003-07-14	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-22	JTS, RTi		Revised after SAM's review.
// 2003-08-11	Steven A. Malers, RTi	* Force the title parameter in the
//					  constructor.
//					* Enable the time series output.
// 2004-02-28	SAM, RTi		* Move some utility code from
//					  StateCU_Data to StateCU_Util.
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
import javax.swing.text.JTextComponent;

import RTi.GRTS.TSProduct;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a GUI for displaying climate station data.
*/
public class StateCU_ClimateStation_JFrame extends JFrame
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
	__BUTTON_CANCEL = "Cancel";

/**
Boolean specifying whether the form is editable or not.
*/
private boolean __editable = false;

/**
Button group for selecting what to search the jworksheet for.
*/
private ButtonGroup __searchCriteriaGroup;

/**
DataSetComponent containing station data.
*/
private DataSetComponent __stationComponent;

/**
Array of the elements of __textDisables that should never be editable.
*/
private int[] __textUneditables;

/**
Index of the currently-selected station.
*/
private int __currentStationIndex;
/**
Index of the previously-selected station.
*/
private int __lastStationIndex;

/**
GUI buttons.
*/
private JButton 
	__findNextStation;

/**
Checkboxes for selecting the kind of time series to graph.
*/
private JCheckBox
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
	__stationIDJTextField,
	__nameJTextField,
	__latitudeJTextField,
	__elevationJTextField,
	__region1JTextField,
	__region2JTextField;

/**
Text fields comprising the status bar at the bottom of the form.
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet for displaying station information.
*/
private JWorksheet __worksheet;

/**
Dataset containing the station data.
*/
private StateCU_DataSet __dataset;

/**
Vector of stations data in the DataSetComponent.
*/
private List __stationsVector;

/**
Constructor.
@param title Title for JFrame.  If null, an appropriate default is used.
@param dataset dataset containing data to display
@param editable whether the display should be editable or not
*/
public StateCU_ClimateStation_JFrame (	String title, StateCU_DataSet dataset,
					boolean editable )
{	if ( title == null ) {
		setTitle ( "StateCU Climate Stations" );
	}
	else {	setTitle ( title );
	}
	__currentStationIndex = -1;
	__editable = editable;

	__dataset = dataset;
	__stationComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CLIMATE_STATIONS);
	__stationsVector = (List)__stationComponent.getData();

	setupGUI(0);
}

/**
Constructor.
@param title Title for JFrame.  If null, an appropriate default is used.
@param dataset dataset containing data to display
@param station StateCU_ClimateStation object to display
@param editable whether the display should be editable or not.
*/
public StateCU_ClimateStation_JFrame (	String title, StateCU_DataSet dataset, 
					StateCU_ClimateStation station,
					boolean editable)
{	if ( title == null ) {
		setTitle ( "StateCU Climate Stations" );
	}
	else {	setTitle ( title );
	}
	__dataset = dataset;
	__currentStationIndex = -1;	
	__stationsVector = new Vector();

	__stationComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CLIMATE_STATIONS);
	__stationsVector = (List)__stationComponent.getData();	

	String id = station.getID();
	int index = StateCU_Util.indexOf(__stationsVector, id);

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
		|| action.equals(__BUTTON_SUMMARY)) {
		displayTSViewJFrame(action);
	}	
	else if (source == __findNextStation) {
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
	List errors = new Vector();
	int errorCount = 0;
	// validate __nameJTextField == string
	// validate __stationIDJTextField == string
	// validate __latitudeJTextField == double
	// validate __elevationJTextField == double
	// validate __region1JTextField == string
	// validate __region2JTextField == string
	
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
		label += errors.get(i) + "\n";
	}
	// TODO SAM 2007-03-01 Why not using Message/Logging
	new ResponseJDialog(this, 
		"Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Display the time series.
@param action Event action that initiated the display.
*/
private void displayTSViewJFrame(String action)
{	String routine = "displayTSViewJFrame";

	// Initialize the display...

	PropList display_props = new PropList("ClimateStation");
	if (action.equals(__BUTTON_GRAPH)) {
		display_props.set("InitialView", "Graph");
	}
	else if (action.equals(__BUTTON_TABLE)) {
		display_props.set("InitialView", "Table");
	}
	else if (action.equals(__BUTTON_SUMMARY)) {
		display_props.set("InitialView", "Summary");
	}
	// display_props.set("HelpKey", "TSTool.ExportMenu");
	display_props.set("TotalWidth", "600");
	display_props.set("TotalHeight", "400");
	display_props.set("Title", "Demand");
	display_props.set("DisplayFont", "Courier");
	display_props.set("DisplaySize", "11");
	display_props.set("PrintFont", "Courier");
	display_props.set("PrintSize", "7");
	display_props.set("PageLength", "100");

	PropList props = new PropList("ClimateStation");

	List tslist = new Vector();

	// Get the time series to display and set plot properties if graphing.
	// For now need to find in the lists because references to time series
	// are not implemented...

	List v = null;
	TS ts = null;
	int sub = 0;
	int pos;
	StateCU_ClimateStation station = (StateCU_ClimateStation)
		__stationsVector.get(__currentStationIndex);
	if (__precipitationCheckBox.isSelected()) {
		v = (List)__dataset.getComponentForComponentType(
			StateCU_DataSet.COMP_PRECIPITATION_TS_MONTHLY).getData();
		pos = TSUtil.indexOf ( v, station.getID(), "Location", 1 );
		if ( pos >= 0 ) {
			ts = (TS)v.get(pos);
		}
		if ( ts != null ) {
			// Add a graph for precipitation...
			++sub;
			ts.setDataType ( "Precipitation" );
			props.set ( "SubProduct " + sub + ".GraphType=Bar" );
			props.set ( "SubProduct " + sub +
				".SubTitleString=Monthly Precipitation" );
			props.set ( "Data " + sub +
				".1.TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
	}
	if (__temperatureCheckBox.isSelected()) {
		v = (List)__dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_TEMPERATURE_TS_MONTHLY_AVERAGE).getData();
		pos = TSUtil.indexOf ( v, station.getID(), "Location", 1 );
		if ( pos >= 0 ) {
			ts = (TS)v.get(pos);
		}
		if ( ts != null ) {
			// Add a graph for temperature...
			++sub;
			ts.setDataType ( "Temperature" );
			props.set ( "SubProduct " + sub + ".GraphType=Line" );
			props.set ( "SubProduct " + sub +
				".SubTitleString=Monthly Average Temperature" );
			props.set ( "Data " + sub +
				".1.TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
	}
	if (__frostDatesCheckBox.isSelected()) {
		// REVISIT - no way to graph currently
	}

	// Display the time series...

	try {	TSProduct tsproduct = new TSProduct ( props, display_props );
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
	__textUneditables = null;
	__findNextStation = null;
	__disables = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__textDisables = null;
	__searchID = null;
	__searchName = null;
	__stationIDJTextField = null;
	__nameJTextField = null;
	__latitudeJTextField = null;
	__elevationJTextField = null;
	__region1JTextField = null;
	__region2JTextField = null;
	__worksheet = null;
	__dataset = null;
	__stationComponent = null;
	__stationsVector = null;
	super.finalize();
}

/**
Initializes the Swing Components that should be disabled or enabled when
stations are selected.  This should be called before the first call to
selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[0];

	__textDisables = new JTextComponent[6];
	__textDisables[0] = __stationIDJTextField;
	__textDisables[1] = __nameJTextField;
	__textDisables[2] = __latitudeJTextField;
	__textDisables[3] = __elevationJTextField;
	__textDisables[4] = __region1JTextField;
	__textDisables[5] = __region2JTextField;

	__textUneditables = new int[1];
	__textUneditables[0] = 0;
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
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it 
on the form.
@param index the index of the reservoir to display on the form.
*/
private void processTableSelection(int index) {
	__lastStationIndex = __currentStationIndex;
	__currentStationIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		nothingSelected();
		return;
	}

	somethingSelected();

	StateCU_ClimateStation station = (StateCU_ClimateStation)
		__stationsVector.get(__currentStationIndex);

	__stationIDJTextField.setText(station.getID());
	__nameJTextField.setText(station.getName());
	StateCU_Util.checkAndSet(station.getLatitude(), __latitudeJTextField);
	StateCU_Util.checkAndSet(station.getElevation(), 
		__elevationJTextField);
	__region1JTextField.setText(station.getRegion1());
	__region2JTextField.setText(station.getRegion2());
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
Saves the information associated with the currently-selected station.
The user doesn't need to hit the return key for the gui to recognize changes.
The info is saved each time the user selects a differents tation or pressed
the close button.
*/
private void saveInformation(int record) {	
	if (!__editable || record == -1) {
		return;
	}

	StateCU_ClimateStation station = 
		(StateCU_ClimateStation)__stationsVector.get(record);

	if(!checkInput()) {
		return;
	}
		
	station.setName(__nameJTextField.getText());
	station.setID(__stationIDJTextField.getText());
	station.setLatitude(
		new Double(__latitudeJTextField.getText()).doubleValue());
	station.setElevation(
		new Double(__elevationJTextField.getText()).doubleValue());
	station.setRegion1(__region1JTextField.getText());
	station.setRegion2(__region2JTextField.getText());
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

	__stationIDJTextField = new JTextField(12);
	__nameJTextField = new JTextField(24);
	__latitudeJTextField = new JTextField(12);
	__elevationJTextField = new JTextField(12);
	__region1JTextField = new JTextField(12);
	__region2JTextField = new JTextField(12);
	
	__searchID = new JTextField(10);
	__searchName = new JTextField(10);
	__searchName.setEditable(false);
	__findNextStation = new JButton(__BUTTON_FIND_NEXT);
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
	left_panel.setLayout(gb);

	int y;

	PropList p = 
		new PropList("StateCU_ClimateStation_JFrame.JWorksheet");

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
		StateCU_ClimateStation_TableModel tmw = new
			StateCU_ClimateStation_TableModel(__stationsVector);
		StateCU_ClimateStation_CellRenderer crw = new
			StateCU_ClimateStation_CellRenderer(tmw);
	
		__worksheet = new JWorksheet(crw, tmw, p);

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
		0, 0, 6, 6, 1, 1, 
		0, 0, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	y = 0;
	JGUIUtil.addComponent(p3, new JLabel("Station ID:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __stationIDJTextField,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__stationIDJTextField.setEditable(false);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Name:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __nameJTextField,
		1, y, 1, 1, 1, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Latitude (Dec. Deg.):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __latitudeJTextField,
		1, y, 1, 1, 1, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Elevation (Feet):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __elevationJTextField,
		1, y, 1, 1, 1, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Region 1:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __region1JTextField,
		1, y, 1, 1, 1, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Region 2:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __region2JTextField,
		1, y, 1, 1, 1, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JPanel graphPanel = new JPanel();
	graphPanel.setLayout(gb);
	graphPanel.setBorder(BorderFactory.createTitledBorder(
		"Time Series"));
	int yy = 0;
	__precipitationCheckBox = new JCheckBox("Precipitation (Monthly)");
	__temperatureCheckBox = new JCheckBox("Temperature (Monthly)");
	__frostDatesCheckBox = new JCheckBox("Frost Dates (Yearly)");
	
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

	if (!__dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_PRECIPITATION_TS_MONTHLY).hasData()) {
		__precipitationCheckBox.setEnabled(false);
	}
	if (!__dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_TEMPERATURE_TS_MONTHLY_AVERAGE).hasData()) {
		__temperatureCheckBox.setEnabled(false);	
	}
	if (!__dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_FROST_DATES_TS_YEARLY).hasData()) {
		__frostDatesCheckBox.setEnabled(false);
	}		

	JButton graphButton = new SimpleJButton(__BUTTON_GRAPH, __BUTTON_GRAPH,
						this);
	JButton tableButton = new SimpleJButton(__BUTTON_TABLE, __BUTTON_TABLE,
						this);
	JButton summaryButton = new SimpleJButton(__BUTTON_SUMMARY,
						__BUTTON_SUMMARY, this);
	
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
	
	JGUIUtil.addComponent(p3, graphPanel,
		0, y++, 2, 1, 1, 0, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST);	
	
	// add search areas
	y = 7;
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
	JGUIUtil.addComponent(searchPanel, __findNextStation,
		0, y2, 4, 1, 0, 0, 
		20, 10, 20, 10,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextStation.addActionListener(this);
	// add buttons which lead to station water rights,
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

	// two top panels of info
	JGUIUtil.addComponent(mainJPanel, p3, 
		6, 0, 1, 6, 0, 1, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST);	
	JGUIUtil.addComponent(mainJPanel, p6,
		6, 7, 1, 1, 0, 0, 
		30, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH);
	applyJButton.addActionListener(this);
	cancelJButton.addActionListener(this);
	helpJButton.addActionListener(this);
	closeJButton.addActionListener(this);
	
	JGUIUtil.addComponent(mainJPanel, left_panel,
		0, 0, 4, 10, 1, 1, 
		10, 10, 10, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	getContentPane().add(mainJPanel);

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

	initializeDisables();

//	JGUIUtil.center(this);
	pack();
	setSize(730,440);
	selectTableIndex(index);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
}

/**
Called when an object has been selected from the left worksheet -- enables
or disables components as appropriate.
*/
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

}
