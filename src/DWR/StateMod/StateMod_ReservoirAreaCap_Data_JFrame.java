// PRINTING CHECKED
// ----------------------------------------------------------------------------
// StateMod_ReservoirAreaCap_Data_JFrame - This is a JFrame that displays 
// 	ReservoirAreaCap data in a tabular format.
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
// 2005-03-30	JTS, RTi		Converted constructor to expect a 
//					Vector of StateMod_Reservoir objects.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a Vector of StateMod_ReservoirAreaCap 
data in a worksheet.  Area capacities for 1+ reservoirs can be displayed in the
same worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateMod_ReservoirAreaCap_Data_JFrame 
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
public StateMod_ReservoirAreaCap_Data_JFrame(List data, String titleString, boolean editable)
throws Exception {
	super();
	
	int j = 0;
	int size = 0;
	int size2 = 0;
	StateMod_Reservoir r = null;
	StateMod_ReservoirAreaCap a = null;
	List areacaps = null;
	List v = new Vector();
	
	if (data != null) {
		size = data.size();
	}
	
	for (int i = 0; i < size; i++) {
		r = (StateMod_Reservoir)data.get(i);
		areacaps = r.getAreaCaps();
		if (areacaps == null) {
			continue;
		}
	
		size2 = areacaps.size();

		for (j = 0; j < size2; j++) {
			a = (StateMod_ReservoirAreaCap)areacaps.get(j);
			a.setCgoto(r.getID());
		    v.add(a);
		}
	}	
	
	initialize(v, titleString, editable);
	setSize(400, getHeight());
}

/**
Called when the Apply button is pressed. This commits any changes to the data objects.
*/
protected void apply() {
	StateMod_ReservoirAreaCap ac = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		ac = (StateMod_ReservoirAreaCap)_data.get(i);
		ac.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_ReservoirAreaCap_Data_TableModel tableModel 
		= new StateMod_ReservoirAreaCap_Data_TableModel(_data, _editable);
		// false means to set up the table model to allow many 
		// reservoirs' area capacities to be in the same table.
	StateMod_ReservoirAreaCap_Data_CellRenderer cellRenderer 
		= new StateMod_ReservoirAreaCap_Data_CellRenderer(tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to the data objects.
*/
protected void cancel() {
	StateMod_ReservoirAreaCap ac = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		ac = (StateMod_ReservoirAreaCap)_data.get(i);
		ac.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_ReservoirAreaCap ac = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		ac = (StateMod_ReservoirAreaCap)_data.get(i);
		ac.createBackup();
	}
}

}