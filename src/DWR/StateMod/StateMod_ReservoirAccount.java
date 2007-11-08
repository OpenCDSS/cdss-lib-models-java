//------------------------------------------------------------------------------
// StateMod_ReservoirAccount - class to store reservoir owner information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 17 Feb 2001	Steven A. Malers, RTi	Handled nulls everwhere.  Add
//					finalize().  Set unused variables to
//					null.  Update javadoc.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
// 2002-11-01	SAM, RTi		Add toString().
//------------------------------------------------------------------------------
// 2003-06-05	J. Thomas Sapienza 	Initial StateMod_ version.
// 2003-06-10	JTS, RTi		Renamed from StateMod_OwnerInfo
// 2003-08-03	SAM, RTi		Change isDirty() back to setDirty().
// 2003-08-19	SAM, RTi		Update code to make consistent with
//					StateMod documentation and use the
//					base class for name.
// 2003-10-09	JTS, RTi		* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
// 					* Added equals(Vector, Vector)
// 2003-10-15	JTS, RTi		Revised the clone() code.
// 2004-07-02	SAM, RTi		Handle null _dataset.
// 2004-09-12	SAM, RTi		* Add getPctevaChoices().
//					* Add getN2ownChoices().
// 2004-10-29	SAM, RTi		Remove temporary data for table model.
// 2005-01-17	JTS, RTi		* Added createBackup().
//					* Added restoreOriginal().
// 2005-04-18	JTS, RTi		Added writeListFile().
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_ReservoirAccount extends StateMod_Data 
implements Cloneable, Comparable {

/**
Maximum storage of owner
*/
protected double	_ownmax;
/**
INitial storage of owner
*/
protected double	_curown;
/**
Prorate res evap btwn accnt owners
*/
protected double	_pcteva;
/**
Ownership is tied to n fill right
*/
protected int		_n2own;

// Base class _name is used for the owner name.
// The ID should be sete when reading, adding, or deleting accounts - set to
// an integer starting at 1.
	
/**
Constructor.
*/
public StateMod_ReservoirAccount() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_ReservoirAccount acct = 
		(StateMod_ReservoirAccount)super.clone();
	acct._isClone = true;
	return acct;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by _ownmax, _curown, 
_pcteva, and _n2own, in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_ReservoirAccount acct = (StateMod_ReservoirAccount)o;
	if (_ownmax < acct._ownmax) {
		return -1;
	}
	else if (_ownmax > acct._ownmax) {
		return 1;
	}

	if (_curown < acct._curown) {	
		return -1;
	}
	else if (_curown > acct._curown) {
		return 1;
	}

	if (_pcteva < acct._pcteva) {
		return -1;
	}
	else if (_pcteva > acct._pcteva) {
		return 1;
	}

	if (_n2own < acct._n2own) {
		return -1;
	}
	else if (_n2own > acct._n2own) {
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
	((StateMod_ReservoirAccount)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_ReservoirAccounts to check.  Can not
be null.
@param v2 the second Vector of StateMod_ReservoirAccounts to check.  Can not
be null.
@return true if they are the same, false if not.
*/
public static boolean equals(Vector v1, Vector v2) {
	String routine = "StateMod_ReservoirAccount.equals(Vector, Vector)";
	StateMod_ReservoirAccount r1;	
	StateMod_ReservoirAccount r2;	
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
			r1 = (StateMod_ReservoirAccount)v1Sort.elementAt(i);	
			r2 = (StateMod_ReservoirAccount)v2Sort.elementAt(i);	
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
@param acct the acct to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_ReservoirAccount acct) {
	if (!super.equals(acct)) {
	 	return false;
	}

	if (	_ownmax == acct._ownmax
		&& _curown == acct._curown
		&& _pcteva == acct._pcteva
		&& _n2own == acct._n2own) {
		return true;
	}
	return false;
}

/**
Clean up memory before garbage collection.
*/
protected void finalize()
throws Throwable {
	super.finalize();
}

/**
Return the initial storage of owner.
*/
public double getCurown() {
	return _curown;
}

/**
Return the maximum storage of owner.
*/
public double getOwnmax() {
	return _ownmax;
}

/**
Return the ownership tied to n fill right.
*/
public int getN2own() {
	return _n2own;
}

/**
Return a list of N2own option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Ownership is tied to first fill right(s)", if include_notes is true.
@return a list of N2own option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getN2ownChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "1 - Ownership is tied to first fill right(s)" );
	v.addElement ( "2 - Ownership is tied to second fill right(s)" );
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
Return the default N2own choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default N2own choice.
*/
public static String getN2ownDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return "1 - Ownership is tied to first fill right(s)";
	}
	else {	return "1";
	}
}

/**
Return the prorate res evap btwn accnt owners.
*/
public double getPcteva() {
	return _pcteva;
}

/**
Return a list of Pcteva option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and
"0 - Prorate evaporation based on current storage", if include_notes is true.
@return a list of Pcteva option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getPctevaChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "0 - Prorate evaporation based on current storage" );
	for ( int i = 100; i >= 1; i-- ) {
		v.addElement ( "" + i + " - Apply " + i +
		" % of evaporation to account" );
	}
	v.addElement ( "-1 - No evaporation for this account" );
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
Return the default Pcteva choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default Pcteva choice.
*/
public static String getPctevaDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return "0 - Prorate evaporation based on current storage";
	}
	else {	return "0";
	}
}

/**
Initialize member variables
*/
private void initialize() {
	_ownmax = 0;
	_curown = 0;
	_pcteva = 0;
	_n2own = 1;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReservoirAccount acct = (StateMod_ReservoirAccount)_original;
	super.restoreOriginal();

	_ownmax = acct._ownmax;
	_curown = acct._curown;
	_pcteva = acct._pcteva;
	_n2own = acct._n2own;
	_isClone = false;
	_original = null;
}

/**
Set the initial storage of owner.
*/
public void setCurown(double d) {
	if (d != _curown) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		_curown = d;
	}
}

/**
Set the initial storage of owner.
*/
public void setCurown(Double d) {
	setCurown(d.doubleValue());
}

/**
Set the initial storage of owner.
*/
public void setCurown(String str) {
	if (str == null) {
		return;
	}
	setCurown(StringUtil.atod(str.trim()));
}

/**
Set the ownership tied to n fill right.
*/
public void setN2own(int i) {
	if (i != _n2own) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		_n2own = i;
	}
}

/**
Set the ownership tied to n fill right.
*/
public void setN2own(Integer i) {
	setN2own(i.intValue());
}

/**
Set the ownership tied to n fill right.
*/
public void setN2own(String str) {
	if (str == null) {
		return;
	}
	setN2own(StringUtil.atoi(str.trim()));
}

/**
Set the maximum storage of owner.
*/
public void setOwnmax(double d) {
	if (d != _ownmax) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		_ownmax = d;
	}
}

/**
Set the maximum storage of owner.
*/
public void setOwnmax(Double d) {
	setOwnmax(d.doubleValue());
}

/**
Set the maximum storage of owner.
*/
public void setOwnmax(String str) {
	if (str == null) {
		return;
	}
	setOwnmax(StringUtil.atod(str.trim()));
}

/**
Set the prorate res evap btwn accnt owners.
*/
public void setPcteva(double d) {
	if (d != _pcteva) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		_pcteva = d;
	}
}

/**
Set the prorate res evap btwn accnt owners.
*/
public void setPcteva(Double d) {
	setPcteva(d.doubleValue());
}

/**
Set the prorate res evap btwn accnt owners.
*/
public void setPcteva(String str) {
	if (str == null) {
		return;
	}
	setPcteva(StringUtil.atod(str.trim()));
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _ownmax + ", " + _curown + ", "
		+ _pcteva + ", " + _n2own;
}

/**
Writes a Vector of StateMod_ReservoirAccount objects to a list file.  A header 
is printed to the top of the file, containing the commands used to generate the
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
	fields.add("ReservoirID");
	fields.add("OwnerID");
	fields.add("OwnerAccount");
	fields.add("MaxStorage");
	fields.add("InitialStorage");
	fields.add("ProrateEvap");
	fields.add("OwnershipTie");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RESERVOIR_STATION_ACCOUNTS;
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
	StateMod_ReservoirAccount acct = null;
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
			acct = (StateMod_ReservoirAccount)data.elementAt(i);
			
			line[0] = StringUtil.formatString(acct.getCgoto(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(acct.getID(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(acct.getName(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(acct.getOwnmax(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(acct.getCurown(), 
				formats[4]).trim();
			line[5] = StringUtil.formatString(acct.getPcteva(), 
				formats[5]).trim();
			line[6] = StringUtil.formatString(acct.getN2own(), 
				formats[6]).trim();

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
