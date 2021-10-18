// StateMod_BTS - read/write time series from StateMod binary file(s)

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Models Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Models Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

// ----------------------------------------------------------------------------
// StateMod_BTS - read/write time series from StateMod binary file(s)
// ----------------------------------------------------------------------------
// History:
//
// 2003-03-12	Steven A. Malers, RTi	Initial version.  Copy RTi.TS.BinaryTS
//					and update for the StateMod binary file
//					format (.b43).
// 2003-10-20	SAM, RTi		* Rename StateModXTS to Statemod_BTS.
//					* Change TSDate to DateTime.
//					* Change TS.INTERVAL* to TimeInterval.
// 2003-10-28	SAM, RTi		* Convert some messages to debugs to
//					  improve performance.
//					* Keep around lists of the various
//					  station IDs and names from the header
//					  to use when creating the time series.
//				 	* Also, since the data are in the order
//					  of river nodes but the requested TSID
//					  uses the station ID, the header data
//					  must be examined to find a match using
//					  the determineRiverStatino() method.
// 2003-11-03	SAM, RTi		* Update to new StateMod 10.43 binary
//					  file format - Ray Bennett has added
//					  the river node ID next to the station
//					  ID!  This allows easy lookup of the
//					  river node to match the station.
//					  Still keep the header information in
//					  memory for now but later may optimize
//					  to jump through the header as needed.
//					* Use xfrnam instead of ifrnam because
//					  Ray has indicated the former in the
//					  documentation.
//					* Rename some private methods to be
//					  specific to B43, in anticipation of
//					  needing to read more files.
//					* Convert all B43 parameters from CFS to
//					  ACFT for output.
//					* Figured out how to read names - just
//					  read little endian char 1!
// 2003-11-14	SAM, RTi		* Add support for binary reservoir and
//					  well files and also all binary daily
//					  files.
//					* Get the list of parameters from the
//					  StateMod_Util class rather than
//					  duplicating the list here.
// 2003-11-26	SAM, RTi		* Finalize support for other data types.
//					* When initializing the object, set the
//					  __comp_type member data - then when
//					  processing data, only search arrays
//					  for the appropriate type of data in
//					  the file.  For example, for the well
//					  file, there is no reason to search
//					  diversion and reservoir identifiers.
// 2004-02-04	SAM, RTi		* Fix bug where duplicate time series
//					  were being matched in readTimeSeries()
//					  because baseflow station IDs are
//					  sometimes the same as other station
//					  IDs.
// 2004-03-15	SAM, RTi		* Add getFileFilter - REVISIT.
//					* Allow a null TSID pattern to be passed
//					  to readTimeSeriesList() - treat null
//					  as "*".
//					* Fix bug where only one data type could
//					  be read in readTimeSeriesList().
// 2004-08-23	SAM, RTi		* Overload readTimeSeries() to have a
//					  PropList to allow additional
//					  customization of reads - however, go
//					  ahead and implement a Hashtable for
//					  file management to use as the default.
//					  There does not seem to be a downside
//					  to this.
// 2005-12-21	SAM, RTi		* Include support for StateMod 11.0
//					  file format updates.
//					* Troubleshoot why reservoir time series
//					  are not being read properly.
//					* Remove setVersion() since the version
//					  really needs to be set in the
//					  constructor.
// 2005-12-22	SAM, RTi		* Add __nowner2 that is the number of
//					  accounts, not cumulative, and
//					  __nowner2_cum, which is cumulative.
// 2006-01-03	SAM, RTi		* Resolve final issues with
//					  new parameter lists and binary
//					  reservoirs.
// 2006-01-04	SAM, RTi		* Finalize recent reservoir file
//					  changes.
// 2006-01-05	SAM, RTi		* One last change because inactive
//					  accounts are at the end of the block.
// 2006-01-15	SAM, RTi		* Add determineFileVersion() to get
//					  the StateMod version from the file,
//					  in order to make other decisions.
//					  This is mainly intented for use
//					  during transition to the new format
//					  where more information is in the
//					  file header.
//					* Add getParameters() to return the
//					  list of parameter names, for use in
//					  displays, etc.
// 2006-07-06	SAM, RTi		* Fix bug where month data were not
//					  being properly initialized for
//					  irrigation year data (Nov-Oct).
// 2007-01-17	SAM, RTi		* Fix bug where determining the file
//					  version needed to use different bytes
//					  because the previous test was failing.
// 2007-01-18	SAM, RTi		* Allow lookup of station that is of
//					  type "other" (only in river network
//					  file *rin) to be looked up.
//					* Fix so dash in identifier is allowed
//					  for other than reservoirs (and
//					  reservoirs use for account).
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.IO.EndianRandomAccessFile;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
Provide an interface to a StateMod binary files.  Binary data are organized as follows:
<pre>
Station Type     Monthly File     Daily File
--------------------------------------------
diversion        *.b43            *.b49
instream flow    *.b43            *.b49
reservoir        *.b44            *.b50
stream gage/est  *.b43            *.b49
well             *.b42            *.b65
---------------------------------------------------------------------------
</pre>
The file and format is determined based on the file extension and/or the
requested parameter and data interval.  Typically the readTimeSeries() or
readTimeSeriesList() methods are used, which open a file, read one or more
time series, and close the file.
All the methods in this class that use time series index numbers use 0 for the first time series.
The format of the file is described in StateMod documentation.
Each file has essentially the same header information, followed by data records.
Relevant notes:
<ol>
<li>	The individual station lists are used to look up a station identifier.
	The river node number is then used to find the position in the main river node list.</li>
<li>	The time series are written in one month blocks, with time series
	within the block listed in the order of the river nodes for stream/
	diversion/ISF and in the order of the specific list for reservoirs and wells.</li>
<li>	For reservoirs, a station's time series consist of a total, and one for
	each account (therefore the reservoir file has a total number of time series that is the
	reservoir count (for totals) plus the number of accounts.</li>
</ol>
*/
public class StateMod_BTS
{

/**
Conversion factor from CFSto ACFT - also need to multiply by days in month.
*/
private final float CFS_TO_ACFT = (float)1.9835;

/**
File format version as a String (e.g., "9.62").
Before version 11.0 there was no version in the binary file
so the code just reflects the documented format for the 9.62 version,
especially since no documentation exists for earlier versions of the binary files.
*/
private String __version = null;

/**
Program that created the file, typically "StateMod".
*/
private String __headerProgram = "";

/**
Date for the software version, in format YYYY/MM/DD.
*/
private String __headerDate = "";

// Data members.

/**
Name of the binary file being operated on (may or may not be an absolute path).
*/
private String __tsfile;

/**
Full path to binary file being operated on.  This is used as the key in the __file_Hashtable.
*/
private String __tsfileFull;

/**
Pointer to random access file (StateMod binary files are assumed to be little endian since they are written
by Lahey FORTRAN and gfortran code on a PC).
If necessary, the year value can be examined to determine the file endian-ness.
*/
private EndianRandomAccessFile __fp;

/**
A hash table for the file pointers (instances of StateMod_BTS).  This is used to increase performance.
*/
private static Hashtable<String,StateMod_BTS> __file_Hashtable = new Hashtable<String,StateMod_BTS>();

/**
Direct access file record length, bytes.  140 is the B43 for 9.62, but this is reset below.
Use a long to avoid casting when calculating position.
*/
private long __recordLength = 140;

/**
Length of the header in bytes, including lists of stations (everything before the time series data).
This is assigned after reading the number of stations.
The first record format various between versions but always fits into one record.
Use a long to avoid casting when calculating position.
*/
private long __headerLength = 0;

/**
Number of bytes for one full interval (month or day) of data for all stations to simplify iterations.
This is assigned after reading the number of stations.
Use a long to avoid casting when calculating position.
*/
private long __intervalBytes = 0;

/**
Estimated size of the file, calculated from the header information - used for
debugging and to check for premature end of file.
*/
private long __estimatedFileLengthBytes = 0;

/**
Interval base for the binary file that is being read.
*/
private int	__intervalBase = TimeInterval.MONTH;

/**
Start of the data, in calendar year, to proper date precision.
*/
private DateTime __date1 = null;

/**
End of the data, in calendar year, to proper date precision.
*/
private DateTime __date2 = null;

/**
Number of parameters for each data record, for the current file.
Set below depending on file contents and version.
This will be set equal to one of __ndivO, __nresO, __nwelO.
*/
private int __numparm = 0;

/**
Maximum length of parameter list, for all files.
*/
private int __maxparm = 0;

/**
Number of parameters specific to the diversion file.
*/
private int __ndivO = 0;

/**
Number of parameters specific to the reservoir file.
*/
private int __nresO = 0;

/**
Number of parameters specific to the well file.
*/
private int __nwelO = 0;

/**
List of the parameter names available to read, as read from the file and corrected (if necessary).
*/
private String  [] __parameters = null;
/**
List of upper case parameter names.
*/
private String  [] __parametersUpper = null;

/**
List of the parameter names available to read, as read from the file.
*/
private String  [] __parametersOrig = null;
/**
List of upper case original parameter names.
*/
private String  [] __parametersOrigUpper = null;

/**
Units for each parameter.
*/
private String  [] __unit = null;

/**
Component type for the binary file: COMP_DIVERSION_STATIONS, COMP_RESERVOIR_STATIONS, or COMP_WELL_STATIONS.
*/
private int __comp_type = StateMod_DataSet.COMP_UNKNOWN;

// Binary header information, according to the StateMod documentation.
// Currently, only the B43 header information is listed.

/**
Beginning year of simulation.
*/
private int	__iystr0 = 0;

/**
Ending year of simulation.
*/
private int	__iyend0 = 0;

/**
Number of river nodes.
*/
private int	__numsta = 0;

/**
Number of diversions.
*/
private int	__numdiv = 0;

/**
Number of instream flow locations.
*/
private int	__numifr = 0;

/**
Number of reservoirs.
*/
private int	__numres = 0;

/**
Number of reservoir owners.
*/
private int	__numown = 0;

/**
Number of active reservoirs.
*/
private int	__nrsact = 0;

/**
Number of baseflow (stream gage + stream estimate).
*/
private int	__numrun = 0;

/**
Number of wells.
*/
private int	__numdivw= 0;

/**
Number of ?
*/
private int	__numdxw = 0;

/**
List of month names, used to determine whether the data are water or calendar year.
*/
private String [] __xmonam = null;

/**
Number of days per month, corresponding to __xmonam.  This is used to convert CFS to ACFT.
Note February always has 28 days.
*/
private int [] __mthday = null;

/**
__mthday, always in calendar order.
*/
private int [] __mthdayCalendar = null;

/**
List of river node IDs.  The data records are in this order.
*/
private String[] __cstaid = null;

/**
Station names for river nodes.
*/
private String[] __stanam = null;

/**
List of diversion IDs.
*/
private String[] __cdivid = null;

/**
Diversion names.
*/
private String[] __divnam = null;

/**
River node position for diversion (1+).
*/
private int[] __idvsta = null;

/**
List of instream flow IDs.
*/
private String[] __cifrid = null;

/**
Instream flow names.
*/
private String[] __xfrnam = null;

/**
River node position for instream flow (1+).
*/
private int[] __ifrsta = null;

/**
List of reservoir IDs.
*/
private String[] __cresid = null;

/**
Reservoir names.
*/
private String[] __resnam = null;

/**
River node position for reservoir (1+).
*/
private int[] __irssta = null;

/**
Indicates whether reservoir is on or off.  Reservoirs that are off do not have output records.
*/
private int[] __iressw = null;

/**
Number of owners (accounts) for each reservoir, cumulative, and does not include, totals,
which are stored as account 0 for each reservoir.
*/
private int[] __nowner = null;

/**
Number of owners (accounts) for each reservoir (not cumulative like __nowner).
This DOES include the total account, which is account 0.
*/
private int[] __nowner2 = null;

/**
Number of owners (accounts) for each reservoir, cumulative, including the current reservoir.
This includes the total and is only for active reservoirs.
This is used when figuring out how many records to skip for previous stations.
*/
private int[] __nowner2_cum = null;

/**
Number of owners (accounts) for each reservoir, cumulative,
taking into account that inactive reservoirs are at
the end of the list of time series and can be ignored.
*/
private int[] __nowner2_cum2 = null;

/**
List of stream gage and stream estimate IDs (nodes that have baseflows).
*/
private String[] __crunid = null;

/**
Stream gage and stream estimate names.
*/
private String[] __runnam = null;

/**
River node position for station (1+).
*/
private int[] __irusta = null;

/**
List of well IDs.
*/
private String[] __cdividw = null;

/**
Well names.
*/
private String[] __divnamw = null;

/**
River node position for well (1+).
*/
private int[] __idvstw = null;

/**
 * Station types consistent with:
	int DIV = 0;  // Diversion stations
	int ISF = 1;  // Instream flow stations
	int RES = 2;  // Reservoir stations
	int BF = 3;   // Baseflow stations
	int WEL = 4;  // Wells
	int RIV = 5;  // River nodes (to find nodes only in RIN file)
*/
private String [] nodeTypes = {
	"Diversion",
	"InstreamFlow",
	"Resevoir",
	"Baseflow",
	"Well",
	"River"
};

/**
Open a binary StateMod binary time series file.  It is assumed that the file
exists and should be opened as read-only because typically only StateMod writes to the file.
The header information is immediately read and is available for access by other methods.
After opening the file, the readTimeSeries*() methods
can be called to read time series using time series identifiers.
@param tsfile Name of binary file to write.
@exception IOException if unable to open the file.
*/
public StateMod_BTS ( String tsfile )
throws IOException
{	// Initialize the file using the version in the header if available.
	initialize ( tsfile, "" );
}

/**
Open a binary StateMod binary time series file.  It is assumed that the file
exists and should be opened as read-only because typically only StateMod writes to the file.
The header information is immediately read and is available for access by other methods.
After opening the file, the readTimeSeries*() methods
can be called to read time series using time series identifiers.
@param tsfile Name of binary file to write.
@param fileVersion Version of StateMod that wrote the file.
This is used, for example, by some TSTool commands to read old file formats.
@exception IOException if unable to open the file.
*/
public StateMod_BTS ( String tsfile, String fileVersion )
throws IOException
{	initialize ( tsfile, fileVersion );
}

/**
Calculate the file position in bytes for any data value.
This DOES NOT position the file pointer!  For example, use this method as follows:
<pre>
// Find the month of data for a station and parameter.
long pos = calculateFilePosition ( date, ista, its, iparam );
// Position the file.
__fp.seek ( pos );
// Read the data.
param = __fp.readLittleEndianFloat ();
</pre>
@param date Date to find.  The month and year are considered by using an
absolute month offset (year*12 + month).
@param ista Station index (0+).  For streamflow/diversion/ISF, this is the
position in the river node list.  For reservoirs and wells, it is the position
in the specific list (which is in a different order than the river nodes).
@param its Time series for a location/parameter combination.  Normally this will
be zero.  For reservoirs, it will be zero for the total time series and 1+ for
the owner/accounts for a reservoir.
@param iparam Parameter to find (0+).
@return byte position in file for requested parameter or -1 if unable to calculate.
*/
private long calculateFilePosition ( DateTime date, int ista, int its, int iparam )
{	long pos = -1;
	// For very large files, the integer math below can result in positions that are larger
	// than integer maximum value 2147483647.  Therefore, make sure that input are long values.
	if ( __intervalBase == TimeInterval.MONTH ) {
		if ( __comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
			int nowner2_cum_prev = 0;
			if ( ista > 0 ) {
				nowner2_cum_prev = __nowner2_cum2[ista - 1];
			}
			pos = __headerLength // First static records + lists of stations Previous full months.
			+ (long)(date.getAbsoluteMonth() - __date1.getAbsoluteMonth())*__intervalBytes
			// Reservoir accounts.
			+ nowner2_cum_prev*__recordLength
			// Previous account time series for this station.
			+ its*__recordLength
			// Previous parameters for this station (each value is a 4-byte float).
			+ iparam*4L;
		}
		else {
		    // Non-reservoirs have only one time series per station.
			pos = __headerLength // First static records + lists of stations.
			+ (long)(date.getAbsoluteMonth() - __date1.getAbsoluteMonth())*__intervalBytes // Previous full months.
			+ ista*__recordLength // Previous stations for current month.
			+ iparam*4L; // Previous parameters for this station (each value is a 4-byte float).
		}
	}
	else {
	    // Daily data.
		if ( __comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
			int nowner2_cum_prev = 0;
			if ( ista > 0 ) {
				nowner2_cum_prev = __nowner2_cum2[ista - 1];
			}
			pos = __headerLength // First static records + lists of stations.
			// Previous full months.
			+ (long)(date.getAbsoluteMonth() - __date1.getAbsoluteMonth())*31L*__intervalBytes
			// Previous full days in month.
			+ (long)(date.getDay() - 1)*__intervalBytes
			// Reservoir accounts.
			+ nowner2_cum_prev*__recordLength
			// Previous account time series for this station.
			+ its*__recordLength +
			// Previous parameters for this station (each value is a 4-byte float).
			+ iparam*4L;
		}
		else {
		    // Non-reservoirs have only one time series per station.
			pos = __headerLength // First static records + lists of stations.
			// Previous full months.
			+ (long)(date.getAbsoluteMonth() - __date1.getAbsoluteMonth())*31L*__intervalBytes
			// Previous full days.
			+ (long)(date.getDay() - 1)*__intervalBytes
			// Previous stations for current day.
			+ ista*__recordLength
			// Previous parameters for this station (each value is a 4-byte float).
			+ iparam*4L;
		}
	}
	return pos;
}

/**
Close the binary time series file.
@exception IOException if there is an error closing the file.
*/
public void close()
throws IOException
{	__fp.close ();
	// Remove from the Hashtable.
	if ( __file_Hashtable.contains(this) ) {
		__file_Hashtable.remove ( __tsfileFull );
	}
}

/**
Close all the binary time series files that may have been opened.
@exception IOException if there is an error closing any file (all closes are
attempted and an Exception is thrown if any failed).
*/
public static void closeAll()
throws IOException
{	// TODO SAM 2004-08-23
	// Loop through the Hashtable and remove all entries.
	// Remove from the Hashtable.

	Enumeration<String> keysEnumeration = __file_Hashtable.keys();

	StateMod_BTS bts = null;
	String filename = null;

	while (keysEnumeration.hasMoreElements()) {
		filename = keysEnumeration.nextElement();	
		bts = __file_Hashtable.get(filename);
		bts.close();
		__file_Hashtable.remove ( filename );
	}	
}

/**
 * Convert the reservoir time series parameters to latest values.
 * This is needed because of bugs in the StateMod code 15.02.00 and earlier.
 * This method is called from 'readHeader' method.
 * @param parametersOrig the reservoir parameters read from the binary file
 * @return the parameters corresponding to the latest version
 */
private String [] convertReservoirParametersToLatest ( String [] parametersOrig ) {
	String routine = getClass().getSimpleName() + ".convertReservoirParametersToLatest";
	int totalReleaseCount = 0;
	int totalSupplyCount = 0;
	String [] parameters = new String[parametersOrig.length];
	for ( int iparam = 0; iparam < parametersOrig.length; ++iparam ) {
		// Initialize by copying the original parameter value.
		parameters[iparam] = new String(parametersOrig[iparam]);
		if ( parametersOrig[iparam].equalsIgnoreCase("Total_Release") ) {
			++totalReleaseCount;
			if ( (totalReleaseCount == 2) && (iparam == 19) ) {
				// StateMod <= 15.00.02 should be River_Release.
				parameters[iparam] = "River_Release";
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, "Changing second occurrence of parameter 'Total_Release' to 'River_Release'." );
				}
			}
		}
		else if ( parametersOrig[iparam].equalsIgnoreCase("Total_Supply") ) {
			++totalSupplyCount;
			if ( (totalSupplyCount == 2) && (iparam == 20) ) {
				// StateMod <= 15.00.02 should be River_Divert.
				parameters[iparam] = "River_Divert";
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, "Changing second occurrence of parameter 'Total_Supply' to 'River_Divert'." );
				}
			}
		}
	}
	return parameters;
}

// TODO SAM 2006-01-15 If it becomes important to read versions before 9.69,
// add logic to check the file size and estimate from that the record length
// that was used, and hence the file version.
/**
Determine the StateMod binary file version.
For StateMod version 11.x+, the file version can be determined from the binary file header.
For older versions, version 9.69 is returned, since this version has been in use for some time and is likely.
@param filename the path to the file to check.  No adjustments to the path
are made; therefore a full path should be provided to prevent errors.
@exception Exception if there is an error opening or reading the file.
*/
public static String determineFileVersion ( String filename )
throws Exception
{	StateMod_BTS bts = new StateMod_BTS ( filename );
	String version = bts.getVersion();
	bts.close();
	bts = null;
	return version;
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	__fp.close();
	__fp = null;
	__tsfile = null;
	__tsfileFull = null;
	super.finalize();
}

/**
Return the interval base (TimeInterval.MONTH or TimeInterval.DAY).
@return the data interval base.
*/
public int getDataIntervalBase ()
{	return __intervalBase;
}

/**
Return the first date in the period.
@return the first date in the period.
*/
public DateTime getDate1 ()
{	return __date1;
}

/**
Return the last date in the period.
@return the last date in the period.
*/
public DateTime getDate2 ()
{	return __date2;
}

/**
Return the parameter list for the file, which is determined from the file
header for version 11.x+ and is unknown otherwise.
Only the public parameters are provided (not extra ones that may be used internally).
@return the parameter list read from the file header, or null if it cannot be determined from the file.
*/
public String [] getParameters ()
{	// Return a copy of the array for only the appropriate parameters.
	// The array by default will have __maxparm items, but not all of these
	// are appropriate for other applications.
	if ( (__numparm == 0) || (__parameters == null) ) {
		return null;
	}
	String [] parameter = new String[__numparm];
	for ( int i = 0; i < __numparm; i++ ) {
		parameter[i] = __parameters[i];
	}
	return parameter;
}

// TODO SAM 2006-01-15 If resources allow, remember to estimate the file version by back-calculating from the file size.
/**
Return the version of the file (the StateMod version that wrote the file).
This information is determined from the file header for version 11.x+ and is unknown otherwise. 
@return the file format version.
*/
public String getVersion ()
{	return __version;
}

/**
Initialize the binary file.  The file is opened and the header is read.
@param tsfile Name of binary file.
@param fileVersion Version of StateMod that wrote the file.
Used to be a double like "9.01" but can now be a three-part version like "10.01.01".
@exception IOException If the file cannot be opened or read.
*/
private void initialize ( String tsfile, String fileVersion )
throws IOException
{	String routine = "StateMod_BTS.initialize";
	__tsfile = tsfile;
	__version = "";
	if ( (fileVersion != null) && !fileVersion.equals("") ) {
	    __version = fileVersion;
	    if ( __version.startsWith("9.") ) {
	        // Add a leading 0 so that version string comparisons work.
	        __version = "0" + __version;
	    }
	}

	// TODO SAM 2003? - for different file extensions, change the interval to TimeInterval.DAY if necessary.

	// Open the binary file as a random access endian file.
	// This allows big-endian Java to read the little-endian (Microsoft/Lahey) file.

	__tsfileFull = IOUtil.getPathUsingWorkingDir ( tsfile );
	__fp = new EndianRandomAccessFile ( __tsfileFull, "r" );

	// Initialize important data.

	__file_Hashtable.put(__tsfileFull, this);

	__intervalBase = TimeInterval.MONTH; // Default.
	String extension = IOUtil.getFileExtension ( __tsfile );

	// Read the file header version.

	if ( __version.equals("") ) {
	    readHeaderVersion ();
	}

	if ( extension.equalsIgnoreCase("b43") ) {
		// Diversions, instream flow, stream (monthly).
		// Use the diversion parameter list since it is the full list.
		__comp_type = StateMod_DataSet.COMP_DIVERSION_STATIONS;
		if ( StateMod_Util.isVersionAtLeast(__version, StateMod_Util.VERSION_11_00) ) {
			__recordLength = 160;
		} 
		else {
		    __recordLength = 140;
		}
	}
	else if ( extension.equalsIgnoreCase("b49") ) {
		// Diversions, instream flow, stream (daily).
		// Use the diversion parameter list since it is the full list.
		__intervalBase = TimeInterval.DAY;
		__comp_type = StateMod_DataSet.COMP_DIVERSION_STATIONS;
		if ( StateMod_Util.isVersionAtLeast(__version, StateMod_Util.VERSION_11_00) ) {
			__recordLength = 160;
		} 
		else {
		    __recordLength = 144;
		}
	}
	else if ( extension.equalsIgnoreCase("b44") ) {
		// Reservoirs (monthly).
		__comp_type = StateMod_DataSet.COMP_RESERVOIR_STATIONS;
		if ( StateMod_Util.isVersionAtLeast(__version, StateMod_Util.VERSION_11_00) ) {
			__recordLength = 160;
		} 
		else {
		    __recordLength = 96;
		}
	}
	else if ( extension.equalsIgnoreCase("b50") ) {
		// Reservoirs (daily).
		__intervalBase = TimeInterval.DAY;
		__comp_type = StateMod_DataSet.COMP_RESERVOIR_STATIONS;
		if ( StateMod_Util.isVersionAtLeast(__version, StateMod_Util.VERSION_11_00) ) {
			__recordLength = 160;
		}
		else {
		    __recordLength = 96;
		}
	}
	else if ( extension.equalsIgnoreCase("b42") ) {
		// Well (monthly).
		__comp_type = StateMod_DataSet.COMP_WELL_STATIONS;
		// Same for all versions.
		__recordLength = 92;
	}
	else if ( extension.equalsIgnoreCase("b65") ) {
		// Well (daily).
		__intervalBase = TimeInterval.DAY;
		__comp_type = StateMod_DataSet.COMP_WELL_STATIONS;
		// Same for all versions.
		__recordLength = 92;
	}

	// Read the file header.

	readHeader ();

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Parameters are as follows, where "+
		"the number equals iparam in following messages." );
		for ( int iparam = 0; iparam < __numparm; iparam++ ) {
			Message.printDebug ( 1, "", "Parameter [" + iparam + "]=\"" + __parameters[iparam] +"\"");
		}
	}

	// Set the dates.

	if ( __intervalBase == TimeInterval.MONTH ) {
		__date1 = new DateTime ( DateTime.PRECISION_MONTH );
		__date2 = new DateTime ( DateTime.PRECISION_MONTH );
	}
	else {
	    __date1 = new DateTime ( DateTime.PRECISION_DAY );
		__date2 = new DateTime ( DateTime.PRECISION_DAY );
	}

	// Set the day in all cases.  It will be ignored with monthly data.

	if ( __xmonam[0].equalsIgnoreCase("JAN") ) {
		// Calendar.
		__date1.setYear ( __iystr0 );
		__date1.setMonth ( 1 );
		__date2.setYear ( __iyend0 );
		__date2.setMonth ( 12 );
	}
	else if ( __xmonam[0].equalsIgnoreCase("OCT") ) {
		// Water year.
		__date1.setYear ( __iystr0 - 1 );
		__date1.setMonth ( 10 );
		__date2.setYear ( __iyend0 );
		__date2.setMonth ( 9 );
	}
	else if ( __xmonam[0].equalsIgnoreCase("NOV") ) {
		// Irrigation year.
		__date1.setYear ( __iystr0 - 1 );
		__date1.setMonth ( 11 );
		__date2.setYear ( __iyend0 );
		__date2.setMonth ( 10 );
	}
	__date1.setDay ( 1 );
	__date2.setDay ( TimeUtil.numDaysInMonth(__date2) );

	// Header length, used to position for data records.
	long offset = 0;
	if ( StateMod_Util.isVersionAtLeast(__version, StateMod_Util.VERSION_11_00) ) {
		// Offset in addition to header in older format files.
		offset = 1L // Program version info.
			+ __maxparm*3L // Parameter list.
			+ 1L; // Unit.
	}
	int resTotalAccountRec = 1;
	__headerLength = __recordLength*(
			offset // Header rec + parameters for new files.
			+ 4L // Period, counts, month names, month days.
			+ __numsta // Number of river nodes.
			+ __numdiv // Number of diversions.
			+ __numifr // Number of ISF reaches.
			+ (__numres + resTotalAccountRec) // Number of reservoirs + total record.
			+ __numrun // Number of base flows.
			+ __numdivw ); // Number of D&W nodes.
	// Number of bytes for one interval (one month or one day) of data for all stations.
	if ( __comp_type == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
		__intervalBytes = __recordLength*__numsta;
	}
	else if ( __comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
		// Include owner/account records in addition to the 1 record
		// for each reservoir for the total account.
		// Inactive reservoirs have the accounts included at the end,
		// but no total (__nowner2_cum is used for the block size but
		// __nowner2_cum2 is used to locate specific time series).
		if ( this.__numres == 0 ) {
			// No data will be present in the file so set to zero.
			__intervalBytes = 0;
		}
		else {
			__intervalBytes = __recordLength*__nowner2_cum[__numres - 1];
		}
	}
	else if ( __comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
		__intervalBytes = __recordLength*__numdivw;
	}

	// Estimated file length.

	if ( __intervalBase == TimeInterval.MONTH ) {
		__estimatedFileLengthBytes = __headerLength + __intervalBytes*12*(__iyend0 - __iystr0 + 1);
	}
	else {
	    // One set of parameters per station.
		__estimatedFileLengthBytes = __headerLength + __intervalBytes*12*31*(__iyend0 - __iystr0 + 1);
	}
	if ( Message.isDebugOn ) {
		if ( __intervalBase == TimeInterval.MONTH ) {
			Message.printDebug ( 1, routine, "Reading monthly data." );
		}
		else {
		    Message.printDebug ( 1, routine, "Reading daily data." );
		}
		Message.printDebug ( 1, routine, "Length of 1 record (bytes) = " + __recordLength );
		Message.printDebug ( 1, routine, "Header length (bytes) = " + __headerLength );
		if ( __comp_type == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
			Message.printDebug ( 1, "", "Number of stations in data set = " + __numsta );
		}
		else if ( __comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
			Message.printDebug ( 1, routine, "Number of reservoirs in data set = " + __numres);
			Message.printDebug ( 1, routine,
			"Total number of reservoir accounts (does not include "+
			"total accounts, does include inactive) = " + __numown);
		}
		else if ( __comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
			Message.printDebug ( 1, routine, "Number of wells in data set = " + __numdivw );
		}
		if ( __intervalBase == TimeInterval.MONTH ) {
			Message.printDebug ( 1, routine, "Length of 1 complete month (bytes) = " + __intervalBytes );
		}
		else {
		    // Daily.
			Message.printDebug ( 1, routine, "Length of 1 complete day (bytes) = " +	__intervalBytes );
		}
		Message.printDebug ( 1, routine, "Estimated file size (bytes) = " + __estimatedFileLengthBytes );
	}

	if ( IOUtil.testing() ) {
		try {
		    printRecords0 ();
			//printRecords ( 10 );
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, "Error printing records." );
			Message.printWarning ( 3, routine, e );
		}
	}
}

/**
Look up the file pointer to use when opening a new file.  If the file is already
open and is in the internal __file_HashTable, use it.  Otherwise, open the file and add it to the Hashtable.
The code to close the file must remove the file from the Hashtable.
@param full_fname Full path to file to open.
*/
private static StateMod_BTS lookupStateModBTS ( String full_fname )
throws Exception
{	String routine = "StateMod_BTS.lookupStateModBTS";
	Object o = __file_Hashtable.get ( full_fname );
	if ( o != null ) {
		// Have a matching file pointer so assume that it can be used.
		Message.printStatus(2, routine, "Using existing binary file.");
		return (StateMod_BTS)o;
	}
	// Else create a new file.
	Message.printStatus(2, routine, "Opening new binary file.");
	StateMod_BTS bts = new StateMod_BTS ( full_fname );
	// Add to the HashTable.
	__file_Hashtable.put ( full_fname, bts );
	return bts;
}

/**
Test code to print records, brute force until data runs out.
@param max_stations Indicate the maximum number of stations to print.
*/
private void printRecords0 ()
throws Exception
{   String routine = "StateMod_BTS.printRecords0";
	StringBuffer b = new StringBuffer();
	double value = 0.0;
	int iparm;
	for ( int irec = 0; irec >= 0; irec++ ) {
		try {
		    __fp.seek ( __headerLength + irec*__recordLength );
		}
		catch ( Exception e ) {
			// End of data.
			break;
		}
		b.setLength(0);
		b.append ( StringUtil.formatString(irec,"%4d") + " " );
		for ( iparm = 0; iparm < __numparm; iparm++ ) {
			try {
			    value = __fp.readLittleEndianFloat();
			}
			catch ( Exception e ) {
				// End of data.
				irec = -2;
				break;
			}
			b.append ( " "+StringUtil.formatString(value,"%#7.0f"));
		}
		Message.printStatus ( 2, routine, b.toString() );
	}
}

/**
Test code to print records, trying to do so intelligently.
The output in the log file can be sorted and
compared against the standard *.xdd, *.xre, etc. reports.
@param max_stations Indicate the maximum number of stations to print.
*/
/* TODO SAM Evaluate use.
private void printRecords ( int max_stations )
throws Exception
{
	__fp.seek ( __header_length );
	StringBuffer b = new StringBuffer();
	float value;
	int irec = 0;
	int iaccount = 0;
	int naccount = 1; // For non-reservoirs.
	int iy, im, ista, iparm, year, month;
	boolean do_res = false;
	int numsta = __numsta;	// Streamflow, diversion, ISF
	if (	StringUtil.endsWithIgnoreCase(__tsfile,"b44") ||
		StringUtil.endsWithIgnoreCase(__tsfile,"b50") ) {
		do_res = true;
		max_stations = __numres;	// Show them all
		numsta = __numres;
	}
	boolean do_well = false;
	if (	StringUtil.endsWithIgnoreCase(__tsfile,"b42") ||
		StringUtil.endsWithIgnoreCase(__tsfile,"b65") ) {
		do_well = true;
		numsta = __numdivw;
	}
	boolean do_water = false;
	if ( __xmonam[0].equalsIgnoreCase("Oct") ) {
		do_water = true;
	}
	boolean do_month = true;
	if (	StringUtil.endsWithIgnoreCase(__tsfile,"b49") ||
		StringUtil.endsWithIgnoreCase(__tsfile,"b50") ||
		StringUtil.endsWithIgnoreCase(__tsfile,"b65") ) {
		do_month = false;
	}
	// Read the rest of the file and just print out the values...
	// For stream/diversion/ISF, loop through river station list.
	// For reservoirs and wells, loop through the specific lists.
	String [] id_array = __cstaid;
	for ( iy = __iystr0; iy <= __iyend0; iy++ ) {
		for ( im = 0; im < 12; im++ ) {
		for ( ista = 0; ista < numsta; ista++ ) {
			// TODO SAM 2005-12-22 need special handling of other nodes as well.
			if ( do_res ) {
				naccount = __nowner2[ista];
				id_array = __cresid;
			}
			else if ( do_well ) {
				id_array = __cdividw;
			}
			// Else in the diversion binary files, all river nodes are listed.
			for (	iaccount=0;
				iaccount<naccount;
				iaccount++, irec++ ){
				if ( ista < max_stations ) {
				// Read and print the data (otherwise just
				// rely on the record counter to increment in
				// the "for" statement)...
				__fp.seek ( __header_length +
						irec*__record_length );
				b.setLength(0);
				year = iy;
				month = im + 1;
				if ( do_water ) {
					if ( im < 3 ) {
						month = im + 10;
						year = iy - 1;
					}
					else {	month = im - 2;
					}
				}
				// List this way so that output can be sorted
				// by station and then time...
				b.append ( StringUtil.formatString(
					id_array[ista],"%-12.12s") + " " +
					StringUtil.formatString(iaccount,
					"%2d"));
				b.append ( " " + year + " " +
					StringUtil.formatString(month,"%02d")
					+ " " + __xmonam[im]);
				for (	iparm = 0; iparm < __numparm;
					iparm++ ) {
					value = __fp.readLittleEndianFloat();
					if ( do_month ) {
						// Convert CFS to ACFT
						value = value*
						CFS_TO_ACFT* (float)
						__mthday2[month - 1];
					}
					b.append ( " " +StringUtil.formatString(
					value,"%#7.0f") );
				}
				Message.printStatus ( 2, routine, b.toString() );
				}
			}
		}
		}
	}
}
*/

/**
Read the header from the opened binary file and save the information in memory for fast lookups.
The header is the same for all of the binary output files.
@exception IOException if there is an error reading from the file.
*/
private void readHeader ( )
throws IOException
{	String routine = "StateMod_BTS.readHeader";
	int dl = 1;

	long header_rec = 0;
	if ( StateMod_Util.isVersionAtLeast(this.__version, StateMod_Util.VERSION_11_00) ) {
		// Need to skip the first record that has the file version information.
		// The period for the file is in record 2.
		header_rec = 1;
	}
	else {
	    // Old format that has period for the file in record 1.
		header_rec = 0;
		// Below we refer to record 2 since this is the newest format.
	}
	this.__fp.seek ( header_rec*this.__recordLength );

	// Record 2 - start and end year - check the months in record 3 to determine the year type.

	this.__iystr0 = this.__fp.readLittleEndianInt ();
	this.__iyend0 = this.__fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug (dl,routine,"Reading binary file header...");
		Message.printDebug ( dl, routine, "iystr0=" + this.__iystr0 );
		Message.printDebug ( dl, routine, "iyend0=" + this.__iyend0 );
	}

	// Record 3 - numbers of various stations.

	this.__fp.seek ( (header_rec + 1L)*this.__recordLength );
	this.__numsta = this.__fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numsta=" + this.__numsta );
	}
	this.__numdiv = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numdiv=" + this.__numdiv );
	}
	this.__numifr = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numifr=" + this.__numifr );
	}
	this.__numres = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numres=" + this.__numres );
	}
	this.__numown = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numown=" + this.__numown );
	}
	this.__nrsact = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "nrsact=" + this.__nrsact );
	}
	this.__numrun = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numrun=" + this.__numrun );
	}
	this.__numdivw= __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numdivw=" + this.__numdivw );
	}
	this.__numdxw = __fp.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "numdxw=" + this.__numdxw );
	}
	if ( StateMod_Util.isVersionAtLeast(this.__version, StateMod_Util.VERSION_11_00) ) {
		this.__maxparm = this.__fp.readLittleEndianInt ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "maxparm=" + this.__maxparm );
		}
		this.__ndivO = this.__fp.readLittleEndianInt ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "ndivO=" + this.__ndivO );
		}
		this.__nresO = this.__fp.readLittleEndianInt ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "nresO=" + this.__nresO );
		}
		this.__nwelO = this.__fp.readLittleEndianInt ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "nwelO=" + this.__nwelO );
		}
	}

	// Record 4 - month names.

	this.__fp.seek ( (header_rec + 2L)*this.__recordLength );
	this.__xmonam = new String[14];
	char [] xmonam = new char[3];
	int j = 0;
	for ( int i = 0; i < 14; i++ ) {
		// The months are written as 4-character strings but only need the first 3.
		for ( j = 0; j < 3; j++ ) {
			xmonam[j] = this.__fp.readLittleEndianChar1();
		}
		this.__fp.readLittleEndianChar1();
		this.__xmonam[i] = new String ( xmonam );
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "xmonam[" + i + "]=" + this.__xmonam[i]);
		}
	}

	// Record 5 - number of days per month.

	this.__fp.seek ( (header_rec + 3L)*this.__recordLength );
	this.__mthday = new int[12];
	for ( int i = 0; i < 12; i++ ) {
		this.__mthday[i] = this.__fp.readLittleEndianInt();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "mthday[" + i + "]="+ this.__mthday[i]);
		}
	}
	// Create a new array that is always in calendar order.
	// Therefore, __mthday2[0] always has the number of days for January.
	this.__mthdayCalendar = new int[12];
	if ( this.__xmonam[0].equalsIgnoreCase("OCT") ) {
	    // Water year.
		this.__mthdayCalendar[9] = this.__mthday[0];	// Oct.
		this.__mthdayCalendar[10] = this.__mthday[1];
		this.__mthdayCalendar[11] = this.__mthday[2];
		this.__mthdayCalendar[0] = this.__mthday[3];	// Jan.
		this.__mthdayCalendar[1] = this.__mthday[4];
		this.__mthdayCalendar[2] = this.__mthday[5];
		this.__mthdayCalendar[3] = this.__mthday[6];
		this.__mthdayCalendar[4] = this.__mthday[7];
		this.__mthdayCalendar[5] = this.__mthday[8];
		this.__mthdayCalendar[6] = this.__mthday[9];
		this.__mthdayCalendar[7] = this.__mthday[10];
		this.__mthdayCalendar[8] = this.__mthday[11];
	}
	else if ( this.__xmonam[0].equalsIgnoreCase("NOV") ) {
	    // Irrigation year.
		this.__mthdayCalendar[10] = this.__mthday[0];	// Nov.
		this.__mthdayCalendar[11] = this.__mthday[1];
		this.__mthdayCalendar[0] = this.__mthday[2];	// Jan.
		this.__mthdayCalendar[1] = this.__mthday[3];
		this.__mthdayCalendar[2] = this.__mthday[4];
		this.__mthdayCalendar[3] = this.__mthday[5];
		this.__mthdayCalendar[4] = this.__mthday[6];
		this.__mthdayCalendar[5] = this.__mthday[7];
		this.__mthdayCalendar[6] = this.__mthday[8];
		this.__mthdayCalendar[7] = this.__mthday[9];
		this.__mthdayCalendar[8] = this.__mthday[10];
		this.__mthdayCalendar[9] = this.__mthday[11];
	}
	else {
	    // Calendar.
		this.__mthdayCalendar = this.__mthday;
	}

	// Record 6 - river stations.

	long offset2 = (header_rec + 4L)*this.__recordLength;
	this.__cstaid = new String[this.__numsta];
	this.__stanam = new String[this.__numsta];
	int counter = 0;
	for ( int i = 0; i < this.__numsta; i++ ) {
		this.__fp.seek ( offset2 + i*this.__recordLength );
		// Counter.
		counter = this.__fp.readLittleEndianInt();
		// Identifier as 12 character string.
		this.__cstaid[i] = this.__fp.readLittleEndianString1(12).trim();
		// Station name as 24 characters, written as 6 reals.
		this.__stanam[i] = this.__fp.readLittleEndianString1(24).trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "" + counter +
			" Riv = \"" + this.__cstaid[i] + "\" \"" + this.__stanam[i]+"\"");
		}
	}

	// Read the station ID/name lists.
	// The station IDs are matched against the requested TSID to find the position in the data array.

	// Record 7 - diversion stations.

	offset2 = (header_rec + 4L + this.__numsta)*this.__recordLength;
	if ( this.__numdiv > 0 ) {
		this.__cdivid = new String[this.__numdiv];
		this.__divnam = new String[this.__numdiv];
		this.__idvsta = new int[this.__numdiv];
	}
	for ( int i = 0; i < this.__numdiv; i++ ) {
		this.__fp.seek ( offset2 + i*this.__recordLength );
		// Counter.
		counter = this.__fp.readLittleEndianInt();
		// Identifier as 12 character string.
		this.__cdivid[i] = this.__fp.readLittleEndianString1(12).trim();
		// Station name as 24 characters, written as 6 reals.
		this.__divnam[i] = this.__fp.readLittleEndianString1(24).trim();
		this.__idvsta[i] = this.__fp.readLittleEndianInt();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "" + counter +
			" Div = \"" + this.__cdivid[i] + "\" \"" + this.__divnam[i] +
			"\" idvsta = " + this.__idvsta[i]);
		}
	}

	// Record 8 - instream flow stations.

	offset2 = (header_rec + 4L + this.__numsta + this.__numdiv)*this.__recordLength;
	if ( this.__numifr > 0 ) {
		this.__cifrid = new String[this.__numifr];
		this.__xfrnam = new String[this.__numifr];
		this.__ifrsta = new int[this.__numifr];
	}
	for ( int i = 0; i < this.__numifr; i++ ) {
		this.__fp.seek ( offset2 + i*this.__recordLength );
		// Counter.
		counter = this.__fp.readLittleEndianInt();
		// Identifier as 12 character string.
		this.__cifrid[i] = this.__fp.readLittleEndianString1(12).trim();
		// Station name as 24 characters, written as 6 reals.
		this.__xfrnam[i] = this.__fp.readLittleEndianString1(24).trim();
		this.__ifrsta[i] = this.__fp.readLittleEndianInt();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "" + counter +
			" Ifr = \"" + this.__cifrid[i] + "\" \"" + this.__xfrnam[i] +	"\" ifrsta = " + this.__ifrsta[i] );
		}
	}

	// Record 9 - reservoir stations.

	offset2 = (header_rec + 4L + this.__numsta + this.__numdiv + this.__numifr)*this.__recordLength;
	// The value of __nowner is the record number (1+) of the first owner for the current reservoir.
	// Therefore, the number of owners is calculated for the current reservoir by taking
	// the number of owners in record (i + 1) minus the value in the current record.
	// Therefore the last record is necessary to calculate the number of owners for the last reservoir.
	// For example:
	//
	// __nowner[0] = 1
	// __nowner[1] = 5
	//
	// Indicates that the first reservoir has 5 - 1 = 4 accounts not counting the total.
	// Therefore, for this example the accounts would be:
	//
	// 0 - total
	// 1 - Account 1
	// 2 - Account 2
	// 3 - Account 3
	// 4 - Account 4
	//
	// Below, allocate the reservoir arrays one more than the actual number of reservoirs to capture the extra data,
	// but __numres reflects only the actual reservoirs, for later processing.
	int iend = this.__numres + 1;
	if ( iend > 0 ) {
		this.__cresid = new String[iend];
		this.__resnam = new String[iend];
		this.__irssta = new int[iend];
		this.__iressw = new int[iend];
		this.__nowner = new int[iend];
	}
	if ( this.__numres >= 0 ) {
		// Zero-length arrays will be created if no reservoirs.
		this.__nowner2 = new int[this.__numres];
		this.__nowner2_cum = new int[this.__numres];
		this.__nowner2_cum2 = new int[this.__numres];
	}
	for ( int i = 0; i < iend; i++ ) {
		this.__fp.seek ( offset2 + i*this.__recordLength );
		// Counter.
		counter = this.__fp.readLittleEndianInt();
		// Identifier as 12 character string.
		this.__cresid[i] = this.__fp.readLittleEndianString1(12).trim();
		// Station name as 24 characters, written as 6 reals.
		this.__resnam[i] = this.__fp.readLittleEndianString1(24).trim();
		this.__irssta[i] = this.__fp.readLittleEndianInt();
		this.__iressw[i] = this.__fp.readLittleEndianInt();
		this.__nowner[i] = this.__fp.readLittleEndianInt();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "" + counter +
			" Res = \"" + this.__cresid[i] + "\" \"" + this.__resnam[i] + "\" irssta = " + this.__irssta[i] +
			" iressw=" + this.__iressw[i] + " nowner = " + this.__nowner[i] );
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "The last reservoir record is used to compute the number of accounts.");
		Message.printDebug ( dl, routine, "In the following, nowner2 is active accounts, including total." );
		Message.printDebug ( dl, routine, "nowner2_cum is cumulative, including the current reservoir." );
		Message.printDebug ( dl, routine,
		"nowner2_cum2 is cumulative, including the current reservoir," +
		" with inactive reservoir accounts at the end." );
	}
	// Compute the actual number of accounts for each reservoir (with the total).
	// Only active reservoirs are counted.
	for ( int i = 0; i < this.__numres; i++ ) {
		// The total number of accounts for reservoirs (from StateMod documentation) is:
		//     Nrsactx = nrsact + numown
		// where the "nrsact" accounts for the "total" time series and
		// "numown" accounts for time series for each account,
		// whether active or not.  For each reservoir, the individual accounts
		// are included (whether the reservoir is active or not),
		// but the total account is only included if the reservoir is active (see the "else" below).
		this.__nowner2[i] = this.__nowner[i + 1] - this.__nowner[i]; // Accounts but no total.
		if ( this.__iressw[i] != 0 ) {
			// Reservoir is active so add the total.
			this.__nowner2[i] += 1;
		}
		// Cumulative accounts (including totals), inclusive of the current reservoir station.
		if ( i == 0 ) {
			// Initialize.
			this.__nowner2_cum[i] = this.__nowner2[i];
			if ( this.__iressw[i] == 0 ) {
				// Position for reading is zero (this time series will never be read but the array
				// is used to increment later elements).
				this.__nowner2_cum2[i] = 0;
			}
			else {
			    this.__nowner2_cum2[i] = this.__nowner2[i];
			}
		}
		else {
		    // Add the current accounts to the previous cumulative value.
			this.__nowner2_cum[i] = this.__nowner2_cum[i - 1] + this.__nowner2[i];
			if ( this.__iressw[i] == 0 ) {
				// Position for reading stays the same (this time series will never be read but the array
				// is used to increment later elements).
				this.__nowner2_cum2[i] = this.__nowner2_cum2[i - 1];
			}
			else {
			    // Increment the counter.
				this.__nowner2_cum2[i] = this.__nowner2_cum2[i - 1] + this.__nowner2[i];
			}
		}
		Message.printDebug ( dl, routine, 
		" Res = \"" + this.__cresid[i] + "\" \"" + this.__resnam[i] + "\" nowner2 = " + this.__nowner2[i] +
		" nowner2_cum = " + this.__nowner2_cum[i] + " nowner2_cum2 = " + this.__nowner2_cum2[i] );
	}

	// A single record after reservoirs contains cumulative account information for reservoirs.
	// Just ignore the record and add a +1 below when computing the position.

	// Record 10 - base flow stations.

	int resTotalAccountRec = 1;
	offset2 = (header_rec + 4L + this.__numsta + this.__numdiv + this.__numifr + this.__numres + resTotalAccountRec)*this.__recordLength;
	if ( this.__numrun > 0 ) {
		this.__crunid = new String[this.__numrun];
		this.__runnam = new String[this.__numrun];
		this.__irusta = new int[this.__numrun];
	}
	for ( int i = 0; i < this.__numrun; i++ ) {
		this.__fp.seek ( offset2 + i*this.__recordLength );
		// Counter.
		counter = this.__fp.readLittleEndianInt();
		// Identifier as 12 character string.
		this.__crunid[i] = this.__fp.readLittleEndianString1(12).trim();
		// Station name as 24 characters, written as 6 reals.
		this.__runnam[i] = this.__fp.readLittleEndianString1(24).trim();
		this.__irusta[i] = this.__fp.readLittleEndianInt();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "" + counter +
			" Baseflow = \"" + this.__crunid[i] + "\" \"" + this.__runnam[i] + "\" irusta = " + this.__irusta[i] );
		}
	}

	// Record 11 - well stations.

	offset2 = (header_rec + 4L + this.__numsta + this.__numdiv + this.__numifr + this.__numres + resTotalAccountRec + this.__numrun)* this.__recordLength;
	if ( this.__numdivw > 0 ) {
		this.__cdividw = new String[this.__numdivw];
		this.__divnamw = new String[this.__numdivw];
		this.__idvstw = new int[this.__numdivw];
	}
	for ( int i = 0; i < this.__numdivw; i++ ) {
		this.__fp.seek ( offset2 + i*this.__recordLength );
		// Counter.
		counter = this.__fp.readLittleEndianInt();
		// Identifier as 12 character string.
		this.__cdividw[i] = this.__fp.readLittleEndianString1(12).trim();
		// Station name as 24 characters, written as 6 reals.
		this.__divnamw[i] = this.__fp.readLittleEndianString1(24).trim();
		this.__idvstw[i] = this.__fp.readLittleEndianInt();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "" + counter +
			" Well = \"" + this.__cdividw[i] + "\" \"" + this.__divnamw[i] + "\" idvstw = " + this.__idvstw[i] );
		}
	}

	// Get the parameters that are expected in the file.
	// Note that this is the full list in the file, not the list that may be appropriate for the station type.
	// It is assumed that code that the calling code is requesting an appropriate parameter.
	// For example, the StateMod GUI graphing tool should have already filtered the parameters based on station type.
	
	if ( StateMod_Util.isVersionAtLeast(this.__version, StateMod_Util.VERSION_11_00) ) {
	    if ( Message.isDebugOn ) {
	        Message.printDebug( dl, routine, "Reading parameters from file." );
	    }
		// Read the parameter lists and units from the file.
	    // The parameters that are available are the same for streamflow, diversions, and wells.
		String [] parameters = null;	// Temporary list - will only save the one that is needed for this file.
		// TODO SAM 2005-12-23
		// For now read through but later can just read the set of
		// parameters that are actually needed for this file.
		String extension = IOUtil.getFileExtension(this.__tsfile);
		for ( int ip = 0; ip < 3; ip++ ) {
			// Parameter lists for div, res, well binary files.
			offset2 = (header_rec + 4L + this.__numsta + this.__numdiv + this.__numifr + (this.__numres + resTotalAccountRec) +
				this.__numrun + this.__numdivw + ip*this.__maxparm)*this.__recordLength;
			if ( this.__maxparm > 0 ) {
				// Reallocate each time so as to not step on the saved array below.
				parameters = new String[this.__maxparm];
			}
			for ( int i = 0; i < this.__maxparm; i++ ) {
				// 4 is to skip the counter at the beginning of the line.
				this.__fp.seek ( offset2 + i*this.__recordLength + 4L );
				parameters[i] = this.__fp.readLittleEndianString1(24).trim();
				//* Use during development.
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Read parameter from file " + extension + ", ip=" + ip +
					", parameter[" + i + "] = \"" + parameters[i] + "\"" );
				}
				//*/
			}
			// Because __maxparm only lists the maximum,
			// reduce the list by decrementing the count if "NA" is at the end.
			// Also save the information if for the proper file.
			if ( (ip == 0) && (this.__comp_type == StateMod_DataSet.COMP_DIVERSION_STATIONS) ) {
				// Original parameters are the same as current parameters.
				this.__parametersOrig = parameters;
				this.__parameters = parameters;
				this.__numparm = this.__ndivO;
				Message.printStatus ( 2, routine, "Saving diversion parameters list." );
			}
			else if ( (ip == 1) && (this.__comp_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS) ) {
				// Reservoir parameters need fixed due to known bug in version 15.00.02 and earlier format.
				this.__parametersOrig = parameters;
				// Convert parameters to latest version to deal with known bug in older output:
				// - 15.00.02 and earlier had errors in the parameter list
				this.__parameters = convertReservoirParametersToLatest ( this.__parametersOrig );
				this.__numparm = this.__nresO;
				Message.printStatus ( 2, routine, "Saving reservoir parameters list." );
			}
			else if ( (ip == 2) && (this.__comp_type == StateMod_DataSet.COMP_WELL_STATIONS) ) {
				// Original parameters are the same as current parameters.
				this.__parametersOrig = parameters;
				this.__parameters = parameters;
				this.__numparm = this.__nwelO;
				Message.printStatus ( 2, routine, "Saving well parameters list." );
			}
		}
	}
	else {
	    // Get the parameter list from hard-coded lists.
	    Message.printDebug( dl, routine, "Getting parameters from hard-coded lists for older file version." );
		this.__parameters = StringUtil.toArray (
			StateMod_Util.getTimeSeriesDataTypes (
			this.__comp_type, // Component.
			null, // ID - we want all in the file.
			null, // DataSet - for now assume none.
			this.__version,	// Really need to get this from the file!
			this.__intervalBase,
			false, // Do not include input parameters.
			false, // Do not include estimated input parameters.
			true, // Do include output parameters.
			false, // No data set so false.
			false, // No group - not needed here.
			false)); // No note - not needed here.
			this.__numparm = __parameters.length;
		this.__parametersOrig = this.__parameters;
	}
	
	// Save upper case versions of parameters to facilitate lookups.
	if ( this.__parameters != null ) {
		this.__parametersUpper = new String[this.__parameters.length];
		for ( int iParam = 0; iParam < this.__parameters.length; ++iParam ) {
			this.__parametersUpper[iParam] = this.__parameters[iParam].toUpperCase();
		}
	}
	if ( this.__parametersOrig != null ) {
		this.__parametersOrigUpper = new String[this.__parametersOrig.length];
		for ( int iParam = 0; iParam < this.__parameters.length; ++iParam ) {
			this.__parametersOrigUpper[iParam] = this.__parametersOrig[iParam].toUpperCase();
		}
	}

	// Parameter names.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Parameters for the file (size is " + __numparm + "):" );
		for ( int i = 0; i < this.__numparm; i++ ) {
			Message.printDebug ( dl, routine, "Parameter[" + i + "] = \"" + this.__parameters[i] + "\"" );
		}
	}

	if ( StateMod_Util.isVersionAtLeast(this.__version, StateMod_Util.VERSION_11_00) ) {
		offset2 = (header_rec + 4L + this.__numsta + this.__numdiv + this.__numifr +
			(this.__numres + resTotalAccountRec) + this.__numrun + this.__numdivw + this.__maxparm*3L)* this.__recordLength;
		this.__fp.seek ( offset2 );
		if ( this.__numparm > 0 ) {
			this.__unit = new String[this.__numparm];
		}
		for ( int i = 0; i < this.__numparm; i++ ) {
			this.__unit[i] = this.__fp.readLittleEndianString1(4).trim();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, 
				"For parameter \"" + this.__parameters[i] + "\" unit= \"" + this.__unit[i] + "\"" );
			}
		}
	}
}

/**
For an open binary file, determine the StateMod version from the first record.
If the record does not contain the version (e.g., old file format), then try
to determine the version from the current StateMod executable that is available.
@exception IOException if an error occurs reading the header (usually due to an empty file).
*/
private void readHeaderVersion()
throws IOException
{	String routine = "StateMod_BTS.readHeaderVersion";
	int dl = 1;
	if ( Message.isDebugOn) {
		Message.printDebug ( dl, routine, "The file version before reading is \"" + __version + "\"" );
	}
    boolean pre11Format = false; // Indicates old format files.
    boolean newestFormat = false; // Indicates new format files.
	// Files >= 12.29 use the following (from Ray Bennett 2008-10-27):
    // write(nf,rec=j1) CodeName, ver, vdate 
    // where codename is character*8, 
    //    ver is character *8, and 
    //    vdate is character *10
	// Therefore, check position [10] for a '.' which indicates the newer header format.
	__fp.seek(10);
	char test_char = __fp.readLittleEndianChar1();
	if ( test_char == '.') {
	    // Have the latest header format.
	    newestFormat = true;
	}
	else {
    	// Files before version 11 have year start and year end (2 integers) in the first record.
		// Therefore, check the characters used for the date and see if at least two are non-null.
		// If non-null, assume the new 11+ format.  If null, assume the old format.
    	__fp.seek ( 16 ); // First '/' in date if YYYY/MM/DD.
    	test_char = __fp.readLittleEndianChar1();
    	if ( test_char == '\0' ) {
    		pre11Format = true;
    		__fp.seek ( 19 ); // Second '/' in date if YYYY/MM/DD.
    		test_char = __fp.readLittleEndianChar1();
    		if ( test_char == '\0' ) {
    			pre11Format = true;
    		}
    	}	// Else both characters were non-null so pretty sure it is 11+
	}
    if ( newestFormat ) {
        Message.printStatus ( 2, routine, "The file version detected from header is >= 12.29." );
    }
    else if ( pre11Format ) {
		Message.printStatus ( 2, routine, "The file version detected from header is < 11.x." );
	}
	else {
	    Message.printStatus ( 2, routine, "The file version detected from header is >= 11.x. and < 12.29" );
	}
	if ( pre11Format ) {
		// No version can be determined from the file.
		__version = "";
	}
	else {
	    // Version 11+ format.
	    // Reposition and read the header.
		__fp.seek ( 0 );
		// 8 characters for the program name.
		__headerProgram = __fp.readLittleEndianString1(8).trim();
		// Program version.
		if ( newestFormat ) {
		    // NN.NN.NN but OK to have remainder.
		    __version = __fp.readLittleEndianString1(8).trim();
		}
		else {
		    // Version 11+ to < 12.29 used NN.NN format as float.
    		double versionDouble = (double)__fp.readLittleEndianFloat();
    		__version = "" + StringUtil.formatString(versionDouble,"%.2f");	// Format to avoid remainder.
		}
		// Date as 10 characters.
		__headerDate = __fp.readLittleEndianString1(10).trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Creator program=\"" +
			__headerProgram + "\" file version=" + __version + " software date=\"" + __headerDate + "\"" );
		}
	}

	// Set the version information so that it can be used elsewhere in the
	// class (for example to determine the parameters).

	if ( __version.equals("") ) {
		// If pre 11.0, then assume that it is the one before 11.0.
        __version = "9.69";
	    Message.printStatus( 2, routine, "Appears to be old file format - assuming that the StateMod file version is " + __version );
	}
	else {
	    Message.printStatus( 2, routine, "Read StateMod file version from header: " + __version );
	}
}

/**
Read a time series from a StateMod binary file.
The TSID string is specified in addition to the path to the file.
It is expected that a TSID in the file matches the TSID (and the path to the file,
if included in the TSID would not properly allow the TSID to be specified).
This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
When a pattern is supplied, the duplicate time series are ignored (only the
first occurrence is kept).  This most often filters out baseflow nodes at diversions, etc.
Returning one instance ensures that the time series will not
be double-counted in lists and subsequent analysis.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
@param tsident_string The full identifier for the time series to
read (where the scenario is NOT the file name).
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param units Units to convert to (currently not supported).
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries (	String tsident_string, String filename,
					DateTime date1, DateTime date2, String units, boolean read_data )
throws Exception
{	return readTimeSeries ( tsident_string, filename, date1, date2, units, read_data, (PropList)null );
}

/**
Read a time series from a StateMod binary file.  The TSID string is specified
in addition to the path to the file.  It is expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
properly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
When a pattern is supplied, the duplicate time series are ignored (only the
first occurrence is kept).  This most often filters out baseflow nodes at diversions, etc.
Returning one instance ensures that the time series will not
be double-counted in lists and subsequent analysis.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
@param tsident_string The full identifier for the time series to
read (where the scenario is NOT the file name).
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param units Units to convert to (currently not supported).
@param read_data Indicates whether data should be read (false=no, true=yes).
@param props A PropList containing information to control the read.  Recognized properties include:
<table width=100% cellpadding=10 cellspacing=0 border=2>

<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>
<tr><td>CloseWhenDone</td>
<td>Specifies whether to close the file once the time series has been read from it (False or True).</td>
<td>False.</td>
</tr>
</table>
*/
public static TS readTimeSeries ( String tsident_string, String filename,
					DateTime date1, DateTime date2, String units, boolean read_data, PropList props )
throws Exception
{	TS ts = null;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );

	boolean closeFile = false;
	
	if (props != null) {
		String closeWhenTrue = props.getValue("CloseWhenDone");
		if ( (closeWhenTrue != null) && closeWhenTrue.trim().equalsIgnoreCase("true")) {
			closeFile = true;
		}
	}
	
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2, "StateMod_BTS.readTimeSeries", "Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	StateMod_BTS in = null;
	try {
	    in = lookupStateModBTS ( full_fname );
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		    "StateMod_BTS.readTimeSeries(String,...)", "Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method.
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	List<TS> tslist = in.readTimeSeriesList ( tsident_string, date1, date2, units, read_data );

	if (closeFile) {
		in.close();
	}

	if ( (tslist == null) || (tslist.size() <= 0) ) {
		Message.printWarning( 2, "StateMod_BTS.readTimeSeries(String,...)",
		"Unable to read time series for \"" + tsident_string + "\"" );
		return ts;
	}
	return (TS)tslist.get(0);
}

/**
Read a list of time series from the binary file.  A list of new time series is returned.
@param tsident_pattern A regular expression for TSIdents to return.  For example
* or null returns all time series.  *.*.XXX.* returns only time series matching
data type XXX.  Currently only location and data type (output parameter) are checked and only a
* wildcard can be specified, if used.  This is useful for TSTool in order to
list all stations that have a data type.  For reservoirs, only the main location
can be matched, and the returned list of time series will include time series
for all accounts.  When matching a specific time series (no wildcards), the
main location part is first matched and the the reservoir account is checked if the main location is matched.
@param date1 First date/time to read, or null to read the full period.
@param date2 Last date/time to read, or null to read the full period.
@param req_units Requested units for the time series (currently not implemented).
@param read_data True if all data should be read or false to only read the headers.
@exception IOException if the interval for the time series does not match that
for the file or if a write error occurs.
*/
public List<TS> readTimeSeriesList ( String tsident_pattern, DateTime date1,
	DateTime date2, String req_units, boolean read_data ) throws Exception {
	String [] includeDataTypes = null;
	String [] excludeDataTypes = null;
	return readTimeSeriesList ( tsident_pattern, date1, date2,
		includeDataTypes, excludeDataTypes, req_units, read_data );
}

/**
Read a list of time series from the binary file.  A Vector of new time series is returned.
@param tsident_pattern A regular expression for TSIdents to return.  For example
* or null returns all time series.  *.*.XXX.* returns only time series matching
data type XXX.  Currently only location and data type (output parameter) are checked and only a
* wildcard can be specified, if used.  This is useful for TSTool in order to
list all stations that have a data type.  For reservoirs, only the main location
can be matched, and the returned list of time series will include time series
for all accounts.  When matching a specific time series (no wildcards), the
main location part is first matched and the the reservoir account is checked if the main location is matched.
@param date1 First date/time to read, or null to read the full period.
@param date2 Last date/time to read, or null to read the full period.
@param req_units Requested units for the time series (currently not implemented).
@param includeDataTypes an array of data types (matching StateMod parameters) to include or null to include all.
@param excludeDataTypes an array of data types (matching StateMod parameters) to exclude or null to exclude none.
The data types are excluded after 'includeDataTypes' is considered.
@param read_data True if all data should be read or false to only read the headers.
@exception IOException if the interval for the time series does not match that
for the file or if a write error occurs.
*/
public List<TS> readTimeSeriesList ( String tsident_pattern, DateTime date1, DateTime date2,
	String [] includeDataTypes, String [] excludeDataTypes, String req_units, boolean read_data )
throws Exception {
	return readTimeSeriesList ( tsident_pattern, date1, date2,
	includeDataTypes, excludeDataTypes, req_units, read_data, null );
}

/**
Read a list of time series from the binary file.  A Vector of new time series is returned.
@param tsident_pattern A regular expression for TSIdents to return.  For example
* or null returns all time series.  *.*.XXX.* returns only time series matching
data type XXX.  Currently only location and data type (output parameter) are checked and only a
* wildcard can be specified, if used.  This is useful for TSTool in order to
list all stations that have a data type.  For reservoirs, only the main location
can be matched, and the returned list of time series will include time series
for all accounts.  When matching a specific time series (no wildcards), the
main location part is first matched and the the reservoir account is checked if the main location is matched.
@param date1 First date/time to read, or null to read the full period.
@param date2 Last date/time to read, or null to read the full period.
@param req_units Requested units for the time series (currently not implemented).
@param includeDataTypes an array of data types (matching StateMod parameters) to include or null to include all.
@param excludeDataTypes an array of data types (matching StateMod parameters) to exclude or null to exclude none.
The data types are excluded after 'includeDataTypes' is considered.
@param read_data True if all data should be read or false to only read the headers.
@param outputVersion indicates if a transformation of file contents to a different
version should occur, can be "Original" (default) or
"Latest" (most recent know version, currently 14).
For example, older versions used "TO" for location for scenario total and new version uses "TOTAL".
@exception IOException if the interval for the time series does not match that
for the file or if a write error occurs.
*/
public List<TS> readTimeSeriesList ( String tsident_pattern, DateTime date1, DateTime date2,
	String [] includeDataTypes, String [] excludeDataTypes, String req_units, boolean read_data, String outputVersion )
throws Exception
{	String routine = "StateMod_BTS.readTimeSeriesList";
	if ( (outputVersion == null) || outputVersion.isEmpty() ) {
		outputVersion = "Original"; // Default.
	}
	
	// Using previously read information, loop through each time series
	// identifier and see if it matches what we are searching for.

	// TODO (JTS - 2004-08-04)
	// there is no non-static readTimeSeries() method.  One should be added for efficiency's sake.
	// SAM 2006-01-04 - non-static is used because there is a penalty reading the header.
	// This needs to be considered.

	int iparam = 0;
	List<TS> tslist = new ArrayList<>();
	boolean defaultPattern = false; // Whether a default pattern is used for TSID, used to speed up processing.
	if ( (tsident_pattern == null) || (tsident_pattern.length() == 0) ) {
		// Set a default pattern.
		tsident_pattern = "*.*.*.*.*";
		defaultPattern = true;
	}
	TSIdent tsident_regexp = new TSIdent ( tsident_pattern );
					// TSIdent containing the regular expression parts.
	// Make sure that parts have wildcards if not specified.
	if ( tsident_regexp.getLocation().length() == 0 ) {
		tsident_regexp.setLocation("*");
	}
	if ( tsident_regexp.getSource().length() == 0 ) {
		tsident_regexp.setSource("*");
	}
	if ( tsident_regexp.getType().length() == 0 ) {
		tsident_regexp.setType("*");
	}
	if ( tsident_regexp.getInterval().length() == 0 ) {
		tsident_regexp.setInterval("*");
	}
	if ( tsident_regexp.getScenario().length() == 0 ) {
		tsident_regexp.setScenario("*");
	}
	// These fields really have no bearing on the filter, but if not wildcarded, may cause a match to not be found.
	tsident_regexp.setSource ( "*" );
	tsident_regexp.setInterval ( "*" );
	tsident_regexp.setScenario ( "*" );
	String tsident_regexp_loc = tsident_regexp.getLocation();
	String tsident_regexp_source = tsident_regexp.getSource();
	String tsident_regexp_type = tsident_regexp.getType();
	String tsident_regexp_interval = tsident_regexp.getInterval();
	String tsident_regexp_scenario = tsident_regexp.getScenario();
	boolean station_has_wildcard = false; // Use to speed up loops.
	boolean datatype_has_wildcard = false;
	TS ts = null;
	float param;
	long filepos;
	DateTime date;
	int dl = 1;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading time series for \"" +
		tsident_pattern + "\" __numsta = " + __numsta );
	}
	
	// Get the parameter list:
	// - this is necessary because StateMod 15.00.02 and earlier has duplicate parameter names
	// - by default the original (actual) file contents are read
	// - if "Latest" is used, the original contents are converted to latest format.
	//   necessary to compare old and new output
	String [] parameters = null;
	String [] parametersUpper = null;
	if ( outputVersion.equalsIgnoreCase("Original") ) {
		parameters = this.__parametersOrig;
		parametersUpper = this.__parametersOrigUpper;
	}
	else {
		parameters = this.__parameters;
		parametersUpper = this.__parametersUpper;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug(dl, routine, "Parameters after accounting for OutputVersion=" + outputVersion );
		for ( int iParam = 0; iParam < parameters.length; ++iParam) {
			Message.printDebug(dl, routine, "Parameter[" + iParam + "] = \"" + parameters[iParam] + "\"");
		}
	}
	
	// If include/exclude lists are provided, convert to upper case to speed comparisons.
	boolean [] includeParameters = null;
	if ( (includeDataTypes != null) || (excludeDataTypes != null) ) {
		// Have one or both include/exclude lists:
		// - initialize to false and then set to true below if including
		includeParameters = new boolean[parameters.length];
		for ( int iParam = 0; iParam < parameters.length; iParam++ ) {
			includeParameters[iParam] = false;
		}
	}
	if ( includeDataTypes != null ) {
		for ( int iInclude = 0; iInclude < includeDataTypes.length; ++iInclude ) {
			includeDataTypes[iInclude] = includeDataTypes[iInclude].toUpperCase();
		}
		// See if the parameter matches the requested data type:
		// - currently it must be an exact match with no wildcard matching, also case is ignored
		int iInclude, iParam;
		for ( iParam = 0; iParam < parameters.length; iParam++ ) {
			includeParameters[iParam] = false;
			if ( parametersUpper[iParam].equals("NA") ) {
				// Never include parameter "NA" since a place-holder.
				continue;
			}
			for ( iInclude = 0; iInclude < includeDataTypes.length; ++iInclude ) {
				if ( includeDataTypes[iInclude].equals(parametersUpper[iParam]) ) {
					// Turn on the parameter.
					includeParameters[iParam] = true;
					break;
				}
			}
		}
	}
	if ( excludeDataTypes != null ) {
		for ( int iExclude = 0; iExclude < excludeDataTypes.length; ++iExclude ) {
			excludeDataTypes[iExclude] = excludeDataTypes[iExclude].toUpperCase();
		}
		// See if the parameter matches the requested data type:
		// - currently it must be an exact match with no wildcard matching, also case is ignored
		int iExclude, iParam;
		for ( iParam = 0; iParam < parameters.length; iParam++ ) {
			for ( iExclude = 0; iExclude < excludeDataTypes.length; ++iExclude ) {
				if ( excludeDataTypes[iExclude].equals(parametersUpper[iParam])) {
					// Turn off the parameter.
					includeParameters[iParam] = false;
					break;
				}
			}
		}
	}
	if ( includeParameters != null ) {
		for ( int iParam = 0; iParam < parameters.length; iParam++ ) {
			Message.printStatus(2, routine, "includeParameters[" + iParam + "] = " + includeParameters[iParam] + " for " + parameters[iParam]);
		}
	}

	// This is used to track matches to ensure that the same
	// station/datatype combination is only included once.
	// It is possible that baseflow stations are listed in more than one list of stations.
	// Even if there are 1000 nodes and 30 data types, this will only take
	// 30K of memory, which is relatively small.
	boolean [][] sta_matched = new boolean[__numsta][__numparm];
	int j;
	for ( int i = 0; i < __numsta; i++ ) {
		for ( j = 0; j < __numparm; j++ ) {
			sta_matched[i][j] = false;
		}
	}
	try {
	if ( tsident_regexp_loc.indexOf("*") >= 0 ) {
		station_has_wildcard = true;
	}
	if ( tsident_regexp_type.indexOf("*") >= 0 ) {
		datatype_has_wildcard = true;
	}
	// If the location contains a dash ("-"), it indicates a reservoir and account.
	// Since the StateMod binary file does not merge these fields,
	// replace the identifier with only the main identifier...
	String req_account = null;
	if ( (tsident_regexp_loc.indexOf("-") >= 0) && (__comp_type==StateMod_DataSet.COMP_RESERVOIR_STATIONS) ) {
		req_account=StringUtil.getToken(tsident_regexp_loc,"-",0,1);
		tsident_regexp.setLocation(	StringUtil.getToken(tsident_regexp_loc,"-",0,0) );
		tsident_regexp_loc = tsident_regexp.getLocation();
	}
	boolean match_found = false;
				// Indicates if a match for the specific station is made.
				// TODO SAM 2006-01-04.  This seems to be a remnant of previous code.
				// It is used but many checks result in "continue" or "break" out of the loops.
	String [] ids = null; // Used to point to each list of station IDs.
	String [] names = null; // Used to point to each list of names.
	int numids = 0; // Used to point to size of each list.
	int ista = 0; // River node position matching the ID.
	int ista2 = 0; // Station in data record portion of file to locate.
				// For diversion/instream/stream it is ista.
				// For reservoirs and wells it is the position of the reservoir or well in the file.
	int nts = 0; // Number of time series per location/parameter combination (used with reservoir accounts).
	int its = 0; // Loop counter for time series.
	String owner = ""; // Used to append a reservoir owner/account to the location, blank normally.
	boolean convert_cfs_to_acft = true;
				// Indicates whether a parameter's data should be converted from CFS to ACFT.
	// Loop through the lists of stations:
	//
	int DIV = 0; // Diversion stations.
	int ISF = 1; // Instream flow stations.
	int RES = 2; // Reservoir stations.
	int BF = 3; // Baseflow stations.
	int WEL = 4; // Wells.
	int RIV = 5; // River nodes (to find nodes only in RIN file).
	//
	// The original file that was opened indicated what
	// type of data the file contains and inappropriate types are skipped below.
	// Diversions, instream flow, and stream stations are stored
	// in the same binary file so multiple lists are checked for that file.
	TSIdent current_tsident = new TSIdent ();
				// Used for the time series below, to compare to the pattern.
	TSIdent tsident = null;	// Used when creating new time series.
					
	for ( int istatype = 0; istatype < 6; istatype++ ) {
		// First check to see if we even need to search the station list for this loop index.
		// Diversion stations file has div, isf, baseflow, other (river
		// nodes that are not another station type).
		if ( (__comp_type==StateMod_DataSet.COMP_DIVERSION_STATIONS)
			&& (istatype != DIV) && (istatype != ISF) && (istatype != BF) && (istatype != RIV)) {
			continue;
		}
		else if ((__comp_type==StateMod_DataSet.COMP_RESERVOIR_STATIONS) && (istatype != RES) ) {
			continue;
		}
		else if ((__comp_type==StateMod_DataSet.COMP_WELL_STATIONS) && (istatype != WEL) ) {
			continue;
		}
		// If here then the correct station list type has been determined.
		// Assign the references to the arrays to search.
		if ( istatype == DIV ) {
			// Search diversions.
			ids = __cdivid;
			names = __divnam;
			numids = __numdiv;
		}
		else if ( istatype == ISF ) {
			// Search instream flows.
			ids = __cifrid;
			names = __xfrnam;
			numids = __numifr;
		}
		else if ( istatype == RES ) {
			// Search reservoirs.
			ids = __cresid;
			names = __resnam;
			numids = __numres;
		}
		else if ( istatype == BF ) {
			// Baseflows (stream gage + stream estimate).
			ids = __crunid;
			names = __runnam;
			numids = __numrun;
		}
		else if ( istatype == WEL ) {
			// Wells.
			ids = __cdividw;
			names = __divnamw;
			numids = __numdivw;
		}
		else if ( istatype == RIV ) {
			// River nodes (nodes not other station will be found).
			ids = __cstaid;
			names = __stanam;
			numids = __numsta;
		}
		// Loop through the ids in the list.
		for ( int iid = 0; iid < numids; iid++ ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Station[" + iid + "] = " + ids[iid] );
			}
			// Loop through the parameters.
			for ( iparam = 0; iparam < __numparm; iparam++ ) {
				// Check the station and parameter to see if they match - all other fields are allowed to be wildcarded.
				if ( Message.isDebugOn ) {
					Message.printDebug ( 2, routine, "Parameter = " + parameters[iparam] );
				}
				// Need to match against each station ID list.
				// Set the information from the file into the working "tsident" to compare.
				current_tsident.setLocation ( ids[iid] );
				current_tsident.setType ( parameters[iparam] );
				// Other TSID fields are left blank to match all.
				if ( !defaultPattern && !current_tsident.matches(
					tsident_regexp_loc,
					tsident_regexp_source,
					tsident_regexp_type,
					tsident_regexp_interval,
					tsident_regexp_scenario,
					null,
					null, false) ) {
					// This time series does not match one that is requested.
					// Just need to match the location and parameter since that is all that is in the file.
					//Message.printStatus ( 1, routine,"Requested \"" + tsident_pattern +
					//"\" does not match \"" +ids[iid] + "\" \""+__parameter[iparam]+ "\"" );
					continue;
				}
				// Additional data type checks to limit output.
				if ( parameters[iparam].equals("NA") ) {
					// Never include parameter "NA".
					continue;
				}
				else if ( (includeParameters != null) && !includeParameters[iparam] ) {
					// Was not found in the include list so ignore.
					continue;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( 2, routine, "Requested \"" + tsident_pattern +
					"\" does match \"" + ids[iid] + "\" \"" + parameters[iparam]+ "\"" );
				}
				// Figure out the river station from the match.
				// The original river node positions start at 1 so subtract 1 to get the in-memory positions.
				// The river node id for stations is only available in StateMod 10.34 or later.
				// If the following results in a value of ista <= 0,
				// then try to match the river node ID directly - this will work with data sets
				// where the station and river node IDs are the same.
				ista = -1; // To allow check below.
				if ( istatype == DIV ) {
					// Diversions.
					ista = __idvsta[iid] - 1;
					ista2 = ista;
					match_found = true;
				}
				else if ( istatype == ISF ) {
					// Instream flow.
					ista = __ifrsta[iid] - 1;
					ista2 = ista;
					match_found = true;
				}
				else if ( istatype == RES ) {
					// Reservoirs.
					ista = __irssta[iid] - 1;
					ista2 = iid;
					match_found = true;
					// Ignore inactive reservoirs because data will be junk.
					if ( __iressw[iid] == 0 ) {
						// Reservoir is not active so do not include the time series.
						// The values will be meaningless (zero) and inactive reservoirs do not have a total,
						// which would require special handling below.
						match_found = false;
					}
					else {
    					// Make sure that the matched reservoir has the requested account.
    					if ( req_account != null ) {
    						// Have an account (not just the total).
    						int naccounts =	__nowner2[iid] - 1; // Ignore total.
    						int ireq_account = StringUtil.atoi(req_account);
    						if ( (ireq_account < 1) || (ireq_account > naccounts) ) {
    							match_found = false;
    						}
    					}
					}
				}
				else if ( istatype == BF ) {
					// Baseflows (stream gage and estimate).
					ista = __irusta[iid] - 1;
					ista2 = ista;
					match_found = true;
				}
				else if ( istatype == WEL ) {
					// Wells.
					ista = __idvstw[iid] - 1;
					ista2 = iid;
					match_found = true;
				}
				else if ( istatype == RIV ) {
					// Already a river node.
					ista = iid;
					ista2 = iid;
					match_found = true;
				}
				if ( match_found && sta_matched[ista][iparam] ){
					// Already matched this station for the current parameter so ignore.
					// It is possible that a baseflow node is also another node type but we only want
					// one instance of the time series.
					// This will also ensure that river nodes are not counted twice.
					match_found = false;
				}
				if ( match_found ) { // Don't just continue because a check to break occurs below.
				sta_matched[ista][iparam] = true;
				convert_cfs_to_acft = true;
				if ( __intervalBase == TimeInterval.DAY ) {
					convert_cfs_to_acft = false;
				}
				if ( istatype == RES ) {
					// For each reservoir, have a total and a time series for each account.
					if ( station_has_wildcard  ) {
						// Requesting all available time series.
						nts = __nowner2[iid];
					}
					else {
					    // Requesting a single total or account.
						nts = 1;
					}
				}
				else {
				    // Other than reservoirs, only one time series per location/parameter.
					nts = 1;
				}
				for ( its = 0; its < nts; its++ ) {
					if ( (istatype == RES) && (its > 0) ) {	
						// Getting all reservoir time series for the accounts:
						// - an owner account is added as a sublocation (1, 2, etc.).
						owner = "-" + its;
					}
					else if ( (istatype == RES) && (req_account != null) ) {
						// A requested reservoir account.
						owner = "-" + req_account;
						// Set "its" to the specific account.
						// This will be OK because it will cause the loop to exit when done with the single time series.
						// Total is 0, so no need to offset.
						its = StringUtil.atoi( req_account);
					}
					else {
					    // A reservoir total (no sub-location) or other station time series.
						owner = "";
					}
					if ( __intervalBase == TimeInterval.MONTH ) {
						ts = new MonthTS();
						tsident = new TSIdent (
							ids[iid] + owner,
							"StateMod",
							parameters[iparam],
							"Month","",
							"StateModB", __tsfile );
					}
					else if( __intervalBase == TimeInterval.DAY ){
						ts = new DayTS();
						tsident = new TSIdent (
							ids[iid] + owner,
							"StateMod",
							parameters[iparam],
							"Day","",
							"StateModB", __tsfile );
					}
					// Set time series header information.
					ts.setIdentifier ( tsident );
					ts.setInputName ( __tsfile );
					if ( (istatype == RES) && owner.length() > 0 ) {
						// Put the owner in the description.
						ts.setDescription ( names[iid] + " - Account " + owner.substring(1) );
					}
					else {
					    ts.setDescription ( names[iid]);
					}
					ts.setDataType ( parameters[iparam] );
					// Data in file are CFS but we convert to ACFT.
					if ( convert_cfs_to_acft ) {
						ts.setDataUnits ( "ACFT" );
					}
					else {
					    ts.setDataUnits ( "CFS" );
					}
					// Original dates from file header.
					ts.setDate1Original ( new DateTime(__date1) );
					ts.setDate2Original ( new DateTime(__date1) );
					// Time series dates from requested parameters or file.
					if ( date1 == null ) {
						date1 = new DateTime(__date1);
						ts.setDate1 ( date1 );
					}
					else {
					    ts.setDate1 ( new DateTime( date1) );
					}
					if ( date2 == null ) {
						date2 = new DateTime(__date2);
						ts.setDate2 ( date2 );
					}
					else {
					    ts.setDate2 ( new DateTime( date2) );
					}
					ts.addToGenesis ( "Read from \"" + __tsfile + " for " + date1 +	" to " + date2 );
					// Set properties.
					setTimeSeriesProperties(ts, istatype);
					tslist.add ( ts );
					if ( read_data ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( 2, routine, "Reading " + date1 + " to " + date2 );
						}
						// Allocate the data space.
						if ( ts.allocateDataSpace () != 0 ) {
							throw new Exception ( "Unable to allocate data space." );
						}
						// Read the data for the time series.
						for ( date = new DateTime(date1); date.lessThanOrEqualTo(date2);
							date.addInterval(__intervalBase, 1) ){
							if ( (date.getMonth()==2)	&& (date.getDay()==29) ){
								// StateMod does not handle.
								continue;
							}
							filepos = calculateFilePosition( date, ista2, its, iparam);
							if ( Message.isDebugOn){
								Message.printDebug ( 2, routine, "Reading for "+ date +
								" ista2="+ista2+ " iparam="+ iparam + " its=" + its +
								" filepos=" + filepos );
							}
							if ( filepos < 0 ) {
								continue;
							}
							__fp.seek ( filepos );
							// Convert CFS to ACFT so output is monthly volume.
							try {
							    param = __fp.readLittleEndianFloat();
							}
							catch ( Exception e ) {
								// Assume end of file so break out of read.
								Message.printWarning ( 3, routine,
								"Unexpected error reading byte " + filepos + " - stop reading data.  Expected file size ="+
								__estimatedFileLengthBytes );
								if ( Message.isDebugOn ) {
								    Message.printWarning(3, routine, e);
								}
								break;
							}
							// Convert to ACFT if necessary.
							if ( convert_cfs_to_acft){
								param = param*CFS_TO_ACFT*(float)__mthdayCalendar[date.getMonth() - 1];
							}
							if ( Message.isDebugOn){
								Message.printDebug ( 2, routine, "Parameter value (AF) is " + param );
							}
							ts.setDataValue( date,param);
						}
					}
				}
				} // End match_found.
				if ( !datatype_has_wildcard ) {
					// No need to keep searching.
					break;
				}
			}
			if ( !datatype_has_wildcard && !station_has_wildcard && match_found ) {
				// No need to keep searching.
				break;
			}
		}
	}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "", e );
	}
	// TODO 2007-01-18 old comment - Might return null if match_found == false????
	return tslist;
}

/**
 * Set time series properties.
 * @param ts time series.
 * @param istatype StateMod station type.
 */
private void setTimeSeriesProperties ( TS ts, int istatype ) {
	ts.setProperty("NodeType", this.nodeTypes[istatype] );
}

/**
Return the number of time series in the file.
@return the number of time series in the file.
*/
public int size ()
{	return __numsta*__numparm;
}

}