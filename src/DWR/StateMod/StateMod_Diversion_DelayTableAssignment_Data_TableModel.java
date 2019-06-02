// StateMod_Diversion_DelayTableAssignment_Data_TableModel - Table model for displaying data for delay table assignment worksheets.

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
// StateMod_Diversion_DelayTableAssignment_Data_TableModel - Table model for 
//	displaying data for delay table assignment worksheets.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2005-03-30	J. Thomas Sapienza, RTi	Initial version.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for displaying delay table data.
*/
@SuppressWarnings("serial")
public class StateMod_Diversion_DelayTableAssignment_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_Diversion> {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 4;

/**
Columns
*/
private final int 
	__COL_ID = 	0,
	__COL_NODE_ID = 1,
	__COL_PERCENT = 2,
	__COL_DELAY_ID = 3;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
Whether to show rows with "TOTAL" values.
*/
private boolean __showTotals = true;

/**
The worksheet that this model is displayed in.  
*/
private JWorksheet __worksheet = null;

/**
A Vector that maps rows in the display when totals are NOT being shown to rows
in the overall data Vectors.  Used to make switching between displays with and
without totals relatively efficient.  See getValueAt() and setupData().
*/
private List<Integer> __rowMap = null;

/**
Array of Vectors, each of which holds the data for one of the columns in the
table.  Since the data cannot be pulled out from the data objects directly, 
this is done to make display efficient.
*/
private List<Object>[] __data = null;

/**
Constructor.  This builds the Model for displaying delay table data
@param data the data that will be displayed in the table.
*/
public StateMod_Diversion_DelayTableAssignment_Data_TableModel(List<StateMod_Diversion> data) {
	this(data, true);
}

/**
Constructor.  This builds the Model for displaying delay table data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateMod_Diversion_DelayTableAssignment_Data_TableModel(List<StateMod_Diversion> data, boolean editable) {
	if (data == null) {
		data = new Vector<StateMod_Diversion>();
		_rows = 0;
	}

	_data = data;
	__editable = editable;
	setupData();
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_ID: 		return String.class;
		case __COL_NODE_ID:	return String.class;
		case __COL_PERCENT:	return Double.class;
		case __COL_DELAY_ID:	return String.class;
	}
	return String.class;
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
@param columnIndex the position for which to return the column name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case __COL_ID:		
			return "\n\n\nDIVERSION\nID";
		case __COL_NODE_ID:	
			return "RIVER\nNODE ID\nRECEIVING\nRETURN\nFLOW";
		case __COL_PERCENT:	
			return "\n\n\n\nPERCENT";
		case __COL_DELAY_ID:	
			return "\n\n\nDELAY\nTABLE ID";
	}
	return " ";
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
		case __COL_ID:		return "%-12.12s";
		case __COL_NODE_ID:	return "%-12.12s";
		case __COL_PERCENT:	return "%10.2f";
		case __COL_DELAY_ID:	return "%-12.12s";
	}
	return "%8d";
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	// make sure the row numbers are never sorted ...
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	
	if (!__showTotals) {
		row = ((Integer)__rowMap.get(row)).intValue();
	}
	return __data[col].get(row);		
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
	widths[__COL_ID] = 9;	
	widths[__COL_NODE_ID] = 9;
	widths[__COL_PERCENT] = 7;	
	widths[__COL_DELAY_ID] = 7;
	return widths;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable
or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}
	return false;
}

/**
Sets up the data to be displayed in the table.
*/
@SuppressWarnings("unchecked")
private void setupData() {
	int num = 0;
	int size = _data.size();
	StateMod_Diversion dt = null;
	String id = null;
	__data = new List[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector<Object>();
	}

	__rowMap = new Vector<Integer>();

	double total = 0;
	int rowCount = 0;
	StateMod_ReturnFlow rf = null;
	List<StateMod_ReturnFlow> returnFlows = null;
	for (int i = 0; i < size; i++) {
		total = 0;
		dt = _data.get(i);
		id = dt.getID();

		num = dt.getNrtn();		
		returnFlows = dt.getReturnFlows();
		for (int j = 0; j < num; j++) {
			rf = returnFlows.get(j);
			__data[__COL_ID].add(id);
			__data[__COL_NODE_ID].add(rf.getCrtnid());
			__data[__COL_PERCENT].add(new Double(rf.getPcttot()));
			__data[__COL_DELAY_ID].add("" + rf.getIrtndl());
			total += rf.getPcttot();
			__rowMap.add(new Integer(rowCount));
			rowCount++;
		}

		__data[__COL_ID].add(id);
		__data[__COL_NODE_ID].add("TOTAL");
		__data[__COL_PERCENT].add(new Double(total));
		__data[__COL_DELAY_ID].add("");

		rowCount++;
	}
	_rows = rowCount;
}

/**
Sets whether to show lines with totals.  setJWorksheet() must have been called
with a non-null worksheet prior to this method.  The worksheet will be updated
instantly.
@param showTotals whether to show lines with totals in the worksheet.
*/
public void setShowTotals(boolean showTotals) {
	__showTotals = showTotals;
	_sortOrder = null;

	if (__showTotals) {
		_rows = __data[__COL_ID].size();
	}
	else {
		_rows = __rowMap.size();
	}
	__worksheet.refresh();
}		

public void setValueAt(Object value, int row, int col) {
}

/**
Sets the worksheet that this model appears in.
@param worksheet the worksheet the model appears in.
*/
public void setJWorksheet(JWorksheet worksheet) {
	__worksheet = _worksheet;
}

}
