package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import RTi.GR.GRText;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

import cdss.domain.hydrology.network.HydrologyNode;
import cdss.domain.hydrology.network.HydrologyNodeNetwork;
import cdss.domain.hydrology.network.RiverLine;

//TODO SAM 2007-02-18 Need to remove circular dependency with StateMod
/*
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_PrfGageData;
import DWR.StateMod.StateMod_RiverNetworkNode;
*/

public class StateMod_NodeNetwork extends HydrologyNodeNetwork
{
	/**
	 * Data used when reading the old Makenet file.
	 * FIXME SAM 2008-03-15 Need to evaluate whether this can be in read code only.
	 */
	
	/**
	Counter for the number of reaches closed for processing (to count matches for
	__openCount).
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
	
	// Start data needed by XML reader
	
	/**
	Used in reading in an XML file.
	*/
	private static Vector __aggregateAnnotations = null;

	/**
	Vector for aggregating layout information when reading statically from the
	XML files.
	*/
	private static Vector __aggregateLayouts = null;

	/**
	Used in reading in an XML file.
	*/
	private static Vector __aggregateLinks = null;

	/**
	Contains the nodes read from an XML file.  This is a static data member because
	the method used to read in an XML file and create a network is static.
	*/
	private static Vector __aggregateNodes;
	
	// End XML data
	
	/**
	Boolean values that specify if all the bounds of the network were read in
	during a static XML read.
	*/
	private static boolean 
		__lxSet = false,
		__bySet = false,
		__rxSet = false,
		__tySet = false,
		__legendXSet = false,
		__legendYSet = false;
	
	/**
	Holds the bounds of the network when read in from the network file.
	*/
	private static double
		__staticLX = 0,
		__staticBY = 0,
		__staticRX = 0,
		__staticTY = 0,
		__staticLegendX = 0,
		__staticLegendY = 0;
	
	/**
	Whether to generate fancy node descriptions or not.
	*/
	private boolean	__createFancyDescription;

	/**
	Whether to create output files or not.
	*/
	private boolean	__createOutputFiles;

	/**
	Whether the database is up or not.
	*/
	private boolean	__isDatabaseUp;
	
	/**
	DMI instance for connecting to the database.
	*/
	/*
	private HydroBaseDMI __dmi;
	*/
	// FIXME SAM 2008-03-15 Use the DMI with StateMod utilities to fill out data.
	
	/**
	 * Construct a StateMod_NodeNetwork.
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
	Builds all the network connections based on individual network nodes read in
	from an XML file and returns the network that was built.
	@return a HydroBase_NodeNetwork with all its connections built.
	*/
	private static StateMod_NodeNetwork buildNetworkFromXMLNodes() {
		// put the nodes into an array for quicker iteration
		int size = __aggregateNodes.size();

		// add the nodes to an array for quicker traversal.  The nodes will be
		// looped through entirely size*3 times, so with a large Vector 
		// performance will be impacted by all the casts.  
		HydrologyNode[] nodes = new HydrologyNode[size];
		for (int i = 0; i < size; i++) {
			nodes[i] = (HydrologyNode)__aggregateNodes.elementAt(i);
			// FIXME 2008-03-15 Need to remove WIS code
			//nodes[i].setInWIS(false);
		}

		// no longer needed
		__aggregateNodes = null;

		String dsid = null;
		String[] usid = null;
		// right now every node has a String that tells what its upstream
		// and downstream nodes are.  No connections.  Find the nodes that
		// match the upstream and downstream node IDs and make the connections.
		for (int i = 0; i < size; i++) {
			dsid = nodes[i].getDownstreamNodeID();
			usid = nodes[i].getUpstreamNodeIDs();

			if (dsid != null && !dsid.equals("") 
			    && !dsid.equalsIgnoreCase("null")) {
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

		// put the nodes back in a Vector for placement back into the 
		// node network.
		Vector v = new Vector();
		for (int i = 0; i < size; i++) {
			v.add(nodes[i]);
		}

		StateMod_NodeNetwork network = new StateMod_NodeNetwork();
		network.setNetworkFromNodes(v);
		return network;
	}
	
	/**
	Creates a HydroBase_NodeNetwork from a Vector of StateMod_RiverNetworkNodes.
	@param nodes the nodes from which to create a HydroBase_NodeNetwork.
	@return the HydroBase_NodeNetwork that was built.<p>
	REVISIT (JTS - 2004-07-03)<br>
	Should not be a static returning a method, I think ...
	*/
	public static StateMod_NodeNetwork createFromStateModVector(Vector nodes) {
		int size = nodes.size();
		
		HydrologyNode[] nodeArray = new HydrologyNode[size];
		StateMod_RiverNetworkNode rnn = null;
		for (int i = size - 1; i >= 0; i--) {
			rnn = (StateMod_RiverNetworkNode)nodes.elementAt(i);
			nodeArray[i] = new HydrologyNode();
			// FIXME 2008-03-15 Need to remove WIS code
			//nodeArray[i].setInWIS(false);
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
						nodeArray[j].addUpstreamNodeID(
							nodeArray[i].getCommonID());
					}
				}
			}
		}

		Vector v = new Vector();
		for (int i = 0; i < size; i++) {
			v.add(nodeArray[i]);
		}

		StateMod_NodeNetwork network = new StateMod_NodeNetwork();
		network.calculateNetworkNodeData(v, false);
		return network;
	}
	
	/**
	Creates a StateMod_RiverNodeNetwork from the nodes in the HydroBase_NodeNetwork.
	The output contains only actual nodes.  Therefore, confluence nodes are
	skipped.
	@return the Vector of StateMod_RiverNodeNetwork nodes.
	*/
	public Vector createStateModRiverNetwork() {
		boolean done = false;
		HydrologyNode holdNode = null;
		HydrologyNode node = getMostUpstreamNode();	
		HydrologyNode dsNode = null;
		StateMod_RiverNetworkNode rnn = null;
		Vector v = new Vector();
		int node_type;		// Type for current node.
		int dsNode_type;	// Type for downstream node.
		HydrologyNode node_downstream = null;		// Used to find a real
		HydrologyNode real_node_downstream = null;	// downstream node.
		// Create a blank node used for disappearing streams.  The identifiers
		// will be empty strings...
		HydrologyNode blankNode = new HydrologyNode();
		// FIXME SAM 2008-03-15 Need to remove WIS code
		//__blankNode.setInWIS(__inWIS);
		blankNode.setDescription("SURFACE WATER LOSS");
		blankNode.setUserDescription("SURFACE WATER LOSS");
		blankNode.setType(HydrologyNode.NODE_TYPE_UNKNOWN);
		while (!done) {
			node_type = node.getType();
			// REVISIT SAM 2004-07-11 - the following may fail if no valid
			// downstream node is found - error handling below is not used.
			if (	(node_type == HydrologyNode.NODE_TYPE_CONFLUENCE) ||
				(node_type == HydrologyNode.NODE_TYPE_XCONFLUENCE) ||
				(node_type == HydrologyNode.NODE_TYPE_BLANK)) {
				node = getDownstreamNode(node, POSITION_COMPUTATIONAL);
				continue;
			}

			// Create a new instance and set the identifier...

			rnn = new StateMod_RiverNetworkNode();
			rnn.setID(node.getCommonID());

			// Set the node type.  This can be used later when filling with
			// HydroBase, to format the name (remove this code if that
			// convention is phased out).
			//
			// This code is the same when reading the stream estimate
			// stations command.
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
				if (	(dsNode_type== HydrologyNode.NODE_TYPE_BLANK)||
					(dsNode_type== HydrologyNode.NODE_TYPE_XCONFLUENCE) ||
					(dsNode_type == HydrologyNode.NODE_TYPE_CONFLUENCE)) {
					// We want to always show a real node in the
					// river network...
					real_node_downstream = findNextRealDownstreamNode(node);
					node_downstream = findNextRealOrXConfluenceDownstreamNode( node);
					//Message.printStatus ( 2, "",
					//"real ds node=" +
					//real_node_downstream.getCommonID() +
					//" real or X ds node=" +
					//node_downstream.getCommonID() );
					// If the downstream node in the reach is an
					// XCONFLUENCE (as opposed to a trib coming in)
					// then this is the last real node in
					// disappearing stream.  Use a blank for the
					// downstream node...
					//
					// Cannot simply check for the downstream node
					// to be an XCONFL because incoming tributaries
					// may be joined to the main stem with an XCONFL
					//
					// There may be cases where multiple XCONFL
					// nodes are in a row (e.g., to bend lines).  In
					// these cases, it is necessary to check the
					// downstream reach node type, rather than
					// make sure that the node is the same as the
					// immediate downstream node (as was done in
					// software prior to 2005-06-13).  This has not
					// been implemented.  The user should make sure
					// that multiple XCONFL nodes are not included
					// on a tributary reach that joins another
					// stream.
					//
					if ( node_downstream.getType() == HydrologyNode.NODE_TYPE_XCONFLUENCE ) {
						// Get node to check...
						/* Use for debugging
						HydroBase_Node temp_node;
						temp_node=
							getDownstreamNode(node,
							POSITION_REACH);
						Message.printStatus ( 2, "",
							"reach end =" + temp_node );
						if ( temp_node != null ) {
						Message.printStatus ( 2, "",
							"reach end ds =" +
							temp_node.
							getDownstreamNode() );
						}
						*/
						if (	node_downstream ==
							getDownstreamNode(node,
							POSITION_REACH).
							getDownstreamNode() ) {
							// This identifies XCONFL nodes
							// at the ends of tributary
							// reaches and works because the
							// nodes are clearly identified
							// on a reach...
							node_downstream = blankNode;
						}
						else if ( node_downstream.getNumUpstreamNodes() == 1 ) {
							// For some reason someone put
							// an XCONFL in a reach but did
							// not split out a trib.
							// Therefore the "main stem"
							// goes dry but is
							// computationally connected to
							// the next downstream node...
							node_downstream = blankNode;
						}
						else {
							// Picking up on a confluence
							// from another trib so use what
							// we normally would have...
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
		// REVISIT -- eliminate the need for hold nodes -- they signify an
		// error in the network.
			else if (node == holdNode) {
				done = true;
			}
			holdNode = node;	
			node = dsNode;
		}
		return v;	
	}
	
	/**
	Finalize before garbage collection.
	*/
	protected void finalize()
	throws Throwable
	{
		super.finalize();
	}
	
	/**
	Initialize data.
	*/
	private void initialize() {
		__closeCount = 			0;
		__isDatabaseUp = 		false;
		__createOutputFiles = 		true;
		__createFancyDescription = 	false;
		//In base... __fontSize = 			10.0;
		// FIXME 2008-03-15 Need to move to StateDMI
		//__dmi = 			null;
		//In base... __labelType = 			LABEL_NODES_NETID;
		//__legendDX = 			1.0;
		//__legendDY = 			1.0;
		//__legendX = 			0.0;
		//__legendY = 			0.0;
		__line = 			1;
		//In base...__nodes = 			new Vector();
		//In base...__plotCommands = 		new Vector();
		//In base...__nodeCount = 			1;
		//In base...__nodeDiam = 			10.0;
		//In base...__nodeHead = 			null;
		__openCount = 			0;
		__reachCounter = 		0;
		//In base... __title = 			"Node Network";
		//In base...__titleX = 			0.0;
		//In base...__titleY = 			0.0;
		//In base... __treatDryAsBaseflow = 		false;
	}
	
	/**
	Processes an annotation node from an XML file and builds the annotation that
	will appear on the network.
	@param node the XML Node containing the annotation information.
	*/
	private static void processAnnotation(Node node) 
	throws Exception {
		if (__aggregateAnnotations == null) {
			__aggregateAnnotations = new Vector();
		}
		NamedNodeMap attributes = node.getAttributes();;
		Node attributeNode;
		String name = null;
		String value = null;	
		int nattributes = attributes.getLength();

		HydrologyNode hnode = new HydrologyNode();
		// FIXME SAM 2008-03-15 Remove WIS code
		//hnode.setInWIS(__setInWIS);
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

		__aggregateAnnotations.add(hnode);
	}

	/**
	Processes a document node while reading from an XML file.
	@param node the node to process.
	@throws Exception if an error occurs.
	*/
	private static void processDocumentNodeForRead(Node node)
	throws Exception {
		NodeList children;
		switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE:
				// The main data set node.  Get the data set type, etc.
				processDocumentNodeForRead(
					((Document)node).getDocumentElement());
				children = node.getChildNodes();
				if (children != null) {
					processDocumentNodeForRead(children.item(0));
				}				
				break;
			case Node.ELEMENT_NODE:
				// Data set components.  Print the basic information...
				String elementName = node.getNodeName();
				if (elementName.equalsIgnoreCase("StateMod_Network")) {
					processStateMod_NetworkNode(node);
					// The main document node will have a list 
					// of children but components will not.
					// Recursively process each node...
					children = node.getChildNodes();
					if (children != null) {
						int len = children.getLength();
						for (int i = 0; i < len; i++) {
							processDocumentNodeForRead(
								children.item(i));
						}
					}
				}
				else if (elementName.equalsIgnoreCase("PageLayout")) {
					processLayoutNode(node);
				}
				else if (elementName.equalsIgnoreCase("Node")) {
					processNode(node);
				}
				else if (elementName.equalsIgnoreCase("Annotation")) {
					processAnnotation(node);
				}			
				else if (elementName.equalsIgnoreCase("Link")) {
					processLink(node);
				}
				break;
		}
	}

	/**
	Processes a "Downstream" node containing the ID of the downstream node from
	the Network node.
	@param hnode the HydroBase_Node being built.
	@param node the XML node read from the file.
	@throws Exception if an error occurs.
	*/
	private static void processDownstreamNode(HydrologyNode hnode, Node node) 
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
	private static void processLayoutNode(Node node) {
		NamedNodeMap attributes;
		Node attributeNode;
		String name = null;
		String value = null;
		
		attributes = node.getAttributes();
		int nattributes = attributes.getLength();

		PropList p = new PropList("Layout");
		p.set("ID=\"Page Layout #" + (__aggregateLayouts.size() + 1) + "\"");
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
		__aggregateLayouts.add(p);
	}
	
	/**
	Processes a link node from an XML file and builds the link that will appear 
	on the network.
	@param node the XML Node containing the link information.
	*/
	private static void processLink(Node node) 
	throws Exception {
		if (__aggregateLinks == null) {
			__aggregateLinks = new Vector();
		}
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

		__aggregateLinks.add(p);
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
	@param skipBlankNodes whether blank nodes should just be skipped when read
	in from the file.
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
			x0, y0, dx, dy, __openCount, __closeCount, _reachLevel,
			skipBlankNodes);
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
	@param skipBlankNodes whether to skip blank nodes when reading nodes in 
	from the file.
	@return a node filled with data from the makenet file.
	*/
	private HydrologyNode processMakenetNodes(HydrologyNode node, 
	BufferedReader netfp, String filename, String wd, double x0, double y0, 
	double dx, double dy, int openCount, int closeCount, int reachLevel,
	boolean skipBlankNodes) {
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
		Vector	list, 
			tokens;

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
					Message.printWarning(2, routine,
						"Skipping line " + __line + " in \""
						+ filename + "\"");
					continue;
				}
			}

			// ------------------------------------------------------------
			// Now evaluate node-level commands...
			// ------------------------------------------------------------
			token0 = new String((String)tokens.elementAt(0));
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,
					"token[0]=\"" + token0 + "\"");
			}
			if (token0.charAt(0) == '#') {
				// Comment line...
				continue;
			}
			if (token0.equalsIgnoreCase("DISTRICT")) {
				wd = new String((String)tokens.elementAt(1));
				Message.printStatus(2, routine,
					"Water district specified as " + wd);
				//HMPrintWarning(1, routine,
				//"DISTRICT command is no longer recognized.  Use " +
				//"full ID");
				continue;
			}
			else if (token0.equalsIgnoreCase("FONT")) {
				setFont((String)tokens.elementAt(1),
					StringUtil.atod((String)tokens.elementAt(2)));
				continue;
			}
			else if (token0.equalsIgnoreCase("TEXT")) {
				message = "The TEXT command is obsolete.  Use FONT";
				Message.printWarning(1, routine, message);
				printCheck(routine, 'W', message);
				continue;
			}
			else if (token0.equalsIgnoreCase("NODESIZE")) {
				message = "The NODESIZE command is obsolete.  Use" +
					" NODEDIAM";
				Message.printWarning(1, routine, message);
				printCheck(routine, 'W', message);
				setNodeDiam(StringUtil.atod(
					(String)tokens.elementAt(1)));
				continue;
			}
			else if (token0.equalsIgnoreCase("NODEDIAM")) {
				setNodeDiam(StringUtil.atod(
					(String)tokens.elementAt(1)));
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
					Message.printDebug(dl, routine,
						"Adding a reach above \"" 
						+ node.getCommonID() + "\" nupstream=" 
						+ node.getNumUpstreamNodes());
				}
				HydrologyNode node_upstream =
					processMakenetNodes(node, netfp, filename,
					wd, x, y, dx, dy, openCount, closeCount,
					reachLevel, skipBlankNodes);
				if (node_upstream == null) {
					// We have bad troubles.  Return null here to
					// back out also (we want to end the program).
					message  = "Major error processing nodes.";
					Message.printWarning(1, routine, message);
					printCheck(routine, 'W', message);
					return null;
				}
				// Else add the node...
				// This would have been done in the recurse!  
				// Don't need to add again...
				//node.addUpstreamNode(node_upstream);
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Added reach starting at \"" 
						+ node_upstream.getNetID() 
						+ "\" reachnum=" 
						+ node_upstream.getTributaryNumber() 
						+ " num=" + node_upstream.getSerial() 
						+ "(downstream is \"" 
						+ node.getNetID() + "\" [" 
						+ node.getSerial() + ", nupstream=" 
						+ node.getNumUpstreamNodes() + ")");
				}
				x += dx;
				y += dy;
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Now back at reach level " 
						+ reachLevel);
				}
			}
			else if (token0.equalsIgnoreCase("STREAM")) {
				// We are starting a stream(this will generally only
				// be called when recursing).

				// with graphing
				river.id = new String((String)tokens.elementAt(1));
				Message.printStatus(2, routine, "Starting stream \"" 
					+ river.id 
					+ "\".  Building network upstream...");
				numRiverNodes = StringUtil.atoi((String)tokens.elementAt(2));
				river_dx = StringUtil.atod((String)tokens.elementAt(3));
				river_dy = StringUtil.atod((String)tokens.elementAt(4));
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
					dx = (river.endx - river.strx)
						/ (numRiverNodes - 1);
					dy = (river.endy - river.stry)
						/ (numRiverNodes - 1);
				}
				if (Message.isDebugOn) {
					Message.printDebug(1, routine,
						"Resetting dx,dy to " + dx + "," + dy);
				}
				// Save the reach label...
				storeLabel((String)tokens.elementAt(1), x, y,
					(x + river_dx),(y + river_dy));
				// Plot the line for the river reach in the old plot
				// file.  The nodes will plot on top and clear the line
				// under the node...
				
				// Because we are on a new reach, we want our first
				// node to be off the previous stem...
			}
			// Add { here to help editor with matching on the next line...
			else if (token0.equalsIgnoreCase("}")) {
				// Make sure that we are not closing off more reaches
				// than we have started...
				++closeCount;
				++__closeCount;
				if (closeCount > openCount) {
					message = "Line " + __line + ":  unmatched }(" 
						+ openCount + " {, " + closeCount 
						+ " })";
					Message.printWarning(1, routine, message);
					printCheck(routine, 'W', message);
					return null;
				}
				// We are done with the recursion on the stream reach
				// but need to return the lowest node in the reach...
				nodePt = getDownstreamNode(node, POSITION_REACH);
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Done with reach(top=\"" 
						+ node.getCommonID() + "\", bottom=\"" 
						+ nodePt.getCommonID() + "\")");
				}
				return nodePt;
			}
			else if (token0.equalsIgnoreCase("STOP")) {
				// We are at the end of the system.  Just return the
				// node (which will be the upstream node).  This should
				// send us back to the main program...
				Message.printStatus(2, routine,
					"Detected STOP in net file.  Stop "
					+ "processing nodes.");
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
				// adding a reach - it is adding a node within a
				// reach)...
				if (token0.length() > 12) {
					Message.printWarning(1, routine,
						"\"" + token0 + "\"(line " + __line
						+ " is > 12 characters.  Truncating "
						+ "to 12.");
					token0 = new String(token0.substring(0, 12));
				}
				if (node == null) {
					// This is the first node in the
					// network...
					node = new HydrologyNode();
					// FIXME SAM 2008-03-15 Remove WIS code
					// node.setInWIS(false);
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,	"Adding first node");
					}
				}
				else {
					// This is a node...

					// First create the upstream node...
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"Adding upstream node \"" 
							+ token0 + "\"(line " + __line 
							+ ")");
					}
				
					tempNode = new HydrologyNode();
					// FIXME 2008-03-15 Remove WIS code
					// tempNode.setInWIS(false);
					node.addUpstreamNode(tempNode);
					// Now it is safe to reset the current node to
					// be the upstream node...
					node = node.getUpstreamNode(
						node.getNumUpstreamNodes() - 1);
					// Set the h_num to be the same as the
					// number of nodes added above the downstream
					// node...
					node.setTributaryNumber(
						(node.getDownstreamNode())
						.getNumUpstreamNodes());
				}
				// Set the node information regardless whether the
				// first node or not.  By here, "node" points to the
				// node that has just been added...
				
				// Set node in reach...
				node.setNodeInReachNumber(nodeInReach);
				
				// Use the saved reach counter so that we
				// do not keep incrementing the reach counter
				// for the same reach...
				node.setReachCounter(reachCounterSave);
				node.setReachLevel(reachLevel);
				node.setSerial(getNodeCount());
				
				if (node.getDownstreamNode() == null) {
					// First node.  Do not print downstream...
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"Added node \"" + token0 
							+ "\" nodeInReach=" + node.getNodeInReachNumber()
							+ " reachnum=" + node.getTributaryNumber()
							+ " reachCounter=" + node.getReachCounter() 
							+ " num=" + node.getSerial()
							+ " #up=" + node.getNumUpstreamNodes());
					}
				}
				else {	
					if (Message.isDebugOn) {
						Message.printDebug(1, routine,
							"Added node \"" + (String)tokens.elementAt(0)
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
					// End of system.  This node has no
					// downstream node...
					node.setType(HydrologyNode.NODE_TYPE_END);
					node.setNetID(token0);
					Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1) + ": END");
				}
				else {	
					// A valid node ID...
					node.setNetID(token0);
					Message.printStatus(2, routine,	"Processing node " + (getNodeCount() - 1) + ": " + token0);
					// Get the node type. 
					String token1 = (String)tokens.elementAt(1);
					if (token1.equalsIgnoreCase("BLANK")) {
						node.setType(HydrologyNode.NODE_TYPE_BLANK);
					}
					else if (token1.equalsIgnoreCase("DIV")) {
						node.setType(HydrologyNode.NODE_TYPE_DIV);
						if (Message.isDebugOn) {
							Message.printDebug(dl, routine,
								"Node \"" 
								+ node.getNetID() 
								+ "\" type is DIV "
								+ "from \"DIV\"");
						}
					}
					else if (token1.equalsIgnoreCase("D&W")
						|| token1.equalsIgnoreCase("DW")) {
						node.setType( HydrologyNode.NODE_TYPE_DIV_AND_WELL);
						if (Message.isDebugOn) {
							Message.printDebug(dl, routine,
								"Node \"" 
								+ node.getNetID() 
								+ "\" type is D&W "
								+ "from \"D&W\"");
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
							message =
								"\"" + node.getNetID() 
								+ "\" Node type \"" 
								+ token1 
								+ "\":  Node type is "
								+ "not recognized.";
							Message.printWarning(1,	routine, message);
							printCheck(routine, 'W', message);
						}
						return null;
					}
					// Now process the area*precip information...
					if (!node.parseAreaPrecip(
	  				    (String)tokens.elementAt(2))) {
						message = "Error processing area*"
							+ "precip info for \""
							+ node.getNetID() + "\"";
						Message.printWarning(1, routine,
							message);
						printCheck(routine, 'W', message);
					}
					if (numTokens >= 4) {
						dir = StringUtil.atoi(
							(String)tokens.elementAt(3));
					}
					else {	
						Message.printWarning(1, routine,
							"No direction for \"" + token0 
							+ "\" symbol.  Assuming 1.");
						dir = 1;
					}
					node.setLabelDirection(dir);
					// Now process the description if specified
					// (it is optional)...
					if (numTokens >= 5) {
						node.setDescription(
							(String)tokens.elementAt(4));
						node.setUserDescription(
							(String)tokens.elementAt(4));
					}
					// Else defaults are empty descriptions.
				}
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Set \"" + node.getNetID() 
						+ "\" node type to " 
						+ node.getType() + " area=" 
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
					// Minimum streamflows.  Get the description
					// from the water rights...  For these
					// we are allowed to abbreviate the
					// identifier on the network.  For the
					// common ID we need to prepend the
					// water district...

					// To support old-style ISF identifiers with
					// periods, we need to have the following...
					list =	StringUtil.breakStringList(
						node.getNetID(), ".", 0);
					nlist = list.size();
					if (nlist >= 1) {
						nodeID = (String)list.elementAt(0);
					}
					else {	
						nodeID = new String(node.getNetID());
					}
					String wdid = formatWDID(wd, nodeID, 
						node.getType());
					node.setCommonID(wdid); 
					node.setCommonID(node.getNetID());
				}
				else if (node.getType() == HydrologyNode.NODE_TYPE_WELL) {
					// Ground water well only.  Don't allow the
					// abbreviation...
					node.setCommonID(node.getNetID());
				}
				else {	
					// Diversions, imports, and D&W.  For
					// these we are allowed to abbreviate the
					// identifier on the network.  For the
					// common ID we need to prepend the
					// water district...
					String wdid = formatWDID(wd, node.getNetID(),
						node.getType());
					node.setCommonID(wdid);

				}
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Set common ID for \"" + node.getNetID()
						+ "\" to \"" + node.getCommonID() 
						+ "\"");
				}

				// Set the river node ID...
				// The new way is to just use the common ID.  The
				// old way is to put an extension on USGS IDs (the old
				// code has been deleted)...

				// The new way...
				node.setRiverNodeID(node.getCommonID());
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Set \"" + node.getNetID() 
						+ "\" river node to \"" 
						+ node.getRiverNodeID() + "\"");
				}
				// Now plot the node...
				node.setX(x);
				node.setY(y);
				if ((node.getType() != HydrologyNode.NODE_TYPE_CONFLUENCE) 
				    && (node.getType() != HydrologyNode.NODE_TYPE_XCONFLUENCE)) {
					// If a confluence we want the line to come
					// in at the same point...
					x += dx;
					y += dy;
				}
			}
		}
		return node;
	}

	/**
	Process the data attributes of a HydroBase_Node in the XML file.
	@param node the XML document node being processed.
	@throws Exception if an error occurs.
	*/
	private static void processNode(Node node) 
	throws Exception {
		NamedNodeMap attributes = node.getAttributes();;
		Node attributeNode;
		String area = null;
		String precip = null;
		String name = null;
		String value = null;	
		int nattributes = attributes.getLength();

		HydrologyNode hnode = new HydrologyNode();
		// FIXME 2008-03-15 Remove WIS code
		//hnode.setInWIS(__setInWIS);
		for (int i = 0; i < nattributes; i++) {
			attributeNode = attributes.item(i);
			name = attributeNode.getNodeName();
			value = attributeNode.getNodeValue();
			if (name.equalsIgnoreCase("AlternateX")) {
				hnode.setDBX(new Double(value).doubleValue());
			}
			else if (name.equalsIgnoreCase("AlternateY")) {
				hnode.setDBY(new Double(value).doubleValue());
			}
			else if (name.equalsIgnoreCase("Area")) {
				area = value;
				hnode.setArea(new Double(value).doubleValue());
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
			else if (name.equalsIgnoreCase("IsBaseflow")) {
				if (value.equalsIgnoreCase("true")) {
					hnode.setIsBaseflow(true);
				}
				else {
					hnode.setIsBaseflow(false);
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
				hnode.setLabelAngle(new Double(value).doubleValue());
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
				hnode.setPrecip(new Double(value).doubleValue());
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
				hnode.setX(new Double(value).doubleValue());
			}
			else if (name.equalsIgnoreCase("Y")) {
				hnode.setY(new Double(value).doubleValue());
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
					processDownstreamNode(hnode, node);
				}
				else if (elementName.equalsIgnoreCase("UpstreamNode")) {
					processUpstreamNodes(hnode, node);
				}
				else {}
			}
		}

		__aggregateNodes.add(hnode);
	}

	/**
	Processes an "Upstream" node containing the IDs of the upstream nodes from
	the Network node.
	@param hnode the HydroBase_Node being built.
	@param node the XML node read from the file.
	@throws Exception if an error occurs.
	*/
	public static void processUpstreamNodes(HydrologyNode hnode, Node node) 
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
	Called by the readXML code when processing a StateMod_Network node.
	@param node the XML node being read.
	*/
	private static void processStateMod_NetworkNode(Node node) 
	throws Exception {
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
				__staticLX = new Double(value).doubleValue();
				__lxSet = true;
			}
			if (name.equalsIgnoreCase("YMin")) {
				__staticBY = new Double(value).doubleValue();
				__bySet = true;
			}
			if (name.equalsIgnoreCase("XMax")) {
				__staticRX = new Double(value).doubleValue();
				__rxSet = true;
			}
			if (name.equalsIgnoreCase("YMax")) {
				__staticTY = new Double(value).doubleValue();
				__tySet = true;
			}
			if (name.equalsIgnoreCase("LegendX")) {
				__staticLegendX = new Double(value).doubleValue();
				__legendXSet = true;
			}
			if (name.equalsIgnoreCase("LegendY")) {
				__staticLegendY = new Double(value).doubleValue();
				__legendYSet = true;
			}
		}
	}
	
	/**
	Read a line from the makenet network file and split into tokens.
	It is assumed that the line number is initialized to zero in the main program.
	Blank lines are ignored.  Comments are parsed and returned (can be ignored
	in calling code).
	@param netfp the reader reading the makenet net file.
	@return the tokens from the line
	@throws IOException if there is an error reading the line from the file.
	*/
	public Vector readMakenetLineTokens(BufferedReader netfp) throws IOException {
		String routine = "HydroBase_NodeNetwork.readMakenetLineTokens";
		int	commentIndex = 0,
			dl = 50, 
			numTokens;
		String lineString;
		Vector tokens;

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
				Message.printDebug(dl, routine,
					"Line " + __line + ":  \"" + lineString + "\"");
			}

			// Trim comment from end (if included)...
			commentIndex = lineString.indexOf('#');
			if (commentIndex >= 0) {
				// Special check to allow _# because it is currently
				// used in some identifiers...
				if ((commentIndex > 0) &&
					lineString.charAt(commentIndex - 1) == '_') {
					Message.printWarning(1, routine,
						"Need to remove # from ID on line " + __line + ": " + lineString);
				}
				else {	
					// OK to reset the line to the the beginning of
					// the line...
					lineString = lineString.substring(0,
						commentIndex);
				}
			}

			// Now break into tokens...

			numTokens = 0;
			tokens = StringUtil.breakStringList(lineString, "\t ",
				StringUtil.DELIM_SKIP_BLANKS 
				| StringUtil.DELIM_ALLOW_STRINGS);
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
	@param dmi HydroBase DMI.
	@param filename Makenet .net file to read and process.
	@return true if the network was read successfully, false if not.
	*/
	// FIXME SAM 2008-03-15 Need to move to StateDMI
	public boolean readMakenetNetworkFile(
			//HydroBaseDMI dmi,
			String filename) {
		return readMakenetNetworkFile(
				//dmi,
				filename, false);
	}

	/**
	Read an entire Makenet network file and save in memory.
	@param dmi HydroBase DMI.
	@param filename Makenet .net file to read and process.
	@param skipBlankNodes whether to skip blank nodes when reading nodes in.
	@return true if the network was read successfully, false if not.
	*/
	public boolean readMakenetNetworkFile(
			//HydroBaseDMI dmi,
			String filename,
	boolean skipBlankNodes) {
		String routine = "HydroBase_NodeNetwork.readMakenetNetworkFile";
		BufferedReader in;
		try {	
			in = new BufferedReader(new FileReader(filename));
		}
		catch (IOException e) {
			String message = "Error opening net file \"" + filename + "\"";
			Message.printWarning(1, routine, message);
			printCheck(routine, 'W', message);
			return false;
		}
		return readMakenetNetworkFile(
				//dmi,
				in, filename, skipBlankNodes);
	}

	/**
	Read an entire Makenet network file and save in memory.
	@param dmi HydroBase DMI.
	@param in the BufferedReader to use for reading from the file.
	@param filename Makenet .net file to read and process.
	@return true if the network was read successfully, false if not.
	*/
	public boolean readMakenetNetworkFile(
			//HydroBaseDMI dmi,
			BufferedReader in, String filename) {
		return readMakenetNetworkFile(
				//dmi,
				in, filename, false);
	}

	/**
	Read an entire makenet network file and save in memory.
	@param dmi HydroBaseDMI.
	@param in the BufferedReader opened on the file to use for reading it.
	@param filename the name of the file to be read.
	@param skipBlankNodes whether to skip blank nodes when reading in from a file.
	@return true if the network was read successfully, false if not.
	*/
	public boolean readMakenetNetworkFile(
			//HydroBaseDMI dmi,
			BufferedReader in, String filename, boolean skipBlankNodes) {
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
		Vector tokens;

		// Set the database information...
		/* FIXME SAM 2008-03-16 Need to handle HydroBase in StateDMI
		if (dmi != null && dmi.isOpen()) {
			__dmi = dmi;
			__isDatabaseUp = true;
		}
		*/

		// Create a blank node used for disappearing streams.  The identifiers
		// will be empty strings...

		HydrologyNode blankNode = new HydrologyNode();
		// FIXME SAM 2008-03-15 Need to remove WIS code
		//__blankNode.setInWIS(__inWIS);
		blankNode.setDescription("SURFACE WATER LOSS");
		blankNode.setUserDescription("SURFACE WATER LOSS");
		blankNode.setType(HydrologyNode.NODE_TYPE_UNKNOWN);

		// Start at the top of the file and read until we get to the STOP
		// command or the end of the file...

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
			token0 = (String)tokens.elementAt(0);
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
					Message.printDebug(dl, routine,
						"Found SCALE command");
				}
				String message = "SCALE is obsolete!";
				Message.printWarning(1, routine, message);
				printCheck(routine, 'W', message);
				//sx = atof(tokens[1]);
				//sy = atof(tokens[2]);
				continue;
			}
			else if (token0.equalsIgnoreCase("RIVER")) {
				// Line information for the river reach...
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
					"Found RIVER command");
				}
				numnodes = StringUtil.atoi((String)tokens.elementAt(2));
				x0 = StringUtil.atod((String)tokens.elementAt(3));
				y0 = StringUtil.atod((String)tokens.elementAt(4));
				x1 = StringUtil.atod((String)tokens.elementAt(5));
				y1 = StringUtil.atod((String)tokens.elementAt(6));

				// Calculate the spacing of nodes on the main stem...
				dx = (x1 - x0)/(double)(numnodes - 1);
				dy = (y1 - y0)/(double)(numnodes - 1);

				// Break out of this loop and go to the next level (do
				// not use a continue...
			}
			else if (token0.equalsIgnoreCase("TITLE")) {
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,	"Found TITLE command");
				}
				double fontSize = StringUtil.atod((String)tokens.elementAt(1));
				double titleX = StringUtil.atod((String)tokens.elementAt(2));
				double titleY = StringUtil.atod((String)tokens.elementAt(3));
				String title = (String)tokens.elementAt(4);
				for (int i = 5; i < numTokens; i++) {
					title = title + " ";
					title = title + (String)tokens.elementAt(i);
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
				setNodeDescriptions();
			}
			catch (Exception e) {
				Message.printWarning(2, routine, e);
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
	Reads a StateMod network file in either Makenet or XML format and returns
	the network that was generated.
	@param filename the name of the file from which to read.
	@param dmi an open and connected dmi object.  Can be null if reading from an
	XML file.
	@param skipBlankNodes whether blank nodes should be read from the Makenet file
	or not.  Does not matter if reading from an XML file.
	@return the network read from the file.
	@throws Exception if an error occurs.
	*/
	// FIXME SAM 2008-03-15 Need to enable reading old makenet file
	public static StateMod_NodeNetwork readStateModNetworkFile(String filename, 
	//HydroBaseDMI dmi,
	boolean skipBlankNodes ) 
	throws Exception {
		StateMod_NodeNetwork network = null;
		if (isXML(filename)) {
			network = readXMLNetworkFile(filename);
		}
		else {
			network = new StateMod_NodeNetwork();
			// FIXME SAM 3008-03-15 Need to enable reading old makenet network
			//network.readMakenetNetworkFile(
					//dmi, filename, skipBlankNodes);
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
		String routine = "StateCU_DataSet.readXMLFile";
		// FIXME SAM 2008-03-15 Need to remove WIS code
		//__setInWIS = false;
		__aggregateNodes = new Vector();

		DOMParser parser = null;
		try {	
			parser = new DOMParser();
			parser.parse(filename);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Error reading XML Network file \"" + filename + "\"");
			Message.printWarning(2, routine, e);
			throw new Exception("Error reading XML Network file \"" + filename + "\"");
		}

		// Now get information from the document.  For now don't hold the
		// document as a data member...
		Document doc = parser.getDocument();

		// Loop through and process the document nodes, starting with the root node...
		__aggregateLayouts = new Vector();

		__lxSet = false;
		__rxSet = false;
		__tySet = false;
		__bySet = false;
		
		processDocumentNodeForRead(doc);

		String unset = "";
		if (!__lxSet) {
			unset += "XMin\n";
		}
		if (!__bySet) {
			unset += "YMin\n";
		}
		if (!__rxSet) {
			unset += "XMax\n";
		}
		if (!__tySet) {
			unset += "YMax\n";
		}
		if (!unset.equals("")) {
			throw new Exception("Not all data points were set for the "
				+ "network.  The following must be defined: " 
				+ unset);
		}

		HydrologyNode node = (HydrologyNode)__aggregateNodes.elementAt(0);
		StateMod_NodeNetwork network = null;
		if (node.getComputationalOrder() == -1) {
			network = new StateMod_NodeNetwork();
			network.calculateNetworkNodeData(__aggregateNodes, false);
		}
		else {
			network = buildNetworkFromXMLNodes();
		}
		network.setAnnotations(__aggregateAnnotations);
		__aggregateAnnotations = null;
		network.setLayouts(__aggregateLayouts);
		__aggregateLayouts = null;
		network.setLinks(__aggregateLinks);
		__aggregateLinks = null;

		network.setBounds(__staticLX, __staticBY, __staticRX, __staticTY);
		if (__legendXSet && __legendYSet) {
			network.setLegendPosition(__staticLegendX, __staticLegendY);
		}

		if (network != null) {
			network.convertNodeTypes();
			network.finalCheck(__staticLX, __staticBY, __staticRX, 
				__staticTY, false);
		}	

		return network;
	}
	
	/**
	Set whether fancy descriptions should be generated (true) or not (false).
	@param fancydesc true to turn on fancy descriptions.
	*/
	public void setFancyDesc(boolean fancydesc) {
		__createFancyDescription = fancydesc;
	}
	
	// FIXME SAM 2008-03-15 Move to StateDMI if still needed
	// FIXME SAM 2004-10-11 This code is probably unneeded because StateDMI fills
	// from HydroBase in a specific step, not as the old .net file is read.
	// However, it probably is needed to facilitate the conversion of old .net files
	// to XML .net (otherwise many nodes might not have names).
	/**
	Sets the node descriptions (names) of all nodes in the network, when reading a
	makenet network file.  Station names are read from HydroBase.
	If __createFancyDescription is true, then fancy descriptions will be set.  
	If the user description has been set, it will be used as is.
	@throws Exception if an error occurs.
	*/
	public void setNodeDescriptions()
	throws Exception
	{	String	routine = "HydroBase_NodeNetwork.setNodeDescriptions";
	/*
		double[] coords = null;
		HydroBase_Station station;
		HydroBase_StationView view;
		HydroBase_Structure structure;
		HydroBase_StructureWDWater wdwater;
		HydroBase_WellApplicationView well_applicationView;		
		int	dl = 15, 
			geoloc_num = 0,
			i = 0, 
			id = 0, 
			type = 0, 
			wd = 0;	
		String 	message,
			nodeType,
			stationName,
			streamName,
			userDesc,
			wdid;
		Vector	idList = null,
			permitList = new Vector(),
			statList = null,
			structList = null,
			waterList = null,
			wellApplications = null;

		Message.printStatus(2, routine, "Setting node names from HydroBase...");

		// Get the list of HydroBase_Stations and HydroBase_Structures 
		// present in the Network as obtained from the database.  First 
		// get stations (do this regardless of whether fancy descriptions 
		// are used since the query is generally fast and output can be 
		// easily formatted)...

		int [] nodeTypes = null;
		try {	
			// Get list of stations as strings...
			nodeTypes = new int[1];
			nodeTypes[0] = Node.NODE_TYPE_FLOW;
			idList = getNodeIdentifiersByType(nodeTypes);
			// Now query to get the HydroBase_Station list...
			Message.printStatus(2, routine,
				"Getting station information from the database...");
			StopWatch timer = new StopWatch();
			timer.start();
			if (__isDatabaseUp) {
				statList = 
					__dmi.readStationListForStation_idList(idList);
			}
			if (statList != null) {
				Message.printStatus(2, routine,
					"Query for " + statList.size()
					+ " stations took " + (int)timer.getSeconds() 
					+ " seconds.");
			}
			else {
				statList = new Vector();
			}
		}
		catch (Exception e) {
			message = "Errors finding stations in node network.  " 
				+ "Can't set station descriptions.";
			Message.printWarning(2, routine, message);
			Message.printWarning(2, routine, e);
			throw new Exception(message);
		}

		// Now get structures so we can fill in descriptions (do this regardless
		// of whether fancy descriptions are used).  Structure types ...

		try {	
			// Get list of structures as strings...
			nodeTypes = new int[5];
			nodeTypes[0] = Node.NODE_TYPE_DIV;
			nodeTypes[1] = Node.NODE_TYPE_RES;
			nodeTypes[2] = Node.NODE_TYPE_ISF;
			nodeTypes[3] = Node.NODE_TYPE_IMPORT;
			nodeTypes[4] = Node.NODE_TYPE_DIV_AND_WELL;
			// Ground water wells only (NODE_TYPE_WELL) are treated
			// separately below...
			idList = getNodeIdentifiersByType(nodeTypes);
			// Now query to get the HydroBase_Structure list...
			Message.printStatus(2, routine,
				"Getting structure information from the database...");
			StopWatch timer = new StopWatch();
			timer.start();
			if (__isDatabaseUp) {
				structList = __dmi.readStructureListForWDIDs(idList);
			}
			timer.stop();
			if (structList != null) {
				Message.printStatus(2, routine,
					"Query for " + structList.size() 
					+ " structures took " + (int)timer.getSeconds() 
					+ " seconds.");
			}
			else {
				structList = new Vector();
			}
		}
		catch (Exception e) {
			message = "Errors finding structures in node network.  " 
				+ "Can't set structure descriptions.";
			Message.printWarning(2, routine, message);
			Message.printWarning(2, routine, e);
			throw new Exception(message);
		}


		// Now get stream information from the database for fancy descriptions
		// so that they can be used for ditches and other structures...
		if (__createFancyDescription) {
			Message.printStatus(2, routine,
				"Getting stream information from the database...");
			// Need to get streams...
			try {	
				StopWatch timer = new StopWatch();
				timer.start();
				if (__isDatabaseUp) {
					waterList = 
						__dmi
						.readStructureWDWaterListForStructureIDs
						(idList);
				}
				timer.stop();
				if (waterList != null) {
					Message.printStatus(2, routine,
						"Query for " + waterList.size() 
						+ " wdwaters took " 
						+ (int)timer.getSeconds() 
						+ " seconds.");
				}
				else {
					waterList = new Vector();
				}
			}
			catch (Exception e) {
				message = "Errors finding HydroBase_WDWater objects "
					+ "for node network.";
				Message.printWarning(2, routine, message);
				Message.printWarning(2, routine, e);
				throw new Exception(message);
			}
		
			if (waterList == null) {
				message = "Could not find HydroBase_WDWater objects "
					+ "for node network.";
				Message.printWarning(2, routine, message);
				throw new Exception(message);
			}
			if (waterList.size() == 0) {
				message = "Could not find HydroBase_WDWater objects "
					+ "in node network.";
				Message.printWarning(2, routine, message);
				throw new Exception(message);
			}
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,
					"Setting fancy descriptions.");
			}
		}

		// Now get the ground water well only...
		/ *
		REVISIT (JTS - 2004-03-15)
		wellList is not used by the rest of the method, so this was 
		commented out
		try {	// Get list of stations as strings...
			nodeTypes = new int[1];
			nodeTypes[0] = HydroBase_Node.NODE_TYPE_WELL;
			idList = getNodeIdentifiersByType(nodeTypes);
			// Now query to get the HydroBase_Wells list...
			Message.printStatus(2, routine,
			"Getting well information from the database...");
			StopWatch timer = new StopWatch();
			timer.start();
			wellList = HydroBaseDMIUtil.getStructureToWellFromIDs(
				__dmi, idList, HydroBaseDMIUtil.STRUCT_TO_WELL_WELL,
				true);
			if (wellList != null) {
				Message.printStatus(2, routine,
					"Query for " + wellList.size()
					+ " wells took " + (int)timer.getSeconds() 
					+ " seconds.");
			}
		}
		catch (Exception e) {
			message = "Errors finding wells in node network.  " +
			"Can't set well descriptions.";
		}
		* /

		// Process all the nodes in the network, setting the descriptions as
		// appropriate...

		Message.printStatus(2, routine, "Setting node descriptions...");

		boolean wdid_structure;
		Node nodePt = null;
		int[]	wdidArray;
		int 	idot = 0,
			i_structList = -1,
			i_waterList = -1,
			i_stationList = -1,
			nstationList = 0,
			nstructList = 0,
			nwaterList = 0;
		String desc = "";

		nodePt = getMostUpstreamNode();
		boolean done = false;

		// REVISIT - reworked from a for loop while doing some debugging
		// should probably set it back as this might be confusing.
		nodePt = getMostUpstreamNode();
		boolean cont = false;
		while (!done) {
			if (cont) {
				cont = false;
				nodePt = getDownstreamNode(nodePt, 
					POSITION_COMPUTATIONAL);
			}

			if (nodePt == null) {
				done = true;
				type = -1;
				userDesc = "";
				nodeType = "";
				streamName = "";
			}
			else {
				type = nodePt.getType();	
				userDesc = nodePt.getUserDescription();
				nodeType = Node.getTypeString(type, 1); 
				streamName = "";
			}

			if (nodePt == null) {}
			else if (type == Node.NODE_TYPE_BLANK) {
				nodePt.setDescription("BLANK NODE - PLOT ONLY");
			}
			else if (type == Node.NODE_TYPE_CONFLUENCE) {
				nodePt.setDescription("CONFLUENCE - PLOT ONLY");
			}
			else if (type == Node.NODE_TYPE_END) {
				if (userDesc.length() > 0) {
					// User-defined...
					nodePt.setDescription(userDesc);
				}
				else {	// Default...
					nodePt.setDescription("END");
				}
				// The end of the network, so done...
				done = true;
			}
			else if ((type == Node.NODE_TYPE_RES) 
			    || (type == Node.NODE_TYPE_DIV) 
			    || (type == Node.NODE_TYPE_DIV_AND_WELL) 
			    || ((type == Node.NODE_TYPE_WELL) 
			    && nodePt.getCommonID().charAt(0) != 'P' 
			    && StringUtil.isInteger(nodePt.getCommonID())) 
			    || (type == Node.NODE_TYPE_ISF) 
			    || (type == Node.NODE_TYPE_IMPORT)) {
				// First see if the id can be parsed out.  If not a
				// true structure, then just leave the description as
				// is(assume it was set by the user).  WELL nodes are
				// included if the ID is not a permit but is a number
				// only (in which case it is assumed to NOT be an
				// aggregation, etc.)...
				wdid_structure = true;
				idot = nodePt.getCommonID().indexOf('.');
				if (idot >= 0) {
					// ID has a period (like old-style ISF)
					wdidArray = HydroBase_WaterDistrict.parseWDID(
						nodePt.getCommonID().substring(0,idot));
				}
				else {	
					try {
						wdidArray = HydroBase_WaterDistrict
							.parseWDID(
							nodePt.getCommonID());
					}
					catch (Exception e) {
						// REVISIT (JTS - 2004-03-24)
						// this is to handle aggregate 
						// diversion nodes (43_ADW3030, etc)
						// that break the routine
						Message.printStatus (2, routine,
						"Node ID \"" + nodePt.getCommonID() +
						"\" is not a WDID.  Skipping " +
						"HydroBase query." );
						//Message.printWarning(2, routine, e);
						wdidArray = null;
					}
						
				}
				
				if (wdidArray == null) {
					wdid_structure = false;
				}
				
				if (wdid_structure) {
					wd = wdidArray[0];
					id = wdidArray[1];
				}
				else {	
					wd = 0;
					id = 0;
				}
				
				// Use the description information from the database...
				if (__createFancyDescription) {
					// Get the stream associated with the
					// structure.  The structure and wdwater list
					// should be in the same order.  As an item
					// is found, remove from the lists so that
					// subsequent searches are faster...
					// First find the structure...
					nstructList = structList.size();
					// Initialize to the current description (which
					// will be the user description if specified)...
					desc = nodePt.getDescription();
					i_structList = -1;
					geoloc_num = -1;
					for (i = 0; i < nstructList; i++) {
						structure = (HydroBase_StructureView)
						structList.elementAt(i);
						if ((structure.getWD() == wd) 
						    && (structure.getID() == id)) {
							// Found the structure...
							i_structList = i;
							desc = structure.getStr_name();
							geoloc_num = structure
								.getGeoloc_num();
							// No need to keep searching...
							break;
						}
					}

					// Now find the HydroBase_StructureWDWater...
					streamName = "";
					i_waterList = -1;
					nwaterList = waterList.size();
					for (i = 0; i < nwaterList; i++) {
						wdwater = (HydroBase_StructureWDWater)
						waterList.elementAt(i);
						if ((wdwater.getWD() == wd) 
						    && (wdwater.getID() == id)) {
							// Found the stream...
							i_waterList = i;
							streamName =
								wdwater.getStr_name();
							if (Message.isDebugOn) {
								Message.printDebug(1,
									routine,
									"wdwater for " 
									+ nodePt
									.getCommonID() 
									+ " is " 
									+ streamName);
							}
							break;
						}
					}
					if (i_waterList < 0) {
						if (Message.isDebugOn) {
							Message.printDebug(1,
								routine,
								"Did not find wdwater "
								+ "for " 
								+ nodePt.getCommonID());
						}
					}
					// Regardless of what we found, format the
					// output to be "fancy"...
					// Structures need to be identified in
					// terms of their "WDID"
					wdid = formatWDID(wd, id, type);
					if (Message.isDebugOn) {
						Message.printDebug(dl, 
							routine, "Structure WDID: " 
							+ StringUtil.atoi(wdid) 
							+ "  Node ID: " 
							+ nodePt.getCommonID());
					}
					// Set the node description using either the
					// existing user description or a stream name
					// from the db...
					if (userDesc.length() != 0) {
						// Use the user's description,
						// not that from the database and
						// ignore the stream...
						nodePt.setDescription(
							StringUtil.formatString(
							userDesc, "%-20.20s") + "_" 
							+ StringUtil.formatString(
							nodeType, "%-3.3s"));
					}
					else if (type == Node.NODE_TYPE_ISF) {
						nodePt.setDescription(
							StringUtil.formatString(
							desc, "%-20.20s") 
							+ "_" + StringUtil.formatString(
							nodeType, "%-3.3s"));
					}
					else {	
						// Use the stream from the database as
						// the first 4 characters...
						nodePt.setDescription(
							StringUtil.formatString(
							streamName, "%-4.4s") 
							+ "_" +	StringUtil.formatString(
							desc, "%-15.15s") 
							+ "_" +	StringUtil.formatString(
							nodeType, "%-3.3s"));
					}
					coords = findGeolocCoordinates(geoloc_num);
					nodePt.setDBX(coords[0]);
					nodePt.setDBY(coords[1]);
					// Can now remove from the list
					// to speed searches for later structures but
					// only do so if not an ISF (because these are
					// reused when old-style WDID.xx notation is
					// used)...
					if ((i_structList >= 0) 
					    && (type != Node.NODE_TYPE_ISF)) {
						structList.removeElementAt(
							i_structList);
					}
					if ((i_waterList >= 0) 
					    && (type != Node.NODE_TYPE_ISF)) {
						waterList.removeElementAt(
							i_waterList);
					}
				}
				else if (__isDatabaseUp && __createOutputFiles) {
					// No fancy description.  Just use the
					// structure name for the description...
					// First find the structure...
					nstructList = structList.size();
					// Initialize to the current description (which
					// will be the user description if specified)...
					desc = nodePt.getDescription();
					i_structList = -1;
					geoloc_num = -1;
					for (i = 0; i < nstructList; i++) {
						structure = (HydroBase_StructureView)
						structList.elementAt(i);
						if ((structure.getWD() == wd) 
						    && (structure.getID() == id)) {
							// Found the structure...
							i_structList = i;
							desc = structure.getStr_name();
							geoloc_num = structure
								.getGeoloc_num();
							break;
						}
					}

					// Regardless of what we found, format the
					// output...
					wdid = formatWDID(wd, id, type);

					coords = findGeolocCoordinates(geoloc_num);
					nodePt.setDBX(coords[0]);
					nodePt.setDBY(coords[1]);

					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"StructureNetID: " 
							+ StringUtil.atoi(wdid));
					}

					if (userDesc.length() > 0) {
						// User-defined...
						nodePt.setDescription(userDesc);
						cont = true;
						continue;
					}
					else {	
						// Use the description from above...
						nodePt.setDescription(desc);
					}
					// Can now remove from the list
					// to speed searches for later structures but
					// only do so if not an ISF (because these are
					// reused when old-style WDID.xx notation is
					// used)...
					if ((i_structList >= 0) 
					    && (type != Node.NODE_TYPE_ISF)) {
						structList.removeElementAt(
							i_structList);
					}
				}
			}
			else if (type == Node.NODE_TYPE_WELL) {
				// Already checked for a possible well as a structure
				// with a WDID above so this is either a well permit
				// with information in struct_to_well, in which case
				// the description is taken from the first matching
				// entry, or an aggregation, in which the description
				// will not be found.
				// First see if the id can be parsed out.  Identifiers
				// that start with P are assumed to be well permits.
				// Otherwise, then just leave the description as is
				// (assume it was set by the user)...
				// Use the description information from the database...

				// Get the description from the well_application
				// table (or other?).  Since there will not
				// typically be many WEL nodes, this should not
				// be that much of a hit...
				try {
				permitList.removeAllElements();
				permitList.add(nodePt.getCommonID());
				if (__isDatabaseUp) {
					wellApplications = 
						__dmi
						.readWellApplicationListForPermitData(
						permitList);
				}
				if ((wellApplications == null) 
				    || (wellApplications.size() == 0)) {
					Message.printStatus(2, routine,
						"No well data for " 
						+ nodePt.getCommonID());
					desc = "";
					geoloc_num = -1;				
				}
				else {	
					// What came back has to be the permit...
					well_applicationView 
						= (HydroBase_WellApplicationView)
						wellApplications.elementAt(0);
					desc = well_applicationView.getWell_name();
					geoloc_num 
						= well_applicationView.getGeoloc_num();
				}
				}
				catch (Exception e) {
					desc = "";
					geoloc_num = -1;
				}

				coords = findGeolocCoordinates(geoloc_num);
				nodePt.setDBX(coords[0]);
				nodePt.setDBY(coords[1]);
					
				if (__createFancyDescription 
				    || (__isDatabaseUp && __createOutputFiles)) {
					if (userDesc.length() != 0) {
						// Use the user's description,
						// not that from the database and
						// ignore the stream...
						nodePt.setDescription(
							StringUtil.formatString(
							userDesc, "%-20.20s") + "_" 
							+ StringUtil.formatString(
							nodeType, "%-3.3s"));
					}
					else {	
						// Use the name from the database as
						// the first 4 characters...
						nodePt.setDescription(
							StringUtil.formatString(
							desc, "%-20.20s") 
							+ "_" +	StringUtil.formatString(
							nodeType, "%-3.3s"));
					}
				}
			}
			else if ((type == Node.NODE_TYPE_FLOW) 
			    || (type == Node.NODE_TYPE_BASEFLOW) 
			    || (type == Node.NODE_TYPE_OTHER)) {
				stationName = "";
				if (userDesc.length() > 0) {
					// User-defined...
					nodePt.setDescription(userDesc);
					stationName = userDesc;
				}
				else {	
					// Default...
					if (type == Node.NODE_TYPE_BASEFLOW) {
						nodePt.setDescription("Baseflow Node");
					}
				}
				if (__createFancyDescription) {
					// Set the new description using the gage 
					// description and the node type...
					// FLOW From user code...
					// Find the Station...
					i_stationList = -1;
					nstationList = statList.size();
					// Search even if a user description has been
					// supplied to clean up list for other
					// searches...
					geoloc_num = -1;
					for (i = 0; i < nstationList; i++) {
						view = (HydroBase_StationView)
						statList.elementAt(i);
						if (view.getStation_id()
						    .equalsIgnoreCase(nodePt
						    .getCommonID())) {
							// Found the view...
							i_stationList = i;
							if (userDesc.length() <= 0) {
								// No user
								// description...
								stationName =
								    view
								    .getStation_name();
								geoloc_num = 
								    view
								    .getGeoloc_num();
							}
							if (Message.isDebugOn) {
								Message.printDebug(1,
									routine,
									"station for " 
									+ nodePt
									.getCommonID() 
									+ " is " 
									+ stationName);
							}
							// No need to search more...
							break;
						}
					}

					if (i_stationList < 0) {
						stationName = nodePt.getDescription();
					}

					coords = findGeolocCoordinates(geoloc_num);
					nodePt.setDBX(coords[0]);
					nodePt.setDBY(coords[1]);

					nodePt.setDescription(StringUtil.formatString(
						stationName, "%-20.20s") + "_" 
						+ StringUtil.formatString(
						nodeType, "%-3.3s"));
					// Now remove from the station list so searches
					// are faster...
					if (i_stationList >= 0) {
						statList.removeElementAt(i_stationList);
					}
				}
				else if (__isDatabaseUp && __createOutputFiles) {
					// Just use the station name...
					if (userDesc.length() > 0) {
						// User-defined...
						nodePt.setDescription(userDesc);
						cont = true;
						continue;
					}
					i_stationList = -1;
					nstationList = statList.size();
					if (__dmi.useStoredProcedures()) {
					for (i = 0; i < nstationList; i++) {
						view = (HydroBase_StationView)
						statList.elementAt(i);
						if (view.getStation_id()
						    .equalsIgnoreCase(
						    nodePt.getCommonID())) {
							// Found the view...
							i_stationList = i;
							if (userDesc.length() <= 0) {
								// No user
								// description...
								nodePt.setDescription(
								    view
								    .getStation_name());
								geoloc_num = 
								    view
								    .getGeoloc_num();
							}
							if (Message.isDebugOn) {
								Message.printDebug(1,
									routine,
									"station for " 
									+ nodePt
									.getCommonID() 
									+ " is " 
									+ stationName);
							}
							// No need to search more...
							break;
						}
					}
					}
					else {
					for (i = 0; i < nstationList; i++) {
						station = (HydroBase_Station)
						statList.elementAt(i);
						if (station.getStation_id()
						    .equalsIgnoreCase(
						    nodePt.getCommonID())) {
							// Found the station...
							i_stationList = i;
							if (userDesc.length() <= 0) {
								// No user
								// description...
								nodePt.setDescription(
								    station
								    .getStation_name());
								geoloc_num = 
								    station
								    .getGeoloc_num();
							}
							if (Message.isDebugOn) {
								Message.printDebug(1,
									routine,
									"station for " 
									+ nodePt
									.getCommonID() 
									+ " is " 
									+ stationName);
							}
							// No need to search more...
							break;
						}
					}
					}
					coords = findGeolocCoordinates(geoloc_num);
					nodePt.setDBX(coords[0]);
					nodePt.setDBY(coords[1]);
					// Can now remove from the list...
					if (i_stationList >= 0) {
						statList.removeElementAt(
							i_stationList);
					}
				}
			}
			else if (type == Node.NODE_TYPE_XCONFLUENCE) {
				if (userDesc.length() > 0) {
					// User-defined...
					nodePt.setDescription(userDesc);
				}
				else {	// Default...
					nodePt.setDescription(
						"XCONFLUENCE - PLOT ONLY");
				}
			}
			nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL);
		}
		Message.printStatus(2, routine,
			"...done setting node names from HydroBase...");
			*/
	}
	
}
