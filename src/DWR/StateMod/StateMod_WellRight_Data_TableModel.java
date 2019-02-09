// StateMod_WellRight_Data_TableModel - Table model for displaying data for well right tables

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
// StateMod_WellRight_Data_TableModel - Table model for displaying data for well
//	right tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-04-04	J. Thomas Sapienza, RTi	Initial version.
// 2007-04-27	Kurt Tometich, RTi		Added getValidators method for check
//									file and data check implementation.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
This table model displays well right data.  The model can display rights data
for a single well or for 1+ wells.  The difference is specified in the
constructor and affects how many columns of data are shown.
*/
public class StateMod_WellRight_Data_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateMod_Data_TableModel
{

/**
Number of columns in the table model.
*/
private int __COLUMNS = 27;

/**
References to columns.
*/
public final static int
	COL_RIGHT_ID = 0,
	COL_RIGHT_NAME = 1,
	COL_STRUCT_ID = 2,
	COL_ADMIN_NUM = 3,
	COL_DCR_AMT = 4,
	COL_ON_OFF = 5,
	COL_PARCEL_YEAR = 6, // These are optional but core to CDSS processing
	COL_PARCEL_CLASS = 7,
	COL_PARCEL_ID = 8,
	COL_COLLECTION_TYPE= 9,
	COL_COLLECTION_PART_TYPE = 10,
	COL_COLLECTION_PART_ID = 11,
	COL_COLLECTION_PART_ID_TYPE = 12,
	COL_X_RIGHT_WDID = 13,
	COL_X_RIGHT_APPROPRIATION_DATE = 14,
	COL_X_RIGHT_ADMIN_NUMBER = 15,
	COL_X_RIGHT_USE = 16,
	COL_X_PERMIT_RECEIPT = 17,
	COL_X_PERMIT_DATE = 18,
	COL_X_PERMIT_ADMIN_NUMBER = 19,
	COL_X_WELL_YIELD_GPM = 20,
	COL_X_WELL_YIELD_CFS = 21,
	COL_X_APEX_GPM = 22,
	COL_X_APEX_CFS = 23,
	COL_X_WELL_FRACTION = 24,
	COL_X_DITCH_FRACTION = 25,
	COL_X_YIELD_PRORATED_GPM = 26;
	
/**
Whether the table data is editable or not.
*/
private boolean __editable = false;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
*/
public StateMod_WellRight_Data_TableModel(List data, boolean editable) {
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
		case COL_RIGHT_ID: return String.class;
		case COL_RIGHT_NAME: return String.class;
		case COL_STRUCT_ID: return String.class;
		case COL_ADMIN_NUM: return String.class;
		case COL_DCR_AMT: return Double.class;
		case COL_ON_OFF: return Integer.class;
		case COL_PARCEL_YEAR: return Integer.class;
		case COL_PARCEL_CLASS: return Integer.class;
		case COL_PARCEL_ID: return String.class;
		case COL_COLLECTION_TYPE: return String.class;
		case COL_COLLECTION_PART_TYPE: return String.class;
		case COL_COLLECTION_PART_ID: return String.class;
		case COL_COLLECTION_PART_ID_TYPE: return String.class;
		case COL_X_RIGHT_WDID: return String.class;
		case COL_X_RIGHT_APPROPRIATION_DATE: return String.class;
		case COL_X_RIGHT_ADMIN_NUMBER: return String.class;
		case COL_X_RIGHT_USE: return String.class;
		case COL_X_PERMIT_RECEIPT: return String.class;
		case COL_X_PERMIT_DATE: return String.class;
		case COL_X_PERMIT_ADMIN_NUMBER: return String.class;
		case COL_X_WELL_YIELD_GPM: return Double.class;
		case COL_X_WELL_YIELD_CFS: return Double.class;
		case COL_X_APEX_GPM: return Double.class;
		case COL_X_APEX_CFS: return Double.class;
		case COL_X_WELL_FRACTION: return Double.class;
		case COL_X_DITCH_FRACTION: return Double.class;
		case COL_X_YIELD_PRORATED_GPM: return Double.class;
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
public String getColumnName(int col) {
	switch (col) {
		case COL_RIGHT_ID: return "\n\nRIGHT ID";
		case COL_RIGHT_NAME: return "\n\nWELL RIGHT NAME";
		case COL_STRUCT_ID: return "WELL ID\nASSOCIATED\nW/ RIGHT";
		case COL_ADMIN_NUM: return "\nADMINISTRATION\nNUMBER";
		case COL_DCR_AMT: return "\nDECREED\nAMOUNT (CFS)";
		case COL_ON_OFF: return "\nON/OFF\nSWITCH";
		case COL_PARCEL_YEAR: return "\nPARCEL\nYEAR";
		case COL_PARCEL_CLASS: return "\nPARCEL\nCLASS";
		case COL_PARCEL_ID: return "\nPARCEL\nID";
		case COL_COLLECTION_TYPE: return "\nCOLLECTION\nTYPE";
		case COL_COLLECTION_PART_TYPE: return "COLLECTION\nPART\nTYPE";
		case COL_COLLECTION_PART_ID: return "COLLECTION\nPART\nID";
		case COL_COLLECTION_PART_ID_TYPE: return "COLLECTION\nPART\nID TYPE";
		case COL_X_RIGHT_WDID: return "\nRIGHT\nWDID";
		case COL_X_RIGHT_APPROPRIATION_DATE: return "RIGHT\nAPPROPRIATION\nDATE";
		case COL_X_RIGHT_ADMIN_NUMBER: return "RIGHT\nADMINISTRATION\nNUMBER";
		case COL_X_RIGHT_USE: return "RIGHT\nUSE\nTYPE";
		case COL_X_PERMIT_RECEIPT: return "\nPERMIT\nID";
		case COL_X_PERMIT_DATE: return "\nPERMIT\nDATE";
		case COL_X_PERMIT_ADMIN_NUMBER: return "PERMIT\nADMINISTRATION\nNUMBER";
		case COL_X_WELL_YIELD_GPM: return "WELL\nYIELD\n(GPM)";
		case COL_X_WELL_YIELD_CFS: return "WELL\nYIELD\n(CFS)";
		case COL_X_APEX_GPM: return "WELL APEX\nYIELD\n(GPM)";
		case COL_X_APEX_CFS: return "WELL APEX\nYIELD\n(CFS)";
		case COL_X_WELL_FRACTION: return "\nWELL\nFRACTION";
		case COL_X_DITCH_FRACTION: return "\nDITCH\nFRACTION";
		case COL_X_YIELD_PRORATED_GPM: return "WELL YIELD\nPRORATED\n(GPM)";
		default: return " ";
	}
}


/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_RIGHT_ID] =
		"The well right ID is typically the well station ID followed by .01, .02, etc.";
	tips[COL_RIGHT_NAME] = "Well right name";
	tips[COL_STRUCT_ID] = "The well ID is the link between well stations and their right(s).";
	tips[COL_ADMIN_NUM] = 
		"<HTML>Lower admininistration numbers indicate greater " +
		"seniority.<BR>99999 is typical for a very junior" +
		" right.</html>";
	tips[COL_DCR_AMT] = "Decreed amount (CFS)";
	tips[COL_ON_OFF] = 
		"<HTML>0 = OFF<BR>1 = ON<BR>" +
		"YYYY indicates to turn on the right in year YYYY."+
		"<BR>-YYYY indicates to turn off the right in year" +
		" YYYY.</HTML>";
	tips[COL_PARCEL_YEAR] = "For irrigated parcels, year of irrigated lands assessment";
	tips[COL_PARCEL_CLASS] = "For irrigated parcels, indicates how well was matched to parcel";
	tips[COL_PARCEL_ID] = "For irrigated parcels, parcel identifier";
	tips[COL_COLLECTION_TYPE] = "Well station collection type when processing the data (blank if not a collection)";
	tips[COL_COLLECTION_PART_TYPE] = "Well station collection part type (Ditch, Well, Parcel)";
	tips[COL_COLLECTION_PART_ID] = "Collection part identifier";
	tips[COL_COLLECTION_PART_ID_TYPE] = "Collection part identifier type";
	tips[COL_X_RIGHT_WDID] = "For parcel/well cross-reference, the matching well structure WDID";
	tips[COL_X_RIGHT_APPROPRIATION_DATE] = "For parcel/well cross-reference, the right appropriation date";
	tips[COL_X_RIGHT_ADMIN_NUMBER] = "For parcel/well cross-reference, the administration number for the right";
	tips[COL_X_RIGHT_USE] = "Water right use type";
	tips[COL_X_PERMIT_RECEIPT] = "For parcel/well cross-reference, the well permit receipt";
	tips[COL_X_PERMIT_DATE] = "For parcel/well cross-reference, the permit date";
	tips[COL_X_PERMIT_ADMIN_NUMBER] = "For parcel/well cross-reference, the permit date administration number";
	tips[COL_X_WELL_YIELD_GPM] = "For parcel/well cross-reference, the well yield (GPM)";
	tips[COL_X_WELL_YIELD_CFS] = "For parcel/well cross-reference, the well yield (CFS)";
	tips[COL_X_APEX_GPM] = "For parcel/well cross-reference, the well right alternate point/exchange (GPM)";
	tips[COL_X_APEX_CFS] = "For parcel/well cross-reference, the well right alternate point/exchange (CFS)";
	tips[COL_X_WELL_FRACTION] = "Fraction of full yield for this relationship (due to multiple parcels served by well)";
	tips[COL_X_DITCH_FRACTION] = "Fraction of full yeild for this relationship (due to proration of parcel served by multiple ditches)";
	tips[COL_X_YIELD_PRORATED_GPM] = "Well yield prorated by well and ditch fraction (GPM)";

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

	widths[COL_RIGHT_ID] = 10;
	widths[COL_RIGHT_NAME] = 20;
	widths[COL_STRUCT_ID] = 10;
	widths[COL_ADMIN_NUM] = 11;
	widths[COL_DCR_AMT] = 10;
	widths[COL_ON_OFF] = 7;
	widths[COL_PARCEL_YEAR] = 5;
	widths[COL_PARCEL_CLASS] = 5;
	widths[COL_PARCEL_ID] = 7;
	widths[COL_COLLECTION_TYPE] = 9;
	widths[COL_COLLECTION_PART_TYPE] = 9;
	widths[COL_COLLECTION_PART_ID] = 10;
	widths[COL_COLLECTION_PART_ID_TYPE] = 9;
	widths[COL_X_RIGHT_WDID] = 7;
	widths[COL_X_RIGHT_APPROPRIATION_DATE] = 11;
	widths[COL_X_RIGHT_ADMIN_NUMBER] = 11;
	widths[COL_X_RIGHT_USE] = 30;
	widths[COL_X_PERMIT_RECEIPT] = 10;
	widths[COL_X_PERMIT_DATE] = 9;
	widths[COL_X_PERMIT_ADMIN_NUMBER] = 11;
	widths[COL_X_WELL_YIELD_GPM] = 7;
	widths[COL_X_WELL_YIELD_CFS] = 7;
	widths[COL_X_APEX_GPM] = 7;
	widths[COL_X_APEX_CFS] = 7;
	widths[COL_X_WELL_FRACTION] = 7;
	widths[COL_X_DITCH_FRACTION] = 7;
	widths[COL_X_YIELD_PRORATED_GPM] = 9;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int col) {
	switch (col) {
		case COL_RIGHT_ID: return "%-12.12s";
		case COL_RIGHT_NAME: return "%-24.24s";
		case COL_STRUCT_ID: return "%-12.12s";
		case COL_ADMIN_NUM: return "%-12.12s";
		case COL_DCR_AMT: return "%12.2f";
		case COL_ON_OFF: return "%8d";
		case COL_PARCEL_YEAR: return "%8d";
		case COL_PARCEL_CLASS: return "%8d";
		case COL_PARCEL_ID: return "%-12s";
		case COL_COLLECTION_TYPE: return "%-12s";
		case COL_COLLECTION_PART_TYPE: return "%-12s";
		case COL_COLLECTION_PART_ID: return "%-20s";
		case COL_COLLECTION_PART_ID_TYPE: return "%-12s";
		case COL_X_RIGHT_WDID: return "%-12s";
		case COL_X_RIGHT_APPROPRIATION_DATE: return "%-10s";
		case COL_X_RIGHT_ADMIN_NUMBER: return "%-12s";
		case COL_X_RIGHT_USE: return "%-30s";
		case COL_X_PERMIT_RECEIPT: return "%-12s";
		case COL_X_PERMIT_DATE: return "%-10s";
		case COL_X_PERMIT_ADMIN_NUMBER: return "%-12s";
		case COL_X_WELL_YIELD_GPM: return "%12.2f";
		case COL_X_WELL_YIELD_CFS: return "%12.2f";
		case COL_X_APEX_GPM: return "%12.2f";
		case COL_X_APEX_CFS: return "%12.2f";
		case COL_X_WELL_FRACTION: return "%12.2f";
		case COL_X_DITCH_FRACTION: return "%12.2f";
		case COL_X_YIELD_PRORATED_GPM: return "%12.2f";
		default: return "%-8s";
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
Returns the validators for the given column.
@param col The column to return the validators for.
@return List of validators.
 */
public Validator[] getValidators( int col ) 
{
	Validator[] no_checks = new Validator[] {};
	switch (col) {
		case COL_RIGHT_ID: return ids;
		case COL_RIGHT_NAME: return blank;
		case COL_STRUCT_ID: return ids;
		case COL_ADMIN_NUM: return nums;
		case COL_DCR_AMT: return nums;
		case COL_ON_OFF: return on_off_switch;
		default: return no_checks;
	}
}	

/**
Returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) 
{
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_WellRight wellr = (StateMod_WellRight)_data.get(row);

	switch (col) {
		case COL_RIGHT_ID: return wellr.getID();
		case COL_RIGHT_NAME: return wellr.getName();
		case COL_STRUCT_ID: return wellr.getCgoto();
		case COL_ADMIN_NUM: return wellr.getIrtem();
		case COL_DCR_AMT: return new Double(wellr.getDcrdivw());
		case COL_ON_OFF: return new Integer(wellr.getSwitch());
		case COL_PARCEL_YEAR: return new Integer(wellr.getParcelYear());
		case COL_PARCEL_CLASS: return new Integer(wellr.getParcelMatchClass());
		case COL_PARCEL_ID: return new String(wellr.getParcelID());
		case COL_COLLECTION_TYPE: return wellr.getCollectionType();
		case COL_COLLECTION_PART_TYPE: return wellr.getCollectionPartType();
		case COL_COLLECTION_PART_ID: return wellr.getCollectionPartId();
		case COL_COLLECTION_PART_ID_TYPE: return wellr.getCollectionPartIdType();
		case COL_X_RIGHT_WDID: return wellr.getXWDID();
		case COL_X_RIGHT_APPROPRIATION_DATE:
			if ( wellr.getXApproDate() == null ) {
				return null;
			}
			else {
				DateTime dt = new DateTime(wellr.getXApproDate());
				return dt.toString();
			}
		case COL_X_RIGHT_ADMIN_NUMBER: return wellr.getXApproDateAdminNumber();
		case COL_X_RIGHT_USE: return wellr.getXUse();
		case COL_X_PERMIT_RECEIPT: return wellr.getXPermitReceipt();
		case COL_X_PERMIT_DATE:
			if ( wellr.getXPermitDate() == null ) {
				return null;
			}
			else {
				DateTime dt = new DateTime(wellr.getXPermitDate());
				return dt.toString();
			}
		case COL_X_PERMIT_ADMIN_NUMBER: return wellr.getXPermitDateAdminNumber();
		case COL_X_WELL_YIELD_GPM: return wellr.getXYieldGPM();
		case COL_X_WELL_YIELD_CFS: return wellr.getXYieldGPM()*.002228;
		case COL_X_APEX_GPM: return wellr.getXYieldApexGPM();
		case COL_X_APEX_CFS: return wellr.getXYieldApexGPM()*.002228;
		case COL_X_WELL_FRACTION: return wellr.getXFractionYield();
		case COL_X_DITCH_FRACTION: return wellr.getXDitchFraction();
		case COL_X_YIELD_PRORATED_GPM: return new Double(wellr.getDcrdivw()/.002228);
		default: return "";
	}
}

/**
Returns whether the cell is editable or not.  If the table is editable, only
the well ID is not editable - this reflects the display being opened from the
well station widnow and maintaining agreement between the two windows.
@param rowIndex unused.
@param col the index of the column to check whether it is editable or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int col) {
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

	StateMod_WellRight wellr = (StateMod_WellRight)_data.get(row);

	switch (col) {
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
				try {
					dval = (new Double(	(String)value)).doubleValue();
				}
				catch (Exception e) {
					Message.printWarning(2, "setValue", e);
					return;
				}
			}
			else {
				dval = ((Double)value).doubleValue();
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
				ival = new Integer( onOff.substring(0, index)).intValue();
				wellr.setSwitch(ival);
			}
			break;				
	}	

	super.setValueAt(value, row, col);	
}

}
