// StateMod_OperationalRight_Data_TableModel - Table model to display operational right data.

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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
Table model to display operational right data.
*/
@SuppressWarnings("serial")
public class StateMod_OperationalRight_Data_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_OperationalRight> {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 20;

/**
References to columns.
*/
public final static int
	COL_ID = 0,
	COL_NAME = 1,
	COL_ADMINISTRATION_NUMBER = 2,
	COL_MONTH_STR_SWITCH = 3,
	COL_ONOFF_SWITCH = 4,
	COL_DEST = 5,
	COL_DEST_ACCOUNT = 6,
	COL_SOURCE1 = 7,
	COL_SOURCE1_ACCOUNT = 8,
	COL_SOURCE2 = 9,
	COL_SOURCE2_ACCOUNT = 10,
	COL_RULE_TYPE = 11,
	COL_REUSE_PLAN = 12,
	COL_DIVERSION_TYPE = 13,
	COL_LOSS = 14,
	COL_LIMIT = 15,
	COL_START_YEAR = 16,
	COL_END_YEAR = 17,
	COL_MONTHLY_SWITCH = 18,
	COL_INTERVENING_STRUCTURES = 19;
// TODO SAM 2010-12-13 Evaluate whether monthly switch and intervening structures should be separate columns

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
Constructor.  This builds the Model for displaying the diversion data.
@param data the data that will be displayed in the table.
@param editable whether the gui data is editable or not.
*/
public StateMod_OperationalRight_Data_TableModel(List<StateMod_OperationalRight> data, boolean editable){
	if (data == null) {
		_data = new Vector<StateMod_OperationalRight>();
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
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID: return String.class;
		case COL_NAME: return String.class;
		case COL_ADMINISTRATION_NUMBER: return String.class;
		case COL_MONTH_STR_SWITCH: return Integer.class;
		case COL_ONOFF_SWITCH: return Integer.class;
		case COL_DEST: return String.class;
		case COL_DEST_ACCOUNT: return String.class;
		case COL_SOURCE1: return String.class;
		case COL_SOURCE1_ACCOUNT: return String.class;
		case COL_SOURCE2: return String.class;
		case COL_SOURCE2_ACCOUNT: return String.class;
		case COL_RULE_TYPE: return Integer.class;
		case COL_REUSE_PLAN: return String.class;
		case COL_DIVERSION_TYPE: return String.class;
		case COL_LOSS: return Double.class;
		case COL_LIMIT: return Double.class;
		case COL_START_YEAR: return Integer.class;
		case COL_END_YEAR: return Integer.class;
		case COL_MONTHLY_SWITCH: return String.class;
		case COL_INTERVENING_STRUCTURES: return String.class;
	}
	return String.class;
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
		case COL_ID: return "\nID";
		case COL_NAME: return "\nNAME";
		case COL_ADMINISTRATION_NUMBER: return "ADMINISTRATION\nNUMBER";
		case COL_MONTH_STR_SWITCH: return "MONTH/STRUCTURE\nSWITCH";
		case COL_ONOFF_SWITCH: return "\nON/OFF";
		case COL_DEST: return "DESTINATION\nID";
		case COL_DEST_ACCOUNT: return "DESTINATION\nACCOUNT";
		case COL_SOURCE1: return "SOURCE 1\nID";
		case COL_SOURCE1_ACCOUNT: return "SOURCE 1\nACCOUNT";
		case COL_SOURCE2: return "SOURCE 2\nID";
		case COL_SOURCE2_ACCOUNT: return "SOURCE 2\nACCOUNT";
		case COL_RULE_TYPE: return "RULE\nTYPE";
		case COL_REUSE_PLAN: return "REUSE\nPLAN ID";
		case COL_DIVERSION_TYPE: return "DIVERSION\nTYPE";
		case COL_LOSS: return "% TRANSIT\nLOSS";
		case COL_LIMIT: return "CAPACITY\nLIMIT";
		case COL_START_YEAR: return "START\nYEAR";
		case COL_END_YEAR: return "END\nYEAR";
		case COL_MONTHLY_SWITCH: return "MONTHLY SWITCH\nVALUES";
		case COL_INTERVENING_STRUCTURES: return "INTERVENING\nSTRUCTURE IDs";
	}
	return " ";
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_ID: return "%-12.12s";
		case COL_NAME: return "%-24.24s";
		case COL_ADMINISTRATION_NUMBER: return "%-12.12s";
		case COL_MONTH_STR_SWITCH: return "%8.0f";
		case COL_ONOFF_SWITCH: return "%8d";
		case COL_DEST: return "%-12.12s";
		case COL_DEST_ACCOUNT: return "%-8.8s";
		case COL_SOURCE1: return "%-12.12s";
		case COL_SOURCE1_ACCOUNT: return "%-8.8s";
		case COL_SOURCE2: return "%-12.12s";
		case COL_SOURCE2_ACCOUNT: return "%-8.8s";
		case COL_RULE_TYPE: return "%8d";
		case COL_REUSE_PLAN: return "%-12.12s";
		case COL_DIVERSION_TYPE: return "%-12.12s";
		case COL_LOSS: return "%8.2f";
		case COL_LIMIT: return "%12.2f";
		case COL_START_YEAR: return "%d";
		case COL_END_YEAR: return "%d";
		case COL_MONTHLY_SWITCH: return "%24.24s";
		case COL_INTERVENING_STRUCTURES: return "%40.40s";
	}
	return "%8s";
}

/**
Returns the number of rows of data in the table.
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

	StateMod_OperationalRight opr = _data.get(row);
	switch (col) {
		case COL_ID: return opr.getID();
		case COL_NAME: return opr.getName();
		case COL_ADMINISTRATION_NUMBER: return opr.getRtem();
		case COL_MONTH_STR_SWITCH: return Integer.valueOf(opr.getDumx());
		case COL_ONOFF_SWITCH: return Integer.valueOf(opr.getSwitch());
		case COL_DEST: return opr.getCiopde();
		case COL_DEST_ACCOUNT: return opr.getIopdes();
		case COL_SOURCE1: return opr.getCiopso1();
		case COL_SOURCE1_ACCOUNT: return opr.getIopsou1();
		case COL_SOURCE2: return opr.getCiopso2();
		case COL_SOURCE2_ACCOUNT: return opr.getIopsou2();
		case COL_RULE_TYPE: return Integer.valueOf(opr.getItyopr());
		case COL_REUSE_PLAN: return opr.getCreuse();
		case COL_DIVERSION_TYPE: return opr.getCdivtyp();
		case COL_LOSS: return Double.valueOf(opr.getOprLoss());
		case COL_LIMIT: return Double.valueOf(opr.getOprLimit());
		case COL_START_YEAR: return Integer.valueOf(opr.getIoBeg());
		case COL_END_YEAR: return Integer.valueOf(opr.getIoEnd());
		case COL_MONTHLY_SWITCH:
			int [] imonsw = opr.getImonsw();
			if ( (imonsw == null) || (imonsw.length == 0) ) {
				return "";
			}
			else {
				StringBuffer b = new StringBuffer();
				for ( int i = 0; i < imonsw.length; i++ ) {
					if ( i > 0 ) {
						b.append(",");
					}
					b.append("" + imonsw[i]);
				}
				return b.toString();
			}
		case COL_INTERVENING_STRUCTURES:
			String [] intern = opr.getIntern();
			if ( (intern == null) || (intern.length == 0) ) {
				return "";
			}
			else {
				StringBuffer b = new StringBuffer();
				for ( int i = 0; i < intern.length; i++ ) {
					if ( i > 0 ) {
						b.append(",");
					}
					b.append("" + intern[i]);
				}
				return b.toString();
			}
		default: return "";
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
	widths[COL_ID] = 12;
	widths[COL_NAME] = 23;
	widths[COL_ADMINISTRATION_NUMBER] = 12;
	widths[COL_MONTH_STR_SWITCH] = 14;
	widths[COL_ONOFF_SWITCH] = 4;
	widths[COL_DEST] = 10;
	widths[COL_DEST_ACCOUNT] = 9;
	widths[COL_SOURCE1] = 10;
	widths[COL_SOURCE1_ACCOUNT] = 9;
	widths[COL_SOURCE2] = 10;
	widths[COL_SOURCE2_ACCOUNT] = 9;
	widths[COL_RULE_TYPE] = 5;
	widths[COL_REUSE_PLAN] = 12;
	widths[COL_DIVERSION_TYPE] = 7;
	widths[COL_LOSS] = 8;
	widths[COL_LIMIT] = 8;
	widths[COL_START_YEAR] = 5;
	widths[COL_END_YEAR] = 5;
	widths[COL_MONTHLY_SWITCH] = 13;
	widths[COL_INTERVENING_STRUCTURES] = 20;

	return widths;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}
	if (columnIndex > 1) {
		return true;
	}
	return false;
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
/* TODO SAM 2010-12-13 Not sure if this is used but if so enable

	switch (col) {
		case COL_STRUCTURE_ID:	
			__interns.set(row, value);
			__parentOperationalRight.setInterns(__interns);
		break;
	}

	super.setValueAt(value, row, col);	
	*/
}	

}
