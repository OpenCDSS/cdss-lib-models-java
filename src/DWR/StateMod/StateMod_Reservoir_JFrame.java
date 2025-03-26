// StateMod_Reservoir_JFrame - JFrame to edit the reservoir information.

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
import RTi.TS.MonthTS;
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
This class displays data for StateMod_Reservoir objects.
*/
@SuppressWarnings("serial")
public class StateMod_Reservoir_JFrame extends JFrame
implements ActionListener, ItemListener, KeyListener, MouseListener, 
WindowListener, JWorksheet_SortListener {

/**
Whether the data on the form can be edited or not.
*/
private boolean __editable = false;

/**
Button group that allows only one search field to be searched on.
*/
private ButtonGroup __searchCriteriaGroup = null;

/**
The index in __disables[] of text fields that should NEVER be made editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
The index in the JWorksheet and Vector of the currently-displayed Reservoir.
*/
private int __currentReservoirIndex = -1;
/**
The index in the JWorksheet and Vector of the last-displayed Reservoir.
*/
private int __lastReservoirIndex = -1;

/**
GUI Buttons.
*/
private JButton 
	__applyJButton = null,
	__areaCapacityContent = null,
	__cancelJButton = null,
	__climateFactors = null,
	__closeJButton = null,
	__helpJButton = null,
	__findNextRes = null,
	__ownerAccounts = null,
	__waterRights = null,
	__returnFlow_JButton = null,
	__showOnMap_JButton = null,
	__showOnNetwork_JButton = null;;

private SimpleJButton
	__graph_JButton = null,
	__summary_JButton = null,
	__table_JButton = null;

private JCheckBox
	__ts_precipitation_monthly_JCheckBox = null,
	__ts_evaporation_monthly_JCheckBox = null,
	__ts_maxtarget_monthly_JCheckBox = null,
	__ts_mintarget_monthly_JCheckBox = null,
	__ts_maxtarget_daily_JCheckBox = null,
	__ts_maxtarget_est_daily_JCheckBox = null,
	__ts_mintarget_daily_JCheckBox = null,
	__ts_mintarget_est_daily_JCheckBox = null,
	__ts_content_monthly_JCheckBox = null,
	__ts_content_daily_JCheckBox = null,
	__ts_content_est_daily_JCheckBox = null;

/**
Array of JComponents that should be disabled when nothing is selected from the list.
*/
private JComponent[] __disables;

/**
Radio button for selecting to search on reservoir id.
*/
private JRadioButton __searchIDJRadioButton = null;
/**
Radio button for selecting to search on reservoir name.
*/
private JRadioButton __searchNameJRadioButton = null;

/**
GUI Textfields.
*/
private JTextField 
	__deadStorageInRes = null,
	__maxReservoirContent = null,
	__maxReservoirRelease = null,
	__minReservoirContent = null,
	__reservoirLocation = null,
	__reservoirName = null,
	__reservoirStationID = null,
	__searchID = null,
	__searchName = null;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
The JWorksheet in which the list of Reservoirs is displayed.
*/
private JWorksheet __worksheet = null;

/**
Combo box for holding reservoir switch values.
*/
private SimpleJComboBox __reservoirSwitch = null;
/**
Combo box for holding one fill rule administration values.
*/
private SimpleJComboBox __oneFillRuleAdmin = null;

private SimpleJComboBox __resDailyID = null;
int __resDailyIDBaseSize = 4;

/**
Strings to be displayed on buttons.
*/
private final static String
	__BUTTON_SHOW_ON_MAP = "Show on Map",	
	__BUTTON_SHOW_ON_NETWORK = "Show on Network",
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	__BUTTON_HELP = "Help";

/**
The StateMod_Dataset that contains the StateMod data.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The data set component (in this case, reservoirs) of the StateMod's 
main StateMod_DataSet that will be used to get data.
*/
private DataSetComponent __reservoirComponent = null;

/**
The list of data in the __reservoirComponent.
*/
private List<StateMod_Reservoir> __reservoirsVector = null;

/**
Constructor.
@param dataset dataset containing the reservoir data
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param editable whether the form dat is editable or not
*/
public StateMod_Reservoir_JFrame ( StateMod_DataSet dataset, StateMod_DataSet_WindowManager
		dataset_wm, boolean editable )
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Reservoirs", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__reservoirComponent = __dataset.getComponentForComponentType( StateMod_DataSet.COMP_RESERVOIR_STATIONS);

	@SuppressWarnings("unchecked")
	List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)__reservoirComponent.getData();
	__reservoirsVector = dataList;

	int size = __reservoirsVector.size();
	for (int i = 0; i < size; i++) {
		StateMod_Reservoir r = __reservoirsVector.get(i);
		r.createBackup();
	}

	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the dataset containing the reservoir data.
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param reservoir the reservoir to select from the list.
@param editable whether the form dat is editable or not.
*/
public StateMod_Reservoir_JFrame ( StateMod_DataSet dataset, StateMod_DataSet_WindowManager dataset_wm,
	StateMod_Reservoir reservoir, boolean editable)
{
	StateMod_GUIUtil.setTitle(this, dataset, "Reservoirs", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__reservoirComponent = __dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_STATIONS);

	@SuppressWarnings("unchecked")
	List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)__reservoirComponent.getData();
	__reservoirsVector = dataList;

	int size = __reservoirsVector.size();
	for (int i = 0; i < size; i++) {
		StateMod_Reservoir r = __reservoirsVector.get(i);
		r.createBackup();
	}

	String id = reservoir.getID();
	int index = StateMod_Util.indexOf(__reservoirsVector, id);

	__editable = editable;

	setupGUI(index);
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String routine="StateMod_Reservoir_JFrame.actionPerformed"; 

	try {
		Object source = e.getSource();
	
		if ( source == __findNextRes) {
			searchWorksheet(__worksheet.getSelectedRow() + 1);
		}
		else if ( (source == __searchID) || (source == __searchName) ) {
			searchWorksheet(0);
		}
		else if ( source == __searchIDJRadioButton ) {
			__searchName.setEditable(false);
			__searchID.setEditable(true);
		}
		else if ( source == __searchNameJRadioButton) {
			__searchName.setEditable(true);
			__searchID.setEditable(false);
		}	
		else if ( source == __helpJButton) {
			// TODO HELP (JTS - 2003-06-09)
		}
		else if ( source == __closeJButton ) {
			saveCurrentRecord();
			int size = __reservoirsVector.size();
			boolean changed = false;
			for (int i = 0; i < size; i++) {
				StateMod_Reservoir r = __reservoirsVector.get(i);
				if (!changed && r.changed()) {
					changed = true;
				}
				r.acceptChanges();
			}					
			if (changed) {
				__dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
			}		
			if ( __dataset_wm != null ) {
				__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_RESERVOIR);
			}
			else {
				JGUIUtil.close ( this );
			}
		}
		else if ( source == __applyJButton ) {
			saveCurrentRecord();
			int size = __reservoirsVector.size();
			boolean changed = false;
			for (int i = 0; i < size; i++) {
				StateMod_Reservoir r = __reservoirsVector.get(i);
				if (!changed && r.changed()) {
					changed = true;
				}
				r.createBackup();
			}			
			if (changed) {
				__dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
			}		
		}
		else if ( source == __cancelJButton ) {
			int size = __reservoirsVector.size();
			for (int i = 0; i < size; i++) {
				StateMod_Reservoir r = __reservoirsVector.get(i);
				r.restoreOriginal();
			}				
			if ( __dataset_wm != null ) {
				__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_RESERVOIR);
			}
			else {
				JGUIUtil.close ( this );
			}
		}
		else if ( source == __showOnMap_JButton ) {
			GeoRecord geoRecord = getSelectedReservoir().getGeoRecord();
			GRShape shape = geoRecord.getShape();
			__dataset_wm.showOnMap ( getSelectedReservoir(),
				"Res: " + getSelectedReservoir().getID() + " - " + getSelectedReservoir().getName(),
				new GRLimits(shape.xmin, shape.ymin, shape.xmax, shape.ymax),
				geoRecord.getLayer().getProjection() );
		}
		else if ( source == __showOnNetwork_JButton ) {
			StateMod_Network_JFrame networkEditor = __dataset_wm.getNetworkEditor();
			if ( networkEditor != null ) {
				HydrologyNode node = networkEditor.getNetworkJComponent().findNode(
					getSelectedReservoir().getID(), false, false);
				if ( node != null ) {
					__dataset_wm.showOnNetwork ( getSelectedReservoir(),
						"Res: " + getSelectedReservoir().getID() + " - " + getSelectedReservoir().getName(),
						new GRLimits(node.getX(),node.getY(),node.getX(),node.getY()));
				}
			}
		}
		// Time series buttons...
		else if ( (source == __graph_JButton) || (source == __table_JButton) || (source == __summary_JButton) ) {
			displayTSViewJFrame(source);
		}
		else {	
			if (__currentReservoirIndex == -1) {
				new ResponseJDialog(this, "You must first select a reservoir from the list.", ResponseJDialog.OK);
				return;
			}
	
			// set placeholder for current reservoir
			StateMod_Reservoir res = getSelectedReservoir();
	
			if (e.getSource() == __ownerAccounts) {
				new StateMod_Reservoir_Owner_JFrame(__dataset, res, __editable);
			}
			else if (e.getSource() == __areaCapacityContent) {
				new StateMod_Reservoir_AreaCap_JFrame( __dataset, res, __editable);
			}
			else if (e.getSource() == __climateFactors) {
				new StateMod_Reservoir_Climate_JFrame( __dataset, res, __editable);
			}
			else if (e.getSource() == __waterRights) {
				new StateMod_Reservoir_Right_JFrame( __dataset, res, __editable);
			}
			else if (e.getSource() == __returnFlow_JButton) {
				new StateMod_Reservoir_Return_JFrame( __dataset, res, __editable );
			}
		}
	}
	catch (Exception ex) {
		Message.printWarning(3, routine, "Error in action performed");
		Message.printWarning(3, routine, ex);
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
Checks the states of the time series selected JCheckBoxes and enables/disables
the time series display buttons appropriately.
*/
private void checkTimeSeriesButtonsStates() {
	boolean enabled = false;

	if (
		(__ts_precipitation_monthly_JCheckBox.isSelected() && __ts_precipitation_monthly_JCheckBox.isEnabled())
		|| (__ts_evaporation_monthly_JCheckBox.isSelected() && __ts_evaporation_monthly_JCheckBox.isEnabled())
		|| (__ts_content_monthly_JCheckBox.isSelected() && __ts_content_monthly_JCheckBox.isEnabled())
		|| (__ts_content_daily_JCheckBox.isSelected() && __ts_content_daily_JCheckBox.isEnabled())
		|| (__ts_content_est_daily_JCheckBox.isSelected() && __ts_content_est_daily_JCheckBox.isEnabled())
		|| (__ts_maxtarget_monthly_JCheckBox.isSelected() && __ts_maxtarget_monthly_JCheckBox.isEnabled())
		|| (__ts_mintarget_monthly_JCheckBox.isSelected() && __ts_mintarget_monthly_JCheckBox.isEnabled())
		|| (__ts_maxtarget_daily_JCheckBox.isSelected() && __ts_maxtarget_daily_JCheckBox.isEnabled())
		|| (__ts_mintarget_est_daily_JCheckBox.isSelected() && __ts_mintarget_est_daily_JCheckBox.isEnabled())
		|| (__ts_mintarget_daily_JCheckBox.isSelected() && __ts_mintarget_daily_JCheckBox.isEnabled())
		|| (__ts_mintarget_est_daily_JCheckBox.isSelected() && __ts_mintarget_est_daily_JCheckBox.isEnabled()) ) {
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
{
	StateMod_Reservoir res = getSelectedReservoir();
	if ( res.getGeoRecord() == null ) {
		// No spatial data are available
		__showOnMap_JButton.setEnabled ( false );
	}
	else {
		// Enable the button...
		__showOnMap_JButton.setEnabled ( true );
	}
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
	// display_props.set("HelpKey", "TSTool.ExportMenu");
	display_props.set("TotalWidth", "600");
	display_props.set("TotalHeight", "400");
	display_props.set("Title", "Demand");
	display_props.set("DisplayFont", "Courier");
	display_props.set("DisplaySize", "11");
	display_props.set("PrintFont", "Courier");
	display_props.set("PrintSize", "7");
	display_props.set("PageLength", "100");

	PropList props = new PropList("Reservoir");

	List<TS> tslist = new Vector<TS>();

	// Get the time series to display and set plot properties if graphing.

	int sub = 0;
	int its = 0;
	TS ts = null;
	StateMod_Reservoir res = __reservoirsVector.get(__currentReservoirIndex);
	@SuppressWarnings("unchecked")
	List<MonthTS> precip_tslist = (List<MonthTS>)(__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY)).getData();
	@SuppressWarnings("unchecked")
	List<MonthTS> evap_tslist = (List<MonthTS>)(__dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY)).getData();
	StateMod_ReservoirClimate clim = null;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "", "SAMX size evapts Vector = " + evap_tslist.size() );
		Message.printDebug ( 1, "", "SAMX numprects= " + res.getNumEvaporationMonthTS(precip_tslist,true) );
		Message.printDebug ( 1, "", "SAMX numevapts= " + res.getNumEvaporationMonthTS(evap_tslist,true) );
	}
	if ( (__ts_precipitation_monthly_JCheckBox.isSelected() &&
		(res.getNumPrecipitationMonthTS(precip_tslist,true) > 0) ) ||
		(__ts_evaporation_monthly_JCheckBox.isSelected() &&
		(res.getNumEvaporationMonthTS(evap_tslist,true) > 0)) ) {
		// Have some non-null data so add the climate data graph...
		++sub;
		props.set ( "SubProduct " + sub + ".GraphType=Bar" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Climate Data for Reservoir" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		// Get non-null precipitation.  Loop through all...
		int nclim = res.getClimates().size();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "", "SAMX will process " + nclim + " climate stations." );
		}
		for ( int i = 0; i < nclim; i++ ) {
			clim = res.getClimate ( i );
			if ( clim.getType() != StateMod_ReservoirClimate.CLIMATE_PTPX ) {
				continue;
			}
			ts = StateMod_Util.lookupTimeSeries ( clim.getID(), precip_tslist, 1 );
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add( ts );
			}
		}
		// TODO - create weighted precipitation time series.
		// Get non-null evaporation.  Loop through all...
		for ( int i = 0; i < nclim; i++ ) {
			clim = res.getClimate ( i );
			if ( clim.getType() != StateMod_ReservoirClimate.CLIMATE_EVAP ) {
				continue;
			}
			ts = StateMod_Util.lookupTimeSeries ( clim.getID(), evap_tslist, 1 );
			//Message.printStatus ( 1, "", "SAMX evap TS for " + clim.getID() + " is " + ts );
			if ( ts != null ) {
				props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
				tslist.add( ts );
			}
		}
		// TODO create weighted evaporation time seris for display.
	}

	if ( (__ts_content_monthly_JCheckBox.isSelected() && res.getContentMonthTS() != null) ||
		(__ts_content_daily_JCheckBox.isSelected() && res.getContentDayTS() != null) ||
		(__ts_maxtarget_monthly_JCheckBox.isSelected() && res.getMaxTargetMonthTS() != null) ||
		(__ts_maxtarget_daily_JCheckBox.isSelected() && res.getMaxTargetDayTS() != null) ) {
		// Add the target/content graph...
		++sub;
		its = 0;
		props.set ( "SubProduct " + sub + ".GraphType=Line" );
		props.set ( "SubProduct " + sub + ".SubTitleString=Reservoir Content and Targets" );
		props.set ( "SubProduct " + sub + ".SubTitleFontSize=12" );
		ts = res.getContentMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
		ts = res.getContentDayTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
		ts = res.getMinTargetMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
		ts = res.getMaxTargetMonthTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
		ts = res.getMinTargetDayTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
		ts = res.getMaxTargetDayTS();
		if ( ts != null ) {
			props.set ( "Data " + sub + "." + (++its) + ".TSID=" + ts.getIdentifierString() );
			tslist.add( ts );
		}
	}

	// TPDP - graph water rights over time...

	// Display the time series...

	try {
		TSProduct tsproduct = new TSProduct ( props, display_props );
		tsproduct.setTSList ( tslist );
		new TSViewJFrame ( tsproduct );
	}
	catch (Exception e) {
		Message.printWarning(1,routine,"Error displaying time series.");
		Message.printWarning(2, routine, e);
	}
}

/**
Get the selected reservoir, based on the current index in the list.
*/
private StateMod_Reservoir getSelectedReservoir ()
{
	return __reservoirsVector.get(__currentReservoirIndex);
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[27];
	int i = 0;
	__disables[i++] = __reservoirStationID;
	__disables[i++] = __applyJButton;
	__disables[i++] = __areaCapacityContent;
	__disables[i++] = __climateFactors;
	__disables[i++] = __ownerAccounts;
	__disables[i++] = __waterRights;
	__disables[i++] = __returnFlow_JButton;
	__disables[i++] = __reservoirSwitch;
	__disables[i++] = __oneFillRuleAdmin;
	__disables[i++] = __deadStorageInRes;
	__disables[i++] = __maxReservoirContent;
	__disables[i++] = __maxReservoirRelease;
	__disables[i++] = __minReservoirContent;
	__disables[i++] = __resDailyID;
	__disables[i++] = __reservoirLocation;
	__disables[i++] = __reservoirName;
	
	__disables[i++] = __ts_precipitation_monthly_JCheckBox;
	__disables[i++] = __ts_evaporation_monthly_JCheckBox;
	__disables[i++] = __ts_content_monthly_JCheckBox;
	__disables[i++] = __ts_content_daily_JCheckBox;
	__disables[i++] = __ts_content_est_daily_JCheckBox;
	__disables[i++] = __ts_maxtarget_monthly_JCheckBox;
	__disables[i++] = __ts_mintarget_monthly_JCheckBox;
	__disables[i++] = __ts_maxtarget_daily_JCheckBox;
	__disables[i++] = __ts_mintarget_est_daily_JCheckBox;
	__disables[i++] = __ts_mintarget_daily_JCheckBox;
	__disables[i++] = __ts_mintarget_est_daily_JCheckBox;

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
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it on the form.
@param index the index of the reservoir to display on the form.
*/
private void processTableSelection(int index) {
	String routine = "StateMod_Reservoir_JFrame.processTableSelection";
	__lastReservoirIndex = __currentReservoirIndex;
	__currentReservoirIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__disables, true);
		return;
	}

	StateMod_Reservoir res = __reservoirsVector.get(__currentReservoirIndex);

	JGUIUtil.enableComponents(__disables, __textUneditables, __editable);
		
	__reservoirStationID.setText(res.getID());
	__reservoirName.setText(res.getName());
	__reservoirLocation.setText(res.getCgoto());
	StateMod_GUIUtil.checkAndSet(res.getVolmin(), __minReservoirContent);
	StateMod_GUIUtil.checkAndSet(res.getVolmax(), __maxReservoirContent);
	StateMod_GUIUtil.checkAndSet(res.getFlomax(), __maxReservoirRelease);
	StateMod_GUIUtil.checkAndSet(res.getDeadst(), __deadStorageInRes);

	rebuildResDailyID();
	if (!__resDailyID.setSelectedPrefixItem(res.getCresdy())) {
		Message.printWarning(2, routine, "No cresdy value matching '" + res.getCresdy() + "' found in combo box.");
		__resDailyID.select(0);
	}

	if (res.getCresdy().equals("")) {
		setOriginalCresdy(res, "0");
	}

	// Enable/disable time series buttons

	/* TODO PATTERN SAM 2003-08-18
	if (SMUtil.isDailyTimeSeriesAvailable(res.getID(), res.getCresdy(),
		res.getMaxTargetTS(), res.getDayTargetTS(), false)) {
		__dailyTargetPattern.setEnabled(true);
	}
	else {
		__dailyTargetPattern.setEnabled(false);
	}
	*/

	// For checkboxes do not change the state of the checkbox, only whether
	// enabled - that way if the user has picked a combination of
	// parameters it is easy for them to keep the same settings when switching between reservoirs.

	if ( res.getContentMonthTS() != null) {
		__ts_content_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_content_monthly_JCheckBox.setEnabled(false);
	}

	if ( res.getContentDayTS() != null) {
		__ts_content_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_content_daily_JCheckBox.setEnabled(false);
	}

	// TODO - need to handle..
	__ts_content_est_daily_JCheckBox.setEnabled(true);

	if ( res.getMinTargetMonthTS() != null) {
		__ts_mintarget_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_mintarget_monthly_JCheckBox.setEnabled(false);
	}
	
	if ( res.getMaxTargetMonthTS() != null) {
		__ts_maxtarget_monthly_JCheckBox.setEnabled(true);
	}
	else {
		__ts_maxtarget_monthly_JCheckBox.setEnabled(false);
	}

	if ( res.getMinTargetDayTS() != null) {
		__ts_mintarget_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_mintarget_daily_JCheckBox.setEnabled(false);
	}
	// TODO - need to handle..
	__ts_mintarget_est_daily_JCheckBox.setEnabled(false);
	
	if ( res.getMaxTargetDayTS() != null) {
		__ts_maxtarget_daily_JCheckBox.setEnabled(true);
	}
	else {
		__ts_maxtarget_daily_JCheckBox.setEnabled(false);
	}
	// TODO - need to handle..
	__ts_maxtarget_est_daily_JCheckBox.setEnabled(false);

	try {
		if(StateMod_ReservoirClimate.getNumPrecip(res.getClimates())>0){
			__ts_precipitation_monthly_JCheckBox.setEnabled(true);
		}
		else {
			__ts_precipitation_monthly_JCheckBox.setEnabled(false);
		}
	}
	catch ( Exception e ) {
		__ts_precipitation_monthly_JCheckBox.setEnabled(false);
	}
	
	try {
		if (StateMod_ReservoirClimate.getNumEvap(res.getClimates())> 0){
			__ts_evaporation_monthly_JCheckBox.setEnabled(true);
		}
		else {
			__ts_evaporation_monthly_JCheckBox.setEnabled(false);
		}
	}
	catch ( Exception e ) {
		__ts_evaporation_monthly_JCheckBox.setEnabled(false);
	}

	// switch
	__reservoirSwitch.select(res.getSwitch());

	if (((int)res.getRdate()) == -1) {
		__oneFillRuleAdmin.select(0);
	}
	else {
		__oneFillRuleAdmin.select((int)res.getRdate());
	}
	
	checkViewButtonState();
}

// TODO (JTS - 2003-09-04)
// found a better way to do this in other forms -- see the sections in 
// the instream flow, diversion, and river GUIs where they fill in their Daily Data ID combo box ...
private void rebuildResDailyID() {
	// First clean out all the reservoir listings ...
	for (int i = __resDailyIDBaseSize; i < __resDailyID.getItemCount();i++){
		__resDailyID.removeAt(__resDailyIDBaseSize);
	}

	// then add the ID of the current reservoir
	String id = __reservoirStationID.getText().trim();
	String line = id + " - Daily TS provided for reservoir";
	__resDailyID.add(line);

	// then add the IDs of all the other reservoirs in the dataset,
	// making sure not to re-add the above ID again.
	List<String> ids = StateMod_Util.createIdentifierListFromStateModData(__reservoirsVector, false, null);
	
	for (int i = 0; i < ids.size(); i++) {
		String currID = (String)ids.get(i);
		if (!currID.trim().equals(id)) {
			__resDailyID.add(currID + " - Daily TS pattern of other reservoir used with monthly TS from "
				+ "this reservoir.");
		}
	}
}

/**
Saves the prior record selected in the table; called when moving to a new 
record by a table selection.
*/
private void saveLastRecord() {
	saveInformation(__lastReservoirIndex);
}

/**
Saves the current record selected in the table; called when the window is closed
or minimized or apply is pressed.
*/
private void saveCurrentRecord() {	
	saveInformation(__currentReservoirIndex);
}

/**
Saves the information associated with the currently-selected reservoir.
The user doesn't need to hit the return key for the gui to recognize changes.
The info is saved each time the user selects a different reservoir or pressed the close button.
*/
private void saveInformation(int record) {
	if (!__editable || record == -1) {
		return;
	}

	if (!checkInput()) {
		return;
	}

	StateMod_Reservoir res = __reservoirsVector.get(record);
	//Message.printStatus(1, "", "Name: '" + res.getName() + "'");
	//Message.printStatus(1, "", "Name: '" + __reservoirName.getText() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setName(__reservoirName.getText());	
	saveDailyID(record);
	//Message.printStatus(1, "", "Cgoto: '" + res.getCgoto() + "'");
	//Message.printStatus(1, "", "Cgoto: '" + __reservoirLocation.getText() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setCgoto(__reservoirLocation.getText());	
	//Message.printStatus(1, "", "MinResCon: '" + res.getVolmin() + "'");
	//Message.printStatus(1, "", "MinResCon: '" + __minReservoirContent.getText() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setVolmin(__minReservoirContent.getText());	
	//Message.printStatus(1, "", "MaxResCon: '" + res.getVolmax() + "'");
	//Message.printStatus(1, "", "MaxResCon: '" + __maxReservoirContent.getText() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setVolmax(__maxReservoirContent.getText());
	//Message.printStatus(1, "", "Flomax: '" + res.getFlomax() + "'");
	//Message.printStatus(1, "", "Flomax: '" + __maxReservoirRelease.getText() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setFlomax(__maxReservoirRelease.getText());	
	//Message.printStatus(1, "", "Dead: '" + res.getDeadst() + "'");
	//Message.printStatus(1, "", "Dead: '" + __deadStorageInRes.getText() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setDeadst(__deadStorageInRes.getText());	
	//Message.printStatus(1, "", "Switch: '" + res.getSwitch() + "'");
	//Message.printStatus(1, "", "Switch: '" + __reservoirSwitch.getSelectedIndex() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setSwitch(__reservoirSwitch.getSelectedIndex());
	//Message.printStatus(1, "", "OneFill: '" + res.getRdate() + "'");
	//Message.printStatus(1, "", "OneFill: '" + __oneFillRuleAdmin.getSelectedIndex() + "'");
	//Message.printStatus(1, "", "\t\t(" + res.isDirty() + ")");
	res.setRdate(__oneFillRuleAdmin.getSelectedIndex());
	/*
		res.setSwitch(__reservoirSwitch.getSelectedIndex());
		res.setRdate(__oneFillRuleAdmin.getSelectedIndex());
	*/
}

/**
Saves the daily id in the TS and reservoir data.
*/
private void saveDailyID(int record) {
	if (record == -1) {
		return;
	}

	StateMod_Reservoir res = __reservoirsVector.get(record);

	if (!__resDailyID.getSelected().equalsIgnoreCase(res.getCresdy())) {
		String id = __resDailyID.getSelected();
		int index = id.indexOf(" -");
		String start = id.substring(0, index);		
		res.setCresdy(start);
		/*
		REVISIT (JTS - 2003-06-11) TS is commented out
		res.connectDailyTS(__dailyResTargetTSVector);
		*/		
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
Selects a row from the left table, scrolls to that row, and displays its information.
@param id the id of the row to select
*/
public void selectID(String id) {
	int rows = __worksheet.getRowCount();
	StateMod_Reservoir res = null;
	for (int i = 0; i < rows; i++) {
		res = (StateMod_Reservoir)__worksheet.getRowData(i);
		if (res.getID().trim().equals(id.trim())) {
			selectTableIndex(i);
			return;
		}
	}
}

/**
Selects the desired index in the table, but also displays the appropriate data in the remainder of the window.
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
Sets the cresdy value in original objects in case it has to be defaulted in
the GUI (ie, if there is no value, it is defaulted to 0).
@param r the StateMod_Reservoir to default the original cresdy.
@param cresdy the cresdy to default to.
*/
private void setOriginalCresdy(StateMod_Reservoir r, String cresdy) {
	((StateMod_Reservoir)r._original)._cresdy = cresdy;
}

/**
Sets up the GUI.
@param index the index to select
*/
public void setupGUI(int index) {
	String routine = "setupGUI";	

	addWindowListener(this);	

	JPanel mainJPanel = new JPanel();
	JPanel topLeft = new JPanel();
	JPanel bottomLeft = new JPanel();
	JPanel topRight = new JPanel();
	JPanel bottomRight = new JPanel();

	JPanel left = new JPanel();
	JPanel right = new JPanel();

	__reservoirStationID = new JTextField(12);
	__reservoirName = new JTextField(24);
	__reservoirLocation = new JTextField(12);
	__minReservoirContent = new JTextField(9);
	__maxReservoirContent = new JTextField(9);
	__maxReservoirRelease = new JTextField(9);
	__deadStorageInRes = new JTextField(9);

	__resDailyID = new SimpleJComboBox();
	__resDailyID.setPrototypeDisplayValue( "0 - Average daily TS from monthly TS");
	__resDailyID.add("0 - Average daily TS from monthly TS");
	__resDailyID.add("3 - Use daily TS");
	__resDailyID.add("4 - Daily TS from connecting mid-points of monthly TS");
	__resDailyID.add("5 - Daily TS from connecting end-points of monthly TS");

	__reservoirSwitch = new SimpleJComboBox();
	__reservoirSwitch.add("0 - Off");
	__reservoirSwitch.add("1 - Store below targets");
	__reservoirSwitch.add("2 - Adjust by dead storage");
	__reservoirSwitch.add("3 - Allow store above targets");

	__oneFillRuleAdmin = new SimpleJComboBox();
	__oneFillRuleAdmin.add("-1 - Don't administer");
	__oneFillRuleAdmin.add("1 - January");
	__oneFillRuleAdmin.add("2 - February");
	__oneFillRuleAdmin.add("3 - March");
	__oneFillRuleAdmin.add("4 - April");
	__oneFillRuleAdmin.add("5 - May");
	__oneFillRuleAdmin.add("6 - June");
	__oneFillRuleAdmin.add("7 - July");
	__oneFillRuleAdmin.add("8 - August");
	__oneFillRuleAdmin.add("9 - September");
	__oneFillRuleAdmin.add("10 - October");
	__oneFillRuleAdmin.add("11 - November");
	__oneFillRuleAdmin.add("12 - December");

	__searchID = new JTextField(15);
	__searchName = new JTextField(15);
	__searchName.setEditable(false);
	__findNextRes = new JButton("Find Next");
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton("ID", true);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	__searchIDJRadioButton.addActionListener(this);
	__searchNameJRadioButton = new JRadioButton("Name", false);
	__searchCriteriaGroup.add(__searchNameJRadioButton);
	__searchNameJRadioButton.addActionListener(this);

	__ownerAccounts = new JButton("Owner Accounts...");
	__areaCapacityContent = new JButton("Content/Area/Seepage...");
	__climateFactors = new JButton("Climate Stations...");
	__waterRights = new JButton("Water Rights...");
	__returnFlow_JButton = new JButton("Return Flow...");
	__helpJButton = new JButton(__BUTTON_HELP);
	__closeJButton = new JButton(__BUTTON_CLOSE);

	GridBagLayout gb = new GridBagLayout();
	getContentPane().setLayout(new BorderLayout());
	mainJPanel.setLayout(gb);
	topLeft.setLayout(gb);
	bottomLeft.setLayout(gb);
	topRight.setLayout(gb);
	bottomRight.setLayout(gb);
	right.setLayout(gb);
	left.setLayout(gb);

	int y;
	
	PropList p = new PropList("StateMod_Reservoir_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");	
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		StateMod_Reservoir_TableModel tmr = new StateMod_Reservoir_TableModel(__reservoirsVector,__editable);
		StateMod_Reservoir_CellRenderer crr = new StateMod_Reservoir_CellRenderer(tmr);
	
		jsw = new JScrollWorksheet(crr, tmr, p);
		__worksheet = jsw.getJWorksheet();
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

	JGUIUtil.addComponent(mainJPanel, jsw,
		0, 0, 3, 1, 1, 1,  
		0, 0, 0, 20,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(mainJPanel, right,
		3, 0, 1, 18, 0, 1,  
		0, 0, 0, 10,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);

	JPanel param_JPanel = new JPanel();
	param_JPanel.setLayout(gb);

	// add top labels and text areas to panels
	y=0;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Reservoir Station ID:"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __reservoirStationID,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__reservoirStationID.addActionListener(this);
	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Name:"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __reservoirName,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__reservoirName.addActionListener(this);

	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("River Node ID:"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __reservoirLocation,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__reservoirLocation.addActionListener(this);
	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Daily Data ID:"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __resDailyID,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Storage Switch:"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __reservoirSwitch,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Minimum Content (AF):"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __minReservoirContent,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__minReservoirContent.addActionListener(this);
	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Maximum Content (AF):"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __maxReservoirContent,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__maxReservoirContent.addActionListener(this);
	y++;
	JGUIUtil.addComponent(param_JPanel,new JLabel("Maximum Release (CFS):"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __maxReservoirRelease,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__maxReservoirRelease.addActionListener(this);
	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("Dead Storage (AF):"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __deadStorageInRes,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__deadStorageInRes.addActionListener(this);
	y++;
	JGUIUtil.addComponent(param_JPanel, new JLabel("One Fill Rule Admin:"),
		0, y, 1, 1, 1, 1,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(param_JPanel, __oneFillRuleAdmin,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	y = 11;
	
	JPanel tsPanel = new JPanel();
	tsPanel.setLayout(gb);
	tsPanel.setBorder(BorderFactory.createTitledBorder("Time Series"));
	
	JPanel subformsPanel = new JPanel();
	subformsPanel.setLayout(gb);
	subformsPanel.setBorder(BorderFactory.createTitledBorder("Related Data"));	

	JGUIUtil.addComponent(right, param_JPanel,
		0, 0, 2, 11, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(right, tsPanel,
		0, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(right, subformsPanel,
		1, y, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);		

	int y4 = 0;
	__ts_precipitation_monthly_JCheckBox = new JCheckBox ("Precipitation (Monthly)" );
	__ts_precipitation_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY).hasData()) {
		__ts_precipitation_monthly_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_precipitation_monthly_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__ts_evaporation_monthly_JCheckBox = new JCheckBox ("Evaporation (Monthly)" );
	__ts_evaporation_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY).hasData()) {
		__ts_evaporation_monthly_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_evaporation_monthly_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__ts_content_monthly_JCheckBox = new JCheckBox ("Content, End of Month (Monthly)" );
	__ts_content_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY).hasData()) {
		__ts_content_monthly_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_content_monthly_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_content_daily_JCheckBox = new JCheckBox ("Content, End of Day (Daily)" );
	__ts_content_daily_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY).hasData()) {
		__ts_content_monthly_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_content_daily_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_content_est_daily_JCheckBox = new JCheckBox ("Content, End of Day (Estimated Daily)" );
	__ts_content_est_daily_JCheckBox.addItemListener(this);
	JGUIUtil.addComponent(tsPanel, __ts_content_est_daily_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_maxtarget_monthly_JCheckBox = new JCheckBox ("Target Maximum (Monthly)" );
	__ts_maxtarget_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY).hasData()) {
		__ts_maxtarget_monthly_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_maxtarget_monthly_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__ts_mintarget_monthly_JCheckBox = new JCheckBox ("Target Minimum (Monthly)" );
	__ts_mintarget_monthly_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY).hasData()) {
		__ts_mintarget_monthly_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_mintarget_monthly_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_maxtarget_daily_JCheckBox = new JCheckBox ("Target Maximum (Daily)" );
	__ts_maxtarget_daily_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_DAILY).hasData()) {
		__ts_maxtarget_daily_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_maxtarget_daily_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_maxtarget_est_daily_JCheckBox = new JCheckBox ("Target Maximum (Estimated Daily)" );
	__ts_maxtarget_est_daily_JCheckBox.addItemListener(this);
	JGUIUtil.addComponent(tsPanel, __ts_maxtarget_est_daily_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_mintarget_daily_JCheckBox = new JCheckBox ("Target Minimum (Daily)" );
	__ts_mintarget_daily_JCheckBox.addItemListener(this);
	if (!__dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_DAILY).hasData()) {
		__ts_mintarget_daily_JCheckBox.setEnabled(false);
	}
	JGUIUtil.addComponent(tsPanel, __ts_mintarget_daily_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__ts_mintarget_est_daily_JCheckBox = new JCheckBox ("Target Minimum (Estimated Daily)" );
	__ts_mintarget_est_daily_JCheckBox.addItemListener(this);
	JGUIUtil.addComponent(tsPanel, __ts_mintarget_est_daily_JCheckBox,
		0, y4++, 1, 1, 1, 1,  
		1, 0, 0, 1,
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
		0, y4++, 1, 1, 1, 1,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	int y3 = 0;
	y3++;
	JGUIUtil.addComponent(subformsPanel, __areaCapacityContent,
		0, y3, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	y3++;
	JGUIUtil.addComponent(subformsPanel, __climateFactors,
		0, y3, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	y3++;
	JGUIUtil.addComponent(subformsPanel, __ownerAccounts,
		0, y3, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	y3++;
	JGUIUtil.addComponent(subformsPanel, __waterRights,
		0, y3, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	y3++;
	JGUIUtil.addComponent(subformsPanel, __returnFlow_JButton,
		0, y3, 1, 1, 1, 1,  
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
		
	__ownerAccounts.addActionListener(this);
	__areaCapacityContent.addActionListener(this);
	__climateFactors.addActionListener(this);
	__waterRights.addActionListener(this);
	__returnFlow_JButton.addActionListener(this);

	y = 12;

	JPanel searchPanel = new JPanel();
	searchPanel.setLayout(gb);
	searchPanel.setBorder(BorderFactory.createTitledBorder("Search above list for:"));

	JGUIUtil.addComponent(mainJPanel, searchPanel,
		0, y, 2, 1, 0, 0,  
		10, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	int y2 = 0;
	JGUIUtil.addComponent(searchPanel, __searchIDJRadioButton,
		0, y2, 1, 1, 0, 0,  
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(searchPanel, __searchID,
		1, y2, 2, 1, 0, 0,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchID.addActionListener(this);
	y2++;
	JGUIUtil.addComponent(searchPanel, __searchNameJRadioButton,
		0, y2, 1, 1, 0, 0,  
		5, 0, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(searchPanel, __searchName,
		1, y2, 2, 1, 0, 0,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchName.addActionListener(this);
	y2++;
	JGUIUtil.addComponent(searchPanel, __findNextRes,
		1, y2, 2, 1, 0, 0,  
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextRes.addActionListener(this);

	// add the final buttons: help & close
	FlowLayout gl5 = new FlowLayout(FlowLayout.RIGHT);
	JPanel p5 = new JPanel();
	p5.setLayout(gl5);
	__showOnMap_JButton = new SimpleJButton(__BUTTON_SHOW_ON_MAP, this);
	__showOnMap_JButton.setToolTipText(
		"Annotate map with location (button is disabled if layer does not have matching ID)" );
	__showOnNetwork_JButton = new SimpleJButton(__BUTTON_SHOW_ON_NETWORK, this);
	__showOnNetwork_JButton.setToolTipText( "Annotate network with location" );
	__applyJButton = new JButton(__BUTTON_APPLY);
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__applyJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	p5.add(__showOnMap_JButton);
	p5.add(__showOnNetwork_JButton);
	if (__editable) {
		p5.add(__applyJButton);
		p5.add(__cancelJButton);
	}
	p5.add(__closeJButton);
	//	p5.add(__helpJButton);
	__helpJButton.addActionListener(this);
	__helpJButton.setEnabled(false);
	__closeJButton.addActionListener(this);
	/*
	JGUIUtil.addComponent(mainJPanel, p5, 
		0, y + 3, 5, 1, 0, 0,
		10, 0, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.SOUTH);
	*/

	getContentPane().add(mainJPanel);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, p5,
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
		StateMod_DataSet_WindowManager.WINDOW_RESERVOIR, this );
	}
	pack();
	right.setMinimumSize(right.getPreferredSize());
	setSize(760, 650);
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
Responds to Window closing events; closes the window and marks it closed in StateMod_GUIUtil.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	saveCurrentRecord();
	int size = __reservoirsVector.size();
	boolean changed = false;
	for (int i = 0; i < size; i++) {
		StateMod_Reservoir r = __reservoirsVector.get(i);
		if (!changed && r.changed()) {
			changed = true;
		}
		r.acceptChanges();
	}					
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
	}

	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_RESERVOIR );
	}
	else {	JGUIUtil.close ( this );
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
public void windowOpened(WindowEvent e)
{
}

/**
Responds to Window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e) {}

private int __sortSelectedRow = -1;

/**
Called just before the worksheet is sorted.
*/
public void worksheetSortAboutToChange(JWorksheet worksheet, int sort) {
	__sortSelectedRow = __worksheet.getOriginalRowNumber(__worksheet.getSelectedRow());
}

/**
Called when the worksheet is sorted.  
*/
public void worksheetSortChanged(JWorksheet worksheet, int sort) {
	__worksheet.deselectAll();
	__worksheet.selectRow(__worksheet.getSortedRowNumber(__sortSelectedRow));
}

}
