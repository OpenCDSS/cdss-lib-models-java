// ----------------------------------------------------------------------------
// StateMod_DelayTable_Data_CellRenderer - Class for rending delay table 
//	jworksheet cells
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2005-03-29	JTS, RTi		Initial version.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used for rendering cells for delay table tables.
*/
public class StateMod_DelayTable_Data_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which this class renders the cells.
*/
private StateMod_DelayTable_Data_TableModel __tableModel;

/**
Constructor.  
@param tableModel the tableModel for which this will render the cells
*/
public StateMod_DelayTable_Data_CellRenderer(
StateMod_DelayTable_Data_TableModel tableModel) {
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
