// ----------------------------------------------------------------------------
// StateMod_GraphingTool_TableModel - Table model for displaying data for 
//	graphing tool-related tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-07	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-10-26	SAM, RTi		* Decrease width of ID column.
//					* Add interval to allow monthly and
//					  daily time series to be added to
//					  graphs (and together).
//					* Add Input Type to emphasize whether
//					  StateMod input text file or binary
//					  output file.
//					* Change scenario to Input Name.
//					* Define column positions as integer
//					  data members to make references to
//					  columns easier.
//					* Use TSIdent to manage rows of data
//					  instead of StateMod_GraphNode.  The
//					  TSIdent alias is used to store the
//					  station type.
//					* Move lookup data from
//					  StateMod_GraphNode to StateMod_Util.
//					* Pass a StateMod_DataSet instance to
//					  the constructor to be able to make
//					  more intelligent decisions in the
//					  graph choices.
// 2003-11-05	JTS, RTi		* Did work on making entered values
//					  automatically select values in other
//					  columns.
//					* Added setInternalValueAt().
//					* Added browseForFile().
// 2003-11-14	SAM, RTi		Enable file formats other than B43
// 2003-11-29	SAM, RTi		Add reservoir accounts to the
//					reservoir identifiers.  Each reservoir
//					is listed without an account for the
//					total and also is listed for each
//					account.
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;

import RTi.TS.TSIdent;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeInterval;

/**
This table model display graphing tool data.
*/
public class StateMod_GraphingTool_TableModel 
extends JWorksheet_AbstractRowTableModel {

private final int __COLUMNS = 6;		// The number of columns.
protected final int _COL_STATION_TYPE = 0;
protected final int _COL_ID = 1;
protected final int _COL_INTERVAL = 2;
protected final int _COL_DATA_TYPE = 3;
protected final int _COL_INPUT_TYPE = 4;
protected final int _COL_INPUT_NAME = 5;

private final String __BROWSE_INPUT_NAME_ABSOLUTE = "Browse for file (absolute path)...";
private final String __BROWSE_INPUT_NAME_RELATIVE = "Browse for file (relative path)...";

/**
The parent frame on which the JWorksheet for this model is displayed.
*/
private StateMod_GraphingTool_JFrame __parent = null;

/**
The worksheet in which this table model is working.
*/
private JWorksheet __worksheet;

/**
The StateMod data set that is being processed.
*/
private StateMod_DataSet __dataset = null;

/**
Vectors of data for filling ID lists.
*/
private Vector 
	__diversions,
	__instreamFlows,
	__reservoirs,
	__streamEstimateStations,
	__streamGageStations,
	__wells;

/**
ID lists to be displayed in the combo boxes.
*/
private Vector
	__diversionIDs = null,
	__instreamFlowIDs = null,
	__reservoirIDs = null,
	__streamEstimateStationIDs = null,
	__streamGageStationIDs = null,
	__wellIDs = null;

/**
Constructor.  
@param parent the StateMod_GraphingTool_JFrame in which the table is displayed.
@param dataset the dataset containing the data
@param data the data to display in the worksheet.
@throws Exception if an invalid data was passed in.
*/
public StateMod_GraphingTool_TableModel ( StateMod_GraphingTool_JFrame parent, StateMod_DataSet dataset, Vector data )
throws Exception {
	__parent = parent;

	__dataset = dataset;
	__reservoirs = (Vector)__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
	__diversions = (Vector)__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_DIVERSION_STATIONS).getData();
	__instreamFlows = (Vector)__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_INSTREAM_STATIONS).getData();
	__wells = (Vector)__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_WELL_STATIONS).getData();
	__streamGageStations = (Vector)__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_STREAMGAGE_STATIONS).getData();
	__streamEstimateStations=(Vector)__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS).getData();
	
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to StateMod_GraphingTool_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;
}

/**
Checks whether enough values have been entered in the current last row to tell
whether a new row can be added.  A new row can only be added if values have 
been set for columns COL_STATION_TYPE and COL_ID.
@return whether it is valid to add a new row.
*/
public boolean canAddNewRow() {
	int rows = getRowCount();

	if (rows == 0) {
		return true;
	}

	String type = (String)getValueAt((rows - 1), _COL_STATION_TYPE);
	String id = (String)getValueAt((rows - 1), _COL_ID);
	
	if (type == null || type.trim().equals("")) {
		return false;
	}
	if (id == null || id.trim().equals("")) {
		return false;
	}

	return true;
}

/**
Creates a list of the available IDs for a Vector of StateMod_Data-extending
objects.  Reservoirs will include an identifier for each reservoir total and each account for the reservoir.
@param nodes the nodes for which to create a list of IDs.
@param include_accounts If true, the 
@return a Vector of Strings, each of which contains an ID followed by the name of Structure in parentheses
*/
private Vector createAvailableIDsList ( Vector nodes ) {
	Vector v = new Vector();

	int num = 0;
	boolean is_reservoir = false;	// To allow check below
	if (nodes != null) {
		num = nodes.size();
		if ( (num > 0) && ((StateMod_Data)nodes.elementAt(0))instanceof StateMod_Reservoir ) {
			is_reservoir = true;
		}
	}

	StateMod_Reservoir res = null;	// These are used if reservoirs.
	int nowner = 0;
	int ia = 0;
	for (int i = 0; i < num; i++) {
		// Add the normal item...
		v.add( StateMod_Util.formatDataLabel(
			((StateMod_Data)nodes.elementAt(i)).getID(),
			((StateMod_Data)nodes.elementAt(i)).getName() ) );
		if ( is_reservoir ) {
			// Also add reservoir owner/accounts...
			res = (StateMod_Reservoir)nodes.elementAt(i);
			nowner = res.getAccounts().size();
			for ( ia = 0; ia < nowner; ia++ ) {
				v.add( StateMod_Util.formatDataLabel(
					res.getID() + "-" + (ia + 1),
					res.getName() + " - " +
					res.getAccount(ia).getName() ) );
			}
		}
	}
	return v;
}

/**
Fills the data type column according to the type of structure selected,
the ID of that structure, and the interval that is selected.
@param row the row of the data type column that is being dealt with
@param outputOnly whether to only display OUTPUT data types.
@param station_type the type of the station (column _COL_STATION_TYPE)
@param id the ID of the station (column _COL_ID)
@param interval_string the data interval (column _COL_INTERVAL )
*/
public void fillDataTypeColumn ( int row, boolean outputOnly,
					String station_type, String id, String interval_string )
{	Vector dataTypes = new Vector();
	int interval = TimeInterval.MONTH;
	if ( interval_string.equalsIgnoreCase("Day") ) {
		interval = TimeInterval.DAY;
	}

	if ( station_type.equalsIgnoreCase(	StateMod_Util.STATION_TYPE_DIVERSION)) {
		dataTypes = StateMod_Util.getTimeSeriesDataTypes(
			StateMod_DataSet.COMP_DIVERSION_STATIONS,
			id, __dataset,
			"",	// Use default version
			//StateMod_Util.getStateModVersion(),
			interval,
			true, true, true, true, false, true );
	}
	else if (station_type.equalsIgnoreCase( StateMod_Util.STATION_TYPE_INSTREAM_FLOW)) {
		dataTypes = StateMod_Util.getTimeSeriesDataTypes(
			StateMod_DataSet.COMP_INSTREAM_STATIONS,
			id, __dataset,
			"",	// Use default version
			//StateMod_Util.getStateModVersion(),
			interval,
			true, true, true, true, false, true );
	}
	else if (station_type.equalsIgnoreCase( StateMod_Util.STATION_TYPE_RESERVOIR)) {
		if (outputOnly) {
			dataTypes = StateMod_Util.getTimeSeriesDataTypes(
				StateMod_DataSet.COMP_RESERVOIR_STATIONS,
				id, __dataset,
				"", // Use default version
				//StateMod_Util.getStateModVersion(),
				interval,
				false, false, true, true, false, true );
		}		
		else {
			dataTypes = StateMod_Util.getTimeSeriesDataTypes(
				StateMod_DataSet.COMP_RESERVOIR_STATIONS,
				id, __dataset,
				"", // Use default version
				//StateMod_Util.getStateModVersion(),
				interval,
				true, true, true, true, false, true );
		}
	}
	else if (station_type.equalsIgnoreCase( StateMod_Util.STATION_TYPE_STREAMGAGE)) {
		dataTypes = StateMod_Util.getTimeSeriesDataTypes(
			StateMod_DataSet.COMP_STREAMGAGE_STATIONS,
			id, __dataset,
			"",  // Use default version
			//StateMod_Util.getStateModVersion(),
			interval,
			true, true, true, true, false, true );
	}
	else if (station_type.equalsIgnoreCase( StateMod_Util.STATION_TYPE_STREAMESTIMATE)) {
		dataTypes = StateMod_Util.getTimeSeriesDataTypes(
			StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS,
			id, __dataset,
			"",  // Use default version
			//StateMod_Util.getStateModVersion(),
			interval,
			true, true, true, true, false, true );
	}
	else if(station_type.equalsIgnoreCase(StateMod_Util.STATION_TYPE_WELL)){
		dataTypes = StateMod_Util.getTimeSeriesDataTypes(
			StateMod_DataSet.COMP_WELL_STATIONS,
			id, __dataset,
			"",  // Use default version
			//StateMod_Util.getStateModVersion(),
			interval,
			true, true, true, true, false, true );
	}

	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues(	row, _COL_DATA_TYPE, dataTypes);
		Vector v = __worksheet.getCellSpecificJComboBoxValues(row, _COL_DATA_TYPE);
		String s = null;
		if (v == null || v.size() == 0) {
			s = "";
		}
		else {
			s = (String)v.elementAt(0);
		}
		setInternalValueAt(s, row,_COL_DATA_TYPE);
	}	
}

/**
Fills the ID column based on the kind of station selected.
@param row the row of the ID column being dealt with
@param type the type of structure selected (column 1)
*/
public void fillIDColumn(int row, String type)
{	Vector ids = new Vector();
	if (type.equalsIgnoreCase(StateMod_Util.STATION_TYPE_DIVERSION)) {
		if (__diversionIDs == null) {
			__diversionIDs = createAvailableIDsList(__diversions);
		}
		ids = __diversionIDs;
	}
	else if (type.equalsIgnoreCase(StateMod_Util.STATION_TYPE_INSTREAM_FLOW)) {
		if (__instreamFlowIDs == null) {
			__instreamFlowIDs = createAvailableIDsList(__instreamFlows);
		}
		ids = __instreamFlowIDs;
	}
	else if (type.equalsIgnoreCase(StateMod_Util.STATION_TYPE_RESERVOIR)) {
		if (__reservoirIDs == null) {
			__reservoirIDs = createAvailableIDsList ( __reservoirs);
		}
		ids = __reservoirIDs;
	}
	else if (type.equalsIgnoreCase(StateMod_Util.STATION_TYPE_STREAMGAGE)) {
		if (__streamGageStationIDs == null) {
			__streamGageStationIDs = createAvailableIDsList(__streamGageStations);
		}
		ids = __streamGageStationIDs;
	}
	else if (type.equalsIgnoreCase(
		StateMod_Util.STATION_TYPE_STREAMESTIMATE)) {
		if (__streamEstimateStationIDs == null) {
			__streamEstimateStationIDs = createAvailableIDsList(__streamEstimateStations);
		}
		ids = __streamEstimateStationIDs;
	}
	else if (type.equalsIgnoreCase(StateMod_Util.STATION_TYPE_WELL)) {
		if (__wellIDs == null) {
			__wellIDs = createAvailableIDsList(__wells);
		}
		ids = __wellIDs;
	}

	if (ids.size() == 0) {
		ids.add(" ");
	}

	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues(row, _COL_ID, ids);
		Vector v = __worksheet.getCellSpecificJComboBoxValues( row, _COL_ID);
		String s = null;
		if (v == null || v.size() == 0) {
			s = "";
		}
		else {
			s = (String)v.elementAt(0);
		}
		setInternalValueAt(s, row, _COL_ID);
	}
}

/**
Fills the input name column combo box according to the type of station, ID,
interval, data type, and input type that is selected.
@param row the row of the data type column that is being dealt with
@param station_type the type of the station (column _COL_STATION_TYPE)
@param id the ID of the structure (column _COL_ID)
@param interval_string The data interval (column _COL_INTERVAL).
@param data_type The data type (column _COL_DATA_TYPE).
@param input_type The input type (column _COL_INPUT_TYPE).
*/
public void fillInputNameColumn ( int row, String station_type, String id,
					String interval_string, String data_type, String input_type )
{	Vector input_names = new Vector();
	int interval = TimeInterval.MONTH;
	if ( interval_string.equalsIgnoreCase("Day") ) {
		interval = TimeInterval.DAY;
	}
	if ( StringUtil.indexOfIgnoreCase(data_type, "Output", 0) > 0 ) {
		// Have an output time series...
		if ( station_type.equalsIgnoreCase ( StateMod_Util.STATION_TYPE_DIVERSION) ||
			station_type.equalsIgnoreCase ( StateMod_Util.STATION_TYPE_STREAMGAGE) ||
			station_type.equalsIgnoreCase ( StateMod_Util.STATION_TYPE_STREAMESTIMATE) ||
			station_type.equalsIgnoreCase ( StateMod_Util.STATION_TYPE_INSTREAM_FLOW) ) {
			if ( interval == TimeInterval.MONTH ) {
				// Substitute base name later...
				input_names.addElement ( "*.b43" );
				// Explicitly specify base name...
				input_names.addElement ( __dataset.getBaseName() + ".b43" );
			}
			else {
			    // Daily...
				input_names.addElement ( "*.b49" );
				// Explicitly specify base name...
				input_names.addElement ( __dataset.getBaseName() + ".b49" );
			}
		}
		else if(station_type.equalsIgnoreCase ( StateMod_Util.STATION_TYPE_RESERVOIR) ) {
			if ( interval == TimeInterval.MONTH ) {
				// Substitute base name later...
				input_names.addElement ( "*.b44" );
				// Explicitly specify base name...
				input_names.addElement ( __dataset.getBaseName() + ".b44" );
			}
			else {
			    // Daily...
				input_names.addElement ( "*.b50" );
				// Explicitly specify base name...
				input_names.addElement ( __dataset.getBaseName() + ".b50" );
			}
		}
		else if(station_type.equalsIgnoreCase ( StateMod_Util.STATION_TYPE_WELL) ) {
			if ( interval == TimeInterval.MONTH ) {
				// Substitute base name later...
				input_names.addElement ( "*.b42" );
				// Explicitly specify base name...
				input_names.addElement ( __dataset.getBaseName() + ".b42" );
			}
			else {
			    // Daily...
				input_names.addElement ( "*.b65" );
				// Explicitly specify base name...
				input_names.addElement ( __dataset.getBaseName() + ".b65" );
			}
		}
	}
	else {
	    // Need to pick the correct input name from the type...
		// This needs to be relative if at all possible!
		String ext = StateMod_DataSet.lookupTimeSeriesDataFileExtension (
			StringUtil.getToken(data_type," ",0,0), interval );
		if ( !ext.equals("") ) {
			input_names.addElement ( "*." + ext );
		}
		String filename = __dataset.getComponentDataFileNameFromTimeSeriesDataType (
			StringUtil.getToken(data_type," ",0,0), interval );
		if ( !filename.equals("") ) {
			input_names.addElement ( filename );
		}
	}

	// Always add a Browse...
	input_names.addElement ( __BROWSE_INPUT_NAME_ABSOLUTE );
	input_names.addElement ( __BROWSE_INPUT_NAME_RELATIVE );

	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues( row, _COL_INPUT_NAME, input_names);
		Vector v = __worksheet.getCellSpecificJComboBoxValues( row,_COL_INPUT_NAME);
		String s = null;
		if (v == null || v.size() == 0) {
			s = "";
		}
		else {	
			s = (String)v.elementAt(0);
		}
		setInternalValueAt(s, row, _COL_INPUT_NAME);
	}
}

/**
Fills the input type column combo box according to the type of station, ID,
interval, and data type that is selected.
@param row the row of the data type column that is being dealt with
@param station_type the type of the station (column _COL_STATION_TYPE)
@param id the ID of the structure (column _COL_ID)
@param interval_string The data interval (column _COL_INTERVAL).
@param data_type The data type (column _COL_DATA_TYPE).
*/
public void fillInputTypeColumn ( int row, String station_type, String id,
					String interval_string, String data_type )
{	Vector input_types = new Vector();

	if ( StringUtil.indexOfIgnoreCase(data_type, "Output", 0) > 0 ) {
		// Have an output time series...
		input_types.addElement ( "StateModB" );
	}
	else {
	    input_types.addElement ( "StateMod" );
	}

	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues( row, _COL_INPUT_TYPE, input_types);
		Vector v = __worksheet.getCellSpecificJComboBoxValues( row,_COL_INPUT_TYPE);
		String s = null;
		if (v == null || v.size() == 0) {
			s = "";
		}
		else {
			s = (String)v.elementAt(0);
		}
		// NOTE: this doesn't call setIntervalValueAt in order that
		// the input name will be populated properly.
		setValueAt(s, row, _COL_INPUT_TYPE);
	}
}

// TODO - need to check the data set to see if daily data are available
/**
Fills the interval column combo box according to the type of station selected and the ID of that station.
@param row the row of the data type column that is being dealt with
@param station_type the type of the station (column _COL_STATION_TYPE)
@param id the ID of the structure (column _COL_ID)
*/
public void fillIntervalColumn ( int row, String station_type, String id )
{	Vector intervals = new Vector();
	intervals.add ( "Month" );
	intervals.add ( "Day" );

	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues( row, _COL_INTERVAL, intervals);
		Vector v = __worksheet.getCellSpecificJComboBoxValues( row, _COL_INTERVAL);
		String s = null;
		if (v == null || v.size() == 0) {
			s = "";
		}
		else {
			s = (String)v.elementAt(0);
		}
		setInternalValueAt(s, row, _COL_INTERVAL);
	}
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case _COL_STATION_TYPE:		return String.class;
		case _COL_ID:			return String.class;
		case _COL_INTERVAL:		return String.class;
		case _COL_DATA_TYPE:		return String.class;
		case _COL_INPUT_TYPE:		return String.class;
		case _COL_INPUT_NAME:		return String.class;
		default:			return String.class;
	}
}

/**
From AbstractTableModel; returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
From AbstractTableModel; returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case _COL_STATION_TYPE:	return "STATION TYPE";
		case _COL_ID:		return "IDENTIFIER (NAME)";
		case _COL_DATA_TYPE:	return "DATA TYPE";
		case _COL_INTERVAL:	return "INTERVAL";
		case _COL_INPUT_TYPE:	return "INPUT TYPE";
		case _COL_INPUT_NAME:	return "INPUT NAME";
		default:		return " ";
	}
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[_COL_STATION_TYPE] =
		"<html>The station type indicates the list of identifiers that should be displayed.</html>";
	tips[_COL_ID] =
		"<html>The identifier corresponds to a station that has time series.</html>";
	tips[_COL_INTERVAL] =
		"<html>The interval indicates whether monthly or daily time series are graphed.</html>";
	tips[_COL_DATA_TYPE] =
		"<html>Data types identify the time series parameter to be "+
		"graphed.<BR>Parameters are listed as input, estimated input, and output.</HTML>";
	tips[_COL_INPUT_TYPE] =
		"<HTML>The input type indicates the file format for data." +
		"<BR>Input time series by default are read from Statemod time series files (StateMod)." +
		"<BR>Output time series by default are read from binary output files (StateModB).</HTML>";
	tips[_COL_INPUT_NAME] =
		"<HTML>The input name indicates the file to be read.<BR>" +
		"Input time series available in memory will be used before reading a matching file.<BR>" +
		"Select a file from a different data set if appropriate.<BR>" +
		"Use the * choice to share a graph between data sets.</HTML>";
	return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}
	widths[_COL_STATION_TYPE] = 13;
	widths[_COL_ID] = 20;
	widths[_COL_INTERVAL] = 7;
	widths[_COL_DATA_TYPE] = 22;
	widths[_COL_INPUT_TYPE] = 7;
	widths[_COL_INPUT_NAME] = 20;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case _COL_ID:		return "%-16s";
		case _COL_INTERVAL:	return "%8s";
		case _COL_DATA_TYPE:	return "%-20s";
		case _COL_INPUT_TYPE:	return "%-8s";
		case _COL_INPUT_NAME:	return "%-40s";
		default:		return "%-8s";
	}
}

/**
From AbstractTableModel; returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableModel; returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	TSIdent tsident = (TSIdent)_data.elementAt(row);

	switch (col) {
		case _COL_STATION_TYPE:	return tsident.getAlias();
		case _COL_ID:		return tsident.getLocation();
		case _COL_INTERVAL:	return tsident.getInterval();
		case _COL_DATA_TYPE:	return tsident.getType();
		case _COL_INPUT_TYPE:	return tsident.getInputType();
		case _COL_INPUT_NAME:	return tsident.getInputName();
		default:		return "";
	}
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	int size = _cellEditOverride.size();

	if (size > 0) {
		int[] temp;
		for (int i = 0; i < size; i++) {
			temp = (int[])_cellEditOverride.elementAt(i);
			if (temp[0] == rowIndex && temp[1] == columnIndex) {
				if (temp[2] == 1) {
					return true;
				}
				else {
					return false;
				}
			}
		}
	}
	return true;
}

/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	/*
	Message.printStatus(1, "", "---------------------------------------");
	Message.printStatus(1, "", "SET VALUE AT: " + row + ", " +col);
	JWorksheet.except(1, 3);
	Message.printStatus(1, "", "---------------------------------------");
	*/

	TSIdent tsident = (TSIdent)_data.elementAt(row);

	switch (col) {
		case _COL_STATION_TYPE:
			String type = tsident.getAlias();
			if (type.equals((String)value)) {
				break;
			}
			tsident.setAlias((String)value);
			tsident.setLocation("");
			setValueAt("", row, _COL_INTERVAL);
			// this next line wouldn't seem to make any sense, but leave it in!!
			setValueAt(getValueAt(row, _COL_DATA_TYPE), row, _COL_DATA_TYPE);
			fireTableDataChanged();
			overrideCellEdit(row, _COL_ID, true);
			fillIDColumn(row, (String)value);
			// Since the ID is filled, select the first item by
			// default to force something to be displayed...
			if ( __worksheet != null ) {
				Vector ids = __worksheet.getCellSpecificJComboBoxValues(row, _COL_ID);
				if ( ids.size() > 0 ) {
					setValueAt(ids.elementAt(0), row, _COL_ID);
				}
				else {
					setValueAt("", row, _COL_ID);
				}
			}
			break;
		case _COL_ID:
			String id = (String)value;
			tsident.setLocation(id);
			boolean outputOnly = false;
			String staType =(String)getValueAt(row, _COL_STATION_TYPE);
			if (staType.equals(	StateMod_Util.STATION_TYPE_RESERVOIR)) {
				if (id.indexOf("-") > -1) {
					outputOnly = true;
				}
			}
			overrideCellEdit(row, _COL_DATA_TYPE, true);
			// Fill the interval cell, given the station type and identifier...
			if (((String)value).length() == 0) {
				fireTableDataChanged();
			}
			fillIntervalColumn(row, (String)getValueAt(row, _COL_STATION_TYPE),	(String)value );
			if (outputOnly) {
				fillDataTypeColumn(row, true,
					(String)getValueAt(row, _COL_STATION_TYPE),
					(String)getValueAt(row, _COL_ID),
					(String)value);				
			}
			else {
				fillDataTypeColumn(row, false,
					(String)getValueAt(row, _COL_STATION_TYPE),
					(String)getValueAt(row, _COL_ID),
					(String)value);				
			}			
			fireTableDataChanged();
			break;
		case _COL_INTERVAL:
			try {
			    tsident.setInterval((String)value);
			}
			catch ( Exception e ) {
				// Should not happen.
			}
			boolean ioutputOnly = false;
			String istaType = (String)getValueAt(row, _COL_STATION_TYPE);
			String iid = (String)getValueAt(row, _COL_ID);
			if (istaType.equals(StateMod_Util.STATION_TYPE_RESERVOIR)) {
				if (iid.indexOf("-") > -1) {
					ioutputOnly = true;
				}
			}
			if (ioutputOnly) {
				fillDataTypeColumn(row, true,
					(String)getValueAt(row, _COL_STATION_TYPE),
					(String)getValueAt(row, _COL_ID),
					(String)value);				
			}
			else {
				fillDataTypeColumn(row, false,
					(String)getValueAt(row, _COL_STATION_TYPE),
					(String)getValueAt(row, _COL_ID),
					(String)value);				
			}						
			// this next line wouldn't seem to make any sense, but leave it in!!				
			setValueAt(getValueAt(row, _COL_DATA_TYPE), row, _COL_DATA_TYPE);				
			fireTableDataChanged();
			break;
		case _COL_DATA_TYPE:
			tsident.setType((String)value);
			// Fill the input type cell, given the station type, identifier, and interval...
			fillInputTypeColumn(row,
				(String)getValueAt(row, _COL_STATION_TYPE),
				(String)getValueAt(row, _COL_ID),
				(String)getValueAt(row, _COL_INTERVAL),
				(String)value);
			fireTableDataChanged();				
			break;
		case _COL_INPUT_TYPE:
			tsident.setInputType((String)value);
			// Fill the input name with defaults...
			fillInputNameColumn(row,
				(String)getValueAt(row, _COL_STATION_TYPE),
				(String)getValueAt(row, _COL_ID),
				(String)getValueAt(row, _COL_INTERVAL),
				(String)getValueAt(row, _COL_DATA_TYPE),
				(String)value);
			break;
		case _COL_INPUT_NAME:
			String s = (String)value;
			if (s.equals(__BROWSE_INPUT_NAME_ABSOLUTE)) {
				s = browseForFile();
			}
			else if (s.equals(__BROWSE_INPUT_NAME_RELATIVE)) {
				String file = browseForFile();
				if (file != null) {
    				try {
        				int index = file.lastIndexOf(File.separator);
        				String workingDir = __dataset.getDataSetDirectory();
        				String dir = IOUtil.toRelativePath(workingDir,file.substring(0, index));
        				s = dir + File.separator + file.substring(index + 1, file.length());
    				}
    				catch (Exception ex) {
    					// TODO (JTS - 2003-11-05)  maybe handle this better.  Right now just defaults to the absolute filename
    					s = file;
    				}
				}
			}			
			tsident.setInputName(s);
			// don't go through the super.setValueAt() at the end of the method ...
			super.setValueAt(s, row, col);
			return;
	}	

	super.setValueAt(value, row, col);	
}	

/**
Inserts the specified value into the data object at the given position.  This
is not like setValueAt() because it doesn't change any combo box values or
update other columns' data.  It simply puts the data into the data object
and notifies the table that data has changed so that it displays the updated values.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setInternalValueAt(Object value, int row, int col) {
	/*
	Message.printStatus(1, "", "---------------------------------------");
	Message.printStatus(1, "", "SET INTERNAL VALUE AT: " + row + ", " +col);
	JWorksheet.except(1, 3);
	Message.printStatus(1, "", "---------------------------------------");
	*/

	TSIdent tsident = (TSIdent)_data.elementAt(row);

	switch (col) {
		case _COL_STATION_TYPE:
			String type = tsident.getAlias();
			if (type.equals((String)value)) {
				break;
			}
			tsident.setAlias((String)value);
			break;
		case _COL_ID:
			tsident.setLocation((String)value);
			break;
		case _COL_INTERVAL:
			try {
			    tsident.setInterval((String)value);
			}
			catch ( Exception e ) {
				// Should not happen.
			}
			break;
		case _COL_DATA_TYPE:
			tsident.setType((String)value);
			break;
		case _COL_INPUT_TYPE:
			tsident.setInputType((String)value);
			break;
		case _COL_INPUT_NAME:
			String s = (String)value;
			if (s.equals(__BROWSE_INPUT_NAME_ABSOLUTE)) {
				s = browseForFile();
			}
			else if (s.equals(__BROWSE_INPUT_NAME_RELATIVE)) {
				String file = browseForFile();
				if (file != null) {
    				try {
        				int index = file.lastIndexOf(File.separator);
        				String workingDir = __dataset.getDataSetDirectory();
        				String dir = IOUtil.toRelativePath(workingDir, file.substring(0, index));
        				s = dir + File.separator + file.substring( index + 1, file.length());
    				}
    				catch (Exception ex) {
    					// TODO (JTS - 2003-11-05) maybe handle this better.  Right now just defaults to the absolute filename
    					s = file;
    				}
				}
			}			
			tsident.setInputName(s);
			// don't go through the super.setValueAt() at the end of the method ...
			super.setValueAt(s, row, col);
			return;		
	}	

	fireTableDataChanged();
	super.setValueAt(value, row, col);	
}	

/**
Sets the worksheet in which this model is being used.
@param worksheet the worksheet in which this model is being used
*/
public void setWorksheet(JWorksheet worksheet) {
	__worksheet = worksheet;
}

/**
Browse for a statemod output file.
*/
private String browseForFile() {
	JGUIUtil.setWaitCursor(__parent, true);
	String lastDirectorySelected = JGUIUtil.getLastFileDialogDirectory();

	JFileChooser fc = JFileChooserFactory.createJFileChooser( lastDirectorySelected );

	fc.setDialogTitle("Select file");
//	SimpleFileFilter ff = new SimpleFileFilter("???", "?Some kind of file?");
//	fc.addChoosableFileFilter(ff);
//	fc.setAcceptAllFileFilterUsed(false);
//	fc.setFileFilter(ff);
	fc.setAcceptAllFileFilterUsed(true);
	fc.setDialogType(JFileChooser.OPEN_DIALOG);	

	JGUIUtil.setWaitCursor(__parent, false);
	int retVal = fc.showOpenDialog(__parent);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return null;
	}

	String currDir = (fc.getCurrentDirectory()).toString();

	if (!currDir.equalsIgnoreCase(lastDirectorySelected)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	
	String filename = fc.getSelectedFile().getName();

	// do some work with the filename, perhaps

	return currDir + File.separator + filename;
}

}