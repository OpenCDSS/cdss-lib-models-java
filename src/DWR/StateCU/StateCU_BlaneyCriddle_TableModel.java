// ----------------------------------------------------------------------------
// StateCU_BlaneyCriddle_TableModel - Table model for displaying data for 
//	Blaney Criddle worksheets.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2005-01-24	JTS, RTi	Initial version.
// 2005-03-28	JTS, RTi	Adjusted column sizes.
// 2007-01-10   Kurt Tometich, RTi
// 							Fixed the format for the cropName to 
//							30 chars instead of 20.
// 2007-01-10	KAT, RTi	Adding new field Blaney-Criddle Method.
// 2007-03-01	SAM, RTi	Clean up code based on Eclipse feedback.
// 2007-03-04	SAM, RTi	Change method signature consistent with other code.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;

/**
This class is a table model for displaying crop char data.
*/
public class StateCU_BlaneyCriddle_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
//private final int __COLUMNS = 3;
private final int __COLUMNS = 4;
/**
Columns
*/
private final int 
	__COL_CROP_NAME = 	0,
	__COL_DAY_PCT = 	1,
	__COL_COEFF =   	2,
	__COL_BCM = 		3;

/**
This array stores, for each StateCU_BlaneyCriddle object in the _data 
Vector, whether the object has daily (true) or percentage (false) data.
*/
private boolean[] __day = null;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
An array that stores the number of the first row of data for each 
StateCU_BlaneyCriddle object to that object's position in the _data
Vector.
*/
private int[] __firstRows = null;

/**
Constructor.  This builds the Model for displaying crop char data
@param data the data that will be displayed in the table.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateCU_BlaneyCriddle_TableModel(List data) {
	this(data, true);
}

/**
Constructor.  This builds the Model for displaying crop char data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateCU_BlaneyCriddle_TableModel(List data, boolean editable) {
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
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_CROP_NAME:	return String.class;
		case __COL_DAY_PCT: 	return Integer.class;
		case __COL_COEFF:		return Double.class;
		case __COL_BCM: 		return Integer.class;
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
		case __COL_CROP_NAME:	return "CROP\nNAME";
		case __COL_DAY_PCT:		return "DAY OR\nPERCENT";
		case __COL_COEFF:		return "COEFFICIENT";
		case __COL_BCM:			return "BLANEY\nCRIDDLE\nMETHOD";
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
	tips[__COL_DAY_PCT] = 
		"<html>Day of year if Perennial (start, middle, end of month)."
		+ "<br>Percent of year if annual (5% increments).</html>";
	tips[__COL_COEFF] = "Crop coefficient";
	tips[__COL_BCM] = "Blaney-Criddle Method";

	return tips;
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
		case __COL_CROP_NAME:	return "%-30.30s";
		case __COL_DAY_PCT:		return "%8d";
		case __COL_COEFF:		return "%10.2f";
		case __COL_BCM:			return "%1d";
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
	// KTSW must be 0,1,2,3,4 or blank.
	Validator [] KTSW = new Validator[] {
		Validators.isEquals( new Integer( 0 ) ),
		Validators.isEquals( new Integer( 1 ) ),
		Validators.isEquals( new Integer( 2 ) ),
		Validators.isEquals( new Integer( 3 ) ),
		Validators.isEquals( new Integer( 4 ) ),
		Validators.isEquals( "" )};
		Validator [] ktswValidators = new Validator[] {
		Validators.or( KTSW ) };
	
	switch ( col ) {
	case __COL_CROP_NAME:	return blank;
	case __COL_DAY_PCT:		return nums;
	case __COL_COEFF:		return nums;
	case __COL_BCM:			return ktswValidators;
	default: 				return no_checks;
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

	int dataPos = lookupVectorPositionForRow(row);

	StateCU_BlaneyCriddle bc = (StateCU_BlaneyCriddle)_data.get(dataPos);
	
	int num = row - __firstRows[dataPos];

	switch (col) {
		case __COL_CROP_NAME:	 return bc.getName();
		case __COL_DAY_PCT:
			if (__day[dataPos]) {
				return new Integer(bc.getNckcp(num));
			}
			else {
				return new Integer(bc.getNckca(num));
			}
		case __COL_COEFF:
			if (__day[dataPos]) {
				return new Double(bc.getCkcp(num));
			}
			else {
				return new Double(bc.getCkca(num));
			}
		case __COL_BCM:		return new Integer(bc.getKtsw());
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
	widths[__COL_CROP_NAME] = 16;	
	widths[__COL_DAY_PCT] = 6;	
	widths[__COL_COEFF] = 10;
	widths[__COL_BCM] = 6;
	
	return widths;
}

/**
Sets up internal arrays.
@param data the Vector of data (non-null) that will be displayed in the table model.
*/
private void initialize(List data) {
	int size = data.size();
	__firstRows = new int[size];
	__day = new boolean[size];
	
	int row = 0;
	StateCU_BlaneyCriddle bc = null;
	
	for (int i = 0; i < size; i++) {
		bc = (StateCU_BlaneyCriddle)data.get(i);

		__firstRows[i] = row;

		if (bc.getFlag().equalsIgnoreCase("Percent")) {
			row += 21;
			__day[i] = false;
		}
		else {
			row += 25;
			__day[i] = true;
		}
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
@return the number of the object in the _data Vector which has its data at
the specified row.
*/
private int lookupVectorPositionForRow(int row) {
	for (int i = 0; i < __firstRows.length; i++) {
		if (row < __firstRows[i]) {
			return (i - 1);
		}
	}
	return (__firstRows.length - 1);
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

	StateCU_BlaneyCriddle bc = (StateCU_BlaneyCriddle)_data.get(dataPos);
	
	int num = row - __firstRows[dataPos];

	switch (col) {
		case __COL_CROP_NAME:
			bc.setName((String)value);
			break;
		case __COL_DAY_PCT:
			int ival = ((Integer)value).intValue();
			bc.setCurvePosition(num, ival);
			break;
		case __COL_COEFF:
			double dval = ((Double)value).doubleValue();
			bc.setCurveValue(num, dval);
			break;
		case __COL_BCM:
			int bcmval = ((Integer)value).intValue();
			bc.setKtsw( bcmval );
			break;
	}

	super.setValueAt(value, row, col);	
}

}