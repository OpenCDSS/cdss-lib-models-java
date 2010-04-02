package DWR.StateCU;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render Penman-Monteith cells.
*/
public class StateCU_PenmanMonteith_CellRenderer extends JWorksheet_AbstractExcelCellRenderer
{

private StateCU_PenmanMonteith_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which to render cells
*/
public StateCU_PenmanMonteith_CellRenderer(StateCU_PenmanMonteith_TableModel tableModel) {
	__tableModel = tableModel;
}

/**
Returns the format for a given column.
@param column the column for which to return the format.
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