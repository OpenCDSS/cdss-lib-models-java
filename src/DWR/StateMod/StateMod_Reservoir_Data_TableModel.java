// StateMod_Reservoir_Data_TableModel - table model for displaying reservoir station data

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;

/**
This table model displays reservoir station data.
*/
@SuppressWarnings("serial")
public class StateMod_Reservoir_Data_TableModel
extends JWorksheet_AbstractRowTableModel<StateMod_Reservoir> implements StateMod_Data_TableModel {

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
Whether the table data is editable or not.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified or not.
*/
public StateMod_Reservoir_Data_TableModel(List<StateMod_Reservoir> data, boolean editable) {
	if (data == null) {
		_data = new Vector<StateMod_Reservoir>();
	}
	else {
		_data = data;
	}
	_rows = _data.size();

	__editable = editable;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
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
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_ID:		return "%-12.12s";
		case COL_NAME:		return "%-24.24s";
		case COL_NODE_ID:	return "%-12.12s";
		case COL_SWITCH:	return "%8d";
		case COL_ONE_FILL_DATE:	return "%8d";
		case COL_MIN_CONTENT:	return "%10.2f";
		case COL_MAX_CONTENT:	return "%10.2f";
		case COL_MAX_RELEASE:	return "%10.2f";
		case COL_DEAD_STORAGE:	return "%10.2f";
		case COL_DAILY_ID:	return "%-12.12s";
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
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators( int col ) 
{
	Validator[] no_checks = new Validator[] {};
	// Switch must be 0, 1, 2 or 3.
	Validator [] data_type = new Validator[] {
		Validators.isEquals( Integer.valueOf( 0 ) ),
		Validators.isEquals( Integer.valueOf( 1 ) ),
		Validators.isEquals( Integer.valueOf( 2 ) ),
		Validators.isEquals( Integer.valueOf( 3 ) ) };
	Validator [] switch_Validators = new Validator[] {
			Validators.or( data_type ) };
	Validator [] rdate = new Validator[] {
		Validators.notBlankValidator(),
		Validators.rangeValidator( -2, 13 )};
	
	switch ( col ) {
	case COL_ID:				return ids;
	case COL_NAME:				return blank;
	case COL_NODE_ID:			return ids;
	case COL_SWITCH:			return switch_Validators;
	case COL_ONE_FILL_DATE:		return rdate;
	case COL_MIN_CONTENT:		return nums;
	case COL_MAX_CONTENT:		return nums;
	case COL_MAX_RELEASE:		return nums;
	case COL_DEAD_STORAGE:		return nums;
	case COL_DAILY_ID:			return ids;
	case COL_NUM_OWNERS:		return nums;
	case COL_NUM_PRECIP_STA: 	return nums;
	case COL_NUM_EVAP_STA:		return nums;
	case COL_NUM_CURVE_ROWS:	return nums;
	default:					return no_checks;
	}
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_Reservoir r = (StateMod_Reservoir)_data.get(row);
	switch (col) {
		case COL_ID:		return r.getID();
		case COL_NAME:		return r.getName();
		case COL_NODE_ID:	return r.getCgoto();
		case COL_SWITCH:	return Integer.valueOf(r.getSwitch());
		case COL_ONE_FILL_DATE:	return Integer.valueOf((int)r.getRdate());
		case COL_MIN_CONTENT:	return Double.valueOf(r.getVolmin());
		case COL_MAX_CONTENT:	return Double.valueOf(r.getVolmax());
		case COL_MAX_RELEASE:	return Double.valueOf(r.getFlomax());
		case COL_DEAD_STORAGE:	return Double.valueOf(r.getDeadst());
		case COL_DAILY_ID:	return r.getCresdy();
		case COL_NUM_OWNERS:	return Integer.valueOf(r.getNowner());
		case COL_NUM_PRECIP_STA:
			int nptpx = StateMod_ReservoirClimate.getNumPrecip(r.getClimates());
			return Integer.valueOf(nptpx);
		case COL_NUM_EVAP_STA:	
			int nevap = StateMod_ReservoirClimate.getNumEvap(r.getClimates());
			return Integer.valueOf(nevap);
		case COL_NUM_CURVE_ROWS:
			List<StateMod_ReservoirAreaCap> v = r.getAreaCaps();
			if (v == null) {
				return Integer.valueOf(0);
			}
			else {
				return Integer.valueOf(v.size());
			}
		default:		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  Currently no columns are editable.
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
	int ival;
	StateMod_Reservoir smr = _data.get(row);
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
				ival = Integer.valueOf( onOff.substring(0, index)).intValue();
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
