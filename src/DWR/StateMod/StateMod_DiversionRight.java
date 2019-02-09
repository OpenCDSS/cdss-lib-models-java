// StateMod_DiversionRight - class to store diversion right

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

public class StateMod_DiversionRight extends StateMod_Data 
implements Cloneable, Comparable<StateMod_Data>, StateMod_ComponentValidator, StateMod_Right {

/**
Administration number.
*/
protected String _irtem;	

/**
Decreed amount
*/
protected double _dcrdiv;

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
order from the StateMod_Data variables, and then by irtem and dcrdiv, in that order.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_DiversionRight right = (StateMod_DiversionRight)data;

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
Creates a copy of the object for later use in checking to see if it was changed in a GUI.
*/
public void createBackup() {
	_original = (StateMod_DiversionRight)clone();
	((StateMod_DiversionRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights list and see if they are the same.
@param v1 the first list of StateMod_DiversionRight to check.  Cannot be null.
@param v2 the second list of StateMod_DiversionRight to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List<StateMod_DiversionRight> v1, List<StateMod_DiversionRight> v2) {
	String routine = "StateMod_DiversionRight.equals(Vector, Vector)";
	StateMod_DiversionRight r1;	
	StateMod_DiversionRight r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(1, routine, "Lists are different sizes");
		return false;
	}
	else {
		// Sort the lists and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		//Message.printStatus(2, routine, "Lists are of size: " + size);
		List<StateMod_DiversionRight> v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List<StateMod_DiversionRight> v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		//Message.printStatus(2, routine, "Lists have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = v1Sort.get(i);	
			r2 = v2Sort.get(i);	
			//Message.printStatus(2, routine, r1.toString());
			//Message.printStatus(2, routine, r2.toString());
			//Message.printStatus(2, routine, "Element " + i + " comparison: " + r1.compareTo(r2));
			if (r1.compareTo(r2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two diversion rights are equal.  Strings are compared with case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_DiversionRight right) {
	if (!super.equals(right)) {
	 	return false;
	}
	if ( right._irtem.equals(_irtem) && right._dcrdiv == _dcrdiv) {
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
Return the administration number, as per the generic interface.
@return the administration number, as a String to protect from roundoff.
*/
public String getAdministrationNumber ()
{	return getIrtem();
}

/**
Returns the column headers for the specific data checked.
@return List of column headers.
 */
public static String[] getDataHeader()
{
	return new String[] {
		"Num",
		"Right ID",
		"Right Name" };
}

/**
Return the decree, as per the generic interface.
@return the decree, in the units of the data.
*/
public double getDecree()
{	return getDcrdiv();
}

//TODO SAM 2007-05-16 Need to evaluate whether should be hard-coded.
/**
Return the decree units.
@return the decree units.
*/
public String getDecreeUnits()
{	return "CFS";
}

/**
Return the decreed amount.
*/
public double getDcrdiv() {
	return _dcrdiv;
}

/**
Return the right identifier, as per the generic interface.
@return the right identifier.
*/
public String getIdentifier()
{	return getID();
}

/**
Return a list of on/off switch option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and "0 - Off", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getIdvrswChoices ( boolean include_notes )
{	List<String> v = new Vector<String>(2);
	v.add ( "0 - Off" );	// Possible options are listed here.
	v.add ( "1 - On" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i, StringUtil.getToken(v.get(i), " ", 0, 0) );
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
	else {
		return "1";
	}
}

/**
Return the administration number.
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
Initializes data members.
*/
private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_DIVERSION_RIGHTS;
	_irtem = "99999";
	_dcrdiv = 0;
}


/**
Determine whether a file is a diversion right file.  Currently true is returned
if the file extension is ".ddr".
@param filename Name of the file being checked.
@return true if the file is a StateMod diversion right file.
*/
public static boolean isDiversionRightFile ( String filename )
{	if ( filename.toUpperCase().endsWith(".DDR")) {
		return true;
	}
	return false;
}

/**
Parses the diversion rights file and returns a Vector of StateMod_DiversionRight objects.
@param filename the diversion rights file to parse.
@return a Vector of StateMod_DiversionRight objects.
@throws Exception if an error occurs
*/
public static List<StateMod_DiversionRight> readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_DiversionRight.readStateModFile";
	List<StateMod_DiversionRight> theDivRights = new Vector<StateMod_DiversionRight> ();

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
	String iline = null;
	List<Object> v = new Vector<Object>(6);
	BufferedReader in = null;
	StateMod_DiversionRight aRight = null;

	Message.printStatus(2, routine, "Reading diversion rights file: " + filename);

	try {	
		in = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine())!= null) {
			// check for comments
			if (iline.startsWith("#")||iline.trim().length()== 0) {
				continue;
			}
			
			aRight = new StateMod_DiversionRight();

			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "iline: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aRight.setID(((String)v.get(0)).trim()); 
			aRight.setName(((String)v.get(1)).trim());
			aRight.setCgoto(((String)v.get(2)).trim());
			aRight.setIrtem(((String)v.get(3)).trim());
			aRight.setDcrdiv((Double)v.get(4));
			aRight.setSwitch((Integer)v.get(5));
			// Mark as clean because set methods may have marked dirty...
			aRight.setDirty ( false );
			theDivRights.add(aRight);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	return theDivRights;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
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
Set the decree, as per the generic interface.
@param decree decree, in the units of the data.
*/
public void setDecree( double decree )
{	setDcrdiv(decree);
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
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _irtem + ", " + _dcrdiv;
}

public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset )
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String cgoto = getCgoto();
	String irtem = getIrtem();
	double dcrdiv = getDcrdiv();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right identifier is blank.",
			"Specify a diversion right identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id + "\" name is blank.",
			"Specify a diversion right name to clarify data.") );
	}
	if ( StateMod_Util.isMissing(cgoto) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id +
			"\" diversion station ID is blank.",
			"Specify a diversion station to associate the diversion right.") );
	}
	else {
		// Verify that the diversion station is in the data set, if the network is available
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_DIVERSION_STATIONS);
		List ddsList = (List)comp2.getData();
		if ( (ddsList != null) && (ddsList.size() > 0) ) {
			if ( StateMod_Util.indexOf(ddsList, cgoto) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id +
					"\" associated diversion (" + cgoto + ") is not found in the list of diversion stations.",
					"Specify a valid diversion station ID to associate the diversion right.") );
			}
		}
	}
	if ( StateMod_Util.isMissing(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id +
			"\" administration number is blank.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else if ( !StringUtil.isDouble(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id +
			"\" administration number (" + irtem + ") is invalid.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else {
		double irtemd = Double.parseDouble(irtem);
		if ( irtemd < 0 ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id +
				"\" administration number (" + irtem + ") is invalid.",
				"Specify an administration number NNNNN.NNNNN.") );
		}
	}
	if ( !(dcrdiv >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Diversion right \"" + id + "\" decree (" +
			StringUtil.formatString(dcrdiv,"%.2f") + ") is invalid.",
			"Specify the decree as a number >= 0.") );
	}
	return validation;
}

/**
Writes a diversion rights file.
@param infile the original file
@param outfile the new file to write
@param theRights a list of StateMod_DiversionRight objects to right
@param newComments new comments to add to the header
@throws Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
		List<StateMod_DiversionRight> theRights, List<String> newComments)
throws Exception {
	writeStateModFile(infile, outfile, theRights, newComments, false);
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
		List<StateMod_DiversionRight> theRights, List<String> newComments, boolean useOldAdminNumFormat)
throws Exception {
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	String routine = "StateMod_DiversionRight.writeStateModFile";
	Message.printStatus(2, routine, "Writing diversion rights to: " + outfile);

	try {	
		out = IOUtil.processFileHeaders( IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);

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
		List<Object> v = new Vector<Object>(6);

		// print out the non-permanent header
		out.println(cmnt);
		out.println(cmnt+ "***************************************************");
		out.println(cmnt + " StateMod Direct Diversion Rights File");
		out.println(cmnt);
		out.println(cmnt + "     format:  (a12, a24, a12, f16.5, f8.2, i8)");
		out.println(cmnt);
		out.println(cmnt + "     ID       cidvri:  Diversion right ID ");
		out.println(cmnt + "     Name      named:  Diversion right name");
		out.println(cmnt + "     Struct    cgoto:  Direct Diversion Structure ID associated with this right");
		out.println(cmnt + "     Admin #   irtem:  Administration number");
		out.println(cmnt + "                       (small is senior).");
		out.println(cmnt + "     Decree   dcrdiv:  Decreed amount (cfs)");
		out.println(cmnt + "     On/Off   idvrsw:  Switch 0 = off, 1 = on");
		out.println(cmnt + "                       YYYY = on for years >= YYYY.");
		out.println(cmnt + "                       -YYYY = off for years > YYYY.");
		out.println(cmnt);
		out.println(cmnt + "   ID            Name              Struct            Admin #   Decree  On/Off");
		out.println(cmnt + "EndHeader");
		out.println(cmnt + "---------eb----------------------eb----------eb--------------eb------eb------e");

		int num = 0;
		if (theRights != null) {
			num = theRights.size();
		}
		for (int i = 0; i < num; i++) {
			right = (StateMod_DiversionRight)theRights.get(i);
			if (right == null) {
				continue;
			}
			v.clear();
			v.add(right.getID());
			v.add(right.getName());
			v.add(right.getCgoto());
			v.add(right.getIrtem());
			v.add(new Double(right.getDcrdiv()));
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
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

/**
Writes a list of StateMod_Diversion objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments comments to add at the top of the file (e.g., command file, HydroBase version).
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update,
	List<StateMod_DiversionRight> data, List<String> newComments ) 
throws Exception
{	String routine = "StateMod_DiversionRight.writeListFile";
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
	int comp = StateMod_DataSet.COMP_DIVERSION_RIGHTS;
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
	StateMod_DiversionRight right = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	PrintWriter out = null;

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
		newComments2.add(1,"StateMod diversion rights as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders(oldFile,IOUtil.getPathUsingWorkingDir(filename), 
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
			line[4] = StringUtil.formatString(right.getDcrdiv(),formats[4]).trim();
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
		Message.printWarning(3, routine, e);
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
