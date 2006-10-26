//-----------------------------------------------------------------------------
// StateMod_DataSet_JTree_Node - This class represents a node in a
//	StateMod_DataSet_JTree
//-----------------------------------------------------------------------------
// History:
// 2003-07-15	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-16	JTS, RTi		Fleshed out the initialize method for
//					all the node types.
//-----------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import RTi.Util.GUI.SimpleJTree_Node;

/**
This class represents a single node in a StateMod_DataSet_JTree and is used
to build popup menus for each node so that a node can be clicked on and the 
proper data form opened.
*/
public class StateMod_DataSet_JTree_Node 
extends SimpleJTree_Node {

/**
The kind of popup node this is (using the StateMod_DataSet component type
values).
*/
private int __popupType = -1;

/**
The popup menu for this node.
*/
private JPopupMenu __popup;

/**
The tree in which this node appears.
*/
private StateMod_DataSet_JTree __parent;

/**
Constructor.
@param name the name of the node (the text that appears)
@param type the type of node this node is (using the StateMod_DataSet component
type values).
@param parent the tree in which this node appears.
*/
public StateMod_DataSet_JTree_Node(String name, int type, 
StateMod_DataSet_JTree parent) {
	super(name);
	
	__popupType = type;
	__parent = parent;

	initialize();
}

/**
Returns the popup menu associated with this node.
@return the popup menu associated with this node.
*/
public JPopupMenu getPopup() {
	return __popup;
}

/**
Returns the type of this node.
@return the type of this node.
*/
public int getType() {
	return __popupType;
}

/**
Initialization routine that builds the proper popup menu for this node based 
on the kind of data stored in it.
*/
private void initialize() {
	__popup = new JPopupMenu();
	JMenuItem item;

	// REVISIT (JTS - 2005-01-24)
	// stop using 'text', just use 'texts' with the same code below.
	String text = null;
	Vector texts = new Vector();
	switch (__popupType) {
		case StateMod_DataSet.COMP_RESPONSE:
		case StateMod_DataSet.COMP_CONTROL:
		case StateMod_DataSet.COMP_OUTPUT_CONTROL:
			text = "Not implemented";
			break;	

		case StateMod_DataSet.COMP_STREAM_STATIONS:
		case StateMod_DataSet.COMP_STREAM_HISTORICAL_TS_MONTHLY:
		case StateMod_DataSet.COMP_STREAM_HISTORICAL_TS_DAILY:
		case StateMod_DataSet.COMP_BASEFLOW_COEFFICIENTS:
		case StateMod_DataSet.COMP_STREAM_BASEFLOW_TS_MONTHLY:
		case StateMod_DataSet.COMP_STREAM_BASEFLOW_TS_DAILY:
			text = "Not implemented";
			break;

		case StateMod_DataSet.COMP_DIVERSION_STATIONS:
			texts.add("View Diversion");
			texts.add("View Diversion Return Flows");
			texts.add("View Diversion Water Rights");
			break;
			
		case StateMod_DataSet.COMP_DIVERSION_RIGHTS:
			text = "Not implemented";
			break;

		case StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY:
		case StateMod_DataSet.COMP_DELAY_TABLES_DAILY:
			text = "View Delay Table";
			break;			

		case StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY:
		case StateMod_DataSet.COMP_DIVERSION_TS_DAILY:
		case StateMod_DataSet.COMP_DEMAND_TS_MONTHLY:
		case StateMod_DataSet.COMP_DEMAND_TS_OVERRIDE_MONTHLY:
		case StateMod_DataSet.COMP_DEMAND_TS_OVERRIDE_AVERAGE_MONTHLY:
		case StateMod_DataSet.COMP_DEMAND_TS_DAILY:
		case StateMod_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY:
		case StateMod_DataSet.COMP_IRRIGATION_WATER_REQUIREMENT_MONTHLY:
		case StateMod_DataSet.COMP_IRRIGATION_WATER_REQUIREMENT_DAILY:
		case StateMod_DataSet.COMP_SOIL_MOISTURE:
			text = "Not implemented";
			break;

		case StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY:
			text = "View Precipitation Station";
			break;
		case StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY:
			text = "View Evaporation Station";
			break;
			
		case StateMod_DataSet.COMP_RESERVOIR_STATIONS:
			texts.add("View Reservoir");
			texts.add("View Reservoir Climate Stations");
			texts.add("View Reservoir Owner Accounts");
			texts.add("View Reservoir Water Rights");
			break;
			
		case StateMod_DataSet.COMP_RESERVOIR_RIGHTS:
			text = "Not implemented";
			break;

		case StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY:
		case StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_DAILY:
		case StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY:
		case StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_DAILY:
			text = "Not implemented";
			break;
		
		case StateMod_DataSet.COMP_INSTREAM_STATIONS:
			text = "View Instream Station";
			break;

		case StateMod_DataSet.COMP_INSTREAM_RIGHTS:
			text = "Not implemented";
			break;

		case StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY:
		case StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY:
		case StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_DAILY:
			text = "Not implemented";
			break;
		
		case StateMod_DataSet.COMP_WELL_STATIONS:
			texts.add("View Well");
			texts.add("View Well Depletions");
			texts.add("View Well Return Flows");
			texts.add("View Well Water Rights");
			break;
			
		case StateMod_DataSet.COMP_WELL_RIGHTS:
			text = "Not implemented";
			break;
		
		case StateMod_DataSet.COMP_WELL_PUMPING_MONTHLY:
		case StateMod_DataSet.COMP_WELL_PUMPING_DAILY:
		case StateMod_DataSet.COMP_WELL_DEMANDS_MONTHLY:
		case StateMod_DataSet.COMP_WELL_DEMANDS_DAILY:
			text = "Not implemented";
			break;
		
		case StateMod_DataSet.COMP_RIVER_NETWORK:
			text = "Not implemented";
			break;
		
		case StateMod_DataSet.COMP_OPERATION_RIGHTS:
			text = "View Operation Right";
			break;
			
		case StateMod_DataSet.COMP_SANJUAN_RIP:
		case StateMod_DataSet.COMP_GEOVIEW:
			text = "Not implemented";
			break;

		default:
			__popup = null;
			break;
	}

	if (__popup != null) {
		if (texts.size() == 0) {		
			item = new JMenuItem(text);
			item.addActionListener(__parent);
			__popup.add(item);
		}
		else {
			for (int i = 0; i < texts.size(); i++) {
				item = new JMenuItem(
					(String)(texts.elementAt(i)));
				item.addActionListener(__parent);
				__popup.add(item);
			}
		}
	}
}

}
