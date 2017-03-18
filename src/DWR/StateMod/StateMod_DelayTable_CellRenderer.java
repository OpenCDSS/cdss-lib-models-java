// ----------------------------------------------------------------------------
// StateMod_DelayTable_CellRenderer - Class for rending delay table 
//	jworksheet cells
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-28	JTS, RTi		Initial version.
// 2003-06-17	JTS, RTi		Removed static references to table model
//					since table model is now passed in 
//					through the constructor.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used for rendering cells for delay table tables.
*/
@SuppressWarnings("serial")
public class StateMod_DelayTable_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which this class renders the cells.
*/
private StateMod_DelayTable_TableModel __tableModel;

/**
Constructor.  
@param tableModel the tableModel for which this will render the cells
*/
public StateMod_DelayTable_CellRenderer(
StateMod_DelayTable_TableModel tableModel) {
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
