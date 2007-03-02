//------------------------------------------------------------------------------
// StateMod_DiversionRight - Derived from SMData class
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 27 Aug 1997	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 11 Feb 1998	Catherine E.		Added SMFileData.setDirty to all set
//		Nutting-Lane, RTi	routines.  Added throws IOException to 
//					read/write routines
// 16 Feb 2001	Steven A. Malers, RTi	Update output header to be consistent
//					with new documentation.  Add finalize().
//					Alphabetize methods.  Set unused
//					variables to null.  Handle null
//					arguments.  Change IO to IOUtil.  Get
//					rid of low-level debugs that are not
//					needed.
// 02 Mar 2001	SAM, RTi		Ray says to use F16.0 for rights and
//					get rid of the 4x.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMDivRights to 
//					StateMod_DiversionRight
// 2003-06-10	JTS, RTi		* Folded dumpDiversionRightsFile() into
//					  writeDiversionRightsFile()
//					* Renamed parseDiversionRightsFile() to
//					  readDiversionRightsFile()
// 2003-06-23	JTS, RTi		Renamed writeDiversionRightsFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readDiversionRightsFile() to
//					readStateModFile()
// 2003-07-07	SAM, RTi		Check for null data set to allow the
//					code to be used outside of a full
//					StateMod data set implementation.
// 2003-07-15	JTS, RTi		Changed code to use new dataset design.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-27	SAM, RTi		Change default value of irtem to
//					99999.
// 2003-08-28	SAM, RTi		* Remove linked list logic since a
//					  Vector of rights is now maintained in
//					  StateMod_Diversion.
//					* Call setDirty() on the individual
//					  objects as well as the component.
//					* Clean up Javadoc for parameters to
//					  make more readable.
// 2003-10-09	JTS, RTi		* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
// 2003-10-10	JTS, RTI		Added equals(Vector, Vector)
// 2003-10-14	JTS, RTi		* Make sure diversion right is marked
//					  not dirty after initial read and
//					  construction.
// 2003-10-15	JTS, RTi		Revised the clone() code.
// 2004-10-28	SAM, RTi		Add getIdvrswChoices() and
//					getIdvrswDefault().
// 2005-01-13	JTS, RTi		* Added createBackup().
// 					* Added restoreOriginal().
// 2005-03-13	SAM, RTi		* Clean up output header information for
//					  switch.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import java.lang.Double;

import java.util.Vector;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

public class StateMod_DiversionRight extends StateMod_Data 
implements Cloneable, Comparable {

/**
Administration number.
*/
protected String 	_irtem;	

/**
Decreed amount
*/
protected double	_dcrdiv;

// ID, Name, and Cgoto are in the base class.
	
/**
Constructor.
*/
public StateMod_DiversionRight() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_DiversionRight right = (StateMod_DiversionRight)super.clone();
	right._irtem = new String(_irtem);
	right._dcrdiv = _dcrdiv;
	right._isClone = true;

	return right;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by irtem and dcrdiv, in that
order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_DiversionRight right = (StateMod_DiversionRight)o;

	res = _irtem.compareTo(right.getIrtem());
	if (res == 0) {
		double dcrdiv = right.getDcrdiv();
		if (dcrdiv == _dcrdiv) {
			return 0;
		}
		else if (_dcrdiv < dcrdiv) {
			return -1;
		}
		else {
			return 1;
		}
	}
	else {
		return res;
	}
}

/**
Creates a copy of the object for later use in checking to see if it was 
changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_DiversionRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_DiversionRights to check.  Can not
be null.
@param v2 the second Vector of StateMod_DiversionRights to check.  Can not
be null.
@return true if they are the same, false if not.
*/
public static boolean equals(Vector v1, Vector v2) {
	String routine = "StateMod_DiversionRight.equals(Vector, Vector)";
	StateMod_DiversionRight r1;	
	StateMod_DiversionRight r2;	
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
			r1 = (StateMod_DiversionRight)v1Sort.elementAt(i);	
			r2 = (StateMod_DiversionRight)v2Sort.elementAt(i);	
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
public boolean equals(StateMod_DiversionRight right) {
	 if (!super.equals(right)) {
	 	return false;
	}
	if (	right._irtem.equals(_irtem)
		&& right._dcrdiv == _dcrdiv) {
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_irtem = null;
	super.finalize();
}

/**
Return the decreed amount.
*/
public double getDcrdiv() {
	return _dcrdiv;
}

/**
Return a list of on/off switch option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and
"0 - Off", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getIdvrswChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "0 - Off" );	// Possible options are listed here.
	v.addElement ( "1 - On" );
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
Return the default on/off switch choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default reservoir replacement choice.
*/
public static String getIdvrswDefault ( boolean include_notes )
{	// Make this agree with the above method...
	if ( include_notes ) {
		return ( "1 - On" );
	}
	else {	return "1";
	}
}

/**
Return the administration number.
*/
public String getIrtem() {
	return _irtem;
}

/**
Initializes data members.
*/
private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_DIVERSION_RIGHTS;
	_irtem = "99999";
	_dcrdiv = 0;
}

/**
Parses the diverion rights file and returns a Vector of StateMod_DiversionRight
objects.
@param filename the diversion rights file to parse.
@return a Vector of StateMod_DiversionRight objects.
@throws Exception if an error occurs
*/
public static Vector readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_DiversionRight.readStateModFile";
	Vector theDivRights = new Vector();

	int format_0[] = {	StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER };
	int format_0w[] = {	12,
				24,
				12,
				16,
				8,
				8 };
	String iline = null;
	Vector v = new Vector(6);
	BufferedReader in = null;
	StateMod_DiversionRight aRight = null;

	Message.printStatus(1, routine, "Reading diversion rights file: "
		+ filename);

	try {	
		in = new BufferedReader(new FileReader(
			IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine())!= null) {
			// check for comments
			if (iline.startsWith("#")|| 
				iline.trim().length()== 0) {
				continue;
			}
			
			aRight = new StateMod_DiversionRight();

			if (Message.isDebugOn) {
				Message.printDebug(50, routine , 
				"iline: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aRight.setID(((String)v.elementAt(0)).trim()); 
			aRight.setName(((String)v.elementAt(1)).trim());
			aRight.setCgoto(((String)v.elementAt(2)).trim());
			aRight.setIrtem(((String)v.elementAt(3)).trim());
			aRight.setDcrdiv((Double)v.elementAt(4));
			aRight.setSwitch((Integer)v.elementAt(5));
			// Mark as clean because set methods may have marked
			// dirty...
			aRight.setDirty ( false );
			theDivRights.addElement(aRight);
		}
	} 
	catch (Exception e) {
		routine = null;
		format_0 = null;
		format_0w = null;
		iline = null;
		v = null;
		aRight = null;
		if (in != null) {
			in.close();
		}
		in = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
	routine = null;
	format_0 = null;
	format_0w = null;
	iline = null;
	v = null;
	aRight = null;
	if (in != null) {
		in.close();
	}
	in = null;
	return theDivRights;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was caled and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_DiversionRight d = (StateMod_DiversionRight)_original;
	super.restoreOriginal();

	_irtem = d._irtem;
	_dcrdiv = d._dcrdiv;

	_isClone = false;
	_original = null;
}

/**
Set the decreed amount.
*/
public void setDcrdiv(double dcrdiv) {
	if (dcrdiv != _dcrdiv) {
		_dcrdiv = dcrdiv;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_DIVERSION_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount.
*/
public void setDcrdiv(Double dcrdiv) {
	setDcrdiv(dcrdiv.doubleValue());
}

/**
Set the decreed amount.
*/
public void setDcrdiv(String dcrdiv) {
	if (dcrdiv == null) {
		return;
	}
	setDcrdiv(StringUtil.atod(dcrdiv.trim()));
}

/**
Set the administration number.
*/
public void setIrtem(String irtem) {
	if (irtem == null) {
		return;
	}
	if (!irtem.equals(_irtem)) {
		_irtem = irtem.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_DIVERSION_RIGHTS, true);
		}
	}
}

/**
Writes a diversion rights file.
@param infile the original file
@param outfile the new file to write
@param theRights a Vector of StateMod_DiversionRight objects to right
@param newComments new comments to add to the header
@throws Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
Vector theRights, String[] newComments)
throws Exception {
	writeStateModFile(infile, outfile, theRights, newComments,false);
}

/**
Writes a diversion rights file.
@param infile the original file
@param outfile the new file to write
@param theRights a Vector of StateMod_DiversionRight objects to right
@param newComments new comments to add to the header
@param useOldAdminNumFormat whether to use the old admin num format or not
@throws Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
Vector theRights, String[] newComments, boolean useOldAdminNumFormat)
throws Exception {
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	String routine = "StateMod_DiversionRight.writeStateModFile";
	Message.printStatus(2, routine, "Print diversion rights to: " +
		outfile);

	try {	
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, comment_str, ignore_comment_str, 0);

	String iline;
	String cmnt = "#>";
	String format_0 = null;
	if (useOldAdminNumFormat) {
		format_0 = "%-12.12s%-24.24s%-12.12s%-12.12s    %8.2F%8d";
	}
	else {	
		format_0 = "%-12.12s%-24.24s%-12.12s%16.16s%8.2F%8d";
	}
	StateMod_DiversionRight right = null;
	Vector v = new Vector(6);

		// print out the nonpermanent header
		out.println(cmnt);
		out.println(cmnt
 			+"***************************************************");
		out.println(cmnt + " Direct Diversion Rights File");
		out.println(cmnt);
		out.println(cmnt
			+ "     format:  (a12, a24, a12, f16.5, f8.2, i8)");
		out.println(cmnt);
		out.println(cmnt
			+ "     ID       cidvri:  Diversion right ID ");
		out.println(cmnt
			+ "     Name      named:  Diversion right name");
		out.println(cmnt
			+ "     Struct    cgoto:  Direct Diversion "
			+ "Structure ID associated with this right");
		out.println(cmnt
			+ "     Admin #   irtem:  Administration number");
		out.println(cmnt
			+ "                       (small is senior).");
		out.println(cmnt
			+ "     Decree   dcrdiv:  Decreed amount (cfs)");
		out.println(cmnt
			+ "     On/Off   idvrsw:  Switch 0 = off, 1 = on");
		out.println(cmnt
			+ "                       YYYY = on for years >= " +
			"YYYY.");
		out.println(cmnt
			+ "                       -YYYY = off for years > " +
			"YYYY.");
		out.println(cmnt);
		out.println(cmnt
			+ "   ID            Name              Struct     "
			+ "       Admin #   Decree  On/Off");
		out.println(cmnt + "EndHeader");
		out.println(cmnt
			+ "---------eb----------------------eb----------e"
			+ "b--------------eb------eb------e");

		int num = 0;
		if (theRights != null) {
			num = theRights.size();
		}
		for (int i = 0; i < num; i++) {
			right = (StateMod_DiversionRight)theRights.elementAt(i);
			if (right == null) {
				continue;
			}
			v.removeAllElements();
			v.addElement(right.getID());
			v.addElement(right.getName());
			v.addElement(right.getCgoto());
			v.addElement(right.getIrtem());
			v.addElement(new Double(right.getDcrdiv()));
			v.addElement(new Integer(right.getSwitch()));
			iline = StringUtil.formatString(v, format_0);
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
			out.flush();
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
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _irtem + ", " + _dcrdiv;
}

/**
Writes a Vector of StateMod_Diversion objects to a list file.  A header is 
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
	fields.add("StationID");
	fields.add("AdministrationNumber");
	fields.add("Decree");
	fields.add("OnOff");

	int fieldCount = fields.size();
	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_DIVERSION_RIGHTS;
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
	StateMod_DiversionRight right = null;
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
			right = (StateMod_DiversionRight)data.elementAt(i);
			
			line[0] = StringUtil.formatString(right.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(right.getIrtem(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrdiv(), 
				formats[4]).trim();
			line[5] = StringUtil.formatString(right.getSwitch(), 
				formats[5]).trim();

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

}
