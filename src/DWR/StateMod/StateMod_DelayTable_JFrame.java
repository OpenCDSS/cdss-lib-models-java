//------------------------------------------------------------------------------
// StateMod_DelayTable_JFrame - dialog to edit the delay information.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 14 Mar 2000	CEN, RTi		Created class
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 04 May 2001	SAM, RTi		Verify that TSView code is properly
//					configured for all views.
// 13 Aug 2001	SAM, RTi		Update to handle returns as percent or
//					decimal fraction.
// 2002-03-07	SAM, RTi		Update to select the first item in the
//					list when displayed - actually - don't
//					do this because it may be slow,
//					depending on what data are available.
//------------------------------------------------------------------------------
// 2003-06-09	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMdelaysWindow
// 2003-06-17	JTS, RTi		Created first functional version.
// 2003-06-19	JTS, RTi		Finished first functional version, 
//					Javadoc'd.
// 2003-06-20	JTS, RTi		Constructor now takes a data set as
//					a parameter, instead of a data set
//					component.
// 2003-06-23	JTS, RTi		Implemented graphing of delay series.
// 2003-07-15	JTS, RTi		* Added status bar.
//					* Changed to use new dataset design.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		* Changed isDirty() back to setDirty().
//					* Require the title parameter in the
//					  constructor.
//					* Add a constructor to select a delay
//					  table at creation.
// 2003-08-16	SAM, RTi		* Change the window type to
//					  WINDOW_DELAY_TABLE_MONTHLY and
//					  WINDOW_DELAY_TABLE_DAILY.  The window
//					  is used for both monthly and daily
//					  delay tables but but both windows can
//					  be open at the same time.
//					* Require a flag for the constructor
//					  indicating whether monthly or daily
//					  delay tables are being displayed.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-08-27	JTS, RTi		Added selectID() to select an ID 
//					on the worksheet from outside the GUI.
// 2003-09-04	SAM, RTi		* Change so daily delay table results in
//					  a daily time series being created.
//					* Pass flag to table model constructor
//					  to indicate whether monthly or daily
//					  data.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-14	JTS, RTi		Updated to use new data saving model.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
// 2004-08-25	JTS, RTi		Based on the value of 'interv' in 
//					the control file, the header for
//					the return column says whether it is
//					a percent or a fraction.
// 2004-08-26	JTS, RTi		Implement Apply/Cancel/Close 
//					functionality.
// 2006-01-19	JTS, RTi		Made the dialog wider so that the 
//					Graph button is displayed.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import DWR.StateMod.StateMod_DelayTable;

import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TSIdent;
import RTi.TS.TS;
import RTi.GRTS.TSViewJFrame;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;

import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class is a gui for displaying and editing DelayTable information.
*/
public class StateMod_DelayTable_JFrame 
extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Local reference to the window number as defined in StateMod_GUIUtil.
*/
public int __window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_MONTHLY;

/**
Whether the data are editable or not.
*/
private boolean __editable = false;

/**
Whether the data are for monthly (true) or daily (false) data.
*/
private boolean __monthly_data = true;

/**
Button labels.
*/
private final String 
	__BUTTON_ADD_RETURN = 		"Add return",
	__BUTTON_APPLY = 		"Apply",
	__BUTTON_CANCEL = 		"Cancel",		
	__BUTTON_CLOSE = 		"Close",
	__BUTTON_DELETE_RETURN = 	"Delete return",
	__BUTTON_HELP = 		"Help";

/**
The kind of component being displayed.
*/
private int __componentType = -1;

/**
Currently-selected delay table index.
*/
private int __currentIndex = -1;

/**
Form buttons.
*/
private JButton 
	__addReturn,
	__closeJButton,
	__deleteReturn,
	__findNextDelay,
	__graphDelayJButton,
	__helpJButton;
	
/**
Textfield for searching the delay list for a certain ID.
*/
private JTextField __searchID;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet for displaying the delay table IDs.
*/
private JWorksheet __worksheetL;
/**
Worksheet for displaying the delay table data.
*/
private JWorksheet __worksheetR;

/**
The StateMod_DataSet that contains the statemod data.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The DataSetComponent that contains the delay data.
*/
private DataSetComponent __delayComponent;

/**
The Vector of delay data in the DataSetComponent.
*/
private Vector __delaysVector;

/**
Constructor.
@param dataset the dataset containing the data to display
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param monthly_data If true, display the monthly delay tables.  If false,
display the daily delay tables.
@param editable whether the data is editable or not
*/
public StateMod_DelayTable_JFrame (	StateMod_DataSet dataset,
					StateMod_DataSet_WindowManager
					dataset_wm,
					boolean monthly_data, boolean editable )
{	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__monthly_data = monthly_data;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	String interval = " (Monthly)";
	if ( __monthly_data ) {
		__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_MONTHLY;
		__delayComponent = __dataset.getComponentForComponentType(
			__dataset.COMP_DELAY_TABLES_MONTHLY);
		__componentType = __dataset.COMP_DELAY_TABLES_MONTHLY;
	}
	else {	__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_DAILY;
		__delayComponent = __dataset.getComponentForComponentType(
			__dataset.COMP_DELAY_TABLES_DAILY);
		__componentType = __dataset.COMP_DELAY_TABLES_DAILY;
		interval = " (Daily)";
	}
	StateMod_GUIUtil.setTitle(this, dataset, "Delay Tables" + interval, 
		null);	

	__delaysVector = (Vector)__delayComponent.getData();

	int size = __delaysVector.size();
	StateMod_DelayTable dt = null;
	for (int i = 0; i < size; i++) {
		dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
		dt.createBackup();
	}

	__editable = editable;
	setupGUI(0);
}

/**
Constructor.
@param dataset the dataset containing the data to display
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param delayTable the delay table to display.
@param monthly_data If true, display the monthly delay tables.  If false,
display the daily delay tables.
@param editable whether the data is editable or not
*/
public StateMod_DelayTable_JFrame (	StateMod_DataSet dataset,
					StateMod_DataSet_WindowManager
					dataset_wm,
					StateMod_DelayTable delayTable,
					boolean monthly_data, boolean editable )
{	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__monthly_data = monthly_data;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	String interval = " (Monthly)";
	if ( __monthly_data ) {
		__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_MONTHLY;
		__delayComponent = __dataset.getComponentForComponentType(
			__dataset.COMP_DELAY_TABLES_MONTHLY);
		__componentType = __dataset.COMP_DELAY_TABLES_MONTHLY;
	}
	else {	__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_DAILY;
		__delayComponent = __dataset.getComponentForComponentType(
			__dataset.COMP_DELAY_TABLES_DAILY);
		__componentType = __dataset.COMP_DELAY_TABLES_DAILY;
		interval = " (Daily)";
	}
	StateMod_GUIUtil.setTitle(this, dataset, "Delay Tables" + interval, 
		null);		

	__delaysVector = (Vector)__delayComponent.getData();

	int size = __delaysVector.size();
	StateMod_DelayTable dt = null;
	for (int i = 0; i < size; i++) {
		dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
		dt.createBackup();
	}

	String id = delayTable.getID();
	int index = StateMod_Util.indexOf ( __delaysVector, id );

	__editable = editable;
	setupGUI(index);
}

/**
Constructor.
@param delaysVector the Vector of delays to show
@param delayTable the delay table to display.
@param monthly_data If true, display the monthly delay tables.  If false,
display the daily delay tables.
@param editable whether the data is editable or not
*/
public StateMod_DelayTable_JFrame (	Vector delaysVector,
					StateMod_DelayTable delayTable,
					boolean monthly_data, boolean editable )
{	__monthly_data = monthly_data;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	String interval = " (Monthly)";
	if ( __monthly_data ) {
		__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_MONTHLY;
		__componentType = __dataset.COMP_DELAY_TABLES_MONTHLY;
	}
	else {	__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_DAILY;
		__componentType = __dataset.COMP_DELAY_TABLES_DAILY;
		interval = " (Daily)";
	}
	StateMod_GUIUtil.setTitle(this, null, "Delay Tables" + interval, 
		null);			

	__delaysVector = delaysVector;

	int size = __delaysVector.size();
	StateMod_DelayTable dt = null;
	for (int i = 0; i < size; i++) {
		dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
		dt.createBackup();
	}

	String id = delayTable.getID();
	int index = StateMod_Util.indexOf ( __delaysVector, id );

	__editable = editable;
	setupGUI(index);
}

/**
Constructor.  This version is used, for example, to display delay tables from
StateDMI when used with a StateCU_DataSet.
@param delaysVector the Vector of delays to show
@param monthly_data If true, display the monthly delay tables.  If false,
display the daily delay tables.
@param editable whether the data is editable or not
*/
public StateMod_DelayTable_JFrame (	Vector delaysVector,
					boolean monthly_data, boolean editable)
{	__monthly_data = monthly_data;
	String interval = " (Monthly)";
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	if ( __monthly_data ) {
		__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_MONTHLY;
		__componentType = __dataset.COMP_DELAY_TABLES_MONTHLY;
	}
	else {	__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_DAILY;
		__componentType = __dataset.COMP_DELAY_TABLES_DAILY;
		interval = " (Daily)";
	}
	StateMod_GUIUtil.setTitle(this, null, "Delay Tables" + interval, 
		null);				

	__delaysVector = delaysVector;

	int size = __delaysVector.size();
	StateMod_DelayTable dt = null;
	for (int i = 0; i < size; i++) {
		dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
		dt.createBackup();
	}

	__editable = editable;

	setupGUI( 0 );
}

/**
Constructor.
@param delay the Delay to show
@param monthly_data If true, display the monthly delay tables.  If false,
display the daily delay tables.
@param editable whether the gui data is editable or not
*/
public StateMod_DelayTable_JFrame (	StateMod_DelayTable delay,
					boolean monthly_data, boolean editable )
{	
	__monthly_data = monthly_data;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	String interval = " (Monthly)";
	if ( __monthly_data ) {
		__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_MONTHLY;
		__componentType = __dataset.COMP_DELAY_TABLES_MONTHLY;
	}
	else {	__window_type =
		StateMod_DataSet_WindowManager.WINDOW_DELAY_TABLE_DAILY;
		__componentType = __dataset.COMP_DELAY_TABLES_DAILY;
		interval = " (Daily)";
	}
	StateMod_GUIUtil.setTitle(this, null, "Delay Table" + interval, 
		null);				
	__delaysVector = new Vector();
	__delaysVector.add(delay);

	int size = __delaysVector.size();
	StateMod_DelayTable dt = null;
	for (int i = 0; i < size; i++) {
		dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
		dt.createBackup();
	}

	__editable = editable;

	setupGUI( 0 );
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_DelayTable_JFrame"
		+ ".actionPerformed"; 
	if (Message.isDebugOn) {
		Message.printDebug(1, routine, "In actionPerformed: " 
			+ e.getActionCommand());
	}

	String action = e.getActionCommand();

	if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-06-09)
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		closeWindow();
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveDelayTable();
		int size = __delaysVector.size();
		StateMod_DelayTable dt = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
			if (!changed && dt.changed()) {
				changed = true;
			}
			dt.createBackup();
		}		
		if (changed) {
			__dataset.setDirty(__componentType, true);
		}		
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		__worksheetR.deselectAll();
		int size = __delaysVector.size();
		StateMod_DelayTable dt = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
			if (!changed && dt.changed()) {
				changed = true;
			}
			dt.restoreOriginal();
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (__window_type);
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_ADD_RETURN)) {
		int row = __worksheetR.getSelectedRow();
		
		int total_num_rows = __worksheetR.getRowCount() - 1;

		if (row == -1) {
			row = total_num_rows;
		}

		if (row != -1) {
			if (row == total_num_rows) {
				int x = new ResponseJDialog(this, "Insert row", 
					"Do you wish to add a new row above "
						+ "the last row?\n"
						+ "(A response of \"No\" "
						+ "indicates to add below)",
						ResponseJDialog.YES | 
						ResponseJDialog.NO |
						ResponseJDialog.CANCEL)
						.response();
				if (x == ResponseJDialog.CANCEL) {
					return;
				}
				else if (x == ResponseJDialog.NO) {
					row += 1;
				}
			}
			__worksheetR.insertRowAt(new Double(0), row);
			__worksheetR.scrollToRow(row);
			__worksheetR.selectRow(row);			
		}
		else {
			__worksheetR.addRow(new Double(0));
			__worksheetR.scrollToRow(0);
			__worksheetR.selectRow(0);	
		}			
		__deleteReturn.setEnabled(true);
	}
	else if (action.equals(__BUTTON_DELETE_RETURN)) {
		int row = __worksheetR.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this,
				"Delete Return",
				"Delete return?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.NO) {
				return;
			}
			StateMod_DelayTable dt = (StateMod_DelayTable)
				__worksheetL.getRowData(
				__worksheetL.getSelectedRow());
			__worksheetR.deleteRow(row);
			__deleteReturn.setEnabled(false);
		}
		else {	
			Message.printWarning(1, routine, 
				"Must select desired right to delete.");
		}
	}
	else if (e.getSource() == __findNextDelay) {
		searchLeftWorksheet(__worksheetL.getSelectedRow() + 1);
	}
	else if (e.getSource() == __searchID) {
		searchLeftWorksheet();
	}
	else {	
		if (__worksheetL.getSelectedRow() == -1) {
			new ResponseJDialog(this,
				"You must first select a delay from the list.",
				ResponseJDialog.OK);
			return;
		}
		else if (e.getSource() == __graphDelayJButton) {
			try {	__worksheetR.deselectAll();

				int index = __worksheetL.getSelectedRow();
				if (index == -1) {
					return;
				}

				StateMod_DelayTable currentDelay = 
					((StateMod_DelayTable)
					__delaysVector.elementAt( index));

				int j;

				DateTime date;

				TSIdent tsident = new TSIdent();
				tsident.setLocation(currentDelay.getID());
				tsident.setSource("StateMod");
				if ( __monthly_data ) {
					tsident.setInterval("Month");
				}
				else {	tsident.setInterval("Day");
				}
				tsident.setType("Delay");

				DateTime date1 = null;
				DateTime date2 = null;
				int interval_base;
				if ( __monthly_data ) {
					date1 =
					new DateTime(DateTime.PRECISION_MONTH);
					date2 =
					new DateTime(DateTime.PRECISION_MONTH);
					interval_base = TimeInterval.MONTH;
				}
				else {	date1 =
					new DateTime(DateTime.PRECISION_DAY);
					date2 =
					new DateTime(DateTime.PRECISION_DAY);
					interval_base = TimeInterval.DAY;
				}
				date1.setMonth(1);
				date1.setYear(1);
				date2.setMonth(1);
				date2.setYear(1);
				date2.addInterval ( interval_base,
					(currentDelay.getNdly()-1) );

				TS ts = null;
				if ( __monthly_data ) {
					ts = new MonthTS();
				}
				else {	ts = new DayTS();
				}
				ts.setDate1(date1);
				ts.setDate2(date2);
				ts.setIdentifier(tsident);
				if ( __monthly_data ) {
					ts.setDescription(ts.getLocation()
					+ " Monthly Delay Table");
				}
				else {	ts.setDescription(ts.getLocation()
					+ " Daily Delay Table");
				}
				ts.setDataType("Delay");
				ts.setDataUnits(currentDelay.getUnits());
				ts.allocateDataSpace();

				double max = 0.0;
				for ( 	date = new DateTime(date1), j=0; 
					date.lessThanOrEqualTo(date2); 
					date.addInterval(interval_base, 1),
					j++) {
					ts.setDataValue(date, 
					currentDelay.getRet_val(j));
					if (currentDelay.getRet_val(j)> max) {
						max =
						currentDelay.getRet_val(j);
					}
				}
				Vector tslist = new Vector();
				tslist.add(ts);

				PropList graphProps = new PropList("TSView");
				// If dealing with small values, use a high
				// of precision...
				if (max < 1.0) {
					graphProps.set("YAxisPrecision","6");
					graphProps.set("OutputPrecision","6");
				}
				else {	
					graphProps.set("YAxisPrecision","3");
					graphProps.set("OutputPrecision","3");
				}
				graphProps.set("InitialView", "Graph");
				graphProps.set("TotalWidth", "600");
				graphProps.set("TotalHeight", "400");
				if ( __monthly_data ) {
					graphProps.set("Title",
					ts.getLocation() +
					" Monthly Delay Table");
				}
				else {	graphProps.set("Title",
					ts.getLocation() +
					" Daily Delay Table");
				}
				graphProps.set("DisplayFont", "Courier");
				graphProps.set("DisplaySize", "11");
				graphProps.set("PrintFont", "Courier");
				graphProps.set("PrintSize", "7");
				graphProps.set("PageLength", "100");
				new TSViewJFrame(tslist, graphProps);
			} 
			catch (Exception ex) {
				Message.printWarning(1, routine, 
				"Unable to graph delay. ");
			}
		}
	}
}

private int checkInput() {
	if (!__worksheetR.stopEditing()) {
		return 1;
	}
	return 0;
	/*
	int size = __worksheetR.getRowCount();
	String warning = "";

	int fatalCount = 0;
	for (int i = 0; i < size; i++) {
		if (!StringUtil.isDouble()) {
			warning += "\nAdministration number (" + adminNum 
				+ ") is not a number.";
			fatalCount++;
		}
	*/
}

/**
Closes the window.
*/
private void closeWindow() {
	if (checkInput() <= 0) {
		saveDelayTable();
		__worksheetR.deselectAll();
	}
	else {
		return;
	}

	int size = __delaysVector.size();
	StateMod_DelayTable dt = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		dt = (StateMod_DelayTable)__delaysVector.elementAt(i);
		if (!changed && dt.changed()) {
			changed = true;
		}
		dt.acceptChanges();
	}		
	if (changed) {
		__dataset.setDirty(__componentType, true);
	}
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (__window_type);
	}
	else {	JGUIUtil.close ( this );
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__addReturn = null;
	__closeJButton = null;
	__deleteReturn = null;
	__findNextDelay = null;
	__graphDelayJButton = null;
	__helpJButton = null;
	__searchID = null;
	__worksheetL = null;
	__worksheetR = null;
	__dataset = null;
	__delayComponent = null;
	__delaysVector = null;
	super.finalize();
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; calls 'processLeftTableSelection' with the 
newly-selected index in the table.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
/*
	if (e.getSource() == __worksheetL) {
		processLeftTableSelection(__worksheetL.getSelectedRow());
	}
	else {
		processRightTableSelection(__worksheetR.getSelectedRow());
	}
*/
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
Responds to mouse released events; calls 'processXXXTableSelection' with the 
newly-selected index in the table.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	if (e.getSource() == __worksheetL) {
		processLeftTableSelection(__worksheetL.getSelectedRow(), true);
	}
	else {	processRightTableSelection(__worksheetR.getSelectedRow());
	}	
}


/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by getting the next selection's data out of the 
data and displaying it on the form.
@param index the index of the delay table info to display on the form.
*/
private void processLeftTableSelection(int index, boolean tryToSave) {
	if (index == -1) {
		return;
	}

	if (tryToSave && (checkInput() > 0)) {
		if (__currentIndex > -1) {
			selectLeftTableIndex(__currentIndex, false, false);
		}
		return;
	}	

	// if gotten this far, then the delay table can be saved back
	// (if it has changed)
	saveDelayTable();
	
	__currentIndex = index;	

	StateMod_DelayTable dt = (StateMod_DelayTable)
		__worksheetL.getRowData(index);

	updateRightTable(dt);
}

/**
Processes a selection on the right table and either selects or deselects
the delete button, depending on whether a row was selected.
@param index the row that was selected (-1 if none are selected).
*/
private void processRightTableSelection(int index) {
	if (index == -1) {
		__deleteReturn.setEnabled(false);
	}
	else {
		__deleteReturn.setEnabled(true);
	}
}

/**
Checks to see if any data has changed in the delay table and if so, writes
the delay table back into the StateMod_DelayTable object.
*/
private void saveDelayTable() {
	String routine = "StateMod_DelayTable_JFrame.saveDelayTable";
	int index = __currentIndex;
	if (index < 0) {
		return;
	}

	Vector wv = __worksheetR.getAllData();
	/*
	StateMod_DelayTable dt = (StateMod_DelayTable)
		__worksheetL.getRowData(index);

	Vector dv = dt.getRet_val();

	boolean needToSave = false;
	if (wv.size() != dv.size()) {
		// definitely save
		needToSave = true;
	}
	else {
		int size = wv.size();
		Double d1;
		Double d2;
		for (int i = 0; i < size; i++) {
			d1 = (Double)wv.elementAt(i);
			d2 = (Double)dv.elementAt(i);
			if (d1.compareTo(d2) != 0) {
				needToSave = true;
				i = size + 1;
			}
		}
	}

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		return;
	}
	Vector clone = new Vector();
	int size = wv.size();	
	for (int i = 0; i < size; i++) {
		clone.add(new Double(((Double)wv.elementAt(i)).doubleValue()));
	}
	dt.setRet_val(clone);
	*/
	((StateMod_DelayTable)__delaysVector.elementAt(index)).setRet_val(wv);	
}

/**
Searches through the worksheet for a value, starting at the first row.
If the value is found, the row is selected.
*/
public void searchLeftWorksheet() {
	searchLeftWorksheet(0);
}

/**
Searches through the worksheet for a value, starting at the given row.
If the value is found, the row is selected.
@param row the row to start searching from.
*/
public void searchLeftWorksheet(int row) {
	String searchFor = null;
	int col = 0;
	searchFor = __searchID.getText().trim();

	int index = __worksheetL.find(searchFor, col, row, 
		JWorksheet.FIND_EQUAL_TO | JWorksheet.FIND_CONTAINS |
		JWorksheet.FIND_CASE_INSENSITIVE | JWorksheet.FIND_WRAPAROUND);
	if (index != -1) {
		selectLeftTableIndex(index, true, true);
	}
}

/**
Selects the desired ID in the table and displays the appropriate data
in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheetL.getRowCount();
	StateMod_DelayTable dt = null;
	for (int i = 0; i < rows; i++) {
		dt = (StateMod_DelayTable)__worksheetL.getRowData(i);
		if (dt.getID().trim().equals(id.trim())) {
			selectLeftTableIndex(i, true, true);
			return;
		}
	}
}

/**
Selects a row from the left table, scrolls to that row, and displays its
delay table information in the right table.
@param index the index of the row to select.  -1 if none were selected.
@param try_to_save Indicates whether the current contents should be saved before
selecting the new row.  A value of false should be passed only at startup or
when checkInput() has failed for a selection, in which case the display will
revert to the failed data rather than a new selection.
@param process_selection If true, then the display will be updated based on the
row that is selected - this should be the case most of the time.  If false, then
the previous contents are retained - this should be the case if checkInput()
detects an error, in which case we want the previous (and erroneous)
user-supplied to be shown because they need to correct the data.
*/
private void selectLeftTableIndex(int index, boolean tryToSave,
boolean processSelection) {
	if (index == -1) { 
		return;
	}
	if (index >= __worksheetL.getRowCount()) {
		return;
	}

	__worksheetL.scrollToRow(index);
	__worksheetL.selectRow(index);
	processLeftTableSelection(index, tryToSave);
}

/**
Sets up the GUI.
@param index Data item to display.
*/
private void setupGUI( int index ) {
	String routine = "StateMod_DelayTable_JFrame";

	addWindowListener(this);
	
	// AWT portion
	JPanel p1 = new JPanel();	// selection list and grid
	JPanel p2 = new JPanel();	// search widgets
	JPanel pmain = new JPanel();	// everything but close and help buttons

	__searchID = new JTextField(10);
	__findNextDelay = new JButton("Find Next");

	PropList p = new PropList("StateMod_DelayTable_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	boolean percent = true;
	if (__dataset.getInterv() == -100 || __dataset.getInterv() < -1) {
		percent = false;
	}

	int[] widthsR = null;
	JScrollWorksheet jswR = null;
	try {
		StateMod_DelayTable_TableModel tmd = new
			StateMod_DelayTable_TableModel(new Vector(),
			__monthly_data, __editable, percent);
		tmd.setSubDelays(new Vector());
		StateMod_DelayTable_CellRenderer crd = new
			StateMod_DelayTable_CellRenderer(tmd);
		
		jswR = new JScrollWorksheet(crd, tmd, p);
		__worksheetR = jswR.getJWorksheet();

		// remove the ID column
		__worksheetR.removeColumn(0);
		widthsR = crd.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jswR = new JScrollWorksheet(0, 0, p);
		__worksheetR = jswR.getJWorksheet();
	}
	__worksheetR.setPreferredScrollableViewportSize(null);

	// Assume all have the same units so pass in the first one...
	__worksheetR.setHourglassJFrame(this);
	__worksheetR.addMouseListener(this);	
	__worksheetR.addKeyListener(this);

	__graphDelayJButton = new JButton("Graph");

	if (__delaysVector.size() == 0) {
		__graphDelayJButton.setEnabled(false);
	}
	
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__addReturn = new JButton(__BUTTON_ADD_RETURN);
	__deleteReturn = new JButton(__BUTTON_DELETE_RETURN);
	__deleteReturn.setEnabled(false);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);

	GridBagLayout gb = new GridBagLayout();
	p1.setLayout(gb);
	p2.setLayout(gb);
	pmain.setLayout(gb);

	int y;
	
	int[] widthsL = null;
	JScrollWorksheet jswL = null;
	try {
		StateMod_DelayTable_TableModel tmd = new
			StateMod_DelayTable_TableModel(__delaysVector,
			__monthly_data, __editable, percent);
		StateMod_DelayTable_CellRenderer crd = new
			StateMod_DelayTable_CellRenderer(tmd);

		jswL = new JScrollWorksheet(crd, tmd, p);
		__worksheetL = jswL.getJWorksheet();

		// remove all the columns but the ID column.
		__worksheetL.removeColumn(1);
		__worksheetL.removeColumn(2);
		widthsL = crd.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jswL = new JScrollWorksheet(0, 0, p);
		__worksheetL = jswL.getJWorksheet();
	}
	__worksheetL.setPreferredScrollableViewportSize(null);
	__worksheetR.setPreferredScrollableViewportSize(null);
	__worksheetL.setHourglassJFrame(this);
	__worksheetL.addMouseListener(this);
	__worksheetL.addKeyListener(this);
	
	JGUIUtil.addComponent(pmain, jswL,
		0, 0, 2, 12, .2, 1, 
		10, 10, 1, 10,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(pmain, jswR,
		5, 1, 18, 24, 1, 1, 
		10, 10, 10, 10,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 1, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__statusJTextField = new JTextField(5);
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __statusJTextField,
		7, 1, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// close and help buttons
	JPanel pfinal = new JPanel();
	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	pfinal.setLayout(fl);
	if (__editable) {
		pfinal.add(__addReturn);
		pfinal.add(__deleteReturn);	
	}
	pfinal.add(applyJButton);
	pfinal.add(cancelJButton);		
	pfinal.add(__closeJButton);
	pfinal.add(__graphDelayJButton);
//	pfinal.add(__helpJButton);

	JGUIUtil.addComponent(bottomJPanel, pfinal,
		0, 0, 8, 1, 1, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__helpJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	__graphDelayJButton.addActionListener(this);
	__addReturn.addActionListener(this);
	__deleteReturn.addActionListener(this);
	cancelJButton.addActionListener(this);
	applyJButton.addActionListener(this);		

	// add search areas
	y=0;
	JPanel searchPanel = new JPanel();
	searchPanel.setLayout(gb);
	searchPanel.setBorder(BorderFactory.createTitledBorder(
		"Search above list for:     "));
	JGUIUtil.addComponent(searchPanel, new JLabel("ID"),
		0, y, 1, 1, 0, 0, 5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(searchPanel, __searchID,
		1, y, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
	__searchID.addActionListener(this);
	y++;
	JGUIUtil.addComponent(searchPanel, __findNextDelay,
		0, y, 4, 1, 0, 0, 10, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__findNextDelay.addActionListener(this);
	JGUIUtil.addComponent(pmain, searchPanel, 0, 
		GridBagConstraints.RELATIVE, 1, 1, 0, 0, 
		5, 10, 20, 10,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);

	getContentPane().add("Center", pmain);
	getContentPane().add("South", bottomJPanel);

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen( __window_type, this);
	}
	pack();
	setSize(530, 400);
	JGUIUtil.center(this);
	setVisible(true);

	if (widthsR != null) {
		__worksheetR.setColumnWidths(widthsR);
	}
	if (widthsL != null) {
		__worksheetL.setColumnWidths(widthsL);
	}

	selectLeftTableIndex(index, false, true);
}

/**
Sorts a Vector of objects.
@param data the Vector to be a sorted.
@return a new Vector of the objects in the other Vector, sorted.
*/
public static Vector sortVector(Vector data) {
	if (data == null) {
		return new Vector();
	}
	int size = data.size();
	if (size == 0) {
		return new Vector();
	}
	if (size == 1) {
		Vector v = new Vector();
		v.add(data.elementAt(0));
		return v;
	}

	Vector v = new Vector();
	for (int i = 0; i < size; i++) {
		v.add(data.elementAt(i));
	}

	Collections.sort(v);
	return v;
}

/**
Updates the right table based on a new selection from the left table, 
or if rows were added or deleted to the right table.
@param dt the StateMod_DelayTable object selected in the left table.
*/
private void updateRightTable(StateMod_DelayTable dt) {
	Vector v = dt.getRet_val();
	Vector v2 = new Vector();
	for (int i = 0; i < v.size(); i++) {
		v2.add(new Double(((Double)v.elementAt(i)).doubleValue()));
	}

	((StateMod_DelayTable_TableModel)__worksheetR.getModel())
		.setSubDelays(v2);
	((StateMod_DelayTable_TableModel)__worksheetR.getModel())
		.fireTableDataChanged();		
	((StateMod_DelayTable_TableModel)__worksheetR.getModel())
		.fireTableDataChanged();
	__deleteReturn.setEnabled(false);
}

/**
Responds to window activated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowActivated(WindowEvent e) {}

/**
Responds to window closed events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowClosed(WindowEvent e) {}

/**
Responds to window closing events -- closes the window reference.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	closeWindow();
}

/**
Responds to window deactivated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to window iconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {}

/**
Responds to window opened events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpened(WindowEvent e) {}

/**
Responds to window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e) {}

}
