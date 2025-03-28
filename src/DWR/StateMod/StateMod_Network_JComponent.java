// StateMod_Network_JComponent - this class draws the network that can be printed, viewed and altered.

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

// TODO SAM 2011-07-07 This class is too complicated
// Need to isolate UI interactions in a separate class or at least make less intermingled here
// Need to have all setup calculations done in one place (e.g., height/width/scale) rather than
// throughout the code.  This will allow the logic to be simpler and likely also will reduce the amount
// of code.  Changes have occurred recently to ensure that printing and saving images occurs without interfering
// with the network objects in the interactive editor.  Current behavior is as follows:
// * Interactive drawing uses legacy code, but the code has checks for printing etc.  This is because the
//   previous design did all the work in-line whereas the current design splits printing into a setup step
//   with calls to the basic rendering methods.
// * Printing the entire network - occurs with a call to printNetwork() which for new printing calls print() -
//   a simpler sequence of steps occurs, bypassing the interactive code in paint().
// * Printing the partial network - currently is not supported (may or may not work) - this needs revisited.
//   Ideally this could work like printing the full network, but set the imageable area to the selected paper
//   size and the data limits to a subset of the network.
// * Saving the entire network to an image file - to be implemented ASAP - do similar to printing the entire network
//   but render to a buffer and then save the buffer
// * Saving the visible network to an image file - need to do something similar to TSTool where the active buffer
//   is saved - this may need rework in the future if batch image saving is implemented in StateDMI and a visible
//   window/buffer cannot be dumped
//
// TODO SAM 2011-07-09 The font size and node size are documented to be in points, but in the code they
// are sometimes treated as pixels.  Since 72 points = 1 inch and many screens are 72+ DPI, this is not a
// horrible error but does lead to the screen output looking different that printed.  Need to scrub the code
// and make sure that configuration information in points is converted accurately to pixels.  The configuration
// should be in absolute coordinates (points) rather than pixels, which can vary between devices.  Making the
// change may require changing data sets to use a different font and node size... so wait for now.
//
// TODO SAM 2011-07-09 Need to figure out how to set the overall network limits... can't just use node
// data because a new network with one node is a singularity.

package DWR.StateMod;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import cdss.domain.hydrology.network.HydrologyNode;

import RTi.GR.GRArrowStyleType;
import RTi.GR.GRAspectType;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRText;
import RTi.GR.GRUnits;
import RTi.GR.GRLineStyleType;
import RTi.Util.GUI.JComboBoxResponseJDialog;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.TextResponseJDialog;
import RTi.Util.IO.GraphicsPrinterJob;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import javax.swing.RepaintManager;

/**
This class draws the network that can be printed, viewed and altered.
There are 3 main ways of drawing the network:
<ol>
<li>	Drawing to the screen - uses double buffering for good visual.</li>
<li>	Printing - prints to a graphics object from the printer.</li>
<li>	Saving an image file - specify the image size and area will be scaled to fit.</li>
</ol>
*/
@SuppressWarnings("serial")
public class StateMod_Network_JComponent extends GRJComponentDevice
implements ActionListener, KeyListener, MouseListener, MouseMotionListener, Printable {

/**
Strings for menu items.
*/
private final String
	__MENU_ADD_ANNOTATION = "Add Annotation",
	__MENU_ADD_LINK = "Add Link",
	__MENU_ADD_NODE = "Add Upstream Node",
	__MENU_DELETE_ANNOTATION = "Delete Annotation",
	__MENU_DELETE_LINK = "Delete Link for Node",
	__MENU_DELETE_NODE = "Delete Node",
	__MENU_DRAW_NODE_LABELS = "Draw Text",
	__MENU_EDITABLE = "Editable",
	__MENU_FIND_NODE = "Find Node",
	__MENU_FIND_ANNOTATION = "Find Annotation",
	__MENU_INCH_GRID = "Show Half-Inch Grid",
	__MENU_MARGIN = "Show Margins",
	__MENU_PIXEL_GRID = "Show 25 Pixel Grid",
	__MENU_PRINT_NETWORK = "Print Entire Network",
	__MENU_PRINT_SCREEN = "Print Screen",
	__MENU_PROPERTIES = "Properties",
	__MENU_REFRESH = "Refresh",
	__MENU_SAVE_NETWORK = "Save Network as Image",
	__MENU_SAVE_SCREEN = "Save Screen as Image",
	__MENU_SAVE_XML = "Save XML Network File",
	__MENU_SHADED_RIVERS = "Shaded Rivers",
	__MENU_SNAP_TO_GRID = "Snap to Grid";

/**
Modes that the network can be placed in for responding to mouse presses.
*/
public final static int 
	MODE_PAN = 0,
	MODE_SELECT = 1;

/**
Whether legacy printing code should be used.  This allows old code to be kept in-line until new code is tested.
*/
private boolean __useOldPrinting = false;

/**
Whether the annotations read in from an XML file were processed yet.
*/
private boolean __annotationsProcessed = false;

/**
The default setting for anti-aliasing.  
*/
private boolean __antiAliasSetting = true;

/**
Whether the drawing code should be anti-aliased or not.
*/
private boolean __antiAlias = __antiAliasSetting;

/**
Whether to draw the grid or not.
*/
private boolean __drawInchGrid = false;

/**
Whether the mouse drag is currently drawing a box around nodes or not.
*/
private boolean __drawingBox = false;

/**
Whether to draw the printable area margin or not.
*/
private boolean __drawMargin = true;

/**
Whether to draw the labels on the nodes in the network.
*/
private boolean __drawNodeLabels = true;

/**
Whether to draw a 50-pixel grid or not.
*/
private boolean __drawPixelGrid = false;

/**
Whether anything on the network can be changed or not.
*/
private boolean __editable = true;

/**
Whether the legend should not be drawn on the next draw (because it is being dragged).
*/
private boolean __eraseLegend = false;

/**
Whether the width was fit to be the max dimension when viewing the network
initially.  There is no matching "fitHeight" variable -- it is assumed that
if __fitWidth is false, then the network was fit to the paper height, instead.
*/
private boolean __fitWidth = false;

/**
Whether to force paint() to refresh the entire drawing area or not.  Must be
initially 'true' when the class is created.
*/
private boolean __forceRefresh = true;

/**
If true, then any calls to repaint the canvas will be ignored.
*/
private boolean __ignoreRepaint = false;

/**
Whether drawing settings need to be initialized because it is the first
time paint() has been called.  This should be false when the class is instantiated.
*/
private boolean __initialized = false;

/**
Whether the last thing clicked on in the network was an annotation or not.
If false, then a node was the last object selected.
*/
private boolean __isLastSelectedAnAnnotation = false;

/**
Whether the legend is being dragged.
*/
private boolean __legendDrag = false;

/**
Whether or not the legend limits have been calculated for the first time.
*/
private boolean __legendLimitsDetermined = false;

/**
Keeps track of whether the network has been changed by adding or deleting
a node or changing network properties since the last save or file open.  Use getIsDirty()
to determine if the main network or any of the node properties have changed.
TODO SAM 2008-12-11 Why isn't this information stored in the network object instead of this component?
*/
private boolean __networkChanged = false;

/**
Whether a node is currently being dragged on the screen.  This is true also
if an annotation node is being dragged on the screen.
*/
private boolean __nodeDrag = false;

/**
Whether the entire network is currently being printed.
*/
private boolean __printingNetwork = false;

/**
Whether only the current screen is currently being printed.
*/
private boolean __printingScreen = false;

/**
Whether annotations were read from a file and will need processed before being drawn.
*/
private boolean __processAnnotations = false;

/**
Whether the entire network image is currently being saved.
*/
private boolean __savingNetwork = false;

/**
Whether only the current screen image is currently being saved.
*/
private boolean __savingScreen = false;

/**
Whether the screen position is currently being dragged.
*/
private boolean __screenDrag = false;

/**
Whether rivers should be shaded.
*/
private boolean __shadedRivers = false;

/**
Whether to draw a red bounding box around annotation text.  Useful for debugging.
*/
private boolean __showAnnotationBoundingBox = false;

/**
Whether to snap node and legend movements to a grid.
*/
private boolean __snapToGrid = false;

/**
A buffered image to use for rendering what will be printed or saved to an image file.
*/
private BufferedImage __tempBuffer = null;

/**
The array of X and Y positions for nodes being dragged.
*/
private double[] 
	__draggedNodesXs = null,
	__draggedNodesYs = null;

/**
The current node size (as drawn for the current scale), in data units.
*/
private double __currNodeSize = 20;

// TODO SAM 2010-12-29 Evaluate how this can be rectified with __dataLimitsMax
/**
The absolute Bottom Y of the data limits.
TODO SAM 2011-07-09 Why not use getDrawingArea().getDataLimits().getBottomY() to avoid redundant data?
*/
private double __dataBottomY;

/**
The absolute Left X of the data limits.
TODO SAM 2011-07-09 Why not use getDrawingArea().getDataLimits().getLeftX() to avoid redundant data?
*/
private double __dataLeftX;

/**
Values to keep track of where a mouse drag started and where it is now
(TODO SAM 2011-07-09, in screen coordinates?).
*/
private double 
	__currDragX,
	__currDragY,
	__dragStartX,
	__dragStartY;

/**
The amount to adjust the x and y values by when panning.  It is calculated
whenever the zoom level changes, and then applied to the actual amount of 
mouse dx and dy to see how far the screen should move during a pan.
*/
private double 
	__DX = 1,
	__DY = 1;

/**
The size of the grid that is both drawn and snapped-to.
*/
private double __gridStep = 25;

/**
The height of the drawing area, in pixels.  Used for determining when the screen has been resized.
*/
private double __drawingAreaHeight = 0;

/**
The width of the drawing area, in pixels.  Used for determining when the screen has been resized.
*/
private double __drawingAreaWidth = 0;

/**
Used when reading in an XML network to hold the print node size so it can be
applied after the graphics have been initialized.
*/
private double __holdPrintNodeSize = -1;

/**
The size of the nodes drawn in the legend, in device units (e.g., pixels for screen drawing).
*/
private double __legendNodeDiameter = 20;

/**
The maximum reach level in the network.
*/
private double __maxReachLevel = 0;

/**
The X and Y location of the last mouse press in data values. 
*/
private double 
	__mouseDataX,	
	__mouseDataY;

/**
The X and Y location of the last mouse press in device values.
*/
private double 
	__mouseDownX = 0,
	__mouseDownY = 0;

/**
The X and Y locations where the last popup menu was opened, in device terms.
*/
private double 
	__popupX = 0,
	__popupY = 0;

/**
The printing scale factor of the drawing.  This is the amount by which the
72 dpi printable pixels are scaled.  A printing scale value of 1 means that
the network will be printed at 72 pixels per inch (ppi), which is the 
Java standard.  A scale factor of .5 means that the network will be 
printed at 144 ppi.  A scale factor of 3 means that the network will be printed at 24 ppi.
*/
private double __printScale = 1;

/**
The Y value (in data units) of the bottom of the screen.
*/
private double __screenBottomY;

/**
The current height of the data limits displayed on screen.  May be much larger
or smaller than the actual data limits of the network, due to zooming.
*/
private double __screenDataHeight;

/**
The X value (in data units) of the left of the screen.
*/
private double __screenLeftX;

/**
The current width of the data limits displayed on screen.  May be much larger
or smaller than the actual data limits of the network, due to zooming.
*/
private double __screenDataWidth;

/**
The total data height necessary to draw the entire network on the paper.  If
the network is taller than the paper, then the height is the height of the entire network.
TODO SAM 2011-07-09 Why not use getDrawingArea().getDataLimits().getHeight() to avoid redundant data?
*/
private double __totalDataHeight;

/**
The total data width necessary to draw the entire network on the paper.  If
the network is wider than the paper, then the width is the width of the entire network.
TODO SAM 2011-07-09 Why not use getDrawingArea().getDataLimits().getWidth() to avoid redundant data?
*/
private double __totalDataWidth;

/**
The difference between where the mouse was pressed on a node or legend to 
drag it, and the far bottom left corner of the node or legend.
*/
private double 
	__xAdjust,
	__yAdjust;

/**
The amount that the current zoom level is at.  100 is equal to a 1:1 zoom, where
things are sized the same on paper that they are on screen.
*/
private double __zoomPercentage = 0;

/**
The dash pattern that is used when drawing grids and debugging lines on the screen.
*/
private float[] __dashes = {3, 5f};

/**
The dash pattern that is used when drawing grids and debugging lines on the screen.
*/
private float[] __dots = {3, 3f};

/**
The graphics context that should be used for drawing to the temporary BufferedImage for printing.
*/
private Graphics2D __bufferGraphics = null;

/**
The drawing area on which the network is drawn, which corresponds to the chosen media (or visible screen area).
*/
private GRJComponentDrawingArea __drawingArea = null;

/**
The drawing area used if rendering were to the full scale media.  This is used only to store the drawing and
data limits at full scale so that the current rendering scale can be computed.
*/
private GRJComponentDrawingArea __drawingAreaFullScale = null;

/**
The array of limits of the nodes being dragged.
*/
private GRLimits[] __dragNodesLimits = null;

// TODO SAM 2011-07-05 This should not be needed but there are some circular references that need to be unwound
// Is this the data limits of actual data before calculateDataLimits() adjusts to what should be used in the
// drawing area?  One is data and one is media?
/**
Backup of the data limits for the visible network.  Stored here so as to 
avoid lots of calls to __drawingArea.getDataLimits();
*/
private GRLimits __dataLimits;

/**
Backup of the data limits for the network as initially opened, for zoom out.
*/
private GRLimits __dataLimitsMax;

/**
The limits of the node being dragged.
*/
private GRLimits __draggedNodeLimits;

/**
Temporary limits used while printing.  Declared here so as to avoid creating
one object every time paint() is called, even when not printing.  Limits used
to store the data limits during a drag when they're changed.
*/
private GRLimits __holdLimits;

/**
The limits of the legend, in data units.
*/
private GRLimits __legendDataLimits = new GRLimits(0, 0, 0, 0);

/**
Array of all the nodes in the node network.  Stored here for quick access,
rather than iterating through the network.  This array does NOT contain
annotations, even though some methods refer to nodes generically and process
network nodes and annotations.
*/
private HydrologyNode[] __nodes;

/**
The node network read in from a makenet file.
*/
private StateMod_NodeNetwork __network;

/**
The array of nodes being dragged.  This will never be null.
*/
private int[] __draggedNodes = new int[0];

/**
The node that was last clicked on.
*/
private int __clickedNodeNum = -1;

/**
The dpi of the screen or paper.  System-dependent.
*/
private int __dpi = 0;

/**
The height in pixels that the node label font should be on the screen, for the current scale, using __dpi
to convert between pixels and points.
*/
private int __fontSizePixels = 10;

/**
The height in points that the font should be on the screen, for the current scale.
*/
private int __fontSizePoints;

/**
Used when reading in an XML network to hold certain values so they can be
applied after the graphics have been initialized.
*/
private int __holdPrintFontSize = -1;

/**
The thickness to scale 1-pixel-wide lines to during zooming in.
*/
private int __lineThickness = 1;

/**
The last mouse click positions in pixels.
*/
private int 
	__mouseDeviceX = 0,
	__mouseDeviceY = 0;

/**
The mode the component is in in regard to how it responds to mouse presses.
*/
private int __networkMouseMode = MODE_PAN;

/**
The node size as set by the GUI at 1:1 zoom, in pixels.
*/
private int __nodeSizeFullScale = 20;

/**
The number of the node that had a popup menu opened on it.
*/
private int __popupNodeNum = -1;

/**
The number of times print(Graphics...) has been called so far per print.  
It gets called three times per print (a Java foible), and 
redrawing the network for the print each time is inefficient.
*/
private int __printCount = 0;

/**
The size in pixels that the printed font should be, for full scale layout paper size.
The __printFontSizePoints is converted to pixels using the screen __dpi.
*/
private int __printFontSizePixels = 10;

/**
The size in points that the printed font should be, for full scale layout paper size, determined
from the layout "NodeLabelFontSize".
*/
private int __printFontSizePoints;

/**
The thickness that lines should be printed at.
*/
private int __printLineThickness = 1;

/**
TODO SAM 2011-07-09 Evaluate whether to make the properties a class.
Selected page layout properties (either from network being edited or batch job).
*/
private PropList __selectedPageLayoutPropList = null;

/**
The total height of the screen buffer.
*/
private int __totalBufferHeight;

/**
The total width of the screen buffer.
*/
private int __totalBufferWidth;

/**
The current position within the undo Vector.  The change operation at 
position __undoPos - 1 is the one that last happened.  If __undoPos is 
less than __undoOperations.size(), then redo can be done.  If __undoPos is 0 then no undo can be done.
*/
private int __undoPos = 0;

/**
Popup JMenuItems
*/
private JMenuItem 
	__deleteLinkMenuItem = null,
	__addNodeMenuItem = null,
	__deleteNodeMenuItem = null;

/**
The popup menu that appears when an annotation is right-clicked on.
*/
private JPopupMenu __annotationPopup = null;

/**
The popup menu that appears when a node is right-clicked on.
*/
private JPopupMenu __nodePopup = null;

/**
The popup menu that appears when the pane is right-clicked on.
*/
private JPopupMenu __networkPopup;

/**
The pageformat to use for printing the network.
*/
private PageFormat __pageFormat;

/**
The parent JFrame in which this is displayed.
*/
private StateMod_Network_JFrame __parent;

/**
The reference window that is displayed along with this display.
*/
private StateMod_NetworkReference_JComponent __referenceJComponent;

/**
Used when reading in an XML network to hold certain values so they can be
applied after the graphics have been initialized.
*/
private String 
	__holdPaperOrientation = null,
	__holdPaperSize = null;

// FIXME SAM 2008-01-25 Need to change so annotations are not same object type normal nodes.
/**
List of all the annotations displayed on the network.  Note that internally
annotations are managed as a list of HydrologyNode.
*/
private List<HydrologyNode> __annotations = new Vector<HydrologyNode>();

/**
StateMod_Network_AnnotationRenderers to display extra information as annotations on the map.
Note that these are complex annotations whereas the __annotations list contains simple lines and shapes.
*/
private List<StateMod_Network_AnnotationData> __annotationDataList = new Vector<StateMod_Network_AnnotationData>();

/**
List of all the links drawn on the network.  Each link is managed as a PropList with link properties.
See the HydrologyNodeNetwork.writeXML() method for supported properties.
*/
private List<PropList> __links = new Vector<PropList>();

/**
List to hold change operations.
*/
private List<StateMod_Network_UndoData> __undoOperations = new Vector<StateMod_Network_UndoData>();

/**
Constructor used for headless operations, in particular printing.
No wrapping JFrame interactions are supported.
*/
public StateMod_Network_JComponent ( StateMod_NodeNetwork net, String requestedPageLayout )
{
	super("StateMod_Network_JComponent");
	// Set the network for use elsewhere in class (calculate
	setNetwork ( net,
		false, // don't mark the network dirty
		true ); // determine secondary lists such as a linear array of nodes, to streamline processing

    // A scale of anything less than 1 seems to make it illegible.
	// __printScale = scale;
	__printScale = 1;

	// Determine the system-dependent DPI for the monitor
	Toolkit t = Toolkit.getDefaultToolkit();
	__dpi = t.getScreenResolution();

	// Setup the initial drawing areas and other information based on the layout.  Final setup will
	// occur in the print() method based on the chosen printer and paper size
	// First tell the class that printing is occurring and set some data
	initializeForPrinting ();
	// Set the page size, etc. from the requested layout (defaults for printing/layout
	// but may be modified when print() gets called from the printJob
	initializeForNetworkPageLayout ( requestedPageLayout );
}

/**
Constructor.
@param parent the JFrame in which this component appears.
@param scale the scale to use for determining how the network should be drawn
for printing.  Printing is done at 72 dpi by default.  A printing scale of
.5 would mean that printed output would be rendered at 144 dpi, and .25 would
mean at 288 dpi.  Since each node is 20 pixels across, a scale of .25 seems to work best.
*/
public StateMod_Network_JComponent(StateMod_Network_JFrame parent, double scale)
{
	super("StateMod_Network_JComponent");

	__parent = parent;
    // A scale of anything less than 1 seems to make it illegible.
	// __printScale = scale;
	__printScale = 1;

	// determine the system-dependent DPI for the monitor
	Toolkit t = Toolkit.getDefaultToolkit();
	__dpi = t.getScreenResolution();

	// make sure this class listens to itself for certain events
	addKeyListener(this);
	addMouseListener(this);
	addMouseMotionListener(this);

	buildPopupMenus();

	// Set the default print font size for when the network is first displayed.
	// TODO (JTS - 2004-07-13) remove this call?
	setPrintFontSize(10);

	__undoOperations = new Vector<StateMod_Network_UndoData>();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event)
{	String routine = getClass().getName() + "actionPerformed";
	String action = event.getActionCommand();

	if (action.equals(__MENU_ADD_ANNOTATION)) {
		if (!__editable) {
			return;
		}
		addAnnotation(__popupX, __popupY);
		setNetworkChanged (true);
	}
	else if (action.equals(__MENU_ADD_LINK)) {
		new StateMod_Network_AddLink_JDialog(__parent, this, __nodes);
		setNetworkChanged (true);
	}
	else if (action.equals(__MENU_ADD_NODE)) {
		if (!__editable) {
			return;
		}
		new StateMod_Network_AddNode_JDialog(__parent, __nodes[__popupNodeNum]);
		// TODO (JTS - 2004-07-13) need a way to make sure that a node was really added 
		// and mark changed appropriately
		setNetworkChanged (true);
	}
	else if (action.equals(__MENU_DELETE_ANNOTATION)) {
		__annotations.remove(__popupNodeNum);
		setNetworkChanged (true);
		forceRepaint();
	}
	else if (action.equals(__MENU_DELETE_LINK)) {
		deleteLink();
	}
	else if (action.equals(__MENU_DELETE_NODE)) {
		if (!__editable) {
			return;
		}
		if (__popupNodeNum == -1) {
			return;
		}
		if (__isLastSelectedAnAnnotation) {
			__annotations.remove(__clickedNodeNum);
		}
		else {
			String id = __nodes[__popupNodeNum].getCommonID();
			removeIDFromLinks(id);
			__network.deleteNode(id);
			buildNodeArray();
			findMaxReachLevel();		
			__undoOperations = new Vector<StateMod_Network_UndoData>();
			__parent.setUndo(false);
			__parent.setRedo(false);			
		}
		setNetworkChanged (true);
		forceRepaint();
	}
	else if (action.equals(__MENU_DRAW_NODE_LABELS)) {
		if (__drawNodeLabels) {
			__drawNodeLabels = false;
		}
		else {
			__drawNodeLabels = true;
		}
		HydrologyNode.setDrawText(__drawNodeLabels);
		forceRepaint();
	}
	else if (action.equals(__MENU_EDITABLE)) {
		if (__editable) {
			__editable = false;
		}
		else {
			__editable = true;
		}
	}
	else if (action.equals(__MENU_FIND_ANNOTATION)) {
		findAnnotation();
	}
	else if (action.equals(__MENU_FIND_NODE)) {
		findNode();
	}
	else if (action.equals(__MENU_INCH_GRID)) {	
		if (__drawInchGrid) {	
			__drawInchGrid = false;
		}
		else {
			__drawInchGrid = true;
		}
		forceRepaint();
		__referenceJComponent.setDrawInchGrid(__drawInchGrid);
	}
	else if (action.equals(__MENU_MARGIN)) {
		if (__drawMargin) {
			__drawMargin = false;
		}
		else {
			__drawMargin = true;
		}
		forceRepaint();
		__referenceJComponent.setDrawMargin(__drawMargin);
	}
	else if (action.equals(__MENU_PIXEL_GRID)) {
		if (__drawPixelGrid) {
			__drawPixelGrid = false;
		}
		else {
			__drawPixelGrid = true;
		}
		forceRepaint();
	}
	else if (action.equals(__MENU_PRINT_NETWORK)) {
		printNetwork();
	}
	else if (action.equals(__MENU_PRINT_SCREEN)) {
		printScreen();
	}
	else if (action.equals(__MENU_PROPERTIES)) {
		if (__isLastSelectedAnAnnotation) {
			HydrologyNode node = __annotations.get(__popupNodeNum);
			new StateMod_Network_AnnotationProperties_JDialog( this, __editable, node, __popupNodeNum);
		}
		else {
			String idPre = __nodes[__popupNodeNum].getCommonID();
			new StateMod_Network_NodeProperties_JDialog(__parent, __nodes, __popupNodeNum);
			String idPost = __nodes[__popupNodeNum].getCommonID();
			if (!idPre.equals(idPost)) {
				adjustLinksForNodeRename(idPre, idPost);
			}
		}
	}
	else if (action.equals(__MENU_REFRESH)) {
		forceRepaint();
	}		
	else if (action.equals(__MENU_SAVE_NETWORK)) {
		saveNetworkAsImage();
//		Message.printStatus(1, "", "Save: setDirty(false)");
		setDirty(false);
	}
	else if (action.equals(__MENU_SAVE_SCREEN)) {
		try {
			saveScreenAsImage();
		}
		catch ( Exception e ) {
			Message.printWarning(1,routine,"Error saving image file (" + e + ")." );
		}
	}
	else if (action.equals(__MENU_SAVE_XML)) {
		saveXML(__parent.getFilename());
//		Message.printStatus(1, "", "Save: setDirty(false)");
		setDirty(false);
	}
	else if (action.equals(__MENU_SHADED_RIVERS)) {
		if (__shadedRivers) {
			__shadedRivers = false;
		}
		else {
			__shadedRivers = true;
		}
		forceRepaint();
	}
	else if (action.equals(__MENU_SNAP_TO_GRID)) {
		if (__snapToGrid) {
			__snapToGrid = false;
		}
		else {
			__snapToGrid = true;
		}
	}
}

/**
Adds an annotation node to the drawing area at the specified point.
@param x the x location of the annotation
@param y the y location of the annotation
*/
protected void addAnnotation(double x, double y)
{
	String text = new TextResponseJDialog(__parent, "Enter the annotation text",
		"Enter the annotation text:", ResponseJDialog.OK | ResponseJDialog.CANCEL).response();

	if (text == null) {
		// text will be null if the user pressed cancel
		return;
	}

	// Escape all instances of " -- they can cause problems when put into a PropList.
	text = StringUtil.replaceString(text, "\"", "'");

	// Round off the X and Y values to 6 digits after the decimal point
	y = toSixDigits(y);
	x = toSixDigits(x);

	// Default most of the information in the node
	HydrologyNode node = new HydrologyNode();
	String position = "Center";
	String props = 
		  "ShapeType=Text;"
		+ "FontSize=11;"
		+ "OriginalFontSize=11;"
		+ "FontStyle=Plain;"
		+ "FontName=Helvetica;"
		+ "Point=" + x + ", " + y + ";"
		+ "Text=\"" + text + "\";"
		+ "TextPosition=" + position;
	PropList p = PropList.parse(props, "", ";");
	node.setAssociatedObject(p);
	GRLimits limits = GRDrawingAreaUtil.getTextExtents(__drawingArea, text, GRUnits.DEVICE);	
	double w = convertDrawingXToDataX(limits.getWidth());
	double h = convertDrawingYToDataY(limits.getHeight());

	// Calculate the actual limits for the from the lower-left corner to the upper-right,
	// in order to know when the text has been clicked on (for dragging, or popup menus).
	
	if (position.equalsIgnoreCase("UpperRight")) {
		node.setPosition(x, y, w, h);
	}
	else if (position.equalsIgnoreCase("Right")) {
		node.setPosition(x, y - (h / 2), w, h);
	}
	else if (position.equalsIgnoreCase("LowerRight")) {
		node.setPosition(x, y - h, w, h);
	}
	else if (position.equalsIgnoreCase("Below") || position.equalsIgnoreCase("BelowCenter")) {
		node.setPosition(x - (w / 2), y - h, w, h);
	}
	else if (position.equalsIgnoreCase("LowerLeft")) {
		node.setPosition(x - w, y - h, w, h);
	}
	else if (position.equalsIgnoreCase("Left")) {
		node.setPosition(x - w, y - (h / 2), w, h);
	}
	else if (position.equalsIgnoreCase("UpperLeft")) {
		node.setPosition(x - w, y, w, h);
	}
	else if (position.equalsIgnoreCase("Above") || position.equalsIgnoreCase("AboveCenter")) {
		node.setPosition(x - (w / 2), y, w, h);
	}
	else if (position.equalsIgnoreCase("Center")) {
		node.setPosition(x - (w / 2), y - (h / 2), w, h);
	}

	node.setDirty(true);
	__annotations.add(node);
	forceRepaint();
}

/**
Add an annotation renderer.  This allows generic objects to be drawn on top of the map, allowing
rendering to occur by external code that is familiar with domain issues.  The StateMod_Network_JComponent
is passed back to the renderer to allow full access to layer information, symbols, etc.
@param renderer the renderer that will be called when it is time to draw the object
@param objectToRender the object to render (will be passed back to the renderer)
@param objectLabel label for the object, to list in the GeoViewJPanel
@param scrollToAnnotation if true, scroll to the annotation (without changing scale)
*/
public StateMod_Network_AnnotationData addAnnotationRenderer ( StateMod_Network_AnnotationRenderer renderer,
	Object objectToRender, String objectLabel, GRLimits limits )
{
	// Only add if the annotation is not already in the list
	for ( StateMod_Network_AnnotationData annotationData: __annotationDataList ) {
		if ( (annotationData.getObject() == objectToRender) &&
			annotationData.getLabel().equalsIgnoreCase(objectLabel) ) {
			// Don't add again.
			return null;
		}
	}
	StateMod_Network_AnnotationData annotationData =
		new StateMod_Network_AnnotationData(renderer,objectToRender,objectLabel,limits);
	__annotationDataList.add ( annotationData );
	repaint ();
	return annotationData;
}

/**
Adds a link to the network.
@param fromNodeId the ID of the node from which the link is drawn.
@param toNodeId the ID of the node to which the link is drawn.
*/
protected void addLink(String fromNodeId, String toNodeId, String linkId, String lineStyle,
	String fromArrowStyle, String toArrowStyle )
{
	PropList p = new PropList("");
	p.set("ID", linkId );
	p.set("ShapeType", "Link");
	p.set("FromNodeID", fromNodeId);
	p.set("ToNodeID", toNodeId);
	p.set("LineStyle", lineStyle );
	p.set("FromArrowStyle", fromArrowStyle );
	p.set("ToArrowStyle", toArrowStyle );
	__links.add(p);
	forceRepaint();
}

/**
Adds a node to the network.
@param name the name of the node.
@param type the type of the node.
@param upID the ID of the node immediately upstream.
@param downID the ID of the node immediately downstream.
@param isNaturalFlow whether the node is a natural flow node.
*/
protected void addNode(String name, int type, String upID, String downID, 
boolean isNaturalFlow, boolean isImport) {
	if (upID == null || upID.equals("None")) {
		upID = null;
	}
	StateMod_NodeNetwork network = getNetwork();

	network.addNode(name, type, upID, downID, isNaturalFlow, isImport);
	// TODO (JTS - 2004-07-13) is there a more efficient way??
	setNetworkChanged (true);
	__parent.setNetwork(network, true, true);
	buildNodeArray();
	findMaxReachLevel();
	forceRepaint();
}

/**
Adds a node change operation to the undo list.
@param nodeNum the node that was moved.
@param x the new node X position.
@param y the new node Y position.
*/
private void addNodeChangeOperation(int nodeNum, double x, double y)
{
	// Create a new UndoData object to store the location of the node before and 
	StateMod_Network_UndoData data = new StateMod_Network_UndoData();
	data.nodeNum = nodeNum;
	if ( __isLastSelectedAnAnnotation ) {
		// TODO SAM 2008-01-25 Figure out if undo applies to annotations also
		//data.oldX = getAnnotationNode(nodeNum).getX();
		//data.oldY = getAnnotationNode(nodeNum).getY();
		return;
	}
	else {
		data.oldX = __nodes[nodeNum].getX();
		data.oldY = __nodes[nodeNum].getY();
	}
	data.newX = x;
	data.newY = y;

	addNodeChangeOperation(data);
}

/**
Adds a change operation to the undo data.  This is called when a node is dragged
so that the operation can be undone.
@param data the UndoData detailing what happened in the operation.
*/
private void addNodeChangeOperation(StateMod_Network_UndoData data) {
	// undoPos tracks the current position within the undo list and
	// is used to allow undos and redos.  If a user has made changes,
	// and then undoes them, and then makes a new change, the previous
	// undos are made unavailable and are lost.  The following gets
	// rid of undos that cannot be undone
	if (__undoPos < __undoOperations.size()) {
		while (__undoPos < __undoOperations.size()) {
			__undoOperations.remove(__undoPos);
		}
	}
	__undoOperations.add(data);
	__undoPos = __undoOperations.size();

	// If a new undo is added, then it is the last undo in the list.
	__parent.setUndo(true);
	__parent.setRedo(false);
}

/**
Adjusts the ID values stored in the links Vector after a node has been renamed
so that the links point to the same node (though it has a different ID).
@param idPre the ID of the node before it was renamed.
@param idPost the ID of the node after it was renamed.
*/
private void adjustLinksForNodeRename(String idPre, String idPost) {
	if (__links == null) {
		return;
	}

	int size = __links.size();
	PropList p = null;
	String id1 = null;
	String id2 = null;
	for (int i = 0; i < size; i++) {
		p = (PropList)__links.get(i);
		id1 = p.getValue("FromNodeID");
		id2 = p.getValue("ToNodeID");

		if (id1.equals(idPre)) {
			p.setValue("FromNodeID", idPost);
		}
		else if (id2.equals(idPre)) {
			p.setValue("ToNodeID", idPost);
		}
	}
}	

/**
Adjust the current data height and width when the GUI screen size is changed, called from paint()
when a resize has been detected.
*/
private void adjustForResize() {
	int width = getBounds().width;
	int height = getBounds().height;

	double wpct = (double)width / (double)__drawingAreaWidth;
	double hpct = (double)height / (double)__drawingAreaHeight;

	__screenDataWidth *= wpct;
	__screenDataHeight *= hpct;
}

/**
From the nodes stored in the node network, this builds the array of nodes.
Nodes are stored in an array for quicker traversal during node operations.
*/
private void buildNodeArray() {
	boolean done = false;
	HydrologyNode node = __network.getMostUpstreamNode();
	HydrologyNode holdNode = null;
	List<HydrologyNode> nodes = new Vector<HydrologyNode>();

	while (!done) {
		if (node == null) {
			done = true;
		}
		else {
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				done = true;
			}		
			if (node.getType() != HydrologyNode.NODE_TYPE_UNKNOWN){
				nodes.add(node);
			}
			holdNode = node;	
			node = StateMod_NodeNetwork.getDownstreamNode(node, StateMod_NodeNetwork.POSITION_COMPUTATIONAL);		
		
			if (node == holdNode) {
				done = true;
			}			
		}
	}		

	int size = nodes.size();

	// Move the nodes from the list into an array for quicker traversal.

	__nodes = new HydrologyNode[size];

	for (int i = 0; i < size; i++) {
		__nodes[i] = (HydrologyNode)nodes.get(i);
		// FIXME SAM 2008-03-16 Need to remove WIS code
		//__nodes[i].setInWis(false);
		__nodes[i].setBoundsCalculated(false);

		double diam = 0;
		if (__fitWidth) {
			diam = convertDrawingXToDataX(__legendNodeDiameter);
		}
		else {
			diam = convertDrawingYToDataY(__legendNodeDiameter);
		}
		__nodes[i].setSymbol(null);
		__nodes[i].setBoundsCalculated(false);
		__nodes[i].setDataDiameter(diam);
		__nodes[i].calculateExtents(__drawingArea);
	}
	
	// Make sure that all the nodes have unique IDs.  If they don't,
	// checkUniqueID will generate a unique ID for the offending nodes.
	for (int i = size - 1; i > -1; i--) {
		checkUniqueID(i);
	}
}

/**
Builds the GUI's popup menus.
*/
private void buildPopupMenus() {
	JMenuItem mi = null;
	JCheckBoxMenuItem jcbmi = null;
	__annotationPopup = new JPopupMenu();
	__networkPopup = new JPopupMenu();
	__nodePopup = new JPopupMenu();
	
	// Popup menu for when an annotation is clicked on
	__annotationPopup = new JPopupMenu();
	mi = new JMenuItem(__MENU_PROPERTIES);
	mi.addActionListener(this);
	__annotationPopup.add(mi);
	mi = new JMenuItem(__MENU_DELETE_ANNOTATION);
	mi.addActionListener(this);
	__annotationPopup.add(mi);	

	// Popup menu for when the network window background is clicked on
	mi = new JMenuItem(__MENU_ADD_ANNOTATION);
	mi.addActionListener(this);
	__networkPopup.add(mi);
	mi = new JMenuItem(__MENU_ADD_LINK);
	mi.addActionListener(this);
	__networkPopup.add(mi);
	__networkPopup.addSeparator();
	// -------------------------
	mi = new JMenuItem(__MENU_FIND_NODE);
	mi.addActionListener(this);
	__networkPopup.add(mi);
	mi = new JMenuItem(__MENU_FIND_ANNOTATION);
	mi.addActionListener(this);
	__networkPopup.add(mi);
	__networkPopup.addSeparator();
	// -------------------------
	jcbmi = new JCheckBoxMenuItem(__MENU_SHADED_RIVERS);
	jcbmi.addActionListener(this);
	__networkPopup.add(jcbmi);	
	jcbmi = new JCheckBoxMenuItem(__MENU_DRAW_NODE_LABELS, true);
	jcbmi.addActionListener(this);
	__networkPopup.add(jcbmi);	
	jcbmi = new JCheckBoxMenuItem(__MENU_EDITABLE, true);
	jcbmi.addActionListener(this);
	__networkPopup.add(jcbmi);
	__networkPopup.addSeparator();
	// -------------------------
	jcbmi = new JCheckBoxMenuItem(__MENU_MARGIN);
	jcbmi.setSelected(true);
	jcbmi.addActionListener(this);
	__networkPopup.add(jcbmi);	
	jcbmi = new JCheckBoxMenuItem(__MENU_INCH_GRID);
	jcbmi.addActionListener(this);
	__networkPopup.add(jcbmi);
	jcbmi = new JCheckBoxMenuItem(__MENU_SNAP_TO_GRID);
	jcbmi.addActionListener(this);
	__networkPopup.add(jcbmi);

	// Popup menu for when a node is clicked on
	__addNodeMenuItem = new JMenuItem(__MENU_ADD_NODE);
	__addNodeMenuItem.addActionListener(this);
	__nodePopup.add(__addNodeMenuItem);
	__deleteNodeMenuItem = new JMenuItem(__MENU_DELETE_NODE);
	__deleteNodeMenuItem.addActionListener(this);
	__nodePopup.add(__deleteNodeMenuItem);
	__deleteLinkMenuItem = new JMenuItem(__MENU_DELETE_LINK);
	__deleteLinkMenuItem.addActionListener(this);
	__nodePopup.add(__deleteLinkMenuItem);
	__deleteLinkMenuItem.setEnabled(false);
	__nodePopup.addSeparator();
	// -------------------------
	mi = new JMenuItem(__MENU_PROPERTIES);
	mi.addActionListener(this);
	__nodePopup.add(mi);
	__nodePopup.addSeparator();
	// -------------------------
	mi = new JMenuItem(__MENU_FIND_NODE);
	mi.addActionListener(this);
	__nodePopup.add(mi);
	mi = new JMenuItem(__MENU_FIND_ANNOTATION);
	mi.addActionListener(this);
	__nodePopup.add(mi);
	__nodePopup.addSeparator();
	jcbmi = new JCheckBoxMenuItem(__MENU_SHADED_RIVERS);
	jcbmi.addActionListener(this);
	__nodePopup.add(jcbmi);	
	jcbmi = new JCheckBoxMenuItem(__MENU_DRAW_NODE_LABELS, true);
	jcbmi.addActionListener(this);
	__nodePopup.add(jcbmi);	
	jcbmi = new JCheckBoxMenuItem(__MENU_EDITABLE, true);
	jcbmi.addActionListener(this);
	__nodePopup.add(jcbmi);
	__nodePopup.addSeparator();
	// -------------------------
	jcbmi = new JCheckBoxMenuItem(__MENU_MARGIN);
	jcbmi.setSelected(true);
	jcbmi.addActionListener(this);
	__nodePopup.add(jcbmi);	
	jcbmi = new JCheckBoxMenuItem(__MENU_INCH_GRID);
	jcbmi.addActionListener(this);
	__nodePopup.add(jcbmi);
	jcbmi = new JCheckBoxMenuItem(__MENU_SNAP_TO_GRID);
	jcbmi.addActionListener(this);
	__nodePopup.add(jcbmi);	
}

/**
Builds limits and sets up the starting position for a drag for multiple nodes. 
This is done prior to dragging starting.
*/
private void buildSelectedNodesLimits() {
	List<Integer> v = new Vector<Integer>();

	// First get a list comprising the indices of the nodes in the
	// __nodes array that are being dragged.  This method is only called
	// if at least 1 node is selected, so the list will never be 0-size.
	for (int i = 0; i < __nodes.length; i++) {	
		if (!__nodes[i].isSelected()) {
			continue;
		}

		if (i == __clickedNodeNum) {	
			continue;
		}
	
		v.add(Integer.valueOf(i));
	}

	Integer I = null;
	int num = 0;
	int size = v.size();

	__dragNodesLimits = new GRLimits[size];
	__draggedNodes = new int[size];
	__draggedNodesXs = new double[size];
	__draggedNodesYs = new double[size];

	double[] p = null;
	
	// Go through the nodes that are selected and populate the arrays
	// created above with the respective nodes' limits and starting X and Y positions.  
	
	for (int i = 0; i < size; i++) {
		I = v.get(i);
		num = I.intValue();

		__dragNodesLimits[i] = __nodes[num].getLimits();
		__draggedNodesXs[i] = __nodes[num].getX();
		__draggedNodesYs[i] = __nodes[num].getY();
		__draggedNodes[i] = num;

		// If snap to grid is on, the positions of the nodes to be 
		// dragged are shifted to the nearest grid points prior to dragging.
		if (__snapToGrid) {
			p = findNearestGridXY(__nodes[num].getX(), __nodes[num].getY());
			__draggedNodesXs[i] = p[0];
			__draggedNodesYs[i] = p[1];
		}
	}
}

/**
Calculates the data limits necessary to make the entire network visible at
first.  This method should be called after the drawing area is set in this 
device, and before the data limits are set in the reference drawing area.
It is also called when the paper orientation changes.
*/
protected void calculateDataLimits() {
	GRLimits data = __drawingArea.getDataLimits();
	if (__dataLimits == null) {
		__dataLimits = data;
	}

	// Get the height and width of the data as set in the main JFrame
	// from what was read from the network.
	double dWidth = __dataLimits.getRightX() - __dataLimits.getLeftX();
	double dHeight = __dataLimits.getTopY() - __dataLimits.getBottomY();

	GRLimits setLimits = new GRLimits(__dataLimits);

	// Next, the ratio of the width to the height of the paper will be
	// checked and if the ratio is not sufficient to allow the data
	// to be all printed on the page (no compression!), the data limits
	// will be reset so that there is extra room (off paper) in the drawing area
	if (dWidth >= dHeight) {
		__fitWidth = true;
		__totalDataWidth = dWidth;
		__dataLeftX = __dataLimits.getLeftX();
		// size __totalDataHeight to be proportional to __totalDataWidth, given the screen proportions
		__totalDataHeight = ((double)__totalBufferHeight/(double)__totalBufferWidth)*__totalDataWidth;

		if (__dataLimits.getHeight() > __totalDataHeight) {
			// If the data limits are greater than the total
			// data height that can fit on the paper, then 
			// reset the total data height to be the height of the data limits.  
			__dataBottomY = __dataLimits.getBottomY();
			__totalDataHeight = __dataLimits.getHeight();
		}
		else {
			// Otherwise, if the height of the network can 
			// fit within the paper entirely, center the network vertically on the paper
			double diff = __totalDataHeight - __dataLimits.getHeight();
			__dataBottomY = __dataLimits.getBottomY() - (diff / 2);
			setLimits.setBottomY(__dataBottomY);
			setLimits.setTopY(setLimits.getBottomY() + __totalDataHeight);
		}
	}
	else {
		__fitWidth = false;
		__totalDataHeight = dHeight;
		__dataBottomY = __dataLimits.getBottomY();
		// size __totalDataWidth to be proportional to __totalDataHeight, given the screen proportions		
		__totalDataWidth = ((double)__totalBufferWidth / (double)__totalBufferHeight)* __totalDataHeight;
		if (__dataLimits.getWidth() > __totalDataWidth) {
			// if the data limits are greater than the total
			// data width that can fit on the paper, then 
			// reset the total data width to be the width of the data limits. 
			__dataLeftX = __dataLimits.getLeftX();
			__totalDataWidth = __dataLimits.getWidth();
		}
		else {
			// otherwise, if the width of the network can
			// fit within the paper entirely, center the network
			// horizontally on the paper.  This is when the network is zoomed out.
			double diff = __totalDataWidth - __dataLimits.getWidth();
			__dataLeftX = __dataLimits.getLeftX() - (diff / 2);
			setLimits.setLeftX(__dataLeftX);
			setLimits.setRightX(__dataLeftX + __totalDataWidth);
		}
	}
	__drawingArea.setDataLimits(setLimits);
	// TODO (JTS - 2004-07-13) why are the __dataLimits not reset here??
}

/**
Calculates the data limits necessary to line up the corners of the drawing area with data values.
This ensures that the requested "view window" is drawn with 1:1 aspect for the given media size.
For example, for printing, this ensures that the network is centered and fits on the page.
This method should be called after the drawing area is set in this device (the drawing limits determine the
aspect ratio).
TODO SAM 2011-07-08 This method was adapted from calculateDataLimits() for printing, but may be used
for other drawing modes in the future.
@param drawingLimits the drawing limits of the media (imageable area on paper or screen drawing area).
@param dataLimitsOrig the data limits of the network data, based on the nodes and legend (typically taken from
the XMin,Ymin,Xmax,Ymax coordinates from the network.
@param buffer an additional buffer to be added to the data limits, for example to allow for labels, in data units.
These values will be used to adjust the dataLimits before computations occur.  The buffer should not be confused
with the margin, which is a non-printable area around the edge of the page.  When printing, the margin is
taken out of the page size, resulting in the imageable area.  The buffer is an additional edge inside the
printable area.  The order of the buffer values is left, right, top, bottom.  If the length of the array is 1,
then the same value is added to all sides.
@param setInternalData if true, then internal variables used for rendering are set; if false, the calculations
occur but are not set in the class; therefore false does not interfere with rendering
@return the new data limits considering the media - the data limits will align with the edges of the media.
*/
protected GRLimits calculateDataLimitsForMedia ( GRLimits drawingLimits, GRLimits dataLimitsOrig,
	double [] buffer, boolean setInternalData )
{	// Copy the data limits so as to not interfere with calling code
	GRLimits dataLimits = new GRLimits(dataLimitsOrig);
	// Add to the limits
	if ( buffer != null ) {
		if ( buffer.length == 1 ) {
			dataLimits.setLeftX(dataLimits.getLeftX() - buffer[0]);
			dataLimits.setRightX(dataLimits.getRightX() + buffer[0]);
			dataLimits.setTopY(dataLimits.getTopY() + buffer[0]);
			dataLimits.setBottomY(dataLimits.getBottomY() - buffer[0]);
		}
		else if ( buffer.length == 4 ) {
			dataLimits.setLeftX(dataLimits.getLeftX() - buffer[0]);
			dataLimits.setRightX(dataLimits.getRightX() + buffer[1]);
			dataLimits.setTopY(dataLimits.getTopY() + buffer[2]);
			dataLimits.setBottomY(dataLimits.getBottomY() - buffer[3]);
		}
	}
	GRLimits dataLimitsMedia = new GRLimits (dataLimits); // Copy because only one dimension will be adjusted

	// Next, the ratio of the width to the height of the media is
	// checked and if the ratio is not sufficient to allow the data
	// to be all printed on the page without distortion, the data limits
	// will be reset so that there is extra room in the drawing area for the "skinny" dimension
	boolean fitWidth = false;
	double totalDataWidth = 0.0;
	double totalDataHeight = 0.0;
	double dataLeftX = 0.0;
	double dataBottomY = 0.0;
	if (dataLimits.getWidth() >= dataLimits.getHeight()) {
		fitWidth = true;
		totalDataWidth = dataLimits.getWidth();
		dataLeftX = dataLimits.getLeftX();
		// size __totalDataHeight to be proportional to __totalDataWidth, given the media proportions
		totalDataHeight = ((double)drawingLimits.getHeight()/(double)drawingLimits.getWidth())*totalDataWidth;

		if (dataLimits.getHeight() > totalDataHeight) {
			// If the data limits are greater than the total data height that can fit on the paper, then 
			// reset the total data height to be the height of the data limits.  
			dataBottomY = dataLimits.getBottomY();
			totalDataHeight = dataLimits.getHeight();
		}
		else {
			// Otherwise, if the height of the network can 
			// fit within the paper entirely, center the network vertically on the paper
			double diff = totalDataHeight - dataLimits.getHeight();
			dataBottomY = dataLimits.getBottomY() - (diff / 2);
			dataLimitsMedia.setBottomY(dataBottomY);
			dataLimitsMedia.setTopY(dataLimitsMedia.getBottomY() + totalDataHeight);
		}
	}
	else {
		fitWidth = false;
		totalDataHeight = dataLimits.getHeight();
		dataBottomY = dataLimits.getBottomY();
		// size __totalDataWidth to be proportional to __totalDataHeight, given the screen proportions		
		totalDataWidth = ((double)drawingLimits.getWidth() /
			(double)drawingLimits.getHeight())* totalDataHeight;
		if (dataLimits.getWidth() > totalDataWidth) {
			// If the data limits are greater than the total data width that can fit on the paper, then 
			// reset the total data width to be the width of the data limits. 
			dataLeftX = dataLimits.getLeftX();
			totalDataWidth = dataLimits.getWidth();
		}
		else {
			// otherwise, if the width of the network can fit within the paper entirely, center the network
			// horizontally on the paper.  This is when the network is zoomed out.
			double diff = totalDataWidth - dataLimits.getWidth();
			dataLeftX = dataLimits.getLeftX() - (diff / 2);
			dataLimitsMedia.setLeftX(dataLeftX);
			dataLimitsMedia.setRightX(dataLeftX + totalDataWidth);
		}
	}
	if ( setInternalData ) {
		this.__fitWidth = fitWidth;
		this.__totalDataWidth = totalDataWidth;
		this.__totalDataHeight = totalDataHeight;
		this.__dataLeftX = dataLeftX;
		this.__dataBottomY = dataBottomY;
	}
	return dataLimitsMedia;
}

/**
Calculates the scaled font size that will equal the font size passed in.
This is to handle the discrepancy between how fonts are drawn in points by
the Java code and how they are handled in the network.  The network font sizes
do not match up to point font sizes, because of the need to scale them.
@param name the name of the font to calculate for.
@param style the style of the font to calculate for.
@param size the network size of the font.
@param isPrinting whether the network is currently being printed or not.
@return the equivalent font size in points.
*/
private int calculateScaledFont(String name, String style, int size, boolean isPrinting) {
	double scale = 0;
	if (__fitWidth) {
		double pixels = __dpi * (int)(__pageFormat.getWidth() / 72);
		double pct = (getBounds().width / pixels);
		double width = __totalDataWidth * pct;
		scale = width / __screenDataWidth;
	}
	else {
		double pixels = __dpi * (int)(__pageFormat.getHeight() / 72);
		double pct = (getBounds().height / pixels);
		double height = __totalDataHeight * pct;
		scale = height / __screenDataHeight;
	}
	int fontPixelSize = (int)(size * scale);
	__drawingArea.setFont(name, style, size);
	int fontSize = __drawingArea.calculateFontSize(fontPixelSize);
	if (!isPrinting) {
		return fontSize;
	}
	double temp = (double)fontSize * ((double)__dpi / 72.0);
	temp += 0.5;
	int printFontSize = (int)temp + 2;
	return printFontSize;
}

/**
Checks the id of the specified node to see if it is unique in the network.
If not, the string _X (where X is the number of instances of this node's ID)
will be appended to the node's ID.   The node is guaranteed to have a unique id by the end of this method.
@param pos the position of the node in the node array.
*/
private void checkUniqueID(int pos) {
	int count = 0;
	String id = __nodes[pos].getCommonID();

	for (int i = 0; i < __nodes.length; i++) {
		if (i != pos) {
			if (__nodes[i].getCommonID().equals(id)) {
				count++;
			}
		}
	}

	if (count > 0) {
		__nodes[pos].setCommonID(id + "_" + count);
	}
}

/**
Clears the screen; fills with white.
*/
public void clear() {
	// drawn the area outside the network with grey first
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.gray);
	GRDrawingAreaUtil.fillRectangle(__drawingArea,
		__screenLeftX, __screenBottomY, __screenDataWidth, __screenDataHeight);

	// the network area is filled with white
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.white);
	GRDrawingAreaUtil.fillRectangle(__drawingArea,
		__dataLeftX, __dataBottomY, __totalDataWidth, __totalDataHeight);
}

/**
Clear annotations and redraw.
*/
public void clearAnnotations ()
{
	List<StateMod_Network_AnnotationData> annotationDataList = getAnnotationData();
	int size = annotationDataList.size();
	annotationDataList.clear();
	// Also redraw
	if ( size > 0 ) {
		// Previously had some annotations and now do not so redraw
		forceRepaint();
	}
}

// TODO evaluate how to change the visible screen extent to ensure that the data extent is fully visible.
/**
Center on the given extent.  This code is similar to findNode() except that the data extent is
already found and provides the coordinates.  If the limits do not fit in the viewpoint, zoom out.
If the limits do fit, keep the current zoom to minimize disruption to the user.
@param limits the limits of the data extents to center on.
*/
public void centerOn ( GRLimits limits )
{
	// Point on which to center...
	
	double x = limits.getCenterX();
	double y = limits.getCenterY();
	double buffer = 1.1;
	double width = limits.getWidth()*buffer;
	double width2 = width/2.0;
	double height = limits.getHeight()*buffer;
	double height2 = height/2.0;
	
	// Recompute the view position in data units...
	__screenLeftX = x - (__screenDataWidth / 2);
	__screenBottomY = y - (__screenDataHeight / 2);
	
	// Loop until the network fits (see how this looks visually - maybe cool?)
	while ( ((x - width2) < __screenLeftX) || ((x + width2) > (__screenLeftX + __screenDataWidth)) ||
		((y - height2) < __screenBottomY) || ((y + height2) > (__screenBottomY + __screenDataHeight)) ) {
		forceRepaint();
		zoomOut();
	}
	forceRepaint();
}

/**
Converts a value that runs from 0 to __totalBufferWidth (the number of 
pixels wide the screen buffer is) to a value from 0 to __totalDataWidth.  
Add the value to __dataLeftX to get the data value.
@param x the value to convert to data limits.
@return a value that runs from 0 to __totalDataWidth.
*/
protected double convertAbsX(double x) {
	return (x / __totalBufferWidth) * __totalDataWidth;
}

/**
Converts a value that runs from 0 to __totalBufferHeight (the number of pixels 
high the screen buffer is) to a value from 0 to __totalDataHeight.
Add the value to __dataBottomY to get the data value.
@param y the value to convert to data limits.
@return a value that runs from 0 to __totalDataHeight.
*/
protected double convertAbsY(double y) {
	return (y / __totalBufferHeight) * __totalDataHeight;
}

// TODO SAM 2011-07-08 Shouldn't this be operating on width?
/**
Converts an X value from being scaled for drawing units to be scaled for 
data units.  The X value passed in to this method will run from 0 to 
the number of pixels across the GUI screen.  This is used for scaling fonts and node sizes.
@param x the x value to scale.
@return the x value scaled to fit in the data units.
*/
private double convertDrawingXToDataX(double x)
{
	if ( __printingNetwork ) {
		// In-memory component is not used for drawing
		return x * (__screenDataHeight / getDrawingArea().getDrawingLimits().getHeight());
	}
	else {
		return x * (__screenDataHeight / getBounds().height);
	}
}

//TODO SAM 2011-07-08 Shouldn't this be operating on height?
/**
Converts an Y value from being scaled for drawing units to be scaled for 
data units.  The Y value passed in to this method will run from 0 to 
the number of pixels up the GUI screen.  This is used for scaling fonts and node sizes.
@param y the y value to scale.
@return the y value scaled to fit in the data units.
*/
private double convertDrawingYToDataY(double y) {
	if ( __printingNetwork ) {
		// In-memory component is not used for drawing
		return y * (__screenDataWidth / getDrawingArea().getDrawingLimits().getWidth());
	}
	else {
		return y * (__screenDataWidth / getBounds().width);
	}
}

/**
Create a drawing area representing the full-scale rendering of the network.  This is used to calculate the
scale when rendering occurs for different media than the layout, or a part of the full layout.
@param network the StateMod network that is being rendered
@param pageLayoutPropList page layout properties
@return the drawing area that is configured for full scale rendering - the drawing and data limits from the
drawing area can be used to compute the scale when compared to the current rendering drawing area
*/
private GRJComponentDrawingArea createDrawingAreaFullScale ( StateMod_NodeNetwork network,
	PropList pageLayoutPropList )
{
	// Determine the full drawing limits for the page layout media (portrait mode).
	String paperSize = pageLayoutPropList.getValue("PaperSize");
	PageFormat pageFormat = PrintUtil.getPageFormat(paperSize); // This is portrait
	// Initialize a device using the full page size (margins=0).  Setting the limits will not actually
	// size the JComponent, just keep the limits in memory for calculations
	GRLimits deviceLimits = new GRLimits ( 0, 0, pageFormat.getWidth(), pageFormat.getHeight() );
	if ( pageLayoutPropList.getValue("PageOrientation").equalsIgnoreCase("Landscape")) {
		// Swap the dimensions
		deviceLimits = new GRLimits ( 0, 0, pageFormat.getHeight(), pageFormat.getWidth() );
	}
	GRJComponentDevice dev = new GRJComponentDevice("Virtual device for full scale limits.");
	dev.setLimits(deviceLimits);
	// Initialize the drawing area using the full page but subtract the margin from the imageable area
	// TODO SAM 2011-07-09 does the actual margin need to be used?  For example, when printing
	// the user may specify a different margin than the defaults - for now it is a minor error
	double margin = getMargin();
	GRLimits drawingLimits = new GRLimits (
		deviceLimits.getLeftX() + margin, deviceLimits.getBottomY() + margin,
		deviceLimits.getRightX() - margin, deviceLimits.getTopY() - margin );
	// Determine the data limits for the media
	GRLimits dataLimitsOrig = new GRLimits(network.getLX(),network.getBY(),network.getRX(),network.getTY());
	double[] buffer = network.getEdgeBuffer();
	// Calculate the data limits required to fill the media
	GRLimits dataLimits = calculateDataLimitsForMedia(drawingLimits, dataLimitsOrig, buffer, false );
	// Create a drawing area that matches the drawing (media) and data (fill media at full scale) limits
	GRJComponentDrawingArea drawingAreaFullScale = new GRJComponentDrawingArea(
		this, "StateMod_Network DrawingArea full scale", GRAspectType.TRUE, 
		drawingLimits, GRUnits.DEVICE, GRLimits.DEVICE, dataLimits );
	return drawingAreaFullScale;
}

/**
Creates a node change operation (for undo/redo) that encapsulates a move 
in which more than one node was dragged around the network.
*/
private void createMultiNodeChangeOperation() {
	double mainX = __mouseDataX - __xAdjust;
	double mainY = __mouseDataY - __yAdjust;
	double dx = mainX - __mouseDownX;
	double dy = mainY - __mouseDownY;

	StateMod_Network_UndoData data = new StateMod_Network_UndoData();
	data.nodeNum = __clickedNodeNum;
	data.oldX = __nodes[__clickedNodeNum].getX();
	data.oldY = __nodes[__clickedNodeNum].getY();
	data.newX = __mouseDataX;
	data.newY = __mouseDataY;

	data.otherNodes = new int[__draggedNodes.length];
	data.oldXs = new double[__draggedNodes.length];
	data.oldYs = new double[__draggedNodes.length];
	data.newXs = new double[__draggedNodes.length];
	data.newYs = new double[__draggedNodes.length];
	
	for (int i = 0; i < __draggedNodes.length; i++) {
		data.otherNodes[i] = __draggedNodes[i];
		data.oldXs[i] = __nodes[__draggedNodes[i]].getX();
		data.oldYs[i] = __nodes[__draggedNodes[i]].getY();
		data.newXs[i] = data.oldXs[i] + dx;
		data.newYs[i] = data.oldYs[i] + dy;
	}

	addNodeChangeOperation(data);
}

/**
Deletes a link from a node.  If the node has multiple links, they are
displayed in a dialog and the user can choose the one to delete.
*/
private void deleteLink() {
	if (__links == null) {
		return;
	}

	int size = __links.size();
	PropList p = null;
	String from = null;
	String id = __nodes[__popupNodeNum].getCommonID();
	String to = null;
	List<String> links = new Vector<String>();
	List<Integer> nums = new Vector<Integer>();	

	// Gather all the links in the network that reference the node that the popup menu was opened in.
	for (int i = 0; i < size; i++) {
		p = __links.get(i);
		from = p.getValue("FromNodeID");
		to = p.getValue("ToNodeID");
		if (from.equals(id) || to.equals(id)) {
			links.add("" + from + " -> " + to);
			nums.add(Integer.valueOf(i));
		}
	}

	size = links.size();
	if (size == 1) {
		// If there is only one link involving the clicked-on node, then delete it outright.
		int i = nums.get(0).intValue();
		__links.remove(i);
		setNetworkChanged (true);
		forceRepaint();
		return;
	}

	// Prompt the user for the link to delete
	JComboBoxResponseJDialog d = new JComboBoxResponseJDialog(__parent,	
		"Select the link to delete", "Select the link to delete", links,
		ResponseJDialog.OK | ResponseJDialog.CANCEL);
		
	String s = d.response();
	if (s == null) {	
		// If no node was selected, don't delete a node.
		return;
	}

	// Find the node to delete and delete it
	String link = null;
	for (int i = 0; i < size; i++) {
		link = links.get(i);
		if (link.equals(s)) {
			int j = nums.get(i).intValue();
			__links.remove(j);
			forceRepaint();
			setNetworkChanged (true);
			return;
		}
	}			
}

/**
Deletes a node from the network and rebuilds the network connectivity.
@param id the id of the node to delete.
*/
public void deleteNode(String id) {
	removeIDFromLinks(id);
	__network.deleteNode(id);
	buildNodeArray();
	findMaxReachLevel();
	forceRepaint();	
	__undoOperations = new Vector<StateMod_Network_UndoData>();
	__parent.setUndo(false);
	__parent.setRedo(false);
}

/**
Draws annotations on the network.  These are the simple line annotations.  There are also annotations for
more complex StateMod data, drawn by the annotation renderers.
*/
private void drawAnnotations() {
	// If annotations were read from an XML file, then they will
	// need an initial process to fill out the bounding box data
	// in the node objects that hold each one
	if (!__annotationsProcessed && __processAnnotations) {
		processAnnotationsFromNetwork();
		__annotationsProcessed = true;
	}
	
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
	Rectangle bounds = null;
	if ( __printingNetwork && !__useOldPrinting) {
		bounds = new Rectangle((int)this.getLimits().getWidth(), (int)this.getLimits().getHeight() );
	}
	else {
		bounds = getBounds();
	}
	double scale = 0;
	if (__fitWidth) {
		double pixels = __dpi * (int)(__pageFormat.getWidth() / 72);
		double pct = (bounds.width / pixels);
		double width = __totalDataWidth * pct;
		scale = width / __screenDataWidth;
	}
	else {
		double pixels = __dpi * (int)(__pageFormat.getHeight() / 72);
		double pct = (bounds.height / pixels);
		double height = __totalDataHeight * pct;
		scale = height / __screenDataHeight;
	}
	if (__printingNetwork) {
		if ( !__useOldPrinting ) {
			scale = getScale();
		}
	}

	double fontSize = -1;
	double temp = -1;
	HydrologyNode node = null;
	int fontPixelSize = -1;
	int origFontSizeInt = -1;
	int printFontSize = -1;
	int size = __annotations.size();
	PropList p = null;
	String origFontSize = null;
	for (int i = 0; i < size; i++) {
		if (i == __clickedNodeNum && __isLastSelectedAnAnnotation) {
			// skip it -- outline being drawn for drag
			continue;
		}
		node = __annotations.get(i);
		p = (PropList)node.getAssociatedObject();
		String fname = p.getValue("FontName");
		String style = p.getValue("FontStyle");
		GRDrawingAreaUtil.setFont(__drawingArea, fname, style, 7);
		origFontSize = p.getValue("OriginalFontSize");
		origFontSizeInt = Integer.decode(origFontSize).intValue();
		fontPixelSize = (int)(origFontSizeInt * scale);
		fontSize =__drawingArea.calculateFontSize(fontPixelSize);
		if (__printingNetwork) {
			if ( __useOldPrinting ) {
				temp = (double)fontSize * ((double)__dpi / 72.0);
				temp += 0.5;
				printFontSize = (int)temp + 2;
			}
			else {
				// For now do not scale
				printFontSize = (int)(origFontSizeInt*scale);
			}
			p.set("FontSize", "" + printFontSize);
		}
		else {
			p.set("FontSize", "" + (int)fontSize);
		}
		p.set("DrawOutOfBounds", "true");
		node.setAssociatedObject(p);

		GRDrawingAreaUtil.drawAnnotation(__drawingArea, (PropList)node.getAssociatedObject());

		if (__showAnnotationBoundingBox) {
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.red);
			GRDrawingAreaUtil.drawRectangle(__drawingArea,
				node.getX(), node.getY(), node.getWidth(), node.getHeight());
		}
	}
}

/**
Draws the outline of a node being dragged.
@param num the index of the node being dragged in the __nodes array.
*/
private void drawDraggedNodeOutline(int num) {
	double mainX = __mouseDataX - __xAdjust;
	double mainY = __mouseDataY - __yAdjust;

	double dx = mainX - __mouseDownX;
	double dy = mainY - __mouseDownY;

	double nodeX = __draggedNodesXs[num] + dx;
	double nodeY = __draggedNodesYs[num] + dy;

	double w = __dragNodesLimits[num].getWidth();
	double h = __dragNodesLimits[num].getHeight();

	GRDrawingAreaUtil.drawLine(__drawingArea, nodeX, nodeY, nodeX + w, nodeY);
	GRDrawingAreaUtil.drawLine(__drawingArea, nodeX, nodeY + h, nodeX + w, nodeY + h);
	GRDrawingAreaUtil.drawLine(__drawingArea, nodeX, nodeY, nodeX, nodeY + h);
	GRDrawingAreaUtil.drawLine(__drawingArea, nodeX + w, nodeY, nodeX + w, nodeY + h);
}

/**
Draws the legend.  A single node is created, its properties are set, and then it is drawn
directly on the network.
*/
private void drawLegend() {
	/*
	Legends look generally like this, where the left column has normal nodes and
	the right column has nodes decorated to indicate natural flow,
	which are typically the same as the normal node but with an extra outer outline.

	+-----------------------------------------------------+
	|   Legend                                            |
	+-----------------------------------------------------+
	|                                                     |
	|   [Icon]  Node Text         [Icon] Node Text        |
	|                                                     |
    |                  .............................      |
	|                                                     |
	|   [Icon]  Node Text         [Icon] Node Text        |
	+-----------------------------------------------------+
	*/

	GRLimits limits = null;
	
	int by = 0;
	int col2x = 0; // the X value of the point where the second column begins
	int dividerY = 0; // the Y value of the line dividing the legend title from the rest
	int height = 0; // running total of the height of the legend
	int tempW = 0;
	int lx = 0;

	int width = 0;

	////////////////////////////////////////////////////////////////
	// Spacing parameters
	////////////////////////////////////////////////////////////////
	double colsp = convertDrawingXToDataX(15); // spacing between columns 1 and 2
	double edge = convertDrawingXToDataX(2); // spacing between the edge lines and the interior boundary line width
	int line = 1;
	double tsp = convertDrawingXToDataX(4); // spacing between nodes and node text
	double vsp = convertDrawingYToDataY(4); // vertical spacing between lines
	
	String text = "Legend";
	limits = GRDrawingAreaUtil.getTextExtents(__drawingArea, text, GRUnits.DATA);
	
	// Add the height of the text, plus space around the text before the boundary lines
	height += (int)limits.getHeight() + (edge * 2);
	// Add the height of the line that divides the title from the rest
	height += line;
	// Hold this point for calculating the exact divider Y point later
	dividerY = height;

	double id = 0; // Inside diameter
	double bd = 0; 
	if (__fitWidth) {
		id = convertDrawingXToDataX(__legendNodeDiameter);
		int third = (int)(__legendNodeDiameter / 3);
		if ((third % 2) == 1) {
			third++;
		}
		bd = convertDrawingXToDataX(third);

		if (id < limits.getHeight()) {
			id = limits.getHeight() * 1.2;
		}
	}
	else {
		// Get the icon size from the first node (there will always be an end node)
		id = convertDrawingYToDataY(__nodes[0].getIconDiameter());
		int third = (int)(__legendNodeDiameter / 3);
		if ((third % 2) == 1) {
			third++;
		}		
		bd = convertDrawingYToDataY(third);

		if (id < limits.getHeight()) {
			id = limits.getHeight() * 1.2;
		}		
	}

	// Add enough room to accommodate the number of node icons vertically, plus vertical
	// space between them, plus space around them between the lines.
	int numIcons = 10;
	height += (numIcons * (id + bd + vsp)) + (edge * 2) - vsp;

	// Determine the width of the legend by taking the widths of the
	// two longest node labels and using them to determine the width required 
	text = "Most Downstream Node";
	limits = GRDrawingAreaUtil.getTextExtents(__drawingArea, text, GRUnits.DATA);
	tempW = (int)limits.getWidth();

	// Store the X point at which the second column begins
	col2x = (int)(edge + id + bd
		+ tsp + tempW + colsp 
		+ ((id + bd)/2));

	text = "Instream (Minimum) Flow / Natural Flow";
	limits = GRDrawingAreaUtil.getTextExtents(__drawingArea, text, GRUnits.DATA);

	tempW = (int)limits.getWidth();
	
	// calculate the overall width of the legend
	width = (int)(col2x + ((id + bd)/ 2) + tsp + tempW + edge);
	
	// the legend's limits will be not null if the legend has already been drawn once.
	if (__legendLimitsDetermined) {
		lx = (int)__legendDataLimits.getLeftX();
		by = (int)__legendDataLimits.getBottomY();
	}
	// they will be null if the legend has not been drawn yet, or if the paper size has changed
	else {
/*	
		lx = (int)convertX((int)(__pageFormat.getImageableX() / __printScale)) + 50;	
		by = (int)convertY((int)((__pageFormat.getHeight() 
			- (__pageFormat.getImageableHeight() + __pageFormat.getImageableY())) / __printScale)) + 50;
*/		
		if (__network.isLegendPositionSet()) {
			lx = (int)__network.getLegendX();
			by = (int)__network.getLegendY();
		}
		else {
			lx = (int)(__dataLimits.getLeftX() + (0.05 * __dataLimits.getWidth()));
			by = (int)(__dataLimits.getBottomY() + (0.05 * __dataLimits.getHeight()));
		}
		__legendLimitsDetermined = true;
	}	
	__legendDataLimits = new GRLimits(lx, by, lx + width, by + height);

	// block out with white the area over which the legend will be drawn.
	// This means no nodes can be drawn over the top of the legend.
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.white);
	GRDrawingAreaUtil.fillRectangle(__drawingArea, lx, by, width, height);

	// determine the absolute point of the divider line.
	dividerY = by + height - dividerY;

	// move the second column X position to take into account the new left X
	col2x += lx;
	// determine the point at which the first column will go.
	int col1x = (int)(lx + edge + ((id + bd)/2));

	// NOTE:
	// since the nodes are drawn centered on points, notice that both the
	// first and second column X values are for the center of the node.

	// draw the outline of the legend
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, lx + width, by);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by + height, lx + width, by + height);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, lx, by + height);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx + width, by, lx + width, by + height);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, dividerY, lx + width, dividerY);

	// determine the amounts to increment X and Y values after drawing nodes and rows of nodes.
	int yInc = (int)((id + bd) + vsp);
	int xInc = (int)((id + bd) / 2 + tsp);
	
	// determine the X positions for drawing the first and second column's text
	int text1x = col1x + xInc;
	int text2x = col2x + xInc;

	// draw the title
	GRDrawingAreaUtil.drawText(__drawingArea, "Legend", col1x, dividerY + edge, 0, 0);

	// draw the nodes.  
	// For the most part, if there are two columns of nodes, the second
	// column is the same type as the first but displays the natural flow representation, with
	// " / Natural Flow" appended to the node label.  
	// So between drawing column 1 and column 2, just change the X 
	// value and set isNaturalFlow to true.  When moving down a row to a
	// new node type, call resetNode() to reset node properties.
	// The node graphic is drawn using the single node and its properties.
	// The text for node labels is drawn separate from the nodes, not as the standard node label.

	// End of network
	HydrologyNode node = new HydrologyNode();
	node.setIconDiameter(__nodes[0].getIconDiameter());
	node.setDecoratorDiameter(__nodes[0].getDecoratorDiameter());
	// FIXME SAM 2003-08-15 Need to remove WIS code
	//node.setInWis(false);
	node.setX(col1x);
	node.setY(dividerY - edge -((id + bd)/2));
	node.setType(HydrologyNode.NODE_TYPE_END);
	node.draw(__drawingArea);
	text = "Most Downstream Node";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);

	// Diversion
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_DIV, false, false );
	node.draw(__drawingArea);
	text = "Diversion";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_DIV, true, false );
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Diversion + Wells
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_DIV_AND_WELL, false, false);
	node.draw(__drawingArea);
	text = "Diversion + Well(s)";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_DIV_AND_WELL, true, false);
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Instream flow
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_ISF, false, false);
	node.draw(__drawingArea);
	text = "Instream (Minimum) Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_ISF, true, false);
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Other
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_OTHER, false, false );
	node.draw(__drawingArea);
	text = "Other";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_OTHER, true, false );
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Plan
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_PLAN, false, false);
	node.draw(__drawingArea);
	text = "Plan";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_PLAN, true, false);
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Reservoir
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_RES, false, false);
	node.draw(__drawingArea);
	text = "Reservoir";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_RES, true, false);
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Streamflow
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_FLOW, false, false );
	node.draw(__drawingArea);
	text = "Streamflow Gage";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_FLOW, true, false );
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Well
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_WELL, false, false );
	node.draw(__drawingArea);
	text = "Well(s)";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_WELL, true, false );
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	
	// Other
	node.setY(node.getY() - yInc);
	node.setX(col1x);
	node.resetNode(HydrologyNode.NODE_TYPE_OTHER, false, true );
	node.draw(__drawingArea);
	text = "Import Indicator";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text1x, node.getY(), 0, GRText.CENTER_Y);
	/* TODO SAM 2008-12-09 - export
	node.setX(col2x);
	node.resetNode(HydrologyNode.NODE_TYPE_OTHER, true, false );
	node.draw(__drawingArea);
	text += " / Natural Flow";
	GRDrawingAreaUtil.drawText(__drawingArea, text, text2x, node.getY(), 0, GRText.CENTER_Y);
	*/
}

/**
Draws links between nodes.
TODO (JTS - 2004-07-13) This would be faster if the nodes were not looked up every time.  Store the
position of the nodes within the node array.  The positions would have to be
recomputed every time a node is added or deleted.
*/
private void drawLinks ( GRJComponentDrawingArea drawingArea, List<PropList> links, HydrologyNode [] nodes,
	float [] dashes )
{
	if (links == null) {
		return;
	}
	float offset = 0;
	HydrologyNode node1 = null;
	HydrologyNode node2 = null;
	String fromNodeId = null;
	String toNodeId = null;
	String propValue = null;
	double [] dashPattern = new double[dashes.length];
	for ( int i = 0; i < dashes.length; i++ ) {
		dashPattern[i] = dashes[i];
	}
	double [] linePattern = null;
	GRLineStyleType lineStyle = GRLineStyleType.DASHED; // Default
	GRArrowStyleType fromArrowStyle = GRArrowStyleType.NONE;
	GRArrowStyleType toArrowStyle = GRArrowStyleType.NONE;
	double arrowWidth = 0; // Arrow head width and length
	double arrowLength = 0;
	for ( PropList p : links ) {
		fromNodeId = p.getValue("FromNodeID");
		toNodeId = p.getValue("ToNodeID");
		propValue = p.getValue("LineStyle");
		lineStyle = GRLineStyleType.valueOfIgnoreCase(propValue);
		if ( lineStyle == null ) {
			lineStyle = GRLineStyleType.DASHED;
			linePattern = dashPattern;
		}
		propValue = p.getValue("FromArrowStyle");
		fromArrowStyle = GRArrowStyleType.valueOfIgnoreCase(propValue);
		if ( fromArrowStyle == null ) {
			fromArrowStyle = GRArrowStyleType.NONE;
		}
		propValue = p.getValue("ToArrowStyle");
		toArrowStyle = GRArrowStyleType.valueOfIgnoreCase(propValue);
		if ( toArrowStyle == null ) {
			toArrowStyle = GRArrowStyleType.NONE;
		}
		node1 = null;
		node2 = null;
		for (int j = 0; j < nodes.length; j++) {
			if (nodes[j].getCommonID().equals(fromNodeId)) {
				node1 = nodes[j];
			}
			if (nodes[j].getCommonID().equals(toNodeId)) {
				node2 = nodes[j];
			}
			if (node1 != null && node2 != null) {
				j = nodes.length + 1;
			}
		}
		if ( lineStyle == GRLineStyleType.DASHED ) {
			// Set line style to dashes
			linePattern = dashPattern;
		}
		else {
			// Draw line solid
			linePattern = null;
		}
		drawingArea.setLineDash(linePattern, offset);
		if ( (fromArrowStyle == GRArrowStyleType.NONE) && (toArrowStyle == GRArrowStyleType.NONE) ) {
			// Connect nodes with a line...
			GRDrawingAreaUtil.drawLine(drawingArea, 
				node1.getX(), node1.getY(), node2.getX(), node2.getY());
		}
		else {
			// Connect nodes with an arrow.  Because the arrow may obscure or be obscured by the
			// node symbol, back off the length of the line to ensure that the arrow will only point
			// to the symbol.  Make the size of the arrow the length of the symbol but only 2/3 the width
			double x1 = node1.getX();
			double y1 = node1.getY();
			double x2 = node2.getX();
			double y2 = node2.getY();
			double diamData = 0.0; // Diameter if node icon in data units
			if ( fromArrowStyle != GRArrowStyleType.NONE ) {
				// Adjust the "from" node coordinates.
				double diamDrawing = node1.getIconDiameter(); // Drawing units
				diamData = GRDrawingAreaUtil.getDataExtents(drawingArea,
					new GRLimits(0,0,diamDrawing,diamDrawing), 0).getWidth();
				double xDiff = node2.getX() - node1.getX();
				double yDiff = node2.getY() - node1.getY();
				double lineLength = Math.sqrt(xDiff+xDiff + yDiff*yDiff);
				double xAdjust = MathUtil.interpolate(diamData/2, 0, lineLength, 0, Math.abs(xDiff));
				double yAdjust = MathUtil.interpolate(diamData/2, 0, lineLength, 0, Math.abs(yDiff));
				if ( xDiff >= 0 ) {
					x1 += xAdjust;
				}
				else {
					x1 -= xAdjust;
				}
				if ( yDiff >= 0 ) {
					y1 += yAdjust;
				}
				else {
					y1 -= yAdjust;
				}
			}
			if ( toArrowStyle != GRArrowStyleType.NONE ) {
				// Adjust the "to" node coordinates.
				double diamDrawing = node2.getIconDiameter(); // Drawing units
				diamData = GRDrawingAreaUtil.getDataExtents(drawingArea,
					new GRLimits(0,0,diamDrawing,diamDrawing), 0).getWidth();
				double xDiff = node1.getX() - node2.getX();
				double yDiff = node1.getY() - node2.getY();
				double lineLength = Math.sqrt(xDiff+xDiff + yDiff*yDiff);
				double xAdjust = MathUtil.interpolate(diamData/2, 0, lineLength, 0, Math.abs(xDiff));
				double yAdjust = MathUtil.interpolate(diamData/2, 0, lineLength, 0, Math.abs(yDiff));
				if ( xDiff >= 0 ) {
					x2 += xAdjust;
				}
				else {
					x2 -= xAdjust;
				}
				if ( yDiff >= 0 ) {
					y2 += yAdjust;
				}
				else {
					y2 -= yAdjust;
				}
			}
			arrowLength = diamData;
			arrowWidth = diamData/2;
			// Exaggerate to troubleshoot
			//arrowLength = diamData*2;
			//arrowWidth = diamData;
			GRDrawingAreaUtil.drawArrow(drawingArea, 
				x1, y1, x2, y2,
				lineStyle, linePattern, 0.0, // Line properties
				fromArrowStyle, toArrowStyle, arrowWidth, arrowLength ); // Arrow properties
		}
	}
	// Set back to solid line for drawing area
	drawingArea.setFloatLineDash(null, (float)0);
}

/**
Draws the lines between all the nodes.
*/
private void drawNetworkLines()
{
	boolean dots = false;
	float offset = 0;
	double[] x = new double[2];
	double[] y = new double[2];
	HydrologyNode ds = null;
	HydrologyNode dsRealNode = null;
	HydrologyNode holdNode = null;
	HydrologyNode holdNode2 = null;
	HydrologyNode node = null;
	HydrologyNode nodeTop = __network.getMostUpstreamNode();
	
	GRDrawingAreaUtil.setLineWidth(__drawingArea, __lineThickness);

	float[] tempDots = new float[2];
	if (__lineThickness >= 1) {
		tempDots[0] = (float)(__dots[0] * __lineThickness);
		tempDots[1] = (float)(__dots[1] * __lineThickness);
	}
	else {
		tempDots[0] = (float)(__dots[0]);
		tempDots[1] = (float)(__dots[1]);
	}

	for (node = nodeTop; node != null; 
	    node = StateMod_NodeNetwork.getDownstreamNode(
	    node, StateMod_NodeNetwork.POSITION_COMPUTATIONAL)) {
	    // Move ahead and skip and blank or unknown nodes (which won't
		// be drawn, anyways -- check buildNodeArray()), so that 
		// connections are only between visible nodes
		if (holdNode == node) {
			GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
			return;
		}
		holdNode2 = node;
		while (node.getType() == HydrologyNode.NODE_TYPE_UNKNOWN) {
			node = StateMod_NodeNetwork.getDownstreamNode(node, 
				StateMod_NodeNetwork.POSITION_COMPUTATIONAL);
			if (node == null || node == holdNode2) {
				GRDrawingAreaUtil.setLineWidth(__drawingArea,1);
				return;
			}
		}
		
		ds = node.getDownstreamNode();
		if (ds == null || node.getType() == HydrologyNode.NODE_TYPE_END) {
			GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
			return;
		}

		dsRealNode = StateMod_NodeNetwork.getDownstreamNode(node, 
			StateMod_NodeNetwork.POSITION_COMPUTATIONAL);

		dots = false;
		if (dsRealNode != null && dsRealNode.getType() == HydrologyNode.NODE_TYPE_XCONFLUENCE) {
			dots = true;
		}

	   	// Move ahead and skip and blank or unknown nodes (which won't
		// be drawn, anyways -- check buildNodeArray()), so that 
		// connections are only between visible nodes
		holdNode2 = ds;
		while (ds.getType() == HydrologyNode.NODE_TYPE_UNKNOWN) {
	    	ds = ds.getDownstreamNode();
			if (ds == null || ds == holdNode2) {
				GRDrawingAreaUtil.setLineWidth(__drawingArea,1);
				return;
			}
		}

		x[0] = node.getX();
		y[0] = node.getY();
		x[1] = ds.getX();
		y[1] = ds.getY();		
	
		if (__shadedRivers) {
			// Show a shaded wide line for the rivers
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.gray);
			GRDrawingAreaUtil.setLineWidth(__drawingArea,
				(double)(__maxReachLevel - node.getReachLevel() + 1) * __lineThickness);
			//Message.printStatus(1, "", "MaxReachLevel: " + __maxReachLevel
			//	+ "   NodeReachLevel: " + node.getReachLevel() + ", line width multiplier = " 
			//	+ ((__maxReachLevel - node.getReachLevel() + 1)) );
			GRDrawingAreaUtil.drawLine(__drawingArea, x, y);
		}

		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		GRDrawingAreaUtil.setLineWidth(__drawingArea, __lineThickness);
		
		if (dots) {
			__drawingArea.setFloatLineDash(tempDots, offset);
			GRDrawingAreaUtil.drawLine(__drawingArea, x, y);
			__drawingArea.setFloatLineDash(null, (float)0);
		}
		else {
			//Message.printStatus(2,"drawNodes","Drawing lines between nodes " + x[0] + "," + y[0] + " to " +
			//	x[1] + "," + y[1] );
			GRDrawingAreaUtil.drawLine(__drawingArea, x, y);
		}
		holdNode = node;
	}
	GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
}		

/**
Draws the nodes on the screen.
*/
private void drawNodes( GRJComponentDrawingArea drawingArea, HydrologyNode [] nodes ) {
	for (int i = 0; i < nodes.length; i++) {
		nodes[i].draw(drawingArea);
	}
}

/**
Draws the outline of a dragged node on the screen while it is being dragged.
@param g the Graphics context to use for dragging the node.
*/
@SuppressWarnings("deprecation")
private void drawNodesOutlines(Graphics g) {
	// Force the graphics context to be the on-screen one, not the double-buffered one
	forceGraphics(g);
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);

	GRDrawingAreaUtil.drawLine(__drawingArea, __mouseDataX - __xAdjust, __mouseDataY - __yAdjust, 
		__draggedNodeLimits.getWidth() + __mouseDataX - __xAdjust, __mouseDataY - __yAdjust);
	
	GRDrawingAreaUtil.drawLine(__drawingArea, __mouseDataX - __xAdjust, 
		__mouseDataY + __draggedNodeLimits.getHeight() - __yAdjust,
		__draggedNodeLimits.getWidth() + __mouseDataX - __xAdjust,
		__mouseDataY + __draggedNodeLimits.getHeight() - __yAdjust);
	GRDrawingAreaUtil.drawLine(__drawingArea, __mouseDataX - __xAdjust, __mouseDataY - __yAdjust,
		__mouseDataX - __xAdjust, __mouseDataY + __draggedNodeLimits.getHeight() - __yAdjust);
	GRDrawingAreaUtil.drawLine(__drawingArea,
		__mouseDataX + __draggedNodeLimits.getWidth() - __xAdjust,	
		__mouseDataY - __yAdjust, 
		__mouseDataX + __draggedNodeLimits.getWidth() - __xAdjust,
		__mouseDataY + __draggedNodeLimits.getHeight() - __yAdjust);
	
	for (int i = 0; i < __draggedNodes.length; i++) {
		drawDraggedNodeOutline(i);
	}
}

/**
Called when a user presses OK on an Add Node dialog.
*/
protected void endAddNode() {
	setNetworkChanged (true);
	forceRepaint();
}

/**
Finds the highest reach level in the entire network.  This is used for shading rivers.
@return the highest reach level in the entire network.
*/
private int findMaxReachLevel() {
	int reach = 0;
	for (int i = 0; i < __nodes.length; i++) {
		if (__nodes[i].getReachLevel() > reach) {
			reach = __nodes[i].getReachLevel();
		}
	}
	return reach;
}

/**
Determines the nearest grid point in data units relative to the data unit points passed in.
@param px an X point, in data units
@param py a Y point, in data units
@return a two-element integer array.  Element one stores the X location of
the nearest grid point and element two stores the Y location.  Both are in data units.
*/
private double[] findNearestGridXY(int px, int py) {
	double[] p = new double[2];

	// determine the amount that the point is away from a grid location
	double lx = Math.abs(__dataLeftX) % __gridStep;
	double by = Math.abs(__dataBottomY) % __gridStep;
	
	double posX = (px % __gridStep) - lx;
	double posY = (py % __gridStep) - by;
	
	// then determine which gridstep it is closer to -- for X, the left one or the right one
	if (posX > (__gridStep / 2)) {
		p[0] = (__gridStep - posX);
	}
	else {
		p[0] = -1 * posX;
	}

	// for Y the top one or the bottom one
	if (posY > (__gridStep / 2)) {
		p[1] = (__gridStep - posY);
	}
	else {
		p[1] = -1 * posY;
	}
	return p;
}

/**
Determines the nearest grid point in data units relative to the point stored in the MouseEvent.
@param event the MouseEvent for which to find the nearest grid point.
@return a two-element integer array.  Element one stores the X location of
the nearest grid point and element two stores the Y location.  Both are in data units.
*/
private double[] findNearestGridXY(MouseEvent event) {
	double[] p = new double[2];

	// Turn both the X and Y mouse locations into data units
	double x = convertDrawingXToDataX(event.getX()) + __screenLeftX;
	double y = (int)convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;

	// Determine how far off the mouse position is from a grid step

	double lx = 0;
	if (__dataLeftX < 0) {
		lx = Math.abs(__dataLeftX) % __gridStep;
	}
	else {
		lx = -1 * (__dataLeftX % __gridStep);
	}
	double by = 0;
	if (__dataBottomY < 0) {
		by = Math.abs(__dataBottomY) % __gridStep;
	}
	else {
		by = -1 * (__dataBottomY % __gridStep);
	}
	
	double posX = (x % __gridStep) + lx;
	double posY = (y % __gridStep) + by;
	// Determine which gridstep it is closer to -- for X, the left one or the right one
	if (posX > (__gridStep / 2)) {
		p[0] = x + (__gridStep - posX);
	}
	else {
		p[0] = x - posX;
	}

	// For Y the top one or the bottom one
	if (posY > (__gridStep / 2)) {
		p[1] = y + (__gridStep - posY);
	}
	else {
		p[1] = y - posY;

	}

	return p;
}

/**
Determines the nearest grid point in data units relative to the point stored in the MouseEvent.
@param event the MouseEvent for which to find the nearest grid point.
@return a two-element integer array.  Element one stores the X location of
the nearest grid point and element two stores the Y location.  Both are in data units.
*/
private double[] findNearestGridXY(double x, double y) {
	double[] p = new double[2];

	// determine how far off the mouse position is from a grid step

	double lx = 0;
	if (__dataLeftX < 0) {
		lx = Math.abs(__dataLeftX) % __gridStep;
	}
	else {
		lx = -1 * (__dataLeftX % __gridStep);
	}
	double by = 0;
	if (__dataBottomY < 0) {
		by = Math.abs(__dataBottomY) % __gridStep;
	}
	else {
		by = -1 * (__dataBottomY % __gridStep);
	}
	
	double posX = (x % __gridStep) + lx;
	double posY = (y % __gridStep) + by;
	// Determine which gridstep it is closer to -- for X, the left one or the right one
	if (posX > (__gridStep / 2)) {
		p[0] = x + (__gridStep - posX);
	}
	else {
		p[0] = x - posX;
	}

	// For Y the top one or the bottom one
	if (posY > (__gridStep / 2)) {
		p[1] = y + (__gridStep - posY);
	}
	else {
		p[1] = y - posY;

	}

	return p;
}

/**
Displays a dialog containing all the annotations on the network; the user can 
select one and the annotation will be highlighted and zoomed to.
*/
public HydrologyNode findAnnotation()
{
	// Compile a list of all the annotations in the network
	List<String> annotationListTextSorted = new Vector<String>();
	PropList props = null;
	String propValue;
	for ( HydrologyNode annotation: getNetwork().getAnnotationList() ) {
		props = (PropList)annotation.getAssociatedObject();
		if ( props != null ) {
			propValue = props.getValue("Text");
			if ( propValue != null ) {
				annotationListTextSorted.add(propValue);
			}
		}
	}

	// Sort to ascending String order
	Collections.sort(annotationListTextSorted, String.CASE_INSENSITIVE_ORDER);	

	// Display a dialog from which the user can choose the annotation to find
	JComboBoxResponseJDialog j = new JComboBoxResponseJDialog(__parent,
		"Select the Annotation to Find", "Select the network annotation to find", annotationListTextSorted,
		ResponseJDialog.OK | ResponseJDialog.CANCEL, true);
		
	String s = j.response();
	if (s == null) {
		// If s is null, then the user pressed CANCEL
		return null;
	}

	// Find the annotation in the network and center the display window around it.
	HydrologyNode foundAnnotation = findAnnotation ( s, true, true );

	if ( foundAnnotation == null ) {
		new ResponseJDialog(__parent, "Annotation '" + s + "' not found",
			"The annotation with text '" + s + "' could not be found.\n"
			+ "The annotation text must match exactly, including case\n"
			+ "sensitivity.", ResponseJDialog.OK).response();
		return null;
	}

	return foundAnnotation;
}

/**
Find an annotation given its common identifier.
@param changeSelection if true, then change the selection of the annotation as the identifier is checked
@param center if true, center on the found node; if false do not change position
@return the found node, or null if not found
*/
public HydrologyNode findAnnotation ( String s, boolean changeSelection, boolean center )
{
	double x = 0;
	double y = 0;
	HydrologyNode foundAnnotation = null;
	PropList props = null;
	String propValue = null;
	for ( HydrologyNode annotation: getNetwork().getAnnotationList() ) {
		props = (PropList)annotation.getAssociatedObject();
		if ( props != null ) {
			propValue = props.getValue("Text");
			if ( propValue != null ) {
				if ( propValue.equals(s)) {
					x = annotation.getX();
					y = annotation.getY();
					if ( changeSelection ) {
						annotation.setSelected(true);
					}
					foundAnnotation = annotation;
				}
			}
		}
		else {
			if ( changeSelection ) {
				annotation.setSelected(false);
			}
		}
	}
	
	if ( center ) {
		// center the screen around the node
		__screenLeftX = x - (__screenDataWidth / 2);
		__screenBottomY = y - (__screenDataHeight / 2);
		forceRepaint();
	}
	
	return foundAnnotation;
}

/**
Displays a dialog containing all the nodes on the network; the user can 
select one and the node will be highlighted and zoomed to.
*/
public HydrologyNode findNode()
{
	// compile a list of all the node IDs in the network
	int size = __nodes.length;
	List<String> v = new Vector<String>(size);
	for (int i = 0; i < size; i++) {
		v.add(__nodes[i].getCommonID());
	}

	// sort to ascending String order
	java.util.Collections.sort(v, String.CASE_INSENSITIVE_ORDER);	

	// display a dialog from which the user can choose the node to find
	JComboBoxResponseJDialog j = new JComboBoxResponseJDialog(__parent,
		"Select the Node to Find", "Select the node to find in the network", v,
		ResponseJDialog.OK | ResponseJDialog.CANCEL, true);
		
	String s = j.response();
	if (s == null) {
		// if s is null, then the user pressed CANCEL
		return null;
	}

	// find the node in the network and center the display window around it.
	HydrologyNode foundNode = findNode ( s, true, true );

	if ( foundNode == null ) {
		new ResponseJDialog(__parent, "Node '" + s + "' not found",
			"The node with ID '" + s + "' could not be found.\n"
			+ "The node ID must match exactly, including case\n"
			+ "sensitivity.", ResponseJDialog.OK).response();
		return null;
	}

	return foundNode;
}

/**
Find a node given its common identifier.
@param changeSelection if true, then change the selection of the node as the identifier is checked
@param center if true, center on the found node; if false do not change position
@return the found node, or null if not found
*/
public HydrologyNode findNode ( String s, boolean changeSelection, boolean center )
{
	double x = 0;
	double y = 0;
	HydrologyNode foundNode = null;
	int size = __nodes.length;
	for (int i = 0; i < size; i++) {
		if (__nodes[i].getCommonID().equals(s)) {
			x = __nodes[i].getX();
			y = __nodes[i].getY();
			if ( changeSelection ) {
				__nodes[i].setSelected(true);
			}
			foundNode = __nodes[i];
		}
		else {
			if ( changeSelection ) {
				__nodes[i].setSelected(false);
			}
		}
	}
	
	if ( center ) {
		// center the screen around the node
		__screenLeftX = x - (__screenDataWidth / 2);
		__screenBottomY = y - (__screenDataHeight / 2);
		forceRepaint();
	}
	
	return foundNode;
}

/**
Finds the number of the node at the specified click.
@param x the x location of the click on the screen
@param y the y location of the click on the screen, inverted for RTi Y style.
@return the number of the node at the specified click, or -1 if no node was clicked on.
*/
private int findNodeOrAnnotationAtXY(double x, double y)
{	String routine = "Statemod_Network_JComponent.findNodeAtXY";
	double newX = x;
	double newY = y;
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine, "Trying to find node or annotation at X: " + x + "   Y: " + y);
	}
	for (int i = (__nodes.length - 1); i >= 0; i--) {
//		Message.printStatus(1, "", "" + __nodes[i].getCommonID() + "  " 
//			+ __nodes[i].getX() + ", " + __nodes[i].getY()
//			+ "   [" + __nodes[i].getWidth() + "/" 
//			+ __nodes[i].getHeight() + "]  "
//			+ __nodes[i].isVisible());
		if (__nodes[i].contains(newX, newY) && __nodes[i].isVisible()) {
			__isLastSelectedAnAnnotation = false;
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Found node [" + i + "] at X: " + x + "   Y: " + y);
			}
			return i;
		}
	}

	GRLimits limits = null;	
	HydrologyNode node = null;
	int size = __annotations.size();
	for (int i = 0; i < size; i++) {	
		node = __annotations.get(i);
		limits = new GRLimits(node.getX(), node.getY(), 
			node.getX() + node.getWidth(), node.getY() + node.getHeight());
		if (limits.contains(x, y) && node.isVisible()) {
			__isLastSelectedAnAnnotation = true;
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Found annotation [" + i + "] at X: " + x + "   Y: " + y);
			}
			return i;
		}
	}
	return -1;
}

/**
Forces the display to be completely repainted.
*/
public void forceRepaint() {
	__forceRefresh = true;
	repaint();
}

/**
Return the list of StateMod_Network_AnnotationData to be processed when rendering the map.
*/
protected List<StateMod_Network_AnnotationData> getAnnotationData ()
{
	return __annotationDataList;
}

/**
Returns the annotation node held in the annotations list at the specified position.
@return the annotation node held in the annotations list at the specified position.
*/
protected HydrologyNode getAnnotationNode(int nodeNum) {
	return __annotations.get(nodeNum);
}

/**
Return the dash pattern used for dashed lines.
*/
protected float [] getDashPattern ()
{
	return __dashes;
}

/**
Returns the bottom Y value of the data.
@return the bottom Y value of the data.
*/
protected double getDataBottomY() {
	return __dataBottomY;
}

/**
Returns the left X value of the data.
@return the left X value of the data.
*/
protected double getDataLeftX() {
	return __dataLeftX;
}

/**
Returns the data limits for the entire network.
@return the data limits for the entire network.
*/
protected GRLimits getDataLimits() {
	return __dataLimits;
}

/**
Returns the data limits for the entire network.
@return the data limits for the entire network.
*/
protected GRLimits getDataLimitsMax() {
	return __dataLimitsMax;
}

/**
Return the GRDrawingArea used for drawing.  This allows external code to draw on the drawing area.
@return the GRDrawingArea used for drawing.
*/
public GRJComponentDrawingArea getDrawingArea ()
{	return __drawingArea;
}

/**
Returns the limits of the legend, which are in data units.
@return the limits of the legend.
*/
protected GRLimits getLegendDataLimits() {
	return __legendDataLimits;
}

/**
Return the link list.
*/
protected List<PropList> getLinkList()
{
	return __links;
}

/**
Return the margin in inches (all sides are the same).
*/
public double getMargin()
{
	return .75;
}

/**
Returns GRLImits with points representing the bounds of the printing margins.  
Called by the reference display.
Returns GRLImits with points representing the bounds of the printing margins.  
*/
public GRLimits getMarginLimits() {
	double leftX = __pageFormat.getImageableX()/__printScale;
	double topY = (__pageFormat.getHeight() - __pageFormat.getImageableY())/__printScale;
	double rightX = (leftX + __pageFormat.getImageableWidth()/__printScale) - 1;
	double bottomY = ((__pageFormat.getHeight() - (__pageFormat.getImageableY() 
			+ __pageFormat.getImageableHeight()))/__printScale) + 1;
	leftX = convertAbsX(leftX) + __dataLeftX;
	topY = convertAbsY(topY) + __dataBottomY;
	rightX = convertAbsX(rightX) + __dataLeftX;
	bottomY = convertAbsY(bottomY) + __dataBottomY;

	return new GRLimits(leftX, bottomY, rightX, topY);
}

/**
Returns the node network.
@return the node network.
*/
protected StateMod_NodeNetwork getNetwork() {
	return __network;
}

/**
Indicate whether any of the main network or its node properties have changed since the last load/save.
*/
public boolean getNetworkChanged ()
{
	return __networkChanged;
}

/**
Returns the nodes array.
@return the nodes array.
*/
protected HydrologyNode[] getNodesArray() {
	return __nodes;
}


/**
Returns a list of all the nodes in the node array that are a given type.
@param type the type of nodes (as defined in HydrologyNode.NODE_*) to return.
@return a list of all the nodes that are the specified type.  The list is guaranteed to be non-null.
*/
public List<HydrologyNode> getNodesForType(int type) {
	List<HydrologyNode> v = new Vector<HydrologyNode>();

	for (int i = 0; i < __nodes.length; i++) {
		if (__nodes[i].getType() == type) {
			v.add(__nodes[i]);
		}
	}

	return v;
}

/**
Returns the print scale for the network.
@return the print scale for the network.
*/
protected double getPrintScale() {
	return __printScale;
}

/**
Return the list of selected nodes, presumably so that specific actions can be taken in coordination
with editing.  Major adjustments to the nodes will require rebuilding the node list by calling
buildNodeArray() and possibly refreshing the network display.
@return the list of selected nodes
*/
protected List<HydrologyNode> getSelectedNodes ()
{	HydrologyNode [] nodeArray = getNodesArray();
	List<HydrologyNode> nodeList = new Vector<HydrologyNode>();
	for ( int i = 0; i < nodeArray.length; i++ ) {
		if ( nodeArray[i].isSelected() ) {
			nodeList.add(nodeArray[i]);
		}
	}
	return nodeList;
}

/**
Returns the data limits from the absolute bottom left to the total data width
and total data height (which may or may not match up with the size of the paper)
due to network size requirements.
*/
protected GRLimits getTotalDataLimits() {
	return new GRLimits(
		__dataLeftX, 
		__dataBottomY,
		__dataLeftX + __totalDataWidth,
		__dataBottomY + __totalDataHeight);
}

/**
Return the scale, relative to a full 1:1 rendering for the page layout.
A value of 2 means that the rendered graphics are twice as big as the full-size rendering.
A value of .5 means that the rendered graphics are 1/2 as big as the full-size rendering.
@return the scale, relative to a full 1:1 rendering for the page layout
*/
public double getScale ()
{	//String routine = getClass().getName() + ".getScale";
	double scale = Double.NaN;
	// The scale is determined by mapping the current drawing area to the full scale drawing area
	// The resulting data range is then compared to the full scale data range.
	// In other words, how big would the full-scale drawing area be if the current rendering settings were
	// used to do a full rendering?  The scale is then the ratio of the full scale drawing area height to
	// the drawing area that would result from the full rendering at current settings.
	double drawingAreaHeightRendered = this.getDrawingArea().getDrawingLimits().getHeight()*
		(this.getDrawingArea().getDataLimits().getHeight()/
		this.__drawingAreaFullScale.getDataLimits().getHeight());
	double drawingAreaHightFullScale = this.__drawingAreaFullScale.getDrawingLimits().getHeight();
	scale = drawingAreaHeightRendered/drawingAreaHightFullScale;
	Message.printStatus(2, "", "Scale is: " + scale );
	return scale;
}

/**
Return the selected network page layout properties to use for rendering.
@return the selected network page layout properties to use for rendering
*/
public PropList getSelectedPageLayoutPropList ()
{
	return this.__selectedPageLayoutPropList;
}

/**
Returns the total height of the entire network.
@return the total height of the entire network.
*/
protected int getTotalHeight() {
	return __totalBufferHeight;
}

/**
Returns the total width of the entire network.
@return the total width of the entire network.
*/
protected int getTotalWidth() {
	return __totalBufferWidth;
}

/**
Returns the visible limits of the screen in data units.
@return the visible limits of the screen in data units.
*/
protected GRLimits getVisibleDataLimits() {
	return new GRLimits(
		__screenLeftX,
		__screenBottomY, 
		__screenLeftX + __screenDataWidth,
		__screenBottomY + __screenDataHeight);
}

/**
Initialize the network component for the network layout.
This method is called in the constructor used for printing.
Extract basic settings from the network page layout and set in this object
This is necessary because these properties are not part of the printer job
Printer job properties are controlled by the GraphicsPrinterJob that is used to
initiate printing.  The print() and paint() methods will adjust to the printer job settings
and imageable area.
*/
protected void initializeForNetworkPageLayout ( String pageLayout )
{	String routine = getClass().getName() + ".initializeForNetworkPageLayout";
	StateMod_NodeNetwork net = getNetwork();
    //String orientation = null;
    //String paperSize = null;
    double nodeSize = 10;
    int nodeFontSize = 10;
    List<PropList> layouts = net.getLayoutList();
    boolean found = false;
    for (PropList p : layouts ) {
        String id = p.getValue("ID");
        if ( id.equalsIgnoreCase(pageLayout)) {
        	setSelectedPageLayoutPropList ( p );
            //orientation = p.getValue("PageOrientation");
            //paperSize = p.getValue("PaperSize");
            nodeSize = Double.parseDouble(p.getValue("NodeSize"));
            Message.printStatus(2,routine,"Node size from layout is " + nodeSize + " points.");
            // This does not work here because it needs graphics from print/paint time
            //setPrintNodeSize(nodeSize);
            nodeFontSize = Integer.parseInt(p.getValue("NodeLabelFontSize"));
            setPrintFontSize(nodeFontSize);
            found = true;
            break;
        }
    }
    if ( !found ) {
    	throw new RuntimeException (
    		"Layout \"" + pageLayout + "\" was not found in network.  Cannot initialize network.");
    }
    setDataLimits ( new GRLimits(net.getLX(), net.getBY(), net.getRX(), net.getTY()) );
	//setPaperSize(paperSize);
	//setOrientation(orientation);
	//setPrintNodeSize(nodeSize);
	//setPrintFontSize(nodeFontSize);
	//forceRepaint();
}

/**
Initialize internal settings for printing.
*/
protected void initializeForPrinting()
{
	__drawMargin = false; // Margin is non-imageable area
	__antiAlias = true; // On for printing, to look better
	__printCount = 0;
	__dpi = 72; // for full-scale printing, used to scale fonts, etc.
	// FIXME SAM 2011-07-05 All of this seems non needed when printing - just use print graphics
	//__tempBuffer = new BufferedImage(__totalBufferWidth, __totalBufferHeight, BufferedImage.TYPE_4BYTE_ABGR);
	//__bufferGraphics = (Graphics2D)(__tempBuffer.createGraphics());
	// TODO SAM 2011-07-04 Had to put the following in for batch processing
	// because graphics is used for checking font size, etc.
	//if ( _graphics == null ) {
	//	_graphics = __bufferGraphics;
	//}
	__printingNetwork = true;
	// FIXME SAM 2011-07-05 Not sure if this is needed
	//zoomOneToOne();
}

/**
Inverts the value of Y so that Y runs from 0 at the bottom to MAX at the top.
This method typically is only called by interactive events such as mouse drags since
other drawing is done using drawing limits that handle the inverted Y axis.
@param y the value of Y to invert.
@return the inverted value of Y.
*/
private double invertY(double y) {
	return _devy2 - y;
}

/**
Checks to see if the network is dirty.
@return true if the network is dirty, false if not.
*/
public boolean isDirty() {
	if (getNetworkChanged()) {
//		Message.printStatus(1, "", "isDirty: NetworkChanged: true");
		return true;
	}
	for (int i = 0; i < __nodes.length; i++) {
		if (__nodes[i].isDirty()) {
//			Message.printStatus(1, "", "isDirty: Node[" + i + "]: dirty");
			return true;
		}
	}
	int size = __annotations.size();
	HydrologyNode node = null;
	for (int i = 0; i < size; i++) {
		node = __annotations.get(i);
		if (node.isDirty()) {
//			Message.printStatus(1, "", "isDirty: Annotation[" + i + "]: dirty");
			return true;
		}
	}
	return false;
}

/**
Listens for key presses events and cancels drags if Escape is pushed.
@param event the KeyEvent that happened.
*/
public void keyPressed(KeyEvent event) {
	if (__legendDrag || __nodeDrag) {
		// only worry about escape keypresses
		if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// and only worry about them if something is currently being dragged
			if (__legendDrag) {
				__legendDrag = false;
			}
			else if (__nodeDrag) {
				if (__isLastSelectedAnAnnotation) {
					HydrologyNode node = __annotations.get(__clickedNodeNum);
					node.setVisible(true);
				}
				else {
					__parent.displayNodeXY(__nodes[__clickedNodeNum].getX(), __nodes[__clickedNodeNum].getY());
					__nodes[__clickedNodeNum].setVisible(true);
				}
				__clickedNodeNum = -1;
				__nodeDrag = false;
			}
			forceRepaint();
		}
	}
}

/**
Does nothing.
*/
public void keyReleased(KeyEvent event) {}

/**
Does nothing.
*/
public void keyTyped(KeyEvent event) {}

/**
Does nothing.
*/
public void mouseClicked(MouseEvent event) {}

// TODO SAM 2011-07-07 The drag of multiple objects distorts the placement of the objects in the group
// and it gets worse as multiple drags occur
/**
Responds to mouse dragged events and moves around the legend, a node, or 
the screen, depending on what is being dragged.
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {
	double xx = convertDrawingXToDataX(event.getX()) + __screenLeftX;
	double yy = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
	__parent.setLocation(xx, yy);
	if (!__editable && !__screenDrag) {
		return;
	}
	if (__nodeDrag) {
		if (__snapToGrid) {
			// this version of findNearestGridXY does 
			// the conversion to data units from mouse-click units 
			double[] p = findNearestGridXY(event);

			// if the nearest grid position is not where the node currently is located ...
			if (p[0] != (int)__mouseDataX || p[1] != (int)__mouseDataY) {
				// move the node to be situated on that grid point
				__mouseDataX = p[0];
				__mouseDataY = p[1];
				repaint();
			}
		}
		else {
			// convert the mouse click to data units so that the
			// node bounding box can be drawn on the screen
			__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
			__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
			repaint();
		}
		__parent.displayNodeXY(__mouseDataX, __mouseDataY);
	}
	else if (__legendDrag) {
		if (__snapToGrid) {
			// this version of findNearestGridXY assumes already-
			// converted X and Y values are passed in.  Use them
			double cx = convertDrawingXToDataX(event.getX()) + __screenLeftX;
			double cy = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
			double[] p = findNearestGridXY((int)cx - (int)__xAdjust, (int)cy - (int)__yAdjust);

			// if the nearest grid location is different from where the legend was last ...
			if (__mouseDataX != ((int)cx + p[0]) || __mouseDataY != ((int)cy + p[1])){
				// move the legend so that it is at that grid location
				__mouseDataX = (int)cx + p[0];
				__mouseDataY = (int)cy + p[1];
			}
			repaint();
		}
		else {
			__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
			__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
			repaint();
		}
	}
	else if (__screenDrag) {
		double mouseX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
		double mouseY = convertDrawingYToDataY(invertY(event.getY())) +  __screenBottomY;

		double dx = __mouseDataX - mouseX;
		double dy = __mouseDataY - mouseY;

		__mouseDataX = mouseX;
		__mouseDataY = mouseY;
	
		if (__screenDataWidth >= __totalDataWidth) {
			// If zoomed out so that the entire network and paper is smaller than the screen ...
			// only allow the screen to be moved to the left, so that left edge of the paper can be aligned
			// with the left edge of the screen
			if (dx > 0) {}
			else {
				__mouseDataX -= __screenLeftX;
				__screenLeftX += (dx * __DX);
				if (__screenLeftX < __dataLeftX) {
					__screenLeftX = __dataLeftX;
				}
	
				__mouseDataX += __screenLeftX;
			}	
		}
		else {
			// If zoomed in so not all the network or paper can
			// be seen at once, then determine how far the 
			// screen was panned and adjust accordingly
			__mouseDataX -= __screenLeftX;
			__screenLeftX += (dx * __DX);

			// Don't allow the screen to be moved too far right or left.
			if (__screenLeftX >(__dataLeftX + __totalDataWidth) - convertDrawingXToDataX(getBounds().width)) {
				__screenLeftX = (__dataLeftX + __totalDataWidth) - convertDrawingXToDataX(getBounds().width);
			}	

			if (__screenLeftX < __dataLeftX) {
				__screenLeftX = __dataLeftX;
			}
	
			__mouseDataX += __screenLeftX;
		}

		if (__screenDataHeight >= __totalDataHeight) {
			// If zoomed out so that the entire network and paper is smaller than the screen ...
			// only allow the screen to be moved down, so that bottom edge of the paper can be aligned
			// with the bottom edge of the screen		
			if (dy > 0) {}
			else {
				__mouseDataY -= __screenBottomY;
				__screenBottomY += (dy * __DY);
	
				if (__screenBottomY < __dataBottomY) {
					__screenBottomY = __dataBottomY;
				}
	
				__mouseDataY += __screenBottomY;
			}				
		}
		else {	
			// if zoomed in so not all the network or paper can
			// be seen at once, then determine how far the 
			// screen was panned and adjust accordingly		
			__mouseDataY -= __screenBottomY;
			__screenBottomY += (dy * __DY);

			// don't allow the screen to be moved too far up or down.
			if (__screenBottomY > (__dataBottomY + __totalDataHeight) - convertDrawingYToDataY(getBounds().height)) {
				__screenBottomY = (__dataBottomY + __totalDataHeight) - convertDrawingYToDataY(getBounds().height);
			}

			if (__screenBottomY < __dataBottomY) {
				__screenBottomY = __dataBottomY;
			}

			__mouseDataY += __screenBottomY;
		}

		forceRepaint();
	}
	else if (__drawingBox) {
		__currDragX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
		__currDragY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
		repaint();
	}
}

/**
Does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
Updates the current mouse location in the parent frame's status bar.
@param event the MouseEvent that happened.
*/
public void mouseMoved(MouseEvent event) {
	double xx = convertDrawingXToDataX(event.getX()) + __screenLeftX;
	double yy = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
	__parent.setLocation(xx, yy);
}

/**
Responds to mouse pressed events by determining whether a node, the legend or
the screen was clicked on.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
	// do not respond to popup events, so make sure that it was the left
	// mouse button that was pressed.
	if (event.getButton() != MouseEvent.BUTTON1) {
		return;
	}
	
	// determine first whether a node was clicked on
	__clickedNodeNum = findNodeOrAnnotationAtXY(convertDrawingXToDataX(event.getX()) + __screenLeftX,
		convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY);

	// if a node was clicked on ...
	if (__clickedNodeNum > -1) {
		__mouseDeviceX = event.getX();
		__mouseDeviceY = event.getY();
		if (__isLastSelectedAnAnnotation) {
			__parent.displayNode(getAnnotationNode(__clickedNodeNum));
		}
		else {
			__parent.displayNode(__nodes[__clickedNodeNum]);
		}
		// ... then set everything up so that the node can be dragged around the screen.  
		if (!__editable) {
			return;
		}		

		if (__isLastSelectedAnAnnotation) {
			HydrologyNode node = __annotations.get(__clickedNodeNum);
			__draggedNodeLimits = new GRLimits(node.getX(), node.getY(), 
				node.getX() + node.getWidth(), node.getY() + node.getHeight());
			if (__snapToGrid) {
				double[] p = findNearestGridXY(event);
				__mouseDataX = p[0];
				__mouseDataY = p[1];
			}
			else {
				__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
				__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
			}
			__xAdjust = __mouseDataX- __draggedNodeLimits.getMinX();
			__yAdjust = __mouseDataY- __draggedNodeLimits.getMinY();

			__draggedNodes = new int[0];
		}
		else {
			if (__snapToGrid) {
				double[] p = findNearestGridXY(event);
				__mouseDataX = p[0];
				__mouseDownX = __mouseDataX;
				__mouseDataY = p[1];
				__mouseDownY = __mouseDataY;
				__draggedNodeLimits = __nodes[__clickedNodeNum].getLimits();
				__xAdjust = __nodes[__clickedNodeNum].getWidth()/ 2;
				__yAdjust =__nodes[__clickedNodeNum].getHeight()/ 2;
			}
			else {
				__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
				__mouseDownX = __mouseDataX;
				__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
				__mouseDownY = __mouseDataY;
				__draggedNodeLimits = __nodes[__clickedNodeNum].getLimits();
				__xAdjust = convertDrawingXToDataX(event.getX()) + __screenLeftX - __draggedNodeLimits.getMinX();
				__yAdjust = convertDrawingYToDataY(invertY(event.getY())) +__screenBottomY - __draggedNodeLimits.getMinY();
			}

			if (__nodes[__clickedNodeNum].isSelected()) {
				// if the node is already selected, retain the existing node selection.
			}
			else {
				// otherwise, the node is the only selected one
				for (int i = 0; i < __nodes.length; i++) {
					__nodes[i].setSelected(false);		
				}		
				__nodes[__clickedNodeNum].setSelected(true);
			}

			buildSelectedNodesLimits();
		}
		__nodeDrag = true;
		forceRepaint();
	}
	else {
		// otherwise, check to ese if the legend was clicked on
		double x = convertDrawingXToDataX(event.getX()) + __screenLeftX;
		double y = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
		if (__legendDataLimits.contains(x, y)) {
			if (!__editable) {
				return;
			}
			// ... if so, set everything up so it can be dragged.
			__legendDrag = true;
			__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
			__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
			__xAdjust = convertDrawingXToDataX(event.getX()) + __screenLeftX - __legendDataLimits.getMinX();
			__yAdjust = convertDrawingYToDataY(invertY(event.getY()))+__screenBottomY - __legendDataLimits.getMinY();
			__eraseLegend = true;
			forceRepaint();
		}
		else {
			if (__networkMouseMode == MODE_PAN) {
				// otherwise, prepare the screen to be moved around
				__screenDrag = true;
				__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
				__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
				repaint();
			}
			else {
				__drawingBox = true;
				__dragStartX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
				__dragStartY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
			}
		}
	}
}

/**
Responds to mouse released events by placing a dragged node or the dragged 
legend in its new position, or by showing a popup menu.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	if (event.isPopupTrigger()) {
		// find the node on which the popup menu was triggered
		int nodeNum = findNodeOrAnnotationAtXY(convertDrawingXToDataX(event.getX()) + __screenLeftX,
			convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY);
		__popupX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
		__popupY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
		
		if (nodeNum > -1) {
			// if nodeNum > -1 then a node was clicked on.
			// findNodeAtXY() checks to see if the node is an
			// annotation node or not, and sets __isAnnotation appropriately
			__popupNodeNum = nodeNum;
			if (__isLastSelectedAnAnnotation) {
				__annotationPopup.show(event.getComponent(), event.getX(), event.getY());
			}
			else {
				if (nodeHasLinks()) {
					__deleteLinkMenuItem.setEnabled(true);
				}
				else {
					__deleteLinkMenuItem.setEnabled(false);
				}
				if (inStateModGUI()) {
					// Adding and deleting nodes is controlled through the Edit menu in the StateMod GUI
					// in order to ensure that other data manipulation occurs properly.
					__addNodeMenuItem.setEnabled(false);
					__addNodeMenuItem.setToolTipText( "Use the Edit > Add menu in " +
						"StateMod GUI and then finish setting network node properties." );
					__deleteNodeMenuItem.setEnabled(false);
					__deleteNodeMenuItem.setToolTipText( "Use the Edit > Delete menu in StateMod GUI." );
				}
				__nodePopup.show(event.getComponent(), event.getX(), event.getY());
			}
		}
		else {
			// no node was clicked on
			__networkPopup.show(event.getComponent(), event.getX(), event.getY());
		}
		return;
	}

	if (!__editable && !(__screenDrag || __drawingBox)) {
		return;
	}

	if (__clickedNodeNum > -1) {
		// if a node was being dragged ...
		__nodeDrag = false;

		// make sure that the user didn't simply click on a node and
		// release the mouse button without moving.  Without the 
		// following check there's a good chance that the node will be moved slightly. 
		if (__mouseDeviceX == event.getX() && __mouseDeviceY == event.getY()) {
			__clickedNodeNum = -1;
			forceRepaint();
			return;
		}
		
		if (!__snapToGrid) {
			__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX;
			__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY;
		}
			
		if (__isLastSelectedAnAnnotation) {
			// prevent nodes from being dragged off the drawing area completely.
			__mouseDataX -= __xAdjust;
			__mouseDataY -= __yAdjust;

			HydrologyNode node = __annotations.get(__clickedNodeNum);
			GRLimits data = __drawingArea.getDataLimits();
			if (__mouseDataX < data.getLeftX()) {
				__mouseDataX = data.getLeftX() + node.getWidth() / 2;
			}
			if (__mouseDataY < data.getBottomY()) {
				__mouseDataY = data.getBottomY() + node.getHeight() / 2;
			}
			if (__mouseDataX > data.getRightX()) {
			__mouseDataX = data.getRightX() - node.getWidth() / 2;
			}
			if (__mouseDataY > data.getTopY()) {
				__mouseDataY = data.getTopY() - node.getHeight() / 2;
			}

			nodeWasMoved();
			
			node.setX(__mouseDataX);
			node.setY(__mouseDataY);
			updateAnnotationLocation(__clickedNodeNum);
			node.setDirty(true);
			forceRepaint();
			__parent.displayNode(node);
		}
		else {
			moveDraggedNodes();
		}
		__clickedNodeNum = -1;
	}
	else if (__legendDrag) {
		// otherwise, if the legend was dragged ...
		__legendDrag = false;
		__eraseLegend = false;
		
		if (__snapToGrid) {
			// TODO SAM Evaluate logic
			//__mouseDataX = __mouseDataX;
			//__mouseDataY = __mouseDataY;
		} 
		else {
			__mouseDataX = convertDrawingXToDataX(event.getX()) + __screenLeftX - __xAdjust;
			__mouseDataY = convertDrawingYToDataY(invertY(event.getY())) + __screenBottomY - __yAdjust;
		}

		// prevent the legend from being dragged off the edge of the screen
		GRLimits data = __drawingArea.getDataLimits();
		if (__mouseDataX < data.getLeftX()) {
			__mouseDataX = data.getLeftX() + 10;
		}
		if (__mouseDataY < data.getBottomY()) {
			__mouseDataY = data.getBottomY() + 10;
		}
		if (__mouseDataX > data.getRightX()) {
			__mouseDataX = data.getRightX() + 10;
		}
		if (__mouseDataY > data.getTopY()) {
			__mouseDataY = data.getTopY() + 10;
		}
		if (__snapToGrid) {
			__legendDataLimits.setLeftX(__mouseDataX - __xAdjust);
			__legendDataLimits.setBottomY(__mouseDataY - __yAdjust);
		}
		else {
			__legendDataLimits.setLeftX(__mouseDataX);
			__legendDataLimits.setBottomY(__mouseDataY);
		}
		forceRepaint();
	}			
	else if (__screenDrag) {
		// cancel the screen drag
		__screenDrag = false;
	}
	else if (__drawingBox) {
		// finish drawing the box and select all the nodes that were within it
		__drawingBox = false;

		double lx, rx, ty, by;
		if (__currDragX < __dragStartX) {
			lx = __currDragX;
			rx = __dragStartX;
		}
		else {
			rx = __currDragX;
			lx = __dragStartX;
		}

		if (__currDragY < __dragStartY) {
			by = __currDragY;
			ty = __dragStartY;
		}
		else {
			ty = __currDragY;
			by = __dragStartY;
		}		

		GRLimits limits = new GRLimits(lx, by, rx, ty);

		for (int i = 0; i < __nodes.length; i++) {
			if (within(limits, __nodes[i])) {
				__nodes[i].setSelected(true);
			}
			else {
				__nodes[i].setSelected(false);
			}
		}
		forceRepaint();
	}
}

/**
Does the actual work of moving a node from one location to another.
@param num the index of the node (in the __nodes array) to be moved.
*/
private void moveDraggedNode(int num) {
	double mainX = __mouseDataX - __xAdjust;
	double mainY = __mouseDataY - __yAdjust;

	double dx = mainX - __mouseDownX;
	double dy = mainY - __mouseDownY;

	double nodeX = __draggedNodesXs[num] + dx;
	double nodeY = __draggedNodesYs[num] + dy;

	if (__snapToGrid) {
		__nodes[__draggedNodes[num]].setX(nodeX + __nodes[__draggedNodes[num]].getWidth() / 2);
		__nodes[__draggedNodes[num]].setY(nodeY + __nodes[__draggedNodes[num]].getHeight() / 2);
	}
	else {
		__nodes[__draggedNodes[num]].setX(nodeX);
		__nodes[__draggedNodes[num]].setY(nodeY);
	}
}

/**
Changes the position of dragged nodes once the mouse button is released on a drag.
*/
private void moveDraggedNodes() {
	// Prevent nodes from being dragged off the drawing area completely.
	GRLimits data = __drawingArea.getDataLimits();
	if (__mouseDataX < data.getLeftX()) {
		__mouseDataX = data.getLeftX() + __nodes[__clickedNodeNum].getWidth() / 2;
	}
	if (__mouseDataY < data.getBottomY()) {
		__mouseDataY = data.getBottomY() + __nodes[__clickedNodeNum].getHeight() / 2;
	}
	if (__mouseDataX > data.getRightX()) {
		__mouseDataX = data.getRightX() - __nodes[__clickedNodeNum].getWidth() / 2;
	}
	if (__mouseDataY > data.getTopY()) {
		__mouseDataY = data.getTopY() - __nodes[__clickedNodeNum].getHeight() / 2;
	}
			
	if (__draggedNodes == null || __draggedNodes.length == 0) {
		nodeWasMoved();
	}
	else {
		createMultiNodeChangeOperation();
	}
		
	__nodes[__clickedNodeNum].setX(__mouseDataX);
	__nodes[__clickedNodeNum].setY(__mouseDataY);
	__nodes[__clickedNodeNum].setDirty(true);
//	Message.printStatus(1, "", "Node moved, set dirty: " + true);

	for (int i = 0; i < __draggedNodes.length; i++) {
		moveDraggedNode(i);
	}
	
	forceRepaint();
	__parent.displayNode(__nodes[__clickedNodeNum]);
//	__nodes[__clickedNodeNum].setSelected(false);
}

/**
Checks to see if a node has any links.
@return true if the node has links, otherwise false.
*/
private boolean nodeHasLinks() {
	if (__links == null) {
		return false;
	}
	String s = null;
	String id = __nodes[__popupNodeNum].getCommonID();
	for ( PropList p: this.__links) {
		s = p.getValue("FromNodeID");
		if (s.equals(id)) {
			return true;
		}
		s = p.getValue("ToNodeID");
		if (s.equals(id)) {
			return true;
		}
	}
	return false;
}

/**
Called when a node was moved internally.  Creates a node change operation.
*/
private void nodeWasMoved() {
	addNodeChangeOperation(__clickedNodeNum, __mouseDataX, __mouseDataY);
}

/**
Paints the screen.
@param g the Graphics context to use for painting.
*/
@SuppressWarnings("deprecation")
public void paint(Graphics g) {
	// When printing is called, the print(Graphics ...) routine ends up
	// getting called three times.  Just a foible of Java, that routine
	// gets called more than once, even if just one page should be printed.
	// Multiple calls to print can result in some weirdly-drawing things,
	// plus they slow it down.  The following check makes sure that when
	// the network is being printed, it is only drawn one time.
    //int dl=5;
    String routine = "StateMod_Network_JComponenet.paint";
	if ( this.__printingNetwork && !this.__useOldPrinting ) {
		// Printing the network using new code.  Just print without all the interactive checks
		// The drawing limits, font size, etc, will have been set in the print(...) method since that
		// has information about the selected paper size
		// Graphics2D object was already set in the print() method before calling this method.
		//drawTestPage ( g );
		// The font size in points for full-scale printing is from the network
		String nodeLabelFontSizeString = getSelectedPageLayoutPropList().getValue("NodeLabelFontSize");
		if ( nodeLabelFontSizeString == null ) {
			nodeLabelFontSizeString = "10";
		}
		double fontSize = Double.parseDouble(nodeLabelFontSizeString);
		this.__drawingArea.setFont("Helvetica", "Plain", (int)fontSize );
		setAntiAlias(__antiAlias);
		// Set the node size
		String nodeSizeString = getSelectedPageLayoutPropList().getValue("NodeSize");
		if ( nodeSizeString == null ) {
			nodeSizeString = "10";
		}
		double nodeSize = Double.parseDouble(nodeSizeString);
		setPrintNodeSize(nodeSize);
		// The following does bad things
		//scaleUnscalables();
		for (int i = 0; i < __nodes.length; i++) {
			__nodes[i].calculateExtents(__drawingArea);
		}
		drawNodes( getDrawingArea(), getNodesArray() );
		drawNetworkLines();
		drawLinks( getDrawingArea(), getLinkList(), getNodesArray(), getDashPattern() );
		drawLegend();
		drawAnnotations();
		return;
	}
	if (this.__printCount > 0) {
		// Apparently this is only an issue with in-memory image drawing but for printing multiple
		// calls are needed to complete rendering (?).
		return;
	}
	// Sets the graphics in the base class appropriately (double-buffered
	// if doing double-buffered drawing, single-buffered if not)
	if (__printingNetwork) {
		Font f = __drawingArea.getFont();
		__bufferGraphics.setFont(new Font(f.getName(), f.getStyle(), __printFontSizePoints));
		forceGraphics(__bufferGraphics);
		__bufferGraphics.setFont(new Font(f.getName(), f.getStyle(), __printFontSizePoints));
	}
	else if (__printingScreen || __savingNetwork || __savingScreen) {
		// force the graphics to use the double-buffer graphics
		forceGraphics(__bufferGraphics);
	}
	else {
		// normal paint, just use the provided graphics
		setGraphics(g);
	}

	setAntiAlias(__antiAlias);
	
	// First time ever through, do the following ...
	if (!__initialized) {
		__initialized = true;

		__drawingAreaHeight = getBounds().height;
		__drawingAreaWidth = getBounds().width;	
		
		Font f = __drawingArea.getFont();
		__drawingArea.setFont(f.getName(), f.getStyle(), 10);

		if ( !__printingNetwork ) {
			setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
		}
		GRLimits limits = new GRLimits(0, 0, getBounds().width, getBounds().height);
		__drawingArea.setDrawingLimits(limits, GRUnits.DEVICE, GRLimits.DEVICE);

		GRLimits data = null;
		
		// Determine the datalimits to be drawn in the current screen
		if (__fitWidth) {
			double pct = ((double)(getBounds().height))	/ ((double)(getBounds().width));
			double height = pct * __totalDataWidth;
			data = new GRLimits(
				__dataLeftX, 
				__dataBottomY, 
				__dataLeftX + __totalDataWidth,
				__dataBottomY + height);			
		}
		else {
			double pct = ((double)(getBounds().width)) / ((double)(getBounds().height));
			double width = pct * __totalDataHeight;
			data = new GRLimits(
				__dataLeftX, 
				__dataBottomY, 
				__dataLeftX + width,
				__dataBottomY + __totalDataHeight);
		}

		__screenLeftX = __dataLeftX;
		__screenBottomY = __dataBottomY;
		__screenDataWidth = data.getWidth();
		__screenDataHeight = data.getHeight();
		__drawingArea.setDataLimits(data);

		// TODO (JTS - 2004-04-05) On the subject of node sizes:
		// Nodes are drawn in data units, so that whatever is specified
		// as the print node size is the number of pixels across the
		// node will appear on screen.  However, because of the magic
		// of different DPIs for print-outs and screen displays,
		// this will need adjusted.  On a PC, 48 pixels across is
		// one half an inch.  On the printed page, a half inch is
		// 36 pixels across. Whatever the final node size is, it 
		// will need adjusted to fit the printed DPI.  

		// and that doesn't even BEGIN to get into the problems with points vs. pixels ...

		setPrintNodeSize(__currNodeSize );

		for (int i = 0; i < __nodes.length; i++) {
			__nodes[i].calculateExtents(__drawingArea);
		}
	
		// Odd-looking, but it works.  With the above things in place,
		// it recalls this method (but this time the initialization
		// section won't be entered).  That sets up some more things
		// and then the NEXT time through, a complete refresh is 
		// forced just to make sure it all drew properly.
		zoomOneToOne();

		// set the grid step to be 1/8s of an inch
		if (__fitWidth) {
			__gridStep = convertDrawingXToDataX(__dpi / 8);
		}
		else {
			__gridStep = convertDrawingYToDataY(__dpi / 8);
		}
		
		repaint();
		__forceRefresh = true;
		scaleUnscalables();
	}
	else {
		// Check to see if the bounds of the device have changed --
		// if they have then the GUI window has been resized and
		// the double buffer size needs changed accordingly.
		if (__drawingAreaHeight != getBounds().height || __drawingAreaWidth != getBounds().width) {
		    adjustForResize();
			__drawingAreaHeight = getBounds().height;
			__drawingAreaWidth = getBounds().width;	
			GRLimits limits = new GRLimits(0, 0, getBounds().width, getBounds().height);
			__drawingArea.setDrawingLimits(limits, GRUnits.DEVICE, GRLimits.DEVICE);
			setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
			__forceRefresh = true;
		}
	}		  
	
	// The following section is for when networks are read in from
	// XML files.  The values below are read in after the GUI is
	// instantiated, and are set so that whenever the GUI finds that
	// one of them is set during a repaint it will apply them to the gui settings.
	boolean repaint = false;
	if (__holdPaperSize != null) {
		setPaperSize(__holdPaperSize);
		repaint = true;
	}
	if (__holdPaperOrientation != null) {
		setOrientation(__holdPaperOrientation);	
		repaint = true;
	}
	if (__holdPrintNodeSize != -1) {
		setPrintNodeSize(__holdPrintNodeSize);
		repaint = true;
	}
	if (__holdPrintFontSize != -1) {
		setPrintFontSize(__holdPrintFontSize);
		repaint = true;
	}

	if (repaint) {
		__forceRefresh = true;
	}
	
	// Only do the following if explicitly instructed to ...
	if (__forceRefresh) {
		Font f = __drawingArea.getFont();
		// A normal paint() call -- translate the screen so the proper
		// portion is drawn in the screen and set up the drawing limits.
		if (!__printingNetwork && !__printingScreen && !__savingNetwork && !__savingScreen) {
			__drawingArea.setFont(f.getName(), f.getStyle(), __fontSizePoints);
			setLimits(getLimits(true));			
			__drawingArea.setDataLimits(new GRLimits(
				__screenLeftX,
				__screenBottomY, 
				__screenLeftX + __screenDataWidth,
				__screenBottomY + __screenDataHeight));
			clear();
		}
		// If printing the entire network, do a translation so that the
		// entire network is drawn in the BufferedImage.  No X change 
		// is needed, but the bottom of the network needs aligned properly.
		else if (__printingNetwork) {
			__holdLimits = __drawingArea.getDataLimits();
			GRLimits data = new GRLimits(
				__dataLeftX, __dataBottomY,
				__dataLeftX + __totalDataWidth, 
				__dataBottomY + __totalDataHeight);
			__drawingArea.setDataLimits(data);
			__drawingArea.setDrawingLimits(new GRLimits(0, 0, 
				__totalBufferWidth, __totalBufferHeight), GRUnits.DEVICE, GRLimits.DEVICE);
			translate(0, __totalBufferHeight - getBounds().height);
			scaleUnscalables();
			clear();
			if ( this.__useOldPrinting ) {
				__bufferGraphics.setFont(new Font(f.getName(), f.getStyle(), __printFontSizePoints));
			}
            //  Message.printDebug(dl, routine, "dataLimits: " + __drawingArea.getDataLimits().toString());
            //  Message.printDebug(dl, routine, "drawingLimits: " + __drawingArea.getDrawingLimits().toString());
		}
		else if (__savingNetwork) {
			__holdLimits = __drawingArea.getDataLimits();
			GRLimits data = new GRLimits(
				__dataLeftX, __dataBottomY,
				__dataLeftX + __totalDataWidth, 
				__dataBottomY + __totalDataHeight);
			__drawingArea.setDataLimits(data);
			__drawingArea.setDrawingLimits(new GRLimits(0, 0, 
				__totalBufferWidth * (72.0 / (double)__dpi), 
				__totalBufferHeight * (72.0 / (double)__dpi)),
				GRUnits.DEVICE, GRLimits.DEVICE);
			translate(0, (int)(__totalBufferHeight 
				* (72.0 / (double)__dpi))  
				- getBounds().height);
			clear();
			__drawingArea.setFont(f.getName(), f.getStyle(), __fontSizePoints);			
		}		
		// if just the current screen is drawn, the same translation 
		// can be done that was done for normal drawing.
		else if (__printingScreen || __savingScreen) {
			clear();
			__drawingArea.setFont(f.getName(), f.getStyle(), __fontSizePoints);			
		}
		
		// Draw annotations below the network since node size is exaggerated
		try {
			for ( StateMod_Network_AnnotationData annotationData: getAnnotationData() ) {
				StateMod_Network_AnnotationRenderer annotationRenderer =
					annotationData.getStateModNetworkAnnotationRenderer();
				annotationRenderer.renderStateModNetworkAnnotation(this, annotationData );
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, "Error drawing annotations (" + e + ")." );
			Message.printWarning ( 3, routine, e );
		}
        
		setAntiAlias(__antiAlias);
		drawNetworkLines();
		setAntiAlias(__antiAlias);
		drawLinks(getDrawingArea(), getLinkList(), getNodesArray(), getDashPattern());
		setAntiAlias(__antiAlias);
		__drawingArea.setFont(f.getName(), f.getStyle(), __fontSizePoints);
		drawNodes( getDrawingArea(), getNodesArray() );

		setAntiAlias(__antiAlias);
		if (!__eraseLegend) {
			drawLegend();
		}

		setAntiAlias(__antiAlias);
		drawAnnotations();

		// If the grid should be drawn, do so ...
		setAntiAlias(__antiAlias);		
		if (__drawInchGrid) {
			// Change the limits so that the drawing is done in device units, not data units
			__drawingArea.setFloatLineDash(__dashes, 0);

			double maxX = __totalBufferWidth;
			double maxY = __totalBufferHeight;
			
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.red);
			int j = 0;
			int minY = (int)(convertAbsY(0) + __dataBottomY);
			int tempMaxY = (int)(convertAbsY(maxY) + __dataBottomY);
			for (int i = 0; i < maxX; i+= ((72/__printScale)/2)) {
				j = (int)(convertAbsX(i) + __dataLeftX);
				GRDrawingAreaUtil.drawLine(__drawingArea, j, minY, j, tempMaxY);
				GRDrawingAreaUtil.drawText(__drawingArea, 
					"" + ((double)i/(72/__printScale)),j, 
					minY, 0, 
					GRText.CENTER_X | GRText.BOTTOM);
			}

			int minX = (int)(convertAbsX(0) + __dataLeftX);
			int tempMaxX = (int)(convertAbsX(maxX) + __dataLeftX);
			for (int i = 0; i < maxY; i+= ((72/__printScale)/2)) {
				j = (int)(convertAbsY(i) + __dataBottomY);
				GRDrawingAreaUtil.drawLine(__drawingArea, minX, j, tempMaxX, j);
				GRDrawingAreaUtil.drawText(__drawingArea, "" + ((double)i/(72/__printScale)),minX,
					j, 0, GRText.CENTER_Y | GRText.LEFT);
			}

			// Set the data limits back
			__drawingArea.setFloatLineDash(null, 0);
		}
		setAntiAlias(__antiAlias);		
		if (__drawPixelGrid) {
			for (int i = (int)__dataBottomY; i < __totalDataHeight; i+= 20) {
				GRDrawingAreaUtil.setColor(__drawingArea, GRColor.green);
				GRDrawingAreaUtil.drawLine(__drawingArea, __dataLeftX, i, __totalDataWidth, i);
				for (int j = i + 5; j < (i + 20); j += 5) {
					GRDrawingAreaUtil.setColor(__drawingArea, GRColor.yellow);
					GRDrawingAreaUtil.drawLine(__drawingArea, __dataLeftX, j, __totalDataWidth, j);
				}
			}
		}
		setAntiAlias(__antiAlias);
		if (__drawMargin) {
			__drawingArea.setFloatLineDash(__dashes, 0);		
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.cyan);			

			double leftX = __pageFormat.getImageableX() / __printScale;
			double topY = (__pageFormat.getHeight() - __pageFormat.getImageableY()) / __printScale;
			double rightX = (leftX + __pageFormat.getImageableWidth()/__printScale) - 1;
			double bottomY = ((__pageFormat.getHeight() - (__pageFormat.getImageableY() 
					+ __pageFormat.getImageableHeight()))/__printScale) + 1;
			leftX = convertAbsX(leftX) + __dataLeftX;
			topY = convertAbsY(topY) + __dataBottomY;
			rightX = convertAbsX(rightX) + __dataLeftX;
			bottomY = convertAbsY(bottomY) + __dataBottomY;

			GRDrawingAreaUtil.drawLine(__drawingArea, leftX, topY, leftX, bottomY);
			GRDrawingAreaUtil.drawLine(__drawingArea, rightX, topY, rightX, bottomY);
			GRDrawingAreaUtil.drawLine(__drawingArea, leftX, topY, rightX, topY);
			GRDrawingAreaUtil.drawLine(__drawingArea, leftX, bottomY, rightX, bottomY);

			__drawingArea.setFloatLineDash(null, 0);
		}
		setAntiAlias(__antiAlias);
		if (true && !__savingNetwork) {
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.yellow);			

			double leftX = 0;
			double topY = (__pageFormat.getHeight() / __printScale);
			double rightX = (__pageFormat.getWidth() / __printScale);
			double bottomY = 0;
			leftX = convertAbsX(leftX) + __dataLeftX;
			topY = convertAbsY(topY) + __dataBottomY - 1;
			rightX = convertAbsX(rightX) + __dataLeftX - 1;
			bottomY = convertAbsY(bottomY) + __dataBottomY;

			GRDrawingAreaUtil.drawLine(__drawingArea, leftX, topY, leftX, bottomY);
			GRDrawingAreaUtil.drawLine(__drawingArea, rightX, topY, rightX, bottomY);
			GRDrawingAreaUtil.drawLine(__drawingArea, leftX, topY, rightX, topY);
			GRDrawingAreaUtil.drawLine(__drawingArea, leftX, bottomY, rightX, bottomY);
		}

		// Make sure the reference window represents the current status of this window
		if ( __referenceJComponent != null ) {
			__referenceJComponent.forceRepaint();
		}
		__forceRefresh = false;
	}

	if (!__printingNetwork && !__savingNetwork && !__printingScreen && !__savingScreen) {
		// Draw the border lines that separate the drawing area from the rest of the GUI.

		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		GRDrawingAreaUtil.drawLine(__drawingArea,
			__screenLeftX, 
			__screenBottomY, 
			__screenLeftX, 
			__screenBottomY + __screenDataHeight);
		GRDrawingAreaUtil.drawLine(__drawingArea,
			__screenLeftX,
			__screenBottomY + __screenDataHeight,
			__screenLeftX + __screenDataWidth,
			__screenBottomY + __screenDataHeight);			

		// The lower- and right-side lines need to be drawn with a
		// width of 2 so that they appear, otherwise they are just
		// the other side of drawing are and not visible.  The top-
		// and left-side lines appear fine normally.
		GRDrawingAreaUtil.setLineWidth(__drawingArea, 2);
		GRDrawingAreaUtil.drawLine(__drawingArea,
			__screenLeftX + __screenDataWidth, 
			__screenBottomY,
			__screenLeftX + __screenDataWidth, 
			__screenBottomY + __screenDataHeight);		
		GRDrawingAreaUtil.drawLine(__drawingArea,
			__screenLeftX, 
			__screenBottomY,
			__screenLeftX + __screenDataWidth, 
			__screenBottomY);
		GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
	}		
	
	setAntiAlias(__antiAlias);	
	// Only show the double buffered image to screen if not printing
	if (!__printingNetwork && !__printingScreen && !__savingNetwork && !__savingScreen) {
		showDoubleBuffer(g);
	}
	else if (__printingScreen || __savingScreen) {
		return;
	}
	else {
		// return here because nodes can't ever be in drag when printing occurs.
		__drawingArea.setDataLimits(__holdLimits);
		__drawingArea.setDrawingLimits(new GRLimits(
			0, 0, getBounds().width, getBounds().height),
			GRUnits.DEVICE, GRLimits.DEVICE);
		scaleUnscalables();
		return;
	}
	setAntiAlias(__antiAlias);
	// If a node is currently being dragged around the screen, draw the
	// outline of the table on top of the double-buffer
	if (__nodeDrag) {
		drawNodesOutlines(g);
	}	
	else if (__legendDrag) {
		// force the graphics context to be the on-screen one, not the double-buffered one
		forceGraphics(g);		
		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		GRDrawingAreaUtil.drawLine(__drawingArea, 
			__mouseDataX - __xAdjust, 
			__mouseDataY - __yAdjust, 
			__legendDataLimits.getWidth() + __mouseDataX - __xAdjust, 
			__mouseDataY - __yAdjust);
		GRDrawingAreaUtil.drawLine(__drawingArea, 
			__mouseDataX - __xAdjust, 
			__mouseDataY + __legendDataLimits.getHeight() - __yAdjust,
			__legendDataLimits.getWidth() + __mouseDataX - __xAdjust,
			__mouseDataY + __legendDataLimits.getHeight() - __yAdjust);
		GRDrawingAreaUtil.drawLine(__drawingArea, 
			__mouseDataX - __xAdjust, 
			__mouseDataY - __yAdjust,
			__mouseDataX - __xAdjust, 
			__mouseDataY + __legendDataLimits.getHeight() - __yAdjust);
		GRDrawingAreaUtil.drawLine(__drawingArea,
			__mouseDataX + __legendDataLimits.getWidth() - __xAdjust,	
			__mouseDataY - __yAdjust, 
			__mouseDataX + __legendDataLimits.getWidth() - __xAdjust,
			__mouseDataY + __legendDataLimits.getHeight() - __yAdjust);
	}
	else if (__drawingBox) {
		g.setXORMode(Color.white);
		forceGraphics(g);
		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.cyan);
		GRDrawingAreaUtil.drawLine(__drawingArea, __dragStartX, __dragStartY, __dragStartX, __currDragY);
		GRDrawingAreaUtil.drawLine(__drawingArea, __currDragX, __dragStartY, __currDragX, __currDragY);
		GRDrawingAreaUtil.drawLine(__drawingArea, __dragStartX, __dragStartY, __currDragX, __dragStartY);
		GRDrawingAreaUtil.drawLine(__drawingArea, __dragStartX, __currDragY, __currDragX, __currDragY);
	}
}

/**
Returns whether the network is displayed in StateModGUI or not.
@return true if the network is in StateModGUI.
*/
public boolean inStateModGUI() {
	return __parent.inStateModGUI();
}

/**
Sets up a print job and submits it.
*/
public void print()
{
	boolean useOldCode = false;
	try {
	    if ( useOldCode ) {
	    	PrinterJob printJob = PrinterJob.getPrinterJob();
	    	printJob.setPrintable(this, __pageFormat);
	    	PrintUtil.print(this, __pageFormat);
	    }
	    else {
	    	// Create a new StateMod_Network_JComponent that is isolated from the interactive plotting.
	    	// It is OK to use the same network because printing won't change anything
	    	// Use the current page layout for the settings
	        StateMod_Network_JComponent networkPrintable = new StateMod_Network_JComponent(getNetwork(),
	        	__parent.getSelectedPageLayout() );
	        networkPrintable.initializeForPrinting();
	        // Get the attributes needed for the printer job
	        networkPrintable.initializeForNetworkPageLayout ( __parent.getSelectedPageLayout() );
	        String orientation = __parent.getSelectedOrientation();
	        String paperSizeFromLayout = __parent.getSelectedPaperSize();
	        if ( paperSizeFromLayout.indexOf(" " ) > 0 ) {
	        	// Have Size - hxw
	        	paperSizeFromLayout = StringUtil.getToken(paperSizeFromLayout, " ", 0, 0).trim();
	        }
	        double margin = networkPrintable.getMargin();
	        // Open the printer job, which will display the print dialog and allow the user to change
	        // settings (mostly they should just pick the printer.
            new GraphicsPrinterJob (
            	networkPrintable, // Current object is printable
                "Network",
                null, // printer name - user will select
                null, //PrintUtil.lookupStandardMediaSize(paperSizeFromLayout),
                null, // paper source
                orientation, // page orientation
                margin, // left margin
                margin, // right
                margin, // top
                margin, // bottom
                null, // print file
                true ); // show print configuration dialog	
	    }
	}
	catch (Exception e) {
		String routine = "StateMod_Network_JComponent.print()";
		Message.printWarning(1, routine, "Error printing network (" + e + ").");
		Message.printWarning(3, routine, e);
	}
}

/**
Prints a page.
@param g the Graphics context to which to print.
@param pageFormat the pageFormat to use for printing.
@param pageIndex the index of the page to print.
@return Printable.NO_SUCH_PAGE if no page should be printed, or 
Printable.PAGE_EXISTS if a page should be printed.
*/
public int print(Graphics g, PageFormat pageFormat, int pageIndex)
{	String routine = getClass().getName() + ".print";
	if (pageIndex > 0) {
		return NO_SUCH_PAGE;
	}
	// Save the graphics and page format for use elsewhere in this class
	Graphics2D g2d = (Graphics2D)g;
	setGraphics(g2d);
	this.__pageFormat = pageFormat;
	// Set the drawing area to the imageable area, which is determined from the pageFormat from
	// the PrinterJob, etc., that controls the call to this method
    double pageHeight = pageFormat.getHeight();
    double imageablePageHeight = pageFormat.getImageableHeight();
    double pageWidth = pageFormat.getWidth();
    double imageablePageWidth = pageFormat.getImageableWidth();
    double imageableX = pageFormat.getImageableX();
    double imageableY = pageFormat.getImageableY();
    Message.printStatus ( 2, routine, "Page dimensions are: width=" + pageWidth + " height=" + pageHeight );
    Message.printStatus ( 2, routine, "Imageable page dimensions are: width=" + imageablePageWidth + " height=" + imageablePageHeight );
    Message.printStatus ( 2, routine, "Imageable origin: X=" + imageableX + " Y=" + imageableY );
	// Set the device (page limits)
    GRLimits deviceLimits = new GRLimits(0,0,pageWidth,pageHeight);
    setLimits ( deviceLimits );
    // Set the drawing limits on the page to the imageable area...
	GRLimits drawingLimits = new GRLimits(
		imageableX, (pageHeight - imageableY - imageablePageHeight),
		(imageableX + imageablePageWidth), (pageHeight - imageableY) );
	this.__drawingArea = new GRJComponentDrawingArea(
		this, "StateMod_Network DrawingArea", GRAspectType.TRUE, 
		drawingLimits, GRUnits.DEVICE, GRLimits.DEVICE, getDataLimits());
	Message.printStatus(2, routine, "Print drawing area limits (from printer imageable area): " + drawingLimits );
	Message.printStatus(2, routine, "Print data limits (from network data): " + getDataLimits() );
	Message.printStatus(2, routine, "Print font: " + this.__drawingArea.getFont() );
	StateMod_NodeNetwork network = getNetwork();
	// Calculate the data limits necessary to maintain aspect of the data and fit the page for full scale
	this.__drawingAreaFullScale = createDrawingAreaFullScale ( network, getSelectedPageLayoutPropList() );
	// Calculate the data limits necessary to maintain aspect of the data and fit the page
	// Use the actual network data
	this.__drawingArea.setDataLimits( calculateDataLimitsForMedia ( getDrawingArea().getDrawingLimits(),
		new GRLimits(network.getLX(),network.getBY(),network.getRX(),network.getTY()),
		network.getEdgeBuffer(), true ) );
    // Set the node icon size based on the scale.
    
    // Set the font size for the scale...
    // TODO SAM 2011-07-08 Only full scale printing currently is supported
	//int fontSize = this.__drawingArea.calculateFontSize(g2d, this.__fontSizePixels);
	
	// TODO SAM 2011-07-05 Adjust the drawing limits accordingly to center on the imageable area when
	// the selected page size does not match the layout size
	// For full-sized printing it should fit as is with no scale adjustments
	
	// This is from TSGraphJComponent.paint(), which contains inline comments to explain.
	// The imageable Y is the first Java pixel (going DOWN) at which drawing can occur.
	// It is therefore the LAST GR=package pixel at which drawing can occur.
	// The bottom margin of the graph is already enforced by setting the size of the drawing area.
	// This makes sure the graph is shifted to fit in the printable area of the page.
	//double transY = pageFormat.getImageableY();
	//g2d.translate(0, transY);
	// The paint() method and subsequent calls will pick up on __printingNetwork = true
	// and adjust accordingly
	paint(g2d);
	
	/*
	double hold = __currNodeSize;
	double pct = 72.0 / (double)__dpi;
	setPrintNodeSize((double)(__currNodeSize / pct));
	__legendNodeDiameter = hold;
	Graphics2D g2d = (Graphics2D)g;
	// Set for the GRDevice because we will temporarily use that to do the drawing...	

	__forceRefresh = true;
	
	Message.printStatus(2, "", "Printing network, printScale=" + __printScale);
	
	if (!__printingScreen) {
		g2d.scale(__printScale, __printScale);  // This doesn't appear to make any difference? - CEN
		// Not sure RepaintManger is effective in this case
        RepaintManager currentManager = RepaintManager.currentManager(this);
        currentManager.setDoubleBufferingEnabled(false);
        // Don't need? because printNetwork and printScreen both call forceRepaint()
        //paint(g2d);
        // __tempBuffer was created for purpose of printing and already has content
		g2d.drawImage(__tempBuffer, 0, 0, null);
        currentManager.setDoubleBufferingEnabled(true);
	}
	else {
		double transX = 0;
		double transY = 0;
	
		if (!StringUtil.startsWithIgnoreCase(PrintUtil.pageFormatToString(pageFormat), "Plotter")) {
			// TODO SAM 2009-02-10 What does the following accomplish?
			if (pageFormat.getOrientation() == PageFormat.LANDSCAPE) {
				transX = pageFormat.getImageableX() * (1 / 1);
				transY = pageFormat.getImageableY() * (1 / 1);
			}
			else {
				transX = pageFormat.getImageableX() * (1 / 1);
				transY = pageFormat.getImageableY() * (1 / 1);
			}
		}

		double iw = pageFormat.getImageableWidth();
		double w = pageFormat.getWidth();
		double ih = pageFormat.getImageableHeight();
		double h = pageFormat.getHeight();

		double scale = 0;

		if (w > h) {
			scale = iw / getBounds().width;
		}
		else {
			scale = ih / getBounds().height;
		}

		g2d.translate(transX, transY);
		paint(g2d);
	}*/
	__printCount++;

	return PAGE_EXISTS;
}

/**
Prints the entire network.
*/
protected void printNetwork()
{	String routine = "StateMod_Network_JComponent.printNetwork";
	Message.printStatus( 2, routine, "Printing entire network" );
	if ( !__useOldPrinting ) {
		// New printing creates a new instance of the component so that printing does not intermingle
		// with the interactive rendering
		new ResponseJDialog ( __parent, "Print Network",
			"You must select a printer and page size that match the network page layout.\n" +
			"The printer dialog will take a few seconds to display.",
				ResponseJDialog.OK);
		print();
		return;
	}
	// Save current settings...
	boolean drawMargin = __drawMargin;
	double zoom = __zoomPercentage;
	// Setup for printing...
	RepaintManager currentManager = printNetworkSetup ();
	// Print...
	print();
	// Now shift back to normal interaction
	// TODO SAM 2011-07-03 Need to separate out printing completely so it does not
	// mess with interactive network editor.
	__tempBuffer = null;
	__printingNetwork = false;
    currentManager.setDoubleBufferingEnabled(true);
	System.gc();
	__printCount = 0;

	__ignoreRepaint = true;

	// go back to the previous zoom level
	if (zoom > 100) {
		for (double d = 100; d < zoom; d *= 2) {
			zoomIn();
		}
	}
	else if (zoom == 100) {}
	else {
		for (double d = 100; d > zoom; d /= 2) {
			zoomOut();
		}
	}
	
	__ignoreRepaint = false;

	__drawMargin = drawMargin;
	__antiAlias = __antiAliasSetting;
	forceRepaint();
}

/**
Performs steps prior to printing the entire network.  This can be called in batch mode before
printing the network.
*/
public RepaintManager printNetworkSetup ()
{
    RepaintManager currentManager = RepaintManager.currentManager(this);
    currentManager.setDoubleBufferingEnabled(false);
	__drawMargin = false;
//	__antiAlias = false;
	__antiAlias = true; // On for printing, to look better

	__printCount = 0;
	__tempBuffer = new BufferedImage(__totalBufferWidth, __totalBufferHeight, BufferedImage.TYPE_4BYTE_ABGR);
	__bufferGraphics = (Graphics2D)(__tempBuffer.createGraphics());
	// TODO SAM 2011-07-04 Had to put the following in for batch processing
	// because graphics is used for checking font size, etc.
	if ( _graphics == null ) {
		_graphics = __bufferGraphics;
	}
	__printingNetwork = true;

	// Make sure that none of the nodes are selected so they don't print blue (print without selection).  
	for (int i = 0; i < __nodes.length; i++) {
		__nodes[i].setSelected(false);
	}
	zoomOneToOne();
	// Redraw with printer settings...
	forceRepaint();
	return currentManager;
}

/**
Prints information about the nodes in the network to status level 2.  Used for debugging.
*/
private void printNetworkInfo() {
	if (__network == null) {
		return;
	}
	List<String> v = __network.getNodeCountsVector();
	Message.printStatus(2, "StateMod_Network_JComponent.printNetworkInfo", "--- Network Node Summary ---");
	for ( String s: v ) {	
		Message.printStatus(2, "StateMod_Network_JComponent.printNetworkInfo", "" + s);
	}
}

/**
Prints whatever is visible on the screen, scaled to fit the default piece of paper.
*/
protected void printScreen() {
	__printCount = 0;
	double leftX = __pageFormat.getImageableX() / __printScale;
	double topY = (__pageFormat.getHeight() - __pageFormat.getImageableY()) / __printScale;
	double rightX = (leftX + __pageFormat.getImageableWidth() / __printScale) - 1;
	double bottomY = ((__pageFormat.getHeight() - (__pageFormat.getImageableY() 
		+ __pageFormat.getImageableHeight())) / __printScale) + 1;		
	__tempBuffer = new BufferedImage((int)(rightX - leftX),
		(int)(topY - bottomY), BufferedImage.TYPE_4BYTE_ABGR);
	__bufferGraphics = (Graphics2D)(__tempBuffer.createGraphics());
	__printingScreen = true;
	forceRepaint();
	print();
	__tempBuffer = null;
	__printingScreen = false;
	System.gc();
	__printCount = 0;
	forceRepaint();
}

/**
Processes nodes that were read in from an XML file and fills in their related
node information so they can be drawn on the screen.  This method is called 
the first time annotations are drawn after a network has been read.
*/
private void processAnnotationsFromNetwork()
{
	PropList p = null;
	for ( HydrologyNode node : this.__annotations ) {
		p = (PropList)node.getAssociatedObject();
		String text = p.getValue("Text");
		String point = p.getValue("Point");
		int index = point.indexOf(",");
		String xs = point.substring(0, index);
		String ys = point.substring(index + 1, point.length());
		String position = p.getValue("TextPosition");
		double x = (Double.valueOf(xs)).doubleValue();
		double y = (Double.valueOf(ys)).doubleValue();

		int fontSize = Integer.valueOf(p.getValue("OriginalFontSize")).intValue();
		fontSize = calculateScaledFont(p.getValue("FontName"), p.getValue("FontStyle"), fontSize, false);

		GRLimits limits = GRDrawingAreaUtil.getTextExtents(
			__drawingArea, text, GRUnits.DEVICE,
			p.getValue("FontName"), p.getValue("FontStyle"), fontSize);	
		double w = convertDrawingXToDataX(limits.getWidth());
		double h = convertDrawingYToDataY(limits.getHeight());

		// Calculate the actual limits for the from the lower-left  corner
		// to the upper-right, in order to know when the text has been 
		// clicked on (for dragging, or popup menus).
	
		if (position.equalsIgnoreCase("UpperRight")) {
			node.setPosition(x, y, w, h);
		}
		else if (position.equalsIgnoreCase("Right")) {
			node.setPosition(x, y - (h / 2), w, h);
		}
		else if (position.equalsIgnoreCase("LowerRight")) {
			node.setPosition(x, y - h, w, h);
		}
		else if (position.equalsIgnoreCase("Below") || position.equalsIgnoreCase("BelowCenter")) {
			node.setPosition(x - (w / 2), y - h, w, h);
		}
		else if (position.equalsIgnoreCase("LowerLeft")) {
			node.setPosition(x - w, y - h, w, h);
		}
		else if (position.equalsIgnoreCase("Left")) {
			node.setPosition(x - w, y - (h / 2), w, h);
		}
		else if (position.equalsIgnoreCase("UpperLeft")) {
			node.setPosition(x - w, y, w, h);
		}
		else if (position.equalsIgnoreCase("Above") || position.equalsIgnoreCase("AboveCenter")) {
			node.setPosition(x - (w / 2), y, w, h);
		}
		else if (position.equalsIgnoreCase("Center")) {
			node.setPosition(x - (w / 2), y - (h / 2), w, h);
		}
	}
}

/**
Reads a network from a makenet file.
@param nodeDataProvider the data provider to use for helping to read the file.
@param filename the name of the makenet file to read.
*/
protected void readMakenetFile(	StateMod_NodeDataProvider nodeDataProvider, String filename ) {
	__network = new StateMod_NodeNetwork();
	__network.readMakenetNetworkFile( nodeDataProvider,	filename, true );
	__annotations = __network.getAnnotationList();
	__processAnnotations = true;
	if (__annotations == null) {
		__annotations = new Vector<HydrologyNode>();
	}	
	__links = __network.getLinkList();
	printNetworkInfo();
	buildNodeArray();
	findMaxReachLevel();	
}

/**
Redoes one change operation.
*/
protected void redo() {
	if (__undoPos == __undoOperations.size() || !__editable) {
		return;
	}

	StateMod_Network_UndoData data = (StateMod_Network_UndoData)__undoOperations.get(__undoPos);
	__undoPos++;
	__nodes[data.nodeNum].setX(data.newX);
	__nodes[data.nodeNum].setY(data.newY);

	if (data.otherNodes != null) {
		for (int i = 0; i < data.otherNodes.length; i++) {
			__nodes[data.otherNodes[i]].setX(data.newXs[i]);
			__nodes[data.otherNodes[i]].setY(data.newYs[i]);
		}
	}
	
	forceRepaint();

	if (__undoPos == __undoOperations.size()) {
		__parent.setRedo(false);
	}
	else {
		__parent.setRedo(true);
	}
	__parent.setUndo(true);
}

/**
Removes all links that involve the node with the given ID.  This is called when
a node is deleted so that links don't try to point to a nonexistent node.
@param id the ID of the node that was deleted and which should not be in any links.
*/
private void removeIDFromLinks(String id) {
	String routine = "StateMod_Network_JComponent.removeIDFromLinks";

	boolean found = false;

	if (__links == null) {
		return;
	}

	int size = __links.size();
	PropList p = null;
	String id1 = null;
	String id2 = null;
	for (int i = size - 1; i >= 0; i--) {
		found = false;
		p = __links.get(i);
		id1 = p.getValue("FromNodeID");
		id2 = p.getValue("ToNodeID");

		if (id1.equals(id)) {
			found = true;
		}
		else if (id2.equals(id)) {
			found = true;
		}

		if (found) {
			__links.remove(i);
			Message.printWarning(2, routine, 
				"ID '" + id + "' found in a link.  The link will no longer be drawn.");
		}
	}
}	

/**
Saves the entire network to an image file.
*/
protected void saveNetworkAsImage() {
	__savingNetwork = true;
	__tempBuffer = new BufferedImage(
		(int)(__totalBufferWidth * (72.0 / (double)__dpi)), 
		(int)(__totalBufferHeight * (72.0 / (double)__dpi)),
		BufferedImage.TYPE_4BYTE_ABGR);
	__bufferGraphics = (Graphics2D)(__tempBuffer.createGraphics());
	forceRepaint();
	new RTi.Util.GUI.SaveImageGUI(__tempBuffer, __parent);
	__tempBuffer = null;
	__savingNetwork = false;
}

/**
Saves what is currently visible on screen to a graphic file by dumping the canvas area.
*/
protected void saveScreenAsImage()
throws IOException
{	boolean useNewCode = true;
	if ( useNewCode ) {
		String last_directory = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = JFileChooserFactory.createJFileChooser(last_directory);
		fc.setDialogTitle ( "Save Visible Network as Image File" );
		fc.setAcceptAllFileFilterUsed ( false );
		SimpleFileFilter jpg_sff = new SimpleFileFilter("jpeg", "JPEG Image File" );
		fc.addChoosableFileFilter ( jpg_sff );
		SimpleFileFilter png_sff = new SimpleFileFilter("png", "PNG Image File" );
		fc.setFileFilter(png_sff);
		if ( fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION ) {
			// Canceled...
			return;
		}
		// Else figure out the file format and location and then do the save...
		last_directory = fc.getSelectedFile().getParent();
		String path = fc.getSelectedFile().getPath();
		JGUIUtil.setLastFileDialogDirectory(last_directory);
		//__parent.setWaitCursor(true);
		if (fc.getFileFilter() == png_sff) {
		    path = IOUtil.enforceFileExtension ( path, "png" );
		}
		// Leave *.jpeg alone but by default enforce *.jpg
		else if ( (fc.getFileFilter() == jpg_sff) && !StringUtil.endsWithIgnoreCase(path,"jpeg")) {
            path = IOUtil.enforceFileExtension ( path, "jpg" );
        }
		// The following will examine the extension and save as PNG or JPG accordingly
 		saveAsFile(path);
	}
	else {
		__savingScreen = true;
		__tempBuffer = new BufferedImage(getBounds().width,
		getBounds().height, BufferedImage.TYPE_4BYTE_ABGR);
		__bufferGraphics = (Graphics2D)(__tempBuffer.createGraphics());
		forceRepaint();
		new RTi.Util.GUI.SaveImageGUI(__tempBuffer, __parent);
		__tempBuffer = null;
		__savingScreen = false;
	}
}

// FIXME SAM 2008-12-11 Need a way to save the selected filename so that it can be the default
// for the next save.
/**
Saves the network as an XML file.
@param filename the name of the file to put into the JFileChooser.  If null, it will be ignored. 
*/
protected void saveXML(String filename) {
	String lastDirectorySelected = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc = JFileChooserFactory.createJFileChooser( lastDirectorySelected);
	fc.setDialogTitle("Save XML Network File");
	SimpleFileFilter netff = new SimpleFileFilter("net", "Network Files");
	fc.addChoosableFileFilter(netff);
	fc.setFileFilter(netff);
	fc.setDialogType(JFileChooser.SAVE_DIALOG);	

	if (filename != null) {
		int index = filename.lastIndexOf(File.separator);
		if (index > -1) {
			filename = filename.substring(index + 1);
		}
		fc.setSelectedFile(new File(filename));
	}

	int retVal = fc.showSaveDialog(this);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return;
	}
	
	String currDir = (fc.getCurrentDirectory()).toString();
	
	if (!currDir.equalsIgnoreCase(lastDirectorySelected)) {
		JGUIUtil.setLastFileDialogDirectory(currDir);
	}
	String selectedFilename = fc.getSelectedFile().getPath();

	selectedFilename = IOUtil.enforceFileExtension(selectedFilename, "net");

	// These are the limits based on the 
	GRLimits limits = new GRLimits(
		__dataLeftX, __dataBottomY, __totalDataWidth + __dataLeftX, __totalDataHeight + __dataBottomY);
	
	PropList p = new PropList("");
	p.set("ID=\"Main\"");
	p.set("PaperSize=\"" + PrintUtil.pageFormatToString(__pageFormat) + "\"");
	p.set("PageOrientation=\"" + PrintUtil.getOrientationAsString(__pageFormat) + "\"");
	p.set("NodeLabelFontPointSize=" + __printFontSizePixels);
	p.set("NodeSize=" + __nodeSizeFullScale);
	double [] edgeBuffer = { 0, 0, 0, 0 };
	try {
		__network.writeXML(selectedFilename, limits, __parent.getLayouts(), __annotations, __links,
			__legendDataLimits, edgeBuffer );
	}
	catch (Exception e) {
		String routine = "StateMod_Network_JComponent.saveXML()";
		Message.printWarning(1, routine, "Error saving network XML file.");
		Message.printWarning(2, routine, e);
	}
	printNetworkInfo();
}

/**
Scales things that are drawn that are not scaled nicely by the GR package.
In particular, this does font scaling, which needs REVISIT ed anyways!!
*/
private void scaleUnscalables() {
	double scale = 0;
	if (__fitWidth) {
		double pixels = __dpi * (int)(__pageFormat.getWidth() / 72);
		double pct = (getBounds().width / pixels);
		double width = __totalDataWidth * pct;
		scale = width / __screenDataWidth;
	}
	else {
		double pixels = __dpi * (int)(__pageFormat.getHeight() / 72);
		double pct = (getBounds().height / pixels);
		double height = __totalDataHeight * pct;
		scale = height / __screenDataHeight;
	}
	__fontSizePixels = (int)(__printFontSizePixels * scale);
	__fontSizePoints = __drawingArea.calculateFontSize(__fontSizePixels);
	double temp = (double)__fontSizePoints * ((double)__dpi / 72.0);
	temp += 0.5;
	__printFontSizePoints = (int)temp + 2;
	__lineThickness = (int)(__printLineThickness * scale);
}

/**
Sets the visible data limits.  No additional computations are done.
@param dataLimits the data limits for the visible network.
*/
private void setDataLimits ( GRLimits dataLimits )
{
	__dataLimits = dataLimits;
}

// TODO SAM 2010-12-29 need to change the limits when a node is added or deleted
// - not a big deal right now because the max is used for zooming
/**
Sets the maximum data limits for the data.
@param dataLimitsMax the maximum data limits for the network.
*/
private void setDataLimitsMax ( GRLimits dataLimitsMax )
{
	__dataLimitsMax = dataLimitsMax;
}

/**
Sets whether the network and its nodes are dirty.  Usually will be called with a parameter of false
after a save has occurred.
@param dirty whether to mark everything dirty or not.
*/
protected void setDirty(boolean dirty) {
//	Message.printStatus(1, "", "SetDirty: " + dirty);
	setNetworkChanged ( dirty );
	for (int i = 0; i < __nodes.length; i++) {
		// FIXME SAM 2008-12-11 Why is the following always false?
		__nodes[i].setDirty(false);
	}
}

/**
Sets the drawing area to be used with this device.
@param drawingArea the drawingArea to use with this device.
*/
protected void setDrawingArea(GRJComponentDrawingArea drawingArea) {
	__drawingArea = drawingArea;
}

/**
Sets the mode the network should be in in regard to how it responds to mouse presses.
@param mode the mode the network should be in in regard to how it responds to mouse presses.
*/
protected void setMode(int mode) {
	__networkMouseMode = mode;
}

/**
Sets the network to be used.  Called by the code that has read in a network from an XML file.
@param network the network to use.
@param dirty indicates whether the network should be marked dirty (changed) - use when?
@param doAll indicates whether all initialization work should occur (internal lists to help with processing).
*/
protected void setNetwork ( StateMod_NodeNetwork network, boolean dirty, boolean doAll ) {
	if (__network == null && network != null) {
		// new network
		__links = network.getLinkList();
		__annotations = network.getAnnotationList();
	}

	__network = network;
	__processAnnotations = true;
	if (__annotations == null) {
		__annotations = new Vector<HydrologyNode>();
	}	
	printNetworkInfo();
	setNetworkChanged ( dirty );
	if (!doAll) {
		return;
	}
	buildNodeArray();
	findMaxReachLevel();	
	__network.setLinkList(__links);
	__network.setAnnotationList(__annotations);
	if ( __referenceJComponent != null ) {
		// Reference network is not used for printing
		__referenceJComponent.setNetwork(__network );
		__referenceJComponent.setNodesArray(__nodes);
	}
}

/**
Set whether the network has been changed, in particular nodes added/deleted.
*/
public void setNetworkChanged ( boolean networkChanged )
{
	__networkChanged = networkChanged;
}

/**
Sets the size of the nodes (in pixels) at the 1:1 zoom level.
@param size the size of the nodes.
*/
public void setNodeSize(double size) {
	__nodeSizeFullScale = (int)size;
	setPrintNodeSize(size);
}

/**
Sets the orientation of the paper.
@param orientation either "Landscape" or "Portrait".
*/
public void setOrientation(String orientation) {
	if (_graphics == null) {
		__holdPaperOrientation = orientation;
		return;
	}
	else {
		__holdPaperOrientation = null;
	}
	try {
		__pageFormat = PrintUtil.getPageFormat( PrintUtil.pageFormatToString(__pageFormat));
		if (orientation.trim().equalsIgnoreCase("Landscape")) {
			PrintUtil.setPageFormatOrientation(__pageFormat, PageFormat.LANDSCAPE);
		}
		else {
			PrintUtil.setPageFormatOrientation(__pageFormat, PageFormat.PORTRAIT);
		}
		double margin = getMargin();
		PrintUtil.setPageFormatMargins(__pageFormat, margin, margin, margin, margin );
		int hPixels = (int)(__pageFormat.getWidth() / __printScale);
		int vPixels = (int)(__pageFormat.getHeight() / __printScale);
		setTotalSize(hPixels, vPixels);
		calculateDataLimits();
	
		zoomOneToOne();
		forceRepaint();
	}
	catch (Exception e) {
		String routine = "StateMod_Network_JComponent.setOrientation";
		Message.printWarning(1, routine, "Error setting orientation.");
		Message.printWarning(2, routine, e);
	}
}

/**
Sets the pageformat to use.
@param pageFormat the pageFormat to use.
*/
public void setPageFormat(PageFormat pageFormat) {
	__pageFormat = pageFormat;
}

/**
Changes the paper size.
@param size the size to set the paper to (see PrintUtil for a list of supported paper sizes).
*/
public void setPaperSize(String size) {
	if (_graphics == null) {
		__holdPaperSize = size;
		return;
	}
	else {
		__holdPaperSize = null;
	}
	try {
		__pageFormat = PrintUtil.getPageFormat(size);
		PrintUtil.setPageFormatOrientation(__pageFormat, PageFormat.LANDSCAPE);
		double margin = getMargin();
		PrintUtil.setPageFormatMargins(__pageFormat, margin, margin, margin, margin );
		int hPixels = (int)(__pageFormat.getWidth() / __printScale);
		int vPixels = (int)(__pageFormat.getHeight() / __printScale);
		setTotalSize(hPixels, vPixels);
		zoomOneToOne();
		forceRepaint();
	}
	catch (Exception e) {
		String routine = "StateMod_Network_JComponent.setPaperSize";
		Message.printWarning(1, routine, "Error setting paper size.");
		Message.printWarning(3, routine, e);
	}
}

/**
Sets the printing scale.  The network is designed to be set up so 
that it fits well on a certain size printed page, and this scale is used to ensure that everything fits.
<p>
By default, Java prints out everything at 72 dpi.  At this size, a Letter-sized
piece of paper could only have 792 x 612 pixels on it.  Since each node in the
network is drawn at 20 pixels high, it is possible that larger networks would run out of space.
<p>
At the same time, it should be possible to scale larger networks so that 
even then can fit on a smaller piece of paper.  Thus the use of the printing scale.  <p>
The printing scale basically adjusts the DPI at which Java prints.  A scale
of .5 would result in 144 DPI and .25 in 288 DPI.  2 would result in 36 DPI.<P>
.25 is a good scale for most purposes, but further adjustments may be necessary
for larger networks and smaller papers.
@param scale the printing scale.
*/
/* TODO SAM 2007-03-01 Evaluate whether needed
private void setPrintingScale(double scale) {	
	__printScale = scale;
}
*/

/**
Sets the size in points that fonts should be printed at when printed at 1:1.
@param fontSizePoints the pixel size of fonts when printed at 1:1.
@param doCalcs if true, do extra legacy calculations, if false, just set the value
*/
public void setPrintFontSize( int fontSizePoints ) {
	if (_graphics == null) {
		__holdPrintFontSize = fontSizePoints;
		return;
	}
	else {
		__holdPrintFontSize = -1;
	}
	__printFontSizePixels = fontSizePoints;
	scaleUnscalables();
	forceRepaint();
}

/**
Sets the size (in points) that nodes should be printed at.
@param nodeSizePoints the size (in points) of nodes when printed at 1:1.
*/
public void setPrintNodeSize ( double nodeSizePoints )
{	String routine = getClass().getName() + ".setPrintNodeSize";
	if ( __printingNetwork ) {
		Message.printStatus ( 2, routine, "Setting print node size=" + nodeSizePoints + " points." );
	}
	if (_graphics == null ) {
		__holdPrintNodeSize = nodeSizePoints;
		return;
	}
	else {
		__holdPrintNodeSize = -1;
	}
	
	__currNodeSize = nodeSizePoints;
	if (nodeSizePoints < 1) {
		nodeSizePoints = 1;
	}
	__legendNodeDiameter = nodeSizePoints;
	double diam = 0;
	if ( __printingNetwork ) {
		// TODO SAM 2011-07-09 Some of this method code seems not right.  The node expects to have the size
		// in data coordinates, which is what the following code does.
		// The diam seems to be what should be calculated in data units, but why is it not set for the
		// node icon diameter?
		// A more straightforward way of setting the size for rendering?
		// For now try the following.  When printing, the node diameter size (points) agrees with the
		// drawing limits units, so can use the standard method to convert to data units
		//nodeSizePoints = getDrawingArea().scaleXData(nodeSizePoints);
	}
	if (__fitWidth) {
		diam = convertDrawingXToDataX(nodeSizePoints);
	}
	else {
		diam = convertDrawingYToDataY(nodeSizePoints);
	}
	if ( __printingNetwork ) {
		Message.printStatus ( 2, routine, "Node size drawing=" + nodeSizePoints + ", data=" + diam );
	}
	for (int i = 0; i < __nodes.length; i++) {
		__nodes[i].setIconDiameter((int)(nodeSizePoints));
		__nodes[i].setSymbol(null);
		__nodes[i].setBoundsCalculated(false);
		__nodes[i].setDataDiameter(diam);
		__nodes[i].calculateExtents(getDrawingArea());
	}
	forceRepaint();
}

/**
Sets the reference display to use in conjunction with this display.
@param reference the reference display that goes along with this network display.
*/
protected void setReference(StateMod_NetworkReference_JComponent reference) {
	__referenceJComponent = reference;
}

/**
Set the selected network page layout properties for rendering.  This generally will either come from the
network editor UI, or from a command parameter when batch printing.
@param selectedPageLayoutPropList the page layout that should be used for rendering
*/
public void setSelectedPageLayoutPropList ( PropList selectedPageLayoutPropList )
{
	this.__selectedPageLayoutPropList = selectedPageLayoutPropList;
}

/**
Sets the total height and width of the entire network display, in device unit pixels.
@param w the total width of the entire network.
@param h the total height of the entire network.
*/
protected void setTotalSize(int w, int h) {
	// Message.printStatus(1, "", "Set Total Size: " + w + ", " + h);
	__totalBufferWidth = w;
	__totalBufferHeight = h;
}

/**
Sets up the double buffer.
*/
/* TODO SAM 2007-03-01 Evaluate whether needed
private void setupDoubleBuffering() {
	setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
	forceRepaint();
}
*/

/**
Sets the part of the network being viewed.  
@param x the left X point of the screen, in device units.
@param y the bottom Y point of the screen, in device units.
*/
protected void setViewPosition(int x, int y) {
	__screenBottomY = y;
	__screenLeftX = x;

	// make sure the view position is not moved beyond the network drawing boundaries
	if (__screenLeftX > (__dataLeftX + __totalDataWidth) - convertDrawingXToDataX(getBounds().width)) {
		__screenLeftX = (__dataLeftX + __totalDataWidth) - convertDrawingXToDataX(getBounds().width);
	}	
	if (__screenLeftX < __dataLeftX)  {
		__screenLeftX = __dataLeftX;
	}

	if (__screenBottomY > (__dataBottomY + __totalDataHeight) - convertDrawingYToDataY(getBounds().height)) {
		__screenBottomY = (__dataBottomY + __totalDataHeight) - convertDrawingYToDataY(getBounds().height);
	}	
	if (__screenBottomY < __dataBottomY) {
		__screenBottomY = __dataBottomY;
	}

	forceRepaint();
}

/**
Sets the data limits for the network from what was read in an XML file.
@param lx the left-most x value in data units.
@param by the bottom-most y value in data units.
@param w the width of the network in data units.
@param h the height of the network in data units.
*/
protected void setXMLDataLimits(double lx, double by, double w, double h) {
	GRLimits limits = new GRLimits( lx, by, lx + w, by + h);
	__dataLimits = limits;
	setDataLimitsMax ( new GRLimits(__dataLimits) );
	__drawingArea.setDataLimits(limits);
	calculateDataLimits();
}

/**
Takes a double and trims its decimal values so that it only has 6 places of precision.
@param d the double to trim.
@return the same double with only 6 places of precision.
*/
private double toSixDigits(double d) {
	String s = StringUtil.formatString(d, "%20.6f");
	Double D = Double.valueOf(s);
	return D.doubleValue();
}

/**
Converts an X value in data units to an X value in drawing units.
@param x the x value to convert.
@return the x value, converted from being scaled for data limits to being scaled for drawing units.
*/
/* TODO SAM Evaluate use
private double unconvertX(double x) {
	GRLimits data = __drawingArea.getDataLimits();
	GRLimits draw = __drawingArea.getDrawingLimits();
	double lx = data.getLeftX();
	double xAdjust = 0;
	if (lx < 0) {
		xAdjust = lx;
	}
	else {
		xAdjust = 0 - lx;
	}	
	x += xAdjust;
	double width = data.getWidth();
	double pct = x / width;
	return draw.getWidth() * pct;
}
*/

/**
Converts an Y value in data units to an Y value in drawing units.
@param y the y value to convert.
@return the y value, converted from being scaled for data limits to being scaled for drawing units.
*/
/* TODO SAM 2007-03-01 Evaluate whether needed
private double unconvertY(double y) {
	GRLimits data = __drawingArea.getDataLimits();
	GRLimits draw = __drawingArea.getDrawingLimits();
	double by = data.getBottomY();
	double yAdjust = 0;
	if (by < 0) {
		yAdjust = by;
	}
	else {
		yAdjust = 0 - by;
	}
	// now it's 0-based
	y += yAdjust;

	double height = data.getHeight();
	double pct = y / height;
	return draw.getHeight() * pct;
}
*/

/**
Undoes one change operation.
*/
protected void undo() {
	if (__undoPos == 0 || !__editable) {
		return;
	}

	__undoPos--;
	StateMod_Network_UndoData data = (StateMod_Network_UndoData)__undoOperations.get(__undoPos);
	__nodes[data.nodeNum].setX(data.oldX);
	__nodes[data.nodeNum].setY(data.oldY);

	if (data.otherNodes != null) {
		for (int i = 0; i < data.otherNodes.length; i++) {
			__nodes[data.otherNodes[i]].setX(data.oldXs[i]);
			__nodes[data.otherNodes[i]].setY(data.oldYs[i]);
		}
	}
	
	forceRepaint();
	if (__undoPos == 0) {
		__parent.setUndo(false);
	}	
	else {
		__parent.setUndo(true);
	}
	__parent.setRedo(true);
}

/**
Given a certain annotation, updates the proplist that holds the annotation
info in reaction to the annotation being moved on the screen.
@param annotation the number of the annotation in the __annotations list to have the proplist updated.
*/
private void updateAnnotationLocation(int annotation) {
	HydrologyNode node = __annotations.get(annotation);

	double x = node.getX();
	double y = node.getY();
	double w = node.getWidth();
	double h = node.getHeight();
		
	PropList p = (PropList)node.getAssociatedObject();
		
	String position = p.getValue("TextPosition");
		
	if (position.equalsIgnoreCase("UpperRight")) {
		p.setValue("Point", "" + x + "," + y);
	}
	else if (position.equalsIgnoreCase("Right")) {
		p.setValue("Point", "" + x + "," + (y + (h / 2)));
	}
	else if (position.equalsIgnoreCase("LowerRight")) {
		p.setValue("Point", "" + x + "," + (y + h));
	}
	else if (position.equalsIgnoreCase("Below") || position.equalsIgnoreCase("BelowCenter")) {
		p.setValue("Point", "" + (x + (w / 2)) + "," + (y + h));
	}
	else if (position.equalsIgnoreCase("LowerLeft")) {
		p.setValue("Point", "" + (x + w) + "," + (y + h));
	}
	else if (position.equalsIgnoreCase("Left")) {
		p.setValue("Point", "" + (x + w) + "," + (y + (h / 2)));
	}
	else if (position.equalsIgnoreCase("UpperLeft")) {
		p.setValue("Point", "" + (x + w) + "," + y);
	}
	else if (position.equalsIgnoreCase("Above") || position.equalsIgnoreCase("AboveCenter")) {
		p.setValue("Point", "" + (x + (w / 2)) + "," + y);
	}
	else if (position.equalsIgnoreCase("Center")) {
		p.setValue("Point", "" + (x + (w / 2)) + "," + (y + (h / 2)));
	}
}

/**
Updates one of the annotation nodes with location and text information stored in the passed-in node.
@param nodeNum the number of the node (in the node array) to update.
@param node the node holding information with which the other node should be updated.
*/
protected void updateAnnotation(int nodeNum, HydrologyNode node) {
	PropList p = (PropList)node.getAssociatedObject();

	HydrologyNode vNode = __annotations.get( nodeNum);
	PropList vp = (PropList)vNode.getAssociatedObject();
	vNode.setAssociatedObject(p);

	String text = p.getValue("Text");
	boolean labelChanged = false;
	if (!text.equals(vp.getValue("Text"))) {
		vp.setValue("Text", text);
		labelChanged = true;
	}

	String val = p.getValue("Point").trim();
	String position = p.getValue("TextPosition");
	String fontSize = p.getValue("OriginalFontSize");
	String fontName = p.getValue("FontName");
	String fontStyle = p.getValue("FontStyle");

	if (!val.equals(vp.getValue("Point"))
		|| labelChanged
		|| !position.equals(vp.getValue("TextPosition"))
		|| !fontSize.equals(vp.getValue("OriginalFontSize"))
		|| !fontName.equals(vp.getValue("FontName"))
		|| !fontStyle.equals(vp.getValue("FontStyle"))
		) {

		int size = Integer.valueOf(p.getValue("OriginalFontSize")).intValue();
		size = calculateScaledFont(p.getValue("FontName"), p.getValue("FontStyle"), size, false);
		GRLimits limits = GRDrawingAreaUtil.getTextExtents(	__drawingArea, text, GRUnits.DEVICE,
			p.getValue("FontName"), p.getValue("FontStyle"), size);	

		double w = convertDrawingXToDataX(limits.getWidth());
		double h = convertDrawingYToDataY(limits.getHeight());

		if (!val.equals(vp.getValue("Point"))) {
			vp.setValue("Point", val);
			vNode.setDirty(true);
		}
		if (!position.equals(vp.getValue("TextPosition"))) {
			vp.setValue("TextPosition", position);
			vNode.setDirty(true);
		}
	
		String temp = StringUtil.getToken(val, ",", 0, 0);
		double x = Double.valueOf(temp).doubleValue();
		temp = StringUtil.getToken(val, ",", 0, 1);
		double y = Double.valueOf(temp).doubleValue();

		if (position.equalsIgnoreCase("UpperRight")) {
			vNode.setPosition(x, y, w, h);
			vNode.setDirty(true);
		}
		else if (position.equalsIgnoreCase("Right")) {
			vNode.setPosition(x, y - (h / 2), w, h);
		}
		else if (position.equalsIgnoreCase("LowerRight")) {
			vNode.setPosition(x, y - h, w, h);
		}
		else if (position.equalsIgnoreCase("Below")|| position.equalsIgnoreCase("BelowCenter")) {
			vNode.setPosition(x - (w / 2), y - h, w, h);
		}
		else if (position.equalsIgnoreCase("LowerLeft")) {
			vNode.setPosition(x - w, y - h, w, h);
		}
		else if (position.equalsIgnoreCase("Left")) {
			vNode.setPosition(x - w, y - (h / 2), w, h);
		}
		else if (position.equalsIgnoreCase("UpperLeft")) {
			vNode.setPosition(x - w, y, w, h);
		}
		else if (position.equalsIgnoreCase("Above") || position.equalsIgnoreCase("AboveCenter")) {
			vNode.setPosition(x - (w / 2), y, w, h);
		}
		else if (position.equalsIgnoreCase("Center")) {
			vNode.setPosition(x - (w / 2), y - (h / 2), w, h);
		}		
	}

	if (!fontName.equals(vp.getValue("FontName"))) {
		vNode.setDirty(true);
		vp.setValue("FontName", fontName);
	}

	val = p.getValue("OriginalFontSize");
	if (!val.equals(vp.getValue("OriginalFontSize"))) {
		vNode.setDirty(true);
		vp.setValue("OriginalFontSize", val);
	}

	if (!fontStyle.equals(vp.getValue("FontStyle"))) {
		vNode.setDirty(true);
		vp.setValue("FontStyle", fontStyle);
	}
	vNode.setAssociatedObject(vp);
	forceRepaint();
}

/**
Checks to see if a node is within the limits defined the passed-in GRLimits.
@param rect GRLimits to check if the node is within.
@param node the node to check to see if it is within the node.
@return true if the node is within the limits, false if not.
*/
private boolean within(GRLimits rect, HydrologyNode node) {
	double x = node.getX();
	double y = node.getY();
	double diam = node.getDataDiameter() / 2;
	if (rect.contains(x, y)) {
		return true;
	}

	if (x < rect.getLeftX()) {
		x += diam;
	}
	else if (x > rect.getRightX()) {
		x -= diam;
	}

	if (y < rect.getBottomY()) {
		y += diam;
	}
	else if (y > rect.getTopY()) {
		y -= diam;
	}

	if (rect.contains(x, y)) {
		return true;
	}

	return false;
}

/**
Zooms to the specified limits.
*/
protected void zoomCenterOn ( GRLimits limits )
{
	setDataLimits ( limits );
	forceRepaint();
}

/**
Zooms in the current view to twice the current view.
*/
protected void zoomIn() {
	if ((__totalDataWidth / __screenDataWidth) > 17) {
		return;
	}

	double cx = __screenLeftX + (__screenDataWidth / 2);
	double cy = __screenBottomY + (__screenDataHeight / 2);
	
	__screenDataWidth /= 2;
	__screenDataHeight /= 2;
	__DY = __screenDataHeight / 50;
	__DY = 1;
	__DX = __screenDataWidth / 50;
	__DX = 1;
	
	__screenLeftX = cx - (__screenDataWidth / 2);
	__screenBottomY = cy - (__screenDataHeight / 2);

	setPrintNodeSize(__currNodeSize * 2);
	scaleUnscalables();
	if (!__ignoreRepaint) {
		forceRepaint();
	}
	__zoomPercentage *= 2;
	if (__zoomPercentage == 100) {
		__parent.setZoomedOneToOne(true);
	}
	else {
		__parent.setZoomedOneToOne(false);
	}

	// Done to avoid a bug in java when painting anti-aliased when zoomed-out
	if (__zoomPercentage < 100) {
		__antiAlias = false;
	}
	else {
		__antiAlias = __antiAliasSetting;
	}
}

/**
Zooms so that the network as shown on the screen is how it will look when printed at full scale.
Although not all of the network may be shown on the screen, it will be at full scale.
*/
protected void zoomOneToOne() {
	//__screenLeftX = __dataLeftX;
	//__screenBottomY = __dataBottomY;

	__antiAlias = true;

	if (__fitWidth) {
		// Find out how many pixels across it should be given the
		// dpi for the screen and the paper size
		double pixels = __dpi * (int)(__pageFormat.getWidth() / 72);

		// Figure out the percentage of the entire paper that can fit on the screen
		double pct = (getBounds().width / pixels);

		// Show that percentage of the data at once
		double width = __totalDataWidth * pct;

		double ratio = __screenDataHeight / __screenDataWidth;
		double height = ratio * width;
	
		__screenDataWidth = width;
		__screenDataHeight = height;

		GRLimits data = __drawingArea.getDataLimits();
		data.setLeftX(__screenLeftX);
		data.setBottomY(__screenBottomY);
		data.setRightX(__screenLeftX + width);
		data.setTopY(__screenBottomY + height);
		__drawingArea.setDataLimits(data);
		setPrintNodeSize(__nodeSizeFullScale);
		scaleUnscalables();
		forceRepaint();
	}
	else {
		// Find out how many pixels high it should be given the
		// dpi for the screen and the paper size
		double pixels = __dpi * (int)(__pageFormat.getHeight() / 72);

		// Figure out the percentage of the entire paper that can fit on the screen
		double pct = (getBounds().height / pixels);

		// Show that percentage of the data at once
		double height = __totalDataHeight * pct;

		double ratio = __screenDataWidth / __screenDataHeight;
		double width = ratio * height;
	
		__screenDataHeight = height;
		__screenDataWidth = width;

		GRLimits data = __drawingArea.getDataLimits();
		data.setLeftX(__screenLeftX);
		data.setBottomY(__screenBottomY);
		data.setRightX(__screenLeftX + width);
		data.setTopY(__screenBottomY + height);
		__drawingArea.setDataLimits(data);
		setPrintNodeSize(__nodeSizeFullScale);
		scaleUnscalables();
		forceRepaint();
	}
	__zoomPercentage = 100;
	if ( __parent != null ) {
		__parent.setZoomedOneToOne(true);
	}
}

/**
Zooms out the current view to half the current view.
*/
protected void zoomOut() {
	if ((__screenDataWidth / __totalDataWidth) > 9) {
		return;
	}

	double cx = __screenLeftX + (__screenDataWidth / 2);
	double cy = __screenBottomY + (__screenDataHeight / 2);
	
	__screenDataWidth *= 2;
	__screenDataHeight *= 2;
	__DY = __screenDataHeight / 500;
	__DY = 1;
	__DX = __screenDataWidth / 500;
	__DX = 1;

	__screenLeftX = cx - (__screenDataWidth / 2);
	__screenBottomY = cy - (__screenDataHeight / 2);
	
	setPrintNodeSize(__currNodeSize / 2);
	scaleUnscalables();
	if (!__ignoreRepaint) {
		forceRepaint();
	}
	__zoomPercentage /= 2;
	if (__zoomPercentage == 100) {
		__parent.setZoomedOneToOne(true);
	}
	else {
		__parent.setZoomedOneToOne(false);
	}

	// Done to avoid a bug in java when painting anti-aliased when zoomed-out
	if (__zoomPercentage < 100) {
		__antiAlias = false;
	}
	else {
		__antiAlias = __antiAliasSetting;
	}
}

/**
Zooms so that the height of the network fits exactly in the height of the screen.
*/
/* TODO SAM 2007-03-01 Evaluate whether needed
private void zoomToHeight() {
	//__screenLeftX = __dataLeftX;
	//__screenBottomY = __dataBottomY;

	__screenDataHeight = __totalDataHeight;
	double pct = ((double)(getBounds().width)) / ((double)(getBounds().height));
	__screenDataWidth = pct * __totalDataHeight;

	scaleUnscalables();
	forceRepaint();
}
*/

/**
Zooms so that the network fits in the screen and all dimension are visible.
FIXME (JTS - 2004-04-01) Doesn't work 100% right.
*/
/* TODO SAM Evaluate whether needed
private void zoomToScreen() {
	int height = getBounds().height;
	int width = getBounds().width;
	double screenRatio = (double)height / (double)width;
	double dataRatio = __totalDataHeight / __totalDataWidth;

	if (__totalDataWidth > __totalDataHeight) {
		if (screenRatio < dataRatio) {
			zoomToHeight();
		}
		else {
			zoomToWidth();
		}
	}
	else {
		if (screenRatio < dataRatio) {
			zoomToWidth();
		}
		else {
			zoomToHeight();
		}

	}
	scaleUnscalables();
	forceRepaint();
}
*/

/**
Zooms so that the width of the network fits exactly in the width of the screen.
*/
/* TODO SAM 2007-03-01 Evaluate whether needed
private void zoomToWidth() {
	__screenDataWidth = __totalDataWidth;
	double pct = ((double)(getBounds().height)) / ((double)(getBounds().width));
	__screenDataHeight = pct * __totalDataWidth;
	
	scaleUnscalables();
	forceRepaint();
}
*/

}

/*
TODO 2004-11-11 (JTS) - when printing at a larger paper size than double 
__mouseDownX = 0;

 8.5x11, the 
canvas does not seem to fill the paper.  To recreate:
 - open an XML file that starts at 8.5x11
 - print it -- the margins, etc, match on screen
 - change paper size to 17x11 and print
 - the network no longer fills the paper as it should
*/

/*
TODO (2005-12-22) 

- legend cannot be dragged off the screen, looks stupid sometimes
- annotation moves cannot be undone, and in fact interfere with undo actions
	on nodes that WERE moved
*/
