//------------------------------------------------------------------------------
// StateMod_Diversion_ReturnFlow_JFrame - dialog to edit a diversion's 
//	return flow information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 08 Dec 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 25 Feb 1998	CEN, RTi		Adding top panel.
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
//------------------------------------------------------------------------------
// 2003-06-10	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMdivReturnFlowFrame
// 2003-06-17	JTS, RTi		First functioning version.
// 2003-07-15	JTS, RTi		* Added status bar.
//					* Change to use new dataset design.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-07-30	SAM, RTi		* Remove import of
//					  StateMod_DataSetComponent.
// 					* Changed isDirty() back to setDirty().
// 2003-08-29	SAM, RTi		Update due to changes in
//					StateMod_Diversion.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-09-30	SAM, RTi		Pass component type to return flow
//					constructor.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
// 2004-10-27	SAM, RTi		* Use return flow table model instead of
//					  the combined diversion data table
//					  model.
//					* Some code referred to "rights" -
//					  change to "return flows" (copy and
//					  paste error?).
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_ReturnFlow;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;

import RTi.Util.Help.URLHelp;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class is a GUI for displaying and editing return flows.
*/
public class StateMod_Diversion_ReturnFlow_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Button labels.
*/
private final String 
	__BUTTON_ADD_RETURN_FLOW = "Add return flow",
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",	
	__BUTTON_CLOSE = "Close",	
	__BUTTON_DEL_RETURN_FLOW = "Delete return flow",
	__BUTTON_HELP = "Help";

/**
Whether the form data can be edited or not.
*/
private boolean __editable = false;

/**
GUI Buttons.
*/
private JButton
	__addReturnFlow,
	__closeJButton,
	__deleteReturnFlow,
	__helpJButton;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet in which diversion return flow data is shown.
*/
private JWorksheet __worksheet;

/**
The dataset containing all the data.
*/
private StateMod_DataSet __dataset;
/**
The currently-selected diversion for which data is being shown.
*/
private StateMod_Diversion __currentDiv;

/**
Constructor.
@param dataset the dataset in which the data is contained.
@param div the diversion for which to display info
@param editable whether the form data can be edited or not
*/
public StateMod_Diversion_ReturnFlow_JFrame(StateMod_DataSet dataset,
StateMod_Diversion div, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, div.getName() 
		+ " - Diversion Return Flow", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	

	__dataset = dataset;

	__currentDiv = div;

	__editable = editable;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that occurred.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_ADD_RETURN_FLOW)) {
		StateMod_ReturnFlow aReturnFlow = 
			new StateMod_ReturnFlow(
			StateMod_DataSet.COMP_DIVERSION_STATIONS);
		aReturnFlow._isClone = true;
		__worksheet.addRow(aReturnFlow);
		__worksheet.scrollToLastRow();
		__worksheet.selectLastRow();
		__deleteReturnFlow.setEnabled(true);
	}
	else if (action.equals(__BUTTON_DEL_RETURN_FLOW)) {
		int row = __worksheet.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this,
				"Delete Return Flow",
				"Delete return flow?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.NO) {
				return;
			}
			__worksheet.cancelEditing();
			__worksheet.deleteRow(row);
			__deleteReturnFlow.setEnabled(false);
		}	
	}
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-06-10)
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		if (saveData()) {
			setVisible(false);
			dispose();
		}
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveData();
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		setVisible(false);
		dispose();
	}
}

/**
Saves the input back into the dataset.
@return true if the data was saved successfuly.  False if not.
*/
private boolean saveData() {
	String routine = "StateMod_Diversion_ReturnFlow_JFrame.saveData";
	if (!__worksheet.stopEditing()) {
		// don't save if there are errors.
		Message.printWarning(1, routine, "There are errors in the data "
			+ "that must be corrected before data can be saved.",
			this);
		return false;
	}
	
	if (checkInput() > 0) {
		return false;
	}

	// now only save data if any are different.
	boolean needToSave = false;

	// if the Vectors are differently-sized, they're different
	Vector wv = __worksheet.getAllData();		// w for worksheet
	Vector dv = __currentDiv.getReturnFlows();	// d for diversion

	needToSave = !(StateMod_ReturnFlow.equals(wv, dv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some return flow locations and added back in identical values
		return true;
	}

	// clone the objects from the worksheet vector and assign them
	// to the diversion object as its new return flows.
	int size = wv.size();
	Vector clone = new Vector();
	StateMod_ReturnFlow rf = null;
	StateMod_ReturnFlow crf = null;
	for (int i = 0; i < size; i++) {
		rf = (StateMod_ReturnFlow)wv.elementAt(i);
		crf = (StateMod_ReturnFlow)rf.clone();
		rf.setCrtnid(StringUtil.getToken(rf.getCrtnid(), " ",
			StringUtil.DELIM_SKIP_BLANKS, 0));
		crf._isClone = false;
		clone.add(crf);
	}

	__currentDiv.setReturnFlow(clone);
	__dataset.setDirty(__dataset.COMP_DIVERSION_STATIONS, true);

	return true;
}

/**
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput() {
	String routine = "StateMod_Diversion_ReturnFlow_JFrame.checkInput";
	Vector v = __worksheet.getAllData();

	int size = 0;
	if ( v != null ) {
		size = v.size();
	}
	StateMod_ReturnFlow rf = null;
	String warning = "";
	String riverNode;
	int fatalCount = 0;
	for (int i = 0; i < size; i++) {
		rf = (StateMod_ReturnFlow)(v.elementAt(i));
		riverNode = rf.getCrtnid();
		riverNode = StringUtil.getToken(riverNode, " ", 
			StringUtil.DELIM_SKIP_BLANKS, 0);
		if (riverNode == null) {
			riverNode = "";
			warning += "\nMust specify a River Node ID.";
			fatalCount++;
		}
		if (riverNode.length() > 12) {
			warning += "\nRiver Node ID (" + riverNode+ ") is "
				+ "longer than 12 characters.";
			fatalCount++;
		}

		if (riverNode.indexOf(" ") > -1 || riverNode.indexOf("-") > -1){
			warning += "\nRiver Node ID (" + riverNode 
				+ ") cannot contain spaces or dashes.";
			fatalCount++;
		}		
	}
	if ( warning.length() > 0 ) {
		warning += "\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, warning, this );
		if ( fatalCount > 0 ) {
			// Fatal errors...
			Message.printStatus ( 1, routine,
				"Returning 1 from checkInput()" );
			return 1;
		}
		else {	// Nonfatal errors...
			Message.printStatus ( 1, routine,
				"Returning -1 from checkInput()" );
			return -1;
		}
	}
	else {	// No errors...
		Message.printStatus ( 1, routine,
			"Returning 0 from checkInput()" );
		return 0;
	}
}

/**
Checks to see if the __deleteReturnFlow button should be enabled or not.
*/
private void checkDeleteReturnFlowButton() {
	int row = __worksheet.getSelectedRow();
	if (row == -1) {
		__deleteReturnFlow.setEnabled(false);
	}
	else {
		__deleteReturnFlow.setEnabled(true);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__currentDiv = null; 
	__worksheet = null;
	__addReturnFlow = null;
	__deleteReturnFlow = null;
	__helpJButton = null;
	__closeJButton = null;
	super.finalize();
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; checks if the __deleteAreaCap button should
be enabled.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	checkDeleteReturnFlowButton();
}

/**
Responds to key typed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyTyped(KeyEvent e) {}

/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {}

/**
Responds to mouse pressed events; does nothing.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {}

/**
Responds to mouse released events; checks to see if the __deleteAreaCap button
should be enabled or not.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	checkDeleteReturnFlowButton();
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
Sets up the GUI.
*/
public void setupGUI() {
	String routine = "setupGUI";

	addWindowListener(this);
	
	GridBagConstraints gbc = new GridBagConstraints();

	__addReturnFlow = new JButton(__BUTTON_ADD_RETURN_FLOW);
	__deleteReturnFlow = new JButton(__BUTTON_DEL_RETURN_FLOW);
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);	

	GridBagLayout gb = new GridBagLayout();
	JPanel bigPanel = new JPanel();
	bigPanel.setLayout(gb);

	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
	JPanel p1 = new JPanel();
	p1.setLayout(new FlowLayout(FlowLayout.RIGHT));

	GridLayout gl = new GridLayout(2, 2, 1, 1);
	JPanel info_panel = new JPanel();
	info_panel.setLayout(gl);

	JPanel main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout());

	info_panel.add(new JLabel("Diversion:"));	
	info_panel.add(new JLabel(__currentDiv.getID()));
	info_panel.add(new JLabel("Diversion name:"));	
	info_panel.add(new JLabel(__currentDiv.getName()));
	if (__editable) {
		p1.add(__addReturnFlow);
		p1.add(__deleteReturnFlow);
		__deleteReturnFlow.setEnabled(false);
	}
//	p1.add(__helpJButton);
	p1.add(applyJButton);
	p1.add(cancelJButton);	
	p1.add(__closeJButton);

	PropList p = new PropList("StateMod_Diversion_ReturnFlow_JFrame"
		+ ".JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int widths[] = null;
	JScrollWorksheet jsw = null;
	try {	
		Vector nodes = (Vector)(__dataset.getComponentForComponentType(
			__dataset.COMP_RIVER_NETWORK).getData());

		Vector v = new Vector();
		Vector v2 = __currentDiv.getReturnFlows();
		int size = v2.size();
		StateMod_ReturnFlow rf;		
		for (int i = 0; i < size; i++) {
			rf = (StateMod_ReturnFlow)
				((StateMod_ReturnFlow)v2.elementAt(i)).clone();
			rf.setCrtnid(rf.getCrtnid()
				+ StateMod_Util.findNameInVector(rf.getCrtnid(),
				nodes, true));
			v.add(rf);			
		}

		StateMod_ReturnFlow_TableModel tmd = new
			StateMod_ReturnFlow_TableModel(
			__dataset, v, __editable, true );
		StateMod_ReturnFlow_CellRenderer crd = new
			StateMod_ReturnFlow_CellRenderer(tmd);
	
		jsw = new JScrollWorksheet(crd, tmd, p);
		__worksheet = jsw.getJWorksheet();

		v = StateMod_Util.createDataList(nodes, true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReturnFlow_TableModel.COL_RIVER_NODE, v,false);

		// 10
		Vector delayIDs = null;
		if (__dataset.getIday() == 1) {
			delayIDs = (Vector)(__dataset
				.getComponentForComponentType(
				__dataset.COMP_DELAY_TABLES_DAILY).getData());
		}
		else {
			delayIDs = (Vector)(__dataset
				.getComponentForComponentType(
				__dataset.COMP_DELAY_TABLES_MONTHLY).getData());
		}
		v = StateMod_Util.createDataList(delayIDs, true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReturnFlow_TableModel.COL_RETURN_ID, v, false);
		widths = crd.getColumnWidths();		
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);

	main_panel.add(jsw, "Center");
	main_panel.add(p1, "South");

	JGUIUtil.addComponent(bigPanel, info_panel, 0, 0, 1, 1, 0, 0,
		gbc.NONE, gbc.NORTHWEST);
	JGUIUtil.addComponent(bigPanel, main_panel, 0, 1, 10, 10, 1, 1,
		gbc.BOTH, gbc.SOUTH);

	__addReturnFlow.addActionListener(this);
	__deleteReturnFlow.addActionListener(this);
	__closeJButton.addActionListener(this);
	__helpJButton.addActionListener(this);
	cancelJButton.addActionListener(this);
	applyJButton.addActionListener(this);	

	getContentPane().add(bigPanel);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 0, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__statusJTextField = new JTextField(5);
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __statusJTextField,
		7, 0, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottomJPanel);	

	pack();
	setSize(530, 280);
	JGUIUtil.center(this);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
}

/**
Responds to window activated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowActivated(WindowEvent e) {}

/**
Responds to window closed events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowClosed(WindowEvent e) {}

/**
Responds to window closing events; closes up the window properly.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	if (saveData()) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(false);
		dispose();
	}
	else {
		setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
	}
}

/**
Responds to window deactivated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to window iconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {}

/**
Responds to window opened events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpened(WindowEvent e) {}

/**
Responds to window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e) {}

}
