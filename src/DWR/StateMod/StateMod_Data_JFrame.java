// PRINTING CHECKED
// ----------------------------------------------------------------------------
// StateMod_Data_JFrame - An abstract class from which all the JFrames that
//	display tabular StateMod data are built.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 
// 2005-01-13	J. Thomas Sapienza, RTi	Initial version.
// 2005-01-20	JTS, RTi		Following review:
//					* Updated comments.
//					* Made JScrollWorksheet private.
//					* Removed getDataType().
//					* GUI title is now passed in to this
//					  class.
//					* The JFrame now sets its icon.
// 2005-03-28	JTS, RTi		Overrode setSize() and setVisible().
// 2005-03-29	JTS, RTi		* formatOutput() puts quotes around the
//					  column names now.
//					* formatOutput() puts quotes around 
//					  String field values.
//					* The worksheet is now set up to show
//					  the row number header.
// 2005-03-30	JTS, RTi		Added the optional top panel.
// 2004-04-06	JTS, RTi		* Added variables that specify the 
//					  number of lines to be printed in 
//					  landscape and portrait modes.
//					* Added print() method.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateMod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_DefaultTableCellRenderer;
import RTi.Util.GUI.ReportPrinter;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.IO.SecurityCheck;
import RTi.Util.Message.Message;

/**
This class is a JFrame that displays StateMod data in a worksheet.  The 
worksheet data can be exported to a file, printed, or edited.  This class 
cannot be instantiated because it is abstract, but it provides a framework 
for the derived classes.  <p>

<b>Derived Class Requirements</b><p>

The derived classes have to implement the following methods:<p>
<ul>
<li>apply() -- called when changes to the data are committed.</li>
<li>buildJScrollWorksheet() -- called by the code that sets up the GUI to
    create the worksheet that will be displayed in the GUI.</li>
<li>cancel() -- called when changes to the data are discarded.</li>
</ul>

<p>

<b>Data Available for Use by Derived Classes</b><p>

There are several protected data members in this class that derived classes are 
expected to use.  These are:<p>

<ul>
<li>_editable -- contains whether the data in the worksheet can be edited or 
    not.  If false, the data cannot be edited and the Apply and Cancel buttons
    will not appear.  If true, the data can be edited and the Apply and Cancel
    buttons will appear on the GUI.</li>
<li>_worksheet -- contains the worksheet created by the derived class.  This is
    assigned automatically in this class's setupGUI() method, but is protected
    so that derived classes have access to it.</li>
<li>_props -- contains default properties that can be used to ensure all the
    derived classes' worksheets have uniform properties.</li>
<li>_titleString -- contains the title of the GUI as passed in to the
    constructor of the super class.  It is protected so that it can be 
    accessed, for instance, to change the GUI title if data have been edited.
    </li>
<li>_data -- contains the data stored in the worksheet.  This is assigned in 
    this class's constructor but is protected so that the data are available
    in the derived classes.</li/>
</ul>

<b>Issues</b><p>
REVISIT (2005-01-20)<p>
Currently there is no window management being done for this class and its 
derived classes.  This will be added in the future but for now multiple windows
will be able to be opened, etc.
*/
public abstract class StateMod_Data_JFrame 
extends JFrame
implements ActionListener, WindowListener {

/**
Button labels.
*/
protected final String
	_BUTTON_APPLY = 	"Apply",
	_BUTTON_CANCEL =	"Cancel",
	_BUTTON_EXPORT = 	"Export",
	_BUTTON_OK = 		"OK",
	_BUTTON_PRINT =		"Print";

/**
Whether the data in the worksheet can be edited (true) or not (false).
*/
protected boolean _editable = false;

/**
The number of lines to be printed in landscape and portrait modes.  Public
so that all derived classes can access them.
*/
public int 
	PRINT_LANDSCAPE_LINES = 44,
	PRINT_PORTRAIT_LINES = 66;

/**
The JScrollWorksheet that is created and placed in the GUI.  It is created
in the derived classes in the method buildJScrollWorksheet().
*/
private JScrollWorksheet __scrollWorksheet = null;

/**
The worksheet that is created in the derived classes.
*/
protected JWorksheet _worksheet = null;

/**
Generic properties that can be used in any worksheet in the derived classes. 
These are available so that all the derived classes' worksheets can be uniform
and have the same properties.
*/
protected PropList _props = null;

/**
The title of the GUI.
*/
protected String _titleString = null;

/**
The data to display in the worksheet, assigned in the derived classes. Assigned
by this class's constructor when the derived classes call super().  Must be 
passed to this class via the constructor in order for the Apply|Cancel|OK 
buttons to function properly.
*/
protected Vector _data = null;

/**
Constructor.  This constructor should only be called if initialize() will be
called later.
*/
public StateMod_Data_JFrame()
throws Exception {
	super();
}

/**
Constructor. 
@param data the data to display in the worksheet.  Can be null or empty, in
which case an empty worksheet is shown.
@param titleString the String to display as the GUI's title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public StateMod_Data_JFrame(Vector data, String titleString, boolean editable) 
throws Exception {
	super();

	initialize(data, titleString, editable);
}

/**
Responds to action events. 
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String routine = "StateMod_Data_JFrame.actionPerformed";

	String action = event.getActionCommand();

	if (action.equals(_BUTTON_APPLY)) {
		_worksheet.stopEditing();
		apply();
	}
	else if (action.equals(_BUTTON_CANCEL)) {
		_worksheet.cancelEditing();
		cancel();
		closeJFrame();
	}
	else if (action.equals(_BUTTON_EXPORT)) {
		String[] s = getFilenameAndFormat();

		if (s == null) {
			// user cancelled
			return;
		}

		Vector v = formatOutput(s[1], true, true);
		try {
			export(s[0], v);
		}
		catch (Exception e) {
			Message.printWarning(1, routine, 
				"Error exporting data.");
			Message.printWarning(2, routine, e);
		}
	}
	else if (action.equals(_BUTTON_OK)) {
		_worksheet.stopEditing();
		apply();
		closeJFrame();
	}
	else if (action.equals(_BUTTON_PRINT)) {
		print();
	}
}

/**
Called when the Apply or OK button is pressed.  This method must commit all 
changes that were made to the data objects.<p>
Derived classes should implement the method similar to:<pre>
protected void apply() {
	StateMod_Reservoir res = null;
	for (int i = 0; i < _data.size(); i++) {
		res = (StateMod_Reservoir)_data.elementAt(i);
		res.createBackup();
	}
}
</pre>
*/
protected abstract void apply();

/**
Called by setupGUI, this builds the table model and cell renderer 
necessary for a worksheet, then builds the JScrollWorksheet and returns it.
@return the JScrollWorksheet that was made and which should be displayed in
the GUI.
*/
protected abstract JScrollWorksheet buildJScrollWorksheet()
throws Exception;

/**
Called when the cancel button is pressed.  Throws away all changes that
were made to data objects and restores them to their original state.<p>
Derived classes should implement the method similar to:<pre>
protected void cancel() {
	StateMod_Reservoir res = null;
	for (int i = 0; i < _data.size(); i++) {
		res = (StateMod_Reservoir)_data.elementAt(i);
		res.restoreOriginal();
	}
}
</pre>
*/
protected abstract void cancel();

/**
Closes the JFrame, setting it not visible and disposing of it.
*/
protected void closeJFrame() {
	setVisible(false);
	dispose();
}

/**
Creates backup copies of all the data to be displayed in the worksheet, so that
changes can later be cancelled.  Called from this superclass's constructor.<p>
Derived classes should implement the method similar to:<pre>
protected void createDataBackup() {
	StateMod_Reservoir res = null;
	for (int i = 0; i < _data.size(); i++) {
		res = (StateMod_Reservoir)_data.elementAt(i);
		res.createBackup();
	}
}
</pre> 
*/
protected abstract void createDataBackup();

/**
Exports a Vector of strings to a file.
@param filename the name of the file to write.
@param strings a non-null Vector of Strings, each element of which will be
another line in the file.
*/
protected void export(String filename, Vector strings)
throws Exception {
	String routine = "StateMod_Data_JFrame.export";
	// First see if we can write the file given the security
	// settings...
	if (!SecurityCheck.canWriteFile(filename)) {
		Message.printWarning(1, routine,
			"Cannot save \"" + filename + "\".");
		throw new Exception("Security check failed - unable to write \""
			+ filename + "\"");
	}

	JGUIUtil.setWaitCursor(this, true);

	// Create a new FileOutputStream wrapped with a DataOutputStream
	// for writing to a file.
	PrintWriter oStream = null;
	try {
		oStream = new PrintWriter( 
			new FileWriter(filename));
	}
	catch (Exception IOException) {
		JGUIUtil.setWaitCursor(this, false);
		throw new Exception("Error opening file \"" 
			+ filename + "\"." );
	}
		
	try {
		// Write each element of the strings Vector to a file.
		// For some reason, when just using println in an
		// applet, the cr-nl pair is not output like it should
		// be on Windows95.  Java Bug???
		String linesep = System.getProperty("line.separator");
		int size = strings.size();
		for (int i = 0; i < size; i++) {
			oStream.print(
				strings.elementAt(i).toString() + linesep);
		}
		oStream.flush(); 
		oStream.close(); 
	}
	catch (Exception IOError) {
		JGUIUtil.setWaitCursor(this, false);
		throw new Exception("Error writing to file \"" 
			+ filename + "\"." );
	}

	JGUIUtil.setWaitCursor(this, false);
}

/**
Cleans up member variables. 
*/
protected void finalize()
throws Throwable {
	__scrollWorksheet = null;
	_worksheet = null;
	_data = null;
}

/**
Formats the data in the worksheet into a Vector of Strings.  Each field in the
worksheet will be separated from the next by the specified delimiter, with
no trailing delimiter.
@param delimiter the character String to use to separate worksheet fields.
@param quotes if true, then the column names will be surrounded by 
quotes.  In addition, if the column data has the delimiter in it, it will be
surrounded by quotes.  If false, nothing will be surrounded by quotes.
@return a Vector of delimited strings.  Each element in the Vector is one
row in the worksheet.
*/
protected Vector formatOutput(String delimiter, boolean trimFieldValue,
boolean quotes) {
	Vector v = new Vector();

	int rows = _worksheet.getRowCount();
	int cols = _worksheet.getColumnCount();

	StringBuffer buff = new StringBuffer();
	String quote = "\"";
	for (int i = 0; i < cols; i++) {
		if (quotes) {
			buff.append(quote);
		}
		buff.append(_worksheet.getColumnName(i, true));
		if (quotes) {
			buff.append(quote);
		}
		if (i < cols - 1) {
			buff.append(delimiter);
		}
	}

	v.add(buff.toString());

	int j = 0;
	String s = null;
	for (int i = 0; i < rows; i++) {
		buff.setLength(0);

		for (j = 0; j < cols; j++) {
			s = _worksheet.getValueAtAsString(i, j);
			if (trimFieldValue) {
				s = s.trim();
			}
			if (s.indexOf(delimiter) > -1 && quotes) {
				buff.append("\"" + s + "\"");
			}
			else {
				buff.append(s);
			}
			
			if (j < cols - 1) {
				// do not do for the last column.
				buff.append(delimiter);
			}
		}
		
		v.add(buff.toString());
	}

	return v;
}

/**
Returns the filename and format type of a file selected from a file chooser
in order that the kind of delimiter for the file can be known when the data
is formatted for output.  Currently the only kinds of files that the data
can be exported to are delimited files.  No StateMod files are yet supported.<p>
Also sets the last selected file dialog directory to whatever directory the
file is located in, if the file selection was approved (i.e., Cancel was not
pressed).
@param title the title of the file chooser.
@param formats a Vector of the valid formats for the file chooser.
@return a two-element String array where the first element is the name of the
file and the second element is the delimiter selected.
*/
protected String[] getFilenameAndFormat() {
	JGUIUtil.setWaitCursor(this, true);
	String dir = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc = JFileChooserFactory.createJFileChooser(dir);
	fc.setDialogTitle("Select Export File");

	SimpleFileFilter tabFF = new SimpleFileFilter("txt", "Tab-delimited");
	SimpleFileFilter commaFF = new SimpleFileFilter("csv", 
		"Comma-delimited");
	SimpleFileFilter semiFF = new SimpleFileFilter("txt", 
		"Semicolon-delimited");
	SimpleFileFilter pipeFF = new SimpleFileFilter("txt", "Pipe-delimited");

	fc.addChoosableFileFilter(commaFF);
	fc.addChoosableFileFilter(pipeFF);
	fc.addChoosableFileFilter(semiFF);
	fc.addChoosableFileFilter(tabFF);
	
	fc.setAcceptAllFileFilterUsed(false);
	fc.setFileFilter(commaFF);		
	fc.setDialogType(JFileChooser.SAVE_DIALOG);

	JGUIUtil.setWaitCursor(this, false);
	int returnVal = fc.showSaveDialog(this);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
		String[] ret = new String[2];
		String filename = fc.getCurrentDirectory() + File.separator 
			+ fc.getSelectedFile().getName();
		JGUIUtil.setLastFileDialogDirectory("" 
			+ fc.getCurrentDirectory());
		SimpleFileFilter sff = (SimpleFileFilter)fc.getFileFilter();

		// this will always return a one-element vector
		Vector extensionV = sff.getFilters();

		String extension = (String)extensionV.elementAt(0);
		
		String desc = sff.getShortDescription();
		String delimiter = "\t";

		if (desc.equals("Tab-delimited")) {
			delimiter = "\t";
		}
		else if (desc.equals("Comma-delimited")) {
			delimiter = ",";
		}
		else if (desc.equals("Semicolon-delimited")) {
			delimiter = ";";
		}
		else if (desc.equals("Pipe-delimited")) {
			delimiter = "|";
		}

		ret[0] = IOUtil.enforceFileExtension(filename, extension);
		ret[1] = delimiter;

		return ret;
	}
	else {
		return null;
	}
}

/**
Initializes the data and sets up the GUI, using values that were passed into
the constructor.  Alternately, if other setup needs done prior to the table 
models being built and the GUI set up, this can be called separately with 
the same parameters that would be into the GUI.
@param data the data to display in the worksheet.  Can be null or empty, in
which case an empty worksheet is shown.
@param titleString the String to display as the GUI's title.
@param editable whether the data in the JFrame can be edited or not.  If true
the data can be edited, if false they can not.
@throws Exception if there is an error building the worksheet.
*/
public void initialize(Vector data, String titleString, boolean editable) 
throws Exception {
	if (data == null) {
		_data = new Vector();
	}
	else {
		_data = data;
	}

	createDataBackup();

	_props = new PropList("Worksheet Props");
	_props.add("JWorksheet.ShowPopupMenu=true");
	_props.add("JWorksheet.AllowCopy=true");
	_props.add("JWorksheet.ShowRowHeader=true");
	_props.add("JWorksheet.SelectionMode="
		+ "MultipleDiscontinuousRowSelection");

	_titleString = titleString;
	_editable = editable;

	setupGUI();
}

/**
Prints the data from the worksheet.
*/
public void print() {
	String titleString = getTitle();
	int index = titleString.indexOf("-");
	if (index > -1) {
		titleString = (titleString.substring(index + 1)).trim();
	}
	Vector v = formatOutput(" ", false, false);
	if (v.size() > 40) {
		for (int i = v.size() - 1; i > 40; i--) {
			v.removeElementAt(i);
		}
	}
	String s = (new RTi.Util.GUI.TextResponseJDialog(this, 
		"Lines per page:")).response();
	if (RTi.Util.String.StringUtil.atoi(s) <= 0) {
		return;
	}
	int lines = RTi.Util.String.StringUtil.atoi(s);
	ReportPrinter.printText(v,
//		PRINT_PORTRAIT_LINES,
//		PRINT_LANDSCAPE_LINES,	
		lines,
		lines,
		titleString, 
		false, 		// not printing in batch mode (ie, a dialog
				// will appear for user interaction)
		null);		// do not use a pre-defined PageFormat for
				// this print job
}

/**
Sets the visibility of the GUI.  Overrides the base setVisible() method in 
order to guarantee that the GUI is repainted properly and sized properly, which
was proving an issue in StateDMI.
@param visible whether the GUI should be made visible or not.
*/
public void setVisible(boolean visible) {
	super.setVisible(visible);
	setSize(getWidth(), getHeight());
}

/**
Sets the size of the GUI.  Overrides the base setSize() method because 
the GUI was not repainting properly in StateDMI after a resize, and this 
ensures that developers do not need to call setSize();validate();repaint() 
everytime a resize is necessary.
@param width the new width of the GUI window.
@param height the new height of the GUI window.
*/
public void setSize(int width, int height) {
	super.setSize(width, height);
	JGUIUtil.center(this);
	invalidate();
	validate();
	repaint();
}

/**
Sets up the GUI.
@throws Exception if there is an error setting up the worksheet.
*/
private void setupGUI() 
throws Exception {
	addWindowListener(this);

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	// buildJScrollWorksheet is defined in the derived classes (it is
	// abstract in this class).  It creates a JScrollWorksheet and returns 
	// it.  
	__scrollWorksheet = buildJScrollWorksheet();
	_worksheet = __scrollWorksheet.getJWorksheet();

	JGUIUtil.addComponent(panel, __scrollWorksheet,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	
	getContentPane().add("Center", panel);

	SimpleJButton exportButton = new SimpleJButton(_BUTTON_EXPORT, this);
	SimpleJButton printButton = new SimpleJButton(_BUTTON_PRINT, this);
	SimpleJButton okButton = new SimpleJButton(_BUTTON_OK, this);
	
	SimpleJButton applyButton = null;
	SimpleJButton cancelButton = null;
	if (_editable) {
		applyButton = new SimpleJButton(_BUTTON_APPLY, this);
		cancelButton = new SimpleJButton(_BUTTON_CANCEL, this);
	}

	JPanel bottomPanel = new JPanel();
	bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	bottomPanel.add(exportButton);
	bottomPanel.add(printButton);
	bottomPanel.add(okButton);
	
	if (_editable) {
		bottomPanel.add(applyButton);
		bottomPanel.add(cancelButton);
	}

	getContentPane().add("South", bottomPanel);

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	
	setTitle(_titleString);
	pack();

	setSize(500, 300);
	JGUIUtil.center(this);
	
	setVisible(true);

	JWorksheet_DefaultTableCellRenderer renderer 
		= _worksheet.getCellRenderer();
	int[] widths = renderer.getColumnWidths();
	if (widths != null) {
		_worksheet.setColumnWidths(widths);
	}
}

public void windowActivated(WindowEvent event) {}
public void windowClosed(WindowEvent event) {}
public void windowClosing(WindowEvent event) {
	setVisible(false);
}
public void windowDeactivated(WindowEvent event) {}
public void windowDeiconified(WindowEvent event) {
	Message.printStatus(1, "", "Size: " + getWidth() + ", " + getHeight());
}
public void windowIconified(WindowEvent event) {}
public void windowOpened(WindowEvent event) {}

}
