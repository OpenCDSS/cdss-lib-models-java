//------------------------------------------------------------------------------
// StateMod_StreamGage - class derived from StateMod_Data.  Contains
//	information the stream gage station file (.ris).
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 23 Feb 1998	Catherine E.		Added write routines.
//		Nutting-Lane, RTi
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 06 Feb 2001	Steven A. Malers, RTi	Update to handle new daily data.  Also,
//					Ray added a gwmaxr data item to the
//					.rin file.  Consequently, this
//					StateMod_RiverInfo class can not be 
//					shared as
//					transparently between .rin and .ris
//					files.  Probably need to make this a
//					base class and derive SMStation (or
//					similar) from it, but for now just put
//					specific .rin and .ris data here and use
//					a flag to indicate which is used.  Need
//					some help from Catherine to clean up at
//					some point.  Update javadoc as I go
//					through and figure things out.  Add
//					finalize method and set unused data to
//					null to help garbage collection.
//					Alphabetize methods.  Optimize loops so
//					size() is not called each iteration.
//					Check for null arguments.  Change some
//					low-level status messages to debug
//					messages to improve performance.
//					Optimize lookups by using _id rather
//					than calling getID().  There are still
//					places (like cases where strings are
//					manipulated without checking for null)
//					where error handling is not complete but
//					leave for now since it seems to be
//					working.  Use trim() instead of
//					StringUtil to simplify code.  Add line
//					cound to read routine to print in
//					error message.  Remove all "additional
//					string" code in favor of specific data
//					since Ray is beginning to add to files
//					in inconsistent ways.  Change IO to
//					IOUtil.  Add constructor to parse a
//					string and handle the setrin() syntax
//					used by makenet.  This allows the
//					StateMod_RiverInfo object to store set
//					information with not much more work.
//					Add applySetRinCommands() to apply
//					edits.
// 2001-12-27	SAM, RTi		Update to use new fixedRead() to
//					improve performance.
// 2002-09-12	SAM, RTi		Add the baseflow time series (.xbm or
//					.rim) to this class for the (.ris) file
//					display.  Remove the overloaded
//					connectAllTS() that only handled monthly
//					time series.  One version of the method
//					should be ok since the StateMod GUI is
//					the only thing that uses it.
//					Also add the daily baseflow time series
//					corresponding to the .rid file.
// 2002-09-19	SAM, RTi		Use isDirty() instead of setDirty() to
//					indicate edits.
// 2002-10-07	SAM, RTi		Add GeoRecord reference to allow 2-way
//					connection between spatial and StateMod
//					data.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMRiverInfo
// 2003-06-10	JTS, RTi		* Folded dumpRiverInfoFile() into
//					  writeRiverInfoFile()
//					* Renamed parseRiverInfoFile() to
//					  readRiverInfoFile()
// 2003-06-23	JTS, RTi		Renamed writeRiverInfoFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readRiverInfoFile() to
//					readStateModFile()
// 2003-07-30	SAM, RTi		* Split river station code out of
//					  StateMod_RiverInfo into this
//					  StateMod_RiverStation class to make
//					  management of data cleaner.
//					* Change isDirty() back to setDirty().
// 2003-08-28	SAM, RTi		* Clean up time series data members and
//					  methods.
//					* Clean up parameter names.
//					* Call setDirty() on each object in
//					  addition to the data set component.
// 2003-09-11	SAM, RTi		Rename from StateMod_RiverStation to
//					StateMod_StreamGage and make
//					appropriate changes throughout.
// 2004-03-15	JTS, RTi		Added in some old member variables for
//					use with writing makenet files:
//					* _comment
//					* _node
//					* setNode()
//					* applySetRinCommands()
//					* applySetRisCommands()
//					* _gwmaxr_string
// 2004-07-06	SAM, RTi		* Overload the constructor to allow
//					  initialization to reasonable defaults
//					  or missing.
//					* Remove the above code from Tom since
//					  these features are either from Makenet
//					  (and now in StateDMI) and meant for
//					  the StateMod_RiverNetworkNode class.
// 2004-07-10	SAM, RTi		Add the _related_smdata_type and
//					_related_smdata_type2 data members.
//					This allows the node types to
//					be set when the list of stream estimate
//					stations is read from the network file.
//					This allows the node type to be properly
//					set for the last 3 characters in the
//					name, as has traditionally been done.
//					This change is made for stream gage and
//					stream estimate stations because in
//					order to support old data sets, the
//					stream estimate stations are combined
//					with stream gage stations.
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
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_StreamGage 
extends StateMod_Data
implements Cloneable, Comparable, StateMod_ComponentValidator
{

//protected String 	_cgoto;		// River node for stream station.

/**
Monthly historical TS from the .rih file that is associated with the
.ris station - only streamflow gages in the .ris have these data
*/
protected MonthTS _historical_MonthTS;

/**
Monthly base flow time series, for use with the river station (.ris)
file, read from the .xbm/.rim file.
*/
protected MonthTS _baseflow_MonthTS;

/**
Daily base flow time series, read from the .rid file.
*/
protected DayTS _baseflow_DayTS;

/**
Daily historical TS from the .riy file that is associated with the .ris
station - only streamflow gages in the .riy have these data
*/
protected DayTS _historical_DayTS;

/**
Used with .ris (column 4) - daily stream station identifier.
*/
protected String _crunidy;

/**
Reference to spatial data for this river station.  Currently not cloned.
*/
protected GeoRecord	_georecord;

/**
The StateMod_DataSet component type for the node.  At some point the related object reference
may also be added, but there are cases when this is not known (only the type is
known, for example in StateDMI).
*/
protected int _related_smdata_type;

/**
Second related type.  This is only used for D&W node types and should be set to the
well stations component type.
*/
protected int _related_smdata_type2;
	
/**
Constructor for stream gage station.
The time series are set to null and other information to empty strings or other reasonable defaults.
*/
public StateMod_StreamGage ()
{	this ( true );
}

/**
Constructor for stream gage station.
@param initialize_defaults If true, the time series are set to null and other
information to empty strings or other reasonable defaults - this is suitable
for the StateMod GUI when creating new instances.  If false, the
data values are set to missing - this is suitable for use with StateDMI, where
data will be filled with commands.
*/
public StateMod_StreamGage ( boolean initialize_defaults )
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
	StateMod_StreamGage s = (StateMod_StreamGage)super.clone();
	s._isClone = true;
	return s;
}

/**
Compares this object to another StateMod_StreamGage object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_StreamGage s = (StateMod_StreamGage)o;

	res = _cgoto.compareTo(s._cgoto);
	if (res != 0) {
		return res;
	}

	res = _crunidy.compareTo(s._crunidy);
	if (res != 0) {
		return res;
	}

	if (_related_smdata_type < s._related_smdata_type) {
		return -1;
	}
	else if (_related_smdata_type > s._related_smdata_type) {
		return 1;
	}

	if (_related_smdata_type2 < s._related_smdata_type2) {
		return -1;
	}
	else if (_related_smdata_type2 > s._related_smdata_type2) {
		return 1;
	}

	return 0;
}

/**
Connect the historical monthly and daily TS pointers to the appropriate TS
for all the elements in the list of StateMod_StreamGage objects.
@param rivs list of StateMod_StreamGage (e.g., as read from StateMod .ris file).
*/
public static void connectAllTS ( List rivs, List historical_MonthTS, List historical_DayTS,
		List baseflow_MonthTS, List baseflow_DayTS )
{	if ( rivs == null ) {
		return;
	}
	StateMod_StreamGage riv;
	int size = rivs.size();
	for ( int i=0; i < size; i++ ) {
		riv = (StateMod_StreamGage)rivs.get(i);
		if ( historical_MonthTS != null ) {
			riv.connectHistoricalMonthTS ( historical_MonthTS );
		}
		if ( historical_DayTS != null ) {
			riv.connectHistoricalDayTS ( historical_DayTS );
		}
		if ( baseflow_MonthTS != null ) {
			riv.connectBaseflowMonthTS ( baseflow_MonthTS );
		}
		if ( baseflow_DayTS != null ) {
			riv.connectBaseflowDayTS ( baseflow_DayTS );
		}
	}
}

/**
Connect the daily base streamflow TS pointer to the appropriate TS in the list.
A connection is made if the node identifier matches the time series location.
@param tslist list of DayTS.
*/
private void connectBaseflowDayTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	DayTS ts;
	int size = tslist.size();
	_baseflow_DayTS = null;
	for ( int i=0; i < size; i++ ) {
		ts = (DayTS)tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		if ( _id.equalsIgnoreCase ( ts.getLocation())) {
			// Set this because the original file does not have...
			ts.setDescription ( getName() );
			_baseflow_DayTS = ts;
			break;
		}
	}
}

/**
Connect monthly baseflow time series.  
@param tslist baseflow time series. 
*/
public void connectBaseflowMonthTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	int num_TS = tslist.size();

	_baseflow_MonthTS = null;
	MonthTS ts = null;
	for (int i=0; i<num_TS; i++) {
		ts = (MonthTS)tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		if ( _id.equalsIgnoreCase(ts.getLocation())) {
			// Set this because the original file does not have...
			ts.setDescription ( getName() );
			_baseflow_MonthTS = ts;
			break;
		}
	}
}

/**
Connect the historical daily TS pointer to the appropriate TS in the Vector.
A connection is made if the node identifier matches the time series location.
@param tslist Vector of DayTS.
*/
private void connectHistoricalDayTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	DayTS ts;
	_historical_DayTS = null;
	int size = tslist.size();
	for ( int i=0; i < size; i++ ) {
		ts = (DayTS)tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		if ( _id.equalsIgnoreCase( ts.getLocation())) {
			// Set this because the original file does not have...
			ts.setDescription ( getName() );
			_historical_DayTS = ts;
			break;
		}
	}
}

/**
Connect the historical monthly TS pointer to the appropriate TS.
A connection is made if the node identifier matches the time series location.
@param tslist Vector of MonthTS.
*/
public void connectHistoricalMonthTS ( List tslist )
{	if ( tslist == null ) {
		return;
	}
	MonthTS ts;
	_historical_MonthTS = null;
	int size = tslist.size();
	for ( int i=0; i < size; i++ ) {
		ts = (MonthTS)tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		if ( _id.equalsIgnoreCase(ts.getLocation())) {
			// The name is usually not set when reading the time series...
			ts.setDescription ( getName() );
			_historical_MonthTS = ts;
			break;
		}
	}
}

/**
Creates a copy of the object for later use in checking to see if it was changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_StreamGage)_original)._isClone = false;
	_isClone = true;
}

/**
Finalize data for garbage collection.
*/
protected void finalize ()
throws Throwable
{	_cgoto = null;
	_historical_MonthTS = null;
	_historical_DayTS = null;
	_baseflow_MonthTS = null;
	_baseflow_DayTS = null;
	_crunidy = null;
	_georecord = null;
	super.finalize();
}

/**
Return the daily baseflow file associated with the *.ris node.
@return the daily baseflow file associated with the *.ris node.
Return null if no time series is available.
*/
public DayTS getBaseflowDayTS ( )
{	return _baseflow_DayTS;
}

/**
Return the monthly baseflow file associated with the *.ris node.
@return the monthly baseflow file associated with the *.ris node.
Return null if no time series is available.
*/
public MonthTS getBaseflowMonthTS ( )
{	return _baseflow_MonthTS;
}

/**
Get the river node identifier for the stream gage.
@return the river node identifier.
*/
public String getCgoto() {
	return _cgoto;
}

/**
Get the daily stream station identifier used with the stream gage station file.
@return the daily stream station identifier.
*/
public String getCrunidy ( ) {
	return _crunidy;
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
Get the geographical data associated with the diversion.
@return the GeoRecord for the diversion.
*/
public GeoRecord getGeoRecord()
{	return _georecord;
}

/**
Get the daily TS pointer (typically only if storing .ris).
@return the daily TS pointer.
*/
public DayTS getHistoricalDayTS ( ) {
	return _historical_DayTS;
}

/**
Get the historical monthly TS pointer (typically only if storing .ris).
@return the historical monthly TS pointer.
*/
public MonthTS getHistoricalMonthTS ( ) {
	return _historical_MonthTS;
}

/**
Get the StateMod_DataSet component type for the data for this node, or
StateMod_DataSet.COMP_UNKNOWN if unknown.
Get the StateMod_DataSet component type for the data for this node.
*/
public int getRelatedSMDataType()
{	return _related_smdata_type;
}

/**
Get the StateMod_DataSet component type for the data for this node, or
StateMod_DataSet.COMP_UNKNOWN if unknown.
This is only used for D&W nodes and should be set to the well component type.
Get the StateMod_DataSet component type for the data for this node.
*/
public int getRelatedSMDataType2()
{	return _related_smdata_type2;
}

/**
Initialize data.
@param initialize_defaults If true, the time series are set to null and other
information to empty strings or other reasonable defaults - this is suitable
for the StateMod GUI when creating new instances.  If false, the
data values are set to missing - this is suitable for use with StateDMI, where
data will be filled with commands.
*/
private void initialize ( boolean initialize_defaults )
{	_smdata_type = StateMod_DataSet.COMP_STREAMGAGE_STATIONS;
	_cgoto = "";
	_historical_MonthTS = null;
	_historical_DayTS = null;
	_baseflow_MonthTS = null;
	_baseflow_DayTS = null;
	if ( initialize_defaults ) {
		// Set to reasonable defaults...
		_crunidy = "0"; // Use monthly data
	}
	else {
		// Initialize to missing
		_crunidy = "";
	}
	_georecord = null;
}

/**
Read the stream gage station file and store return a Vector of StateMod_StreamGage.
@return a Vector of StateMod_StreamGage.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List readStateModFile ( String filename )
throws Exception
{	String rtn = "StateMod_StreamGage.readStateModFile";
	List theRivs = new Vector();
	String iline;
	List v = new Vector ( 5 );
	int [] format_0;
	int [] format_0w;
	format_0 = new int[5];
	format_0[0] = StringUtil.TYPE_STRING;
	format_0[1] = StringUtil.TYPE_STRING;
	format_0[2] = StringUtil.TYPE_STRING;
	format_0[3] = StringUtil.TYPE_STRING;
	format_0[4] = StringUtil.TYPE_STRING;
	format_0w = new int [5];
	format_0w[0] = 12;
	format_0w[1] = 24;
	format_0w[2] = 12;
	format_0w[3] = 1;
	format_0w[4] = 12;
	int linecount = 0;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, rtn, "in " + rtn + " reading file: " + filename );
	}
	BufferedReader in = null;
	try {
		in = new BufferedReader ( new FileReader ( filename ));
		while ( (iline = in.readLine()) != null ) {
			++linecount;
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}

			// allocate new StateMod_StreamGage node
			StateMod_StreamGage aRiverNode = new StateMod_StreamGage();

			// line 1
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, "line 1: " + iline );
			}
			StringUtil.fixedRead ( iline, format_0, format_0w, v );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, "Fixed read returned " + v.size() + " elements");
			}
			aRiverNode.setID ( ((String)v.get(0)).trim() );
			aRiverNode.setName ( ((String)v.get(1)).trim() );
			aRiverNode.setCgoto ( ((String)v.get(2)).trim() );
			// Space
			aRiverNode.setCrunidy (((String)v.get(4)).trim());

			// add the node to the vector of river nodes
			theRivs.add ( aRiverNode );
		}
	}
	catch (Exception e) {
		// Clean up...
		Message.printWarning ( 3, rtn, "Error reading \"" + filename + "\" at line " + linecount );
		throw e;
	}
	finally {
		// Clean up...
		if ( in != null ) {
			in.close();
		}
	}
	return theRivs;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_StreamGage s = (StateMod_StreamGage)_original;
	super.restoreOriginal();
	_related_smdata_type = s._related_smdata_type;
	_related_smdata_type2 = s._related_smdata_type2;
	_crunidy = s._crunidy;
	_isClone = false;
	_original = null;
}

/**
Set the daily baseflow TS.
@param ts daily baseflow TS.
*/
public void setBaseflowDayTS ( DayTS ts )
{	_baseflow_DayTS = ts;
}

/**
Set the monthly baseflow TS.
@param ts monthly baseflow TS.
*/
public void setBaseflowMonthTS ( MonthTS ts )
{	_baseflow_MonthTS = ts;
}

/**
Set the river node identifier.
@param cgoto River node identifier.
*/
public void setCgoto ( String cgoto ) {
	if ( (cgoto != null) && !cgoto.equals(_cgoto )) {
		_cgoto = cgoto;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty ( _smdata_type, true );
		}
	}
}

/**
Set the daily stream station for the node.
@param crunidy Daily station identifier for node.
*/
public void setCrunidy ( String crunidy )
{	if ( (crunidy != null) && !_crunidy.equals(crunidy) ) {
		_crunidy = crunidy;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty ( _smdata_type, true );
		}
	}
}

/**
Set the geographic information object associated with the diversion.
@param georecord Geographic record associated with the diversion.
*/
public void setGeoRecord ( GeoRecord georecord )
{	_georecord = georecord;
}

/**
Set the daily historical TS pointer.
@param ts Daily historical TS.
*/
public void setHistoricalDayTS ( DayTS ts ) {
	_historical_DayTS = ts;
}

/**
Set the historical monthly TS pointer.
@param ts historical monthly TS.
*/
public void setHistoricalMonthTS ( MonthTS ts ) {
	_historical_MonthTS = ts;
}

/**
Set the StateMod_DataSet component type for the data for this node.
@param related_smdata_type The StateMod_DataSet component type for the data for this node.
*/
public void setRelatedSMDataType ( int related_smdata_type )
{	_related_smdata_type = related_smdata_type;
}

/**
Set the second StateMod_DataSet component type for the data for this node.
@param related_smdata_type The second StateMod_DataSet component type for the data for this node.
This is only used for D&W nodes and should be set to the well component type.
*/
public void setRelatedSMDataType2 ( int related_smdata_type2 )
{	_related_smdata_type2 = related_smdata_type2;
}

/**
@param dataset StateMod dataset object.
@return validation results.
 */
public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String riverID = getCgoto();
	String dailyID = getCrunidy();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Stream gage identifier is blank.",
			"Specify a station identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Stream gage \"" + id + "\" name is blank.",
			"Specify a station name to clarify data.") );
	}
	if ( StateMod_Util.isMissing(riverID) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Stream gage \"" + id + "\" river ID is blank.",
			"Specify a river ID to associate the station with a river network node.") );
	}
	else {
		// Verify that the river node is in the data set, if the network is available
		if ( dataset != null ) {
			DataSetComponent comp = dataset.getComponentForComponentType(StateMod_DataSet.COMP_RIVER_NETWORK);
			List rinList = (List)comp.getData();
			if ( (rinList != null) && (rinList.size() > 0) ) {
				if ( StateMod_Util.indexOf(rinList, riverID) < 0 ) {
					validation.add(new StateMod_ComponentValidationProblem(this,"Stream gage \"" + id +
						"\" river network ID (" + riverID + ") is not found in the list of river network nodes.",
						"Specify a valid river network ID to associate the station with a river network node.") );
				}
			}
		}
	}
	// Verify that the daily ID is in the data set
	if ( !StateMod_Util.isMissing(dailyID) ) {
		if ( dataset != null ) {
			DataSetComponent comp = dataset.getComponentForComponentType(StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
			List risList = (List)comp.getData();
			if ( (risList != null) && (risList.size() > 0) ) {
				if ( !dailyID.equals("0") && !dailyID.equals("3") && !dailyID.equals("4") &&
					(StateMod_Util.indexOf(risList, dailyID) < 0) ) {
					validation.add(new StateMod_ComponentValidationProblem(this,"Stream gage \"" + id + "\" daily ID (" + dailyID +
						") is not 0, 3, or 4 and is not found in the list of stream gages.",
						"Specify the daily ID as 0, 3, 4, or that matches a stream gage ID.") );
				}
			}
		}
	}
	return validation;
}

/**
Write the new (updated) stream gage stations file.  If an original file is
specified, then the original header is carried into the new file.
@param infile Name of old file or null if no old file to update.
@param outfile Name of new file to create (can be the same as the old file).
@param theRivs Vector of StateMod_StreamGage to write.
@param newcomments New comments to write in the file header.
@param do_daily Indicates whether daily modeling fields should be written.
*/
public static void writeStateModFile( String infile, String outfile,
		List theRivs, List newcomments, boolean do_daily )
throws Exception
{	PrintWriter	out = null;
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String routine = "StateMod_StreamGage.writeStateModFile";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine, "Writing stream gage stations to file \"" +
		outfile + "\" using \"" + infile + "\" header..." );
	}

	try {
		// Process the header from the old file...
		out = IOUtil.processFileHeaders ( IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newcomments, commentIndicators, ignoredCommentIndicators, 0 );

		String cmnt = "#>";
		String iline = null;
		String format = null;
		StateMod_StreamGage riv = null;

		out.println ( cmnt + " *******************************************************" );
		out.println ( cmnt + "  Stream Gage Station File" );
		out.println ( cmnt );
		out.println ( cmnt + "     format:  (a12, a24, a12, 1x, a12)" );
		out.println ( cmnt );
		out.println ( cmnt + "  ID         crunid:  Station ID" );
		out.println ( cmnt + "  Name       runnam:  Station name" );
		out.println ( cmnt + "  River ID    cgoto:  River node with stream gage" );
		out.println ( cmnt + "  Daily ID  crunidy:  Daily stream station ID." );
		out.println ( cmnt );
		out.println ( cmnt + "    ID              Name           River ID     Daily ID   " );
		out.println ( cmnt + "---------eb----------------------eb----------exb----------e" );
		if ( do_daily ) {
			format = "%-12.12s%-24.24s%-12.12s %-12.12s";
		}
		else {
			format = "%-12.12s%-24.24s%-12.12s";
		}
		out.println ( cmnt );
		out.println ( cmnt + "EndHeader" );
		out.println ( cmnt );

		int num = 0;
		if ( theRivs != null ) {
			num = theRivs.size();
		}
		List v = new Vector ( 5 );
		for ( int i=0; i< num; i++ ) {
			riv = (StateMod_StreamGage) theRivs.get(i);
			v.clear ();
			v.add ( riv.getID() );
			v.add ( riv.getName() );
			v.add ( riv.getCgoto() );
			if ( do_daily ) {
				v.add ( riv.getCrunidy() );
			}
			iline = StringUtil.formatString ( v, format );
			out.println ( iline );
		}
		riv = null;
		routine = null;
		cmnt = null;
		iline = null;
		format = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
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
Writes a list of StateMod_StreamGage objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.  
@param newComments list of comments to add at the top of the file.
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
	fields.add("NodeID");
	fields.add("DailyID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_STREAMGAGE_STATIONS;
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
	StateMod_StreamGage gage = null;
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
		newComments2.add(1,"StateMod stream gage stations as a delimited list file.");
		newComments2.add(2,"");
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
			gage = (StateMod_StreamGage)data.get(i);
			
			line[0] = StringUtil.formatString(gage.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(gage.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(gage.getCgoto(), formats[2]).trim();
			line[3] = StringUtil.formatString(gage.getCrunidy(), formats[3]).trim();

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if ( j > 0 ) {
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