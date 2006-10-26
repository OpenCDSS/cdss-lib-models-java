//------------------------------------------------------------------------------
// StateMod_StreamEstimate - class derived from StateMod_Data.  Contains
//	information the stream estimate station file (part of old .ris or new
//	.ses?)
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2003-08-14	Steven A. Malers, RTi	Copy StateMod_RiverStation and modify
//					accordingly.  The two classes are
//					essentially identical because they are
//					being read from the same file.
//					However, these baseflow nodes do not
//					have historical data.
// 2003-08-28	SAM, RTi		* Call setDirty() on each object in
//					  addition to the data component.
//					* Clean up handling of time series.
// 2003-09-11	SAM, RTi		Rename class from
//					StateMod_BaseFlowStation to
//					StateMod_StreamEstimate and make
//					appropriate changes throughout.
// 2003-09-12	SAM, RTi		* Ray Bennett decided to keep one file
//					  for the baseflow time series so no
//					  need to split apart.
//					* Rename processRiverData() to
//					  processStreamData().
// 2004-07-06	SAM, RTi		* Fix bug where writing the file was not
//					  adjusting the path using the working
//					  directory.
//					* Add information to the header comments
//					  to better explain the file contents.
//					* Overload the constructor to allow
//					  initialization to default values or
//					  missing data.
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
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;
import java.lang.Double;
import java.lang.Integer;

import RTi.GIS.GeoView.GeoRecord;
import RTi.TS.TS;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TSUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_StreamEstimate 
extends StateMod_Data
implements Cloneable, Comparable {

protected MonthTS	_baseflow_MonthTS;
					// Monthly base flow time series, for
					// use with the stream estimate station
					// file, read from the .xbm/.rim file.
protected DayTS		_baseflow_DayTS;// Daily base flow time series, read
					// from the .rid file
protected String	_crunidy;	// Used with .rbs (column 4) - daily
					// stream station identifier.
protected GeoRecord	_georecord;	// Reference to spatial data for this
					// stream estimate station.

protected int		_related_smdata_type;
					// The StateMod_DataSet component type
					// for the node.  At some point the
					// related object reference may also be
					// added, but there are cases when this
					// is not known (only the type is
					// known, for example in StateDMI).

protected int		_related_smdata_type2;
					// Second related type.  This is only
					// used for D&W node types and should
					// be set to the well stations component
					// type.

// REVISIT - should we connect the .rib data similar to how water rights are
// connected?   The data are not used as much as water rights.
	
/**
Constructor for stream estimate station.
The time series are set to null and other information is empty strings.
*/
public StateMod_StreamEstimate ()
{	this ( true );
}

/**
Constructor for stream estimate station.
@param initialize_defaults If true, the time series are set to null and other
information is empty strings - this is suitable for the StateMod GUI.  If false,
the data are set to missing - this is suitable for StateDMI where data will
be filled.
*/
public StateMod_StreamEstimate ( boolean initialize_defaults )
{	super ( );
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
	StateMod_StreamEstimate s = (StateMod_StreamEstimate)super.clone();
	s._isClone = true;
	return s;
}

/**
Compares this object to another StateMod_StreamEstimate object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_StreamEstimate s = (StateMod_StreamEstimate)o;
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
Connect the time series pointers to the appropriate time series objects
for all the elements in the Vector of StateMod_StreamEstimate objects.
@param rivs Vector of StateMod_StreamEstimate (e.g., as read from StateMod 
.rbs file).
@param baseflow_MonthTS Vector of baseflow MonthTS (e.g., as read from StateMod
.xbm or .rim file).  Pass as null to not connect.
@param baseflow_DayTS Vector of baseflow MonthTS (e.g., as read from StateMod
.xbd? or .rid file).  Pass as null to not connect.
*/
public static void connectAllTS (	Vector rivs,
					Vector baseflow_MonthTS,
					Vector baseflow_DayTS )
{	if ( rivs == null ) {
		return;
	}
	StateMod_StreamEstimate riv;
	int size = rivs.size();
	for ( int i=0; i < size; i++ ) {
		riv = (StateMod_StreamEstimate)rivs.elementAt(i);
		if ( baseflow_MonthTS != null ) {
			riv.connectBaseflowMonthTS ( baseflow_MonthTS );
		}
		if ( baseflow_DayTS != null ) {
			riv.connectBaseflowDayTS ( baseflow_DayTS );
		}
	}
}

/**
Connect the daily base streamflow TS pointer to the appropriate TS in the
Vector.
A connection is made if the node identifier matches the time series location.
@param tslist Vector of DayTS.
*/
private void connectBaseflowDayTS ( Vector tslist )
{	if ( tslist == null ) {
		return;
	}
	DayTS ts;
	int size = tslist.size();
	_baseflow_DayTS = null;
	for ( int i=0; i < size; i++ ) {
		ts = (DayTS)tslist.elementAt(i);
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
@param tslist monthly baseflow time series. 
*/
public void connectBaseflowMonthTS ( Vector tslist )
{	if ( tslist == null ) {
		return;
	}
	int num_TS = tslist.size();

	MonthTS ts = null;
	_baseflow_MonthTS = null;
	for (int i=0; i<num_TS; i++) {
		ts = (MonthTS)tslist.elementAt(i);
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
Creates a copy of the object for later use in checking to see if it was 
changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_StreamEstimate)_original)._isClone = false;
	_isClone = true;
}

/**
Finalize data for garbage collection.
*/
protected void finalize ()
throws Throwable
{	
	_baseflow_MonthTS = null;
	_baseflow_DayTS = null;
	_crunidy = null;
	_georecord = null;
	super.finalize();
}

/**
Return the daily baseflow file associated with the stream estimate node.
@return the daily baseflow file associated with the stream estimate node.
Return null if no time series is available.
*/
public DayTS getBaseflowDayTS ( )
{	return _baseflow_DayTS;
}

/**
Return the monthly baseflow file associated with the stream estimate node.
@return the monthly baseflow file associated with the stream estimate node.
Return null if no time series is available.
*/
public MonthTS getBaseflowMonthTS ( )
{	return _baseflow_MonthTS;
}

/**
Get the daily stream station identifier used with the stream estimate station.
@return the daily stream station identifier.
*/
public String getCrunidy ( ) {
	return _crunidy;
}

/**
Get the geographical data associated with the diversion.
@return the GeoRecord for the diversion.
*/
public GeoRecord getGeoRecord()
{	return _georecord;
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
information is empty strings - this is suitable for the StateMod GUI.  If false,
the data are set to missing - this is suitable for StateDMI where data will
be filled.
*/
private void initialize ( boolean initialize_defaults )
{	_smdata_type = _dataset.COMP_STREAMESTIMATE_STATIONS;
	_cgoto = "";
	_baseflow_MonthTS = null;
	_baseflow_DayTS = null;
	_related_smdata_type = _dataset.COMP_UNKNOWN;
	if ( initialize_defaults ) {
		// Reasonable defaults...
		_crunidy = "0";	// Estimate average daily from monthly data.
	}
	else {	// Missing...
		_crunidy = "";
	}
	_georecord = null;
}

/**
This method can be called when an old-style *.ris file containing both stream
gage station and stream estimate stations is read.  The following adjustments to
the data occur:
<ol>
<li>	Objects in the ris that do not have matching identifiers in the rih are
	removed from the ris.</li>
<li>	Objects in the rbs that do not have matching identifiers in the rib are
	removed from the rbs.</li>
</ol>
@param ris_Vector Vector of StateMod_StreamGage, after initial read.
@param rih_Vector Vector of historical MonthTS, after initial read.
@param rbs_Vector Vector of StateMod_StreamEstimate, after initial read.
@param rib_Vector Vector of StateMod_StreamEstimte_Coefficients, after initial
read.
*/
public static void processStreamData (	Vector ris_Vector,
					Vector rih_Vector,
					Vector rbs_Vector,
					Vector rib_Vector )
{	int nris = 0;
	if ( ris_Vector != null ) {
		nris = ris_Vector.size();
	}
	int nrih = 0;
	if ( rih_Vector != null ) {
		nrih = rih_Vector.size();
	}
	int nrbs = 0;
	if ( rbs_Vector != null ) {
		nrbs = rbs_Vector.size();
	}
	int nrib = 0;
	if ( rib_Vector != null ) {
		nrib = rib_Vector.size();
	}

	int i, j, pos;
	TS ts;
	StateMod_StreamGage ris;
	String id;
	boolean found = false;
	for ( i=0; i < nris; i++) {
		ris = (StateMod_StreamGage)ris_Vector.elementAt(i);
		id = ris.getID();
		found = false;
		for ( j = 0; j < nrih; j++ ) {
			ts = (TS)rih_Vector.elementAt(j);
			if ( id.equalsIgnoreCase(ts.getLocation())) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			ris_Vector.removeElementAt ( i );
			--i;	// So next item will be properly checked.
			--nris;
		}
	}
	StateMod_StreamEstimate rbs;
	StateMod_StreamEstimate_Coefficients rib;
	for ( i=0; i < nrbs; i++) {
		rbs = (StateMod_StreamEstimate)rbs_Vector.elementAt(i);
		id = rbs.getID();
		found = false;
		for ( j = 0; j < nrib; j++ ) {
			rib = (StateMod_StreamEstimate_Coefficients)
				rib_Vector.elementAt(j);
			if ( id.equalsIgnoreCase(rib.getID())) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			rbs_Vector.removeElementAt ( i );
			--i;	// So next item will be properly checked.
			--nrbs;
		}
	}
}

/**
Read river station file and store return a Vector of StateMod_StreamEstimate.
Note that ALL stations are returned.  Call the processRiverData() method to
remove instances that are not actually base flow nodes.
@return a Vector of StateMod_BaseFlowStation.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static Vector readStateModFile ( String filename )
throws Exception
{	String rtn = "StateMod_StreamEstimate.readStateModFile";
	Vector theRivs = new Vector();
	String iline, s;
	Vector v = new Vector ( 5 );
	int i;
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
		Message.printDebug ( 10, rtn, 
		"in " + rtn + " reading file: " + filename );
	}
	try {	BufferedReader in = new BufferedReader (
			new FileReader ( filename ));
		while ( (iline = in.readLine()) != null )
		{
			++linecount;
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0)
				continue;

			// allocate new StateMod_BaseFlowStation node
			StateMod_StreamEstimate aRiverNode =
				new StateMod_StreamEstimate();

			// line 1
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, 
				"line 1: " + iline );
			}
			StringUtil.fixedRead ( iline, format_0, format_0w, v );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, 
				"Fixed read returned " 
				+ v.size() + " elements");
			}
			aRiverNode.setID ( ((String)v.elementAt(0)).trim() );
			aRiverNode.setName ( ((String)v.elementAt(1)).trim() );
			aRiverNode.setCgoto ( ((String)v.elementAt(2)).trim() );
			aRiverNode.setCrunidy (((String)v.elementAt(3)).trim());

			// add the node to the vector of river nodes
			theRivs.addElement ( aRiverNode );
		}
	} catch (Exception e) {
		// Clean up...
		v = null;
		rtn = null;
		iline = null;
		format_0 = null;
		format_0w = null;
		s = null;
		Message.printWarning ( 2, rtn,
		"Error reading \"" + filename + "\" at line " + linecount );
		throw e;
	}
	// Clean up...
	v = null;
	rtn = null;
	iline = null;
	format_0 = null;
	format_0w = null;
	s = null;
	return theRivs;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_StreamEstimate s = (StateMod_StreamEstimate)_original;
	super.restoreOriginal();
	_crunidy = s._crunidy;
	_related_smdata_type = s._related_smdata_type;
	_related_smdata_type2 = s._related_smdata_type2;
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
Set the StateMod_DataSet component type for the data for this node.
@param related_smdata_type The StateMod_DataSet component type for the data for 
this node.
*/
public void setRelatedSMDataType ( int related_smdata_type )
{	_related_smdata_type = related_smdata_type;
}

/**
Set the second StateMod_DataSet component type for the data for this node.
@param related_smdata_type The second StateMod_DataSet component type for the
data for this node.
This is only used for D&W nodes and should be set to the well component type.
*/
public void setRelatedSMDataType2 ( int related_smdata_type2 )
{	_related_smdata_type2 = related_smdata_type2;
}

/**
Write the new (updated) river baseflow stations file.  If an original file is
specified, then the original header is carried into the new file.
@param infile Name of old file or null if no old file to update.
@param outfile Name of new file to create (can be the same as the old file).
@param theRivs Vector of StateMod_StreamEstimate to write.
@param newcomments New comments to write in the file header.
@param do_daily Indicates whether daily modeling fields should be written.
*/
public static void writeStateModFile( String infile, String outfile,
			Vector theRivs, String[] newcomments,
			boolean do_daily )
throws Exception
{	PrintWriter	out;
	String [] comment_str = { "#" };
	String [] ignore_str = { "#>" };
	String routine = "StateMod_StreamEstimate.writeStateModFile";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine, 
		"Writing stream estimate stations to file \"" +
		outfile + "\" using \"" + infile + "\" header..." );
	}

	try {	// Process the header from the old file...
		out = IOUtil.processFileHeaders (
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newcomments, comment_str, ignore_str, 0 );

		String cmnt = "#>";
		String iline = null;
		String format = null;
		StateMod_StreamEstimate riv = null;

		out.println ( cmnt + 
			" *****************************" +
			"**************************" );
		out.println ( cmnt + "  Stream Estimate Station File" );
		out.println ( cmnt );
		out.println ( cmnt +
		"  This file contains a list of stations at which stream" );
		out.println ( cmnt +
		"  baseflows are estimated." );
		out.println ( cmnt +
		"  The IDs for nodes will match the IDs in one of the" +
		"following files:" );
		out.println ( cmnt +
		"      Diversion stations" );
		out.println ( cmnt +
		"      Reservoir stations" );
		out.println ( cmnt +
		"      Instream flow stations" );
		out.println ( cmnt +
		"      Well stations" );
		out.println ( cmnt +
		"  Stream gages with historical data are in the stream gage" +
		" station file." );
		out.println ( cmnt +
		"  \"Other\" nodes with baseflow data are only listed in the" +
		" river network file." );
		out.println ( cmnt );
		out.println ( cmnt +
		"     format:  (a12, a24, a12, 1x, a12)" );
		out.println ( cmnt );
		out.println ( cmnt +
		"  ID         crunid:  Station ID" );
		out.println ( cmnt +
		"  Name       runnam:  Station name" );
		out.println ( cmnt +
		"  River ID    cgoto:  River node with stream estimate " +
			"station" );
		out.println ( cmnt +
		"  Daily ID  crunidy:  Daily stream station ID." );
		out.println ( cmnt );
		out.println ( cmnt +
		"    ID              Name          " +
		" River ID     Daily ID   " );
		out.println ( cmnt +
		"---------eb----------------------e" +
		"b----------exb----------e" );
		if ( do_daily ) {
			format = "%-12.12s%-24.24s%-12.12s %-12.12s";
		}
		else {	format = "%-12.12s%-24.24s%-12.12s";
		}
		out.println ( cmnt );
		out.println ( cmnt + "EndHeader" );
		out.println ( cmnt );

		int num = 0;
		if ( theRivs != null ) {
			num = theRivs.size();
		}
		Vector v = new Vector ( 5 );
		for ( int i=0; i< num; i++ ) {
			riv = (StateMod_StreamEstimate)theRivs.elementAt(i);
			v.removeAllElements ();
			v.addElement ( riv.getID() );
			v.addElement ( riv.getName() );
			v.addElement ( riv.getCgoto() );
			if ( do_daily ) {
				v.addElement ( riv.getCrunidy() );
			}
			iline = StringUtil.formatString ( v, format );
			out.println ( iline );
		}
		riv = null;
		routine = null;
		cmnt = null;
		iline = null;
		format = null;

		out.flush();
		out.close();
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e );
		throw e;
	}
}

/**
Writes a Vector of StateMod_StreamEstimate objects to a list file.  A header is 
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
	fields.add("RiverNodeID");
	fields.add("DailyID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS;
	
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
	StateMod_StreamEstimate se = null;
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
			se = (StateMod_StreamEstimate)data.elementAt(i);
			
			line[0] = StringUtil.formatString(se.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(se.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(se.getCgoto(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(se.getCrunidy(), 
				formats[3]).trim();

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

} // End StateMod_StreamEstimate
