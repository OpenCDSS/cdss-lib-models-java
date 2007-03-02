//------------------------------------------------------------------------------
// StateMod_Reservoir - class derived from StateMod_Data.  Contains information 
//	read from the reservoir file.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 27 Aug 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 25 Oct 1999	CEN, RTi		Adding daily id.
// 10 Nov 1999	CEN, RTi		Added time series pointers and set/get
//					routines for ts similar to diversions
// 19 Feb 2001	Steven A. Malers, RTi	Code review.  Clean up javadoc.  Add
//					finalize.  Handle nulls and set unused
//					variables to null.  Alphabetize methods.
//					Change IO to IOUtil.  Add a switch to
//					allow printing with or without daily
//					data.  Update output header.  Change
//					area capacity output to be printed with
//					numbers 1... rather than 0...
// 02 Mar 2001	SAM, RTi		Change reservoir curve number back to
//					start with zero but make sure the
//					zero record has zeros all the way
//					across to allow interpolation.
//					Add insertAreaCapAt()to allow
//					insertion.
// 22 Aug 2001	SAM, RTi		When writing the reservoir area/capacity
//					file, if the area or capacity is less
//					than 100, write to 2 decimal points.
//					Otherwise, write as before.  This
//					prevents StateMod from complaining.
//					Print counter for curve to 3 digits.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-09	SAM, RTi		Add GeoRecord reference to allow 2-way
//					connection between spatial and StateMod
//					data.
// 2002-09-19	SAM, RTi		Use isDirty() instead of setDirty() to
//					indicate edits.
// 2002-11-01	SAM, RTi		Minor revision to add description of
//					first line to the output header.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza	Renamed from SMReservoir to 
//					StateMod_Reservoir
// 2003-06-10	JTS, RTi		* Folded dumpReservoirFile() into
//					  writeReservoirFile()
//					* Renamed parseReservoirFile() to
//					  readReservoirFile()
// 2003-06-23	JTS, RTi		Renamed writeReservoirFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readReservoirFile() to
//					readStateModFile()
// 2003-07-15	JTS, RTi		Changed to use new dataset design.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-15	SAM, RTi		* Changed GeoRecordNoSwing() to
//					  GeoRecord.
//					* Change StateMod_Climate to
//					  StateMod_ReservoirClimate.
// 2003-08-18	SAM, RTi		* Clean up the data members and method
//					  names for time series - daily were
//					  not being properly handled.
//					* Add hasXXXTS() to indicate whether the
//					  reservoir has climate data time
//					  series.
//					* Add getXXXTS() to get climate time
//					  series.
//					* Change so StateMod_ReservoirClimate
//					  uses the StateMod_Data base class for
//					  the identifiers.
// 2003-08-28	SAM, RTi		* Change water rights to be a simple
//					  Vector, not a linked list.
//					* Call setDirty() for the individual
//					  objects as well as the component.
//					* Remove data members for numbers of
//					  items - these can be determined from
//					  the data Vectors as needed.
//					* Clean up Javadoc.
//					* Alphabetize the methods.
//					* Change getRes* setRes* to remove the
//					  "Res" - it is redundant.
// 2003-09-18	SAM, RTi		Change reservoir accounts to use the
//					base class for the name and assign
//					sequential integers for the account
//					ID.
// 2003-10-10	SAM, RTi		Add disconnectRights().
// 2003-10-15	SAM, RTi		Change some initial values to agree
//					with the old SMGUI for new instance.
// 2004-06-05	SAM, RTi		* Add methods to handle collections,
//					  similar to StateCU locations.
// 2004-07-02	SAM, RTi		* Overload the constructor to indicate
//					  whether reasonable defaults should be
//					  assigned.
//					* Add getRdateChoices() and
//					  getRdateDefault() to help provide
//					  information for GUIs.
//					* Add getIresswChoices() and
//					  getIresswDefault() to help provide
//					  information for GUIs.
// 2004-07-14	JTS, RTi		* Added acceptChanges().
//					* Added changed().
//					* Added clone().
//					* Added compareTo().
//					* Added createBackup().
//					* Added restoreOriginal().
//					* Now implements Cloneable.
//					* Now implements Comparable.
//					* Clone status is checked via _isClone
//					  when the component is marked as dirty.
// 2004-09-09	SAM, RTi		When reading and writing, adjust the
//					file paths using the working directory.
// 2004-11-12	SAM, RTi		Remove "Fill #" from second line in
//					output header.
// 2005-02-01	SAM, RTi		The writeStateModFile() method was
//					automatically adding a dead storage
//					account as the last account, if dead
//					storage was specified, and it was
//					decrementing the account numbers by the
//					dead storage value.  Remove this code
//					and include options in StateDMI -
//					handling in the write method is
//					confusing.
// 2005-03-30	JTS, RTi		* Added getCollectionPartType().
//					* Added getCollectionYears().
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2005-04-19	JTS, RTi		Added writeCollectionListFile().
// 2005-05-06	SAM, RTi		Correct a couple of typos in StateMod
//					data set components for writing the
//					delimited files.
// 2006-06-13	SAM, RTi		Change the names of secondary list files
//					to be more appropriate.
// 2006-08-15	SAM, RTi		Fix so that if target time series file
//					has one time series for a reservoir,
//					then assign to the maximum target and
//					leave the minimum as null.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import java.lang.Double;
import java.lang.Integer;

import java.util.Vector;

import RTi.GIS.GeoView.GeoRecord;

import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
The Reservoir class holds data for entries in the StateMod reservoir station
file.  Secondary data classes are used in cases where lists of data are used.
*/
public class StateMod_Reservoir 
extends StateMod_Data
implements Cloneable, Comparable {

/**
date for one fill rule admin
*/
protected double	_rdate;	

/**
minimum reservoir content
*/
protected double 	_volmin;

/**
Maximum reservoir content
*/
protected double 	_volmax;

/**
maximum reservoir release
*/
protected double 	_flomax;

/**
dead storage in reservoir
*/
protected double 	_deadst;

/**
Vector of owners
*/
protected Vector	_owners;

/**
Daily id
*/
protected String	_cresdy;

/**
Vector of evap/precip stations
*/
protected Vector	_climate_Vector;

/**
vector of area capacity values
*/
protected Vector	_areacapvals;

/**
Vector of reservoir rights
*/
protected Vector	_rights;

/**
End of month content time series.
*/
protected MonthTS _content_MonthTS;

/**
End of day content time series.
*/
protected DayTS	 _content_DayTS;

/**
Minimum target time series (Monthly).
*/
protected MonthTS _mintarget_MonthTS;

/**
Maximum target time series (Monthly).
*/
protected MonthTS _maxtarget_MonthTS;

/**
Minimum target time series (Daily).
*/
protected DayTS	_mintarget_DayTS;

/**
Maximum target time series (Daily).
*/
protected DayTS	_maxtarget_DayTS;

/**
link to spatial data -- currently NOT cloned.
*/
protected GeoRecord _georecord;

// Collections are set up to be specified by year, although currently for
// reservoir collections are always the same for the full period.

/**
Types of collections.  An aggregate merges the water rights whereas
a system keeps all the water rights but just has one ID.
*/
public static String COLLECTION_TYPE_AGGREGATE = "Aggregate";
public static String COLLECTION_TYPE_SYSTEM = "System";

private String __collection_type = StateMod_Util.MISSING_STRING;

private String __collection_part_type = "Reservoir";
					// Used by DMI software - currently no
					// options.
private Vector __collection_Vector = null;
					// The identifiers for data that are
					// collected - null if not a collection
					// location.  This is actually a Vector
					// of Vector's where the
					// __collection_year is the first
					// dimension.  This is ugly but need to
					// use the code to see if it can be
					// made cleaner.

private int [] __collection_year = null;
					// An array of years that correspond to
					// the aggregate/system.  Ditches
					// currently only have one year.

/**
Construct and initialize data to reasonable defaults where appropriate.
**/
public StateMod_Reservoir()
{	this ( true );
}

/**
Construct and initialize data to reasonable defaults where appropriate.
@param initialize_defaults If true, initialize data to reasonable defaults
(e.g., zero dead storage) - this is suitable for defaults in the StateMod GUI.
If false, don't initialize data - this is suitable for filling in StateDMI.
**/
public StateMod_Reservoir ( boolean initialize_defaults ) {
	super();
	initialize ( initialize_defaults );
}

/**
Accepts any changes made inside of a GUI to this object.
*/
public void acceptChanges() {
	_isClone = false;
	_original = null;
}

/**
Add owner (account).
@param owner StateMod_ReservoirAccount to add.
*/
public void addAccount(StateMod_ReservoirAccount owner)
{	if (owner != null) {
		_owners.addElement(owner);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Add AreaCap.
@param areacap StateMod_ReservoirAreaCap to add.
*/
public void addAreaCap(StateMod_ReservoirAreaCap areacap)
{	if (areacap != null) {
		_areacapvals.addElement(areacap);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
	}
}

/**
Add climate.
@param climate StateMod_ReservoirClimate to add.
*/
public void addClimate(StateMod_ReservoirClimate climate) {
	if (climate != null) {
		_climate_Vector.addElement(climate);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Adds a right to the rights linked list
*/
public void addRight(StateMod_ReservoirRight right)
{	if ( right != null ) {
		_rights.addElement ( right );
	}
	// No need to set dirty because right is not stored in station file.
}

/**
Compares this object with its original value (generated by createBackup() upon
entering a GUI) to see if it has changed.
*/
public boolean changed() {
	if (_original == null) {
		return true;
	}
	if (compareTo(_original) == 0) {
		return false;
	}
	return true;
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_Reservoir r = (StateMod_Reservoir)super.clone();
	r._isClone = true;

	// The following are not cloned because there is no need to.  
	// The cloned values are only used for comparing between the 
	// values that can be changed in a single GUI.  The following
	// Vectors' data have their changes committed in other GUIs.
	r._climate_Vector = _climate_Vector;
	r._areacapvals = _areacapvals;
	r._rights = _rights;
	return r;
}

/**
Compares this object to another StateMod_Reservoir object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_Reservoir r = (StateMod_Reservoir)o;

	if (_rdate < r._rdate) {
		return -1;
	}
	else if (_rdate > r._rdate) {	
		return 1;
	}

	if (_volmin < r._volmin) {
		return -1;
	}
	else if (_volmin > r._volmin) {
		return 1;
	}

	if (_volmax < r._volmax) {
		return -1;
	}
	else if (_volmax > r._volmax) {
		return 1;
	}

	if (_flomax < r._flomax) {
		return -1;
	}
	else if (_flomax > r._flomax) {
		return 1;
	}

	if (_deadst < r._deadst) {
		return -1;
	}
	else if (_deadst > r._deadst) {
		return 1;
	}

	res = _cresdy.compareTo(r._cresdy);
	if (res != 0) {
		return res;
	}
	
	return 0;
}

/**
This set of routines don't actually add an element to an array.  They already
exist as part of a Vector of StateMod_ReservoirRight.  We are just connecting 
pointers.
*/
public static void connectAllRights(Vector reservoirs, Vector rights)
{	if (reservoirs == null) {
		return;
	}
	int i, num_res = reservoirs.size();

	StateMod_Reservoir res = null;
	for (i = 0; i < num_res; i++) {
		res = (StateMod_Reservoir)reservoirs.elementAt(i);
		if (res == null) {
			continue;
		}
		res.connectRights(rights);
	}
	res = null;
}

/**
Connect all reservoir-related time series to reservoir data objects.
@param reservoirs Vector of StateMod_Reservoir.
@param content_MonthTS Vector of MonthTS containing end-of-month content.
@param content_DayTS Vector of DayTS containing end-of-day content.
@param target_MonthTS Vector of MonthTS containing minimum/maximum target time
series pairs.
@param target_DayTS Vector of DayTS containing minimum/maximum target time
series pairs.
*/
public static void connectAllTS (	Vector reservoirs,
					Vector content_MonthTS,
					Vector content_DayTS,
					Vector target_MonthTS,
					Vector target_DayTS)
{	if (reservoirs == null) {
		return;
	}
	int numRes = reservoirs.size();

	StateMod_Reservoir res = null;
	for (int i = 0; i < numRes; i++) {
		res = (StateMod_Reservoir)reservoirs.elementAt(i);
		if (res == null) {
			continue;
		}
		res.connectContentMonthTS(content_MonthTS);
		res.connectContentDayTS(content_DayTS);
		res.connectTargetMonthTS(target_MonthTS);
		res.connectTargetDayTS(target_DayTS);
	}
	res = null;
}

/**
Connect the end-of-day content time series to this reservoir, using the
time series location and the reservoir identifier to make a match.
The reservoir name is also set as the time series description.
@param contentTS Vector of end-of-day content time series to search.
*/
public void connectContentDayTS ( Vector contentTS )
{	if (contentTS == null) {
		return;
	}
	int numTS = contentTS.size();
	DayTS ts = null;

	for (int i = 0; i < numTS; i++) {
		ts = (DayTS)contentTS.elementAt(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			setContentDayTS(ts);
			ts.setDescription(getName());
			break;
		}
	}
	ts = null;
}

/**
Connect the end-of-month content time series to this reservoir, using the
time series location and the reservoir identifier to make a match.
The reservoir name is also set as the time series description.
@param contentTS Vector of end-of-month content time series to search.
*/
public void connectContentMonthTS ( Vector contentTS )
{	if (contentTS == null) {
		return;
	}
	int numTS = contentTS.size();
	MonthTS ts = null;

	for (int i = 0; i < numTS; i++) {
		ts = (MonthTS)contentTS.elementAt(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			setContentMonthTS(ts);
			ts.setDescription(getName());
			break;
		}
	}
	ts = null;
}

/**
Connect the minimum and maximum target time series (daily) to the reservoir,
using the time series location and the reservoir identifier to make the match.
The time series must be in the order of min, max, min, max, etc.
@param targetTS The Vector of DayTS containing minimum and maximum targets.
*/
public void connectTargetDayTS(Vector targetTS) {
	if (targetTS == null) {
		return;
	}
	int numTS = targetTS.size();
	DayTS ts = null;

	for (int i = 0; i < numTS; i++) {
		ts = (DayTS)targetTS.elementAt(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			setMinTargetDayTS(ts);
			ts.setDescription(getName());
			// now set max
			ts = (DayTS)targetTS.elementAt(i+1);
			if (ts == null) {
				continue;
			}
			if (_id.equals(ts.getIdentifier().getLocation())) {
				setMaxTargetDayTS(ts);
				ts.setDescription(getName());
			}
			break;
		}
	}
	ts = null;
}

/**
Connect the minimum and maximum target time series (monthly) to the reservoir,
using the time series location and the reservoir identifier to make the match.
The time series must be in the order of min, max, min, max, etc.
@param targetTS The Vector of MonthTS containing minimum and maximum targets.
*/
public void connectTargetMonthTS(Vector targetTS) {
	if (targetTS == null) {
		return;
	}
	int numTS = targetTS.size();
	MonthTS ts = null, ts1 = null, ts2 = null;

	for (int i = 0; i < numTS; i++) {
		ts = (MonthTS)targetTS.elementAt(i);
		ts1 = null;
		ts2 = null;
		if (ts == null) {
			// Can't check the time series...
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			// Found a matching identifier so this is the minimum
			// or maximum target part of the min/max time series
			// pair.  If only one time series is found, set it to
			// the maximum and set the minimum to null (interpreted
			// as zeros in other code like graphs).
			ts1 = ts;
			// Now set max, which should be the next time series.
			if ( (i + 1) < numTS ) {
				ts2 = (MonthTS)targetTS.elementAt(i+1);
				if (	!_id.equals(ts2.getIdentifier().
					getLocation())) {
					// Time series is for a different
					// reservoir so reset to null...
					ts2 = null;
				}
			}
			// Now link the time series...
			if ( (ts1 == null) && (ts2 == null) ) {
				// Nothing to do...
			}
			else if ( ts2 == null ) {
				// Only one time series is specified so it is
				// the maximum...
				setMaxTargetMonthTS(ts1);
				ts1.setDescription(getName());
			}
			else {	// Have both time series...
				setMinTargetMonthTS(ts1);
				ts1.setDescription(getName());
				setMaxTargetMonthTS(ts2);
				ts2.setDescription(getName());
			} 
			break;
		}
	}
}

/**
Connect the rights to this instance of a reservoir.
@param rights Vector of all reservoir rights.
*/
public void connectRights ( Vector rights )
{	if (rights == null) {
		return;
	}
	int i;
	int num_rights = rights.size();

	StateMod_ReservoirRight right;
	for (i = 0; i < num_rights; i++) {
		right = (StateMod_ReservoirRight)rights.elementAt(i);
		if (right == null) {
			continue;
		}
		if (_id.equals(right.getCgoto())) {
			_rights.addElement ( right );
		}
	}
	right = null;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_Reservoir)_original)._isClone = false;
	_isClone = true;
}

/**
Deletes the area capacity at the given index
@param index of the area capacity to delete.
*/
public void deleteAreaCapAt(int index) {
	_areacapvals.removeElementAt(index);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
	}
}

/**
Deletes the climate at the given index
@param index of the climate to delete
*/
public void deleteClimateAt(int index) {
	_climate_Vector.removeElementAt(index);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
	}
}

/**
Deletes the owner at the given index
@param index of the owner to delete
*/
public void deleteAccountAt(int index) {
	_owners.removeElementAt(index);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
	}
}

// REVISIT - need to decide in the GUI if the right is actually removed from
// the main list.
/**
Remove right from list.  A comparison on the ID is made.
@param right Right to remove.  Note that the right is only removed from the
list for this diversion and must also be removed from the main diversion right
list.
*/
public void disconnectRight ( StateMod_ReservoirRight right )
{	if (right == null) {
		return;
	}
	int size = _rights.size();
	StateMod_ReservoirRight right2;
	// Assume that more than on instance can exist, even though this is
	// not allowed...
	for ( int i = 0; i < size; i++ ) {
		right2 = (StateMod_ReservoirRight)_rights.elementAt(i);
		if ( right2.getID().equalsIgnoreCase(right.getID()) ) {
			_rights.removeElementAt(i);
		}
	}
}

/**
Disconnect all rights.
*/
public void disconnectRights ()
{	_rights.removeAllElements();
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_owners = null;
	_climate_Vector = null;
	_areacapvals = null;
	_cresdy = null;
	_rights = null;
	_content_MonthTS = null;
	_content_DayTS = null;
	_mintarget_MonthTS = null;
	_maxtarget_MonthTS = null;
	_mintarget_DayTS = null;
	_maxtarget_DayTS = null;
	_georecord = null;
	super.finalize();
}

/**
Retrieve the owner at a particular index.
*/
public StateMod_ReservoirAccount getAccount(int index) {
	return((StateMod_ReservoirAccount)_owners.elementAt(index));
}

/**
Get all owners.
*/
public Vector getAccounts()
{	return _owners;
}

/**
Return the area capacity at a particular index.
*/
public StateMod_ReservoirAreaCap getAreaCap(int index)
{	return((StateMod_ReservoirAreaCap)_areacapvals.elementAt(index));
}

/**
Return all the area capacity data.
*/
public Vector getAreaCaps ()
{	return _areacapvals;
}

/**
Return the climate station at a particular index.
*/
public StateMod_ReservoirClimate getClimate(int index) {
	if (_climate_Vector.isEmpty() || index >= _climate_Vector.size()) {
		return(new StateMod_ReservoirClimate());
	}
	return((StateMod_ReservoirClimate)_climate_Vector.elementAt(index));
}

/**
Return the climate station asignments.
*/
public Vector getClimates() {
	return _climate_Vector;
}

/**
Return the collection part ID list for the specific year.  For wells, only one
aggregate/system list is currently supported so the same information is returned
regardless of the year value.
@return the list of collection part IDS, or null if not defined.
*/
public Vector getCollectionPartIDs ( int year )
{	if ( __collection_Vector.size() == 0 ) {
			return null;
	}
	//if ( __collection_part_type.equalsIgnoreCase("Reservoir") ) {
		// The list of part IDs will be the first and only list...
		return (Vector)__collection_Vector.elementAt(0);
	//}
	/* Not supported
	else if ( __collection_part_type.equalsIgnoreCase("Parcel") ) {
		// The list of part IDs needs to match the year.
		for ( int i = 0; i < __collection_year.length; i++ ) {
			if ( year == __collection_year[i] ) {
				return (Vector)__collection_Vector.elementAt(i);
			}
		}
	}
	return null;
	*/
}

/**
Returns the collection part type ("Reservoir").
@return the collection part type ("Reservoir").
*/
public String getCollectionPartType() {
	return __collection_part_type;
}

/**
Return the collection type, "Aggregate" or "System".
@return the collection type, "Aggregate" or "System".
*/
public String getCollectionType()
{	return __collection_type;
}

/**
Returns the collection years.
@return the collection years.
*/
public int[] getCollectionYears() {
	return __collection_year;
}

/**
Return end-of-day content time series.
@return end-of-day content time series.
*/
public DayTS getContentDayTS()
{ 	return _content_DayTS;
}

/**
Return end-of-month content time series.
@return end-of-month content time series.
*/
public MonthTS getContentMonthTS()
{ 	return _content_MonthTS;
}

/**
Return cresdy
*/
public String getCresdy() {
	return _cresdy;
}

/**
Return the dead storage in reservoir.
*/
public double getDeadst() {
	return _deadst;
}

/**
Return the maximum reservoir release.
*/
public double getFlomax() {
	return _flomax;
}

/**
Get the geographical data associated with the diversion.
@return the GeoRecord for the diversion.
*/
public GeoRecord getGeoRecord() {
	return _georecord;
}

/**
Return a list of on/off switch option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and
"0 - Off", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getIresswChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "0 - Off" );	// Possible options are listed here.
	v.addElement ( "1 - On, do not store above reservoir targets" );
	v.addElement ( "2 - 1 and adjust volume, etc. by dead storage" );
	v.addElement ( "3 - On, do store above reservoir targets" );
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
Return the default on/off switch choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default reservoir replacement choice.
*/
public static String getIresswDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return "1 - On, do not store above reservoir targets";
	}
	else {	return "1";
	}
}

/**
Get the last right associated with diversion.
*/
public StateMod_ReservoirRight getLastRight()
{	if ( (_rights == null) || (_rights.size() == 0) ) {
		return null;
	}
	return (StateMod_ReservoirRight)_rights.elementAt(_rights.size() - 1);
}

/**
Get the maximum target time series (daily).
@return the maximum target time series (daily).
*/
public DayTS getMaxTargetDayTS() {
	return _maxtarget_DayTS;
}

/**
Get the maximum target time series (monthly).
@return the maximum target time series (monthly).
*/
public MonthTS getMaxTargetMonthTS() {
	return _maxtarget_MonthTS;
}

/**
Get the minimum target time series (daily).
@return the minimum target time series (daily).
*/
public DayTS getMinTargetDayTS() {
	return _mintarget_DayTS;
}

/**
Get the minimum target time series (monthly).
@return the minimum target time series (monthly).
*/
public MonthTS getMinTargetMonthTS() {
	return _mintarget_MonthTS;
}

/**
Return the number of owners.
*/
public int getNowner() {
	return _owners.size();
}

/**
Return the number of area capacity values.
*/
public int getNrange() {
	return _areacapvals.size();
}

/**
Return the number of evaporation time series for the reservoir.
@param tslist The Vector of monthly evaporation data to check.
@param check_ts If true, get the count of non-null time series (the reservoir
may reference evaporation station indentifiers but the identifiers may not
actually exist).
@return the number of evaporation time series for the reservoir.
*/
public int getNumEvaporationMonthTS ( Vector tslist, boolean check_ts )
{	// Loop through the evaporation data for this reservoir.  For each
	// referenced evaporation station, if a matching time series is
	// found, increment the count.
	int nsta = _climate_Vector.size();
	int nts = 0;
	TS ts;
	int pos = 0;
	StateMod_ReservoirClimate sta = null;
	for ( int i = 0; i < nsta; i++ ) {
		sta = (StateMod_ReservoirClimate)_climate_Vector.elementAt(i);
		if ( sta.getType() != StateMod_ReservoirClimate.CLIMATE_EVAP ) {
			Message.printStatus ( 1, "", "SAMX climate " +
				sta.getID() + " is not evap" );
			continue;
		}
		pos = TSUtil.indexOf ( tslist, sta.getID(), "Location", 1 );
		Message.printStatus ( 1, "", "SAMX climate " +
				sta.getID() + " pos is " + pos );
		if ( pos >= 0 ) {
			if ( check_ts ) {
				// Make sure that the time series has data...
				ts = (TS)tslist.elementAt(pos);
				if ( (ts != null) && ts.hasData() ) {
					Message.printStatus ( 1, "",
					"SAMX ts has data." );
					++nts;
				}
				else	Message.printStatus ( 1, "",
					"SAMX ts has NO data." );
			}
			else {	// Just a count of the evaporation time
				// series...
				++nts;
			}
		}
	}
	return nts;
}

/**
Return the number of precipitation time series for the reservoir.
@param tslist The Vector of monthly precipitation data to check.
@param check_ts If true, get the count of non-null time series (the reservoir
may reference precipitation station indentifiers but the identifiers may not
actually exist).
@return the number of precipitation time series for the reservoir.
*/
public int getNumPrecipitationMonthTS ( Vector tslist, boolean check_ts )
{	// Loop through the precipitation data for this reservoir.  For each
	// referenced precipitation station, if a matching time series is
	// found, then increment the count.
	int nsta = _climate_Vector.size();
	int nts = 0;
	TS ts;
	int pos = 0;
	StateMod_ReservoirClimate sta = null;
	for ( int i = 0; i < nsta; i++ ) {
		sta = (StateMod_ReservoirClimate)_climate_Vector.elementAt(i);
		if ( sta.getType() != StateMod_ReservoirClimate.CLIMATE_PTPX ) {
			continue;
		}
		pos = TSUtil.indexOf ( tslist, sta.getID(), "Location", 1 );
		if ( pos >= 0 ) {
			if ( check_ts ) {
				ts = (TS)tslist.elementAt(pos);
				// Make sure that the time series has data...
				if ( (ts != null) && ts.hasData() ) {
					++nts;
				}
			}
			else {	// Just a count of the evaporation time
				// series...
				++nts;
			}
		}
	}
	return nts;
}

/**
Return the number of rights for this reservoir.
*/
public int getNumrights()
{	return _rights.size();
}

/**
Return the date for one fill rule admin.
*/
public double getRdate() {
	return _rdate;
}

/**
Return a list of one fill rule (rdate) switch option strings, for use in GUIs.
The options are of the form "-1" if include_notes is false and
"-1 - Do not adminisiter one fill rule", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter
values.
*/
public static Vector getRdateChoices ( boolean include_notes )
{	Vector v = new Vector(2);
	v.addElement ( "-1 - Do not administer the one fill rule" );
	v.addElement ( "1 - January" );	// Possible options are listed here.
	v.addElement ( "2 - February" );
	v.addElement ( "3 - March" );
	v.addElement ( "4 - April" );
	v.addElement ( "5 - May" );
	v.addElement ( "6 - June" );
	v.addElement ( "7 - July" );
	v.addElement ( "8 - August" );
	v.addElement ( "9 - September" );
	v.addElement ( "10 - October" );
	v.addElement ( "11 - November" );
	v.addElement ( "12 - December" );
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
Return the default one fill rule switch choice.  This can be used by GUI code
to pick a default for a new diversion.
@return the default reservoir replacement choice.
*/
public static String getRdateDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return ( "-1 - Do not administer the one fill rule" );
	}
	else {	return "-1";
	}
}

/**
Return the right associated with the given index.  If index
number of rights don't exist, null will be returned.
@param index desired right index
*/
public StateMod_ReservoirRight getRight(int index)
{	if ( (index < 0) || (index >= _rights.size()) ) {
		return null;
	}
	else {	return (StateMod_ReservoirRight)_rights.elementAt(index);
	}
}
/**
get the rights
*/
public Vector getRights()
{	return _rights;
}

/**
Return the maximum reservoir content.
*/
public double getVolmax() {
	return _volmax;
}

/**
Return the minimum reservoir content.
*/
public double getVolmin() {
	return _volmin;
}

/**
Initialize data members.
@param initialize_defaults If true, initialize data to reasonable defaults
(e.g., zero dead storage) - this is suitable for defaults in the StateMod GUI.
If false, don't initialize data - this is suitable for filling in StateDMI.
*/
private void initialize ( boolean initialize_defaults )
{	_smdata_type = StateMod_DataSet.COMP_RESERVOIR_STATIONS;
	_owners = new Vector();
	_climate_Vector = new Vector();
	_areacapvals = new Vector();
	_rights = new Vector();
	_content_MonthTS = null;
	_content_DayTS = null;
	_mintarget_MonthTS = null;
	_maxtarget_MonthTS = null;
	_mintarget_DayTS = null;
	_maxtarget_DayTS = null;
	_georecord = null;
	if ( initialize_defaults ) {
		_cresdy = "0";		// Use monthly TS for daily
		_switch = 1;		// In base class
		_rdate	= -1;
		_volmin	= 0;
		_volmax	= 0;
		_flomax	= 99999.0;	// As per old SMGUI new reservoir
		_deadst	= 0;
	}
	else {	_cresdy = "";
		_rdate = StateMod_Util.MISSING_INT;
		_volmin	= StateMod_Util.MISSING_DOUBLE;
		_volmax	= StateMod_Util.MISSING_DOUBLE;
		_flomax	= StateMod_Util.MISSING_DOUBLE;
		_deadst	= StateMod_Util.MISSING_DOUBLE;
	}
}

/**
Indicate whether the reservoir is a collection (an aggregate or system).
@return true if the reservoir is an aggregate or system.
*/
public boolean isCollection()
{	if ( __collection_Vector == null ) {
		return false;
	}
	else {	return true;
	}
}

/**
Insert AreaCap
*/
public void insertAreaCapAt(StateMod_ReservoirAreaCap areacap, int i) {
	if (areacap != null) {
		_areacapvals.insertElementAt(areacap, i);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Read reservoir information in and store in a Vector.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static Vector readStateModFile(String filename)
throws Exception
{	String routine = "StateMod_Reservoir.readStateModFile";
	Vector theReservoirs = new Vector();
	String iline = null;
	Vector v = new Vector(9);
	int [] format_0 = {	StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING };
	int [] format_0w = {	12,
				24,
				12,
				8,
				8,
				1,
				12 };
	int [] format_1 = {	StringUtil.TYPE_SPACE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER };
	int [] format_1w = {	24,
				8,
				8,
				8,
				8,
				8,
				8,
				8,
				8 };
	int [] format_2 = {	StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER };
	int [] format_2w = {	12,
				12,
				8,
				8,
				8,
				8 };
	int [] format_3 = {	StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE };
	int [] format_3w = {	24,
				12,
				8 };
	int [] format_4 = {	StringUtil.TYPE_SPACE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE };
	int [] format_4w = {	24,
				8,
				8,
				8 };
	BufferedReader in = null;
	StateMod_Reservoir aReservoir = null;
	StateMod_ReservoirAccount anAccount = null;
	StateMod_ReservoirClimate anEvap = null;
	StateMod_ReservoirClimate aPtpx = null;
	int i = 0;

	if (Message.isDebugOn) {
		Message.printDebug(10, routine, 
		"in SMParseResFile reading file: " 
		+ filename);
	}
	int line_count = 0;
	try {	in = new BufferedReader(new FileReader(
		IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine())!= null) {
			++line_count;
			// check for comments
			if (iline.startsWith("#")|| iline.trim().length()==0)
				continue;

			// allocate new reservoir node
			aReservoir = new StateMod_Reservoir();

			// line 1
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, 
					"line 1: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aReservoir.setID(((String)v.elementAt(0)).trim());
			aReservoir.setName(((String)v.elementAt(1)).trim());
			aReservoir.setCgoto(((String)v.elementAt(2)).trim());
			aReservoir.setSwitch((Integer)v.elementAt(3));
			aReservoir.setRdate((Double)v.elementAt(4));
			aReservoir.setCresdy(((String)v.elementAt(5)).trim());

			// line 2
			iline = in.readLine();
			++line_count;
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, 
					"line 2: " + iline);
			}
			StringUtil.fixedRead(iline, format_1, format_1w, v);
			aReservoir.setVolmin(((Double)v.elementAt(0)));
			aReservoir.setVolmax(((Double)v.elementAt(1)));
			aReservoir.setFlomax(((Double)v.elementAt(2)));
			aReservoir.setDeadst(((Double)v.elementAt(3)));
			int nowner =((Integer)v.elementAt(4)).intValue();
			int nevap =((Integer)v.elementAt(5)).intValue();
			int nptpx =((Integer)v.elementAt(6)).intValue();
			int nrange =((Integer)v.elementAt(7)).intValue();

			// get the owner's information
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, 
					"Number of owners: " + nowner);
			}
			for (i = 0; i < nowner; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_2,
					format_2w, v);
				anAccount = new StateMod_ReservoirAccount();
				anAccount.setID ( "" + (i + 1) );
				anAccount.setName(
					((String)v.elementAt(0)).trim());
				anAccount.setOwnmax( ((Double)v.elementAt(1)));
				anAccount.setCurown( ((Double)v.elementAt(2)));
				anAccount.setPcteva( ((Double)v.elementAt(3)));
				anAccount.setN2own( ((Integer)v.elementAt(4)));
				aReservoir.addAccount(anAccount);
			}

			// get the evaporation information
			for (i = 0; i < nevap; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_3,
					format_3w, v);
				anEvap = new StateMod_ReservoirClimate();
				anEvap.setID( ((String)v.elementAt(0)).trim());
				anEvap.setType(
				StateMod_ReservoirClimate.CLIMATE_EVAP);
				anEvap.setWeight(((Double)v.elementAt(1)));
				aReservoir.addClimate(anEvap);
			}
			
			// get the precipitation information
			for (i = 0; i < nptpx; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_3,
					format_3w, v);
				aPtpx = new StateMod_ReservoirClimate();
				aPtpx.setID( ((String)v.elementAt(0)).trim());
				aPtpx.setType(
				StateMod_ReservoirClimate.CLIMATE_PTPX);
				aPtpx.setWeight(((Double)v.elementAt(1)));
				aReservoir.addClimate(aPtpx);
			}
			
			// get the area capacity information
			for (i = 0; i < nrange; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_4,
					format_4w, v);
				StateMod_ReservoirAreaCap anAreaCap = 
					new StateMod_ReservoirAreaCap();
				anAreaCap.setConten(
					((Double)v.elementAt(0)));
				anAreaCap.setSurarea(
					((Double)v.elementAt(1)));
				anAreaCap.setSeepage(
					((Double)v.elementAt(2)));
				aReservoir.addAreaCap(anAreaCap);
			}

			// add the reservoir to the vector of reservoirs
			theReservoirs.addElement(aReservoir);
		}
	} catch (Exception e) {
		routine = null;
		iline = null;
		v = null;
		format_0 = null;
		format_0w = null;
		format_1 = null;
		format_1w = null;
		format_2 = null;
		format_2w = null;
		format_3 = null;
		format_3w = null;
		format_4 = null;
		format_4w = null;
		if (in != null) {
			in.close();
		}
		in = null;
		aReservoir = null;
		anAccount = null;
		anEvap = null;
		aPtpx = null;
		Message.printWarning(2, routine,
		"Error reading reservoir stations in line " + line_count );
		throw e;
	}

	routine = null;
	iline = null;
	v = null;
	format_0 = null;
	format_0w = null;
	format_1 = null;
	format_1w = null;
	format_2 = null;
	format_2w = null;
	format_3 = null;
	format_3w = null;
	format_4 = null;
	format_4w = null;
	if (in != null) {
		in.close();
	}
	in = null;
	aReservoir = null;
	anAccount = null;
	anEvap = null;
	aPtpx = null;
	return theReservoirs;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_Reservoir r = (StateMod_Reservoir)_original;
	super.restoreOriginal();
	_rdate = r._rdate;
	_volmin = r._volmin;
	_volmax = r._volmax;
	_flomax = r._flomax;
	_deadst = r._deadst;
	_cresdy = r._cresdy;
	_isClone = false;
	_original = null;
}

/**
Set owners (accounts).  The new list may have the same or different objects than
the original list.  A comparison of objects is made to verify whether any
data are dirty.
@param owners Vector of StateMod_ReservoirAccount to set.  This should be a
non-null Vector.
*/
public void setAccounts ( Vector owners )
{	// All of the following work id done to make sure the dirty flag on the
	// component and individual objects is correct.  We could just delete
	// all existing accounts and re-add, but we don't know for sure that
	// the dirty flags would be correct.
/* REVISIT
	int size = owners.size();
	StateMod_ReservoirAccount account;
	// Array to track whether all old accounts are accounted for.  If the
	// number is not the same, then we need to set the component dirty,
	// regardless of whether any individual objects have changed.
	boolean [] old_matched = null;
	int old_size = _owners.size();
	if ( old_size > 0 ) {
		old_matched = new boolean[old_size];
		for ( int i = 0; i < old_size; i++ ) {
			old_match[i] = false;
		}
	}
	int pos;
	for ( int i = 0; i < size; i++ ) {
		account = (StateMod_ReservoirAccount)owners.elementAt(i);
		pos = StateMod_Util.indexOf ( _owners, account.getID() );
		// Mark that we have checked the old account...
		if ( pos >= 0 ) {
			old_matched[pos] = true;
		}
		// ARG - sam does not have time for this - REVISIT!!!
	}
*/
	// Finally do the assignment...
	_owners = owners;
}

// REVISIT - need to check dirty flag
/**
Set the area capacity vector.
*/
public void setAreaCaps ( Vector areacapvals )
{	_areacapvals = areacapvals ;
}

// REVISIT - need to check dirty flag
/**
Sets the climate station vector.
*/
public void setClimates ( Vector climates ) {
	_climate_Vector = climates;
}

/**
Set the collection list for an aggregate/system.  It is assumed that the
collection applies to all years of data.
@param ids The identifiers indicating the locations to collection.
*/
public void setCollectionPartIDs ( Vector ids )
{	if ( __collection_Vector == null ) {
		__collection_Vector = new Vector ( 1 );
		__collection_year = new int[1];
	}
	else {	// Remove the previous contents...
		__collection_Vector.removeAllElements();
	}
	// Now assign...
	__collection_Vector.addElement ( ids );
	__collection_year[0] = 0;
}

/**
Set the collection type.
@param collection_type The collection type, either "Aggregate" or "System".
*/
public void setCollectionType ( String collection_type )
{	__collection_type = collection_type;
}

/**
Set the end-of-day content time series.
@param ts the end-of-day content time series.
*/
public void setContentDayTS(DayTS ts)
{	_content_DayTS = ts;
}

/**
Set the end-of-month content time series.
@param ts the end-of-month content time series.
*/
public void setContentMonthTS(MonthTS ts) {
	_content_MonthTS = ts;
}

/**
set cresdy
*/
public void setCresdy(String cresdy) {
	if ( (cresdy != null) && !cresdy.equals(_cresdy)) {
		_cresdy = cresdy;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Set the dead storage in reservoir.
*/
public void setDeadst(double deadst) {
	if (deadst != _deadst) {
		_deadst = deadst;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Set the dead storage in reservoir.
*/
public void setDeadst(Double deadst) {
	setDeadst(deadst.doubleValue());
}

/**
Set the dead storage in reservoir.
*/
public void setDeadst(String deadst) {
	if (deadst != null) {
		setDeadst(StringUtil.atod(deadst.trim()));
	}
}

/**
Set the maximum reservoir release.
*/
public void setFlomax(double flomax) {
	if (flomax != _flomax) {
		_flomax = flomax;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Set the maximum reservoir release.
*/
public void setFlomax(Double flomax) {
	setFlomax(flomax.doubleValue());
}

/**
Set the maximum reservoir release.
*/
public void setFlomax(String flomax) {
	if (flomax != null) {
		setFlomax(StringUtil.atod(flomax.trim()));
	}
}

/**
Set the geographic information object associated with the diversion.
@param georecord Geographic record associated with the diversion.
*/
public void setGeoRecord(GeoRecord georecord) {
	_georecord = georecord;
}

/**
Set the maximum target time series (daily).
@param ts maximum target time series (daily).
*/
public void setMaxTargetDayTS(DayTS ts)
{	_maxtarget_DayTS = ts;
}

/**
Set the maximum target time series (monthly).
@param ts maximum target time series (monthly).
*/
public void setMaxTargetMonthTS(MonthTS ts)
{	_maxtarget_MonthTS = ts;
}

/**
Set the minimum target time series (daily).
@param ts minimum target time series (daily).
*/
public void setMinTargetDayTS(DayTS ts)
{	_mintarget_DayTS = ts;
}

/**
Set the minimum target time series (monthly).
@param ts minimum target time series (monthly).
*/
public void setMinTargetMonthTS(MonthTS ts)
{	_mintarget_MonthTS = ts;
}

/**
Set the date for one fill rule admin.
The value 0 is meaninging(??)- should be 1-12 or -1 to signify do not 
administer rule
*/
public void setRdate(double rdate) {
	if ( rdate == 0 ) {
		rdate = -1;
	}

	if (rdate != _rdate) {
		_rdate = rdate;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Set the date for the one fill rule.
*/
public void setRdate(Double rdate) {
	setRdate(rdate.doubleValue());
}

/**
Set the date for the one fill rule.
*/
public void setRdate(String rdate) {
	if (rdate != null) {
		setRdate(StringUtil.atod(rdate.trim()));
	}
}

// REVISIT - need to check dirty correctly
/**
Set the rights
*/
public void setRights ( Vector rights )
{	_rights = rights;
}

/**
Set the maximum reservoir content.
*/
public void setVolmax(double volmax) {
	if (volmax != _volmax) {
		_volmax = volmax;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Set the maximum reservoir content.
*/
public void setVolmax(Double volmax) {
	setVolmax(volmax.doubleValue());
}

/**
Set the maximum reservoir content.
*/
public void setVolmax(String volmax) {
	if (volmax != null) {
		setVolmax(StringUtil.atod(volmax.trim()));
	}
}

/**
Set the minimum reservoir content.
*/
public void setVolmin(double volmin) {
	if (volmin != _volmin) {
		_volmin = volmin;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(
			StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Set the minimum reservoir content.
*/
public void setVolmin(Double volmin) {
	setVolmin(volmin.doubleValue());
}

/**
Set the minimum reservoir content.
*/
public void setVolmin(String volmin) {
	if (volmin != null) {
		setVolmin(StringUtil.atod(volmin.trim()));
	}
}

/**
Write reservoirs information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theReservoirs vector of reservoirs to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/

public static void writeStateModFile(String infile, String outfile, 
Vector theReservoirs, String[] newComments)
throws Exception {
	writeStateModFile(infile, outfile, theReservoirs, newComments, true);
}

/**
Write reservoirs information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theReservoirs vector of reservoirs to print
@param newComments addition comments which should be included in history
@param useDailyData whether to use daily data
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile, 
Vector theReservoirs, String[] newComments, boolean useDailyData)
throws Exception {
	String routine = "StateMod_Reservoirs.writeStateModFile";
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;

	if (Message.isDebugOn) {
		Message.printDebug(1, routine, 
		"in writeStateModFile printing file: " + outfile);
	}

	try {	
	out = IOUtil.processFileHeaders(
		IOUtil.getPathUsingWorkingDir(infile),
		IOUtil.getPathUsingWorkingDir(outfile), 
		newComments, comment_str, ignore_comment_str, 0);

	String iline = null;
	String cmnt = "#>";
	String format_0 = null;
	if (useDailyData) {
		format_0 = "%-12.12s%-24.24s%-12.12s%8d%#8.0f %-12.12s";
	}
	else {	
		format_0 = "%-12.12s%-24.24s%-12.12s%8d%#8.0f";
	}
	String format_1 =
		"                        %#8.0f%#8.0f%#8.0f%#8.0f%8d%8d%8d%8d";
	String format_2 = "            %-12.12s%#8.0f%#8.0f%8.0f%8d";
	// TODO SAM 2007-03-01 Evaluate use
	//String format_3 = "            %-12.12s%#8.0f%#8.0f%8.0f%8d";
	String format_4 = "            %-12.12s%-12.12s%#8.0f";
	//String format_5 = "            %-12.12s%#8.0f%8.0f%8.0f";
	String desc = null;
	StateMod_Reservoir res = null;
	StateMod_ReservoirAreaCap ac = null;
	StateMod_ReservoirAccount own = null;
	StateMod_ReservoirClimate clmt = null;
	String ch1 = null;
	Vector v = new Vector(6);	
	Vector ownv = null;
	Vector climatev = null;
	Vector areacapv = null;

	int i,j=0;
	
	// print out header
	out.println(cmnt);
	out.println(cmnt +
		" *********************************"
		+ "****************************");
	out.println(cmnt + "  Reservoir Station file");
	out.println(cmnt);
	out.println(cmnt
		+ "  Card 1   format:  (a12, a24, a12, i8, f8.0, 1x, a12)");
	out.println(cmnt);
	out.println(cmnt
		+ "  ID       cresid:  Reservoir Id");
	out.println(cmnt
		+ "  Name     resnam:  Reservoir name");
	out.println(cmnt
		+ "  Riv ID    cgoto:  Node where Reservoir is located");
	out.println(cmnt
		+ "  On/Off   iressw:  Switch 0 = off, 1 = on");
	out.println(cmnt
		+ "  Admin #   rdate:  Administration date for 1 fill rule");
	out.println(cmnt
		+ "  Daily ID cresdy:  Identifier for daily time series.");
	out.println(cmnt);
	out.println(cmnt
		+ "  Card 2 format:  (24x, 4f8.0, 4i8)");
	out.println(cmnt);
	out.println(cmnt
		+ "  VolMin   volmin:  Min storage (ac-ft)");
	out.println(cmnt
		+ "  VolMax   volmax:  Max storage (ac-ft)");
	out.println(cmnt
		+ "  FloMax   flomax:  Max discharge (cfs)");
	out.println(cmnt
		+ "  DeadSt   deadst:  Dead storage (ac-ft)");
	out.println(cmnt
		+ "  NumOwner nowner:  Number of owners");
	out.println(cmnt
		+ "  NumEva   nevapo:  Number of evaporation stations");
	out.println(cmnt
		+ "  NumPre   nprecp:  Number of precipitation stations");
	out.println(cmnt
		+ "  NumTable nrange:  Number of area capacity values");
	out.println(cmnt);
	out.println(cmnt
		+ "     Card 3 format:  (12x, a12, 3f8.0, i8)");
	out.println(cmnt);
	out.println(cmnt
		+ "  OwnName  ownnam:  Owner name");
	out.println(cmnt
		+ "  OwnMax   ownmax:  Maximum storage for "
		+ "that owner (ac-ft)");
	out.println(cmnt
		+ "  Sto-1    curown:  Initial storage for "
		+ "that owner (ac-ft)");
	out.println(cmnt
		+ "  EvapTyp  pcteva:  Evaporation distribution");
	out.println(cmnt
		+ "  FillTyp   n2own:  Ownership type 1=First "
		+ "fill; 2=Second fill");
	out.println(cmnt);
	out.println(cmnt
		+ "  Card 4  format:  (24x, a12, f8.0)");
	out.println(cmnt);
	out.println(cmnt
		+ "  Evap ID  cevar:  Evaporation station");
	out.println(cmnt
		+ "  EvapWt  weigev:  Evaporation station weight (%%)");
	out.println(cmnt);
	out.println(cmnt
		+ "  Card 5 format:  (24x, a12, f8.0)");
	out.println(cmnt);
	out.println(cmnt
		+ "  Prec ID   cprer:  Precipitation station");
	out.println(cmnt
		+ "  PrecWt   weigpr:  Precipitation station weight (%%)");
	out.println(cmnt);
	out.println(cmnt
		+ "  Card 6 format:  (24x, 3f8.0)");
	out.println(cmnt
		+ "     Cont     conten:  Content (ac-ft)");
	out.println(cmnt
		+ "     Area    surarea:  Area (ac)");
	out.println(cmnt
		+ "     Seep    seepage:  Seepage (ac-ft)");
	out.println(cmnt);
	out.println(cmnt
		+ " *****************************************"
		+ "********************");
	out.println(cmnt);

	out.println(cmnt
		+ "    ID              Name              Node    "
		+ " On/Off  RDate       DailyID ");
	out.println(cmnt
		+ "---------eb----------------------eb----------e"
		+ "b------eb------exb----------e");
	out.println(cmnt
		+ "                       VolMin  VolMax  FloMax "
		+ " DeadSt NumOwner NumEva  NumPre NumTable");
	out.println(cmnt
		+ "xxxxxxxxxxxxxxxxxxxxxxb------eb------eb------e"
		+ "b------eb------eb------eb------eb------e");
	out.println(cmnt
		+ "                         OwnName   OwnMax   Sto-1 "
		+ "EvapTyp FillTyp");
	out.println(cmnt
		+ "xxxxxxxxxxxxxxxxxxxxxxb----------eb------eb------e"
		+ "b------eb------e");
	out.println(cmnt
		+ "                        Evap Id    EvapWt ");
	out.println(cmnt
		+ "xxxxxxxxxxxxxxxxxxxxxxb----------eb------e");
	out.println(cmnt
		+ "                        Prec Id    PrecWt ");
	out.println(cmnt
		+ "xxxxxxxxxxxxxxxxxxxxxxb----------eb------e");
	out.println(cmnt
		+ "                        Cont    Area    Seep  ");
	out.println(cmnt
		+ "xxxxxxxxxxxxxxxxxxxxxxb------eb------eb------e");
	out.println(cmnt + "EndHeader");
	out.println(cmnt);

	int num = 0;
	if (theReservoirs != null) {
		num = theReservoirs.size();
	}
	int nevap, nptpx, nareacap, nclmt, nowner;
	for (i = 0; i < num; i++) {
		res =(StateMod_Reservoir)theReservoirs.elementAt(i);
		if (res == null) {
			continue;
		}

		v.removeAllElements();
		v.addElement(res.getID());
		v.addElement(res.getName());
		v.addElement(res.getCgoto());
		v.addElement(new Integer(res.getSwitch()));
		v.addElement(new Double(res.getRdate()));
		if (useDailyData) {
			v.addElement(res.getCresdy());
		}
		iline = StringUtil.formatString(v, format_0);
		out.println(iline);

		// print reservoir statics: min, max, maxrelease, dead storage,
		// #owners, #evaps ...
		// count the number climate stations which are evap vs precip
		nevap = StateMod_ReservoirClimate.getNumEvap(
			res.getClimates());
		nptpx = StateMod_ReservoirClimate.getNumPrecip(
			res.getClimates());
		if (Message.isDebugOn) {
			Message.printDebug(50, routine, "nevap: " + nevap + 
			" nptpx: " + nptpx);
		}
		v.removeAllElements();
		v.addElement(new Double(res.getVolmin()));
		v.addElement(new Double(res.getVolmax()));
		v.addElement(new Double(res.getFlomax()));
		v.addElement(new Double(res.getDeadst()));
		v.addElement(new Integer(res.getNowner()));
		v.addElement(new Integer(nevap));
		v.addElement(new Integer(nptpx));
		v.addElement(new Integer(res.getNrange()));
		iline = StringUtil.formatString(v, format_1);
		out.println(iline);

		// print the owner information 
		ownv = res.getAccounts();
		nowner = ownv.size();
		for (j = 0; j < nowner; j++) {
			own = (StateMod_ReservoirAccount)ownv.elementAt(j);
			if (own == null) {
				out.println();
				continue;
			}
			desc = own.getName();
			if (desc.length()== 0) {
				desc = "Account " + (j + 1);
			}
			v.removeAllElements();
			v.addElement(desc);
			v.addElement(new Double(own.getOwnmax()));
			v.addElement(new Double(own.getCurown()));
			v.addElement(new Double(own.getPcteva()));
			v.addElement(new Integer(own.getN2own()));
			iline = StringUtil.formatString(v, format_2);
			out.println(iline);
		}

		// print the evap information
		climatev = res.getClimates();
		nclmt = climatev.size();
		for (j = 0; j < nclmt; j++) {
			clmt =(StateMod_ReservoirClimate)climatev.elementAt(j);
			if (clmt == null) {
				out.println();
				continue;
			}
			if (	clmt.getType()==
				StateMod_ReservoirClimate.CLIMATE_EVAP) {
				v.removeAllElements();
				v.addElement("Evaporation");
				v.addElement(clmt.getID());
				v.addElement(new Double(clmt.getWeight()));
				iline = StringUtil.formatString(v, format_4);
				out.println(iline);
			}
		}

		// Print the precip information
		for (j = 0; j < nclmt; j++) {
			clmt =(StateMod_ReservoirClimate)climatev.elementAt(j);
			if (clmt == null) {
				out.println();
				continue;
			}
			if (	clmt.getType()==
				StateMod_ReservoirClimate.CLIMATE_PTPX) {
				v.removeAllElements();
				v.addElement("Precipitatn");
				v.addElement(clmt.getID());
				v.addElement(new Double(clmt.getWeight()));
				iline = StringUtil.formatString(v, format_4);
				out.println(iline);
			}
		}

		// print the area capacity information
		areacapv = res.getAreaCaps();
		nareacap = areacapv.size();
		for (j = 0; j < nareacap; j++) {
			ac = (StateMod_ReservoirAreaCap)areacapv.elementAt(j);
			if (ac == null) {
				out.println();
				continue;
			}
			ch1 = "CAP-AREA" + StringUtil.formatString(j, "%3.3s");
			iline =
			StringUtil.formatString(ch1, "            %-12.12s");
			// Not very efficient but this file does not get written
			// often...
			if (ac.getConten()< 100.0) {
				iline += StringUtil.formatString(
					ac.getConten(), "%8.2f");
			}
			else {	iline += StringUtil.formatString(
					ac.getConten(), "%#8.0f");
			}
			if (ac.getSurarea()< 100.0) {
				iline += StringUtil.formatString(
					ac.getSurarea(), "%8.2f");
			}
			else {	iline += StringUtil.formatString(
					ac.getSurarea(), "%8.0f");
			}
			iline += StringUtil.formatString(
					ac.getSeepage(),"%8.0f");
			out.println(iline);
		}
	}
		
	out.flush();
	out.close();
	out = null;
	routine = null;
	comment_str = null;
	ignore_comment_str = null;
	} 
	catch (Exception e) {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
		routine = null;
		comment_str = null;
		ignore_comment_str = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
}

/**
Writes a Vector of StateMod_Reservoir objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter 
will be wrapped in "...".  <p>This method also writes out Reservoir Area Cap,
Account, Climate Data and Collections data to separate files.  If this method 
is called with a filename parameter of "reservoirs.txt", six files will be 
generated:
- reservoirs.txt
- reservoirs_Accounts.txt
- reservoirs_ContentAreaSeepage.txt
- reservoirs_EvapStations.txt
- reservoirs_PrecipStations.txt
- reservoirs_Collections.txt
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
	fields.add("RiverNodeID");
	fields.add("OnOff");
	fields.add("OneFillRule");
	fields.add("ContentMin");
	fields.add("ContentMax");
	fields.add("ReleaseMax");
	fields.add("DeadStorage");
	fields.add("DailyID");
	fields.add("NumOwners");
	fields.add("NumEvapStations");
	fields.add("NumPrecipStations");
	fields.add("NumCurveRows");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RESERVOIR_STATIONS;
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
	int nAreaCaps = 0;
	int size2 = 0;
	PrintWriter out = null;
	StateMod_Reservoir res = null;
	StateMod_ReservoirAccount account = null;
	StateMod_ReservoirAreaCap areaCap = null;
	StateMod_ReservoirClimate climate = null;	
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
	String[] line = new String[fieldCount];
	String[] newComments = null;
	StringBuffer buffer = new StringBuffer();
	Vector accounts = new Vector();
	Vector areaCaps = new Vector();
	Vector evapClimates = new Vector();
	Vector precipClimates = new Vector();
	Vector tempV = null;
	
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
			res = (StateMod_Reservoir)data.elementAt(i);
			
			line[0] = StringUtil.formatString(res.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(res.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(res.getCgoto(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(res.getSwitch(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(res.getRdate(), 
				formats[4]).trim();
			line[5] = StringUtil.formatString(res.getVolmin(), 
				formats[5]).trim();
			line[6] = StringUtil.formatString(res.getVolmax(), 
				formats[6]).trim();
			line[7] = StringUtil.formatString(res.getFlomax(), 
				formats[7]).trim();
			line[8] = StringUtil.formatString(res.getDeadst(), 
				formats[8]).trim();
			line[9] = StringUtil.formatString(res.getCresdy(), 
				formats[9]).trim();
			line[10] = StringUtil.formatString(res.getNowner(), 
				formats[10]).trim();
			line[11] = StringUtil.formatString(
				StateMod_ReservoirClimate.getNumEvap(
					res.getClimates()), 
				formats[11]).trim();
			line[12] = StringUtil.formatString(
				StateMod_ReservoirClimate.getNumPrecip(
					res.getClimates()), 
				formats[12]).trim();
			
			tempV = res.getAreaCaps();
		 	if (tempV == null) {
				nAreaCaps = 0;
			}
			else {
				nAreaCaps = tempV.size();
			}
			line[13] = StringUtil.formatString(nAreaCaps,
				formats[13]).trim();
				
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

			tempV = res.getAccounts();
			size2 = tempV.size();
			for (j = 0; j < size2; j++) {
				account = (StateMod_ReservoirAccount)
					tempV.elementAt(j);
				account.setCgoto(res.getID());
				accounts.add(account);
			}

			tempV = res.getAreaCaps();
			size2 = tempV.size();
			for (j = 0; j < size2; j++) {
				areaCap = (StateMod_ReservoirAreaCap)
					tempV.elementAt(j);
				areaCap.setCgoto(res.getID());
				areaCaps.add(areaCap);
			}
			
			tempV = res.getClimates();
			size2 = tempV.size();
			for (j = 0; j < size2; j++) {
				climate = (StateMod_ReservoirClimate)
					tempV.elementAt(j);
				climate.setCgoto(res.getID());
				if (climate.getType() 
				    == StateMod_ReservoirClimate.CLIMATE_PTPX) {
				    	precipClimates.add(climate);
				}
				else {
					evapClimates.add(climate);
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

	int lastIndex = filename.lastIndexOf(".");
	String front = filename.substring(0, lastIndex);
	String end = filename.substring((lastIndex + 1), filename.length());

	String accountFilename = front + "_Accounts." + end;
	StateMod_ReservoirAccount.writeListFile(accountFilename, delimiter,
		update, accounts);	
		
	String areaCapFilename = front + "_ContentAreaSeepage." + end;
	StateMod_ReservoirAreaCap.writeListFile(areaCapFilename, delimiter,
		update, areaCaps);	

	String precipClimateFilename = front + "_PrecipStations." + end;
	StateMod_ReservoirClimate.writeListFile(precipClimateFilename, 
		delimiter, update, precipClimates,
		StateMod_DataSet.COMP_RESERVOIR_STATION_PRECIP_STATIONS);

	String evapClimateFilename = front + "_EvapStations." + end;
	StateMod_ReservoirClimate.writeListFile(evapClimateFilename, 
		delimiter, update, evapClimates,
		StateMod_DataSet.COMP_RESERVOIR_STATION_EVAP_STATIONS);

	String collectionFilename = front + "_Collections." + end;
	writeCollectionListFile(collectionFilename, delimiter,
		update, data);
}

/**
Writes the collection data from a Vector of StateMod_Reservoir objects to a 
list file.  A header is printed to the top of the file, containing the commands 
used to generate the file.  Any strings in the body of the file that contain 
the field delimiter will be wrapped in "...". 
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the Vector of objects to write.  
@throws Exception if an error occurs.
*/
public static void writeCollectionListFile(String filename, 
String delimiter, boolean update, Vector data) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	Vector fields = new Vector();
	fields.add("LocationID");
	fields.add("Year");
	fields.add("CollectionType");
	fields.add("PartType");
	fields.add("PartID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RESERVOIR_STATION_COLLECTIONS;
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
	
	int[] years = null;
	int j = 0;
	int k = 0;
	int num = 0;
	StateMod_Reservoir res = null;
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
	String[] line = new String[fieldCount];
	String[] newComments = null;	
	String colType = null;
	String id = null;
	String partType = null;
	StringBuffer buffer = new StringBuffer();
	PrintWriter out = null;
	Vector ids = null;
	
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
			res = (StateMod_Reservoir)data.elementAt(i);
			id = res.getID();
			years = res.getCollectionYears();
			if (years == null) {
				num = 0;
			}
			else {
				num = years.length;
			}
			colType = res.getCollectionType();
			partType = res.getCollectionPartType();
			
			for (j = 0; j < num; j++) {
				ids = res.getCollectionPartIDs(years[j]);
				line[0] = StringUtil.formatString(id,
					formats[0]).trim();
				line[1] = StringUtil.formatString(years[j],
					formats[1]).trim();
				line[2] = StringUtil.formatString(colType,
					formats[2]).trim();
				line[3] = StringUtil.formatString(partType,
					formats[3]).trim();
				line[4] = StringUtil.formatString(
					((String)(ids.elementAt(k))),
					formats[4]).trim();

				buffer = new StringBuffer();	
				for (k = 0; k < fieldCount; k++) {
					if (line[k].indexOf(delimiter) > -1) {
						line[k] = "\"" + line[k] + "\"";
					}
					buffer.append(line[k]);
					if (k < (fieldCount - 1)) {
						buffer.append(delimiter);
					}
				}
	
				out.println(buffer.toString());
			}
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

} // End StateMod_Reservoir
