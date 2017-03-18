//------------------------------------------------------------------------------
// StateMod_Reservoir_Owner_JFrame - dialog to edit a reservoir's 
//	ownership information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 24 Dec 1997	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 25 Feb 1998	CEN, RTi		Added header information
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
//------------------------------------------------------------------------------
// 2003-06-09	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMresOwnersFrame.
// 2003-06-16	JTS, RTi		Javadoc'd.
// 2003-07-15	JTS, RTi		* Added status bar.
//					* Changed to use new dataset design.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-29	SAM, RTi		Update due to changes in
//					StateMod_Reservoir.
// 2003-09-19	JTS, RTi		Account ID is automatically generated
//					upon adding a new account.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-13	JTS, RTi		* Worksheet now uses multiple-line
//					  headers.
// 					* Added saveData().
//					* Added checkInput().
//					* Added apply and cancel buttons.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		Changed layout of buttons to be
//					aligned in the lower-right.
// 2004-10-28	SAM, RTi		Use the table model specific to account
//					data.
// 2005-01-21	JTS, RTi		Table model constructor changed.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
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
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class displays reservoir owner account information and allows 
owner accounts to be added or deleted from the current reservoir.
*/
@SuppressWarnings("serial")
public class StateMod_Reservoir_Owner_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Strings to be displayed on the action buttons.
*/
private final static String 
	__BUTTON_ADD_OWNER = 	"Add owner",
	__BUTTON_APPLY = 	"Apply",
	__BUTTON_CANCEL = 	"Cancel",		
	__BUTTON_DEL_OWNER = 	"Delete owner",
	__BUTTON_HELP = 	"Help",
	__BUTTON_CLOSE = 	"Close";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
GUI Buttons.
*/
private JButton 
	__addOwner,
	__closeJButton,
	__deleteOwner,
	__helpJButton;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
JWorksheet to display owner data.
*/
private JWorksheet __worksheet;

/**
The dataset in which the data is stored.
*/
private StateMod_DataSet __dataset;

/**
The current reservoir for which data will be displayed.
*/
private StateMod_Reservoir __currentRes = null;

/**
Constructor.
@param dataset the dataset in which the data is contained.
@param res the reservoir for which to display data
@param editable whether the gui data is editable or not.
*/
public StateMod_Reservoir_Owner_JFrame(StateMod_DataSet dataset, 
StateMod_Reservoir res, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, res.getName()
		+ " - Reservoir Owner Accounts", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__currentRes = res;

	__dataset = dataset;

	__editable = editable;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that occurred.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();

	if (action.equals(__BUTTON_ADD_OWNER)) {
		StateMod_ReservoirAccount anAccount =
			new StateMod_ReservoirAccount();
		anAccount._isClone = true;
		int rowCount = __worksheet.getRowCount();
		if (rowCount == 0) {
			anAccount.setID(1);
		}
		else {
			StateMod_ReservoirAccount lastAccount = 
				(StateMod_ReservoirAccount)
				__worksheet.getLastRowData();
			String id = lastAccount.getID();
			anAccount.setID("" +((new Integer(id)).intValue() + 1));
		}
		__worksheet.addRow(anAccount);
		__worksheet.scrollToLastRow();					
		__worksheet.selectLastRow();
		__deleteOwner.setEnabled(true);
	}
	else if (action.equals(__BUTTON_DEL_OWNER)) {
		int row = __worksheet.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this,
				"Delete owner",
				"Delete owner?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.NO) {
				return;
			}
			__worksheet.deleteRow(row);
			__deleteOwner.setEnabled(false);
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
		// REVISIT HELP (JTS - 2003-06-09)
	}
}

/**
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput() {
	String routine = "StateMod_Reservoir_Owner_JFrame.checkInput";
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirAccount> v = (List<StateMod_ReservoirAccount>)__worksheet.getAllData();

	int size = v.size();
	StateMod_ReservoirAccount acct = null;
	String warning = "";
	String id;
	String name;
	int fatalCount = 0;
	int lastID = 0;
	int currID = 0;

	if (size > 0) {
		acct = v.get(0);

		id = acct.getID();
		if (!id.trim().equals("1")) {
			warning += "\nThe first reservoir account must have "
				+ "an ID of '1', not '" + id + "'";
			fatalCount++;
		}
	}
	
	for (int i = 0; i < size; i++) {
		acct = v.get(i);

		id = acct.getID();

		if (i == 0) {
			lastID = (new Integer(id)).intValue();
		}
		else {
			currID = (new Integer(id)).intValue();
			if (currID > (lastID + 1)) {
				warning += "\nOwner ID values must be "
					+ "consecutive (row #" + (i) + " is " 
					+ lastID + ", row #" + (i + 1) + " is "
					+ currID + ")";
				fatalCount++;
			}
			lastID = currID;
		}
		
		name = acct.getName();
	
		if (id.length() > 12) {
			warning += "\nReservoir acct ID (" + id + ") is "
				+ "longer than 12 characters.";
			fatalCount++;
		}

		if (id.indexOf(" ") > -1 || id.indexOf("-") > -1) {
			warning += "\nReservoir acct ID (" + id + ") cannot "
				+ "contain spaces or dashes.";
			fatalCount++;
		}

		if (name.length() > 24) {
			warning += "\nReservoir name (" + name + ") is "
				+ "longer than 24 characters.";
			fatalCount++;
		}

		/* REVISIT SAM 2004-10-29 should be enforced by the table
		model since it is a choice
		ownerTie = acct.getN2owns();
		if (ownerTie == null) {
			warning += "\nMust fill in Ownership Tie.";
			fatalCount++;
		}
		*/

		// the rest are handled automatically by the worksheet
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
	String routine = "StateMod_Reservoir_Owner_JFrame.saveData";
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
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirAccount> wv = (List<StateMod_ReservoirAccount>)__worksheet.getAllData();		// w for worksheet
	List<StateMod_ReservoirAccount> rv = __currentRes.getAccounts();	// i for instream flow

	needToSave = !(StateMod_ReservoirAccount.equals(wv, rv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	// now add the elements from the new Vector to the reservoirRights 
	// Vector.
	int size = wv.size();
	List<StateMod_ReservoirAccount> clone = new Vector<StateMod_ReservoirAccount>();
	StateMod_ReservoirAccount ra;
	for (int i = 0; i < size; i++) {
		ra = (StateMod_ReservoirAccount)wv.get(i).clone();
		clone.add(ra);	
	}

	__currentRes.setAccounts(clone);
	__dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
	return true;
}

/**
Checks to see if the __deleteOwner button should be enabled or not.
*/
private void checkDeleteOwnerButton() {
	int row = __worksheet.getSelectedRow();
	if (row == -1) {
		__deleteOwner.setEnabled(false);
	}
	else {
		__deleteOwner.setEnabled(true);
	}
}

/**
Helper method used when data is put into the table model.
@param n2own the value of n2own
@return a String to display in the gui for n2own.
*/
/* TODO SAM 2007-03-01 Evaluate use
private String fillN2owns(int n2own) {
	if (n2own == 1) {
		return "1 - To First Fill Right(s)";
	}
	else {
		return "2 - To Second Fill Right(s)";
	}
}
*/

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__currentRes = null;
	__worksheet = null;
	__addOwner = null;
	__deleteOwner = null;
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
Responds to key released events; checks if the __deleteOwner button should
be enabled.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	checkDeleteOwnerButton();
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
Responds to mouse released events; checks to see if the __deleteOwner button
should be enabled or not.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	checkDeleteOwnerButton();
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

	__addOwner = new JButton(__BUTTON_ADD_OWNER);
	__deleteOwner = new JButton(__BUTTON_DEL_OWNER);
	__deleteOwner.setEnabled(false);
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);		

	GridBagLayout gb = new GridBagLayout();
	JPanel bigPanel = new JPanel();	
	bigPanel.setLayout(gb);

	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel p1 = new JPanel();
	p1.setLayout(fl);

	GridLayout gl = new GridLayout(2, 2, 1, 1);
	JPanel info_panel = new JPanel();
	info_panel.setLayout(gl);

	JPanel main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout());

	info_panel.add(new JLabel("Reservoir ID:"));	
	info_panel.add(new JLabel(__currentRes.getID()));
	info_panel.add(new JLabel("Reservoir name:"));	
	info_panel.add(new JLabel(__currentRes.getName()));

	if (__editable) {
		p1.add(__addOwner);
		p1.add(__deleteOwner);
	}
	p1.add(applyJButton);
	p1.add(cancelJButton);			
//	p1.add(__helpJButton);
	p1.add(__closeJButton);

	PropList p =
		new PropList("StateMod_Reservoir_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int widths[] = null;
	JScrollWorksheet jsw = null;
	try {	
		List<StateMod_ReservoirAccount> v = new Vector<StateMod_ReservoirAccount>();
		List<StateMod_ReservoirAccount> v2 = __currentRes.getAccounts();
		StateMod_ReservoirAccount ra;
		for (int i = 0; i < v2.size(); i++) {
			ra = (StateMod_ReservoirAccount)v2.get(i).clone();
			v.add(ra);
		}			
		StateMod_ReservoirAccount_TableModel tmr = new
			StateMod_ReservoirAccount_TableModel(v, __editable, 
			true);
		StateMod_ReservoirAccount_CellRenderer crr = new
			StateMod_ReservoirAccount_CellRenderer(tmr);
		
		jsw = new JScrollWorksheet(crr, tmr, p);
		__worksheet = jsw.getJWorksheet();

		List<String> owner = StateMod_ReservoirAccount.getN2ownChoices (true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirAccount_TableModel.COL_OWNERSHIP_TIE,
			owner, false);

		widths = crr.getColumnWidths();		
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
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(bigPanel, main_panel, 0, 1, 10, 10, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.SOUTH);
	__addOwner.addActionListener(this);
	__deleteOwner.addActionListener(this);
	__helpJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	applyJButton.addActionListener(this);
	cancelJButton.addActionListener(this);	

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
	setSize(700, 400);
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
