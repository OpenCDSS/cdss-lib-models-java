// ----------------------------------------------------------------------------
// StateCU_CropCharacteristics_CellRenderer - Class for rendering cells for 
//	crop char tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-28	JTS, RTi		Initial version.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render crop char cells.
*/
@SuppressWarnings("serial")
public class StateCU_CropCharacteristics_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

private StateCU_CropCharacteristics_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which to render cells
*/
public StateCU_CropCharacteristics_CellRenderer(
StateCU_CropCharacteristics_TableModel tableModel) {
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
