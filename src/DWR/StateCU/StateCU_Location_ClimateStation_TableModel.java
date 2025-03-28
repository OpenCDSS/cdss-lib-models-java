// StateCU_Location_ClimateStation_TableModel - Table model for displaying location climate station data.

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
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class is a table model for displaying location climate station data.
*/
@SuppressWarnings("serial")
public class StateCU_Location_ClimateStation_TableModel 
extends JWorksheet_AbstractRowTableModel<StateCU_Location> implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 6;

/**
Column references.
*/
private final int 
	__COL_ID = 0,
	__COL_STA_ID = 1,
	__COL_TEMP_WT = 2,
	__COL_PRECIP_WT = 3,
	__COL_ORO_TEMP_ADJ = 4,
	__COL_ORO_PRECIP_ADJ = 5;

/**
Whether the data are editable or not.
*/
private boolean __editable = false;

/**
The parent location for which subdata is displayed.
*/
// TODO SAM 2007-03-01 Evaluate use
//private StateCU_Location __parentLocation;

/**
The data displayed in the table.
*/
private List<Object>[] __data = null;

/**
Constructor.  This builds the Model for displaying location data
@param data the data that will be displayed in the table.
*/
public StateCU_Location_ClimateStation_TableModel(List<StateCU_Location> data) {
	this(data, false);
}

/**
Constructor.  This builds the Model for displaying location data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
*/
public StateCU_Location_ClimateStation_TableModel(List<StateCU_Location> data, boolean editable) {
	if (data == null) {
		data = new Vector<StateCU_Location>();
	}
	_data = data;
	__editable = editable;

	setupData();
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_ID:		return String.class;
		case __COL_STA_ID:	return String.class;
		case __COL_TEMP_WT:	return Double.class;
		case __COL_PRECIP_WT:	return Double.class;
		case __COL_ORO_TEMP_ADJ:	return Double.class;
		case __COL_ORO_PRECIP_ADJ:	return Double.class;
	}
	return String.class;
}

/**
From AbstractTableMode.  Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
From AbstractTableMode.  Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case __COL_ID:  return "\nCU\nLOCATION\nID";
		case __COL_STA_ID:  return "\nCLIMATE\nSTATION\nID";
		case __COL_TEMP_WT:  return "TEMPERATURE\nSTATION\nWEIGHT\n(FRACTION)";
		case __COL_PRECIP_WT:  return "PRECIPITATION\nSTATION\nWEIGHT\n(FRACTION)";
		case __COL_ORO_TEMP_ADJ:  return "OROGRAPHIC\nTEMPERATURE\nADJUSTMENT\n(DEGF/1000 FT)";
		case __COL_ORO_PRECIP_ADJ:  return "OROGRAPHIC\nPRECIPITATION\nADJUSTMENT\n(FRACTION)";
	}

	return " ";
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
		case __COL_ID:  return "%-20.20s";
		case __COL_STA_ID:  return "%-20.20s";
		case __COL_TEMP_WT:  return "%8.2f";
		case __COL_PRECIP_WT:  return "%8.2f";
		case __COL_ORO_TEMP_ADJ:  return "%8.2f";
		case __COL_ORO_PRECIP_ADJ:  return "%8.2f";
	}
	return "%-8s";	
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators(int col) {
	Validator[] no_checks = new Validator[] {};
	
	// TODO KAT 2007-04-12 Add checks here ...
	return no_checks;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	return __data[col].get(row);
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
	widths[__COL_ID] = 7;
	widths[__COL_STA_ID] = 6;
	widths[__COL_TEMP_WT] = 10;
	widths[__COL_PRECIP_WT] = 10;
	widths[__COL_ORO_TEMP_ADJ] = 10;
	widths[__COL_ORO_PRECIP_ADJ] = 10;
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

	return false;
}

/**
Sets up the data Vectors to display the location climate station data in the GUI.
*/
@SuppressWarnings("unchecked")
private void setupData() {
	int num = 0;
	int size = _data.size();
	StateCU_Location l = null;
	String id = null;
	__data = new List[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector<Object>();
	}

	int rows = 0;
	
	for (int i = 0; i < size; i++) {
		l = _data.get(i);
		id = l.getID();
		num = l.getNumClimateStations();

		for (int j = 0; j < num; j++ ) {
			__data[__COL_ID].add(id);
			__data[__COL_STA_ID].add(l.getClimateStationID(j));
			__data[__COL_TEMP_WT].add(Double.valueOf(l.getTemperatureStationWeight(j)));
			__data[__COL_PRECIP_WT].add(Double.valueOf(l.getPrecipitationStationWeight(j)));
			__data[__COL_ORO_TEMP_ADJ].add(Double.valueOf(l.getOrographicTemperatureAdjustment(j)));
			__data[__COL_ORO_PRECIP_ADJ].add(Double.valueOf(l.getOrographicPrecipitationAdjustment(j)));
			rows++;
		}
	}
	_rows = rows;
}

/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col) {

	switch (col) {
	}	

	super.setValueAt(value, row, col);	
}	

}
