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
// 2007-04-12	Kurt Tometich, RTi		Added checkComponentData() and
//									getDataHeader() methods for check
//									file and data check support.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// 2007-05-14	SAM, RTi		Implement the StateMod_Right interface to make
//					it easier to handle rights generically in other code.
// 2007-05-16	SAM, RTi		Add isWellRightFile() to help code like TSTool
//					generically handle reading.  Add optional comment to output.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class provides stores all the information associated with a well right.
*/
public class StateMod_WellRight extends StateMod_Data
implements Cloneable, Comparable, StateMod_ComponentValidator, StateMod_Right {

/**
Administration number.
*/
private String 	_irtem;
/**
Decreed amount.
*/
private double	_dcrdivw;

// Parcel data used in processing and relationships to GIS.
// This work is exploratory and may or may not be part of the
// official files.

/**
Year for parcels.
*/
private int __parcelYear;
/**
Identifier for parcel.
*/
private String __parcelId;
/**
Well to parcel matching "class".
*/
private int __parcelMatchClass;
	
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
@param v1 the first Vector of StateMod_WellRight s to check.  Cannot be null.
@param v2 the second Vector of StateMod_WellRight s to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List v1, List v2) {
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
		Message.printStatus(2, routine, "Vectors are of size: " + size);
		List v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(2, routine, "Vectors have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = (StateMod_WellRight)v1Sort.get(i);	
			r2 = (StateMod_WellRight)v2Sort.get(i);	
			Message.printStatus(2, routine, r1.toString());
			Message.printStatus(2, routine, r2.toString());
			Message.printStatus(2, routine, "Element " + i + " comparison: " + r1.compareTo(r2));
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
public boolean equals(StateMod_WellRight right) {
	 if (!super.equals(right)) {
	 	return false;
	}
	
	if ( right._irtem.equals(_irtem) && right._dcrdivw == _dcrdivw) {
		return true;
	}
	return false;
}

/**
Return the administration number, as per the generic interface.
@return the administration number, as a String to protect from roundoff.
*/
public String getAdministrationNumber ()
{	return getIrtem();
}

/**
Returns the table header for StateMod_WellRight data tables.
@return String[] header - Array of header elements.
 */
public static String[] getDataHeader()
{
	return new String[] {
		"Num",
		"Well Right ID",
		"Well Station ID" };
		//"Well Name" };
}

/**
@return the decreed amount(cfs)
*/
public double getDcrdivw() {
	return _dcrdivw;
}

/**
Return the decree, as per the generic interface.
@return the decree, in the units of the data.
*/
public double getDecree()
{	return getDcrdivw();
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
@return the administration number
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
@return the parcel identifier
*/
public String getParcelID() {
	return __parcelId;
}

/**
@return the parcel match class.
*/
public int getParcelMatchClass() {
	return __parcelMatchClass;
}

/**
@return the parcel year.
*/
public int getParcelYear() {
	return __parcelYear;
}

/**
Create summary comments suitable to add to a file header.
*/
private static List getSummaryCommentList(List rightList)
{
	List summaryList = new Vector();
	int size = rightList.size();
	List parcelMatchClassList = new Vector();
	StateMod_WellRight right = null;
	int parcelMatchClass;
	// Determine the unique list of water right classes - ok to end up with one class of -999
	// if class match information is not available.
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_WellRight)rightList.get(i);
		parcelMatchClass = right.getParcelMatchClass();
		boolean found = false;
		for ( int j = 0; j < parcelMatchClassList.size(); j++ ) {
			if ( ((Integer)parcelMatchClassList.get(j)).intValue() == parcelMatchClass ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			parcelMatchClassList.add ( new Integer(parcelMatchClass));
		}
	}
	// Now summarize information by class
	//int parcelMatchClassListSize = parcelMatchClassList.size();
	//int [] countByClass = new int[parcelMatchClassListSize];
	return summaryList;
}

private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_WELL_RIGHTS;
	_irtem = "99999";
	_dcrdivw = 0;
	// Parcel data...
	__parcelId = "";
	__parcelYear = StateMod_Util.MISSING_INT;
	__parcelMatchClass = StateMod_Util.MISSING_INT;
}

/**
Determine whether the right is for an estimated well.  Estimated wells are those that are copies of
real wells, as an estimate of water supply for parcels that are clearly groundwater irrigated but a supply
well is not physically evident in remote sensing work.
@return true if the well right is for an estimated well.
*/
public boolean isEstimatedWell ()
{
	int parcelMatchClass = getParcelMatchClass();
	if ( (parcelMatchClass == 4) || (parcelMatchClass == 9) ) {
		return true;
	}
	return false;
}

/**
Determine whether a file is a well right file.  Currently true is returned if the file extension is ".wer".
@param filename Name of the file being checked.
@return true if the file is a StateMod well right file.
*/
public static boolean isWellRightFile ( String filename )
{	if ( filename.toUpperCase().endsWith(".WER")) {
		return true;
	}
	return false;
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
	__parcelId = right.__parcelId;
	__parcelMatchClass = right.__parcelMatchClass;
	__parcelYear = right.__parcelYear;
	_isClone = false;
	_original = null;
}

/**
Read the well rights file.
@param filename name of file containing well rights
@return list of well rights
*/
public static List<StateMod_WellRight> readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_WellRight.readStateModFile";
	List<StateMod_WellRight> theWellRights = new Vector<StateMod_WellRight>();
	int [] format_0 = {
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER,
				// If comments used
				//StringUtil.TYPE_STRING,
				// Parcel data...
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_STRING
				};
	int [] format_0w = {
				12,
				24,
				12,
				16,
				8,
				8,
				// If comments used
				//80,
				// Parcel data
				5,
				5,
				//7}; // When maximum of 6-digit parcel identifiers were used
				13}; // Now using 12-digit maximum parcel identifiers
	String iline = null;
	List v = new Vector(10);
	BufferedReader in = null;
	StateMod_WellRight aRight = null;

	Message.printStatus(1, routine, "Reading well rights file: " + filename);

	try {
		in = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine()) != null) {
			// check for comments
			if (iline.startsWith("#") || 
				iline.trim().length() == 0) {
				continue;
			}
			
			aRight = new StateMod_WellRight();

			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "iline: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aRight.setID(((String)v.get(0)).trim());
			aRight.setName(((String)v.get(1)).trim());
			aRight.setCgoto(((String)v.get(2)).trim());
			aRight.setIrtem(((String)v.get(3)).trim());
			aRight.setDcrdivw((Double)v.get(4));
			aRight.setSwitch((Integer)v.get(5));
			/* If comments used
			if ( v.size() > 6 ) {
				// Save the comment at the end of the line
				comment = (String)v.get(6);
				if ( comment != null ) {
					aRight.setComment ( comment.trim() );
				}
			}
			*/
			// Evaluate handling parcel data... 
			aRight.setParcelYear((Integer)v.get(6));
			aRight.setParcelMatchClass((Integer)v.get(7));
			aRight.setParcelID(((String)v.get(8)).trim());
			theWellRights.add(aRight);
		}
	} 
	catch(Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
		in = null;
	}
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
	if (dcrdivw == null) {
		return;
	}
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
Set the decree, as per the generic interface.
@param decree decree, in the units of the data.
*/
public void setDecree(double decree)
{	setDcrdivw(decree);
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
Set the parcel identifier.
@param parcel_id Parcel identifier.
*/
public void setParcelID(String parcel_id) {
	if (parcel_id == null) {
		return;
	}
	if (!parcel_id.equals(__parcelId)) {
		__parcelId = parcel_id.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the parcel match class.
@param parcel_match_class Parcel to well match class.
*/
public void setParcelMatchClass(Integer parcel_match_class) {
	if (parcel_match_class == null) {
		return;
	}
	setParcelMatchClass(parcel_match_class.intValue());
}

/**
Set the parcel match class, used to match wells to parcels.
@param parcel_match_class Parcel match class.
*/
public void setParcelMatchClass(int parcel_match_class) {
	if (parcel_match_class != __parcelMatchClass) {
		__parcelMatchClass = parcel_match_class;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the parcel year.
@param parcel_year Parcel year.
*/
public void setParcelYear(Integer parcel_year) {
	if (parcel_year == null) {
		return;
	}
	setParcelYear(parcel_year.intValue());
}

/**
Set the parcel year, used to match wells to parcels.
@param parcel_year Parcel year.
*/
public void setParcelYear(int parcel_year) {
	if (parcel_year != __parcelYear) {
		__parcelYear = parcel_year;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Performs specific data checks for StateMod Well Rights.
@param dataset StateMod dataset.
@return validation results.
 */
public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String cgoto = getCgoto();
	String irtem = getIrtem();
	double dcrdivw = getDcrdivw();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right identifier is blank.",
			"Specify a well right identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id + "\" name is blank.",
			"Specify a well right name to clarify data.") );
	}
	if ( StateMod_Util.isMissing(cgoto) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
			"\" well station ID is blank.",
			"Specify a well station to associate the well right.") );
	}
	else {
		// Verify that the well station is in the data set, if the network is available
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_WELL_STATIONS);
		List wesList = (List)comp2.getData();
		if ( (wesList != null) && (wesList.size() > 0) ) {
			if ( StateMod_Util.indexOf(wesList, cgoto) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
					"\" associated well (" + cgoto + ") is not found in the list of well stations.",
					"Specify a valid well station ID to associate with the well right.") );
			}
		}
	}
	if ( StateMod_Util.isMissing(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
			"\" administration number is blank.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else if ( !StringUtil.isDouble(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
			"\" administration number (" + irtem + ") is invalid.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else {
		double irtemd = Double.parseDouble(irtem);
		if ( irtemd < 0 ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
				"\" administration number (" + irtem + ") is invalid.",
				"Specify an administration number NNNNN.NNNNN.") );
		}
	}
	if ( !(dcrdivw >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id + "\" decree (" +
			StringUtil.formatString(dcrdivw,"%.2f") + ") is invalid.",
			"Specify the decree as a number >= 0.") );
	}

	return validation;
}

/**
FIXME SAM 2009-06-03 Evaluate how to call from main validate method
Check the well rights.
*/
private void validateComponent2 ( List werList, List wesList, String idpattern_Java,
	int warningCount, int warningLevel, String commandTag, CommandStatus status )
{	String routine = getClass().getName() + ".checkWellRights";
	String message;
	
	StateMod_Well wes_i = null;

	StateMod_Parcel parcel = null; // Parcel associated with a well station
	int wes_parcel_count = 0; // Parcel count for well station
	double wes_parcel_area = 0.0; // Area of parcels for well station
	int wes_well_parcel_count = 0; // Parcel (with wells) count for well station.
	double wes_well_parcel_area = 0.0; // Area of parcels with wells for well station.
	List parcel_Vector; // List of parcels for well station.
	int count = 0; // Count of well stations with potential problems.
	String id_i = null;
	List rightList = null;
	int welListSize = wesList.size();
	for ( int i = 0; i < welListSize; i++ ) {
		wes_i = (StateMod_Well)wesList.get(i);
		if ( wes_i == null ) {
			continue;
		}
		id_i = wes_i.getID();
		rightList = StateMod_Util.getRightsForStation ( id_i, werList );
		// TODO SAM 2007-01-02 Evaluate how to put this code in a separate method and share between rights and stations.
		if ( (rightList == null) || (rightList.size() == 0) ) {
			// The following is essentially a copy of code for well
			// stations. Keep the code consistent.  Note that the
			// following assumes that when reading well rights from
			// HydroBase that lists of parcels are saved with well
			// stations.  This will clobber any parcel data that
			// may have been saved at the time that well stations
			// were processed (if processed in the same commands file).
			++count;
			// Check for parcels...
			wes_parcel_count = 0;
			wes_parcel_area = 0.0;
			wes_well_parcel_count = 0;
			wes_well_parcel_area = 0.0;
			// Parcels associated with the well station
			parcel_Vector = wes_i.getParcels();
			if ( parcel_Vector != null ) {
				// Number of parcels associated with the well station
				wes_parcel_count = parcel_Vector.size();
				for ( int j = 0; j < wes_parcel_count; j++ ) {
					parcel = (StateMod_Parcel)parcel_Vector.get(j);
					// Increment parcel area associated with the well station
					if ( parcel.getArea() > 0.0 ) {
						wes_parcel_area += parcel.getArea();
					}
					if ( parcel.getWellCount() > 0 ) {
						// Count and area of parcels that have wells
						wes_well_parcel_count += parcel.getWellCount();
						wes_well_parcel_area += parcel.getArea();
					}
				}
			}
			message = "The following well station has no water rights (no irrigated parcels served by " +
				"wells) well station ID=" + id_i +
				", well name=" + wes_i.getName() +
				", collection type=" + wes_i.getCollectionType() +
				", parcels for well station=" + wes_parcel_count +
				", parcel area for well station (acres)=" + StringUtil.formatString(wes_parcel_area,"%.3f") +
				", count of wells on parcels=" + wes_well_parcel_count +
				", area of parcels with wells (acres)=" + StringUtil.formatString(wes_well_parcel_area,"%.3f");
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			/*
			status.addToLog ( CommandPhaseType.RUN,
				new WellRightValidation(CommandStatusType.WARNING,
					message, "Data may be OK if the station has no wells.  " +
						"Parcel count and area in the following table are available " +
						"only if well rights are read from HydroBase." ) );
						*/
		}
	}

	// Since well rights are determined from parcel data, print a list of
	// well rights that do not have associated yield (decree)...

	int werListSize = werList.size();
	int pos = 0; // Position in well station vector
	String wes_name = null; // Well station name
	String wes_id = null; // Well station ID
	String wer_id = null; // Well right identifier
	double decree = 0.0;
	StateMod_WellRight wer_i = null;
	int matchCount = 0;
	for ( int i = 0; i < werListSize; i++ ) {
		wer_i = (StateMod_WellRight)werList.get(i);
		wer_id = wer_i.getID();
		if ( !wer_id.matches(idpattern_Java)) {
			continue;
		}
		++matchCount;
		// Format to two digits to match StateMod output...
		decree = StringUtil.atod(StringUtil.formatString(wer_i.getDcrdivw(),"%.2f") );
		if ( decree <= 0.0 ) {
			// Find associated well station for output to print ID and name...
			pos = StateMod_Util.indexOf(wesList,wer_i.getCgoto() );
			wes_i = null;
			if ( pos >= 0 ) {
				wes_i = (StateMod_Well)wesList.get(pos);
			}
			wes_name = "";
			if ( wes_i != null ) {
				wes_id = wes_i.getID();
				wes_name = wes_i.getName();
			}
			// Format suitable for output in a list that can be copied to a spreadsheet or table.
			message = "Well right \"" + wer_id + "\" (well station " + wes_id + " \"" + wes_name +
				"\") has decree (" + decree + ") <= 0.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			/*
			status.addToLog ( CommandPhaseType.RUN,
				new WellRightValidation(CommandStatusType.FAILURE,
					message, "Verify that parcels are available for wells and check that well " +
						"right at 2-digit precision is > 0." ) );
						*/
		}
	}
	// Return values
	int [] retVals = new int[2];
	retVals[0] = matchCount;
	retVals[1] = warningCount;
	//return retVals;
}

/**
Write the well rights file.  The comments from the previous
rights file are transferred into the next one.  Also, a history is maintained
and printed in the header for the file.  Additional header comments can be added
through the new_comments parameter.
Comments for each data item will be written if provided - these are being used
for evaluation during development but are not a part of the standard file.
@param infile name of file to retrieve previous comments and history from
@param outfile name of output file to write.
@param theRights list of rights to write.
@param newComments additional comments to print to the comment section
@param writeProps Properties to control the rights.  Currently only
WriteDataComments=True/False is recognized
*/
public static void writeStateModFile(String infile, String outfile,
		List theRights, List newComments, PropList writeProps )
throws Exception {
	PrintWriter out = null;
	String routine = "StateMod_WellRight.writeStateModFile";
	
	if ( writeProps == null ) {
		// Create properties to check
		writeProps = new PropList ( "" );
	}
	String WriteDataComments = writeProps.getValue ( "WriteDataComments");
	boolean WriteDataComments_boolean = false;
	if ( (WriteDataComments != null) && WriteDataComments.equalsIgnoreCase("True") ) {
		WriteDataComments_boolean = true;
	}

	if (outfile == null) {
		String msg = "Unable to write to null filename";
		Message.printWarning(3, routine, msg);
		throw new Exception(msg);
	}
			
	Message.printStatus(2, routine, "Writing well rights to: " + outfile);

	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	try {	
		out = IOUtil.processFileHeaders(IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		String iline = null;
		String cmnt = "#>";
		String format_0 = "%-12.12s%-24.24s%-12.12s%16.16s%8.2F%8d";
		StateMod_WellRight right = null;
		List v = new Vector(6);

		out.println(cmnt);
		out.println(cmnt + "***************************************************");
		out.println(cmnt + "  StateMod Well Right File (" + theRights.size() + " rights)");
		out.println(cmnt);
		List fileSummary = getSummaryCommentList(theRights);
		for ( int i = 0; i < fileSummary.size(); i++ ) {
			out.println(cmnt + (String)fileSummary.get(i));
		}
		out.println(cmnt);
		String format_add = "";
		if ( WriteDataComments_boolean ) {
			format_add = ", 1x, i4, 1x, i4, ix, a6";
		}
		out.println(cmnt + "  Format:  (a12, a24, a12, f16.5, f8.2, i8" + format_add + ")");
		out.println(cmnt);
		out.println(cmnt + "     ID        cidvi:  Well right ID ");
		out.println(cmnt + "     Name     cnamew:  Well right name");
		out.println(cmnt + "     Struct    cgoto:  Well Structure ID associated with this right");
		out.println(cmnt + "     Admin #   irtem:  Administration number" );
		out.println(cmnt + "                       (priority, small is senior)");
		out.println(cmnt + "     Decree  dcrdivw:  Well right (cfs)");
		out.println(cmnt + "     On/Off  idvrsww:  Switch 0 = off, 1 = on");
		out.println(cmnt + "                       YYYY = on for years >= YYYY");
		out.println(cmnt + "                       -YYYY = off for years > YYYY" );
		String header1_add = "";
		String header2_add = "";
		if ( WriteDataComments_boolean ) {
			header1_add = " PYr--Cls-------PID   ";
			header2_add = "xb--exb--exb----------e";
			out.println(cmnt + " Parcel Year     Pyr:  Parcel year used for parcel/well matching");
			out.println(cmnt + "Well match class Cls:  Indicates how well matched to parcel");
			out.println(cmnt + "                       (see CDSS documentation).");
			out.println(cmnt + "Parcel ID        PID:  Parcel ID for year.");
		}
		out.println(cmnt);
		out.println(cmnt + "   ID               Name             Struct          Admin #   Decree  On/Off " + header1_add );
		out.println(cmnt + "---------eb----------------------eb----------eb--------------eb------eb------e" + header2_add );
		out.println(cmnt);
		out.println(cmnt + "EndHeader");
		out.println(cmnt);

		int num = 0;
		if (theRights != null) {
			num = theRights.size();
		}

		String comment = null;	// Comment for data item
		for (int i = 0; i < num; i++) {
			right = (StateMod_WellRight)theRights.get(i);
			if (right == null) {
				continue;
			}

			v.clear();
			v.add(right.getID());
			v.add(right.getName());
			v.add(right.getCgoto());
			v.add(right.getIrtem());
			v.add(new Double(right.getDcrdivw()));
			v.add(new Integer(right.getSwitch()));
			iline = StringUtil.formatString(v, format_0);
			if ( WriteDataComments_boolean ) {
				comment = right.getComment();
				// TODO SAM Evaluate how best to set parcel data
				comment = StringUtil.formatString(right.getParcelYear(),"%4d")+ " "+
					StringUtil.formatString(right.getParcelMatchClass(),"%4d") +" " +
					StringUtil.formatString(right.getParcelID(),"%12.12s");
				//if ( comment.length() > 0 ) {
					// Append to the line
					out.println ( iline + " " + comment );
				//}
			}
			else {
				out.println(iline);
			}
		}
	} 
	catch(Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (out != null) {
			out.close();
		}
		out = null;
	}
}

/**
Writes a list of StateMod_WellRight objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments the list of new comments to write to the header.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List data,
	List newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
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
	StateMod_WellRight right = null;
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
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
		newComments2.add(1,"StateMod well rights as a delimited list file.");
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
			right = (StateMod_WellRight)data.get(i);
			
			line[0] = StringUtil.formatString(right.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(), formats[2]).trim();
			line[3] = StringUtil.formatString(right.getIrtem(), formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrdivw(), formats[4]).trim();
			line[5] = StringUtil.formatString(right.getSwitch(), formats[5]).trim();

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
		// FIXME SAM 2009-01-12 Log?
		throw e;
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
	}
}

}