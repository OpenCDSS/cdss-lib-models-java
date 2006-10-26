//----------------------------------------------------------------------------
// StateMod_Well_Right_JFrame - dialog to edit a well's rights information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 18 Oct 1999	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 2001-12-11	SAM, RTi		Change help key from SMGUI.Well to
//					SMGUI.Wells.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-10	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMwellRightsFrame
// 2003-06-24	JTS, RTi		First functional version.
// 2003-07-15	JTS, RTi		* Added status bar.
//					* Changed to use new dataset design.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-29	SAM, RTi		Update because of changed in
//					StateMod_WellRight.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-13	JTS, RTi		* Worksheet now uses multiple-line
//					  headers.
// 					* Added saveData().
//					* Added checkInput().
//					* Added apply and cancel buttons.
// 2004-01-22	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
// 2004-08-26	JTS, RTi		* On/Off column now has a combo box from
//					  which users can select values.
// 2005-01-21	JTS, RTi		Table model constructor changed.
//------------------------------------------------------------------------------

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

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;

import RTi.Util.Help.URLHelp;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.GUI.JWorksheet_CellAttributes;

import RTi.Util.String.StringUtil;

/**
This class is a gui for displaying and editing well right data.
*/
public class StateMod_Well_Right_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Button labels.
*/
private final String 
	__BUTTON_ADD_RIGHT = 	"Add right",
	__BUTTON_APPLY = 	"Apply",
	__BUTTON_CANCEL = 	"Cancel",			
	__BUTTON_DEL_RIGHT = 	"Delete right",
	__BUTTON_CLOSE = 	"Close",
	__BUTTON_HELP = 	"Help";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
GUI buttons.
*/
private JButton 
	__addRight,
	__deleteRight,
	__closeJButton,
	__helpJButton;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet in which the well right data is displayed.
*/
private JWorksheet __worksheet;

/**
The dataset containing the data.
*/
private StateMod_DataSet __dataset;

/**
The well for which the right data is shown.
*/
private StateMod_Well __currentWell;

/**
Constructor.
@param dataset the dataset in which the data is contained.
@param well the well that is being displayed.
@param editable whether the well data is editable or not
*/
public StateMod_Well_Right_JFrame(StateMod_DataSet dataset, 
StateMod_Well well, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, well.getName()
		+ " - Well Water Rights", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__currentWell = well;

	__dataset = dataset;

	__editable = editable;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_Well_Right_JFrame.actionPerformed";

	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_ADD_RIGHT)) {
		StateMod_WellRight aRight = new StateMod_WellRight();
		aRight._isClone = true;
		StateMod_WellRight last = 
			(StateMod_WellRight)__worksheet.getLastRowData();

		if (last == null) {
			aRight.setID(StateMod_Util.createNewID(
				__currentWell.getID()));
			aRight.setCgoto(__currentWell.getID());
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
				"Delete right", "Delete well right?",
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
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput() {
	String routine = "StateMod_Well_Right_JFrame.checkInput";
	Vector v = __worksheet.getAllData();

	int size = v.size();
	StateMod_WellRight right = null;
	String warning = "";
	String id;
	String name;
	String wellID;
	String adminNum;
	int fatalCount = 0;
	for (int i = 0; i < size; i++) {
		right = (StateMod_WellRight)(v.elementAt(i));

		id = right.getID();
		name = right.getName();
		wellID = right.getCgoto();
		adminNum = right.getIrtem();
	
		if (id.length() > 12) {
			warning += "\nWell right ID (" + id + ") is "
				+ "longer than 12 characters.";
			fatalCount++;
		}

		if (id.indexOf(" ") > -1 || id.indexOf("-") > -1) {
			warning += "\nWell right ID (" + id + ") cannot "
				+ "contain spaces or dashes.";
			fatalCount++;
		}

		if (name.length() > 24) {
			warning += "\nWell name (" + name + ") is "
				+ "longer than 24 characters.";
			fatalCount++;
		}

		if (wellID.length() > 12) {
			warning += "\nWell ID associated with right ("
				+ wellID + ") is longer than 12 characters.";
		}

		if (!StringUtil.isDouble(adminNum)) {
			warning += "\nAdministration number (" + adminNum 
				+ ") is not a number.";
			fatalCount++;
		}
		
		// the rest are handled by the worksheet
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
Saves the input back into the dataset.
@return true if the data was saved successfuly.  False if not.
*/
private boolean saveData() {
	String routine = "StateMod_Well_Right_JFrame.saveData";
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
	Vector lv = __currentWell.getRights();		// l for welL

	needToSave = !(StateMod_WellRight.equals(wv, lv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	// at this point, remove the old diversion rights from the original
	// component Vector
	Vector wellRights = (Vector)(__dataset.getComponentForComponentType(
		__dataset.COMP_WELL_RIGHTS)).getData();
	int holdsize1 = wellRights.size();
	int size = lv.size();
	StateMod_WellRight wr;
	for (int i = 0; i < size; i++) {
		wr = (StateMod_WellRight)lv.elementAt(i);
		StateMod_Util.removeFromVector(wellRights, wr);
	}

	// now add the elements from the new Vector to the wellRights 
	// Vector.
	size = wv.size();
	Vector clone = new Vector();
	StateMod_WellRight cwr = null;
	for (int i = 0; i < size; i++) {
		wr = (StateMod_WellRight)wv.elementAt(i);
		cwr = (StateMod_WellRight)wr.clone();
		cwr._isClone = false;
		wellRights.add(cwr);
	}

	// sort the wellRights Vector
	// REVISIT (JTS - 2003-10-10)
	// here we are sorting the full data array -- may be a performance
	// issue
	Vector sorted = StateMod_Util.sortStateMod_DataVector(wellRights);
	__dataset.getComponentForComponentType(__dataset.COMP_WELL_RIGHTS)
		.setData(sorted);
	__currentWell.disconnectRights();
	__currentWell.connectRights(sorted);
	__dataset.setDirty(__dataset.COMP_WELL_RIGHTS, true);
	return true;
}

/**
Checks to see if the __deleteRight button should be enabled or not.
*/
private void checkDeleteRightButton() {
	int row = __worksheet.getSelectedRow();
	if (row == -1) {
		__deleteRight.setEnabled(false);
	}
	else {
		__deleteRight.setEnabled(true);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__addRight = null;
	__deleteRight = null;
	__closeJButton = null;
	__helpJButton = null;
	__worksheet = null;
	__currentWell = null;

	super.finalize();
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; checks if the __deleteRight button should
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
Responds to mouse released events; checks to see if the __deleteRight button
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
	__deleteRight.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__helpJButton = new JButton(__BUTTON_HELP);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);		

	GridBagLayout gb = new GridBagLayout();
	JPanel mainJPanel = new JPanel();
	mainJPanel.setLayout(gb);

	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel p1 = new JPanel();
	p1.setLayout(fl);

	GridLayout gl = new GridLayout(2, 2, 1, 1);
	JPanel info_panel = new JPanel();
	info_panel.setLayout(gl);

	JPanel main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout());

	info_panel.add(new JLabel("Well: "));
	info_panel.add(new JLabel(__currentWell.getID()));
	info_panel.add(new JLabel("Well name: "));
	info_panel.add(new JLabel(__currentWell.getName()));

	if (__editable) {
		p1.add(__addRight);
		p1.add(__deleteRight);
	}
	p1.add(applyJButton);
	p1.add(cancelJButton);			
//	p1.add(__helpJButton);
	p1.add(__closeJButton);

	PropList p = 
		new PropList("StateMod_Well_Right_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int widths[] = null;
	JScrollWorksheet jsw = null;
	try {	
		Vector v = new Vector();
		Vector v2 = __currentWell.getRights();
		for (int i = 0; i < v2.size(); i++) {
			v.add(((StateMod_WellRight)
				(v2.elementAt(i))).clone());
		}			
		StateMod_WellRight_TableModel tmw = new
			StateMod_WellRight_TableModel( v, __editable, true);
		StateMod_WellRight_CellRenderer crw = new
			StateMod_WellRight_CellRenderer(tmw);
		
		jsw = new JScrollWorksheet(crw, tmw, p);
		__worksheet = jsw.getJWorksheet();

		widths = crw.getColumnWidths();		
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

	Vector v = new Vector();
	v.add("0 - Off");
	v.add("1 - On");
	__worksheet.setColumnJComboBoxValues(
			StateMod_WellRight_TableModel.COL_ON_OFF, v);

	main_panel.add(jsw, "Center");
	main_panel.add(p1, "South");

	// assemble window from parts
	JGUIUtil.addComponent(mainJPanel, info_panel, 
		0, 0, 1, 1, 0, 0, 
		gbc.NONE, gbc.NORTHWEST);

	JGUIUtil.addComponent(mainJPanel, main_panel, 
		0, 1, 10, 10, 1, 1, 
		gbc.BOTH, gbc.SOUTH);
	__addRight.addActionListener(this);
	__deleteRight.addActionListener(this);
	__closeJButton.addActionListener(this);
	__helpJButton.addActionListener(this);
	__helpJButton.setEnabled(false);
	applyJButton.addActionListener(this);
	cancelJButton.addActionListener(this);	

	getContentPane().add(mainJPanel);

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
	setSize(670, 400);
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
