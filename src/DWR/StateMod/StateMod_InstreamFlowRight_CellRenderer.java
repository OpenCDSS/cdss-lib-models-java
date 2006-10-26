// ----------------------------------------------------------------------------
// StateMod_InstreamFlowRight_CellRenderer - Class for rending data for instream
//					flow right tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-28	JTS, RTi		Initial version.
// 2004-10-28	Steven A. Malers, RTi	Split code out of
//					StateMod_InstreamFlow_CellRenderer.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render cells for instream flow right tables.
*/
public class StateMod_InstreamFlowRight_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
Table model for which this class renders cells.
*/
private StateMod_InstreamFlowRight_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which this class renders cells.
*/
public StateMod_InstreamFlowRight_CellRenderer(
StateMod_InstreamFlowRight_TableModel tableModel) {	
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
