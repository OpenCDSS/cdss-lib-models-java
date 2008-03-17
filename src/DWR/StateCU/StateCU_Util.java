//-----------------------------------------------------------------------------
// StateCU_Util - utility classes for StateCU package
//-----------------------------------------------------------------------------
// History:
//
// 2003-06-04	Steven A. Malers, RTi	* Change name of class from
//					  StateCUUtil to StateCU_Util.
//					* Update to use DateTime instead of
//					  TSDate.
// 2004-02-28	SAM, RTi		* Move indexOf(), indexOfName(), match()
//					  from StateCU_Data.
// 2005-03-08	SAM, RTi		* Add sortStateCU_DataVector(), similar
//					  to the StateMod version.
// 2005-04-05	SAM, RTi		* Add lookupTimeSeriesGraphTitle().
// 2005-04-18	J. Thomas Sapienza, RTi	Added the lookup*() methods.
//-----------------------------------------------------------------------------

package DWR.StateCU;

import java.util.Collections;
import java.util.Vector;

import javax.swing.JTextField;

import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;
import RTi.Util.Time.DateTime;

/**
This StateCU_Util class contains static data and methods used in the StateCU
package.
*/
public abstract class StateCU_Util
{

public static String MISSING_STRING = "";
public static int MISSING_INT = -999;
public static float MISSING_FLOAT = (float)-999.0;
public static double MISSING_DOUBLE = -999.0;
private static double MISSING_DOUBLE_FLOOR = -999.1;
private static double MISSING_DOUBLE_CEILING = -998.9;
public static DateTime MISSING_DATE = null;

public static void checkAndSet(int i, JTextField textField) {
	if (isMissing(i)) {
		textField.setText("");
	}
	else {
		textField.setText("" + i);
	}
}

public static void checkAndSet(double d, JTextField textField) {
	if (isMissing(d)) {
		textField.setText("");
	}
	else {
		textField.setText("" + d);
	}
}

/**
Helper method to return validators to check an ID.
@return List of Validators.
 */
public static Validator[] getIDValidators()
{
	return new Validator[] { Validators.notBlankValidator(),
		Validators.regexValidator( "^[0-9a-zA-Z\\._]+$" ) };
}

/**
Determine the CU Location given a part identifier.  If the part identifier
matches a full location, then the full location identifier is returned.  Only ditch
identifiers can be matched (collections of parcels cannot).
@param CULocation_Vector a Vector of StateCU_Location to be searched.  The
collection information is assumed to have been defined for the locations.
@param part_id The identifier to be found in the list of locations.
@return the matching StateCU_Location, or null if a match cannot be found.
*/
public static StateCU_Location getLocationForPartID (
		Vector CULocation_Vector, String part_id )
{
	// First try to match the main location.
	
	int pos = indexOf ( CULocation_Vector, part_id );
	if ( pos >= 0 ) {
		return ( (StateCU_Location)CULocation_Vector.elementAt(pos) );
	}
	// If here, search the location collections...
	int size = 0;
	if ( CULocation_Vector != null ) {
		size = CULocation_Vector.size();
	}
	StateCU_Location culoc;
	Vector part_ids;
	for ( int i = 0; i < size; i++ ) {
		culoc = (StateCU_Location)CULocation_Vector.elementAt(i);
		// Only check aggregates/collections that are composed of ditches.
		if ( !culoc.getCollectionPartType().equalsIgnoreCase(StateCU_Location.COLLECTION_PART_TYPE_DITCH) ) {
			continue;
		}
		// Get the part identifiers...
		part_ids = culoc.getCollectionPartIDs(-1);	// Since ditches, year is irrelevant
		int size2 = part_ids.size();
		for ( int j = 0; j < size2; j++ ) {
			if ( part_id.equalsIgnoreCase((String)part_ids.elementAt(j))) {
				return culoc;
			}
		}
	}
	return null;
}

/**
Helper method to return general validators for numbers.
@return List of Validators.
 */
public static Validator[] getNumberValidators()
{
	//Validators.regexValidator( "^[0-9]+.*" ),
	return new Validator[] { Validators.notBlankValidator(),
		Validators.rangeValidator( 0, 999999 )};
}

/**
Helper method to return general validators for an on/off switch.
@return List of Validators.
 */
public static Validator[] getOnOffSwitchValidator()
{
	Validator[] orValidator = new Validator[] {
		Validators.isEquals( new Integer( 0 )),
		Validators.isEquals( new Integer( 1 )) };		
	return new Validator[] { Validators.notBlankValidator(),
		Validators.or( orValidator ) };
}

/**
Find the position of a StateCU_Data object in the data Vector, using the
identifer.  The position for the first match is returned.
@return the position, or -1 if not found.
@param id StateCU_Data identifier.
*/
public static int indexOf ( Vector data, String id )
{	int size = 0;
	if ( data != null ) {
		size = data.size();
	}
	StateCU_Data d = null;
	for (int i = 0; i < size; i++) {
		d = (StateCU_Data)data.elementAt(i);
		if ( id.equalsIgnoreCase ( d._id ) ) {
			return i;
		}
	}
	return -1;
}

/**
Find the position of a StateCU_Data object in the data Vector, using the name.
The position for the first match is returned.
@return the position, or -1 if not found.
@param name StateCU_Data name.
*/
public static int indexOfName ( Vector data, String name )
{	int size = 0;
	if ( data != null ) {
		size = data.size();
	}
	StateCU_Data d = null;
	for (int i = 0; i < size; i++) {
		d = (StateCU_Data)data.elementAt(i);
		if ( name.equalsIgnoreCase ( d._name ) ) {
			return i;
		}
	}
	return -1;
}

/**
Indicate if a double value is missing.
@param d Double precision value to check.
@return true if the value is missing, false, if not.
*/
public static boolean isMissing ( double d )
{	if (	(d < MISSING_DOUBLE_CEILING) &&
		(d > MISSING_DOUBLE_FLOOR) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Indicate if an integer value is missing.
@param i Integer value to check.
@return true if the value is missing, false, if not.
*/
public static boolean isMissing ( int i )
{	if ( i == MISSING_INT ) {
		return true;
	}
	else {	return false;
	}
}

/**
Indicate if a String value is missing.
@param s String value to check.
@return true if the value is missing, false, if not.
*/
public static boolean isMissing ( String s )
{	if ( (s == null) || (s.length() == 0) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Look up a title to use for a time series graph, given the data set component.
Currently this simply returns the component name, replacing " TS " with
" Time Series ".
@param comp_type StateCU component type.
*/
public static String lookupTimeSeriesGraphTitle ( int comp_type )
{	try {	StateCU_DataSet dataset = new StateCU_DataSet();
		return dataset.lookupComponentName (
			comp_type ).replaceAll(" TS ", " Time Series " );
	}
	catch ( Exception e ) {
		// Should not happen.
		return "";
	}
}

/**
Find a list of StateCU_Data in a Vector, using a regular expression to match
identifiers.
@param data_Vector a Vector of StateCU_Data to search.
@param pattern Regular expression pattern to use when finding 
@return a Vector containing StateCU_Data from data_Vector that have an
identifier that matches the requested pattern.  A non-null Vector will be
returned but it may have zero elements.
*/
public static Vector match ( Vector data_Vector, String pattern )
{	int size = 0;
	if ( data_Vector != null ) {
		size = data_Vector.size();
	}
	StateCU_Data data = null;
	Vector matches_Vector = new Vector ( size/10 + 1 );	// Guess size
	// Apparently if the pattern is "*", Java complains so do a specific
	// check...
	boolean return_all = false;
	if ( pattern.equals("*") ) {
		return_all = true;
	}
	// Loop regardless (always return a new Vector).
	for ( int i = 0; i < size; i++ ) {
		data = (StateCU_Data)data_Vector.elementAt(i);
		if ( return_all || data.getID().matches(pattern) ) {
			matches_Vector.addElement ( data );
		}
	}
	return matches_Vector;
}

/**
Sorts a Vector of StateCU_Data objects, depending on the compareTo() method
for the specific object.
@param data a Vector of StateCU_Data objects.  Can be null.
@return a new sorted Vector with references to the same data objects in the
passed-in Vector.  If a null Vector is passed in, an empty Vector will be
returned.
*/
public static Vector sortStateCU_DataVector ( Vector data )
{	return sortStateCU_DataVector ( data, true );
}

/**
Sorts a Vector of StateCU_Data objects, depending on the compareTo() method
for the specific object.
@param data a Vector of StateMod_Data objects.  Can be null.
@param return_new If true, return a new Vector with references to the data.
If false, return the original Vector, with sorted contents.
@return a sorted Vector with references to the same data objects in the
passed-in Vector.  If a null Vector is passed in, an empty Vector will be
returned.
*/
public static Vector sortStateCU_DataVector ( Vector data, boolean return_new )
{	if (data == null) {
		return new Vector();
	}
	Vector v = data;
	int size = data.size();
	if ( return_new ) {
		if (size == 0) {
			return new Vector();
		}
		v = new Vector();
		for (int i = 0; i < size; i++) {
			v.add(data.elementAt(i));
		}
	}

	if (size == 1) {
		return v;
	}

	Collections.sort(v);
	return v;
}

/**
Returns the property value for a component.  
@param componentType the kind of component to look up for.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
public static String lookupPropValue(int componentType, String propType,
String field) {
	if (componentType == StateCU_DataSet.COMP_BLANEY_CRIDDLE) {
		return lookupBlaneyCriddlePropValue(propType, field);
	}
	else if (componentType == StateCU_DataSet.COMP_CLIMATE_STATIONS) {
		return lookupClimatePropValue(propType, field);
	}
	else if (componentType == StateCU_DataSet.COMP_CROP_CHARACTERISTICS) {
		return lookupCropCharacteristicsPropValue(propType, field);
	}
	else if (componentType == StateCU_DataSet.COMP_CU_LOCATIONS) {
		return lookupLocationPropValue(propType, field);
	}
	else if (componentType 
	    == StateCU_DataSet.COMP_CU_LOCATION_CLIMATE_STATIONS) {
		return lookupLocationClimateStationPropValue(propType, field);
	}	
	else if (componentType 
	    == StateCU_DataSet.COMP_CU_LOCATION_COLLECTIONS) {
		return lookupLocationCollectionPropValue(propType, field);
	}		
	else if (componentType 
	    == StateCU_DataSet.COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY) {
	    	return lookupDelayTableAssignmentPropValue(propType, field);
	}

	return null;
}

/**
Returns the property value for blaney criddle data.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupBlaneyCriddlePropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("Name")) {
			return "CROP NAME";
		}
		else if (field.equalsIgnoreCase("DayPercent")) {
			return "DAY OR PERCENT";
		}
		else if (field.equalsIgnoreCase("Coefficient")) {
			return "COEFFICIENT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("Name")) {
			return "CROP\nNAME";
		}
		else if (field.equalsIgnoreCase("DayPercent")) {
			return "DAY OR\nPERCENT";
		}
		else if (field.equalsIgnoreCase("Coefficient")) {
			return "COEFFICIENT";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("Name")) {
			return "Crop name";
		}
		else if (field.equalsIgnoreCase("DayPercent")) {
			return "<html>Day of year if Perennial (start, middle, "
				+ "end of month).<br>Percent of year if annual "
				+ "(5% increments).</html>";
		}
		else if (field.equalsIgnoreCase("Coefficient")) {	
			return "Crop coefficient";
		}
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("Name")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("DayPercent")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("Coefficient")) {	
			return "%10.2f";
		}
	}

	return null;
}

/**
Returns the property value for climate data.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupClimatePropValue(String propType, String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "ELEVATION (FT)";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "LATITUDE (DEC. DEG.)";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "REGION1";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "REGION2";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nNAME";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "ELEVATION\n(FT)";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "LATITUDE\n(DEC. DEG.)";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "\nREGION1";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "\nREGION2";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "";
		}		
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "%-20.20s";
		}		
	}

	return null;
}

/**
Returns the property value for crop characteristics.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupCropCharacteristicsPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("PlantingMonth")) {
			return "PLANTING MONTH";
		}
		else if (field.equalsIgnoreCase("PlantingDay")) {
			return "PLANTING DAY";
		}
		else if (field.equalsIgnoreCase("HarvestMonth")) {
			return "HARVEST MONTH";
		}
		else if (field.equalsIgnoreCase("HarvestDay")) {
			return "HARVEST DAY";
		}
		else if (field.equalsIgnoreCase("DaysToCover")) {
			return "DAYS TO FULL COVER";
		}
		else if (field.equalsIgnoreCase("SeasonLength")) {
			return "SEASON LENGTH";
		}
		else if (field.equalsIgnoreCase("EarlyMoisture")) {
			return "TEMP EARLY MOISTURE (F)";
		}
		else if (field.equalsIgnoreCase("LateMoisture")) {
			return "TEMP LATE MOISTURE (F)";
		}
		else if (field.equalsIgnoreCase("DeficitLevel")) {
			return "MANAGEMENT ALLOWABLE DEFICIT LEVEL";
		}
		else if (field.equalsIgnoreCase("InitialRootZone")) {
			return "INITIAL ROOT ZONE DEPTH (IN)";
		}
		else if (field.equalsIgnoreCase("MaxRootZone")) {
			return "MAXIMUM ROOT ZONE DEPTH (IN)";
		}
		else if (field.equalsIgnoreCase("AWC")) {
			return "AVAILABLE WATER HOLDING CAPACITY AWC (IN)";
		}
		else if (field.equalsIgnoreCase("MAD")) {
			return "MAXIMUM APPLICATION DEPTH (IN)";
		}
		else if (field.equalsIgnoreCase("SpringFrost")) {
			return "SPRING FROST FLAG";	
		}
		else if (field.equalsIgnoreCase("FallFrost")) {
			return "FALL FROST FLAG";
		}
		else if (field.equalsIgnoreCase("DaysBetween1And2")) {
			return "DAYS BETWEEN 1ST AND 2ND CUT";
		}
		else if (field.equalsIgnoreCase("DaysBetween2And3")) {
			return "DAYS BETWEEN 2ND AND 3RD CUT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {
		if (field.equalsIgnoreCase("Name")) {
			return "\n\n\nNAME";
		}
		else if (field.equalsIgnoreCase("PlantingMonth")) {
			return "\n\nPLANTING\nMONTH";
		}
		else if (field.equalsIgnoreCase("PlantingDay")) {
			return "\n\nPLANTING\nDAY";
		}
		else if (field.equalsIgnoreCase("HarvestMonth")) {
			return "\n\nHARVEST\nMONTH";
		}
		else if (field.equalsIgnoreCase("HarvestDay")) {
			return "\n\nHARVEST\nDAY";
		}
		else if (field.equalsIgnoreCase("DaysToCover")) {
			return "\n\nDAYS TO\nFULL COVER";
		}
		else if (field.equalsIgnoreCase("SeasonLength")) {
			return "\n\nSEASON\nLENGTH";
		}
		else if (field.equalsIgnoreCase("EarlyMoisture")) {
			return "\n\nTEMP EARLY\nMOISTURE (F)";
		}
		else if (field.equalsIgnoreCase("LateMoisture")) {
			return "\n\nTEMP LATE\nMOISTURE (F)";
		}
		else if (field.equalsIgnoreCase("DeficitLevel")) {
			return "\nMANAGEMENT\nALLOWABLE\nDEFICIT LEVEL";
		}
		else if (field.equalsIgnoreCase("InitialRootZone")) {
			return "\nINITIAL ROOT\nZONE DEPTH\n(IN)";
		}
		else if (field.equalsIgnoreCase("MaxRootZone")) {
			return "\nMAXIMUM ROOT\nZONE DEPTH\n(IN)";
		}
		else if (field.equalsIgnoreCase("AWC")) {
			return "AVAILABLE\nWATER HOLDING\nCAPACITY\nAWC (IN)";
		}
		else if (field.equalsIgnoreCase("MAD")) {
			return "\nMAXIMUM\nAPPLICATION\nDEPTH (IN)";
		}
		else if (field.equalsIgnoreCase("SpringFrost")) {
			return "\nSPRING\nFROST\nFLAG";	
		}
		else if (field.equalsIgnoreCase("FallFrost")) {
			return "\nFALL\nFROST\nFLAG";
		}
		else if (field.equalsIgnoreCase("DaysBetween1And2")) {
			return "\nDAYS BETWEEN\n1ST AND 2ND\nCUT";
		}
		else if (field.equalsIgnoreCase("DaysBetween2And3")) {
			return "\nDAYS BETWEEN\n2ND AND 3RD\nCUT";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("PlantingMonth")) {
			return "";
		}
		else if (field.equalsIgnoreCase("PlantingDay")) {
			return "";
		}
		else if (field.equalsIgnoreCase("HarvestMonth")) {
			return "";
		}
		else if (field.equalsIgnoreCase("HarvestDay")) {
			return "";
		}
		else if (field.equalsIgnoreCase("DaysToCover")) {
			return "";
		}
		else if (field.equalsIgnoreCase("SeasonLength")) {
			return "";
		}
		else if (field.equalsIgnoreCase("EarlyMoisture")) {
			return "";
		}
		else if (field.equalsIgnoreCase("LateMoisture")) {
			return "";
		}
		else if (field.equalsIgnoreCase("DeficitLevel")) {
			return "";
		}
		else if (field.equalsIgnoreCase("InitialRootZone")) {
			return "";
		}
		else if (field.equalsIgnoreCase("MaxRootZone")) {
			return "";
		}
		else if (field.equalsIgnoreCase("AWC")) {
			return "";
		}
		else if (field.equalsIgnoreCase("MAD")) {
			return "";
		}
		else if (field.equalsIgnoreCase("SpringFrost")) {
			return "<html>Spring frost date flag"
				+ "<br>0 = mean<br>1 = 28F<br>2 = 32F</html>"; 
		}
		else if (field.equalsIgnoreCase("FallFrost")) {
			return "<html>Fall frost date flag"
				+ "<br>0 = mean<br>1 = 28F<br>2 = 32F</html>";
		}
		else if (field.equalsIgnoreCase("DaysBetween1And2")) {
			return "<html>Days between 1st and 2nd cutting."
				+ "<br>Alfalfa only.</html>";
		}
		else if (field.equalsIgnoreCase("DaysBetween2And3")) {
			return "<html>Days between 2nd and 3rd cutting."
				+ "<br>Alfalfa only.</html>";
		}		
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("Name")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("PlantingMonth")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("PlantingDay")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("HarvestMonth")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("HarvestDay")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DaysToCover")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("SeasonLength")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("EarlyMoisture")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("LateMoisture")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("DeficitLevel")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("InitialRootZone")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("MaxRootZone")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("AWC")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("MAD")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("SpringFrost")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("FallFrost")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DaysBetween1And2")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DaysBetween2And3")) {
			return "%8d";
		}	
	}

	return null;
}

/**
Returns the property value for delay table assignments.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDelayTableAssignmentPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "CU LOCATION ID";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY TABLE ID";
		}
		else if (field.equalsIgnoreCase("Percent")) {
			return "PERCENT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "CU\nLOCATION ID";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY\nTABLE ID";
		}
		else if (field.equalsIgnoreCase("Percent")) {
			return "\nPERCENT";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Percent")) {	
			return "";
		}
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Percent")) {	
			return "%10.2f";
		}	
	}

	return null;
}

/**
Returns the property value for locations.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupLocationPropValue(String propType, String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "ELEVATION (FT)";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "LATITUDE (DEC. DEG.)";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "REGION1";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "REGION2";
		}
		else if (field.equalsIgnoreCase("NumClimateStations")) {
			return "NUMBER OF CLIMATE STATIONS";
		}
		else if (field.equalsIgnoreCase("AWC")) {
			return "AVAILABLE WATER CONTENT AWC, (FRACTION)";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\n\n\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\n\n\n\nNAME";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "\n\n\nELEVATION\n(FT)";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "\n\n\nLATITUDE\n(DEC. DEG.)";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "\n\n\n\nREGION1";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "\n\n\n\nREGION2";
		}
		else if (field.equalsIgnoreCase("NumClimateStations")) {
			return "\n\nNUMBER OF\nCLIMATE\nSTATIONS";
		}
		else if (field.equalsIgnoreCase("AWC")) {
			return "AVAILABLE\nWATER\nCONTENT\nAWC,\n(FRACTION)";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "";
		}
		else if (field.equalsIgnoreCase("NumClimateStations")) {
			return "";
		}
		else if (field.equalsIgnoreCase("AWC")) {	
			return "";
		}
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Elevation")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("Latitude")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("Region1")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Region2")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("NumClimateStations")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("AWC")) {	
			return "%8.4f";
		}
	}

	return null;
}

/**
Returns the property value for location climate stations.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupLocationClimateStationPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "CU LOCATION ID";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "CLIMATE STATION ID";
		}
		else if (field.equalsIgnoreCase("PrecipWeight")) {
			return "PRECIPITATION STATION WEIGHT (FRACTION)";
		}
		else if (field.equalsIgnoreCase("TempWeight")) {
			return "TEMPERATURE STATION WEIGHT (FRACTION)";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("LocationID")) {
			return "\nCU\nLOCATION\nID";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "\nCLIMATE\nSTATION\nID";
		}
		else if (field.equalsIgnoreCase("PrecipWeight")) {
			return "PRECIPITATION\nSTATION\nWEIGHT\n(FRACTION)";
		}
		else if (field.equalsIgnoreCase("TempWeight")) {
			return "TEMPERATURE\nSTATION\nWEIGHT\n(FRACTION)";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("PrecipWeight")) {
			return "";
		}
		else if (field.equalsIgnoreCase("TempWeight")) {	
			return "";
		}
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("PrecipWeight")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("TempWeight")) {	
			return "%8.2f";
		}
	}

	return null;
}

/**
Returns the property value for location collections.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupLocationCollectionPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "CU LOCATION ID";
		}
		else if (field.equalsIgnoreCase("Division")) {
			return "DIVISION";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "YEAR";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "COLLECTION TYPE";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "PART TYPE";
		}
		else if (field.equalsIgnoreCase("PartID")) {
			return "PART ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("LocationID")) {
			return "CU\nLOCATION\nID";
		}
		else if (field.equalsIgnoreCase("Division")) {
			return "\n\nDIVISION";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "\n\nYEAR";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "\nCOLLECTION\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "\nPART\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartID")) {
			return "\nPART\nID";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Division")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "";
		}
		else if (field.equalsIgnoreCase("PartID")) {	
			return "";
		}
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("Division")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("PartID")) {	
			return "%-20.20s";
		}
	}

	return null;
}

} // End StateCU_Util