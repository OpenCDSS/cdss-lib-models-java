// StateCU_Location_TableModel - Table model for displaying data for location tables

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// ----------------------------------------------------------------------------
// StateCU_Location_TableModel - Table model for displaying data for 
//	location tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-14	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-22	JTS, RTi		Revised following SAM's review.
// 2004-02-28	Steven A. Malers, RTi	Moved some utility methods from
//					StateCU_Data to StateCU_Util.
// 2005-01-21	JTS, RTi		Added the editable flag.
// 2005-01-24	JTS, RTi		Added the ability to display multiple
//					locations in a single table model.
// 2005-03-28	JTS, RTi		Adjusted column sizes.
// 2005-03-29	JTS, RTi		* Removed the Collection Division 
//					  column.
//					* Adjusted the column order.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.List;

import DWR.StateMod.StateMod_DiversionRight;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class is a table model for displaying location data.
*/
@SuppressWarnings("serial")
public class StateCU_Location_TableModel 
extends JWorksheet_AbstractRowTableModel<StateCU_Location> implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 14;

/**
Column references.
*/
private final int 
	__COL_ID = 		0,
	__COL_NAME = 		1,
	__COL_LATITUDE = 	2,
	__COL_ELEVATION = 	3,
	__COL_REGION1 = 	4,
	__COL_REGION2 = 	5,
	__COL_NUM_STA = 	6,
	__COL_AWC = 		7;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
Whether a single location is being shown in the table model (true) or many
locations are being shown (false).
*/
private boolean __singleLocation = true;

/**
The parent location for which subdata is displayed.
*/
private StateCU_Location __parentLocation;

private StateCU_DelayTableAssignment __delays;

private List<StateCU_ClimateStation> __stations;

private List<StateMod_DiversionRight> __rights;

/**
Constructor.  This builds the Model for displaying location data
@param data the data that will be displayed in the table.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateCU_Location_TableModel(List<StateCU_Location> data)
throws Exception {
	this(data, true, true);
}

/**
Constructor.  This builds the Model for displaying location data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
@param singleLocation whether a single location (true) or many locations (false)
are being shown in the table model.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateCU_Location_TableModel(List<StateCU_Location> data, boolean editable, boolean singleLocation)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data list passed to StateCU_Location_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;
	__editable = editable;
	__singleLocation = singleLocation;

	if (__singleLocation) {	
		__COLUMNS = 14;
	}
	else {
		__COLUMNS = 8;
	}
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	if (__singleLocation) {
		switch (columnIndex) {
			case  0: 	return Integer.class;	// row #
			case  1:	return String.class;	// id
			case  2:	return String.class;	// name
			case  3:	return Double.class;	// pct return
			case  4:	return String.class;	// pattern no
			case  5:	return String.class;	// id
			case  6:	return String.class;	// name
			case  7:	return Double.class;	// precip wt
			case  8:	return Double.class;	// temp wt
			case  9:	return String.class;	// id
			case 10:	return String.class;	// name
			case 11:	return Double.class;	// irtem
			case 12:	return Double.class;	// dcrdiv
			case 13:	return Integer.class;	// switch
		}
	}
	else {
		switch (columnIndex) {
			case __COL_ID:		return String.class;
			case __COL_NAME:	return String.class;
			case __COL_ELEVATION:	return Double.class;
			case __COL_LATITUDE:	return Double.class;
			case __COL_REGION1:	return String.class;
			case __COL_REGION2:	return String.class;	
			case __COL_NUM_STA:	return Integer.class;
			case __COL_AWC:		return Double.class;
		}
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
	if (__singleLocation) {
		switch (columnIndex) {
			case  0:	return " ";
			case  1:	return "ID";
			case  2:	return "NAME";
			case  3: 	return "% RETURN";
			case  4:	return "DELAY TABLE ID";
			case  5:	return "STATION ID";
			case  6:	return "STATION NAME";
			case  7:	return "PRECIP WT";
			case  8:	return "TEMP WT";
			case  9:	return "RIGHT ID";
			case 10:	return "RIGHT NAME";
			case 11:	return "IRTEM";
			case 12:	return "DCRDIV";
			case 13:	return "SWITCH";
		}
	}
	else {
		switch (columnIndex) {
			case __COL_ID:		return "\n\n\n\nID";
			case __COL_NAME:	return "\n\n\n\nNAME";
			case __COL_ELEVATION:	return "\n\n\nELEVATION\n(FT)";
			case __COL_LATITUDE:	
				return "\n\n\nLATITUDE\n(DEC. DEG.)";
			case __COL_REGION1:	return "\n\n\n\nREGION1";
			case __COL_REGION2:	return "\n\n\n\nREGION2";
			case __COL_NUM_STA:	return 
				"\n\nNUMBER OF\nCLIMATE\nSTATIONS";
			case __COL_AWC:		
				return "AVAILABLE\nWATER\nCONTENT\nAWC,"
					+ "\n(FRACTION)";
		}
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
	if (__singleLocation) {
		switch (column) {
			case  0:	return "%8d";	// row #
			case  1:	return "%-20.20s";	// id
			case  2:	return "%-20.20s";	// name
			case  3:	return "%8.2f";	// pct return
			case  4:	return "%-20.20s";	// pattern no
			case  5:	return "%-20.20s";	// station id
			case  6:	return "%-20.20s";	// station name
			case  7:	return "%8.2f";	// precip wt
			case  8:	return "%8.2f";	// temp wt
			case  9:	return "%-20.20s";	// id
			case 10:	return "%-20.20s";	// name
			case 11:	return "%12.6f";// irtem
			case 12:	return "%8.1f";	// dcrdiv
			case 13:	return "%8d";	// switch
			default:	return "%-8s";
		}
	}
	else {
		switch (column) {
			case __COL_ID:		return "%-20.20s";
			case __COL_NAME:	return "%-20.20s";
			case __COL_ELEVATION:	return "%8.2f";
			case __COL_LATITUDE:	return "%8.2f";
			case __COL_REGION1:	return "%-20.20s";
			case __COL_REGION2:	return "%-20.20s";
			case __COL_NUM_STA:	return "%8d";
			case __COL_AWC:		return "%8.4f";
		}
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
public Validator[] getValidators( int col ) {
	Validator[] no_checks = new Validator[] {};

	if (__singleLocation) {
		switch (col) {
			case 1:		return ids;
			case 2: 	return blank;	
			//case 3:		return nums;
			//case 4:		return ids;
			case 5: 	return ids;
			case 6:		return blank;
			case 7:		return nums;
			case 8:		return nums;		
			case 13:	return blank;
			default:	return no_checks;
		}
	}
	else {	
		switch (col) {
			case __COL_ID:			return ids;
			case __COL_NAME:	 	return blank;
			case __COL_ELEVATION:	return nums;
			case __COL_LATITUDE:	return nums;
			case __COL_REGION1:		return blank;
			case __COL_REGION2:		return blank;
			case __COL_NUM_STA:		return nums;
			case __COL_AWC:			return nums;
			default: 				return no_checks;
		}
	}
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	if (__singleLocation) {
		switch (col) {
			case 1:
			case 2:
				StateCU_Location location = (StateCU_Location)_data.get(row);
				switch (col) {
					case 1:	return location.getID();
					case 2: return location.getName();
				}	
			case 3:	
				return new Double(__delays.getDelayTablePercent(row));
			case 4:	
				return new String(__delays.getDelayTableID(	row));
			case 5: 
				return __parentLocation.getClimateStationID(row);
			case 6:	
				int index = StateCU_Util.indexOf(__stations, __parentLocation.getClimateStationID(row));
				if (index == -1) {
					return "N/A";
				}
				return (__stations.get(row)).getName();
			case 7:	
				return new Double(__parentLocation.getPrecipitationStationWeight(	row));
			case 8:	
				return new Double(__parentLocation.getTemperatureStationWeight(row));	
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
				StateMod_DiversionRight right = __rights.get(row);
				switch (col) {
					case 9:	return right.getID();
					case 10:return right.getName();
					case 11:return new Double(
						right.getIrtem());
					case 12:return new Double(
						right.getDcrdiv());
					case 13:return new Integer(
						right.getSwitch());
				}
			default:	return "";
		}
	}
	else {
		StateCU_Location location = (StateCU_Location)_data.get(row);	
		switch (col) {
			case __COL_ID:		
				return location.getID();
			case __COL_NAME:	
				return location.getName();
			case __COL_ELEVATION:	
				return new Double(location.getElevation());
			case __COL_LATITUDE:	
				return new Double(location.getLatitude());
			case __COL_REGION1:	
				return location.getRegion1();
			case __COL_REGION2:	
				return location.getRegion2();
			case __COL_NUM_STA:
				return new Integer(
					location.getNumClimateStations());
			case __COL_AWC:		
				return new Double(location.getAwc());
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
	int i = 0;
	if (__singleLocation) {
		widths[i++] = 5;	// row #
		widths[i++] = 12;	// id
		widths[i++] = 20;	// name
		widths[i++] = 12;	// % return
		widths[i++] = 20;	// pattern id
		widths[i++] = 12;	// id
		widths[i++] = 20;	// name
		widths[i++] = 12;	// precip wt
		widths[i++] = 12;	// temp wt
		widths[i++] = 15;	// right id
		widths[i++] = 24;	// right name
		widths[i++] = 13;	// irtem
		widths[i++] = 8;	// dcrdiv
		widths[i++] = 8;	// switch
	}
	else {
		widths[__COL_ID] =		5;
		widths[__COL_NAME] =		20;
		widths[__COL_ELEVATION] =	8;
		widths[__COL_LATITUDE] =	7;
		widths[__COL_REGION1] =		10;
		widths[__COL_REGION2] =		6;
		widths[__COL_NUM_STA] = 	8;
		widths[__COL_AWC] =		8;
	}
	return widths;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}

	if (columnIndex > 2) {
		return false;
	}
	return false;
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

public void setDelay(StateCU_Location location, StateCU_DelayTableAssignment dta) {
	__parentLocation = location;
	__delays = dta;
	if (dta == null) {
		_rows = 0;
	}
	else {
		_rows = dta.getNumDelayTables();
	}
	fireTableDataChanged();
}

public void setStations(StateCU_Location location, List<StateCU_ClimateStation> stations) {
	__parentLocation = location;
	__stations = stations;

	_rows = location.getNumClimateStations();
	fireTableDataChanged();
}

public void setRights(StateCU_Location location, List<StateMod_DiversionRight> rights) {
	__parentLocation = location;
	__rights = rights;

	_rows = __rights.size();
	fireTableDataChanged();
}

}
