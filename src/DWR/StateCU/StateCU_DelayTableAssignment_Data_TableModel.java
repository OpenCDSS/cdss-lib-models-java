// ----------------------------------------------------------------------------
// StateCU_DelayTableAssignment_Data_TableModel - Table model for displaying 
//	data for delay table assignment worksheets.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2005-01-24	J. Thomas Sapienza, RTi	Initial version.
// 2005-03-29	JTS, RTi		Updated to actually work.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class is a table model for displaying delay table data.
*/
public class StateCU_DelayTableAssignment_Data_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 3;

/**
Columns
*/
private final int 
	__COL_ID = 	0,
	__COL_DELAY_ID = 1,
	__COL_PERCENT = 2;

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
private List __rowMap = null;

/**
Array of Vectors, each of which holds the data for one of the columns in the
table.  Since the data cannot be pulled out from the data objects directly, 
this is done to make display efficient.
*/
private List[] __data = null;

/**
Constructor.  This builds the Model for displaying delay table data
@param data the data that will be displayed in the table.
*/
public StateCU_DelayTableAssignment_Data_TableModel(List data) {
	this(data, true);
}

/**
Constructor.  This builds the Model for displaying delay table data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateCU_DelayTableAssignment_Data_TableModel(List data, boolean editable) {
	if (data == null) {
		data = new Vector();
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
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_ID: 		return String.class;
		case __COL_DELAY_ID:	return String.class;
		case __COL_PERCENT:	return Double.class;
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
		case __COL_ID:		return "CU\nLOCATION ID";
		case __COL_DELAY_ID:	return "DELAY\nTABLE ID";
		case __COL_PERCENT:	return "\nPERCENT";
	}
	return " ";
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
	widths[__COL_DELAY_ID] = 7;
	widths[__COL_PERCENT] = 7;	
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
		case __COL_ID:		return "%-20.20s";
		case __COL_DELAY_ID:	return "%-20.20s";
		case __COL_PERCENT:	return "%10.2f";
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
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators( int col ) {
	Validator[] no_checks = new Validator[] {};
	
	// TODO KAT 2007-04-12 Add checks here..
	return no_checks;
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
private void setupData() {
	int num = 0;
	int size = _data.size();
	StateCU_DelayTableAssignment dt = null;
	String id = null;
	__data = new List[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector();
	}

	__rowMap = new Vector();

	double total = 0;
	int rowCount = 0;
	for (int i = 0; i < size; i++) {
		total = 0;
		dt = (StateCU_DelayTableAssignment)_data.get(i);
		id = dt.getID();
		num = dt.getNumDelayTables();
		for (int j = 0; j < num; j++) {
			__data[__COL_ID].add(id);
			__data[__COL_DELAY_ID].add(dt.getDelayTableID(j));
			__data[__COL_PERCENT].add(new Double(dt.getDelayTablePercent(j)));
			total += dt.getDelayTablePercent(j);
			__rowMap.add(new Integer(rowCount));
			rowCount++;
		}

		__data[__COL_ID].add(id);
		__data[__COL_DELAY_ID].add("TOTAL");
		__data[__COL_PERCENT].add(new Double(total));

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
