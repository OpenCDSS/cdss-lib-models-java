// ----------------------------------------------------------------------------
// StateCU_CropPatternTS.java - base class for StateCU crop pattern time series
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-09-26	J. Thomas Sapienza, RTi	Initial version from CUTimeSeries.java
// 2002-09-30	JTS, RTi		Added the output code, added code to
//					translate missing data to 0s for doing
//					totals and percents.
// 2003-02-23	Steven A. Malers, RTi	Rework for the official StateDMI
//					release.
// 2003-06-04	SAM, RTi		Change name of class from
//					CUCropPatternTS to
//					StateCU_CropPatternTS.
//					Update to use new TS package.
// 2003-07-07	SAM, RTi		Update to use new format as per Erin
//					Wilson.  This involves using a header
//					record, number of crops, and formatting
//					more suitable for a spreadsheet.  The
//					old code is commented out in case we
//					need to support reading the old files.
//					The units have been added to the
//					constructor since they are in the file
//					header.
// 2004-02-05	SAM, RTi		Update for use with TSTool.
//					* Add readTimeSeries() and
//					  readTimeSeriesList() methods to read
//					  time series only, which can be used by
//					  TSTool and other software for pure
//					  time series analysis and manipulation.
//					* Change so data types have "CropArea-"
//					  in front.
// 2004-02-28	SAM, RTi		* Move some utility code from
//					  StateCU_Data to StateCU_Util.
// 2004-02-29	SAM, RTi		* Fix writeStateCU to use the exact
//					  format (without extra formats) so that
//					  StringUtil.formatString() does not
//					  complain.
// 2004-03-02	SAM, RTi		* Add removeAllTS() to remove all time
//					  series for the location.  This is
//					  called before resetting the time
//					  series.
//					* Add translateCropName() to deal with
//					  StateDMI input not being of the
//					  correct crop type.
// 2004-03-03	SAM, RTi		* Add translateCropName() to support
//					  StateDMI.
//					* Add removeCropName() to support
//					  StateDMI.
// 2004-03-11	SAM, RTi		* Add option to write the crop acreage
//					  in addition to the fraction.
// 2004-03-31	SAM, RTi		* Erin Wilson agreed to add the acreage
//					  in addition to the percent so make it
//					  happen - also line up the columns.
// 2004-05-17	SAM, RTi		* Add setCropAreasToZero() to simplify
//					  processing in StateDMI.
// 2004-06-02	SAM, RTi		* Add a header to the output, similar
//					  to the irrigation practice time series
//					  file.
//					* Remove commented code for old format.
// 2004-06-24	SAM, RTi		* Update translateCropName() to combine
//					  the crops if the new crop name is the
//					  same as an existing crop name.
// 2005-01-19	SAM, RTi		* Add toTSVector() utility method to
//					  simplify getting a vector of all the
//					  time series.
// 2005-02-25	SAM, RTi		* Add setTotalArea() to allow data to be
//					  reset.
//					* Add setCropArea() to simplify data
//					  reset.
// 2005-03-31	SAM, RTi		* Add overload toTSVector() method to
//					  add total time series to the raw
//					  data Vector, for use by StateDMI and
//					  other applications.  In the future,
//					  it may make sense to have this method
//					  be optionally called when reading the
//					  IPY file in TSTool, etc.
// 2005-06-03	SAM, RTI		* Add WriteOnlyTotal and overload the
//					  writeStateCUFile to take a PropList.
// 2005-06-08	SAM, RTi		* Overload readStateCUFile() to take a
//					  PropList and support a Version
//					  property.
// 2005-07-28	SAM, RTi		* When writing the area for each crop,
//					  calculate to the precision of the
//					  fraction, so that the numbers agree.
// 2005-11-17	SAM, RTi		* The start/end months and year type
//					  were not previously included.
// 2005-11-30	SAM, RTi		* Fix so that trying to set data outside
//					  the period in memory results in no
//					  action.
//					* When reading time series, the
//					  requested period was being used to
//					  initialize memory but was not being
//					  used to optimize the read start/stop.
//					  Put in a REVISIT for now to note that
//					  an improvement can be made when there
//					  is time.
// 2007-01-09	Kurt Tometich RTi
//						Added a new method isVersion10() to detect
//						whether the file being read is Version10 or
//						newer.  
//						Updated the writeVector and readStateCUFile methods
//						for version10 and newer formats.
// 2007-01-29	SAM, RTi		Review KAT's code.
// 2007-03-02	SAM, RTi		Final cleanup before release.
// 2007-03-21	SAM, RTi		Change isVersion10() to be less fragile.
// 2007-04-13	SAM, RTi		Reenable support for really old format where no
//						period of record header was used.
// 2007-05-18	SAM, RTi		Add saving parcel data for each year of
//						observations to facilitate filling later.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateCU;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Vector;

import RTi.TS.DateValueTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.TS.YearTS;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
The StateCU_CropPatternTS class is used to hold crop time series data
(acreage/area) used in StateDMI for CDS files.  Each instance has an identifier,
which will match a StateCU_Location identifier, and a list of time series for
various crops that are associated with the CU Location for a period of time.
If an average annual analysis is done, the period may consist of one zero year.
*/
public class StateCU_CropPatternTS extends StateCU_Data
{

/**
The file that is read, used to set the time series input name.
*/
private String __filename = "";

/**
The list of crop time series.  The data type for each time series is the crop type.
*/
private List __tslist = new Vector();

/**
The list of StateCU_Parcel observations, as an archive of observations to use with data filling.
These are read from HydroBase.
*/
private List __parcel_Vector = new Vector();

/**
Total acres (total of all crops) for each year in the period.  This is computed
from the time series in __tslist when crop values are set.
*/
private double [] __total_area = null;

/**
Dates for the period of record (for all time series).
*/
private DateTime __date1 = null;
private DateTime __date2 = null;

/**
Units of the data in the file, typically acres.
*/
private String __units = "";

/**
Internal data that is used to set/get data so new DateTime objects don't need to
be created each time.  Only the year is manipulated.
*/
private DateTime __temp_DateTime = new DateTime();

/**
List of crop types for the time series.  This is consistent with the data
sub-types for the time series.  The list is maintained to simplify use at output.
*/
private List __crop_name_Vector = null;

/**
Construct a new StateCU_CropPatternTS object for the specified CU Location identifier.
@param id StateCU_Location identifier.
@param date1 Starting date of period.  Specify with year 0 if an average annual data set.
@param date2 Ending date of period.  Specify with year 0 if an average annual data set.
@param units Data units for the acreage.
*/
public StateCU_CropPatternTS (	String id, DateTime date1, DateTime date2, String units )
{	this ( id, date1, date2, units, null );
}

/**
Construct a new StateCU_CropPatternTS object for the specified CU Location
identifier.  This is typically only called by readStateCUFile().
@param id StateCU_Location identifier.
@param date1 Starting date of period.  Specify with year 0 if an average annual data set.
@param date2 Ending date of period.  Specify with year 0 if an average annual data set.
@param units Data units for the acreage.
@param filename The name of the file that is being read, or null if created in memory.
*/
public StateCU_CropPatternTS ( String id, DateTime date1, DateTime date2, String units, String filename )
{	super();
	_id = id;
	__tslist = new Vector();
	__crop_name_Vector = new Vector();
	__units = units;
	__filename = filename;
	if ( (date1 == null) || (date2 == null) ) {
		// Assume an average condition and use year 0...
		__date1 = new DateTime ();
		__date2 = new DateTime ();
	}
	else {
		// Use the specified dates...
		__date1 = new DateTime ( date1 );
		__date2 = new DateTime ( date2 );
	}
	__total_area = new double[__date2.getYear() - __date1.getYear() + 1];
	for ( int i = 0; i < __total_area.length; i++ ) {
		__total_area[i] = -999.0;
	}
}

/**
Add a parcel containing observations.  This is used to store raw data (e.g., from HydroBase) so that
later filling and checks can more easily be performed.
@param parcel StateCU_Parcel to add.
*/
public void addParcel ( StateCU_Parcel parcel )
{	__parcel_Vector.add ( parcel );
	String routine = "StateCU_CropPatternTS.addParcel";
	Message.printStatus(2, routine, "Adding parcel " + parcel.toString() );
}

/**
Add another crop time series, using the period that was defined in the constructor.
@param crop_name Name of crop for this time series (only include the crop name, not the leading "CropArea-").
@param overwrite If false, the time series is only added if it does not already
exist.  If true, the time series is added regardless and replaces the existing time series.
@return the position that the time series was added in the list (zero reference).
@exception Exception if there is an error adding the time series.
*/
public YearTS addTS ( String crop_name, boolean overwrite )
{	int pos = indexOf ( crop_name );
	YearTS yts = null;
	if ( (pos < 0) || overwrite ) {
		yts = new YearTS();
		try {
			TSIdent tsident = new TSIdent ( _id, "StateCU", "CropArea-" + crop_name, "Year", "" );
			yts.setIdentifier ( tsident );
			yts.getIdentifier().setInputType ( "StateCU" );
			if ( __filename != null ) {
				yts.getIdentifier().setInputName ( __filename );
			}
		}
		catch ( Exception e ) {
			// This should NOT happen because the TSID is being controlled...
			Message.printWarning ( 2, "StateCU_CropPatternTS.addTS",
			"Error adding time series for \"" + crop_name + "\"" );
		}
		yts.setDataUnits ( __units );
		yts.setDataUnitsOriginal ( __units );
		yts.setDescription ( _id + " " + crop_name + " crop area" );
		yts.setDate1(new DateTime(__date1));
		yts.setDate2(new DateTime(__date2));
		yts.setDate1Original(new DateTime(__date1));
		yts.setDate2Original(new DateTime(__date2));
		yts.allocateDataSpace();
	}
	if ( pos < 0 ) {
		pos = __tslist.size();
		__tslist.add ( yts );
		__crop_name_Vector.add ( crop_name );
	}
	else {
		__tslist.set ( pos, yts );
		__crop_name_Vector.set ( pos, crop_name );
	} 
	//Message.printStatus ( 1, "", "SAMX Adding time series for " + _id +
		//" " + crop_name + " for " + __date1 + " to " + __date2 +
		//" at " + pos );
	return yts;
}

/**
Get the crop acreage for the given year.
@return the crop acreage for the given year.  Return -999.0 if the crop is not
found or the requested year is outside the data period.
@param crop_name Name of the crop, only the crop name without the leading "CropArea-".
@param year Year to retrieve data.
@param return_fraction If true, return the acreage as a fraction (0.0 to 1.0) of
the total.  If false, return the acreage.
*/
public double getCropArea (String crop_name, int year, boolean return_fraction)
{	int pos = indexOf ( crop_name );
	if ( pos < 0 ) {
		return -999.0;
	}
	if ( (year < __date1.getYear()) || (year > __date2.getYear()) ) {
		return -999.0;
	}
	YearTS yts = (YearTS)__tslist.get(pos);
	__temp_DateTime.setYear ( year );
	if ( return_fraction ) {
		// Need to consider a total that is zero or missing..
		double total_area = __total_area[year - __date1.getYear()];
		if ( total_area < 0.0 ) {
			// Missing...
			return -999.0;
		}
		else if ( total_area == 0.0 ) {
			return 0.0;
		}
		else {
			// Total is an actual value so evaluate the specific value...
			double value = yts.getDataValue ( __temp_DateTime );
			if ( value < 0.0 ) {
				// Missing...
				return -999.0;
			}
			else {
				return (value/total_area);
			}
		}
	}
	else {
		// Will return missing value if that is what it is...
		return yts.getDataValue ( __temp_DateTime );
	}
}

/**
Return the list of crops for this CU Location.
*/
public List getCropNames ()
{	return __crop_name_Vector;
}

/**
Return the list of distinct crop names for a Vector of StateCU_CropPatternTS.
The list will be sorted alphabetically.
@param dataList A Vector of StateCU_CropPatternTS to process.
@return a Vector of distinct crop names, determined from data_Vector.  A
non-null Vector is guaranteed, but may be empty.
*/
public static List getCropNames ( List dataList )
{	List v = new Vector ( 10 );
	int size = 0;
	StateCU_CropPatternTS cds = null;
	if ( dataList != null ) {
		size = dataList.size();
	}
	List crop_names = null;
	String crop_name;
	int vsize = 0;
	int ncrops, j, k;
	boolean found;
	// Loop through StateCU_CropPatternTS instances...
	for ( int i = 0; i < size; i++ ) {
		cds = (StateCU_CropPatternTS)dataList.get(i);
		if ( cds == null ) {
			continue;
		}
		// Get the crops from the StateCU_CropPatternTS instance...
		crop_names = cds.getCropNames();
		ncrops = 0;
		if ( crop_names != null ) {
			ncrops = crop_names.size();
		}
		// Loop through each crop and add to the main Vector if it is not already in that list...
		for ( j = 0; j < ncrops; j++ ) {
			crop_name = (String)crop_names.get(j);
			vsize = v.size();
			found = false;
			for ( k = 0; k < vsize; k++ ) {
				if ( crop_name.equalsIgnoreCase((String)v.get(k)) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				// Add the name...
				v.add ( crop_name );
			}
		}
	}

	// Alphabetize...

	if ( v.size() == 0 ) {
		return new Vector();
	}
	else {
		return ( StringUtil.sortStringList ( v ) );
	}
}

/**
Return the time series for the matching crop.
@return the time series for the matching crop, or null if the crop does not have a time series.
@param crop_name The name of a crop to check for (just the crop name, without the leading "CropArea-").
*/
public YearTS getCropPatternTS ( String crop_name )
{	int pos = indexOf ( crop_name );
	if ( pos < 0 ) {
		return null;
	}
	else {
		return (YearTS)__tslist.get(pos);
	}
}

/**
Returns __date1.
@return __date1
*/
public DateTime getDate1()
{	return __date1;
}

/**
Returns __date2.
@return __date2.
*/
public DateTime getDate2()
{	return __date2;
}

/**
Return the parcels for a requested year and crop type.  These values can be used in data filling.
@param year Parcel year of interest or <= number if all years should be returned.
@param crop Crop type of interest or null if all crops should be returned.
@return the list of StateCU_Parcel for a year
*/
public List getParcelListForYearAndCropName ( int year, String crop )
{	List parcels = new Vector();
	int size = __parcel_Vector.size();
	StateCU_Parcel parcel;
	for ( int i = 0; i < size; i++ ) {
		parcel = (StateCU_Parcel)__parcel_Vector.get(i);
		if ( (year > 0) && (parcel.getYear() != year) ) {
			// Requested year does not match.
			continue;
		}
		if ( (crop != null) && !crop.equalsIgnoreCase(parcel.getCrop()) ) {
			continue;
		}
		// Criteria are met
		parcels.add ( parcel );
	}
	return parcels;
}

/**
Return the total area (acres) for a year.
@return the total area (acres) for a year or -999.0 if the year is outside the period of record.
*/
public double getTotalArea ( int year )
{	if ( (year < __date1.getYear()) || (year > __date2.getYear()) ) {
		return -999.0;
	}
	return __total_area[year - __date1.getYear()];
}

/**
Return the data units for the time series.
@return the data units for the time series.
*/
public String getUnits()
{	return __units;
}

/**
Determine the index of a crop time series within the list based on the crop name.
@param crop_name Crop to search for (without leading "CropArea-").
@return index within the list (zero referenced) or -1 if not found.
*/
public int indexOf ( String crop_name )
{	int size = __tslist.size();
	YearTS yts = null;
	for (int i = 0; i < size; i++) {
		yts = (YearTS)__tslist.get(i);
		if(yts.getDataType().equalsIgnoreCase("CropArea-"+crop_name)){
			return i;
		}
	}
	return -1;
}

/**
Determine whether a StateCU file is a crop pattern time series file.
Currently the only check is to see if the file has a "cds" extension.
@param filename Name of file to examine.
@return true if the file is crop pattern time series file, false otherwise.
*/
public static boolean isCropPatternTSFile ( String filename )
{	String ext = IOUtil.getFileExtension ( filename );
	if ( ext.equalsIgnoreCase("cds") ) {
		return true;
	}
	return false;
}

/**
Checks for the period in the header by reading the first non-comment line.
If the first 2 characters are spaces, it is assumed that a period header is present.
@param filename Absolute path to filename to check.
@return true if the file includes a period in the header, false if not.
@throws IOException if the file cannot be opened.
 */
private static boolean isPeriodInHeader( String filename ) throws IOException
{
	String fname = filename;
	String line = "";
	BufferedReader input = null;
	
	// Read the StateCU file.  Only read the first non-comment line 
	input = new BufferedReader ( new FileReader (fname));
	while ( (line = input.readLine()) != null ) {
		// check for comments
		if (line.startsWith("#") || line.trim().length()==0 ){
			continue;
		}
		// Not a comment so break out of loop...
		break;
	}
	// The last line read above should be the header line with the period, or the first line of data.
		
	boolean period_in_header = false;
	if ( (line.length() > 2) && line.substring(0,2).equals("  ")) {
		// Assume period in header
		period_in_header = true;
	}
	else {
		// Assume no period in header
		period_in_header = false;
	}

	input.close();
	return period_in_header;
}

/**
Checks for version 10 by reading the file and checking the length of the first
data record.  The total length for version 12+ is 55 characters and for version
10 is 45.  Therefore, a data record length of 50 is considered version 12 (not version 10).
@param filename Name of file to check.
@return true if the file is version 10, false if not (version 12+).
@throws IOException
 */
private static boolean isVersion_10( String filename ) throws IOException
{
	String fname = filename;
	String line = "";
	BufferedReader input = null;
	
	// Read the StateCU file.  Only read the first line 
	// This is enough to know if it is version 10
	input = new BufferedReader ( new FileReader (fname));
	while ( (line = input.readLine()) != null ) {
		// check for comments
		if (line.startsWith("#") || line.trim().length()==0 ){
			continue;
		}
		// Not a comment so break out of loop...
		break;
	}
	// The last line read above should be the header line with the
	// period.  Ignore it because there is some inconsistency in formatting
	// and read another line.  For really old files, a header line will not be used.
	if ( isPeriodInHeader(filename) ) {
		line = input.readLine();
	}
		
	boolean version10 = false;
	if ( line.length() < 50 ) {
		// Assume version 10
		version10 = true;
	}
	else {
		// Assume version 12+ (not version 10)
		version10 = false;
	}

	input.close();
	return version10;
}

/**
Read the StateCU CDS file and return as a Vector of StateCU_CropPattern.
@param filename filename containing CDS records.
@param date1_req Requested start of period.
@param date2_req Requested end of period.
*/
public static List readStateCUFile ( String filename, DateTime date1_req, DateTime date2_req )
throws Exception
{	return readStateCUFile ( filename, date1_req, date2_req, null );
}

/**
Read the StateCU CDS file and return as a Vector of StateCU_CropPattern.
@param filename filename containing CDS records.
@param date1_req Requested start of period.
@param date2_req Requested end of period.
@param props properties to control the read, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>Version</b></td>
<td>If "10", the StateCU version 10 format file will be read.  Otherwise, the
most current format will be read.  This is used for backward compatibility.</td>
<td>True</td>
</tr>

<tr>
<td><b>ReadDataFrom</b></td>
<td>If "CropArea", crop area time series will be read from the individual acreage
values - this should be used for newer software.  If "TotalAndCropFraction", the
individual crops will be computed from the total multiplied by the fractions - this
should only be used when processing older files.</td>
<td>True</td>
</tr>

<tr>
<td><b>AutoAdjust</b></td>
<td>If "True", automatically adjust the following information when reading the file:
<ol>
<li>	Crop data types with "." - replace with "-".</li>
</ol>
</td>
<td>True</td>
</tr>
</table>
*/
public static List readStateCUFile ( String filename, DateTime date1_req, DateTime date2_req, PropList props )
throws Exception
{	String routine = "StateCU_CropPatternTS.readStateCUFile";
	String iline = null;
	List v = new Vector ( 5 );
	List cupat_Vector = new Vector ( 100 );	// Data to return.
	
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	Message.printStatus(2,routine, "Reading StateCU CDS file: " + full_filename );
	
	if ( props == null ) {
		props = new PropList ( "CDS" );
	}
	// check the version
	String Version = props.getValue ( "Version" );
	boolean version10 = false;
	// TODO SAM 2007-02-18 Evaluate phasing out property - default the check
	if ( (Version != null) && Version.equalsIgnoreCase("10") ) {
		version10 = true;
	}
	else if( isVersion_10( full_filename ) )
	{	// Automatically check for the old format.
		Message.printStatus(2, routine, "Format of file was found to be" +
				" version 10.  Will use old format for reading.");
		version10 = true;
	}
	
	// If early versions (earlier than 10?), the period is not in the header
	// so determine by reading the first and last part of the file...
	// TODO SAM 2007-03-04 Need to figure out what version the addition
	// of period to the header occurred.  It seems to have been in version 10 and later?
	boolean period_in_header = isPeriodInHeader ( full_filename );
	String AutoAdjust = props.getValue ( "AutoAdjust" );
	boolean AutoAdjust_boolean = false;
	if ( (AutoAdjust != null) && AutoAdjust.equalsIgnoreCase("True") ) {
		AutoAdjust_boolean = true;
	}
	
	// Check how to read the data...
	String ReadDataFrom = props.getValue ( "ReadDataFrom" );
	boolean ReadDataFrom_CropArea_boolean = true;	// Default
	if ( (ReadDataFrom != null) && ReadDataFrom.equalsIgnoreCase("TotalAndCropFraction")) {
		ReadDataFrom_CropArea_boolean = false;
	}

	// If an older file version, automatically adjust to the old setting...
	if ( version10 || !period_in_header ) {
		if ( ReadDataFrom_CropArea_boolean ) {
			ReadDataFrom_CropArea_boolean = false;
		}
	}
	if ( ReadDataFrom_CropArea_boolean ) {
		Message.printStatus( 2, routine,
		"Reading crop area time series from individual time series (NOT fraction).");
	}
	else {
		Message.printStatus( 2, routine,
		"Reading crop area time series from total acres and fraction.");
	}
	
	// Format specifiers for defaults (version 12).
	int format_0[] = {
				StringUtil.TYPE_STRING,	// year
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// CU Location ID
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// Total area
				StringUtil.TYPE_STRING};// Number of crops

	int format_0w[] = {
			4,	// Year
			1,
			12,	// CU Location ID
			18,
			10,	// Total area
			10 };	// Number of crops
	
	if ( !period_in_header ) {
		// Use the even older format (what version?)...
			format_0 = new int [6];
			format_0[0] = StringUtil.TYPE_STRING;
			format_0[1] = StringUtil.TYPE_SPACE;
			format_0[2] = StringUtil.TYPE_STRING;
			format_0[3] = StringUtil.TYPE_SPACE;
			format_0[4] = StringUtil.TYPE_STRING;
			//format_0[5] = StringUtil.TYPE_STRING;

			format_0w = new int[6];
			format_0w[0] = 4;	// Year
			format_0w[1] = 1;	// Space
			format_0w[2] = 12;	// CU Location ID
			format_0w[3] = 3;	// Space
			format_0w[4] = 10;	// Total area
			//format_0w[5] = 10;	// Number of crops (not in data)
	}
	else if ( version10 ) {
		// Use the older format...
		format_0 = new int [6];
		format_0[0] = StringUtil.TYPE_STRING;
		format_0[1] = StringUtil.TYPE_SPACE;
		format_0[2] = StringUtil.TYPE_STRING;
		format_0[3] = StringUtil.TYPE_SPACE;
		format_0[4] = StringUtil.TYPE_STRING;
		format_0[5] = StringUtil.TYPE_STRING;

		format_0w = new int[6];
		format_0w[0] = 4;	// Year
		format_0w[1] = 1;	// Space
		format_0w[2] = 12;	// CU Location ID
		format_0w[3] = 8;	// Space
		format_0w[4] = 10;	// Total area
		format_0w[5] = 10;	// Number of crops
	}

	// Data records within a location.
	// Newest (version 12) format specifiers for crop record, including the crop fraction and area...
	int format_1[] = {	
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// Crop name
				StringUtil.TYPE_STRING, // Crop fraction
				StringUtil.TYPE_STRING };	// Crop area
	int format_1w[] = {
			5,	// Spaces
			30,	// Crop name
			10,	// Crop fraction
			10 };	// Crop area
	if ( !ReadDataFrom_CropArea_boolean ) {
		// Read the crop area from the total and fraction (no need to read crop area).
		// This was the normal way of doing it until 2007-10-02
		format_1 = new int[3];
		format_1[0] = StringUtil.TYPE_SPACE;
		format_1[1] = StringUtil.TYPE_STRING;	// Crop name
		format_1[2] = StringUtil.TYPE_STRING;	// Crop fraction
		
		format_1w = new int[3];
		format_1w[0] = 5;	// Spaces
		format_1w[1] = 30;	// Crop name
		format_1w[2] = 10;	// Crop fraction
	}

	if ( !period_in_header ) {
		// Even older format
		format_1w[0] = 4;
		format_1w[1] = 20;
		format_1w[2] = 10;
	}
	else if ( version10 ) {
		// Use the older format...
		format_1w[0] = 5;
		format_1w[1] = 20;
		format_1w[2] = 10;
	}
	
	StateCU_CropPatternTS cupat = null;
	BufferedReader in = null;

	DateTime date1 = null, date2 = null;
	DateTime date1_file = null;
	DateTime date2_file = null;
	String units_file = "";
	int	year1 = -1,		// First year in the file
		year = 0;		// Year being processed
	//TODO SAM 2007-02-18 Evaluate why requested years are not used.
	//	year1_req = -1,		// Requested first year to process
					// (null requested date = read all)
	//	year2_req = -1;		// Requested last year to process

	if ( !period_in_header ) {
		// Version 10 and older files do not have header information
		// with the period for the time series.  Therefore, grab a
		// reasonable amount of the start and end of the file - then
		// read lines (broken by line breaks) until the last data
		// line is encountered...
	
		RandomAccessFile ra = new RandomAccessFile(full_filename, "r" );

		// Get the start of the file...

		byte[] b = new byte[5000];
		ra.read ( b );
		String string = null;
		String bs = new String ( b );
		List v2 = StringUtil.breakStringList ( bs, "\n\r", StringUtil.DELIM_SKIP_BLANKS );
		// Loop through and figure out the first date.
		int size = v2.size();
		String date1_string = null;
		List tokens = null;
		for ( int i = 0; i < size; i++ ) {
			string = ((String)v2.get(i)).trim();
			if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == ' ') ) {
				continue;
			}
			tokens = StringUtil.breakStringList( string, " \t", StringUtil.DELIM_SKIP_BLANKS );
			date1_string = (String)tokens.get(0);
			// Check for reasonable dates...
			if ( StringUtil.isInteger(date1_string) && (StringUtil.atoi(date1_string) < 2050) ) {
				break;
			}
		}
		date1_file = DateTime.parse ( date1_string );

		// Get the end of the file...
		long length = ra.length();
		// Skip to 5000 bytes from the end.  This should get some actual
		// data lines.  Save in a temporary array in memory.
		if ( length >= 5000 ) {
			ra.seek ( length - 5000 );
		}
		ra.read ( b );
		ra.close();
		ra = null;
		// Now break the bytes into records...
		bs = new String ( b );
		v2 = StringUtil.breakStringList ( bs, "\n\r", StringUtil.DELIM_SKIP_BLANKS );
		// Loop through and figure out the last date.  Start at the
		// second record because it is likely that a complete record was not found...
		size = v2.size();
		String date2_string = null;
		for ( int i = 1; i < size; i++ ) {
			string = ((String)v2.get(i)).trim();
			if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == ' ') ) {
				continue;
			}
			tokens = StringUtil.breakStringList( string, " \t", StringUtil.DELIM_SKIP_BLANKS );
			string = (String)tokens.get(0);
			// Check for reasonable dates...
			if ( StringUtil.isInteger(string) && (StringUtil.atoi(string) < 2050) ) {
				date2_string = string;
			}
		}
		v2 = null;
		bs = null;
		date2_file = DateTime.parse ( date2_string );

		Message.printStatus ( 2, routine,
		"No period in file header.  Period determined from data to be " +
		date1_file + " to " + date2_file );

		if ( date1_req != null ) {
			date1 = date1_req;
			//year1_req = date1_req.getYear();
		}
		else {
			date1 = date1_file;
			//year1_req = date1_file.getYear();
		}
		if ( date2_req != null ) {
			date2 = date2_req;
			//year2_req = date2_req.getYear();
		}
		else {
			date2 = date2_file;
			//year2_req = date2_file.getYear();
		}

		year1 = date1_file.getYear();
		units_file = "ACRE";
	}

	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (full_filename));
	int ncrops = 0;
	String [] crop_names = new String[50];	// Should never exceed
	double [] crop_fractions = new double[50];	// Should never exceed
	double [] crop_areas = new double[50];	// Should never exceed
	String total_area = "";
	String culoc = "";
	int pos = 0;
	int linecount = 0;
	List tokens;
	try {
	while ( (iline = in.readLine()) != null ) {
		++linecount;
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}

		// If the dates have not been determined, do so (assume that
		// the first line is header with the period, etc.)...
		//if ( !version10 && date1_file == null ) {
		if ( date1_file == null ) {
			// Treat all as strings for initial read...
			// Header format is the following, although start and
			// end months and the year type are ignored...
			// "  Header format (i5,1x,i4,5x,i5,1x,i4,a5,a5)" );
			//"    M/YYYY        MM/YYYY UNIT  CYR"
			String format_header = "x6s4x11s4s5s5";
			tokens = StringUtil.fixedRead ( iline, format_header );
			date1_file = new DateTime ( DateTime.PRECISION_YEAR );
			date1_file.setYear ( StringUtil.atoi( ((String)tokens.get(0)).trim()) );
			year1 = date1_file.getYear();
			date2_file = new DateTime ( DateTime.PRECISION_YEAR );
			date2_file.setYear ( StringUtil.atoi( ((String)tokens.get(1)).trim()) );
			
			if(iline.startsWith("      ")) {
				// this is the newest format - have startYear, endYear, units
				List toks = StringUtil.breakStringList( iline, " ", StringUtil.DELIM_SKIP_BLANKS );
				units_file = ((String)toks.get(2));
			}
			else {
				units_file = ((String)tokens.get(2)).trim();
			}
			Message.printStatus( 2, routine, "Units from file are \"" + units_file + "\"" );
			// Year type is ignored - not used for anything - will be output as CYR when writing.

			// Set the dates for processing...
			if ( date1_req != null ) {
				date1 = date1_req;
				//year1_req = date1_req.getYear();
			}
			else {
				date1 = date1_file;
				//year1_req = date1_file.getYear();
			}
			if ( date2_req != null ) {
				date2 = date2_req;
				//year2_req = date2_req.getYear();
			}
			else {
				date2 = date2_file;
				//year2_req = date2_file.getYear();
			}
			continue;
		}

		if ( iline.charAt(0) == ' ' ) {
			// Continuation of previous Year/CULocation, indicating another crop.
			StringUtil.fixedRead ( iline, format_1, format_1w, v );
			if ( AutoAdjust_boolean ) {
				// Replace "." with "-" in the crop names.
				crop_names[ncrops] = ((String)v.get(0)).trim().replace('.','-');
			}
			else {
				// Just use the crop name as it is in the file...
				crop_names[ncrops] = ((String)v.get(0)).trim();
			}
			crop_fractions[ncrops] = StringUtil.atod(((String)v.get(1)).trim());
			if ( ReadDataFrom_CropArea_boolean ) {
				// Additionally, read the crop area...
				crop_areas[ncrops] = StringUtil.atod(((String)v.get(2)).trim());
			}
			// Increment crop count.
			++ncrops;
		}
		else {
			// Assume a new year/CULocation...
			// If the number of crops from the previous record is
			// >= 0, set the crops (if the total is zero then the total will still be set)...
			if ( (ncrops >= 0) && (cupat != null) ) {
				if ( ReadDataFrom_CropArea_boolean ) {
					cupat.setPatternUsingAreas ( year, ncrops, crop_names, crop_areas );
				}
				else {
					cupat.setPatternUsingFractions ( year, StringUtil.atod(total_area),
					ncrops, crop_names, crop_fractions );
				}
			}
			// Now process the new data...
			StringUtil.fixedRead ( iline, format_0, format_0w, v );
			year = StringUtil.atoi(((String)v.get(0)).trim());
			// TODO SAM 2005-11-30 Need to optimize code
			// Need to optimize code here to quit reading if
			// requested period is done or have not reached the
			// start of the requested period.   The problem is that
			// the code only saves data when a new year/crop is
			// detected so probably need to always try to process
			// one year before the requested period.
			culoc = ((String)v.get(1)).trim();
			total_area = ((String)v.get(2)).trim();
			// Important... ncrops from the file is not actually
			// used.  Instead the space at the beginning of lines is
			// used to indicate crops.  This works for Version 10
			// and newer formats.
			ncrops = 0;
			if ( year == year1 ) {
				// Create an object for the CU Location.  It is
				// assumed that a structure is listed for each
				// year, even if it has zero crops for a year.
				pos = StateCU_Util.indexOf(cupat_Vector, culoc);
				if ( pos >= 0 ) {
					// Should not happen!  The CU Location is apparently listed twice in the
					// first year...
					Message.printWarning ( 1, routine,
					"CU Location \"" + culoc + "\" is listed more than once in the first year." );
					cupat = (StateCU_CropPatternTS)cupat_Vector.get(pos);
				}
				else {
					cupat = new StateCU_CropPatternTS ( culoc, date1, date2, units_file, full_filename );
					cupat_Vector.add ( cupat );
					//Message.printStatus ( 1, "", "SAMX created new StateCU_CropPatternTS:" + cupat );
				}
			}
			else {
				// Find the object of interest for this CU Location so it can be used to set data
				// values...
				pos = StateCU_Util.indexOf(cupat_Vector, culoc);
				if ( pos < 0 ) {
					// Should not happen!  Apparently the CU Location was not listed in the first year...
					Message.printWarning ( 3, routine, "CU Location \"" + culoc + "\" found in year " +
						year + " but was not listed in the first year." );
					cupat = new StateCU_CropPatternTS ( culoc, date1, date2, units_file, full_filename );
					cupat_Vector.add ( cupat );
					//Message.printStatus ( 1, "", "SAMX created new StateCU_CropPatternTS:" + cupat );
				}
				else {
					cupat = (StateCU_CropPatternTS)cupat_Vector.get(pos);
				}
			}
		}
	}
	// Process the data for the last CU Location that is read...
	if ( ncrops >= 0 ) {
		if ( ReadDataFrom_CropArea_boolean ) {
			cupat.setPatternUsingAreas ( year, ncrops, crop_names, crop_areas );
		}
		else {
			cupat.setPatternUsingFractions ( year,
			StringUtil.atod(total_area), ncrops, crop_names, crop_fractions );
		}
	}
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "Error processing near line " + linecount + ": " + iline );
		Message.printWarning ( 2, routine, e );
		// Now rethrow to calling code...
		throw ( e );
	}
	if ( in != null ) {
		in.close();
	}
	return cupat_Vector;
}

/**
Read a time series from a StateCU format file, using a time series identifier.
The TSID string is specified in addition to the path to the file.  It is
expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
properly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
@param tsident_string The full identifier for the time series to
read.  This string can also be the alias for the time series in the file.
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (null to read the entire time
series).
@param date2 Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, String filename, DateTime date1, DateTime date2,
	String units, boolean read_data )
throws Exception
{	TS ts = null;
	List v = readTimeSeriesList ( tsident_string, filename, date1, date2, units, read_data );
	if ( (v != null) && (v.size() > 0) ) {
		ts = (TS)v.get(0);
	}
	return ts;
}

/**
Read all the time series from a StateCU format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated Vector of time series if successful, a NULL pointer if not.
@param fname Name of file to read.
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read.
*/
public static List readTimeSeriesList (	String fname, DateTime date1, DateTime date2,
	String units, boolean read_data)
throws Exception
{	List tslist = null;

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	tslist = readTimeSeriesList ( null, full_fname, date1, date2, units, read_data );
	// Return all the time series.
	return tslist;
}

/**
Read one or more time series from a StateCU crop pattern time series format file.
@return a Vector of time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_tsident Identifier for requested item series.  If null,
return all new time series in the vector.  If not null, return the matching time series.
@param full_filename Full path to filename, used for messages.
@param req_date1 Requested starting date to initialize period (or NULL to read the entire time series).
@param req_date2 Requested ending date to initialize period (or NULL to read the entire time series).
@param units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception Exception if there is an error reading the time series.
*/
private static List readTimeSeriesList ( String req_tsident, String full_filename, DateTime req_date1,
	DateTime req_date2, String req_units, boolean read_data )
throws Exception
{	// TODO - can optimize this later to only read one time series...
	// First read the whole file...

	List data_Vector = readStateCUFile ( full_filename, req_date1, req_date2 );
	// If all the time series are required, return all...
	int size = 0;
	if ( data_Vector != null ) {
		size = data_Vector.size();
	}
	// Guess at non-zero size (assume 1.5 crops per structure)...
	List tslist = new Vector((size + 1)*3/2);
	StateCU_CropPatternTS cds;
	int nts = 0, j;
	TSIdent tsident = null;
	if ( req_tsident != null ) {
		tsident = new TSIdent ( req_tsident );
	}
	TS ts;
	boolean include_ts = true;
	for ( int i = 0; i < size; i++ ) {
		include_ts = true;
		cds = (StateCU_CropPatternTS)data_Vector.get(i);
		if ( req_tsident != null ) {
			// Check to see if the location match...
			if ( !cds.getID().equalsIgnoreCase(	tsident.getLocation() ) ) {
				include_ts = false;
			}
		}
		if ( !include_ts ) {
			continue;
		}
		nts = cds.__tslist.size();
		for ( j = 0; j < nts; j++ ) {
			// TODO - optimize this by evaluating when reading the file...
			ts = (TS)cds.__tslist.get(j);
			if ( req_tsident != null ) {
				// Check to see if the location and data type match...
				if ( !tsident.getType().equalsIgnoreCase( ts.getDataType() ) ) {
					continue;
				}
			}
			tslist.add ( ts );
		}
	}
	return tslist;
}

/**
Recalculate the total values for the structure for each year of data.  This
method should be called if individual time series values are manipulated outside
of file read methods.  Missing will be assigned if all the component time series
are missing.  If no time series are available, the total will remain the same as
previous, as determined by other code.
*/
public void refresh ()
{	int year1 = __date1.getYear();
	int year2 = __date2.getYear();
	double total_area = 0.0;
	double area = 0.0;
	YearTS yts = null;
	int size = __tslist.size();
	for ( int year = year1; year <= year2; year++ ) {
		__temp_DateTime.setYear ( year );
		total_area = -999.0;
		for ( int i = 0; i < size; i++ ) {
			yts = (YearTS)__tslist.get(i);
			area = yts.getDataValue ( __temp_DateTime );
			if ( !yts.isDataMissing(area) ) {
				if ( total_area < 0.0 ) {
					// Total is missing so assign...
					total_area = area;
				}
				else {
					// Total is not missing so increment.
					total_area += area;
				}
			}
		}
		if ( size != 0 ) {
			__total_area[year - year1] = total_area;
		}
		// Otherwise leave the total as missing or zero as previous
	}
}

/**
Remove all the time series from the object.  This can be used, for example, when
resetting the time series to override a read.
*/
public void removeAllTS ()
{	if ( __crop_name_Vector != null ) {
		__crop_name_Vector.clear();
	}
	if ( __tslist != null ) {
		__tslist.clear();
	}
}

/**
Remove a the time series for a crop name.
@param crop_name Crop name to remove.
*/
public void removeCropName ( String crop_name )
{	int size = 0;
	if ( __crop_name_Vector != null ) {
		size = __crop_name_Vector.size();
	}
	// Remove from the crop names Vector...
	for ( int i = 0; i < size; i++ ) {
		if ( ((String)__crop_name_Vector.get(i)).equalsIgnoreCase(crop_name) ) {
			// Remove the crop name...
			__crop_name_Vector.remove(i);
			--i;
			--size;
		}
	}
	// Remove from the time series Vector...
	size = 0;
	if ( __tslist != null ) {
		size = __tslist.size();
	}
	TS ts;
	for ( int i = 0; i < size; i++ ) {
		ts = (TS)__tslist.get(i);
		if ( ts.getIdentifier().getSubType().equalsIgnoreCase(crop_name) ) {
			__tslist.remove(i);
			--i;
			--size;
		}
	}
}

/**
Set the area for a crop and year.  This method will NOT add a new crop.
Trying to set data outside the period will cause the value to be ignored (the
period will not be extended).
The total for all crops in the year will be reset to reflect the new value. 
@param crop_name Crop name.
@param year Year to set the area.
@param area Area for the crop, for the given year.
@exception Exception if there is an error setting the data (e.g., the time
series for the crop cannot be found).
*/
public void setCropArea ( String crop_name, int year, double area )
throws Exception
{	if ( (year < __date1.getYear()) || (year > __date2.getYear()) ) {
		return;
	}
	// First find the time series...
	YearTS yts = getCropPatternTS ( crop_name );
	if ( yts == null ) {
		throw new Exception ( "Unable to find time series for \"" + crop_name + "\"" );
	}
	// Set the data value...
	__temp_DateTime.setYear ( year );
	double old_total = getTotalArea ( year );
	double old_value = yts.getDataValue ( __temp_DateTime );
	yts.setDataValue ( __temp_DateTime, area );
	// Reset the total by adjusting the old value (this performs better than
	// looping through the crop time series)...
	if ( yts.isDataMissing(old_value) || yts.isDataMissing(area) ) {
		// Set to the new value...
		__total_area[year - __date1.getYear()] = area;
	}
	else {
		// Adjust the old value...
		__total_area[year - __date1.getYear()] = old_total - old_value + area;
	}
}

/**
Set the areas for each crop to zero.  This is useful, for example, when crop
patterns are being processed from individual records and any record in a year
should cause other time series to be set to zero for the year.  A later reset
of the zero can occur without issue.  However, leaving the value as -999 may
result in unexpected filled values later.
@param year The year to set data.  If negative, all years with non-missing values are processed.
@param set_all If true, then all defined time series are set to zero.  If false,
then only missing values are set to zero.
*/
public void setCropAreasToZero ( int year, boolean set_all )
{	int size = 0;
	if ( __tslist != null ) {
		size = __tslist.size();
	}
	YearTS yts = null;
	// Default to process one year
	if ( (year < __date1.getYear()) || (year > __date2.getYear()) ) {
		// No need to process...
		return;
	}
	int year1 = year;
	int year2 = year;
	if ( year < 0 ) {
		// Process all data
		year1 = __date1.getYear();
		year2 = __date2.getYear();
	}
	// Loop through the requested period.
	for ( int iyear = year1; iyear <= year2; iyear++ ) {
		__temp_DateTime.setYear ( iyear );
		if ( size == 0 ) {
			// No time series so set the total to zero.
			__total_area[iyear - __date1.getYear()] = 0.0;
			Message.printStatus ( 2, "StateCU_CropPatternTS.setCropAreasToZero",
				"Setting " + _id + " " + iyear + " crop total to zero since no crops." );
		}
		else { // Process each time series...
			for ( int i = 0; i < size; i++ ) {
				yts = (YearTS)__tslist.get(i);
				if ( set_all || yts.isDataMissing(yts.getDataValue(__temp_DateTime)) ) {
					yts.setDataValue ( __temp_DateTime, 0.0 );
					Message.printStatus ( 2, "StateCU_CropPatternTS.setCropAreasToZero",
						"Setting " + _id + " " + iyear + " crop " + yts.getDataType() + " to zero." );
				}
			}
		}
	}
}

/**
Set the pattern for a crop for a given year by specifying the areas for each crop.
@param year Year for the crop data.
@param ncrops Number of crops that should be processed.
@param crop_names List of crops that are planted.
@param crop_areas Areas planted for each crop, acres.
*/
public void setPatternUsingAreas ( int year, int ncrops, String [] crop_names, double [] crop_areas ) 
{	if ( (year < __date1.getYear()) || (year > __date2.getYear()) ) {
		return;
	}
	__temp_DateTime.setYear ( year );
	int size = 0;
	if ( crop_names != null ) {
		size = crop_names.length;
	}
	if ( ncrops < size ) {
		size = ncrops;
	}
	double total_area = -999.0;
	if ( ncrops == 0 ) {
		// No crops.
		total_area = 0.0;
	}
	for ( int i = 0; i < size; i++ ) {
		int pos = indexOf ( crop_names[i] );
		YearTS yts = null;
		if ( pos < 0 ) {
			// Add a new time series...
			yts = addTS ( crop_names[i], true );
		}
		else if ( pos >= 0 ) {
			yts = (YearTS)__tslist.get(pos);
		}
		yts.setDataValue ( __temp_DateTime, crop_areas[i] );
		// FIXME SAM 2007-10-03 Need to consolidate into refresh(year) and reuse code
		if ( total_area < 0.0 ) {
			// Set the value (ok even if crop area is missing)...
			total_area = crop_areas[i];
		}
		else {
			// Add the value (but only if not missing)...
			if ( crop_areas[i] >= 0.0 ) {
				total_area += crop_areas[i];
			}
		}
	}
	__total_area[year - __date1.getYear()] = total_area;
}

/**
Set the pattern for crops for a given year by specifying a total area and
distribution in fractions.  If the year is outside the period, then no action occurs.
@param year Year for the crop data.
@param ncrops Number of crops that should be processed.
@param total_area Total area that is cultivated (acres).
@param crop_names List of crops that are planted.
@param crop_fractions Fractions for each crop (0.0 to 1.0).
*/
public void setPatternUsingFractions ( int year, double total_area, int ncrops, String [] crop_names,
	double [] crop_fractions )
{	__temp_DateTime.setYear ( year );
	if ( (year < __date1.getYear()) || (year > __date2.getYear()) ) {
		return;
	}
	int size = 0;
	if ( crop_names != null ) {
		size = crop_names.length;
	}
	if ( ncrops < size ) {
		size = ncrops;
	}
	//Message.printStatus ( 1, "", "SAMX: Setting " + year + " to " + total_area + " date1=" + __date1 );
	__total_area[year - __date1.getYear()] = total_area;
	YearTS yts = null;
	for ( int i = 0; i < size; i++ ) {
		int pos = indexOf ( crop_names[i] );
		if ( pos < 0 ) {
			// Add a new time series...
			yts = addTS ( crop_names[i], true );
		}
		else if ( pos >= 0 ) {
			yts = (YearTS)__tslist.get(pos);
		}
		yts.setDataValue (__temp_DateTime,total_area*crop_fractions[i]);
	}
}

/**
Set the total crop area.  This will adjust the areas of each crop
proportionally, using the initial fractions.  This method is usually only called
when data are being reset based on some knowledge of the total acres.
If the old total is zero, all the crops will be zero to zero area.  This method
should NOT be called by other methods that result in a call to this method or
an infinite loop will occur.
@param year Year to set the total area.
@param total_area Total area for the CU location.
@exception Exception If there is no initial crop data defined.
*/
public void setTotalArea ( int year, double total_area )
throws Exception
{	double old_total_area = getTotalArea ( year );
	int ncrops = __crop_name_Vector.size();
	if ( old_total_area <= 0.0 ) {
		// TODO SAM 2007-06-20 Evaluate how to handle when all crops are zero or missing.
		if ( ncrops > 0 ) {
			// Do not have crop data to prorate...
			Message.printWarning ( 2, "StateCU_CropPatternTS.setTotalArea",
				"No initial crop data for \"" + _id + "\".  Cannot prorate crops to new total " +
			StringUtil.formatString(total_area,"%.3f") + ".");
			throw new Exception ( "Unable to set total area for \"" + _id + "\"" );
		}
		else {
			// No previous crops so just set the total (probably zero).
			__total_area[year - __date1.getYear()] = total_area;
		}
	}
	else {
		// Modify the existing areas by the factor...
		double factor = total_area/old_total_area;
		// Loop through the crops...
		String crop_name;
		double area;
		for ( int i = 0; i < ncrops; i++ ) {
			crop_name = (String)__crop_name_Vector.get(i);
			area = getCropArea ( crop_name, year, false );
			setCropArea ( crop_name, year, area*factor );
		}
	}
}

/**
Return a string representation of this object (the crop name list as a string).
@return a string representation of this object.
*/
public String toString()
{	return _id + " " + __crop_name_Vector;
}

/**
Return a list containing all the time series in the data list.  Only the raw
time series are returned.  Use the overloaded version to also return total time series.
@return a list containing all the time series in the data list.
@param dataList A list of StateCU_CropPatternTS.
*/
public static List toTSList ( List dataList )
{	return toTSList ( dataList, false, false, null, null );
}

/**
Return a list containing all the time series in the data list.
Optionally, process the time series in the instance and add total time series
by location and for the entire data set.
This is a performance hit but is useful for summarizing the data.  Any non-zero
value in the individual time series will result in a value in the total.
Missing for all time series will result in missing in the total.  The period for
the totals is the overall period from all StateCU_CropPatternTS being processed.
@return a list containing all the time series in the data list.
@param dataList A list of CropPatternTS.
@param include_location_totals If true, include totals for each location, equal
to the sum of the acreage for all crops.
@param include_dataset_totals If true, include totals for the entire data set,
equal to the sum of the acreage for all locations, by crop, and in total.
@param dataset_location A string used as the location for the data set totals.
If not specified, "DataSet" will be used.  A non-null value should be supplied,
in particular, if the totals for different data sets will be graphed or manipulated.
@param dataset_datasource Data source to be used for the total time series.
If not specified, "StateCU" will be used.
*/
public static List toTSList ( List dataList, boolean include_location_totals,
	boolean include_dataset_totals, String dataset_location, String dataset_datasource )
{	String routine = "StateCU_CropPatternTS.toTSVector";
	List tslist = new Vector();
	int size = 0;
	if ( dataList != null ) {
		size = dataList.size();
	}
	StateCU_CropPatternTS cds = null;
	List distinct_crop_names = null;	// For data set totals.
	String crop_name, crop_name2;		// Single crop name.
	int ndistinct_crops = 0;		// For data set totals.
	DateTime start_DateTime = null,
		end_DateTime = null, date;	// To allocate new time series.
	int end_year = 0, start_year = 0, year;	// For data set totals.
	YearTS yts = null, yts2;		// For data set totals.
	String units = "";			// Units for new time series.
	List dataset_ts_Vector = null;	// List of data set total time series.
	int j, k, nts;
	if ( include_dataset_totals ) {
		// Get a list of unique crops in the time series list...
		distinct_crop_names = getCropNames ( dataList );
		// Add for the total...
		distinct_crop_names.add ( "AllCrops" );
		ndistinct_crops = distinct_crop_names.size();
		// Set the data set location if not provided...
		if ( (dataset_location == null) || (dataset_location.length() == 0) ) {
			dataset_location = "DataSet";
		}
		if ( (dataset_datasource == null) || (dataset_datasource.length() == 0) ) {
			dataset_datasource = "StateCU";
		}
		// Determine the period to use for new time series...
		for ( int i = 0; i < size; i++ ) {
			cds = (StateCU_CropPatternTS)dataList.get(i);
			date = cds.getDate1();
			if ( (start_DateTime == null) || date.lessThan(start_DateTime) ) {
				start_DateTime = new DateTime ( date );
			}
			start_year = start_DateTime.getYear();
			date = cds.getDate2();
			if ( (end_DateTime == null) || date.greaterThan(end_DateTime) ) {
				end_DateTime = new DateTime ( date );
			}
			end_year = end_DateTime.getYear();
			if ( cds.getUnits() != null ) {
				units = cds.getUnits();
			}
		}
		// Add a time series for each distinct crop and for the data set total...
		dataset_ts_Vector = new Vector ( ndistinct_crops );
		for ( j = 0; j < ndistinct_crops; j++ ) {
			// Add a new time series for all the distinct crops...
			crop_name = (String)distinct_crop_names.get(j);
			yts = new YearTS ();
			try {
				TSIdent tsident = new TSIdent (
				dataset_location, dataset_datasource, "CropArea-" + crop_name, "Year", "" );
				yts.setIdentifier ( tsident );
				yts.getIdentifier().setInputType ( "StateCU" );
			}
			catch ( Exception e ) {
				// This should NOT happen because the TSID is being controlled...
				Message.printWarning ( 3, routine, "Error adding time series for \"" + crop_name + "\"" );
			}
			yts.setDataUnits ( units );
			yts.setDataUnitsOriginal ( units );
			yts.setDescription ( dataset_location + " " + crop_name + " crop area" );
			yts.setDate1(new DateTime(start_DateTime));
			yts.setDate2(new DateTime(end_DateTime));
			yts.setDate1Original(new DateTime(start_DateTime));
			yts.setDate2Original(new DateTime(end_DateTime));
			yts.allocateDataSpace();
			dataset_ts_Vector.add ( yts );
		}
	}
	for ( int i = 0; i < size; i++ ) {
		cds = (StateCU_CropPatternTS)dataList.get(i);
		nts = cds.__tslist.size();
		for ( j = 0; j < nts; j++ ) {
			yts = (YearTS)cds.__tslist.get(j);
			tslist.add ( yts );
			crop_name = yts.getDataType();
			if ( include_dataset_totals ) {
				// Add to the data set total...
				for ( k = 0; k < ndistinct_crops; k++ ) {
					// Need to concatenate to compare...
					crop_name2 = "CropArea-" + (String)distinct_crop_names.get(k);
					if ( crop_name2.equalsIgnoreCase(crop_name ) ) {
						// Matching crop name - add it...
						yts2 = (YearTS)dataset_ts_Vector.get(k);
						try {
							TSUtil.add ( yts2, yts);
						}
						catch ( Exception e ) {
							Message.printWarning(3, routine, "Error adding time series." );
						}
					}
				}
				// Add to the overall total...
				yts2 = (YearTS)dataset_ts_Vector.get( ndistinct_crops - 1);
				try {
					TSUtil.add ( yts2, yts );
				}
				catch ( Exception e ) {
					Message.printWarning(3, routine, "Error adding time series." );
				}
			}
		}
		if ( include_location_totals ) {
			// Insert a new time series with the total acreage for
			// the location.  Add after the time series for the location...
			crop_name = "AllCrops";
			yts = new YearTS ();
			try {
				TSIdent tsident = new TSIdent (
				cds.getID(), dataset_datasource, "CropArea-" + crop_name, "Year", "" );
				yts.setIdentifier ( tsident );
				yts.getIdentifier().setInputType ( "StateCU" );
			}
			catch ( Exception e ) {
				// This should NOT happen because the TSID is being controlled...
				Message.printWarning ( 3, routine, "Error adding time series for \"" + crop_name + "\"" );
			}
			yts.setDataUnits ( units );
			yts.setDataUnitsOriginal ( units );
			yts.setDescription ( cds.getID() + " " + crop_name + " area" );
			yts.setDate1(new DateTime(start_DateTime));
			yts.setDate2(new DateTime(end_DateTime));
			yts.setDate1Original(new DateTime(start_DateTime));
			yts.setDate2Original(new DateTime(end_DateTime));
			yts.allocateDataSpace();
			// No need to add because the totals are maintained with
			// the data.  Just assign the values.
			for ( date = new DateTime(start_DateTime),
				year = date.getYear();
				year <= end_year; date.addYear(1), year++ ) {
				yts.setDataValue ( date, cds.__total_area[year - start_year] );
			}
			tslist.add ( yts );
		}
	}
	if ( include_dataset_totals ) {
		// Insert the time series with the total acreage for
		// the data set, by crop and overall total.  Do this after all
		// other time series have been added.  Also reset the description to be more concise.
		for ( j = 0; j < ndistinct_crops; j++ ) {
			yts = (YearTS)dataset_ts_Vector.get(j);
			yts.setDescription ( yts.getLocation() + " " + yts.getDataType() );
			tslist.add ( yts );
		}
	}
	return tslist;
}

/**
Translate the crop name from the current value to a new value.  The time series
identifiers are adjusted.  This is used by StateDMI to correct crop names in
input to those used in the DSS.  If a match of the old_crop_name is not found, then nothing occurs.
@param old_crop_name Old crop name.
@param new_crop_name New crop name to be used.
@exception Exception if there is an error (generally only if add fails when
the new name is the same as an existing name).
*/
public void translateCropName ( String old_crop_name, String new_crop_name )
throws Exception
{	String routine = "StateCU_CropPatternTS.translateCropName";
	int size = 0;
	if ( __crop_name_Vector != null ) {
		size = __crop_name_Vector.size();
	}

	// Check to see if the new crop name is the same as an old crop name...

	int existing_crop_pos = -1;	// Position matching new crop name -
					// will be >= 0 if the new name already is in use.
	for ( int i = 0; i < size; i++ ) {
		if ( ((String)__crop_name_Vector.get(i)).equalsIgnoreCase(new_crop_name) ) {
			existing_crop_pos = i;
			Message.printStatus ( 2, routine, getID() + " new crop name matches an existing crop name. " +
			"Crop data for \"" + old_crop_name + "\" will be added to existing \"" + new_crop_name + "\"" );
			break;
		}
	}

	// Reset in the crop names Vector...

	int old_crop_pos = -1;	// The position of the old crop time series, before changing its name.
	boolean found = false;
	for ( int i = 0; i < size; i++ ) {
		if ( ((String)__crop_name_Vector.get(i)).equalsIgnoreCase(old_crop_name) ) {
			// Reset the crop name...
			found = true;
			__crop_name_Vector.set(i,new_crop_name);
			// This crop either needs to be renamed or merged with another crop of the same name...
			if ( existing_crop_pos >= 0 ) {
				old_crop_pos = i;
				break;
			}
		}
	}

	if ( !found ) {
		// No need to continue...
		return;
	}

	// Reset in the time series Vector...

	size = 0;
	if ( __tslist != null ) {
		size = __tslist.size();
	}
	TS ts = null;
	// Search for and modify the time series...
	for ( int i = 0; i < size; i++ ) {
		ts = (TS)__tslist.get(i);
		if (	ts.getIdentifier().getSubType().
			equalsIgnoreCase( old_crop_name) ) {
			// This crop needs to be renamed, but only do so if
			// not merging.  If merging keep the same name so the add comments make sense
			if ( existing_crop_pos < 0 ) {
				ts.getIdentifier().setSubType(new_crop_name);
				ts.addToGenesis ("Translated crop name from \""+
				old_crop_name + "\" to \"" + new_crop_name + "\"" );
				ts.setDescription ( ts.getLocation() + " " + new_crop_name + " crop area" );
			}
			// Only allow one rename at a time...
			break;
		}
	}

	if ( existing_crop_pos >= 0 ) {
		// Need to add the translated crop to the existing crop and
		// remove the translated crop.  The overall crop totals will
		// remain the same so their is no need to recompute the totals.
		TS ts_existing = (TS)__tslist.get(existing_crop_pos);
		TSUtil.add ( ts_existing, ts );
		__tslist.remove ( old_crop_pos );
		__crop_name_Vector.remove ( old_crop_pos );
	}
}

/**
Write a list of StateCU_CropPatternTS to a DateValue file.  The filename is
adjusted to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList list of StateCU_CropPatternTS to write.
@param new_comments Comments to add to the top of the file.  Specify as null if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeDateValueFile (	String filename_prev, String filename,
					List dataList, List new_comments )
throws Exception
{	// For now ignore the previous file and new comments.
	// Create a new Vector with the time series data...
	List tslist = toTSList ( dataList );
	// Now write using a standard DateValueTS call...
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	DateValueTS.writeTimeSeriesList ( tslist, full_filename );	
}

/**
Write a List of StateCU_CropPatternTS to a CDS file.  The filename is
adjusted to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_CropPatternTS to write.
@param new_comments Comments to add to the top of the file.  Specify as null
if no comments are available.
@param write_crop_area If true, then the acreage for each crop is shown in addition to the fractions.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename,
		List dataList, List new_comments, boolean write_crop_area )
throws IOException
{	PropList props = new PropList ( "writeStateCUFile" );
	if ( !write_crop_area ) {
		// Default is true so only set to false if specified...
		props.set ( "WriteCropArea", "False" );
	}
	writeStateCUFile ( filename_prev, filename, dataList, new_comments, null, null, props );
}

/**
Write a list of StateCU_CropPatternTS to a CDS file.  The filename is
adjusted to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_CropPatternTS to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@param write_crop_area If true, then the acreage for each crop is shown in addition to the fractions.
@param req_date1 Requested output start date.
@param req_date2 Requested output end date.
@param props Properties to control the write.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename,
		List dataList, List new_comments, DateTime req_date1, DateTime req_date2, PropList props )
throws IOException
{	List commentStr = new Vector(1);
	commentStr.add ( "#" );
	List ignoreCommentStr = new Vector(1);
	ignoreCommentStr.add ( "#>" );
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir ( filename_prev );
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
		new_comments, commentStr, ignoreCommentStr, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" + full_filename + "\"" );
	}
	writeStateCUFile ( dataList, out, req_date1, req_date2, props );
	out.flush();
	out.close();
	out = null;
}

/**
Write a list of StateCU_CropPatternTS to an opened file.
@param dataList A list of StateCU_CropPatternTS to write.
@param out output PrintWriter.
@param add_part_acres If true, then the acreage for each crop is shown in addition to the fractions.
@param req_date1 Requested output start date.
@param req_date2 Requested output end date.
@param props Properties to control the write, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>WriteCropArea</b></td>
<td>If True, the crop area is written in output for each crop, in addition to
the fraction of the total.  If False, only the faction is written.</td>
<td>True</td>
</tr>

<tr>
<td><b>WriteOnlyTotal</b></td>
<td>If True, the output for each crop is omitted, and only the total is written.
This is useful when verifying output and only the total is being checked.
</td>
<td>False</td>
</tr>

</table>
@exception IOException if an error occurs.
*/
private static void writeStateCUFile ( List dataList, PrintWriter out,
					DateTime req_date1, DateTime req_date2, PropList props )
throws IOException
{	int i;
	String iline;
	String cmnt = "#>";
	String format0 = "%-4.4s %-12.12s                  %10.10s%10.10s";
	String format1 = "     %-30.30s%10.10s";
	String rec1_format = "  Record 1 format (i4,1x,a12,18x,f10.0,i10) - for each year/CULocation.";
	String rec2_format = "  Record 2 format (5x,a30,f10.3,f10.3) - for each crop for Record 1";
	
	if ( props == null ) {
		props = new PropList ( "writeVector" );
	}
	
	boolean WriteCropArea_boolean = true; // Default
	String propval = props.getValue ( "WriteCropArea" );
	if ( (propval != null) && propval.equalsIgnoreCase("False") ) {
		WriteCropArea_boolean = false;
	}
	boolean WriteOnlyTotal_boolean = false; // Default
	propval = props.getValue ( "WriteOnlyTotal" );
	if ( (propval != null) && propval.equalsIgnoreCase("True") ) {
		WriteOnlyTotal_boolean = true;
	}
	
	if ( WriteCropArea_boolean ) {
		format1 = "     %-30.30s%10.10s%10.10s";
	}

	// setup to write as old version 10
	String Version = props.getValue ( "Version" );
	boolean version10 = false;	// Use the most current format
	if ( (Version != null) && Version.equalsIgnoreCase("10") ) {
		version10 = true;
		format0 = "%-4.4s %-12.12s        %10.10s%10.10s";
		format1 = "     %-20.20s%10.10s";
	
		// set the record format strings
		rec1_format = "  Record 1 format (i4,1x,a12,8x,f10.3,i10) - for each year/CULocation.";
		if ( WriteCropArea_boolean ) {
			rec2_format = "  Record 2 format (5x,a20,f10.3,f10.3) - for each crop for Record 1";
		}
		else {
			rec2_format = "  Record 2 format (5x,a20,f10.3) - for each crop for Record 1";
		}
		
		if ( WriteCropArea_boolean ) {
			format1 = "     %-20.20s%10.10s%10.10s";
		}
	}

	List v = new Vector(3);	// Reuse for all output lines.

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Crop Patterns (CDS) File" );
	out.println ( cmnt );
	if ( version10 ) {
		out.println ( cmnt + "  Header format (i5,1x,i4,5x,i5,1x,i4,a5,a5)" );
	}
	else {
		out.println ( cmnt + "  Header format (6x,i4,5x,6x,i4,a5,a5)" );
	}
	out.println ( cmnt );
	if ( version10 ) {
		out.println ( cmnt + "  month1           :  Beginning month of data (always 1)." );
	}
	out.println ( cmnt + "  year1            :  Beginning year of data (calendar year)." );
	if ( version10 ) {
		out.println ( cmnt + "  month2           :  Ending month of data (always 12)." );
	}
	out.println ( cmnt + "  year2            :  Ending year of data (calendar year)." );
	if ( version10) {
		out.println ( cmnt + "  units            :  Data units for crop areas." );
	}
	out.println ( cmnt + "  yeartype         :  Year type (always CYR for calendar)." );
	out.println ( cmnt );
	out.println ( cmnt + rec1_format );
	out.println ( cmnt );
	out.println ( cmnt + "  Yr            tyr:  Year for data (calendar year)." );
	out.println ( cmnt + "  CULocation    tid:  CU Location ID (e.g., structure/station).");
	out.println ( cmnt + "  TotalAcres ttacre:  Total acreage for the CU Location." );
	out.println ( cmnt + "  NCrop            :  Number of crops at location/year." );
	out.println ( cmnt );
	out.println ( cmnt + rec2_format);
	out.println ( cmnt );
	out.println ( cmnt + "  CropName    cropn:  Crop name (e.g., ALFALFA).");
	out.println ( cmnt + "  Fraction     tpct:  Decimal fraction of total acreage");
	out.println ( cmnt + "                      for the crop (0.0 to 1.0) - INFO ONLY." );
	out.println ( cmnt + "                      Equal to total/crop acres." );
	out.println ( cmnt + "                      Fractions should add to 1.0." );
	if ( WriteCropArea_boolean ) {
		out.println ( cmnt + "  Acres       acres:  Acreage for crop.");
		out.println ( cmnt + "                      Should sum to the total acres.");
	}
	out.println ( cmnt );
	if ( version10 ) {
		// Old format...
		out.println ( cmnt + "Yr  CULocation     TotalArea       NCrop" );
		out.println ( cmnt + "-exb----------exxxxxxxxb--------eb--------e" );
		if ( WriteCropArea_boolean ) {
			out.println ( cmnt + "     CropName          Fraction    Acres" );
			out.println ( cmnt + "xxxb------------------eb--------eb--------e" );
		}
		else {
			out.println ( cmnt + "     CropName          Fraction" );
			out.println ( cmnt + "xxxb------------------eb--------e" );
		}
	}
	else {
		// Current format...
		out.println ( cmnt + "Yr  CULocation                   TotalArea   NCrop" );
		out.println ( cmnt + "-exb----------exxxxxxxxxxxxxxxxxxb--------eb--------e" );
		if ( WriteCropArea_boolean ) {
			out.println ( cmnt + "     CropName                    Fraction    Acres" );
			out.println ( cmnt + "xxxb----------------------------eb--------eb--------e" );
		}
		else {
			out.println ( cmnt + "     CropName                    Fraction" );
			out.println ( cmnt + "xxxb----------------------------eb--------e" );
		}
	}
	if ( !WriteCropArea_boolean ) {
		out.println ( cmnt + "   Writing crop areas has been disabled (only fractions are shown)." );
	}
	if ( WriteOnlyTotal_boolean ) {
		out.println ( cmnt + "   Only totals for location are shown (area by crop has been disabled)." );
	}
	out.println ( cmnt + "EndHeader" );

	int num = 0;
	if ( dataList != null ) {
		num = dataList.size();
	}
	if ( num == 0 ) {
		return;
	}
	StateCU_CropPatternTS cds = null;
	// The dates are taken from the first object and are assumed to be consistent between objects...
	cds = (StateCU_CropPatternTS)dataList.get(0);
	DateTime date1 = cds.getDate1();
	if ( req_date1 != null ) {
		date1 = req_date1;
	}
	DateTime date2 = cds.getDate2();
	if ( req_date2 != null ) {
		date2 = req_date2;
	}
	String units = cds.getUnits();
	DateTime date = new DateTime(date1);
	int icrop = 0;
	int ncrops = 0;
	int year = 0;
	List crop_names = null;
	String crop_name = null;
	// Default is for current version...
	String row1_header = "      " + StringUtil.formatString(date1.getYear(),"%4d") +
		"           " +	StringUtil.formatString(date2.getYear(),"%4d") + " " +
		StringUtil.formatString(units,"%-4.4s") + " " + StringUtil.formatString("CYR","%-4.5s");
	double total_area, area, fraction;
	// Print the header...
	if( version10 )	{
		row1_header = "    1/" +
		StringUtil.formatString(date1.getYear(),"%4d") + "        12/" +
		StringUtil.formatString(date2.getYear(),"%4d") + " " +
		StringUtil.formatString(units,"%4.4s") +
		StringUtil.formatString("CYR","%4.4s");
	}
	
	out.println ( row1_header ) ;
	// Make sure that the time series are refreshed before writing.  The
	// totals are needed to calculate percentages.
	for ( i=0; i<num; i++ ) {
		cds = (StateCU_CropPatternTS)dataList.get(i);
		cds.refresh();
	}
	// Outer loop is for the time series period...
	for ( ; date.lessThanOrEqualTo(date2); date.addYear(1) ) {
		year = date.getYear();
		// Inner loop is for each StateCU_Location
		for ( i=0; i<num; i++ ) {
			cds = (StateCU_CropPatternTS)dataList.get(i);
			if ( cds == null ) {
				continue;
			}
			v.clear();
			v.add(StringUtil.formatString(date.getYear(),"%4d"));
			v.add(cds._id);
			total_area = cds.getTotalArea(year);
			v.add(StringUtil.formatString(total_area,"%10.3f"));
			crop_names = cds.getCropNames();
			ncrops = crop_names.size();
			v.add(StringUtil.formatString(ncrops,"%10d"));
			iline = StringUtil.formatString ( v, format0 );
			out.println ( iline );
			if ( WriteOnlyTotal_boolean ) {
				// Only writing the total so no need to do the following...
				continue;
			}
			// Now loop through the crops for the year and CULocation...
			//long area_sum_int = 0;
			//long fraction_sum_int = 0;
			for ( icrop = 0; icrop < ncrops; icrop++ ) {
				v.clear();
				crop_name = (String)crop_names.get(icrop);
				v.add ( crop_name );
				// Write the fraction...
				fraction=cds.getCropArea(crop_name, year, true);
				/* Allow missing as of 2007-06-14
				if ( (fraction < 0.0) ) {
					fraction = 0.0;
				}
				*/
				v.add( StringUtil.formatString(fraction,"%10.3f"));
				// Write the area...
				if ( WriteCropArea_boolean ) {
					// TODO SAM 2004-07-28 May need to evaluate whether there
					// needs to be an option of how to write the crop area.  For now, calculate to
					// 3 significant figures since that is what the fraction is...
					area = cds.getCropArea ( crop_name,	year, false );
					/* TODO SAM 2007-10-02 Old code - remove when tested out
					if ( (total_area < 0.0) || (fraction < 0.0) ) {
						area = -999.0;
					}
					else { area = total_area*
						StringUtil.atod(
						StringUtil.formatString(
						fraction,"%.3f"));
					}
					/ * Allow missing as of 2007-06-14...
					if ( (area < 0.0) ) {
						area = 0.0;
					}
					* /
					*/
					/* TODO SAM Need to make area and fraction agree to .3 precision.
					if ( icrop == (ncrops -1) ) {
						// Want to make sure that the value that is printed results in
						// a total that agrees with the overall total.  Round off the
						// last crop name acreage so that everything agrees.
						// Multiply by 1000 to get to the precision of output.
						long total_area_int = Math.round(total_area*1000.0);
						long total_area_sum = 0;
						for ( int i2 = 0; i2 < (ncrops - 1); i2++ ) {
							total_area_sum 
						}
					}
					*/
					v.add( StringUtil.formatString(area,"%10.3f"));
				}
				iline = StringUtil.formatString ( v, format1 );
				out.println ( iline );
			}
		}
	}
}

}