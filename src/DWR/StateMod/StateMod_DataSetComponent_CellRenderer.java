// ----------------------------------------------------------------------------
// StateMod_DataSetComponent_CellRenderer - class to render cells for
//				StateMod_DataSet component
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-10	Steven A. Malers, RTi	Initial version - copy and modify the
//					similar StateCU class.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.GUI.JWorksheet_DefaultTableCellRenderer;

/**
This class is used to render cells for StateMod_DataSetComponent_TableModel
data.
*/
public class StateMod_DataSetComponent_CellRenderer extends
JWorksheet_DefaultTableCellRenderer{

StateMod_DataSetComponent_TableModel __table_model = null;	// Table model
								// to render

/**
Constructor.
@param table_model The StateMod_DataSetComponent_TableModel to render.
*/
public StateMod_DataSetComponent_CellRenderer (
	StateMod_DataSetComponent_TableModel table_model )
{	__table_model = table_model;
}

/**
Returns the format for a given column.
@param column the colum for which to return the format.
@return the column format as used by StringUtil.formatString().
*/
public String getFormat(int column) {
	return __table_model.getFormat(column);	
}

/**
Returns the widths of the columns in the table.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __table_model.getColumnWidths();
}

} // End StateMod_DataSetComponent_CellRenderer
