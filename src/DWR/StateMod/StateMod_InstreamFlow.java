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

import java.lang.Integer;

import java.util.Vector;

import RTi.GIS.GeoView.GeoRecord;

import RTi.TS.DayTS;
import RTi.TS.MonthTS;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

public class StateMod_InstreamFlow extends StateMod_Data
implements StateMod_Component
{

/**
Daily instream flow id
*/
protected String 	_cifridy;

/**
Downstream river node, ins located
*/
protected String 	_ifrrdn;

/**
Instream flow rights
*/
protected Vector	_rights;

/**
Annual demand time series
*/
protected MonthTS	_demand_average_MonthTS;

/**
Monthly demand time series
*/
protected MonthTS	_demand_MonthTS;

/**
Daily demand time series
*/
protected DayTS		_demand_DayTS;

/**
Data type switch
*/
protected int		_iifcom;

/**
Link to spatial data -- currently not cloned.
*/
protected GeoRecord	_georecord;
	
/**
Construct a new instance of a StateMod instream flow station.
The initial data values are empty strings, no rights or time series, and
iifcom=1.
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
Adds a right for the diversion.
*/
public void addRight ( StateMod_InstreamFlowRight right )
{	if (right == null) {
		return;
	}
	_rights.addElement ( right );
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
@param count Number of components checked.
@param dataset StateMod dataset object.
@param props Extra properties for specific data checks.
@return List of data that failed specific checks.
 */
public String[] checkComponentData( int count, 
StateMod_DataSet dataset, PropList props ) 
{
	// TODO KAT 2007-04-16
	// add specific checks here
	return null;
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
	// Vectors' data have their changes committed in other GUIs.	
	i._rights = _rights;
	return i;
}

/**
Compares this object to another StateMod_InstreamFlow object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_InstreamFlow i = (StateMod_InstreamFlow)o;

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
@param isfs Vector of instream flow stations
@param rights Vector of instream flow rights
*/
public static void connectAllRights ( Vector isfs, Vector rights )
{	if ((isfs == null) || (rights == null)) {
		return;
	}
	int num_insf = isfs.size();

	StateMod_InstreamFlow insf;
	for (int i = 0; i < num_insf; i++) {
		insf = (StateMod_InstreamFlow)isfs.elementAt(i);
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
public static void connectAllTS (	Vector theIns,
					Vector demand_MonthTS,
					Vector demand_average_MonthTS,
					Vector demand_DayTS )
{	if ( theIns == null ) {
		return;
	}
	int numInsf = theIns.size();
	StateMod_InstreamFlow insflow;

	for (int i = 0; i < numInsf; i++) {
		insflow = (StateMod_InstreamFlow)theIns.elementAt(i);
		if (insflow == null) {
			continue;
		}
		if ( demand_MonthTS != null ) {
			insflow.connectDemandMonthTS(demand_MonthTS);
		}
		if ( demand_average_MonthTS != null ) {
			insflow.connectDemandAverageMonthTS(
			demand_average_MonthTS);
		}
		if ( demand_DayTS != null ) {
			insflow.connectDemandDayTS(demand_DayTS);
		}
	}
}

/**
Connect daily demand time series to the instream flow.
The daily id "cifridy" must match the time series.
The time series description is set to the station name.
*/
public void connectDemandDayTS ( Vector tslist )
{	if ( tslist == null) {
		return;
	}
	_demand_DayTS = null;
	int numTS = tslist.size();
	DayTS ts;

	for (int i = 0; i < numTS; i++) {
		ts = (DayTS)tslist.elementAt(i);
		if (ts == null) {
			continue;
		}
		if (_cifridy.equalsIgnoreCase(ts.getLocation())) {
			_demand_DayTS = ts; 
			ts.setDescription(getName());
			break;
		}
	}
	ts = null;
}

/**
Connect average monthly demand time series to the instream flow.
The time series description is set to the station name.
*/
public void connectDemandAverageMonthTS(Vector tslist) {
	if (tslist == null) {
		return;
	}
	int numTS = tslist.size();
	MonthTS ts;

	_demand_average_MonthTS = null;
	for (int i = 0; i < numTS; i++) {
		ts = (MonthTS)tslist.elementAt(i);
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
public void connectDemandMonthTS(Vector tslist) {
	if (tslist == null) {
		return;
	}
	int numTS = tslist.size();
	MonthTS ts;

	_demand_MonthTS = null;
	for (int i = 0; i < numTS; i++) {
		ts = (MonthTS)tslist.elementAt(i);
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
Connect the rights in the main rights file to this instream flow, using the
instream flow ID.
*/
public void connectRights(Vector rights) {
	if (rights == null) {
		return;
	}
	int num_rights = rights.size();

	StateMod_InstreamFlowRight right;
	for (int i = 0; i < num_rights; i++) {
		right = (StateMod_InstreamFlowRight)rights.elementAt(i);
		if (right == null) {
			continue;
		}
		if ( _id.equalsIgnoreCase(right.getCgoto()) ) {
			_rights.addElement ( right );
		}
	}
}

/**
Creates a copy of the object for later use in checking to see if it was 
changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_InstreamFlow)_original)._isClone = false;
	_isClone = true;
}

// REVISIT - in the GUI need to decide if the right is actually removed from
// the main list
/**
Remove right from list.  A comparison on the ID is made.
@param right Right to remove.  Note that the right is only removed from the
list for this diversion and must also be removed from the main diversion right
list.
*/
public void disconnectRight ( StateMod_InstreamFlowRight right )
{	if (right == null) {
		return;
	}
	int size = _rights.size();
	StateMod_InstreamFlowRight right2;
	// Assume that more than on instance can exist, even though this is
	// not allowed...
	for ( int i = 0; i < size; i++ ) {
		right2 = (StateMod_InstreamFlowRight)_rights.elementAt(i);
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
Free memory for gargage collection.
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
	// TODO KAT 2007-04-16 
	// When specific checks are added to checkComponentData
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
Get the geographical data associated with the diversion.
@return the GeoRecord for the diversion.
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
public static Vector getIifcomChoices ( boolean include_notes )
{	Vector v = new Vector(5);
	v.addElement ( "1 - Monthly demand" );
	v.addElement ( "2 - Average monthly demand" );
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
Return the default demand type choice.  This can be used by GUI code
to pick a default for a new instream flow.
@return the default demand type choice.
*/
public static String getIifcomDefault ( boolean include_notes )
{	if ( include_notes ) {
		return "2 - Average monthly demand";
	}
	else {	return "2";
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
	return (StateMod_InstreamFlowRight)_rights.elementAt(_rights.size() -1);
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
	else {	return (StateMod_InstreamFlowRight)_rights.elementAt(index);
	}
}

/**
Returns Vector of rights.
*/
public Vector getRights() {
	return _rights;
}

/**
Initialize data.
@param initialize_defaults If true, then data values are initialized to
reasonable defaults - this is suitable for adding a new instance in the
StateMod GUI.  If false, data values are initialized to missing - this is
suitable for a new instance in StateDMI.
*/
private void initialize ( boolean initialize_defaults )
{	_smdata_type = StateMod_DataSet.COMP_INSTREAM_STATIONS;
	_ifrrdn = "";
	if ( initialize_defaults ) {
		_cifridy = "0";	// Estimate average daily data from monthly
		_iifcom = 2;	// Default to annual
	}
	else {	_cifridy = "";
		_iifcom = StateMod_Util.MISSING_INT;
	}
	_rights = new Vector();
	_demand_DayTS = null;
	_demand_MonthTS = null;
	_demand_average_MonthTS = null;
	_georecord = null;
}

/**
Read instream flow information in and store in a Vector.  The new instream
flows are added to the end of the previously stored instream flows.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static Vector readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_InstreamFlow.readStateModFile";
	String iline, s;
	Vector theIns = new Vector();
	Vector v = new Vector(9);
	int format_0[] = {	StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_INTEGER,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING };
	int format_0w[] = {	12,
				24,
				12,
				8,
				1,
				12,
				1,
				12,
				8 };

	if (Message.isDebugOn) {
		Message.printDebug(10, routine, 
		"in SMParseInsfFile reading file: " + filename);
	}
	try {
		BufferedReader in = new BufferedReader(
			new FileReader(filename));
		StateMod_InstreamFlow anIns;
		while ((iline = in.readLine()) != null) {
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0)
				continue;

			// allocate new instream flow node
			anIns = new StateMod_InstreamFlow();

			// line 1
			if (Message.isDebugOn)
				Message.printDebug(50, routine, 
				"line 1: " + iline);
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			if (Message.isDebugOn)
				Message.printDebug(50, routine, 
				"Fixed read returned " 
				+ v.size()+ " elements");
			s = StringUtil.unpad((String)v.elementAt(0), 
				" ", StringUtil.PAD_FRONT_BACK);
			anIns.setID(s);
			s = StringUtil.unpad((String)v.elementAt(1), 
				" ", StringUtil.PAD_FRONT_BACK);
			anIns.setName(s);
			s = StringUtil.unpad((String)v.elementAt(2), 
				" ", StringUtil.PAD_FRONT_BACK);
			anIns.setCgoto(s);
			anIns.setSwitch((Integer)v.elementAt(3));
			s = StringUtil.unpad((String)v.elementAt(5), 
				" ", StringUtil.PAD_FRONT_BACK);
			if (Message.isDebugOn)
				Message.printDebug(50, routine, "Ifrrdn: " + s);
			anIns.setIfrrdn(s);
			// daily id
			s = StringUtil.unpad((String)v.elementAt(7), 
				" ", StringUtil.PAD_FRONT_BACK);
			anIns.setCifridy(s);
			// Data type(read as string and convert to integer)...
			s = StringUtil.unpad((String)v.elementAt(8), 
				" ", StringUtil.PAD_FRONT_BACK);
			anIns.setIifcom(s);

			// add the instream flow to the vector of instream flows
			theIns.addElement(anIns);
		}
	} 
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		routine = null;
		iline = null;
		s = null;
		v = null;
		format_0 = null;
		format_0w = null;
		throw e;
	}
	routine = null;
	iline = null;
	s = null;
	v = null;
	format_0 = null;
	format_0w = null;
	return theIns;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was caled and sets _original to null.
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
Set the geographic information object associated with the diversion.
@param georecord Geographic record associated with the diversion.
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

// REVISIT - need to make sure dirty flag is handled.
/**
Set the rights vector.
@param rights Vector of rights to set - this should not be null.
*/
public void setRights ( Vector rights )
{	_rights = rights;
}

/**
Write instream flow information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theInsf vector of instream flows to write
@param newcomments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
Vector theInsf, String[] newcomments)
throws Exception {
	writeStateModFile(infile, outfile, theInsf, newcomments, true);
}

/**
Write the instream flow objects to the StateMod file.
@param infile input file(original file read from, can be null).
@param outfile output file(to create or update, can be same as input).
@param theInsf Vector of StateMod_InstreamFlow instances.
@param newcomments Comments to add at the top of the file.
@param use_daily_data Indicates whether daily and extended data(cifridy,
iifcom)should be used.
@throws Exception if an error occurs
*/
public static void writeStateModFile(String infile, String outfile,
Vector theInsf, String[] newcomments, boolean use_daily_data)
throws Exception {
	String routine = "StateMod_InstreamFlow.writeStateModFile";
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out;
	Message.printStatus(2, routine,
		"Writing new instream flows to file \""
		+ outfile + "\" using \"" + infile + "\" header...");

	// Process the header from the old file...

	try {	
	out = IOUtil.processFileHeaders(
		IOUtil.getPathUsingWorkingDir(infile),
		IOUtil.getPathUsingWorkingDir(outfile), 
		newcomments, comment_str, ignore_comment_str, 0);

	int i;
	String iline;
	String cmnt = "#>";
	Vector v = new Vector(7);
	StateMod_InstreamFlow insf = null;
	String format_0 = "%-12.12s%-24.24s%-12.12s%8d %-12.12s %-12.12s%8d";
	String format_1 = "%-12.12s%-24.24s%-12.12s%8d %-12.12s";

	out.println(cmnt);
	out.println(cmnt 
		+ " ******************************************************* ");
	out.println(cmnt 
		+ "  Instream Flow Station File");
	out.println(cmnt);
	out.println(cmnt 
		+ "  Card format:  (a12,a24,a12,i8,1x,a12,1x,a12,i8)");
	out.println(cmnt);
	out.println(cmnt 
		+ "  ID           cifrid:  Instream Flow ID");
	out.println(cmnt 
		+ "  Name         cfrnam:  Instream Flow Name");
	out.println(cmnt 
		+ "  Riv ID        cgoto:  Upstream river ID where instream "
		+ "flow is located");
	out.println(cmnt 
		+ "  On/Off       ifrrsw:  Switch; 0=off, 1=on");
	out.println(cmnt 
		+ "  Downstream   ifrrdn:  Downstream river ID where instream "
		+ "flow is located");
	out.println(cmnt 
		+ "                        (blank indicates downstream=" +
		"upstream)");
	out.println(cmnt 
		+ "  DailyID     cifridy:  Daily instream flow ID (" +
		"see StateMod doc)");
	out.println(cmnt 
		+ "  DemandType   iifcom:  Demand type switch (" +
		"see StateMod doc)");
	out.println(cmnt);
	out.println(cmnt 
		+ " ID        Name                    Riv ID     On/Off  "
		+ " Downstream    DailyID    DemandType");
	out.println(cmnt 
		+ "---------eb----------------------eb----------eb------e"
		+ "-b----------exb----------eb------e");
	out.println(cmnt + "EndHeader");
	out.println(cmnt);

	int num = 0;
	if (theInsf != null) {
		num = theInsf.size();
	}
	for (i = 0; i < num; i++) {
		insf = (StateMod_InstreamFlow)theInsf.elementAt(i);
		if (insf == null) {
			continue;
		}
		v.removeAllElements();
		v.addElement(insf.getID());
		v.addElement(insf.getName());
		v.addElement(insf.getCgoto());
		v.addElement(new Integer(insf.getSwitch()));
		v.addElement(insf.getIfrrdn());
		if (use_daily_data) {
			v.addElement(insf.getCifridy());
			v.addElement(new Integer(insf.getIifcom()));
			iline = StringUtil.formatString(v, format_0);
		}
		else {	
			iline = StringUtil.formatString(v, format_1);
		}
		out.println(iline);
	}
		
	out.flush();
	out.close();
	} 
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		routine = null;
		comment_str = null;
		ignore_comment_str = null;
		out = null;
		throw e;
	} 
	routine = null;
	comment_str = null;
	ignore_comment_str = null;
	out = null;
}

/**
Writes a Vector of StateMod_InstreamFlow objects to a list file.  A header is 
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
boolean update, Vector data) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	Vector fields = new Vector();
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
	StateMod_InstreamFlow flo = null;
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
			flo = (StateMod_InstreamFlow)data.elementAt(i);
			
			line[0] = StringUtil.formatString(flo.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(flo.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(flo.getCgoto(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(flo.getSwitch(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(flo.getIfrrdn(), 
				formats[4]).trim();
			line[5] = StringUtil.formatString(flo.getCifridy(), 
				formats[5]).trim();				
			line[6] = StringUtil.formatString(flo.getIifcom(), 
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

} // End StateMod_InstreamFlow
