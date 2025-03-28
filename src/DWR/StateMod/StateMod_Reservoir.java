// StateMod_Reservoir - class for reservoir station data

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.GIS.GeoView.GeoRecord;
import RTi.GIS.GeoView.HasGeoRecord;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
The Reservoir class holds data for entries in the StateMod reservoir station
file.  Secondary data classes are used in cases where lists of data are used.
*/
public class StateMod_Reservoir extends StateMod_Data
implements Cloneable, Comparable<StateMod_Data>, HasGeoRecord, StateMod_ComponentValidator
{

/**
date for one fill rule admin
*/
protected double _rdate;	

/**
minimum reservoir content
*/
protected double _volmin;

/**
Maximum reservoir content
*/
protected double _volmax;

/**
Maximum reservoir release
*/
protected double _flomax;

/**
Dead storage in reservoir
*/
protected double _deadst;

/**
List of owners
*/
protected List<StateMod_ReservoirAccount> _owners;

/**
Daily id
*/
protected String _cresdy;

/**
List of evap/precip stations
*/
protected List<StateMod_ReservoirClimate> _climate_Vector;

/**
List of area capacity values
*/
protected List<StateMod_ReservoirAreaCap> _areacapvals;

/**
List of reservoir rights
*/
protected List<StateMod_ReservoirRight> _rights;

/**
End of month content time series.
*/
protected MonthTS _content_MonthTS;

/**
End of day content time series.
*/
protected DayTS _content_DayTS;

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
protected DayTS _mintarget_DayTS;

/**
Maximum target time series (Daily).
*/
protected DayTS _maxtarget_DayTS;

/**
Link to spatial data -- currently NOT cloned.
*/
protected GeoRecord _georecord;

// Collections are set up to be specified by year, although currently for
// reservoir collections are always the same for the full period.

/**
 * Collection type for the reservoir, or null if not a collection.
 */
private StateMod_Reservoir_CollectionType __collection_type = null;

/**
Used by DMI software for aggregate reservoirs to indicate what is being aggregated, currently must always be RESERVOIR.
*/
private StateMod_Reservoir_CollectionPartType __collection_part_type = StateMod_Reservoir_CollectionPartType.RESERVOIR;

/**
The identifiers for data that are collected - null if not a collection location.
This is a list of List where the __collection_year is the first dimension.
This is ugly but need to use the code to see if it can be made cleaner.
 */
private List<List<String>> __collection_Vector = null;

/**
An array of years that correspond to the aggregate/system.  Reservoirs currently only have one year.
*/
private int [] __collection_year = null;
					
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
		_owners.add(owner);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Add AreaCap.
@param areacap StateMod_ReservoirAreaCap to add.
*/
public void addAreaCap(StateMod_ReservoirAreaCap areacap)
{	if (areacap != null) {
		_areacapvals.add(areacap);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS,true);
		}
	}
}

/**
Add climate.
@param climate StateMod_ReservoirClimate to add.
*/
public void addClimate(StateMod_ReservoirClimate climate) {
	if (climate != null) {
		_climate_Vector.add(climate);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Adds a right to the rights linked list
*/
public void addRight(StateMod_ReservoirRight right)
{	if ( right != null ) {
		_rights.add ( right );
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
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_Reservoir r = (StateMod_Reservoir)data;

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
exist as part of a list of StateMod_ReservoirRight.  We are just connecting pointers.
*/
public static void connectAllRights(List<StateMod_Reservoir> reservoirs, List<StateMod_ReservoirRight> rights)
{	if (reservoirs == null) {
		return;
	}
	int i, num_res = reservoirs.size();

	StateMod_Reservoir res = null;
	for (i = 0; i < num_res; i++) {
		res = reservoirs.get(i);
		if (res == null) {
			continue;
		}
		res.connectRights(rights);
	}
	res = null;
}

/**
Connect all reservoir-related time series to reservoir data objects.
@param reservoirs List of StateMod_Reservoir.
@param content_MonthTS List of MonthTS containing end-of-month content.
@param content_DayTS List of DayTS containing end-of-day content.
@param target_MonthTS List of MonthTS containing minimum/maximum target time series pairs.
@param target_DayTS List of DayTS containing minimum/maximum target time series pairs.
*/
public static void connectAllTS ( List<StateMod_Reservoir> reservoirs,
	List<MonthTS> content_MonthTS, List<DayTS> content_DayTS,
	List<MonthTS> target_MonthTS, List<DayTS> target_DayTS)
{	if (reservoirs == null) {
		return;
	}
	int numRes = reservoirs.size();

	StateMod_Reservoir res = null;
	for (int i = 0; i < numRes; i++) {
		res = reservoirs.get(i);
		if (res == null) {
			continue;
		}
		res.connectContentMonthTS(content_MonthTS);
		res.connectContentDayTS(content_DayTS);
		res.connectTargetMonthTS(target_MonthTS);
		res.connectTargetDayTS(target_DayTS);
	}
}

/**
Connect the end-of-day content time series to this reservoir, using the
time series location and the reservoir identifier to make a match.
The reservoir name is also set as the time series description.
@param contentTS Vector of end-of-day content time series to search.
*/
public void connectContentDayTS ( List<DayTS> contentTS )
{	if (contentTS == null) {
		return;
	}
	int numTS = contentTS.size();
	DayTS ts = null;

	for (int i = 0; i < numTS; i++) {
		ts = contentTS.get(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			setContentDayTS(ts);
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect the end-of-month content time series to this reservoir, using the
time series location and the reservoir identifier to make a match.
The reservoir name is also set as the time series description.
@param contentTS Vector of end-of-month content time series to search.
*/
public void connectContentMonthTS ( List<MonthTS> contentTS )
{	if (contentTS == null) {
		return;
	}
	int numTS = contentTS.size();
	MonthTS ts = null;

	for (int i = 0; i < numTS; i++) {
		ts = contentTS.get(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			setContentMonthTS(ts);
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect the minimum and maximum target time series (daily) to the reservoir,
using the time series location and the reservoir identifier to make the match.
The time series must be in the order of min, max, min, max, etc.
@param targetTS The Vector of DayTS containing minimum and maximum targets.
*/
public void connectTargetDayTS(List<DayTS> targetTS) {
	if (targetTS == null) {
		return;
	}
	int numTS = targetTS.size();
	DayTS ts = null;

	for (int i = 0; i < numTS; i++) {
		ts = targetTS.get(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getIdentifier().getLocation())) {
			setMinTargetDayTS(ts);
			ts.setDescription(getName());
			// now set max
			ts = targetTS.get(i+1);
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
}

/**
Connect the minimum and maximum target time series (monthly) to the reservoir,
using the time series location and the reservoir identifier to make the match.
The time series must be in the order of min, max, min, max, etc.
@param targetTS The Vector of MonthTS containing minimum and maximum targets.
*/
public void connectTargetMonthTS(List<MonthTS> targetTS) {
	if (targetTS == null) {
		return;
	}
	int numTS = targetTS.size();
	MonthTS ts = null, ts1 = null, ts2 = null;

	for (int i = 0; i < numTS; i++) {
		ts = targetTS.get(i);
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
				ts2 = targetTS.get(i+1);
				if ( !_id.equals(ts2.getIdentifier().getLocation())) {
					// Time series is for a different reservoir so reset to null...
					ts2 = null;
				}
			}
			// Now link the time series...
			if ( (ts2 == null) && (ts1 != null) ) {
				// Only one time series is specified so it is the maximum...
				setMaxTargetMonthTS(ts1);
				ts1.setDescription(getName());
			}
			else if ( (ts1 != null) && (ts2 != null) ) {
				// Have both time series...
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
public void connectRights ( List<StateMod_ReservoirRight> rights )
{	if (rights == null) {
		return;
	}
	int i;
	int num_rights = rights.size();

	StateMod_ReservoirRight right;
	for (i = 0; i < num_rights; i++) {
		right = rights.get(i);
		if (right == null) {
			continue;
		}
		if (_id.equals(right.getCgoto())) {
			_rights.add ( right );
		}
	}
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = (StateMod_Reservoir)clone();
	((StateMod_Reservoir)_original)._isClone = false;
	_isClone = true;
}

/**
Deletes the area capacity at the given index
@param index of the area capacity to delete.
*/
public void deleteAreaCapAt(int index) {
	_areacapvals.remove(index);
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
	_climate_Vector.remove(index);
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
	_owners.remove(index);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
	}
}

// TODO - need to decide in the GUI if the right is actually removed from the main list.
/**
Remove right from list.  A comparison on the ID is made.
@param right Right to remove.  Note that the right is only removed from the
list for this reservoir and must also be removed from the main reservoir right list.
*/
public void disconnectRight ( StateMod_ReservoirRight right )
{	if (right == null) {
		return;
	}
	int size = _rights.size();
	StateMod_ReservoirRight right2;
	// Assume that more than on instance can exist, even though this is not allowed...
	for ( int i = 0; i < size; i++ ) {
		right2 = _rights.get(i);
		if ( right2.getID().equalsIgnoreCase(right.getID()) ) {
			_rights.remove(i);
		}
	}
}

/**
Disconnect all rights.
*/
public void disconnectRights ()
{	_rights.clear();
}

/**
Retrieve the owner at a particular index.
*/
public StateMod_ReservoirAccount getAccount(int index) {
	return(_owners.get(index));
}

/**
Get all owners.
*/
public List<StateMod_ReservoirAccount> getAccounts()
{	return _owners;
}

/**
Return the area capacity at a particular index.
*/
public StateMod_ReservoirAreaCap getAreaCap(int index)
{	return(_areacapvals.get(index));
}

/**
Return all the area capacity data.
*/
public List<StateMod_ReservoirAreaCap> getAreaCaps ()
{	return _areacapvals;
}

/**
Return the climate station at a particular index.
*/
public StateMod_ReservoirClimate getClimate(int index) {
	if (_climate_Vector.isEmpty() || index >= _climate_Vector.size()) {
		return(new StateMod_ReservoirClimate());
	}
	return(_climate_Vector.get(index));
}

/**
Return the climate station assignments.
*/
public List<StateMod_ReservoirClimate> getClimates() {
	return _climate_Vector;
}

/**
Return the collection part ID list for the specific year.  For reservoirs, only one
aggregate/system list is currently supported so the same information is returned
regardless of the year value.
@return the list of collection part IDS, or null if not defined.
*/
public List<String> getCollectionPartIDs ( int year )
{	if ( __collection_Vector.size() == 0 ) {
		return null;
	}
	//if ( __collection_part_type.equalsIgnoreCase("Reservoir") ) {
		// The list of part IDs will be the first and only list...
		return __collection_Vector.get(0);
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
public StateMod_Reservoir_CollectionPartType getCollectionPartType() {
	return __collection_part_type;
}

/**
Return the collection type, "Aggregate" or "System".
@return the collection type, "Aggregate" or "System".
*/
public StateMod_Reservoir_CollectionType getCollectionType()
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
Get the geographical data associated with the reservoir.
@return the GeoRecord for the reservoir.
*/
public GeoRecord getGeoRecord() {
	return _georecord;
}

/**
Return a list of on/off switch option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and "0 - Off", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getIresswChoices ( boolean include_notes )
{	List<String> v = new Vector<String>();
	v.add ( "0 - Off" );	// Possible options are listed here.
	v.add ( "1 - On, do not store above reservoir targets" );
	v.add ( "2 - 1 and adjust volume, etc. by dead storage" );
	v.add ( "3 - On, do store above reservoir targets" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i,StringUtil.getToken(v.get(i), " ", 0, 0) );
		}
	}
	return v;
}

/**
Return the default on/off switch choice.  This can be used by GUI code
to pick a default for a new reservoir.
@return the default reservoir replacement choice.
*/
public static String getIresswDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return "1 - On, do not store above reservoir targets";
	}
	else {
		return "1";
	}
}

/**
Get the last right associated with reservoir.
*/
public StateMod_ReservoirRight getLastRight()
{	if ( (_rights == null) || (_rights.size() == 0) ) {
		return null;
	}
	return _rights.get(_rights.size() - 1);
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
@param tslist The list of monthly evaporation data to check.
@param check_ts If true, get the count of non-null time series (the reservoir
may reference evaporation station identifiers but the identifiers may not actually exist).
@return the number of evaporation time series for the reservoir.
*/
public int getNumEvaporationMonthTS ( List<MonthTS> tslist, boolean check_ts )
{	// Loop through the evaporation data for this reservoir.  For each
	// referenced evaporation station, if a matching time series is found, increment the count.
	int nsta = _climate_Vector.size();
	int nts = 0;
	TS ts;
	int pos = 0;
	StateMod_ReservoirClimate sta = null;
	for ( int i = 0; i < nsta; i++ ) {
		sta = _climate_Vector.get(i);
		if ( sta.getType() != StateMod_ReservoirClimate.CLIMATE_EVAP ) {
			Message.printStatus ( 1, "", "SAMX climate " + sta.getID() + " is not evap" );
			continue;
		}
		pos = TSUtil.indexOf ( tslist, sta.getID(), "Location", 1 );
		//Message.printStatus ( 2, "", "SAMX climate " + sta.getID() + " pos is " + pos );
		if ( pos >= 0 ) {
			if ( check_ts ) {
				// Make sure that the time series has data...
				ts = tslist.get(pos);
				if ( (ts != null) && ts.hasData() ) {
					//Message.printStatus ( 2, "", "SAMX ts has data." );
					++nts;
				}
				else {
					//Message.printStatus ( 2, "", "SAMX ts has NO data." );
				}
			}
			else {
				// Just a count of the evaporation time series...
				++nts;
			}
		}
	}
	return nts;
}

/**
Return the number of precipitation time series for the reservoir.
@param tslist The list of monthly precipitation data to check.
@param check_ts If true, get the count of non-null time series (the reservoir
may reference precipitation station identifiers but the identifiers may not actually exist).
@return the number of precipitation time series for the reservoir.
*/
public int getNumPrecipitationMonthTS ( List<MonthTS> tslist, boolean check_ts )
{	// Loop through the precipitation data for this reservoir.  For each
	// referenced precipitation station, if a matching time series is found, then increment the count.
	int nsta = _climate_Vector.size();
	int nts = 0;
	TS ts;
	int pos = 0;
	StateMod_ReservoirClimate sta = null;
	for ( int i = 0; i < nsta; i++ ) {
		sta = _climate_Vector.get(i);
		if ( sta.getType() != StateMod_ReservoirClimate.CLIMATE_PTPX ) {
			continue;
		}
		pos = TSUtil.indexOf ( tslist, sta.getID(), "Location", 1 );
		if ( pos >= 0 ) {
			if ( check_ts ) {
				ts = tslist.get(pos);
				// Make sure that the time series has data...
				if ( (ts != null) && ts.hasData() ) {
					++nts;
				}
			}
			else {
				// Just a count of the evaporation time series...
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
"-1 - Do not administer one fill rule", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getRdateChoices ( boolean include_notes )
{	List<String> v = new Vector<String>(2);
	v.add ( "-1 - Do not administer the one fill rule" );
	v.add ( "1 - January" );	// Possible options are listed here.
	v.add ( "2 - February" );
	v.add ( "3 - March" );
	v.add ( "4 - April" );
	v.add ( "5 - May" );
	v.add ( "6 - June" );
	v.add ( "7 - July" );
	v.add ( "8 - August" );
	v.add ( "9 - September" );
	v.add ( "10 - October" );
	v.add ( "11 - November" );
	v.add ( "12 - December" );
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
Return the default one fill rule switch choice.  This can be used by GUI code
to pick a default for a new reservoir.
@return the default reservoir replacement choice.
*/
public static String getRdateDefault ( boolean include_notes )
{	// Make this agree with the above method...
	if ( include_notes ) {
		return ( "-1 - Do not administer the one fill rule" );
	}
	else {
		return "-1";
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
	else {
		return _rights.get(index);
	}
}
/**
get the rights
*/
public List<StateMod_ReservoirRight> getRights()
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
	_owners = new Vector<StateMod_ReservoirAccount>();
	_climate_Vector = new Vector<StateMod_ReservoirClimate>();
	_areacapvals = new Vector<StateMod_ReservoirAreaCap>();
	_rights = new Vector<StateMod_ReservoirRight>();
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
	else {
		_cresdy = "";
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
	else {
		return true;
	}
}

/**
Insert AreaCap
*/
public void insertAreaCapAt(StateMod_ReservoirAreaCap areacap, int i) {
	if (areacap != null) {
		_areacapvals.add(i,areacap);
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
		}
	}
}

/**
Read reservoir information in and store in a Vector.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List<StateMod_Reservoir> readStateModFile(String filename)
throws Exception
{	String routine = "StateMod_Reservoir.readStateModFile";
	List<StateMod_Reservoir> theReservoirs = new Vector<StateMod_Reservoir>();
	String iline = null;
	List<Object> v = new Vector<Object>(9);
	int [] format_0 = {
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_STRING };
	int [] format_0w = {
		12,
		24,
		12,
		8,
		8,
		1,
		12 };
	int [] format_1 = {
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_INTEGER };
	int [] format_1w = {
		24,
		8,
		8,
		8,
		8,
		8,
		8,
		8,
		8 };
	int [] format_2 = {
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_INTEGER };
	int [] format_2w = {
		12,
		12,
		8,
		8,
		8,
		8 };
	int [] format_3 = {
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_DOUBLE };
	int [] format_3w = {
		24,
		12,
		8 };
	int [] format_4 = {
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_DOUBLE };
	int [] format_4w = {
		24,
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
		Message.printDebug(10, routine, "in SMParseResFile reading file: " + filename);
	}
	int line_count = 0;
	try {
		in = new BufferedReader(new FileReader(
		IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine())!= null) {
			++line_count;
			// check for comments
			if (iline.startsWith("#")|| iline.trim().length()==0) {
				continue;
			}

			// allocate new reservoir node
			aReservoir = new StateMod_Reservoir();

			// line 1
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line 1: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aReservoir.setID(((String)v.get(0)).trim());
			aReservoir.setName(((String)v.get(1)).trim());
			aReservoir.setCgoto(((String)v.get(2)).trim());
			aReservoir.setSwitch((Integer)v.get(3));
			aReservoir.setRdate((Double)v.get(4));
			aReservoir.setCresdy(((String)v.get(5)).trim());

			// line 2
			iline = in.readLine();
			++line_count;
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line 2: " + iline);
			}
			StringUtil.fixedRead(iline, format_1, format_1w, v);
			aReservoir.setVolmin(((Double)v.get(0)));
			aReservoir.setVolmax(((Double)v.get(1)));
			aReservoir.setFlomax(((Double)v.get(2)));
			aReservoir.setDeadst(((Double)v.get(3)));
			int nowner =((Integer)v.get(4)).intValue();
			int nevap =((Integer)v.get(5)).intValue();
			int nptpx =((Integer)v.get(6)).intValue();
			int nrange =((Integer)v.get(7)).intValue();

			// get the owner's information
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "Number of owners: " + nowner);
			}
			for (i = 0; i < nowner; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_2,format_2w, v);
				anAccount = new StateMod_ReservoirAccount();
				// Account ID is set to the numerical count (StateMod uses the number)
				anAccount.setID ( "" + (i + 1) );
				anAccount.setName( ((String)v.get(0)).trim());
				anAccount.setOwnmax( ((Double)v.get(1)));
				anAccount.setCurown( ((Double)v.get(2)));
				anAccount.setPcteva( ((Double)v.get(3)));
				anAccount.setN2own( ((Integer)v.get(4)));
				aReservoir.addAccount(anAccount);
			}

			// get the evaporation information
			for (i = 0; i < nevap; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_3,format_3w, v);
				anEvap = new StateMod_ReservoirClimate();
				anEvap.setID( ((String)v.get(0)).trim());
				anEvap.setType(StateMod_ReservoirClimate.CLIMATE_EVAP);
				anEvap.setWeight(((Double)v.get(1)));
				aReservoir.addClimate(anEvap);
			}
			
			// get the precipitation information
			for (i = 0; i < nptpx; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_3, format_3w, v);
				aPtpx = new StateMod_ReservoirClimate();
				aPtpx.setID( ((String)v.get(0)).trim());
				aPtpx.setType( StateMod_ReservoirClimate.CLIMATE_PTPX);
				aPtpx.setWeight(((Double)v.get(1)));
				aReservoir.addClimate(aPtpx);
			}
			
			// get the area capacity information
			for (i = 0; i < nrange; i++) {
				iline = in.readLine();
				++line_count;
				StringUtil.fixedRead(iline, format_4, format_4w, v);
				StateMod_ReservoirAreaCap anAreaCap = new StateMod_ReservoirAreaCap();
				anAreaCap.setConten(((Double)v.get(0)));
				anAreaCap.setSurarea(((Double)v.get(1)));
				anAreaCap.setSeepage(((Double)v.get(2)));
				aReservoir.addAreaCap(anAreaCap);
			}

			// add the reservoir to the vector of reservoirs
			theReservoirs.add(aReservoir);
		}
	} catch (Exception e) {
		Message.printWarning(3, routine, "Error reading reservoir stations in line " + line_count );
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
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
the original list.  A comparison of objects is made to verify whether any data are dirty.
@param owners Vector of StateMod_ReservoirAccount to set.  This should be a non-null Vector.
*/
public void setAccounts ( List<StateMod_ReservoirAccount> owners )
{	// All of the following work id done to make sure the dirty flag on the
	// component and individual objects is correct.  We could just delete
	// all existing accounts and re-add, but we don't know for sure that
	// the dirty flags would be correct.
/* TODO
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

// TODO - need to check dirty flag
/**
Set the area capacity vector.
*/
public void setAreaCaps ( List<StateMod_ReservoirAreaCap> areacapvals )
{	_areacapvals = areacapvals ;
}

// TODO - need to check dirty flag
/**
Sets the climate station vector.
*/
public void setClimates ( List<StateMod_ReservoirClimate> climates ) {
	_climate_Vector = climates;
}

/**
Set the collection list for an aggregate/system.  It is assumed that the
collection applies to all years of data.
@param ids The identifiers indicating the locations to collection.
*/
public void setCollectionPartIDs ( List<String> ids )
{	if ( __collection_Vector == null ) {
		__collection_Vector = new Vector<List<String>> ( 1 );
		__collection_year = new int[1];
	}
	else {
		// Remove the previous contents...
		__collection_Vector.clear();
	}
	// Now assign...
	__collection_Vector.add ( ids );
	__collection_year[0] = 0;
}

/**
Set the collection type.
@param collection_type The collection type, either "Aggregate" or "System".
*/
public void setCollectionType ( StateMod_Reservoir_CollectionType collection_type )
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
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
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
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
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
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
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
Set the geographic information object associated with the reservoir.
@param georecord Geographic record associated with the reservoir.
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
The value 0 is meaning (??)- should be 1-12 or -1 to signify do not administer rule
*/
public void setRdate(double rdate) {
	if ( rdate == 0 ) {
		rdate = -1;
	}

	if (rdate != _rdate) {
		_rdate = rdate;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
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

// TODO - need to check dirty correctly
/**
Set the rights
*/
public void setRights ( List<StateMod_ReservoirRight> rights )
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
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
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
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_STATIONS, true);
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
@param dataset StateMod dataset object.
@return validation results.
*/
public StateMod_ComponentValidation validateComponent ( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String riverID = getCgoto();
	int iressw = getSwitch();
	double rdate = getRdate();
	String dailyID = getCresdy();
	double volmin = getVolmin();
	double volmax = getVolmax();
	double flomax = getFlomax();
	double deadst = getDeadst();
	int nowner = getNowner();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir identifier is blank.",
			"Specify a reservoir station identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id + "\" name is blank.",
			"Specify a reservoir name to clarify data.") );
	}
	// Get the network list if available for checks below
	DataSetComponent comp = null;
	List<StateMod_RiverNetworkNode> rinList = null;
	if ( dataset != null ) {
		comp = dataset.getComponentForComponentType(StateMod_DataSet.COMP_RIVER_NETWORK);
		@SuppressWarnings("unchecked")
		List<StateMod_RiverNetworkNode> dataList = (List<StateMod_RiverNetworkNode>)comp.getData();
		rinList = dataList;
		if ( (rinList != null) && (rinList.size() == 0) ) {
			// Set to null to simplify checks below
			rinList = null;
		}
	}
	if ( StateMod_Util.isMissing(riverID) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id + "\" river node ID is blank.",
			"Specify a river node ID to associate the reservoir with a river network node.") );
	}
	else {
		// Verify that the river node is in the data set, if the network is available
		if ( rinList != null ) {
			if ( StateMod_Util.indexOf(rinList, riverID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" river network ID (" + riverID + ") is not found in the list of river network nodes.",
					"Specify a valid river network ID to associate the reservoir with a river network node.") );
			}
		}
	}
	List<String> choices = getIresswChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + iressw) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" switch (" + iressw + ") is invalid.",
			"Specify the switch as one of " + choices) );
	}
	if ( !((rdate >= -1.01) && (rdate <= 12.01)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" date for one fill rule administration (" +
			StringUtil.formatString(rdate,"%.0f") + ") is invalid.",
			"Specify the value as -1 to not administer, or 0 - 12 for the month for reoperation." ) );
	}
	// Verify that the daily ID is in the data set (daily ID is allowed to be missing)
	if ( (dataset != null) && !StateMod_Util.isMissing(dailyID) ) {
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_STATIONS);
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> resList = (List<StateMod_Reservoir>)comp2.getData();
		if ( dailyID.equals("0") || dailyID.equals("3") || dailyID.equals("4") || dailyID.equals("5") ) {
			// OK
		}
		else if ( (resList != null) && (resList.size() > 0) ) {
			// Check the reservoir station list
			if ( StateMod_Util.indexOf(resList, dailyID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id + "\" daily ID (" + dailyID +
				") is not 0, 3, 4, or 5 and is not found in the list of reservoir stations.",
				"Specify the daily ID as 0, 3, 4, 5, or a matching reservoir ID.") );
			}
		}
	}
	if ( !((volmin >= 0.0)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" minimum volume (" + StringUtil.formatString(volmin,"%.1f") + ") is invalid.",
			"Specify the value as >= 0." ) );
	}
	if ( !((volmax >= 0.0)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" maximum volume (" + StringUtil.formatString(volmax,"%.1f") + ") is invalid.",
			"Specify the value as >= 0." ) );
	}
	if ( !((volmax >= volmin)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" maximum volume (" + StringUtil.formatString(volmax,"%.1f") + ") is < the minimum volume (" +
			StringUtil.formatString(volmin,"%.1f") + ").",
			"Specify the maximum volumen >= the minimum volume." ) );
	}
	if ( !((flomax >= 0.0)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" maximum release (" + StringUtil.formatString(flomax,"%.1f") + ") is invalid.",
			"Specify the value as >= 0." ) );
	}
	if ( !((deadst >= 0.0)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" dead storage (" + StringUtil.formatString(deadst,"%.1f") + ") is invalid.",
			"Specify the value as >= 0." ) );
	}
	if ( !(nowner >= 0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" number of owners (" + nowner + ") is invalid.",
			"Specify the number of owners as >= 0.") );
	}
	else {
		// Check owner information
		List<StateMod_ReservoirAccount> accounts = getAccounts();
		StateMod_ReservoirAccount account;
		String ownnam;
		double ownmax;
		double curown;
		double pcteva;
		int n2own;
		List<String> n2ownChoices = StateMod_ReservoirAccount.getN2ownChoices(false);
		for ( int i = 0; i < nowner; i++ ) {
			account = accounts.get(i);
			ownnam = account.getName();
			ownmax = account.getOwnmax();
			curown = account.getCurown();
			pcteva = account.getPcteva();
			n2own = account.getN2own();
			if ( StateMod_Util.isMissing(ownnam) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id + "\" account " +
					(i + 1) + " name is blank.",
					"Specify a reservoir account name to clarify data.") );
			}
			if ( !((ownmax >= 0.0) && (ownmax <= volmax)) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" account " + (i + 1) + " maximum (" +
					StringUtil.formatString(ownmax,"%.2f") + ") is invalid.",
					"Specify the account maximum as >= 0 and less than the reservoir maximum (" +
					StringUtil.formatString(volmax,"%.2f") + ").") );
			}
			if ( !((curown >= 0.0) && (curown <= volmax)) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" account " + (i + 1) + " initial value (" +
					StringUtil.formatString(curown,"%.2f") + ") is invalid.",
					"Specify the account initial value >= 0 and less than the reservoir maximum (" +
					StringUtil.formatString(volmax,"%.2f") + ").") );
			}
			if ( !((curown <= ownmax)) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" account " + (i + 1) + " initial value (" +
					StringUtil.formatString(curown,"%.2f") + ") is greater than the account maximum volume (" +
					StringUtil.formatString(ownmax,"%.2f") + ").",
					"Specify the account initial value as less than the account maximum." ) );
			}
			if ( !((pcteva >= -1.01) && (pcteva <= 100.0)) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" account " + (i + 1) + " evaporation distribution parameter (" +
					StringUtil.formatString(pcteva,"%.2f") + ") is invalid.",
					"Specify the evaporation distribution parameter as -1 or 0 to 100." ) );
			}
			if ( StringUtil.indexOfIgnoreCase(choices,"" + n2own) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" owernership switch (" + n2own + ") is invalid.",
					"Specify as one of " + n2ownChoices) );
			}
		}
	}
	// Check the climate information
	// TODO SAM 2009-06-01 Evaluate whether to cross check with time series identifiers
	List<StateMod_ReservoirClimate> climatev = getClimates();
	StateMod_ReservoirClimate clmt = null;
	int nclmt = climatev.size();
	double weight;
	String staType = null;
	for ( int i = 0; i < nclmt; i++) {
		clmt =(StateMod_ReservoirClimate)climatev.get(i);
		if (clmt == null) {
			continue;
		}
		else if ( clmt.getType()== StateMod_ReservoirClimate.CLIMATE_EVAP) {
			staType = "evaportation";
		}
		else if ( clmt.getType()== StateMod_ReservoirClimate.CLIMATE_PTPX) {
			staType = "precipitation";
		}
		weight = clmt.getWeight();
		if ( !((weight >= 0) && (weight <= 100.0)) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
				"\" " + staType + " station \"" + clmt.getID() + " weight (" +
				StringUtil.formatString(weight,"%.2f") + ") is invalid.",
				"Specify the weight as 0 to 100." ) );
		}
	}
	// Check the area capacity data.
	List<StateMod_ReservoirAreaCap> areacapv = getAreaCaps();
	StateMod_ReservoirAreaCap ac = null;
	int nareacap = areacapv.size();
	double content;
	double area;
	double seepage;
	double contentPrev = -1.0;
	double areaPrev = -1.0;
	double seepagePrev = -1.0;
	for ( int i = 0; i < nareacap; i++) {
		ac = areacapv.get(i);
		if (ac == null) {
			continue;
		}
		content = ac.getConten();
		area = ac.getSurarea();
		seepage = ac.getSeepage();
		if ( !((content >= 0.0) ) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
				"\" area/cap/seepage " + (i + 1) + " content (" +
				StringUtil.formatString(content,"%.2f") + ") is invalid.",
				"Specify the content >= 0." ) );
		}
		if ( !((content > contentPrev) ) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
				"\" area/cap/seepage " + (i + 1) + " content (" +
				StringUtil.formatString(content,"%.2f") + ") is not increasing.",
				"Specify the content > the previous content value." ) );
		}
		if ( !((area >= 0.0) ) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
				"\" area/cap/seepage " + (i + 1) + " area (" +
				StringUtil.formatString(area,"%.2f") + ") is invalid.",
				"Specify the area >= 0." ) );
		}
		if ( i == nareacap - 1 ) {
			// Last one allowed to be the same
			if ( !((area >= areaPrev) ) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" area/cap/seepage " + (i + 1) + " area (" +
					StringUtil.formatString(area,"%.2f") + ") is not increasing.",
					"Specify the area > the previous area value (= allowed on last point)." ) );
			}
		}
		else {
			// Must be increasing
			if ( !((area > areaPrev) ) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
					"\" area/cap/seepage " + (i + 1) + " area (" +
					StringUtil.formatString(area,"%.2f") + ") is not increasing.",
					"Specify the area > the previous area value." ) );
			}
		}
		if ( !((seepage >= 0.0) ) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
				"\" area/cap/seepage " + (i + 1) + " seepage (" +
				StringUtil.formatString(seepage,"%.2f") + ") is invalid.",
				"Specify the seepage >= 0." ) );
		}
		// Seepage might be zero so allow same as previous
		if ( !((seepage >= seepagePrev) ) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
				"\" area/cap/seepage " + (i + 1) + " seepage (" +
				StringUtil.formatString(seepage,"%.2f") + ") is decreasing.",
				"Specify the seepage >= the previous seepage value." ) );
		}
		contentPrev = content;
		areaPrev = area;
		seepagePrev = seepage;
	}
	// TODO SAM 2009-06-01) evaluate how to check rights (with getRights() or checking the rights data
	// set component).
	return validation;
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
		List<StateMod_Reservoir> theReservoirs, List<String> newComments)
throws Exception {
	writeStateModFile(infile, outfile, theReservoirs, newComments, true);
}

/**
Write reservoirs information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theReservoirs list of reservoirs to print
@param newComments addition comments which should be included in history
@param useDailyData whether to use daily data
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile, 
		List<StateMod_Reservoir> theReservoirs, List<String> newComments, boolean useDailyData)
throws Exception {
	String routine = "StateMod_Reservoirs.writeStateModFile";
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;

	if (Message.isDebugOn) {
		Message.printDebug(1, routine, "in writeStateModFile printing file: " + outfile);
	}

	try {	
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		String iline = null;
		String cmnt = "#>";
		String format_0 = null;
		if (useDailyData) {
			format_0 = "%-12.12s%-24.24s%-12.12s%8d%#8.0f %-12.12s";
		}
		else {	
			format_0 = "%-12.12s%-24.24s%-12.12s%8d%#8.0f";
		}
		String format_1 = "                        %#8.0f%#8.0f%#8.0f%#8.0f%8d%8d%8d%8d";
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
		List<Object> v = new Vector<Object>(6);	
		List<StateMod_ReservoirAccount> ownv = null;
		List<StateMod_ReservoirClimate> climatev = null;
		List<StateMod_ReservoirAreaCap> areacapv = null;
	
		int i,j=0;
		
		// print out header
		out.println(cmnt);
		out.println(cmnt + " *************************************************************");
		out.println(cmnt + "  StateMod Reservoir Station file");
		out.println(cmnt);
		out.println(cmnt + "  Card 1   format:  (a12, a24, a12, i8, f8.0, 1x, a12)");
		out.println(cmnt);
		out.println(cmnt + "  ID       cresid:  Reservoir Id");
		out.println(cmnt + "  Name     resnam:  Reservoir name");
		out.println(cmnt + "  Riv ID    cgoto:  Node where Reservoir is located");
		out.println(cmnt + "  On/Off   iressw:  Switch 0 = off");
		out.println(cmnt + "                           1 = on, do not adjust for dead storage");
		out.println(cmnt + "                               do not store above rervoir targets");
		out.println(cmnt + "                           2 = do not store above rervoir targets");
		out.println(cmnt + "                               adjust maximum ownership and initial");
		out.println(cmnt + "                               storage of the last account by the");
		out.println(cmnt + "                               dead storage volume");
		out.println(cmnt + "                           3 = on, do not adjust for dead storage");
		out.println(cmnt + "                               do store above rervoir targets");
		out.println(cmnt + "                               (Note: in conjunction with an");
		out.println(cmnt + "                               operational release for targets this");
		out.println(cmnt + "                               results in a 'paper fill' activity.)");
		out.println(cmnt + "  Admin #   rdate:  Administration date for 1 fill rule");
		out.println(cmnt + "  Daily ID cresdy:  Identifier for daily time series.");
		out.println(cmnt);
		out.println(cmnt + "  Card 2 format:  (24x, 4f8.0, 4i8)");
		out.println(cmnt);
		out.println(cmnt + "  VolMin   volmin:  Min storage (ac-ft)");
		out.println(cmnt + "  VolMax   volmax:  Max storage (ac-ft)");
		out.println(cmnt + "  FloMax   flomax:  Max discharge (cfs)");
		out.println(cmnt + "  DeadSt   deadst:  Dead storage (ac-ft)");
		out.println(cmnt + "  NumOwner nowner:  Number of owners");
		out.println(cmnt + "  NumEva   nevapo:  Number of evaporation stations");
		out.println(cmnt + "  NumPre   nprecp:  Number of precipitation stations");
		out.println(cmnt + "  NumTable nrange:  Number of area capacity values");
		out.println(cmnt);
		out.println(cmnt + "  Card 3 format:  (12x, a12, 3f8.0, i8)");
		out.println(cmnt);
		out.println(cmnt + "  OwnName  ownnam:  Owner name");
		out.println(cmnt + "  OwnMax   ownmax:  Maximum storage for that owner (ac-ft)");
		out.println(cmnt + "  Sto-1    curown:  Initial storage for that owner (ac-ft)");
		out.println(cmnt + "  EvapTyp  pcteva:  Evaporation distribution");
		out.println(cmnt + "  FillTyp   n2own:  Ownership type 1=First fill; 2=Second fill");
		out.println(cmnt);
		out.println(cmnt + "  Card 4  format:  (24x, a12, f8.0)");
		out.println(cmnt);
		out.println(cmnt + "  Evap ID  cevar:  Evaporation station");
		out.println(cmnt + "  EvapWt  weigev:  Evaporation station weight (%%)");
		out.println(cmnt);
		out.println(cmnt + "  Card 5 format:  (24x, a12, f8.0)");
		out.println(cmnt);
		out.println(cmnt + "  Prec ID   cprer:  Precipitation station");
		out.println(cmnt + "  PrecWt   weigpr:  Precipitation station weight (%%)");
		out.println(cmnt);
		out.println(cmnt + "  Card 6 format:  (24x, 3f8.0)");
		out.println(cmnt);
		out.println(cmnt + "  Cont     conten:  Content (ac-ft)");
		out.println(cmnt + "  Area    surarea:  Area (ac)");
		out.println(cmnt + "  Seep    seepage:  Seepage (ac-ft)");
		out.println(cmnt);
		out.println(cmnt + " *************************************************************");
		out.println(cmnt);
	
		out.println(cmnt + "    ID              Name              Node     On/Off  RDate       DailyID ");
		out.println(cmnt + "---------eb----------------------eb----------eb------eb------exb----------e");
		out.println(cmnt + "                       VolMin  VolMax  FloMax  DeadSt NumOwner NumEva  NumPre NumTable");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxb------eb------eb------eb------eb------eb------eb------eb------e");
		out.println(cmnt + "                         OwnName   OwnMax   Sto-1 EvapTyp FillTyp");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxb----------eb------eb------eb------eb------e");
		out.println(cmnt + "                        Evap Id    EvapWt ");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxb----------eb------e");
		out.println(cmnt + "                        Prec Id    PrecWt ");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxb----------eb------e");
		out.println(cmnt + "                        Cont    Area    Seep  ");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxb------eb------eb------e");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int num = 0;
		if (theReservoirs != null) {
			num = theReservoirs.size();
		}
		int nevap, nptpx, nareacap, nclmt, nowner;
		for (i = 0; i < num; i++) {
			res = theReservoirs.get(i);
			if (res == null) {
				continue;
			}
	
			v.clear();
			v.add(res.getID());
			v.add(res.getName());
			v.add(res.getCgoto());
			v.add(Integer.valueOf(res.getSwitch()));
			v.add(Double.valueOf(res.getRdate()));
			if (useDailyData) {
				v.add(res.getCresdy());
			}
			iline = StringUtil.formatString(v, format_0);
			out.println(iline);
	
			// print reservoir statics: min, max, maxrelease, dead storage,
			// #owners, #evaps ...
			// count the number climate stations which are evap vs precip
			nevap = StateMod_ReservoirClimate.getNumEvap( res.getClimates());
			nptpx = StateMod_ReservoirClimate.getNumPrecip(	res.getClimates());
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "nevap: " + nevap + " nptpx: " + nptpx);
			}
			v.clear();
			v.add(Double.valueOf(res.getVolmin()));
			v.add(Double.valueOf(res.getVolmax()));
			v.add(Double.valueOf(res.getFlomax()));
			v.add(Double.valueOf(res.getDeadst()));
			v.add(Integer.valueOf(res.getNowner()));
			v.add(Integer.valueOf(nevap));
			v.add(Integer.valueOf(nptpx));
			v.add(Integer.valueOf(res.getNrange()));
			iline = StringUtil.formatString(v, format_1);
			out.println(iline);
	
			// print the owner information 
			ownv = res.getAccounts();
			nowner = ownv.size();
			for (j = 0; j < nowner; j++) {
				own = ownv.get(j);
				if (own == null) {
					out.println();
					continue;
				}
				desc = own.getName();
				if (desc.length()== 0) {
					desc = "Account " + (j + 1);
				}
				v.clear();
				v.add(desc);
				v.add(Double.valueOf(own.getOwnmax()));
				v.add(Double.valueOf(own.getCurown()));
				v.add(Double.valueOf(own.getPcteva()));
				v.add(Integer.valueOf(own.getN2own()));
				iline = StringUtil.formatString(v, format_2);
				out.println(iline);
			}
	
			// print the evap information
			climatev = res.getClimates();
			nclmt = climatev.size();
			for (j = 0; j < nclmt; j++) {
				clmt = climatev.get(j);
				if (clmt == null) {
					out.println();
					continue;
				}
				if ( clmt.getType()== StateMod_ReservoirClimate.CLIMATE_EVAP) {
					v.clear();
					v.add("Evaporation");
					v.add(clmt.getID());
					v.add(Double.valueOf(clmt.getWeight()));
					iline = StringUtil.formatString(v, format_4);
					out.println(iline);
				}
			}
	
			// Print the precip information
			for (j = 0; j < nclmt; j++) {
				clmt = climatev.get(j);
				if (clmt == null) {
					out.println();
					continue;
				}
				if ( clmt.getType()== StateMod_ReservoirClimate.CLIMATE_PTPX) {
					v.clear();
					v.add("Precipitatn");
					v.add(clmt.getID());
					v.add(Double.valueOf(clmt.getWeight()));
					iline = StringUtil.formatString(v, format_4);
					out.println(iline);
				}
			}
	
			// print the area capacity information
			areacapv = res.getAreaCaps();
			nareacap = areacapv.size();
			for (j = 0; j < nareacap; j++) {
				ac = areacapv.get(j);
				if (ac == null) {
					out.println();
					continue;
				}
				ch1 = "CAP-AREA" + StringUtil.formatString(j, "%3.3s");
				iline = StringUtil.formatString(ch1, "            %-12.12s");
				// Not very efficient but this file does not get written often...
				if (ac.getConten()< 100.0) {
					iline += StringUtil.formatString(ac.getConten(), "%8.2f");
				}
				else {
					iline += StringUtil.formatString(ac.getConten(), "%#8.0f");
				}
				if (ac.getSurarea()< 100.0) {
					iline += StringUtil.formatString(ac.getSurarea(), "%8.2f");
				}
				else {
					iline += StringUtil.formatString(ac.getSurarea(), "%8.0f");
				}
				iline += StringUtil.formatString(ac.getSeepage(),"%8.0f");
				out.println(iline);
			}
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
Writes a list of StateMod_Reservoir objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter 
will be wrapped in "...".  <p>This method also writes out Reservoir Area Cap,
Account, Climate Data and Collections data to separate files.  If this method 
is called with a filename parameter of "reservoirs.txt", six files will be generated:
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
@param data the list of objects to write.
@param newComments comments to add to the top of the file.
@return a list of files that were actually written, because this method controls all the secondary
filenames.
@throws Exception if an error occurs.
*/
public static List<File> writeListFile(String filename, String delimiter, boolean update,
	List<StateMod_Reservoir> data, List<String> newComments ) 
throws Exception
{	String routine = "StateMod_Reservoir.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
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
		s = fields.get(i);
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
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	List<StateMod_ReservoirAccount> accounts = new Vector<StateMod_ReservoirAccount>();
	List<StateMod_ReservoirAreaCap> areaCaps = new Vector<StateMod_ReservoirAreaCap>();
	List<StateMod_ReservoirClimate> evapClimates = new Vector<StateMod_ReservoirClimate>();
	List<StateMod_ReservoirClimate> precipClimates = new Vector<StateMod_ReservoirClimate>();
	List<StateMod_ReservoirAccount> tempVAccounts = null;
	List<StateMod_ReservoirAreaCap> tempVAreaCap = null;
	List<StateMod_ReservoirClimate> tempClimates = null;
	
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
		newComments2.add(1,"StateMod reservoir stations as a delimited list file.");
		newComments2.add(2,"See also the associated account, precipitation station, evaporation station,");
		newComments2.add(3,"content/area/seepage, and collection files.");
		newComments2.add(4,"");
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names[i] + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			res = data.get(i);
			
			line[0] = StringUtil.formatString(res.getID(),formats[0]).trim();
			line[1] = StringUtil.formatString(res.getName(),formats[1]).trim();
			line[2] = StringUtil.formatString(res.getCgoto(),formats[2]).trim();
			line[3] = StringUtil.formatString(res.getSwitch(),formats[3]).trim();
			line[4] = StringUtil.formatString(res.getRdate(), formats[4]).trim();
			line[5] = StringUtil.formatString(res.getVolmin(),formats[5]).trim();
			line[6] = StringUtil.formatString(res.getVolmax(),formats[6]).trim();
			line[7] = StringUtil.formatString(res.getFlomax(),formats[7]).trim();
			line[8] = StringUtil.formatString(res.getDeadst(),formats[8]).trim();
			line[9] = StringUtil.formatString(res.getCresdy(),formats[9]).trim();
			line[10] = StringUtil.formatString(res.getNowner(),formats[10]).trim();
			line[11] = StringUtil.formatString(
				StateMod_ReservoirClimate.getNumEvap(res.getClimates()),formats[11]).trim();
			line[12] = StringUtil.formatString(
				StateMod_ReservoirClimate.getNumPrecip(res.getClimates()),formats[12]).trim();
			
			tempVAreaCap = res.getAreaCaps();
		 	if (tempVAreaCap == null) {
				nAreaCaps = 0;
			}
			else {
				nAreaCaps = tempVAreaCap.size();
			}
			line[13] = StringUtil.formatString(nAreaCaps,formats[13]).trim();
				
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

			tempVAccounts = res.getAccounts();
			size2 = tempVAccounts.size();
			for (j = 0; j < size2; j++) {
				account = tempVAccounts.get(j);
				account.setCgoto(res.getID());
				accounts.add(account);
			}

			tempVAreaCap = res.getAreaCaps();
			size2 = tempVAreaCap.size();
			for (j = 0; j < size2; j++) {
				areaCap = tempVAreaCap.get(j);
				areaCap.setCgoto(res.getID());
				areaCaps.add(areaCap);
			}
			
			tempClimates = res.getClimates();
			size2 = tempClimates.size();
			for (j = 0; j < size2; j++) {
				climate = tempClimates.get(j);
				climate.setCgoto(res.getID());
				if (climate.getType() == StateMod_ReservoirClimate.CLIMATE_PTPX) {
				    precipClimates.add(climate);
				}
				else {
					evapClimates.add(climate);
				}
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

	int lastIndex = filename.lastIndexOf(".");
	String front = filename.substring(0, lastIndex);
	String end = filename.substring((lastIndex + 1), filename.length());

	String accountFilename = front + "_Accounts." + end;
	StateMod_ReservoirAccount.writeListFile(accountFilename, delimiter, update, accounts, newComments );	
		
	String areaCapFilename = front + "_ContentAreaSeepage." + end;
	StateMod_ReservoirAreaCap.writeListFile(areaCapFilename, delimiter, update, areaCaps, newComments );	

	String evapClimateFilename = front + "_EvapStations." + end;
	StateMod_ReservoirClimate.writeListFile(evapClimateFilename, 
		delimiter, update, evapClimates, newComments,
		StateMod_DataSet.COMP_RESERVOIR_STATION_EVAP_STATIONS );
	
	String precipClimateFilename = front + "_PrecipStations." + end;
	StateMod_ReservoirClimate.writeListFile(precipClimateFilename, 
		delimiter, update, precipClimates, newComments,
		StateMod_DataSet.COMP_RESERVOIR_STATION_PRECIP_STATIONS );

	String collectionFilename = front + "_Collections." + end;
	writeCollectionListFile(collectionFilename, delimiter, update, data, newComments );
	
	List<File> filesWritten = new Vector<File>();
	filesWritten.add ( new File(filename) );
	filesWritten.add ( new File(accountFilename) );
	filesWritten.add ( new File(areaCapFilename) );
	filesWritten.add ( new File(precipClimateFilename) );
	filesWritten.add ( new File(evapClimateFilename) );
	filesWritten.add ( new File(collectionFilename) );
	return filesWritten;
}

/**
Writes the collection data from a list of StateMod_Reservoir objects to a 
list file.  A header is printed to the top of the file, containing the commands 
used to generate the file.  Any strings in the body of the file that contain 
the field delimiter will be wrapped in "...". 
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments list of comments to add to the top of the file.
@throws Exception if an error occurs.
*/
public static void writeCollectionListFile(String filename, String delimiter, boolean update,
	List<StateMod_Reservoir> data, List<String> newComments ) 
throws Exception
{	String routine = "StateMod_Reservoir.writeCollectionListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
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
		s = fields.get(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int[] years = null;
	int numYears = 0;
	StateMod_Reservoir res = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];	
	StateMod_Reservoir_CollectionType colType = null;
	String id = null;
	StateMod_Reservoir_CollectionPartType partType = null;
	StringBuffer buffer = new StringBuffer();
	PrintWriter out = null;
	List<String> ids = null;
	
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector<String>();
		}
		else {
			newComments2 = new Vector<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod reservoir station collection information as delimited list file.");
		newComments2.add(2,"See also the associated reservoir station file.");
		newComments2.add(3,"");
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
			res = data.get(i);
			id = res.getID();
			years = res.getCollectionYears();
			if (years == null) {
				numYears = 0;
			}
			else {
				numYears = years.length;
			}
			colType = res.getCollectionType();
			partType = res.getCollectionPartType();
			//Message.printStatus(2, routine, "For " + id + " numYears=" + numYears );
			// Loop through the number of years of collection data...
			for ( int iyear = 0; iyear < numYears; iyear++) {
				ids = res.getCollectionPartIDs(years[iyear]);
				//Message.printStatus(2, routine, "Part ids for year " + years[iyear] + " = " + ids );
				// Loop through the identifiers for the specific year
				for ( int k = 0; k < ids.size(); k++ ) { 
					line[0] = StringUtil.formatString(id,formats[0]).trim();
					line[1] = StringUtil.formatString(years[iyear],formats[1]).trim();
					line[2] = StringUtil.formatString(colType.toString(),formats[2]).trim();
					line[3] = StringUtil.formatString(partType.toString(),formats[3]).trim();
					line[4] = StringUtil.formatString(ids.get(k),formats[4]).trim();
	
					buffer = new StringBuffer();	
					for (int ifield = 0; ifield < fieldCount; ifield++) {
						if (ifield > 0) {
							buffer.append(delimiter);
						}
						if (line[ifield].indexOf(delimiter) > -1) {
							line[ifield] = "\"" + line[ifield] + "\"";
						}
						buffer.append(line[ifield]);
					}
					out.println(buffer.toString());
				}
			}
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