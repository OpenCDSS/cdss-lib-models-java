// StateMod_QueryTool_JFrame - window to query StateMod data set

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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.TableModel_JFrame;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class displays a query tool interface for selecting information from a
StateMod data set.
*/
@SuppressWarnings("serial")
public class StateMod_QueryTool_JFrame extends JFrame
implements ActionListener, ItemListener, WindowListener {

private final String 
	__Cancel_String = "Cancel",
	__Display_String = "Display",
	__Query_String = "Query";

private final String 
	__Ready = "Ready",
	__Wait = "Wait";

private SimpleJButton 
	__query_JButton = null,
	__display_JButton = null,
	__cancel_JButton = null;

private SimpleJComboBox __component_JComboBox = null;

private JTextField __message_JTextField = null, __status_JTextField = null;

/**
The dataset that contains all the information about the statemod data.
*/
private StateMod_DataSet __dataset;

/**
Data set window manager.
*/
private StateMod_DataSet_WindowManager __dataset_wm;

private InputFilter_JPanel __input_filter_diversion_JPanel = null;

private List<StateMod_Diversion> __matches_Vector = null;	// The list of matching data.

/** 
Constructor.
@param dataset the dataset in which the Control data with which to populate
this form is found.
@param dataset_wm the dataset window manager or null if the data set windows
are not being managed.
@param editable If true, the data in the window are editable.
*/
public StateMod_QueryTool_JFrame (	StateMod_DataSet dataset,
					StateMod_DataSet_WindowManager
					dataset_wm )
{
	StateMod_GUIUtil.setTitle(this, dataset, "Query Tool", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	

	__dataset = dataset;
	__dataset_wm = dataset_wm;
	
	setupGUI();
}

/**
Handle action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event)
{	String routine = "StateMod_QueryTool_JFrame.actionPerformed";

	String action = event.getActionCommand();

	if (action.equals(__Cancel_String )) {
		closeWindow();
	}
	else if (action.equals(__Display_String )) {
		try {	displayQueryResults ();
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Unable to display query results.", this );
			Message.printWarning ( 2, routine, e );
		}
	}
	else if (action.equals(__Query_String )) {
		try {	doQuery ();
		}
		catch ( Exception e ) {
			__status_JTextField.setText ( __Ready );
			JGUIUtil.setWaitCursor ( this, false );
		}
	}
}

/**
Closes the window.
*/
protected void closeWindow()
{	if ( __dataset_wm != null ) {
		__dataset_wm.closeWindow (
		StateMod_DataSet_WindowManager.WINDOW_QUERY_TOOL );
	}
	else {	JGUIUtil.close ( this );
	}
}

/**
Display the results of the query.
*/
private void displayQueryResults()
{	String routine = "StateMod_QueryTool_JFrame.displayQueryResults";
	// For now only diversion stations are supported...
	InputFilter_JPanel ifp = __input_filter_diversion_JPanel;
	if ( ifp instanceof StateMod_Diversion_InputFilter_JPanel ) {
		try {	StateMod_Diversion_TableModel tm = new
				StateMod_Diversion_TableModel (
				__matches_Vector, false );
			StateMod_Diversion_CellRenderer cr = new
				StateMod_Diversion_CellRenderer ( tm );
			TableModel_JFrame f =
			new TableModel_JFrame ( tm, cr,
			(PropList)null, (PropList)null );
			StateMod_GUIUtil.setTitle ( f, __dataset, 
			"Diversion Station Query Results", null );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error displaying results.", this );
			Message.printWarning ( 2, routine, e );
		}
	}
}

/**
Perform the query.  Currently only diversion stations are supported.
*/
private void doQuery ()
{	String routine = "StateMod_QueryTool_JFrame.doQuery", message;
	InputFilter_JPanel ifp = __input_filter_diversion_JPanel;
	InputFilter filter = null;
	int size;			// Size of data Vector
	int i;				// Loop for data items.
	DataSetComponent comp = null;
	String where, operator, input;
	int where_length = 0;		// Length of "where", to optimize code.
	boolean do_int,			// Indicate the filter data type
		do_double,
		do_string;
	String input_string;		// Data parameters to check, as
	int input_int;			// transferred from the data objects.
	double input_double;
	boolean	do_ID,			// Indicate the data item to check
		do_Name,		// for a filter.
		do_RiverNodeID,
		do_OnOff,
		do_Capacity,
		do_ReplaceResOption,
		do_DailyID,
		do_UserName,
		do_DemandType,
		do_EffAnnual,
		do_Area,
		do_UseType,
		do_DemandSource;
	boolean [] matches = null;	// Indicates if a data item matches all
					// the filter criteria.
	boolean item_matches;		// Indicates whether the itme matches a
					// single filter criteria.
	boolean [] checked = null;	// Indicates whether a data item has
					// already been checked for a criteria.
					// If checked and false, then a "true"
					// should not reset the false.
	int nfg = 0;			// Number of filter groups.
	do_int = false;			// Whether the data item is an integer.
	do_double = false;		// Whether the data item is a double.
	do_string = false;		// Whether the data item is a string.
	StateMod_Diversion dds = null;
	__status_JTextField.setText ( __Wait );
	JGUIUtil.setWaitCursor ( this, true );
	if ( ifp instanceof StateMod_Diversion_InputFilter_JPanel ) {
		input_int = StateMod_Util.MISSING_INT;
		input_double = StateMod_Util.MISSING_DOUBLE;
		input_string = StateMod_Util.MISSING_STRING;
		comp = __dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_DIVERSION_STATIONS);
		@SuppressWarnings("unchecked")
		List<StateMod_Diversion> dds_Vector = (List<StateMod_Diversion>)comp.getData ();
		size = 0;
		if ( dds_Vector != null ) {
			size = dds_Vector.size();
		}
		// REVISIT SAM 2004-10-27 Remove when debugging is complete
		//Message.printStatus ( 2, routine,
		//"Searching " + size + " diversion stations." );
		// Initialize the arrays indicating if data objects have been
		// checked and whether they matched the filter(s)...
		if ( size > 0 ) {
			matches = new boolean[size];
			checked = new boolean[size];
		}
		for ( i = 0; i < size; i++ ) {
			matches[i] = false;
			checked[i] = false;
		}
		// Loop through the where clauses...
		nfg = ifp.getNumFilterGroups();
		for ( int ifg = 0; ifg < nfg; ifg++ ) {
			// Get the filter information...
			filter = ifp.getInputFilter ( ifg );
			where = filter.getWhereInternal();
			where_length = where.length();
			operator = ifp.getOperator ( ifg );
			input = filter.getInput ( false );
			// REVISIT SAM 2004-10-27 Remove when debugging is
			// complete
			//Message.printStatus ( 2, routine,
			//"where=" + where + " operator=" + operator +
			//" input=" + input );
			// Initialize flags to indicate what data will be
			// checked...
			do_int = false;
			do_double = false;
			do_string = false;
			do_ID = false;
			do_Name = false;
			do_RiverNodeID = false;
			do_OnOff = false;
			do_Capacity = false;
			do_ReplaceResOption = false;
			do_DailyID = false;
			do_UserName = false;
			do_DemandType = false;
			do_EffAnnual = false;
			do_Area = false;
			do_UseType = false;
			do_DemandSource = false;
			// The following checks on "where" need to match the
			// input filter internal where labels assigned in
			// StateMod_Diversion_InputFilter_JPanel.
			// List in the order of the StateMod documentation...
			if ( where.equalsIgnoreCase("ID") ) {
				do_string = true;
				do_ID = true;
			}
			else if ( where.equalsIgnoreCase("Name") ) {
				do_string = true;
				do_Name = true;
			}
			else if ( where.equalsIgnoreCase("RiverNodeID") ) {
				do_string = true;
				do_RiverNodeID = true;
			}
			else if(where.equalsIgnoreCase("OnOff") &&
				StringUtil.isInteger(input) ) {
				do_int = true;
				do_OnOff = true;
			}
			else if(where.equalsIgnoreCase("Capacity") &&
				StringUtil.isDouble(input) ) {
				do_double = true;
				do_Capacity = true;
			}
			else if(where.equalsIgnoreCase("ReplaceResOption") &&
				StringUtil.isInteger(input) ) {
				do_int = true;
				do_ReplaceResOption = true;
			}
			else if ( where.equalsIgnoreCase("DailyID") ) {
				do_string = true;
				do_DailyID = true;
			}
			else if ( where.equalsIgnoreCase("UserName") ) {
				do_string = true;
				do_UserName = true;
			}
			else if ( where.equalsIgnoreCase("DemandType") ) {
				do_int = true;
				do_DemandType = true;
			}
			else if(where.equalsIgnoreCase("EffAnnual") &&
				StringUtil.isDouble(input) ) {
				do_double = true;
				do_EffAnnual = true;
			}
			else if(where.equalsIgnoreCase("Area") &&
				StringUtil.isDouble(input) ) {
				do_double = true;
				do_Area = true;
			}
			else if(where.equalsIgnoreCase("UseType") &&
				StringUtil.isInteger(input) ) {
				do_int = true;
				do_UseType = true;
			}
			else if(where.equalsIgnoreCase("DemandSource") &&
				StringUtil.isInteger(input) ) {
				do_int = true;
				do_DemandSource = true;
			}
			else if ( where_length == 0 ) {
				// WIll match anything.
			}
			else {	// Unrecognized where...
				continue;
			}
			// REVISIT SAM 2004-10-27 Remove when debugging is
			// complete
			/*
			Message.printStatus ( 2, routine,
			"do_string=" + do_string + " do_int=" + do_int +
			"do_double=" + do_double + 
			" " + do_ID +
			" " + do_Name +
			" " + do_RiverNodeID +
			" " + do_OnOff +
			" " + do_Capacity +
			" " + do_ReplaceResOption +
			" " + do_DailyID +
			" " + do_UserName +
			" " + do_DemandType +
			" " + do_EffAnnual +
			" " + do_Area +
			" " + do_UseType +
			" " + do_DemandSource );
			*/
			for ( i = 0; i < size; i++ ) {
				dds=(StateMod_Diversion)dds_Vector.get(i);
				// Get the specific data to compare...
				if ( do_ID ) {
					input_string = dds.getID();
				}
				else if ( do_Name ) {
					input_string = dds.getName();
				}
				else if ( do_RiverNodeID ) {
					input_string = dds.getCgoto();
				}
				else if ( do_OnOff ) {
					input_int = dds.getSwitch();
				}
				else if ( do_Capacity ) {
					input_double = dds.getDivcap();
				}
				else if ( do_ReplaceResOption ) {
					input_int = dds.getIreptype();
				}
				else if ( do_DailyID ) {
					input_string = dds.getCdividy();
				}
				else if ( do_UserName ) {
					input_string = dds.getUsername();
				}
				else if ( do_DemandType ) {
					input_int = dds.getIdvcom();
				}
				else if ( do_EffAnnual ) {
					input_double = dds.getDivefc();
				}
				else if ( do_Area ) {
					input_double = dds.getArea();
				}
				else if ( do_UseType ) {
					input_int = dds.getIrturn();
				}
				else if ( do_DemandSource ) {
					input_int = dds.getDemsrc();
				}
				else {	// Unrecognized...
					continue;
				}
				// Compare the data with the input filter...
				item_matches = false;
				if ( do_string ) {
					item_matches = filter.matches(
						input_string, operator, true );
				}
				else if ( do_int ) {
					item_matches = filter.matches(
						input_int, operator );
				}
				else if ( do_double ) {
					item_matches = filter.matches(
						input_double, operator );
				}
				if ( where_length == 0 ) {
					// Always consider a match...
					item_matches = true;
				}
				if (	item_matches && (!checked[i] ||
					(checked[i] && matches[i])) ) {
					// So far the item matches all
					// filters...
					matches[i] = true;
				}
				else if ( checked[i] && !item_matches ) {
					// Does not match this filter to reset
					// result to false...
					matches[i] = false;
				}
				// Indicate that we have checked the item
				// against at least one filter...
				checked[i] = true;
			}
		}
		// Get a count so the Vector can be sized appropriately (this
		// should be fast)...
		int match_count = 0;
		for ( i = 0; i < size; i++ ) {
			if ( matches[i] ) {
				++match_count;
			}
		}
		// Loop through and set up the matches_Vector...
		__matches_Vector = null;
		if ( match_count > 0 ) {
			__matches_Vector = new Vector<StateMod_Diversion> ( match_count );
			for ( i = 0; i < size; i++ ) {
				if ( matches[i] ) {
					// The diversion station matches so add
					// to the list...
					__matches_Vector.add(dds_Vector.get(i) );
				}
			}
		}
		message = "Matched " + match_count +
		" diversion stations (from original " + size + ").";
		Message.printStatus ( 2, routine, message );
		__message_JTextField.setText ( message );
		__status_JTextField.setText ( __Ready );
		JGUIUtil.setWaitCursor ( this, false );
	}
	if ( __matches_Vector == null ) {
		__display_JButton.setEnabled ( false );
	}
	else {	__display_JButton.setEnabled ( true );
	}
}

/**
Responds to item state changed events.
@param event the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent event) {
	Object source = event.getSource();
	if (	(source == __component_JComboBox) && 
	    	(event.getStateChange() == ItemEvent.SELECTED)) {
		// REVISIT SAM 2004-10-25 Need to pick the proper input filter
		// panel depending on the component
	}
}

/**
Sets up the GUI.
*/
private void setupGUI()
{	String routine = "StateMod_QueryTool_JFrame.setupGUI";

	addWindowListener(this);
	
	GridBagLayout gb = new GridBagLayout();

	JPanel  main_JPanel = new JPanel();
	main_JPanel.setLayout(gb);
	JGUIUtil.addComponent(main_JPanel, new JLabel("Component:"),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__component_JComboBox = new SimpleJComboBox ( false );
	__component_JComboBox.add ( "Diversion Stations" );
	JGUIUtil.addComponent(main_JPanel, __component_JComboBox,
		1, 0, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	try {	__input_filter_diversion_JPanel = new
			StateMod_Diversion_InputFilter_JPanel ( __dataset );
		JGUIUtil.addComponent(main_JPanel,
			__input_filter_diversion_JPanel,
			0, 1, 4, 4, 0, 0,
			GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, routine,
		"Unable to initialize input filter for diversion stations." );
	}
	getContentPane().add( main_JPanel );

	// add bottom buttons
	FlowLayout fl = new FlowLayout(FlowLayout.CENTER);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(fl);
	__query_JButton = new SimpleJButton(__Query_String, this);
	__query_JButton.setToolTipText(
		"Query the data set using specified criteria.");
	__display_JButton = new SimpleJButton(__Display_String, this);
	__display_JButton.setToolTipText( "Display the results of the query.");
	__display_JButton.setEnabled ( false );	// Enable after query.
	__cancel_JButton = new SimpleJButton(__Cancel_String, this);
	__cancel_JButton.setToolTipText("Cancel query and close window.");

	buttonPanel.add(__query_JButton);
	buttonPanel.add(__display_JButton);
	buttonPanel.add(__cancel_JButton);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gb);
	JGUIUtil.addComponent(bottomJPanel, buttonPanel,
		0, 0, 8, 1, 1, 0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	__message_JTextField = new JTextField();
	__message_JTextField.setText (
		"Select filter criteria and then press Query" );
	__message_JTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __message_JTextField,
		0, 1, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__status_JTextField = new JTextField(5);
	__status_JTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __status_JTextField,
		7, 1, 1, 1, 0.0, 0.0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add ("South", bottomJPanel);	

	if ( __dataset_wm != null ) {
		__dataset_wm.setWindowOpen (
		StateMod_DataSet_WindowManager.WINDOW_CONTROL, this );
	}

	pack();
	setSize ( 550, 190 );	// Allows replacement res option to display
	JGUIUtil.center(this);
	setResizable(true);
	setVisible(true);
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
Responds to the window closing, calls closeWindow().
@param event the WindowEvent that happened.
*/
public void windowClosing(WindowEvent event) {
	closeWindow();
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
Does nothing.
*/
public void windowOpening(WindowEvent event) {}

}
