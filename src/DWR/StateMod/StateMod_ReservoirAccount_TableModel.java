// StateMod_ReservoirAccount_TableModel - table model for displaying reservoir account data

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

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays reservoir account data.  The model can display 
account data for a single reservoir or for 1+ reservoirs.  The difference is
specified in the constructor and affects how many columns of data are shown.
*/
@SuppressWarnings("serial")
public class StateMod_ReservoirAccount_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_ReservoirAccount> {

/**
Number of columns in the table model.  For table models that display accounts
for a single reservoir, the tables only have 6 columns.  Table models that
display accounts for 1+ reservoirs have 7.  The variable is modified in the
constructor.
*/
private int __COLUMNS = 6;

/**
References to columns.
*/
public final static int
	COL_RESERVOIR_ID =	-1,
	COL_OWNER_ID =		0,	// REVISIT SAM 2004-10-28
	COL_OWNER_ACCOUNT =	1,	// COL_OWNER_ID is not actually in the
	COL_MAX_STORAGE =	2,	// StateMod file -- it is a sequential
	COL_INITIAL_STORAGE =	3,	// number 1+
	COL_PRORATE_EVAP =	4,
	COL_OWNERSHIP_TIE =	5;
	
/**
Whether the table data is editable.
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
@param editable whether the table data can be modified.
@param singleReservoir if true, then the table model is set up to only display
a single reservoir's right data.  This means that the reservoir ID field will
not be shown.  If false then the reservoir right field will be included.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_ReservoirAccount_TableModel(List<StateMod_ReservoirAccount> data, boolean editable,
boolean singleReservoir)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_ReservoirAccount_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
	__singleReservoir = singleReservoir;

	if (!__singleReservoir) {
		// this is done because if accounts for 1+ reservoirs are
		// shown in the table the ID for the associated reservoirs
		// needs shown as well.  So instead of the usual 6 columns
		// of data, an additional one is necessary.
		__COLUMNS++;
	}
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class<?> getColumnClass (int col) {
	// necessary for table models that display accounts for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}
	
	switch (col) {
		case COL_RESERVOIR_ID:		return String.class;
		case COL_OWNER_ID:		return String.class;
		case COL_OWNER_ACCOUNT:		return String.class;
		case COL_MAX_STORAGE:		return Double.class;
		case COL_INITIAL_STORAGE:	return Double.class;
		case COL_PRORATE_EVAP:		return Double.class;
		case COL_OWNERSHIP_TIE:		return Integer.class;
		default:			return String.class;
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
	// necessary for table models that display accounts for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}
	
	switch (col) {
		case COL_RESERVOIR_ID:		return "\nRESERVOIR\nID";
		case COL_OWNER_ID:		return "\nOWNER\nID";
		case COL_OWNER_ACCOUNT:		return "\nOWNER\nACCOUNT";
		case COL_MAX_STORAGE:	 return "MAXIMUM\nSTORAGE\n(ACFT)";
		case COL_INITIAL_STORAGE: return "INITIAL\nSTORAGE\n(ACFT)";
		case COL_PRORATE_EVAP:	
			return "EVAPORATION\nDISTRIBUTION\nFLAG";
		case COL_OWNERSHIP_TIE:		
			return "ACCOUNT\nONE FILL\nCALCULATION";
		default:			return " ";
	}	
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	// the offset is used because in tables that have accounts data for 1+
	// reservoirs the first column is numbered -1 at the top of the class.
	// The offset calculation allows this column number, which is usually
	// invalid for a table, to be used only for those tables that need to
	// display the -1st column.
	int offset = 0;
	if (!__singleReservoir) {
		offset = 1;
		tips[COL_RESERVOIR_ID + offset] =
			"<html>The reservoir station ID of the reservoir to "
			+ "which<br>the rights belong.</html>";
	}

	tips[COL_OWNER_ID + offset] 
		= "Sequential number 1+ (not used by StateMod)";
	tips[COL_OWNER_ACCOUNT + offset] = "Account name.";
	tips[COL_MAX_STORAGE + offset] = "Maximum account storage (ACFT).";
	tips[COL_INITIAL_STORAGE + offset] = "Initial account storage (ACFT).";
	tips[COL_PRORATE_EVAP + offset] = "How to prorate evaporation.";
	tips[COL_OWNERSHIP_TIE + offset] = "One fill rule calculation flag.";
	return tips;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int col) {
	// necessary for table models that display accounts for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}

	switch (col) {
		case COL_RESERVOIR_ID:		return "%-8s";
		case COL_OWNER_ID:		return "%-8s";	
		case COL_OWNER_ACCOUNT:		return "%-40s";	
		case COL_MAX_STORAGE:		return "%12.1f";
		case COL_INITIAL_STORAGE:	return "%12.1f";
		case COL_PRORATE_EVAP:		return "%.0f";
		case COL_OWNERSHIP_TIE:		return "%d";	
		default:			return "%-8s";
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

	StateMod_ReservoirAccount rac = (StateMod_ReservoirAccount)_data.get(row);

	// necessary for table models that display accounts for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}

	switch (col) {
		case COL_RESERVOIR_ID:	return rac.getCgoto();
		case COL_OWNER_ID:	return rac.getID();
		case COL_OWNER_ACCOUNT:	return rac.getName();
		case COL_MAX_STORAGE:	return Double.valueOf(rac.getOwnmax());
		case COL_INITIAL_STORAGE:	
					return Double.valueOf(rac.getCurown());
		case COL_PRORATE_EVAP:	return Double.valueOf(rac.getPcteva());
		case COL_OWNERSHIP_TIE: return Integer.valueOf(rac.getN2own());
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

	// the offset is used because in tables that have accounts data for 1+
	// reservoirs the first column is numbered -1 at the top of the class.
	// The offset calculation allows this column number, which is usually
	// invalid for a table, to be used only for those tables that need to
	// display the -1st column.
	int offset = 0;
	if (!__singleReservoir) {
		offset = 1;
		widths[COL_RESERVOIR_ID + offset] = 8;
	}

	widths[COL_OWNER_ID + offset] = 	7;
	widths[COL_OWNER_ACCOUNT + offset] = 	14;
	widths[COL_MAX_STORAGE + offset] = 	7;
	widths[COL_INITIAL_STORAGE + offset] = 	7;
	widths[COL_PRORATE_EVAP + offset] = 	10;
	widths[COL_OWNERSHIP_TIE + offset] = 	9;

	return widths;
}

/**
Returns whether the cell at the given position is editable.  All columns
are editable unless the table is not editable.
@param rowIndex unused
@param col the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int col) {
	// necessary for table models that display accounts for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}

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
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	StateMod_ReservoirAccount rac = _data.get(row);

	// necessary for table models that display accounts for 1+ reservoirs,
	// so that the -1st column (ID) can also be displayed.  By doing it
	// this way, code can be shared between the two kinds of table models
	// and less maintenance is necessary.
	if (!__singleReservoir) {
		col--;
	}
	
	switch (col) {
		case COL_RESERVOIR_ID:
			rac.setCgoto((String)value);
			break;
		case COL_OWNER_ID:	
			rac.setID((String)value);
			break;
		case COL_OWNER_ACCOUNT:	
			rac.setName((String)value);
			break;
		case COL_MAX_STORAGE:	
			dval = ((Double)value).doubleValue();
			rac.setOwnmax(dval);
			break;
		case COL_INITIAL_STORAGE:	
			dval = ((Double)value).doubleValue();
			rac.setCurown(dval);
			break;
		case COL_PRORATE_EVAP:	
			if (value instanceof Double) {
				dval = ((Double)value).doubleValue();
				rac.setPcteva(dval);
			}
			else if (value instanceof String) {
				int index=((String)value).indexOf(" -");
				String s = ((String)value).substring(
					0, index);
				dval = Double.valueOf(s).doubleValue();
				rac.setPcteva(dval);
			}
			break;
		case COL_OWNERSHIP_TIE:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				rac.setN2own(ival);
			}
			else if (value instanceof String) {
				String n2owns = (String)value;
				int index = n2owns.indexOf(" -");
				ival = Integer.valueOf( n2owns.substring(0, index)).intValue();
				rac.setN2own(ival);
			}
			break;
	}	
	
	if (!__singleReservoir) {
		col++;
	}
	
	super.setValueAt(value, row, col);
}

}
