// ----------------------------------------------------------------------------
// StateMod_DiversionRight_Data_TableModel - Table model for displaying data 
//	in the diversion right data tables.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-03-30	J. Thomas Sapienza, RTi	Initial version.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import DWR.StateMod.StateMod_DiversionRight;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays diversion right data.  The model can display rights
data for a single diversion or for 1+ diversion.  The difference is specified
in the constructor and affects how many columns of data are shown.
*/
public class StateMod_DiversionRight_Data_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.  For table models that display rights
for a single diversion, the tables only have 6 columns.  Table models that
display rights for 1+ diversions have 7.  The variable is modified in the
constructor.
*/
private int __COLUMNS = 6;

/**
References to columns.
*/
public final static int
	COL_RIGHT_ID =		0,
	COL_RIGHT_NAME = 	1,
	COL_STRUCT_ID =		2,
	COL_ADMIN_NUM =		3,
	COL_DCR_AMT =		4,
	COL_ON_OFF =		5;

/**
Whether the table data can be edited.
*/
private boolean __editable = true;

/**
Constructor.  This builds the table model for displaying the diversion right 
data.
@param dataset the dataset for the data being displayed.
@param data the diversion right data that will be displayed in the table.
@param editable whether the data can be edited
*/
public StateMod_DiversionRight_Data_TableModel(StateMod_DataSet dataset, 
Vector data, boolean editable) {
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
Constructor.  This builds the table model for displaying the diversion right 
data.
@param dataset the dataset for the data being displayed.
@param data the diversion right data that will be displayed in the table.
@param editable whether the data can be edited
*/
public StateMod_DiversionRight_Data_TableModel(Vector data, boolean editable) {
	this(null, data, editable);
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.  This is
base 0.
@return the class of the data stored in a given column.
*/
public Class getColumnClass (int col) {
	switch (col) {
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
@param col the index of the column for which to return the name.  This
is base 0.
@return the name of the column at the given position.
*/
public String getColumnName(int col) {
	switch (col) {
		case COL_RIGHT_ID:	return "\nDIVERSION\nRIGHT ID";
		case COL_RIGHT_NAME:	return "\nDIVERSION RIGHT\nNAME";
		case COL_STRUCT_ID:	
			return "DIVERSION ID\nASSOCIATED\nWITH RIGHT";
		case COL_ADMIN_NUM:	return "\nADMINISTRATION\nNUMBER";
		case COL_DCR_AMT:	return "DECREE\nAMOUNT\n(CFS)";
		case COL_ON_OFF:	return "\nON/OFF\nSWITCH";
		default:		return " ";
	}
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.  This is base 0.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int col) {
	switch (col) {
		case COL_RIGHT_ID:	return "%-12.12s";	
		case COL_RIGHT_NAME:	return "%-24.24s";	
		case COL_STRUCT_ID:	return "%-12.12s";	
		case COL_ADMIN_NUM:	return "%-12.12s";	
		case COL_DCR_AMT:	return "%12.2f";
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
@param col the column for which to return data.  This is base 0.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	StateMod_DiversionRight dr 
		= (StateMod_DiversionRight)_data.elementAt(row);

	switch (col) {
		case COL_RIGHT_ID:	return dr.getID();
		case COL_RIGHT_NAME:	return dr.getName();
		case COL_STRUCT_ID:	return dr.getCgoto();
		case COL_ADMIN_NUM:	return dr.getIrtem();
		case COL_DCR_AMT:	return new Double(dr.getDcrdiv());
		case COL_ON_OFF:	return new Integer(dr.getSwitch());
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

	widths[COL_RIGHT_ID] = 	8;
	widths[COL_RIGHT_NAME] = 	18;
	widths[COL_STRUCT_ID] = 	9;
	widths[COL_ADMIN_NUM] = 	12;
	widths[COL_DCR_AMT] = 		7;
	widths[COL_ON_OFF] = 		5;
	return widths;
}

/**
Returns whether the cell is editable.  If the table is editable, only
the diversion ID is not editable - this reflects the display being opened from
the diversion station window and maintaining agreement between the two windows.
@param row unused.
@param col the index of the column to check whether it is editable.  This is
base 0.
@return whether the cell is editable.
*/
public boolean isCellEditable(int row, int col) {
	if (!__editable || (col == COL_STRUCT_ID)) {
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
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	int index;

	StateMod_DiversionRight dr = 
		(StateMod_DiversionRight)_data.elementAt(row);
	switch (col) {
		case COL_RIGHT_ID:	
			dr.setID((String)value);
			break;
		case COL_RIGHT_NAME:	
			dr.setName((String)value);
			break;
		case COL_STRUCT_ID:	
			dr.setCgoto((String)value);
			break;
		case COL_ADMIN_NUM:	
			dr.setIrtem((String)value);
			break;
		case COL_DCR_AMT:	
			dval = ((Double)value).doubleValue();
			dr.setDcrdiv(dval);
			break;
		case COL_ON_OFF:	
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				dr.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				index = onOff.indexOf(" -");
				ival = new Integer(
					onOff.substring(0,
					index)).intValue();
				dr.setSwitch(ival);
			}
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
		"<html>The diversion right ID is typically the diversion" +
		" station ID<br> followed by .01, .02, etc.</html>";
	tips[COL_RIGHT_NAME] = 
		"Diversion right name";
	tips[COL_STRUCT_ID] = 
		"<HTML>The diversion ID is the link between diversion stations "
		+ "and their right(s).</HTML>";
	tips[COL_ADMIN_NUM] = 
		"<HTML>Lower admininistration numbers indicate greater " +
		"seniority.<BR>99999 is typical for a very junior" +
		" right.</html>";
	tips[COL_DCR_AMT] = 
		"Decree amount (CFS)";
	tips[COL_ON_OFF] = 
		"<HTML>0 = OFF<BR>1 = ON<BR>" +
		"YYYY indicates to turn on the right in year YYYY."+
		"<BR>-YYYY indicates to turn off the right in year" +
		" YYYY.</HTML>";

	return tips;
}

}
