//------------------------------------------------------------------------------
// StateMod_Well - Derived from StateMod_Data class
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 01 Feb 1999	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi	
// 10 Feb 1999  CEN, RTi		Implemented changes due to SAM review
//					of code.
// 24 Feb 2000	Steven A. Malers, RTi	Added comments to the right of return
//					flows and depletions as per Ray Bennett.
// 13 Mar 2000	SAM, RTi		Change some variable names to reflect
//					more recent StateMod documentation:
//						cdividwx -> cdividyw
//						cgoto2 -> idvcow2
//						diveffw -> divefcw
// 10 Apr 2000	CEN, RTi		Added "primary" variable
// 09 Aug 2000	SAM, RTi		Change areaw to a double.
// 18 Feb 2001	SAM, RTi		Code review.  Clean up javadoc.  Handle
//					nulls and set unused variables to null.
//					Alphabetize methods.  Update output
//					header.
// 02 Mar 2001	SAM, RTi		Correct problem with 1.7 primary flag
//					being formatted 11.5 rather than 12.5
//					as documented.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-09	SAM, RTi		Add GeoRecord reference to allow 2-way
//					connection between spatial and StateMod
//					data.
// 2002-09-19	SAM, RTi		Use isDirty() instead of setDirty() to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMWell to StateMod_Well
// 2003-06-10	JTS, RTi		* Folded dumpWellFile() into
//					  writeWellFile()
//					* Renamed parseWellFile() to 
//					  readWellFile()
// 2003-06-23	JTS, RTi		Renamed writeWellFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readWellFile() to 
//					readStateModFile()
// 2003-07-15	JTS, RTi		Changed to use new dataset design.
// 2003-08-03	SAM, RTi		Change isDirty() back to setDirty().
// 2003-08-15	SAM, RTi		Change GeoRecordNoSwing to GeoRecord.
// 2003-08-28	SAM, RTi		* Change rights to use a simple Vector,
//					  not a linked list.
//					* Clean up parameter names and Javadoc.
//					* Call setDirty() on individual objects
//					  and the data set component.
//					* Remove unneeded "num" data - get from
//					  the Vector size.
//					* Remove redundant "Well" from some
//					  methods.
//					* Support all well-related time series
//					  and clean up names.
// 2003-10-10	SAM, RTi		Add disconnectRights().
// 2003-10-16	SAM, RTi		Change innitial efficiency to 60%.
// 2003-10-21	SAM, RTi		* Add CWR, similar to diversions.
//					* Default idvcow2 to "N/A".
// 2004-02-25	SAM, RTi		Add isStateModWellFile().
// 2004-06-05	SAM, RTi		* Add methods to handle collections,
//					  similar to StateCU locations.
// 2004-07-06	SAM, RTi		* Overload the constructor to allow
//					  initialization to defaults or missing.
// 2004-07-08	SAM, RTi		* Add getPrimaryChoices() and
//					  getPrimaryDefault().
// 2004-08-25	JTS, RTi		* Added acceptChanges().
//					* Added changed().
//					* Added clone().
//					* Added compareTo().
//					* Added createBackup().
//					* Added restoreOriginal().
//					* Now implements Cloneable.
//					* Now implements Comparable.
//					* Clone status is checked via _isClone
//					  when the component is marked as dirty.
// 2004-08-26	JTS, RTi		compareTo() now handles _idvcow2.
// 2004-09-16	SAM, RTi		* Change so read and write methods
//					  adjust the path relative to the
//					  working directory.
// 2004-09-29	SAM, RTi		* Add the following for use with
//					  StateDMI only - no need to check for
//					  dirty - only set/gets on the entire
//					  array are enabled:
//						__cwr_monthly
//						__ddh_monthly
//						__calculated_efficiencies
//						__calculated_efficiency_stddevs
//						__model_efficiecies
// 2004-10-07	SAM, RTi		* Add 6 as an option for idvcomw.
// 2005-04-18	JTS, RTi		* Added writeListFile().
//					* Added writeCollectionListFile().
// 2005-08-18	SAM, RTi		* Add static data for part types to
//					  minimize errors in string use.
// 2005-10-10	SAM, RTi		* Change some javadoc - was using
//					  "diversion" instead of "well".
// 2005-11-16	SAM, RTi		Overload set/get methods for monthly
//					efficiency to take a year type, to
//					facilitate handling of non-calendar
//					year type.
// 2006-01-30	SAM, RTi		* Add hasAssociatedDiversion() to
//					  facilitate processing.
// 2006-04-09	SAM, RTi		Add _parcels_Vector data member and
//					associated methods, to help with
//					StateDMI error handling.
// 2007-04-12	Kurt Tometich, RTi		Added checkComponentData() and
//									getDataHeader() methods for check
//									file and data check support.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader
// TODO SAM 2006-04-09
// The _parcel_Vector has minimal support and is not yet considered in
// copy, clone, equals, etc.

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_IrrigationPracticeTS;
import RTi.GIS.GeoView.GeoRecord;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;

/**
This class stores all relevant data for a StateMod well.  
*/

public class StateMod_Well 
extends StateMod_Data
implements Cloneable, Comparable, StateMod_ComponentValidator {
/**
Well id to use for daily data.
*/
protected String _cdividyw;
/**
Well capacity(cfs).
*/
private double _divcapw;
/**
Diversion this well is tied to ("N/A" if not tied to a diversion).
*/
private String _idvcow2;
/**
Demand code.
*/
private int _idvcomw;
/**
System efficiency(%).
*/
private double _divefcw;
/**
Irrigated area associated with well.
*/
private double _areaw;
/**
Use type.
*/
private int _irturnw;
/**
Irrigated acreage source.
*/
private int _demsrcw;
/**
12 efficiency values. 
*/
private double _diveff[];
/**
Return flow data.
*/
private List _rivret;
/**
Depletion data.
*/
private List _depl;
/**
Historical time series (monthly).
*/
private MonthTS _pumping_MonthTS;
/**
12 monthly and annual average over period, used by StateDMI.
*/
private double [] __weh_monthly = null;
/**
Historical time series (daily).
*/
private DayTS _pumping_DayTS;
/**
Demand time series.
*/
private MonthTS _demand_MonthTS;
/**
Daily demand time series.
*/
private DayTS _demand_DayTS;
/**
Irrigation practice time series.
*/
private StateCU_IrrigationPracticeTS _ipy_YearTS;
/**
Consumptive water requirement.
*/
private MonthTS _cwr_MonthTS;
/**
12 monthly and annual average over period, used by StateDMI.
*/
private double [] __cwr_monthly = null;
/**
Time series - only used when idvcow2 is "N/A".
*/
private DayTS _cwr_DayTS;
/**
Well rights.
*/
private List _rights;
/**
Priority switch.
*/
private double _primary;
/**
Link to spatial data.
*/
private GeoRecord _georecord;

/**
List of parcel data, in particular to allow StateDMI to detect when a well had no data.
*/
protected List _parcel_Vector = new Vector();

// Collections are set up to be specified by year for wells, using parcels as the parts.

/**
Types of collections.  An aggregate merges the water rights/permits whereas
a system keeps all the water rights but just has one ID.
*/
public static String COLLECTION_TYPE_AGGREGATE = "Aggregate";
public static String COLLECTION_TYPE_SYSTEM = "System";

public static String COLLECTION_PART_TYPE_DITCH = "Ditch";
public static String COLLECTION_PART_TYPE_PARCEL = "Parcel";
public static String COLLECTION_PART_TYPE_WELL = "Well";

private String __collection_type = StateMod_Util.MISSING_STRING;

/**
Used by DMI software - currently no options.
*/
private String __collection_part_type = COLLECTION_PART_TYPE_PARCEL;

/**
The identifiers for data that are collected - null if not a collection location.
This is actually a list of lists where the __collection_year is the first dimension.
This is ugly but need to use the code to see if it can be made cleaner.
*/
private List __collection_Vector = null;

/**
An array of years that correspond to the aggregate/system.  Well collections are defined by year.
*/
private int [] __collection_year = null;

/**
The division that corresponds to the aggregate/system.  Currently it is expected that the same division
number is assigned to all the data.
*/
private int __collection_div = StateMod_Util.MISSING_INT;

/**
The following are used only by StateDMI and do not needed to be handled in
comparison, initialization, etc.
*/
private double [] __calculated_efficiencies = null;
private double [] __calculated_efficiency_stddevs = null;
private double [] __model_efficiencies = null;

/**
Constructor - initialize to default values.
*/
public StateMod_Well()
{	this ( true );
}

/**
Constructor.
@param initialize_defaults If true, initialize data to reasonable values.  If
false, initialize to missing values.
*/
public StateMod_Well ( boolean initialize_defaults )
{	super();
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
Add depletion node to the vector of depletion nodes.  Updates the variable
which tracks the number of depletion nodes for this well.
@param depl depletion data object
*/
public void addDepletion(StateMod_ReturnFlow depl)
{	if ( depl == null ) {
		return;
	}
	_depl.add(depl);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
	}
}

/**
Add return flow node to the vector of return flow nodes.  Also, updates
the number of return flow nodes variable.
@param rivret return flow
*/
public void addReturnFlow(StateMod_ReturnFlow rivret)
{	if ( rivret == null ) {
		return;
	}
	_rivret.add(rivret);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
	}
}

/**
Adds a right to the rights linked list
*/
public void addRight(StateMod_WellRight right)
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

/*
	Message.printStatus(1, "", "'" + _cdividyw + "' : '" 
		+ ((StateMod_Well)_original)._cdividyw + "'");
	Message.printStatus(1, "", "'" + _divcapw + "' : '" 
		+ ((StateMod_Well)_original)._divcapw + "'");
	Message.printStatus(1, "", "'" + _idvcow2 + "' : '" 
		+ ((StateMod_Well)_original)._idvcow2 + "'");
	Message.printStatus(1, "", "'" + _idvcomw + "' : '" 
		+ ((StateMod_Well)_original)._idvcomw + "'");
	Message.printStatus(1, "", "'" + _divefcw + "' : '" 
		+ ((StateMod_Well)_original)._divefcw + "'");
	Message.printStatus(1, "", "'" + _areaw + "' : '" 
		+ ((StateMod_Well)_original)._areaw + "'");
	Message.printStatus(1, "", "'" + _irturnw + "' : '" 
		+ ((StateMod_Well)_original)._irturnw + "'");
	Message.printStatus(1, "", "'" + _demsrcw + "' : '" 
		+ ((StateMod_Well)_original)._demsrcw + "'");
	Message.printStatus(1, "", "'" + _primary + "' : '" 
		+ ((StateMod_Well)_original)._primary + "'");
	if (_diveff == null && ((StateMod_Well)_original)._diveff == null) {
		Message.printStatus(1, "", "BOTH NULL");
	}
	else if (_diveff == null) {
		Message.printStatus(1, "", "1 NULL");
	}
	else if (((StateMod_Well)_original)._diveff == null) {
		Message.printStatus(1, "", "2 NULL");
	}
	else {
		for (int i = 0; i < _diveff.length; i++) {
			Message.printStatus(1, "", "'" + _diveff[i] + "' : '" 
				+ ((StateMod_Well)_original)._diveff[i] 
				+ "' (" + i + ")");
		}
	}
*/
	return true;
}

/**
Performs data checks for the capacity portion of this component.
@param wer_Vector List of water rights.
@return String[] array of data that has been checked.  Returns null if there were no problems found.
 */
public String[] checkComponentData_Capacity( List wer_Vector, int count )
{
	double decree;
	double decree_sum;
	int onoff = 0;		// On/off switch for right
	int size_rights = 0;
	String id_i = null;
	List rights = null;
	id_i = getID();
	StateMod_WellRight wer_i = null;
	rights = StateMod_Util.getRightsForStation ( id_i, wer_Vector );
	size_rights = 0;
	if ( rights != null ) {
		size_rights = rights.size();
	}
	if ( size_rights == 0 ) {
		return null;
	}
	// Get the sum of the rights, assuming that all should be
	// compared against the capacity (i.e., sum of rights at the
	// end of the period will be compared with the current well capacity)...
	decree_sum = 0.0;
	for ( int iright = 0; iright < size_rights; iright++ ) {
		wer_i = (StateMod_WellRight)rights.get(iright);
		decree = wer_i.getDcrdivw();
		onoff = getSwitch();
		if ( decree < 0.0 ) {
			// Ignore - missing values will cause a bad sum.
			continue;
		}
		if ( onoff <= 0 ) {
			// Subtract the decree...
			decree_sum -= decree;
		}
		else {
			// Add the decree...
			decree_sum += decree;
		}
	}
	// Compare to a whole number, which is the greatest precision for documented files.
	if ( !StringUtil.formatString(decree_sum,"%.2f").equals(
		StringUtil.formatString(getDivcapw(),"%.2f")) ) {
		// new format for check file
		String [] data_table = {
			StringUtil.formatString(++count,"%4d"),
			StringUtil.formatString(id_i,"%-12.12s"),
			getName(),
			StringUtil.formatString(getCollectionType(),"%-10.10s"),
			StringUtil.formatString(getDivcapw(),"%9.2f"),
			StringUtil.formatString(decree_sum,"%9.2f"),
			StringUtil.formatString( size_rights,"%8d"), };
		return StateMod_Util.checkForMissingValues( data_table );
	}
	return null;
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_Well w = (StateMod_Well)super.clone();
	w._isClone = true;

	// The following are not cloned because there is no need to.  
	// The cloned values are only used for comparing between the 
	// values that can be changed in a single GUI.  The following
	// Vectors' data have their changes committed in other GUIs.	
	w._rivret = _rivret;
	w._rights = _rights;
	w._depl = _depl;

	if (_diveff == null) {
		w._diveff = null;
	}
	else {
		w._diveff = (double[])_diveff.clone();
	}
	return w;
}

/**
Compares this object to another StateMod_Well object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_Well w = (StateMod_Well)o;

	res = _cdividyw.compareTo(w._cdividyw);
	if (res != 0) {
		return res;
	}

	if (_divcapw < w._divcapw) {
		return -1;
	}
	else if (_divcapw > w._divcapw) {
		return 1;
	}

	res = _idvcow2.compareTo(w._idvcow2);
	if (res != 0) {
		return res;
	}

	if (_idvcomw < w._idvcomw) {
		return -1;
	}
	else if (_idvcomw > w._idvcomw) {
		return 1;
	}

	if (_divefcw < w._divefcw) {
		return -1;
	}
	else if (_divefcw > w._divefcw) {
		return 1;
	}

	if (_areaw < w._areaw) {
		return -1;
	}
	else if (_areaw > w._areaw) {
		return 1;
	}

	if (_irturnw < w._irturnw) {
		return -1;
	}
	else if (_irturnw > w._irturnw) {
		return 1;
	}

	if (_demsrcw < w._demsrcw) {
		return -1;
	}
	else if (_demsrcw > w._demsrcw) {
		return 1;
	}

	if (_primary < w._primary) {
		return -1;
	}
	else if (_primary > w._primary) {
		return 1;
	}

	if (_diveff == null && w._diveff == null) {
		return 0;
	}
	else if (_diveff == null && w._diveff != null) {
		return -1;
	}
	else if (_diveff != null && w._diveff == null) {
		return 1;
	}
	else {
		int size1 = _diveff.length;
		int size2 = w._diveff.length;
		if (size1 < size2) {
			return -1;
		}
		else if (size1 > size2) {
			return 1;
		}
		
		for (int i = 0; i < size1; i++) {
			if (_diveff[i] < w._diveff[i]) {
				return -1;
			}
			else if (_diveff[i] > w._diveff[i]) {
				return 1;
			}
		}
	}

	return 0;
}

/**
Creates a copy of the object for later use in checking to see if it was changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_Well)_original)._isClone = false;
	_isClone = true;
}

/**
Connect all water rights to the corresponding wells.
This routines doesn't add an element to an array - the array
already exists. This method just connects next and previous pointers.
@param wells all wells
@param rights all rights
*/
public static void connectAllRights ( List wells, List rights ) {
	if ( (wells == null) || (rights == null) ) {
		return;
	}
	int num_wells = wells.size();
	
	StateMod_Well well = null;
	for (int i = 0; i < num_wells; i++) {
		well = (StateMod_Well)wells.get(i);
		if (well == null) {
			continue;
		}
		well.connectRights(rights);
	}
}

/**
Connect the wells time series to this instance.
@param wells all wells 
@param cwr_MonthTS list of monthly consumptive water requirement time series, or null.
@param cwr_DayTS list of daily consumptive water requirement time series, or null.
*/
public static void connectAllTS ( List wells, List pumping_MonthTS, List pumping_DayTS,
	List demand_MonthTS, List demand_DayTS, List ipy_YearTS, List cwr_MonthTS, List cwr_DayTS )
{	if (wells == null) {
		return;
	}

	int num_wells = wells.size();
	
	StateMod_Well well = null;
	for (int i = 0; i < num_wells; i++) {
		well = (StateMod_Well)wells.get(i);
		if (well == null) {
			continue;
		}
		if ( pumping_MonthTS != null ) {
			well.connectPumpingMonthTS(pumping_MonthTS);
		}
		if ( pumping_DayTS != null ) {
			well.connectPumpingDayTS(pumping_DayTS);
		}
		if ( demand_MonthTS != null ) {
			well.connectDemandMonthTS(demand_MonthTS);
		}
		if ( demand_DayTS != null ) {
			well.connectDemandDayTS(demand_DayTS);
		}
		if ( ipy_YearTS != null ) {
			well.connectIrrigationPracticeYearTS ( ipy_YearTS );
		}
		if ( cwr_MonthTS != null ) {
			well.connectCWRMonthTS ( cwr_MonthTS );
		}
		if ( cwr_DayTS != null ) {
			well.connectCWRDayTS ( cwr_DayTS );
		}
	}
}

/**
Connect daily CWR series pointer.  The connection is made using the value of "cdividyw" for the well.
@param tslist demand time series
*/
public void connectCWRDayTS ( List tslist )
{	if ( tslist == null) {
		return;
	}
	_cwr_DayTS = null;
	int num_TS = tslist.size();

	DayTS ts;
	for (int i = 0; i < num_TS; i++) {
		ts = (DayTS)tslist.get(i);
		if ( ts == null ) {
			return;
		}
		if (_cdividyw.equalsIgnoreCase(ts.getLocation())) {
			_cwr_DayTS = ts;
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect monthly CWR time series pointer.  The time series name is set to that of the well.
@param tslist Time series list.
*/
public void connectCWRMonthTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	int num_TS = tslist.size();

	MonthTS ts;
	_cwr_MonthTS = null;
	for ( int i = 0; i < num_TS; i++ ) {
		ts = (MonthTS)tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		if ( _id.equalsIgnoreCase(ts.getLocation()) ) {
			_cwr_MonthTS = ts;
			ts.setDescription ( getName() );
			break;
		}
	}
}

/**
Connect daily demand time series pointer to this object.
@param tslist Daily demand time series.
*/
public void connectDemandDayTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	_demand_DayTS = null;

	int num_TS = tslist.size();

	DayTS ts = null;
	for (int i = 0; i < num_TS; i++) {
		ts = (DayTS)tslist.get(i);
		if (ts == null) {
			continue;
		}
		if (_cdividyw.equalsIgnoreCase(ts.getLocation())) {
			_demand_DayTS = ts;
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect monthly demand time series pointer to this object.
@param tslist demand time series
*/
public void connectDemandMonthTS ( List tslist )
{	if ( tslist == null) {
		return;
	}
	_demand_MonthTS = null;
	int num_TS = tslist.size();

	MonthTS ts = null;
	for (int i = 0; i < num_TS; i++) {
		ts = (MonthTS)tslist.get(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getLocation())) {
			_demand_MonthTS = ts;
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect the irrigation practice TS object.
@param tslist Time series list.
*/
public void connectIrrigationPracticeYearTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	int num_TS = tslist.size();

	_ipy_YearTS = null;
	StateCU_IrrigationPracticeTS ipy_YearTS;
	for ( int i = 0; i < num_TS; i++ ) {
		ipy_YearTS = (StateCU_IrrigationPracticeTS)tslist.get(i);
		if ( ipy_YearTS == null ) {
			continue;
		}
		if ( _id.equalsIgnoreCase(ipy_YearTS.getID()) ) {
			_ipy_YearTS = ipy_YearTS;
			break;
		}
	}
}

/**
Connect daily pumping time series pointer to this object.
@param tslist Daily pumping time series.
*/
public void connectPumpingDayTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	_pumping_DayTS = null;

	int num_TS = tslist.size();

	DayTS ts = null;
	for (int i = 0; i < num_TS; i++) {
		ts = (DayTS)tslist.get(i);
		if (ts == null) {
			continue;
		}
		if (_cdividyw.equalsIgnoreCase(ts.getLocation())) {
			_pumping_DayTS = ts;
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect monthly pumping time series pointer to this object.
@param tslist monthly pumping time series
*/
public void connectPumpingMonthTS ( List tslist )
{	if ( tslist == null) {
		return;
	}
	_pumping_MonthTS = null;
	int num_TS = tslist.size();

	MonthTS ts = null;
	for (int i = 0; i < num_TS; i++) {
		ts = (MonthTS)tslist.get(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getLocation())) {
			_pumping_MonthTS = ts;
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Create a list of references to rights for this well.
@param rights all rights
*/
public void connectRights(List rights) {
	if (rights == null) {
		return;
	}

	int num_rights = rights.size();

	StateMod_WellRight right = null;
	for (int i = 0; i < num_rights; i++) {
		right = (StateMod_WellRight)rights.get(i);
		if (right == null) {
			continue;
		} 
		if (_id.equalsIgnoreCase(right.getCgoto())) {
			_rights.add ( right );
		}
	}
}

/**
delete depletion table record at a specified index
@param index index desired depletion data object to delete
@exception ArrayIndexOutOfBounds throws exception if unable to remove the 
specified depletion data object
*/
public void deleteDepletionAt(int index) {
	_depl.remove(index);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
	}
}

/**
Delete return flow node at a specified index.  Also, updates the number of return flow nodes variable.
@param index index of return flow data object to delete
*/
public void deleteReturnFlowAt(int index) {
	_rivret.remove(index);
	setDirty ( true );
	if ( !_isClone && _dataset != null ) {
		_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
	}
}

// TODO - in the GUI need to decide if the right is actually removed from the main list
/**
Remove right from list.  A comparison on the ID is made.
@param right Right to remove.  Note that the right is only removed from the
list for this well and must also be removed from the main well right list.
*/
public void disconnectRight ( StateMod_WellRight right )
{	if (right == null) {
		return;
	}
	int size = _rights.size();
	StateMod_WellRight right2;
	// Assume that more than on instance can exist, even though this is not allowed...
	for ( int i = 0; i < size; i++ ) {
		right2 = (StateMod_WellRight)_rights.get(i);
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
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_pumping_MonthTS = null;
	_pumping_DayTS = null;
	_demand_MonthTS = null;
	_demand_DayTS = null;
	_rights = null;
	_rivret = null;
	_depl = null;
	_diveff = null;
	_idvcow2 = null;
	_cdividyw = null;
	_georecord = null;
	super.finalize();
}

/**
@return the area(This is currently not being used but is provided for consistency within this class).
*/
public double getAreaw() {
	return _areaw;
}

/**
Return the average monthly CWR (12 monthly values + annual average), for the
data set calendar type.  This is ONLY used by StateDMI and does not need
to be considered in comparison code.
*/
public double [] getAverageMonthlyCWR ()
{	return __cwr_monthly;
}

/**
Return the average monthly historical pumping (12 monthly values + annual
average), for the data set calendar type.  This is ONLY used by StateDMI and
does not need to be considered in comparison code.
*/
public double [] getAverageMonthlyHistoricalPumping ()
{	return __weh_monthly;
}

/**
Return the average monthly efficiencies calculated from CWR and historical
pumping (12 monthly values + annual average), for the data set calendar type.
This is ONLY used by StateDMI and does not need to be considered in comparison code.
*/
public double [] getCalculatedEfficiencies()
{	return __calculated_efficiencies;
}

/**
Return the standard deviation of monthly efficiencies calculated from CWR and
historical pumping (12 monthly values + annual average), for the data set calendar type.
This is ONLY used by StateDMI and does not need to be considered in comparison code.
*/
public double [] getCalculatedEfficiencyStddevs()
{	return __calculated_efficiency_stddevs;
}

/**
@return the well id to use for daily data
*/
public String getCdividyw() {
	return _cdividyw;
}

/**
Return the collection part division the specific year.  Currently it is
expected that the user always uses the same division.
@return the division for the collection, or 0.
*/
public int getCollectionDiv ()
{	return __collection_div;
}

/**
Return the collection part ID list for the specific year.  For wells, collections are by year.
@return the list of collection part IDS, or null if not defined.
*/
public List getCollectionPartIDs ( int year )
{	if ( __collection_Vector.size() == 0 ) {
		return null;
	}
	if ( __collection_part_type.equalsIgnoreCase("Ditch") || __collection_part_type.equalsIgnoreCase("Well") ) {
		// The list of part IDs will be the first and only list...
		return (List)__collection_Vector.get(0);
	}
	else if ( __collection_part_type.equalsIgnoreCase("Parcel") ) {
		// The list of part IDs needs to match the year.
		for ( int i = 0; i < __collection_year.length; i++ ) {
			if ( year == __collection_year[i] ) {
				return (List)__collection_Vector.get(i);
			}
		}
	}
	return null;
}

/**
Return the collection part type (see COLLECTION_PART_TYPE_*).
*/
public String getCollectionPartType()
{	return __collection_part_type;
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
Get daily consumptive water requirement time series.
*/
public DayTS getConsumptiveWaterRequirementDayTS() {
	return _cwr_DayTS;
}

/**
Get monthly consumptive water requirement time series.
*/
public MonthTS getConsumptiveWaterRequirementMonthTS() {
	return _cwr_MonthTS;
}

/**
Returns the table header for StateMod_Well data tables.
@return String[] header - Array of header elements.
*/
public static String[] getDataHeader()
{
	return new String[] {
			"Num",
			"Well Station ID",
			"Well Station Name",
			"Collection Type",
			"# Parcels for Well Station",
			"Total Parcel Area",
			"# Parcels with Wells",
			"Parcels with Well Area (ACRE)"};
}

/**
@return daily demand time series
*/
public DayTS getDemandDayTS() {
	return _demand_DayTS;
}

/**
@return monthly demand time series
*/
public MonthTS getDemandMonthTS() {
	return _demand_MonthTS;
}

/**
@return the irrigated acreage source
*/
public int getDemsrcw() {
	return _demsrcw;
}

/**
Return a list of demand source option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Irrigated acres from GIS", if include_notes is true.
@return a list of demand source option strings, for use in GUIs.
@param includeNotes Indicate whether notes should be included.
*/
public static List getDemsrcwChoices ( boolean includeNotes )
{
	return StateMod_Diversion.getDemsrcChoices(includeNotes);
}

/**
@return the depletion at a particular index
@param index index desired to retrieve
@exception ArrayIndexOutOfBounds throws exception if unable to retrieve the specified depletion node
*/
public StateMod_ReturnFlow getDepletion(int index) {
	return (StateMod_ReturnFlow)_depl.get(index);
}

/**
@return the depletion vector
*/
public List getDepletions() {
	return _depl;
}

/**
@return the well capacity
*/
public double getDivcapw() {
	return _divcapw;
}

/*
@return the system efficiency
*/
public double getDivefcw() {
	return _divefcw;
}

/**
@return the variable efficiency
*/
public double getDiveff(int index) {
	return _diveff[index];
}

/**
Return the system efficiency for the specified month index, where the month
is always for calendar year (0=January).
@param index 0-based monthly index (0=January).
@param yeartype The year type for the well stations file (consistent with
the control file for a full data set).  Recognized values are:
<ol>
<li>	"Calendar", "CYR" (Jan - Dec).</li>
<li>	"Irrigation", "IYR" (Oct - Sep).</li>
<li>	"Water", "WYR" (Nov - Oct).</li>
</ol>
*/
public double getDiveff ( int index, String yeartype )
{	// Adjust the index if necessary based on the year type...
	if ( yeartype == null ) {
		// Assume calendar.
	}
	else if ( yeartype.equalsIgnoreCase("Water") ||
		yeartype.equalsIgnoreCase("WYR") ) {
		index = TimeUtil.convertCalendarMonthToCustomMonth ( (index + 1), 10 ) - 1;
	}
	else if ( yeartype.equalsIgnoreCase("Irrigation") ||
		yeartype.equalsIgnoreCase("IYR") ) {
		index = TimeUtil.convertCalendarMonthToCustomMonth ( (index + 1), 11 ) - 1;
	}
	return _diveff[index];
}

/**
Get the geographical data associated with the well.
@return the GeoRecord for the well.
*/
public GeoRecord getGeoRecord() {
	return _georecord;
}

/**
Returns the table header for capacity data tables.
@return String[] header - Array of header elements.
 */
public static String[] getCapacityHeader()
{
	return new String[] {
			"Num",
			"Well Station ID",
			"Well Station Name",
			"Collection Type",
			"Well Capacity (CFS)",
			"Sum of Rights (CFS)",
			"Number of Rights (CFS)", };
}

/**
Return the average monthly efficiencies to be used for modeling (12 monthly values + annual average),
for the data set calendar type.  This is ONLY used by StateDMI and does not need
to be considered in comparison code.
*/
public double [] getModelEfficiencies()
{	return __model_efficiencies;
}

/**
@return historical time series for this well.
*/
public DayTS getPumpingDayTS() {
	return _pumping_DayTS;
}

/**
@return historical time series for this well.
*/
public MonthTS getPumpingMonthTS() {
	return _pumping_MonthTS;
}

/**
@return the demand code(see StateMod documentation for acceptable values)
*/
public int getIdvcomw() {
	return _idvcomw;
}

/**
Return a list of monthly demand type option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Monthly total demand", if include_notes is true.
@return a list of monthly demand type option strings, for use in GUIs.
@param include_notes Indicate whether notes should be included.
*/
public static List getIdvcomwChoices ( boolean include_notes )
{	List v = new Vector(6);
	v.add ( "1 - Monthly total demand" );
	v.add ( "2 - Annual total demand" );
	v.add ( "3 - Monthly irrigation water requirement" );
	v.add ( "4 - Annual irrigation water requirement" );
	v.add ( "5 - Estimate to be zero" );
	v.add ( "6 - Diversion+well demand is with diversion" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i, StringUtil.getToken( (String)v.get(i), " ", 0, 0) );
		}
	}
	return v;
}

/**
Return the default monthly demand type choice.  This can be used by GUI code
to pick a default for a new well.
@return the default monthly demand type choice.
*/
public static String getIdvcomDefault ( boolean include_notes )
{	if ( include_notes ) {
		return "1 - Monthly total demand";
	}
	else {
		return "1";
	}
}

/**
@return the diversion this well is tied to
*/
public String getIdvcow2() {
	return _idvcow2;
}

/**
Get yearly irrigation practice time series.
*/
public StateCU_IrrigationPracticeTS getIrrigationPracticeYearTS() {
	return _ipy_YearTS;
}

/**
@return the use type
*/
public int getIrturnw() {
	return _irturnw;
}

/**
Return a list of use type option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Irrigation", if include_notes is true.
@return a list of use type option strings, for use in GUIs.
@param includeNotes Indicate whether notes should be included.
*/
public static List getIrturnwChoices ( boolean includeNotes )
{
	return StateMod_Diversion.getIrturnChoices(includeNotes);
}

/**
Get the last right associated with the well.
*/
public StateMod_WellRight getLastRight()
{	if ( (_rights == null) || (_rights.size() == 0) ) {
		return null;
	}
	return (StateMod_WellRight)_rights.get(_rights.size() - 1);
}

/**
@return the number of return flow locations.
There is not a set function for this data because it is automatically
calculated whenever a return flow is added or removed.
*/
public int getNrtnw() {
	return _rivret.size();
}

/**
@return the number of depletion locations
There is not a set function for this data because it is automatically
calculated whenever a depletion location is added or removed.
*/
public int getNrtnw2() {
	return _depl.size();
}

/**
Return the Vector of parcels.
@return the Vector of parcels.
*/
public List getParcels()
{	return _parcel_Vector;
}

/**
@return the priority switch
*/
public double getPrimary() {
	return _primary;
}

/**
Return a list of primary option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and
"0 - Off", if include_notes is true.
@return a list primary switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List getPrimaryChoices ( boolean include_notes )
{	List v = new Vector(52);
	v.add ( "0 - Use water right priorities" );
	for ( int i = 1000; i < 50000; i += 1000 ) {
		v.add ( "" + i + " - Well water rights will be adjusted by " + i );
	}
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i,StringUtil.getToken( (String)v.get(i), " ", 0, 0) );
		}
	}
	return v;
}

/**
Return the default primary switch choice.  This can be used by GUI code to pick a default for a new well.
@return the default primary choice.
*/
public static String getPrimaryDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return ( "0 - Use water right priorities" );
	}
	else {
		return "0";
	}
}

/**
@return the return flow at a particular index
@param index index desired to retrieve
@exception ArrayIndexOutOfBounds throws exception if unable to retrieve the specified return flow node
*/
public StateMod_ReturnFlow getReturnFlow(int index) {
	return (StateMod_ReturnFlow)_rivret.get(index);
}

/**
@return the return flow vector
*/
public List getReturnFlows() {
	return _rivret;
}

/**
Return the right associated with the given index.  If index
number of rights don't exist, null will be returned.
@param index desired right index
*/
public StateMod_WellRight getRight(int index)
{	if ( (index < 0) || (index >= _rights.size()) ) {
		return null;
	}
	else {
		return (StateMod_WellRight)_rights.get(index);
	}
}

/** 
@return rights Vector
*/
public List getRights() {
	return _rights;
}

/**
Indicate if the well is a DivAndWell (D&W, DW) node, indicated by an associated diversion ID in idvcow2
@return true if "idvcow2" is not blank or "NA", false otherwise.
*/
public boolean hasAssociatedDiversion ()
{	if ( (_idvcow2 != null) && !_idvcow2.equals("") && !_idvcow2.equalsIgnoreCase("NA") ) {
		return true;
	}
	return false;
}

/**
Indicate whether the well has groundwater only supply.  This will
be the case if the location is a collection with part type of "Parcel".
*/
public boolean hasGroundwaterOnlySupply ()
{
	if ( isCollection() && getCollectionPartType().equalsIgnoreCase("Parcel")) {
		// TODO SAM 2007-05-11 Rectify part types with StateCU
		return true;
	}
	return false;
}

/**
Indicate whether the well has surface water supply.  This will
be the case if the location is NOT a groundwater only supply location.
*/
public boolean hasSurfaceWaterSupply ()
{
	if ( hasGroundwaterOnlySupply() ) {
		return false;
	}
	return true;
}

/**
Set default values for all arguments
@param initialize_defaults If true, initialize data to reasonable values.  If
false, initialize to missing values.
*/
private void initialize ( boolean initialize_defaults )
{	_smdata_type = StateMod_DataSet.COMP_WELL_STATIONS;
	_rivret = new Vector(10);
	_depl = new Vector(10);
	_pumping_MonthTS = null;
	_pumping_DayTS = null;
	_demand_MonthTS = null;
	_demand_DayTS = null;
	_ipy_YearTS = null;
	_cwr_MonthTS = null;
	_cwr_DayTS = null;
	_rights = new Vector();
	_georecord = null;

	if ( initialize_defaults ) {
		_cdividyw = "0";	// Estimate average daily from monthly data.
		_idvcow2 = "N/A";
		_diveff = new double[12];
		for ( int i = 0; i < 12; i++ ) {
			_diveff[i] = 60.0;
		}
		_divcapw = 0;
		_idvcomw = 1;
		_divefcw = -60.0; // Indicate to use monthly efficiencies
		_areaw = 0.0;
		_irturnw = 0;
		_demsrcw = 1;
		_primary = 0;
	}
	else {
		_cdividyw = "";
		_idvcow2 = "";
		_diveff = new double[12];
		for (int i=0; i<12; i++) {
			_diveff[i] = StateMod_Util.MISSING_DOUBLE;
		}
		_divcapw = StateMod_Util.MISSING_DOUBLE;
		_idvcomw = StateMod_Util.MISSING_INT;
		_divefcw = StateMod_Util.MISSING_DOUBLE;
		_areaw = StateMod_Util.MISSING_DOUBLE;
		_irturnw = StateMod_Util.MISSING_INT;
		_demsrcw = StateMod_Util.MISSING_INT;
		_primary = StateMod_Util.MISSING_INT;
	}
}

/**
Indicate whether the well is a collection (an aggregate or system).
@return true if the well is an aggregate or system.
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
Indicate whether a file is a StateMod well file.  Currently the only
check that is done is to see if the file name ends in "wes".
@param filename File name.
@return true if the file appears to be a well file, false if not.
*/
public static boolean isStateModWellFile ( String filename )
{	if ( StringUtil.endsWithIgnoreCase(filename,".wes") ) {
		return true;
	}
	return false;
}

/**
Read a well input file.
@param filename name of file containing well information
@return status(always 0 since exception handling is now used)
*/
public static List readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_Well.readStateModFile";
	List theWellStations = new Vector();
	int [] format_1 = {
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_DOUBLE };
	int [] format_1w = {
				12,
				24,
				12,
				8,
				8,
				1,
				12,
				1,
				12 };
	int [] format_2 = {
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_INTEGER };
	int [] format_2w = {
				36,
				12,
				8,
				8,
				8,
				8,
				8,
				8,
				8 };
	int [] format_4 = {
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER };
	int [] format_4w = {
				36,
				12,
				8,
				8 };
	String iline = null;
	int linecount = 0;
	String s = null;
	List v = new Vector(9);
	BufferedReader in = null;
	StateMod_Well aWell = null;
	StateMod_ReturnFlow aReturnNode = null;
	List effv = null;
	int nrtn, ndepl;

	Message.printStatus(1, routine, "Reading well file: " + filename);
	
	try {
		in = new BufferedReader(new FileReader(	IOUtil.getPathUsingWorkingDir(filename)));
		++linecount;
		while ((iline = in.readLine()) != null) {
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}

			aWell = new StateMod_Well();

			StringUtil.fixedRead(iline, format_1, format_1w, v);
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "iline: " + iline);
			}
			aWell.setID(((String)v.get(0)).trim());
			aWell.setName(((String)v.get(1)).trim());
			aWell.setCgoto(((String)v.get(2)).trim());
			aWell.setSwitch((Integer)v.get(3));
			aWell.setDivcapw((Double)v.get(4));
			aWell.setCdividyw(((String)v.get(5)).trim());
			aWell.setPrimary((Double)v.get(6));

			// user data

			iline = in.readLine();
			StringUtil.fixedRead(iline, format_2, format_2w, v);
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "iline: " + iline);
			}
			aWell.setIdvcow2(((String)v.get(0)).trim());
			aWell.setIdvcomw((Integer)v.get(1));
			// Don't set the number of return flow data get(2) or depletion data get(3) because
			// those will be calculated
			nrtn = ((Integer)v.get(2)).intValue();
			ndepl = ((Integer)v.get(3)).intValue();
			aWell.setDivefcw((Double)v.get(4));
			aWell.setAreaw((Double)v.get(5));
			aWell.setIrturnw((Integer)v.get(6));
			aWell.setDemsrcw((Integer)v.get(7));
			if (aWell.getDivefcw()>= 0) {
				// Efficiency line won't be included - set each value to the average
				for (int i = 0; i < 12; i++) {
					aWell.setDiveff(i, aWell.getDivefcw());
				}
			}
			else {
				// 12 efficiency values
				iline = in.readLine();
				effv = StringUtil.breakStringList(iline, " ", StringUtil.DELIM_SKIP_BLANKS);
				for (int i = 0; i < 12; i++) {
					aWell.setDiveff(i, (String)effv.get(i));
				}
			}

			// return flow data

			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "Number of return flows: " + nrtn);
			}
			for (int i = 0; i < nrtn; i++) {
				iline = in.readLine();
				StringUtil.fixedRead(iline, format_4, format_4w, v);
				if (Message.isDebugOn) {
					Message.printDebug(50, routine, "Fixed read returned " + v.size()+ " elements");
				}

				aReturnNode = new StateMod_ReturnFlow(StateMod_DataSet.COMP_WELL_STATIONS);
				s = ((String)v.get(0)).trim();
				if (s.length()<= 0) {
					aReturnNode.setCrtnid(s);
					Message.printWarning(2, routine, "Return node for structure \"" + aWell.getID()+ "\" is blank. ");
				}
				else {	
					aReturnNode.setCrtnid(s);
				}

				aReturnNode.setPcttot( ((Double)v.get(1)));
				aReturnNode.setIrtndl( ((Integer)v.get(2)));
				aWell.addReturnFlow(aReturnNode);
			}

			// depletion data

			for (int i = 0; i < ndepl; i++) {
				iline = in.readLine();
				StringUtil.fixedRead(iline, format_4, format_4w, v);

				aReturnNode = new StateMod_ReturnFlow( StateMod_DataSet.COMP_WELL_STATIONS);
				s = ((String)v.get(0)).trim();
				if (s.length() <= 0) {
					aReturnNode.setCrtnid(s);
					Message.printWarning(2, routine, 
						"Return node for structure \"" + aWell.getID()+ "\" is blank. ");
				}
				else {	
					aReturnNode.setCrtnid(s);
				}

				aReturnNode.setPcttot( ((Double)v.get(1)));
				aReturnNode.setIrtndl( ((Integer)v.get(2)));
				aWell.addDepletion(aReturnNode);
			}

			theWellStations.add(aWell);
		}
	} 
	catch (Exception e) {
		routine = null;
		format_1 = null;
		format_1w = null;
		format_2 = null;
		format_2w = null;
		format_4 = null;
		format_4w = null;
		s = null;
		v = null;
		if (in != null) {
			in.close();
		}
		in = null;
		aWell = null;
		aReturnNode = null;
		effv = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
	routine = null;
	format_1 = null;
	format_1w = null;
	format_2 = null;
	format_2w = null;
	format_4 = null;
	format_4w = null;
	iline = null;
	s = null;
	v = null;
	if (in != null) {
		in.close();
	}
	in = null;
	aWell = null;
	aReturnNode = null;
	effv = null;
	return theWellStations;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_Well w = (StateMod_Well)_original;
	super.restoreOriginal();

	_cdividyw = w._cdividyw;
	_divcapw = w._divcapw;
	_idvcow2 = w._idvcow2;
	_idvcomw = w._idvcomw;
	_divefcw = w._divefcw;
	_areaw = w._areaw;
	_irturnw = w._irturnw;
	_demsrcw = w._demsrcw;
	_diveff = w._diveff;
	_primary = w._primary;
	_isClone = false;
	_original = null;
}

/**
Set the area.
@param area area
*/
public void setAreaw(double area) {
	if (area != _areaw) {
		_areaw = area;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the well area.
@param area well area(AF)
*/
public void setAreaw(Double area) {
	if (area == null) {
		return;
	}
	setAreaw(area.doubleValue());
}

/**
Set the area(This is currently not being used but is provided for
consistency within this class).
@param area area
*/
public void setAreaw(String area) {
	if (area == null) {
		return;
	}
	setAreaw(StringUtil.atod(area.trim()));
}

/**
Set the average monthly CWR (12 monthly values + annual average), for the
data set calendar type.  This is ONLY used by StateDMI and does not need
to be considered in comparison code.
*/
public void setAverageMonthlyCWR ( double [] cwr_monthly )
{	__cwr_monthly = cwr_monthly;
}

/**
Set the average monthly historical historical pumping (12 monthly values +
annual average), for the data set calendar type.  This is ONLY used by StateDMI
and does not need to be considered in comparison code.
*/
public void setAverageMonthlyHistoricalPumping ( double [] weh_monthly )
{	__weh_monthly = weh_monthly;
}

/**
Set the average monthly efficiencies calculated from CWR and historical
pumping (12 monthly values + annual average), for the
data set calendar type.  This is ONLY used by StateDMI and does not need
to be considered in comparison code.
*/
public void setCalculatedEfficiencies ( double [] calculated_efficiencies )
{	__calculated_efficiencies = calculated_efficiencies;
}

/**
Set the standard deviation of monthly efficiencies calculated from CWR and
historical pumping (12 monthly values + annual average), for the
data set calendar type.  This is ONLY used by StateDMI and does not need
to be considered in comparison code.
*/
public void setCalculatedEfficiencyStddevs (
	double [] calculated_efficiency_stddevs )
{	__calculated_efficiency_stddevs = calculated_efficiency_stddevs;
}

/**
Set the well id to use for daily data
@param cdividyw well id to use for daily data
*/
public void setCdividyw(String cdividyw) {
	if (cdividyw == null) {
		return;
	}
	if (!cdividyw.equals(_cdividyw)) {
		_cdividyw = cdividyw.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the collection division.  This is needed to uniquely identify the parcels.
@param collection_div The division for the collection.
*/
public void setCollectionDiv ( int collection_div )
{	__collection_div = collection_div;
}

/**
Set the collection list for an aggregate/system for a specific year.
@param year The year to which the collection applies.
@param ids The identifiers indicating the locations in the collection.
*/
public void setCollectionPartIDs ( int year, List ids )
{	int pos = -1;	// Position of year in data lists.
	if ( __collection_Vector == null ) {
		// No previous data so create memory...
		__collection_Vector = new Vector ( 1 );
		__collection_Vector.add ( ids );
		__collection_year = new int[1];
		__collection_year[0] = year;
	}
	else {
		// See if the year matches any previous contents...
		for ( int i = 0; i < __collection_year.length; i++ ) {
			if ( year == __collection_year[i] ) {
				pos = i;
				break;
			}
		}
		// Now assign...
		if ( pos < 0 ) {
			// Need to add an item...
			pos = __collection_year.length;
			__collection_Vector.add ( ids );
			int [] temp = new int[__collection_year.length + 1];
			for ( int i = 0; i < __collection_year.length; i++ ) {
				temp[i] = __collection_year[i];
			}
			__collection_year = temp;
			__collection_year[pos] = year;
		}
		else {
			// Existing item...
			__collection_Vector.set ( pos, ids );
			__collection_year[pos] = year;
		}
	}
}

/**
Set the collection part type.
@param collection_part_type The collection part type (see COLLECTION_PART_TYPE_*).
*/
public void setCollectionPartType ( String collection_part_type )
{	__collection_part_type = collection_part_type;
}

/**
Set the collection type.
@param collection_type The collection type, either "Aggregate" or "System".
*/
public void setCollectionType ( String collection_type )
{	__collection_type = collection_type;
}

/**
Set the consumptive water requirement daily time series for the well structure.
*/
public void setConsumptiveWaterRequirementDayTS ( DayTS cwr_DayTS) {
	_cwr_DayTS = cwr_DayTS;
}

/**
Set the consumptive water requirement monthly time series for the well structure.
*/
public void setConsumptiveWaterRequirementMonthTS ( MonthTS cwr_MonthTS) {
	_cwr_MonthTS = cwr_MonthTS;
}

/**
Set the demand time series "pointer" to the daily demand time series.
@param demand_DayTS time series known to refer to this well.
*/
public void setDemandDayTS(DayTS demand_DayTS) {
	_demand_DayTS = demand_DayTS;
}

/**
Set the demand time series "pointer" to the monthly demand time series.
@param demand_MonthTS time series known to refer to this well.
*/
public void setDemandMonthTS ( MonthTS demand_MonthTS) {
	_demand_MonthTS = demand_MonthTS;
}

/**
Set the irrigated acreage source(see StateMod documentation for list of available sources).
@param demsrcw source for irrigated acreage
*/
public void setDemsrcw(int demsrcw) {
	if (demsrcw != _demsrcw) {
		_demsrcw = demsrcw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the irrigated acreage source(see StateMod documentation for list of available sources).
@param demsrcw source for irrigated acreage
*/
public void setDemsrcw(Integer demsrcw) {
	if (demsrcw == null) {
		return;
	}
	setDemsrcw(demsrcw.intValue());
}

/**
Set the irrigated acreage source(see StateMod documentation for list of available sources).
@param demsrcw source for irrigated acreage
*/
public void setDemsrcw(String demsrcw) {
	if (demsrcw == null) {
		return;
	}
	setDemsrcw(StringUtil.atoi(demsrcw.trim()));
}

// TODO - need to handle dirty flag
public void setDepletions(List depl) {
	_depl = depl;
}

/**
Set the well capacity
@param divcapw well capacity(cfs)
*/
public void setDivcapw(double divcapw) {
	if (divcapw != _divcapw) {
		_divcapw = divcapw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the well capacity
@param divcapw well capacity(cfs)
*/
public void setDivcapw(Double divcapw) {
	if (divcapw == null) {
		return;
	}
	setDivcapw(divcapw.doubleValue());
}

/**
Set the well capacity
@param divcapw well capacity(cfs)
*/
public void setDivcapw(String divcapw) {
	if (divcapw == null) {
		return;
	}
	setDivcapw(StringUtil.atod(divcapw.trim()));
}

/**
Set the system efficiency
@param divefcw efficiency of the system.  If negative, 12 efficiency values will be used.
*/
public void setDivefcw(double divefcw) {
	if (divefcw != _divefcw) {
		_divefcw = divefcw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the system efficiency
@param divefcw efficiency of the system.  If negative, 12 efficiency values will be used.
@see StateMod_Well#setDiveff
*/
public void setDivefcw(Double divefcw) {
	if (divefcw == null) {
		return;
	}
	setDivefcw(divefcw.doubleValue());
}

/**
Set the system efficiency
@param divefcw efficiency of the system.  If negative, 12 efficiency values will be used.
@see StateMod_Well#setDiveff
*/
public void setDivefcw(String divefcw) {
	if (divefcw == null) {
		return;
	}
	setDivefcw(StringUtil.atod(divefcw.trim()));
}

/**
Set the variable efficiency
@param index index of month for which to set efficiency(0-11)
@param diveff eff value(0-100)
*/
public void setDiveff(int index, Double diveff) {
	if (diveff == null) {
		return;
	}
	setDiveff(index, diveff.doubleValue());
}

/**
Set the system efficiency for a particular month.
The efficiencies are specified with month 0 being January.
@param index month index (0=January).
@param diveff monthly efficiency
@param yeartype The yeartype for the diversion stations file (consistent with
the control file for a full data set).  Recognized values are:
<ol>
<li>	null, "Calendar", "CYR" (Jan - Dec).</li>
<li>	"Irrigation", "IYR" (Oct - Sep).</li>
<li>	"Water", "WYR" (Nov - Oct).</li>
</ol>
*/
public void setDiveff(int index, double diveff, String yeartype )
{	// Adjust the index if necessary based on the year type...
	if ( yeartype == null ) {
		// Assume calendar.
	}
	else if ( yeartype.equalsIgnoreCase("Water") ||
		yeartype.equalsIgnoreCase("WYR") ) {
		index = TimeUtil.convertCalendarMonthToCustomMonth ( (index + 1), 10 ) - 1;
	}
	else if ( yeartype.equalsIgnoreCase("Irrigation") ||
		yeartype.equalsIgnoreCase("IYR") ) {
		index = TimeUtil.convertCalendarMonthToCustomMonth ( (index + 1), 11 ) - 1;
	}
	if (_diveff[index] != diveff) {
		_diveff[index] = diveff;
		setDirty(true);
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty( StateMod_DataSet.COMP_DIVERSION_STATIONS, true);
		}
	}
}

/**
Set the variable efficiency
@param index index of month for which to set efficiency(0-11)
@param diveff eff value(0-100)
*/
public void setDiveff(int index, double diveff) {
	if (index < 0 || index > 11) {
		Message.printWarning(2, "setDiveff", "Unable to set efficiency for month index " + index);
		return;
	}
	if (diveff != _diveff[index]) {
		_diveff[index] = diveff;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the variable efficiency
@param index index of month for which to set efficiency(0-11)
@param diveff eff value(0-100)
*/
public void setDiveff(int index, String diveff) { 
	if (diveff == null) {
		return;
	}
	setDiveff(index, StringUtil.atod(diveff.trim()));
}

/**
Set the geographic information object associated with the well.
@param georecord Geographic record associated with the well.
*/
public void setGeoRecord ( GeoRecord georecord )
{	_georecord = georecord;
}

/**
Set the historical daily pumping time series.
@param pumping_DayTS time series known to refer to this well.
*/
public void setPumpingDayTS(DayTS pumping_DayTS) {
	_pumping_DayTS = pumping_DayTS;
}

/**
Set the historical monthly pumping time series.
@param pumping_MonthTS time series known to refer to this well.
*/
public void setPumpingMonthTS(MonthTS pumping_MonthTS) {
	_pumping_MonthTS = pumping_MonthTS;
}

/**
Set the demand code
@param idvcomw demand code to use
*/
public void setIdvcomw(int idvcomw) {
	if (idvcomw != _idvcomw) {
		_idvcomw = idvcomw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the demand code
@param idvcomw demand code to use
*/
public void setIdvcomw(Integer idvcomw) { 
	if (idvcomw == null) {
		return;
	}
	setIdvcomw(idvcomw.intValue());
}

/**
Set the demand code
@param idvcomw demand code to use
*/
public void setIdvcomw(String idvcomw) {
	if (idvcomw == null) {
		return;
	}
	setIdvcomw(StringUtil.atoi(idvcomw.trim()));
}

/**
Set the diversion this well is tied to
@param idvcow2 diversion this well is tied to
*/
public void setIdvcow2(String idvcow2) {
	if (idvcow2 == null) {
		return;
	}
	if (!idvcow2.equals(_idvcow2)) {
		_idvcow2 = idvcow2;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the use type
@param irturnw use type
*/
public void setIrturnw(int irturnw) {
	if (irturnw != _irturnw) {
		_irturnw = irturnw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the use type
@param irturnw use type
*/
public void setIrturnw(Integer irturnw) {
	if (irturnw == null) {
		return;
	}
	setIrturnw(irturnw.intValue());
}

/**
Set the use type
@param irturnw use type
*/
public void setIrturnw(String irturnw) {
	if (irturnw == null) {
		return;
	}
	setIrturnw(StringUtil.atoi(irturnw.trim()));
}

/**
Set the average monthly efficiencies to be used for modeling
(12 monthly values + annual average), for the
data set calendar type.  This is ONLY used by StateDMI and does not need
to be considered in comparison code.
*/
public void setModelEfficiencies ( double [] model_efficiencies )
{	__model_efficiencies = model_efficiencies;
}

/**
Set the parcel Vector.
@param parcel_Vector the Vector of StateMod_Parcel to set for parcel data.
*/
void setParcels ( List parcel_Vector )
{	_parcel_Vector = parcel_Vector;
}

/**
Set the Priority switch
@param primary 0 = off; +n = on, adjust well rights by -n
*/
public void setPrimary(double primary) {
	if (primary != _primary) {
		_primary = primary;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_STATIONS, true);
		}
	}
}

/**
Set the Priority switch
@param primary  0 = off; +n = on, adjust well rights by -n
*/
public void setPrimary(Double primary) {
	if (primary == null) {
		return;
	}
	setPrimary(primary.doubleValue());
}

/**
Set the Priority switch
@param primary 0 = off; +n = on, adjust well rights by -n
*/
public void setPrimary(String primary) {
	if (primary == null) {
		return;
	}
	setPrimary(StringUtil.atod(primary.trim()));
}

// TODO - need to handle dirty flag
public void setReturnFlows(List rivret) {
	_rivret = rivret;
}

// TODO - need to handle dirty flag
public void setRights(List rights) {
	_rights = rights;
}

/**
Performs validation for this object within a StateMod data set.
@param dataset a StateMod_DataSet that is managing this object.
@return validation results, from which information about problems can be extracted.
*/
public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset )
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String riverID = getCgoto();
	double capacity = getDivcapw();
	String dailyID = getCdividyw();
	double primary = getPrimary();
	
	String idvcow2 = getIdvcow2();
	int idvcomw = getIdvcomw();
	int nrtnw = getNrtnw();
	int nrtnw2 = getNrtnw2();
	double divefcw = getDivefcw();
	double areaw = getAreaw();
	int irturnw = getIrturnw();
	int demsrcw = getDemsrcw();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well identifier is blank.",
			"Specify a station identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" name is blank.",
			"Specify a well name to clarify data.") );
	}
	// Get the network list if available for checks below
	DataSetComponent comp = null;
	List rinList = null;
	if ( dataset != null ) {
		comp = dataset.getComponentForComponentType(StateMod_DataSet.COMP_RIVER_NETWORK);
		rinList = (List)comp.getData();
		if ( (rinList != null) && (rinList.size() == 0) ) {
			// Set to null to simplify checks below
			rinList = null;
		}
	}
	if ( StateMod_Util.isMissing(riverID) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" river ID is blank.",
			"Specify a river ID to associate the well with a river network node.") );
	}
	else {
		// Verify that the river node is in the data set, if the network is available
		if ( rinList != null ) {
			if ( StateMod_Util.indexOf(rinList, riverID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
					"\" river network ID (" + riverID + ") is not found in the list of river network nodes.",
					"Specify a valid river network ID to associate the well with a river network node.") );
			}
		}
	}
	if ( !(capacity >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" capacity (" +
			StringUtil.formatString(capacity,"%.2f") + ") is invalid.",
			"Specify the capacity as a number >= 0.") );
	}
	// Verify that the daily ID is in the data set (daily ID is allowed to be missing)
	if ( (dataset != null) && !StateMod_Util.isMissing(dailyID) ) {
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_WELL_STATIONS);
		List wesList = (List)comp2.getData();
		if ( dailyID.equals("0") || dailyID.equals("3") || dailyID.equals("4") ) {
			// OK
		}
		else if ( (wesList != null) && (wesList.size() > 0) ) {
			// Check the diversion station list
			if ( StateMod_Util.indexOf(wesList, dailyID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" daily ID (" + dailyID +
				") is not 0, 3, or 4 and is not found in the list of well stations.",
				"Specify the daily ID as 0, 3, 4, or a matching well ID.") );
			}
		}
	}
	if ( !(primary >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
			"\" primary switch (" +
			StringUtil.formatString(primary,"%.2f") + ") is invalid.",
			"Specify the primary switch as >= 0." ) );
	}
	// Verify that the diversion ID is in the data set (diversion ID is allowed to be missing)
	if ( (dataset != null) && !StateMod_Util.isMissing(idvcow2) ) {
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_DIVERSION_STATIONS);
		List ddsList = (List)comp2.getData();
		if ( !dailyID.equals("NA") && !dailyID.equals("N/A") ) {
			// OK
		}
		else if ( (ddsList != null) && (ddsList.size() > 0) ) {
			// Check the diversion station list
			if ( StateMod_Util.indexOf(ddsList, idvcow2) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" diversion ID (" +
				idvcow2 + ") is not \"NA\" and is not found in the list of diversion stations.",
				"Specify the diversion ID as \"NA\" or a matching diversion ID.") );
			}
		}
	}
	List<String> choices = getIdvcomwChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + idvcomw) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" data type (" +
			idvcomw + ") is invalid.",
			"Specify the data type as one of " + choices) );
	}
	if ( !(nrtnw >= 0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
			"\" number of return flow locations (" + nrtnw + ") is invalid.",
			"Specify the number of return flow locations as >= 0.") );
	}
	if ( !((divefcw >= -100.0) && (divefcw <= 100.0)) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
			"\" annual system efficiency (" +
			StringUtil.formatString(divefcw,"%.2f") + ") is invalid.",
			"Specify the efficiency as 0 to 100 for annual (negative if monthly values are provided)." ) );
	}
	else if ( divefcw < 0.0 ) {
		// Check that each monthly efficiency is in the range 0 to 100
		double diveffw;
		for ( int i = 0; i < 12; i++ ) {
			diveffw = getDiveff(i);
			if ( !((diveffw >= 0.0) && (diveffw <= 100.0)) ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
					"\" system efficiency for month " + (i + 1) + " (" +
					StringUtil.formatString(diveffw,"%.2f") + ") is invalid.",
					"Specify the efficiency as 0 to 100." ) );
			}
		}
	}
	if ( !(areaw >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" area (" +
			StringUtil.formatString(areaw,"%.2f") + ") is invalid.",
			"Specify the area as a number >= 0.") );
	}
	choices = getIrturnwChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + irturnw) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" use type (" +
			irturnw + ") is invalid.",
			"Specify the use type as one of " + choices) );
	}
	choices = getDemsrcwChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + demsrcw) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" demand source (" +
			demsrcw + ") is invalid.",
			"Specify the demand source as one of " + choices) );
	}
	// Check return flow locations...
	StateMod_ReturnFlow ret;
	double pcttot;
	String crtnid;
	for ( int i = 0; i < nrtnw; i++ ) {
		ret = getReturnFlow ( i );
		pcttot = ret.getPcttot();
		crtnid = ret.getCrtnid();
		if ( rinList != null ) {
			// Make sure that the return location is in the network list
			if ( StateMod_Util.indexOf(rinList, crtnid) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
					"\" return " + (i + 1) + " location (" + crtnid +
					") is not found in the list of river network nodes.",
					"Specify a valid river network ID to associate the return location with a river network node.") );
			}
		}
		if ( !((pcttot >= 0.0) && (pcttot <= 100.0)) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" return " +
				(i + 1) + " percent (" +
				StringUtil.formatString(pcttot,"%.2f") + ") is invalid.",
				"Specify the return percent as a number 0 to 100.") );
		}
	}
	// Check depletion locations...
	StateMod_ReturnFlow depl;
	double pcttot2;
	String crtnid2;
	for ( int i = 0; i < nrtnw2; i++ ) {
		depl = getDepletion ( i );
		pcttot2 = depl.getPcttot();
		crtnid2 = depl.getCrtnid();
		if ( rinList != null ) {
			// Make sure that the return location is in the network list
			if ( StateMod_Util.indexOf(rinList, crtnid2) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
					"\" depletion " + (i + 1) + " location (" + crtnid2 +
					") is not found in the list of river network nodes.",
					"Specify a valid river network ID to associate the depletion location with a river network node.") );
			}
		}
		if ( !((pcttot2 >= 0.0) && (pcttot2 <= 100.0)) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id + "\" depletion " +
				(i + 1) + " percent (" +
				StringUtil.formatString(pcttot2,"%.2f") + ") is invalid.",
				"Specify the depletion percent as a number 0 to 100.") );
		}
	}
	// TODO SAM 2009-06-01) evaluate how to check rights (with getRights() or checking the rights data
	// set component).
	boolean checkRights = false;
	List wer_Vector = null;
	if ( dataset != null ) {
		DataSetComponent wer_comp = dataset.getComponentForComponentType ( StateMod_DataSet.COMP_WELL_RIGHTS );
		wer_Vector = (List)wer_comp.getData();
	}
	
	// Check to see if any parcels were associated with wells

	StateMod_Parcel parcel = null;	// Parcel associated with a well station
	String id_i = null;
	int wes_parcel_count = 0;	// Parcel count for well station
	double wes_parcel_area = 0.0;	// Area of parcels for well station
	int wes_well_parcel_count = 0;	// Parcel (with wells) count for well station.
	double wes_well_parcel_area = 0.0; // Area of parcels with wells for well station.
	List parcel_Vector;		// List of parcels for well station.

	id_i = getID();
	if ( getAreaw() <= 0.0 ) {
		if ( checkRights ) {
			List rights = StateMod_Util.getRightsForStation ( id_i, wer_Vector );
			if ( (rights != null) && (rights.size() != 0) ) {
				return null;
			}
		}
		// Check for parcels...
		wes_parcel_count = 0;
		wes_parcel_area = 0.0;
		wes_well_parcel_count = 0;
		wes_well_parcel_area = 0.0;
		parcel_Vector = getParcels();
		if ( parcel_Vector != null ) {
			wes_parcel_count = parcel_Vector.size();
			for ( int j = 0; j < wes_parcel_count; j++ ) {
				parcel = (StateMod_Parcel)parcel_Vector.get(j);
				if ( parcel.getArea() > 0.0 ) {
					wes_parcel_area += parcel.getArea();
				}
				if ( parcel.getWellCount() > 0 ) {
					wes_well_parcel_count += parcel.getWellCount();
					wes_well_parcel_area += parcel.getArea();
				}
			}
		}
		// new format for check file
		/*String [] data_table = {
			StringUtil.formatString(count,"%4d"),
			StringUtil.formatString(id_i,"%-12.12s"),
			getName(),
			StringUtil.formatString(getCollectionType(), "%-10.10s"),
			StringUtil.formatString(wes_parcel_count,"%9d"),
			StringUtil.formatString(wes_parcel_area,"%11.0f"),
			StringUtil.formatString(wes_well_parcel_count,"%9d"),
			StringUtil.formatString(wes_well_parcel_area,"%11.0f") };*/
		if ( (capacity <= 0.0) && (wes_parcel_count == 0) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Well \"" + id +
				"\" capacity is zero and no parcels are associated with well station.",
				"Verify well input data (this check only applies when data are processed from HydroBase).") );
		}
		
	}
	return validation;
}

/**
FIXME SAM 2009-06-03 Evaluate how to call from above to add more specific checks.
Check the well stations.
*/
private void validateComponent2 ( List werList, List wesList, String idpattern_Java,
	int warningCount, int warningLevel, String commandTag, CommandStatus status )
{	String routine = getClass().getName() + ".checkWellRights";
	String message;
	int matchCount = 0;
	/*
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
			parcel_Vector = wes_i.getParcels();
			if ( parcel_Vector != null ) {
				wes_parcel_count = parcel_Vector.size();
				for ( int j = 0; j < wes_parcel_count; j++ ) {
					parcel = (StateMod_Parcel)parcel_Vector.get(j);
					if ( parcel.getArea() > 0.0 ) {
						wes_parcel_area += parcel.getArea();
					}
					if ( parcel.getWellCount() > 0 ) {
						wes_well_parcel_count += parcel.getWellCount();
						wes_well_parcel_area += parcel.getArea();
					}
				}
			}
			// Format suitable for output in a list that can be copied to a spreadsheet or table.
			message_list.add (
				StringUtil.formatString(count,"%4d") +
				", " +
				StringUtil.formatString(id_i,"%-12.12s") +
				", " +
				StringUtil.formatString(
				wes_i.getCollectionType(),"%-10.10s") +
				", " +
				StringUtil.formatString(wes_parcel_count,"%9d")+
				", " +
				StringUtil.formatString(
				wes_parcel_area,"%11.0f")+
				", " +
				StringUtil.formatString(
				wes_well_parcel_count,"%9d")+
				", " +
				StringUtil.formatString(
				wes_well_parcel_area,"%11.0f")
				+ ", \"" + wes_i.getName() + "\"" );
		}
	}
	if ( message_list.size() > 0 ) {
		int line = 0;		// Line number for output (zero index).
		// Prepend introduction to the specific warnings...
		message_list.add ( line++, "" );
		message_list.add ( line++,
		"The following well stations (" + count + " out of " + size +
		") have no water rights (no irrigated parcels served by " +
		"wells)." );
		message_list.add ( line++,
		"Data may be OK if the station has no wells." );
		message_list.add ( line++, "" );
		message_list.add ( line++,
		"Parcel count and area in the following table are available " +
		"only if well rights are read from HydroBase." );
		message_list.add ( line++, "" );
		message_list.add ( line++,
		"    ,             ,           , # PARCELS, TOTAL      , # PARCELS, PARCELS    , WELL" );
		message_list.add (line++,
		"    , WELL        , COLLECTION, FOR WELL , PARCEL     , WITH     , WITH WELLS , STATION" );
		message_list.add (line++,
		"NUM., STATION ID  , TYPE      , STATION  , AREA (ACRE), WELLS    , AREA (ACRE), NAME" );
		message_list.add (line++,
		"----,-------------,-----------,----------,------------,----------,------------,-------------------------" );
	}

	// Check to make sure the sum of well rights equals the well station capacity...

	checkComponentData_WellRights_Capacity ( message_list );
	*/

	// Return values
	int [] retVals = new int[2];
	retVals[0] = matchCount;
	retVals[1] = warningCount;
	//return retVals;
}

/**
FIXME SAM 2009-06-03 Evaluate how to integrate into checks.
Helper method to check that well rights sum to the well station capacity.  This
is called by the well right and well station checks.  The check is performed
by formatting the capacity and decree sum to .NN precision.
@param message_list Vector of string to be printed to the check file, which will
be added to in this method.
*/
private int validateComponent_checkWellRights_SumToCapacity ( List wesList, List werList,
	int warningCount, int warningLevel, String commandTag, CommandStatus status  )
{	String routine = getClass().getName() + "checkWellRights_SumToCapacity";
	String message;
	StateMod_WellRight wer_i = null;
	StateMod_Well wes_i = null;
	int size = 0;
	if ( wesList != null ) {
		size = wesList.size();
	}
	double decree;
	double decree_sum;
	int onoff = 0;		// On/off switch for right
	int size_rights = 0;
	String id_i = null;
	List rights = null;
	for ( int i = 0; i < size; i++ ) {
		wes_i = (StateMod_Well)wesList.get(i);
		if ( wes_i == null ) {
			continue;
		}
		id_i = wes_i.getID();
		rights = StateMod_Util.getRightsForStation ( id_i, werList );
		size_rights = 0;
		if ( rights != null ) {
			size_rights = rights.size();
		}
		if ( size_rights == 0 ) {
			continue;
		}
		// Get the sum of the rights, assuming that all should be
		// compared against the capacity (i.e., sum of rights at the
		// end of the period will be compared with the current well capacity)...
		decree_sum = 0.0;
		for ( int iright = 0; iright < size_rights; iright++ ) {
			wer_i = (StateMod_WellRight)rights.get(iright);
			decree = wer_i.getDcrdivw();
			onoff = wer_i.getSwitch();
			if ( decree < 0.0 ) {
				// Ignore - missing values will cause a bad sum.
				continue;
			}
			if ( onoff <= 0 ) {
				// Subtract the decree...
				decree_sum -= decree;
			}
			else {
				// Add the decree...
				decree_sum += decree;
			}
		}
		// Compare to a whole number, which is the greatest precision for documented files.
		String decree_sum_formatted = StringUtil.formatString(decree_sum,"%.2f");
		String capacity_formatted = StringUtil.formatString(wes_i.getDivcapw(),"%.2f");
		if ( !decree_sum_formatted.equals(capacity_formatted) ) {
			message = "Well station \"" + id_i + " capacity (" + capacity_formatted +
			") does not sum to rights (" + decree_sum_formatted + ")";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			/*
			status.addToLog ( CommandPhaseType.RUN,
				new WellRightValidation(CommandStatusType.FAILURE,
					message, "Check that the StateDMI command parameters used to process " +
					"well stations and rights are consistent." ) );
					*/
		}
	}
	return warningCount;
}

/**
Print well information to output.  History header information 
is also maintained by calling this routine.
@param instrfile input file from which previous history should be taken
@param outstrfile output file to which to write
@param theWellStations list of wells to print
@param newComments addition comments which should be included in history
@see RTi.Util.IOUtil#processFileHeaders
*/
public static void writeStateModFile(String instrfile, String outstrfile,
		List theWellStations, List newComments)
throws Exception {
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	try {
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(instrfile),
			IOUtil.getPathUsingWorkingDir(outstrfile),
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		int i,j;
		String iline = null;
		String cmnt = "#>";
		String format_1 = "%-12.12s%-24.24s%-12.12s%8d%#8.2F %-12.12s %#12.5F";
		String format_2 = "                                    %-12.12s%8d%8d%8d%#8.0F%#8.0F%8d%8d";
		String format_4 = "                                    %-12.12s%8.2F%8d";
		StateMod_Well well = null;
		StateMod_ReturnFlow ret = null;
		List v = new Vector(8);
		List wellDepletion = null;
		List wellReturnFlow = null;
	
		out.println(cmnt);
		out.println(cmnt + " *******************************************************");
		out.println(cmnt + "  StateMod Well Station File");
		out.println(cmnt);
		out.println(cmnt + "  Row 1 format:  (a12, a24, a12,i8, f8.2, 1x, a12, 1x, f12.5)");
		out.println(cmnt);
		out.println(cmnt + "  ID        cdividw:  Well ID");
		out.println(cmnt + "  Name      divnamw:  Well name");
		out.println(cmnt + "  Riv ID    idvstaw:  River Node where well is located");
		out.println(cmnt + "  On/Off    idivsww:  Switch 0=off; 1=on");
		out.println(cmnt + "  Capacity  divcapw:  Well capacity (cfs)");
		out.println(cmnt + "  Daily ID cdividyw:  Well ID to use for daily data");
		out.println(cmnt + "  Primary   primary:  See StateMod documentation");
		out.println(cmnt);
		out.println(cmnt + "  Row 2 format:  (36x, a12, 3i8, f8.2, f8.0, i8, f8.0)");
		out.println(cmnt);
		out.println(cmnt + "  DivID     idvcow2:  Diversion this well is tied to (N/A if not tied to a diversion)");
		out.println(cmnt + "  DataType  idvcomw:  Data type (see StateMod doc)");
		out.println(cmnt + "  #-Ret       nrtnw:  Number of return flow locations");
		out.println(cmnt + "  #-Dep      nrtnw2:  Number of depletion locations");
		out.println(cmnt + "  Eff %     divefcw:  System efficiency (%) - if negative, include row 3");
		out.println(cmnt + "  Area        areaw:  Area served.");
		out.println(cmnt + "  UseType   irturnw:  Use type; 1-3=Inbasin; 4=Transmountain");
		out.println(cmnt + "  Demsrc    demsrcw:  Irrig acreage source (1=GIS, 2=tia, 3=GIS-primary, 4=tia-primary,");
		out.println(cmnt + "                       5=secondary, 6=M&I no acreage, 7=carrier no acreage, 8=user),");
		out.println(cmnt);
		out.println(cmnt + "  Row 3   Variable efficiency data, % (enter if divefcw < 0) - free format");
		out.println(cmnt);
		out.println(cmnt + "  Row 4  format:  (36x, a12, f8.0, i8)");
		out.println(cmnt);
		out.println(cmnt + "  Ret Id   crtnidw:  River ID receiving return flow");
		out.println(cmnt + "  Ret %    pcttotw:  Percent of return flow to location ");
		out.println(cmnt + "  Table #  irtndlw:  Return flow table id");
		out.println(cmnt);
		out.println(cmnt + "  Row 5  format:  (36x, a12, f8.0, i8)");
		out.println(cmnt + "  Dep Id   crtnidw2:  River ID being depletion");
		out.println(cmnt + "  Dep %    pcttotw2:  Percent of depletion to river node ");
		out.println(cmnt + "  Table #  irtndlw2:  Return (depletion) table id");
		out.println(cmnt);
		out.println(cmnt + "   ID         Name                  Riv ID     On/Off Capacity  Daily ID     Primary    ");
		out.println(cmnt + "---------eb----------------------eb----------eb------eb------exb----------exb----------e");
		out.println(cmnt + "                                       DivID  DataCode #-Ret   #-Dep   Eff %   Area   UseType  Demsrc");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb----------eb------eb------eb------eb------eb------eb------eb------e");
		out.println(cmnt + "  Eff %    Diveff Efficiency for month 1-12 where 1 is tied to year type");
		out.println(cmnt + "eff(1)  eff(2)  eff(3)  eff(4)  eff(5)  eff(6)  eff(7)  eff(8)  eff(9) eff(10) eff(11) eff(12)");
		out.println(cmnt + "-----eb------eb------eb------eb------eb------eb------eb------eb------eb------eb------eb------e");
		out.println(cmnt + "                                    Ret ID     Ret %   Table #");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb----------eb------eb------e");
		out.println(cmnt + "                                    Dep ID     Dep %   Table #");
		out.println(cmnt + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb----------eb------eb------e");
		out.println(cmnt);
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int num = 0;
		if (theWellStations != null) {
			num = theWellStations.size();
		}
	
		for (i = 0; i < num; i++) {
			well = (StateMod_Well)theWellStations.get(i);
			if (well == null) {
				continue;
			}
	
			// line 1
			v.clear();
			v.add(well.getID());
			v.add(well.getName());
			v.add(well.getCgoto());
			v.add(new Integer(well.getSwitch()));
			v.add(new Double (well.getDivcapw()));
			v.add(well.getCdividyw());
			v.add(new Double(well.getPrimary()));
			iline = StringUtil.formatString(v, format_1);
			out.println(iline);
	
			// line 2
			v.clear();
			v.add(well.getIdvcow2());
			v.add(new Integer(well.getIdvcomw()));
			v.add(new Integer(well.getNrtnw()));
			v.add(new Integer(well.getNrtnw2()));
			v.add(new Double (well.getDivefcw()));
			v.add(new Double (well.getAreaw()));
			v.add(new Integer(well.getIrturnw()));
			v.add(new Integer(well.getDemsrcw()));
			iline = StringUtil.formatString(v, format_2);
			out.println(iline);
	
			// line 3 - well efficiency
			if (well.getDivefcw()< 0) {
				for (j = 0; j < 12; j++) {
					v.clear();
					v.add(new Double(well.getDiveff(j)));
					iline = StringUtil.formatString(v, " %#5.0F");
					out.print(iline);
				}
	
				out.println();
			}
	
			// line 4 - return information
			int nrtn = well.getNrtnw();
			wellReturnFlow = well.getReturnFlows();
			for (j = 0; j < nrtn; j++) {
				v.clear();
				ret = (StateMod_ReturnFlow)wellReturnFlow.get(j);
				v.add(ret.getCrtnid());
				v.add(new Double(ret.getPcttot()));
				v.add(new Integer(ret.getIrtndl()));
				// SAM changed on 2000-02-24 as per Ray Bennett...
				//iline = StringUtil.formatString(v, format_4);
				iline = StringUtil.formatString(v, format_4) +" Rtn" + StringUtil.formatString((j+1),"%02d");
				out.println(iline);
			}
	
			// line 5 - depletion information
			nrtn = well.getNrtnw2();
			wellDepletion = well.getDepletions();
			for (j = 0; j < nrtn; j++) {
				v.clear();
				ret = (StateMod_ReturnFlow)wellDepletion.get(j);
				v.add(ret.getCrtnid());
				v.add(new Double(ret.getPcttot()));
				v.add(new Integer(ret.getIrtndl()));
				// SAM changed on 2000-02-24 as per Ray Bennett...
				//iline = StringUtil.formatString(v, format_4);
				iline = StringUtil.formatString(v, format_4) +" Dep" + StringUtil.formatString((j+1),"%02d");
				out.println(iline);
			}
		}
			
		out.flush();
		out.close();
		out = null;
	} 
	catch (Exception e) {
		out = null;
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
Writes a list of StateMod_Well objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
This method also writes well Return Flows to
filename[without extension]_ReturnFlows[extension], so if this method is called
with a filename parameter of "wells.txt", four files will be generated:
- wells.txt
- wells_Collections.txt
- wells_DelayTableAssignments.txt
- wells_ReturnFlows.txt
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments comments to add at the top of the file (e.g., command file, HydroBase version).
@return a list of files that were actually written, because this method controls all the secondary
filenames.
@throws Exception if an error occurs.
*/
public static List writeListFile(String filename, String delimiter, boolean update,
		List data, List newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("ID");
	fields.add("Name");
	fields.add("RiverNodeID");
	fields.add("OnOff");
	fields.add("Capacity");
	fields.add("DailyID");
	fields.add("Primary");
	fields.add("DiversionID");
	fields.add("DemandType");
	fields.add("EffAnnual");
	fields.add("IrrigatedAcres");
	fields.add("UseType");
	fields.add("DemandSource");
	fields.add("EffMonthly01");
	fields.add("EffMonthly02");
	fields.add("EffMonthly03");
	fields.add("EffMonthly04");
	fields.add("EffMonthly05");
	fields.add("EffMonthly06");
	fields.add("EffMonthly07");
	fields.add("EffMonthly08");
	fields.add("EffMonthly09");
	fields.add("EffMonthly10");
	fields.add("EffMonthly11");
	fields.add("EffMonthly12");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_WELL_STATIONS;
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
	int size2 = 0;
	PrintWriter out = null;
	StateMod_ReturnFlow rf = null;
	StateMod_Well well = null;
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	String id = null;
	StringBuffer buffer = new StringBuffer();
	List depletions = new Vector();
	List returnFlows = new Vector();
	List temp = null;
	
	String filenameFull = IOUtil.getPathUsingWorkingDir(filename);
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
		newComments2.add(1,"StateMod well stations as a delimited list file.");
		newComments2.add(2,"See also the associated return flow, depletion, and collection files.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filenameFull), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names[i] + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			well = (StateMod_Well)data.get(i);
			
			line[0] = StringUtil.formatString(well.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(well.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(well.getCgoto(), formats[2]).trim();
			line[3] = StringUtil.formatString(well.getSwitch(), formats[3]).trim();
			line[4] = StringUtil.formatString(well.getDivcapw(), formats[4]).trim();
			line[5] = StringUtil.formatString(well.getCdividyw(), formats[5]).trim();
			line[6] = StringUtil.formatString(well.getPrimary(), formats[6]).trim();
			line[7] = StringUtil.formatString(well.getIdvcow2(), formats[7]).trim();
			line[8] = StringUtil.formatString(well.getIdvcomw(), formats[8]).trim();
			line[9] = StringUtil.formatString(well.getDivefcw(), formats[9]).trim();
			line[10] = StringUtil.formatString(well.getAreaw(), formats[10]).trim();
			line[11] = StringUtil.formatString(well.getIrturnw(), formats[11]).trim();
			line[12] = StringUtil.formatString(well.getDemsrcw(), formats[12]).trim();
			line[13] = StringUtil.formatString(well.getDiveff(0), formats[13]).trim();
			line[14] = StringUtil.formatString(well.getDiveff(1), formats[14]).trim();
			line[15] = StringUtil.formatString(well.getDiveff(2), formats[15]).trim();
			line[16] = StringUtil.formatString(well.getDiveff(3), formats[16]).trim();
			line[17] = StringUtil.formatString(well.getDiveff(4), formats[17]).trim();
			line[18] = StringUtil.formatString(well.getDiveff(5), formats[18]).trim();
			line[19] = StringUtil.formatString(well.getDiveff(6), formats[19]).trim();
			line[20] = StringUtil.formatString(well.getDiveff(7), formats[20]).trim();
			line[21] = StringUtil.formatString(well.getDiveff(8), formats[21]).trim();
			line[22] = StringUtil.formatString(well.getDiveff(9), formats[22]).trim();
			line[23] = StringUtil.formatString(well.getDiveff(10), formats[23]).trim();
			line[24] = StringUtil.formatString(well.getDiveff(11), formats[24]).trim();

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

			temp = well.getReturnFlows();
			size2 = temp.size();
			id = well.getID();
			for (j = 0; j < size2; j++) {
				rf = (StateMod_ReturnFlow)temp.get(j);
				rf.setID(id);
				returnFlows.add(rf);
			}

			temp = well.getDepletions();
			size2 = temp.size();
			for (j = 0; j < size2; j++) {
				rf = (StateMod_ReturnFlow)temp.get(j);
				rf.setID(id);
				depletions.add(rf);
			}
		}
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
	}

	int lastIndex = filename.lastIndexOf(".");
	String front = filename.substring(0, lastIndex);
	String end = filename.substring((lastIndex + 1), filename.length());
	
	String returnFlowFilename = front + "_ReturnFlows." + end;
	StateMod_ReturnFlow.writeListFile(returnFlowFilename, delimiter,
		update, returnFlows, StateMod_DataSet.COMP_WELL_STATION_DELAY_TABLES, newComments );	

	String depletionFilename = front + "_Depletions." + end;
	StateMod_ReturnFlow.writeListFile(depletionFilename, delimiter,
		update, depletions, StateMod_DataSet.COMP_WELL_STATION_DEPLETION_TABLES, newComments );	

	String collectionFilename = front + "_Collections." + end;
	writeCollectionListFile(collectionFilename, delimiter, update, data, newComments );
	
	List filesWritten = new Vector();
	filesWritten.add ( new File(filenameFull) );
	filesWritten.add ( new File(returnFlowFilename) );
	filesWritten.add ( new File(depletionFilename) );
	filesWritten.add ( new File(collectionFilename) );
	return filesWritten;
}

/**
Writes the collection data from a Vector of StateMod_Well objects to a 
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
public static void writeCollectionListFile(String filename, String delimiter, boolean update,
	List data, List newComments )
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("LocationID");
	fields.add("Year");
	fields.add("CollectionType");
	fields.add("PartType");
	fields.add("PartID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_WELL_STATION_COLLECTIONS;
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
	
	int[] years = null;
	int k = 0;
	int num = 0;
	PrintWriter out = null;
	StateMod_Well well = null;
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	String colType = null;
	String id = null;
	String partType = null;	
	StringBuffer buffer = new StringBuffer();
	List ids = null;

	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector();
		}
		else {
			newComments2 = new Vector(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod well station collection information as delimited list file.");
		newComments2.add(2,"See also the associated well station file.");
		newComments2.add(3,"");
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
			well = (StateMod_Well)data.get(i);
			id = well.getID();
			years = well.getCollectionYears();
			if (years == null) {
				num = 0;
			}
			else {
				num = years.length;
			}
			colType = well.getCollectionType();
			partType = well.getCollectionPartType();
			
			// Loop through the number of years of collection data
			for (int iyear = 0; iyear < num; iyear++) {
				ids = well.getCollectionPartIDs(years[iyear]);
				// Loop through the identifiers for the specific year
				for ( k = 0; k < ids.size(); k++ ) {
					line[0] = StringUtil.formatString(id, formats[0]).trim();
					line[1] = StringUtil.formatString(years[iyear], formats[1]).trim();
					line[2] = StringUtil.formatString(colType, formats[2]).trim();
					line[3] = StringUtil.formatString(partType, formats[3]).trim();
					line[4] = StringUtil.formatString( ((String)(ids.get(k))), formats[4]).trim();
	
					buffer = new StringBuffer();	
					for (int ifield = 0; ifield < fieldCount; ifield++) {
						if (ifield > 0) {
							buffer.append(delimiter);
						}
						if (line[ifield].indexOf(delimiter) > -1) {
							// Wrap delimiter in quoted field
							line[ifield] = "\"" + line[ifield] + "\"";
						}
						buffer.append(line[ifield]);
					}
		
					out.println(buffer.toString());
				}
			}
		}
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