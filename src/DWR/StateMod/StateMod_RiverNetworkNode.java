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
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;
import java.lang.Double;
import java.lang.Integer;

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
public class StateMod_RiverNetworkNode 
extends StateMod_Data
implements Cloneable, Comparable {

protected String 	_cstadn;	// Downstream node identifier
					// Third column of files
protected String	_comment;	// Used with .rin (column 4) - not
					// really used anymore except by
					// old watright code.
protected double	_gwmaxr;	// Used with .rin (column 5) - ground
					// water maximum recharge limit.

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
	
/**
Constructor.
The time series are set to null and other information is empty strings.
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
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_RiverNetworkNode r = (StateMod_RiverNetworkNode)o;

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
	_original = clone();
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
	_smdata_type = _dataset.COMP_RIVER_NETWORK;
}

/**
Read river network or stream gage information and return a Vector of
StateMod_RiverNetworkNode.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static Vector readStateModFile ( String filename )
throws Exception
{	String rtn = "StateMod_RiverNetworkNode.readStateModFile";
	Vector theRivs = new Vector();
	String iline, s;
	Vector v = new Vector ( 7 );
	int i;
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
		Message.printDebug ( 10, rtn, "in " + rtn + " reading file: " 
		+ filename );
	}
	try {	BufferedReader in = new BufferedReader (
			new FileReader ( filename ));
		while ( (iline = in.readLine()) != null ) {
			++linecount;
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0)
				continue;

			// allocate new StateMod_RiverNetworkNode
			StateMod_RiverNetworkNode aRiverNode =
				new StateMod_RiverNetworkNode();

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
			aRiverNode.setCstadn ( ((String)v.elementAt(2)).trim());

			// Expect that we also may have the comment and
			// possibly the gwmaxr value...
			aRiverNode.setComment (
				((String)v.elementAt(3)).trim() );
			s = ((String)v.elementAt(4)).trim();
			if ( s.length() > 0 ) { 
				aRiverNode.setGwmaxr ( StringUtil.atod(s) );
			}
			
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
Write the new (updated) river network file to the StateMod river network
file.  If an original file is specified, then the original header is carried
into the new file.
@param infile Name of old file or null if no old file to update.
@param outfile Name of new file to create (can be the same as the old file).
@param theRivs Vector of StateMod_RiverInfo to write.
@param newcomments New comments to write in the file header.
@param do_well Indicates whether well modeling fields should be written.
*/
public static void writeStateModFile( String infile, String outfile,
			Vector theRivs, String[] newcomments, boolean do_well )
throws Exception
{	PrintWriter	out;
	String [] comment_str = { "#" };
	String [] ignore_str = { "#>" };
	String routine = "StateMod_RiverNetworkNode.writeStateModFile";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine, 
		"Writing river network file \"" +
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
	StateMod_RiverNetworkNode riv = null;

		out.println ( cmnt + 
			" *****************************" +
			"**************************" );
		out.println ( cmnt + "  StateMod River Network File" );
		out.println ( cmnt + "  WARNING - if .net file is available, " +
				"it should be edited and the .rin" );
		out.println ( cmnt + "  file should be created from the .net" );
		out.println ( cmnt );
		out.println ( cmnt +
		"  format:  (a12, a24, a12, 1x, a12, 1x, f8.0)" );
		out.println ( cmnt );
		out.println ( cmnt +
		"  ID           cstaid:  Station ID" );
		out.println ( cmnt +
		"  Name         stanam:  Station name" );
		out.println ( cmnt +
		"  Downstream   cstadn:  Downstream node ID" );
		out.println ( cmnt +
		"  Comment     comment:  Alternate identifier/"
		+ "comment." );
		out.println ( cmnt +
		"  GWMax        gwmaxr:  Max recharge limit (cfs) - "
		+ "see iwell in control file." );
		out.println ( cmnt );
		out.println ( cmnt +
		"   ID                Name         " +
		" DownStream     Comment    GWMax  " );
		out.println ( cmnt +
		"---------eb----------------------e" +
		"b----------exb----------exb------e" );
		if ( do_well ) {
			format =
			"%-12.12s%-24.24s%-12.12s %-12.12s %-8.8s";
		}
		else {	format = "%-12.12s%-24.24s%-12.12s %-12.12s";
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
			riv = (StateMod_RiverNetworkNode) theRivs.elementAt(i);
			v.removeAllElements ();
			v.addElement ( riv.getID() );
			v.addElement ( riv.getName() );
			v.addElement ( riv.getCstadn() );
			v.addElement ( riv.getComment() );
			if ( do_well ) {
				v.addElement (
				StringUtil.formatString(riv.getGwmaxr(),
				"%8.0f") );
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
Writes a Vector of StateMod_RiverNetworkNode objects to a list file.  A header 
is printed to the top of the file, containing the commands used to generate the
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
	fields.add("DownstreamID");
	fields.add("Comment");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RIVER_NETWORK;
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
	StateMod_RiverNetworkNode rnn = null;
	String[] line = new String[fieldCount];
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
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
			rnn = (StateMod_RiverNetworkNode)data.elementAt(i);
			
			line[0] = StringUtil.formatString(rnn.getID(),
				formats[0]).trim();
			line[1] = StringUtil.formatString(rnn.getName(),
				formats[1]).trim();
			line[2] = StringUtil.formatString(rnn.getCstadn(),
				formats[2]).trim();
			line[3] = StringUtil.formatString(rnn.getComment(),
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

public String toString() {
	return "ID: " + _id + "    Downstream node: " + _cstadn;
}

} // End StateMod_RiverNetworkNode
