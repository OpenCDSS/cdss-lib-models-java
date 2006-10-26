// ----------------------------------------------------------------------------
// StateMod_Response_TableModel - table model for displaying the response
//	table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-09-10	J. Thomas Sapienza, RTi	Initial version.
// 2003-09-15	JTS, RTi		Group name is now displayed along with
//					component name in the table model.
// 2003-09-18 	JTS, RTi		* Directory information is no longer
//					  shown in the first row of the model
//					* Only visible components are shown.
// 2003-10-13 	Steven A. Malers, RTi	* Modify headers to be more appropriate.
//					* Don't show the component number -
//					  the table model now has two columns.
// 2004-01-21	JTS, RTi		Removed the row count column and 
//					changed all the other column numbers.
// 2004-06-22	JTS, RTi		Added new column to show dirty status
//					of all components.
// 2004-08-25	JTS, RTi		* Split the first column into two:
//					  data set group and data set component.
//					* Modified column now only marks if
//					  something has changed.
//					* Browsed-for filenames are converted 
//					  to relative paths of the main 
//					  directory.
// 2004-10-28	SAM, RTi		Change setValueAt() to support sort.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This table model displays response data.
*/
public class StateMod_Response_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private int __COLUMNS = 4;

/**
References to columns.
*/
public static final int
	COL_GROUP = 0,	// Component group
	COL_COMP = 1,	// Component name
	COL_NAME = 2,	// File name for component
	COL_DIRTY = 3;	// Whether the component is dirty

/**
The data behind the table.  This array contains the int value of the
data set components's type for each row.
*/
private int[] __data;

/**
The worksheet on which this table model is being used.
*/
private JWorksheet __worksheet;

/**
The dataset for which to display data set component information.
*/
private StateMod_DataSet __dataset;

/**
Constructor.  
@param dataset the dataset for which to display data set component information.
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Response_TableModel(StateMod_DataSet dataset)
throws Exception {
	__dataset = dataset;
	// get the array of the ints that refer to the groups in
	// the data set.
	int[] groups = __dataset.getComponentGroupNumbers();

	Vector ints = new Vector();
	DataSetComponent dsc = null;
	Vector v = null;

	// Go through each of the groups and get their data out.  Group data
	// consists of the DataSetComponents the group contains.  For each
	// of the group's DataSetComponents, if it has data, then add its
	// component type to the accumulation vector.
	for (int i = 0; i < groups.length; i++) {
		dsc = __dataset.getComponentForComponentType(groups[i]);
		v = (Vector)dsc.getData();
		if (v == null) {
			v = new Vector();
		}
		for (int j = 0; j < v.size(); j++) {
			dsc = (DataSetComponent)v.elementAt(j);
			// the following makes sure that the response file 
			// is not added here ... the response file is added
			// below because it must always be in the GUI.
			if (dsc.getComponentType() != __dataset.COMP_RESPONSE 
				&& dsc.isVisible()) {
				ints.add(new Integer(dsc.getComponentType()));
			}
		}
	}
	
	// now transfer the numbers of the DataSetComponents with data into
	// an int array from the Vector.
	__data = new int[ints.size() + 1];
	__data[0] = __dataset.COMP_RESPONSE;
	for (int i = 0; i < ints.size(); i++) {
		__data[i + 1] = ((Integer)ints.elementAt(i)).intValue();
	}
	
	_rows = __data.length;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	return String.class;
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
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case  COL_GROUP:return "\nDATA GROUP";
		case  COL_COMP: return "\nDATA SET COMPONENT";
		case  COL_NAME:	return "\nFILE NAME";
		case  COL_DIRTY:return "ARE DATA\nMODIFIED?";
		default:	return " ";
	}	
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[0] = "<html>Data Group<BR>" +
		"Visible components correspond to current control " +
		"settings.</html>";
	tips[0] = "<html>Data Set Component<BR>" +
		"Visible components correspond to current control " +
		"settings.</html>";
	tips[2] = "<html>The file name is relative to the data set " +
		"directory, or is an absolute path.<BR>" +
		"Specify a blank string if the file name is unknown.</html>";
	tips[3] = "<html>The data modified flag tells whether the component "
		+ "has been modified or not.</html>";

	return tips;
}

public String getComponentName(int row) {
	return __dataset.getComponentForComponentType(__data[row])
		.getComponentName();
}

public int getComponentTypeForRow(int row) {
	return __data[row];
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
		case COL_GROUP:	return "%-40s";
		case COL_COMP:	return "%-40s";
		case COL_NAME:	return "%-40s";
		case COL_DIRTY: return "%-40s";
		default:	return "%-8s";
	}
}

/**
From AbstractTableModel; returns the number of rows of data in the table.
@return the number of rows of data in the table.
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
	// make sure the row numbers are never sorted ...
	
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	switch (col) {
		case  COL_GROUP:
			return __dataset.getComponentForComponentType(
				__dataset.lookupComponentGroupTypeForComponent(
				__data[row])).getComponentName();
		case  COL_COMP:
			return __dataset.getComponentForComponentType(
				__data[row]).getComponentName();
		case  COL_NAME:
			return __dataset.getComponentForComponentType(
				__data[row]).getDataFileName();
		case  COL_DIRTY:
			if (__dataset.getComponentForComponentType(
				__data[row]).isDirty()) {
				return "YES";
			}
			else {
				return "";
			}
			/*
			return "" + __dataset.getComponentForComponentType(
				__data[row]).isDirty();
//				+ " (" + __data[row] + ")";
			*/
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
	widths[COL_GROUP] = 16;
	widths[COL_COMP] = 28;
	widths[COL_NAME] = 18;
	widths[COL_DIRTY] = 8;
	return widths;
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model, column [1] is editable.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (columnIndex == 2) {
		return true;
	}
	return false;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col)
{	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	double dval;
	int ival;
	switch (col) {
		case COL_NAME:	// File name...
			String s = ((String)value).trim();
			String dir = __dataset.getDataSetDirectory();
			try {
				s = IOUtil.toRelativePath(dir, s);
			}
			catch (Exception e) {
				String routine = "StateMod_Response_TableModel"
					+ ".setValue";
				Message.printWarning(2, routine,
					"Error converting to relative path ("
					+ dir + ", " + s + "):");
				Message.printWarning(2, routine, e);
			} 

			if (s.equals(((String)getValueAt(row, col)).trim())) {
				return;
			}

			__dataset.getComponentForComponentType(
				__data[row]).setDataFileName(s);
			__dataset.getComponentForComponentType(
				__data[row]).setDirty(true);
		default:	break;
	}	
	
	super.setValueAt(value, row, col);
}

/**
Sets the worksheet on which this table model is being used.
@param worksheet the worksheet on which this table model can be used.
*/
public void setWorksheet(JWorksheet worksheet) {
	__worksheet = worksheet;
}
}
