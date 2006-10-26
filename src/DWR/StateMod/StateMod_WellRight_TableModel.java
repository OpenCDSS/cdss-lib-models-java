// ----------------------------------------------------------------------------
// StateMod_WellRight_TableModel - Table model for displaying data for well
//	right tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-10	JTS, RTi		* Added 'right' fields.
//					* Added 'return flow' fields.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-08-29	Steven A. Malers, RTi	Update for changes in StateMod_Well.
// 2003-10-13	JTS, RTi		Removed references to parent well.
// 2004-01-22	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Split out code from
//					StateMod_Well_TableModel.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2005-01-20	JTS, RTi		Added ability to display data for either
//					one or many wells.
// 2005-03-28	JTS, RTi		Adjusted column sizes.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.Message.Message;

/**
This table model displays well right data.  The model can display rights data
for a single well or for 1+ wells.  The difference is specified in the
constructor and affects how many columns of data are shown.
*/
public class StateMod_WellRight_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.  For table models that display rights for
a single well, the worksheets only have have 6 columns.  Table models that 
display rights for 1+ wells have 7.  The variable is modified in the 
constructor.
*/
private int __COLUMNS = 6;

/**
References to columns.
*/
public final static int
	COL_WELL_ID =	-1,
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
Whether the table model should be set up for displaying the rights for only
a single well (true) or for multiple wells (false).  If true, then
the well ID field will not be displayed.  If false, then it will be.
*/
private boolean __singleWell = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
@param singleWell if true, then the table model is set up to only display
a single well's right data.  This means that the well ID field will
not be shown.  If false then the well right field will be included.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_WellRight_TableModel(Vector data, boolean editable,
boolean singleWell)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_WellRight_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
	__singleWell = singleWell;

	if (!__singleWell) {
		// this is done because if rights for 1+ wells are shown in 
		// the worksheet the ID for the associated wells needs shown
		// as well.  So instead of just 6 columns of data, an 
		// additional one is necessary.
		__COLUMNS++;
	}
}

/**
Constructor.  
@param dataset the dataset in which the data are displayed
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
@param singleWell if true, then the table model is set up to only display
a single well's right data.  This means that the well ID field will
not be shown.  If false then the well right field will be included.
@throws Exception if an invalid data or dmi was passed in.
@deprecated use the other constructor.  This will be phased out soon 
(2005-01-25)
*/
public StateMod_WellRight_TableModel(StateMod_DataSet dataset, 
Vector data, boolean editable, boolean singleWell)
throws Exception {	
	this(data, editable, singleWell);
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class getColumnClass (int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

	switch (col) {
		case COL_WELL_ID:	return String.class;
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
public String getColumnName(int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

	switch (col) {
		case COL_WELL_ID:	return "\n\nWELL ID";
		case COL_RIGHT_ID:	return "\n\nRIGHT ID";
		case COL_RIGHT_NAME:	return "\n\nWELL RIGHT NAME";
		case COL_STRUCT_ID:	return "WELL ID\nASSOCIATED\nW/ RIGHT";
		case COL_ADMIN_NUM:	return "\n\nADMIN NUMBER";
		case COL_DCR_AMT:	return "\nDECREED\nAMOUNT (CFS)";
		case COL_ON_OFF:	return "\nON/OFF\nSWITCH";
		default:	return " ";
	}
}


/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	// the offset is used because in worksheets that have rights data for 1+
	// wells the first column is numbered -1.  The offset calculation 
	// allows this column number, which is normally invalid for a worksheet,
	// to be used only for those worksheets that need to display the -1st
	// column.
	int offset = 0;
	if (!__singleWell) {	
		offset = 1;
		tips[COL_WELL_ID + offset] = 
			"<html>The ID of the well to "
			+ "which<br>the rights belong.</html>";
	}

	tips[COL_RIGHT_ID + offset] =
		"<html>The well right ID is typically the well" +
		" station ID<br> followed by .01, .02, etc.</html>";
	tips[COL_RIGHT_NAME + offset] = 
		"Well right name";
	tips[COL_STRUCT_ID + offset] = 
		"<HTML>The well ID is the link between well stations "
		+ "and their right(s).</HTML>";
	tips[COL_ADMIN_NUM + offset] = 
		"<HTML>Lower admininistration numbers indicate greater " +
		"seniority.<BR>99999 is typical for a very junior" +
		" right.</html>";
	tips[COL_DCR_AMT + offset] = 
		"Decreed amount (CFS)";
	tips[COL_ON_OFF + offset] = 
		"<HTML>0 = OFF<BR>1 = ON<BR>" +
		"YYYY indicates to turn on the right in year YYYY."+
		"<BR>-YYYY indicates to turn off the right in year" +
		" YYYY.</HTML>";

	return tips;
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

	// the offset is used because in worksheets that have rights data for 1+
	// wells the first column is numbered -1.  The offset calculation 
	// allows this column number, which is normally invalid for a worksheet,
	// to be used only for those worksheets that need to display the -1st
	// column.
	int offset = 0;
	if (!__singleWell) {
		offset = 1;
		widths[COL_WELL_ID + offset] = 8;
	}
	
	widths[COL_RIGHT_ID + offset] = 	10;
	widths[COL_RIGHT_NAME + offset] = 	20;
	widths[COL_STRUCT_ID + offset] =	10;
	widths[COL_ADMIN_NUM + offset] = 	11;
	widths[COL_DCR_AMT + offset] = 	10;
	widths[COL_ON_OFF + offset] = 	7;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

	switch (col) {
		case COL_WELL_ID:	return "%-12s";
		case COL_RIGHT_ID:	return "%-12s";
		case COL_RIGHT_NAME:	return "%-24s";
		case COL_STRUCT_ID:	return "%-40s";
		case COL_ADMIN_NUM:	return "%-40s";
		case COL_DCR_AMT:	return "%12.2f";
		case COL_ON_OFF:	return "%8d";
		default:		return "%-s";
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
Returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_WellRight wellr = (StateMod_WellRight)_data.elementAt(row);

	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}
	
	switch (col) {
		case COL_WELL_ID:	return wellr.getCgoto();
		case COL_RIGHT_ID:	return wellr.getID();
		case COL_RIGHT_NAME:	return wellr.getName();
		case COL_STRUCT_ID:	return wellr.getCgoto();
		case COL_ADMIN_NUM:	return wellr.getIrtem();
		case COL_DCR_AMT:	return new Double(wellr.getDcrdivw());
		case COL_ON_OFF:	return new Integer(wellr.getSwitch());
		default:		return "";
	}
}

/**
Returns whether the cell is editable or not.  If the table is editable, only
the well ID is not editable - this reflects the display being opened from the
well station widnow and maintaining agreement between the two windows.
@param rowIndex unused.
@param col the index of the column to check whether it is editable
or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
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

	StateMod_WellRight wellr = (StateMod_WellRight)_data.elementAt(row);

	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}
	
	switch (col) {
		case COL_WELL_ID:
			wellr.setCgoto((String)value);
			break;
		case COL_RIGHT_ID:	
			wellr.setID((String)value);
			break;
		case COL_RIGHT_NAME:
			wellr.setName((String)value);
			break;
		case COL_STRUCT_ID:
			wellr.setCgoto((String)value);
			break;
		case COL_ADMIN_NUM:
			wellr.setIrtem((String)value);
			break;
		case COL_DCR_AMT:
			if (value instanceof String) {
				try {	dval = (new Double(
						(String)value)).doubleValue();
				}
				catch (Exception e) {
					Message.printWarning(2, "setValue", e);
					return;
				}
			}
			else {	dval = ((Double)value).doubleValue();
			}
			wellr.setDcrdivw(dval);
			break;
		case COL_ON_OFF:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				wellr.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				int index = onOff.indexOf(" -");
				ival = new Integer(
					onOff.substring(0, index)).intValue();
				wellr.setSwitch(ival);
			}
			break;				
	}	

	if (!__singleWell) {
		col++;
	}

	super.setValueAt(value, row, col);	
}	

}
