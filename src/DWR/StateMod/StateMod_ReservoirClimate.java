//------------------------------------------------------------------------------
// StateMod_ReservoirClimate - class to store reservoir climate information,
//	including evaporation and precipitation stations
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 11 Feb 1998	CEN, RTi		Add StateMod_DataSet.setDirty 
//					to all set
//					commands.
// 01 Apr 1998	CEN, RTi		Added javadoc comments.
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 17 Feb 2001	Steven A. Malers, RTi	Code review.  Clean up javadoc.  Add
//					finalize().  Handle nulls and set unused
//					variables to null.  Alphabetize code.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-05	J. Thomas Sapienza 	Initial StateMod_ version.
// 2003-07-15	JTS, RTi		Changed code to use new DatSet design.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-16	SAM, RTi		* Change name of class from
//					  StateMod_Climate to
//					  StateMod_ReservoirClimate because the
//					  data are really for assigning climate
//					  stations to reservoirs.
//					* Remove exceptions from getNumEvap()
//					  and getNumPrecip() since it was
//					  somewhat redundant.
//					* Use the base class identifier data
//					  member than the old cevap_cprer
//					  data member - one less data member to
//					  manage and the old name is ugly.
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
This class stores reservoir climate information, including evaporation and 
precipitation stations, which pertain to an individual reservoir.
The actual time series data are stored as separate components and are shared
among reservoirs.
Any calls to "set" routines sets the StateMod_DataSet.COMP_RESERVOIR_STATIONS 
dirty.
*/
public class StateMod_ReservoirClimate extends StateMod_Data
implements Cloneable, Comparable {

/**
Precipitation station type.
*/
public static final int CLIMATE_PTPX	= 0;
/**
Evaporation station type.
*/
public static final int CLIMATE_EVAP	= 1;

/**
CLIMATE_PTPX or CLIMATE_EVAP
*/
protected int		_type;

/**
Percent of this station to use.
*/
protected double	_weight;
	
/**
Constructor allocates a String for the station id, sets the weight to 0 and
sets the type to an invalid value so developer must set to a valid value.
*/
public StateMod_ReservoirClimate() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_ReservoirClimate rc = 
		(StateMod_ReservoirClimate)super.clone();
	rc._isClone = true;
	return rc;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by _type and _weight, in 
that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_ReservoirClimate rc = (StateMod_ReservoirClimate)o;
	if (_type < rc._type) {
		return -1;
	}
	else if (_type > rc._type) {
		return 1;
	}

	if (_weight < rc._weight) {
		return -1;
	}
	else if (_weight > rc._weight) {
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
	((StateMod_ReservoirClimate)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_ReservoirClimate s to check.  Can not
be null.
@param v2 the second Vector of StateMod_ReservoirClimate s to check.  Can not
be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List v1, List v2) {
	String routine = "StateMod_ReservoirClimate.equals(Vector, Vector)";
	StateMod_ReservoirClimate r1;	
	StateMod_ReservoirClimate r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(1, routine, "Vectors are different sizes");
		return false;
	}
	else {
		// sort the Vectors and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		Message.printStatus(1, routine, "Vectors are of size: " + size);
		List v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(1, routine, "Vectors have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = (StateMod_ReservoirClimate)v1Sort.get(i);	
			r2 = (StateMod_ReservoirClimate)v2Sort.get(i);	
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
@param rc the rc to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_ReservoirClimate rc) {
	if (!super.equals(rc)) {
	 	return false;
	}

	if (	_type == rc._type
		&& _weight == rc._weight) {
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
Returns the type
@return _type
*/
public int getType() {
	return _type;
}

/**
Returns the weight
@return _weight
*/
public double getWeight() {
	return _weight;
}

/**
Initializes member variables.
*/
private void initialize()
{	_type = 2;	// not valid - forces us to set
	_weight = 0;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReservoirClimate clim = (StateMod_ReservoirClimate)_original;
	super.restoreOriginal();

	_type = clim._type;
	_weight = clim._weight;
	_isClone = false;
	_original = null;
}

/**
Set the type.  Use either CLIMATE_PTPX or CLIMATE_EVAP.
@param i the type to set
*/
public void setType(int i) {
	if (i != _type) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
		_type = i;
	}
}

/**
Set the weight.
@param d weight to set
*/
public void setWeight(double d) {
	if (d != _weight) {
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
		_weight = d;
	}
}

/**
Set the weight.
@param d weight to set
*/
public void setWeight(Double d) {
	setWeight(d.doubleValue());
}

/**
Set the weight.
@param str weight to set
*/
public void setWeight(String str) {
	if (str != null) {
		setWeight(StringUtil.atod(str.trim()));
	}
}

/**
Calculates the number of StateMod_ReservoirClimate objects that are evaporation
stations.
@return the number of StateMod_ReservoirClimate objects that are evaporation
stations.
*/
public static int getNumEvap(List climates)
{	if (climates == null) {
		return 0;
	}

	int nevap=0;
	int num = climates.size();
	
	for (int i=0; i<num; i++) {
		if (((StateMod_ReservoirClimate)
			climates.get(i)).getType() == CLIMATE_EVAP) {
			nevap++;
		}
	} 
	return nevap;	
}

/**
Calculates the number of StateMod_ReservoirClimate objects that are
precipitation stations.
@return the number of StateMod_ReservoirClimate objects that are precipitation
stations.
*/
public static int getNumPrecip(List climates)
{	if (climates == null) {
		return 0;
	}
	
	int nptpx=0;
	int num = climates.size();
	
	for (int i=0; i<num; i++) {
		if (((StateMod_ReservoirClimate)
			climates.get(i)).getType() == CLIMATE_PTPX) {
			nptpx++;
		}
	}
	return nptpx;	
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _type + ", " + _weight;
}

/**
Writes a Vector of StateMod_ReservoirClimate objects to a list file.  A header 
is printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter 
will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the Vector of objects to write.  
@param componentType one of either 
StateMod_DataSet.COMP_RESERVOIR_PRECIP_STATIONS or
StateMod_DataSet.COMP_RESERVOIR_EVAP_STATIONS.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter,
boolean update, List data, int componentType) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("ReservoirID");
	fields.add("StationID");
	fields.add("PercentWeight");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = componentType;
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
	StateMod_ReservoirClimate cli = null;
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
			cli = (StateMod_ReservoirClimate)data.get(i);
			
			line[0] = StringUtil.formatString(cli.getCgoto(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(cli.getID(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(cli.getWeight(), 
				formats[2]).trim();

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
