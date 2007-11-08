// ----------------------------------------------------------------------------
// StateCU_ClimateStation.java - class for the StateCU climate station objects,
//				CLI file
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-05-07	Steven A. Malers, RTi	Initial version copied from
//					CUClimateStationWeights.
// 2003-06-04	SAM, RTi		Rename class from CUClimateStation to
//					StateCU_ClimateStation.
// 2005-01-17	J. Thomas Sapienza, RTi	* Added createBackup().
//					* Added restoreOriginal().
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.StateCU;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
The StateCU_ClimateStation class holds data from one object (data line) in a
StateCU climate station (CLI) file.  The method names do not exactly
correspond to StateCU file variables because the content is general enough to
describe with more general conventions.  The unique identifier for an object
is the climate station identifier.
*/
public class StateCU_ClimateStation 
extends StateCU_Data implements StateCU_Component {

// List data in the same order as in the StateCU documentation.

// StationID is stored in the base class ID.
// Station name is stored in the base class name.

private double __latitude = StateCU_Util.MISSING_DOUBLE;
private double __elevation = StateCU_Util.MISSING_DOUBLE;

private String __region1 = StateCU_Util.MISSING_STRING;
private String __region2 = StateCU_Util.MISSING_STRING;

/**
Construct a StateCU_ClimateStation instance and set to missing and empty data.
*/
public StateCU_ClimateStation()
{	super();
}

/**
Performs specific data checks and returns a list of data
that failed the data checks.
@param count Index of the data vector currently being checked.
@param dataset StateCU dataset currently in memory.
@param props Extra properties to perform checks with.
@return List of invalid data.
 */
public String[] checkComponentData( int count, StateCU_DataSet dataset,
PropList props ) {
	// TODO KAT 2007-04-12 Add specific checks here ...
	return null;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_ClimateStation)_original)._isClone = false;
	_isClone = true;
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__region1 = null;
	__region2 = null;
	super.finalize();
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-12 
	// When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Return the station elevation, feet.
@return the station elevation, feet.
*/
public double getElevation() {
	return __elevation;
}

/**
Return the station latitude, decimal degrees.
@return the station latitude, decimal degrees.
*/
public double getLatitude() {
	return __latitude;
}

/**
Return Region 1.
@return Region 1.
*/
public String getRegion1() {
	return __region1;
}

/**
Return Region 2.
@return Region 2.
*/
public String getRegion2() {
	return __region2;
}

/**
Read the StateCU climate stations file and return as a Vector of
CUClimateStation.
@param filename filename containing CLI records.
*/
public static Vector readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_ClimateStation.readStateCUFile";
	String iline = null;
	Vector v = new Vector ( 10 );
	Vector sta_Vector = new Vector ( 100 );	// Data to return.
	int format_0[] = {	StringUtil.TYPE_STRING,		// station id
				StringUtil.TYPE_STRING,		// latitude
				StringUtil.TYPE_STRING,		// elevation
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// region1
				StringUtil.TYPE_STRING,		// region2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING };	// station name

	int format_0w[] = {	12,	// station id
				6,	// latitude
				9,	// elevation
				2,
				20,	// region1
				8,	// region2
				2,
				24 };	// station name
	StateCU_ClimateStation sta = null;
	BufferedReader in = null;

	Message.printStatus ( 1, rtn,
		"Reading StateCU climate station file: \"" +
		filename + "\"" );
	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	String string;
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}

		// allocate new StateCU_ClimateStation instance...
		sta = new StateCU_ClimateStation();

		StringUtil.fixedRead ( iline, format_0, format_0w, v );
		sta.setID ( ((String)v.elementAt(0)).trim() ); 
		string = ((String)v.elementAt(1)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			sta.setLatitude ( StringUtil.atod(string) );
		}
		string = ((String)v.elementAt(2)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			sta.setElevation ( StringUtil.atod(string) );
		}
		sta.setRegion1 ( ((String)v.elementAt(3)).trim() ); 
		sta.setRegion2 ( ((String)v.elementAt(4)).trim() ); 
		sta.setName ( ((String)v.elementAt(5)).trim() ); 

		// add the StateCU_ClimateStation to the vector...
		sta_Vector.addElement ( sta );
	}
	if ( in != null ) {
		in.close();
	}
	return sta_Vector;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_ClimateStation station = (StateCU_ClimateStation)_original;
	super.restoreOriginal();

	__latitude = station.__latitude;
	__elevation = station.__elevation;
	__region1 = station.__region1;
	__region2 = station.__region2;

	_isClone = false;
	_original = null;
}

/**
Set the elevation, feet.
@param elevation The elevation, feet.
*/
public void setElevation(double elevation) {
	__elevation = elevation;
}

/**
Set the latitude, decimal degrees.
@param latitude The latitude, decimal degrees.
*/
public void setLatitude(double latitude) {
	__latitude = latitude;
}

/**
Set Region 1.
@param region1 Region 1.
*/
public void setRegion1(String region1) {
	__region1 = region1;
}

/**
Set Region 2.
@param region2 Region 2.
*/
public void setRegion2(String region2) {
	__region2 = region2;
}

/**
Write a Vector of StateCU_ClimateStation to a file.  The filename is adjusted
to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A Vector of StateCU_ClimateStation to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile (	String filename_prev, String filename,
					Vector data_Vector,
					String [] new_comments )
throws IOException
{	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir (
		filename_prev );
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
		new_comments, comment_str, ignore_comment_str, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" +
			full_filename + "\"" );
	}
	writeVector ( data_Vector, out );
	out.flush();
	out.close();
	out = null;
}

/**
Write a Vector of StateCU_ClimateStation to an opened file.
@param data_Vector A Vector of StateCU_ClimateStation to write.
@param out output PrintWriter.
@exceptoin IOException if an error occurs.
*/
private static void writeVector ( Vector data_Vector, PrintWriter out )
throws IOException
{	int i;
	String iline;
	String cmnt = "#>";
	// Missing data handled by formatting all as strings...
	String format =
	"%-12.12s%6.6s%9.9s  %-20.20s%-8.8s  %-24.24s";
	Vector v = new Vector(6);	// Reuse for all output lines.

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Climate Stations File" );
	out.println ( cmnt );
	out.println ( cmnt +
		"  Record format (a12,f6.2,f9.2,2x,a20,a8,2x,a24)" );
	out.println ( cmnt );
	out.println ( cmnt +
		"  StationID:  Station identifier (e.g., 3951)" );
	out.println ( cmnt +
		"        Lat:  Latitude (decimal degrees)" );
	out.println ( cmnt +
		"       Elev:  Elevation (feet)" );
	out.println ( cmnt +
		"    Region1:  Region1 (e.g., County)" );
	out.println ( cmnt +
		"    Region2:  Region2 (e.g., Hydrologic Unit Code, HUC)" );
	out.println ( cmnt +
		"StationName:  Station name" );
	out.println ( cmnt );
	out.println ( cmnt +	
	" StationID  Lat   Elev            Region1      Region2  " +
	"      StationName" );
	out.println ( cmnt +	
	"---------eb----eb-------exxb------------------eb------exx" +
	"b----------------------e" );
	out.println ( cmnt + "EndHeader" );

	int num = 0;
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	StateCU_ClimateStation sta = null;
	for ( i=0; i<num; i++ ) {
		sta = (StateCU_ClimateStation)data_Vector.elementAt(i);
		if ( sta == null ) {
			continue;
		}

		v.removeAllElements();
		v.add(sta._id);
		// Latitude...
		if ( StateCU_Util.isMissing(sta.__latitude) ) {
			v.add("");
		}
		else {	v.add( StringUtil.formatString(sta.__latitude,"%6.2f"));
		}
		// Elevation...
		if ( StateCU_Util.isMissing(sta.__elevation) ) {
			v.add("");
		}
		else {	v.add(StringUtil.formatString(sta.__elevation,"%9.2f"));
		}
		v.add(sta.__region1);
		v.add(sta.__region2);
		v.add(sta._name);

		iline = StringUtil.formatString ( v, format);
		out.println ( iline );
	}
}

/**
Writes a Vector of StateCU_ClimateStation objects to a list file.  A header is 
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
	fields.add("Latitude");
	fields.add("Elevation");
	fields.add("Region1");
	fields.add("Region2");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_CLIMATE_STATIONS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.elementAt(i);
		names[i] = StateCU_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateCU_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	StateCU_ClimateStation cli = null;
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
			cli = (StateCU_ClimateStation)data.elementAt(i);
			
			line[0] = StringUtil.formatString(cli.getID(), 
				formats[0]).trim();
			line[1] = StringUtil.formatString(cli.getName(), 
				formats[1]).trim();
			line[2] = StringUtil.formatString(cli.getLatitude(), 
				formats[2]).trim();
			line[3] = StringUtil.formatString(cli.getElevation(), 
				formats[3]).trim();
			line[4] = StringUtil.formatString(cli.getRegion1(), 
				formats[4]).trim();
			line[5] = StringUtil.formatString(cli.getRegion2(), 
				formats[5]).trim();

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

} // End StateCU_ClimateStation
