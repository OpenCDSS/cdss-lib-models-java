// StateCU_DataSetComponent_TableModel - table model for StateCU_DataSet component to display data objects in a table

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
// StateCU_DataSetComponent_TableModel - Table Model for a
//		StateCU_DataSet component to display data objects in a table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-07-01	Steven A. Malers, RTi	Initial version.
// 2003-07-13	SAM, RTi		Update because StateCU_DataSetComponent
//					has been replaced by
//					RTi.Util.IO.DataSetComponent.
// 2004-02-19	SAM, RTi		Update because the row number is now
//					handled in the JWorksheet.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.List;

import DWR.StateMod.StateMod_Data;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.Message.Message;

/**
This class is a table model for the data objects in a StateCU_DataSet component.
It is not designed for the group components or control objects.
*/
public class StateCU_DataSetComponent_TableModel extends
JWorksheet_AbstractRowTableModel
{

/**
Number of columns in the table model.
*/
private int __COLUMNS = 2;

private final int __COL_ID = 0;
private final int __COL_NAME = 1;

/**
The component group that is used for the list.
*/
private DataSetComponent __component_group = null;

/**
The specific component (primary component) that is used for the list.
*/
private DataSetComponent __component = null;

/**
Constructor.  This builds the model for displaying the given component data.
@param dataset StateCU_DataSet that is being displayed.  If not a group
component, the group component will be determined.
@param comp the DataSetComponent to be displayed.
@throws Exception an invalid component is passed in.
*/
public StateCU_DataSetComponent_TableModel ( StateCU_DataSet dataset, DataSetComponent comp )
throws Exception
{	List data = null;
	String routine = "StateCU_DataSetComponent_TableModel";
	// Make sure that the list is for a group component...
	if ( (comp != null) && !comp.isGroup() ) {
		__component_group = comp.getParentComponent ();
		Message.printStatus ( 1, routine,
		"Component is not a group.  Parent is:  "  + __component_group);
	}
	else {
		__component_group = comp;
	}
	if ( __component_group == null ) {
		_rows = 0;
		_data = null;
		return;
	}
	// Figure out the data component that is actually used to get the list
	// of data objects.  For example, if working on climate stations, there
	// is no list with the group so we need to use the climate stations component list...
	int comptype = dataset.lookupPrimaryComponentTypeForComponentGroup
		( __component_group.getComponentType() );
	if ( comptype >= 0 ) {
		__component = dataset.getComponentForComponentType ( comptype );
	}
	else {
		comp = null;
		Message.printWarning ( 2, routine,
		"Unable to find primary component for group:  " + __component_group.getComponentName() );
	}
	if ( __component == null ) {
		_rows = 0;
	}
	else {
		data = ((List)__component.getData());
		if ( data == null ) {
			_rows = 0;
		}
		else {
			_rows = data.size();
		}
	}
	_data = data;
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex)
{	// TODO - expand this to handle data set component properties for the ID, name, etc. columns
	switch (columnIndex) {
		case __COL_ID:		return String.class;	// ID
		case __COL_NAME:	return String.class;	// Name
		default:	return String.class;
	}
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
		case __COL_ID:		return "ID";
		case __COL_NAME:	return "Name";
		default:	return "";
	}
}

/**
Return the component group that corresponds to the list.  This can be used, for
example to label visible components.
@return the component group that corresponds to the list (can be null).
*/
public DataSetComponent getComponentGroup ()
{	return __component_group;
}

/**
Returns the format to display the specified column.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString()).
*/
public String getFormat ( int column ) {
	switch (column) {
		default:	return "%s";	// All are strings.
	}
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	// make sure the row numbers are never sorted ...

	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	if ( __component.getComponentType() == StateCU_DataSet.COMP_DELAY_TABLES_MONTHLY ) {
		// StateMod_Data...
		StateMod_Data data = (StateMod_Data)_data.get(row);
		switch (col) {
			// case 0 handled above.
			case __COL_ID:		return data.getID();
			case __COL_NAME: 	return data.getName();
			default:	return "";
		}
	}
	else {
		// StateCU_Data...
		StateCU_Data data = (StateCU_Data)_data.get(row);
		switch (col) {
			case __COL_ID:		return data.getID();
			case __COL_NAME: 	return data.getName();
			default:	return "";
		}
	}
}

/**
Returns an array containing the column widths (in number of characters).
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	widths[__COL_ID] = 12;	// ID
	widths[__COL_NAME] = 20;	// Name/Description
	return widths;
}

}
