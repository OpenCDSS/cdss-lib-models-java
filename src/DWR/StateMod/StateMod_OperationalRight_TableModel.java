// StateMod_OperationalRight_TableModel - class to display operational right data

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

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class displays operational right data, in particular the list of rights in the StateMod GUI
editor, and also the list of intervening structures (in which case the first two columns are hidden).
*/
@SuppressWarnings("serial")
public class StateMod_OperationalRight_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_OperationalRight> {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 4;

/**
References to columns.
*/
public final static int
	COL_ID = 0,
	COL_NAME = 1,
	COL_TYPE = 2,
	COL_STRUCTURE_ID = 3;

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
The parent diversion under which the right and return flow is stored.
*/
private StateMod_OperationalRight __parentOperationalRight = null;

/**
A 10-element list of an Operational Right's intern data (identifiers).
*/
private List<String> __interns = null;

/**
Constructor.  This builds the Model for displaying the diversion data.
@param data the data that will be displayed in the table.
@param editable whether the gui data are editable or not.
*/
public StateMod_OperationalRight_TableModel(List<StateMod_OperationalRight> data, boolean editable)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data list passed to " 
			+ "StateMod_OperationalRight_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID: return String.class;
		case COL_NAME: return String.class;
		case COL_TYPE: return String.class;
		case COL_STRUCTURE_ID: return String.class;
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
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_ID: return "ID";
		case COL_NAME: return "NAME";
		case COL_TYPE: return "TYPE";
		case COL_STRUCTURE_ID: return "STRUCTURE ID";
	}
	return " ";
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_ID: return "%-40s";
		case COL_NAME: return "%-40s";
		case COL_TYPE: return "%s";
		case COL_STRUCTURE_ID: return "%-40s";
	}
	return "%8s";
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
	StateMod_OperationalRight smo = _data.get(row);
	switch (col) {
		case COL_ID: return smo.getID();
		case COL_NAME: return smo.getName();
		case COL_TYPE: return "" + smo.getItyopr();
		case COL_STRUCTURE_ID:
			return "N/A";
			//return (String)__interns.elementAt(row);		
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
	widths[COL_ID] = 8;
	widths[COL_NAME] = 18;
	widths[COL_TYPE] = 4;
	widths[COL_STRUCTURE_ID] =	15;

	return widths;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable or not.
@return whether the cell is editable or not.
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
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	switch (col) {
		case COL_STRUCTURE_ID:	
			__interns.set(row, (String)value);
			__parentOperationalRight.setInterns(__interns);
		break;
	}

	super.setValueAt(value, row, col);	
}	

/**
Sets the parent diversion under which the right and return flow data is stored.
@param parent the parent diversion.
*/
public void setParentOperationalRight(StateMod_OperationalRight parent) {
	__parentOperationalRight = parent;
}

/**
The list of intern data associated with a specific Operational Right.
@param interns a list of 10 elements containing an Operational Right's intern data.
*/
public void setInterns(List<String> interns) {
	_rows = 10;
	__interns = interns;
}

}
