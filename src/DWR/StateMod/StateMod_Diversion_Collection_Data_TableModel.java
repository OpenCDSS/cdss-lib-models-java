// StateMod_Diversion_Collection_Data_TableModel - Table model for displaying diversion collection data.

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
// StateMod_Diversion_Collection_Data_TableModel - Table model for displaying 
//	diversion collection data.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-03-30	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-11	JTS, RTi		Corrected the classes returned from
//					getColumnClass().
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for displaying diversion collection data.
*/
@SuppressWarnings("serial")
public class StateMod_Diversion_Collection_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_Diversion> {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 5;

/**
Column references.
*/
private final int 
	__COL_DIV = -1,
	__COL_ID = 0,
	__COL_YEAR = 1,
	__COL_COL_TYPE = 2,
	__COL_PART_TYPE = 3,
	__COL_PART_ID = 4;

/**
Whether the data are editable or not.
*/
private boolean __editable = false;

/**
The data displayed in the table.
*/
private List<Object>[] __data = null;

/**
Constructor.  This builds the Model for displaying diversion data
@param data the data that will be displayed in the table.
*/
public StateMod_Diversion_Collection_Data_TableModel(List<StateMod_Diversion> data) {
	this(data, false);
}

/**
Constructor.  This builds the Model for displaying diversion data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateMod_Diversion_Collection_Data_TableModel(List<StateMod_Diversion> data, boolean editable) {
	if (data == null) {
		data = new Vector<StateMod_Diversion>();
	}
	_data = data;
	__editable = editable;

	setupData();
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_ID:		return String.class;
		case __COL_DIV:		return Integer.class;
		case __COL_YEAR:	return Integer.class;
		case __COL_COL_TYPE:	return String.class;
		case __COL_PART_TYPE:	return String.class;
		case __COL_PART_ID:	return String.class;
	}
	return String.class;
}

/**
From AbstractTableMode.  Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
From AbstractTableMode.  Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case __COL_ID:		return "DIVERSION\nID";
		case __COL_DIV:		return "\nDIVISION";
		case __COL_YEAR:	return "\nYEAR";
		case __COL_COL_TYPE:	return "COLLECTION\nTYPE";
		case __COL_PART_TYPE:	return "PART\nTYPE";
		case __COL_PART_ID:	return "PART\nID";
	}

	return " ";
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
		case __COL_ID:		return "%-12.12s";
		case __COL_DIV:		return "%8d";
		case __COL_YEAR:	return "%8d";
		case __COL_COL_TYPE:	return "%-12.12s";
		case __COL_PART_TYPE:	return "%-12.12s"; 
		case __COL_PART_ID:	return "%-12.12s";
	}
	return "%-8s";	
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable
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
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}
	widths[__COL_ID] =		8;
//	widths[__COL_DIV] =		6;
	widths[__COL_YEAR] =		5;
	widths[__COL_COL_TYPE] =	9;
	widths[__COL_PART_TYPE] =	5;
	widths[__COL_PART_ID] =		6;
	return widths;
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
Sets up the data Vectors to display the diversion collection data in the
GUI.
*/
@SuppressWarnings("unchecked")
private void setupData() {
	int[] years = null;
	int len = 0;
	int size = _data.size();
	int size2 = 0;
	StateMod_Diversion l = null;
	String colType = null;
	String id = null;
	String partType = null;
	List<String> ids = null;
	__data = new List[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector<Object>();
	}
	
	int rows = 0;
	
	for (int i = 0; i < size; i++) {
		l = _data.get(i);
		id = l.getID();
//		div = new Integer(l.getCollectionDiv());

		years = l.getCollectionYears();
		colType = l.getCollectionType();
		partType = l.getCollectionPartType();

		if (years == null) {
			len = 0;
		}
		else {
			len = years.length;
		}

		for (int j = 0; j < len; j++) {
			ids = l.getCollectionPartIDs(years[j]);
			if (ids == null) {
				size2 = 0;
			}
			else {
				size2 = ids.size();
			}

			for (int k = 0; k < size2; k++) {
				__data[__COL_ID].add(id);
//				__data[__COL_DIV].add(div);
				__data[__COL_YEAR].add(new Integer(years[j]));
				__data[__COL_COL_TYPE].add(colType);
				__data[__COL_PART_TYPE].add(partType);
				__data[__COL_PART_ID].add(ids.get(k));
				rows++;
			}
		}
	}
	_rows = rows;
}

/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col) {
	switch (col) {
	}	

	super.setValueAt(value, row, col);	
}	

}