//------------------------------------------------------------------------------
// StateMod_Diversion_Right_JFrame - dialog to edit a diversion's rights 
//	information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 16 Dec 1997	Catherine E.
//		Nutting-Lane, RTi	Created initial version of class.
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-10	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMdivRightsFrame
// 2003-06-17	JTS, RTi		First functioning version.
// 2003-07-15	JTS, RTi		* Added status bar.
//					* Change to use new dataset design.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		Change isDirty() back to setDirty().
// 2003-08-28	SAM, RTi		Update because of change in the
//					StateMod_DiversionRight class - no
//					more linked list.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-09	JTS, RTi		Implemented new way of handling rights
//					in this GUI.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
// 2004-08-26	JTS, RTi		The on/off column again has a combo box
//					from which users can choose values.
// 2004-10-27	SAM, RTi		Use the new table model that is specific
//					to diversion rights.
// 2006-03-05	SAM, RTi		Increase the width slighthly to better
//					accomodate all columns at startup.
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
import DWR.StateMod.StateMod_DiversionRight;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;

import RTi.Util.GUI.ResponseJDialog;

import RTi.Util.Help.URLHelp;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class is a gui for displaying and editing diversion rights.
*/
public class StateMod_Diversion_Right_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Button labels.
*/
private final String 
	__BUTTON_ADD_RIGHT = "Add right",
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_DEL_RIGHT = "Delete right",
	__BUTTON_HELP = "Help";

/**
Whether the form data can be edited or not.
*/
private boolean __editable = false;

/**
GUI Buttons.
*/
private JButton 
	__addRight,
	__closeJButton,
	__deleteRight,
	__helpJButton;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet for displaying diversion right data.
*/
private JWorksheet __worksheet;

/**
The current diversion for which the rights are being shown.
*/
private StateMod_Diversion __currentDiv;

private StateMod_DataSet __dataset;

/**
Constructor.
@param dataset the dataset in which the data are contained.
@param div the diversion to display.
@param editable whether the form data can be edited or not
*/
public StateMod_Diversion_Right_JFrame(StateMod_DataSet dataset, 
StateMod_Diversion div, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, div.getName()
		+ " - Diversion Water Rights", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__currentDiv = div;

	__dataset = dataset;

	__editable = editable;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_Diversion_Right_JFrame::actionPerformed";

	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_ADD_RIGHT)) {
		StateMod_DiversionRight aRight = new StateMod_DiversionRight();
		aRight._isClone = true;
		StateMod_DiversionRight last = 
			(StateMod_DiversionRight)__worksheet.getLastRowData();

		if (last == null) {
			aRight.setID(StateMod_Util.createNewID(
				__currentDiv.getID()));
			aRight.setCgoto(__currentDiv.getID());
		}
		else {
			aRight.setID(StateMod_Util.createNewID(
				last.getID()));
			aRight.setCgoto(last.getCgoto());
		}
		__worksheet.addRow(aRight);
		__worksheet.scrollToLastRow();
		__worksheet.selectLastRow();
		__deleteRight.setEnabled(true);
	}			
	else if (action.equals(__BUTTON_DEL_RIGHT)) {
		int row = __worksheet.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this,
				"Delete Diversion Right",
				"Delete diversion right?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.NO) {
				return;
			}
			__worksheet.cancelEditing();
			__worksheet.deleteRow(row);
			__deleteRight.setEnabled(false);
		}
		else {	
			Message.printWarning(1, routine, 
				"Must select desired right to delete.");
		}
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
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT (JTS - 2003-06-10)
	}
}

/**
Saves the input back into the dataset.
@return true if the data was saved successfuly.  False if not.
*/
private boolean saveData() {
	String routine = "StateMod_Diversion_Right_JFrame.saveData";
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
	Vector dv = __currentDiv.getRights();		// d for diversion

	needToSave = !(StateMod_DiversionRight.equals(wv, dv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	// at this point, remove the old diversion rights from the original
	// component Vector
	Vector diversionRights =(Vector)(__dataset.getComponentForComponentType(
		__dataset.COMP_DIVERSION_RIGHTS)).getData();
	int holdsize1 = diversionRights.size();
	int size = dv.size();
	StateMod_DiversionRight dr;	
	for (int i = 0; i < size; i++) {
		dr = (StateMod_DiversionRight)dv.elementAt(i);
		StateMod_Util.removeFromVector(diversionRights, dr);
	}

	// now add the elements from the new Vector to the diversionRights 
	// Vector.
	size = wv.size();
	StateMod_DiversionRight cdr = null;
	for (int i = 0; i < size; i++) {
		dr = (StateMod_DiversionRight)wv.elementAt(i);
		cdr = (StateMod_DiversionRight)dr.clone();
		cdr._isClone = false;
		diversionRights.add(cdr);
	}

	// sort the diversionRights Vector
	// REVISIT (JTS - 2003-10-10)
	// here we are sorting the full data array -- may be a performance
	// issue
	Vector sorted = StateMod_Util.sortStateMod_DataVector(diversionRights);
	__dataset.getComponentForComponentType(__dataset.COMP_DIVERSION_RIGHTS)
		.setData(sorted);
	__currentDiv.disconnectRights();
	__currentDiv.connectRights(sorted);
	__dataset.setDirty(__dataset.COMP_DIVERSION_RIGHTS, true);
	return true;
}

/**
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput() {
	String routine = "StateMod_Diversion_Right_JFrame.checkInput";
	Vector v = __worksheet.getAllData();

	int size = v.size();
	StateMod_DiversionRight right = null;
	String warning = "";
	String id;
	String name;
	String divID;
	String adminNum;
	int fatalCount = 0;
	for (int i = 0; i < size; i++) {
		right = (StateMod_DiversionRight)(v.elementAt(i));

		id = right.getID();
		name = right.getName();
		divID = right.getCgoto();
		adminNum = right.getIrtem();
	
		if (id.length() > 12) {
			warning += "\nDiversion right ID (" + id + ") is "
				+ "longer than 12 characters.";
			fatalCount++;
		}

		if (id.indexOf(" ") > -1 || id.indexOf("-") > -1) {
			warning += "\nDiversion right ID (" + id + ") cannot "
				+ "contain spaces or dashes.";
			fatalCount++;
		}

		if (name.length() > 24) {
			warning += "\nDiversion name (" + name + ") is "
				+ "longer than 24 characters.";
			fatalCount++;
		}

		if (divID.length() > 12) {
			warning += "\nDiversion ID associated with right ("
				+ divID + ") is longer than 12 characters.";
		}

		if (!StringUtil.isDouble(adminNum)) {
			warning += "\nAdministration number (" + adminNum 
				+ ") is not a number.";
			fatalCount++;
		}
		
		// decreed amount is not checked to be a double because that
		// is enforced by the worksheet and its table model

		// on/off is not checked to be an integer because that is
		// enforced by the worksheet and its table model

	}
	// REVISIT - if daily time series are supplied, check for time series
	// and allow creation if not available.
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
Checks to see if the __deleteRight button should be enabled or not.
*/
private void checkDeleteRightButton() {
	int row = __worksheet.getSelectedRow();
	if (row == -1) {
		JGUIUtil.setEnabled(__deleteRight, false);
	}
	else {
		JGUIUtil.setEnabled(__deleteRight, true);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__currentDiv = null;
	__worksheet = null;
	__addRight = null;
	__deleteRight = null;
	__closeJButton = null;
	__helpJButton = null;
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
	checkDeleteRightButton();
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
	checkDeleteRightButton();
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
private void setupGUI() {
	String routine = "setupGUI";
	
	addWindowListener(this);
	
	GridBagConstraints gbc = new GridBagConstraints();

	__addRight = new JButton(__BUTTON_ADD_RIGHT);
	__deleteRight = new JButton(__BUTTON_DEL_RIGHT);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__helpJButton = new JButton(__BUTTON_HELP);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);
	__helpJButton.setEnabled(false);

	GridBagLayout gbl = new GridBagLayout();
	JPanel bigPanel = new JPanel();
	bigPanel.setLayout(gbl);

	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
	JPanel p1 = new JPanel();
	p1.setLayout(new FlowLayout(FlowLayout.RIGHT));

	GridLayout gl = new GridLayout(2, 2, 1, 1);
	JPanel info_panel = new JPanel();
	info_panel.setLayout(gl);

	JPanel main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout());

	info_panel.add(new JLabel("Diversion ID: "));
	info_panel.add(new JLabel(__currentDiv.getID()));
	info_panel.add(new JLabel("Diversion name: "));
	info_panel.add(new JLabel(__currentDiv.getName()));
	if (__editable) {
		p1.add(__addRight);
		p1.add(__deleteRight);
		__deleteRight.setEnabled(false);
	}
	//p1.add(__helpJButton);
	p1.add(applyJButton);
	p1.add(cancelJButton);
	p1.add(__closeJButton);

	PropList p = new PropList("StateMod_Diversion_Right_JFrame"
		+ ".JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");
	
	int widths[] = null;
	JScrollWorksheet jsw = null;
	try {	Vector v = new Vector();
		Vector v2 = __currentDiv.getRights();
		for (int i = 0; i < v2.size(); i++) {
			v.add(((StateMod_DiversionRight)
				(v2.elementAt(i))).clone());
		}
		StateMod_DiversionRight_TableModel tmd = new
			StateMod_DiversionRight_TableModel( v, 
			__editable, false);

		StateMod_DiversionRight_CellRenderer crd = new
			StateMod_DiversionRight_CellRenderer(tmd);
		
		jsw = new JScrollWorksheet(crd, tmd, p);
		__worksheet = jsw.getJWorksheet();

		widths = crd.getColumnWidths();		
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);

	Vector v = new Vector();
	v.add("0 - Off");
	v.add("1 - On");

	__worksheet.setColumnJComboBoxValues(
		StateMod_DiversionRight_TableModel.COL_ON_OFF, v);

	main_panel.add(jsw, "Center");
	main_panel.add(p1, "South");

	JGUIUtil.addComponent(bigPanel, info_panel, 0, 0, 1, 1, 0, 0, 
		gbc.NONE, gbc.NORTHWEST);
	JGUIUtil	.addComponent(bigPanel, main_panel, 0, 1, 10, 10, 1, 1, 
		gbc.BOTH, gbc.SOUTH);
		
	getContentPane().add(bigPanel);
		
	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gbl);
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

	__addRight.addActionListener(this);
	__deleteRight.addActionListener(this);
	__closeJButton.addActionListener(this);
	__helpJButton.addActionListener(this);
	cancelJButton.addActionListener(this);
	applyJButton.addActionListener(this);

	pack();
	setSize(600, 450);
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
