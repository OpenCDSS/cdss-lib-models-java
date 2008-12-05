//------------------------------------------------------------------------------
// StateMod_GraphNode - Created to store an array of graph options.  This 
//	includes:
//		* station type ( diversion, instream flow, reservoir, stream),
//		* ID
//		* data type (variable options: See the SMGUI documentation).
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 19 Aug 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 04 Mar 1998	CEN, RTi		Added scenario, wrote SMParseGraphFile>
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 18 Feb 2001	Steven A. Malers, RTi	Code review.  Add finalize().  Handle
//					nulls and set unused variables to null.
//					Alphabetize methods.  Change IO to
//					IOUtil.
// 19 Jul 2001	SAM, RTi		Update to use the StateMod version to
//					determine the columns for output.
//					Leave out wells from the visible choice
//					for now.
// 13 Aug 2001	SAM, RTi		Change so when writing graph template
//					there are no single quotes around the
//					location.  Enable wells to output.
// 2001-12-27	SAM, RTi		Update to use new fixedRead() to
//					improve performance.
// 2002-05-01	SAM, RTi		Add getNodeTypes() as newer version of
//					getGraphTypes().
// 2002-06-20	SAM, RTi		Update getGraphTypes() to default to new
//					StateMod if version is not known and
//					add a flag indicating whether to return
//					all types.
// 2002-08-05	SAM, RTi		Update SMWriteDelpltFile() to have more
//					current information.  Pass the file name
//					to SMDumpDelpltFile() so that it can be
//					shown in the header.  Change "instream
//					flow" to "instream" to be consistent
//					with Delplt.
// 2002-09-16	SAM, RTi		Change historical streamflow data type
//					from "Historical" to "StreamflowHist".
//					This allows "StreamflowBase" to be
//					added as appropriate.  Overload
//					getGraphDataType() to accept a flag
//					indicating whether a node is a base
//					flow node.
//------------------------------------------------------------------------------
// 2003-07-07	J. Thomas Sapienza, RTi	Renamed to StateMod_GraphNode
// 2003-08-25	JTS, RTi		* Renamed SMParseDelpltFile to 
//					  readStateModDelPltFile.
//					* Renamed SMWriteDelpltFile to 
//					  writeStateModDelPltFile.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.TS.TSIdent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_GraphNode {

// Note that the following lists are set up as newline delimited lists to
// support the grid.

public static final int TYPES = 5;
public static final int typesSize = 5;
public static final String typesOptions =	"Diversion\n" +
						"Instream Flow\n" +
						"Reservoir\n" +
						"Streamflow\n" +
						"Well";
public static final String[] typesOptionsArray = {
						"Diversion",
						"Instream Flow",
						"Reservoir",
						"Streamflow",
						"Well" };

public static final String[] node_types = {	"Diversion",
						"Instream Flow",
						"Reservoir",
						"Streamflow",
						"Well" };

public static final int STREAM_TYPE = 0;
public static final int streamOptionsSize_0100 = 7;
public static final String[] streamOptions_0100 = {
	"UpstreamInflow",
	"ReachGain",
	"ReturnFlow",
	"RiverInflow",
	"RiverDivert",
	"RiverOutflow",
	"StreamflowHist" };

public static final int streamOptionsSize_0901 = 8;
public static final String[] streamOptions_0901 = {
	"UpstreamInflow",
	"ReachGain",
	"ReturnFlow",
	"RiverInflow",
	"RiverDivert",
	"RiverOutflow",
	"AvailableFlow",
	"StreamflowHist" };

public static final int streamOptionsSize_0969 = 9;
public static final String[] streamOptions_0969 = {
	"Upstream_Inflow",
	"Reach_Gain",
	"Return_Flow",
	"River_Inflow",
	"River_Divert",
	"River_By_Well",
	"River_Outflow",
	"Available_Flow",
	"StreamflowHist" };

public static final int RESERVOIR_TYPE = 1;
public static final int reservoirOptionsSize_0100 = 21;
public static final String[] reservoirOptions_0100 = {
	"InitialStorage",
	"RiverPriority",
	"RiverStorage",
	"RiverExchange",
	"CarrierPriority",
	"CarrierStorage",
	"TotalSupply",
	"StorageUse",
	"StorageExchange",
	"CarrierUse",
	"TotalRelease",
	"Evap",
	"SeepSpill",
	"SimEOM",
	"TargetLimit",
	"FillLimit",
	"Inflow",
	"Outflow",
	"HistoricalEOM",
	"HistoricalMin",
	"HistoricalMax"
	};

public static final int reservoirOptionsSize_0901 = 24;
public static final String[] reservoirOptions_0901 = {
	"InitialStorage",
	"RiverPriority",
	"RiverStorage",
	"RiverExchange",
	"CarrierPriority",
	"CarrierStorage",
	"TotalSupply",
	"StorageUse",
	"StorageExchange",
	"CarrierUse",
	"TotalRelease",
	"Evap",
	"SeepSpill",
	"SimEOM",
	"TargetLimit",
	"FillLimit",
	"RiverInflow",
	"TotalRelease",
	"TotalSupply",
	"RiverByWell",
	"RiverOutflow",
	"HistoricalEOM",
	"HistoricalMin",
	"HistoricalMax"
	};

public static final int reservoirOptionsSize_0969 = 24;
public static final String[] reservoirOptions_0969 = {
	"Initial_Storage",
	"River_Priority",
	"River_Storage",
	"River_Exchange",
	"Carrier_Priority",
	"Carrier_Storage",
	"Total_Supply",
	"Storage_Use",
	"Storage_Exchange",
	"Carrier_Use",
	"Total_Release",
	"Evap",
	"Seep_Spill",
	"Sim_EOM",
	"Target_Limit",
	"Fill_Limit",
	"River_Inflow",
	"Total_Release",
	"Total_Supply",
	"River_By_Well",
	"River_Outflow",
	"HistoricalEOM",
	"HistoricalMin",
	"HistoricalMax"
	};

public static final int INSTREAM_TYPE = 2;

public static final int instreamOptionsSize_0100 = 14;
public static final String[] instreamOptions_0100 = {
	"ConsDemand",
	"FromRiverByPriority",
	"FromRiverByStorage",
	"FromRiverByExchange",
	"TotalSupply",
	"Short",
	"WaterUse,TotalReturn",
	"UpstreamInflow",
	"ReachGain",
	"ReturnFlow",
	"RiverInflow",
	"RiverDivert",
	"RiverOutflow",
	"Demand"
	};

public static final int instreamOptionsSize_0901 = 15;
public static final String[] instreamOptions_0901 = {
	"ConsDemand",
	"FromRiverByPriority",
	"FromRiverByStorage",
	"FromRiverByExchange",
	"TotalSupply",
	"Short",
	"WaterUse,TotalReturn",
	"UpstreamInflow",
	"ReachGain",
	"ReturnFlow",
	"RiverInflow",
	"RiverDivert",
	"RiverOutflow",
	"AvailFlow",
	"Demand"
	};

public static final int instreamOptionsSize_0969 = 17;
public static final String[] instreamOptions_0969 = {
	"Total_Demand",
	"CU_Demand",
	"From_River_By_Priority",
	"From_River_By_Storage",
	"From_River_By_Exchange",
	"Total_Supply",
	"Total_Short",
	"Total_Return",
	"Upstream_Inflow",
	"Reach_Gain",
	"Return_Flow",
	"River_Inflow",
	"River_Divert",
	"River_By_Well",
	"River_Outflow",
	"Available_Flow",
	"Demand"
	};

public static final int DIVERSION_TYPE = 3;
public static final int diversionOptionsSize_0100 = 18;
public static final String[] diversionOptions_0100 = {
	"ConsDemand",
	"FromRiverByPriority",
	"FromRiverByStorage",
	"FromRiverByExchange",
	"FromCarrierByPriority",
	"FromCarierByStorage",
	"CarriedWater",
	"TotalSupply",
	"Short",
	"ConsumptiveWaterUse",
	"WaterUse,TotalReturn",
	"UpstreamInflow",
	"ReachGain",
	"ReturnFlow",
	"RiverInflow",
	"RiverDivert",
	"RiverOutflow",
	"Historical"
	};

public static final int diversionOptionsSize_0901 = 23;
public static final String[] diversionOptions_0901 = {
	"ConsDemand",
	"FromRiverByPriority",
	"FromRiverByStorage",
	"FromRiverByExchange",
	"FromWell",
	"FromCarrierByPriority",
	"FromCarierByStorage",
	"CarriedWater",
	"TotalSupply",
	"Short",
	"ConsumptiveWaterUse",
	"WaterUse,TotalReturn",
	"UpstreamInflow",
	"ReachGain",
	"ReturnFlow",
	"WellDepletion",
	"To/FromGWStorage",
	"RiverInflow",
	"RiverDivert",
	"RiverByWell",
	"RiverOutflow",
	"AvailableFlow",
	"Historical"
	};

public static final int diversionOptionsSize_0969 = 28;
public static final String[] diversionOptions_0969 = {
	"Total_Demand",
	"CU_Demand",
	"From_River_By_Priority",
	"From_River_By_Storage",
	"From_River_By_Exchange",
	"From_Well",
	"From_Carrier_By_Priority",
	"From_Carrier_By_Storage",
	"Carried_Water",
	"From_Soil",
	"Total_Supply",
	"Total_Short",
	"CU_Short",
	"Consumptive_Use",
	"To_Soil",
	"Total_Return",
	"Loss",
	"Upstream_Inflow",
	"Reach_Gain",
	"Return_Flow",
	"Well_Depletion",
	"To/From_GW_Storage",
	"River_Inflow",
	"River_Divert",
	"River_By_Well",
	"River_Outflow",
	"Available_Flow",
	"Historical"
	};

public static final int WELL_TYPE = 4;
public static final int wellOptionsSize_0901 = 11;
public static final String[] wellOptions_0901 =	{
	"Demand",
	"FromWell",
	"FromOther",
	"Short",
	"ConsumptiveUse",
	"Return",
	"Loss",
	"River",
	"GWStor",
	"Salvage",
	"Historical"
	};

public static final int wellOptionsSize_0969 = 19;
public static final String[] wellOptions_0969 =	{
	"Total_Demand",
	"CU_Demand",
	"From_Well",
	"From_SW",
	"From_Soil",
	"Total_Supply",
	"Total_Short",
	"CU_Short",
	"Total_CU",
	"To_Soil",
	"Total_Return",
	"Loss",
	"Total_Use",
	"From_River",
	"From_GwStor",
	"From_Salvage",
	"From_Soil",
	"Total_Source",
	"Historical"
	};

public static final int RUNTYPE_SINGLE		= 0;
public static final int RUNTYPE_MULTIPLE	= 1;
public static final int RUNTYPE_DIFFERENCE	= 2;
public static final int RUNTYPE_MERGE		= 3;
public static final int RUNTYPE_DIFFX		= 4;

protected String	_type;	// diversion, instream, reservoir, stream
protected String	_ID;	// location
protected String	_Name;	// used for output control file
protected String	_dtype;	// data type associated with _type
protected String	_scenario;	// scenario, optional
protected int		_switch;	// off=0, on=1, used for output control
					// also used for big picture: run type
protected List	_IDVec;	// location(s), used for big picture
protected String	_YrAve;	// year or average, used for big picture
protected String	_fileName; // file name or entire path, big picture

/**
Constructor.
*/
public StateMod_GraphNode ()
{	super ( );
	initialize ();
}

/**
Add an ID to the ID Vector.
*/
public void addID ( String s )
{	if ( s != null ) {
		_IDVec.add ( s);
	}
}

//FIXME SAM 2008-03-24 Why is this here?  Does anything use it outside of this class?
public static List arrayToVector(String[] array) {
	int size = array.length;
	List v = new Vector();
	for (int i = 0; i < size; i++) {
		v.add(array[i]);
	}

	return v;
}

/**
Clean up before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_type = null;
	_ID = null;
	_dtype = null;
	_scenario = null;
	_YrAve = null;
	_fileName = null;
	_IDVec = null;
	super.finalize();
}

/**
Locate column number in output file which contains TS of interest.  
filename must be included so a determination of StateMod version can be 
made (i.e. were well included in this version or not).  The overloaded version
with a StateMod version of -1.0 is used.
@param s_type Station/structure type (e.g., "diversion").
@param dtype Data type for the s_type.
*/
public static int getDataOutputColumn ( String s_type, String dtype, String filename )
throws Exception
{	return getDataOutputColumn ( s_type, dtype, filename, -1.0 );
}

/**
Locate column number in output file which contains TS of interest.
Filename must be included so a determination of StateMod version can be
made (i.e. were well included in this version or not).
@return the column (1 is the first column in the output file).
@param s_type Station/structure type (e.g., "diversion").
@param dtype Data type for the s_type.
@param statemod_version StateMod version as floating point number, used to
determine order of columns.
*/
public static int getDataOutputColumn ( String s_type, String dtype, String filename, double statemod_version )
throws Exception
{	String rtn = "StateMod_GraphNode.getDataOutputColumn";
	int type = getTypeFromString ( s_type );
	int index = getDataOutputIndex ( type, dtype, statemod_version );
	if ( index == -999 ) {
		Message.printWarning ( 1, rtn, "Unable to get index for " + s_type + ", " + dtype );
		return -999;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 30, rtn, "Finding column for " + s_type + " (" + type + "), " + dtype );
	}
	if ( type == RESERVOIR_TYPE ) {
		// Does not depend on the version.  Make all columns available...
		return (index + 5);
	}
	else if ( type == STREAM_TYPE ) {
		if ( statemod_version > 9.69 ) {
			if ( (index == 0) || (index == 1) || (index == 2) ) {
				return index + 22;
			}
			else if ( index >= 3 ) {
				return index + 24;
			}
		}
		else if ( statemod_version > 9.01 ) {
			if ( (index == 0) || (index == 1) || (index == 2) ) {
				return index + 17;
			}
			else if ( (index == 3) || (index == 4) ) {
				return index + 19;
			}
			else if ( index >= 5 ) {
				return index + 20;
			}
		}
		else {
			// Older...
			return index + 16;
		}
	}
	else if ( type == INSTREAM_TYPE ) {
		if ( statemod_version >= 9.69 ) {
			switch ( index ) {
				case 0: return 5;
				case 1: return 6; 
				case 2: return 7;
				case 3: return 8; 
				case 4: return 9; 
				case 5: return 15;
				case 6: return 16; 
				case 7: return 20;
				case 8: return 22; 
				case 9: return 23;
				case 10: return 24;
				case 11: return 27;
				case 12: return 28;
				case 13: return 29;
				case 14: return 30;
				case 15: return 31;
				default: return -999;
			}
		}
		else if ( statemod_version >= 9.01 ) {
			switch ( index ) {
				case 0: return 5;
				case 1: return 6; 
				case 2: return 7;
				case 3: return 8; 
				case 4: return 13; 
				case 5: return 14;
				case 6: return 16; 
				case 7: return 17;
				case 8: return 18; 
				case 9: return 19;
				case 10: return 22;
				case 11: return 23;
				case 12: return 25;
				case 13: return 26;
				default: return -999;
			}
		}
		else {	// Assume older...	
			switch ( index ) {
				case 0: return 5;
				case 1: return 6; 
				case 2: return 7;
				case 3: return 8; 
				case 4: return 12; 
				case 5: return 13;
				case 6: return 15;
				case 7: return 16; 
				case 8: return 17;
				case 9: return 18;
				case 10: return 19;
				case 11: return 20;
				case 12: return 21;
				default: return -999;
			}
		}
	}
	else if ( type == DIVERSION_TYPE ) {
		// Version does not matter...
		return ( index + 5 );
	}
	else if ( type == WELL_TYPE ) {
		// Full list available...
		return (index + 5);
	}
	return -999;
}

/**
Locate the index of the string matching dtype.
*/
public static int getDataOutputIndex ( int type, String dtype )
{	return getDataOutputIndex ( type, dtype, -1.0 );
}

/**
Locate the index of the string matching dtype.
@param type Structure/station type as an integer.
@param dtype Data type to request (e.g., "Total_Return");
@param statemod_version Statemod version as a double.
@return the index in the data types for the structure type (starting at zero).
*/
public static int getDataOutputIndex(int type, String dtype, double statemod_version) {
	// first, set Options to correct string list of options depending on the type

	List Options = getGraphDataType ( type, statemod_version, true );
	if ( Options == null ) {
		Message.printWarning(1, "StateMod_GraphNode.getDataOutputIndex",
			"Unable to determine the StateMod output column "
			+ "for \"" + dtype  + "\"" );
	}

	for (int i = 0; i < Options.size(); i++) {
		String s = (String)Options.get(i);
		if (s.equalsIgnoreCase(dtype)) {
			return i;
		}
	}
	return -999;
}

/**
Return the dtype.
*/
public String getDtype() {
	return _dtype;
}

/**
Retrieve the fileName.
*/
public String getFileName() {
	return _fileName;
}

/**
Get data type associated with type.  It is assumed that StateMod is the newest version.
Input (historical) and output data types are returned.
@param type Structure/station data type (see StateMod_GraphNode.*_TYPE).
*/
public static List getGraphDataType ( int type )
{	return getGraphDataType ( type, 1000.0, true );
}

/**
Get data type associated with type.
It is assumed that StateMod is the newest version.
@param type Structure/station data type (see StateMod_GraphNode.*_TYPE).
@param include_all If true, input (historical) and output data types are returned.
If false, only output data types are returned.
*/
public static List getGraphDataType (int type, boolean include_all) {
	return getGraphDataType(type, 1000.0, include_all);
}

/**
Get the graph data types associated with type, for use with the StateMod GUI.
It is assumed that the structure/station is NOT a baseflow node.
@param type Structure/station data type.
@param statemod_version StateMod version as a floating point number.
@param include_all If true, historic time series are returned with the
list (suitable for StateMod GUI graphing tool).  If false, only model output
parameters are returned (suitable for delplt usage with the big picture plot).
*/
public static List getGraphDataType ( int type, double statemod_version, boolean include_all )
{	return getGraphDataType ( type, statemod_version, include_all, false );
}

/**
Get the graph data types associated with type, for use with the StateMod GUI.
@param type Structure/station data type.
@param statemod_version StateMod version as a floating point number.
@param include_all If true, historic time series are returned with the
list (suitable for StateMod GUI graphing tool).  If false, only model output
parameters are returned (suitable for delplt usage with the big picture plot).
@param is_baseflow Handled separately from "include_all".  If true, then
a data type of "StreamflowBase" will be appended to the list.
@return a new-line delimited list of appropriate graph parameter types or null
if the requested station type or version does not match a known combination.
*/
public static List getGraphDataType ( int type, double statemod_version, boolean include_all, boolean is_baseflow )
{
	List options = null;
	if ( statemod_version >= 9.69 ) {
		if ( type == STREAM_TYPE ) {
			options = arrayToVector(streamOptions_0969);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == RESERVOIR_TYPE ) {
			options = arrayToVector(reservoirOptions_0969);
			if ( !include_all ) {
				options = removeLastNElements(options, 3);
			}
		}
		else if ( type == INSTREAM_TYPE ) {
			options = arrayToVector(instreamOptions_0969);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == DIVERSION_TYPE ) {
			options = arrayToVector(diversionOptions_0969);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == WELL_TYPE ) {
			options = arrayToVector(wellOptions_0969);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
	}
	else if ( statemod_version >= 9.01 ) {
		if ( type == STREAM_TYPE ) {
			options = arrayToVector(streamOptions_0901);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == RESERVOIR_TYPE ) {
			options = arrayToVector(reservoirOptions_0901);
			if ( !include_all ) {
				options = removeLastNElements(options, 3);
			}
		}
		else if ( type == INSTREAM_TYPE ) {
			options = arrayToVector(instreamOptions_0901);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == DIVERSION_TYPE ) {
			options = arrayToVector(diversionOptions_0901);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == WELL_TYPE ) {
			options = arrayToVector(wellOptions_0901);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
	}
	else {
		// Assume old...
		if ( type == STREAM_TYPE ) {
			options = arrayToVector(streamOptions_0100);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == RESERVOIR_TYPE ) {
			options = arrayToVector(reservoirOptions_0100);
			if ( !include_all ) {
				options = removeLastNElements(options, 3);
			}
		}
		else if ( type == INSTREAM_TYPE ) {
			options = arrayToVector(instreamOptions_0100);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
		else if ( type == DIVERSION_TYPE ) {
			options = arrayToVector(diversionOptions_0100);
			if ( !include_all ) {
				options = removeLastNElements(options, 1);
			}
		}
	}

	if ( options == null ) {
		options = new Vector();
	}
	
	if ( is_baseflow ) {
		// Append "StreamflowBase" on the end of the data types.
		// Based on past work the end of the string may have a "\n " or
		// be a parameter.
		options.add("StreamflowBase");
	}
	return options;
}

/**
Get number of options associated with type.
*/
public static int getGraphDataTypeSize ( int type )
{	return getGraphDataTypeSize ( type, -1.0 );
}

/**
Get number of options associated with type.
@param type Structure/station type.
@param statemod_version StateMod version 
*/
public static int getGraphDataTypeSize ( int type, double statemod_version )
{	if ( statemod_version >= 9.69 ) {
		if ( type == STREAM_TYPE ) {
			return streamOptionsSize_0969;
		}
		else if ( type == RESERVOIR_TYPE ) {
			return reservoirOptionsSize_0969;
		}
		else if ( type == INSTREAM_TYPE ) {
			return instreamOptionsSize_0969;
		}
		else if ( type == DIVERSION_TYPE ) {
			return diversionOptionsSize_0969;
		}
		else if ( type == WELL_TYPE ) {
			return wellOptionsSize_0969;
		}
		return 0;
	}
	if ( statemod_version >= 9.01 ) {
		if ( type == STREAM_TYPE ) {
			return streamOptionsSize_0901;
		}
		else if ( type == RESERVOIR_TYPE ) {
			return reservoirOptionsSize_0901;
		}
		else if ( type == INSTREAM_TYPE ) {
			return instreamOptionsSize_0901;
		}
		else if ( type == DIVERSION_TYPE ) {
			return diversionOptionsSize_0901;
		}
		else if ( type == WELL_TYPE ) {
			return wellOptionsSize_0901;
		}
		return 0;
	}
	else {
		// Assume old...
		if ( type == STREAM_TYPE ) {
			return streamOptionsSize_0100;
		}
		else if ( type == RESERVOIR_TYPE ) {
			return reservoirOptionsSize_0100;
		}
		else if ( type == INSTREAM_TYPE ) {
			return instreamOptionsSize_0100;
		}
		else if ( type == DIVERSION_TYPE ) {
			return diversionOptionsSize_0100;
		}
		return 0;
	}
}

/**
Get types available.
*/
public static String getGraphTypes ( )
{	return typesOptions;
}

public static String getGraphTypes ( int index )
{	return typesOptionsArray[index];
}

/**
Return the ID.
*/
public String getID() {
	return _ID;
}

/**
Return the ID at a position in the ID vector (used with Delplt runs).
*/
public String getID ( int pos )
{	return (String)_IDVec.get(pos);
}

/**
Retrieve the ID Vector.
*/
public List getIDVec() {
	return _IDVec;
}

/**
Retrieve the size of the ID Vector.
*/
public int getIDVectorSize() {
	return _IDVec.size();
}

/**
Return the Name.
*/
public String getName() {
	return _Name;
}

/**
Get available node types.
@return array of available node types.
*/
public static String [] getNodeTypes ( )
{	return node_types;
}

/**
Get available node type by index.
@return available node type by index.
@param index Index in the node types array.
*/
public static String getNodeType ( int index )
{	return node_types[index];
}

/**
Return the scenario.
*/
public String getScenario() {
	return _scenario;
}

/**
Return the switch.
*/
public int getSwitch() {
	return _switch;
}

/**
Return the type.
*/
public String getType() {
	return _type;
}

/**
Given a node type (e.g., "diversion") return the internal integer value
corresponding to the type.
@param type node type (e.g., "diversion");
@return internal integer value (e.g., DIVERSION_TYPE).
*/
public static int getTypeFromString ( String type )
{	if ( type == null ) {
		return -999;
	}
	if ( type.regionMatches(true,0,"d",0,1)) {		// diversion
		return DIVERSION_TYPE;
	}
	else if ( type.regionMatches (true,0,"i",0,1 )) {	// instream flow
		return INSTREAM_TYPE;
	}
	else if ( type.regionMatches (true,0,"r",0,1 )) {	// reservoir
		return RESERVOIR_TYPE;
	}
	else if ( type.regionMatches (true,0,"s",0,1 )) {	// stream
		return STREAM_TYPE;
	}
	else if ( type.regionMatches (true,0,"w",0,1 )) {	// stream
		return WELL_TYPE;
	}
	return -999;
}

/**
Retrieve the string containing either a year ("1989") or the string "Ave".
*/
public String getYrAve() {
	return _YrAve;
}

private void initialize ()
{	_type = "";
	_ID = "";
	_dtype = "";
	_scenario = "";
	_switch = 1;
	_YrAve = "";
	_fileName = "";
	_IDVec = new Vector(1,10);
}

/**
Set the type.
*/
public void setDtype ( String s )
{	if ( s != null ) {
		_dtype = s;
	}
}

/**
Set the fileName.
*/
public void setFileName ( String s )
{	if ( s != null ) {
		_fileName = s;
	}
}

/**
Set the ID.
*/
public void setID ( String s )
{	if ( s != null ) {
		_ID = s;
	}
}

/**
Set the Name.
*/
public void setName ( String s )
{	if ( s != null ) {
		_Name = s;
	}
}

/**
Set the scenario.
*/
public void setScenario ( String s )
{	if ( s != null ) {
		_scenario = s;
	}
}

/**
Set the switch.
*/
public void setSwitch ( int i ) {
	_switch = i;
}

public void setSwitch ( Integer i ) {
	_switch = i.intValue();
}

/**
Set the type.
*/
public void setType ( String s ) {
	String temp = new String(s);
	if (temp != null) {
		if (temp.equals("diversion")) {
			temp = "Diversion";
		}
		else if (temp.equalsIgnoreCase("instream") || temp.equalsIgnoreCase("InstreamFlow")) {
			temp = "Instream Flow";
		}
		else if (temp.equals("reservoir")) {
			temp = "Reservoir";
		}
		else if (temp.equalsIgnoreCase("stream")) {
			temp = "Streamflow";
		}
		else if (temp.equals("well")) {
			temp = "Well";
		}

		_type = temp;
	}
}

/**
Set the YrAve.
*/
public void setYrAve ( String s )
{	if ( s!= null ) {
		_YrAve = s;
	}
}

public static int SMDumpDelpltFile ( List theTemplate, String filename, PrintWriter out )
throws IOException
{	String rtn = "StateMod_GraphNode.SMDumpDelpltFile";
	String temp_cmnt = "#>";
	String cmnt = "#";
	StateMod_GraphNode node = null;
	List v = null;

	int num = 0;
	if ( theTemplate != null ) {
		num = theTemplate.size();
	}
	try {
		out.println ( temp_cmnt + " " + filename + " - SmDelta input file" );
		out.println ( temp_cmnt );
		out.println ( temp_cmnt );

		for ( int i=0; i<num; i++ ) {
			node = (StateMod_GraphNode)theTemplate.get(i);
			if ( i==0 ) {
				out.println ( cmnt );
				out.println ( cmnt + " Run type (Single, Multiple, Difference, " + "Diffx, Merge):" );
				out.println ( cmnt );
				int run_type = node.getSwitch();
				if ( run_type == RUNTYPE_SINGLE ) {
					out.println ("Single");
				}
				else if ( run_type == RUNTYPE_MULTIPLE ) {
					out.println("Multiple");
				}
				else if ( run_type == RUNTYPE_DIFFERENCE ) {
					out.println("Difference");
				}
				else if ( run_type == RUNTYPE_DIFFX ) {
					out.println("Diffx");
				}
				else if ( run_type == RUNTYPE_MERGE ) {
					out.println("Merge");
				}
			}

			out.println ( cmnt+cmnt+cmnt+cmnt+cmnt );
			out.println ( cmnt );
			out.println ( cmnt + "     File:" );
			out.println ( cmnt + "          For reservoirs use .xre or .b44" );
			out.println ( cmnt + "          For others use .xde or .b43" );
			out.println ( node.getFileName());
			out.println ( cmnt );
			out.println ( cmnt + "     Data type (Diversion, Instream, StreamGage, StreamID, Reservoir, Well):" );
			out.println ( node.getType());
			out.println ( cmnt );
			out.println ( cmnt + "     Parameter (same as StateModGUI) or type statemod -h" );
			out.println ( node.getDtype());
			out.println ( cmnt );
			out.println ( cmnt + "     ID (0=all, n=ID, end with a -999)" );
			v = node.getIDVec();
			int numIDs = v.size();
			for ( int j=0; j<numIDs; j++ ) {
				out.println ((String)v.get(j));
			}
			out.println ( "-999" );
			out.println ( cmnt );
			out.println ( cmnt + "     Time (year [e.g., 1989], year and month [e.g. 1989 NOV], or Ave)" );
			out.println ( node.getYrAve());
			out.println ( cmnt );
		}
		out.println ( cmnt+cmnt+cmnt+cmnt+cmnt );
		out.println ( cmnt );
		out.println ( cmnt + "     End of file indicator" );
		out.println ( "-999" );
	} catch (Exception e) {
		rtn = null;
		temp_cmnt = null;
		cmnt = null;
		node = null;
		v = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
	rtn = null;
	temp_cmnt = null;
	cmnt = null;
	node = null;
	v = null;
	return 0;
}

public static int SMDumpGraphFile ( List theGraphOpts, PrintWriter out )
throws IOException
{	StateMod_GraphNode node = null;
	String ident = null;

	int i;
	int num = 0;
	out.println ( "#>" );
	out.println ( "#> Each line below identifies a time series." );
	out.println ( "#> The identifier consists of the following fields:" );
	out.println ( "#> LocationID..NodeType_DataType.Interval." );
	out.println ( "#>" );
	if ( theGraphOpts != null ) {
		num = theGraphOpts.size();
	}
	try {
		for ( i=0; i<num; i++ ) {
			node = (StateMod_GraphNode)theGraphOpts.get(i);
			// Identifier is ID..StructType_DataType.MONTH.Scenario
			ident = node.getID() + ".." + node.getType() + "_" + node.getDtype() + ".MONTH." + node.getScenario();
			out.println ( ident );
		}
	} catch (Exception e) {
		node = null;
		ident = null;
		Message.printWarning ( 2, "StateMod_GraphNode.SMDumpGraphFile", e );
		throw new IOException ( e.getMessage());
	}
	
	node = null;
	ident = null;
	return 0;
}

public static int SMDumpOutputControlFile ( List theOC, PrintWriter out )
throws IOException
{	String iline = null;
	String rtn = "StateMod_GraphNode.SMDumpOutputControlFile";
	String format = "%-12.12s %-24.24s %-3.3s %5d";
	String cmnt = "#>";
	StateMod_GraphNode node = null;
	List v = new Vector ( 4 );

	int i;
	int num = 0;
	if ( theOC != null ) {
		num = theOC.size();
	}
	try {
		out.println ( cmnt + "*.xou; Output request file for StateMod" );
		out.println ( cmnt );
		out.println ( cmnt + "Type ( e.g.  Diversion, StreamGage or Reservoir, or All)" );
		out.println ( cmnt );
		out.println ( "All" );	// hardcoded - may need to change
		out.println ( cmnt );
		out.println ( cmnt + "Parameter (e.g. TotalSupply, SimEOM, RiverOutflow, or All)");
		out.println ( cmnt );
		out.println ( "All" );	// hardcoded - may need to change
		out.println ( cmnt );
		out.println ( cmnt + "ID Name Type and Print Code (0=no, 1=yes)" );
		out.println ( cmnt + "Note: id = All prints all" );
		out.println ( cmnt + "      id = -999 = stop" );
		out.println ( cmnt + "      default is to turn on all stream gages (FLO)" );
		out.println ( cmnt );

		int istart=0;
		if ( num > 0 ) {
			node = (StateMod_GraphNode)theOC.get(0);

			if ((node.getID()).equalsIgnoreCase("All")) {
				out.println ( "All" );
				istart=1;
			}
		}

		for ( i=istart; i<num; i++ )
		{
			node = (StateMod_GraphNode)theOC.get(i);
			if ( node == null ) {
				continue;
			}

			// need to format
			v.clear();
			v.add ( node.getID());
			v.add ( node.getName());
			v.add ( node.getType());
			v.add ( new Integer ( node.getSwitch()));

			iline = StringUtil.formatString ( v, format );

			if ( Message.isDebugOn ) {
				Message.printDebug (30, rtn, "Adding " + iline);
			}
			out.println ( iline );
		}
		out.println ( "-999" );	// stop code to StateMod
	} catch (Exception e) {
		iline = null;
		rtn = null;
		format = null;
		cmnt = null;
		node = null;
		v = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
	
	iline = null;
	rtn = null;
	format = null;
	cmnt = null;
	node = null;
	v = null;
	return 0;
}

/**
Parse a SmDelta input file.  Each StateMod_GraphNode contains a
station type/file/parameter combination.  The identifiers associated with the
graph node are stored in a Vector with each node (e.g., can be a single zero
string in the Vector or a Vector of identifiers).  The run mode is stored with
each item returned.
@param theTemplate An existing Vector to have graph nodes added to it.
@param filename Name of the file name to read.
@return 1 if an error or 0 if successful.
*/
public static int readStateModDelPltFile ( List theTemplate, String filename )
throws IOException
{	String rtn = "StateMod_GraphNode.readStateModDelPltFile";
	String iline = null;
	BufferedReader in = null;
	StateMod_GraphNode aNode=null;

	int step=0, run_type=0;

	Message.printStatus ( 1, rtn, "Reading SmDelta template: " + filename );
	try {	in = new BufferedReader ( new FileReader (filename));
		while ( (iline = in.readLine()) != null ) {
			// check for comments
			iline = StringUtil.removeNewline(iline);
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}

			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, "line: \"" + iline + "\", step=" + step );
			}

			if ( step == 0 ) {
				// Need the run mode...
				if ( iline.equalsIgnoreCase("single")) {
					run_type = RUNTYPE_SINGLE;
				}
				else if ( iline.equalsIgnoreCase("multiple")) {
					run_type = RUNTYPE_MULTIPLE;
				}
				else if ( iline.equalsIgnoreCase("difference")){
					run_type = RUNTYPE_DIFFERENCE;
				}
				else if ( iline.equalsIgnoreCase("diffx")) {
					run_type = RUNTYPE_DIFFX;
				}
				else if ( iline.equalsIgnoreCase("merge")) {
					run_type = RUNTYPE_MERGE;
				}
				step++;
			}
			else if ( step == 1 ) {
				if ( iline.equals("-999")) {
					// end of file indicator
					Message.printDebug (40, rtn, "End of file indicator found" );
					break;
				}
				else {
					// Allocate new graph node and set top-level data like the run mode...
					aNode = new StateMod_GraphNode();

					// File
					// create a new node, store file name
					aNode.setSwitch ( run_type );
					aNode.setFileName ( iline.trim() );
					step++;
				}
			}
			else if ( step == 2 ) {
				// Node type
				aNode.setType ( iline.trim() );
				step++;
			}
			else if ( step == 3 ) {
				// parameter
				aNode.setDtype ( iline.trim() );
				step++;
			}
			else if ( step == 4 ) {
				// ID
				// If end of list indicator is found, 
				// increment the step counter.  Otherwise, 
				// add the id to the list, but don't increment
				// the step counter because the next line will
				// be either the end of list indicator or another ID.
				if ( iline.equals("-999")) {
					step++;
				}
				else {
					aNode.addID( iline.trim() );
				}
			}
			else if ( step == 5 ) {
				// Year or Average ( Ave or 1989 NOV)
				// LAST STEP - set the step counter to 1
				// (0 was runtype and will only occur once).
				aNode.setYrAve ( iline.trim() );
				step = 1;
				theTemplate.add ( aNode );
			}
		}
	} catch (Exception e) {
		rtn = null;
		iline = null;
		if ( in != null ) {
			in.close();
		}
		in = null;
		aNode=null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}

	rtn = null;
	iline = null;
	if ( in != null ) {
		in.close();
	}
	in = null;
	aNode=null;
	return 0;
}

/**
Read graph information in and store in a java vector.  The new graph types are
added to the end of the previously stored diversions.
*/
public static int readStateModGraphFile ( List theGraphOpts, String filename )
throws IOException
{	String rtn = "StateMod_GraphNode.readStateModGraphFile";
	String iline = null;
	BufferedReader in = null;
	StateMod_GraphNode aNode = null;
	TSIdent ident = null;
	List list = null;
	int dtype_pos = 0;

	Message.printStatus ( 1, rtn, "Reading graph template: " + filename );
	try {	in = new BufferedReader ( new FileReader (filename));
		while ( (iline = in.readLine()) != null ) {
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}

			// allocate new diversion node
			aNode = new StateMod_GraphNode();

			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, "line 1: " + iline );
			}
			ident = TSIdent.parseIdentifier ( iline );

			aNode.setID (ident.getLocation());
			aNode.setScenario ( ident.getScenario());

			// break up type into type (e.g., "diversion") and
			// dtype (e.g., "Total_Demand")...
			list = StringUtil.breakStringList (	ident.getType(), "_", 0 );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, ident.getType());
			}
			if ( list.size() < 2 ) {
				continue;
			}
			aNode.setType ( (String)list.get(0));
			if ( aNode.getType().equalsIgnoreCase( "instream flow") ) {
				// New convention is shorter as of 2002-08-06
				aNode.setType("instream");
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, aNode.getType());
			}
			// The data type may have an underscore so just set the
			// specific data type to the remaining string.
			//aNode.setDtype ( (String)list.elementAt(1));
			dtype_pos = ident.getType().indexOf("_");
			if ( dtype_pos < ident.getType().length() ) { 
				aNode.setDtype ( ident.getType().substring(dtype_pos + 1) );
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, aNode.getDtype());
			}

			theGraphOpts.add ( aNode );
		}
	} catch (Exception e) {
		rtn = null;
		iline = null;
		if ( in != null ) {
			in.close();
		}
		in = null;
		aNode = null;
		ident = null;
		list = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
	rtn = null;
	iline = null;
	if ( in != null ) {
		in.close();
	}
	in = null;
	aNode = null;
	ident = null;
	list = null;
	return 0;
}

/**
Read output control information in and store in a java vector.  The new control
information is added to the end of the previously stored information.
*/
public static int readStateModOutputControlFile ( List theOC, String filename )
throws IOException
{	String rtn = "StateMod_GraphNode.readStateModOutputControlFile";
	String format = "s12x1s24x1s3x1i5";
	String iline = null;
	List v = null;
	BufferedReader in = null;
	StateMod_GraphNode aNode = null;

	int skipAll=0;// there are 2 "All" statements we need to skip over
			// if a 3rd exists, we need to pass that info back

	Message.printStatus ( 1, rtn, "Reading output control template: " + filename );
	try {
		in = new BufferedReader ( new FileReader (filename));
		while ( (iline = in.readLine()) != null )
		{
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}

			if ( iline.equalsIgnoreCase ( "All" ) && skipAll < 2 )
			{
				skipAll++;
				continue;
			}

			if ( iline.startsWith ( "-999" )) {
				return 0;
			}

			// allocate new diversion node
			aNode = new StateMod_GraphNode();

			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, rtn, "line 1: " + iline );
			}
			v = StringUtil.fixedRead ( iline, format );
			if ( v.size() < 4 )
			{
				if ( iline.equalsIgnoreCase ( "All" ))
				{
					aNode.setID ("All");
					theOC.add ( aNode );
					continue;
				}
				else
				{
					Message.printWarning ( 2, rtn, "Unable to process \"" + iline + "\"" );
					continue;
				}
			}

			aNode.setID (((String)v.get(0)).trim());
			aNode.setName (((String)v.get(1)).trim());
			aNode.setType (((String)v.get(2)).trim());
			aNode.setSwitch ((Integer)v.get(3));

			theOC.add ( aNode );
		}
	} catch (Exception e) {
		rtn = null;
		format = null;
		iline = null;
		v = null;
		if ( in != null ) {
			in.close();
		}
		in = null;
		aNode = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
	rtn = null;
	format = null;
	iline = null;
	v = null;
	if ( in != null ) {
		in.close();
	}
	in = null;
	aNode = null;
	return 0;
}

private static List removeLastNElements(List v, int count) {
	List r = new Vector();
	int itransfer = v.size() - count;
	for (int i = 0; i < itransfer; i++) {
		r.add ( new String((String)v.get(i)) );
	}
	
	return r;
}

public static void writeStateModDelPltFile ( String instrfile, 
	String outstrfile, List theTemplate, String[] new_comments )
throws IOException
{	String rtn = "StateMod_GraphNode.writeStateModDelPltFile";
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, rtn, "in writeStateModDelPltFile printing file: " + outstrfile );
	}
	try {
		out = IOUtil.processFileHeaders ( instrfile, outstrfile, new_comments, comment_str, ignore_comment_str, 0 );
		SMDumpDelpltFile ( theTemplate, outstrfile, out );
		out.flush();
		out.close();
		out = null;
		rtn = null;
		comment_str = null;
		ignore_comment_str = null;
	} catch ( Exception e ) {
		if ( out != null ) {
			out.flush();
			out.close();
		}
		out = null;
		rtn = null;
		comment_str = null;
		ignore_comment_str = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
}

public static void writeStateModGraphFile ( String instrfile, String outstrfile,
		List theGraphOpts, String[] new_comments )
throws IOException
{	String rtn = "StateMod_GraphNode.writeStateModGraphFile";
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, rtn, "in writeStateModGraphFile printing file: " + outstrfile );
	}
	try {
		out = IOUtil.processFileHeaders ( instrfile, outstrfile, new_comments, comment_str, ignore_comment_str, 0 );
		SMDumpGraphFile ( theGraphOpts, out );
		out.flush();
		out.close();
		out = null;
		rtn = null;
		comment_str = null;
		ignore_comment_str = null;
	} catch ( Exception e ) {
		if ( out != null ) {
			out.flush();
			out.close();
		}
		out = null;
		rtn = null;
		comment_str = null;
		ignore_comment_str = null;
		throw new IOException ( e.getMessage());
	}
}

public static void writeStateModOutputControlFile ( String instrfile, String outstrfile, List theOC, String[] new_comments )
throws IOException
{	String rtn = "StateMod_GraphNode.writeStateModOutputControlFile";
	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, rtn, "Printing file: " + outstrfile );
	}
	try {
		out = IOUtil.processFileHeaders ( instrfile, outstrfile, new_comments, comment_str, ignore_comment_str, 0 );
		SMDumpOutputControlFile ( theOC, out );
		out.flush();
		out.close();
		out = null;
		rtn = null;
		comment_str = null;
		ignore_comment_str = null;
	} catch ( Exception e ) {
		if ( out != null ) {
			out.flush();
			out.close();
		}
		out = null;
		rtn = null;
		comment_str = null;
		ignore_comment_str = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
}

/**
Return a string representation of the object.
@return a string representation of the object.
*/
public String toString()
{	return "Type: " + _type
		+ ", ID: " + _ID
		+ ", Name: " + _Name
		+ ", DataType: " + _dtype
		+ ", Scenario: " + _scenario
		+ ", Switch/RunType: " + _switch
		+ ", IDVec: " + _IDVec
		+ ", YrAve: " + _YrAve
		+ ", Filename: " + _fileName;
}

} // end StateMod_GraphNode
