// ----------------------------------------------------------------------------
// StateMod_Network_AnnotationProperties_JDialog - class to display and edit
// 	annotation properties for a node in the diagram display.
// ----------------------------------------------------------------------------
//  Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
// 2004-07-12	J. Thomas Sapienza, RTi	Initial version from
//					HydroBase_GUI_WISDiagramNodeProperties.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import DWR.DMI.HydroBaseDMI.HydroBase_Node;
import RTi.GR.GRLimits;
import RTi.GR.GRText;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
GUI for displaying, editing, and validating node properties for normal nodes
and annotations.
*/
public class StateMod_Network_AnnotationProperties_JDialog 
extends JFrame 
implements ActionListener, KeyListener, WindowListener {
	
/**
Button labels.
*/
private final String
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK";

/**
Whether the node data are editable.
*/
private boolean __editable = false;

/**
The default background color of editable text fields.
*/
private Color __textFieldBackground = null;

/**
The GUI that instantiated this GUI.
*/
private StateMod_Network_JComponent __parent;

/**
The node that is being edited.
*/
private HydroBase_Node __node;

/**
The number of the node (for use in the parent GUI).
*/
private int __nodeNum = -1;

/**
GUI buttons
*/
private JButton 
	__applyButton = null,
	__okButton = null;

/**
GUI text fields.
*/
private JTextField 
	__fontSizeTextField,
	__textTextField,
	__xTextField,
	__yTextField;

/**
GUI combo boxes.
*/
private SimpleJComboBox
	__fontNameComboBox,
	__fontStyleComboBox,
	__textPositionComboBox;

/**
Constructor.
@param dmi the dmi to use for getting data from the database.
@param parent the parent GUI that instantiated this GUI.
@param node the node for which to edit properties.
@param nodeNum the number of the node in the parent GUI.
*/
public StateMod_Network_AnnotationProperties_JDialog(
StateMod_Network_JComponent parent, boolean editable, 
HydroBase_Node node, int nodeNum) {
	__parent = parent;
	__editable = editable;
	__node = node;
	__nodeNum = nodeNum;

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	setupGUI();
}

/**
Responds to button presses.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(__BUTTON_APPLY)) {
		applyChanges();
		__node = __parent.getAnnotationNode(__nodeNum);
		__applyButton.setEnabled(false);
		__okButton.setEnabled(false);
	}
	else if (command.equals(__BUTTON_CANCEL)) {
		closeWindow();
	}
	else if (command.equals(__BUTTON_OK)) {
		if (__editable) {
			applyChanges();
		}
		closeWindow();
	}
	else {
		// a combo box action triggered it.
		validateData();
	}
}

/**
Saves the changes made in the GUI and applies them to the node in the parent 
GUI.
*/
private void applyChanges() {
	if (!validateData()) {
		// if the data are not valid, don't close
		return;
	}

	HydroBase_Node node = new HydroBase_Node();
	PropList p = new PropList("");
	String temp = null;

	temp = __textTextField.getText().trim();
	temp = StringUtil.replaceString(temp, "\"", "'");
	p.set("Text", temp);

	temp = __xTextField.getText().trim() + ","
		+ __yTextField.getText().trim();
	p.set("Point", temp);

	temp = __textPositionComboBox.getSelected();
	p.set("TextPosition", temp);

	temp = __fontNameComboBox.getSelected().trim();
	p.set("FontName", temp);

	temp = __fontSizeTextField.getText().trim();
	p.set("OriginalFontSize", temp);

	temp = __fontStyleComboBox.getSelected().trim();
	p.set("FontStyle", temp);
	node.setAssociatedObject(p);

	node.setDirty(true);

	__parent.updateAnnotation(__nodeNum, node);
	__applyButton.setEnabled(false);
	__okButton.setEnabled(false);
}

/**
Closes the GUI.
*/
private void closeWindow() {
	setVisible(false);
	dispose();
}

/**
Displays the annotation values from the annotation proplist in the components
in the GUI.
*/
private void displayPropListValues() {
	PropList p = (PropList)__node.getAssociatedObject();

	String temp = null;
	String val = null;

	val = p.getValue("Text").trim();
	__textTextField.setText(val);

	val = p.getValue("Point").trim();
	temp = StringUtil.getToken(val, ",", 0, 0);
	__xTextField.setText(StringUtil.formatString(temp, "%20.6f").trim());
	temp = StringUtil.getToken(val, ",", 0, 1);
	__yTextField.setText(StringUtil.formatString(temp, "%20.6f").trim());

	val = p.getValue("TextPosition").trim();
	__textPositionComboBox.select(val);

	val = p.getValue("FontName").trim();
	__fontNameComboBox.select(val);

	val = p.getValue("OriginalFontSize").trim();
	__fontSizeTextField.setText(val);

	val = p.getValue("FontStyle").trim();
	__fontStyleComboBox.select(val);
}

/**
Responds when users press the enter button in an edit field.  Saves the changes
and closes the GUI.
@param event that KeyEvent that happened.
*/
public void keyPressed(KeyEvent event) {
	if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		if (__editable) {
			applyChanges();
		}
		closeWindow();
	}
}

/**
Responds after users presses a key -- tries to validate the data that has
been entered.
*/
public void keyReleased(KeyEvent event) {
	validateData();
}

/**
Does nothing.
*/
public void keyTyped(KeyEvent event) {}

/**
Sets up the GUI.
*/
private void setupGUI() {
	addWindowListener(this);

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	int y = 0;

	JGUIUtil.addComponent(panel, new JLabel("Text:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("X:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("Y:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("Text Position:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("Font Name:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("Font Size:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, new JLabel("Font Style:"),
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	
	__textTextField = new JTextField(30);
	__textFieldBackground = __textTextField.getBackground();
	__xTextField = new JTextField(15);
	__yTextField = new JTextField(15);

	__textPositionComboBox = new SimpleJComboBox(false);
	String[] positions = GRText.getTextPositions();
	__textPositionComboBox.setMaximumRowCount(positions.length);
	for (int i = 0; i < positions.length; i++) {
		__textPositionComboBox.addItem(positions[i]);
	}
	
	__fontNameComboBox = JGUIUtil.newFontNameJComboBox();
	__fontSizeTextField = new JTextField(7);
	__fontStyleComboBox = JGUIUtil.newFontStyleJComboBox();
	
	displayPropListValues();

	__textTextField.addKeyListener(this);
	__xTextField.addKeyListener(this);
	__yTextField.addKeyListener(this);
	__fontSizeTextField.addKeyListener(this);
	__textPositionComboBox.addActionListener(this);
	__fontNameComboBox.addActionListener(this);
	__fontStyleComboBox.addActionListener(this);
	
	y = 0;
	JGUIUtil.addComponent(panel, __textTextField,
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __xTextField,
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __yTextField,
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __textPositionComboBox,
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __fontNameComboBox,
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __fontSizeTextField,
		1, y++, 1, 1, 1, 1,
	GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __fontStyleComboBox,
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	if (!__editable) {
		__textTextField.setEditable(false);
		__xTextField.setEditable(false);
		__yTextField.setEditable(false);
		__textPositionComboBox.setEnabled(false);
		__fontNameComboBox.setEnabled(false);
		__fontSizeTextField.setEditable(false);
		__fontStyleComboBox.setEnabled(false);
	}	

	__applyButton = new JButton(__BUTTON_APPLY);
	if (__editable) {
		__applyButton.setToolTipText("Apply changes.");
		__applyButton.addActionListener(this);
	}
	__applyButton.setEnabled(false);

	JButton cancelButton = new JButton(__BUTTON_CANCEL);
	cancelButton.setToolTipText("Discard changes and return.");
	cancelButton.addActionListener(this);

	__okButton = new JButton(__BUTTON_OK);
	__okButton.setToolTipText("Accept changes and return.");
	__okButton.setEnabled(false);
	__okButton.addActionListener(this);

	JPanel southPanel = new JPanel();
	southPanel.setLayout(new GridBagLayout());

	if (__editable) {
		JGUIUtil.addComponent(southPanel, __applyButton,
			0, 0, 1, 1, 1, 1,
			0, 10, 0, 10,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
	}

	int space = 0;
	if (!__editable) {
		space = 1;
	}

	JGUIUtil.addComponent(southPanel, __okButton,
		1, 0, 1, 1, space, space,
		0, 10, 0, 10,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	if (__editable) {
		JGUIUtil.addComponent(southPanel, cancelButton,
			2, 0, 1, 1, 0, 0,
			0, 10, 0, 10,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else {
		__okButton.setEnabled(true);
	}

	getContentPane().add("Center", panel);
	getContentPane().add("South", southPanel);

	String app = JGUIUtil.getAppNameForWindows();
	if (app == null || app.trim().equals("")) {
		app = "";
	}
	else {
		app += " - ";	
	}	
	setTitle(app + "Node Properties");

	pack();
	setSize(getWidth() + 100, getHeight());
	JGUIUtil.center(this);
	show();
}

/**
Validates data entered in the GUI.  If any values are invalid (non-numbers in
the X and Y fields, blank label field), the OK button is disabled and the
field is highlighted in red.
@return true if all the text is valid.  False if not.
*/
private boolean validateData() {
	String text = __textTextField.getText().trim();
	double x = -1;
	boolean badX = false;
	double y = -1;
	boolean badY = false;

	GRLimits limits = __parent.getDataLimits();

	// make sure the X value is a double and that it is within the range
	// of the X values in the data limits
	try {
		x = (new Double(__xTextField.getText().trim())).doubleValue();
		if (x < limits.getLeftX() || x > limits.getRightX()) {
			badX = true;
		}
	}
	catch (Exception e) {
		badX = true;
	}
	
	// if the X value is not valid, set the textfield red to show this. 
	// Otherwise, make sure the text has the proper background color.
	if (badX) {
		__xTextField.setBackground(Color.red);
	}
	else {
		__xTextField.setBackground(__textFieldBackground);
	}

	// make sure the Y value is a double and that it is within the range
	// of the Y values in the data limits
	try {
		y = (new Double(__yTextField.getText().trim())).doubleValue();
		if (y < limits.getBottomY() || x > limits.getTopY()) {
			badY = true;
		}
	}
	catch (Exception e) {
		badY = true;
	}
	
	// if the Y value is not valid, set the textfield red to show this. 
	// Otherwise, make sure the text has the proper background color.
	if (badY) {
		__yTextField.setBackground(Color.red);
	}
	else {
		__yTextField.setBackground(__textFieldBackground);
	}
	
	// make sure that the text is not an empty string.  If it is, make
	// its textfield red.  Otherwise, the textfield will have the normal
	// textfield color.
	boolean badText = false;
	if (text.trim().equals("")) {
		badText = true;
		__textTextField.setBackground(Color.red);		
	}
	else {
		__textTextField.setBackground(__textFieldBackground);
	}

	// make sure that the font size is an integer greater than 0.  If not,
	// set its textfield to red.  Otherwise the textfield will have a
	// normal textfield color.
	boolean badFontSize = false;
	int size = 0;
	try {
		size = (new Integer(
			__fontSizeTextField.getText().trim()))
			.intValue();
	}
	catch (Exception e) {
		badFontSize = true;
	}

	if (size <= 0) {	
		badFontSize = true;
	}

	if (badFontSize) {
		__fontSizeTextField.setBackground(Color.red);
	}
	else {
		__fontSizeTextField.setBackground(
			__textFieldBackground);
	}

	if (!badText && !badX && !badY && !badFontSize) {
		// if all the data validated properly then mark whether
		// the data are dirty or not.   OK is only active if 
		// the data are valid and something is dirty.
		boolean dirty = false;
		PropList p = (PropList)__node.getAssociatedObject();
		
		String temp = p.getValue("Text").trim();
		if (!temp.equals(__textTextField.getText().trim())) {
			dirty = true;
		}

		String val = p.getValue("Point").trim();
		temp = StringUtil.getToken(val, ",", 0, 0);

		if (!temp.equals(__xTextField.getText().trim())) {
			dirty = true;
		}
		
		temp = StringUtil.getToken(val, ",", 0, 1);
		if (!temp.equals(__yTextField.getText().trim())) {
			dirty = true;
		}

		temp = p.getValue("OriginalFontSize").trim();
		if (!temp.equals(
			__fontSizeTextField.getText().trim())) {
			dirty = true;
		}

		temp = p.getValue("FontName").trim();
		if (!temp.equals(
			__fontNameComboBox.getSelected().trim())) {
			dirty = true;
		}

		temp = p.getValue("FontStyle").trim();
		if (!temp.equals(
			__fontStyleComboBox.getSelected().trim())) {
			dirty = true;
		}

		temp = p.getValue("TextPosition").trim();
		if (!temp.equals(
			__textPositionComboBox.getSelected().trim())) {
			dirty = true;
		}

		__applyButton.setEnabled(dirty);
		__okButton.setEnabled(dirty);
		return true;
	}
	else {
		// if the data aren't valid, the ok button is not enabled
		__applyButton.setEnabled(false);
		__okButton.setEnabled(false);
		return false;
	}
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Closes the GUI.
*/
public void windowClosing(WindowEvent event) {
	closeWindow();
}
/**
Does nothing.
*/
public void windowClosed(WindowEvent event) {}

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

}
