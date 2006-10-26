// ----------------------------------------------------------------------------
// readStateModB_JDialog - editor for readStateModBB()
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-03-16	Steven A. Malers, RTi	Initial version (copy and modify
//					readStateMod_JDialog).
// 2004-07-20	SAM, RTi		* Make sure that an input file is
//					  specified.
//					* The filter was listing "stm" but not
//					  StateMod binary files.
//					* Change the note to indicate that
//					  all fields can be wildcarded.
// 2005-09-29	SAM, RTi		Move from TSTool to StateMod package.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.io.File;
import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.Command;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class readStateModB_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton	__browse_JButton = null,// File browse button
			__cancel_JButton = null,// Cancel Button
			__ok_JButton = null,	// Ok Button
			__path_JButton = null;	// Convert between relative and
						// absolute paths.
private JFrame		__parent_JFrame = null;	// parent Frame GUI class
private readStateModB_Command __command = null; // Command to edit
private JTextArea	__command_JTextArea=null;// Command as TextField
private String		__working_dir = null;	// Working directory.
private JTextField	__InputFile_JTextField = null;// Field for input file. 
private JTextField	__InputStart_JTextField = null;
						// Start of period for input
private JTextField	__InputEnd_JTextField = null;
						// End of period for input
private JTextField	__TSID_JTextField = null;// Field for time series
						// identifier
private JTextField	__Version_JTextField = null;
						// Field for StateMod version
private boolean		__error_wait = false;	// Is there an error that we
						// are waiting to be cleared up
						// or Cancel?
private boolean		__first_time = true;
private boolean		__use_alias = false;	// If true, then the syntax is
						// TS Alias = readStateMod().
						// If false, it is:
						// readStateMod().
private boolean		__ok = false;		// Indicates whether OK was
						// pressed when closing the
						// dialog.

/**
readStateModB_JDialog constructor.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
public readStateModB_JDialog ( JFrame parent, Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __browse_JButton ) {
		String last_directory_selected =
			JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(
				last_directory_selected );
		}
		else {	fc = JFileChooserFactory.createJFileChooser(
				__working_dir );
		}
		fc.setDialogTitle( "Select StateMod Time Series File");
		// REVISIT - maybe need to list all recognized StateMod file
		// extensions for data sets.
		SimpleFileFilter sff = new SimpleFileFilter("b43",
			"Diversion, Stream, Instream stations (Monthly)");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("b49",
			"Diversion, Stream, Instream stations (Daily)") );
		fc.addChoosableFileFilter( new SimpleFileFilter("b44",
			"Reservoir stations (Monthly)") );
		fc.addChoosableFileFilter( new SimpleFileFilter("b50",
			"Reservoir stations (Daily)") );
		fc.addChoosableFileFilter( new SimpleFileFilter("b42",
			"Well stations (Monthly)") );
		fc.addChoosableFileFilter( new SimpleFileFilter("b65",
			"Well stations (Daily)") );
		fc.setFileFilter(sff);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				__InputFile_JTextField.setText(path );
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if (	__path_JButton.getText().equals(
			"Add Working Directory") ) {
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__InputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(
			"Remove Working Directory") ) {
			try {	__InputFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir,
				__InputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,
				"readStateModB_JDialog",
				"Error converting file to relative path." );
			}
		}
		refresh ();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String InputFile = __InputFile_JTextField.getText().trim();
	String TSID = __TSID_JTextField.getText().trim();
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	String Version = __Version_JTextField.getText().trim();
	__error_wait = false;
	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
	}
	if ( TSID.length() > 0 ) {
		props.set ( "TSID", TSID );
	}
	if ( InputStart.length() > 0 ) {
		props.set ( "InputStart", InputStart );
	}
	if ( InputEnd.length() > 0 ) {
		props.set ( "InputEnd", InputEnd );
	}
	if ( Version.length() > 0 ) {
		props.set ( "Version", Version );
	}
	try {	// This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String InputFile = __InputFile_JTextField.getText().trim();
	String TSID = __TSID_JTextField.getText().trim();
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	String Version = __Version_JTextField.getText().trim();
	__command.setCommandParameter ( "InputFile", InputFile );
	__command.setCommandParameter ( "TSID", TSID );
	__command.setCommandParameter ( "InputStart", InputStart );
	__command.setCommandParameter ( "InputEnd", InputEnd );
	__command.setCommandParameter ( "Version", Version );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__browse_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__InputFile_JTextField = null;
	__TSID_JTextField = null;
	__InputStart_JTextField = null;
	__InputEnd_JTextField = null;
	__Version_JTextField = null;
	__command = null;
	__ok_JButton = null;
	__path_JButton = null;
	__parent_JFrame = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__parent_JFrame = parent;
	__command = (readStateModB_Command)command;
	__use_alias = false;	// Currently no support for alias version.
	Object o =__command.getCommandProcessor().getPropContents("WorkingDir");
	if ( o != null ) {
		__working_dir = (String)o;
	}

	addWindowListener( this );

        Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	GridBagConstraints gbc = new GridBagConstraints();
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Read all the time series from a StateMod binary output file, "+
		"using information in the file to assign the identifier."),
		0, y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify a full or relative path (relative to working " +
		"directory)." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
	if ( __working_dir != null ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The working directory is: " + __working_dir ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
	}
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The time series identifier pattern, if specified, will" +
		" filter the read." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  Use blank or * to read all time series." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  Use A* to read all time series with alias or location" +
		" starting with A." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  Use *.*.XXXXX.*.* to read all time series with data type " +
		"XXXXX." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  Currently, data source, interval, and scenario are " +
		"internally defaulted to *." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"StateMod binary file to read:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__InputFile_JTextField = new JTextField ( 50 );
	__InputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.WEST);
	__browse_JButton = new SimpleJButton ( "Browse", this );
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.CENTER);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Time series ID:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__TSID_JTextField = new JTextField ( 10 );
	__TSID_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __TSID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Specify a TSID pattern to match."), 
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Input start:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__InputStart_JTextField = new JTextField (20);
	__InputStart_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
		1, y, 2, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Overrides the global input start."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input end:"), 
		0, ++y, 2, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__InputEnd_JTextField = new JTextField (20);
	__InputEnd_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
		1, y, 6, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Overrides the global input end."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "StateMod version:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__Version_JTextField = new JTextField ( 10 );
	__Version_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __Version_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Default (blank) is current version.  Example:  9.53"), 
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__command_JTextArea = new JTextArea ( 4, 55 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.WEST);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.CENTER);

	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative
		// path...
		__path_JButton = new SimpleJButton(
					"Remove Working Directory",this);
		button_JPanel.add ( __path_JButton );
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add ( __ok_JButton );

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// Dialogs do not need to be resizable...
	setResizable ( true );
        pack();
        JGUIUtil.center( this );
	refresh();	// Sets the __path_JButton status
        super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == event.VK_ENTER ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			response ( false );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event )
{	refresh();
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = "readStateModB_JDialog.refresh";
	String InputFile="";
	String TSID="";
	String InputStart = "";
	String InputEnd = "";
	String Version="";
	PropList props = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		props = __command.getCommandParameters();
		InputFile = props.getValue ( "InputFile" );
		TSID = props.getValue ( "TSID" );
		InputStart = props.getValue ( "InputStart" );
		InputEnd = props.getValue ( "InputEnd" );
		Version = props.getValue ( "Version" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText (InputFile);
		}
		if ( TSID != null ) {
			__TSID_JTextField.setText (TSID);
		}
		if ( InputStart != null ) {
			__InputStart_JTextField.setText ( InputStart );
		}
		if ( InputEnd != null ) {
			__InputEnd_JTextField.setText ( InputEnd );
		}
		if ( Version != null ) {
			__Version_JTextField.setText (Version);
		}
	}
	// Regardless, reset the command from the fields...
	InputFile = __InputFile_JTextField.getText().trim();
	TSID = __TSID_JTextField.getText().trim();
	InputStart = __InputStart_JTextField.getText().trim();
	InputEnd = __InputEnd_JTextField.getText().trim();
	Version = __Version_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "InputFile=" + InputFile );
	props.add ( "TSID=" + TSID );
	props.add ( "InputStart=" + InputStart );
	props.add ( "InputEnd=" + InputEnd );
	props.add ( "Version=" + Version );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should
	// be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( InputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText ( "Remove Working Directory" );
		}
		else {	__path_JButton.setText ( "Add Working Directory" );
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	__ok = ok;	// Save to be returned by ok()
	if ( ok ) {
		// Commit the changes...
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out!
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

} // end readStateModB_JDialog
