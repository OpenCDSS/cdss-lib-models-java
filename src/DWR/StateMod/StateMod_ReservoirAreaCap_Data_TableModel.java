// ----------------------------------------------------------------------------
// StateMod_ReservoirAreaCap_Data_TableModel - table model for displaying reservoir 
//	area/capacity/seepage data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-11	JTS, RTi		Revised so that it displays real data
//					instead of dummy data for the main
//					Reservoir display.
// 2003-06-13	JTS, RTi		* Created code for displaying the
//					  area cap and climate data
//					* Added code to handle editing
//					  data
// 2003-06-16	JTS, RTi		* Added code for the reservoir account
//					  data
//					* Added code for the reservoir right
//					  data
// 2003-06-17	JTS, RTi		Revised javadocs.
// 2003-07-17	JTS, RTi		Constructor now takes a editable flag
//					to specify whether the data should be
//					editable.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-08-16	Steven A. Malers, RTi	Update because of changes in the
//					StateMod_ReservoirClimate class.
// 2003-08-22	JTS, RTi		* Changed headings for the rights table.
//					* Changed headings for the owner account
//					  table.
//					* Added code to accomodate tables which
//					  are now using comboboxes for entering
//					  data.
// 2003-08-25	JTS, RTi		Added partner table code for saving
//					data in the reservoir climate gui.
// 2003-08-28	SAM, RTi		* Change setRightsVector() call to
//					  setRights().
//					* Update for changes in
//					  StateMod_Reservoir.
// 2003-09-18	JTS, RTi		Added ID column for reservoir
//					accounts.
// 2003-10-10	JTS, RTi		* Removed reference to parent reservoir.
//					* Added getColumnToolTips().
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Split code out of
//					StateMod_Reservoir_TableModel.
//					Change setValueAt() to support sort.
//					Define tool tips.
// 2005-01-21	JTS, RTi		Added ability to display data for either
//					one or many reservoirs.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays reservoir area capacity data.  The model can display
area capacity data for a single reservoir or for 1+ reservoirs.  The difference
is specified in the constructor and affects how many columns of data are shown.
*/
public class StateMod_ReservoirAreaCap_Data_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.  For table models that display area
capacity for a single reservoir the tables only have 3 columns.  Table models
that display area capacities for 1+ reservoirs have 4.  The variable is modified in the constructor.
*/
private int __COLUMNS = 4;

/**
References to columns.
*/
public final static int
	COL_RESERVOIR_ID = 	0,
	COL_CAPACITY =		1,
	COL_AREA =		2,
	COL_SEEPAGE =		3;
	
/**
Whether the table data is editable.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the reservoir area/cap/seepage data that will be displayed in the table.
@param editable whether the table data can be modified.
*/
public StateMod_ReservoirAreaCap_Data_TableModel(List data, boolean editable){
	if (data == null) {
		_data = new Vector();
	}
	else {
		_data = data;
	}
	_rows = data.size();

	__editable = editable;
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class getColumnClass (int col) {
	switch (col) {
		case COL_RESERVOIR_ID:	return String.class;
		case COL_CAPACITY:	return Double.class;
		case COL_AREA:		return Double.class;
		case COL_SEEPAGE:	return Double.class;
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
@param col the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int col) {
	switch (col) {
		case COL_RESERVOIR_ID:	return "RESERVOIR\nID";
		case COL_CAPACITY:	return "CONTENT\n(ACFT)";
		case COL_AREA:		return "AREA\n(ACRE)";
		case COL_SEEPAGE:	return "SEEPAGE\n(AF/M)";
		default:		return " ";
	}	
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_RESERVOIR_ID] =
		"<html>The reservoir station ID of the reservoir to "
		+ "which<br>the area capacity information belongs."
		+ "</html>";
	tips[COL_CAPACITY] = "Reservoir content (ACFT).";
	tips[COL_AREA] = "Reservoir area (ACRE).";
	tips[COL_SEEPAGE] = "Reservoir seepage (AF/M).";
	return tips;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int col) {
	switch (col) {
		case COL_RESERVOIR_ID:	return "%-12.12s";
		case COL_CAPACITY:	return "%12.1f";
		case COL_AREA:		return "%12.1f";
		case COL_SEEPAGE:	return "%12.1f";
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

	StateMod_ReservoirAreaCap ra = (StateMod_ReservoirAreaCap)_data.get(row);

	switch (col) {
		case COL_RESERVOIR_ID:	return ra.getCgoto();
		case COL_CAPACITY:	return new Double(ra.getConten());
		case COL_AREA:		return new Double(ra.getSurarea());
		case COL_SEEPAGE:	return new Double(ra.getSeepage());
		default:		return "";
	}
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];

	widths[COL_RESERVOIR_ID] = 8;
	widths[COL_CAPACITY] =	8;
	widths[COL_AREA] = 	8;
	widths[COL_SEEPAGE] = 	8;
	return widths;
}

/**
Returns whether the cell at the given position is editable.  In this
table model all columns are editable (unless the table is not editable).
@param rowIndex unused
@param col the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int col) {
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
	double dval;
	StateMod_ReservoirAreaCap ra 
		= (StateMod_ReservoirAreaCap)_data.get(row);

	switch (col) {
		case COL_RESERVOIR_ID:
			ra.setCgoto((String)value);
			break;
		case COL_CAPACITY:
			dval = ((Double)value).doubleValue();
			ra.setConten(dval);
			break;
		case COL_AREA:
			dval = ((Double)value).doubleValue();
			ra.setSurarea(dval);
			break;
		case COL_SEEPAGE:
			dval = ((Double)value).doubleValue();
			ra.setSeepage(dval);
			break;
	}	
	
	super.setValueAt(value, row, col);
}

}