// ----------------------------------------------------------------------------
// StateMod_Reservoir_TableModel - table model for displaying reservoir 
//	station data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-11	JTS, RTi		Revised so that it displays real data
//					instead of dummy data for the main
//					Reservoir display.
// 2003-06-13	JTS, RTi		* Created code for displaying the
//					  area cap and climate data
//					* Added code to handle editing
//					  data
// 2003-06-16	JTS, RTi		* Added code for the reservoir account
//					  data
//					* Added code for the reservoir right
//					  data
// 2003-06-17	JTS, RTi		Revised javadocs.
// 2003-07-17	JTS, RTi		Constructor now takes a editable flag
//					to specify whether the data should be
//					editable or not.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-08-16	Steven A. Malers, RTi	Update because of changes in the
//					StateMod_ReservoirClimate class.
// 2003-08-22	JTS, RTi		* Changed headings for the rights table.
//					* Changed headings for the owner account
//					  table.
//					* Added code to accomodate tables which
//					  are now using comboboxes for entering
//					  data.
// 2003-08-25	JTS, RTi		Added partner table code for saving
//					data in the reservoir climate gui.
// 2003-08-28	SAM, RTi		* Change setRightsVector() call to
//					  setRights().
//					* Update for changes in
//					  StateMod_Reservoir.
// 2003-09-18	JTS, RTi		Added ID column for reservoir
//					accounts.
// 2003-10-10	JTS, RTi		* Removed reference to parent reservoir.
//					* Added getColumnToolTips().
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Remove code for other than reservoir
//					stations.
//					Change setValueAt() to support sort.
//					Add tool tips.
// 2005-01-20	JTS, RTi		* Added the compactForm.
//					* Added some new fields.
// 2005-03-30	JTS, RTi		Added new fields:
//					* River Node ID
//					* Switch
//					* Num owners
//					* Num precip stations
//					* Num evap stations
//					* Num curve rows
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.String.StringUtil;

/**
This table model displays reservoir station data.
*/
public class StateMod_Reservoir_TableModel
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 14;

/**
References to columns.
*/
public final static int
	COL_ID =		0,
	COL_NAME =		1,
	COL_NODE_ID = 		2,
	COL_SWITCH = 		3,
	COL_ONE_FILL_DATE =	4,
	COL_MIN_CONTENT = 	5,
	COL_MAX_CONTENT = 	6,
	COL_MAX_RELEASE = 	7,
	COL_DEAD_STORAGE = 	8,
	COL_DAILY_ID = 		9,
	COL_NUM_OWNERS = 	10,
	COL_NUM_EVAP_STA = 	11,
	COL_NUM_PRECIP_STA = 	12,
	COL_NUM_CURVE_ROWS = 	13;
	
/**
Whether all the columns are shown (false) or only the ID and name columns are
shown (true).
*/
private boolean __compactForm = true;
	
/**
Whether the table data is editable or not.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified or not.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Reservoir_TableModel(Vector data, boolean editable)
throws Exception {
	this(data, editable, true);
}

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified or not.
@param compactForm whether to only show the ID and name columns (true) or all
data columns.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Reservoir_TableModel(Vector data, boolean editable,
boolean compactForm)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_Reservoir_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;

	__compactForm = compactForm;

	if (__compactForm) {
		__COLUMNS = 2;
	}
	else {
		__COLUMNS = 14;
	}
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_NAME:		return String.class;
		case COL_NODE_ID:	return String.class;
		case COL_SWITCH:	return Integer.class;
		case COL_ONE_FILL_DATE:	return Integer.class;
		case COL_MIN_CONTENT:	return Double.class;
		case COL_MAX_CONTENT:	return Double.class;
		case COL_MAX_RELEASE:	return Double.class;
		case COL_DEAD_STORAGE:	return Double.class;
		case COL_DAILY_ID:	return String.class;
		case COL_NUM_OWNERS:	return Integer.class;
		case COL_NUM_PRECIP_STA:return Integer.class;
		case COL_NUM_EVAP_STA:	return Integer.class;
		case COL_NUM_CURVE_ROWS:return Integer.class;
		default:		return String.class;
	}
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
Returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return "\n\nID";
		case COL_NAME:		return "\n\nNAME";
		case COL_NODE_ID:	return "\nRIVER\nNODE ID";
		case COL_SWITCH:	return "\nON/OFF\nSWITCH";
		case COL_ONE_FILL_DATE:	return "ONE\nFILL\nDATE";
		case COL_MIN_CONTENT:	return "MIN\nCONTENT\n(ACFT)";
		case COL_MAX_CONTENT:	return "MAX\nCONTENT\n(ACFT)";
		case COL_MAX_RELEASE:	return "MAX\nRELEASE\n(CFS)";
		case COL_DEAD_STORAGE:	return "DEAD\nSTORAGE\n(ACFT)";
		case COL_DAILY_ID:	return "\nDAILY\nID";
		case COL_NUM_OWNERS:	return "NUMBER\nOF\nOWNERS";
		case COL_NUM_PRECIP_STA:return "NUMBER\nOF PRECIP.\nSTATIONS";
		case COL_NUM_EVAP_STA:	return "NUMBER\nOF EVAP.\nSTATIONS";
		case COL_NUM_CURVE_ROWS:return "NUMBER\nOF CURVE\nROWS";
		default:	return " ";
	}	
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_ID] = 
		"<html>The reservoir station identifier is the main link" +
		" between reservoir data<BR>" +
		"and must be unique in the data set.</html>";
	tips[COL_NAME] = 
		"Reservoir station name.";

	if (__compactForm) {
		return tips;
	}

	tips[COL_NODE_ID] = "Node where reservoir is located.";
	tips[COL_SWITCH] = "<html>Switch.<br>0 = off<br>1 = on</html>";

	tips[COL_ONE_FILL_DATE] =
		"<html>Date for one fill rule admin.</html>";
	tips[COL_MIN_CONTENT] =
		"<html>Minimum reservoir content (ACFT).</html>";
	tips[COL_MAX_CONTENT] =
		"<html>Maximum reservoir content (ACFT).</html>";
	tips[COL_MAX_RELEASE] =
		"<html>Maximum release (CFS).</html>";
	tips[COL_DEAD_STORAGE] =
		"<html>Dead storage in reservoir (ACFT).</html>";
	tips[COL_DAILY_ID] =
		"Identifier for daily time series.";
	tips[COL_NUM_OWNERS] = "Number of owners.";
	tips[COL_NUM_PRECIP_STA] = "Number of precipitation stations.";
	tips[COL_NUM_EVAP_STA] = "Number of evaporation stations.";
	tips[COL_NUM_CURVE_ROWS] = "Number of curve rows.";
	
	return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	widths[COL_ID] = 		9;
	widths[COL_NAME] = 		23;
	
	if (__compactForm) {
		return widths;
	}

	widths[COL_NODE_ID] = 		8;
	widths[COL_SWITCH] = 		5;
	widths[COL_ONE_FILL_DATE] =	4;
	widths[COL_MIN_CONTENT] =	8;
	widths[COL_MAX_CONTENT] =	8;
	widths[COL_MAX_RELEASE] =	8;
	widths[COL_DEAD_STORAGE] =	8;
	widths[COL_DAILY_ID] =		5;
	widths[COL_NUM_OWNERS] = 	8;
	widths[COL_NUM_PRECIP_STA] = 	8;
	widths[COL_NUM_EVAP_STA] = 	8;
	widths[COL_NUM_CURVE_ROWS] = 	8;

	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_ID:		return "%-40s";
		case COL_NAME:		return "%-40s";
		case COL_NODE_ID:	return "%-40s";
		case COL_SWITCH:	return "%8d";
		case COL_ONE_FILL_DATE:	return "%8d";
		case COL_MIN_CONTENT:	return "%10.2f";
		case COL_MAX_CONTENT:	return "%10.2f";
		case COL_MAX_RELEASE:	return "%10.2f";
		case COL_DEAD_STORAGE:	return "%10.2f";
		case COL_DAILY_ID:	return "%-40s";
		case COL_NUM_OWNERS:	return "%8d";
		case COL_NUM_PRECIP_STA:return "%8d";
		case COL_NUM_EVAP_STA:	return "%8d";
		case COL_NUM_CURVE_ROWS:return "%8d";
		default:		return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_Reservoir r = (StateMod_Reservoir)_data.elementAt(row);
	switch (col) {
		case COL_ID:		return r.getID();
		case COL_NAME:		return r.getName();
		case COL_NODE_ID:	return r.getCgoto();
		case COL_SWITCH:	return new Integer(r.getSwitch());
		case COL_ONE_FILL_DATE:	return new Integer((int)r.getRdate());
		case COL_MIN_CONTENT:	return new Double(r.getVolmin());
		case COL_MAX_CONTENT:	return new Double(r.getVolmax());
		case COL_MAX_RELEASE:	return new Double(r.getFlomax());
		case COL_DEAD_STORAGE:	return new Double(r.getDeadst());
		case COL_DAILY_ID:	return r.getCresdy();
		case COL_NUM_OWNERS:	return new Integer(r.getNowner());
		case COL_NUM_PRECIP_STA:
			int nptpx = StateMod_ReservoirClimate.getNumPrecip(
				r.getClimates());
			return new Integer(nptpx);
		case COL_NUM_EVAP_STA:	
			int nevap = StateMod_ReservoirClimate.getNumEvap(
				r.getClimates());
			return new Integer(nevap);
		case COL_NUM_CURVE_ROWS:
			Vector v = r.getAreaCaps();
			if (v == null) {
				return new Integer(0);
			}
			else {
				return new Integer(v.size());
			}
		default:		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  Currently
no columns are editable.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}
	return true;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	StateMod_Reservoir smr = (StateMod_Reservoir)_data.elementAt(row);
	switch (col) {
		case COL_ID:
			smr.setID((String)value);
			break;
		case COL_NAME:
			smr.setName((String)value);
			break;
		case COL_NODE_ID:
			smr.setCgoto((String)value);
			break;
		case COL_SWITCH:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smr.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				int index = onOff.indexOf(" -");
				ival = new Integer(
					onOff.substring(0,
					index)).intValue();
				smr.setSwitch(ival);
			}
			break;
		case COL_ONE_FILL_DATE:
			smr.setRdate(((Integer)value).intValue());
			break;
		case COL_MIN_CONTENT:
			smr.setVolmin(((Double)value).doubleValue());
			break;
		case COL_MAX_CONTENT:
			smr.setVolmax(((Double)value).doubleValue());
			break;
		case COL_MAX_RELEASE:
			smr.setFlomax(((Double)value).doubleValue());
			break;
		case COL_DEAD_STORAGE:
			smr.setDeadst(((Double)value).doubleValue());
			break;
		case COL_DAILY_ID:
			smr.setCresdy((String)value);
			break;
	}	
}

}
