// ----------------------------------------------------------------------------
// StateMod_DelayTable_TableModel - class for displaying delay table data
//	in a jworksheet
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-09-04	SAM, RTi		Add monthlyData boolean to the
//					constructor.
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-08-25	JTS, RTi		Based on the value of 'interv' in 
//					the control file, the header for
//					the return column says whether it is
//					a percent or a fraction.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2005-03-28	JTS, RTi		Adjusted column sizes.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class displays delay table related data.
*/
public class StateMod_DelayTable_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 3;

/**
Whether the table data is editable or not.
*/
private boolean __editable = false;

/**
Return values under a delay table.
*/
private List __subDelays = null;

/**
Indicate whether the delay table is for monthly (true) or daily (false) data.
*/
private boolean __monthlyData = true;

/**
Indicates whether the return amounts are in percents (true) or fractions (false).
*/
private boolean __returnIsPercent = true;

/**
References to columns.
*/
public final static int 
	COL_ID = 		0,
	COL_DATE =		1,
	COL_RETURN_AMT =	2;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param monthlyData If true the data are for monthly delay tables.  If false, the delay tables are daily.
@param editable whether the table data is editable or not
@param returnIsPercent whether the return amounts are in percents (true) or fractions (false).
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_DelayTable_TableModel (	List data, boolean monthlyData,
					boolean editable, boolean returnIsPercent)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_DelayTable_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__monthlyData = monthlyData;
	__editable = editable;
	__returnIsPercent = returnIsPercent;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_DATE:		return Integer.class;
		case COL_RETURN_AMT:	return Double.class;
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
		// REVISIT (SAM - 2005-01-20)
		// how is this class being used with Well Depletion displays
		// in the StateMod GUI?  We might needa  flag for the header.
		case COL_ID:		
			return "DELAY\nTABLE ID";
		case COL_DATE:	
			if ( __monthlyData ) {
				return "\nMONTH";
			}
			else {	return "\nDAY";
			}
		case COL_RETURN_AMT:
			if (__returnIsPercent) {
				return "\nPERCENT";
			}
			else {
				return "\nFRACTION";
			}
		default:	
			return " ";
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
		case  COL_ID:		return "%-40s";	
		case  COL_DATE:		return "%8d";	
		case  COL_RETURN_AMT:	return "%12.6f";
		default:		return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
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

	switch (col) {
		case COL_ID:		
			StateMod_DelayTable dt = (StateMod_DelayTable)_data.get(row);
			return dt.getTableID();
		case COL_DATE:	
			return new Integer(row + 1);	
		case COL_RETURN_AMT:	
			if (__subDelays != null) {
				return __subDelays.get(row);
			}
			else {
				return new Double(0.0);
			}
		default:	
			return "";
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
	widths[COL_ID] = 		7;
	widths[COL_DATE] = 		4;
	widths[COL_RETURN_AMT] = 	8;
	return widths;
}

/**
Sets the delay table data that will be displayed for a particular delay.
@param subDelays Vector of subDelay information (month/day, pct)
*/
public void setSubDelays(List subDelays) {
	__subDelays = subDelays;
	_data = subDelays;
	if (__subDelays == null) {
		_rows = 0;
	}
	else {
		_rows = __subDelays.size();
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
	if (columnIndex > 1) {
		return true;
	}
	return false;
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
	// REVISIT (JTS - 2005-01-17)
	// not fleshed out for ID, Name
	switch (col) {
		case COL_ID:		
			break;
		case COL_DATE:
			break;
		case  COL_RETURN_AMT:	
			__subDelays.set(row, (Double)value);
	}
	super.setValueAt(value, row, col);
}

}