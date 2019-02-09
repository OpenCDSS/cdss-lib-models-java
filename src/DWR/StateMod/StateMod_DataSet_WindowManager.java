// StateMod_DataSet_WindowManager - class to manage data set windows

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
import java.util.List;

import javax.swing.JFrame;

import cdss.domain.hydrology.network.HydrologyNode;

import DWR.StateCU.StateCU_DataSet;
import DWR.StateCU.StateCU_Location_JFrame;
import RTi.GIS.GeoView.GeoProjection;
import RTi.GIS.GeoView.GeoRecord;
import RTi.GIS.GeoView.GeoViewAnnotationRenderer;
import RTi.GIS.GeoView.GeoViewJComponent;
import RTi.GIS.GeoView.GeoViewJPanel;
import RTi.GIS.GeoView.HasGeoRecord;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingArea;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
<p>
The StateMod_DataSet_WindowManager class opens/manages/closes display windows
for StateMod data set component data.  Currently, only the main windows
(e.g., Diversion, Reservoir, but not the secondary windows) are managed and
only one of each main window is allowed to be open at a time.  In the future,
secondary windows may also be managed more closely but currently more than one
copy of secondary windows can be opened at the same time.
</p>
<p>
An instance of the window manager also helps with window coordination, such as initiating a request
to display (annotate) a data object on the map or network.
</p>
*/
public class StateMod_DataSet_WindowManager
implements WindowListener, GeoViewAnnotationRenderer, StateMod_Network_AnnotationRenderer
{

/**
Window status settings.  See setWindowOpen(), etc.
*/
private final int 
	CLOSED = 0,
	OPEN = 1;
	// INVISIBLE - might be needed if we decide to not fully destroy
	// windows - that is why an integer is tracked (not a boolean)

/**
Windows numbers, managed by the StateMod_GUI - list in the order of the data
set components, although there is not a one to one correspondence.
Note that the network editor is not managed by this component and must be opened/closed separately.
*/
public static final int 
	WINDOW_MAIN = 0, // Main interface - currently used to supply some
						// listeners for closing windows opened by displayWindow().
	WINDOW_CONTROL = 1, // Control 
	WINDOW_OUTPUT_CONTROL = 2, // Control 
	WINDOW_RESPONSE	= 3, // Response file
	WINDOW_STREAMGAGE = 4, // Stream gage stations
	WINDOW_DELAY_TABLE_MONTHLY = 5, // Delay tables (monthly)
	WINDOW_DELAY_TABLE_DAILY = 6, // Delay tables (daily)
	WINDOW_DIVERSION = 7, // Diversions
	WINDOW_PRECIPITATION = 8, // Precipitation time series
	WINDOW_EVAPORATION = 9, // Evaporation time series
	WINDOW_RESERVOIR = 10, // Reservoirs
	WINDOW_INSTREAM = 11, // Instream flows
	WINDOW_WELL = 12, // Wells
	WINDOW_PLAN = 13, // Plans
	WINDOW_STREAMESTIMATE = 14, // Stream estimate stations
	WINDOW_RIVER_NETWORK = 15, // River network
	WINDOW_OPERATIONAL_RIGHT = 16, // Operational rights
	// The following are actions that modify or view data...
	WINDOW_ADD_NODE = 17, // Add node
	WINDOW_DELETE_NODE = 18, // Delete node
	WINDOW_DATASET_SUMMARY = 19, // Data set summary
	WINDOW_RUN_REPORT = 20, // Run Statemod -report
	WINDOW_GRAPHING_TOOL = 21, // Graphing tool?
	WINDOW_RUN_DELPLT = 22, // Run delplt
	WINDOW_QUERY_TOOL = 23, // Query tool
	WINDOW_CONSUMPTIVE_USE = 24; // Consumptive use (StateCU structure)

/**
The number of windows handled by the methods in this class (one more than the last value above).
*/
private final int __NUM_WINDOWS = 25;

/**
Array to keep track of window status (OPEN or CLOSED)
*/
private int __windowStatus[];

/**
Array of all the windows that are open.
*/
private JFrame __windows[];

/**
StateMod data set for which windows are being managed.
*/
private StateMod_DataSet __dataset = null;

/**
StateCU data set for which windows are being managed (to handle structure file).
*/
private StateCU_DataSet __datasetStateCU = null;

/**
Map panel, needed for interaction between editor windows and the map.
*/
private GeoViewJPanel __mapPanel = null;

/**
Network editor window, needed for interaction between editor windows and the network.
*/
private StateMod_Network_JFrame __networkEditor = null;

/**
Constructor.
*/
public StateMod_DataSet_WindowManager ()
{	this ( null, null );
}

/**
Constructor.
*/
public StateMod_DataSet_WindowManager ( StateMod_DataSet dataset )
{
	this ( dataset, null );
}

/**
Constructor.
*/
public StateMod_DataSet_WindowManager ( StateMod_DataSet dataset, StateCU_DataSet datasetStateCU )
{	__dataset = dataset;
	__datasetStateCU = datasetStateCU;
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
Closes all windows, except the main window.
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
	closeWindow(WINDOW_CONSUMPTIVE_USE);
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
	Message.printStatus ( 2, "closeWindow", "Window closed: " + win_type );
}

/**
Format a summary report about the data set and display in a report window.
@param dataset StateMod_DataSet to display.
*/
private JFrame displayDataSetSummary ( StateMod_DataSet dataset )
{	if ( dataset == null ) {
		// Should not happen because menu should be disabled if no data set...
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

// TODO - how to deal with printStatus and other calls that need the
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
		Message.printStatus ( 2, "displayWindow", "Window displayed (already open): " + window_type );
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
		win = new StateMod_Control_JFrame( __dataset, this, true );
		setWindowOpen ( WINDOW_CONTROL, win );
	}
	else if ( window_type == WINDOW_RESPONSE ) {
		// printStatus("Initializing response window.", WAIT);
		win = new StateMod_Response_JFrame (__dataset, this);
		setWindowOpen(WINDOW_RESPONSE, win);
	}
	else if ( window_type == WINDOW_CONSUMPTIVE_USE ) {
		//printStatus("Initializing consumptive use edit window.", WAIT);
		setWindowOpen ( WINDOW_CONSUMPTIVE_USE, win );
		// TODO SAM 2011-06-22 For now disable editing
		win = new StateCU_Location_JFrame( false, __datasetStateCU, this, false );
	}
	else if ( window_type == WINDOW_STREAMGAGE ) {
		//printStatus("Initializing river station window.", WAIT);
		setWindowOpen ( WINDOW_STREAMGAGE, win );
		win = new StateMod_StreamGage_JFrame(__dataset, this, true);
	}
	else if ( window_type == WINDOW_DELAY_TABLE_MONTHLY ) {
		//printStatus("Initializing delay edit window.", WAIT);
		win = new StateMod_DelayTable_JFrame( __dataset, this, true, true );
		setWindowOpen ( WINDOW_DELAY_TABLE_MONTHLY, win );
	}
	else if ( window_type == WINDOW_DELAY_TABLE_DAILY ) {
		//printStatus("Initializing delay edit window.", WAIT);
		win = new StateMod_DelayTable_JFrame( __dataset, this, false, true );
		setWindowOpen ( WINDOW_DELAY_TABLE_DAILY, win );
	}
	else if ( window_type == WINDOW_DIVERSION ) {
		//printStatus("Initializing diversions edit window.", WAIT);
		setWindowOpen ( WINDOW_DIVERSION, win );
		win = new StateMod_Diversion_JFrame( __dataset, this, true );
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
		try {
			@SuppressWarnings("unchecked")
			List<TS> tslist = (List<TS>)(__dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY)).getData();
			win = new TSViewJFrame ( tslist, props );
			setWindowOpen ( WINDOW_PRECIPITATION, win );
			// Use a window listener to know when the window closes
			// so that it can be managed like all the other data windows.
			win.addWindowListener ( this );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error displaying precipitation data." );
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
		try {
			@SuppressWarnings("unchecked")
			List<TS> tslist = (List<TS>)(__dataset.getComponentForComponentType(
				StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY)).getData();
			win = new TSViewJFrame ( tslist, props );
			setWindowOpen ( WINDOW_EVAPORATION, win );
			// Use a window listener to know when the window closes
			// so that it can be managed like all the other data windows.
			win.addWindowListener ( this );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error displaying precipitation data." );
			Message.printWarning ( 2, routine, e );
		}
	}
	else if ( window_type == WINDOW_RESERVOIR ) {
		//printStatus("Initializing reservoirs edit window.", WAIT);
		win = new StateMod_Reservoir_JFrame( __dataset, this, true );
		setWindowOpen ( WINDOW_RESERVOIR, win );
	}
	else if ( window_type == WINDOW_INSTREAM ) {
		//printStatus("Initializing instream flows edit window.",WAIT);
		setWindowOpen ( WINDOW_INSTREAM, win );
		win = new StateMod_InstreamFlow_JFrame( __dataset, this, true );
	}
	else if ( window_type == WINDOW_WELL ) {
		//printStatus("Initializing well edit window.", WAIT);
		win = new StateMod_Well_JFrame( __dataset, this, true );
		setWindowOpen ( WINDOW_WELL, win );
	}
	else if ( window_type == WINDOW_PLAN ) {
		//printStatus("Initializing plan edit window.", WAIT);
		win = new StateMod_Plan_JFrame( __dataset, this, true );
		setWindowOpen ( WINDOW_PLAN, win );
	}
	else if ( window_type == WINDOW_STREAMESTIMATE ) {
		//printStatus("Initializing baseflows edit window.", WAIT);
		win = new StateMod_StreamEstimate_JFrame(__dataset, this, true);
		setWindowOpen ( WINDOW_STREAMESTIMATE, win );
	}
	else if ( window_type == WINDOW_RIVER_NETWORK ) {
		//printStatus("Initializing river network edit window.", WAIT);
		win = new StateMod_RiverNetworkNode_JFrame(__dataset, this, true);
		setWindowOpen ( WINDOW_RIVER_NETWORK, win );
	}
	else if ( window_type == WINDOW_OPERATIONAL_RIGHT ) {
		//printStatus("Initializing operational rights window.",WAIT);
		win = new StateMod_OperationalRight_JFrame( __dataset, this, true );
		setWindowOpen ( WINDOW_OPERATIONAL_RIGHT, win );
	}
	else if ( window_type == WINDOW_QUERY_TOOL ) {
		win = new StateMod_QueryTool_JFrame( __dataset, this );
		setWindowOpen ( WINDOW_QUERY_TOOL, win );
	}
	else {
		Message.printWarning(2, routine, "Unable to display specified window type: " + window_type);
	}

	//JGUIUtil.setWaitCursor(this, false);
	//printStatus("Ready", READY);

	// Return the JFrame so that it is easy for other code to do subsequent calls...

	return win;
}

/**
Return the StateMod dataset being managed.
*/
private StateMod_DataSet getDataSet ()
{
	return __dataset;
}

/**
Return the map panel used for StateMod.
*/
private GeoViewJPanel getMapPanel ()
{
	return __mapPanel;
}

/**
Return the network editor used for StateMod.
*/
public StateMod_Network_JFrame getNetworkEditor ()
{
	return __networkEditor;
}

/**
Returns the window at the specified position.
@param win_index the position of the window (should be one of the public fields above).
@return the window at the specified position.
*/
public JFrame getWindow(int win_index) {
	return __windows[win_index];
}

/**
Returns the status of the window at the specified position.
@param win_index the position of the window (should be one of the public fields above).
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
window to the front (but it is not automatically moved to the front by this method).
*/
public JFrame refreshWindow ( int win_type, boolean always_open )
{	// Check to see if the window was opened...
	int status = getWindowStatus ( win_type );
	JFrame window = getWindow ( win_type );
	// First close the window...
	closeWindow ( win_type );
	// Now open if requested or if it was previously open...
	if ( (status == OPEN) ||
		// Window was opened before so open again...
		always_open ) {
		// Window was not opened before but want to open it now...
		window = displayWindow ( win_type );
	}
	return window;
}

/**
Render an object as an annotation on the GeoView map.
@param geoviewPanel the map object
@param objectToRender the object to render as an annotation on the map
@param label the string that is used to label the annotation on the map
*/
public void renderGeoViewAnnotation ( GeoViewJComponent geoview, Object objectToRender, String label )
{
	GeoRecord geoRecord = null;
	GRDrawingArea da = geoview.getDrawingArea();
	GRDrawingAreaUtil.setColor(da, GRColor.black );
	GRDrawingAreaUtil.setFont(da, "Helvetica", "Bold", 14.0 );
	// Make the symbol size relatively large so it is visible...
	// The pushpin is always drawn with the pin point on the point so center the text is OK
	// May need to project on the fly depending on the original data
	if ( objectToRender instanceof StateMod_OperationalRight ) {
		// Draw source and destination as separate symbols
		StateMod_OperationalRight opr = (StateMod_OperationalRight)objectToRender;
		StateMod_DataSet dataset = getDataSet();
		StateMod_Data smdata = opr.lookupDestinationDataObject(dataset);
		GRPoint pointDest = null;
		GRPoint pointSource1 = null;
		GRPoint pointSource2 = null;
		String labelDest = "";
		String labelSource1 = "";
		String labelSource2 = "";
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				pointDest = (GRPoint)geoRecord.getShape();
				GeoProjection layerProjection = geoRecord.getLayer().getProjection();
				GeoProjection geoviewProjection = geoview.getProjection();
				boolean doProject = GeoProjection.needToProject ( layerProjection, geoview.getProjection() );
				if ( doProject ) {
					pointDest = (GRPoint)GeoProjection.projectShape( layerProjection, geoviewProjection, pointDest, false );
				}
				labelDest = label + "\n(Dest: " + smdata.getID() + " - " + smdata.getName() + ")";
			}
		}
		smdata = opr.lookupSource1DataObject(dataset);
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				pointSource1 = (GRPoint)geoRecord.getShape();
				GeoProjection layerProjection = geoRecord.getLayer().getProjection();
				GeoProjection geoviewProjection = geoview.getProjection();
				boolean doProject = GeoProjection.needToProject ( layerProjection, geoview.getProjection() );
				if ( doProject ) {
					pointSource1 = (GRPoint)GeoProjection.projectShape( layerProjection, geoviewProjection, pointSource1, false );
				}
				labelSource1 = label + "\n(Source1: " + smdata.getID() + " - " + smdata.getName() + ")";
			}
		}
		smdata = opr.lookupSource2DataObject(dataset);
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				pointSource2 = (GRPoint)geoRecord.getShape();
				GeoProjection layerProjection = geoRecord.getLayer().getProjection();
				GeoProjection geoviewProjection = geoview.getProjection();
				boolean doProject = GeoProjection.needToProject ( layerProjection, geoview.getProjection() );
				if ( doProject ) {
					pointSource2 = (GRPoint)GeoProjection.projectShape( layerProjection, geoviewProjection, pointSource2, false );
				}
				labelSource2 = label + "\n(Source2: " + smdata.getID() + " - " + smdata.getName() + ")";
			}
		}
		// Draw connecting lines first (under the symbols and text)...
		if ( (pointDest != null) && (pointSource1 != null) ) {
			GRDrawingAreaUtil.setLineWidth(da, 2);
			GRDrawingAreaUtil.drawLine(da, pointDest.x, pointDest.y, pointSource1.x, pointSource1.y);
		}
		if ( (pointDest != null) && (pointSource2 != null) ) {
			GRDrawingAreaUtil.setLineWidth(da, 2);
			GRDrawingAreaUtil.drawLine(da, pointDest.x, pointDest.y, pointSource2.x, pointSource2.y);
		}
		// Now draw all the symbols and text
		if ( pointDest != null ) {
			GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_PUSHPIN_VERTICAL, pointDest.getX(), pointDest.getY(), 32.0, 0, 0 );
			GRDrawingAreaUtil.drawText(da, labelDest, pointDest.getX(), pointDest.getY(), 0.0, GRText.CENTER_X|GRText.TOP );
		}
		if ( pointSource1 != null ) {
			GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_PUSHPIN_VERTICAL, pointSource1.getX(), pointSource1.getY(), 32.0, 0, 0 );
			GRDrawingAreaUtil.drawText(da, labelSource1, pointSource1.getX(), pointSource1.getY(), 0.0, GRText.CENTER_X|GRText.TOP );
		}
		if ( pointSource2 != null ) {
			GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_PUSHPIN_VERTICAL, pointSource2.getX(), pointSource2.getY(), 32.0, 0, 0 );
			GRDrawingAreaUtil.drawText(da, labelSource2, pointSource2.getX(), pointSource2.getY(), 0.0, GRText.CENTER_X|GRText.TOP );
		}
	}
	else if ( objectToRender instanceof StateMod_InstreamFlow ) {
		// Draw upstream and downstream as separate symbols
		StateMod_InstreamFlow ifs = (StateMod_InstreamFlow)objectToRender;
		StateMod_DataSet dataset = getDataSet();
		StateMod_Data smdata = ifs;
		GRPoint pointUpstream = null;
		GRPoint pointDownstream = null;
		String labelUpstream = "";
		String labelDownstream = "";
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				pointUpstream = (GRPoint)geoRecord.getShape();
				GeoProjection layerProjection = geoRecord.getLayer().getProjection();
				GeoProjection geoviewProjection = geoview.getProjection();
				boolean doProject = GeoProjection.needToProject ( layerProjection, geoview.getProjection() );
				if ( doProject ) {
					pointUpstream = (GRPoint)GeoProjection.projectShape( layerProjection, geoviewProjection, pointUpstream, false );
				}
				labelUpstream = label + "\n(Up: " + smdata.getID() + " - " + smdata.getName() + ")";
			}
		}
		smdata = ifs.lookupDownstreamDataObject(dataset);
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				pointDownstream = (GRPoint)geoRecord.getShape();
				GeoProjection layerProjection = geoRecord.getLayer().getProjection();
				GeoProjection geoviewProjection = geoview.getProjection();
				boolean doProject = GeoProjection.needToProject ( layerProjection, geoview.getProjection() );
				if ( doProject ) {
					pointDownstream = (GRPoint)GeoProjection.projectShape( layerProjection, geoviewProjection, pointDownstream, false );
				}
				labelDownstream = label + "\n(Down: " + smdata.getID() + " - " + smdata.getName() + ")";
			}
		}
		// Draw connecting lines first (under the symbols and text)...
		if ( (pointUpstream != null) && (pointDownstream != null) ) {
			GRDrawingAreaUtil.setLineWidth(da, 2);
			GRDrawingAreaUtil.drawLine(da, pointUpstream.x, pointUpstream.y, pointDownstream.x, pointDownstream.y);
		}
		// Now draw all the symbols and text
		if ( pointUpstream == pointDownstream ) {
			labelUpstream = label;
		}
		if ( pointUpstream != null ) {
			GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_PUSHPIN_VERTICAL, pointUpstream.getX(), pointUpstream.getY(), 32.0, 0, 0 );
			GRDrawingAreaUtil.drawText(da, labelUpstream, pointUpstream.getX(), pointUpstream.getY(), 0.0, GRText.CENTER_X|GRText.TOP );
		}
		if ( (pointDownstream != null) && (pointDownstream != pointUpstream) ) {
			GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_PUSHPIN_VERTICAL, pointDownstream.getX(), pointDownstream.getY(), 32.0, 0, 0 );
			GRDrawingAreaUtil.drawText(da, labelDownstream, pointDownstream.getX(), pointDownstream.getY(), 0.0, GRText.CENTER_X|GRText.TOP );
		}
	}
	else if ( objectToRender instanceof StateMod_Data ) {
		Message.printStatus(2, "", "Rendering \"" + label + "\" annotation on map." );
		StateMod_Data smdata = (StateMod_Data)objectToRender;
		GRPoint point = null;
		if ( smdata instanceof HasGeoRecord) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			geoRecord = hasGeoRecord.getGeoRecord();
			point = (GRPoint)geoRecord.getShape();
		}
		else {
			// Do not have geographic data (should not get here)...
			return;
		}
		GeoProjection layerProjection = geoRecord.getLayer().getProjection();
		GeoProjection geoviewProjection = geoview.getProjection();
		boolean doProject = GeoProjection.needToProject ( layerProjection, geoview.getProjection() );
		if ( doProject ) {
			point = (GRPoint)GeoProjection.projectShape( layerProjection, geoviewProjection, point, false );
		}
		GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_PUSHPIN_VERTICAL, point.getX(), point.getY(), 32.0, 0, 0 );
		GRDrawingAreaUtil.drawText(da, label, point.getX(), point.getY(), 0.0, GRText.CENTER_X|GRText.TOP );
	}
	else {
		// Do not know how to handle
		return;
	}
}

/**
Render an object as an annotation on the network editor.
@param network the StateMod network object being rendered
@param annotationData annotation data to render
*/
public void renderStateModNetworkAnnotation ( StateMod_Network_JComponent network,
	StateMod_Network_AnnotationData annotationData )
{
	//GRPoint point = null;
	String commonID = null;
	Object objectToRender = annotationData.getObject();
	String label = annotationData.getLabel();
	// Draw the annotated version on top...
	GRDrawingArea da = network.getDrawingArea();
	GRDrawingAreaUtil.setFont(da, "Helvetica", "Bold", 14.0 );
	// Use blue so that red can be used to highlight problems.  The color should allow black
	// symbols on top to be legible
	GRColor blue = new GRColor(0,102,255);
	GRDrawingAreaUtil.setColor(da,blue );
	if ( objectToRender instanceof StateMod_OperationalRight ) {
		// Draw source and destination as separate symbols
		StateMod_OperationalRight opr = (StateMod_OperationalRight)objectToRender;
		StateMod_OperationalRight_Metadata metadata =
			StateMod_OperationalRight_Metadata.getMetadata(opr.getItyopr());
		StateMod_DataSet dataset = getDataSet();
		HydrologyNode nodeDest = null;
		HydrologyNode nodeSource1 = null;
		HydrologyNode nodeSource2 = null;
		String labelDest = "";
		String labelSource1 = "";
		String labelSource2 = "";
		StateMod_Data smdataDest = opr.lookupDestinationDataObject(dataset);
		if ( smdataDest != null ) {
			nodeDest = network.findNode ( smdataDest.getID(),
				false, // Do not change the selection
				false ); // Do not zoom to the node
		}
		else {
			Message.printStatus(2,"","Unable to find StateMod destination data object for OPR \"" +
				opr.getID() + "\" destination \"" + opr.getCiopde() + "\"" );
		}
		StateMod_Data smdataSource1 = opr.lookupSource1DataObject(dataset);
		if ( smdataSource1 != null ) {
			nodeSource1 = network.findNode ( smdataSource1.getID(),
				false, // Do not change the selection
				false ); // Do not zoom to the node
		}
		StateMod_Data smdataSource2 = opr.lookupSource2DataObject(dataset);
		if ( smdataSource2 != null ) {
			nodeSource2 = network.findNode ( smdataSource2.getID(),
				false, // Do not change the selection
				false ); // Do not zoom to the node
		}
		int textPositionDest = 0;
		if ( nodeDest != null ) {
			// The symbol is drawn behind the normal network so make bigger
			double size = nodeDest.getSymbol().getSize()*2.0;
			labelDest = label + "\n(Dest: " + smdataDest.getID() + " - " + smdataDest.getName() + ")";
			//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, 0, 0);
			//GRDrawingAreaUtil.drawText(da, label, node.getX(), node.getY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
			String labelPosition = nodeDest.getTextPosition();
			if ( (labelPosition.indexOf("Above") >= 0) || (labelPosition.indexOf("Upper") >= 0) ) {
				textPositionDest = GRText.CENTER_X|GRText.BOTTOM;
			}
			else {
				textPositionDest = GRText.CENTER_X|GRText.TOP;
			}
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_FCIR, nodeDest.getX(), nodeDest.getY(),
				size, labelDest, 0.0, textPositionDest, 0, 0);
		}
		if ( nodeSource1 != null ) {
			// The symbol is drawn behind the normal network so make bigger
			double size = nodeSource1.getSymbol().getSize()*2.0;
			labelSource1 = label + "\n(Source1: " + smdataSource1.getID() + " - " + smdataSource1.getName() + ")";
			//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, 0, 0);
			//GRDrawingAreaUtil.drawText(da, label, node.getX(), node.getY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
			// TODO SAM 2010-12-29 Need to optimize label positioning
			String labelPosition = nodeSource1.getTextPosition();
			int textPosition = 0;
			if ( (labelPosition.indexOf("Above") >= 0) || (labelPosition.indexOf("Upper") >= 0) ) {
				textPosition = GRText.CENTER_X|GRText.BOTTOM;
			}
			else {
				// Default text below symbol.
				textPosition = GRText.CENTER_X|GRText.TOP;
			}
			// Override to make sure it does not draw on the destination
			if ( nodeDest == nodeSource1 ) {
				if ( textPositionDest == (GRText.CENTER_X|GRText.TOP) ) {
					textPosition = GRText.CENTER_X|GRText.BOTTOM;
				}
				else {
					textPosition = GRText.CENTER_X|GRText.TOP;
				}
			}
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_FCIR, nodeSource1.getX(), nodeSource1.getY(),
				size, labelSource1, 0.0, textPosition, 0, 0);
		}
		if ( nodeSource2 != null ) {
			// The symbol is drawn behind the normal network so make bigger
			double size = nodeSource2.getSymbol().getSize()*2.0;
			labelSource2 = label + "\n(Source2: " + smdataSource2.getID() + " - " + smdataSource2.getName() + ")";
			//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, 0, 0);
			//GRDrawingAreaUtil.drawText(da, label, node.getX(), node.getY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
			String labelPosition = nodeSource2.getTextPosition();
			int textPosition = 0;
			if ( (labelPosition.indexOf("Above") >= 0) || (labelPosition.indexOf("Upper") >= 0) ) {
				textPosition = GRText.CENTER_X|GRText.BOTTOM;
			}
			else {
				textPosition = GRText.CENTER_X|GRText.TOP;
			}
			// Override to make sure it does not draw on the destination
			if ( nodeDest == nodeSource2 ) {
				if ( textPositionDest == (GRText.CENTER_X|GRText.TOP) ) {
					textPosition = GRText.CENTER_X|GRText.BOTTOM;
				}
				else {
					textPosition = GRText.CENTER_X|GRText.TOP;
				}
			}
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_FCIR, nodeSource2.getX(), nodeSource2.getY(),
				size, labelSource2, 0.0, textPosition, 0, 0);
		}
		// Draw the intervening structures specifically mentioned in the operational right
		if ( metadata.getRightTypeUsesInterveningStructuresWithoutLoss() ||
			metadata.getRightTypeUsesInterveningStructuresWithLoss(opr.getOprLimit())) {
			List<String> structureIDList = opr.getInterveningStructureIDs ();
			for ( String structureID : structureIDList ) {
				HydrologyNode nodeIntervening = network.findNode ( structureID,
						false, // Do not change the selection
						false ); // Do not zoom to the node
				if ( nodeIntervening != null ) {
					double size = nodeIntervening.getSymbol().getSize()*2.0;
					GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR,
						nodeIntervening.getX(), nodeIntervening.getY(), size, 0, 0);
				}
			}
		}
		// Draw the network lines connecting the source and destination
		GRDrawingAreaUtil.setColor(da,blue );
		if ( (nodeDest != null) && (nodeSource1 != null) ) {
			GRDrawingAreaUtil.setLineWidth(da, Math.max(nodeDest.getSymbol().getSize(),
				nodeSource1.getSymbol().getSize()) );
			List<HydrologyNode> connectingNodes = network.getNetwork().getNodeSequence(nodeDest,nodeSource1);
			if ( connectingNodes.size() > 0 ) {
				// Draw a line between each node
				double [] x = new double[connectingNodes.size()];
				double [] y = new double[x.length];
				int i = -1;
				for ( HydrologyNode node: connectingNodes ) {
					++i;
					x[i] = node.getX();
					y[i] = node.getY();
				}
				GRDrawingAreaUtil.drawPolyline(da, x.length, x, y);
			}
		}
		if ( (nodeDest != null) && (nodeSource2 != null) ) {
			GRDrawingAreaUtil.setLineWidth(da, Math.max(nodeDest.getSymbol().getSize(),
				nodeSource2.getSymbol().getSize()) );
			List<HydrologyNode> connectingNodes = network.getNetwork().getNodeSequence(nodeDest,nodeSource2);
			if ( connectingNodes.size() > 0 ) {
				// Draw a line between each node
				double [] x = new double[connectingNodes.size()];
				double [] y = new double[x.length];
				int i = -1;
				for ( HydrologyNode node: connectingNodes ) {
					++i;
					x[i] = node.getX();
					y[i] = node.getY();
				}
				GRDrawingAreaUtil.drawPolyline(da, x.length, x, y);
			}
		}
	}
	else if ( objectToRender instanceof StateMod_InstreamFlow ) {
		// Draw source and destination as separate symbols
		StateMod_InstreamFlow ifs = (StateMod_InstreamFlow)objectToRender;
		StateMod_DataSet dataset = getDataSet();
		HydrologyNode nodeUpstream = null;
		HydrologyNode nodeDownstream = null;
		String labelUpstream = "";
		String labelDownstream = "";
		StateMod_Data smdataUpstream = ifs;
		if ( smdataUpstream != null ) {
			nodeUpstream = network.findNode ( smdataUpstream.getID(),
				false, // Do not change the selection
				false ); // Do not zoom to the node
		}
		StateMod_Data smdataDownstream = ifs.lookupDownstreamDataObject(dataset);
		if ( smdataDownstream != null ) {
			nodeDownstream = network.findNode ( smdataDownstream.getID(),
				false, // Do not change the selection
				false ); // Do not zoom to the node
		}
		int textPositionUpstream = 0;
		if ( nodeUpstream != null ) {
			// The symbol is drawn behind the normal network so make bigger
			double size = nodeUpstream.getSymbol().getSize()*2.0;
			if ( nodeUpstream == nodeDownstream ) {
				labelUpstream = label;
			}
			else {
				labelUpstream = label + "\n(Up: " + smdataUpstream.getID() + " - " + smdataUpstream.getName() + ")";
			}
			//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, 0, 0);
			//GRDrawingAreaUtil.drawText(da, label, node.getX(), node.getY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
			if ( (nodeDownstream == null) || (nodeUpstream.getY() > nodeDownstream.getY())) {
				textPositionUpstream = GRText.CENTER_X|GRText.BOTTOM;
			}
			else {
				textPositionUpstream = GRText.CENTER_X|GRText.TOP;
			}
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_FCIR, nodeUpstream.getX(), nodeUpstream.getY(),
				size, labelUpstream, 0.0, textPositionUpstream, 0, 0);
		}
		if ( (nodeDownstream != null) && (nodeDownstream != nodeUpstream) ) {
			// The symbol is drawn behind the normal network so make bigger
			double size = nodeDownstream.getSymbol().getSize()*2.0;
			labelDownstream = label + "\n(Down: " + smdataDownstream.getID() + " - " + smdataDownstream.getName() + ")";
			//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, 0, 0);
			//GRDrawingAreaUtil.drawText(da, label, node.getX(), node.getY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
			// TODO SAM 2010-12-29 Need to optimize label positioning
			int textPositionDownstream = 0;
			if ( (nodeDownstream == null) || (nodeUpstream.getY() < nodeDownstream.getY()) ) {
				textPositionDownstream = GRText.CENTER_X|GRText.BOTTOM;
			}
			else {
				// Default text below symbol.
				textPositionDownstream = GRText.CENTER_X|GRText.TOP;
			}
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_FCIR, nodeDownstream.getX(), nodeDownstream.getY(),
				size, labelDownstream, 0.0, textPositionDownstream, 0, 0);
		}
		// Draw the network lines connecting the upstream and downstream
		GRDrawingAreaUtil.setColor(da,blue );
		if ( (nodeUpstream != null) && (nodeDownstream != null) ) {
			GRDrawingAreaUtil.setLineWidth(da, Math.max(nodeUpstream.getSymbol().getSize(),
				nodeDownstream.getSymbol().getSize()) );
			List<HydrologyNode> connectingNodes = network.getNetwork().getNodeSequence(nodeUpstream,nodeDownstream);
			if ( connectingNodes.size() > 0 ) {
				// Draw a line between each node
				double [] x = new double[connectingNodes.size()];
				double [] y = new double[x.length];
				int i = -1;
				for ( HydrologyNode node: connectingNodes ) {
					++i;
					x[i] = node.getX();
					y[i] = node.getY();
				}
				GRDrawingAreaUtil.drawPolyline(da, x.length, x, y);
			}
		}
	}
	else if ( objectToRender instanceof StateMod_Data ) {
		Message.printStatus(2, "", "Rendering \"" + label + "\" annotation on network." );
		StateMod_Data smdata = (StateMod_Data)objectToRender;
		commonID = smdata.getID();
		if ( commonID != null ) {
			// Find the node in the network and scroll to it.
			HydrologyNode node = network.findNode ( commonID,
				false, // Do not change the selection
				false ); // Do not zoom to the node
			// The symbol is drawn behind the normal network so make bigger
			double size = node.getSymbol().getSize()*2.0;
			//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, 0, 0);
			//GRDrawingAreaUtil.drawText(da, label, node.getX(), node.getY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_FCIR, node.getX(), node.getY(), size, label, 0.0, GRText.CENTER_X|GRText.BOTTOM, 0, 0);
		}
	}
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
Sets the data set for which this class is the window manager.
@param dataset the dataset for which to manage windows.
*/
public void setDataSet(StateCU_DataSet datasetStateCU) {
	__datasetStateCU = datasetStateCU;
}

/**
Set the map panel.
*/
public void setMapPanel ( GeoViewJPanel mapPanel )
{
	__mapPanel = mapPanel;
}

/**
Set the network editor.
*/
public void setNetworkEditor ( StateMod_Network_JFrame networkEditor )
{
	__networkEditor = networkEditor;
}

/**
Sets the window at the specified position.
@param win_index the position of the window (should be one of the public fields above).
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
	Message.printStatus ( 1, "setWindowOpen", "Window set open: " + win_type );
}

/**
Sets the window at the specified position to be either OPEN or CLOSED.
@param win_index the position of the window (should be one of the public fields above).
@param status the status of the window (OPEN or CLOSED)
*/
private void setWindowStatus ( int win_index, int status )
{	__windowStatus[win_index] = status;
}

/**
Show a StateMod data object on the map.  This method simply helps with the hand-off of information.
@param smData the StateMod data object to display on the map
@param label the label for the data object on the map
*/
public void showOnMap ( StateMod_Data smData, String label, GRLimits limits, GeoProjection projection )
{
	GeoViewJPanel geoviewPanel = getMapPanel();
	if ( geoviewPanel != null ) {
		geoviewPanel.addAnnotationRenderer ( this, smData, label, limits, projection, true );
	}
}

/**
Show a StateMod data object on the network.  This method simply helps with the hand-off of information.
@param smData the StateMod data object to display on the network
@param label the label for the data object on the network
*/
public void showOnNetwork ( StateMod_Data smData, String label, GRLimits limits )
{
	StateMod_Network_JFrame networkEditor = getNetworkEditor();
	if ( networkEditor != null ) {
		networkEditor.addAnnotationRenderer ( this, smData, label, limits, true );
	}
}

/**
Update the status of a window.  This is primarily used to update the status
of the main JFrame, with the title and menus being updated if the data set has been modified.
*/
public void updateWindowStatus ( int win_type )
{	if ( win_type == WINDOW_MAIN ) {
		StateMod_GUIUpdatable window = (StateMod_GUIUpdatable)getWindow ( win_type );
		window.updateWindowStatus ();
	}
}

/**
Responds to window activated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowActivated(WindowEvent evt) {}

// TODO SAM 2006-08-16 Perhaps all windows should be listened for here to simplify the individual
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

}
