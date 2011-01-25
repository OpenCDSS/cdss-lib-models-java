package DWR.StateMod;

import java.awt.Dimension;
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
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;

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
	__BUTTON_SHOW_ON_MAP = "Show on Map",	
	__BUTTON_SHOW_ON_NETWORK = "Show on Network",
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
The index in __disables[] of textfields that should NEVER be made editable (e.g., ID fields).
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
	__destination_JPanel,
	__source_JPanel,
	__rioGrande_JPanel,
	__opr8Panel,
	__opr20Panel,
	__monthlySwitch_JPanel,
	__qdebtPanel,
	__interveningStructures_JPanel,
	__comments_JPanel,
	__textEditor_JPanel;

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
	__ruleTypeFullyEditable_JTextField,
	__oprID_JTextField,
	__opr20Sjmina_JTextField,
	__opr20Sjrela_JTextField,
	__qdebt_JTextField,
	__qdebtx_JTextField;

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
	__oprSwitch_JComboBox;

/**
Combobox array for holding month switches.
*/
private SimpleJComboBox[] __monthSwitch_JComboBox = new SimpleJComboBox[12];

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

private int __currentItyopr = -1;

/**
Vectors used to populate combo boxes.  They are only initialized if they need
to be used, and then they are re-used.
*/
private List<String>
	__reservoirIDs = null,
	__diversionRightIDs = null,
	__streamGageIDs = null,
	__instreamFlowIDs = null,
	__diversionIDs = null,
	__operationalRightIDs = null;

private List<StateMod_Reservoir> __reservoirs = null;
private List<StateMod_ReservoirRight>__reservoirRights = null;
private List<StateMod_DiversionRight>__diversionRights = null;
private List<StateMod_Diversion>__diversions = null;
private List<StateMod_StreamGage>__streamGages = null;
private List<StateMod_InstreamFlow>__instreamFlows = null;

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
	__operationalRights = (List<StateMod_OperationalRight>)__operationalRightsComponent.getData();
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
	__operationalRights = (List)__operationalRightsComponent.getData();
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
		// REVISIT HELP (JTS - 2003-06-24)
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
	else if (source == __destination_JComboBox) {		
		fillDestinationAccount(__currentItyopr, __destination_JComboBox.getSelected());
		if (__currentItyopr == 8) {
			// special case, the source is tied to the destination
			__source1_JComboBox.setSelectedItem(__destination_JComboBox.getSelected());
		}
	}
	else if (source == __source1_JComboBox) {
		StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
		opr.setCiopso1(getFirstToken(__source1_JComboBox.getSelected()));
		fillSourceAccount1(__currentItyopr, opr);
	}
	else if (source == __ruleTypeSwitch_JComboBox) {
		StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
		opr.setItyopr(getFirstToken(__ruleTypeSwitch_JComboBox.getSelected()));
		populateOperationalRight(opr);
	}	
}

/**
Checks the text fields for validity before they are saved back into the data object.
@return true if the text fields are okay, false if not.
*/
private boolean checkInput() {
	List<String> errors = new Vector();
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
Combines two lists into a new list.  
@param v1 the first list to add into the new Vector.
@param v2 the second list to add into the new Vector.
@return a new list that contains all the elements from the first and second lists.
*/
private List combineLists(List v1, List v2) {
	List v = new Vector();
	v.addAll(v1);
	v.addAll(v2);
	return v;
}


/**
Creates the instream flow ID Vector, stores it in __instreamFlowIDs, and 
stores the instream flow Vector from the dataset into __instreamFlows.
*/
private void createInstreamFlowIDVector() {
	DataSetComponent isfComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_STATIONS);
	__instreamFlows = (List<StateMod_InstreamFlow>)isfComp.getData();
	__instreamFlowIDs = StateMod_Util.createIdentifierList(__instreamFlows, true);
}

/**
Creates the diversion ID Vector, stores it into __diversionIDs, and
stores the diversions Vector from the dataset into __diversions.
*/
private void createDiversionIDVector() {
	DataSetComponent divComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_STATIONS);
	__diversions = (List)divComp.getData();
	__diversionIDs = StateMod_Util.createIdentifierList(__diversions, true);
}

/**
Creates the diversion right ID Vector, stores it into __diversionRightIDs, and
stores the diversion rights Vector from the dataset into __diversionRights.
*/
private void createDiversionRightIDVector() {
	DataSetComponent divRightComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_RIGHTS);
	__diversionRights = (List)divRightComp.getData();
	__diversionRightIDs = StateMod_Util.createIdentifierList(__diversionRights, true);
}

/**
Creates the operational right ID Vector and stores it into __operationalRightIDs.
*/
private void createOperationalRightIDVector() {
	__operationalRightIDs = StateMod_Util.createIdentifierList(__operationalRights, true);
}

/**
Creates the reservoir ID Vector, stores it into __reservoirIDs, and
stores the reservoir Vector from the dataset into __reservoirs.
*/
private void createReservoirIDVector() {
	DataSetComponent resComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_STATIONS);
	__reservoirs = (List)resComp.getData();
	__reservoirIDs = StateMod_Util.createIdentifierList(__reservoirs, true);
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
	//__reservoirRightIDs = StateMod_Util.createDataList(__reservoirRights, true);
}
*/

/**
Creates the stream gage ID Vector, stores it into __streamGageIDs, and
stores the stream gage Vector from the dataset into __streamGages.
*/
private void createStreamGageIDVector() {
	DataSetComponent gageComp = __dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
	__streamGages = (List)gageComp.getData();
	__streamGageIDs = StateMod_Util.createIdentifierList(__streamGages, true);
}

/**
Disables a combo box by setting it disabled, uneditable, and removing all the items.
@param cb the SimpleJComboBox to disable.
*/
private void disableComboBox(SimpleJComboBox cb) {
	cb.removeAllItems();
	cb.setEnabled(false);
	cb.setEditable(false);
}

/**
Enables the monthly switch panel, or disables it, depending on the value passed in.
@param enable whether to enable the monthly switches or disable them.
*/
private void enableOpPanel(boolean enable) {
	for (int i = 0; i < 12; i++) {
		if (enable) {
			__monthSwitch_JComboBox[i].setEnabled(true);
		}
		else {
			__monthSwitch_JComboBox[i].removeAllItems();
			__monthSwitch_JComboBox[i].setEditable(false);
			__monthSwitch_JComboBox[i].setEnabled(false);
		}
	}
}

/**
Fills the destination account field based on the value in the destination field.
@param ityopr the current operational rule.
@param value the value in the destination field.
*/
private void fillDestinationAccount(int ityopr, String value) {
	value = getFirstToken(value);
//	System.out.println("fillDestinationAccount: " + ityopr + " '" + value + "'");
	int index = 0;
	List accounts = null;
	StateMod_Reservoir r = null;
	switch (ityopr) {
		case 2:
		case 3:
		case 11:
		case 14:
			__destinationAccount_JComboBox.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index == -1) {
				// not a reservoir, check to see if it's a diversion
				index = StateMod_Util.indexOf(__diversions, value);
				if (index == -1) {
					return;
				}
				__destinationAccount_JComboBox.add("1");
				__destinationAccount_JComboBox.setEditable(false);
				return;
			}
			r = (StateMod_Reservoir)__reservoirs.get(index);
			accounts = r.getAccounts();
			__destinationAccount_JComboBox.setData(StateMod_Util.createIdentifierList(accounts, true));
			__destinationAccount_JComboBox.setEditable(true);
			break;
		case 5:
			__destinationAccount_JComboBox.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirRights,value);
			if (index > -1) {
				// TODO SAM 2007-03-01 Evaluate logic
				//rr = __reservoirRights.elementAt(index);
				/*
				REVISIT (JTS - 2003-09-18) when rights get accounts
				accounts = rr.getAccounts();
				__destinationAccount.setData(StateMod_Util.createDataList(accounts,true));
				*/
				__destinationAccount_JComboBox.setEditable(true);
				return;
			}
			
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index > -1) {
				r = __reservoirs.get(index);
				accounts = r.getAccounts();
				__destinationAccount_JComboBox.setData(StateMod_Util.createIdentifierList(accounts,true));
				__destinationAccount_JComboBox.setEditable(true);
				return;
			}
			return;
		case 8:
		case 16:
			__destinationAccount_JComboBox.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index > -1) {
				r = __reservoirs.get(index);
				accounts = r.getAccounts();
				__destinationAccount_JComboBox.setData(StateMod_Util.createIdentifierList(accounts,true));
				__destinationAccount_JComboBox.setEditable(true);
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

//	System.out.println("fillSourceAccount1: " + ityopr + " '" + value + "'");
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
			__sourceAccount1_JComboBox.removeAllItems();
			index = StateMod_Util.indexOf(__reservoirs, value);
			if (index == -1) {
				return;
			}
			StateMod_Reservoir r = __reservoirs.get(index);
			List accounts = r.getAccounts();
			__sourceAccount1_JComboBox.setData(StateMod_Util.createIdentifierList(accounts, true));
			if (ityopr == 9 || ityopr == 20) {
				// special case
				__sourceAccount1_JComboBox.addAt("0 - Prorate", 0);
			}
			__sourceAccount1_JComboBox.setEditable(true);
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
	__monthlySwitch_JPanel = null;
	__interveningStructures_JPanel = null;
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
	__opr8Panel = null;
	__opr8_JComboBox = null;
	__opr20Panel = null;
	__opr20Sjmina_JTextField = null;
	__opr20Sjrela_JTextField = null;
	__qdebt_JTextField = null;
	__qdebtx_JTextField = null;
	__qdebtPanel = null;

	super.finalize();
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
	__disables[i++] = __interveningStructures_JPanel;
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
	__disables[i++] = __opr20Sjmina_JTextField;
	__disables[i++] = __opr20Sjrela_JTextField;
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
Fills in the additional data section of the form for the specified operational right.
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
			__interveningStructures_JPanel.setVisible(true);			
			__rioGrande_JPanel.setVisible(false);
			break;
		case 3:
			enableOpPanel(false);
			__interveningStructures_JPanel.setVisible(true);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 6:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__interveningStructures_JPanel.setVisible(false);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 8:
			enableOpPanel(false);
			__opr8Panel.setVisible(true);
			__opr8_JComboBox.removeAllItems();
			// REVISIT (JTS - 2003-09-22)
			// what goes here?
//			__opr8JComboBox.add("" + opr.getIntern());
			__rioGrande_JPanel.setVisible(true);
			__interveningStructures_JPanel.setVisible(false);
			break;
		case 11:
			enableOpPanel(false);
			__interveningStructures_JPanel.setVisible(true);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 13:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__interveningStructures_JPanel.setVisible(true);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 14:
			enableOpPanel(false);
			__interveningStructures_JPanel.setVisible(false);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 15:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__interveningStructures_JPanel.setVisible(false);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 16:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__interveningStructures_JPanel.setVisible(true);
			__rioGrande_JPanel.setVisible(false);
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
			__interveningStructures_JPanel.setVisible(false);
			__qdebtPanel.setVisible(true);
			__qdebt_JTextField.setText("" + opr.getQdebt());
			__qdebtx_JTextField.setText("" + opr.getQdebtx());
			__rioGrande_JPanel.setVisible(true);
			break;
		case 19:
			enableOpPanel(false);
			__interveningStructures_JPanel.setVisible(false);
			__rioGrande_JPanel.setVisible(false);
			break;
		case 20:
			__opr20Panel.setVisible(true);
			__opr20Sjmina_JTextField.setText("" + opr.getSjmina());
			__opr20Sjrela_JTextField.setText("" + opr.getSjrela());
			__rioGrande_JPanel.setVisible(true);
			if (dumx == 12) {
				setupMonthlyChoices(opr);
				enableOpPanel(true);
			}
			else {
				enableOpPanel(false);
			}
			__interveningStructures_JPanel.setVisible(false);
			break;
		case 23:
			setupMonthlyChoices(opr);
			enableOpPanel(true);
			__interveningStructures_JPanel.setVisible(false);
			__rioGrande_JPanel.setVisible(false);
			break;
		default:
			enableOpPanel(false);
			__interveningStructures_JPanel.setVisible(false);
			__rioGrande_JPanel.setVisible(false);
			break;
	}
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
	if ( (metadata == null) || !metadata.getFullEditingSupported() ) {
		useTextEditor = true;
	}
	
	// Populate the various panels/controls depending on right metadata
	
	populateOperationalRightAttributes ( opr, metadata, useTextEditor );
	populateOperationalRightDestination ( opr, metadata, useTextEditor );
	populateOperationalRightSource ( opr, metadata, useTextEditor );
	populateOperationalRightMonthSwitch ( opr, metadata, useTextEditor );
	populateOperationalRightInterveningStructures ( opr, metadata, useTextEditor );
	populateOperationalRightRioGrande ( opr, metadata, useTextEditor );
	populateOperationalRightComments ( opr, metadata, useTextEditor );
	populateOperationalRightTextEditor ( opr, metadata, useTextEditor );
	
	// Legacy code...
	
	populateAdditionalData(opr);
	
	__ruleTypeSwitch_JComboBox.setEditable(false);
	__oprSwitch_JComboBox.setEditable(false);
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
	
	// FIXME JTS 2003-09-22
	// whenever the rules for rule type #19 are determined, removed
	// the check for 19 from this if statement.
	// Also disable when a right is not fully handled.
	if ( useTextEditor ) {
		// Make sure that even basic information is not editable...
		//__irtem.setEditable (false);
		// More specific information
		__destination_JComboBox.removeAllItems();
		__destination_JComboBox.setEnabled(false);
		__destination_JComboBox.setEditable(false);
		__destinationAccount_JComboBox.removeAllItems();
		__destinationAccount_JComboBox.setEnabled(false);
		__destinationAccount_JComboBox.setEditable(false);
		__source1_JComboBox.removeAllItems();
		__source1_JComboBox.setEnabled(false);
		__source1_JComboBox.setEditable(false);
		__sourceAccount1_JComboBox.removeAllItems();
		__sourceAccount1_JComboBox.setEnabled(false);
		__sourceAccount1_JComboBox.setEditable(false);
		__source2_JComboBox.removeAllItems();
		__source2_JComboBox.setEnabled(false);
		__source2_JComboBox.setEditable(false);
		__sourceAccount2_JComboBox.removeAllItems();
		__sourceAccount2_JComboBox.setEnabled(false);
		__sourceAccount2_JComboBox.setEditable(false);
		__source3_JComboBox.removeAllItems();
		__source3_JComboBox.setEnabled(false);
		__source3_JComboBox.setEditable(false);
		__sourceAccount3_JComboBox.removeAllItems();
		__sourceAccount3_JComboBox.setEnabled(false);
		__sourceAccount3_JComboBox.setEditable(false);
		__source4_JComboBox.removeAllItems();
		__source4_JComboBox.setEnabled(false);
		__source4_JComboBox.setEditable(false);
		__sourceAccount4_JComboBox.removeAllItems();
		__sourceAccount4_JComboBox.setEnabled(false);
		__sourceAccount4_JComboBox.setEditable(false);
		__source5_JComboBox.removeAllItems();
		__source5_JComboBox.setEnabled(false);
		__source5_JComboBox.setEditable(false);
		__sourceAccount5_JComboBox.removeAllItems();
		__sourceAccount5_JComboBox.setEnabled(false);
		__sourceAccount5_JComboBox.setEditable(false);
		return;
	}
	else {
		__destination_JComboBox.setEnabled(true);
		__destination_JComboBox.setEditable(true);
		__destinationAccount_JComboBox.setEnabled(true);
		__destinationAccount_JComboBox.setEditable(true);
		__source1_JComboBox.setEnabled(true);
		__source1_JComboBox.setEditable(true);
		__sourceAccount1_JComboBox.setEnabled(true);
		__sourceAccount1_JComboBox.setEditable(true);
		__source2_JComboBox.setEnabled(true);
		__source2_JComboBox.setEditable(true);
		__sourceAccount2_JComboBox.setEnabled(true);
		__sourceAccount2_JComboBox.setEditable(true);
		__source3_JComboBox.setEnabled(true);
		__source3_JComboBox.setEditable(true);
		__sourceAccount3_JComboBox.setEnabled(true);
		__sourceAccount3_JComboBox.setEditable(true);
		__source4_JComboBox.setEnabled(true);
		__source4_JComboBox.setEditable(true);
		__sourceAccount4_JComboBox.setEnabled(true);
		__sourceAccount4_JComboBox.setEditable(true);
		__source5_JComboBox.setEnabled(true);
		__source5_JComboBox.setEditable(true);
		__sourceAccount5_JComboBox.setEnabled(true);
		__sourceAccount5_JComboBox.setEditable(true);		
	}
	
//	System.out.println("populateRightInformation: " + ityopr);
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

}

/**
Populate operational right for the destination data panel.
*/
private void populateOperationalRightAttributes ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	boolean enabled = false; // Whether editor components should be enabled
	__ruleTypeFullyEditable_JTextField.setEditable(false);
	__ruleTypeFullyEditable_JTextField.setEnabled(true);
	if ( useTextEditor ) {
		__ruleTypeFullyEditable_JTextField.setText("No - use text editor");
	}
	else {
		__ruleTypeFullyEditable_JTextField.setText("Yes - use data fields");
	}
}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightComments ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
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
}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightDestination ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	if ( useTextEditor ) {
		__destination_JPanel.setVisible(false);
		__destination_JPanel.setEnabled(false);
	}
	else {
		__destination_JPanel.setVisible(true);
		__destination_JPanel.setEnabled(true);
	}
}

/**
Populate operational right for the intervening structures data panel.
*/
private void populateOperationalRightInterveningStructures ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	if ( useTextEditor ) {
		__interveningStructures_JPanel.setVisible(false);
		__interveningStructures_JPanel.setEnabled(false);
	}
	else {
		__interveningStructures_JPanel.setVisible(true);
		__interveningStructures_JPanel.setEnabled(true);
	}
}

/**
Populate operational right for the month switch data panel.
*/
private void populateOperationalRightMonthSwitch ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	if ( useTextEditor ) {
		__monthlySwitch_JPanel.setVisible(false);
		__monthlySwitch_JPanel.setEnabled(false);
	}
	else {
		__monthlySwitch_JPanel.setVisible(true);
		__monthlySwitch_JPanel.setEnabled(true);
	}
}

/**
Populate operational right for the Rio Grande data panel.
*/
private void populateOperationalRightRioGrande ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	if ( useTextEditor ) {
		__rioGrande_JPanel.setVisible(false);
		__rioGrande_JPanel.setEnabled(false);
	}
	else {
		__rioGrande_JPanel.setVisible(true);
		__rioGrande_JPanel.setEnabled(true);
	}
}

/**
Populate operational right for the source data panel.
*/
private void populateOperationalRightSource ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	if ( useTextEditor ) {
		__source_JPanel.setVisible(false);
		__source_JPanel.setEnabled(false);
	}
	else {
		__source_JPanel.setVisible(true);
		__source_JPanel.setEnabled(true);
	}
}

/**
Populate operational right for the text editor panel.
*/
private void populateOperationalRightTextEditor ( StateMod_OperationalRight opr,
	StateMod_OperationalRight_Metadata metadata, boolean useTextEditor )
{
	if ( useTextEditor ) {
		// Set up the text editor text
		StringBuffer b = new StringBuffer();
		String nl = "\n"; // As per Java doc, this should be used in memory
		for ( String s : opr.getRightStrings() ) {
			b.append(s + nl);
		}
		__textEditor_JTextArea.setText(b.toString());
		__textEditor_JPanel.setVisible(true);
	}
	else {
		__textEditor_JPanel.setVisible(false);
	}
}

/**
Processes a table selection (either via a mouse press or programmatically 
from selectTableIndex() by writing the old data back to the data set component
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

	JGUIUtil.enableComponents(__disables, __textUneditables, __editable);

	StateMod_OperationalRight opr = __operationalRights.get(__currentOpRightsIndex);
	__oprID_JTextField.setText(opr.getID());
	__oprName_JTextField.setText(opr.getName());
	__oprAdminNumber_JTextField.setText(opr.getRtem());
	//FIXME SAM 2011-01-24 Need to use admin number
	//if (opr.getCgoto().equals("")) {	
	//	setOriginalCgoto(opr, opr.getRtem());
	//}

	// switch
	if (opr.getSwitch() == 1) {
		__oprSwitch_JComboBox.select("1 - On");
	}
	else {
		__oprSwitch_JComboBox.select("0 - Off");
	}	

	// rule type
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
	//__ruleTypeSwitch_JComboBox.select(ityopr);
	StateMod_OperationalRight_Metadata metadata = StateMod_OperationalRight_Metadata.getMetadata(ityopr);
	if ( metadata != null && metadata.getFullEditingSupported() ) {
		setMessageText ( "" );
	}
	else {
		setMessageText ( "Rule type " + ityopr + " is supported for text editing only." );
	}
	
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

	__interveningStructuresWorksheet.stopEditing();

	StateMod_OperationalRight opr = __operationalRights.get(record);
	// Comments are always set
	List<String> commentsBeforeData = new Vector();
	String comments = __comments_JTextArea.getText(); // Do not trim because blank lines will be lost
	if ( comments.length() > 0 ) {
		commentsBeforeData = StringUtil.breakStringList(comments, "\n", 0);
	}
	opr.setCommentsBeforeData(commentsBeforeData);

	// The rest of the data depends on whether editing as text or not
	StateMod_OperationalRight_Metadata metadata =
		StateMod_OperationalRight_Metadata.getMetadata(opr.getItyopr());
	boolean useTextEditor = false;
	if ( (metadata == null) || !metadata.getFullEditingSupported() ) {
		useTextEditor = true;
	}
	if ( useTextEditor ) {
		List<String> rightStringList = new Vector();
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
	
	// Intervening structures the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightInterveningStructures(routine),
		0, yRight, 12, 1, 0, 0,  	
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Rio Grande on the right...

	++yRight;
	JGUIUtil.addComponent(rightJPanel, setupGUI_OperationalRightRioGrande(routine),
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
	setSize(1050,700);
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
	int [] widthsR = __interveningStructuresWorksheet.getCellRenderer().getColumnWidths();
	if (widthsR != null) {
		__interveningStructuresWorksheet.setColumnWidths(widthsR);
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
	__cancelJButton = new JButton(__BUTTON_CANCEL);
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	
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
//	pfinal.add(__helpJButton);
	pfinal.add(__closeJButton);
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

/**
Setup the GUI components for the operational rights list.
@param routine
*/
private JPanel setupGUI_OperationalRightAttributes (String routine)
{
	JPanel attribute_JPanel = new JPanel();	// Right side of interface for specific right data
	GridBagLayout gb = new GridBagLayout();
	attribute_JPanel.setLayout(gb);
	attribute_JPanel.setBorder(BorderFactory.createTitledBorder("Primary attributes:"));
	int y = 0;
	__oprID_JTextField = new JTextField(20);
	JGUIUtil.addComponent(
		attribute_JPanel, new JLabel("Operational right ID:"),
		0, y, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		attribute_JPanel, __oprID_JTextField,
		1, y, 1, 1, 1.0, 0.0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
//	__oprStationID.addActionListener(this);
	__oprID_JTextField.setEditable(false);
	y++;
	__oprName_JTextField = new JTextField(24);
	__oprName_JTextField.setEditable(false);
	JGUIUtil.addComponent(
		attribute_JPanel, new JLabel("Operational right name:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		attribute_JPanel, __oprName_JTextField,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
//	__oprName.addActionListener(this);
	y++;
	__ruleTypeSwitch_JComboBox = new SimpleJComboBox();
	List<StateMod_OperationalRight_Metadata> metadataList =
		StateMod_OperationalRight_Metadata.getAllMetadata();
	for ( int i = 0; i < metadataList.size(); i++ ) {
		__ruleTypeSwitch_JComboBox.add("" + metadataList.get(i).getRightTypeNumber() + " - " +
			metadataList.get(i).getRightTypeName() );
	}
	__ruleTypeSwitch_JComboBox.setEnabled(false);
	__ruleTypeSwitch_JComboBox.setEditable(false);
	JGUIUtil.addComponent(
		attribute_JPanel, new JLabel("Operational right type:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		attribute_JPanel, __ruleTypeSwitch_JComboBox,
		1, y, 6, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__ruleTypeSwitch_JComboBox.addActionListener(this);
	y++;
	__ruleTypeFullyEditable_JTextField = new JTextField(12);
	JGUIUtil.addComponent(
		attribute_JPanel, new JLabel("Fully editable?:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		attribute_JPanel, __ruleTypeFullyEditable_JTextField,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__ruleTypeFullyEditable_JTextField.setEnabled(false); // For display only
	__ruleTypeFullyEditable_JTextField.setEditable(false);
	y++;
	__oprAdminNumber_JTextField = new JTextField(12);
	JGUIUtil.addComponent(
		attribute_JPanel, new JLabel("Administration number:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		attribute_JPanel, __oprAdminNumber_JTextField,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
//	__oprLocation.addActionListener(this);
	y++;
	__oprSwitch_JComboBox = new SimpleJComboBox();
	__oprSwitch_JComboBox.setPrototypeDisplayValue(" 0 - Off " );
	__oprSwitch_JComboBox.add("0 - Off");
	__oprSwitch_JComboBox.add("1 - On");	
	__oprSwitch_JComboBox.setEditable(false);
	JGUIUtil.addComponent(
		attribute_JPanel, new JLabel("On/off switch:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(
		attribute_JPanel, __oprSwitch_JComboBox,
		1, y, 1, 1, 1, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	return attribute_JPanel;
}

/**
Setup comments panel.
@param routine
*/
private JPanel setupGUI_OperationalRightComments (String routine)
{
	__comments_JPanel = new JPanel();
	__comments_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Comments (will be output above operational right data, with # at start of line):"));
	GridBagLayout gb = new GridBagLayout();
	__comments_JPanel.setLayout(gb);
	//__comments_JPanel.setMinimumSize(new Dimension(600,200));
	__comments_JTextArea = new JTextArea(8,100);
	JGUIUtil.addComponent(__comments_JPanel, new JScrollPane(__comments_JTextArea),
		0, 0, 6, 1, 1, 1, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	return __comments_JPanel;
}

private JPanel setupGUI_OperationalRightDestination ( String routine )
{
	__destination_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__destination_JPanel.setLayout(gb);
	__destination_JPanel.setBorder(BorderFactory.createTitledBorder("Destination:"));
	int y = 0;
	JGUIUtil.addComponent(
		__destination_JPanel, new JLabel("Destination:"),
		0, y, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__destination_JComboBox = new SimpleJComboBox();
	__destination_JComboBox.setPrototypeDisplayValue("                    "
		+ "                                                  ");
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
	__destinationAccount_JComboBox = new SimpleJComboBox();
	__destinationAccount_JComboBox.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__destinationAccount_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	JGUIUtil.addComponent(
		__destination_JPanel, __destinationAccount_JComboBox,
		3, y, 1, 1, 0, 0,
		1, 0, 0, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__destination_JComboBox.addActionListener(this);
	return __destination_JPanel;
}

private JPanel setupGUI_OperationalRightInterveningStructures(String routine)
{
	JButton addButton = new JButton(__BUTTON_ADD);
	addButton.addActionListener(this);
	JButton deleteButton = new JButton(__BUTTON_DELETE);
	deleteButton.addActionListener(this);

	// TODO (JTS - 2003-09-23) will need enabled later
	addButton.setEnabled(false);
	deleteButton.setEnabled(false);
	__interveningStructures_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__interveningStructures_JPanel.setLayout(gb);
	JGUIUtil.addComponent(
		__interveningStructures_JPanel, addButton,
		0, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(
		__interveningStructures_JPanel, deleteButton,
		1, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	
	__interveningStructures_JPanel.setBorder(BorderFactory.createTitledBorder(
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
	
	JScrollWorksheet opRightJSW = null;
	try {
		StateMod_OperationalRight_TableModel tmo = new
			StateMod_OperationalRight_TableModel( new Vector(), __editable);
		StateMod_OperationalRight_CellRenderer cro = new
			StateMod_OperationalRight_CellRenderer(tmo);
	
		opRightJSW = new JScrollWorksheet(cro, tmo, P2);
		__interveningStructuresWorksheet = opRightJSW.getJWorksheet();
		__interveningStructuresWorksheet.removeColumn(StateMod_OperationalRight_TableModel.COL_ID);
		__interveningStructuresWorksheet.removeColumn(StateMod_OperationalRight_TableModel.COL_NAME);
		__interveningStructuresWorksheet.removeColumn(StateMod_OperationalRight_TableModel.COL_TYPE);
		//__opRightWorksheet.setColumnComboBoxValues(2, new Vector());
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error building intervening structures list.");
		Message.printWarning(2, routine, e);
		opRightJSW = new JScrollWorksheet(0, 0, P2);
		__interveningStructuresWorksheet = opRightJSW.getJWorksheet();
	}
	__interveningStructuresWorksheet.setPreferredScrollableViewportSize(null);
	__interveningStructuresWorksheet.setHourglassJFrame(this);
	/*
	__opRightWorksheet.addMouseListener(this);
	__opRightWorksheet.addKeyListener(this);
	*/
	return __interveningStructures_JPanel;
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

private JPanel setupGUI_OperationalRightMonthSwitch ( String routine )
{

	__monthlySwitch_JPanel = new JPanel();
	__monthlySwitch_JPanel.setBorder(BorderFactory.createTitledBorder("Monthly on/off switch:"));
	GridBagLayout gb = new GridBagLayout();
	__monthlySwitch_JPanel.setLayout(gb);
	
	int [] monthsCyr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	int [] monthsWyr = { 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	int [] monthsIyr = { 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	int [] months = monthsCyr;
	if ( __dataset.getCyrl() == StateMod_DataSet.SM_WYR ) {
		months = monthsWyr;
	}
	else if ( __dataset.getCyrl() == StateMod_DataSet.SM_IYR ) {
		months = monthsIyr;
	}
	// Put in two rows because one row would be too wide
	for ( int i = 0; i < 12; i++ ) {
		__monthSwitch_JComboBox[i] = new SimpleJComboBox(false);
		__monthSwitch_JComboBox[i].setPrototypeDisplayValue("-9999 - Off Year");
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

private JPanel setupGUI_OperationalRightRioGrande(String routine)
{
	__rioGrande_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__rioGrande_JPanel.setLayout(gb);
	__rioGrande_JPanel.setBorder(BorderFactory.createTitledBorder("Additional Data"));

	JPanel lowerPanel = new JPanel();
	lowerPanel.setLayout(gb);

	JGUIUtil.addComponent(
		lowerPanel, __interveningStructures_JPanel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);		

	__opr8Panel = new JPanel();
	__opr8_JComboBox = new SimpleJComboBox();
	__opr8_JComboBox.setPrototypeDisplayValue("                   ");
	__opr20Panel = new JPanel();
	__opr20Sjmina_JTextField = new JTextField(20);
	__opr20Sjrela_JTextField = new JTextField(20);
	__qdebtPanel = new JPanel();
	__qdebt_JTextField = new JTextField(20);
	__qdebtx_JTextField = new JTextField(20);

	__opr8Panel.setLayout(gb);
	JGUIUtil.addComponent(__opr8Panel, 
		new JLabel("Destination Water Right ID:"),
		0, 0, 1, 1, 1, 1, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);	
	JGUIUtil.addComponent(__opr8Panel, __opr8_JComboBox,
		1, 0, 1, 1, 0, 0, 
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	
	__opr20Panel.setLayout(gb);
	JGUIUtil.addComponent(__opr20Panel, 
		new JLabel("Minimum Available Water (AF):"),
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);	
	JGUIUtil.addComponent(__opr20Panel, __opr20Sjmina_JTextField,
		1, 0, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__opr20Panel, 	
		new JLabel("Average Release (AF/YR):"),
		0, 1, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__opr20Panel, __opr20Sjrela_JTextField,
		1, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	__qdebtPanel.setLayout(gb);
	JGUIUtil.addComponent(__qdebtPanel,
		new JLabel("Year when Calculations Include Adjustment:"),
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__qdebtPanel, __qdebt_JTextField,
		1, 0, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);		
	JGUIUtil.addComponent(__qdebtPanel,
		new JLabel("Initial Surplus/Shortage (AF):"),
		0, 1, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__qdebtPanel, __qdebtx_JTextField,
		1, 1, 1, 1, 0, 0,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	JGUIUtil.addComponent(__rioGrande_JPanel, __opr8Panel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__rioGrande_JPanel, __qdebtPanel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(__rioGrande_JPanel, __opr20Panel,
		0, 0, 1, 1, 1, 1,
		0, 0, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		
	JGUIUtil.addComponent(
		lowerPanel, __rioGrande_JPanel,
		1, 0, 1, 1, 1, 1,
		0, 0, 0, 0,		
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	__rioGrande_JPanel.setVisible(false);
	return __rioGrande_JPanel;
}

private JPanel setupGUI_OperationalRightSource(String routine)
{
	// assemble panel2
	__source_JPanel = new JPanel();
	GridBagLayout gb = new GridBagLayout();
	__source_JPanel.setLayout(gb);
	__source_JPanel.setBorder(BorderFactory.createTitledBorder("Sources:"));
	
	__source1_JComboBox = new SimpleJComboBox();
	__source1_JComboBox.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount1_JComboBox = new SimpleJComboBox();
	__sourceAccount1_JComboBox.setPrototypeDisplayValue("          "
		+ "                                                  ");
	__source2_JComboBox = new SimpleJComboBox();
	__source2_JComboBox.setPrototypeDisplayValue("                    "
		+ "                                                  ");
	__sourceAccount2_JComboBox = new SimpleJComboBox();
	__sourceAccount2_JComboBox.setPrototypeDisplayValue("          "
		+ "                                                  ");
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
	__source1_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__source2_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__source3_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__source4_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__source5_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);

	__sourceAccount1_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount2_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount3_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount4_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
	__sourceAccount5_JComboBox.setSelectionFailureFallback("~ - Unknown", 0);
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
	__textEditor_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Operational right text (for types where detailed editing has not been implemented):"));
	GridBagLayout gb = new GridBagLayout();
	__textEditor_JPanel.setLayout(gb);
	__textEditor_JPanel.setMinimumSize(new Dimension(600,200));
    JTextArea ref_JTextArea = new JTextArea (2, 100);
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

/**
Sets up the monthly choices for the operational rules that use them.
@param opr the operational right to use for filling in the monthly switches.
*/
private void setupMonthlyChoices(StateMod_OperationalRight opr) {
	int ityopr = opr.getItyopr();
	List v = null;

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
				__monthSwitch_JComboBox[i].setData(v);
				__monthSwitch_JComboBox[i].setEditable(false);
				__monthSwitch_JComboBox[i].select(opr.getImonsw(i));
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
				__monthSwitch_JComboBox[i].setData(v);
				__monthSwitch_JComboBox[i].setEditable(false);
				imonsw = opr.getImonsw(i);
				__monthSwitch_JComboBox[i].select(imonsw + 1);
			}
			break;

		default:
			return;
	}
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