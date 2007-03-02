// ----------------------------------------------------------------------------
// StateMod_StreamEstimateCoefficients_Data_TableModel - table model for 
//	displaying stream estimate station coefficients data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-04-05	J. Thomas Sapienza, RTi	Initial version.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays stream estimate station coefficients data.
*/
public class StateMod_StreamEstimateCoefficients_Data_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 6;

/**
References to columns.
*/
public final static int
	COL_ID = 		0,
	COL_STREAM_NAME =	1,
	COL_UPSTREAM_GAGE =	2,
	COL_GAIN_TERM_PRO = 	3,
	COL_GAIN_TERM_WT =	4,
	COL_GAIN_TERM_GAGE_ID =	5;

/**
Whether data are editable or not.
*/
private boolean __editable = true;

/**
The data displayed in the table (calculated by setupData()).
*/
private Vector[] __data = null;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateMod_StreamEstimateCoefficients_Data_TableModel(Vector data,
boolean editable) {
	if (data == null) {
		_data = new Vector();
	}
	else {
		_data = data;
	}
	_rows = _data.size();

	setupData(_data);

	__editable = editable;
}

/**
From AbstractTableModel; returns the class of the data stored in a given
column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:			return String.class;
		case COL_STREAM_NAME:		return Double.class;
		case COL_UPSTREAM_GAGE:		return String.class;
		case COL_GAIN_TERM_PRO:		return Double.class;
		case COL_GAIN_TERM_WT:		return Double.class;
		case COL_GAIN_TERM_GAGE_ID:	return String.class;
		default:			return String.class;
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
		case COL_ID:			return "\n\nID";
		case COL_STREAM_NAME:		return "\nSTREAM\nTERM";
		case COL_UPSTREAM_GAGE:		return "\nUPSTREAM\nTERM GAGE";
		case COL_GAIN_TERM_PRO:		
			return "GAIN TERM\nPRORATION\nFACTOR";
		case COL_GAIN_TERM_WT:		return "\nGAIN TERM\nWEIGHT";
		case COL_GAIN_TERM_GAGE_ID:	return "\nGAIN TERM\nGAGE ID";
		default:			return " ";
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
		case COL_ID:			return "%-12.12s";
		case COL_STREAM_NAME:		return "%8.1f";
		case COL_UPSTREAM_GAGE:		return "%-12.12s";
		case COL_GAIN_TERM_PRO:		return "%8.1f";
		case COL_GAIN_TERM_WT:		return "%8.1f";
		case COL_GAIN_TERM_GAGE_ID:	return "%-12.12s";
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

	return __data[col].elementAt(row);
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
	widths[COL_ID] = 		9;
	widths[COL_STREAM_NAME] = 	8;
	widths[COL_UPSTREAM_GAGE] = 	8;
	widths[COL_GAIN_TERM_PRO] = 	8;
	widths[COL_GAIN_TERM_WT] = 	8;
	widths[COL_GAIN_TERM_GAGE_ID] =	9;
	return widths;
}

/**
Returns whether the cell at the given position is editable or not.  
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
	switch (col) {
		case COL_ID:
		case COL_STREAM_NAME:
		case COL_UPSTREAM_GAGE:
		case COL_GAIN_TERM_PRO:
		case COL_GAIN_TERM_WT:
		case COL_GAIN_TERM_GAGE_ID:
		default:
			break;
	}	
	super.setValueAt(value, row, col);
}

/**
Sets up the data to be displayed in the table.
@param data a Vector of StateMod_StreamEstimate_Coefficients objects from 
which the data to be be displayed in the table will be gathered.
*/
private void setupData(Vector data) {
	int num = 0;
	int size = data.size();
	StateMod_StreamEstimate_Coefficients coeff = null;
	__data = new Vector[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector();
	}

	String id = null;
	int rowCount = 0;
	int M = 0;
	int N = 0;
	for (int i = 0; i < size; i++) {
		coeff = (StateMod_StreamEstimate_Coefficients)data.elementAt(i);
		id = coeff.getID();
		M = coeff.getM();
		N = coeff.getN();
		num = M < N ? N : M;
		for (int j = 0; j < num; j++) {
			__data[COL_ID].add(id);
	
			if (j < N) {
				__data[COL_STREAM_NAME].add(new Double(
					coeff.getCoefn(j)));
				__data[COL_UPSTREAM_GAGE].add(coeff.getUpper(
					j));
			}
			else {
				__data[COL_STREAM_NAME].add(new Double(-999));
				__data[COL_UPSTREAM_GAGE].add("");
			}

			if (j < M) {
				__data[COL_GAIN_TERM_PRO].add(new Double(
					coeff.getProratnf()));
				__data[COL_GAIN_TERM_WT].add(new Double(
					coeff.getCoefm(j)));
				__data[COL_GAIN_TERM_GAGE_ID].add(
					coeff.getFlowm(j));
			}
			else {
				__data[COL_GAIN_TERM_PRO].add(new Double(-999));
				__data[COL_GAIN_TERM_WT].add(new Double(-999));
				__data[COL_GAIN_TERM_GAGE_ID].add("");
			}
			rowCount++;
		}
	}
	_rows = rowCount;
}

}
