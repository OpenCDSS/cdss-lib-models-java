// ----------------------------------------------------------------------------
// StateMod_StreamEstimate_CellRenderer - Class for rendering cells for 
//	stream estimate station-related tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-08-20	J. Thomas Sapienza, RTi	Initial version.
// 2003-09-11	Steven A. Malers, RTi	Rename the class from
//					StateMod_BaseFlowStation_CellRenderer to
//					StateMod_StreamEstimate_CellRenderer and
//					make related changes.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class renders cells for stream estimate station tables.
*/
public class StateMod_StreamEstimate_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
Table model for which this class renders the cells.
*/
private StateMod_StreamEstimate_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which this class renders cells.
*/
public StateMod_StreamEstimate_CellRenderer(
StateMod_StreamEstimate_TableModel tableModel) {
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
