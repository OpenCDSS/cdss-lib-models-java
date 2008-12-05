// ----------------------------------------------------------------------------
// StateMod_ReservoirRight_TableModel - table model for displaying reservoir 
//	right data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-11	JTS, RTi		Revised so that it displays real data
//					instead of dummy data for the main
//					Reservoir display.
// 2003-06-13	JTS, RTi		* Created code for displaying the
//					  area cap and climate data
//					* Added code to handle editing
//					  data
// 2003-06-16	JTS, RTi		* Added code for the reservoir account
//					  data
//					* Added code for the reservoir right
//					  data
// 2003-06-17	JTS, RTi		Revised javadocs.
// 2003-07-17	JTS, RTi		Constructor now takes a editable flag
//					to specify whether the data should be
//					editable or not.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2003-08-16	Steven A. Malers, RTi	Update because of changes in the
//					StateMod_ReservoirClimate class.
// 2003-08-22	JTS, RTi		* Changed headings for the rights table.
//					* Changed headings for the owner account
//					  table.
//					* Added code to accomodate tables which
//					  are now using comboboxes for entering
//					  data.
// 2003-08-25	JTS, RTi		Added partner table code for saving
//					data in the reservoir climate gui.
// 2003-08-28	SAM, RTi		* Change setRightsVector() call to
//					  setRights().
//					* Update for changes in
//					  StateMod_Reservoir.
// 2003-09-18	JTS, RTi		Added ID column for reservoir
//					accounts.
// 2003-10-10	JTS, RTi		* Removed reference to parent reservoir.
//					* Added getColumnToolTips().
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Split code out of
//					StateMod_Reservoir_TableModel.
//					Change setValueAt() to support sort.
//					Add tool tips.
// 2005-01-20	JTS, RTi		Added ability to display data for either
//					one or many reservoirs.
// 2005-03-28	JTS, RTi		* Changed Struct ID to a String field.
//					* Adjusted column sizes.
//					* Added flag to tell whether this 
//					  is being used in a data JFrame or not.
// 2006-04-11	JTS, RTi		Corrected the classes returned from
//					getColumnClass().
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays reservoir right data.  The model can display rights
data for a single reservoir or for 1+ reservoirs.  The difference is specified
in the constructor and affects how many columns of data are shown.
*/
public class StateMod_ReservoirRight_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.  For table models that display rights 
for a single reservoir, the tables only have 10 columns.  Table models that
display rights for 1+ reservoirs have 11.  The variable is modified in the
constructor.
*/
private int __COLUMNS = 10;

/**
References to columns.
*/
public final static int
	COL_RESERVOIR_ID = 	-1,
	COL_RIGHT_ID =		0,
	COL_RIGHT_NAME =	1,
	COL_STRUCT_ID =		2,
	COL_ADMIN_NUM =		3,
	COL_DCR_AMT =		4,
	COL_ON_OFF =		5,
	COL_ACCOUNT_DIST =	6,
	COL_RIGHT_TYPE =	7,
	COL_FILL_TYPE =		8,
	COL_OOP_RIGHT =		9;
	
/**
Whether this table model is being used in a data JFrame or not.
*/
private boolean __inData = false;
	
/**
Whether the table data is editable or not.
*/
private boolean __editable = true;

/**
Whether the table model should be set up for displaying the rights for only
a single reservoir (true) or for multiple reservoirs (false).  If true, then
the reservoir ID field will not be displayed.  If false, then it will be.
*/
private boolean __singleReservoir = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified or not.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_ReservoirRight_TableModel(List data, boolean editable)
throws Exception {
	this(data, editable, true);
}

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified or not.
@param singleReservoir if true, then the table model is set up to only display
a single reservoir's right data.  This means that the reservoir ID field will
not be shown.  If false then the reservoir right field will be included.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_ReservoirRight_TableModel(List data, boolean editable,
boolean singleReservoir)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_ReservoirRight_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
	__singleReservoir = singleReservoir;

	if (!__singleReservoir) {
		// this is done because if rights for 1+ reservoirs are
		// shown in the worksheet the ID for the associated reservoirs
		// needs shown as well.  So instead of the usual 10 columns
		// of data, an additional one is necessary.
		__COLUMNS++;
	}
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class getColumnClass (int col) {
	// necessary for table models that display rights for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}

	switch (col) {
		case COL_RESERVOIR_ID:	return String.class;
		case COL_RIGHT_ID:	return String.class;
		case COL_RIGHT_NAME:	return String.class;
		case COL_STRUCT_ID:	return String.class;
		case COL_ADMIN_NUM:	return String.class;
		case COL_DCR_AMT:	return Double.class;
		case COL_ON_OFF:	return Integer.class;
		case COL_ACCOUNT_DIST:	return Integer.class;
		case COL_RIGHT_TYPE:	return Integer.class;
		case COL_FILL_TYPE:	return Integer.class;
		case COL_OOP_RIGHT:	return String.class;
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
@param col the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int col) {
	// necessary for table models that display rights for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}
	
	switch (col) {
		case COL_RESERVOIR_ID:	return "\nRESERVOIR\nID";
		case COL_RIGHT_ID:	return "\nRIGHT\nID";
		case COL_RIGHT_NAME:	return "\nRIGHT\nNAME";
		case COL_STRUCT_ID:
			return "RESERVOIR\nSTATION ID\nASSOC. W/ RIGHT";
		case COL_ADMIN_NUM:	return "\nADMINISTRATION\nNUMBER";
		case COL_DCR_AMT:	return "DECREE\nAMOUNT\n(ACFT)";
		case COL_ON_OFF:	return "\nON/OFF\nSWITCH";
		case COL_ACCOUNT_DIST:	return "\nACCOUNT\nDISTRIBUTION";
		case COL_RIGHT_TYPE:	return "\n\nRIGHT TYPE";
		case COL_FILL_TYPE:	return "\n\nFILL TYPE";
		case COL_OOP_RIGHT:	return "OUT OF\nPRIORITY\nRIGHT";
		default:		return " ";
	}
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	// the offset is used because in worksheets that have rights data for 1+
	// reservoirs the first column is numbered -1.  The offset calculation
	// allows this column number, which is normally invalid for a table,
	// to be used only for those worksheets that need to display the
	// -1st column.
	int offset = 0;
	if (!__singleReservoir) {
		offset = 1;
		tips[COL_RESERVOIR_ID + offset] =
			"<html>The reservoir station ID of the reservoir to "
			+ "which<br>the rights belong.</html>";
	}

	tips[COL_RIGHT_ID + offset] =
		"<html>The reservoir right ID is typically the reservoir" +
		" station ID<br> followed by .01, .02, etc.</html>";
	tips[COL_RIGHT_NAME + offset] =
		"Reservoir right name";
	tips[COL_STRUCT_ID + offset] =
		"<HTML>The reservoir ID is the link between reservoir stations "
		+ "and their right(s).</HTML>";
	tips[COL_ADMIN_NUM + offset] =
		"<HTML>Lower admininistration numbers indicate greater " +
		"seniority.<BR>99999 is typical for a very junior" +
		" right.</html>";
	tips[COL_DCR_AMT + offset] =
		"Decreed amount (ACFT)";
	tips[COL_ON_OFF + offset] =
		"<HTML>0 = OFF<BR>1 = ON<BR>" +
		"YYYY indicates to turn on the right in year YYYY."+
		"<BR>-YYYY indicates to turn off the right in year" +
		" YYYY.</HTML>";
	tips[COL_ACCOUNT_DIST + offset] =
		"Account distribution switch.";
	tips[COL_RIGHT_TYPE + offset] =
		"Right type.";
	tips[COL_FILL_TYPE + offset] =
		"Fill type.";
	tips[COL_OOP_RIGHT + offset] =
		"Out-of-priority associated operational right.";
	return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];

	// the offset is used because in worksheets that have rights data for 1+
	// reservoirs the first column is numbered -1.  The offset calculation
	// allows this column number, which is normally invalid for a table,
	// to be used only for those worksheets that need to display the
	// -1st column.
	int offset = 0;
	if (!__singleReservoir) {
		offset = 1;
		widths[COL_RESERVOIR_ID + offset] = 8;
	}
	
	widths[COL_RIGHT_ID + offset] = 		8;
	widths[COL_RIGHT_NAME + offset] = 		18;
	widths[COL_STRUCT_ID + offset] = 		12;
	widths[COL_ADMIN_NUM + offset] = 		12;
	widths[COL_DCR_AMT + offset] = 			8;
	widths[COL_ON_OFF + offset] = 			6;
	if (__inData) {
		widths[COL_ACCOUNT_DIST + offset] = 		10;
		widths[COL_RIGHT_TYPE + offset] = 		8;
		widths[COL_FILL_TYPE + offset] = 		7;
		widths[COL_OOP_RIGHT + offset] = 		6;
	}
	else {
		widths[COL_ACCOUNT_DIST + offset] = 		14;
		widths[COL_RIGHT_TYPE + offset] = 		17;
		widths[COL_FILL_TYPE + offset] = 		9;
		widths[COL_OOP_RIGHT + offset] = 		6;
	}	

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
	// necessary for table models that display rights for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}
	
	switch (col) {
		case COL_RESERVOIR_ID:	return "%-12s";
		case COL_RIGHT_ID:	return "%-12s";	
		case COL_RIGHT_NAME:	return "%-24s";	
		case COL_STRUCT_ID:	return "%s";	
		case COL_ADMIN_NUM:	return "%-40s";	
		case COL_DCR_AMT:	return "%12.1f";
		case COL_ON_OFF:	return "%d";	
		case COL_ACCOUNT_DIST:	return "%d";	
		case COL_RIGHT_TYPE:	return "%d";	
		case COL_FILL_TYPE:	return "%d";	
		case COL_OOP_RIGHT:	return "%-40s";	
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
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_ReservoirRight rr = (StateMod_ReservoirRight)_data.get(row);

	// necessary for table models that display rights for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}

	switch (col) {
		case COL_RESERVOIR_ID:	return rr.getCgoto();
		case COL_RIGHT_ID:	return rr.getID();
		case COL_RIGHT_NAME:	return rr.getName();
		case COL_STRUCT_ID:	return rr.getCgoto();
		case COL_ADMIN_NUM:	return rr.getRtem();
		case COL_DCR_AMT:	return new Double(rr.getDcrres());
		case COL_ON_OFF:	return new Integer(rr.getSwitch());
		case COL_ACCOUNT_DIST:	return new Integer(rr.getIresco());
		case COL_RIGHT_TYPE:	return new Integer(rr.getItyrstr());
		case COL_FILL_TYPE:	return new Integer(rr.getN2fill());
		case COL_OOP_RIGHT:	return rr.getCopid();
		default:		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model all columns are editable (unless the table is not editable).
@param rowIndex unused
@param col the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int col) {
	if (!__editable) {
		return false;
	}
	return true;
}

/**
Sets whether this table model is being used in a data JFrame.
*/
public void setInDataJFrame(boolean inData) {
	__inData = inData;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	StateMod_ReservoirRight rr = (StateMod_ReservoirRight)_data.get(row);

	// necessary for table models that display rights for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}
		
	switch (col) {
		case COL_RESERVOIR_ID:
			rr.setCgoto((String)value);
			break;
		case COL_RIGHT_ID:	
			rr.setID((String)value);
			break;
		case COL_RIGHT_NAME:	
			rr.setName((String)value);
			break;
		case COL_STRUCT_ID:	
			rr.setCgoto((String)value);
			break;
		case COL_ADMIN_NUM:	
			rr.setRtem((String)value);
			break;
		case COL_DCR_AMT:	
			dval = ((Double)value).doubleValue();
			rr.setDcrres(dval);
			break;
		case COL_ON_OFF:	
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				rr.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				int index = onOff.indexOf(" -");
				ival = new Integer(onOff.substring(0,
					index)).intValue();
				rr.setSwitch(ival);
			}
			break;
		case COL_ACCOUNT_DIST:	
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				rr.setIresco(ival);
			}
			else if (value instanceof String) {
				String acct = (String)value;
				int index = acct.indexOf(" -");
				ival = new Integer(acct.substring(0,
					index)).intValue();
				rr.setIresco(ival);
			}
			break;			
		case COL_RIGHT_TYPE:	
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				rr.setItyrstr(ival);
			}
			else if (value instanceof String) {
				String right = (String)value;
				int index = right.indexOf(" -");
				ival = new Integer(right.substring(0,
					index)).intValue();
				rr.setItyrstr(ival);
			}	
			break;			
		case COL_FILL_TYPE:	
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				rr.setN2fill(ival);
			}
			else if (value instanceof String) {
				String fill = (String)value;
				int index = fill.indexOf(" -");
				ival = new Integer(fill.substring(0,
					index)).intValue();
				rr.setN2fill(ival);
			}
			break;			
		case COL_OOP_RIGHT:	
			rr.setCopid((String)value);
			break;
	}	
	
	if (!__singleReservoir) {
		col++;
	}
	
	super.setValueAt(value, row, col);
}

}
