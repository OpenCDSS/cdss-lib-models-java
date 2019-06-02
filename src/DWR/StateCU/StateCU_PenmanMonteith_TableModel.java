// StateCU_PenmanMonteith_TableModel - table model for displaying Penman-Monteith crop coefficients data.

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

package DWR.StateCU;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class is a table model for displaying Penman-Monteith crop coefficients data.
*/
@SuppressWarnings("serial")
public class StateCU_PenmanMonteith_TableModel 
extends JWorksheet_AbstractRowTableModel<StateCU_PenmanMonteith> implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
//private final int __COLUMNS = 3;
private final int __COLUMNS = 4;
/**
Columns
*/
private final int 
	__COL_CROP_NAME = 0,
	__COL_GROWTH_STAGE = 1,
	__COL_PERCENT = 2,
	__COL_COEFF = 3;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
An array that stores the number of the first row of data for each 
StateCU_PenmanMonteith object to that object's position in the _data list.
An additional level of lookup is required to determine the growth stage for data lookup.
*/
private int[] __cropFirstRows = null;

/**
Constructor.  This builds the model for displaying crop char data
@param data the data that will be displayed in the table.
@throws Exception if an invalid data.
*/
public StateCU_PenmanMonteith_TableModel(List<StateCU_PenmanMonteith> data) {
	this(data, true);
}

/**
Constructor.  This builds the Model for displaying crop char data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
@throws Exception if an invalid data.
*/
public StateCU_PenmanMonteith_TableModel(List<StateCU_PenmanMonteith> data, boolean editable) {
	if (data == null) {
		_rows = 0;
	}
	else {
		initialize(data);
	}

	_data = data;
	__editable = editable;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_CROP_NAME: return String.class;
		case __COL_GROWTH_STAGE: return Integer.class;
		case __COL_PERCENT: return Double.class;
		case __COL_COEFF: return Double.class;
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
		case __COL_CROP_NAME: return "CROP\nNAME";
		case __COL_GROWTH_STAGE: return "GROWTH\nSTAGE";
		case __COL_PERCENT: return "\nPERCENT";
		case __COL_COEFF: return "\nCOEFFICIENT";
	}
	return " ";
}

/**
Returns the tool tips for the columns.
@return the tool tips for the columns.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		tips[i] = null;
	}

	tips[__COL_CROP_NAME] = "Crop name";
	tips[__COL_GROWTH_STAGE] = "Growth stage.";
	tips[__COL_PERCENT] = 
		"<html>Time from start of growth to effective cover (%) or number of days after effective cover.</html>";
	tips[__COL_COEFF] = "Crop coefficient";

	return tips;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case __COL_CROP_NAME: return "%-30.30s";
		case __COL_GROWTH_STAGE: return "%1d";
		case __COL_PERCENT: return "%5.3f";
		case __COL_COEFF: return "%10.3f";
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
public Validator[] getValidators(int col) {
	Validator[] no_checks = new Validator[] {};

	switch ( col ) {
		case __COL_CROP_NAME: return blank;
		case __COL_GROWTH_STAGE: return blank;
		case __COL_PERCENT: return nums;
		case __COL_COEFF: return nums;
		default: return no_checks;
	}
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

	// Position of the data object...
	int dataPos = lookupVectorPositionForRow(row);

	StateCU_PenmanMonteith pm = (StateCU_PenmanMonteith)_data.get(dataPos);
	
	// Row position in the data object...
	int num = row - __cropFirstRows[dataPos];
	// Which growth stage (0+ index)
	int igs = num/StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
	// Which value in the growth stage
	int ipos = num - igs*StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();

	switch (col) {
		case __COL_CROP_NAME:
			return pm.getName();
		case __COL_GROWTH_STAGE:
			return new Integer(igs + 1);
		case __COL_PERCENT:
			return new Double(pm.getKcday(igs, ipos));
		case __COL_COEFF:
			return new Double(pm.getKcb(igs, ipos));
	}
	return "";
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
	widths[__COL_CROP_NAME] = 22;
	widths[__COL_GROWTH_STAGE] = 6;
	widths[__COL_PERCENT] = 6;
	widths[__COL_COEFF] = 10;
	
	return widths;
}

/**
Sets up internal arrays.
@param data the list of data (non-null) that will be displayed in the table model.
*/
private void initialize(List<StateCU_PenmanMonteith> data)
{
	int size = data.size();
	__cropFirstRows = new int[size];
	
	int row = 0;
	StateCU_PenmanMonteith kpm;
	for (int i = 0; i < size; i++) {
		kpm = data.get(i);
		__cropFirstRows[i] = row;
		// The number of rows per crop is the number of growth stages times the number of values per stage
		row += kpm.getNGrowthStages()*StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
	}
	_rows = row;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}
	return true;
}

/**
Given a row number in the table, returns the object in the _data Vector that
has data displayed at that row.
@param row the number of the row in the table.
@return the number of the object in the _data Vector which has its data at the specified row.
*/
private int lookupVectorPositionForRow(int row) {
	for (int i = 0; i < __cropFirstRows.length; i++) {
		if (row < __cropFirstRows[i]) {
			return (i - 1);
		}
	}
	return (__cropFirstRows.length - 1);
}

/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	int dataPos = lookupVectorPositionForRow(row);

	StateCU_PenmanMonteith pm = _data.get(dataPos);
	
	// Row position in the data object...
	int num = row - __cropFirstRows[dataPos];
	// Which growth stage (0+ index)
	int igs = num/StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
	// Which value in the growth stage
	int ipos = num - igs*StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();

	switch (col) {
		case __COL_CROP_NAME:
			pm.setName((String)value);
			break;
		case __COL_GROWTH_STAGE:
			/* TODO SAM 2010-03-31 Not editable...
			int gsval = ((Integer)value).intValue();
			pm.setKtsw( gsval );
			*/
			break;
		case __COL_PERCENT:
			double percent = ((Double)value).doubleValue();
			pm.setCurvePosition(igs, ipos, percent);
			break;
		case __COL_COEFF:
			double coeff = ((Double)value).doubleValue();
			pm.setCurveValue(igs, ipos, coeff);
			break;
	}

	super.setValueAt(value, row, col);	
}

}
