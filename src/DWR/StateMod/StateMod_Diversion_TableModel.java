// StateMod_Diversion_TableModel - Table model for displaying data in the diversion station tables.

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
This table model display data in diversion tables.
*/
@SuppressWarnings("serial")
public class StateMod_Diversion_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_Diversion> {

/**
Number of columns in the table model (this includes all data - other code
can hide columns if necessary).
*/
private int __COLUMNS = 25;

/**
References to columns.
*/
public final static int
	COL_ID =			0,
	COL_NAME =			1,
	COL_RIVER_NODE_ID =		2,
	COL_ON_OFF =			3,
	COL_CAPACITY =			4,
	COL_REPLACE_RES_OPTION =	5,
	COL_DAILY_ID =			6,
	COL_USER_NAME = 		7,
	COL_DEMAND_TYPE =		8,
	COL_EFF_ANNUAL =		9,
	COL_AREA =			10,
	COL_USE_TYPE =			11,
	COL_DEMAND_SOURCE =		12,
	COL_EFF_01 =			13,
	COL_EFF_02 =			14,
	COL_EFF_03 =			15,
	COL_EFF_04 =			16,
	COL_EFF_05 =			17,
	COL_EFF_06 =			18,
	COL_EFF_07 =			19,
	COL_EFF_08 =			20,
	COL_EFF_09 =			21,
	COL_EFF_10 =			22,
	COL_EFF_11 =			23,
	COL_EFF_12 =			24;

/**
Whether the table data can be edited or not.
*/
private boolean __editable = true;

/**
Constructor.  This builds the Model for displaying the diversion station data.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Diversion_TableModel(List<StateMod_Diversion> data, boolean editable)
throws Exception {
	this(null, data, editable, false);
}

/**
Constructor.  This builds the Model for displaying the diversion station data.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not
@param compactForm if true, then the compact form of the table model will be
used.  In the compact form, only the name and ID are shown.  If false, all fields will be shown.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Diversion_TableModel(List<StateMod_Diversion> data, boolean editable, boolean compactForm)
throws Exception {
	this(null, data, editable, compactForm);
}

/**
Constructor.  This builds the Model for displaying the diversion data.
@param dataset the dataset for the data being displayed.  Only necessary for return flow tables.
@param data the data that will be displayed in the table.
@param editable whether the data can be edited or not
@param compactForm if true, then the compact form of the table model will be
used.  In the compact form, only the name and ID are shown.  If false, all fields will be shown.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Diversion_TableModel(StateMod_DataSet dataset, List<StateMod_Diversion> data, boolean editable, boolean compactForm)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateMod_Diversion_TableModel constructor.");
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
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:		return String.class;
		case COL_NAME:		return String.class;
		case COL_RIVER_NODE_ID:	return String.class;
		case COL_ON_OFF:	return Integer.class;
		case COL_CAPACITY:	return Double.class;
		case COL_REPLACE_RES_OPTION:	return Integer.class;
		case COL_DAILY_ID:	return String.class;
		case COL_USER_NAME:	return String.class;
		case COL_DEMAND_TYPE:	return Integer.class;
		case COL_EFF_ANNUAL:	return Double.class;
		case COL_AREA:		return Double.class;
		case COL_USE_TYPE:	return Integer.class;
		case COL_DEMAND_SOURCE:	return Integer.class;
		case COL_EFF_01:	return Double.class;
		case COL_EFF_02:	return Double.class;
		case COL_EFF_03:	return Double.class;
		case COL_EFF_04:	return Double.class;
		case COL_EFF_05:	return Double.class;
		case COL_EFF_06:	return Double.class;
		case COL_EFF_07:	return Double.class;
		case COL_EFF_08:	return Double.class;
		case COL_EFF_09:	return Double.class;
		case COL_EFF_10:	return Double.class;
		case COL_EFF_11:	return Double.class;
		case COL_EFF_12:	return Double.class;
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
		case COL_CAPACITY:	return "CAPACITY\n(CFS)";
		case COL_REPLACE_RES_OPTION:	return "REPLACE.\nRES. OPTION";
		case COL_DAILY_ID:	return "DAILY\nID";
		case COL_USER_NAME:	return "USER\nNAME";
		case COL_DEMAND_TYPE:	return "DEMAND\nTYPE";
		case COL_EFF_ANNUAL:	return "EFFICIENCY\nANNUAL (%)";
		case COL_AREA:		return "AREA\n(ACRE)";
		case COL_USE_TYPE:	return "USE\nTYPE";
		case COL_DEMAND_SOURCE:	return "DEMAND\nSOURCE";
		case COL_EFF_01:	return "EFFICIENCY\nMONTH 1";
		case COL_EFF_02:	return "EFFICIENCY\nMONTH 2";
		case COL_EFF_03:	return "EFFICIENCY\nMONTH 3";
		case COL_EFF_04:	return "EFFICIENCY\nMONTH 4";
		case COL_EFF_05:	return "EFFICIENCY\nMONTH 5";
		case COL_EFF_06:	return "EFFICIENCY\nMONTH 6";
		case COL_EFF_07:	return "EFFICIENCY\nMONTH 7";
		case COL_EFF_08:	return "EFFICIENCY\nMONTH 8";
		case COL_EFF_09:	return "EFFICIENCY\nMONTH 9";
		case COL_EFF_10:	return "EFFICIENCY\nMONTH 10";
		case COL_EFF_11:	return "EFFICIENCY\nMONTH 11";
		case COL_EFF_12:	return "EFFICIENCY\nMONTH 12";
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
		case COL_CAPACITY:	return "%.2f";
		case COL_REPLACE_RES_OPTION:	return "%d";
		case COL_DAILY_ID:	return "%-12s";
		case COL_USER_NAME:	return "%-24s";
		case COL_DEMAND_TYPE:	return "%d";
		case COL_EFF_ANNUAL:	return "%.1f";
		case COL_AREA:		return "%.2f";
		case COL_USE_TYPE:	return "%d";
		case COL_DEMAND_SOURCE:	return "%d";
		case COL_EFF_01:	return "%10.2f";
		case COL_EFF_02:	return "%10.2f";
		case COL_EFF_03:	return "%10.2f";
		case COL_EFF_04:	return "%10.2f";
		case COL_EFF_05:	return "%10.2f";
		case COL_EFF_06:	return "%10.2f";
		case COL_EFF_07:	return "%10.2f";
		case COL_EFF_08:	return "%10.2f";
		case COL_EFF_09:	return "%10.2f";
		case COL_EFF_10:	return "%10.2f";
		case COL_EFF_11:	return "%10.2f";
		case COL_EFF_12:	return "%10.2f";
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
Returns the data that should be placed in the JTable at the given row and 
column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_Diversion smd = _data.get(row);
	switch (col) {
		case COL_ID:		return smd.getID();
		case COL_NAME:		return smd.getName();
		case COL_RIVER_NODE_ID:	return smd.getCgoto();
		case COL_ON_OFF:	return Integer.valueOf(smd.getSwitch());
		case COL_CAPACITY:	return Double.valueOf(smd.getDivcap());
		case COL_REPLACE_RES_OPTION:
					return Integer.valueOf(smd.getIreptype());
		case COL_DAILY_ID:	return smd.getCdividy();
		case COL_USER_NAME:	return smd.getUsername();
		case COL_DEMAND_TYPE:	return Integer.valueOf(smd.getIdvcom());
		case COL_EFF_ANNUAL:	return Double.valueOf(smd.getDivefc());
		case COL_AREA:		return Double.valueOf(smd.getArea());
		case COL_USE_TYPE:	return Integer.valueOf(smd.getIrturn());
		case COL_DEMAND_SOURCE:	return Integer.valueOf(smd.getDemsrc());
		case COL_EFF_01:	return Double.valueOf(smd.getDiveff(0));
		case COL_EFF_02:	return Double.valueOf(smd.getDiveff(1));
		case COL_EFF_03:	return Double.valueOf(smd.getDiveff(2));
		case COL_EFF_04:	return Double.valueOf(smd.getDiveff(3));
		case COL_EFF_05:	return Double.valueOf(smd.getDiveff(4));
		case COL_EFF_06:	return Double.valueOf(smd.getDiveff(5));
		case COL_EFF_07:	return Double.valueOf(smd.getDiveff(6));
		case COL_EFF_08:	return Double.valueOf(smd.getDiveff(7));
		case COL_EFF_09:	return Double.valueOf(smd.getDiveff(8));
		case COL_EFF_10:	return Double.valueOf(smd.getDiveff(9));
		case COL_EFF_11:	return Double.valueOf(smd.getDiveff(10));
		case COL_EFF_12:	return Double.valueOf(smd.getDiveff(11));
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
	widths[COL_CAPACITY] =		7;
	widths[COL_REPLACE_RES_OPTION]= 8;
	widths[COL_DAILY_ID] =		8;
	widths[COL_USER_NAME] =		18;
	widths[COL_DEMAND_TYPE] =	5;
	widths[COL_EFF_ANNUAL] =	8;
	widths[COL_AREA] =		6;	// Wider than title for big
						// ditches
	widths[COL_USE_TYPE] =		7;
	widths[COL_DEMAND_SOURCE] =	6;
	widths[COL_EFF_01] = 8;
	widths[COL_EFF_02] = 8;
	widths[COL_EFF_03] = 8;
	widths[COL_EFF_04] = 8;
	widths[COL_EFF_05] = 8;
	widths[COL_EFF_06] = 8;
	widths[COL_EFF_07] = 8;
	widths[COL_EFF_08] = 8;
	widths[COL_EFF_09] = 8;
	widths[COL_EFF_10] = 8;
	widths[COL_EFF_11] = 8;
	widths[COL_EFF_12] = 8;
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

	StateMod_Diversion smd = _data.get(row);

	switch (col) {
		case COL_ID:
			smd.setID((String)value);
			break;
		case COL_NAME:
			smd.setName((String)value);
			break;
		case COL_RIVER_NODE_ID:
			smd.setCgoto((String)value);
			break;
		case COL_ON_OFF:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smd.setSwitch(ival);
			}
			else if (value instanceof String) {
				String onOff = (String)value;
				index = onOff.indexOf(" -");
				ival = Integer.valueOf( onOff.substring(0, index)).intValue();
				smd.setSwitch(ival);
			}
			break;
		case COL_CAPACITY:
			smd.setDivcap((Double)value);
			break;
		case COL_REPLACE_RES_OPTION:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
				smd.setIreptype(ival);
			}
			else if (value instanceof String) {
				String ireptyp = (String)value;
				index = ireptyp.indexOf(" -");
				ival = Integer.valueOf( ireptyp.substring(0, index)).intValue();
				smd.setIreptype(ival);
			}
			break;
		case COL_DAILY_ID:
			smd.setCdividy((String)value);
			break;
		case COL_USER_NAME:
			smd.setUsername((String)value);
			break;
		case COL_DEMAND_TYPE:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smd.setIdvcom(ival);
			}
			else if (value instanceof String) {
				String idvcom = (String)value;
				index = idvcom.indexOf(" -");
				ival = Integer.valueOf( idvcom.substring(0, index)).intValue();
				smd.setIdvcom(ival);
			}
			break;
		case COL_EFF_ANNUAL:
			smd.setDivefc((Double)value);
			break;
		case COL_AREA:
			smd.setArea((Double)value);
			break;
		case COL_USE_TYPE:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smd.setIrturn(ival);
			}
			else if (value instanceof String) {
				String irturn = (String)value;
				index = irturn.indexOf(" -");
				ival = Integer.valueOf( irturn.substring(0, index)).intValue();
				smd.setIrturn(ival);
			}
			break;
		case COL_DEMAND_SOURCE:
			if (value instanceof Integer) {
				ival = ((Integer)value).intValue();
					smd.setDemsrc(ival);
			}
			else if (value instanceof String) {
				String demsrc = (String)value;
				index = demsrc.indexOf(" -");
				ival = Integer.valueOf( demsrc.substring(0, index)).intValue();
				smd.setDemsrc(ival);
			}
			break;
		case COL_EFF_01:	
			smd.setDiveff(0, (Double)value);
			break;
		case COL_EFF_02:
			smd.setDiveff(1, (Double)value);
			break;
		case COL_EFF_03:
			smd.setDiveff(2, (Double)value);
			break;
		case COL_EFF_04:
			smd.setDiveff(3, (Double)value);
			break;
		case COL_EFF_05:
			smd.setDiveff(4, (Double)value);
			break;
		case COL_EFF_06:
			smd.setDiveff(5, (Double)value);
			break;
		case COL_EFF_07:
			smd.setDiveff(6, (Double)value);
			break;
		case COL_EFF_08:
			smd.setDiveff(7, (Double)value);
			break;
		case COL_EFF_09:
			smd.setDiveff(8, (Double)value);
			break;
		case COL_EFF_10:
			smd.setDiveff(9, (Double)value);
			break;
		case COL_EFF_11:
			smd.setDiveff(10, (Double)value);
			break;
		case COL_EFF_12:
			smd.setDiveff(11, (Double)value);
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
		"<html>The diversion station identifier is the main link" +
		" between diversion data<BR>" +
		"and must be unique in the data set.</html>";
	tips[COL_NAME] = 
		"Diversion station name.";

	if (__COLUMNS == 2) {
		return tips;
	}

	tips[COL_RIVER_NODE_ID] =
		"River node where diversion station is located.";
	tips[COL_ON_OFF] =
		"Indicates whether diversion station is on (1) or off (0)";
	tips[COL_CAPACITY] = "Diversion station capacity (CFS)";
	tips[COL_REPLACE_RES_OPTION]= "Replacement reservoir option.";
	tips[COL_DAILY_ID] = "Daily identifier (for daily time series).";
	tips[COL_USER_NAME] = "User name.";
	tips[COL_DEMAND_TYPE] =	"(Monthly) demand type.";
	tips[COL_EFF_ANNUAL] =
		"Efficiency, annual (%).  Negative indicates "
		+ "monthly efficiencies.";
	tips[COL_AREA] = "Irrigated area (ACRE).";
	tips[COL_USE_TYPE] = "Use type.";
	tips[COL_DEMAND_SOURCE] = "Demand source.";
	tips[COL_EFF_01] = "Diversion efficiency for month 1 of year.";
	tips[COL_EFF_02] = "Diversion efficiency for month 2 of year.";
	tips[COL_EFF_03] = "Diversion efficiency for month 3 of year.";
	tips[COL_EFF_04] = "Diversion efficiency for month 4 of year.";
	tips[COL_EFF_05] = "Diversion efficiency for month 5 of year.";
	tips[COL_EFF_06] = "Diversion efficiency for month 6 of year.";
	tips[COL_EFF_07] = "Diversion efficiency for month 7 of year.";
	tips[COL_EFF_08] = "Diversion efficiency for month 8 of year.";
	tips[COL_EFF_09] = "Diversion efficiency for month 9 of year.";
	tips[COL_EFF_10] = "Diversion efficiency for month 10 of year.";
	tips[COL_EFF_11] = "Diversion efficiency for month 11 of year.";
	tips[COL_EFF_12] = "Diversion efficiency for month 12 of year.";
	return tips;
}

}
