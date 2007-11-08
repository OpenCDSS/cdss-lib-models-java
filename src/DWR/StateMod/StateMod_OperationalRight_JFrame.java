//------------------------------------------------------------------------------
// StateMod_OperationalRight_JFrame - JFrame to edit the operational rights 
//	information.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 12 Jan 1998	Catherine E. 		Created initial version of class
//		Nutting-Lane, RTi
// 22 Sep 1998	CEN, RTi		Changed list to multilist
// 08 Mar 2000	CEN, Rti		Added radio button to search
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 15 Aug 2001	SAM, RTi		Select the first item automatically.
//------------------------------------------------------------------------------
// 2003-06-24	J. Thomas Sapienza, RTi	Initial Swing version.
// 2003-07-15	JTS, RTi		* Added checkInput() framework for 
//					validating user input prior to the 
//					values being saved.
// 					* Added status bar.
//					* Changed to use new dataset design.
// 2003-07-16	JTS, RTi		Added constructor that allows an
//					operational right to be initially
//					selected.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		* isMissing(), indexOf() methods are now
//					  in StateMod_Util.
//					* Force title parameter in constructor.
// 2003-08-16	SAM, RTi		Change the window type to
//					WINDOW_OPERATIONAL_RIGHT.
// 2003-08-25	SAM, RTi		Change global operational right types
//					array from oprightsOptions to TYPES.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-08-27	JTS, RTi		Added selectID() to select an ID 
//					on the worksheet from outside the GUI.
// 2003-09-16	SAM, RTi		Update becuase of changes in
//					StateMod_OperationalRight to handle
//					types up to 23.
// 2003-09-17	JTS, RTi		Rule type option now displays the
//					the value of the rule.
// 2003-09-17	JTS, RTI		Began implementing new code for
//					destination, source and constraint
//					boxes.
// 2003-09-18	JTS, RTI		* Continued work on the destination, 
//					  source, and constaint boxes.
//					* Changed the title-handling code.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		* For data changes, enabled the
//					  Apply and Cancel buttons through new
//					  methods in the data classes.
//					* Changed layout of buttons to be
//					  aligned in the lower-right.
// 					* windowDeactivated() no longer saves
//					  data as it was causing problems with
//					  the cancel code.
// 2006-01-19	JTS, RTi		* Now implements JWorksheet_SortListener
//					* Reselects the record that was selected
//					  when the worksheet is sorted.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_SortListener;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a gui for displaying and editing operational right data.
*/
public class StateMod_OperationalRight_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener,
JWorksheet_SortListener {

/**
Button labels.
*/
private final String 
	__BUTTON_APPLY = "Apply",
	__BUTTON_ADD = "Add",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_DELETE = "Delete",
	__BUTTON_HELP =  "Help";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
Button group for search criteria settings.
*/
private ButtonGroup __searchCriteriaGroup;

/**
The index in __disables[] of textfields that should NEVER be made
editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
Index of the last-selected and currently-selected operational right.
*/
private int 
	__currentOpRightsIndex,
	__lastOpRightsIndex;

/**
Stores the index of the record that was selected before the worksheet is sorted,
in order to reselect it after the sort is complete.
*/
private int __sortSelectedRow = -1;

/**
GUI buttons.
*/
private JButton
	__findNextOpr,
	__helpJButton,
	__closeJButton,
	__cancelJButton,
	__applyJButton;

/**
Array of JComponents that should be disabled when nothing is selected 
from the list.
*/
private JComponent[] __disables;

/**
GUI panels.
*/
private JPanel 
	__additionalPanel,
	__opr8Panel,
	__opr20Panel,
	__opSwitchPanel,
	__qdebtPanel,
	__gridPanel;

/**
Radio buttons for selecting the kind of search to do.
*/
private JRadioButton
	__searchIDJRadioButton,
	__searchNameJRadioButton;

/**
GUI text fields.
*/
private JTextField 
	__oprLocation,
	__oprName,
	__oprStationID;

/**
Status bar textfields 
*/
private JTextField 
	__opr20Sjmina,
	__opr20Sjrela,
	__qdebt,
	__qdebtx,
	__messageJTextField,
	__statusJTextField;

/**
GUI textfields to enter the value on which to search the worksheet.
*/
private JTextField
	__searchID,
	__searchName;

/**
The worksheet for displaying the list of operational rights.
*/
private JWorksheet __worksheet;
/**
The worksheet for displaying specific data about a selected operational right.
*/
private JWorksheet __opRightWorksheet;

/**
Combobox for holding the rule type switch.
*/
private SimpleJComboBox 
	__opr8JComboBox,
	__destination,
	__destinationAccount,
	__source1,
	__source2,
	__source3,
	__source4,
	__source5,
	__sourceAccount1,
	__sourceAccount2,
	__sourceAccount3,
	__sourceAccount4,
	__sourceAccount5,
	__ruleTypeSwitch,
	__oprSwitch;

/**
Combobox array for holding month switches.
*/
private SimpleJComboBox[] __monthSwitch;

/**
The dataset of data for the StateMod run.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The DataSetComponent that contains the operational rights data.
*/
private DataSetComponent __operationalRightsComponent;

/**
Vector of operational rights data.
*/
private Vector __operationalRights;

private int __currentItyopr = -1;

/**
Vectors used to populate combo boxes.  They are only initialized if they need
to be used, and then they are re-used.
*/
private Vector
	__reservoirIDs = null,
	__diversionRightIDs = null,
	__streamGageIDs = null,
	__instreamFlowIDs = null,
	__diversionIDs = null,
	__operationalRightIDs = null;

private Vector
	__reservoirs = null,
	__reservoirRights = null,
	__diversionRights = null,
	__diversions = null,
	__streamGages = null,
	__instreamFlows = null;

/**
Constructor.
@param dataset the StateMod_DataSet object that has the data.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param editable whether the data are editable or not.
*/
public StateMod_OperationalRight_JFrame (	
						StateMod_DataSet dataset,
						StateMod_DataSet_WindowManager
						dataset_wm, boolean editable)
{
	StateMod_GUIUtil.setTitle(this, dataset, "Operational Rights", null);
	__currentOpRightsIndex = -1;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__operationalRightsComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_OPERATION_RIGHTS);
	__operationalRights = (Vector)__operationalRightsComponent.getData();
	int size = __operationalRights.size();
	StateMod_OperationalRight o = null;
	for (int i = 0; i < size; i++) {
		o = (StateMod_OperationalRight)__operationalRights.elementAt(i);
		o.createBackup();
	}

	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the StateMod_DataSet object that has the data.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param operationalRight the operational right to select from the list
@param editable whether the data are editable or not.
*/
public StateMod_OperationalRight_JFrame (	
						StateMod_DataSet dataset,
						StateMod_DataSet_WindowManager
						dataset_wm,
						StateMod_OperationalRight
						operationalRight,
						boolean editable )
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Operational Rights", null);
	__currentOpRightsIndex = -1;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__operationalRightsComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_OPERATION_RIGHTS);
	__operationalRights = (Vector)__operationalRightsComponent.getData();
	int size = __operationalRights.size();
	StateMod_OperationalRight o = null;
	for (int i = 0; i < size; i++) {
		o = (StateMod_OperationalRight)__operationalRights.elementAt(i);
		o.createBackup();
	}

	String id = operationalRight.getID();
	int index = StateMod_Util.indexOf(__operationalRights, id);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {

	String action = e.getActionCommand();

	Object source = e.getSource();

	if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-06-24)
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		saveCurrentRecord();
		int size = __operationalRights.size();
		StateMod_OperationalRight o = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			o = (StateMod_OperationalRight)
				__operationalRights.elementAt(i);
			if (!changed && o.changed()) {
				changed = true;
			}
			o.acceptChanges();
		}				
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, 
				true);
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_OPERATIONAL_RIGHT) ;
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
		int size = __operationalRights.size();
		StateMod_OperationalRight o = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			o = (StateMod_OperationalRight)
				__operationalRights.elementAt(i);
			if (!changed && o.changed()) {
				changed = true;
			}
			o.createBackup();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, 
				true);
		}		
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		int size = __operationalRights.size();
		StateMod_OperationalRight o = null;
		for (int i = 0; i < size; i++) {
			o = (StateMod_OperationalRight)
				__operationalRights.elementAt(i);
			o.restoreOriginal();
		}					
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_OPERATIONAL_RIGHT) ;
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (source == __findNextOpr) {
		searchWorksheet(__worksheet.getSelectedRow() + 1);
	}
	else if (source == __searchID || source == __searchName) {	
		searchWorksheet();
	}	
	else if (source == __searchIDJRadioButton) {
		__searchID.setEditable(true);
		__searchName.setEditable(false);
	}
	else if (source == __searchNameJRadioButton) {
		__searchID.setEditable(false);
		__searchName.setEditable(true);
	}
	else if (source == __destination) {		
		fillDestinationAccount(__currentItyopr,
			__destination.getSelected());
		if (__currentItyopr == 8) {
			// special case, the source is tied to the destination
			__source1.setSelectedItem(__destination.getSelected());
		}
	}
	else if (source == __source1) {
		StateMod_OperationalRight opr = (StateMod_OperationalRight)
			__operationalRights.elementAt(__currentOpRightsIndex);
		opr.setCiopso1(trim(__source1.getSelected()));
		fillSourceAccount1(__currentItyopr, opr);
	}
	else if (source == __ruleTypeSwitch) {
		StateMod_OperationalRight opr = (StateMod_OperationalRight)
			__operationalRights.elementAt(__currentOpRightsIndex);
		opr.setItyopr(trim(__ruleTypeSwitch.getSelected()));
		populateRightInformation(opr);
		populateAdditionalData(opr);
	}	
}

/**
Checks the text fields for validity before they are saved back into the
data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	Vector errors = new Vector();
	int errorCount = 0;

	// for each field, check if it contains valid input.  If not,
	// create a string of the format "fieldname -- reason why it
	// is not correct" and add it to the errors vector.  also
	// increment error count
	
	if (errorCount == 0) {
		return true;
	}

	String plural = " was ";
	if (errorCount > 1) {
		plural = "s were ";
	}
	String label = "The following error" + plural + "encountered "
		+ "trying to save the record:\n";
	for (int i = 0; i < errorCount; i++) {
		label += errors.elementAt(i) + "\n";
	}
	new ResponseJDialog(this, 
		"Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Combines two Vectors into a new Vector.  
@param v1 the first Vector to add into the new Vector.
@param v2 the second Vector to add into the new Vector.
@return a new Vector that contains all the elements from the first and second
Vectors.
*/
private Vector combineVectors(Vector v1, Vector v2) {
	Vector v = new Vector();
	int size = v1.size();
	for (int i = 0; i < size; i++) {
		v.add(v1.elementAt(i));
	}
	size = v2.size();
	for (int i = 0; i < size; i++) {
		v.add(v2.elementAt(i));
	}

	return v;
}


/**
Creates the instream flow ID Vector, stores it in __instreamFlowIDs, and 
stores the instream flow Vector from the dataset into __instreamFlows.
*/
private void createInstreamFlowIDVector() {
	DataSetComponent isfComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_STATIONS);
	__instreamFlows = (Vector)isfComp.getData();
	__instreamFlowIDs = StateMod_Util.createDataList(__instreamFlows, true);
}

/**
Creates the diversion ID Vector, stores it into __diversionIDs, and
stores the diversions Vector from the dataset into __diversions.
*/
private void createDiversionIDVector() {
	DataSetComponent divComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_STATIONS);
	__diversions = (Vector)divComp.getData();
	__diversionIDs = StateMod_Util.createDataList(__diversions, true);
}

/**
Creates the diversion right ID Vector, stores it into __diversionRightIDs, and
stores the diversion rights Vector from the dataset into __diversionRights.
*/
private void createDiversionRightIDVector() {
	DataSetComponent divRightComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_RIGHTS);
	__diversionRights = (Vector)divRightComp.getData();
	__diversionRightIDs = StateMod_Util.createDataList(__diversionRights, 
		true);
}

/**
Creates the operational right ID Vector and stores it into 
__operationalRightIDs.
*/
private void createOperationalRightIDVector() {
	__operationalRightIDs = StateMod_Util.createDataList(
		__operationalRights, true);
}

/**
Creates the reservoir ID Vector, stores it into __reservoirIDs, and
stores the reservoir Vector from the dataset into __reservoirs.
*/
private void createReservoirIDVector() {
	DataSetComponent resComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_STATIONS);
	__reservoirs = (Vector)resComp.getData();
	__reservoirIDs = StateMod_Util.createDataList(__reservoirs, true);
}

/**
Creates the reservoir right ID Vector, stores it into __reservoirRightIDs, and
stores the reservoir rights Vector from the dataset into __reservoirRights.
*/
/* TODO SAM 2007-03-01 Evaluate use
private void createReservoirRightIDVector() {
	DataSetComponent resRightComp = __dataset.getComponentForComponentType(
		__dataset.COMP_RESERVOIR_RIGHTS);
	__reservoirRights = (Vector)resRightComp.getData();
	// TODO SAM 2007-03-01 Evaluate use
	//__reservoirRightIDs = StateMod_Util.createDataList(__reservoirRights, 
	//	true);
}
*/

/**
Creates the stream gage ID Vector, stores it into __streamGageIDs, and
stores the stream gage Vector from the dataset into __streamGages.
*/
private void createStreamGageIDVector() {
	DataSetComponent gageComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
	__streamGages = (Vector)gageComp.getData();
	__streamGageIDs = StateMod_Util.createDataList(__streamGages, true);
}

/**
Disables a combo box by setting it disabled, uneditable, and removing all the
items.
@param cb the SimpleJComboBox to disable.
*/
private void disableComboBox(SimpleJComboBox cb) {
	cb.removeAllItems();
	cb.setEnabled(false);
	cb.setEditable(false);
}

/**
Enables the monthly switch panel, or disables it, depending on the value
passed in.
@param enable whether to enable the monthly switches or disable them.
*/
private void enableOpPanel(boolean enable) {
	for (int i = 0; i < 12; i++) {
		if (enable) {
			__monthSwitch[i].setEnabled(true);
		}
		else {
			__monthSwitch[i].removeAllItems();
			__monthSwitch[i].setEditable(false);
			__monthSwitch[i].setEnabled(false);
		}
	}
}

/**
Fills the destination account field based on the value in the destination 
field.
@param ityopr the current operational rule.
@param value the value in the destination field.
*/
private void fillDestinationAccount(int ityopr, String value) {
	value = trim(value);
//	System.out.println("fillDestinationAccount: " + ityopr + " '" + value 
//		+ "'");
	int index = 0;
	Vector accounts = null;
	StateMod_Reservoir r = null;
	switch (ityopr) {
		case 2:
		case 3:
		case 11:
		case 14:
			__destinationAccount.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index == -1) {
				// not a reservoir, check to see if it's
				// a diversion
				index = StateMod_Util.indexOf(__diversions,
					value);
				if (index == -1) {
					return;
				}
				__destinationAccount.add("1");
				__destinationAccount.setEditable(false);
				return;
			}
			r = (StateMod_Reservoir)__reservoirs.elementAt(index);
			accounts = r.getAccounts();
			__destinationAccount.setData(
				StateMod_Util.createDataList(accounts, true));
			__destinationAccount.setEditable(true);
			break;
		case 5:
			__destinationAccount.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirRights,
				value);
			if (index > -1) {
				// TODO SAM 2007-03-01 Evaluate logic
				//rr = (StateMod_ReservoirRight)
					//__reservoirRights.elementAt(index);
				/*
				REVISIT (JTS - 2003-09-18)
				when rights get accounts
				accounts = rr.getAccounts();
				__destinationAccount.setData(
					StateMod_Util.createDataList(accounts,
					true));
				*/
				__destinationAccount.setEditable(true);
				return;
			}
			
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index > -1) {
				r = (StateMod_Reservoir)
					__reservoirs.elementAt(index);
				accounts = r.getAccounts();
				__destinationAccount.setData(
					StateMod_Util.createDataList(accounts,
					true));
				__destinationAccount.setEditable(true);
				return;
			}
			return;
		case 8:
		case 16:
			__destinationAccount.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index > -1) {
				r = (StateMod_Reservoir)
					__reservoirs.elementAt(index);
				accounts = r.getAccounts();
				__destinationAccount.setData(
					StateMod_Util.createDataList(accounts,
					true));
				__destinationAccount.setEditable(true);
				return;
			}
			break;
		default:
			return;
	}

}

/**
Fills the value in the source account 1 field based on the current operational
right rule and the right.
@param ityopr the the current right rule.
@param opr the current operational right.
*/
private void fillSourceAccount1(int ityopr, StateMod_OperationalRight opr) {
	String value = opr.getCiopso1().trim();

//	System.out.println("fillSourceAccount1: " + ityopr + " '" + value 
//		+ "'");
	int index = 0;
	switch (ityopr) {
		case 0:
			return;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 20:
			__sourceAccount1.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index == -1) {
				return;
			}
			StateMod_Reservoir r = (StateMod_Reservoir)
				__reservoirs.elementAt(index);
			Vector accounts = r.getAccounts();
			__sourceAccount1.setData(StateMod_Util.createDataList(
				accounts, true));
			if (ityopr == 9 || ityopr == 20) {
				// special case
				__sourceAccount1.addAt("0 - Prorate", 0);
			}
			__sourceAccount1.setEditable(true);
			break;
		default:
			return;
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__searchCriteriaGroup = null;
	__findNextOpr = null;
	__helpJButton = null;
	__closeJButton = null;
	__applyJButton = null;
	__cancelJButton = null;
	__opSwitchPanel = null;
	__gridPanel = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__destination = null;
	__destinationAccount = null;
	__source1 = null;
	__source2 = null;
	__source3 = null;
	__source4 = null;
	__source5 = null;
	__sourceAccount1 = null;
	__sourceAccount2 = null;
	__sourceAccount3 = null;
	__sourceAccount4 = null;
	__sourceAccount5 = null;
	__oprLocation = null;
	__oprName = null;
	__oprStationID = null;
	__oprSwitch = null;
	__searchID = null;
	__searchName = null;
	__worksheet = null;
	__opRightWorksheet = null;
	__ruleTypeSwitch = null;
	__monthSwitch = null;
	__dataset = null;
	__operationalRightsComponent = null;
	__operationalRights = null;
	__opr8Panel = null;
	__opr8JComboBox = null;
	__opr20Panel = null;
	__opr20Sjmina = null;
	__opr20Sjrela = null;
	__qdebt = null;
	__qdebtx = null;
	__qdebtPanel = null;

	super.finalize();
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to 
selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[36];
	int i = 0;
	__disables[i++] = __oprStationID;
	__disables[i++] = __oprName;
	__disables[i++] = __applyJButton;
	__disables[i++] = __gridPanel;
	__disables[i++] = __ruleTypeSwitch;
	__disables[i++] = __oprSwitch;
	__disables[i++] = __destination;
	__disables[i++] = __destinationAccount;
	__disables[i++] = __source1;
	__disables[i++] = __source2;
	__disables[i++] = __source3;
	__disables[i++] = __source4;
	__disables[i++] = __source5;
	__disables[i++] = __sourceAccount1;
	__disables[i++] = __sourceAccount2;
	__disables[i++] = __sourceAccount3;
	__disables[i++] = __sourceAccount4;
	__disables[i++] = __sourceAccount5;
	__disables[i++] = __oprLocation;
	__disables[i++] = __opr8JComboBox;
	__disables[i++] = __opr20Sjmina;
	__disables[i++] = __opr20Sjrela;
	__disables[i++] = __qdebt;
	__disables[i++] = __qdebtx;
	for (int j = 0; j < 12; j++) {
		__disables[i + j] = __monthSwitch[j];
	}

	__textUneditables = new int[2];
	__textUneditables[0] = 0;
	__textUneditables[1] = 1;
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; calls 'processTableSelection' with the 
newly-selected index in the table.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	processTableSelection(__worksheet.getSelectedRow());
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
public void mouseReleased(MouseEvent e) {
	processTableSelection(__worksheet.getSelectedRow());
}

/**
Fills in the additional data section of the form for the specified 
operational right.
@param opr the operational right to use to fill out the additional data.
*/
private void populateAdditionalData(StateMod_OperationalRight opr) {
	int ityopr = opr.getItyopr();
	int dumx = opr.getDumx();

	__opr8Panel.setVisible(false);
	__opr20Panel.setVisible(false);
	__qdebtPanel.setVisible(false);

	switch (ityopr) {
		case 2:
			if (dumx <= -12) {
				setupMonthlyChoices(opr);
				enableOpPanel(true);
				// enable monthly on/off switches
			}
			else {
				enableOpPanel(false);
			}
			__gridPanel.setVisible(true);			
			__additionalPanel.setVisible(false);
			break;
		case 3:
			enableOpPanel(false);
			__gridPanel.setVisible(true);
			__additionalPanel.setVisible(false);
			break;
		case 6:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__gridPanel.setVisible(false);
			__additionalPanel.setVisible(false);
			break;
		case 8:
			enableOpPanel(false);
			__opr8Panel.setVisible(true);
			__opr8JComboBox.removeAllItems();
			// REVISIT (JTS - 2003-09-22)
			// what goes here?
//			__opr8JComboBox.add("" + opr.getIntern());
			__additionalPanel.setVisible(true);
			__gridPanel.setVisible(false);
			break;
		case 11:
			enableOpPanel(false);
			__gridPanel.setVisible(true);
			__additionalPanel.setVisible(false);
			break;
		case 13:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__gridPanel.setVisible(true);
			__additionalPanel.setVisible(false);
			break;
		case 14:
			enableOpPanel(false);
			__gridPanel.setVisible(false);
			__additionalPanel.setVisible(false);
			break;
		case 15:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__gridPanel.setVisible(false);
			__additionalPanel.setVisible(false);
			break;
		case 16:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__gridPanel.setVisible(true);
			__additionalPanel.setVisible(false);
			break;
		case 17:
		case 18:
			if (dumx == -20) {
				setupMonthlyChoices(opr);
				enableOpPanel(true);
			}
			else {
				enableOpPanel(false);
			}
			__gridPanel.setVisible(false);
			__qdebtPanel.setVisible(true);
			__qdebt.setText("" + opr.getQdebt());
			__qdebtx.setText("" + opr.getQdebtx());
			__additionalPanel.setVisible(true);
			break;
		case 19:
			enableOpPanel(false);
			__gridPanel.setVisible(false);
			__additionalPanel.setVisible(false);
			break;
		case 20:
			__opr20Panel.setVisible(true);
			__opr20Sjmina.setText("" + opr.getSjmina());
			__opr20Sjrela.setText("" + opr.getSjrela());
			__additionalPanel.setVisible(true);
			if (dumx == 12) {
				setupMonthlyChoices(opr);
				enableOpPanel(true);
			}
			else {
				enableOpPanel(false);
			}
			__gridPanel.setVisible(false);
			break;
		case 23:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__gridPanel.setVisible(false);
			__additionalPanel.setVisible(false);
			break;
		default:
			enableOpPanel(false);
			__gridPanel.setVisible(false);
			__additionalPanel.setVisible(false);
			break;
	}
}

/**
Fills in the right information for the specified operational right.
@param opr the operational right to use to populate the fields for
right information.
*/
private void populateRightInformation(StateMod_OperationalRight opr) {
	__ruleTypeSwitch.setEditable(false);
	__oprSwitch.setEditable(false);
	int ityopr = opr.getItyopr();
	__currentItyopr = ityopr;

	String dest = opr.getCiopde();
	String destAcct = opr.getIopdes();
	String src1 = opr.getCiopso1();
	String srcAcct1 = opr.getIopsou1();
	String src2 = opr.getCiopso2();
	String srcAcct2 = opr.getIopsou2();
//	System.out.println("dest '" + dest + "' / '" + destAcct + "'");
//	System.out.println("src1 '" + src1  + "' / '" + srcAcct1 + "'");
//	System.out.println("src2 '" + src2 + "' / '" + srcAcct2 + "'");

	String src3 = null;
	String srcAcct3 = null;
	String src4 = null;
	String srcAcct4 = null;
	String src5 = null;
	String srcAcct5 = null;
	
	// REVISIT (JTS - 2003-09-22)
	// whenever the rules for rule type #19 are determined, removed
	// the check for 19 from this if statement.
	if (ityopr == 0 || ityopr == 19) {
		__destination.removeAllItems();
		__destination.setEnabled(false);
		__destination.setEditable(false);
		__destinationAccount.removeAllItems();
		__destinationAccount.setEnabled(false);
		__destinationAccount.setEditable(false);
		__source1.removeAllItems();
		__source1.setEnabled(false);
		__source1.setEditable(false);
		__sourceAccount1.removeAllItems();
		__sourceAccount1.setEnabled(false);
		__sourceAccount1.setEditable(false);
		__source2.removeAllItems();
		__source2.setEnabled(false);
		__source2.setEditable(false);
		__sourceAccount2.removeAllItems();
		__sourceAccount2.setEnabled(false);
		__sourceAccount2.setEditable(false);
		__source3.removeAllItems();
		__source3.setEnabled(false);
		__source3.setEditable(false);
		__sourceAccount3.removeAllItems();
		__sourceAccount3.setEnabled(false);
		__sourceAccount3.setEditable(false);
		__source4.removeAllItems();
		__source4.setEnabled(false);
		__source4.setEditable(false);
		__sourceAccount4.removeAllItems();
		__sourceAccount4.setEnabled(false);
		__sourceAccount4.setEditable(false);
		__source5.removeAllItems();
		__source5.setEnabled(false);
		__source5.setEditable(false);
		__sourceAccount5.removeAllItems();
		__sourceAccount5.setEnabled(false);
		__sourceAccount5.setEditable(false);		
		return;
	}
	else {
		__destination.setEnabled(true);
		__destination.setEditable(true);
		__destinationAccount.setEnabled(true);
		__destinationAccount.setEditable(true);
		__source1.setEnabled(true);
		__source1.setEditable(true);
		__sourceAccount1.setEnabled(true);
		__sourceAccount1.setEditable(true);
		__source2.setEnabled(true);
		__source2.setEditable(true);
		__sourceAccount2.setEnabled(true);
		__sourceAccount2.setEditable(true);
		__source3.setEnabled(true);
		__source3.setEditable(true);
		__sourceAccount3.setEnabled(true);
		__sourceAccount3.setEditable(true);
		__source4.setEnabled(true);
		__source4.setEditable(true);
		__sourceAccount4.setEnabled(true);
		__sourceAccount4.setEditable(true);
		__source5.setEnabled(true);
		__source5.setEditable(true);
		__sourceAccount5.setEnabled(true);
		__sourceAccount5.setEditable(true);		
	}
	
//	System.out.println("populateRightInformation: " + ityopr);
	switch (ityopr) {
		case 1:		// reservoir release to isf
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination.setData(__instreamFlowIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			// no additional data

			break;

		case 2:		// reservoir release to diversion or 
				// reservoir or carrier by river
		case 3:		// reservoir release to diversion or
				// reservoir by carrier				
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(combineVectors(
				__reservoirIDs, __diversionIDs));
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount.setSelectedPrefixItem(destAcct);

			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);
			
			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);
	
			break;
			
		case 4:		// reservoir release to a direct diversion
				// by exchange with river
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			__destination.setData(__diversionIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);
	
			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.add("-1");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);			
			
			// no additional data
			
			break;

		case 5:		// reservoir storage by exchange
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(__reservoirIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount.setSelectedPrefixItem(destAcct);

			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);
	
			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			// no additional data
			
			return;

		case 6:		// reservoir to reservoir transfer (bookover)
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(__reservoirIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			// REVISIT (JTS - 2003-09-18)
			__destinationAccount.removeAllItems();
			__destinationAccount.add("REVISIT");
			// waiting for RRB
			// __destinationAccount.setSelectedPrefixItem(destAcct);
			__destinationAccount.setEditable(true);
			
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.setData(StateMod_Util.createDataList(
				__operationalRights, true));			
			__source2.addAt("0", 0);
			__source2.setSelectedPrefixItem(srcAcct2);
			__source2.setEditable(true);
	
			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			break;
			
		case 7:		// diversion by a carrier by exchange
			if (__operationalRightIDs == null) {
				createOperationalRightIDVector();
			}
			__destination.setData(__operationalRightIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);
	
			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			// no additional data

			break;

		case 8:		// out of priority reservoir storage
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(__reservoirIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount.setSelectedPrefixItem(destAcct);

			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(dest);
			__source1.setEnabled(false);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source2.setData(__diversionRightIDs);
			__source2.setSelectedPrefixItem(src2);
			__source2.setEditable(false);
			
			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			break;

		case 9:		// reservoir target
			__destination.removeAllItems();
			__destination.add("0");
			__destination.setEditable(false);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("0");
			__destinationAccount.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(srcAcct1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			// no additional data

			break;

		case 10:	// general replacement reservoir to a 
				// diversion by a direct release or exchange
			__destination.removeAllItems();
			__destination.add("0");
			__destination.setEditable(false);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("0");
			__destinationAccount.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(srcAcct1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			// no additional data

			break;

		case 11:	// direct flow diversion to a demand or 
				// reservoir through intervening (carrier)
				// structures	
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(combineVectors(
				__reservoirIDs, __diversionIDs));
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount.setSelectedPrefixItem(destAcct);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source1.setData(__diversionRightIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.setEditable(false);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			break;
				
		case 12:	// reoperation
			__destination.removeAllItems();
			__destination.add("0");
			__destination.setEditable(false);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("0");
			__destinationAccount.setEditable(false);

			__source1.removeAllItems();
			__source1.add("0");
			__source1.setEditable(false);
			__source1.setEnabled(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.setEditable(false);
			
			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);	

			// no additional data

			break;

		case 13:	// index flow constraint on an instream flow
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination.setData(__instreamFlowIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);
			
			Vector cgotoIDs = StateMod_Util.createCgotoDataList(
				__instreamFlows, true);
			__source1.setData(cgotoIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEditable(true);
			__source1.setEnabled(true);
			
			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("" + srcAcct1);
			__sourceAccount1.setEditable(true);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);
			
			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);
	
			break;
		case 14:	// direct flow diversion to a demand or
				// reservoir through intervening (carrier)
				// structures limited by demand at both the
				// destination and first carrier
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(combineVectors(
				__reservoirIDs, __diversionIDs));
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);
				
			fillDestinationAccount(ityopr, dest);
			__destinationAccount.setSelectedPrefixItem(destAcct);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source1.setData(__diversionRightIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEnabled(true);
			__source1.setEditable(true);
		
			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("0");
			__sourceAccount1.add(
				"(enter a positive integer value)");

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);
		
			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);
	
			break;
			
		case 15:	// interruptible supply
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination.setData(__instreamFlowIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);
		
			__source1.removeAllItems();
			__source1.add("REVISIT");
			__source1.setEditable(true);
		
			// REVISIT (JTS - 2003-09-22)
			// checking with RRB re: ciopso1

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("" + srcAcct1);
			__sourceAccount1.setEditable(true);

			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			__source2.setData(__diversionIDs);
			__source2.setSelectedPrefixItem(src2);
			__source2.setEditable(true);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0 - Allow 100% to be Diverted");
			__sourceAccount2.add("-1 - Allow the Depletion to be "
				+ "Diverted");
			__sourceAccount2.setSelectedPrefixItem("" + srcAcct2);
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);
	
			// no additional data

			break;

		case 16:	// direct flow storage
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination.setData(__reservoirIDs);
			__destination.setSelectedPrefixItem(dest);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount.setSelectedPrefixItem(destAcct);

			if (__diversionRightIDs == null) {			
				createDiversionRightIDVector();
			}
			__source1.setData(__diversionRightIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEditable(true);
			__source1.setEnabled(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("1");
			__sourceAccount1.setEditable(false);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("" + srcAcct2);
			__sourceAccount2.setEditable(true);
			
			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);

			break;

		case 17:	// rio grande compact -- rio grande river
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination.setData(__instreamFlowIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);

			if (__streamGageIDs == null) {
				createStreamGageIDVector();
			}
			__source1.setData(__streamGageIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEditable(true);
			__source1.setEnabled(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("1");
			__sourceAccount1.setEditable(false);

			__source2.setData(__streamGageIDs);
			__source2.setSelectedPrefixItem(src2);
			__source2.setEditable(true);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("-1");
			__sourceAccount2.setEditable(false);
		
			src3 = opr.getCiopso3();
			srcAcct3 = opr.getIopsou3();
			src4 = opr.getCiopso4();
			srcAcct4 = opr.getIopsou4();
			src5 = opr.getCiopso5();
			srcAcct5 = opr.getIopsou5();

			__source3.setData(__streamGageIDs);
			__source3.setSelectedPrefixItem(src3);
			__source3.setEnabled(true);
			__source3.setEditable(true);

			__sourceAccount3.removeAllItems();
			__sourceAccount3.addItem("" + srcAcct3);
			__sourceAccount3.setEditable(true);
			__sourceAccount3.setEnabled(true);
		
			__source4.setData(__streamGageIDs);
			__source4.setSelectedPrefixItem(src4);
			__source4.setEnabled(true);
			__source4.setEditable(true);

			__sourceAccount4.removeAllItems();
			__sourceAccount4.addItem("" + srcAcct4);
			__sourceAccount4.setEditable(true);
			__sourceAccount4.setEnabled(true);

			__source5.setData(__streamGageIDs);
			__source5.setSelectedPrefixItem(src5);
			__source5.setEnabled(true);
			__source5.setEditable(true);

			__sourceAccount5.removeAllItems();
			__sourceAccount5.addItem("" + srcAcct5);
			__sourceAccount5.setEditable(true);
			__sourceAccount5.setEnabled(true);

			break;

		case 18:	// rio grande compact -- conejos river
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination.setData(__instreamFlowIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);

			if (__streamGageIDs == null) {
				createStreamGageIDVector();
			}
			__source1.setData(__streamGageIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEditable(true);
			__source1.setEnabled(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("1");
			__sourceAccount1.setEditable(false);

			__source2.setData(__streamGageIDs);
			__source2.setSelectedPrefixItem(src2);
			__source2.setEditable(true);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("-1");
			__sourceAccount2.setEditable(false);

			src3 = opr.getCiopso3();
			srcAcct3 = opr.getIopsou3();
			src4 = opr.getCiopso4();
			srcAcct4 = opr.getIopsou4();
			src5 = opr.getCiopso5();
			srcAcct5 = opr.getIopsou5();

			__source3.setData(__streamGageIDs);
			__source3.setSelectedPrefixItem(src3);
			__source3.setEnabled(true);
			__source3.setEditable(true);

			__sourceAccount3.removeAllItems();
			__sourceAccount3.addItem("" + srcAcct3);
			__sourceAccount3.setEditable(true);
			__sourceAccount3.setEnabled(true);
		
			__source4.setData(__streamGageIDs);
			__source4.setSelectedPrefixItem(src4);
			__source4.setEnabled(true);
			__source4.setEditable(true);

			__sourceAccount4.removeAllItems();
			__sourceAccount4.addItem("" + srcAcct4);
			__sourceAccount4.setEditable(true);
			__sourceAccount4.setEnabled(true);

			__source5.setData(__streamGageIDs);
			__source5.setSelectedPrefixItem(src5);
			__source5.setEnabled(true);
			__source5.setEditable(true);

			__sourceAccount5.removeAllItems();
			__sourceAccount5.addItem("" + srcAcct5);
			__sourceAccount5.setEditable(true);
			__sourceAccount5.setEnabled(true);
		
			break;
			
		case 19:	// split channel operation -- under development 
			return;
		case 20:	// san juan rip reservoir operation
			__destination.removeAllItems();
			__destination.add("0");
			__destination.setEditable(false);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("0");
			__destinationAccount.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1.setData(__reservoirIDs);
			__source1.setSelectedPrefixItem(src1);
			__source1.setEditable(true);
			__source1.setEnabled(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1.setSelectedPrefixItem(srcAcct1);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);			

			break;

		case 21:	// sprinkler use
		case 22:	// soil moisture flow
			__destination.removeAllItems();
			__destination.add("0");
			__destination.setEditable(false);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("0");
			__destinationAccount.setEditable(false);
			
			__source1.removeAllItems();
			__source1.add("0");
			__source1.setEditable(false);
			__source1.setEnabled(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("1");
			__sourceAccount1.setEditable(false);

			__source2.removeAllItems();
			__source2.add("0");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);			

			// no additional data

			break;
		case 23:	// direct flow exchange
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			__destination.setData(__diversionIDs);
			__destination.setSelectedPrefixItem(dest);
			__destination.setEditable(true);

			__destinationAccount.removeAllItems();
			__destinationAccount.add("1");
			__destinationAccount.setEditable(false);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source1.setData(__diversionIDs);
			__source1.setEditable(true);
			__source1.setEnabled(true);

			__sourceAccount1.removeAllItems();
			__sourceAccount1.add("0");
			__sourceAccount1.add("1");
			__sourceAccount1.setEditable(false);

			__source2.removeAllItems();
			__source2.add("1");
			__source2.setEditable(false);

			__sourceAccount2.removeAllItems();
			__sourceAccount2.add("0");
			__sourceAccount2.setEditable(false);

			disableComboBox(__source3);
			disableComboBox(__source4);
			disableComboBox(__source5);

			disableComboBox(__sourceAccount3);
			disableComboBox(__sourceAccount4);
			disableComboBox(__sourceAccount5);			

			break;


		default:
			return;
	}

}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it 
on the form.
@param index the index of the reservoir to display on the form.
*/
private void processTableSelection(int index) {
	__lastOpRightsIndex = __currentOpRightsIndex;
	__currentOpRightsIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__disables, true);
		return;
	}

	JGUIUtil.enableComponents(__disables, __textUneditables,
		__editable);

	StateMod_OperationalRight opr = (StateMod_OperationalRight)
		__operationalRights.elementAt(__currentOpRightsIndex);
	__oprStationID.setText(opr.getID());
	__oprName.setText(opr.getName());
	__oprLocation.setText(opr.getRtem());
	if (opr.getCgoto().equals("")) {	
		setOriginalCgoto(opr, opr.getRtem());
	}

	// switch
	if (opr.getSwitch() == 1) {
		__oprSwitch.select("1 - On");
	}
	else {
		__oprSwitch.select("0 - Off");
	}	

	// rule type
	int ityopr = opr.getItyopr();
	if (ityopr >= 0 && ityopr < 24) {
		__ruleTypeSwitch.select(ityopr);
	}
	else {
		__ruleTypeSwitch.select(0);
	}
	
	populateRightInformation(opr);
	populateAdditionalData(opr);
}

/**
Saves the prior record selected in the table; called when moving to a new 
record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastOpRightsIndex);
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentOpRightsIndex);
}

/**
Saves the information associated with the currently-selected operational right.
The user doesn't need to hit the return key for the gui to recognize changes.
The info is saved each time the user selects a differents tation or pressed
the close button.
*/
private void saveInformation(int record) {
	if (!__editable || record == -1) {
		return;
	}

	if (!checkInput()) {
		return;
	}

	__opRightWorksheet.stopEditing();

	StateMod_OperationalRight opr = (StateMod_OperationalRight)
		__operationalRights.elementAt(record);
	opr.setName(__oprName.getText());
	opr.setSwitch(__oprSwitch.getSelectedIndex());
	opr.setCgoto(__oprLocation.getText());

	opr.setCiopde(trim(__destination.getSelected()));
	opr.setIopdes(trim(__destinationAccount.getSelected()));
	opr.setCiopso1(trim(__source1.getSelected()));
	opr.setIopsou1(trim(__sourceAccount1.getSelected()));
	opr.setCiopso2(trim(__source2.getSelected()));
	opr.setIopsou2(trim(__sourceAccount2.getSelected()));

	int ityopr = __ruleTypeSwitch.getSelectedIndex();
	opr.setItyopr(ityopr);

	if (__monthSwitch[0].isEnabled()) {
		String value = null;
		for (int i = 0; i < 12; i++) {
			value = __monthSwitch[i].getSelected();
			value = trim(value);
			opr.setImonsw(i, value);
		}
	}

	if (__opr8Panel.isVisible()) {
	// REVISIT (JTS - 2003-09-22)
	// what goes here?
//		opr.set??(__opr8JComboBox.getSelected().trim());
	}
	else if (__opr20Panel.isVisible()) {
		opr.setSjmina(__opr20Sjmina.getText().trim());
		opr.setSjrela(__opr20Sjrela.getText().trim());
	}
	else if (__qdebtPanel.isVisible()) {
		opr.setQdebt(__qdebt.getText().trim());
		opr.setQdebtx(__qdebtx.getText().trim());
	}

	if (__source3.isEnabled()) {
		opr.setCiopso3(trim(__source3.getSelected()));
		opr.setIopsou3(trim(__sourceAccount3.getSelected()));
		opr.setCiopso4(trim(__source4.getSelected()));
		opr.setIopsou4(trim(__sourceAccount4.getSelected()));
		opr.setCiopso5(trim(__source5.getSelected()));
		opr.setIopsou5(trim(__sourceAccount5.getSelected()));
	}
}

/**
Searches through the worksheet for a value, starting at the first row.
If the value is found, the row is selected.
*/
public void searchWorksheet() {
	searchWorksheet(0);
}

/**
Searches through the worksheet for a value, starting at the given row.
If the value is found, the row is selected.
@param row the row to start searching from.
*/
public void searchWorksheet(int row) {
	String searchFor = null;
	int col = -1;
	if (__searchIDJRadioButton.isSelected()) {
		searchFor = __searchID.getText().trim();
		col = 0;
	}
	else {
		searchFor = __searchName.getText().trim();
		col = 1;
	}
	int index = __worksheet.find(searchFor, col, row, 
		JWorksheet.FIND_EQUAL_TO | JWorksheet.FIND_CONTAINS |
		JWorksheet.FIND_CASE_INSENSITIVE | JWorksheet.FIND_WRAPAROUND);
	if (index != -1) {
		selectTableIndex(index);
	}
}

/**
Selects the desired ID in the left table and displays the appropriate data
in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheet.getRowCount();
	StateMod_OperationalRight or = null;
	for (int i = 0; i < rows; i++) {
		or = (StateMod_OperationalRight)__worksheet.getRowData(i);
		if (or.getID().trim().equals(id.trim())) {
			selectTableIndex(i);
			return;
		}
	}
}

/**
Selects the desired index in the table, but also displays the appropriate data
in the remainder of the window.
@param index the index to select in the list.
*/
public void selectTableIndex(int index) {
	int rowCount = __worksheet.getRowCount();
	if (rowCount == 0) {
		return;
	}
	if (index > (rowCount + 1)) {
		return;
	}
	if (index < 0) {
		return;
	}
	__worksheet.scrollToRow(index);
	__worksheet.selectRow(index);
	processTableSelection(index);
}

/**
Sets up the GUI.
@param index the index of operational right to be initially selected
*/
private void setupGUI(int index) {
	String routine = "setupGUI";

	addWindowListener(this);

	JPanel p1 = new JPanel();	// user name -> rule type switch
	JPanel p2 = new JPanel();	// source -> final account
	__opSwitchPanel = new JPanel();	// month operation switches
	__gridPanel = new JPanel();
	JPanel psearch = new JPanel();	// search area

	__oprStationID = new JTextField(12);
	__oprName = new JTextField(24);
	__oprName.setEditable(false);
	__oprLocation = new JTextField(12);
	__oprSwitch = new SimpleJComboBox();
	__oprSwitch.add("0 - Off");
	__oprSwitch.add("1 - On");	
	__oprSwitch.setEditable(false);

	__destination = new SimpleJComboBox();
	__destination.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__destinationAccount = new SimpleJComboBox();
	__destinationAccount.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source1 = new SimpleJComboBox();
	__source1.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount1 = new SimpleJComboBox();
	__sourceAccount1.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source2 = new SimpleJComboBox();
	__source2.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount2 = new SimpleJComboBox();
	__sourceAccount2.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source3 = new SimpleJComboBox();
	__source3.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount3 = new SimpleJComboBox();
	__sourceAccount3.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source4 = new SimpleJComboBox();
	__source4.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount4 = new SimpleJComboBox();
	__sourceAccount4.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source5 = new SimpleJComboBox();
	__source5.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount5 = new SimpleJComboBox();
	__sourceAccount5.setPrototypeDisplayValue("          "
		+ "                                                  ");

	__destination.setSelectionFailureFallback("~ - Unknown", 0);
	__destinationAccount.setSelectionFailureFallback("~ - Unknown", 0);

	__source1.setSelectionFailureFallback("~ - Unknown", 0);
	__source2.setSelectionFailureFallback("~ - Unknown", 0);
	__source3.setSelectionFailureFallback("~ - Unknown", 0);
	__source4.setSelectionFailureFallback("~ - Unknown", 0);
	__source5.setSelectionFailureFallback("~ - Unknown", 0);

	__sourceAccount1.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount2.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount3.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount4.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount5.setSelectionFailureFallback("~ - Unknown", 0);

	__monthSwitch = new SimpleJComboBox[12];
	for (int i = 0; i < 12; i++) {
		__monthSwitch[i] = new SimpleJComboBox(false);
		__monthSwitch[i].setPrototypeDisplayValue(
			"                           ");
	}
	
	__ruleTypeSwitch = new SimpleJComboBox();
	String[] options = StateMod_OperationalRight.NAMES;
	int num = options.length;
	for (int i = 0; i < num; i++) {		
		__ruleTypeSwitch.add("" + i + " - " + options[i]);
	}

	__ruleTypeSwitch.setEnabled(false);
	__ruleTypeSwitch.setEditable(false);

	__searchID = new JTextField(10);
	__searchName = new JTextField(10);
	__searchName.setEditable(false);
	__findNextOpr = new JButton("Find Next");
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton("ID", true);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	__searchNameJRadioButton = new JRadioButton("Name", false);
	__searchCriteriaGroup.add(__searchNameJRadioButton);
	__searchIDJRadioButton.addActionListener(this);
	__searchNameJRadioButton.addActionListener(this);

	__applyJButton = new JButton(__BUTTON_APPLY);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);

	GridBagLayout gb = new GridBagLayout();
	JPanel mainJPanel = new JPanel();
	mainJPanel.setLayout(gb);
	p1.setLayout(gb);
	p2.setLayout(gb);
	psearch.setLayout(gb);
	__opSwitchPanel.setLayout(gb);
	__gridPanel.setLayout(gb);

	int y;

	PropList p =new PropList("StateMod_OperationalRight_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		StateMod_OperationalRight_TableModel tmo = new
			StateMod_OperationalRight_TableModel(
			__operationalRights, __editable);
		StateMod_OperationalRight_CellRenderer cro = new
			StateMod_OperationalRight_CellRenderer(tmo);
	
		jsw = new JScrollWorksheet(cro, tmo, p);
		__worksheet = jsw.getJWorksheet();
		__worksheet.removeColumn(2);
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
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);
	
	JGUIUtil.addComponent(mainJPanel, jsw,
		0, 0, 6, 4, 1, 0,
		10, 10, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	// add top labels and text areas to panels
	// assemble panel1, first
	y=0;
	JGUIUtil.addComponent(
		p1, new JLabel("Op Right Name:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __oprName,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
//	__oprName.addActionListener(this);
	y++;
	JGUIUtil.addComponent(
		p1, new JLabel("Operational Right ID:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __oprStationID,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
//	__oprStationID.addActionListener(this);
	__oprStationID.setEditable(false);
	y++;
	JGUIUtil.addComponent(
		p1, new JLabel("Administration Number:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __oprLocation,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
//	__oprLocation.addActionListener(this);
	y++;
	JGUIUtil.addComponent(
		p1, new JLabel("On/Off Switch:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __oprSwitch,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(
		p1, new JLabel("Rule Type:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		p1, __ruleTypeSwitch,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel dest = new JPanel();
	dest.setLayout(gb);
	JGUIUtil.addComponent(
		p2, dest,
		0, 0, 4, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	dest.setBorder(BorderFactory.createTitledBorder("Destination:"));
	y = 0;
	JGUIUtil.addComponent(
		dest, new JLabel("Destination:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		dest, __destination,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		dest, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		dest, __destinationAccount,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// assemble panel2
	JPanel source = new JPanel();
	source.setLayout(gb);
	JGUIUtil.addComponent(
		p2, source,
		0, 1, 4, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	source.setBorder(BorderFactory.createTitledBorder("Sources:"));
	
	y=0;
	JGUIUtil.addComponent(
		source, new JLabel("Source 1:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __source1,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		source, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __sourceAccount1,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		source, new JLabel("Source 2:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __source2,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		source, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __sourceAccount2,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		source, new JLabel("Source 3:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __source3,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		source, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __sourceAccount3,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		source, new JLabel("Source 4:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __source4,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		source, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __sourceAccount4,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		source, new JLabel("Source 5:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __source5,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		source, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		source, __sourceAccount5,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// two top panels of info
	JGUIUtil.addComponent(
		mainJPanel, p1,
		6, 0, 2, 1, 0, 0,
		10, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		mainJPanel, p2,
		6, GridBagConstraints.RELATIVE, 
		2, 1, 0, 0,
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__gridPanel.setBorder(BorderFactory.createTitledBorder(
		"Intervening Structures or Secondary Constraints"));

	PropList P2=new PropList("StateMod_OperationalRight_JFrame.JWorksheet");

	/*
	P2.add("JWorksheet.CellFont=Courier");
	P2.add("JWorksheet.CellStyle=Plain");
	P2.add("JWorksheet.CellSize=11");
	P2.add("JWorksheet.HeaderFont=Arial");
	P2.add("JWorksheet.HeaderStyle=Plain");
	P2.add("JWorksheet.HeaderSize=11");
	P2.add("JWorksheet.HeaderBackground=LightGray");
	P2.add("JWorksheet.RowColumnPresent=true");
	*/
	P2.add("JWorksheet.ShowRowHeader=true");
	P2.add("JWorksheet.ShowPopupMenu=true");
	P2.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widthsR = null;
	JScrollWorksheet opRightJSW = null;
	try {
		StateMod_OperationalRight_TableModel tmo = new
			StateMod_OperationalRight_TableModel(
			new Vector(), __editable);
		StateMod_OperationalRight_CellRenderer cro = new
			StateMod_OperationalRight_CellRenderer(tmo);
	
		opRightJSW = new JScrollWorksheet(cro, tmo, P2);
		__opRightWorksheet = opRightJSW.getJWorksheet();
		__opRightWorksheet.removeColumn(0);
		__opRightWorksheet.removeColumn(1);
//		__opRightWorksheet.setColumnComboBoxValues(2, new Vector());
		widthsR = cro.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building worksheet.");
		Message.printWarning(2, routine, e);
		opRightJSW = new JScrollWorksheet(0, 0, p);
		__opRightWorksheet = opRightJSW.getJWorksheet();
	}
	__opRightWorksheet.setPreferredScrollableViewportSize(null);
	__opRightWorksheet.setHourglassJFrame(this);
	/*
	__opRightWorksheet.addMouseListener(this);
	__opRightWorksheet.addKeyListener(this);
	*/

	JGUIUtil.addComponent(
		__gridPanel, opRightJSW,
		0, 0, 2, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	JButton addButton = new JButton(__BUTTON_ADD);
	addButton.addActionListener(this);
	JButton deleteButton = new JButton(__BUTTON_DELETE);
	deleteButton.addActionListener(this);

	// REVISIT (JTS - 2003-09-23)
	// will need enabled later
	addButton.setEnabled(false);
	deleteButton.setEnabled(false);

	JGUIUtil.addComponent(
		__gridPanel, addButton,
		0, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(
		__gridPanel, deleteButton,
		1, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	__additionalPanel = new JPanel();
	__additionalPanel.setLayout(gb);
	__additionalPanel.setBorder(BorderFactory.createTitledBorder(
		"Additional Data"));

	JPanel lowerPanel = new JPanel();
	lowerPanel.setLayout(gb);

	JGUIUtil.addComponent(
		lowerPanel, __gridPanel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);		

	__opr8Panel = new JPanel();
	__opr8JComboBox = new SimpleJComboBox();
	__opr8JComboBox.setPrototypeDisplayValue("                   ");
	__opr20Panel = new JPanel();
	__opr20Sjmina = new JTextField(20);
	__opr20Sjrela = new JTextField(20);
	__qdebtPanel = new JPanel();
	__qdebt = new JTextField(20);
	__qdebtx = new JTextField(20);

	__opr8Panel.setLayout(gb);
	JGUIUtil.addComponent(__opr8Panel, 
		new JLabel("Destination Water Right ID:"),
		0, 0, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);	
	JGUIUtil.addComponent(__opr8Panel, __opr8JComboBox,
		1, 0, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	
	__opr20Panel.setLayout(gb);
	JGUIUtil.addComponent(__opr20Panel, 
		new JLabel("Minimum Available Water (AF):"),
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);	
	JGUIUtil.addComponent(__opr20Panel, __opr20Sjmina,
		1, 0, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__opr20Panel, 	
		new JLabel("Average Release (AF/YR):"),
		0, 1, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__opr20Panel, __opr20Sjrela,
		1, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	__qdebtPanel.setLayout(gb);
	JGUIUtil.addComponent(__qdebtPanel,
		new JLabel("Year when Calculations Include Adjustment:"),
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__qdebtPanel, __qdebt,
		1, 0, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);		
	JGUIUtil.addComponent(__qdebtPanel,
		new JLabel("Initial Surplus/Shortage (AF):"),
		0, 1, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__qdebtPanel, __qdebtx,
		1, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	JGUIUtil.addComponent(__additionalPanel, __opr8Panel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__additionalPanel, __qdebtPanel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__additionalPanel, __opr20Panel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	JGUIUtil.addComponent(
		lowerPanel, __additionalPanel,
		1, 0, 1, 1, 1, 1,
		0, 0, 0, 0,		
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	__additionalPanel.setVisible(false);

	JGUIUtil.addComponent(
		mainJPanel, lowerPanel,
		6, 2, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);


	// add bottom buttons
	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel pfinal = new JPanel();
	pfinal.setLayout(fl);
	if (__editable) {
		pfinal.add(__applyJButton);
		pfinal.add(__cancelJButton);
	}
//	pfinal.add(__helpJButton);
	pfinal.add(__closeJButton);
	__helpJButton.addActionListener(this);
	__applyJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	/*
	JGUIUtil.addComponent(
		mainJPanel, pfinal,
		6, 8,
		2, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTH);
	*/
	
	// assemble panel3 - monthly operations
	y=0;
	JGUIUtil.addComponent(
		__opSwitchPanel, new JLabel("Month Op.:"),
		0, y, 2, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	y++;
	for (int i=0; i<12; i++) {
		JGUIUtil.addComponent(
			__opSwitchPanel, new JLabel("" + (i + 1) + ")  "),
			0, y, 1, 1, 1, 0,
			1, 0, 0, -2,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
		JGUIUtil.addComponent(
			__opSwitchPanel, __monthSwitch[i],
			1, y, 1, 1, 1, 0,
			1, 0, 0, 0,
			GridBagConstraints.NONE, GridBagConstraints.WEST);
		y++;
	}
	JGUIUtil.addComponent(
		mainJPanel, __opSwitchPanel,
		GridBagConstraints.RELATIVE, 0, 1, 4, 0, 0,
		0, 5, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	// add search areas
	//y=5;
	y=0;
	psearch.setBorder(BorderFactory.createTitledBorder(
		"Search above list for:"));
	JGUIUtil.addComponent(psearch, __searchIDJRadioButton,
		0, y, 1, 1, 0, 0,
		1, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(psearch, __searchID,
		1, y, 1, 1, 1, 1,
		1, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__searchID.addActionListener(this);
	y++;
	JGUIUtil.addComponent(
		psearch, __searchNameJRadioButton,
		0, y, 1, 1, 0, 0,
		1, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		psearch, __searchName,
		1, y, 1, 1, 1, 1,
		1, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__searchName.addActionListener(this);
	y++;
	JGUIUtil.addComponent(
		psearch, __findNextOpr,
		0, y, 4, 1, 0, 0,
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextOpr.addActionListener(this);
	JGUIUtil.addComponent(
		mainJPanel, psearch,
		0, 5, 4, 1, 0, 0,
		3, 10, 3, 0,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	getContentPane().add(mainJPanel);
	
	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, pfinal,
		0, 0, 8, 1, 1, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);	
	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 1, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__statusJTextField = new JTextField(5);
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __statusJTextField,
		7, 1, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottomJPanel);	

	initializeDisables();

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_OPERATIONAL_RIGHT, this);
	}

	pack();
	setSize(950,700);
	JGUIUtil.center(this);
	//setResizable(false);
	selectTableIndex(index);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
	if (widthsR != null) {
		__opRightWorksheet.setColumnWidths(widthsR);
	}

	// put these here so any changes while the GUI is initializing 
	// don't cause itemStateChanged events.
	__destination.addActionListener(this);
	__source1.addActionListener(this);
	__ruleTypeSwitch.addActionListener(this);

	__worksheet.addSortListener(this);
}

/**
Sets up the monthly choices for the operational rules that use them.
@param opr the operational right to use for filling in the monthly switches.
*/
private void setupMonthlyChoices(StateMod_OperationalRight opr) {
	int ityopr = opr.getItyopr();
	Vector v = null;

	switch (ityopr) {
		case 2:
		case 6:
		case 13:
		case 15:
		case 17:
		case 20:
			if (!opr.hasImonsw()) {
				opr.setupImonsw();
			}
			v = new Vector();
			v.add("0 - Off");
			v.add("1 - On");
			for (int i = 0; i < 12; i++) {
				__monthSwitch[i].setData(v);
				__monthSwitch[i].setEditable(false);
				__monthSwitch[i].select(opr.getImonsw(i));
			}
			break;

		case 16:		
		case 23:
			if (!opr.hasImonsw()) {
				opr.setupImonsw();
			}		
			int imonsw = 0;
			v = new Vector();
			v.add("-1 - Month to Stop");
			v.add("0 - Off");
			v.add("1 - On");
			v.add("2 - On / Month to Check");
			for (int i = 0; i < 12; i++) {
				__monthSwitch[i].setData(v);
				__monthSwitch[i].setEditable(false);
				imonsw = opr.getImonsw(i);
				__monthSwitch[i].select(imonsw + 1);
			}
			break;

		default:
			return;
	}
}

/**
Trims a string in the format "XXX - YYY" by removing everything after 'XXX'.
@param s the String to trim.
@return a trimmed string.
*/
private String trim(String s) {
	if (s == null) {
		return "";
	}
	int index = s.indexOf(" - ");
	String s2 = new String(s);
	if (index > -1) {
		s2 = s2.substring(0, index);
	}
	s2 = s2.trim();
	return s2;
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
	saveCurrentRecord();
	int size = __operationalRights.size();
	StateMod_OperationalRight o = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		o = (StateMod_OperationalRight)
			__operationalRights.elementAt(i);
		if (!changed && o.changed()) {
			changed = true;
		}
		o.acceptChanges();
	}					
	if (changed) {
		__dataset.setDirty(
			StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (
		StateMod_DataSet_WindowManager.  WINDOW_OPERATIONAL_RIGHT );
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
Responds to Window iconified events; saves the current information.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {
	saveCurrentRecord();
}

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

private void setOriginalCgoto(StateMod_OperationalRight o, String cgoto) {
	((StateMod_OperationalRight)o._original)._cgoto = cgoto;
}

/**
Called just before the worksheet is sorted.  Stores the index of the record
that is selected.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortAboutToChange(JWorksheet worksheet, int sort) {
	__sortSelectedRow = __worksheet.getOriginalRowNumber(
		__worksheet.getSelectedRow());
}

/**
Called when the worksheet is sorted.  Reselects the record that was selected
prior to the sort.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortChanged(JWorksheet worksheet, int sort) {
	__worksheet.deselectAll();
	__worksheet.selectRow(__worksheet.getSortedRowNumber(
		__sortSelectedRow));
}

}
