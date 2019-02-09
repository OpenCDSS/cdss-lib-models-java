// StateMod_ReturnFlow_TableModel - Table model for displaying data in the return flow tables
// (for return flows and depletions).

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
// StateMod_ReturnFlow_TableModel - Table model for displaying data in the
//	return flow tables (for return flows and depletions).
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-10	JTS, RTi		* Added the right fields
//					* Added the return flow fields
// 2003-06-17	JTS, RTi		Return flow data is now displayable
//					and editable.
// 2003-07-17	JTS, RTi		Constructor has switch to determine
//					if data is editable or not.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-10-07	JTS, RTi		Changed 'readOnly' to 'editable'.
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-26	SAM, RTi		Split code out of the
//					StateMod_Diversion_TableModel class.
// 					Change setValueAt() to support sort.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model display data in return flow tables for use with return flows
and depletions.
*/
public class StateMod_ReturnFlow_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final static int __COLUMNS = 3;

/**
References to columns.
*/
public final static int
	COL_RIVER_NODE = 0,
	COL_RETURN_PCT = 1,
	COL_RETURN_ID = 2;

/**
Whether the table data can be edited or not.
*/
private boolean __editable = true;

/**
Whether the data are return flows or depletions. If true, the data are return
flows.  If false the data are depletions.
*/
private boolean __is_return;

/**
Dataset that contains the data.
*/
private StateMod_DataSet __dataset;

/**
Constructor.  This builds the Model for displaying the return flow data.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not.
@param is_return Specify true for return flows and false for depletions.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_ReturnFlow_TableModel (	List data, boolean editable, boolean is_return )
throws Exception {
	this(null, data, editable, is_return );
}

/**
Constructor.  This builds the Model for displaying the return flow data.
@param dataset the dataset for the data being displayed.  The control
information is used to determine whether return flows for daily or monthly.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not.
@param is_return Specify true for return flows and false for depletions.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_ReturnFlow_TableModel(StateMod_DataSet dataset, List data, boolean editable, boolean is_return )
throws Exception {
	__dataset = dataset;
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_ReturnFlow_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
	__is_return = is_return;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_RIVER_NODE:	return String.class;
		case COL_RETURN_PCT:	return Double.class;
		case COL_RETURN_ID:	return Integer.class;
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
		case COL_RIVER_NODE:	
			if ( __is_return ) {
				return "\nRIVER NODE RECEIVING RETURN FLOW";
			}
			else {	return "\nRIVER NODE BEING DEPLETED";
			}
		case COL_RETURN_PCT:
			if ( __is_return ) {
				return "\n% OF RETURN";
			}
			else {	return "\n% OF DEPLETION";
			}
		case COL_RETURN_ID:	
			if (__dataset == null ) {
				return "DELAY\nTABLE ID";
			}
			else if (__dataset.getIday() == 1) {
				return "DAILY DELAY\nTABLE ID";
			}
			else {	return "MONTHLY DELAY\nTABLE ID";
			}
		default:	return " ";
	}	
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	if ( __is_return ) {
		tips[COL_RIVER_NODE] =
		"River node ID receiving return flow.";
		tips[COL_RETURN_PCT] = 
		"% of return (0-100)";
	}
	else {	tips[COL_RIVER_NODE] =
		"River node ID being depleted.";
		tips[COL_RETURN_PCT] = 
		"% of depletion (0-100)";
	}
	tips[COL_RETURN_ID] = 
		"Delay table identifier";

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
	widths[COL_RIVER_NODE] = 31;
	widths[COL_RETURN_PCT] = 12;
	widths[COL_RETURN_ID] = 14;
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
		case COL_RIVER_NODE:	return "%-12s";	
		case COL_RETURN_PCT:	return "%12.6f";
		case COL_RETURN_ID:	return "%8d";
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
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)_data.get(row);
	switch (col) {
		case COL_RIVER_NODE:	return rf.getCrtnid();
		case COL_RETURN_PCT:	return new Double(rf.getPcttot());
		case COL_RETURN_ID:	return new Integer(rf.getIrtndl());
		default:		return "";
	}
}

/**
Returns whether the cell is editable or not.  All cells are editable, unless
the worksheet is not editable.
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
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	int index;
	String s;

	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)_data.get(row);
	switch (col) {
		case COL_RIVER_NODE:	
			/*
			index = ((String)value).indexOf(" -");
			s = null;
			if (index > -1) {
				s = ((String)value).substring(0,
					index);
			}
			else {
				s = (String)value;
			}
			rf.setCrtnid(s);
			*/
			rf.setCrtnid((String)value);
			break;
		case COL_RETURN_PCT:	
			dval = ((Double)value).doubleValue();
			rf.setPcttot(dval);
			break;
		case COL_RETURN_ID:	
			if (value instanceof String) {
				index = ((String)value).indexOf(" -");
				s = null;
				if (index > -1) {
					s = ((String)value).substring(0,
						index);
				}
				else {	s = (String)value;
				}
				rf.setIrtndl(s);
			}
			else {	if (value == null) {
					// user input a blank value --
					// just keep what was originally
					// in the table
					return;
				}
				ival = ((Integer)value).intValue();
				rf.setIrtndl(ival);
			}
			break;
	}

	super.setValueAt(value, row, col);	
}	

}
