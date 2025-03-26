// StateMod_ReservoirAccount_Data_TableModel - table model for displaying reservoir account data

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
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays reservoir account data.  The model can display 
account data for a single reservoir or for 1+ reservoirs.  The difference is
specified in the constructor and affects how many columns of data are shown.
*/
@SuppressWarnings("serial")
public class StateMod_ReservoirAccount_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_ReservoirAccount> {

/**
Number of columns in the table model.  
*/
private int __COLUMNS = 7;

/**
References to columns.
*/
public final static int
	COL_RESERVOIR_ID =	0,
	COL_OWNER_ID =		1,
	COL_OWNER_ACCOUNT =	2,
	COL_MAX_STORAGE =	3,
	COL_INITIAL_STORAGE =	4,
	COL_PRORATE_EVAP =	5,
	COL_OWNERSHIP_TIE =	6;
	
/**
Whether the table data is editable.
*/
private boolean __editable = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data can be modified.
*/
public StateMod_ReservoirAccount_Data_TableModel(List<StateMod_ReservoirAccount> data, boolean editable){
	if (data == null) {
		_data = new Vector<StateMod_ReservoirAccount>();
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
public Class<?> getColumnClass (int col) {
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
	switch (col) {
		case COL_RESERVOIR_ID:	return "\nRESERVOIR\nID";
		case COL_OWNER_ID:	return "\nOWNER\nID";
		case COL_OWNER_ACCOUNT:	return "\nOWNER\nACCOUNT";
		case COL_MAX_STORAGE:	return "MAXIMUM\nSTORAGE\n(ACFT)";
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

	tips[COL_RESERVOIR_ID] =
		"<html>The reservoir station ID of the reservoir to "
		+ "which<br>the rights belong.</html>";
	tips[COL_OWNER_ID] 
		= "Sequential number 1+ (not used by StateMod)";
	tips[COL_OWNER_ACCOUNT] = "Account name.";
	tips[COL_MAX_STORAGE] = "Maximum account storage (ACFT).";
	tips[COL_INITIAL_STORAGE] = "Initial account storage (ACFT).";
	tips[COL_PRORATE_EVAP] = "How to prorate evaporation.";
	tips[COL_OWNERSHIP_TIE] = "One fill rule calculation flag.";
	return tips;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int col) {
	switch (col) {
		case COL_RESERVOIR_ID:		return "%-12.12s";
		case COL_OWNER_ID:		return "%-12.12s";	
		case COL_OWNER_ACCOUNT:		return "%-24.24s";	
		case COL_MAX_STORAGE:		return "%12.1f";
		case COL_INITIAL_STORAGE:	return "%12.1f";
		case COL_PRORATE_EVAP:		return "%8.0f";
		case COL_OWNERSHIP_TIE:		return "%8d";	
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

	widths[COL_RESERVOIR_ID] = 8;
	widths[COL_OWNER_ID] = 	7;
	widths[COL_OWNER_ACCOUNT] = 	14;
	widths[COL_MAX_STORAGE] = 	7;
	widths[COL_INITIAL_STORAGE] = 	7;
	widths[COL_PRORATE_EVAP] = 	10;
	widths[COL_OWNERSHIP_TIE] = 	9;

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
				String s = ((String)value).substring( 0, index);
				dval = (Double.valueOf(s)).doubleValue();
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
	
	super.setValueAt(value, row, col);
}

}
