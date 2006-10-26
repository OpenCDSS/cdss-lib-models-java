//------------------------------------------------------------------------------
// StateMod_Reservoir_AreaCap_JFrame - frame to edit a reservoir's 
//	content/area/seepage curve
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 24 Dec 1997	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 01 Apr 2001	Steven A. Malers, RTi	Change GUI to JGUIUtil.  Add finalize().
//					Remove import *.
//------------------------------------------------------------------------------
// 2003-06-09	J. Thomas Sapienza, RTi	Initial swing version from 
//					SMresAreaCapFrame
// 2003-06-13	JTS, RTi		Began using StateMod_Reservoir code
// 2003-06-16	JTS, RTi		Javadoc'd.
// 2003-07-15	JTS, RTi		* Added status bar.
//					* Changed to use new dataset design.
// 2003-07-17	JTS, RTI		Change so that constructor takes a 
//					boolean that says whether the form's
//					data can be modified.
// 2003-07-23	JTS, RTi		Updated JWorksheet code following
//					JWorksheet revisions.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-29	SAM, RTi		Change due to changes in
//					StateMod_Reservoir.
// 2003-09-23	JTS, RTi		Uses new StateMod_GUIUtil code for
//					setting titles.
// 2003-10-13	JTS, RTi		* Worksheet now uses multiple-line
//					  headers.
// 					* Added saveData().
//					* Added checkInput().
//					* Added apply and cancel buttons.
// 2004-01-21	JTS, RTi		Updated to use JScrollWorksheet and
//					the new row headers.
// 2004-07-15	JTS, RTi		Changed layout of buttons to be
//					aligned in the lower-right.
// 2004-10-28	SAM, RTi		Use new table model that deals only with
//					area/cap data.
// 2005-01-21	JTS, RTi		Table model constructor changed.
// 2006-02-28	SAM, RTi		* Use JFreeChart to display the area
//					  capacity curves.
//					* Change the "capacity" in the title to
//					  "content".
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirAreaCap;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This class is a gui for displaying area cap information associated with a 
reservoir, and deleting and adding area cap information to the reservoir.
*/
public class StateMod_Reservoir_AreaCap_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
String labels for the buttons.
*/
private final static String 
	__GraphArea_String = "Graph Area",
	__GraphSeepage_String = "Graph Seepage",
	__BUTTON_ADD_AREA_CAPACITY = 	"Add line",
	__BUTTON_APPLY = 		"Apply",
	__BUTTON_CANCEL = 		"Cancel",			
	__BUTTON_DEL_AREA_CAPACITY = 	"Delete line",
	__BUTTON_HELP = 		"Help",
	__BUTTON_CLOSE = 		"Close";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
GUI Buttons.
*/
private SimpleJButton
	__GraphArea_JButton,
	__GraphSeepage_JButton;
private JButton 
	__addAreaCap,
	__closeJButton,
	__deleteAreaCap,
	__helpJButton;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
Worksheet in which area cap information will be displayed.
*/
private JWorksheet __worksheet; 

/**
The dataset containing the data.
*/
private StateMod_DataSet __dataset;

/**
The current reservoir of which the data is being displayed.
*/
private StateMod_Reservoir __currentRes;

/**
Constructor.
@param dataset the dataset in which the data is contained.
@param res the reservoir object to display in the frame.
@param editable whether the gui data is editable or not
*/
public StateMod_Reservoir_AreaCap_JFrame(StateMod_DataSet dataset, 
StateMod_Reservoir res, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, res.getName() 
		+ " - Reservoir Content/Area/Seepage", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	
	__currentRes = res;

	__dataset = dataset;

	__editable = editable;

	setupGUI();
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();

	if (action.equals(__GraphArea_String)) {
		graph(__GraphArea_String);
	}
	else if (action.equals(__GraphSeepage_String)) {
		graph(__GraphSeepage_String);
	}
	else if (action.equals(__BUTTON_ADD_AREA_CAPACITY)) {
		StateMod_ReservoirAreaCap anAreaCapNode = 
			new StateMod_ReservoirAreaCap();
		anAreaCapNode._isClone = true;
		__worksheet.addRow(anAreaCapNode);
		__worksheet.scrollToLastRow();			
		__worksheet.selectLastRow();
		__deleteAreaCap.setEnabled(true);
	}
	else if (action.equals(__BUTTON_DEL_AREA_CAPACITY)) {
		int row = __worksheet.getSelectedRow();
		if (row != -1) {	
			int x = new ResponseJDialog(this,
				"Delete Content/Area/Seepage line?",
				"Delete Content/Area/Seepage line?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.NO) {
				return;
			}
			__worksheet.deleteRow(row);
			__deleteAreaCap.setEnabled(false);
		}
	}
	else if (action.equals(__BUTTON_CLOSE)) {
		if (saveData()) {
			setVisible(false);
			dispose();
		}
	}
	else if (action.equals(__BUTTON_APPLY)) {
		saveData();
	}
	else if (action.equals(__BUTTON_CANCEL)) {
		setVisible(false);
		dispose();
	}		
	else if (action.equals(__BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-06-09)
	}
}

/**
Check the GUI state.  In particular, indicate whether the graph buttons should
be enabled.
*/
private void checkGUIState ()
{	Vector rv = __currentRes.getAreaCaps();	
	if ( (rv != null) && (rv.size() > 0) ) {
		boolean area_ok = true, seepage_ok = true;
		// Check for data all one value...
		int size = rv.size();
		StateMod_ReservoirAreaCap ac = null;
		double value, value_prev = -99999.0;
		// REVISIT SAM 2006-08-20
		// JFreeChart has a problem when the values are the same as
		// the previous values.  However, for now, increment the values
		// slightly when graphing rather than disabling the graph.
		// Go ahead and disable if all values are the same.
		double area0 = 0.0, seepage0 = 0.0;
		boolean area_all_same = true, seepage_all_same = true;
		for ( int i = 0; i < size; i++ ) {
			ac = (StateMod_ReservoirAreaCap)rv.elementAt(i);
			value = ac.getSurarea();
			/*
			if ( value == value_prev ) {
				area_ok = false;
				break;
			}
			*/
			if ( i == 0 ) {
				area0 = value;
			}
			else {	if ( value != area0 ) {
					area_all_same = false;
				}
			}
			value_prev = value;
		}
		value_prev = -99999.0;
		for ( int i = 0; i < size; i++ ) {
			ac = (StateMod_ReservoirAreaCap)rv.elementAt(i);
			value = ac.getSeepage();
			/*
			if ( value == value_prev ) {
				seepage_ok = false;
				break;
			}
			*/
			if ( i == 0 ) {
				seepage0 = value;
			}
			else {	if ( value != seepage0 ) {
					seepage_all_same = false;
				}
			}
			value_prev = value;
		}
		if ( area_all_same ) {
			area_ok = false;
		}
		if ( seepage_all_same ) {
			seepage_ok = false;
		}
		JGUIUtil.setEnabled ( __GraphArea_JButton, area_ok );
		JGUIUtil.setEnabled ( __GraphSeepage_JButton, seepage_ok );
	}
	else {	JGUIUtil.setEnabled ( __GraphArea_JButton, false );
		JGUIUtil.setEnabled ( __GraphSeepage_JButton, false );
	}
	// Only enable the graph buttons if the new charting package is in the
	// path.  This will allow for some graceful transition to distribution
	// of the new software.
	if ( !IOUtil.classCanBeLoaded("org.jfree.chart.ChartPanel") ) {
		JGUIUtil.setEnabled ( __GraphArea_JButton, false );
		JGUIUtil.setEnabled ( __GraphSeepage_JButton, false );
	}
}

/**
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput() {
	// all the checking can be handled by the worksheet, since all the
	// values are numeric
	return 0;
}

/**
Saves the input back into the dataset.
@return true if the data was saved successfuly.  False if not.
*/
private boolean saveData() {
	String routine = "StateMod_Reservoir_AreaCap_JFrame.saveData";
	if (!__worksheet.stopEditing()) {
		// don't save if there are errors.
		Message.printWarning(1, routine, "There are errors in the data "
			+ "that must be corrected before data can be saved.",
			this);
		return false;
	}
	
	if (checkInput() > 0) {
		return false;
	}

	// now only save data if any are different.
	boolean needToSave = false;

	// if the Vectors are differently-sized, they're different
	Vector wv = __worksheet.getAllData();		// w for worksheet
	Vector rv = __currentRes.getAreaCaps();	

	needToSave = !(StateMod_ReservoirAreaCap.equals(wv, rv));

	Message.printStatus(1, routine, "Saving? .........[" + needToSave +"]");

	if (!needToSave) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	// now add the elements from the new Vector to the reservoirRights 
	// Vector.
	int size = wv.size();
	Vector clone = new Vector();
	for (int i = 0; i < size; i++) {
		clone.add(((StateMod_ReservoirAreaCap)
			(wv.elementAt(i))).clone());
	}

	__currentRes.setAreaCaps(clone);
	__dataset.setDirty(__dataset.COMP_DIVERSION_STATIONS, true);
	return true;
}

/**
Checks to see if the __deleteAreaCap button should be enabled or not.
*/
private void checkDeleteAreaCapButton() {
	int row = __worksheet.getSelectedRow();
	if (row == -1) {
		__deleteAreaCap.setEnabled(false);
	}
	else {
		__deleteAreaCap.setEnabled(true);
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__currentRes = null;
	__worksheet = null;
	__addAreaCap = null;
	__deleteAreaCap = null;
	__helpJButton = null;
	__closeJButton = null;
	super.finalize();
}

/**
Show a graph of data.
@param choice Indicate data to graph, either __GraphArea_String or
__GraphSeepage_String.
*/
private void graph ( String choice )
{	// For now all the display setup is done in this one method.
	// REVISIT SAM 2006-02-28 This could be made more general.
	if ( choice.equals(__GraphArea_String) ) {
		new StateMod_Reservoir_AreaCap_Graph_JFrame ( __dataset,
			__currentRes,
			"Area", false );
	}
	else if ( choice.equals(__GraphSeepage_String) ) {
		new StateMod_Reservoir_AreaCap_Graph_JFrame ( __dataset,
			__currentRes,
			"Seepage", false );
	}
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; checks if the __deleteAreaCap button should
be enabled.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	checkDeleteAreaCapButton();
}

/**
Responds to key typed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyTyped(KeyEvent e) {}

/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {}

/**
Responds to mouse pressed events; does nothing.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {}

/**
Responds to mouse released events; checks to see if the __deleteAreaCap button
should be enabled or not.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	checkDeleteAreaCapButton();
}

/**
Responds to mouse entered events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent e) {}

/**
Responds to mouse exited events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseExited(MouseEvent e) {}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "setupGUI";

	addWindowListener(this);

	GridBagConstraints gbc = new GridBagConstraints();

	__addAreaCap = new JButton(__BUTTON_ADD_AREA_CAPACITY);
	__deleteAreaCap = new JButton(__BUTTON_DEL_AREA_CAPACITY);
	__deleteAreaCap.setEnabled(false);
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);			

	GridBagLayout gb = new GridBagLayout();
	JPanel bigPanel = new JPanel(); 
	bigPanel.setLayout(gb);

	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel p0 = new JPanel();
	p0.setLayout(fl);
	p0.add(__GraphArea_JButton = new SimpleJButton(
		__GraphArea_String,
		__GraphArea_String, this) );
	p0.add(__GraphSeepage_JButton = new SimpleJButton(
		__GraphSeepage_String,
		__GraphSeepage_String, this) );

	GridLayout gl = new GridLayout(2, 2, 1, 1);
	JPanel info_panel = new JPanel();
	info_panel.setLayout(gl);

	//JPanel main_panel = new JPanel();
	//main_panel.setLayout(new BorderLayout());

	info_panel.add(new JLabel("Reservoir:"));
	info_panel.add(new JLabel(__currentRes.getID()));
	info_panel.add(new JLabel("Reservoir name:"));
	info_panel.add(new JLabel(__currentRes.getName()));

	JPanel p1 = new JPanel();
	p1.setLayout(fl);
	if (__editable) {
		p1.add(__addAreaCap);
		p1.add(__deleteAreaCap);
	}
	p1.add(applyJButton);
	p1.add(cancelJButton);				
//	p1.add(__helpJButton);
	p1.add(__closeJButton);

	PropList p =
	       new PropList("StateMod_Reservoir__AreaCap_JFrame.JWorksheet");
	/*
	p.add("JWorksheet.CellFont=Courier");
	p.add("JWorksheet.CellStyle=Plain");
	p.add("JWorksheet.CellSize=11");
	p.add("JWorksheet.HeaderFont=Arial");
	p.add("JWorksheet.HeaderStyle=Plain");
	p.add("JWorksheet.HeaderSize=11");
	p.add("JWorksheet.HeaderBackground=LightGray");
	p.add("JWorksheet.RowColumnPresent=false");
	*/
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");
	
	int widths[] = null;
	JScrollWorksheet jsw = null;
	try {	
		Vector v = new Vector();
		Vector v2 = __currentRes.getAreaCaps();
		for (int i = 0; i < v2.size(); i++) {
			v.add(((StateMod_ReservoirAreaCap)
				(v2.elementAt(i))).clone());
		}				
		StateMod_ReservoirAreaCap_TableModel tmr = new
			StateMod_ReservoirAreaCap_TableModel(
			v, __editable, true);
		StateMod_ReservoirAreaCap_CellRenderer crr = new
			StateMod_ReservoirAreaCap_CellRenderer(tmr);
		
		jsw = new JScrollWorksheet(crr, tmr, p);
		__worksheet = jsw.getJWorksheet();

		widths = crr.getColumnWidths();		
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);

	//main_panel.add(jsw, "Center");
	// Does not work well...
	//main_panel.add(p1, "South");

	// assemble parts 
	JGUIUtil.addComponent(bigPanel, info_panel,
		0, 0, 1, 1,
		0, 0,
		gbc.HORIZONTAL, gbc.NORTHWEST);
	JGUIUtil.addComponent(bigPanel, jsw,
		0, 1, 10, 10,
		1.0, 1.0,
		gbc.BOTH, gbc.SOUTH);
	JPanel button_panel = new JPanel();
	button_panel.setLayout ( gb );
	JGUIUtil.addComponent(button_panel, p0,
		0, 0, 10, 1,
		0, 0,
		gbc.HORIZONTAL, gbc.SOUTHEAST);
	JGUIUtil.addComponent(button_panel, p1,
		0, 1, 10, 1,
		0, 0,
		gbc.HORIZONTAL, gbc.SOUTHEAST);
	JGUIUtil.addComponent(bigPanel, button_panel,
		0, 11, 10, 1,
		0, 0,
		gbc.HORIZONTAL, gbc.SOUTHEAST);
	__addAreaCap.addActionListener(this);
	__deleteAreaCap.addActionListener(this);
	__helpJButton.addActionListener(this);
	__closeJButton.addActionListener(this);
	applyJButton.addActionListener(this);
	cancelJButton.addActionListener(this);		

	getContentPane().add(bigPanel);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 0, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__statusJTextField = new JTextField(5);
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __statusJTextField,
		7, 0, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottomJPanel);	

	pack();
	checkGUIState();
	setSize(420, 400);
	JGUIUtil.center(this);
	setVisible(true);

	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
}

/**
Responds to window activated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowActivated(WindowEvent e) {}

/**
Responds to window closed events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowClosed(WindowEvent e) {}

/**
Responds to window closing events; closes up the window properly.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	if (saveData()) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(false);
		dispose();
	}
	else {
		setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
	}
}

/**
Responds to window deactivated events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Responds to window deiconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Responds to window iconified events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {}

/**
Responds to window opened events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpened(WindowEvent e) {}

/**
Responds to window opening events; does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpening(WindowEvent e) {}

}
