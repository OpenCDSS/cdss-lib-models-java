//------------------------------------------------------------------------------
// StateMod_ReservoirAreaCap - class to store area capacity values for reservoir
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 11 Feb 1998	CEN, RTi		Added _dataset.setDirty
//					to all set
//					commands.
// 17 Feb 2001	Steven A. Malers, RTi	Add finalize().  Clean up javadoc.
//					Handle nulls.  Set variables to null
//					when done.  Alphabetize methods.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-05	J. Thomas Sapienza 	Initial StateMod_ version.
// 2003-06-10	JTS, RTi		Renamed from StateMod_AreaCap
// 2003-07-15	JTS, RTi		Changed to use new dataset design.
// 2003-08-03	SAM, RTi		Change isDirty() to setDirty().
// 2003-10-09	JTS, RTi		* Implemented Cloneable.
//					* Added clone().
//					* Added equals().
//					* Implemented Comparable.
//					* Added compareTo().
// 					* Added equals(Vector, Vector)
// 2003-10-15	JTS, RTi		* Revised the clone() code.
//					* Added toString().
// 2004-07-02	SAM, RTi		Handle null _dataset.
// 2005-01-17	JTS, RTi		* Added createBackup().
//					* Added restoreOriginal().
// 2005-03-30	JTS, RTi		Corrected class mis-type in 
//					createBackup().
// 2005-04-18	JTS, RTi		Added writeListFile().
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Object used to store reservoir area capacity information in.
Any calls to "set" routines sets the _dataset.COMP_RESERVOIR_STATIONS flag dirty.
*/
public class StateMod_ReservoirAreaCap extends StateMod_Data 
implements Cloneable, Comparable {

/**
Content in area cap table.
*/
protected double _conten;
/**
Area associated with the content.
*/
protected double _surarea;
/**
Seepage associated with the content.
*/
protected double _seepage;
	
/**
Constructor.
*/
public StateMod_ReservoirAreaCap() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_ReservoirAreaCap ac = (StateMod_ReservoirAreaCap)super.clone();
	ac._isClone = true;
	return ac;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by conten, surarea and seepage, in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_ReservoirAreaCap ac = (StateMod_ReservoirAreaCap)o;

	if (_conten < ac._conten) {
		return -1;
	}
	else if (_conten > ac._conten) {
		return 1;
	}

	if (_surarea < ac._surarea) {
		return -1;
	}
	else if (_surarea > ac._surarea) {
		return 1;
	}

	if (_seepage < ac._seepage) {
		return -1;
	}
	else if (_seepage > ac._seepage) {
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
	((StateMod_ReservoirAreaCap)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_ReservoirAreaCap s to check.  Cannot be null.
@param v2 the second Vector of StateMod_ReservoirAreaCap s to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List v1, List v2) {
	String routine = "StateMod_ReservoirAreaCap.equals";
	StateMod_ReservoirAreaCap r1;	
	StateMod_ReservoirAreaCap r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(2, routine, "Lists are different sizes");
		return false;
	}
	else {
		// sort the Vectors and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		Message.printStatus(1, routine, "Lists are of size: " + size);
		List v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(2, routine, "Vectors have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = (StateMod_ReservoirAreaCap)v1Sort.get(i);	
			r2 = (StateMod_ReservoirAreaCap)v2Sort.get(i);	
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
Tests to see if two diversion rights are equal.  Strings are compared with case sensitivity.
@param ac the ac to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_ReservoirAreaCap ac) {
	if (!super.equals(ac)) {
	 	return false;
	}

	if ( _conten == ac._conten && _surarea == ac._surarea && _seepage == ac._seepage) {
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	super.finalize();
}

/**
Returns the content in area capacity table.
@return the content in area capacity table.
*/
public double getConten() {
	return _conten;
}

/**
Returns the seepage associated with the content.
@return the seepage associated with the content.
*/
public double getSeepage() {
	return _seepage;
}

/**
Returns the area associated with the content.
@return the area associated with the content.
*/
public double getSurarea() {
	return _surarea;
}

/**
Initializes member variables.
*/
private void initialize() {
	_conten = 0;
	_surarea = 0;
	_seepage = 0;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReservoirAreaCap ac = (StateMod_ReservoirAreaCap)_original;
	super.restoreOriginal();

	_conten = ac._conten;
	_surarea = ac._surarea;
	_seepage = ac._seepage;
	_isClone = false;
	_original = null;
}

/**
Set the content in area capacity table.
@param d content to set
*/
public void setConten(double d) {
	if (d != _conten) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
		_conten = d;
	}
}

/**
Set the content in area capacity table.
@param d content to set
*/
public void setConten(Double d) {
	setConten(d.doubleValue());
}

/**
Set the content in area capacity table.
@param str content to set
*/
public void setConten(String str) {
	if (str == null) {
		return;
	}
	setConten(StringUtil.atod(str.trim()));
}

/**
Set the seepage associated with the content.
@param d seepage to set
*/
public void setSeepage(double d) {
	if (d != _seepage) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
		_seepage = d;
	}
}

/**
Set the seepage associated with the content.
@param d seepage to set
*/
public void setSeepage(Double d) {
	setSeepage(d.doubleValue());
}

/**
Set the seepage associated with the content.
@param str seepage to set
*/
public void setSeepage(String str) {
	if (str == null) {
		return;
	}
	setSeepage(StringUtil.atod(str.trim()));
}

/**
Set the area associated with the content.
@param d content to set
*/
public void setSurarea(double d) {
	if (d != _surarea) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
		_surarea = d;
	}
}

/**
Set the area associated with the content.
@param d content to set
*/
public void setSurarea(Double d) {
	setSurarea(d.doubleValue());
}

/**
Set the area associated with the content.
@param str content to set
*/
public void setSurarea(String str) {
	if (str == null) {
		return;
	}
	setSurarea(StringUtil.atod(str.trim()));
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _conten + ", " + _surarea + ", " + _seepage;
}
 
/**
Writes a list of StateMod_ReservoirAreaCap objects to a list file.  A header 
is printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments new comments to add at the top of the file.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List data,
	List newComments ) 
throws Exception
{	String routine = "StateMod_ReservoirAreaCap.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("ReservoirID");
	fields.add("Content");
	fields.add("Area");
	fields.add("Seepage");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_Util.COMP_RESERVOIR_AREA_CAP;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.get(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	StateMod_ReservoirAreaCap area = null;
	String[] line = new String[fieldCount];
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  Do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector();
		}
		else {
			newComments2 = new Vector(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod reservoir content/area/seepage data as a delimited list file.");
		newComments2.add(2,"See also the associated station, account, precipitation station,");
		newComments2.add(3,"evaporation station, and collection files.");
		newComments2.add(4,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			area = (StateMod_ReservoirAreaCap)data.get(i);
			
			line[0] = StringUtil.formatString(area.getCgoto(),formats[0]).trim();
			line[1] = StringUtil.formatString(area.getConten(),formats[1]).trim();
			line[2] = StringUtil.formatString(area.getSurarea(),formats[2]).trim();
			line[3] = StringUtil.formatString(area.getSeepage(),formats[3]).trim();

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
		Message.printWarning ( 3, routine, e );
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