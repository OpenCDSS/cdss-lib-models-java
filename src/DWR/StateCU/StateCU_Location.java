//------------------------------------------------------------------------------
// StateCU_Location - class to hold StateCU Location data, compatible with
//			StateCU STR file
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2002-09-16	J. Thomas Sapienza, RTi	Initial version. 
// 2002-09-19	JTS, RTi		Region2 changed from int to String
// 2002-09-23	JTS, RTi		Added toStringForSTRFile()
// 2002-10-08	JTS, RTi		Added "filled"
// 2002-10-09	JTS, RTi		Formatting was SLIGHTLY off.
// 2002-11-06	Steven A. Malers, RTi	Review code for official software
//					release:
//					* simplify names of data members.
//					* rely on CUData base class for some
//					  data and behavior.
//					* remove code related to "fill".
//					* add writeSTRFile to streamline output.
// 2002-05-12	SAM, RTi		Change STR to StateCU in read/write
//					methods.	
// 2003-06-04	SAM, RTi		Change name of class from CULocation to
//					StateCU_Location.
// 2003-07-01	SAM, RTi		* Support the new format.  The format in
//					  the old documentation will not be
//					  supported.
// 2004-02-25	SAM, RTi		* Finalize new format based on StateCU
//					  4.35 release.
//					* Add a Vector to store aggregate
//					  information.  For now only store the
//					  identifiers but it may make sense in
//					  the future to store objects.
//					* Allow climate station data to be
//					  set dynamically because of resets.
// 2004-02-27	SAM, RTi		* Fix read code to handle climate
//					  stations on 2nd+ lines.
// 2004-02-29	SAM, RTi		* Change so setting the aggregate list
//					  also sets a date.
//					* Add isCollection().
// 2004-03-01	SAM, RTi		* Change "aggregate" to "collection"
//					  since aggregate and system are the
//					  collection types.  Using aggregate was
//					  becoming confusing.
//					* Parcel identifiers are not unique
//					  unless the division is included so add
//					  the division for the collection.  It
//					  should not vary by year.
// 2004-04-04	SAM, RTi		* Fix some justification problems in the
//					  output of numerical fields.
// 2005-01-17	J. Thomas Sapienza, RTi	* Added createBackup().
//					* Added restoreOriginal().
// 2005-03-07	SAM, RTi		* Add getCollectionType().
// 2005-03-29	SAM, RTi		* Add Elevation label to header - must
//					  have been an oversight.
// 2005-04-19	JTS, RTi		* Added writeListFile().
//   					* Added writeClimateStationListFile().
// 2005-05-24	SAM, RTi		* Update writeStateCUFile() to include
//					  properties, to allow old and new
//					  versions to be written.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// 2007-05-11	SAM, RTi		Add hasGroundwaterOnlySupply() and
//					  hasSurfaceWaterSupply().
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateCU;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Class to hold StateCU Location data for StateCU/StateDMI, compatible with the StateCU STR file.
*/
public class StateCU_Location extends StateCU_Data
implements StateCU_Component
{

/**
Types of collections.  An aggregate merges the water rights whereas
a system keeps all the water rights but just has one ID.
*/
public static String COLLECTION_TYPE_AGGREGATE = "Aggregate";
public static String COLLECTION_TYPE_SYSTEM = "System";

/**
Types of collection parts, either Ditch or Parcel
*/
public static String COLLECTION_PART_TYPE_DITCH = "Ditch";
public static String COLLECTION_PART_TYPE_PARCEL = "Parcel";

private String __collection_type = StateCU_Util.MISSING_STRING;

/**
Collection part type (see COLLECTION_PART_TYPE_*), used by DMI software.
*/
private String __collection_part_type = StateCU_Util.MISSING_STRING;

/**
The identifiers for data that are collected - null if not a collection
location.  This is actually a Vector of Vector's where the
__collection_year is the first dimension.  This is ugly but need to
use the code to see if it can be made cleaner.
*/
private List __collection_Vector = null;

/**
An array of years that correspond to the aggregate/system.  Parcel
collections can have multiple years but ditches currently only have one year.
*/
private int [] __collection_year = null;

/**
The division that corresponds to the aggregate/system.  Currently
it is expected that the same division number is assigned to all the data.
*/
private int __collection_div = StateCU_Util.MISSING_INT;


/** 
CULocation Elevation.
*/
private double __elevation = StateCU_Util.MISSING_DOUBLE;

/** 
CULocation Latitude.
*/
private double __latitude = StateCU_Util.MISSING_DOUBLE;

/**
Region 1 (e.g., County).
*/
private String __region1 = StateCU_Util.MISSING_STRING;

/**
Region 2 (e.g., HUC).
*/
private String __region2 = StateCU_Util.MISSING_STRING;

/**
Available water content (AWC).
*/
private double __awc = StateCU_Util.MISSING_DOUBLE;

/**
Orographic temperature adjustment (DEGF/1000 FT) - set to 0.0 because this
means no adjustment.  The data will be reset (not filled) if needed.  The size
is the number of climate stations.
*/
private double[] __ota;

/**
Orographic precipitation adjustment (fraction) - set to 1.0 because this
means no adjustment.  The data will be reset (not filled) if needed.  The size
is the number of climate stations.
*/
private double[] __opa;

/**
Number of stations.
*/
private String[] __climate_station_ids = null;
private double[] __precipitation_station_weights = null;
private double[] __temperature_station_weights = null;

/**
Construct a StateCU_Location instance and set to missing and empty data.
*/
public StateCU_Location()
{	super();
}

/**
Performs specific data checks and returns a list of data that failed the data checks.
@param count Index of the data vector currently being checked.
@param dataset StateCU dataset currently in memory.
@param props Extra properties to perform checks with.
@return List of invalid data.
*/
public String[] checkComponentData( int count, StateCU_DataSet dataset, PropList props ) {
	// TODO KAT 2007-04-12 Add specific checks here ...
	return null;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_Location)_original)._isClone = false;
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
	__climate_station_ids = null;
	__precipitation_station_weights = null;
	__temperature_station_weights = null;
	__ota = null;
	__opa = null;
	super.finalize();
}

/**
Return the AWC.
@return the AWC.
*/
public double getAwc()
{	return __awc;
}

/**
Return the AWC.
@return the AWC.
*/
public double getAWC()
{	return __awc;
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
Return the collection part ID list for the specific year.  For ditches, only one
aggregate/system list is currently supported so the same information is returned
regardless of the year value.  For wells, the collection is done for a specific year.
@param year The year of interest, only used for well identifiers.
@return the list of collection part IDS, or null if not defined.
*/
public List getCollectionPartIDs ( int year )
{	if ( __collection_Vector.size() == 0 ) {
		return null;
	}
	if ( __collection_part_type.equalsIgnoreCase(COLLECTION_PART_TYPE_DITCH) ) {
		// The list of part IDs will be the first and only list...
		return (List)__collection_Vector.get(0);
	}
	else if ( __collection_part_type.equalsIgnoreCase(COLLECTION_PART_TYPE_PARCEL) ) {
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
Return the collection part type, COLLECTION_PART_TYPE_DITCH or COLLECTION_PART_TYPE_PARCEL.
*/
public String getCollectionPartType()
{	return __collection_part_type;
}

/**
Return the collection type, "Aggregate", "System", or "MultiStruct".
@return the collection type, "Aggregate", "System", or "MultiStruct".
*/
public String getCollectionType()
{	return __collection_type;
}

/**
Return the array of years for the defined collections.
@return the array of years for the defined collections.
*/
public int [] getCollectionYears ()
{	return __collection_year;
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
Get the climate station identifier.
@return Climate station identifier or "" if not available.
@param pos Station index (relative to zero).
*/
public String getClimateStationID ( int pos )
{	if ( __climate_station_ids == null ) {
		return "";
	}
	if ( (pos >= 0) && (pos < __climate_station_ids.length) ) {
		return __climate_station_ids[pos];
	}
	else {
		return "";
	}
}

/**
Return the elevation.
@return the elevation.
*/
public double getElevation()
{	return __elevation;
}

/**
Return the latitude.
@return the latitude.
*/
public double getLatitude()
{	return __latitude;
}

/**
Return the number of climate stations.
@return the number of climate stations.
*/
public int getNumClimateStations()
{	if ( __climate_station_ids == null ) {
		return 0;
	}
	else {
		return __climate_station_ids.length;
	}
}

/**
Return the orographic precipitation adjustment factor.
@param pos Index (0+) for climate (precipitation) station.
@return the orographic precipitation adjustment factor.
*/
public double getOrographicPrecipitationAdjustment(int pos) {
	if ( (__opa == null) || (pos >= __opa.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __opa[pos];
	}
}

/**
Return the orographic temperature adjustment factor.
@param pos Index (0+) for climate (temperature) station.
@return the orographic temperature adjustment factor.
*/
public double getOrographicTemperatureAdjustment(int pos) {
	if ( (__ota == null) || (pos >= __ota.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __ota[pos];
	}
}

/**
Return the precipitation station weight.
@param pos Index (0+) for climate (precipitation) station.
@return the precipitation station weight.
*/
public double getPrecipitationStationWeight(int pos) {
	if ( (__precipitation_station_weights == null) || (pos >= __precipitation_station_weights.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __precipitation_station_weights[pos];
	}
}

/**
Return the temperature station weight.
@param pos Index (0+) for climate (temperature) station.
@return the temperature station weight.
*/
public double getTemperatureStationWeight(int pos) {
	if ( (__temperature_station_weights == null) || (pos >= __temperature_station_weights.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __temperature_station_weights[pos];
	}
}

/**
Return region 1.
@return region 1.
*/
public String getRegion1()
{	return __region1;
}

/**
Return region 2.
@return region 2.
*/
public String getRegion2()
{	return __region2;
}

public List getTemp() {
	return __collection_Vector;
}

/**
Indicate whether the CU Location has groundwater only supply.  This will
be the case if the location is a collection with part type of "Parcel".
*/
public boolean hasGroundwaterOnlySupply ()
{
	if ( isCollection() && getCollectionPartType().equalsIgnoreCase("Parcel")) {
		// TODO SAM 2007-05-11 Rectify part types with StateMod
		return true;
	}
	return false;
}

/**
Indicate whether the CU Location has surface water supply.  This will
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
Indicate whether the CU Location is a collection (an aggregate or system).
@return true if the CU Location is an aggregate or system.
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
Read the StateCU STR file and return as a Vector of StateCU_Location.
@param filename filename containing STR data.
*/
public static List readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_Location.readStateCUFile";
	String iline = null;
	List v = new Vector ( 8 );
	List culoc_Vector = new Vector ( 100 );	// Data to return.
	int i;
	int format_0[] = {
				StringUtil.TYPE_STRING,	// CU Location
				StringUtil.TYPE_STRING,	// Latitude
				StringUtil.TYPE_STRING,	// Elevation
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// Region 1
				StringUtil.TYPE_STRING,	// Region 2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// CU Location name
				StringUtil.TYPE_STRING,	// Num stations
				StringUtil.TYPE_STRING};// AWC
	int format_0w[] = {
				12,	// CU Location
				6,	// Latitude
				9,	// Elevation
				2,
				20,	// Region1
				8,	// Region2
				2,
				24,	// CU Location name
				4,	// Number of stations
				8 };	// AWC
	int format_1[] = {
				StringUtil.TYPE_STRING,	// Station ID
				StringUtil.TYPE_STRING,	// Temp weight
				StringUtil.TYPE_STRING,	// Precip weight
				StringUtil.TYPE_STRING,	// Orographic temperature adjustment
				StringUtil.TYPE_STRING };// Orographic precipitation adjustment
	int format_1w[] = {
				12,	// Station ID
				6,	// Temp weight
				9,	// Precip weight
				9,	// Orographic temperature adjustment
				9 };// Orographic precipitation adjustment	

	StateCU_Location culoc = null;
	BufferedReader in = null;
	Message.printStatus ( 1, rtn, "Reading StateCU Locations file: " + filename );

	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	String latitude, elevation, num_climate_stations, awc, weight, opa, ota;
	int ncli = 0;
	int vsize;	// Size of parsed token list
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}

		// Allocate new CULocation instance...
		culoc = new StateCU_Location();

		StringUtil.fixedRead ( iline, format_0, format_0w, v );
		culoc.setID ( ((String)v.get(0)).trim() ); 
		latitude = ((String)v.get(1)).trim();
		if ((latitude.length() != 0) && StringUtil.isDouble(latitude)) {
			culoc.setLatitude ( StringUtil.atod(latitude) );
		}
		elevation = ((String)v.get(2)).trim();
		if ( (elevation.length() != 0) && StringUtil.isDouble(elevation)) {
			culoc.setElevation ( StringUtil.atod(elevation) );
		}
		culoc.setRegion1 ( ((String)v.get(3)).trim() ); 
		culoc.setRegion2 ( ((String)v.get(4)).trim() ); 
		culoc.setName ( ((String)v.get(5)).trim() ); 
		num_climate_stations = ((String)v.get(6)).trim();
		if ( (num_climate_stations.length() != 0) && StringUtil.isInteger(num_climate_stations)) {
			culoc.setNumClimateStations ( StringUtil.atoi(num_climate_stations) );
		}
		awc = ((String)v.get(7)).trim();
		if ( (awc.length() != 0) && StringUtil.isDouble(awc)) {
			culoc.setAwc ( StringUtil.atod(awc) );
		}
		ncli = culoc.getNumClimateStations();
		for ( i = 0; i < ncli; i++ ) {
			iline = in.readLine();
			if ( iline == null ) {
				break;
			}
			StringUtil.fixedRead ( iline, format_1, format_1w, v );
			vsize = v.size();
			culoc.setClimateStationID ( ((String)v.get(0)).trim(), i ); 
			weight = ((String)v.get(1)).trim();
			if ( (weight.length() != 0) && StringUtil.isDouble(weight)) {
				culoc.setTemperatureStationWeight ( StringUtil.atod(weight), i );
			}
			weight = ((String)v.get(2)).trim();
			if ( (weight.length() != 0) && StringUtil.isDouble(weight)) {
				culoc.setPrecipitationStationWeight ( StringUtil.atod(weight), i );
			}
			if ( vsize > 3 ) {
				ota = ((String)v.get(3)).trim();
				if ( (ota.length() != 0) && StringUtil.isDouble(ota)) {
					culoc.setOrographicTemperatureAdjustment ( StringUtil.atod(ota), i );
				}
			}
			if ( vsize > 4 ) {
				opa = ((String)v.get(4)).trim();
				if ( (opa.length() != 0) &&	StringUtil.isDouble(opa)) {
					culoc.setOrographicPrecipitationAdjustment ( StringUtil.atod(opa), i );
				}
			}
		}

		// Add the StateCU_Location to the vector...
		culoc_Vector.add ( culoc );
	}
	if ( in != null ) {
		in.close();
	}
	return culoc_Vector;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Location loc = (StateCU_Location)_original;
	super.restoreOriginal();

	__awc = loc.__awc;
	__collection_div = loc.__collection_div;
	__collection_part_type = loc.__collection_part_type;
	__collection_type = loc.__collection_type;
	__elevation = loc.__elevation;
	__latitude = loc.__latitude;
	__region1 = loc.__region1;
	__region2 = loc.__region2;

	_isClone = false;
	_original = null;
}

/**
Set the AWC.
@param awc awc, fraction.
*/
public void setAwc ( double awc )
{	__awc = awc;
}

/**
Set the climate station identifier.
@param id Climate station identifier.
@param pos Station index (relative to zero).
*/
public void setClimateStationID ( String id, int pos )
{	if ( __climate_station_ids == null ) {
		__climate_station_ids = new String[pos + 1];
	}
	else if ( pos >= __climate_station_ids.length ) {
		// Resize the array...
		String [] temp = new String[pos + 1];
		for ( int i = 0; i < __climate_station_ids.length; i++ ) {
			temp[i] = __climate_station_ids[i];
		}
		__climate_station_ids = temp;
	}
	// Finally, assign...
	__climate_station_ids[pos] = id;
}

/**
Set the collection division.  This is needed to uniquely identify the parcels.
@param collection_div The division for the collection.
*/
public void setCollectionDiv ( int collection_div )
{	__collection_div = collection_div;
}

/**
Set the collection list for an aggregate/system.  It is assumed that the
collection applies to all years of data.
@param ids The identifiers indicating the locations to collection.
*/
public void setCollectionPartIDs ( List ids )
{	if ( __collection_Vector == null ) {
		__collection_Vector = new Vector ( 1 );
		__collection_year = new int[1];
	}
	else {
		// Remove the previous contents...
		__collection_Vector.clear();
	}
	// Now assign...
	__collection_Vector.add ( ids );
	__collection_year[0] = 0;
}

/**
Set the collection list for an aggregate/system for a specific year.  It is
assumed that the collection applies to all years of data.
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
@param collection_part_type The collection part type,
either COLLECTION_PART_TYPE_DITCH or COLLECTION_PART_TYPE_PARCEL.
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
Set the elevation.
@param elevation Elevation, feet.
*/
public void setElevation ( double elevation )
{	__elevation = elevation;
}

/**
Set the latitude.
@param latitude Latitude, decimal degrees.
*/
public void setLatitude ( double latitude )
{	__latitude = latitude;
}

/**
Set the number of climate stations.
@param num_climate_stations Number of climate stations.
*/
public void setNumClimateStations ( int num_climate_stations )
{	if ( num_climate_stations == 0 ) {
		// Clear the arrays...
		__climate_station_ids = null;
		__precipitation_station_weights = null;
		__temperature_station_weights = null;
		__ota = null;
		__opa = null;
	}
	else {
		__climate_station_ids = new String[num_climate_stations];
		__precipitation_station_weights =
		new double[num_climate_stations];
		__temperature_station_weights =new double[num_climate_stations];
		__ota = new double[num_climate_stations];
		__opa = new double[num_climate_stations];
	}
}

/**
Set the orographic precipitation adjustment for a station.
@param opa orographic precipitation adjustment.
@param pos Station index (relative to zero).
*/
public void setOrographicPrecipitationAdjustment ( double opa, int pos )
{	if ( __opa == null ) {
		__opa = new double[pos + 1];
	}
	else if ( pos >= __opa.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for(int i = 0; i < __opa.length; i++){
			temp[i] = __opa[i];
		}
		__opa = temp;
	}
	// Finally, assign...
	__opa[pos] = opa;
}

/**
Set the orographic temperature adjustment for a station.
@param ota orographic temperature adjustment.
@param pos Station index (relative to zero).
*/
public void setOrographicTemperatureAdjustment ( double ota, int pos )
{	if ( __ota == null ) {
		__ota = new double[pos + 1];
	}
	else if ( pos >= __ota.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for(int i = 0; i < __ota.length; i++){
			temp[i] = __ota[i];
		}
		__ota = temp;
	}
	// Finally, assign...
	__ota[pos] = ota;
}

/**
Set the precipitation station weight.
@param wt precipitation station weight.
@param pos Station index (relative to zero).
*/
public void setPrecipitationStationWeight ( double wt, int pos )
{	if ( __precipitation_station_weights == null ) {
		__precipitation_station_weights = new double[pos + 1];
	}
	else if ( pos >= __precipitation_station_weights.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for(int i = 0; i < __precipitation_station_weights.length; i++){
			temp[i] = __precipitation_station_weights[i];
		}
		__precipitation_station_weights = temp;
	}
	// Finally, assign...
	__precipitation_station_weights[pos] = wt;
}

/**
Set region 1.
@param region1 Region 1 (e.g., the name of a county).
*/
public void setRegion1 ( String region1 )
{	__region1 = region1;
}

/**
Set region 2.
@param region2 Region 1 (e.g., the name of a HUC).
*/
public void setRegion2 ( String region2 )
{	__region2 = region2;
}

/**
Set the temperature station weight.
@param wt temperature station weight.
@param pos Station index (relative to zero).
*/
public void setTemperatureStationWeight ( double wt, int pos )
{	if ( __temperature_station_weights == null ) {
		__temperature_station_weights = new double[pos + 1];
	}
	else if ( pos >= __temperature_station_weights.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for ( int i = 0; i < __temperature_station_weights.length; i++){
			temp[i] = __temperature_station_weights[i];
		}
		__temperature_station_weights = temp;
	}
	// Finally, assign...
	__temperature_station_weights[pos] = wt;
}

/**
Write a list of StateCU_Location to a file using default properties.  The filename is adjusted to the
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filenamePrev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_Location to write.
@param newComments Comments to add to the top of the file.  Specify as null if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filenamePrev, String filename,
					List dataList, List newComments )
throws IOException
{	writeStateCUFile ( filenamePrev, filename, dataList, newComments, null );
}

/**
Write a list of StateCU_Location to a file.  The filename is adjusted to the
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filenamePrev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_Location to write.
@param newComments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@param props Properties to control the write.  Currently only the following
property is supported:  Version=True|False.  If the version is "10", then the
file format will match that for version 10.  Otherwise, the newest format is
used.  This is useful for comparing with or regenerating old data sets.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filenamePrev, String filename,
					List dataList, List newComments, PropList props )
throws IOException
{	List commentStr = new Vector(1);
	commentStr.add ( "#" );
	List ignoreCommentStr = new Vector(1);
	ignoreCommentStr.add ( "#>" );
	PrintWriter out = null;
	String fullFilenamePrev = IOUtil.getPathUsingWorkingDir ( filenamePrev );
	String fullFilename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( fullFilenamePrev, fullFilename, 
		newComments, commentStr, ignoreCommentStr, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" + fullFilename + "\"" );
	}
	writeStateCUFile ( dataList, out, props );
	out.flush();
	out.close();
	out = null;
}

/**
Write a Vector of StateCU_Location to an opened file.
@param data_Vector A Vector of StateCU_Location to write.
@param out output PrintWriter.
@param props Properties to control the write.  See the writeStateCUFile() method for a description.
@exception IOException if an error occurs.
*/
private static void writeStateCUFile ( List data_Vector, PrintWriter out, PropList props )
throws IOException
{	int i,j;
	String cmnt = "#>";
	// Missing data handled by formatting as a string...
	// The following format works for both lines.
	String format = "%-12.12s%6.6s%9.9s  %-20.20s%-8.8s  %-24.24s%4.4s%8.8s";
	String format_version10 = "%-12.12s%6.6s%9.9s  %-20.20s%-8.8s  %-24.24s";
	String format2 = "%-12.12s%6.6s%9.9s%9.9s%9.9s";
	// Not used but indicates format before orographic adjustments were added
	//String format2_version10 = "%-12.12s%6.6s%9.9s";
	StateCU_Location cu_loc = null;
	List v = new Vector(8);	// Reuse for all output lines.

	if ( props == null ) {
		props = new PropList ( "StateCU_Location" );
	}
	String Version = props.getValue ( "Version" );
	boolean Version_10 = false;
	if ( (Version != null) && Version.equalsIgnoreCase("10") ) {
		Version_10 = true;
	}

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU CU Locations (STR) File" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record 1 format (a12,f6.2,11x,a10,10x,i8,2x,a24,i4,f8.4)");
	out.println ( cmnt );
	out.println ( cmnt + "  ID       base_id:  CU Location identifier" );
	out.println ( cmnt + "  Latitude    blat:  Latitude (decimal degrees)" );
	out.println ( cmnt + "  Elevation   elev:  Elevation (feet)" );
	out.println ( cmnt + "  Region1  ttcount:  Region1 (e.g., County)" );
	out.println ( cmnt + "  Region2    tthuc:  Region2 (e.g., Hydrologic Unit)");
	out.println ( cmnt + "                     Optional");
	out.println ( cmnt + "  Name     base_id:  CU Location name" );
	if ( !Version_10 ) {
		out.println ( cmnt + "  NCli            :  Number of climate stations" );
		out.println ( cmnt + "  AWC             :  Available water content (fraction)" );
		out.println ( cmnt );
		out.println ( cmnt + "  Record 2+ format (a12,f6.2,3f9.2)");
		out.println ( cmnt );
		out.println ( cmnt + "  ClimID          :  Climate station identifier" );
		out.println ( cmnt + "  TmpWT           :  Temperature station weight (fraction)" );
		out.println ( cmnt + "  PptWT           :  Precipitation station weight (fraction)");
		out.println ( cmnt + "                     Weights for each type should add to 1.0");
		out.println ( cmnt + "  OroTmpAdj       :  Orographic temperature station adjustment (DEGF/1000 FT)" );
		out.println ( cmnt + "  OroPptAdj       :  Orographic precipitation station adjustment (fraction)");
		out.println ( cmnt );
	}
	if ( Version_10 ) {
		out.println ( cmnt + "    ID     Lat  Elevation   Region1             Region2       Name" );
		out.println ( cmnt + "---------eb----eb-------bxxb--------exxxxxxxxxxb------exxb----------------------e" );
	}
	else {
		// Full output...
		out.println ( cmnt + "    ID     Lat  Elevation   Region1             Region2       Name               NCli  AWC" );
		out.println ( cmnt + "---------eb----eb-------bxxb--------exxxxxxxxxxb------exxb----------------------eb--eb------e" );
		out.println ( cmnt );
		out.println ( cmnt + " ClimID    TmpWT  PptWt  OroTmpAdj OroPptAdj" );
		out.println ( cmnt + "---------eb----eb-------eb-------eb-------e" );
	}
	out.println ( cmnt + "EndHeader" );

	int num = 0;
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	int numclimate = 0;
	double val;	// Generic value
	for ( i=0; i<num; i++ ) {
		cu_loc = (StateCU_Location)data_Vector.get(i);
		if ( cu_loc == null ) {
			continue;
		}

		v.clear();
		v.add(cu_loc._id);
		if ( StateCU_Util.isMissing(cu_loc.__latitude) ) {
			v.add("");
		}
		else {
			v.add(StringUtil.formatString(cu_loc.__latitude,"%6.2f"));
		}
		if ( StateCU_Util.isMissing(cu_loc.__elevation) ) {
			v.add("");
		}
		else {
			v.add( StringUtil.formatString(cu_loc.__elevation,"%9.2f"));
		}
		v.add(cu_loc.__region1);
		v.add(cu_loc.__region2);
		v.add(cu_loc._name);
		if ( !Version_10 ) {
			numclimate = cu_loc.getNumClimateStations();
			v.add( StringUtil.formatString(numclimate,"%4d"));
			if ( StateCU_Util.isMissing(cu_loc.__awc) ) {
				v.add("");
			}
			else {
				v.add( StringUtil.formatString(cu_loc.__awc,"%8.4f"));
			}
		}

		if ( Version_10 ) {
			out.println ( StringUtil.formatString ( v, format_version10) );
		}
		else {
			out.println ( StringUtil.formatString ( v, format) );
		}
		if ( !Version_10 ) {
			// Print the climate station weights.
			// If values are missing, assign reasonable defaults.
			for ( j = 0; j < numclimate; j++ ) {
				v.clear();
				v.add(StringUtil.formatString(cu_loc.getClimateStationID(j),"%-12.12s"));
				val = cu_loc.getTemperatureStationWeight(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 0.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				val = cu_loc.getPrecipitationStationWeight(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 0.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				val = cu_loc.getOrographicTemperatureAdjustment(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 0.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				val = cu_loc.getOrographicPrecipitationAdjustment(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 1.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				out.println ( StringUtil.formatString (v, format2) );
			}
		}
	}
}

/**
Writes a Vector of StateCU_Location objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter 
will be wrapped in "...".  <p>
This method also writes Climate Station and Collection data to
so if this method is called with a filename parameter of "locations.txt", 
three files may be generated:
- locations.txt
- locations_ClimateStations.txt
- locations_Collections.txt
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@return a list of files that were actually written, because this method controls all the secondary
filenames.
@param data the list of objects to write.
@param newComments comments to add to the top of the file (e.g., command file and HydroBase version). 
@throws Exception if an error occurs.
*/
public static List writeListFile(String filename, String delimiter, boolean update, List data,
		List newComments) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("ID");
	fields.add("Name");
	fields.add("Latitude");
	fields.add("Elevation");
	fields.add("Region1");
	fields.add("Region2");
	fields.add("NumClimateStations");
	fields.add("AWC");
	int fieldCount = fields.size();

	List names = new Vector(fieldCount);
	List formats = new Vector(fieldCount); 
	int comp = StateCU_DataSet.COMP_CU_LOCATIONS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.get(i);
		names.add(StateCU_Util.lookupPropValue(comp, "FieldName", s));
		formats.add(StateCU_Util.lookupPropValue(comp, "Format", s));
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	StateCU_Location loc = null;
	List commentString = new Vector(1);
	commentString.add ( "#" );
	List ignoreCommentString = new Vector(1);
	ignoreCommentString.add ("#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
	String filenameFull = IOUtil.getPathUsingWorkingDir(filename);
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
		newComments2.add(1,"StateCU location information as delimited list file.");
		newComments2.add(2,"See also the associated climate station assignment and collection files.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders( oldFile, filenameFull, 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names.get(i) + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			loc = (StateCU_Location)data.get(i);
			
			line[0] = StringUtil.formatString(loc.getID(), ((String)formats.get(0))).trim();
			line[1] = StringUtil.formatString(loc.getName(), ((String)formats.get(1))).trim();
			line[2] = StringUtil.formatString(loc.getLatitude(), ((String)formats.get(2))).trim();				
			line[3] = StringUtil.formatString(loc.getElevation(), ((String)formats.get(3))).trim();
			line[4] = StringUtil.formatString(loc.getRegion1(), ((String)formats.get(4))).trim();
			line[5] = StringUtil.formatString(loc.getRegion2(), ((String)formats.get(5))).trim();
			line[6] = StringUtil.formatString(loc.getNumClimateStations(), ((String)formats.get(6))).trim();
			line[7] = StringUtil.formatString(loc.getAwc(), ((String)formats.get(7))).trim();

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

	int lastIndex = filename.lastIndexOf(".");
	String front = filename.substring(0, lastIndex);
	String end = filename.substring((lastIndex + 1), filename.length());
	
	String climateFilename = front + "_ClimateStations." + end;
	writeClimateStationListFile(climateFilename, delimiter, update, data, newComments );

	String collectionFilename = front + "_Collections." + end;
	writeCollectionListFile(collectionFilename, delimiter, update, data, newComments );
	
	List filesWritten = new Vector();
	filesWritten.add ( new File(filenameFull) );
	filesWritten.add ( new File(climateFilename) );
	filesWritten.add ( new File(collectionFilename) );
	return filesWritten;
}

/**
Writes the climate station data from a list of StateCU_Location objects to a 
list file.  A header is printed to the top of the file, containing the commands 
used to generate the file.  Any strings in the body of the file that contain 
the field delimiter will be wrapped in "...". 
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of StateCU_Location objects to write (climate station assignments will
be extracted). 
@param newComments comments to add at the top of the file (e.g., commands, HydroBase information).
@throws Exception if an error occurs.
*/
public static void writeClimateStationListFile(String filename, String delimiter, boolean update,
	List data, List newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("LocationID");
	fields.add("StationID");
	fields.add("TempWeight");
	fields.add("PrecipWeight");
	fields.add("OrographicTempAdj");
	fields.add("OrographicPrecipAdj");
	int fieldCount = fields.size();

	List names = new Vector(fieldCount);
	List formats = new Vector(fieldCount); 
	int comp = StateCU_DataSet.COMP_CU_LOCATION_CLIMATE_STATIONS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.get(i);
		names.add( StateCU_Util.lookupPropValue(comp, "FieldName", s));
		formats.add(StateCU_Util.lookupPropValue(comp, "Format", s));
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	int k = 0;
	int num = 0;
	PrintWriter out = null;
	StateCU_Location loc = null;
	List commentString = new Vector(1);
	commentString.add ( "#" );
	List ignoreCommentString = new Vector(1);
	ignoreCommentString.add ( "#>" );
	String[] line = new String[fieldCount];
	String id = null;
	StringBuffer buffer = new StringBuffer();
	
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
		newComments2.add(1,"StateCU location climate station assignment information as delimited list file.");
		newComments2.add(2,"See also the associated location and collection files.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders(
			oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names.get(i) + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			loc = (StateCU_Location)data.get(i);
			id = loc.getID();
			num = loc.getNumClimateStations();
			
			for (j = 0; j < num; j++) {
				line[0] = StringUtil.formatString(id, ((String)formats.get(0))).trim();
				line[1] = StringUtil.formatString( loc.getClimateStationID(j), ((String)formats.get(1))).trim();
				line[2] = StringUtil.formatString( loc.getTemperatureStationWeight(j), ((String)formats.get(2))).trim();
				line[3] = StringUtil.formatString( loc.getPrecipitationStationWeight(j), ((String)formats.get(3))).trim();
				line[4] = StringUtil.formatString( loc.getOrographicTemperatureAdjustment(j), ((String)formats.get(4))).trim();
				line[5] = StringUtil.formatString( loc.getOrographicPrecipitationAdjustment(j), ((String)formats.get(5))).trim();
				
				buffer = new StringBuffer();	
				for (k = 0; k < fieldCount; k++) {
					if (line[k].indexOf(delimiter) > -1) {
						line[k] = "\"" + line[k] + "\"";
					}
					buffer.append(line[k]);
					if (k < (fieldCount - 1)) {
						buffer.append(delimiter);
					}
				}
	
				out.println(buffer.toString());
			}
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

/**
Writes the collection data from a list of StateCU_Location objects to a 
list file.  A header is printed to the top of the file, containing the commands 
used to generate the file.  Any strings in the body of the file that contain 
the field delimiter will be wrapped in "...". 
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of StateCU_Location objects to write, from which collection information will
be extracted.
@param newComments comments to add at the top of the file (e.g., commands, HydroBase information).
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
	fields.add("Division");
	fields.add("Year");
	fields.add("CollectionType");
	fields.add("PartType");
	fields.add("PartID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_CU_LOCATION_COLLECTIONS;
	
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.get(i);
		names[i] = StateCU_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateCU_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int[] years = null;
	int div = 0;
	int j = 0;
	int k = 0;
	int num = 0;
	PrintWriter out = null;
	StateCU_Location loc = null;
	List commentString = new Vector(1);
	commentString.add ( "#" );
	List ignoreCommentString = new Vector(1);
	ignoreCommentString.add ( "#>" );
	String[] field = new String[fieldCount];
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
		newComments2.add(1,"StateCU location collection information as delimited list file.");
		newComments2.add(2,"See also the associated location and climate station assignment files.");
		newComments2.add(3,"Division and year are only used with well aggregates.");
		newComments2.add(4,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			if ( i != 0 ) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			loc = (StateCU_Location)data.get(i);
			id = loc.getID();
			div = loc.getCollectionDiv();
			years = loc.getCollectionYears();
			if (years == null) {
				num = 0;
			}
			else {
				num = years.length;
			}
			colType = loc.getCollectionType();
			partType = loc.getCollectionPartType();
			// Loop through the number of years of collection data
			for (j = 0; j < num; j++) {
				ids = loc.getCollectionPartIDs(years[j]);
				// Loop through the identifiers for the specific year
				for ( k = 0; k < ids.size(); k++ ) {
					field[0] = StringUtil.formatString(id,formats[0]).trim();
					field[1] = StringUtil.formatString(div,formats[1]).trim();
					field[2] = StringUtil.formatString(years[j],formats[2]).trim();
					field[3] = StringUtil.formatString(colType,formats[3]).trim();
					field[4] = StringUtil.formatString(partType,formats[4]).trim();
					field[5] = StringUtil.formatString(((String)(ids.get(k))),formats[5]).trim();
	
					buffer = new StringBuffer();	
					for (int ifield = 0; ifield < fieldCount; ifield++) {
						if (ifield > 0) {
							buffer.append(delimiter);
						}
						if (field[ifield].indexOf(delimiter) > -1) {
							// Wrap delimiter in quoted field
							field[ifield] = "\"" + field[ifield] + "\"";
						}
						buffer.append(field[ifield]);
					}
		
					out.println(buffer.toString());
				}
			}
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

}