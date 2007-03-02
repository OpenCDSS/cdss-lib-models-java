// ----------------------------------------------------------------------------
// StateMod_Network_AddNode_JDialog - class for adding nodes interactively to
//	the network.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-03-16	J. Thomas Sapienza, RTi	Initial version.  
// 2004-07-07	JTS, RTi		* Confluence nodes can be added now.
//					* Grouped related items on the panels.
// 2004-07-12	JTS, RTi		Added support for XConfluence nodes.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import DWR.DMI.HydroBaseDMI.HydroBase_Node;
import DWR.DMI.HydroBaseDMI.HydroBase_NodeNetwork;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;

/**
Dialog for adding nodes interactively to the network.
*/
public class StateMod_Network_AddNode_JDialog
extends JDialog
implements ActionListener, KeyListener {

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = 	"Cancel",
	__BUTTON_OK = 		"OK";

/**
Node types for display in a combo box.
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
The node downstream of the node to be added.
*/
private HydroBase_Node __ds = null;

/**
Checkbox to mark whether the node is a baseflow node or not.
*/
private JCheckBox __baseflowJCheckBox;

/**
Checkbox to mark whether the node is a baseflow node or not.
*/
private JCheckBox __importJCheckBox;

/**
Ok button to accept entered values.
*/
private JButton __okJButton = null;

/**
Dialog text fields.
*/
private JTextField
	__downstreamIDJTextField,
	__nodeNameJTextField;

/**
Dialog combo boxes.
*/
private SimpleJComboBox
	__nodeTypeComboBox,
	__upstreamIDComboBox;

/**
The parent window on which this dialog is being displayed.
*/
private StateMod_Network_JFrame __parent = null;

/**
Constructor.
@param parent the JFrame on which this dialog will be shown.
@param ds the Downstream node from where the ndoe should be added.
*/
public StateMod_Network_AddNode_JDialog(StateMod_Network_JFrame parent,
HydroBase_Node ds) {
	super(parent, "Add Node", true);
	__parent = parent;
	__ds = ds;

	setupGUI();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String action = event.getActionCommand();
	Object o = event.getSource();

	if (action.equals(__BUTTON_CANCEL)) {
		dispose();
	}
	else if (action.equals(__BUTTON_OK)) {
		int type = -1;
		String s = __nodeTypeComboBox.getSelected();
		if (s.equals(__NODE_CONFLUENCE)) {
			type = HydroBase_Node.NODE_TYPE_CONFLUENCE;
		}
		else if (s.equals(__NODE_DIVERSION)) {
			type = HydroBase_Node.NODE_TYPE_DIV;
		}
		else if (s.equals(__NODE_DIVERSION_AND_WELL)) {
			type = HydroBase_Node.NODE_TYPE_DIV_AND_WELL;
		}
		else if (s.equals(__NODE_END)) {
			type = HydroBase_Node.NODE_TYPE_END;
		}
		else if (s.equals(__NODE_INSTREAM_FLOW)) {
			type = HydroBase_Node.NODE_TYPE_ISF;
		}
		else if (s.equals(__NODE_OTHER)) {
			type = HydroBase_Node.NODE_TYPE_OTHER;
		}
		else if (s.equals(__NODE_RESERVOIR)) {
			type = HydroBase_Node.NODE_TYPE_RES;
		}
		else if (s.equals(__NODE_STREAMFLOW)) {
			type = HydroBase_Node.NODE_TYPE_FLOW;
		}
		else if (s.equals(__NODE_WELL)) {
			type = HydroBase_Node.NODE_TYPE_WELL;
		}
		else if (s.equals(__NODE_XCONFLUENCE)) {
			type = HydroBase_Node.NODE_TYPE_XCONFLUENCE;
		}

		HydroBase_NodeNetwork network = __parent.getNetwork();

		String up = __upstreamIDComboBox.getSelected().trim();
		if (up.equals("[none]")) {
			up = null;
		}
		network.addNode(__nodeNameJTextField.getText().trim(),
			type, up,
			__downstreamIDJTextField.getText().trim(),
			__baseflowJCheckBox.isSelected(),
			__importJCheckBox.isSelected());
		__parent.setNetwork(network, true, true);
//		__parent.resetNodeSize();
		__parent.endAddNode();
		dispose();
	}
	else if (o == __nodeTypeComboBox) {
		String selected = __nodeTypeComboBox.getSelected();
		
		if (!selected.equals(__NODE_END)) {
			__baseflowJCheckBox.setEnabled(true);
			__importJCheckBox.setEnabled(true);
		}
	}
}

/**
Checks to make sure that the name entered in the node name field is valid.
*/
private void checkValidity() {
	if (!__nodeNameJTextField.getText().trim().equals("")) {
		__okJButton.setEnabled(true);
	}
	else {
		__okJButton.setEnabled(false);
	}
}

/**
Called when the user types something in the node name field -- checks to make 
sure that the entry is valid.
@param event the KeyEvent that happened.
*/
public void keyPressed(KeyEvent event) {
	checkValidity();
}

/**
Called when the user types something in the node name field -- checks to make 
sure that the entry is valid.
@param event the KeyEvent that happened.
*/
public void keyReleased(KeyEvent event) {
	checkValidity();
}

/**
Called when the user types something in the node name field -- checks to make 
sure that the entry is valid.
@param event the KeyEvent that happened.
*/
public void keyTyped(KeyEvent event) {
	checkValidity();
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	addWindowListener(__parent);

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	int y = 0;

	__downstreamIDJTextField = new JTextField(10);
	__downstreamIDJTextField.setEditable(false);
	__downstreamIDJTextField.setText(__ds.getCommonID());
	__upstreamIDComboBox = new SimpleJComboBox(false);
	__upstreamIDComboBox.setPrototypeDisplayValue(
		"[none] - Start a new TributaryXX");
	__nodeNameJTextField = new JTextField(10);
	__nodeNameJTextField.addKeyListener(this);

	String[] usid = __ds.getUpstreamNodesIDs();	
	for (int i = 0; i < usid.length; i++) {
		__upstreamIDComboBox.add(usid[i]);
	}
	__upstreamIDComboBox.add("[none] - Start a new Tributary");

	__nodeTypeComboBox = new SimpleJComboBox();

	__nodeTypeComboBox.add(__NODE_CONFLUENCE);
	__nodeTypeComboBox.add(__NODE_DIVERSION);
	__nodeTypeComboBox.add(__NODE_DIVERSION_AND_WELL);
	__nodeTypeComboBox.add(__NODE_INSTREAM_FLOW);
	__nodeTypeComboBox.add(__NODE_OTHER);
	__nodeTypeComboBox.add(__NODE_RESERVOIR);
	__nodeTypeComboBox.add(__NODE_STREAMFLOW);
	__nodeTypeComboBox.add(__NODE_WELL);
	__nodeTypeComboBox.add(__NODE_XCONFLUENCE);
	__nodeTypeComboBox.select(__NODE_STREAMFLOW);
	__nodeTypeComboBox.setMaximumRowCount(
		__nodeTypeComboBox.getItemCount());
	__nodeTypeComboBox.addActionListener(this);

	__baseflowJCheckBox = new JCheckBox();
	__baseflowJCheckBox.addActionListener(this);

	__importJCheckBox = new JCheckBox();
	__importJCheckBox.addActionListener(this);

	JPanel top = new JPanel();
	top.setLayout(new GridBagLayout());
	top.setBorder(BorderFactory.createTitledBorder(
		"Existing Nodes"));

	JPanel bottom = new JPanel();
	bottom.setLayout(new GridBagLayout());
	bottom.setBorder(BorderFactory.createTitledBorder(
		"New Node Data"));

	JGUIUtil.addComponent(panel, top,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, bottom,
		0, 1, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	y = 0;
	JGUIUtil.addComponent(top, new JLabel("Downstream Node: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __downstreamIDJTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(top, new JLabel("Upstream Node: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __upstreamIDComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	y = 0;
	JGUIUtil.addComponent(bottom, new JLabel("Node ID: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(bottom, __nodeNameJTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(bottom, new JLabel("Node Type: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(bottom, __nodeTypeComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(bottom, new JLabel("Is Baseflow: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(bottom, __baseflowJCheckBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(bottom, new JLabel("Is Import: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(bottom, __importJCheckBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel southPanel = new JPanel();
	southPanel.setLayout(new GridBagLayout());
	
	__okJButton = new JButton(__BUTTON_OK);
	__okJButton.addActionListener(this);
	__okJButton.setEnabled(false);
	JButton cancelButton = new JButton(__BUTTON_CANCEL);
	cancelButton.addActionListener(this);

	JGUIUtil.addComponent(southPanel, __okJButton,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(southPanel, cancelButton,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	getContentPane().add(panel);
	getContentPane().add(southPanel, "South");

	pack();
	JGUIUtil.center(this);
	setVisible(true);
}

}
