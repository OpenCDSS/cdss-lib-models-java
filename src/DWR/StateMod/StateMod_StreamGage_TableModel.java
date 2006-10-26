// ----------------------------------------------------------------------------
// StateMod_StreamGage_TableModel - table model for displaying stream gage
//					station data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-08-18	J. Thomas Sapienza, RTi	Initial version.
// 2003-09-04	JTS, RTi		Changed table heading "RIVER STATION
//					ID" to "ID" and "STATION NAME" to
//					"Name".
// 2003-09-11	Steven A. Malers, RTi	Rename class from
//					StateMod_RiverStation_TableModel to
//					StateMod_StreamGage_TableModel.
// 2004-01-22	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2005-01-21	JTS, RTi		* Added the editable flag.
//					* Fleshed out setValueAt() method.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays reservoir data.
*/
public class StateMod_StreamGage_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 2;

/**
References to columns.
*/
public final static int
	COL_ID = 	0,
	COL_NAME =	1;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_StreamGage_TableModel(Vector data)
throws Exception {
	this(data, true);
}

/**
Constructor.  
@param data the data that will be displayed in the table.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_StreamGage_TableModel(Vector data, boolean editable)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_StreamGage_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;
	__editable = editable;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:	return String.class;
		case COL_NAME:	return String.class;
		default:	return String.class;
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
		case COL_ID:	return "ID";
		case COL_NAME:	return "NAME";
		default:	return " ";
	}	
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
		case COL_ID:	return "%-40s";
		case COL_NAME:	return "%-40s";
		default:	return "%-8s";
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

	StateMod_StreamGage sg = (StateMod_StreamGage)_data.elementAt(row);

	switch (col) {
		case COL_ID:	return sg.getID();
		case COL_NAME:	return sg.getName();
		default:	return "";
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
	widths[COL_ID] = 	19;
	widths[COL_NAME] = 	23;

	return widths;
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
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_StreamGage sg = (StateMod_StreamGage)_data.elementAt(row);

	switch (col) {
		case COL_ID:	
			sg.setID((String)value);
			break;
		case COL_NAME:
			sg.setName((String)value);
			break;
	}
	
	super.setValueAt(value, row, col);
}

}
