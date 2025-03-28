// StateMod_Reservoir_Right_JFrame - dialog to edit a reservoir's rights information

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

25u should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

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
import RTi.Util.String.StringUtil;

/**
This class is a gui for displaying the rights associated with a reservoir, 
as well as deleting and adding rights with that reservoir.
*/
@SuppressWarnings("serial")
public class StateMod_Reservoir_Right_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Labels for the buttons.
*/
private final static String 
	__BUTTON_ADD_RIGHT = 		"Add right",
	__BUTTON_APPLY = 		"Apply",
	__BUTTON_CANCEL = 		"Cancel",		
	__BUTTON_DEL_RIGHT = 		"Delete right",
	__BUTTON_HELP = 		"Help",
	__BUTTON_CLOSE = 		"Close";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
GUI buttons.
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
Worksheet in which the rights are shown.
*/
private JWorksheet __worksheet;

/**
Dataset that contains the data.
*/
private StateMod_DataSet __dataset;

/**
The current reservoir for which rights are being shown.
*/
private StateMod_Reservoir __currentRes;

/**
Constructor.
@param dataset the dataset in which the data is contained.
@param res the Reservoir about which to display right information.
@param editable whether the gui data is editable or not.
*/
public StateMod_Reservoir_Right_JFrame(StateMod_DataSet dataset,
StateMod_Reservoir res, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, res.getName() 
		+ " - Reservoir Water Rights", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__currentRes = res;

	__dataset = dataset;

	__editable = editable;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_Reservoir_Right_JFrame::actionPerformed";

	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_ADD_RIGHT)) {
		StateMod_ReservoirRight aRight = new StateMod_ReservoirRight();
		aRight._isClone = true;
		StateMod_ReservoirRight last = 
			(StateMod_ReservoirRight)__worksheet.getLastRowData();

		if (last == null) {
			aRight.setID(StateMod_Util.createNewID(
				__currentRes.getID()));
			aRight.setCgoto(__currentRes.getID());
		}
		else {
			aRight.setID(StateMod_Util.createNewID(
				last.getID()));
			aRight.setCgoto(last.getCgoto());
		}
		__worksheet.scrollToLastRow();
		__worksheet.addRow(aRight);
		__worksheet.selectLastRow();
		__deleteRight.setEnabled(true);
	}
	else if (action.equals(__BUTTON_DEL_RIGHT)) {
		int row = __worksheet.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this,
				"Delete right", "Delete reservoir right?",
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
	else if (e.getSource()== __helpJButton) {
		// REVISIT HELP (JTS - 2003-06-09)
	}
}

/**
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput() {
	String routine = "StateMod_Reservoir_Right_JFrame.checkInput";
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirRight> v = (List<StateMod_ReservoirRight>)__worksheet.getAllData();

	int size = v.size();
	StateMod_ReservoirRight right = null;
	String warning = "";
	String id;
	String name;
	String resID;
	String adminNum;
	int fatalCount = 0;
	for (int i = 0; i < size; i++) {
		right = (StateMod_ReservoirRight)(v.get(i));

		id = right.getID();
		name = right.getName();
		resID = right.getCgoto();
		adminNum = right.getRtem();
		//copID = right.getCopid();
	
		if (id.length() > 12) {
			warning += "\nReservoir right ID (" + id + ") is "
				+ "longer than 12 characters.";
			fatalCount++;
		}

		if (id.indexOf(" ") > -1 || id.indexOf("-") > -1) {
			warning += "\nReservoir right ID (" + id + ") cannot "
				+ "contain spaces or dashes.";
			fatalCount++;
		}

		if (name.length() > 24) {
			warning += "\nReservoir name (" + name + ") is "
				+ "longer than 24 characters.";
			fatalCount++;
		}

		if (resID.length() > 12) {
			warning += "\nReservoir ID associated with right ("
				+ resID + ") is longer than 12 characters.";
		}

		if (!StringUtil.isDouble(adminNum)) {
			warning += "\nAdministration number (" + adminNum 
				+ ") is not a number.";
			fatalCount++;
		}
		
		/* REVISIT SAM 2004-10-29 These don't need to be checked
		because they are enforced by the table model?

		switchx = right.getSwitch();
		iresco = right.getIresco();
		ityrstr = right.getItyrstr();
		n2fill = right.getN2fill();

		if (switchx == null) {
			warning += "\nMust fill in On/Off switch.";
			fatalCount++;
		}
		if (iresco == null) {
			warning += "\nMust fill in Accounts served.";
			fatalCount++;
		}
		if (ityrstr == null) {
			warning += "\nMust fill in Right type.";
			fatalCount++;
		}
		if (n2fill == null) {
			warning += "\nMust fill in Fill type.";
			fatalCount++;
		}
		*/
		
		// copid is not handled yet
		
		// decreed amount is not checked to be a double because that
		// is enforced by the worksheet and its table model
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
	String routine = "StateMod_Reservoir_Right_JFrame.saveData";
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
	List<StateMod_ReservoirRight> wv = (List<StateMod_ReservoirRight>)__worksheet.getAllData();	// w for worksheet
	List<StateMod_ReservoirRight> rv = __currentRes.getRights();	// i for reservoir

	needToSave = !(StateMod_ReservoirRight.equals(wv, rv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	// at this point, remove the old diversion rights from the original
	// component list
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirRight> reservoirRights = (List<StateMod_ReservoirRight>)(__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_RIGHTS)).getData();
	int size = rv.size();
	StateMod_ReservoirRight ir;
	for (int i = 0; i < size; i++) {
		ir = (StateMod_ReservoirRight)rv.get(i);
		StateMod_Util.removeFromVector(reservoirRights, ir);
	}

	// now add the elements from the new Vector to the reservoirRights 
	// Vector.
	size = wv.size();
	StateMod_ReservoirRight cdr = null;

	for (int i = 0; i < size; i++) {
		ir = (StateMod_ReservoirRight)wv.get(i);
		cdr = (StateMod_ReservoirRight)ir.clone();
		cdr._isClone = false;
		reservoirRights.add(cdr);
	}

	// sort the reservoirRights Vector
	// REVISIT (JTS - 2003-10-10)
	// here we are sorting the full data array -- may be a performance
	// issue
	List<StateMod_ReservoirRight> sorted = StateMod_Util.sortStateMod_DataVector(reservoirRights);
	__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_RIGHTS)
		.setData(sorted);
	__currentRes.disconnectRights();
	__currentRes.connectRights(sorted);
	__dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
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
public void setupGUI() {
	String routine = "setupGUI";

	addWindowListener(this);

	PropList p =
		new PropList("StateMod_Reservoir_JFrame.JWorksheet");
	p.add("JWorksheet.AllowCopy=true");		
	p.add("JWorksheet.CellFont=Courier");
	p.add("JWorksheet.CellStyle=Plain");
	p.add("JWorksheet.CellSize=11");
	p.add("JWorksheet.HeaderFont=Arial");
	p.add("JWorksheet.HeaderStyle=Plain");
	p.add("JWorksheet.HeaderSize=11");
	p.add("JWorksheet.HeaderBackground=LightGray");
	p.add("JWorksheet.RowColumnPresent=false");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");
	
	int widths[] = null;
	JScrollWorksheet jsw = null;
	try {	
		List<StateMod_ReservoirAccount> accounts = __currentRes.getAccounts();
		List<String> v3 = new Vector<String>();
		int size = accounts.size();
		StateMod_ReservoirAccount ra = null;
		for (int i = 0; i < size; i++) {
			ra = accounts.get(i);
			v3.add("" + ra.getID() + " - " + ra.getName());
		}
		for (int i = 1; i < size; i++) {
			v3.add("-" + (i + 1) + " - Fill first " + (i + 1) 
				+ " accounts");
		}

		List<StateMod_ReservoirRight> v = new Vector<StateMod_ReservoirRight>();
		List<StateMod_ReservoirRight> v2 = __currentRes.getRights();
		StateMod_ReservoirRight rr;
		for (int i = 0; i < v2.size(); i++) {
			rr = (StateMod_ReservoirRight)v2.get(i).clone();
			v.add(rr);
		}		
		StateMod_ReservoirRight_TableModel tmr = new
			StateMod_ReservoirRight_TableModel(
			v, __editable);
		StateMod_ReservoirRight_CellRenderer crr = new
			StateMod_ReservoirRight_CellRenderer(tmr);
		
		jsw = new JScrollWorksheet(crr, tmr, p);
		__worksheet = jsw.getJWorksheet();

		List<String> onOff = StateMod_ReservoirRight.getIrsrswChoices(true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_ON_OFF, onOff,
			false);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_ACCOUNT_DIST,
			v3, false);		
		List<String> rightTypes =
			StateMod_ReservoirRight.getItyrsrChoices(true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_RIGHT_TYPE,
			rightTypes, false);
		List<String> fillTypes=StateMod_ReservoirRight.getN2fillChoices(true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_FILL_TYPE,
			fillTypes, false);

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
	
	__addRight = new JButton(__BUTTON_ADD_RIGHT);
	__deleteRight = new JButton(__BUTTON_DEL_RIGHT);
	__deleteRight.setEnabled(false);
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

	info_panel.add(new JLabel("Reservoir:"));	
	info_panel.add(new JLabel(__currentRes.getID()));
	info_panel.add(new JLabel("Reservoir name:"));	
	info_panel.add(new JLabel(__currentRes.getName()));

	if (__editable) {
		p1.add(__addRight);
		p1.add(__deleteRight);
	}	
	p1.add(applyJButton);
	p1.add(cancelJButton);		
//	p1.add(__helpJButton);
	p1.add(__closeJButton);

	main_panel.add(jsw, "Center");
	main_panel.add(p1, "South");

	JGUIUtil.addComponent(bigPanel, info_panel,
		0, 0, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(bigPanel, main_panel,
		0, 1, 10, 10, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.SOUTH);
	__addRight.addActionListener(this);
	__deleteRight.addActionListener(this);
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
	setSize(760, 400);
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
