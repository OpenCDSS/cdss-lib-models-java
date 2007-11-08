// ----------------------------------------------------------------------------
// StateCU_CropCharacteristics_Data_JFrame - This is a JFrame that displays 
//	CropCharacteristics data in a tabular format.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 
// 2005-01-17	J. Thomas Sapienza, RTi	Initial version.
// 2005-01-20	JTS, RTi		* Constructor now takes parameter to
//					  set the title.
//					* Constructor now takes parameter to
//					  set whether the data are editable or
//					  not.
//					* Removed getDataType().
// 2005-03-28	JTS, RTi		Adjusted GUI size.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.Vector;

import DWR.StateMod.StateMod_Data_JFrame;
import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a Vector of StateCU_CropCharacteristics 
data in a worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateCU_CropCharacteristics_Data_JFrame 
extends StateMod_Data_JFrame {

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty.
@param titleString the String to display as the GUI title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateCU_CropCharacteristics_Data_JFrame(Vector data, String titleString,
boolean editable)
throws Exception {
	super(data, titleString, editable);
	PRINT_PORTRAIT_LINES = -1;
	PRINT_LANDSCAPE_LINES = 55;
	setSize(750, getHeight());
}

/**
Called when the Apply button is pressed. This commits any changes to the data
objects.
*/
protected void apply() {
	StateCU_CropCharacteristics station = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		station = (StateCU_CropCharacteristics)_data.elementAt(i);
		station.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the 
constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateCU_CropCharacteristics_TableModel tableModel 
		= new StateCU_CropCharacteristics_TableModel(_data, _editable, 
		false);
	StateCU_CropCharacteristics_CellRenderer cellRenderer 
		= new StateCU_CropCharacteristics_CellRenderer(tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to 
the data objects.
*/
protected void cancel() {
	StateCU_CropCharacteristics station = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		station = (StateCU_CropCharacteristics)_data.elementAt(i);
		station.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can 
later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateCU_CropCharacteristics station = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		station = (StateCU_CropCharacteristics)_data.elementAt(i);
		station.createBackup();
	}
}

}
