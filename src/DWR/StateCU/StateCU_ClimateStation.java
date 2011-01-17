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
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
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
extends StateCU_Data implements StateCU_ComponentValidator {

// List data in the same order as in the StateCU documentation.

// StationID is stored in the base class ID.
// Station name is stored in the base class name.

private double __latitude = StateCU_Util.MISSING_DOUBLE;
private double __elevation = StateCU_Util.MISSING_DOUBLE;

private String __region1 = StateCU_Util.MISSING_STRING;
private String __region2 = StateCU_Util.MISSING_STRING;

private double __zh = StateCU_Util.MISSING_DOUBLE;
private double __zm = StateCU_Util.MISSING_DOUBLE;

/**
Construct a StateCU_ClimateStation instance and set to missing and empty data.
*/
public StateCU_ClimateStation()
{	super();
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
Return the height of humidity and temperature measurements, feet.
@return the height of humidity and temperature measurements, feet.
*/
public double getZh() {
	return __zh;
}

/**
Return the height of wind speed measurements, feet.
@return the height of wind speed measurements, feet.
*/
public double getZm() {
	return __zm;
}

/**
Read the StateCU climate stations file and return as a list of CUClimateStation.
@param filename filename containing CLI records.
*/
public static List<StateCU_ClimateStation> readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_ClimateStation.readStateCUFile";
	String iline = null;
	List v = new Vector ( 10 );
	List<StateCU_ClimateStation> sta_Vector = new Vector ( 100 ); // Data to return.
	int format_0[] = {
		StringUtil.TYPE_STRING, // station id
		StringUtil.TYPE_STRING, // latitude
		StringUtil.TYPE_STRING, // elevation
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_STRING, // region1
		StringUtil.TYPE_STRING, // region2
		StringUtil.TYPE_SPACE,
		StringUtil.TYPE_STRING, // station name
		StringUtil.TYPE_STRING, // zh
		StringUtil.TYPE_STRING }; // zm

	int format_0w[] = {
		12,	// station id
		6, // latitude
		9, // elevation
		2,
		20, // region1
		8, // region2
		2,
		24, // station name
		8, // zh
		8}; // zm
	StateCU_ClimateStation sta = null;
	BufferedReader in = null;

	try {
		Message.printStatus ( 2, rtn, "Reading StateCU climate station file: \"" + filename + "\"" );
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
			sta.setID ( ((String)v.get(0)).trim() ); 
			string = ((String)v.get(1)).trim();
			if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
				sta.setLatitude ( StringUtil.atod(string) );
			}
			string = ((String)v.get(2)).trim();
			if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
				sta.setElevation ( StringUtil.atod(string) );
			}
			sta.setRegion1 ( ((String)v.get(3)).trim() ); 
			sta.setRegion2 ( ((String)v.get(4)).trim() ); 
			sta.setName ( ((String)v.get(5)).trim() );
			string = ((String)v.get(6)).trim();
			if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
				sta.setZh ( Double.parseDouble(string) );
			}
			string = ((String)v.get(7)).trim();
			if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
				sta.setZm ( Double.parseDouble(string) );
			}
	
			// add the StateCU_ClimateStation to the list...
			sta_Vector.add ( sta );
		}
	}
	finally {
		if ( in != null ) {
			in.close();
		}
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
	__zh = station.__zh;
	__zm = station.__zm;

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
Set the height of humidity and temperature measurements.
@param zh height of humidity and temperature measurements, feet
*/
public void setZh(double zh) {
	__zh = zh;
}

/**
Set the height of wind speed measurement.
@param zm height of wind speed measurement, feet
*/
public void setZm(double zm) {
	__zm = zm;
}

/**
Performs specific data checks and returns a list of data that failed the data checks.
@param dataset StateCU dataset currently in memory.
@return Validation results.
 */
public StateCU_ComponentValidation validateComponent( StateCU_DataSet dataset )
{	StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
	String id = getID();
	double latitude = getLatitude();
	if ( !((latitude >= -90.0) && (latitude <= 90.0)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Climate station \"" + id + "\" latitude (" +
			latitude + ") is invalid.", "Specify a latitude -90 to 90.") );
	}
	double elevation = getElevation();
	if ( !((elevation >= 0.0) && (elevation <= 15000.00)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Climate station \"" + id + "\" elevation (" +
			elevation + ") is invalid.", "Specify an elevation 0 to 15000 FT (maximum varies by location).") );
	}
	String name = getName();
	if ( (name == null) || name.trim().length() == 0 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Climate station \"" + id +
			"\" name is blank - may cause confusion.",
			"Specify the station name or use the ID for the name.") );
	}
	String region1 = getRegion1();
	if ( (region1 == null) || region1.trim().length() == 0 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Climate station \"" + id +
			"\" region1 is blank - may cause region lookups to fail for other data.",
			"Specify as county or other region indicator.") );
	}
	double zh = getZh();
	if ( !(zh >= 0.0) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Climate station \"" + id + "\" zh (" +
			zh + ") is invalid.", "Specify a zh >= 0.") );
	}
	double zm = getZm();
	if ( !(zm >= 0.0) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Climate station \"" + id + "\" zm (" +
			zm + ") is invalid.", "Specify a zm >= 0.") );
	}
	return validation;
}

/**
Write a list of StateCU_ClimateStation to a file.  The filename is adjusted
to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filenamePrev The name of the previous version of the file (for processing headers).
Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_ClimateStation to write.
@param newComments Comments to add to the top of the file.  Specify as null if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filenamePrev, String filename, List<StateCU_ClimateStation> dataList,
		List<String> newComments )
throws IOException
{	List<String> comment_str = new Vector(1);
	comment_str.add ( "#" );
	List<String> ignore_comment_str = new Vector(1);
	ignore_comment_str.add ( "#>" );
	PrintWriter out = null;
	try {
		String full_filename_prev = IOUtil.getPathUsingWorkingDir ( filenamePrev );
		String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
		out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
			newComments, comment_str, ignore_comment_str, 0 );
		if ( out == null ) {
			throw new IOException ( "Error writing to \"" + full_filename + "\"" );
		}
		writeStateCUFile ( dataList, out );
	}
	finally {
		if ( out != null ) {
			out.flush();
			out.close();
		}
	}
}

/**
Write a list of StateCU_ClimateStation to an opened file.
@param dataList A list of StateCU_ClimateStation to write.
@param out output PrintWriter.
@exception IOException if an error occurs.
*/
private static void writeStateCUFile ( List<StateCU_ClimateStation> dataList, PrintWriter out )
throws IOException
{	int i;
	String iline;
	String cmnt = "#>";
	// Missing data handled by formatting all as strings...
	String format =	"%-12.12s%6.6s%9.9s  %-20.20s%-8.8s  %-24.24s%8.8s%8.8s";
	List v = new Vector(8);	// Reuse for all output lines.

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Climate Stations File" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record format (a12,f6.2,f9.2,2x,a20,a8,2x,a24,8.2f,2.2f)" );
	out.println ( cmnt );
	out.println ( cmnt + "  StationID:  Station identifier (e.g., 3951)" );
	out.println ( cmnt + "        Lat:  Latitude (decimal degrees)" );
	out.println ( cmnt + "       Elev:  Elevation (feet)" );
	out.println ( cmnt + "    Region1:  Region1 (e.g., County)" );
	out.println ( cmnt + "    Region2:  Region2 (e.g., Hydrologic Unit Code, HUC)" );
	out.println ( cmnt + "StationName:  Station name" );
	out.println ( cmnt + "     zHumid:  Height of humidity and temperature measurements (feet, daily analysis only)" );
	out.println ( cmnt + "      zWind:  Height of wind speed measurement (feet, daily analysis only)" );
	out.println ( cmnt );
	out.println ( cmnt + " StationID  Lat   Elev            Region1      Region2        StationName         zHumid  zWind" );
	out.println ( cmnt + "---------eb----eb-------exxb------------------eb------exxb----------------------eb------eb------e" );
	out.println ( cmnt + "EndHeader" );

	int num = 0;
	if ( dataList != null ) {
		num = dataList.size();
	}
	StateCU_ClimateStation sta = null;
	for ( i=0; i<num; i++ ) {
		sta = dataList.get(i);
		if ( sta == null ) {
			continue;
		}

		v.clear();
		v.add(sta._id);
		// Latitude...
		if ( StateCU_Util.isMissing(sta.__latitude) ) {
			v.add("");
		}
		else {
			v.add( StringUtil.formatString(sta.__latitude,"%6.2f"));
		}
		// Elevation...
		if ( StateCU_Util.isMissing(sta.__elevation) ) {
			v.add("");
		}
		else {
			v.add(StringUtil.formatString(sta.__elevation,"%9.2f"));
		}
		v.add(sta.__region1);
		v.add(sta.__region2);
		v.add(sta._name);
		// zh...
		if ( StateCU_Util.isMissing(sta.__zh) ) {
			v.add("");
		}
		else {
			v.add( StringUtil.formatString(sta.__zh,"%8.2f"));
		}
		// zm...
		if ( StateCU_Util.isMissing(sta.__zm) ) {
			v.add("");
		}
		else {
			v.add( StringUtil.formatString(sta.__zm,"%8.2f"));
		}

		iline = StringUtil.formatString ( v, format);
		out.println ( iline );
	}
}

/**
Writes a list of StateCU_ClimateStation objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header@param data the Vector of objects to write.  
@param outputComments Comments to add to the header, usually the command file and database information.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update,
		List<StateCU_ClimateStation> data, List<String> outputComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector();
	fields.add("ID");
	fields.add("Name");
	fields.add("Latitude");
	fields.add("Elevation");
	fields.add("Region1");
	fields.add("Region2");
	fields.add("HeightHumidity");
	fields.add("HeightWind");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_CLIMATE_STATIONS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
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
	List<String> commentString = new Vector(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new Vector(1);
	ignoreCommentString.add ( "#>" );
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List newComments2 = null;
		if ( outputComments == null ) {
			newComments2 = new Vector();
		}
		else {
			newComments2 = new Vector(outputComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU climate stations as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names[i] + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			cli = data.get(i);
			
			line[0] = StringUtil.formatString(cli.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(cli.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(cli.getLatitude(), formats[2]).trim();
			line[3] = StringUtil.formatString(cli.getElevation(), formats[3]).trim();
			line[4] = StringUtil.formatString(cli.getRegion1(), formats[4]).trim();
			line[5] = StringUtil.formatString(cli.getRegion2(), formats[5]).trim();
			line[6] = StringUtil.formatString(cli.getZh(), formats[6]).trim();
			line[7] = StringUtil.formatString(cli.getZm(), formats[7]).trim();

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