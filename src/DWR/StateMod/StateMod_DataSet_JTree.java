//-----------------------------------------------------------------------------
// StateMod_DataSet_JTree - an object to display a StateMod data set in a JTree.
//-----------------------------------------------------------------------------
// History:
// 2003-07-15	J. Thomas Sapienza, RTi	Initial version from 
//					StateMod_DataSet_JTree
// 2003-07-16	JTS, RTi		Added code to open specific forms based
//					on node selections.
// 2003-07-30	Steven A. Malers, RTi	* Change COMP_STREAM_STATIONS to
//					  COMP_RIVER_STATIONS.
//					* Remove use of
//					  StateMod_DataSet_JTree_Node, as per
//					  the StateCU package - the same
//					  functionality can be achieved without
//					  the extra class.
//					* The reworked code reuses one popup
//					  rather than having a popup on each
//					  node - much less overhead.
//					* Change name of maybeShowPopup() to
//					  showPopupMenu().
// 2003-08-04	JTS, RTi		Folder icons now display for group
//					components even if they have no data.
// 2003-08-14	SAM, RTi		* Label the nodes with the identifier
//					  and description/name.
//					* For now assume monthly delay tables -
//					  need to handle daily delay tables.
// 2003-08-26	SAM, RTi		* Enable StateMod_DataSet_WindowManager.
//					* Enable river station, river baseflow
//					  station, and network.
//					* Add editable parameter to the
//					  constructor.
// 2003-08-29	SAM, RTi		Update for separate daily and monthly
//					delay table groups.
// 2003-09-11	SAM, RTi		Update due to changes in the naming
//					of river station components.
// 2003-09-18	SAM, RTi		Change so popup menus are not shown
//					if a group's primary component does
//					not have data.
// 2003-09-29	SAM, RTi		* Move formatNodeLabel() to
//					  StateMod_Util.formatDataLabel().
//					* Add refresh() and
//					  displayDataSetComponent() for use with
//					  adding and deleting data.
//					* Add to the popup a "Summarize use in
//					  data set" option to help
//					  users/developers understand the
//					  relationships between data.
// 2006-03-06	JTS, RTi		When diversions are summarized, the
//					main diversions window is no longer 
//					opened.
// 2006-03-07	JTS, RTi		When refreshing the tree, fast add is
//					used.
// 2006-08-22	SAM, RTi		Add plans.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import DWR.StateCU.StateCU_Data;
import RTi.GRTS.TSViewJFrame;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.GUI.SimpleJTree;
import RTi.Util.GUI.SimpleJTree_Node;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This StateMod_DataSet_JTree class displays a StateMod_DataSet and its components
in a JTree.  It can be constructed to show all the data, or just the high-level
objects.
*/
public class StateMod_DataSet_JTree extends SimpleJTree
implements ActionListener, MouseListener
{

private final String __SUMMARIZE_HOW1 = "Summarize how ";
private final String __SUMMARIZE_HOW2 = " is used";
					// String used in popup menus - checked
					// for in actionPerformed().
private final String __PROPERTIES = " Properties";
					// String used in popup menus - checked
					// for in actionPerformed().

/**
Whether data objects should be listed in the tree (true) or only the 
top-level components should be (false).
*/
private boolean __display_data_objects = false;

/**
Whether the data in the tree are editable or not.  For example, StateDMI will
set as false but StateMod GUI will set as true.
*/
private boolean __editable = false;

/**
Stores the folder icon to be used for group components with no data.
*/
private Icon __folderIcon = null;

private JPopupMenu __popup_JPopupMenu;		// A single popup menu that is
						// used to provide access to
						// other features from the tree.
						// The single menu has its
						// items added/removed as
						// necessary based on the state
						// of the tree.

/**
The dataset to be displayed.
*/
private StateMod_DataSet __dataset = null;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

/**
The node that last opened a popup menu.
*/
private SimpleJTree_Node __popup_Node;

/**
Construct a StateMod_DataSet_JFrame.
@param parent JFrame from which this instance is constructed.
@param dataset StateMod_DataSet that is being displayed/managed.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param display_data_objects If true, data objects are listed in the tree.  If
@param editable If true, data objects can be edited.
false, only the top-level data set components are listed.
*/
public StateMod_DataSet_JTree (	JFrame parent, StateMod_DataSet dataset,
				StateMod_DataSet_WindowManager dataset_wm,
				boolean display_data_objects,
				boolean editable )
{	__dataset = dataset;
	__dataset_wm = dataset_wm;
	__display_data_objects = display_data_objects;
	__editable = editable;

	__folderIcon = getClosedIcon();
	
	showRootHandles(true);
	addMouseListener(this);
	setLeafIcon(null);
	setTreeTextEditable(false);
	__popup_JPopupMenu = new JPopupMenu();
}

/**
Construct a StateMod_DataSet_JFrame.  The data set should be set when available
using setDataSet() and setDataSetWindowManager().
@param parent JFrame from which this instance is constructed.
@param display_data_objects If true, data objects are listed in the tree.  If
false, only the top-level data set components are listed.
@param editable If true, data objects can be edited.
*/
public StateMod_DataSet_JTree ( JFrame parent, boolean display_data_objects,
				boolean editable )
{	__display_data_objects = display_data_objects;
	__editable = editable;
	__folderIcon = getClosedIcon();

	showRootHandles(true);
	addMouseListener(this);
	setLeafIcon(null);
	setTreeTextEditable(false);
	__popup_JPopupMenu = new JPopupMenu();
}

/**
Set the data set to be displayed.
@param dataset StateMod_DataSet that is being displayed/managed.
*/
public void setDataSet ( StateMod_DataSet dataset )
{	__dataset = dataset;
}

/**
Set the data set window manager.
@param dataset_wm StateMod_DataSet_WindowManager that is being used to
display/manage the data set.
*/
public void setDataSetWindowManager ( StateMod_DataSet_WindowManager dataset_wm)
{	__dataset_wm = dataset_wm;
}

/**
Responds to action performed events sent by popup menus of the tree nodes.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event)
{	String action = event.getActionCommand();
	String routine = "StateMod_DataSet_JTree.actionPerformed";

	Object data = __popup_Node.getData();

	if ( data instanceof DataSetComponent ) {
		DataSetComponent comp = (DataSetComponent)data;
		int comp_type = comp.getComponentType();
		if ( comp_type == StateMod_DataSet.COMP_CONTROL_GROUP ){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.WINDOW_CONTROL,
				__editable );
		}
		else if ( comp_type == StateMod_DataSet.COMP_STREAMGAGE_GROUP ){
			__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE,
				__editable );
		}
		else if (comp_type ==
			StateMod_DataSet.COMP_DELAY_TABLE_MONTHLY_GROUP){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DELAY_TABLE_MONTHLY, __editable );
		}
		else if (comp_type ==
			StateMod_DataSet.COMP_DELAY_TABLE_DAILY_GROUP){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DELAY_TABLE_DAILY, __editable );
		}
		else if ( comp_type == StateMod_DataSet.COMP_DIVERSION_GROUP ){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DIVERSION, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_PRECIPITATION_GROUP){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_PRECIPITATION, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_EVAPORATION_GROUP){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_EVAPORATION, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_RESERVOIR_GROUP ) {
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_RESERVOIR, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_INSTREAM_GROUP ) {
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_INSTREAM, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_WELL_GROUP ) {
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_WELL, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_PLAN_GROUP ) {
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_PLAN, __editable );
		}
		else if (comp_type==StateMod_DataSet.COMP_STREAMESTIMATE_GROUP){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_STREAMESTIMATE, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_RIVER_NETWORK_GROUP){
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_RIVER_NETWORK, __editable );
		}
		else if ( comp_type==StateMod_DataSet.COMP_OPERATION_GROUP ) {
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_OPERATIONAL_RIGHT, __editable );
		}
	}
	// Below here are specific instances of objects.  Similar to above,
	// display the main window but then also select the specific object...
	else if ( data instanceof StateMod_StreamGage ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE,
			__editable);
		((StateMod_StreamGage_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.WINDOW_STREAMGAGE )).
			selectID ( ((StateMod_StreamGage)data).getID() );
	}
	else if ( data instanceof StateMod_DelayTable ) {
		StateMod_DelayTable dt = (StateMod_DelayTable)data;
		if ( dt.isMonthly() ) {
			__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DELAY_TABLE_MONTHLY, __editable );
				((StateMod_DelayTable_JFrame)
				__dataset_wm.getWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DELAY_TABLE_MONTHLY )).selectID (
				dt.getID() );
		}
		else {	__dataset_wm.displayWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DELAY_TABLE_DAILY, __editable );
				((StateMod_DelayTable_JFrame)
				__dataset_wm.getWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DELAY_TABLE_DAILY )).selectID (
				dt.getID() );
		}
	}
	else if ( data instanceof StateMod_Diversion ) {
		if ( action.indexOf ( __SUMMARIZE_HOW1 ) >= 0 ) {
			PropList props = new PropList ( "Diversion" );
			props.set ( "Title=" +
			((StateMod_Diversion)data).getID() +
			" Diversion use in Data Set" );
			new ReportJFrame (
				__dataset.getDataObjectDetails(
				StateMod_DataSet.COMP_DIVERSION_STATIONS,
				((StateMod_Diversion)data).getID() ), props );
		}
		else {
			// Assume properties...
			__dataset_wm.displayWindow (StateMod_DataSet_WindowManager.WINDOW_DIVERSION,__editable);
			((StateMod_Diversion_JFrame)__dataset_wm.getWindow (
				StateMod_DataSet_WindowManager.
				WINDOW_DIVERSION )).selectID (((StateMod_Diversion)data).getID() );
		}
	}
	else if ( data instanceof TS ) {
		// Might be precipitation or evaporation.  Check the data type
		// to determine...
		TS ts = (TS)data;
		PropList props = new PropList ( "Precipitation/Evaporation" );
		if ( action.indexOf ( __SUMMARIZE_HOW1 ) >= 0 ) {
			if (	StringUtil.startsWithIgnoreCase(
				ts.getDataType(),"e") ) {
				props.set ( "Title=" + ts.getLocation() +
				" Evaporation TS use in Data Set" );
				new ReportJFrame (
				__dataset.getDataObjectDetails(
				StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY,
				ts.getLocation() ), props );
			}
			else if(StringUtil.startsWithIgnoreCase(
				ts.getDataType(),"p") ) {
				props.set ( "Title=" + ts.getLocation() +
				" Precipitation TS use in Data Set" );
				new ReportJFrame (
				__dataset.getDataObjectDetails(
				StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY,
				ts.getLocation() ), props );
			}
		}
		else if ( action.indexOf ( __PROPERTIES ) >= 0 ) {
			if (	StringUtil.startsWithIgnoreCase(
				ts.getDataType(),"e") ) {
				props.set ( "Title=Evaporation" );
			}
			else if ( StringUtil.startsWithIgnoreCase(
				ts.getDataType(),"p") ) {
				props.set ( "Title=Precipitation" );
			}
			props.set ( "InitialView=Graph" );
			props.set ( "GraphType=Bar" );
			List tslist = new Vector(1);
			tslist.add ( ts );
			try {	new TSViewJFrame ( tslist, props );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Error displaying data." );
			}
		}
	}
	else if ( data instanceof StateMod_Reservoir ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_RESERVOIR, __editable );
		((StateMod_Reservoir_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_RESERVOIR )).selectID (
			((StateMod_Reservoir)data).getID() );
	}
	else if ( data instanceof StateMod_InstreamFlow ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_INSTREAM, __editable );
		((StateMod_InstreamFlow_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_INSTREAM )).selectID (
			((StateMod_InstreamFlow)data).getID() );
	}
	else if ( data instanceof StateMod_Well ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_WELL, __editable );
		((StateMod_Well_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_WELL )).selectID (
			((StateMod_Well)data).getID() );
	}
	else if ( data instanceof StateMod_Plan ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_PLAN, __editable );
		((StateMod_Plan_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_PLAN )).selectID (
			((StateMod_Plan)data).getID() );
	}
	else if ( data instanceof StateMod_StreamEstimate ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_STREAMESTIMATE, __editable );
		((StateMod_StreamEstimate_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_STREAMESTIMATE )).selectID (
			((StateMod_StreamEstimate)data).getID() );
	}
	else if ( data instanceof StateMod_RiverNetworkNode ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_RIVER_NETWORK, __editable );
		((StateMod_RiverNetworkNode_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_RIVER_NETWORK )).selectID (
			((StateMod_RiverNetworkNode)data).getID() );
	}
	else if ( data instanceof StateMod_OperationalRight ) {
		__dataset_wm.displayWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_OPERATIONAL_RIGHT, __editable );
		((StateMod_OperationalRight_JFrame)__dataset_wm.getWindow (
			StateMod_DataSet_WindowManager.
			WINDOW_OPERATIONAL_RIGHT )).selectID (
			((StateMod_OperationalRight)data).getID() );
	}
}

/**
Clear all data from the tree.
*/
public void clear() {
	String routine = "StateMod_DataSet_JTree.clear";
	SimpleJTree_Node node = getRoot();
	List v = getChildrenList(node);
	int size = 0;
	if (v != null) {
		size = v.size();
	}

	for (int i = 0; i < size; i++) {
		try {	removeNode((SimpleJTree_Node)v.get(i), false);
		}
		catch (Exception e) {
			Message.printWarning(2, routine,
				"Cannot remove node " + node.toString());
			Message.printWarning(2, routine, e);
		}
	}
}

/**
Display all the information in the data set.  This can be called, for example,
after a data set has been read.
*/
public void displayDataSet()
{	String routine = "StateMod_DataSet_JTree.displayDataSet";
	List v = __dataset.getComponentGroups();
	int size = 0;
	if (v != null) {
		size = v.size();
	}
	SimpleJTree_Node node = null, node2 = null;
	DataSetComponent comp = null;
	boolean hadData = false;
	boolean isGroup = false;
	int type;
	// Add each component group...
	setFastAdd(true);
	Icon folder_Icon = getClosedIcon();
	for (int i = 0; i < size; i++) {
		hadData = false;
		isGroup = false;
		comp = (DataSetComponent)v.get(i);
		if ( (comp == null) || !comp.isVisible()) {
			continue;
		}
		type = comp.getComponentType();
		if ( type == StateMod_DataSet.COMP_GEOVIEW_GROUP ) {
			// Don't want to list the groups because there is no
			// way to display edit (or they are displayed
			// elsewhere)...
			continue;
		}
		node = new SimpleJTree_Node(comp.getComponentName());
		node.setData(comp);

		if (comp.isGroup()) {
			isGroup = true;
		}
		
		// To force groups to be folders, even if no data underneath...
		node.setIcon(folder_Icon);
		try {	addNode(node);
		}
		catch (Exception e) {
			Message.printWarning(2, routine,
				"Error adding component group "
				+ comp.getComponentName());
			Message.printWarning(2, routine, e);
			continue;
		}
		if ( __display_data_objects ) {
			// Display the primary object in each group
			hadData = displayDataSetComponent ( comp, node );
		}
		else {	// Add the components in the group...
			List v2 = (List)comp.getData();
			int size2 = 0;
			if (v2 != null) {
				size2 = v2.size();
			}
			for (int j = 0; j < size2; j++) {
				comp = (DataSetComponent)v2.get(j);
				if ( (comp == null) || !comp.isVisible()) {
					continue;
				}
				node2 = new SimpleJTree_Node(
					comp.getComponentName() );
				node2.setData(comp);
				try {	addNode(node2, node);
				}
				catch (Exception e) {
					Message.printWarning(2, routine,
						"Error adding component "
					+ comp.getComponentName());
					Message.printWarning(2, routine, e);
					continue;
				}
			}
			if (size2 > 0) {
				hadData = true;
			}
		}
		if (isGroup && !hadData) {
			node.setIcon(__folderIcon);
		}
	}
	setFastAdd(false);
}

/**
Display the primary data for a component.  This method is called when adding
nodes under a group node.
@param comp Component to display data.
@param node Parent node to display under.
*/
private boolean displayDataSetComponent (	DataSetComponent comp,
						SimpleJTree_Node node )
{	String routine = "StateMod_DataSet_JTree.displayDataSetComponent";
	boolean hadData = false;	// No data for component...
	String label = "";
	int primary_type = __dataset.lookupPrimaryComponentTypeForComponentGroup
				(comp.getComponentType());
	if (primary_type >= 0) {
		comp = __dataset.getComponentForComponentType( primary_type);
	}
	// Revisit - later may enable even if a component does
	// not have data - for example have an "Add" popup...
	if ( (comp == null) || !comp.isVisible() || !comp.hasData() ) {
		return hadData;
	}
	Object data_Object = comp.getData();
	if ( data_Object == null ) {
		return hadData;
	}
	List data = null;
	if (data_Object instanceof List) {
		data = (List)comp.getData();
	}
	else {	// Continue (REVISIT - what components would
		// this happen for?)...
		Message.printWarning ( 2, routine,
		"Unexpected non-Vector for " + comp.getComponentName() );
		return hadData;
	}
	StateCU_Data cudata;
	StateMod_Data smdata;
	SimpleJTree_Node node2 = null;
	TS tsdata;
	int dsize = 0;
	if (data != null) {
		dsize = data.size();
	}
	for (int idata = 0; idata < dsize; idata++) {
		data_Object = data.get(idata);
		if ( data_Object instanceof StateMod_Data ) {
			smdata = (StateMod_Data)data.get(idata);
			label = StateMod_Util.formatDataLabel ( smdata.getID(),
				smdata.getName() );
			node2 = new SimpleJTree_Node( label );
			node2.setData(smdata);
		}
		else if ( data_Object instanceof StateCU_Data ){
			cudata = (StateCU_Data)data.get(idata);
			label = StateMod_Util.formatDataLabel ( cudata.getID(),
				cudata.getName() );
			node2 = new SimpleJTree_Node( label );
			node2.setData(cudata);
		}
		else if ( data_Object instanceof TS ) {
			tsdata = (TS)data.get(idata);
			label = StateMod_Util.formatDataLabel (
				tsdata.getLocation(),
				tsdata.getDescription() );
			node2 = new SimpleJTree_Node( label );
			node2.setData(tsdata);
		}
		try {	addNode(node2, node);
		}
		catch (Exception e) {
			Message.printWarning(2,
			routine, "Error adding data \"" + label + "\"" );
			Message.printWarning(2, routine, e);
			continue;
		}
	}
	if ( dsize > 0 ) {
		hadData = true;
	}
	// Collapse the node because the lists are
	// usually pretty long...
	try {	collapseNode(node);
	}
	catch (Exception e) {
		// Ignore.
	}
	return hadData;	// Needed in the calling code.
}

/**
Responds to mouse clicked events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent event) {}

/**
Responds to mouse dragged events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {}

/**
Responds to mouse entered events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent event) {}

/**
Responds to mouse exited events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseExited(MouseEvent event) {}

/**
Responds to mouse moved events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseMoved(MouseEvent event) {}

/**
Responds to mouse pressed events; does nothing.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {}

/**
Responds to mouse released events and possibly shows a popup menu.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	showPopupMenu(event);
}

/**
Refresh a part of the JTree based on the component.  This method is only
designed to work with the detailed display.  It is currently assumed that all
components are represented in the tree, even if no data are listed below the
group node.
@param comp_type Component type being refreshed.  Use the component groups.
*/
public void refresh ( int comp_type )
{	String routine = "StateMod_DataSet_JTree.refresh";
	if ( !__display_data_objects ) {
		return;
	}
	DataSetComponent comp =
		__dataset.getComponentForComponentType ( comp_type );
	// Find the node...
	SimpleJTree_Node node = findNodeByName(comp.getComponentName());
	if ( node == null ) {
		return;
	}
	// Remove the sub-nodes...
	try {	removeChildren(node);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error removing old nodes - error should not occur." );
	}
	// Now redraw the data...
	setFastAdd(true);
	displayDataSetComponent ( comp, node );
	setFastAdd(false);
}

/**
Checks to see if the mouse event would trigger display of the popup menu.
The popup menu does not display if it is null.
@param e the MouseEvent that happened.
*/
private void showPopupMenu(MouseEvent e)
{	String routine = "StateMod_DataSet_JTree.showPopupMenu";
	if ( !e.isPopupTrigger() || !__display_data_objects ) {
		// Do not do anything...
		return;
	}
	TreePath path = getPathForLocation(e.getX(), e.getY());	
	if (path == null) {
		return;
	}
	__popup_Node = (SimpleJTree_Node)path.getLastPathComponent();
	// First remove the menu items that are currently in the menu...
	__popup_JPopupMenu.removeAll();
	Object data = null;		// Data object associated with the node
	DataSetComponent comp2;		// Used to check components in groups.
	// Now reset the popup menu based on the selected node...
	if ( __display_data_objects ) {
		// Get the data for the node.  If the node is a data object,
		// the type can be checked to know what to display.
		// The tree is displaying data objects so the popup will show
		// specific JFrames for each data group.  If the group folder
		// was selected, then display the JFrame showing the first item
		// selected.  If a specific data item in the group was selected,
		// then show the specific data item.
		JMenuItem item;
		data = __popup_Node.getData();
		if ( data instanceof DataSetComponent ) {
			// Specific checks need to be done to identify the
			// component group...

			DataSetComponent comp = (DataSetComponent)data;
			int comp_type = comp.getComponentType();

			if ( comp_type == StateMod_DataSet.COMP_CONTROL_GROUP ){
				// For now display the control file information
				// only...
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_CONTROL);
				if ( comp2.hasData() ) {
					item = new SimpleJMenuItem (
						"Control Properties",
						this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if (comp_type ==
				StateMod_DataSet.COMP_STREAMGAGE_GROUP ) {
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_STREAMGAGE_STATIONS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Stream Gage Station Properties", this);
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_DELAY_TABLE_MONTHLY_GROUP ) {
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_DELAY_TABLES_MONTHLY);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Delay Table Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_DELAY_TABLE_DAILY_GROUP ) {
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_DELAY_TABLES_MONTHLY);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Delay Table Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_DIVERSION_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_DIVERSION_STATIONS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Diversion Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if((comp_type == StateMod_DataSet.
				COMP_PRECIPITATION_GROUP) ) {
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_PRECIPITATION_TS_MONTHLY);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
						"Precipitation Properties",
						this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_EVAPORATION_GROUP ) {
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_EVAPORATION_TS_MONTHLY);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Evaporation Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_RESERVOIR_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_RESERVOIR_STATIONS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Reservoir Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_INSTREAM_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_INSTREAM_STATIONS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Instream Flow Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}
			else if(comp_type == StateMod_DataSet.COMP_WELL_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_WELL_STATIONS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Well Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}
			else if(comp_type == StateMod_DataSet.COMP_PLAN_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_PLANS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Plan Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}
			else if(comp_type ==
				StateMod_DataSet.COMP_STREAMESTIMATE_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.
					COMP_STREAMESTIMATE_STATIONS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Stream Estimate Station Properties",
					this );
					__popup_JPopupMenu.add ( item );
				}
			}
			else if(comp_type == StateMod_DataSet.
				COMP_RIVER_NETWORK_GROUP){
				// Only add if data are available...
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_RIVER_NETWORK);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"River Network Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}

			else if(comp_type == StateMod_DataSet.
				COMP_OPERATION_GROUP){
				comp2 =	__dataset.getComponentForComponentType(
					StateMod_DataSet.COMP_OPERATION_RIGHTS);
				if ( (comp2 != null) && comp2.hasData() ) {
					item = new SimpleJMenuItem (
					"Operation Rights Properties", this );
					__popup_JPopupMenu.add ( item );
				}
			}
		}
		// The data are a specific data instance so display the
		// properties for the specific item if a primary data item.

		// Control... nothing for now.

		else if ( data instanceof StateMod_StreamGage ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_DelayTable ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_Diversion ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
			__popup_JPopupMenu.add ( new SimpleJMenuItem (
				__SUMMARIZE_HOW1 + "\"" +__popup_Node.getText()+
				"\"" + __SUMMARIZE_HOW2, this ));
		}
		else if ( data instanceof MonthTS ) {
			// Precipitation or evaporation time series...
			__popup_JPopupMenu.add ( new SimpleJMenuItem (
				__popup_Node.getText() + __PROPERTIES, this ));
			__popup_JPopupMenu.add ( new SimpleJMenuItem (
				__SUMMARIZE_HOW1 + "\"" +__popup_Node.getText()+
				"\"" + __SUMMARIZE_HOW2, this ));
		}
		else if ( data instanceof StateMod_Reservoir ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_InstreamFlow ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_Well ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_Plan ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_StreamEstimate ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_OperationalRight ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		else if ( data instanceof StateMod_RiverNetworkNode ) {
			item = new SimpleJMenuItem (
				__popup_Node.getText() + " Properties", this );
			__popup_JPopupMenu.add ( item );
		}
		// Others (e.g., San Juan Sediment) supported later....
		else {	Message.printWarning ( 2, routine,
			"Node data is not recognized" );
			return;
		}
	}
	// Now display the popup so that the user can select the appropriate
	// menu item...
	Point pt = JGUIUtil.computeOptimalPosition (
		e.getPoint(), e.getComponent(), __popup_JPopupMenu );
	__popup_JPopupMenu.show(e.getComponent(), pt.x, pt.y);
}

} // End of StateMod_DataSet_JTree
