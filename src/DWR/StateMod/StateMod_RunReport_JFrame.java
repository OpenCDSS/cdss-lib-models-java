//------------------------------------------------------------------------------
// StateMod_RunReport_JFrame - dialog to run report options of StateMod
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 21 Oct 1997	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 25 Feb 1998	CEN, RTi		Added help button.
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 13 Aug 2001	SAM, RTi		Handle exception when running StateMod.
//					Actually - this code is no longer used
//					becase ProcessManagerGUI is used.  Just
//					remove this file from the makefile and
//					remove the class file.
// 2002-09-09	SAM, RTi		Actually, this class is used in the main
//					GUI.  Not sure where the above comment
//					came from.
// 2003-01-01	SAM, RTi		Change the "Close" button to "Cancel".
// 2003-07-10	SAM, RTi		* Change -xn to -xnm.
//					* Add output file extensions to
//					  descriptions.
//					* Add -xwg.
//					* Add -xsp.
//					* Alphabetize report options.
//					* Add -xwc.
//					* Add -xdy, -xry, -xwy.
//					* Change title.
//					* Change the -xdg, -xrg, -xwg text
//					  fields to choices.
//------------------------------------------------------------------------------
// 2003-08-21	J. Thomas Sapienza, RTi	Initial swing version.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-09-15	JTS, RTi		Converted to use a tabbed pane to
//					display all the run options.
// 2003-09-16	JTS, RTi		Added mouse listeners to all JLabels
//					and SimpleJComboBoxes so that 
//					clicking on them selects their 
//					associated JRadioButton.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-24	SAM, RTi		* Add stream gages for -xdg.  Put the
//					  node type in the label.  Use
//					  StateMod_Util to generate the lists.
//					* Enable running StateMod to generate
//					  the reports.
//					* Comment out the help button - will add
//					  tool tips if necessary.
//					* Change that a radio button group is
//					  used for each panel and then when
//					  "Run StateMod Report" is pressed, use
//					  the selected item from the visible
//					  panel.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

// REVISIT:
//
// * Improve layout to not waste so much space
// * Perhaps provide separate lists for -xdg since the data come from
//   streamflow, diversions, and instream flow

package DWR.StateMod;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is a gui for choosing the options for statemod for running certain 
reports.
*/
public class StateMod_RunReport_JFrame extends JFrame
implements ActionListener, MouseListener, WindowListener {

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_HELP = "Help",
	__BUTTON_RUN_REPORT = "Run StateMod Report";

/**
The button groups for each panel.  Makes sure that only
one radio button is selected per group at a time.
*/
private ButtonGroup __general_ButtonGroup;
private ButtonGroup __compare_ButtonGroup;
private ButtonGroup __graph_ButtonGroup;
private ButtonGroup __daily_ButtonGroup;
private ButtonGroup __other_ButtonGroup;

private JTabbedPane __main_JTabbedPane;	// The main pane to hold all components.

private JPanel __general_JPanel;
private JPanel __compare_JPanel;
private JPanel __graph_JPanel;
private JPanel __daily_JPanel;
private JPanel __other_JPanel;

/**
Command buttons that appear at the bottom of the form.
*/
private JButton 
	__runStateModJButton,
	__cancelJButton;
/**
Labels for each of the radio buttons.
*/
private JLabel
	__xbnJLabel,
	__xcuJLabel1,
	__xcuJLabel2,
	__xcuJLabel3,
	__xcuJLabel4,
	__xdcJLabel,
	__xdgJLabel,
	__xdyJLabel,
	__xnmJLabel1,
	__xnmJLabel2,
	__xrcJLabel,
	__xrgJLabel,
	__xrxJLabel,
	__xryJLabel,
	__xscJLabel,
	__xspJLabel,
	__xstJLabel1,
	__xstJLabel2,
	__xstJLabel3,
	__xstJLabel4,
	__xstJLabel5,
	__xwbJLabel,
	__xwcJLabel,
	__xwgJLabel,
	__xwrJLabel,
	__xwyJLabel;	

/**
Radio buttons for selecting the type of report to run.
*/
private JRadioButton 
	__xbnJRadioButton,
	__xcuJRadioButton,
	__xdcJRadioButton,
	__xdgJRadioButton,
	__xdyJRadioButton,
	__xnmJRadioButton,
	__xrcJRadioButton,
	__xrgJRadioButton,
	__xrxJRadioButton,
	__xryJRadioButton,
	__xscJRadioButton,
	__xspJRadioButton,
	__xstJRadioButton,
	__xwbJRadioButton,
	__xwcJRadioButton,
	__xwgJRadioButton,
	__xwrJRadioButton,
	__xwyJRadioButton;

/**
Combo boxes for choosing additional information for graph reports.
*/
private SimpleJComboBox 
	__xdgSimpleJComboBox,
	__xrgSimpleJComboBox,
	__xwgSimpleJComboBox;

/**
The data set from which data will be read.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
Constructor.
@param dataset the dataset that contains the data.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
*/
public StateMod_RunReport_JFrame(StateMod_DataSet dataset,
				StateMod_DataSet_WindowManager dataset_wm ) {
	StateMod_GUIUtil.setTitle(this, dataset, "Run StateMod Report", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	__dataset_wm = dataset_wm;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_RunReport_JFrame.actionPerformed"; 
	String action = e.getActionCommand();

	if (action.equals(__BUTTON_RUN_REPORT)) {
		JRadioButton cb = getSelectedJRadioButton();
		if (cb == null) {
			return;
		}
		StringBuffer sb = new StringBuffer("-report ");
		sb.append(cb.getText());

		// now check if station id is needed; if so, append it
		if (cb == __xdgJRadioButton) {
			sb.append(" -");
			sb.append(StringUtil.getToken(
				__xdgSimpleJComboBox.getSelected(), " ", 0, 0));
		}
		else if (cb == __xrgJRadioButton) {
			sb.append(" -");
			sb.append(StringUtil.getToken(
				__xrgSimpleJComboBox.getSelected(), " ", 0, 0));
		}
		else if (cb == __xwgJRadioButton) {
			sb.append(" -");
			sb.append(StringUtil.getToken(
				__xwgSimpleJComboBox.getSelected(), " ", 0, 0));
		}

		String s = sb.toString();
		try {	// Run the report using the process manager dialog.
			// Pass in the main GUI class so that the modal process
			// manager dialog is on top of that window.
			Message.printStatus(1, routine, "Running statemod "
				+ "with command line options: '" + sb + "'");
			StateMod_Util.runStateMod ( __dataset, s, true,
				__dataset_wm.getWindow(
				StateMod_DataSet_WindowManager.WINDOW_MAIN) );
		}
		catch (Exception ex) {
			Message.printWarning(1, routine,
				"There was an error running:  \n" + s, this);
		}
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.WINDOW_RUN_REPORT );
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-08-21)
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__xdgSimpleJComboBox = null;
	__xrgSimpleJComboBox = null;
	__xwgSimpleJComboBox = null;

	__general_ButtonGroup = null;
	__compare_ButtonGroup = null;
	__graph_ButtonGroup = null;
	__daily_ButtonGroup = null;
	__other_ButtonGroup = null;
	__xbnJRadioButton = null;
	__xcuJRadioButton = null;
	__xdcJRadioButton = null;
	__xdgJRadioButton = null;
	__xdyJRadioButton = null;
	__xnmJRadioButton = null;
	__xrcJRadioButton = null;
	__xrgJRadioButton = null;
	__xrxJRadioButton = null;
	__xryJRadioButton = null;
	__xscJRadioButton = null;
	__xspJRadioButton = null;
	__xstJRadioButton = null;
	__xwbJRadioButton = null;
	__xwcJRadioButton = null;
	__xwgJRadioButton = null;
	__xwrJRadioButton = null;
	__xwyJRadioButton = null;

	__xbnJLabel = null;
	__xcuJLabel1 = null;
	__xcuJLabel2 = null;
	__xcuJLabel3 = null;
	__xcuJLabel4 = null;
	__xdcJLabel = null;
	__xdgJLabel = null;
	__xdyJLabel = null;
	__xnmJLabel1 = null;
	__xnmJLabel2 = null;
	__xrcJLabel = null;
	__xrgJLabel = null;
	__xrxJLabel = null;
	__xryJLabel = null;
	__xscJLabel = null;
	__xspJLabel = null;
	__xstJLabel1 = null;
	__xstJLabel2 = null;
	__xstJLabel3 = null;
	__xstJLabel4 = null;
	__xstJLabel5 = null;
	__xwbJLabel = null;
	__xwcJLabel = null;
	__xwgJLabel = null;
	__xwrJLabel = null;
	__xwyJLabel = null;	
	
	__runStateModJButton = null;
	__cancelJButton = null;
	super.finalize();
}

/**
Determines which JRadioButton is currently-selected and returns it.
@return the JRadioButton that is currently selected.  If none are selected,
return null.
*/
private JRadioButton getSelectedJRadioButton()
{	// First figure out which panel is selected...
	JPanel panel = (JPanel)__main_JTabbedPane.getSelectedComponent();
	// List in the order of the interface...
	if ( panel == __general_JPanel ) {
		if (__xstJRadioButton.isSelected()) {
			return __xstJRadioButton;
		}
		else if (__xwbJRadioButton.isSelected()) {
			return __xwbJRadioButton;
		}
		else if (__xwrJRadioButton.isSelected()) {
			return __xwrJRadioButton;
		}
		else if (__xcuJRadioButton.isSelected()) {
			return __xcuJRadioButton;
		}
	}
	else if ( panel == __compare_JPanel ) {
		if (__xdcJRadioButton.isSelected()) {
			return __xdcJRadioButton;
		}
		else if (__xrcJRadioButton.isSelected()) {
			return __xrcJRadioButton;
		}
		else if (__xwcJRadioButton.isSelected()) {
			return __xwcJRadioButton;
		}
		else if (__xscJRadioButton.isSelected()) {
			return __xscJRadioButton;
		}
	}
	else if ( panel == __graph_JPanel ) {
		if (__xdgJRadioButton.isSelected()) {
			return __xdgJRadioButton;
		}
		else if (__xrgJRadioButton.isSelected()) {
			return __xrgJRadioButton;
		}
		else if (__xwgJRadioButton.isSelected()) {
			return __xwgJRadioButton;
		}
	}
	else if ( panel == __daily_JPanel ) {
		if (__xdyJRadioButton.isSelected()) {
			return __xdyJRadioButton;
		}
		else if (__xryJRadioButton.isSelected()) {
			return __xryJRadioButton;
		}
		else if (__xwyJRadioButton.isSelected()) {
			return __xwyJRadioButton;
		}
	}
	else if ( panel == __other_JPanel ) {
		if (__xnmJRadioButton.isSelected()) {
			return __xnmJRadioButton;
		}
		else if (__xrxJRadioButton.isSelected()) {
			return __xrxJRadioButton;
		}
		else if (__xspJRadioButton.isSelected()) {
			return __xspJRadioButton;
		}
		else if (__xbnJRadioButton.isSelected()) {
			return __xbnJRadioButton;
		}
	}
	return null;
}

/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {}

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
Responds to mouse pressed events; selects the radio button associated with a
label or combo box that was clicked on.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {
	Object o = e.getSource();

	//////////////////////////////////////////////////////////////
	// general panel
	//////////////////////////////////////////////////////////////
	if (o == __xstJLabel1 || o == __xstJLabel2 || o == __xstJLabel3
		|| o == __xstJLabel4 || o == __xstJLabel5) {
		if (__xstJRadioButton.isEnabled()) {
			__xstJRadioButton.setSelected(true);
		}
	}
	else if (o == __xwbJLabel) {
		if (__xwbJRadioButton.isEnabled()) {
			__xwbJRadioButton.setSelected(true);	
		}
	}
	else if (o == __xwrJLabel) {
		if (__xwrJRadioButton.isEnabled()) {
			__xwrJRadioButton.setSelected(true);
		}
	}
	else if (o == __xcuJLabel1 || o == __xcuJLabel2 || o == __xcuJLabel3
		|| o == __xcuJLabel4) {
		if (__xcuJRadioButton.isEnabled()) {
			__xcuJRadioButton.setSelected(true);	
		}
	}
	//////////////////////////////////////////////////////////////
	// compare panel
	//////////////////////////////////////////////////////////////
	else if (o == __xdcJLabel) {
		if (__xdcJRadioButton.isEnabled()) {
			__xdcJRadioButton.setSelected(true);	
		}
	}
	else if (o == __xrcJLabel) {
		if (__xrcJRadioButton.isEnabled()) {
			__xrcJRadioButton.setSelected(true);
		}
	}
	else if (o == __xwcJLabel) {
		if (__xwcJRadioButton.isEnabled()) {
			__xwcJRadioButton.setSelected(true);
		}
	}
	else if (o == __xscJLabel) {
		if (__xscJRadioButton.isEnabled()) {
			__xscJRadioButton.setSelected(true);
		}
	}
	//////////////////////////////////////////////////////////////
	// graph panel
	//////////////////////////////////////////////////////////////	
	else if (o == __xrgJLabel 
		|| (o == __xrgSimpleJComboBox
		&& __xrgSimpleJComboBox.isEnabled())) {
		if (__xrgJRadioButton.isEnabled()) {
			__xrgJRadioButton.setSelected(true);
		}
	}
	else if (o == __xdgJLabel 
		|| (o == __xdgSimpleJComboBox
		&& __xdgSimpleJComboBox.isEnabled())) {
		if (__xdgJRadioButton.isEnabled()) {
			__xdgJRadioButton.setSelected(true);
		}
	}
	else if (o == __xwgJLabel 
		|| (o == __xwgSimpleJComboBox 
		&& __xwgSimpleJComboBox.isEnabled())) {
		if (__xwgJRadioButton.isEnabled()) {
			__xwgJRadioButton.setSelected(true);
		}
	}
	//////////////////////////////////////////////////////////////
	// daily panel
	//////////////////////////////////////////////////////////////	
	else if (o == __xdyJLabel) {
		if (__xdyJRadioButton.isEnabled()) {
			__xdyJRadioButton.setSelected(true);
		}
	}
	else if (o == __xryJLabel) {
		if (__xryJRadioButton.isEnabled()) {
			__xryJRadioButton.setSelected(true);
		}
	}
	else if (o == __xwyJLabel) {
		if (__xwyJRadioButton.isEnabled()) {
			__xwyJRadioButton.setSelected(true);
		}
	}
	//////////////////////////////////////////////////////////////
	// daily panel
	//////////////////////////////////////////////////////////////		
	else if (o == __xnmJLabel1 || o == __xnmJLabel2) {
		if (__xnmJRadioButton.isEnabled()) {
			__xnmJRadioButton.setSelected(true);
		}
	}
	else if (o == __xrxJLabel) {
		if (__xrxJRadioButton.isEnabled()) {
			__xrxJRadioButton.setSelected(true);
		}
	}
	else if (o == __xspJLabel) {
		if (__xspJRadioButton.isEnabled()) {
			__xspJRadioButton.setSelected(true);
		}
	}
	else if (o == __xbnJLabel) {
		if (__xbnJRadioButton.isEnabled()) {
			__xbnJRadioButton.setSelected(true);
		}
	}
}

/**
Responds to mouse released events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {}

/**
Sets up the GUI.
*/
private void setupGUI() {
	__xdgSimpleJComboBox = new SimpleJComboBox();

	List diversionsVector = (List)
		__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_STATIONS).getData();
	List list_names = StateMod_Util.createIdentifierList (
		diversionsVector, true );
	int size = list_names.size();
	for (int i = 0; i < size; i++) {
		__xdgSimpleJComboBox.add ( list_names.get(i) +
		" - Diversion");
	}

	List instreamFlowsVector = (List)
		__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_STATIONS).getData();
	list_names = StateMod_Util.createIdentifierList ( instreamFlowsVector, true );
	size = list_names.size();
	for (int i = 0; i < size; i++) {
		__xdgSimpleJComboBox.add ( list_names.get(i) +
		" - Instream Flow");
	}

	List gageVector = (List)
		__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_STATIONS).getData();
	list_names = StateMod_Util.createIdentifierList ( gageVector, true );
	size = list_names.size();
	for (int i = 0; i < size; i++) {
		__xdgSimpleJComboBox.add ( list_names.get(i) +
		" - Stream Gage");
	}

	List estimateVector = (List)
		__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS).getData();
	list_names = StateMod_Util.createIdentifierList ( estimateVector, true );
	size = list_names.size();
	for (int i = 0; i < size; i++) {
		__xdgSimpleJComboBox.add ( list_names.get(i) +
		" - Stream Estimate");
	}

	List reservoirsVector = (List)
		__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
	list_names = StateMod_Util.createIdentifierList ( reservoirsVector, true );
	size = list_names.size();
	__xrgSimpleJComboBox = new SimpleJComboBox();
	for (int i = 0; i < size; i++) {
		__xrgSimpleJComboBox.add ( list_names.get(i) +
		" - Reservoir");
	}

	List wellsVector = (List)
		__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_WELL_STATIONS).getData();
	list_names = StateMod_Util.createIdentifierList ( wellsVector, true );
	size = list_names.size();
	__xwgSimpleJComboBox = new SimpleJComboBox();
	for (int i = 0; i < size; i++) {
		__xwgSimpleJComboBox.add ( list_names.get(i) +
		" - Well");
	}

	// Define the button groups and add radio buttons to the group in the
	// order of the GUI...

	__general_ButtonGroup = new ButtonGroup();
	__general_ButtonGroup.add(
		__xstJRadioButton = new JRadioButton("-xst", false) );
	__general_ButtonGroup.add(
		__xwbJRadioButton = new JRadioButton("-xwb", true) );
	__general_ButtonGroup.add(
		__xwrJRadioButton = new JRadioButton("-xwr", false) );
	__general_ButtonGroup.add(
		__xcuJRadioButton = new JRadioButton("-xcu", false) );
	__compare_ButtonGroup = new ButtonGroup();
	__compare_ButtonGroup.add(
		__xdcJRadioButton = new JRadioButton("-xdc", true) );
	__compare_ButtonGroup.add(
		__xrcJRadioButton = new JRadioButton("-xrc", false) );
	__compare_ButtonGroup.add(
		__xwcJRadioButton = new JRadioButton("-xwc", false) );
	__compare_ButtonGroup.add(
		__xscJRadioButton = new JRadioButton("-xsc", false) );
	__graph_ButtonGroup = new ButtonGroup();
	__graph_ButtonGroup.add(
		__xdgJRadioButton = new JRadioButton("-xdg", true) );
	__graph_ButtonGroup.add(
		__xrgJRadioButton = new JRadioButton("-xrg", false) );
	__graph_ButtonGroup.add(
		__xwgJRadioButton = new JRadioButton("-xwg", false) );
	__daily_ButtonGroup = new ButtonGroup();
	__daily_ButtonGroup.add(
		__xdyJRadioButton = new JRadioButton("-xdy", true) );
	__daily_ButtonGroup.add(
		__xryJRadioButton = new JRadioButton("-xry", false) );
	__daily_ButtonGroup.add(
		__xwyJRadioButton = new JRadioButton("-xwy", false) );
	__other_ButtonGroup = new ButtonGroup();
	__other_ButtonGroup.add(
		__xnmJRadioButton = new JRadioButton("-xnm", true) );
	__other_ButtonGroup.add(
		__xrxJRadioButton = new JRadioButton("-xrx", false) );
	__other_ButtonGroup.add(
		__xspJRadioButton = new JRadioButton("-xsp", false) );
	__other_ButtonGroup.add(
		__xbnJRadioButton = new JRadioButton("-xbn", false) );

	// Set the selected item in the graph output, based on whether data
	// exists...

	boolean something_selected = false;	// To help select a radio button
	if (__xdgSimpleJComboBox.getItemCount() == 0) {
		__xdgSimpleJComboBox.setEnabled(false);
	}
	else {	__xdgSimpleJComboBox.select(0);
		__xdgJRadioButton.setSelected(true);
		something_selected = true;
	}
	if (__xrgSimpleJComboBox.getItemCount() == 0) {
		__xrgSimpleJComboBox.setEnabled(false);
	}
	else {	__xrgSimpleJComboBox.select(0);
		if ( !something_selected ) {
			__xrgJRadioButton.setSelected(true);
			something_selected = true;
		}
	}
	if (__xwgSimpleJComboBox.getItemCount() == 0) {
		__xwgSimpleJComboBox.setEnabled(false);
	}
	else {	__xwgSimpleJComboBox.select(0);
		if ( !something_selected ) {
			__xwgJRadioButton.setSelected(true);
		}
	}

	__runStateModJButton = new JButton(__BUTTON_RUN_REPORT);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	// REVISIT - enable when full help system is redesigned
	//__helpJButton = new JButton(__BUTTON_HELP);	

	GridBagLayout gb = new GridBagLayout();
	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(gb);

	int col1=0;
	int col2=1;
	int col3=2;
	int y;
	int bound = 2;	// buffer around components.

	__main_JTabbedPane = new JTabbedPane();

	__general_JPanel = new JPanel();
	__general_JPanel.setLayout(gb);
	__compare_JPanel = new JPanel();
	__compare_JPanel.setLayout(gb);
	__graph_JPanel = new JPanel();
	__graph_JPanel.setLayout(gb);
	__daily_JPanel = new JPanel();
	__daily_JPanel.setLayout(gb);
	__other_JPanel = new JPanel();
	__other_JPanel.setLayout(gb);

	/////////////////////////////////////////////////////////////
	// general panel
	/////////////////////////////////////////////////////////////
	y = 0;
	JGUIUtil.addComponent(
		__general_JPanel, new JLabel(
		"Select one of the following report options."
		+ "  Output files will have the extensions shown."),
		col1, y, 3, 1, 0, 0,
		0, 0, bound, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	JGUIUtil.addComponent(
		__general_JPanel, __xstJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xstJLabel1 = new JLabel("Direct and Instream Flow Data (*.xdd)");
	__xstJLabel1.addMouseListener(this);		
	JGUIUtil.addComponent(
		__general_JPanel, __xstJLabel1,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xstJLabel2 = new JLabel("Reservoir Data - Total and by Account "
		+ "(*.xre)");
	__xstJLabel2.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xstJLabel2,
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xstJLabel3 = new JLabel("Operation Right Summary (*.xop)");
	__xstJLabel3.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xstJLabel3,
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xstJLabel4 = new JLabel("Instream Flow Reach Summary (*.xir)");
	__xstJLabel4.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xstJLabel4, 
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xstJLabel5 = new JLabel("Well Summary (*.xwe)");
	__xstJLabel5.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xstJLabel5,
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__general_JPanel, __xwbJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xwbJLabel = new JLabel("Water balance (*.xwb) and "
		+ "Ground Water Balance (*.xgw)");
	__xwbJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xwbJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__general_JPanel, __xwrJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xwrJLabel = new JLabel("Water rights list, sorted by basin "
		+ "rank (*.xwr)");
	__xwrJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xwrJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__general_JPanel, __xcuJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xcuJLabel1 = new JLabel("Simulated Diversions and "
		+ "Consumptive Use (*.xcu)");
	__xcuJLabel1.addMouseListener(this);
	JGUIUtil.addComponent(
		__general_JPanel, __xcuJLabel1,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xcuJLabel2 = new JLabel("Water supply summary (*.xsu)");
	__xcuJLabel2.addMouseListener(this);		
	JGUIUtil.addComponent(
		__general_JPanel, __xcuJLabel2,
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xcuJLabel3 = new JLabel("Shortage Summary (*.xsh)");
	__xcuJLabel3.addMouseListener(this);		
	JGUIUtil.addComponent(
		__general_JPanel, __xcuJLabel3,
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xcuJLabel4 = new JLabel("CU by Water District (*.xwd)");
	__xcuJLabel4.addMouseListener(this);		
	JGUIUtil.addComponent(
		__general_JPanel, __xcuJLabel4,
		col2, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	/////////////////////////////////////////////////////////////
	// compare panel
	/////////////////////////////////////////////////////////////
	y = 0;
	JGUIUtil.addComponent(
		__compare_JPanel, new JLabel(
		"Select one of the following report options."
		+ "  Output files will have the extensions shown."),
		col1, y, 3, 1, 0, 0,
		0, 0, bound, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		

	JGUIUtil.addComponent(
		__compare_JPanel, __xdcJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xdcJLabel = new JLabel("Diversion comparison (*.xdc)");
	__xdcJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__compare_JPanel, __xdcJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__compare_JPanel, __xrcJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xrcJLabel = new JLabel("Reservoir comparison (*.xrc)");
	__xrcJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__compare_JPanel, __xrcJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__compare_JPanel, __xwcJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xwcJLabel = new JLabel("Well comparison (*.xwc)");
	__xwcJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__compare_JPanel, __xwcJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__compare_JPanel, __xscJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xscJLabel = new JLabel("Stream flow gage comparison (*.xsc)");
	__xscJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__compare_JPanel, __xscJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	/////////////////////////////////////////////////////////////
	// graph panel
	/////////////////////////////////////////////////////////////
	y = 0;
	JGUIUtil.addComponent(
		__graph_JPanel, new JLabel(
		"Select one of the following report options."
		+ "  Output files will have the extensions shown."),
		col1, y, 3, 1, 0, 0,
		0, 0, bound, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		

	JGUIUtil.addComponent(
		__graph_JPanel, __xdgJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__graph_JPanel, __xdgSimpleJComboBox,
		col2, y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	__xdgSimpleJComboBox.addMouseListener(this);
	__xdgJLabel = new JLabel("Direct diversion, instream flow and "
		+ "gage graph (*.xdg)");
	__xdgJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__graph_JPanel, __xdgJLabel,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__graph_JPanel, __xrgJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__graph_JPanel, __xrgSimpleJComboBox,
		col2, y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	__xrgSimpleJComboBox.addMouseListener(this);
	__xrgJLabel = new JLabel("Reservoir graph (*.xrg)");
	__xrgJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__graph_JPanel, __xrgJLabel,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__graph_JPanel, __xwgJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__graph_JPanel, __xwgSimpleJComboBox,
		col2, y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	__xwgSimpleJComboBox.addMouseListener(this);
	__xwgJLabel = new JLabel("Well graph (*.xwg)");
	__xwgJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__graph_JPanel, __xwgJLabel,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);


	/////////////////////////////////////////////////////////////
	// daily panel
	/////////////////////////////////////////////////////////////
	y = 0;
	JGUIUtil.addComponent(
		__daily_JPanel, new JLabel(
		"Select one of the following report options."
		+ "  Output files will have the extensions shown."),
		col1, y, 3, 1, 0, 0,
		0, 0, bound, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		

	JGUIUtil.addComponent(
		__daily_JPanel, __xdyJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xdyJLabel = new JLabel("Daily Direct Diversion and Instream Flow Data"
		+ " (*.xdy)");
	__xdyJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__daily_JPanel, __xdyJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__daily_JPanel, __xryJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xryJLabel = new JLabel("Daily Reservoir Data (*.xry)");
	__xryJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__daily_JPanel, __xryJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__daily_JPanel, __xwyJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xwyJLabel = new JLabel("Daily Well Data (*.xwy)");
	__xwyJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__daily_JPanel, __xwyJLabel,
		col2, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	/////////////////////////////////////////////////////////////
	// other panel
	/////////////////////////////////////////////////////////////
	y=0;
	JGUIUtil.addComponent(
		__other_JPanel, new JLabel(
		"Select one of the following report options."
		+ "  Output files will have the extensions shown."),
		col1, y, 3, 1, 0, 0,
		0, 0, bound, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);	

	JGUIUtil.addComponent(
		__other_JPanel, __xnmJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xnmJLabel1 = new JLabel(
		"Detailed Node Accounting, monthly (*.xnm) and ");
	__xnmJLabel1.addMouseListener(this);
	JGUIUtil.addComponent(
		__other_JPanel, __xnmJLabel1,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xnmJLabel2 = new JLabel(
		"Summary Node Accounting, average (*.xna)");
	__xnmJLabel2.addMouseListener(this);
	JGUIUtil.addComponent(
		__other_JPanel, __xnmJLabel2,
		col3, ++y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(
		__other_JPanel, __xrxJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xrxJLabel = new JLabel("River data summary (*.xrx)");
	__xrxJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__other_JPanel, __xrxJLabel,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__other_JPanel, __xspJRadioButton,
		col1, ++y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xspJLabel = new JLabel("Selected parameter printout," 
		+ " uses output control file (*.xsp)");
	__xspJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__other_JPanel, __xspJLabel,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(
		__other_JPanel, __xbnJRadioButton,
		col1, ++y, 1, 1,0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__xbnJLabel = new JLabel("ASCII listing of Binary Direct and "
		+ "Instream Data (*.xbn)");
	__xbnJLabel.addMouseListener(this);
	JGUIUtil.addComponent(
		__other_JPanel, __xbnJLabel,
		col3, y, 1, 1, 0, 0,
		0, bound, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__main_JTabbedPane.add("General", __general_JPanel);
	__main_JTabbedPane.add("Compare", __compare_JPanel);
	__main_JTabbedPane.add("Graph", __graph_JPanel);
	__main_JTabbedPane.add("Daily", __daily_JPanel);
	__main_JTabbedPane.add("Other", __other_JPanel);

	getContentPane().add("Center", __main_JTabbedPane);

	// add buttons
	JPanel p1 = new JPanel();
	p1.add(__runStateModJButton);
	p1.add(__cancelJButton);
	// REVISIT - add when full help system is redesigned.
	//p1.add(__helpJButton);
	//__helpJButton.setEnabled(false);
	//__helpJButton.addActionListener(this);
	__runStateModJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);

	getContentPane().add("South", p1);

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_RUN_REPORT, this );
	}

	pack();
	JGUIUtil.center(this);
	setVisible(true);
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
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (
		StateMod_DataSet_WindowManager.WINDOW_RUN_REPORT );
	}
	else {	JGUIUtil.close ( this );
	}
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
