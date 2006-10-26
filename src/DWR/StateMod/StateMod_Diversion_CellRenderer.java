// ----------------------------------------------------------------------------
// StateMod_Diversion_CellRenderer - Class for rendering data for 
//	diversion station tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-10	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-17	JTS, RTi		Removed static references to table model
//					since table model is now passed in 
//					through the constructor.
// 2004-10-26	SAM, RTi		Split out the rights and return flows
//					into their own cell renderers.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class renders cells for diversion station tables.
*/
public class StateMod_Diversion_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which this class renders the cells.
*/
private StateMod_Diversion_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which this class renders the cells.
*/
public StateMod_Diversion_CellRenderer(
StateMod_Diversion_TableModel tableModel) {
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
