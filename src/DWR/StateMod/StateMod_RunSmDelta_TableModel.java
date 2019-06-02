// StateMod_RunSmDelta_TableModel - Table model for displaying data for delta plot-related tables

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
// StateMod_RunSmDelta_TableModel - Table model for displaying data for 
//	delta plot-related tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-08-21	J. Thomas Sapienza, RTi	Initial version.
// 2003-08-25	JTS, RTi		Expanded with much more logic including:
//					- filling in parameter types based on
//					  selections
//					- filling in IDs based on selections
//					- browsing for filenames
// 2003-08-26	JTS, RTi		Continued expanding the logic to
//					fill out the table data properly.
// 2003-08-27	JTS, RTi		Further continued work on the logic.
// 2004-01-22	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// 2006-03-06	JTS, RTi		* The last row in the worksheet can now
//					  be deleted with preDeleteRow().
//					* Added javadocs for all the methods.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.GUI.JWorksheet_CellAttributes;
import RTi.Util.GUI.SimpleFileFilter;

/**
This table model display delta plot data.
*/
@SuppressWarnings({ "serial" })
public class StateMod_RunSmDelta_TableModel 
extends JWorksheet_AbstractRowTableModel<StateMod_GraphNode> {

/**
Number of columns in the table model.
*/
private final static int __COLUMNS = 5;

/**
The parent frame on which the JWorksheet for this model is displayed.
*/
private StateMod_RunSmDelta_JFrame __parent = null;

/**
The worksheet in which this table model is working.
*/
private JWorksheet __worksheet;

/**
List of data for filling ID lists.
*/
private List<StateMod_Reservoir> __reservoirs;
private List<StateMod_Diversion> __diversions;
private List<StateMod_InstreamFlow> __instreamFlows;
private List<StateMod_Well> __wells;
private List<StateMod_StreamGage> __streamGages;

/**
ID lists to be displayed in the combo boxes.
*/
private List<String> __reservoirIDs = null;
private List<String> __diversionIDs = null;
private List<String> __instreamFlowIDs = null;
private List<String> __streamflowIDs = null;
private List<String> __streamflow0IDs = null;
private List<String> __wellIDs = null;

protected String _ABOVE_STRING = "";

private JWorksheet_CellAttributes __needsFilled;

public final static int 
	COL_FILE = 0,
	COL_TYPE = 1,
	COL_PARM = 2,
	COL_YEAR = 3,
	COL_ID = 4;

/**
Constructor.  
@param parent the StateMod_RunDeltaPlot_JFrame in which the table is displayed
@param data the data that will be displayed in the table.
@param reservoirs list of StateMod_Reservoir objects
@param diversions list of StateMod_Diversion objects
@param instreamFlows list of StateMod_InstreamFlow objects.
@param streamGages list of StateMod_??? objects
@param wells list of StateMod_Well objects
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_RunSmDelta_TableModel(StateMod_RunSmDelta_JFrame parent,
List<StateMod_GraphNode> data, List<StateMod_Reservoir> reservoirs, List<StateMod_Diversion> diversions,
List<StateMod_InstreamFlow> instreamFlows, List<StateMod_StreamGage> streamGages, List<StateMod_Well> wells)
throws Exception {
	__parent = parent;

	__reservoirs = reservoirs;
	__diversions = diversions;
	__instreamFlows = instreamFlows;
	__wells = wells;
	__streamGages = streamGages;

	__needsFilled = new JWorksheet_CellAttributes();
	__needsFilled.borderColor = Color.red;

	if (data == null) {
		throw new Exception ("Invalid data Vector passed to StateMod_RunSmDelta_TableModel constructor.");
	}
	_rows = data.size();
	_data = data;
}

/**
Opens a dialog from which users can browse for delta plot files.
@return the path to the file the user chose, or "" if no file was selected.
*/
private String browseForFile() {
	JGUIUtil.setWaitCursor(__parent, true);
	String directory = 
		JGUIUtil.getLastFileDialogDirectory();

	JFileChooser fc = null;
	if (directory != null) {
		fc = new JFileChooser(directory);
	}
	else {
		fc = new JFileChooser();
	}

	fc.setDialogTitle("Select File");
	SimpleFileFilter xre = new SimpleFileFilter("xdd", "ASCII xre");
	SimpleFileFilter b44 = new SimpleFileFilter("b44", "Binary b44");
	SimpleFileFilter xdd = new SimpleFileFilter("xdd", "ASCII xdd");
	SimpleFileFilter b43 = new SimpleFileFilter("b43", "Binary b43");

	fc.addChoosableFileFilter(xre);
	fc.addChoosableFileFilter(b44);
	fc.addChoosableFileFilter(xdd);
	fc.addChoosableFileFilter(b43);
	fc.setAcceptAllFileFilterUsed(true);
	fc.setFileFilter(xre);
	fc.setDialogType(JFileChooser.SAVE_DIALOG); 	

	JGUIUtil.setWaitCursor(__parent, false);
	int retVal = fc.showSaveDialog(__parent);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return "";
	}

	String currDir = (fc.getCurrentDirectory()).toString();

	if (!currDir.equalsIgnoreCase(directory)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	String filename = fc.getSelectedFile().getName();

	return currDir + File.separator + filename;
}

/**
Checks to see if a row can be added to the table.  Rows cannot be added if the first row is not fully
filled out.  
TODO (JTS - 2006-03-06)
I think this is bad code.  I think the elementAt() call should return the 
last data value -- as it is, it is only checking that the first row is 
set up properly.
@return true if a new row can be added, false if not.
*/
public boolean canAddRow() {
	if (_rows == 0) {
		return true;
	}
	StateMod_GraphNode gn = _data.get(0);
	if (gn.getFileName().trim().equals("")) {
		return false;
	}
	if (gn.getType().trim().equals("")) {
		return false;
	}
	if (gn.getDtype().trim().equals("")) {
		return false;
	}
	if (gn.getYrAve().trim().equals("")) {
		return false;
	}
	if (gn.getID().trim().equals("")) {
		return false;
	}
	return true;
}

/**
This method is called when a new row is added, and it copies the nearest combo
box type from above the new row and sets the new row's columns to the same value
as that in the combo box.  Also fills the parameter and ID columns the same.
*/
public void copyDownComboBoxes() {
	if (_rows == 1) {
		return;
	}
	
	String type = null;
	int i = _rows - 2;
	while (i >= 0 && type == null) {
		type = (String)(getValueAt(i, COL_TYPE));
		if (type.trim().equals("")) {
			type = null;
		}
		i--;
	}
	if (type == null) {
		return;
	}
	
	setValueAt("", _rows - 1, COL_PARM);
	fillParameterColumn(_rows - 1, type);
	fillIDColumn(_rows - 1, type);
}

/**
Creates a list of the available IDs for a Vector of StateMod_Data-extending objects.
@param nodes the nodes for which to create a list of IDs.
@return a list of Strings, each of which contains an ID followed by the 
name of Structure in parentheses
*/
private List<String> createAvailableIDsList(List<? extends StateMod_Data> nodes) {
	List<String> v = new Vector<String>();

	int num = 0;
	if (nodes != null) {
		num = nodes.size();
	}

	v.add("0 (All)");

	for (int i = 0; i < num; i++) {
		v.add(nodes.get(i).getID() + " (" 
			+ nodes.get(i).getName() + ")");
	}
	return v;
}

/**
Clears the values in the parameter ID combo box in the given row.
@param row the row to clear the combo box values.
*/
public void emptyParmIDComboBoxes(int row) {
	String s = null;
	boolean skip = true;
	for (int i = row; i < _rows; i++) {
		if (skip) {
			s = "";
			skip = false;
		}
		else {
			s = (String)(getValueAt(i, COL_TYPE));
		}

		if (!s.equals("")) {
			return;
		}
		fillParameterColumn(i, "");
		fillIDColumn(i, "");
		setValueAt("", i, COL_PARM);
		setValueAt("", i, COL_ID);
	}
}

/**
Fills the parameter column in the given row based on the station type selected for that row.
@param row the row for which to fill the parameter column.
@param type the type of the station in the given row.
*/
public void fillParameterColumn(int row, String type) {
	List<String> datatypes = new Vector<String>();

	// Get the model output data types (no input since SmDelta deals with output).
	if (type.equalsIgnoreCase("Diversion")) {
		datatypes = StateMod_GraphNode.getGraphDataType(StateMod_GraphNode.DIVERSION_TYPE, false);
	}
	else if (type.equalsIgnoreCase("Instream flow")) {
		datatypes = StateMod_GraphNode.getGraphDataType(StateMod_GraphNode.INSTREAM_TYPE, false);
	}
	else if (type.equalsIgnoreCase("Reservoir")) {
		datatypes = StateMod_GraphNode.getGraphDataType(StateMod_GraphNode.RESERVOIR_TYPE, false);
	}
	else if (type.equalsIgnoreCase("Streamflow")) {
		datatypes = StateMod_GraphNode.getGraphDataType(StateMod_GraphNode.STREAM_TYPE, false);
	}
	else if (type.equalsIgnoreCase("Well")) {
		datatypes = StateMod_GraphNode.getGraphDataType(StateMod_GraphNode.WELL_TYPE, false);
	}
	else if (type.equalsIgnoreCase("Stream ID (0* Gages)")) {
		datatypes = StateMod_GraphNode.getGraphDataType(StateMod_GraphNode.STREAM_TYPE, false);
	}

	List<String> finalTypes = new Vector<String>();
	finalTypes.add("");

	for (int i = 0; i < datatypes.size(); i++) {
		// FIXME SAM 2008-03-20 No need to remove underscores for newer versions of StateMod, right?.
		// Use data types from binary file.
		//finalTypes.add(((String)datatypes.elementAt(i)).replace('_', ' '));
		finalTypes.add(datatypes.get(i));
	}

	if (__worksheet != null) {
		//System.out.println("Setting cell-specific stuff");
		__worksheet.setCellSpecificJComboBoxValues(row, COL_PARM, finalTypes);
	}
}

/**
Fills the ID column based on the kind of structure selected.
@param row the row of the ID column being dealt with
@param type the type of structure selected (column 1)
*/
public void fillIDColumn(int row, String type) {
	List<String> ids = new Vector<String>();
	if (type.equalsIgnoreCase("Diversion")) {
		if (__diversionIDs == null) {
			__diversionIDs = createAvailableIDsList(__diversions);
		}
		ids = __diversionIDs;
	}
	else if (type.equalsIgnoreCase("Instream flow")) {
		if (__instreamFlowIDs == null) {
			__instreamFlowIDs = createAvailableIDsList(	__instreamFlows);
		}
		ids = __instreamFlowIDs;
	}
	else if (type.equalsIgnoreCase("Reservoir")) {
		if (__reservoirIDs == null) {
			__reservoirIDs = createAvailableIDsList(__reservoirs);
		}
		ids = __reservoirIDs;
	}
	else if (type.equalsIgnoreCase("Streamflow")) {
		if (__streamflowIDs == null) {
			__streamflowIDs = createAvailableIDsList(__streamGages);
		}
		ids = __streamflowIDs;
	}
	else if (type.equalsIgnoreCase("Well")) {
		if (__wellIDs == null) {
			__wellIDs = createAvailableIDsList(__wells);
		}
		ids = __wellIDs;
	}
	else if (type.equalsIgnoreCase("Stream ID (0* Gages)")) {
		if (__streamflow0IDs == null) {
			List<String> v = createAvailableIDsList(__streamGages);
			String s = null;
			__streamflow0IDs = new Vector<String>();
			for (int i = 0; i < v.size(); i++) {
				s = v.get(i);
				if (s.startsWith("0")) {
					__streamflow0IDs.add(s);
				}
			}
		}
		ids = __streamflow0IDs;
	}

	if (__worksheet != null) {
		__worksheet.setCellSpecificJComboBoxValues(row, COL_ID, ids);
	}
}

/**
Creates a list of objects suitable for use in the worksheet from the data
read from a delta plot file.
@param fileData the fileData to process.
@return a list of objects suitable for use within a form.
*/
public List<StateMod_GraphNode> formLoadData(List<StateMod_GraphNode> fileData) {
	int rows = fileData.size();

	if (rows == 0 ) {
		return new Vector<StateMod_GraphNode>();
	}

	// gnf will be a node used to read data FROM the _F_ile nodes
	StateMod_GraphNode gnf = fileData.get(0);

	String pfile = "";
	String ptype = "";
	String pyear = "";

	String file = null;
	String type = null;
	String dtype = null;
	String year = null;

	// gnw will be a node used for creating the _W_orksheet nodes
	StateMod_GraphNode gnw = null;

	List<StateMod_GraphNode> v = new Vector<StateMod_GraphNode>();

	int ids = 0;

	for (int i = 0; i < rows; i++) {
		gnf = fileData.get(i);
		ids = gnf.getIDVectorSize();

		file = gnf.getFileName().trim();
		type = gnf.getType().trim();
		dtype = gnf.getDtype().trim();
		year = gnf.getYrAve().trim();

		for (int j = 0; j < ids; j++) {
			if (j == 0) {
				gnw = new StateMod_GraphNode();
				if (!file.equals(pfile)) {
					gnw.setFileName(file);
				}
				else {
					gnw.setFileName("");
				}
				if (!type.equals(ptype)) {
					gnw.setType(type);
				}
				else {
					gnw.setType("");
				}
				if (!dtype.equals(dtype)) {
					gnw.setDtype(dtype);
				}
				else {
					gnw.setDtype("");
				}
				if (!year.equals(pyear)) {
					gnw.setYrAve(year);
				}
				else {
					gnw.setYrAve("");
				}
				gnw.setID(gnf.getID(0).trim());
			}
			else {
				gnw.setFileName("");
				gnw.setType("");
				gnw.setDtype("");
				gnw.setYrAve("");
				gnw.setID(gnf.getID(j).trim());
			}
			gnw.setSwitch(gnf.getSwitch());
			v.add(gnw);
		}

		pfile = file;
		ptype = type;
		pyear = year;
	}

	return v;
}

/**
Saves form data to the data set.
@param worksheetData the data in the worksheet to save.
@return a list of data objects created from the data in the worksheet.
*/
public List<StateMod_GraphNode> formSaveData(List<StateMod_GraphNode> worksheetData) {
	int rows = worksheetData.size();
	
	if (rows == 0) {
		return new Vector<StateMod_GraphNode>();
	}

	// gnw will be a node used to read data FROM the _W_orksheet nodes
	StateMod_GraphNode gnw = worksheetData.get(0);

	String pfile = gnw.getFileName().trim();
	String ptype = gnw.getType().trim();
	String pdtype = gnw.getDtype().trim();
	String pyear = gnw.getYrAve().trim();
	String pid = gnw.getID().trim();

	String file = null;
	String type = null;
	String dtype = null;
	String year = null;
	String id = null;

	// gno will be a node used for creating the _O_utput nodes
	StateMod_GraphNode gno = new StateMod_GraphNode();
	gno.setFileName(pfile);
	gno.setType(ptype);
	gno.setDtype(pdtype);
	gno.setYrAve(pyear);
	int paren = pid.indexOf("(");
	if (paren > -1) {
		gno.addID(pid.substring(0, paren).trim());
	}
	else {
		gno.addID(pid);
	}

	List<StateMod_GraphNode> v = new Vector<StateMod_GraphNode>();

	for (int i = 1; i < rows; i++) {
		gnw = worksheetData.get(i);

		file = gnw.getFileName().trim();
		if (file.equals("")) {
			file = pfile;
		}
		type = gnw.getType().trim();
		if (type.equals("")) {
			type = ptype;
		}
		dtype = gnw.getDtype().trim();
		if (dtype.equals("")) {
			dtype = pdtype;
		}
		year = gnw.getYrAve().trim();
		if (year.equals("")) {
			year = pyear;
		}
		id = gnw.getID().trim();
		if (id.equals("")) {
			id = pid;
		}

		if (file.equals(pfile) && type.equals(ptype) 
			&& dtype.equals(pdtype) && year.equals(pyear)) {
			// all the fields match, so this is a different ID
			// added to the Vector of IDs in the node's vector.
			paren = id.indexOf("(");
			if (paren > -1) {
				gno.addID(id.substring(0, paren).trim());
			}
			else {
				gno.addID(id);
			}
		}
		else {
			// otherwise, values other than just the ID are 
			// different, so a new node needs created
			v.add(gno);

			gno = new StateMod_GraphNode();
			gno.setFileName(file);
			gno.setType(type);
			gno.setDtype(dtype);
			gno.setYrAve(year);
			paren = id.indexOf("(");
			if (paren > -1) {
				gno.addID(id.substring(0, paren).trim());
			}
			else {
				gno.addID(id);
			}
		}

		pfile = file;
		ptype = type;
		pdtype = dtype;
		pyear = year;
		pid = id;
	}

	v.add(gno);

	return v;
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_FILE:	return String.class;
		case COL_TYPE:	return String.class;
		case COL_PARM:	return String.class;
		case COL_YEAR:	return String.class;
		case COL_ID: 	return String.class;
		default:	return String.class;

	}
}

/**
From AbstractTableModel; returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
From AbstractTableModel; returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_FILE:	return "FILE (1)";
		case COL_TYPE:	return "STATION TYPE (2)";
		case COL_PARM:	return "PARAMETER (3)";
		case COL_YEAR:	return "YEAR/AVE (4)";
		case COL_ID:	return "ID (5)";
		default:	return " ";
	}
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_FILE:	return "%-40s";
		case COL_TYPE:	return "%-40s";
		case COL_PARM:	return "%-40s";
		case COL_YEAR:	return "%-40s";
		case COL_ID:	return "%-40s";
		default:	return "%-8s";
	}
}

/**
Returns the nearest station type to the given row.  It does this by looking
throw the worksheet data starting at the row prior to the given one and running
back through row #0, stopping when it finds a row that has the station type
set.
@return the type of the nearest station, or "" if no type could be found.
*/
public String getNearestType(int row) {
	String s = null;
	for (int i = (row - 1); i >= 0; i--) {
		s = (String)(getValueAt(i, COL_TYPE));
		if (!s.equals("")) {
			return s;
		}
	}
	return "";
}

/**
From AbstractTableModel; returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableModel; returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	StateMod_GraphNode gn = (StateMod_GraphNode)_data.get(row);

	switch (col) {
		case COL_FILE:	return gn.getFileName();
		case COL_TYPE:	return gn.getType();
		case COL_PARM:	
			String s = gn.getDtype();
			// FIXME SAM 2008-03-24 No need to do this with newer StateMod
			// since binary file uses underscores for data types.
			//s = (s.replace('_', ' ')).trim();
			return s;
		case COL_YEAR:	return gn.getYrAve();
		case COL_ID:	return gn.getID();
		default:	return "";
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
	widths[COL_FILE] = 	15;
	widths[COL_TYPE] = 	20;
	widths[COL_PARM] = 	20;
	widths[COL_YEAR] = 	12;
	widths[COL_ID] = 	30;
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
	if (columnIndex == COL_PARM) {
		String value = (String)getValueAt(rowIndex, COL_TYPE);
		if (value == null || value.equals("")) {
			return true;
		}
		else {
			return true;
		}
	}
	else {
		return true;
	}
}

/**
Called in order to delete a data row from the worksheet.  This method does
some operations prior to the actual row delete in order to keep the delta 
plot bookkeeping going.
@param row the row to delete.
*/
public void preDeleteRow(int row) {
	if (row < 0 || row >= _rows) {
		return;
	}
	
	__worksheet.editingCanceled(new javax.swing.event.ChangeEvent(
		__worksheet));
	
	if (row < (_rows - 1)) {
		String dfile = (String)(getValueAt(row, COL_FILE));
		String file = (String)(getValueAt(row + 1, COL_FILE));
		String dtype = (String)(getValueAt(row, COL_TYPE));
		String type = (String)(getValueAt(row + 1, COL_TYPE));
		String dparm = (String)(getValueAt(row, COL_PARM));
		String parm = (String)(getValueAt(row + 1, COL_PARM));
		String dyear = (String)(getValueAt(row, COL_YEAR));
		String year = (String)(getValueAt(row + 1, COL_YEAR));
		String did = (String)(getValueAt(row, COL_ID));
		String id = (String)(getValueAt(row + 1, COL_ID));
	
		if (file.equals("")) {
			if (!dfile.equals("")) {
				setValueAt(dfile, row + 1, COL_FILE);
			}
		}
		if (type.equals("")) {
			if (!dtype.equals("")) {
				setValueAt(dtype, row + 1, COL_TYPE);
			}
		}
		if (parm.equals("")) {
			if (!dparm.equals("")) {
				setValueAt(dparm, row + 1, COL_PARM);
			}
		}
		if (year.equals("")) {
			if (!dyear.equals("")) {
				setValueAt(dyear, row + 1, COL_YEAR);
			}
		}
		if (id.equals("")) {
			if (!did.equals("")) {
				setValueAt(did, row + 1, COL_ID);
			}
		}
	}

	__worksheet.deleteRow(row);
}

/**
Sets the attributes for the cell at the given location.  
@param row the row of the cell.
@param column the column of the cell.
@param useAttributes if true then attributes will be set on the cell to 
indicate that a value needs to be placed in the row.  If false, then the
cell's attributes will be cleared.
*/
public void setCellAttributes(int row, int column, boolean useAttributes) {
	if (useAttributes) {
		__worksheet.setCellAttributes(row, column, __needsFilled);
	}
	else {
		__worksheet.setCellAttributes(row, column, null);
	}
}

/**
Blanks out all the data values in the final row.
*/
public void setLastRowAsBlank() {
	setValueAt("", _rows - 1, 1);
	setValueAt("", _rows - 1, 2);
	setValueAt("", _rows - 1, 3);
	setValueAt("", _rows - 1, 4);
	setValueAt("", _rows - 1, 5);
}

/**
Sets the parameter ID values in the given row for the given type of station.
@param row the row in which to set the values.
@param type the type of station in the row.
*/
public void setParmIDComboBoxes(int row, String type) {
	String s = (String)(getValueAt(row, COL_TYPE));
	boolean skip = true;
	if (s.equals("")) {
		skip = false;
	}
	for (int i = row; i < _rows; i++) {
		if (skip) {
			s = "";
			skip = false;
		}
		else {
			s = (String)(getValueAt(i, COL_TYPE));
		}

		if (!s.equals("")) {
			return;
		}
		fillParameterColumn(i, type);
		fillIDColumn(i, type);
		setValueAt("", i, COL_PARM);
		setValueAt("", i, COL_ID);
	}
}

/**
Inserts the specified value into the table at the given position.
@param value the object to store in the table cell.
@param row the row of the cell in which to place the object.
@param col the column of the cell in which to place the object.
*/
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	if (row >= _data.size()) {
		// this is probably happening as a result of an edit that did not end in time.
	}
	
	StateMod_GraphNode gn = (StateMod_GraphNode)_data.get(row);
//Message.printStatus(1, "", "Set value at: " + row + ", " + col + " '" + value + "'");
	switch (col) {
		case COL_FILE:	
			String filename = (String)value;
			String oldFilename = (String)(getValueAt(row, col));
			if (filename.equals(oldFilename)) {
				return;
			}

			if (filename.equalsIgnoreCase(
				StateMod_RunSmDelta_JFrame.OPTION_BROWSE)) {
				String newfile = browseForFile();
				gn.setFileName(newfile);
				if (row == 0) {
					if (newfile.trim().equals("")) {
						setCellAttributes(row, col, 
							true);
					}
					else {
						setCellAttributes(row, col, 
							false);
					}
				}
				super.setValueAt(newfile, row, col);
				return;
			}
			else {
				gn.setFileName(filename);
			}
			if (row == 0) {
				if (filename.trim().equals("")) {
					setCellAttributes(row, col, true);
				}
				else {
					setCellAttributes(row, col, false);
				}
			}
			break;
		case COL_TYPE:
			String type = (String)value;
			String oldType = (String)(getValueAt(row, col));

			if (type.equals(oldType)) {
				return;
			}

			gn.setType(type);

			if (type.equals("")) {
				if (row == 0) {
					setCellAttributes(row, col, true);
					emptyParmIDComboBoxes(0);
				}
				else {
					String aboveType = getNearestType(row);
					if (aboveType.equals("")) {
						// error!
						emptyParmIDComboBoxes(row);
					}
					else {
						setParmIDComboBoxes(row, 
							aboveType);
					}
				}
			}
			else {
				if (row == 0) {
					setCellAttributes(row, col, false);
				}
				String aboveType = getNearestType(row);
				if (!type.equals(aboveType)) {
					setCellAttributes(row, COL_PARM,true);
					setParmIDComboBoxes(row, type);
				}
				else {
					setCellAttributes(row,COL_PARM,false);
				}
			}

			break;
		case COL_PARM:	
			String dtype = ((String)value).trim();
			// FIXME SAM 2008-03-24 No need to do this with newer StateMod
			// since binary file uses underscores for data types.
			//dtype = dtype.replace(' ', '_');
			String oldDtype = (String)(getValueAt(row, col));
			if (dtype.equals(oldDtype)) {
				return;
			}
			gn.setDtype(dtype);
			if (row == 0) {
				if (dtype.trim().equals("")) {
					setCellAttributes(row, col, true);
				}
				else {
					setCellAttributes(row, col, false);
				}
			}
			else if (!dtype.trim().equals("")) {
				setCellAttributes(row, col, false);
			}
			break;
		case COL_YEAR:	
			String year = ((String)value).trim();
			String oldYear = (String)(getValueAt(row, col));
			if (year.equals(oldYear)) {
				return;
			}
			gn.setYrAve(year);
			if (row == 0) {
				if (year.trim().equals("")) {
					setCellAttributes(row, col, true);
				}
				else {
					setCellAttributes(row, col, false);
				}
			}
			break;
		case COL_ID:	
			String id = (String)value;
			String oldID = (String)(getValueAt(row, col));
			if (id.equals(oldID)) {
				return;
			}
			/*
			int index = id.indexOf("(");
			if (index > -1) {
				id = id.substring(0, index);
			}
			id = id.trim();
			*/
			gn.setID(id);
			if (id.trim().equals("")) {
				setCellAttributes(row, col, true);
			}
			else {
				setCellAttributes(row, col, false);
			}
			break;
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
