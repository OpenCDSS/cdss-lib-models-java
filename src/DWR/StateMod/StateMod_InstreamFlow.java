// StateMod_InstreamFlow - class instream flow station

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

//------------------------------------------------------------------------------
// StateMod_InstreamFlow - class derived from SMData.  Contains information 
//	read from the instream flow file.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 08 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 11 Feb 1998	CEN, RTi		Added _dataset.setDirty
//					to all set
//					routines.
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 25 Oct 1999	CEN, RTi		Added daily instream flow id.
// 03 Mar 2000	Steven A. Malers, RTi	Add iifcom(data type switch).  Javadoc
//					the constructor.  Add a finalize()
//					method.  Also Javadoc the I/O code.
// 15 Feb 2001	SAM, RTi		Add use_daily_data parameter to
//					writeInstreamFlowFile()method to 
//					allow comparison
//					with older files.  Clean up javadoc some
//					more.  Alphabetize methods.  Optimize
//					memory by setting unused variables to
//					null.  Handle null arguments better.
//					Update header information with current
//					variables.  Change IO to IOUtil.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-09	SAM, RTi		Add GeoRecord reference to allow 2-way
//					connection between spatial and StateMod
//					data.
// 2002-09-19	SAM, RTi		Use isDirty()instead of setDirty()to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMInsflow to 
//					StateMod_InstreamFlow
// 2003-06-10	JTS, RTI		* Folded dumpInstreamFlowFile() into
//					  writeInstreamFlowFile()
// 					* Renamed parseInstreamFlowFile() to
//					  readInstreamFlowFile()
// 2003-06-23	JTS, RTi		Renamed writeInstreamFlowFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readInstreamFlowFile() to
//					readStateModFile()
// 2003-07-15	JTS, RTi		Changed code to use new dataset design.
// 2003-08-03	SAM, RTi		Change isDirty() back to setDirty().
// 2003-08-15	SAM, RTi		Change GeoRecordNoSwing to GeoRecord.
// 2003-08-28	SAM, RTi		* Call setDirty() for each object and
//					  the component.
//					* Change the rights to a simple Vector
//					  (not a linked list) and remove the
//					  data member for the number of rights.
//					* Clean up Javadoc for parameters.
//					* Clean up handling of the time series.
//					* Clean up method names to not have
//					  "Insf" - this is redundant.
// 2003-10-10	SAM, RTi		Add disconnectRights().
// 2004-07-06	SAM, RTi		* Overload the constructor to allow data
//					  to be set to missing or be initialized
//					  to reasonable defaults.
//					* Add getIifcomChoices() and
//					  getDefaultIifcom().
//					* Fix output header - was not lined up
//					  correctly.
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
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2006-03-06	SAM, RTi		Fix bug where all rights were being
//					connected, not just the ones associated
//					with this instream flow station/reach.
// 2007-04-12	Kurt Tometich, RTi		Added checkComponentData() and
//									getDataHeader() methods for check
//									file and data check support.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.GIS.GeoView.GeoRecord;
import RTi.GIS.GeoView.HasGeoRecord;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_InstreamFlow extends StateMod_Data
implements Comparable<StateMod_Data>, HasGeoRecord, StateMod_ComponentValidator
{

/**
Daily instream flow id
*/
protected String _cifridy;

/**
Downstream river node, ins located
*/
protected String _ifrrdn;

/**
Instream flow rights
*/
protected List<StateMod_InstreamFlowRight> _rights;

/**
Annual demand time series
*/
protected MonthTS _demand_average_MonthTS;

/**
Monthly demand time series
*/
protected MonthTS _demand_MonthTS;

/**
Daily demand time series
*/
protected DayTS _demand_DayTS;

/**
Data type switch
*/
protected int _iifcom;

/**
Link to spatial data.
*/
protected GeoRecord _georecord = null;
	
/**
Construct a new instance of a StateMod instream flow station.
The initial data values are empty strings, no rights or time series, and ifcom=1.
*/
public StateMod_InstreamFlow()
{	this ( true );
}

/**
Construct a new instance of a StateMod instream flow station.
@param initialize_defaults If true, then data values are initialized to
reasonable defaults - this is suitable for adding a new instance in the
StateMod GUI.  If false, data values are initialized to missing - this is
suitable for a new instance in StateDMI.
*/
public StateMod_InstreamFlow ( boolean initialize_defaults )
{	super ();
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
Adds a right for the instream flow station.
*/
public void addRight ( StateMod_InstreamFlowRight right )
{	if (right == null) {
		return;
	}
	_rights.add ( right );
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
	StateMod_InstreamFlow i = (StateMod_InstreamFlow)super.clone();
	i._isClone = true;

	// The following are not cloned because there is no need to.  
	// The cloned values are only used for comparing between the 
	// values that can be changed in a single GUI.  The following
	// lists' data have their changes committed in other GUIs.	
	i._rights = _rights;
	return i;
}

/**
Compares this object to another StateMod_InstreamFlow object.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_InstreamFlow i = (StateMod_InstreamFlow)data;

	res = _cifridy.compareTo(i._cifridy);
	if (res != 0) {
		return res;
	}

	res = _ifrrdn.compareTo(i._ifrrdn);
	if (res != 0) {
		return res;
	}

	if (_iifcom < i._iifcom) {
		return -1;
	}
	else if (_iifcom > i._iifcom) {
		return 1;
	}

	return 0;
}

/**
Connect instream flow rights to stations.
@param isfs list of instream flow stations
@param rights list of instream flow rights
*/
public static void connectAllRights ( List<StateMod_InstreamFlow> isfs, List<StateMod_InstreamFlowRight> rights )
{	if ((isfs == null) || (rights == null)) {
		return;
	}
	int num_insf = isfs.size();

	StateMod_InstreamFlow insf;
	for (int i = 0; i < num_insf; i++) {
		insf = isfs.get(i);
		if (insf == null) {
			continue;
		}
		insf.connectRights(rights);
	}
	insf = null;
}

/**
Connect all instream flow time series to the instream flow objects.
*/
public static void connectAllTS ( List<StateMod_InstreamFlow> theIns, List<MonthTS> demandMonthTS,
	List<MonthTS> demandAverageMonthTS, List<DayTS> demandDayTS )
{	if ( theIns == null ) {
		return;
	}
	int numInsf = theIns.size();
	StateMod_InstreamFlow insflow;

	for (int i = 0; i < numInsf; i++) {
		insflow = theIns.get(i);
		if (insflow == null) {
			continue;
		}
		if ( demandMonthTS != null ) {
			insflow.connectDemandMonthTS(demandMonthTS);
		}
		if ( demandAverageMonthTS != null ) {
			insflow.connectDemandAverageMonthTS(demandAverageMonthTS);
		}
		if ( demandDayTS != null ) {
			insflow.connectDemandDayTS(demandDayTS);
		}
	}
}

/**
Connect daily demand time series to the instream flow.
The daily id "cifridy" must match the time series.
The time series description is set to the station name.
*/
public void connectDemandDayTS ( List<DayTS> tslist )
{	if ( tslist == null) {
		return;
	}
	_demand_DayTS = null;
	int numTS = tslist.size();
	DayTS ts;

	for (int i = 0; i < numTS; i++) {
		ts = tslist.get(i);
		if (ts == null) {
			continue;
		}
		if (_cifridy.equalsIgnoreCase(ts.getLocation())) {
			_demand_DayTS = ts; 
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect average monthly demand time series to the instream flow.
The time series description is set to the station name.
*/
public void connectDemandAverageMonthTS(List<MonthTS> tslist) {
	if (tslist == null) {
		return;
	}
	int numTS = tslist.size();
	MonthTS ts;

	_demand_average_MonthTS = null;
	for (int i = 0; i < numTS; i++) {
		ts = tslist.get(i);
		if (ts == null) {
			continue;
		}
		if (_id.equals(ts.getLocation())) {
			_demand_average_MonthTS = ts;
			ts.setDescription(getName());
			break;
		}
	}
}

/**
Connect monthly demand time series to the instream flow.
The time series description is set to the station name.
*/
public void connectDemandMonthTS(List<MonthTS> tslist) {
	if (tslist == null) {
		return;
	}
	int numTS = tslist.size();
	MonthTS ts;

	_demand_MonthTS = null;
	for (int i = 0; i < numTS; i++) {
		ts = tslist.get(i);
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
Connect the rights in the main rights file to this instream flow, using the instream flow ID.
*/
public void connectRights(List<StateMod_InstreamFlowRight> rights) {
	if (rights == null) {
		return;
	}
	int num_rights = rights.size();

	StateMod_InstreamFlowRight right;
	for (int i = 0; i < num_rights; i++) {
		right = rights.get(i);
		if (right == null) {
			continue;
		}
		if ( _id.equalsIgnoreCase(right.getCgoto()) ) {
			_rights.add ( right );
		}
	}
}

/**
Creates a copy of the object for later use in checking to see if it was changed in a GUI.
*/
public void createBackup() {
	_original = (StateMod_InstreamFlow)clone();
	((StateMod_InstreamFlow)_original)._isClone = false;
	_isClone = true;
}

// TODO - in the GUI need to decide if the right is actually removed from the main list
/**
Remove right from list.  A comparison on the ID is made.
@param right Right to remove.  Note that the right is only removed from the
list for this instream flow station and must also be removed from the main instream flow right list.
*/
public void disconnectRight ( StateMod_InstreamFlowRight right )
{	if (right == null) {
		return;
	}
	int size = _rights.size();
	StateMod_InstreamFlowRight right2;
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
Free memory for garbage collection.
*/
protected void finalize()
throws Throwable {
	_ifrrdn = null;
	_cifridy = null;
	_rights = null;
	_demand_MonthTS = null;
	_demand_average_MonthTS = null;
	_demand_DayTS = null;
	_georecord = null;
	super.finalize();
}

/**
Return Cifridy
*/
public String getCifridy() {
	return _cifridy;
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
Returns daily demand ts
*/
public DayTS getDemandDayTS() {
	return _demand_DayTS;
}

/**
Returns average monthly demand ts
*/
public MonthTS getDemandAverageMonthTS() {
	return _demand_average_MonthTS;
}

/**
Returns monthly demand ts
*/
public MonthTS getDemandMonthTS() {
	return _demand_MonthTS;
}

/**
Get the geographical data associated with the instream flow station.
@return the GeoRecord for the instream flow station.
*/
public GeoRecord getGeoRecord() {
	return _georecord;
}

/**
@return the value for iifcom for the StateMod instream flow station.
*/
public int getIifcom() {
	return _iifcom;
}

/**
Return a list of demand type option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Monthly demand", if include_notes is true.
@return a list of demand type option strings, for use in GUIs.
@param include_notes Indicate whether notes should be included.
*/
public static List<String> getIifcomChoices ( boolean include_notes )
{	List<String> v = new Vector<String>(5);
	v.add ( "1 - Monthly demand" );
	v.add ( "2 - Average monthly demand" );
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
Return the default demand type choice.  This can be used by GUI code
to pick a default for a new instream flow.
@return the default demand type choice.
*/
public static String getIifcomDefault ( boolean include_notes )
{	if ( include_notes ) {
		return "2 - Average monthly demand";
	}
	else {
		return "2";
	}
}

/**
Return the downstream river node where instream is located.
*/
public String getIfrrdn() {
	return _ifrrdn;
}

/**
Get the last right associated with the instream flow.
*/
public StateMod_InstreamFlowRight getLastRight()
{	if ( (_rights == null) || (_rights.size() == 0) ) {
		return null;
	}
	return (StateMod_InstreamFlowRight)_rights.get(_rights.size() -1);
}

/**
Return the number of rights.
*/
public int getNumrights() {
	return _rights.size();
}

/**
Return the right associated with the given index.  If index
number of rights don't exist, null will be returned.
@param index desired right index
*/
public StateMod_InstreamFlowRight getRight(int index)
{	if ( (index < 0) || (index >= _rights.size()) ) {
		return null;
	}
	else {
		return (StateMod_InstreamFlowRight)_rights.get(index);
	}
}

/**
Returns list of rights.
*/
public List<StateMod_InstreamFlowRight> getRights() {
	return _rights;
}

/**
Initialize data.
@param initializeDefaults If true, then data values are initialized to
reasonable defaults - this is suitable for adding a new instance in the
StateMod GUI.  If false, data values are initialized to missing - this is
suitable for a new instance in StateDMI.
*/
private void initialize ( boolean initializeDefaults )
{	_smdata_type = StateMod_DataSet.COMP_INSTREAM_STATIONS;
	_ifrrdn = "";
	if ( initializeDefaults ) {
		_cifridy = "0";	// Estimate average daily data from monthly
		_iifcom = 2;	// Default to annual
	}
	else {
		_cifridy = "";
		_iifcom = StateMod_Util.MISSING_INT;
	}
	_rights = new Vector<StateMod_InstreamFlowRight>();
	_demand_DayTS = null;
	_demand_MonthTS = null;
	_demand_average_MonthTS = null;
	_georecord = null;
}

/**
Return the downstream data set object by searching appropriate dataset lists.
@param dataset the full dataset from which the destination should be extracted
*/
public StateMod_Data lookupDownstreamDataObject ( StateMod_DataSet dataset )
{
	String downstreamID = getIfrrdn();
	StateMod_OperationalRight_Metadata_SourceOrDestinationType downstreamTypes[] =
		{ StateMod_OperationalRight_Metadata_SourceOrDestinationType.RIVER_NODE };
	List<StateMod_Data> smdataList = new Vector<StateMod_Data>();
	for ( int i = 0; i < downstreamTypes.length; i++ ) {
		 smdataList.addAll ( StateMod_Util.getDataList ( downstreamTypes[i], dataset,
			downstreamID, true ) );
		if ( smdataList.size() > 0 ) {
			break;
		}
	}
	if ( smdataList.size() == 1 ) {
		return smdataList.get(0);
	}
	else if ( smdataList.size() == 0 ) {
		return null;
	}
	else {
		throw new RuntimeException ( "" + smdataList.size() +
			" data objects returned matching downstream \"" + downstreamID +
			"\" for instream flow \"" + getID() + " - one is expected." );
	}
}

/**
Read instream flow information in and store in a list.  The new instream
flows are added to the end of the previously stored instream flows.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List<StateMod_InstreamFlow> readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_InstreamFlow.readStateModFile";
	String iline, s;
	List<StateMod_InstreamFlow> theIns = new Vector<StateMod_InstreamFlow>();
	List<Object> v = new Vector<Object>(9);
	int format_0[] = {
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING };
	int format_0w[] = {
		12,
		24,
		12,
		8,
		1,
		12,
		1,
		12,
		8 };

	if (Message.isDebugOn) {
		Message.printDebug(10, routine, "Reading file: " + filename);
	}
	BufferedReader in = null;
	try {
		in = new BufferedReader(new FileReader(filename));
		StateMod_InstreamFlow anIns;
		while ((iline = in.readLine()) != null) {
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}

			// allocate new instream flow node
			anIns = new StateMod_InstreamFlow();

			// line 1
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line 1: " + iline);
			}
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "Fixed read returned " + v.size()+ " elements");
			}
			s = StringUtil.unpad((String)v.get(0), " ", StringUtil.PAD_FRONT_BACK);
			anIns.setID(s);
			s = StringUtil.unpad((String)v.get(1), " ", StringUtil.PAD_FRONT_BACK);
			anIns.setName(s);
			s = StringUtil.unpad((String)v.get(2), " ", StringUtil.PAD_FRONT_BACK);
			anIns.setCgoto(s);
			anIns.setSwitch((Integer)v.get(3));
			s = StringUtil.unpad((String)v.get(5), " ", StringUtil.PAD_FRONT_BACK);
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "Ifrrdn: " + s);
			}
			anIns.setIfrrdn(s);
			// daily id
			s = StringUtil.unpad((String)v.get(7), " ", StringUtil.PAD_FRONT_BACK);
			anIns.setCifridy(s);
			// Data type(read as string and convert to integer)...
			s = StringUtil.unpad((String)v.get(8), " ", StringUtil.PAD_FRONT_BACK);
			anIns.setIifcom(s);

			// add the instream flow to the vector of instream flows
			theIns.add(anIns);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if ( in != null ) {
			in.close();
		}
	}
	return theIns;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_InstreamFlow i = (StateMod_InstreamFlow)_original;
	super.restoreOriginal();
	_cifridy = i._cifridy;
	_ifrrdn = i._ifrrdn;
	_iifcom = i._iifcom;
	_original = null;
	_isClone = false;
}

/**
Sets cifridy
*/
public void setCifridy(String cifridy) {
	if ( (cifridy != null) && !cifridy.equals(_cifridy)) {
		_cifridy = cifridy;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_STATIONS,true);
		}
	}
}

/**
Sets daily demand ts
*/
public void setDemandDayTS(DayTS demand_DayTS) {
	_demand_DayTS = demand_DayTS;
}

/**
Sets average monthly demand ts
*/
public void setDemandAverageMonthTS(MonthTS demand_average_MonthTS) {
	_demand_average_MonthTS = demand_average_MonthTS;
}

/**
Sets monthly demand ts
*/
public void setDemandMonthTS(MonthTS demand_MonthTS) {
	_demand_MonthTS = demand_MonthTS;
}

/**
Set the geographic information object associated with the instream flow station.
@param georecord Geographic record associated with the instream flow station.
*/
public void setGeoRecord ( GeoRecord georecord ) {
	_georecord = georecord;
}

/**
Set the value for iifcom for the StateMod instream flow station.
*/
public void setIifcom(int iifcom) {
	// Only set if value has changed...
	if (iifcom != _iifcom) {
		_iifcom = iifcom;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_STATIONS,true);
		}
	}
}

/**
Set the value for iifcom for the StateMod instream flow station.
*/
public void setIifcom(String iifcom) {
	setIifcom(StringUtil.atoi(iifcom));
}

/**
Set the downstream river node where instream is located.
*/
public void setIfrrdn(String ifrrdn) {
	if (!ifrrdn.equals(_ifrrdn)) {
		_ifrrdn = ifrrdn;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_INSTREAM_STATIONS,true);
		}
	}
}

// TODO - need to make sure dirty flag is handled.
/**
Set the rights list.
@param rights list of rights to set - this should not be null.
*/
public void setRights ( List<StateMod_InstreamFlowRight> rights )
{	_rights = rights;
}

/**
@param dataset StateMod dataset object.
@return List of data that failed specific checks.
 */
public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String riverID = getCgoto();
	String downstreamRiverID = getIfrrdn();
	String dailyID = getCifridy();
	int iifcom = getIifcom();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station identifier is blank.",
			"Specify a station identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id + "\" name is blank.",
			"Specify an instream flow station name to clarify data.") );
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
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id +
			"\" river node ID is blank.",
			"Specify a river node ID to associate the instream flow station with a river network node.") );
	}
	else {
		// Verify that the river node is in the data set, if the network is available
		if ( rinList != null ) {
			if ( StateMod_Util.indexOf(rinList, riverID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id +
					"\" river network ID (" + riverID + ") is not found in the list of river network nodes.",
					"Specify a valid river network ID to associate the instream flow station.") );
			}
		}
	}
	if ( StateMod_Util.isMissing(downstreamRiverID) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id +
			"\" downstream river node ID is blank.",
			"Specify a downstream river node ID to associate the instream flow station with a river network node.") );
	}
	else {
		// Verify that the river node is in the data set, if the network is available
		if ( rinList != null ) {
			if ( StateMod_Util.indexOf(rinList, downstreamRiverID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id +
					"\" downstream river network ID (" + riverID +
					") is not found in the list of river network nodes.",
					"Specify a valid river network ID to associate the instream flow station downstream node.") );
			}
		}
	}
	// Verify that the daily ID is in the data set (daily ID is allowed to be missing)
	if ( (dataset != null) && !StateMod_Util.isMissing(dailyID) ) {
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_INSTREAM_STATIONS);
		@SuppressWarnings("unchecked")
		List<StateMod_InstreamFlow> ifsList = (List<StateMod_InstreamFlow>)comp2.getData();
		if ( dailyID.equals("0") || dailyID.equals("3") || dailyID.equals("4") ) {
			// OK
		}
		else if ( (ifsList != null) && (ifsList.size() > 0) ) {
			// Check the instream flow station list
			if ( StateMod_Util.indexOf(ifsList, dailyID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id +
				"\" daily ID (" + dailyID +
				") is not 0, 3, or 4 and is not found in the list of instream flow stations.",
				"Specify the daily ID as 0, 3, 4, or a matching instream flow station ID.") );
			}
		}
	}
	List<String> choices = getIifcomChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + iifcom) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Instream flow station \"" + id +
			"\" data type (" +
			iifcom + ") is invalid.",
			"Specify the data type as one of " + choices) );
	}
	// TODO SAM 2009-06-01) evaluate how to check rights (with getRights() or checking the rights data
	// set component).
	return validation;
}

/**
Write instream flow information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theInsf list of instream flows to write
@param newcomments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
	List<StateMod_InstreamFlow> theInsf, List<String> newcomments)
throws Exception {
	writeStateModFile(infile, outfile, theInsf, newcomments, true);
}

/**
Write the instream flow objects to the StateMod file.
@param infile input file(original file read from, can be null).
@param outfile output file(to create or update, can be same as input).
@param theInsf list of StateMod_InstreamFlow instances.
@param newcomments Comments to add at the top of the file.
@param useDailyData Indicates whether daily and extended data(cifridy, iifcom)should be used.
@throws Exception if an error occurs
*/
public static void writeStateModFile(String infile, String outfile,
	List<StateMod_InstreamFlow> theInsf, List<String> newcomments, boolean useDailyData)
throws Exception {
	String routine = "StateMod_InstreamFlow.writeStateModFile";
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	Message.printStatus(2, routine, "Writing instream flows to file \""
		+ outfile + "\" using \"" + infile + "\" header...");

	// Process the header from the old file...

	try {	
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newcomments, commentIndicators, ignoredCommentIndicators, 0);
	
		int i;
		String iline;
		String cmnt = "#>";
		List<Object> v = new Vector<Object>(7);
		StateMod_InstreamFlow insf = null;
		String format_0 = "%-12.12s%-24.24s%-12.12s%8d %-12.12s %-12.12s%8d";
		String format_1 = "%-12.12s%-24.24s%-12.12s%8d %-12.12s";
	
		out.println(cmnt);
		out.println(cmnt + " ******************************************************* ");
		out.println(cmnt + "  StateMod Instream Flow Station File");
		out.println(cmnt);
		out.println(cmnt + "  Card format:  (a12,a24,a12,i8,1x,a12,1x,a12,i8)");
		out.println(cmnt);
		out.println(cmnt + "  ID           cifrid:  Instream Flow ID");
		out.println(cmnt + "  Name         cfrnam:  Instream Flow Name");
		out.println(cmnt + "  Riv ID        cgoto:  Upstream river ID where instream flow is located");
		out.println(cmnt + "  On/Off       ifrrsw:  Switch; 0=off, 1=on");
		out.println(cmnt + "  Downstream   ifrrdn:  Downstream river ID where instream flow is located");
		out.println(cmnt + "                        (blank indicates downstream=upstream)");
		out.println(cmnt + "  DailyID     cifridy:  Daily instream flow ID (see StateMod doc)");
		out.println(cmnt + "  DemandType   iifcom:  Demand type switch (see StateMod doc)");
		out.println(cmnt);
		out.println(cmnt + " ID        Name                    Riv ID     On/Off   Downstream    DailyID    DemandType");
		out.println(cmnt + "---------eb----------------------eb----------eb------e-b----------exb----------eb------e");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int num = 0;
		if (theInsf != null) {
			num = theInsf.size();
		}
		for (i = 0; i < num; i++) {
			insf = theInsf.get(i);
			if (insf == null) {
				continue;
			}
			v.clear();
			v.add(insf.getID());
			v.add(insf.getName());
			v.add(insf.getCgoto());
			v.add(new Integer(insf.getSwitch()));
			v.add(insf.getIfrrdn());
			if (useDailyData) {
				v.add(insf.getCifridy());
				v.add(new Integer(insf.getIifcom()));
				iline = StringUtil.formatString(v, format_0);
			}
			else {	
				iline = StringUtil.formatString(v, format_1);
			}
			out.println(iline);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if ( out != null ) {
			out.flush();
			out.close();
		}
	}
}

/**
Writes a list of StateMod_InstreamFlow objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write. 
@param newComments additional comments to write to the header.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update,
	List<StateMod_InstreamFlow> data, List<String> newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("ID");
	fields.add("Name");
	fields.add("UpstreamRiverNodeID");
	fields.add("OnOff");
	fields.add("DownstreamRiverNodeID");
	fields.add("DailyID");
	fields.add("DemandType");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_INSTREAM_STATIONS;
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
	StateMod_InstreamFlow flo = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
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
		newComments2.add(1,"StateMod instream flow stations as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders( oldFile,
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
			flo = (StateMod_InstreamFlow)data.get(i);
			
			line[0] = StringUtil.formatString(flo.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(flo.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(flo.getCgoto(), formats[2]).trim();
			line[3] = StringUtil.formatString(flo.getSwitch(), formats[3]).trim();
			line[4] = StringUtil.formatString(flo.getIfrrdn(), formats[4]).trim();
			line[5] = StringUtil.formatString(flo.getCifridy(), formats[5]).trim();				
			line[6] = StringUtil.formatString(flo.getIifcom(), formats[6]).trim();

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
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}	
	}
}

}
