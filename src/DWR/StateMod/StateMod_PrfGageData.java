// StateMod_PrfGageData - proration factor information, used by StateDMI when
// processing StateMod_StreamEstimate_Coefficients

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

import java.util.List;

import RTi.Util.Message.Message;

/**
This class stores proration factor data for use by StateDMI when processing
StateMod_StreamEstimate_Coeffients data.
*/
public class StateMod_PrfGageData 
extends StateMod_Data
{

/**
Name gage to use for data.
*/
private String __gageID = StateMod_Data.MISSING_STRING;

/**
Name ID of node to reset.
*/
private String __nodeID = StateMod_Data.MISSING_STRING;

/**
Pointer to gage node, cast as HydroBase_Node, when processed by
HydroBase_NodeNetwork code.
*/
private Object __gageNode = null;

/**
Pointer to node, cast as HydroBase_Node, when processed by
HydroBase_NodeNetwork code.
*/
private Object __node = null;

/**
Construct and initialize with empty identifiers, and null nodes.
*/
public StateMod_PrfGageData ()
{	__nodeID = "";
	setID ( "" );
	__gageID = "";
	__node = null;
	__gageNode = null;
}

/**
Construct and initialize with empty identifiers, and null nodes.
@param nodeID Identifier for the node.
@param gageID Identifier for the gage to supply data.
*/
public StateMod_PrfGageData ( String nodeID, String gageID )
{	__nodeID = nodeID;
	setID ( nodeID );
	__gageID = gageID;
	__node = null;
	__gageNode = null;
}

/**
Returns the gage identifier.
@return the gage identifier.
*/
public String getGageID () {
	return __gageID;
}

/**
Returns the gage node.
@return the gage node.
*/
public Object getGageNode () {
	return __gageNode;
}

/**
Returns the node.
@return the node.
*/
public Object getNode ()
{	return __node;
}

/**
@return the node identifier.
*/
public String getNodeID ()
{	return __nodeID;
}

/**
Checks whether the node is a gage that is to supply proration data.
@param id the identifier for the station/node to check.
@param prfGageData vector of prf gage data 
@return true if the node is gage that supplies proration data, false if not.
*/
public static boolean isSetprfSource ( String id, List<StateMod_PrfGageData> prfGageData)
{	String routine = "StateMod_PrfGageData.isSetprfSource";
	int dl = 10;

	int numPrfGageData = 0;
	if (prfGageData != null) {
		numPrfGageData = prfGageData.size();
	}
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Looking through " + numPrfGageData 
			+ " prfGageData's for " + id );
	}

	StateMod_PrfGageData prfGageData_j;
	for (int j = 0; j < numPrfGageData; j++) {
		prfGageData_j = prfGageData.get(j);
		if (prfGageData_j.getGageID().equalsIgnoreCase( id ) ) {
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,
					"Found a prfGageData gage \""
					+ id + "\".");
			}
			return true;
		}
	}
	return false;
}

/**
Checks to see if the requested node is the target of a "set proration factor gage" (see StateDMI
SetStreamEstimateCoefficientsPFGage() command).
@param commonID the id of the node to check
@param prfGageData list of proration gage data
@return the index of in the PrfGageData that has a node ID that matches the specified commonID, or -1 if
the node wasn't found.
*/
public static int isSetprfTarget ( String commonID, List<StateMod_PrfGageData> prfGageData )
{	String routine = "StateMod_NodeNetwork.isSetprfTarget";
	int dl = 10;

	int numPrfGageData = 0;
	if (prfGageData != null) {
		numPrfGageData = prfGageData.size();
	}
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Looking through " + numPrfGageData + " prfGageData's for " + commonID);
	}

	StateMod_PrfGageData prfGageData_j = null;
	for (int j = 0; j < numPrfGageData; j++) {
		prfGageData_j = prfGageData.get(j);
		if (prfGageData_j.getNodeID().equalsIgnoreCase(commonID) ) {
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Found a prfGageData target structure \"" + commonID + "\".");
			}
			return j;
		}
	}
	return -1;
}

/**
Set the gage identifier.
@param gageid Gage identifier.
*/
public void setGageID (String gageid) {
	if (gageid != null) {
		__gageID = gageid;
	}
}

/**
Set the gage node.
@param gagenode Gage node.
*/
public void setGageNode ( Object gagenode )
{	__gageNode = gagenode;
}

/**
Set the node.
@param node Node.
*/
public void setNode ( Object node)
{	__node = node;
}

/**
Set the node identifier.
@param nodeid Node identifier.
*/
public void setNodeID (String nodeid) {
	if (nodeid != null) {
		__nodeID = nodeid;
		setID ( nodeid );
	}
}

}
