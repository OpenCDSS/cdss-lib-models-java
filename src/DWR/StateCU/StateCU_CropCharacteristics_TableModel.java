// ----------------------------------------------------------------------------
// StateCU_CropCharacteristics_TableModel - Table model for displaying data for 
//	crop char tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-14	J. Thomas Sapienza, RTi	Initial version.
// 2005-01-21	JTS, RTi		Added the editable flag.
// 2005-01-24	JTS, RTi		* Removed the row count column because
//					  worksheets now handle that.
//					* Added column reference variables.
// 2005-03-28	JTS, RTi		* Adjusted column sizes.
//					* Removed the ID column.
//					* Added tool tips.
// 2007-01-10   Kurt Tometich, RTi
// 								Fixed the format for the cropName to 
//								30 chars instead of 20.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;

/**
This class is a table model for displaying crop char data.
*/
public class StateCU_CropCharacteristics_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateCU_Data_TableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 3;

/**
Column references.
*/
private final int
	__COL_NAME = 	0,
	__COL_DAY_PCT = 1,
	__COL_VALUE = 	2;

/**
Column references.
*/
private final int
	__COL_GDATE1 =		1,
	__COL_GDATE2 = 		2,
	__COL_GDATE3 = 		3,
	__COL_GDATE4 = 		4,
	__COL_GDATE5 = 		5,
	__COL_GDATES = 		6,
	__COL_TMOIS1 = 		7,
	__COL_TMOIS2 = 		8,
	__COL_MAD = 		9,
	__COL_IRX = 		10,
	__COL_FRX = 		11,
	__COL_AWC = 		12,
	__COL_APD = 		13,
	__COL_TFLAG1 = 		14,
	__COL_TFLAG2 = 		15,
	__COL_CUT2 = 		16,
	__COL_CUT3 = 		17;


private boolean __dayNotPercent = true;

/**
Whether the data are editable or not.
*/
private boolean __editable = true;

/**
Whether the table model is set up to show a single crop or many crops.
*/
private boolean __singleCrop = true;

/**
The parent crop for which subdata is displayed.
*/
// TODO 2007-03-01 Evaluate use
//private StateCU_CropCharacteristics __parentCrop;

private StateCU_BlaneyCriddle __blaneyCriddle;


/**
Constructor.  This builds the Model for displaying crop char data
@param data the data that will be displayed in the table.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateCU_CropCharacteristics_TableModel( Vector data )
throws Exception {
	this(data, true, true);
}

/**
Constructor.  This builds the Model for displaying crop char data
@param data the data that will be displayed in the table.
@param editable whether the data are editable or not.
@param singleCrop whether a single crop's characteristics are shown (true) or
the characteristics for many crops are shown.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateCU_CropCharacteristics_TableModel(Vector data, boolean editable,
boolean singleCrop)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data Vector passed to " 
			+ "StateCU_CropCharacteristics_TableModel "
			+ "constructor.");
	}
	_rows = data.size();
	_data = data;
	__editable = editable;
	__singleCrop = singleCrop;
	
	if (singleCrop) {
		__COLUMNS = 4;
	}
	else {
		__COLUMNS = 19;
	}
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	if (__singleCrop) {
		switch (columnIndex) {
			case __COL_NAME:	return String.class;
			case __COL_DAY_PCT:	return Integer.class;
			case __COL_VALUE:	return Double.class;
		}
	}
	else {
		switch (columnIndex) {
			case __COL_NAME:	return String.class;
			case __COL_GDATE1:	return Integer.class;
			case __COL_GDATE2:	return Integer.class;
			case __COL_GDATE3:	return Integer.class;
			case __COL_GDATE4:	return Integer.class;
			case __COL_GDATE5:	return Integer.class;
			case __COL_GDATES:	return Integer.class;
			case __COL_TMOIS1:	return Double.class;
			case __COL_TMOIS2:	return Double.class;
			case __COL_MAD:		return Double.class;
			case __COL_IRX:		return Double.class;
			case __COL_FRX:		return Double.class;
			case __COL_AWC:		return Double.class;
			case __COL_APD:		return Double.class;
			case __COL_TFLAG1:	return Integer.class;
			case __COL_TFLAG2:	return Integer.class;
			case __COL_CUT2:	return Integer.class;
			case __COL_CUT3:	return Integer.class;
		}
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
	if (__singleCrop) {
		switch (columnIndex) {
			case __COL_NAME:	return "\nNAME";
			case __COL_DAY_PCT:	return "\nDAY/PCT";
			case __COL_VALUE:	return "CROP\nCOEFFICIENT";
		}
	}
	else {
		switch (columnIndex) {
			case __COL_NAME:	return "\n\n\nNAME";
			case __COL_GDATE1:	return "\n\nPLANTING\nMONTH";
			case __COL_GDATE2:	return "\n\nPLANTING\nDAY";
			case __COL_GDATE3:	return "\n\nHARVEST\nMONTH";
			case __COL_GDATE4:	return "\n\nHARVEST\nDAY";
			case __COL_GDATE5:	return 
				"\n\nDAYS TO\nFULL COVER";
			case __COL_GDATES:	return "\n\nSEASON\nLENGTH";
			case __COL_TMOIS1:	
				return "\n\nTEMP EARLY\nMOISTURE (F)";
			case __COL_TMOIS2:	
				return "\n\nTEMP LATE\nMOISTURE (F)";
			case __COL_MAD:		
				return "\nMANAGEMENT\nALLOWABLE\nDEFICIT LEVEL";
			case __COL_IRX:		
				return "\nINITIAL ROOT\nZONE DEPTH\n(IN)";
			case __COL_FRX:		
				return "\nMAXIMUM ROOT\nZONE DEPTH\n(IN)";
			case __COL_AWC:		
				return "AVAILABLE\nWATER HOLDING\nCAPACITY"
					+ "\nAWC (IN)";
			case __COL_APD:		
				return "\nMAXIMUM\nAPPLICATION\nDEPTH (IN)";
			case __COL_TFLAG1:	
				return "\nSPRING\nFROST\nFLAG";
			case __COL_TFLAG2:	
				return "\nFALL\nFROST\nFLAG";
			case __COL_CUT2:	
				return "\nDAYS BETWEEN\n1ST AND 2ND\nCUT";
			case __COL_CUT3:	
				return "\nDAYS BETWEEN\n2ND AND 3RD\nCUT";
		}
	}
	return "";
}

/**
Returns the tool tips for the columns.
@return the tool tips for the columns.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		tips[i] = null;
	}

	if (__singleCrop) {
		tips[__COL_NAME] = null;
		tips[__COL_DAY_PCT] = null;
		tips[__COL_VALUE] = null;
	}
	else {
		tips[__COL_NAME] = null;
		tips[__COL_GDATE1] = null;
		tips[__COL_GDATE2] = null;
		tips[__COL_GDATE3] = null;
		tips[__COL_GDATE4] = null;
		tips[__COL_GDATE5] = null;
		tips[__COL_GDATES] = null;
		tips[__COL_TMOIS1] = null;
		tips[__COL_TMOIS2] = null;
		tips[__COL_MAD] = null;
		tips[__COL_IRX] = null;
		tips[__COL_FRX] = null;
		tips[__COL_AWC] = null;
		tips[__COL_APD] = null;
		tips[__COL_TFLAG1] = "<html>Spring frost date flag"
			+ "<br>0 = mean<br>1 = 28F<br>2 = 32F</html>";
		tips[__COL_TFLAG2] = "<html>Fall frost date flag"
			+ "<br>0 = mean<br>1 = 28F<br>2 = 32F</html>";
		tips[__COL_CUT2] = "<html>Days between 1st and 2nd cutting."
			+ "<br>Alfalfa only.</html>";
		tips[__COL_CUT3] = "<html>Days between 2nd and 3rd cutting."
			+ "<br>Alfalfa only.</html>";
	}

	return tips;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int column) {
	if (__singleCrop) {
		switch (column) {
			case __COL_NAME:	return "%-40s";
			case __COL_DAY_PCT:	return "%8d";
			case __COL_VALUE:	return "%8.2f";
		}
	}
	else {
		switch (column) {
			case __COL_NAME:	return "%-30.30s";
			case __COL_GDATE1:	return "%8d";
			case __COL_GDATE2:	return "%8d";
			case __COL_GDATE3:	return "%8d";
			case __COL_GDATE4:	return "%8d";
			case __COL_GDATE5:	return "%8d";
			case __COL_GDATES:	return "%8d";
			case __COL_TMOIS1:	return "%8.2f";
			case __COL_TMOIS2:	return "%8.2f";
			case __COL_MAD:		return "%8.2f";
			case __COL_IRX:		return "%8.2f";
			case __COL_FRX:		return "%8.2f";
			case __COL_AWC:		return "%8.2f";
			case __COL_APD:		return "%8.2f";
			case __COL_TFLAG1:	return "%8d";
			case __COL_TFLAG2:	return "%8d";
			case __COL_CUT2:	return "%8d";
			case __COL_CUT3:	return "%8d";
		}
	}
	return "%-8s";
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
	// Numbers for months must be greater than 0 and less than 13
	Validator[] month = new Validator[] {
		Validators.notBlankValidator(),
		Validators.rangeValidator( 0, 13 ) };
	// Numbers for days must be greater than 0 and less than 32
	Validator[] day = new Validator[] {
		Validators.notBlankValidator(),
		Validators.rangeValidator( 0, 32 ) };
	// farenheit temperatures must be greater than -1 and less
	// than  101
	Validator[] temp = new Validator[] {
		Validators.notBlankValidator(),
		Validators.rangeValidator( -1, 101 ) };
	// The frost date flag must be 0,1 or 2
	Validator [] frostFlag = new Validator[] {
		Validators.isEquals( new Integer( 0 ) ),
		Validators.isEquals( new Integer( 1 ) ),
		Validators.isEquals( new Integer( 2 ) )};
	Validator [] frostFlagValidators = new Validator[] {
		Validators.notBlankValidator(),
		Validators.or( frostFlag ) };
	
	if (__singleCrop) {
		switch (col) {
			case __COL_NAME: 	return blank;
			case __COL_DAY_PCT:			
				if (__blaneyCriddle == null) {
					return no_checks;
				}
				// TODO KAT 2007-04-12 Need to find out
				// if a different check needs to happen
				// if dayNotPercent is true or false
				if (__dayNotPercent) {
					return nums; 
				}
				else {
					return nums; 
				}
			case __COL_VALUE:
				if (__blaneyCriddle == null) {
					return no_checks;
				}
				// TODO KAT 2007-04-12 Need to find out
				// if a different check needs to happen
				// if dayNotPercent is true or false
				if (__dayNotPercent) {
					return nums;
				}
				else {
					return nums;
				}
			default:	return no_checks;
		}
	} 
	else {
		switch (col) {
			case __COL_NAME:	return blank;
			// TODO KAT 2007-04-12 Need to add checks for dates
			// and test date checks.  For now just check for blank.
			case __COL_GDATE1:	return month;
			case __COL_GDATE2:	return day;
			case __COL_GDATE3:	return month;
			case __COL_GDATE4:	return day;
			case __COL_GDATE5:	return day;
			case __COL_GDATES:	return nums;
			case __COL_TMOIS1:	return temp;
			case __COL_TMOIS2:	return temp;
			case __COL_MAD:		return nums;
			case __COL_IRX:		return nums;
			case __COL_FRX:		return nums;
			case __COL_AWC:		return nums;
			case __COL_APD:		return nums;
			case __COL_TFLAG1:	return frostFlagValidators;
			case __COL_TFLAG2:	return frostFlagValidators;
			case __COL_CUT2:	return nums;
			case __COL_CUT3:	return nums;
			default: 			return no_checks;
		}
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

	StateCU_CropCharacteristics crop 
		= (StateCU_CropCharacteristics)_data.elementAt(row);
	
	if (__singleCrop) {
		switch (col) {
			case __COL_NAME: return crop.getName();
			case __COL_DAY_PCT:			
				if (__blaneyCriddle == null) {
					return new Integer( 0 );
				}
				if (__dayNotPercent) {
					return new Integer(
						__blaneyCriddle.getNckcp(row)); 
				}
				else {
					return new Integer(
						__blaneyCriddle.getNckca(row)); 
				}
			case __COL_VALUE:
				if (__blaneyCriddle == null) {
					return new Double( 0 );
				}
	
				if (__dayNotPercent) {
					return new Double(
						__blaneyCriddle.getCkcp(row));
				}
				else {
					return new Double(
						__blaneyCriddle.getCkca(row));
	
				}
		}
	} 
	else {
		switch (col) {
			case __COL_NAME:	return crop.getName();
			case __COL_GDATE1:	
				return new Integer(crop.getGdate1());
			case __COL_GDATE2:	
				return new Integer(crop.getGdate2());
			case __COL_GDATE3:	
				return new Integer(crop.getGdate3());
			case __COL_GDATE4:	
				return new Integer(crop.getGdate4());
			case __COL_GDATE5:	
				return new Integer(crop.getGdate5());
			case __COL_GDATES:	
				return new Integer(crop.getGdates());
			case __COL_TMOIS1:	
				return new Double(crop.getTmois1());
			case __COL_TMOIS2:	
				return new Double(crop.getTmois2());
			case __COL_MAD:		
				return new Double(crop.getMad());
			case __COL_IRX:		
				return new Double(crop.getIrx());
			case __COL_FRX:		
				return new Double(crop.getFrx());
			case __COL_AWC:		
				return new Double(crop.getAwc());
			case __COL_APD:		
				return new Double(crop.getApd());
			case __COL_TFLAG1:	
				return new Integer(crop.getTflg1());
			case __COL_TFLAG2:	
				return new Integer(crop.getTflg2());
			case __COL_CUT2:	
				return new Integer(crop.getCut2());
			case __COL_CUT3:	
				return new Integer(crop.getCut3());
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

	if (__singleCrop) {
		widths[__COL_NAME] = 	20;
		widths[__COL_DAY_PCT] = 11;
		widths[__COL_VALUE] = 	16;
	}
	else {
		widths[__COL_NAME] = 	15;
		widths[__COL_GDATE1] = 	6;
		widths[__COL_GDATE2] = 	6;
		widths[__COL_GDATE3] = 	6;
		widths[__COL_GDATE4] = 	6;
		widths[__COL_GDATE5] = 	8;
		widths[__COL_GDATES] = 	6;
		widths[__COL_TMOIS1] = 	9;
		widths[__COL_TMOIS2] = 	9;
		widths[__COL_MAD] = 	9;
		widths[__COL_IRX] =	9;
		widths[__COL_FRX] = 	11;
		widths[__COL_AWC] = 	11;
		widths[__COL_APD] = 	9;
		widths[__COL_TFLAG1] = 	6;
		widths[__COL_TFLAG2] = 	4;
		widths[__COL_CUT2] = 	10;
		widths[__COL_CUT3] = 	10;
	}

	return widths;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
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
public void setValueAt(Object value, int row, int col) {

	switch (col) {
	}	

	super.setValueAt(value, row, col);	
}	

/**
Sets the parent well under which the right and return flow data is stored.
@param parent the parent well.
*/
public void setParentCropCharacteristics(StateCU_CropCharacteristics parent) {
	// TODO SAM 2007-03-01 Evaluate use
	//__parentCrop = parent;
}

public void setBlaneyCriddle(StateCU_BlaneyCriddle bc) {
	__blaneyCriddle = bc;

	if (bc == null) {
		_rows = 0;
		fireTableDataChanged();
		return;
	}

	String flag = bc.getFlag();
	if (flag.equalsIgnoreCase("Day")) {
		__dayNotPercent = true;
		_rows = 25;
	}
	else {
		__dayNotPercent = false;
		_rows = 21;
	}
	fireTableDataChanged();
}

}
