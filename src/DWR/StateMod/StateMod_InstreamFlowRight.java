//------------------------------------------------------------------------------
// StateMod_InstreamFlowRight - Derived from SMData class
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 08 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 21 Dec 1998	CEN, RTi		Added throws IOException to
//					read/write routines.
// 15 Feb 2001	Steven A. Malers, RTi	Update header for current documentation.
//					Update IO to IOUtil.  Add finalize()and
//					make sure all data are initialized.  Add
//					more Javadoc.  Alphabetize methods.  Add
//					checks for null data to prevent errors.
//					Set unused variables to null.
// 02 Mar 2001	SAM, RTi		Ray says to use F16.0 for rights and
//					get rid of the 4x.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTI	Renamed from StateMod_InstreamFlowRight 
//					to StateMod_InstreamFlowRight
// 2003-06-10	JTS, RTi		* Folded dumpInstreaFlowRightsFile()
//					  into writeInstreamFlowRightsFile()
//					* Renamed parseInstreamFlowRightsFile()
//					  to readInstreamFlowRightsFile()
//					* Renamed writeInstreamFlowRightsFile()
//					  to writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readInstreamFlowRightsFile()
//					readStateModFile()
// 2003-08-03	SAM, RTi		Change isDirty() back to setDirty().
// 2003-08-28	SAM, RTi		* Do not use linked list since
//					  StateMod_InstreamFlow has a Vector of
//					  rights.
//					* Call setDirty() on individual objects
//					  in addition to the data set component.
//					* Clean up Javadoc and parameter names.
// 2003-10-13	JTS, RTi		* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
// 					* Added equals(Vector, Vector)
// 2003-10-15	JTS, RTi		* Revised the clone() code.
//					* Added toString().
// 2004-07-08	SAM, RTi		* When writing, adjust paths using
//					  working directory.
//					* Overload the constructor to allow
//					  initializing to default values or
//					  missing.
// 2005-03-13	SAM, RTi		* Expand header to explain switch
//					  better.
// 2005-03-31	JTS, RTi		Added createBackup().
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2007-04-12	Kurt Tometich, RTi		Added checkComponentData() and
//									getDataHeader() methods for check
//									file and data check support.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// 2007-05-16	SAM, RTi		Implement StateMod_Right interface.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This StateMod_InstreamFlowRight class holds information for StateMod instream flow rights.
*/
public class StateMod_InstreamFlowRight extends StateMod_Data 
implements Cloneable, Comparable<StateMod_Data>, StateMod_ComponentValidator, StateMod_Right {

/**
Administration number.  The value is stored as a string to allow exact
representation of the administration number, without any roundoff or precision issues.
*/
protected String _irtem;	

/**
Decreed amount
*/
protected double _dcrifr;
	
/**
Construct and initialize to default values.
*/
public StateMod_InstreamFlowRight() {
	this ( true );
}

/**
Constructor.
@param initialize_defaults If true, initialize to default values, suitable for
creating instances in the StateMod GUI.  If false, initialize to missing,
suitable for use with StateDMI.
*/
public StateMod_InstreamFlowRight ( boolean initialize_defaults )
{	super();
	initialize ( initialize_defaults );
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_InstreamFlowRight right = (StateMod_InstreamFlowRight)super.clone();
	right._isClone = true;
	return right;
}

/**
Creates a copy of the object for later use in checking to see if it was changed in a GUI.
*/
public void createBackup() {
	_original = (StateMod_InstreamFlowRight)clone();
	((StateMod_InstreamFlowRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by irtem and dcrifr, in that order.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_InstreamFlowRight right = (StateMod_InstreamFlowRight)data;

	res = _irtem.compareTo(right.getIrtem());
	if (res == 0) {
		double dcrifr = right.getDcrifr();
		if (dcrifr == _dcrifr) {
			return 0;
		}
		else if (_dcrifr < dcrifr) {
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
Compare two rights lists and see if they are the same.
@param v1 the first list of StateMod_InstreamFlowRights to check.  Cannot be null.
@param v2 the second list of StateMod_InstreamFlowRights to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List<StateMod_InstreamFlowRight> v1, List<StateMod_InstreamFlowRight> v2) {
	String routine = "StateMod_InstreamFlowRight.equals(Vector, Vector)";
	StateMod_InstreamFlowRight r1;	
	StateMod_InstreamFlowRight r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(1, routine, "Lists are different sizes");
		return false;
	}
	else {
		// sort the Vectors and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		Message.printStatus(2, routine, "Lists are of size: " + size);
		List<StateMod_InstreamFlowRight> v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List<StateMod_InstreamFlowRight> v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(2, routine, "Lists have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = v1Sort.get(i);	
			r2 = v2Sort.get(i);	
			Message.printStatus(1, routine, r1.toString());
			Message.printStatus(1, routine, r2.toString());
			Message.printStatus(1, routine, "Element " + i + " comparison: " + r1.compareTo(r2));
			if (r1.compareTo(r2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two instream flow rights are equal.  Strings are compared with case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_InstreamFlowRight right) {
	if (!super.equals(right)) {
	 	return false;
	}
	if ( right._irtem.equals(_irtem) && right._dcrifr == _dcrifr) {
		return true;
	}
	return false;
}

/**
Free memory before garbage collection.
*/
protected void finalize()
throws Throwable {
	_irtem = null;
	super.finalize();
}

/**
Return the administration number, as per the generic interface.
@return the administration number, as a String to protect from roundoff.
*/
public String getAdministrationNumber ()
{	return getIrtem();
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
*/
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-16 When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Return the decreed amount.
*/
public double getDcrifr() {
	return _dcrifr;
}

/**
Return the decree, as per the generic interface.
@return the decree, in the units of the data.
*/
public double getDecree()
{	return getDcrifr();
}

// TODO SAM 2007-05-15 Need to evaluate whether should be hard-coded.
/**
Return the decree units.
@return the decree units.
*/
public String getDecreeUnits()
{	return "CFS";
}

/**
Return the right identifier, as per the generic interface.
@return the right identifier.
*/
public String getIdentifier()
{	return getID();
}

/**
Retrieve the administration number.
*/
public String getIrtem() {
	return _irtem;
}

/**
Return the right location identifier, as per the generic interface.
@return the right location identifier (location where right applies).
*/
public String getLocationIdentifier()
{	return getCgoto();
}

/**
Initialize data.
@param initialize_defaults If true, initialize to default values, suitable for
creating instances in the StateMod GUI.  If false, initialize to missing, suitable for use with StateDMI.
*/
private void initialize ( boolean initialize_defaults )
{	_smdata_type = StateMod_DataSet.COMP_INSTREAM_RIGHTS;
	_irtem = "";
	if ( initialize_defaults ) {
		_dcrifr = 0;
	}
	else {
		_dcrifr = StateMod_Util.MISSING_DOUBLE;
	}
}

/**
Determine whether a file is an instream flow right file.  Currently true is returned
if the file extension is ".ifr".
@param filename Name of the file being checked.
@return true if the file is a StateMod instream flow right file.
*/
public static boolean isInstreamFlowRightFile ( String filename )
{	if ( filename.toUpperCase().endsWith(".IFR")) {
		return true;
	}
	return false;
}

/**
Read instream flow rights information in and store in a list.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List<StateMod_InstreamFlowRight> readStateModFile(String filename)
throws Exception {
	String routine ="StateMod_InstreamFlowRight.readStateModFile";
	List<StateMod_InstreamFlowRight> theInsfRights = new Vector<StateMod_InstreamFlowRight>();
	int format_0[] = {
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_INTEGER };
	int format_0w[] = {
		12,
		24,
		12,
		16,
		8,
		8 };
	String iline;
	StateMod_InstreamFlowRight aRight = null;
	List<Object> v = new Vector<Object>(6);

	Message.printStatus(1, routine, "Reading Instream Flow Rights File: " + filename);
	BufferedReader in = null;
	try {	
		in = new BufferedReader( new FileReader(filename));
		while ((iline = in.readLine()) != null) {
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()== 0) {
				continue;
			}

			aRight = new StateMod_InstreamFlowRight();

			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aRight.setID(((String)v.get(0)).trim()); 
			aRight.setName(((String)v.get(1)).trim()); 
			aRight.setCgoto(((String)v.get(2)).trim()); 
			aRight.setIrtem(((String)v.get(3)).trim()); 
			aRight.setDcrifr((Double)v.get(4));
			aRight.setSwitch((Integer)v.get(5));

			theInsfRights.add(aRight);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if ( in != null ) {
			in.close();
		}
	}
	return theInsfRights;
}

/**
Set the decreed amount.
*/
public void setDcrifr(double dcrifr) {
	if (dcrifr != _dcrifr) {
		_dcrifr = dcrifr;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount.
*/
public void setDcrifr(Double dcrifr) {
	setDcrifr(dcrifr.doubleValue());
}

/**
Set the decreed amount.
*/
public void setDcrifr(String dcrifr) {
	if (dcrifr == null) {
		return;
	}
	setDcrifr(StringUtil.atod(dcrifr.trim()));
}

/**
Set the decree, as per the generic interface.
@param decree, in the units of the data.
*/
public void setDecree( double decree )
{	setDcrifr(decree);
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
			_dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_RIGHTS, true);
		}
	}
}


/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _irtem + ", " + _dcrifr;
}

/**
@param dataset StateMod dataset object.
@return validation results.
 */
public StateMod_ComponentValidation validateComponent ( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String cgoto = getCgoto();
	String irtem = getIrtem();
	double dcrifr = getDcrifr();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right identifier is blank.",
			"Specify a instream flow right identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id + "\" name is blank.",
			"Specify an instream flow right name to clarify data.") );
	}
	if ( StateMod_Util.isMissing(cgoto) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id +
			"\" station ID is blank.",
			"Specify an instream flow station to associate with the instream flow right.") );
	}
	else {
		// Verify that the instream flow station is in the data set, if the network is available
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_INSTREAM_STATIONS);
		List ifsList = (List)comp2.getData();
		if ( (ifsList != null) && (ifsList.size() > 0) ) {
			if ( StateMod_Util.indexOf(ifsList, cgoto) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id +
					"\" associated instream flow (" + cgoto + ") is not found in the list of instream flow stations.",
					"Specify a valid instream flow station ID to associate with the instream flow right.") );
			}
		}
	}
	if ( StateMod_Util.isMissing(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id +
			"\" administration number is blank.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else if ( !StringUtil.isDouble(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id +
			"\" administration number (" + irtem + ") is invalid.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else {
		double irtemd = Double.parseDouble(irtem);
		if ( irtemd < 0 ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id +
				"\" administration number (" + irtem + ") is invalid.",
				"Specify an administration number NNNNN.NNNNN.") );
		}
	}
	if ( !(dcrifr >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow right \"" + id + "\" decree (" +
			StringUtil.formatString(dcrifr,"%.2f") + ") is invalid.",
			"Specify the decree as a number >= 0.") );
	}
	return validation;
}

/**
Print instream flow rights information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theInsfRights list of instream flow rights to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
		List<StateMod_InstreamFlowRight> theInsfRights, List<String> newComments)
throws Exception {
	writeStateModFile(infile, outfile, theInsfRights, newComments, false);
}

/**
Print instream flow rights information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theInsfRights list of instream flow rights to print
@param newComments addition comments which should be included in history
@param oldAdminNumFormat whether to use the old admin num format or not
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
		List<StateMod_InstreamFlowRight> theInsfRights, List<String> newComments, boolean oldAdminNumFormat)
throws Exception {
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	String routine = "StateMod_InstreamFlowRight.writeStateModFile";
	if (Message.isDebugOn)
		Message.printDebug(2, routine, "Write instream flow rights to " + outfile);

	try {	
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		String iline;
		String cmnt = "#>";
		StateMod_InstreamFlowRight right;
		List<Object> v = new Vector<Object>(6);
		String format_0 = null;
		if (oldAdminNumFormat) {
			format_0 = "%-12.12s%-24.24s%-12.12s    %-12.12s%8.2F%8d";
		}
		else {	
			format_0 = "%-12.12s%-24.24s%-12.12s%16.16s%8.2F%8d";
		}
	
		out.println(cmnt);
		out.println(cmnt + " *******************************************************");
		out.println(cmnt + "  StateMod Instream Flow Right file ");
		out.println(cmnt);
		out.println(cmnt + "       format:  (a12, a24, a12, F16.5, f8.2, i8)");
		out.println(cmnt);
		out.println(cmnt + "  ID         cifrri:      Instream flow right ID");
		out.println(cmnt + "  Name        namei:      Instream flow right name");
		out.println(cmnt + "  Structure   cgoto:      Instream flow station associated with the right");
		out.println(cmnt + "  Admin#      irtem:      Priority or Administration number");
		out.println(cmnt + "                          (small is senior).");
		out.println(cmnt + "  Decree     dcrifr:      Decreed amount (cfs)");
		out.println(cmnt + "  On/Off     iifrsw:      Switch 0 = off, 1 = on");
		out.println(cmnt + "                          YYYY = on for years >= YYYY" );
		out.println(cmnt + "                          -YYYY = off for years > YYYY" );
		out.println(cmnt);
		out.println(cmnt + "   ID           Name               Structure        Admin#     Decree On/Off");
		out.println(cmnt + "---------eb----------------------eb----------exxxxb----------eb------eb------e");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int num = 0;
		if (theInsfRights != null) {
			num = theInsfRights.size();
		}
		for (int i = 0; i < num; i++) {
			right =(StateMod_InstreamFlowRight)theInsfRights.get(i);
			if (right == null) {
				continue;
			}
			v.clear();
			v.add(right.getID());
			v.add(right.getName());
			v.add(right.getCgoto());
			v.add(right.getIrtem());
			v.add(new Double(right.getDcrifr()));
			v.add(new Integer(right.getSwitch()));
			iline = StringUtil.formatString(v, format_0);
			out.println(iline);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if ( out != null ) {
			out.flush();
			out.close();
		}
	}
}

/**
Writes a list of StateMod_InstreamFlowRight objects to a list file.  A header 
is printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments comments to add to the the file header.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List<StateMod_InstreamFlowRight> data,
	List<String> newComments ) 
throws Exception {
	String routine = "StateMod_IntreamFlowRight.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("ID");
	fields.add("Name");
	fields.add("StationID");
	fields.add("AdministrationNumber");
	fields.add("Decree");
	fields.add("OnOff");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_INSTREAM_RIGHTS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	StateMod_InstreamFlowRight right = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  Do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector<String>();
		}
		else {
			newComments2 = new Vector<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod instream flow rights as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			right = data.get(i);
			
			line[0] = StringUtil.formatString(right.getID(),formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(),formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(),formats[2]).trim();
			line[3] = StringUtil.formatString(right.getIrtem(),formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrifr(),formats[4]).trim();
			line[5] = StringUtil.formatString(right.getSwitch(),formats[5]).trim();

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if (j > 0) {
					buffer.append(delimiter);
				}
				if (line[j].indexOf(delimiter) > -1) {
					line[j] = "\"" + line[j] + "\"";
				}
				buffer.append(line[j]);
			}

			out.println(buffer.toString());
		}
	}
	catch (Exception e) {
		Message.printWarning( 3, routine, e);
		throw e;
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

}