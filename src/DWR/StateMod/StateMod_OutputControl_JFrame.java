// StateMod_OutputControl_JFrame - dialog to create/edit the output control file

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

// REVISIT SAM 2006-03-04
// There is "graph" notation in this file - it needs to be removed since this
// class no longer works with graph products.

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a GUI for displaying and editing output control files.
*/
@SuppressWarnings("serial")
public class StateMod_OutputControl_JFrame extends JFrame
implements ActionListener, WindowListener {

/**
Button and JCheckbox labels.
*/
private final String 
	__BUTTON_ADD_ROW = 	"Add a Row (Append)",
	__BUTTON_DELETE_ROW = 	"Delete Selected Row(s)",
	__BUTTON_SAVE_FILE = 	"Save File",
	__BUTTON_LOAD_FILE = 	"Load File",
	__BUTTON_CLEAR_LIST = 	"Clear List",
	__BUTTON_HELP = 	"Help",
	__BUTTON_CLOSE = 	"Close",
	__CHECKBOX_USE_ALL = 	"Use all (itemized list ignored)";

/**
Whether the file has been saved since it has changed or not.
*/
private boolean __dirty = false;

/**
JCheckbox that specifies whether the output control file should be set up
so that all ids are used or not.
*/
private JCheckBox __useAllJCheckBox;

/**
JTextfields that build the status bar at the bottom of the gui.
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet in which the data is displayed.
*/
private JWorksheet __worksheet;

/**
GUI Buttons.
*/
private SimpleJButton 
	__addRowButton,
	__deleteRowButton,
	__clearWorksheetButton,
	__loadTemplateButton,
	__saveTemplateButton,
	__helpButton,
	__closeButton;

/**
Dataset containing the data for the gui.
*/
private StateMod_DataSet __dataset;

/**
The Table model for the worksheet.
*/
private StateMod_OutputControl_TableModel __tableModel;

/**
Specific dataset components that are kept handy.
*/
private DataSetComponent __riverNetworkComp = null;

// TODO SAM 2010-12-18 Needs an editable parameter like for other data files
/** 
Constructor for output control
@param dataset the dataset containing the data
*/
public StateMod_OutputControl_JFrame(StateMod_DataSet dataset) {
	StateMod_GUIUtil.setTitle(this, dataset, "Output Control", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;

	__riverNetworkComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RIVER_NETWORK);
	setupGUI();
}

/**
Responds to action performed events.
@param ae the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent ae) {
	String routine = "StateMod_OutputControl_JFrame.actionPerformed";
	String action = ae.getActionCommand();

	if (action.equals(__BUTTON_ADD_ROW)) {
		if (!__tableModel.canAddNewRow()) {
			return;
		}
		__dirty = true;
		int row = __worksheet.getRowCount();

		StateMod_GraphNode n = new StateMod_GraphNode();
		__worksheet.addRow(n);
		n.setType("");
		n.setID("");
		n.setSwitch(-1);
		__tableModel.setDirty(true);

		// when a row is added, mark the second and third columns
		// as uneditable.  They will be set to editable as soon as
		// the user enters a value in the first column
		__worksheet.setCellEditable(row, 1, false);
		__worksheet.setCellEditable(row, 2, false);
	}
	else if (action.equals(__BUTTON_CLEAR_LIST)) {
		__dirty = true;
		__worksheet.clear();
		setMessages("Add station to list.", "Ready");
		__tableModel.setDirty(true);
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		closeWindow();
	}
	else if (action.equals(__BUTTON_DELETE_ROW)) {
		int[] rows = __worksheet.getSelectedRows();

		int length = rows.length;
		__tableModel.setDirty(true);

		if (length == 0) {
			return;
		}
	
		for (int i = (length - 1); i >= 0; i--) {
			__worksheet.deleteRow(rows[i]);
		}
		__dirty = true;
		setMessages("Time series list has changed.", "Ready");
	}
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-07-09)
	}
	else if (action.equals(__BUTTON_SAVE_FILE)) {
		JGUIUtil.setWaitCursor(this, true);
		String lastDirectorySelected = 
			JGUIUtil.getLastFileDialogDirectory();
	
		JFileChooser fc = null;
		if (lastDirectorySelected != null) {
			fc = new JFileChooser(lastDirectorySelected);
		}
		else {
			fc = new JFileChooser();
		}

		fc.setDialogTitle("Select Output Control File");
		SimpleFileFilter out = new SimpleFileFilter("out",
			"StateMod Output Control Files");
		fc.addChoosableFileFilter(out);
		SimpleFileFilter tpl = new SimpleFileFilter("tpo",
			"StateMod Output Control Files");
		fc.addChoosableFileFilter(tpl);
		SimpleFileFilter xou = new SimpleFileFilter("xou",
			"StateMod Output Control Files");
		fc.addChoosableFileFilter(xou);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(tpl);
		fc.setDialogType(JFileChooser.SAVE_DIALOG);	

		JGUIUtil.setWaitCursor(this, false);

		int retVal = fc.showSaveDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
	
		String currDir = (fc.getCurrentDirectory()).toString();
	
		if (!currDir.equalsIgnoreCase(lastDirectorySelected)) {
			JGUIUtil.setLastFileDialogDirectory(currDir);
		}
		String filename = fc.getSelectedFile().getName();

		FileFilter ff = fc.getFileFilter();
		if ( ff == out ) {
			filename = IOUtil.enforceFileExtension(filename, "out");
		}
		else if ( ff == tpl ) {
			filename = IOUtil.enforceFileExtension(filename, "tpo");
		}
		else if ( ff == xou ) {
			filename = IOUtil.enforceFileExtension(filename, "xou");
		}
		
		__dirty = false;
		__tableModel.setDirty(false);
		
		@SuppressWarnings("unchecked")
		List<StateMod_GraphNode> theGraphNodes = (List<StateMod_GraphNode>)__worksheet.getAllData();	

		try {	
			StateMod_GraphNode.writeStateModOutputControlFile(null, 
				currDir + File.separator + filename, 
				theGraphNodes, null);
		} catch (Exception e) {
			Message.printWarning(1, routine,
				"Error saving output control file\n"
				+ "\"" + currDir + File.separator 
				+ filename + "\"",
				this);
			Message.printWarning(2, routine, e);
		}
	}
	else if (action.equals(__BUTTON_LOAD_FILE)) {
		JGUIUtil.setWaitCursor(this, true);
		String lastDirectorySelected = 
			JGUIUtil.getLastFileDialogDirectory();
	
		JFileChooser fc = null;
		if (lastDirectorySelected != null) {
			fc = new JFileChooser(lastDirectorySelected);
		}
		else {
			fc = new JFileChooser();
		}

		fc.setDialogTitle("Select Output Control File");
		SimpleFileFilter out = new SimpleFileFilter("out",
			"StateMod Output Control Files");
		fc.addChoosableFileFilter(out);
		SimpleFileFilter tpl = new SimpleFileFilter("tpo",
			"StateMod Output Control Files");
		fc.addChoosableFileFilter(tpl);
		SimpleFileFilter xou = new SimpleFileFilter("xou",
			"StateMod Output Control Files");
		fc.addChoosableFileFilter(xou);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(tpl);
		fc.setDialogType(JFileChooser.OPEN_DIALOG);	

		JGUIUtil.setWaitCursor(this, false);
		int retVal = fc.showOpenDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
	
		String currDir = (fc.getCurrentDirectory()).toString();
	
		if (!currDir.equalsIgnoreCase(lastDirectorySelected)) {
			JGUIUtil.setLastFileDialogDirectory(currDir);
		}
		String filename = fc.getSelectedFile().getName();

		JGUIUtil.setWaitCursor(this, true);

		__dirty = false;

		List<StateMod_GraphNode> theGraphNodes = new Vector<StateMod_GraphNode>(20, 1);
		
		try {	
			__worksheet.clear();
			__tableModel.setDirty(false);
			StateMod_GraphNode.readStateModOutputControlFile(
				theGraphNodes, 
				currDir + File.separator + filename);

			int size = theGraphNodes.size();
			StateMod_GraphNode g = null;
			int row = 0;
			for (int i = 0; i < size; i++) {
				row = __worksheet.getRowCount();
				g = (StateMod_GraphNode)theGraphNodes
					.get(i);
				if (i == 0) {
					if (g.getID().equals("All")) {
						__useAllJCheckBox.setSelected(
							true);
					}
					else {
						__useAllJCheckBox.setSelected(
							false);
					}
				}
				__worksheet.addRow(g);

				__tableModel.fillIDColumn(row, g.getType());
			}
			__worksheet.setData(theGraphNodes);
		} catch (Exception e) {
			Message.printWarning(1, routine,
				"Error loading output control file\n" +
				"\"" + currDir + File.separator + filename+"\"",
				this);
			Message.printWarning(2, routine, e);
		}
		JGUIUtil.setWaitCursor(this, false);		
	}
	else if (action.equals(__CHECKBOX_USE_ALL)) {
		// if the use all checkbox is selected, subtle changes from the
		// default functionality are made.  The buttons to add and
		// delete rows are disabled, and the ID of the only record
		// in the worksheet is set to "All".  This ID value is used
		// in the table model to determine when the checkbox is 
		// selected.  In addition, the ComboBox functionality of the
		// first and third data columns is turned off.

		if (__useAllJCheckBox.isSelected()) {
			__addRowButton.setEnabled(false);
			__deleteRowButton.setEnabled(false);
			__worksheet.setColumnJComboBoxValues(0, null);	
			__worksheet.setColumnJComboBoxValues(2, null);
			__worksheet.clear();
			StateMod_GraphNode g = new StateMod_GraphNode();
			g.setID("All");
			__worksheet.setCellEditable(0, 0, false);
			__worksheet.setCellEditable(0, 1, false);
			__worksheet.setCellEditable(0, 2, false);
			__worksheet.addRow(g);
		}
		else {
			__addRowButton.setEnabled(true);
			__deleteRowButton.setEnabled(true);
			List<String> v = StateMod_Util.arrayToList ( StateMod_GraphNode.node_types);
			v.add("Other");
			__worksheet.setColumnJComboBoxValues(0, v);
			__worksheet.clear();
			List<String> offOn = new Vector<String>(2);
			offOn.add("Off");
			offOn.add("On");
			__worksheet.setCellEditable(0, 0, true);
			__worksheet.setColumnJComboBoxValues(2, offOn);
		}
	}
}

/**
Closes the window, first checking to see if the user needs to save any changes
made to the output control file.
*/
protected void closeWindow() {
	if (__dirty || __tableModel.isDirty()) {
		int x = new ResponseJDialog(this, 
			"Save Output Control File",
			"You have not saved the output control file.\n\n" +
			"Continue without saving?",
			ResponseJDialog.YES | ResponseJDialog.NO).response();
		if (x == ResponseJDialog.NO) {
			setDefaultCloseOperation (
				WindowConstants.DO_NOTHING_ON_CLOSE);
			setVisible(true);

			return;
		}
	}

	setVisible(false);
	setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
	dispose();
}

/**
Set the messages that are visible in the bottom of the window.
@param message General message string.
@param status Status string(e.g., "Ready", "Wait".
*/
private void setMessages(String message, String status) {
	if (message != null) {
		__messageJTextField.setText(message);
	}
	if (status != null) {
		__statusJTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "setupGUI";

	addWindowListener(this);

	__addRowButton = new SimpleJButton(__BUTTON_ADD_ROW, this);
	__deleteRowButton = new SimpleJButton(__BUTTON_DELETE_ROW, this);
	__saveTemplateButton = new SimpleJButton(__BUTTON_SAVE_FILE, this);
	__clearWorksheetButton = new SimpleJButton(__BUTTON_CLEAR_LIST,
		this);
	__loadTemplateButton = new SimpleJButton(__BUTTON_LOAD_FILE, this);
	__helpButton = new SimpleJButton(__BUTTON_HELP, this);
	__helpButton.setEnabled(false);
	__closeButton = new SimpleJButton(__BUTTON_CLOSE, this);

	__useAllJCheckBox = new JCheckBox(__CHECKBOX_USE_ALL, false);
	__useAllJCheckBox.addActionListener(this);

	// Make a main panel to be the resizable body of the frame...

	JPanel main_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	main_JPanel.setLayout(gb);

	GridLayout gl = new GridLayout(2, 2, 2, 2);
	JPanel top_panel = new JPanel();
	top_panel.setLayout(gl);

	JPanel radio_panel = new JPanel();
	radio_panel.setLayout(gb);

	GridLayout gl2 = new GridLayout(1, 0, 2, 0);
	JPanel bottom_JPanel = new JPanel();
	bottom_JPanel.setLayout(gl2);

	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
	JPanel final_button_JPanel = new JPanel();
	final_button_JPanel.setLayout(fl);

	JPanel grid_JPanel = new JPanel();
	grid_JPanel.setLayout(gb);


	// add add a row, delete selected rows, clear spreadsheet,
	// select template, save template buttons
	top_panel.add(__addRowButton);
	top_panel.add(__deleteRowButton);
	top_panel.add(__clearWorksheetButton);
	top_panel.add(__loadTemplateButton);
	int y = 0;
	JGUIUtil.addComponent(main_JPanel, top_panel, 
		0, y, 10, 3,
		0, 0, 
		10, 10, 10, 10, 
		GridBagConstraints.NONE, GridBagConstraints.NORTH);

	y += 3;
	JGUIUtil.addComponent(main_JPanel, __useAllJCheckBox,
		0, ++y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	PropList p = new PropList("StateMod_OutputControl_JFrame.JWorksheet");
	p.add("JWorksheet.ShowRowHeader=true");
	p.add("JWorksheet.AllowCopy=true");	
	p.add("JWorksheet.ShowPopupMenu=true");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_RiverNetworkNode> rinList = (List<StateMod_RiverNetworkNode>)__riverNetworkComp.getData();
		__tableModel = new StateMod_OutputControl_TableModel(this, new Vector<StateMod_GraphNode>(), rinList);
			
		StateMod_OutputControl_CellRenderer cro = new
			StateMod_OutputControl_CellRenderer(__tableModel);
	
		jsw = new JScrollWorksheet(cro, __tableModel, p);		
		__worksheet = jsw.getJWorksheet();

		List<String> v = StateMod_Util.arrayToList(StateMod_GraphNode.node_types);
		v.add("Other");
		__worksheet.setColumnJComboBoxValues(0, v);

		__worksheet.setCellSpecificJComboBoxColumn(1, false);

		List<String> offOn = new Vector<String>(2);
		offOn.add("Off");
		offOn.add("On");
		__worksheet.setColumnJComboBoxValues(2, offOn);

		__tableModel.setWorksheet(__worksheet);

		widths = cro.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(this);

	JGUIUtil.addComponent(grid_JPanel, jsw,
		0, 0, 1, 1, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	JGUIUtil.addComponent(main_JPanel, grid_JPanel, 
		0, ++y, 10, 12, 
		1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	y += 11;	// To account for grid height

	// Add bottom buttons - these are alphabetical so be
	//	careful if you add more ...

	final_button_JPanel.add(__closeButton);
	// REVISIT SAM 2006-03-04
	// Help is not currently enabled
	//final_button_JPanel.add(__helpButton);
	final_button_JPanel.add(__saveTemplateButton);

	// Add the final buttons on the bottom to the bottom panel...
	bottom_JPanel.add(final_button_JPanel);
	// Add the button panel to the frame...
	JGUIUtil.addComponent(main_JPanel, bottom_JPanel, 
		//0, gbc.RELATIVE, 10, 1, 
		0, ++y, 10, 1, 
		0, 0, 
		GridBagConstraints.VERTICAL, GridBagConstraints.SOUTH);

	// Add the main panel as the resizable content...

	getContentPane().add("Center", main_JPanel);

	// Add JTextFields for messages...
	JPanel message_JPanel = new JPanel();
	message_JPanel.setLayout(gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	__statusJTextField = new JTextField("             ");
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(message_JPanel, __messageJTextField, 
		0, 0, 9, 1, 
		1, 0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(message_JPanel, __statusJTextField, 
		9, 0, 1, 1, 
		0, 0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH);
	getContentPane().add("South", message_JPanel);

	pack();
	setSize(670, 500);
	JGUIUtil.center(this);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
}

/**
Responds to Window Activated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowActivated(WindowEvent e) {}

/**
Responds to Window closed events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowClosed(WindowEvent e) {}

/**
Responds to Window closing events; closes the window and marks it closed
in StateMod_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	closeWindow();
}

/**
Responds to Window deactivated events; saves the current information.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to Window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to Window iconified events; saves the current information.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {}

/**
Responds to Window opened events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpened(WindowEvent e) {}

/**
Responds to Window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e) {}

}
