// StateMod_StreamEstimate_Data_TableModel - table model for displaying stream estimate station data

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

// ----------------------------------------------------------------------------
// StateMod_StreamEstimate_Data_TableModel - table model for displaying stream
//						estimate station data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-04-04	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-05	JTS, RTi		Added River node ID and Daily ID 
//					columns.
// 2007-04-27	Kurt Tometich, RTi		Added getValidators method for check
//									file and data check implementation.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This table model displays stream estimate station data.
*/
public class StateMod_StreamEstimate_Data_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateMod_Data_TableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 4;

/**
References to columns.
*/
public final static int
	COL_ID =		0,
	COL_NAME =		1,
	COL_NODE_ID = 		2,
	COL_DAILY_ID = 		3;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_StreamEstimate_Data_TableModel(List data) 
throws Exception {
	this(data, true);
}

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateMod_StreamEstimate_Data_TableModel(List data, boolean editable) {
	if (data == null) {
		_data = new Vector();
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
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;	
		case COL_NAME:		return String.class;	
		case COL_NODE_ID:	return String.class;
		case COL_DAILY_ID:	return String.class;
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
		case COL_ID:		return "\nID";
		case COL_NAME:		return "\nNAME";
		case COL_NODE_ID:	return "RIVER\nNODE ID";
		case COL_DAILY_ID:	return "DAILY\nID";
		default:		return " ";
	}	
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
	widths[COL_ID] = 	9;
	widths[COL_NAME] = 	23;
	widths[COL_NODE_ID] = 	9;
	widths[COL_DAILY_ID] = 	9;
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
		case COL_DAILY_ID:	return "%-12.12s";
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
	
	switch (col) {
	case COL_ID:		return ids;
	case COL_NAME:		return blank;	
	case COL_NODE_ID:	return ids;
	case COL_DAILY_ID:	return ids;
	default:			return no_checks;
	}
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

	StateMod_StreamEstimate bfs = (StateMod_StreamEstimate)_data.get(row);

	switch (col) {
		case COL_ID:		return bfs.getID();
		case COL_NAME:		return bfs.getName();	
		case COL_NODE_ID:	return bfs.getCgoto();
		case COL_DAILY_ID:	return bfs.getCrunidy();
		default:		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model all columns above #2 are editable.
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
	switch (col) {
		default:	break;
	}	
	
	super.setValueAt(value, row, col);
}

}
