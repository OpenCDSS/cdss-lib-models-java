package DWR.StateMod;

import java.util.List;

import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a list of plan station return data in
a worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateMod_Plan_Return_Data_JFrame 
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
public StateMod_Plan_Return_Data_JFrame(List data, String titleString, boolean editable)
throws Exception {
	super();
	initialize(data, titleString, editable);
	setSize(650, 600);
}

/**
Called when the Apply button is pressed. This commits any changes to the data objects.
*/
protected void apply() {
	StateMod_ReturnFlow rf = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		rf = (StateMod_ReturnFlow)_data.get(i);
		rf.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_Plan_Return_Data_TableModel tableModel 
		= new StateMod_Plan_Return_Data_TableModel(_data, _editable);
	StateMod_Plan_Return_Data_CellRenderer cellRenderer 
		= new StateMod_Plan_Return_Data_CellRenderer(tableModel);

	// Note (JTS - 2005-03-31)
	// while it would seem the right thing to do would be to here assign
	// the table model to __tableModel, JTS found that in practice for
	// some reason once the constructor was returned to (in order to finish
	// building the GUI with checkbox) that the table model was then 
	// null!  JTS didn't understand why it was happening but didn't
	// want to spend too long investigating.

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to the data objects.
*/
protected void cancel() {
	StateMod_ReturnFlow rf = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		rf = (StateMod_ReturnFlow)_data.get(i);
		rf.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can later be canceled if necessary.
*/
protected void createDataBackup() {
	StateMod_ReturnFlow rf = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		rf = (StateMod_ReturnFlow)_data.get(i);
		rf.createBackup();
	}
}

}