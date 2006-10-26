// ----------------------------------------------------------------------------
// StateMod_InstreamFlowRight_Data_TableModel - Table model for displaying 
//	data in Instream Flow right data tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-03-31	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-11	JTS, RTi		Corrected the classes returned from
//					getColumnClass().
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.Date;
import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.Time.DateTime;

/**
This table model displays instream flow right data.
*/
public class StateMod_InstreamFlowRight_Data_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 6;

/**
References to columns.
*/
public final static int
	COL_RIGHT_ID =	0,
	COL_RIGHT_NAME= 1,
	COL_STRUCT_ID =	2,
	COL_ADMIN_NUM =	3,
	COL_DCR_AMT =	4,
	COL_ON_OFF =	5;
	
/**
Whether the table data is editable or not.
*/
private boolean __editable = false;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
*/
public StateMod_InstreamFlowRight_Data_TableModel(Vector data, 
boolean editable) {
	if (data == null) {
		_data = new Vector();
	}
	else {
		_data = data;
	}
	_rows = _data.size();

	__editable = editable;
}


/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_RIGHT_ID:	return String.class;
		case COL_RIGHT_NAME:	return String.class;
		case COL_STRUCT_ID:	return String.class;
		case COL_ADMIN_NUM:	return String.class;
		case COL_DCR_AMT:	return Double.class;
		case COL_ON_OFF:	return Integer.class;
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
		case COL_RIGHT_ID:	return "\n\n\nRIGHT ID";
		case COL_RIGHT_NAME:	return "\n\n\nRIGHT NAME";
		case COL_STRUCT_ID:	
			return "INSTREAM FLOW\nSTATION ID\nASSOCIATED"
				+ "\nWITH RIGHT";
		case COL_ADMIN_NUM:	return "\n\nADMINISTRATION\nNUMBER";
		case COL_DCR_AMT:	return "\n\nDECREED\nAMOUNT (CFS)";
		case COL_ON_OFF:	return "\n\nON/OFF\nSWITCH";
		default:		return " ";
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
		case COL_RIGHT_ID:	return "%-12.12s";
		case COL_RIGHT_NAME:	return "%-24.24s";
		case COL_STRUCT_ID:	return "%-12.12s";
		case COL_ADMIN_NUM:	return "%-12.12s";
		case COL_DCR_AMT:	return "%8.2f";
		case COL_ON_OFF:	return "%8d";
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
Returns the data that should be placed in the JTable at the given row 
and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_InstreamFlowRight infr = 
		       (StateMod_InstreamFlowRight)_data.elementAt(row);
	switch (col) {
		case COL_RIGHT_ID:	return infr.getID();
		case COL_RIGHT_NAME:	return infr.getName();
		case COL_STRUCT_ID:	return infr.getCgoto();
		case COL_ADMIN_NUM:	return infr.getIrtem();
		case COL_DCR_AMT:	return new Double(infr.getDcrifr());
		case COL_ON_OFF:	return new Integer(infr.getSwitch());
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

	widths[COL_RIGHT_ID] = 	8;
	widths[COL_RIGHT_NAME]= 20;
	widths[COL_STRUCT_ID] =	12;
	widths[COL_ADMIN_NUM] =	12;
	widths[COL_DCR_AMT] = 	10;
	widths[COL_ON_OFF] = 	8;

	return widths;
}

/**
Returns whether the cell is editable or not.  If the table is editable, only
the instream flow ID is not editable - this reflects the display being opened
from the instream flow station window and maintaining agreement between the two
windows.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable
or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable || (columnIndex == COL_STRUCT_ID)) {
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
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	StateMod_InstreamFlowRight ifr =
		(StateMod_InstreamFlowRight)_data.elementAt(row);

	switch (col) {
		case COL_RIGHT_ID:
			ifr.setID((String)value);
			break;
		case COL_RIGHT_NAME:
			ifr.setName((String)value);
			break;
		case COL_STRUCT_ID:
			ifr.setCgoto((String)value);
			break;
		case COL_ADMIN_NUM:
			ifr.setIrtem((String)value);
			break;
		case COL_DCR_AMT:
			dval = ((Double)value).doubleValue();
			ifr.setDcrifr(dval);
			break;
		case COL_ON_OFF:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				ifr.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				int index = onOff.indexOf(" -");
				ival = new Integer(
					onOff.substring(0, index)).intValue();
					ifr.setSwitch(ival);
			}
			/*
			ival = ((Integer)value).intValue();
			ifr.setSwitch(ival);
			break;
			*/
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

	tips[COL_RIGHT_ID] = 
		"<html>The instream flow right ID is typically the "
		+ "instream flow ID<br> followed by .01, .02, "
		+ "etc.</html>";
	tips[COL_RIGHT_NAME] = "Instream flow right name.";
	tips[COL_STRUCT_ID] = 
		"<HTML>The instream flow ID is the link between instream "
		+ " flows and their right<BR>(not editable here).</HTML>";
	tips[COL_ADMIN_NUM] = 
		"<HTML>Lower admininistration numbers indicate greater"
		+ "seniority.<BR>99999 is typical for a very junior"
		+ " right.</html>";
	tips[COL_DCR_AMT] = 
		"Decreed amount (CFS).";
	tips[COL_ON_OFF] = 
		"<HTML>0 = OFF<BR>1 = ON<BR>"
		+ "YYYY indicates to turn on the right in year YYYY."
		+ "<BR>-YYYY indicates to turn off the right in year"
		+ " YYYY.</HTML>";

	return tips;
}

}
