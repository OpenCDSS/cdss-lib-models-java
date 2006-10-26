// ----------------------------------------------------------------------------
// StateMod_Response_CellRenderer - Class for rendering cells for the
//	response table.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-09-10	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class renders cells for the response table.
*/
public class StateMod_Response_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
Table model for which this class renders the cells.
*/
private StateMod_Response_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which this class renders cells.
*/
public StateMod_Response_CellRenderer(
StateMod_Response_TableModel tableModel) {
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
