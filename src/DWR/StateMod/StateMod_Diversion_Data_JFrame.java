// PRINTING CHECKED
// ----------------------------------------------------------------------------
// StateMod_Diversion_Data_JFrame - This is a JFrame that displays Diversion
//	data in a tabular format.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 
// 2005-01-13	J. Thomas Sapienza, RTi	Initial version.
// 2005-01-20	JTS, RTi		Following review:
//					* Improved some loop performance.
//					* Removed getDataType().
//					* Title string is now passed to the
//					  super constructor.
//					* Editability of data in the worksheet
//					  is now passed in via the constructor.
// 2005-03-28	JTS, RTi		Adjusted GUI size.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a Vector of StateMod_Diversion data in
a worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateMod_Diversion_Data_JFrame 
extends StateMod_Data_JFrame {

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty, in 
which case an empty worksheet is shown.
@param titleString the String to display as the GUI title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateMod_Diversion_Data_JFrame(Vector data, String titleString,
boolean editable)
throws Exception {
	super(data, titleString, editable);
	setSize(800, getHeight());
	PRINT_PORTRAIT_LINES = -1;
	PRINT_LANDSCAPE_LINES = 70;	// bigger than letter, though
}

/**
Called when the Apply button is pressed. This commits any changes to the data
objects.
*/
protected void apply() {
	StateMod_Diversion div = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		div = (StateMod_Diversion)_data.elementAt(i);
		div.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the 
constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_Diversion_Data_TableModel tableModel 
		= new StateMod_Diversion_Data_TableModel(_data, _editable);
	StateMod_Diversion_Data_CellRenderer cellRenderer 
		= new StateMod_Diversion_Data_CellRenderer(tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to 
the data objects.
*/
protected void cancel() {
	StateMod_Diversion div = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		div = (StateMod_Diversion)_data.elementAt(i);
		div.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can 
later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_Diversion div = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		div = (StateMod_Diversion)_data.elementAt(i);
		div.createBackup();
	}
}

}
