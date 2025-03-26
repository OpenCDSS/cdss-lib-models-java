// StateMod_WellRight_TableModel - Table model for displaying data for well right tables

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
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

// TODO SAM 2016-06-09 why does this seem redundant with StateMod_WellRight_Data_TableModel

/**
This table model displays well right data.  The model can display rights data
for a single well or for 1+ wells.  The difference is specified in the
constructor and affects how many columns of data are shown.
*/
@SuppressWarnings("serial")
public class StateMod_WellRight_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_WellRight> {

/**
Number of columns in the table model.  For table models that display rights for
a single well, the worksheets only have have 26 columns.  Table models that 
display rights for 1+ wells have 27.  The variable is modified in the constructor.
*/
private int __COLUMNS = 26;

/**
References to columns.
*/
public final static int
	COL_WELL_ID = -1,
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
	COL_X_PERMIT_RECEIPT = 16,
	COL_X_PERMIT_DATE = 17,
	COL_X_PERMIT_ADMIN_NUMBER = 18,
	COL_X_WELL_YIELD_GPM = 19,
	COL_X_WELL_YIELD_CFS = 20,
	COL_X_APEX_GPM = 21,
	COL_X_APEX_CFS = 22,
	COL_X_WELL_FRACTION = 23,
	COL_X_DITCH_FRACTION = 24,
	COL_X_YIELD_PRORATED_GPM = 25;
	
/**
Whether the table data is editable or not.
*/
private boolean __editable = false;

/**
Whether the table model should be set up for displaying the rights for only
a single well (true) or for multiple wells (false).  If true, then
the well ID field will not be displayed.  If false, then it will be.
*/
private boolean __singleWell = true;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
@param singleWell if true, then the table model is set up to only display
a single well's right data.  This means that the well ID field will
not be shown.  If false then the well right field will be included.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_WellRight_TableModel(List<StateMod_WellRight> data, boolean editable, boolean singleWell)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data list passed to StateMod_WellRight_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__editable = editable;
	__singleWell = singleWell;

	if (!__singleWell) {
		// This is done because if rights for 1+ wells are shown in 
		// the worksheet the ID for the associated wells needs shown
		// as well.  So instead of just 6 columns of data, an 
		// additional one is necessary.
		__COLUMNS++;
	}
}

/**
Returns the class of the data stored in a given column.
@param col the column for which to return the data class.
@return the class of the data stored in a given column.
*/
public Class<?> getColumnClass (int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

	switch (col) {
		case COL_WELL_ID: return String.class;
		case COL_RIGHT_ID: return String.class;
		case COL_RIGHT_NAME: return String.class;
		case COL_STRUCT_ID: return String.class;
		case COL_ADMIN_NUM: return String.class;
		case COL_DCR_AMT: return Double.class;
		case COL_ON_OFF: return Integer.class;
		case COL_PARCEL_YEAR: return Integer.class;
		case COL_PARCEL_CLASS: return Integer.class;
		case COL_PARCEL_ID: return Integer.class;
		case COL_COLLECTION_TYPE: return String.class;
		case COL_COLLECTION_PART_TYPE: return String.class;
		case COL_COLLECTION_PART_ID: return String.class;
		case COL_COLLECTION_PART_ID_TYPE: return String.class;
		case COL_X_RIGHT_WDID: return String.class;
		case COL_X_RIGHT_APPROPRIATION_DATE: return String.class;
		case COL_X_RIGHT_ADMIN_NUMBER: return String.class;
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
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

	switch (col) {
		case COL_WELL_ID: return "\n\nWELL ID";
		case COL_RIGHT_ID: return "\n\nRIGHT ID";
		case COL_RIGHT_NAME: return "\n\nWELL RIGHT NAME";
		case COL_STRUCT_ID: return "WELL ID\nASSOCIATED\nW/ RIGHT";
		case COL_ADMIN_NUM: return "\n\nADMIN NUMBER";
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

	// the offset is used because in worksheets that have rights data for 1+
	// wells the first column is numbered -1.  The offset calculation 
	// allows this column number, which is normally invalid for a worksheet,
	// to be used only for those worksheets that need to display the -1st column.
	int offset = 0;
	if (!__singleWell) {	
		offset = 1;
		tips[COL_WELL_ID + offset] = "The ID of the well to which the rights belong.";
	}

	tips[COL_RIGHT_ID + offset] = "The well right ID is typically the well station ID<br> followed by .01, .02, etc.";
	tips[COL_RIGHT_NAME + offset] = "Well right name";
	tips[COL_STRUCT_ID + offset] = "The well ID is the link between well stations and their right(s).";
	tips[COL_ADMIN_NUM + offset] = "Lower admininistration numbers indicate greater seniority.  99999 is typical for a very junior right.";
	tips[COL_DCR_AMT + offset] = "Decreed amount (CFS)";
	tips[COL_ON_OFF + offset] = "0 = OFF, 1 = ON, YYYY indicates to turn on the right in year YYYY, -YYYY indicates to turn off the right in year YYYY.";
	tips[COL_PARCEL_YEAR + offset] = "For irrigated parcels, year of irrigated lands assessment";
	tips[COL_PARCEL_CLASS + offset] = "For irrigated parcels, indicates how well was matched to parcel";
	tips[COL_PARCEL_ID + offset] = "For irrigated parcels, parcel identifier";
	tips[COL_COLLECTION_TYPE + offset] = "Well station collection type when processing the data (blank if not a collection)";
	tips[COL_COLLECTION_PART_TYPE + offset] = "Well station collection part type (Ditch, Well, Parcel)";
	tips[COL_COLLECTION_PART_ID + offset] = "Collection part identifier";
	tips[COL_COLLECTION_PART_ID_TYPE + offset] = "Collection part identifier type";
	tips[COL_X_RIGHT_WDID + offset] = "For parcel/well cross-reference, the matching well structure WDID";
	tips[COL_X_RIGHT_APPROPRIATION_DATE + offset] = "For parcel/well cross-reference, the right appropriation date";
	tips[COL_X_RIGHT_ADMIN_NUMBER + offset] = "For parcel/well cross-reference, the administration number for the right";
	tips[COL_X_PERMIT_RECEIPT + offset] = "For parcel/well cross-reference, the well permit receipt";
	tips[COL_X_PERMIT_DATE + offset] = "For parcel/well cross-reference, the permit date";
	tips[COL_X_PERMIT_ADMIN_NUMBER + offset] = "For parcel/well cross-reference, the permit date administration number";
	tips[COL_X_WELL_YIELD_GPM + offset] = "For parcel/well cross-reference, the well yield (GPM)";
	tips[COL_X_WELL_YIELD_CFS + offset] = "For parcel/well cross-reference, the well yield (CFS)";
	tips[COL_X_APEX_GPM + offset] = "For parcel/well cross-reference, the well right alternate point/exchange (GPM)";
	tips[COL_X_APEX_CFS + offset] = "For parcel/well cross-reference, the well right alternate point/exchange (CFS)";
	tips[COL_X_WELL_FRACTION + offset] = "Fraction of full yield for this relationship (due to multiple parcels served by well)";
	tips[COL_X_DITCH_FRACTION + offset] = "Fraction of full yeild for this relationship (due to proration of parcel served by multiple ditches)";
	tips[COL_X_YIELD_PRORATED_GPM + offset] = "Well yield prorated by well and ditch fraction (GPM)";
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

	// the offset is used because in worksheets that have rights data for 1+
	// wells the first column is numbered -1.  The offset calculation 
	// allows this column number, which is normally invalid for a worksheet,
	// to be used only for those worksheets that need to display the -1st column.
	int offset = 0;
	if (!__singleWell) {
		offset = 1;
		widths[COL_WELL_ID + offset] = 8;
	}
	
	widths[COL_RIGHT_ID + offset] = 10;
	widths[COL_RIGHT_NAME + offset] = 20;
	widths[COL_STRUCT_ID + offset] = 10;
	widths[COL_ADMIN_NUM + offset] = 11;
	widths[COL_DCR_AMT + offset] = 10;
	widths[COL_ON_OFF + offset] = 7;
	widths[COL_PARCEL_YEAR + offset] = 5;
	widths[COL_PARCEL_CLASS + offset] = 5;
	widths[COL_PARCEL_ID + offset] = 7;
	widths[COL_COLLECTION_TYPE + offset] = 9;
	widths[COL_COLLECTION_PART_TYPE + offset] = 9;
	widths[COL_COLLECTION_PART_ID + offset] = 10;
	widths[COL_COLLECTION_PART_ID_TYPE + offset] = 9;
	widths[COL_X_RIGHT_WDID + offset] = 7;
	widths[COL_X_RIGHT_APPROPRIATION_DATE + offset] = 11;
	widths[COL_X_RIGHT_ADMIN_NUMBER + offset] = 11;
	widths[COL_X_PERMIT_RECEIPT + offset] = 10;
	widths[COL_X_PERMIT_DATE + offset] = 9;
	widths[COL_X_PERMIT_ADMIN_NUMBER + offset] = 11;
	widths[COL_X_WELL_YIELD_GPM + offset] = 7;
	widths[COL_X_WELL_YIELD_CFS + offset] = 7;
	widths[COL_X_APEX_GPM + offset] = 7;
	widths[COL_X_APEX_CFS + offset] = 7;
	widths[COL_X_WELL_FRACTION + offset] = 7;
	widths[COL_X_DITCH_FRACTION + offset] = 7;
	widths[COL_X_YIELD_PRORATED_GPM + offset] = 9;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param col column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

	switch (col) {
		case COL_WELL_ID: return "%-12s";
		case COL_RIGHT_ID: return "%-12s";
		case COL_RIGHT_NAME: return "%-24s";
		case COL_STRUCT_ID: return "%-40s";
		case COL_ADMIN_NUM: return "%-40s";
		case COL_DCR_AMT: return "%12.2f";
		case COL_ON_OFF: return "%8d";
		case COL_PARCEL_YEAR: return "%8d";
		case COL_PARCEL_CLASS: return "%8d";
		case COL_PARCEL_ID: return "%8d";
		case COL_COLLECTION_TYPE: return "%-12s";
		case COL_COLLECTION_PART_TYPE: return "%-12s";
		case COL_COLLECTION_PART_ID: return "%-20s";
		case COL_COLLECTION_PART_ID_TYPE: return "%-12s";
		case COL_X_RIGHT_WDID: return "%-12s";
		case COL_X_RIGHT_APPROPRIATION_DATE: return "%-10s";
		case COL_X_RIGHT_ADMIN_NUMBER: return "%-12s";
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
		default: return "%-s";
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

	StateMod_WellRight wellr = (StateMod_WellRight)_data.get(row);

	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}
	
	switch (col) {
		case COL_WELL_ID: return wellr.getCgoto();
		case COL_RIGHT_ID: return wellr.getID();
		case COL_RIGHT_NAME: return wellr.getName();
		case COL_STRUCT_ID: return wellr.getCgoto();
		case COL_ADMIN_NUM: return wellr.getIrtem();
		case COL_DCR_AMT: return Double.valueOf(wellr.getDcrdivw());
		case COL_ON_OFF: return Integer.valueOf(wellr.getSwitch());
		case COL_PARCEL_YEAR: return Integer.valueOf(wellr.getParcelYear());
		case COL_PARCEL_CLASS: return Integer.valueOf(wellr.getParcelMatchClass());
		case COL_PARCEL_ID: return Integer.valueOf(wellr.getParcelID());
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
		case COL_X_YIELD_PRORATED_GPM: return Double.valueOf(wellr.getDcrdivw()/.002228);
		default: return "";
	}
}

/**
Returns whether the cell is editable or not.  If the table is editable, only
the well ID is not editable - this reflects the display being opened from the
well station window and maintaining agreement between the two windows.
@param rowIndex unused.
@param col the index of the column to check whether it is editable or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int col) {
	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}

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

	StateMod_WellRight wellr = _data.get(row);

	// necessary for table models that display rights for 1+ wells so that
	// the -1st column (ID) can also be displayed.  By doing it this way,
	// code can be shared between the two kinds of table models and less
	// maintenance is necessary.
	if (!__singleWell) {
		col--;
	}
	
	switch (col) {
		case COL_WELL_ID:
			wellr.setCgoto((String)value);
			break;
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
				try {	dval = Double.valueOf( (String)value).doubleValue();
				}
				catch (Exception e) {
					Message.printWarning(2, "setValue", e);
					return;
				}
			}
			else {	dval = ((Double)value).doubleValue();
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
				ival = Integer.valueOf( onOff.substring(0, index)).intValue();
				wellr.setSwitch(ival);
			}
			break;				
	}	

	if (!__singleWell) {
		col++;
	}

	super.setValueAt(value, row, col);	
}	

}
