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

import java.io.PrintWriter;

import java.lang.Double;

import java.util.Vector;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class stores return flow assignments.  A Vector of instances is maintained
for each StateMod_Diversion and StateMod_Well.  Each instance indicates the
river node receiving the return flow, percent of the flow going to the node, and
the delay table identifier to use for the time distribution of the flow.
*/
public class StateMod_ReturnFlow extends StateMod_Data 
implements Cloneable, Comparable {
protected String 	_crtnid;	// River node receiving the return flow
protected double	_pcttot;	// % of return flow to this river node
protected int		_irtndl;	// delay (return q) table for return
protected boolean	_monthly_data;

/**
Construct an instance of StateMod_ReturnFlow.
@param smdata_type Either StateMod_DataSet.COMP_DIVERSION_STATIONS or
StateMod_DataSet.COMP_WELL_STATIONS.  Return flow assignments are associated
with these data components.  Therefore, when a change is made, the appropriate
component must be marked dirty.
*/
public StateMod_ReturnFlow(int smdata_type)
{	super();
	_smdata_type = smdata_type;
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
order from the StateMod_ReturnFlow variables, and then by crtnid, pcttot,
and irtndl, in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)o;

	int index = -1;
	String crtnid1 = _crtnid;
	index = crtnid1.indexOf(" - ");
	if (index > 0) {
		crtnid1 = crtnid1.substring(0, index).trim();
	}

	String crtnid2 = rf._crtnid;
	index = crtnid2.indexOf(" - ");
	if (index > 0) {
		crtnid2 = crtnid2.substring(0, index).trim();
	}

	res = crtnid1.compareTo(crtnid2);
	if (res != 0) {
		return res;
	}

	if (_pcttot < rf._pcttot) {
		return -1;
	}
	else if (_pcttot > rf._pcttot) {
		return 1;
	}

	if (_irtndl < rf._irtndl) {
		return -1;
	}
	else if (_irtndl > rf._irtndl) {
		return 1;
	}

	// sort false before true
	if (_monthly_data == false) {
		if (rf._monthly_data == true) {
			return -1;
		}
		return 0;
	}
	else {
		if (rf._monthly_data == true) {
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
	((StateMod_ReservoirAccount)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two return flow Vectors and see if they are the same.
@param v1 the first Vector of StateMod_ReturnFlows to check.  Can not be null.
@param v2 the second Vector of StateMod_ReturnFlows to check.  Can not be null.
@return true if they are the same, false if not.
*/
public static boolean equals(Vector v1, Vector v2) {
	String routine = "StateMod_ReturnFlow.equals(Vector, Vector)";
	StateMod_ReturnFlow rf1;	
	StateMod_ReturnFlow rf2;	
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
			rf1 = (StateMod_ReturnFlow)v1Sort.elementAt(i);	
			rf2 = (StateMod_ReturnFlow)v2Sort.elementAt(i);	
			Message.printStatus(1, routine, rf1.toString());
			Message.printStatus(1, routine, rf2.toString());
			Message.printStatus(1, routine, "Element " + i 
				+ " comparison: " + rf1.compareTo(rf2));
			if (rf1.compareTo(rf2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two return flows are equal.  Strings are compared with
case sensitivity.
@param rf the return flow to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_ReturnFlow rf) {
	if (!super.equals(rf)) {
	 	return false;
	}
	if (	rf._crtnid.equals(_crtnid)
		&& rf._pcttot == _pcttot
		&& rf._irtndl == _irtndl
		&& rf._monthly_data == _monthly_data) {
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_crtnid = null;
	super.finalize();
}

/**
Return the crtnid.
*/
public String getCrtnid() {
	return _crtnid;
}

/**
Retrieve the delay table for return.
*/
public int getIrtndl() {
	return _irtndl;
}

/**
Return the % of return flow to this river node.
*/
public double getPcttot() {
	return _pcttot;
}

private void initialize() {
	_crtnid = "";
	_pcttot = 100;
	_irtndl = 1;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReturnFlow rf = (StateMod_ReturnFlow)_original;
	super.restoreOriginal();

	_crtnid = rf._crtnid;
	_pcttot = rf._pcttot;
	_irtndl = rf._irtndl;
	_isClone = false;
	_original = null;
}

public boolean isMonthly_data() {
	return _monthly_data;
}

/**
Set the crtnid.
*/
public void setCrtnid(String s) {
	if (s != null) {
		if (!s.equals(_crtnid)) {
			setDirty ( true );
			if ( !_isClone && _dataset != null ) {
				_dataset.setDirty(_smdata_type, true);
			}
			_crtnid = s;
		}
	}
}

/**
Set the delay table for return.
*/
public void setIrtndl(int i) {
	if (i != _irtndl) {
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_smdata_type, true);
		}
		_irtndl = i;
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
	if (d != _pcttot) {
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_smdata_type, true);
		}
		_pcttot = d;
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
	return super.toString() + ", " + _crtnid + ", " + _pcttot + ", "
		+ _irtndl + ", " + _monthly_data;
}

/**
Writes a Vector of StateMod_ReturnFlow objects to a list file.  A header is 
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
boolean update, Vector data, int componentType) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	Vector fields = new Vector();
	fields.add("ID");
	fields.add("RiverNodeID");
	if (componentType 
	   == StateMod_DataSet.COMP_WELL_STATION_DEPLETION_TABLES) {
	   	fields.add("DepletionPercent");
	}
	else {
		fields.add("ReturnPercent");
	}
	fields.add("DelayTableID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = componentType;
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
	StateMod_ReturnFlow rf = null;
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
			rf = (StateMod_ReturnFlow)data.elementAt(i);
			
			line[0] = StringUtil.formatString(rf.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(rf.getCrtnid(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(rf.getPcttot(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(rf.getIrtndl(), 
				formats[3]).trim();

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
