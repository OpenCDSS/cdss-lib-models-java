// StateMod_Network_AddLink_JDialog - dialog for adding nodes interactively to the network.

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
import javax.swing.JTextField;

import cdss.domain.hydrology.network.HydrologyNode;
import RTi.GR.GRArrowStyleType;
import RTi.GR.GRLineStyleType;
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
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK";

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
	__node2ComboBox,
	__lineStyleComboBox,
	__fromArrowStyleComboBox,
	__toArrowStyleComboBox;

private JTextField
	__linkId_JTextField;

private StateMod_Network_JComponent __device = null;

/**
The parent window on which this dialog is being displayed.
*/
private StateMod_Network_JFrame __parent = null;

/**
Constructor.
@param parent the JFrame on which this dialog will be shown.
@param ds the Downstream node from where the node should be added.
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
		__device.addLink(__node1ComboBox.getSelected(), __node2ComboBox.getSelected(),
			__linkId_JTextField.getText(), __lineStyleComboBox.getSelected(),
			__fromArrowStyleComboBox.getSelected(), __toArrowStyleComboBox.getSelected() );
		dispose();
	}
	else {
		checkValidity();
	}
}

/**
*/
private void checkValidity() {
	if (!__node1ComboBox.getSelected().equals(__node2ComboBox.getSelected())) {
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

	List<String> nodeIds = new Vector(__nodes.length);
	for (int i = 0; i < __nodes.length; i++) {	
		nodeIds.add(__nodes[i].getCommonID());
	}
	java.util.Collections.sort(nodeIds);

	JPanel top = new JPanel();
	top.setLayout(new GridBagLayout());

	JGUIUtil.addComponent(panel, top,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	y = 0;
	__node1ComboBox = new SimpleJComboBox(nodeIds);
	__node1ComboBox.addItemListener(this);
	__node1ComboBox.addActionListener(this);
	JGUIUtil.addComponent(top, new JLabel("From node: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __node1ComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__node2ComboBox = new SimpleJComboBox(nodeIds);
	__node2ComboBox.addItemListener(this);
	__node2ComboBox.addActionListener(this);
	JGUIUtil.addComponent(top, new JLabel("To node: "),
		0, ++y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __node2ComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	__linkId_JTextField = new JTextField(10);
	JGUIUtil.addComponent(top, new JLabel("Link ID: "),
		0, ++y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __linkId_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	List<String> lineStyleChoices = new Vector();
	lineStyleChoices.add ( "" + GRLineStyleType.DASHED );
	lineStyleChoices.add ( "" + GRLineStyleType.SOLID );
	__lineStyleComboBox = new SimpleJComboBox(lineStyleChoices);
	__lineStyleComboBox.addItemListener(this);
	__lineStyleComboBox.addActionListener(this);
	JGUIUtil.addComponent(top, new JLabel("Line style: "),
		0, ++y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __lineStyleComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	List<String> arrowEndChoices = new Vector();
	arrowEndChoices.add ( "" + GRArrowStyleType.NONE );
	arrowEndChoices.add ( "" + GRArrowStyleType.SOLID );
	__fromArrowStyleComboBox = new SimpleJComboBox(arrowEndChoices);
	__fromArrowStyleComboBox.addItemListener(this);
	__fromArrowStyleComboBox.addActionListener(this);
	JGUIUtil.addComponent(top, new JLabel("Arrow (from) style: "),
		0, ++y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __fromArrowStyleComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	__toArrowStyleComboBox = new SimpleJComboBox(arrowEndChoices);
	__toArrowStyleComboBox.addItemListener(this);
	__toArrowStyleComboBox.addActionListener(this);
	JGUIUtil.addComponent(top, new JLabel("Arrow (to) style: "),
		0, ++y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(top, __toArrowStyleComboBox,
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
