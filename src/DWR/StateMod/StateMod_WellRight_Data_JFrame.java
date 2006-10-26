// PRINTING CHECKED
// ----------------------------------------------------------------------------
// StateMod_WellRight_Data_JFrame - This is a JFrame that displays WellRight
//	data in a tabular format.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 
// 2005-01-17	J. Thomas Sapienza, RTi	Initial version.
// 2005-01-20	JTS, RTi		Following review:
//					* Improved some loop performance.
//					* Removed getDataType().
//					* Title string is now passed to the
//					  super constructor.
//					* Editability of data in the worksheet
//					  is now passed in via the constructor.
// 2005-03-28	JTS, RTi		Adjusted GUI size.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JScrollWorksheet;

import RTi.Util.Message.Message;

/**
This class is a JFrame for displaying a Vector of StateMod_WellRight data in
a worksheet.  Well rights for 1+ wells can be displayed in the same worksheet.
The worksheet data can be exported to a file or printed.
*/
public class StateMod_WellRight_Data_JFrame 
extends StateMod_Data_JFrame {

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty, in
which case an empty worksheet is shown.
@param titleString the String to display in the GUI title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateMod_WellRight_Data_JFrame(Vector data, String titleString,
boolean editable)
throws Exception {
	super(data, titleString, editable);
	setSize(748, getHeight());
}

/**
Called when the Apply button is pressed. This commits any changes to the data
objects.
*/
protected void apply() {
	StateMod_WellRight right = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		right = (StateMod_WellRight)_data.elementAt(i);
		right.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the 
constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_WellRight_Data_TableModel tableModel 
		= new StateMod_WellRight_Data_TableModel(_data, _editable);
		// false means to set up the table model to allow 1+ 
		// wells' rights to be in the same worksheet.
	StateMod_WellRight_Data_CellRenderer cellRenderer 
		= new StateMod_WellRight_Data_CellRenderer(tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to 
the data objects.
*/
protected void cancel() {
	StateMod_WellRight right = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		right = (StateMod_WellRight)_data.elementAt(i);
		right.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can 
later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_WellRight right = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		right = (StateMod_WellRight)_data.elementAt(i);
		right.createBackup();
	}
}

}
