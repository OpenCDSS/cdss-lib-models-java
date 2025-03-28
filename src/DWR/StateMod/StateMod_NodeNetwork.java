// StateMod_NodeNetwork - data used when reading the old Makenet file.

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// Before 2017-07-01 used Xerces but Java is now distributed with embedded Xerces
//import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Java 8.
//import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import RTi.DMI.DMIUtil;
import RTi.GR.GRLimits;
import RTi.GR.GRText;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

import cdss.domain.hydrology.network.HydrologyNode;
import cdss.domain.hydrology.network.HydrologyNodeNetwork;
import cdss.domain.hydrology.network.RiverLine;

public class StateMod_NodeNetwork extends HydrologyNodeNetwork
{
/**
 * Data used when reading the old Makenet file.
 * FIXME SAM 2008-03-15 Need to evaluate whether this can be in read code only.
 */

/**
Counter for the number of reaches closed for processing (to count matches for __openCount).
*/
private int __closeCount;

/**
The counter for the line in the input file.  
*/
private int __line;

/**
The number of reaches opened for processing.
*/
private int __openCount;

/**
The number of reaches processed.
*/
private int __reachCounter;

// End Makenet data

/**
Whether to generate fancy node descriptions or not.
*/
private boolean	__createFancyDescription;

/**
Whether to create output files or not.
*/
private boolean	__createOutputFiles;

/**
Construct a StateMod_NodeNetwork but do not add an end node.
*/
public StateMod_NodeNetwork ()
{
	this (false);
}

/**
Construct a StateMod_NodeNetwork.
@param addEndNode if true an end node will automatically be added at initialization.
*/
public StateMod_NodeNetwork(boolean addEndNode)
{
	super ( addEndNode);
	initialize();
}

/**
Append a network to this network.  The process taken is to reduce the data to a list of nodes and then
recalculate the node connectivity.  This uses data and methods that mix HydrologyNode and ID representations
of the network since it is a mix of processing raw data from existing networks.
@param networkToAppend a network to append to this existing network
@param appendEndAs the identifier for a node in the current network, which will become the downstream
node for the appended network
*/
public StateMod_NodeNetwork append ( StateMod_NodeNetwork networkToAppend,
	StateMod_NodeNetwork_AppendHowType appendHowType, String existingDownstreamNodeID,
	String appendedUpstreamNodeID, Double scaleXY, Double shiftX, Double shiftY )
{	String routine = getClass().getName() + ".append";
	// The network will already have been read in and cleaned up.  Therefore just extract the
	// nodes and replace the end node with the node matching the "appendEndAs" identifier, which must
	// exist in the current network.
	
	// Find the new end node.
	HydrologyNode existingDownstreamNode = findNode ( existingDownstreamNodeID );
	if ( existingDownstreamNode == null ) {
		throw new RuntimeException ( "Unable to match node \"" + existingDownstreamNodeID +
			"\" in existing network to serve as downstream node for appended network.");
	}
	// Find the starting node on the network to be appended.
	HydrologyNode appendedUpstreamNode = networkToAppend.findNode (appendedUpstreamNodeID);
	if ( appendedUpstreamNode == null ) {
		throw new RuntimeException ( "Unable to find appended upstream node \"" + appendedUpstreamNodeID + "\".");
	}
	// Verify that the existing node does not also exist in the other network.
	/*
	HydrologyNode endNode = networkToAppend.getNodeHead();
	if ( endNode == null ) {
		throw new RuntimeException ( "Unable to get downstream (end) node for appended network.");
	}
	*/
	
	// The following logic is similar to that in the readXMLNetworkFile() method.  Basically the lists of
	// nodes, labels, etc., are combined into one big list and then the internal network is reconstructed
	// to assign network navigation information.  The items being appended are also repositioned to align
	// with the joined network.  Finally, overall limits are reset.
	
	// Get the original node lists...
	
	List<HydrologyNode> networkNodeList = this.getNodeList(); // List of all nodes read
	List<PropList> networkLinkList = this.getLinkList(); // List of all links read (lines from one node to another)
	List<PropList> networkLayoutList = this.getLayoutList(); // List of all layouts
	List<HydrologyNode> networkAnnotationList = this.getAnnotationList(); // All annotations.
	
	// Get the lists to append...
	
	List<HydrologyNode> appendNetworkNodeList = networkToAppend.getNodeList();
	List<PropList> appendNetworkLinkList = networkToAppend.getLinkList();
	//List<PropList> appendNetworkLayoutList = networkToAppend.getLayoutList();
	List<HydrologyNode> appendNetworkAnnotationList = networkToAppend.getAnnotationList();
	// Get the list of confluences, which have internally assigned identifiers like "CONFL_1"
	// (although the first one may just be "CONFL"
	// It is likely that there are duplicate identifiers so find the maximum confluence ID number
	// and change all the confluences in the network being appended to continue the series.
	int [] types = { HydrologyNode.NODE_TYPE_CONFLUENCE };
	List<String> conflIDList = new Vector<String>();
	try {
		conflIDList = getNodeIdentifiersByType(types);
	}
	catch ( Exception e ) {
		throw new RuntimeException ( e );
	}
	int max = 1;
	for ( String conflID: conflIDList ) {
		int pos = conflID.indexOf("_");
		if ( pos > 0 ) {
			max = Math.max(max,Integer.parseInt(conflID.substring(pos + 1)));
		}
	}
	// Get the confluence nodes in the network to append...
	List<String> conflIDAppendList = new Vector<String>();
	try {
		conflIDAppendList = networkToAppend.getNodeIdentifiersByType(types);
	}
	catch ( Exception e ) {
		throw new RuntimeException ( e );
	}
	Collections.sort(conflIDAppendList);
	// Create a hash and define the translation
	Hashtable<String,String> confIDLookup = new Hashtable<String,String>();
	int conflNum = max + 1;
	Message.printStatus(2,routine,
		"Renumbering confluence nodes in appended network to start with CONFL_" + conflNum);
	for ( String conflIDAppend: conflIDAppendList ) {
		// Remove the number and replace with another
		int pos = conflIDAppend.indexOf("_");
		if ( pos < 0 ) {
			// CONFL
			confIDLookup.put(conflIDAppend,conflIDAppend + "_" + conflNum );
		}
		else {
			// CONFL_N
			confIDLookup.put(conflIDAppend,conflIDAppend.substring(0,pos) + "_" + conflNum );
		}
		++conflNum;
	}
	// Finally, loop through all the nodes and replace the confluence identifiers with non-conflicting values
	String id;
	Object oHashValue;
	for ( HydrologyNode nodeToAppend: appendNetworkNodeList ) {
		id = nodeToAppend.getCommonID();
		oHashValue = confIDLookup.get(id);
		if ( oHashValue != null ) {
			// Have a match...
			nodeToAppend.setCommonID((String)oHashValue);
		}
		id = nodeToAppend.getNetID(); // Legacy...
		oHashValue = confIDLookup.get(id);
		if ( oHashValue != null ) {
			// Have a match...
			nodeToAppend.setNetID((String)oHashValue);
		}
		// Also process the node upstream and downstream IDs, which are maintained as strings during the
		// network build process...
		// Upstream...
		String [] upstreamIDs = nodeToAppend.getUpstreamNodeIDs();
		if ( (upstreamIDs != null) && (upstreamIDs.length > 0) ) {
			nodeToAppend.clearUpstreamNodeIDs();
			for ( int i = 0; i < upstreamIDs.length; i++ ) {
				id = upstreamIDs[i];
				if ( id != null ) {
					oHashValue = confIDLookup.get(id);
					if ( oHashValue != null ) {
						// Have a match...
						nodeToAppend.addUpstreamNodeID((String)oHashValue);
					}
				}
			}
		}
		// Downstream...
		id = nodeToAppend.getDownstreamNodeID();
		if ( id != null ) {
			oHashValue = confIDLookup.get(id);
			if ( oHashValue != null ) {
				// Have a match...
				nodeToAppend.setDownstreamNodeID((String)oHashValue);
			}
		}
	}
	
	// Calculate the coordinate offset such that the end node would exactly overly the node that will
	// replace it.  The offset can be added to the coordinates in the new network.
	// This aligns the appended network such that the appended node is at coordinate (0,0) of its space,
	// and it will subsequently be scaled to match the scaling of the existing network.
	double shiftXAlignNetworks1 = -appendedUpstreamNode.getX();
	double shiftYAlignNetworks1 = -appendedUpstreamNode.getY();
	double shiftXAlignNetworks2 = existingDownstreamNode.getX();
	double shiftYAlignNetworks2 = existingDownstreamNode.getY();
	double scale = 1.0; // TODO SAM 2011-01-04 Compute from average node spacing?
	if ( scaleXY != null ) {
		scale = scaleXY;
	}
	double shiftXAdditional = 0.0; // Additional offset needed to position whole block of new nodes
	double shiftYAdditional = 0.0;
	if ( shiftX != null ) {
		shiftXAdditional = shiftX;
	}
	if ( shiftY != null ) {
		shiftYAdditional = shiftY;
	}
	Message.printStatus(2, routine, "Shift to set coordinates of append node to zero = " +
		shiftXAlignNetworks1 + "," + shiftYAlignNetworks1 );
	Message.printStatus(2, routine, "Scale to apply to appended network = " + scale );
	Message.printStatus(2, routine, "Shift to set coordinates of append point to downstream = " +
		shiftXAlignNetworks2 + "," + shiftYAlignNetworks2 );
	Message.printStatus(2, routine, "Additional shift to apply to appended network = " +
		shiftXAdditional + "," + shiftYAdditional );
	
	// Adjust the merge point nodes

	if ( appendHowType == StateMod_NodeNetwork_AppendHowType.ADD_UPSTREAM_OF_DOWNSTREAM ) {
		// Remove all nodes downstream in the appended network.  Because this code will rebuild the
		// network below, just do this brute force without properly recalculating the navigation data.
		// Start with the node downstream of the requested node...
		append_RemoveNodesDownsteamOfAppendNode ( routine, appendedUpstreamNode, appendNetworkNodeList );
		appendedUpstreamNode.setDownstreamNodeID(null);
		// Now reset the downstream node of the append point
		appendedUpstreamNode.setDownstreamNode(existingDownstreamNode);
		appendedUpstreamNode.setDownstreamNodeID(existingDownstreamNode.getCommonID());
		Message.printStatus(2,routine,"Setting append node \"" + appendedUpstreamNode.getCommonID() +
			"\" downstream to: " + existingDownstreamNode.getCommonID() );
		if ( appendedUpstreamNode.getType() == HydrologyNode.NODE_TYPE_END ) {
			// Change to an "Other" node
			Message.printStatus(2,routine,"Changing append node type from End to Other" );
			appendedUpstreamNode.setType(HydrologyNode.NODE_TYPE_OTHER);
		}
		Message.printStatus(2,routine,"Adding to existing downstream node \"" +
			existingDownstreamNode.getCommonID() + "\" the append node as upstream: " +
			appendedUpstreamNode.getCommonID() );
		existingDownstreamNode.addUpstreamNode(appendedUpstreamNode);
		existingDownstreamNode.addUpstreamNodeID(appendedUpstreamNode.getCommonID());
	}
	else if ( appendHowType == StateMod_NodeNetwork_AppendHowType.REPLACE_UPSTREAM_OF_DOWNSTREAM ) {
		// Remove all nodes downstream of the appended network.  Because this code will rebuild
		// the network below, just do this brute force without properly recalculating the navigation data.
		// Start with the node downstream of the requested node...
		append_RemoveNodesDownsteamOfAppendNode ( routine, appendedUpstreamNode, appendNetworkNodeList );
		appendedUpstreamNode.setDownstreamNodeID(null);
		// Remove all the nodes upstream of the downstream node, on the reach
		append_RemoveNodesUpstreamOfDownstreamNode(routine, existingDownstreamNode.getUpstreamNodes(),
			networkNodeList );
		// Also need to manually remove because the above removes from the node list but does not reset
		// the pointers on the node.
		int nUp = existingDownstreamNode.getNumUpstreamNodes();
		for ( int i = (nUp - 1); i >= 0; i-- ) {
			existingDownstreamNode.removeUpstreamNode(i);
		}
		existingDownstreamNode.clearUpstreamNodeIDs();
		// Set the downstream node for the append point
		appendedUpstreamNode.setDownstreamNode(existingDownstreamNode);
		appendedUpstreamNode.setDownstreamNodeID(existingDownstreamNode.getCommonID());
		Message.printStatus(2,routine,"Setting append node \"" + appendedUpstreamNode.getCommonID() +
			"\" downstream=\"" + existingDownstreamNode.getCommonID() + "\".");
		if ( appendedUpstreamNode.getType() == HydrologyNode.NODE_TYPE_END ) {
			// Change to an "Other" node
			Message.printStatus(2,routine,"Changing append node type from End to Other" );
			appendedUpstreamNode.setType(HydrologyNode.NODE_TYPE_OTHER);
		}
		Message.printStatus(2,routine,"Adding to existing downstream node \"" +
			existingDownstreamNode.getCommonID() + "\" the append node as upstream: " +
			appendedUpstreamNode.getCommonID() );
		// All upstream nodes were removed above so the following should be the only one.
		existingDownstreamNode.addUpstreamNode(appendedUpstreamNode);
		existingDownstreamNode.addUpstreamNodeID(appendedUpstreamNode.getCommonID());
	}
	else {
		throw new InvalidParameterException ("AppendHowType is not supported: " + appendHowType );
	}
	
	// Check for duplicate identifiers.  If not removed, they will cause lots of problems with infinite loops
	// in network navigation.
	
	StringBuffer b = new StringBuffer();
	double xOld, yOld;
	for ( HydrologyNode nodeToAppend: appendNetworkNodeList ) {
		// Adjust the coordinates of appended node and check for duplicates
		xOld = nodeToAppend.getX();
		yOld = nodeToAppend.getY();
		nodeToAppend.setX((xOld + shiftXAlignNetworks1)*scale +
			shiftXAlignNetworks2 + shiftXAdditional);
		nodeToAppend.setY((yOld + shiftYAlignNetworks1)*scale +
			shiftYAlignNetworks2 + shiftYAdditional);
		Message.printStatus(2,routine,"For appended node \"" + nodeToAppend.getCommonID() + "\" oldXY=" +
			xOld + "," + yOld + " newXY=" + nodeToAppend.getX() + "," + nodeToAppend.getY() );
		for ( HydrologyNode nodeToCheck: networkNodeList ) {
			if ( nodeToCheck.getCommonID().equalsIgnoreCase(nodeToAppend.getCommonID() ) ) {
				b.append(" " + nodeToCheck.getCommonID());
			}
		}
		// Add the node to the list.
		networkNodeList.add(nodeToAppend);
	}
	if ( b.length() > 0 ) {
		throw new RuntimeException (
			"Network being appended has nodes with identifiers in the existing network:  " + b );
	}
	Message.printStatus(2,routine,"Merged network has " + networkNodeList.size() +
		" nodes (from simple list merge)." );
	// TODO SAM 2011-01-05 Evaluate whether duplicate labels, etc. are an issue
	
	// Now merge the nodes from each network.
	
	for ( PropList linkToAppend: appendNetworkLinkList ) {
		// Links are just two identifiers so no need to adjust coordinates.
		networkLinkList.add(linkToAppend);
	}
	//for ( PropList layoutToAppend: appendNetworkLayoutList ) {
		// For now use the layout from the original network
		// TODO SAM 2011-01-04 Any need to adjust any coordinates?  Check layout consistency?
		//networkLayoutList.add(layoutToAppend);
	//}
	for ( HydrologyNode annotationToAppend: appendNetworkAnnotationList ) {
		// Adjust the coordinates and add
		annotationToAppend.setX((annotationToAppend.getX() + shiftXAlignNetworks1)*scale +
			shiftXAlignNetworks2 + shiftXAdditional);
		annotationToAppend.setY((annotationToAppend.getY() + shiftYAlignNetworks1)*scale +
			shiftYAlignNetworks2 + shiftYAdditional);
		networkAnnotationList.add(annotationToAppend);
	}
	
	StateMod_NodeNetwork mergedNetwork = new StateMod_NodeNetwork();
	boolean debug = Message.isDebugOn ;
	if ( debug ) {
		try {
			HydrologyNodeNetwork.writeListFile(getInputName() + ".beforeCalc", null, false,
				networkNodeList, null, true );
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,e);
		}
	}
	mergedNetwork.calculateNetworkNodeData(networkNodeList, false); // False means upstream first
	Message.printStatus(2,routine,"Merged network has " + mergedNetwork.getNodeCount() +
		" nodes (from complete network)." );
	if ( debug ) {
		try {
			HydrologyNodeNetwork.writeListFile(getInputName() + ".afterCalc", null, false,
				networkNodeList, null, true );
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,e);
		}
	}
	// FIXME SAM 2011-01-05 Do not know why but the above call sometimes results in duplicate
	// upstream nodes in the list.  Fix that here by removing the duplicate.  Don't have time right
	// now to track down the root issue
	HydrologyNode node2, node3;
	for ( HydrologyNode node: networkNodeList ) {
		List<HydrologyNode> upstreamNodeList = node.getUpstreamNodes();
		if ( upstreamNodeList != null ) {
			// Loop through each node in the list...
			for ( int i = 0; i < upstreamNodeList.size(); i++ ) {
				node2 = upstreamNodeList.get(i);
				// Loop through the remaining items in the list..
				for ( int i1 = (i + 1); i1 < upstreamNodeList.size(); i1++ ) {
					node3 = upstreamNodeList.get(i1);
					if ( node2 == node3 ) {
						upstreamNodeList.remove(i1);
						--i1; // To ensure next node will also be compared.
					}
				}
			}
		}
	}
	if ( debug ) {
		try {
			HydrologyNodeNetwork.writeListFile(getInputName() + ".afterRemoveDuplicates", null, false,
				networkNodeList, null, true );
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,e);
		}
	}
	mergedNetwork.setAnnotationList(networkAnnotationList);
	mergedNetwork.setLayoutList(networkLayoutList);
	mergedNetwork.setLinkList(networkLinkList);
	
	// For lack of a better option, get the new data extents for the bounds.
	// TODO SAM 2011-01-04 this really needs to consider the page size, labels, etc.,
	// but do the following for a first cut

	GRLimits networkDataLimits = mergedNetwork.determineExtentFromNetworkData();
	//GRLimits networkDataLimits = determineExtentFromNetworkData ( networkNodeList );
	Message.printStatus(2,routine,"Limits of merged network data=" + networkDataLimits );
	mergedNetwork.setBounds(networkDataLimits.getLeftX(), networkDataLimits.getBottomY(),
		networkDataLimits.getRightX(), networkDataLimits.getTopY() );
	// Use the old legend position
	mergedNetwork.setLegendPosition(this.getLegendX(), this.getLegendY());

	mergedNetwork.convertNodeTypes();
	mergedNetwork.finalCheck(networkDataLimits.getLeftX(), networkDataLimits.getBottomY(),
		networkDataLimits.getRightX(), networkDataLimits.getTopY(), false);
	
	return mergedNetwork;
}

/**
Helper method to remove nodes downstream of append node (local data from append() method).
*/
private void append_RemoveNodesDownsteamOfAppendNode ( String routine, HydrologyNode appendedUpstreamNode,
	List<HydrologyNode> appendNetworkNodeList )
{
	HydrologyNode node = appendedUpstreamNode.getDownstreamNode();
	if ( node != null ) {
		// Not at the bottom of the network so remove downstream nodes.
		List<HydrologyNode> downstreamNodesToRemoveList = new Vector<HydrologyNode>();
		// Move to the bottom of the network and save node references in a list...
		while ( true ) {
			// This node needs to be removed.
			downstreamNodesToRemoveList.add(node);
			// Get the next downstream...
			node = node.getDownstreamNode();
			if ( node == null ) {
				break;
			}
		}
		// Now have a list of nodes to remove
		// Delete the nodes from the network being appended...
		for ( HydrologyNode node2: downstreamNodesToRemoveList ) {
			Message.printStatus(2,routine,"Deleting the following downstream node in " +
				"append network before appending: " + node2.getCommonID() );
			appendNetworkNodeList.remove(node2);
		}
	}
}

/**
Helper method to remove nodes upstream of the existing downstream node (local data from append() method).
*/
private void append_RemoveNodesUpstreamOfDownstreamNode ( String routine,
	List<HydrologyNode> upstreamNodeList, List<HydrologyNode> networkNodeList )
{
	// Follow each reach upstream
	for ( HydrologyNode upstreamNode: upstreamNodeList ) {
		List<HydrologyNode> upstreamNodeList2 = upstreamNode.getUpstreamNodes();
		if ( upstreamNodeList2 != null ) {
			// Call recursively.  This should result in a march up the network and deletes of each node
			// as the recursive calls back down.
			append_RemoveNodesUpstreamOfDownstreamNode ( routine,
				upstreamNodeList2, networkNodeList );
		}
		// Now remove this current node.
		Message.printStatus(2,routine,"Deleting node \"" + upstreamNode.getCommonID() +
			"\" that is upstream of \"" + upstreamNode.getDownstreamNode().getCommonID() +
			"\" in the existing network." );
			networkNodeList.remove(upstreamNode);
	}
}

/**
Creates a HydroBase_NodeNetwork from a list of StateMod_RiverNetworkNodes.
@param nodes the nodes from which to create a HydroBase_NodeNetwork.
@return the HydroBase_NodeNetwork that was built.
TODO (JTS - 2004-07-03) should not be a static returning a method, I think ...
*/
public static StateMod_NodeNetwork createFromStateModVector(List<StateMod_RiverNetworkNode> nodes) {
	int size = nodes.size();
	
	HydrologyNode[] nodeArray = new HydrologyNode[size];
	StateMod_RiverNetworkNode rnn = null;
	for (int i = size - 1; i >= 0; i--) {
		rnn = nodes.get(i);
		nodeArray[i] = new HydrologyNode();
		nodeArray[i].setCommonID(rnn.getID().trim());
		nodeArray[i].setDownstreamNodeID(rnn.getCstadn().trim());
		nodeArray[i].setType(HydrologyNode.NODE_TYPE_UNKNOWN);
	}
	nodeArray[size - 1].setType(HydrologyNode.NODE_TYPE_END);

	String dsid = null;
	for (int i = size - 1; i >= 0; i--) {
		dsid = nodeArray[i].getDownstreamNodeID();
		if (dsid != null) {
			for (int j = 0; j < size; j++) {
				if (nodeArray[j].getCommonID().equals(dsid)) {
					nodeArray[j].addUpstreamNodeID(nodeArray[i].getCommonID());
				}
			}
		}
	}

	List<HydrologyNode> v = new Vector<HydrologyNode>();
	for (int i = 0; i < size; i++) {
		v.add(nodeArray[i]);
	}

	StateMod_NodeNetwork network = new StateMod_NodeNetwork();
	network.calculateNetworkNodeData(v, false);
	return network;
}

/**
Creates a StateMod_RiverNodeNetwork from the nodes in the HydroBase_NodeNetwork.
The output contains only actual nodes.  Therefore, confluence nodes are skipped.
@return the list of StateMod_RiverNetworkNode nodes.
*/
public List<StateMod_RiverNetworkNode> createStateModRiverNetwork() {
	boolean done = false;
	HydrologyNode holdNode = null;
	HydrologyNode node = getMostUpstreamNode();	
	HydrologyNode dsNode = null;
	StateMod_RiverNetworkNode rnn = null;
	List<StateMod_RiverNetworkNode> v = new Vector<StateMod_RiverNetworkNode>();
	int node_type; // Type for current node.
	int dsNode_type; // Type for downstream node.
	HydrologyNode node_downstream = null; // Used to find a real
	HydrologyNode real_node_downstream = null; // downstream node.
	// Create a blank node used for disappearing streams.  The identifiers will be empty strings...
	HydrologyNode blankNode = new HydrologyNode();
	blankNode.setDescription("SURFACE WATER LOSS");
	blankNode.setUserDescription("SURFACE WATER LOSS");
	blankNode.setType(HydrologyNode.NODE_TYPE_UNKNOWN);
	while (!done) {
		node_type = node.getType();
		// TODO SAM 2004-07-11 - the following may fail if no valid
		// downstream node is found - error handling below is not used.
		if ( (node_type == HydrologyNode.NODE_TYPE_CONFLUENCE) ||
			(node_type == HydrologyNode.NODE_TYPE_XCONFLUENCE) ||
			(node_type == HydrologyNode.NODE_TYPE_BLANK)) {
			node = getDownstreamNode(node, POSITION_COMPUTATIONAL);
			continue;
		}

		// Create a new instance and set the identifier...

		rnn = new StateMod_RiverNetworkNode();
		rnn.setID(node.getCommonID());

		// Set the node type.  This can be used later when filling with
		// HydroBase, to format the name (remove this code if that convention is phased out).
		//
		// This code is the same when reading the stream estimate stations command.
		if ( node.getType() == HydrologyNode.NODE_TYPE_DIV ) {
			rnn.setRelatedSMDataType ( StateMod_DataSet.COMP_DIVERSION_STATIONS );
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_DIV_AND_WELL ) {
			rnn.setRelatedSMDataType ( StateMod_DataSet.COMP_DIVERSION_STATIONS );
			rnn.setRelatedSMDataType2 (	StateMod_DataSet.COMP_WELL_STATIONS );
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_ISF ) {
			rnn.setRelatedSMDataType ( StateMod_DataSet.COMP_INSTREAM_STATIONS );
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_PLAN ) {
			rnn.setRelatedSMDataType ( StateMod_DataSet.COMP_PLANS );
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_RES ) {
			rnn.setRelatedSMDataType ( StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_WELL ) {
			rnn.setRelatedSMDataType ( StateMod_DataSet.COMP_WELL_STATIONS );
		}

		// Set the downstream node information...

		dsNode = getDownstreamNode(node, POSITION_COMPUTATIONAL);

		// Taken from old createRiverNetworkFile() code...

		//Message.printStatus ( 2, "", "Processing node: " + node  );
		if ( node.getDownstreamNode() != null ) {
			dsNode_type = node.getDownstreamNode().getType();
			if ( (dsNode_type== HydrologyNode.NODE_TYPE_BLANK)||
				(dsNode_type== HydrologyNode.NODE_TYPE_XCONFLUENCE) ||
				(dsNode_type == HydrologyNode.NODE_TYPE_CONFLUENCE)) {
				// We want to always show a real node in the river network...
				real_node_downstream = findNextRealDownstreamNode(node);
				node_downstream = findNextRealOrXConfluenceDownstreamNode( node);
				//Message.printStatus ( 2, "", "real ds node=" + real_node_downstream.getCommonID() +
				//" real or X ds node=" + node_downstream.getCommonID() );
				// If the downstream node in the reach is an XCONFLUENCE (as opposed to a tributary
				// coming in) then this is the last real node in disappearing stream.
				// Use a blank for the downstream node...
				//
				// Cannot simply check for the downstream node to be an XCONFL because incoming tributaries
				// may be joined to the main stem with an XCONFL
				//
				// There may be cases where multiple XCONFL nodes are in a row (e.g., to bend lines).  In
				// these cases, it is necessary to check the downstream reach node type, rather than
				// make sure that the node is the same as the immediate downstream node (as was done in
				// software prior to 2005-06-13).  This has not been implemented.  The user should make sure
				// that multiple XCONFL nodes are not included on a tributary reach that joins another stream.
				//
				if ( node_downstream.getType() == HydrologyNode.NODE_TYPE_XCONFLUENCE ) {
					// Get node to check...
					/* Use for debugging
					HydroBase_Node temp_node;
					temp_node = getDownstreamNode(node, POSITION_REACH);
					Message.printStatus ( 2, "", "reach end =" + temp_node );
					if ( temp_node != null ) {
						Message.printStatus ( 2, "", "reach end ds =" + temp_node.getDownstreamNode() );
					}
					*/
					if ( node_downstream == getDownstreamNode(node,POSITION_REACH).getDownstreamNode() ) {
						// This identifies XCONFL nodes at the ends of tributary reaches and works
						// because the nodes are clearly identified on a reach...
						node_downstream = blankNode;
					}
					else if ( node_downstream.getNumUpstreamNodes() == 1 ) {
						// For some reason someone put an XCONFL in a reach but did not split out
						// a tributary.  Therefore the "main stem" goes dry but is computationally
						// connected to the next downstream node...
						node_downstream = blankNode;
					}
					else {
						// Picking up on a confluence from another trib so use what normally would have...
						node_downstream = real_node_downstream;
					}
				}
				else {
					// Normal node...
					node_downstream = real_node_downstream;
				}
			}
			else {
				node_downstream = node.getDownstreamNode();
			}
			rnn.setCstadn(node_downstream.getCommonID());
		}

		// Add the node to the list...

		v.add(rnn);

		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			done = true;
		}		
		// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		else if (node == holdNode) {
			done = true;
		}
		holdNode = node;	
		node = dsNode;
	}
	return v;	
}

/**
Gets the extent of the nodes in the network in the form of GRLimits, in network plotting coordinates
(NOT alternative coordinates).  This is different from the HydrologyNodeNetwork.getExtents() in that
it processes the raw nodes before they have been assimilated into a network.  This method may need to be
called if the XMin, Ymin, XMax, Ymax data are corrupted in the network metadata (happened because of a
bug that used alternative coordinates for computing these limits).
@param nodeList list of nodes to be processed.
@return the GRLimits that represent the bounds of the nodes in the network, or null if no nodes have
coordinates.
*/
public static GRLimits determineExtentFromNetworkData ( List<HydrologyNode> nodeList )
{
	double lx = Double.NaN;
	double rx = Double.NaN;
	double by = Double.NaN;
	double ty = Double.NaN;	

	for ( HydrologyNode node: nodeList ) {
		if ( Double.isNaN(lx) || (node.getX() < lx) ) {
			lx = node.getX();
		}
		if ( Double.isNaN(rx) || (node.getX() > rx) ) {
			rx = node.getX();
		}
		if ( Double.isNaN(by) || (node.getY() < by) ) {
			by = node.getY();
		}
		if ( Double.isNaN(ty) || (node.getY() > ty) ) {
			ty = node.getY();
		}
	}


	if ( Double.isNaN(lx) || Double.isNaN(rx) || Double.isNaN(by) || Double.isNaN(ty) ) {
		// Do not have all parts of the data limits
		return null;
	}
	else {
		return new GRLimits(lx, by, rx, ty);
	}
}

/**
Fills the locations of the nodes in the network, interpolating if necessary, 
and looking up from the database if possible.
@param nodeDataProvider the dmi to use for talking to the database.  Should be open and non-null.
@param interpolate whether node locations should be interpolated, or just
looked up from the database.
@param limits if interpolating, the limits to use as the far bounds of the network.
*/
public void fillLocations(StateMod_NodeDataProvider nodeDataProvider, boolean interpolate, GRLimits limits) {
	double lx, rx, by, ty;
	if (limits == null) {
		limits = determineExtentFromNetworkData();
	}

	lx = limits.getLeftX();
	by = limits.getBottomY();
	rx = limits.getRightX();
	ty = limits.getTopY();

	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
	HydrologyNode holdNode = null;
	HydrologyNode node = getMostUpstreamNode();	
	boolean done = false;
	double[] loc = null;
	while (!done) {
		loc = nodeDataProvider.lookupNodeLocation(node.getCommonID());
		if (DMIUtil.isMissing(node.getX())) {
			node.setX(loc[0]);
			node.setDBX(loc[0]);
		}
		
		if (DMIUtil.isMissing(node.getY())) {
			node.setY(loc[1]);
			node.setDBY(loc[1]);
		}	
	
		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			done = true;
		}		
		else if (node == holdNode) {
			done = true;
		}

		holdNode = node;	
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);		
	}

	if (!interpolate) {
		return;
	}

	setLx ( lx );
	setBy ( by );

	if ((rx - lx) > (ty - by)) {
		setNodeSpacing ( (ty - by) * 0.06 );
	}
	else {
		setNodeSpacing ( (rx - lx) * 0.06 );
	}

	// Fills in any missing locations for all the nodes on the main stream stem.
	fillMainStemLocations();

	// Fills in missing locations for any node upstream of the main stem.
	fillUpstreamLocations();

	finalCheck(lx, by, rx, ty);
}

/**
Initialize data.
*/
private void initialize() {
	__closeCount = 0;
	__createOutputFiles = true;
	__createFancyDescription = false;
	//In base... __fontSize = 10.0;
	//In base... __labelType = LABEL_NODES_NETID;
	//__legendDX = 1.0;
	//__legendDY = 1.0;
	//__legendX = 0.0;
	//__legendY = 0.0;
	__line = 1;
	//In base...__nodes = new Vector();
	//In base...__plotCommands = new Vector();
	//In base...__nodeCount = 1;
	//In base...__nodeDiam = 10.0;
	//In base...__nodeHead = null;
	__openCount = 0;
	__reachCounter = 0;
	//In base... __title = "Node Network";
	//In base...__titleX = 0.0;
	//In base...__titleY = 0.0;
	//In base... __treatDryAsBaseflow = false;
}

/**
Processes node information from a makenet file. This version initializes the 
counters properly and then calls the version that has the full argument list.
@param netfp the BufferedReader to use for reading from the file.
@param filename the name of the file being read.
@return a node filled with data from the makenet file.
*/
public HydrologyNode processMakenetNodes(BufferedReader netfp, String filename) {
	return processMakenetNodes(netfp, filename, false);
}

/**
Processes node information from a makenet file. This version initializes the 
counters properly and then calls the version that has the full argument list.
@param netfp the BufferedReader to use for reading from the file.
@param filename the name of the file being read.
@param skipBlankNodes whether blank nodes should just be skipped when read in from the file.
@return a node filled with data from the makenet file.
*/
private HydrologyNode processMakenetNodes(BufferedReader netfp, String filename, boolean skipBlankNodes) {
	double	dx = 1.0, 
		dy = 1.0,
		x0 = 0.0, 
		y0 = 0.0; 
	int __closeCount = 0;	// Number of closing }.
	int __openCount = 0;	// Number of opening {.
	int _reachLevel = 1;
	String wd = "";

	return processMakenetNodes((HydrologyNode)null, netfp, filename, wd,
		x0, y0, dx, dy, __openCount, __closeCount, _reachLevel, skipBlankNodes);
}

/**
Processes node information from a makenet file. This version is called 
recursively.  A main program should call the other version.
@param node the node processed prior to the current iteration.
@param netfp the BufferedReader to use for reading from the file.
@param filename the name of the file being read.
@param wd the water district in effect.
@param x0 the starting X for the reach.
@param y0 the starting Y for the reach.
@param dx the X-increment for drawing each node.
@param dy the Y-increment for drawing each node.
@param openCount the number of open {s.
@param closeCount the number of closed }s.
@param reachLevel the current reach level being iterated over.
@param skipBlankNodes whether to skip blank nodes when reading nodes in from the file.
@return a node filled with data from the makenet file.
*/
private HydrologyNode processMakenetNodes(HydrologyNode node, 
	BufferedReader netfp, String filename, String wd, double x0, double y0, 
	double dx, double dy, int openCount, int closeCount, int reachLevel,
	boolean skipBlankNodes)
{
	String routine = "HydroBase_NodeNetwork.processMakenetNodes";
	double 	river_dx, 
		river_dy, 
		x, 
		y;
	HydrologyNode nodePt;
	HydrologyNode tempNode;
	int	dir, 
		dl = 20, 
		nlist, 
		nodeInReach, 
		numRiverNodes,
		numTokens, 
		reachCounterSave;	
	RiverLine river = new RiverLine();
	String	message, 
		nodeID, 
		token0;
	List<String> list; 
	List<String> tokens;

	__closeCount = 0;	// Number of closing curly-braces.
	__openCount = 0;	// Number of opening curly-braces.

	// Since some of the original C code used pointers, we use globals and
	// locals in conjunction to maintain the original C version logic.

	++reachLevel;
	++__reachCounter;
	reachCounterSave = __reachCounter;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Starting new reach at (x0,y0) = " + x0 + "," + y0 
			+ " dx,dy = " + dx + "," + dy + " reachLevel=" 
			+ reachLevel + " reachCounter=" + __reachCounter);
	}
	x = x0;
	y = y0;

	// If the downstream node was a BLANK, then we need to subtract a
	// dx,dy to get so the diagram will look right (in this case we are
	// using a BLANK instead of a CONFL or XCONFL)...

	// Reset the node counter for the reach...

	nodeInReach = 1;

	while (true) {
		numTokens = 0;
		try {	
			tokens = readMakenetLineTokens(netfp);
		}
		catch (IOException e) {
			// End of file...
			return node;
		}
		if (tokens == null) {
			// End of file?
			break;
		}
		else {	
			numTokens = tokens.size();
			if (numTokens == 0) {
				// Blank line...
				Message.printWarning(2, routine, "Skipping line " + __line + " in \"" + filename + "\"");
				continue;
			}
		}

		// ------------------------------------------------------------
		// Now evaluate node-level commands...
		// ------------------------------------------------------------
		token0 = new String((String)tokens.get(0));
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "token[0]=\"" + token0 + "\"");
		}
		if (token0.charAt(0) == '#') {
			// Comment line...
			continue;
		}
		if (token0.equalsIgnoreCase("DISTRICT")) {
			wd = new String(tokens.get(1));
			Message.printStatus(2, routine, "Water district specified as " + wd);
			//HMPrintWarning(1, routine, "DISTRICT command is no longer recognized.  Use full ID");
			continue;
		}
		else if (token0.equalsIgnoreCase("FONT")) {
			setFont(tokens.get(1), StringUtil.atod(tokens.get(2)));
			continue;
		}
		else if (token0.equalsIgnoreCase("TEXT")) {
			message = "The TEXT command is obsolete.  Use FONT";
			Message.printWarning(3, routine, message);
			printCheck(routine, 'W', message);
			continue;
		}
		else if (token0.equalsIgnoreCase("NODESIZE")) {
			message = "The NODESIZE command is obsolete.  Use NODEDIAM";
			Message.printWarning(3, routine, message);
			printCheck(routine, 'W', message);
			setNodeDiam(StringUtil.atod(tokens.get(1)));
			continue;
		}
		else if (token0.equalsIgnoreCase("NODEDIAM")) {
			setNodeDiam(StringUtil.atod(tokens.get(1)));
			continue;
		}
		else if (token0.equalsIgnoreCase("{")) {
			// put a closing } here to help editor!

			// We are starting a new stream reach so recursively
			// call this routine.  Note that the coordinates are
			// the ones for the current node...

			// If the current node(subsequent to the new reach)
			// was a blank, then do not increment the counter...
			if (node.getType() == HydrologyNode.NODE_TYPE_BLANK) {
				x -= dx;
				y -= dy;
			}
			++openCount;
			++__openCount;
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Adding a reach above \"" 
					+ node.getCommonID() + "\" nupstream=" + node.getNumUpstreamNodes());
			}
			HydrologyNode node_upstream = processMakenetNodes(node, netfp, filename,
				wd, x, y, dx, dy, openCount, closeCount, reachLevel, skipBlankNodes);
			if (node_upstream == null) {
				// We have bad troubles.  Return null here to
				// back out also (we want to end the program).
				message  = "Major error processing nodes.";
				Message.printWarning(3, routine, message);
				printCheck(routine, 'W', message);
				return null;
			}
			// Else add the node...
			// This would have been done in the recurse!  
			// Don't need to add again...
			//node.addUpstreamNode(node_upstream);
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Added reach starting at \"" 
					+ node_upstream.getNetID() + "\" reachnum=" 
					+ node_upstream.getTributaryNumber() 
					+ " num=" + node_upstream.getSerial() 
					+ "(downstream is \"" + node.getNetID() + "\" [" 
					+ node.getSerial() + ", nupstream=" + node.getNumUpstreamNodes() + ")");
			}
			x += dx;
			y += dy;
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Now back at reach level " + reachLevel);
			}
		}
		else if (token0.equalsIgnoreCase("STREAM")) {
			// We are starting a stream(this will generally only be called when recursing).

			// with graphing
			river.id = new String(tokens.get(1));
			Message.printStatus(2, routine, "Starting stream \"" + river.id 
				+ "\".  Building network upstream...");
			numRiverNodes = StringUtil.atoi(tokens.get(2));
			river_dx = StringUtil.atod(tokens.get(3));
			river_dy = StringUtil.atod(tokens.get(4));
			river.strx = x;
			river.stry = y;
			river.endx = x + river_dx;
			river.endy = y + river_dy;
			
			// Reset the dx and dy based on this reach...
			if (numRiverNodes == 1) {
				dx = river.endx - river.strx;
				dy = river.endy - river.stry;
			}
			else {	
				dx = (river.endx - river.strx)/(numRiverNodes - 1);
				dy = (river.endy - river.stry)/(numRiverNodes - 1);
			}
			if (Message.isDebugOn) {
				Message.printDebug(1, routine, "Resetting dx,dy to " + dx + "," + dy);
			}
			// Save the reach label...
			storeLabel((String)tokens.get(1), x, y, (x + river_dx),(y + river_dy));
			// Plot the line for the river reach in the old plot
			// file.  The nodes will plot on top and clear the line under the node...
			
			// Because we are on a new reach, we want our first
			// node to be off the previous stem...
		}
		// Add { here to help editor with matching on the next line...
		else if (token0.equalsIgnoreCase("}")) {
			// Make sure that we are not closing off more reaches than we have started...
			++closeCount;
			++__closeCount;
			if (closeCount > openCount) {
				message = "Line " + __line + ":  unmatched }(" + openCount + " {, " + closeCount + " })";
				Message.printWarning(3, routine, message);
				printCheck(routine, 'W', message);
				return null;
			}
			// We are done with the recursion on the stream reach
			// but need to return the lowest node in the reach...
			nodePt = getDownstreamNode(node, POSITION_REACH);
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Done with reach(top=\"" 
					+ node.getCommonID() + "\", bottom=\"" + nodePt.getCommonID() + "\")");
			}
			return nodePt;
		}
		else if (token0.equalsIgnoreCase("STOP")) {
			// We are at the end of the system.  Just return the
			// node (which will be the upstream node).  This should
			// send us back to the main program...
			Message.printStatus(2, routine, "Detected STOP in net file.  Stop processing nodes.");
			return node;
		}
		else if (token0.equalsIgnoreCase("BLANK") && skipBlankNodes) {
			// skip it
			x += dx;
			y += dy;
		}
		else {	
			// Node information...

			// Create a new node above another node (this is not
			// adding a reach - it is adding a node within a reach)...
			if (token0.length() > 12) {
				Message.printWarning(1, routine, "\"" + token0 + "\"(line " + __line
					+ " is > 12 characters.  Truncating to 12.");
				token0 = new String(token0.substring(0, 12));
			}
			if (node == null) {
				// This is the first node in the network...
				node = new HydrologyNode();
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,	"Adding first node");
				}
			}
			else {
				// This is a node...

				// First create the upstream node...
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Adding upstream node \"" 
						+ token0 + "\"(line " + __line + ")");
				}
			
				tempNode = new HydrologyNode();
				node.addUpstreamNode(tempNode);
				// Now it is safe to reset the current node to be the upstream node...
				node = node.getUpstreamNode( node.getNumUpstreamNodes() - 1);
				// Set the h_num to be the same as the number of nodes added above the downstream node...
				node.setTributaryNumber( (node.getDownstreamNode()).getNumUpstreamNodes());
			}
			// Set the node information regardless whether the
			// first node or not.  By here, "node" points to the node that has just been added...
			
			// Set node in reach...
			node.setNodeInReachNumber(nodeInReach);
			
			// Use the saved reach counter so that we
			// do not keep incrementing the reach counter for the same reach...
			node.setReachCounter(reachCounterSave);
			node.setReachLevel(reachLevel);
			node.setSerial(getNodeCount());
			
			if (node.getDownstreamNode() == null) {
				// First node.  Do not print downstream...
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Added node \"" + token0 
						+ "\" nodeInReach=" + node.getNodeInReachNumber()
						+ " reachnum=" + node.getTributaryNumber()
						+ " reachCounter=" + node.getReachCounter() 
						+ " num=" + node.getSerial()
						+ " #up=" + node.getNumUpstreamNodes());
				}
			}
			else {	
				if (Message.isDebugOn) {
					Message.printDebug(1, routine, "Added node \"" + tokens.get(0)
						+ "\" nodeInReach=" + node.getNodeInReachNumber() 
						+ " reachnum=" + node.getTributaryNumber() 
						+ " reachCounter=" + node.getReachCounter() 
						+ " num=" + node.getSerial() 
						+ " #up=" + node.getNumUpstreamNodes() 
						+ "(downstream is \"" + (node.getDownstreamNode()).getNetID()
						+ " nupstream=" + (node.getDownstreamNode()).getNumUpstreamNodes() + ")");
				}
			}
			++nodeInReach;
			setNodeCount ( getNodeCount() + 1 );
			if (token0.equalsIgnoreCase("BLANK")) {
				// Allow blank nodes for spacing...
				node.setType(HydrologyNode.NODE_TYPE_BLANK);
				node.setNetID("BLANK");
				Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1)+ ": BLANK");
			}
			else if (token0.equalsIgnoreCase("CONFL")) {
				// Confluence...
				node.setType(HydrologyNode.NODE_TYPE_CONFLUENCE);
				node.setNetID("CONFL");
				Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1) + ": CONFL");
			}
			else if (token0.equalsIgnoreCase("XCONFL")) {
				// Computational confluence for off-stream channel...
				node.setType(HydrologyNode.NODE_TYPE_XCONFLUENCE);
				node.setNetID("XCONFL");
				Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1) + ": XCONFL");
			}
			else if (token0.equalsIgnoreCase("END")) {
				// End of system.  This node has no downstream node...
				node.setType(HydrologyNode.NODE_TYPE_END);
				node.setNetID(token0);
				Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1) + ": END");
			}
			else {	
				// A valid node ID...
				node.setNetID(token0);
				Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1) + ": " + token0);
				// Get the node type. 
				String token1 = tokens.get(1);
				if (token1.equalsIgnoreCase("BLANK")) {
					node.setType(HydrologyNode.NODE_TYPE_BLANK);
				}
				else if (token1.equalsIgnoreCase("DIV")) {
					node.setType(HydrologyNode.NODE_TYPE_DIV);
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"Node \"" + node.getNetID() + "\" type is DIV " + "from \"DIV\"");
					}
				}
				else if (token1.equalsIgnoreCase("D&W") || token1.equalsIgnoreCase("DW")) {
					node.setType( HydrologyNode.NODE_TYPE_DIV_AND_WELL);
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"Node \"" + node.getNetID() + "\" type is D&W from \"D&W\"");
					}
				}
				else if (token1.equalsIgnoreCase("WELL")) {
					node.setType(HydrologyNode.NODE_TYPE_WELL);
				}
				else if (token1.equalsIgnoreCase("FLOW")) {
					node.setType(HydrologyNode.NODE_TYPE_FLOW);
				}
				else if (token1.equalsIgnoreCase("CONFL")) {
					node.setType(HydrologyNode.NODE_TYPE_CONFLUENCE);
				}
				else if (token1.equalsIgnoreCase("XCONFL")) {
					node.setType( HydrologyNode.NODE_TYPE_XCONFLUENCE);
				}
				else if (token1.equalsIgnoreCase("END")) {
					node.setType(HydrologyNode.NODE_TYPE_END);
				}
				else if (token1.equalsIgnoreCase("ISF")) {
					node.setType(HydrologyNode.NODE_TYPE_ISF);
				}
				else if (token1.equalsIgnoreCase("OTH")) {
					node.setType(HydrologyNode.NODE_TYPE_OTHER);
				}
				else if (token1.equalsIgnoreCase("PLN")) {
					node.setType(HydrologyNode.NODE_TYPE_PLAN);
				}
				else if (token1.equalsIgnoreCase("RES")) {
					node.setType(HydrologyNode.NODE_TYPE_RES);
				}
				else if (token1.equalsIgnoreCase("IMPORT")) {
					node.setType(HydrologyNode.NODE_TYPE_IMPORT);
				}
				else if (token1.equalsIgnoreCase("BFL")) {
					node.setType(HydrologyNode.NODE_TYPE_BASEFLOW);
				}
				else {	
					// Assume number type...
					if (Message.isDebugOn) {
						message = "\"" + node.getNetID() + "\" Node type \"" 
							+ token1 + "\":  Node type is not recognized.";
						Message.printWarning(3,	routine, message);
						printCheck(routine, 'W', message);
					}
					return null;
				}
				// Now process the area*precip information...
				if (!node.parseAreaPrecip(tokens.get(2))) {
					message = "Error processing area*precip info for \"" + node.getNetID() + "\"";
					Message.printWarning(3, routine,message);
					printCheck(routine, 'W', message);
				}
				if (numTokens >= 4) {
					dir = StringUtil.atoi(tokens.get(3));
				}
				else {	
					Message.printWarning(3, routine, "No direction for \"" + token0 + "\" symbol.  Assuming 1.");
					dir = 1;
				}
				node.setLabelDirection(dir);
				// Now process the description if specified (it is optional)...
				if (numTokens >= 5) {
					node.setDescription(tokens.get(4));
					node.setUserDescription(tokens.get(4));
				}
				// Else defaults are empty descriptions.
			}
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Set \"" + node.getNetID() 
					+ "\" node type to " + node.getType() + " area=" 
					+ node.getArea() + " precip=" 
					+ node.getPrecip() + " water=" 
					+ node.getWater() + " desc=\"" 
					+ node.getDescription() + "\"");
			}
			// Now get the common ID and description for
			// the node.  Only do this if we have not already
			// manually set the description. This is a little ugly
			// because some nodes types have multiple values being
			// set - we can't just check the description being
			// empty in an outside loop.
			if (node.getType() == HydrologyNode.NODE_TYPE_FLOW) {
				node.setCommonID(node.getNetID());
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_CONFLUENCE) {
				// Confluences...
		    	node.setCommonID("CONFL");
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_XCONFLUENCE) {
				// Confluences...
		    	node.setCommonID("XCONFL");
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_BLANK) {
				// Blank nodes...
		    	node.setCommonID("BLANK");
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				// End node...
		    	node.setCommonID(node.getNetID());
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_BASEFLOW) {
				// Special baseflow node...
		    	node.setCommonID(node.getNetID());
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_RES) {
				// Reservoirs...
				node.setCommonID(node.getNetID());
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_ISF) {
				// Minimum streamflows.  Get the description from the water rights...  For these
				// we are allowed to abbreviate the identifier on the network.  For the
				// common ID we need to prepend the water district...

				// To support old-style ISF identifiers with periods, we need to have the following...
				list = StringUtil.breakStringList( node.getNetID(), ".", 0);
				nlist = list.size();
				if (nlist >= 1) {
					nodeID = list.get(0);
				}
				else {	
					nodeID = new String(node.getNetID());
				}
				String wdid = formatWDID(wd, nodeID, node.getType());
				node.setCommonID(wdid); 
				node.setCommonID(node.getNetID());
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_WELL) {
				// Ground water well only.  Don't allow the abbreviation...
				node.setCommonID(node.getNetID());
			}
			else if (node.getType() == HydrologyNode.NODE_TYPE_PLAN) {
				// Plans...
				node.setCommonID(node.getNetID());
			}
			else {	
				// Diversions, imports, and D&W.  For these we are allowed to abbreviate the
				// identifier on the network.  For the common ID we need to prepend the
				// water district...
				String wdid = formatWDID(wd, node.getNetID(), node.getType());
				node.setCommonID(wdid);
			}
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Set common ID for \"" + node.getNetID()
					+ "\" to \"" + node.getCommonID() + "\"");
			}

			// Set the river node ID...
			// The new way is to just use the common ID.  The
			// old way is to put an extension on USGS IDs (the old code has been deleted)...

			// The new way...
			node.setRiverNodeID(node.getCommonID());
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Set \"" + node.getNetID() 
					+ "\" river node to \"" + node.getRiverNodeID() + "\"");
			}
			// Now plot the node...
			node.setX(x);
			node.setY(y);
			if ((node.getType() != HydrologyNode.NODE_TYPE_CONFLUENCE) 
			    && (node.getType() != HydrologyNode.NODE_TYPE_XCONFLUENCE)) {
				// If a confluence we want the line to come in at the same point...
				x += dx;
				y += dy;
			}
		}
	}
	return node;
}

/**
Read a line from the makenet network file and split into tokens.
It is assumed that the line number is initialized to zero in the main program.
Blank lines are ignored.  Comments are parsed and returned (can be ignored in calling code).
@param netfp the reader reading the makenet net file.
@return the tokens from the line
@throws IOException if there is an error reading the line from the file.
*/
private List<String> readMakenetLineTokens(BufferedReader netfp) throws IOException {
	String routine = "StateMod_NodeNetwork.readMakenetLineTokens";
	int	commentIndex = 0,
		dl = 50, 
		numTokens;
	String lineString;
	List<String> tokens;

	while (true) {
		try {	
			lineString = netfp.readLine();
			if (lineString == null) {
				break;
			}
		}
		catch (IOException e) {
			// End of file.
			throw new IOException("End of file");
		}
		++__line;
		lineString = StringUtil.removeNewline(lineString.trim());
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Line " + __line + ":  \"" + lineString + "\"");
		}

		// Trim comment from end (if included)...
		commentIndex = lineString.indexOf('#');
		if (commentIndex >= 0) {
			// Special check to allow _# because it is currently
			// used in some identifiers...
			if ((commentIndex > 0) &&
				lineString.charAt(commentIndex - 1) == '_') {
				Message.printWarning(3, routine,
					"Need to remove # from ID on line " + __line + ": " + lineString);
			}
			else {	
				// OK to reset the line to the the beginning of the line...
				lineString = lineString.substring(0, commentIndex);
			}
		}

		// Now break into tokens...

		numTokens = 0;
		tokens = StringUtil.breakStringList(lineString, "\t ",
			StringUtil.DELIM_SKIP_BLANKS | StringUtil.DELIM_ALLOW_STRINGS);
		if (tokens != null) {
			numTokens = tokens.size();
		}
		if (numTokens == 0) {
			// Blank line...
			continue;
		}
		return tokens;
	}
	return null;
}

/**
Read an entire Makenet network file and save in memory.
@param nodeDataProvider Object that fills in node data (e.g., from HydroBase).
@param filename Makenet .net file to read and process.
@return true if the network was read successfully, false if not.
*/
public boolean readMakenetNetworkFile( StateMod_NodeDataProvider nodeDataProvider, String filename) {
	return readMakenetNetworkFile( nodeDataProvider, filename, false);
}

/**
Read an entire Makenet network file and save in memory.
@param nodeDataProvider Object that fills in node data (e.g., from HydroBase).
@param filename Makenet .net file to read and process.
@param skipBlankNodes whether to skip blank nodes when reading nodes in.
@return true if the network was read successfully, false if not.
*/
public boolean readMakenetNetworkFile( StateMod_NodeDataProvider nodeDataProvider,
	String filename, boolean skipBlankNodes)
{
	String routine = "HydroBase_NodeNetwork.readMakenetNetworkFile";
	BufferedReader in;
	try {	
		in = new BufferedReader(new FileReader(filename));
	}
	catch (IOException e) {
		String message = "Error opening net file \"" + filename + "\"";
		Message.printWarning(3, routine, message);
		printCheck(routine, 'W', message);
		return false;
	}
	// Set the filename so that it can be selected by default, for example in save dialogs
	setInputName ( filename );
	return readMakenetNetworkFile( nodeDataProvider, in, filename, skipBlankNodes);
}

/**
Read an entire Makenet network file and save in memory.
@param nodeDataProvider Object that fills in node data (e.g., from HydroBase).
@param in the BufferedReader to use for reading from the file.
@param filename Makenet .net file to read and process.
@return true if the network was read successfully, false if not.
*/
public boolean readMakenetNetworkFile( StateMod_NodeDataProvider nodeDataProvider,
	BufferedReader in, String filename)
{
	return readMakenetNetworkFile( nodeDataProvider, in, filename, false);
}

/**
Read an entire makenet network file and save in memory.
@param nodeDataProvider Object that fills in node data (e.g., from HydroBase).
@param in the BufferedReader opened on the file to use for reading it.
@param filename the name of the file to be read.
@param skipBlankNodes whether to skip blank nodes when reading in from a file.
@return true if the network was read successfully, false if not.
*/
public boolean readMakenetNetworkFile( StateMod_NodeDataProvider nodeDataProvider,
	BufferedReader in, String filename, boolean skipBlankNodes)
{
	String routine = "HydroBase_NodeNetwork.readMakenetNetworkFile";
	double 	dx = 1.0, 
		dy = 1.0, 
		x0 = 0.0, 
		x1 = 1.0, 
		y0 = 0.0, 
		y1 = 1.0;
	int dl = 30, 
		numTokens, 
		numnodes, 
		reachLevel = 0;		
	String token0;
	List<String> tokens;

	// Create a blank node used for disappearing streams.  The identifiers will be empty strings...

	HydrologyNode blankNode = new HydrologyNode();
	blankNode.setDescription("SURFACE WATER LOSS");
	blankNode.setUserDescription("SURFACE WATER LOSS");
	blankNode.setType(HydrologyNode.NODE_TYPE_UNKNOWN);

	// Start at the top of the file and read until we get to the STOP command or the end of the file...

	while (true) {
		numTokens = 0;
		try {	
			tokens = readMakenetLineTokens(in);
		}
		catch (IOException e) {
			// End of the file so break...
			break;
		}
		if (tokens == null) {
			continue;
		}
		numTokens = tokens.size();
		if (numTokens == 0) {
			// Blank line...
			continue;
		}
		token0 = tokens.get(0);
		if (Message.isDebugOn) {
			Message.printDebug(10, routine,	"token[0]=\"" + token0 + "\"");
		}
		if (token0.equalsIgnoreCase("STOP")) {
			// End of run...
			break;
		}
		else if (token0.charAt(0) == '#') {
			// Comment...
			continue;
		}
		else if (token0.equalsIgnoreCase("LEGEND")) {
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Found LEGEND command");
			}
			//TODO SAM 2007-02-18 Evaluate whether needed
			//__legendX = StringUtil.atod(
			//		 (String)tokens.elementAt(1));
			//__legendY = StringUtil.atod(
			//		 (String)tokens.elementAt(2));
			//__legendDX = StringUtil.atod(
			//		 (String)tokens.elementAt(3));
			//__legendDY = StringUtil.atod(
			//		 (String)tokens.elementAt(4));
			continue;
		}
		else if (token0.equalsIgnoreCase("SCALE")) {
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,"Found SCALE command");
			}
			String message = "SCALE is obsolete!";
			Message.printWarning(3, routine, message);
			printCheck(routine, 'W', message);
			//sx = atof(tokens[1]);
			//sy = atof(tokens[2]);
			continue;
		}
		else if (token0.equalsIgnoreCase("RIVER")) {
			// Line information for the river reach...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,"Found RIVER command");
			}
			numnodes = StringUtil.atoi((String)tokens.get(2));
			x0 = StringUtil.atod((String)tokens.get(3));
			y0 = StringUtil.atod((String)tokens.get(4));
			x1 = StringUtil.atod((String)tokens.get(5));
			y1 = StringUtil.atod((String)tokens.get(6));

			// Calculate the spacing of nodes on the main stem...
			dx = (x1 - x0)/(double)(numnodes - 1);
			dy = (y1 - y0)/(double)(numnodes - 1);

			// Break out of this loop and go to the next level (do not use a continue...
		}
		else if (token0.equalsIgnoreCase("TITLE")) {
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,	"Found TITLE command");
			}
			double fontSize = StringUtil.atod((String)tokens.get(1));
			double titleX = StringUtil.atod((String)tokens.get(2));
			double titleY = StringUtil.atod((String)tokens.get(3));
			String title = (String)tokens.get(4);
			for (int i = 5; i < numTokens; i++) {
				title = title + " ";
				title = title + (String)tokens.get(i);
			}
			setFont ( null, fontSize );
			setTitle ( title );
			setTitleX ( titleX );
			setTitleY ( titleY );
			// TODO SAM 2008-03-16 Evaluate whether the title is special or just another label/annotation
			// Also add as a label
			addLabel(titleX, titleY, fontSize, GRText.LEFT | GRText.BOTTOM, title);
			continue;
		}

		// Else, recursively process the child reaches...

		StopWatch timer = new StopWatch();
		timer.start();
		setNodeHead ( processMakenetNodes(getNodeHead(), in, filename,
			"", x0, y0, dx, dy, __openCount, __closeCount, 
			reachLevel, skipBlankNodes) );
		timer.stop();
		Message.printStatus(2, routine, "Reading net file took " + (int)timer.getSeconds() + " seconds.");

		// Now set the descriptions, which are dependent on database information...

		try {	
			setNodeDescriptions(nodeDataProvider);
		}
		catch (Exception e) {
			Message.printWarning(3, routine, e);
			convertNodeTypes();
			return false;
		}

		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,	"Done reading net file.");
			Message.printDebug(dl, routine, "Node is \"" + getNodeHead().getCommonID() + "\"");
		}
		resetComputationalOrder();
		convertNodeTypes();
		return true;
	}
	convertNodeTypes();
	return true;
}

/**
Reads a StateMod network file in either Makenet or XML format and returns the network that was generated.
@param filename the name of the file from which to read.
@param nodeDataProvider Object that fills in node data (e.g., from HydroBase).
Can be null if reading from an XML file.
@param skipBlankNodes whether blank nodes should be read from the Makenet file
or not.  Does not matter if reading from an XML file.
@return the network read from the file.
@throws Exception if an error occurs.
*/
public static StateMod_NodeNetwork readStateModNetworkFile(String filename, 
	StateMod_NodeDataProvider nodeDataProvider, boolean skipBlankNodes ) 
throws Exception {
	StateMod_NodeNetwork network = null;
	if (isXML(filename)) {
		network = readXMLNetworkFile(filename);
	}
	else {
		network = new StateMod_NodeNetwork();
		network.readMakenetNetworkFile(	nodeDataProvider, filename, skipBlankNodes);
	}
	return network;
}

/**
Reads a HydroBase_NodeNetwork from an XML Network file.
@param filename the name of the file to read.
@return the network read from the file.
*/
public static StateMod_NodeNetwork readXMLNetworkFile(String filename) 
throws Exception {
	String routine = "StateMod_NodeNetwork.readXMLNetworkFile";
	List<HydrologyNode> networkNodeList = new Vector<HydrologyNode>(); // List of all nodes read
	List<PropList> networkLinkList = new Vector<PropList>(); // List of all links read (lines from one node to another)
	List<PropList> networkLayoutList = new Vector<PropList>(); // List of all layouts
	List<HydrologyNode> networkAnnotationList = new Vector<HydrologyNode>(); // List of all annotations read - these are built-in
													// as opposed to run-time annotations from the StateMod GUI

	// java 8.
	//DOMParser parser = null;

	// Java 11.
	Document doc = null;
	try {	
		// Java 8.
		//parser = new DOMParser();
		//parser.parse(filename);

		// Java 11.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.parse(filename);
		doc.getDocumentElement().normalize();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading XML Network file \"" + filename + "\"");
		Message.printWarning(2, routine, e);
		throw new Exception("Error reading XML Network file \"" + filename + "\"");
	}

	// Now get information from the document.  For now don't hold the document as a data member...
	// Java 8.
	//Document doc = parser.getDocument();

	// Loop through and process the document nodes, starting with the root node.
	// Pass data needed during processing but keep handing within static code

	// LeftX, LowerY, RightX, TopY, LegendX, LegendY - from network properties
	Double [] extentData = { Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN };
	// LeftX, LowerY, RightX, TopY, from checking network coordinates
	Double [] extentDataFromNodes = { Double.NaN, Double.NaN, Double.NaN, Double.NaN };
	
	// Left, right, top, bottom
	Double [] edgeBuffer = { Double.NaN, Double.NaN, Double.NaN, Double.NaN };
	
	readXMLNetworkFile_ProcessDocumentNode(doc, networkNodeList, networkLinkList, networkLayoutList,
		networkAnnotationList, extentData, extentDataFromNodes, edgeBuffer );
	
	// Check the extent data for the network against the extents from the nodes...
	// Legacy code adjusts the data limits to page size so for example a wide network will have its Y
	// limits adjusted to be higher
	Message.printStatus(2, routine, "Extents from network properties:  (" +
		extentData[0] + "," + extentData[1] + ") to (" + extentData[2] + "," + extentData[3] + ")");
	// True data limits are what we really want so that the page can be properly sized as needed...
	Message.printStatus(2, routine, "Extents from node data:  (" +
		extentDataFromNodes[0] + "," + extentDataFromNodes[1] + ") to (" + extentDataFromNodes[2] + "," + extentDataFromNodes[3] + ")");
	// TODO SAM 2011-07-08 However, the issue is with a new network, where the node data would be limiting
	// Need to resolve this - need a network editor tool to "recenter" with an edge buffer.
	//extentData[0] = extentDataFromNodes[0];
	//extentData[1] = extentDataFromNodes[1];
	//extentData[2] = extentDataFromNodes[2];
	//extentData[3] = extentDataFromNodes[3];

	HydrologyNode node = networkNodeList.get(0);

	StateMod_NodeNetwork network = null;
	if (node.getComputationalOrder() == -1) {
		network = new StateMod_NodeNetwork();
		network.calculateNetworkNodeData(networkNodeList, false);
	}
	else {
		network = readXMLNetworkFile_BuildNetworkFromXMLNodes(networkNodeList);
	}
	// Set the filename so that it can be selected by default, for example in save dialogs
	// This is in the base class
	network.setInputName ( filename );
	network.setAnnotationList(networkAnnotationList);
	network.setLayoutList(networkLayoutList);
	network.setLinkList(networkLinkList);

	network.setBounds(extentData[0], extentData[1], extentData[2], extentData[3]);
	if ( !extentData[4].isNaN() && !extentData[5].isNaN() ) {
		network.setLegendPosition(extentData[4], extentData[5]);
	}

	if (network != null) {
		network.convertNodeTypes();
		network.finalCheck(extentData[0], extentData[1], extentData[2], extentData[3], false);
	}	

	return network;
}

/**
Builds all the network connections based on individual network nodes read in
from an XML file and returns the network that was built.
@return a HydroBase_NodeNetwork with all its connections built.
*/
private static StateMod_NodeNetwork readXMLNetworkFile_BuildNetworkFromXMLNodes(
	List<HydrologyNode> networkNodeList) {
	// Put the nodes into an array for quicker iteration
	int size = networkNodeList.size();

	// Add the nodes to an array for quicker traversal.  The nodes will be
	// looped through entirely size*3 times, so with a large Vector 
	// performance will be impacted by all the casts.  
	HydrologyNode[] nodes = new HydrologyNode[size];
	for (int i = 0; i < size; i++) {
		nodes[i] = networkNodeList.get(i);
	}

	String dsid = null;
	String[] usid = null;
	// Right now every node has a String that tells what its upstream
	// and downstream nodes are.  No connections.  Find the nodes that
	// match the upstream and downstream node IDs and make the connections.
	for (int i = 0; i < size; i++) {
		dsid = nodes[i].getDownstreamNodeID();
		usid = nodes[i].getUpstreamNodeIDs();

		if (dsid != null && !dsid.equals("") && !dsid.equalsIgnoreCase("null")) {
			for (int j = 0; j < size; j++) {
				if (nodes[j].getCommonID().equals(dsid)) {
					nodes[i].setDownstreamNode(nodes[j]);
					j = size + 1;
				}
			}
		}
		
		for (int j = 0; j < usid.length; j++) {
			for (int k = 0; k < size; k++) {
				if (nodes[k].getCommonID().equals(usid[j])) {
					nodes[i].addUpstreamNode(nodes[k]);
					k = size + 1;
				}
			}
		}
	}

	// Put the nodes back in a list for placement back into the node network.
	List<HydrologyNode> v = new Vector<HydrologyNode>();
	for (int i = 0; i < size; i++) {
		v.add(nodes[i]);
	}

	StateMod_NodeNetwork network = new StateMod_NodeNetwork();
	network.setNetworkFromNodes(v);
	// TODO SAM 2011-07-08 Why does the shading in the network diagram not seem to work?
	// Also calculate secondary information like stream level.
	//network.calculateNetworkNodeData(networkNodeList, false);
	return network;
}

/**
Processes an annotation node from an XML file and builds the annotation that will appear on the network.
@param node the XML Node containing the annotation information.
*/
private static void readXMLNetworkFile_ProcessAnnotation(Node node, List<HydrologyNode> networkAnnotationList ) 
throws Exception {
	NamedNodeMap attributes = node.getAttributes();;
	Node attributeNode;
	String name = null;
	String value = null;	
	int nattributes = attributes.getLength();

	HydrologyNode hnode = new HydrologyNode();
	PropList p = new PropList("");
	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		if (name.equalsIgnoreCase("FontSize")) {
			p.set("OriginalFontSize", value);
		}
		else {
			p.set(name, value);
		}
	}
	hnode.setAssociatedObject(p);

	networkAnnotationList.add(hnode);
}

/**
Processes a document node while reading from an XML file, the data lists and arrays will be populated during
the read.
@param node the node to process
@param networkNodeList empty list of nodes
@param networkLinkList empty list of link data
@param networkLayoutList empty list of page layout data
@param networkAnnotationList empty list of annotations,
@param extentData empty array of extent data (xmin, xmax, ymin, ymax, legendx, legendy)
@param edgeBuffer empty array of buffer values (left, right, top, bottom)
@throws Exception if an error occurs.
*/
private static void readXMLNetworkFile_ProcessDocumentNode(Node node, List<HydrologyNode> networkNodeList,
	List<PropList> networkLinkList, List<PropList> networkLayoutList, List<HydrologyNode> networkAnnotationList,
	Double[] extentData, Double[] extentDataFromNodes, Double [] edgeBuffer )
throws Exception {
	NodeList children;
	switch (node.getNodeType()) {
		case Node.DOCUMENT_NODE:
			// The main data set node.  Get the data set type, etc.
			readXMLNetworkFile_ProcessDocumentNode(((Document)node).getDocumentElement(), networkNodeList,
				networkLinkList, networkLayoutList, networkAnnotationList, extentData, extentDataFromNodes,
				edgeBuffer );
			children = node.getChildNodes();
			if (children != null) {
				readXMLNetworkFile_ProcessDocumentNode(children.item(0), networkNodeList, networkLinkList,
					networkLayoutList, networkAnnotationList, extentData, extentDataFromNodes, edgeBuffer );
			}				
			break;
		case Node.ELEMENT_NODE:
			// Data set components.  Print the basic information...
			String elementName = node.getNodeName();
			if (elementName.equalsIgnoreCase("StateMod_Network")) {
				readXMLNetworkFile_ProcessStateMod_NetworkNode(node, extentData, edgeBuffer);
				// The main document node will have a list 
				// of children but components will not.
				// Recursively process each node...
				children = node.getChildNodes();
				if (children != null) {
					int len = children.getLength();
					for (int i = 0; i < len; i++) {
						readXMLNetworkFile_ProcessDocumentNode(children.item(i),networkNodeList,
							networkLinkList, networkLayoutList, networkAnnotationList, extentData,
							extentDataFromNodes, edgeBuffer );
					}
				}
			}
			else if (elementName.equalsIgnoreCase("PageLayout")) {
				readXMLNetworkFile_ProcessLayoutNode(node, networkLayoutList);
			}
			else if (elementName.equalsIgnoreCase("Node")) {
				readXMLNetworkFile_ProcessNode(node, networkNodeList, extentData, extentDataFromNodes );
			}
			else if (elementName.equalsIgnoreCase("Annotation")) {
				readXMLNetworkFile_ProcessAnnotation(node, networkAnnotationList );
			}			
			else if (elementName.equalsIgnoreCase("Link")) {
				readXMLNetworkFile_ProcessLink(node, networkLinkList);
			}
			break;
	}
}

/**
Processes a "Downstream" node containing the ID of the downstream node from the Network node.
@param hnode the HydroBase_Node being built.
@param node the XML node read from the file.
@throws Exception if an error occurs.
*/
private static void readXMLNetworkFile_ProcessDownstreamNode(HydrologyNode hnode, Node node) 
throws Exception
{
	NamedNodeMap attributes = node.getAttributes();
	Node attributeNode;
	int nattributes = attributes.getLength();
	String name = null;
	String value = null;

	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		if (name.equalsIgnoreCase("ID")) {
			hnode.setDownstreamNodeID(value);
		}
	}
}

/**
Called by the readXML code when processing a Layout node.
@param node the node being read.
*/
private static void readXMLNetworkFile_ProcessLayoutNode(Node node, List<PropList> networkLayoutList ) {
	NamedNodeMap attributes;
	Node attributeNode;
	String name = null;
	String value = null;
	
	attributes = node.getAttributes();
	int nattributes = attributes.getLength();

	PropList p = new PropList("Layout");
	p.set("ID=\"Page Layout #" + (networkLayoutList.size() + 1) + "\"");
	p.set("PaperSize=\"" + DEFAULT_PAPER_SIZE + "\"");
	p.set("PageOrientation=\"" + DEFAULT_PAGE_ORIENTATION + "\"");
	p.set("NodeLabelFontSize=\"" + DEFAULT_FONT_SIZE + "\"");
	p.set("NodeSize=\"" + DEFAULT_NODE_SIZE + "\"");
	p.set("IsDefault=\"false\"");
	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		if (name.equalsIgnoreCase("ID")) {
			p.set("ID=\"" + value + "\"");
		}
		if (name.equalsIgnoreCase("IsDefault")) {
			p.set("IsDefault=\"" + value + "\"");
		}
		if (name.equalsIgnoreCase("PaperSize")) {
			p.set("PaperSize=\"" + value + "\"");
		}
		if (name.equalsIgnoreCase("PageOrientation")) {
			p.set("PageOrientation=\"" + value + "\"");
		}		
		if (name.equalsIgnoreCase("NodeLabelFontSize")) {
			p.set("NodeLabelFontSize=\"" + value + "\"");
		}
		if (name.equalsIgnoreCase("NodeSize")) {
			p.set("NodeSize=\"" + value + "\"");
		}
	}
	networkLayoutList.add(p);
}

/**
Processes a link node from an XML file and builds the link that will appear on the network.
@param node the XML Node containing the link information.
*/
private static void readXMLNetworkFile_ProcessLink(Node node, List<PropList> networkLinkList) 
throws Exception {
	NamedNodeMap attributes = node.getAttributes();;
	Node attributeNode;
	String name = null;
	String value = null;	
	int nattributes = attributes.getLength();

	PropList p = new PropList("");
	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		p.set(name, value);
	}

	networkLinkList.add(p);
}

/**
Process the data attributes of a HydroBase_Node in the XML file.
@param node the XML document node being processed
@param networkNodeList the list of nodes from the network
@param extentData the network extent (from network properties)
@param extentDataFromNodes the network extent from node coordinates
@throws Exception if an error occurs.
*/
private static void readXMLNetworkFile_ProcessNode(Node node, List<HydrologyNode> networkNodeList,
		Double [] extentData, Double [] extentDataFromNodes ) 
throws Exception {
	NamedNodeMap attributes = node.getAttributes();;
	Node attributeNode;
	String area = null;
	String precip = null;
	String name = null;
	String value = null;	
	int nattributes = attributes.getLength();

	HydrologyNode hnode = new HydrologyNode();
	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		if (name.equalsIgnoreCase("AlternateX")) {
			hnode.setDBX(Double.valueOf(value).doubleValue());
		}
		else if (name.equalsIgnoreCase("AlternateY")) {
			hnode.setDBY(Double.valueOf(value).doubleValue());
		}
		else if (name.equalsIgnoreCase("Area")) {
			area = value;
			hnode.setArea(Double.valueOf(value).doubleValue());
		}
		else if (name.equalsIgnoreCase("ComputationalOrder")) {
			hnode.setComputationalOrder( Integer.decode(value).intValue());
		}
		else if (name.equalsIgnoreCase("Description")) {
			hnode.setDescription(value);
		}
		else if (name.equalsIgnoreCase("ID")) {
			hnode.setCommonID(value);
		}
		// FIXME SAM 2008-12-10 Support both in the code: legacy "IsBaseflow" and new "IsNaturalFlow"
		else if (name.equalsIgnoreCase("IsBaseflow") || name.equalsIgnoreCase("IsNaturalFlow")) {
			if (value.equalsIgnoreCase("true")) {
				hnode.setIsNaturalFlow(true);
			}
			else {
				hnode.setIsNaturalFlow(false);
			}
		}
		else if (name.equalsIgnoreCase("IsImport")) {
			if (value.equalsIgnoreCase("true")) {
				hnode.setIsImport(true);
			}
			else {
				hnode.setIsImport(false);
			}
		}		
		else if (name.equalsIgnoreCase("LabelAngle")) {
			hnode.setLabelAngle(Double.valueOf(value).doubleValue());
		}
		else if (name.equalsIgnoreCase("LabelPosition")) {
			int div = hnode.getLabelDirection() / 10;
			if (value.equalsIgnoreCase("AboveCenter")) {
				hnode.setLabelDirection((div * 10) + 1);
			}
			else if (value.equalsIgnoreCase("UpperRight")) {
				hnode.setLabelDirection((div * 10) + 7);
			}			
			else if (value.equalsIgnoreCase("Right")) {
				hnode.setLabelDirection((div * 10) + 4);
			}
			else if (value.equalsIgnoreCase("LowerRight")) {
				hnode.setLabelDirection((div * 10) + 8);
			}			
			else if (value.equalsIgnoreCase("BelowCenter")) {
				hnode.setLabelDirection((div * 10) + 2);
			}
			else if (value.equalsIgnoreCase("LowerLeft")) {
				hnode.setLabelDirection((div * 10) + 5);
			}			
			else if (value.equalsIgnoreCase("Left")) {
				hnode.setLabelDirection((div * 10) + 3);
			}
			else if (value.equalsIgnoreCase("UpperLeft")) {
				hnode.setLabelDirection((div * 10) + 6);
			}
			else if (value.equalsIgnoreCase("Center")) {
				hnode.setLabelDirection((div * 10) + 1);
			}			
			else {
				hnode.setLabelDirection((div * 10) + 1);
			}
		}
		else if (name.equalsIgnoreCase("NetID")) {
			hnode.setNetID(value);
		}
		else if (name.equalsIgnoreCase("NodeInReachNum")) {
			hnode.setNodeInReachNumber(	Integer.decode(value).intValue());
		}
		else if (name.equalsIgnoreCase("Precipitation")) {
			precip = value;
			hnode.setPrecip(Double.valueOf(value).doubleValue());
		}
		else if (name.equalsIgnoreCase("ReachCounter")) {
			hnode.setReachCounter( Integer.decode(value).intValue());
		}		
		else if (name.equalsIgnoreCase("ReservoirDir")) {
			int mod = hnode.getLabelDirection() % 10;
			if (value.equalsIgnoreCase("Up")) {
				hnode.setLabelDirection(20 + mod);
			}
			else if (value.equalsIgnoreCase("Down")) {
				hnode.setLabelDirection(10 + mod);
			}
			else if (value.equalsIgnoreCase("Left")) {
				hnode.setLabelDirection(40 + mod);
			}
			else if (value.equalsIgnoreCase("Right")) {
				hnode.setLabelDirection(30 + mod);
			}
			else {
				hnode.setLabelDirection(40 + mod);
			}
		}			
		else if (name.equalsIgnoreCase("Serial")) {
			hnode.setSerial( Integer.decode(value).intValue());
		}				
		else if (name.equalsIgnoreCase("TributaryNum")) {
			hnode.setTributaryNumber( Integer.decode(value).intValue());
		}						
		else if (name.equalsIgnoreCase("Type")) {
			hnode.setVerboseType(value);
		}								
		else if (name.equalsIgnoreCase("UpstreamOrder")) {
			hnode.setUpstreamOrder(
				Integer.decode(value).intValue());
		}						
		else if (name.equalsIgnoreCase("X")) {
			hnode.setX(Double.valueOf(value).doubleValue());
			// Left...
			if ( Double.isNaN(extentDataFromNodes[0]) ) {
				extentDataFromNodes[0] = hnode.getX();
			}
			else {
				extentDataFromNodes[0] = Math.min(extentDataFromNodes[0], hnode.getX());
			}
			// Right...
			if ( Double.isNaN(extentDataFromNodes[2]) ) {
				extentDataFromNodes[2] = hnode.getX();
			}
			else {
				extentDataFromNodes[2] = Math.max(extentDataFromNodes[2], hnode.getX());
			}
		}
		else if (name.equalsIgnoreCase("Y")) {
			hnode.setY(Double.valueOf(value).doubleValue());
			// Bottom...
			if ( Double.isNaN(extentDataFromNodes[1]) ) {
				extentDataFromNodes[1] = hnode.getY();
			}
			else {
				extentDataFromNodes[1] = Math.min(extentDataFromNodes[1], hnode.getY());
			}
			// Top...
			if ( Double.isNaN(extentDataFromNodes[3]) ) {
				extentDataFromNodes[3] = hnode.getY();
			}
			else {
				extentDataFromNodes[3] = Math.max(extentDataFromNodes[3], hnode.getY());
			}
		}
	}

	if (area != null && precip != null) {
		area = area.trim();
		precip = precip.trim();
		hnode.parseAreaPrecip(area + "*" + precip);
	}
	else if (area != null && precip == null) {
		hnode.parseAreaPrecip(area);
	}
	else {
		// do nothing
	}

	NodeList children = node.getChildNodes();

	if (children != null) {
		String elementName = null;
		int len = children.getLength();
		for (int i = 0; i < len; i++) {
			node = children.item(i);
			elementName = node.getNodeName();
			// Evaluate the nodes attributes...
			if (elementName.equalsIgnoreCase("DownstreamNode")) {
				readXMLNetworkFile_ProcessDownstreamNode(hnode, node );
			}
			else if (elementName.equalsIgnoreCase("UpstreamNode")) {
				readXMLNetworkFile_ProcessUpstreamNodes(hnode, node);
			}
			else {}
		}
	}

	networkNodeList.add(hnode);
}

/**
Called by the readXML code when processing a StateMod_Network node.
@param node the XML node being read
@param extentData the maximum data coordinates of the network, considering node coordinates
@param edgeBuffer the additional edge buffer, in node coordinate units, that should be added to the
edges of the network when rendering
*/
private static void readXMLNetworkFile_ProcessStateMod_NetworkNode(Node node, Double [] extentData,
	Double [] edgeBuffer ) 
throws Exception {
	String routine = "StateMod_NodeNetwork.processStateMod_NetworkNode";
	NamedNodeMap attributes;
	Node attributeNode;
	String name = null;
	String value = null;
	
	attributes = node.getAttributes();
	int nattributes = attributes.getLength();
	
	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		if (name.equalsIgnoreCase("XMin")) {
			extentData[0] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read Xmin=" + extentData[0] );
		}
		else if (name.equalsIgnoreCase("YMin")) {
			extentData[1] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read Ymin=" + extentData[1] );
		}
		else if (name.equalsIgnoreCase("XMax")) {
			extentData[2] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read Xmax=" + extentData[2] );
		}
		else if (name.equalsIgnoreCase("YMax")) {
			extentData[3] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read Ymax=" + extentData[3] );
		}
		else if (name.equalsIgnoreCase("EdgeBufferLeft")) {
			edgeBuffer[0] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read EdgeBufferLeft=" + edgeBuffer[0] );
		}
		else if (name.equalsIgnoreCase("EdgeBufferRight")) {
			edgeBuffer[1] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read EdgeBufferRight=" + edgeBuffer[1] );
		}
		else if (name.equalsIgnoreCase("EdgeBufferTop")) {
			edgeBuffer[2] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read EdgeBufferTop=" + edgeBuffer[2] );
		}
		else if (name.equalsIgnoreCase("EdgeBufferBottom")) {
			edgeBuffer[3] = Double.valueOf(value);
			Message.printStatus(2, routine, "Read EdgeBufferBottom=" + edgeBuffer[3] );
		}
		else if (name.equalsIgnoreCase("LegendX")) {
			extentData[4] = Double.valueOf(value);
		}
		else if (name.equalsIgnoreCase("LegendY")) {
			extentData[5] = Double.valueOf(value);
		}
	}
}

/**
Processes an "Upstream" node containing the IDs of the upstream nodes from the Network node.
@param hnode the HydroBase_Node being built.
@param node the XML node read from the file.
@throws Exception if an error occurs.
*/
private static void readXMLNetworkFile_ProcessUpstreamNodes(HydrologyNode hnode, Node node) 
throws Exception {
	NamedNodeMap attributes = node.getAttributes();
	Node attributeNode;
	int nattributes = attributes.getLength();
	String name = null;
	String value = null;

	for (int i = 0; i < nattributes; i++) {
		attributeNode = attributes.item(i);
		name = attributeNode.getNodeName();
		value = attributeNode.getNodeValue();
		if (name.equalsIgnoreCase("ID")) {
			hnode.addUpstreamNodeID(value);
		}
	}
}

/**
Set whether fancy descriptions should be generated (true) or not (false).
@param fancydesc true to turn on fancy descriptions.
*/
public void setFancyDesc(boolean fancydesc) {
	__createFancyDescription = fancydesc;
}

/**
Sets the node descriptions (names) of all nodes in the network, when reading a
makenet network file.  Station names are read from HydroBase.
If __createFancyDescription is true, then fancy descriptions will be set.  
If the user description has been set, it will be used as is.
@param nodeDataProvider Object that fills in node data (e.g., from HydroBase).
@throws Exception if an error occurs.
*/
public void setNodeDescriptions( StateMod_NodeDataProvider nodeDataProvider )
throws Exception
{
	if ( nodeDataProvider != null ) {
		nodeDataProvider.setNodeDescriptions( this, __createFancyDescription, __createOutputFiles );
	}
}

}
