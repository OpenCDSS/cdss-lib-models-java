//------------------------------------------------------------------------------
// StateMod_RiverNetworkNode_JFrame - dialog to edit the river network (.rin)
//	information
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
// 2003-09-04	JTS, RTi		Added cancel and apply buttons.
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
// 2004-07-17	JTS, RTi		Current and last indices were not 
//					defaulting to -1 when the class was
//					instantiated, and this was causing
//					problems.
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
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_SortListener;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Class to display data about river stations.
*/
public class StateMod_RiverNetworkNode_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener,
JWorksheet_SortListener {

/**
Button group for selecting the kind of search to do.
*/
private ButtonGroup __searchCriteriaGroup;

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
	__cancelJButton,
	__findNext,
	__helpJButton,
	__closeJButton;

/**
Radio buttons for selecting the kind of search to do.
*/
private JRadioButton 
	__searchIDJRadioButton,
	__searchNameJRadioButton;

/**
Text fields for searching through the worksheet.
*/
private JTextField 
	__searchID,
	__searchName;

/**
Worksheet for displaying river station data.
*/
private JWorksheet __worksheet;

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

private DataSetComponent __riverNetworkNodeComponent;
private List __riverNetworkNodesVector;

private JTextField __idJTextField;
private JTextField __nameJTextField;
private JTextField __nodeJTextField;
private JTextField __commentJTextField;

private int __lastStationIndex = 1;
private int __currentStationIndex = -1;

/**
The index in __disables[] of textfields that should NEVER be made
editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
Array of JComponents that should be disabled when nothing is selected 
from the list.
*/
private JComponent[] __disables;

private boolean __editable = true;

/**
Constructor.
@param dataset the dataset containing the data to show in the form.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param editable whether the data on the gui can be edited or not.
*/
public StateMod_RiverNetworkNode_JFrame (
						StateMod_DataSet dataset,
						StateMod_DataSet_WindowManager
						dataset_wm, boolean editable )
{	
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	StateMod_GUIUtil.setTitle(this, dataset, "River Network Nodes", null);

	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__riverNetworkNodeComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RIVER_NETWORK);
	__riverNetworkNodesVector=(List)__riverNetworkNodeComponent.getData();

	int size = __riverNetworkNodesVector.size();
	StateMod_RiverNetworkNode r = null;
	for (int i = 0; i < size; i++) {
		r = (StateMod_RiverNetworkNode)
			__riverNetworkNodesVector.get(i);
		r.createBackup();
	}

	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the dataset containing the data to show in the form.
@param editable whether the data on the gui can be edited or not.
*/
public StateMod_RiverNetworkNode_JFrame(StateMod_DataSet dataset,
StateMod_RiverNetworkNode node, boolean editable) {
	super("");
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	StateMod_GUIUtil.setTitle(this, dataset, "River Network Nodes", null);

	__dataset = dataset;
	__riverNetworkNodeComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RIVER_NETWORK);
	__riverNetworkNodesVector=(List)__riverNetworkNodeComponent.getData();

	int size = __riverNetworkNodesVector.size();
	StateMod_RiverNetworkNode r = null;
	for (int i = 0; i < size; i++) {
		r = (StateMod_RiverNetworkNode)
			__riverNetworkNodesVector.get(i);
		r.createBackup();
	}

	String id = node.getID();
	int index = StateMod_Util.locateIndexFromID(id, 
		__riverNetworkNodesVector);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine="StateMod_RiverNetworkNode_JFrame.actionPerformed"; 
	if (Message.isDebugOn) {
		Message.printDebug(1, routine, 
		"In actionPerformed: " + e.getActionCommand());
	}

	if (e.getSource() == __closeJButton) {
		saveCurrentRecord();
		int size = __riverNetworkNodesVector.size();
		StateMod_RiverNetworkNode r = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			r = (StateMod_RiverNetworkNode)
				__riverNetworkNodesVector.get(i);
			if (!changed && r.changed()) {
				changed = true;
			}
			r.acceptChanges();
		}				
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_RIVER_NETWORK, true);
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.WINDOW_RIVER_NETWORK );
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (e.getSource() == __applyJButton) {
		saveCurrentRecord();
		int size = __riverNetworkNodesVector.size();
		StateMod_RiverNetworkNode r = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			r = (StateMod_RiverNetworkNode)
				__riverNetworkNodesVector.get(i);
			if (!changed && r.changed()) {
				changed = true;
			}
			r.createBackup();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_RIVER_NETWORK, true);
		}		
	}
	else if (e.getSource() == __cancelJButton) {
		int size = __riverNetworkNodesVector.size();
		StateMod_RiverNetworkNode r = null;
		for (int i = 0; i < size; i++) {
			r = (StateMod_RiverNetworkNode)
				__riverNetworkNodesVector.get(i);
			r.restoreOriginal();
		}			
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.WINDOW_RIVER_NETWORK );
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (e.getSource() == __helpJButton) {
		// REVISIT HELP (JTS - 2003-08-18)
	}
	else if (e.getSource() == __searchIDJRadioButton) {
		__searchName.setEditable(false);
		__searchID.setEditable(true);
	}
	else if (e.getSource() == __searchNameJRadioButton) {
		__searchName.setEditable(true);
		__searchID.setEditable(false);
	}		
	else if (e.getSource() == __findNext) {
		searchWorksheet(__worksheet.getSelectedRow() + 1);
	}
	else if (e.getSource() == __searchID || e.getSource() == __searchName) {
		searchWorksheet(0);
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
	new ResponseJDialog(this, 
		"Errors encountered", label, ResponseJDialog.OK);
	return false;
}
 
/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__searchCriteriaGroup = null;
	__findNext = null;
	__helpJButton = null;
	__closeJButton = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__searchID = null;
	__searchName = null;
	__worksheet = null;
	__dataset = null;
	super.finalize();
}

/**
Finds a river network node in the __riverNetworkNodesVector that has the
specified id.
@param id the id to match.
@return the matching base flow coefficient object, or null if no matches could
be found.
*/
/* TODO SAM 2007-03-01 Evaluate use
private StateMod_RiverNetworkNode findRiverNetworkNode(String id) {
	StateMod_RiverNetworkNode rnn = null;
	for (int i = 0; i < __riverNetworkNodesVector.size(); i++) {
		rnn = (StateMod_RiverNetworkNode)
			__riverNetworkNodesVector.elementAt(i);
		if (rnn.getID().equals(id)) {
			return rnn;
		}
	}
	return null;
}
*/

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to 
selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[4];
	int i = 0;
	__disables[i++] = __idJTextField;
	__disables[i++] = __nameJTextField;
	__disables[i++] = __nodeJTextField;
	__disables[i++] = __commentJTextField;

	__textUneditables = new int[2];
	__textUneditables[0] = 0;
	__textUneditables[1] = 1;
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
/*
Message.printStatus(1, "", "Current: " + __currentStationIndex);
Message.printStatus(1, "", "Last: " + __lastStationIndex);
Message.printStatus(1, "", "Orig: " + __worksheet.getOriginalRowNumber(index));
Message.printStatus(1, "", "Index: " + index);
*/
	__lastStationIndex = __currentStationIndex;
	__currentStationIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__disables, true);
		return;
	}

	JGUIUtil.enableComponents(__disables, __textUneditables,
		__editable);	

	StateMod_RiverNetworkNode rnn = (StateMod_RiverNetworkNode) 
		__riverNetworkNodesVector.get(__currentStationIndex);

	__idJTextField.setText(rnn.getID());
	__nameJTextField.setText(rnn.getName());
	__nodeJTextField.setText(rnn.getCstadn());
	__commentJTextField.setText(rnn.getComment());
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

	StateMod_RiverNetworkNode rnn = (StateMod_RiverNetworkNode) 
		__riverNetworkNodesVector.get(record);

Message.printStatus(1, "", "Setting " + record + " cstadn: " 
	+ __nodeJTextField.getText());
	rnn.setCstadn(__nodeJTextField.getText());
Message.printStatus(1, "", "Setting " + record + " comment: " 
	+ __commentJTextField.getText());
	rnn.setComment(__commentJTextField.getText());
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
Selects the desired ID in the left table and displays the appropriate data
in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheet.getRowCount();
	StateMod_RiverNetworkNode rnn = null;
	for (int i = 0; i < rows; i++) {
		rnn = (StateMod_RiverNetworkNode)__worksheet.getRowData(i);
		if (rnn.getID().trim().equals(id.trim())) {
			selectTableIndex(i);
			return;
		}
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
@param index the index of the network node to preselect.
*/
private void setupGUI(int index) {
	String routine = "StateMod_RiverNetworkNode_JFrame.setupGUI";

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
	__nodeJTextField = new JTextField(12);
	__commentJTextField = new JTextField(24);

	__applyJButton = new JButton(__BUTTON_APPLY);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__helpJButton = new JButton(__BUTTON_HELP);
	__closeJButton = new JButton(__BUTTON_CLOSE);

	GridBagLayout gb = new GridBagLayout();
	p1.setLayout(gb);

	int y = 0;
	
	PropList p= new PropList("StateMod_RiverNetworkNode_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		StateMod_RiverNetworkNode_TableModel tmr = new
			StateMod_RiverNetworkNode_TableModel(
			__riverNetworkNodesVector);
		StateMod_RiverNetworkNode_CellRenderer crr = new
			StateMod_RiverNetworkNode_CellRenderer(tmr);
	
		jsw = new JScrollWorksheet(crr, tmr, p);
		__worksheet = jsw.getJWorksheet();

		widths = crr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);	
	JGUIUtil.addComponent(p1, jsw,
		0, y, 4, 9, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(p1, new JLabel("ID:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p1, __idJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p1, new JLabel("Name:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p1, __nameJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p1, new JLabel("Downstream Node:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p1, __nodeJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(p1, new JLabel("Comment:"),
		5, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p1, __commentJTextField,
		6, y++, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	
	//
	// add search areas
	//	

	y = 10;
	
	JPanel searchPanel = new JPanel();
	searchPanel.setLayout(gb);
	searchPanel.setBorder(BorderFactory.createTitledBorder(
		"Search list for:     "));
	JGUIUtil.addComponent(p1, searchPanel,
		0, y, 1, 1, 0, 0,
		10, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
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
	__applyJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	__helpJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	getContentPane().add("Center", p1);
	getContentPane().add("South", pfinal);
	
	initializeDisables();
	
	selectTableIndex(index);

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_RIVER_NETWORK, this );
	}

	pack();
	setSize(690, 400);
	JGUIUtil.center(this);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}

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
Responds to Window closing events; closes the window and marks it closed
in StateMod_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	saveCurrentRecord();
	int size = __riverNetworkNodesVector.size();
	StateMod_RiverNetworkNode r = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		r = (StateMod_RiverNetworkNode)
			__riverNetworkNodesVector.get(i);
		if (!changed && r.changed()) {
			changed = true;
		}
		r.acceptChanges();
	}					
	if (changed) {		
		__dataset.setDirty(StateMod_DataSet.COMP_RIVER_NETWORK, true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.WINDOW_RIVER_NETWORK );
	}
	else {	JGUIUtil.close ( this );
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

/**
Called just before the worksheet is sorted.  Stores the index of the record
that is selected.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortAboutToChange(JWorksheet worksheet, int sort) {
	__sortSelectedRow = __worksheet.getOriginalRowNumber(
		__worksheet.getSelectedRow());
}

/**
Called when the worksheet is sorted.  Reselects the record that was selected
prior to the sort.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortChanged(JWorksheet worksheet, int sort) {
	__worksheet.deselectAll();
	__worksheet.selectRow(__worksheet.getSortedRowNumber(
		__sortSelectedRow));
}

}
