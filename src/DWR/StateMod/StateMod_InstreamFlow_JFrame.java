// StateMod_InstreamFlow_JFrame - JFrame to edit the instream flow information.

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

import RTi.GIS.GeoView.GeoProjection;
import RTi.GIS.GeoView.GeoRecord;
import RTi.GIS.GeoView.HasGeoRecord;
import RTi.GR.GRLimits;
import RTi.GR.GRShape;
import RTi.GRTS.TSProduct;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
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

/**
This class is a gui for displaying and editing Instream Flow information.
*/
@SuppressWarnings("serial")
public class StateMod_InstreamFlow_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener, JWorksheet_SortListener {

/**
Button labels.
*/
private final String
	__BUTTON_SHOW_ON_MAP = "Show on Map",	
	__BUTTON_SHOW_ON_NETWORK = "Show on Network",
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_FIND_NEXT = "Find Next",
	__BUTTON_HELP = "Help",
	__BUTTON_WATER_RIGHTS = "Water Rights...";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
The button group for the search radio buttons.
*/
private ButtonGroup __searchCriteriaGroup;

/**
The index in __disables[] of textfields that should NEVER be made editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
The position in the worksheet of the currently-selected instream flow.
*/
private int __currentInstreamFlowIndex = -1;
/**
The position in the worksheet of the last-selected instream flow.
*/
private int __lastInstreamFlowIndex = -1;

/**
Stores the index of the record that was selected before the worksheet is sorted,
in order to reselect it after the sort is complete.
*/
private int __sortSelectedRow = -1;

/**
GUI Buttons.
*/
private JButton 
	__applyJButton,
	__cancelJButton,
	__closeJButton,
	__findNextInsf,
	__helpJButton,
	__waterRightsJButton,
	__showOnMap_JButton = null,
	__showOnNetwork_JButton = null;

/**
Checkboxes for selecting the time series to view.
*/
private JCheckBox
	__demandsMonthlyTS,
	__demandsAveMonthlyTS,
	__demandsDailyTS,
	__demandsEstDailyTS;

/**
Array of JComponents that should be disabled when nothing is selected from the list.
*/
private JComponent[] __disables;

/**
The radio buttons to select the kind of search to do.
*/
private JRadioButton 
	__searchIDJRadioButton,
	__searchNameJRadioButton;

/**
Textfields for displaying the current instream flow's data.
*/
private JTextField 
	__downstreamRiverNode,
	__instreamFlowStationID,
	__instreamFlowName,
	__upstreamRiverNode;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Textfields for entering the data on which to search the worksheet.
*/
private JTextField 
	__searchID,
	__searchName;

/**
The worksheet to hold the list of instream flows.
*/
private JWorksheet __worksheet;

/**
Buttons to determine how to view the selected time series.
*/
private SimpleJButton
	__graph_JButton = null,
	__summary_JButton = null,
	__table_JButton = null;

/**
SimpleJComboBoxes.
*/
private SimpleJComboBox 
	__dataType,
	__instreamDailyID,
	__insflowSwitch;

/**
The StateMod_DataSet that contains the statemod data.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The DataSetComponent containing the instream flow data.
*/
private DataSetComponent __instreamFlowComponent;

/**
The list of instream flows to be displayed.
*/
private List<StateMod_InstreamFlow> __instreamFlowsVector;

/**
Constructor.
@param dataset the dataset containing the instream flow information
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param editable whether the data are editable or not.
*/
public StateMod_InstreamFlow_JFrame ( StateMod_DataSet dataset, StateMod_DataSet_WindowManager
		dataset_wm, boolean editable)
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Instream Flows", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	

	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__instreamFlowComponent = __dataset.getComponentForComponentType(StateMod_DataSet.COMP_INSTREAM_STATIONS);

	@SuppressWarnings("unchecked")
	List<StateMod_InstreamFlow> isfList = (List<StateMod_InstreamFlow>)__instreamFlowComponent.getData();
	__instreamFlowsVector = isfList;
	int size = __instreamFlowsVector.size();
	StateMod_InstreamFlow isf = null;
	for (int i = 0; i < size; i++) {
		isf = __instreamFlowsVector.get(i);
		isf.createBackup();
	}

	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the dataset containing the instream flow information
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param instreamFlow the instreamFlow to select from the list
@param editable whether the gui data is editable or not
*/
public StateMod_InstreamFlow_JFrame ( StateMod_DataSet dataset, StateMod_DataSet_WindowManager dataset_wm,
	StateMod_InstreamFlow instreamFlow, boolean editable)
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Instream Flows", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__instreamFlowComponent = __dataset.getComponentForComponentType(StateMod_DataSet.COMP_INSTREAM_STATIONS);

	@SuppressWarnings("unchecked")
	List<StateMod_InstreamFlow> isfList = (List<StateMod_InstreamFlow>)__instreamFlowComponent.getData();
	__instreamFlowsVector = isfList;
	int size = __instreamFlowsVector.size();
	StateMod_InstreamFlow isf = null;
	for (int i = 0; i < size; i++) {
		isf = __instreamFlowsVector.get(i);
		isf.createBackup();
	}

	String id = instreamFlow.getID();
	int index = StateMod_Util.indexOf(__instreamFlowsVector, id);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine = "StateMod_InstreamFlow_JFrame.actionPerformed"; 

	try {
	if (Message.isDebugOn) {	
		Message.printDebug(1, routine, "In actionPerformed: " + e.getActionCommand());
	}

	String action = e.getActionCommand();
	Object source = e.getSource();

	if (source == __findNextInsf) {
		searchWorksheet(__worksheet.getSelectedRow() + 1);		
	}
	else if (source == __searchIDJRadioButton) {
		__searchName.setEditable(false);
		__searchID.setEditable(true);
	}
	else if (source == __searchNameJRadioButton) {
		__searchName.setEditable(true);
		__searchID.setEditable(false);
	}		
	else if (source == __searchID || source == __searchName) {
		searchWorksheet();
	}
	else if (action.equals(__BUTTON_HELP)) {
		// TODO HELP (JTS - 2003-06-09)
	}
	// Time series buttons...
	else if ( (source == __graph_JButton) || (source == __table_JButton)
		|| (source == __summary_JButton) ) {
		displayTSViewJFrame(source);
	}	
	else if (action.equals(__BUTTON_CLOSE)) {
		saveCurrentRecord();
		int size = __instreamFlowsVector.size();
		StateMod_InstreamFlow isf = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			isf = __instreamFlowsVector.get(i);
			if (!changed && isf.changed()) {
				changed = true;
			}
			isf.acceptChanges();
		}
		if (changed) {		
			__dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_STATIONS, true);
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow(StateMod_DataSet_WindowManager.WINDOW_INSTREAM );
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
		int size = __instreamFlowsVector.size();
		StateMod_InstreamFlow isf = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			isf = __instreamFlowsVector.get(i);
			if (!changed && isf.changed()) {
				changed = true;
			}
			isf.createBackup();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_STATIONS, true);
		}		
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		int size = __instreamFlowsVector.size();
		StateMod_InstreamFlow isf = null;
		for (int i = 0; i < size; i++) {
			isf = __instreamFlowsVector.get(i);
			isf.restoreOriginal();
		}			
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_INSTREAM);
		}
		else {
			JGUIUtil.close ( this );
		}		
	}
	else if ( source == __showOnMap_JButton ) {
		// The button is only enabled if some spatial data exist, which can be one or more of
		// upstream (this) or downstream location...
		StateMod_InstreamFlow ifs = getSelectedInstreamFlow();
		GRLimits limits = null;
		GeoProjection limitsProjection = null; // for the data limits
		if ( ifs instanceof HasGeoRecord ) {
			GeoRecord geoRecord = ((HasGeoRecord)ifs).getGeoRecord();
			if ( geoRecord != null ) {
				GRShape shapeUpstream = geoRecord.getShape();
				if ( shapeUpstream != null ) {
					limits = new GRLimits(shapeUpstream.xmin, shapeUpstream.ymin, shapeUpstream.xmax, shapeUpstream.ymax);
					limitsProjection = geoRecord.getLayer().getProjection();
				}
			}
		}
		// Extend the limits for the downstream
		StateMod_Data smdata = ifs.lookupDownstreamDataObject(__dataset);
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			GeoRecord geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				GRShape shapeDownstream = geoRecord.getShape();
				if ( shapeDownstream != null ) {
					GeoProjection layerProjection = geoRecord.getLayer().getProjection();
					if ( limitsProjection == null ) {
						limitsProjection = layerProjection;
					}
					boolean doProject = GeoProjection.needToProject ( layerProjection, limitsProjection );
					if ( doProject ) {
						shapeDownstream = GeoProjection.projectShape( layerProjection, limitsProjection, shapeDownstream, false );
					}
					if ( limits == null ) {
						limits = new GRLimits(shapeDownstream.xmin,shapeDownstream.ymin,shapeDownstream.xmax,shapeDownstream.ymax);
					}
					else {
						limits.max(shapeDownstream.xmin,shapeDownstream.ymin,shapeDownstream.xmax,shapeDownstream.ymax,true);
					}
				}
			}
		}
		__dataset_wm.showOnMap ( ifs, "Instream: " + ifs.getID() + " - " + ifs.getName(), limits,
			limitsProjection );
	}
	else if ( source == __showOnNetwork_JButton ) {
		StateMod_Network_JFrame networkEditor = __dataset_wm.getNetworkEditor();
		if ( networkEditor != null ) {
			HydrologyNode node = networkEditor.getNetworkJComponent().findNode(
				getSelectedInstreamFlow().getID(), false, false);
			if ( node != null ) {
				__dataset_wm.showOnNetwork ( getSelectedInstreamFlow(),
					"Instream: " + getSelectedInstreamFlow().getID() + " - " + getSelectedInstreamFlow().getName(),
					new GRLimits(node.getX(),node.getY(),node.getX(),node.getY()));
			}
		}
	}
	else {
		if (__currentInstreamFlowIndex == -1) {
			new ResponseJDialog(this, "You must first select an instream flow from the list.", ResponseJDialog.OK);
			return;
		}

		// set placeholder to current instream flow
		StateMod_InstreamFlow insf = __instreamFlowsVector.get(__currentInstreamFlowIndex);

		if (source == __waterRightsJButton) {
			new StateMod_InstreamFlow_Right_JFrame(__dataset, insf, __editable);
		}
	}
	}
	catch (Exception ex) {
		Message.printWarning(2, routine, "Error in action performed");
		Message.printWarning(2, routine, ex);
	}
}

/**
Checks the text fields for validity before they are saved back into the data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	List<String> errors = new Vector<String>();
	int errorCount = 0;

	// for each field, check if it contains valid input.  If not,
	// create a string of the format "fieldname -- reason why it
	// is not correct" and add it to the errors vector.  also increment error count
	
	if (errorCount == 0) {
		return true;
	}

	String plural = " was ";
	if (errorCount > 1) {
		plural = "s were ";
	}
	String label = "The following error" + plural + "encountered trying to save the record:\n";
	for (int i = 0; i < errorCount; i++) {
		label += errors.get(i) + "\n";
	}
	new ResponseJDialog(this, "Errors encountered", label, ResponseJDialog.OK);
	return false;
}

/**
Checks the states of the time series checkboxes and enables/disables the time series buttons appropriately.
*/
private void checkTimeSeriesButtonsStates() {
	boolean enabled = false;

	if(
		(__demandsMonthlyTS.isSelected() && __demandsMonthlyTS.isEnabled()) 
		|| (__demandsAveMonthlyTS.isSelected() && __demandsAveMonthlyTS.isEnabled())
		|| (__demandsDailyTS.isSelected() && __demandsDailyTS.isEnabled())
		|| (__demandsEstDailyTS.isSelected() && __demandsDailyTS.isEnabled())) {
		enabled = true;
	}

	__graph_JButton.setEnabled(enabled);
	__table_JButton.setEnabled(enabled);
	__summary_JButton.setEnabled(enabled);
}

/**
Checks the states of the map and network view buttons based on the selected diversion.
*/
private void checkViewButtonState()
{	String routine = getClass().getName() + ".checkViewButtonState";
	StateMod_InstreamFlow ifs = getSelectedInstreamFlow();
	StateMod_Data smdataDown = null;
	boolean mapEnabled = false;
	try {
		smdataDown = ifs.lookupDownstreamDataObject(__dataset);
	}
	catch ( Exception e ) {
		Message.printWarning(3, routine, e);
		__showOnMap_JButton.setEnabled ( false );
		__showOnNetwork_JButton.setEnabled ( false );
		return;
	}
	if ( ifs instanceof HasGeoRecord ) {
		HasGeoRecord hasGeoRecord = (HasGeoRecord)ifs;
		if ( hasGeoRecord.getGeoRecord() != null ) {
			mapEnabled = true;
		}
	}
	if ( smdataDown instanceof HasGeoRecord ) {
		HasGeoRecord hasGeoRecord = (HasGeoRecord)smdataDown;
		if ( hasGeoRecord.getGeoRecord() != null ) {
			mapEnabled = true;
		}
	}
	__showOnMap_JButton.setEnabled ( mapEnabled );
	__showOnNetwork_JButton.setEnabled ( true );
}

/**
Display the time series.
@param action Event action that initiated the display.
*/
private void displayTSViewJFrame(Object o)
{	String routine = "displayTSViewJFrame";

	// Initialize the display...

	PropList display_props = new PropList("Reservoir");
	if ( o == __graph_JButton ) {
		display_props.set("InitialView", "Graph");
	}
	else if ( o == __table_JButton ) {
		display_props.set("InitialView", "Table");
	}
	else if ( o == __summary_JButton ) {
		display_props.set("InitialView", "Summary");
	}

	StateMod_InstreamFlow insf = (StateMod_InstreamFlow)__instreamFlowsVector.get(__currentInstreamFlowIndex);

	// display_props.set("HelpKey", "TSTool.ExportMenu");
	display_props.set("TSViewTitleString", StateMod_Util.createDataLabel(insf,true) + " Time Series");
	display_props.set("DisplayFont", "Courier");
	display_props.set("DisplaySize", "11");
	display_props.set("PrintFont", "Courier");
	display_props.set("PrintSize", "7");
	display_props.set("PageLength", "100");

	PropList props = new PropList("Instream Flow");
	props.set("Product.TotalWidth", "600");
	props.set("Product.TotalHeight", "400");

	List<TS> tslist = new Vector<TS>();

	int sub = 0;
	int its = 0;
	TS ts = null;

	if ( (__demandsMonthlyTS.isSelected() && (insf.getDemandMonthTS() != null) )  ) {
		// Do the monthly graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Monthly Data for Instream Flow "
			+ insf.getID() + " (" + insf.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = insf.getDemandMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	if ( (__demandsAveMonthlyTS.isSelected() && (insf.getDemandAverageMonthTS() != null) ) ) {
		// Do the monthly average graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Monthly Average Data for Instream Flow "
			+ insf.getID() + " (" + insf.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = insf.getDemandAverageMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}

	// TODO - need to add estimated daily demands...
	if ( (__demandsDailyTS.isSelected() && (insf.getDemandDayTS() != null) ) ) {
		// Do the daily graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Daily Data for Instream Flow "
			+ insf.getID() + " (" + insf.getName() + ")" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = insf.getDemandDayTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add ( ts );
		}
	}
	
	try {
		TSProduct tsproduct = new TSProduct ( props, display_props );
		tsproduct.setTSList ( tslist );
		new TSViewJFrame ( tsproduct );
	}
	catch (Exception e) {
		Message.printWarning(1,routine,"Error displaying time series (" + e + ").");
		Message.printWarning(2, routine, e);
	}
}

/**
Get the selected instream flow, based on the current index in the list.
*/
private StateMod_InstreamFlow getSelectedInstreamFlow ()
{
	return __instreamFlowsVector.get(__currentInstreamFlowIndex );
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[16];
	int i = 0;
	__disables[i++] = __instreamFlowStationID;
	__disables[i++] = __applyJButton;
	__disables[i++] = __demandsMonthlyTS;
	__disables[i++] = __demandsAveMonthlyTS;
	__disables[i++] = __demandsDailyTS;
	__disables[i++] = __demandsEstDailyTS;
	__disables[i++] = __waterRightsJButton;
	__disables[i++] = __insflowSwitch;
	__disables[i++] = __downstreamRiverNode;
	__disables[i++] = __instreamDailyID;
	__disables[i++] = __instreamFlowName;
	__disables[i++] = __upstreamRiverNode;
	__disables[i++] = __table_JButton;
	__disables[i++] = __graph_JButton;
	__disables[i++] = __summary_JButton;
	__disables[i++] = __dataType;
	
	__textUneditables = new int[1];
	__textUneditables[0] = 0;
}

/**
Responds to item state changed events.
@param e the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent e) {
	checkTimeSeriesButtonsStates();
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; calls 'processTableSelection' with the newly-selected index in the table.
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
Responds to mouse released events; calls 'processTableSelection' with the newly-selected index in the table.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	processTableSelection(__worksheet.getSelectedRow());
}

/**
Populates the diversion daily id combo box.
*/
private void populateInstreamDailyID() {
	__instreamDailyID.removeAllItems();

	__instreamDailyID.add("0 - Use average daily value from monthly time series");
	__instreamDailyID.add("3 - Daily time series are supplied");
	__instreamDailyID.add("4 - Daily time series interpolated from midpoints of monthly data");

	List<String> idNameVector = StateMod_Util.createIdentifierListFromStateModData(__instreamFlowsVector, true, null);
	int size = idNameVector.size();

	String s = null;
	for (int i = 0; i < size; i++) {
		s = idNameVector.get(i);
		__instreamDailyID.add(s.trim());
	}
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it on the form.
@param index the index of the instream flow to display on the form.
*/
private void processTableSelection(int index) {
	String routine = "StateMod_InstreamFlow_JFrame.processTableSelection";
	__lastInstreamFlowIndex = __currentInstreamFlowIndex;
	__currentInstreamFlowIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__disables, true);
		return;
	}

	JGUIUtil.enableComponents(__disables, __textUneditables, __editable);
	checkTimeSeriesButtonsStates();

	StateMod_InstreamFlow insf = (StateMod_InstreamFlow)__instreamFlowsVector.get(__currentInstreamFlowIndex);
		
	__instreamFlowStationID.setText(insf.getID());
	__instreamFlowName.setText(insf.getName());
	__upstreamRiverNode.setText(insf.getCgoto());
	__downstreamRiverNode.setText(insf.getIfrrdn());

	// For checkboxes, do not change the state of the checkbox, only
	// whether enabled - that way if the user has picked a combination of
	// parameters it is easy for them to keep the same settings when
	// switching between stations.  Make sure to do the following after
	// the generic enable/disable code is called above!

	if ( insf.getDemandMonthTS() != null ) {
		__demandsMonthlyTS.setEnabled(true);
	}
	else {
		__demandsMonthlyTS.setEnabled(false);
	}

	if ( insf.getDemandAverageMonthTS() != null ) {
		__demandsAveMonthlyTS.setEnabled(true);
	}
	else {
		__demandsAveMonthlyTS.setEnabled(false);
	}

	if ( insf.getDemandDayTS() != null ) {
		__demandsDailyTS.setEnabled(true);
	}
	else {
		__demandsDailyTS.setEnabled(false);
	}

	// TODO - need to enable pattern logic based on daily ID.

	__demandsEstDailyTS.setEnabled(false);
	
	// switch
	if (insf.getSwitch() == 1) {
		__insflowSwitch.select("1 - On");
	}
	else {
		__insflowSwitch.select("0 - Off");
	}
	
	if (__lastInstreamFlowIndex != -1) {
		String s = __instreamDailyID.getStringAt(3);
		__instreamDailyID.removeAt(3);
		__instreamDailyID.addAlpha(s, 3);
	}
	String s = "" + insf.getID() + " - " + insf.getName();
	__instreamDailyID.remove(s);
	__instreamDailyID.addAt(s, 3);
	
	String c = insf.getCifridy();
	if (c.trim().equals("")) {
		if (!__instreamDailyID.setSelectedPrefixItem(insf.getID())) {
			Message.printWarning(2, routine, "No cifridy value matching '" + insf.getID() + "' found in combo box.");
			__instreamDailyID.select(0);
		}
		setOriginalCifridy(insf, "" + insf.getID());
	}
	else {
		if (!__instreamDailyID.setSelectedPrefixItem(c)) {
			Message.printWarning(2, routine, "No cifridy value matching '" + insf.getID() + "' found in combo box.");
			__instreamDailyID.select(0);
		}
	}

	int iifcom = insf.getIifcom();
	__dataType.select(iifcom);
	
	checkViewButtonState();
}

/**
Saves the prior record selected in the table; called when moving to a new record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastInstreamFlowIndex);
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentInstreamFlowIndex);
}

/**
Saves the information associated with the currently-selected instream flow.
The user doesn't need to hit the return key for the gui to recognize changes.
The info is saved each time the user selects a different station or pressed the close button.
*/
private void saveInformation(int record) {
	if (!__editable || record == -1) {
		return;
	}

	if (!checkInput()) {
		return;
	}

	// set placeholder to last instream flow
	StateMod_InstreamFlow insf = (StateMod_InstreamFlow)__instreamFlowsVector.get(record);

	insf.setID(__instreamFlowStationID.getText().trim());
	insf.setName(__instreamFlowName.getText().trim());
	insf.setCgoto(__upstreamRiverNode.getText().trim());
	insf.setIfrrdn(__downstreamRiverNode.getText().trim());
	insf.setSwitch(__insflowSwitch.getSelectedIndex());

	String cifridy = __instreamDailyID.getSelected();
	int index = cifridy.indexOf(" - ");
	if (index > -1) {
		cifridy = cifridy.substring(0, index);
	}
	cifridy = cifridy.trim();
	if (!cifridy.equalsIgnoreCase(insf.getCifridy())) {
		insf.setCifridy(cifridy);
		/*
		TODO TS (JTS - 2003-06-19)
		connect the daily ts to the linked list
		insf.connectDailyDemandTS(__dailyInsfTSVector);
		*/
	}

	insf.setIifcom(__dataType.getSelectedIndex());
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
Selects the desired ID in the table and displays the appropriate data in the remainder of the window.
@param id the identifier to select in the list.
*/
public void selectID(String id) {
	int rows = __worksheet.getRowCount();
	StateMod_InstreamFlow isf = null;
	for (int i = 0; i < rows; i++) {
		isf = (StateMod_InstreamFlow)__worksheet.getRowData(i);
		if (isf.getID().trim().equals(id.trim())) {
			selectTableIndex(i);
			return;
		}
	}
}

/**
Selects the desired index in the and also displays the appropriate data in the remainder of the window.
@param index the index to select.
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
Sets up the gui.
@param index the index of the flow to be initially selected
*/
private void setupGUI(int index) {
	String routine = "setupGUI";

	addWindowListener(this);

	JPanel p3 = new JPanel();	// div sta id -> switch for diversion

	__instreamFlowStationID = new JTextField(12);
	__instreamFlowName = new JTextField(24);
	__upstreamRiverNode = new JTextField(12);
	__downstreamRiverNode = new JTextField(12);

	__instreamDailyID = new SimpleJComboBox();
	__insflowSwitch = new SimpleJComboBox();
	__insflowSwitch.add("0 - Off");
	__insflowSwitch.add("1 - On");

	__searchID = new JTextField(15);
	__searchName = new JTextField(15);
	__findNextInsf = new JButton(__BUTTON_FIND_NEXT);
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton("ID", true);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	__searchIDJRadioButton.addActionListener(this);
	__searchNameJRadioButton = new JRadioButton("Name", false);
	__searchCriteriaGroup.add(__searchNameJRadioButton);
	__searchNameJRadioButton.addActionListener(this);

	__waterRightsJButton = new JButton(__BUTTON_WATER_RIGHTS);
	__demandsMonthlyTS = new JCheckBox("Demands (Monthly)");
	__demandsMonthlyTS.addItemListener(this);
	__demandsAveMonthlyTS = new JCheckBox("Demands (Average Monthly)");
	__demandsAveMonthlyTS.addItemListener(this);
	__demandsDailyTS = new JCheckBox("Demands (Daily)");
	__demandsDailyTS.addItemListener(this);
	__demandsEstDailyTS = new JCheckBox("Demands (Estimated Daily)");
	__demandsEstDailyTS.addItemListener(this);

	__dataType = new SimpleJComboBox();
	__dataType.add("0 - Average Monthly");
	__dataType.add("1 - Monthly");
	__dataType.add("2 - Average Monthly");

	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY).hasData()) {
		__demandsMonthlyTS.setEnabled(false);
	}
	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY).hasData()) {
		__demandsAveMonthlyTS.setEnabled(false);
	}
	if (!__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_DAILY).hasData()) {
		__demandsMonthlyTS.setEnabled(false);
	}
	__demandsEstDailyTS.setEnabled(false);

	__showOnMap_JButton = new SimpleJButton(__BUTTON_SHOW_ON_MAP, this);
	__showOnMap_JButton.setToolTipText(
		"Annotate map with location (button is disabled if layer does not have matching ID)" );
	__showOnNetwork_JButton = new SimpleJButton(__BUTTON_SHOW_ON_NETWORK, this);
	__showOnNetwork_JButton.setToolTipText( "Annotate network with location" );
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__applyJButton = new JButton(__BUTTON_APPLY);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	
	GridBagLayout gb = new GridBagLayout();
	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(gb);
	p3.setLayout(gb);

	int y;

	PropList p = new PropList("StateMod_InstreamFlow_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		StateMod_InstreamFlow_TableModel tmi = new StateMod_InstreamFlow_TableModel(__instreamFlowsVector, __editable);
		StateMod_InstreamFlow_CellRenderer cri = new StateMod_InstreamFlow_CellRenderer(tmi);

		jsw = new JScrollWorksheet(cri, tmi, p);
		__worksheet = jsw.getJWorksheet();

		widths = cri.getColumnWidths();
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

	// add top labels and text areas to panels
	y = 0;
	JGUIUtil.addComponent(p3, new JLabel("Station ID:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __instreamFlowStationID,
		1, y, 1, 1, 0, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__instreamFlowStationID.addActionListener(this);
	__instreamFlowStationID.setEditable(false);
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Name:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __instreamFlowName,
		1, y, 1, 1, 0, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__instreamFlowName.addActionListener(this);
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Daily Data ID:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __instreamDailyID,
		1, y, 1, 1, 0, 0, 
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Upstream river node:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __upstreamRiverNode,
		1, y, 1, 1, 0, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__upstreamRiverNode.addActionListener(this);
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Downstream river node:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __downstreamRiverNode,
		1, y, 1, 1, 0, 0, 
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__downstreamRiverNode.addActionListener(this);
	y++;
	JGUIUtil.addComponent(p3, new JLabel("On/Off Switch:"),
		0, y, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __insflowSwitch,
		1, y, 1, 1, 0, 0, 
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);		
	y++;
	JGUIUtil.addComponent(p3, new JLabel("Data Type:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(p3, __dataType,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JPanel subformsPanel = new JPanel();
	subformsPanel.setLayout(gb);
	subformsPanel.setBorder(BorderFactory.createTitledBorder("Related Data"));

	JGUIUtil.addComponent(subformsPanel, __waterRightsJButton,
		1, 0, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JPanel tsPanel = new JPanel();
	tsPanel.setLayout(gb);
	tsPanel.setBorder(BorderFactory.createTitledBorder("Time Series"));

	y++;		

	int y3 = 0;
	JGUIUtil.addComponent(tsPanel, __demandsMonthlyTS,
		0, y3, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(tsPanel, __demandsAveMonthlyTS,
		0, ++y3, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(tsPanel, __demandsDailyTS,
		0, ++y3, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(tsPanel, __demandsEstDailyTS,
		0, ++y3, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	// Buttons for the time series...
	JPanel tsb_JPanel = new JPanel ();
	tsb_JPanel.setLayout ( new FlowLayout() );
	__graph_JButton = new SimpleJButton ( "Graph", this );
	tsb_JPanel.add ( __graph_JButton );
	__table_JButton = new SimpleJButton ( "Table", this );
	tsb_JPanel.add ( __table_JButton );
	__summary_JButton = new SimpleJButton ( "Summary", this );
	tsb_JPanel.add ( __summary_JButton );

	JGUIUtil.addComponent(tsPanel, tsb_JPanel,
		0, ++y3, 1, 1, 1, 1,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	__waterRightsJButton.addActionListener(this);

	// add search areas
	JPanel searchPanel = new JPanel();
	searchPanel.setLayout(gb);
	searchPanel.setBorder(BorderFactory.createTitledBorder("Search above list for:"));	

	int y2 = 0;
	JGUIUtil.addComponent(searchPanel, __searchIDJRadioButton,
		0, y2, 1, 1, 0, 0, 5, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(searchPanel, __searchID,
		1, y2, 1, 1, 0, 0, 0, 0, 0, 0,
		GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__searchID.addActionListener(this);
	y2++;
	JGUIUtil.addComponent(searchPanel, __searchNameJRadioButton,
		0, y2, 1, 1, 0, 0, 5, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(searchPanel, __searchName,
		1, y2, 1, 1, 0, 0, 0, 0, 0, 0,
		GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__searchName.addActionListener(this);
	__searchName.setEditable(false);
	y2++;
	JGUIUtil.addComponent(searchPanel, __findNextInsf,
		0, y2, 4, 1, 0, 0, 
		20, 10, 20, 10,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextInsf.addActionListener(this);

	// add the help and close buttons
	JPanel pfinal = new JPanel();
	pfinal.setLayout(fl);
	pfinal.add(__showOnMap_JButton);
	pfinal.add(__showOnNetwork_JButton);
	if (__editable) {
		pfinal.add(__applyJButton);
		pfinal.add(__cancelJButton);
	}
	pfinal.add(__closeJButton);
//	pfinal.add(__helpJButton);
	__applyJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	__helpJButton.addActionListener(this);

	// add all the panels to the main form
	JGUIUtil.addComponent(mainPanel, jsw,
		0, 0, 1, 1, 1, 1, 
		10, 10, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(mainPanel, searchPanel, 
		0, 1, 1, 1, 0, 0, 
		10, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel right = new JPanel();
	right.setLayout(gb);
	JGUIUtil.addComponent(right, p3,
		0, 0, 2, 1, 0, 0, 
		10, 10, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(right, tsPanel,
		0, 1, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(right, subformsPanel,
		1, 1, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(mainPanel, right,
		1, 0, 1, 2, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
/*
	JGUIUtil.addComponent(mainPanel, pfinal,
		1, 2, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST);
*/
	getContentPane().add(mainPanel);

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
		StateMod_DataSet_WindowManager.WINDOW_INSTREAM, this );
	}

	// must be called before the first call to selectTableIndex()
	populateInstreamDailyID();

	pack();
	setSize(835,500);
	JGUIUtil.center(this);
	selectTableIndex(index);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
	__graph_JButton.setEnabled(false);
	__table_JButton.setEnabled(false);
	__summary_JButton.setEnabled(false);	

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
Responds to Window closing events; closes the window and marks it closed.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	saveCurrentRecord();
	int size = __instreamFlowsVector.size();
	StateMod_InstreamFlow isf = null;
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		isf = (StateMod_InstreamFlow)__instreamFlowsVector.get(i);
		if (!changed && isf.changed()) {
			changed = true;
		}
		isf.acceptChanges();
	}	
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_STATIONS, true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow(
		StateMod_DataSet_WindowManager.WINDOW_INSTREAM );
	}
	else {
		JGUIUtil.close ( this );
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
public void windowDeiconified(WindowEvent e)
{
}

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
public void windowOpened(WindowEvent e)
{
}

/**
Responds to Window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e)
{
}

private void setOriginalCifridy(StateMod_InstreamFlow i, String cifridy) {
	((StateMod_InstreamFlow)i._original)._cifridy = cifridy;
}

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
