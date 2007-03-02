//------------------------------------------------------------------------------
// StateMod_WellRight - Derived from StateMod_Data class
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 01 Feb 1999	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 19 Mar 2000	Steven A. Malers, RTi	Change some data members and methods to
//					agree with recent StateMod documentation
//						dcrwel -> dcrdivw
//						rtem -> irtem
// 18 Feb 2001	SAM, RTi		Code review.  Add finalize().  Handle
//					nulls and set unused variables to null.
//					Alphabetize methods.  Add ability to
//					print old style(left-justified)rights
//					but change default to right-justified.
//					Change IO to IOUtil.  Remove unneeded
//					debug messages.
// 02 Mar 2001	SAM, RTi		Ray says to use F16.5 for rights and
//					get rid of the 4x.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMWellRights to 
//					StateMod_WellRight
// 2003-06-10	JTS, RTi		* Folded dumpWellRightsFile() into
//					  writeWellRightsFile()
//					* Renamed parseWellRightsFile() to
//					  readWellRightsFile()
// 2003-06-23	JTS, RTi		Renamed writeWellRightsFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readWellRightsFile() to
//					readStateModFile()
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-28	SAM, RTi		Remove use of linked list since
//					StateMod_Well maintains a Vector of
//					rights.
// 2003-10-09	JTS, RTi		* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
// 					* Added equals(Vector, Vector)
// 2004-09-16	SAM, RTi		* Change so that the read and write
//					  methods adjust the file path using the
//					  working directory.
// 2005-01-17	JTS, RTi		* Added createBackup().
//					* Added restoreOriginal().
// 2005-03-10	SAM, RTi		* Clarify the header some for admin #
//					  and switch.
// 2005-03-28	JTS, RTi		Corrected wrong class name in 
//					createBackup().
// 2005-04-18	JTS, RTi		Added writeListFile().
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

/**
This class provides stores all the information associated with a well right.
*/

public class StateMod_WellRight extends StateMod_Data
implements Cloneable, Comparable {

private String 	_irtem;		// administration number
private double	_dcrdivw;	// decreed amount
	
/**
Constructor
*/
public StateMod_WellRight() {
	super();
	initialize();
}

/**
Clean up before garbage collection.
*/
protected void finalize()throws Throwable {
	_irtem = null;
	super.finalize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_WellRight right = (StateMod_WellRight)super.clone();
	right._irtem = new String(_irtem);
	right._dcrdivw = _dcrdivw;
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

	StateMod_WellRight right = (StateMod_WellRight)o;

	res = _irtem.compareTo(right._irtem);
	if (res != 0) {
		return res;
	}

	if (_dcrdivw < right._dcrdivw) {
		return -1;
	}
	else if (_dcrdivw > right._dcrdivw) {
		return 1;
	}

	return 0;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_WellRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_WellRight s to check.  Can not
be null.
@param v2 the second Vector of StateMod_WellRight s to check.  Can not
be null.
@return true if they are the same, false if not.
*/
public static boolean equals(Vector v1, Vector v2) {
	String routine = "StateMod_WellRight.equals(Vector, Vector)";
	StateMod_WellRight r1;	
	StateMod_WellRight r2;	
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
			r1 = (StateMod_WellRight)v1Sort.elementAt(i);	
			r2 = (StateMod_WellRight)v2Sort.elementAt(i);	
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
public boolean equals(StateMod_WellRight right) {
	 if (!super.equals(right)) {
	 	return false;
	}
	
	if (	right._irtem.equals(_irtem) 
		&& right._dcrdivw == _dcrdivw) {
		return true;
	}
	return false;
}

/**
@return the decreed amount(cfs)
*/
public double getDcrdivw() {
	return _dcrdivw;
}

/**
@return the administration number
*/
public String getIrtem() {
	return _irtem;
}

private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_WELL_RIGHTS;
	_irtem = "99999";
	_dcrdivw = 0;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_WellRight right = (StateMod_WellRight)_original;
	super.restoreOriginal();

	_smdata_type = right._smdata_type;
	_irtem = right._irtem;
	_dcrdivw = right._dcrdivw;
	_isClone = false;
	_original = null;
}

/**
Read the well rights file.  Add the new rights onto the vector of well
rights passed in the parameter list.
@param filename name of file containing well rights
@return Vector of well rights
*/
public static Vector readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_WellRight.readStateModFile";
	Vector theWellRights = new Vector();
	int [] format_0 = {	StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER };
	int [] format_0w = {	12,
				24,
				12,
				16,
				8,
				8 };
	String iline = null;
	Vector v = new Vector(6);
	BufferedReader in = null;
	StateMod_WellRight aRight = null;

	Message.printStatus(1, routine, "Reading well rights file: "
		+ filename);

	try {	in = new BufferedReader(new FileReader(
		IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine()) != null) {
			// check for comments
			if (iline.startsWith("#") || 
				iline.trim().length() == 0) {
				continue;
			}
			
			aRight = new StateMod_WellRight();

			if (Message.isDebugOn) {
				Message.printDebug(50, routine , 
				"iline: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aRight.setID(((String)v.elementAt(0)).trim());
			aRight.setName(((String)v.elementAt(1)).trim());
			aRight.setCgoto(((String)v.elementAt(2)).trim());
			aRight.setIrtem(((String)v.elementAt(3)).trim());
			aRight.setDcrdivw((Double)v.elementAt(4));
			aRight.setSwitch((Integer)v.elementAt(5));
			theWellRights.addElement(aRight);
		}

	} 
	catch(Exception e) {
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
	return theWellRights;
}

/**
Set the decreed amount(cfs)
@param dcrdivw decreed amount for this right
*/
public void setDcrdivw(double dcrdivw) {
	if (dcrdivw != _dcrdivw) {
		_dcrdivw = dcrdivw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount(cfs)
@param dcrdivw decreed amount for this right
*/
public void setDcrdivw(Double dcrdivw) {
	if (dcrdivw == null)
		return;
	setDcrdivw(dcrdivw.doubleValue());
}

/**
Set the decreed amount(cfs)
@param dcrdivw decreed amount for this right
*/
public void setDcrdivw(String dcrdivw) {
	if (dcrdivw == null) {
		return;
	}
	setDcrdivw(StringUtil.atod(dcrdivw.trim()));
}

/**
Set the administration number
@param irtem admin number of right
*/
public void setIrtem(String irtem) {
	if (irtem == null) {
		return;
	}
	if (!irtem.equals(_irtem)) {
		_irtem = irtem.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Write the well rights file.  The comments from the previous
rights file are transferred into the next one.  Also, a history is maintained
and printed in the header fo the file.  Additional comments can be added
through the new_comments parameter.
@param infile name of file to retrieve previous comments and history from
@param outfile name of output file to print to
@param theRights vector of rights to print
@param new_comments additional comments to print to the comment section
*/
public static void writeStateModFile(String infile, String outfile,
Vector theRights, String[]new_comments)
throws Exception {
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	String routine = "StateMod_WellRight.writeStateModFile";

	if (outfile == null) {
		String msg = "Unable to print to null filename";
		Message.printWarning(2, routine, msg);
		throw new Exception(msg);
	}
			
	Message.printStatus(2, routine, "Print well rights to: " +
		outfile);

	try {	
	out = IOUtil.processFileHeaders(
		IOUtil.getPathUsingWorkingDir(infile),
		IOUtil.getPathUsingWorkingDir(outfile), 
		new_comments, comment_str, ignore_comment_str, 0);

	String iline = null;
	String cmnt = "#>";
	String format_0 = "%-12.12s%-24.24s%-12.12s%16.16s%8.2F%8d";
	StateMod_WellRight right = null;
	Vector v = new Vector(6);

		out.println(cmnt);
		out.println(cmnt 
 			+"***************************************************");
		out.println(cmnt + "  Well Right File");
		out.println(cmnt);
		out.println(cmnt 
			+ "  Format:  (a12, a24, a12, f16.5, f8.2, i8)");
		out.println(cmnt);
		out.println(cmnt 
			+ "     ID        cidvi:  Well right ID ");
		out.println(cmnt 
			+ "     Name     cnamew:  Well right name");
		out.println(cmnt 
			+ "     Struct    cgoto:  Well " 
			+ "Structure ID associated with this right");
		out.println(cmnt 
			+ "     Admin #   irtem:  Administration number" );
		out.println(cmnt 
			+ "                       (priority, small is senior)");
		out.println(cmnt 
			+ "     Decree  dcrdivw:  Well right (cfs)");
		out.println(cmnt 
			+ "     On/Off  idvrsww:  Switch 0 = off, 1 = on");
		out.println(cmnt 
			+ "                       YYYY = on for years >= YYYY");
		out.println(cmnt 
			+ "                       -YYYY = off for years > " +
			"YYYY" );
		out.println(cmnt);
		out.println(cmnt 
			+ "   ID               Name             Struct   " 
			+ "       Admin #   Decree  On/Off");
		out.println(cmnt 
			+ "---------eb----------------------eb----------e" 
			+ "b--------------eb------eb------e");
		out.println(cmnt);
		out.println(cmnt + "EndHeader");
		out.println(cmnt);

		int num = 0;
		if (theRights != null) {
			num = theRights.size();
		}

		for (int i = 0; i < num; i++) {
			right = (StateMod_WellRight)theRights.elementAt(i);
			if (right == null) {
				continue;
			}

			v.removeAllElements();
			v.addElement(right.getID());
			v.addElement(right.getName());
			v.addElement(right.getCgoto());
			v.addElement(right.getIrtem());
			v.addElement(new Double(right.getDcrdivw()));
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
	catch(Exception e) {
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
Writes a Vector of StateMod_WellRight objects to a list file.  A header is 
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
	int comp = StateMod_DataSet.COMP_WELL_RIGHTS;
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
	PrintWriter out = null;
	StateMod_WellRight right = null;
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
	String[] line = new String[fieldCount];
	String[] newComments = null;
	StringBuffer buffer = new StringBuffer();
	
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
			right = (StateMod_WellRight)data.elementAt(i);
			
			line[0] = StringUtil.formatString(right.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(right.getIrtem(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrdivw(), 
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

} // End StateMod_WellRight
