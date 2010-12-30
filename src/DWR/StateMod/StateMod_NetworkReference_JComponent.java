// ----------------------------------------------------------------------------
// StateMod_NetworkReference_JComponent - class to control drawing of 
//	the network reference window
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-03-18	J. Thomas Sapienza, RTi	Initial version.  
// 2004-03-19 - 2004-03-22	JTS,RTi	Much more work getting a cleaner-
//					working version. 
// 2004-03-23	JTS, RTi		Javadoc'd.
// 2004-10-20	JTS, RTi		A black separator line is now drawn
//					around the component.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import cdss.domain.hydrology.network.HydrologyNode;

import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRText;

/**
This class draws the reference window for the network drawing code.  The 
reference window and the main window interact in ways, such as by allowing
positioning by clicking on the reference window, and responding to network
changes from the main window.
*/
public class StateMod_NetworkReference_JComponent 
extends GRJComponentDevice 
implements MouseListener, MouseMotionListener {

/**
Whether to draw the grid or not.
*/
private boolean __drawInchGrid = false;

/**
Whether to draw the printable area margin or not.
*/
private boolean __drawMargin = false;

/**
Whether to force paint() to refresh the entire drawing area or not.
*/
private boolean __forceRefresh = true;

/**
Whether the box representing the current network view is being dragged around the display.
*/
private boolean __inDrag = false;

/**
Whether drawing settings need to be initialized because it is the first
time paint() has been called.
*/
private boolean __initialized = false;

/**
The printing scale factor of the drawing.  This is the amount by which the
72 dpi printable pixels are scaled.  A printing scale value of 1 means that
the ER diagram will be printed at 72 pixels per inch (ppi), which is the 
java standard.   A scale factor of .5 means that the ER Diagram will be 
printed at 144 ppi.  A scale factor of 3 means that the ER Diagram will be printed at 24 ppi.
*/
// TODO SAM 2007-03-01 Evaluate use
//private double __printScale = 1;

/**
The height of the drawing area, in pixels.
*/
private double __height = 0;

/**
The width of the drawing area, in pixels.
*/
private double __width = 0;

/**
A dash pattern with large dash spacing
*/
private float[] __bigDashes = {3f, 5f};

/**
A dash pattern with smaller dash spacing.
*/
private float[] __smallDashes = {2f, 2f};

/**
The drawing area on which the main network is drawn.
*/
private GRJComponentDrawingArea __drawingArea;

/**
The node network read in from a makenet file.
*/
private StateMod_NodeNetwork __network;

/**
The difference between where the user clicked in the box that represents the
currently-viewed area of the network and the far bottom left point of the box.
*/
private int 
	__xAdjust,
	__yAdjust;

/**
The far bounds of the network, in terms of the reference window pixels.
*/
private int 
	__leftX = 0,
	__bottomY = 0,
	__totalHeight = 0,
	__totalWidth = 0;

/**
The class that draws the full network, and which interacts with the reference display.
*/
private StateMod_Network_JComponent __networkJComponent;

/**
Constructor.
@param parent the JFrame on which this appears.
*/
public StateMod_NetworkReference_JComponent(JFrame parent) {
	super("StateMod_NetworkReference_JComponent");
	addMouseListener(this);
	addMouseMotionListener(this);
}

/**
Clears the screen; fills with white.
*/
public void clear() {
	__drawingArea.clear(GRColor.gray);
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.white);
	__drawingArea.setScaleData(false);
	GRDrawingAreaUtil.fillRectangle(__drawingArea, __leftX, __bottomY, __totalWidth, __totalHeight);
	__drawingArea.setScaleData(true);
}

/**
Converts an X value from being scaled for drawing units to be scaled for data units.
@param x the x value to scale.
@return the x value scaled to fit in the data units.
*/
public double convertX(double x) {
	GRLimits data = __drawingArea.getDataLimits();
	GRLimits draw = __drawingArea.getDrawingLimits();
	double newX = (data.getLeftX() + (x / draw.getWidth()) * data.getWidth());
	return newX;
}

/**
Converts an Y value from being scaled for drawing units to be scaled for data units.
@param y the y value to scale.
@return the y value scaled to fit in the data units.
*/
public double convertY(double y) {
	GRLimits data = __drawingArea.getDataLimits();
	GRLimits draw = __drawingArea.getDrawingLimits();
	double newY = (data.getBottomY() + (y / draw.getHeight()) * data.getHeight());
	return newY;
}

/**
Draws the bounds that represent the currently-visible area of the network.
*/
private void drawBounds() {
	GRLimits vl = __networkJComponent.getVisibleDataLimits();
	if (vl == null) {
		return;
	}
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.green);
	double lx = vl.getLeftX();
	double by = vl.getBottomY();
	double rx = vl.getRightX();
	double ty = vl.getTopY();
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, rx, by);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, ty, rx, ty);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, lx, ty);
	GRDrawingAreaUtil.drawLine(__drawingArea, rx, by, rx, ty);
	
}

/**
Draws the outline of the legend.
*/
public void drawLegend() {
	GRLimits l = __networkJComponent.getLegendLimits();
	if (l == null) {
		return;
	}
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.white);
	double lx = l.getLeftX();
	double by = l.getBottomY();
	double rx = l.getRightX();
	double ty = l.getTopY();
	GRDrawingAreaUtil.fillRectangle(__drawingArea, lx, by, l.getWidth(), l.getHeight());
	GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, rx, by);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, ty, rx, ty);
	GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, lx, ty);
	GRDrawingAreaUtil.drawLine(__drawingArea, rx, by, rx, ty);
}

/**
Draws the network between all the nodes.
*/
private void drawNetworkLines() {
	boolean dash = false;
    float[] dashes = { 5f, 4f };
	float offset = 0;
	double[] x = new double[2];
	double[] y = new double[2];
	HydrologyNode ds = null;
	HydrologyNode dsRealNode = null;
	HydrologyNode holdNode = null;
	HydrologyNode holdNode2 = null;
	HydrologyNode node = null;
	HydrologyNode nodeTop = __network.getMostUpstreamNode();
	
	GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
	
	for (node = nodeTop; node != null; 
	    node = StateMod_NodeNetwork.getDownstreamNode(
	    node, StateMod_NodeNetwork.POSITION_COMPUTATIONAL)) {
	    	// move ahead and skip and blank or unknown nodes (which won't
		// be drawn, anyways -- check buildNodeArray()), so that 
		// connections are only between visible nodes
		if (holdNode == node) {
			GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
			return;
		}
		holdNode2 = node;
		while (node.getType() == HydrologyNode.NODE_TYPE_UNKNOWN) {
			node = StateMod_NodeNetwork.getDownstreamNode(node, StateMod_NodeNetwork.POSITION_COMPUTATIONAL);
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

		dsRealNode = StateMod_NodeNetwork.findNextRealOrXConfluenceDownstreamNode(node);

		// if the confluence of the reach (as opposed to a trib coming
		// in) then this is the last real node in disappearing stream.
		// Use the end node for the downstream node.
		dash = false;
		if (dsRealNode == StateMod_NodeNetwork.getDownstreamNode(node, StateMod_NodeNetwork.POSITION_REACH)) {
			dash = true;
		}

	    // move ahead and skip and blank or unknown nodes (which won't
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
	
		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
		
		if (dash) {
			__drawingArea.setFloatLineDash(dashes, offset);
			GRDrawingAreaUtil.drawLine(__drawingArea, x, y);
			__drawingArea.setFloatLineDash(null, (float)0);
		}
		else {
			GRDrawingAreaUtil.drawLine(__drawingArea, x, y);
		}
		holdNode = node;
	}
	GRDrawingAreaUtil.setLineWidth(__drawingArea, 1);
}		

/**
Forces the display to completely repaint itself immediately.
*/
public void forceRepaint() {
	__forceRefresh = true;
	repaint();
}

/**
Returns the height of the drawing area (in pixels).  
@return the height of the drawing area (in pixels).
*/
public double getDrawingHeight() {
	return __height;
}

/**
Returns the width of the drawing area (in pixels).
@return the width of the drawing area (in pixels).
*/
public double getDrawingWidth() {	
	return __width;
}

/**
Does nothing.
*/
public void mouseClicked(MouseEvent event) {}

/**
Responds to mouse dragged events by moving around the box representing 
the viewable network area, and also making the large network display change to show that area.
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {
	if (__inDrag) {
		if (event.getX() < __leftX || event.getX() > __leftX + __totalWidth) {
			return;
		}
		if (event.getY() < __bottomY || event.getY() >__bottomY+__totalHeight) {
			return;
		}

		__inDrag = true;
		GRLimits data = __networkJComponent.getTotalDataLimits();

		int width = __totalWidth;
		double pct = (double)(event.getX() - __leftX) / (double)width;
		int nw = (int)data.getWidth();
		int xPoint = (int)((nw * pct) + data.getLeftX());
	
		int height = __totalHeight;
		pct = (double)(height - event.getY() + __bottomY) / (double)height;
		int nh = (int)data.getHeight();
		int yPoint = (int)((nh * pct) + data.getBottomY());
		
		int x = xPoint - __xAdjust;
		int y = yPoint - __yAdjust;
		__networkJComponent.setViewPosition(x, y);
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
Does nothing.
*/
public void mouseMoved(MouseEvent event) {}

/**
Responds to mouse pressed events.  If the mouse was clicked within the box that
represents the viewable network area, then that view can be dragged around and
will be represented in the large display.  Otherwise, the view is re-centered
around the mouse click point.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
	if (event.getX() < __leftX || event.getX() > __leftX + __totalWidth) {
		return;
	}
	if (event.getY() < __bottomY || event.getY() >__bottomY+__totalHeight) {
		return;
	}
	__inDrag = true;

	GRLimits data = __networkJComponent.getTotalDataLimits();
	int width = __totalWidth;
	double pct = (double)(event.getX() - __leftX) / (double)width;
	int nw = (int)data.getWidth();
	int xPoint = (int)((nw * pct) + data.getLeftX());

	int height = __totalHeight;
	pct = (double)(height - event.getY() + __bottomY) / (double)height;
	int nh = (int)data.getHeight();
	int yPoint = (int)((nh * pct) + data.getBottomY());
	GRLimits limits = __networkJComponent.getVisibleDataLimits();

	int lx = (int)limits.getLeftX();
	int by = (int)limits.getBottomY();
	int rx = (int)limits.getRightX();
	int ty = (int)limits.getTopY();
	int w = rx - lx;
	int h = ty - by;

	// If the mouse was clicked in a point outside of the display box, 
	// the box is re-centered on the point and the network display is updated to show that area
    __xAdjust = (w / 2);
	__yAdjust = (h / 2);
	int x = xPoint - __xAdjust;
	int y = yPoint - __yAdjust;
	__networkJComponent.setViewPosition(x, y);
}

/**
Responds to mouse released events -- ends any dragging taking place.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	__inDrag = false;
}

/**
Paints the screen.
@param g the Graphics context to use for painting.
*/
public void paint(Graphics g) {
	// sets the graphics in the base class appropriately (double-buffered
	// if doing double-buffered drawing, single-buffered if not)
	setGraphics(g);

	// Set up drawing limits based on current window size...
	setLimits(getLimits(true));

	// first time through, do the following ...
	if (!__initialized) {
		// one time ONLY, do the following.
		__height = getBounds().height;
		__width = getBounds().width;	
		__initialized = true;
		setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
		
		repaint();
		__forceRefresh = true;
	}

	// only do the following if explicitly instructed to ...
	if (__forceRefresh) {
		clear();	
		
		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		
		setAntiAlias(true);
		drawNetworkLines();

		setAntiAlias(true);
		drawLegend();

		setAntiAlias(true);
		if (__drawMargin) {
			GRLimits margins = __networkJComponent.getMarginLimits();
			int lx = (int)margins.getLeftX();
			int rx = (int)margins.getRightX();
			int by = (int)margins.getBottomY();
			int ty = (int)margins.getTopY();

			__drawingArea.setFloatLineDash(__bigDashes, 0);
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.cyan);
			GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, rx, by);
			GRDrawingAreaUtil.drawLine(__drawingArea, lx, ty, rx, ty);
			GRDrawingAreaUtil.drawLine(__drawingArea, lx, by, lx, ty);
			GRDrawingAreaUtil.drawLine(__drawingArea, rx, by, rx, ty);
			__drawingArea.setFloatLineDash(null, 0);
		}

		setAntiAlias(true);
		if (__drawInchGrid) {
			__drawingArea.setFloatLineDash(__smallDashes, 0);
			double maxX = __networkJComponent.getTotalWidth();
			double maxY = __networkJComponent.getTotalHeight();
			double leftX = __networkJComponent.getDataLeftX();
			double bottomY = __networkJComponent.getDataBottomY();
			double printScale = __networkJComponent.getPrintScale();
			
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.red);
			int j = 0;
			int minY = (int)(__networkJComponent.convertAbsY(0) + bottomY);
			int tempMaxY = (int)(__networkJComponent.convertAbsY( maxY) + bottomY);
			for (int i = 0; i < maxX; i+= ((72/printScale)/2)) {
				j = (int)(__networkJComponent.convertAbsX(i) + leftX);
				GRDrawingAreaUtil.drawLine(__drawingArea, j, minY, j, tempMaxY);
				GRDrawingAreaUtil.drawText(__drawingArea, "" + ((double)i/(72/printScale)),j, 
					minY, 0, GRText.CENTER_X | GRText.BOTTOM);
			}

			int minX = (int)(__networkJComponent.convertAbsX(0) + leftX);
			int tempMaxX = (int)(__networkJComponent.convertAbsX(maxX) + leftX);
			for (int i = 0; i < maxY; i+= ((72/printScale)/2)) {
				j = (int)(__networkJComponent.convertAbsY(i) + bottomY);
				GRDrawingAreaUtil.drawLine(__drawingArea, minX, j, tempMaxX, j);
				GRDrawingAreaUtil.drawText(__drawingArea, "" + ((double)i/(72/printScale)),minX,
					j, 0, GRText.CENTER_Y | GRText.LEFT);
			}
			__drawingArea.setFloatLineDash(null, 0);
		}

		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		GRLimits drawingLimits = __networkJComponent.getTotalDataLimits();
		GRDrawingAreaUtil.drawLine(__drawingArea,
			drawingLimits.getLeftX(), drawingLimits.getBottomY(),
			drawingLimits.getRightX(), drawingLimits.getBottomY());
		GRDrawingAreaUtil.drawLine(__drawingArea,
			drawingLimits.getLeftX(), drawingLimits.getTopY(),
			drawingLimits.getRightX(), drawingLimits.getTopY());
		GRDrawingAreaUtil.drawLine(__drawingArea,
			drawingLimits.getLeftX(), drawingLimits.getTopY(),
			drawingLimits.getLeftX(), drawingLimits.getBottomY());
		GRDrawingAreaUtil.drawLine(__drawingArea,
			drawingLimits.getRightX(), drawingLimits.getTopY(),
			drawingLimits.getRightX(), drawingLimits.getBottomY());

		setAntiAlias(true);
		drawBounds();
	}
	
	// displays the graphics
	showDoubleBuffer(g);
}

/**
Sets whether the inch grid should be drawn.  The reference display will be instantly redrawn.
@param draw whether the inch grid should be drawn.
*/
public void setDrawInchGrid(boolean draw) {
	__drawInchGrid = draw;
	forceRepaint();
}

/**
Sets the drawing area to be used with this device.
@param drawingArea the drawingArea to use with this device.
*/
public void setDrawingArea(GRJComponentDrawingArea drawingArea) {
	__drawingArea = drawingArea;

	GRLimits data = __drawingArea.getDataLimits();
	double dw = data.getRightX() - data.getLeftX();
	double dh = data.getTopY() - data.getBottomY();
	
	double ratio = 0;
	if (dw > dh) {
		ratio = 200 / dw;
		__totalWidth = 200;
		__leftX = 0;
		__totalHeight = (int)(ratio * dh);
		__bottomY = (200 - __totalHeight) / 2;
	}
	else {
		ratio = 200 / dh;
		__totalHeight = 200;
		__bottomY = 0;
		__totalWidth = (int)(ratio * dw);
		__leftX = (200 - __totalWidth) / 2;
	}
}

/**
Sets whether the margin should be drawn.  The reference display will be instantly redrawn.
@param draw whether the margin should be drawn.
*/
public void setDrawMargin(boolean draw) {
	__drawMargin = draw;
	forceRepaint();
}

/**
Sets the HydrologyNodeNetwork to use.
@param network the network to be drawn.
*/
public void setNetwork(StateMod_NodeNetwork network) {
	__network = network;
}

/**
Sets the component that stores the full-view network display.
@param net the component with the full-view network display.
*/
public void setNetworkJComponent(StateMod_Network_JComponent net) {
	__networkJComponent = net;
}

/**
Sets new data limits for the network reference window.  Called when, 
for instance, the paper orientation changes.
@param data the GRLimits of the data.
*/
public void setNewDataLimits(GRLimits data) {
	double dw = data.getRightX() - data.getLeftX();
	double dh = data.getTopY() - data.getBottomY();
	
	__drawingArea.setDataLimits(data);
/*
System.out.println(">> W/H: " + __totalWidth + "  " + __totalHeight);
System.out.println(">> L/B: " + __leftX + "  " + __bottomY);
*/
	double ratio = 0;
	if (dw > dh) {
		ratio = 200 / dw;
		__totalWidth = 200;
		__leftX = 0;
		__totalHeight = (int)(ratio * dh);
		__bottomY = (200 - __totalHeight) / 2;
	}
	else {
		ratio = 200 / dh;
		__totalHeight = 200;
		__bottomY = 0;
		__totalWidth = (int)(ratio * dw);
		__leftX = (200 - __totalWidth) / 2;
	}
/*
System.out.println("<< W/H: " + __totalWidth + "  " + __totalHeight);
System.out.println("<< L/B: " + __leftX + "  " + __bottomY);	
*/
}

/**
Sets the nodes array to use.
@param nodes the nodes array to use.
*/
public void setNodesArray(HydrologyNode[] nodes) {
	//TODO SAM 2007-03-01 Evaluate use
	//__nodes = nodes;
}

/**
Sets up the double buffer to be used when drawing.
*/
public void setupDoubleBuffering() {
	setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
	forceRepaint();
}

}