// ----------------------------------------------------------------------------
// StateMod_Plan_TableModel - Table model for displaying data in the
//	plan tables.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2006-08-22	Steven A. Malers, RTi	Initial version, based on the
//					StateMod_Diversion_TableModel.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model display data in plan tables.
*/
public class StateMod_Plan_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model (this includes all data - other code
can hide columns if necessary).
*/
private int __COLUMNS = 10;

/**
References to columns.
*/
public final static int
	COL_ID =			0,
	COL_NAME =			1,
	COL_RIVER_NODE_ID =		2,
	COL_ON_OFF =			3,
	COL_TYPE =			4,
	COL_EFFICIENCY =		5,
	COL_RETURN_FLOW_TABLE =		6,
	COL_FAILURE_SWITCH =		7,
	COL_INITIAL_STORAGE =		8,
	COL_SOURCE =			9;

/**
Whether the table data can be edited or not.
*/
private boolean __editable = true;

/**
Constructor.  This builds the Model for displaying the plan station data.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Plan_TableModel(List data, boolean editable)
throws Exception {
	this(null, data, editable, false);
}

/**
Constructor.  This builds the Model for displaying the plan station data.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not
@param compactForm if true, then the compact form of the table model will be
used.  In the compact form, only the name and ID are shown.  If false, all fields will be shown.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Plan_TableModel(List data, boolean editable, boolean compactForm)
throws Exception {
	this(null, data, editable, compactForm);
}

/**
Constructor.  This builds the Model for displaying the plan data.
@param dataset the dataset for the data being displayed.  Only necessary for return flow tables.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not
@param compactForm if true, then the compact form of the table model will be
used.  In the compact form, only the name and ID are shown.  If false, all fields will be shown.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Plan_TableModel(StateMod_DataSet dataset, List data, boolean editable, boolean compactForm)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to StateMod_Plan_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;

	if (compactForm) {
		__COLUMNS = 2;
	}
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_NAME:		return String.class;
		case COL_RIVER_NODE_ID:	return String.class;
		case COL_ON_OFF:	return Integer.class;
		case COL_TYPE:		return Integer.class;
		case COL_EFFICIENCY:	return Double.class;
		case COL_RETURN_FLOW_TABLE:	return Integer.class;
		case COL_FAILURE_SWITCH:	return Integer.class;
		case COL_INITIAL_STORAGE:	return Double.class;
		case COL_SOURCE:	return String.class;
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
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_ID:	return "ID";
		case COL_NAME:	return "NAME";
		case COL_RIVER_NODE_ID:	return "RIVER\nNODE ID";
		case COL_ON_OFF:	return "ON/OFF\nSWITCH";
		case COL_TYPE:		return "PLAN\nTYPE";
		case COL_EFFICIENCY:	return "EFFICIENCY\n(%)";
		case COL_RETURN_FLOW_TABLE:	return "RETURN\nFLOW TABLE";
		case COL_FAILURE_SWITCH:	return "FAILURE\nSWITCH";
		case COL_INITIAL_STORAGE:	return "INITIAL\nSTORAGE";
		case COL_SOURCE:	return "SOURCE\nID";
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
		case COL_ID:		return "%-12s";
		case COL_NAME:		return "%-40s";
		case COL_RIVER_NODE_ID:	return "%-12s";
		case COL_ON_OFF:	return "%d";
		case COL_TYPE:		return "%d";
		case COL_EFFICIENCY:	return "%.1f";
		case COL_RETURN_FLOW_TABLE:	return "%d";
		case COL_FAILURE_SWITCH:	return "%d";
		case COL_INITIAL_STORAGE:	return "%.0f";
		case COL_SOURCE:	return "%-s";
		default:		return "%-s";
	}
}

// REVISIT (SAM - 2005-01-20)
// we might need to display flag values as "1 - XXX" to be readable.  Let's 
// wait on some user feedback.  If editable, this may mean that choices are 
// shown.

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

	StateMod_Plan smp = (StateMod_Plan)_data.get(row);
	switch (col) {
		case COL_ID:		return smp.getID();
		case COL_NAME:		return smp.getName();
		case COL_RIVER_NODE_ID:	return smp.getCgoto();
		case COL_ON_OFF:	return new Integer(smp.getSwitch());
		case COL_TYPE:		return new Integer(smp.getIPlnTyp());
		case COL_EFFICIENCY:	return new Double(smp.getPeff());
		case COL_RETURN_FLOW_TABLE:
					return new Integer(smp.getIPrf());
		case COL_FAILURE_SWITCH:return new Integer(smp.getIPfail());
		case COL_INITIAL_STORAGE:
					return new Double(smp.getPsto1());
		case COL_SOURCE:	return smp.getPsource();
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
	widths[COL_ID] = 		8;
	widths[COL_NAME] = 		18;

	if (__COLUMNS == 2) {
		return widths;
	}
	
	widths[COL_RIVER_NODE_ID] =	8;
	widths[COL_ON_OFF] =		5;
	widths[COL_TYPE] =		8;
	widths[COL_EFFICIENCY] =	8;
	widths[COL_RETURN_FLOW_TABLE] =	7;
	widths[COL_FAILURE_SWITCH] =	7;
	widths[COL_INITIAL_STORAGE] =	8;
	widths[COL_SOURCE] =		8;
	return widths;
}

/**
Returns whether the cell is editable or not.  Currently no cells are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable
or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}
	return true;
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
	int ival;
	int index;

	StateMod_Plan smp = (StateMod_Plan)_data.get(row);

	switch (col) {
		case COL_ID:
			smp.setID((String)value);
			break;
		case COL_NAME:
			smp.setName((String)value);
			break;
		case COL_RIVER_NODE_ID:
			smp.setCgoto((String)value);
			break;
		case COL_ON_OFF:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smp.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				index = onOff.indexOf(" -");
				ival = new Integer(
					onOff.substring(0,
					index)).intValue();
				smp.setSwitch(ival);
			}
			break;
		case COL_TYPE:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smp.setIPlnTyp(ival);
			}
			else if (value instanceof String) {
				String iPlnTyp = (String)value;
				index = iPlnTyp.indexOf(" -");
				ival = new Integer(
					iPlnTyp.substring(0, index)).intValue();
				smp.setIPlnTyp(ival);
			}
			break;
		case COL_EFFICIENCY:
			smp.setPeff((Double)value);
			break;
		case COL_RETURN_FLOW_TABLE:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smp.setIPrf(ival);
			}
			else if (value instanceof String) {
				String iPrf = (String)value;
				index = iPrf.indexOf(" -");
				ival = new Integer(
					iPrf.substring(0, index)).intValue();
				smp.setIPrf(ival);
			}
			break;
		case COL_FAILURE_SWITCH:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smp.setIPfail(ival);
			}
			else if (value instanceof String) {
				String iPfail = (String)value;
				index = iPfail.indexOf(" -");
				ival = new Integer(
					iPfail.substring(0, index)).intValue();
				smp.setIPfail(ival);
			}
			break;
		case COL_INITIAL_STORAGE:
			smp.setPsto1((Double)value);
			break;
		case COL_SOURCE:
			smp.setPsource((String)value);
			break;
	}

	super.setValueAt(value, row, col);	
}	

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_ID] = 
		"<html>The plan station identifier is the main link" +
		" between plan data<BR>" +
		"and must be unique in the data set.</html>";
	tips[COL_NAME] = 
		"Plan name.";

	if (__COLUMNS == 2) {
		return tips;
	}

	tips[COL_RIVER_NODE_ID] =
		"River node where plan is located.";
	tips[COL_ON_OFF] =
		"Indicates whether plan is on (1) or off (0)";
	tips[COL_TYPE]= "Plan type.";
	tips[COL_EFFICIENCY] =
		"Efficiency, annual (%).";
	tips[COL_RETURN_FLOW_TABLE] = "Plan return flow table.";
	tips[COL_INITIAL_STORAGE] = "Initial plan storage (AF).";
	tips[COL_SOURCE] = "Source ID for structure where reuse water became" +
		" available or a T&C condition originated.";
	return tips;
}

}
