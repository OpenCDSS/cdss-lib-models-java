// ----------------------------------------------------------------------------
// StateMod_DiversionRight_CellRenderer - Class for rendering data for 
//	diversion right tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-10	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-17	JTS, RTi		Removed static references to table model
//					since table model is now passed in 
//					through the constructor.
// 2004-10-26	SAM, RTi		Split out from the
//					StateMod_Diversion_CellRenderer class.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class renders cells for diversion right tables.
*/
public class StateMod_DiversionRight_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which this class renders the cells.
*/
private StateMod_DiversionRight_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which this class renders the cells.
*/
public StateMod_DiversionRight_CellRenderer(
StateMod_DiversionRight_TableModel tableModel) {
	__tableModel = tableModel;
}

/**
Returns the format for a given column.
@param column the colum for which to return the format.
@return the format (as used by StringUtil.format) for a column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Returns the widths of the columns in the table.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __tableModel.getColumnWidths();
}

}
