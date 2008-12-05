// ----------------------------------------------------------------------------
// StateMod_RiverNetworkNode_Data_JFrame - This is a JFrame that displays 
//	RiverNetworkNode data in a tabular format.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 
// 2005-04-05	J. Thomas Sapienza, RTi	Initial version.
// 2007-01-07   Kurt Tometich, RTi
//								Changed the width from 386 to 720 to support
//								the new fields added.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a Vector of StateMod_RiverNetworkNode data
in a worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateMod_RiverNetworkNode_Data_JFrame 
extends StateMod_Data_JFrame {

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty, in
which case an empty worksheet is shown.
@param titleString the String to display as the GUI title.
@throws Exception if there is an error building the worksheet.
*/
public StateMod_RiverNetworkNode_Data_JFrame(List data, String titleString)
throws Exception {
	super(data, titleString, false);
	setSize(720, getHeight());
}

/**
Called when the Apply button is pressed. This commits any changes to the data
objects.
*/
protected void apply() {
	StateMod_RiverNetworkNode rnn = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		rnn = (StateMod_RiverNetworkNode)_data.get(i);
		rnn.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the 
constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_RiverNetworkNode_Data_TableModel tableModel 
		= new StateMod_RiverNetworkNode_Data_TableModel(_data);
	StateMod_RiverNetworkNode_Data_CellRenderer cellRenderer 
		= new StateMod_RiverNetworkNode_Data_CellRenderer(tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to 
the data objects.
*/
protected void cancel() {
	StateMod_RiverNetworkNode rnn = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		rnn = (StateMod_RiverNetworkNode)_data.get(i);
		rnn.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can 
later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_RiverNetworkNode rnn = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		rnn = (StateMod_RiverNetworkNode)_data.get(i);
		rnn.createBackup();
	}
}

}
