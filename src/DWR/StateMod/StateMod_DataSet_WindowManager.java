//------------------------------------------------------------------------------
// StateMod_DataSet_WindowManager - class to manage data set windows
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2003-08-26	Steven A. Malers, RTi	Split relevant code out of
//					StateMod_GUIUtil and the main
//					StateModGUI_JFrame classes.
//					StateMod_DataSet.
// 2003-08-27	J. Thomas Sapienza, RTi	* Added isWindowOpen().
//					* Added setDataSet().
// 2003-08-27	SAM, RTi		Change so methods that open windows
//					return the JFrame to simplify later
//					use.
// 2003-08-29	SAM, RTi		Separate delay table groups into
//					monthly and daily.
// 2003-09-10	JTS, RTi		Enabled Response Window.
// 2003-09-11	SAM, RTi		Updated based on changes in the names
//					of the river stations.
// 2003-09-23	JTS, RTi		GUI constructors changed because of
//					the use of the new StateMod_GUIUtil
//					title-setting code.
// 2003-10-14	SAM, RTi		Add updateWindowStatus() to allow
//					edits in a window to result in a
//					change in the main GUI state.
// 2004-07-15	JTS, RTi		Added closeAllWindows().
// 2004-08-25	JTS, RTi		Data set summary uses 8 point fonts now.
// 2004-10-25	SAM, RTi		Add query tool window.
// 2006-01-18	JTS, RTi		Initial Evaporation view now a table.
// 2006-08-22	SAM, RTi		Add WINDOW_PLAN for plans.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.GRTS.TSViewJFrame;

import RTi.Util.GUI.ReportJFrame;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
The StateMod_DataSet_WindowManager class opens/manages/closes display windows
for StateMod data set component data.  Currently, only the main windows
(e.g., Diversion, Reservoir, but not the secondary windows) are managed and
only one of each main window is allowed to be open at a time.  In the future,
secondary windows may also be managed more closely but currently more than one
copy of secondary windows can be opened at the same time.
*/
public class StateMod_DataSet_WindowManager
implements WindowListener
{

/**
Window status settings.  See setWindowOpen(), etc.
*/
private final int 
	CLOSED = 	0,
	OPEN = 		1;
	// INVISIBLE - might be needed if we decide to not fully destroy
	// windows - that is why an integer is tracked (not a boolean)

/**
Windows numbers, managed by the StateMod_GUI - list in the order of the data
set components, although there is not a one to one correspondence.
*/
public static final int 
	WINDOW_MAIN = 			0,	// Main interface - this is
						// currently used to supply some
						// listeners for closing windows
						// opened by displayWindow().
	WINDOW_CONTROL = 		1,	// Control 
	WINDOW_OUTPUT_CONTROL = 	2,	// Control 
	WINDOW_RESPONSE	= 		3,	// Response file
	WINDOW_STREAMGAGE =		4,	// Stream gage stations
	WINDOW_DELAY_TABLE_MONTHLY =	5,	// Delay tables (monthly)
	WINDOW_DELAY_TABLE_DAILY = 	6,	// Delay tables (daily)
	WINDOW_DIVERSION = 		7,	// Diversions
	WINDOW_PRECIPITATION = 		8,	// Precipitation time series
	WINDOW_EVAPORATION = 		9,	// Evaporation time series
	WINDOW_RESERVOIR = 		10,	// Reservoirs
	WINDOW_INSTREAM = 		11,	// Instream flows
	WINDOW_WELL = 			12,	// Wells
	WINDOW_PLAN = 			13,	// Plans
	WINDOW_STREAMESTIMATE	= 	14,	// Stream estimate stations
	WINDOW_RIVER_NETWORK = 		15,	// River network
	WINDOW_OPERATIONAL_RIGHT = 	16,	// Operational rights
	// The following are actions that modify or view data...
	WINDOW_ADD_NODE = 		17,	// Add node
	WINDOW_DELETE_NODE = 		18,	// Delete node
	WINDOW_DATASET_SUMMARY =	19,	// Data set summary
	WINDOW_RUN_REPORT = 		20,	// Run Statemod -report
	WINDOW_GRAPHING_TOOL = 		21,	// Graphing tool?
	WINDOW_RUN_DELPLT = 		22,	// Run delplt
	WINDOW_QUERY_TOOL = 		23;	// Query tool

/**
The number of windows handled by the methods in this class.
*/
private final int __NUM_WINDOWS = 24;

/**
Array to keep track of window status (OPEN or CLOSED)
*/
private int __windowStatus[];

/**
Array of all the windows that are open.
*/
private JFrame __windows[];

/**
Data set for which windows are being managed.
*/
private StateMod_DataSet __dataset = null;

/**
Constructor.
*/
public StateMod_DataSet_WindowManager ()
{	this ( null );
}

/**
Constructor.
*/
public StateMod_DataSet_WindowManager ( StateMod_DataSet dataset )
{	__dataset = dataset;
	__windowStatus = new int[__NUM_WINDOWS];
	for (int i = 0; i < __NUM_WINDOWS; i++) {
		__windowStatus[i] = CLOSED;
	}
	
	__windows = new JFrame[__NUM_WINDOWS];
	for (int i = 0; i < __NUM_WINDOWS; i++) {
		__windows[i] = null;;
	}
}

/**
Closes all windows.
*/
public void closeAllWindows() {
	closeWindow(WINDOW_CONTROL);
	closeWindow(WINDOW_OUTPUT_CONTROL);
	closeWindow(WINDOW_RESPONSE);
	closeWindow(WINDOW_STREAMGAGE);
	closeWindow(WINDOW_DELAY_TABLE_MONTHLY);
	closeWindow(WINDOW_DELAY_TABLE_DAILY);
	closeWindow(WINDOW_DIVERSION);
	closeWindow(WINDOW_PRECIPITATION);
	closeWindow(WINDOW_EVAPORATION);
	closeWindow(WINDOW_RESERVOIR);
	closeWindow(WINDOW_INSTREAM);
	closeWindow(WINDOW_WELL);
	closeWindow(WINDOW_PLAN);
	closeWindow(WINDOW_STREAMESTIMATE);
	closeWindow(WINDOW_RIVER_NETWORK);
	closeWindow(WINDOW_OPERATIONAL_RIGHT);
	closeWindow(WINDOW_ADD_NODE);
	closeWindow(WINDOW_DELETE_NODE);
	closeWindow(WINDOW_DATASET_SUMMARY);
	closeWindow(WINDOW_RUN_REPORT);
	closeWindow(WINDOW_GRAPHING_TOOL);
	closeWindow(WINDOW_RUN_DELPLT);
	closeWindow(WINDOW_QUERY_TOOL);
}

/**
Close the window and set its reference to null.  If the window was never opened,
then no action is taken.  Use setWindowOpen() when opening a window to allow
the management of windows to occur.
@param win_type the number of the window.
*/
public void closeWindow ( int win_type )
{	if ( getWindowStatus(win_type) == CLOSED ) {
		// No need to do anything...
		return;
	}
	// Get the window...
	JFrame window = getWindow(win_type);
	// Now close the window...
	window.setVisible(false);
	window.dispose();
	// Set the "soft" data...
	setWindowStatus ( win_type, CLOSED );
	setWindow(win_type, null);
	Message.printStatus ( 1, "closeWindow", "Window closed: " + win_type );
}

/**
Format a summary report about the data set and display in a report window.
@param dataset StateMod_DataSet to display.
*/
private JFrame displayDataSetSummary ( StateMod_DataSet dataset )
{	if ( dataset == null ) {
		// Should not happen because menu should be disabled if no data
		// set...
		return null;
	}
	PropList props = new PropList("Data Set Summary");
	props.add("Title=" + dataset.getBaseName() + " - Data Set Summary");
	props.add("PrintSize=8");
	JFrame f = new ReportJFrame( dataset.getSummary(), props);
	setWindow ( WINDOW_DATASET_SUMMARY, f );
	// Because the window is a simple ReportJFrame, it will not know how
	// to set its "close" status in this window manager, so need to listen
	// for a close event here...
	f.addWindowListener ( this );
	props = null;
	return f;
}

/**
Show the indicated window type and allow editing.  If that window type is
already displayed, bring it to the front.  Otherwise create the window.
@param window_type See WINDOW_*.
@return the window that is displayed.
*/
public JFrame displayWindow ( int window_type )
{	return displayWindow ( window_type, true );
}

// REVISIT - how to deal with printStatus and other calls that need the
// StateModGUI_JFrame - for now comment out the calls.
/**
Show the indicated window type.  If that window type is already displayed,
bring it to the front.  Otherwise create the window.
@param window_type See WINDOW_*.
@param editable Indicates if the data in the window should be editable.
@return the window that is displayed.
*/
public JFrame displayWindow ( int window_type, boolean editable )
{	String routine = "StateMod_GUIUtil.displayWindow";
	//JGUIUtil.setWaitCursor(this, true);
	JFrame win = null;

	if ( getWindowStatus(window_type) == OPEN ) {
		// Window_type already opened so make sure that it is not
		// minimized and move to the front...
		win = getWindow(window_type);
		if ( win.getState() == Frame.ICONIFIED ) {
			win.setState(Frame.NORMAL);
		}
		// Now make sure it is in the front...
		win.toFront();
		Message.printStatus ( 2, "displayWindow",
		"Window displayed (already open): " + window_type );
	}

	// Create window for the appropriate window.
	// List windows in the order of the menus (generally)...

	else if ( window_type == WINDOW_DATASET_SUMMARY ) {
		//printStatus("Initializing data set summary window.", WAIT);
		win = displayDataSetSummary ( __dataset );
		setWindowOpen ( WINDOW_DATASET_SUMMARY, win );
	}
	else if ( window_type == WINDOW_CONTROL ) {
		//printStatus("Initializing control edit window.", WAIT);
		win = new StateMod_Control_JFrame(
			__dataset, this, true );
		setWindowOpen ( WINDOW_CONTROL, win );
	}
	else if ( window_type == WINDOW_RESPONSE ) {
		// printStatus("Initializing response window.", WAIT);
		win = new StateMod_Response_JFrame (
			__dataset, this);
		setWindowOpen(WINDOW_RESPONSE, win);
	}
	else if ( window_type == WINDOW_STREAMGAGE ) {
		//printStatus("Initializing river station window.", WAIT);
		setWindowOpen ( WINDOW_STREAMGAGE, win );
		win = new StateMod_StreamGage_JFrame(
			__dataset, this, true);
	}
	else if ( window_type == WINDOW_DELAY_TABLE_MONTHLY ) {
		//printStatus("Initializing delay edit window.", WAIT);
		win = new StateMod_DelayTable_JFrame(
			__dataset, this, true, true );
		setWindowOpen ( WINDOW_DELAY_TABLE_MONTHLY, win );
	}
	else if ( window_type == WINDOW_DELAY_TABLE_DAILY ) {
		//printStatus("Initializing delay edit window.", WAIT);
		win = new StateMod_DelayTable_JFrame(
			__dataset, this, false, true );
		setWindowOpen ( WINDOW_DELAY_TABLE_DAILY, win );
	}
	else if ( window_type == WINDOW_DIVERSION ) {
		//printStatus("Initializing diversions edit window.", WAIT);
		setWindowOpen ( WINDOW_DIVERSION, win );
		win = new StateMod_Diversion_JFrame(
			__dataset, this, true );
	}
	else if ( window_type == WINDOW_PRECIPITATION ) {
		// Don't have a special window - just display all precipitation
		// time series as a graph.  This usually works because there are
		// not that many precipitation stations.  If it becomes a
		// problem, add additional logic.
		//printStatus("Initializing precipitation window.", WAIT);
		PropList props = new PropList ( "Precipitation" );
		props.set ( "InitialView", "Graph" );
		props.set ( "GraphType", "Bar" );
		props.set ( "TotalWidth", "600" );
		props.set ( "TotalHeight", "400" );
		props.set ( "Title", "Precipitation (Monthly)" );
		props.set ( "DisplayFont", "Courier" );
		props.set ( "DisplaySize", "11" );
		props.set ( "PrintFont", "Courier" );
		props.set ( "PrintSize", "7" );
		props.set ( "PageLength", "100" );
		try {	win = new TSViewJFrame (
			(Vector)(__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY)).
			getData(), props );
			setWindowOpen ( WINDOW_PRECIPITATION, win );
			// Use a window listener to know when the window closes
			// so that it can be managed like all the other data
			// windows.
			win.addWindowListener ( this );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error displaying precipitation data." );
			Message.printWarning ( 2, routine, e );
		}
	}
	else if ( window_type == WINDOW_EVAPORATION ) {
		// Don't have a special window - just display all evaporation
		// time series as a graph.  This usually works because there are
		// not that many precipitation stations.  If it becomes a
		// problem, add additional logic.
		//printStatus("Initializing evaporation window.", WAIT);
		PropList props = new PropList ( "Evaporation" );
		props.set ( "InitialView", "Table" );
		props.set ( "GraphType", "Bar" );
		props.set ( "TotalWidth", "600" );
		props.set ( "TotalHeight", "400" );
		props.set ( "Title", "Evaporation (Monthly)" );
		props.set ( "DisplayFont", "Courier" );
		props.set ( "DisplaySize", "11" );
		props.set ( "PrintFont", "Courier" );
		props.set ( "PrintSize", "7" );
		props.set ( "PageLength", "100" );
		try {	win = new TSViewJFrame (
			(Vector)(__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY)).
			getData(), props );
			setWindowOpen ( WINDOW_EVAPORATION, win );
			// Use a window listener to know when the window closes
			// so that it can be managed like all the other data
			// windows.
			win.addWindowListener ( this );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error displaying precipitation data." );
			Message.printWarning ( 2, routine, e );
		}
	}
	else if ( window_type == WINDOW_RESERVOIR ) {
		//printStatus("Initializing reservoirs edit window.", WAIT);
		win = new StateMod_Reservoir_JFrame(
			__dataset, this, true );
		setWindowOpen ( WINDOW_RESERVOIR, win );
	}
	else if ( window_type == WINDOW_INSTREAM ) {
		//printStatus("Initializing instream flows edit window.",WAIT);
		setWindowOpen ( WINDOW_INSTREAM, win );
		win = new StateMod_InstreamFlow_JFrame(
			__dataset, this, true );
	}
	else if ( window_type == WINDOW_WELL ) {
		//printStatus("Initializing well edit window.", WAIT);
		win = new StateMod_Well_JFrame(
			__dataset, this, true );
		setWindowOpen ( WINDOW_WELL, win );
	}
	else if ( window_type == WINDOW_PLAN ) {
		//printStatus("Initializing plan edit window.", WAIT);
		win = new StateMod_Plan_JFrame( __dataset, this, true );
		setWindowOpen ( WINDOW_PLAN, win );
	}
	else if ( window_type == WINDOW_STREAMESTIMATE ) {
		//printStatus("Initializing baseflows edit window.", WAIT);
		win = new StateMod_StreamEstimate_JFrame(
			__dataset, this, true);
		setWindowOpen ( WINDOW_STREAMESTIMATE, win );
	}
	else if ( window_type == WINDOW_RIVER_NETWORK ) {
		//printStatus("Initializing river network edit window.", WAIT);
		win = new StateMod_RiverNetworkNode_JFrame(
			__dataset, this, true);
		setWindowOpen ( WINDOW_RIVER_NETWORK, win );
	}
	else if ( window_type == WINDOW_OPERATIONAL_RIGHT ) {
		//printStatus("Initializing operational rights window.",WAIT);
		win = new StateMod_OperationalRight_JFrame(
			__dataset, this, true );
		setWindowOpen ( WINDOW_OPERATIONAL_RIGHT, win );
	}
	else if ( window_type == WINDOW_QUERY_TOOL ) {
		win = new StateMod_QueryTool_JFrame( __dataset, this );
		setWindowOpen ( WINDOW_QUERY_TOOL, win );
	}
	else {	Message.printWarning(2, routine, "Unable to display specified "
			+ "window type: " + window_type);
	}

	//JGUIUtil.setWaitCursor(this, false);
	//printStatus("Ready", READY);

	// Return the JFrame so that it is easy for other code to do subsequent
	// calls...

	return win;
}

/**
Returns the window at the specified position.
@param win_index the position of the window (should be one of the public fields
above).
@return the window at the specified position.
*/
public JFrame getWindow(int win_index) {
	return __windows[win_index];
}

/**
Returns the status of the window at the specified position.
@param win_index the position of the window (should be one of the public fields
above).
@return the status of the window at the specified position.
*/
public int getWindowStatus(int win_index) {
	return __windowStatus[win_index];
}

/**
Returns whether a window is currently open or not.
@return true if the window is open, false if not.
*/
public boolean isWindowOpen(int win_index) {
	if (__windowStatus[win_index] == OPEN) {
		return true;
	}
	return false;
}

/**
Refresh the contents of a window.  Currently this occurs by closing the window
and then opening it again.  In the future, individual windows may be made more
intelligent so that a full refresh of the contents is not necessary.  For now
the close/open sequence should work, and is the same approach taken in the
previous version of the GUI.
@param win_type The window type to refresh (see WINDOW_*).
@param always_open If true, the window will always be opened.  If false, no
action will occur if the window was not originally opened.
@return the JFrame that was refreshed.  This is used, for example, to move the
window to the front (but it is not automatically moved to the front by this
method).
*/
public JFrame refreshWindow ( int win_type, boolean always_open )
{	// Check to see if the window was opened...
	int status = getWindowStatus ( win_type );
	JFrame window = getWindow ( win_type );
	// First close the window...
	closeWindow ( win_type );
	// Now open if requested or if it was previously open...
	if (	(status == OPEN) ||
		// Window was opened before so open again...
		always_open ) {
		// Window was not opened before but want to open it now...
		window = displayWindow ( win_type );
	}
	return window;
}

/**
Selects the desired ID in the table and displays the appropriate data
in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID ( String id )
{
}

/**
Sets the data set for which this class is the window manager.
@param dataset the dataset for which to manage windows.
*/
public void setDataSet(StateMod_DataSet dataset) {
	__dataset = dataset;
}

/**
Sets the window at the specified position.
@param win_index the position of the window (should be one of the public fields
above).
@param window the window to set.
*/
public void setWindow(int win_index, JFrame window) {
	__windows[win_index] = window;
}

/**
Indicate that a window is opened, and provide the JFrame corresponding to the
window.  This method should be called to allow the StateMod GUI to track
windows (so that only one copy of a data set group window is open at a time).
@param win_type Window type (see WINDOW_*).
@param window The JFrame associated with the window.
*/
public void setWindowOpen(int win_type, JFrame window)
{	setWindow(win_type, window);
	setWindowStatus(win_type, OPEN);
	Message.printStatus ( 1, "setWindowOpen", "Window set open: " +
		win_type );
}

/**
Sets the window at the specified position to be either OPEN or CLOSED.
@param win_index the position of the window (should be one of the public fields
above).
@param status the status of the window (OPEN or CLOSED)
*/
private void setWindowStatus ( int win_index, int status )
{	__windowStatus[win_index] = status;
}

/**
Update the status of a window.  This is primarily used to update the status
of the main JFrame, with the title and menus being updated if the data set
has been modified.
*/
public void updateWindowStatus ( int win_type )
{	if ( win_type == WINDOW_MAIN ) {
		StateMod_GUIUpdatable window =
			(StateMod_GUIUpdatable)getWindow ( win_type );
		window.updateWindowStatus ();
	}
}

/**
Responds to window activated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowActivated(WindowEvent evt) {}

// REVISIT SAM 2006-08-16
// Perhaps all windows should be listened for here to simplify the individual
// window code.
/**
Responds to window closed events.  The following windows are handled because
they use generic components that cannot specifically set their close status:
<pre>
Data Set Summary
Precipitation Time Series
Evaporation Time Series
</pre>
@param e the WindowEvent that happened.
*/
public void windowClosed ( WindowEvent e )
{	Component c = e.getComponent();
	if ( c == getWindow ( WINDOW_DATASET_SUMMARY ) ) {
		setWindowStatus ( WINDOW_DATASET_SUMMARY, CLOSED );
		setWindow ( WINDOW_DATASET_SUMMARY, null );
	}
	else if ( c == getWindow ( WINDOW_PRECIPITATION ) ) {
		setWindowStatus ( WINDOW_PRECIPITATION, CLOSED );
		setWindow ( WINDOW_PRECIPITATION, null );
	}
	else if ( c == getWindow ( WINDOW_EVAPORATION ) ) {
		setWindowStatus ( WINDOW_EVAPORATION, CLOSED );
		setWindow ( WINDOW_EVAPORATION, null );
	}
}

/**
Responds to window closing events; closes the application.
@param evt the WindowEvent that happened.
*/
public void windowClosing(WindowEvent evt) {
}

/**
Responds to window deactivated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent evt) {}

/**
Responds to window deiconified events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent evt) {}

/**
Responds to window iconified events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowIconified(WindowEvent evt) {}

/**
Responds to window opened events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowOpened(WindowEvent evt) {}

} // End StateMod_DataSet_WindowManager
