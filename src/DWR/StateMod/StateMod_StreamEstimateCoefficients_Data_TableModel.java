// StateMod_StreamEstimateCoefficients_Data_TableModel - table model for displaying stream estimate station coefficients data

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
// StateMod_StreamEstimateCoefficients_Data_TableModel - table model for 
//	displaying stream estimate station coefficients data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-04-05	J. Thomas Sapienza, RTi	Initial version.
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
This table model displays stream estimate station coefficients data.
*/
@SuppressWarnings("serial")
public class StateMod_StreamEstimateCoefficients_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_StreamEstimate_Coefficients> implements StateMod_Data_TableModel {

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
private List<Object>[] __data = null;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateMod_StreamEstimateCoefficients_Data_TableModel(List<StateMod_StreamEstimate_Coefficients> data, boolean editable) {
	if (data == null) {
		_data = new Vector<StateMod_StreamEstimate_Coefficients>();
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
public Class<?> getColumnClass (int columnIndex) {
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
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators( int col ) {
	Validator[] no_checks = new Validator[] {};
	
	// TODO KAT 2007-04-16 need to find out which general validators are needed here ...
	
	return no_checks;
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

	return __data[col].get(row);
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
@SuppressWarnings("unchecked")
private void setupData(List<StateMod_StreamEstimate_Coefficients> data) {
	int num = 0;
	int size = data.size();
	StateMod_StreamEstimate_Coefficients coeff = null;
	__data = new List[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector<Object>();
	}

	String id = null;
	int rowCount = 0;
	int M = 0;
	int N = 0;
	for (int i = 0; i < size; i++) {
		coeff = data.get(i);
		id = coeff.getID();
		M = coeff.getM();
		N = coeff.getN();
		num = M < N ? N : M;
		for (int j = 0; j < num; j++) {
			__data[COL_ID].add(id);
	
			if (j < N) {
				__data[COL_STREAM_NAME].add(new Double(coeff.getCoefn(j)));
				__data[COL_UPSTREAM_GAGE].add(coeff.getUpper(j));
			}
			else {
				__data[COL_STREAM_NAME].add(new Double(-999));
				__data[COL_UPSTREAM_GAGE].add("");
			}

			if (j < M) {
				__data[COL_GAIN_TERM_PRO].add(new Double(coeff.getProratnf()));
				__data[COL_GAIN_TERM_WT].add(new Double(coeff.getCoefm(j)));
				__data[COL_GAIN_TERM_GAGE_ID].add(coeff.getFlowm(j));
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
