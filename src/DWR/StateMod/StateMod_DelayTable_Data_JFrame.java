// StateMod_DelayTable_Data_JFrame - JFrame that displays DelayTable data in a tabular format.

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package DWR.StateMod;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;

/**
This class is a JFrame for displaying a list of StateMod_DelayTable data in a worksheet.
The worksheet data can be exported to a file or printed.
*/
@SuppressWarnings("serial")
public class StateMod_DelayTable_Data_JFrame 
extends StateMod_Data_JFrame<StateMod_DelayTable> {

/**
Whether this JFrame is displaying monthly or daily delay table data.
*/
private boolean __monthlyData = true;

/**
The checkbox for selecting whether to show rows with totals or not.
*/
private JCheckBox __checkBox = null;

/**
The table model for the worksheet in the GUI.
*/
private StateMod_DelayTable_Data_TableModel __tableModel = null;

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty, in which case an empty worksheet is shown.
@param titleString the String to display as the GUI title.
@param monthlyData if true, the delay tables are showing monthly data.  If false, they are showing daily data.
@param returnIsPercent whether the return amounts are in percents (true) or fractions (false).
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
@deprecated use the other one without returnIsPercent.
*/
@Deprecated
//public StateMod_DelayTable_Data_JFrame(List<StateMod_DelayTable> data, String titleString,
//boolean monthlyData, boolean returnIsPercent, boolean editable)
//throws Exception {
//	this(data, titleString, monthlyData, editable);
//}

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty, in which case an empty worksheet is shown.
@param titleString the String to display as the GUI title.
@param monthlyData if true, the delay tables are showing monthly data.  If false, they are showing daily data.
@param editable whether the data in the JFrame can be edited or not.  If true the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateMod_DelayTable_Data_JFrame(JFrame parent, List<StateMod_DelayTable> data, String titleString, boolean monthlyData, boolean editable)
throws Exception {
	super(parent, -1, -1, data, titleString, editable);
	__monthlyData = monthlyData;
	//initialize(data, titleString, editable);

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	JLabel label = new JLabel("Show totals: ");
	JGUIUtil.addComponent(panel, label,
		0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__checkBox = new JCheckBox((String)null, true);
	__checkBox.addActionListener(this);
	JGUIUtil.addComponent(panel, __checkBox,
		1, 0, 1, 1, 1, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add("North", panel);	
	pack();
	
	__tableModel = (StateMod_DelayTable_Data_TableModel)_worksheet.getTableModel();
	__tableModel.setJWorksheet(_worksheet);

	setSize(300, 600);
}

/**
Responds to action events.
@param event the ActionEvent that occurred.
*/
public void actionPerformed(ActionEvent event) {
	if (event.getSource() == __checkBox) {
		__tableModel.setShowTotals(__checkBox.isSelected());
	}
	else {
		super.actionPerformed(event);
	}
}

/**
Called when the Apply button is pressed. This commits any changes to the data objects.
*/
protected void apply() {
	StateMod_DelayTable delay = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		delay = _data.get(i);
		delay.createBackup();
	}
}

/**
Creates a JScrollWorksheet for the current data and returns it.
@return a JScrollWorksheet containing the data Vector passed in to the constructor.
*/
protected JScrollWorksheet buildJScrollWorksheet() 
throws Exception {
	StateMod_DelayTable_Data_TableModel tableModel 
		= new StateMod_DelayTable_Data_TableModel(_data, __monthlyData, _editable);
	StateMod_DelayTable_Data_CellRenderer cellRenderer 
		= new StateMod_DelayTable_Data_CellRenderer(tableModel);

	// Note (JTS - 2005-03-31)
	// While it would seem the right thing to do would be to here assign the table model to __tableModel,
	// JTS found that in practice for some reason once the constructor was returned to
	// (in order to finish building the GUI with checkbox) that the table model was then null).
	// JTS didn't understand why it was happening but didn't want to spend too long investigating.

	// _props is defined in the super class.
	return new JScrollWorksheet(cellRenderer, tableModel, _props);
}

/**
Called when the cancel button is pressed.  This discards any changes made to the data objects.
*/
protected void cancel() {
	StateMod_DelayTable delay = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		delay = _data.get(i);
		delay.restoreOriginal();
	}
}

/**
Creates backups of all the data objects in the Vector so that changes can later be cancelled if necessary.
*/
protected void createDataBackup() {
	StateMod_DelayTable delay = null;
	int size = _data.size();
	for (int i = 0; i < size; i++) {
		delay = _data.get(i);
		delay.createBackup();
	}
}

}