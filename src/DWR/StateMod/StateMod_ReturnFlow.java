//------------------------------------------------------------------------------
// StateMod_ReturnFlow - store and manipulate return flow assignments
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 27 Aug 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 18 Oct 1999	CEN, RTi		Because this is now being used for both
//					diversions and wells, I am adding a
//					constructor that indicates that any
//					changes affects a particular file
//					(default remains StateMod_DataSet.
//					COMP_DIVERSION_STATIONS).
// 17 Feb 2001	Steven A. Malers, RTi	Code review.  Clean up javadoc.  Add
//					finalize().  Alphabetize methods.
//					Handle nulls and set unused variables
//					to null.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-09-30	SAM, RTi		* Fix bug where initialize was not
//					  getting called.
//					* Change _dirtyFlag to use the base
//					  class _smdata_type.
// 2003-10-09	J. Thomas Sapienza, RTi	* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
//					* Added equals(Vector, Vector).
//					* Added isMonthly_data().
// 2003-10-15	JTS, RTi		Revised the clone() code.
// 2004-07-14	JTS, RTi		Changed compareTo to account for
//					crtnids that have descriptions, too.
// 2005-01-17	JTS, RTi		* Added createBackup().
//					* Added restoreOriginal().
// 2005-04-15	JTS, RTi		Added writeListFile().
//------------------------------------------------------------------------------

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
<p>
This class stores return flow assignments.  A list of instances is maintained for each StateMod_Diversion
and StateMod_Well (included in station files) and separate files for reservoirs and plans.
Each instance indicates the river node receiving the return flow, percent of the flow going to the node, and
the delay table identifier to use for the time distribution of the flow.
</p>
<p>
The StateMod_Data ID is the station for which the return flow applies.
</p>
*/
public class StateMod_ReturnFlow extends StateMod_Data 
implements Cloneable, Comparable {

/**
River node receiving the return flow.
*/
private String __crtnid;
/**
% of return flow to this river node.
*/
private double __pcttot;
/**
Delay (return q) table for return.
*/
private int __irtndl;
/**
Indicates whether the returns are for daily (false) or monthly (true) data.
*/
private boolean __isMonthlyData;

/**
Construct an instance of StateMod_ReturnFlow.
@param smdataType Either StateMod_DataSet.COMP_DIVERSION_STATIONS,
StateMod_DataSet.COMP_WELL_STATIONS, StateMod_DataSet.COMP_RESERVOIR_RETURN, or
StateMod_DataSet.COMP_PLAN_RETURN.  Return flow assignments are associated
with these data components.  Therefore, when a change is made, the appropriate
component must be marked dirty.
*/
public StateMod_ReturnFlow(int smdataType)
{	super();
	_smdata_type = smdataType;
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)super.clone();
	rf._isClone = true;

	return rf;
}

/**
Compares this object to another StateMod_ReturnFlow object based on the sorted
order from the StateMod_ReturnFlow variables, and then by crtnid, pcttot, and irtndl, in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)o;

	int index = -1;
	String crtnid1 = __crtnid;
	index = crtnid1.indexOf(" - ");
	if (index > 0) {
		crtnid1 = crtnid1.substring(0, index).trim();
	}

	String crtnid2 = rf.__crtnid;
	index = crtnid2.indexOf(" - ");
	if (index > 0) {
		crtnid2 = crtnid2.substring(0, index).trim();
	}

	res = crtnid1.compareTo(crtnid2);
	if (res != 0) {
		return res;
	}

	if (__pcttot < rf.__pcttot) {
		return -1;
	}
	else if (__pcttot > rf.__pcttot) {
		return 1;
	}

	if (__irtndl < rf.__irtndl) {
		return -1;
	}
	else if (__irtndl > rf.__irtndl) {
		return 1;
	}

	// sort false before true
	if (__isMonthlyData == false) {
		if (rf.__isMonthlyData == true) {
			return -1;
		}
		return 0;
	}
	else {
		if (rf.__isMonthlyData == true) {
			return 0;
		}
		return 1;
	}
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_ReturnFlow)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two return flow lists and see if they are the same.
@param v1 the first list of StateMod_ReturnFlow to check.  Cannot be null.
@param v2 the second list of StateMod_ReturnFlow to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List<StateMod_ReturnFlow> v1, List<StateMod_ReturnFlow> v2) {
	String routine = "StateMod_ReturnFlow.equals";
	StateMod_ReturnFlow rf1;	
	StateMod_ReturnFlow rf2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(2, routine, "Return flow lists are different sizes");
		return false;
	}
	else {
		// Sort the lists and compare item-by-item.  Any differences
		// and data will need to be saved back into the data set.
		int size = v1.size();
		//Message.printStatus(2, routine, "Return flow lists are of size: " + size);
		List v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		//Message.printStatus(2, routine, "Return flow lists have been sorted");
	
		for (int i = 0; i < size; i++) {			
			rf1 = (StateMod_ReturnFlow)v1Sort.get(i);	
			rf2 = (StateMod_ReturnFlow)v2Sort.get(i);	
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
public boolean equals(StateMod_ReturnFlow rf) {
	if (!super.equals(rf)) {
	 	return false;
	}
	if ( rf.__crtnid.equals(__crtnid) && rf.__pcttot == __pcttot && rf.__irtndl == __irtndl
		&& rf.__isMonthlyData == __isMonthlyData) {
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__crtnid = null;
	super.finalize();
}

/**
Return the crtnid.
*/
public String getCrtnid() {
	return __crtnid;
}

/**
Retrieve the delay table for return.
*/
public int getIrtndl() {
	return __irtndl;
}

/**
Return the % of return flow to this river node.
*/
public double getPcttot() {
	return __pcttot;
}

private void initialize() {
	__crtnid = "";
	__pcttot = 100;
	__irtndl = 1;
}

public boolean isMonthly_data() {
	return __isMonthlyData;
}

/**
Read return information in and store in a list.
@param filename filename containing return flow information
@param smdataCompType the StateMod_DataSet component type, passed to the constructor of StateMod_ReturnFlow
objects.
@throws Exception if an error occurs
*/
public static List<StateMod_ReturnFlow> readStateModFile(String filename, int smdataCompType )
throws Exception
{	String routine = "StateMod_ReturnFlow.readStateModFile";
	String iline = null;
	List<String> v;
	List<StateMod_ReturnFlow> theReturns = new Vector();
	int linecount = 0;
	
	StateMod_ReturnFlow aReturn = null;
	BufferedReader in = null;

	Message.printStatus(2, routine, "Reading return file: " + filename);
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
			if ( size < 4 ) {
				Message.printStatus ( 2, routine, "Ignoring line " + linecount +
				" not enough data values.  Have " + size + " expecting 4+" );
				++errorCount;
				continue;
			}
			// Uncomment if testing...
			//Message.printStatus ( 2, routine, "" + v );

			// Allocate new plan node and set the values
			aReturn = new StateMod_ReturnFlow(smdataCompType);
			aReturn.setID(v.get(0).trim()); 
			aReturn.setName(v.get(0).trim()); // Same as ID
			aReturn.setCrtnid(v.get(1).trim());
			aReturn.setCgoto(v.get(1).trim()); // Redundant
			aReturn.setPcttot(v.get(2).trim());
			aReturn.setIrtndl(v.get(3).trim());
			if ( v.size() > 4 ) {
				aReturn.setComment(v.get(4).trim());
			}

			// Set the return to not dirty because it was just initialized...

			aReturn.setDirty ( false );

			// Add the return to the list of returns
			theReturns.add(aReturn);
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
	return theReturns;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)_original;
	super.restoreOriginal();

	__crtnid = rf.__crtnid;
	__pcttot = rf.__pcttot;
	__irtndl = rf.__irtndl;
	_isClone = false;
	_original = null;
}

/**
Set the crtnid.
*/
public void setCrtnid(String s) {
	if (s != null) {
		if (!s.equals(__crtnid)) {
			setDirty ( true );
			if ( !_isClone && _dataset != null ) {
				_dataset.setDirty(_smdata_type, true);
			}
			__crtnid = s;
		}
	}
}

/**
Set the delay table for return.
*/
public void setIrtndl(int i) {
	if (i != __irtndl) {
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_smdata_type, true);
		}
		__irtndl = i;
	}
}

public void setIrtndl(Integer i) {
	setIrtndl(i.intValue());
}

public void setIrtndl(String str) {
	if (str != null) {
		setIrtndl(StringUtil.atoi(str.trim()));
	}
}

/**
Set the % of return flow to this river node.
*/
public void setPcttot(double d) {
	if (d != __pcttot) {
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_smdata_type, true);
		}
		__pcttot = d;
	}
}

public void setPcttot(Double d) {
	setPcttot(d.doubleValue());
}

public void setPcttot(String str) {
	if (str != null) {
		setPcttot(StringUtil.atod(str.trim()));
	}
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + __crtnid + ", " + __pcttot + ", " + __irtndl + ", " + __isMonthlyData;
}

/**
Writes a list of StateMod_ReturnFlow objects to a list file.  A header is 
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
boolean update, List<StateMod_ReturnFlow> data, int componentType, List<String> newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector();
	fields.add("ID");
	fields.add("RiverNodeID");
	if (componentType == StateMod_DataSet.COMP_WELL_STATION_DEPLETION_TABLES) {
	   	fields.add("DepletionPercent");
	}
	else {
		fields.add("ReturnPercent");
	}
	fields.add("DelayTableID");
	fields.add("Comment");
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
	StateMod_ReturnFlow rf = null;
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
		if (componentType == StateMod_DataSet.COMP_DIVERSION_STATION_DELAY_TABLES) {
			newComments2.add(1,"StateMod diversion delay (return) file.");
			newComments2.add(2,"See also the associated diversion station file.");
		}
		else if (componentType == StateMod_DataSet.COMP_WELL_STATION_DEPLETION_TABLES) {
			newComments2.add(1,"StateMod well depletion file.");
			newComments2.add(2,"See also the associated well station and return files.");
		}
		else if (componentType == StateMod_DataSet.COMP_WELL_STATION_DELAY_TABLES) {
			newComments2.add(1,"StateMod well delay (return) file.");
			newComments2.add(2,"See also the associated well station and depletion files.");
		}
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
			rf = data.get(i);
			
			line[0] = StringUtil.formatString(rf.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(rf.getCrtnid(), formats[1]).trim();
			line[2] = StringUtil.formatString(rf.getPcttot(), formats[2]).trim();
			line[3] = StringUtil.formatString(rf.getIrtndl(), formats[3]).trim();
			line[4] = StringUtil.formatString(rf.getComment(), formats[4]).trim();

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
Write return flow information to a StateMod file.  History header information 
is also maintained by calling this routine.
@param instrfile input file from which previous history should be taken
@param outstrfile output file to which to write
@param stationType the station type, for the file header (e.g., "Plan", "Reservoir").
@param theReturns list of plans to write.
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String instrfile, String outstrfile,
		String stationType, List<StateMod_ReturnFlow> theReturns, List<String> newComments )
throws Exception
{	String routine = "StateMod_ReturnFlow.writeStateModFile";
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
		String formatLine1 = "%-12.12s %-12.12s %8.2f %-12.12s"; // Comment only written if not blank
		StateMod_ReturnFlow rf = null;
		List<Object> v = new Vector(11); // Reuse for all output lines.

		out.println(cmnt);
		out.println(cmnt + "*************************************************");
		out.println(cmnt + "  StateMod " + stationType + " Return Flows");
		out.println(cmnt);
		out.println(cmnt + "  Free format; however historical format based on StateMod" );
		out.println(cmnt + "               identifier string lengths is used for consistency." );
		out.println(cmnt);
		out.println(cmnt + "  ID                   :  " + stationType + " ID");
		out.println(cmnt + "  River Node     crtnid:  River node identifier receiving return flow");
		out.println(cmnt + "  Ret %         pcttot*:  Percent of return flow the the river node");
		out.println(cmnt + "  Table ID      irtndl*:  Delay (return flow) table identifier for return");
		out.println(cmnt + "  Comment              :  Optional (e.g., return type, name)");
		out.println(cmnt + "                          Double quote to faciliate free-format processing.");
		out.println(cmnt);
		out.println(cmnt + " ID         River Node    Ret %    Table ID       Comment");
		out.println(cmnt + "---------exb----------exb------exb----------exb-------------------------------e");
		out.println(cmnt + "EndHeader");

		int num = 0;
		if (theReturns != null) {
			num = theReturns.size();
		}
		for (i = 0; i < num; i++) {
			rf = theReturns.get(i);
			if (rf == null) {
				continue;
			}
			
			// line 1
			v.clear();
			v.add(rf.getID());
			v.add(rf.getCrtnid());
			v.add(rf.getPcttot());
			v.add("" + rf.getIrtndl()); // Format as string
			comment = rf.getComment().trim();
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