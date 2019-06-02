// StateMod_OperationalRight_JFrame - a UI for displaying and editing operational right data.

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

package DWR.StateMod;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import cdss.domain.hydrology.network.HydrologyNode;

import RTi.GIS.GeoView.GeoProjection;
import RTi.GIS.GeoView.GeoRecord;
import RTi.GIS.GeoView.HasGeoRecord;
import RTi.GR.GRLimits;
import RTi.GR.GRShape;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_SortListener;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
This class is a gui for displaying and editing operational right data.
*/
@SuppressWarnings("serial")
public class StateMod_OperationalRight_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener,
JWorksheet_SortListener {

/**
Button labels.
*/
private final String
	__BUTTON_SHOW_ON_MAP = "Show on Map",	
	__BUTTON_SHOW_ON_NETWORK = "Show on Network",
	__BUTTON_APPLY = "Apply",
	//__BUTTON_ADD = "Add",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_CLOSE = "Close",
	//__BUTTON_DELETE = "Delete",
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
The index in __disables[] of textfields that should NEVER be made editable (e.g., ID fields).
*/
private int[] __textUneditables;

/**
Index of the currently-selected operational right in the data array
(may be different from worksheet row if sorted).
*/
private int __currentOpRightsIndex;

/**
Index of the last-selected operational right in the data array
(may be different from worksheet row if sorted).
 */
private int __lastOpRightsIndex;

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
	__applyJButton,
	__showOnMap_JButton = null,
	__showOnNetwork_JButton = null;

/**
Array of JComponents that should be disabled when nothing is selected from the list.
*/
private JComponent[] __disables;

/**
Panels containing groups of data.
*/
private JPanel
	__attributes_JPanel,
	__destination_JPanel,
	__source_JPanel,
	__rioGrande_JPanel,
	__sanJuan_JPanel,
	__monthlySwitch_JPanel,
	__interveningStructuresWithoutLoss_JPanel,
	__interveningStructuresWithLoss_JPanel,
	__comments_JPanel,
	__textEditor_JPanel,
	__monthlyOprMax_JPanel,
	__monthlyOprEff_JPanel,
	__associatedOperatingRule_JPanel;

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
	__oprAdminNumber_JTextField,
	__oprName_JTextField,
	__oprID_JTextField,
	__sanJuanSjmina_JTextField,
	__sanJuanSjrela_JTextField,
	__conveyanceLoss_JTextField,
	__limits_JTextField,
	__firstYear_JTextField,
	__lastYear_JTextField,
	// Rio Grande...
	__qdebt_JTextField,
	__qdebtx_JTextField,
	__rioGrandeCoefficient_JTextField,
	__rioGrandeBasin_JTextField,
	__rioGrandeBasinYield_JTextField,
	__rioGrandeDrain_JTextField,
	__rioGrandeDrainYield_JTextField;

/**
Text area for editing the operational right, when right type is not supported.
*/
private JTextArea
	__comments_JTextArea,
	__textEditor_JTextArea;

/**
Status bar textfields 
*/
private JTextField 
	__message_JTextField,
	__status_JTextField;

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
private JWorksheet __interveningStructuresWorksheet;

/**
Combobox for holding the rule type switch.
*/
private SimpleJComboBox 
	__opr8_JComboBox,
	__destination_JComboBox,
	__destinationAccount_JComboBox,
	__source1_JComboBox,
	__source2_JComboBox,
	__source3_JComboBox,
	__source4_JComboBox,
	__source5_JComboBox,
	__sourceAccount1_JComboBox,
	__sourceAccount2_JComboBox,
	__sourceAccount3_JComboBox,
	__sourceAccount4_JComboBox,
	__sourceAccount5_JComboBox,
	__ruleTypeSwitch_JComboBox,
	__oprSwitch_JComboBox,
	__associatedPlan_JComboBox,
	__diversionType_JComboBox,
	// Associated operating rule
	__associatedOperatingRule_JComboBox,
	// Rio Grande
	__rioGrandeIndexGage_JComboBox;

/**
Combobox array for holding month switches.
*/
private SimpleJComboBox[] __monthSwitch_JComboBox = new SimpleJComboBox[12];

/**
Combobox array for holding intervening structures (without loss).
*/
private SimpleJComboBox[] __interveningStructuresWithoutLoss_JComboBox =
	new SimpleJComboBox[StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES];

/**
Combobox array for holding intervening structures (with loss).
*/
private SimpleJComboBox[] __interveningStructuresWithLoss_JComboBox =
	new SimpleJComboBox[StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES];

/**
Combobox array for holding intervening structures (with loss) type.
*/
private SimpleJComboBox[] __interveningStructuresWithLossType_JComboBox =
	new SimpleJComboBox[StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES];

/**
Textfield array for holding intervening structure loss percent.
*/
private JTextField[] __interveningStructuresWithLossPercent_JTextField = new JTextField[13];

/**
Textfield array for holding month operating limits.
*/
private JTextField[] __monthlyOprMax_JTextField = new JTextField[13];

/**
Textfield array for holding monthly efficiencies.
*/
private JTextField[] __monthlyOprEff_JTextField = new JTextField[12];

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
List of operational rights data.
*/
private List<StateMod_OperationalRight> __operationalRights;

//private int __currentItyopr = -1;

/**
Constructor.
@param dataset the StateMod_DataSet object that has the data.
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param editable whether the data are editable or not.
*/
public StateMod_OperationalRight_JFrame ( StateMod_DataSet dataset,
	StateMod_DataSet_WindowManager dataset_wm, boolean editable)
{
	StateMod_GUIUtil.setTitle(this, dataset, "Operational Rights", null);
	__currentOpRightsIndex = -1;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__operationalRightsComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_OPERATION_RIGHTS);
	@SuppressWarnings("unchecked")
	List<StateMod_OperationalRight> dataList = (List<StateMod_OperationalRight>)__operationalRightsComponent.getData();
	__operationalRights = dataList;
	int size = __operationalRights.size();
	StateMod_OperationalRight o = null;
	for (int i = 0; i < size; i++) {
		o = __operationalRights.get(i);
		o.createBackup();
	}

	__editable = editable;

	setupGUI(0);
}

/**
Constructor.
@param dataset the StateMod_DataSet object that has the data.
@param dataset_wm the dataset window manager or null if the data set windows are not being managed.
@param operationalRight the operational right to select from the list
@param editable whether the data are editable or not.
*/
public StateMod_OperationalRight_JFrame ( StateMod_DataSet dataset,
	StateMod_DataSet_WindowManager dataset_wm, StateMod_OperationalRight operationalRight,
	boolean editable )
{	
	StateMod_GUIUtil.setTitle(this, dataset, "Operational Rights", null);
	__currentOpRightsIndex = -1;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__operationalRightsComponent = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_OPERATION_RIGHTS);
	@SuppressWarnings("unchecked")
	List<StateMod_OperationalRight> dataList = (List<StateMod_OperationalRight>)__operationalRightsComponent.getData();
	__operationalRights = dataList;
	int size = __operationalRights.size();
	StateMod_OperationalRight o = null;
	for (int i = 0; i < size; i++) {
		o = __operationalRights.get(i);
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
public void actionPerformed(ActionEvent e)
{
	String action = e.getActionCommand();
	Object source = e.getSource();

	if (action.equals(__BUTTON_HELP)) {
		showHelpDocument();
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		saveCurrentRecord();
		int size = __operationalRights.size();
		StateMod_OperationalRight o = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			o = __operationalRights.get(i);
			if (!changed && o.changed()) {
				Message.printStatus(2, "", "Operational right [" + i + "] \"" + o.getID() +
					"\" has changed - accepting changes (discarding backup).");
				changed = true;
			}
			o.acceptChanges();
		}				
		if (changed) {
			// TODO SAM 2011-01-24 Why isn't this automatically triggered with set methods?
			__dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}		
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_OPERATIONAL_RIGHT) ;
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveCurrentRecord();
		int size = __operationalRights.size();
		StateMod_OperationalRight o = null;
		boolean changed = false;
		for (int i = 0; i < size; i++) {
			o = __operationalRights.get(i);
			if (!changed && o.changed()) {
				changed = true;
				Message.printStatus(2, "", "Operational right [" + i + "] \"" + o.getID() +
				"\" has changed - accepting changes (discarding backup).");
			}
			o.createBackup();
		}		
		if (changed) {
			__dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}		
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		int size = __operationalRights.size();
		StateMod_OperationalRight o = null;
		for (int i = 0; i < size; i++) {
			o = __operationalRights.get(i);
			o.restoreOriginal();
		}					
		if ( __dataset_wm != null ) {
			__dataset_wm.closeWindow ( StateMod_DataSet_WindowManager.WINDOW_OPERATIONAL_RIGHT) ;
		}
		else {
			JGUIUtil.close ( this );
		}
	}
	// Search actions...
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
	// ...end search actions
	else if ( source == __showOnMap_JButton ) {
		// The button is only enabled if some spatial data exist, which can be one or more of
		// destination, source1, source2...
		StateMod_OperationalRight opr = getSelectedOperationalRight();
		StateMod_Data smdata = opr.lookupDestinationDataObject(__dataset);
		GRLimits limits = null;
		GeoProjection limitsProjection = null; // for the data limits
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			GeoRecord geoRecord = ((HasGeoRecord)smdata).getGeoRecord();
			if ( geoRecord != null ) {
				GRShape shapeDest = geoRecord.getShape();
				if ( shapeDest != null ) {
					limits = new GRLimits(shapeDest.xmin, shapeDest.ymin, shapeDest.xmax, shapeDest.ymax);
					limitsProjection = geoRecord.getLayer().getProjection();
				}
			}
		}
		// Extend the limits for source1 and source2
		smdata = opr.lookupSource1DataObject(__dataset);
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			GeoRecord geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				GRShape shapeSource1 = geoRecord.getShape();
				if ( shapeSource1 != null ) {
					GeoProjection layerProjection = geoRecord.getLayer().getProjection();
					if ( limitsProjection == null ) {
						limitsProjection = layerProjection;
					}
					boolean doProject = GeoProjection.needToProject ( layerProjection, limitsProjection );
					if ( doProject ) {
						shapeSource1 = GeoProjection.projectShape( layerProjection, limitsProjection, shapeSource1, false );
					}
					if ( limits == null ) {
						limits = new GRLimits(shapeSource1.xmin,shapeSource1.ymin,shapeSource1.xmax,shapeSource1.ymax);
					}
					else {
						limits.max(shapeSource1.xmin,shapeSource1.ymin,shapeSource1.xmax,shapeSource1.ymax,true);
					}
				}
			}
		}
		smdata = opr.lookupSource2DataObject(__dataset);
		if ( (smdata != null) && (smdata instanceof HasGeoRecord) ) {
			HasGeoRecord hasGeoRecord = (HasGeoRecord)smdata;
			GeoRecord geoRecord = hasGeoRecord.getGeoRecord();
			if ( geoRecord != null ) {
				GRShape shapeSource2 = geoRecord.getShape();
				if ( shapeSource2 != null ) {
					GeoProjection layerProjection = geoRecord.getLayer().getProjection();
					if ( limitsProjection == null ) {
						limitsProjection = layerProjection;
					}
					boolean doProject = GeoProjection.needToProject ( layerProjection, limitsProjection );
					if ( doProject ) {
						shapeSource2 = GeoProjection.projectShape( layerProjection, limitsProjection, shapeSource2, false );
					}
					if ( limits == null ) {
						limits = new GRLimits(shapeSource2.xmin,shapeSource2.ymin,shapeSource2.xmax,shapeSource2.ymax);
					}
					else {
						limits.max(shapeSource2.xmin,shapeSource2.ymin,shapeSource2.xmax,shapeSource2.ymax,true);
					}
				}
			}
		}
		__dataset_wm.showOnMap ( opr,
			"OpRight: " + opr.getID() + " - " + opr.getName(), limits, limitsProjection );
	}
	else if ( source == __showOnNetwork_JButton ) {
		StateMod_Network_JFrame networkEditor = __dataset_wm.getNetworkEditor();
		if ( networkEditor != null ) {
			StateMod_OperationalRight opr = getSelectedOperationalRight();
			StateMod_Data smdataDest = opr.lookupDestinationDataObject(__dataset);
			GRLimits limits = null;
			if ( smdataDest != null ) {
				HydrologyNode nodeDest = networkEditor.getNetworkJComponent().findNode(
					smdataDest.getID(), false, false);
				if ( nodeDest != null ) {
					limits = new GRLimits(nodeDest.getX(),nodeDest.getY(),nodeDest.getX(),nodeDest.getY() );
				}
			}
			StateMod_Data smdataSource1 = opr.lookupSource1DataObject(__dataset);
			if ( smdataSource1 != null ) {
				HydrologyNode nodeSource1 = networkEditor.getNetworkJComponent().findNode(
					smdataSource1.getID(), false, false);
				if ( nodeSource1 != null ) {
					if ( limits == null ) {
						limits = new GRLimits(nodeSource1.getX(),nodeSource1.getY(),nodeSource1.getX(),nodeSource1.getY() );
					}
					else {
						limits = limits.max(nodeSource1.getX(),nodeSource1.getY(),nodeSource1.getX(),nodeSource1.getY(),false);
					}
				}
			}
			StateMod_Data smdataSource2 = opr.lookupSource2DataObject(__dataset);
			if ( smdataSource2 != null ) {
				HydrologyNode nodeSource2 = networkEditor.getNetworkJComponent().findNode(
					smdataSource2.getID(), false, false);
				if ( nodeSource2 != null ) {
					if ( limits == null ) {
						limits = new GRLimits(nodeSource2.getX(),nodeSource2.getY(),nodeSource2.getX(),nodeSource2.getY() );
					}
					else {
						limits = limits.max(nodeSource2.getX(),nodeSource2.getY(),nodeSource2.getX(),nodeSource2.getY(),false);
					}
				}
			}
			__dataset_wm.showOnNetwork ( opr,
				"OpRight: " + getSelectedOperationalRight().getID() + " - " + getSelectedOperationalRight().getName(),
				limits );
		}
	}
	// Operational right data...
	else if (source == __destination_JComboBox) {		
		populateOperationalRightDestinationAccount(getSelectedOperationalRight(),
			__destination_JComboBox.getSelected(),__editable);
		// TODO SAM 2011-02-06 Figure out how strict to handle this - currently rely on user
		/*
		if (__currentItyopr == 8) {
			// special case, the source is tied to the destination
			__source1_JComboBox.setSelectedItem(__destination_JComboBox.getSelected());
		}
		*/
	}
	else if (source == __source1_JComboBox) {
		// Respond to change in Source 1
		StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
		populateOperationalRightSourceAccount1(opr, __editable);
	}
	else if (source == __source2_JComboBox) {
		// Respond to change in Source 1
		StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
		populateOperationalRightSourceAccount2(opr, __editable);
	}
	else if (source == __ruleTypeSwitch_JComboBox) {
		StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
		opr.setItyopr(getFirstToken(__ruleTypeSwitch_JComboBox.getSelected()));
		// Repopulate the entire right since the operating rule controls many things...
		populateOperationalRight(opr);
	}
	// ...end operational right data
}

/**
Checks the text fields for validity before they are saved back into the data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	List<String> errors = new Vector<String>();
	int errorCount = 0;

	// For each field, check if it contains valid input.  If not,
	// create a string of the format "fieldname -- reason why it
	// is not correct" and add it to the errors vector.  Also increment error count
	
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
Checks the states of the map and network view buttons based on the selected operational right.
If any of the source or destination data are available then enable the view.
*/
private void checkViewButtonState()
{	String routine = getClass().getName() + ".checkViewButtonState";
	StateMod_OperationalRight opr = getSelectedOperationalRight();
	StateMod_Data smdataDest = null;
	StateMod_Data smdataSource1 = null;
	StateMod_Data smdataSource2 = null;
	boolean mapEnabled = false;
	boolean networkEnabled = false;
	try {
		smdataDest = opr.lookupDestinationDataObject(__dataset);
		smdataSource1 = opr.lookupSource1DataObject(__dataset);
		smdataSource2 = opr.lookupSource2DataObject(__dataset);
	}
	catch ( Exception e ) {
		Message.printWarning(3, routine, e);
		__showOnMap_JButton.setEnabled ( false );
		__showOnNetwork_JButton.setEnabled ( false );
		return;
	}
	if ( smdataDest instanceof HasGeoRecord ) {
		HasGeoRecord hasGeoRecord = (HasGeoRecord)smdataDest;
		if ( hasGeoRecord.getGeoRecord() != null ) {
			mapEnabled = true;
		}
	}
	if ( smdataSource1 instanceof HasGeoRecord ) {
		HasGeoRecord hasGeoRecord = (HasGeoRecord)smdataSource1;
		if ( hasGeoRecord.getGeoRecord() != null ) {
			mapEnabled = true;
		}
	}
	if ( smdataSource2 instanceof HasGeoRecord ) {
		HasGeoRecord hasGeoRecord = (HasGeoRecord)smdataSource2;
		if ( hasGeoRecord.getGeoRecord() != null ) {
			mapEnabled = true;
		}
	}
	if ( smdataDest != null ) {
		networkEnabled = true;
	}
	if ( smdataSource1 != null ) {
		networkEnabled = true;
	}
	if ( smdataSource2 != null ) {
		networkEnabled = true;
	}
	__showOnMap_JButton.setEnabled ( mapEnabled );
	__showOnNetwork_JButton.setEnabled ( networkEnabled );
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
	__monthlySwitch_JPanel = null;
	__interveningStructuresWithoutLoss_JPanel = null;
	__searchIDJRadioButton = null;
	__searchNameJRadioButton = null;
	__destination_JComboBox = null;
	__destinationAccount_JComboBox = null;
	__source1_JComboBox = null;
	__source2_JComboBox = null;
	__source3_JComboBox = null;
	__source4_JComboBox = null;
	__source5_JComboBox = null;
	__sourceAccount1_JComboBox = null;
	__sourceAccount2_JComboBox = null;
	__sourceAccount3_JComboBox = null;
	__sourceAccount4_JComboBox = null;
	__sourceAccount5_JComboBox = null;
	__oprAdminNumber_JTextField = null;
	__oprName_JTextField = null;
	__oprID_JTextField = null;
	__oprSwitch_JComboBox = null;
	__searchID = null;
	__searchName = null;
	__worksheet = null;
	__interveningStructuresWorksheet = null;
	__ruleTypeSwitch_JComboBox = null;
	__monthSwitch_JComboBox = null;
	__dataset = null;
	__operationalRightsComponent = null;
	__operationalRights = null;
	__opr8_JComboBox = null;
	__sanJuan_JPanel = null;
	__sanJuanSjmina_JTextField = null;
	__sanJuanSjrela_JTextField = null;
	__qdebt_JTextField = null;
	__qdebtx_JTextField = null;

	super.finalize();
}

/**
Return the data set for the operational rights being edited.
*/
private StateMod_DataSet getDataSet ()
{
	return __dataset;
}

/**
Get a data string from a component.  ComboBoxes should generally be non-editable and will be populated
with predefined choices, with format "XXX - NOTE".  If a data value is not in a list, it should be added
as something like "UknownValue - NOT MATCHED IN DATA".  In any case, spaces are used for delimiters.
Values are returned as follows:
<ol>
<li>	If the string has " - ", return the token prior to the first space, trimmed.</li>
<li>	Else, return the string, trimmed.
</ol>
*/
private String getDataString ( SimpleJComboBox comboBox )
{
	String dataString = "";
	String selected = comboBox.getSelected();
	if ( selected != null ) {
		if ( selected.indexOf(" - ") >= 0 ) {
			// Typical string.  Return the first token before the space
			dataString = selected.split(" ")[0].trim();
		}
		else {
			// For some reason the value is not in the choice so return the text as is
			dataString = selected.trim();
		}
	}
	// If the combo box is editable and the user has provided non-blank text, use it
	if ( dataString.equals("") ) {
		if ( comboBox.isEditable() ) {
			String fieldText = comboBox.getFieldText();
			if ( (fieldText != null) && !fieldText.equals("") ) {
				dataString = fieldText;
			}
		}
	}
	return dataString;
}

/**
Trims a string in the format "XXX - YYY" by removing everything after 'XXX'.
@param s the String to trim.
@return a trimmed string.
*/
private String getFirstToken(String s) {
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
Return the name of the the help document.  This can be checked for existence to enable the Help
button and also to actually display the document.
*/
private String getHelpDocumentName()
{
    String docFileName = IOUtil.getApplicationHomeDir() + "/doc/UserManual/StateMod-OperationalRights.pdf";
    // Convert for the operating system
    docFileName = IOUtil.verifyPathForOS(docFileName, true);
    return docFileName;
}

/**
Get the selected operational right, based on the current index in the list.
*/
private StateMod_OperationalRight getSelectedOperationalRight ()
{
	return __operationalRights.get(__currentOpRightsIndex);
}

/**
Initializes the arrays that are used when items are selected and deselected.
This should be called from setupGUI() before the a call is made to 
selectTableIndex().
*/
private void initializeDisables() {
	__disables = new JComponent[36];
	int i = 0;
	__disables[i++] = __oprID_JTextField;
	__disables[i++] = __oprName_JTextField;
	__disables[i++] = __applyJButton;
	__disables[i++] = __interveningStructuresWithoutLoss_JPanel;
	__disables[i++] = __ruleTypeSwitch_JComboBox;
	__disables[i++] = __oprSwitch_JComboBox;
	__disables[i++] = __destination_JComboBox;
	__disables[i++] = __destinationAccount_JComboBox;
	__disables[i++] = __source1_JComboBox;
	__disables[i++] = __source2_JComboBox;
	__disables[i++] = __source3_JComboBox;
	__disables[i++] = __source4_JComboBox;
	__disables[i++] = __source5_JComboBox;
	__disables[i++] = __sourceAccount1_JComboBox;
	__disables[i++] = __sourceAccount2_JComboBox;
	__disables[i++] = __sourceAccount3_JComboBox;
	__disables[i++] = __sourceAccount4_JComboBox;
	__disables[i++] = __sourceAccount5_JComboBox;
	__disables[i++] = __oprAdminNumber_JTextField;
	__disables[i++] = __opr8_JComboBox;
	__disables[i++] = __sanJuanSjmina_JTextField;
	__disables[i++] = __sanJuanSjrela_JTextField;
	__disables[i++] = __qdebt_JTextField;
	__disables[i++] = __qdebtx_JTextField;
	for (int j = 0; j < 12; j++) {
		__disables[i + j] = __monthSwitch_JComboBox[j];
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
Fills in the display information for the specified operational right.
@param opr the operational right to use to populate the fields for right information.
*/
private void populateOperationalRight(StateMod_OperationalRight opr)
{
	// Get the metadata for the right type
	StateMod_OperationalRight_Metadata metadata =
		StateMod_OperationalRight_Metadata.getMetadata(opr.getItyopr());
	boolean useTextEditor = false;
	if ( (metadata == null) || !metadata.getFullEditingSupported(getDataSet()) ) {
		useTextEditor = true;
	}
	
	// Populate the various panels/controls depending on right metadata
	
	int ityopr = opr.getItyopr();
	//__currentItyopr = ityopr;
	populateOperationalRightAttributes ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightDestination ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightSource ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightMonthSwitch ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightInterveningStructuresWithoutLoss ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightAssociatedOperatingRule ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightInterveningStructuresWithLoss ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightRioGrande ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightSanJuan ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightMonthlyOprMax ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightMonthlyOprEff ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightComments ( opr, metadata, useTextEditor, __editable );
	populateOperationalRightTextEditor ( opr, metadata, useTextEditor, __editable );
	
	if ( metadata != null && metadata.getFullEditingSupported(getDataSet()) ) {
		setMessageText ( "" );
	}
	else {
		setMessageText ( "Rule type " + ityopr + " is supported for text editing only." );
	}
	
	// Legacy code...
	// FIXME SAM 2011-01-31 Transfer code to new methods.
	//populateAdditionalData(opr);
	
	/*
	switch (ityopr) {
		case 1:		// reservoir release to isf
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination_JComboBox.setData(__instreamFlowIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

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
			__destination_JComboBox.setData(combineLists(__reservoirIDs, __diversionIDs));
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount_JComboBox.setSelectedPrefixItem(destAcct);

			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);
			
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);
	
			break;
			
		case 4:		// reservoir release to a direct diversion
				// by exchange with river
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			__destination_JComboBox.setData(__diversionIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);
	
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.add("-1");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);			
			
			// no additional data
			
			break;

		case 5:		// reservoir storage by exchange
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination_JComboBox.setData(__reservoirIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount_JComboBox.setSelectedPrefixItem(destAcct);

			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);
	
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			// no additional data
			
			return;

		case 6:		// reservoir to reservoir transfer (bookover)
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination_JComboBox.setData(__reservoirIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			// REVISIT (JTS - 2003-09-18)
			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("REVISIT");
			// waiting for RRB
			// __destinationAccount.setSelectedPrefixItem(destAcct);
			__destinationAccount_JComboBox.setEditable(true);
			
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.setData(StateMod_Util.createIdentifierList(__operationalRights, true));			
			__source2_JComboBox.addAt("0", 0);
			__source2_JComboBox.setSelectedPrefixItem(srcAcct2);
			__source2_JComboBox.setEditable(true);
	
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			break;
			
		case 7:		// diversion by a carrier by exchange
			if (__operationalRightIDs == null) {
				createOperationalRightIDVector();
			}
			__destination_JComboBox.setData(__operationalRightIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);
	
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			// no additional data

			break;

		case 8:		// out of priority reservoir storage
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination_JComboBox.setData(__reservoirIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount_JComboBox.setSelectedPrefixItem(destAcct);

			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(dest);
			__source1_JComboBox.setEnabled(false);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source2_JComboBox.setData(__diversionRightIDs);
			__source2_JComboBox.setSelectedPrefixItem(src2);
			__source2_JComboBox.setEditable(false);
			
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			break;

		case 9:		// reservoir target
			__destination_JComboBox.removeAllItems();
			__destination_JComboBox.add("0");
			__destination_JComboBox.setEditable(false);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("0");
			__destinationAccount_JComboBox.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(srcAcct1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			// no additional data

			break;

		case 10:	// general replacement reservoir to a 
				// diversion by a direct release or exchange
			__destination_JComboBox.removeAllItems();
			__destination_JComboBox.add("0");
			__destination_JComboBox.setEditable(false);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("0");
			__destinationAccount_JComboBox.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(srcAcct1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

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
			__destination_JComboBox.setData(combineLists(
				__reservoirIDs, __diversionIDs));
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount_JComboBox.setSelectedPrefixItem(destAcct);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source1_JComboBox.setData(__diversionRightIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.setEditable(false);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			break;
				
		case 12:	// reoperation
			__destination_JComboBox.removeAllItems();
			__destination_JComboBox.add("0");
			__destination_JComboBox.setEditable(false);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("0");
			__destinationAccount_JComboBox.setEditable(false);

			__source1_JComboBox.removeAllItems();
			__source1_JComboBox.add("0");
			__source1_JComboBox.setEditable(false);
			__source1_JComboBox.setEnabled(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.setEditable(false);
			
			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);	

			// no additional data

			break;

		case 13:	// index flow constraint on an instream flow
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination_JComboBox.setData(__instreamFlowIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);
			
			List<String> cgotoIDs = StateMod_Util.createCgotoDataList(__instreamFlows, true);
			__source1_JComboBox.setData(cgotoIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEditable(true);
			__source1_JComboBox.setEnabled(true);
			
			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("" + srcAcct1);
			__sourceAccount1_JComboBox.setEditable(true);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);
			
			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);
	
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
			__destination_JComboBox.setData(combineLists(__reservoirIDs, __diversionIDs));
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);
				
			fillDestinationAccount(ityopr, dest);
			__destinationAccount_JComboBox.setSelectedPrefixItem(destAcct);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source1_JComboBox.setData(__diversionRightIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEnabled(true);
			__source1_JComboBox.setEditable(true);
		
			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add(	"(enter a positive integer value)");

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);
		
			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);
	
			break;
			
		case 15:	// interruptible supply
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination_JComboBox.setData(__instreamFlowIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);
		
			__source1_JComboBox.removeAllItems();
			__source1_JComboBox.add("REVISIT");
			__source1_JComboBox.setEditable(true);
		
			// REVISIT (JTS - 2003-09-22)
			// checking with RRB re: ciopso1

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("" + srcAcct1);
			__sourceAccount1_JComboBox.setEditable(true);

			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			__source2_JComboBox.setData(__diversionIDs);
			__source2_JComboBox.setSelectedPrefixItem(src2);
			__source2_JComboBox.setEditable(true);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0 - Allow 100% to be Diverted");
			__sourceAccount2_JComboBox.add("-1 - Allow the Depletion to be Diverted");
			__sourceAccount2_JComboBox.setSelectedPrefixItem("" + srcAcct2);
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);
	
			// no additional data

			break;

		case 16:	// direct flow storage
			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__destination_JComboBox.setData(__reservoirIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);

			fillDestinationAccount(ityopr, dest);
			__destinationAccount_JComboBox.setSelectedPrefixItem(destAcct);

			if (__diversionRightIDs == null) {			
				createDiversionRightIDVector();
			}
			__source1_JComboBox.setData(__diversionRightIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEditable(true);
			__source1_JComboBox.setEnabled(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("1");
			__sourceAccount1_JComboBox.setEditable(false);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("" + srcAcct2);
			__sourceAccount2_JComboBox.setEditable(true);
			
			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);

			break;

		case 17:	// rio grande compact -- rio grande river
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination_JComboBox.setData(__instreamFlowIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);

			if (__streamGageIDs == null) {
				createStreamGageIDVector();
			}
			__source1_JComboBox.setData(__streamGageIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEditable(true);
			__source1_JComboBox.setEnabled(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("1");
			__sourceAccount1_JComboBox.setEditable(false);

			__source2_JComboBox.setData(__streamGageIDs);
			__source2_JComboBox.setSelectedPrefixItem(src2);
			__source2_JComboBox.setEditable(true);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("-1");
			__sourceAccount2_JComboBox.setEditable(false);
		
			src3 = opr.getCiopso3();
			srcAcct3 = opr.getIopsou3();
			src4 = opr.getCiopso4();
			srcAcct4 = opr.getIopsou4();
			src5 = opr.getCiopso5();
			srcAcct5 = opr.getIopsou5();

			__source3_JComboBox.setData(__streamGageIDs);
			__source3_JComboBox.setSelectedPrefixItem(src3);
			__source3_JComboBox.setEnabled(true);
			__source3_JComboBox.setEditable(true);

			__sourceAccount3_JComboBox.removeAllItems();
			__sourceAccount3_JComboBox.addItem("" + srcAcct3);
			__sourceAccount3_JComboBox.setEditable(true);
			__sourceAccount3_JComboBox.setEnabled(true);
		
			__source4_JComboBox.setData(__streamGageIDs);
			__source4_JComboBox.setSelectedPrefixItem(src4);
			__source4_JComboBox.setEnabled(true);
			__source4_JComboBox.setEditable(true);

			__sourceAccount4_JComboBox.removeAllItems();
			__sourceAccount4_JComboBox.addItem("" + srcAcct4);
			__sourceAccount4_JComboBox.setEditable(true);
			__sourceAccount4_JComboBox.setEnabled(true);

			__source5_JComboBox.setData(__streamGageIDs);
			__source5_JComboBox.setSelectedPrefixItem(src5);
			__source5_JComboBox.setEnabled(true);
			__source5_JComboBox.setEditable(true);

			__sourceAccount5_JComboBox.removeAllItems();
			__sourceAccount5_JComboBox.addItem("" + srcAcct5);
			__sourceAccount5_JComboBox.setEditable(true);
			__sourceAccount5_JComboBox.setEnabled(true);

			break;

		case 18:	// rio grande compact -- conejos river
			if (__instreamFlowIDs == null) {
				createInstreamFlowIDVector();
			}
			__destination_JComboBox.setData(__instreamFlowIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);

			if (__streamGageIDs == null) {
				createStreamGageIDVector();
			}
			__source1_JComboBox.setData(__streamGageIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEditable(true);
			__source1_JComboBox.setEnabled(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("1");
			__sourceAccount1_JComboBox.setEditable(false);

			__source2_JComboBox.setData(__streamGageIDs);
			__source2_JComboBox.setSelectedPrefixItem(src2);
			__source2_JComboBox.setEditable(true);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("-1");
			__sourceAccount2_JComboBox.setEditable(false);

			src3 = opr.getCiopso3();
			srcAcct3 = opr.getIopsou3();
			src4 = opr.getCiopso4();
			srcAcct4 = opr.getIopsou4();
			src5 = opr.getCiopso5();
			srcAcct5 = opr.getIopsou5();

			__source3_JComboBox.setData(__streamGageIDs);
			__source3_JComboBox.setSelectedPrefixItem(src3);
			__source3_JComboBox.setEnabled(true);
			__source3_JComboBox.setEditable(true);

			__sourceAccount3_JComboBox.removeAllItems();
			__sourceAccount3_JComboBox.addItem("" + srcAcct3);
			__sourceAccount3_JComboBox.setEditable(true);
			__sourceAccount3_JComboBox.setEnabled(true);
		
			__source4_JComboBox.setData(__streamGageIDs);
			__source4_JComboBox.setSelectedPrefixItem(src4);
			__source4_JComboBox.setEnabled(true);
			__source4_JComboBox.setEditable(true);

			__sourceAccount4_JComboBox.removeAllItems();
			__sourceAccount4_JComboBox.addItem("" + srcAcct4);
			__sourceAccount4_JComboBox.setEditable(true);
			__sourceAccount4_JComboBox.setEnabled(true);

			__source5_JComboBox.setData(__streamGageIDs);
			__source5_JComboBox.setSelectedPrefixItem(src5);
			__source5_JComboBox.setEnabled(true);
			__source5_JComboBox.setEditable(true);

			__sourceAccount5_JComboBox.removeAllItems();
			__sourceAccount5_JComboBox.addItem("" + srcAcct5);
			__sourceAccount5_JComboBox.setEditable(true);
			__sourceAccount5_JComboBox.setEnabled(true);
		
			break;
			
		case 19:	// split channel operation -- under development 
			return;
		case 20:	// san juan rip reservoir operation
			__destination_JComboBox.removeAllItems();
			__destination_JComboBox.add("0");
			__destination_JComboBox.setEditable(false);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("0");
			__destinationAccount_JComboBox.setEditable(false);

			if (__reservoirIDs == null) {
				createReservoirIDVector();
			}
			__source1_JComboBox.setData(__reservoirIDs);
			__source1_JComboBox.setSelectedPrefixItem(src1);
			__source1_JComboBox.setEditable(true);
			__source1_JComboBox.setEnabled(true);

			fillSourceAccount1(ityopr, opr);
			__sourceAccount1_JComboBox.setSelectedPrefixItem(srcAcct1);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);			

			break;

		case 21:	// sprinkler use
		case 22:	// soil moisture flow
			__destination_JComboBox.removeAllItems();
			__destination_JComboBox.add("0");
			__destination_JComboBox.setEditable(false);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("0");
			__destinationAccount_JComboBox.setEditable(false);
			
			__source1_JComboBox.removeAllItems();
			__source1_JComboBox.add("0");
			__source1_JComboBox.setEditable(false);
			__source1_JComboBox.setEnabled(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("1");
			__sourceAccount1_JComboBox.setEditable(false);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("0");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);			

			// no additional data

			break;
		case 23:	// direct flow exchange
			if (__diversionIDs == null) {
				createDiversionIDVector();
			}
			__destination_JComboBox.setData(__diversionIDs);
			__destination_JComboBox.setSelectedPrefixItem(dest);
			__destination_JComboBox.setEditable(true);

			__destinationAccount_JComboBox.removeAllItems();
			__destinationAccount_JComboBox.add("1");
			__destinationAccount_JComboBox.setEditable(false);

			if (__diversionRightIDs == null) {
				createDiversionRightIDVector();
			}
			__source1_JComboBox.setData(__diversionIDs);
			__source1_JComboBox.setEditable(true);
			__source1_JComboBox.setEnabled(true);

			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.add("0");
			__sourceAccount1_JComboBox.add("1");
			__sourceAccount1_JComboBox.setEditable(false);

			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.add("1");
			__source2_JComboBox.setEditable(false);

			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.add("0");
			__sourceAccount2_JComboBox.setEditable(false);

			disableComboBox(__source3_JComboBox);
			disableComboBox(__source4_JComboBox);
			disableComboBox(__source5_JComboBox);

			disableComboBox(__sourceAccount3_JComboBox);
			disableComboBox(__sourceAccount4_JComboBox);
			disableComboBox(__sourceAccount5_JComboBox);			

			break;


		default:
			return;
	}
	*/

}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightAssociatedOperatingRule ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__associatedOperatingRule_JPanel.setVisible(false);
		__associatedOperatingRule_JPanel.setEnabled(false);
	}
	else {
		if ( metadata.getRightTypeUsesAssociatedOperatingRule(opr.getOprLimit()) ) {
			StateMod_OperationalRight_Metadata_SourceOrDestinationType [] operatingRuleType =
				{ StateMod_OperationalRight_Metadata_SourceOrDestinationType.OPERATIONAL_RIGHT };
			StateMod_DataSet dataset = getDataSet();
			List<String> operatinalRightIDStrings = new Vector<String>();
			// Always allow blank...
			operatinalRightIDStrings.add("");
			operatinalRightIDStrings.addAll ( StateMod_Util.createIdentifierList(
				dataset, operatingRuleType, true ) );
			__associatedOperatingRule_JComboBox.setData(operatinalRightIDStrings);
			__associatedOperatingRule_JPanel.setVisible(true);
			__associatedOperatingRule_JPanel.setEnabled(true);
			__associatedOperatingRule_JComboBox.setEnabled(true);
			__associatedOperatingRule_JComboBox.setEditable(false); // User must select from choices
			// Select the matching operational right and if not in the data set add it
			String cx = opr.getCx();
			try {
				JGUIUtil.selectTokenMatches(__associatedOperatingRule_JComboBox,
					true, // Ignore case
					" ",
					0, // No special parse flag
					0, // match first token
					cx, // Match the operating rule
					null, // No default
					true); // Trim tokens before comparing
			}
			catch ( Exception e ) {
				// If here the data includes destination not in the data set so add to the list
				String choice = cx + " - NOT MATCHED IN DATA SET!";
				__associatedOperatingRule_JComboBox.add ( choice );
				__associatedOperatingRule_JComboBox.select ( choice );
			}
			// Now populate the destination account matching the selected destination
			populateOperationalRightDestinationAccount(
				opr, __associatedOperatingRule_JComboBox.getSelected(),editable);
		}
		else {
			__associatedOperatingRule_JPanel.setVisible(false);
		}
	}
}

/**
Populate operational right for the destination data panel.
*/
private void populateOperationalRightAttributes ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		((TitledBorder)__attributes_JPanel.getBorder()).setTitle(
			"Primary attributes (right is NOT fully editable - use text editor)");
		// Always reset choices to not be editable
		editable = false;
	}
	else {
		((TitledBorder)__attributes_JPanel.getBorder()).setTitle("Primary attributes");
	}
	
	// Always set the core data items...
	__oprID_JTextField.setText(opr.getID());
	__oprID_JTextField.setEditable(false); // Always NOT editable
	
	__oprName_JTextField.setText(opr.getName());
	__oprName_JTextField.setEditable(editable);
	__oprName_JTextField.setEnabled(true);
	
	// Rule type
	int ityopr = opr.getItyopr();
	try {
		JGUIUtil.selectTokenMatches(__ruleTypeSwitch_JComboBox,
			true, // Ignore case
			"-",
			0, // No special parse flag
			0, // match first token
			("" + ityopr), // Match the rule type
			null, // No default
			true); // Trim tokens before comparing
	}
	catch ( Exception e ) {
		// This should not happen because even unknown operational rights are now handled
	}
	__ruleTypeSwitch_JComboBox.setEditable(false); // User must select from choices
	__ruleTypeSwitch_JComboBox.setEnabled(true);
	
	__oprAdminNumber_JTextField.setText(opr.getRtem());
	__oprAdminNumber_JTextField.setEditable(editable);
	__oprAdminNumber_JTextField.setEnabled(true);
	
	// On/off switch - repopulate with normal choices and also add if an integer is not in the list
	List<String> oprSwitchChoices = new Vector<String>(2);
	oprSwitchChoices.add ( "1 - On" );
	oprSwitchChoices.add ( "0 - Off" );
	__oprSwitch_JComboBox.setData(oprSwitchChoices);
	int ioprsw = opr.getSwitch();
	try {
		JGUIUtil.selectTokenMatches(__oprSwitch_JComboBox,
			true, // Ignore case
			" ",
			0, // No special parse flag
			0, // match first token
			("" + ioprsw), // Match the switch
			null, // No default
			true); // Trim tokens before comparing
	}
	catch ( Exception e ) {
		// If here the data includes a year so add and select
		String choice;
		if ( ioprsw >= 0 ) {
			choice = "" + ioprsw + " - Year on";
		}
		else {
			choice =  "" + ioprsw + " - Year off";
		}
		oprSwitchChoices.add ( choice );
		__oprSwitch_JComboBox.select(choice);
	}
	__oprSwitch_JComboBox.setEditable(false); // Force user to select choice (IoBeg, IoEnd better for years)
	__oprSwitch_JComboBox.setEnabled(true);
	
	// Associated plan - always show, but may be NA if not used
	if ( metadata.getRightTypeUsesAssociatedPlan() ) {
		String creuse = opr.getCreuse();
		StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType []
		    allowedPlanTypes = metadata.getAssociatedPlanAllowedTypes();
		StateMod_DataSet dataset = getDataSet();
		List<String> planIDStrings = new Vector<String>();
		// Always allow blank...
		planIDStrings.add("");
		// Also add NA if specified...
		for ( int i = 0; i < allowedPlanTypes.length; i++ ) {
			if ( allowedPlanTypes[i] == StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA ) {
				planIDStrings.add("NA - Not used");
			}
		}
		planIDStrings.addAll ( StateMod_Util.createIdentifierList(dataset, allowedPlanTypes, true) );
		__associatedPlan_JComboBox.setData(planIDStrings);
		try {
			JGUIUtil.selectTokenMatches(__associatedPlan_JComboBox,
				true, // Ignore case
				" ",
				0, // No special parse flag
				0, // match first token
				creuse, // Match the plan ID
				null, // No default
				true); // Trim tokens before comparing
		}
		catch ( Exception e ) {
			// If here the data includes a year so add and select
			String choice = creuse + " - NOT MATCHED IN DATA SET";
			planIDStrings.add ( choice );
			__associatedPlan_JComboBox.setData(planIDStrings);
			__associatedPlan_JComboBox.select(choice);
		}
		__associatedPlan_JComboBox.setEditable(false); // Force user to select choice
		__associatedPlan_JComboBox.setEnabled(true);
	}
	else {
		List<String> planIDStrings = new Vector<String>();
		// Only NA allowed but older rights may have blank
		String creuse = opr.getCreuse();
		if ( creuse.equals("") ) {
			planIDStrings.add ("");
		}
		// Allow for upgrades
		planIDStrings.add("" + StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA + " - Not used" );
		__associatedPlan_JComboBox.setData(planIDStrings);
		__associatedPlan_JComboBox.select(0);
		__associatedPlan_JComboBox.setEditable(false); // Force user to select choice
		__associatedPlan_JComboBox.setEnabled(true);
	}
	
	// Diversion type - always show but may be NA if not used
	if ( metadata.getRightTypeUsesDiversionType() ) {
		String cdivtyp = opr.getCdivtyp();
		StateMod_OperationalRight_Metadata_DiversionType [] diversionTypes = metadata.getDiversionTypes();
		List<String> diversionTypeStrings = new Vector<String>();
		// Only display the specific choices...
		for ( int i = 0; i < diversionTypes.length; i++ ) {
			diversionTypeStrings.add("" + diversionTypes[i]);
		}
		__diversionType_JComboBox.setData(diversionTypeStrings);
		try {
			JGUIUtil.selectIgnoreCase(__diversionType_JComboBox, cdivtyp );
		}
		catch ( Exception e ) {
			// If here the data does not match recognized value
			String choice = cdivtyp + " - UNKNOWN DIVERSION TYPE";
			if ( cdivtyp.equals("") ) {
				choice = "";
			}
			diversionTypeStrings.add ( choice );
			__diversionType_JComboBox.setData(diversionTypeStrings);
			__diversionType_JComboBox.select(choice);
		}
		__diversionType_JComboBox.setEditable(false); // Force user to select choice
		__diversionType_JComboBox.setEnabled(true);
	}
	else {
		List<String> diversionTypeStrings = new Vector<String>();
		// Only NA is used, but allow blank if that is what is in the data
		String cdivtyp = opr.getCdivtyp();
		if ( cdivtyp.equals("") ) {
			diversionTypeStrings.add("");
		}
		// Allow for upgrades...
		diversionTypeStrings.add("" + StateMod_OperationalRight_Metadata_DiversionType.NA + " - Not used" );
		__diversionType_JComboBox.setData(diversionTypeStrings);
		__diversionType_JComboBox.select(0);
		__diversionType_JComboBox.setEditable(false); // Force user to select choice
		__diversionType_JComboBox.setEnabled(true);
	}
	
	// Conveyance loss - always set from data file value but may not be used
	double oprLoss = opr.getOprLoss();
	if ( StateMod_Util.isMissing(oprLoss) ) {
		__conveyanceLoss_JTextField.setText("");
	}
	else {
		__conveyanceLoss_JTextField.setText("" + oprLoss);
	}
	if ( metadata.getRightTypeUsesConveyanceLoss() ) {
		__conveyanceLoss_JTextField.setEditable(editable);
		__conveyanceLoss_JTextField.setEnabled(true);
	}
	else {
		__conveyanceLoss_JTextField.setEditable(false);
		__conveyanceLoss_JTextField.setEnabled(false);
	}
	__conveyanceLoss_JTextField.setToolTipText(metadata.getConveyanceLossNotes());
	
	// Limits - always show file value but may be disabled if not used

	double oprLimit = opr.getOprLimit();
	if ( StateMod_Util.isMissing(oprLimit) ) {
		__limits_JTextField.setText("");
	}
	else {
		__limits_JTextField.setText("" + oprLimit);
	}
	if ( metadata.getRightTypeUsesLimits() ) {
		__limits_JTextField.setToolTipText(metadata.getLimitsNotes());
		__limits_JTextField.setEditable(editable);
		__limits_JTextField.setEnabled(true);
	}
	else {
		__limits_JTextField.setText("");
		__limits_JTextField.setToolTipText("");
		__limits_JTextField.setEditable(false);
		__limits_JTextField.setEnabled(false);
	}
	
	// First year of operation
	int ioBeg = opr.getIoBeg();
	if ( StateMod_Util.isMissing(ioBeg) ) {
		__firstYear_JTextField.setText("");
	}
	else {
		__firstYear_JTextField.setText("" + ioBeg);
	}
	__firstYear_JTextField.setEditable(editable);
	
	// Last year of operation
	int ioEnd = opr.getIoEnd();
	if ( StateMod_Util.isMissing(ioEnd) ) {
		__lastYear_JTextField.setText("");
	}
	else {
		__lastYear_JTextField.setText("" + ioEnd);
	}
	__lastYear_JTextField.setEditable(editable);
}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightComments ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	// Always enabled
	// Set up the text editor text
	StringBuffer b = new StringBuffer();
	String nl = "\n"; // As per java doc this should be used in memory
	for ( String s : opr.getCommentsBeforeData() ) {
		b.append(s + nl);
	}
	__comments_JTextArea.setText(b.toString());
	__comments_JPanel.setVisible(true);
	__comments_JTextArea.setEditable(editable);
}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightDestination ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__destination_JPanel.setVisible(false);
		__destination_JPanel.setEnabled(false);
	}
	else {
		if ( metadata.getRightTypeUsesDestination() ) {
			StateMod_OperationalRight_Metadata_SourceOrDestinationType []
			    allowedDestinationTypes = metadata.getDestinationTypes();
			StateMod_DataSet dataset = getDataSet();
			List<String> destIDStrings = new Vector<String>();
			// Always allow blank...
			destIDStrings.add("");
			destIDStrings.addAll ( StateMod_Util.createIdentifierList(
				dataset, allowedDestinationTypes, true ) );
			__destination_JComboBox.setData(destIDStrings);
			__destination_JPanel.setVisible(true);
			__destination_JPanel.setEnabled(true);
			__destination_JComboBox.setEnabled(true);
			__destination_JComboBox.setEditable(false); // Force user to select choice
			__destinationAccount_JComboBox.setEnabled(true);
			__destinationAccount_JComboBox.setEditable(false); // Force user to select choice
			// Select the matching type and if not in the data set add it
			String ciopde = opr.getCiopde();
			try {
				JGUIUtil.selectTokenMatches(__destination_JComboBox,
					true, // Ignore case
					" ",
					0, // No special parse flag
					0, // match first token
					ciopde, // Match the destination
					null, // No default
					true); // Trim tokens before comparing
			}
			catch ( Exception e ) {
				// If here the data includes destination not in the data set so add to the list
				String choice = ciopde + " - NOT MATCHED IN DATA SET!";
				__destination_JComboBox.add ( choice );
				__destination_JComboBox.select ( choice );
			}
			// Now populate the destination account matching the selected destination
			populateOperationalRightDestinationAccount(opr, __destination_JComboBox.getSelected(),editable);
		}
		else {
			__destination_JPanel.setVisible(false);
		}
		if ( metadata.getRightTypeUsesSpecialDestinationAccount() ) {
			// Special case
			populateOperationalRightDestinationAccountSpecial(opr, editable);
		}
	}
}

/**
Fills the destination account field based on the value in the destination field.
This method is only going to be called when a destination is actually used.
@param opr the current operational right
@param destinationSelection the value in the destination field
*/
private void populateOperationalRightDestinationAccount (
	StateMod_OperationalRight opr, String destinationSelection, boolean editable )
{   String destinationID = getFirstToken(destinationSelection);
	// The only time that accounts are other than 1 is when a reservoir
	__destinationAccount_JComboBox.removeAllItems();
	if ( destinationSelection.indexOf(
		"(" + StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR + ")") > 0 ) {
		// Get the accounts for the reservoir
		StateMod_DataSet dataset = getDataSet();
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> reservoirList =
			(List<StateMod_Reservoir>)dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
		int pos = StateMod_Util.indexOf(reservoirList,destinationID);
		List<String> accountChoices = new Vector<String>();
		if ( pos >= 0 ) {
			StateMod_Reservoir res = reservoirList.get(pos);
			List<String> positiveAccounts = StateMod_Util.createIdentifierListFromStateModData(res.getAccounts(), true, null);
			accountChoices.addAll(positiveAccounts);
			if ( opr.getMetadata().getRightTypeUsesNegativeDestinationAccounts() ) {
				// Add the accounts with negatives...
				for ( String positiveAccount : positiveAccounts ) {
					accountChoices.add( "-" + positiveAccount +
						" - fill up to this account based on ratio" );
				}
			}
			__destinationAccount_JComboBox.setData(accountChoices);
			// Now select
			String iopdes = opr.getIopdes();
			try {
				JGUIUtil.selectTokenMatches(__destinationAccount_JComboBox,
					true, // Ignore case
					" ",
					0, // No special parse flag
					0, // match first token
					iopdes, // Match the destination account
					null, // No default
					true); // Trim tokens before comparing
			}
			catch ( Exception e ) {
				// If here the data includes account not in the data set so add to the list
				String choice = iopdes + " - NOT MATCHED IN DATA SET";
				__destinationAccount_JComboBox.add ( choice );
				__destinationAccount_JComboBox.select ( choice );
			}
		}
	}
	else {
		List<String> accountChoices = new Vector<String>();
		accountChoices.add("0 - Not used");
		accountChoices.add("1 - Default account is used");
		__destinationAccount_JComboBox.setData(accountChoices);
		// Now select
		String iopdes = opr.getIopdes();
		try {
			JGUIUtil.selectTokenMatches(__destinationAccount_JComboBox,
				true, // Ignore case
				" ",
				0, // No special parse flag
				0, // match first token
				iopdes, // Match the destination account
				null, // No default
				true); // Trim tokens before comparing
		}
		catch ( Exception e ) {
			// If here the data includes account not in the data set so add to the list
			String choice = iopdes + " - NOT MATCHED IN DATA SET";
			__destinationAccount_JComboBox.add ( choice );
			__destinationAccount_JComboBox.select ( choice );
		}
	}
	__destinationAccount_JComboBox.setEditable(false); // Force user to select choice
	__destinationAccount_JComboBox.setEnabled(true);
}

/**
Fills the destination account field.
This method is only going to be called when the destination account is actually used.
@param opr the current operational right
*/
private void populateOperationalRightDestinationAccountSpecial ( StateMod_OperationalRight opr, boolean editable )
{
	__destinationAccount_JComboBox.removeAllItems();
	List<String>specialChoices = opr.getMetadata().getDestinationAccountSpecialChoices ();
	__destinationAccount_JComboBox.setData(specialChoices);
	// Now select
	String ciopde = opr.getCiopde();
	try {
		JGUIUtil.selectTokenMatches(__destinationAccount_JComboBox,
			true, // Ignore case
			" ",
			0, // No special parse flag
			0, // match first token
			ciopde, // Match the source account
			null, // No default
			true); // Trim tokens before comparing
	}
	catch ( Exception e ) {
		// If here the data includes account not in the data set so add to the list
		String choice = ciopde + " - UNKNOWN VALUE";
		__destinationAccount_JComboBox.add ( choice );
		__destinationAccount_JComboBox.select ( choice );
	}
	__destinationAccount_JComboBox.setEnabled(true);
	__destinationAccount_JComboBox.setEditable(false); // Force user to select choice
}

/**
Populate operational right for the intervening structures (without loss) data panel.
*/
private void populateOperationalRightInterveningStructuresWithLoss ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__interveningStructuresWithLoss_JPanel.setVisible(false);
		__interveningStructuresWithLoss_JPanel.setEnabled(false);
	}
	else {
		String oprLoss = __conveyanceLoss_JTextField.getText();
		double oprLossDouble = StateMod_Util.MISSING_DOUBLE;
		if ( StringUtil.isDouble(oprLoss) ) {
			oprLossDouble = Double.valueOf(oprLoss);
		}
		if ( metadata.getRightTypeUsesInterveningStructuresWithLoss(oprLossDouble) ) {
			__interveningStructuresWithLoss_JPanel.setVisible(true);
			__interveningStructuresWithLoss_JPanel.setEnabled(true);
			StateMod_DataSet dataset = getDataSet();
			/*
			From Kara Sobiesky at Rice on 2011-02-03...
			In regards to your questions, diversion structures (including D and DW
			structures) and 'other' structures (O structures) can be used as intervening
			structures or carriers.  They can be pretty much anywhere in the network
			(not constrained by upstream/downstream location).  Reservoir structures,
			instream flow structures and gages cannot be used as intervening structures.
			I can't think of any operating rule that anything else can be used as an
			intervening structure.  I ran this past Erin too, so you have two votes of
			confidence on this issue. 
			 */
			StateMod_OperationalRight_Metadata_SourceOrDestinationType [] structureTypes =
				{ StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION, 
				StateMod_OperationalRight_Metadata_SourceOrDestinationType.OTHER };
			for ( int i = 0; i < StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES; i++ ) {
				List<String> structureIDStrings = new Vector<String>();
				// Always allow blank...
				structureIDStrings.add("");
				structureIDStrings.addAll ( StateMod_Util.createIdentifierList( dataset, structureTypes, true ) );
				__interveningStructuresWithLoss_JComboBox[i].setData(structureIDStrings);
				__interveningStructuresWithLoss_JComboBox[i].setEnabled(true);
				__interveningStructuresWithLoss_JComboBox[i].setEditable(false); // Force user to select choice
				// Select the matching type and if not in the data set add it
				String intern = opr.getIntern(i);
				if ( intern.equals("") ) {
					__interveningStructuresWithLoss_JComboBox[i].select ( "" );
				}
				else {
					try {
						JGUIUtil.selectTokenMatches(__interveningStructuresWithLoss_JComboBox[i],
							true, // Ignore case
							" ",
							0, // No special parse flag
							0, // match first token
							intern, // Match the destination
							null, // No default
							true); // Trim tokens before comparing
					}
					catch ( Exception e ) {
						// If here the data includes destination not in the data set so add to the list
						String choice = intern + " - NOT MATCHED IN DATA SET!";
						__interveningStructuresWithLoss_JComboBox[i].add ( choice );
						__interveningStructuresWithLoss_JComboBox[i].select ( choice );
					}
				}
				// Percent
				double oprLossC = opr.getOprLossC(i);
				if ( StateMod_Util.isMissing(oprLossC) ) {
					__interveningStructuresWithLossPercent_JTextField[i].setText("");
				}
				else {
					__interveningStructuresWithLossPercent_JTextField[i].setText("" + oprLossC );
				}
				// Type...
				List<String> typeStrings = new Vector<String>();
				// TODO SAM 2011-02-05 Allow blank?
				typeStrings.add("");
				typeStrings.add("Carrier");
				typeStrings.add("Return");
				__interveningStructuresWithLossType_JComboBox[i].setData(typeStrings);
				__interveningStructuresWithLossType_JComboBox[i].setEnabled(true);
				__interveningStructuresWithLossType_JComboBox[i].setEditable(false); // Force user to select choice
				// Select the matching type and if not in the data set add it
				String internT = opr.getInternT(i);
				if ( internT.equals("") ) {
					__interveningStructuresWithLoss_JComboBox[i].select ( "" );
				}
				else {
					try {
						JGUIUtil.selectTokenMatches(__interveningStructuresWithLossType_JComboBox[i],
							true, // Ignore case
							" ",
							0, // No special parse flag
							0, // match first token
							internT, // Match the destination
							null, // No default
							true); // Trim tokens before comparing
					}
					catch ( Exception e ) {
						// If here the data includes destination not in the data set so add to the list
						String choice = internT + " - NOT MATCHED IN DATA SET!";
						__interveningStructuresWithLossType_JComboBox[i].add ( choice );
						__interveningStructuresWithLossType_JComboBox[i].select ( choice );
					}
				}
			}
		}
		else {
			__interveningStructuresWithLoss_JPanel.setVisible(false);
		}
	}
}

/**
Populate operational right for the intervening structures (without loss) data panel.
*/
private void populateOperationalRightInterveningStructuresWithoutLoss ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__interveningStructuresWithoutLoss_JPanel.setVisible(false);
		__interveningStructuresWithoutLoss_JPanel.setEnabled(false);
	}
	else {
		if ( metadata.getRightTypeUsesInterveningStructuresWithoutLoss() ) {
			__interveningStructuresWithoutLoss_JPanel.setVisible(true);
			__interveningStructuresWithoutLoss_JPanel.setEnabled(true);
			StateMod_DataSet dataset = getDataSet();
			List<String> structureIDStrings = new Vector<String>();
			// Always allow blank...
			structureIDStrings.add("");
			/*
			From Kara Sobiesky at Rice on 2011-02-03...
			In regards to your questions, diversion structures (including D and DW
			structures) and 'other' structures (O structures) can be used as intervening
			structures or carriers.  They can be pretty much anywhere in the network
			(not constrained by upstream/downstream location).  Reservoir structures,
			instream flow structures and gages cannot be used as intervening structures.
			I can't think of any operating rule that anything else can be used as an
			intervening structure.  I ran this past Erin too, so you have two votes of
			confidence on this issue.
			
			From StateMod doc...
			Exception is Type 41, which uses out of priority plans
			*/
			StateMod_OperationalRight_Metadata_SourceOrDestinationType [] structureTypes =
			{ StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION, 
			StateMod_OperationalRight_Metadata_SourceOrDestinationType.OTHER };
			if ( opr.getItyopr() == 41 ) {
				structureTypes = new StateMod_OperationalRight_Metadata_SourceOrDestinationType[1];
				structureTypes[0] = StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_OUT_OF_PRIORITY;
			}
			structureIDStrings.addAll ( StateMod_Util.createIdentifierList( dataset, structureTypes, true ) );
			for ( int i = 0; i < StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES; i++ ) {
				__interveningStructuresWithoutLoss_JComboBox[i].setData(structureIDStrings);
				__interveningStructuresWithoutLoss_JComboBox[i].setEnabled(true);
				__interveningStructuresWithoutLoss_JComboBox[i].setEditable(false); // Force user to select choice
				// Select the matching type and if not in the data set add it
				String intern = opr.getIntern(i);
				if ( intern.equals("") ) {
					__interveningStructuresWithoutLoss_JComboBox[i].select ( "" );
				}
				else {
					try {
						JGUIUtil.selectTokenMatches(__interveningStructuresWithoutLoss_JComboBox[i],
							true, // Ignore case
							" ",
							0, // No special parse flag
							0, // match first token
							intern, // Match the destination
							null, // No default
							true); // Trim tokens before comparing
					}
					catch ( Exception e ) {
						// If here the data includes destination not in the data set so add to the list
						String choice = intern + " - NOT MATCHED IN DATA SET!";
						__interveningStructuresWithoutLoss_JComboBox[i].add ( choice );
						__interveningStructuresWithoutLoss_JComboBox[i].select ( choice );
					}
				}
			}
		}
		else {
			__interveningStructuresWithoutLoss_JPanel.setVisible(false);
		}
	}
}

/**
Populate operational right for the monthly operating limits data panel.
*/
private void populateOperationalRightMonthlyOprEff ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__monthlyOprEff_JPanel.setVisible(false);
		__monthlyOprEff_JPanel.setEnabled(false);
	}
	else {
		String source2 = getDataString ( __source1_JComboBox );
		String sourceAccount2 = getDataString ( __sourceAccount1_JComboBox );
		if ( metadata.getRightTypeUsesMonthlyOprEff(__dataset, source2, sourceAccount2) ) {
			for ( int i = 0; i < 13; i++ ) {
				if ( StateMod_Util.isMissing(opr.getOprMax(i))) {
					__monthlyOprEff_JTextField[i].setText("");
				}
				else {
					__monthlyOprEff_JTextField[i].setText("" + opr.getOprMax(i));
				}
				__monthlyOprEff_JTextField[i].setEnabled(true);
				__monthlyOprEff_JTextField[i].setEditable(true);
			}
			__monthlyOprEff_JPanel.setVisible(true);
			__monthlyOprEff_JPanel.setEnabled(true);
		}
		else {
			__monthlyOprEff_JPanel.setVisible(false);
			__monthlyOprEff_JPanel.setEnabled(false);
		}
	}
}

/**
Populate operational right for the monthly operating limits data panel.
*/
private void populateOperationalRightMonthlyOprMax ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__monthlyOprMax_JPanel.setVisible(false);
		__monthlyOprMax_JPanel.setEnabled(false);
	}
	else {
		// TODO SAM 2011-02-06 Is this a performance hit?
		((TitledBorder)__monthlyOprMax_JPanel.getBorder()).setTitle(
			metadata.getMonthlyLimitsTitle());
		String oprLimitString = __limits_JTextField.getText();
		double oprLimit = StateMod_Util.MISSING_DOUBLE;
		if ( StringUtil.isDouble(oprLimitString) ) {
			oprLimit = Double.valueOf(oprLimitString);
		}
		if ( metadata.getRightTypeUsesMonthlyOprMax(oprLimit) ) {
			for ( int i = 0; i < 13; i++ ) {
				if ( StateMod_Util.isMissing(opr.getOprMax(i))) {
					__monthlyOprMax_JTextField[i].setText("");
				}
				else {
					__monthlyOprMax_JTextField[i].setText("" + opr.getOprMax(i));
				}
				__monthlyOprMax_JTextField[i].setEnabled(true);
				__monthlyOprMax_JTextField[i].setEditable(editable);
			}
			__monthlyOprMax_JPanel.setVisible(true);
			__monthlyOprMax_JPanel.setEnabled(true);
		}
		else {
			__monthlyOprMax_JPanel.setVisible(false);
			__monthlyOprMax_JPanel.setEnabled(false);
		}
	}
}

/**
Populate operational right for the month switch data panel.
*/
private void populateOperationalRightMonthSwitch ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__monthlySwitch_JPanel.setVisible(false);
		__monthlySwitch_JPanel.setEnabled(false);
	}
	else {
		if ( metadata.getRightTypeUsesMonthlySwitch() ) {
			// Clear the monthly switches and repopulate with valid choices
			List<String> choices = new Vector<String>();
			choices.add("" );
			choices.add("0 - Off for month" );
			choices.add("1 - On for month" );
			for ( int i = 2; i <= 31; i++ ) {
				choices.add( "" + i + " - First day used" );
			}
			for ( int i = -1; i >= -31; i-- ) {
				choices.add( "" + i + " - Last day used" );
			}
			for ( int i = 0; i < 12; i++ ) {
				__monthSwitch_JComboBox[i].removeAllItems();
				__monthSwitch_JComboBox[i].setData(choices);
				__monthSwitch_JComboBox[i].setEnabled(true);
				__monthSwitch_JComboBox[i].setEditable(false); // User must select from options
				// Try to select
				try {
					if ( StateMod_Util.isMissing(opr.getImonsw(i)) ) {
						__monthSwitch_JComboBox[i].select("");
					}
					else {
						JGUIUtil.selectTokenMatches(__monthSwitch_JComboBox[i],
							true, // Ignore case
							" ",
							0, // No special parse flag
							0, // match first token
							"" + opr.getImonsw(i), // Match the month
							null, // No default
							true); // Trim tokens before comparing
					}
				}
				catch ( Exception e ) {
					// If here the data includes switch not in the data set so add to the list
					String choice = "" + opr.getImonsw(i) + " - UNKNOWN";
					__monthSwitch_JComboBox[i].add ( choice );
					__monthSwitch_JComboBox[i].select ( choice );
				}
			}
			__monthlySwitch_JPanel.setVisible(true);
			__monthlySwitch_JPanel.setEnabled(true);
		}
		else {
			__monthlySwitch_JPanel.setVisible(false);
			__monthlySwitch_JPanel.setEnabled(false);
		}
	}
}

/**
Populate operational right for the Rio Grande data panel.
*/
private void populateOperationalRightRioGrande ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{	int ityopr = opr.getItyopr();
	if ( useTextEditor || ((ityopr != 17) && (ityopr != 18)) ) {
		__rioGrande_JPanel.setVisible(false);
		__rioGrande_JPanel.setEnabled(false);
	}
	else {
		__rioGrande_JPanel.setVisible(true);
		__rioGrande_JPanel.setEnabled(true);
		double qdebt = opr.getQdebt(); // FIXME SAM 2011-02-05 Need to treat qdebt as int in opr?
		if ( StateMod_Util.isMissing(qdebt)) {
			__qdebt_JTextField.setText("");
		}
		else {
			__qdebt_JTextField.setText("" + (int)(qdebt + .1));
		}
		double qdebtx = opr.getQdebtx();
		if ( StateMod_Util.isMissing(qdebt)) {
			__qdebtx_JTextField.setText("");
		}
		else {
			__qdebtx_JTextField.setText("" + qdebtx);
		}
		if ( ityopr == 17 ) {
			// Not used
			List<String> gageList = new Vector<String>();
			gageList.add("");
			__rioGrandeIndexGage_JComboBox.setData(gageList);
			__rioGrandeIndexGage_JComboBox.select(0);
			__rioGrandeIndexGage_JComboBox.setEnabled(false);
			__rioGrandeIndexGage_JComboBox.setEditable(false); // Force user to select
		}
		else if ( ityopr == 18 ) {
			String ciopso3 = opr.getCiopso3();
			StateMod_OperationalRight_Metadata_SourceOrDestinationType []
			    allowedSourceTypes = { StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE };
			StateMod_DataSet dataset = getDataSet();
			List<String> indexGageIDStrings = new Vector<String>();
			// Now add identifier choices...
			indexGageIDStrings.addAll ( StateMod_Util.createIdentifierList(
				dataset, allowedSourceTypes, true ) );
			__rioGrandeIndexGage_JComboBox.setEnabled(true);
			__rioGrandeIndexGage_JComboBox.setEditable(false); // Force user to select
			__rioGrandeIndexGage_JComboBox.setData(indexGageIDStrings);
			// Select the matching source 1 in the data set
			try {
				JGUIUtil.selectTokenMatches(__rioGrandeIndexGage_JComboBox,
					true, // Ignore case
					" ",
					0, // No special parse flag
					0, // match first token
					ciopso3, // Match the index gage
					null, // No default
					true); // Trim tokens before comparing
			}
			catch ( Exception e ) {
				// If here the data includes gage not in the data set so add to the list
				String choice = ciopso3 + " - NOT MATCHED IN DATA SET";
				__rioGrandeIndexGage_JComboBox.add ( choice );
				__rioGrandeIndexGage_JComboBox.select ( choice );
			}
		}
		// Coefficient is always just the coefficient
		String iopsou3 = opr.getIopsou3();
		if ( StateMod_Util.isMissing(iopsou3) ) {
			// Default to 1.0 - new right?
			__rioGrandeCoefficient_JTextField.setText("1.0");
		}
		else {
			__rioGrandeCoefficient_JTextField.setText(iopsou3);
		}
		// Basin should be set to default if blank...
		String ciopso4 = opr.getCiopso4();
		if ( StateMod_Util.isMissing(ciopso4) ) {
			// Default to ClosedBasin - new right?
			__rioGrandeBasin_JTextField.setText("ClosedBasin");
		}
		else {
			__rioGrandeBasin_JTextField.setText(ciopso4);
		}
		// Basin yield...
		String iopsou4 = opr.getIopsou4();
		__rioGrandeBasinYield_JTextField.setText(iopsou4);
		// Drain should be set to default if blank...
		String ciopso5 = opr.getCiopso5();
		if ( StateMod_Util.isMissing(ciopso5) ) {
			// Default to NortonDrnS - new right?
			__rioGrandeDrain_JTextField.setText("NortonDrnS");
		}
		else {
			__rioGrandeDrain_JTextField.setText(ciopso5);
		}
		// Basin yield...
		String iopsou5 = opr.getIopsou5();
		__rioGrandeDrainYield_JTextField.setText(iopsou5);
	}
}

/**
Populate operational right for the San Juan data panel.
*/
private void populateOperationalRightSanJuan ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{	int ityopr = opr.getItyopr();
	if ( useTextEditor || (ityopr != 20) ) {
		__sanJuan_JPanel.setVisible(false);
		__sanJuan_JPanel.setEnabled(false);
	}
	else {
		__sanJuan_JPanel.setVisible(true);
		__sanJuan_JPanel.setEnabled(true);
		double sjmina = opr.getSjmina();
		if ( StateMod_Util.isMissing(sjmina)) {
			__sanJuanSjmina_JTextField.setText("");
		}
		else {
			__sanJuanSjmina_JTextField.setText("" + sjmina );
		}
		double sjrela = opr.getSjrela();
		if ( StateMod_Util.isMissing(sjmina)) {
			__sanJuanSjrela_JTextField.setText("");
		}
		else {
			__sanJuanSjrela_JTextField.setText("" + sjrela);
		}
	}
}

/**
Populate operational right for the source data panel.
*/
private void populateOperationalRightSource ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		__source_JPanel.setVisible(false);
		__source_JPanel.setEnabled(false);
	}
	else {
		boolean rightTypeUsesSource1 = metadata.getRightTypeUsesSource1();
		boolean rightTypeUsesSource2 = metadata.getRightTypeUsesSource2();
		boolean rightTypeUsesSpecialSourceAccount1 = metadata.getRightTypeUsesSpecialSourceAccount1();
		boolean rightTypeUsesSpecialSourceAccount2 = metadata.getRightTypeUsesSpecialSourceAccount2();
		if ( rightTypeUsesSource1 || rightTypeUsesSource2 ||
			rightTypeUsesSpecialSourceAccount1 || rightTypeUsesSpecialSourceAccount2 ) {
			// Need to enable source panel and populate components
			__source_JPanel.setVisible(true);
			__source_JPanel.setEnabled(true);
			// Clear lists (will populate below if used)...
			__source1_JComboBox.removeAllItems();
			__source1_JComboBox.setToolTipText ( "" );
			__sourceAccount1_JComboBox.removeAllItems();
			__sourceAccount1_JComboBox.setToolTipText ( "" );
			__source2_JComboBox.removeAllItems();
			__source2_JComboBox.setToolTipText ( "" );
			__sourceAccount2_JComboBox.removeAllItems();
			__sourceAccount2_JComboBox.setToolTipText ( "" );
			if ( rightTypeUsesSource1 ) {
				String ciopso1 = opr.getCiopso1();
				StateMod_OperationalRight_Metadata_SourceOrDestinationType []
				    allowedSourceTypes = metadata.getSource1Types();
				StateMod_DataSet dataset = getDataSet();
				List<String> source1IDStrings = new Vector<String>();
				// Always allow blank...
				source1IDStrings.add("");
				// Now add identifier choices...
				source1IDStrings.addAll ( StateMod_Util.createIdentifierList(
					dataset, allowedSourceTypes, true ) );
				__source1_JComboBox.setEnabled(true);
				__source1_JComboBox.setEditable(false); // Force user to select
				__source1_JComboBox.setData(source1IDStrings);
				// Select the matching source 1 in the data set
				try {
					JGUIUtil.selectTokenMatches(__source1_JComboBox,
						true, // Ignore case
						" ",
						0, // No special parse flag
						0, // match first token
						ciopso1, // Match the source 1
						null, // No default
						true); // Trim tokens before comparing
				}
				catch ( Exception e ) {
					// If here the data includes source 1 not in the data set so add to the list
					String choice = ciopso1 + " - NOT MATCHED IN DATA SET";
					__source1_JComboBox.add ( choice );
					__source1_JComboBox.select ( choice );
				}
				// Populate the appropriate account for source 1...
				populateOperationalRightSourceAccount1(opr,editable);
			}
			else {
				// Default and disable
				List<String> source1IDStrings = new Vector<String>();
				source1IDStrings.add("0 - Not used");
				__source1_JComboBox.setData(source1IDStrings);
				__source1_JComboBox.select ( 0 );
				__source1_JComboBox.setEnabled(false);
				__source1_JComboBox.setEditable(false); // Force user to select
				populateOperationalRightSourceAccount1(opr,editable);
			}
			
			// Source 2...
			
			if ( rightTypeUsesSource2 ) {
				String source1Selection = __source1_JComboBox.getSelected();
				if ( source1Selection.indexOf(
					"(" + StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_RECHARGE+ ")") > 0 ) {
					// Expect a reservoir ID, for example for Type 49
					StateMod_DataSet dataset = getDataSet();
					@SuppressWarnings("unchecked")
					List<StateMod_Reservoir> reservoirList =
						(List<StateMod_Reservoir>)dataset.getComponentForComponentType(
						StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
					List<String> reservoirChoices = StateMod_Util.createIdentifierListFromStateModData(reservoirList, true,null);
					__source2_JComboBox.setEnabled(true);
					__source2_JComboBox.setEditable(false); // Force user to select
					__source2_JComboBox.setData(reservoirChoices);
					// Now select
					String ciopso2 = opr.getCiopso2();
					try {
						JGUIUtil.selectTokenMatches(__sourceAccount2_JComboBox,
							true, // Ignore case
							" ",
							0, // No special parse flag
							0, // match first token
							ciopso2, // Match the source 2
							null, // No default
							true); // Trim tokens before comparing
					}
					catch ( Exception e ) {
						// If here the data includes account not in the data set so add to the list
						String choice = ciopso2 + " - NOT MATCHED IN DATA SET";
						__source2_JComboBox.add ( choice );
						__source2_JComboBox.select ( choice );
					}
				}
				else {
					// For most cases rely on the metadata to configure the choice...
					String ciopso2 = opr.getCiopso2();
					StateMod_OperationalRight_Metadata_SourceOrDestinationType []
					    allowedSourceTypes = metadata.getSource2Types();
					StateMod_DataSet dataset = getDataSet();
					List<String> source2IDStrings = new Vector<String>();
					// Always allow blank...
					source2IDStrings.add("");
					// Add special choices...
					if ( metadata.getRightTypeUsesSpecialSource2() ) {
						source2IDStrings.addAll ( metadata.getSource2SpecialChoices() );
					}
					source2IDStrings.addAll ( StateMod_Util.createIdentifierList(
						dataset, allowedSourceTypes, true ) );
					__source2_JComboBox.setEnabled(true);
					__source2_JComboBox.setEditable(false); // Force user to select
					__source2_JComboBox.setData(source2IDStrings);
					// Select the matching source 2 in the data set
					try {
						JGUIUtil.selectTokenMatches(__source2_JComboBox,
							true, // Ignore case
							"-",
							0, // No special parse flag
							0, // match first token
							ciopso2, // Match the source 2
							null, // No default
							true); // Trim tokens before comparing
					}
					catch ( Exception e ) {
						// If here the data includes source 2 not in the data set so add to the list
						String choice = ciopso2 + " - NOT MATCHED IN DATA SET";
						__source2_JComboBox.add ( choice );
						__source2_JComboBox.select ( choice );
					}
					// Populate the appropriate account for source 1...
					populateOperationalRightSourceAccount2(opr,editable);
				}
			}
			else {
				// Default and disable
				List<String> source2IDStrings = new Vector<String>();
				source2IDStrings.add("0 - Not used");
				__source2_JComboBox.setData(source2IDStrings);
				__source2_JComboBox.select ( 0 );
				__source2_JComboBox.setEnabled(false);
				__source2_JComboBox.setEditable(false); // Force user to select
				populateOperationalRightSourceAccount2(opr,editable);
			}
		}
		else {
			__source_JPanel.setVisible(false);
			__source_JPanel.setEnabled(false);
		}
	}
}

/**
Fills the source 1 account field based on the value in the source 1 field.
This method is only going to be called when source 1 is actually used.
@param opr the current operational right
@param editable whether the data should be editable
*/
private void populateOperationalRightSourceAccount1 ( StateMod_OperationalRight opr, boolean editable )
{	String source1Selection = __source1_JComboBox.getSelected();
	String source1ID = getFirstToken(source1Selection);
	// The only time that accounts are other than 1 is when a reservoir
	__sourceAccount1_JComboBox.removeAllItems();
	__sourceAccount1_JComboBox.setEnabled(true);
	__sourceAccount1_JComboBox.setEditable(false); // Force user to select
	StateMod_OperationalRight_Metadata metadata = opr.getMetadata();
	if ( source1Selection.indexOf(
		"(" + StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR + ")") > 0 ) {
		// Get the accounts for the reservoir
		StateMod_DataSet dataset = getDataSet();
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> reservoirList =
			(List<StateMod_Reservoir>)dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
		int pos = StateMod_Util.indexOf(reservoirList,source1ID);
		List<String> accountChoices = new Vector<String>();
		if ( pos >= 0 ) {
			// Add special choices...
			if ( metadata.getRightTypeUsesSpecialSourceAccount1() ) {
				accountChoices.addAll ( metadata.getSourceAccount1SpecialChoices() );
			}
			StateMod_Reservoir res = reservoirList.get(pos);
			// Add identifiers...
			accountChoices.addAll(StateMod_Util.createIdentifierListFromStateModData(res.getAccounts(), true, null));
			__sourceAccount1_JComboBox.setData(accountChoices);
			// Now select
			String iopsou1 = opr.getIopsou1();
			try {
				JGUIUtil.selectTokenMatches(__sourceAccount1_JComboBox,
					true, // Ignore case
					" ",
					0, // No special parse flag
					0, // match first token
					iopsou1, // Match the source account
					null, // No default
					true); // Trim tokens before comparing
			}
			catch ( Exception e ) {
				// If here the data includes account not in the data set so add to the list
				String choice = iopsou1 + " - NOT MATCHED IN DATA SET";
				__sourceAccount1_JComboBox.add ( choice );
				__sourceAccount1_JComboBox.select ( choice );
			}
		}
	}
	else {
		__sourceAccount1_JComboBox.add("0 - Not used");
		__sourceAccount1_JComboBox.select(0);
	}
	if ( metadata.getRightTypeUsesSpecialSourceAccount1() ) {
		// Special case - may totally repopulate
		populateOperationalRightSourceAccount1Special(opr, editable);
	}
}

/**
Fills the source 1 account field based on the value in the source 1 field.
This method is only going to be called when source 1 is actually used.
@param opr the current operational right
*/
private void populateOperationalRightSourceAccount1Special ( StateMod_OperationalRight opr, boolean editable )
{
	__sourceAccount1_JComboBox.removeAllItems();
	List<String>specialChoices = opr.getMetadata().getSourceAccount1SpecialChoices ();
	__sourceAccount1_JComboBox.setData(specialChoices);
	// Now select
	String iopsou1 = opr.getIopsou1();
	try {
		JGUIUtil.selectTokenMatches(__sourceAccount1_JComboBox,
			true, // Ignore case
			" ",
			0, // No special parse flag
			0, // match first token
			iopsou1, // Match the source account
			null, // No default
			true); // Trim tokens before comparing
	}
	catch ( Exception e ) {
		// If here the data includes account not in the data set so add to the list
		String choice = iopsou1 + " - UNKNOWN VALUE";
		__sourceAccount1_JComboBox.add ( choice );
		__sourceAccount1_JComboBox.select ( choice );
	}
	__sourceAccount1_JComboBox.setEnabled(true);
	__sourceAccount1_JComboBox.setEditable(false); // Force user to select
}

/**
Fills the source 2 account field based on the value in the source 2 field.
This method is only going to be called when source 2 is actually used.
@param opr the current operational right
@param source2Selection the value in the source 1 field
*/
private void populateOperationalRightSourceAccount2 ( StateMod_OperationalRight opr, boolean editable )
{	String source2Selection = __source2_JComboBox.getSelected();
	String source2ID = getFirstToken(source2Selection);
	// The only time that accounts are other than 0 is when a reservoir
	__sourceAccount2_JComboBox.removeAllItems();
	__sourceAccount2_JComboBox.setEditable(false); // Force user to select
	__sourceAccount2_JComboBox.setEnabled(true);
	StateMod_OperationalRight_Metadata metadata = opr.getMetadata();
	if ( source2Selection.indexOf(
		"(" + StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR + ")") > 0 ) {
		// Get the accounts for the reservoir
		StateMod_DataSet dataset = getDataSet();
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> reservoirList =
			(List<StateMod_Reservoir>)dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
		int pos = StateMod_Util.indexOf(reservoirList,source2ID);
		List<String> accountChoices = new Vector<String>();
		if ( pos >= 0 ) {
			StateMod_Reservoir res = reservoirList.get(pos);
			accountChoices.addAll(StateMod_Util.createIdentifierListFromStateModData(res.getAccounts(), true, null));
			__sourceAccount2_JComboBox.setData(accountChoices);
			// Now select
			String iopsou2 = opr.getIopsou2();
			try {
				JGUIUtil.selectTokenMatches(__sourceAccount2_JComboBox,
					true, // Ignore case
					" ",
					0, // No special parse flag
					0, // match first token
					iopsou2, // Match the source account
					null, // No default
					true); // Trim tokens before comparing
			}
			catch ( Exception e ) {
				// If here the data includes account not in the data set so add to the list
				String choice = iopsou2 + " - NOT MATCHED IN DATA SET";
				__sourceAccount2_JComboBox.add ( choice );
				__sourceAccount2_JComboBox.select ( choice );
			}
		}
	}
	else {
		__sourceAccount2_JComboBox.add("0 - Not used");
		__sourceAccount2_JComboBox.select(0);
		__sourceAccount2_JComboBox.setEditable(false); // Force user to select
		__sourceAccount2_JComboBox.setEnabled(false);
	}
	if ( metadata.getRightTypeUsesSpecialSourceAccount2() ) {
		// Special case
		populateOperationalRightSourceAccount2Special(opr, editable);
	}
}

/**
Fills the source 2 account field based on the value in the source 2 field.
This method is only going to be called when source 2 is actually used.
@param opr the current operational right
*/
private void populateOperationalRightSourceAccount2Special ( StateMod_OperationalRight opr, boolean editable )
{
	__sourceAccount2_JComboBox.removeAllItems();
	List<String>specialChoices = opr.getMetadata().getSourceAccount2SpecialChoices ();
	__sourceAccount2_JComboBox.setData(specialChoices);
	// Now select
	String iopsou2 = opr.getIopsou2();
	try {
		JGUIUtil.selectTokenMatches(__sourceAccount2_JComboBox,
			true, // Ignore case
			" ",
			0, // No special parse flag
			0, // match first token
			iopsou2, // Match the source account
			null, // No default
			true); // Trim tokens before comparing
	}
	catch ( Exception e ) {
		// If here the data includes account not in the data set so add to the list
		String choice = iopsou2 + " - UNKNOWN VALUE";
		__sourceAccount2_JComboBox.add ( choice );
		__sourceAccount2_JComboBox.select ( choice );
	}
	__sourceAccount2_JComboBox.setEnabled(true);
	__sourceAccount2_JComboBox.setEditable(false); // Force user to select
}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightTextEditor ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor, boolean editable )
{
	if ( useTextEditor ) {
		// Set up the text editor text
		StringBuffer b = new StringBuffer();
		String nl = "\n"; // As per Java doc, this should be used in memory
		for ( String s : opr.getRightStrings() ) {
			b.append(s + nl);
		}
		__textEditor_JTextArea.setText(b.toString());
		__textEditor_JTextArea.setEditable(editable);
		__textEditor_JPanel.setVisible(true);
	}
	else {
		__textEditor_JPanel.setVisible(false);
	}
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex()) by writing the old data back to the data set component
and getting the next selection's data out of the data and displaying it on the form.
@param index the index of the reservoir to display on the form.
*/
private void processTableSelection(int index)
{
	__lastOpRightsIndex = __currentOpRightsIndex;
	__currentOpRightsIndex = __worksheet.getOriginalRowNumber(index);

	saveLastRecord();
	
	if (__worksheet.getSelectedRow() == -1) {
		JGUIUtil.disableComponents(__disables, true);
		return;
	}

	// TODO SAM 2011-01-24 Evaluate use - op rights have too many specific checks
	//JGUIUtil.enableComponents(__disables, __textUneditables, __editable);

	StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
	populateOperationalRight(opr);
	checkViewButtonState();
}

/**
Saves the prior record selected in the table; called when moving to a new record by a table selection.
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
The info is saved each time the user selects a different operational right or when the close button is pressed.
*/
private void saveInformation(int record)
{
	if (!__editable || record == -1) {
		// Do not save anything (consequently nothing will be set as dirty in the object)
		return;
	}

	if (!checkInput()) {
		return;
	}

	if ( __interveningStructuresWorksheet != null ) {
		__interveningStructuresWorksheet.stopEditing();
	}

	StateMod_OperationalRight opr = __operationalRights.get(record);
	// Comments are always set
	List<String> commentsBeforeData = new Vector<String>();
	String comments = __comments_JTextArea.getText(); // Do not trim because blank lines will be lost
	if ( comments.length() > 0 ) {
		commentsBeforeData = StringUtil.breakStringList(comments, "\n", 0);
	}
	opr.setCommentsBeforeData(commentsBeforeData);

	// The rest of the data depends on whether editing as text or not
	StateMod_OperationalRight_Metadata metadata =
		StateMod_OperationalRight_Metadata.getMetadata(opr.getItyopr());
	boolean useTextEditor = false;
	if ( (metadata == null) || !metadata.getFullEditingSupported(getDataSet()) ) {
		useTextEditor = true;
	}
	if ( useTextEditor ) {
		List<String> rightStringList = new Vector<String>();
		String textData = __textEditor_JTextArea.getText(); // Do not trim because blank lines will be lost
		if ( textData.length() > 0 ) {
			rightStringList = StringUtil.breakStringList(textData, "\n", 0);
		}
		opr.setRightStrings(rightStringList);
	}
	else {
		opr.setCommentsBeforeData(commentsBeforeData);
		opr.setName(__oprName_JTextField.getText());
		opr.setSwitch(__oprSwitch_JComboBox.getSelectedIndex());
		// FIXME SAM 2011-01-24 cgoto is not used in version 2 opr
		//opr.setCgoto(__oprAdminNumber_JTextField.getText());
	
		opr.setCiopde(getFirstToken(__destination_JComboBox.getSelected()));
		opr.setIopdes(getFirstToken(__destinationAccount_JComboBox.getSelected()));
		opr.setCiopso1(getFirstToken(__source1_JComboBox.getSelected()));
		opr.setIopsou1(getFirstToken(__sourceAccount1_JComboBox.getSelected()));
		opr.setCiopso2(getFirstToken(__source2_JComboBox.getSelected()));
		opr.setIopsou2(getFirstToken(__sourceAccount2_JComboBox.getSelected()));

		opr.setItyopr(getFirstToken(__ruleTypeSwitch_JComboBox.getSelected()));
		//int ityopr = opr.getItyopr();
	
		/* FIXME SAM 2011-01-31 Need to enable based on what data are appropriate for the right
		if (__monthSwitch_JComboBox[0].isEnabled()) {
			String value = null;
			for (int i = 0; i < 12; i++) {
				value = __monthSwitch_JComboBox[i].getSelected();
				value = getFirstToken(value);
				opr.setImonsw(i, value);
			}
		}
	
		if (__opr8Panel.isVisible()) {
		// REVISIT (JTS - 2003-09-22)
		// what goes here?
	//		opr.set??(__opr8JComboBox.getSelected().trim());
		}
		else if (__opr20Panel.isVisible()) {
			opr.setSjmina(__opr20Sjmina_JTextField.getText().trim());
			opr.setSjrela(__opr20Sjrela_JTextField.getText().trim());
		}
		else if (__qdebtPanel.isVisible()) {
			opr.setQdebt(__qdebt_JTextField.getText().trim());
			opr.setQdebtx(__qdebtx_JTextField.getText().trim());
		}
	
		if (__source3_JComboBox.isEnabled()) {
			opr.setCiopso3(getFirstToken(__source3_JComboBox.getSelected()));
			opr.setIopsou3(getFirstToken(__sourceAccount3_JComboBox.getSelected()));
			opr.setCiopso4(getFirstToken(__source4_JComboBox.getSelected()));
			opr.setIopsou4(getFirstToken(__sourceAccount4_JComboBox.getSelected()));
			opr.setCiopso5(getFirstToken(__source5_JComboBox.getSelected()));
			opr.setIopsou5(getFirstToken(__sourceAccount5_JComboBox.getSelected()));
		}
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
public void selectTableIndex(int index)
{
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
 * Set the message text field at the bottom of the window.  Currently this is used mainly
 * to indicate whether an operational right is handled not.
 */
private void setMessageText ( String text )
{
	__message_JTextField.setText ( text );
}

// FIXME SAM Need to figure out if needed in version 2 format
//private void setOriginalCgoto(StateMod_OperationalRight o, String cgoto) {
//	((StateMod_OperationalRight)o._original)._cgoto = cgoto;
//}

/**
Sets up the GUI.
@param index the index of operational right to be initially selected
*/
private void setupGUI(int index) {
	String routine = "setupGUI";

	addWindowListener(this);

	GridBagLayout gb = new GridBagLayout();
	JPanel mainJPanel = new JPanel(); // Holds left and right panels
	mainJPanel.setLayout(gb);
	JPanel leftJPanel = new JPanel (); // Left side (list and search)
	leftJPanel.setLayout(gb);
	JPanel rightJPanel = new JPanel(); // Right side (all data)
	rightJPanel.setLayout(gb);

	// Operational right list...

	JGUIUtil.addComponent(leftJPanel, setupGUI_OperationalRightList(routine),
		0, 0, 1, 5, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	
	// Search control under the main list...
	
	JGUIUtil.addComponent(leftJPanel, setupGUI_SearchPanel(routine),
		0, 5, 1, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Operational right panels for general and optional parameters...
	
	int yRight = 0;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightAttributes ( routine ),
		0, yRight, 12, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Destination on the right...
	
	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightDestination(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Sources on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightSource(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Monthly switches on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightMonthSwitch(routine),
		0, yRight, 12, 1, 0, 0,  
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Monthly operational limits on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightMonthlyOprMax(routine),
		0, yRight, 12, 1, 0, 0,  
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Monthly efficiencies on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightMonthlyOprEff(routine),
		0, yRight, 12, 1, 0, 0,  
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Associated operating rule on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightAssociatedOperatingRule(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Intervening structures (without loss) on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightInterveningStructuresWithoutLoss(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Intervening structures (with loss) on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightInterveningStructuresWithLoss(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Rio Grande on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightRioGrande(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// San Juan on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightSanJuan(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Comments for right, on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightComments(routine),
		0, yRight, 20, 1, 1.0, 1.0,  	
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	
	// Text editor on the right (make it wider than the other panels so everything is
	// not sized to it...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightTextEditor(routine),
		0, yRight, 20, 5, 1.0, 1.0,  	
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	
	// Add a blank panel to fill space, in particular when panels are disabled

	yRight += 5; // Height of the text editor
	JGUIUtil.addComponent(rightJPanel, new JPanel(),
		0, yRight, 20, 5, 1.0, 1.0,  	
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	
	// Now add the left and right panels to the main panel and add to the center of the window
	// Use a split pane to better control resizing between list and other data
	// Redraw as the pane is drawn

	JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftJPanel, rightJPanel );
	mainSplitPane.setResizeWeight(0.0); // Right side gets all resizing when window is resized
	getContentPane().add(mainSplitPane);
	/*
	JGUIUtil.addComponent(mainJPanel, leftJPanel,
		0, 0, 1, 5, 1.0, 1.0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(mainJPanel, rightJPanel,
		1, 0, 1, 5, 1.0, 1.0,
		GridBagConstraints.BOTH, GridBagConstraints.EAST);
	getContentPane().add(mainJPanel);
	*/
	
	getContentPane().add ("South", setupGUI_BottomPanel(routine, gb));	

	initializeDisables();

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_OPERATIONAL_RIGHT, this);
	}

	pack();
	// Sizes based on looking at all operational right types
	// Would like to do width of 1050 but comments seem to blow out
	setSize(1100,900);
	JGUIUtil.center(this);
	//setResizable(false);
	selectTableIndex(index);
	setVisible(true);

	// Ensure widths in the sheets
	// TODO SAM 2011-01-24 Why do this here and not in setup code?
	int [] widths = __worksheet.getCellRenderer().getColumnWidths();
	if ( widths != null) {
		__worksheet.setColumnWidths(widths);
	}
	if ( __interveningStructuresWorksheet != null ) {
		int [] widthsR = __interveningStructuresWorksheet.getCellRenderer().getColumnWidths();
		if (widthsR != null) {
			__interveningStructuresWorksheet.setColumnWidths(widthsR);
		}
	}

	__worksheet.addSortListener(this);
}

private JPanel setupGUI_Buttons ( String routine )
{

	__showOnMap_JButton = new SimpleJButton(__BUTTON_SHOW_ON_MAP, this);
	__showOnMap_JButton.setToolTipText(
		"Annotate map with location (button is disabled if layer does not have matching ID)" );
	__showOnNetwork_JButton = new SimpleJButton(__BUTTON_SHOW_ON_NETWORK, this);
	__showOnNetwork_JButton.setToolTipText( "Annotate network with location" );
	__applyJButton = new JButton(__BUTTON_APPLY);
	__applyJButton.setToolTipText( "Apply data changes" );
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__cancelJButton.setToolTipText( "Cancel data changes and close the window" );
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setToolTipText(
		"Show the operatingal rights documentation, including the decision tree for selecting the appropriate right" );
	File helpDocument = new File (getHelpDocumentName());
	if ( !helpDocument.canRead() ) {
		__helpJButton.setEnabled(false);
	}
	__closeJButton = new JButton(__BUTTON_CLOSE);
	__cancelJButton.setToolTipText( "Apply data changes and close the window" );
	
	// add bottom buttons
	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel pfinal = new JPanel();
	pfinal.setLayout(fl);
	pfinal.add(__showOnMap_JButton);
	pfinal.add(__showOnNetwork_JButton);
	if (__editable) {
		pfinal.add(__applyJButton);
		pfinal.add(__cancelJButton);
	}
	pfinal.add(__closeJButton);
	pfinal.add(__helpJButton);
	__helpJButton.addActionListener(this);
	__applyJButton.addActionListener(this);
	__cancelJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	return pfinal;
}

private JPanel setupGUI_BottomPanel ( String routine, GridBagLayout gb )
{
	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__message_JTextField = new JTextField();
	__message_JTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, setupGUI_Buttons(routine),
		0, 0, 8, 1, 1, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);	
	JGUIUtil.addComponent(bottomJPanel, __message_JTextField,
		0, 1, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__status_JTextField = new JTextField(5);
	__status_JTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __status_JTextField,
		7, 1, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	return bottomJPanel;
}

private JPanel setupGUI_OperationalRightAssociatedOperatingRule ( String routine )
{
	__associatedOperatingRule_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__associatedOperatingRule_JPanel.setLayout(gb);
	__associatedOperatingRule_JPanel.setBorder(BorderFactory.createTitledBorder("Associated operating rule"));
	int y = 0;
	JGUIUtil.addComponent(
		__associatedOperatingRule_JPanel, new JLabel("Operating rule:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__associatedOperatingRule_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__associatedOperatingRule_JComboBox.setPrototypeDisplayValue(
		" 123456789012 - (Operational Right) 123456789012345678901234 ");
	__associatedOperatingRule_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	JGUIUtil.addComponent(
		__associatedOperatingRule_JPanel, __associatedOperatingRule_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	return __associatedOperatingRule_JPanel;
}

/**
Setup the GUI components for the operational rights list.
@param routine
*/
private JPanel setupGUI_OperationalRightAttributes (String routine)
{
	__attributes_JPanel = new JPanel();	// Right side of interface for specific right data
	GridBagLayout gb = new GridBagLayout();
	__attributes_JPanel.setLayout(gb);
	__attributes_JPanel.setBorder(BorderFactory.createTitledBorder("Primary attributes"));
	int y = 0;
	int width = 3; // Labels 1, data 3 at most - this allows 2 columns of components if necessary
	__oprID_JTextField = new JTextField(12);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Operational right ID:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __oprID_JTextField,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__oprName_JTextField = new JTextField(24);
	__oprName_JTextField.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Operational right name:"),
		2, y, 1, 1, 0, 0,
		0, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __oprName_JTextField,
		3, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	y++;
	__ruleTypeSwitch_JComboBox = new SimpleJComboBox(false); // Text field not editable
	List<StateMod_OperationalRight_Metadata> metadataList =
		StateMod_OperationalRight_Metadata.getAllMetadata();
	// OK to add these here because the list will not change with data selections
	for ( int i = 0; i < metadataList.size(); i++ ) {
		__ruleTypeSwitch_JComboBox.add("" + metadataList.get(i).getRightTypeNumber() + " - " +
			metadataList.get(i).getRightTypeName() );
	}
	__ruleTypeSwitch_JComboBox.setEnabled(false);
	__ruleTypeSwitch_JComboBox.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Operational right type:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __ruleTypeSwitch_JComboBox,
		1, y, width, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__ruleTypeSwitch_JComboBox.addActionListener(this);
	y++;
	__oprAdminNumber_JTextField = new JTextField(12);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Administration number:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __oprAdminNumber_JTextField,
		1, y, 1, 1, 1, 0,
		1, 1, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__oprSwitch_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__oprSwitch_JComboBox.setPrototypeDisplayValue(" -9999 - last year off  " );
	__oprSwitch_JComboBox.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("On/off switch:"),
		2, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __oprSwitch_JComboBox,
		3, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__associatedPlan_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__associatedPlan_JComboBox.setPrototypeDisplayValue(
		" 123456789012 (Plan (Out of Priority Div. or Storage)) - 123456789012345678901234 " );
	__associatedPlan_JComboBox.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Associated plan data:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __associatedPlan_JComboBox,
		1, y, width, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__diversionType_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__diversionType_JComboBox.setPrototypeDisplayValue(" ????????? - UNKNOWN DIVERSION TYPE " );
	__diversionType_JComboBox.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Diversion type:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __diversionType_JComboBox,
		1, y, width, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__conveyanceLoss_JTextField = new JTextField(24);
	__conveyanceLoss_JTextField.setToolTipText("Blank will default to 0");
	__conveyanceLoss_JTextField.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Conveyance loss (%):"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __conveyanceLoss_JTextField,
		1, y, width, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	y++;
	__limits_JTextField = new JTextField(24);
	__limits_JTextField.setToolTipText("Blank will default to 0");
	__limits_JTextField.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Limits:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __limits_JTextField,
		1, y, width, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	y++;
	__firstYear_JTextField = new JTextField(5);
	__firstYear_JTextField.setToolTipText("Blank will default to start of run");
	__firstYear_JTextField.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("First year of operation:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __firstYear_JTextField,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__lastYear_JTextField = new JTextField(5);
	__lastYear_JTextField.setToolTipText("Blank will default to end of run");
	__lastYear_JTextField.setEditable(false);
	JGUIUtil.addComponent(
		__attributes_JPanel, new JLabel("Last year of operation:"),
		2, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__attributes_JPanel, __lastYear_JTextField,
		3, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	return __attributes_JPanel;
}

/**
Setup comments panel.
@param routine
*/
private JPanel setupGUI_OperationalRightComments (String routine)
{
	__comments_JPanel = new JPanel();
	__comments_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Comments (will be output above operational right data, with # at start of line)"));
	GridBagLayout gb = new GridBagLayout();
	__comments_JPanel.setLayout(gb);
	//__comments_JPanel.setMinimumSize(new Dimension(600,200));
	__comments_JTextArea = new JTextArea(4,100);
	// Set font smaller because its expansion sometimes adds a scroll bar that fouls up layouts
	__comments_JTextArea.setFont ( new Font("Lucida Console", Font.PLAIN, 10 ) );
	JGUIUtil.addComponent(__comments_JPanel, new JScrollPane(__comments_JTextArea),
		0, 0, 6, 1, 1, 1, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	return __comments_JPanel;
}

private JPanel setupGUI_OperationalRightDestination ( String routine )
{
	__destination_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__destination_JPanel.setLayout(gb);
	__destination_JPanel.setBorder(BorderFactory.createTitledBorder("Destination"));
	int y = 0;
	JGUIUtil.addComponent(
		__destination_JPanel, new JLabel("Destination:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__destination_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__destination_JComboBox.setPrototypeDisplayValue(
		" 123456789012 - (Instream Flow) 123456789012345678901234 ");
	__destination_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	JGUIUtil.addComponent(
		__destination_JPanel, __destination_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__destination_JPanel, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__destinationAccount_JComboBox = new SimpleJComboBox(false); // Text field not editable
	// Handle...
	// "Acc - Fill accounts based on ratio of ownership"
	__destinationAccount_JComboBox.setPrototypeDisplayValue(" 123 - 1234567890123456789012345678901234567890 ");
	__destinationAccount_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	JGUIUtil.addComponent(
		__destination_JPanel, __destinationAccount_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__destination_JComboBox.addActionListener(this);
	return __destination_JPanel;
}

/**
Set up the intervening structures (with loss) panel.
@param routine for intervening structures
@return the panel for intervening structures
*/
private JPanel setupGUI_OperationalRightInterveningStructuresWithLoss ( String routine )
{
	__interveningStructuresWithLoss_JPanel = new JPanel();
	__interveningStructuresWithLoss_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Intervening structures with loss (specify up to " +
		StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES + ")"));
	GridBagLayout gb = new GridBagLayout();
	__interveningStructuresWithLoss_JPanel.setLayout(gb);
	
	// Put each structure in a row
	int nInterveningStructues = StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES;
	for ( int i = 0; i < nInterveningStructues; i++ ) {
		__interveningStructuresWithLoss_JComboBox[i] = new SimpleJComboBox(false);
		__interveningStructuresWithLoss_JComboBox[i].setPrototypeDisplayValue(
			" 123456789012 - (Instream Flow) 123456789012345678901234 ");
		JGUIUtil.addComponent(__interveningStructuresWithLoss_JPanel, new JLabel("" + (i + 1) + " ID:"),
			0, i, 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
		JGUIUtil.addComponent(__interveningStructuresWithLoss_JPanel, __interveningStructuresWithLoss_JComboBox[i],
			1, i, 1, 1, 0, 0,
			1, 1, 1, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		// Loss (percent)...
		__interveningStructuresWithLossPercent_JTextField[i] = new JTextField(10);
		JGUIUtil.addComponent(__interveningStructuresWithLoss_JPanel, new JLabel("Loss %:"),
			2, i, 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
		JGUIUtil.addComponent(__interveningStructuresWithLoss_JPanel, __interveningStructuresWithLossPercent_JTextField[i],
			3, i, 1, 1, 1, 0,
			0, 1, 1, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		// Type...
		__interveningStructuresWithLossType_JComboBox[i] = new SimpleJComboBox(false);
		__interveningStructuresWithLossType_JComboBox[i].setPrototypeDisplayValue( " Diversion ");
		JGUIUtil.addComponent(__interveningStructuresWithLoss_JPanel, new JLabel("Type:"),
			4, i, 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
		JGUIUtil.addComponent(__interveningStructuresWithLoss_JPanel, __interveningStructuresWithLossType_JComboBox[i],
			5, i, 1, 1, 0, 0,
			1, 1, 1, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	}
	return __interveningStructuresWithLoss_JPanel;
}

/**
Set up the intervening structures (without loss) panel.
@param routine for intervening structures
@return the panel for intervening structures
*/
private JPanel setupGUI_OperationalRightInterveningStructuresWithoutLoss ( String routine )
{
	__interveningStructuresWithoutLoss_JPanel = new JPanel();
	__interveningStructuresWithoutLoss_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Intervening structures without loss (specify up to " +
		StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES + ")"));
	GridBagLayout gb = new GridBagLayout();
	__interveningStructuresWithoutLoss_JPanel.setLayout(gb);
	
	// Put in two columns to save space
	int nInterveningStructues = StateMod_OperationalRight_Metadata.MAXIMUM_INTERVENING_STRUCTURES;
	int nRows = nInterveningStructues/2;
	for ( int i = 0; i < nInterveningStructues; i++ ) {
		__interveningStructuresWithoutLoss_JComboBox[i] = new SimpleJComboBox(false);
		__interveningStructuresWithoutLoss_JComboBox[i].setPrototypeDisplayValue(
			" 123456789012 - (Instream Flow) 123456789012345678901234 ");
		JGUIUtil.addComponent(__interveningStructuresWithoutLoss_JPanel, new JLabel("" + (i + 1)),
			((i/nRows)*2), (i - (i/nRows)*nRows), 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
		JGUIUtil.addComponent(__interveningStructuresWithoutLoss_JPanel, __interveningStructuresWithoutLoss_JComboBox[i],
			((i/nRows)*2 + 1), (i - (i/nRows)*nRows), 1, 1, 0, 0,
			1, 1, 1, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	}
	return __interveningStructuresWithoutLoss_JPanel;
}

/**
Setup the GUI components for the operational rights list.
@param routine
*/
private JScrollWorksheet setupGUI_OperationalRightList(String routine)
{
	PropList p = new PropList("StateMod_OperationalRight_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	JScrollWorksheet jsw = null;
	try {
		StateMod_OperationalRight_TableModel tmo = new
			StateMod_OperationalRight_TableModel( __operationalRights, __editable);
		StateMod_OperationalRight_CellRenderer cro = new StateMod_OperationalRight_CellRenderer(tmo);
	
		jsw = new JScrollWorksheet(cro, tmo, p);
		__worksheet = jsw.getJWorksheet();
		// Don't want structure ID to be visible ( this is used for intervening structures)
		__worksheet.removeColumn(StateMod_OperationalRight_TableModel.COL_STRUCTURE_ID);
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building operational rights list.");
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);
	//jsw.setPreferredSize(new Dimension(300,600));
	return jsw;
}

/**
Set up the monthly efficiency panel.
@param routine for logging
@return the panel for monthly efficiencies
*/
private JPanel setupGUI_OperationalRightMonthlyOprEff ( String routine )
{
	__monthlyOprEff_JPanel = new JPanel();
	__monthlyOprEff_JPanel.setBorder(BorderFactory.createTitledBorder(
		"T&C consumptive use factors (%)"));
	GridBagLayout gb = new GridBagLayout();
	__monthlyOprEff_JPanel.setLayout(gb);
	
	int [] monthsCyr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	int [] monthsWyr = { 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	int [] monthsIyr = { 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	int [] months = monthsCyr;
	if ( __dataset.getCyrl() == YearType.WATER ) {
		months = monthsWyr;
	}
	else if ( __dataset.getCyrl() == YearType.NOV_TO_OCT ) {
		months = monthsIyr;
	}
	// Put in two rows because one row would be too wide
	for ( int i = 0; i < 12; i++ ) {
		JGUIUtil.addComponent(__monthlyOprEff_JPanel, new JLabel(TimeUtil.monthAbbreviation(months[i])),
			i-(i/6)*6, (i/6)*2, 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		__monthlyOprEff_JTextField[i] = new JTextField(6);
		JGUIUtil.addComponent(__monthlyOprEff_JPanel, __monthlyOprEff_JTextField[i],
			i-(i/6)*6, ((i/6)*2 + 1), 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	}
	return __monthlyOprEff_JPanel;
}

/**
Set up the monthly operating limits panel.
@param routine for logging
@return the panel for monthly operating limits
*/
private JPanel setupGUI_OperationalRightMonthlyOprMax ( String routine )
{
	__monthlyOprMax_JPanel = new JPanel();
	__monthlyOprMax_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Monthly and annual operating limits (ACFT)"));
	GridBagLayout gb = new GridBagLayout();
	__monthlyOprMax_JPanel.setLayout(gb);
	
	int [] monthsCyr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	int [] monthsWyr = { 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	int [] monthsIyr = { 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	int [] months = monthsCyr;
	if ( __dataset.getCyrl() == YearType.WATER ) {
		months = monthsWyr;
	}
	else if ( __dataset.getCyrl() == YearType.NOV_TO_OCT ) {
		months = monthsIyr;
	}
	// Put in two rows because one row would be too wide
	for ( int i = 0; i < 12; i++ ) {
		JGUIUtil.addComponent(__monthlyOprMax_JPanel, new JLabel(TimeUtil.monthAbbreviation(months[i])),
			i-(i/6)*6, (i/6)*2, 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		__monthlyOprMax_JTextField[i] = new JTextField(6);
		JGUIUtil.addComponent(__monthlyOprMax_JPanel, __monthlyOprMax_JTextField[i],
			i-(i/6)*6, ((i/6)*2 + 1), 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	}
	// Add the annual value at the end.
	JGUIUtil.addComponent(__monthlyOprMax_JPanel, new JLabel("Annual"),
		6, 2, 1, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	__monthlyOprMax_JTextField[12] = new JTextField(6);
	JGUIUtil.addComponent(__monthlyOprMax_JPanel, __monthlyOprMax_JTextField[12],
		6, 3, 1, 1, 0, 0,
		1, 0, 0, 1, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	return __monthlyOprMax_JPanel;
}

/**
Set up the monthly switch panel.
@param routine for logging
@return the panel for monthly switches
*/
private JPanel setupGUI_OperationalRightMonthSwitch ( String routine )
{
	__monthlySwitch_JPanel = new JPanel();
	__monthlySwitch_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Monthly on/off switch (leave all blank for default of all on; otherwise, specify every value)"));
	GridBagLayout gb = new GridBagLayout();
	__monthlySwitch_JPanel.setLayout(gb);
	
	int [] monthsCyr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	int [] monthsWyr = { 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	int [] monthsIyr = { 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	int [] months = monthsCyr;
	if ( __dataset.getCyrl() == YearType.WATER ) {
		months = monthsWyr;
	}
	else if ( __dataset.getCyrl() == YearType.NOV_TO_OCT ) {
		months = monthsIyr;
	}
	// Put in two rows because one row would be too wide
	for ( int i = 0; i < 12; i++ ) {
		__monthSwitch_JComboBox[i] = new SimpleJComboBox(false);
		__monthSwitch_JComboBox[i].setPrototypeDisplayValue("-31 - Last day used ");
		JGUIUtil.addComponent(__monthlySwitch_JPanel, new JLabel(TimeUtil.monthAbbreviation(months[i])),
			i-(i/6)*6, (i/6)*2, 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(__monthlySwitch_JPanel, __monthSwitch_JComboBox[i],
			i-(i/6)*6, ((i/6)*2 + 1), 1, 1, 0, 0,
			1, 0, 0, 1, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	}
	return __monthlySwitch_JPanel;
}

/**
Setup the panel used for Rio Grande Compact operational rights.  Put qdebt* on the left and the sources
on the right.
@param routine
@return
*/
private JPanel setupGUI_OperationalRightRioGrande(String routine)
{
	__rioGrande_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__rioGrande_JPanel.setLayout(gb);
	__rioGrande_JPanel.setBorder(BorderFactory.createTitledBorder("Rio Grande compact data"));

	int y = 0;
	int x1 = 0;
	int x2 = 1;
	int x3 = 2;
	int x4 = 3;
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Year for adjustment:"),
		x1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__qdebt_JTextField = new JTextField(20);
	__qdebt_JTextField.setToolTipText ("Year when annual obligation calculation includes " +
			"an adjustment for the cumulative surplus storage.");
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __qdebt_JTextField,
		x2, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Initial Surplus/Shortage (ACFT):"),
		x3, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__qdebtx_JTextField = new JTextField(6);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __qdebtx_JTextField,
		x4, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	++y;
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Index gage:"),
		x1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__rioGrandeIndexGage_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__rioGrandeIndexGage_JComboBox.setPrototypeDisplayValue(
		" 123456789012 - (Stream Gage) 123456789012345678901234 ");
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __rioGrandeIndexGage_JComboBox,
		x2, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Coefficient:"),
		x3, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__rioGrandeCoefficient_JTextField = new JTextField(6);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __rioGrandeCoefficient_JTextField,
		x4, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	++y;
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Basin:"),
		x1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__rioGrandeBasin_JTextField = new JTextField(20);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __rioGrandeBasin_JTextField,
		x2, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Basin yield (ACFT/year):"),
		x3, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__rioGrandeBasinYield_JTextField = new JTextField(6);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __rioGrandeBasinYield_JTextField,
		x4, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	++y;
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Drain:"),
		x1, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__rioGrandeDrain_JTextField = new JTextField(20);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __rioGrandeDrain_JTextField,
		x2, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, new JLabel("Drain yield (ACFT/year):"),
		x3, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__rioGrandeDrainYield_JTextField = new JTextField(6);
	JGUIUtil.addComponent(
		__rioGrande_JPanel, __rioGrandeDrainYield_JTextField,
		x4, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	return __rioGrande_JPanel;
}

/**
Setup the panel used for San Juan recovery operational right.
@param routine
@return
*/
private JPanel setupGUI_OperationalRightSanJuan(String routine)
{
	__sanJuan_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__sanJuan_JPanel.setLayout(gb);
	__sanJuan_JPanel.setBorder(BorderFactory.createTitledBorder("San Juan Recovery Implementation Program data"));

	int y = 0;
	JGUIUtil.addComponent( __sanJuan_JPanel, new JLabel("Minimum available water (CFS):"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__sanJuanSjmina_JTextField = new JTextField(10);
	JGUIUtil.addComponent( __sanJuan_JPanel, __sanJuanSjmina_JTextField,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	++y;
	JGUIUtil.addComponent( __sanJuan_JPanel, new JLabel("Average release (ACFT/year):"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__sanJuanSjrela_JTextField = new JTextField(10);
	JGUIUtil.addComponent( __sanJuan_JPanel, __sanJuanSjrela_JTextField,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	return __sanJuan_JPanel;
}

private JPanel setupGUI_OperationalRightSource(String routine)
{
	// assemble panel2
	__source_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__source_JPanel.setLayout(gb);
	__source_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Source(s) - note that Source 2 is used for special options for some right types"));
	
	__source1_JComboBox = new SimpleJComboBox(false); // Text field not editable
	__source1_JComboBox.setPrototypeDisplayValue(
		" 123456789012 - (" + StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_OUT_OF_PRIORITY +
		") 123456789012345678901234 ");
	__sourceAccount1_JComboBox = new SimpleJComboBox(false); // Text field not editable
	//__sourceAccount1_JComboBox.setPrototypeDisplayValue(" 1234 - 123456789012345678901234 ");
	__sourceAccount1_JComboBox.setPrototypeDisplayValue(" 0 - Meet target by releasing from each account ");
	__source2_JComboBox = new SimpleJComboBox(false); // Text field not editable
	//__source2_JComboBox.setPrototypeDisplayValue(" 123456789012 - (Instream Flow) 123456789012345678901234 ");
	__source2_JComboBox.setPrototypeDisplayValue(
		" 123456789012 - (" + StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_OUT_OF_PRIORITY +
		") 123456789012345678901234 ");
	__sourceAccount2_JComboBox = new SimpleJComboBox(false); // Text field not editable
	//__sourceAccount2_JComboBox.setPrototypeDisplayValue(" 1234 - 123456789012345678901234 ");
	//__sourceAccount2_JComboBox.setPrototypeDisplayValue(" 0 - specify if Source 2 is an operational right ");
	__sourceAccount2_JComboBox.setPrototypeDisplayValue(
		" 1 - Monthly diversion limit is in direct diversion demand ");
	
	
	/* FIXME SAM 2011-01-30 Need to be moved to other panels since only 2 main sources recognized now
	__source3_JComboBox = new SimpleJComboBox();
	__source3_JComboBox.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount3_JComboBox = new SimpleJComboBox();
	__sourceAccount3_JComboBox.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source4_JComboBox = new SimpleJComboBox();
	__source4_JComboBox.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount4_JComboBox = new SimpleJComboBox();
	__sourceAccount4_JComboBox.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source5_JComboBox = new SimpleJComboBox();
	__source5_JComboBox.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount5_JComboBox = new SimpleJComboBox();
	__sourceAccount5_JComboBox.setPrototypeDisplayValue("          "
		+ "                                                  ");
		*/
	__source1_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__source2_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	//__source3_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	//__source4_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	//__source5_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);

	__sourceAccount1_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount2_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	//__sourceAccount3_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	//__sourceAccount4_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	//__sourceAccount5_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	int y=0;
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Source 1:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __source1_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __sourceAccount1_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Source 2:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __source2_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __sourceAccount2_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	/*
	y++;
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Source 3:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __source3_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __sourceAccount3_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Source 4:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __source4_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __sourceAccount4_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Source 5:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __source5_JComboBox,
		1, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		__source_JPanel, new JLabel("Account:"),
		2, y, 1, 1, 0, 0,
		0, 4, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		__source_JPanel, __sourceAccount5_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__source1_JComboBox.addActionListener(this);
	*/
	return __source_JPanel;
}

private JPanel setupGUI_SearchPanel(String routine)
{
	int y = 0;
	JPanel psearch = new JPanel();
	psearch.setMinimumSize(new Dimension(250,100));
	psearch.setPreferredSize(new Dimension(250,100));
	GridBagLayout gb = new GridBagLayout();
	psearch.setLayout(gb);
	psearch.setBorder(BorderFactory.createTitledBorder("Search above list for:"));
	__searchCriteriaGroup = new ButtonGroup();
	__searchIDJRadioButton = new JRadioButton("ID", true);
	__searchIDJRadioButton.addActionListener(this);
	__searchCriteriaGroup.add(__searchIDJRadioButton);
	JGUIUtil.addComponent(psearch, __searchIDJRadioButton,
		0, y, 1, 1, 0, 0,
		1, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__searchID = new JTextField(10);
	JGUIUtil.addComponent(psearch, __searchID,
		1, y, 1, 1, 1.0, 0.0,
		1, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
	__searchID.addActionListener(this);
	y++;
	__searchNameJRadioButton = new JRadioButton("Name", false);
	__searchNameJRadioButton.addActionListener(this);
	__searchCriteriaGroup.add(__searchNameJRadioButton);
	__searchName = new JTextField(10);
	__searchName.setEditable(false);
	JGUIUtil.addComponent(
		psearch, __searchNameJRadioButton,
		0, y, 1, 1, 0, 0,
		1, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(
		psearch, __searchName,
		1, y, 1, 1, 1.0, 1.0,
		1, 0, 0, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
	__searchName.addActionListener(this);
	y++;
	__findNextOpr = new JButton("Find Next");
	JGUIUtil.addComponent(
		psearch, __findNextOpr,
		0, y, 4, 1, 0, 0,
		5, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__findNextOpr.addActionListener(this);

	return psearch;
}

/**
Setup text editor panel, used when the GUI does not know how to handle the details of an operational right.
@param routine
*/
private JPanel setupGUI_OperationalRightTextEditor (String routine)
{
	__textEditor_JPanel = new JPanel();
	// Set font smaller
    __textEditor_JPanel.setFont ( new Font("Lucida Console", Font.PLAIN, 11 ) );
	__textEditor_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Operational right text (for types where detailed editing has not been implemented)"));
	GridBagLayout gb = new GridBagLayout();
	__textEditor_JPanel.setLayout(gb);
	__textEditor_JPanel.setMinimumSize(new Dimension(600,200));
    JTextArea ref_JTextArea = new JTextArea (2, 100);
    ref_JTextArea.setFont ( new Font("Lucida Console", Font.PLAIN, 11 ) );
    // Add a string buffer with reference positions - similar to TSTool comments
    // 206 characters from record 1 so put for 210
    //0        10        20
    //12345678901234567890...
    int n10 = 21; // number to repeat.
    // TODO SAM 2007-04-22 REVISIT layout for n10=20
    StringBuffer b = new StringBuffer();
    b.append ( StringUtil.formatString(0,"%-9d"));
    for ( int i = 1; i < n10; i++ ) {
    	b.append( StringUtil.formatString(i*10,"%-10d"));
    }
    b.append ( "\n");
    b.append ( "1234567890");
    for ( int i = 1; i < n10; i++ ) {
    	b.append( "1234567890");
    }
    ref_JTextArea.setText( b.toString() );
	ref_JTextArea.setEditable (false);
	ref_JTextArea.setEnabled ( false );
	__textEditor_JTextArea = new JTextArea(8,100);
	JPanel combined_JPanel = new JPanel();
	combined_JPanel.setLayout(gb);
	JGUIUtil.addComponent(combined_JPanel, ref_JTextArea,
			0, 0, 6, 1, 1, 1, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(combined_JPanel, __textEditor_JTextArea,
			0, 1, 6, 1, 1, 1, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(__textEditor_JPanel, new JScrollPane(combined_JPanel),
		0, 0, 6, 1, 1, 1, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	return __textEditor_JPanel;
}

// TODO smalers 2019-06-01 evaluate not using deprecated code
/**
Show help about the operational rights - this is the documentation from the StateMod documentation.
*/
@SuppressWarnings("deprecation")
private void showHelpDocument ()
{
    // The location of the documentation is relative to the application home
    String docFileName = getHelpDocumentName();
    // Now display using the default application for the file extension
    Message.printStatus(2, "", "Opening operational rights documentation \"" + docFileName + "\"" );
    IOUtil.openURL(docFileName);
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
			__operationalRights.get(i);
		if (!changed && o.changed()) {
			changed = true;
		}
		o.acceptChanges();
	}					
	if (changed) {
		__dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
	}	
	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (StateMod_DataSet_WindowManager.WINDOW_OPERATIONAL_RIGHT );
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
