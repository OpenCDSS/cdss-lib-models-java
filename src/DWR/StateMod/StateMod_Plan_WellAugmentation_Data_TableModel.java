// StateMod_Plan_WellAugmentation_Data_TableModel - This class displays well augmentation plan data.

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

//import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class displays well augmentation plan data.
*/
@SuppressWarnings("serial")
public class StateMod_Plan_WellAugmentation_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_Plan_WellAugmentation> implements StateMod_Data_TableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 4;

/**
Whether the table data is editable or not.
*/
private boolean __editable = false;

/**
The worksheet that this model is displayed in.  
*/
//private JWorksheet __worksheet = null;

/**
A Vector that maps rows in the display when totals are NOT being shown to rows
in the overall data Vectors.  Used to make switching between displays with and
without totals relatively efficient.  See getValueAt() and setupData().
*/
//private List __rowMap = null;

/**
The List of data that will actually be shown in the table.
*/
//private List[] __data = null;

/**
References to columns.
*/
public final static int 
	COL_PLAN_ID = 0,
	COL_WELL_RIGHT_ID = 1,
	COL_WELL_STRUCTURE_ID = 2,
	COL_COMMENT = 3;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Plan_WellAugmentation_Data_TableModel ( List<StateMod_Plan_WellAugmentation> data, boolean editable )
throws Exception {
	if (data == null) {
		_data = new Vector<StateMod_Plan_WellAugmentation>();
	}
	else {
		_data = data;
	}
	_rows = data.size();

	__editable = editable;

	//setupData(data);
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_PLAN_ID: return String.class;
		case COL_WELL_RIGHT_ID: return String.class;
		case COL_WELL_STRUCTURE_ID: return String.class;
		case COL_COMMENT: return String.class;
		default: return String.class;
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
		// TODO (SAM - 2005-01-20) how is this class being used with Well Depletion displays
		// in the StateMod GUI?  We might need a flag for the header.
		case COL_PLAN_ID:		
			return "PLAN\nID";
		case COL_WELL_RIGHT_ID:	
			return "WELL RIGHT\nID";
		case COL_WELL_STRUCTURE_ID:		
			return "WELL\nID";
		case COL_COMMENT:		
			return "\nCOMMENT";
		default:	
			return " ";
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
	widths[COL_PLAN_ID] = 8;
	widths[COL_WELL_RIGHT_ID] = 8;
	widths[COL_WELL_STRUCTURE_ID] = 8;
	widths[COL_COMMENT] = 30;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case  COL_PLAN_ID: return "%-12.12s";	
		case  COL_WELL_RIGHT_ID: return "%-12.12s";	
		case  COL_WELL_STRUCTURE_ID: return "%-12.12s";
		case  COL_COMMENT: return "%-s";
		default: return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators( int col ) 
{
	// TODO KAT 2007-04-16
	// Need to add general validators but don't know
	// what data is going to be checked.
	Validator[] no_checks = new Validator[] {};
	return no_checks;
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

	StateMod_Plan_WellAugmentation wellAug = (StateMod_Plan_WellAugmentation)_data.get(row);
	switch (col) {
		case COL_PLAN_ID: return wellAug.getID();
		case COL_WELL_RIGHT_ID: return wellAug.getCistatW();
		case COL_WELL_STRUCTURE_ID: return wellAug.getCistatS();
		case COL_COMMENT: return wellAug.getComment();
		default: return "";
	}
	/*
	if (!__showTotals) {
		row = ((Integer)__rowMap.get(row)).intValue();
	}
	return __data[col].get(row);
	*/
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
Sets up the data to be displayed in the table.
@param data a Vector of StateMod_DelayTable objects from which the data to b
be displayed in the table will be gathered.
*/
/*
private void setupData(List data) {
	int num = 0;
	int size = data.size();
	StateMod_DelayTable dt = null;
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
		dt = (StateMod_DelayTable)data.get(i);
		id = dt.getID();
		num = dt.getNdly();
		for (int j = 0; j < num; j++) {
			__data[COL_PLAN_ID].add(id);
			__data[COL_RIVER_NODE_ID].add(Integer.valueOf(j + 1));
			__data[COL_PERCENT_RETURN].add( Double.valueOf(dt.getRet_val(j)));
			total += dt.getRet_val(j);
			__rowMap.add(Integer.valueOf(rowCount));
			rowCount++;
		}

		__data[COL_PLAN_ID].add("TOTAL " + id);
		__data[COL_RIVER_NODE_ID].add(Integer.valueOf(-999));
		__data[COL_PERCENT_RETURN].addDouble.valueOf(total));

		rowCount++;
	}
	_rows = rowCount;
}
*/

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
	
	StateMod_Plan_WellAugmentation wellAug = _data.get(row);
	
	switch (col) {
		case COL_PLAN_ID:
			wellAug.setID((String)value);
			break;
		case COL_WELL_RIGHT_ID:
			wellAug.setCistatW((String)value);
			break;
		case COL_WELL_STRUCTURE_ID:
			wellAug.setCistatS((String)value);
			break;
		case COL_COMMENT:
			wellAug.setComment((String)value);
			break;
	}
	super.setValueAt(value, row, col);
}

/**
Sets whether to show lines with totals.  setJWorksheet() must have been called
with a non-null worksheet prior to this method.  The worksheet will be updated instantly.
@param showTotals whether to show lines with totals in the worksheet.
*/
/*
public void setShowTotals(boolean showTotals) {
	__showTotals = showTotals;
	_sortOrder = null;

	if (__showTotals) {
		_rows = __data[COL_PLAN_ID].size();
	}
	else {
		_rows = __rowMap.size();
	}
	__worksheet.refresh();
}
*/	

/**
Sets the worksheet that this model appears in.
@param worksheet the worksheet the model appears in.
*/
/*
public void setJWorksheet(JWorksheet worksheet) {
	__worksheet = _worksheet;
}
*/

}
