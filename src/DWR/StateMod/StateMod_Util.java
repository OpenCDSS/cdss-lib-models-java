//------------------------------------------------------------------------------
// StateMod_Util - Utility functions for statemod operation
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2003-07-02	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-30	Steven A. Malers, RTi	* Remove import for
//					  StateMod_DataSetComponnent.
//					* Remove static __basinName, which is
//					  the response file name without the
//					  .rsp.  StateMod now can take the name
//					  with or without the .rsp so just pass
//					  the response file name to the
//					  runStateMod() method.
//					* Change runStateModOption() to
//					  runStateMod() and pass the data set
//					  to the method.
//					* Make __statemod_version and
//					  __statemod_executable private and
//					  add set/get methods.
//					* Move remaining static methods from
//					  StateMod_Data.
//					* Alphabetize methods.
// 2003-08-21	SAM, RTi		* Add lookupTimeSeries() to simplify
//					  finding time series for components.
//					* Add createDataList() to help with
//					  choices, etc.
// 2003-08-25	SAM, RTi		Add getUpstreamNetworkNodes() from
//					old SMRiverInfo.retrieveUpstreams().
//					Change it to return data objects, not
//					strings.
// 2003-09-11	SAM, RTi		Update due to changes in the river
//					station component names.
// 2003-09-19	JTS, RTi		Added createCgotoDataList().
// 2003-09-24	SAM, RTi		* Change findEarliestPOR() to
//					  findEarliestDateInPOR().
// 					* Change findLatestPOR() to
//					  findLatestDateInPOR().
//					* Change the above methods to return
//					  null if no date can be found (e.g.,
//					  for a new data set).
// 2003-09-29	SAM, RTi		Add formatDataLabel().
// 2003-10-09	JTS, RTi		* Added removeFromVector().
//					* Added sortStateMod_DataVector().
// 2003-10-10	SAM, RTi		Add estimateDayTS ().
// 2003-10-24	SAM, RTi		Overload runStateMod() to take a 
//					StateMod_DataSet, so the response file
//					can be determined.
// 2003-10-29	SAM, RTi		* Change estimateDailyTS() to
//					  createDailyEstimateTS().
//					* Add createWaterRightTS().
// 2003-11-03	SAM, RTi		Change From_Well parameter to
//					From_River_By_Well.
// 2003-11-05	SAM, RTi		Got clarification from Ray Bennett on
//					which parameters should be listed for
//					output.
// 2003-11-14	SAM, RTi		Ray Bennett provided documentation for
//					the reservoir and well monthly binary
//					files as well as all the daily binary
//					files.  Therefore update the data type
//					lists, etc.
// 2003-11-29	SAM, RTi		In getTimeSeriesDataTypes(),
//					automatically turn off input types if
//					the request is for reservoirs and
//					the identifier has an account part.
// 2004-06-01	SAM, RTi		Update getTimeSeriesDataTypes() to have
//					a flag for data groups and use Ray
//					Bennett feedback for the groups.
// 2004-07-02	SAM, RTi		Add indexOfRiverNodeID().
// 2004-07-06	SAM, RTi		Overload sortStateMod_DataVector() to
//					allow option of creating new or using
//					existing data Vector.
// 2004-08-12	JTS, RTi		Added calculateTimeSeriesDifference().
// 2004-08-25	JTS, RTi		Removed the property that defined a
//					"HelpKey" for the dialog that runs 
//					StateMod.
// 2004-09-07	SAM, RTi		* Reordered some methods to be
//					  alphabetical.
//					* Add findWaterRightInsertPosition().
// 2004-09-14	SAM, RTi		For findWaterRightInsertPosition(), just
//					insert based on the right ID.
// 2004-10-05	SAM, RTi		* Add data type notes as per recent
//					  documentation (? are removed).
//					* Add River_Outflow for reservoir
//					  station output parameters.
// 2005-03-03	SAM, RTi		* Add compareFiles() to help with
//					  testing.
// 2005-04-01	SAM, RTi		* Add createTotalTimeSeries() method to
//					  facilitate summarizing information.
// 2005-04-05	SAM, RTi		* Add lookupTimeSeriesGraphTitle() to
//					  provide default titles based on the
//					  component type.
// 2005-04-18	JTS, RTi		Added the lookup methods.
// 2005-04-19	JTS, RTi		Removed testDirty().
// 2005-05-06	SAM, RTi		Correct a couple of typos in reservoir
//					subcomponent IDs in lookupPropValue().
// 2005-08-30	SAM, RTi		Add getTimeSeriesOutputPrecision().
// 2005-10-05	SAM, RTi		Handle well historical pumping time
//					series in createTotalTS().
// 2005-12-20	SAM, RTi		Add VERSION_XXX and isVersionAtLeast()
//					to help with binary file format
//					versions.
// 2006-01-15	SAM, RTi		Overload getTimeSeriesDataTypes() to
//					take the file name, to facilitate
//					reading the parameters from the newer
//					binary files.
// 2006-03-05	SAM, RTi		calculateTimeSeriesDifference() was
//					resulting in a division by zero, with
//					infinity values being returned.
// 2006-04-10	SAM, RTi		Add getRightsForStation(), which
//					extracts rights for an identifier.
// 2006-06-13	SAM, RTi		Add properties for downstream ID for
//					river network file.
// 2006-08-20	SAM, RTi		Move code to check for edits before
//					running to StateModGUI_JFrame.
// 2007-04-15	Kurt Tometich, RTi		Added some helper methods that
//								return validators for data checks.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.Collections;
import java.util.Vector;

import javax.swing.JFrame;

import DWR.StateMod.StateMod_DataSet;

import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
import RTi.TS.YearTS;

import RTi.Util.IO.DataFormat;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.Math.MathUtil;
import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.ProcessManagerJDialog;
import RTi.Util.IO.PropList;
import RTi.Util.IO.Validators;
import RTi.Util.IO.Validator;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
This class contains utility methods related to a StateMod data set.
*/
public class StateMod_Util
{

/**
Strings used when handling free water rights.
*/
public static String AlwaysOn = "AlwaysOn";
public static String UseSeniorRightAppropriationDate = "UseSeniorRightAppropriationDate";

public static String MISSING_STRING = "";
public static int MISSING_INT = -999;
public static float MISSING_FLOAT = (float)-999.0;
public static double MISSING_DOUBLE = -999.0;
private static double MISSING_DOUBLE_FLOOR = -999.1;
private static double MISSING_DOUBLE_CEILING = -998.9;
public static DateTime MISSING_DATE = null;

/**
Strings indicating binary file versions.  These are used by StateMod_BTS and
other software to determine appropriate parameters to list.
*/
public static final double VERSION_9_01 = 9.01;
public static final double VERSION_9_69 = 9.69;
public static final double VERSION_11_00 = 11.00;

/**
Strings for the station types.  These should be used in displays (e.g., graphing
tool) for consistency.  They can also be used to compare GUI values, rather than
hard-coding the literal strings.  Make sure that the following lists agree with
the StateMod_BTS file - currently the lists are redundant because StateMod_BTS
may be used independent of a data set.
*/
public static final String STATION_TYPE_DIVERSION = "Diversion";
public static final String STATION_TYPE_INSTREAM_FLOW = "Instream Flow";
public static final String STATION_TYPE_RESERVOIR = "Reservoir";
public static final String STATION_TYPE_STREAMESTIMATE =
						"Stream Estimate Station";
public static final String STATION_TYPE_STREAMGAGE = "Stream Gage Station";
public static final String STATION_TYPE_WELL = "Well";

/**
Used for looking up properties for data types which do not have separate 
components.
*/
public static final int 
	COMP_RESERVOIR_AREA_CAP = -102;

// Used by getStationTypes().
private static final String[] __station_types = {
	STATION_TYPE_DIVERSION,
	STATION_TYPE_INSTREAM_FLOW,
	STATION_TYPE_RESERVOIR,
	STATION_TYPE_STREAMESTIMATE,
	STATION_TYPE_STREAMGAGE,
	STATION_TYPE_WELL };

/**
The following arrays list the output time series data types for various station
types and various significant versions of StateMod.  These are taken from the
StateMod binary data file(s).  Time series should ultimately be read from the
following files (the StateMod_BTS class handles):
<pre>
Diversions      *.B43
Reservoirs      *.B44
Wells           *.B65
StreamGage      *.B43
StreamEstimate  *.B43
IntreamFlow     *.B43
</pre>
Use getTimeSeriesDataTypes() to get the list of parameters to use
for graphical interfaces, etc.  Important:  the lists are in the order of the
StateMod binary file parameters, with no gaps.  If the lists need to be
alphabetized, this should be done separately, not by reordering the arrays
below.
*/

/**
The stream station (stream gage and stream estimate) parameters are written to
the *.xdg file by StateMod's -report module.  The raw data are in the *.B43
(monthly) binary output file.
As per Ray Bennett 2003-11-05 email, all parameters are valid for output.
Include a group and remove the group later if necessary.
*/
private static final String[] __output_ts_data_types_stream_0100 = {
	"Station In/Out - UpstreamInflow",
	"Station In/Out - ReachGain",
	"Station In/Out - ReturnFlow",
	"Station Balance - RiverInflow",
	"Station Balance - RiverDivert",
	"Station Balance - RiverOutflow" };

private static final String[] __output_ts_data_types_stream_0901 = {
	"Station In/Out - UpstreamInflow",
	"Station In/Out - ReachGain",
	"Station In/Out - ReturnFlow",
	"Station Balance - RiverInflow",
	"Station Balance - RiverDivert",
	"Station Balance - RiverOutflow",
	"Available Flow - AvailableFlow" };

private static final String[] __output_ts_data_types_stream_0969 = {
	"Demand - Total_Demand",
	"Demand - CU_Demand",
	"Water Supply - From_River_By_Priority",
	"Water Supply - From_River_By_Storage",
	"Water Supply - From_River_By_Exchange",
	"Water Supply - From_River_By_Well",		// Prior to 2003-11-03
							// was From_Well
	"Water Supply - From_Carrier_By_Priority",
	"Water Supply - From_Carrier_By_Storage",
	"Water Supply - Carried_Water",
	"Water Supply - From_Soil",
	"Water Supply - Total_Supply",
	"Shortage - Total_Short",
	"Shortage - CU_Short",
	"Water Use - Consumptive_Use",
	"Water Use - To_Soil",
	"Water Use - Total_Return",
	"Water Use - Loss",
	"Station In/Out - Upstream_Inflow",
	"Station In/Out - Reach_Gain",
	"Station In/Out - Return_Flow",
	"Station In/Out - Well_Depletion",
	"Station In/Out - To_From_GW_Storage",
	"Station Balance - River_Inflow",
	"Station Balance - River_Divert",
	"Station Balance - River_By_Well",
	"Station Balance - River_Outflow",
	"Available Flow - Available_Flow" };

/**
The reservoir station parameters are written to
the *.xrg file by StateMod's -report module.  The raw monthly data are in the
*.B44 (monthly) binary output file.  The raw daily data are in the
*.B50 (daily) binary output file.
*/

private static final String[] __output_ts_data_types_reservoir_0100 = {
	"General - InitialStorage",
	"Supply From River by - RiverPriority",
	"Supply From River by - RiverStorage",
	"Supply From River by - RiverExchange",
	"Supply From Carrier by - CarrierPriority",
	"Supply From Carrier by - CarrierStorage",
	"Supply From Carrier by - TotalSupply",
	"Water Use from Storage to - StorageUse",
	"Water Use from Storage to - StorageExchange",
	"Water Use from Storage to - CarrierUse",
	"Water Use from Storage to - TotalRelease",
	"Other - Evap",
	"Other - SeepSpill",
	"Other - SimEOM",
	"Other - TargetLimit",
	"Other - FillLimit",
	"Station Balance - Inflow",
	"Station Balance - Outflow" };

private static final String[] __output_ts_data_types_reservoir_0901 = {
	"General - InitialStorage",
	"Supply From River by - RiverPriority",
	"Supply From River by - RiverStorage",
	"Supply From River by - RiverExchange",
	"Supply From Carrier by - CarrierPriority",
	"Supply From Carrier by - CarrierStorage",
	"Supply From Carrier by - TotalSupply",
	"Water Use from Storage to - StorageUse",
	"Water Use from Storage to - StorageExchange",
	"Water Use from Storage to - CarrierUse",
	"Water Use from Storage to - TotalRelease",
	"Other - Evap",
	"Other - SeepSpill",
	"Other - SimEOM",
	"Other - TargetLimit",
	"Other - FillLimit",
	"Station Balance - RiverInflow",
	"Station Balance - TotalRelease",
	"Station Balance - TotalSupply",
	"Station Balance - RiverByWell",
	"Station Balance - RiverOutflow" };

private static final String[] __output_ts_data_types_reservoir_0969 = {
	"General - Initial_Storage",
	"Supply From River by - River_Priority",
	"Supply From River by - River_Storage",
	"Supply From River by - River_Exchange",
	"Supply From Carrier by - Carrier_Priority",
	"Supply From Carrier by - Carrier_Storage",
	"Supply From Carrier by - Total_Supply",
	"Water Use from Storage to - Storage_Use",
	"Water Use from Storage to - Storage_Exchange",
	"Water Use from Storage to - Carrier_Use",
	"Water Use from Storage to - Total_Release",
	"Other - Evap",
	"Other - Seep_Spill",
	"Other - Sim_EOM",
	"Other - Target_Limit",
	"Other - Fill_Limit",
	"Station Balance - River_Inflow",
	"Station Balance - Total_Release",
	"Station Balance - Total_Supply",
	"Station Balance - River_By_Well",
	"Station Balance - River_Outflow" };

/**
The instream flow station parameters are written to
the *.xdg file by StateMod's -report module.  The raw monthly data are in the
*.B43 (monthly) binary output file.  The raw daily data are in the *.B49
(daily) binary output file.
As per Ray Bennett 2003-11-05 email, all parameters are valid for output.
*/

private static final String[] __output_ts_data_types_instream_0100 = {
	"Demand - ConsDemand",
	"Water Supply - FromRiverByPriority",
	"Water Supply - FromRiverByStorage",
	"Water Supply - FromRiverByExchange",
	"Water Supply - TotalSupply",
	"Shortage - Short",
	"Water Use - WaterUse,TotalReturn",
	"Station In/Out - UpstreamInflow",
	"Station In/Out - ReachGain",
	"Station In/Out - ReturnFlow",
	"Station Balance - RiverInflow",
	"Station Balance - RiverDivert",
	"Station Balance - RiverOutflow" };

private static final String[] __output_ts_data_types_instream_0901 = {
	"Demand - ConsDemand",
	"Water Supply - FromRiverByPriority",
	"Water Supply - FromRiverByStorage",
	"Water Supply - FromRiverByExchange",
	"Water Supply - TotalSupply",
	"Shortage - Short",
	"Water Use - WaterUse,TotalReturn",
	"Station In/Out - UpstreamInflow",
	"Station In/Out - ReachGain",
	"Station In/Out - ReturnFlow",
	"Station Balance - RiverInflow",
	"Station Balance - RiverDivert",
	"Station Balance - RiverOutflow",
	"Available Flow - AvailFlow" };

private static final String[] __output_ts_data_types_instream_0969 = {
	"Demand - Total_Demand",
	"Demand - CU_Demand",
	"Water Supply - From_River_By_Priority",
	"Water Supply - From_River_By_Storage",
	"Water Supply - From_River_By_Exchange",
	"Water Supply - From_River_By_Well",		// Prior to 2003-11-03
							// was From_Well
	"Water Supply - From_Carrier_By_Priority",
	"Water Supply - From_Carrier_By_Storage",
	"Water Supply - Carried_Water",
	"Water Supply - From_Soil",
	"Water Supply - Total_Supply",
	"Shortage - Total_Short",
	"Shortage - CU_Short",
	"Water Use - Consumptive_Use",
	"Water Use - To_Soil",
	"Water Use - Total_Return",
	"Water Use - Loss",
	"Station In/Out - Upstream_Inflow",
	"Station In/Out - Reach_Gain",
	"Station In/Out - Return_Flow",
	"Station In/Out - Well_Depletion",
	"Station In/Out - To_From_GW_Storage",
	"Station Balance - River_Inflow",
	"Station Balance - River_Divert",
	"Station Balance - River_By_Well",
	"Station Balance - River_Outflow",
	"Available Flow - Available_Flow" };

/**
The diversion station parameters are written to
the *.xdg file by StateMod's -report module.  The raw data are in the *.B43
(monthly) binary output file.
As per Ray Bennett 2003-11-05 email, all parameters are valid for output.
*/

private static final String[] __output_ts_data_types_diversion_0100 = {
	"Demand - ConsDemand",
	"Water Supply - FromRiverByPriority",
	"Water Supply - FromRiverByStorage",
	"Water Supply - FromRiverByExchange",
	"Water Supply - FromCarrierByPriority",
	"Water Supply - FromCarierByStorage",
	"Water Supply - CarriedWater",
	"Water Supply - TotalSupply",
	"Shortage - Short",
	"Water Use - ConsumptiveWaterUse",
	"Water Use - WaterUse,TotalReturn",
	"Station In/Out - UpstreamInflow",
	"Station In/Out - ReachGain",
	"Station In/Out - ReturnFlow",
	"Station Balance - RiverInflow",
	"Station Balance - RiverDivert",
	"Station Balance - RiverOutflow" };

private static final String[] __output_ts_data_types_diversion_0901 = {
	"Demand - ConsDemand",
	"Water Supply - FromRiverByPriority",
	"Water Supply - FromRiverByStorage",
	"Water Supply - FromRiverByExchange",
	"Water Supply - FromWell",
	"Water Supply - FromCarrierByPriority",
	"Water Supply - FromCarierByStorage",
	"Water Supply - CarriedWater",
	"Water Supply - TotalSupply",
	"Shortage - Short",
	"Water Use - ConsumptiveWaterUse",
	"Water Use - WaterUse,TotalReturn",
	"Station In/Out - UpstreamInflow",
	"Station In/Out - ReachGain",
	"Station In/Out - ReturnFlow",
	"Station In/Out - WellDepletion",
	"Station In/Out - To/FromGWStorage",
	"Station Balance - RiverInflow",
	"Station Balance - RiverDivert",
	"Station Balance - RiverByWell",
	"Station Balance - RiverOutflow",
	"Available Flow - AvailableFlow" };

private static final String[] __output_ts_data_types_diversion_0969 = {
	"Demand - Total_Demand",
	"Demand - CU_Demand",
	"Water Supply - From_River_By_Priority",
	"Water Supply - From_River_By_Storage",
	"Water Supply - From_River_By_Exchange",
	"Water Supply - From_River_By_Well",		// Prior to 2003-11-03
							// was From_Well
	"Water Supply - From_Carrier_By_Priority",
	"Water Supply - From_Carrier_By_Storage",
	"Water Supply - Carried_Water",
	"Water Supply - From_Soil",
	"Water Supply - Total_Supply",
	"Shortage - Total_Short",
	"Shortage - CU_Short",
	"Water Use - Consumptive_Use",
	"Water Use - To_Soil",
	"Water Use - Total_Return",
	"Water Use - Loss",
	"Station In/Out - Upstream_Inflow",
	"Station In/Out - Reach_Gain",
	"Station In/Out - Return_Flow",
	"Station In/Out - Well_Depletion",
	"Station In/Out - To_From_GW_Storage",
	"Station Balance - River_Inflow",
	"Station Balance - River_Divert",
	"Station Balance - River_By_Well",
	"Station Balance - River_Outflow",
	"Available Flow - Available_Flow" };

	// Ray Bennett says not to include these (2003-11-05 email) - they
	// are useful internally but users should not see...
	//"Divert_For_Instream_Flow",
	//"Divert_For_Power",
	//"Diversion_From_Carrier",
	// "N/A"
	// "Structure Type"
	// "Number of Structures at Node"
		
/**
The well station parameters are written to
the *.wdg file by StateMod's -report module.  The raw monthly data are in the
*.B42 (monthly) binary output file.  The raw daily data are in the *.B65 (daily)
binary output file.
*/

private static final String[] __output_ts_data_types_well_0901 = {
	"Demand - Demand",
	"Water Supply - FromWell",
	"Water Supply - FromOther",
	"Shortage - Short",
	"Water Use - ConsumptiveUse",
	"Water Use - Return",
	"Water Use - Loss",
	"Water Source - River",
	"Water Source - GWStor",
	"Water Source - Salvage" };

public static final String[] __output_ts_data_types_well_0969 =	{
	"Demand - Total_Demand",
	"Demand - CU_Demand",
	"Water Supply - From_Well",
	"Water Supply - From_SW",
	"Water Supply - From_Soil",
	"Water Supply - Total_Supply",
	"Shortage - Total_Short",
	"Shortage - CU_Short",
	"Water Use - Total_CU",
	"Water Use - To_Soil",
	"Water Use - Total_Return",
	"Water Use - Loss",
	"Water Use - Total_Use",
	"Water Source - From_River",
	"Water Source - To_From_GW_Storage",
	"Water Source - From_Salvage",
	"Water Source - From_Soil",
	"Water Source - Total_Source" };

/**
The version of statemod that is being run.  This is normally set at the
beginning of a StateMod GUI session by calling runStateMod ( ... "-version" ).
Then its value can be checked with getStateModVersion();
*/
private static double __statemod_version = 0.0;

/**
The latest known version is returned by getStateModVersionLatest() as a
default.  This is used by StateMod_BTS when requesting parameters.
*/
private static double __statemod_version_latest = 10.34;

/**
The program to use when running StateMod.  In general, this should just be the
program name and rely on the PATH to find.  However, a full path can be
specified to override the PATH.
*/
private static String __statemod_executable = "statemod";

/**
Turns an array of Strings into a Vector of Strings.
*/
public static Vector arrayToVector(String[] array) {
	Vector v = new Vector();
	
	if (array == null) {
		return v;
	}

	for (int i = 0; i < array.length; i++) {
		v.add(array[i]);
	}

	return v;
}

// REVISIST SAM 2004-09-07 JTS needs to javadoc.
public static double calculateTimeSeriesDifference(TS ts1, TS ts2,
boolean percent) 
throws Exception
{	// Loop through the time series and convert to yearly...  Do it the
	// brute force way right now...

	if (ts1 == null || ts2 == null) {
		return -999.0;
	}

	TS[] tsArray = new TS[2];
	tsArray[0] = ts1;
	tsArray[1] = ts2;

	DateTime dt1 = null;
	DateTime dt2 = null;
	double total = 0.0;
	double value = 0.0;
	int count = 0;
	TSIdent tsident = null;
	YearTS yts = null;
	YearTS[] ytsArray = new YearTS[2];

	for (int its = 0; its < 2; its++) {
		if (tsArray[its].getDataIntervalBase() == TimeInterval.YEAR) {
			// Just add to the list...
			yts = (YearTS)tsArray[its];
		}
		else if (tsArray[its].getDataIntervalBase() 
			== TimeInterval.MONTH) {
			// Create a new time series and accumulate...
			tsident = new TSIdent(tsArray[its].getIdentifier());
			tsident.setInterval("Year");
			yts = new YearTS();
			yts.setIdentifier(tsident);
			yts.setDescription(tsArray[its].getDescription());
			yts.setDate1(tsArray[its].getDate1());
			yts.setDate2(tsArray[its].getDate2());
			yts.setDataUnits(tsArray[its].getDataUnits());
			yts.allocateDataSpace();
			dt1 = new DateTime(tsArray[its].getDate1());
			// Accumulate in calendar time...
			dt1.setMonth(1);
			dt2 = tsArray[its].getDate2();
			dt2.setMonth(12);
			for (; dt1.lessThanOrEqualTo(dt2);
				dt1.addMonth(1)) {
				value = tsArray[its].getDataValue(dt1);
				if (!tsArray[its].isDataMissing(value)) {
					total += value;
					++count;
				}
				if (dt1.getMonth() == 12) {
					// Transfer to year time series only if
					// all data are available in month...
					if (count == 12) {
						yts.setDataValue(dt1, total);
					}
					// Reset the accumulators...
					total = 0.0;
					count = 0;
				}
			}
		}

		// Add to the list...
		ytsArray[its] = yts;
	}

	dt1 = new DateTime(ytsArray[0].getDate1());
	if (ytsArray[1].getDate1().lessThan(dt1)) {
		dt1 = new DateTime(ytsArray[1].getDate1());
	}
	dt2 = new DateTime(ytsArray[0].getDate2());
	if (ytsArray[1].getDate2().greaterThan(dt2)) {
		dt2 = new DateTime(ytsArray[1].getDate2());
	}


	count = 0;
	double total1 = 0;
	double total2 = 0;
	double value1 = 0;
	double value2 = 0;
	while (dt1.lessThanOrEqualTo(dt2)) {
		value1 = ytsArray[0].getDataValue(dt1);
		value2 = ytsArray[1].getDataValue(dt1);
		if (!ytsArray[0].isDataMissing(value1)
			&& !ytsArray[1].isDataMissing(value2)) {
			total1 += value1;
			total2 += value2;
			count++;
		}
		dt1.addYear(1);
	}

	double result = -999.0;	// Unable to calculate
	if ( count > 0 ) {
		result = (total1 - total2) / count;

		if (percent) {	
			if ( total1 != 0.0 ) {
				result = (result / total1) * 100;
			}
		}
	}

	return result;
}

/**
Fills in missing values with a space.  This is needed for the HTML table
to show a blank value and not an empty cell.
@param String[] data - The array of data to check.
@return String[] - Formatted data array.
 */
public static String[] checkForMissingValues( String[] data )
{
	if( data != null)
	{
		for( int i = 0; i < data.length; i++ ) {
			String element = data[i].trim();
			if( element == null || element.length() == 0 ) {
				data[i] = " ";
			}
			else {
				data[i] = element;
			}
		}
	}
	return data;
}


// REVISIT SAM 2005-03-03 This simple test needs to be evaluated to determine
// if it should be supported in all the file types.  For example, this code
// could be moved to each StateMod class.
/**
Compare similar StateMod files and generate a summary of the files, with
differences.
@param path1 Path to first file.
@param path2 Path to second file.
@param comp_type Component type.
@exception Exception if an error is generated.
*/
public static Vector compareFiles ( String path1, String path2, int comp_type )
throws Exception
{	Vector v = new Vector(50);
	Vector data1_Vector, data2_Vector;	// Data to compare
	String full_path1 = IOUtil.getPathUsingWorkingDir ( path1 );
	String full_path2 = IOUtil.getPathUsingWorkingDir ( path2 );
	int n1, n2;				// Size of data vectors
	int i, pos, size;
	StringBuffer b = new StringBuffer();
	if ( comp_type == StateMod_DataSet.COMP_WELL_RIGHTS ) {
		StateMod_WellRight wer1, wer2;
		data1_Vector = StateMod_WellRight.readStateModFile(full_path1 );
		n1 = data1_Vector.size();
		data2_Vector = StateMod_WellRight.readStateModFile(full_path2 );
		n2 = data2_Vector.size();
		Vector allids_Vector = new Vector(n1*3/2);	// guess at size
		double decree;
		double decree1_alltotal = 0.0;	// All decrees in file
		double decree2_alltotal = 0.0;
		int missing_n1 = 0;
		int missing_n2 = 0;
		Vector onlyin1_Vector = new Vector();
		Vector onlyin2_Vector = new Vector();
		// Get a list of all identifiers.  This is used to summarize
		// results by location...
		for ( i = 0; i < n1; i++ ) {
			// Add to list of all identifiers...
			wer1 = (StateMod_WellRight)data1_Vector.elementAt(i);
			allids_Vector.addElement ( wer1.getCgoto() );
		}
		for ( i = 0; i < n2; i++ ) {
			// Add to list of all identifiers...
			wer2 = (StateMod_WellRight)data2_Vector.elementAt(i);
			allids_Vector.addElement ( wer2.getCgoto() );
		}
		// Sort all the identifiers...
		allids_Vector = StringUtil.sortStringList (
			allids_Vector, StringUtil.SORT_ASCENDING, null,
			false, true );
		Message.printStatus ( 1, "", "Count after sort= " +
			allids_Vector.size() );
		// Remove the duplicates...
		StringUtil.removeDuplicates ( allids_Vector, true, true );
		Message.printStatus ( 1, "","Count after removing duplicates= "+
			allids_Vector.size() );
		int nall = allids_Vector.size();
		// Initialize the totals for each location...
		double [] decree1_total = new double[nall];
		double [] decree2_total = new double[nall];
		for ( i = 0; i < nall; i++ ) {
			decree1_total[i] = -999.0;
			decree2_total[i] = -999.0;
		}

		// Now process each list...

		for ( i = 0; i < n1; i++ ) {
			wer1 = (StateMod_WellRight)data1_Vector.elementAt(i);
			decree = wer1.getDcrdivw();
			if ( StateMod_Util.isMissing( decree ) ) {
				++missing_n1;
			}
			else {	// Data set...
				decree1_alltotal += decree;
				// By structure...
				pos = StringUtil.indexOf (
					allids_Vector, wer1.getCgoto() );
				if ( pos >= 0 ) {
					if ( decree1_total[pos] < 0.0 ) {
						decree1_total[pos] = decree;
					}
					else { decree1_total[pos] += decree;
					}
				}
			}
			// Search other list...
			pos = StateMod_Util.indexOf ( data2_Vector,
				wer1.getID() );
			if ( pos < 0 ) {
				onlyin1_Vector.addElement ( wer1 );
			}
		}
		for ( i = 0; i < n2; i++ ) {
			wer2 = (StateMod_WellRight)data2_Vector.elementAt(i);
			decree = wer2.getDcrdivw();
			if ( StateMod_Util.isMissing( decree ) ) {
				++missing_n2;
			}
			else {	// By data set...
				decree2_alltotal += decree;
				// By structure...
				pos = StringUtil.indexOf (
					allids_Vector, wer2.getCgoto() );
				if ( pos >= 0 ) {
					if ( decree2_total[pos] < 0.0 ) {
						decree2_total[pos] = decree;
					}
					else { decree2_total[pos] += decree;
					}
				}
			}
			// Search other list...
			pos = StateMod_Util.indexOf ( data1_Vector,
				wer2.getID() );
			if ( pos < 0 ) {
				onlyin2_Vector.addElement ( wer2 );
			}
		}

		// Now print the results...

		v.addElement ( "First file:            " + full_path1 );
		v.addElement ( "Number of rights:      " + n1 );
		v.addElement ( "Total decrees (CFS):   " +
			StringUtil.formatString(decree1_alltotal,"%.2f") );
		v.addElement ( "Second file:           " + full_path2 );
		v.addElement ( "Number of rights:      " + n2 );
		v.addElement ( "Total decrees (CFS):   " +
			StringUtil.formatString(decree2_alltotal,"%.2f") );
		v.addElement ( "" );
		v.addElement ( "Summary of decree differences, by location:" );
		v.addElement ( "" );
		v.addElement (
		"Well ID      | File2 Total | File1 Total | File2-File1" );
		for ( i = 0; i < nall; i++ ) {
			b.setLength(0);
			b.append ( StringUtil.formatString(
				allids_Vector.elementAt(i),"%-12.12s") + " | ");
			if ( decree2_total[i] >= 0.0 ) {
				b.append ( StringUtil.formatString(
				decree2_total[i],"%11.2f") + " | " );
			}
			else {	b.append ( "            | " );
			}
			if ( decree1_total[i] >= 0.0 ) {
				b.append ( StringUtil.formatString(
				decree1_total[i],"%11.2f") + " | " );
			}
			else {	b.append ( "            | " );
			}
			if (	(decree1_total[i] >= 0.0) &&
				(decree2_total[i] >= 0.0) ) {
				b.append ( StringUtil.formatString(
				(decree2_total[i] -
				decree1_total[i]),"%11.2f") );
			}
			else {	b.append ( "           " );
			}
			v.addElement ( b.toString() );
		}
		v.addElement ( "Total        | " +
			StringUtil.formatString (decree2_alltotal,"%11.2f") +
			" | " +
			StringUtil.formatString (decree1_alltotal,"%11.2f") +
			" | " +
			StringUtil.formatString (
				(decree2_alltotal-decree1_alltotal),"%11.2f") );
		v.addElement ( "" );
		v.addElement ( "First file:" );
		v.addElement ( "" );
		v.addElement ( "Rights with no decree: " + missing_n1 );
		size = onlyin1_Vector.size();
		if ( size == 0 ) {
			v.addElement ( "Rights only in first file:" );
			v.addElement ( "All are found in 2nd file." );
		}
		else {	v.addElement ( "Rights only in first file (" +
				size + " total):" );
			v.addElement ("    ID           Decree    AdminNumber");
			for ( i = 0; i < size; i++ ) {
				wer1 = (StateMod_WellRight)
					onlyin1_Vector.elementAt(i);
				v.addElement (
					StringUtil.formatString(
					wer1.getID(),"%-12.12s") +
					" " + StringUtil.formatString(
					wer1.getDcrdivw(),"%11.2f") + " " +
					wer1.getIrtem() );
			}
		}
		v.addElement ( "" );
		v.addElement ( "Second file:" );
		v.addElement ( "" );
		v.addElement ( "Rights with no decree: " + missing_n2 );
		size = onlyin2_Vector.size();
		if ( size == 0 ) {
			v.addElement ( "Rights only in second file:" );
			v.addElement ( "All are found in 1st file." );
		}
		else {	v.addElement ( "Rights only in second file (" +
				size + " total):" );
			v.addElement ("    ID           Decree    AdminNumber");
			for ( i = 0; i < size; i++ ) {
				wer2 = (StateMod_WellRight)
					onlyin2_Vector.elementAt(i);
				v.addElement (
					StringUtil.formatString(
					wer2.getID(),"%-12.12s") +
					" " + StringUtil.formatString(
					wer2.getDcrdivw(),"%11.2f") + " " +
					wer2.getIrtem() );
			}
		}
	}
	return v;
}

/**
Create a label for a single data objects, for use in choices, etc.
@return a String containing formatted identifiers and names.
@param smdata A single StateMod_Data object.
@param include_name If false, the string will consist of only the value
returned from StateMod_Data.getID().  If true the string will contain the ID,
followed by " - xxxx", where xxxx is the value returned from
StateMod_Data.getName().  If the identifier and name are the same only one
part will be returned.
*/
public static String createDataLabel (	StateMod_Data smdata,
					boolean include_name )
{	if ( smdata == null ) {
		return "";
	}
	String id = "", name = "";
	id = smdata.getID();
	name = smdata.getName();
	if ( id.equalsIgnoreCase(name) ) {
		return id;
	}
	else if ( include_name ) {
		return id + " - " + name;
	}
	else {	return id;
	}
}

/**
Create a list of data objects, for use in choices, etc -- this method differs
from createDataList in that it contains the Cgoto instead of the ID.
@return a Vector of String containing formatted identifiers and names.  A
non-null list is guaranteed; however, the list may have zero items.
@param include_name If false, each string will consist of only the value
returned from StateMod_Data.getID().  If true the string will contain the ID,
followed by " - xxxx", where xxxx is the value returned from
StateMod_Data.getName().
*/
public static Vector createCgotoDataList (	Vector smdata_Vector,
						boolean include_name )
{	Vector v = null;
	if ( smdata_Vector == null ) {
		return new Vector();
	}
	else {	// This optimizes memory management...
		v = new Vector ( smdata_Vector.size() );
	}
	int size = smdata_Vector.size();
	StateMod_Data smdata;
	String cgoto = "", name = "";
	TS ts;
	Object o;
	for ( int i = 0; i < size; i++ ) {
		o = smdata_Vector.elementAt(i);
		if ( o == null ) {
			continue;
		}
		if ( o instanceof StateMod_Data ) {
			smdata = (StateMod_Data)o;
			cgoto = smdata.getCgoto();
			name = smdata.getName();
		}
		else if ( o instanceof TS ) {
			ts = (TS)o;
			cgoto = ts.getLocation();
			name = ts.getDescription();
		}
		else {	Message.printWarning ( 2,"StateMod_Util.createDataList",
			"Unrecognized StateMod data." );
		}
		if ( cgoto.equalsIgnoreCase(name) ) {
			v.addElement ( cgoto );
		}
		else if ( include_name ) {
			v.addElement ( cgoto + " - " + name );
		}
		else {	v.addElement ( cgoto );
		}
	}
	return v;
}

/**
Create a daily estimate a time series, for viewing only.  The time series
should match the estimate that StateMod will make at run time.
@return a new time series containing the daily estimate, or null if the
estimate cannot be created.  The period of the daily time series will be that
of the monthly time series.
@param id Identifier (location) for the time series.
@param desc Description for the time series.
@param datatype Data type for the time series.
@param units Data units for the time series.
@param dayflag The daily ID flag from diversion stations, etc.  The daily time
series is estimated using one of the following methods:
<ol>
<li>	A value of "0" will estimate an average daily CFS value from the monthly
	ACFT by assuming 30 days in the month.</li>
<li>	A value of "4" will estimate a daily CFS time series by interpolating
	from the mid-points of the monthly time series (day 15).</li>
<li>	Other values indicate that the monthly total time series should be used
	to get the ACFT total for the month and the daily values should use the
	pattern in the supplied daily time series to generate a new daily time
	series.<li>
</ol>
@param monthts The monthly time series needed for estimation.
@param dayts The daily time series needed for estimation.
*/
public static DayTS createDailyEstimateTS (	String id, String desc,
						String datatype, String units,
						String dayflag, MonthTS monthts,
						DayTS dayts )
{	DayTS estdayts = null;

	String tsid = id + ".StateMod." + datatype + ".Day.Estimated";
	DateTime date1;
	DateTime date2;
	if ( dayflag.equals("0") ) {
		// Convert the monthly time series to daily by assuming the
		// monthly value is the average daily value...
		if ( monthts == null ) {
			return null;
		}
		try {	estdayts = (DayTS)TSUtil.newTimeSeries ( tsid, true );
			estdayts.setIdentifier ( tsid );
		}
		catch ( Exception e ) {
			// Should not occur.
		}
		estdayts.setDescription ( desc );
		estdayts.setDataUnits ( units );
		date1 = new DateTime(monthts.getDate1 ());
		date1.setPrecision ( DateTime.PRECISION_DAY );
		date1.setDay ( 1 );
		date2 = new DateTime(monthts.getDate2 ());
		date2.setPrecision ( DateTime.PRECISION_DAY );
		date2.setDay ( TimeUtil.numDaysInMonth(date2) );
		estdayts.setDate1 ( date1 );
		estdayts.setDate1 ( date1 );
		estdayts.setDate2 ( date2 );
		estdayts.setDate2 ( date2 );
		if ( estdayts.allocateDataSpace () != 0 ) {
			return null;
		}
		// Iterate based on the daily data and grab data from the
		// monthly when day = 1...
		double value = 0.0;
		int ndays_in_month;
		for (	DateTime date = new DateTime (date1);
			date.lessThanOrEqualTo(date2);
			date.addInterval(TimeInterval.DAY,1) ) {
			if ( date.getDay() == 1 ) {
				// Get a new value from the monthly time
				// series...
				value = monthts.getDataValue ( date );
				if ( !monthts.isDataMissing(value) ) {
					ndays_in_month =
					TimeUtil.numDaysInMonth(date);
					if ( units.equalsIgnoreCase("cfs") ) {
						// Monthlies are always ACFT so
						// convert to CFS...
						// (ACFT/daysInMonth)
						// (43560FT2/AC)
						// (1day/86400s).
						value = value/
							(1.9835*ndays_in_month);
					}
					// Else leave value as the monthly
					// value for assignment.
				}
			}
			estdayts.setDataValue ( date, value );
		}
		if ( units.equalsIgnoreCase("cfs") ) {
			estdayts.addToGenesis ( "Daily data were estimated " +
			"by using monthly data as average" +
			" daily values (divide by 1.9835*DaysInMonth)." );
		}
		else {	estdayts.addToGenesis ( "Daily data were estimated " +
			"by using monthly data as average" +
			" daily values (Assign monthly value to daily)." );
		}
	}
	else if ( dayflag.equals("4") ) {
		// Convert the monthly time series to daily by interpolating
		// the midpoint values.
		if ( monthts == null ) {
			return null;
		}
		try {	estdayts = (DayTS)TSUtil.newTimeSeries ( tsid, true );
			estdayts.setIdentifier ( tsid );
		}
		catch ( Exception e ) {
			// Should not occur.
		}
		estdayts.setDescription ( desc );
		estdayts.setDataUnits ( units );
		date1 = new DateTime(monthts.getDate1 ());
		date1.setPrecision ( DateTime.PRECISION_DAY );
		date1.setDay ( 1 );
		date2 = new DateTime(monthts.getDate2 ());
		date2.setPrecision ( DateTime.PRECISION_DAY );
		date2.setDay ( TimeUtil.numDaysInMonth(date2) );
		estdayts.setDate1 ( date1 );
		estdayts.setDate1 ( date1 );
		estdayts.setDate2 ( date2 );
		estdayts.setDate2 ( date2 );
		if ( estdayts.allocateDataSpace () != 0 ) {
			return null;
		}
		// Iterate based on the daily data and grab data from the
		// monthly when day = 1...
		double value = 0.0;
		int ndays_in_month;
		for (	DateTime date = new DateTime (date1);
			date.lessThanOrEqualTo(date2);
			date.addInterval(TimeInterval.DAY,1) ) {
			if ( date.getDay() == 1 ) {
				// Get a new value from the monthly time
				// series...
				value = monthts.getDataValue ( date );
				if ( !monthts.isDataMissing(value) ) {
					ndays_in_month =
					TimeUtil.numDaysInMonth(date);
					if ( units.equalsIgnoreCase("cfs") ) {
						// Monthlies are always ACFT so
						// convert to CFS...
						// (ACFT/daysInMonth)
						// (43560FT2/AC)
						// (1day/86400s).
						value = value/
							(1.9835*ndays_in_month);
					}
					// Else leave value as the monthly
					// value for assignment.
				}
			}
			estdayts.setDataValue ( date, value );
		}
		if ( units.equalsIgnoreCase("cfs") ) {
			estdayts.addToGenesis ( "Daily data were estimated " +
			"by using monthly data as average" +
			" daily values (divide by 1.9835*DaysInMonth)." );
		}
		else {	estdayts.addToGenesis ( "Daily data were estimated " +
			"by using monthly data as average" +
			" daily values (Assign monthly value to daily)." );
		}
	}
	return estdayts;
}

/**
Create a sum of the time series in a Vector, representing the total water for
a data set.  This time series can be used for summaries.
The period of the time series is the maximum of the time series in the list.
The total value will be set if one or more values in the parts is available.  If
no value is available, the total will be missing.
@return a time series that is the sum of all the time series in the input
Vector.
@param tslist a Vector of time series to process.
@param dataset_location the location to use for the total time series.
@param dataset_datasource the data source to use for the total time series.
@param dataset_location the description to use for the total time series.
@param comp_type Component type, used to determine the units and other
information for the new time series.
@exception Exception if there is an error creating the time series.
*/
public static TS createTotalTS (	Vector tslist, String dataset_location,
					String dataset_datasource,
					String dataset_description,
					int comp_type )
throws Exception
{	String routine = "StateMod_Util.createTotalTS";
	TS newts = null;
	String interval = "";
	if (	(comp_type == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) ||
		(comp_type == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY) ||
		(comp_type == StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY) ||
		(comp_type == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY) ) {
		newts = new MonthTS();
		interval = "Month";
	}
	else if((comp_type == StateMod_DataSet.COMP_DIVERSION_TS_DAILY) ||
		(comp_type == StateMod_DataSet.COMP_DEMAND_TS_DAILY) ||
		(comp_type == StateMod_DataSet.COMP_WELL_PUMPING_TS_DAILY) ||
		(comp_type == StateMod_DataSet.COMP_WELL_DEMAND_TS_DAILY) ) {
		newts = new DayTS();
		interval = "Day";
	}
	else {	Message.printWarning ( 3, routine,
		"Cannot create total - cannot handle component type " +
		comp_type );
		throw new Exception ( "Cannot create total TS." );
	}
	if ( (dataset_location == null) || (dataset_location.length()==0) ) {
		dataset_location = "DataSet";
	}
	if ( (dataset_datasource == null) || (dataset_datasource.length()==0) ){
		dataset_datasource = "StateMod";
	}
	if ( (dataset_description == null) ||(dataset_description.length()==0)){
		dataset_description = "Dataset " +
			StateMod_DataSet.lookupTimeSeriesDataType(comp_type);
	}
	newts.setIdentifier (
		new TSIdent(	dataset_location, dataset_datasource,
				StateMod_DataSet.
				lookupTimeSeriesDataType(comp_type),
				interval, "" ) );
	newts.setDataUnits (
		StateMod_DataSet.lookupTimeSeriesDataUnits(comp_type));
	// Get the period for the total time series...
	// Try to get from the data...
	TSLimits valid_dates = TSUtil.getPeriodFromTS ( tslist, TSUtil.MAX_POR);
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	if ( (start == null) || (end == null) ) {
		Message.printStatus ( 3,routine,"Cannot get period from data.");
		throw new Exception ( "Cannot create total TS." );
	}
	newts.setDate1 ( start );
	newts.setDate1Original ( start );
	newts.setDate2 ( end );
	newts.setDate2Original ( end );
	newts.allocateDataSpace ();
	TSUtil.add ( newts, tslist );
	// Reset the description to something short...
	newts.setDescription ( dataset_description );
	return newts;
}

/**
Create a long time series for the average monthly time series.  This time
series can be used for displays but should not be edited.  It is assumed that
the input time series is 12 months long.  Therefore, the repeating time series
is created by allocating a time series for the requested period and setting
all January values to the January value of ts, etc.  All of the header
information is retained (description, etc.).
@return a longer time series consisting of repeating the 12 values in the
original time series.
@param date1 Start date for long time series.
@param date2 End date for long time series.
*/
public static MonthTS createRepeatingAverageMonthTS (	TS ts, DateTime date1,
							DateTime date2 )
{	MonthTS newts = new MonthTS();
	try {	newts.setIdentifier ( new TSIdent(ts.getIdentifier()) );
	}
	catch ( Exception e ) {
		// Should not happen.
	}
	newts.setDataUnits ( ts.getDataUnits() );
	newts.setDate1 ( date1 );
	newts.setDate2 ( date2 );
	newts.allocateDataSpace ();
	// Get the old data...
	double [] ts_data = new double[12];	// Data [0] = january
	DateTime date = new DateTime ( ts.getDate1() );
	for ( int i = 0; i < 0; i++, date.addMonth(1) ) {
		ts_data[date.getMonth() - 1] = ts.getDataValue(date);
	}
	// Fill the data in the new time series...
	date = new DateTime ( date1 );
	for ( ; date.lessThanOrEqualTo(date2); date.addMonth(1) ) {
		newts.setDataValue ( date, ts_data[date.getMonth() - 1] );
	}
	return newts;
}

/**
Create a water right time series, in which each interval has a value of the
total water rights in effect at the time.  Switches are considered.  This method
can be used when processing rights for a single structure (e.g., when plotting
rights in the StateMod GUI).
@param smdata A StateMod data object (e.g., StateMod_Diversion) that the time
series is being created for.
@param interval TimeInterval.MONTH or TimeInterval.DAY.
@param units Data units for the time series.
@param date1 Starting date for the time series.  Must not be null.
@param date2 Ending date for the time series.  Must not be null.
*/
public static TS createWaterRightTS (	StateMod_Data smdata,
					int interval, String units,
					DateTime date1, DateTime date2)
{	TS ts = null;
	String tsid = null;
	if ( interval == TimeInterval.MONTH ) {
		tsid = smdata.getID() + ".StateMod.TotalWaterRights.Month";
		try {	ts = (MonthTS)TSUtil.newTimeSeries ( tsid, true );
			ts.setIdentifier ( tsid );
		}
		catch ( Exception e ) {
			// Should not occur.
		}
	}
	else {	tsid = smdata.getID() + ".StateMod.TotalWaterRights.Day";
		try {	ts = (DayTS)TSUtil.newTimeSeries ( tsid, true );
			ts.setIdentifier ( tsid );
		}
		catch ( Exception e ) {
			// Should not occur.
		}
	}
	ts.setDescription ( smdata.getName() );
	ts.setDataUnits ( units );
	ts.setDate1 ( date1 );
	ts.setDate1Original ( date1 );
	ts.setDate2 ( date2 );
	ts.setDate2Original ( date2 );
	// Initialize to zero...
	if ( interval == TimeInterval.MONTH ) {
		if ( ((MonthTS)ts).allocateDataSpace ( 0.0 ) != 0 ) {
			return null;
		}
	}
	else {	if ( ((DayTS)ts).allocateDataSpace ( 0.0 ) != 0 ) {
			return null;
		}
	}

	// Loop through each water right...

	int size = 0;
	int onoff = 0;
	double decree = 0.0;
	StateMod_DiversionRight dright = null;
	StateMod_Diversion div = null;
	DateTime fill_date1 = null;	// Dates to fill the water right time
	DateTime fill_date2 = null;	// series.
	if ( smdata instanceof StateMod_Diversion ) {
		div = (StateMod_Diversion)smdata;
		size = div.getRights().size();
	}
	for ( int i = 0; i < size; i++ ) {
		if ( smdata instanceof StateMod_Diversion ) {
			dright = div.getRight ( i );
			onoff = dright.getSwitch();
			decree = dright.getDcrdiv();
		}
		if ( onoff == 0 ) {
			// Not on...
			continue;
		}
		else if ( onoff != 0 ) {
			// Turn on for the full period...
			if ( onoff == 1 ) {
				fill_date1 = date1;
				fill_date2 = date2;
			}
			else if ( onoff > 1 ) {
				// Turn on starting in the given year...
				fill_date1 = new DateTime();
				fill_date1.setYear ( onoff );
				fill_date2 = date2;
			}
			else if ( onoff < 0 ) {
				// On at begining but off in a given year...
				fill_date1 = date1;
				fill_date2 = new DateTime();
				fill_date2.setYear ( onoff );
				// TODO SAM 2007-03-01 Evaluate logic
				//decree = (decree);
			}
		}
		if ( interval == TimeInterval.MONTH ) {
			for (	DateTime date = new DateTime (fill_date1);
				date.lessThanOrEqualTo(fill_date2);
				date.addInterval(TimeInterval.MONTH,1) ) {
				// Monthlies are always ACFT so
				// convert from CFS...
				// (ACFT/daysInMonth)
				// (43560FT2/AC)
				// (1day/86400s).
				ts.setDataValue ( date,
					ts.getDataValue(date) + 
					decree*(1.9835*
					TimeUtil.numDaysInMonth(date)) );
			}
		}
		else if ( interval == TimeInterval.DAY ) {
			for (	DateTime date = new DateTime (fill_date1);
				date.lessThanOrEqualTo(fill_date2);
				date.addInterval(TimeInterval.DAY,1) ) {
				// Dailies are always CFS...
				ts.setDataValue ( date,
					ts.getDataValue(date) + decree );
			}
		}
	}
	return ts;
}

/**
Create a list of time series from a list of water rights.  A non-null list
is guaranteed.
@param smrights A Vector of StateMod_Right.
@param interval_base Time series interval for returned time series, either
TimeInterval.DAY, TimeInterval.MONTH, TimeInterval.YEAR, or TimeInterval.IRREGULAR
(smaller interval is slower).
@param spatial_aggregation If 0, create a time series for the location,
which is the sum of the water rights at that location.  If 1, time series will
be created by parcel (requires parcel information in right - only for well rights).
If 2, individual
time series will be created (essentially step functions with one step).
@parcel_year If spatial_aggregation = 1, include the year to specify the years
for parcel indentifiers.
@param include_dataset_totals If true, create a time series including a total
of all time series.
@param start Start DateTime for the time series.  If not specified, the date
corresponding to the first right for a location will be used.
@param end End DateTime for the time series.  If not specified, the date
corresponding to the last right for a location will be used.
@param FreeWaterAdministrationNumber_double A value >= to this is considered a free
water right and will be handled as per FreeWaterMethod.  Specify a number larger
than 99999.99999 to avoid adjusting for free water rights.
@param FreeWaterMethod If null, handle the right as any other right, generally
meaning that the decree in the time series will take effect in the future.
If UseSeniorRightApropriationDate, use the appropriation date of the
senior right for the location.
If AlwaysOn, use the earliest available date specified by
"start" or that of data for the free water right appropriation date.
@param FreeWaterAppropriationDate_DateTime A DateTime that is used for the appropriation
date if free water and FreeWaterMethod=UseSpecifiedDate.
@param process_data If true, process the time series data.  If false, only
create the time series header information.
@return a list of time series created from a list of water rights.
@exception Exception if there is an error
*/
public static Vector createWaterRightTimeSeriesList ( Vector smrights,
		int interval_base, int spatial_aggregation, int parcel_year,
		boolean include_dataset_totals,
		DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime,
		double FreeWaterAdministrationNumber_double,
		String FreeWaterMethod,
		DateTime FreeWaterAppropriationDate_DateTime,
		boolean process_data )
throws Exception
{	String routine = "StateMod_Util.createWaterRightTimeSeriesList";
	Message.printStatus ( 2, routine, "Creating time series of water rights for requested period " +
			OutputStart_DateTime + " to " + OutputEnd_DateTime );	
	int size = 0;
	// Spatial aggregation values
	int BYLOC = 0;	// Time series for location (default)
	int BYPARCEL = 1;	// Time series for parcel
	int BYRIGHT = 2;	// Time series for right (one point)
	// Free water methods...
	int AlwaysOn_int = 0;
	int UseSeniorRightAppropriationDate_int = 1;
	int AsSpecified_int = 1;
	int FreeWaterMethod_int = AsSpecified_int;
	if ( FreeWaterMethod == null ) {
		FreeWaterMethod_int = AsSpecified_int;
	}
	else if ( FreeWaterMethod.equalsIgnoreCase(AlwaysOn)) {
		FreeWaterMethod_int = AlwaysOn_int;
	}
	else if ( FreeWaterMethod.equalsIgnoreCase(UseSeniorRightAppropriationDate)) {
		FreeWaterMethod_int = UseSeniorRightAppropriationDate_int;
	}

	Vector tslist = new Vector();
	TS ts = null;	// Time series to add.
	StateMod_Right smright;
	StateMod_WellRight smwellright; // Only for parcel processing.
	boolean need_to_create_ts;	// Indicate whether new TS needed
	String tsid = null;	// Time series identifier
	String id = null;	// ID part of tsid
	int pos = 0;		// Position of time series in list
	String adminnum_String = null;
	double adminnum_double;
	StateMod_AdministrationNumber adminnum = null;
						// Administration number corresponding to
						// date for right.
	DateTime decree_DateTime = null; // Right appropriation date, to day.
	// Get the locations that have water rights.
	Vector loc_Vector = null;
	if ( spatial_aggregation == BYPARCEL ) {
		loc_Vector = getWaterRightParcelList ( smrights, parcel_year );
	}
	else { // Process by location or individual rights...
		loc_Vector = getWaterRightLocationList ( smrights, parcel_year );
	}
	int loc_size = 0;
	if ( loc_Vector != null ) {
		loc_size = loc_Vector.size();
	}
	int smrights_size = 0;
	if ( smrights != null ) {
		smrights_size = smrights.size();
	}
	if ( spatial_aggregation == BYLOC ) {
		Message.printStatus ( 2, routine, "Found " + loc_size + " locations from " +
			smrights_size + " rights.");
	}
	else if ( spatial_aggregation == BYPARCEL ) {
		Message.printStatus ( 2, routine, "Found " + loc_size + " parcels from " +
				smrights_size + " rights.");
	}
	else { Message.printStatus ( 2, routine, "Found " + loc_size + " rights from " +
			smrights_size + " rights.");
	}
	String loc_id = null;	// Identifier for a location or parcel
	Vector loc_rights = null; // Vector of StateMod_Right
	DateTime min_DateTime = null;
	DateTime max_DateTime = null;
	double decree = 0;	// Decree for water right
	String datatype = "WaterRight"; // Default data type, reset below
	String nodetype = "";	// Node type, for description, etc.
	int status = 0;	// Used for error handling
	int onoff;	// On/off switch for the right
	int free_right_count; // count of free water rights at location
	// Process the list of locations.
	for ( int iloc = 0; iloc < loc_size; iloc++ ) {
		loc_id = (String)loc_Vector.elementAt(iloc);
		Message.printStatus ( 2, routine, "Processing location \"" + loc_id + "\"");
		if ( spatial_aggregation == BYPARCEL ) {
			loc_rights = getWaterRightsForParcel ( smrights, loc_id, parcel_year );
		}
		else { // Process by location or individual rights...
			loc_rights = getWaterRightsForLocation ( smrights, loc_id, parcel_year );
		}
		size = 0;
		if ( loc_rights != null ) {
			size = loc_rights.size();
		}
		// If processing for the location or parcel, set the period of the time
		// series data to the bounding limits of the dates.
		min_DateTime = null;	// Initialize
		max_DateTime = null;
		free_right_count = 0;
		if ( (spatial_aggregation == BYLOC) ||
				(spatial_aggregation == BYPARCEL) ) {
			for ( int i = 0; i < size; i++ ) {
				smright = (StateMod_Right)loc_rights.elementAt(i);
				if ( smright == null ) {
					continue;
				}
				adminnum_String = smright.getAdministrationNumber();
				adminnum_double = StringUtil.atod(adminnum_String);
				adminnum = new StateMod_AdministrationNumber ( adminnum_double );
				decree_DateTime = new DateTime(adminnum.getAppropriationDate());
				if ( (min_DateTime == null) ||
						decree_DateTime.lessThan(min_DateTime) ) {
					min_DateTime = decree_DateTime;
				}
				if ( (max_DateTime == null) ||
						decree_DateTime.greaterThan(max_DateTime) ) {
					max_DateTime = decree_DateTime;
				}
				// Check whether a free water right...
				if ( (adminnum_double >= FreeWaterAdministrationNumber_double)) {
					++free_right_count;
				}
			}
		}
		// Now process each right for the location...
		for ( int i = 0; i < size; i++ ) {
			smright = (StateMod_Right)loc_rights.elementAt(i);
			if ( smright == null ) {
				continue;
			}
			decree = smright.getDecree();
			onoff = smright.getSwitch();
			// Get the appropriation date from the admin number.
			adminnum_String = smright.getAdministrationNumber();
			adminnum_double = StringUtil.atod(adminnum_String);
			if ( (adminnum_double >= FreeWaterAdministrationNumber_double) &&
					(FreeWaterMethod != null) ) {
				// The right is a free water right.  Adjust if requested
				if ( FreeWaterMethod_int == AlwaysOn_int ) {
					// Set to earliest of starting date and most senior
					if ( (spatial_aggregation == BYRIGHT) ||
							(free_right_count == size) ) {
						// Minimum date will not have been determined.
						decree_DateTime = OutputStart_DateTime;
					}
					else {	// have a valid minimum
						decree_DateTime = min_DateTime;
						if ( OutputStart_DateTime.lessThan(decree_DateTime)) {
							decree_DateTime = OutputStart_DateTime;
						}
					}
				}
				else if ( FreeWaterMethod_int == UseSeniorRightAppropriationDate_int ) {
					if (min_DateTime != null ) {
						decree_DateTime = min_DateTime;
					}
					else { decree_DateTime =
						FreeWaterAppropriationDate_DateTime;
					}
				}
			}
			else {	// Process the admin number to get the decree date...
				adminnum = new StateMod_AdministrationNumber ( adminnum_double );
				decree_DateTime = new DateTime(adminnum.getAppropriationDate());
			}
			// TODO SAM 2007-05-16 Can optimize by saving instances above in memory
			// so they don't need to be recreated.
			need_to_create_ts = false;
			if ( spatial_aggregation == BYLOC ) {
				// Search for the location in the time series list.
				// If found, add to the time series.  Otherwise, create
				// a new time series.
				pos = TSUtil.indexOf ( tslist, smright.getLocationIdentifier(),
					"Location", 1 );
				if ( pos >= 0 ) {
					// Will add to the matched right
					ts = (TS)tslist.elementAt(pos);
				}
				else {	// Need to create a new total right.
					id = smright.getLocationIdentifier();
					need_to_create_ts = true;
				}
			}
			else if ( spatial_aggregation == BYPARCEL ) {
				// Search for the location in the time series list.
				// If found, add to the time series.  Otherwise, create
				// a new time series.
				smwellright = (StateMod_WellRight)smright;
				pos = TSUtil.indexOf ( tslist, smwellright.getParcelID(),
					"Location", 1 );
				if ( pos >= 0 ) {
					// Will add to the matched right
					ts = (TS)tslist.elementAt(pos);
				}
				else {	// Need to create a new total right.
					id = smwellright.getParcelID();
					need_to_create_ts = true;
				}
			}
			else {	// Create an individual time series for each right.
				need_to_create_ts = true;
				id = smright.getLocationIdentifier() + "-" +
				smright.getIdentifier();
			}
			// Create the time series (either first right for a location or
			// time series are being created for each right).
			if ( need_to_create_ts ) {
				if ( smright instanceof StateMod_DiversionRight ) {
					datatype = "DiversionWaterRight";
					nodetype = "Diversion";
				}
				else if ( smright instanceof StateMod_InstreamFlowRight ) {
					datatype = "InstreamFlowWaterRight";
					nodetype = "InstreamFlow";
				}
				else if ( smright instanceof StateMod_ReservoirRight ) {
					datatype = "ReservoirWaterRight";
					nodetype = "Reservoir";
				}
				else if ( smright instanceof StateMod_WellRight ) {
					datatype = "WellWaterRight";
					nodetype = "Well";
				}
				if ( spatial_aggregation == BYLOC ) {
					// Append to the datatype
					datatype += "sTotal";
				}
				else if ( spatial_aggregation == BYPARCEL ) {
					// Append to the datatype
					datatype += "sParcelTotal";
				}
				if ( interval_base == TimeInterval.DAY ) {
					tsid = id + ".StateMod." + datatype + ".Day";
				}
				else if ( interval_base == TimeInterval.MONTH ) {
					tsid = id + ".StateMod." + datatype + ".Month";
				}
				else if ( interval_base == TimeInterval.YEAR ) {
					tsid = id + ".StateMod." + datatype + ".Year";
				}
				else if ( interval_base == TimeInterval.IRREGULAR ) {
					tsid = id + ".StateMod." + datatype + ".Irregular";
				}
				ts = TSUtil.newTimeSeries ( tsid, true );
				ts.setIdentifier ( tsid );
				if ( spatial_aggregation == BYLOC ) {
					ts.setDescription ( smright.getLocationIdentifier() + " Total " + nodetype + " Rights for Location" );
				}
				else if ( spatial_aggregation == BYPARCEL ) {
					if ( smright instanceof StateMod_WellRight ) {
						ts.setDescription ( ((StateMod_WellRight)smright).getParcelID() + " Total " + nodetype + " Rights for Parcel" );
					}
					else {
						ts.setDescription ( smright.getLocationIdentifier() + " Total " + nodetype + " Rights for Parcel" );
										}
				}
				else {
					// Individual rights
					ts.setDescription ( smright.getName() );
				}
				ts.setDataUnits ( smright.getDecreeUnits() );
				// Set the dates for the time series. If a single right
				// is being used, use the specific date.  Otherwise,
				// use the extent of the dates found for all rights.  If
				// a period has been specified, use that.
				// Set the original dates to that from the data...
				if ( (spatial_aggregation == BYLOC) ||
					(spatial_aggregation == BYPARCEL)) {
					ts.setDate1Original ( min_DateTime );
					ts.setDate2Original ( max_DateTime );
				}
				else { ts.setDate1Original( decree_DateTime );
					ts.setDate2Original ( decree_DateTime );
				}
				// Set the active dates to that requested or found...
				if ( (OutputStart_DateTime != null) && (OutputEnd_DateTime != null) ) {
					ts.setDate1 ( OutputStart_DateTime );
					ts.setDate2 ( OutputEnd_DateTime );
					Message.printStatus ( 2, routine,
							"Setting right time series period to requested " +
							ts.getDate1() + " to " + ts.getDate2() );
				}
				else {
					if ( (spatial_aggregation == BYLOC) ||
							(spatial_aggregation == BYPARCEL)) {
						ts.setDate1 ( min_DateTime );
						ts.setDate2 ( max_DateTime );
					}
					else {
						ts.setDate1 ( decree_DateTime );
						ts.setDate2 ( decree_DateTime );
					}
					Message.printStatus ( 2, routine,
							"Setting right time series period to data limit " +
							ts.getDate1() + " to " + ts.getDate2() );
				}
				// Initialize to zero...
				if ( process_data ) {
					if ( interval_base == TimeInterval.DAY ) {
						status = ((DayTS)ts).allocateDataSpace ( 0.0 );
					}
					else if ( interval_base == TimeInterval.MONTH ) {
						status = ((MonthTS)ts).allocateDataSpace ( 0.0 );
					}
					else if ( interval_base == TimeInterval.YEAR ) {
						status = ((YearTS)ts).allocateDataSpace ( 0.0 );
					}
					if ( status != 0 ){
						// Don't add the time series
						continue;
					}
				}
				// No need to allocate space for irregular.
				// Add the time series to the list...
				tslist.addElement ( ts );
			}
			// Now add to the right.  If daily, add to each time step.
			if ( process_data ) {
				// Set data in the time series.
				if ( onoff == 0 ) {
					// Do not process.
					continue;
				}
				else if ( onoff > 1 ) {
					// On/off is a year.
					// Only turn on the right for the indicated
					// year.  Reset the year of the right to the on/off but
					// only if it is later than the year from the admin number.
					if ( onoff > decree_DateTime.getYear() ) {
						Message.printStatus(2,"", "Resetting decree year from " +
								decree_DateTime.getYear() + " to on/off " + onoff );
						decree_DateTime.setYear( onoff );
					}
				}
				else if ( onoff < 0 ) {
					// Not yet handled - not expected to occur with well rights.
					// TODO SAM 2007-06-08 Evaluate how to handle negative switch - skip
					Message.printStatus( 2, routine,
							"Software not able to handle negative on/off well right switch.  Skipping right.");
					continue;
				}
				if ( (interval_base == TimeInterval.DAY) ||
						(interval_base == TimeInterval.MONTH) ||
						(interval_base == TimeInterval.YEAR)) {
					if ( (spatial_aggregation == BYLOC) ||
							(spatial_aggregation == BYPARCEL)) {
						if ( decree > 0.0 ) {
							Message.printStatus(2,"", "Adding constant decree " + decree + " starting in " + decree_DateTime + " to " + ts.getDate2());
							TSUtil.addConstant ( ts, decree_DateTime, ts.getDate2(), -1, decree,
									TSUtil.IGNORE_MISSING );
						}
					}
					else {
						// Set the single value since one point in time...
						ts.setDataValue ( decree_DateTime, decree );
					}
				}
				else { // Irregular...
					ts.setDataValue ( decree_DateTime, decree );
				}
			}
		}
	}
	size = tslist.size();
	if ( include_dataset_totals && (size > 0) ) {
		// Include one time series that is the sum of all other time series.
		if ( interval_base == TimeInterval.DAY ) {
			tsid = "DataSet.StateMod." + datatype + ".Day";
		}
		else if ( interval_base == TimeInterval.MONTH ) {
			tsid = "DataSet.StateMod." + datatype + ".Month";
		}
		else if ( interval_base == TimeInterval.YEAR ) {
			tsid = "DataSet.StateMod." + datatype + ".Year";
		}
		TS totalts = TSUtil.newTimeSeries ( tsid, true );
		totalts.setIdentifier ( tsid );

		TSLimits limits = TSUtil.getPeriodFromTS ( tslist, TSUtil.MAX_POR );
		totalts.setDate1( limits.getDate1() );
		totalts.setDate1Original( limits.getDate1() );
		totalts.setDate2( limits.getDate2() );
		totalts.setDate2Original( limits.getDate2() );
		Message.printStatus ( 2, routine, "Date limits for total time series are " +
				limits.getDate1() + " to " + limits.getDate2() );

		totalts.allocateDataSpace();
		boolean units_set = false;
		DateTime date = null;
		double value;
		for ( int i = 0; i < size; i++ ) {
			ts = (TS)tslist.elementAt(i);
			if ( !units_set && (ts.getDataUnits().length() > 0) ) {
				totalts.setDataUnits ( ts.getDataUnits() );
				totalts.setDataUnitsOriginal ( ts.getDataUnits() );
				units_set = true;
			}
			// The time series have different periods but want
			// the last value to be continued to the end of the
			// period.  Therefore, first add each time series, and then add
			// the last value from one interval past the end date to the
			// of the time series to the end of the total time series.
			Message.printStatus ( 2, routine, "Add " + ts.getLocation() + " " +
					ts.getDate1() + " to " + ts.getDate2() );
			TSUtil.add ( totalts, ts );
			date = new DateTime(ts.getDate2());
			// Should be non-missing...
			value = ts.getDataValue( date );
			if ( !ts.isDataMissing(value)) {
				// Add constant at the end of the time series.
				date.addInterval ( ts.getDataIntervalBase(),
						ts.getDataIntervalMult() );
				TSUtil.addConstant ( totalts, date, totalts.getDate2(), -1, value,
						TSUtil.IGNORE_MISSING);
				Message.printStatus ( 2, routine, "Add constant " + value + " " +
						ts.getLocation() + " " + date + " to " + totalts.getDate2() );
			}
		}
		totalts.setDescription ( "Total " + nodetype + " water right time series." );
		tslist.addElement ( totalts );
	}
	return tslist;
}

/**
Create a list of data objects, for use in choices, etc.
@return a Vector of String containing formatted identifiers and names.  A
non-null list is guaranteed; however, the list may have zero items.
@param include_name If false, each string will consist of only the value
returned from StateMod_Data.getID().  If true the string will contain the ID,
followed by " - xxxx", where xxxx is the value returned from
StateMod_Data.getName().
*/
public static Vector createDataList (	Vector smdata_Vector,
					boolean include_name )
{	Vector v = null;
	if ( smdata_Vector == null ) {
		return new Vector();
	}
	else {	// This optimizes memory management...
		v = new Vector ( smdata_Vector.size() );
	}
	int size = smdata_Vector.size();
	StateMod_Data smdata;
	String id = "", name = "";
	TS ts;
	Object o;
	for ( int i = 0; i < size; i++ ) {
		o = smdata_Vector.elementAt(i);
		if ( o == null ) {
			continue;
		}
		if ( o instanceof StateMod_Data ) {
			smdata = (StateMod_Data)o;
			id = smdata.getID();
			name = smdata.getName();
		}
		else if ( o instanceof TS ) {
			ts = (TS)o;
			id = ts.getLocation();
			name = ts.getDescription();
		}
		else {	Message.printWarning ( 2,"StateMod_Util.createDataList",
			"Unrecognized StateMod data." );
		}
		if ( id.equalsIgnoreCase(name) ) {
			v.addElement ( id );
		}
		else if ( include_name ) {
			v.addElement ( id + " - " + name );
		}
		else {	v.addElement ( id );
		}
	}
	return v;
}

/**
Creates a new ID given an ID used for rights.  If the previous right ID was
12345.05, 12345.06 will be returned.  This also works for alphanumeric IDs
(ABC_003.05 becomes ABC_003.06).  If the old ID doesn't contain a '.', the
same old ID is returned.
@return the new ID.
*/
public static String createNewID(String oldID) {
	String routine = "StateMod_Util.createNewID";
	// create new id
	int dotIndex = oldID.indexOf(".");
	if (dotIndex == -1) {
		return oldID + ".01";
	}
	
	String front = oldID.substring(0, dotIndex);

	String back = oldID.substring(dotIndex + 1);
	
	Integer D = null;
	try {
		D = new Integer(back);			
	}
	catch (NumberFormatException e) {
		Message.printWarning(1, routine, "Could not create new ID "
			+ "because old ID does not have a number after the "
			+ "decimal: '" + back + "'");
		return oldID;
	}
	int d = D.intValue();
	d += 1;
	Integer DD = new Integer(d); 

	String newBack = DD.toString();

	String zero = "";
	if (d < 10) {
		zero = "0";
	}
	String newid = front + "." + zero + newBack;
	
	return newid;
}

/**
Calculates the earliest date in the period of record for the time series in the
dataset.  Note that this is always a calendar date.  The time series that are
checked are:<br>
<ol>
<li>StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY</li>
<li>StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_DEMAND_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY</li>
</ol>
@param dataset the dataset in which to check for the earliest POR
@return a DateTime object with the earliest POR, or null if the earliest date
cannot be found.
*/
public static DateTime findEarliestDateInPOR(StateMod_DataSet dataset)
{	DateTime newDate = null;
	Vector tsVector = null;
	DateTime tempDate = null;

	int numFiles = 8;
	int[] files = new int[numFiles];
	files[0] = StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY;
	files[1] = StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY;
	files[2] = StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY;
	files[3] = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
	files[4] = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY;
	files[5] = StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY;
	files[6] = StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY;
	files[7] = StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY;

	DateTime seedDate = new DateTime();
	seedDate.setYear(3000);

	for (int i = 0; i < numFiles; i++) {
		if (dataset.getComponentForComponentType(files[i]).hasData()) {
			tsVector = (Vector)(
				(dataset.getComponentForComponentType(
				files[i])).getData());
			if ( newDate == null ) {
				tempDate = findEarliestDateInPORHelper(tsVector,
						seedDate);
			}
			else {	tempDate = findEarliestDateInPORHelper(tsVector,
						newDate);
			}
			if (tempDate != null) {
				newDate = tempDate;
			}
		}
	}

	return newDate;
}

/**
A private helper method for <b>findEarliestDateInPOR()</b> for finding the
earliest date in the period.  Because the vector of time series is assumed to
have been read from a single file, just check the first time series with a
non-zero date (no need to check all time series after that).
@param tsVector a vector of time series.
@param newDate the data against which to check the first date in the time series
@return first date of the time series or null if the first date of the time
series is not earlier than newDate
*/
private static DateTime findEarliestDateInPORHelper(Vector tsVector, 
DateTime newDate) {
	DateTime date = null;
	if (tsVector.size() > 0) {
		date = ((TS)tsVector.elementAt(0)).getDate1();
		if (date.getYear() > 0 && date.lessThan(newDate)) {
			return date;
		}
	}
	return null;
}

/**
Calculates the latest date in the period of record for the time series in the
dataset.  Note that this is always a calendar date.  The time series that are
checked are:<br>
<ol>
<li>StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY</li>
<li>StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_DEMAND_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY</li>
<li>StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY</li>
</ol>
@param dataset the dataset in which to check for the latest POR
@return a DateTime object with the latest date in the period, or null if no
data are available.
*/
public static DateTime findLatestDateInPOR(StateMod_DataSet dataset)
{	DateTime newDate = null;
	Vector tsVector = null;
	DateTime tempDate = null;

	int numFiles = 8;
	int[] files = new int[numFiles];
	files[0] = StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY;
	files[1] = StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY;
	files[2] = StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_MONTHLY;
	files[3] = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
	files[4] = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY;
	files[5] = StateMod_DataSet.COMP_RESERVOIR_CONTENT_TS_MONTHLY;
	files[6] = StateMod_DataSet.COMP_RESERVOIR_TARGET_TS_MONTHLY;
	files[7] = StateMod_DataSet.COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY;

	DateTime seedDate = new DateTime();
	seedDate.setYear(-3000);
	
	for (int i = 0; i < numFiles; i++) {
		if (dataset.getComponentForComponentType(files[i]).hasData()) {
			tsVector = (Vector)(
				(dataset.getComponentForComponentType(
				files[i])).getData());
			if ( newDate == null ) {
				tempDate = findLatestDateInPORHelper(tsVector,
						seedDate);
			}
			else {	tempDate = findLatestDateInPORHelper(tsVector,
						newDate);
			}
			if (tempDate != null) {
				newDate = tempDate;
			}
		}
	}

	return newDate;
}

/**
A private helper method for <b>findLatestDateInPOR()</b> for finding the latest
POR.  Because the vector of time series is assumed to have been read from a
single file, just check the first time series with a non-zero date (no need
to check all time series after that).
@param tsVector a vector of time series.
@param newDate the data against which to check the last date in the time series
@return last date of the time series or null if the last date of the time
series is not later than newDate
*/
private static DateTime findLatestDateInPORHelper (	Vector tsVector,
							DateTime newDate )
{	DateTime date = null;
	if (tsVector == null) {
		return null;
	}
	if (tsVector.size() > 0) {
		date = ((TS)tsVector.elementAt(0)).getDate2();
		if (date.getYear() > 0 && date.greaterThan(newDate) ) {
			return date;
		}
	}
	return null;
}

// REVISIT SAM 2004-09-07 JTS needs to javadoc?
public static String findNameInVector(String id, Vector v, boolean includeDash)
{	int size = v.size();

	StateMod_Data data;
	for (int i = 0; i < size; i++) {
		data = (StateMod_Data)v.elementAt(i);
		if (data.getID().equals(id)) {
			if (includeDash) {
				return " - " + data.getName();
			}
			else {
				return data.getName();
			}
		}
	}
	return "";
}

/**
Find the insert position for a new water right, in the full list of rights.
The position that is returned can be used with Vector.insertElementAt(), for the
full data array.  The position is determined by finding the an item in the
data vector with the same "cgoto" value.  The insert then considers the value of
"irtem" so that the result after the insert is water rights sorted by "irtem". 
It is assumed that the water rights for the same "cgoto" are grouped together
and are sorted by "irtem".  If no matching "cgoto" is found, the insert position
will be according to cgoto order.
@return the insert position for a new water right, in the full list of rights,
or -1 if the right should be inserted at the end (no other option).
@param data_Vector a Vector of StateMod_*Right, with data members populated.
@param item A single StateMod_*Right to insert.
*/
public static int findWaterRightInsertPosition ( Vector data_Vector,
						StateMod_Data item )
{	if ( (data_Vector == null) || (data_Vector.size() == 0) ) {
		// Add at the end...
		return -1;
	}
	int size = 0;
	if ( data_Vector != null ) {
		size = data_Vector.size();
	}
	StateMod_Data data = null;	// StateMod data object to evaluate
	for ( int i = 0; i < size; i++ ) {
		data = (StateMod_Data)data_Vector.elementAt(i);
		if (data.getID().compareTo(item.getID()) > 0 ) {
			// Vector item is greater than the new item
			// to insert so insert at this position...
			return i;
		}
	}
	// Add at the end...
	return -1;
	/* REVISIT - all of this seemed to be getting too complicated.  Just
	 inserting based on the right ID seems to be simplest
	int pos = locateIndexFromCGOTO ( item.getCgoto(), data_Vector );
	int size = 0;
	if ( data_Vector != null ) {
		size = data_Vector.size();
	}
	int sizem1 = size - 1;		// To check end of loop.
	StateMod_Data data = null;	// StateMod data object to evaluate
	if ( pos < 0 ) {
		/ * For now sort by right ID because CGOTO seems to sometimes
		   have a totally different spelling.
		// Unable to find the cgoto.  The water right to be inserted is
		// the first one for the structure.  Assume alphabetical and
		// find a right with the next cgoto and insert before it...
		for ( int i = 0; i < size; i++ ) {
			data = (StateMod_Data)data_Vector.elementAt(i);
			if (data.getCgoto().compareTo(item.getCgoto()) > 0 ) {
				// Vector item is greater than the new item
				// to insert so insert at this position...
				return i;
			}
		}
		* /
		// Unable to find the cgoto.  The water right to be inserted is
		// the first one for the structure.  Assume alphabetical right
		// identifiers and insert before accordingly...
		for ( int i = 0; i < size; i++ ) {
			data = (StateMod_Data)data_Vector.elementAt(i);
			if (data.getID().compareTo(item.getID()) > 0 ) {
				// Vector item is greater than the new item
				// to insert so insert at this position...
				return i;
			}
		}
		// Add at the end...
		return -1;
	}
	// Loop through rights with the same "cgoto" until the item.irtem is
	// greater than a value in the list (in which case the insert position
	// is the last Vector item processed)...
	String irtem = null;
	if ( item instanceof StateMod_DiversionRight ) {
		irtem = ((StateMod_DiversionRight)item).getIrtem();
	}
	else if ( item instanceof StateMod_ReservoirRight ) {
		irtem = ((StateMod_ReservoirRight)item).getRtem();
	}
	else if ( item instanceof StateMod_InstreamFlowRight ) {
		irtem = ((StateMod_InstreamFlowRight)item).getIrtem();
	}
	else if ( item instanceof StateMod_WellRight ) {
		irtem = ((StateMod_WellRight)item).getIrtem();
	}
	double irtem_double = StringUtil.atod ( irtem );
					// The irtem from the specific right
	String irtem_data = null;	// The irtem from a data object, as a
					// string
	double irtem_data_double = 0.0;	// The irtem from a data object, as a
					// double.
	data = (StateMod_Data)data_Vector.elementAt(pos);
	for ( int i = pos; i < size; ) {
		// Get the double value in the vector...
		if ( data instanceof StateMod_DiversionRight ) {
			irtem_data = ((StateMod_DiversionRight)data).getIrtem();
		}
		else if ( data instanceof StateMod_ReservoirRight ) {
			irtem_data = ((StateMod_ReservoirRight)data).getRtem();
		}
		else if ( data instanceof StateMod_InstreamFlowRight ) {
			irtem_data =
				((StateMod_InstreamFlowRight)data).getIrtem();
		}
		else if ( data instanceof StateMod_WellRight ) {
			irtem_data = ((StateMod_WellRight)data).getIrtem();
		}
		// Compare the double values...
		irtem_data_double = StringUtil.atod ( irtem_data );
		Message.printStatus ( 2, "",
		"Checking " + item.getCgoto() + " " + irtem_double +
			" against vector " + data.getCgoto() + " " +
			irtem_data_double );
		if ( irtem_data_double > irtem_double ) {
			// Have gone past the "irtem" and need to insert before
			// For debugging...
			Message.printStatus ( 2, "", "returning " + i );
			// the current item...
			return i;
		}
		if ( i != sizem1 ) {
			// Now check for overrun to the next structure...
			data = (StateMod_Data)data_Vector.elementAt(++i);
			if (!data.getCgoto().equalsIgnoreCase(item.getCgoto())){
				// This is a new cgoto so need to insert before
				// it...
				return i;
			}
		}
		else {	// Last iteration
			break;
		}
	}
	return -1;
	*/
}

/**
Format a label to use for a data object.  The format will be "ID (Name)".  If
the name is null or the same as the ID, then the format will be ID.
@param id Data identifier.
@param name Data name.
@return formatted label for data.
*/
public static String formatDataLabel ( String id, String name )
{	if ( id.equalsIgnoreCase(name) ) {
		return id;
	}
	else if ( id.equals("") ) {
		return name;
	}
	else if ( name.equals("") ) {
		return id;
	}
	else {	return id + " (" + name + ")";
	}
}

/**
Works in coordination with isDailyTimeSeriesAvailable-make sure to keep in sync
@return daily time series based on rules for getting daily TS
	dailyID = dayTS identifier, return daily ts
	dailyID = 0, return daily average based on month ts
	else return a calculated daily by getting ratio of daily to monthly 
		and multiplying to daily (daily is then a pattern)
@param dailyID daily id to use in comparison as described above
@param calculate only used when daily ID != dayTS identifier;  
	if true, calculates time series using ratio of monthly to daily
	if false, returns original dayTS (pattern)
*/
public static DayTS getDailyTimeSeries ( String ID, String dailyID, 
	MonthTS monthTS, DayTS dayTS, boolean calculate )
{ 	String rtn = "StateMod_GUIUtil.getDailyTimeSeries";
	// if dailyID is 0
	if ( dailyID.equalsIgnoreCase ( "0" )) {
		if ( monthTS == null ) {
			Message.printWarning ( 2, 
				"StateMod_GUIUtil.getDailyTimeSeries", 
			"Monthly time series is null.  Unable to calculate" +
			" daily." );
			return null;
		}
		String monthTSUnits = monthTS.getDataUnits();
		if ( ! ( monthTSUnits.equalsIgnoreCase ( "ACFT" ) ||
		     monthTSUnits.equalsIgnoreCase ( "AF/M" )))
		{
			Message.printWarning ( 2, 
				"StateMod_GUIUtil.getDailyTimeSeries",
			"Monthly time series units \"" + monthTSUnits +
			"\" not \"ACFT\".  Unable to process daily ts." );
			return null;
		}

		double convertAFtoCFS = 43560.0 / (3600.0 * 24.0);

		DayTS newDayTS = new DayTS ( );

		newDayTS.copyHeader(monthTS);
		newDayTS.setDataUnits( "CFS" );
		DateTime date1 = monthTS.getDate1();
		date1.setDay ( 1 );
		DateTime date2 = monthTS.getDate2();
		date2.setDay ( TimeUtil.numDaysInMonth ( date2 ) );

		newDayTS.setDate1 ( date1 );
		newDayTS.setDate2 ( date2 );
		newDayTS.allocateDataSpace();

		int numDaysInMonth;
		double avgValue;
		DateTime ddate;
		for ( DateTime date = new DateTime ( date1 );
			date.lessThanOrEqualTo ( date2 );
			date.addInterval ( TimeInterval.MONTH, 1 )) 
		{
			numDaysInMonth = TimeUtil.numDaysInMonth ( date );
			avgValue = monthTS.getDataValue ( date )
				* convertAFtoCFS / numDaysInMonth;

			ddate = new DateTime ( date );
			for ( int i=0; i<numDaysInMonth; i++ ) 
			{
				newDayTS.setDataValue ( ddate, avgValue );
				ddate.addInterval ( TimeInterval.DAY, 1 );
			}
		}
		return newDayTS;
	}

	// if dailyID = ID identifier
	else if ( dailyID.equalsIgnoreCase ( ID ))
	{	if ( dayTS == null ) {
			Message.printWarning ( 1, 
				"StateMod_GUIUtil.getDailyTimeSeries", 
			"Daily time series is null." );
			return null;
		}
		return dayTS;
	}

	// if dailyID != ID and pattern is desired
	else if ( !calculate )
	{
		if ( dayTS == null ) {
			Message.printWarning ( 1, 
				"StateMod_GUIUtil.getDailyTimeSeries", 
			"Daily time series pattern is null." );
			return null;
		}
		return dayTS;
	}
	// if dailyID != dayTS identifier and calculation is desired
	else
	{
		if ( dayTS == null ) {
			Message.printWarning ( 1, 
				"StateMod_GUIUtil.getDailyTimeSeries", 
			"Daily time series pattern is null." );
			return null;
		}
		if ( monthTS == null ) {
			Message.printWarning ( 1, 
				"StateMod_GUIUtil.getDailyTimeSeries", 
			"Monthly time series is null." );
			return null;
		}

		int numValuesInMonth;
		double sum, ratio, value;
		DateTime ddate, enddate;
		boolean isFlow = true;

		DayTS newDayTS = new DayTS ( );
		newDayTS.copyHeader(dayTS);
		newDayTS.allocateDataSpace();

		double convertAFtoCFS = 43560.0 / (3600.0 * 24.0);
		String dayTSUnits = dayTS.getDataUnits();
		if ( dayTSUnits.equalsIgnoreCase ( "ACFT" ) ||
		     dayTSUnits.equalsIgnoreCase ( "AF/M" ))
			isFlow = false;
		else if ( dayTSUnits.equalsIgnoreCase ( "CFS" ))
			isFlow = true;
		else {
			Message.printWarning ( 2, 
				"StateMod_GUIUtil.getDailyTimeSeries", 
				"Unable to process dayTS due to units \"" + 
				dayTSUnits + "\"" );
			return null;
		}

		if ( Message.isDebugOn )
			Message.printDebug ( 30, rtn, "convertAFtoCFS is " +
			convertAFtoCFS );

		// check for units we can handle


		Message.printStatus ( 1, rtn, "Looking through dates " +
			dayTS.getDate1() + " " + dayTS.getDate2());
		for ( DateTime date = new DateTime ( dayTS.getDate1() );
			date.lessThanOrEqualTo ( dayTS.getDate2() );
			date.addInterval ( TimeInterval.MONTH, 1 )) 
		{
			try {
			enddate = new DateTime ( date );
			enddate.addInterval ( TimeInterval.MONTH, 1 );

			numValuesInMonth = 0;
			sum = 0;
			for ( ddate = new DateTime ( date ); 
				ddate.lessThan( enddate ); 
				ddate.addInterval ( TimeInterval.DAY, 1 ) )
			{
				value = dayTS.getDataValue(ddate);
				if ( !dayTS.isDataMissing ( value )) {
					numValuesInMonth++;
					// value is CFS, need AF/D
					sum += value;
				}
			}

			if ( Message.isDebugOn ) {
				Message.printDebug ( 30, rtn, 
					"Sum for month " + 
					date.getMonth() + " is " + sum );
				Message.printDebug ( 30, rtn, 
					"monthTS data value is " +
					monthTS.getDataValue(date));
				Message.printDebug ( 30, rtn, 
					"numDaysInMonth is " +
					TimeUtil.numDaysInMonth ( date ) );
				Message.printDebug ( 30, rtn, 
					"numValuesInMonth is " +
					numValuesInMonth );
			}
				
			// to accomodate for missing, create a ratio as follows
			//   monthly value / numDaysInMonth divided by
			//   sum / numValuesInMonth
			if ( isFlow )
				ratio = ( 
				monthTS.getDataValue(date)*convertAFtoCFS / 
				TimeUtil.numDaysInMonth ( date)) /
				( sum / numValuesInMonth );
			else
				ratio =
				monthTS.getDataValue(date) / 
				( sum / numValuesInMonth );
			if ( Message.isDebugOn )
				Message.printDebug ( 30,
				"StateMod_GUIUtil.getDailyTimeSeries", 
				"Ratio is " + ratio );

			for ( ddate = new DateTime ( date ); 
				ddate.lessThan( enddate ); 
				ddate.addInterval ( TimeInterval.DAY, 1 ) )
			{
				newDayTS.setDataValue ( ddate, 
					dayTS.getDataValue (ddate) * ratio );
			}
			} catch ( Exception e ) {
				Message.printWarning ( 2, rtn, e);
			}
		}
		return newDayTS;
		
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
Helper method to return general validators for numbers.
@return List of Validators.
 */
public static Validator[] getNumberValidators()
{
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
Return the list of water rights for a station.  The "cgoto" value in the water
rights is compared with the supplied station identifier.
@param station_id Station identifier to match.
@param rights_Vector The full list of water rights to search.
@return the list of water rights that match the station identifer.  A non-null
list is guaranteed (but may have zero length).
*/
public static Vector getRightsForStation (	String station_id,
						Vector rights_Vector )
{	Vector matches = new Vector();
	int size = 0;
	if ( rights_Vector != null ) {
		size = rights_Vector.size();
	}
	Object o;
	StateMod_DiversionRight ddr;
	StateMod_InstreamFlowRight ifr;
	StateMod_ReservoirRight rer;
	StateMod_WellRight wer;
	for ( int i = 0; i < size; i++ ) {
		o = (Object)rights_Vector.elementAt(i);
		if ( o instanceof StateMod_DiversionRight ) {
			ddr = (StateMod_DiversionRight)o;
			if ( ddr.getCgoto().equalsIgnoreCase(station_id) ) {
				matches.addElement ( ddr );
			}
		}
		else if ( o instanceof StateMod_InstreamFlowRight ) {
			ifr = (StateMod_InstreamFlowRight)o;
			if ( ifr.getCgoto().equalsIgnoreCase(station_id) ) {
				matches.addElement ( ifr );
			}
		}
		else if ( o instanceof StateMod_ReservoirRight ) {
			rer = (StateMod_ReservoirRight)o;
			if ( rer.getCgoto().equalsIgnoreCase(station_id) ) {
				matches.addElement ( rer );
			}
		}
		else if ( o instanceof StateMod_WellRight ) {
			wer = (StateMod_WellRight)o;
			if ( wer.getCgoto().equalsIgnoreCase(station_id) ) {
				matches.addElement ( wer );
			}
		}
	}
	return matches;
}

// REVISIT - may need to add parameters to modify the list based on what is
// available in a data set (e.g., leave out wells).
/**
Return a list of station types for use in the GUI.
@return a list of station types for use in the GUI.
*/
public static Vector getStationTypes ()
{	return StringUtil.toVector(__station_types);
}

/**
Get the appropriate time series from a StateMod DataSetComponent.  This method
is called in two main ways:
<ol>
<li>	From a specific display window, where the StateMod_Data object is
	known.</li>
<li>	From the StateMod_GraphingTool_JFrame, where the TSID is initially
	known.  The TSID is used to call ?????, which determines the proper
	StateMod_Data object to retrieve the time series.</li>
</ol>
@param smdata StateMod_Data object
StateMod GUI graphing tool or a time series product.
*/
/* REVISIT - thrashing as to whether this is needed.
public TS getTimeSeries (	StateMod_Data smdata,
				DateTime req_date1, DateTime req_date2,
				String req_units, boolean read_data )
{
	return null;
}
*/

/**
Get the time series data types associated with a component, for use with the
graphing tool.  Currently this returns all possible data types but does not
cut down the lists based on what is actually available.
@param comp_type Component type for a station:
StateMod_DataSet.COMP_STREAMGAGE_STATIONS,
StateMod_DataSet.COMP_DIVERSION_STATIONS,
StateMod_DataSet.COMP_RESERVOIR_STATIONS,
StateMod_DataSet.COMP_INSTREAM_STATIONS,
StateMod_DataSet.COMP_WELL_STATIONS, or
StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS.
@param id If non-null, it will be used with the data set to limit returned
choices to those appropriate for the dataset.
@param dataset If a non-null StateMod_DataSet is specified, it will be used with
the id to check for valid time series data types.  For example, it can be used
to return data types for estimated time series.
@param statemod_version StateMod version as a floating point number.  If a
negative number, then the parameters for 9.69 will be returned.
@param interval TimeInterval.DAY or TimeInterval.MONTH.
@param include_input If true, input time series including historic data from
ASCII input files are returned with the
list (suitable for StateMod GUI graphing tool).
@param include_input_estimated If true, input time series that are estimated are
included.
@param include_output If true, output time series are included in the list (this
is used by the graphing tool).  Note that some output time series are for
internal model use and are not suitable for viewing (as per Ray Bennett) and
are therefore not returned in this list.
@param check_availability If true, an input data type will only be added if it
is available in the input data set.  Because it is difficult and somewhat time
consuming to check for the validity of output time series, output time series
are not checked.  This flag is currently not used.
@param add_note If true, the string " - Input", " - Output" will be added to the
data types, to help identify input and output parameters.  This is particularly
useful when retrieving time series.
@return a non-null list of data types.  The list will have zero size if no
data types are requested or are valid.
@deprecated Use the version that has add_group and add_note.
*/
public static Vector getTimeSeriesDataTypes (	int comp_type, String id,
						StateMod_DataSet dataset,
						double statemod_version,
						int interval,
						boolean include_input,
						boolean include_input_estimated,
						boolean include_output,
						boolean check_availability,
						boolean add_note )
{	return getTimeSeriesDataTypes ( comp_type, id, dataset,
					statemod_version, interval,
					include_input,
					include_input_estimated,
					include_output,
					check_availability,
					false,	// No group for backward-comp.
					add_note );
}

/**
Get the time series data types associated with a component, for use with the
graphing tool.  Currently this returns all possible data types but does not
cut down the lists based on what is actually available.
@param comp_type Component type for a station:
StateMod_DataSet.COMP_STREAMGAGE_STATIONS,
StateMod_DataSet.COMP_DIVERSION_STATIONS,
StateMod_DataSet.COMP_RESERVOIR_STATIONS,
StateMod_DataSet.COMP_INSTREAM_STATIONS,
StateMod_DataSet.COMP_WELL_STATIONS, or
StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS.
@param id If non-null, it will be used with the data set to limit returned
choices to those appropriate for the dataset.
@param dataset If a non-null StateMod_DataSet is specified, it will be used with
the id to check for valid time series data types.  For example, it can be used
to return data types for estimated time series.
@param statemod_version StateMod version as a floating point number.
If a negative number, the parameters for version 9.69 will be returned.
@param interval TimeInterval.DAY or TimeInterval.MONTH.
@param include_input If true, input time series including historic data from
ASCII input files are returned with the
list (suitable for StateMod GUI graphing tool).
@param include_input_estimated If true, input time series that are estimated are
included.
@param include_output If true, output time series are included in the list (this
is used by the graphing tool).  Note that some output time series are for
internal model use and are not suitable for viewing (as per Ray Bennett) and
are therefore not returned in this list.
@param check_availability If true, an input data type will only be added if it
is available in the input data set.  Because it is difficult and somewhat time
consuming to check for the validity of output time series, output time series
are not checked.  This flag is currently not used.
@param add_group If true, a group is added to the front of the data type to
allow grouping of the parameters.  Currently this should only be used for
output parametes (e.g., in TSTool) because other data types have not been
grouped.
@param add_note If true, the string " - Input", " - Output" will be added to the
data types, to help identify input and output parameters.  This is particularly
useful when retrieving time series.
@return a non-null list of data types.  The list will have zero size if no
data types are requested or are valid.
*/
public static Vector getTimeSeriesDataTypes (	int comp_type, String id,
						StateMod_DataSet dataset,
						double statemod_version,
						int interval,
						boolean include_input,
						boolean include_input_estimated,
						boolean include_output,
						boolean check_availability,
						boolean add_group,
						boolean add_note )
{	return getTimeSeriesDataTypes ( 	null,	// No filename
						comp_type, id,
						dataset,
						statemod_version,
						interval,
						include_input,
						include_input_estimated,
						include_output,
						check_availability,
						add_group,
						add_note );
}

/**
Get the time series data types associated with a component, for use with the
graphing tool.  Currently this returns all possible data types but does not
cut down the lists based on what is actually available.
@param comp_type Component type for a station:
StateMod_DataSet.COMP_STREAMGAGE_STATIONS,
StateMod_DataSet.COMP_DIVERSION_STATIONS,
StateMod_DataSet.COMP_RESERVOIR_STATIONS,
StateMod_DataSet.COMP_INSTREAM_STATIONS,
StateMod_DataSet.COMP_WELL_STATIONS, or
StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS.
@param binary_filename name of the binary output file for which data types (
parameters) are being returned, typically selected by the user with a file
chooser.  The path to the file is not adjusted to a working directory so do that
before calling, if necessary.
@param id If non-null, it will be used with the data set to limit returned
choices to those appropriate for the dataset.
@param dataset If a non-null StateMod_DataSet is specified, it will be used with
the id to check for valid time series data types.  For example, it can be used
to return data types for estimated time series.
@param statemod_version StateMod version as a floating point number.  If this
is greater than VERSION_11_00, then binary file parameters are read from the
file.  Otherwise, the parameters are hard-coded in this method, based on
StateMod documentation.
If a negative number, the parameters for version 9.69 will be returned.
@param interval TimeInterval.DAY or TimeInterval.MONTH.
@param include_input If true, input time series including historic data from
ASCII input files are returned with the
list (suitable for StateMod GUI graphing tool).
@param include_input_estimated If true, input time series that are estimated are
included.
@param include_output If true, output time series are included in the list (this
is used by the graphing tool).  Note that some output time series are for
internal model use and are not suitable for viewing (as per Ray Bennett) and
are therefore not returned in this list.
@param check_availability If true, an input data type will only be added if it
is available in the input data set.  Because it is difficult and somewhat time
consuming to check for the validity of output time series, output time series
are not checked.  This flag is currently not used.
@param add_group If true, a group is added to the front of the data type to
allow grouping of the parameters.  Currently this should only be used for
output parametes (e.g., in TSTool) because other data types have not been
grouped.
@param add_note If true, the string " - Input", " - Output" will be added to the
data types, to help identify input and output parameters.  This is particularly
useful when retrieving time series.
@return a non-null list of data types.  The list will have zero size if no
data types are requested or are valid.
*/
public static Vector getTimeSeriesDataTypes (	String binary_filename,
						int comp_type, String id,
						StateMod_DataSet dataset,
						double statemod_version,
						int interval,
						boolean include_input,
						boolean include_input_estimated,
						boolean include_output,
						boolean check_availability,
						boolean add_group,
						boolean add_note )
{	String routine = "StateMod_Util.getTimeSeriesDataTypes";
	Vector data_types = new Vector();
	String [] diversion_types0 = null;
	String [] instream_types0 = null;
	String [] reservoir_types0 = null;
	String [] stream_types0 = null;
	String [] well_types0 = null;
	String [] diversion_types = null;
	String [] instream_types = null;
	String [] reservoir_types = null;
	String [] stream_types = null;
	String [] well_types = null;

	// If a filename is given and reading it shows a version >= 11.0, read
	// information from the file for use below.

	StateMod_BTS bts = null;
	if ( binary_filename != null ) {
		try {	bts = new StateMod_BTS ( binary_filename );
		}
		catch ( Exception e ) {
			// Error reading the file.  Print a warning but go on
			// and just do not have a list of parameters...
			Message.printWarning ( 3, routine,
			"Error opening/reading binary file \"" +
			binary_filename + "\" to determine parameters." );
			Message.printWarning ( 3, routine, e );
			bts = null;
		}
		// Close the file below after getting information...
		double version_double = bts.getVersion();
		if ( isVersionAtLeast ( version_double, VERSION_11_00) ) {
			// Reset information to override possible user flags.
			statemod_version = version_double;
			add_group = false;	// Not available from file
			add_note = false;	// Not available from file
		}
	}
	
	if (	(comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS) &&
		(id != null) && (id.indexOf('-') > 0) ) {
		// If the identifier includes a dash, turn off the input data
		// types because only output time series are available for
		// owner/accounts...
		include_input = false;
		include_input_estimated = false;
	}

	// Get the list of output data types based on the StateMod version.
	// These are then used below.
	if ( statemod_version < 0.0 ) {
		// Default is the latest version before the parameters are in
		// the binary file...
		statemod_version = VERSION_9_69;
	}
	if ( statemod_version >= VERSION_11_00 ) {
		// The parameters come from the binary file header.
		// Close the file because it is no longer needed...
		String [] parameters = null;
		if ( bts != null ) {
			parameters = bts.getParameters();
			// REVISIT SAM 2006-01-15
			// Remove when tested in production.
			/*
			Message.printStatus ( 2, routine,
			"Parameters from file:  " +
			StringUtil.toVector(parameters) );
			*/
			try {	bts.close();
			}
			catch ( Exception e ) {
				// Ignore - problem would have occurred at open.
			}
			bts = null;
		}
		// The binary file applies only to certain node types...
		if (	(comp_type==StateMod_DataSet.COMP_STREAMGAGE_STATIONS)||
			(comp_type==
			StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS) ||
			(comp_type==StateMod_DataSet.COMP_DIVERSION_STATIONS)||
			(comp_type==StateMod_DataSet.COMP_INSTREAM_STATIONS) ) {
			diversion_types0 = parameters;
			instream_types0 = parameters;
			stream_types0 = parameters;
		}
		else if (comp_type==StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
			reservoir_types0 = parameters;
		}
		else if (comp_type==StateMod_DataSet.COMP_WELL_STATIONS ) {
			well_types0 = parameters;
		}
	}
	else if ( statemod_version >= VERSION_9_69 ) {
		// The parameters are hard-coded because they are not in the
		// binary file header.
		diversion_types0 = __output_ts_data_types_diversion_0969;
		instream_types0 = __output_ts_data_types_instream_0969;
		reservoir_types0 = __output_ts_data_types_reservoir_0969;
		stream_types0 = __output_ts_data_types_stream_0969;
		well_types0 = __output_ts_data_types_well_0969;
	}
	else if ( statemod_version >= VERSION_9_01 ) {
		// The parameters are hard-coded because they are not in the
		// binary file header.
		diversion_types0 = __output_ts_data_types_diversion_0901;
		instream_types0 = __output_ts_data_types_instream_0901;
		reservoir_types0 = __output_ts_data_types_reservoir_0901;
		stream_types0 = __output_ts_data_types_stream_0901;
		well_types0 = __output_ts_data_types_well_0901;
	}
	else {	// Assume very old...
		// The parameters are hard-coded because they are not in the
		// binary file header.
		diversion_types0 = __output_ts_data_types_diversion_0100;
		instream_types0 = __output_ts_data_types_instream_0100;
		reservoir_types0 = __output_ts_data_types_reservoir_0100;
		stream_types0 = __output_ts_data_types_stream_0100;
		well_types0 = null;	// Should never happen.
	}
	// REVISIT SAM 2006-01-15
	// Remove when tested in production.
	/*
	Message.printStatus ( 2, routine,
	"Diversion parameters from file:  " +
	StringUtil.toVector(diversion_types0) );
	Message.printStatus ( 2, routine,
	"Reservoir parameters from file:  " +
	StringUtil.toVector(reservoir_types0) );
	Message.printStatus ( 2, routine,
	"Instream parameters from file:  " +
	StringUtil.toVector(instream_types0) );
	Message.printStatus ( 2, routine,
	"Stream parameters from file:  " +
	StringUtil.toVector(stream_types0) );
	Message.printStatus ( 2, routine,
	"Well parameters from file:  " +
	StringUtil.toVector(well_types0) );
	*/

	// Based on the requested data type, put together a list of time series
	// data types.  To simplify determination of whether a type is input or
	// output, add one of the following descriptors to the end if
	// requested...
	String input = "";
	String output = "";
	// The above lists contain the data group.  If the group is NOT desired,
	// remove the group below...
	if ( add_note ) {
		input = " - Input";
		output = " - Output";
	}
	int diversion_types_length = 0;
	int instream_types_length = 0;
	int reservoir_types_length = 0;
	int stream_types_length = 0;
	int well_types_length = 0;
	if ( diversion_types0 != null ) {
		diversion_types_length = diversion_types0.length;
		diversion_types = new String[diversion_types_length];
	}
	if ( instream_types0 != null ) {
		instream_types_length = instream_types0.length;
		instream_types = new String[instream_types_length];
	}
	if ( reservoir_types0 != null ) {
		reservoir_types_length = reservoir_types0.length;
		reservoir_types = new String[reservoir_types_length];
	}
	if ( stream_types0 != null ) {
		stream_types_length = stream_types0.length;
		stream_types = new String[stream_types_length];
	}
	if ( well_types0 != null ) {
		well_types_length = well_types0.length;
		well_types = new String[well_types_length];
	}
	for ( int i = 0; i < diversion_types_length; i++ ) {
		if ( add_group ) {
			diversion_types[i] = diversion_types0[i] + output;
		}
		else {	// Remove group from front if necessary...
			if ( diversion_types0[i].indexOf("-") > 0 ) {
				diversion_types[i] = StringUtil.getToken(
				diversion_types0[i],"-",0,1).trim() + output;
			}
			else {	diversion_types[i] = diversion_types0[i];
			}
		}
	}
	for ( int i = 0; i < instream_types_length; i++ ) {
		if ( add_group ) {
			instream_types[i] = instream_types0[i] + output;
		}
		else {	// Remove group from front if necessary...
			if ( instream_types0[i].indexOf("-") > 0 ) {
				instream_types[i] = StringUtil.getToken(
				instream_types0[i],"-",0,1).trim() + output;
			}
			else {	instream_types[i] = instream_types0[i];
			}
		}
	}
	for ( int i = 0; i < reservoir_types_length; i++ ) {
		if ( add_group ) {
			reservoir_types[i] = reservoir_types0[i] + output;
		}
		else {	// Remove group from front if necessary...
			if ( reservoir_types0[i].indexOf("-") > 0 ) {
				reservoir_types[i] = StringUtil.getToken(
				reservoir_types0[i],"-",0,1).trim() + output;
			}
			else {	reservoir_types[i] = reservoir_types0[i];
			}
		}
	}
	for ( int i = 0; i < stream_types_length; i++ ) {
		if ( add_group ) {
			stream_types[i] = stream_types0[i] + output;
		}
		else {	// Remove group from front if necessary...
			if ( stream_types0[i].indexOf("-") > 0 ) {
				stream_types[i] = StringUtil.getToken(
				stream_types0[i],"-",0,1).trim() + output;
			}
			else {	stream_types[i] = stream_types0[i];
			}
		}
	}
	for ( int i = 0; i < well_types_length; i++ ) {
		if ( add_group ) {
			well_types[i] = well_types0[i] + output;
		}
		else {	// Remove group from front if necessary...
			if ( well_types0[i].indexOf("-") > 0 ) {
				well_types[i] = StringUtil.getToken(
				well_types0[i],"-",0,1).trim() + output;
			}
			else {	well_types[i] = well_types0[i];
			}
		}
	}

	if (	(comp_type == StateMod_DataSet.COMP_STREAMGAGE_STATIONS) ||
		(comp_type == StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS) ) {
		// Stream gage and stream estimate stations are the same other
		// than stream estimate do not have historical time series...
		// Include input time series if reqeusted...
		// Input baseflow...
		if ( include_input && (interval == TimeInterval.MONTH) ) {
			data_types.addElement ( 
			StateMod_DataSet.lookupTimeSeriesDataType (
			StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_MONTHLY) +
			input );
		}
		else if(include_input && (interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
			StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_DAILY) +
			input );
		}
		// Input historical time series if requested...
		if (	include_input && (interval == TimeInterval.MONTH) &&
			(comp_type==StateMod_DataSet.COMP_STREAMGAGE_STATIONS)){
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_STREAMGAGE_HISTORICAL_TS_MONTHLY) + input);
		}
		else if (include_input && (interval == TimeInterval.DAY) &&
			(comp_type==StateMod_DataSet.COMP_STREAMGAGE_STATIONS)){
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_STREAMGAGE_HISTORICAL_TS_DAILY) + input);
		}
		// Include the estimated input time series if requested...
		// Add the estimated input...
		if (	include_input_estimated &&
			(interval == TimeInterval.DAY) ) {
			// REVISIT - need to check daily ID on station...
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
			StateMod_DataSet.COMP_STREAMGAGE_BASEFLOW_TS_DAILY) +
			"Estimated" + input);
		}
		// Input historical time series if requested...
		if (	include_input_estimated &&
			(interval == TimeInterval.DAY) &&
			(comp_type==StateMod_DataSet.COMP_STREAMGAGE_STATIONS)){
			// REVISIT - need to check daily ID on station...
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_STREAMGAGE_HISTORICAL_TS_DAILY) +
				"Estimated" + input);
		}
		// Include the output time series if requested...
		if ( include_output ) {
			data_types = StringUtil.addListToStringList (data_types,
				StringUtil.toVector ( stream_types ) );
		}
	}
	else if ( comp_type == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
		if ( include_input && (interval == TimeInterval.MONTH) ) {
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) +
				input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DEMAND_TS_MONTHLY) +
				input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_DEMAND_TS_OVERRIDE_MONTHLY) +
				input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_DEMAND_TS_AVERAGE_MONTHLY) +
				input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_MONTHLY) +
				input);
		}
		else if ( include_input && (interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DIVERSION_TS_DAILY) +
				input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DEMAND_TS_DAILY) +
				input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY) +
				input);
		}
		if ( include_input ) {
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DIVERSION_RIGHTS) +
				input);
		}
		if (	include_input_estimated &&
			(interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DIVERSION_TS_DAILY) +
				"Estimated" + input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_DEMAND_TS_DAILY) +
				"Estimated" + input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_CONSUMPTIVE_WATER_REQUIREMENT_TS_DAILY) +
				"Estimate" + input);
		}
		if ( include_output ) {
			data_types = StringUtil.addListToStringList (data_types,
				StringUtil.toVector ( diversion_types ) );
		}
	}
	else if ( comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
		// Include input time series if requested...
		if ( include_input && (interval == TimeInterval.MONTH) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_CONTENT_TS_MONTHLY) + input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_MONTHLY) + "Min" +
				input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_MONTHLY) + "Max" +
				input);
		}
		else if ( include_input && (interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_CONTENT_TS_DAILY) + input );
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_DAILY) +"Min" + input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_DAILY) +"Max" + input);
		}
		if ( include_input ) {
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_RESERVOIR_RIGHTS) +
				input);
		}
		// Include estimated input if requested...
		if (	include_input_estimated &&
			(interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_CONTENT_TS_DAILY) + "Estimated" +
				input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_DAILY)+"MinEstimated" +
				input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_RESERVOIR_TARGET_TS_DAILY)+"MaxEstimated" +
				input);
		}
		// Include output if requested...
		if ( include_output ) {
			data_types = StringUtil.addListToStringList (data_types,
				StringUtil.toVector ( reservoir_types ) );
		}
	}
	else if ( comp_type == StateMod_DataSet.COMP_INSTREAM_STATIONS ) {
		if ( include_input && (interval == TimeInterval.MONTH) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_INSTREAM_DEMAND_TS_MONTHLY) + input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.
				COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY) +
				input);
		}
		else if ( include_input && (interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
			StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_DAILY) +input);
		}
		if ( include_input ) {
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_INSTREAM_RIGHTS) +
				input);
		}
		if (	include_input_estimated &&
			(interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_DAILY)+
				"Estimated" + input);
		}
		if ( include_output ) {
			data_types = StringUtil.addListToStringList (data_types,
				StringUtil.toVector ( instream_types ) );
		}
	}
	else if ( comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
		if ( include_input && (interval == TimeInterval.MONTH) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY) +
				input);
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY) +
				input);
		}
		else if ( include_input && (interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_PUMPING_TS_DAILY) +
				input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_DEMAND_TS_DAILY) +
				input);
		}
		if ( include_input ) {
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_RIGHTS) + input);
		}
		if (	include_input_estimated &&
			(interval == TimeInterval.DAY) ) {
			data_types.addElement ( 
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_PUMPING_TS_DAILY) +
				"Estimated" + input);
			data_types.addElement (
				StateMod_DataSet.lookupTimeSeriesDataType (
				StateMod_DataSet.COMP_WELL_DEMAND_TS_DAILY) +
				"Estimated" + input);
		}
		if ( include_output ) {
			data_types = StringUtil.addListToStringList (data_types,
				StringUtil.toVector ( well_types ) );
		}
	}

	return data_types;
}

/**
Determine the output precision for a list of time series (e.g., for use with the
time series write methods or to display data in a table).  The default is to get
the precision from the units of the first time series.
*/
public static int getTimeSeriesOutputPrecision ( Vector tslist )
{	int	list_size = 0, precision = -2;	// Default
	TS	tspt = null;
	if ( (tslist != null) && (tslist.size() > 0) ) {
		tspt = (TS)tslist.elementAt(0);
		list_size = tslist.size();
	}
	if ( tspt != null ) {
		String units = tspt.getDataUnits();
		//Message.printStatus ( 2, "", "Data units are " + units );
		DataFormat outputformat = DataUnits.getOutputFormat(units,10);
		if ( outputformat != null ) {
			precision = outputformat.getPrecision();
			if ( precision > 0 ) {
				// Change to negative so output code will handle
				// overflow...
				precision *= -1;
			}
		}
		outputformat = null;
		Message.printStatus ( 2, "",
			"Precision from units output format *-1 is "+precision);
	}
	// Old code that we still need to support...
	// In year of CRDSS 2, we changed the precision to 0 for RSTO.
	// See if any of the TS in the list are RSTO...
	for ( int ilist = 0; ilist < list_size; ilist++ ) {
		tspt = (TS)tslist.elementAt(ilist);
		if ( tspt == null ) {
			continue;
		}
		if (	tspt.getIdentifier().getType(
			).equalsIgnoreCase( "RSTO") ) {
			precision = 0;
			break;
		}
	}
	return precision;
}

// REVISIT - might move this to a different class once the network builder falls
// into place.
/**
Determine the nodes that are immediately upstream of a given downstream node.
@return Vector of StateMod_RiverNetworkNode that are upstream of the node for
the given identifier.  If none are found, an empty non-null Vector is returned.
@param node_Vector Vector of StateMod_RiverNetworkNode.
@param downstream_id Downstream identifier of interest.
*/
public static Vector getUpstreamNetworkNodes (	Vector node_Vector,
						String downstream_id )
{	String rtn = "StateMod_Util.getUpstreamNetworkNodes";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, rtn,
		"Trying to find upstream nodes for " + downstream_id );
	}
	Vector v = new Vector ();
	if ( node_Vector == null ) {
		return v;
	}
	int num = node_Vector.size();
	StateMod_RiverNetworkNode riv;
	for ( int i=0; i<num; i++ ) {
		riv = (StateMod_RiverNetworkNode)node_Vector.elementAt(i);	
		if ( riv.getCstadn().equalsIgnoreCase ( downstream_id )) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, rtn,
				"Adding upstream node " + riv.getID() );
			}
			v.addElement ( riv );
		}
	}
	return v;
}

/**
Get a list of water right identifiers for a location.  The locations are the
nodes at which the rights apply.  One or more water right can exist with
the same identifier.
@param smrights List of StateMod_Right to search.
@param loc_id Location identifier to match (case-insensitive).
@param req_parcel_year Parcel year for data or -1 to use all (only used with well rights).
@return a list of locations for water rights, in the order found in the original list.
*/
public static Vector getWaterRightIdentifiersForLocation ( Vector smrights, String loc_id, int req_parcel_year )
{	Vector matchlist = new Vector();	// Returned data, identifiers (not full right)
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_Right right = null;
	int parcel_year;
	String right_id;	// Right identifier
	int matchlist_size = 0;
	boolean found = false; // used to indicate matching ID found
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_Right)smrights.elementAt(i);
		if ( (req_parcel_year != -1) && right instanceof StateMod_WellRight ) {
			// Allow the year to filter.
			parcel_year = ((StateMod_WellRight)right).getParcelYear();
			if ( parcel_year != req_parcel_year ) {
				// No need to process right.
				continue;
			}
		}
		if ( (loc_id != null) &&
			!loc_id.equalsIgnoreCase(right.getLocationIdentifier()) ) {
			// Not a matching location
			continue;
		}
		// If here need to add the identifier if not already in the
		// list...
		right_id = right.getIdentifier();
		found = false;
		for ( int j = 0; j < matchlist_size; j++ ) {
			if ( right_id.equalsIgnoreCase((String)matchlist.elementAt(j)) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Add to the list
			matchlist.addElement ( right_id );
			matchlist_size = matchlist.size();
		}
	}
	return matchlist;
}

/**
Get a list of water rights for a location.  The locations are the
nodes at which the rights apply.
@param smrights List of StateMod_Right to search.
@param loc_id Location identifier to match (case-insensitive).
@param req_parcel_year Parcel year for data or -1 to use all (only used with well rights).
@return a list of locations for water rights, in the order found in the original list.
*/
public static Vector getWaterRightsForLocation ( Vector smrights, String loc_id, int req_parcel_year )
{	Vector matchlist = new Vector();	// Returned data
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_Right right = null;
	int parcel_year;
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_Right)smrights.elementAt(i);
		if ( (req_parcel_year != -1) && right instanceof StateMod_WellRight ) {
			// Allow the year to filter.
			parcel_year = ((StateMod_WellRight)right).getParcelYear();
			if ( parcel_year != req_parcel_year ) {
				// No need to process right.
				continue;
			}
		}
		if ( loc_id.equalsIgnoreCase(right.getLocationIdentifier()) ) {
			matchlist.addElement ( right );
		}
	}
	return matchlist;
}

/**
Get a list of water rights for a location matching a right identifier.  The locations are the
nodes at which the rights apply.
@param smrights List of StateMod_Right to search.
@param loc_id Location identifier to match (case-insensitive).
@param right_id Right identifier to match (case-insensitive).
@param req_parcel_year Parcel year for data or -1 to use all (only used with well rights).
@return a list of locations for water rights, in the order found in the original list.
*/
public static Vector getWaterRightsForLocationAndRightIdentifier (
		Vector smrights, String loc_id, String right_id, int req_parcel_year )
{	Vector matchlist = new Vector();	// Returned data
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_Right right = null;
	int parcel_year;
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_Right)smrights.elementAt(i);
		if ( (req_parcel_year != -1) && right instanceof StateMod_WellRight ) {
			// Allow the year to filter.
			parcel_year = ((StateMod_WellRight)right).getParcelYear();
			if ( parcel_year != req_parcel_year ) {
				// No need to process right.
				continue;
			}
		}
		if ( (loc_id != null) && !loc_id.equalsIgnoreCase(right.getLocationIdentifier()) ) {
			continue;
		}
		if ( (right_id != null) && !right_id.equalsIgnoreCase(right.getIdentifier()) ) {
			continue;
		}
		// If here it is a match...
		matchlist.addElement ( right );
	}
	return matchlist;
}

/**
Get a list of water rights for a parcel.
@param smrights List of StateMod_WellRight to search.
@param parcel_id Parcel identifier to match (case-insensitive).
@param req_parcel_year Parcel year for data or -1 to use all.
@return a list of water rights for the parcel, in the order found in the original list.
*/
public static Vector getWaterRightsForParcel ( Vector smrights, String parcel_id, int req_parcel_year )
{	Vector matchlist = new Vector();	// Returned data
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_WellRight right = null;
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_WellRight)smrights.elementAt(i);
		if ( (req_parcel_year != -1) && (right.getParcelYear() != req_parcel_year) ) {
			// No need to process right.
			continue;
		}
		if ( parcel_id.equalsIgnoreCase(right.getParcelID()) ) {
			matchlist.addElement ( right );
		}
	}
	return matchlist;
}


/**
Get a list of locations from a list of water rights.  The locations are the
nodes at which the rights apply.
@param smrights Vector of StateMod_Right to search.
@param req_parcel_year Specific parcel year to match, or -1 to match all, if input is a
Vector of StateMod_WellRight.
@return a list of locations for water rights, in the order found in the original list.
*/
public static Vector getWaterRightLocationList ( Vector smrights, int req_parcel_year )
{	Vector loclist = new Vector();	// Returned data
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_Right right = null;
	int size_loc = 0;	// size of location list
	boolean found = false;	// Indicate whether the location has been found.
	String right_loc_id = null; // ID for location
	int parcel_year = 0;	// Parcel year to process.
	int j = 0; // Loop index for found locations.
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_Right)smrights.elementAt(i);
		if ( req_parcel_year != -1 ) {
			// Check the parcel year and skip if necessary.
			if ( right instanceof StateMod_WellRight ) {
				parcel_year = ((StateMod_WellRight)right).getParcelYear();
				if ( parcel_year != req_parcel_year ) {
					// No need to consider the right.
					continue;
				}
			}
		}
		right_loc_id = right.getLocationIdentifier();
		// Search the list to see if it is a new item...
		found = false;
		for ( j = 0; j < size_loc; j++ ) {
			if ( right_loc_id.equalsIgnoreCase((String)loclist.elementAt(j)) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Add to the list
			loclist.addElement ( right_loc_id );
			size_loc = loclist.size();
		}
	}
	return loclist;
}

/**
Get a list of parcels from a list of well water rights.  The parcels are the
locations at which well rights have been matched.
@param smrights a Vector of StateMod_WellRight to process.
@param req_parcel_year a requested year to constrain the parcel list (or -1 to return all).
@return a list of parcels for water rights, in the order found in the original list.
*/
public static Vector getWaterRightParcelList ( Vector smrights, int req_parcel_year )
{	Vector loclist = new Vector();	// Returned data
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_WellRight right = null;
	int size_loc = 0;	// size of location list
	boolean found = false;	// Indicate whether the location has been found.
	String parcel_id = null; // ID for location
	int parcel_year = 0;	// Year for parcels.
	int j = 0; // Loop index for found locations.
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_WellRight)smrights.elementAt(i);
		parcel_id = right.getParcelID();
		parcel_year = right.getParcelYear();
		if ( (req_parcel_year != -1) && (parcel_year != req_parcel_year) ) {
			// No need to process right
			continue;
		}
		// Search the list to see if it is a new item...
		found = false;
		for ( j = 0; j < size_loc; j++ ) {
			if ( parcel_id.equalsIgnoreCase((String)loclist.elementAt(j)) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Add to the list
			loclist.addElement ( parcel_id );
			size_loc = loclist.size();
		}
	}
	return loclist;
}

/**
Get a list of parcel years from a list of well water rights.
@param smrights a Vector of StateMod_WellRight to process.
@return a list of parcel years for water rights, in ascending order.
*/
public static int [] getWaterRightParcelYearList ( Vector smrights )
{	Vector yearlist = new Vector();	// Returned data
	int size = 0;
	if ( smrights != null ) {
		size = smrights.size();
	}
	StateMod_WellRight right = null;
	int size_years = 0;	// size of location list
	boolean found = false;	// Indicate whether the year has been found.
	int parcel_year = 0;	// Year for parcels.
	int j = 0; // Loop index for found years.
	for ( int i = 0; i < size; i++ ) {
		right = (StateMod_WellRight)smrights.elementAt(i);
		parcel_year = right.getParcelYear();
		// Search the list to see if it is a new item...
		found = false;
		for ( j = 0; j < size_years; j++ ) {
			if ( parcel_year == ((Integer)yearlist.elementAt(j)).intValue()) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Add to the list
			yearlist.addElement ( new Integer(parcel_year) );
			size_years = yearlist.size();
		}
	}
	int [] parcel_years = new int[yearlist.size()];
	for ( int i = 0; i < size_years; i++ ) {
		parcel_years[i] = ((Integer)yearlist.elementAt(i)).intValue();
	}
	// Sort the array...
	MathUtil.sort ( parcel_years, MathUtil.SORT_QUICK, MathUtil.SORT_ASCENDING,
			null, false );
	return parcel_years;
}

/**
Works in coordination with getDailyTimeSeries - make sure to keep in sync
@return whether daily time series is available or not, based on rules for 
getting daily TS
	dailyID = dayTS identifier, return true if daily ts exists
	dailyID = 0, return true if month ts exists
	else return true if both monthly and daily ts exist
@param dailyID daily id to use in comparison as described above
@param calculate only used when daily ID != dayTS identifier;  
	if true, returns true if monthly and daily exist
	if false, returns true if daily exists
*/
public static boolean isDailyTimeSeriesAvailable(String ID, String dailyID, 
MonthTS monthTS, DayTS dayTS, boolean calculate) {
	if (Message.isDebugOn) {
		Message.printDebug(30, "StateMod_GUIUtil."
			+ "isDailyTimeSeriesAvailable",
			"ID: " + ID + ", dailyID: " + dailyID
			+ ", monthTS: " +(monthTS!=null)+ ", dayTS: " 
			+ (dayTS!=null) + ", calculate: " + calculate);
	}

	// if dailyID is 0
	if (dailyID.equalsIgnoreCase("0")) {
		if (monthTS == null) {
			return false;
		}
		else {	
			return true;
		}
	}
	// if dailyID = ID identifier
	else if (dailyID.equalsIgnoreCase(ID)) {
		if (dayTS == null) {
			return false;
		}
		else {	
			return true;
		}
	}

	// if dailyID != ID and pattern is desired
	else if (!calculate) {
		if (dayTS == null) {
			return false;
		}
		else {	
			return true;
		}
	}
	// if dailyID != dayTS identifier and calculation is desired
	else {	
		if (dayTS == null) {
			return false;
		}
		if (monthTS == null) {
			return false;
		}
		return true;
	}
}

/**
Indicate whether the StateMod version is at least some standard value.  This is
useful when checking binary formats against a recognized version.
@return true if the version is >= the known version that is being checked.
@param version A version to check.
@param known_version A known version to check against (see VERSION_*).
*/
public static boolean isVersionAtLeast ( double version, double known_version )
{	if ( version >= known_version ) {
		return true;
	}
	else {	return false;
	}
}

/**
Return the StateMod executable, which is used to run the program.
*/
public static String getStateModExecutable ()
{	return __statemod_executable;
}

/**
Return the StateMod model version, which was determined in the last call to
runStateMod ( ... "-version" ).
*/
public static double getStateModVersion()
{	return __statemod_version;
}

/**
Return the latest known StateMod model version.  This can be used as a default
if getStateModVersion() returns zero, meaning the version has not been
determined.  In this case, the latest version is a good guess, especially for
determining binary file parameters, which don't change much between versions.
*/
public static double getStateModVersionLatest()
{	return __statemod_version_latest;
}

/**
Find the position of a StateMod_Data object in the data Vector, using the
identifier.  The position for the first match is returned.
@return the position, or -1 if not found.
@param id StateMod_Data identifier.
*/
public static int indexOf ( Vector data, String id )
{	int size = 0;
	if ( data != null ) {
		size = data.size();
	}
	StateMod_Data d = null;
	for (int i = 0; i < size; i++) {
		d = (StateMod_Data)data.elementAt(i);
		if ( id.equalsIgnoreCase ( d._id ) ) {
			return i;
		}
	}
	return -1;
}

/**
Find the position of a StateMod_Data object in the data Vector, using the name.
The position for the first match is returned.
@return the position, or -1 if not found.
@param name StateMod_Data name.
*/
public static int indexOfName ( Vector data, String name )
{	int size = 0;
	if ( data != null ) {
		size = data.size();
	}
	StateMod_Data d = null;
	for (int i = 0; i < size; i++) {
		d = (StateMod_Data)data.elementAt(i);
		if ( name.equalsIgnoreCase ( d._name ) ) {
			return i;
		}
	}
	return -1;
}

/**
Find the position of a StateMod_Data object in the data Vector, using the
river node identifier.  The position for the first match is returned.
This method can only be used for station data objects that have a river node
identifier.
@return the position, or -1 if not found.
@param id StateMod_Data identifier.
*/
public static int indexOfRiverNodeID ( Vector data, String id )
{	int size = 0;
	if ( data != null ) {
		size = data.size();
	}
	StateMod_Data d = null;
	for (int i = 0; i < size; i++) {
		// Stream gage and stream estimate have their own CGOTO
		// data members.  All other use the data in the StateMod_Data
		// base class.
		d = (StateMod_Data)data.elementAt(i);
		if ( d instanceof StateMod_StreamGage ) {
			if ( id.equalsIgnoreCase (
				((StateMod_StreamGage)d).getCgoto() ) ) {
				return i;
			}
		}
		else if ( d instanceof StateMod_StreamEstimate ) {
			if ( id.equalsIgnoreCase (
				((StateMod_StreamEstimate)d).getCgoto() ) ) {
				return i;
			}
		}
		else {	if ( id.equalsIgnoreCase (
				((StateMod_Data)d).getCgoto() ) ) {
				return i;
			}
		}
	}
	return -1;
}

/**
Determine whether a date value is missing.
@param value the date to be checked 
@return true if the date is missing, false if not
*/
public static boolean isMissing(DateTime value) {
	if ( value == MISSING_DATE ) {
		return true;
	}
	return false;
}

/**
Determine whether a double value is missing.
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
Determine whether a float value is missing.
@param f Float precision value to check.
@return true if the value is missing, false, if not.
*/
public static boolean isMissing ( float f )
{	if (	(f < (float)MISSING_DOUBLE_CEILING) &&
		(f > (float)MISSING_DOUBLE_FLOOR) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether an integer value is missing.
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
Determine whether a long integer value is missing.
@param i Integer value to check.
@return true if the value is missing, false, if not.
*/
public static boolean isMissing ( long i )
{	if ( i == MISSING_INT ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether if a String value is missing.
@param s String value to check.
@return true if the value is missing (null or empty), false, if not.
*/
public static boolean isMissing ( String s )
{	if ( (s == null) || (s.length() == 0) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Locates the index of an StateMod_Data node.  The node can be a diversion, 
reservoir, or any other StateMod object which has been derived from the 
StateMod_Data type.
from the specified CGoto.
@param ID CGoto ID to search for
@param theData vector of StateMod_Data objects
*/
public static int locateIndexFromCGOTO(String ID, Vector theData) {
	int num = 0;
	if (theData != null) {
		num = theData.size();
	}
	
	for (int i = 0; i<num; i++) {
		if (ID.equalsIgnoreCase( 
			((StateMod_Data)theData.elementAt(i)).getCgoto())) {
			return i;
		}
	}
	return StateMod_Data.MISSING_INT;
}

/**
Locates the index of a data object derived from StateMod_Data in a Vector.
@param ID ID to search for
@param theData vector of StateMod_Data objects
@return index or -999 when not found
*/
public static int locateIndexFromID(String ID, Vector theData) {
	int num = 0;
	if (theData != null) {
		num = theData.size();
	}
	
	for (int i = 0; i < num; i++) {
		if (ID.equalsIgnoreCase( 
			((StateMod_Data)theData.elementAt(i)).getID())) {
			return i;
		}
	}
	return StateMod_Data.MISSING_INT;
}

/**
Get the matching time series for an identifier.  This is used, for example, 
to find the time series associated with a StateMod data object, when the time
series is not a reference data member within the data object (e.g., climate
time series for reservoirs.
@param id Identifier associated with a StateMod data object, which will be
compared with the location part of the time series identifier.
@param tslist Vector of time series to search, typically read from one of the
time series data files.
@param match_count Indicates which match to return.  In most cases this will be
1 but for some time series (e.g., reservoir targets) the second match may be
requested.
@return matching time series or null if no match is found.
*/
public static TS lookupTimeSeries (	String id, Vector tslist,
					int match_count )
{	if ( (id == null) || id.equals("") ) {
		return null;
	}
	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	TS ts = null;
	Object o = null;
	int match_count2 = 0;
	for ( int i = 0; i < size; i++ ) {
		o = tslist.elementAt(i);
		if ( o == null ) {
			continue;
		}
		ts = (TS)o;
		if ( id.equalsIgnoreCase(ts.getLocation()) ) {
			++match_count2;
			if ( match_count2 == match_count ) {
				// Match is found so return.
				return ts;
			}
		}
	}
	return null;
}

/**
Look up a title to use for a time series graph, given the data set component.
Currently this simply returns the component name, replacing " TS " with
" Time Series ".
@param comp_type StateMod component type.
*/
public static String lookupTimeSeriesGraphTitle ( int comp_type )
{	StateMod_DataSet dataset = new StateMod_DataSet();
	return dataset.lookupComponentName (
		comp_type ).replaceAll(" TS ", " Time Series " );
}

/**
Sorts a Vector of StateMod_Data objects, depending on the compareTo() method
for the specific object.
@param data a Vector of StateMod_Data objects.  Can be null.
@return a new sorted Vector with references to the same data objects in the
passed-in Vector.  If a null Vector is passed in, an empty Vector will be
returned.
*/
public static Vector sortStateMod_DataVector ( Vector data )
{	return sortStateMod_DataVector ( data, true );
}

/**
Sorts a Vector of StateMod_Data objects, depending on the compareTo() method
for the specific object.
@param data a Vector of StateMod_Data objects.  Can be null.
@param return_new If true, return a new Vector with references to the data.
If false, return the original Vector, with sorted contents.
@return a sorted Vector with references to the same data objects in the
passed-in Vector.  If a null Vector is passed in, an empty Vector will be
returned.
*/
public static Vector sortStateMod_DataVector ( Vector data, boolean return_new )
{	if (data == null) {
		return new Vector();
	}
	Vector v = data;
	int size = data.size();
	if ( return_new ) {
		if (size == 0) {
			return new Vector();
		}
		v = new Vector(size);
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
Run the command "statemod <response_file_name> <option>".
@param dataset Data set to get the response file from.
@param option Option to run (e.g., "-simx" for a fast simulate).
@param withGUI If true, the process manager gui will be displayed.  True should
typcially be used for model run options but is normally false when running the
StateMod report mode.
@param parent Calling JFrame, used when withGUI is true.
@exception Exception if there is an error running the command (non Stop 0 from
StateMod).
*/
public static void runStateMod (	StateMod_DataSet dataset,
					String option, 
					boolean withGUI,
					JFrame parent )
throws Exception
{	DataSetComponent comp = dataset.getComponentForComponentType(
			StateMod_DataSet.COMP_RESPONSE );
	runStateMod (	dataset.getDataFilePathAbsolute(comp.getDataFileName()),
			option, withGUI, parent, 0 );
}

/**
Run the command "statemod <response_file_name> <option>".
The response file is typically the original response file that was used to open
the data set.
@param response_file_name Response file name, with full path.
@param option Option to run (e.g., "-simx" for a fast simulate).
@param withGUI If true, the process manager gui will be displayed.  True should
typcially be used for model run options but is normally false when running the
StateMod report mode.
@param parent Calling JFrame, used when withGUI is true.
@exception Exception if there is an error running the command (non Stop 0 from
StateMod).
*/
public static void runStateMod (	String response_file_name,
					String option, 
					boolean withGUI,
					JFrame parent )
throws Exception {
	runStateMod ( response_file_name, option, withGUI, parent, 0 );
}

/**
Run the command "statemod <response_file_name> <option>".
The response file is typically the original response file that was used to open
the data set.  It can be null if not needed for the option (e.g., -help).
@param response_file_name Response file name, with full path.
@param option Option to run(parameters after the program name).
@param withGUI If true, a ProcessManagerDialog will be used.  If false, the GUI
will not be shown (although a DOS window may pop up).
@param parent Calling JFrame, used when withGUI is true.
@param wait_after Number of milliseconds to wait after running.  This is
sometimes needed to allow the output file (e.g., .x*g) file to be recognized
by the operating system.  This will not be needed if time series are read from
binary model output files but may be needed if reports are viewed immediately
after running.
@exception Exception if there is an error running the command (non Stop 0 from
StateMod).
*/
public static void runStateMod (	String response_file_name,
					String option, 
					boolean withGUI, JFrame parent,
					int wait_after )
throws Exception
{	String routine = "StateMod_Util.runStateMod";
	if ( response_file_name == null ) {
		response_file_name = "";
	}

	String command = __statemod_executable +
			" " + response_file_name +
			" " + option;
	String str;

	Message.printStatus(1, routine, "Running \"" + command + "\"");

	if ( withGUI ) {
		// Run using a process manager dialog...
		PropList props = new PropList("StateMod_Util.runStateMod");
		PropList pm_props = new PropList ("StateMod_Util.runStateMod");
		if (	option.startsWith("-update") ||
			option.startsWith("-help") ||
			option.startsWith("-version") ) {
			// Display all the output...
			props.set("BufferSize","0");
		}
		else {	// Display a reasonably large number of lines of
			// output...
			props.set("BufferSize","1000");
		}
		// Tell the process manager to check for "STOP" as an exit
		// code - this seems to work better than a process exit code.
		// Comment out for now.  The exit() calls built into the
		// statemod.exe seem to be working
		//pm_props.set("ExitStatusTokens","STOP");
		// Use the following to test the Statemod.exe with exit() codes
		// Use the array of command arguments - seems to work better...
		//String [] test = new String[3];
		//test[0] = "statemod";
		//test[1] = "-test";
		//test[2] = "-0";
		//new ProcessManagerJDialog(parent, "StateMod",
			//new ProcessManager( test, pm_props), props);
		new ProcessManagerJDialog(parent, "StateMod",
			new ProcessManager(StringUtil.toArray(
			StringUtil.breakStringList(command,
			" ", StringUtil.DELIM_SKIP_BLANKS)), pm_props), props);
	}
	else if (option.equalsIgnoreCase("-v") ||
		option.equalsIgnoreCase("-version")) {
		// Run StateMod -version and search the output for the version
		// information...
		String [] command_array = new String[2];
		command_array[0] = __statemod_executable;
		command_array[1] = "-version";
		ProcessManager sp = new ProcessManager(command_array);
		sp.saveOutput(true);
		sp.run();
		Vector output = sp.getOutputVector();
		int size = 0;
		if (output != null){
			size = output.size();	
		}
		boolean	versionFound = false;
		for (int i = 0; i < size; i++){
			str = (String)output.elementAt(i);
			if (str.indexOf("Version:") >= 0) {
				String version = StringUtil.getToken(
					str.trim()," ",
					StringUtil.DELIM_SKIP_BLANKS,1);
				// For now treat as a floating point number...
				__statemod_version = StringUtil.atof(version);
				versionFound = true;
				break;
			}
		}
		if (!versionFound) {
			Message.printWarning(1, routine,
			"Unable to determine StateMod version from version " +
			"output.\n"
			+"Model may not run and output may not be accessible.\n"
			+"Is statemod.exe in the PATH?");
			return;	// To skip sleep below.
		}
	}
	else {	// No GUI and not getting the version.
		ProcessManager sp = new ProcessManager(
			StringUtil.toArray(
			StringUtil.breakStringList(command, " \t",
				StringUtil.DELIM_SKIP_BLANKS)));
		sp.saveOutput(true);
		sp.run();
		if (sp.getExitStatus()!= 0){
			// There was an error running StateMod.
			Message.printWarning(2, routine,
			"Error running \"" + command + "\"");
			throw new Exception("Error running \"" + command +
			"\"");
		}
		Vector output = sp.getOutputVector();
		int size = 0;
		if (output != null) {
			size = output.size();	
		}
		for (int i = 0; i < size; i++) {
			// Print the output as status messages since no GUI is
			// being shown but we may want to see what is going on
			// in the log and console...
			str = (String)output.elementAt(i);
			if (str != null) {
				// This prints to the Diagnostics GUI and to
				// the console window...
				Message.printStatus(1, routine, str);
			}
		}
		// Appears that in some cases the model is not completing saving
		// its output so sleep .1 second...
		if (wait_after > 0) {
			Message.printStatus(1,"", "Waiting " + wait_after
				+ " milliseconds to let output finish.");
			TimeUtil.sleep(wait_after);
			Message.printStatus(1,"", "Done waiting.");
		}
	}
}

/**
Sets description field in each time series using supplied StateMod_Data object
identifiers.  The StateMod time series files include only the start/end period
of record, units, year type, ID and values only, no descriptions are included.
This method correlates the descriptions in the stations files with the
TimeSeries.
@param theData StateMod_Data objects from which we will use the name is used 
to fill in the description field in the time series
@param theTS vector of time series
@param mult the number of TS in a row which will use the description.  For
example, the reservoir min/max vector has two time series for each node in
theData (min and max) whereas most have a one-to-one correlation.
*/
public static void setTSDescriptions (Vector theData, Vector theTS, int mult) {
	if ((theData == null) || (theTS == null)) {
		return;
	}
	if (theData.size() == 0 || theTS.size() == 0) {
		return;
	}

	int size = theData.size();

	StateMod_Data smdata = null;
	TS ts = null;
	for (int i=0; i<size; i++) {
		smdata = (StateMod_Data)theData.elementAt(i);
		for (int j=0; j<mult; j++) {
			try {	ts = (TS)theTS.elementAt(i*mult + j);

				if (smdata.getID().equalsIgnoreCase
					(ts.getIdentifier().getLocation())) {
					ts.setDescription(smdata.getName());
				}
			} catch (Exception e) {
				Message.printWarning (2,
					"StateMod_GUIUtil.setTSDescriptions", 
					"Unable to set description for ts");
			}
		}
				
	}
	smdata = null;
	ts = null;
}

/**
Removes all the objects that match the specified object (with a compareTo() 
call) from the Vector.
@param v the Vector from which to remove the element.
@param data the object to match and remove.
*/
public static void removeFromVector(Vector v, StateMod_Data data) {
	if (v == null || v.size() == 0) {
		return;
	}
	int size = v.size();
	StateMod_Data element = null;
	for (int i = size - 1; i >= 0; i--) {
		element = (StateMod_Data)v.elementAt(i);
		if (element.compareTo(data) == 0) {
			v.remove(i);
		}
	}
}

/**
Set the program to use when running StateMod.  In general, this should just be
the program name and rely on the PATH to find.  However, a full path can be
specified to override the PATH.
*/
public static void setStateModExecutable ( String statemod_executable )
{	if ( statemod_executable != null ) {
		__statemod_executable = statemod_executable;
	}
}

/*
	COMP_RESERVOIR_AREA_CAP = -102,
*/

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
	if (componentType == StateMod_DataSet.COMP_DELAY_TABLES_DAILY) {
		return lookupDelayTableDailyPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY) {
		return lookupDelayTableMonthlyPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_DIVERSION_RIGHTS) {
		return lookupDiversionRightPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_DIVERSION_STATIONS) {
		return lookupDiversionPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_DIVERSION_STATION_COLLECTIONS) {
	    	return lookupDiversionCollectionPropValue(propType, field);
	}
	else if (componentType
	     == StateMod_DataSet.COMP_DIVERSION_STATION_DELAY_TABLES) {
	     	return lookupDiversionReturnFlowPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_INSTREAM_RIGHTS) {
		return lookupInstreamFlowRightPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_INSTREAM_STATIONS) {
		return lookupInstreamFlowPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_RESERVOIR_STATIONS) {
		return lookupReservoirPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_RESERVOIR_STATION_ACCOUNTS) {
		return lookupReservoirAccountPropValue(propType, field);
	}
	else if (componentType == StateMod_Util.COMP_RESERVOIR_AREA_CAP) {	
		return lookupReservoirAreaCapPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_RESERVOIR_STATION_PRECIP_STATIONS) {
		return lookupReservoirPrecipStationPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_RESERVOIR_STATION_EVAP_STATIONS) {
		return lookupReservoirEvapStationPropValue(propType, field);
	}	
	else if (componentType 
	    == StateMod_DataSet.COMP_RESERVOIR_STATION_COLLECTIONS) {
	    	return lookupReservoirCollectionPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_RESERVOIR_RIGHTS) {	
		return lookupReservoirRightPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_RIVER_NETWORK) {
		return lookupRiverNodePropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS) {
	    	return lookupStreamEstimatePropValue(propType, field);
	}
	else if (componentType
	    == StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS) {
	    	return lookupStreamEstimateCoefficientPropValue(propType,field);
	}
	else if (componentType == StateMod_DataSet.COMP_STREAMGAGE_STATIONS) {
		return lookupStreamGagePropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_WELL_STATIONS) {
		return lookupWellPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_WELL_STATION_COLLECTIONS) {
	    	return lookupWellCollectionPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_WELL_STATION_DEPLETION_TABLES) {
		return lookupWellDepletionPropValue(propType, field);
	}
	else if (componentType 
	    == StateMod_DataSet.COMP_WELL_STATION_DELAY_TABLES) {
	    	return lookupWellReturnFlowPropValue(propType, field);
	}
	else if (componentType == StateMod_DataSet.COMP_WELL_RIGHTS) {
		return lookupWellRightPropValue(propType, field);
	}
	
	return null;
}

/**
Returns property values for daily delay tables.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDelayTableDailyPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY TABLE ID";
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "DAY";
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {
			return "PERCENT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY\nTABLE ID";
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "\nDAY";
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {
			return "\nPERCENT";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "%-12.12s";	
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "%8d";	
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {
			return "%12.6f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "";
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {	
			return "";
		}
	}

	return null;
}

/**
Returns property values for monthly delay tables.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDelayTableMonthlyPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY TABLE ID";
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "MONTH";
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {
			return "PERCENT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY\nTABLE ID";
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "\nMONTH";
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {
			return "\nPERCENT";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "%-12.12s";	
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "%8d";	
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {
			return "%12.6f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("DelayTableID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Date")) {
			return "";
		}
		else if (field.equalsIgnoreCase("ReturnAmount")) {	
			return "";
		}
	}

	return null;
}

/**
Returns property values for diversions
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDiversionPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "CAPACITY (CFS)";
		}
		else if (field.equalsIgnoreCase("ReplaceResOption")) {
			return "REPLACE. RES. OPTION";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY ID";
		}
		else if (field.equalsIgnoreCase("UserName")) {
			return "USER NAME";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "DEMAND TYPE";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "AREA (ACRE)";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "USE TYPE";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "DEMAND SOURCE";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "EFFICIENCY ANNUAL (%)";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "EFFICIENCY MONTH 1";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "EFFICIENCY MONTH 2";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "EFFICIENCY MONTH 3";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "EFFICIENCY MONTH 4";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "EFFICIENCY MONTH 5";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "EFFICIENCY MONTH 6";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "EFFICIENCY MONTH 7";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "EFFICIENCY MONTH 8";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "EFFICIENCY MONTH 9";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "EFFICIENCY MONTH 10";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "EFFICIENCY MONTH 11";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {
			return "EFFICIENCY MONTH 12";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nNAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF\nSWITCH";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "CAPACITY\n(CFS)";
		}
		else if (field.equalsIgnoreCase("ReplaceResOption")) {
			return "REPLACE.\nRES. OPTION";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY\nID";
		}
		else if (field.equalsIgnoreCase("UserName")) {
			return "USER\nNAME";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "DEMAND\nTYPE";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "AREA\n(ACRE)";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "USE\nTYPE";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "DEMAND\nSOURCE";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "EFFICIENCY\nANNUAL (%)";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "EFFICIENCY\nMONTH 1";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "EFFICIENCY\nMONTH 2";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "EFFICIENCY\nMONTH 3";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "EFFICIENCY\nMONTH 4";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "EFFICIENCY\nMONTH 5";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "EFFICIENCY\nMONTH 6";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "EFFICIENCY\nMONTH 7";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "EFFICIENCY\nMONTH 8";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "EFFICIENCY\nMONTH 9";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "EFFICIENCY\nMONTH 10";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "EFFICIENCY\nMONTH 11";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {
			return "EFFICIENCY\nMONTH 12";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12.s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("ReplaceResOption")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("UserName")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "%8.1f";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {	
			return "%10.2f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The diversion station identifier is the "
				+ "main link between diversion data<BR>" 
				+ "and must be unique in the data set.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Diversion station name.";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "River node where diversion station is located.";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "Indicates whether diversion station is on (1) "
				+ "or off (0)";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "Diversion station capacity (CFS)";
		}
		else if (field.equalsIgnoreCase("ReplaceResOption")) {
			return "Replacement reservoir option.";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "Daily identifier (for daily time series).";
		}
		else if (field.equalsIgnoreCase("UserName")) {
			return "User name.";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "(Monthly) demand type.";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "Irrigated area (ACRE).";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "Use type.";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "Demand source.";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "Efficiency, annual (%).  Negative indicates "
				+ "monthly efficiencies.";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "Diversion efficiency for month 1 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "Diversion efficiency for month 2 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "Diversion efficiency for month 3 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "Diversion efficiency for month 4 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "Diversion efficiency for month 5 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "Diversion efficiency for month 6 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "Diversion efficiency for month 7 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "Diversion efficiency for month 8 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "Diversion efficiency for month 9 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "Diversion efficiency for month 10 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "Diversion efficiency for month 11 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {	
			return "Diversion efficiency for month 12 of year.";
		}
	}

	return null;
}

/**
Returns property values for diversion collections.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDiversionCollectionPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "DIVERSION ID";
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
			return "DIVERSION\nID";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "\nYEAR";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "COLLECTION\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "PART\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartID")) {
			return "PART\nID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PartID")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("LocationID")) {
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

	return null;
}

/**
Returns property values for diversion return flows.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDiversionReturnFlowPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "DIVERSION STATION ID";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE ID RECEIVING RETURN FLOW";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "PERCENT";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY TABLE ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nDIVERSION\nSTATION\nID";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER\nNODE ID\nRECEIVING\nRETURN\nFLOW";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "\n\n\n\nPERCENT";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "\n\n\nDELAY\nTABLE ID";
		}
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "%12.6f";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "%8d";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "Associated diversion station ID.";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "River node ID receiving return flow.";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "% of return (0-100)";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "Delay table identifier";
		}	
	}

	return null;
}

/**
Returns property values for diversion rights
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupDiversionRightPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "DIVERSION RIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "DIVERSION RIGHT NAME";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "DIVERSION ID ASSOCIATED WITH RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "ADMINISTRATION NUMBER";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "DECREE AMOUNT (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nDIVERSION\nRIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nDIVERSION RIGHT\nNAME";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "DIVERSION ID\nASSOCIATED\nWITH RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "\nADMINISTRATION\nNUMBER";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "DECREE\nAMOUNT\n(CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\nON/OFF\nSWITCH";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "%12.2f";
		}
		else if (field.equalsIgnoreCase("OnOff")) {	
			return "%8d";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The diversion right ID is typically the "
				+ "diversion station ID<br> followed by .01, "
				+ ".02, etc.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Diversion right name";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "<HTML>The diversion ID is the link between "
				+ "diversion stations and their "
				+ "right(s).</HTML>";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "<HTML>Lower admininistration numbers indicate "
				+ "greater seniority.<BR>99999 is typical for "
				+ "a very junior right.</html>";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "Decree amount (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {	
			return "<HTML>0 = OFF<BR>1 = ON<BR>" 
				+ "YYYY indicates to turn on the right in "
				+ "year YYYY.<BR>-YYYY indicates to turn off "
				+ "the right in year YYYY.</HTML>";
		}
	}

	return null;
}

/**
Returns property values for instream flows.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupInstreamFlowPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("UpstreamRiverNodeID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
		else if (field.equalsIgnoreCase("DownstreamRiverNodeID")) {
			return "DOWNSTREAM RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY ID";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "DEMAND TYPE";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\n\nNAME";
		}
		else if (field.equalsIgnoreCase("UpstreamRiverNodeID")) {
			return "\nRIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\nON/OFF\nSWITCH";
		}
		else if (field.equalsIgnoreCase("DownstreamRiverNodeID")) {
			return "DOWNSTREAM\nRIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "\n\nDAILY ID";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "\nDEMAND\nTYPE";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("UpstreamRiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DownstreamRiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("DemandType")) {	
			return "%8d";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The instream flow identifier is the "
				+ "main link between instream data data<BR>"
				+ "and must be unique in the data set.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "<html>Instream flow name.</html>";
		}
		else if (field.equalsIgnoreCase("UpstreamRiverNodeID")) {
			return "Upstream river ID where instream flow "
				+ "is located.";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "<html>Switch.<br>0 = off<br>1 = on</html";
		}
		else if (field.equalsIgnoreCase("DownstreamRiverNodeID")) {
			return "<html>Daily instream flow ID.</html>";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "<html>Downstream river node, for instream flow "
				+ "reach.</html>";
		}
		else if (field.equalsIgnoreCase("DemandType")) {	
			return "<html>Data type switch.</html>";
		}
	}

	return null;
}

/**
Returns property values for instream flow rights.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupInstreamFlowRightPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "RIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "RIGHT NAME";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "INSTREAM FLOW STATION ID ASSOCIATED"
				+ " WITH RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "ADMINISTRATION NUMBER";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "DECREED AMOUNT (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\n\nRIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\n\n\nRIGHT NAME";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "INSTREAM FLOW\nSTATION ID\nASSOCIATED"
				+ "\nWITH RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "\n\nADMINISTRATION\nNUMBER";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "\n\nDECREED\nAMOUNT (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\n\nON/OFF\nSWITCH";
		}
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "%8.2f";
		}
		else if (field.equalsIgnoreCase("OnOff")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The instream flow right ID is typically "
				+ "the instream flow ID<br> followed by .01, "
				+ ".02, etc.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Instream flow right name.";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "<HTML>The instream flow ID is the link between "
				+ "instream  flows and their right<BR>(not "
				+ "editable here).</HTML>";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "<HTML>Lower admininistration numbers indicate "
				+ "greater seniority.<BR>99999 is typical for "
				+ "a very junior right.</html>";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "Decreed amount (CFS).";
		}
		else if (field.equalsIgnoreCase("OnOff")) {	
			return "<HTML>0 = OFF<BR>1 = ON<BR>"
				+ "YYYY indicates to turn on the right in year "
				+ "YYYY.<BR>-YYYY indicates to turn off the "
				+ "right in year YYYY.</HTML>";
		}
	}

	return null;
}

/**
Returns property values for reservoirs.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
		else if (field.equalsIgnoreCase("OneFillRule")) {
			return "ONE FILL DATE";
		}
		else if (field.equalsIgnoreCase("ContentMin")) {
			return "MIN CONTENT (ACFT)";
		}
		else if (field.equalsIgnoreCase("ContentMax")) {
			return "MAX CONTENT (ACFT)";
		}
		else if (field.equalsIgnoreCase("ReleaseMax")) {
			return "MAX RELEASE (CFS)";
		}
		else if (field.equalsIgnoreCase("DeadStorage")) {
			return "DEAD STORAGE (ACFT)";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY ID";
		}
		else if (field.equalsIgnoreCase("NumOwners")) {
			return "NUMBER OF OWNERS";
		}
		else if (field.equalsIgnoreCase("NumPrecipStations")) {
			return "NUMBER OF PRECIP. STATIONS";
		}
		else if (field.equalsIgnoreCase("NumEvapStations")) {
			return "NUMBER OF EVAP. STATIONS";
		}
		else if (field.equalsIgnoreCase("NumCurveRows")) {
			return "NUMBER OF CURVE ROWS";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\n\nNAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "\nRIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\nON/OFF\nSWITCH";
		}
		else if (field.equalsIgnoreCase("OneFillRule")) {
			return "ONE\nFILL\nDATE";
		}
		else if (field.equalsIgnoreCase("ContentMin")) {
			return "MIN\nCONTENT\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("ContentMax")) {
			return "MAX\nCONTENT\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("ReleaseMax")) {
			return "MAX\nRELEASE\n(CFS)";
		}
		else if (field.equalsIgnoreCase("DeadStorage")) {
			return "DEAD\nSTORAGE\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "\nDAILY\nID";
		}
		else if (field.equalsIgnoreCase("NumOwners")) {
			return "NUMBER\nOF\nOWNERS";
		}
		else if (field.equalsIgnoreCase("NumPrecipStations")) {
			return "NUMBER\nOF PRECIP.\nSTATIONS";
		}
		else if (field.equalsIgnoreCase("NumEvapStations")) {
			return "NUMBER\nOF EVAP.\nSTATIONS";
		}
		else if (field.equalsIgnoreCase("NumCurveRows")) {
			return "NUMBER\nOF CURVE\nROWS";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("OneFillRule")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("ContentMin")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("ContentMax")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("ReleaseMax")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("DeadStorage")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("NumOwners")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("NumPrecipStations")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("NumEvapStations")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("NumCurveRows")) {	
			return "%8d";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The reservoir station identifier is the "
				+ "main link between reservoir data<BR>" 
				+ "and must be unique in the data set.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Reservoir station name.";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "Node where reservoir is located.";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "<html>Switch.<br>0 = off<br>1 = on</html>";
		}
		else if (field.equalsIgnoreCase("OneFillRule")) {
			return "<html>Date for one fill rule admin.</html>";
		}
		else if (field.equalsIgnoreCase("ContentMin")) {
			return "<html>Minimum reservoir content (ACFT).</html>";
		}
		else if (field.equalsIgnoreCase("ContentMax")) {
			return "<html>Maximum reservoir content (ACFT).</html>";
		}
		else if (field.equalsIgnoreCase("ReleaseMax")) {
			return "<html>Maximum release (CFS).</html>";
		}
		else if (field.equalsIgnoreCase("DeadStorage")) {
			return "<html>Dead storage in reservoir (ACFT).</html>";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "Identifier for daily time series.";
		}
		else if (field.equalsIgnoreCase("NumOwners")) {
			return "Number of owners.";
		}
		else if (field.equalsIgnoreCase("NumPrecipStations")) {
			return "Number of precipitation stations.";
		}
		else if (field.equalsIgnoreCase("NumEvapStations")) {
			return "Number of evaporation stations.";
		}
		else if (field.equalsIgnoreCase("NumCurveRows")) {	
			return "Number of curve rows.";
		}
	}

	return null;
}

/**
Returns property values for reservoir accounts.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirAccountPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR ID";
		}
		else if (field.equalsIgnoreCase("OwnerID")) {
			return "OWNER ID";
		}
		else if (field.equalsIgnoreCase("OwnerAccount")) {
			return "OWNER ACCOUNT";
		}
		else if (field.equalsIgnoreCase("MaxStorage")) {
			return "MAXIMUM STORAGE (ACFT)";
		}
		else if (field.equalsIgnoreCase("InitialStorage")) {
			return "INITIAL STORAGE (ACFT)";
		}
		else if (field.equalsIgnoreCase("ProrateEvap")) {
			return "EVAPORATION DISTRIBUTION FLAG";
		}
		else if (field.equalsIgnoreCase("OwnershipTie")) {
			return "ACCOUNT ONE FILL CALCULATION";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "\nRESERVOIR\nID";
		}
		else if (field.equalsIgnoreCase("OwnerID")) {
			return "\nOWNER\nID";
		}
		else if (field.equalsIgnoreCase("OwnerAccount")) {
			return "\nOWNER\nACCOUNT";
		}
		else if (field.equalsIgnoreCase("MaxStorage")) {
			return "MAXIMUM\nSTORAGE\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("InitialStorage")) {
			return "INITIAL\nSTORAGE\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("ProrateEvap")) {
			return "EVAPORATION\nDISTRIBUTION\nFLAG";
		}
		else if (field.equalsIgnoreCase("OwnershipTie")) {
			return "ACCOUNT\nONE FILL\nCALCULATION";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("OwnerID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("OwnerAccount")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("MaxStorage")) {
			return "%12.1f";
		}
		else if (field.equalsIgnoreCase("InitialStorage")) {
			return "%12.1f";
		}
		else if (field.equalsIgnoreCase("ProrateEvap")) {
			return "%8.0f";
		}
		else if (field.equalsIgnoreCase("OwnershipTie")) {	
			return "%8d";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "<html>The reservoir station ID of the "
				+ "reservoir to which<br>the rights "
				+ "belong.</html>";
		}
		else if (field.equalsIgnoreCase("OwnerID")) {
			return "Sequential number 1+ (not used by StateMod)";
		}
		else if (field.equalsIgnoreCase("OwnerAccount")) {
			return "Account name.";
		}
		else if (field.equalsIgnoreCase("MaxStorage")) {
			return "Maximum account storage (ACFT).";
		}
		else if (field.equalsIgnoreCase("InitialStorage")) {
			return "Initial account storage (ACFT).";
		}
		else if (field.equalsIgnoreCase("ProrateEvap")) {
			return "How to prorate evaporation.";
		}
		else if (field.equalsIgnoreCase("OwnershipTie")) {	
			return "One fill rule calculation flag.";
		}
	}

	return null;
}

/**
Returns property values for reservoir area caps.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirAreaCapPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR ID";
		}
		else if (field.equalsIgnoreCase("Content")) {
			return "CONTENT (ACFT)";
		}
		else if (field.equalsIgnoreCase("Area")) {
			return "AREA (ACRE)";
		}
		else if (field.equalsIgnoreCase("Seepage")) {
			return "SEEPAGE (AF/M)";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR\nID";
		}
		else if (field.equalsIgnoreCase("Content")) {
			return "CONTENT\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("Area")) {
			return "AREA\n(ACRE)";
		}
		else if (field.equalsIgnoreCase("Seepage")) {
			return "SEEPAGE\n(AF/M)";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Content")) {
			return "%12.1f";
		}
		else if (field.equalsIgnoreCase("Area")) {
			return "%12.1f";
		}
		else if (field.equalsIgnoreCase("Seepage")) {	
			return "%12.1f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "<html>The reservoir station ID of the "
				+ "reservoir to which<br>the area capacity "
				+ "information belongs.</html>";
		}
		else if (field.equalsIgnoreCase("Content")) {
			return "Reservoir content (ACFT).";
		}
		else if (field.equalsIgnoreCase("Area")) {
			return "Reservoir area (ACRE).";
		}
		else if (field.equalsIgnoreCase("Seepage")) {	
			return "Reservoir seepage (AF/M).";
		}
	}

	return null;
}

/**
Returns property values for reservoir precipitation stations.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirPrecipStationPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR ID";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "STATION ID";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {
			return "WEIGHT (%)";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR ID";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "STATION ID";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {
			return "WEIGHT (%)";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {	
			return "%12.1f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "<html>The reservoir station ID of the "
				+ "reservoir to which<br>the climate data "
				+ "belong.</html>";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "Station identifier.";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {	
			return "Weight for station's data (%).";
		}
	}

	return null;
}

/**
Returns property values for reservoir evaporation stations.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirEvapStationPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR ID";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "STATION ID";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {
			return "WEIGHT (%)";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "RESERVOIR ID";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "STATION ID";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {
			return "WEIGHT (%)";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {	
			return "%12.1f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ReservoirID")) {
			return "<html>The reservoir station ID of the "
				+ "reservoir to which<br>the climate data "
				+ "belong.</html>";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "Station identifier.";
		}
		else if (field.equalsIgnoreCase("PercentWeight")) {	
			return "Weight for station's data (%).";
		}
	}

	return null;
}

/**
Returns property values for reservoir collections.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirCollectionPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "RESERVOIR ID";
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
			return "RESERVOIR\nID";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "\nYEAR";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "COLLECTION\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "PART\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartID")) {
			return "PART\nID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PartID")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("LocationID")) {
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

	return null;
}

/**
Returns property values for reservoir rights.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupReservoirRightPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "RIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "RIGHT NAME";
		}
		else if (field.equalsIgnoreCase("StructureID")) {
			return "RESERVOIR STATION ID ASSOC. W/ RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "ADMINISTRATION NUMBER";
		}
		else if (field.equalsIgnoreCase("DecreedAmount")) {
			return "DECREE AMOUNT (ACFT)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
		else if (field.equalsIgnoreCase("AccountDistribution")) {
			return "ACCOUNT DISTRIBUTION";
		}
		else if (field.equalsIgnoreCase("Type")) {
			return "RIGHT TYPE";
		}
		else if (field.equalsIgnoreCase("FillType")) {
			return "FILL TYPE";
		}
		else if (field.equalsIgnoreCase("OopRight")) {
			return "OUT OF PRIORITY RIGHT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nRIGHT\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nRIGHT\nNAME";
		}
		else if (field.equalsIgnoreCase("StructureID")) {
			return "RESERVOIR\nSTATION ID\nASSOC. W/ RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "\nADMINISTRATION\nNUMBER";
		}
		else if (field.equalsIgnoreCase("DecreedAmount")) {
			return "DECREE\nAMOUNT\n(ACFT)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\nON/OFF\nSWITCH";
		}
		else if (field.equalsIgnoreCase("AccountDistribution")) {
			return "\nACCOUNT\nDISTRIBUTION";
		}
		else if (field.equalsIgnoreCase("Type")) {
			return "\n\nRIGHT TYPE";
		}
		else if (field.equalsIgnoreCase("FillType")) {
			return "\n\nFILL TYPE";
		}
		else if (field.equalsIgnoreCase("OopRight")) {
			return "OUT OF\nPRIORITY\nRIGHT";
		}
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("StructureID")) {
			return "%-8.8s";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "%-20.20s";
		}
		else if (field.equalsIgnoreCase("DecreedAmount")) {
			return "%12.1f";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("AccountDistribution")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("Type")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("FillType")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("OopRight")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The reservoir right ID is typically the "
				+ "reservoir station ID<br> followed by .01, "
				+ ".02, etc.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Reservoir right name";
		}
		else if (field.equalsIgnoreCase("StructureID")) {
			return "<HTML>The reservoir ID is the link between "
				+ "reservoir stations and their "
				+ "right(s).</HTML>";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "<HTML>Lower admininistration numbers indicate "
				+ "greater seniority.<BR>99999 is typical for "
				+ "a very junior right.</html>";
		}
		else if (field.equalsIgnoreCase("DecreedAmount")) {
			return "Decreed amount (ACFT)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "<HTML>0 = OFF<BR>1 = ON<BR>" 
				+ "YYYY indicates to turn on the right in year "
				+ "YYYY.<BR>-YYYY indicates to turn off the "
				+ "right in year YYYY.</HTML>";
		}
		else if (field.equalsIgnoreCase("AccountDistribution")) {
			return "Account distribution switch.";
		}
		else if (field.equalsIgnoreCase("Type")) {
			return "Right type.";
		}
		else if (field.equalsIgnoreCase("FillType")) {
			return "Fill type.";
		}
		else if (field.equalsIgnoreCase("OopRight")) {	
			return "Out-of-priority associated operational right.";
		}
	}

	return null;
}

/**
Returns property values for river nodes.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupRiverNodePropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "STATION NAME";
		}
		else if (field.equalsIgnoreCase("DownstreamID")) {
			return "DOWNSTREAM RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("Comment")) {
			return "COMMENT";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "STATION NAME";
		}
		else if (field.equalsIgnoreCase("DownstreamID")) {
			return "DOWNSTREAM RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("Comment")) {
			return "COMMENT";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("DownstreamID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Comment")) {	
			return "%-80.80s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("DownstreamID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Comment")) {	
			return "";
		}
	}

	return null;
}

/**
Returns property values for stream estimate stations.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupStreamEstimatePropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nNAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY\nID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("DailyID")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("DailyID")) {	
			return "";
		}
	}

	return null;
}

/**
Returns property values for stream estimate coefficients.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupStreamEstimateCoefficientPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "STREAM TERM";
		}
		else if (field.equalsIgnoreCase("UpstreamGage")) {
			return "UPSTREAM TERM GAGE";
		}
		else if (field.equalsIgnoreCase("ProrationFactor")) {
			return "GAIN TERM PRORATION FACTOR";
		}
		else if (field.equalsIgnoreCase("Weight")) {
			return "GAIN TERM WEIGHT";
		}
		else if (field.equalsIgnoreCase("GageID")) {
			return "GAIN TERM GAGE ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nSTREAM\nTERM";
		}
		else if (field.equalsIgnoreCase("UpstreamGage")) {
			return "\nUPSTREAM\nTERM GAGE";
		}
		else if (field.equalsIgnoreCase("ProrationFactor")) {
			return "GAIN TERM\nPRORATION\nFACTOR";
		}
		else if (field.equalsIgnoreCase("Weight")) {
			return "\nGAIN TERM\nWEIGHT";
		}
		else if (field.equalsIgnoreCase("GageID")) {
			return "\nGAIN TERM\nGAGE ID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%8.1f";
		}
		else if (field.equalsIgnoreCase("UpstreamGage")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("ProrationFactor")) {
			return "%8.1f";
		}
		else if (field.equalsIgnoreCase("Weight")) {
			return "%8.1f";
		}
		else if (field.equalsIgnoreCase("GageID")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("UpstreamGage")) {
			return "";
		}
		else if (field.equalsIgnoreCase("ProrationFactor")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Weight")) {
			return "";
		}
		else if (field.equalsIgnoreCase("GageID")) {	
			return "";
		}
	}

	return null;
}

/**
Returns property values for stream gages.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupStreamGagePropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("NodeID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\nNAME";
		}
		else if (field.equalsIgnoreCase("NodeID")) {
			return "RIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY\nID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("NodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("DailyID")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "";
		}
		else if (field.equalsIgnoreCase("NodeID")) {
			return "";
		}
		else if (field.equalsIgnoreCase("DailyID")) {	
			return "";
		}
	}

	return null;
}

/**
Returns property values for wells.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupWellPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "NAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "CAPACITY (CFS) ";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "DAILY ID";
		}
		else if (field.equalsIgnoreCase("Primary")) {
			return "ADMINISTRATION NUMBER SWITCH";
		}
		else if (field.equalsIgnoreCase("DiversionID")) {
			return "RELATED DIVERSION ID";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "DATA TYPE";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "ANNUAL EFFICIENCY (PERCENT)";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "WELL IRRIGATED AREA (ACRE)";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "USE TYPE";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "DEMAND SOURCE";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "EFFICIENCY MONTH 1 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "EFFICIENCY MONTH 2 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "EFFICIENCY MONTH 3 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "EFFICIENCY MONTH 4 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "EFFICIENCY MONTH 5 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "EFFICIENCY MONTH 6 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "EFFICIENCY MONTH 7 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "EFFICIENCY MONTH 8 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "EFFICIENCY MONTH 9 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "EFFICIENCY MONTH 10 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "EFFICIENCY MONTH 11 (PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {
			return "EFFICIENCY MONTH 12 (PERCENT)";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\n\nNAME";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "\nRIVER\nNODE ID";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\nON/OFF\nSWITCH";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "\nCAPACITY\n(CFS)\n";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "\nDAILY\nID";
		}
		else if (field.equalsIgnoreCase("Primary")) {
			return "ADMINISTRATION\nNUMBER\nSWITCH";
		}
		else if (field.equalsIgnoreCase("DiversionID")) {
			return "\nRELATED\nDIVERSION ID";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "\nDATA\nTYPE";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "ANNUAL\nEFFICIENCY\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "WELL\nIRRIGATED\nAREA (ACRE)";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "\nUSE\nTYPE";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "\nDEMAND\nSOURCE";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "EFFICIENCY\nMONTH 1\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "EFFICIENCY\nMONTH 2\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "EFFICIENCY\nMONTH 3\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "EFFICIENCY\nMONTH 4\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "EFFICIENCY\nMONTH 5\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "EFFICIENCY\nMONTH 6\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "EFFICIENCY\nMONTH 7\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "EFFICIENCY\nMONTH 8\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "EFFICIENCY\nMONTH 9\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "EFFICIENCY\nMONTH 10\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "EFFICIENCY\nMONTH 11\n(PERCENT)";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {
			return "EFFICIENCY\nMONTH 12\n(PERCENT)";
		}	
	}
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Primary")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("DiversionID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "%10.2f";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {	
			return "%10.2f";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The well station identifier is the main "
				+ "link between well data<BR>and must be "
				+ "unique in the data set.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Well station name.";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "River node where well station is located.";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "Indicates whether well station is on (1) "
				+ "or off (0)";	
		}
		else if (field.equalsIgnoreCase("Capacity")) {
			return "<html>Well capacity (CFS)</html>";
		}
		else if (field.equalsIgnoreCase("DailyID")) {
			return "<html>Well ID to use for daily data.</html>";
		}
		else if (field.equalsIgnoreCase("Primary")) {
			return "<html>Priority switch.</html>";
		}
		else if (field.equalsIgnoreCase("DiversionID")) {
			return "<html>Diversion this well is tied to.</html>";
		}
		else if (field.equalsIgnoreCase("DemandType")) {
			return "<html>Demand code.</html>";
		}
		else if (field.equalsIgnoreCase("EffAnnual")) {
			return "<html>System efficiency (%).</html>";
		}
		else if (field.equalsIgnoreCase("IrrigatedAcres")) {
			return "<html>Irrigated area associated with the "
				+ "well.</html>";
		}
		else if (field.equalsIgnoreCase("UseType")) {
			return "<html>Use type.</html>";
		}
		else if (field.equalsIgnoreCase("DemandSource")) {
			return "<html>Irrigated acreage source.</html>";
		}
		else if (field.equalsIgnoreCase("EffMonthly01")) {
			return "Well efficiency for month 1 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly02")) {
			return "Well efficiency for month 2 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly03")) {
			return "Well efficiency for month 3 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly04")) {
			return "Well efficiency for month 4 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly05")) {
			return "Well efficiency for month 5 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly06")) {
			return "Well efficiency for month 6 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly07")) {
			return "Well efficiency for month 7 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly08")) {
			return "Well efficiency for month 8 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly09")) {
			return "Well efficiency for month 9 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly10")) {
			return "Well efficiency for month 10 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly11")) {
			return "Well efficiency for month 11 of year.";
		}
		else if (field.equalsIgnoreCase("EffMonthly12")) {	
			return "Well efficiency for month 12 of year.";
		}
	}

	return null;
}

/**
Returns property values for well collections.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupWellCollectionPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "WELL ID";
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
			return "WELL\nID";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "\nYEAR";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "COLLECTION\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "PART\nTYPE";
		}
		else if (field.equalsIgnoreCase("PartID")) {
			return "PART\nID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("LocationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Year")) {
			return "%8d";
		}
		else if (field.equalsIgnoreCase("CollectionType")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PartType")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("PartID")) {	
			return "%-12.12s";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("LocationID")) {
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

	return null;
}

/**
Returns property values for well depletions.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupWellDepletionPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "WELL ID";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE BEING DEPLETED";
		}
		else if (field.equalsIgnoreCase("DepletionPercent")) {
			return "PERCENT";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY TABLE ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\nWELL\nSTATION\nID";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER\nNODE ID\nBEING\nDEPLETED";
		}
		else if (field.equalsIgnoreCase("DepletionPercent")) {
			return "\n\n\nPERCENT";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "\n\nDELAY\nTABLE ID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("DepletionPercent")) {
			return "%12.6f";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "%8d";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "Associated well station ID.";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "River node ID being depleted.";
		}
		else if (field.equalsIgnoreCase("DepletionPercent")) {
			return "% of depletion (0-100)";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "Delay table identifier";
		}	
	}

	return null;
}

/**
Returns property values for well return flows.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupWellReturnFlowPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "WELL ID";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER NODE ID RECEIVING RETURN FLOW";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "PERCENT";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "DELAY TABLE ID";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nWELL\nSTATION\nID";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "RIVER\nNODE ID\nRECEIVING\nRETURN\nFLOW";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "\n\n\n\nPERCENT";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "\n\n\nDELAY\nTABLE ID";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "%12.6f";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "%8d";
		}	
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "Associated well station ID.";
		}
		else if (field.equalsIgnoreCase("RiverNodeID")) {
			return "River node ID receiving return flow.";
		}
		else if (field.equalsIgnoreCase("ReturnPercent")) {
			return "% of return (0-100)";
		}
		else if (field.equalsIgnoreCase("DelayTableID")) {
			return "Delay table identifier";
		}	
	}

	return null;
}

/**
Returns property values for well rights.
@param propType the property to look up.  One of "FieldName", 
"FieldNameHeader", "ToolTip", or "Format".  
@param field the field for which to return the property.  
@return the property, or if it could not be found null will be returned.
*/
private static String lookupWellRightPropValue(String propType, 
String field) {
	if (propType.equalsIgnoreCase("FieldName")) {
		if (field.equalsIgnoreCase("ID")) {
			return "RIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "WELL RIGHT NAME";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "WELL ID ASSOCIATED W/ RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "ADMINISTRATION NUMBER";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "DECREED AMOUNT (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "ON/OFF SWITCH";
		}
	}
	else if (propType.equalsIgnoreCase("FieldNameHeader")) {	
		if (field.equalsIgnoreCase("ID")) {
			return "\n\nRIGHT ID";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "\n\nWELL RIGHT NAME";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "WELL ID\nASSOCIATED\nW/ RIGHT";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "\nADMINISTRATION\nNUMBER";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "\nDECREED\nAMOUNT (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {
			return "\nON/OFF\nSWITCH";
		}	
	}	
	else if (propType.equalsIgnoreCase("Format")) {
		if (field.equalsIgnoreCase("ID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "%-24.24s";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "%-12.12s";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "%12.2f";
		}
		else if (field.equalsIgnoreCase("OnOff")) {	
			return "%8d";
		}
	}	
	else if (propType.equalsIgnoreCase("ToolTip")) {
		if (field.equalsIgnoreCase("ID")) {
			return "<html>The well right ID is typically the well" 
				+ " station ID<br> followed by .01, .02, "
				+ "etc.</html>";
		}
		else if (field.equalsIgnoreCase("Name")) {
			return "Well right name";
		}
		else if (field.equalsIgnoreCase("StationID")) {
			return "<HTML>The well ID is the link between well "
				+ "stations and their right(s).</HTML>";
		}
		else if (field.equalsIgnoreCase("AdministrationNumber")) {
			return "<HTML>Lower admininistration numbers indicate "
				+ "greater seniority.<BR>99999 is typical for "
				+ "a very junior right.</html>";
		}
		else if (field.equalsIgnoreCase("Decree")) {
			return "Decreed amount (CFS)";
		}
		else if (field.equalsIgnoreCase("OnOff")) {	
			return "<HTML>0 = OFF<BR>1 = ON<BR>" 
				+ "YYYY indicates to turn on the right in "
				+ "year YYYY.<BR>-YYYY indicates to turn off "
				+ "the right in year YYYY.</HTML>";
		}
	}

	return null;
}

}
