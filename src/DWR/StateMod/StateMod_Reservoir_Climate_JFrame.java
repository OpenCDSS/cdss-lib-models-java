// StateMod_Reservoir_Climate_JFrame - dialog to edit a reservoir's climate information.

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
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class is a gui for displaying the climate stations associated with 
a reservoir, and adding and deleting stations from the reservoir.
*/
@SuppressWarnings("serial")
public class StateMod_Reservoir_Climate_JFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, WindowListener {

/**
Text to appear on the GUI buttons.
*/
private final String 
	__BUTTON_ADD_PRECIPITATION_STATION = 	"Add precipitation station",
	__BUTTON_ADD_EVAPORATION_STATION = 	"Add evaporation station",
	__BUTTON_APPLY = 			"Apply",
	__BUTTON_CANCEL = 			"Cancel",		
	__BUTTON_CLOSE = 			"Close",
	__BUTTON_DELETE_PRECIPITATION_STATION =	"Delete Station",
	__BUTTON_HELP = 			"Help";

/**
Whether the gui data is editable or not.
*/
private boolean __editable = false;

/**
GUI Buttons.
*/
private JButton 
	__addEvap,
	__addPrecip,
	__closeJButton,
	__deleteStation,
	__helpJButton;

/**
Status bar textfields 
*/
private JTextField 
	__messageJTextField,
	__statusJTextField;

/**
worksheet in which the list of precipitation stations will be displayed.
*/
private JWorksheet __worksheetP;

/**
Worksheet in which the list of evaporation stations will be displayed.
*/
private JWorksheet __worksheetE;

/**
The reservoir for which to display station data.
*/
private StateMod_Reservoir __currentRes;

StateMod_ReservoirClimate_TableModel __tableModelP;
StateMod_ReservoirClimate_TableModel __tableModelE; 

StateMod_DataSet __dataset;

/**
Constructor.  
@param res the reservoir for which to display climate information.
@param editable whether the gui data is editable or not.
*/
public StateMod_Reservoir_Climate_JFrame(StateMod_DataSet dataset,
StateMod_Reservoir res, boolean editable) {
	StateMod_GUIUtil.setTitle(this, dataset, res.getName() 
		+ " - Reservoir Climate Stations", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	__dataset = dataset;
	
	__currentRes = res;

	__editable = editable;

	setupGUI();
}

private List<StateMod_ReservoirClimate> getPrecipitationStations(List<StateMod_ReservoirClimate> stations) {
	List<StateMod_ReservoirClimate> v = new Vector<StateMod_ReservoirClimate>();
	StateMod_ReservoirClimate s = null;
	for (int i = 0; i < stations.size(); i++) {
		s = stations.get(i);
		if (s.getType() == StateMod_ReservoirClimate.CLIMATE_PTPX) {
			v.add(s);
		}
	}
	return v;
}

private List<StateMod_ReservoirClimate> getEvaporationStations(List<StateMod_ReservoirClimate> stations) {
	List<StateMod_ReservoirClimate> v = new Vector<StateMod_ReservoirClimate>();
	StateMod_ReservoirClimate s = null;
	for (int i = 0; i < stations.size(); i++) {
		s = stations.get(i);
		if (s.getType() == StateMod_ReservoirClimate.CLIMATE_EVAP) {
			v.add(s);
		}
	}
	return v;
}

/**
Reponds to action performed events.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();
	
	if (action.equals(__BUTTON_ADD_PRECIPITATION_STATION)) {
		StateMod_ReservoirClimate aClimateNode =
			new StateMod_ReservoirClimate();
		aClimateNode._isClone = true;
		aClimateNode.setType(StateMod_ReservoirClimate.CLIMATE_PTPX);
		__worksheetP.addRow(aClimateNode);
		__worksheetP.scrollToLastRow();					
		__worksheetP.selectLastRow();
		checkDeleteStationButton();
	}
	else if (action.equals(__BUTTON_ADD_EVAPORATION_STATION)) {
		StateMod_ReservoirClimate aClimateNode =
			new StateMod_ReservoirClimate();
		aClimateNode._isClone = true;
		aClimateNode.setType(StateMod_ReservoirClimate.CLIMATE_EVAP);
		__worksheetE.addRow(aClimateNode);
		__worksheetE.scrollToLastRow();					
		__worksheetE.selectLastRow();
		checkDeleteStationButton();
	}
	else if (action.equals(__BUTTON_DELETE_PRECIPITATION_STATION)) {
		int rowP = __worksheetP.getSelectedRow();
		int rowE = __worksheetE.getSelectedRow();
		int count = 0;
		if (rowP > -1) {
			count++;
		}
		if (rowE > -1) {
			count++;
		}
		if (count > 0) {
			String plural = "s";
			if (count == 1) {
				plural = "";
			}
			int x = new ResponseJDialog(this,
				"Delete climate station" + plural,
				"Delete climate station" + plural + "?",
				ResponseJDialog.YES | ResponseJDialog.NO)
				.response();
			if (x == ResponseJDialog.NO) {
				return;
			}
			if (rowP > -1) {
				__worksheetP.deleteRow(rowP);
				__deleteStation.setEnabled(false);
				__worksheetP.scrollToLastRow();
			}
			if (rowE > -1) {
				__worksheetE.deleteRow(rowE);
				__deleteStation.setEnabled(false);
				__worksheetE.scrollToLastRow();
			}			
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
Checks the data to make sure that all the data are valid. 
@return 0 if the data are valid, 1 if errors exist and -1 if non-fatal errors
exist.
*/
private int checkInput(JWorksheet worksheet, String name) {
	String routine = "StateMod_Reservoir_Climate_JFrame.checkInput";
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirClimate> v = (List<StateMod_ReservoirClimate>)worksheet.getAllData();

	int size = v.size();
	StateMod_ReservoirClimate acct = null;
	String warning = "";
	String id;
	int fatalCount = 0;

	for (int i = 0; i < size; i++) {
		acct = (v.get(i));

		id = acct.getID();
	
		if (id.length() > 12) {
			warning += "\n" + name + " reservoir climate ID (" 
				+ id + ") is "
				+ "longer than 12 characters.";
			fatalCount++;
		}

		if (id.indexOf(" ") > -1 || id.indexOf("-") > -1) {
			warning += "\n" + name + " reservoir climate ID (" 
				+ id + ") cannot "
				+ "contain spaces or dashes.";
			fatalCount++;
		}

		// the rest are handled automatically by the worksheet
	}
	// REVISIT - if daily time series are supplied, check for time series
	// and allow creation if not available.
	if ( warning.length() > 0 ) {
		warning += "\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, warning, this );
		if ( fatalCount > 0 ) {
			// Fatal errors...
			Message.printStatus ( 1, routine,
				"Returning 1 from checkInput()" );
			return 1;
		}
		else {	// Nonfatal errors...
			Message.printStatus ( 1, routine,
				"Returning -1 from checkInput()" );
			return -1;
		}
	}
	else {	// No errors...
		Message.printStatus ( 1, routine,
			"Returning 0 from checkInput()" );
		return 0;
	}
}

/**
Saves the input back into the dataset.
@return true if the data was saved successfuly.  False if not.
*/
private boolean saveData() {
	String routine = "StateMod_Reservoir_Climate_JFrame.saveData";
	if (!__worksheetP.stopEditing()) {
		// don't save if there are errors.
		Message.printWarning(1, routine, "There are errors in the "
			+ "precipitation data "
			+ "that must be corrected before data can be saved.",
			this);
		return false;
	}
	if (!__worksheetE.stopEditing()) {
		// don't save if there are errors.
		Message.printWarning(1, routine, "There are errors in the "
			+ "evaporation data "
			+ "that must be corrected before data can be saved.",
			this);
		return false;
	}

	if (checkInput(__worksheetP, "Precipitation") > 0) {
		return false;
	}
	if (checkInput(__worksheetE, "Evaporation") > 0) {
		return false;
	}

	// if the Vectors are differently-sized, they're different
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirClimate> wv1 = (List<StateMod_ReservoirClimate>)__worksheetP.getAllData();		// w for worksheet
	List<StateMod_ReservoirClimate> rv1 = getPrecipitationStations(__currentRes.getClimates());
	@SuppressWarnings("unchecked")
	List<StateMod_ReservoirClimate> wv2 = (List<StateMod_ReservoirClimate>)__worksheetE.getAllData();		// w for worksheet
	List<StateMod_ReservoirClimate> rv2 = getEvaporationStations(__currentRes.getClimates());

	boolean needToSave1 = !(StateMod_ReservoirClimate.equals(wv1, rv1));
	boolean needToSave2 = !(StateMod_ReservoirClimate.equals(wv2, rv2));

	Message.printStatus(1, routine, "Saving Precip? .......["+ needToSave1 
		+"]");
	Message.printStatus(1, routine, "Saving Evap? .........["+ needToSave2
		+"]");

	if (!needToSave1 && !needToSave2) {
		// there's nothing different -- users may even have deleted
		// some rights and added back in identical values
		return true;
	}

	int size = wv1.size();
	List<StateMod_ReservoirClimate> clone = new Vector<StateMod_ReservoirClimate>();
	StateMod_ReservoirClimate r = null;
	StateMod_ReservoirClimate cr = null;
	for (int i = 0; i < size; i++) {
		r = wv1.get(i);
		cr = (StateMod_ReservoirClimate)r.clone();
		cr._isClone = false;
		clone.add(cr);
	}

	size = wv2.size();
	for (int i = 0; i < size; i++) {
		r = wv2.get(i);
		cr = (StateMod_ReservoirClimate)r.clone();
		cr._isClone = false;
		clone.add(cr);
	}

	__currentRes.setClimates(clone);
	__dataset.setDirty(StateMod_DataSet.COMP_DIVERSION_STATIONS, true);
	return true;
}

/**
Checks to see if the __deleteStation button should be enabled or not.
*/
private void checkDeleteStationButton() {
	int rowP = __worksheetP.getSelectedRow();
	int rowE = __worksheetE.getSelectedRow();
	if (rowP == -1 && rowE == -1) {
		__deleteStation.setEnabled(false);
	}
	else {
		__deleteStation.setEnabled(true);
	}
}

/**
Responds to key pressed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {}

/**
Responds to key released events; checks if the __deleteStation button should
be enabled.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {
	checkDeleteStationButton();
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
Responds to mouse released events; checks to see if the __deleteStation button
should be enabled or not.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	checkDeleteStationButton();
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
public void setupGUI() {
	String routine = "setupGUI";

	addWindowListener(this);
	
	__addPrecip = new JButton(__BUTTON_ADD_PRECIPITATION_STATION);
	__addEvap = new JButton(__BUTTON_ADD_EVAPORATION_STATION);
	__deleteStation = new JButton(__BUTTON_DELETE_PRECIPITATION_STATION);
	__deleteStation.setEnabled(false);
	__helpJButton = new JButton(__BUTTON_HELP);
	__helpJButton.setEnabled(false);
	__closeJButton = new JButton(__BUTTON_CLOSE);
	JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	JButton applyJButton = new JButton(__BUTTON_APPLY);			

	GridBagLayout gb = new GridBagLayout();
	JPanel bigPanel = new JPanel(); 
	bigPanel.setLayout(gb);	

	FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
	JPanel p1 = new JPanel();
	p1.setLayout(fl);

	GridLayout gl = new GridLayout(2, 2, 1, 1);
	JPanel info_panel = new JPanel();
	info_panel.setLayout(gl);

	JPanel main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout());

	info_panel.add(new JLabel("Reservoir:"));
	info_panel.add(new JLabel(__currentRes.getID()));
	info_panel.add(new JLabel("Reservoir name:"));
	info_panel.add(new JLabel(__currentRes.getName()));

	if (__editable) {
		p1.add(__addPrecip);
		p1.add(__addEvap);
		p1.add(__deleteStation);
	}
	p1.add(applyJButton);
	p1.add(cancelJButton);			
//	p1.add(__helpJButton);
	p1.add(__closeJButton);

	PropList p =
		new PropList("StateMod_Reservoir_Climate_JFrame.JWorksheet");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.AllowCopy=true");	
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	int widthsP[] = null;
	JScrollWorksheet jswP = null;
	@SuppressWarnings("unchecked")
	List<String> stations = StateMod_Util.createIdentifierListFromTS(
		combineData(
		(List<MonthTS>)__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY).getData(),
		(List<MonthTS>)__dataset.getComponentForComponentType(
		StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY).getData()), true, null);

	try {			
		List<StateMod_ReservoirClimate> temp = getPrecipitationStations(__currentRes.getClimates());
		List<StateMod_ReservoirClimate> clones = new Vector<StateMod_ReservoirClimate>();
		StateMod_ReservoirClimate r = null;
		int size = temp.size();
		for (int i = 0; i < size; i++) {
			r = temp.get(i);
			clones.add((StateMod_ReservoirClimate)r.clone());
		}
		
		__tableModelP = new StateMod_ReservoirClimate_TableModel(clones,
			__editable, true);
		StateMod_ReservoirClimate_CellRenderer crr = new
			StateMod_ReservoirClimate_CellRenderer(__tableModelP);
		
		jswP = new JScrollWorksheet(crr, __tableModelP, p);
		__worksheetP = jswP.getJWorksheet();

		__worksheetP.setColumnJComboBoxValues(
			StateMod_ReservoirClimate_TableModel.COL_STATION,
			stations, true);

		widthsP = crr.getColumnWidths();		
	}
	catch (Exception e) {
		Message.printWarning(1, routine,
			"Error building worksheet.", this);
		Message.printWarning(2, routine, e);
		jswP = new JScrollWorksheet(0, 0, p);
		__worksheetP = jswP.getJWorksheet();
	}
	__worksheetP.setPreferredScrollableViewportSize(null);

	__worksheetP.setHourglassJFrame(this);
	__worksheetP.addMouseListener(this);	
	__worksheetP.addKeyListener(this);

	int widthsE[] = null;
	JScrollWorksheet jswE = null;
	try {	
		List<StateMod_ReservoirClimate> temp = getEvaporationStations(__currentRes.getClimates());
		List<StateMod_ReservoirClimate> clones = new Vector<StateMod_ReservoirClimate>();
		StateMod_ReservoirClimate r = null;
		int size = temp.size();
		for (int i = 0; i < size; i++) {
			r = temp.get(i);
			clones.add((StateMod_ReservoirClimate)r.clone());
		}
	
		__tableModelE = new StateMod_ReservoirClimate_TableModel(clones,
			__editable, true);
		StateMod_ReservoirClimate_CellRenderer crr = new
			StateMod_ReservoirClimate_CellRenderer(__tableModelE);
		
		jswE = new JScrollWorksheet(crr, __tableModelE, p);
		__worksheetE = jswE.getJWorksheet();

		__worksheetE.setColumnJComboBoxValues(
			StateMod_ReservoirClimate_TableModel.COL_STATION,
			stations, true);
		widthsE = crr.getColumnWidths();		
	}
	catch (Exception e) {
		Message.printWarning(1, routine,
			"Error building worksheet.", this );
		Message.printWarning(2, routine, e);
		jswE = new JScrollWorksheet(0, 0, p);
		__worksheetE = jswE.getJWorksheet();
	}
	__worksheetE.setPreferredScrollableViewportSize(null);

	__worksheetE.setHourglassJFrame(this);
	__worksheetE.addMouseListener(this);	
	__worksheetE.addKeyListener(this);

	JPanel worksheets = new JPanel();
	worksheets.setLayout(gb);
	
	JPanel panelP = new JPanel();
	panelP.setLayout(gb);
	panelP.setBorder(BorderFactory.createTitledBorder(
		"Precipitation Stations"));
	JGUIUtil.addComponent(panelP, jswP,
		0, 0, 1, 1, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	
	JPanel panelE = new JPanel();
	panelE.setLayout(gb);
	panelE.setBorder(BorderFactory.createTitledBorder(
		"Evaporation Stations"));
	JGUIUtil.addComponent(panelE, jswE,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	
	JGUIUtil.addComponent(worksheets, panelP,
		0, 0, 1, 1, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(worksheets, panelE,
		0, 1, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	main_panel.add(worksheets, "Center");
	main_panel.add(p1, "South");

	// assemble parts
	JGUIUtil.addComponent(bigPanel, info_panel, 0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(bigPanel, main_panel, 0, 1, 10, 10, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.SOUTH);
	__addEvap.addActionListener(this);
	__addPrecip.addActionListener(this);
	__deleteStation.addActionListener(this);
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
	setSize(650, 400);
	JGUIUtil.center(this);
	setVisible(true);

	if (widthsP != null) {
		__worksheetP.setColumnWidths(widthsP);
	}	
	if (widthsE != null) {
		__worksheetE.setColumnWidths(widthsE);
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

/**
Combines the values in two lists into a single lists.
@param data1 the first non-null list from which to add values.
@param data2 the second non-null list from which to add values.
@return a list containing all the values of data1 and data2
*/
public static List<TS> combineData(List<? extends TS> data1, List<? extends TS> data2) {
	List<TS> v = new Vector<TS>();
	for (int i = 0; i < data1.size(); i++) {
		v.add(data1.get(i));
	}
	for (int i = 0; i < data2.size(); i++) {
		v.add(data2.get(i));
	}
	return v;
}

}
