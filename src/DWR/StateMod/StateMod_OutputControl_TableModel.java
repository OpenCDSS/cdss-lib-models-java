// StateMod_OutputControl_TableModel - Table model for displaying data for output control-related tables

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
// StateMod_OutputControl_TableModel - Table model for displaying data for 
//	output control-related tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-09	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-29	JTS, RTi		JWorksheet_RowTableModel changed to
//					JWorksheet_AbstractRowTableModel.
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays output control data.
*/
@SuppressWarnings("serial")
public class StateMod_OutputControl_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_GraphNode> {

/**
The kinds of station types that can appear in the table.
*/
private final String	
	__DIVtype = "DIV",
	__REStype = "RES",
	__ISFtype = "ISF",
	__FLOtype = "FLO",
	__WELtype = "WEL",
	__OTHtype = "OTH";

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 3;

/**
References to columns.
*/
public static final int
	COL_TYPE =	0,
	COL_ID =	1,
	COL_SWITCH = 	2;

/**
Whether the data has been edited or not.
*/
private boolean __dirty = false;

/**
The worksheet in which this table model is working.
*/
private JWorksheet __worksheet;

/**
Liss of data for filling ID lists.
*/
private List<StateMod_RiverNetworkNode> __riverNetworkList;

/**
ID lists to be displayed in the combo boxes.
*/
private List<String>
	__reservoirIDs = null,
	__diversionIDs = null,
	__instreamFlowIDs = null,
	__streamIDs = null,
	__wellIDs = null,
	__otherIDs = null;

/**
Constructor.  
@param parent the StateMod_OutputControl_JFrame in which the table is displayed
@param data the data that will be used to fill in the table.
@param riverNetworkList the data that will be used to fill in the table IDS.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_OutputControl_TableModel(StateMod_OutputControl_JFrame parent, List<StateMod_GraphNode> data, List<StateMod_RiverNetworkNode> riverNetworkList) 
throws Exception {
	__riverNetworkList = riverNetworkList;
	
	if (data == null) {
		throw new Exception ("Invalid data list passed to " 
			+ "StateMod_OutputControl_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;

	__diversionIDs = createAvailableIDsList(getTypeIDVector(__DIVtype));
	__instreamFlowIDs = createAvailableIDsList(getTypeIDVector(__ISFtype));
	__reservoirIDs = createAvailableIDsList(getTypeIDVector(__REStype));
	__streamIDs = createAvailableIDsList(getTypeIDVector(__FLOtype));
	__wellIDs = createAvailableIDsList(getTypeIDVector(__WELtype));
	__otherIDs = createAvailableIDsList(getTypeIDVector(__OTHtype));
}

/**
Checks whether enough values have been entered in the current last row to tell
whether a new row can be added.  A new row can only be added if values have 
been set for columns 1 and 2 and 3
@return whether it is valid to add a new row
*/
public boolean canAddNewRow() {
	int rows = getRowCount();

	if (rows == 0) {
		return true;
	}

	String type = (String)getValueAt((rows - 1), 0);
	String id = (String)getValueAt((rows - 1), 1);
	String offOn = (String)getValueAt((rows - 1), 2);
	
	if (type == null || type.trim().equals("")) {
		return false;
	}
	if (id == null || id.trim().equals("")) {
		return false;
	}
	if (offOn == null || offOn.trim().equals("")) {
		return false;
	}

	return true;
}

/**
Creates a list of the available IDs for a list of StateMod_Data-extending objects.
@param nodes the nodes for which to create a list of IDs.
@return a Vector of Strings, each of which contains an ID followed by the 
name of Structure in parentheses
*/
private List<String> createAvailableIDsList(List<? extends StateMod_Data> nodes) {
	List<String> v = new Vector<String>();

	int num = 0;
	if (nodes != null) {
		num = nodes.size();
	}

	String name = null;
	for (int i = 0; i < (num - 1); i++) {
		name = nodes.get(i).getName();
		name = name.substring (0, name.length() - 4).trim();
		v.add( nodes.get(i).getID() + " (" + name + ")");
	}
	return v;
}

/**
Fills the ID column based on the kind of structure selected.
@param row the row of the ID column being dealt with
@param type the type of structure selected (column 1)
*/
public void fillIDColumn(int row, String type) {
	List<String> ids = new Vector<String>();
	if (type.equalsIgnoreCase("Diversion")) {
		ids = __diversionIDs;
	}
	else if (type.equalsIgnoreCase("Instream flow")) {
		ids = __instreamFlowIDs;
	}
	else if (type.equalsIgnoreCase("Reservoir")) {
		ids = __reservoirIDs;
	}
	else if (type.equalsIgnoreCase("Streamflow")) {
		ids = __streamIDs;
	}
	else if (type.equalsIgnoreCase("Well")) {
		ids = __wellIDs;
	}
	else if (type.equalsIgnoreCase("Other")) {
		ids = __otherIDs;
	}
	else if (type.equalsIgnoreCase("INS")) {
		ids = __instreamFlowIDs;
	}
	else if (type.equalsIgnoreCase("DIV")) {	
		ids = __diversionIDs;
	}
	else if (type.equalsIgnoreCase("RES")) {
		ids = __reservoirIDs;
	}
	else if (type.equalsIgnoreCase("STR")) {
		ids = __streamIDs;
	}
	else if (type.equalsIgnoreCase("WEL")) {
		ids = __wellIDs;
	}
	else {
		ids = __otherIDs;
	}
	
	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues(row, 1, ids);
	}
}

/**
Finds the ID of the appropriate type that starts with the given ID.
@param type the type of ID to search
@param ID the character string that the matching ID starts with
@return the matching ID.
*/
private String findIDMatch(String type, String ID) {
	List<String> search = null;
	
	if (type.equalsIgnoreCase("INS")) {
		search = __instreamFlowIDs;
	}
	else if (type.equalsIgnoreCase("DIV")) {	
		search = __diversionIDs;
	}
	else if (type.equalsIgnoreCase("RES")) {
		search = __reservoirIDs;
	}
	else if (type.equalsIgnoreCase("STR")) {
		search = __streamIDs;
	}
	else if (type.equalsIgnoreCase("WEL")) {
		search = __wellIDs;
	}
	else {
		search = __otherIDs;
	}

	int index = ID.indexOf(" ");
	if (index != -1) {
		ID = ID.substring(0, index);
	}

	int size = search.size();
	String val = null;
	for (int i = 0; i < size; i++) {	
		val = search.get(i);
		if (val.startsWith(ID)) {
			return val;
		}
	}
	return "";
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_TYPE:		return String.class;
		case COL_ID:		return String.class;
		case COL_SWITCH:	return String.class;
		default:		return String.class;
		
	}
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_TYPE:		return "STATION TYPE";
		case COL_ID:		return "ID";
		case COL_SWITCH:	return "SWITCH";
		default:		return " ";
	}
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_TYPE:		return "%-40s";
		case COL_ID:		return "%-40s";
		case COL_SWITCH:	return "%-40s";
		default:		return "%-8s";
	}
}

/**
Returns the appropriate ID list for the given structure type.
@param type the structure type for which to return the ID Vector.
@return the ID Vector for the structure type.
*/
private List<StateMod_RiverNetworkNode> getTypeIDVector(String type) {
	int size = __riverNetworkList.size();

	StateMod_RiverNetworkNode node = null;
	String name = null;
	List<StateMod_RiverNetworkNode> v = new Vector<StateMod_RiverNetworkNode>();
	// loops through to (num - 1) because the last element of the network Vector is "END"
	for (int i = 0; i < size; i++) {
		node = __riverNetworkList.get(i);

		name = node.getName();
		if (type.equals(__OTHtype) || name.endsWith(type)) {
			v.add(node);
		}
	}
	return v;
}

/**
Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_GraphNode gn = _data.get(row);

	String ID = gn.getID();
	switch (col) {
		case COL_TYPE:	
			String type = gn.getType();
			if (type.equals("")) {
				return "";
			}			
			else if (type.equalsIgnoreCase("INS") ||
			    type.equalsIgnoreCase("Instream Flow")) {
				return "Instream Flow";
			}
			else if (type.equalsIgnoreCase("DIV") ||
				 type.equalsIgnoreCase("Diversion")) {	
				return "Diversion";
			}
			else if (type.equalsIgnoreCase("RES") ||
				 type.equalsIgnoreCase("Reservoir")) {
				return "Reservoir";
			}
			else if (type.equalsIgnoreCase("STR") ||
				 type.equalsIgnoreCase("Streamflow")) {
				return "Streamflow";
			}
			else if (type.equalsIgnoreCase("WEL") ||
			 	 type.equalsIgnoreCase("Well")) {
				return "Well";
			}
			else {
				if (ID.equals("All")) {
					return "";
				}
				return "Other";
			}
		case COL_ID:	
			if (ID.equals("All")) {
				return "All";
			}
			else if (ID.equals("")) {
				return "";
			}
			return findIDMatch(gn.getType(), gn.getID());
		case COL_SWITCH:	
			if (ID.equals("All")) {
				return "";
			}
			if (gn.getSwitch() == 0) {
				return "Off";
			}
			else if (gn.getSwitch() == 1) {
				return "On";
			}
			else {
				return "";
			}
		default:
			return "";
	}
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}
	widths[COL_TYPE] = 15;	// station type
	widths[COL_ID] = 37;	// id
	widths[COL_SWITCH] = 17;	// switch
	return widths;
}

/**
Returns whether the cell is editable or not.  In this model, all the cells in
columns 3 and greater are editable.
@param rowIndex unused.
@param columnIndex the index of the column to check whether it is editable
or not.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	int size = _cellEditOverride.size();

	if (size > 0) {
		int[] temp;
		for (int i = 0; i < size; i++) {
			temp = (int[])_cellEditOverride.get(i);
			if (temp[0] == rowIndex && temp[1] == columnIndex) {
				if (temp[2] == 1) {
					return true;
				}
				else {
					return false;
				}
			}
		}
	}
	return true;
}

/**
Returns whether any of the data has been modified.
@return whether any of the data has been modified.
*/
public boolean isDirty() {
	return __dirty;
}

/**
Sets whether any of the data has been modified.
@param dirty whether any of the data has been modified
*/
public void setDirty(boolean dirty) {
	__dirty = dirty;
}
	
/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	setDirty(true);
	StateMod_GraphNode gn = _data.get(row);

	switch (col) {
		case COL_TYPE:	
			String type = gn.getType();
			if (type.equals((String)value)) {
				break;
			}
			gn.setType((String)value);		
			gn.setID("");
			gn.setSwitch(-1);
			setValueAt("", row, 1);
			setValueAt("", row, 2);
			fireTableDataChanged();
			overrideCellEdit(row, 1, true);
			overrideCellEdit(row, 2, false);
			fillIDColumn(row, (String)value);
			break;
		case COL_ID:	
			gn.setID((String)value);
			gn.setSwitch(-1);
			setValueAt("", row, 2);
			fireTableDataChanged();
			overrideCellEdit(row, 2, true);
			break;
		case COL_SWITCH:	
			String offOn = (String)value;
			if (offOn.equals("Off")) {
				gn.setSwitch(0);
			}
			else if (offOn.equals("On")) {
				gn.setSwitch(1);
			}
			else {
				gn.setSwitch(-1);
			}
	}	

	super.setValueAt(value, row, col);	
}	

/**
Sets the worksheet in which this model is being used.
@param worksheet the worksheet in which this model is being used

*/
public void setWorksheet(JWorksheet worksheet) {
	__worksheet = worksheet;
}

}
