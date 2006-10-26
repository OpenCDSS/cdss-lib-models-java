// ----------------------------------------------------------------------------
// StateMod_Network_NodeProperties_JDialog -
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-04-15	J. Thomas Sapienza, RTi	Initial version.  
// 2004-07-09	JTS, RTi		Removed Baseflow and Import type nodes
//					as they will be specified with the 
//					import and baseflow properties now.
// 2004-07-12	JTS, RTi		Added support for XConfluence nodes.
// 2004-12-20	JTS, RTi		Changed how node labels are numbered
//					so that Makenet networks display
//					properly.
// 2005-06-13	JTS, RTi		Properties now prints out the IDs
//					of upstream and downstream nodes.
// 2005-12-07	JTS, RTi		* When showing the properties for
//					  reservoir nodes, the Reservoir 
//					  Direction combo box and label was 
//					  being overwritten by the first 
//					  Upstream Node line.  Corrected.
//					* Corrected a bug causing null pointer
//					  errors when showing the downstream 
//					  nodes for the End node.
// 2005-12-20	JTS, RTi		Reservoir direction label problem from
//					2005-12-07 was only half solved -- nodes
//					that were not originally reservoirs when
//					the properties dialog was opened still
//					had problems.
// 2006-02-21	JTS, RTi		Fixed a bug that was resulting in 
//					baseflow and import booleans always 
//					being set to false if "Cancel" was
//					pressed.
// 2006-03-07	JTS, RTi		* If running in StateModGUI, important
//					  fields are now noneditable.
//					* Added finalize().
// 2006-04-18	JTS, RTi		__nodes is no longer finalized in this
//					class.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import DWR.DMI.HydroBaseDMI.HydroBase_Node;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class draws the network that can be printed, viewed and altered.
*/
public class StateMod_Network_NodeProperties_JDialog
extends JDialog
implements ActionListener, KeyListener, WindowListener {

/**
Possible node types to appear in the type combo box.
*/
private final String 
	__NODE_CONFLUENCE = 		"CONFL - Confluence",
	__NODE_DIVERSION = 		"DIV - Diversion",
	__NODE_DIVERSION_AND_WELL = 	"D&W - Diversion and Well",
	__NODE_END = 			"END - End Node",
	__NODE_INSTREAM_FLOW = 		"ISF - Instream Flow",
	__NODE_OTHER = 			"OTH - Other",
	__NODE_RESERVOIR = 		"RES - Reservoir",
	__NODE_STREAMFLOW = 		"FLOW - Streamflow",
	__NODE_WELL = 			"WELL - Well",
	__NODE_XCONFLUENCE = 		"XCONFL - XConfluence";

/**
Reservoir directions.
*/
private final String
	__ABOVE_CENTER = 	"AboveCenter",
	__UPPER_RIGHT = 	"UpperRight",
	__RIGHT = 		"Right",
	__LOWER_RIGHT = 	"LowerRight",
	__BELOW_CENTER = 	"BelowCenter",
	__BOTTOM = 		"Bottom",
	__LOWER_LEFT = 		"LowerLeft",
	__LEFT = 		"Left",
	__UPPER_LEFT = 		"UpperLeft",
	__CENTER = 		"Center",
	__TOP = 		"Top";

/**
Button labels.
*/
private final String
	__BUTTON_APPLY = 	"Apply",
	__BUTTON_CANCEL = 	"Cancel",
	__BUTTON_OK = 		"OK";

private boolean __ignoreEvents;
private boolean __origBaseflow;
private boolean __origDirty = false;
private boolean __origImport;

/**
Array of nodes from which one will be displayed and edited.
*/
private HydroBase_Node[] __nodes;

/**
The number of the node being displayed in the __nodes array.
*/
private int __nodeNum = -1;

/**
Original node int values.
*/
private int 
	__origResIDir,
	__origIDir,
	__origIType;

/**
GUI buttons.
*/
private JButton 
	__applyButton = null,
	__okButton = null;

/**
Checkbox for specifying if the node is a baseflow node or not.
*/
private JCheckBox __isBaseflowCheckBox;

/**
Checkbox for specifying if the node is an import node or not.
*/
private JCheckBox __isImportCheckBox;

/**
Reservoir direction JLabel.
*/
private JLabel __reservoirDirectionLabel;

/**
Textfields for holding node values.
*/
private JTextField
	__descriptionTextField,
	__idTextField,
	__xTextField,
	__yTextField,
	__areaTextField,
	__precipitationTextField;

/**
GUI combo boxes.
*/
private SimpleJComboBox 
	__labelPositionComboBox,
	__reservoirDirectionComboBox,
	__typeComboBox;

/**
The parent JFrame on which this dialog appears.
*/
private StateMod_Network_JFrame __parent = null;

/**
Original node String values.
*/
private String 
	__origArea,
	__origDesc,
	__origDir,
	__origID,
	__origPrecipitation,
	__origResDir,
	__origType,
	__origX,
	__origY;

/**
Constructor.
@param parent the parent JFrame on which this dialog appears.
@param nodes array of nodes, from which one will be displayed.
@param nodeNum the number of the node being displayed from the array.
*/
public StateMod_Network_NodeProperties_JDialog(StateMod_Network_JFrame parent, 
HydroBase_Node[] nodes, int nodeNum) {
	super(parent, "Node Properties - " + nodes[nodeNum].getCommonID(), 
		true);
	__parent = parent;
	__nodes = nodes;
	__nodeNum = nodeNum;
	setupGUI();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	if (__ignoreEvents) {	
		return;
	}
	String action = event.getActionCommand();
	Object source = event.getSource();

	if (action.equals(__BUTTON_CANCEL)) {
		cancelClicked();
	}
	else if (action.equals(__BUTTON_OK)
	      || action.equals(__BUTTON_APPLY)) {
	      	applyClicked();
		if (action.equals(__BUTTON_OK)) {
			setVisible(false);
			dispose();
		}
	}
	else if (source == __typeComboBox) {
		String type = __typeComboBox.getSelected();
		if (type.equals(__NODE_RESERVOIR)) {
			__reservoirDirectionLabel.setVisible(true);
			__reservoirDirectionComboBox.setVisible(true);
		}
		else {
			__reservoirDirectionLabel.setVisible(false);
			__reservoirDirectionComboBox.setVisible(false);
		}
	}
	else if (source == __isBaseflowCheckBox) {
		if (__isBaseflowCheckBox.isSelected()) {
			__areaTextField.setEnabled(true);
			__precipitationTextField.setEnabled(true);
		}
		else {
			__areaTextField.setEnabled(false);
			__precipitationTextField.setEnabled(false);
		}
	}
	else if (source == __isImportCheckBox) {
		if (__isImportCheckBox.isSelected()) {
		}
		else {
		}
	}	
}

/**
Adds the IDs of the downstream nodes to the main JPanel.
@param panel the panel to which to add the node IDs.
@param y the y coordinate at which to start adding them.
*/
private void addDownstreamNodeToPanel(JPanel panel, int y) {
	HydroBase_Node node = __parent.getNetwork().findNextRealDownstreamNode(
		__nodes[__nodeNum]);
	JGUIUtil.addComponent(panel, new JLabel("Downstream node: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("" + node.getCommonID()
		+ " (model node)"),
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;

	node = __nodes[__nodeNum].getDownstreamNode();
	if (node != null) {
		JGUIUtil.addComponent(panel, new JLabel("" + node.getCommonID()
			+ " (diagram node)"),
			1, y, 1, 1, 0, 0,
			GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	else {
		JGUIUtil.addComponent(panel,new JLabel("[None] (diagram node)"),
			1, y, 1, 1, 0, 0,
			GridBagConstraints.NONE, GridBagConstraints.WEST);
	}	
}

/**
Adds the IDs of the upstream nodes to the main JPanel.
@param panel the panel to which to add the node IDs.
@param y the y coordinate at which to start adding them.
*/
private int addUpstreamNodesToPanel(JPanel panel, int y) {
	Vector upstreams = __nodes[__nodeNum].getUpstreamNodes();
	if (upstreams == null || upstreams.size() == 0) {
		return y;
	}

	int size = upstreams.size();
	String plural = "s";
	if (size == 1) {
		plural = "";
	}

	for (int i = 0; i < size; i++) {
		if (i == 0) {
			JGUIUtil.addComponent(panel, 
				new JLabel("Upstream node" + plural + ": "),
				0, y, 1, 1, 0, 0,
				GridBagConstraints.NONE, 
				GridBagConstraints.EAST);
		}
		JGUIUtil.addComponent(panel,
			new JLabel("" + ((HydroBase_Node)upstreams.elementAt(i))
				.getCommonID()),
			1, y, 1, 1, 0, 0, 
			GridBagConstraints.NONE, GridBagConstraints.WEST);
		y++;
	}

	return y;
}

/**
Called when Apply or OK is clicked.  Commits any changes to the node.
*/
private void applyClicked() {
	boolean dirty = false;
	String id = __idTextField.getText();
	String x = __xTextField.getText();
	String y = __yTextField.getText();
	String desc = __descriptionTextField.getText();
	boolean baseflow = __isBaseflowCheckBox.isSelected();
	boolean mport = __isImportCheckBox.isSelected();
	String type = __typeComboBox.getSelected();
	String dir = __labelPositionComboBox.getSelected();
	String resDir = __reservoirDirectionComboBox.getSelected();
	String area = __areaTextField.getText();
	String precip = __precipitationTextField.getText();
	
	__nodes[__nodeNum].setCommonID(id);
	if (!id.equals(__origID)) {
		dirty = true;
	}
	
	__nodes[__nodeNum].setX((new Double(x)).doubleValue());
	if (!x.equals(__origX)) {
		dirty = true;
	}
		
	__nodes[__nodeNum].setY((new Double(y)).doubleValue());
	if (!y.equals(__origY)) {
		dirty = true;
	}

	__nodes[__nodeNum].setDescription(desc);
	if (!desc.equals(__origDesc)) {
		dirty = true;
	}

	__nodes[__nodeNum].setIsBaseflow(baseflow);
	if (baseflow != __origBaseflow) {
		dirty = true;
	}

	__nodes[__nodeNum].setIsImport(mport);
	if (mport != __origImport) {
		dirty = true;
	}

	__nodes[__nodeNum].setPrecip(
		(new Double(precip)).doubleValue());
	if (!precip.equals(__origPrecipitation)) {
		dirty = true;
	}

	__nodes[__nodeNum].setArea((new Double(area)).doubleValue());
	if (!area.equals(__origArea)) {
		dirty = true;
	}

	int itype = -1;
	boolean res = false;
	if (type.equals(__NODE_CONFLUENCE)) {
		itype = HydroBase_Node.NODE_TYPE_CONFLUENCE;
	}
	else if (type.equals(__NODE_DIVERSION)) {
		itype = HydroBase_Node.NODE_TYPE_DIV;
	}
	else if (type.equals(__NODE_DIVERSION_AND_WELL)) {
		itype = HydroBase_Node.NODE_TYPE_DIV_AND_WELL;
	}
	else if (type.equals(__NODE_END)) {
		itype = HydroBase_Node.NODE_TYPE_END;
	}
	else if (type.equals(__NODE_INSTREAM_FLOW)) {
		itype = HydroBase_Node.NODE_TYPE_ISF;
	}
	else if (type.equals(__NODE_OTHER)) {
		itype = HydroBase_Node.NODE_TYPE_OTHER;
	}
	else if (type.equals(__NODE_RESERVOIR)) {
		itype = HydroBase_Node.NODE_TYPE_RES;
		res = true;
	}
	else if (type.equals(__NODE_STREAMFLOW)) {
		itype = HydroBase_Node.NODE_TYPE_FLOW;
	}
	else if (type.equals(__NODE_WELL)) {
		itype = HydroBase_Node.NODE_TYPE_WELL;
	}		
	else if (type.equals(__NODE_XCONFLUENCE)) {
		itype = HydroBase_Node.NODE_TYPE_XCONFLUENCE;
	}	
	else {
		int index = type.indexOf(":");
		type = type.substring(index + 1).trim();
		itype = Integer.decode(type).intValue();
	}
	__nodes[__nodeNum].setType(itype);
	if (!type.equals(__origType)) {
		dirty = true;
	}

	int idir = -1;
	if (dir.equals(__ABOVE_CENTER)) {
		idir = 1;
	}
	else if (dir.equals(__UPPER_RIGHT)) {
		idir = 7;
	}
	else if (dir.equals(__RIGHT)) {
		idir = 4;
	}
	else if (dir.equals(__LOWER_RIGHT)) {
		idir = 8;
	}
	else if (dir.equals(__BELOW_CENTER)) {
		idir = 2;
	}
	else if (dir.equals(__LOWER_LEFT)) {
		idir = 5;
	}
	else if (dir.equals(__LEFT)) {
		idir = 3;
	}
	else if (dir.equals(__UPPER_LEFT)) {
		idir = 6;
	}
	else if (dir.equals(__CENTER)) {
		idir = 9;
	}
	else {
		int index = dir.indexOf(":");
		dir = dir.substring(index + 1).trim();
		idir = Integer.decode(dir).intValue();
	}
	int iresdir = 0;
	if (res) {
		if (resDir.equals(__TOP)) {
			iresdir = 2;	
		}
		else if (resDir.equals(__BOTTOM)) {
			iresdir = 1;
		}
		else if (resDir.equals(__LEFT)) {
			iresdir = 4;
		}
		else if (resDir.equals(__RIGHT)) {
			iresdir = 3;
		}
		else {
			int index = resDir.indexOf(":");
			resDir = resDir.substring(index + 1).trim();
			iresdir = Integer.decode(resDir).intValue();
		}
	}
	
	__nodes[__nodeNum].setLabelDirection((iresdir * 10) + idir);
	if (!resDir.equals(__origResDir) || !dir.equals(__origDir)) {
		dirty = true;
	}

	__nodes[__nodeNum].setDirty(true);
	__nodes[__nodeNum].setBoundsCalculated(false);
		
	__parent.nodePropertiesChanged();
}

/**
Called when cancel is pressed -- reverts any changes made to the node.
*/
private void cancelClicked() {
	__nodes[__nodeNum].setCommonID(__origID);
	__nodes[__nodeNum].setX((new Double(__origX)).doubleValue());
	__nodes[__nodeNum].setY((new Double(__origY)).doubleValue());
	__nodes[__nodeNum].setDescription(__origDesc);
	__nodes[__nodeNum].setIsBaseflow(__origBaseflow);
	__nodes[__nodeNum].setType(__origIType);
	__nodes[__nodeNum].setLabelDirection(
		__origResIDir * 10 + __origIDir);
	__nodes[__nodeNum].setDirty(__origDirty);
	__parent.nodePropertiesChanged();	
	setVisible(false);
	dispose();
}

/**
Checks an id to make sure it's unique within the network.
@param id the id to check
@return true if the ID is unique, false if not.
*/
private boolean checkUniqueID(String id) {
	for (int i = 0; i < __nodes.length; i++) {
		if (i != __nodeNum) {
			if (__nodes[i].getCommonID().equals(id)) {
				return false;
			}
		}
	}

	return true;
}

/**
Checks the current values for the node's data to make sure they are valid.  If
any are invalid their textfields are colored red and the OK button is 
disabled.
*/
private void checkValidity() {
	String id = __idTextField.getText();
	boolean valid = true;
	
	if (!__parent.inStateModGUI()) {
		if (id.trim().equals("")) {
			__idTextField.setBackground(Color.red);
			valid = false;
		}
		else if (!checkUniqueID(id)) {
			__idTextField.setBackground(Color.red);
			valid = false;
		}
		else {
			__idTextField.setBackground(Color.white);
		}
	}

	String xs = __xTextField.getText();
	if (xs.trim().equals("")) {
		__xTextField.setBackground(Color.red);
		valid = false;
	}
	else {
		try {
			Double xx = new Double(xs);
			__xTextField.setBackground(Color.white);
		}
		catch (Exception e) {
			__xTextField.setBackground(Color.red);
			valid = false;
		}
	}

	String ys = __yTextField.getText();
	if (ys.trim().equals("")) {
		__yTextField.setBackground(Color.red);
		valid = false;
	}
	else {
		try {
			Double yy = new Double(ys);
			__yTextField.setBackground(Color.white);
		}
		catch (Exception e) {
			__yTextField.setBackground(Color.red);
			valid = false;
		}
	}

	if (!__parent.inStateModGUI()) {
		String area = __areaTextField.getText();
		if (area.trim().equals("")) {
			__areaTextField.setBackground(Color.red);
			valid = false;
		}
		else {
			try {
				Double a = new Double(area);
				__areaTextField.setBackground(Color.white);
			}
			catch (Exception e) {
				__areaTextField.setBackground(Color.red);
				valid = false;
			}
		}
	}

	if (!__parent.inStateModGUI()) {
		String precipitation = __precipitationTextField.getText();
		if (precipitation.trim().equals("")) {
			__precipitationTextField.setBackground(Color.red);
			valid = false;
		}
		else {
			try {
				Double p = new Double(precipitation);
				__precipitationTextField.setBackground(
					Color.white);
			}
			catch (Exception e) {
				__precipitationTextField.setBackground(
					Color.red);
				valid = false;
			}
		}
	}

	if (!valid) {
		__okButton.setEnabled(false);
		__applyButton.setEnabled(false);
	}
	else {
		__okButton.setEnabled(true);
		__applyButton.setEnabled(true);
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	// DO NOT DO THE FOLLOWING:
	// RTi.Util.IO.IOUtil.nullArray(__nodes);
	// That will result in the array being nulled in the calling code.
	__applyButton = null;
	__okButton = null;
	__isBaseflowCheckBox = null;
	__isImportCheckBox = null;
	__reservoirDirectionLabel = null;
	__descriptionTextField = null;
	__idTextField = null;
	__xTextField = null;
	__yTextField = null;
	__areaTextField = null;
	__precipitationTextField = null;
	__labelPositionComboBox = null;
	__reservoirDirectionComboBox = null;
	__typeComboBox = null;
	__parent = null;
	__origArea = null;
	__origDesc = null;
	__origDir = null;
	__origID = null;
	__origPrecipitation = null;
	__origResDir = null;
	__origType = null;
	__origX = null;
	__origY = null;
	super.finalize();
}

/**
Responds to key press events and calls checkValidity().
@param event the KeyEvent that happened.
*/
public void keyPressed(KeyEvent event) {
	checkValidity();
}

/**
Responds to key releases and calls checkValidity().
@param event the KeyEvent that happened.
*/
public void keyReleased(KeyEvent event) {
	checkValidity();
}

/**
Responds to key types events and calls checkValidity().
@param event the KeyEvent that happened.
*/
public void keyTyped(KeyEvent event) {
	checkValidity();
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	addWindowListener(this);

	__descriptionTextField = new JTextField(25);
	__origDesc = __nodes[__nodeNum].getDescription();
	__descriptionTextField.setText(__origDesc);
	__idTextField = new JTextField(10);
	__origID = __nodes[__nodeNum].getCommonID();
	__idTextField.setText(__origID);
	__idTextField.addKeyListener(this);
	__xTextField = new JTextField(10);
	__origX = StringUtil.formatString(__nodes[__nodeNum].getX(), 
		"%13.6f").trim();
	__xTextField.setText(__origX);
	__xTextField.addKeyListener(this);
	__yTextField = new JTextField(10);
	__origY = StringUtil.formatString(__nodes[__nodeNum].getY(), 
		"%13.6f").trim();
	__yTextField.setText(__origY);
	__yTextField.addKeyListener(this);
	__areaTextField = new JTextField(10);
	__origArea = StringUtil.formatString(__nodes[__nodeNum].getArea(), 
		"%13.6f").trim();
	__areaTextField.setText(__origArea);
	__areaTextField.addKeyListener(this);
	__precipitationTextField = new JTextField(10);
	__origPrecipitation = StringUtil.formatString(
		__nodes[__nodeNum].getPrecip(), "%13.6f").trim();
	__precipitationTextField.setText(__origPrecipitation);
	__precipitationTextField.addKeyListener(this);

	__typeComboBox = new SimpleJComboBox(false);
	__typeComboBox.add(__NODE_DIVERSION);
	__typeComboBox.add(__NODE_DIVERSION_AND_WELL);
	__typeComboBox.add(__NODE_INSTREAM_FLOW);
	__typeComboBox.add(__NODE_OTHER);
	__typeComboBox.add(__NODE_RESERVOIR);
	__typeComboBox.add(__NODE_STREAMFLOW);
	__typeComboBox.add(__NODE_WELL);	

	int type = __nodes[__nodeNum].getType();
	if (type == HydroBase_Node.NODE_TYPE_CONFLUENCE) {
		__typeComboBox.select(__NODE_CONFLUENCE);
	}
	else if (type == HydroBase_Node.NODE_TYPE_DIV) {
		__typeComboBox.select(__NODE_DIVERSION);
	}
	else if (type == HydroBase_Node.NODE_TYPE_DIV_AND_WELL) {
		__typeComboBox.select(__NODE_DIVERSION_AND_WELL);
	}
	else if (type == HydroBase_Node.NODE_TYPE_END) {
		__typeComboBox.removeAll();
		__typeComboBox.add(__NODE_END);
	}
	else if (type == HydroBase_Node.NODE_TYPE_ISF) {
		__typeComboBox.select(__NODE_INSTREAM_FLOW);
	}
	else if (type == HydroBase_Node.NODE_TYPE_OTHER) {
		__typeComboBox.select(__NODE_OTHER);
	}
	else if (type == HydroBase_Node.NODE_TYPE_RES) {
		__typeComboBox.select(__NODE_RESERVOIR);
	}
	else if (type == HydroBase_Node.NODE_TYPE_FLOW) {
		__typeComboBox.select(__NODE_STREAMFLOW);
	}
	else if (type == HydroBase_Node.NODE_TYPE_WELL) {
		__typeComboBox.select(__NODE_WELL);
	}
	else if (type == HydroBase_Node.NODE_TYPE_XCONFLUENCE) {
		__typeComboBox.select(__NODE_XCONFLUENCE);
	}	
	else {
		__typeComboBox.removeAll();
		__typeComboBox.add("Unknown Type: " + type);
	}

	__typeComboBox.setMaximumRowCount(__typeComboBox.getItemCount());

	__origIType = type;
	__origType = __typeComboBox.getSelected();

	__labelPositionComboBox = new SimpleJComboBox(false);
	__labelPositionComboBox.add(__ABOVE_CENTER);
	__labelPositionComboBox.add(__UPPER_RIGHT);
	__labelPositionComboBox.add(__RIGHT);
	__labelPositionComboBox.add(__LOWER_RIGHT);
	__labelPositionComboBox.add(__BELOW_CENTER);
	__labelPositionComboBox.add(__LOWER_LEFT);
	__labelPositionComboBox.add(__LEFT);
	__labelPositionComboBox.add(__UPPER_LEFT);
	__labelPositionComboBox.add(__CENTER);
	__labelPositionComboBox.setMaximumRowCount(
		__labelPositionComboBox.getItemCount());

	int dir = __nodes[__nodeNum].getLabelDirection() % 10;
	if (dir == 2) {
		__labelPositionComboBox.select(__BELOW_CENTER);
	}
	else if (dir == 1) {
		__labelPositionComboBox.select(__ABOVE_CENTER);
	}
	else if (dir == 4) {
		__labelPositionComboBox.select(__RIGHT);
	}
	else if (dir == 3) {
		__labelPositionComboBox.select(__LEFT);
	}
	else if (dir == 7) {
		__labelPositionComboBox.select(__UPPER_RIGHT);
	}
	else if (dir == 8) {
		__labelPositionComboBox.select(__LOWER_RIGHT);
	}
	else if (dir == 5) {
		__labelPositionComboBox.select(__LOWER_LEFT);
	}
	else if (dir == 6) {
		__labelPositionComboBox.select(__UPPER_LEFT);
	}
	else if (dir == 9) {
		__labelPositionComboBox.select(__CENTER);
	}
	else {
		__labelPositionComboBox.removeAll();
		__labelPositionComboBox.add("Unknown Position: " + dir);
	}

	__origIDir = dir;
	__origDir = __labelPositionComboBox.getSelected();

	__isBaseflowCheckBox = new JCheckBox();
	__isImportCheckBox = new JCheckBox();

	__origDirty = __nodes[__nodeNum].isDirty();
	
	__reservoirDirectionLabel = new JLabel("Reservoir Direction: ");
	__reservoirDirectionComboBox = new SimpleJComboBox(false);
	__reservoirDirectionComboBox.add(__TOP);
	__reservoirDirectionComboBox.add(__BOTTOM);
	__reservoirDirectionComboBox.add(__LEFT);
	__reservoirDirectionComboBox.add(__RIGHT);

	int resDir = __nodes[__nodeNum].getLabelDirection() / 10;
	if (resDir == 1) {
		__reservoirDirectionComboBox.select(__BOTTOM);
	}
	else if (resDir == 2) {
		__reservoirDirectionComboBox.select(__TOP);
	}
	else if (resDir == 3) {
		__reservoirDirectionComboBox.select(__RIGHT);
	}
	else if (resDir == 4) {
		__reservoirDirectionComboBox.select(__LEFT);
	}
	else if (resDir == 0) {}
	else {
		__reservoirDirectionComboBox.removeAll();
		__reservoirDirectionComboBox.add("Unknown Direction: "+ resDir);
	}

	__origResIDir = resDir;
	__origResDir = __reservoirDirectionComboBox.getSelected();

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	int y = 0;
	JGUIUtil.addComponent(panel, new JLabel("ID: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __idTextField,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Type: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __typeComboBox,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Description: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __descriptionTextField,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("X: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __xTextField,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Y: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __yTextField,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Is Baseflow: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __isBaseflowCheckBox,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Is Import: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __isImportCheckBox,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Area: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __areaTextField,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(panel, new JLabel("Precipitation: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __precipitationTextField,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	if (__nodes[__nodeNum].isBaseflow()) {
		__isBaseflowCheckBox.setSelected(true);
	}
	else {
		__isBaseflowCheckBox.setSelected(false);
		__areaTextField.setEnabled(false);
		__precipitationTextField.setEnabled(false);
	}
	__isBaseflowCheckBox.addActionListener(this);

	if (__nodes[__nodeNum].isImport()) {
		__isImportCheckBox.setSelected(true);
	}
	else {
		__isImportCheckBox.setSelected(false);
	}
	__isImportCheckBox.addActionListener(this);

	__origBaseflow = __isBaseflowCheckBox.isSelected();
	__origImport = __isImportCheckBox.isSelected();

	y++;
	JGUIUtil.addComponent(panel, new JLabel("Label Position: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __labelPositionComboBox,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(panel, __reservoirDirectionLabel,
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __reservoirDirectionComboBox,
		1, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;

	y = addUpstreamNodesToPanel(panel, y);
	addDownstreamNodeToPanel(panel, y);

	getContentPane().add(panel);

	JPanel southPanel = new JPanel();
	southPanel.setLayout(new GridBagLayout());
	
	__applyButton = new JButton(__BUTTON_APPLY);
	__applyButton.addActionListener(this);
	__okButton = new JButton(__BUTTON_OK);
	__okButton.addActionListener(this);
	JButton cancelButton = new JButton(__BUTTON_CANCEL);
	cancelButton.addActionListener(this);

	JGUIUtil.addComponent(southPanel, __applyButton,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(southPanel, __okButton,
		2, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(southPanel, cancelButton,
		3, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	getContentPane().add(southPanel, "South");

	pack();

	if (__origIType != HydroBase_Node.NODE_TYPE_RES) {
		__reservoirDirectionLabel.setVisible(false);
		__reservoirDirectionComboBox.setVisible(false);
	}
	
	__typeComboBox.addActionListener(this);

	if (__parent.inStateModGUI()) {
		__typeComboBox.setEnabled(false);
		__precipitationTextField.setEditable(false);
		__precipitationTextField.removeKeyListener(this);
		__descriptionTextField.setEditable(false);
		__descriptionTextField.removeKeyListener(this);
		__areaTextField.removeKeyListener(this);
		__areaTextField.setEditable(false);
		__idTextField.removeKeyListener(this);
		__idTextField.setEditable(false);
		__isBaseflowCheckBox.setEnabled(false);
		__isImportCheckBox.setEnabled(false);
	}

	JGUIUtil.center(this);
	setVisible(true);
}

public void windowActivated(WindowEvent event) {}
public void windowDeactivated(WindowEvent event) {}
public void windowIconified(WindowEvent event) {}
public void windowDeiconified(WindowEvent event) {}
public void windowOpened(WindowEvent event) {}

public void windowClosing(WindowEvent event) {
	cancelClicked();
}

public void windowClosed(WindowEvent event) {}

}
