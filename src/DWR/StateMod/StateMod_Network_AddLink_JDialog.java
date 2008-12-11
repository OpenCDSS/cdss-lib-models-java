// ----------------------------------------------------------------------------
// StateMod_Network_AddLink_JDialog - class for adding links interactively to
//	the network.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-07-12	J. Thomas Sapienza, RTi	Initial version.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cdss.domain.hydrology.network.HydrologyNode;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;

/**
Dialog for adding nodes interactively to the network.
*/
public class StateMod_Network_AddLink_JDialog
extends JDialog
implements ActionListener, ItemListener {

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = 	"Cancel",
	__BUTTON_OK = 		"OK";

private HydrologyNode[] __nodes = null;

/**
Ok button to accept entered values.
*/
private JButton __okJButton = null;

/**
Dialog combo boxes.
*/
private SimpleJComboBox
	__node1ComboBox,
	__node2ComboBox;

private StateMod_Network_JComponent __device = null;

/**
The parent window on which this dialog is being displayed.
*/
private StateMod_Network_JFrame __parent = null;

/**
Constructor.
@param parent the JFrame on which this dialog will be shown.
@param ds the Downstream node from where the ndoe should be added.
*/
public StateMod_Network_AddLink_JDialog(StateMod_Network_JFrame parent,
StateMod_Network_JComponent device, HydrologyNode[] nodes) {
	super(parent, "Add Link", true);
	__parent = parent;
	__device = device;
	__nodes = nodes;

	setupGUI();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String action = event.getActionCommand();

	if (action.equals(__BUTTON_CANCEL)) {
		dispose();
	}
	else if (action.equals(__BUTTON_OK)) {
		__device.addLink(__node1ComboBox.getSelected(), __node2ComboBox.getSelected());
		dispose();
	}
	else {
		checkValidity();
	}
}

/**
*/
private void checkValidity() {
	if (!__node1ComboBox.getSelected().equals(
		__node2ComboBox.getSelected())) {
		__okJButton.setEnabled(true);
	}
	else {
		__okJButton.setEnabled(false);
	}
}

public void finalize() 
throws Throwable {
	// DO NOT FINALIZE __nodes IN HERE.
	super.finalize();
}

public void itemStateChanged(ItemEvent event) {
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

	List nodes = new Vector(__nodes.length);
	for (int i = 0; i < __nodes.length; i++) {	
		nodes.add(__nodes[i].getCommonID());
	}
	java.util.Collections.sort(nodes);

	__node1ComboBox = new SimpleJComboBox(nodes);
	__node1ComboBox.addItemListener(this);
	__node1ComboBox.addActionListener(this);
	__node2ComboBox = new SimpleJComboBox(nodes);
	__node2ComboBox.addItemListener(this);
	__node2ComboBox.addActionListener(this);
	
	JPanel top = new JPanel();
	top.setLayout(new GridBagLayout());

	JGUIUtil.addComponent(panel, top,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	y = 0;
	JGUIUtil.addComponent(top, new JLabel("From Node: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __node1ComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	JGUIUtil.addComponent(top, new JLabel("To Node: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __node2ComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

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