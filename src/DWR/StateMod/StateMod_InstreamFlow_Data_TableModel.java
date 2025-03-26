// StateMod_InstreamFlow_Data_TableModel - Table model for displaying data in the Instream Flow station tables

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
This table model displays instream flow data.
*/
@SuppressWarnings("serial")
public class StateMod_InstreamFlow_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_InstreamFlow> implements StateMod_Data_TableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 7;

/**
References to columns.
*/
public final static int
	COL_ID =		0,
	COL_NAME =		1,
	COL_NODE_ID = 		2,
	COL_SWITCH = 		3,
	COL_DOWN_NODE = 	4,
	COL_DAILY_ID =		5,
	COL_DEMAND_TYPE = 	6;
	
/**
Whether the table data are editable or not.
*/
private boolean __editable = false;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
*/
public StateMod_InstreamFlow_Data_TableModel(List<StateMod_InstreamFlow>data, boolean editable) {
	if (data == null) {
		_data = new Vector<StateMod_InstreamFlow>();	
	}
	else {
		_data = data;
	}
	_rows = _data.size();

	__editable = editable;

}

/**
returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_NAME:		return String.class;
		case COL_NODE_ID:	return String.class;
		case COL_SWITCH:	return Integer.class;
		case COL_DAILY_ID:	return String.class;
		case COL_DOWN_NODE:	return String.class;
		case COL_DEMAND_TYPE:	return Integer.class;
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
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return "\n\nID";
		case COL_NAME:		return "\n\nNAME";
		case COL_NODE_ID:	return "\nRIVER\nNODE ID";
		case COL_SWITCH:	return "\nON/OFF\nSWITCH";
		case COL_DOWN_NODE:	return "DOWNSTREAM\nRIVER\nNODE ID";
		case COL_DAILY_ID:	return "\n\nDAILY ID";
		case COL_DEMAND_TYPE:	return "\nDEMAND\nTYPE";
		default:		return " ";
	}	
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_ID] = 
		"<html>The instream flow identifier is the main link"
		+ " between instream data data<BR>"
		+ "and must be unique in the data set.</html>";
	tips[COL_NAME] = 
		"<html>Instream flow name.</html>";
	tips[COL_NODE_ID] = 
		"Upstream river ID where instream flow is located.";
	tips[COL_SWITCH] = 
		"<html>Switch.<br>0 = off<br>1 = on</html";
	tips[COL_DAILY_ID] =
		"<html>Daily instream flow ID.</html>";
	tips[COL_DOWN_NODE] =
		"<html>Downstream river node, for instream flow "
		+ "reach.</html>";
	tips[COL_DEMAND_TYPE] =
		"<html>Data type switch.</html>";

	return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	widths[COL_ID] = 		8;
	widths[COL_NAME] = 		21;
	widths[COL_NODE_ID] = 		8;
	widths[COL_SWITCH] = 		6;
	widths[COL_DAILY_ID] =		8;
	widths[COL_DOWN_NODE] =		12;
	widths[COL_DEMAND_TYPE] =	5;

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
		case COL_ID:		return "%-12.12s";
		case COL_NAME:		return "%-24.24s";
		case COL_NODE_ID:	return "%-12.12s";
		case COL_SWITCH:	return "%8d";
		case COL_DAILY_ID:	return "%-12.12s";
		case COL_DOWN_NODE:	return "%-12.12s";
		case COL_DEMAND_TYPE:	return "%8d";
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
public Validator[] getValidators( int col ) {
	Validator[] no_checks = new Validator[] {};
	// Data type switch must be 0, 1 or 2.
	Validator [] data_type = new Validator[] {
		Validators.isEquals( Integer.valueOf( 0 ) ),
		Validators.isEquals( Integer.valueOf( 1 ) ),
		Validators.isEquals( Integer.valueOf( 2 ) ) };
	Validator [] data_type_Validators = new Validator[] {
			Validators.or( data_type ) };
	
	switch (col) {
		case COL_ID: 			return ids;
		case COL_NAME: 			return blank;
		case COL_NODE_ID:		return ids;
		case COL_SWITCH:		return on_off_switch;
		case COL_DAILY_ID:		return ids;
		case COL_DOWN_NODE:		return blank;
		case COL_DEMAND_TYPE:	return data_type_Validators;
		default:				return no_checks;
	}
}
	
/**
Returns the data that should be placed in the JTable at the given row 
and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_InstreamFlow isf = (StateMod_InstreamFlow)_data.get(row);
	switch (col) {
		case COL_ID: 		return isf.getID();
		case COL_NAME: 		return isf.getName();
		case COL_NODE_ID:	return isf.getCgoto();
		case COL_SWITCH:	return Integer.valueOf(isf.getSwitch());
		case COL_DAILY_ID:	return isf.getCifridy();
		case COL_DOWN_NODE:	return isf.getIfrrdn();
		case COL_DEMAND_TYPE:	return Integer.valueOf(isf.getIifcom());
		default:		return "";
	}
}

/**
Returns whether the cell is editable or not.  Currently no cells are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable
or not.
@return whether the cell is editable or not.
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
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	StateMod_InstreamFlow isf = (StateMod_InstreamFlow)_data.get(row);
	switch (col) {
		case COL_ID:
			isf.setID((String)value);
			break;
		case COL_NAME:
			isf.setName((String)value);
			break;
		case COL_NODE_ID:
			isf.setCgoto((String)value);
			break;
		case COL_SWITCH:
			isf.setSwitch(((Integer)value).intValue());
			break;
		case COL_DAILY_ID:
			isf.setCifridy((String)value);
			break;
		case COL_DOWN_NODE:
			isf.setIfrrdn((String)value);
			break;
		case COL_DEMAND_TYPE:
			isf.setIifcom(((Integer)value).intValue());
			break;
	}
	super.setValueAt(value, row, col);
}

}
