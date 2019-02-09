// StateCU_CropCharacteristics_JFrame - dialog to display crop char info

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
// StateCU_CropCharacteristics_JFrame - dialog to display crop char info
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 2003-07-14	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-22	JTS, RTi		Revised following SAM's review.
// 2004-02-28	Steven A. Malers, RTi	Move some utility code from StateCU_Data
//					to StateCU_Util.
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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a GUI for displaying crop char data.
*/
@SuppressWarnings("serial")
public class StateCU_CropCharacteristics_JFrame extends JFrame
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
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_APPLY = "Apply";

/**
Strings for the flag combo boxes.
*/
private final String
	__0_MEAN_TEMP = 	"0 - Mean Temp",
	__1_28_DEG_FROST = 	"1 - 28 Degree Frost",
	__2_32_DEG_FROST = 	"2 - 32 Degree Frost",
	__999_NONE = 		"-999 - N/A";

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
Index of the currently-selected crop.
*/
private int __currentCropIndex;
/**
Index of the previously-selected crop.
*/
private int __lastCropIndex;

/**
GUI buttons.
*/
private JButton __findNextCrop;

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
	__cropIDJTextField,
	__nameJTextField,
	__plantMonthJTextField,
	__plantDayJTextField,
	__harvestMonthJTextField,
	__harvestDayJTextField,
	__daysToCoverJTextField,
	__seasonLengthJTextField,
	__earliestValueJTextField,
	__latestValueJTextField,
	__maxRootFeetJTextField,
	__maxAppDepthJTextField,
	__firstDaysBetweenJTextField,
	__secondDaysBetweenJTextField;

/**
Worksheet for displaying crop information.
*/
private JWorksheet __worksheet;

private JWorksheet __coeffWorksheet;

private SimpleJComboBox
	__earliestFlagComboBox,
	__latestFlagComboBox;

/**
Dataset containing the crop data.
*/
private StateCU_DataSet __dataset;

/**
DataSetComponent containing crop data.
*/
private DataSetComponent __cropComponent;

private DataSetComponent __blaneyComponent;

/**
List of crops data in the DataSetComponent.
*/
private List<StateCU_CropCharacteristics> __cropsVector;

private List<StateCU_BlaneyCriddle> __blaneyVector;

private StateCU_CropCharacteristics_TableModel __blaneyModel;

private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Constructor.
@param dataset dataset containing data to display
@param editable whether the display should be editable or not
*/
public StateCU_CropCharacteristics_JFrame(StateCU_DataSet dataset, 
boolean editable) {
	super(dataset.getBaseName() + " - StateCU GUI - Crop Characteristics"
		+ " / Coefficients");
	__currentCropIndex = -1;
	__editable = editable;

	__dataset = dataset;
	__cropComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CROP_CHARACTERISTICS);
	@SuppressWarnings("unchecked")
	List<StateCU_CropCharacteristics> cropsVector0 = (List<StateCU_CropCharacteristics>)__cropComponent.getData();
	__cropsVector = cropsVector0;

	__blaneyComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_BLANEY_CRIDDLE);
	@SuppressWarnings("unchecked")
	List<StateCU_BlaneyCriddle> blaneyVector0 = (List<StateCU_BlaneyCriddle>)__blaneyComponent.getData();
	__blaneyVector = blaneyVector0;

	setupGUI(0);
}

/**
Constructor.
@param crop StateCU_CropCharacteristics object to display
@param editable whether the display should be editable or not.
*/
public StateCU_CropCharacteristics_JFrame(StateCU_DataSet dataset, 
StateCU_CropCharacteristics crop, boolean editable) {
	super(dataset.getBaseName() + " - StateCU GUI - Crop Characteristics");
	__currentCropIndex = -1;
	__editable = editable;

	__dataset = dataset;
	__cropComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_CROP_CHARACTERISTICS);
	@SuppressWarnings("unchecked")
	List<StateCU_CropCharacteristics> cropsVector0 = (List<StateCU_CropCharacteristics>)__cropComponent.getData();
	__cropsVector = cropsVector0;

	__blaneyComponent = __dataset.getComponentForComponentType(
		StateCU_DataSet.COMP_BLANEY_CRIDDLE);
	@SuppressWarnings("unchecked")
	List<StateCU_BlaneyCriddle> blaneyVector0 = (List<StateCU_BlaneyCriddle>)__blaneyComponent.getData();
	__blaneyVector = blaneyVector0;

	String id = crop.getID();
	int index = StateCU_Util.indexOf(__cropsVector, id);

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
	else if (action.equals(__BUTTON_CANCEL)) {
		dispose();
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
	}
	else if (source == __findNextCrop) {
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
	List<String> errors = new Vector<String>();
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
		label += errors.get(i) + "\n";
	}
	// TODO SAM 2007-03-01 Why is message/logging not used?
	new ResponseJDialog(this, 
		"Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {

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
	__lastCropIndex = __currentCropIndex;
	__currentCropIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		nothingSelected();
		return;
	}

	somethingSelected();

	StateCU_CropCharacteristics crop = (StateCU_CropCharacteristics)
		__cropsVector.get(__currentCropIndex);

	__cropIDJTextField.setText(crop.getID());
	__nameJTextField.setText(crop.getName());
	StateCU_Util.checkAndSet(crop.getGdate1(), __plantMonthJTextField);
	StateCU_Util.checkAndSet(crop.getGdate2(), __plantDayJTextField);
	StateCU_Util.checkAndSet(crop.getGdate3(), __harvestMonthJTextField);
	StateCU_Util.checkAndSet(crop.getGdate4(), __harvestDayJTextField);
	StateCU_Util.checkAndSet(crop.getGdate5(), __daysToCoverJTextField);
	StateCU_Util.checkAndSet(crop.getGdates(), __seasonLengthJTextField);
	StateCU_Util.checkAndSet(crop.getTmois1(), __earliestValueJTextField);
	StateCU_Util.checkAndSet(crop.getTmois2(), __latestValueJTextField);
	StateCU_Util.checkAndSet(crop.getFrx(), __maxRootFeetJTextField);
	StateCU_Util.checkAndSet(crop.getApd(), __maxAppDepthJTextField);
	StateCU_Util.checkAndSet(crop.getCut2(), __firstDaysBetweenJTextField);
	StateCU_Util.checkAndSet(crop.getCut3(), __secondDaysBetweenJTextField);

	int flag = crop.getTflg1();
	if (flag == -999) {
		flag = 3;
	}
	__earliestFlagComboBox.select(flag);

	flag = crop.getTflg2();
	if (flag == -999) {
		flag = 3;
	}	
	__latestFlagComboBox.select(flag);

	int bcindex = StateCU_Util.indexOfName(__blaneyVector, crop.getID());
	StateCU_BlaneyCriddle bc = null;	
	if (bcindex != -1) {
		bc = (StateCU_BlaneyCriddle)__blaneyVector.get(bcindex);
		if (bc.getFlag().equalsIgnoreCase("Percent")) {
			__coeffWorksheet.setColumnName(3, "PERCENT");
		}
		else {
			__coeffWorksheet.setColumnName(3, "DAY");
		}
	}
	__blaneyModel.setBlaneyCriddle(bc);
}

/**
Saves the prior record selected in the table; called when moving to a new 
record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastCropIndex);
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentCropIndex);
}

/**
Saves the information associated with the currently-selected crop.
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

	StateCU_CropCharacteristics crop = 
		(StateCU_CropCharacteristics)__cropsVector.get(record);

	crop.setName(__nameJTextField.getText());
	crop.setID(__cropIDJTextField.getText());
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
@param index the index in the worksheet to first select
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

	__cropIDJTextField = new JTextField(12);
	__nameJTextField = new JTextField(24);
	__plantMonthJTextField = new JTextField(6);
	__plantDayJTextField = new JTextField(6);
	__harvestMonthJTextField = new JTextField(6);
	__harvestDayJTextField = new JTextField(6);
	__daysToCoverJTextField = new JTextField(6);
	__seasonLengthJTextField = new JTextField(6);
	__earliestValueJTextField = new JTextField(6);
	__latestValueJTextField = new JTextField(6);
	__maxRootFeetJTextField = new JTextField(6);
	__maxAppDepthJTextField = new JTextField(6);
	__firstDaysBetweenJTextField = new JTextField(6);
	__secondDaysBetweenJTextField = new JTextField(6);

	List<String> v = new Vector<String>();
	v.add(__0_MEAN_TEMP);
	v.add(__1_28_DEG_FROST);
	v.add(__2_32_DEG_FROST);
	v.add(__999_NONE);
	__earliestFlagComboBox = new SimpleJComboBox(v);
	__latestFlagComboBox = new SimpleJComboBox(v);
	
	__searchID = new JTextField(10);
	__searchName = new JTextField(10);
	__searchName.setEditable(false);
	__findNextCrop = new JButton(__BUTTON_FIND_NEXT);
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
		new PropList("StateCU_CropCharacteristics_JFrame.JWorksheet");

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
		StateCU_CropCharacteristics_TableModel tmw = new
			StateCU_CropCharacteristics_TableModel(__cropsVector);
		StateCU_CropCharacteristics_CellRenderer crw = new
			StateCU_CropCharacteristics_CellRenderer(tmw);
	
		__worksheet = new JWorksheet(crw, tmw, p); 

		__worksheet.removeColumn(3);
		__worksheet.removeColumn(4);
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
		0, 0, 6, 14, 1, 1, 
		0, 0, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	y = 0;
	JGUIUtil.addComponent(p3, new JLabel("Crop ID:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __cropIDJTextField,
		1, y, 1, 1, 1, 1,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__cropIDJTextField.setEditable(false);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Name:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __nameJTextField,
		1, y, 3, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Planting Month and Day:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __plantMonthJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p3, __plantDayJTextField,
		2, y, 2, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Harvest Month and Day:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __harvestMonthJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p3, __harvestDayJTextField,
		2, y, 2, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Days to Full Cover:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __daysToCoverJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Length of Season (days):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __seasonLengthJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Earliest Moisture Use:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __earliestFlagComboBox,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p3, new JLabel("Value (F Deg.):"),
		2, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p3, __earliestValueJTextField,
		3, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(p3, new JLabel("Latest Moisture Use:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __latestFlagComboBox,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p3, new JLabel("Value (F Deg.):"),
		2, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p3, __latestValueJTextField,
		3, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Maximum Root Zone (feet):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __maxRootFeetJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, 
		new JLabel("Maximum Application Depth (inches):"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __maxAppDepthJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, 
		new JLabel("Days between 1st and 2nd cuttings:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __firstDaysBetweenJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	y++;
	JGUIUtil.addComponent(p3, 
		new JLabel("Days between 2nd and 3rd cuttings:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __secondDaysBetweenJTextField,
		1, y, 1, 1, 1, 1, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// two top panels of info
	JGUIUtil.addComponent(right_panel, p3, 
		0, 0, 2, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	int[] widths2 = null;
	try {
		__blaneyModel = new StateCU_CropCharacteristics_TableModel(
			__cropsVector);
		StateCU_CropCharacteristics_CellRenderer crw = 
			new StateCU_CropCharacteristics_CellRenderer(
			__blaneyModel);
	
		__coeffWorksheet = new JWorksheet(crw, __blaneyModel, p);

		__coeffWorksheet.removeColumn(1);
		__coeffWorksheet.removeColumn(2);
		widths2 = crw.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		__coeffWorksheet = new JWorksheet(0, 0, p);
		e.printStackTrace();
	}
	__coeffWorksheet.setPreferredScrollableViewportSize(null);
	__coeffWorksheet.setHourglassJFrame(this);

	JScrollPane jsp = new JScrollPane(__coeffWorksheet);
	jsp.setBorder(BorderFactory.createTitledBorder(jsp.getBorder(),
		"Blaney-Criddle Crop Coefficients"));
	JGUIUtil.addComponent(right_panel, jsp,
		0, y, 4, 4, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	// add search areas
	y = 14;
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
	JGUIUtil.addComponent(searchPanel, __findNextCrop,
		0, y2, 4, 1, 0, 0, 
		20, 10, 20, 10,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextCrop.addActionListener(this);
	// add buttons which lead to crop
	// direct flow demand, and return flow information
	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);

	// add help and close buttons
	y++;
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
		GridBagConstraints.RELATIVE, 0, 4, 10, 0, 1, 
		10, 10, 10, 10,
		GridBagConstraints.BOTH, GridBagConstraints.EAST);

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
	setSize(850,620);
	selectTableIndex(index);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
	if (widths2 != null) {
		__coeffWorksheet.setColumnWidths(widths2);
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
	__disables = new JComponent[2];
	__disables[0] = __latestFlagComboBox;
	__disables[1] = __earliestFlagComboBox;

	__textDisables = new JTextComponent[14];
	__textDisables[0] = __cropIDJTextField;
	__textDisables[1] = __nameJTextField;
	__textDisables[2] = __plantMonthJTextField;
	__textDisables[3] = __plantDayJTextField;
	__textDisables[4] = __harvestMonthJTextField;
	__textDisables[5] = __harvestDayJTextField;
	__textDisables[6] = __daysToCoverJTextField;
	__textDisables[7] = __seasonLengthJTextField;
	__textDisables[8] = __earliestValueJTextField;
	__textDisables[9] = __latestValueJTextField;
	__textDisables[10] = __maxRootFeetJTextField;
	__textDisables[11] = __maxAppDepthJTextField;
	__textDisables[12] = __firstDaysBetweenJTextField;
	__textDisables[13] = __secondDaysBetweenJTextField;

	__textUneditables = new int[1];
	__textUneditables[0] = 0;
}

private void somethingSelected() {
	if (!__editable) {
		for (int i = 0; i < __disables.length; i++) {
			__disables[i].setEnabled(false);
			if (__disables[i] instanceof SimpleJComboBox) {
				((SimpleJComboBox)__disables[i])
					.setEditable(false);
			}
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
}

}
