//-----------------------------------------------------------------------------
// StateMod_DataSet - an object to manage data in a StateMod data set.
//-----------------------------------------------------------------------------
// History:
//
// 2003-05-05	Steven A. Malers, RTi	Created class.
// 2003-06-04	J. Thomas Sapienza, RTi	Adapted from CUDataSet.
// 2003-06-10	JTS, RTI		Rolled in SAM's changes to the 
//					StateCU_DataSet class.
// 2003-06-20	JTS, RTi		Implement ProcessListener methods.
// 2003-06-23	JTS, RTi		Code now reads monthly time series.
// 2003-06-26	JTS, RTi		Revisions based on SAM's review.
// 2003-07-13	SAM, RTi		Define the arrays necessary to use the
//					generic RTi.Util.IO.DataSet class as a
//					base class.  Note that these used to be
//					in StateMod_DataSetComponent but that
//					class is no longer needed.  Also clean
//					up the component names to be more
//					explicit.
// 2003-07-15	JTS, RTi		Adapted to use SAM's new design for the
//					DataSet and DataSetComponent.
// 2003-07-30	SAM, RTi		* Change COMP_STREAM_STATIONS to
//					  COMP_RIVER_STATIONS (agrees better
//					  with the StateMod documentation).
//					* Previously the components were not
//					  being added to the groups - all were
//					  being added at the same level.  Fix.
//					* Remove printStackTrace() calls since
//					  Message warnings are in place.
//					* Set the working directory to that of
//					  the response file.
//					* Do not open the GeoView project when
//					  reading the response file because
//					  StateDMI and other applications won't
//					  behave the same.
//					* Remove __names[] since file names are
//					  now stored with data set components.
//					* Merge StateMod_Response and
//					  StateMod_Control into this class since
//					  they need to be processed in an
//					  integrated fashion anyhow - this is
//					  consistent with StateCU.
//					* Verify that the ProcessListeners are
//					  working in StateMod GUI.
// 2003-08-14	SAM, RTi		* Split river data into River Data
//					  and Baseflow Data.
//					* Add getSummary() - previously in
//					  StateMod GUI.
// 2003-08-18	SAM, RTi		Time series methods for
//					StateMod_Reservoir have been renamed.
// 2003-08-25	SAM, RTi		Add COMP_OTHER_NODE for use with the
//					GUI but it is not a true data set
//					component and is not included in the
//					data set configuration.
// 2003-08-27	SAM, RTi		* All files should now be read, except
//					  for the San Juan recovery and the
//					  StateCU file for soil moisture.
//					* The baseflow time series for old data
//					  sets are properly handled by splitting
//					  into the new river station and river
//					  baseflow station time series lists.
// 2003-08-29	SAM, RTi		* In agreement with Ray Bennett's email
//					  of today, it is safest to split the
//					  delay tables in to monthly and daily
//					  groups.  This is actually the easiest
//					  solution also because existing code
//					  can be reused.
//					* Because control data are initialized
//					  to missing, for special checks like
//					  iwell, need to check for missing
//					  before evaluating the parameter.
//					* Default factor control data to
//					  appropriate values.
// 2003-09-08	SAM, RTi		readFileLine() method was returning
//					the full path, which was redundant.
// 2003-09-10	JTS, RTi		* Added getComponentGroupNumbers for 
//					  use in the response file table model.
//					* Added getComponentTags for use in
//					  use in the reponse file table model.
// 2003-09-11	SAM, RTi		* Add array __statemod_file_properties
//					  as a reference for the properties.
//					* Add getStateModFileProperty (int).
//					* Change component definitions for
//					  river and river baseflow based on
//					  discussions with Ray Bennett.
//					* Change the __component_tags array to
//					  __component_file_extensions.
//					* Change getComponentTags() to
//					  getComponentFileExtensions().
//					* Add getComponentFileExtension(int).
//					* Add checkForFreeFormat() and use the
//					  result in readStateModFile() to handle
//					  old and new response files.
//					* Set defaults for newer advanced
//					  control data to non-missing so that
//					  reasonable defaults will be saved for
//					  older data sets that migrate forward.
//					* Change cdem to icondem.
// 2003-09-12	SAM, RTi		* Ray decided not to have a separate
//					  file for the baseflow time series so
//					  refer to the same file for both
//					  StreamGage and StreamEstimate
//					  stations.  The GUI will NEVER allow
//					  edits to the baseflow files because
//					  the data are created by StateMod.
// 2003-09-18	SAM, RTi		* Make sure that the free format read
//					  is working with Ray's ex50 data set.
//					* Add getDataObjectDetails() for use in
//					  the GUI.
// 2003-10-10	SAM, RTi		Updated after 2003-10-10 progress
//					meeting.
//					* Change component name from
//					  "Irrigation" to "Consumptive".  Change
//					  COMP_IRRIGATION_WATER_REQUIREMENT...
//					  to COMP_CONSUMPTIVE_WATER_REQUIREMENT.
//					* Add a copy constructor for use in the
//					  response display, to detect edits in
//					  the window.
//					* Set the stream estimate baseflow
//					  components to hidden since they are
//					  shared with the normal baseflow files.
//					* Add isFreeFormat() for use in the
//					  response display.
//					* Make private data __xxx.  Previously
//					  only had _xx, which was inconsistent
//					  with RTi standards.
//					* Add __component_ts_data_types to help
//					  assign data types for time series.
//					  Add lookupTimeSeriesDataType() to
//					  return the appropriate value and use
//					  this method rather than hard-coding
//					  time series data types.
//					* Add getModifiedDataSummary() to help
//					  track down what is dirty in a data
//					  set.
//					* Change writeStateModResponseFile to
//					  writeStateModFile() and enable the
//					  code to write.
// 2003-10-21	SAM, RTi		* Change demand override average monthly
//					  to demand average monthly.
//					* Add lookupTimeSeriesDataUnits() to
//					  centralize the assignment of units.
// 2003-10-23	SAM, RTi		Add initializeDataFileNames() for use
//					when creating a new data set from
//					scratch (e.g., the GUI).
// 2003-10-26	SAM, RTi		* Add getGraphDataTypes() for use with
//					  graphing tools.
//					* Add supporting static data for the
//					  previous item.
//					* Add getComponentDataFileNameFrom
//					  TimeSeriesDataType().
//					* Add lookupTimeSeriesFileExtension().
//					* Add getDataStart(), getDataEnd(),
//					  getRunStart(), and getRunEnd(),
//					  which return DateTime, to simplify
//					  other code.
// 2004-03-23	SAM, RTi		* Minor changes to some component names
//					  based on work in StateDMI.
// 2004-07-09	SAM, RTi		* Change units for instream flow to be
//					  "CFS" in all cases, as per data
//					  sets.  Specific applications may
//					  override with AF/M.
// 2004-08-16	JTS, RTi		* Uncommented writeStateModControlFile()
//					* Added code for writing the free-format
//					  response file.
// 2004-08-25	JTS, RTi		Revised the getSummary() method.
// 2005-01-18	JTS, RTi		Added a static method 
//					getComponentName() to return the names
//					of data set components.
// 2005-01-19	SAM, RTi		* The base class already had
//					  lookupComponentName() so change
//					  the getComponentName() method added
//					  yesterday to lookupComponentName().
//					* Add several sub-component's like
//					  diversion station delay tables and
//					  use the above overloaded method to
//					  return the name.  The sub-components
//					  are mainly used for displays.
//					* Clean up the definition of the
//					  component numbers to make the code
//					  more readable - mainly shift things to
//					  the left and remove obsolete
//					  commments.
// 2005-03-30	SAM, RTi		* Change some component names to have
//					  "TS" - previously an indication of
//					  time series was not given for well
//					  and instream flow time series.
// 2006-08-22	SAM, RTi		* Add "isig" data member.
// 2006-08-22	SAM, RTi		* Add call time series and plan
//					  stations.
// 2007-01-01	SAM, RTi		* Add additional check for water rights
//					  to verify sum is the same as well
//					  station capacity.
// 2007-02-18	SAM, RTi		Clean up code based on Eclipse feedback.
// 2007-04-10	Kurt Tometich, RTi		Added a runComponentChecks method
//									for support of new check file 
//									implemenation.
//-----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;

import DWR.DMI.HydroBaseDMI.HydroBase_NodeNetwork;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.IO.CheckFile;
import RTi.Util.IO.DataSet;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessListener;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.StopWatch;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
This StateMod_DataSet class manages data components in a StateMod data set, 
essentially managing the list of components from the response file.
Typically, each component corresponds to a file.  A list of components is
maintained and is displayed by StateMod_DataSetManager.
*/
public class StateMod_DataSet extends DataSet
{

// REVISIT - are these really needed?  Are they consistent with some StateMod
// choices?

/**
Calendar year. (January - December) Potential parameter in setCyrl.
*/
public static final int SM_CYR = 1;

/**
Water year. (October - September) Potential parameter in setCyrl.
*/
public static final int SM_WYR = 2;
/**
Irrigation year. (November - October) Potential parameter in setCyrl.
*/
public static final int SM_IYR = 3;

/**
Cubic-feet per second.  Potential parameter in setIresop.
*/
public static final int SM_CFS = 1;  
/**
Acre-feet per second.  Potential parameter in setIresop.
*/
public static final int SM_ACFT = 2;
/**
Kilo Acre-feet per second.  Potential parameter in setIresop.
*/
public static final int SM_KACFT = 3;
/**
CFS for daily, ACFT for monthly.  Potential parameter in setIresop.
*/
public static final int SM_CFS_ACFT = 4;
/**
Cubic meters per second.  Potential parameter in setIresop.
*/
public static final int SM_CMS = 5;

/**
Monthly data.  Potential parameter in setMoneva.
*/
public static final int SM_MONTHLY = 0;
/**
Average data.  Potential parameter in setMoneva.
*/
public static final int SM_AVERAGE = 1;

/**
Average data.  Potential parameter in setIopflo.
*/
public static final int SM_TOT = 1;
/**
Gains data.  Potential parameter in setIopflo.
*/
public static final int SM_GAINS = 2;

public final int WAIT = 0;
public final int READY = 1;

private Vector __processListeners = null;
private boolean __tsAreRead = false;		// Should time series be read
						// when reading the data set?
private boolean __is_free_format = false;	// Is the response file free
						// format?

public final String BLANK_FILE_NAME = "";	// String indicating blank file
						// name - allowed to be a
						// duplicate.

private final String __ESTIMATED = "Estimated";	// Appended to some daily time
						// series data types to indicate
						// an estimated time series.

// REVISIT - SAM - StateMod data set types are not the same as StateCU and may
// not be needed at all.

/**
The StateMod data set type is unknown.
*/
public static final int TYPE_UNKNOWN = 0;
public static final String NAME_UNKNOWN = "Unknown";

// REVISIT - Start SAM new definitions (indent to illustrate groups).

public final static int COMP_UNKNOWN = -1;	// Use for initialization, if
						// needed
public final static int COMP_OTHER_NODE = -5;	// Used when defining other
						// nodes in the network, via the
						// GUI.

// The following should be sequential from 0 because they have lookup positions
// in DataSet arrays.
//
// Some of the following values are for sub-components (e.g., delay table
// assignment for diversions).  These are typically one-to-many data items that
// are managed with a component but may need to be displayed separately.  The
// sub-components have numbers that are the main component*100 + N.  These
// values are checked in methods like lookupComponentName() but do not have
// sequential arrays.
//
// REVISIT SAM 2005-01-19 - Evaluate whether sub-components should be handled
// in the arrays.

public final static int
	COMP_CONTROL_GROUP = 0,
		COMP_RESPONSE = 1,
		COMP_CONTROL = 2,
		COMP_OUTPUT_REQUEST = 3,
	
	COMP_STREAMGAGE_GROUP = 4,
		COMP_STREAMGAGE_STATIONS = 5,
		COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY = 6,
		COMP_STREAMGAGE_HISTORICAL_TS_DAILY = 7,
		COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY = 8,
		COMP_STREAMGAGE_BASEFLOW_TS_DAILY = 9,

	COMP_DELAY_TABLE_MONTHLY_GROUP = 10,
		COMP_DELAY_TABLES_MONTHLY = 11,

	COMP_DELAY_TABLE_DAILY_GROUP = 12,
		COMP_DELAY_TABLES_DAILY = 13,

	COMP_DIVERSION_GROUP = 14,
		COMP_DIVERSION_STATIONS = 15,
			COMP_DIVERSION_STATION_DELAY_TABLES = 1501,
			COMP_DIVERSION_STATION_COLLECTIONS = 1502,
		COMP_DIVERSION_RIGHTS = 16,
		COMP_DIVERSION_TS_MONTHLY = 17,
		COMP_DIVERSION_TS_DAILY = 18,
		COMP_DEMAND_TS_MONTHLY = 19,
		COMP_DEMAND_TS_OVERRIDE_MONTHLY = 20,
		COMP_DEMAND_TS_AVERAGE_MONTHLY = 21,
		COMP_DEMAND_TS_DAILY = 22,
		COMP_IRRIGATION_PRACTICE_TS_YEARLY = 23,
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY = 24,
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY = 25,
		COMP_SOIL_MOISTURE = 26,

	COMP_PRECIPITATION_GROUP = 27,
		COMP_PRECIPITATION_TS_MONTHLY = 28,

	COMP_EVAPORATION_GROUP = 29,
		COMP_EVAPORATION_TS_MONTHLY = 30,

	COMP_RESERVOIR_GROUP = 31,
		COMP_RESERVOIR_STATIONS = 32,
			COMP_RESERVOIR_STATION_ACCOUNTS = 3201,
			COMP_RESERVOIR_STATION_PRECIP_STATIONS = 3202,
			COMP_RESERVOIR_STATION_EVAP_STATIONS = 3203,
			COMP_RESERVOIR_STATION_CURVE = 3204,
			COMP_RESERVOIR_STATION_COLLECTIONS = 3205,
		COMP_RESERVOIR_RIGHTS = 33,
		COMP_RESERVOIR_CONTENT_TS_MONTHLY = 34,
		COMP_RESERVOIR_CONTENT_TS_DAILY = 35,
		COMP_RESERVOIR_TARGET_TS_MONTHLY = 36,
		COMP_RESERVOIR_TARGET_TS_DAILY = 37,

	COMP_INSTREAM_GROUP = 38,
		COMP_INSTREAM_STATIONS = 39,
		COMP_INSTREAM_RIGHTS = 40,
		COMP_INSTREAM_DEMAND_TS_MONTHLY = 41,
		COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY = 42,
		COMP_INSTREAM_DEMAND_TS_DAILY = 43,

	COMP_WELL_GROUP = 44,
		COMP_WELL_STATIONS = 45,
			COMP_WELL_STATION_DELAY_TABLES = 4501,
			COMP_WELL_STATION_DEPLETION_TABLES = 4502,
			COMP_WELL_STATION_COLLECTIONS = 4503,
		COMP_WELL_RIGHTS = 46,
		COMP_WELL_PUMPING_TS_MONTHLY = 47,
		COMP_WELL_PUMPING_TS_DAILY = 48,
		COMP_WELL_DEMAND_TS_MONTHLY = 49,
		COMP_WELL_DEMAND_TS_DAILY = 50,

	COMP_PLAN_GROUP = 51,
		COMP_PLANS = 52,

	COMP_STREAMESTIMATE_GROUP = 53,
		COMP_STREAMESTIMATE_STATIONS = 54,
		COMP_STREAMESTIMATE_COEFFICIENTS = 55,
		COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY = 56,
		COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY = 57,

	COMP_RIVER_NETWORK_GROUP = 58,
		COMP_RIVER_NETWORK = 59,	// StateMod RIN
		COMP_NETWORK = 60,		// HydroBase_NodeNetwork

	COMP_OPERATION_GROUP = 61,
		COMP_OPERATION_RIGHTS = 62,
		COMP_CALL_TS_DAILY = 63,

	COMP_SANJUAN_GROUP = 64,
		COMP_SANJUAN_RIP = 65,

	COMP_GEOVIEW_GROUP = 66,
		COMP_GEOVIEW = 67;

// The data set component names, including the component groups.  Subcomponent
// names are defined after this array and are currently treated as special
// cases.
private static String[] __component_names = {
	"Control Data",
		"Response",
		"Control",
		"Output Request",

	"Stream Gage Data",
		"Stream Gage Stations",
		"Stream Gage Historical TS (Monthly)",
		"Stream Gage Historical TS (Daily)",
		"Stream Gage Base TS (Monthly)",
		"Stream Gage Base TS (Daily)",

	"Delay Table (Monthly) Data",
		"Delay Tables (Monthly)",

	"Delay Table (Daily) Data",
		"Delay Tables (Daily)",

	"Diversion Data",
		"Diversion Stations",
		"Diversion Rights",
		"Diversion Historical TS (Monthly)",
		"Diversion Historical TS (Daily)",
		"Diversion Demand TS (Monthly)",
		"Diversion Demand TS Override (Monthly)",
		"Diversion Demand TS (Average Monthly)",
		"Diversion Demand TS (Daily)",
		"Irrigation Practice TS (Yearly)",
		"Consumptive Water Requirement TS (Monthly)",
		"Consumptive Water Requirement TS (Daily)",
		"Soil Moisture",
		
	"Precipitation Data",
		"Precipitation Time Series (Monthly)",

	"Evaporation Data",
		"Evaporation Time Series (Monthly)",
	
	"Reservoir Data",
		"Reservoir Stations",
		"Reservoir Rights",
		"Reservoir Content TS, End of Month (Monthly)",
		"Reservoir Content TS, End of Day (Daily)",
		"Reservoir Target TS (Monthly)",
		"Reservoir Target TS (Daily)",
	
	"Instream Flow Data",
		"Instream Flow Stations",
		"Instream Flow Rights",
		"Instream Flow Demand TS (Monthly)",
		"Instream Flow Demand TS (Average Monthly)",
		"Instream Flow Demand TS (Daily)",

	"Well Data",
		"Well Stations",
		"Well Rights",
		"Well Historical Pumping TS (Monthly)",
		"Well Historical Pumping TS (Daily)",
		"Well Demand TS (Monthly)",
		"Well Demand TS (Daily)",

	"Plan Data",
		"Plans",
	
	"Stream Estimate Data",
		"Stream Estimate Stations",
		"Stream Estimate Coefficients",
		"Stream Estimate Base TS (Monthly)",
		"Stream Estimate Base TS (Daily)",

	"River Network Data",
		"River Network",
		"Network (Graphical)",	// RTi version (behind the scenes)
		
	"Operational Data",
		"Operational Rights",
		"Call Time Series (Daily)",

	"San Juan Sediment Recovery Plan Data",
		"San Juan Sediment Recovery Plan",
	
	"Spatial Data",
		"GeoView Project"
	};

// Subcomponent names used with lookupComponentName().  These are special
// cases for labels and displays but the data are managed with a component
// listed above.  Make private to force handling through lookup methods.
private final static String
	__COMPNAME_DIVERSION_STATION_DELAY_TABLES =
		"Diversion Station Delay Table Assignment",
	__COMPNAME_DIVERSION_STATION_COLLECTIONS =
		"Diversion Station Collection Definitions",
	__COMPNAME_RESERVOIR_STATION_ACCOUNTS =
		"Reservoir Station Accounts",
	__COMPNAME_RESERVOIR_STATION_PRECIP_STATIONS =
		"Reservoir Station Precipitation Stations",
	__COMPNAME_RESERVOIR_STATION_EVAP_STATIONS =
		"Reservoir Station Evaporation Stations",
	__COMPNAME_RESERVOIR_STATION_CURVE =
		"Reservoir Station Content/Area/Seepage",
	__COMPNAME_RESERVOIR_STATION_COLLECTIONS =
		"Reservoir Station Collection Definitions",
	__COMPNAME_WELL_STATION_DELAY_TABLES =
		"Well Station Delay Table Assignment",
	__COMPNAME_WELL_STATION_DEPLETION_TABLES =
		"Well Station Depletion Table Assignment",
	__COMPNAME_WELL_STATION_COLLECTIONS =
		"Well Station Collection Definitions";

private static int[] __component_types = {
	COMP_CONTROL_GROUP,
		COMP_RESPONSE,
		COMP_CONTROL,
		COMP_OUTPUT_REQUEST, 

	COMP_STREAMGAGE_GROUP,
		COMP_STREAMGAGE_STATIONS,
		COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY,
		COMP_STREAMGAGE_HISTORICAL_TS_DAILY,
		COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY,
		COMP_STREAMGAGE_BASEFLOW_TS_DAILY,
	
	COMP_DELAY_TABLE_MONTHLY_GROUP,
		COMP_DELAY_TABLES_MONTHLY,

	COMP_DELAY_TABLE_DAILY_GROUP,
		COMP_DELAY_TABLES_DAILY,
		
	COMP_DIVERSION_GROUP,
		COMP_DIVERSION_STATIONS,
		COMP_DIVERSION_RIGHTS,
		COMP_DIVERSION_TS_MONTHLY,
		COMP_DIVERSION_TS_DAILY,
		COMP_DEMAND_TS_MONTHLY,
		COMP_DEMAND_TS_OVERRIDE_MONTHLY,
		COMP_DEMAND_TS_AVERAGE_MONTHLY,
		COMP_DEMAND_TS_DAILY,
		COMP_IRRIGATION_PRACTICE_TS_YEARLY,
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY,
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY,
		COMP_SOIL_MOISTURE,

	COMP_PRECIPITATION_GROUP,
		COMP_PRECIPITATION_TS_MONTHLY,

	COMP_EVAPORATION_GROUP,
		COMP_EVAPORATION_TS_MONTHLY,

	COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_STATIONS,
		COMP_RESERVOIR_RIGHTS,
		COMP_RESERVOIR_CONTENT_TS_MONTHLY,
		COMP_RESERVOIR_CONTENT_TS_DAILY,
		COMP_RESERVOIR_TARGET_TS_MONTHLY,
		COMP_RESERVOIR_TARGET_TS_DAILY,

	COMP_INSTREAM_GROUP,
		COMP_INSTREAM_STATIONS,
		COMP_INSTREAM_RIGHTS,
		COMP_INSTREAM_DEMAND_TS_MONTHLY,
		COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY,
		COMP_INSTREAM_DEMAND_TS_DAILY,

	COMP_WELL_GROUP,
		COMP_WELL_STATIONS,
		COMP_WELL_RIGHTS,
		COMP_WELL_PUMPING_TS_MONTHLY,
		COMP_WELL_PUMPING_TS_DAILY,
		COMP_WELL_DEMAND_TS_MONTHLY,
		COMP_WELL_DEMAND_TS_DAILY,

	COMP_PLAN_GROUP,
		COMP_PLANS,

	COMP_STREAMESTIMATE_GROUP,
		COMP_STREAMESTIMATE_STATIONS,
		COMP_STREAMESTIMATE_COEFFICIENTS,
		COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY,
		COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY,

	COMP_RIVER_NETWORK_GROUP,
		COMP_RIVER_NETWORK,
		COMP_NETWORK,

	COMP_OPERATION_GROUP,
		COMP_OPERATION_RIGHTS,
		COMP_CALL_TS_DAILY,

	COMP_SANJUAN_GROUP,
		COMP_SANJUAN_RIP,

	COMP_GEOVIEW_GROUP,
		COMP_GEOVIEW
};

/**
This array indicates the default file extension to use with each component.
These extensions can be used in file choosers.
*/
private static String[] __component_file_extensions = {
	"Control Group",
		"rsp",
		"ctl",
		"out",

	"Stream Gage Group",
		"ris",
		"rih",
		"riy",
		"rim",
		"rid",
	
	"Delay Tables (Monthly) Group",
		"dly",

	"Delay Tables (Daily) Group",
		"dld",
		
	"Diversion Group",
		"dds",
		"ddr",
		"ddh",
		"ddy",
		"ddm",
		"ddo",
		"dda",
		"ddd",
		"ipy",
		"iwr",
		"iwd",
		"par",
	
	"Precipitation Group",
		"pre",
		
	"Evaporation Group",
		"eva",

	"Reservoir Group",
		"res",
		"rer",
		"eom",
		"eoy",
		"tar",
		"tad",
	
	"Instream Group",
		"ifs",
		"ifr",
		"ifm",
		"ifa",		
		"ifd",
	
	"Well Group",
		"wes",
		"wer",
		"weh",
		"wey",
		"wem",
		"wed",

	"Plan Group",
		"pln",

	"StreamEstimate Group",
		"ses",
		"rib",
		"rim",	// Note: shared with StreamGage
		"rid",	// Note: shared with StreamGage
	
	"River Network Group",
		"rin",
		"net",

	"Operation Group",
		"opr",
		"cal",	// Call time series.

	"San Juan Group",
		"sjr",

	"GeoView Group",	
		"gvp"
};

/**
This array indicates the StateMod response file property name to use with each
component.  The group names are suitable for comments (put a # in front when
writing the response file).  Any value that is a blank string should NOT be
written to the StateMod file.
*/
private static String[] __statemod_file_properties = {
	"Control Data",
		"Response",
		"Control",
		"OutputRequest",

	"StreamGage Data",
		"StreamGage_Station",
		"StreamGage_Historic_Monthly",
		"StreamGage_Historic_Daily",
		"Stream_Base_Monthly",
		"Stream_Base_Daily",
	
	"Delay Tables (Monthly) Data",
		"DelayTable_Monthly",

	"Delay Tables (Daily) Data",
		"DelayTable_Daily",
		
	"Diversion Data",
		"Diversion_Station",
		"Diversion_Right",
		"Diversion_Historic_Monthly",
		"Diversion_Historic_Daily",
		"Diversion_Demand_Monthly",
		"Diversion_DemandOverride_Monthly",
		"Diversion_Demand_AverageMonthly",
		"Divesion_Demand_Daily",
		"IrrigationPractice_Yearly",
		"ConsumptiveWaterRequirement_Monthly",
		"ConsumptiveWaterRequirement_Daily",
		"SoilMoisture",
	
	"Precipitation Data",
		"Precipitation_Monthly",
		
	"Evaporation Data",
		"Evaporation_Monthly",

	"Reservoir Data",
		"Reservoir_Station",
		"Reservoir_Right",
		"Reservoir_Historic_Monthly",
		"Reservoir_Historic_Daily",
		"Reservoir_Target_Monthly",
		"Reservoir_Target_Daily",
	
	"Instream Flow Data",
		"Instreamflow_Station",
		"Instreamflow_Right",
		"Instreamflow_Demand_Monthly",
		"Instreamflow_Demand_AverageMonthly",		
		"Instreamflow_Demand_Daily",
	
	"Well Data",
		"Well_Station",
		"Well_Right",
		"Well_Historic_Monthly",
		"Well_Historic_Daily",
		"Well_Demand_Monthly",
		"Well_Demand_Daily",

	"Plan Data",
		"Plan_Data",

	"Stream (Estimated) Data",
		"StreamEstimate_Station",
		"StreamEstimate_Coefficients",
		"Stream_Base_Monthly",	// Note:  Shared with StreamGage
		"Stream_Base_Daily",	// Note:  Shared with StreamGage
	
	"River Network Data",
		"River_Network",
		"Network",

	"Operational Rights Data",
		"Operational_Right",
		"Downstream_Call",

	"San Juan Recovery Data",
		"SanJuanRecovery",

	"Geographic Data",	
		"GeographicInformation"
};

/**
Array indicating which components are groups.
*/
private static int[] __component_groups = {
	COMP_CONTROL_GROUP,
	COMP_STREAMGAGE_GROUP,
	COMP_DELAY_TABLE_MONTHLY_GROUP,
	COMP_DELAY_TABLE_DAILY_GROUP,
	COMP_DIVERSION_GROUP,
	COMP_PRECIPITATION_GROUP,
	COMP_EVAPORATION_GROUP,
	COMP_RESERVOIR_GROUP,
	COMP_INSTREAM_GROUP,
	COMP_WELL_GROUP,
	COMP_PLAN_GROUP,
	COMP_STREAMESTIMATE_GROUP,
	COMP_RIVER_NETWORK_GROUP,
	COMP_OPERATION_GROUP,
	COMP_SANJUAN_GROUP,
	COMP_GEOVIEW_GROUP,
};
	
/**
Array indicating the primary components within each component group.  The
primary components are used to get the list of identifiers for displays and
processing.  The number of values should agree with the list above.
*/
private static int[] __component_group_primaries = {
		COMP_RESPONSE,			// COMP_CONTROL_GROUP
		COMP_STREAMGAGE_STATIONS,	// COMP_STREAMGAGE_GROUP
		COMP_DELAY_TABLES_MONTHLY,// COMP_DELAY_TABLES_MONTHLY_GROUP
		COMP_DELAY_TABLES_DAILY,	// COMP_DELAY_TABLES_DAILY_GROUP
		COMP_DIVERSION_STATIONS,	// COMP_DIVERSION_GROUP
		COMP_PRECIPITATION_TS_MONTHLY,	// COMP_PRECIPITATION_GROUP
		COMP_EVAPORATION_TS_MONTHLY,	// COMP_EVAPORATION_GROUP
		COMP_RESERVOIR_STATIONS,	// COMP_RESERVOIR_GROUP
		COMP_INSTREAM_STATIONS,		// COMP_INSTREAM_GROUP
		COMP_WELL_STATIONS,		// COMP_WELL_GROUP
		COMP_PLANS,			// COMP_PLAN_GROUP
		COMP_STREAMESTIMATE_STATIONS,	// COMP_STREAMESTIMATE_GROUP
		COMP_RIVER_NETWORK,		// COMP_RIVER_NETWORK_GROUP
		COMP_OPERATION_RIGHTS,		// COMP_OPERATION_GROUP
		COMP_SANJUAN_RIP,		// COMP_SANJUAN_GROUP
		COMP_GEOVIEW			// COMP_GEOVIEW_GROUP
};		

/**
Array indicating the groups for components.
*/
private static int[] __component_group_assignments = {
	COMP_CONTROL_GROUP,
		COMP_CONTROL_GROUP,
		COMP_CONTROL_GROUP,
		COMP_CONTROL_GROUP,

	COMP_STREAMGAGE_GROUP,
		COMP_STREAMGAGE_GROUP,
		COMP_STREAMGAGE_GROUP,
		COMP_STREAMGAGE_GROUP,
		COMP_STREAMGAGE_GROUP,
		COMP_STREAMGAGE_GROUP,		
	
	COMP_DELAY_TABLE_MONTHLY_GROUP,
		COMP_DELAY_TABLE_MONTHLY_GROUP,

	COMP_DELAY_TABLE_DAILY_GROUP,
		COMP_DELAY_TABLE_DAILY_GROUP,
		
	COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,
		COMP_DIVERSION_GROUP,

	COMP_PRECIPITATION_GROUP,
		COMP_PRECIPITATION_GROUP,

	COMP_EVAPORATION_GROUP,
		COMP_EVAPORATION_GROUP,

	COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_GROUP,
		COMP_RESERVOIR_GROUP,

	COMP_INSTREAM_GROUP,
		COMP_INSTREAM_GROUP,
		COMP_INSTREAM_GROUP,
		COMP_INSTREAM_GROUP,
		COMP_INSTREAM_GROUP,
		COMP_INSTREAM_GROUP,

	COMP_WELL_GROUP,
		COMP_WELL_GROUP,
		COMP_WELL_GROUP,
		COMP_WELL_GROUP,
		COMP_WELL_GROUP,
		COMP_WELL_GROUP,
		COMP_WELL_GROUP,

	COMP_PLAN_GROUP,
		COMP_PLAN_GROUP,

	COMP_STREAMESTIMATE_GROUP,
		COMP_STREAMESTIMATE_GROUP,
		COMP_STREAMESTIMATE_GROUP,
		COMP_STREAMESTIMATE_GROUP,
		COMP_STREAMESTIMATE_GROUP,
	
	COMP_RIVER_NETWORK_GROUP,
		COMP_RIVER_NETWORK_GROUP,
		COMP_RIVER_NETWORK_GROUP,

	COMP_OPERATION_GROUP,
		COMP_OPERATION_GROUP,
		COMP_OPERATION_GROUP,

	COMP_SANJUAN_GROUP,
		COMP_SANJUAN_GROUP,

	COMP_GEOVIEW_GROUP,
		COMP_GEOVIEW_GROUP
};

/**
The following array assigns the time series data types for use with time series.
For example, StateMod data sets do not contain a data type and therefore after
reading the file, the time series data type must be assumed.  If the data
component is known (e.g., because reading from a response file), then the
following array can be used to look up the data type for the time series.
Components that are not time series have blank strings for data types.
*/
private static String[] __component_ts_data_types = {
	"",	// "Control Data",
		"",	// "Response",
		"",	// "Control",
		"",	// "Output Request",

	"",	// "Stream Gage Data",
		"",	// "Stream Gage Stations",
		"FlowHist",
		"FlowHist",
		"FlowBase",
		"FlowBase",

	"",	// "Delay Table (Monthly) Data",
		"",	// "Delay Tables (Monthly)",

	"",	// "Delay Table (Daily) Data",
		"",	// "Delay Tables (Daily)",

	"",	// "Diversion Data",
		"",	// "Diversion Stations",
		"TotalWaterRights",	// "Diversion Rights",
		"DiversionHist",	// "Diversion Historical TS (Monthly)",
		"DiversionHist",	// "Diversion Historical TS (Daily)",
		"Demand",		// "Demand TS (Monthly)",
		"DemandOverride",
		"DemandAverage",
		"Demand",
		"",			// "Irrigation Practice TS (Yearly)",
		"CWR",	// "Consumptive Water Requirement (Monthly)",
		"CWR",	// "Consumptive Water Requirement (Daily)",
		"",	// "Soil Moisture",
		
	"",	// "Precipitation Data",
		"Precipitation",	//"Precipitation Time Series (Monthly)",

	"",	// "Evaporation Data",
		"Evaporation",		// "Evaporation Time Series (Monthly)",
	
	"",	// "Reservoir Data",
		"",	// "Reservoir Stations",
		"TotalWaterRights",	// "Reservoir Rights",
		"ContentEOMHist", //"Content, End of Month (Monthly)",
		"ContentEODHist", // "Content, End of Day (Daily)",
		"Target",	// "Reservoir Targets (Monthly)",
		"Target",	// "Reservoir Targets (Daily)",
				// "Min" and "Max" must be appended since the
				// targets always go in pairs.
	
	"",	// "Instream Flow Data",
		"",	// "Instream Flow Stations",
		"TotalWaterRights",	// "Instream Flow Rights",
		"Demand",	// "Demand (Monthly)",
		"DemandAverage",	// "Demand (Average Monthly)",
		"Demand",	// "Demand (Daily)",

	"",	// "Well Data",
		"",	// "Well Stations",
		"TotalWaterRights",	// "Well Rights",
		"PumpingHist",	// "Well Historical Pumping (Monthly)",
		"PumpingHist",	// "Well Historical Pumping (Daily)",
		"Demand",		// "Demand (Monthly)",
		"Demand",		// "Demand (Daily)",

	"",	// "Plan Data",
		"",	// "Plans",
	
	"",	// "Stream Estimate Data",
		"",	// "Stream Estimate Stations",
		"",	// "Stream Estimate Coefficients",
		"FlowBase",	// "Stream Base TS (Monthly)",
		"FlowBase",	// "Stream Base TS (Daily)",

	"",	// "River Network Data",
		"",	// "River Network",
		"",	// "Network (Graphical)",	// RTi version (behind
							// the scenes)
		
	"",	// "Operational Data",
		"",	// "Operational Rights",
		"Call",	// "Call time series",

	"",	// "San Juan Sediment Recovery Plan Data",
		"",	// "San Juan Sediment Recovery Plan",
	
	"",	// "Spatial Data",
		""	//"GeoView Project"
};

/**
The following array assigns the time series data intervals for use with time
series.  This information is important because the data types themselves may
not be unique and the interval must be examined.
*/
private static int[] __component_ts_data_intervals = {
	TimeInterval.UNKNOWN,	// "Control Data",
		TimeInterval.UNKNOWN,	// "Response",
		TimeInterval.UNKNOWN,	// "Control",
		TimeInterval.UNKNOWN,	// "Output Request",

	TimeInterval.UNKNOWN,	// "Stream Gage Data",
		TimeInterval.UNKNOWN,	// "Stream Gage Stations",
		TimeInterval.MONTH,
		TimeInterval.DAY,
		TimeInterval.MONTH,
		TimeInterval.DAY,

	TimeInterval.UNKNOWN,	// "Delay Table (Monthly) Data",
		TimeInterval.UNKNOWN,	// "Delay Tables (Monthly)",

	TimeInterval.UNKNOWN,	// "Delay Table (Daily) Data",
		TimeInterval.UNKNOWN,	// "Delay Tables (Daily)",

	TimeInterval.UNKNOWN,	// "Diversion Data",
		TimeInterval.UNKNOWN,	// "Diversion Stations",
		TimeInterval.UNKNOWN,	// "Diversion Rights",
		TimeInterval.MONTH,	// "Diversion Historical TS (Monthly)",
		TimeInterval.DAY,	// "Diversion Historical TS (Daily)",
		TimeInterval.MONTH,	// "Demand TS (Monthly)",
		TimeInterval.MONTH,
		TimeInterval.MONTH,
		TimeInterval.DAY,
		TimeInterval.YEAR,	// "Irrigation Practice TS (Yearly)",
		TimeInterval.MONTH,	// "Consumptive Water Req. (Monthly)",
		TimeInterval.DAY,	// "Consumptive Water Req. (Daily)",
		TimeInterval.UNKNOWN,	// "Soil Moisture",
		
	TimeInterval.UNKNOWN,	// "Precipitation Data",
		TimeInterval.MONTH,	//"Precipitation Time Series (Monthly)",

	TimeInterval.UNKNOWN,	// "Evaporation Data",
		TimeInterval.MONTH,	// "Evaporation Time Series (Monthly)",
	
	TimeInterval.UNKNOWN,	// "Reservoir Data",
		TimeInterval.UNKNOWN,	// "Reservoir Stations",
		TimeInterval.UNKNOWN,	// "Reservoir Rights",
		TimeInterval.MONTH,	// "Content, End of Month (Monthly)",
		TimeInterval.DAY,	// "Content, End of Day (Daily)",
		TimeInterval.MONTH,	// "Reservoir Targets (Monthly)",
		TimeInterval.DAY,	// "Reservoir Targets (Daily)"
	
	TimeInterval.UNKNOWN,	// "Instream Flow Data",
		TimeInterval.UNKNOWN,	// "Instream Flow Stations",
		TimeInterval.UNKNOWN,	// "Instream Flow Rights",
		TimeInterval.MONTH,	// "Demand (Monthly)",
		TimeInterval.MONTH,	// "Demand (Average Monthly)",
		TimeInterval.DAY,	// "Demand (Daily)",

	TimeInterval.UNKNOWN,	// "Well Data",
		TimeInterval.UNKNOWN,	// "Well Stations",
		TimeInterval.UNKNOWN,	// "Well Rights",
		TimeInterval.MONTH,	// "Well Historical Pumping (Monthly)",
		TimeInterval.DAY,	// "Well Historical Pumping (Daily)",
		TimeInterval.MONTH,	// "Demand (Monthly)",
		TimeInterval.DAY,	// "Demand (Daily)",

	TimeInterval.UNKNOWN,	// "Plan Data",
		TimeInterval.UNKNOWN,	// "Plans",
	
	TimeInterval.UNKNOWN,	// "Stream Estimate Data",
		TimeInterval.UNKNOWN,	// "Stream Estimate Stations",
		TimeInterval.UNKNOWN,	// "Stream Estimate Coefficients",
		TimeInterval.MONTH,	// "Stream Base TS (Monthly)",
		TimeInterval.DAY,	// "Stream Base TS (Daily)",

	TimeInterval.UNKNOWN,	// "River Network Data",
		TimeInterval.UNKNOWN,	// "River Network",
		TimeInterval.UNKNOWN,	// "Network (Graphical)"
		
	TimeInterval.UNKNOWN,	// "Operational Data",
		TimeInterval.UNKNOWN,	// "Operational Rights",
		TimeInterval.DAY,	// "Call time series",

	TimeInterval.UNKNOWN,	// "San Juan Sediment Recovery Plan Data",
		TimeInterval.UNKNOWN,	// "San Juan Sediment Recovery Plan",
	
	TimeInterval.UNKNOWN,	// "Spatial Data",
		TimeInterval.UNKNOWN	//"GeoView Project"
};

/**
The following array assigns the time series data units for use with time series.
These can be used when creating new time series.  If the data
component is known (e.g., because reading from a response file), then the
following array can be used to look up the data units for the time series.
Components that are not time series have blank strings for data units.
*/
private static String[] __component_ts_data_units = {
	"",	// "Control Data",
		"",	// "Response",
		"",	// "Control",
		"",	// "Output Request",

	"",	// "Stream Gage Data",
		"",	// "Stream Gage Stations",
		"ACFT",
		"CFS",
		"ACFT",
		"CFS",

	"",	// "Delay Table (Monthly) Data",
		"",	// "Delay Tables (Monthly)",

	"",	// "Delay Table (Daily) Data",
		"",	// "Delay Tables (Daily)",

	"",	// "Diversion Data",
		"",	// "Diversion Stations",
		"CFS",	// "Diversion Rights",
		"ACFT",	// "Diversion Historical TS (Monthly)",
		"CFS",	// "Diversion Historical TS (Daily)",
		"ACFT",	// "Demand TS (Monthly)",
		"ACFT",
		"ACFT",
		"CFS",
		"",	// "Irrigation Practice TS (Yearly)" - units vary
		"ACFT",	// "Consumptive Water Requirement (Monthly)",
		"CFS",	// "Consumptive Water Requirement (Daily)",
		"",	// "Soil Moisture",
		
	"",	// "Precipitation Data",
		"Precipitation",	//"Precipitation Time Series (Monthly)",

	"",	// "Evaporation Data",
		"Evaporation",		// "Evaporation Time Series (Monthly)",
	
	"",	// "Reservoir Data",
		"",	// "Reservoir Stations",
		"ACFT",	// "Reservoir Rights",
		"ACFT", //"Content, End of Month (Monthly)",
		"ACFT", // "Content, End of Day (Daily)",
		"ACFT",	// "Reservoir Targets (Monthly)",
		"ACFT",	// "Reservoir Targets (Daily)",
	
	"",	// "Instream Flow Data",
		"",	// "Instream Flow Stations",
		"CFS",	// "Instream Flow Rights",
		"CFS",	// "Demand (Monthly)",
		"CFS",	// "Demand (Average Monthly)",
		"CFS",	// "Demand (Daily)",

	"",	// "Well Data",
		"",	// "Well Stations",
		"CFS",	// "Well Rights",
		"ACFT",	// "Well Historical Pumping (Monthly)",
		"CFS",	// "Well Historical Pumping (Daily)",
		"ACFT",	// "Demand (Monthly)",
		"CFS",	// "Demand (Daily)",

	"",	// "Plan Data",
		"",	// "Plans",
	
	"",	// "Stream Estimate Data",
		"",	// "Stream Estimate Stations",
		"",	// "Stream Estimate Coefficients",
		"ACFT",	// "Stream Base TS (Monthly)",
		"CFS",	// "Stream Base TS (Daily)",

	"",	// "River Network Data",
		"",	// "River Network",
		"",	// "Network (Graphical)",	// RTi version (behind
							// the scenes)
		
	"",	// "Operational Data",
		"",	// "Operational Rights",
		"DAY",	// "Call time series",

	"",	// "San Juan Sediment Recovery Plan Data",
		"",	// "San Juan Sediment Recovery Plan",
	
	"",	// "Spatial Data",
		""	//"GeoView Project"
};

// Control file data specific to StateMod.  Defaults are assigned to allow
// backward compatibility with old data sets - newer settings are set to
// "no data" values.  Note that after reading the control file, setDirty(false)
// is called and the new defaults may be ignored if the file is not written.

private String __heading1 = "";			// Heading for output.
private String __heading2 = "";
						// Heading for output.
private int __iystr = StateMod_Util.MISSING_INT;// Starting year of the
						// simulation.  Must be defined.
private int __iyend = StateMod_Util.MISSING_INT;// Ending year of the
						// simulation.  Must be defined.
private int __iresop = 2;			// Switch for output units
						// Default is ACFT.
private int __moneva = 0;			// Monthly or avg monthly evap
						// Default to monthly.
private int __iopflo = 1;			// Total or gains streamflow
						// Default to total.
private int __numpre = 0;			// Number of precipitation
						// stations - should be set when
						// the time series are read -
						// this will be phased out in
						// the future.
private int __numeva = 0;			// Number of evaporation
						// stations - should be set when
						// the time series are read -
						// this will be phased out in
						// the future.
private int __interv = -1;			// Max number entries in delay
						// pattern.
						// Default is variable number as
						// percents.
						// The following defaults assume
						// normal operation...
private double __factor = 1.9835;		// factor, CFS to AF/D
private double __rfacto = 1.9835;		// Divisor for streamflow data
						// units
private double __dfacto = 1.9835;		// Divisor for diversion data
						// units
private double __ffacto = 1.9835;		// Divisor for instreamflow data
						// units
private double __cfacto = 1.0;			// Factor, reservoir content
						// data to AF
private double __efacto = 1.0;			// Factor, evaporation data to
						// FT
private double __pfacto = 1.0;			// Factor, precipitation data
						// to FT
private int __cyrl = SM_CYR;			// Calendar/water/irrigation
						// year - default to calendar.
private int __icondem = 1;			// Switch for demand type.
						// Default to historic approach
private int __ichk = 0;				// Switch for detailed output.
						// Default is no detailed output
private int __ireopx = 0;			// Swith for reoperation control
						// Default is yes reoperate
						// Unlike most StateMod options
						// this uses 0 for do it.
private int __ireach = 1;			// Switch for instream flow
						// approach
						// Default to use reaches and
						// average monthly demands.
private int __icall = 0;			// Switch for detailed call data
						// Default to no data.
private String __ccall = "";			// Default to not used.
						// Detailed call water right ID
private int __iday = 0;				// Switch for daily analysis.
						// Default to no daily analysis.
private int __iwell = 0;			// Switch for well analysis.
						// Default to no well analysis.
private double __gwmaxrc = 0.0;			// Maximum recharge limit
						// Default to not used.
private int __isjrip = 0;			// San Juan recovery program.
						// Default to no SJRIP.
private int __itsfile = 0;			// Is IPY data used?
						// Default to no data.
private int __ieffmax = 0;			// IWR switch - default to no
						// data.
private int __isprink = 0;			// Sprinkler switch
						// Default to no sprinkler data.
private double __soild = 0.0;			// Soil moisture accounting.
						// Default to not used.
private int __isig = 0;				// Significant figures for
						// output.

/**
Constructor.  Makes a blank data set.  It is expected that other information 
will be set during further processing.
*/
public StateMod_DataSet() {
	super(__component_types, __component_names, __component_groups,
		__component_group_assignments, __component_group_primaries);

	initialize();
}
/**
Constructor.  Makes a blank data set.  Specific output files, by default, will 
use the output directory and base file name in output file names.
@param type Data set type (currently ignored).
*/
public StateMod_DataSet(int type)
{	super(__component_types, __component_names, __component_groups,
		__component_group_assignments, __component_group_primaries);
	try {	setDataSetType(type, true);
	}
	catch (Exception e) {
		// Type not important
	}

	initialize();
}

/**
Copy constructor.
@param dataset Original data set to copy.
@param deep_copy If true, all data are copied (currently not recognized).
If false, the data set components are copied but not the data itself.  This is
typically used to save the names of components before editing in the response
file editor.
*/
public StateMod_DataSet ( StateMod_DataSet dataset, boolean deep_copy )
{	this ();	// To initialize a new instance.  Next - clear out all
			// the components.  Otherwise they are added again!
	String routine = "StateMod_DataSet";
	getComponents().removeAllElements();
	setDataSetDirectory(dataset.getDataSetDirectory());
	// Internal data...
	__tsAreRead = dataset.__tsAreRead;
	__is_free_format = dataset.__is_free_format;
	// Control settings...
	__heading1 = dataset.__heading1;
	__heading2 = dataset.__heading2;
	__iystr = dataset.__iystr;
	__iyend = dataset.__iyend;
	__iresop = dataset.__iresop;
	__moneva = dataset.__moneva;
	__iopflo = dataset.__iopflo;
	__numpre = dataset.__numpre;
	__numeva = dataset.__numeva;
	__interv = dataset.__interv;
	__factor = dataset.__factor;
	__rfacto = dataset.__rfacto;
	__dfacto = dataset.__dfacto;
	__ffacto = dataset.__ffacto;
	__cfacto = dataset.__cfacto;
	__efacto = dataset.__efacto;
	__pfacto = dataset.__pfacto;
	__cyrl = dataset.__cyrl;
	__icondem = dataset.__icondem;
	__ichk = dataset.__ichk;
	__ireopx = dataset.__ireopx;
	__ireach = dataset.__ireach;
	__icall = dataset.__icall;
	__ccall = dataset.__ccall;
	__iday = dataset.__iday;
	__iwell = dataset.__iwell;
	__gwmaxrc = dataset.__gwmaxrc;
	__isjrip = dataset.__isjrip;
	__itsfile = dataset.__itsfile;
	__ieffmax = dataset.__ieffmax;
	__isprink = dataset.__isprink;
	__soild = dataset.__soild;
	__isig = dataset.__isig;

	// Add each component, doing a shallow copy...

	Vector data_Vector = dataset.getComponents();
	int size = 0;
	if (data_Vector != null) {
		size = data_Vector.size();
	}
	DataSetComponent comp, comp2, newcomp = null, newcomp2;
	for ( int i = 0; i < size; i++ ) {
		comp = (DataSetComponent)data_Vector.elementAt(i);
		if ( comp == null ) {
			addComponent ( null );
		}
		else {	try {	newcomp = 
				new DataSetComponent (comp, this, false);
				addComponent ( newcomp );
			}
			catch ( Exception e ) {
				addComponent ( null );
				Message.printWarning ( 2, routine,
				"Error copying component." );
				Message.printWarning ( 2, routine, e );
				continue;
			}
			if ( comp.isGroup() ) {
				// Need to add components to the group...
				Vector data2 = (Vector)comp.getData();
				int size2 = 0;
				if ( data2 != null ) {
					size2 = data2.size();
				}
				for ( int j = 0; j < size2; j++ ) {
					comp2 = (DataSetComponent)
					data2.elementAt(j);
					try {	newcomp2 = new DataSetComponent(
							comp2, this, false );
						// Need to manually set...
						newcomp2.setParent ( newcomp );
						newcomp.addComponent (newcomp2);
					}
					catch ( Exception e ) {
						Message.printWarning ( 2,
						routine,
						"Error copying component." );
						Message.printWarning ( 2,
						routine, e );
					}
				}
			}
		}
	}
	setDataSetDirectory(dataset.getDataSetDirectory());
}

/**
Indicate whether time series where read with the data set.
@return true if all time series were read.
*/
public boolean areTSRead()
{	return __tsAreRead;
}

/**
Check a component's data, using other available components as appropriate.
@param comp_type the component type.
@param props Properties to control the check (currently unused).  This may
be used to control whether results are returned, and in what format.
@return the Vector of check results messages.
*/
public Vector checkComponentData ( int comp_type, PropList props )
{	if ( comp_type == COMP_WELL_STATIONS ) {
		return checkComponentData_WellStations ( props );
	}
	else if ( comp_type == COMP_WELL_RIGHTS ) {
		return checkComponentData_WellRights ( props );
	}
	return new Vector();
}

/**
Helper method to check well rights component data.  The following are checked:
<ol>
<li>	Well stations without at least one right are listed.  This requires that
	the dataset include well stations.</li>
<li>	Well rights with yield <= 0.0</li>
<li>	Well rights summary for a station is not equal to the well capacity.
	This requires that the dataset include well stations.<li>
</ol>
*/
private Vector checkComponentData_WellRights ( PropList props )
{	Vector message_list = new Vector();

	// Make sure that there is at least one well right for each well
	// station...

	DataSetComponent wes_comp = getComponentForComponentType (
		COMP_WELL_STATIONS );
	DataSetComponent wer_comp = getComponentForComponentType (
		COMP_WELL_RIGHTS );
	Vector wes_Vector = (Vector)wes_comp.getData();
	Vector wer_Vector = (Vector)wer_comp.getData();
	int size = 0;
	if ( wes_Vector != null ) {
		size = wes_Vector.size();
	}
	StateMod_Well wes_i = null;
	StateMod_Parcel parcel = null;	// Parcel associated with a well station
	int wes_parcel_count = 0;	// Parcel count for well station
	double wes_parcel_area = 0.0;	// Area of parcels for well station
	int wes_well_parcel_count = 0;	// Parcel (with wells) count for well
					// station.
	double wes_well_parcel_area = 0.0;
					// Area of parcels with wells for well
					// station.
	Vector parcel_Vector;		// List of parcels for well station.
	int count = 0;			// Count of well stations with
					// potential problems.
	String id_i = null;
	Vector rights = null;
	for ( int i = 0; i < size; i++ ) {
		wes_i = (StateMod_Well)wes_Vector.elementAt(i);
		if ( wes_i == null ) {
			continue;
		}
		id_i = wes_i.getID();
		rights = StateMod_Util.getRightsForStation ( id_i, wer_Vector );
		// REVISIT SAM 2007-01-02
		// Evaluate how to put this code in a separate method and share
		// between rights and stations.
		if ( (rights == null) || (rights.size() == 0) ) {
			// The following is essentially a copy of code for well
			// stations. Keep the code consistent.  Note that the
			// following assumes that when reading well rights from
			// HydroBase that lists of parcels are saved with well
			// stations.  This will clobber any parcel data that
			// may have been saved at the time that well stations
			// were processed (if processed in the same commands
			// file).
			++count;
			// Check for parcels...
			wes_parcel_count = 0;
			wes_parcel_area = 0.0;
			wes_well_parcel_count = 0;
			wes_well_parcel_area = 0.0;
			parcel_Vector = wes_i.getParcels();
			if ( parcel_Vector != null ) {
				wes_parcel_count = parcel_Vector.size();
				for ( int j = 0; j < wes_parcel_count; j++ ) {
					parcel = (StateMod_Parcel)
						parcel_Vector.elementAt(j);
					if ( parcel.getArea() > 0.0 ) {
						wes_parcel_area +=
							parcel.getArea();
					}
					if ( parcel.getWellCount() > 0 ) {
						wes_well_parcel_count +=
							parcel.getWellCount();
						wes_well_parcel_area +=
							parcel.getArea();
					}
				}
			}
			// Format suitable for output in a list that
			// can be copied to a spreadsheet or table.
			message_list.addElement (
				StringUtil.formatString(count,"%4d") +
				", " +
				StringUtil.formatString(id_i,"%-12.12s") +
				", " +
				StringUtil.formatString(
				wes_i.getCollectionType(),"%-10.10s") +
				", " +
				StringUtil.formatString(wes_parcel_count,"%9d")+
				", " +
				StringUtil.formatString(
				wes_parcel_area,"%11.0f")+
				", " +
				StringUtil.formatString(
				wes_well_parcel_count,"%9d")+
				", " +
				StringUtil.formatString(
				wes_well_parcel_area,"%11.0f")
				+ ", \"" + wes_i.getName() + "\"" );
		}
	}
	if ( message_list.size() > 0 ) {
		int line = 0;		// Line number for output (zero index).
		// Prepend introduction to the specific warnings...
		message_list.insertElementAt ( "", line++ );
		message_list.insertElementAt (
		"The following well stations (" + count + " out of " + size +
		") have no water rights (no irrigated parcels served by " +
		"wells).", line++ );
		message_list.insertElementAt (
		"Data may be OK if the station has no wells.", line++ );
		message_list.insertElementAt ( "", line++ );
		message_list.insertElementAt (
		"Parcel count and area in the following table are available " +
		"only if well rights are read from HydroBase.", line++ );
		message_list.insertElementAt ( "", line++ );
		message_list.insertElementAt (
		"    ,             ,           , # PARCELS, TOTAL      , # PARCELS, PARCELS    , WELL",
		line++ );
		message_list.insertElementAt (
		"    , WELL        , COLLECTION, FOR WELL , PARCEL     , WITH     , WITH WELLS , STATION",
		line++ );
		message_list.insertElementAt (
		"NUM., STATION ID  , TYPE      , STATION  , AREA (ACRE), WELLS    , AREA (ACRE), NAME",
		line++ );
		message_list.insertElementAt (
		"----,-------------,-----------,----------,------------,----------,------------,-------------------------",
		line++ );
	}

	// Check to make sure the sum of well rights equals the well station
	// capacity...

	checkComponentData_WellRights_Capacity ( message_list );

	// Since well rights are determined from parcel data, print a list of
	// well rights that do not have associated yield (decree)...

	size = 0;
	if ( wer_Vector != null ) {
		size = wer_Vector.size();
	}
	int pos = 0;			// Position in well station vector
	String wes_name = null;		// Well station name
	String wes_id = null;		// Well station ID
	Vector message_list2 = new Vector();
	String wer_id = null;		// Well right identifier
	count = 0;
	double decree = 0.0;
	StateMod_WellRight wer_i = null;
	for ( int i = 0; i < size; i++ ) {
		wer_i = (StateMod_WellRight)wer_Vector.elementAt(i);
		wer_id = wer_i.getID();
		// Format to two digits to match StateMod output...
		decree =StringUtil.atod(
			StringUtil.formatString(wer_i.getDcrdivw(),"%.2f") );
		if ( decree <= 0.0 ) {
			// Find associated well station for output to print ID
			// and name...
			++count;
			pos = StateMod_Util.indexOf(wes_Vector,
				wer_i.getCgoto() );
			wes_i = null;
			if ( pos >= 0 ) {
				wes_i = (StateMod_Well)wes_Vector.elementAt(pos);
			}
			wes_name = "";
			if ( wes_i != null ) {
				wes_id = wes_i.getID();
				wes_name = wes_i.getName();
			}
			// Format suitable for output in a list that
			// can be copied to a spreadsheet or table.
			message_list2.addElement (
				StringUtil.formatString(count,"%4d") +
				", " +
				StringUtil.formatString(wer_id,"%-12.12s") +
				", " +
				StringUtil.formatString(wes_id,"%-12.12s") +
				", \"" + wes_name + "\"" );
		}
	}
	if ( message_list2.size() > 0 ) {
		// Prepend introduction to the specific warnings...
		message_list2.insertElementAt ( "", 0 );
		message_list2.insertElementAt (
		"The following well rights (" + count + " out of " + size +
		") have no decree (checked to StateMod file .XX precision).",1);
		message_list2.insertElementAt (
		"Well yield data may not be available.", 2 );
		message_list2.insertElementAt ( "", 3 );
		message_list2.insertElementAt (
		"    , WELL        , WELL        ,", 4);
		message_list2.insertElementAt (
		"NUM., RIGHT ID    , STATION ID  , WELL NAME", 5);
		message_list2.insertElementAt (
		"----,-------------,-------------,------------------------", 5);

		StringUtil.addListToStringList ( message_list, message_list2 );
	}

	// Save the messages in case something needs to extract later...
	wer_comp.setDataCheckResults ( message_list );
	return message_list;
}

/**
Helper method to check that well rights sum to the well station capacity.  This
is called by the well right and well station checks.  The check is performed
by formatting the capacity and decree sum to .NN precision.
@param message_list Vector of string to be printed to the check file, which will
be added to in this method.
*/
void checkComponentData_WellRights_Capacity ( Vector message_list )
{	Vector message_list2 = new Vector();	// New messages generated by
						// this method.
	DataSetComponent wes_comp = getComponentForComponentType (
		COMP_WELL_STATIONS );
	DataSetComponent wer_comp = getComponentForComponentType (
		COMP_WELL_RIGHTS );
	Vector wes_Vector = (Vector)wes_comp.getData();
	Vector wer_Vector = (Vector)wer_comp.getData();
	StateMod_WellRight wer_i = null;
	StateMod_Well wes_i = null;
	int size = 0;
	if ( wes_Vector != null ) {
		size = wes_Vector.size();
	}
	double decree;
	double decree_sum;
	int count = 0;
	int onoff = 0;		// On/off switch for right
	int size_rights = 0;
	String id_i = null;
	Vector rights = null;
	for ( int i = 0; i < size; i++ ) {
		wes_i = (StateMod_Well)wes_Vector.elementAt(i);
		if ( wes_i == null ) {
			continue;
		}
		id_i = wes_i.getID();
		rights = StateMod_Util.getRightsForStation ( id_i, wer_Vector );
		size_rights = 0;
		if ( rights != null ) {
			size_rights = rights.size();
		}
		if ( size_rights == 0 ) {
			continue;
		}
		// Get the sum of the rights, assuming that all should be
		// compared against the capacity (i.e., sum of rights at the
		// end of the period will be compared with the current well
		// capacity)...
		decree_sum = 0.0;
		for ( int iright = 0; iright < size_rights; iright++ ) {
			wer_i = (StateMod_WellRight)rights.elementAt(iright);
			decree = wer_i.getDcrdivw();
			onoff = wer_i.getSwitch();
			if ( decree < 0.0 ) {
				// Ignore - missing values will cause a bad
				// sum.
				continue;
			}
			if ( onoff <= 0 ) {
				// Subtract the decree...
				decree_sum -= decree;
			}
			else {	// Add the decree...
				decree_sum += decree;
			}
		}
		// Compare to a whole number, which is the greatest precision
		// for documented files.
		if (	!StringUtil.formatString(decree_sum,"%.2f").equals(
			StringUtil.formatString(wes_i.getDivcapw(),"%.2f")) ) {
			// Format suitable for output in a list that
			// can be copied to a spreadsheet or table.
			message_list2.addElement (
				StringUtil.formatString(++count,"%4d") +
				", " +
				StringUtil.formatString(id_i,"%-12.12s") +
				", " +
				StringUtil.formatString(
				wes_i.getCollectionType(),"%-10.10s") +
				"," +
				StringUtil.formatString(
				wes_i.getDivcapw(),"%9.2f")+
				"," +
				StringUtil.formatString(decree_sum,"%9.2f") +
				"," +
				StringUtil.formatString( size_rights,"%8d") +
				", \"" + wes_i.getName() + "\"" );
		}
	}
	if ( message_list2.size() > 0 ) {
		// Prepend introduction to the specific warnings...
		int line = 0;
		message_list2.insertElementAt ( "", line++ );
		message_list2.insertElementAt (
		"The following well stations (" + count + " out of " + size +
		") have capacity different from the sum of well rights for " +
		"the station.", line++ );
		message_list2.insertElementAt (
		"Check that the StateDMI command parameters used to process " +
		"well stations and rights are consistent.", line++ );
		message_list2.insertElementAt ( "", line++ );
		message_list2.insertElementAt (
		"Parcel count and area in the following table are available " +
		"only if well rights are read from HydroBase.", line++ );
		message_list2.insertElementAt ( "", line++ );
		message_list2.insertElementAt (
		"    ,             ,           , WELL    , SUM OF  , NUMBER , WELL",
		line++ );
		message_list2.insertElementAt (
		"    , WELL        , COLLECTION, CAPACITY, RIGHTS  , OF     , STATION",
		line++ );
		message_list2.insertElementAt (
		"NUM., STATION ID  , TYPE      , (CFS)   , (CFS)   , RIGHTS , NAME",
		line++ );
		message_list2.insertElementAt (
		"----,-------------,-----------,---------,---------,--------,-------------------------",
		line++ );
		// Add to the main message list...
		StringUtil.addListToStringList ( message_list, message_list2 );
	}
}

/**
Helper method to check well stations component data.  The following are checked:
<ol>
<li>	Well stations with acreage <= 0.0.</li>
<li>	The well capacity is equal to the sum of the well rights (this requires
	that well rights are also available in memory).
</ol>
*/
private Vector checkComponentData_WellStations ( PropList props )
{	Vector message_list = new Vector();

	DataSetComponent wes_comp = getComponentForComponentType (
		COMP_WELL_STATIONS );
	Vector wes_Vector = (Vector)wes_comp.getData();
	int size = 0;
	if ( wes_Vector != null ) {
		size = wes_Vector.size();
	}
	StateMod_Well wes_i = null;
	StateMod_Parcel parcel = null;	// Parcel associated with a well station
	String id_i = null;

	// Generate a list of well stations that do not have associated acreage
	// or parcels...

	int wes_parcel_count = 0;	// Parcel count for well station
	double wes_parcel_area = 0.0;	// Area of parcels for well station
	int wes_well_parcel_count = 0;	// Parcel (with wells) count for well
					// station.
	double wes_well_parcel_area = 0.0;
					// Area of parcels with wells for well
					// station.
	Vector parcel_Vector;		// List of parcels for well station.
	int count = 0;			// Count of well stations with
					// potential problems.
	for ( int i = 0; i < size; i++ ) {
		wes_i = (StateMod_Well)wes_Vector.elementAt(i);
		id_i = wes_i.getID();
		if ( wes_i.getAreaw() <= 0.0 ) {
			++count;
			// Check for parcels...
			wes_parcel_count = 0;
			wes_parcel_area = 0.0;
			wes_well_parcel_count = 0;
			wes_well_parcel_area = 0.0;
			parcel_Vector = wes_i.getParcels();
			if ( parcel_Vector != null ) {
				wes_parcel_count = parcel_Vector.size();
				for ( int j = 0; j < wes_parcel_count; j++ ) {
					parcel = (StateMod_Parcel)
						parcel_Vector.elementAt(j);
					if ( parcel.getArea() > 0.0 ) {
						wes_parcel_area +=
							parcel.getArea();
					}
					if ( parcel.getWellCount() > 0 ) {
						wes_well_parcel_count +=
							parcel.getWellCount();
						wes_well_parcel_area +=
							parcel.getArea();
					}
				}
			}
			// Format suitable for output in a list that
			// can be copied to a spreadsheet or table.
			message_list.addElement (
				StringUtil.formatString(count,"%4d") +
				", " +
				StringUtil.formatString(id_i,"%-12.12s") +
				", " +
				StringUtil.formatString(
				wes_i.getCollectionType(),"%-10.10s") +
				", " +
				StringUtil.formatString(wes_parcel_count,"%9d")+
				", " +
				StringUtil.formatString(
				wes_parcel_area,"%11.0f")+
				", " +
				StringUtil.formatString(
				wes_well_parcel_count,"%9d")+
				", " +
				StringUtil.formatString(
				wes_well_parcel_area,"%11.0f")
				+ ", \"" + wes_i.getName() + "\"" );
		}
	}
	if ( message_list.size() > 0 ) {
		// Prepend introduction to the specific warnings...
		message_list.insertElementAt ( "", 0 );
		message_list.insertElementAt (
		"The following well stations (" + count + " out of " + size +
		") have no irrigated parcels served by wells.", 1 );
		message_list.insertElementAt (
		"Data may be OK if the station is an M&I or has no wells.", 2 );
		message_list.insertElementAt ( "", 3 );
		message_list.insertElementAt (
		"Parcel count and area in the following table are available " +
		"only if well stations are read from HydroBase.", 4 );
		message_list.insertElementAt ( "", 5 );
		message_list.insertElementAt (
		"    ,             ,           , # PARCELS, TOTAL      , # PARCELS, PARCELS    , WELL",
		6 );
		message_list.insertElementAt (
		"    , WELL        , COLLECTION, FOR WELL , PARCEL     , WITH     , WITH WELLS , STATION",
		7 );
		message_list.insertElementAt (
		"NUM., STATION ID  , TYPE      , STATION  , AREA (ACRE), WELLS    , AREA (ACRE), NAME",
		8 );
		message_list.insertElementAt (
		"----,-------------,-----------,----------,------------,----------,------------,-------------------------",
		9 );
	}

	// Check to make sure the sum of well rights equals the well station
	// capacity...

	checkComponentData_WellRights_Capacity ( message_list );

	// Save the messages in case something needs to extract later...
	wes_comp.setDataCheckResults ( message_list );
	return message_list;
}

/**
Check all file names for compliance with the 8.3 naming convention.  This is
currently a limitation in StateMod.  It appears that StateMod will run even if
a directory name has a full 8.3 part.  So, break all the response file path
parts for every file into tokens based on the path separator and check for:
<ol>
<li>	Overall length of part must be less than 13.</li>
<li>	Length without . is <= 8.</li>
<li>	Only one period can exist and it must be within the last 4
	characters.</li>
</ol>
@return true if the filenames pass inspection, false if any do not.
@param warning_level If 1, then a warning dialog will be shown. Otherwise,
messages will be printed to the log file only, depending on the global warning
level.
*/
public boolean checkComponentFilenames ( Vector file_names, int warning_level )
{	// For now check each part of all, even though the path is reused...
	Vector parts = null;
	String fullname = null;
	StringBuffer warnings = new StringBuffer();
	String part = null;
	int j = 0;
	int size = 0;
	int count = 0;
	int nfiles = 0;
	if ( file_names != null ) {
		nfiles = file_names.size();
	}
	String file_name;
	for (int i = 0; i < nfiles; i++) {
		// Some ugly checks because at this point we are not being
		// really careful about knowing the positions of specific files
		// in the list.  When Ray Bennett starts using a PropList it
		// should be a lot easier...
		file_name = (String)file_names.elementAt(i);
		//if ( == StateMod_DataSet.COMP_GEOVIEW) {}
		if (	StringUtil.endsWithIgnoreCase(file_name,".gvp") ||
			StringUtil.endsWithIgnoreCase(file_name,".net") ) {
			// No need to check the GIS project file or network
			// file because the model does not even use these
			// files...
			continue;
		}
		
		// Get the full file name, relative to the data set directory...
		fullname = getDataFilePathAbsolute(file_name);
		// Break into parts...
		parts = StringUtil.breakStringList(fullname, File.separator, 0);
		size = 0;
		
		if (parts != null) {
			size = parts.size();
		}
		
		for (j = 0; j < size; j++) {
			part = (String)parts.elementAt(j);
			if (part == null) {
				continue;
			}
			if (	(part.length() > 12) ||
				((part.indexOf(".") < 0) &&
				(part.length()> 8)) ||
				((part.length() <= 12) &&
				(part.indexOf(".") >= 0) &&
				(part.indexOf(".") < (part.length()- 4))) ||
				(StringUtil.patternCount(part,".") > 1)) {
				if (count == 0) {
					warnings.append(
					"\nThe following files do not adhere" +
					" to the 8.3 file standard " +
					"required by StateMod:\n");
				}
				warnings.append("File \"" + fullname 
					+ "\" part (" + part + ")\n");
				// No need to process the remaining parts...
				++count;
				break;
			}
		}
		if (count == 10) {
			// Enough...
			break;
		}
	}
	
	boolean status = true;
	
	if (warnings.length() > 0) {
		if (count == 10) {
			warnings.append("Stopped at 10 warnings\n");
		}
		Message.printWarning(warning_level,
		"StateMod_DataSet.checkComponentFilenames",warnings.toString());
		status = false;
	}
	warnings = null;
	part = null;
	parts = null;
	fullname = null;
	return status;
}

/**
Check the data set components for visibility based on the control file settings.
If the control settings indicate
that a file is not needed in a data set, it is marked as not visible and will
not be shown in display components.  Invisible componets are retained in the
data set because sometimes they are included in the response file but we don't
want to throw away the data as control settings change.  Only files that are
impacted by control file settings are checked.
*/
public void checkComponentVisibility ()
{	DataSetComponent comp;
	boolean visibility = true;

	// Check for daily data set (some may be reset in other checks below)...

	if ( __iday != 0 ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType (
		COMP_STREAMGAGE_BASEFLOW_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_DEMAND_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_INSTREAM_DEMAND_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_WELL_DEMAND_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_RESERVOIR_TARGET_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_DELAY_TABLE_DAILY_GROUP );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_DELAY_TABLES_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType (
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType (
		COMP_STREAMGAGE_HISTORICAL_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_DIVERSION_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_WELL_PUMPING_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_RESERVOIR_CONTENT_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	
	// The stream estimate baseflow time series are always invisible because
	// they are shared with the stream gage baseflow time series files...

	comp = getComponentForComponentType (
		COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY );
	if ( comp != null ) {
		comp.setVisible ( false );
	}
	comp = getComponentForComponentType (
		COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY );
	if ( comp != null ) {
		comp.setVisible ( false );
	}

	// Check well data set...

	if ( hasWellData(false) ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType ( COMP_WELL_GROUP );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_WELL_STATIONS );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_WELL_RIGHTS );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_WELL_DEMAND_TS_MONTHLY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_WELL_PUMPING_TS_MONTHLY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	if ( __iday != 0 ) {	// Else checked above
		comp = getComponentForComponentType (
			COMP_WELL_DEMAND_TS_DAILY );
		if ( comp != null ) {
			comp.setVisible ( visibility );
		}
		comp = getComponentForComponentType (
			COMP_WELL_PUMPING_TS_DAILY );
		if ( comp != null ) {
			comp.setVisible ( visibility );
		}
	}

	// Check instream demand flag (component is in the instream flow
	// group)...

	if ( (__ireach == 2) || (__ireach == 3) ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType ( COMP_INSTREAM_DEMAND_TS_MONTHLY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}

	// Check SJRIP flag...

	if ( __isjrip != 0 ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType ( COMP_SANJUAN_GROUP );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	comp = getComponentForComponentType ( COMP_SANJUAN_RIP );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}

	// Check irrigation practice flag (component is in the diversion
	// group)...

	if ( __itsfile != 0 ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType(COMP_IRRIGATION_PRACTICE_TS_YEARLY);
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}

	// Check variable efficiency flag (component is in the diversions
	// group)...

	if ( __ieffmax != 0 ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType (
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}
	if ( __iday != 0 ) {	// Else already check above
		comp = getComponentForComponentType (
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY );
		if ( comp != null ) {
			comp.setVisible ( visibility );
		}
	}

	// Check the soil moisture flag (component is in the diversions
	// group)...

	if ( __soild != 0.0 ) {
		visibility = true;
	}
	else {	visibility = false;
	}
	comp = getComponentForComponentType ( COMP_SOIL_MOISTURE );
	if ( comp != null ) {
		comp.setVisible ( visibility );
	}

	// Hide the Network (Graphical) file until it is fully implemented...

	comp = getComponentForComponentType ( COMP_NETWORK );
	if (comp != null && comp.hasData()) {
		comp.setVisible ( true );
	}
}

/**
Determine whether a StateMod response file is free format.  The response file
is opened and checked for a non-commented line with the string "Control"
followed by "=".
@param filename Full path to the StateMod response file.
*/
private void checkForFreeFormat ( String filename )
{	__is_free_format = false;
	Message.printStatus ( 1, "StateMod_DataSet.checkForFreeFormat",
	"Trying to read free-format response file." );
	PropList response_props = new PropList ( "Response" );
	response_props.setPersistentName ( filename );
	try {	response_props.readPersistent();
	}
	catch ( Exception e ) {
		// Assume old fixed format.
		return;
	}
	String prop_val = response_props.getValue ( "Control" );
	if ( prop_val != null ) {
		__is_free_format = true;
		Message.printStatus ( 1, "StateMod_DataSet.checkForFreeFormat",
			"Detected free-format response file." );
	}
	else {	Message.printStatus ( 1, "StateMod_DataSet.checkForFreeFormat",
		"Response file is not free format." );
	}
}

/**
Create a Network from a StateMod river network data set component.
*/
/* TODO SAM 2007-02-18 Evaluate whether needed
private Network createNetworkFromStateModRiverNetwork()
{	Network network = new Network ();
	
	DataSetComponent comp = getComponentForComponentType(
					COMP_RIVER_NETWORK );
	if ( comp == null ) {
		return null;
	}
	Vector net_data = (Vector)comp.getData();
	if ( net_data == null ) {
		return null;
	}
	Vector div_data = (Vector)getComponentForComponentType(
				COMP_DIVERSION_STATIONS).getData();
	Vector res_data = (Vector)getComponentForComponentType(
				COMP_RESERVOIR_STATIONS).getData();
	Vector isf_data = (Vector)getComponentForComponentType(
				COMP_INSTREAM_STATIONS).getData();
	Vector wel_data = (Vector)getComponentForComponentType(
				COMP_WELL_STATIONS).getData();
	Vector sta_data = (Vector)getComponentForComponentType(
				COMP_STREAMGAGE_STATIONS).getData();
	StateMod_Data smdata = null;
	StateMod_StreamGage stadata = null;
	StateMod_RiverNetworkNode rivnode = null;
	int pos = 0;	// Position of a data object in the above data vectors.
	String id = "";
	String warning = "";

	// First add all the features to the network...

	int size = net_data.size();
	for ( int i = 0; i < size; i++ ) {
		rivnode = (StateMod_RiverNetworkNode)net_data.elementAt(i);
		id = rivnode.getID();
		if ( id.equals("") ) {
			warning += "\nRiver node [" + i + "] has no identifier";
			continue;
		}
		//Message.printStatus ( 1, routine, "Adding node \"" + id+"\"");
		if (	(pos = StateMod_Util.locateIndexFromID(
			id, div_data)) >= 0 ) {
			smdata = (StateMod_Data)div_data.elementAt(pos);
			network.addFeature ( new StateMod_Diversion_Node( id,
				smdata.getName() ) );
		}
		else if((pos = StateMod_Util.locateIndexFromID(
			id, res_data)) >= 0 ) {
			smdata = (StateMod_Data)res_data.elementAt(pos);
			network.addFeature ( new StateMod_Reservoir_Node( id,
				smdata.getName() ) );
		}
		else if((pos = StateMod_Util.locateIndexFromID(
			id, isf_data)) >= 0 ) {
			smdata = (StateMod_Data)isf_data.elementAt(pos);
			network.addFeature ( new StateMod_InstreamFlow_Node( id,
				smdata.getName() ) );
		}
		else if((pos = StateMod_Util.locateIndexFromID(
			id, wel_data)) >= 0 ) {
			smdata = (StateMod_Data)wel_data.elementAt(pos);
			network.addFeature ( new StateMod_Well_Node( id,
				smdata.getName() ) );
		}
		else if((pos = StateMod_Util.locateIndexFromID(
			id, wel_data)) >= 0 ) {
			stadata =(StateMod_StreamGage)sta_data.elementAt(pos);
			// In the river station file but only truly a stream
			// station if it has historical time series..
			// REVISIT - this is handled better now.
			if (	(stadata.getHistoricalMonthTS() != null) ||
				(stadata.getHistoricalDayTS() != null) ) {
				network.addFeature (
				new StateMod_StreamGage_Node( id,
				smdata.getName() ) );
			}
			else {	// Add as an "other" node - probably included
				// as a base flow node...
				network.addFeature ( new StateMod_Other_Node(
					id, rivnode.getName() ) );
			}
		}
		else {	network.addFeature ( new StateMod_Other_Node( id,
				rivnode.getName() ) );
			warning += "\nRiver node \"" + id +
				"\" not found in station files.  Adding as " +
				"OTHER type.";
		}
	}
	if ( !warning.equals("") ) {
		//Message.printWarning ( 1, routine, warning );
	}

	return network;
}
*/

/**
Helper method to check to see whether a file is empty.  Traditionally, StateMod
data files have been set to "xxxx.dum" or "dummy", which were non-existent or
empty files, as place-holders for data.  This can cause read errors if code
attempts to read the files.
@param filename Name of file to check.
@return true if the file is empty (therefore don't try to read) or false if the
file does not exist or exists but has zero size.
*/
private static boolean fileIsEmpty ( String filename )
{	File f = new File ( filename );
	if ( !f.exists() || (f.length() == 0L) ) {
		return true;	// File is empty.
	}
	return false;	// File might have data.
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable {
	super.finalize();
}

/**
Retrieve detailed call water right ID
@return __ccall
*/
public String getCcall() {
	return __ccall;
}

/**
Get the factor for converting reservoir content data to AF
@return __cfacto
*/
public double getCfacto() {
	return __cfacto;
}

/**
Return the component's data file name for a requested time series data type.
@return the component's data file name for a requested time series data type,
or "".
@param data_type The time series data type to match, as per
__component_ts_data_types.
@param interval A data interval TimeInterval.MONTH or TimeInterval.DAY.
*/
public String getComponentDataFileNameFromTimeSeriesDataType (
					String data_type, int interval )
{	int length = __component_ts_data_types.length;
	for ( int i = 0; i < length; i++ ) {
		if (	__component_ts_data_types[i].equalsIgnoreCase(data_type)
			&& (__component_ts_data_intervals[i] == interval ) ) {
			return getComponentForComponentType(i).
				getDataFileName();
		}
	}
	return "";
}

/**
Return the default file extension for a component.
@return the default file extension for a component.
*/
public String getComponentFileExtension ( int component_type )
{	return __component_file_extensions[component_type];
}

/**
Return the complete array of file extensions.
@return the complete array of file extensions.
*/
public String[] getComponentFileExtensions()
{	return __component_file_extensions;
}

/**
Return the array of component group numbers.
@return the array of component group numbers.
*/
public int[] getComponentGroupNumbers() {
	return __component_groups;
}

/**
Get the calendar/water/irrigation year
@return __cyrl
*/
public int getCyrl() {
	return __cyrl;
}

/**
Determine the full path to a component data file.  This prepends the data set
directory to the component data file name (which may be relative to the data
set directory).  See also getDataFilePathAbsolute().
@param file File name(e.g., from component getFileName()).
@return Full path to the data file, relative to the data set directory if the
component data file is relative.
*/
public String getDataFilePath ( String file )
{	if ( IOUtil.isAbsolute( file ) ) {
		// Make sure that the path has the leading drive from the
		// data set...
		File f = new File ( file );
		if ( f.isAbsolute() ) {
			return file;
		}
		else {	// Has a leading slash but needs the drive.  If for some
			// reason the data set directory does not have a drive,
				// an empty string will be returned.
			return IOUtil.getDrive(getDataSetDirectory()) + file;
		}
	}
	else { return getDataSetDirectory() + File.separator + file;
	}
}

/**
Determine the full path to a component data file, including accounting for the
working directory.  If the file is already an absolute path, the same value is
returned.  Otherwise, the data set directory is prepended to the component data
file name (which may be relative to the data set directory) and then calls
IOUtil.getPathUsingWorkingDir().
@param comp Data set component.
@return Full path to the data file (absolute), using the working directory.
*/
public String getDataFilePathAbsolute ( DataSetComponent comp )
{	String file = comp.getDataFileName();
	return getDataFilePathAbsolute ( file );
}

/**
Determine the full path to a component data file, including accounting for the
working directory.  If the file is already an absolute path, the same value is
returned.  Otherwise, the data set directory is prepended to the component data
file name (which may be relative to the data set directory) and then calls
IOUtil.getPathUsingWorkingDir().
@param file File name(e.g., from component getFileName()).
@return Full path to the data file (absolute), using the working directory.
*/
public String getDataFilePathAbsolute ( String file )
{	if ( IOUtil.isAbsolute(file) ) {
		return file;
	}
	else {	return IOUtil.getPathUsingWorkingDir(
			getDataSetDirectory() + File.separator + file);
	}
}

/**
Returns the file name for the given component, relative to the main directory
for the state mod files.
@param comp the number of the component to return the relative path for.
@return the relative path file name of the component's data file.
*/
public String getDataFilePathRelative ( int comp ) {
	return getDataFilePathRelative ( 
		getComponentForComponentType(comp).getDataFileName());
}

/**
Returns the path to the passed-in filename, relative to the main directory
for the state mod files.
@param file a filename 
@return the relative path of the file from the state mod directory, or if it
is on another drive, the full path.
*/
public String getDataFilePathRelative ( String file ) {
	if (file.trim().startsWith(".")) {
		if (file.trim().startsWith("." + File.separator)) {
			file = file.substring(2);
		}
		// already in relative format
		return file;
	}

	if (file.indexOf(File.separator) > -1) {
		// file has a directory structure, convert to relative
		try {
			return IOUtil.toRelativePath(getDataSetDirectory(),
				file);
		}
		catch (Exception e) {
			return getDataFilePathAbsolute(file);
		}		
	}
	else {
		// just a filename, return as is
		return file;
	}
}

/**
Return a concatenation of the data component data check results.
The results are listed in the order of data components.
@return a concatenation of the data component data check results.
*/
public Vector getDataCheckResults ()
{	// Loop through the components and append to the overall data check
	// results...
	Vector check_Vector = new Vector();
	Vector data_Vector = getComponents();
	int size = 0;
	if ( data_Vector != null ) {
		size = data_Vector.size();
	}
	DataSetComponent comp;
	Vector check_Vector2 = null;	// Data check comments from a component
	for ( int i = 0; i < size; i++ ) {
		comp = (DataSetComponent)data_Vector.elementAt(i);
		if ( comp.isGroup() ) {
			// Process components within the group...
			Vector data2 = (Vector)comp.getData();
			int size2 = 0;
			if ( data2 != null ) {
				size2 = data2.size();
			}
			for ( int j = 0; j < size2; j++ ) {
				comp = (DataSetComponent)data2.elementAt(j);
				// Append the data check results, if
				// available...
				check_Vector2 = comp.getDataCheckResults();
				if (	(check_Vector2 != null) &&
					(check_Vector2.size() > 0) ) {
					check_Vector.addElement ( "" );
					check_Vector.addElement (
					"====================================" +
					"====================================" +
					"========" );
					check_Vector.addElement (
						"DATA CHECK RESULTS FOR: " +
						comp.getComponentName() );
					check_Vector.addElement ( "" );
					check_Vector =
						StringUtil.addListToStringList (
						check_Vector,
					comp.getDataCheckResults() );
				}
			}
		}
	}
	if ( check_Vector.size() > 0 ) {
		check_Vector.insertElementAt ( "", 0 );
		check_Vector.insertElementAt ( "Data check results are " +
		"listed for output products that have potential data issues.",
		1);
		check_Vector.insertElementAt (
		"The following data check results are listed in order of " +
		"StateDMI components (data products).", 2 );
		check_Vector.insertElementAt ( "", 3 );
	}
	return check_Vector;
}

/**
Return a Vector of String containing information about
an object.  This is useful, for example, to see what reservoirs use an
evaporation station, what diversions use a delay table, etc.).
@param comp_type Component type to examine.
@param id Identifier of specific object to examine.
*/
public Vector getDataObjectDetails ( int comp_type, String id )
{	Vector v = new Vector();
	DataSetComponent comp1;			// "1" corresponds to "id" above
	Vector data1;
	int pos1;
	DataSetComponent comp2;			// "2" corresponds to the first
	Vector data2;				// level of related data
	int size2;
	Vector data3;				// level of related data
	int size3;
	StateMod_Diversion div;			// All these are for general use
	StateMod_RiverNetworkNode netnode;	// below
	StateMod_Reservoir res;
	StateMod_ReservoirClimate climate;
	if ( comp_type == COMP_DIVERSION_STATIONS ) {
		comp1 = getComponentForComponentType (
				StateMod_DataSet.COMP_DIVERSION_STATIONS );
		data1 = (Vector)comp1.getData();
		pos1 = StateMod_Util.indexOf ( data1, id );
		div = (StateMod_Diversion)data1.elementAt(pos1);
		v.addElement("Data set details for diversion: "+
			StateMod_Util.formatDataLabel ( id, div.getName()));
		v.addElement ( "Network nodes that are directly upstream:" );
		v.addElement ( "" );
		v.addElement (
		"Upstream" );
		v.addElement (
		"Node ID       Upstream Node Name                  " );
		v.addElement (
		"--------------------------------------------------" );
		comp2 = getComponentForComponentType (
				StateMod_DataSet.COMP_RIVER_NETWORK );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		for ( int i = 0; i < size2; i++ ) {
			netnode = (StateMod_RiverNetworkNode)data2.elementAt(i);
			if ( netnode.getCstadn().equalsIgnoreCase(id) ) {
				v.addElement (
				StringUtil.formatString(
					netnode.getID(),"%-12.12s")+ "  " + 
				StringUtil.formatString(netnode.getName(),
					"%-24.24s") );
			}
		}
		v.addElement ( "" );
		v.addElement (
		"Search of nodes that return to this diversion - " +
		"not implemented" );
		v.addElement ( "" );
		v.addElement (
		"Search of diversion rights for this diversion - " +
		"not implemented" );
		v.addElement ( "" );
		v.addElement (
		"Search of operational rights using this diversion - " +
		"not implemented" );
		v.addElement ( "" );
		v.addElement (
		"Search of stream estimate node for this diversion - " +
		"not implemented" );
		v.addElement ( "" );
		v.addElement (
		"Search of time series for this diversion - " +
		"not implemented" );
		v.addElement (
		"Search of soil moisture for this diversion - " +
		"not implemented" );
		v.addElement (
		"Search of irrigation practice for diversion - " +
		"not implemented" );
	}
	else if ( comp_type == COMP_PRECIPITATION_TS_MONTHLY ) {
		v.addElement("Data set details for precipitation time series: "+
		id );
		v.addElement ( "Reservoirs that use the time series:" );
		v.addElement ( "" );
		v.addElement (
		"Res ID        Res Name                  Weight (%)" );
		v.addElement (
		"--------------------------------------------------" );
		comp2 = getComponentForComponentType (
				StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		for ( int i = 0; i < size2; i++ ) {
			res = (StateMod_Reservoir)data2.elementAt(i);
			data3 = res.getClimates();
			size3 = data3.size();
			for ( int j = 0; j < size3; j++ ) {
				climate = (StateMod_ReservoirClimate)
					data3.elementAt(j);
				if (	(climate.getType() ==
					StateMod_ReservoirClimate.CLIMATE_PTPX)
					&&climate.getID().equalsIgnoreCase(id)){
					// Found a reservoir that uses the
					// station...
					v.addElement (
					StringUtil.formatString(
						res.getID(),"%-12.12s")+ "  " + 
					StringUtil.formatString(res.getName(),
						"%-24.24s") + "  " +
					StringUtil.formatString(
					climate.getWeight(),"%8.2f") );
				}
			}
		}
	}
	else if ( comp_type == COMP_EVAPORATION_TS_MONTHLY ) {
		v.addElement ( "Data set details for evaporation time series: "+
		id );
		v.addElement ( "Reservoirs that use the time series:" );
		v.addElement ( "" );
		v.addElement (
		"Res ID        Res Name                  Weight (%)" );
		v.addElement (
		"--------------------------------------------------" );
		comp2 = getComponentForComponentType (
				StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		for ( int i = 0; i < size2; i++ ) {
			res = (StateMod_Reservoir)data2.elementAt(i);
			data3 = res.getClimates();
			size3 = data3.size();
			for ( int j = 0; j < size3; j++ ) {
				climate = (StateMod_ReservoirClimate)
					data3.elementAt(j);
				if (	(climate.getType() ==
					StateMod_ReservoirClimate.CLIMATE_EVAP)
					&&climate.getID().equalsIgnoreCase(id)){
					// Found a reservoir that uses the
					// station...
					v.addElement (
					StringUtil.formatString(
						res.getID(),"%-12.12s")+ "  " + 
					StringUtil.formatString(res.getName(),
						"%-24.24s") + "  " +
					StringUtil.formatString(
					climate.getWeight(),"%8.2f") );
				}
			}
		}
	}
	return v;
}

/**
Return the data set type name.  This method calls lookupTypeName()for the
instance.
@return the data set type name.
*/
public String getDataSetTypeName() {
	return lookupTypeName(getDataSetType());
}

/**
Return a DateTime corresponding to the latest data.  Daily precision is always
returned and calendar dates are used.
@return a DateTime corresponding to the latest data.
*/
public DateTime getDataEnd ()
{	DateTime d = StateMod_Util.findLatestDateInPOR ( this );
	d.setDay ( TimeUtil.numDaysInMonth(d.getMonth(), d.getYear()) );
	return d;
}

/**
Return a DateTime corresponding to the earliest data.  Daily precision is always
returned and calendar dates are used.
@return a DateTime corresponding to the earliest data.
*/
public DateTime getDataStart ()
{	DateTime d = StateMod_Util.findEarliestDateInPOR ( this );
	d.setDay ( 1 );
	return d;
}

/**
Get the divisor for diversion data units
@return __dfacto
*/
public double getDfacto() {
	return __dfacto;
}

/**
Get the factor for converting evaporation data to FT
@return __efacto
*/
public double getEfacto() {
	return __efacto;
}

/**
Return factor for converting CFS to AF/Day (1.9835).
@return __factor
*/
public double getFactor() {
	return __factor;
}

/**
Get the divisor for instreamflow data units
@return __ffacto
*/
public double getFfacto() {
	return __ffacto;
}

/**
Get the gwmaxrc value.
@return __gwmaxrc
*/
public double getGwmaxrc() {
	return __gwmaxrc;
}

/**
Return first line of heading.
@return __heading1
*/
public String getHeading1() {
	return __heading1;
}

/**
Return second line of heading.
@return __heading2
*/
public String getHeading2() {
	return __heading2;
}

/**
Get the switch for detailed call data
@return __icall
*/
public int getIcall() {
	return __icall;
}

/**
Get the switch for detailed printout
@return __ichk
*/
public int getIchk() {
	return __ichk;
}

/**
Get the switch for demand printout
@return __icondem
*/
public int getIcondem() {
	return __icondem;
}

/**
Get the switch for daily calculations
The hasDailyData() should be used in most cases.  Only use getIday() in code
that is changing/writing the raw value.
@return __iday
*/
public int getIday() {
	return __iday;
}

/**
Get the ieffmax value.
The hasIrrigationWaterRequirementData() should be used in most cases.  Only use
getIeffmax() in code that is changing/writing the raw value.
@return __ieffmax
*/
public int getIeffmax() {
	return __ieffmax;
}

/**
Retrieve max number of entries in a delay pattern.
@return __interv
*/
public int getInterv() {
	return __interv;
}

/**
Retrieve streamflow type(total or gains streamflow).
@return __iopflo
*/
public int getIopflo() {
	return __iopflo;
}

/**
Get the switch for instream flow reach approach
The hasMonthlyISFData() should be used in most cases.  Only use getIreach() in
code that is changing/writing the raw value.
@return __ireach
*/
public int getIreach() {
	return __ireach;
}

/**
Get the switch for reoperation control.
@return __ireopx
*/
public int getIreopx() {
	return __ireopx;
}

/**
Return switch for output.
@return __iresop
*/
public int getIresop() {
	return __iresop;
}

/**
Return switch for output significant figures.
@return __isig
*/
public int getIsig() {
	return __isig;
}

/**
Get the isjrip value.
The hasSanJuanData() should be used in most cases.  Only use getIsjrip() in code
that is changing/writing the raw value.
@return __isjrip
*/
public int getIsjrip() {
	return __isjrip;
}

/**
Get the isprink value.
@return __isprink
*/
public int getIsprink() {
	return __isprink;
}

/**
Get the itsfile value.
The hasIrrigationPracticeData() should be used in most cases.  Only use
getItsfile() in code that is changing/writing the raw value.
@return __itsfile
*/
public int getItsfile() {
	return __itsfile;
}

/**
Get the switch for well calculations.
The hasWellData() should be used in most cases.  Only use getIwell() in code
that is changing/writing the raw value.
@return __iwell
*/
public int getIwell() {
	return __iwell;
}

/**
Return ending year of the simulation.
@return __iyend
*/
public int getIyend() {
	return __iyend;
}

/**
Return starting year of the simulation.
@return __iystr
*/
public int getIystr() {
	return __iystr;
}

/**
Return a Vector of String containing information about modified data in the data
set.  This can be used during development to see how a GUI modifies data when
it is set.
*/
public Vector getModifiedDataSummary ()
{	Vector v = new Vector();

	v.addElement (
	"Summary of data objects that have been modified in computer memory" );
	v.addElement (
	"but not yet written to files." );
	v.addElement (
	"Components are listed by data group and files within each group." );
	v.addElement ( "" );

	DataSetComponent comp1;
	Vector data1;
	int size1;

	// Stream gage...

	v.addElement ( "" );
	v.addElement ( "Stream Gage Data are not checked." );
	v.addElement ( "" );

	// Delay Table (Monthly)...

	v.addElement ( "" );
	v.addElement ( "Delay Tables (Monthly) are not checked." );
	v.addElement ( "" );

	// Delay Table (Daily)...

	v.addElement ( "" );
	v.addElement ( "Delay Tables (Daily) are not checked." );
	v.addElement ( "" );

	// Diversions...

	comp1 = getComponentForComponentType ( COMP_DIVERSION_STATIONS );
	if ( comp1.hasData() ) {
	v.addElement ( comp1.getComponentName() );
	v.addElement ( "" );
	v.addElement (
	"Diversion ID  Diversion Name" );
	v.addElement ( "-----------------------------------------");
	data1 = (Vector)comp1.getData();
	size1 = data1.size();
	StateMod_Data smdata1;
	for ( int i = 0; i < size1; i++ ) {
		smdata1 = (StateMod_Data)data1.elementAt(i);
		if ( smdata1.isDirty() ) {
			v.addElement (
			StringUtil.formatString(
				smdata1.getID(),"%-12.12s")+"  "+
			StringUtil.formatString(
				smdata1.getName(), "%-24.24s") );
		}
	}
	} // End comp1.hasData()

	comp1 = getComponentForComponentType ( COMP_DIVERSION_RIGHTS );
	if ( comp1.hasData() ) {
	v.addElement ( "" );
	v.addElement ( comp1.getComponentName() );
	v.addElement ( "" );
	v.addElement (
	"Div Right ID   Diversion Name" );
	v.addElement ( "-----------------------------------------");
	data1 = (Vector)comp1.getData();
	size1 = data1.size();
	StateMod_Data smdata1;
	for ( int i = 0; i < size1; i++ ) {
		smdata1 = (StateMod_Data)data1.elementAt(i);
		if ( smdata1.isDirty() ) {
			v.addElement (
			StringUtil.formatString(
				smdata1.getID(),"%-12.12s")+"  "+
			StringUtil.formatString(
				smdata1.getName(), "%-24.24s") );
		}
	}
	} // End comp1.hasData()

	v.addElement ( "" );
	v.addElement ( "Diversion time series data are not checked." );
	v.addElement ( "" );

	// Precipitation time series (monthly)...

	v.addElement ( "" );
	v.addElement ( "Precipitation data are not checked." );
	v.addElement ( "" );

/*
	comp1 = getComponentForComponentType ( COMP_PRECIPITATION_TS_MONTHLY );
	if ( comp1.hasData() ) {
	v.addElement ( comp1.getComponentName() );
	v.addElement ( "" );
	v.addElement (
	"Precip TS ID   Precip TS Name" );
	v.addElement ( "-----------------------------------------");
	data1 = (Vector)comp1.getData();
	size1 = data1.size();
	String id;
	boolean found = false;
	for ( int i = 0; i < size1; i++ ) {
		found = false;
		ts = (TS)data1.elementAt(i);
		id = ts.getLocation();
		StateMod_Reservoir res;
		comp2 = getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		Vector climates;
		StateMod_ReservoirClimate climate;
		for ( int j = 0; j < size2; j++ ) {
			res = (StateMod_Reservoir)data2.elementAt(j);
			climates = res.getClimates();
			size3 = climates.size();
			for ( int k = 0; k < size3; k++ ) {
				climate = (StateMod_ReservoirClimate)
					climates.elementAt(k);
				if (	(climate.getType() ==
					StateMod_ReservoirClimate.CLIMATE_PTPX)
					&&climate.getID().equalsIgnoreCase(id)){
					// Found a reservoir that uses the
					// station...
					found = true;
					break;
				}
			}
		}
		if ( !found ) {
			// The precipitation station is not used in the data
			// set so print...
			v.addElement (
				StringUtil.formatString(id,"%-12.12s") + "  " + 
				StringUtil.formatString( ts.getDescription(),
				"%-24.24s") );
		}
	}
	} // End if comp1.hasData()
*/

	// Evaporation time series (monthly)...

	v.addElement ( "" );
	v.addElement ( "Evaporation data are not checked." );
	v.addElement ( "" );

/*
	comp1 = getComponentForComponentType ( COMP_EVAPORATION_TS_MONTHLY );
	if ( comp1.hasData() ) {
	v.addElement ( comp1.getComponentName() );
	v.addElement ( "" );
	v.addElement (
	"Evap TS ID    Evap TS Name" );
	v.addElement ( "-----------------------------------------");
	data1 = (Vector)comp1.getData();
	size1 = data1.size();
	String id;
	boolean found = false;
	for ( int i = 0; i < size1; i++ ) {
		found = false;
		ts = (TS)data1.elementAt(i);
		id = ts.getLocation();
		StateMod_Reservoir res;
		comp2 = getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		Vector climates;
		StateMod_ReservoirClimate climate;
		for ( int j = 0; j < size2; j++ ) {
			res = (StateMod_Reservoir)data2.elementAt(j);
			climates = res.getClimates();
			size3 = climates.size();
			for ( int k = 0; k < size3; k++ ) {
				climate = (StateMod_ReservoirClimate)
					climates.elementAt(k);
				if (	(climate.getType() ==
					StateMod_ReservoirClimate.CLIMATE_EVAP)
					&&climate.getID().equalsIgnoreCase(id)){
					// Found a reservoir that uses the
					// station...
					found = true;
					break;
				}
			}
		}
		if ( !found ) {
			// The evaporation station is not used in the data
			// set so print...
			v.addElement (
				StringUtil.formatString(id,"%-12.12s") + "  " + 
				StringUtil.formatString( ts.getDescription(),
				"%-24.24s") );
		}
	}
	} // End if comp1.hasData()
*/

	// Reservoirs...

	v.addElement ( "" );
	v.addElement ( "Reservoirs are not checked." );
	v.addElement ( "" );

	// Instream flows...

	v.addElement ( "" );
	v.addElement ( "Instream flows are not checked." );
	v.addElement ( "" );

	// Wells...

	v.addElement ( "" );
	v.addElement ( "Wells are not checked." );
	v.addElement ( "" );

	// Plans...

	v.addElement ( "" );
	v.addElement ( "Plans are not checked." );
	v.addElement ( "" );

	// Stream Estimate stations...

	v.addElement ( "" );
	v.addElement ( "Stream estimate stations are not checked." );
	v.addElement ( "" );

	// River network...

	v.addElement ( "" );
	v.addElement ( "River network data are not checked." );
	v.addElement ( "" );

	// Operational rights.

	v.addElement ( "" );
	v.addElement ( "Operational rights data are not checked." );
	v.addElement ( "" );

	return v;
}

/**
Retrieve type of evaporation data, monthly or average.
@return __moneva
*/
public int getMoneva() {
	return __moneva;
}

/**
Return number of evaporation stations.
@return __numeva
*/
public int getNumeva() {
	return __numeva;
}

/**
Return number of precipitation stations.
@return __numpre
*/
public int getNumpre() {
	return __numpre;
}

/**
Get the factor for converting precipitation data to FT
@return __pfacto
*/
public double getPfacto() {
	return __pfacto;
}

/**
Return the divisor for streamflow data units.
@return __rfacto
*/
public double getRfacto() {
	return __rfacto;
}

/**
Return a DateTime corresponding to the run end.  Daily precision is always
returned and calendar dates are used.
@return a DateTime corresponding to the run end.
*/
public DateTime getRunEnd ()
{	DateTime d = new DateTime ( DateTime.PRECISION_DAY );
	if ( __cyrl == SM_WYR ) {
		d.setMonth ( 9 );
	}
	else if ( __cyrl == SM_IYR) {
		d.setMonth ( 10 );
	}
	else {	// Calendar...
		d.setMonth ( 12 );
	}
	d.setYear ( __iyend );
	d.setDay ( TimeUtil.numDaysInMonth(d.getMonth(), d.getYear()) );
	return d;
}

/**
Return a DateTime corresponding to the run start.  Daily precision is always
returned and calendar dates are used.
@return a DateTime corresponding to the earliest data.
*/
public DateTime getRunStart ()
{	DateTime d = new DateTime ( DateTime.PRECISION_DAY );
	d.setDay ( 1 );
	if ( __cyrl == SM_WYR ) {
		d.setMonth ( 10 );
		d.setYear ( __iystr - 1 );
	}
	else if ( __cyrl == SM_IYR) {
		d.setMonth ( 11 );
		d.setYear ( __iystr - 1 );
	}
	else {	// Calendar...
		d.setMonth ( 1 );
		d.setYear ( __iystr );
	}
	return d;
}

/**
Get the value of soild. 
The hasSoilMoistureData() should be used in most cases.  Only use
getSoild() in code that is changing/writing the raw value.
@return _solid
*/
public double getSoild() {
	return __soild;
}

/**
Return the StateMod response file property for a component.
@return the StateMod response file property for a component.
*/
public String getStateModFileProperty ( int component_type )
{	return __statemod_file_properties[component_type];
}

/**
Return a summary of the data set.
@return a summary of the data set as a Vector of String.
*/
public Vector getSummary()
{	Vector data_Vector = null;	// Reuse as needed below.
	Vector rights_Vector = null;	// Reuse as needed below.
	Vector infoVector = new Vector (100);
	infoVector.addElement("                                STATEMOD "
		+ "DATA SET SUMMARY");
	infoVector.addElement("Basin                 : " 
		+ getHeading1().trim());
	infoVector.addElement("Base name (from *.rsp): " + getBaseName() );
	if ( !areTSRead() ) {
		infoVector.addElement("Note:     " +
		"You elected NOT to read the time series information");
		infoVector.addElement(
		"          after initially selecting this scenario.");
		infoVector.addElement(
		"          Minimal time series information can be " +
		"provided here.");
	}
	infoVector.addElement("");

	infoVector.addElement("RIVER NETWORK:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_RIVER_NETWORK).getData();
	int size = data_Vector.size();
	infoVector.addElement("Number of nodes/stations in network: " + size);

	infoVector.addElement("");
	infoVector.addElement("STREAM GAGE STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMGAGE_STATIONS).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of stream gage stations: " + size);
	infoVector.addElement("Stream gage stations without geographic " +
			"locations:");
	infoVector.addElement("             # ID           Name");
	StateMod_StreamGage sta;
	int count = 0;
	for (int i = 0; i < size; i++) {
		sta = (StateMod_StreamGage)data_Vector.elementAt(i);
		if ( sta.getGeoRecord() == null ) {
			infoVector.addElement("        " +
			StringUtil.formatString((i + 1),"%6d") + " " +
			StringUtil.formatString(sta.getID(),"%-12.12s")+
			" " + sta.getName());
			++count;
		}
	}
	infoVector.addElement("        " +
	StringUtil.formatString(count,"%6d") + " Total missing");
	infoVector.addElement("");

	infoVector.addElement("STREAM ESTIMATE STATIONS (PRORATED FLOW):");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of stream estimate stations: " + size);
// REVISIT - do actual counts of non-null time series links.
/*
	infoVector.addElement("Number of river stations with estimated " +
		"baseflows: " + _baseflowsVector.size());
	infoVector.addElement("Number of stations with monthly base flow " +
		"time series: " + _baseflowTSVector.size());
	if (controls.hasDailyData(false)&& _areTSRead) {
		infoVector.addElement("Number of river stations with " +
			"historic daily time series: " +
			_dailyHistStreamflowTSVector.size());
		infoVector.addElement(
		"Number of stations with daily base flow time series: " +
		_dailyBaseflowTSVector.size());
	}
*/
	infoVector.addElement("Stream estimate stations without geographic " +
	"locations:");
	infoVector.addElement("             # ID           Name");
	StateMod_StreamEstimate bsta;
	count = 0;
	for (int i = 0; i < size; i++) {
		bsta = (StateMod_StreamEstimate)data_Vector.elementAt(i);
		if ( bsta.getGeoRecord() == null ) {
			infoVector.addElement("        " +
			StringUtil.formatString((i + 1),"%6d") + " " +
			StringUtil.formatString(bsta.getID(),"%-12.12s")+
			" " + bsta.getName());
			++count;
		}
	}
	infoVector.addElement("        " +
	StringUtil.formatString(count,"%6d") + " Total missing");
	infoVector.addElement("");

	infoVector.addElement("DELAY TABLE:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of delay tables (monthly): " + size);
	if ( hasDailyData(false) ) {
		data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_DELAY_TABLES_DAILY).getData();
		size = data_Vector.size();
		infoVector.addElement("Number of delay tables (daily): " +size);
	}
	infoVector.addElement("");

	infoVector.addElement("DIVERSION STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_STATIONS).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of diversion stations: " + size);
	rights_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_DIVERSION_RIGHTS).getData();
	int rsize = rights_Vector.size();
	infoVector.addElement("Number of rights: " + rsize );
	if ( areTSRead() ) {
/* REVISIT - need actual counts...
		infoVector.addElement(
			"Number of historical monthly time series: " + 
			_divHistTSVector.size());
		infoVector.addElement(
			"Number of monthly demand time series: " + 
			_demandTSVector.size());
		if (controls.hasDailyData(false)) {
			infoVector.addElement(
				"Number of historical daily time series: " + 
				_dailyHistDiversionTSVector.size());
			infoVector.addElement(
				"Number of daily demand time series: " + 
				_dailyDemandTSVector.size());
		}
*/
	}
	infoVector.addElement("Diversion stations without geographic "
		+ "locations:");
	infoVector.addElement("             # ID           Name");
	StateMod_Diversion div;
	count = 0;
	for (int i = 0; i < size; i++) {
		div = (StateMod_Diversion)data_Vector.elementAt(i);
		if ( div.getGeoRecord() == null ) {
			infoVector.addElement("        " +
			StringUtil.formatString((i + 1),"%6d") + " " +
			StringUtil.formatString(div.getID(),"%-12.12s") +
			" " + div.getName());
			++count;
		}
	}
	infoVector.addElement("        " +
	StringUtil.formatString(count,"%6d") + " Total missing");
	infoVector.addElement("");

	infoVector.addElement("INSTREAM FLOW STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_STATIONS).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of instream flow stations:" + size);
	rights_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_INSTREAM_RIGHTS).getData();
	rsize = rights_Vector.size();
	infoVector.addElement("Number of rights: " + rsize );
	if ( areTSRead() ) {
/* REVISIT 
		infoVector.addElement("Number of monthly demand time series: "+
			_isfMonthlyDemandTSVector.size());
		infoVector.addElement(
			"Number of monthly average demand time series: " + 
			_isfDemandTSVector.size());
		if (controls.hasDailyData(false)) {
			infoVector.addElement(
			"Number of daily instream flow demand time series: "+
			_dailyInsfTSVector.size());
		}
*/
	}
	infoVector.addElement("Instream flow stations without "
		+ "geographic locations:");
	infoVector.addElement("             # ID           Name");
	StateMod_InstreamFlow isf;
	count = 0;
	for (int i = 0; i < size; i++) {
		isf = (StateMod_InstreamFlow)data_Vector.elementAt(i);
		if ( isf.getGeoRecord() == null ) {
			infoVector.addElement("        " +
			StringUtil.formatString((i + 1),"%6d") + " " +
			StringUtil.formatString(isf.getID(),"%-12.12s") +
			" " + isf.getName());
			++count;
		}
	}
	infoVector.addElement("        " +
	StringUtil.formatString(count,"%6d") + " Total missing");
	infoVector.addElement("");

	infoVector.addElement("PRECIPITATION STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY).getData();
	size = data_Vector.size();
	infoVector.addElement(
		"Number of monthly precipitation time series: " + size );
	infoVector.addElement("");

	infoVector.addElement("EVAPORATION STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY).getData();
	size = data_Vector.size();
	infoVector.addElement(
		"Number of monthly evaporation time series: " + size );
	infoVector.addElement("");

	infoVector.addElement("RESERVOIR STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_STATIONS).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of reservoir stations: " + size);
	rights_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_RESERVOIR_RIGHTS).getData();
	rsize = rights_Vector.size();
	infoVector.addElement("Number of rights: " + rsize );
	if ( areTSRead() ) {
/* REVISIT
		infoVector.addElement(
			"Number of historical end of month time series: "+ 
			_eomVector.size());
		infoVector.addElement(
			"Number of monthly min/max target time series: "+
			_minMaxVector.size()/ 2);
		if (controls.hasDailyData(false)) {
			infoVector.addElement(
			"Number of historical end of day time series: "+ 
			_dailyHistResEODTSVector.size());
			infoVector.addElement(
			"Number of daily min/max daily target time series: "+
			_dailyResTargetTSVector.size()/ 2);
		}
*/
	}
	infoVector.addElement("Reservoir stations without geographic "
		+ "locations:");
	infoVector.addElement("             # ID           Name");
	StateMod_Reservoir res;
	count = 0;
	for (int i = 0; i < size; i++) {
		res = (StateMod_Reservoir)data_Vector.elementAt(i);
		if (res.getGeoRecord() == null) {
			infoVector.addElement("        " +
			StringUtil.formatString((i + 1),"%6d") + " " +
			StringUtil.formatString(res.getID(),"%-12.12s")
			+ " " + res.getName());
			++count;
		}
	}
	infoVector.addElement("        " +
	StringUtil.formatString(count,"%6d") + " Total missing");
	infoVector.addElement("");

	infoVector.addElement("WELL STATIONS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_WELL_STATIONS).getData();
	size = data_Vector.size();
	infoVector.addElement("Number of well stations: " + size);
	rights_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_WELL_RIGHTS).getData();
	rsize = rights_Vector.size();
	infoVector.addElement("Number of rights: " + rsize );
	if ( areTSRead() ) {
/* REVISIT
		infoVector.addElement(
		"Number of historical monthly well pumping time series: " +
		_wellHistTSVector.size());
		infoVector.addElement(
		"Number of monthly well demand time series: " +
		_wellDemandTSVector.size());
		if (controls.hasDailyData(false)) {
			infoVector.addElement(
			"Number of historical daily well pumping time series: "+
			_dailyHistWellTSVector.size());
			infoVector.addElement(
			"Number of daily well demand time series: "+
			_dailyWellDemandTSVector.size());
		}
*/
	}
	infoVector.addElement("Well stations without geographic locations:");
	infoVector.addElement("             # ID           Name");
	StateMod_Well well;
	count = 0;
	for (int i = 0; i < size; i++) {
		well = (StateMod_Well)data_Vector.elementAt(i);
		if (well.getGeoRecord() == null) {
			infoVector.addElement("        " +
			StringUtil.formatString((i + 1),"%6d") + " " +
			StringUtil.formatString(well.getID(),"%-12.12s")
			+ " " + well.getName());
			++count;
		}
	}
	infoVector.addElement("        " +
	StringUtil.formatString(count,"%6d") + " Total missing");
	infoVector.addElement("");

	infoVector.addElement("OPERATIONAL RIGHTS:");
	infoVector.addElement("");
	data_Vector = (Vector)getComponentForComponentType(
		StateMod_DataSet.COMP_OPERATION_RIGHTS).getData();
	size = data_Vector.size();
	infoVector.addElement( 
		"Type Name                                           "
		+ "                   Count");
	// Figure out the maximum operational right number...
	int ityopr_max = 0;
	StateMod_OperationalRight opright = null;
	for ( int i = 0; i < size; i++ ) {
		opright = (StateMod_OperationalRight)data_Vector.elementAt(i);
		if ( opright.getItyopr() > ityopr_max ) {
			ityopr_max = opright.getItyopr();
		}
	}
	//int count2[] = new int[ityopr_max + 1];	// To allow for 0th "Unknown"
	int count2[] = new int[StateMod_OperationalRight.NAMES.length];
	// Now loop through all the rights and count the number by type...
	for ( int i = 0; i < StateMod_OperationalRight.NAMES.length; i++ ) {
		count2[i] = 0;
	}
	for ( int i = 0; i < size; i++ ) {
		opright = (StateMod_OperationalRight)data_Vector.elementAt(i);
		++count2[opright.getItyopr()];
	}
	String name = null;
	for ( int i = 1; i < ityopr_max; i++ ) {
		// Default for oprights not understood by the software
		name = StateMod_OperationalRight.NAMES[0];
		if ( i <= StateMod_OperationalRight.NAMES.length ) {
			name = StateMod_OperationalRight.NAMES[i];
		}
		infoVector.addElement( " " +
		StringUtil.formatString(i,"%2d") + "  " +
		StringUtil.formatString( name, "%-64.64s") + "  " +
		StringUtil.formatString(count2[i],"%4d"));
	}
	infoVector.addElement( "     Total                               " +
		"                              "+
		StringUtil.formatString(size,"%4d") );
	infoVector.addElement("");

	return infoVector;
}

// REVISIT - need to support other data set or file using the input name part
// of the TSID.
/**
Get the appropriate time series from a data set.  Only input time series
(actual and estimated) are processed.  If the time series does not exist in
memory, it is read from the input file if read_data is true.
@param tsident_string Time series identifier string, for example from the
StateMod GUI graphing tool or a time series product.
@exception if there is an error processing the time series identifier.
*/
public TS getTimeSeries (	String tsident_string,
				DateTime req_date1, DateTime req_date2,
				String req_units, boolean read_data )
throws Exception
{	String routine = "StateMod_DataSet.getTimeSeries";
	TSIdent tsident = new TSIdent ( tsident_string );
	// The identifier in some cases cannot be used directly to find the
	// time series of interest.  For example, "Demand" is used for several
	// time series.  For now, hard code the lookups for the data types, in
	// the order of the components.
	String id = tsident.getLocation();
	String datatype = tsident.getType();
	String interval = tsident.getInterval();
	DataSetComponent comp = null, comp2 = null;
	Vector data = null;
	int pos = 0;
	StateMod_StreamGage gage = null;
	StateMod_StreamEstimate estimate = null;
	StateMod_Diversion div = null;
	StateMod_Reservoir res = null;
	StateMod_InstreamFlow instream = null;
	StateMod_Well well = null;
	Message.printStatus ( 1, routine, "Getting time series for \"" +
		tsident_string + "\"" );
	String fn;	// File name string
	DateTime date1 = null, date2 = null;
	if ( req_date1 != null ) {
		date1 = new DateTime ( req_date1 );
	}
	else {	date1 = getDataStart ();
	}
	if ( req_date2 != null ) {
		date2 = new DateTime ( req_date2 );
	}
	else {	date1 = getDataEnd ();
	}
	TS ts = null;		// Used where a time series must be processed.

	// The general order is by station type (StreamGage, Diversion,
	// Reservoir, InstreamFlow, Well), with exceptions being that parameters
	// shared between data types will be processed with the first station
	// type that uses the data.  Use the lookup methods to request the data
	// types to avoid hard-coded data types.  In some cases, the data types
	// are know to be shared between station types - in these cases lists
	// all the data types at the top, even though some will be exactly the
	// same - in this way it is easier to see that all data types are
	// accounted for.
	if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
				COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY) ) ) {
		// Historical flow...
		// Always a stream gage...
		comp = getComponentForComponentType( COMP_STREAMGAGE_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			gage = (StateMod_StreamGage)data.elementAt(pos);
		}
		// Stream gage data are always in memory...
		if ( interval.equalsIgnoreCase("Month") ) {
			return gage.getHistoricalMonthTS ();
		}
		else if ( interval.equalsIgnoreCase("Day") ) {
			return gage.getHistoricalDayTS ();
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_STREAMGAGE_HISTORICAL_TS_DAILY ) + __ESTIMATED )) {
		// Always a stream gage and daily...
		comp = getComponentForComponentType( COMP_STREAMGAGE_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			gage = (StateMod_StreamGage)data.elementAt(pos);
		}
		// Inputs are always in memory...
		return StateMod_Util.createDailyEstimateTS ( id,
			"Daily historical streamflow estimate",
			datatype, "CFS", gage.getCrunidy(),
			gage.getHistoricalMonthTS(),
			gage.getHistoricalDayTS () );
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
				COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
				COMP_STREAMGAGE_BASEFLOW_TS_DAILY ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
				COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
				COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY ) ) ) {
		// Could be a stream gage or base flow node.  Try the stream
		// gage first...
		comp = getComponentForComponentType( COMP_STREAMGAGE_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			gage = (StateMod_StreamGage)data.elementAt(pos);
			// Stream gage data are always in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				return gage.getBaseflowMonthTS ();
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				return gage.getBaseflowDayTS ();
			}
		}
		// If here then the stream gage did not have the data.  Try the
		// stream estimate.
		comp = getComponentForComponentType(
			COMP_STREAMESTIMATE_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			estimate = (StateMod_StreamEstimate)data.elementAt(pos);
			// Stream estimate data are always in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				return estimate.getBaseflowMonthTS ();
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				return estimate.getBaseflowMonthTS ();
			}
		}
	}
	else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_STREAMGAGE_BASEFLOW_TS_DAILY ) + __ESTIMATED ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY ) + __ESTIMATED)){
		// Could be a stream gage or base flow node but always daily.
		// Try the stream gage first...
		comp = getComponentForComponentType( COMP_STREAMGAGE_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			gage = (StateMod_StreamGage)data.elementAt(pos);
			// Stream gage data are always in memory...
			return StateMod_Util.createDailyEstimateTS ( id,
				"Daily base flow estimate",
				datatype, "CFS", gage.getCrunidy(),
				gage.getBaseflowMonthTS(),
				gage.getBaseflowDayTS () );
		}
		// If here then the stream gage did not have the data...
		comp = getComponentForComponentType(
			COMP_STREAMESTIMATE_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			estimate = (StateMod_StreamEstimate)data.elementAt(pos);
			// Stream estimate data are always in memory...
			return StateMod_Util.createDailyEstimateTS ( id,
				"Daily base flow estimate",
				datatype, "CFS", estimate.getCrunidy(),
				estimate.getBaseflowMonthTS(),
				estimate.getBaseflowDayTS () );
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_DIVERSION_TS_MONTHLY )) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_DEMAND_TS_OVERRIDE_MONTHLY ) ) ) {
		// Always a diversion...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			div = (StateMod_Diversion)data.elementAt(pos);
		}
		// Diversion data might be in memory...
		if (	datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_DIVERSION_TS_MONTHLY ) ) &&
			interval.equalsIgnoreCase("Month") ) {
			ts = div.getDiversionMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_DIVERSION_TS_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries (
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
						COMP_DIVERSION_TS_MONTHLY ));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
		else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_DIVERSION_TS_DAILY ) ) &&
			interval.equalsIgnoreCase("Day") ) {
			ts = div.getDiversionDayTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_DIVERSION_TS_DAILY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
						COMP_DIVERSION_TS_DAILY ));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
		else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_DEMAND_TS_OVERRIDE_MONTHLY ) ) ) {
			ts = div.getDemandOverrideMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
				COMP_DEMAND_TS_OVERRIDE_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
					COMP_DEMAND_TS_OVERRIDE_MONTHLY));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
	}
	else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_DEMAND_TS_MONTHLY )) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_DEMAND_TS_DAILY )) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_INSTREAM_DEMAND_TS_MONTHLY )) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_INSTREAM_DEMAND_TS_DAILY )) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_WELL_DEMAND_TS_MONTHLY )) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_WELL_DEMAND_TS_DAILY )) ) {
		// May be a diversion, instream flow, or well...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			div = (StateMod_Diversion)data.elementAt(pos);
			// Diversion data might be in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				ts = div.getDemandMonthTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
						COMP_DEMAND_TS_MONTHLY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_DEMAND_TS_MONTHLY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts = div.getDemandDayTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
						COMP_DEMAND_TS_DAILY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_DEMAND_TS_DAILY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
		}
		// If here, did not find a matching diversion so try the
		// instream flow stations...
		comp = getComponentForComponentType( COMP_INSTREAM_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			instream = (StateMod_InstreamFlow)data.elementAt(pos);
			// Instream flow data might be in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				ts = instream.getDemandMonthTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
					COMP_INSTREAM_DEMAND_TS_MONTHLY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_INSTREAM_DEMAND_TS_MONTHLY
						));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts = instream.getDemandDayTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
						COMP_INSTREAM_DEMAND_TS_DAILY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_INSTREAM_DEMAND_TS_DAILY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
		}
		// If here, did not find a matching instream flow so try the
		// well stations...
		comp = getComponentForComponentType( COMP_WELL_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			well = (StateMod_Well)data.elementAt(pos);
			// Well data might be in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				ts = well.getDemandMonthTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
					COMP_WELL_DEMAND_TS_MONTHLY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_WELL_DEMAND_TS_MONTHLY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts = well.getDemandDayTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
						COMP_WELL_DEMAND_TS_DAILY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_WELL_DEMAND_TS_DAILY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
		}
	}
	else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_DEMAND_TS_AVERAGE_MONTHLY ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY ) ) ) {
		// May be a diversion or instream flow...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			div = (StateMod_Diversion)data.elementAt(pos);
			// Diversion data might be in memory...
			ts = div.getDemandAverageMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data){
				// Time series is not in memory so try
				// reading from the data file...
				comp2 = getComponentForComponentType(
					COMP_DEMAND_TS_AVERAGE_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_Util.
						createRepeatingAverageMonthTS (
						StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null,true),
						date1, date2 );
					ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_DEMAND_TS_AVERAGE_MONTHLY
						));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
		// If get to here, a diversion was not matched so try the
		// instream flows...
		comp = getComponentForComponentType( COMP_INSTREAM_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			instream = (StateMod_InstreamFlow)data.elementAt(pos);
			// Instream flow data might be in memory...
			ts = instream.getDemandAverageMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data){
				// Time series is not in memory so try
				// reading from the data file...
				comp2 = getComponentForComponentType(
				COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_Util.
						createRepeatingAverageMonthTS (
						StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null,true),
						date1, date2 );
					ts.setDataType(
						lookupTimeSeriesDataType(
					COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY
						));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
	}
	else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY )) ) {
		// May be a diversion or well...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			div = (StateMod_Diversion)data.elementAt(pos);
			// Diversion data might be in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				ts =div.getConsumptiveWaterRequirementMonthTS();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
					COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts = div.getConsumptiveWaterRequirementDayTS ();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
						COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
		}
		// If here, did not find a matching instream flow so try the
		// well stations...
		comp = getComponentForComponentType( COMP_WELL_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			well = (StateMod_Well)data.elementAt(pos);
			// Well data might be in memory...
			if ( interval.equalsIgnoreCase("Month") ) {
				ts=well.getConsumptiveWaterRequirementMonthTS();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
					COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts = well.getConsumptiveWaterRequirementDayTS();
				if ( (ts == null) && !areTSRead() && read_data){
					// Time series is not in memory so try
					// reading from the data file...
					comp2 = getComponentForComponentType(
						COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY);
					fn = getDataFilePathAbsolute(
						comp2.getDataFileName() );
					try {	ts = StateMod_TS.readTimeSeries(
							tsident_string, fn,
							null, null, null,true);
						ts.setDataType(
						lookupTimeSeriesDataType(
						COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY));
					}
					catch ( Exception e ) {
						ts = null;
					}
				}
				return ts;
			}
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY ) + __ESTIMATED )) {
		// Always diversion or well...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			div = (StateMod_Diversion)data.elementAt(pos);
			return StateMod_Util.createDailyEstimateTS ( id,
				"Daily CWR, estimated",
				datatype,
				lookupTimeSeriesDataUnits(
				StateMod_DataSet.
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY),
				div.getCdividy(),
				div.getConsumptiveWaterRequirementMonthTS(),
				div.getConsumptiveWaterRequirementDayTS () );
		}
		// If here, no diversion was found, so try well...
		comp = getComponentForComponentType ( COMP_WELL_STATIONS );
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			well = (StateMod_Well)data.elementAt(pos);
			return StateMod_Util.createDailyEstimateTS ( id,
				"Daily CWR, estimated",
				datatype,
				lookupTimeSeriesDataUnits(
				StateMod_DataSet.
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY),
				well.getCdividyw(),
				well.getConsumptiveWaterRequirementMonthTS(),
				well.getConsumptiveWaterRequirementDayTS () );
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_DIVERSION_RIGHTS ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_RESERVOIR_RIGHTS ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_INSTREAM_RIGHTS ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
		COMP_WELL_RIGHTS ) ) ) {
		// Water rights.
		// May be a diversion, instream flow, reservoir, or well...
		// Figure out which one and then create a time series...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			div = (StateMod_Diversion)data.elementAt(pos);
			if ( interval.equalsIgnoreCase("Month") ) {
				ts =	StateMod_Util.createWaterRightTS ( div,
					TimeInterval.MONTH,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_DIVERSION_TS_MONTHLY),
					getDataStart(), getDataEnd() );
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts =	StateMod_Util.createWaterRightTS ( div,
					TimeInterval.DAY,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_DIVERSION_TS_DAILY),
					getDataStart(), getDataEnd() );
				return ts;
			}
		}
		// If here, did not find a matching diversion so try the
		// instream flow stations...
		comp = getComponentForComponentType( COMP_INSTREAM_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			instream = (StateMod_InstreamFlow)data.elementAt(pos);
			if ( interval.equalsIgnoreCase("Month") ) {
				ts =	StateMod_Util.createWaterRightTS (
					instream, TimeInterval.MONTH,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_INSTREAM_DEMAND_TS_MONTHLY),
					getDataStart(), getDataEnd() );
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts =	StateMod_Util.createWaterRightTS (
					instream, TimeInterval.DAY,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_INSTREAM_DEMAND_TS_DAILY),
					getDataStart(), getDataEnd() );
				return ts;
			}
		}
		// If here, did not find a matching diversion so try the
		// reservoir stations...
		comp = getComponentForComponentType( COMP_RESERVOIR_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			res = (StateMod_Reservoir)data.elementAt(pos);
			if ( interval.equalsIgnoreCase("Month") ) {
				ts =	StateMod_Util.createWaterRightTS (
					res, TimeInterval.MONTH,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_RESERVOIR_CONTENT_TS_MONTHLY),
					getDataStart(), getDataEnd() );
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts =	StateMod_Util.createWaterRightTS (
					res, TimeInterval.DAY,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_RESERVOIR_CONTENT_TS_MONTHLY),
					getDataStart(), getDataEnd() );
				return ts;
			}
		}
		// If here, did not find a matching instream flow so try the
		// well stations...
		comp = getComponentForComponentType( COMP_WELL_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			well = (StateMod_Well)data.elementAt(pos);
			// Get the units from the diversion time series...
			if ( interval.equalsIgnoreCase("Month") ) {
				ts =	StateMod_Util.createWaterRightTS (
					well, TimeInterval.MONTH,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_DIVERSION_TS_MONTHLY),
					getDataStart(), getDataEnd() );
				return ts;
			}
			else if ( interval.equalsIgnoreCase("Day") ) {
				ts =	StateMod_Util.createWaterRightTS (
					res, TimeInterval.DAY,
					lookupTimeSeriesDataUnits(
					StateMod_DataSet.
					COMP_DIVERSION_TS_DAILY),
					getDataStart(), getDataEnd() );
				return ts;
			}
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_CONTENT_TS_MONTHLY ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_CONTENT_TS_DAILY ) ) ) {
		// Always a reservoir...
		comp = getComponentForComponentType( COMP_RESERVOIR_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			res = (StateMod_Reservoir)data.elementAt(pos);
		}
		// Reservoir data might be in memory...
		if ( interval.equalsIgnoreCase("Month") ) {
			ts = res.getContentMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_RESERVOIR_CONTENT_TS_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries (
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
					COMP_RESERVOIR_CONTENT_TS_MONTHLY ));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
		if ( interval.equalsIgnoreCase("Day") ) {
			ts = res.getContentDayTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_RESERVOIR_CONTENT_TS_DAILY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
					COMP_RESERVOIR_CONTENT_TS_DAILY ));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_TARGET_TS_MONTHLY ) + "Min" ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_TARGET_TS_DAILY ) + "Min" ) ) {
		// Always a reservoir...
		comp = getComponentForComponentType( COMP_RESERVOIR_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			res = (StateMod_Reservoir)data.elementAt(pos);
		}
		// Reservoir data might be in memory...
		if ( interval.equalsIgnoreCase("Month") ) {
			ts = res.getMinTargetMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file.  Target minimum should
				// be found before the maximum...
				comp2 = getComponentForComponentType(
					COMP_RESERVOIR_TARGET_TS_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries (
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
					COMP_RESERVOIR_TARGET_TS_MONTHLY) +
					"Min" );
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
		if ( interval.equalsIgnoreCase("Day") ) {
			ts = res.getMinTargetDayTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_RESERVOIR_TARGET_TS_DAILY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
					COMP_RESERVOIR_TARGET_TS_DAILY)+"Min");
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_TARGET_TS_MONTHLY ) + "Max") ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_TARGET_TS_DAILY ) + "Max") ) {
		// Always a reservoir...
		comp = getComponentForComponentType( COMP_RESERVOIR_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			res = (StateMod_Reservoir)data.elementAt(pos);
		}
		// Reservoir data might be in memory...
		if ( interval.equalsIgnoreCase("Month") ) {
			ts = res.getMaxTargetMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// REVISIT - need to read 2nd time series in
				// pair - code does not handle because it will
				// find the first time series.
			}
			return ts;
		}
		if ( interval.equalsIgnoreCase("Day") ) {
			ts = res.getMaxTargetDayTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// REVISIT - need to read 2nd time series in
				// pair - code does not handle because it will
				// find the first time series.
			}
			return ts;
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_RESERVOIR_CONTENT_TS_DAILY ) + __ESTIMATED )) {
		comp = getComponentForComponentType( COMP_RESERVOIR_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			res = (StateMod_Reservoir)data.elementAt(pos);
			// Try to get the estimated time series...
			return StateMod_Util.createDailyEstimateTS ( id,
				"End of day content, estimated",
				datatype, "ACFT", res.getCresdy(),
				res.getContentMonthTS(),
				res.getContentDayTS () );
		}
	}
	else if(datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_WELL_PUMPING_TS_MONTHLY ) ) ||
		datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_WELL_PUMPING_TS_DAILY ) ) ) {
		// Always a well...
		comp = getComponentForComponentType( COMP_WELL_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			well = (StateMod_Well)data.elementAt(pos);
		}
		// Well data might be in memory...
		if ( interval.equalsIgnoreCase("Month") ) {
			ts = well.getPumpingMonthTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_WELL_PUMPING_TS_MONTHLY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries (
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
						COMP_WELL_PUMPING_TS_MONTHLY ));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
		if ( interval.equalsIgnoreCase("Day") ) {
			ts = well.getPumpingDayTS ();
			if ( (ts == null) && !areTSRead() && read_data ) {
				// Time series is not in memory so try reading
				// from the data file...
				comp2 = getComponentForComponentType(
					COMP_WELL_PUMPING_TS_DAILY);
				fn = getDataFilePathAbsolute(
					comp2.getDataFileName() );
				try {	ts = StateMod_TS.readTimeSeries(
						tsident_string, fn,
						null, null, null, true );
					ts.setDataType(lookupTimeSeriesDataType(
						COMP_WELL_PUMPING_TS_DAILY ));
				}
				catch ( Exception e ) {
					ts = null;
				}
			}
			return ts;
		}
	}
	else if ( datatype.equalsIgnoreCase( lookupTimeSeriesDataType(
			COMP_WELL_PUMPING_TS_DAILY ) + __ESTIMATED )) {
		comp = getComponentForComponentType( COMP_WELL_STATIONS);
		data = (Vector)comp.getData();
		pos = StateMod_Util.indexOf ( data, id );
		if ( pos >= 0 ) {
			well = (StateMod_Well)data.elementAt(pos);
			// Try to get the estimated time series...
			return StateMod_Util.createDailyEstimateTS ( id,
				"Well pumping, historical, estimated",
				datatype,
				lookupTimeSeriesDataUnits( StateMod_DataSet.
				COMP_WELL_PUMPING_TS_DAILY),
				well.getCdividyw(),
				well.getPumpingMonthTS(),
				well.getPumpingDayTS () );
		}
	}

	// Default is to return null...
	return null;
}

/**
Return a Vector of String containing information about unused data in the data
set.  For example, these may be evaporation time series or delay tables that
are not used.
*/
public Vector getUnusedDataSummary ()
{	Vector v = new Vector();

	v.addElement (
	"Summary of data objects that are not used in the data set");
	v.addElement ( "" );

	DataSetComponent comp1, comp2;
	Vector data1, data2;
	int size1, size2, size3;
	TS ts;

	// Stream gage...

	v.addElement ( "" );
	v.addElement ( "Stream Gage Data are not checked." );
	v.addElement ( "" );

	// Delay Table (Monthly)...

	v.addElement ( "" );
	v.addElement ( "Delay Tables (Monthly) are not checked." );
	v.addElement ( "" );

	// Delay Table (Daily)...

	v.addElement ( "" );
	v.addElement ( "Delay Tables (Daily) are not checked." );
	v.addElement ( "" );

	// Diversions...

	v.addElement ( "" );
	v.addElement ( "Diversion data are not checked." );
	v.addElement ( "" );

	// Precipitation time series (monthly)...

	comp1 = getComponentForComponentType ( COMP_PRECIPITATION_TS_MONTHLY );
	if ( comp1.hasData() ) {
	v.addElement ( comp1.getComponentName() );
	v.addElement ( "" );
	v.addElement (
	"Precip TS ID   Precip TS Name" );
	v.addElement ( "-----------------------------------------");
	data1 = (Vector)comp1.getData();
	size1 = data1.size();
	String id;
	boolean found = false;
	for ( int i = 0; i < size1; i++ ) {
		found = false;
		ts = (TS)data1.elementAt(i);
		id = ts.getLocation();
		StateMod_Reservoir res;
		comp2 = getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		Vector climates;
		StateMod_ReservoirClimate climate;
		for ( int j = 0; j < size2; j++ ) {
			res = (StateMod_Reservoir)data2.elementAt(j);
			climates = res.getClimates();
			size3 = climates.size();
			for ( int k = 0; k < size3; k++ ) {
				climate = (StateMod_ReservoirClimate)
					climates.elementAt(k);
				if (	(climate.getType() ==
					StateMod_ReservoirClimate.CLIMATE_PTPX)
					&&climate.getID().equalsIgnoreCase(id)){
					// Found a reservoir that uses the
					// station...
					found = true;
					break;
				}
			}
		}
		if ( !found ) {
			// The precipitation station is not used in the data
			// set so print...
			v.addElement (
				StringUtil.formatString(id,"%-12.12s") + "  " + 
				StringUtil.formatString( ts.getDescription(),
				"%-24.24s") );
		}
	}
	} // End if comp1.hasData()

	// Evaporation time series (monthly)...

	comp1 = getComponentForComponentType ( COMP_EVAPORATION_TS_MONTHLY );
	if ( comp1.hasData() ) {
	v.addElement ( "" );
	v.addElement ( comp1.getComponentName() );
	v.addElement ( "" );
	v.addElement (
	"Evap TS ID    Evap TS Name" );
	v.addElement ( "-----------------------------------------");
	data1 = (Vector)comp1.getData();
	size1 = data1.size();
	String id;
	boolean found = false;
	for ( int i = 0; i < size1; i++ ) {
		found = false;
		ts = (TS)data1.elementAt(i);
		id = ts.getLocation();
		StateMod_Reservoir res;
		comp2 = getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		data2 = (Vector)comp2.getData();
		size2 = data2.size();
		Vector climates;
		StateMod_ReservoirClimate climate;
		for ( int j = 0; j < size2; j++ ) {
			res = (StateMod_Reservoir)data2.elementAt(j);
			climates = res.getClimates();
			size3 = climates.size();
			for ( int k = 0; k < size3; k++ ) {
				climate = (StateMod_ReservoirClimate)
					climates.elementAt(k);
				if (	(climate.getType() ==
					StateMod_ReservoirClimate.CLIMATE_EVAP)
					&&climate.getID().equalsIgnoreCase(id)){
					// Found a reservoir that uses the
					// station...
					found = true;
					break;
				}
			}
		}
		if ( !found ) {
			// The evaporation station is not used in the data
			// set so print...
			v.addElement (
				StringUtil.formatString(id,"%-12.12s") + "  " + 
				StringUtil.formatString( ts.getDescription(),
				"%-24.24s") );
		}
	}
	} // End if comp1.hasData()

	// Reservoirs...

	v.addElement ( "" );
	v.addElement ( "Reservoirs are not checked." );
	v.addElement ( "" );

	// Instream flows...

	v.addElement ( "" );
	v.addElement ( "Instream flows are not checked." );
	v.addElement ( "" );

	// Wells...

	v.addElement ( "" );
	v.addElement ( "Wells are not checked." );
	v.addElement ( "" );

	// Stream Estimate stations...

	v.addElement ( "" );
	v.addElement ( "Stream estimate stations are not checked." );
	v.addElement ( "" );

	// River network...

	v.addElement ( "" );
	v.addElement ( "River network data are not checked." );
	v.addElement ( "" );

	// Operational rights.

	v.addElement ( "" );
	v.addElement ( "Operational rights data are not checked." );
	v.addElement ( "" );

	return v;
}

/**
Indicate whether the data set has daily data (iday not missing and iday not
equal to 0).
Use this method instead of checking iday directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes daily data (iday not missing and
iday != 0).  Return false if daily data are not used.
@param is_active Only return true if daily data are included in the data set and
the data are active (iday = 1).
*/
public boolean hasDailyData ( boolean is_active )
{	if ( is_active ) {
		if ( __iday == 1 ) {
			// Daily data are included in the data set and are
			// used...
			return true;
		}
		else {	// Daily data may or may not be included in the data set
			// but are not used...
			return false;
		}
	}
	else if ( !StateMod_Util.isMissing(__iday) && (__iday != 0) ) {
		// Data are specified in the data set but are not used...
		return true;
	}
	else {	// Daily data are not included...
		return false;
	}
}

/**
Indicate whether the data set has irrigation practice data for
variable efficiency (itsfile not missing and itsfile not equal to 0).
Use this method instead of checking itsfile directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes irrigation practice data (itsfile not
missing and itsfile != 0).  Return false if irrigation practice data are not
used.
@param is_active Only return true if irrigation practice data are included in
the data set and the data are active (itsfile = 1).
*/
public boolean hasIrrigationPracticeData ( boolean is_active )
{	if ( is_active ) {
		if ( __itsfile == 1 ) {
			// Irrigation practice data are included in the data set
			// and are used...
			return true;
		}
		else {	// Irrigtaion practice data may or may not be included
			// in the data set but are not used...
			return false;
		}
	}
	else if ( !StateMod_Util.isMissing(__itsfile) && (__itsfile != 0) ) {
		// Data are specified in the data set but are not used...
		return true;
	}
	else {	// Irrigation practice data are not included...
		return false;
	}
}

/**
Indicate whether the data set has Irrigation Water Requirement (IWR) data for
variable efficiency (ieffmax not missing and ieffmax not equal to 0).
Use this method instead of checking ieffmax directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes IWR data (ieffmax not missing and
ieffmax != 0).  Return false if IWR data are not used.
@param is_active Only return true if IWR data are included in the data set and
the data are active (ieffmax = 1).
*/
public boolean hasIrrigationWaterRequirementData ( boolean is_active )
{	if ( is_active ) {
		if ( __ieffmax == 1 ) {
			// IWR data are included in the data set and are used...
			return true;
		}
		else {	// IWR data may or may not be included in the data set
			// but are not used...
			return false;
		}
	}
	else if ( !StateMod_Util.isMissing(__ieffmax) && (__ieffmax != 0) ) {
		// Data are specified in the data set but are not used...
		return true;
	}
	else {	// IWR data are not included...
		return false;
	}
}

/**
Indicate whether the data set has monthly instream flow data.
Use this method instead of checking ireach directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes monthly instream flow demand data
(ireach == 2 or ireach == 3).  Return false if monthly instream flow demands are
not used.
*/
public boolean hasMonthlyISFData ()
{	
	if ( (__ireach == 2) || (__ireach == 3) ) {
		// Monthly instream flow demand data are included in the data
		// set and are used...
		return true;
	}
	else {	return false;
	}
}

/**
Indicate whether the data set has San Juan Recovery data (isjrip not missing and
isjrip not equal 0).
Use this method instead of checking isjrip directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes San Juan Recovery data (isjrip not missing
and isjrip != 0).  Return false if San Juan Recovery data are not used.
@param is_active Only return true if San Juan Recovery data are included in the
data set and the data are active (isjrip = 1).
*/
public boolean hasSanJuanData ( boolean is_active )
{	if ( is_active ) {
		if ( __isjrip == 1 ) {
			// San Juan Recovery data are included in the data set
			// and are used...
			return true;
		}
		else {	// San Juan Revovery data may or may not be included in
			// the data set but are not used...
			return false;
		}
	}
	else if ( !StateMod_Util.isMissing(__isjrip) && (__isjrip != 0) ) {
		// Data are specified in the data set but are not used...
		return true;
	}
	else {	// San Juan Recovery data are not included...
		return false;
	}
}

/**
Indicate whether the data set has soil moisture data (soild not missing and
soild not equal to 0).
Use this method instead of checking soild directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes soil moisture data (soild not missing
and soild != 0).  Return false if soil moisture data are not used.
@param is_active Only return true if soil moisture data are included in the
data set and the data are active (soild = 1).
*/
public boolean hasSoilMoistureData ( boolean is_active )
{	if ( is_active ) {
		if ( __soild > 0.0 ) {
			// Soil moisture data are included in the data set
			// and are used...
			return true;
		}
		else {	// Soil moisture data may or may not be included in
			// the data set but are not used...
			return false;
		}
	}
	else if ( !StateMod_Util.isMissing(__soild) && (__soild != 0.0) ) {
		// Data are specified in the data set but are not used...
		return true;
	}
	else {	// Soil moisture data are not included...
		return false;
	}
}

/**
Indicate whether the data set has well data (iwell not missing and iwell 
not equal to 0).
Use this method instead of checking iwell directly to simplify logic and allow
for future changes to the model input.
@return true if the data set includes daily data (iwell not missing and
iwell != 0).  Return false if well data are not used.
@param is_active Only return true if well data are included in the data set and
the data are active (iwell = 1).
*/
public boolean hasWellData ( boolean is_active )
{	if ( is_active ) {
		if ( __iwell == 1 ) {
			// Well data are included in the data set and are
			// used...
			return true;
		}
		else {	// Well data may or may not be included in the data set
			// but are not used...
			return false;
		}
	}
	else if ( !StateMod_Util.isMissing(__iwell) && (__iwell != 0) ) {
		// Data are specified in the data set but are not used...
		return true;
	}
	else {	// Well data are not included...
		return false;
	}
}

/**
Initialize a data set by defining all the components for the data set.  This
ensures that software will be able to evaluate all components.
*/
private void initialize()
{	String routine = "StateMod_DataSet.initialize";
	// Always add all the components to the data set because StateMod does
	// not really differentiate between data set types.  Instead, control
	// file information controls.  Components are added to their groups.
	// Also initialize the data for each sub-component to empty Vectors so
	// that GUI based code does not need to check for nulls.  This is
	// consistent with StateMod GUI initializing data vectors to empty at
	// startup.
	//
	// REVISIT - need to turn on data set components (set visible, etc.) as
	// the control file is changed.  This allows new components to be
	// enabled in the right order.
	//
	// REVISIT - should be allowed to have null data Vector but apparently
	// StateMod GUI cannot handle yet - need to allow null later and use
	// hasData() or similar to check.

	DataSetComponent comp, subcomp;
	try {	comp = new DataSetComponent ( this, COMP_CONTROL_GROUP );
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
		subcomp = new DataSetComponent(this, COMP_RESPONSE);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_CONTROL);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_OUTPUT_REQUEST);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_STREAMGAGE_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
		subcomp = new DataSetComponent(this, COMP_STREAMGAGE_STATIONS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,
			COMP_STREAMGAGE_HISTORICAL_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_STREAMGAGE_BASEFLOW_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp =new DataSetComponent(this,COMP_DELAY_TABLE_MONTHLY_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_DELAY_TABLES_MONTHLY);
		subcomp.setData ( new Vector() );

		comp = new DataSetComponent(this, COMP_DELAY_TABLE_DAILY_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_DELAY_TABLES_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_DIVERSION_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_DIVERSION_STATIONS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_DIVERSION_RIGHTS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,COMP_DIVERSION_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_DIVERSION_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_DEMAND_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_DEMAND_TS_OVERRIDE_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,
			COMP_DEMAND_TS_AVERAGE_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_DEMAND_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_IRRIGATION_PRACTICE_TS_YEARLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_SOIL_MOISTURE);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_PRECIPITATION_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this,
			COMP_PRECIPITATION_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_EVAPORATION_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp =new DataSetComponent(this,COMP_EVAPORATION_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_RESERVOIR_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_RESERVOIR_STATIONS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,COMP_RESERVOIR_RIGHTS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,
			COMP_RESERVOIR_CONTENT_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_RESERVOIR_CONTENT_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_RESERVOIR_TARGET_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, 		
			COMP_RESERVOIR_TARGET_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_INSTREAM_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_INSTREAM_STATIONS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_INSTREAM_RIGHTS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,
			COMP_INSTREAM_DEMAND_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,
			COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,
			COMP_INSTREAM_DEMAND_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_WELL_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_WELL_STATIONS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_WELL_RIGHTS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp=new DataSetComponent(this,COMP_WELL_PUMPING_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this,COMP_WELL_PUMPING_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp =new DataSetComponent(this,COMP_WELL_DEMAND_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_WELL_DEMAND_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_PLAN_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_PLANS);
		subcomp.setData ( new Vector() );
		comp.addComponent ( subcomp );

		comp = new DataSetComponent(this, COMP_STREAMESTIMATE_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent ( comp );
		subcomp = new DataSetComponent(
			this,COMP_STREAMESTIMATE_STATIONS );
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(
			this,COMP_STREAMESTIMATE_COEFFICIENTS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(
			this, COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(
			this, COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_RIVER_NETWORK_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_RIVER_NETWORK);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
		subcomp = new DataSetComponent(this, COMP_NETWORK);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this,COMP_OPERATION_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this,COMP_OPERATION_RIGHTS);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_SANJUAN_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_SANJUAN_RIP);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );

		comp = new DataSetComponent(this, COMP_GEOVIEW_GROUP);
		comp.setListSource (
			DataSetComponent.LIST_SOURCE_PRIMARY_COMPONENT );
		addComponent( comp );
		subcomp = new DataSetComponent(this, COMP_GEOVIEW);
		subcomp.setData ( new Vector() );
		comp.addComponent( subcomp );
	}
	catch (Exception e) {
		// Should not happen...
		Message.printWarning(2, routine, e);
	}
}

/**
Initialize the data file names to basename.ext, where the basename should be
set previously and "ext" is the default extension for the file.
All file names are initialized, even if they are not used.
*/
public void initializeDataFileNames ()
{	Vector data_Vector = getComponents();
	int size = 0;
	if (data_Vector != null) {
		size = data_Vector.size();
	}
	DataSetComponent comp;
	String basename = getBaseName();
	for ( int i = 0; i < size; i++ ) {
		comp = (DataSetComponent)data_Vector.elementAt(i);
		if ( !comp.isGroup() ) {
			// Set the name...
			comp.setDataFileName ( basename + "." +
				__component_file_extensions[
				comp.getComponentType()] );
			continue;
		}
		// Need to add components to the group...
		Vector data2 = (Vector)comp.getData();
		int size2 = 0;
		if ( data2 != null ) {
			size2 = data2.size();
		}
		for ( int j = 0; j < size2; j++ ) {
			comp = (DataSetComponent)data2.elementAt(j);
			comp.setDataFileName ( basename + "." +
				__component_file_extensions[
				comp.getComponentType()] );
		}
	}
}


/**
Indicate whether the component contains time series that are impacted by the
decision of whether to read time series.
@return true if the component is controlled by the initial specification of
whether to read time series.
*/ 
public boolean isDynamicTSComponent ( int comp_type )
{	// The following time series components are only read when __areTSRead
	// is true...
	if (	(comp_type == COMP_DIVERSION_TS_MONTHLY) ||
		(comp_type == COMP_DIVERSION_TS_DAILY) ||
		(comp_type == COMP_DEMAND_TS_MONTHLY) ||
		(comp_type == COMP_DEMAND_TS_OVERRIDE_MONTHLY) ||
		(comp_type == COMP_DEMAND_TS_AVERAGE_MONTHLY) ||
		(comp_type == COMP_DEMAND_TS_DAILY) ||
		(comp_type == COMP_IRRIGATION_PRACTICE_TS_YEARLY) ||
		(comp_type == COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY) ||
		(comp_type == COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY) ||
		(comp_type == COMP_RESERVOIR_CONTENT_TS_MONTHLY) ||
		(comp_type == COMP_RESERVOIR_CONTENT_TS_DAILY) ||
		(comp_type == COMP_RESERVOIR_TARGET_TS_MONTHLY) ||
		(comp_type == COMP_RESERVOIR_TARGET_TS_DAILY) ||
		(comp_type == COMP_INSTREAM_DEMAND_TS_MONTHLY) ||
		(comp_type == COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY) ||
		(comp_type == COMP_INSTREAM_DEMAND_TS_DAILY) ||
		(comp_type == COMP_WELL_PUMPING_TS_MONTHLY) ||
		(comp_type == COMP_WELL_PUMPING_TS_DAILY) ||
		(comp_type == COMP_WELL_DEMAND_TS_MONTHLY) ||
		(comp_type == COMP_WELL_DEMAND_TS_DAILY) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Indicate whether the original file that was read was free-format (new) or
fixed-format (old).
@return true if free format, false if fixed format.
*/ 
public boolean isFreeFormat ()
{	return __is_free_format;
}

/**
Returns the name of the specified component.  Subcomponents (e.g., diversion
delay tables) are specifically checked and then the base class method is called.
@param comp_type the component type integer.
@return the name of the specified component.
*/
public String lookupComponentName ( int comp_type )
{	if ( comp_type == COMP_DIVERSION_STATION_DELAY_TABLES ) {
		return __COMPNAME_DIVERSION_STATION_DELAY_TABLES;
	}
	else if ( comp_type == COMP_DIVERSION_STATION_COLLECTIONS ) {
		return __COMPNAME_DIVERSION_STATION_COLLECTIONS;
	}
	else if ( comp_type == COMP_RESERVOIR_STATION_ACCOUNTS ) {
		return __COMPNAME_RESERVOIR_STATION_ACCOUNTS;
	}
	else if ( comp_type == COMP_RESERVOIR_STATION_PRECIP_STATIONS ) {
		return __COMPNAME_RESERVOIR_STATION_PRECIP_STATIONS;
	}
	else if ( comp_type == COMP_RESERVOIR_STATION_EVAP_STATIONS ) {
		return __COMPNAME_RESERVOIR_STATION_EVAP_STATIONS;
	}
	else if ( comp_type == COMP_RESERVOIR_STATION_CURVE ) {
		return __COMPNAME_RESERVOIR_STATION_CURVE;
	}
	else if ( comp_type == COMP_RESERVOIR_STATION_COLLECTIONS ) {
		return __COMPNAME_RESERVOIR_STATION_COLLECTIONS;
	}
	else if ( comp_type == COMP_WELL_STATION_DELAY_TABLES ) {
		return __COMPNAME_WELL_STATION_DELAY_TABLES;
	}
	else if ( comp_type == COMP_WELL_STATION_DEPLETION_TABLES ) {
		return __COMPNAME_WELL_STATION_DEPLETION_TABLES;
	}
	else if ( comp_type == COMP_WELL_STATION_COLLECTIONS ) {
		return __COMPNAME_WELL_STATION_COLLECTIONS;
	}
	else {	return super.lookupComponentName(comp_type);
	}
}

/**
Determine the component type for a string time series data type and interval.
@param data_type Time series data type
@param interval Data interval as TimeInterval.MONTH or TimeInterval.DAY.
@return the component type for a time series, or COMP_UNKNOWN if not found.
*/
public static int lookupTimeSeriesDataComponentType ( String data_type,
							int interval )
{	int length = __component_ts_data_types.length;
	for ( int i = 0; i < length; i++ ) {
		if (	__component_ts_data_types[i].equalsIgnoreCase(data_type)
			&& (__component_ts_data_intervals[i] == interval ) ) {
			return i;
		}
	}
	return COMP_UNKNOWN;
}

/**
Determine the time series data type string for a component type.
@param comp_type Component type. 
@return the time series data type string or an empty string if not found.
The only problem is with COMP_RESERVOIR_TARGET_TS_MONTHLY and
COMP_RESERVOIR_TARGET_TS_DAILY, each of which contain both the maximum and
minimum time series.  For these components, add "Max" and "Min" to the returned
values.
*/
public static String lookupTimeSeriesDataType ( int comp_type)
{	return __component_ts_data_types[comp_type];
}

/**
Determine the time series data units string for a component type.  There is
currently no plan to have different units - StateMod units are constant.
@param comp_type Component type. 
@return the time series data units string or an empty string if not found.
*/
public static String lookupTimeSeriesDataUnits ( int comp_type)
{	return __component_ts_data_units[comp_type];
}

/**
Determine the time series file extension string for a string time series data
type and interval.
@param data_type Time series data type
@param interval Data interval as TimeInterval.MONTH or TimeInterval.DAY.
@return the time series data units string or an empty string if not found.
*/
public static String lookupTimeSeriesDataFileExtension ( String data_type,
							int interval )
{	int length = __component_ts_data_types.length;
	for ( int i = 0; i < length; i++ ) {
		if (	__component_ts_data_types[i].equalsIgnoreCase(data_type)
			&& (__component_ts_data_intervals[i] == interval ) ) {
			return __component_file_extensions[i];
		}
	}
	return "";
}

/**
Determine the data set type from a string.
@param type Data set type as a string
@return the data set type as an int or -1 if not found.
*/
public static int lookupType(String type) {
	if (type.equalsIgnoreCase(NAME_UNKNOWN)) {
		return TYPE_UNKNOWN;
	}
	return TYPE_UNKNOWN;
}

/**
Return the data set type name.  This is suitable for warning messages and 
simple output.
@param dataset_type data set type(see TYPE_*)
@return the data set type name.
*/
public static String lookupTypeName(int dataset_type) {
	if (dataset_type == TYPE_UNKNOWN) {
		return NAME_UNKNOWN;
	}
	return NAME_UNKNOWN;
}

public boolean DUMP_DIRTY = false;

/**
Set a component dirty (edited).  This method is usually called by the set
methods in the individual StateMod_Data classes.  This marks the component as
dirty independent of the state of the individual data objects in the component.
If a component is dirty, it needs to be written to a file because data or the
file name have changed.
@param component_type The component type within the data set(see COMP*).
@param is_dirty true if the component should be marked as dirty (edited), false
if the component should be marked clean (from data read, or edits saved).
*/
public void setDirty(int component_type, boolean is_dirty)
{	DataSetComponent comp = getComponentForComponentType ( component_type );
	// REVISIT SAM 2006-08-22
	// What is this?  Left over from Tom???
/*
	if (DUMP_DIRTY && component_type == COMP_OPERATION_RIGHTS) {
		RTi.Util.GUI.JWorksheet.except(2, 4);
	}
*/
	if ( comp != null ) {
		comp.setDirty(is_dirty);
	}
}

/**
Indicate whether the data set should be considered free-format.  If the last
read/write of the response file was free-format, then this setting should be
true, false otherwise.
@param is_free_format Indicates whether the data set should be considered as
free format for read/write.
*/
public void setFreeFormat ( boolean is_free_format )
{	__is_free_format = is_free_format;
}

/**
Read a StateMod control file and stores its information in this StateMod_DataSet
object.
@param filename the file in which the control file is stored
@throws Exception if an error occurs
*/
public void readStateModControlFile ( String filename )
throws Exception
{	String routine = "StateMod_DataSet.readStateModControlFile";
	int line_num = 0;
	String iline = null;
	BufferedReader in = null;
	String nextToken = null;
	StringTokenizer split = null;

	if (Message.isDebugOn) {
		Message.printDebug(10, routine, 
		"Reading control file \"" + filename + "\"" );
	}

	try {	in = new BufferedReader(new FileReader(filename));
		while ((iline = in.readLine())!= null) {
			if (iline.startsWith("#") || 
				iline.trim().length() ==0) {
				continue;
			}
			line_num++;
			split = new StringTokenizer(iline);
			if ((split == null) || (split.countTokens()== 0)) {
				continue;
			}
			nextToken = split.nextToken();
			if (nextToken.equals(":")) {
				continue;
			}
			switch(line_num) {	// Non-comment lines
				case 1: setHeading1(iline); 	break;
				case 2: setHeading2(iline); 	break;
				case 3: setIystr(nextToken); 	break;
				case 4: setIyend(nextToken); 	break;
				case 5: setIresop(nextToken);	break;
				case 6: setMoneva(nextToken);	break;
				case 7: setIopflo(nextToken);	break;
				case 8: setNumpre(nextToken);	break;
				case 9: setNumeva(nextToken);	break;
				case 10: setInterv(nextToken); 	break;
				case 11: setFactor(nextToken); 	break;
				case 12: setRfacto(nextToken); 	break;
				case 13: setDfacto(nextToken); 	break;
				case 14: setFfacto(nextToken); 	break;
				case 15: setCfacto(nextToken); 	break;
				case 16: setEfacto(nextToken); 	break;
				case 17: setPfacto(nextToken); 	break;
				case 18: setCyrl(nextToken); 	break;
				case 19: setIcondem(nextToken);	break;
				case 20: setIchk(nextToken); 	break;
				case 21: setIreopx(nextToken); 	break;
				case 22: setIreach(nextToken); 	break;
				case 23: setIcall(nextToken); 	break;
				case 24: setCcall(nextToken); 	break;
				case 25: setIday(nextToken); 	break;
				case 26: setIwell(nextToken);	break;
				case 27: setGwmaxrc(nextToken); break;
				case 28: setIsjrip(nextToken); 	break;
				case 29: setItsfile(nextToken); break;
				case 30: setIeffmax(nextToken); break;
				case 31: setIsprink(nextToken); break;
				case 32: setSoild(nextToken); 	break;
				case 33: setIsig(nextToken); 	break;
			}
		}
	}
	catch (Exception e) {
		routine = null;
		iline = null;
		nextToken = null;
		split = null;
		if (in != null) {
			in.close();
			in = null;
		}
		Message.printWarning(2, routine, e);
		throw e;
	}

	routine = null;
	iline = null;
	nextToken = null;
	split = null;
	in.close();
	in = null;
}

// REVISIT - StateCU has this return a DataSet object, not populate an existing
// one
/**
Read the StateMod response file and fill the current StateMod_DataSet object.
The file and settings that are read are those set when the object was created.
@param filename Name of the StateMod response file.  This must be the
full path (e.g., from a JFileChooser, with a drive).  The working directory will
be set to the directory of the response file.
@param tsAreRead Indicates that the time series files should be read.
@param useGUI If true, then interactive prompts will be used where necessary.
@param parent The parent JFrame used to position warning dialogs if useGUI
is true.
*/
public void readStateModFile (	String filename, boolean tsAreRead,
				boolean useGUI, JFrame parent )
throws Exception
{	String routine = "StateMod_DataSet.readStateModFile";
	__tsAreRead = tsAreRead;

	File f = new File(filename);
	setDataSetDirectory(f.getParent());
	setDataSetFileName(f.getName());

	// Check whether the response file is free format.  If it is free
	// format then the file is read into a PropList below...

	checkForFreeFormat ( filename );

	// Set the working directory to that of the response file...

	IOUtil.setProgramWorkingDir ( f.getParent() );

	// The following sets the static reference to the current data set
	// which is then accessible by every data object which extends
	// StateMod_Data.  This is done in order that setting components
	// dirty or not can be handled at a low level when values change.
	StateMod_Data._dataset = this;

	// Set basic information about the response file component - only save
	// the file name - the data itself are stored in this data set object.

	getComponentForComponentType( COMP_RESPONSE).setDataFileName(
			f.getName() );

	// Open the response file so that it can be read...

	BufferedReader in = null;

	if ( !__is_free_format ) {
		in = new BufferedReader ( new FileReader(filename) );
	}

	String fn = "";

	int i = 0;
	int size = 0;	// For general use

	DataSetComponent comp = null;

	// Now start reading new scenario...
	StopWatch totalReadTime = new StopWatch();
	StopWatch readTime = new StopWatch();

	Message.printStatus(1, routine, 
		"Reading all information from input directory: \"" 
		+ getDataSetDirectory());

	Message.printStatus(1, routine, "Reading response file \"" +
		filename + "\"" );
	
	totalReadTime.start();

	PropList response_props = null;
	if ( __is_free_format ) {
		// Read the response file into a PropList...
		response_props = new PropList ( "Response" );
		response_props.setPersistentName ( filename );
		response_props.readPersistent();
	}

	try {	// Try for all reads.

	// Read the lines of the response file.  Of major importance is reading
	// the control file, which indicates data set properties that allow
	// figuring out which files are being read.

	boolean continue_reading = true;	// Used to indicate whether
						// files should continue to
						// be read.  It will normally
						// only be set to false when
						// reading a fixed-format
						// response file and the
						// end-of-file is prematurely
						// detected.

	// Read the files in the order of the StateMod documentation for the
	// response file, checking the control settings where a decision is
	// needed.

	// Note that for the fixed-format response file, the lines MUST be read
	// from the file, whether the file is actually read or not (the time
	// series may not be read due to the user's preference).

	// Control file (.ctl)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Control" );
		}
		else {	if ( (fn = readFileLine ( in )) == null ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_CONTROL );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if ( continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			readStateModControlFile(fn);
			comp.setDirty ( false );
			// Control does not have its own data file now so use
			// the data set.
			comp.setData ( this );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
		
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading control file:\n"
			+"\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// River network file (.rin)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "River_Network" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType ( COMP_RIVER_NETWORK );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_RiverNetworkNode.
				readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading river "
			+ "network file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Reservoir stations file (.res)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Reservoir_Station" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_RESERVOIR_STATIONS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_Reservoir.readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading reservoir station file:\n\"" + fn +"\"");
		Message.printWarning(2, routine, e);
	}

	// Diversion stations file (.dds)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Diversion_Station" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_DIVERSION_STATIONS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_Diversion.readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading diversion station file:\n\"" + fn +"\"");
		Message.printWarning(2, routine, e);
	}

	// Stream gage stations file (.ris)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "StreamGage_Station" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_STREAMGAGE_STATIONS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn) ) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData(StateMod_StreamGage.readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading stream gage stations file:\n\""+fn+"\"");
		Message.printWarning(2, routine, e);
	}

	// If not a free-format data set, re-read the legacy stream station
	// file because some stations will be stream estimate stations.  If free
	// format, get the file name...

	try {	if ( __is_free_format ) {
			fn = response_props.getValue ("StreamEstimate_Station");
		}
		else {	// Get from the stream gage component...
			comp = getComponentForComponentType (
				COMP_STREAMGAGE_STATIONS );
			if ( comp == null ) {
				fn = null;
			}
			else {	fn = comp.getDataFileName();
			}
		}
		// Always set the file name...
		comp=getComponentForComponentType(COMP_STREAMESTIMATE_STATIONS);
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn) ) ) {
			readTime.clear();
			readTime.start();
			// Use the relative path...
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData(
				StateMod_StreamEstimate.readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading stream estimate stations file:\n\"" +
			fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Instream flow stations file (.ifs)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Instreamflow_Station" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_INSTREAM_STATIONS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData (
				StateMod_InstreamFlow.readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading instream flow station file:\n\""+fn+"\"");
		Message.printWarning(2, routine, e);
	}

	// Well stations...

	if ( __is_free_format || (!__is_free_format && hasWellData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue ( "Well_Station");
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType(COMP_WELL_STATIONS);
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && hasWellData(false) &&
				(fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				comp.setData(
					StateMod_Well.readStateModFile(fn) );
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading well station file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Plans...

	if ( __is_free_format ) {
		try {	fn = response_props.getValue ( "Plan_Data");
			// Always set the file name...
			comp = getComponentForComponentType(COMP_PLANS);
			if ( comp == null ) {
				Message.printWarning ( 2, routine,
				"Unable to look up plans component " +
				COMP_PLANS );
			}
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				comp.setData(
					StateMod_Plan.readStateModFile(fn) );
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading plan file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Instream flow rights file (.ifr)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Instreamflow_Right" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_INSTREAM_RIGHTS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_InstreamFlowRight.
				readStateModFile(fn) );
			comp.setDirty ( false );
			Message.printStatus ( 1, routine,
				"Connecting instream flow rights to stations.");
			StateMod_InstreamFlow.connectAllRights(
				(Vector)getComponentForComponentType (
				COMP_INSTREAM_STATIONS ).getData(),
				(Vector)comp.getData() );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading instream flow rights file:\n\"" + fn+"\"");
		Message.printWarning(2, routine, e);
	}

	// Reservoir rights file (.rer)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Reservoir_Right" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_RESERVOIR_RIGHTS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_ReservoirRight.
				readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
			Message.printStatus ( 1, routine,
			"Connecting reservoir rights with reservoir stations.");
			StateMod_Reservoir.connectAllRights(
				(Vector)getComponentForComponentType(
				COMP_RESERVOIR_STATIONS).getData(),
				(Vector)comp.getData());
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading reservoir rights file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}
	
	// Diversion rights file (.ddr)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Diversion_Right" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_DIVERSION_RIGHTS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_DiversionRight.
				readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
			Message.printStatus ( 1, routine,
			"Connecting diversion rights to diversion stations" );
			StateMod_Diversion.connectAllRights(
				(Vector)getComponentForComponentType (
				COMP_DIVERSION_STATIONS).getData(),
				(Vector)comp.getData());
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading diversion rights file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Operational rights file (.opr)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Operational_Right" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_OPERATION_RIGHTS );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData(
				StateMod_OperationalRight.readStateModFile(fn));
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading operational rights file:\n\"" + fn+"\"");
		Message.printWarning(2, routine, e);
	}

	// Well rights file (.wer)...

	if ( __is_free_format || (!__is_free_format && hasWellData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue ( "Well_Right" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType(
				COMP_WELL_RIGHTS );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read data...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				comp.setData( StateMod_WellRight.
					readStateModFile(fn) );
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
				Message.printStatus ( 1, routine, "Connecting "+
					"well rights to well stations.");
				StateMod_Well.connectAllRights(
					(Vector)getComponentForComponentType(
					COMP_WELL_STATIONS).getData(),
					(Vector)comp.getData() );
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading well "
				+ "rights file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Precipitation TS monthly file (.pre) - always read...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Precipitation_Monthly");
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name in the component...
		comp = getComponentForComponentType(
				COMP_PRECIPITATION_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the file...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn, null,
				null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			// REVISIT
			// Old-style data that may be removed in new StateMod...
			setNumpre ( size );
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_PRECIPITATION_TS_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading precipitation time series file:\n\"" + fn +"\"");
		Message.printWarning(2, routine, e);
	}

	// Evaporation time series file (.eva) - always read...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "Evaporation_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp =getComponentForComponentType(COMP_EVAPORATION_TS_MONTHLY);
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn, null,
				null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			// REVISIT
			// Old-style data that may be removed in new StateMod...
			setNumeva ( size );
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_EVAPORATION_TS_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading evaporation time series file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Stream gage base flow time series (.rim or .xbm) - always read...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Stream_Base_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType(
			COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn, null,
				null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );

			// The StreamGage and StreamEstimate groups
			// share the same baseflow time series files...

			DataSetComponent comp2 =
			getComponentForComponentType(
			COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY );
			comp2.setDataFileName ( comp.getDataFileName());
			comp2.setData(v);
			comp2.setDirty ( false );

			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading baseflow time series (monthly) file:\n\""+
		fn+"\"");
		Message.printWarning(2, routine, e);
	}

	// Direct flow demand time series (monthly) file (.ddm)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn =response_props.getValue("Diversion_Demand_Monthly");
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name in the component...
		comp = getComponentForComponentType(
			COMP_DEMAND_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the data...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			readInputAnnounce1(comp);
			fn = getDataFilePathAbsolute ( fn );
			Vector v = StateMod_TS.readTimeSeriesList(fn, 
				null, null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_DEMAND_TS_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );

		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading demand"+
			" time series (monthly) file:\n\""+fn+"\"");
		Message.printWarning(2, routine, e);
	}
	
	// Direct flow demand time series override (monthly) file (.ddo)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Diversion_DemandOverride_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Set the file name in the component...
		comp = getComponentForComponentType(
			COMP_DEMAND_TS_OVERRIDE_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the file...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			readInputAnnounce1(comp);
			fn = getDataFilePathAbsolute ( fn );
			Vector v = StateMod_TS.readTimeSeriesList(fn, 
				null, null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_DEMAND_TS_OVERRIDE_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );

		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading demand"+
			" time series override (monthly) file:\n\""+fn+"\"");
		Message.printWarning(2, routine, e);
	}
	
	// Direct flow demand time series average (monthly) file (.dda)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Diversion_Demand_AverageMonthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Set the file name in the component...
		comp = getComponentForComponentType(
			COMP_DEMAND_TS_AVERAGE_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the data file...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			readInputAnnounce1(comp);
			fn = getDataFilePathAbsolute ( fn );
			Vector v = StateMod_TS.readTimeSeriesList(fn, 
				null, null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_DEMAND_TS_AVERAGE_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );

		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading demand"+
			" time series (average monthly) file:\n\""+fn+"\"");
		Message.printWarning(2, routine, e);
	}
	
	// Monthly instream flow demand...

	if ( __is_free_format || (!__is_free_format && hasMonthlyISFData()) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Instreamflow_Demand_Monthly" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_INSTREAM_DEMAND_TS_MONTHLY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data file...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				readInputAnnounce1(comp);
				fn = getDataFilePathAbsolute ( fn );
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i<size; i++) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType(
					COMP_INSTREAM_DEMAND_TS_MONTHLY ) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, 
			"Error reading monthly instream flow demand time "
			+ "series file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Instream demand time series (average monthly) file (.ifa)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Instreamflow_Demand_AverageMonthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Set the file name in the component...
		comp = getComponentForComponentType(
			COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the data file...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn, null,
				null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i< size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType(
				COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY ) );
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine, 
			"Error reading instream flow demand time "
			+ "series file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Well demand time series (monthly) file (.wem)...

	if ( __is_free_format || (!__is_free_format && hasWellData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Well_Demand_Monthly" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_WELL_DEMAND_TS_MONTHLY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();				
				for (i = 0; i < size; i++) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType(
					COMP_WELL_DEMAND_TS_MONTHLY ) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
				"Error reading well demand time "
				+ "series (monthly) file:\n" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Delay file (monthly) file (.dly)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "DelayTable_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_DELAY_TABLES_MONTHLY);
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_DelayTable.readStateModFile(
				fn,true,getInterv()) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading delay table "
			+ " (monthly) file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Reservoir target time series (monthly) file (.tar)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Reservoir_Target_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Set the file name in the component...
		comp = getComponentForComponentType(
			COMP_RESERVOIR_TARGET_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the data file...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn, null,
				null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i < size; i++) {
				if ( (i%2) == 0 ) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType(
					COMP_RESERVOIR_TARGET_TS_MONTHLY) +
					"Min" );
				}
				else {	((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType(
					COMP_RESERVOIR_TARGET_TS_MONTHLY) +
					"Max" );
				}
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading " +
		"reservoir target time series (monthly) file:\n\"" + fn +
		"\"");
		Message.printWarning(2, routine, e);
	}

	// REVISIT - San Juan Sediment Recovery

	if ( __is_free_format || (!__is_free_format && hasSanJuanData(false))) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"SanJuanRecovery" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType( COMP_SANJUAN_RIP );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && (fn != null) &&
				hasSanJuanData(false) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				Message.printWarning ( 1, routine,
				"Do not know how to read the San Juan " +
				"Recover File." );
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading San"+
			" Juan Recovery file:\n\""+fn+"\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Irrigation practice time series (tsp/ipy)...

	if (	__is_free_format ||
		(!__is_free_format && hasIrrigationPracticeData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"IrrigationPractice_Yearly" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_IRRIGATION_PRACTICE_TS_YEARLY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				comp.setData(
				StateCU_IrrigationPracticeTS.readStateCUFile(
				fn, null, null ) );
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading "
				+ "irrigation practice file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Irrigation water requirement (iwr) - monthly...

	if (	__is_free_format ||
		(!__is_free_format&& hasIrrigationWaterRequirementData(false))){
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"ConsumptiveWaterRequirement_Monthly" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();				
				for (i = 0; i < size; i++) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY));
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1,routine,"Error reading "+
			"irrigation water requirement (monthly) time series " +
			"file:\n\"" +
			fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// REVISIT - Soil moisture (StateCU STR?) - StateMod used to read PAR
	// but the AWC is now in the STR file.

	if (	__is_free_format ||
		(!__is_free_format && hasSoilMoistureData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"SoilMoisture" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType(COMP_SOIL_MOISTURE);
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				Message.printWarning ( 2, routine,
				"StateCU Soil moisture File - " +
				"not yet supported.");
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading soil "+
			" moisture file:\n\""+fn+"\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Reservoir content time series (monthly) file (.eom)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Reservoir_Historic_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Set the file name in the component...
		comp = getComponentForComponentType(
			COMP_RESERVOIR_CONTENT_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the data file...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn,
				null, null, null, true);
			if (v == null) {
				v = new Vector();
			}
			// Set the data type because it is not in the StateMod
			// file...
			size = v.size();
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_RESERVOIR_CONTENT_TS_MONTHLY ));
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading reservoir end of month time series " +
		"file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Baseflow coefficients file (.rib)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"StreamEstimate_Coefficients" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType(
			COMP_STREAMESTIMATE_COEFFICIENTS);
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setData( StateMod_StreamEstimate_Coefficients.
				readStateModFile(fn) );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine, "Error reading baseflow "
			+ "coefficient file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Historical streamflow (monthly) file (.rih)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"StreamGage_Historic_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType(
			COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Read the data...
		if (	continue_reading && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			Vector v = StateMod_TS.readTimeSeriesList(fn, null,
				null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i<size; i++) {
				// Set this information because it is not in the
				// StateMod time series file...
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType (
				COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY ));
			}
			comp.setData(v);
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
		"Error reading historical streamflow time series file:\n"
		+ "\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Diversion time series (historical monthly) file (.ddh)...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue (
				"Diversion_Historic_Monthly" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Make sure the file name is set in the component...
		comp = getComponentForComponentType(COMP_DIVERSION_TS_MONTHLY );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		// Now read the file if requested...
		if (	continue_reading && __tsAreRead && (fn != null) &&
			!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
			readTime.clear();
			readTime.start();
			readInputAnnounce1(comp);
			fn = getDataFilePathAbsolute ( fn );
			Vector v = StateMod_TS.readTimeSeriesList(fn, 
				null, null, null, true);
			if (v == null) {
				v = new Vector();
			}
			size = v.size();
			for (i = 0; i < size; i++) {
				((MonthTS)v.elementAt(i)).setDataType(
				lookupTimeSeriesDataType (
				COMP_DIVERSION_TS_MONTHLY ));
			}
			comp.setData ( v );
			comp.setDirty ( false );
			readTime.stop();
			readInputAnnounce2(comp, readTime.getSeconds() );
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading historical diversion time "
			+ "series (monthly) file:\n\"" + fn + "\"");
		Message.printWarning(2, routine, e);
	}

	// Well historical pumping time series (monthly) file (.weh)..

	if ( __is_free_format || (!__is_free_format && hasWellData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Well_Historic_Monthly" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_WELL_PUMPING_TS_MONTHLY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i < size; i++) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_WELL_PUMPING_TS_MONTHLY ));
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
				"Error reading well pumping time "
				+ "series (monthly) file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// GeoView project file...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "GeographicInformation");
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_GEOVIEW );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
		if ( continue_reading && (fn != null) ) {
			readTime.clear();
			readTime.start();
			fn = getDataFilePathAbsolute ( fn );
			readInputAnnounce1(comp);
			comp.setDirty ( false );
			readTime.stop();
			//readInputAnnounce2(comp, readTime.getSeconds() );
			// Read data and display when the GUI is shown - no read
			// for data to be read if no GUI
		} 
	}
	catch (Exception e) {
		// Print this at level 2 because the main GUI will warn if it
		// cannot read the file.  We don't want 2 warnings.
		Message.printWarning(2, routine, "Unable to read/process "
			+ " GeoView project file \"" + fn + "\"" );
		Message.printWarning(2, routine, e);
	}

	// REVISIT - output control - this is usually read separately when
	// running reports, etc.  Just read the line but do not read the file...

	try {	fn = null;
		if ( __is_free_format ) {
			fn = response_props.getValue ( "OutputRequest" );
		}
		else {	if (	continue_reading &&
				((fn = readFileLine ( in )) == null) ) {
				continue_reading = false;
			}
		}
		// Always set the file name...
		comp = getComponentForComponentType( COMP_OUTPUT_REQUEST );
		if ( (comp != null) && (fn != null) ) {
			comp.setDataFileName ( fn );
		}
	} catch (Exception e) {
		//Message.printWarning(1, routine, "Error reading output"+
		//" control file:\n\""+fn+"\"");
		//Message.printWarning(2, routine, e);
	}

	// Stream base flow time series (daily) file (.rid)...
	// Always read if a daily data set.

	if ( __is_free_format || (!__is_free_format && hasDailyData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Stream_Base_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType(
				COMP_STREAMGAGE_BASEFLOW_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(
					fn, null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i < size; i++) {
					((DayTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_STREAMGAGE_BASEFLOW_TS_DAILY ));
				}
				comp.setData(v);
				comp.setDirty ( false );

				// The StreamGage and StreamEstimate groups
				// share the same baseflow time series files...

				DataSetComponent comp2 =
				getComponentForComponentType(
				COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY );
				comp2.setDataFileName ( comp.getDataFileName());
				comp2.setData(v);
				comp2.setDirty ( false );

				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading daily baseflow time series file:\n\"" +
				fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}
	
	// Direct diversion demand time series (daily) file (.ddd)...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Diversion_Demand_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp=getComponentForComponentType(COMP_DEMAND_TS_DAILY);
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn, 
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i < size; i++) {
					((DayTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_DEMAND_TS_DAILY ));
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading daily demand time series file:\n"+
			"\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Instream flow demand time series (daily) file (.ifd)...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Instreamflow_Demand_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_INSTREAM_DEMAND_TS_DAILY);
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i< size; i++) {
					((DayTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_INSTREAM_DEMAND_TS_DAILY ));
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading"
			+ " daily instream flow demand time series"
			+ " file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Well demand time series (daily) file (.wed)...

	if (	__is_free_format ||
		(!__is_free_format&&hasDailyData(false) && hasWellData(false))){
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Well_Demand_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_WELL_DEMAND_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();				
				for (i = 0; i < size; i++) {
					((DayTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_WELL_DEMAND_TS_DAILY ));
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading daily "+
				"well demand time series file:\n\"" +fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Reservoir target time series (daily) file (.tad)...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Reservoir_Target_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_RESERVOIR_TARGET_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null) 
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i < size; i++) {
					if ( (i%2) == 0 ) {
						((DayTS)v.elementAt(i)).
						setDataType(
						lookupTimeSeriesDataType (
						COMP_RESERVOIR_TARGET_TS_DAILY)+
						"Min");
					}
					else {	((DayTS)v.elementAt(i)).
						setDataType(
						lookupTimeSeriesDataType (
						COMP_RESERVOIR_TARGET_TS_DAILY)+
						"Max");
					}
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading"
			+ "daily reservoir target time series file:\n\""
			+ fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Delay table (daily)...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"DelayTable_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType(
				COMP_DELAY_TABLES_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				comp.setData( StateMod_DelayTable.
					readStateModFile(
					fn,false,getInterv()) );
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
				"Error reading delay table "
				+ " (daily) file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Irrigation water requirement (iwr) - daily...

	if (	__is_free_format || (!__is_free_format && hasDailyData(false) &&
		hasIrrigationWaterRequirementData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"ConsumptiveWaterRequirement_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();				
				for (i = 0; i < size; i++) {
					((DayTS)v.elementAt(i)).setDataType(
						lookupTimeSeriesDataType (
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine, "Error reading "+
			"irrigation water requirement (daily) time series " +
			"file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Streamflow historical time series (daily) file (.riy) - always
	// read...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"StreamGage_Historic_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Always set the file name...
			comp = getComponentForComponentType(
				COMP_STREAMGAGE_HISTORICAL_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null,null, null, true);
				if (v == null) {
					v = new Vector();
				}
				size = v.size();
				for (i = 0; i<size; i++) {
					// Set this information because it is
					// not in the StateMod time series
					// file...
					((DayTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_STREAMGAGE_HISTORICAL_TS_DAILY) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		}
		catch (Exception e) {
			Message.printWarning(1, routine, "Error reading "+
			"daily historical streamflow time series file:\n\"" +
			fn+ "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Diversion (daily) time series (.ddd)...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false) ) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
				"Diversion_Historic_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_DIVERSION_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				// Set the data type because it is not in the
				// StateMod file...
				size = v.size();
				for (i = 0; i < size; i++) {
					((DayTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_DIVERSION_TS_DAILY) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading diversion (daily) time series " +
			"file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Well pumping (daily) time series...

	if (	__is_free_format ||
		(!__is_free_format&&hasDailyData(false) && hasWellData(false))){
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Well_Historic_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_WELL_PUMPING_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Now read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&&!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				// Set the data type because it is not in the
				// StateMod file...
				size = v.size();
				for (i = 0; i < size; i++) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_WELL_PUMPING_TS_DAILY) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading well pumping (daily) time series " +
			"file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Daily reservoir content "eoy"...

	if ( __is_free_format || (!__is_free_format && hasDailyData(false)) ) {
		try {	fn = null;
			if ( __is_free_format ) {
				fn = response_props.getValue (
					"Reservoir_Historic_Daily" );
			}
			else {	if (	continue_reading &&
					((fn = readFileLine ( in )) == null) ) {
					continue_reading = false;
				}
			}
			// Set the file name in the component...
			comp = getComponentForComponentType(
				COMP_RESERVOIR_CONTENT_TS_DAILY );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data file...
			if (	continue_reading && __tsAreRead && (fn != null)
				&& !fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				Vector v = StateMod_TS.readTimeSeriesList(fn,
					null, null, null, true);
				if (v == null) {
					v = new Vector();
				}
				// Set the data type because it is not in the
				// StateMod file...
				size = v.size();
				for (i = 0; i < size; i++) {
					((MonthTS)v.elementAt(i)).setDataType(
					lookupTimeSeriesDataType (
					COMP_RESERVOIR_CONTENT_TS_DAILY) );
				}
				comp.setData(v);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		} catch (Exception e) {
			Message.printWarning(1, routine,
			"Error reading reservoir end of day time series " +
			"file:\n\"" + fn + "\"");
			Message.printWarning(2, routine, e);
		}
	}

	// Connect all the instream flow time series to the stations...

	Message.printStatus (1,routine,"Connect all instream flow time series");
	StateMod_InstreamFlow.connectAllTS (
		(Vector)getComponentForComponentType(
			COMP_INSTREAM_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_INSTREAM_DEMAND_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_INSTREAM_DEMAND_TS_DAILY).getData() );

	// Connect all the reservoir time series to the stations...

	StateMod_Reservoir.connectAllTS (
		(Vector)getComponentForComponentType(
			COMP_RESERVOIR_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_RESERVOIR_CONTENT_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_RESERVOIR_CONTENT_TS_DAILY).getData(),
		(Vector)getComponentForComponentType(
			COMP_RESERVOIR_TARGET_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_RESERVOIR_TARGET_TS_DAILY).getData());

	// Connect all the diversion time series to the stations...

	Message.printStatus ( 1, routine, "Connect all diversion time series");
	StateMod_Diversion.connectAllTS(
		(Vector)getComponentForComponentType(
			COMP_DIVERSION_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_DIVERSION_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_DIVERSION_TS_DAILY).getData(),
		(Vector)getComponentForComponentType(
			COMP_DEMAND_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_DEMAND_TS_OVERRIDE_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_DEMAND_TS_AVERAGE_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_DEMAND_TS_DAILY).getData(),
		(Vector)getComponentForComponentType(
			COMP_IRRIGATION_PRACTICE_TS_YEARLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY).
			getData(),
		(Vector)getComponentForComponentType(
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY).getData());

	// Connect all the well time series to the stations...

	Message.printStatus ( 1, routine, "Connect all well time series");
	StateMod_Well.connectAllTS(
		(Vector)getComponentForComponentType(
			COMP_WELL_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_WELL_PUMPING_TS_MONTHLY).getData(), 
		(Vector)getComponentForComponentType(
			COMP_WELL_PUMPING_TS_DAILY).getData(), 
		(Vector)getComponentForComponentType(
			COMP_WELL_DEMAND_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_WELL_DEMAND_TS_DAILY).getData(),
		(Vector)getComponentForComponentType(
			COMP_IRRIGATION_PRACTICE_TS_YEARLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY).
			getData(),
		(Vector)getComponentForComponentType(
			COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY).getData());

	// Process the old-style ris, rim, rid files for the new convention...

	if ( !__is_free_format ) {
		StateMod_StreamEstimate.processStreamData ( 
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMESTIMATE_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMESTIMATE_COEFFICIENTS).getData() );
	}	// Else the StreamGage and StreamEstimate stations are already
		// split into separate files.

	// Connect all the stream gage station time series to the stations...

	Message.printStatus (1,routine,"Connect all river station time series");
	StateMod_StreamGage.connectAllTS(
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_HISTORICAL_TS_DAILY).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMGAGE_BASEFLOW_TS_DAILY).getData());

	// Connect all the stream estimate station time series to the
	// stations...

	Message.printStatus (1,routine,
		"Connect all stream estimate station time series");
	StateMod_StreamEstimate.connectAllTS(
		(Vector)getComponentForComponentType(
			COMP_STREAMESTIMATE_STATIONS).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMESTIMATE_BASEFLOW_TS_MONTHLY).getData(),
		(Vector)getComponentForComponentType(
			COMP_STREAMESTIMATE_BASEFLOW_TS_DAILY).getData());

	totalReadTime.stop();
	Message.printStatus(1, routine, "Total time to read StateMod files is "
		+ StringUtil.formatString(totalReadTime.getSeconds(),"%.3f")
		+ " seconds");
	totalReadTime.start();

	// Convert the StateMod network to the more general network object...

	Message.printStatus ( 1, routine,
	"Creating linked network from data set files." );

	try {	fn = null;
		if ( __is_free_format ) {
			// only read in if from a free-format file
			fn = response_props.getValue ( "Network" );
			// Always set the file name...
			comp = getComponentForComponentType( COMP_NETWORK );
			if ( (comp != null) && (fn != null) ) {
				comp.setDataFileName ( fn );
			}
			// Read the data...
			if (	continue_reading && (fn != null) &&
				!fileIsEmpty(getDataFilePathAbsolute(fn)) ) {
				readTime.clear();
				readTime.start();
				fn = getDataFilePathAbsolute ( fn );
				readInputAnnounce1(comp);
				HydroBase_NodeNetwork network 
					= HydroBase_NodeNetwork.readStateModNetworkFile( fn, null, true);
				comp.setData(network);
				comp.setVisible(true);
				comp.setDirty ( false );
				readTime.stop();
				readInputAnnounce2(comp, readTime.getSeconds());
			}
		}
	} catch (Exception e) {
		Message.printWarning(1, routine,
			"Error reading network file:\n\"" + fn+"\"");
		Message.printWarning(2, routine, e);
	}

	
//	Network network = createNetworkFromStateModRiverNetwork();
//	getComponentForComponentType( COMP_NETWORK).setData(network);
	Message.printStatus ( 1, routine,
		"Finished creating linked network from data set files.");

	} catch (Exception e) {
		// Main catch for all reads.
		Message.printStatus ( 1, routine, "Error during read.");
		Message.printWarning(1, routine, e);
		// REVISIT...
		// Just rethrow for now
		throw ( e );
	}

	// Check the filenames for 8.3.  This limitation may be removed at
	// some point...
	// REVISIT - may move this to the front again, but only if the response
	// file is read up front for the check and then reading continues as per
	// the above logic.

/* REVISIT 
	if (	!checkComponentFilenames(files_from_response, 1) &&
		useGUI && (parent != null) ) {
		Message.printWarning ( 1, routine, "StateMod may not run." );
	}
*/

	// Set component visibility based on the the control information...

	checkComponentVisibility ();

	totalReadTime.stop();
	String msg = "Total time to read all files is "
		+ StringUtil.formatString(totalReadTime.getSeconds(),"%.3f")
		+ " seconds";
	Message.printStatus(1, routine, msg );
	sendProcessListenerMessage(StateMod_GUIUtil.STATUS_READ_COMPLETE, msg );
	setDirty(COMP_CONTROL, false);

	readTime = null;
	totalReadTime = null;
	// REVISIT - uncomment for debugging
	//Message.printStatus ( 1, routine,
	//		"SAMX - After reading all files, control dirty =" +
	//		getComponentForComponentType(
	//		COMP_CONTROL).isDirty() );
	//Message.printStatus ( 1, routine, super.toString () );
}

/**
Read the next file name from the StateMod response file.  Skip over comments
and blank lines and process only lines that have file names.
@param in BufferedRead to read.
@return the File name read from the response file, or null if at the end of
the file.  The file name may be relative (to the directory where the response
file resides), or absolute.
@exception if there is an error reading the file.
*/
private String readFileLine ( BufferedReader in )
throws Exception
{	String fn = null;	// File name
	String rsp_line;	// Line read from response file.
	while ( (rsp_line = in.readLine()) != null ) {
		// Check for comments...
		rsp_line = rsp_line.trim();
		if ( rsp_line.startsWith("#") || (rsp_line.length() == 0) ) {
			continue;
		}

		// The actual file name is simply the first string on the line.
		// However, sometimes directories and files have spaces so
		// assume that if more than one space is together that it
		// signifies the end of the row.

		int last_index = rsp_line.indexOf("  ");	// 2 spaces
		if (last_index >= 0) {
			// Sequence of 2 spaces has been found so shorten the
			// name...
			fn = rsp_line.substring(0, last_index).trim();
		}

		break;
	}
	return fn;
}

/**
This method is a helper routine to readStateModFile().  It calls
Message.printStatus() with the message that a particular file is being read,
including path.  Then it prints a similar, but shorter,
message to the status bar.  If there is an error with the file (not specified,
does not exist, etc.), then an Exception is thrown.  There are many StateMod
files and therefore the same basic checks are done many times.
@param comp Data set component that is being read.
@exception if there is a basic error with the file not being found, etc.
*/
private void readInputAnnounce1 ( DataSetComponent comp )
throws Exception
{	String fn = getDataFilePathAbsolute ( comp );
	String description = comp.getComponentName();

	if ( (fn == null) || (fn.length() == 0) ) {
		throw new Exception(description + " file name unavailable.");
	}
	// REVISIT - need to know whether this is an error that the user
	// should acknowlege...
	if ( !IOUtil.fileExists(fn) ) {
		throw new Exception(description + " file \""
			+ fn + "\" does not exist.");
	}
	if ( !IOUtil.fileReadable(fn)) {
		throw new Exception(description + " file \""
			+ fn + "\" not readable.");
	}

	String msg = "Reading " + description + " data from \"" + fn + "\"";
	// The status message is printed becauset process listeners may not
	// be registered.
	Message.printStatus(1, "StateMod_DataSet.readInputAnnounce1", msg );
	sendProcessListenerMessage ( StateMod_GUIUtil.STATUS_READ_START, msg );
}

/**
This method is a helper routine to readStateModFile().  It calls
Message.printStatus() with the message that a file has been read successively.
Then it prints a similar, but shorter, message to the status bar.
@param comp Component being read.
@param seconds Number of seconds to read.
*/
private void readInputAnnounce2 ( DataSetComponent comp, double seconds )
{	String routine = "StateMod_DataSet.readInputAnnounce2";
	String fn = getDataFilePathAbsolute ( comp );
	String description = comp.getComponentName();

	// The status message is printed becauset process listeners may not
	// be registered.
	String msg = description + " data read from \"" + fn + "\" in "
	+ StringUtil.formatString(seconds,"%.3f") + " seconds";
	Message.printStatus(1, routine, msg );
	sendProcessListenerMessage (StateMod_GUIUtil.STATUS_READ_COMPLETE,msg);
}

/**
Remove a ProcessListener that was previously added with addProcessListener().
@param p ProcessListener to remove.
*/
public void removeProcessListener(ProcessListener p) {
	if (__processListeners == null) {
		return;
	}
	__processListeners.removeElement(p);
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
	StateMod_ComponentDataCheck check = new StateMod_ComponentDataCheck(
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
Write the data set to an XML file.  The filename is adjusted to the 
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev the name of the previous version of the file(for
processing headers).  Specify as null if no previous file is available.
@param filename the name of the file to write.
@param dataset the dataset
@param new_comments Comments to add to the top of the file.  Specify as null
if no comments are available.
@throws IOException if there is an error writing the file.
*/
public void writeXMLFile(String filename_prev, String filename, 
StateMod_DataSet dataset, String[] new_comments)
throws IOException {
	String[] comment_str = { "#" };
	String[] ignore_comment_str = { "#" };
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir(
		filename_prev);
	if (!StringUtil.endsWithIgnoreCase(filename, ".xml")) {
		filename = filename + ".xml";
	}

	String full_filename = IOUtil.getPathUsingWorkingDir(filename);
	out = IOUtil.processFileHeaders(full_filename_prev, full_filename,
		new_comments, comment_str, ignore_comment_str, 0);
	if (out == null) {
		throw new IOException("Error writing to \"" 
			+ full_filename + "\"");
	}

	writeDataSetToXMLFile(dataset, out);
	out.flush();
	out.close();
	out = null;
}

/**
Write a data set to an opened XML file.
@param data a StateMod_DataSet to write.
@param out output PrintWriter
@throws IOException if an error occurs.
*/
private void writeDataSetToXMLFile(StateMod_DataSet dataset,
PrintWriter out)
throws IOException {
	String comment = "#>";
	DataSetComponent comp = null;
	// start xml tag...
	out.println("<!--");
	out.println(comment);
	out.println(comment + "  StateMod Data Set(XML)File");
	out.println(comment);
	out.println(comment + "EndHeader");
	out.println("-->");

	out.println("<StateMod_DataSet "
		+ "Type=\"" + StateMod_DataSet.lookupTypeName(dataset.getDataSetType())
		+ "\"" + "BaseName=\"" + dataset.getBaseName() + "\">");
	
	int num = 0;
	Vector data_Vector = dataset.getComponents();
	if (data_Vector != null) {
		num = data_Vector.size();
	}

	String indent1 = "  ";
	String indent2 = indent1 + indent1;

	for (int i = 0; i < num; i++) {
		comp = (DataSetComponent)data_Vector.elementAt(i);
		if (comp == null) {
			continue;
		}

		out.println(indent1 + "<DataSetComponent");
		out.println(indent2 + "Type=\""
			+ dataset.lookupComponentName(
			comp.getComponentType())+ "\"");
		out.println(indent2 + "DataFile=\"" + comp.getDataFileName()
			+ "\"");
		out.println(indent2 + "ListFile=\"" + comp.getListFileName()
			+ "\"");
		out.println(indent2 + "CommandsFile=\""
			+ comp.getCommandsFileName()+ "\"");
		out.println(indent2 + ">");
	
		out.println(indent1 + "</DataSetComponent>");
	}

	out.println("</StateMod_DataSet>");
}

/**
Add a process listener.  This is used to notify other code (e.g., the StateMod
GUI as progress is made reading the data set).
@param p ProcessListener to remove.
*/
public void addProcessListener(ProcessListener p) {
	if (__processListeners == null) {
		__processListeners = new Vector();
	}
	__processListeners.add(p);
}

/**
Send a message to ProcessListener that have been registered with this object.
This is usually a main application that is giving feedback to a user via the
messages.
*/
public void sendProcessListenerMessage ( int status, String message )
{	int size = 0;
	if (__processListeners != null) {
		size = __processListeners.size();
	}

	ProcessListener p = null;
	for (int i = 0; i < size; i++) {
		p = (ProcessListener)__processListeners.elementAt(i);
		p.processStatus(status, message);
	}
}

/**
Set detailed call water right ID
@param ccall __ccall to set
*/
public void setCcall(String ccall) {
	if ( (ccall != null) && !ccall.equals(__ccall)) {
		__ccall = ccall;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the factor for converting reservoir content data to AF
@param cfacto factor
*/
public void setCfacto(double cfacto) {
	if (cfacto != __cfacto) {
		__cfacto = cfacto;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the factor for converting reservoir content data to AF
@param  cfacto factor
*/
public void setCfacto(Double cfacto) {
	setCfacto(cfacto.doubleValue());
}

/**
Set the factor for converting reservoir content data to AF
@param  cfacto factor
*/
public void setCfacto(String cfacto) {
	if (cfacto != null) {
		setCfacto(StringUtil.atod(cfacto.trim()));
	}
}

/**
Set the calendar/water/irrigation year
@param  cyrl year type
*/
public void setCyrl(Integer cyrl) {
	setCyrl(cyrl.intValue());
}

/**
Set the calendar/water/irrigation year
@param  cyrl year type
*/
public void setCyrl(int cyrl) {
	if (cyrl != __cyrl) {
		__cyrl = cyrl;
		setDirty(COMP_CONTROL, true);
	}
	if (cyrl != SM_CYR && cyrl != SM_WYR && cyrl != SM_IYR) {
		Message.printWarning(1, "StateMod_DataSet.setCyrl", 
			"Setting year type using invalid value("
			+ cyrl + "); use SM_CYR, SM_WYR, or SM_IYR");
	}
}

/**
Set the calendar/water/irrigation year
@param  cyrl year type
*/
public void setCyrl(String cyrl) {
	if (cyrl == null) {
		return;
	}
	// expecting "CYR", "WYR", "IYR"
	cyrl = cyrl.trim();
	if (cyrl.equalsIgnoreCase("CYR")) {
		setCyrl(SM_CYR);
	}
	else if (cyrl.equalsIgnoreCase("WYR")) {
		setCyrl(SM_WYR);
	}
	else if (cyrl.equalsIgnoreCase("IYR")) {
		setCyrl(SM_IYR);
	}
	else {	Message.printWarning(1, "StateMod_Control.setCyrl", 
			"Setting year type using invalid value("
			+ cyrl + "); use \"CYR\", \"WYR\", or \"IYR\"");
		setCyrl(SM_CYR);
	}
}

/**
Set the divisor for diversion data units
@param  dfacto factor
*/
public void setDfacto(double dfacto) {
	if (dfacto != __dfacto) {
		__dfacto = dfacto;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the divisor for diversion data units
@param  dfacto factor
*/
public void setDfacto(Double dfacto) {
	setDfacto(dfacto.doubleValue());
}

/**
Set the divisor for diversion data units
@param  dfacto factor
*/
public void setDfacto(String dfacto) {
	if (dfacto != null) {
		setDfacto(StringUtil.atod(dfacto.trim()));
	}
}

/**
Set the factor for converting evaporation data to FT
@param  efacto factor
*/
public void setEfacto(double efacto) {
	if (efacto != __efacto) {
		__efacto = efacto;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the factor for converting evaporation data to FT
@param  efacto factor
*/
public void setEfacto(Double efacto) {
	setEfacto(efacto.doubleValue());
}

/**
Set the factor for converting evaporation data to FT
@param  efacto factor
*/
public void setEfacto(String efacto) {
	if (efacto != null) {
		setEfacto(StringUtil.atod(efacto.trim()));
	}
}

/**
Set the divisor for instreamflow data units
@param  ffacto factor
*/
public void setFfacto(double ffacto) {
	if (ffacto != __ffacto) {
		__ffacto = ffacto;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the divisor for instreamflow data units
@param  ffacto factor
*/
public void setFfacto(Double ffacto) {
	setFfacto(ffacto.doubleValue());
}

/**
Set the divisor for instreamflow data units
@param ffacto factor
*/
public void setFfacto(String ffacto) {
	if (ffacto != null) {
		setFfacto(StringUtil.atod(ffacto.trim()));
	}
}

/**
Set factor for converting CFS to AF/Day (1.9835).
@param factor factor
*/
public void setFactor(double factor) {
	if (factor != __factor) {
		__factor = factor;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set factor for converting CFS to AF/Day (1.9835).
@param  factor factor
*/
public void setFactor(Double factor) {
	setFactor(factor.doubleValue());
}

/**
Set factor for converting CFS to AF/Day (1.9835).
@param  factor factor
*/
public void setFactor(String factor) {
	if (factor != null) {
		setFactor(StringUtil.atod(factor.trim()));
	}
}

/**
Set gwmaxrc
*/
public void setGwmaxrc(double gwmaxrc) {
	if (gwmaxrc != __gwmaxrc) {
		__gwmaxrc = gwmaxrc;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set gwmaxrc
*/
public void setGwmaxrc(Double gwmaxrc) {
	setGwmaxrc(gwmaxrc.doubleValue());
}

/**
Set gwmaxrc
*/
public void setGwmaxrc(String gwmaxrc) {
	if (gwmaxrc != null) {
		setGwmaxrc(StringUtil.atod(gwmaxrc.trim()));
	}
}

/**
Set first line of heading
@param  heading1 first line of text in file
*/
public void setHeading1(String heading1) {
	if ( (heading1 != null) && !heading1.equals(__heading1)) {
		__heading1 = heading1;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set second line of heading.
@param  heading2 second line of text in file.
*/
public void setHeading2(String heading2) {
	if ( (heading2 != null) && !heading2.equals(__heading2)) {
		__heading2 = heading2;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for detailed call data
*/
public void setIcall(int icall) {
	if (icall != __icall) {
		__icall = icall;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for detailed call data
*/
public void setIcall(Integer icall) {
	setIcall(icall.intValue());
}

/**
Set the switch for detailed call data
*/
public void setIcall(String icall) {
	if (icall != null) {
		setIcall(StringUtil.atoi(icall.trim()));
	}
}

/**
Set the switch for detailed printout.
@param  ichk switch
*/
public void setIchk(int ichk) {
	if (ichk != __ichk) {
		__ichk = ichk;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for detailed printout
*/
public void setIchk(Integer ichk) {
	setIchk(ichk.intValue());
}

/**
Set the switch for detailed printout
*/
public void setIchk(String ichk) {
	if (ichk != null) {
		setIchk(StringUtil.atoi(ichk.trim()));
	}
}

/**
Set the switch for demand type
@param icondem __icondem to set
*/
public void setIcondem(int icondem) {
	if (icondem != __icondem) {
		__icondem = icondem;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for demand type
@param icondem __icondem to set
*/
public void setIcondem(Integer icondem) {
	setIcondem(icondem.intValue());
}

/**
Set the switch for demand type
@param icondem __icondem to set
*/
public void setIcondem(String icondem)
{	if (icondem != null) {
		setIcondem(StringUtil.atoi(icondem.trim()));
	}
}

/**
Set the switch for daily calculations
*/
public void setIday(int iday) {
	if (iday != __iday) {
		__iday = iday;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for daily calculations
*/
public void setIday(Integer iday) {
	setIday(iday.intValue());
}

/**
Set the switch for daily calculations
*/
public void setIday(String iday) {
	if (iday != null) {
		setIday(StringUtil.atoi(iday.trim()));
	}
}

/**
Set the switch for IWR.
@param  ieffmax switch
*/
public void setIeffmax(int ieffmax) {
	if (ieffmax != __ieffmax) {
		__ieffmax = ieffmax;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for IWR.
*/
public void setIeffmax(Integer ieffmax) {
	setIeffmax(ieffmax.intValue());
}

/**
Set the switch for IWR.
*/
public void setIeffmax(String ieffmax) {
	if (ieffmax != null) {
		setIeffmax(StringUtil.atoi(ieffmax.trim()));
	}
}

/**
Set max number of entries in a delay pattern.
@param  interv number of entries
*/
public void setInterv(int interv) {
	if (interv != __interv) {
		__interv = interv;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set max number of entries in a delay pattern.
@param  interv number of entries
*/
public void setInterv(Integer interv) {
	setInterv(interv.intValue());
}

/**
Set max number of entries in a delay pattern.
@param  interv number of entries
*/
public void setInterv(String interv) {
	if (interv != null) {
		setInterv(StringUtil.atoi(interv.trim()));
	}
}

/**
Set streamflow type.  Use SM_TOTAL or SM_GAINS.
@param  iopflo streamflow type
*/
public void setIopflo(int iopflo) {
	if (iopflo != __iopflo) {
		__iopflo = iopflo;
		setDirty(COMP_CONTROL, true);
	}

	if (iopflo != SM_TOT && iopflo != SM_GAINS) {
		Message.printWarning(1, "StateMod_Control.setIopflo", 
			"Setting iopflo using invalid value("
			+ iopflo + "); use SM_TOT or SM_GAINS");
	}
}

/**
Set streamflow type.
@param  iopflo streamflow type
*/
public void setIopflo(Integer iopflo) {
	setIopflo(iopflo.intValue());
}

/**
Set streamflow type.
@param  iopflo streamflow type
*/
public void setIopflo(String iopflo) {
	if (iopflo != null) {
		setIopflo(StringUtil.atoi(iopflo.trim()));
	}
}

/**
Set the switch for instream flow reach approach
*/
public void setIreach(int ireach) {
	if (ireach != __ireach) {
		__ireach = ireach;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for instream flow reach approach
*/
public void setIreach(Integer ireach) {
	setIreach(ireach.intValue());
}

/**
Set the switch for instream flow reach approach
*/
public void setIreach(String ireach) {
	if (ireach != null) {
		setIreach(StringUtil.atoi(ireach.trim()));
	}
}

/**
Set the switch for reoperation control.
*/
public void setIreopx(int ireopx) {
	if (ireopx != __ireopx) {
		__ireopx = ireopx;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for reoperation control
*/
public void setIreopx(Integer ireopx) {
	setIreopx(ireopx.intValue());
}

/**
Set the switch for reoperation control
*/
public void setIreopx(String ireopx) {
	if (ireopx != null) {
		setIreopx(StringUtil.atoi(ireopx.trim()));
	}
}

/**
Set switch for output.  Use SM_CFS, SM_ACFT, SM_KACFT.
@param  iresop switch
*/
public void setIresop(int iresop) {
	if (iresop != __iresop) {
		__iresop = iresop;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set switch for output.
@param iresop switch
*/
public void setIresop(Integer iresop) {
	setIresop(iresop.intValue());
}

/**
Set switch for output.
@param iresop switch
*/
public void setIresop(String iresop) {
	if (iresop != null) {
		setIresop(StringUtil.atoi(iresop.trim()));
	}
}

/**
Set the switch for significant figures.
@param isig switch
*/
public void setIsig(int isig) {
	if (isig != __isig) {
		__isig = isig;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for significant figures.
*/
public void setIsig(Integer isig) {
	setIsig(isig.intValue());
}

/**
Set the switch for significant figures.
*/
public void setIsig(String isig) {
	if (isig != null) {
		setIsig(StringUtil.atoi(isig.trim()));
	}
}

/**
Set the switch for SJRIP.
@param  isjrip switch
*/
public void setIsjrip(int isjrip) {
	if (isjrip != __isjrip) {
		__isjrip = isjrip;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for SJRIP.
*/
public void setIsjrip(Integer isjrip) {
	setIsjrip(isjrip.intValue());
}

/**
Set the switch for SJRIP.
*/
public void setIsjrip(String isjrip) {
	if (isjrip != null) {
		setIsjrip(StringUtil.atoi(isjrip.trim()));
	}
}

/**
Set the switch for sprinklers.
@param  isprink switch
*/
public void setIsprink(int isprink) {
	if (isprink != __isprink) {
		__isprink = isprink;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for sprinklers.
*/
public void setIsprink(Integer isprink) {
	setIsprink(isprink.intValue());
}

/**
Set the switch for sprinklers.
*/
public void setIsprink(String isprink) {
	if (isprink != null) {
		setIsprink(StringUtil.atoi(isprink.trim()));
	}
}

/**
Set the switch for the ipy/tsp file.
@param  itsfile switch
*/
public void setItsfile(int itsfile) {
	if (itsfile != __itsfile) {
		__itsfile = itsfile;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for the ipy/tsp file.
*/
public void setItsfile(Integer itsfile) {
	setItsfile(itsfile.intValue());
}

/**
Set the switch for the ipy/tsp file.
*/
public void setItsfile(String itsfile) {
	if (itsfile != null) {
		setItsfile(StringUtil.atoi(itsfile.trim()));
	}
}

/**
Set the switch for well calculations
*/
public void setIwell(int iwell) {
	if (iwell != __iwell) {
		__iwell = iwell;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the switch for well calculations
*/
public void setIwell(Integer iwell) {
	setIwell(iwell.intValue());
}

/**
Set the switch for well calculations
*/
public void setIwell(String iwell) {
	if (iwell != null) {
		setIwell(StringUtil.atoi(iwell.trim()));
	}
}

/**
Set ending year of the simulation.
@param  iyend ending year
*/
public void setIyend(int iyend) {
	if (iyend != __iyend) {
		__iyend = iyend;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set ending year of the simulation.
@param  iyend ending year
*/
public void setIyend(Integer iyend) {
	setIyend(iyend.intValue());
}

/**
Set ending year of the simulation.
@param  iyend ending year
*/
public void setIyend(String iyend) {
	if (iyend != null) {
		setIyend(StringUtil.atoi(iyend.trim()));
	}
}

/**
Set starting year of the simulation.
@param  iystr starting year
*/
public void setIystr(int iystr) {
	if (iystr != __iystr) {
		__iystr = iystr;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set starting year of the simulation.
@param  iystr starting year
*/
public void setIystr(Integer iystr) {
	setIystr(iystr.intValue());
}

/**
Set starting year of the simulation.
@param  iystr starting year
*/
public void setIystr(String iystr) {
	if (iystr != null) {
		setIystr(StringUtil.atoi(iystr.trim()));
	}
}

/**
Set type of evaporation data. Use SM_MONTHLY or SM_AVERAGE.
@param  moneva type of evaporation data
*/
public void setMoneva(int moneva) {
	if (moneva != __moneva) {
		__moneva = moneva;
		setDirty(COMP_CONTROL, true);
	}

	if (moneva != SM_MONTHLY && moneva != SM_AVERAGE) {
		Message.printWarning(1, "StateMod_Control.setMoneva", 
			"Setting moneva using invalid value("
			+ moneva + "); use SM_MONTHLY or SM_AVERAGE");
	}
}

/**
Set type of evaporation data.
@param  moneva type of evaporation data
*/
public void setMoneva(Integer moneva) {
	setMoneva(moneva.intValue());
}

/**
Set type of evaporation data.
@param  moneva type of evaporation data
*/
public void setMoneva(String moneva) {
	if (moneva != null) {
		setMoneva(StringUtil.atoi(moneva.trim()));
	}
}

/**
Set number of evaporation stations.
@param numeva number of stations
*/
public void setNumeva(int numeva) {
	if (numeva != __numeva) {
		__numeva = numeva;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set number of evaporation stations.
@param  numeva number of stations
*/
public void setNumeva(Integer numeva) {
	setNumeva(numeva.intValue());
}

/**
Set number of evaporation stations.
@param  numeva number of stations
*/
public void setNumeva(String numeva) {
	if (numeva != null) {
		setNumeva(StringUtil.atoi(numeva.trim()));
	}
}

/**
Set number of precipitation stations.
@param numpre number of stations
*/
public void setNumpre(int numpre) {
	if (numpre != __numpre) {
		__numpre = numpre;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set number of precipitation stations.
@param numpre number of stations
*/
public void setNumpre(Integer numpre) {
	setNumpre(numpre.intValue());
}

/**
Set number of precipitation stations.
@param numpre number of stations
*/
public void setNumpre(String numpre) {
	if (numpre != null) {
		setNumpre(StringUtil.atoi(numpre.trim()));
	}
}

/**
Set the factor for converting precipitation data to FT
@param  pfacto factor
*/
public void setPfacto(double pfacto) {
	if (pfacto != __pfacto) {
		__pfacto = pfacto;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set the factor for converting precipitation data to FT
@param  pfacto factor
*/
public void setPfacto(Double pfacto) {
	setPfacto(pfacto.doubleValue());
}

/**
Set the factor for converting precipitation data to FT
@param  pfacto factor
*/
public void setPfacto(String pfacto) {
	if (pfacto != null) {
		setPfacto(StringUtil.atod(pfacto.trim()));
	}
}

/**
Set the divisor for streamflow data units.
@param  rfacto factor
*/
public void setRfacto(double rfacto) {
	if (rfacto != __rfacto) {
		__rfacto = rfacto;
		setDirty(COMP_CONTROL, true);
	}
}
/**
Set the divisor for streamflow data units.
@param  rfacto factor
*/
public void setRfacto(Double rfacto) {
	setRfacto(rfacto.doubleValue());
}

/**
Set the divisor for streamflow data units.
@param  rfacto factor
*/
public void setRfacto(String rfacto) {
	if (rfacto != null) {
		setRfacto(StringUtil.atod(rfacto.trim()));
	}
}

/**
Set soild
*/
public void setSoild(double soild) {
	if (soild != __soild) {
		__soild = soild;
		setDirty(COMP_CONTROL, true);
	}
}

/**
Set soild
*/
public void setSoild(Double soild) {
	setSoild(soild.doubleValue());
}

/**
Set soild
*/
public void setSoild(String soild) {
	if (soild != null) {
		setSoild(StringUtil.atod(soild.trim()));
	}
}

/**
Writes the new (updated) control file.
If an original file is specified, then the original header is carried
into the new file.
@param dataset the dataset from which to write control settings.
@param inputFile old file (used as input)
@param outputFile new control file to create
@param newcomments String array of new comments to add to the header of the
file.
@throws Exception if an error occurs.
*/
public static void writeStateModControlFile (	StateMod_DataSet dataset,
						String inputFile,
						String outputFile,
						String[] newcomments )
throws Exception
{	PrintWriter out = null;
	String [] comment_str = { "#" };
	String [] ignore_str = { "#>" };
	String routine = "writeControlFile";

	Message.printStatus(1, routine,
		"Writing new control to file \"" + outputFile 
		+ "\" using \"" + inputFile + "\" header...");

	try {	
	// Process the header from the old file...
	out = IOUtil.processFileHeaders(inputFile, outputFile,
		newcomments, comment_str, ignore_str, 0);

	// Now write the new control data...
	String month_del = "   ";
	String iline;
	String cmnt = "#>";
	String formatd = "%8d";
	String formatf = "%8.4f";
	String formatf1 = "%8.1f";
	String formatf0 = "%8.0f";
	String formats12 = "%-12.12s";
	Vector v = new Vector(1);

	if (dataset.getCyrl()== SM_CYR) {
		month_del = "CYR";
	}
	else if (dataset.getCyrl()== SM_WYR) {
		month_del = "WYR";
	}
	else if (dataset.getCyrl()== SM_IYR) {
		month_del = "IYR";
	}

	out.println(cmnt);
	out.println(cmnt
		+ " StateMod Control file(see StateMod documentation)");
	out.println(cmnt);
	out.println(cmnt
		+ " First 2 lines are headers(title, description)");
	out.println(cmnt
		+ " Remaining lines use first 8 characters for values.");
	out.println(cmnt);
	out.println(dataset.getHeading1());
	out.println(dataset.getHeading2());

	v.addElement(new Integer(dataset.getIystr()));
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : iystr   STARTING YEAR OF SIMULATION");

	v.setElementAt(new Integer(dataset.getIyend()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : iyend   ENDING YEAR OF SIMULATION");

	v.setElementAt(new Integer(dataset.getIresop()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : iresop  OUTPUT UNITS 1=cfs," +
		"2-acft,3=KAF,4=cfs day acft mon,5=cms");

	v.setElementAt(new Integer(dataset.getMoneva()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : moneva  TYPE OF EVAP/PRECIP. DATA. "
		+ "0=monthly, 1=average");

	v.setElementAt(new Integer(dataset.getIopflo()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : iopflo  TYPE OF STREAM INFLOW. "
		+ "1=Total, 2=Gains");

	v.setElementAt(new Integer(dataset.getNumpre()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : numpre  NO. OF PRECIPITATION STATIONS");

	v.setElementAt(new Integer(dataset.getNumeva()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : numeva  NO. OF EVAPORATION STATIONS");

	v.setElementAt(new Integer(dataset.getInterv()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : interv  INTERVALS IN DELAY"
		+ " TABLE n=fixed, %;-1=var, %; -100=var, dec.");

	v.setElementAt(new Double(dataset.getFactor()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : factor  FACTOR TO CONVERT CFS "
		+ "TO AC-FT/DAY (1.9835)");

	v.setElementAt(new Double(dataset.getRfacto()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : rfacto  DIVISOR FOR STREAM FLOW DATA."
		+ " 0 FOR DATA IN cfs, 1.9835 af/mo");

	v.setElementAt(new Double(dataset.getDfacto()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : dfacto  DIVISOR FOR DIVERSION DATA. " 
		+ "0 FOR DATA IN cfs, 1.9835 af/mo");

	v.setElementAt(new Double(dataset.getFfacto()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : ffacto  DIVISOR FOR INSTREAM FLOW DATA."
		+ " 0 FOR DATA IN cfs, 1.9835 af/mo");

	v.setElementAt(new Double(dataset.getCfacto()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : cfacto  FACTOR TO CONVERT RESERVOIR "
		+ "CONTENT TO AC-FT");

	v.setElementAt(new Double(dataset.getEfacto()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : efacto  FACTOR TO CONVERT EVAPORATION "
		+ "DATA TO FEET");

	v.setElementAt(new Double(dataset.getPfacto()), 0);
	iline = StringUtil.formatString(v, formatf);
	out.println(iline + " : pfacto  FACTOR TO CONVERT "
		+ "PRECIPITATION DATA TO FEET");

	out.println("  " + month_del
		+ "    : cyr1    YEAR TYPE (a5, All caps, "
		+ "right justified!) CYR, WYR, or IYR");

	v.setElementAt(new Integer(dataset.getIcondem()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : icondem Demand type. "
		+ "1=HistDem,2=HistSum,3=StrDem,4=SupDem,5=DecDem");

	v.setElementAt(new Integer(dataset.getIchk()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : ichk    Detailed print. "
		+ "0=off,1=net,4=calls,5=dem,6=day,7=ret,"
		+ "91=well,92=soil,-NodeId)");

	v.setElementAt(new Integer(dataset.getIreopx()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : ireopx  Reoperation. "
		+ "(0=reoperate,1=no reop,-n=reop for releases>n) ");

	v.setElementAt(new Integer(dataset.getIreach()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : ireach  ISF approach. 0=Phase II,"
		+ "1=Phase III,2=0+.ifm,3=1+.ifm");

	v.setElementAt(new Integer(dataset.getIcall()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : icall   Detailed call data. "
		+ "0=no, 1=yes");

	v.setElementAt(dataset.getCcall(), 0);
	iline = StringUtil.formatString(v, formats12);
	out.println(iline + "    :ccall Detailed call water right ID"
		+ " (if icall != 0)");

	v.setElementAt(new Integer(dataset.getIday()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : iday    Daily calculations. "
		+ "0=monthly, 1=daily");

	v.setElementAt(new Integer(dataset.getIwell()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : iwell   Wells "
		+ "-1=no,in .rsp;0=no;1=no gwmaxrc,2=gwmaxrc,3=var gwmaxrc");

	v.setElementAt(new Double(dataset.getGwmaxrc()), 0);
	iline = StringUtil.formatString(v, formatf1);
	out.println(iline + " : gwmaxrc Constanct max recharge, CFS"
		+ " (if iwell=2)");

	v.setElementAt(new Integer(dataset.getIsjrip()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : isjrip  (SJRIP) sediment file. "
		+ "-1=no but in .rsp, 0=no, 1=yes");

	v.setElementAt(new Integer(dataset.getItsfile()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : itsfile Use *.tsp file "
		+ "-1=no,in .rsp;0=no;1=annual GW lim,10=all data");

	v.setElementAt(new Integer(dataset.getIeffmax()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : ieffmax Use IWR file. "
		+ "-1=no but in .rsp, 0=no, 1=yes");

	v.setElementAt(new Integer(dataset.getIsprink()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : isprink Use sprinkler data. "
		+ "0=no, 1=yes");

	v.setElementAt(new Double(dataset.getSoild()), 0);
	if (dataset.getSoild()> 0.0) {
		iline = StringUtil.formatString(v, formatf1);
	}
	else {	
		iline = StringUtil.formatString(v, formatf0);
	}
	out.println(iline + " : soild   Soil moist acct. "
		+ "-1=no,.par in .rsp;0=no;+n=soil zone dep,ft");

	v.setElementAt(new Integer(dataset.getIsig()), 0);
	iline = StringUtil.formatString(v, formatd);
	out.println(iline + " : isig    0=none, 1=one, 2=two");

	out.flush();
	out.close();
	out = null;
	comment_str = null;
	ignore_str = null;
	routine = null;
	} 
	catch (Exception e) {
	if (out != null) {
		out.close();
	}
	out = null;
	comment_str = null;
	ignore_str = null;
	routine = null;
	Message.printWarning(2, routine, e);
	throw new IOException(e.getMessage());
	}
}

/**
Write data set information to the StateMod response file.  History header
information is also maintained by calling this routine.
@param instrfile input file from which previous history should be taken.
@param outstrfile output file to write.
@param newComments addition comments that should be included in history.
@param free_format If true, the response file will be written free-format.
If false, the old-style fixed-format will be used.
@exception Exception if an error occurs.
*/
public static void writeStateModFile (	StateMod_DataSet dataset,
					String instrfile,
					String outstrfile,
					String[] newComments,
					boolean free_format )
throws Exception {
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	try {	
	out = IOUtil.processFileHeaders(instrfile, outstrfile, 
		newComments, comment_str, ignore_comment_str, 0);

	String cmnt = "#>";
	DataSetComponent comp = null;
	if ( free_format ) {
		out.println(cmnt);
		out.println(cmnt + "  StateMod Response File");
		out.println(cmnt);
		out.println(cmnt + "  See other component write code like in "
			+ "StateMod_Diversion for heading information.");
		out.println(cmnt + "> The start of the comment indicates "
			+ "whether the comment can be disposed if the file "
			+ "is updated.");
		out.println(cmnt);

		for (int i = 0; i < __component_names.length; i++) {
			comp = dataset.getComponentForComponentType(i);	
			
			if (comp.isGroup()) {
				out.println(cmnt 
					+ dataset.getStateModFileProperty(i));
			}
			else {
				out.println(dataset.getStateModFileProperty(i) 
				+ " = "
				+ dataset.getDataFilePathRelative(i));
			}
		}
	}
	else {	// Write fixed-format response file...
		out.println(cmnt);
		out.println(cmnt + "  Response File");
		out.println(cmnt);
		// Header line...
		writeStateModFileHelper ( out,
			cmnt + " Name", "Description (optional)");
		writeStateModFileHelper ( out,
			cmnt + "________","______________________");
		// Control file...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_CONTROL );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// River network file...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_RIVER_NETWORK );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Reservoir station...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_STATIONS );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Diversion station...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_DIVERSION_STATIONS );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Stream Gage stations...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Instream flow stations...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_INSTREAM_STATIONS );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Well stations...
		if ( dataset.hasWellData ( false ) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_WELL_STATIONS );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Instream flow rights...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_INSTREAM_RIGHTS);
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Reservoir rights...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_RIGHTS);
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Diversion rights...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_DIVERSION_RIGHTS);
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Operational rights...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_OPERATION_RIGHTS );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Well rights...
		if ( dataset.hasWellData ( false ) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_WELL_RIGHTS );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Precipitation...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_PRECIPITATION_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Evaporation...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_EVAPORATION_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Base streamflow - file is shared with the StreamEstimate
		// stations so supply the comment manually...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), "Stream baseflow (Monthly)" );
		// Diversion demand (monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_DEMAND_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Diversion demand (override monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_DEMAND_TS_OVERRIDE_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Diversion demand (average monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.
			COMP_DEMAND_TS_AVERAGE_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Instream flow demand (Monthly)...
		if ( dataset.getIreach() >= 2 ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.
				COMP_INSTREAM_DEMAND_TS_MONTHLY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Instream flow demand (average monthly)...
		comp = dataset.getComponentForComponentType ( StateMod_DataSet.
			COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Well demands (monthly)...
		if ( dataset.hasWellData ( false ) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Delay table (monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Reservoir target (monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Reservoir EOM (monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Stream Estimate Coefficients...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// StreamGage historical flows...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY);
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Diversions historical (monthly)...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Well pumping (monthly)...
		if ( dataset.hasWellData ( false ) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// GIS...
		comp = dataset.getComponentForComponentType (
			StateMod_DataSet.COMP_GEOVIEW );
		writeStateModFileHelper ( out,
			comp.getDataFileName(), comp.getComponentName() );
		// Diversion demand (daily)...
		if ( dataset.hasDailyData(false) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_DEMAND_TS_DAILY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Instream demand (daily)...
		if ( dataset.hasDailyData(false) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.
				COMP_INSTREAM_DEMAND_TS_DAILY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Instream demand (daily)...
		if ( dataset.hasDailyData(false) && dataset.hasWellData(false)){
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_WELL_DEMAND_TS_DAILY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Reservoir target (daily)...
		if ( dataset.hasDailyData(false) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_DAILY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
		// Delay tables (daily)...
		if ( dataset.hasDailyData(false) ) {
			comp = dataset.getComponentForComponentType (
				StateMod_DataSet.COMP_DELAY_TABLES_DAILY );
			writeStateModFileHelper ( out,
				comp.getDataFileName(),comp.getComponentName());
		}
	}

	out.flush();
	out.close();
	out = null;
	comment_str = null;
	ignore_comment_str = null;
	} 
	catch (Exception e) {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
		comment_str = null;
		ignore_comment_str = null;
		throw e;
	}
}

/**
Method to help writeStateModFile() with repetitive formatting/output, for use
in the old-style fixed-format response file.
*/
private static void writeStateModFileHelper (	PrintWriter out,
						String filename, 
						String description )
{	String format = "%-73.73s%-24s";
	Vector v = new Vector(2);
	v.addElement(filename);
	v.addElement(description);
	out.println ( StringUtil.formatString ( v, format ) );
}

} // End StateMod_DataSet
