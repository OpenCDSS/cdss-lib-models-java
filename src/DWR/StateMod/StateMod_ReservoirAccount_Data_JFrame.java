// StateMod_ReservoirAccount_Data_JFrame - JFrame that displays ReservoirAccount data in a tabular format.

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Models Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Models Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

// PRINTING CHECKED
// ----------------------------------------------------------------------------
// StateMod_ReservoirAccount_Data_JFrame - This is a JFrame that displays 
// 	ReservoirAccount data in a tabular format.
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
This class is a JFrame for displaying a Vector of StateMod_ReservoirAccount 
data in a worksheet.  Accounts for 1+ reservoirs can be displayed in the same
worksheet.  The worksheet data can be exported to a file or printed.
*/
public class StateMod_ReservoirAccount_Data_JFrame 
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
public StateMod_ReservoirAccount_Data_JFrame(List data, String titleString, boolean editable)
throws Exception {
	super();
	
	int j = 0;
	int size = 0;
	int size2 = 0;
	StateMod_Reservoir r = null;
	StateMod_ReservoirAccount a = null;
	List accounts = null;
	List v = new Vector();
	
	if (data != null) {
		size = data.size();
	}
	
	for (int i = 0; i < size; i++) {
		r = (StateMod_Reservoir)data.get(i);
		accounts = r.getAccounts();
		if (accounts == null) {
			continue;
		}
	
		size2 = accounts.size();

		for (j = 0; j < size2; j++) {
			a = (StateMod_ReservoirAccount)accounts.get(j);
			a.setCgoto(r.getID());
		   	v.add(a);
		}
	}
		
	initialize(v, titleString, editable);
	setSize(691, getHeight());
}

/**
Called when the Apply button is pressed. This commits any changes to the data objects.
*/
protected void apply() {
	StateMod_ReservoirAccount acct = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		acct = (StateMod_ReservoirAccount)_data.get(i);
		acct.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_ReservoirAccount_Data_TableModel tableModel 
		= new StateMod_ReservoirAccount_Data_TableModel(_data, _editable);
	StateMod_ReservoirAccount_Data_CellRenderer cellRenderer 
		= new StateMod_ReservoirAccount_Data_CellRenderer(tableModel);

	// _props is defined in the super class
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to the data objects.
*/
protected void cancel() {
	StateMod_ReservoirAccount acct = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		acct = (StateMod_ReservoirAccount)_data.get(i);
		acct.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_ReservoirAccount acct = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		acct = (StateMod_ReservoirAccount)_data.get(i);
		acct.createBackup();
	}
}

}
