// StateMod_Diversion_JFrame - dialog to edit the diversion information.

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
// StateMod_Diversion_JFrame - dialog to edit the diversion information.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 20 Aug 1997	Catherine E. 		Created initial version of class.
//		Nutting-Lane, RTi
// 14 Feb 1998	CEN, RTi		Bringing up a time series ...
// 11 May 1998	CEN, RTi		Incremented value sent to
//					setIdvcom by one.
// 31 Aug 1998  CEN, RTi		added check for TS
// 19 Sep 1998  CEN, RTi		Changed list to multilist
// 25 Oct 1999	CEN, RTi		Added daily id
// 08 Mar 2000	CEN, RTi		Added radio buttons to search
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
// 04 May 2001	SAM, RTi		Enable TSView and associated properties.
// 15 Aug 2001	SAM, RTi		Add disabled TSP, IWR, PAR buttons.
// 2002-09-19	SAM, RTi		Use isDirty() instead of setDirty() to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-06	J. Thomas Sapienza, RTi	Began initial swing version.
// 2003-06-16	JTS, RTi		Began functional swing version.
// 2003-06-17	JTS, RTi		* Wrangled with getting the layout to
//					  work.
//					* Added apply button.
// 2003-06-19	JTS, RTi		Changed search code to wrap around.
// 2003-06-20	JTS, RTi		Constructor now takes a data set as
//					a parameter, instead of a data set
//					component.
// 2003-06-23	JTS, RTi		Added code to graph monthly time series.
// 2003-07-15	JTS, RTi		* Added checkInput() framework for 
//					  validating user input prior to the 
//					  values being saved.
// 					* Added status bar.
//					* Changed to use new dataset design.
// 2003-07-16	JTS, RTi		Added constructor to select an initial
//					diversion from the index.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		* indexOf() is now in StateMod_Util.
//					* Force title as a parameter in the
//					  constructor.
// 2003-08-16	SAM, RTi		Change the window type to
//					WINDOW_DIVERSION.
// 2003-08-26	SAM, RTi		Enable StateMod_DataSet_WindowManager.
// 2003-08-27	JTS, RTi		Added selectID() to select an ID 
//					on the worksheet from outside the GUI.
// 2003-08-28	SAM, RTi		Updated based on changes in
//					StateMod_Diversion.
// 2003-09-03	JTS, RTi		* Removed old traces of button-based
//					  time series selected.
//					* Added new time series JCheckboxes.
//					* Cleaned up actionPerformed.
//					* Added AWC field.
// 2003-09-04	SAM, RTi		Make sure all time series are
//					represented and reorder to put
//					historical time series on top.
// 		JTS, RTi		* Changed field labels.
//					* Changed the daily data id field into
//					  a combo box.
//					* Removed the logic for the weight 
//					  factor.
// 2003-09-05	JTS, RTi		Class is now an item listener in 
//					order to enable/disable graph buttons
//					based on selected checkboxes.
// 2003-09-05	JTS, RTi		Disable graph/table/summary at
//					creation until item event fires.
// 2003-09-05	SAM, RTi		* Layouts were not working correctly.
//					  Rework so that the top parameters are
//					  in their own panel to be isolated from
//					  other panels and put the related data
//					  next to the time series.
//					* Rename some data to be more consistent
//					  with other code, in particular the
//					  time series checkboxes.
//					* Change itemStateChanged() handling for
//					  the time series to just check the
//					  checkboxes.  The previous code was not
//					  working properly.
//					* Set __currentDiversionIndex in
//					  selectTableIndex() - it was not
//					  getting initialized and was causing
//					  problems in itemStateChanged().
// 2003-09-08	JTS, RTI		* Changed the logic for ignoring item
//					  state change events and added an
//					  initialized boolean to not worry
//					  about any itemStateChanges while the 
//					  GUI is setting up.
//					* added checkTimeSeriesButtonsStates()
//					  so the graphing buttons can be 
//					  set properly when items are selected.
//					* for the monthly/constant efficiency 
//					  radio buttons, itemStateChange now
//					  only performs an action on a
//					  selection event.
// 2003-09-09	JTS, RTi		* __currentDiversionIndex no longer set
//					  in selectTableIndex() because it was
//					  causing problems with the first
//					  diversion in the table.  The problem
//					  this was introduced to fix has been
//					  removed, so it wasn't necessary
//					  anymore, anyways.
// 2003-09-18	SAM, RTi		Add estimated daily time series
//					checkboxes for historical data and
//					IWR.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-01	SAM, RTi		* General review before release for
//					  State review.
//					* In general move code around so GUI
//					  components are created and then added
//					  in close proximity - it is too
//					  confusing when things are spread
//					  around.
//					* When processing GUI components, list
//					  from left to right and top to bottom.
//					* Rename data members to use more recent
//					  standard to include Swing class name.
//					* Enable checkInput().
//					* Change efficiencies to be shown using
//					  the calendar for the data set.
//					* Change layout so related data are next
//					  to efficiencies and spread time series
//					  over 3 columns to make better use of
//					  horizontal space.
//					* Button groups can just be local data.
//					* River node ID is always uneditable.
//					* Fix so that a failed checkInput() does
//					  not allow the next selection to be
//					  displayed.
// 2003-10-08	SAM, RTi		Changes after SPDSS progress meeting
//					demonstration.
//					* Move daily ID and AWC to bottom and
//					  disable based on the control data.
//					* Move demsrc to the bottom and disable
//					  because GUI users should not need to
//					  set.
//					* Implement tool tip help.
//					* Move all code that modifies the
//					  diversion into the saveInformation()
//					  method - previously some data for
//					  the diversion was getting set before
//					  apply was executed.
//					* Show the constant efficiency separate
//					  from the monthly efficiencies - less
//					  confusing.
//					* Change set*Eff() to displayEff() and
//					  enable/disable text fields
//					  accordingly.
//					* Change so that when setting data from
//					  an object the value being used must
//					  be recognized.  If not, a default will
//					  be chosen and the diversion will be
//					  marked as dirty.  This is easier than
//					  dealing with unknown data that may
//					  actually be errors.  The only
//					  exception is the daily ID.
//					* Handle all time series, now that flags
//					  are understood.
// 2003-10-14	SAM, RTi		* Change Irrigation Water Requirement
//					  (IWR) to Consumptive Water Requirement
//					  (CWR) as per Ray Bennett's comments.
//					* After an apply, refresh the main GUI
//					  state via the window manager
//					  checkGUIState().
//					* Change saveInformation() to saveData()
//					  to be consistent with other classes.
// 2003-10-21	SAM, RTi		Change demand override average monthly
//					to demand average monthly.
// 2003-10-28	SAM, RTi		* Use the setData() method to populate
//					  choices, to increase performance.
//					* Add the water right as an option for
//					  graphing.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-06-06	SAM, RTi		* Move the definitions of switches to
//					  the StateMod_Diversion class and
//					  handle generically.
// 2005-01-13	JTS, RTi		Calls to removeColumn() in the worksheet
//					were removed in favor of using the new
//					constructor for the table model.
// 2006-01-19	JTS, RTi		* Now implements JWorksheet_SortListener
//					* Corrected bug that was corrupting data
//					  when rights or return flow sub-forms
//					  were entered.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import cdss.domain.hydrology.network.HydrologyNode;

import RTi.GIS.GeoView.GeoRecord;
import RTi.GR.GRLimits;
import RTi.GR.GRShape;
import RTi.GRTS.TSProduct;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.DayTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_SortListener;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.YearType;

// The layout for the GUI is as follows, for the most part using grid bag
// layout.  Only the button_JPanel and bottom_JPanel use FlowLayout.  In this
// layout, only the worksheet is truly resizable.  The left panel is 1 column
// and the right is 3 columns.
//
//	--------------------------main_JPanel-----------------------------------
//	||--left_JPanel------||-------------right_JPanel----------------------||
//	||                   ||                                               ||
//	||                   ||          param_JPanel                         ||
//	||   worksheet       ||                                               ||
//	||                   || --------------------------------------------- ||
//	||                   ||                                               ||
//	|| ----------------- ||         eff_JPanel       | relatedData_JPanel ||
//	||                   ||                                               ||
//	||   search_JPanel   || --------------------------------------------- ||
//	||                   ||        ts_JPanel                              ||
//	||                   || --------------------------------------------- ||
//	||                   ||        button_JPanel                          ||
//	||-------------------||-----------------------------------------------||
//	------------------------------------------------------------------------
//	|                         bottom_JPanel                                |
//	------------------------------------------------------------------------

/**
This class is a gui that displays a list of all the Diversions and the 
data for each diversion, once it is selected.
*/
@SuppressWarnings("serial")
public class StateMod_Diversion_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener, JWorksheet_SortListener {

/**
Whether the form is editable
*/
private boolean __editable = false;

/**
String labels for buttons.
*/
private static final String
	__BUTTON_SHOW_ON_MAP = "Show on Map",	
	__BUTTON_SHOW_ON_NETWORK = "Show on Network",
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close";

/**
Whether itemStateChanged() should ignore next state change that occurs.
This is to prevent endless loops caused by the setMonthlyEff and setConstantEff methods.
*/
private boolean __ignoreNextStateChange = false;

/**
Whether the entire GUI has been initialized yet.  Used to ignore completely all
item state change events prior to the GUI being ready.
*/
private boolean __initialized = false;

/**
The index in __all_JComponents[] of textfields that should NEVER be made
editable (e.g., ID fields).
*/
private int[] __disabled_JComponents;

/**
The index of the currently-selected diversion.
*/
private int __currentDiversionIndex = -1;
/**
The index of the last-selected diversion.
*/
private int __lastDiversionIndex = -1;

/**
Stores the index of the record that was selected before the worksheet is sorted,
in order to reselect it after the sort is complete.
*/
private int __sortSelectedRow = -1;

/**
GUI JButtons.
*/
private JButton 
	__findNextDiv_JButton = null,
	__waterRights_JButton = null,
	__returnFlow_JButton = null,
	__graph_JButton = null,
	__table_JButton = null,
	__summary_JButton = null,
	__help_JButton = null,
	__close_JButton = null,
	__cancel_JButton = null,
	__apply_JButton = null,
	__showOnMap_JButton = null,
	__showOnNetwork_JButton = null;

/**
Checkboxes for selecting the kind of time series to display.
*/
private JCheckBox 
	__ts_diversion_monthly_JCheckBox,
	__ts_diversion_daily_JCheckBox,
	__ts_diversion_est_daily_JCheckBox,
	__ts_water_right_monthly_JCheckBox,
	__ts_water_right_daily_JCheckBox,
	__ts_demand_monthly_JCheckBox,
	__ts_demand_override_monthly_JCheckBox,
	__ts_demand_ave_monthly_JCheckBox,
	__ts_demand_daily_JCheckBox,
	__ts_demand_est_daily_JCheckBox,
	__ts_ipy_yearly_JCheckBox,
	__ts_cwr_monthly_JCheckBox,
	__ts_cwr_daily_JCheckBox,
	__ts_cwr_est_daily_JCheckBox;

/**
Array of JComponents that should be disabled when nothing is selected from the list.
*/
private JComponent[] __all_JComponents;

/**
GUI JRadioButtons.
*/
private JRadioButton 
	__searchID_JRadioButton = null,
	__searchName_JRadioButton = null,
	__effConstant_JRadioButton = null,
	__effMonthly_JRadioButton = null;

/**
GUI JTextFields.
*/
private JTextField 
	__searchID_JTextField = null,
	__searchName_JTextField = null,
	__diversionStationID_JTextField = null,
	__diversionName_JTextField = null,
	__riverNodeID_JTextField = null,
	__diversionCapacity_JTextField = null,
	__awc_JTextField = null,
	__userName_JTextField = null,
	__irrigatedAcreage_JTextField = null;

/**
Status bar textfields 
*/
private JTextField 
	__message_JTextField,
	__status_JTextField;

/**
Array of textfields for the eff.
*/
private JTextField __effConstant_JTextField;
private JTextField[] __effMonthly_JTextField;

/**
The worksheet displaying the data.
*/
private JWorksheet __worksheet;

/**
GUI SimpleJComboBoxes.
*/
private SimpleJComboBox	
	__diversionSwitch_JComboBox = null,
	__diversionDailyID_JComboBox = null,
	__demandType_JComboBox = null,
	__demandSource_JComboBox = null,
	__replacementReservoirOption_JComboBox = null,
	__useType_JComboBox = null;

/**
The StateMod_DataSet that contains the statemod data.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The DataSetComponent that contains the diversions data.
*/
private DataSetComponent __diversionsComponent;

/**
The list of diversions to view/edit.
*/
private List<StateMod_Diversion> __diversionsVector;

/**
Constructor.
@param dataset StateMod_DataSet containing diversion data.
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param editable whether the data values on the form can be edited or not.
*/
public StateMod_Diversion_JFrame ( StateMod_DataSet dataset,
	StateMod_DataSet_WindowManager dataset_wm, boolean editable )
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Diversions", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__diversionsComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_STATIONS);

	__diversionsVector = (List<StateMod_Diversion>)__diversionsComponent.getData();	
	int size = __diversionsVector.size();
	StateMod_Diversion div = null;
	for (int i = 0; i < size; i++) {
		div = __diversionsVector.get(i);
		div.createBackup();
	}

	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset StateMod data set being displayed.
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param diversion the diversion to select and show in the list of diversion
@param editable whether the data values on the form can be edited or not.
*/
public StateMod_Diversion_JFrame ( StateMod_DataSet dataset, StateMod_DataSet_WindowManager dataset_wm,
	StateMod_Diversion diversion, boolean editable)
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Diversions", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__diversionsComponent = __dataset.getComponentForComponentType(StateMod_DataSet.COMP_DIVERSION_STATIONS);

	__diversionsVector = (List<StateMod_Diversion>)__diversionsComponent.getData();	
	int size = __diversionsVector.size();
	StateMod_Diversion div = null;
	for (int i = 0; i < size; i++) {
		div = __diversionsVector.get(i);
		div.createBackup();
	}

	String id = diversion.getID();
	int index = StateMod_Util.indexOf(__diversionsVector, id);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to actionPerformed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_Diversion_JFrame.actionPerformed"; 

	String action = e.getActionCommand();
	Object source = e.getSource();

	try {

	if ( source == __help_JButton ) {
		// REVISIT HELP (JTS 2003-06-09)
	}
	else if ( source == __effConstant_JRadioButton ) {
		// Display efficiencies as constant, do not reset data...
		displayEff ( false, false );
	}
	else if ( source == __effMonthly_JRadioButton ) {
		// Display efficiencies as monthly, do not reset data...
		displayEff ( true, false );
	}	
	else if (action.equals("Graph") || action.equals("Table") || action.equals("Summary")) {
		displayTSViewJFrame(action);
	}
	else if ( source == __close_JButton ) {
		saveCurrentDiversion();
		boolean changed = false;
		for ( StateMod_Diversion div : __diversionsVector ) {
			if (!changed && div.changed()) {
				changed = true;
			}
			div.acceptChanges();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_DIVERSION_STATIONS,true);
		}
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_DIVERSION );
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if ( source == __apply_JButton ) {
		saveCurrentDiversion();
		boolean changed = false;
		for (StateMod_Diversion div : __diversionsVector) {
			if (!changed && div.changed()) {
				changed = true;
			}
			div.createBackup();
		}
		if (changed) {			
			__dataset.setDirty(StateMod_DataSet.COMP_DIVERSION_STATIONS,true);
		}		
	}
	else if ( source == __cancel_JButton ) {
		for (StateMod_Diversion div : __diversionsVector) {
			div.restoreOriginal();
		}

		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_DIVERSION );
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if (source == __findNextDiv_JButton) {
		searchWorksheet(__worksheet.getSelectedRow() + 1);
	}
	else if (source == __searchID_JTextField || source == __searchID_JTextField) {	
		searchWorksheet();
	}	
	else if (e.getSource() == __searchID_JRadioButton) {
		__searchName_JTextField.setEditable(false);		
		__searchID_JTextField.setEditable(true);
	}
	else if (source == __searchName_JRadioButton) {
		__searchID_JTextField.setEditable(false);
		__searchName_JTextField.setEditable(true);
	}
	else if ( source == __showOnMap_JButton ) {
		GeoRecord geoRecord = getSelectedDiversion().getGeoRecord();
		GRShape shape = geoRecord.getShape();
		__dataset_wm.showOnMap ( getSelectedDiversion(),
			"Div: " + getSelectedDiversion().getID() + " - " + getSelectedDiversion().getName(),
			new GRLimits(shape.xmin, shape.ymin, shape.xmax, shape.ymax),
			geoRecord.getLayer().getProjection() );
	}
	else if ( source == __showOnNetwork_JButton ) {
		StateMod_Network_JFrame networkEditor = __dataset_wm.getNetworkEditor();
		if ( networkEditor != null ) {
			HydrologyNode node = networkEditor.getNetworkJComponent().findNode(
				getSelectedDiversion().getID(), false, false);
			if ( node != null ) {
				__dataset_wm.showOnNetwork ( getSelectedDiversion(),
					"Div: " + getSelectedDiversion().getID() + " - " + getSelectedDiversion().getName(),
					new GRLimits(node.getX(),node.getY(),node.getX(),node.getY()) );
			}
		}
	}
	else if (source == __waterRights_JButton) {
		if (__currentDiversionIndex == -1) {
			new ResponseJDialog(this, "You must first select a diversion from the list.",
			ResponseJDialog.OK);
			return;
		}

		// set placeholder to current diversion
		StateMod_Diversion div = __diversionsVector.get(__currentDiversionIndex);
	
		new StateMod_Diversion_Right_JFrame(__dataset, div, __editable);
	}
	else if (source == __returnFlow_JButton) {
		if (__currentDiversionIndex == -1) {
			new ResponseJDialog(this, "You must first select a diversion from the list.",
			ResponseJDialog.OK);
			return;
		}

		// set placeholder to current diversion
		StateMod_Diversion div = __diversionsVector.get(__currentDiversionIndex);
	
		new StateMod_Diversion_ReturnFlow_JFrame(__dataset, div, __editable);
	}
	}
	catch (Exception ex) {
		Message.printWarning(2, routine, "Error processing action");
		Message.printWarning(2, routine, ex);
	}
}

/**
Checks the text fields for validity before they are saved back into the data object.
@return 0 if the text fields are okay, 1 if fatal errors exist, and -1 if only non-fatal errors exist.
*/
private int checkInput()
{	String routine = "StateMod_Diversion_JFrame.checkInput";
	String name = __diversionName_JTextField.getText().trim();
	String rivernode = __riverNodeID_JTextField.getText().trim();
	String capacity = __diversionCapacity_JTextField.getText().trim();
	String acres = __irrigatedAcreage_JTextField.getText().trim();
	String awc = __awc_JTextField.getText().trim();
	String eff_constant = __effConstant_JTextField.getText().trim();
	String [] eff_monthly = new String[12];
	for ( int i = 0; i < 12; i++ ) {
		eff_monthly[i] = __effMonthly_JTextField[i].getText().trim();
	}
	String warning = "";
	int fatal_count = 0;
	int nonfatal_count = 0;

	if ( name.length() > 24 ) {
		warning += "\nDiversion name is > 24 characters.";
		++fatal_count;
	}
	if ( !StringUtil.isDouble(capacity) ) {
		warning += "\nCapacity (" + capacity + ") is not a number.";
		++fatal_count;
	}
	if ( !acres.equals("") && !StringUtil.isDouble(acres) ) {
		warning += "\nIrrigated acreage (" + acres + ") is not a number.";
		++fatal_count;
	}
	if ( !awc.equals("") && !StringUtil.isDouble(awc) ) {
		warning += "\nAWC (" + awc + ") is not a number.";
		++fatal_count;
	}
	// Non-fatal errors (need to be corrected somehow)...
	if ( __dataset != null ) {
		DataSetComponent comp = __dataset.getComponentForComponentType (StateMod_DataSet.COMP_RIVER_NETWORK );
		@SuppressWarnings("unchecked")
		List<StateMod_RiverNetworkNode> data = (List<StateMod_RiverNetworkNode>)comp.getData();
		if ( !rivernode.equals("") && (StateMod_Util.indexOf(data,rivernode) < 0) ) {
			warning += "\nRiver node ID (" + rivernode + ") is not in the network.";
			++nonfatal_count;
		}
	}
	if ( __effConstant_JRadioButton.isSelected() && !StringUtil.isDouble(eff_constant) ) {
		warning += "\nConstant efficiency (" + eff_constant + ") is not a number.";
		++fatal_count;
	}
	else if ( __effMonthly_JRadioButton.isSelected() ) {
		for ( int i = 0; i < 12; i++ ) {
			if ( !StringUtil.isDouble(eff_monthly[i]) ) {
				warning += "\nMonthly efficiency (" + eff_monthly[i] + ") is not a number.";
				++fatal_count;
			}
		}
	}
	// TODO - if daily time series are supplied, check for time series and allow creation if not available.
	if ( warning.length() > 0 ) {
		StateMod_Diversion div = __diversionsVector.get(__currentDiversionIndex);
		warning = "\nDiversion:  " +
		StateMod_Util.formatDataLabel ( div.getID(), div.getName() ) + warning + "\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, warning, this );
		if ( fatal_count > 0 ) {
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
		Message.printStatus ( 1, routine, "Returning 0 from checkInput()" );
		return 0;
	}
}

/**
Checks the states of the time series check boxes and appropriately enables or
disables the graphing/table/summary buttons.
*/
private void checkTimeSeriesButtonsStates() {
	if ( (__ts_diversion_monthly_JCheckBox.isEnabled() && __ts_diversion_monthly_JCheckBox.isSelected()) ||
		(__ts_diversion_daily_JCheckBox.isEnabled() && __ts_diversion_daily_JCheckBox.isSelected()) ||
		(__ts_diversion_est_daily_JCheckBox.isEnabled() && __ts_diversion_est_daily_JCheckBox.isSelected()) ||
		(__ts_water_right_monthly_JCheckBox.isEnabled() && __ts_water_right_monthly_JCheckBox.isSelected()) ||
		(__ts_water_right_daily_JCheckBox.isEnabled() && __ts_water_right_daily_JCheckBox.isSelected()) ||
		(__ts_demand_monthly_JCheckBox.isEnabled() && __ts_demand_monthly_JCheckBox.isSelected()) ||
		(__ts_demand_override_monthly_JCheckBox.isEnabled() && __ts_demand_override_monthly_JCheckBox.isSelected()) ||
		(__ts_demand_ave_monthly_JCheckBox.isEnabled() && __ts_demand_ave_monthly_JCheckBox.isSelected()) ||
		(__ts_demand_daily_JCheckBox.isEnabled() && __ts_demand_daily_JCheckBox.isSelected() ) ||
		(__ts_demand_est_daily_JCheckBox.isEnabled() && __ts_demand_est_daily_JCheckBox.isSelected()) ||
		(__ts_ipy_yearly_JCheckBox.isEnabled() && __ts_ipy_yearly_JCheckBox.isSelected()) ||
		(__ts_cwr_monthly_JCheckBox.isEnabled() && __ts_cwr_monthly_JCheckBox.isSelected()) ||
		(__ts_cwr_daily_JCheckBox.isEnabled() && __ts_cwr_daily_JCheckBox.isSelected()) ||
		(__ts_cwr_est_daily_JCheckBox.isEnabled() && __ts_cwr_est_daily_JCheckBox.isSelected()) ) {
		// One or more time series is selected and enabled so enable the view buttons...
		__graph_JButton.setEnabled(true);
		__table_JButton.setEnabled(true);
		__summary_JButton.setEnabled(true);	
	}
	else {
		// No time series are enabled and selected so disable the time series view buttons...
		__graph_JButton.setEnabled(false);
		__table_JButton.setEnabled(false);
		__summary_JButton.setEnabled(false);	
	}
}

/**
Checks the states of the map and network view buttons based on the selected diversion.
*/
private void checkViewButtonState()
{
	StateMod_Diversion div = getSelectedDiversion();
	if ( div.getGeoRecord() == null ) {
		// No spatial data are available
		__showOnMap_JButton.setEnabled ( false );
	}
	else {
		// Enable the button...
		__showOnMap_JButton.setEnabled ( true );
	}
}

/**
Display the efficiency values from the data object to the GUI.
@param monthly_data If true, then the monthly efficiencies are enabled and the
constant efficiency is disabled.  If false, the monthly efficiencies are
disabled and the constant efficiency is enabled.
@param reset_data If true, the display values are reset from the data object -
suitable for when a new diversion is picked.  If false, the old values remain,
suitable for when the use is just changing radio buttons.
*/
private void displayEff ( boolean monthly_data, boolean reset_data )
{	if (__currentDiversionIndex == -1) {
		return;
	}
	
	StateMod_Diversion div = __diversionsVector.get(__currentDiversionIndex);
	// Set this because setting 
	__ignoreNextStateChange = true;
	// This is only really needed when needed when the data are initially being populated.
	if ( monthly_data ) {
		JGUIUtil.setEnabled ( __effConstant_JTextField, false );
		__effMonthly_JRadioButton.setSelected(true);
	}
	else {
		JGUIUtil.setEnabled ( __effConstant_JTextField, true );
		__effConstant_JRadioButton.setSelected(true);
	}
	if ( reset_data ) {
		double efc = div.getDivefc();
		if ( efc < 0 ) {
			efc = -1 * efc;
		}
		StateMod_GUIUtil.checkAndSet ( efc, __effConstant_JTextField );
	}

	for (int i = 0; i < 12; i++) {
		if ( reset_data ) {
			StateMod_GUIUtil.checkAndSet ( div.getDiveff(i),__effMonthly_JTextField[i]);
		}
		if ( monthly_data ) {
			// Enable all the monthly efficiency text fields...
			JGUIUtil.setEnabled (__effMonthly_JTextField[i], true );
		}
		else {
			// Disable all the monthly efficiency text fields...
			JGUIUtil.setEnabled (__effMonthly_JTextField[i], false);
		}
	}
}

/**
Display a graph, table, or summary, based on the button that was pressed
and the time series checkboxes that are selected.
@param type the type of view to display.
*/
public void displayTSViewJFrame(String type)
{	String routine = "StateMod_Diversion_JFrame.displayTSViewJFRame";
	__currentDiversionIndex = __worksheet.getSelectedRow();
	if (__currentDiversionIndex == -1) {
		new ResponseJDialog(this, "You must first select a diversion from the list.",
		ResponseJDialog.OK);
		return;
	}
	StateMod_Diversion div = __diversionsVector.get(__currentDiversionIndex);

	PropList display_props = new PropList("TSView");

	display_props.set("InitialView", type);
	display_props.set("TSViewTitleString", StateMod_Util.createDataLabel(div,true) + " Time Series");
	display_props.set("DisplayFont", "Courier");
	display_props.set("DisplaySize", "11");
	display_props.set("PrintFont", "Courier");
	display_props.set("PrintSize", "7");
	display_props.set("PageLength", "100");

	PropList props = new PropList("Diversion");
	props.set("Product.TotalWidth", "600");
	props.set("Product.TotalHeight", "400");

	List<TS> tslist = new Vector<TS>();

	int sub = 0;
	int its = 0;
	TS ts = null;

	// For all the tests, can initially see whether the checkboxes are
	// enabled AND selected - don't need to check for time series because
	// this would have been determined when the checkboxes were enabled in processTableSelection().

	// Irrigated acres...

	if ( __ts_ipy_yearly_JCheckBox.isSelected() && __ts_ipy_yearly_JCheckBox.isEnabled() ) {
		// Do the yearly graph for crops...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Irrigated Acres for Diversion "
			+ div.getID() + " (" + div.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
        /* FIXME SAM 2007-11-12 Need to use irrigation parts, not total.
		ts = div.getIrrigationPracticeYearTS().getSacreTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
		ts = div.getIrrigationPracticeYearTS().getGacreTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
        */
		ts = div.getIrrigationPracticeYearTS().getTacreTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	// Efficiencies...

	if ( __ts_ipy_yearly_JCheckBox.isSelected() && __ts_ipy_yearly_JCheckBox.isEnabled() ) {
		// Do the yearly graph for efficiencies...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Efficiencies for Diversion "
			+ div.getID() + " (" + div.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = div.getIrrigationPracticeYearTS().getCeffTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
		ts = div.getIrrigationPracticeYearTS().getFeffTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
		ts = div.getIrrigationPracticeYearTS().getSeffTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	// Monthly data (ACFT)...

	if ( (__ts_diversion_monthly_JCheckBox.isSelected() && __ts_diversion_monthly_JCheckBox.isEnabled() ) ||
		(__ts_diversion_monthly_JCheckBox.isSelected() && __ts_diversion_monthly_JCheckBox.isEnabled() ) ||
		(__ts_demand_monthly_JCheckBox.isSelected() && __ts_demand_monthly_JCheckBox.isEnabled() ) ||
		(__ts_demand_override_monthly_JCheckBox.isSelected() && __ts_demand_override_monthly_JCheckBox.isEnabled() ) ||
		(__ts_cwr_monthly_JCheckBox.isSelected() && __ts_cwr_monthly_JCheckBox.isEnabled() ) ||
		(__ts_water_right_monthly_JCheckBox.isSelected() && __ts_water_right_monthly_JCheckBox.isEnabled() ) ) {
		// Do the monthly graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub +
			".SubTitleString=Monthly Data for Diversion " + div.getID() + " (" + div.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		if ( __ts_diversion_monthly_JCheckBox.isSelected() && __ts_diversion_monthly_JCheckBox.isEnabled() ) {
			ts = div.getDiversionMonthTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_demand_monthly_JCheckBox.isSelected() && __ts_demand_monthly_JCheckBox.isEnabled() ) {
			ts = div.getDemandMonthTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_demand_override_monthly_JCheckBox.isSelected() &&
			__ts_demand_override_monthly_JCheckBox.isEnabled() ) {
			ts = div.getDemandOverrideMonthTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_cwr_monthly_JCheckBox.isSelected() && __ts_cwr_monthly_JCheckBox.isEnabled() ) {
			ts = div.getConsumptiveWaterRequirementMonthTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_water_right_monthly_JCheckBox.isSelected() &&
			__ts_water_right_monthly_JCheckBox.isEnabled() ) {
			if ( div.getRights().size() > 0 ) {
				// Create a monthly time series to illustrate the water right volume...
				ts = StateMod_Util.createWaterRightTS ( div, TimeInterval.MONTH,
					StateMod_DataSet.lookupTimeSeriesDataUnits(
					StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY),
					__dataset.getDataStart(), __dataset.getDataEnd() );
				if ( ts != null ) {
					props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
					tslist.add ( ts );
				}
			}
		}
	}

	// Monthly average (ACFT)...

	if ( __ts_demand_ave_monthly_JCheckBox.isSelected() && __ts_demand_ave_monthly_JCheckBox.isEnabled() ) {
		// Do the monthly average graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Monthly Average Data for Diversion "
			+ div.getID() + " (" + div.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = div.getDemandAverageMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	// Daily (CFS)...

	String cdividy = StringUtil.getToken ( __diversionDailyID_JComboBox.getSelected(), " -",0,0 );
	DayTS dayts = null;
	int pos;
	DataSetComponent comp;
	List data;
	if ( (__ts_diversion_daily_JCheckBox.isSelected() && __ts_diversion_daily_JCheckBox.isEnabled()) ||
		(__ts_diversion_est_daily_JCheckBox.isSelected() && __ts_diversion_est_daily_JCheckBox.isEnabled()) ||
		(__ts_demand_daily_JCheckBox.isSelected() && __ts_demand_daily_JCheckBox.isEnabled() ) ||
		(__ts_demand_est_daily_JCheckBox.isSelected() && __ts_demand_est_daily_JCheckBox.isEnabled() ) ||
		(__ts_cwr_daily_JCheckBox.isSelected() && __ts_cwr_daily_JCheckBox.isEnabled() ) ||
		(__ts_cwr_est_daily_JCheckBox.isSelected() && __ts_cwr_est_daily_JCheckBox.isEnabled() ) ||
		(__ts_water_right_daily_JCheckBox.isSelected() && __ts_water_right_daily_JCheckBox.isEnabled() ) ) {
		// Do the daily graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Daily Data for Diversion "
			+ div.getID() + " (" + div.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		if ( __ts_diversion_daily_JCheckBox.isSelected() && __ts_diversion_daily_JCheckBox.isEnabled() ) {
			ts = div.getDiversionDayTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_diversion_est_daily_JCheckBox.isSelected() && __ts_diversion_est_daily_JCheckBox.isEnabled() ) {
			// Need to estimate the daily diversion...
			dayts = null;
			if ( cdividy.equalsIgnoreCase(div.getID()) ) {
				// The daily time series is from this diversion...
				dayts = div.getDiversionDayTS();
			}
			else if(!cdividy.equals("0") && !cdividy.equals("4") ) {
				// Get the daily time series from another diversion...
				comp = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_DIVERSION_TS_DAILY );
				data = (List)comp.getData();
				pos = TSUtil.indexOf ((List)comp.getData(), cdividy, "Location", 0 );
				if ( pos >= 0 ) {
					dayts = (DayTS)data.get(pos);
				}
			}
			ts = StateMod_Util.createDailyEstimateTS ( div.getID(), div.getName(), "Diversion", "CFS",
				cdividy, div.getDiversionMonthTS(), dayts );
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_demand_daily_JCheckBox.isSelected() && __ts_demand_daily_JCheckBox.isEnabled() ) {
			ts = div.getDemandDayTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_demand_est_daily_JCheckBox.isSelected() && __ts_demand_est_daily_JCheckBox.isEnabled() ) {
			// Need to estimate the daily demand...
			dayts = null;
			if ( cdividy.equalsIgnoreCase(div.getID()) ) {
				// The daily time series is from this diversion...
				dayts = div.getDemandDayTS();
			}
			else if(!cdividy.equals("0") && !cdividy.equals("4") ) {
				// Get the daily time series from another diversion...
				comp = __dataset.getComponentForComponentType(StateMod_DataSet.COMP_DEMAND_TS_DAILY );
				data = (List)comp.getData();
				pos = TSUtil.indexOf ((List)comp.getData(),	cdividy, "Location", 0 );
				if ( pos >= 0 ) {
					dayts = (DayTS)data.get(pos);
				}
			}
			ts = StateMod_Util.createDailyEstimateTS (
				div.getID(), div.getName(), "Demand", "CFS", cdividy, div.getDemandMonthTS(), dayts );
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_cwr_daily_JCheckBox.isSelected() && __ts_cwr_daily_JCheckBox.isEnabled() ) {
			ts = div.getConsumptiveWaterRequirementDayTS();
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_cwr_est_daily_JCheckBox.isSelected() && __ts_cwr_est_daily_JCheckBox.isEnabled() ) {
			// Need to estimate the daily cwr...
			dayts = null;
			if ( cdividy.equalsIgnoreCase(div.getID()) ) {
				// The daily time series is from this diversion...
				dayts=div.getConsumptiveWaterRequirementDayTS();
			}
			else if(!cdividy.equals("0") && !cdividy.equals("4") ) {
				// Get the daily time series from another diversion...
				comp = __dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY );
				data = (List)comp.getData();
				pos = TSUtil.indexOf ((List)comp.getData(), cdividy, "Location", 0 );
				if ( pos >= 0 ) {
					dayts = (DayTS)data.get(pos);
				}
			}
			ts = StateMod_Util.createDailyEstimateTS (
				div.getID(), div.getName(), "CWR", "CFS", cdividy, div.getDemandMonthTS(), dayts );
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add ( ts );
			}
		}
		if ( __ts_water_right_daily_JCheckBox.isSelected() && __ts_water_right_daily_JCheckBox.isEnabled() ) {
			if ( div.getRights().size() > 0 ) {
				// Create a daily time series to illustrate the water right volume...
				ts = StateMod_Util.createWaterRightTS ( div, TimeInterval.DAY,
					StateMod_DataSet.lookupTimeSeriesDataUnits( StateMod_DataSet.COMP_DIVERSION_TS_DAILY),
					__dataset.getDataStart(), __dataset.getDataEnd() );
				if ( ts != null ) {
					props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
					tslist.add ( ts );
				}
			}
		}
	}
	
	try {
		TSProduct tsproduct = new TSProduct ( props, display_props );
		tsproduct.setTSList ( tslist );
		new TSViewJFrame ( tsproduct );
	}
	catch (Exception e) {
		Message.printWarning(1,routine,"Error displaying time series (" + e + ").", this);
		Message.printWarning(2, routine, e);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__findNextDiv_JButton = null;
	__waterRights_JButton = null;
	__returnFlow_JButton = null;
	__help_JButton = null;
	__close_JButton = null;
	__apply_JButton = null;
	__cancel_JButton = null;
	__searchID_JRadioButton = null;
	__searchName_JRadioButton = null;
	__effConstant_JRadioButton = null;
	__effMonthly_JRadioButton = null;
	__searchID_JTextField = null;
	__searchName_JTextField = null;
	__diversionStationID_JTextField = null;
	__diversionDailyID_JComboBox = null;
	__diversionName_JTextField = null;
	__riverNodeID_JTextField = null;
	__diversionCapacity_JTextField = null;
	__userName_JTextField = null;
	__replacementReservoirOption_JComboBox = null;
	__demandSource_JComboBox = null;
	__useType_JComboBox = null;
	__irrigatedAcreage_JTextField = null;
	__effMonthly_JTextField = null;
	__worksheet = null;
	__diversionSwitch_JComboBox = null;
	__demandType_JComboBox = null;

	super.finalize();
}

/**
Get the selected diversion, based on the current index in the list.
*/
private StateMod_Diversion getSelectedDiversion ()
{
	return __diversionsVector.get(__currentDiversionIndex);
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to selectTableIndex().
*/
private void initializeJComponents()
{	__all_JComponents = new JComponent[47];
	int i = 0;
	// These may be disabled...
	__all_JComponents[i++] = __diversionStationID_JTextField;
	__all_JComponents[i++] = __riverNodeID_JTextField;
	__all_JComponents[i++] = __demandSource_JComboBox;
	__all_JComponents[i++] = __diversionDailyID_JComboBox;
	__all_JComponents[i++] = __awc_JTextField;
	// The rest are always enabled...
	__all_JComponents[i++] = __apply_JButton;
	__all_JComponents[i++] = __returnFlow_JButton;
	__all_JComponents[i++] = __waterRights_JButton;
	__all_JComponents[i++] = __effConstant_JRadioButton;
	__all_JComponents[i++] = __effMonthly_JRadioButton;
	__all_JComponents[i++] = __diversionName_JTextField;
	__all_JComponents[i++] = __diversionCapacity_JTextField;
	__all_JComponents[i++] = __userName_JTextField;
	__all_JComponents[i++] = __replacementReservoirOption_JComboBox;
	__all_JComponents[i++] = __useType_JComboBox;
	__all_JComponents[i++] = __irrigatedAcreage_JTextField;
	__all_JComponents[i++] = __graph_JButton;
	__all_JComponents[i++] = __table_JButton;
	__all_JComponents[i++] = __summary_JButton;
	__all_JComponents[i++] = __diversionSwitch_JComboBox;
	__all_JComponents[i++] = __demandType_JComboBox;
	__all_JComponents[i++] = __ts_diversion_monthly_JCheckBox;
	__all_JComponents[i++] = __ts_diversion_daily_JCheckBox;
	__all_JComponents[i++] = __ts_diversion_est_daily_JCheckBox;
	__all_JComponents[i++] = __ts_water_right_monthly_JCheckBox;
	__all_JComponents[i++] = __ts_water_right_daily_JCheckBox;
	__all_JComponents[i++] = __ts_demand_monthly_JCheckBox;		
	__all_JComponents[i++] = __ts_demand_override_monthly_JCheckBox;
	__all_JComponents[i++] = __ts_demand_ave_monthly_JCheckBox;	
	__all_JComponents[i++] = __ts_demand_daily_JCheckBox;
	__all_JComponents[i++] = __ts_demand_est_daily_JCheckBox;
	__all_JComponents[i++] = __ts_ipy_yearly_JCheckBox;
	__all_JComponents[i++] = __ts_cwr_monthly_JCheckBox;
	__all_JComponents[i++] = __ts_cwr_daily_JCheckBox;	
	__all_JComponents[i++] = __ts_cwr_est_daily_JCheckBox;	
	
	for (int j = 0; j < __effMonthly_JTextField.length; j++) {
		__all_JComponents[i + j] = __effMonthly_JTextField[j];
	}
	
	// Indicate components that are never enabled.
	// The ID and the node ID are disabled in all cases.
	// Other components are disabled because the control data indicate that some data are not used.
	// All time series are enabled because it may be useful to compare
	// time series, regardless of the control settings.

	__disabled_JComponents = new int[5];
	__disabled_JComponents[0] = 0;	//__diversionStationID_JTextField
	__disabled_JComponents[1] = 1;	// __riverNodeID_JTextField
	__disabled_JComponents[2] = 2;	// __demandSource_JComboBox Disabled because only used by DMIs - may phase out
	__disabled_JComponents[3] = -1;	// For daily data ID
	__disabled_JComponents[4] = -1;	// For soil switch below

	if ( __dataset != null ) {
		if ( __dataset.getIday() <= 0 ) {
			__disabled_JComponents[3] = 3;
		}
		if ( __dataset.getSoild() == 0.0 ) {
			__disabled_JComponents[4] = 4;
		}
	}
}

/**
Responds to item state changed events.
@param e the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent e)
{	if (e.getSource() instanceof JCheckBox) {
		checkTimeSeriesButtonsStates();
		return;
	}

	if (!__initialized) {
		return;
	}
	if (__ignoreNextStateChange) {
		__ignoreNextStateChange = false;
		return;
	}

	if (__currentDiversionIndex == -1) {
		return;
	}

	// set placeholder to current diversion
	// TODO SAM Evaluate logic
	//StateMod_Diversion div = __diversionsVector.elementAt(__currentDiversionIndex);
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {}

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
	processTableSelection(__worksheet.getSelectedRow(), true);
}

/**
Populates the diversion daily id combo box with the static choices and the
available diversion identifiers.  This information will be modified on the
fly as different diversions are selected.
*/
private void populateDiversionDailyID()
{	__diversionDailyID_JComboBox.removeAllItems();

	// Get a list of all the diversions with "ID - Name"...
	List<String> idNameVector = StateMod_Util.createIdentifierListFromStateModData(__diversionsVector, true, null);
	List<String> static_choices = StateMod_Diversion.getCdividyChoices ( true );
	// Take special care if no diversions are in the list...
	if ( idNameVector.size() == 0 ) {
		idNameVector.add( static_choices.get(0) );
	}
	else {
		idNameVector.add(0,static_choices.get(0) );
	}
	int size = static_choices.size();
	for ( int i = 1; i < size; i++ ) {
		idNameVector.add(i, static_choices.get(0) );
	}
	// Add at once to increase performance...
	__diversionDailyID_JComboBox.setData ( idNameVector );
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex()) by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it on the form.
@param index the index of the diversion to display on the form.
@param try_to_save Indicates whether the current data should try to be saved.
false should be specified if the call is being made after checkInput() fails.
*/
private void processTableSelection(int index, boolean try_to_save )
{	String routine = "processTableSelection";	

 	// First save the previous information before displaying the new information...

	Message.printStatus ( 1, "", "processTableSelection index " + index + " save flag: " + try_to_save );
/*
	if ( try_to_save && !saveCurrentDiversion() ) {
		// Save was unsuccessful.  Revert to the previous index.  This
		// will eventually end up being a recursive call back to here but try_to_save will be false.
		Message.printStatus ( 1, "", "processTableSelection index " +
			index + " save flag: " + try_to_save +
			" Error saving so display old index: " +
			__currentDiversionIndex );
		selectTableIndex(__currentDiversionIndex, false, false);
		return;
	}
*/
	// Now switch to show data for the selected diversion...

	__lastDiversionIndex = __currentDiversionIndex;
	__currentDiversionIndex = __worksheet.getOriginalRowNumber(index);
	
	if (__worksheet.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__all_JComponents, true );
		return;
	}

	JGUIUtil.enableComponents ( __all_JComponents, __disabled_JComponents, __editable);
	
	// List these in the order of the GUI...

	StateMod_Diversion div = __diversionsVector.get(__currentDiversionIndex);

	// Diversion identifier...

	__diversionStationID_JTextField.setText(div.getID());

	// Diversion name...

	__diversionName_JTextField.setText(div.getName());

	// River node...
	// TODO - if river node is made editable, change to a JComboBox...

	__riverNodeID_JTextField.setText(div.getCgoto());

	// Capacity...

	StateMod_GUIUtil.checkAndSet(div.getDivcap(), __diversionCapacity_JTextField );

	// On/off switch...

	String idivsw = "" + div.getSwitch();
	// Select the switch that matches the first token in the available choices...
	try {
		JGUIUtil.selectTokenMatches ( __diversionSwitch_JComboBox, true, " ", 0, 0, idivsw, null );
	}
	catch ( Exception e ) {
		// Default...
		Message.printWarning ( 2, routine, "Using default value idivsw = \"" + 
		StateMod_Diversion.getIdivswDefault(true) + "\" because data value " + idivsw + " is unknown." );
		__diversionSwitch_JComboBox.select(StateMod_Diversion.getIdivswDefault(true) );
	}

	// User name...

	__userName_JTextField.setText(div.getUsername());

	// Replacement reservoir...

	String ireptype = "" + div.getIreptype();
	try {
		JGUIUtil.selectTokenMatches ( __replacementReservoirOption_JComboBox, true, " ", 0, 0, ireptype, null );
	}
	catch ( Exception e ) {
		// Default...
		Message.printWarning ( 2, routine, "Using default value ireptype = \"" +
		StateMod_Diversion.getIreptypeDefault(true) + "\" because data value " + ireptype + " is unknown." );
		__replacementReservoirOption_JComboBox.select(StateMod_Diversion.getIreptypeDefault(true) );
	}

	// Use type...

	String irturn = "" + div.getIrturn();
	try {
		JGUIUtil.selectTokenMatches ( __useType_JComboBox, true, " ", 0, 0, irturn, null );
	}
	catch ( Exception e ) {
		// Default...
		Message.printWarning ( 2, routine, "Using default value irturn = \"" +
		StateMod_Diversion.getIrturnDefault(true) + "\" because data value " + irturn + " is unknown." );
		__useType_JComboBox.select( StateMod_Diversion.getIrturnDefault(true) );
	}

	// Irrigated acreage...

	StateMod_GUIUtil.checkAndSet(div.getArea(), __irrigatedAcreage_JTextField);

	// Monthly demand type switch..

	String idvcom = "" + div.getIdvcom();
	try {
		JGUIUtil.selectTokenMatches ( __demandType_JComboBox, true, " ", 0, 0, idvcom, null );
	}
	catch ( Exception e ) {
		// Default...
		Message.printWarning ( 2, routine, "Using default value idvcom = \"" +
		StateMod_Diversion.getIdvcomChoices(true) + "\" because data value " + idvcom + " is unknown." );
		__useType_JComboBox.select(	StateMod_Diversion.getIdvcomDefault(true) );
	}

	// Demand source used by DMIs...

	String demsrc = "" + div.getDemsrc();
	try {
		JGUIUtil.selectTokenMatches (__demandSource_JComboBox,true, " ", 0, 0, demsrc, null );
	}
	catch ( Exception e ) {
		// Default...
		Message.printWarning ( 2, routine, "Using default value demsrc = \"" +
			StateMod_Diversion.getDemsrcDefault(true) + " because data value " + demsrc + " is unknown." );
		__demandSource_JComboBox.select (StateMod_Diversion.getDemsrcDefault(true) );
	}

	// Daily ID...
	// Put the current ID - Name into the combo box and if there was
	// one already placed at the top of the combo box, remove it
	// and place it back in the list in alphabetical order.

	// First add the current diversion at the top of the list (removing the
	// previous item from the list if necessary)...

	if (__lastDiversionIndex != -1) {
		// Remove the previous diversion as the first structure in the choice...
		String s = __diversionDailyID_JComboBox.getStringAt(3);
		__diversionDailyID_JComboBox.removeAt(3);
		// Add alphabetically in the list...
		__diversionDailyID_JComboBox.addAlpha(s, 3);
	}
	String s = div.getID() + " - " + div.getName();
	// Remove the current diversion from the full list...
	__diversionDailyID_JComboBox.remove(s);
	// Now add as the first structure in the list...
	__diversionDailyID_JComboBox.addAt(s, 3);
	// For now do not remove any "Unknown" items as added below.  Hopefully there will not be any...

	// Now select the item that matches the data...

	String cdividy = div.getCdividy();
	if (cdividy.equals("")) {
		// Default to using monthly values...
		__diversionDailyID_JComboBox.select(StateMod_Diversion.getCdividyDefault(true) );
	}
	else {
		// A value has been specified...
		try {
			JGUIUtil.selectTokenMatches (__diversionDailyID_JComboBox, true, " -", 0, 0, cdividy, null);
		}
		catch ( Exception e ) {
			Message.printWarning(2, routine, "No dividy value matching '" + div.getID() +
				"' found in combo box.  Adding as Unknown.");
			s = cdividy + " - Unknown";
			// Add at the end...
			__diversionDailyID_JComboBox.add(s);
			__diversionDailyID_JComboBox.select(s);
		}
	}

	// AWC...

	StateMod_GUIUtil.checkAndSet(div.getAWC(), __awc_JTextField);

	// Return flows and water rights are accessed only when those windows are opened.

	// Efficiency switch...

	if ( !StateMod_Util.isMissing(div.getDivefc()) && (div.getDivefc() < 0) ) {
		// Display monthly efficiencies...
		displayEff ( true, true );
	}
	else {
		// Display constant efficiencies...
		displayEff ( false, true );
	}

	// For time series checkboxes, do not change the state of the checkbox,
	// only whether enabled - that way if the user has picked a combination
	// of parameters it is easy for them to keep the same settings when
	// switching between stations.  Make sure to do the following after
	// the generic enable/disable code is called above!

	if ( div.getDiversionMonthTS() != null ) {
		__ts_diversion_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_diversion_monthly_JCheckBox.setEnabled(false);
	}

	// This will be used if cdividy == 3...
	if ( div.getDiversionDayTS() != null ) {
		__ts_diversion_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_diversion_daily_JCheckBox.setEnabled(false);
	}

	// Get this from the display because some default may have been assigned...

	cdividy = StringUtil.getToken (__diversionDailyID_JComboBox.getSelected(), " -",0,0 );
	int pos_d = 0, pos_m;		// Daily and monthly index positions
	DataSetComponent comp_d, comp_m;// Daily and monthly components
	if ( !cdividy.equals("3") ) {
		String monthid = cdividy;
		if ( cdividy.equals("0") || cdividy.equals("4") ) {
			monthid = div.getID();
		}
		// Enable if the monthly time series for this diversion is
		// available (and daily if an identifier is specified)...
		comp_m = __dataset.getComponentForComponentType ( StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY );
		pos_m = TSUtil.indexOf ( (List)comp_m.getData(), monthid, "Location", 0 );
		if ( cdividy.equalsIgnoreCase(div.getID()) ) {
			// The daily time series for this diversion must also be available...
			comp_d = __dataset.getComponentForComponentType (StateMod_DataSet.COMP_DIVERSION_TS_DAILY );
			pos_d = TSUtil.indexOf ((List)comp_d.getData(), cdividy, "Location", 0 );
		}
		else {
			// Set so the following logic will pass...
			pos_d = 0;
		}
		if ( (pos_m >= 0) && (pos_d >= 0) ) {
			__ts_diversion_est_daily_JCheckBox.setEnabled(true);
		}
		else {
			__ts_diversion_est_daily_JCheckBox.setEnabled(false);
		}
	}

	if ( div.getRights().size() > 0 ) {
		__ts_water_right_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_water_right_monthly_JCheckBox.setEnabled(false);
	}

	if ( div.getDemandMonthTS() != null ) {
		__ts_demand_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_demand_monthly_JCheckBox.setEnabled(false);
	}

	if ( div.getDemandOverrideMonthTS() != null ) {
		__ts_demand_override_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_demand_override_monthly_JCheckBox.setEnabled(false);
	}

	if ( div.getDemandAverageMonthTS() != null ) {
		__ts_demand_ave_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_demand_ave_monthly_JCheckBox.setEnabled(false);
	}

	if ( div.getDemandDayTS() != null ) {
		__ts_demand_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_demand_daily_JCheckBox.setEnabled(false);
	}

	if ( !cdividy.equals("3") ) {
		String monthid = cdividy;
		if ( cdividy.equals("0") || cdividy.equals("4") ) {
			monthid = div.getID();
		}
		// Enable if the monthly time series for this diversion is
		// available (and daily if an identifier is specified)...
		comp_m = __dataset.getComponentForComponentType ( StateMod_DataSet.COMP_DEMAND_TS_MONTHLY );
		pos_m = TSUtil.indexOf ( (List)comp_m.getData(), monthid,
				"Location", 0 );
		if ( cdividy.equalsIgnoreCase(div.getID()) ) {
			// The daily time series for this diversion must also be available...
			comp_d = __dataset.getComponentForComponentType ( StateMod_DataSet.COMP_DEMAND_TS_DAILY );
			pos_d = TSUtil.indexOf ((List)comp_d.getData(), cdividy, "Location", 0 );
		}
		else {
			// Set so the following logic will pass...
			pos_d = 0;
		}
		if ( (pos_m >= 0) && (pos_d >= 0) ) {
			__ts_demand_est_daily_JCheckBox.setEnabled(true);
		}
		else {
			__ts_demand_est_daily_JCheckBox.setEnabled(false);
		}
	}

	if ( div.getRights().size() > 0 ) {
		__ts_water_right_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_water_right_daily_JCheckBox.setEnabled(false);
	}

	if ( div.getIrrigationPracticeYearTS() != null ) {
		__ts_ipy_yearly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_ipy_yearly_JCheckBox.setEnabled(false);
	}

	if ( div.getConsumptiveWaterRequirementMonthTS() != null ) {
		__ts_cwr_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_cwr_monthly_JCheckBox.setEnabled(false);
	}

	if ( div.getConsumptiveWaterRequirementDayTS() != null ) {
		__ts_cwr_daily_JCheckBox.setEnabled(true);
	}
	else {	__ts_cwr_daily_JCheckBox.setEnabled(false);
	}

	if ( !cdividy.equals("3") ) {
		String monthid = cdividy;
		if ( cdividy.equals("0") || cdividy.equals("4") ) {
			monthid = div.getID();
		}
		// Enable if the monthly time series for this diversion is
		// available (and daily if an identifier is specified)...
		comp_m = __dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY );
		pos_m = TSUtil.indexOf ( (List)comp_m.getData(), monthid, "Location", 0 );
		if ( cdividy.equalsIgnoreCase(div.getID()) ) {
			// The daily time series for this diversion must also be available...
			comp_d = __dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY );
			pos_d = TSUtil.indexOf ((List)comp_d.getData(), cdividy, "Location", 0 );
		}
		else {
			// Set so the following logic will pass...
			pos_d = 0;
		}
		if ( (pos_m >= 0) && (pos_d >= 0) ) {
			__ts_cwr_est_daily_JCheckBox.setEnabled(true);
		}
		else {
			__ts_cwr_est_daily_JCheckBox.setEnabled(false);
		}
	}

	checkTimeSeriesButtonsStates();
	checkViewButtonState();
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
@return true if the save was successful, false if not.
*/
private boolean saveCurrentDiversion() {	
	Message.printStatus ( 1, "", "saveCurrentDiversion - start" );
	return saveData(__currentDiversionIndex);
}

/**
Saves the information associated with the currently-selected diversion.
The user doesn't need to hit the return key for the GUI to recognize changes.
The info is saved each time the user selects a different station, presses
the Close button, or presses the Apply button.
@return true if the save was successful, false if not.  True is also returned
if edits are not allowed or if no previous item has been selected.
*/
private boolean saveData(int record) {
	if ( !__editable || (record == -1) ) {
		return true;
	}

	if ( checkInput() > 0 ) {
		// Fatal errors so cannot save...
		Message.printStatus ( 1, "", "saveData detected error in checkInput - returning false" );
		return false;
	}

	StateMod_Diversion div = __diversionsVector.get(record);

	// Save in the order of the GUI.  Save all the items, even if not
	// currently editable/enabled (like ID and river node ID) because later
	// these fields may be made editable and checkInput() will warn the user about issues

	div.setID ( __diversionStationID_JTextField.getText().trim() );

	// Diversion name...

	div.setName (__diversionName_JTextField.getText().trim() );

	// River node...

	div.setCgoto (__riverNodeID_JTextField.getText().trim() );

	// Diversion capacity...

	String divcap = __diversionCapacity_JTextField.getText().trim();
	if ( divcap.equals("") && !StateMod_Util.isMissing(div.getDivcap()) ) {
		// User has blanked to missing for some reason...
		div.setDivcap(StateMod_Util.MISSING_DOUBLE);
	}
	else if ( !divcap.equals("") ) {
		// Something has changed so set it...
		div.setDivcap ( divcap );
	}

	// On/off switch...

	String idivsw = StringUtil.getToken(__diversionSwitch_JComboBox.getSelected()," ",0,0);
	div.setSwitch(StringUtil.atoi(idivsw));

	// User name...

	div.setUsername(__userName_JTextField.getText().trim());

	// Replacement reservoir...

	String ireptype = StringUtil.getToken(__replacementReservoirOption_JComboBox.getSelected()," ",0,0);
	div.setIreptype(StringUtil.atoi(ireptype));

	// Use type...

	div.setIrturn(__useType_JComboBox.getSelectedIndex());

	// Irrigated acreage...

	String area = __irrigatedAcreage_JTextField.getText().trim();
	if ( area.equals("") && !StateMod_Util.isMissing(div.getArea()) ) {
		// User has blanked to missing for some reason...
		div.setArea(StateMod_Util.MISSING_DOUBLE);
	}
	else if ( !area.equals("") ) {
		// Something has changed so set it...
		div.setArea ( area );
	}

	// Monthly demand type switch...

	String idvcom = StringUtil.getToken(__demandType_JComboBox.getSelected()," ",0,0);
	div.setIdvcom(StringUtil.atoi(idvcom));

	// Demand source...

	int demsrc = __demandSource_JComboBox.getSelectedIndex();
	div.setDemsrc(demsrc);

	// Daily data ID...

	String cdividy = StringUtil.getToken (__diversionDailyID_JComboBox.getSelected(), " -", 0, 0 );
	div.setCdividy(cdividy);

	// AWC...

	String awc = __awc_JTextField.getText().trim();
	if ( awc.equals("") && !StateMod_Util.isMissing(div.getAWC()) ) {
		// User has blanked to missing for some reason...
		div.setAWC(StateMod_Util.MISSING_DOUBLE);
	}
	else if ( !awc.equals("") ) {
		// Something has changed so set it...
		div.setAWC(awc);
	}

	// Return flows and water rights are saved when those individual screens are accessed.

	// Efficiencies...

	saveEfficiencies(div);

	// Update the main interface - if the data set is dirty, the File...Save menu will be enabled...

	if ( __dataset_wm != null ) {
		__dataset_wm.updateWindowStatus ( StateMod_DataSet_WindowManager.WINDOW_MAIN );
	}

	return true;
}

/**
Saves the efficiency information back into the object.
*/
private void saveEfficiencies(StateMod_Diversion div)
{	// Always try to save the monthly efficiencies...
	String eff;
	for (int i = 0; i < 12; i++) {
		eff = __effMonthly_JTextField[i].getText().trim();
		if ( !eff.equals("") ) {
			div.setDiveff(i,eff);
		}
	}
	if (__effMonthly_JRadioButton.isSelected()) {
		// Save the constant efficiency as a negative number to
		// indicate that monthly efficiencies will be used...
		div.setDivefc ( "-"+__effConstant_JTextField.getText().trim() );
	}
	else {
		// Save the constant efficiency as is...
		div.setDivefc ( __effConstant_JTextField.getText().trim() );
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
	if (__searchID_JRadioButton.isSelected()) {
		searchFor = __searchID_JTextField.getText().trim();
		col = 0;
	}
	else {
		searchFor = __searchName_JTextField.getText().trim();
		col = 1;
	}
	int index = __worksheet.find(searchFor, col, row, 
		JWorksheet.FIND_EQUAL_TO | JWorksheet.FIND_CONTAINS |
		JWorksheet.FIND_CASE_INSENSITIVE | JWorksheet.FIND_WRAPAROUND);
	if (index != -1) {
		// REVISIT - how to handle the last parameter...
		selectTableIndex(index, true, true);
	}
}

/**
Selects the desired ID in the table and displays the appropriate data
in the remainder of the window.  This method is called when the network is edited.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheet.getRowCount();
	StateMod_Diversion div = null;
	for (int i = 0; i < rows; i++) {
		div = (StateMod_Diversion)__worksheet.getRowData(i);
		if (div.getID().equalsIgnoreCase(id)) {
			// REVISIT - how to handle the last parameter...
			selectTableIndex(i, true, true);
			return;
		}
	}	
}

/**
Selects the desired index in the table, but also displays the appropriate data in the remainder of the window.
@param index the index to select in the list.
@param try_to_save Indicates whether the current contents should be saved before
selecting the new row.  A value of false should be passed only at startup or
when checkInput() has failed for a selection, in which case the display will
revert to the failed data rather than a new selection.
@param process_selection If true, then the display will be updated based on the
row that is selected - this should be the case most of the time.  If false, then
the previous contents are retained - this should be the case if checkInput()
detects an error, in which case we want the previous (and erroneous)
user-supplied to be shown because they need to correct the data.
*/
public void selectTableIndex ( int index, boolean try_to_save, boolean process_selection )
{	int rowCount = __worksheet.getRowCount();
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
	//__currentDiversionIndex = __worksheet.getSelectedRow();
	Message.printStatus ( 1, "", "selectTableIndex index " + index + " save flag: " + try_to_save );
	if ( process_selection ) {
		processTableSelection(index, try_to_save);
	}
}

/**
Sets up the GUI.
@param index the index of the element to highlight and display on the form.
*/
private void setupGUI(int index)
{	String routine = "StateMod_Diversion_JFrame.setupGUI";

	addWindowListener(this);

	GridBagLayout gbl = new GridBagLayout();

	JPanel main_JPanel = new JPanel();	// Contains all in center panel
	main_JPanel.setLayout(gbl);		// of the JFrame
	JPanel left_JPanel = new JPanel ();	// Left side (list and search)
	left_JPanel.setLayout(gbl);
	JPanel right_JPanel = new JPanel();	// Right side (all data)
	right_JPanel.setLayout(gbl);

	// Diversion list...

	PropList p = new PropList("StateMod_Diversion_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");
	
	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		StateMod_Diversion_TableModel tmd = new StateMod_Diversion_TableModel(__diversionsVector, __editable, true);
		StateMod_Diversion_CellRenderer crd = new StateMod_Diversion_CellRenderer(tmd);
	
		jsw = new JScrollWorksheet(crd, tmd, p);
		__worksheet = jsw.getJWorksheet();

		// Only want ID and name in this worksheet...
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

	JGUIUtil.addComponent(left_JPanel, jsw,
		0, 0, 1, 5, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	// Search panel...

	JPanel search_JPanel = new JPanel();
	search_JPanel.setLayout(gbl);
	search_JPanel.setBorder(BorderFactory.createTitledBorder("Search above list for:"));
	int y = 0;
	ButtonGroup searchCriteriaGroup = new ButtonGroup();
	__searchID_JRadioButton = new JRadioButton("ID", true);
	__searchID_JRadioButton.addActionListener(this);
	__searchID_JRadioButton.setSelected(true);
	searchCriteriaGroup.add(__searchID_JRadioButton);
	JGUIUtil.addComponent(search_JPanel, __searchID_JRadioButton,
		0, y, 1, 1, 0, 0,  
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchID_JTextField = new JTextField(15);
	__searchID_JTextField.setEnabled(true);	
	__searchID_JTextField.addActionListener(this);
	JGUIUtil.addComponent(search_JPanel, __searchID_JTextField,
		1, y, 2, 1, 1, 0,  
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__searchName_JRadioButton = new JRadioButton("Name", false);
	__searchName_JRadioButton.addActionListener(this);
	searchCriteriaGroup.add(__searchName_JRadioButton);
	JGUIUtil.addComponent(search_JPanel, __searchName_JRadioButton,
		0, ++y, 1, 1, 0, 0,  
		5, 0, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchName_JTextField = new JTextField(15);
	__searchName_JTextField.setEditable(false);
	__searchName_JTextField.setEnabled(false);
	__searchName_JTextField.addActionListener(this);
	JGUIUtil.addComponent(search_JPanel, __searchName_JTextField,
		1, y, 2, 1, 0, 0,  
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__findNextDiv_JButton = new SimpleJButton("Find Next", this);
	JGUIUtil.addComponent(search_JPanel, __findNextDiv_JButton,
		1, ++y, 2, 1, 0, 0,  
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(left_JPanel, search_JPanel,
		0, 5, 1, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);		

	JGUIUtil.addComponent(main_JPanel, left_JPanel,
		0, 0, 1, 10, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	
	// All the parametric information...

	JPanel param_JPanel = new JPanel();
	param_JPanel.setLayout ( gbl );
	y = 0;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Diversion ID:"),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__diversionStationID_JTextField = new JTextField(12);
	__diversionStationID_JTextField.setEditable(false);
	__diversionStationID_JTextField.setToolTipText (
		"<HTML>The diversion ID is the primary identifier for the diversion.<BR>"+
		"The ID is used to relate data in various data files.</HTML>");
	JGUIUtil.addComponent(param_JPanel, __diversionStationID_JTextField,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Diversion name:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__diversionName_JTextField = new JTextField(24);
	__diversionName_JTextField.setToolTipText (
		"<HTML>The diversion name is used for labels and output.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __diversionName_JTextField,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("River node ID:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__riverNodeID_JTextField = new JTextField(12);
	__riverNodeID_JTextField.setEditable(false);
	__riverNodeID_JTextField.setToolTipText (
		"<HTML>The river node is used in the network file.<BR>" +
		"In most cases the river node ID is the same as the diversion "+
		"ID,<BR>although StateMod internally uses two identifiers.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __riverNodeID_JTextField,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Capacity (CFS):"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__diversionCapacity_JTextField = new JTextField(11);
	__diversionCapacity_JTextField.setToolTipText (
		"<HTML>The capacity is a physical limit above which water cannot be diverted.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __diversionCapacity_JTextField,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("On/off Switch:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__diversionSwitch_JComboBox = new SimpleJComboBox();
	__diversionSwitch_JComboBox.setData (StateMod_Diversion.getIdivswChoices(true) );
	__diversionSwitch_JComboBox.addItemListener(this);
	__diversionSwitch_JComboBox.setToolTipText (
		"<HTML>The on/off switch tells StateMod whether to include the diversion in the analysis.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __diversionSwitch_JComboBox,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("User name:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__userName_JTextField = new JTextField(24);
	__userName_JTextField.setToolTipText (
		"<HTML>The user name is the owner for the ditch and is often set to the diversion name.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __userName_JTextField,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Replacement reservoir:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__replacementReservoirOption_JComboBox = new SimpleJComboBox();
	__replacementReservoirOption_JComboBox.addActionListener(this);
	__replacementReservoirOption_JComboBox.setData (
		StateMod_Diversion.getIreptypeChoices ( true ) );
	__replacementReservoirOption_JComboBox.setToolTipText (
		"<HTML>Used with operational right type 10 (Replacement Reservoir) (see operational rights data).</HTML>" );
	JGUIUtil.addComponent(param_JPanel,
		__replacementReservoirOption_JComboBox,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Use type:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__useType_JComboBox = new SimpleJComboBox();
	__useType_JComboBox.setData (StateMod_Diversion.getIrturnChoices(true));
	__useType_JComboBox.addActionListener(this);
	__useType_JComboBox.setToolTipText (
		"<HTML>The use type indicates how the diverted water will be used.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __useType_JComboBox,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Irrigated acreage:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__irrigatedAcreage_JTextField = new JTextField(8);
	__irrigatedAcreage_JTextField.setToolTipText (
		"<HTML>The irrigated acreage is an estimate of current " +
		"acreage and is not used by StateMod.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __irrigatedAcreage_JTextField,
		1, y, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Monthly demand type:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__demandType_JComboBox = new SimpleJComboBox();
	__demandType_JComboBox.setData (StateMod_Diversion.getIdvcomChoices(true) );
	__demandType_JComboBox.addItemListener(this);
	__demandType_JComboBox.setToolTipText (
		"<HTML>The monthly demand type indicates which " +
		" time series should be used for monthly demands.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __demandType_JComboBox,
		1, y, 2, 1, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// May be able to phase this out...
	JGUIUtil.addComponent(param_JPanel, new JLabel("Demand source:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__demandSource_JComboBox = new SimpleJComboBox();
	__demandSource_JComboBox.setData(StateMod_Diversion.getDemsrcChoices(true) );
	__demandSource_JComboBox.addActionListener(this);
	__demandSource_JComboBox.setToolTipText (
		"<HTML>The demand source is not used by StateMod.<BR>" +
		"It is used by CDSS DMI software to automate data processing.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __demandSource_JComboBox,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Daily data ID:"),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__diversionDailyID_JComboBox = new SimpleJComboBox();
	__diversionDailyID_JComboBox.setToolTipText (
		"<HTML>The daily ID is used with a daily time step (see control data).<BR>" +
		"Specify 3 if daily time series are available for the diversion.<BR>" +
		"All other cases use the monthly amount distributed to daily.</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __diversionDailyID_JComboBox,
		1, y, 2, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(param_JPanel, new JLabel("Available water content (AWC):"),
		0, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__awc_JTextField = new JTextField(12);
	__awc_JTextField.setToolTipText (
		"<HTML>Available water content (AWC) is used only when<BR>" +
		"soil moisture is considered (see control data).</HTML>" );
	JGUIUtil.addComponent(param_JPanel, __awc_JTextField,
		1, y, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(right_JPanel,param_JPanel,
		0, 0, 11, 5, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Efficiencies are in a separate panel...

	JPanel middle_JPanel = new JPanel();	// Seem to need this to prevent
	middle_JPanel.setLayout(gbl);		// efficiency text fields from
	JPanel eff_JPanel = new JPanel();	// resizing in weird ways.
	eff_JPanel.setLayout(gbl);
	eff_JPanel.setBorder(BorderFactory.createTitledBorder("System Efficiency"));
	ButtonGroup effButtonGroup = new ButtonGroup();
	__effConstant_JRadioButton=new JRadioButton("Constant efficiency",true);
	__effConstant_JRadioButton.addActionListener(this);
	__effConstant_JRadioButton.setToolTipText (
	"<HTML>A constant efficiency is used if monthly or variable efficiencies are not specified.</HTML>" );
	effButtonGroup.add(__effConstant_JRadioButton);
	JGUIUtil.addComponent(eff_JPanel, __effConstant_JRadioButton,
		0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__effConstant_JTextField = new JTextField(4);
	__effConstant_JTextField.setToolTipText (
	"<HTML>A constant efficiency is used if monthly or variable " +
	"efficiencies are not specified.</HTML>" );
	JGUIUtil.addComponent(eff_JPanel, __effConstant_JTextField,
		1, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__effMonthly_JRadioButton = new JRadioButton("Monthly efficiency  ", false);
	__effMonthly_JRadioButton.addActionListener(this);
	__effMonthly_JRadioButton.setToolTipText (
	"<HTML>Monthly efficiencies are used if variable efficiencies are not used<BR>" +
	"(see variable effiency control data).</HTML>" );
	effButtonGroup.add(__effMonthly_JRadioButton);
	JGUIUtil.addComponent(eff_JPanel, __effMonthly_JRadioButton,
		0, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	// Add the monthly efficiencies as text fields in their own panel...
	__effMonthly_JTextField = new JTextField[12];
	for (int i = 0; i < 12; i++) {
		__effMonthly_JTextField[i] = new JTextField(4);
		__effMonthly_JTextField[i].setToolTipText (
		"<HTML>Monthly efficiencies are used if variable efficiencies are not used<BR>" +
		"(see variable effiency control data).</HTML>" );
	}
	// first 6 months' efficiency
	JPanel moneff_JPanel = new JPanel();
	moneff_JPanel.setLayout(gbl);
	// Always add the fields so that field [0] corresponds to January...
	y = 0;
	int x = 0;
	if ( __dataset.getCyrl() == YearType.WATER ) {
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Oct"),
			x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Nov"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Dec"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jan"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Feb"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Mar"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	else if ( __dataset.getCyrl() == YearType.NOV_TO_OCT ) {
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Nov"),
			x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Dec"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jan"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Feb"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Mar"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Apr"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	else {	JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jan"),
			x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Feb"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Mar"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Apr"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("May"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jun"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	// Efficiencies are assumed to be in the order of the calendar type, as per StateMod documentation.
	x = -1;
	++y;
	for (int i = 0; i < 6; i++) {
		JGUIUtil.addComponent(moneff_JPanel,
			__effMonthly_JTextField[i],
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE,
			GridBagConstraints.WEST);
	}
	if ( __dataset.getCyrl() == YearType.WATER ) {
		++y;
		x = -1;
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Apr"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("May"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jun"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jul"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Aug"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Sep"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	else if ( __dataset.getCyrl() == YearType.NOV_TO_OCT ) {
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("May"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jun"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jul"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Aug"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Sep"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Oct"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	else {	JGUIUtil.addComponent(moneff_JPanel, new JLabel("Jul"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Aug"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Sep"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Oct"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Nov"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(moneff_JPanel, new JLabel("Dec"),
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	x = -1;
	++y;
	for (int i = 6; i < 12; i++) {
		JGUIUtil.addComponent(moneff_JPanel, __effMonthly_JTextField[i],
			++x, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE,
			GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(eff_JPanel, moneff_JPanel,
		1, 1, 6, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(middle_JPanel, eff_JPanel,
		0, 0, 2, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	// Related data panel...

	JPanel relatedData_JPanel = new JPanel();
	relatedData_JPanel.setLayout(gbl);
	relatedData_JPanel.setBorder(BorderFactory.createTitledBorder("Related Data"));
	int y2 = 0;
	__returnFlow_JButton = new SimpleJButton("Return Flow ...", this);
	__returnFlow_JButton.setToolTipText (
		"<HTML>Assign return flow locations and specify<BR>" +
		"delay tables for the return pattern.</HTML>" );
	JGUIUtil.addComponent(relatedData_JPanel, __returnFlow_JButton, 
		0, y2, 4, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__waterRights_JButton = new SimpleJButton("Water Rights...", this);
	__waterRights_JButton.setToolTipText (
		"<HTML>View/edit water rights for this diversion.</HTML>" );
	JGUIUtil.addComponent(relatedData_JPanel, __waterRights_JButton, 
		0, ++y2, 4, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	//JGUIUtil.addComponent(right_JPanel, relatedData_JPanel, 
	//	8, 5, 2, 2, 0, 0, 
	//	GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(middle_JPanel, relatedData_JPanel, 
		2, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Now add the middle panel to the right side of the main panel...
	JGUIUtil.addComponent(right_JPanel, middle_JPanel, 
		0, 5, 8, 2, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Time series panel...

	JPanel ts_JPanel = new JPanel();
	ts_JPanel.setLayout(gbl);
	if ( (__dataset != null) && !__dataset.areTSRead() ) {
		ts_JPanel.setBorder(BorderFactory.createTitledBorder("Time Series (time series were not read)"));
	}
	else {
		ts_JPanel.setBorder(BorderFactory.createTitledBorder("Time Series"));
	}

	y2 = 0;
	x = 0;
	__ts_diversion_monthly_JCheckBox =new JCheckBox("Historical Diversion (Monthly)");
	__ts_diversion_monthly_JCheckBox.addItemListener(this);
	__ts_diversion_monthly_JCheckBox.setToolTipText (
		"<HTML>The diversion time series is used to estimate base " +
		"(naturalized) flows and for reports.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_diversion_monthly_JCheckBox, 
		x, y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_diversion_daily_JCheckBox =new JCheckBox("Historical Diversion (Daily)");
	__ts_diversion_daily_JCheckBox.addItemListener(this);
	__ts_diversion_daily_JCheckBox.setToolTipText (
		"<HTML>The diversion time series is used to estimate base " +
		"(naturalized) flows and for reports.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_diversion_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_diversion_est_daily_JCheckBox =new JCheckBox(
		"Historical Diversion, Estimated (Daily)");
	__ts_diversion_est_daily_JCheckBox.addItemListener(this);
	__ts_diversion_est_daily_JCheckBox.setToolTipText (
		"<HTML>This time series is calculated from other data " +
		"based on the Daily Data ID.<BR>" +
		"StateMod will compute the time series at run time.<BR>" +
		"If available here, it is for information only and cannot be edited.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_diversion_est_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_water_right_monthly_JCheckBox =new JCheckBox(
		"Water Right (Monthly)");
	__ts_water_right_monthly_JCheckBox.addItemListener(this);
	__ts_water_right_monthly_JCheckBox.setToolTipText (
		"<HTML>The total water rights can be graphed as a constant." +
		"<BR>The rights in CFS are constant but AF/M vary." +
		"<BR>This indicates a legal right to water.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_water_right_monthly_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_water_right_daily_JCheckBox =new JCheckBox(
		"Water Right (Daily)");
	__ts_water_right_daily_JCheckBox.addItemListener(this);
	__ts_water_right_daily_JCheckBox.setToolTipText (
		"<HTML>The total water rights can be graphed as a constant." +
		"<BR>The rights in CFS are constant but AF/M vary." +
		"<BR>This indicates a legal right to water.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_water_right_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y2 = 0;
	__ts_demand_monthly_JCheckBox = new JCheckBox("Demands (Monthly)");
	__ts_demand_monthly_JCheckBox.addItemListener(this);
	__ts_demand_monthly_JCheckBox.setToolTipText (
		"<HTML>This demand time series is used if the Monthly demand type is 1 or 3.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_demand_monthly_JCheckBox, 
		++x, y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_demand_override_monthly_JCheckBox =new JCheckBox("Demands, Override (Monthly)");
	__ts_demand_override_monthly_JCheckBox.addItemListener(this);
	__ts_demand_override_monthly_JCheckBox.setToolTipText (
		"<HTML>This demand time series overrides the above data and is useful for scenario analysis.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel,
		__ts_demand_override_monthly_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_demand_ave_monthly_JCheckBox = new JCheckBox("Demands, Average (Monthly)");
	__ts_demand_ave_monthly_JCheckBox.addItemListener(this);
	__ts_demand_ave_monthly_JCheckBox.setToolTipText (
		"<HTML>This demand time series is used if the Monthly" +
		"demand type is 2 or 4.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_demand_ave_monthly_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_demand_daily_JCheckBox = new JCheckBox("Demands (Daily)");
	__ts_demand_daily_JCheckBox.addItemListener(this); 
	JGUIUtil.addComponent(ts_JPanel, __ts_demand_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_demand_est_daily_JCheckBox =new JCheckBox(
		"Demands, Estimated (Daily)");
	__ts_demand_est_daily_JCheckBox.addItemListener(this);
	__ts_demand_est_daily_JCheckBox.setToolTipText (
		"<HTML>This time series is calculated from other data based on the Daily Data ID.<BR>" +
		"StateMod will compute the time series at runtime.<BR>" +
		"If available here, it is for information only and cannot be edited.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_demand_est_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y2 = 0;
	__ts_ipy_yearly_JCheckBox =new JCheckBox("Irrigation Practice (Yearly)");
	__ts_ipy_yearly_JCheckBox.addItemListener(this);
	__ts_ipy_yearly_JCheckBox.setToolTipText (
		"<HTML>The irrigation practice data have yearly values " +
		"for maximum efficiency, irrigation method and acres irrigated.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_ipy_yearly_JCheckBox, 
		++x, y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_cwr_monthly_JCheckBox =new JCheckBox("Consumptive Water Requirement (Monthly)");
	__ts_cwr_monthly_JCheckBox.addItemListener(this);
	__ts_cwr_monthly_JCheckBox.setToolTipText (
		"<HTML>This requirement time series is used in place of " +
		"monthly demands<BR>if the control data indicate to use variable efficiency.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_cwr_monthly_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_cwr_daily_JCheckBox = new JCheckBox("Consumptive Water Requirement (Daily)");
	__ts_cwr_daily_JCheckBox.addItemListener(this); 
	__ts_cwr_daily_JCheckBox.setToolTipText (
		"<HTML>This requirement time series is used in place of " +
		"daily demands<BR>if the control data indicate to use variable efficiency.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_cwr_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_cwr_est_daily_JCheckBox = new JCheckBox("Consumptive Water Requirement, Estimated (Daily)");
	__ts_cwr_est_daily_JCheckBox.addItemListener(this); 
	__ts_cwr_est_daily_JCheckBox.setToolTipText (
		"<HTML>This time series is calculated from other data based on the Daily Data ID.<BR>" +
		"StateMod will compute the time series at runtime.<BR>" +
		"If available here, it is for information only and cannot be edited.</HTML>" );
	JGUIUtil.addComponent(ts_JPanel, __ts_cwr_est_daily_JCheckBox, 
		x, ++y2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel tsb_JPanel = new JPanel ();
	tsb_JPanel.setLayout ( new FlowLayout() );
	__graph_JButton = new SimpleJButton("Graph", this );
	__graph_JButton.setEnabled ( false );
	tsb_JPanel.add ( __graph_JButton );
	__table_JButton = new SimpleJButton("Table", this );
	__table_JButton.setEnabled ( false );
	tsb_JPanel.add ( __table_JButton );
	__summary_JButton = new SimpleJButton("Summary", this );
	__summary_JButton.setEnabled ( false );
	tsb_JPanel.add ( __summary_JButton );
	// Put under CWR because there is space and we need to save vertical space...
	JGUIUtil.addComponent(ts_JPanel, tsb_JPanel,
		x, ++y2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	JGUIUtil.addComponent(right_JPanel, ts_JPanel, 
		0, 7, 10, 2, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, right_JPanel,
		1, 0, 3, 10, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
		
	// Main window buttons...

	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout());
	//__help_JButton = new SimpleJButton(__BUTTON_HELP, this);
	//__help_JButton.setEnabled(false);
	__showOnMap_JButton = new SimpleJButton(__BUTTON_SHOW_ON_MAP, this);
	__showOnMap_JButton.setToolTipText(
		"Annotate map with location (button is disabled if layer does not have matching ID)" );
	__showOnNetwork_JButton = new SimpleJButton(__BUTTON_SHOW_ON_NETWORK, this);
	__showOnNetwork_JButton.setToolTipText( "Annotate network with location" );
	__close_JButton = new SimpleJButton(__BUTTON_CLOSE, this);
	__apply_JButton = new SimpleJButton(__BUTTON_APPLY, this);
	__cancel_JButton = new SimpleJButton(__BUTTON_CANCEL, this);
	button_JPanel.add(__showOnMap_JButton);
	button_JPanel.add(__showOnNetwork_JButton);
	if (__editable) {
		button_JPanel.add(__apply_JButton);
		button_JPanel.add(__cancel_JButton);
	}
	button_JPanel.add(__close_JButton);
	//button_JPanel.add(__help_JButton);
	JGUIUtil.addComponent(right_JPanel, button_JPanel,
		//6, 9, 4, 1, 0, 0,
		0, 9, 10, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST);

	getContentPane().add(main_JPanel);

	// Panel at the bottom for messages...

	JPanel bottom_JPanel = new JPanel();
	bottom_JPanel.setLayout (gbl);
	__message_JTextField = new JTextField();
	__message_JTextField.setEditable(false);
	JGUIUtil.addComponent(bottom_JPanel, __message_JTextField,
		0, 0, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__status_JTextField = new JTextField(5);
	__status_JTextField.setEditable(false);
	JGUIUtil.addComponent(bottom_JPanel, __status_JTextField,
		7, 0, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottom_JPanel);	
	
	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen ( StateMod_DataSet_WindowManager.WINDOW_DIVERSION, this );
	}

	// make sure this is done before the first call to selectTableIndex()
	populateDiversionDailyID();

	pack();
	setSize(930,650);
	JGUIUtil.center(this);
	initializeJComponents();
	selectTableIndex(index, false, true);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}	
	__initialized = true;
	__worksheet.addSortListener(this);
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
Responds to Window closing events; closes the window and marks it closed in StateMod_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	saveCurrentDiversion();
	int size = __diversionsVector.size();
	StateMod_Diversion div = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		div = __diversionsVector.get(i);
		if (!changed && div.changed()) {
			changed = true;
		}
		div.acceptChanges();
	}			
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_DIVERSION_STATIONS, true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_DIVERSION );
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
	saveCurrentDiversion();
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

/**
Called just before the worksheet is sorted.  Stores the index of the record that is selected.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortAboutToChange(JWorksheet worksheet, int sort) {
	__sortSelectedRow = __worksheet.getOriginalRowNumber(__worksheet.getSelectedRow());
}

/**
Called when the worksheet is sorted.  Reselects the record that was selected prior to the sort.
@param worksheet the worksheet being sorted.
@param sort the type of sort being performed.
*/
public void worksheetSortChanged(JWorksheet worksheet, int sort) {
	__worksheet.deselectAll();
	__worksheet.selectRow(__worksheet.getSortedRowNumber(__sortSelectedRow));
}

}
