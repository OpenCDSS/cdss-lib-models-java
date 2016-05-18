// PRINTING CHECKED
// ----------------------------------------------------------------------------
// StateMod_Well_Collection_Data_JFrame - This is a JFrame that 
//	displays reservoir collection data in a tabular format.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 
// 2005-04-07	J. Thomas Sapienza, RTi	Initial version.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a list of StateMod_Well 
collection data in a worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateMod_Well_Collection_Data_JFrame extends StateMod_Data_JFrame
{

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty.
@param titleString the String to display as the GUI title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateMod_Well_Collection_Data_JFrame(List data, String titleString, boolean editable)
throws Exception {
	super(data, titleString, editable);
	setSize(550, getHeight());
}

/**
Called when the Apply button is pressed. This commits any changes to the data objects.
*/
protected void apply() {
	StateMod_Well well = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		well = (StateMod_Well)_data.get(i);
		well.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_Well_Collection_Data_TableModel tableModel 
		= new StateMod_Well_Collection_Data_TableModel(_data, _editable);
	StateMod_Well_Collection_Data_CellRenderer cellRenderer 
		= new StateMod_Well_Collection_Data_CellRenderer( tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to the data objects.
*/
protected void cancel() {
	StateMod_Well well = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		well = (StateMod_Well)_data.get(i);
		well.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_Well well = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		well = (StateMod_Well)_data.get(i);
		well.createBackup();
	}
}

}