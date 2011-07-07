package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;

import cdss.domain.hydrology.network.HydrologyNode;

// TODO SAM 2011-07-07 These methods do not support the undo feature - need to enable
/**
This toolkit provides utility methods for processing network data in coordination with
the network editor.  It is intended to remove some of the utilitarian code from the other objects, and
coordinate where appropriate.
*/
public class StateMod_Network_EditorToolkit {
	
/**
Editor JFrame.  If null then dialog positioning may be off.
*/
private StateMod_Network_JFrame __editorJFrame = null;

/**
Editor JComponent.  May be null if node data are edited directly.
*/
private StateMod_Network_JComponent __editorJComponent = null;

/**
Network being edited.
*/
private StateMod_NodeNetwork __network = null;

/**
Constructor.  The major objects used in editing are passed in, but may not be required for all actions.
@param editorJFrame the controlling editor window (e.g., to allow popup dialogs to position)
@param editorComponent the graphical editor component that renders the network, which does some internal
optimized data management
@param network the network that will be manipulated, although subsets of the network may be passed in to
specific methods
*/
public StateMod_Network_EditorToolkit ( StateMod_Network_JFrame editorJFrame,
	StateMod_Network_JComponent editorJComponent, StateMod_NodeNetwork network )
{
	this.__editorJFrame = editorJFrame;
	this.__editorJComponent = editorJComponent;
	this.__network = network;
}

/**
Position the nodes evenly between the end nodes.
@return the number of nodes that were edited
*/
protected int positionNodesEvenlyBetweenEndNodes ( List<HydrologyNode> nodeList )
{	if ( nodeList.size() < 3 ) {
		return 0;
	}
	String routine = getClass().getName() + "positionNodesEvenlyBetweenEndNodes";
	// First make a copy of the list so that the sort does not change the original list
	List<HydrologyNode> nodeListSorted = null;
	// Sort the list so that it is in the order from upstream to downstream
	int editCount = 0;
	try {
		nodeListSorted = sortNodesSequential ( nodeList );
	}
	catch ( Exception e ) {
		// Likely not in a line
		Message.printWarning ( 1, routine,
			"Error positioning nodes (" + e +
			").  Make sure that nodes are in a single reach with no branches.",
			this.__editorJFrame);
		Message.printWarning(3, routine, e);
		return 0;
	}
	HydrologyNode firstNode = nodeListSorted.get(0);
	HydrologyNode lastNode = nodeListSorted.get(nodeListSorted.size() - 1);
	// Compute the delta between the first node and last node
	double dx = (lastNode.getX() - firstNode.getX())/(nodeListSorted.size() - 1);
	double dy = (lastNode.getY() - firstNode.getY())/(nodeListSorted.size() - 1);
	// Apply the delta to the middle nodes
	HydrologyNode node;
	for ( int i = 1; i < nodeListSorted.size() - 1; i++ ) {
		node = nodeListSorted.get(i);
		node.setX(firstNode.getX() + dx*i);
		node.setY(firstNode.getY() + dy*i);
		node.setDirty(true);
		++editCount;
	}
	return editCount;
}

/**
Adjust the X coordinate of all the given nodes to match the X coordinate of the confluence node.
The operation will only be performed if there is only one confluence node.
@return the number of nodes that were edited
*/
protected int setNodeXToConfluenceX ( List<HydrologyNode> nodeList )
{
	// First find the confluence node
	HydrologyNode confNode = null;
	int confNodeCount = 0;
	for ( HydrologyNode node : nodeList ) {
		if ( node.getType() == HydrologyNode.NODE_TYPE_CONFLUENCE ) {
			confNode = node;
			++confNodeCount;
		}
	}
	int editCount = 0;
	if ( confNodeCount == 1 ) {
		for ( HydrologyNode node : nodeList ) {
			if ( node != confNode ) {
				// Set the coordinate...
				node.setX(confNode.getX());
				node.setDirty(true);
				++editCount;
			}
		}
	}
	return editCount;
}

/**
Adjust the Y coordinate of all the given nodes to match the Y coordinate of the confluence node.
The operation will only be performed if there is only one confluence node.
@return the number of nodes that were edited
*/
protected int setNodeYToConfluenceY ( List<HydrologyNode> nodeList )
{
	// First find the confluence node
	HydrologyNode confNode = null;
	int confNodeCount = 0;
	for ( HydrologyNode node : nodeList ) {
		if ( node.getType() == HydrologyNode.NODE_TYPE_CONFLUENCE ) {
			confNode = node;
			++confNodeCount;
		}
	}
	int editCount = 0;
	if ( confNodeCount == 1 ) {
		for ( HydrologyNode node : nodeList ) {
			if ( node != confNode ) {
				// Set the coordinate...
				node.setY(confNode.getY());
				node.setDirty(true);
				++editCount;
			}
		}
	}
	return editCount;
}

/**
Sort a list of nodes to be sequential upstream to downstream.  This ensures that processing that requires
a sequence will have expected data.  A new list is returned, but original node objects are not copied.
*/
protected List<HydrologyNode> sortNodesSequential ( List<HydrologyNode> nodeListOrig )
throws RuntimeException
{
	List<HydrologyNode> nodeList = new Vector(nodeListOrig);
	List<HydrologyNode> nodeListSorted = new Vector();
	// First find a node that has only one of the other nodes downstream (no matching upstream).
	// This will be the upstream node.
	HydrologyNode upstreamNode = null;
	for ( HydrologyNode node : nodeList ) {
		List<HydrologyNode> nodeUpstreamNodes = node.getUpstreamNodes();
		if ( (nodeUpstreamNodes == null) || (nodeUpstreamNodes.size() == 0) ) {
			// No upstream nodes so this is the most upstream node
			upstreamNode = node;
		}
		else {
			boolean foundUpstream = false;
			for ( HydrologyNode node2 : nodeUpstreamNodes ) {
				for ( HydrologyNode node3 : nodeList ) {
					if ( (node3 != node) && (node2 == node3) ) {
						// Found an upstream node so this is not the upstream node in the list
						foundUpstream = true;
						break;
					}
				}
				if ( foundUpstream ) {
					// No need to keep searching
					break;
				}
			}
			if ( !foundUpstream ) {
				// The node has no upstream nodes in the list so this is the upstream node
				upstreamNode = node;
				break;
			}
		}
	}
	if ( upstreamNode == null ) {
		throw new RuntimeException ( "Could not find node in list that has no upstream nodes." );
	}
	// Now go through the list and add in the order upstream to downstream.  Remove the upstream node
	// because it does not need to be considered
	nodeList.remove ( upstreamNode );
	nodeListSorted.add ( upstreamNode );
	HydrologyNode nodePrev = upstreamNode;
	while ( true ) {
		int nodeListSortedSizePrev = nodeListSorted.size();
		for ( HydrologyNode node : nodeList ) {
			if ( node == nodePrev.getDownstreamNode() ) {
				// Found the node downstream from the upstream so add it to the list
				nodeListSorted.add ( node );
				nodeList.remove ( node );
				nodePrev = node;
				// Start the search again
				break;
			}
		}
		if ( nodeListSorted.size() == nodeListSortedSizePrev ) {
			// No further matches found
			break;
		}
	}
	// If the number in the list is not the same as the original list there was a disconnect somewhere
	if ( nodeListSorted.size() != nodeListOrig.size() ) {
		throw new RuntimeException ( "Sorted node list (" + nodeListSorted.size() +
			") is not same length as original list (" + nodeListOrig.size() + ")." );
	}
	return nodeListSorted;
}

/**
Write list files for the main station lists.  These can then be used with
list-based commands in StateDMI.
The user is prompted for a list file name.
*/
protected void writeListFiles()
{	String routine = "StateMod_Network_JComponent.writeListFiles";

	String lastDirectorySelected = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc = JFileChooserFactory.createJFileChooser( lastDirectorySelected);
	fc.setDialogTitle("Select Base Filename for List Files");
	SimpleFileFilter tff = new SimpleFileFilter("txt", "Text Files");
	fc.addChoosableFileFilter(tff);
	SimpleFileFilter csv_ff = new SimpleFileFilter("csv", "Comma-separated Values");
	fc.addChoosableFileFilter(csv_ff);
	fc.setFileFilter(csv_ff);
	fc.setDialogType(JFileChooser.SAVE_DIALOG);	

	int retVal = fc.showSaveDialog(this.__editorJComponent);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return;
	}
	
	String currDir = (fc.getCurrentDirectory()).toString();
	
	if (!currDir.equalsIgnoreCase(lastDirectorySelected)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	String filename = fc.getSelectedFile().getPath();

	// Station types...

	int[] types = {
		-1, // All nodes
		HydrologyNode.NODE_TYPE_FLOW, // Stream gage
		HydrologyNode.NODE_TYPE_DIV, // Diversion
		HydrologyNode.NODE_TYPE_DIV_AND_WELL,// Diversion + Well
		HydrologyNode.NODE_TYPE_PLAN, // Plan stations
		HydrologyNode.NODE_TYPE_RES, // Reservoir
		HydrologyNode.NODE_TYPE_ISF, // Instream flow
		HydrologyNode.NODE_TYPE_WELL, // Well
		HydrologyNode.NODE_TYPE_OTHER // Not other stations
	};
	
	/* TODO SAM 2006-01-03 Just use node abbreviations from network
	// Suffix for output, to be added to file basename...

	String[] nodetype_string = {
		"All",
		"StreamGage",
		"Diversion",
		"DiversionAndWell",
		"Plan",
		"Reservoir",
		"InstreamFlow",
		"Well",
		// TODO SAM 2006-01-03 Evaluate similar to node type above.
		//"StreamEstimate",
		"Other"
	};
	*/

	// Put the extension on the file (user may or may not have added)...

	if ( fc.getFileFilter() == tff ) {
		filename = IOUtil.enforceFileExtension ( filename, "txt" );
	}
	else if ( fc.getFileFilter() == csv_ff ) {
		filename = IOUtil.enforceFileExtension ( filename, "csv" );
	}

	// Now get the base name and remaining extension so that the basename can be adjusted below...

	int lastIndex = filename.lastIndexOf(".");
	String front = filename.substring(0, lastIndex);
	String end = filename.substring((lastIndex + 1), filename.length());

	String outputFilename = null;
	List<HydrologyNode> v = null;

	String warning = "";
	String [] comments = null;
	for (int i = 0; i < types.length; i++) {
		v = this.__editorJComponent.getNodesForType(types[i]);
		
		if (v != null && v.size() > 0) {

			comments = new String[1];
			if ( types[i] == -1 ) {
				comments[0] = "The following list contains data for all node types.";
				outputFilename = front + "_All." + end;
			}
			else {
				comments[0] = "The following list contains data for the following node type:  " +
					HydrologyNode.getTypeString( types[i], HydrologyNode.ABBREVIATION) +
					" (" + HydrologyNode.getTypeString(types[i], HydrologyNode.FULL) + ")";
				outputFilename = front + "_" +
					HydrologyNode.getTypeString(types[i], HydrologyNode.ABBREVIATION) + "." + end;
			}
	
			try {
				StateMod_NodeNetwork.writeListFile( outputFilename, ",", false, v, comments, false );
			}
			catch (Exception e) {
				Message.printWarning(3, routine, e);
				warning += "\nUnable to create list file \"" + outputFilename + "\"";
			}
		}
	}
	// TODO SAM 2006-01-03 Write at level 1 since this is currently triggered from an
	// interactive action.  However, may need to change if executed in batch mode.
	if ( warning.length() > 0 ) {
		Message.printWarning(1, routine, warning );
	}
}

}