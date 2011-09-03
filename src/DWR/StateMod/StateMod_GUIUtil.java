//------------------------------------------------------------------------------
// StateMod_GUIUtil - GUI-related utility functions
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-07	JTS, RTi		Added code for displaying graphs.
// 2003-07-17	JTS, RTi		Added nothingSelected() and 
//					somethingSelected()
// 2003-08-03	Steven A. Malers, RTi	* Constants that were in
//					  StateMod_Control are now in
//					  StateMod_DataSet.
//					* Add READ_START for process listener.
// 2003-08-13	SAM, RTi		* Change ProcessListeners to be more
//					  consistent with StateDMI command
//					  processing and verify that process
//					  listener is working.
//					* Remove map layers window.
//					* Handle basin summary like other
//					  windows.
//					* Remove diagnostics window from the
//					  list of managed windows - it manages
//					  itself.
//					* Clean up the names of the windows to
//					  be consistent with data set
//					  components.
// 2003-08-26	SAM, RTi		Split window manager code into
//					StateMod_DataSet_WindowManager and
//					remove from here.
// 2003-09-24	SAM, RTi		Overload setTitle() to take a JDialog.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.IO.DataSet;
import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.YearType;

/**
This class provides static data and methods for user interface methods associated with StateMod, mainly
the StateMod GUI.  This class is part of the StateMod package
(rather than StateModGUI) because all of the windows for displaying StateMod
data set components are in the StateMod package).
In the future, this class may be made non-static if it is necessary that a GUI
display more than one data set.
*/
public class StateMod_GUIUtil
{

protected static String _editorPreference = "NotePad";

/**
Settings for use in relaying data back to the calling application via ProcessListener calls.
*/
public final static int 
	STATUS_READ_START =	20,	// Start reading a data file
	STATUS_READ_COMPLETE =	22,	// End reading a data file
	STATUS_READ_GVP_START =	50,	// Start reading the GVP file
	STATUS_READ_GVP_END = 51;	// End reading the GVP file;

/**
Add filename filters to the file chooser for time series files.  A general ".stm" entry is added as well
as well rights.
@param fc File chooser.
@param timeInterval the TimeInterval for choices (TimeInterval.DAY, TimeInterval.MONTH,
or TimeInterval.UNKNOWN for all).
@param addRightFiles indicate whether water right files should be added.
*/
public static void addTimeSeriesFilenameFilters ( JFileChooser fc, int timeInterval, boolean addRightFiles )
{
    // Interleave the entries because TimeInterval.UKNOWN will want a complete list
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ddd", "StateMod Diversion Demands (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ddm", "StateMod Diversion Demands (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "dda", "StateMod Diversion Demands (Average Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ddo", "StateMod Diversion Demands Override (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ddy", "StateMod Diversions, Historical (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ddh", "StateMod Diversions, Historicial (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( addRightFiles ) {
        SimpleFileFilter ssf = new SimpleFileFilter( "ddr", "StateMod Diversion Rights");
        fc.addChoosableFileFilter(ssf);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "eva", "StateMod Evaporation (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ifd", "StateMod Instream Flow Demands (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ifa", "StateMod Instream Flow Demands (Average Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ifm", "StateMod Instream Flow Demands (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( addRightFiles ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ifr", "StateMod Instream Flow Rights");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "ddc", "StateMod/StateCU Irrigation Water Requirement (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "iwr", "StateMod/StateCU Irrigation Water Requirement (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "pre", "StateMod Precipitation (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "eoy", "StateMod Reservoir Storage (End of Day)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "eom", "StateMod Reservoir Storage (End of Month)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "tad", "StateMod Reservoir Min/Max Targets (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "tar", "StateMod Reservoir Min/Max Targets (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( addRightFiles ) {
        SimpleFileFilter sff = new SimpleFileFilter( "rer", "StateMod Reservoir Rights");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "riy", "StateMod Streamflow, Historical (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "rih", "StateMod Streamflow, Historicial (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "riy", "StateMod Streamflow, Natural (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "riy", "StateMod Streamflow, Natural (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    SimpleFileFilter stm_sff = new SimpleFileFilter( "stm", "StateMod Time Series");
    fc.addChoosableFileFilter(stm_sff);
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "wed", "StateMod Well Demands (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "wem", "StateMod Well Demands (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.DAY) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "wey", "StateMod Well Historical Pumping (Daily)");
        fc.addChoosableFileFilter(sff);
    }
    if ( (timeInterval == TimeInterval.MONTH) || (timeInterval == TimeInterval.UNKNOWN) ) {
        SimpleFileFilter sff = new SimpleFileFilter( "weh", "StateMod Well Historicial Pumping (Monthly)");
        fc.addChoosableFileFilter(sff);
    }
    if ( addRightFiles ) {
        SimpleFileFilter sff = new SimpleFileFilter( "wer", "StateMod Well Rights");
        fc.addChoosableFileFilter(sff);
    }
    // Select the "stm" filter as the most generic.
    fc.setFileFilter(stm_sff);
}

/**
Used to set a numeric value in a JTextField.  This method will check the value
and see if it is missing, and if so, will set the JTextField to "".  Otherwise
the text field will be filled with the value of the specified int.
@param i the int to check and possibly put in the text field.
@param textField the JTextField to put the value in.
*/
public static void checkAndSet(int i, JTextField textField) {
	if (StateMod_Util.isMissing(i)) {
		textField.setText("");
	}
	else {
		textField.setText("" + i);
	}
}

/**
Used to set a numeric value in a JTextField.  This method will check the value
and see if it is missing, and if so, will set the JTextField to "".  Otherwise
the text field will be filled with the value of the specified double.
@param d the double to check and possibly put in the text field.
@param textField the JTextField to put the value in.
*/
public static void checkAndSet(double d, JTextField textField) {
	if (StateMod_Util.isMissing(d)) {
		textField.setText("");
	}
	else {
		textField.setText("" + d);
	}
}

/**
Displays a graph for a time series.
@param ts the time series to graph.
@param title the title of the graph
@param dataset the dataset in which the ts data exists
*/
public static void displayGraphForTS (TS ts, String title, 
StateMod_DataSet dataset)
throws Exception
{
	List v = new Vector();
	v.add(ts);

	// add title to proplist
	PropList props = new PropList("displayGraphForTSProps");
	props.set("titlestring", title);

	displayGraphForTS(v, props, dataset);
}

/**
Displays a graph for a time series.
@param ts the time series to graph.
@param props props defining how the graph should be shown
@param dataset the dataset in which the ts data exists
*/
public static void displayGraphForTS ( TS ts, PropList props,
StateMod_DataSet dataset)
throws Exception
{
	List v = new Vector();
	v.add(ts);
	displayGraphForTS(v, props, dataset);
}

/**
Displays a graph for a time series.
@param tslist Vector of time series to graph
@param title the title of the graph
@param dataset the dataset in which the ts data exists
*/
public static void displayGraphForTS ( List tslist, String title,
StateMod_DataSet dataset)
throws Exception
{
	// add title to proplist
	PropList props = new PropList("displayGraphForTSProps");
	props.set("titlestring", title);
	displayGraphForTS(tslist, props, dataset);
}

/**
Draw a graph.
@param tslist Vector of TS to plot.
@param props Graph properties.
@param dataset the dataset in which the ts data exists
The properties may contain valid TSViewGraphGUI properies or
may not, in which case defaults will be used.
If some important properties are not set, they are set using the same PropList.
*/
public static void displayGraphForTS(List tslist, PropList props, StateMod_DataSet dataset)
{
	PropList proplist = null;
	if (props == null) {
		// Create a new one...
		proplist = new PropList("SMGUIApp");
	}
	else {	
		// Use what was passed in...
		proplist = props;
	}
	// Make sure some important properties are set...

	if (proplist.getValue("InitialView") == null) {
		proplist.set("InitialView", "Graph");
	}
	if (proplist.getValue("DisplayFont") == null) {
		proplist.set("DisplayFont", "Courier");
	}
	if (proplist.getValue("DisplaySize") == null) {
		proplist.set("DisplaySize", "11");
	}
	if (proplist.getValue("PrintFont") == null) {
		proplist.set("PrintFont", "Courier");
	}
	if (proplist.getValue("PrintSize") == null) {
		proplist.set("PrintSize", "7");
	}
	if (proplist.getValue("PageLength") == null) {
		proplist.set("PageLength", "100");
	}
	// Use titlestring now but Title may be passed in as property
	String title = props.getValue("Title");
	if ((title != null) && (proplist.getValue("titlestring") == null)) {
		proplist.set("TitleString", title);
	}

	// CalendarType: Wateryear, IrrigationYear, CalendarYear

	if (dataset.getCyrl() == YearType.CALENDAR) {
		proplist.set("CalendarType", "CalendarYear");
	}
	else if (dataset.getCyrl() == YearType.WATER) {
		proplist.set("CalendarType", "WaterYear");
	}
	else if (dataset.getCyrl() == YearType.NOV_TO_OCT) {
		proplist.set("CalendarType", "IrrigationYear");
	}

	try {	
		new TSViewJFrame(tslist, proplist);
	} 
	catch (Exception e) {
		String routine = "StateMod_GUIUtil.displayGraphForTS";
		Message.printWarning(1, routine, "Unable to display graph.");
		Message.printWarning(2, routine, e);
	}
}

public static void editFile(String filename) 
throws Exception
{
	String [] command_array = new String[2];
	command_array[0] = _editorPreference;
	command_array[1] = filename;
	ProcessManager p = new ProcessManager ( command_array );
	// This will run as a thread until the process is shut down...
	Thread t = new Thread ( p );
	t.start ();
}

/**
Return the editor preference (default is "NotePad"), for use in viewing/editing files.
@return the editor preference.
*/
public static String getEditorPreference ()
{	return _editorPreference;
}

/**
Called by JFrames when nothing is selected from the table of ids and names.
This disables all the JComponents on the form that are only relevant if a data object is selected
@param components an array of all the JComponents on the form which can be
disabled when nothing is selected.
@deprecated Use JGUIUtil.disableComponents
*/
public static void nothingSelected(JComponent[] components)
{	JGUIUtil.disableComponents ( components, true );
}

/**
Set the program to be used for editing/viewing files.
@param editor The editor program to use, as a full path, or the name of a
program in the PATH environment variable (or current directory).
*/
public static void setEditorPreference ( String editor )
{	_editorPreference = editor;
}

/**
Sets the title in a uniform fashion, as determined by the values passed in.
The general pattern of the title will be 
"AppName - DataSet Base Name - Window Name (status)"
@param frame the frame on which to set the title.  Cannot be null.
@param dataset the dataset from which to get the base dataset name.   The 
basename can be null or "", in which case it won't be included in the title.
The dataset can be null.
@param window_title the title of the window.  Can be null or "", in which 
case it won't be included in the title.
@param status the status of the window.  Can be null or "", in which case 
it won't be included in the title.
*/
public static void setTitle(JFrame frame, DataSet dataset, String window_title, String status)
{
	String title = "";
	int count = 0;
	
	String appName = JGUIUtil.getAppNameForWindows().trim();
	if (!appName.trim().equals("")) {
		title += appName;
		count++;
	}
	
	if (dataset != null) {
		String basename = dataset.getBaseName();
		if (basename != null && !basename.trim().equals("")) {
			if (count > 0) {
				title += " - ";
			}
			title += basename.trim();
			count++;
		}
	}

	if (window_title != null && !window_title.trim().equals("")) {
		if (count > 0) {
			title += " - ";
		}
		title += window_title.trim();
		count++;
	}

	if (status != null && !status.trim().equals("")) {
		if (count > 0) {
			title += " ";
		}
		title += "(" + status + ")";
	}
	
	frame.setTitle(title);
}

/**
Sets the title in a uniform fashion, as determined by the values passed in.
The general pattern of the title will be 
"AppName - DataSet Base Name - Window Name (status)"
@param dialog the dialog on which to set the title.  Cannot be null.
@param dataset the dataset from which to get the base dataset name.   The 
basename can be null or "", in which case it won't be included in the title.
The dataset can be null.
@param window_title the title of the window.  Can be null or "", in which 
case it won't be included in the title.
@param status the status of the window.  Can be null or "", in which case 
it won't be included in the title.
*/
public static void setTitle(JDialog dialog, StateMod_DataSet dataset, String window_title, String status)
{
	String title = "";
	int count = 0;
	
	String appName = JGUIUtil.getAppNameForWindows().trim();
	if (!appName.trim().equals("")) {
		title += appName;
		count++;
	}
	
	if (dataset != null) {
		String basename = dataset.getBaseName();
		if (basename != null && !basename.trim().equals("")) {
			if (count > 0) {
				title += " - ";
			}
			title += basename.trim();
			count++;
		}
	}

	if (window_title != null && !window_title.trim().equals("")) {
		if (count > 0) {
			title += " - ";
		}
		title += window_title.trim();
		count++;
	}

	if (status != null && !status.trim().equals("")) {
		if (count > 0) {
			title += " ";
		}
		title += "(" + status + ")";
	}
	
	dialog.setTitle(title);
}

/**
Called by JFrames when a data object is selected from the table of names and
ids.  This enables what needs to be enabled properly.
@param components an array of all the JComponents on the form which can be
enabled when something is selected.
@param textUneditables an array of the elements in disables[] that should never be editable.
@param editable whether the form is editable or not.
@deprecated Use JGUIUtil.enableComponents
*/
public static void somethingSelected(JComponent[] components, int[] textUneditables, boolean editable)
{
	JGUIUtil.enableComponents ( components, textUneditables, editable );
}

}