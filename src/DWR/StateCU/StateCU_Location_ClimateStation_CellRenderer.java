// StateCU_Location_ClimateStation_CellRenderer - Class for rendering cells for location to climate station tables

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Models Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Models Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

// ----------------------------------------------------------------------------
// StateCU_Location_ClimateStation_CellRenderer - Class for rendering cells for 
//	location to climate station tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-29	JTS, RTi		Initial version.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render location cells.
*/
@SuppressWarnings("serial")
public class StateCU_Location_ClimateStation_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

private StateCU_Location_ClimateStation_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which to render cells
*/
public StateCU_Location_ClimateStation_CellRenderer(
StateCU_Location_ClimateStation_TableModel tableModel) {
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
