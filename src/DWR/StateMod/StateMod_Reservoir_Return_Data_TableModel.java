package DWR.StateMod;

import java.util.List;
import java.util.Vector;

//import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.Validator;

/**
This class displays reservoir station return assignment data.
*/
public class StateMod_Reservoir_Return_Data_TableModel 
extends JWorksheet_AbstractRowTableModel implements StateMod_Data_TableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 5;

/**
Whether the table data is editable or not.
*/
private boolean __editable = false;

/**
The worksheet that this model is displayed in.  
*/
//private JWorksheet __worksheet = null;

/**
A Vector that maps rows in the display when totals are NOT being shown to rows
in the overall data Vectors.  Used to make switching between displays with and
without totals relatively efficient.  See getValueAt() and setupData().
*/
//private List __rowMap = null;

/**
The List of data that will actually be shown in the table.
*/
//private List[] __data = null;

/**
References to columns.
*/
public final static int 
	COL_RESERVOIR_ID = 0,
	COL_RIVER_NODE_ID = 1,
	COL_PERCENT_RETURN = 2,
	COL_DELAY_TABLE_ID = 3,
	COL_COMMENT = 4;

/**
Constructor.  
@param data the data that will be displayed in the table.
@param editable whether the table data is editable or not
@throws Exception if an invalid data or dmi was passed in.
*/
public StateMod_Reservoir_Return_Data_TableModel ( List data, boolean editable )
throws Exception {
	if (data == null) {
		_data = new Vector();
	}
	else {
		_data = data;
	}
	_rows = data.size();

	__editable = editable;

	//setupData(data);
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_RESERVOIR_ID: return String.class;
		case COL_RIVER_NODE_ID: return String.class;
		case COL_PERCENT_RETURN: return Double.class;
		case COL_DELAY_TABLE_ID: return String.class;
		case COL_COMMENT: return String.class;
		default: return String.class;
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
		// TODO (SAM - 2005-01-20) how is this class being used with Well Depletion displays
		// in the StateMod GUI?  We might need a flag for the header.
		case COL_RESERVOIR_ID:		
			return "RESERVOIR\nID";
		case COL_RIVER_NODE_ID:	
			return "RIVER NODE\nID";
		case COL_PERCENT_RETURN:
			return "PERCENT TO\nRIVER NODE";
		case COL_DELAY_TABLE_ID:		
			return "DELAY\nTABLE ID";
		case COL_COMMENT:		
			return "\nCOMMENT";
		default:	
			return " ";
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
	widths[COL_RESERVOIR_ID] = 8;
	widths[COL_RIVER_NODE_ID] = 8;
	widths[COL_PERCENT_RETURN] = 8;
	widths[COL_DELAY_TABLE_ID] = 8;
	widths[COL_COMMENT] = 30;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_RESERVOIR_ID: return "%-12.12s";	
		case COL_RIVER_NODE_ID: return "%-12.12s";	
		case COL_PERCENT_RETURN: return "%12.2f";
		case COL_DELAY_TABLE_ID: return "%-12.12s";
		case COL_COMMENT: return "%-s";
		default: return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns general validators based on column of data being checked.
@param col Column of data to check.
@return List of validators for a column of data.
 */
public Validator[] getValidators( int col ) 
{
	// TODO KAT 2007-04-16
	// Need to add general validators but don't know
	// what data is going to be checked.
	Validator[] no_checks = new Validator[] {};
	return no_checks;
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

	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)_data.get(row);
	switch (col) {
		case COL_RESERVOIR_ID: return rf.getID();
		case COL_RIVER_NODE_ID: return rf.getCrtnid();
		case COL_PERCENT_RETURN: return rf.getPcttot();
		case COL_DELAY_TABLE_ID: return rf.getIrtndl();
		case COL_COMMENT: return rf.getComment();
		default: return "";
	}
	/*
	if (!__showTotals) {
		row = ((Integer)__rowMap.get(row)).intValue();
	}
	return __data[col].get(row);
	*/
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model all columns above #2 are editable.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!__editable) {
		return false;
	}
	if (columnIndex > 1) {
		return true;
	}
	return false;
}

/**
Sets up the data to be displayed in the table.
@param data a Vector of StateMod_DelayTable objects from which the data to b
be displayed in the table will be gathered.
*/
/*
private void setupData(List data) {
	int num = 0;
	int size = data.size();
	StateMod_DelayTable dt = null;
	String id = null;
	__data = new List[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__data[i] = new Vector();
	}

	__rowMap = new Vector();

	double total = 0;
	int rowCount = 0;
	for (int i = 0; i < size; i++) {
		total = 0;
		dt = (StateMod_DelayTable)data.get(i);
		id = dt.getID();
		num = dt.getNdly();
		for (int j = 0; j < num; j++) {
			__data[COL_PLAN_ID].add(id);
			__data[COL_RIVER_NODE_ID].add(new Integer(j + 1));
			__data[COL_PERCENT_RETURN].add( new Double(dt.getRet_val(j)));
			total += dt.getRet_val(j);
			__rowMap.add(new Integer(rowCount));
			rowCount++;
		}

		__data[COL_PLAN_ID].add("TOTAL " + id);
		__data[COL_RIVER_NODE_ID].add(new Integer(-999));
		__data[COL_PERCENT_RETURN].add(new Double(total));

		rowCount++;
	}
	_rows = rowCount;
}
*/

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	
	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)_data.get(row);
	
	switch (col) {
		case COL_RESERVOIR_ID:
			rf.setID((String)value);
			break;
		case COL_RIVER_NODE_ID:
			rf.setCrtnid((String)value);
			break;
		case COL_PERCENT_RETURN:
			rf.setPcttot((Double)value);
			break;
		case COL_DELAY_TABLE_ID:
			rf.setIrtndl((String)value);
			break;
		case COL_COMMENT:
			rf.setComment((String)value);
			break;
	}
	super.setValueAt(value, row, col);
}

/**
Sets whether to show lines with totals.  setJWorksheet() must have been called
with a non-null worksheet prior to this method.  The worksheet will be updated instantly.
@param showTotals whether to show lines with totals in the worksheet.
*/
/*
public void setShowTotals(boolean showTotals) {
	__showTotals = showTotals;
	_sortOrder = null;

	if (__showTotals) {
		_rows = __data[COL_PLAN_ID].size();
	}
	else {
		_rows = __rowMap.size();
	}
	__worksheet.refresh();
}
*/	

/**
Sets the worksheet that this model appears in.
@param worksheet the worksheet the model appears in.
*/
/*
public void setJWorksheet(JWorksheet worksheet) {
	__worksheet = _worksheet;
}
*/

}