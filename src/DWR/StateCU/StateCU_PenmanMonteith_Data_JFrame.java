package DWR.StateCU;

import java.util.List;

import DWR.StateMod.StateMod_Data_JFrame;
import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a list of StateCU_PenmanMonteith data in
a worksheet.  The worksheet data can be exported to a file or printed.
*/
@SuppressWarnings("serial")
public class StateCU_PenmanMonteith_Data_JFrame 
extends StateMod_Data_JFrame {

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty.
@param titleString the String to display as the GUI title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateCU_PenmanMonteith_Data_JFrame(List<StateCU_PenmanMonteith> data, String titleString,
boolean editable)
throws Exception {
	super(data, titleString, editable);
	setSize(500, 600);
}

/**
Called when the Apply button is pressed. This commits any changes to the data objects.
*/
protected void apply() {
	StateCU_PenmanMonteith station = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		station = (StateCU_PenmanMonteith)_data.get(i);
		station.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateCU_PenmanMonteith_TableModel tableModel = new StateCU_PenmanMonteith_TableModel(_data, _editable);
	StateCU_PenmanMonteith_CellRenderer cellRenderer = new StateCU_PenmanMonteith_CellRenderer(tableModel);
	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to the data objects.
*/
protected void cancel() {
	StateCU_PenmanMonteith station = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		station = (StateCU_PenmanMonteith)_data.get(i);
		station.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateCU_PenmanMonteith station = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		station = (StateCU_PenmanMonteith)_data.get(i);
		station.createBackup();
	}
}

}