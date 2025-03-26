// StateCU_ClimateStation_TableModel - table model to display climate station data

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

package DWR.StateCU;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class is a table model for displaying StateCU climate station data.
*/
@SuppressWarnings("serial")
public class StateCU_ClimateStation_TableModel 
extends JWorksheet_AbstractRowTableModel<StateCU_ClimateStation> implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 8;

/**
References to the columns.
*/
private final int
	__COL_ID = 0,
	__COL_NAME = 1,
	__COL_LATITUDE = 2,
	__COL_ELEVATION = 3,
	__COL_REGION1 = 4,
	__COL_REGION2 = 5,
	__COL_ZH = 6,
	__COL_ZM = 7;

/**
Whether the data are editable or not.
*/
private boolean __editable = false;

/**
The parent climate station for which subdata is displayed.
*/
// TODO SAM 2007-03-01 Evaluate use
//private StateCU_ClimateStation __parentStation;

/**
Constructor.  This builds the Model for displaying climate station data
@param data the data that will be displayed in the table.
@throws Exception if invalid data or dmi was passed in.
*/
public StateCU_ClimateStation_TableModel(List<StateCU_ClimateStation> data)
throws Exception {
	this(data, true);
}

/**
Constructor.  This builds the Model for displaying climate station data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
@throws Exception if invalid data or dmi was passed in.
*/
public StateCU_ClimateStation_TableModel(List<StateCU_ClimateStation> data, boolean editable)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data list passed to " 
			+ "StateCU_ClimateStation_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_ID: return String.class;
		case __COL_NAME: return String.class;
		case __COL_ELEVATION: return Double.class;
		case __COL_LATITUDE: return Double.class;
		case __COL_REGION1:	return String.class;
		case __COL_REGION2:	return String.class;
		case __COL_ZH: return Double.class;
		case __COL_ZM: return Double.class;
		default: return String.class;
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
		case __COL_ID: return "\nID";
		case __COL_NAME: return "\nNAME";
		case __COL_ELEVATION: return "ELEVATION\n(FT)";
		case __COL_LATITUDE: return "LATITUDE\n(DEC. DEG.)";
		case __COL_REGION1: return "\nREGION1";
		case __COL_REGION2: return "\nREGION2";
		case __COL_ZH: return "HEIGHT HUMID.&TEMP.\nMEASUREMENTS (FT)";
		case __COL_ZM: return "HEIGHT WIND\nMEASUREMENT (FT)";
		default: return " ";
	}
}


/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case __COL_ID: return "%-20.20s";	
		case __COL_NAME: return "%-20.20s";	
		case __COL_ELEVATION: return "%10.2f";
		case __COL_LATITUDE: return "%10.2f";
		case __COL_REGION1: return "%-20.20s";
		case __COL_REGION2: return "%-20.20s";
		case __COL_ZH: return "%8.2f";
		case __COL_ZM: return "%8.2f";
		default: return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
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
	
	switch ( col ) {
		case __COL_ID: return ids;
		case __COL_NAME: return blank;
		case __COL_ELEVATION: return nums;
		case __COL_LATITUDE: return nums;
		case __COL_REGION1: return blank;
		case __COL_REGION2: return blank;
		default: return no_checks;
	}	
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

	StateCU_ClimateStation station = (StateCU_ClimateStation)_data.get(row);
	switch (col) {
		case __COL_ID: return station.getID();
		case __COL_NAME: return station.getName();
		case __COL_ELEVATION: return Double.valueOf(station.getElevation());
		case __COL_LATITUDE: return Double.valueOf(station.getLatitude());
		case __COL_REGION1: return station.getRegion1();
		case __COL_REGION2: return station.getRegion2();
		case __COL_ZH:
			if ( StateCU_Util.isMissing(station.getZh()) ) {
				return null;
			}
			else {
				return Double.valueOf(station.getZh());
			}
		case __COL_ZM:
			if ( StateCU_Util.isMissing(station.getZm()) ) {
				return null;
			}
			else {
				return Double.valueOf(station.getZm());
			}
	}	

	return "";
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
	widths[__COL_ID] = 6;
	widths[__COL_NAME] = 24;
	widths[__COL_ELEVATION] = 7;
	widths[__COL_LATITUDE] = 7;
	widths[__COL_REGION1] = 9;
	widths[__COL_REGION2] = 6;
	widths[__COL_ZH] = 15;
	widths[__COL_ZM] = 15;
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

	return true;
}

/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col) {
	StateCU_ClimateStation station = (StateCU_ClimateStation)_data.get(row);
	switch (col) {
		case __COL_ID:			
			station.setID((String)value);
			break;
		case __COL_NAME: 	
			station.setName((String)value);
			break;
		case __COL_ELEVATION:
			station.setElevation(((Double)value).doubleValue());
			break;
		case __COL_LATITUDE:
			station.setLatitude(((Double)value).doubleValue());
			break;
		case __COL_REGION1:
			station.setRegion1((String)value);
			break;
		case __COL_REGION2:
			station.setRegion2((String)value);
			break;
		case __COL_ZH:
			station.setZh(((Double)value).doubleValue());
			break;
		case __COL_ZM:
			station.setZm(((Double)value).doubleValue());
			break;
	}	

	super.setValueAt(value, row, col);	
}	

/**
Sets the parent well under which the right and return flow data is stored.
@param parent the parent well.
*/
public void setParentClimateStation(StateCU_ClimateStation parent) {
	// TODO SAM 2007-03-01 Evaluate use
	//__parentStation = parent;
}

}
