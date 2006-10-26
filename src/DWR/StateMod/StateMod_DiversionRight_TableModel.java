// ----------------------------------------------------------------------------
// StateMod_DiversionRight_TableModel - Table model for displaying data in the
//	diversion right tables.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-10	JTS, RTi		* Added the right fields
//					* Added the return flow fields
// 2003-06-17	JTS, RTi		Return flow data is now displayable
//					and editable.
// 2003-07-17	JTS, RTi		Constructor has switch to determine
//					if data is editable.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-10-07	JTS, RTi		Changed 'readOnly' to 'editable'.
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-26	SAM, RTi		Split out code from
//					StateMod_Diversion_TableModel.
// 2005-01-21	JTS, RTi		Added ability to display data for either
//					one or many diversions.
// 2005-01-24	JTS, RTi		* Touched up the javadocs.
//					* Removed reference to a dataset.
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
public class StateMod_DiversionRight_TableModel 
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
	COL_DIVERSION_ID = 	-1,
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
Whether only one diversion's rights are shown (true) or many diversions' rights
are shown (false).
*/
private boolean __singleDiversion = true;

/**
Constructor.  This builds the table model for displaying the diversion right 
data.
@param dataset the dataset for the data being displayed.
@param data the diversion right data that will be displayed in the table.
@param editable whether the data can be edited
@param singleDiversion whether data for a single diversion is shown (true)
or data for multiple diversions is shown (false).
@throws Exception if an invalid data or dmi was passed in.
@deprecated use the other method.  This will be phased out soon (2005-01-24)
*/
public StateMod_DiversionRight_TableModel(StateMod_DataSet dataset, 
Vector data, boolean editable, boolean singleDiversion)
throws Exception {
	this(data, editable, singleDiversion);
}

/**
Constructor.  This builds the table model for displaying the diversion right 
data.
@param dataset the dataset for the data being displayed.
@param data the diversion right data that will be displayed in the table.
@param editable whether the data can be edited
@param singleDiversion whether data for a single diversion is shown (true)
or data for multiple diversions is shown (false).
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_DiversionRight_TableModel(Vector data, boolean editable, 
boolean singleDiversion)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_DiversionRight_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
	__singleDiversion = singleDiversion;

	if (!__singleDiversion) {	
		// this is done because if rights for 1+ diversions are 
		// shown in the worksheet the ID for the associated diversions 
		// needs shown as well.  So instead of the usual 6 columns
		// of data, an additional one is necessary.
		__COLUMNS++;
	}
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.  This is
base 0.
@return the class of the data stored in a given column.
*/
public Class getColumnClass (int col) {
	// necessary for table models that display rights for 1+ diversions,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleDiversion) {
		col--;
	}

	switch (col) {
		case COL_DIVERSION_ID:	return String.class;
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
	// necessary for table models that display rights for 1+ diversions,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleDiversion) {
		col--;
	}

	switch (col) {
		case COL_DIVERSION_ID:	return "\nDIVERSION\nID";
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
	// necessary for table models that display rights for 1+ diversions,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleDiversion) {
		col--;
	}

	switch (col) {
		case COL_DIVERSION_ID:	return "%-12s";
		case COL_RIGHT_ID:	return "%-12s";	
		case COL_RIGHT_NAME:	return "%-24s";	
		case COL_STRUCT_ID:	return "%-12s";	
		case COL_ADMIN_NUM:	return "%-40s";	
		case COL_DCR_AMT:	return "%12.1f";
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

	// necessary for table models that display rights for 1+ diversions,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleDiversion) {
		col--;
	}

	switch (col) {
		case COL_DIVERSION_ID:	return dr.getCgoto();
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

	// the offset is used because in worksheets that have rights data for 1+
	// diversions the first column is numbered -1.  The offset calculation 
	// allows this column number, which is normally invalid for a table, 
	// to be used only for those worksheets that need to display the 
	// -1st column.
	int offset = 0;
	if (!__singleDiversion) {
		offset = 1;
		widths[COL_DIVERSION_ID + offset] = 8;
	}

	widths[COL_RIGHT_ID + offset] = 	8;
	widths[COL_RIGHT_NAME + offset] = 	18;
	widths[COL_STRUCT_ID + offset] = 	9;
	widths[COL_ADMIN_NUM + offset] = 	12;
	widths[COL_DCR_AMT + offset] = 		7;
	widths[COL_ON_OFF + offset] = 		5;
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
	// necessary for table models that display rights for 1+ diversions,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleDiversion) {	
		col--;
	}

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
	String s;

	// necessary for table models that display rights for 1+ diversions,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleDiversion) {
		col--;
	}

	StateMod_DiversionRight dr = 
		(StateMod_DiversionRight)_data.elementAt(row);
	switch (col) {
		case COL_DIVERSION_ID:
			dr.setCgoto((String)value);
			break;
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

	if (!__singleDiversion) {
		col++;
	}

	super.setValueAt(value, row, col);	
}	

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	int offset = 0;
	if (!__singleDiversion) {
		offset = 1;
	}

	if (!__singleDiversion) {
		tips[COL_DIVERSION_ID + offset] =
			"<html>The ID of the diversion to which the rights<br>"
			+ "belong.</html>";
	}

	tips[COL_RIGHT_ID + offset] =
		"<html>The diversion right ID is typically the diversion" +
		" station ID<br> followed by .01, .02, etc.</html>";
	tips[COL_RIGHT_NAME + offset] = 
		"Diversion right name";
	tips[COL_STRUCT_ID + offset] = 
		"<HTML>The diversion ID is the link between diversion stations "
		+ "and their right(s).</HTML>";
	tips[COL_ADMIN_NUM + offset] = 
		"<HTML>Lower admininistration numbers indicate greater " +
		"seniority.<BR>99999 is typical for a very junior" +
		" right.</html>";
	tips[COL_DCR_AMT + offset] = 
		"Decree amount (CFS)";
	tips[COL_ON_OFF + offset] = 
		"<HTML>0 = OFF<BR>1 = ON<BR>" +
		"YYYY indicates to turn on the right in year YYYY."+
		"<BR>-YYYY indicates to turn off the right in year" +
		" YYYY.</HTML>";

	return tips;
}

}
