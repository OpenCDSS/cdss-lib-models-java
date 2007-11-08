//------------------------------------------------------------------------------
// StateMod_ReservoirRight - Derived from StateMod_Data class
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 25 Jun 2000	Steven A. Malers, RTi	Make so that res rights < 1.0 are
//					printed with 8.2 precision.  This is an
//					issue in the Rio Grande for very small
//					rights(e.g., stock ponds).
// 17 Feb 2001	SAM, RTi		Code review.  Add finalize().  Clean up
//					javadoc.  Handle nulls and set unused
//					variables to null.  Alphabetize methods.
//					Change so right admin numbers are right
//					justified but add option to print old
//					style.  Change IO to IOUtil.
// 02 Mar 2001	SAM, RTi		Ray says to use F16.5 for water rights
//					and get rid of the 4X.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, Rti	Renamed from SMResRights to 
//					StateMod_ReservoirRight
// 2003-06-10	JTS, RTi		* Folded dumpReservoirRightsFile() into
//					  writeReservoirRightsFile()
//					* Renamed parseReservoirRightsFile() to
//					  readReservoirRightsFile()
// 2003-06-23	JTS, RTi		Renamed writeReservoirRightsFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readReservoirRightsFile() to
//					readStateModFile()
// 2003-07-15	JTS, RTi		Changed to use new dataset design.
// 2003-08-03	SAM, RTi		Change isDirty() back to setDirty().
// 2003-08-28	SAM, RTi		* Remove linked list data since a
//					  Vector of rights is maintained in the
//					  StateMod_Reservoir.
//					* Clean up parameters to methods to be
//					  clearer.
//					* Alphabetize methods.
// 2003-10-09	JTS, RTi		* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
// 					* Added equals(Vector, Vector)
// 2003-10-15	JTS, RTi		* Revised the clone() code.
//					* Added toString().
// 2003-10-15	SAM, RTi		Changed some initial values to agree
//					with the old GUI for new instances.
// 2004-07-08	SAM, RTi		* Add getIrescoChoices() and
//					  getIrescoDefault() for use by GUIs.
//					* Add getItyrsrChoices() and
//					  getItyrsrDefault().
//					* Add getN2fillChoices() and
//					  getN2fillDefault().
// 2004-09-14	SAM, RTi		Open files considering the working
//					directory.
// 2004-10-28	SAM, RTi		Add getIrsrswChoices() and
//					getIrsrswDefault().
// 2004-10-29	SAM, RTi		Remove temporary data members used with
//					table model.  Displaying the expanded
//					strings only in the dropdown is OK and
//					makes the code simpler.
// 2004-11-11	SAM, RTi		Fix getIrescoChoices() - was showing
//					a negative number in the note.
// 2005-01-17	JTS, RTi		* Added createBackup().
//					* Added restoreOriginal().
// 2005-03-14	SAM, RTi		Clarify output header for switch.
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2007-04-12	Kurt Tometich, RTi		Added checkComponentData() and
//									getDataHeader() methods for check
//									file and data check support.
// 2007-05-16	SAM, RTi		Implement StateMod_Right interface.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_ReservoirRight extends StateMod_Data 
implements Cloneable, Comparable, StateMod_Component, StateMod_Right {
/**
Administration number
*/
protected String 	_rtem;
/**
Decreed amount
*/
protected double	_dcrres;
/**
Filling ratio
*/
protected int		_iresco;
/**
Reservoir type
*/
protected int		_ityrstr;
/**
Reservoir right type
*/
protected int		_n2fill;
/**
out of priority associated op right
*/
protected String	_copid;
	
/**
Constructor.
*/
public StateMod_ReservoirRight() {
	super();
	initialize();
}

/**
@param count Number of components checked.
@param dataset StateMod dataset object.
@param props Extra properties for specific data checks.
@return List of data that failed specific checks.
 */
public String[] checkComponentData( int count, 
StateMod_DataSet dataset, PropList props ) 
{
	// TODO KAT 2007-04-16
	// add specific checks here
	return null;
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_ReservoirRight right = (StateMod_ReservoirRight)super.clone();
	right._isClone = true;
	return right;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by rtem, dcrres, iresco,
ityrstr, n2fill and copid, in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_ReservoirRight right = (StateMod_ReservoirRight)o;

	res = _rtem.compareTo(right.getRtem());
	if (res != 0) {
		return res;
	}

	if (_dcrres < right._dcrres) {
		return -1;
	}
	else if (_dcrres > right._dcrres) {
		return 1;
	}

	if (_iresco < right._iresco) {
		return -1;
	}
	else if (_iresco > right._iresco) {
		return 1;
	}

	if (_ityrstr < right._ityrstr) {
		return -1;
	}
	else if (_ityrstr > right._ityrstr) {
		return 1;
	}

	if (_n2fill < right._n2fill) {
		return -1;
	}
	else if (_n2fill > right._n2fill) {
		return 1;
	}

	res = _copid.compareTo(right._copid);
	return res;	
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_ReservoirRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_ReservoirRight s to check.  Can not
be null.
@param v2 the second Vector of StateMod_ReservoirRight s to check.  Can not
be null.
@return true if they are the same, false if not.
*/
public static boolean equals(Vector v1, Vector v2) {
	String routine = "StateMod_ReservoirRight.equals(Vector, Vector)";
	StateMod_ReservoirRight r1;	
	StateMod_ReservoirRight r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(1, routine, "Vectors are different sizes");
		return false;
	}
	else {
		// sort the Vectors and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		Message.printStatus(1, routine, "Vectors are of size: " + size);
		Vector v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		Vector v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(1, routine, "Vectors have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = (StateMod_ReservoirRight)v1Sort.elementAt(i);	
			r2 = (StateMod_ReservoirRight)v2Sort.elementAt(i);	
			Message.printStatus(1, routine, r1.toString());
			Message.printStatus(1, routine, r2.toString());
			Message.printStatus(1, routine, "Element " + i 
				+ " comparison: " + r1.compareTo(r2));
			if (r1.compareTo(r2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two diversion rights are equal.  Strings are compared with
case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_ReservoirRight right) {
	 if (!super.equals(right)) {
	 	return false;
	}
	
	if (	right._rtem.equals(_rtem)
		&& right._dcrres == _dcrres
		&& right._iresco == _iresco
		&& right._ityrstr == _ityrstr
		&& right._n2fill == _n2fill
		&& right._copid.equals(_copid)) {
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_rtem = null;
	_copid = null;
	super.finalize();
}

/**
Return the administration number, as per the generic interface.
@return the administration number, as a String to protect from roundoff.
*/
public String getAdministrationNumber ()
{	return getRtem();
}

/**
Return the out-of-priority associated op right.
*/
public String getCopid() {
	return _copid;
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-16 
	// When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Return the decreed amount.
*/
public double getDcrres() {
	return _dcrres;
}

/**
Return the decree, as per the generic interface.
@return the decree, in the units of the data.
*/
public double getDecree()
{	return getDcrres();
}

// TODO SAM 2007-05-15 Need to evaluate whether should be hard-coded.
/**
Return the decree units.
@return the decree units.
*/
public String getDecreeUnits()
{	return "ACFT";
}

/**
Return the right identifier, as per the generic interface.
@return the right identifier.
*/
public String getIdentifier()
{	return getID();
}

/**
Return the filling ratio.
*/
public int getIresco() {
	return _iresco;
}

/**
Return a list of account distribution switch option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Account to be served by right", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getIrescoChoices ( boolean include_notes )
{	Vector v = new Vector(102);	// Allow for one blank in StateDMI
	for ( int i = 1; i <= 50; i++ ) {
		v.addElement ( "" + i + " - Account served by right" );
	}
	for ( int i = -1; i >= -50; i-- ) {
		v.addElement ( "" + i + " - Fill first " + (-i) + " accounts" );
	}
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.setElementAt(StringUtil.getToken(
				(String)v.elementAt(i), " ", 0, 0), i );
		}
	}
	return v;
}

/**
Return the default account distribution switch choice.  This can be used by GUI
code to pick a default for a new reservoir.
@return the default reservoir account distribution.
*/
public static String getIrescoDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return ( "1 - Account served by right" );
	}
	else {	return "1";
	}
}

/**
Return a list of on/off switch option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and
"0 - Off", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getIrsrswChoices ( boolean include_notes )
{	return StateMod_DiversionRight.getIdvrswChoices ( include_notes );
}

/**
Return the default on/off switch choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default reservoir on/off choice.
*/
public static String getIrsrswDefault ( boolean include_notes )
{	return StateMod_DiversionRight.getIdvrswDefault ( include_notes );	
}

/**
Return the reservoir type.
*/
public int getItyrstr() {
	return _ityrstr;
}

/**
Return a list of right type option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Standard", if include_notes is true.
@return a list of fill switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getItyrsrChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "1 - Standard" ); // Possible options are listed here.
	v.addElement ( "-1 - Out of priority water right" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.setElementAt(StringUtil.getToken(
				(String)v.elementAt(i), " ", 0, 0), i );
		}
	}
	return v;
}

/**
Return the default right type choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default right type choice.
*/
public static String getItyrsrDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return ( "1 - Standard" );
	}
	else {	return "1";
	}
}

/**
Return the right location identifier, as per the generic interface.
@return the right location identifier (location where right applies).
*/
public String getLocationIdentifier()
{	return getCgoto();
}

/**
Retrieve the reservoir right type.
*/
public int getN2fill() {
	return _n2fill;
}

/**
Return a list of fill switch option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - First fill", if include_notes is true.
@return a list of fill switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getN2fillChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "1 - First fill" ); // Possible options are listed here.
	v.addElement ( "2 - Second fill" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.setElementAt(StringUtil.getToken(
				(String)v.elementAt(i), " ", 0, 0), i );
		}
	}
	return v;
}

/**
Return the default fill type choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default fill type choice.
*/
public static String getN2fillDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return ( "1 - First fill" );
	}
	else {	return "1";
	}
}

/**
Return the administration number.
*/
public String getRtem() {
	return _rtem;
}

/**
INitialize data members
*/
private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_RESERVOIR_RIGHTS;
	_rtem = "99999";	// Default as per old SMGUI.
	_copid = "";
	_dcrres	= 0;
	_iresco	= 1;	// Server first account, as per old SMGUI default
	_ityrstr= 1;
	_n2fill	= 1;
}


/**
Determine whether a file is a reservoir right file.  Currently true is returned
if the file extension is ".rer".
@param filename Name of the file being checked.
@return true if the file is a StateMod reservoir right file.
*/
public static boolean isReservoirRightFile ( String filename )
{	if ( filename.toUpperCase().endsWith(".RER")) {
		return true;
	}
	return false;
}

/**
Read reservoir right information in and store in a Vector.
@param filename Name of file to read.
@return Vector of reservoir right data
@exception Exception if there is an error reading the file.
*/
public static Vector readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_ReservoirRight.readStateModFile";
	Vector theRights = new Vector();
	int [] format_0 = {	StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_STRING };
	int [] format_0w = {	12,
				24,
				12,
				16,
				8,
				8,
				8,
				8,
				8,
				12 };
	String iline = null;
	Vector v = new Vector(10);
	BufferedReader in = null;
	StateMod_ReservoirRight aRight = null;

	Message.printStatus(1, routine, "Reading reservoir rights file: " +
		filename);
	
	try {	in = new BufferedReader(new FileReader(
			IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine())!= null) {
			if (iline.startsWith("#")|| iline.trim().length()==0) {
				continue;
			}

			aRight = new StateMod_ReservoirRight();

			StringUtil.fixedRead(iline, format_0, format_0w, v);
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, 
				"iline: " + iline);
			}
			aRight.setID(((String)v.elementAt(0)).trim());
			aRight.setName(((String)v.elementAt(1)).trim());
			aRight.setCgoto(((String)v.elementAt(2)).trim());
			aRight.setRtem(((String)v.elementAt(3)).trim());
			aRight.setDcrres((Double)v.elementAt(4));
			aRight.setSwitch((Integer)v.elementAt(5));
			aRight.setIresco((Integer)v.elementAt(6));
			aRight.setItyrstr((Integer)v.elementAt(7));
			aRight.setN2fill((Integer)v.elementAt(8));
			aRight.setCopid(((String)v.elementAt(9)).trim());
			theRights.addElement(aRight);
		}
	} 
	catch (Exception e) {
		routine = null;
		format_0 = null;
		format_0w = null;
		iline = null;
		v = null;
		if (in != null) {
			in.close();
		}
		in = null;
		aRight = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
	routine = null;
	format_0 = null;
	format_0w = null;
	iline = null;
	v = null;
	if (in != null) {
		in.close();
	}
	in = null;
	aRight = null;
	return theRights;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReservoirRight r = (StateMod_ReservoirRight)_original;
	super.restoreOriginal();
	_rtem = r._rtem;
	_dcrres = r._dcrres;
	_iresco = r._iresco;
	_ityrstr = r._ityrstr;
	_n2fill = r._n2fill;
	_copid = r._copid;
	_isClone = false;
	_original = null;
}

/**
Set the out-of-priority associated op right.
*/
public void setCopid(String copid) {
	if ( (copid != null) && !copid.equals(_copid) ) {
		_copid = copid;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount.
*/
public void setDcrres(double dcrres) {
	if (dcrres != _dcrres) {
		_dcrres = dcrres;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount.
*/
public void setDcrres(Double dcrres) {
	setDcrres(dcrres.doubleValue());
}

/**
Set the decreed amount.
*/
public void setDcrres(String dcrres) {
	if (dcrres != null) {
		setDcrres(StringUtil.atod(dcrres.trim()));
	}
}

/**
Set the filling ratio.
*/
public void setIresco(int iresco) {
	if (iresco != _iresco) {
		_iresco = iresco;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the filling ratio.
*/
public void setIresco(Integer iresco) {
	setIresco(iresco.intValue());
}

/**
Set the filling ratio.
*/
public void setIresco(String iresco) {
	if (iresco != null) {
		setIresco(StringUtil.atoi(iresco.trim()));
	}
}

/**
Set the reservoir type.
*/
public void setItyrstr(int ityrstr) {
	if (ityrstr != _ityrstr) {
		_ityrstr = ityrstr;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the reservoir type.
*/
public void setItyrstr(Integer ityrstr) {
	setItyrstr(ityrstr.intValue());
}

/**
Set the reservoir type.
*/
public void setItyrstr(String ityrstr) {
	if (ityrstr != null) {
		setItyrstr(StringUtil.atoi(ityrstr.trim()));
	}
}

/**
Set the reservoir right type.
*/
public void setN2fill(int n2fill) {
	if (n2fill != _n2fill) {
		_n2fill = n2fill;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the reservoir right type.
*/
public void setN2fill(Integer n2fill) {
	setN2fill(n2fill.intValue());
}

/**
Set the reservoir right type.
*/
public void setN2fill(String n2fill) {
	if (n2fill != null) {
		setN2fill(StringUtil.atoi(n2fill.trim()));
	}
}

/**
Set the administration number.
*/
public void setRtem(String rtem) {
	if ( (rtem != null) && !rtem.equals(_rtem) ) {
		_rtem = rtem;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Write reservoir right information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theRights vector of reservoir right to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
Vector theRights, String []newComments)
throws Exception {
	writeStateModFile(infile, outfile, theRights, newComments,false);
}

/**
Write reservoir right information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theRights vector of reservoir right to print
@param newComments addition comments which should be included in history
@param oldAdminNumFormat whether to use the old admin num format or not
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
Vector theRights, String []newComments, boolean oldAdminNumFormat)
throws Exception {
	String [] comment_str = { "#" };	
	String [] ignore_comment_str = { "#>" };	
	String routine = "StateMod_ReservoirRight.writeStateModFile";
	PrintWriter out = null;

	if (Message.isDebugOn) {
		Message.printDebug(2, routine, 
		"Print reservoir rights to file: " + outfile);
	}
	try {	
	out = IOUtil.processFileHeaders(
		IOUtil.getPathUsingWorkingDir(infile),
		IOUtil.getPathUsingWorkingDir(outfile), 
		newComments, comment_str, ignore_comment_str, 0);

	String iline = null;
	String cmnt = "#>";
	StateMod_ReservoirRight right = null;
	Vector v = new Vector(10);
	String format_0 = null;
	String format_1 = null;
	if (oldAdminNumFormat) {
		// Left justify...
		format_0 =
			"%-12.12s%-24.24s%-12.12s    %-12.12s%8.0f%8d%8d%8d%8d"
			+ "%-12.12s";
		format_1 =
			"%-12.12s%-24.24s%-12.12s    %-12.12s%8.2f%8d%8d%8d"
			+ "%8d%-12.12s";
	}
	else {	
		// Right justify...
		format_0 =
			"%-12.12s%-24.24s%-12.12s    %12.12s%8.0f%8d%8d%8d%8d"
			+ "%-12.12s";
		format_1 = 
			"%-12.12s%-24.24s%-12.12s    %12.12s%8.2f%8d%8d%8d%8d"
			+ "%-12.12s";
	}

	out.println(cmnt);
	out.println(cmnt
		+ " *******************************************************");
	out.println(cmnt
		+ "  Reservoir Right File");
	out.println(cmnt);
	out.println(cmnt
		+ "  format:  (a12, a24, a12, F16.5, f8.0, 4i8, a12)");
	out.println(cmnt);
	out.println(cmnt
		+ "  ID       cirsid:  Reservoir right ID");
	out.println(cmnt
		+ "  Name      namer:  Reservoir name");
	out.println(cmnt
		+ "  Res ID    cgoto:  Reservoir ID tied to this right");
	out.println(cmnt
		+ "  Admin #    rtem:  Administration number");
	out.println(cmnt
		+ "                    (small is senior).");
	out.println(cmnt
		+ "  Decree   dcrres:  Decreed amount (af)");
	out.println(cmnt
		+ "  On/Off   irsrsw:  Switch 0 = off,1 = on");
	out.println(cmnt
		+ "                    YYYY = on for years >= YYYY" );
	out.println(cmnt
		+ "                    -YYYY = off for years > YYYY" );
	out.println(cmnt
		+ "  Owner    iresco:  Ownership code");
	out.println(cmnt
		+ "                      >0, account to be filled");
	out.println(cmnt
		+ "                      <0, ownership go to 1st (n) accounts");
	out.println(cmnt
		+ "  Type     ityrsr:  Reservoir type");
	out.println(cmnt
		+ "                      1=Standard");
	out.println(cmnt
		+ "                      2=Out of priority water right");
	out.println(cmnt
		+ "  Fill #   n2fill:  Right type 1=1st fill, 2=2nd "
		+ "fill");
	out.println(cmnt
		+"  Out ID    copid:  Out of priority associated operational ");
	out.println(cmnt
		+ "                      right  (when ityrsr=-1)");
	out.println(cmnt);
	out.println(cmnt
		+ "    ID     Name                    Res ID          "
		+ "  Admin #   Decree  On/Off  Owner   Type    Fill # "
		+ " Out ID     ");
	out.println(cmnt
		+ "---------eb----------------------eb----------eb---"
		+ "-----------eb------eb------eb------eb------eb------e"
		+ "b----------e");
	out.println(cmnt);
	out.println(cmnt + "EndHeader");
	out.println(cmnt);

	int num = 0;
	if (theRights != null) {
		num = theRights.size();
	}
	for (int i = 0; i < num; i++) {
		right =(StateMod_ReservoirRight)theRights.elementAt(i);
		if (right == null) {
			continue;
		}
		v.removeAllElements();
		v.addElement(right.getID());
		v.addElement(right.getName());
		v.addElement(right.getCgoto());
		v.addElement(right.getRtem());
		v.addElement(new Double(right.getDcrres()));
		v.addElement(new Integer(right.getSwitch()));
		v.addElement(new Integer(right.getIresco()));
		v.addElement(new Integer(right.getItyrstr()));
		v.addElement(new Integer(right.getN2fill()));
		v.addElement(right.getCopid());
		if (right.getDcrres()< 1.0) {
			// Use the format for a small right(8.2)...
			iline = StringUtil.formatString(v, format_1);
		}
		else {	
			// Default format 8.0...
			iline = StringUtil.formatString(v, format_0);
		}
		out.println(iline);
	}		

	out.flush();
	out.close();
	out = null;
	comment_str = null;
	ignore_comment_str = null;
	routine = null;
	}
	catch (Exception e) {
		if (out != null) {
			out.close();
		}
		out = null;
		comment_str = null;
		ignore_comment_str = null;
		routine = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
}

/**
Writes a Vector of StateMod_XXX objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter 
will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the Vector of objects to write.  
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter,
boolean update, Vector data) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	Vector fields = new Vector();
	fields.add("ID");
	fields.add("Name");
	fields.add("StructureID");
	fields.add("AdministrationNumber");
	fields.add("DecreedAmount");
	fields.add("OnOff");
	fields.add("AccountDistribution");
	fields.add("Type");
	fields.add("FillType");
	fields.add("OopRight");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RESERVOIR_RIGHTS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.elementAt(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	StateMod_ReservoirRight right = null;
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
	String[] line = new String[fieldCount];
	String[] newComments = null;
	StringBuffer buffer = new StringBuffer();
	PrintWriter out = null;

	try {	
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names[i] + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			right = (StateMod_ReservoirRight)data.elementAt(i);
			
			line[0] = StringUtil.formatString(right.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(right.getRtem(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrres(), 
				formats[4]).trim();
			line[5] = StringUtil.formatString(right.getSwitch(), 
				formats[5]).trim();
			line[6] = StringUtil.formatString(right.getIresco(), 
				formats[6]).trim();
			line[7] = StringUtil.formatString(right.getItyrstr(), 
				formats[7]).trim();
			line[8] = StringUtil.formatString(right.getN2fill(), 
				formats[8]).trim();
			line[9] = StringUtil.formatString(right.getCopid(), 
				formats[9]).trim();

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if (line[j].indexOf(delimiter) > -1) {
					line[j] = "\"" + line[j] + "\"";
				}
				buffer.append(line[j]);
				if (j < (fieldCount - 1)) {
					buffer.append(delimiter);
				}
			}

			out.println(buffer.toString());
		}
		out.flush();
		out.close();
		out = null;
	}
	catch (Exception e) {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
		throw e;
	}
}

} // End StateMod_ReservoirRight
