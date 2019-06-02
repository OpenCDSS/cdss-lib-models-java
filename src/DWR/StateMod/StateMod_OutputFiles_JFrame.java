// StateMod_OutputFiles_JFrame - dialog to edit/view output files within the current basin directory

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

//------------------------------------------------------------------------------
// StateMod_OutputFiles_JFrame - dialog to edit/view output files within the 
//	current basin directory
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 21 Jan 1998	Catherine E. 		Created initial version of class
//		Nutting-Lane, RTi	
// 17 May 1999	CEN, RTi		Added path to desired edit file
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 2003-06-26	SAM, RTi		* Add the following output files:
//					  Simulate: xop, xss
//					  Data Check:  xcw, xwr, xou
//					* Change the editSelectedFile() method
//					  to use a thread for the editor so it
//					  does not freeze up the application.
// 2003-07-10	SAM, RTi		* Add several output files to be
//					  consistent with recent feedback from
//					  Ray Bennett.
//					* Alphabetize within each group.
//					* Remove .xbe, as per Ray Bennett.
//					* Move .xdd, .xre, .xop, .xir, .xwe
//					  under report, not simulate, as per
//					  Ray Bennett.
//------------------------------------------------------------------------------
// 2003-08-20	J. Thomas Sapienza, RTi	Initial swing version.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-26	SAM, RTi		* Make the list single selection.
//					* Comment out the help button - use
//					  some tool tips instead.
// 2006-08-16	SAM, RTi		* Add *xpl for plan output.
//					* Add *chk for check file.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is a GUI for displaying the output files that have been created
and also viewing/editing them.
*/
@SuppressWarnings("serial")
public class StateMod_OutputFiles_JFrame extends JFrame
implements ActionListener, ListSelectionListener, MouseListener, WindowListener{

/**
Button labels.
*/
private final String
	__BUTTON_CLOSE = "Close",
	__BUTTON_HELP = "Help",
	__BUTTON_VIEW_EDIT = "View/Edit";

/**
GUI buttons.
*/
private JButton 
	__closeJButton,
	__viewEditJButton;

/**
The list in which the output files are displayed.
*/
private JList<String> __fileJList;

/**
The data set from which the data is read.
*/
private StateMod_DataSet __dataset;

/**
The name of the basin for which the output files were generated.
*/
private String __basinName;

/**
The path to the data files.
*/
private String __path;

/**
Constructor.
@param dataset the dataset containing the data.
*/
public StateMod_OutputFiles_JFrame(StateMod_DataSet dataset) {
	StateMod_GUIUtil.setTitle(this, dataset, "View/Edit Ouput Files", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;

	__path = __dataset.getComponentDataFilePath(StateMod_DataSet.COMP_RESPONSE);
	__basinName = __dataset.getBaseName();
	
	int index = __path.lastIndexOf(File.separator);
	if (index > -1) {
		__path = __path.substring(0, index + 1);
	}

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_VIEW_EDIT)) {
		editSelectedFile();
	}
	else if (e.getSource()== __fileJList) {
		editSelectedFile();
	}
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-08-20)
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		closeWindow();
	}
}

/**
Closes the window.
*/
protected void closeWindow() {
	setVisible(false);
	dispose();
}

/**
Edits the selected file.
*/
private void editSelectedFile() {
	if (__fileJList.getSelectedIndex() >= 2) {
		StringTokenizer split = new StringTokenizer((String)
			__fileJList.getSelectedValue());
		if (split != null) {
			String filename = __path + split.nextToken();
			try {	StateMod_GUIUtil.editFile(filename);
			}
			catch (Exception e2) {
				Message.printWarning(1, 
					"StateMod_OutputFiles_JFrame."
					+ "editSelectedFile",
					"Unable to view/edit file \"" 
					+ filename + "\"" );
			}		
		}
	}
}

/**
Fils the file list contents.
*/
public void fillFileJListContents(){
	String iline, format = "%-16.16s%-12.12s%-48s",
		rtn = "fillFileListContents()";
	String filename;
	String[] filelist = {
		__basinName + ".xbg", "Base Flow",
			"Gaged base flow estimates",
		__basinName + ".xbi", "Base Flow",
			"Base flow information at stream gage locations",
		__basinName + ".xbm", "Base Flow",
			"Base flow estimates formatted for model input",

		__basinName + ".xri", "Simulate",
			"River (stream) gage data",
		__basinName + ".xss", "Simulate",
			"Structure Summary (if variable efficiency is used)",

		__basinName + ".xbn", "Report",
			"ASCII listing of Binary Direct and Instream Data",
		__basinName + ".xcu", "Report",
			"Simulated Diversions and Consumptive Use Summary",
		__basinName + ".xdc", "Report",
			"Diversion comparison file",
		__basinName + ".xdd", "Report",
			"Direct and instream diversion monthly " + 
			"data (very large)",
		__basinName + ".xdg", "Report",
			"Direct and instream diversions and gage graph files",
		__basinName + ".xdy", "Report",
			"Direct and instream diversion daily data (very large)",
		__basinName + ".xgw", "Report",
			"Ground Water balance",
		__basinName + ".xir", "Report",
			"Instream reach summary",
		__basinName + ".xnm", "Report",
			"Detailed node accounting for all years",
		__basinName + ".xna", "Report",
			"Detailed node accounting average",
		__basinName + ".xop", "Report",
			"Operation Right Summary",
		__basinName + ".xpl", "Report",
			"Plan output file.",
		__basinName + ".xrc", "Report",
			"Reservoir comparison file",
		__basinName + ".xre", "Report",
			"Reservoir monthly data (total and by account)",
		__basinName + ".xrg", "Report",
			"Reservoir graph file",
		__basinName + ".xrx", "Report",
			"River data summary",
		__basinName + ".xry", "Report",
			"Reservoir daily data (total and by account)",
		__basinName + ".xsc", "Report",
			"Stream flow gage comparison file",
		__basinName + ".xsh", "Report",
			"Diversion shortage",
		__basinName + ".xsp", "Report",
			"Selected Parameter printout",
		__basinName + ".xsu", "Report",
			"Water supply summary",
		__basinName + ".xtb", "Report",
			"Summary table",
		__basinName + ".xwb", "Report",
			"Water balance",
		__basinName + ".xwc", "Report",
			"Well comparison file",
		__basinName + ".xwd", "Report",
			"Consumptive Use by Water District",
		__basinName + ".xwe", "Report",
			"Monthly water use and source for each structure " +
			"with well",
		__basinName + ".xwg", "Report",
			"Well graph file",
		__basinName + ".xwr", "Report",
			"Water right list sorted by basin rank",
		__basinName + ".xwy", "Report",
			"Daily water use and source for each structure with " +
			"well",

		__basinName + ".xcb", "Data Check",
			"Base flow by river ID",
		__basinName + ".xcd", "Data Check",
			"Direct demand by river ID",
		__basinName + ".xci", "Data Check",
			"Instream demand by river ID",
		__basinName + ".xcw", "Data Check",
			"Well demand by river ID",
		__basinName + ".xwr", "Data Check",
			"Same as *.xwr from the report option",
		__basinName + ".xou", "Data Check",
			"List of ID's for specific ID data requests",

		"delplt.xgr", "Delplt",
			"Scenario comparison",

		__basinName + ".chk", "Check File",
			"Check file from StateMod run (summary of run).",
		__basinName + ".log", "Log File",
		"Log file from StateMod run (step by step history of run).",
		"smgui.log", "Log File",
			"Log file from StateModGUI run.",
		"delplt.log", "Log File",
			"Log file from Delplt run."
	};
	int i = 0;
	int length = filelist.length;

	List<String> overall = new Vector<String>();

	List<String> v1 = new Vector<String>(3);
	v1.add("File");
	v1.add("Contents");
	v1.add("Description");
	iline = StringUtil.formatString(v1, format);
	overall.add(iline);
	List<String> v2 = new Vector<String>(3);
	v2.add("----");
	v2.add("--------");
	v2.add("-----------");
	iline = StringUtil.formatString(v2, format);
	overall.add(iline);

	if (Message.isDebugOn) {
		Message.printDebug(1, rtn, __path);
	}
	while (i<length) {
		filename = __path + filelist[i];
		if (IOUtil.fileReadable(filename)) {
			Message.printStatus(10, rtn, "Adding " + filename);
			List<String> v = new Vector<String>(3);
			v.add(filelist[i++]);
			v.add(filelist[i++]);
			v.add(filelist[i++]);
			iline = StringUtil.formatString(v, format);
			overall.add(iline);
		}
		else {	
			if (Message.isDebugOn) {
				Message.printDebug(10, rtn, "NOT adding " + filename);
			}
			i += 3;
		}
	}
	__fileJList.setListData(new Vector<String>(overall));
}

/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {
	if  (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
		JGUIUtil.setWaitCursor(this, true);
		editSelectedFile();
		JGUIUtil.setWaitCursor(this, false);
	}
}
/**
Responds to mouse entered events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent e) {}
/**
Responds to mouse exited events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseExited(MouseEvent e) {}

/**
Responds to mouse pressed events; does nothing.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {}

/**
Responds to mouse released events; calls 'processTableSelection' with the 
newly-selected index in the table.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {}

/**
Sets up the GUI.
*/
private void setupGUI() {

	__fileJList = new JList<String>();
	__fileJList.setSelectionMode ( ListSelectionModel.SINGLE_SELECTION );
	__fileJList.addListSelectionListener(this);
	__fileJList.setToolTipText(
		"<HTML>To view an output file, select a file and press " +
		"View/Edit.<br>Notepad will be used to view the file.</HTML>" );

	// REVISIT - SAM 2003-10-26 if a better help system is enabled.
	//__helpJButton = new JButton(__BUTTON_HELP);
	//__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__viewEditJButton = new JButton(__BUTTON_VIEW_EDIT);
	__viewEditJButton.setToolTipText(
		"<HTML>Use Notepad to view/edit the selected file.</HTML>" );

	GridBagLayout gb = new GridBagLayout();
	JPanel mainPanel = new JPanel();	
	mainPanel.setLayout(gb);

	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
	JPanel final_panel = new JPanel();
	final_panel.setLayout(fl);

	Font orig = __fileJList.getFont();
	Font fixed = null;
	if (orig != null) {
		fixed = new Font("Courier", orig.getStyle(), orig.getSize());
	}
	else {
		fixed = new Font("Courier", Font.PLAIN, 11);
	}
	__fileJList.setFont(fixed);

	__fileJList.addMouseListener(this);

	JGUIUtil.addComponent(mainPanel, new JScrollPane(__fileJList), 
		0, 0, 10, 12, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	// add contents to the list box
	fillFileJListContents();

	final_panel.add(__viewEditJButton);
	__viewEditJButton.addActionListener(this);
	__viewEditJButton.setEnabled(false);	// Change state when something
						// is selected.
	//final_panel.add(__helpJButton);
	//__helpJButton.addActionListener(this);
	final_panel.add(__closeJButton);
	__closeJButton.addActionListener(this);

	JGUIUtil.addComponent(mainPanel, final_panel, 
		0, 12, 10, 5, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.SOUTH);

	getContentPane().add("Center", mainPanel);

	pack();
	setSize(700, 300);
	JGUIUtil.center(this);
	setVisible(true);
}

/**
Responds to list selections.
@param e the ListSelectionEvent that happened.
*/
public void valueChanged(ListSelectionEvent e) {
	int index = __fileJList.getSelectedIndex();
	if (index == 0 || index == 1) {
		__fileJList.removeSelectionInterval(index, index);
		return;	// Don't want the following to somehow get confused...
	}
	// If anything is selected in the list, enable the View/Edit button...
	if (	(__fileJList.getSelectedIndices() != null) &&
		(__fileJList.getSelectedIndices().length > 0) ) {
		__viewEditJButton.setEnabled ( true );
	}
	else {	__viewEditJButton.setEnabled ( false );
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
Responds to Window closing events; closes the window.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	closeWindow();
}

/**
Responds to Window deactivated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to Window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to Window iconified events; does nothing.
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
