//-----------------------------------------------------------------------------
// StateCU_DataSet - an object to manage data in a StateCU data set.
//-----------------------------------------------------------------------------
// History:
//
// 2003-05-05	Steven A. Malers, RTi	Created class.
// 2003-06-04	SAM, RTi		* Rename class from CUDataSet to
//					  StateCU_DataSet.
//					* Add getTypeName().
//					* Add readXMLFile() method.
//					* Add writeXMLFile() method.
//					* Add the response file as a component.
// 2003-06-30	SAM, RTi		* Add support for component groups and
//					  group data components as per the
//					  StateCU documentation.
//					* Fold the StateCU_Control class into
//					  this class since the response and
//					  control file always go hand in hand.
//					  Separate read/write methods are still
//					  offered because the files are
//					  separate.
//					* Include iprtysm control file parameter
//					  as per Erin May 15 email.
//					* Remove obcout as per Erin July 1
//					  email.
//					* Add support for time series and other
//					  files to enable full support of data
//					  sets.
//					* Change isDirty(boolean) to setDirty(),
//					  consistent with other Java conventions
//					  (e.g., setEnabled()).
// 2003-07-13	SAM, RTi		* Extend the class from
//					  RTi.Util.IO.DataSet to allow more
//					  flexibility.
// 2004-02-19	SAM, RTi		* Update based on StateCU 4.35 data set.
//					* Use new file tags.
// 2004-03-18	SAM, RTi		* Add well pumping file, similar to
//					  historical diversions.
// 2005-01-17	JTS, RTi		Commented out StateCU_FrostTS call
//					around lines 1550 as it was causing
//					compile-time errors.
// 2005-03-29	SAM, RTi		Add sub-components for display only, and
//					handle specifically in the lookup
//					methods.  This is done similar to the
//					StateMod features.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

// FIXME SAM 2009-04-29 Need to remove code related to delay tables since not used by StateCU

package DWR.StateCU;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_DelayTable;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_TS;
import RTi.Util.IO.CheckFile;
import RTi.Util.IO.DataSet;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This StateCU_DataSet class manages data components in a StateCU data set,
essentially managing the list of components from the response file.  Control
data are also managed in this class.
Typically, each component corresponds to a file.  A list of components is
maintained and is displayed by StateCU_DataSet_JTree and StateCU_DataSet_JFrame.
*/
public class StateCU_DataSet extends DataSet
{

/**
The StateCU data set type is unknown.
*/
public static final int TYPE_UNKNOWN = 0;
private static String NAME_UNKNOWN = "Unknown";

/**
The StateCU data set is for climate stations (level 1).
*/
public static final int TYPE_CLIMATE_STATIONS = 1;
private static String NAME_CLIMATE_STATIONS = "Climate Stations";

/**
The StateCU data set is for structures (level 2).
*/
public static final int TYPE_STRUCTURES = 2;
private static String NAME_STRUCTURES = "Structures";

/**
The StateCU data set is for water supply limited (level 3).
*/
public static final int TYPE_WATER_SUPPLY_LIMITED = 3;
private static String NAME_WATER_SUPPLY_LIMITED =
				"Structures (Water Supply Limited)";

/**
The StateCU data set is for water supply limited by water rights (level 4).
*/
public static final int TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS = 4;
private static String NAME_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS =
			"Structures (Water Supply Limited by Water Rights)";

/**
The StateCU data set is for river depletion (level 5).
*/
public static final int TYPE_RIVER_DEPLETION = 5;
private static String NAME_RIVER_DEPLETION =
			"Structures (River Depletion)";

/**
The StateCU data set is for other uses (use -100 to identify).
*/
public static final int TYPE_OTHER_USES = -100;
private static String NAME_OTHER_USES = "Other Uses";

/**
StateCU data set component types, including groups.
*/
public static final int
	COMP_UNKNOWN = -1,	// Unknown component (e.g., for initialization)

	COMP_CONTROL_GROUP = 0,	// Control files
		COMP_RESPONSE = 1,	// All data sets need
		COMP_CONTROL = 2,	// All data sets need

	COMP_CLIMATE_STATIONS_GROUP = 3,	// Climate station files
		COMP_CLIMATE_STATIONS = 4,
		COMP_TEMPERATURE_TS_MONTHLY_AVERAGE = 5,
		COMP_FROST_DATES_TS_YEARLY = 6,
		COMP_PRECIPITATION_TS_MONTHLY = 7,

	COMP_CROP_CHARACTERISTICS_GROUP = 8,	// Crop characteristic files
		COMP_CROP_CHARACTERISTICS = 9,
		COMP_BLANEY_CRIDDLE = 10,

	COMP_DELAY_TABLES_GROUP = 11,		// This group supports the
						// River Depletion data set but
						// it does not have a one to
						// one assignment with CU
						// Locations so put in a
						// separate group
		COMP_DELAY_TABLES_MONTHLY = 12,

	COMP_CU_LOCATIONS_GROUP = 13,	// CU Location files
		COMP_CU_LOCATIONS = 14,	// Climate and structures...
			//COMP_CU_LOCATION_CLIMATE_STATIONS = 1401,
					// Climate station assignment (one to
					// many).
			//COMP_CU_LOCATION_COLLECTIONS = 1402,
					// Collection information (one to many).
		// TODO KAT 2007-04-12 Find out if changing these
		// values affects any command processing
		// had to change these values because of a check in the
		// parent class that dissallows the use of any type with
		// an integer number greater than the number of elements
		// in this array.  Since the numbers were 1401 and 1402
		// The parent code would always throw an exception.  Changed
		// to 29 and 30.
		COMP_CU_LOCATION_CLIMATE_STATIONS = 29,
			// Climate station assignment (one to
			// many).
		COMP_CU_LOCATION_COLLECTIONS = 30,
		// Collection information (one to many).
		COMP_CROP_PATTERN_TS_YEARLY = 15,
		// Water supply below...
		COMP_IRRIGATION_PRACTICE_TS_YEARLY = 16,
		// Water supply limited by rights below...
		COMP_DIVERSION_TS_MONTHLY = 17,
		COMP_WELL_PUMPING_TS_MONTHLY = 18,
		COMP_DIVERSION_RIGHTS = 19,
		// River Depletion below...
		COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY = 20,

	COMP_GIS_GROUP = 21,
		COMP_GIS_STATE = 22,
		COMP_GIS_DIVISIONS = 23,
		COMP_GIS_RIVERS = 24,
		COMP_GIS_CLIMATE_STATIONS = 25,
		// Structures only data below...
		COMP_GIS_STRUCTURES = 26,

	COMP_OTHER_GROUP = 27,
		COMP_OTHER = 28;	// Other - no files below

/**
Component types array for the above numbers.
*/
private static int [] __component_types = {
				COMP_CONTROL_GROUP,
				  COMP_RESPONSE,
				  COMP_CONTROL,
				COMP_CLIMATE_STATIONS_GROUP,
				  COMP_CLIMATE_STATIONS,
				  COMP_TEMPERATURE_TS_MONTHLY_AVERAGE,
				  COMP_FROST_DATES_TS_YEARLY,
				  COMP_PRECIPITATION_TS_MONTHLY,
				COMP_CROP_CHARACTERISTICS_GROUP,
				  COMP_CROP_CHARACTERISTICS,
				  COMP_BLANEY_CRIDDLE,
                                COMP_DELAY_TABLES_GROUP,
				  COMP_DELAY_TABLES_MONTHLY,
				COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS,
				  COMP_CROP_PATTERN_TS_YEARLY,
				  COMP_IRRIGATION_PRACTICE_TS_YEARLY,
				  COMP_DIVERSION_TS_MONTHLY,
				  COMP_WELL_PUMPING_TS_MONTHLY,
				  COMP_DIVERSION_RIGHTS,
				  COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY,
				COMP_GIS_GROUP,
				  COMP_GIS_STATE,
				  COMP_GIS_DIVISIONS,
				  COMP_GIS_RIVERS,
				  COMP_GIS_CLIMATE_STATIONS,
				  COMP_GIS_STRUCTURES,
				COMP_OTHER_GROUP,
				  COMP_OTHER,
				  COMP_CU_LOCATION_CLIMATE_STATIONS, 
				  COMP_CU_LOCATION_COLLECTIONS };

/**
Array indicating which components are groups.
*/
private static int [] __component_groups = {
				COMP_CONTROL_GROUP,
				COMP_CLIMATE_STATIONS_GROUP,
				COMP_CROP_CHARACTERISTICS_GROUP,
                                COMP_DELAY_TABLES_GROUP,
				COMP_CU_LOCATIONS_GROUP,
				COMP_GIS_GROUP,
				COMP_OTHER_GROUP };

/**
Array indicating the primary components within each component group.  The
primary components are used to get the list of identifiers for displays and
processing.  The number of values should agree with the list above.
*/
private static int [] __component_group_primaries = {
				COMP_RESPONSE,	// COMP_CONTROL_GROUP
				COMP_CLIMATE_STATIONS,
					// COMP_CLIMATE_STATIONS_GROUP
				COMP_CROP_CHARACTERISTICS,
					// COMP_CROP_CHARACTERISTICS_GROUP
                                COMP_DELAY_TABLES_MONTHLY,
					// COMP_DELAY_TABLES_GROUP
				COMP_CU_LOCATIONS,
					// COMP_CU_LOCATIONS_GROUP
				-1,	// COMP_GIS_GROUP
				-1 };	// COMP_OTHER_GROUP

/**
Array indicating the groups for components.
*/
private static int [] __component_group_assignments = {
				COMP_CONTROL_GROUP,
				  COMP_CONTROL_GROUP,
				  COMP_CONTROL_GROUP,
				COMP_CLIMATE_STATIONS_GROUP,
				  COMP_CLIMATE_STATIONS_GROUP,
				  COMP_CLIMATE_STATIONS_GROUP,
				  COMP_CLIMATE_STATIONS_GROUP,
				  COMP_CLIMATE_STATIONS_GROUP,
				COMP_CROP_CHARACTERISTICS_GROUP,
				  COMP_CROP_CHARACTERISTICS_GROUP,
				  COMP_CROP_CHARACTERISTICS_GROUP,
                                COMP_DELAY_TABLES_GROUP,
				  COMP_DELAY_TABLES_GROUP,
				COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				  COMP_CU_LOCATIONS_GROUP,
				COMP_GIS_GROUP,
				  COMP_GIS_GROUP,
				  COMP_GIS_GROUP,
				  COMP_GIS_GROUP,
				  COMP_GIS_GROUP,
				  COMP_GIS_GROUP,
				COMP_OTHER_GROUP,
				  COMP_OTHER_GROUP };

/**
Component names matching the above numbers.
*/
private static String [] __component_names = {
				"Control Data",
				  "Response",
				  "Control",
				"Climate Station Data",
				  "Climate Stations",
				  "Temperature TS (Monthly Average)",
				  "Frost Dates TS (Yearly)",
				  "Precipitation TS (Monthly)",
				"Crop Characteristics/Coefficient Data",
				  "Crop Characteristics",
				  "Blaney-Criddle Crop Coefficients",
				"Delay Table Data",
				  "Delay Tables",
				"CU Location Data and Crops",
				  "CU Locations",
				  "Crop Pattern TS (Yearly)",
				  "Irrigation Practice TS (Yearly)",
				  "Diversion TS (Monthly)",
				  "Well Pumping TS (Monthly)",
				  "Diversion Water Rights",
				  "Delay Assignment",
				"Spatial Data (GIS)",
				  "GIS - State Boundary",
				  "GIS - Water Divisions",
				  "GIS - Rivers",
				  "GIS - Climate Stations",
				  "GIS - Structures",
				"Other",
				  "Other" };

// Subcomponent names used with lookupComponentName().  These are special
// cases for labels and displays but the data are managed with a component
// listed above.  Make private to force handling through lookup methods.
private final static String
	__COMPNAME_CU_LOCATION_CLIMATE_STATIONS =
		"CU Location Climate Station Assignment",
	__COMPNAME_CU_LOCATION_COLLECTIONS =
		"CU Location Collection Definitions";

/**
Component names matching the above numbers, to use for XML tags.
*/
/* TODO SAM 2007-03-01 Evaluate use
private static String [] __xml_component_names = {
				"ControlData",
				  "Response",
				  "Control",
				"ClimateStationData",
				  "ClimateStations",
				  "TemperatureTS",
				  "FrostDatesTS",
				  "PrecipitationTS",
				"CropCharacteristicsData",
				  "CropCharacteristics",
				  "Blaney-CriddleCropCoefficients",
				"DelayTables",
				  "DelayTables",
				"CULocations",
				  "CULocations",
				  "CropPatternTS",
				  "IrrigationPracticeTS",
				  "DiversionTS",
				  "WellPumpingTS",
				  "DiversionWaterRights",
				  "DelayAssignment",
				"SpatialData",
				  "StateBoundary",
				  "WaterDivisions",
				  "Rivers",
				  "ClimateStations",
				  "Structures",
				"Other",
				  "Other" };
				  */
/**
Component type indicators (from response file) - group tags should never be
used.
*/
private static String [] __component_tags = {
				"Control",
				  "rcu",
				  "ccu",
				"Climate Stations",
				  "cli",
				  "tmp",
				  "fd",
				  "ppt",
				"Crop Characteristics",
				  "cch",
				  "kbc",
				"Delay Tables",
				  "dly",
				"CU Locations",
				  "str",
				  "cds",
				  "ipy",	// Previously tsp
				  "ddh",
				  "pvh",
				  "ddr",
				  "dla",
				"Spatial Data",
				  "stategis",
				  "divgis",
				  "hydrogis",
				  "climgis",
				  "strucgis",
				"Other",
				  "oth" };

/**
Component preferred extensions matching the above numbers.
*/
/* TODO SAM 2007-03-01 Evaluate use
private static String [] __component_ext = {
				"Control",
				  "rcu",
				  "ccu",
				"Climate Stations",
				  "cli",
				  "stm",
				  "stm",
				  "stm",
				"Crop Characteristics",
				  "cch",
				  "kbc",
				"Delay Tables",
				  "dly",
				"CU Locations",
				  "str",
				  "cds",
				  "ipy",
				  "ddh",
				  "pvh",
				  "ddr",
				  "dla",
				"Spatial Data",
				  "shp",
				  "shp",
				  "shp",
				  "shp",
				  "shp",
				"Other",
				  "oth" };
				  */

// Control file data specific to StateCU...

private String [] __comments = null;
private int __nyr1 = StateCU_Util.MISSING_INT;
private int __nyr2 = StateCU_Util.MISSING_INT;
private int __flag1 = StateCU_Util.MISSING_INT;
private int __rn_xco = StateCU_Util.MISSING_INT;
private int __iclim = StateCU_Util.MISSING_INT;
private int __isupply = StateCU_Util.MISSING_INT;
private int __sout = StateCU_Util.MISSING_INT;
private int __ism = StateCU_Util.MISSING_INT;
private double __pjunmo = StateCU_Util.MISSING_DOUBLE;
private double __pothmo = StateCU_Util.MISSING_DOUBLE;
private double __psenmo = StateCU_Util.MISSING_DOUBLE;
private int __iprtysm = StateCU_Util.MISSING_INT;
private int __typout = StateCU_Util.MISSING_INT;
private int __iflood = StateCU_Util.MISSING_INT;
private int __ddcsw = StateCU_Util.MISSING_INT;
private int __idaily = StateCU_Util.MISSING_INT;
private double __admin_num = StateCU_Util.MISSING_DOUBLE;

/**
Construct a blank data set.  It is expected that other information will be set
during further processing.  Component groups are not initialized until a data
set type is set.
*/
public StateCU_DataSet ()
throws Exception
{	// Pass the arrays of information about the data set to the base class
	// so general methods will work...

	super ( __component_types, __component_names,
		__component_groups, __component_group_assignments,
		__component_group_primaries );

	// Initialize data specific to the StateCU data set...
	__comments = new String[3];
	__comments[0] = StateCU_Util.MISSING_STRING;
	__comments[1] = StateCU_Util.MISSING_STRING;
	__comments[2] = StateCU_Util.MISSING_STRING;
	
	initializeComponentGroups();
}

/**
Construct a blank data set.  Specific output files, by default, will use the
output directory and base file name in output file names.
@param type Data set type.
@param dataset_dir Data set directory.
@param basename Basename for files (no directory).
@exception Exception if there is an error.
*/
public StateCU_DataSet ( int type, String dataset_dir, String basename )
throws Exception
{	// Pass the arrays of information about the data set to the base class
	// so general methods will work...

	super ( __component_types, __component_names,
		__component_groups, __component_group_assignments,
		__component_group_primaries );

	String routine = "StateCU_DataSet";
	setDataSetType ( type, true );
	setDataSetDirectory ( dataset_dir );
	setBaseName ( basename );

	__comments = new String[3];
	__comments[0] = StateCU_Util.MISSING_STRING;
	__comments[1] = StateCU_Util.MISSING_STRING;
	__comments[2] = StateCU_Util.MISSING_STRING;

	// Every data set type gets a response and control file...

	try {	initializeComponentGroups();

		getComponentForComponentType (
			COMP_CONTROL_GROUP ).addComponent (
			new DataSetComponent ( this, COMP_RESPONSE ) );
		getComponentForComponentType (
			COMP_CONTROL_GROUP ).addComponent (
			new DataSetComponent ( this, COMP_CONTROL ) );
	}
	catch ( Exception e ) {
		// Should not happen...
		Message.printWarning ( 2, routine, e );
	}

	// Other uses

	if ( type == TYPE_OTHER_USES ) {
		// REVISIT - add later.
	}
	else {	// Every data set has...
		try {	getComponentForComponentType (
			COMP_CLIMATE_STATIONS_GROUP ).addComponent (
			new DataSetComponent ( this, COMP_CLIMATE_STATIONS ) );
			getComponentForComponentType (
			COMP_CLIMATE_STATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_TEMPERATURE_TS_MONTHLY_AVERAGE ));
			getComponentForComponentType (
			COMP_CLIMATE_STATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_FROST_DATES_TS_YEARLY ) );
			getComponentForComponentType (
			COMP_CLIMATE_STATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_PRECIPITATION_TS_MONTHLY) );

			getComponentForComponentType (
			COMP_CROP_CHARACTERISTICS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_CROP_CHARACTERISTICS ));
			getComponentForComponentType (
			COMP_CROP_CHARACTERISTICS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_BLANEY_CRIDDLE ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}

	if ( type == TYPE_RIVER_DEPLETION ) {
		// Want this before CU Locations...
		try {	getComponentForComponentType (
			COMP_DELAY_TABLES_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_DELAY_TABLES_MONTHLY ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}

	if ( type != TYPE_OTHER_USES ) {
		// Add CU Locations...
		try {	getComponentForComponentType (
			COMP_CU_LOCATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_CU_LOCATIONS ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}

	if ( type >= TYPE_STRUCTURES ) {
		try {	getComponentForComponentType (
			COMP_CU_LOCATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_CROP_PATTERN_TS_YEARLY ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}
	if ( type >= TYPE_WATER_SUPPLY_LIMITED ) {
		try {	getComponentForComponentType (
			COMP_CU_LOCATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_IRRIGATION_PRACTICE_TS_YEARLY ) );
			getComponentForComponentType (
			COMP_CU_LOCATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_DIVERSION_TS_MONTHLY ) );
			getComponentForComponentType (
			COMP_CU_LOCATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_WELL_PUMPING_TS_MONTHLY ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}
	if ( type >= TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS ) {
		try {	getComponentForComponentType (
			COMP_CU_LOCATIONS_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_DIVERSION_RIGHTS ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}
	if ( type == TYPE_RIVER_DEPLETION ) {
		try {	getComponentForComponentType (
			COMP_DELAY_TABLES_GROUP ).addComponent (
			new DataSetComponent ( this,
			COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY ) );
		}
		catch ( Exception e ) {
			// Should not happen...
			Message.printWarning ( 2, routine, e );
		}
	}
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	__comments = null;
	super.finalize();
}

/**
Return the administration number for CU by priority.
@return the administration number for CU by priority.
*/
public double getAdminNumForCUByPriority ()
{	return __admin_num;
}

/**
Return a comment for the data set.
@return a comment for the data set.
@param pos Comment index (0 - 2).
*/
public String getComment ( int pos )
{	if ( (pos < 0) || (pos > 2) ) {
		return "";
	}
	return __comments[pos];
}

/**
Return the data set type name.  This method calls lookupDataSetName() for the
instance.
@return the data set type name.
*/
public String getDataSetName ()
{	return lookupDataSetName ( getDataSetType() );
}

/**
Return whether to create StateMod output.
@return whether to create StateMod output.
*/
public int getDdcsw ()
{	return __ddcsw;
}

/**
Return the value of flag1 (CU method).
@return the value of flag1.
*/
public int getFlag1 ()
{	return __flag1;
}

/**
Return the value of iclim (climate stations or structures data set type).
@return the value of iclim.
*/
public int getIclim ()
{	return __iclim;
}

/**
Return the value of idaily (daily/monthly switch).
@return the value of idaily.
*/
public int getIdaily ()
{	return __idaily;
}

/**
Return the value of iflood (groundwater use switch).
@return the value of iflood.
*/
public int getIflood ()
{	return __iflood;
}

/**
Return the value of iprtysm (operate soil moisture by proration).
@return the value of iprtysm.
*/
public int getIprtysm ()
{	return __iprtysm;
}

/**
Return the value of ism (soil moisture flag).
@return the value of ism.
*/
public int getIsm ()
{	return __ism;
}

/**
Return the value of isupply (water supply option).
@return the value of isupply.
*/
public int getIsupply ()
{	return __isupply;
}

/**
Return the starting year for the data set.
@return the starting year for the data set.
*/
public int getNyr1 ()
{	return __nyr1;
}

/**
Return the ending year for the data set.
@return the ending year for the data set.
*/
public int getNyr2 ()
{	return __nyr2;
}

/**
Return the initial soil moisture content for junior parcels (fraction of
capacity).
@return the initial soil moisture content for junior parcels.
*/
public double getPjunmo ()
{	return __pjunmo;
}

/**
Return the initial soil moisture content for other parcels (fraction of
capacity).
@return the initial soil moisture content for other parcels.
*/
public double getPothmo ()
{	return __pothmo;
}

/**
Return the initial soil moisture content for senior parcels (fraction of
capacity).
@return the initial soil moisture content for senior parcels.
*/
public double getPsenmo ()
{	return __psenmo;
}

/**
Return the value of rn_xco (monthly precipitation method).
@return the value of rn_xco.
*/
public int getRn_xco ()
{	return __rn_xco;
}

/**
Return the value of sout (input summary type).
@return the value of sout.
*/
public int getSout ()
{	return __sout;
}

/**
Return the value of typout (output summary detail).
@return the value of typout.
*/
public int getTypout ()
{	return __typout;
}

/**
Initialize the component groups.  This is usually done immediately after
the data set type is known (the data set type must be set).
The list source for each group is set to
DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT.
 * @throws Exception 
@exception Exception if there is an error initializing the component groups.
*/
private void initializeComponentGroups ()
{	
	// Always add the control group...
	DataSetComponent comp,subcomp = null;
	try {
		comp = new DataSetComponent ( this, COMP_CONTROL_GROUP );
	
	comp.setListSource ( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
	addComponent ( comp );
	int type = getDataSetType();
	if ( type == TYPE_OTHER_USES ) {
		comp = new DataSetComponent ( this, COMP_OTHER_GROUP );
		comp.setListSource (
		DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
	}
	else {
		// TODO KAT 2007-04-12
		// Need to evaluate whether these are subgroups or not
		comp = new DataSetComponent( this, COMP_BLANEY_CRIDDLE );
		comp.setListSource( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		comp = new DataSetComponent( this, COMP_CROP_PATTERN_TS_YEARLY );
		comp.setListSource( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		comp = new DataSetComponent( this, COMP_IRRIGATION_PRACTICE_TS_YEARLY );
		comp.setListSource( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		comp = new DataSetComponent( this, COMP_DELAY_TABLES_MONTHLY );
		comp.setListSource( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		comp = new DataSetComponent( this, COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY );
		comp.setListSource( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		/////////////////
		
		comp = new DataSetComponent (this,COMP_CLIMATE_STATIONS_GROUP );
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
		subcomp = new DataSetComponent( this, COMP_CLIMATE_STATIONS );
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		// TODO KAT 2007-04-12 
		// add extra subcomponents for climate stations here
		// need to figure out all which components go here
		
		comp = new DataSetComponent( this,
			COMP_CROP_CHARACTERISTICS_GROUP);
		comp.setListSource (
		DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
		addComponent ( comp );
		subcomp = new DataSetComponent( this, COMP_CROP_CHARACTERISTICS );
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		// TODO KAT 2007-04-12 
		// need to figure out all subcomponents which go here
		
		if ( type == TYPE_RIVER_DEPLETION ) {
			comp = new DataSetComponent( this,
				COMP_DELAY_TABLES_GROUP);
			comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
			addComponent ( comp );
			subcomp = new DataSetComponent( this, COMP_DELAY_TABLES_MONTHLY );
			subcomp.setData ( new Vector() );
			comp.addComponent( subcomp );
			subcomp = new DataSetComponent( this, 
				COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY );
			subcomp.setData ( new Vector() );
			comp.addComponent( subcomp );
		}
		comp = new DataSetComponent( this, COMP_CU_LOCATIONS_GROUP);
		comp.setListSource (
		DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
		subcomp = new DataSetComponent( this, COMP_CU_LOCATIONS );
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent( this, COMP_CU_LOCATION_CLIMATE_STATIONS );
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent( this, COMP_CU_LOCATION_COLLECTIONS );
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
	}
	// Always add GIS a separate group...
	comp = new DataSetComponent(this, COMP_GIS_GROUP);
	comp.setListSource ( DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
	addComponent ( comp );
	} catch (Exception e) {
		e.printStackTrace();
	}
}

/**
Returns the name of the specified component.  Subcomponents (e.g., CU location
climate station assignment) are specifically checked and then the base class
method is called.
@param comp_type the component type integer.
@return the name of the specified component.
*/
public String lookupComponentName ( int comp_type )
{	if ( comp_type == COMP_CU_LOCATION_CLIMATE_STATIONS ) {
		return __COMPNAME_CU_LOCATION_CLIMATE_STATIONS;
	}
	else if ( comp_type == COMP_CU_LOCATION_COLLECTIONS ) {
		return __COMPNAME_CU_LOCATION_COLLECTIONS;
	}
	else {	return super.lookupComponentName(comp_type);
	}
}

/**
Determine the data set type from a string.
@param type Data set type as a string.
@return the data set type as an integer or -1 if not found.
*/
public static int lookupDataSetType ( String type )
{	if ( type.equalsIgnoreCase(NAME_UNKNOWN) ) {
		return TYPE_UNKNOWN;
	}
	else if ( type.equalsIgnoreCase( NAME_CLIMATE_STATIONS) ) {
		return TYPE_CLIMATE_STATIONS;
	}
	else if ( type.equalsIgnoreCase( NAME_STRUCTURES) ) {
		return TYPE_STRUCTURES;
	}
	else if ( type.equalsIgnoreCase( NAME_WATER_SUPPLY_LIMITED) ) {
		return TYPE_WATER_SUPPLY_LIMITED;
	}
	else if ( type.equalsIgnoreCase(
		NAME_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS) ) {
		return TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS;
	}
	else if ( type.equalsIgnoreCase( NAME_RIVER_DEPLETION) ) {
		return TYPE_RIVER_DEPLETION;
	}
	else if ( type.equalsIgnoreCase( NAME_OTHER_USES) ) {
		return TYPE_OTHER_USES;
	}
	else {	return -1;
	}
}

/**
Return the data set type name.  This is suitable for warning messages and
simple output.
@param dataset_type Data set type (see TYPE_*).
@return the data set type name.
*/
public static String lookupDataSetName ( int dataset_type )
{	if ( dataset_type == TYPE_UNKNOWN ) {
		return NAME_UNKNOWN;
	}
	else if ( dataset_type == TYPE_CLIMATE_STATIONS ) {
		return NAME_CLIMATE_STATIONS;
	}
	else if ( dataset_type == TYPE_STRUCTURES ) {
		return NAME_STRUCTURES;
	}
	else if ( dataset_type == TYPE_WATER_SUPPLY_LIMITED ) {
		return NAME_WATER_SUPPLY_LIMITED;
	}
	else if ( dataset_type == TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS ) {
		return NAME_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS;
	}
	else if ( dataset_type == TYPE_RIVER_DEPLETION ) {
		return NAME_RIVER_DEPLETION;
	}
	else if ( dataset_type == TYPE_OTHER_USES ) {
		return NAME_OTHER_USES;
	}
	else {	return "";
	}
}

/**
Return the numeric component type given its string tag, from the StateCU
response file.  This method is called when reading the StateCU response file.
@return the numeric component type given its string type, or -1 if not found.
@param component_tag the component tag from the response file.
*/
public static int lookupComponentTypeFromTag ( String component_tag )
{	for ( int i = 0; i < __component_tags.length; i++ ) {
		if ( __component_tags[i].equalsIgnoreCase(component_tag) ) {
			return i;
		}
	}
	return -1;
}

/**
Construct a DataSetComponent object from a response file string.  The string
should be of the form:
<pre>
file.ext, type
</pre>
@param dataset The StateCU_DataSet for the component.
@param line Non-comment data line from response file.
@exception Exception if there is an error creating the object.
*/
public static DataSetComponent parseDataSetComponent (	StateCU_DataSet dataset,
							String line )
throws Exception
{	List v = StringUtil.breakStringList ( line, ", \t", StringUtil.DELIM_SKIP_BLANKS );
	if ( (v == null) || (v.size() < 2) ) {
		throw new Exception ( "Bad StateCU data (\"" + line +"\"");
	}
	String type_string = (String)v.get(1);
	int comptype = lookupComponentTypeFromTag ( type_string );
	if ( comptype < 0 ) {
		throw new Exception ( "Unrecognized type in (\"" + line +"\"");
	}
	DataSetComponent comp = new DataSetComponent ( dataset, comptype );
	comp.setDataFileName ( (String)v.get(0) );
	return comp;
}

/**
Process an XML Document node during the read process.
@param dataset StateCU_DataSet that is being read.
@param node an XML document node, which may have children.
@exception Exception if there is an error processing the node.
*/
/* TODO SAM 2007-03-01 Evaluate if in the base class
private static void processDocumentNodeForRead (	StateCU_DataSet dataset,
							Node node )
throws Exception
{	String routine = "StateCU_DataSet.processDocumentNodeForRead";
	/ * REVISIT - need to figure out if this is in the base class
	switch ( node.getNodeType() ) {
		case Node.DOCUMENT_NODE:
			// The main data set node.  Get the data set type, etc.
			processDocumentNodeForRead( dataset,
				((Document)node).getDocumentElement() );
			break;
		case Node.ELEMENT_NODE:
			// Data set components.  Print the basic information...
			String element_name = node.getNodeName();
			Message.printStatus ( 1, routine, "Element name: " +
				element_name );
			NamedNodeMap attributes;
			Node attribute_Node;
			String attribute_name, attribute_value;
			// Evaluate the nodes attributes...
			if ( element_name.equalsIgnoreCase("StateCU_DataSet") ){
				attributes = node.getAttributes();
				int nattributes = attributes.getLength();
				for ( int i = 0; i < nattributes; i++ ) {
					attribute_Node = attributes.item(i);
					attribute_name =
						attribute_Node.getNodeName();
					if (	attribute_name.equalsIgnoreCase(
						"Type" ) ) {
						try {	dataset.setType (
							attribute_Node.
							getNodeValue(), true );
						}
						catch ( Exception e ) {
							Message.printWarning (
							2, routine,
							"Data set type \"" +
							attribute_name + "\" is"
							+ " not recognized." );
							throw new Exception (
							"Error processing data "
							+ "set" );
						}
					}
					else if (
						attribute_name.equalsIgnoreCase(
						"BaseName" ) ) {
						dataset.setBaseName (
						attribute_Node.getNodeValue() );
					}
				}
			}
			else if ( element_name.equalsIgnoreCase(
				"DataSetComponent") ) {
				attributes = node.getAttributes();
				int nattributes = attributes.getLength();
				String	comptype = "", compdatafile = "",
					complistfile = "", compcommandsfile ="";
				for ( int i = 0; i < nattributes; i++ ) {
					attribute_Node = attributes.item(i);
					attribute_name =
						attribute_Node.getNodeName();
					attribute_value =
						attribute_Node.getNodeValue();
					if (	attribute_name.equalsIgnoreCase(
						"Type" ) ) {
						comptype = attribute_value;
					}
					else if(attribute_name.equalsIgnoreCase(
						"DataFile" ) ) {
						compdatafile = attribute_value;
					}
					else if(attribute_name.equalsIgnoreCase(
						"ListFile" ) ) {
						complistfile = attribute_value;
					}
					else if(attribute_name.equalsIgnoreCase(
						"CommandsFile" ) ) {
						compcommandsfile =
						attribute_value;
					}
					else {	Message.printWarning ( 2,
						routine, "Unrecognized " +
						"attribute \"" + attribute_name+
						" for \"" + element_name +"\"");
					}
				}
				int component_type =
				DataSetComponent.lookupComponentType(
							comptype );
				if ( component_type < 0 ) {
					Message.printWarning ( 2, routine,
					"Unrecognized data set component \"" +
					comptype + "\".  Skipping." );
					return;
				}
				// Add the component...
				DataSetComponent comp = 
				new DataSetComponent ( this, component_type );
				comp.setDataFileName ( compdatafile );
				comp.setListFileName ( complistfile );
				comp.setCommandsFileName ( compcommandsfile );
				Message.printStatus ( 1, routine,
				"Adding new component for data \"" +
				compdatafile + "\" \"" );
				dataset.addComponent ( comp );
			}
			// The main document node will have a list of children
			// (data set components) but components will not.
			// Recursively process each node...
			NodeList children = node.getChildNodes();
			if ( children != null ) {
				int len = children.getLength();
				for ( int i = 0; i < len; i++ ) {
					processDocumentNodeForRead (
						dataset, children.item(i) );
				}
			}
			break;
	}
}
*/

/**
Read the StateCU control file and handle data in the StateCU_DataSet instance.
This method is usually only called by readStateCUFile(), which reads the StateCU
response file.
@param filename filename containing StateCU control data.
*/
private void readStateCUControlFile ( String filename )
throws IOException
{	String rtn = "StateCU_DataSet.readStateCUControlFile";
	String iline = null;
	BufferedReader in = null;

	Message.printStatus ( 1, rtn,
		"Reading StateCU control file: \"" + filename + "\"" );
	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	String string;
	String token1;		// First token on a line
	int datarec = -1;	// Counter for data records (not comments).
	List v = null;
	// Use a while to check for comments.
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ) {
			continue;
		}
		++datarec;
		string = iline.trim();
		token1 = StringUtil.getToken ( string, " \t",
			StringUtil.DELIM_SKIP_BLANKS, 0 );
		if ( (datarec >= 0) && (datarec <= 2) ) {
			setComment ( string, datarec );
		}
		else if ( datarec == 3 ) {
			v = StringUtil.breakStringList ( string, " \t",
				StringUtil.DELIM_SKIP_BLANKS );
			if ( v != null ) {
				if (	(v.size() >= 1) &&
					StringUtil.isInteger(
					(String)v.get(0)) ) {
					setNyr1 ( StringUtil.atoi (
					(String)v.get(0)) );
				}
				if (	(v.size() >= 2) &&
					StringUtil.isInteger(
					(String)v.get(1)) ) {
					setNyr2 ( StringUtil.atoi (
					(String)v.get(1)) );
				}
			}
		}
		else if ( datarec == 4 ) {
			setFlag1 ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 5 ) {
			setRn_xco ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 6 ) {
			setIclim ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 7 ) {
			setIsupply ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 8 ) {
			setSout ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 9 ) {
			setIsm ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 10 ) {
			v = StringUtil.fixedRead ( iline, "s5s5s5" );
			if ( v != null ) {
				if (	(v.size() >= 1) &&
					StringUtil.isDouble(
					((String)v.get(0)).trim()) ) {
					setPsenmo ( StringUtil.atod (
					((String)v.get(0)).trim()) );
				}
				if (	(v.size() >= 2) &&
					StringUtil.isDouble(
					((String)v.get(1)).trim()) ) {
					setPjunmo ( StringUtil.atod (
					((String)v.get(1)).trim()) );
				}
				if (	(v.size() >= 3) &&
					StringUtil.isDouble(
					((String)v.get(2)).trim()) ) {
					setPothmo ( StringUtil.atod (
					((String)v.get(2)).trim()) );
				}
			}
		}
		else if ( datarec == 11 ) {
			setIprtysm ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 12 ) {
			setTypout ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 13 ) {
			setIflood ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 14 ) {
			setDdcsw ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 15 ) {
			setIdaily ( StringUtil.atoi(token1) );
		}
		else if ( datarec == 16 ) {
			setAdminNumForCUByPriority (
			StringUtil.atod(token1) );
		}
	}
	if ( in != null ) {
		in.close();
	}
}

/**
Read the StateCU response file and return a StateCU_DataSet object.
@param filename StateCU response file.
@param read_all If true, all the data files mentioned in the response file will
be read into memory, providing a complete data set for viewing and manipulation.
*/
public static StateCU_DataSet readStateCUFile(String filename,boolean read_all )
throws Exception
{	String routine = "StateCU_DataSet.readStateCUFile";
	String iline = null;
	BufferedReader in = null;
	Message.printStatus ( 1, routine, "Reading StateCU response file: " +
		filename );

	// Set the data set directory to be used when opening the component
	// files...

	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	File f = new File ( full_filename );

	// Declare the data set of the appropriate type (the data set type is
	// handled below after reading the control file)...  

	StateCU_DataSet dataset = new StateCU_DataSet ();
	dataset.setDataSetDirectory ( f.getParent() );
	dataset.setDataSetFileName ( f.getName() );

	// Add a data set component for the response and control files...

	DataSetComponent 
		response_comp = new DataSetComponent ( dataset, COMP_RESPONSE );
	response_comp.setDataFileName ( f.getName() );

	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));

	DataSetComponent comp = null;
	int comptype;		// Component type
	String compfile;	// Data file for component
	int iclim = 0;
	int isupply = 0;
	String read_warning = "";
	String unneeded_warning = "";

	// First read in the entire response file.  This allows the control
	// file to be determined and read at once, which then allows the data
	// set to be intialized properly (groups, etc.).  This is also necessary
	// because the control file is not guaranteed to be the first file
	// specified.

	List rcu_strings = new Vector();
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		iline = iline.trim();
		if (iline.startsWith("#") || (iline.length() == 0) ) {
			continue;
		}
		rcu_strings.add ( iline );
	}

	// Read the control file...

	int size = rcu_strings.size();
	int dataset_type = TYPE_UNKNOWN;
	DataSetComponent control_comp = null;
	for ( int i = 0; i < size; i++ ) {
		try {	comp = parseDataSetComponent ( dataset,
				(String)rcu_strings.get(i) );
			// The following are set in the above parse method...
			comptype = comp.getComponentType ();
			compfile = comp.getDataFileName();
			if ( comptype != COMP_CONTROL ) {
				continue;
			}
			// Else, read the control file...
			// Save to add to the data set below...
			control_comp = comp;
			// Always read this because it specifies
			// configuration information...
			f = new File(compfile);
			if ( !f.isAbsolute() ) {
				compfile = dataset.getDataSetDirectory() +
					File.separator + compfile;
			}
			try {	// Previously, the StateCU_Control
				// object was used to store the control
				// data but now the data are in the
				// StateCU_DataSet object itself so declare
				// the dataset object here...

				// Now read the control file.
				comp.setData ( dataset );
				dataset.readStateCUControlFile (
					compfile );
				iclim = dataset.getIclim();
				isupply = dataset.getIsupply();
				Message.printStatus ( 1, routine,
				"iclim=" + iclim + " isupply=" +
				isupply );
				if ( iclim == 0 ) {
					dataset_type =
					TYPE_CLIMATE_STATIONS;
					Message.printStatus ( 1,
					routine,
					"Reading climate stations " +
					"data set" );
				}
				// A structures data set - check the
				// supply flag to know what type of data
				// set...
				else if ( isupply == 0 ) {
					dataset_type = TYPE_STRUCTURES;
					Message.printStatus ( 1,
					routine,
					"Reading structures data set" );
				}
				else if ( isupply == 1 ) {
					dataset_type =
					TYPE_WATER_SUPPLY_LIMITED;
					Message.printStatus ( 1,
					routine,
					"Reading structures " +
					"(water supply limited) data " +
					"set" );
				}
				else if ( isupply == 2 ) {
					dataset_type =
					TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS;
					Message.printStatus ( 1,
					routine,
					"Reading structures " +
					"(water supply limited by water"
					+ " rights) data set" );
				}
				else if ( isupply == 3 ) {
					dataset_type =
					TYPE_RIVER_DEPLETION;
					Message.printStatus ( 1,
					routine,
					"Reading structures " +
					"(river depletion) data set" );
				}
			}
			catch ( Exception e2 ) {
				Message.printWarning ( 1, routine,
				"Error reading data for:\n" +
				"\"" + compfile + "\"" );
				Message.printWarning ( 2, routine, e2 );
			}
		}
		catch ( Exception e2 ) {
			// Ignore bad components...
			continue;
		}
	}

	// Initialize the data set components using the data set type...

	dataset.setDataSetType ( dataset_type, true );
	// Reinitialize the groups now that we know the data set type...
	dataset.initializeComponentGroups ();

	// Add the response and control file components...

	dataset.getComponentForComponentType ( COMP_CONTROL_GROUP ).
		addComponent( response_comp );
	dataset.getComponentForComponentType ( COMP_CONTROL_GROUP ).
		addComponent( control_comp );

	// Now loop through the remaining components...

	for ( int i = 0; i < size; i++ ) {
		iline = (String)rcu_strings.get(i);
		// Allocate new DataSetComponent instance...
		try {	try {	comp = parseDataSetComponent(dataset,iline);
			}
			catch ( Exception e2 ) {
				Message.printWarning ( 1, routine,
				"Unrecognized StateCU data component for:\n\"" +
				iline + "\"" );
				Message.printWarning ( 2, routine, e2 );
				continue;
			}
			// The following are set in the constructor for
			// DataSetComponent...
			comptype = comp.getComponentType ();
			compfile = comp.getDataFileName();
			// Checks to not read in unneeded data (REVISIT - take
			// this out or keep the code so that unneeded data are
			// filtered out)?
			if ( comptype == COMP_CONTROL ) {
				// Already read it above...
				continue;
			}
			if (	(dataset.getDataSetType() == TYPE_OTHER_USES) &&
				(comptype != COMP_OTHER) ) {
				unneeded_warning += "\n" + iline;
				comp.setVisible ( false );
			}
			else if ((dataset.getDataSetType() ==
				TYPE_CLIMATE_STATIONS) &&
				(comptype != COMP_CU_LOCATIONS) &&
				(comptype != COMP_BLANEY_CRIDDLE) &&
				(comptype != COMP_CROP_CHARACTERISTICS)&&
				(comptype != COMP_CLIMATE_STATIONS) &&
				(comptype !=
					COMP_TEMPERATURE_TS_MONTHLY_AVERAGE) &&
				(comptype!= COMP_FROST_DATES_TS_YEARLY)&&
				(comptype != COMP_PRECIPITATION_TS_MONTHLY)&&
				(comptype != COMP_GIS_STATE) &&
				(comptype != COMP_GIS_DIVISIONS) &&
				(comptype != COMP_GIS_RIVERS) &&
				(comptype != COMP_GIS_CLIMATE_STATIONS)){
				unneeded_warning += "\n" + iline;
				comp.setVisible ( false );
			}
			else if ((dataset.getDataSetType() == TYPE_STRUCTURES)&&
				(comptype != COMP_CU_LOCATIONS) &&
				(comptype != COMP_BLANEY_CRIDDLE) &&
				(comptype != COMP_CROP_CHARACTERISTICS)&&
				(comptype != COMP_CLIMATE_STATIONS) &&
				(comptype !=
					COMP_TEMPERATURE_TS_MONTHLY_AVERAGE) &&
				(comptype!= COMP_FROST_DATES_TS_YEARLY)&&
				(comptype != COMP_PRECIPITATION_TS_MONTHLY)&&
				(comptype != COMP_GIS_STATE) &&
				(comptype != COMP_GIS_DIVISIONS) &&
				(comptype != COMP_GIS_RIVERS) &&
				(comptype != COMP_GIS_CLIMATE_STATIONS)&&
				(comptype != COMP_GIS_STRUCTURES)&&
				// Add...
				(comptype != COMP_CROP_PATTERN_TS_YEARLY)){
				unneeded_warning += "\n" + iline;
				comp.setVisible ( false );
			}
			else if ((dataset.getDataSetType() ==
				TYPE_WATER_SUPPLY_LIMITED)&&
				(comptype != COMP_CU_LOCATIONS) &&
				(comptype != COMP_BLANEY_CRIDDLE) &&
				(comptype != COMP_CROP_CHARACTERISTICS)&&
				(comptype != COMP_CLIMATE_STATIONS) &&
				(comptype !=
					COMP_TEMPERATURE_TS_MONTHLY_AVERAGE) &&
				(comptype!= COMP_FROST_DATES_TS_YEARLY)&&
				(comptype != COMP_PRECIPITATION_TS_MONTHLY)&&
				(comptype != COMP_GIS_STATE) &&
				(comptype != COMP_GIS_DIVISIONS) &&
				(comptype != COMP_GIS_RIVERS) &&
				(comptype != COMP_GIS_CLIMATE_STATIONS)&&
				(comptype != COMP_GIS_STRUCTURES)&&
				(comptype != COMP_CROP_PATTERN_TS_YEARLY)&&
				// Add...
				(comptype != COMP_DIVERSION_TS_MONTHLY)&&
				(comptype != COMP_WELL_PUMPING_TS_MONTHLY)&&
				(comptype!=COMP_IRRIGATION_PRACTICE_TS_YEARLY)){
				unneeded_warning += "\n" + iline;
				comp.setVisible ( false );
			}
			else if ((dataset.getDataSetType() ==
				TYPE_WATER_SUPPLY_LIMITED_BY_WATER_RIGHTS)&&
				(comptype != COMP_CU_LOCATIONS) &&
				(comptype != COMP_BLANEY_CRIDDLE) &&
				(comptype != COMP_CROP_CHARACTERISTICS)&&
				(comptype != COMP_CLIMATE_STATIONS) &&
				(comptype !=
					COMP_TEMPERATURE_TS_MONTHLY_AVERAGE) &&
				(comptype != COMP_FROST_DATES_TS_YEARLY)&&
				(comptype != COMP_PRECIPITATION_TS_MONTHLY)&&
				(comptype != COMP_GIS_STATE) &&
				(comptype != COMP_GIS_DIVISIONS) &&
				(comptype != COMP_GIS_RIVERS) &&
				(comptype != COMP_GIS_CLIMATE_STATIONS)&&
				(comptype != COMP_GIS_STRUCTURES)&&
				(comptype != COMP_CROP_PATTERN_TS_YEARLY) &&
				(comptype != COMP_DIVERSION_TS_MONTHLY)&&
				(comptype != COMP_WELL_PUMPING_TS_MONTHLY)&&
				(comptype !=
					COMP_IRRIGATION_PRACTICE_TS_YEARLY)&&
				// Add...
				(comptype != COMP_DIVERSION_RIGHTS)){
				unneeded_warning += "\n" + iline;
				comp.setVisible ( false );
			}
			else if ((dataset.getDataSetType() ==
				TYPE_RIVER_DEPLETION)&&
				(comptype != COMP_CU_LOCATIONS) &&
				(comptype != COMP_BLANEY_CRIDDLE) &&
				(comptype != COMP_CROP_CHARACTERISTICS)&&
				(comptype != COMP_CLIMATE_STATIONS) &&
				(comptype !=
					COMP_TEMPERATURE_TS_MONTHLY_AVERAGE) &&
				(comptype != COMP_FROST_DATES_TS_YEARLY)&&
				(comptype != COMP_PRECIPITATION_TS_MONTHLY)&&
				(comptype != COMP_GIS_STATE) &&
				(comptype != COMP_GIS_DIVISIONS) &&
				(comptype != COMP_GIS_RIVERS) &&
				(comptype != COMP_GIS_CLIMATE_STATIONS)&&
				(comptype != COMP_GIS_STRUCTURES)&&
				(comptype != COMP_CROP_PATTERN_TS_YEARLY)&&
				(comptype != COMP_DIVERSION_TS_MONTHLY)&&
				(comptype != COMP_WELL_PUMPING_TS_MONTHLY)&&
				(comptype !=
					COMP_IRRIGATION_PRACTICE_TS_YEARLY) &&
				(comptype != COMP_DIVERSION_RIGHTS) &&
				// Add...
				(comptype != COMP_DELAY_TABLES_MONTHLY)&&
				(comptype !=
					COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY)){
				unneeded_warning += "\n" + iline;
				comp.setVisible ( false );
			}
			// Evaluate each data component...
			if ( !read_all || !comp.isVisible() ) {
				// Not reading the actual data so skip...
				continue;
			}
			// Read the data for the object.  Some files are
			// actually not read here (e.g., spatial data) but the
			// components are added and the data can be read later.
			f = new File(compfile);
			if ( !f.isAbsolute() ) {
				compfile =	dataset.getDataSetDirectory() +
						File.separator + compfile;
			}
			// List these in the order that they are normally
			// processed/listed in StateDMI and other software...
			//
			// Climate Stations...
			//
			if ( comptype == COMP_CLIMATE_STATIONS ) {
				try {	comp.setData (
					StateCU_ClimateStation.readStateCUFile
						( compfile ) );
					dataset.getComponentForComponentType (
					COMP_CLIMATE_STATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if (	(comptype ==
					COMP_TEMPERATURE_TS_MONTHLY_AVERAGE) ||
					(comptype == 
					COMP_PRECIPITATION_TS_MONTHLY) ) {
				try {	comp.setData (
					StateMod_TS.readTimeSeriesList
						( compfile, null, null, null,
						true ) );
					dataset.getComponentForComponentType (
					COMP_CLIMATE_STATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if ( comptype == COMP_FROST_DATES_TS_YEARLY ) {
				try {	
// REVISIT (JTS - 2005-01-17)
// throwing compile errors
//					comp.setData (
//					StateCU_FrostDatesTS.readTimeSeriesList
//						( compfile, null, null,
//						null, null, true ) );
					dataset.getComponentForComponentType (
					COMP_CLIMATE_STATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			//
			// Crop Characteristics...
			//
			else if ( comptype == COMP_CROP_CHARACTERISTICS ) {
				try {	comp.setData (
					StateCU_CropCharacteristics.
					readStateCUFile ( compfile ) );
					dataset.getComponentForComponentType (
					COMP_CROP_CHARACTERISTICS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if ( comptype == COMP_BLANEY_CRIDDLE ) {
				try {	comp.setData (
					StateCU_BlaneyCriddle.readStateCUFile
						( compfile ) );
					dataset.getComponentForComponentType (
					COMP_CROP_CHARACTERISTICS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			//
			// Delay tables...
			//
			else if ( comptype == COMP_DELAY_TABLES_MONTHLY ) {
				try {	// StateCU assumes percent (0-100) for
					// values, which is indicated by the
					// -1 flag...
					comp.setData (
					StateMod_DelayTable.readStateModFile
						( compfile, true, -1 ) );
					dataset.getComponentForComponentType (
					COMP_DELAY_TABLES_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			//
			// CU Locations...
			//
			else if ( comptype == COMP_CU_LOCATIONS ) {
				try {	comp.setData (
					StateCU_Location.readStateCUFile
						( compfile ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).addComponent(
					comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if ( comptype == COMP_CROP_PATTERN_TS_YEARLY ) {
				try {	comp.setData (
					StateCU_CropPatternTS.readStateCUFile
						( compfile, null, null ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if (	comptype == 
					COMP_IRRIGATION_PRACTICE_TS_YEARLY ) {
				try {	comp.setData (
					StateCU_IrrigationPracticeTS.
						readStateCUFile
						( compfile, null, null ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if ( comptype == COMP_DIVERSION_TS_MONTHLY ) {
				try {	comp.setData (
					StateMod_TS.readTimeSeriesList
						( compfile, null, null, null,
						true ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if ( comptype == COMP_WELL_PUMPING_TS_MONTHLY ) {
				try {	comp.setData (
					StateMod_TS.readTimeSeriesList
						( compfile, null, null, null,
						true ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if ( comptype == COMP_DIVERSION_RIGHTS ) {
				try {	comp.setData (
					StateMod_DiversionRight.readStateModFile
						( compfile ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			else if (comptype==COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY){
				try {	comp.setData (
					StateCU_DelayTableAssignment.
						readStateCUFile ( compfile ) );
					dataset.getComponentForComponentType (
					COMP_CU_LOCATIONS_GROUP ).
					addComponent( comp );
				}
				catch ( Exception e2 ) {
					read_warning += "\n" + iline;
					Message.printWarning ( 2, routine, e2 );
				}
			}
			// Files not specifically handled (e.g., GIS)...
			else {	// Add to the component group but don't read in
				// the data...
				int gtype = dataset.
					lookupComponentGroupTypeForComponent
					( comptype );
				if ( gtype < 0 ) {
					Message.printWarning ( 2, routine,
					"Group for component is unknown.  Not" +
					" adding: " + comp.getComponentName() );
				}
				dataset.getComponentForComponentType ( gtype ).
					addComponent( comp );
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Unexpected error for:\n\"" + iline + "\"" );
			Message.printWarning ( 2, routine, e );
		}
	}
	if ( unneeded_warning.length() > 0 ) {
		Message.printWarning ( 2, routine,
		lookupDataSetName(dataset.getDataSetType()) +
		" data set.  Unnecessary data files will not be visible:" +
		unneeded_warning );
	}
	if ( read_warning.length() > 0 ) {
		Message.printWarning ( 1, routine,
		"Error reading data files:" + read_warning );
	}
	if ( in != null ) {
		in.close();
	}
	return dataset;
}

/**
Read a complete StateCU data set from an XML data set file.
@param filename XML data set file to read.
@param read_all If true, all the data files mentioned in the response file will
be read into memory, providing a complete data set for viewing and manipulation.
@exception Exception if there is an error reading the file.
*/
public static StateCU_DataSet readXMLFile ( String filename, boolean read_all )
throws Exception
{	/* TODO SAM 2007-03-01
	String routine = "StateCU_DataSet.readXMLFile";
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	
	need to figure out if this is in the base class.

	DOMParser parser = null;
	try {	parser = new DOMParser();
		parser.parse ( full_filename );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, 
			"Error reading StateCU Data set \"" + filename + "\"" );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( "Error reading StateCU Data set \"" +
			filename + "\"" );
	}

	// Create a new data set object...

	StateCU_DataSet dataset = new StateCU_DataSet();
	File f = new File ( full_filename );
	dataset.setDirectory ( f.getParent() );
	dataset.setDataSetFileName ( f.getName() );

	// Now get information from the document.  For now don't hold the
	// document as a data member...

	Document doc = parser.getDocument();

	// Loop through and process the document nodes, starting with the root
	// node...

	processDocumentNodeForRead ( dataset, doc );

	// Synchronize the response file with the control file (for now just
	// check - need to decide how to make bulletproof)...

	// REVISIT
	//DataSetComponent comp = dataset.getComponentForComponentType (
	//	COMP_RESPONSE );
	//if ( comp != null ) {
	//	StateCU_DataSet ds2 = readStateCUFile (
	//	comp.getDataFile(), false );
	//}

	// Compare components and response file.  Need to REVISIT this.

	// Now just read the components - the assumption is that the data set
	// components are correct for the data set but need to tighten this
	// down

	String read_warning = "";
	if ( read_all ) {
		Vector components = dataset.getComponents();
		int size = dataset.__components.size();
		String datafile = "";
		DataSetComponent comp;
		for ( int i = 0; i < size; i++ ) {
			comp = (DataSetComponent)
				components.elementAt(i);
			try {	datafile = comp.getDataFileName();
				f = new File(datafile);
				if ( !f.isAbsolute() ) {
					datafile =	dataset.getDirectory() +
							File.separator +
							datafile;
				}
				if ( comp.getComponentType() == COMP_CU_LOCATIONS ) {
					comp.setData (
					StateCU_Location.readStateCUFile(
					datafile));
				}
				else if (comp.getType() ==
					COMP_CROP_CHARACTERISTICS) {
					comp.setData (
					StateCU_CropCharacteristics.
					readStateCUFile( datafile));
				}
				else if (comp.getType() ==
					COMP_BLANEY_CRIDDLE) {
					comp.setData (
					StateCU_BlaneyCriddle.readStateCUFile(
					datafile));
				}
				else if (comp.getType() ==
					COMP_CLIMATE_STATIONS) {
					comp.setData (
					StateCU_ClimateStation.readStateCUFile(
					datafile));
				}
			}
			catch ( Exception e ) {
				read_warning += "\n" + datafile;
				Message.printWarning ( 2, routine, e );
			}
		}
	}
	else {	// Read the control file???
	}
	if ( read_warning.length() > 0 ) {
		Message.printWarning ( 1, routine,
		"Error reading data files:" + read_warning );
	}

	return dataset;
*/
	return null;
}

/**
Performs the check file setup and calls code to check component.  Also sets
the check file to the list in the GUI.  If problems are encountered when
running data checks are added to the check file.
@param int type - StateModComponent type.
*/
public String runComponentChecks( int type, String fname, 
String commands, String header )
{
	String check_file = "";
	CheckFile chk = new CheckFile( fname, commands );
	chk.addToHeader( header );
	StateCU_ComponentDataCheck check = new StateCU_ComponentDataCheck(
	type, chk, this);
	// Run the data checks for the component and retrieve the
	// finalized check file
	CheckFile final_check = check.checkComponentType( null );
	try {
		final_check.finalizeCheckFile();
		check_file = final_check.toString();
	} catch (Exception e) {
		Message.printWarning(2, "StateDMI_Processor.runComponentChecks",
		"Check file: " +  final_check.toString() + " couldn't be finalized.");
		Message.printWarning(3, "StateDMI_Processor.runComponentChecks",
		e);
	}
	return check_file;
}

/**
Set the administration number for CU by priority.
@param admin_num the administration number for CU by priority.
*/
public void setAdminNumForCUByPriority ( double admin_num )
{	__admin_num = admin_num;
}

/**
Set a comment.
@param comment Comment to set.
@param pos Comment index (0 - 2).
*/
public void setComment ( String comment, int pos )
{	if ( (pos < 0) || (pos > 2) ) {
		return;
	}
	__comments[pos] = comment;
}

/**
Set whether to create StateMod output.
@param ddcsw Indicate whether to create StateMod output.
*/
public void setDdcsw ( int ddcsw )
{	__ddcsw = ddcsw;
}

/**
Set the value of flag1 (CU method).
@param flag1 the value of flag1.
*/
public void setFlag1 ( int flag1 )
{	__flag1 = flag1;
}

/**
Set the value of iclim (climate stations or structures data set).
@param iclim the value of iclim.
*/
public void setIclim ( int iclim )
{	__iclim = iclim;
}

/**
Set the value of idaily (daily/monthy switch).
@param idaily the value of idaily.
*/
public void setIdaily ( int idaily )
{	__idaily = idaily;
}

/**
Set the value of iflood (handle groundwater use).
@param iflood the value of iflood.
*/
public void setIflood ( int iflood )
{	__iflood = iflood;
}

/**
Set the iprtysm flag (operate soil moisture by proration).
@param iprtysm the iprtysm flag.
*/
public void setIprtysm ( int iprtysm )
{	__iprtysm = iprtysm;
}

/**
Set the value of ism (consider soil moisture?).
@param ism the value of ism.
*/
public void setIsm ( int ism )
{	__ism = ism;
}

/**
Set the value of isupply (water supply option).
@param isupply the value of isupply.
*/
public void setIsupply ( int isupply )
{	__isupply = isupply;
}

/**
Set the starting year for the data set.
@param nyr1 the starting year for the data set.
*/
public void setNyr1 ( int nyr1 )
{	__nyr1 = nyr1;
}

/**
Set the ending year for the data set.
@param nyr2 the ending year for the data set.
*/
public void setNyr2 ( int nyr2 )
{	__nyr2 = nyr2;
}

/**
Set the initial soil moisture content for junior parcels (fraction of capacity).
@param pjunmo the initial soil moisture content for junior parcels.
*/
public void setPjunmo ( double pjunmo )
{	__pjunmo = pjunmo;
}

/**
Set the initial soil moisture content for senior parcels (fraction of capacity).
@param pothmo the initial soil moisture content for senior parcels.
*/
public void setPothmo ( double pothmo )
{	__pothmo = pothmo;
}

/**
Set the initial soil moisture content for other parcels (fraction of capacity).
@param psenmo the initial soil moisture content for other parcels.
*/
public void setPsenmo ( double psenmo )
{	__psenmo = psenmo;
}

/**
Set the rn_xco flag (monthly precipitation method).
@param rn_xco the rn_xco flag.
*/
public void setRn_xco ( int rn_xco )
{	__rn_xco = rn_xco;
}

/**
Set the sout flag (input summary type).
@param sout the sout flag.
*/
public void setSout ( int sout )
{	__sout = sout;
}

/**
Set the typout flag (output summary type).
@param typout the typout flag.
*/
public void setTypout ( int typout )
{	__typout = typout;
}

/**
Write a StateCU_Control object to a file.  The filename is adjusted
to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param control A StateCU_Control object to write.
@new_comments Comments to add to the top of the file.  Specify as null if no
comments are available.
@exception IOException if there is an error writing the file.
*/
/* TODO SAM Evaluate whether needed
private void writeStateCUControlFile (	String filename_prev,
					String filename,
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
	String cmnt = "#>";
	// Missing data handled by formatting all as strings...

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Control File" );
	out.println ( cmnt );
	out.println ( cmnt +
		"  Values are free-format except where noted." );
	out.println ( cmnt );
	out.println ( cmnt +
		"  Notes       comment:  First three non-comment lines are "+
					" general notes");
	out.println ( cmnt +
		"  Begin/End nyr1,nyr2:  4-digit years for simulation period.");
	out.println ( cmnt +
		"                        The time series files can be longer.");
	out.println ( cmnt +
		"  CUMethod      flag1:  CU Method" );
	out.println ( cmnt +
		"                        1 = Blaney-Criddle" );
	out.println ( cmnt +
		"                        2 = Other uses (non-agriculture)" );
	out.println ( cmnt +
		"                        3 = Penman-Monteith" );
	out.println ( cmnt +
		"                        4 = Hargreaves" );
	out.println ( cmnt +
		"  PrecipMeth   RN_XCO:  Monthly precipitation method." );
	out.println ( cmnt +
		"                        1 = Soil Conservation Service" );
	out.println ( cmnt +
		"                        2 = United States Bureau of Rec." );
	out.println ( cmnt +
		"  DataSetType   iclim:  Data set type." );
	out.println ( cmnt +
		"                        0 = CU at climate stations (unit" +
					"of area)" );
	out.println ( cmnt +
		"                        1 = CU at structures (ditches/wells)");
	out.println ( cmnt +
		"  WaterSupply isupply:  Water supply option (levels are" +
		"incremental)." );
	out.println ( cmnt +
		"                        0 = none");
	out.println ( cmnt +
		"                        1 = supply limited");
	out.println ( cmnt +
		"                        2 = water rights considered");
	out.println ( cmnt +
		"                        3 = return flows considered");
	out.println ( cmnt +
		"                        4 = groundwater considered");
	out.println ( cmnt +
		"  InputSummary   sout:  Input summary flag." );
	out.println ( cmnt +
		"                        0 = output basic summary" );
	out.println ( cmnt +
		"                        1 = output detailed summary" );
	out.println ( cmnt +
		"  SoilMoisture   isim:  Soil moisture flag." );
	out.println ( cmnt +
		"                        0 = do not consider soil moisture" );
	out.println ( cmnt +
		"                        1 = consider user-initialized soil" +
					" moisture" );
	out.println ( cmnt +
		"                        2 = consider run presimulation to" +
					" initialize" );
	out.println ( cmnt +
		"  SoilMoist0   p***mo:  Initial soil moisture content for." );
	out.println ( cmnt +
		"                        senior, junior, other parcels" );
	out.println ( cmnt +
		"                        (fraction of capacity), format " +
					"3f5.0.");
	out.println ( cmnt +
		"  SMProrate   iprtysm:  0 = operate soil moisture by " +
					"proration");
	out.println ( cmnt +
		"                        1 = operate by priority" );
	out.println ( cmnt +
		"                        sprinkler separately." );
	out.println ( cmnt +
		"                        (REVISIT possible values?)" );
	out.println ( cmnt +
		" Output        typout:  Output summary flag (format i5).");
	out.println ( cmnt +
		"                        0 = output basic summary" );
	out.println ( cmnt +
		"                        1 = +irrigation water requirement" );
	out.println ( cmnt +
		"                            +water supply limited" );
	out.println ( cmnt +
		"                        2 = +water budget" );
	out.println ( cmnt +
		"                        3 = +water budget by structure" );
	out.println ( cmnt +
		"  IrrigMethod  iflood:  Output groundwater use by flood and" );
	out.println ( cmnt +
		"                        sprinkler separately." );
	out.println ( cmnt +
		"                        (REVISIT possible values?)" );
	out.println ( cmnt +
		"  StateMod      ddcsw:  StateMod output format switch" );
	out.println ( cmnt +
		"                        0 = no" );
	out.println ( cmnt +
		"                        1 = yes" );
	out.println ( cmnt +
		"  Daily        idaily:  Daily data switch" );
	out.println ( cmnt +
		"                        0 = (REVISIT what is zero?)" );
	out.println ( cmnt +
		"                        1 = daily diversions with daily" +
					" admin" );
	out.println ( cmnt +
		"                        2 = daily diversions with monthly" +
					" admin" );
	out.println ( cmnt +
		"                        3 = daily diversions with single" +
					" admin" );
	out.println ( cmnt +
		"                        4 = monthly diversions with monthly"+
					" admin" );
	out.println ( cmnt +
		"                        5 = monthly diversions with single"+
					" admin" );
	out.println ( cmnt +
		"  AdminNum           :  Administration number for CU by" +
					" priority." );
	out.println ( cmnt );
	out.println ( cmnt +	
	" StationID  Lat   Elev            Region1      Region2  " +
	"      StationName" );
	out.println ( cmnt +	
	"--------------------------------------------------------------------" +
	"--------------------" );
	out.println ( cmnt + "EndHeader" );

	out.println ( getComment(0) );
	out.println ( getComment(1) );
	out.println ( getComment(2) );
	// Just print all, even if missing...
	out.println (	StringUtil.formatString( getNyr1(),"%4d") + " " +
		StringUtil.formatString(getNyr2(),"%4d") );
	out.println ( getFlag1() );
	out.println ( getRn_xco() );
	out.println ( getIclim() );
	out.println ( getIsupply() );
	out.println ( getSout() );
	out.println ( getIsm() );
	out.println (	StringUtil.formatString( getPsenmo(),"%5.0f") +
			StringUtil.formatString( getPjunmo(),"%5.0f") +
			StringUtil.formatString( getPothmo(),"%5.0f") );
	out.println ( getTypout() );
	out.println ( getIprtysm() );
	out.println ( getIflood() );
	out.println ( getDdcsw() );
	out.println ( getIdaily() );
	out.println ( StringUtil.formatString(
			getAdminNumForCUByPriority(),"%11.0f"));
	out.flush();
	out.close();
	out = null;
}
*/

/*
Write the data set to an XML file.  The filename is adjusted to the
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A Vector of StateCU_Location to write.
@new_comments Comments to add to the top of the file.  Specify as null if no
comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeXMLFile (	String filename_prev, String filename,
					StateCU_DataSet dataset,
					String [] new_comments )
throws IOException
{	/* REVISIT - need to figure out if this is in the base class
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir (
		filename_prev );
	if ( !StringUtil.endsWithIgnoreCase(filename,".xml") ) {
		filename = filename + ".xml";
	}
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
		new_comments, comment_str, ignore_comment_str, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" +
			full_filename + "\"" );
	}
	writeDataSetToXMLFile ( dataset, out );
	out.flush();
	out.close();
	out = null;
	*/
}

/**
Write a data set to an opened XML file.
@param data A StateCU_DataSet to write.
@param out output PrintWriter.
@exceptoin IOException if an error occurs.
*/
/* TODO SAM 2007-03-01 Evaluate whether needed
private static void writeDataSetToXMLFile (	StateCU_DataSet dataset,
						PrintWriter out )
throws IOException
{	// Start XML tag...
	out.println ("<!--" );
	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Data Set (XML) File" );
	out.println ( cmnt );
	out.println ( cmnt + "EndHeader" );
	out.println ("-->" );

	out.println ("<StateCU_DataSet " +
		"Type=\"" + lookupTypeName(dataset.getDataSetType()) + "\"" +
		"BaseName=\"" + dataset.getBaseName() + "\"" +
		">" );

	int num = 0;
	Vector data_Vector = dataset.getComponents();
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	String indent1 = "  ";
	String indent2 = indent1 + indent1;
	for ( int i = 0; i < num; i++ ) {
		comp = (DataSetComponent)data_Vector.elementAt(i);
		if ( comp == null ) {
			continue;
		}
		out.println ( indent1 + "<DataSetComponent" );

		out.println ( indent2 + "Type=\"" +
			DataSetComponent.lookupComponentName(
			comp.getType()) + "\"" );
		out.println ( indent2 + "DataFile=\"" +
			comp.getDataFileName() + "\"" );
		out.println ( indent2 + "ListFile=\"" +
			comp.getListFileName() + "\"" );
		out.println ( indent2 + "CommandsFile=\"" +
			comp.getCommandsFileName() + "\"" );
		out.println ( indent2 + ">" );

		out.println ( indent1 + "</DataSetComponent>");
	}
	out.println ("</StateCU_DataSet>" );
}
*/

} // End StateCU_DataSet
