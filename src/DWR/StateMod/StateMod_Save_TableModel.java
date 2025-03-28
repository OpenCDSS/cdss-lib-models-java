// StateMod_Save_TableModel - table model for displaying the save table.

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

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.DataSetComponent;

/**
This table model displays response data.
*/
@SuppressWarnings("serial")
public class StateMod_Save_TableModel 
extends JWorksheet_AbstractRowTableModel<Object> {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 2;

/**
References to columns.
*/
public final static int 
	COL_DESC = 0,
	COL_FILE = 1;
	
/**
The numbers of the components stored in the worksheet at each row.
*/
private int[] __data;

/**
The dataset for which to display information in the worksheet.
*/
private StateMod_DataSet __dataset;

/**
Constructor.  
@param dataset the dataset for which to display information in the worksheet.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Save_TableModel(StateMod_DataSet dataset)
throws Exception {
	__dataset = dataset;

	// get the array of the ints that refer to the groups in
	// the data set.
	int[] groups = __dataset.getComponentGroupNumbers();

	List<Integer> ints = new Vector<Integer>();
	DataSetComponent dsc = null;
	List<DataSetComponent> v = null;

	// Go through each of the groups and get their data out.  Group data
	// consists of the DataSetComponents the group contains.  For each
	// of the group's DataSetComponents, if it has data, then add its
	// component type to the accumulation vector.
	for (int i = 0; i < groups.length; i++) {
	
		dsc = __dataset.getComponentForComponentType(groups[i]);
		@SuppressWarnings("unchecked")
		List<DataSetComponent> compList = (List<DataSetComponent>)dsc.getData();
		v = compList;
		if (v == null) {
			v = new Vector<DataSetComponent>();
		}
		for (int j = 0; j < v.size(); j++) {
			dsc = (DataSetComponent)v.get(j);
			// get the dirty components -- they can be saved 
			// (or not).
			if (dsc.isDirty()) {
				ints.add(Integer.valueOf(dsc.getComponentType()));
			}
		}
	}
	
	// now transfer the numbers of the DataSetComponents with data into
	// an int array from the Vector.
	__data = new int[ints.size()];
	for (int i = 0; i < ints.size(); i++) {
		__data[i] = ((Integer)ints.get(i)).intValue();
	}
	
	_rows = __data.length;

}

/**
From AbstractTableModel; returns the class of the data stored in a given
column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_DESC:	return String.class;
		case COL_FILE:	return String.class;
		default:	return String.class;
	}
}

/**
From AbstractTableModel; returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
From AbstractTableModel; returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_DESC:	return "DATA SET COMPONENT";
		case COL_FILE:	return "FILE NAME";
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
		case COL_DESC:	return "%-40s";
		case COL_FILE:	return "%-40s";
		default:	return "%-8s";
	}
}

/**
Gets the number of the component for which table is displayed at the specified row.
@param row the row of the component for which to return the component number.
*/
public int getRowComponentNum(int row) {
	return __data[row];
}

/**
From AbstractTableModel; returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableModel; returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	switch (col) {
		case COL_DESC:	
			String group = __dataset.getComponentForComponentType(
				__dataset.lookupComponentGroupTypeForComponent(
				__data[row])).getComponentName();

			return group + ": " 
				+__dataset.getComponentForComponentType(
				__data[row]).getComponentName();
		case COL_FILE:
			return __dataset.getComponentForComponentType(
				__data[row]).getDataFileName();
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
	widths[COL_DESC] = 35;
	widths[COL_FILE] = 35;
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
	return false;
}

}
