// StateMod_Plan_Return_JFrame - GUI for displaying/editing the return flow assignments for a plan.

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

// TODO SAM 2011-01-02 Copied similar reservoir class - both need updated if full editing is enabled.
/**
GUI for displaying/editing the return flow assignments for a plan.
*/
@SuppressWarnings("serial")
public class StateMod_Plan_Return_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Labels for the buttons.
*/
private final static String 
	__BUTTON_ADD_RETURN = "Add Return",
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",		
	__BUTTON_DEL_RETURN = "Delete Return",
	__BUTTON_HELP = "Help",
	__BUTTON_CLOSE = "Close";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
GUI buttons.
*/
private JButton 
	__addReturn_JButton,
	__close_JButton,
	__deleteReturn_JButton,
	__help_JButton;

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
//private StateMod_DataSet __dataset;

/**
The current plan for which returns are being shown.
*/
private StateMod_Plan __currentPlan;

/**
The list of return data to view.  These returns are maintained as a separate data component and not
currently linked to the plan.  This is a different approach than diversion/well returns, mainly
because effort has not been put into the full editing features and "dirty" data need to be separate.
*/
private List<StateMod_ReturnFlow> __currentPlanReturnList = new Vector<StateMod_ReturnFlow>();

/**
Constructor.
@param dataset the dataset in which the data is contained.
@param plan the plan for which to display return information.
@param editable whether the gui data is editable or not.
*/
public StateMod_Plan_Return_JFrame(StateMod_DataSet dataset, StateMod_Plan plan, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, plan.getName() 
		+ " - Plan Return Flow Table Assignment", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__currentPlan = plan;
	@SuppressWarnings("unchecked")
	List<StateMod_ReturnFlow> allReturns = (List<StateMod_ReturnFlow>)dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_PLAN_RETURN).getData();
	__currentPlanReturnList = (List<StateMod_ReturnFlow>)StateMod_Util.getDataList (allReturns,plan.getID());
	Message.printStatus(2,"","Have " + __currentPlanReturnList.size() + " return records for plan \"" +
		__currentPlan.getID() + "\" (selected from full list of size " + allReturns.size() + ")." );
	//__dataset = dataset;
	// TODO SAM 2011-01-02 For now editing is disabled...
	editable = false;
	__editable = editable;
	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_Plan_Return_JFrame.actionPerformed";

	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_ADD_RETURN)) {
		StateMod_ReturnFlow aReturn = new StateMod_ReturnFlow(StateMod_DataSet.COMP_PLAN_RETURN);
		aReturn._isClone = true;
		StateMod_ReturnFlow last = (StateMod_ReturnFlow)__worksheet.getLastRowData();

		if (last == null) {
			aReturn.setID(StateMod_Util.createNewID(__currentPlan.getID()));
			aReturn.setCgoto(__currentPlan.getID());
		}
		else {
			aReturn.setID(StateMod_Util.createNewID(last.getID()));
			aReturn.setCgoto(last.getCgoto());
		}
		__worksheet.scrollToLastRow();
		__worksheet.addRow(aReturn);
		__worksheet.selectLastRow();
		__deleteReturn_JButton.setEnabled(true);
	}
	else if (action.equals(__BUTTON_DEL_RETURN)) {
		int row = __worksheet.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this, "Delete return", "Delete plan return?",
				ResponseJDialog.YES | ResponseJDialog.NO).response();
			if (x == ResponseJDialog.NO) {
				return;
			}

			__worksheet.cancelEditing();
			__worksheet.deleteRow(row);
			__deleteReturn_JButton.setEnabled(false);
		}
		else {	
			Message.printWarning(1, routine, "Must select desired return to delete.");
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
	else if (e.getSource()== __help_JButton) {
		// TODO HELP (JTS - 2003-06-09)
	}
}

// TODO smalers 2019-06-01 evaluate whether needed
/**
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors exist.
*/
@SuppressWarnings("unused")
private int checkInput() {
	String routine = "StateMod_Plan_Return_JFrame.checkInput";
	@SuppressWarnings("unchecked")
	List<StateMod_ReturnFlow> v = (List<StateMod_ReturnFlow>)__worksheet.getAllData();

	int size = v.size();
	StateMod_ReturnFlow aReturn = null;
	String warning = "";
	String id;
	String riverNodeID;
	//double percent;
	//String tableID;
	int fatalCount = 0;
	//String comment;
	for (int i = 0; i < size; i++) {
		aReturn = (StateMod_ReturnFlow)(v.get(i));

		id = aReturn.getID();
		riverNodeID = aReturn.getCrtnid();
		//percent = aReturn.getPcttot();
		//tableID = "" + aReturn.getIrtndl();
		//comment = aReturn.getComment();
	
		// TODO SAM 2011-01-02 Need to implement validators
		if (id.length() > 12) {
			warning += "\nPlan ID (" + id + ") is longer than 12 characters.";
			fatalCount++;
		}

		if (id.indexOf(" ") > -1 || id.indexOf("-") > -1) {
			warning += "\nPlan ID (" + id + ") cannot contain spaces or dashes.";
			fatalCount++;
		}

		if (riverNodeID.length() > 12) {
			warning += "River node ID (" + riverNodeID + ") is longer than 12 characters.";
		}
	}

	if ( warning.length() > 0 ) {
		warning += "\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, warning, this );
		if ( fatalCount > 0 ) {
			// Fatal errors...
			Message.printStatus ( 1, routine, "Returning 1 from checkInput()" );
			return 1;
		}
		else {
			// Nonfatal errors...
			Message.printStatus ( 1, routine, "Returning -1 from checkInput()" );
			return -1;
		}
	}
	else {
		// No errors...
		Message.printStatus ( 2, routine, "Returning 0 from checkInput()" );
		return 0;
	}
}

/**
Saves the input back into the dataset.
@return true if the data was saved successfully.  False if not.
*/
private boolean saveData() {
	//String routine = "StateMod_Plan_Return_JFrame.saveData";
	/* TODO SAM 2011-01-02 Enable - for now no editing is allowed
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

	// if the lists are differently-sized, they're different
	List wv = __worksheet.getAllData(); // w for worksheet
	List rv = __currentRes.getRights();	// i for instream flow

	needToSave = !(StateMod_ReservoirRight.equals(wv, rv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	// At this point, remove the old diversion rights from the original component list
	List reservoirRights =(List)(__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_RIGHTS)).getData();
	int size = rv.size();
	StateMod_ReservoirRight ir;
	for (int i = 0; i < size; i++) {
		ir = (StateMod_ReservoirRight)rv.get(i);
		StateMod_Util.removeFromVector(reservoirRights, ir);
	}

	// Now add the elements from the new list to the reservoirReturns list.
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
	List sorted=StateMod_Util.sortStateMod_DataVector(reservoirRights);
	__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_RIGHTS)
		.setData(sorted);
	__currentRes.disconnectRights();
	__currentRes.connectRights(sorted);
	__dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
	*/
	return true;
}

/**
Checks to see if the __deleteReturn button should be enabled or not.
*/
private void checkDeleteReturnButton() {
	int row = __worksheet.getSelectedRow();
	if ( __editable ) {
		if (row == -1) {
			__deleteReturn_JButton.setEnabled(false);
		}
		else {
			__deleteReturn_JButton.setEnabled(true);
		}
	}
	else {
		__deleteReturn_JButton.setEnabled(false);
	}
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; checks if the __deleteRight button should be enabled.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	checkDeleteReturnButton();
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
Responds to mouse released events; checks to see if the __deleteRight button should be enabled or not.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	checkDeleteReturnButton();
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

	PropList p = new PropList("StateMod_Plan_Return_JFrame.JWorksheet");
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
		/* TODO SAM 2011-01-02 Comment out - might allow something similar if editing is enabled
		 * and choices of IDs are provided
		List accounts = __currentRes.getAccounts();
		List v3 = new Vector();
		int size = accounts.size();
		StateMod_ReservoirAccount ra = null;
		for (int i = 0; i < size; i++) {
			ra = (StateMod_ReservoirAccount)accounts.get(i);
			v3.add("" + ra.getID() + " - " + ra.getName());
		}
		for (int i = 1; i < size; i++) {
			v3.add("-" + (i + 1) + " - Fill first " + (i + 1) 
				+ " accounts");
		}

		List v = new Vector();
		List v2 = __currentRes.getRights();
		StateMod_ReservoirRight rr;
		for (int i = 0; i < v2.size(); i++) {
			rr = (StateMod_ReservoirRight)
				((StateMod_ReservoirRight)v2.get(i))
				.clone();
			v.add(rr);
		}
		*/
		// Get the list of all returns and filter for this reservoir
		// TODO SAM 2011-01-02 The code needs to use a table model with lists if editing is enabled
		StateMod_Plan_Return_Data_TableModel tmr =
			new StateMod_Plan_Return_Data_TableModel(__currentPlanReturnList, __editable);
		StateMod_Plan_Return_Data_CellRenderer crr =
			new StateMod_Plan_Return_Data_CellRenderer(tmr);
		
		jsw = new JScrollWorksheet(crr, tmr, p);
		__worksheet = jsw.getJWorksheet();

		/*
		List onOff = StateMod_ReservoirRight.getIrsrswChoices(true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_ON_OFF, onOff,
			false);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_ACCOUNT_DIST,
			v3, false);		
		List rightTypes =
			StateMod_ReservoirRight.getItyrsrChoices(true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_RIGHT_TYPE,
			rightTypes, false);
		List fillTypes=StateMod_ReservoirRight.getN2fillChoices(true);
		__worksheet.setColumnJComboBoxValues(
			StateMod_ReservoirRight_TableModel.COL_FILL_TYPE,
			fillTypes, false);
			*/

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
	
	__addReturn_JButton = new JButton(__BUTTON_ADD_RETURN);
	__deleteReturn_JButton = new JButton(__BUTTON_DEL_RETURN);
	__deleteReturn_JButton.setEnabled(false);
	__help_JButton = new JButton(__BUTTON_HELP);
	__help_JButton.setEnabled(false);
	__close_JButton = new JButton(__BUTTON_CLOSE);
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

	info_panel.add(new JLabel("Plan:"));	
	info_panel.add(new JLabel(__currentPlan.getID()));
	info_panel.add(new JLabel("Plan name:"));	
	info_panel.add(new JLabel(__currentPlan.getName()));

	if (__editable) {
		p1.add(__addReturn_JButton);
		p1.add(__deleteReturn_JButton);
	}	
	p1.add(applyJButton);
	p1.add(cancelJButton);
//	p1.add(__helpJButton);
	p1.add(__close_JButton);
	if ( !__editable ) {
		applyJButton.setEnabled(false);
		applyJButton.setToolTipText ( "Editing plan return data is not implemented." );
		__close_JButton.setEnabled(false);
		__close_JButton.setToolTipText ( "Editing plan return data is not implemented." );
	}

	main_panel.add(jsw, "Center");
	main_panel.add(p1, "South");

	JGUIUtil.addComponent(bigPanel, info_panel,
		0, 0, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(bigPanel, main_panel,
		0, 1, 10, 10, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.SOUTH);
	__addReturn_JButton.addActionListener(this);
	__deleteReturn_JButton.addActionListener(this);
	__help_JButton.addActionListener(this);
	__close_JButton.addActionListener(this);
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
