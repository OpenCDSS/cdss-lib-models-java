package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class stores Plan (Well Augmentation) data.  The plan ID is stored in the StateMod_Data ID.
*/
public class StateMod_Plan_WellAugmentation extends StateMod_Data 
implements Cloneable, Comparable {

/**
Well right ID.
*/
private String __cistatW;

/**
Well structure ID.
*/
private String __cistatS;

/**
Construct an instance.
*/
public StateMod_Plan_WellAugmentation()
{	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_Plan_WellAugmentation rf = (StateMod_Plan_WellAugmentation)super.clone();
	rf._isClone = true;

	return rf;
}

/**
Compares this object to another object based on the well structure ID and well right ID.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_Plan_WellAugmentation rf = (StateMod_Plan_WellAugmentation)o;
	
	// Strip off trailing "- name" - may be present if comparing in UI

	String cistatS = __cistatS;
	int index = cistatS.indexOf(" - ");
	if (index > 0) {
		cistatS = cistatS.substring(0, index).trim();
	}

	String cistatS2 = rf.__cistatS;
	index = cistatS2.indexOf(" - ");
	if (index > 0) {
		cistatS2 = cistatS2.substring(0, index).trim();
	}

	res = cistatS.compareTo(cistatS2);
	if (res != 0) {
		return res;
	}
	
	String cistatW = __cistatW;
	index = cistatW.indexOf(" - ");
	if (index > 0) {
		cistatW = cistatW.substring(0, index).trim();
	}

	String cistatW2 = rf.__cistatW;
	index = cistatW2.indexOf(" - ");
	if (index > 0) {
		cistatW2 = cistatW2.substring(0, index).trim();
	}

	return cistatW.compareTo(cistatW2);
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_Plan_WellAugmentation)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two return flow lists and see if they are the same.
@param v1 the first list of Plan_WellAugmentation to check.  Cannot be null.
@param v2 the second list of Plan_WellAugmentation to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List<StateMod_Plan_WellAugmentation> v1, List<StateMod_Plan_WellAugmentation> v2) {
	String routine = "StateMod_Plan_WellAugmentation.equals";
	StateMod_Plan_WellAugmentation rf1;	
	StateMod_Plan_WellAugmentation rf2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(2, routine, "Well augmentation lists are different sizes");
		return false;
	}
	else {
		// Sort the lists and compare item-by-item.  Any differences
		// and data will need to be saved back into the data set.
		int size = v1.size();
		//Message.printStatus(2, routine, "Well augmentation lists are of size: " + size);
		List v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		//Message.printStatus(2, routine, "Well augmentation lists have been sorted");
	
		for (int i = 0; i < size; i++) {			
			rf1 = (StateMod_Plan_WellAugmentation)v1Sort.get(i);	
			rf2 = (StateMod_Plan_WellAugmentation)v2Sort.get(i);	
			Message.printStatus(2, routine, rf1.toString());
			Message.printStatus(2, routine, rf2.toString());
			//Message.printStatus(2, routine, "Element " + i + " comparison: " + rf1.compareTo(rf2));
			if (rf1.compareTo(rf2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two return flows are equal.  Strings are compared with case sensitivity.
@param rf the return flow to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_Plan_WellAugmentation rf) {
	if (!super.equals(rf)) {
	 	return false;
	}
	if ( rf.__cistatW.equals(__cistatW) && rf.__cistatS.equals(__cistatS) ) {
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__cistatW = null;
	super.finalize();
}

/**
Retrieve the structure ID.
*/
public String getCistatS() {
	return __cistatS;
}

/**
Return the cistatW.
*/
public String getCistatW() {
	return __cistatW;
}

private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_PLAN_WELL_AUGMENTATION;
	__cistatW = "";
	__cistatS = "";
}

/**
Read return information in and store in a list.
@param filename filename for data file to read
@throws Exception if an error occurs
*/
public static List<StateMod_Plan_WellAugmentation> readStateModFile(String filename )
throws Exception
{	String routine = "StateMod_Plan_WellAugmentation.readStateModFile";
	String iline = null;
	List<String> v = new Vector(9);
	List<StateMod_Plan_WellAugmentation> theWellAugs = new Vector();
	int linecount = 0;
	
	StateMod_Plan_WellAugmentation aWellAug = null;
	BufferedReader in = null;

	Message.printStatus(2, routine, "Reading well augmentation plan file: " + filename);
	int size = 0;
	int errorCount = 0;
	try {	
		in = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine()) != null) {
			++linecount;
			// check for comments
			if (iline.startsWith("#") || (iline.trim().length()== 0) ) {
				// Special dynamic header comments written by software and blank lines - no need to keep
				continue;
			}
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line: " + iline);
			}
			// Break the line using whitespace, while allowing for quoted strings...
			v = StringUtil.breakStringList (
				iline, " \t", StringUtil.DELIM_ALLOW_STRINGS|StringUtil.DELIM_SKIP_BLANKS );
			size = 0;
			if ( v != null ) {
				size = v.size();
			}
			if ( size < 3 ) {
				Message.printStatus ( 2, routine, "Ignoring line " + linecount +
				" not enough data values.  Have " + size + " expecting 3" );
				++errorCount;
				continue;
			}
			// Uncomment if testing...
			//Message.printStatus ( 2, routine, "" + v );

			// Allocate new plan node and set the values
			aWellAug = new StateMod_Plan_WellAugmentation();
			aWellAug.setID(v.get(0).trim()); 
			aWellAug.setName(v.get(0).trim()); // Same as ID
			aWellAug.setCistatW(v.get(1).trim());
			aWellAug.setCistatS(v.get(2).trim());
			if ( v.size() > 3 ) {
				aWellAug.setComment(v.get(3).trim());
			}

			// Set the return to not dirty because it was just initialized...

			aWellAug.setDirty ( false );

			// Add the return to the list of returns
			theWellAugs.add(aWellAug);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, "Error reading line " + linecount + " \"" + iline + "\" (" + e + ")." );
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	if ( errorCount > 0 ) {
		throw new Exception ( "There were " + errorCount + " errors processing the data - refer to log file." );
	}
	return theWellAugs;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_Plan_WellAugmentation rf = (StateMod_Plan_WellAugmentation)_original;
	super.restoreOriginal();

	__cistatW = rf.__cistatW;
	__cistatS = rf.__cistatS;
	_isClone = false;
	_original = null;
}

/**
Set the cistatS.
*/
public void setCistatS(String cistatS) {
	if (cistatS != null) {
		if (!cistatS.equals(__cistatS)) {
			setDirty ( true );
			if ( !_isClone && _dataset != null ) {
				_dataset.setDirty(_smdata_type, true);
			}
			__cistatS = cistatS;
		}
	}
}

/**
Set the cistatW.
*/
public void setCistatW(String cistatW) {
	if (cistatW != null) {
		if (!cistatW.equals(__cistatW)) {
			setDirty ( true );
			if ( !_isClone && _dataset != null ) {
				_dataset.setDirty(_smdata_type, true);
			}
			__cistatW = cistatW;
		}
	}
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + __cistatW + ", " + __cistatS;
}

/**
Writes a list of StateMod_Plan_WellAugmentation objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write. 
@param newComments new comments to add to the header (e.g., command file, HydroBase version).
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter,
boolean update, List<StateMod_Plan_WellAugmentation> data, int componentType, List<String> newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector();
	fields.add("PlanID");
	fields.add("WellRightID");
	fields.add("WellStructureID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = componentType;
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
	StateMod_Plan_WellAugmentation wellAug = null;
	List<String> commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();

	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector();
		}
		else {
			newComments2 = new Vector(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod well augmentation plan data file.");
		newComments2.add(2,"See also the associated plan station file.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names[i] + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			wellAug = data.get(i);
			
			line[0] = StringUtil.formatString(wellAug.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(wellAug.getCistatW(), formats[1]).trim();
			line[2] = StringUtil.formatString(wellAug.getCistatS(), formats[2]).trim();

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
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

/**
Write well augmentation data to a StateMod file.  History header information 
is also maintained by calling this routine.
@param instrfile input file from which previous history should be taken
@param outstrfile output file to which to write
@param wellAugList list of plans to write.
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String instrfile, String outstrfile,
		List<StateMod_Plan_WellAugmentation> wellAugList, List<String> newComments )
throws Exception
{	String routine = "StateMod_Plan_WellAugmentation.writeStateModFile";
	List<String> commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	String comment;
	try {
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(instrfile),
			IOUtil.getPathUsingWorkingDir(outstrfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);

		int i;
		String iline;
		String cmnt = "#>";
		// This format follows historical conventions found in example files, limited by StateMod ID lengths
		String formatLine1 = "%-12.12s %-12.12s %-12.12s"; // Comment only written if not blank
		StateMod_Plan_WellAugmentation wellAug = null;
		List<Object> v = new Vector(11); // Reuse for all output lines.

		out.println(cmnt);
		out.println(cmnt + "*************************************************");
		out.println(cmnt + "  StateMod Well Augmentation Plan Data");
		out.println(cmnt);
		out.println(cmnt + "  Free format; however historical format based on StateMod" );
		out.println(cmnt + "               identifier string lengths is used for consistency." );
		out.println(cmnt);
		out.println(cmnt + "  Plan ID      cistatP :  Plan identifier");
		out.println(cmnt + "  WellRightID  cistatW :  Well right identifier");
		out.println(cmnt + "  Well ID      cistatS :  Well (structure) identifier");
		out.println(cmnt + "  Comment              :  Optional comments");
		out.println(cmnt + "                          Double quote to faciliate free-format processing.");
		out.println(cmnt);
		out.println(cmnt + " Plan ID    WellRightID    Well ID        Comment");
		out.println(cmnt + "---------exb----------exb----------exb-------------------------------e");
		out.println(cmnt + "EndHeader");

		int num = 0;
		if (wellAugList != null) {
			num = wellAugList.size();
		}
		for (i = 0; i < num; i++) {
			wellAug = wellAugList.get(i);
			if (wellAug == null) {
				continue;
			}
			
			// line 1
			v.clear();
			v.add(wellAug.getID());
			v.add(wellAug.getCistatW());
			v.add(wellAug.getCistatS());
			comment = wellAug.getComment().trim();
			if ( comment.length() > 0 ) {
				comment = " \"" + comment + "\"";
				
			}
			iline = StringUtil.formatString(v, formatLine1) + comment;
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

}