//------------------------------------------------------------------------------
// StateMod_RiverNetworkNode - class derived from StateMod_Data.  Contains
//	information read from the river network file
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
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMrivInfo
// 2003-06-10	JTS, RTi		* Folded dumpRiverInfoFile() into
//					  writeRiverInfoFile()
//					* Renamed parseRiverInfoFile() to
//					  readRiverInfoFile()
// 2003-06-23	JTS, RTi		Renamed writeRiverInfoFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readRiverInfoFile() to
//					readStateModFile()
// 2003-07-30	SAM, RTi		* Change name of class from
//					  StateMod_RiverInfo to
//					  StateMod_RiverNetworkNode.
//					* Remove all code related to the RIS
//					  file, which is now in
//					  StateMod_RiverStation.
//					* Change isDirty() back to setDirty().
// 2003-08-28	SAM, RTi		* Call setDirty() on each object in
//					  addition to the data set component.
//					* Clean up javadoc and parameters.
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
// 2005-06-13	JTS, RTi		Made a new toString().
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
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This StateMod_RiverNetworkNode class manages a record of data from the StateMod
river network (.rin) file.  It is derived from StateMod_Data similar to other
StateMod data objects.  It should not be confused with network node objects
(e.g., StateMod_Diversion_Node).   See the readStateModFile() method to read
the .rin file into a true network.
*/
public class StateMod_RiverNetworkNode  extends StateMod_Data
implements Cloneable, Comparable<StateMod_Data>, HasGeoRecord, StateMod_ComponentValidator {

/**
Downstream node identifier - third column of files.
*/
protected String _cstadn;
/**
Used with .rin (column 4) - not really used anymore except by old watright code.
*/
protected String _comment;
/**
Reference to spatial data for this diversion -- currently NOT cloned.  If null, then no spatial data
are available.
*/
protected GeoRecord _georecord = null;
/**
Used with .rin (column 5) - ground water maximum recharge limit.
*/
protected double _gwmaxr;
/**
The StateMod_DataSet component type for the node.  At some point the related object reference
may also be added, but there are cases when this is not known (only the type is
known, for example in StateDMI).
*/
protected int _related_smdata_type;

/**
Second related type.  This is only used for D&W node types and should
be set to the well stations component type.
*/
protected int _related_smdata_type2;

/**
Constructor.  The time series are set to null and other information is empty strings.
*/
public StateMod_RiverNetworkNode ()
{	super ( );
	initialize ();
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
Compares this object to another StateMod_RiverNetworkNode object.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_RiverNetworkNode r = (StateMod_RiverNetworkNode)data;

	res = _cstadn.compareTo(r._cstadn);
	if (res != 0) {
		return res;
	}

	res = _comment.compareTo(r._comment);
	if (res != 0) {
		return res;
	}

	if (_gwmaxr < r._gwmaxr) {
		return -1;
	}
	else if (_gwmaxr > r._gwmaxr) {
		return 1;
	}

	if (_related_smdata_type < r._related_smdata_type) {
		return -1;
	}
	else if (_related_smdata_type > r._related_smdata_type) {
		return 1;
	}

	if (_related_smdata_type2 < r._related_smdata_type2) {
		return -1;
	}
	else if (_related_smdata_type2 > r._related_smdata_type2) {
		return 1;
	}

	return 0;
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_RiverNetworkNode r = (StateMod_RiverNetworkNode)super.clone();
	r._isClone = true;
	return r;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = (StateMod_RiverNetworkNode)clone();
	((StateMod_RiverNetworkNode)_original)._isClone = false;
	_isClone = true;
}

/**
Finalize data for garbage collection.
*/
protected void finalize ()
throws Throwable
{	_cstadn = null;
	_comment = null;
	super.finalize();
}

/**
Get the comment used with the network file.
@return the comment.
*/
public String getComment ( ) {
	return _comment;
}

/**
Get the downstream river node identifier.
@return the downstream river node identifier.
*/
public String getCstadn() {
	return _cstadn;
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
public GeoRecord getGeoRecord() {
	return _georecord;
}

/**
Get the maximum recharge limit used with the network file.
@return the maximum recharge limit.
*/
public double getGwmaxr () {
	return _gwmaxr;
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
*/
private void initialize ()
{	_cstadn = "";
	_comment = "";
	_gwmaxr = -999;
	_smdata_type = StateMod_DataSet.COMP_RIVER_NETWORK;
}

/**
Read river network or stream gage information and return a list of StateMod_RiverNetworkNode.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List<StateMod_RiverNetworkNode> readStateModFile ( String filename )
throws Exception
{	String rtn = "StateMod_RiverNetworkNode.readStateModFile";
	List<StateMod_RiverNetworkNode> theRivs = new Vector<StateMod_RiverNetworkNode>();
	String iline, s;
	List<Object> v = new Vector<Object>( 7 );
	int [] format_0;
	int [] format_0w;
	format_0 = new int[7];
	format_0[0] = StringUtil.TYPE_STRING;
	format_0[1] = StringUtil.TYPE_STRING;
	format_0[2] = StringUtil.TYPE_STRING;
	format_0[3] = StringUtil.TYPE_STRING;
	format_0[4] = StringUtil.TYPE_STRING;
	format_0[5] = StringUtil.TYPE_STRING;
	format_0[6] = StringUtil.TYPE_STRING;
	format_0w = new int [7];
	format_0w[0] = 12;
	format_0w[1] = 24;
	format_0w[2] = 12;
	format_0w[3] = 1;
	format_0w[4] = 12;
	format_0w[5] = 1;
	format_0w[6] = 8;

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

			// allocate new StateMod_RiverNetworkNode
			StateMod_RiverNetworkNode aRiverNode = new StateMod_RiverNetworkNode();

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
			aRiverNode.setCstadn ( ((String)v.get(2)).trim());
			// 3 is whitespace
			// Expect that we also may have the comment and possibly the gwmaxr value...
			aRiverNode.setComment ( ((String)v.get(4)).trim() );
			// 5 is whitespace
			s = ((String)v.get(6)).trim();
			if ( s.length() > 0 ) { 
				aRiverNode.setGwmaxr ( StringUtil.atod(s) );
			}
			
			// add the node to the vector of river nodes
			theRivs.add ( aRiverNode );
		}
	} catch (Exception e) {
		Message.printWarning ( 3, rtn, "Error reading \"" + filename + "\" at line " + linecount );
		throw e;
	}
	finally {
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
	StateMod_RiverNetworkNode r = (StateMod_RiverNetworkNode)_original;
	super.restoreOriginal();
	_cstadn = r._cstadn;
	_comment = r._comment;
	_gwmaxr = r._gwmaxr;
	_related_smdata_type = r._related_smdata_type;
	_related_smdata_type2 = r._related_smdata_type2;
	_isClone = false;
	_original = null;
}

/**
Set the comment for use with the network file.
@param comment Comment for node.
*/
public void setComment ( String comment )
{	if ( (comment != null) && !_comment.equals(comment) ) {
		_comment = comment;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty ( _smdata_type, true );
		}
	}
}

/**
Set the downstream river node identifier.
@param cstadn Downstream river node identifier.
*/
public void setCstadn ( String cstadn ) {
	if ( (cstadn != null) && !cstadn.equals(_cstadn )) {
		_cstadn = cstadn;
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
Set the maximum recharge limit for network file.
@param gwmaxr Maximum recharge limit.
*/
public void setGwmaxr ( String gwmaxr )
{	if ( StringUtil.isDouble(gwmaxr) ) {
		setGwmaxr ( StringUtil.atod(gwmaxr) );
	}
}

/**
Set the maximum recharge limit for network file.
@param gwmaxr Maximum recharge limit.
*/
public void setGwmaxr ( double gwmaxr )
{	if ( _gwmaxr != gwmaxr ) {
		_gwmaxr = gwmaxr;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty ( _smdata_type, true );
		}
	}
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
@return Validation results.
 */
public StateMod_ComponentValidation validateComponent ( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String downstreamRiverID = getCstadn();
	double gwmaxr = getGwmaxr();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"River node identifier is blank.",
			"Specify a river node identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"River node \"" + id + "\" name is blank.",
			"Specify a river node name to clarify data.") );
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
	if ( StateMod_Util.isMissing(downstreamRiverID) && !name.equalsIgnoreCase("END") &&
		!name.endsWith("_END")) {
		validation.add(new StateMod_ComponentValidationProblem(this,"River node \"" + id + "\" downstream node ID is blank.",
			"Specify a downstream node ID.") );
	}
	else {
		// Verify that the downstream river node is in the data set, if the network is available - skip this
		// check for the end node.
		if ( (rinList != null) && !name.equalsIgnoreCase("END") && !name.endsWith("_END")) {
			if ( StateMod_Util.indexOf(rinList, downstreamRiverID) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"River node \"" + id +
					"\" downstream node ID (" + downstreamRiverID + ") is not found in the list of river network nodes.",
					"Specify a valid river network ID for the downstream node.") );
			}
		}
	}
	if ( !StateMod_Util.isMissing(gwmaxr) && !(gwmaxr >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"River node \"" + id +
			"\" maximum groundwater recharge (" +
			StringUtil.formatString(gwmaxr,"%.2f") + ") is invalid.",
			"Specify the maximum groundwater recharge as a number >= 0.") );
	}
	return validation;
}

/**
Write the new (updated) river network file to the StateMod river network
file.  If an original file is specified, then the original header is carried into the new file.
@param infile Name of old file or null if no old file to update.
@param outfile Name of new file to create (can be the same as the old file).
@param theRivs list of StateMod_RiverNetworkNode to write.
@param newcomments New comments to write in the file header.
@param doWell Indicates whether well modeling fields should be written.
*/
public static void writeStateModFile( String infile, String outfile,
		List<StateMod_RiverNetworkNode> theRivs, List<String> newcomments, boolean doWell )
throws Exception
{	PrintWriter	out = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String routine = "StateMod_RiverNetworkNode.writeStateModFile";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine, "Writing river network file \"" +
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
		StateMod_RiverNetworkNode riv = null;
	
		out.println ( cmnt + " *******************************************************" );
		out.println ( cmnt + "  StateMod River Network File" );
		out.println ( cmnt + "  WARNING - if .net file is available, it should be edited and the .rin" );
		out.println ( cmnt + "  file should be created from the .net" );
		out.println ( cmnt );
		out.println ( cmnt + "  format:  (a12, a24, a12, 1x, a12, 1x, f8.0)" );
		out.println ( cmnt );
		out.println ( cmnt + "  ID           cstaid:  Station ID" );
		out.println ( cmnt + "  Name         stanam:  Station name" );
		out.println ( cmnt + "  Downstream   cstadn:  Downstream node ID" );
		out.println ( cmnt + "  Comment     comment:  Alternate identifier/comment." );
		out.println ( cmnt + "  GWMax        gwmaxr:  Max recharge limit (cfs) - see iwell in control file." );
		out.println ( cmnt );
		out.println ( cmnt + "   ID                Name          DownStream     Comment    GWMax  " );
		out.println ( cmnt + "---------eb----------------------eb----------exb----------exb------e" );
		if ( doWell ) {
			format = "%-12.12s%-24.24s%-12.12s %-12.12s %8.8s";
		}
		else {
			format = "%-12.12s%-24.24s%-12.12s %-12.12s";
		}
		out.println ( cmnt );
		out.println ( cmnt + "EndHeader" );
		out.println ( cmnt );

		int num = 0;
		if ( theRivs != null ) {
			num = theRivs.size();
		}
		List<Object> v = new Vector<Object>( 5 );
		for ( int i=0; i< num; i++ ) {
			riv = theRivs.get(i);
			v.clear ();
			v.add ( riv.getID() );
			v.add ( riv.getName() );
			v.add ( riv.getCstadn() );
			v.add ( riv.getComment() );
			if ( doWell ) {
				// Format as string since main format uses string.
				v.add ( StringUtil.formatString(riv.getGwmaxr(), "%8.0f") );
			}
			iline = StringUtil.formatString ( v, format );
			out.println ( iline );
		}
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
Writes a list of StateMod_RiverNetworkNode objects to a list file.  A header 
is printed to the top of the file, containing the commands used to generate the
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments new comments to add to the file header.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List<StateMod_RiverNetworkNode> data,
	List<String> newComments ) 
throws Exception
{	String routine = "StateMod_RiverNetworkNode.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("ID");
	fields.add("Name");
	fields.add("DownstreamID");
	fields.add("Comment");
	fields.add("GWMaxRecharge");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RIVER_NETWORK;
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
	StateMod_RiverNetworkNode rnn = null;
	String[] line = new String[fieldCount];
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
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
		newComments2.add(1,"StateMod river network as a delimited list file.");
		newComments2.add(2,"See also the generalized network file.");
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
			rnn = (StateMod_RiverNetworkNode)data.get(i);
			
			line[0] = StringUtil.formatString(rnn.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(rnn.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(rnn.getCstadn(), formats[2]).trim();
			line[3] = StringUtil.formatString(rnn.getComment(), formats[3]).trim();
			line[4] = StringUtil.formatString(rnn.getGwmaxr(), formats[4]).trim();

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

public String toString() {
	return "ID: " + _id + "    Downstream node: " + _cstadn;
}

}