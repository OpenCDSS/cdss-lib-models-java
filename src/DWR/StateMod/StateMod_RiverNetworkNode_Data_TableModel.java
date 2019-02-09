// StateMod_RiverNetworkNode_Data_TableModel - table model for displaying reservoir data

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
// StateMod_RiverNetworkNode_Data_TableModel - table model for displaying reservoir 
//	data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-08-18	J. Thomas Sapienza, RTi	Initial version.
// 2004-01-22	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2007-01-07   Kurt Tometich, RTi
//								Added new fields for the data model for
//								RiverNetworkNode.  Added Downstream Node ID
//								and Maximum Recharge Limit.  Formatted the
//								fields to match other commands.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This table model displays reservoir data.
*/
public class StateMod_RiverNetworkNode_Data_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateMod_Data_TableModel {

/**
Number of columns in the table model.
*/
	private final int __COLUMNS = 5;
/**
References to columns.
*/
public final static int
	COL_ID =	0,		// ID
	COL_NAME =	1,		// Name
	COL_COMMENT = 3,	// Comment field
	COL_CSTADN = 2,		// Downstream River Node ID
	COL_GWMAXR = 4;		// Maximum Recharge Limit (CFS)

/**
Constructor.  
@param data the data that will be displayed in the table.
*/
public StateMod_RiverNetworkNode_Data_TableModel(List data) {
	if (data == null) {
		_data = new Vector();
	}
	else {
		_data = data;
	}
	_rows = _data.size();
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_NAME:		return String.class;
		case COL_COMMENT:	return String.class;
		case COL_CSTADN:	return String.class;
		case COL_GWMAXR:	return Double.class;
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
		case COL_ID:		return "RIVER NODE ID";
		case COL_NAME:		return "STATION NAME";
		case COL_COMMENT:	return "COMMENT";
		case COL_CSTADN:	return "DOWNSTREAM \nRIVER NODE ID";
		case COL_GWMAXR:	return "MAX RECHARGE \nLIMIT (CFS)";
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
	widths[COL_ID] = 11;
	widths[COL_NAME] = 23;
	widths[COL_CSTADN] = 11;
	widths[COL_GWMAXR] = 11;
	widths[COL_COMMENT] = 16;
	
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
		case COL_COMMENT:	return "%-80.80s";
		case COL_CSTADN:	return "%-12.12s";
		case COL_GWMAXR:	return "%12.2f";
		default:			return "%-8s";
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
	case COL_COMMENT: 	return no_checks;		// can be blank
	case COL_CSTADN:	return no_checks;		// can be blank
	case COL_GWMAXR:	return nums;
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

	StateMod_RiverNetworkNode r = (StateMod_RiverNetworkNode)_data.get(row);
	
	switch (col) {
		case COL_ID:	return r.getID();
		case COL_NAME:	return r.getName();
		case COL_COMMENT: return r.getComment();
		case COL_CSTADN:	return r.getCstadn();
		case COL_GWMAXR:	return new Double(r.getGwmaxr());
		default:	return "";
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
	return false;
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
	super.setValueAt(value, row, col);
}

}
