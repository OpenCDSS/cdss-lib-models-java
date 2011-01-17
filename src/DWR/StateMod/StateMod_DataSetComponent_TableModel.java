// ----------------------------------------------------------------------------
// StateMod_DataSetComponent_TableModel - Table Model for a
//		StateMod_DataSet component to display data objects in a table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-10	Steven A. Malers, RTi	Initial version - copy and modify the
//					similar StateCU class.
// 2003-10-14	SAM, RTi		Change irrigation water requirement to
//					consumptive water requirement.
// 2004-01-21	J. Thomas Sapienza, RTi	Removed the row count column and 
//					changed all the other column numbers.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;

import DWR.StateCU.StateCU_Data;
import RTi.TS.TS;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.Message.Message;

/**
This class is a table model for the data objects in a StateMod_DataSet component.
It is not designed for the group components or control objects.
*/
public class StateMod_DataSetComponent_TableModel extends
JWorksheet_AbstractRowTableModel
{

/**
Number of columns in the table model.
*/
private int __COLUMNS = 2;

/**
References to column numbers.
*/
public final static int 
	COL_ID = 	0,
	COL_NAME = 	1;

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
@param dataset StateMod_DataSet that is being displayed.  If not a group
component, the group component will be determined.
@param comp the DataSetComponent to be displayed.
@throws Exception an invalid component is passed in.
*/
public StateMod_DataSetComponent_TableModel ( StateMod_DataSet dataset, DataSetComponent comp )
throws Exception
{	List data = null;
	String routine = "StateMod_DataSetComponent_TableModel";
	// Make sure that the list is for a group component...
	if ( (comp != null) && !comp.isGroup() ) {
		__component_group = comp.getParentComponent ();
		//Message.printStatus ( 1, routine,
		//"Component is not a group.  Parent is:  " +__component_group);
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
	// is no list with the group so we need to use the climate stations
	// component list...
	int comptype = dataset.lookupPrimaryComponentTypeForComponentGroup ( __component_group.getComponentType() );
	if ( comptype >= 0 ) {
		__component = dataset.getComponentForComponentType ( comptype );
	}
	else {	comp = null;
		Message.printWarning ( 2, routine,
		"Unable to find primary component for group:  " + __component_group.getComponentName() );
	}
	if ( __component == null ) {
		_rows = 0;
	}
	else {	data = ((List)__component.getData());
		if ( data == null ) {
			_rows = 0;
		}
		else {	_rows = data.size();
		}
	}
	_data = data;
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex)
{	// REVISIT - expand this to handle data set component properties for the
	// ID, name, etc. columns
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_NAME:		return String.class;
		default:		return String.class;
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
		case COL_ID:		return "ID";
		case COL_NAME:		return "Name";
		default:		return "";
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
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	Object o = (Object)_data.get(row);
	if ( o instanceof TS ) {
		TS ts = (TS)o;
		switch (col) {
			case COL_ID:	return ts.getIdentifier().toString();
			case COL_NAME: 	return ts.getDescription();
			default:	return "";
		}
	}
	else if ((__component.getComponentType() ==
		StateMod_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY) ||
		(__component.getComponentType() ==
		StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY)
		||
		(__component.getComponentType() ==
		StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY) ||
		(__component.getComponentType() ==
		StateMod_DataSet.COMP_STATECU_STRUCTURE) ) {
		// StateCU_Data...
		StateCU_Data data = (StateCU_Data)_data.get(row);
		switch (col) {
			case COL_ID:	return data.getID();
			case COL_NAME: 	return data.getName();
			default:	return "";
		}
	}
	else {	// StateMod_Data...
		StateMod_Data data = (StateMod_Data)_data.get(row);
		switch (col) {
			case COL_ID:	return data.getID();
			case COL_NAME: 	return data.getName();
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
	widths[COL_ID] = 	12;
	widths[COL_NAME] = 	20;
	return widths;
}

}