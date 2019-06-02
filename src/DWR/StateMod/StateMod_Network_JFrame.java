// StateMod_Network_JFrame - main JFrame for viewing a network.

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

// ----------------------------------------------------------------------------
// StateMod_Network_JFrame - main JFrame for viewing a network.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-03-16	J. Thomas Sapienza, RTi	Initial version.  
// 2004-07-08	JTS, RTi		Added support for multiple page layouts.
// 2004-08-17	JTS, RTi		Corrected bug where data limits for 
//					the reference window were not being
//					set properly when a network was read
//					in from an XML file.
// 2004-08-25	JTS, RTi		* Removed deprecated constructors.
// 					* The constructor that takes a pre-built
//					  network can check that network for 
//					  layouts and use them now.
// 2004-10-20	JTS, RTi		* Reference window was not drawing
//					  properly, so the way its bounds are
//					  set when an XML file is read were 
//					  corrected.
//					* Renamed some variables to represent
//					  the fact that the XML file now stores
//					  the corner points, instead of the
//					  lower-left point and the network 
//					  width and height.
// 2004-11-15	JTS, RTi		Changed the tooltip text for the 1:1
//					button.
// 2005-04-08	JTS, RTi		* Added a constructor that allows for
//					  a new network to be built.
//					* JFrame now keeps track of the network
//					  file that it opened and read a network
//					  from, for saving purposes.
// 2005-04-11	JTS, RTi		Added 'saveOnExit' flag so that the
//					network will prompt for it to be
//					saved when the window is closed.
// 2005-11-02	JTS, RTi		Changes with how icons are handled:
//					* IOUtil.release() is used to help test 
//					  that local-drive icons are never 
//					  loaded for released apps.
//					* Debug messages explain where the 
//					  icons are loaded from.
// 2006-03-07	JTS, RTi		* Added setInStateModGUI() and
//					  inStateModGUI().
//					* Added finalize().
// 2006-05-01	JTS, RTi		* Corrected bug where the layout combo
//					  box was not selecting an initial 
//					  value.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

// Before 2017-07-01 Xerces was used - as of this date rely on built-in XML support in Java
//import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import cdss.domain.hydrology.network.HydrologyNode;

import RTi.GR.GRAspect;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRUnits;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.GUI.SimpleJToggleButton;
import RTi.Util.GUI.TextResponseJDialog;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is a class for displaying the network editor.
*/
@SuppressWarnings("serial")
public class StateMod_Network_JFrame extends JFrame
implements ActionListener, ItemListener, WindowListener {

/**
The name of the class.
*/
public final static String CLASS = "StateMod_Network_JFrame";

/**
The path to icon files, etc. used in this package.
*/
private final String __RESOURCE_PATH = "/DWR/StateMod";

/**
Strings for buttons.
*/
private final String __BUTTON_PrintEntireNetwork = "Print Entire Network";
private final String __BUTTON_PrintScreen = "Print Screen";
private final String __BUTTON_SaveEntireNetworkAsImage = "Save Entire Network as Image";
private final String __BUTTON_SaveScreenAsImage = "Save Screen as Image";

/**
Strings defining the modes the GUI can be put into.
*/
public final String 
	MODE_INFO = "Info",
	MODE_PAN = "Pan",
	MODE_SELECT = "Select";

/**
Default node and font sizes for populating layout information.
*/
private final int 
	__DEFAULT_FONT_SIZE = 10,
	__DEFAULT_NODE_SIZE = 20;

/**
Default paper information for populating layout information.
*/
private final String
	__DEFAULT_PAPER_SIZE = "C",
	__DEFAULT_PAGE_ORIENTATION = "Landscape";

/**
Button labels.
*/
private final String
	__BUTTON_ADD_LAYOUT = "Add Layout",
	__BUTTON_DELETE_LAYOUT = "Delete Layout",
	__BUTTON_SET_LAYOUT_NAME = "Set Name";

/**
Menu labels.
*/
private final String
	__Menu_File_String = "File",
		__Menu_File_Save_Network_String = "Save Network...",
		__Menu_File_Save_VisibleNetworkAsImage_String = "Save Visible Network as Image...",
		__Menu_File_Print_EntireNetwork_String = "Print Entire Network...",
		__Menu_File_Close_String = "Close",
	__Menu_Tools = "Tools",
		__Menu_Tools_SetXToConfluenceX_String = "Set X to Confluence X",
		__Menu_Tools_SetYToConfluenceY_String = "Set Y to Confluence Y",
		__Menu_Tools_PositionNodesEvenlyBetweenEndNodes_String = "Position Nodes Evenly Between End Nodes",
		__Menu_Tools_WriteNetworkAsListFiles_String = "Write Network as List Files";

/**
Whether the network is running in StateModGUI or not.  If true, then the network cannot be edited
directly (it must be edited through the Edit > Add/Delete menus.
*/
private boolean __inStateModGUI = false;

/**
Whether the network that is read is in an XML file or not.
*/
private boolean __isXML = false;

/**
Checks used when reading in an XML file that make sure that everything is set properly.
*/
private boolean 
	__lxSet = false, 
	__bySet = false, 
	__wSet = false, 
	__hSet = false;

private boolean __ignoreEvents = false;

/**
Whether a check should occur when saving.
TODO SAM 2011-07-07 Can this be removed since the close() method has a flag?  Normally
the check should be done unless an application like the StateMod GUI controls saving files.
*/
private boolean __saveOnExit = false;

/**
Used when reading in an XML file to get the bounds of the network.
*/
private double 
	__lx = 0, 
	__by = 0, 
	__rx = 0, 
	__ty = 0;

/**
Data provider to fill the nodes with.
*/
//private StateMod_NodeDataProvider __nodeDataProvider = null;

/**
The font size stored in an XML file.
*/
private int __fontSize = -1;

/**
The index in the layout Vector of the currently-selected layout.
*/
private int __layoutIndex = 0;

/**
The node size stored in an XML file.
*/
private int __nodeSize = -1;

/**
Button for deleting a page layout.
*/
private JButton __deleteButton;

/**
Checkbox for selecting the default page layout.
*/
private JCheckBox __defaultLayoutCheckBox;

/**
Text fields for network information (column 2).
*/
private JTextField
	__xminJTextField,
	__yminJTextField,
	__xmaxJTextField,
	__ymaxJTextField,
	__edgeBufferLeftJTextField,
	__edgeBufferRightJTextField,
	__edgeBufferTopJTextField,
	__edgeBufferBottomJTextField;

/**
Textfields for displaying node information.
*/
private JTextField 
	__nodeDescriptionTextField,
	__nodeTypeTextField,
	__nodeXYTextField,
	__nodeDBXYTextField,
	__nodeCommonIDTextField;

/**
Status bars.
*/
private JTextField 
	__locationJTextField,
	__statusJTextField;

/**
GUI toggle buttons.
*/
private JToggleButton 
	__infoJButton = null,
	__panJButton = null,
	__selectJButton = null;

/**
The tool bar across the top of the screen.
*/
private JToolBar __toolBar;

/**
Toolbar buttons.
*/
private SimpleJButton 
	__printEntireNetworkJButton,
	__printScreenJButton,
	__refreshJButton,
	__saveEntireNetworkAsImageJButton,
	__saveScreenAsImageJButton,
	__saveXMLJButton,
	__undoJButton,
	__redoJButton,
	__zoomOutJButton,
	__zoomInJButton,
	__zoom1JButton;

/**
Combo boxes for selecting the paper size, node size, and font size.
*/
private SimpleJComboBox 
	__layoutComboBox,
	__nodeSizeComboBox,
	__orientationComboBox,
	__paperSizeComboBox,
	__printedFontSizeComboBox;

/**
The device that draws the network.
*/
private StateMod_Network_JComponent __device;

/**
The reference window.
*/
private StateMod_NetworkReference_JComponent __reference;

/**
The name of the network file that was opened.  If null, the JFrame was opened
with a pre-existing network.
*/
private String __filename = null;

/**
Information about the current layout.
*/
private String 
	__orient = null,
	__paperSize = null;

/**
The String ID of the current layout.
*/
private String __id = "";

/**
List to manage all the different pre-defined layouts for the network.
*/
private List<PropList> __layouts = null;

/**
The panel that includes the list of StateMod_Network_AnnotationData.
*/
private StateMod_Network_AnnotationDataListJPanel __annotationListJPanel = null;

/**
Constructor.  
@param network the network to display.
*/
public StateMod_Network_JFrame(StateMod_NodeNetwork network) 
throws Exception {
	super();
	double scale = .5;
	__device = new StateMod_Network_JComponent(this, scale);
	__device.setNetwork(network, false, false);
	addKeyListener(__device);

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	String app = JGUIUtil.getAppNameForWindows();
	if (app == null || app.trim().equals("")) {
		setTitle("StateMod Network");	
	}
	else {
		setTitle(app + " - StateMod Network");
	}
	setupGUI();
	__device.setNetwork(network, false, true);

	__layouts = network.getLayoutList();
	if (__layouts == null || __layouts.size() == 0) {
		createFirstLayout();
		__layoutComboBox.add(__id);
	}	

	setupPaper();

	setVisible(true);

	setBoundsFromNetwork(network);
}

/**
Constructor.
@param nodeDataProvider the data provider to use for setting up the network.
@param filename the file from which to read the network.
*/
public StateMod_Network_JFrame( StateMod_NodeDataProvider nodeDataProvider, String filename )
throws Exception {
	super();
	initializeExistingNetwork ( nodeDataProvider, filename );
}

/**
Constructor.
@param nodeDataProvider the data provider to use for setting up the network.
@param filename the file from which to read the network.
*/
public StateMod_Network_JFrame( StateMod_NodeDataProvider nodeDataProvider,	String filename, boolean newNetwork)
throws Exception {
	super();
	if (newNetwork) {
		initializeNewNetwork ( nodeDataProvider, filename );
	}
	else {
		initializeExistingNetwork ( nodeDataProvider, filename );
	}
}

/**
Responds to button presses.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event)
{
	String routine = "StateMod_Network.actionPerformed";
	if (__ignoreEvents) {
		return;
	}

	String command = event.getActionCommand();

	// Properties area buttons...
	if (command.equals(__BUTTON_ADD_LAYOUT)) {
		PropList p = new PropList("Layout");
		String id = "Page Layout #" + (__layouts.size() + 1);
		p.set("ID=\"" + id + "\"");
		p.set("PaperSize=\"" + __DEFAULT_PAPER_SIZE + "\"");
		p.set("PageOrientation=\"" + __DEFAULT_PAGE_ORIENTATION + "\"");
		p.set("NodeLabelFontSize=\"" + __DEFAULT_FONT_SIZE + "\"");
		p.set("NodeSize=\"" + __DEFAULT_NODE_SIZE + "\"");
		p.set("IsDefault=\"false\"");
		__layouts.add(p);
		__ignoreEvents = true;
		__layoutComboBox.add(id);
		__ignoreEvents = false;
		__layoutComboBox.select(id);
		__deleteButton.setEnabled(true);
		__defaultLayoutCheckBox.setSelected(false);
	}
	else if (command.equals(__BUTTON_DELETE_LAYOUT)) {
		int index = __layoutComboBox.getSelectedIndex();
		int count = __layoutComboBox.getItemCount();
		__layouts.remove(index);
		__ignoreEvents = true;
		__layoutComboBox.removeAt(index);
		__layoutComboBox.select(null);
		__ignoreEvents = false;
		if (index == (count - 1)) {
			index--;
		}
		__layoutComboBox.select(index);
		if (count == 2) {
			__deleteButton.setEnabled(false);
		}
	}
	else if (command.equals(__BUTTON_SET_LAYOUT_NAME)) {
		String name = new TextResponseJDialog(this, "Enter Page Layout Name", 
			"Enter the name for the current page layout:",
			ResponseJDialog.OK | ResponseJDialog.CANCEL).response();
		if (name == null) {
			return;
		}
		name = name.trim();
		if (name.equals("")) {
			return;
		}

		int index = __layoutComboBox.getSelectedIndex();
		PropList p = __layouts.get(index);		
		p.set("ID=\"" + name + "\"");
		__ignoreEvents = true;
		__layoutComboBox.removeAt(index);
		__layoutComboBox.insertItemAt(name, index);
		__layoutComboBox.select(null);
		__layoutComboBox.select(index);
		__ignoreEvents = false;
	}
	// Menus...
	else if (command.equals(__Menu_File_Close_String)) {
		// This will prompt the user if the network is dirty and needs to be saved.
		close(true);
	}
	else if (command.equals(__Menu_Tools_SetXToConfluenceX_String)) {
		// Set the X coordinates for the selected nodes to the X coordinate of the confluence
		StateMod_Network_EditorToolkit tk = new StateMod_Network_EditorToolkit ( this, this.__device, getNetwork() );
		if ( tk.setNodeXToConfluenceX ( this.__device.getSelectedNodes() ) > 0 ) {
			// Redraw the network
			this.__device.forceRepaint();
		}
	}
	else if (command.equals(__Menu_Tools_SetYToConfluenceY_String)) {
		// Set the Y coordinates for the selected nodes to the Y coordinate of the confluence
		StateMod_Network_EditorToolkit tk = new StateMod_Network_EditorToolkit ( this, this.__device, getNetwork() );
		if ( tk.setNodeYToConfluenceY ( this.__device.getSelectedNodes() ) > 0 ) {
			// Redraw the network
			this.__device.forceRepaint();
		}
	}
	else if (command.equals(__Menu_Tools_PositionNodesEvenlyBetweenEndNodes_String)) {
		// Space the nodes evenly between the selected end nodes
		StateMod_Network_EditorToolkit tk = new StateMod_Network_EditorToolkit ( this, this.__device, getNetwork() );
		if ( tk.positionNodesEvenlyBetweenEndNodes ( this.__device.getSelectedNodes() ) > 0 ) {
			// Redraw the network
			this.__device.forceRepaint();
		}
	}
	else if (command.equals(__Menu_Tools_WriteNetworkAsListFiles_String)) {
		// Space the nodes evenly between the selected end nodes
		StateMod_Network_EditorToolkit tk = new StateMod_Network_EditorToolkit ( this, this.__device, getNetwork() );
		tk.writeListFiles();
	}
	// Tool bar buttons...
	else if (command.equals("Zoom Out")) {
		__device.zoomOut();
	}
	else if (command.equals("Zoom In")) {
		__device.zoomIn();
	}	
	else if (command.equals("1:1")) {
		__device.zoomOneToOne();
	}
	else if (command.equals("Save XML") || command.equals(__Menu_File_Save_Network_String)) {
		__device.saveXML(getFilename());
		__device.setDirty(false);
	}
	else if (command.equals(__BUTTON_SaveEntireNetworkAsImage) ) {
		__device.saveNetworkAsImage();
	}
	else if (command.equals(__BUTTON_SaveScreenAsImage) ||
		command.equals(__Menu_File_Save_VisibleNetworkAsImage_String)) {
		try {
			__device.saveScreenAsImage();
		}
		catch ( Exception e ) {
			Message.printWarning(1,routine,"Error saving image file (" + e + ")." );
		}
	}
	else if (command.equals(__BUTTON_PrintEntireNetwork) ||
		command.equals(__Menu_File_Print_EntireNetwork_String)) {
		Message.printStatus(2, routine, "Printing entire network.");
		__device.printNetwork();
	}
	else if (command.equals(__BUTTON_PrintScreen)) {
		Message.printStatus(2, routine, "Printing screen.");
		__device.printScreen();
	}
	else if (command.equals("Refresh")) {
		__device.forceRepaint();
	}
	else if (command.equals("Undo")) {
		__device.undo();
	}
	else if (command.equals("Redo")) {
		__device.redo();
	}
	else if (command.equals(MODE_INFO)) {

	}
	else if (command.equals(MODE_PAN)) {
		__selectJButton.setSelected(false);
		__infoJButton.setEnabled(false);
		__infoJButton.setSelected(false);
		__device.setMode(StateMod_Network_JComponent.MODE_PAN);
	}
	else if (command.equals(MODE_SELECT)) {
		__infoJButton.setEnabled(false);
		__infoJButton.setSelected(false);
		__panJButton.setSelected(false);
		__device.setMode(StateMod_Network_JComponent.MODE_SELECT);
	}

}

/**
Add an annotation renderer.  Just chain to the network component.
@param renderer the renderer that will be called when it is time to draw the object
@param objectToRender the object to render (will be passed back to the renderer)
@param label label for the object, to list in the GeoViewJPanel
@param scrollToAnnotation if true, scroll to the annotation (without changing scale)
*/
public void addAnnotationRenderer ( StateMod_Network_AnnotationRenderer renderer, Object objectToRender,
	String label, GRLimits limits, boolean scrollToAnnotation )
{	// Add the annotation to the list, which will trigger a redraw, during which the limits of the
	// rendered data will be set.  Once the limits are set, the zoomToAnnotations() call below will
	// properly center on the annotations.
	StateMod_Network_AnnotationData annotationData = __device.addAnnotationRenderer (
		renderer, objectToRender, label, limits );
	// Also add to the annotation list for managing the list from the UI
	if ( annotationData != null ) {
		__annotationListJPanel.addAnnotation ( annotationData );
	}
	// TODO SAM 2010-12-28 Need to enable
	if ( scrollToAnnotation ) {
		// Scroll and zoom so the object is visible (do this even if no new data were added because
		// the user may have asked to reposition the display to see the annotation)...
		// Make the buffer relatively large due to wide text labels.
		zoomToAnnotations ( .2, .1 );
	}
}

/**
Adds a node to the network.
@param name the name of the node to add.
@param type the type of node to add to the network.
@param upID the ID of the upstream node from the node to be added.
@param downID the ID of the node downstream from the node to be added.
@param isNaturalFlow whether the node to be added is a natural flow node.
*/
public void addNode(String name, int type, String upID, String downID, 
boolean isNaturalFlow, boolean isImport) {
	__device.addNode(name, type, upID, downID, isNaturalFlow, isImport);
}

/**
Builds the menu bar along the top of the network window.
*/
private void buildMenuBar ()
{
	JMenuBar menuBar = new JMenuBar();
	// File...
	JMenu file_JMenu = new JMenu( __Menu_File_String, true );
	menuBar.add(file_JMenu);
	file_JMenu.add( new SimpleJMenuItem( __Menu_File_Save_Network_String, this ) );
	file_JMenu.add( new SimpleJMenuItem( __Menu_File_Save_VisibleNetworkAsImage_String, this ) );
	file_JMenu.addSeparator();
	file_JMenu.add( new SimpleJMenuItem( __Menu_File_Print_EntireNetwork_String, this ) );
	file_JMenu.addSeparator();
	file_JMenu.add( new SimpleJMenuItem( __Menu_File_Close_String, this ) );
	// Tools...
	JMenu tools_JMenu = new JMenu( __Menu_Tools, true );
	menuBar.add(tools_JMenu);
	tools_JMenu.add( new SimpleJMenuItem( __Menu_Tools_SetYToConfluenceY_String, this ) );
	tools_JMenu.add( new SimpleJMenuItem( __Menu_Tools_SetXToConfluenceX_String, this ) );
	tools_JMenu.add( new SimpleJMenuItem( __Menu_Tools_PositionNodesEvenlyBetweenEndNodes_String, this ) );
	tools_JMenu.addSeparator();
	tools_JMenu.add( new SimpleJMenuItem( __Menu_Tools_WriteNetworkAsListFiles_String, this ) );
	// TODO SAM 2011-07-07 Determine whether to enable/disable menus based on UI state, add menu tooltips
	setJMenuBar ( menuBar );
}

/**
Builds the toolbar along the top of the network window.
*/
private void buildToolBar() {
	String routine = "buildToolBar";
	__toolBar = new JToolBar("Network Control Buttons");
	Insets none = new Insets(0, 0, 0, 0);
	URL url = this.getClass().getResource( __RESOURCE_PATH + "/icon_print.gif" );
	String buttonLabel = __BUTTON_PrintEntireNetwork;
	if (url != null) {
		__printEntireNetworkJButton = new SimpleJButton(
			new ImageIcon(url), buttonLabel, buttonLabel, none, false, this);
	}
	else {
		__printEntireNetworkJButton = new SimpleJButton("Print", buttonLabel, buttonLabel, none, false, this);		
	}
	__printEntireNetworkJButton.setToolTipText (
		"Print the entire network, full-scale (select a page size that matches the network page layout)" );
	__toolBar.add(__printEntireNetworkJButton);

	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_printScreen.gif");
	buttonLabel = __BUTTON_PrintScreen;
	if (url != null) {
		__printScreenJButton = new SimpleJButton(
			new ImageIcon(url), buttonLabel, buttonLabel, none, false, this);
	}
	else {
		__printScreenJButton = new SimpleJButton( "Print", buttonLabel, buttonLabel, none, false, this);
	}	
	// TODO SAM 2011-07-07 Re-enable
	__printScreenJButton.setEnabled(false);
	__printScreenJButton.setToolTipText("Print the network shown in the editor - CURRENTLY DISABLED.");
	__toolBar.add(__printScreenJButton);

	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_saveAsImage.gif");
	buttonLabel = __BUTTON_SaveEntireNetworkAsImage;
	if (url != null) {
		__saveEntireNetworkAsImageJButton = new SimpleJButton(new ImageIcon(url),
			buttonLabel, buttonLabel, none, false, this);	
	}
	else {
		__saveEntireNetworkAsImageJButton = new SimpleJButton("Save", buttonLabel, buttonLabel, none, false, this);	
	}
	__saveEntireNetworkAsImageJButton.setEnabled ( false );
	__saveEntireNetworkAsImageJButton.setToolTipText (
		"Save entire network as image file, full-scale - CURRENTLY DISABLED." );
	__toolBar.add(__saveEntireNetworkAsImageJButton);
	
	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_saveScreenAsImage.gif");
	buttonLabel = __BUTTON_SaveScreenAsImage;
	if (url != null) {
		__saveScreenAsImageJButton = new SimpleJButton(
			new ImageIcon(url), buttonLabel, buttonLabel, none, false, this);	
	}
	else {
		__saveScreenAsImageJButton = new SimpleJButton("Save",buttonLabel, buttonLabel, none, false, this);
	}
	__saveScreenAsImageJButton.setToolTipText("Save the network shown in the editor to an image file." );
	__toolBar.add(__saveScreenAsImageJButton);	

	__toolBar.addSeparator();

	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_saveXML.gif");
	if (url != null) {
		__saveXMLJButton = new SimpleJButton(new ImageIcon(url),
			"Save XML", "Save XML Network File", none, false, this);
	}
	else {
		__saveXMLJButton = new SimpleJButton("Save", "Save XML", 
			"Save XML Network File", none, false, this);
	}
	__saveXMLJButton.setToolTipText("Save the network to an XML file." );
	__toolBar.add(__saveXMLJButton);	

	__toolBar.addSeparator();

	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_refresh.gif");
	if (url != null) {
		__refreshJButton = new SimpleJButton(new ImageIcon(url),"Refresh", "Refresh", none, false, this);
	}
	else {
		__refreshJButton = new SimpleJButton("Refresh", "Refresh", "Refresh", none, false, this);
	}	
	__refreshJButton.setActionCommand("Refresh");
	__refreshJButton.setToolTipText("Refresh (redraw) the network." );
	__toolBar.add(__refreshJButton);
	
	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_zoomOut.gif");
	if (url != null) {
		__zoomOutJButton = new SimpleJButton(new ImageIcon(url),"Zoom Out", "Zoom Out", none, false, this);
	}
	else {
		__zoomOutJButton = new SimpleJButton("Zoom Out", "Zoom Out", "Zoom Out", none, false, this);
	}
	__zoomOutJButton.setToolTipText ( "Zoom out to twice the area.");
	__zoomOutJButton.setActionCommand("Zoom Out");	

	__zoom1JButton = new SimpleJButton( "1:1", "1:1", "Draw at 1:1 scale for page layout", 
		none, false, this);
	__zoom1JButton.setActionCommand("1:1");

	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_zoomMode.gif");
	if (url != null) {
		__zoomInJButton = new SimpleJButton(new ImageIcon(url), "Zoom In", "Zoom In", none, false, this);
	}
	else {
		__zoomInJButton = new SimpleJButton("Zoom In", "Zoom In", "Zoom In", none, false, this);
	}	
	__zoomInJButton.setActionCommand("Zoom In");
	__zoomInJButton.setToolTipText ( "Zoom in to half the area.");
	__toolBar.add(__zoomOutJButton);	
	__toolBar.add(__zoom1JButton);
	__toolBar.add(__zoomInJButton);	
	__toolBar.addSeparator();

	__undoJButton = new SimpleJButton( "Undo", "Undo", "Undo", none, false, this);
	__undoJButton.setActionCommand("Undo");
	__undoJButton.setEnabled(false);
	__undoJButton.setToolTipText ( "Undo the previous move action(s)." );
	__toolBar.add(__undoJButton);

	__redoJButton = new SimpleJButton( "Redo", "Redo", "Redo", none, false, this);
	__redoJButton.setActionCommand("Redo");
	__redoJButton.setEnabled(false);
	__redoJButton.setToolTipText ( "Redo the previous move action(s)." );
	__toolBar.addSeparator();
	__toolBar.add(__redoJButton);
	
	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_hand.gif");
	if (url != null) {
		__panJButton = new SimpleJToggleButton(new ImageIcon(url),
			MODE_PAN, "Enter Pan Mode", none, false, this, true);
		Message.printDebug(10, routine, "Enter Pan Mode icon loaded from Jar file.");
	}
	else {
		__panJButton = new SimpleJToggleButton("Pan Mode",
			MODE_PAN, "Enter Pan Mode", none, false, this, true);
	}
	__toolBar.addSeparator();
	__panJButton.setActionCommand(MODE_PAN);
	__panJButton.setToolTipText ( "Enter pan mode - network can be scrolled by dragging the mouse." );
	__toolBar.add(__panJButton);	
	
	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_infoMode.gif");
	if (url != null) {
		__infoJButton = new SimpleJToggleButton(new ImageIcon(url),
			MODE_INFO, "Enter Info Mode", none, false, this, false);
	}
	else {
		__infoJButton = new SimpleJToggleButton("Info Mode",
			MODE_INFO, "Enter Info Mode", none, false, this, false);
	}	
	__infoJButton.setActionCommand(MODE_INFO);
	__infoJButton.setToolTipText ( "Enter information mode - CURRENTLY DISABLED." );
	__infoJButton.setEnabled(false);
	__toolBar.add(__infoJButton);	
	
	url = this.getClass().getResource( __RESOURCE_PATH + "/icon_selectMode.gif");
	if (url != null) {
		__selectJButton = new SimpleJToggleButton(new ImageIcon(url),
			MODE_SELECT, "Enter Select Mode", none, false, this, false);
	}
	else {
		__selectJButton = new SimpleJToggleButton("Select Mode",
			MODE_SELECT, "Enter Select Mode", none, false, this, false);
	}	
	__selectJButton.setActionCommand(MODE_SELECT);
	__selectJButton.setToolTipText ( "Enter select mode - select node and right-click for node properties, " +
		"or drag node to reposition." );
	__toolBar.add(__selectJButton);
	
	__printEntireNetworkJButton.addKeyListener(__device);
	__printScreenJButton.addKeyListener(__device);
	__refreshJButton.addKeyListener(__device);
	__saveEntireNetworkAsImageJButton.addKeyListener(__device);
	__saveScreenAsImageJButton.addKeyListener(__device);
	__saveXMLJButton.addKeyListener(__device);
	__undoJButton.addKeyListener(__device);
	__redoJButton.addKeyListener(__device);
	__zoomOutJButton.addKeyListener(__device);
	__zoomInJButton.addKeyListener(__device);
	__zoom1JButton.addKeyListener(__device);
	__selectJButton.addKeyListener(__device);
	__panJButton.addKeyListener(__device);
	__infoJButton.addKeyListener(__device);
}

// TODO SAM 2011-07-07 Need to combine logic of various close methods - not sure if this work is done.
// Calling with false seems to be what is expected by the StateMod GUI
/**
Closes the GUI and frees resources.  This method is called, for example, from the StateMod GUI when
a new data set is opened and the old network editor needs to be closed down.
@param checkForChanges if true, then the user is allowed to save if changes have occurred;
if false, then the network editor is closed regardless of whether changes have occurred
*/
public void close ( boolean checkForChanges )
{
	if ( checkForChanges ) {
		if (__saveOnExit) {
			if (__device.isDirty()) {
				int x = (new ResponseJDialog(this, "Save network?",
					"The network has not been saved.  Save?",
					ResponseJDialog.YES | ResponseJDialog.NO | ResponseJDialog.CANCEL)).response();
				if (x == ResponseJDialog.YES) {
					__device.saveXML(getFilename());
					__device.setDirty(false);
				}
				else if (x == ResponseJDialog.CANCEL) {
					return;
				}
			}
		}
	}
	closeClicked(true);
}

/**
Closes the GUI and optionally frees resources.
@param hardClose if true, the window is disposed; if false, the window is set not visible
*/
private void closeClicked( boolean hardClose) {
	setVisible(false);
	if ( hardClose ) {
		dispose();
	}
}

/**
Creates the default layout that will be used if a network is read with 
no layouts defined in it.
*/
private void createFirstLayout() {
	if (__layouts == null) {
		__layouts = new Vector<PropList>();
	}
	PropList main = new PropList("Layout");
	__id = "Page Layout #" + (__layouts.size() + 1);
	main.set("ID=\"" + __id + "\"");
	main.set("PaperSize=\"" + __DEFAULT_PAPER_SIZE + "\"");
	main.set("PageOrientation=\"" + __DEFAULT_PAGE_ORIENTATION + "\"");
	main.set("NodeLabelFontSize=\"" + __DEFAULT_FONT_SIZE + "\"");
	main.set("NodeSize=\"" + __DEFAULT_NODE_SIZE + "\"");
	main.set("IsDefault=\"true\"");			
	__layouts.add(main);
	__layoutIndex = 0;
}

/**
Deletes the node with the specified ID from the network.
@param id the id of the node to be deleted.
*/
public void deleteNode(String id) {
	__device.deleteNode(id);
}

/**
@deprecated -- use the other one
*/
public void displayNode(HydrologyNode node, int nodeNum) {
	displayNode(node);
}

/**
Displays the information about the node in the textfields on screen.
@param node the node to display.
*/
public void displayNode(HydrologyNode node) {
	__nodeDescriptionTextField.setText(node.getDescription());
	__nodeTypeTextField.setText(HydrologyNode.getVerboseType(node.getType()));
	displayNodeXY(node.getX(), node.getY());
	displayNodeDBXY(node.getDBX(), node.getDBY());
	__nodeCommonIDTextField.setText(node.getCommonID());
}	

/**
Displays the node's x and y values on the screen.
@param x the x value to display.
@param y the y value to display.
*/
public void displayNodeXY(double x, double y) {
	String xs = StringUtil.formatString(x, "%13.6f");
	String ys = StringUtil.formatString(y, "%13.6f");
	__nodeXYTextField.setText("" + xs.trim() + ", " + ys.trim());
}	

/**
Displays the node's alternate x and y values on the screen.
@param x the x value to display.
@param y the y value to display.
*/
public void displayNodeDBXY(double x, double y) {
	String xs = StringUtil.formatString(x, "%13.6f");
	String ys = StringUtil.formatString(y, "%13.6f");
	__nodeDBXYTextField.setText("" + xs.trim() + ", " + ys.trim());
}	

/**
Called when OK is pressed in the add node dialog.
*/
protected void endAddNode() {
	__device.endAddNode();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__deleteButton = null;
	__defaultLayoutCheckBox = null;
	__nodeDescriptionTextField = null;
	__nodeTypeTextField = null;
	__nodeXYTextField = null;
	__nodeDBXYTextField = null;
	__nodeCommonIDTextField = null;
	__locationJTextField = null;
	__statusJTextField = null;
	__infoJButton = null;
	__panJButton = null;
	__selectJButton = null;
	__toolBar = null;
	__printEntireNetworkJButton = null;
	__printScreenJButton = null;
	__refreshJButton = null;
	__saveEntireNetworkAsImageJButton = null;
	__saveScreenAsImageJButton = null;
	__saveXMLJButton = null;
	__undoJButton = null;
	__redoJButton = null;
	__zoomOutJButton = null;
	__zoomInJButton = null;
	__zoom1JButton = null;
	__layoutComboBox = null;
	__nodeSizeComboBox = null;
	__orientationComboBox = null;
	__paperSizeComboBox = null;
	__printedFontSizeComboBox = null;
	__device = null;
	__reference = null;
	__filename = null;
	__orient = null;
	__paperSize = null;
	__id = null;
	__layouts = null;
	super.finalize();
}

/**
Forces the network and the reference window to repaint.
*/
protected void forceRepaint() {
	__device.forceRepaint();
	__reference.forceRepaint();
}

/**
Returns the name of the file passed into the constructor and from which the
network was read.  If the class was instantiated with a pre-existing network, this will return null.
@return the name of the file from which the network was read.
*/
public String getFilename() {
	return __filename;
}

/**
Indicate whether the network has unsaved changes.
*/
public boolean getIsDirty()
{	// Loop through the network nodes.  If any of them are dirty then the network is dirty.
	
	return false;
}

/**
Returns a list of all the layouts used in the current network.
@return a list of all the layouts used in the current network.
*/
public List<PropList> getLayouts() {
	return __layouts;
}

/**
Returns the network being drawn.
@return the network being drawn.
*/
public StateMod_NodeNetwork getNetwork() {
	return __device.getNetwork();
}

/**
Returns the network editor JComponent that displays the network.
*/
public StateMod_Network_JComponent getNetworkJComponent ()
{
	return __device;
}

/**
Return the selected orientation.
*/
public String getSelectedOrientation ()
{
	return __orientationComboBox.getSelected();
}

/**
Return the selected page layout.
*/
public String getSelectedPageLayout ()
{
	return __layoutComboBox.getSelected();
}

/**
Return the selected paper size.
*/
public String getSelectedPaperSize ()
{
	return __paperSizeComboBox.getSelected();
}

/**
Initializes class settings for a network in a net file.  
@param nodeDataProvider the data provider to use for communicating with the database.
@param filename the file from which the network will be read.
@throws Exception if an error occurs when initializing.
*/
private void initializeExistingNetwork( StateMod_NodeDataProvider nodeDataProvider, String filename )
throws Exception {
	//__nodeDataProvider = nodeDataProvider;
	__filename = filename;
	__device = new StateMod_Network_JComponent(this, .5);
	addKeyListener(__device);

	boolean isXML = StateMod_NodeNetwork.isXML(filename);

	if (!isXML) {
		__device.readMakenetFile ( nodeDataProvider, filename );
	}
	else {
		__isXML = true;
	}	

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	String app = JGUIUtil.getAppNameForWindows();
	if (app == null || app.trim().equals("")) {
		setTitle("StateMod Network");	
	}
	else {
		setTitle(app + " - StateMod Network");
	}

	setupGUI();

	if (__isXML) {
		readXML(filename);
	}
	// Display the coordinate limits
	StateMod_NodeNetwork network = getNetwork();
	updateNetworkLayoutExtents(network.getLX(), network.getBY(), network.getRX(), network.getTY(), 
		network.getEdgeBuffer());

	setVisible(true);

	if (__isXML) {
		if (__layouts == null) {
			createFirstLayout();
			__layoutComboBox.add(__id);
			__layoutIndex = 0;
		}

		__device.forceRepaint();
		__device.setPaperSize(__paperSize);
		__device.setOrientation(__orient);	
		__device.setPrintNodeSize(__nodeSize);
		__device.setPrintFontSize(__fontSize);

		__nodeSizeComboBox.select("" + __nodeSize);
		__orientationComboBox.select(__orient);
		__paperSizeComboBox.setSelectedPrefixItem(__paperSize + " -");
		__printedFontSizeComboBox.select("" + __fontSize);
	}	
	else {
		createFirstLayout();
		__layoutComboBox.add(__id);
		__layoutIndex = 0;
	}
	__layoutComboBox.select(__layoutIndex);
}

/**
Initializes class settings for a network to be built by the user.
@param nodeDataProvider the data provider to use for communicating with the database.
@param filename the file to which the network will be saved when the user saves.
@throws Exception if an error occurs when initializing.
*/
private void initializeNewNetwork ( StateMod_NodeDataProvider nodeDataProvider, String filename )
throws Exception {
	//__nodeDataProvider = nodeDataProvider;
	__isXML = true;
	__filename = filename;
	__device = new StateMod_Network_JComponent(this, .5);
	addKeyListener(__device);

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	String app = JGUIUtil.getAppNameForWindows();
	if (app == null || app.trim().equals("")) {
		setTitle("StateMod Network");	
	}
	else {
		setTitle(app + " - StateMod Network");
	}

	setupGUI();

	__lx = 0;
	__by = 0;
	__rx = 1100;
	__ty = 850;

	StateMod_NodeNetwork network = new StateMod_NodeNetwork(true);
	__device.setNetwork(network, false, true);
	List<HydrologyNode> v = network.getNodesForType(HydrologyNode.NODE_TYPE_END);
	// Change the "5" to something better if a different default paper size than "C" is used.
	v.get(0).setX(__rx / 4.3);
	v.get(0).setY(__ty / 5);

	__device.setXMLDataLimits(__lx, __by, __rx - __lx, __ty - __by);
	__reference.setNewDataLimits(new GRLimits(__lx, __by, __rx, __ty));
	// Display the coordinate limits
	updateNetworkLayoutExtents(network.getLX(), network.getRX(), network.getTY(), network.getBY(),
		network.getEdgeBuffer());
	
	setVisible(true);

	createFirstLayout();
	__layoutComboBox.add(__id);

	__nodeSize = __DEFAULT_NODE_SIZE;
	__fontSize = __DEFAULT_FONT_SIZE;
	__paperSize = __DEFAULT_PAPER_SIZE;
	__orient = __DEFAULT_PAGE_ORIENTATION;

	__device.forceRepaint();
	__device.setPaperSize(__paperSize);
	__device.setOrientation(__orient);
	__device.setPrintNodeSize(__nodeSize);
	__device.setPrintFontSize(__fontSize);

	__nodeSizeComboBox.select("" + __nodeSize);
	__orientationComboBox.select(__orient);
	__paperSizeComboBox.setSelectedPrefixItem(__paperSize + " -");
	__printedFontSizeComboBox.select("" + __fontSize);
	__layoutComboBox.select(0);
	__deleteButton.setEnabled(false);
}

/**
Returns whether the network is running in StateModGUI.
@return true if the network is in StateModGUI.
*/
public boolean inStateModGUI() {
	return __inStateModGUI;
}

/**
Checks whether the network is dirty, meaning that edits have occurred.
@return true if the network is dirty, otherwise false.
*/
public boolean isDirty() {
	return __device.isDirty();
}

/**
Responds to item state change events, such as those that change page layout information.
@param event the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent event)
{	String routine = "StateMod_Network_JFrame.itemStateChanged";
	if (__ignoreEvents) {
		return;
	}
	if (event.getStateChange() != ItemEvent.SELECTED) {
		return;
	}

	if (event.getSource() == __paperSizeComboBox) {
		int index = __layoutComboBox.getSelectedIndex();
		String value = __paperSizeComboBox.getSelected();
		__device.setPaperSize( shorten(__paperSizeComboBox.getSelected()));
		PropList p = __layouts.get(index);
		p.set("PaperSize=\"" + value + "\"");
	}
	else if (event.getSource() == __printedFontSizeComboBox) {	
		int index = __layoutComboBox.getSelectedIndex();
		String value = __printedFontSizeComboBox.getSelected();
		PropList p = __layouts.get(index);
		try {
			int i = Integer.decode(	__printedFontSizeComboBox.getSelected()).intValue();
			__device.setPrintFontSize(i);
			p.set("NodeLabelFontSize=\"" + value + "\"");
		}
		catch (Exception e) {}
	}
	else if (event.getSource() == __nodeSizeComboBox) {
		int index = __layoutComboBox.getSelectedIndex();
		String value = __nodeSizeComboBox.getSelected();
		PropList p = __layouts.get(index);
		try {
			int i = Integer.decode(	__nodeSizeComboBox.getSelected()).intValue();
			__device.setNodeSize((double)i);
			p.set("NodeSize=\"" + value + "\"");
		}
		catch (Exception e) {}
	}
	else if (event.getSource() == __orientationComboBox) {
		orientationComboBoxSelected();
	}
	else if (event.getSource() == __layoutComboBox) {
		int index = __layoutComboBox.getSelectedIndex();	
		PropList p = __layouts.get(index);
		String paperFormat = p.getValue("PaperSize");
		Message.printStatus ( 2, routine, "Selected layout has paper size \"" + paperFormat + "\"" );
		if ( !__paperSizeComboBox.setSelectedPrefixItem(paperFormat + " -") ) {
			__paperSizeComboBox.setSelectedPrefixItem(paperFormat);
		}
		String orient = p.getValue("PageOrientation");
		__orientationComboBox.select(orient);
		String sFontSize = p.getValue("NodeLabelFontSize");
		int fontSize = 10;
		try {
			fontSize = (Integer.decode(sFontSize)).intValue();
			__printedFontSizeComboBox.select("" + fontSize);
		}
		catch (Exception e) {}
		String sNodeSize = p.getValue("NodeSize");
		int nodeSize = 20;
		try {
			nodeSize = (Integer.decode(sNodeSize)).intValue();
			__nodeSizeComboBox.select("" + nodeSize);
		}
		catch (Exception e) {}	
		/*
		Message.printStatus(1, "", ""
			+ "Index: " + index + "\n"
			+ "Format: " + paperFormat + "\n"
			+ "Orient: " + orient + "\n"
			+ "Font: " + sFontSize + "\n"
			+ "Node: " + sNodeSize);
		*/
		String isDefault = p.getValue("IsDefault");
		if (isDefault.equalsIgnoreCase("true")) {
			__defaultLayoutCheckBox.setSelected(true);
		}
		else {
			__defaultLayoutCheckBox.setSelected(false);
		}
	}
	else if (event.getSource() == __defaultLayoutCheckBox) {
		int index = __layoutComboBox.getSelectedIndex();
		boolean set = __defaultLayoutCheckBox.isSelected();
		if (set) {
			for (PropList p : __layouts) {
				p.set("IsDefault=\"False\"");
			}
		}

		PropList p = __layouts.get(index);
		p.set("IsDefault=\"" + set + "\"");
	}
}

/**
Called when properties were changed in the node properties dialog.  Forces the network to repaint.
*/
protected void nodePropertiesChanged() {
	__device.forceRepaint();
}

/**
Called by the readXML code to process a StateMod Network XML file.
@param node the head node of the file.
*/
private void processDocumentNodeForRead(Node node)
throws Exception {
	NodeList children;
	if (node.getNodeType() == Node.DOCUMENT_NODE) {
		// The main data set node.  Get the data set type, etc.
		Node docNode = ((Document)node).getDocumentElement();
		String elementName = docNode.getNodeName();
		if (elementName.equalsIgnoreCase("StateMod_Network")) {
			children = docNode.getChildNodes();
			processStateMod_NetworkNode(docNode);
			__layouts = new Vector<PropList>();
			if (children != null) {
				elementName = null;
				int len = children.getLength();
				for (int i = 0; i < len; i++) {
					node = children.item(i);
					elementName = node.getNodeName();
					// Evaluate the nodes attributes...
					if (elementName.equalsIgnoreCase("PageLayout")) {
						processLayoutNode(node);
					}
				}
			}
		}
	}
}

/**
Called by the readXML code when processing a Layout node.
@param node the node being read.
*/
private void processLayoutNode(Node node) {
	NamedNodeMap attributes;
	Node attributeNode;
	String name = null;
	String value = null;
	
	attributes = node.getAttributes();
	int nattributes = attributes.getLength();

	PropList p = new PropList("Layout");
	p.set("ID=\"Page Layout #" + (__layouts.size() + 1) + "\"");
	p.set("PaperSize=\"" + __DEFAULT_PAPER_SIZE + "\"");
	p.set("PageOrientation=\"" + __DEFAULT_PAGE_ORIENTATION + "\"");
	p.set("NodeLabelFontSize=\"" + __DEFAULT_FONT_SIZE + "\"");
	p.set("NodeSize=\"" + __DEFAULT_NODE_SIZE + "\"");
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
	__layouts.add(p);
}

/**
Called by the readXML code when processing a StateMod_Network node.
@param node the XML node being read.
*/
private void processStateMod_NetworkNode(Node node) 
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
			__lx = new Double(value).doubleValue();
			__lxSet = true;
		}
		if (name.equalsIgnoreCase("YMin")) {
			__by = new Double(value).doubleValue();
			__bySet = true;
		}
		if (name.equalsIgnoreCase("XMax")) {
			__rx = new Double(value).doubleValue();
			__wSet = true;
		}
		if (name.equalsIgnoreCase("YMax")) {
			__ty = new Double(value).doubleValue();
			__hSet = true;
		}
	}


}

// FIXME SAM 2008-12-11 Why is this here - see the StateMod_NodeNetwork read method!
// is it reading only the metadata here?  Need comments.
/**
Reads a network from an XML file.
@param filename the name of the XML file to read.
*/
public void readXML(String filename) 
throws Exception {
	String routine = "StateMod_Network_JFRame.readXML";

	DOMParser parser = null;
	try {	
		parser = new DOMParser();
		parser.parse(filename);
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading StateCU Data set \"" + filename + "\"");
		Message.printWarning(2, routine, e);
		throw new Exception("Error reading StateCU Data set \"" + filename + "\"");
	}

	// Now get information from the document.  For now don't hold the document as a data member...
	Document doc = parser.getDocument();

	// Loop through and process the document nodes, starting with the root node...

	__layouts = new Vector<PropList>();
	processDocumentNodeForRead(doc);

	StateMod_NodeNetwork network = StateMod_NodeNetwork.readXMLNetworkFile(filename);
	__device.setNetwork(network, false, true);

	if (__lxSet && __bySet && __wSet && __hSet) {
		__device.setXMLDataLimits(__lx, __by, __rx - __lx, __ty - __by);
		__reference.setNewDataLimits(new GRLimits(__lx, __by, __rx, __ty));
	}
	else {
		String unset = "";
		if (!__lxSet) {
			unset += "XMin\n";
		}
		if (!__bySet) {
			unset += "YMin\n";
		}
		if (!__wSet) {
			unset += "XMax\n";
		}
		if (!__hSet) {
			unset += "YMax\n";
		}
		throw new Exception("Not all data points were set for the "
			+ "network.  The following must be defined: " + unset);
	}

	setVisible(true);

	int size = __layouts.size();
	PropList main = null;

	int index = 0;
	if (size == 0) {
		Message.printWarning(2, routine, 
			"No layouts were defined in this file.  Page layout "
			+ "values will be set to defaults.");
		createFirstLayout();
		index = 0;
	}
	
	PropList p = null;
	String s = null;
	List<String> ids = new Vector<String>();
	for (int i = 0; i < size; i++) {
		p = __layouts.get(i);
		s = p.getValue("IsDefault");
		if (main == null && s != null && s.equalsIgnoreCase("true")) {
			main = p;
			index = i;
		}
		ids.add(p.getValue("ID"));
	}
	
	if (main == null) {
		Message.printWarning(2, routine,
			"No layout was marked as the main layout.  Values "
			+ "from the first layout will be used, instead.");
		main = __layouts.get(0);
	}

	String id = main.getValue("ID");
	String paperSize = main.getValue("PaperSize");
	String orient = main.getValue("PageOrientation");
	String sFontSize = main.getValue("NodeLabelFontSize");
	int fontSize = -1;
	try {
		fontSize = (Integer.decode(sFontSize)).intValue();
	}
	catch (Exception e) {
		fontSize = __DEFAULT_FONT_SIZE;
	}

	String sNodeSize = main.getValue("NodeSize");
	int nodeSize = -1;
	try {
		nodeSize = (Integer.decode(sNodeSize)).intValue();
	}
	catch (Exception e) {
		nodeSize = __DEFAULT_NODE_SIZE;
	}
	
	__orient = orient;
	__paperSize = paperSize;
	__nodeSize = nodeSize;
	__fontSize = fontSize;

	__ignoreEvents = true;
	__layoutComboBox.setData(ids);
	__layoutComboBox.select(id);
	__ignoreEvents = false;

	__layoutIndex = index;
	if (size == 1) {
		__deleteButton.setEnabled(false);
	}
}

/**
Sets the bounds for the paper based on the network.  This is only done for 
pre-existing networks (i.e., not those read from a net file).
@param network the network to use for determining paper bounds.
*/
private void setBoundsFromNetwork(StateMod_NodeNetwork network) {
	__rx = network.getRX();
	__ty = network.getTY();
	__lx = network.getLX();
	__by = network.getBY();

	if (__rx != -999.0 && __ty != -999.0 && __lx != -999.0 
	    && __by != -999.0) {
		__device.setXMLDataLimits(__lx, __by, __rx - __lx, __ty - __by);
		__reference.setNewDataLimits(new GRLimits(__lx, __by, __rx, __ty));
	}
}

/**
Sets whether the network is running within StateModGUI.
@param inStateModGUI if true, then the network is being displayed within StateModGUI.
*/
public void setInStateModGUI(boolean inStateModGUI) {
	__inStateModGUI = inStateModGUI;
}

/**
Sets the location shown in the status bar.
*/
public void setLocation(double x, double y) {
	__locationJTextField.setText("" 
		+ StringUtil.formatString(x, "%13.6f").trim()
		+ ", " 
		+ StringUtil.formatString(y, "%13.6f").trim());
}

/**
Sets the network to draw.
@param dirty whether the network should be marked dirty or not.
@param doAll whether the drawing component should do a re-initialization of
other data members when the dirty is set or not.  This should only be true
if setting the network for the first time.
*/
protected void setNetwork(StateMod_NodeNetwork network, boolean dirty,
boolean doAll) {
	__device.setNetwork(network, dirty, doAll);
}

/**
Sets whether the redo button should be enabled or not.
@param enabled whether the redo button should be enabled or not.
*/
public void setRedo(boolean enabled) {
	__redoJButton.setEnabled(enabled);
}

/**
Sets whether the undo button should be enabled or not.
@param enabled whether the undo button should be enabled or not.
*/
public void setUndo(boolean enabled) {
	__undoJButton.setEnabled(enabled);
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "StateMod_Network_JFrame.setupGUI";
	int dl = 20;

	JGUIUtil.setSystemLookAndFeel(true);

//	IOUtil.testing(true);
	addWindowListener(this);

	JPanel centerPanel = new JPanel();
	centerPanel.setLayout(new GridBagLayout());
	getContentPane().add("Center", centerPanel);

	double scale = .5;
	try {
		
	// read in the pageformat from the network here!
		
	PageFormat pageFormat = PrintUtil.getPageFormat(__DEFAULT_PAPER_SIZE);
	PrintUtil.setPageFormatOrientation(pageFormat, PageFormat.LANDSCAPE);
	PrintUtil.setPageFormatMargins(pageFormat, .75, .75, .75, .75);	
	__device.setPageFormat(pageFormat);

	int hPixels = (int)(pageFormat.getWidth() / scale);
	int vPixels = (int)(pageFormat.getHeight() / scale);	

	int aspect = GRAspect.TRUE;
	aspect = GRAspect.TRUE;

	int leftMargin = (int)(pageFormat.getImageableX() / scale);
	int rightMargin = (int)((pageFormat.getWidth() 
		- (pageFormat.getImageableWidth() 
		+ pageFormat.getImageableX())) / scale);
	int topMargin = (int)(pageFormat.getImageableY() / scale);
	int bottomMargin = (int)((pageFormat.getHeight() 
		- (pageFormat.getImageableHeight() 
		+ pageFormat.getImageableY())) / scale);	
	__device.setTotalSize(hPixels, vPixels);
	GRLimits drawingLimits = new GRLimits(0.0, 0.0, 1000, 1000);
	GRJComponentDrawingArea drawingArea = new GRJComponentDrawingArea(
		__device, "StateMod_Network DrawingArea", GRAspect.TRUE, 
		drawingLimits, GRUnits.DEVICE, GRLimits.DEVICE, drawingLimits);	

	__reference = new StateMod_NetworkReference_JComponent(this);
	__reference.setPreferredSize(new Dimension(200, 200));

	if (!__isXML) {
		__reference.setNetwork(__device.getNetwork());
		__reference.setNodesArray(__device.getNodesArray());		
	}
	GRLimits refLimits = new GRLimits(0, 0, 200, 200);
	GRJComponentDrawingArea refDrawingArea = new GRJComponentDrawingArea(
		__reference, "StateMod_NetworkReference DrawingArea", 
		aspect, refLimits, GRUnits.DEVICE, 
		GRLimits.DEVICE, refLimits);	

	// Find the maximum and minimum coordinates to be plotted, considering the nodes...
	double xmax, xmin, ymax, ymin;

	xmin = ymin = 10000000.0;
	xmax = ymax = -10000000.0;

	if (!__isXML) {
		// Don't have limits in legacy network so need to compute
		StateMod_NodeNetwork network = __device.getNetwork();

		HydrologyNode node = null;
		HydrologyNode nodeTop = network.getMostUpstreamNode();
	
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Start with node \"" + nodeTop.getCommonID() + "\"");
		}
		for (node = nodeTop; node != null;
		    node = StateMod_NodeNetwork.getDownstreamNode(node, StateMod_NodeNetwork.POSITION_COMPUTATIONAL)) {
			try {	
				xmin = MathUtil.min(xmin, node.getX());
				xmax = MathUtil.max(xmax, node.getX());
				ymin = MathUtil.min(ymin, node.getY());
				ymax = MathUtil.max(ymax, node.getY());
			}
			catch (Exception e) {
				Message.printWarning(2, routine, "Unknown error:");
				Message.printWarning(2, routine, e);
			}
			
			// Break if we are at the end of the list...
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				break;
			}
		}

		double dataWidth = xmax - xmin;
		double dataHeight = ymax - ymin;
	
		double yAdd = dataHeight * .05;
		double xAdd = dataWidth * .05;

		xmin -= (leftMargin + xAdd);
		xmax += (rightMargin + xAdd);
	
		ymin -= (bottomMargin + yAdd);
		ymax += (topMargin + yAdd);

		// Now set the data limits for the drawing area...

		Message.printStatus(2, routine,
			"Limits for plot data are " + xmin + "," + ymin + " to " + xmax + "," + ymax);
	
		GRLimits grlimits = new GRLimits(xmin, ymin, xmax, ymax);
		drawingArea.setDataLimits(grlimits);
	}
	
	__device.setDrawingArea(drawingArea);
	__device.calculateDataLimits();
	refDrawingArea.setDataLimits(__device.getDataLimits());
	__reference.setDrawingArea(refDrawingArea);
	__reference.setNewDataLimits(__device.getDataLimits());
	__device.setReference(__reference);
	__reference.setNetworkJComponent(__device);

	JGUIUtil.addComponent(centerPanel, __device,
		0, 1, 2, 1, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JPanel southPanel = new JPanel();
	southPanel.setLayout(new GridBagLayout());
	getContentPane().add("South", southPanel);

	__reference.setBorder(BorderFactory.createTitledBorder( "Network Reference"));

	JGUIUtil.addComponent(centerPanel, __reference,
		0, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel panel = new JPanel();
	panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
	panel.setLayout(new GridBagLayout());
	JButton button1 = new JButton("Zoom in");
	button1.addActionListener(this);
	JButton button2 = new JButton("Zoom out");
	button2.addActionListener(this);
	JButton button3 = new JButton("1:1");
	button3.addActionListener(this);
	JButton button4 = new JButton("Fit Width");
	button4.addActionListener(this);
	JButton button5 = new JButton("Fit Height");
	button5.addActionListener(this);
	JButton button6 = new JButton("Fit On Screen");
	button6.addActionListener(this);

	JButton button7 = new JButton("Save to XML");
	button7.addActionListener(this);

	button1.addKeyListener(__device);
	button2.addKeyListener(__device);
	button3.addKeyListener(__device);
	button4.addKeyListener(__device);
	button5.addKeyListener(__device);
	button6.addKeyListener(__device);
	button7.addKeyListener(__device);
	
	__layoutComboBox = new SimpleJComboBox();
	__layoutComboBox.addItemListener(this);
	__layoutComboBox.setToolTipText("Page layout name (e.g., \"11x17 Landscape\")." );
	
	__printedFontSizeComboBox = new SimpleJComboBox(false); // Do not allow text edits
	__printedFontSizeComboBox.add("3");
	__printedFontSizeComboBox.add("4");
	__printedFontSizeComboBox.add("5");
	__printedFontSizeComboBox.add("6");
	__printedFontSizeComboBox.add("7");
	__printedFontSizeComboBox.add("8");
	__printedFontSizeComboBox.add("9");
	__printedFontSizeComboBox.add("10");
	__printedFontSizeComboBox.add("11");
	__printedFontSizeComboBox.add("12");
	__printedFontSizeComboBox.add("13");
	__printedFontSizeComboBox.add("14");
	__printedFontSizeComboBox.add("15");
	__printedFontSizeComboBox.add("16");
	__printedFontSizeComboBox.add("17");
	__printedFontSizeComboBox.add("18");
	__printedFontSizeComboBox.add("19");
	__printedFontSizeComboBox.add("20");
	__printedFontSizeComboBox.add("21");
	__printedFontSizeComboBox.add("22");
	__printedFontSizeComboBox.add("23");
	__printedFontSizeComboBox.add("24");
	__printedFontSizeComboBox.add("25");
	__printedFontSizeComboBox.add("26");
	__printedFontSizeComboBox.add("27");
	__printedFontSizeComboBox.add("28");
	__printedFontSizeComboBox.add("29");
	__printedFontSizeComboBox.add("30");
	__printedFontSizeComboBox.setToolTipText("Node label size in points for the printed network, " +
			"for the specified page layout." );
	__printedFontSizeComboBox.select("" + __DEFAULT_FONT_SIZE);
	__printedFontSizeComboBox.addKeyListener(__device);
	__printedFontSizeComboBox.addItemListener(this);

	__orientationComboBox = new SimpleJComboBox();
	__orientationComboBox.add("Landscape");
	__orientationComboBox.add("Portrait");
	__orientationComboBox.setToolTipText("Page orientation (screen and printed network), " +
			"for the specified page layout.");
	__orientationComboBox.select(__DEFAULT_PAGE_ORIENTATION);
	__orientationComboBox.addKeyListener(__device);
	__orientationComboBox.addItemListener(this);
	
	__nodeSizeComboBox = new SimpleJComboBox(false); // Do not allow text edits
	__nodeSizeComboBox.add("3");
	__nodeSizeComboBox.add("4");
	__nodeSizeComboBox.add("5");
	__nodeSizeComboBox.add("6");
	__nodeSizeComboBox.add("7");
	__nodeSizeComboBox.add("8");
	__nodeSizeComboBox.add("9");
	__nodeSizeComboBox.add("10");
	__nodeSizeComboBox.add("11");
	__nodeSizeComboBox.add("12");
	__nodeSizeComboBox.add("14");
	__nodeSizeComboBox.add("16");
	__nodeSizeComboBox.add("18");
	__nodeSizeComboBox.add("20");
	__nodeSizeComboBox.add("22");
	__nodeSizeComboBox.add("24");
	__nodeSizeComboBox.add("26");
	__nodeSizeComboBox.add("28");
	__nodeSizeComboBox.add("30");
	__nodeSizeComboBox.add("48");
	__nodeSizeComboBox.setToolTipText("Node symbol size in points for the printed network, " +
			"for the specified page layout." );
	__nodeSizeComboBox.select("" + __DEFAULT_NODE_SIZE);
	__nodeSizeComboBox.addKeyListener(__device);
	__nodeSizeComboBox.addItemListener(this);

	__paperSizeComboBox = new SimpleJComboBox();
	__paperSizeComboBox.add("11x17");
	__paperSizeComboBox.add("A - 8.5x11");
	__paperSizeComboBox.add("B - 11x17");
	__paperSizeComboBox.add("C - 17x22");
	__paperSizeComboBox.add("D - 22x34");
	__paperSizeComboBox.add("E - 34x44");
	__paperSizeComboBox.add("Executive - 7.5x10");
	__paperSizeComboBox.add("Letter - 8.5x11");
	__paperSizeComboBox.add("Legal - 8.5x14");
	__paperSizeComboBox.setToolTipText("Paper size for the printed network, for the specified page layout.");
	__paperSizeComboBox.select(__DEFAULT_PAPER_SIZE);
	__paperSizeComboBox.addKeyListener(__device);
	__paperSizeComboBox.addItemListener(this);

	__defaultLayoutCheckBox = new JCheckBox();
	__defaultLayoutCheckBox.setToolTipText(
		"Indicate whether the current layout should be shown when the network is loaded." );
	__defaultLayoutCheckBox.setSelected(true);
	__defaultLayoutCheckBox.addItemListener(this);

	int y = 0;
	JPanel pagePanel = new JPanel();
	pagePanel.setLayout(new GridBagLayout());
	pagePanel.setBorder(BorderFactory.createTitledBorder("Network and Page Layout Properties"));
	JGUIUtil.addComponent(pagePanel, new JLabel("Page layout: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__layoutComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
	JGUIUtil.addComponent(pagePanel, __layoutComboBox,
		1, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JButton setNameButton = new JButton(__BUTTON_SET_LAYOUT_NAME);
	setNameButton.addActionListener(this);

	JGUIUtil.addComponent(pagePanel, setNameButton,
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(pagePanel, new JLabel("Layout for editing? "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(pagePanel, __defaultLayoutCheckBox,
		1, y, 9, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(pagePanel, new JLabel("Paper size: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(pagePanel, __paperSizeComboBox,
		1, y, 9, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(pagePanel, new JLabel("Paper orientation: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(pagePanel, __orientationComboBox,
		1, y, 9, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(pagePanel, new JLabel("Printed font size: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(pagePanel, __printedFontSizeComboBox,
		1, y, 9, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(pagePanel, new JLabel("Printed node size: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(pagePanel, __nodeSizeComboBox,
		1, y, 9, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JButton addButton = new JButton(__BUTTON_ADD_LAYOUT);
	addButton.addActionListener(this);
	__deleteButton = new JButton(__BUTTON_DELETE_LAYOUT);
	__deleteButton.addActionListener(this);
	JGUIUtil.addComponent(pagePanel, addButton,
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(pagePanel, __deleteButton,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, pagePanel,
		0, 1, 10, 1, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	JPanel nodePanel = new JPanel();
	nodePanel.setBorder(BorderFactory.createTitledBorder("Node Properties"));
	JGUIUtil.addComponent(panel, nodePanel,
		10, 1, 1, 1, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	__nodeDescriptionTextField = new JTextField(20);
	__nodeDescriptionTextField.setToolTipText("Longer name for the node (e.g., the station or structure name).");
	__nodeTypeTextField = new JTextField(20);
	__nodeTypeTextField.setToolTipText("Node type, enforced by the network editor.");
	__nodeXYTextField = new JTextField(20);
	__nodeXYTextField.setToolTipText("Node coordinates in the network, unitless.");
	__nodeDBXYTextField = new JTextField(20);
	__nodeDBXYTextField.setToolTipText("Alternate node coordinates (e.g., geographic coordinates).");
	__nodeCommonIDTextField = new JTextField(20);
	__nodeCommonIDTextField.setToolTipText("Short identifier used in modeling and labeling the network.");

	__nodeDescriptionTextField.setEditable(false);
	__nodeTypeTextField.setEditable(false);
	__nodeXYTextField.setEditable(false);
	__nodeDBXYTextField.setEditable(false);
	__nodeCommonIDTextField.setEditable(false);

	__nodeDescriptionTextField.addKeyListener(__device);
	__nodeTypeTextField.addKeyListener(__device);
	__nodeXYTextField.addKeyListener(__device);
	__nodeDBXYTextField.addKeyListener(__device);
	__nodeCommonIDTextField.addKeyListener(__device);
	
	// Vertical separator
	
	JGUIUtil.addComponent(pagePanel, new JSeparator(JSeparator.VERTICAL),
		11, 0, 1, 8, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	
	// Right column of network properties. x=11 for label, x=12-21 for data field
	
	y = 0;
	int xLabel = 12;
	int xData = 13;
	JGUIUtil.addComponent(pagePanel, new JLabel("Xmin: "),
		xLabel, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__xminJTextField = new JTextField ( 10 );
	__xminJTextField.setEnabled(false); // Display only
	__xminJTextField.setToolTipText ( "Minimum x-coordinate calculated from node positions" );
	JGUIUtil.addComponent(pagePanel, __xminJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(pagePanel, new JLabel("Ymin: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__yminJTextField = new JTextField ( 10 );
	__yminJTextField.setEnabled(false); // Display only
	__yminJTextField.setToolTipText ( "Minimum y-coordinate calculated from node positions" );
	JGUIUtil.addComponent(pagePanel, __yminJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(pagePanel, new JLabel("Xmax: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__xmaxJTextField = new JTextField ( 10 );
	__xmaxJTextField.setEnabled(false); // Display only
	__xmaxJTextField.setToolTipText ( "Maximum x-coordinate calculated from node positions" );
	JGUIUtil.addComponent(pagePanel, __xmaxJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(pagePanel, new JLabel("Ymax: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ymaxJTextField = new JTextField ( 10 );
	__ymaxJTextField.setEnabled(false); // Display only
	__ymaxJTextField.setToolTipText ( "Maximum y-coordinate calculated from node positions" );
	JGUIUtil.addComponent(pagePanel, __ymaxJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(pagePanel, new JLabel("Left edge buffer: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__edgeBufferLeftJTextField = new JTextField ( 10 );
	__edgeBufferLeftJTextField.setEnabled(false); // Display only
	__edgeBufferLeftJTextField.setToolTipText ( "Additional width added to left of Xmin" );
	JGUIUtil.addComponent(pagePanel, __edgeBufferLeftJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(pagePanel, new JLabel("Right edge buffer: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__edgeBufferRightJTextField = new JTextField ( 10 );
	__edgeBufferRightJTextField.setEnabled(false); // Display only
	__edgeBufferRightJTextField.setToolTipText ( "Additional width added to right of Xmax" );
	JGUIUtil.addComponent(pagePanel, __edgeBufferRightJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(pagePanel, new JLabel("Top edge buffer: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__edgeBufferTopJTextField = new JTextField ( 10 );
	__edgeBufferTopJTextField.setEnabled(false); // Display only
	__edgeBufferTopJTextField.setToolTipText ( "Additional height added to top of Ymax" );
	JGUIUtil.addComponent(pagePanel, __edgeBufferTopJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	JGUIUtil.addComponent(pagePanel, new JLabel("Bottom edge buffer: "),
		xLabel, ++y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__edgeBufferBottomJTextField = new JTextField ( 10 );
	__edgeBufferBottomJTextField.setEnabled(false); // Display only
	__edgeBufferBottomJTextField.setToolTipText ( "Additional height added to bottom of Ymin" );
	JGUIUtil.addComponent(pagePanel, __edgeBufferBottomJTextField,
		xData, y, 9, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Node properties...

	nodePanel.setLayout(new GridBagLayout());
	JGUIUtil.addComponent(nodePanel, new JLabel("Type: "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(nodePanel, __nodeTypeTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(nodePanel, new JLabel("Description: "),
		0, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(nodePanel, __nodeDescriptionTextField,
		1, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(nodePanel, new JLabel("ID: "),
		0, 2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(nodePanel, __nodeCommonIDTextField,
		1, 2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(nodePanel, new JLabel("X, Y: "),
		0, 3, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(nodePanel, __nodeXYTextField,
		1, 3, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(nodePanel, new JLabel("Alt. X, Y: "),
		0, 4, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(nodePanel, __nodeDBXYTextField,
		1, 4, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	__annotationListJPanel = new StateMod_Network_AnnotationDataListJPanel (
		__device.getAnnotationData(), __device, true );
	__annotationListJPanel.setMinimumSize(new Dimension(175, 150));
	__annotationListJPanel.setPreferredSize(new Dimension(175, 150));
	JGUIUtil.addComponent(panel, __annotationListJPanel,
		11, 1, 1, 1, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	JGUIUtil.addComponent(centerPanel, panel,
		1, 2, 1, 1, 0, 0, 
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	buildMenuBar();
	buildToolBar();
	getContentPane().add("North", __toolBar);

	JPanel statusBar = new JPanel();
	statusBar.setLayout(new GridBagLayout());
	__statusJTextField = new JTextField(10);
	__statusJTextField.setEditable(false);
	__locationJTextField = new JTextField(20);
	__locationJTextField.setEditable(false);
	
	JGUIUtil.addComponent(statusBar, __statusJTextField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.EAST);
	JGUIUtil.addComponent(statusBar, __locationJTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	getContentPane().add(statusBar, "South");
	
	pack();
	setSize(900, 700);
	JGUIUtil.center(this);
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error setting up GUI.");
		Message.printWarning(2, routine, e);
	}
}

/**
Sets up the paper information when this class is instantiated with a 
pre-existing network (i.e., not one read from a net file).
*/
private void setupPaper() {
	String routine = "StateMod_Network_JFrame.setupPaper()";
	int index = 0;
	int size = __layouts.size();
	PropList main = null;
	PropList p = null;
	String s = null;
	List<String> ids = new Vector<String>();
	for (int i = 0; i < size; i++) {
		p = __layouts.get(i);
		s = p.getValue("IsDefault");
		if (main == null && s != null && s.equalsIgnoreCase("true")) {
			main = p;
			index = i;
		}
		ids.add(p.getValue("ID"));
	}
	
	if (main == null) {
		Message.printWarning(2, routine,
			"No layout was marked as the main layout.  Values "
			+ "from the first layout will be used, instead.");
		main = __layouts.get(0);
	}

	String id = main.getValue("ID");
	String paperSize = shorten(main.getValue("PaperSize"));
	String orient = main.getValue("PageOrientation");
	String sFontSize = main.getValue("NodeLabelFontSize");
	int fontSize = -1;
	try {
		fontSize = (Integer.decode(sFontSize)).intValue();
	}
	catch (Exception e) {
		fontSize = __DEFAULT_FONT_SIZE;
	}
	String sNodeSize = main.getValue("NodeSize");
	int nodeSize = -1;
	try {
		nodeSize = (Integer.decode(sNodeSize)).intValue();
	}
	catch (Exception e) {
		nodeSize = __DEFAULT_NODE_SIZE;
	}
	
	__orient = orient;
	__paperSize = paperSize;
	__nodeSize = nodeSize;
	__fontSize = fontSize;

	__ignoreEvents = true;
	__layoutComboBox.setData(ids);
	__layoutComboBox.select(id);
	__ignoreEvents = false;

	__layoutIndex = index;
	if (size == 1) {
		__deleteButton.setEnabled(false);
	}

	__device.forceRepaint();
	__device.setPaperSize(__paperSize);
	__device.setOrientation(__orient);	
	__device.setPrintNodeSize(__nodeSize);
	__device.setPrintFontSize(__fontSize);

	__nodeSizeComboBox.select("" + __nodeSize);
	__orientationComboBox.select(__orient);
	__paperSizeComboBox.setSelectedPrefixItem(__paperSize + " -");
	__printedFontSizeComboBox.select("" + __fontSize);
}

/**
Called when the display zooms 1:1 to enable/disable the 1:1 button.
@param zoomed if the zoom is something other than 1:1, this is true.
*/
public void setZoomedOneToOne(boolean zoomed) {
	if (zoomed) {
		__zoom1JButton.setEnabled(false);
	}
	else {
		__zoom1JButton.setEnabled(true);
	}
}

/**
Checks to see if a string contains a dash, and if so, strips off the text
before the dash and returns it.  Otherwise just returns the string.
@param s the String to check.
@return the entire String if it does not contain a dash, otherwise return what comes before the dash.
*/
public String shorten(String s) {
	int index = s.indexOf("-");
	if (index < 0) {
		return s;
	}
	s = s.substring(0, index);
	return s.trim();
}

/**
Update the network display information (Xmin, etc., and edgeBuffer).
Only the limits should be changed external to this interface but update all to allow initialization from
the network.
*/
public void updateNetworkLayoutExtents ( double xmin, double ymin, double xmax, double ymax,
	double [] edgeBuffer )
{
	if ( !Double.isNaN(xmin) ) {
		this.__xminJTextField.setText ( StringUtil.formatString(xmin,"%0.6f") );
	}
	if ( !Double.isNaN(ymin) ) {
		this.__yminJTextField.setText ( StringUtil.formatString(ymin,"%0.6f") );
	}
	if ( !Double.isNaN(xmax) ) {
		this.__xmaxJTextField.setText ( StringUtil.formatString(xmax,"%0.6f") );
	}
	if ( !Double.isNaN(ymax) ) {
		this.__ymaxJTextField.setText ( StringUtil.formatString(ymax,"%0.6f") );
	}
	if ( !Double.isNaN(edgeBuffer[0]) ) {
		this.__edgeBufferLeftJTextField.setText ( StringUtil.formatString(edgeBuffer[0],"%0.6f") );
	}
	if ( !Double.isNaN(edgeBuffer[1]) ) {
		this.__edgeBufferRightJTextField.setText ( StringUtil.formatString(edgeBuffer[1],"%0.6f") );
	}
	if ( !Double.isNaN(edgeBuffer[2]) ) {
		this.__edgeBufferTopJTextField.setText ( StringUtil.formatString(edgeBuffer[2],"%0.6f") );
	}
	if ( !Double.isNaN(edgeBuffer[3]) ) {
		this.__edgeBufferBottomJTextField.setText ( StringUtil.formatString(edgeBuffer[3],"%0.6f") );
	}
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowClosed(WindowEvent event) {}

/**
Responds to window closing events and calls closeClicked().
@param event the WindowEvent that happened.
*/
public void windowClosing(WindowEvent event) {
//	Message.printStatus(1, "", "WindowClosing:\n"
//		+ "SaveOnExit: " + __saveOnExit 
//		+ "\nNewNetwork: " + __newNetwork
//		+ "\nDirty: " + __device.isDirty());
	if (__saveOnExit) {
		if (__device.isDirty()) {
			int x = (new ResponseJDialog(this, "Save network?",
				"The network has not been saved.  Save?",
				ResponseJDialog.YES | ResponseJDialog.NO)).response();
			if (x == ResponseJDialog.YES) {
				__device.saveXML(getFilename());
				__device.setDirty(false);
			}
		}
	}
	if (event.getSource() == this) {
		// TODO SAM 2011-07-07 What is the rationale?  To speed up redisplay of windows in StateDMI?
		// Event was generated from within this class - do not dispose
		closeClicked( false );
	}
}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent event) {}

/**
Zoom to the annotations and redraw.  This is generally called after adding a new annotation,
so the user will see what was highlighted on the map.
@param zoomBuffer The percent (1.0 is 100%) to expand the visible area in
both directions for the selected shapes.  For example, specifying a value of
1.0 would result in a viewable area that is 50% bigger than selected shapes on each edge.
@param zoomBuffer2 If the selected shapes result in a region that is a single
point, then zoomBuffer2 can be applied similar to zoomBuffer but using the
dimension of the main view as the reference region.
*/
public void zoomToAnnotations ( double zoomBuffer, double zoomBuffer2 )
{
	StateMod_Network_JComponent networkJComponent = __device;
	List<StateMod_Network_AnnotationData> annotationDataList = networkJComponent.getAnnotationData();
	GRLimits dataLimits = null; 
	for ( StateMod_Network_AnnotationData annotationData: annotationDataList ) {
		// Have to check for zero because some shapes
		// don't have coordinates...  For now check only the max...
		GRLimits annotationDataLimits = annotationData.getLimits();
		if ( dataLimits == null) {
			dataLimits = new GRLimits( annotationDataLimits );
		}
		else {	
			dataLimits = dataLimits.max( annotationDataLimits );
		}
	}
	// Increase the limits...
	double xincrease = 0.0, yincrease = 0.0;
	if (dataLimits.getMinX() == dataLimits.getMaxX()) {
		xincrease = networkJComponent.getDataLimitsMax().getWidth()*zoomBuffer2;
	}
	else {	
		xincrease = dataLimits.getWidth()*zoomBuffer;
	}
	if (dataLimits.getMinY() == dataLimits.getMaxY()) {
		yincrease = networkJComponent.getDataLimitsMax().getHeight()*zoomBuffer2;
	}
	else {	
		yincrease = dataLimits.getHeight()*zoomBuffer;
	}
	dataLimits.increase(xincrease, yincrease);
	// Center the reference network zoom on the given limits and reposition the main map..
	networkJComponent.centerOn(dataLimits);
}

// TODO (JTS - 2005-04-19) necessary anymore??
public void setSaveOnExit(boolean saveOnExit) {
	__saveOnExit = saveOnExit;
}

public void orientationComboBoxSelected() {
	int index = __layoutComboBox.getSelectedIndex();
	String value = __orientationComboBox.getSelected();
	/*
System.out.println("DL1: " + __device.getDataLimits());		
System.out.println("DL1: " + __device.getTotalDataLimits());		
System.out.println("TW/TH: " + __device.getTotalWidth() + "  "
+ __device.getTotalHeight());
*/
	__device.setOrientation(__orientationComboBox.getSelected());
	/*
System.out.println("DL2: " + __device.getDataLimits());		
System.out.println("DL2: " + __device.getTotalDataLimits());		
System.out.println("TW/TH: " + __device.getTotalWidth() + "  "
+ __device.getTotalHeight());
*/
	__reference.setNewDataLimits(__device.getTotalDataLimits());
	__reference.forceRepaint();
	PropList p = __layouts.get(index);
	p.set("PageOrientation=\"" + value + "\"");
}

}

// TODO (JTS - 2004-08-17)
// the box in the reference window does not follow the mouse cursor exactly.
// the more you move to the up and right, the more "off" the box becomes.
