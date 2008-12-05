//------------------------------------------------------------------------------
// StateMod_Control_JFrame - dialog to edit/view control
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 24 Mar 1998	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 2001-08-08	SAM, RTi		Update for 9.92 version(add the choices
//					for ground water data).  Set background
//					on data period to gray since it is not
//					editable.  Add StateMod numeric values
//					to labels.  Add more units.  Add number
//					of precipitation and evaporation(view
//					only).  List choices in numeric order
//					according to StateMod conventions.
//					Catch exceptions so that an unrecognized
//					choice value is handled gracefully.
//					Change so buttons are more consistent
//					with industry standards - change OK to
//					Close.  Change Close to cancel.  Add
//					Apply.  Change the checkboxes to
//					choices so they can be put in the order
//					of the file, save GUI space, and more
//					easily allow handling of new control
//					values that are not coded into the GUI.
//					Fix so that multiple copies of the
//					window cannot be displayed.
//------------------------------------------------------------------------------
// 2003-06-24	J. Thomas Sapienza	Initial swing version.
// 2003-07-15	JTS, RTi		* Added checkInput() framework for 
//					validating user input prior to the 
//					values being saved.
// 					* Added status bar.
// 2003-08-03	SAM, RTi		* Control information is now managed
//					  in the StateMod_DataSet class and does
//					  not have its own class - update calls
//					  accordingly.
//					* Require a title parameter in the
//					  constructor.
//					* Add an editable parameter in the
//					  constructor
// 2003-08-16	SAM, RTi		Change the window type to
//					WINDOW_CONTROL.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-09-02	JTS, RTi		Revised after SAM's review.
//					* Changed various labels.
//					* Added values to combo boxes.
//					* Enabled combo boxes that had been
//					  disabled.
//					* Changed the layout of the units tab.
//					* Changed the overall tab order.
//					* Changed how unrecognized data is 
//					  handled at open and save time.
// 2003-09-04	JTS, RTi		* Changed references to files by only
//					  their extension (e.g., "*.rsp") to
//					  the file name (e.g., "response file").
//					* Changed some text field sizes.
//					* Compacted some tabs by rearranging
//					  label positions.
//					* GUI can now be opened in an 
//					  uneditable state.
// 2003-09-11	JTS, RTi		Massive revisions:
//					* Completely changed the placement 
//					  of variables within tabs.
//					* Removed half the tabs and spread the
//				 	  variables among the rest.
//					* Removed the numpre and numeva 
//					  variables.
//					* Changed runDate1 and runDate2 to be
//					  combo boxes.
// 2003-09-19	JTS, RTi		* Warnings fwhen the data values are 
//					  checked are now combined into one
//					  large warning.
//					* Corrected error in checking 'interv'
//					  value.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-09-24	JTS, RTi		Change name of method that gets POR.
// 2004-08-25	JTS, RTi		Updated GUI.
// 2004-08-26	JTS, RTi		Changed the interv combo box behavior
// 					so that it better handles other values.
// 2006-03-04	SAM, RTi		Clarify note about how data period is
//					determined.
// 2006-08-22	SAM, RTi		* Recognize more options for "ichk".
//					* Increase size of text field for
//					  "ccall".
//					* Add support for "isig".
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class is a GUI for displaying and editing control file data.
*/
public class StateMod_Control_JFrame extends JFrame
implements ActionListener, ItemListener, WindowListener {

/**
Button labels.
*/
private final String 
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_HELP = "Help";

/**
Whether the data on the form is editable or not.
*/
private boolean __editable = true;

/**
GUI buttons.
*/
private JButton 
	__applyJButton = null,
	__cancelJButton = null,
	__closeJButton = null,
	__helpJButton = null;

/**
GUI text fields.
*/
private JTextField
	__factor = null,
	__rfacto = null,
	__dfacto = null,
	__ffacto = null,
	__cfacto = null,
	__efacto = null,
	__pfacto = null,
	__ccall = null,
	__dataDate1 = null,
	__dataDate2 = null,
	__gwmaxrcJTextField = null,
	__interv = null,
	__ireopx = null,
	__soildJTextField = null,
	__title1 = null,
	__title2 = null;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
GUI SimpleJComboBoxes
*/
private SimpleJComboBox 
	__runDate1SimpleJComboBox = null,
	__runDate2SimpleJComboBox = null,
	__cyr1SimpleJComboBox = null,
	__icallSimpleJComboBox = null,
	__ichkSimpleJComboBox = null,
	__icondemSimpleJComboBox = null,
	__idaySimpleJComboBox = null,
	__ieffmaxSimpleJComboBox = null,
	__intervSimpleJComboBox = null,
	__iopfloSimpleJComboBox = null,
	__ireachSimpleJComboBox = null,
	__ireopxSimpleJComboBox = null,
	__iresopSimpleJComboBox = null,
	__isjripSimpleJComboBox = null,
	__isprinkSimpleJComboBox = null,
	__itsfileSimpleJComboBox = null,
	__iwellSimpleJComboBox = null,
	__monevaSimpleJComboBox = null,
	__soildSimpleJComboBox = null,
	__isigSimpleJComboBox = null;

/**
The dataset that contains all the information about the statemod data.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/** 
Constructor.
@param dataset the dataset in which the Control data with which to populate
this form is found.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param editable If true, the data in the window are editable.
*/
public StateMod_Control_JFrame (	StateMod_DataSet dataset,
					StateMod_DataSet_WindowManager
					dataset_wm, boolean editable )
{
	StateMod_GUIUtil.setTitle(this, dataset, "Control Data", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	

	__dataset = dataset;
	__dataset_wm = dataset_wm;

	__editable = editable;
	
	setupGUI();
}

/**
Handle action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String routine = "StateMod_Control_JFrame.actionPerformed";

	String action = event.getActionCommand();

	if (action.equals(__BUTTON_APPLY)) {
		try {	
			saveControls();
		}
		catch (Exception ex) {
			Message.printWarning(2, routine, ex);
		}
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (
			StateMod_DataSet_WindowManager.WINDOW_CONTROL );
		}
		else {	JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		try {	
			saveControls();
			closeWindow();
		}
		catch (Exception ex) {
			Message.printWarning(2, routine, ex);
			Message.printWarning(1, "",
				"Correct the error or Cancel.", this);
		}
	}
	else if (action.equals(__BUTTON_HELP)) {}
}

/**
Checks the text fields for validity before they are saved back into the
data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	List errors = new Vector();
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
		label += errors.get(i) + "\n";
	}
	new ResponseJDialog(this, 
		"Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Closes the window.
*/
protected void closeWindow()
{	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (
		StateMod_DataSet_WindowManager.WINDOW_CONTROL );
	}
	else {	JGUIUtil.close ( this );
	}
}

/**
Disables components (if the form is not in editable mode, for instance).
*/
private void disableComponents() {
	__factor.setEditable(false);
	__rfacto.setEditable(false);
	__dfacto.setEditable(false);
	__ffacto.setEditable(false);
	__cfacto.setEditable(false);
	__efacto.setEditable(false);
	__pfacto.setEditable(false);
	__ccall.setEditable(false);
	__dataDate1.setEditable(false);
	__dataDate2.setEditable(false);
	__gwmaxrcJTextField.setEditable(false);
	__interv.setEditable(false);
	__ireopx.setEditable(false);
	__soildJTextField.setEditable(false);
	__title1.setEditable(false);
	__title2.setEditable(false);
	__runDate1SimpleJComboBox.setEditable(false);
	__runDate1SimpleJComboBox.setEnabled(false);
	__runDate2SimpleJComboBox.setEditable(false);
	__runDate2SimpleJComboBox.setEnabled(false);
	__cyr1SimpleJComboBox.setEditable(false);
	__cyr1SimpleJComboBox.setEnabled(false);
	__icallSimpleJComboBox.setEditable(false);
	__icallSimpleJComboBox.setEnabled(false);
	__ichkSimpleJComboBox.setEditable(false);
	__ichkSimpleJComboBox.setEnabled(false);
	__icondemSimpleJComboBox.setEditable(false);
	__icondemSimpleJComboBox.setEnabled(false);
	__idaySimpleJComboBox.setEditable(false);
	__idaySimpleJComboBox.setEnabled(false);
	__ieffmaxSimpleJComboBox.setEditable(false);
	__ieffmaxSimpleJComboBox.setEnabled(false);
	__intervSimpleJComboBox.setEditable(false);
	__intervSimpleJComboBox.setEnabled(false);
	__iopfloSimpleJComboBox.setEditable(false);
	__iopfloSimpleJComboBox.setEnabled(false);
	__ireachSimpleJComboBox.setEditable(false);
	__ireachSimpleJComboBox.setEnabled(false);
	__ireopxSimpleJComboBox.setEditable(false);
	__ireopxSimpleJComboBox.setEnabled(false);
	__iresopSimpleJComboBox.setEditable(false);
	__iresopSimpleJComboBox.setEnabled(false);
	__isjripSimpleJComboBox.setEditable(false);
	__isjripSimpleJComboBox.setEnabled(false);
	__isprinkSimpleJComboBox.setEditable(false);
	__isprinkSimpleJComboBox.setEnabled(false);
	__itsfileSimpleJComboBox.setEditable(false);
	__itsfileSimpleJComboBox.setEnabled(false);
	__iwellSimpleJComboBox.setEditable(false);
	__iwellSimpleJComboBox.setEnabled(false);
	__monevaSimpleJComboBox.setEditable(false);
	__monevaSimpleJComboBox.setEnabled(false);
	__soildSimpleJComboBox.setEditable(false);
	__soildSimpleJComboBox.setEnabled(false);
	__isigSimpleJComboBox.setEditable(false);
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__applyJButton = null;
	__cancelJButton = null;
	__closeJButton = null;
	__helpJButton = null;
	__ccall = null;
	__dataDate1 = null;
	__dataDate2 = null;
	__gwmaxrcJTextField = null;
	__interv = null;
	__ireopx = null;
	__runDate1SimpleJComboBox = null;
	__runDate2SimpleJComboBox = null;
	__soildJTextField = null;
	__title1 = null;
	__title2 = null;
	__factor = null;
	__rfacto = null;
	__dfacto = null;
	__ffacto = null;
	__cfacto = null;
	__efacto = null;
	__pfacto = null;
	__cyr1SimpleJComboBox = null;
	__icallSimpleJComboBox = null;
	__ichkSimpleJComboBox = null;
	__icondemSimpleJComboBox = null;
	__idaySimpleJComboBox = null;
	__ieffmaxSimpleJComboBox = null;
	__intervSimpleJComboBox = null;
	__iopfloSimpleJComboBox = null;
	__ireachSimpleJComboBox = null;
	__ireopxSimpleJComboBox = null;
	__iresopSimpleJComboBox = null;
	__isjripSimpleJComboBox = null;
	__isprinkSimpleJComboBox = null;
	__itsfileSimpleJComboBox = null;
	__iwellSimpleJComboBox = null;
	__monevaSimpleJComboBox = null;
	__soildSimpleJComboBox = null;
	__isigSimpleJComboBox = null;

	super.finalize();
}

/**
Responds to item state changed events.
@param event the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent event) {
	Object source = event.getSource();
	if ((source == __intervSimpleJComboBox) && 
	    (event.getStateChange() == ItemEvent.SELECTED)) {
		int selectedIndex = __intervSimpleJComboBox.getSelectedIndex();
		if (selectedIndex == 1) {	
			__interv.setEditable(false);
			__interv.setText("");
		}
		else if (selectedIndex == 2) {	
			__interv.setEditable(false);
			__interv.setText("");
		}
		else {	__interv.setEditable(true);
			StateMod_GUIUtil.checkAndSet(__dataset.getInterv(),
				__interv);
		}
	}
	else if (source == __ireopxSimpleJComboBox && 
		event.getStateChange() == ItemEvent.SELECTED) {
		int selectedIndex = __ireopxSimpleJComboBox.getSelectedIndex();
		if (selectedIndex <= 1) {
			__ireopx.setEditable(false);
			__ireopx.setText("");
		}
		else {	__ireopx.setEditable(true);
			StateMod_GUIUtil.checkAndSet(__dataset.getIreopx(),
				__ireopx);
		}
	}
	else if (source == __icallSimpleJComboBox) {
		if (__icallSimpleJComboBox.getSelectedIndex() == 0) {
			__ccall.setEditable(false);
		}
		else {	
			__ccall.setEditable(true);
		}
	}
	else if (source == __iwellSimpleJComboBox) {
		if (__iwellSimpleJComboBox.getSelectedIndex() == 3) {
			__gwmaxrcJTextField.setEditable(true);
		}
		else {	
			__gwmaxrcJTextField.setEditable(false);
		}
	}
	else if (source == __soildSimpleJComboBox) {
		if (__soildSimpleJComboBox.getSelectedIndex() == 2) {
			__soildJTextField.setEditable(true);
			if (!StateMod_Util.isMissing(__dataset.getSoild())) {
				__soildJTextField.setText(
					StringUtil.formatString(
					__dataset.getSoild(),"%.1f"));
			}
			else {
				__soildJTextField.setText("");
			}
		}
		else {	
			__soildJTextField.setEditable(false);
			__soildJTextField.setText("");
		}
	}
}

/**
Save the control information in the GUI.  Throw an exception if the visible data
are not correct(e.g., character string in integer field).
@exception Exception if the visible data are not correct.
*/
protected void saveControls()
throws Exception {
	if (!checkInput()) {
		return;
	}

	__dataset.setHeading1(__title1.getText());
	__dataset.setHeading2(__title2.getText());

	__dataset.setIystr(__runDate1SimpleJComboBox.getSelected());
	__dataset.setIyend(__runDate2SimpleJComboBox.getSelected());

	String message = null;

	String s = __intervSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setInterv(s.substring(0, sindex));
	}
	else {
		if (__intervSimpleJComboBox.getSelectedIndex() == 1) {
			__dataset.setInterv(-1);
		}
		else if (__intervSimpleJComboBox.getSelectedIndex() == 2) {
			__dataset.setInterv(-100);
		}
		else { 
			if (!StringUtil.isInteger((String)__interv.getText())) {
				if (message == null) {
					message = "\n";
				}
				message += "interv value is not an integer "
					+ "value\n";
			}
			__dataset.setInterv(__interv.getText());
		}
	}

	s = __rfacto.getText();
	if (!StringUtil.isDouble(s)) {
		if (message == null) {
			message = "\n";
		}
		message += "rfacto value is not a double value\n";
	}
	__dataset.setRfacto(s);

	s = __dfacto.getText();
	if (!StringUtil.isDouble(s)) {
		if (message == null) {
			message = "\n";
		}
		message += "dfacto value is not a double value\n";
	}
	__dataset.setDfacto(s);

	s = __ffacto.getText();
	if (!StringUtil.isDouble(s)) {
		if (message == null) {
			message = "\n";
		}
		message += "ffacto value is not a double value";
	}
	__dataset.setFfacto(s);

	s = __cfacto.getText();
	if (!StringUtil.isDouble(s)) {
		message = "cfacto value is not a double value\n";
	}
	__dataset.setCfacto(s);

	s = __efacto.getText();
	if (!StringUtil.isDouble(s)) {
		if (message == null) {
			message = "\n";
		}
		message += "efacto value is not a double value\n";
	}
	__dataset.setEfacto(s);

	s = __pfacto.getText();
	if (!StringUtil.isDouble(s)) {
		if (message == null) {
			message = "\n";
		}
		message += "pfacto value is not a double value\n";
	}
	__dataset.setPfacto(s);

	s = __icondemSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIcondem(s.substring(0, sindex));
	}
	else {
		// Values 1-5 are supported with anything more being a literal
		// value in the last slot...
		int icondemSelectIndex = 
			__icondemSimpleJComboBox.getSelectedIndex();
		if (icondemSelectIndex <= 4) {
			__dataset.setIcondem(
				__icondemSimpleJComboBox.getSelectedIndex()+ 1);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__icondemSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "icondem value is not an integer "
					+ "value\n";
			}
			__dataset.setIcondem(
			StringUtil.atoi((String)
				__icondemSimpleJComboBox.getSelectedItem()));
		}
	}

	// REVISIT SAM 2006-08-22
	// Need to evaluate other items to see if they can be set as simply
	// as here.  Using getToken() simplifies processing.

	s = __ichkSimpleJComboBox.getSelected();
	// Don't split on dash "-" because a negative flag may be specified...
	String token0 = StringUtil.getToken ( s, " ", 0, 0 ).trim();
	if ( !StringUtil.isInteger(token0) ) {
		if (message == null) {
			message = "\n";
		}
		message += "ichk value is not an integer value\n";
	}
	else {	__dataset.setIchk(StringUtil.atoi( token0 ) );
	}

	s = __ireopxSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIreopx(s.substring(0, sindex));
	}
	else {
		if (__ireopxSimpleJComboBox.getSelectedIndex() == 2) {
			if (!StringUtil.isInteger((String)__ireopx.getText())) {
				if (message == null) {
					message = "\n";
				}
				message += "ireopx value is not an integer "
					+ "value\n";
		}
		int ireopx = -1 * Math.abs(StringUtil.atoi((String)
			__ireopx.getText()));
		__dataset.setIreopx(ireopx);
		}
		else if (__ireopxSimpleJComboBox.getSelectedIndex() < 2) {
			__dataset.setIreopx(
				__ireopxSimpleJComboBox.getSelectedIndex());
		}
		else {	
			if (!StringUtil.isInteger((String)
				__ireopxSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "ireopx value is not an integer "
					+ "value\n";
			}
			__dataset.setIreopx(
				StringUtil.atoi((String)
				__ireopxSimpleJComboBox.getSelectedItem()));
		}
	}

	s = __ireachSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIreach(s.substring(0, sindex));
	}
	else {
		int ireachSelectIndex = 
			__ireachSimpleJComboBox.getSelectedIndex();
		if ((ireachSelectIndex >= 0) && (ireachSelectIndex <= 3)) {
			__dataset.setIreach(ireachSelectIndex);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__ireachSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "ireach value is not an integer "
					+ "value\n";
			}
			__dataset.setIreach(
				StringUtil.atoi((String)
				__ireachSimpleJComboBox.getSelectedItem()));
		}
	}

	s = __icallSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIcall(s.substring(0, sindex));
	}
	else {
		int icallSelectIndex =__icallSimpleJComboBox.getSelectedIndex();
		if ((icallSelectIndex >= 0) && (icallSelectIndex <= 1)) {
			__dataset.setIcall(icallSelectIndex);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__icallSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "icall value is not an integer "
					+ "value\n";
			}
			__dataset.setIcall(
				StringUtil.atoi((String)
				__icallSimpleJComboBox.getSelectedItem()));
		}
	}

	__dataset.setCcall(__ccall.getText());

	s = __idaySimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIday(s.substring(0, sindex));
	}
	else {
		int idaySelectIndex = __idaySimpleJComboBox.getSelectedIndex();
		if ((idaySelectIndex >= 0) && (idaySelectIndex <= 2)) {
			__dataset.setIday(idaySelectIndex);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__idaySimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "iday value is not an integer "
					+ "value\n";
			}
			__dataset.setIday(
				StringUtil.atoi((String)
				__idaySimpleJComboBox.getSelectedItem()));
		}
	}

	// iwell...
	s = __iwellSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIwell(s.substring(0, sindex));
	}
	else {	
		int iwellSelectIndex =__iwellSimpleJComboBox.getSelectedIndex();
		if ((iwellSelectIndex >= 0) && (iwellSelectIndex <= 4)) {
			__dataset.setIwell(iwellSelectIndex - 1);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__iwellSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "iwell value is not an integer "
					+ "value\n";
			}
			__dataset.setIwell(
				StringUtil.atoi((String)
				__iwellSimpleJComboBox.getSelectedItem()));
		}
	}

	// gwmaxrc...
	if (!StringUtil.isDouble(__gwmaxrcJTextField.getText())) {
		int windex = __iwellSimpleJComboBox.getSelectedIndex();
		if (windex == 3) {
			// only #2 (the 3rd element) can have a valid value
			// for gwmaxrc.  The rest are allowed to have blank
			// values.			
			if (message == null) {
				message = "\n";
			}
			message += "gwmaxrc value is not a number\n";
		}
	}
	else {
		__dataset.setGwmaxrc(__gwmaxrcJTextField.getText());
	}

	// isjrip...
	s = __isjripSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIsjrip(s.substring(0, sindex));
	}
	else {	
		int isjripSelectIndex = 
			__isjripSimpleJComboBox.getSelectedIndex();
		if ((isjripSelectIndex >= 0) && (isjripSelectIndex <= 2)) {
			__dataset.setIsjrip(isjripSelectIndex - 1);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__isjripSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "isjrip value is not an integer "
					+ "value\n";
			}
			__dataset.setIsjrip(
				StringUtil.atoi((String)
				__isjripSimpleJComboBox.getSelectedItem()));
		}
	}

	// itsfile...
	s = __itsfileSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setItsfile(s.substring(0, sindex));
	}
	else {	
		int itsfileSelectIndex = 
			__itsfileSimpleJComboBox.getSelectedIndex();
		if ((itsfileSelectIndex >= 0) && (itsfileSelectIndex <= 3)) {
			__dataset.setItsfile(itsfileSelectIndex - 1);
		}
		else if (itsfileSelectIndex == 4) {
			__dataset.setItsfile(10);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__itsfileSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "itsfile value is not an integer "
					+ "value\n";
			}
			__dataset.setItsfile(
				StringUtil.atoi((String)
				__itsfileSimpleJComboBox.getSelectedItem()));
		}
	}

	// ieffmax...
	s = __ieffmaxSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIeffmax(s.substring(0, sindex));
	}
	else {
		int ieffmaxSelectIndex = 
			__ieffmaxSimpleJComboBox.getSelectedIndex();
		if ((ieffmaxSelectIndex >= 0) && (ieffmaxSelectIndex <= 2)) {
			__dataset.setIeffmax(ieffmaxSelectIndex - 1);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__ieffmaxSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "ieffmax value is not an integer "
					+ "value\n";
			}
			__dataset.setIeffmax(
				StringUtil.atoi((String)
				__ieffmaxSimpleJComboBox.getSelectedItem()));
		}
	}

	// isprink...
	s = __isprinkSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIsprink(s.substring(0, sindex));
	}
	else {	
		int isprinkSelectIndex = 
			__isprinkSimpleJComboBox.getSelectedIndex();
		if ((isprinkSelectIndex >= 0) && (isprinkSelectIndex <= 1)) {
			__dataset.setIsprink(isprinkSelectIndex);
		}
		else {	
			if (!StringUtil.isInteger((String)
				__isprinkSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "isprink value is not an integer "
					+ "value\n";
			}
			__dataset.setIsprink(
				StringUtil.atoi((String)
				__isprinkSimpleJComboBox.getSelectedItem()));
		}
	}

	s = __soildSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setSoild(s.substring(0, sindex));
	}
	else {
		if (__soildSimpleJComboBox.getSelectedIndex() == 2) {
			if (!StringUtil.isDouble(__soildJTextField.getText())) {
				if (message == null) {
					message = "\n";
				}
				message += "soild value is not a number\n";
			}
			__dataset.setSoild(__soildJTextField.getText());
		}
		else if (__soildSimpleJComboBox.getSelectedIndex() <= 1) {
		__dataset.setSoild("" 
			+ (__soildSimpleJComboBox.getSelectedIndex()-1));
		}
		else {	
			if (!StringUtil.isDouble((String)
				__soildSimpleJComboBox.getSelectedItem())) {
				if (message == null) {
					message = "\n";
				}
				message += "soild value is not a number\n";
			}
			__dataset.setSoild((String)
				__soildSimpleJComboBox.getSelectedItem());
		}
	}

	s = __isigSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIsig(s.substring(0, sindex));
	}
	else {	__dataset.setIsig( __isigSimpleJComboBox.getSelectedIndex());
	}

	if (message != null) {
		Message.printWarning(1, "", message, this);
		throw new Exception(message);
	}

	int index = 0;

	s = __iresopSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIresop(s.substring(0, sindex));
	}
	else {
		index = __iresopSimpleJComboBox.getSelectedIndex();
		index++;
		__dataset.setIresop(index);
	}

	s = __cyr1SimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setCyrl(s.substring(0, sindex));
	}
	else {
		index = __cyr1SimpleJComboBox.getSelectedIndex();
		if (index == 0) {
			__dataset.setCyrl(StateMod_DataSet.SM_CYR);
		}
		else if (index == 1) {
			__dataset.setCyrl(StateMod_DataSet.SM_IYR);
		}
		else if (index == 2) {
			__dataset.setCyrl(StateMod_DataSet.SM_WYR);
		}
	}

	s = __iopfloSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIopflo(s.substring(0, sindex));
	}
	else {
		index = __iopfloSimpleJComboBox.getSelectedIndex();
		index++;
		__dataset.setIopflo(index);
	}

	s = __monevaSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setMoneva(s.substring(0, sindex));
	}
	else {
		index = __monevaSimpleJComboBox.getSelectedIndex();
		__dataset.setMoneva(index);
	}

	s = __isjripSimpleJComboBox.getSelected();
	if (StringUtil.endsWithIgnoreCase(s, "- Unknown")) {
		int sindex = s.indexOf(" - ");
		__dataset.setIsjrip(s.substring(0, sindex));
	}
	else {
		index = __isjripSimpleJComboBox.getSelectedIndex();
		index--;
		__dataset.setIsjrip(index);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "StateMod_Control_JFrame.setupGUI";

	addWindowListener(this);
	
	GridBagLayout gb = new GridBagLayout();

	__applyJButton = new JButton(__BUTTON_APPLY);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__helpJButton = new JButton(__BUTTON_HELP);

	__title1 = new JTextField(45);
	__title2 = new JTextField(45);

	__dataDate1 = new JTextField(5);
	__dataDate2 = new JTextField(5);

	JPanel timePanel = new JPanel();
	timePanel.setLayout(gb);
	
	JPanel  generalPanel = new JPanel();
	generalPanel.setLayout(gb);

	JPanel optionsPanel = new JPanel();
	optionsPanel.setLayout(gb);
	
	JPanel  factorsPanel = new JPanel();
	factorsPanel.setLayout(gb);
	
	JPanel  checksPanel = new JPanel();
	checksPanel.setLayout(gb);
	
	JPanel  advancedPanel = new JPanel();
	advancedPanel.setLayout(gb);
	
	////////////////////////////////////////////////////////
	// Units choice(iresop)...
	__iresopSimpleJComboBox = new SimpleJComboBox();
	__iresopSimpleJComboBox.add("1 - CFS for all");
	__iresopSimpleJComboBox.add("2 - ACFT for all");
	__iresopSimpleJComboBox.add("3 - KACFT for all");
	__iresopSimpleJComboBox.add("4 - CFS for daily and ACFT for monthly");
	__iresopSimpleJComboBox.add("5 - CMS for all");
	
	String message = null;
	
	int iresop = __dataset.getIresop();
	if (iresop == StateMod_DataSet.SM_CFS) {
		__iresopSimpleJComboBox.select(0);
	}
	else if (iresop == StateMod_DataSet.SM_ACFT) {
		__iresopSimpleJComboBox.select(1);
	}
	else if (iresop == StateMod_DataSet.SM_KACFT) {
		__iresopSimpleJComboBox.select(2);
	}
	else if (iresop == StateMod_DataSet.SM_CFS_ACFT) {
		__iresopSimpleJComboBox.select(3);
	}
	else if (iresop == StateMod_DataSet.SM_CMS) {
		__iresopSimpleJComboBox.select(4);
	}
	else {	
		if (message == null) {
			message = "\n";
		}
		message += "iresop value " + __dataset.getIresop()
			+ " not recognized by GUI.  Allowing as is.\n";
		__iresopSimpleJComboBox.add("" + __dataset.getIresop()
			+ " - Unknown");
		__iresopSimpleJComboBox.select(5);
	}

	////////////////////////////////////////////////////////
	// Calendar choice(cyr1)...
	__cyr1SimpleJComboBox = new SimpleJComboBox();
	__cyr1SimpleJComboBox.add("CYR - Calendar Year (Jan - Dec)");
	__cyr1SimpleJComboBox.add("IYR - Irrigation Year (Nov - Oct)");
	__cyr1SimpleJComboBox.add("WYR - Water Year (Oct - Sep)");
	
	int cyrl = __dataset.getCyrl();
	if (cyrl == StateMod_DataSet.SM_CYR) {
		__cyr1SimpleJComboBox.select(0);
	}
	else if (cyrl == StateMod_DataSet.SM_IYR) {
		__cyr1SimpleJComboBox.select(1);
	}
	else if (cyrl == StateMod_DataSet.SM_WYR) {
		__cyr1SimpleJComboBox.select(2);
	}
	else {	
		Message.printWarning(1, routine,
			"cyr1 value not recognized by GUI.");
	}

	////////////////////////////////////////////////////////
	// evap/precip interval choice(moneva)...
	__monevaSimpleJComboBox = new SimpleJComboBox();
	__monevaSimpleJComboBox.add("0 - Monthly");
	__monevaSimpleJComboBox.add("1 - Average Monthly");
	
	try {	
		__monevaSimpleJComboBox.select(__dataset.getMoneva());
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "moneva value " + __dataset.getMoneva()
			+ " not recognized by GUI.  Allowing as is.\n";
		__monevaSimpleJComboBox.add("" + __dataset.getMoneva()
			+ " - Unknown");
		__monevaSimpleJComboBox.select(2);
	}

	////////////////////////////////////////////////////////
	// Streamflow type choice (iopflo)...
	__iopfloSimpleJComboBox = new SimpleJComboBox();
	__iopfloSimpleJComboBox.add("1 - Total");
	__iopfloSimpleJComboBox.add("2 - Gains");
	
	try {	
		__iopfloSimpleJComboBox.select(__dataset.getIopflo()- 1);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "iopflo value " + __dataset.getIopflo()
			+ " not recognized by GUI.  Allowing as is.\n";
		__iopfloSimpleJComboBox.add("" + __dataset.getIopflo()
			+ " - Unknown");
		__iopfloSimpleJComboBox.select(2);
	}

	////////////////////////////////////////////////////////
	// interv...
	__interv = new JTextField(5);
	int intervSelectIndex = 0;
	boolean showIntervText = true;
	
	__intervSimpleJComboBox = new SimpleJComboBox();
	__intervSimpleJComboBox.add("+n - Fixed:"); 
	__intervSimpleJComboBox.add("-1 - Variable as %"); 
	__intervSimpleJComboBox.add("-100 - Variable as decimal"); 
	
	if (__dataset.getInterv() == -1) {
		intervSelectIndex = 1;
		__interv.setText("");
		showIntervText = false;
	}
	else if (__dataset.getInterv() == -100) {
		intervSelectIndex = 2;
		__interv.setText("");
		showIntervText = false;
	}
	else {
		intervSelectIndex = 0;
		__interv.setText((new Integer(__dataset.getInterv()))
			.toString());
	}

	__interv.setEditable(showIntervText);
	__intervSimpleJComboBox.select(intervSelectIndex);
	__intervSimpleJComboBox.addItemListener(this);

	////////////////////////////////////////////////////////
	// ichk - only allow users the option of other than -n and 100+n since
	//	it is too complex for a typical user
	int ichkSelectIndex = __dataset.getIchk();
	__ichkSimpleJComboBox = new SimpleJComboBox();
	__ichkSimpleJComboBox.add("0 - No detailed results"); 
	__ichkSimpleJComboBox.add("1 - Print river network"); 
	__ichkSimpleJComboBox.add("4 - Print detailed calls data"); 
	__ichkSimpleJComboBox.add("5 - Print detailed demand data"); 
	__ichkSimpleJComboBox.add("6 - Print detailed daily data"); 
	__ichkSimpleJComboBox.add("7 - Print detailed return flow data"); 
	__ichkSimpleJComboBox.add("8 - Print detailed daily baseflow data"); 
	__ichkSimpleJComboBox.add("9 - Print detailed reoperation data"); 
	__ichkSimpleJComboBox.add("10 - Echo operating right file read"); 
	__ichkSimpleJComboBox.add(
		"21 - Print top of binary file for *.xbn report"); 
	__ichkSimpleJComboBox.add(
		"24 - Print detailed results for operating rule 23" +
		" (downstream call)"); 
	__ichkSimpleJComboBox.add("30 - Do not print daily binary results"); 
	__ichkSimpleJComboBox.add(
		"90 - Print detailed water use data from return"); 
	__ichkSimpleJComboBox.add("91 - Print well water right details"); 
	__ichkSimpleJComboBox.add("92 - Print detailed soil moisture data" );
	__ichkSimpleJComboBox.add(
		"201 - Print detailed output for ISF call water right of " +
		"interest" );
	__ichkSimpleJComboBox.add(
		"202 - Print detailed output for reservoir call water right of "
		+ "interest" );
	__ichkSimpleJComboBox.add(
		"203 - Print detailed output for diversion call water right of "
		+ "interest" );
		
	if ((ichkSelectIndex != 0) && (ichkSelectIndex != 1)
	 && (ichkSelectIndex != 4) && (ichkSelectIndex != 5)
	 && (ichkSelectIndex != 6) && (ichkSelectIndex != 7)
	 && (ichkSelectIndex != 8) && (ichkSelectIndex != 9)
	 && (ichkSelectIndex != 10) && (ichkSelectIndex != 21)
	 && (ichkSelectIndex != 24) && (ichkSelectIndex != 30)
	 && (ichkSelectIndex != 90) && (ichkSelectIndex != 91)
	 && (ichkSelectIndex != 92) && (ichkSelectIndex != 201)
	 && (ichkSelectIndex != 202) && (ichkSelectIndex != 203)) {
		if (message == null) {
			message = "\n";
		}	 
	 	message += "Detailed output \"ichk\" value " + ichkSelectIndex
			+ " not recognized by GUI.  Allowing as is.\n";
		__ichkSimpleJComboBox.add("" + ichkSelectIndex + " - Unknown");
	}

	// Select - have to do manually because the possible values are not
	// sequential
	if ((ichkSelectIndex == 0) || (ichkSelectIndex == 1)) {
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else if ((ichkSelectIndex >= 4) && (ichkSelectIndex <= 10)) {
		ichkSelectIndex -= 2;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else if ( ichkSelectIndex == 21 ) {
		ichkSelectIndex = 9;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else if ( ichkSelectIndex == 24 ) {
		ichkSelectIndex = 10;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else if ( ichkSelectIndex == 30 ) {
		ichkSelectIndex = 11;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else if ((ichkSelectIndex >= 90) && (ichkSelectIndex <= 92)) {
		ichkSelectIndex -= 78;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else if ((ichkSelectIndex >= 201) && (ichkSelectIndex <= 203)) {
		ichkSelectIndex -= 186;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	else {	// Unknown...
		ichkSelectIndex = 18;
		__ichkSimpleJComboBox.select(ichkSelectIndex);
	}
	__ichkSimpleJComboBox.addItemListener(this);

	////////////////////////////////////////////////////////
	// ireopx
	__ireopx = new JTextField(5);
	int ireopxSelectIndex = 0;
	
	if (__dataset.getIreopx() < 0) {	
		// print at res release amt.
		ireopxSelectIndex = 2;
		__ireopx.setEditable(true);
		StateMod_GUIUtil.checkAndSet(Math.abs(__dataset.getIreopx()),
			__ireopx);
	}
	else {	
		ireopxSelectIndex = __dataset.getIreopx();
		__ireopx.setEditable(false);
		__ireopx.setText("");
	}

	__ireopxSimpleJComboBox = new SimpleJComboBox();
	__ireopxSimpleJComboBox.add("0 - Reoperate for reservoir releases and "
		+ "returns"); 
	__ireopxSimpleJComboBox.add("1 - Do not reoperate"); 
	__ireopxSimpleJComboBox.add("Reoperate when the sum (acft) of "
		+ "reservoir releases exceeds:");
	try {	
		__ireopxSimpleJComboBox.select(ireopxSelectIndex);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "ireopx value " + __dataset.getIreopx()
			+ " not recognized by GUI.  Allowing as is.";
		__ireopxSimpleJComboBox.add("" + __dataset.getIreopx() 
			+ " - Unknown");
		__ireopxSimpleJComboBox.select(3);
	}
	__ireopxSimpleJComboBox.addItemListener(this);

	////////////////////////////////////////////////////////
	// soild
	__soildJTextField = new JTextField(5);
	int soildSelectIndex = 0;
	
	if (__dataset.getSoild() > 0) {
		soildSelectIndex = 2;
		__soildJTextField.setEditable(true);
		StateMod_GUIUtil.checkAndSet(__dataset.getSoild(), 
			__soildJTextField);
	}
	else if (__dataset.getSoild() < 0) {
		soildSelectIndex = 0;
		__soildJTextField.setEditable(false);
		__soildJTextField.setText("");
	}
	else {	
		// Presumably zero...
		soildSelectIndex = 1;
		__soildJTextField.setEditable(false);
		__soildJTextField.setText("");
	}
	
	__soildSimpleJComboBox = new SimpleJComboBox();
	__soildSimpleJComboBox.add("-1 - Soil moisture file provided but not "
		+ "used"); 
	__soildSimpleJComboBox.add("0 - No soil moisture file provided"); 
	__soildSimpleJComboBox.add("Soil moisture file used where soil "
		+ "zone depth, FT, is:");
	__soildSimpleJComboBox.select(soildSelectIndex);
	__soildSimpleJComboBox.addItemListener(this);

	////////////////////////////////////////////////////////
	// isig

	int isigSelectIndex = 0;
	__isigSimpleJComboBox = new SimpleJComboBox();
	__isigSimpleJComboBox.add("0 - No significant figures" );
	__isigSimpleJComboBox.add("1 - One significant figure" );
	__isigSimpleJComboBox.add("2 - Two significant figures" );
	try {	
		__isigSimpleJComboBox.select(isigSelectIndex);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "Significant figures \"isig\" value " +
			__dataset.getIsig()
			+ " not recognized by GUI.  Allowing as is.";
		__isigSimpleJComboBox.add("" + __dataset.getIsig() 
			+ " - Unknown");
		__isigSimpleJComboBox.select(3);
	}
	__isigSimpleJComboBox.addItemListener(this);

	////////////////////////////////////////////////////////
	// ccall, numpre, numeva, gwmaxrc
	__ccall = new JTextField(10);
	__gwmaxrcJTextField = new JTextField(5);

	////////////////////////////////////////////////////////
	// incondem
	__icondemSimpleJComboBox = new SimpleJComboBox();
	__icondemSimpleJComboBox.add("1 - Historic Demand Approach");
	__icondemSimpleJComboBox.add("2 - Historic Sum Demand Approach");
	__icondemSimpleJComboBox.add("3 - Structure Demand Approach");
	__icondemSimpleJComboBox.add("4 - Supply Demand Approach");
	__icondemSimpleJComboBox.add("5 - Decreed Demand Approach");
	
	try {	
		__icondemSimpleJComboBox.select(__dataset.getIcondem() - 1);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "icondem value " + __dataset.getIcondem()
			+ " not recognized by GUI.  Allowing as is.\n";
		__icondemSimpleJComboBox.add("" + __dataset.getIcondem() 
			+ " - Unknown");
		__icondemSimpleJComboBox.select(5);
	}

	////////////////////////////////////////////////////////
	// ireach
	__ireachSimpleJComboBox = new SimpleJComboBox();
	__ireachSimpleJComboBox.add("0 - No instream reach approach");
	__ireachSimpleJComboBox.add("1 - Instream reach approach");
	__ireachSimpleJComboBox.add("2 - No instream reach approach and "
		+ "monthly instream demands file");
	__ireachSimpleJComboBox.add("3 - Instream reach approach and monthly "
		+ "instream demands file");
		
	try {	
		__ireachSimpleJComboBox.select(__dataset.getIreach());
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "ireach value " + __dataset.getIreach()
			+ " not recognized by GUI.  Allowing as is.\n";
		__ireachSimpleJComboBox.add("" + __dataset.getIreach()
			+ " - Unknown");
		__ireachSimpleJComboBox.select(4);
	}

	////////////////////////////////////////////////////////
	// icall
	__icallSimpleJComboBox = new SimpleJComboBox();
	__icallSimpleJComboBox.add("0 - No detailed call data");
	__icallSimpleJComboBox.add("1 - Provide detailed call data");
	__icallSimpleJComboBox.addItemListener(this);
	
	try {	
		__icallSimpleJComboBox.select(__dataset.getIcall());
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "icall value " + __dataset.getIcall()
			+ " not recognized by GUI.  Allowing as is.\n";
		__icallSimpleJComboBox.add("" + __dataset.getIcall()
			+ " - Unknown");
		__icallSimpleJComboBox.select(2);
	}
	
	////////////////////////////////////////////////////////
	// iday
	__idaySimpleJComboBox = new SimpleJComboBox();
	__idaySimpleJComboBox.add("-1 - No daily analysis but daily data files "
		+ "are in response file");
	__idaySimpleJComboBox.add("0 - Monthly analysis");
	__idaySimpleJComboBox.add("1 - Daily analysis");
	
	try {	
		__idaySimpleJComboBox.select(__dataset.getIday());
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "iday value " + __dataset.getIday()
			+ " not recognized by GUI.  Allowing as is.\n";
		__idaySimpleJComboBox.add("" + __dataset.getIday()
			+ " - Unknown");
		__idaySimpleJComboBox.select(2);
	}

	////////////////////////////////////////////////////////
	// iwell
	__iwellSimpleJComboBox = new SimpleJComboBox();
	__iwellSimpleJComboBox.addItemListener(this);
	__iwellSimpleJComboBox.add("-1 - No well analysis but well data files "
		+ "are in response file");
	__iwellSimpleJComboBox.add("0 - No well analysis");
	__iwellSimpleJComboBox.add("1 - Well analysis with no maximum "
		+ "recharge");
	__iwellSimpleJComboBox.add("2 - Well analysis with constant "
		+ "maximum recharge");
	__iwellSimpleJComboBox.add("3 - Well analysis with variable "
		+ "maximum recharge");
		
	try {	
		__iwellSimpleJComboBox.select(__dataset.getIwell() + 1);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "iwell value " + __dataset.getIwell()
			+ " not recognized by GUI.  Allowing as is.\n";
		__iwellSimpleJComboBox.add("" + __dataset.getIwell()
			+ " - Unknown");
		__iwellSimpleJComboBox.select(5);
	}

	if (__dataset.getIwell()!= 2) {
		__gwmaxrcJTextField.setEditable(false);
	}

	////////////////////////////////////////////////////////
	// isjrip
	__isjripSimpleJComboBox = new SimpleJComboBox();
	__isjripSimpleJComboBox.add("-1 - No SJRIP analysis but file in "
		+ "response file");
	__isjripSimpleJComboBox.add("0 - No SJRIP analysis");
	__isjripSimpleJComboBox.add("1 - SJRIP file provided");
	
	try {	
		__isjripSimpleJComboBox.select(__dataset.getIsjrip() + 1);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "isjrip value " + __dataset.getIsjrip()
			+ " not recognized by GUI.  Allowing as is.\n";
		__isjripSimpleJComboBox.add("" + __dataset.getIsjrip()
			+ " - Unknown");
		__isjripSimpleJComboBox.select(3);
	}

	////////////////////////////////////////////////////////
	// itsfile
	__itsfileSimpleJComboBox = new SimpleJComboBox();
	__itsfileSimpleJComboBox.add("-1 - File in response file but not used");
	__itsfileSimpleJComboBox.add("0 - No file provided");
	__itsfileSimpleJComboBox.add("1 - Use annual ground water area limit");
	__itsfileSimpleJComboBox.add("2 - Use annual well capacity");
	__itsfileSimpleJComboBox.add("10 - Use all data in file");	

	if ((__dataset.getItsfile() >= -1) && (__dataset.getItsfile() <= 2)) {
		__itsfileSimpleJComboBox.select(__dataset.getItsfile() + 1);
	}
	else if (__dataset.getItsfile() == 10) {
		__itsfileSimpleJComboBox.select(4);
	}
	else {	
		if (message == null) {
			message = "\n";
		}	
		message += "itsfile value " + __dataset.getItsfile()
			+ " not recognized by GUI.  Allowing as is.\n";
		__itsfileSimpleJComboBox.add("" + __dataset.getItsfile()
			+ " - Unknown");
		__itsfileSimpleJComboBox.select(5);
	}

	////////////////////////////////////////////////////////
	// ieffmax
	__ieffmaxSimpleJComboBox = new SimpleJComboBox();
	__ieffmaxSimpleJComboBox.add("-1 - IWR file in response file but not "
		+ "used");
	__ieffmaxSimpleJComboBox.add("0 - No IWR file");
	__ieffmaxSimpleJComboBox.add("1 - IWR file provided and variable "
		+ "efficiency used");
		
	try {	
		__ieffmaxSimpleJComboBox.select(__dataset.getIeffmax() + 1);
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "ieffmax value " + __dataset.getIeffmax()
			+ " not recognized by GUI.  Allowing as is.\n";
		__ieffmaxSimpleJComboBox.add("" + __dataset.getIeffmax()
			+ " - Unknown");
		__ieffmaxSimpleJComboBox.select(3);
	}

	////////////////////////////////////////////////////////
	// isprink
	__isprinkSimpleJComboBox = new SimpleJComboBox();
	__isprinkSimpleJComboBox.add("0 - No sprinkler data used");
	__isprinkSimpleJComboBox.add("1 - Use sprinkler data from irrigation "
		+ "practice file");

	try {	
		__isprinkSimpleJComboBox.select(__dataset.getIsprink());
	}
	catch (Exception e) {
		if (message == null) {
			message = "\n";
		}	
		message += "isprink value " + __dataset.getIsprink()
			+ " not recognized by GUI.  Allowing as is.\n";
		__isprinkSimpleJComboBox.add("" + __dataset.getIsprink()
			+ " - Unknown");
		__isprinkSimpleJComboBox.select(2);
	}

	////////////////////////////////////////////////////////
	// earliest POR
	DateTime earliestPOR_DateTime =
		StateMod_Util.findEarliestDateInPOR(__dataset);
	if (earliestPOR_DateTime == null) {
		__dataDate1.setText("NA");
	}
	else {	StateMod_GUIUtil.checkAndSet(earliestPOR_DateTime.getYear(),
			__dataDate1);
	}

	////////////////////////////////////////////////////////
	// latestPOR
	DateTime latestPOR_DateTime =
		StateMod_Util.findLatestDateInPOR(__dataset);
	if (latestPOR_DateTime == null) {
		__dataDate2.setText("NA");
	}
	else {	StateMod_GUIUtil.checkAndSet(latestPOR_DateTime.getYear(),
			__dataDate2);
	}

	__dataDate1.setEditable(false);
	__dataDate2.setEditable(false);

	__title1.setText(__dataset.getHeading1());
	__title2.setText(__dataset.getHeading2());

	if (__icallSimpleJComboBox.getSelected().startsWith("0 - ")) {
		__ccall.setEditable(false);
	}
	else {
		__ccall.setEditable(true);
	}
	__ccall.setText(__dataset.getCcall());
	StateMod_GUIUtil.checkAndSet(__dataset.getGwmaxrc(),
		__gwmaxrcJTextField);

	// Now add the components to the frame...

	////////////////////////////////////////////////////////
	// Run Dates
	__runDate1SimpleJComboBox = new SimpleJComboBox();
	__runDate2SimpleJComboBox = new SimpleJComboBox();	
	if (__dataDate1.getText().equals("NA") 
		|| __dataDate2.getText().equals("NA")) {
		__runDate1SimpleJComboBox.add("NA");
		__runDate2SimpleJComboBox.add("NA");
	}
	else {
		for (	int i = earliestPOR_DateTime.getYear();
			i <= latestPOR_DateTime.getYear(); i++) {
			__runDate1SimpleJComboBox.add("" + i);
			__runDate2SimpleJComboBox.add("" + i);
		}
		__runDate1SimpleJComboBox.select("" + __dataset.getIystr());
		__runDate2SimpleJComboBox.select("" + __dataset.getIyend());
	}

	////////////////////////////////////////////////////////
	// general panel
	JGUIUtil.addComponent(generalPanel, new JLabel("Title (1): "),
		0, 0, 1, 1, 0, 0, 
		5, 5, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(generalPanel, __title1,
		1, 0, 9, 1, 0, 0, 
		5, 0, 0, 5,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(generalPanel, new JLabel("Title (2): "),
		0, 1, 1, 1, 0, 0, 
		5, 5, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(generalPanel, __title2, 
		1, 1, 9, 1, 0, 0, 
		5, 0, 0, 5,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(generalPanel,new JLabel("Data start:"),
		0, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(generalPanel, __dataDate1,
		1, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(generalPanel, new JLabel("Data end:"),
		2, 2, 1, 1, 0, 0, 
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(generalPanel, __dataDate2,
		3, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(generalPanel, new JLabel("Run start:"),
		0, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(generalPanel, __runDate1SimpleJComboBox,
		1, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(generalPanel, new JLabel("Run end:"),
		2, 3, 1, 1, 0, 0, 
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(generalPanel, __runDate2SimpleJComboBox,
		3, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
		
	JGUIUtil.addComponent(generalPanel,
		new JLabel("Output units:"),
		0, 4, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(generalPanel, __iresopSimpleJComboBox,
		1, 4, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);	

	JGUIUtil.addComponent(generalPanel, 
		new JLabel("Year type:"),
		0, 5, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(generalPanel, __cyr1SimpleJComboBox,
		1, 5, 8, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(generalPanel,
		new JLabel("Data start and end are determined from "
			+ "historical input time series."),
		0, 6, 20, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	////////////////////////////////////////////////////////
	// options panel
	JGUIUtil.addComponent(optionsPanel, 
		new JLabel("Evaporation/precipitation data:"),
		0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(optionsPanel, __monevaSimpleJComboBox,
		1, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(optionsPanel, 
		new JLabel("Streamflow data:"),
		0, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(optionsPanel, __iopfloSimpleJComboBox,
		1, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(optionsPanel, 
		new JLabel("Number of delay table entries:"),
		0, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	JGUIUtil.addComponent(optionsPanel, __intervSimpleJComboBox,
		1, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	JGUIUtil.addComponent(optionsPanel, __interv,
		2, 2, 1, 1, 0, 0, 
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(optionsPanel, 
		new JLabel("Demand data type:"),
		0, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(optionsPanel, __icondemSimpleJComboBox,
		1, 3, 2, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(optionsPanel, 
		new JLabel("Instream flow approach:"),
		0, 4, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(optionsPanel, __ireachSimpleJComboBox,
		1, 4, 8, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	////////////////////////////////////////////////////////
	// factors panel
	__factor = new JTextField(5);
	__factor.setEditable(false);
	StateMod_GUIUtil.checkAndSet(__dataset.getFactor(), __factor);
	__rfacto = new JTextField(5);
	StateMod_GUIUtil.checkAndSet(__dataset.getRfacto(), __rfacto);
	__dfacto = new JTextField(5);
	StateMod_GUIUtil.checkAndSet(__dataset.getDfacto(), __dfacto);
	__ffacto = new JTextField(5);
	StateMod_GUIUtil.checkAndSet(__dataset.getFfacto(), __ffacto);
	__cfacto = new JTextField(5);
	StateMod_GUIUtil.checkAndSet(__dataset.getCfacto(), __cfacto);
	__efacto = new JTextField(5);
	StateMod_GUIUtil.checkAndSet(__dataset.getEfacto(), __efacto);
	__pfacto = new JTextField(5);
	StateMod_GUIUtil.checkAndSet(__dataset.getPfacto(), __pfacto);
	JGUIUtil.addComponent(factorsPanel, 
		new JLabel("Factor to convert from CFS to AF/D:"),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __factor,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(factorsPanel,
		new JLabel("Divisor for streamflow data units:"),
		0, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __rfacto,
		1, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(factorsPanel,
		new JLabel("Divisor for diversion data units:"),
		0, 2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __dfacto,
		1, 2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(factorsPanel,
		new JLabel("Divisor for instream flow data units:"),
		0, 3, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __ffacto,
		1, 3, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(factorsPanel,
		new JLabel("Factor to convert reservoir content data to AF:"),
		0, 4, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __cfacto,
		1, 4, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(factorsPanel,
		new JLabel("Factor to convert evaporation data to feet:"),
		0, 5, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __efacto,
		1, 5, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(factorsPanel, 
		new JLabel("Factor to convert precipitation data to feet:"),
		0, 6, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(factorsPanel, __pfacto,
		1, 6, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);


	////////////////////////////////////////////////////////
	// checks panel
		
	JGUIUtil.addComponent(checksPanel, 
		new JLabel("Detailed output:"),
		0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(checksPanel, __ichkSimpleJComboBox,
		1, 0, 2, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(checksPanel, 
		new JLabel("Detailed call output:"),
		0, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
		
	JGUIUtil.addComponent(checksPanel, __icallSimpleJComboBox,
		1, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(checksPanel, 
		new JLabel("Call water right of interest:"),
		0, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
		
	JGUIUtil.addComponent(checksPanel, __ccall,
		1, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);


	////////////////////////////////////////////////////////
	// advanced panel

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Daily analysis:"),
		0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __idaySimpleJComboBox,
		1, 0, 8, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Well analysis:"),
		0, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __iwellSimpleJComboBox,
		1, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Max recharge (CFS):"),
		0, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __gwmaxrcJTextField,
		1, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("San juan:"),
		0, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __isjripSimpleJComboBox,
		1, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Variable efficiency:"),
		0, 4, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __itsfileSimpleJComboBox,
		1, 4, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Sprinkler irrigation:"),
		0, 5, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __isprinkSimpleJComboBox,
		1, 5, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Irrigation water requirement:"),
		0, 6, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __ieffmaxSimpleJComboBox,
		1, 6, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Soil moisture:"),
		0, 7, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __soildSimpleJComboBox,
		1, 7, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(advancedPanel, __soildJTextField,
		2, 7, 1, 1, 0, 0, 
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Reoperate reservoirs:"),
		0, 8, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __ireopxSimpleJComboBox,
		1, 8, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(advancedPanel, __ireopx,
		2, 8, 1, 1, 0, 0, 
		0, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(advancedPanel, 
		new JLabel("Significant figures:"),
		0, 9, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(advancedPanel, __isigSimpleJComboBox,
		1, 9, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JTabbedPane tabs = new JTabbedPane();
	tabs.add("General", generalPanel);
	tabs.add("Options", optionsPanel);
	tabs.add("Factors", factorsPanel);
	tabs.add("Checks", checksPanel);
	tabs.add("Advanced Data", advancedPanel);

	getContentPane().add(tabs, "Center");

	// add bottom buttons
	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(fl);
	if (__editable) {
		buttonPanel.add(__applyJButton);
		buttonPanel.add(__cancelJButton);
	}
	buttonPanel.add(__closeJButton);
//	buttonPanel.add(__helpJButton);
	__helpJButton.setEnabled(false);
	__applyJButton.addActionListener(this);
	__applyJButton.setToolTipText("<HTML>Apply changes.<br>File...Save is "
		+ "still required to update files.</HTML>");
	__cancelJButton.addActionListener(this);
	__cancelJButton.setToolTipText("Cancel changes, reverting to previous "
		+ "values.");
	__closeJButton.addActionListener(this);
	__closeJButton.setToolTipText("<HTML>Apply changes and close window."
		+ "<br>File...Save is still required to save files.</HTML>");
	__helpJButton.addActionListener(this);


	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	JGUIUtil.addComponent(bottomJPanel, buttonPanel,
		0, 0, 8, 1, 1, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 1, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__statusJTextField = new JTextField(5);
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __statusJTextField,
		7, 1, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottomJPanel);	

	if (message != null) {
		Message.printWarning(1, routine, message);
	}

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_CONTROL, this );
	}

	if (!__editable) {
		disableComponents();
	}
	
	pack();
	JGUIUtil.center(this);
	setResizable(false);
	setVisible(true);
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowClosed(WindowEvent event) {}

/**
Responds to the window closing, calls closeWindow().
@param event the WindowEvent that happened.
*/
public void windowClosing(WindowEvent event) {
	closeWindow();
}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpening(WindowEvent event) {}

}
