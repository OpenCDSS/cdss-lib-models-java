// ----------------------------------------------------------------------------
// StateMod_ReservoirRight_Data_TableModel - table model for displaying 
//	reservoir right data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-04-04	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-11	JTS, RTi		Corrected the classes returned from
//							getColumnClass().
// 2007-04-27	Kurt Tometich, RTi		Added getValidators method for check
//									file and data check implementation.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;

/**
This table model displays reservoir right data.  The model can display rights
data for a single reservoir or for 1+ reservoirs.  The difference is specified
in the constructor and affects how many columns of data are shown.
*/
public class StateMod_ReservoirRight_Data_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateMod_Data_TableModel {

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
Whether the table data is editable or not.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified or not.
*/
public StateMod_ReservoirRight_Data_TableModel(Vector data, boolean editable) {
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
@param col the column for which to return the data class.
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
	switch (col) {
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

	tips[COL_RIGHT_ID] =
		"<html>The reservoir right ID is typically the reservoir" +
		" station ID<br> followed by .01, .02, etc.</html>";
	tips[COL_RIGHT_NAME] =
		"Reservoir right name";
	tips[COL_STRUCT_ID] =
		"<HTML>The reservoir ID is the link between reservoir stations "
		+ "and their right(s).</HTML>";
	tips[COL_ADMIN_NUM] =
		"<HTML>Lower admininistration numbers indicate greater " +
		"seniority.<BR>99999 is typical for a very junior" +
		" right.</html>";
	tips[COL_DCR_AMT] =
		"Decreed amount (ACFT)";
	tips[COL_ON_OFF] =
		"<HTML>0 = OFF<BR>1 = ON<BR>" +
		"YYYY indicates to turn on the right in year YYYY."+
		"<BR>-YYYY indicates to turn off the right in year" +
		" YYYY.</HTML>";
	tips[COL_ACCOUNT_DIST] =
		"Account distribution switch.";
	tips[COL_RIGHT_TYPE] =
		"Right type.";
	tips[COL_FILL_TYPE] =
		"Fill type.";
	tips[COL_OOP_RIGHT] =
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

	widths[COL_RIGHT_ID] = 		8;
	widths[COL_RIGHT_NAME] = 		18;
	widths[COL_STRUCT_ID] = 		12;
	widths[COL_ADMIN_NUM] = 		12;
	widths[COL_DCR_AMT] = 			8;
	widths[COL_ON_OFF] = 			6;
	widths[COL_ACCOUNT_DIST] = 		10;
	widths[COL_RIGHT_TYPE] = 		8;
	widths[COL_FILL_TYPE] = 		7;
	widths[COL_OOP_RIGHT] = 		6;

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
	switch (col) {
		case COL_RIGHT_ID:	return "%-12.12s";	
		case COL_RIGHT_NAME:	return "%-24.24s";	
		case COL_STRUCT_ID:	return "%-8.8s";	
		case COL_ADMIN_NUM:	return "%-20.20s";	
		case COL_DCR_AMT:	return "%12.1f";
		case COL_ON_OFF:	return "%8d";	
		case COL_ACCOUNT_DIST:	return "%8d";	
		case COL_RIGHT_TYPE:	return "%8d";	
		case COL_FILL_TYPE:	return "%8d";	
		case COL_OOP_RIGHT:	return "%-12.12s";	
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
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators( int col ) {
	Validator[] no_checks = new Validator[] {};
	// Reservior right type must be -1 or 1
	Validator [] right_type = new Validator[] {
		Validators.isEquals( new Integer( -1 ) ),
		Validators.isEquals( new Integer( 1 ) ) };
	Validator [] type_Validators = new Validator[] {
		Validators.or( right_type ) };
	// Reservoir fill type must be 1 or 2
	Validator [] fill_type = new Validator[] {
		Validators.isEquals( new Integer( 1 ) ),
		Validators.isEquals( new Integer( 2 ) ) };
	Validator [] fill_Validators = new Validator[] {
		Validators.or( fill_type ) };
		
	switch (col) {
	case COL_RIGHT_ID:		return ids;
	case COL_RIGHT_NAME:	return blank;
	case COL_STRUCT_ID:		return ids;
	case COL_ADMIN_NUM:		return nums;
	case COL_DCR_AMT:		return nums;
	case COL_ON_OFF:		return nums;
	case COL_ACCOUNT_DIST:	return nums;
	case COL_RIGHT_TYPE:	return type_Validators;
	case COL_FILL_TYPE:		return fill_Validators;
	case COL_OOP_RIGHT:		return nums;
	default:				return no_checks;
	}
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

	StateMod_ReservoirRight rr 
		= (StateMod_ReservoirRight)_data.elementAt(row);

	switch (col) {
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
	StateMod_ReservoirRight rr 
		= (StateMod_ReservoirRight)_data.elementAt(row);

	switch (col) {
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
	
	super.setValueAt(value, row, col);
}

}
