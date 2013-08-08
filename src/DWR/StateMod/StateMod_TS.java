// ----------------------------------------------------------------------------
// StateMod_TS - class to read/write StateMod format time series
// ----------------------------------------------------------------------------
// History:
//
// 28 Nov 2000	Steven A. Malers, RTi	Copy and modify DateValueTS to include
//					getSample().  This class will eventually
//					combine the StateModMonthTS and
//					StateModDayTS code, as time allows.
// 28 Feb 2001	SAM, RTi		Add getFileInterval() to help
//					automatically determine whether data
//					are monthly or daily format.
// 2003-06-19	SAM, RTi		* Rename class from StateModTS to
//					  StateMod_TS and start to include code
//					  from legacy StateModMonthTS and
//					  StateModDayTS classes.
//					* Update to use new TS package (e.g.,
//					  use DateTime instead of TSDate and
//					  TimeInterval instead of TSInterval).
// 2003-07-08	SAM, RTI		Rename write methods from
//					writePersistent*() to
//					writeTimeSeries*().
// 2003-08-22	SAM, RTI		Enable daily time series read in
//					readTimeSeriesList().
// 2003-09-03	SAM, RTI		Fix a bug reading daily data.
// 2003-10-09	SAM, RTI		Fix a bug reading data other than
//					calendar year - the year was not
//					getting set correctly in the read.
// 2003-11-04	SAM, RTi		Add readTimeSeries() to take a TSID
//					and file name, to read a requested
//					time series.
// 2003-12-11	SAM, RTi		Add readPatternTimeSeriesList() -
//					from the old StateModMonthTS.
//					readPatternFile().
// 2004-01-15	SAM, RTi		* Remove revisits that were limiting
//					  previous functionality (precision
//					  formatting, calendar type).
//					* Change writeTimeSeriesList(,PropList)
//					  to return void.
// 2004-01-31	SAM, RTi		* Enable writing of daily files.
//					  Similar to readTimeSeries() both
//					  daily and monthly format is supported
//					  in writeTimeSeries().
//					* Optimize the writeTimeSeries() code
//					  some - remove extra loops and checks,
//					  and add a boolean array to help avoid
//					  repeated checks for null or bad time
//					  series.
// 2005-05-06	SAM, RTi		* Add writePatternTimeSeriesList().
//					  Copy and modify writeTimeSeriesList to
//					  implement.  Some additional features
//					  may be enabled later.
//					* Clean up some of the old messages that
//					  were still using "writePersistent".
// 2005-09-09	SAM, RTi		* Allow comments in the data part of
//					  monthly and daily files.
// 2006-01-23	SAM, RTi		* Fix bug where monthly average time
//					  series were not being read in properly
//					  for water year.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
// 2007-04-10	SAM, RTi		Change default prevision from 2 to -2 as per
//						writeStateMod() command docs - this is a better default.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;

import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.StringMonthTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
import RTi.Util.IO.DataFormat;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

// TODO SAM 2009-06-02 Evaluate renaming StateMod_TSUtil to be consistent with other static utility classes
// and avoid confusion with the StateMod_TimeSeries class, which may be implemented to wrap normal time series.
/**
The StateMod_TS class provides static input/output methods and general utilities
for StateMod time series.  Methods are provided to read/write from daily and
monthly StateMod time series.  This class replaces the legacy StateModMonthTS
and StateModDayTS classes.  This class will ideally contain all StateMod time
series related code.  However, this may not be possible - design decisions will
be made as code is incorporated into this class.
*/
public class StateMod_TS
{

/**
Use the following to specify that precision of output should be determined from the units file.
*/
public static final int PRECISION_USE_UNITS = 1000000;

/**
If the precision is divided by 1000, use the following to indicate from the
remainder special actions that should be taken for the precision.  For example,
specifying a -2001 precision is equivalent to -2 with NO decimal point for
large numbers.  THESE ARE BIT MASK VALUES!!!
*/
public static final int PRECISION_NO_DECIMAL_FOR_LARGE = 1;

/**
Offset used to shift requested precisions for special requests...
*/
public static final int PRECISION_SPECIAL_OFFSET = 1000;

/**
Default precision for output.
*/
public static final int PRECISION_DEFAULT = -2;

/**
Comment character for permanent comments.
*/
public static String PERMANENT_COMMENT = "#";

/**
Comment character for non-permanent comments.
*/
public static String NONPERMANENT_COMMENT = "#>";

/**
Determine whether a StateMod file is daily or monthly format.  This is done
by reading through the file until the first line of data.  If that line has
more than 150 characters, it is assumed to be a daily file; otherwise it is assumed 
to be a monthly file.  Currently, this method does NOT verify that the file
contents are actually for StateMod.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param filename Name of file to examine.
@return TimeInterval.DAY, TimeInterval.MONTH or -999 if unknown.
*/
public static int getFileDataInterval ( String filename )
{	String message = null, routine = "StateMod_TS.getFileDataInterval";
	BufferedReader ifp = null;
	String iline = null;
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	try {
		ifp = new BufferedReader ( new FileReader ( full_filename));
	}
	catch ( Exception e ) {
		message = "Unable to open file \"" + full_filename + "\" to determine data interval.";
		Message.printWarning ( 2, routine, message );
		return -999;
	}
	try {
		// Read while a comment or blank line...
		while ( true ) {
			iline = ifp.readLine();
			if ( iline == null ) {
				throw new Exception ( "end of file" );
			}
			iline = iline.trim();
			if ( (iline.length() != 0) && (iline.charAt(0) != '#')){
				break; // iline should be the header line
			}
		}
		// Now should have the header.  Read one more to get to a data line...
		iline = ifp.readLine();
		if ( iline == null ) {
			throw new Exception ( "end of file" );
		}
		// Should be first data line.  If longer than the threshold, assume daily.
		if ( iline.length() > 150 ) {
			ifp.close();
			return TimeInterval.DAY;
		}
		else {
			ifp.close();
			return TimeInterval.MONTH;
		}
	}
	catch ( Exception e ) {
		// Could not determine file interval
		return -999;
	}
	finally {
		if ( ifp != null ) {
			try {
				ifp.close();
			}
			catch ( IOException e ) {
				// Ignore - should not happen
			}
		}
	}
}

/**
Get the total for a line as a Double.  This computes the total based on what will be printed, not what
is in memory.  In this way the printed total will agree with the printed monthly or daily values.
@param ts time series being written
@param standardTS if false, it is an annual average so no year
@param nvals number of values possible in the line of data (12 for monthly or the number of days in the month
for daily)
@param lineObjects data values to print as objects of the appropriate type
@param formatObjects formats that are used to print each value
@param reqIntervalBase indicates whether monthly or daily time series are being printed
@param do_total if true then the total is calculated for the end of the line, if false the average is calculated
@param sum the sum of the values to be considered for the total
@param count the count of the values to be considered for the average (includes only non-missing values)
*/
private static Double getLineTotal ( TS ts, boolean standardTS, int nvals, List lineObjects,
	List<String> formatObjects,	int reqIntervalBase, boolean do_total, double sum, int count,
	boolean doSumToPrinted )
{
	if ( doSumToPrinted ) {
		// The total needs to sum to the printed values on the line
		// Loop through the original values and format each.  Then add them and format the total
		int i1 = 2; // Start and end indices for looping through values, year, ID, first value
		if ( !standardTS ) {
			--i1; // Average annual so no year
		}
		int i2 = i1 + nvals - 1; // always 12 values
		if ( reqIntervalBase == TimeInterval.DAY ) {
			i1 = 3; // Year, month, ID, first value
			i2 = i1 + nvals - 1; // actual number of values (may be less than 31 for daily)
		}
		String formattedString;
		sum = 0.0;
		count = 0;
		double val;
		for ( int i = i1; i <= i2; i++ ) {
			formattedString = StringUtil.formatString ( lineObjects.get(i), formatObjects.get(i) );
			val = Double.parseDouble(formattedString);
			if ( !ts.isDataMissing(val) ) {
				sum += val;
				++count;
			}
		}
	}

	// Legacy code where the total sums to the in-memory values
	if ( count == 0 ) {
		// Missing
		return new Double(ts.getMissing());
	}
	else if ( do_total ) {
		// Sum of whatever is available
		return new Double(sum);
	}
	else {
		// Mean of whatever is available
		return new Double(sum/count);
	}
}

/**
Static data for the following routine to improve performance.
*/
private static int _exp_saved = -999;
private static double _largest_number_saved = 0.0;
/**
@return The appropriate precision to output a value.
@param req_precision The requested precision.  If 0 or positive, then use the
requested precision.  If negative, use the requested precision (positive value)
if the resulting number fits into the width of the column.  Otherwise adjust the precision to fit.
*/
public static int getPrecision ( int req_precision, int width, double value )
{	// If req_precision is > PRECISION_SPECIAL_OFFSET, divide to get the
	// numeric requested precision...

	if ( (req_precision > PRECISION_SPECIAL_OFFSET) || (req_precision*-1 > PRECISION_SPECIAL_OFFSET) ) {
		req_precision = req_precision/PRECISION_SPECIAL_OFFSET;
	}

	// If req_precision is a positive value, return req_precision...
	if ( req_precision >= 0 ) {
		return req_precision;
	}
	// Else if value is within the width of column, return req_precision...
	// The 2 allows for the decimal and a sign.
	// For example, for a width of 8, and a requested precision of -2:
	//
	// -1234.12
	//
	// Would be the output format.  Because width is negative if we get to
	// here, add to the width instead of subtracting...
	int	exp;
	if ( value < 0.0 ) {
		// Decimal point and sign...
		exp = width + req_precision - 2; 
	}
	else {
		// Decimal only...
		exp = width + req_precision - 1; 
	}
	double	largest_number = 0.0;;
	if ( (exp == _exp_saved) && (exp != -999) ) {
		// We have called this code with the same information before
		// so don't regenerate the largest_number...
		largest_number = _largest_number_saved;
	}
	else {
		// Need to compute the largest number.  Using the example above,
		// we would get 10^4 = 10000 - 1.0 = 9999.99
		largest_number = Math.pow ( 10, (double)exp ) - 1.0;
		_largest_number_saved = largest_number;
		_exp_saved = exp;
	}
	// Handle negative and positive values...
	double plus_value;
	if ( value > 0.0 ) {
		plus_value = value;
	}
	else {
		plus_value = value*-1.0;
	}
	if ( plus_value <= largest_number ) {
		// It fits within the precision.  Return the positive value of the precision...
		return req_precision*-1;
	}
	// Else, the value is larger than the allowable width and req_precision
	// indicates that some changes are allowed - return 0
	// Note that this handles big numbers well but may not handle the case
	// of small numbers.  That may be an enhancement for later.
	return 0;
}

private static String __last_units = "";
private static int __last_units_precision = PRECISION_DEFAULT;
/**
@return The appropriate precision to output a value.
@param req_precision The requested precision.  If 0 or positive, then use the
requested precision.  If negative, use the requested precision (positive value)
if the resulting number fits into the width of the column.  Otherwise adjust
the precision to fit.  If PRECISION_USE_UNITS, use the units to determine the precision.
*/
public static int getPrecision ( int req_precision, int width, double value, String units )
{	// if no units data, call the simple version...

	if ( units == null ) {
		return getPrecision ( req_precision, width, value );
	}
	if ( units.equals("") ) {
		return getPrecision ( req_precision, width, value );
	}

	// Else we need to check the units and get the precision.  In general
	// all the units in the file will be the same so we just save the
	// last lookup and return that if the units match...

	if ( units.equalsIgnoreCase(__last_units) ) {
		return __last_units_precision;
	}

	int units_precision = PRECISION_DEFAULT;

	try {
		DataFormat units_format = DataUnits.getOutputFormat( units, width);
		__last_units = units;
		__last_units_precision = units_format.getPrecision();
	}
	catch ( Exception e ) {
		// Could not get units so return the requested precision without checking units...
		return getPrecision ( req_precision, width, value );
	}

	// Now have the units precision so call the general routine with a negative value...

	if ( units_precision < 0 ) {
		return getPrecision ( units_precision, width, value );
	}
	else {
		return getPrecision ( -1*units_precision, width, value );
	}
}

/**
Read a monthly pattern file to be used with time series filling.  The format of
the file is the same as a StateMod time series file except that instead of
stations a pattern name is used, and instead of monthly data values, string
values are used (e.g., "WET", "DRY", "AVE").  This data can then be used
to fill missing data using TSUtil.fillUsingPattern().
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return A Vector of StringMonthTS containing pattern data.
@param filename Name of pattern file to read.
@param read_data true if all the data should be read, false if only the header should be read.
*/
public static List readPatternTimeSeriesList ( String filename, boolean read_data )
{	int	dl = 1, i, m1, m2, y1, y2, num_years, year = 0, len,
		currentTSindex, current_year=0, init_year, numts = 0;
	String	chval, iline, message, rtn="StateMod_TS.readPatternTimeSeriesList", value;
	DateTime date = new DateTime (DateTime.PRECISION_MONTH);
	DateTime date1 = new DateTime (DateTime.PRECISION_MONTH);
	DateTime date2 = new DateTime (DateTime.PRECISION_MONTH);

	List v;
	List tslist = new Vector ( 10, 5 );

	String full_filename = IOUtil.getPathUsingWorkingDir(filename);
	BufferedReader ifp = null;
	try {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "Reading StateMod format pattern file: \"" + full_filename + "\"");
		}
		try {
		    ifp = new BufferedReader ( new FileReader ( full_filename));
		}
		catch ( Exception e ) {
			message = "Unable to open file \"" + full_filename + "\"";
			Message.printWarning ( 2, rtn, message );
			throw new Exception ( message );
		}
		iline = ifp.readLine();
		if ( iline == null ) {
			ifp.close();
			message = "Incomplete file \"" + full_filename + "\"";
			Message.printWarning ( 3, rtn, message );
			throw new Exception ( message );
		}
		len = iline.trim().length();
		if ( len < 1 ) {
			ifp.close();
			message = "Incomplete file \"" + full_filename + "\"";
			Message.printWarning ( 3, rtn, message );
			throw new Exception ( message );
		}
	
		// Read lines until no more comments are found.  The last line read will
		// need to be processed as the main header line...
	
		while ( iline.startsWith("#") ) {
			iline = ifp.readLine();
		}
	
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "Done with comments" );	
		}
	
		// Process the main header line...
	
		// CEN:  don't like this being formatted - free format?
		// 
		// SAM:  It looks like some of the replace() files for demandts have the
		// header line malformatted.  Rather than change all the files, check
		// for a '/' in the [3] position and adjust the format.  Print a warning
		// at level 1.
		String format_0 = null;
		if ( iline.charAt(3) == '/' ) {
			Message.printWarning ( 1, rtn,
			"Non-standard header for file \"" + full_filename +	"\" allowing with work-around." );
			format_0 = "i3x1i4x3i5x1i4s5s5";
		}
		else {
		    // Probably formatted correctly...
			format_0 = "i5x1i4x5i5x1i4s5s5";
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "Parsing line for calperiod: \"" + iline + "\"" );
		}
		v = StringUtil.fixedRead ( iline, format_0 );
		m1 = ((Integer)v.get(0)).intValue();
		y1 = ((Integer)v.get(1)).intValue();
		m2 = ((Integer)v.get(2)).intValue();
		y2 = ((Integer)v.get(3)).intValue();
		String units = ((String)v.get(4)).trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "Retrieved: \"" + 
			m1 + " " + y1 + " " + m2 + " " + y2 + " " + units + "\"");	
		}
	
		if (y1==0) { // annual time series 
			if (m2 < m1) {
				y2 = 1;	// Treat as calendar year 0...1
			}
			else {
			    y2 = 0;	// Treat as calendar year 0
			}
		}
	
		if ( m2<m1 ) {
			num_years = y2 - y1;
		}
		else {
		    num_years = y2 - y1 + 1;
		}
	
		int [] format_month = {
		    StringUtil.TYPE_INTEGER,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING };
		int [] format_month_w = {
		    5,
			12,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8 };
		int [] format_annual = {
		    StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING };
		int [] format_annual_w = {
		    5,
			12,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8,
			8 };
		iline = ifp.readLine();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "Parsing line: \"" + iline +"\"");
		}
		if (y1 != 0) { // this is monthly and includes year
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, rtn, "Found monthly time series" );	
			}
			StringUtil.fixedRead ( iline, format_month, format_month_w, v );
			current_year = ((Integer)v.get(0)).intValue();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, rtn, "current year set to: " +	current_year );
			}
			init_year = current_year;
		}
		else {
		    // this is annual and will not include year
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, rtn, "Found annual time series" );	
			}
			StringUtil.fixedRead ( iline, format_annual, format_annual_w,v);	
			init_year = 0;
		}
		currentTSindex = 0;
	
		// Read remaining data lines.  If in the first year, allocate memory
		// for each time series as a new station is encountered...
	
		//while ( iline.length() > 0 )
		StringMonthTS currentTS = null, month_ts = null; // Used to fill data.
		while ( iline != null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, rtn, "Parsing line: \"" + iline + "\"" );
			}
			if ( iline.length() == 0 ) {
				// Blank line - probably at the bottom but handle
				// anywhere by skipping (copy this code from below)...
				iline = ifp.readLine();
	
				if ( (iline != null) && (iline.length() > 0) ) {
					if (y1 != 0) {
						// this is monthly and includes year
						StringUtil.fixedRead ( iline, format_month, format_month_w, v);
						current_year = ( (Integer)v.get(0)).intValue();
					}
					else {
					    StringUtil.fixedRead ( iline,format_annual,format_annual_w,v);	
					}
				}
				continue;
			}
	
			// The first thing that we do is get the time series
			// identifier so we can check against a requested identifier...
	
			chval = (String)v.get(1);
			String id = chval.trim();
	
			// We are still establishing the list of stations in file
			if (current_year == init_year) {
				date1.setMonth ( m1 );
				date1.setYear ( y1 );
	
				date2.setMonth ( m2 );
				date2.setYear ( y2 );
	
				TSIdent ident = new TSIdent ();
				ident.setLocation ( id );
				ident.setSource ( "StateMod_Pattern" );
	
				// Create a new time series...
	
				if ( read_data ) {
					month_ts = new StringMonthTS( ident.getIdentifier(), date1, date2);
				}
				else {
				    month_ts = new StringMonthTS( ident.getIdentifier(), null, null);
				}
	
				month_ts.setIdentifier ( ident );
				month_ts.setDescription ( "Fill pattern" );
				month_ts.setDataUnits ( "" );
				month_ts.setDataUnitsOriginal ( "" );
	
				// Genesis information...
	
				month_ts.addToGenesis ( "Read StateMod TS for " + date1.toString() + " to " +
				        date2.toString() + " from \"" + full_filename + "\"" );
	
				// Attach new time series to list.
				tslist.add ( month_ts );
				numts++;
			}
			else {
			    if ( !read_data ) {
					// Done reading the data.
					break;
				}
			}
	
			// If we are working through the first year, currentTS will be = TStail.
			// On the other hand, if we have already established the list and are filling the rest of the
			// rows, currentTS should be reset to the head of the list and the year should be increased 
	
			if (currentTSindex >= numts ) {
				currentTSindex = 0;
				year++;
				num_years--;
			}
	
			// Filling a vector of TS...
			currentTS = (StringMonthTS)tslist.get(currentTSindex);
	
			date.setYear (y1 + year );
			date.setMonth (m1);
	
			for ( i=0; i<12; i++ ) {
				value =	((String)v.get(i+2)).trim();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, rtn, "Setting data value for " + date.toString() + " to " + value );
				}
				currentTS.setDataValue ( date, value );
				date.addMonth ( 1 );
			}
	
			// Prepare the next line for reading ...
	
			iline = ifp.readLine();
	
			if ( iline != null && iline.length()>0) {
				if (y1 != 0) { // this is monthly and includes year
					StringUtil.fixedRead ( iline, format_month, format_month_w, v );
					current_year = ( (Integer)v.get(0)).intValue();
				}
				else {
				    StringUtil.fixedRead ( iline, format_annual, format_annual_w, v );	
				}
			}
	
			currentTSindex++;
		}
	} catch ( Exception e ) {
		Message.printWarning ( 3, rtn, "Error reading file.  See log file." );
		Message.printWarning ( 3, rtn, e );
	}
	finally {
		if ( ifp != null ) {
			try {
				ifp.close();
			}
			catch ( IOException e ) {
				// Should not happen
			}
		}
	}
	return tslist;
}

/**
Read a time series from a StateMod format file.  The TSID string is specified
in addition to the path to the file.  It is expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
properly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
@param tsident_string The full identifier for the time series to read.
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (null to read the entire time series).
@param date2 Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, String filename,
	DateTime date1, DateTime date2, String units, boolean read_data )
throws Exception
{	TS	ts = null;
	String routine = "StateMod_TS.readTimeSeries";

	if ( filename == null ) {
	    throw new IOException ( "Requesting StateMod file with null filename.");
	}
	String input_name = filename;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2, routine, "Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	int data_interval = TimeInterval.MONTH;
	try {
		data_interval = getFileDataInterval ( full_fname );
		in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Determine the interval of the file and create a time series that matches...
	ts = TSUtil.newTimeSeries ( tsident_string, true );
	if ( ts == null ) {
		Message.printWarning( 2, routine, "Unable to create time series for \"" + tsident_string + "\"" );
		return ts;
	}
	ts.setIdentifier ( tsident_string );
	// The specific time series is modified...
	// TODO SAM 2007-03-01 Evaluate logic
	readTimeSeriesList ( ts, in, full_fname, data_interval, date1, date2, units, read_data );
	ts.getIdentifier().setInputType("StateMod");
	ts.setInputName ( full_fname );
	// Already in the low-level code
	//ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read all the time series from a StateMod format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated Vector of time series if successful, a NULL pointer if not.
@param fname Name of file to read.
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read.
*/
public static List readTimeSeriesList ( String fname, DateTime date1, DateTime date2, String units, boolean read_data)
throws Exception
{	List tslist = null;
    String routine = "StateMod_TS.readTimeSeriesList";

	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	BufferedReader in = null;
	int data_interval = 0;
    if ( !IOUtil.fileExists(full_fname) ) {
        Message.printWarning( 2, routine, "File does not exist: \"" + full_fname + "\"" );
    }
    if ( !IOUtil.fileReadable(full_fname) ) {
        Message.printWarning( 2, routine, "File is not readable: \"" + full_fname + "\"" );
    }
    data_interval = getFileDataInterval ( full_fname );
    // Let the following thrown FileNotFoundException, etc.
	in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
	tslist = readTimeSeriesList ( null, in, full_fname, data_interval, date1, date2, units, read_data );
	TS ts;
	int nts = 0;
	if ( tslist != null ) {
		nts = tslist.size();
	}
	for ( int i = 0; i < nts; i++ ) {
		ts = (TS)tslist.get(i);
		if ( ts != null ) {
			ts.setInputName ( full_fname );
			// TODO SAM 2008-05-11 is this needed?
			//ts.getIdentifier().setInputType ( "StateMod" );
			ts.getIdentifier().setInputName ( input_name );
		}
	}
	in.close();
	return tslist;
}

/**
Read one or more time series from a StateMod format file.
@return a list of time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return all new time series in the vector.  All data are reset, except for the
identifier, which is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param full_filename Full path to filename, used for messages.
@param reqDate1 Requested starting date to initialize period (or NULL to read the entire time series).
@param fileInterval Indicates the file type (TimeInterval.DAY or TimeInterval.MONTH).
@param reqDate2 Requested ending date to initialize period (or NULL to read the entire time series).
@param units Units to convert to (currently ignored).
@param readData Indicates whether data should be read.
@exception Exception if there is an error reading the time series.
*/
private static List readTimeSeriesList ( TS req_ts, BufferedReader in, String fullFilename,
	int fileInterval, DateTime reqDate1, DateTime reqDate2, String reqUnits, boolean readData )
throws Exception
{	int	dl = 40, i, line_count = 0, m1, m2, y1, y2,
		currentTSindex, current_month = 1, current_year = 0,
		doffset = 2, init_month = 1, init_year, ndata_per_line = 12,
		numts = 0;
	String	chval, iline, routine="StateMod_TS.readTimeSeriesList";
	List v = new Vector (15);
	DateTime date = null;
	if ( fileInterval == TimeInterval.DAY ) {
		date = new DateTime ( DateTime.PRECISION_DAY );
		doffset = 3; // Used when setting data to skip the leading fields on the data line
	}
	else if ( fileInterval == TimeInterval.MONTH ){
	    date = new DateTime ( DateTime.PRECISION_MONTH );
	}
	else {
		throw new InvalidParameterException( "Requested file interval is invalid." );
	}
	boolean	req_id_found = false; // Indicates if we have found the requested TS in the file.
	boolean standard_ts = true; // Non-standard indicates 12 monthly averages in file.

	List tslist = null; // List of time series to return.
	String req_id = null;
	if ( req_ts != null ) {
		req_id = req_ts.getLocation();
	}
	try {// General error handler
		// read first line of the file
		++line_count;
		iline = in.readLine();
		if ( iline == null ) {
			in.close();
			Message.printWarning ( 2, routine, "Zero length file." );
			return null;
		}
		if ( iline.trim().length() < 1 ) {
			in.close();
			Message.printWarning ( 2, routine, "Zero length file." );
			return null;
		}
	
		// Read lines until no more comments are found.  The last line read will
		// need to be processed as the main header line...
	
		while ( iline.startsWith("#") ) {
			++line_count;
			iline = in.readLine();
		}
	
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Done with comments" );	
		}
	
		// Process the main header line...
	
		// SAM:  It looks like some of the replace() files for demandts have the
		// header line malformatted.  Rather than change all the files, check
		// for a '/' in the [3] position and adjust the format.  Print a warning at level 1.
		String format_fileContents = null;
		if ( iline.charAt(3) == '/' ) {
			Message.printWarning ( 3, routine,
			"Non-standard header for file \"" + fullFilename + "\" allowing with work-around." );
			format_fileContents = "i3x1i4x5i5x1i4s5s5";
		}
		else {
			// Probably formatted correctly...
			format_fileContents = "i5x1i4x5i5x1i4s5s5";
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Parsing line for calperiod: \"" + iline + "\""  );
		}
		v = StringUtil.fixedRead ( iline, format_fileContents );
		m1 = ((Integer)v.get(0)).intValue();
		y1 = ((Integer)v.get(1)).intValue();
		m2 = ((Integer)v.get(2)).intValue();
		y2 = ((Integer)v.get(3)).intValue();
		DateTime date1_header;
		if ( fileInterval == TimeInterval.DAY ) {
			date1_header = new DateTime ( DateTime.PRECISION_DAY );
			date1_header.setYear ( y1 );
			date1_header.setMonth ( m1 );
			date1_header.setDay ( 1 );
		}
		else {
			date1_header = new DateTime ( DateTime.PRECISION_MONTH );
			date1_header.setYear ( y1 );
			date1_header.setMonth ( m1 );
		}
		DateTime date2_header;
		if ( fileInterval == TimeInterval.DAY ) {
			date2_header = new DateTime ( DateTime.PRECISION_DAY );
			date2_header.setYear ( y2 );
			date2_header.setMonth ( m2 );
			date2_header.setDay ( TimeUtil.numDaysInMonth(m2,y2) );
		}
		else {
			date2_header = new DateTime ( DateTime.PRECISION_MONTH );
			date2_header.setYear ( y2 );
			date2_header.setMonth ( m2 );
		}
		String units = ((String)v.get(4)).trim();
		String yeartypes = ((String)v.get(5)).trim();
		YearType yeartype = YearType.CALENDAR;
		// Year type is used in one place to initialize the year when
		// transferring data.  However, it is assumed that m1 is always correct for the year type.
		if ( yeartypes.equalsIgnoreCase("WYR") ) {
			yeartype = YearType.WATER;
		}
		else if ( yeartypes.equalsIgnoreCase("IYR") ) {
			yeartype = YearType.NOV_TO_OCT;
		}
		// year that are specified are used to set the period.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Parsed m1=" + m1 + " y1=" +
			y1 + " m2=" + m2 + " y2=" + y2 + " units=\"" + units + "\" yeartype=\"" + yeartypes + "\"" );
		}
	
		int format[] = null;
		int format_w[] = null;
		if ( fileInterval == TimeInterval.DAY ) {
			format = new int[35];
			format_w = new int[35];
			format[0] = StringUtil.TYPE_INTEGER;
			format[1] = StringUtil.TYPE_INTEGER;
			format[2] = StringUtil.TYPE_SPACE;
			format[3] = StringUtil.TYPE_STRING;
			format[4] = StringUtil.TYPE_DOUBLE;
			format[5] = StringUtil.TYPE_DOUBLE;
			format[6] = StringUtil.TYPE_DOUBLE;
			format[7] = StringUtil.TYPE_DOUBLE;
			format[8] = StringUtil.TYPE_DOUBLE;
			format[9] = StringUtil.TYPE_DOUBLE;
			format[10] = StringUtil.TYPE_DOUBLE;
			format[11] = StringUtil.TYPE_DOUBLE;
			format[12] = StringUtil.TYPE_DOUBLE;
			format[13] = StringUtil.TYPE_DOUBLE;
			format[14] = StringUtil.TYPE_DOUBLE;
			format[15] = StringUtil.TYPE_DOUBLE;
			format[16] = StringUtil.TYPE_DOUBLE;
			format[17] = StringUtil.TYPE_DOUBLE;
			format[18] = StringUtil.TYPE_DOUBLE;
			format[19] = StringUtil.TYPE_DOUBLE;
			format[20] = StringUtil.TYPE_DOUBLE;
			format[21] = StringUtil.TYPE_DOUBLE;
			format[22] = StringUtil.TYPE_DOUBLE;
			format[23] = StringUtil.TYPE_DOUBLE;
			format[24] = StringUtil.TYPE_DOUBLE;
			format[25] = StringUtil.TYPE_DOUBLE;
			format[26] = StringUtil.TYPE_DOUBLE;
			format[27] = StringUtil.TYPE_DOUBLE;
			format[28] = StringUtil.TYPE_DOUBLE;
			format[29] = StringUtil.TYPE_DOUBLE;
			format[30] = StringUtil.TYPE_DOUBLE;
			format[31] = StringUtil.TYPE_DOUBLE;
			format[32] = StringUtil.TYPE_DOUBLE;
			format[33] = StringUtil.TYPE_DOUBLE;
			format[34] = StringUtil.TYPE_DOUBLE;
	
			format_w[0] = 4;
			format_w[1] = 4;
			format_w[2] = 1;
			format_w[3] = 12;
			format_w[4] = 8;
			format_w[5] = 8;
			format_w[6] = 8;
			format_w[7] = 8;
			format_w[8] = 8;
			format_w[9] = 8;
			format_w[10] = 8;
			format_w[11] = 8;
			format_w[12] = 8;
			format_w[13] = 8;
			format_w[14] = 8;
			format_w[15] = 8;
			format_w[16] = 8;
			format_w[17] = 8;
			format_w[18] = 8;
			format_w[19] = 8;
			format_w[20] = 8;
			format_w[21] = 8;
			format_w[22] = 8;
			format_w[23] = 8;
			format_w[24] = 8;
			format_w[25] = 8;
			format_w[26] = 8;
			format_w[27] = 8;
			format_w[28] = 8;
			format_w[29] = 8;
			format_w[30] = 8;
			format_w[31] = 8;
			format_w[32] = 8;
			format_w[33] = 8;
			format_w[34] = 8;
		}
		else {
			format = new int[14];
			format[0] = StringUtil.TYPE_INTEGER;
			format[1] = StringUtil.TYPE_STRING;
			format[2] = StringUtil.TYPE_DOUBLE;
			format[3] = StringUtil.TYPE_DOUBLE;
			format[4] = StringUtil.TYPE_DOUBLE;
			format[5] = StringUtil.TYPE_DOUBLE;
			format[6] = StringUtil.TYPE_DOUBLE;
			format[7] = StringUtil.TYPE_DOUBLE;
			format[8] = StringUtil.TYPE_DOUBLE;
			format[9] = StringUtil.TYPE_DOUBLE;
			format[10] = StringUtil.TYPE_DOUBLE;
			format[11] = StringUtil.TYPE_DOUBLE;
			format[12] = StringUtil.TYPE_DOUBLE;
			format[13] = StringUtil.TYPE_DOUBLE;
		
			format_w = new int[14];
			format_w[0] = 5;
			format_w[1] = 12;
			format_w[2] = 8;
			format_w[3] = 8;
			format_w[4] = 8;
			format_w[5] = 8;
			format_w[6] = 8;
			format_w[7] = 8;
			format_w[8] = 8;
			format_w[9] = 8;
			format_w[10] = 8;
			format_w[11] = 8;
			format_w[12] = 8;
			format_w[13] = 8;
		}
		if ( y1 == 0 ) {
			// average monthly series
			standard_ts = false;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Found average monthly series" );	
			}
	
			format[0] = StringUtil.TYPE_STRING;	// Year not used.
			current_year = 0; // Start year will be calendar year 0
			init_year = 0;
			if ( m2 < m1 ) {
				y2 = 1; // End year is calendar year 1.
			}
		}
		else {
			// standard time series, includes a year on input lines
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Found time series" );	
			}
	
			current_year = y1;
			if ( (fileInterval == TimeInterval.MONTH) && (m2 < m1) ) {
				// Monthly data and not calendar year - the first year
				// shown in the data will be water or irrigation year
				// and will not match the calendar dates shown in the header...
				init_year = y1 + 1;
			}
			else {
				init_year = y1;
			}
		}
		current_month = m1;
		init_month = m1;
	
		// Read remaining data lines.  If in the first year, allocate memory
		// for each time series as a new station is encountered...
		currentTSindex = 0;
		TS currentTS = null, ts = null; // Used to fill data.
		// TODO SAM 2007-03-01 Evaluate use
		//int req_ts_index; // Position of requested TS in data.
		String id = null; // Identifier for a row.
	
		// Sometimes, the time series files have empty lines at the
		// bottom, checking it's length seemed to solve the problem.
		String second_iline = null;
		boolean single_ts = false; // Indicates whether a single time series is in the file.
		boolean have_second_line = false;
		int data_line_count = 0;
		while ( true ) {
			if ( data_line_count == 0 ) {
				iline = in.readLine();
				if ( iline == null ) {
					break;
				}
				else if ( iline.startsWith("#") ) {
					// Comment line.  Count the line but do not treat as data...
					++line_count;
					continue;
				}
				// To allow for the case where only one time series is
				// in the file and a req_id is specified that may
				// be different (but always return the file contents), read the second line...
				second_iline = in.readLine();
				have_second_line = true;
				if ( second_iline != null ) {
					// Check to see if the year from the first line
					// is different from the second line, and the
					// identifiers are the same.  If so, assume one time series in the file...
					int line1_year = StringUtil.atoi( iline.substring(0,5).trim() );
					int line2_year = StringUtil.atoi( second_iline.substring(0,5).trim() );
					String line1_id = iline.substring(5,17).trim();
					String line2_id = second_iline.substring(5,17).trim();
					if ( line1_id.equals(line2_id) && (line1_year != line2_year) ) {
						single_ts = true;
						Message.printStatus ( 2, routine, "Single TS detected - reading all." );
						if ( (req_id != null) && !line1_id.equalsIgnoreCase(req_id) ) {
							Message.printStatus ( 2, routine,
							"Reading StateMod file, the requested ID is \"" +
							req_id + "\" but the file contains only \"" + line1_id + "\"." );
							Message.printStatus ( 2, routine,
							"Will read the file's data but use the requested identifier." );
						}
					}
				}
			}
			else if ( have_second_line ) {
				// Special case where the 2nd line was read immediately after the first to check to
				// see if only one time series is in the file.  If here, set the line to
				// what was read before...
				have_second_line = false;
				iline = second_iline;
				second_iline = null;
			}
			else {
				// Read another line...
				iline = in.readLine();
			}
			if ( iline == null ) {
				// No more data...
				break;
			}
			++line_count;
			if ( iline.startsWith("#") ) {
				// Comment line.  Count the line but do not treat as data...
				continue;
			}
			++data_line_count;
			if ( iline.length() == 0 ) {
				// Treat as a blank data line...
				continue;
			}
	
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Parsing line: \"" + iline + "\" line_count=" + line_count +
					" data_line_count=" + data_line_count );
			}
	
			// The first thing that we do is get the time series identifier
			// so we can check against a requested identifier.  If there is
			// only one time series in the file, always use it.
			if ( req_id != null ) {
				// Have a requested identifier and there are more than one time series.
				// Get the ID from the input line.  Don't parse
				// out the remaining lines unless this line is a match...
				if ( fileInterval == TimeInterval.MONTH ) {
					chval = iline.substring(5,17);
				}
				else {
					// Daily, offset for month...
					chval = iline.substring(9,21);
				}
				// Need this below...
				id = chval.trim();
	
				if ( !single_ts ) {
					if ( !id.equalsIgnoreCase(req_id) ) {
						// We are not interested in this time series so don't process...
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl,routine, "Looking for \"" + req_id +
							"\".  Not interested in \"" +id+ "\".  Continuing." );
						}
						continue;
					}
				}
			}
	
			// Parse the data line...
			StringUtil.fixedRead ( iline, format, format_w, v );
			if ( standard_ts ) {
				// This is monthly and includes year
				current_year = ( (Integer)v.get(0)).intValue();
				if ( fileInterval == TimeInterval.DAY ) {
					current_month = ( (Integer)v.get(1)).intValue();
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine,
						"Found id!  Current date is " + current_year + "-" + current_month );
					}
				}
				else {
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Found id!  Current year is " + current_year );
					}
				}
			}
			else {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Found ID!  Read average format." );
				}
			}
	
			// If we are reading the entire file, set id to current id
			if ( req_id == null ) {
				if ( fileInterval == TimeInterval.DAY ) {
					// Have year, month, and then ID...
					id = ((String)v.get(2)).trim();
				}
				else {
					// Have year, and then ID...
					id = ((String)v.get(1)).trim();
				}
			}
	
			// We are still establishing the list of stations in file
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Current year: " + current_year + ", Init year: " + init_year );
			}
			if ( ((fileInterval == TimeInterval.DAY) && (current_year == init_year) &&
				(current_month == init_month)) || ((fileInterval == TimeInterval.MONTH) &&
				(current_year == init_year)) ) {
				if ( req_id == null ) {
					// Create a new time series...
					if ( fileInterval == TimeInterval.DAY ) {
						ts = new DayTS();
					}
					else {
						ts = new MonthTS();
					}
				}
				else if ( id.equalsIgnoreCase(req_id) || single_ts ){
					// We want the requested time series to get filled in...
					ts = req_ts;
					req_id_found = true;
					numts = 1;
					// Save this index as that used for the requested time series...
					// TODO SAM 2007-04-10 Evaluate use
					//req_ts_index = currentTSindex;
				}
				// Else, we already caught this in a check above and would not get to here.
	
				if ( (reqDate1 != null) && (reqDate2 != null) ) {
					// Allocate memory for the time series based on the requested period.
					ts.setDate1 ( reqDate1 );
					ts.setDate2 ( reqDate2 );
					ts.setDate1Original ( date1_header );
					ts.setDate2Original ( date2_header );
				}
				else {
					// Allocate memory for the time series based on the file header....
					date.setMonth ( m1 );
					date.setYear ( y1 );
					if ( fileInterval == TimeInterval.DAY ) {
						date.setDay ( 1 );
					}
					ts.setDate1 ( date );
					ts.setDate1Original ( date1_header );
	
					date.setMonth ( m2 );
					date.setYear ( y2 );
					if ( fileInterval == TimeInterval.DAY ) {
						date.setDay ( TimeUtil.numDaysInMonth ( m2, y2 ) );
					}
					ts.setDate2 ( date );
					ts.setDate2Original ( date2_header );
				}
	
				if ( readData ) {
					ts.allocateDataSpace();
				}
				
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Setting data units to " + units );
				}
				ts.setDataUnits ( units );
				ts.setDataUnitsOriginal ( units );
	
				// The input name is the full path to the input file...
				ts.setInputName ( fullFilename );
				// Set other identifier information.  The readTimeSeries() version that takes a full
				// identifier will reset the file information because it knows
				// whether the original filename was from the scenario, etc.
				TSIdent ident = new TSIdent ();
				ident.setLocation ( id );
				// TODO SAM 2008-05-11 - should not need now that input type is default
				//ident.setSource ( "StateMod" );
				if ( fileInterval == TimeInterval.DAY ) {
					ident.setInterval ( "DAY" );
				}
				else {
				    ident.setInterval ( "MONTH" );
				}
				ident.setInputType ( "StateMod" );
				ident.setInputName ( fullFilename );
				ts.setDescription ( id );
				// May be forcing a read if only one time series but only reset the identifier if the
				// file identifier does match the requested...
				if ( ((req_id != null) && req_id_found && id.equalsIgnoreCase(req_id)) || (req_id == null) ) {
					// Found the matching ID.
					ts.setIdentifier ( ident );
					if ( Message.isDebugOn) {
						Message.printDebug ( dl, routine, "Setting id to " + id + " and ident to " + ident );
					}
					ts.addToGenesis ( "Read StateMod TS for " +
					ts.getDate1() + " to " + ts.getDate2() + " from \"" + fullFilename + "\"" );
				}
				if ( !req_id_found ) {
					// Attach new time series to list.  This is only done if we have not passed
					// in a requested time series to fill.
					if ( tslist == null ) {
						tslist = new Vector(100);
					}
					tslist.add ( ts );
					numts++;
				}
			}
			else {
				if ( !readData ) {
					// Done reading the data.
					break;
				}
			}
	
			// If we are working through the first year, currentTSindex will
			// be the last element index.  On the other hand, if we have
			// already established the list and are filling the rest of the
			// rows, currentTSindex should be reset to 0.  This assumes that
			// the number and order of stations is consistent in the file from year to year.
	
			if ( currentTSindex >= numts ) {
				currentTSindex = 0;
			}
	
			if ( !req_id_found ) {
				// Filling a vector of TS...
				currentTS = (TS)tslist.get(currentTSindex);
			}
			else {
				// Filling a single time series...
				currentTS = (TS)req_ts;
			}
	
			if ( fileInterval == TimeInterval.DAY ) {
				// Year from the file is always calendar year...
				date.setYear ( current_year );
				// Month from the file is always calendar month...
				date.setMonth ( current_month );
				// Day always starts at 1...
				date.setDay (1);
			}
			else {
				// Monthly data.  The year is for the calendar type and
				// therefore the starting year may actually need to
				// be set to the previous year.  Don't do the shift for average monthly values.
				if ( standard_ts && (yeartype != YearType.CALENDAR) ) {
					date.setYear ( current_year - 1 );
				}
				else {
					date.setYear ( current_year );
				}
				// Month is assumed from calendar type - it is assumed that the header is correct...
				date.setMonth (m1);
			}
			if ( reqDate2 != null ) {
				if ( date.greaterThan(reqDate2) ) {
					break;
				}
			}
	
			if ( readData ) {
				if ( fileInterval == TimeInterval.DAY ) {
					// Need to loop through the proper number of days for the month...
					ndata_per_line = TimeUtil.numDaysInMonth(date.getMonth(), date.getYear() );
				}
				for ( i=0; i < ndata_per_line; i++ ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Setting data value for " +
						date.toString() + " to " + ((Double)v.get(i + doffset)));
					}
					currentTS.setDataValue ( date, ((Double)v.get(i+doffset)).doubleValue());
					if ( fileInterval == TimeInterval.DAY ) {
						date.addDay ( 1 );
					}
					else {
						date.addMonth ( 1 );
					}
				}
			}
			currentTSindex++;
		}
	} // Main try around routine.
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error reading file near line " + line_count );
		Message.printWarning ( 3, routine, e );
		throw new Exception ( "Error reading StateMod file" );
	}
	return tslist;
}

/**
This method writes a monthly pattern file in StateMod format.
The dates can each be specified as null, in which case, the
maximum period of record for all the time series will be used for output.  
Time series with shorter periods will be filled with "MissingDV."
Currently this is only enabled for monthly data.
@param out The PrintWriter to which to write.
@param comments output comments for top of file (e.g., command file)
@param tslist A vector of time series.
@param date1 Start of period to write.
@param date2 End of period to write.
@param props Properties to control the output.  The following properties are recognized:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>CalendarType</b></td>
<td>The type of calendar, either "Water" (Oct through Sep), "NovToOct" (Nov through Oct),
or "Calendar" (Jan through Dec), consistent with the YearType enumeration.
</td>
<td>CalenderYear (but may be made sensitive to the data type or units in the future).</td>
</tr>

<tr>
<td><b>MissingValue</b></td>
<td>The missing data value to use for output (previously MissingDataValue).
</td>
<td>-999</td>
</tr>

</table>
@exception Exception if there is an error writing the file.
*/
public static void writePatternTimeSeriesList ( String filename, List<String> comments, List tslist,
	DateTime date1, DateTime date2, PropList props )
throws Exception
{	String rtn = "StateMod_TS.writePatternTimeSeriesList";
	String cmnt = PERMANENT_COMMENT;
	String iline; // string for use with StringUtil.formatString
	int year = 0;
	String value; // Used when printing each data item
	List v = new Vector(20,10);	// Used while formatting
	StringMonthTS tsptr = null;	// Reference to current time series

	// Verify that the ts vector is not null.  Then count the number of series in list.
	if ( tslist == null ) {
		Message.printWarning ( 2, rtn, "Null time series list" );
		return;
	}

	// Get the properties for output...

	if ( props == null ) {
		props = new PropList ( "StateMod" );
	}
	String prop_value = props.getValue ( "CalendarType" );
	String output_format = "CYR"; // Default
	if ( prop_value == null ) {
		prop_value = "" + YearType.CALENDAR;
	}
	YearType yearType = YearType.valueOfIgnoreCase(prop_value);
	if ( yearType == YearType.CALENDAR ) {
		output_format = "CYR";
	}
	else if ( yearType == YearType.WATER ) {
		output_format = "WYR";
	}
	else if ( yearType == YearType.NOV_TO_OCT ) {
		output_format = "IYR";
	}

	String MissingDV = "-999.0"; // Default
	prop_value = props.getValue ( "MissingValue" );
	if ( prop_value == null ) {
		// Try old...
		prop_value = props.getValue ( "MissingDataValue" );
	}
	if ( prop_value != null ) {
		MissingDV = prop_value;
	}

	boolean print_genesis = false; // TODO SAM 2005-05-06 enable later?

	int nseries = tslist.size();
	int req_interval_base = TimeInterval.MONTH;
						// The interval for time series passed to this method.  Get
						// from the first non-null time series - later may pass in.

	// Determine the interval by checking the time series.  The first
	// interval found will be written later in the code...

	for ( int i = 0; i < nseries; i++ ) {
		if ( tslist.get(i) == null ) {
			continue;
		}
		/* TODO SAM 2005-05-06 Support daily or yearly later?
		if ( tslist.elementAt(i) instanceof DayTS ) {
			req_interval_base = TimeInterval.DAY;
		}
		*/
		else if ( tslist.get(i) instanceof StringMonthTS ) {
			req_interval_base = TimeInterval.MONTH;
		}
		else {
			iline = "StateMod time series list has time series other than monthly.";
			Message.printWarning ( 2, rtn, iline );
			throw new Exception ( iline );
		}
		break;
	}

	// Check the intervals to make sure that all the intervals are
	// consistent.  Put in a separate loop from above because to make sure
	// that the requested interval is determined first.

	int interval_base;	// Used to check each time series against the
				// interval for the file, which was determined above

	boolean [] include_ts = new boolean[nseries];
	for ( int i=0; i<nseries; i++ ) {
		include_ts[i] = true;
		if ( tslist.get(i) == null ) {
			include_ts[i] = false;
			continue;
		}
		tsptr = (StringMonthTS)tslist.get(i);
		interval_base = tsptr.getDataIntervalBase();
		if ( interval_base != req_interval_base ) {
			include_ts[i] = false;
			if ( req_interval_base == TimeInterval.MONTH ) {
				Message.printWarning ( 2, rtn, "A TS interval other than monthly " +
				"detected for " + tsptr.getIdentifier() + " - skipping in output.");
			}
			/* TODO SAM 2005-05-06 Might enable daily later...
			else if ( req_interval_base == TimeInterval.DAY ) {
				Message.printWarning ( 2, rtn,
				"A TS interval other than daily detected for " + tsptr.getIdentifier() + " - skipping in output.");
			}
			*/
		}
	}

	// Open the file...

	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = IOUtil.processFileHeaders (
		null, // No need to update the old file headers
		full_filename, // New file to write
		comments, // New comments to add.
		commentIndicators, ignoredCommentIndicators, 0 );
	if ( out == null ) {
		Message.printWarning ( 3, rtn, "Error writing time series to \"" + full_filename + "\"" );
		throw new Exception ( "Error opening output file \"" + full_filename + "\"" );
	}

	// Write comments at the top of the file...

	out.println ( cmnt );
	out.println ( cmnt + " StateMod format pattern time series" );
	out.println ( cmnt + " ***********************************" );
	out.println ( cmnt );
	if ( output_format.equalsIgnoreCase( "WYR" )) {
		out.println ( cmnt + " Years Shown = Water Years (Oct to Sep)" );
	}
	else if ( output_format.equalsIgnoreCase( "IYR" )) {
		out.println ( cmnt + " Years Shown = Irrigation Years (Nov to Oct)" );
	}
	else {
		// if ( output_format.equalsIgnoreCase ("CYR" ))
		out.println ( cmnt + " Years Shown = Calendar Years" );
	}
	out.println( cmnt + " The period of record for each time series may vary");
	out.println( cmnt + " because of the original input and data processing steps.");
	out.println ( cmnt );

	// Print each time series id, description, and type...

	out.println ( cmnt + "     TS ID                    Type" +
	"   Source   Units  Period of Record    Location    Description");

	String	empty_string = "-", tmpdesc, tmpid, tmplocation, tmpsource, tmptype, tmpunits;
	String	format= "%s %3d %-24.24s %-6.6s %-8.8s %-6.6s %3.3s/%d - %3.3s/%d %-12.12s%-24.24s";
	List genesis = null;

	for ( int i=0; i < nseries; i++ ) {
		tsptr = (StringMonthTS)tslist.get(i);
		tmpid = tsptr.getIdentifierString();

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, rtn, "Processing time series [" + i+ "] \"" + tmpid + "\"" );
		}
		if ( tmpid.length() == 0 ) {
			tmpid = empty_string;
		}

		tmpdesc = tsptr.getDescription();
		if ( tmpdesc.length() == 0 ) {
			tmpdesc = empty_string;
		}

		tmptype = tsptr.getIdentifier().getType();
		if ( tmptype.length() == 0 ) {
			tmptype = empty_string;
		}

		tmpsource = tsptr.getIdentifier().getSource();
		if ( tmpsource.length() == 0 ) {
			tmpsource = empty_string;
		}

		tmpunits = "";

		tmplocation = tsptr.getIdentifier().getLocation();
		if (tmplocation.length() == 0) {
			tmplocation = empty_string;
		}

		v.clear();
		v.add ( cmnt );
		v.add ( new Integer ( i+1 ));
		v.add ( tmpid );
		v.add ( tmptype );
		v.add ( tmpsource );
		v.add ( tmpunits );
		v.add ( TimeUtil.monthAbbreviation(tsptr.getDate1().getMonth()));
		v.add ( new Integer ( tsptr.getDate1().getYear()));
		v.add ( TimeUtil.monthAbbreviation(tsptr.getDate2().getMonth()));
		v.add ( new Integer ( tsptr.getDate2().getYear()));
		v.add ( tmplocation );
		v.add ( tmpdesc );

		iline = StringUtil.formatString ( v, format );
		out.println ( iline );

		// Print the genesis information if requested...

		if ( print_genesis ) {
			genesis = tsptr.getGenesis();
			if ( genesis != null ) {
				int size = genesis.size();
				if ( size > 0 ) {
					out.println ( cmnt + "      Time series creation history:" );
					for(int igen = 0; igen < size; igen++){
						out.println( cmnt + "      " + (String)genesis.get(igen) );
					}
				}
			}
		}
	}
	out.println ( cmnt );

	// Ready to write to file.  Check for no data, which was not checked
	// before because the comments should be printed even if there is no time series data to print.

	if ( nseries == 0 ) {
		return;
	}

	// Switch to non-permanent comments...

	cmnt = NONPERMANENT_COMMENT;
	out.println( cmnt + "EndHeader" );
	out.println( cmnt );
	if ( req_interval_base == TimeInterval.MONTH ) {
		if ( output_format.equalsIgnoreCase( "WYR" )) {
			out.println( cmnt + " Yr ID            Oct     Nov     Dec     Jan" +
			"     Feb     Mar     Apr     May     Jun     Jul     Aug     Sep     ");
		}
		else if ( output_format.equalsIgnoreCase( "IYR" )) {
			out.println( cmnt + " Yr ID            Nov     Dec     Jan     Feb" +
			"     Mar     Apr     May     Jun     Jul     Aug     Sep     Oct     ");
		}
		else {
			out.println( cmnt + " Yr ID            Jan" +
			"     Feb     Mar     Apr     May     Jun     Jul" +
			"     Aug     Sep     Oct     Nov     Dec     " );
		}

		out.println ( cmnt + "-e-b----------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------eb------e" );
	}
	/* TODO SAM 2005-05-06 maybe enable daily later...
	else {	// Daily output...
		out.println( cmnt + 
		"Yr  Mo ID            d(x,1)  d(x,2)  d(x,3)  " +
		"d(x,4)  d(x,5)  d(x,6)  d(x,7)  d(x,8)  d(x,9) " +
		"d(x,10) d(x,11) d(x,12) d(x,13) d(x,14) d(x,15) " +
		"d(x,16) d(x,17) d(x,18) d(x,19) d(x,20) d(x,21) " +
		"d(x,22) d(x,23) d(x,24) d(x,25) d(x,26) d(x,27) " +
		"d(x,28) d(x,29) d(x,30) d(x,31)   " );

		out.println ( 
		cmnt + "--xx--xb----------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------e" );
	}
	*/

	// Calculate period of record using months since that is the block of
	// time that StateMod operates with...

	int month1 = 0, year1 = 0, month2 = 0, year2 = 0;
	if ( date1 != null ) {
		month1 = date1.getMonth();
		year1 = date1.getYear();
	}
	if ( date2 != null ) {
		month2 = date2.getMonth();
		year2 = date2.getYear();
	}
	// Initialize...
	DateTime req_date1 = new DateTime (DateTime.PRECISION_MONTH);
	DateTime req_date2 = new DateTime (DateTime.PRECISION_MONTH);
	req_date1.setMonth ( month1 );
	req_date1.setYear ( year1 );
	req_date2.setMonth ( month2 );
	req_date2.setYear ( year2 );

	// Define string variables for output;
	// initial_format contains the initial part of each output line
	//	For monthly, either
	//	"year ID3456789012" (monthly) or
	//	"     ID3456789012" (average monthly).
	//	For daily,
	//	"year  mo ID3456789012"
	// iline_format_buffer is created on the fly (it depends on precision
	// 	and initial_format)
	// These are set in the following section
	String initial_format = "";

	// If period of record of interest was not requested, find
	// period of record that covers all time series...
	String yeartype = "WYR"; // Default

	if ( (date1 == null) || (date2 == null) ) {
		try { TSLimits valid_dates = TSUtil.getPeriodFromTS ( tslist, TSUtil.MAX_POR );
			if ( (month1==0) || (year1==0) ) {
				req_date1.setMonth ( valid_dates.getDate1().getMonth());
				req_date1.setYear ( valid_dates.getDate1().getYear());
			}
			if ( (month2==0) || (year2==0) ) {
				req_date2.setMonth ( valid_dates.getDate2().getMonth());
				req_date2.setYear ( valid_dates.getDate2().getYear());
			}
		} catch ( Exception e ) {
			Message.printWarning ( 3, rtn, "Unable to determine output period." );
			throw new Exception ( "Unable to determine output period." );
		}
	}

	// Set req_date* to the appropriate month if req_date* doesn't
	// agree with output_format (e.g., if "WYR" requested but req_date1 != 10)
	if ( output_format.equalsIgnoreCase ( "WYR" )) {
		while ( req_date1.getMonth() != 10 ) {
			req_date1.addMonth ( -1 );
		}
		while ( req_date2.getMonth() != 9 ) {
			req_date2.addMonth ( 1 );
		}
		year = req_date1.getYear() + 1;
		yeartype = "WYR";
	}
	else if ( output_format.equalsIgnoreCase ( "IYR" )) {
		while ( req_date1.getMonth() != 11 ) {
			req_date1.addMonth ( -1 );
		}
		while ( req_date2.getMonth() != 10 ) {
			req_date2.addMonth ( 1 );
		}
		year = req_date1.getYear() + 1;
		yeartype = "IYR";
	}
	else {
		// if ( output_format.equalsIgnoreCase ( "CYR" ))
		while ( req_date1.getMonth() != 1 ) {
			req_date1.addMonth ( -1 );
		}
		while ( req_date2.getMonth() != 12 ) {
			req_date2.addMonth ( 1 );
		}
		year = req_date1.getYear();
		yeartype = "CYR";
	}
	format = "   %2d/%4d  -     %2d/%4d%5.5s" + StringUtil.formatString ( yeartype,"%5.5s");
	// Format for start of line...
	if ( req_interval_base == TimeInterval.MONTH ) {
		initial_format = "%4d %-12.12s";
	}
	/* TODO SAM 2005-05-06 maybe enable daily later
	else {
	    // daily...
		initial_format = "%4d%4d %-12.12s";
	}
	*/

	// Write the header line with the period of record...

	v.clear();
	v.add ( new Integer ( req_date1.getMonth()));
	v.add(new Integer (req_date1.getYear()));
	v.add ( new Integer ( req_date2.getMonth()));
	v.add(new Integer (req_date2.getYear()));
	v.add ( "" );
	iline = StringUtil.formatString ( v, format );
	out.println ( iline );

	// Write the data...

	// date is the starting date for each line and is incremented once
	// 	each station's time series has been written for that year
	// cdate is a counter for 12 months' worth of data for each station
	Message.printStatus ( 2, rtn, "Writing time series data for " + req_date1 + " to " + req_date2 );
	DateTime date = new DateTime ( DateTime.PRECISION_MONTH );
	DateTime cdate = new DateTime ( DateTime.PRECISION_MONTH );
	date.setMonth ( req_date1.getMonth());
	date.setYear ( req_date1.getYear());
	List iline_v = null; // List for output lines.
	int	mon, j;	// counters

	if ( req_interval_base == TimeInterval.MONTH ) {
		iline_v = new Vector(15,1);
	}
	/* TODO SAM 2005-05-06 maybe enable daily later
	else {
	    // Daily...
		iline_v = new Vector(36,1);
	}
	*/

	// Buffer that is used to format each line...

	StringBuffer iline_format_buffer = new StringBuffer();

	if ( req_interval_base == TimeInterval.MONTH ) {
		// Monthly data file.  Need to output in the calendar for the
		// file, which results in a little juggling of data...
		// Print one year at a time for each time series

		year--;	// Decrment because the loop increments it
		String iline_format = initial_format +
		"%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s%8.8s";
		for ( ; date.lessThanOrEqualTo(req_date2); date.addMonth(12)) {
			year++;
			for ( j = 0; j < nseries; j++ ) {
				cdate.setMonth ( date.getMonth());
				cdate.setYear ( date.getYear());
				// First, clear this string out, then append to
				// it until ready to println to the output file...
				if ( !include_ts[j] ) {
					continue;
				}
				tsptr = (StringMonthTS)tslist.get(j);
				if ( tsptr.getDataIntervalBase() != req_interval_base ) {
					// We've already warned user above.
					continue;
				}
				iline_v.clear();
				iline_format_buffer.setLength(0);
				iline_format_buffer.append ( initial_format );
				iline_v.add( new Integer (year));
				iline_v.add( tsptr.getIdentifier().getLocation());
	
				for (mon=0; mon <12; mon++) {
					value = tsptr.getDataValueAsString (cdate);
	
					if (tsptr.isDataMissing (value)) {
						// Missing data so don't add to the annual value.  Print
						// using the same format as for other data...
						iline_v.add ( MissingDV );
						if ( Message.isDebugOn ) {
							// Wrap to increase performance...
							Message.printWarning ( 20, rtn, "Missing Data Found in TS at "+
							cdate.toString(DateTime.FORMAT_YYYY_MM) + ", printing " + MissingDV );
						}
					}
					else {
						iline_v.add ( value );
					}
					cdate.addMonth(1);
				}
	
				iline = StringUtil.formatString (iline_v, iline_format );
				out.println ( iline );
			}
		}
	}
	out.flush();
	out.close();
}

/**
This method writes a file in StateMod format.  It is the lowest-level write
method.  The dates can each be specified as null, in which case, the
maximum period of record for all the time series will be used for output.  
Time series with shorter periods will be filled with "MissingDV."
@param out The PrintWriter to which to write.
@param tslist A list of time series.
@param date1 Start of period to write.
@param date2 End of period to write.
@param outputYearType output year type
@param MissingDV Value to be printed when missing values are encountered.
@param req_precision Requested precision of output (number of digits after the decimal
point).  The default is generally 2.  This should be set according to the
datatype in calling routines and is not automatically set here.  The full width
for time series values is 8 characters and 10 for the total.
@param print_genesis Specify as true to include time series genesis information
in the file header, or false to omit from the header.
@exception Exception if there is an error writing the file.
*/
private static void writeTimeSeriesList ( PrintWriter out, List tslist, 
	DateTime date1, DateTime date2, YearType outputYearType, double MissingDV,
	int req_precision, boolean print_genesis )
throws Exception
{	String rtn	= "StateMod_TS.writeTimeSeriesList";
	//String cmnt	= "#>"; // non-permanent comment string
	// SAM switch to the following when doing genesis output...
	String cmnt	= PERMANENT_COMMENT;
	String iline; // string for use with StringUtil.formatString
	double value=0, annual_sum;	// Used when printing each data item
	int annual_count = 0, year = 0; // A "counter" for the year
	List v = new Vector(20,10);	// Used while formatting
	TS tsptr = null; // Reference to current time series
	boolean standard_ts = true;	// Non-standard indicates 12 monthly average values

	// Verify that the ts vector is not null.  Then count the number of series in list.
	if ( tslist == null ) {
		Message.printWarning ( 3, rtn, "Null time series list" );
		return;
	}
	int nseries = tslist.size();
	int req_interval_base = TimeInterval.MONTH;
						// The interval for time series passed to this method.  Get
						// from the first non-null time series - later may pass in.

	// Determine the interval by checking the time series.  The first
	// interval found will be written later in the code...

	for ( int i = 0; i < nseries; i++ ) {
		if ( tslist.get(i) == null ) {
			continue;
		}
		if ( tslist.get(i) instanceof DayTS ) {
			req_interval_base = TimeInterval.DAY;
		}
		else if ( tslist.get(i) instanceof MonthTS ) {
			req_interval_base = TimeInterval.MONTH;
		}
		else {
			iline = "StateMod time series list has time series other than daily or monthly.";
			Message.printWarning ( 2, rtn, iline );
			throw new Exception ( iline );
		}
		break;
	}

	// Check the intervals to make sure that all the intervals are
	// consistent.  Also, figure out if summary should be total or average.
	// Use the first non-null time series.  Put in a separate loop from
	// above because to make sure that the requested interval is determined first.

	boolean unitsfound = false;
	String year_title = "Total";
	int interval_base;	// Used to check each time series against the
				// interval for the file, which was determined above

	String output_units = ""; // Output units.
	boolean [] include_ts = new boolean[nseries];
	for (int i=0; i<nseries; i++ ) {
		include_ts[i] = true;
		if ( tslist.get(i) == null ) {
			include_ts[i] = false;
			continue;
		}
		tsptr = (TS)tslist.get(i);
		// Get the units for output...
		if ( !unitsfound ) {
			output_units = tsptr.getDataUnits();
			unitsfound = true;
		}
		// Determine if a monthly_average - check all time series
		// because mixing a monthly average with normal monthly data
		// will result in output starting in year 0.
		if ( tsptr.getDate1().getYear() == 0 ) {
			standard_ts = false;
		}
		interval_base = tsptr.getDataIntervalBase();
		if ( interval_base != req_interval_base ) {
			include_ts[i] = false;
			if ( req_interval_base == TimeInterval.MONTH ) {
				Message.printWarning ( 3, rtn, "A TS interval other than monthly " +
				"detected for " + tsptr.getIdentifier() + " - skipping in output.");
			}
			else if ( req_interval_base == TimeInterval.DAY ) {
				Message.printWarning ( 3, rtn, "A TS interval other than daily " +
				"detected for " + tsptr.getIdentifier() + " - skipping in output.");
			}
		}
	}
	boolean do_total = true;
	if ( output_units.equalsIgnoreCase("AF") || output_units.equalsIgnoreCase("ACFT") ||
		output_units.equalsIgnoreCase("AF/M") || output_units.equalsIgnoreCase("IN") ||
		output_units.equalsIgnoreCase("MM") ) {
		// Assume only these need to be totaled...
		Message.printStatus ( 2, rtn,
		"Using total for annual value, based on units \"" + output_units + "\"" );
		do_total = true;
		year_title = "Total";	// Also used as "month_title" for daily
	}
	else {
		Message.printStatus ( 2, rtn,
		"Using average for annual value, based on units \"" + output_units + "\""  );
		do_total = false;
		year_title = "Average";
	}

	// Write comments at the top of the file...

	out.println ( cmnt );
	out.println ( cmnt + " StateMod time series" );
	out.println ( cmnt + " ********************" );
	out.println ( cmnt );
	if ( outputYearType == YearType.WATER) {
		out.println ( cmnt + " Years Shown = Water Years (Oct to Sep)" );
	}
	else if ( outputYearType == YearType.NOV_TO_OCT) {
		out.println ( cmnt + " Years Shown = Irrigation Years (Nov to Oct)" );
	}
	else {
		// if ( output_format.equalsIgnoreCase ("CYR" ))
		out.println ( cmnt + " Years Shown = Calendar Years" );
	}
	out.println( cmnt + " The period of record for each time series may vary");
	out.println( cmnt + " because of the original input and data processing steps.");
	out.println ( cmnt );

	// Print each time series id, description, and type...

	out.println ( cmnt + "     TS ID                    Type" +
	"   Source   Units  Period of Record    Location    Description");

	String	empty_string = "-", tmpdesc, tmpid, tmplocation, tmpsource, tmptype, tmpunits;
	String	format= "%s %3d %-24.24s %-6.6s %-8.8s %-6.6s %3.3s/%d - %3.3s/%d %-12.12s%-24.24s";
	List genesis = null;

	for ( int i=0; i < nseries; i++ ) {
		tsptr = (TS)tslist.get(i);
		tmpid = tsptr.getIdentifierString();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, rtn, "Processing time series [" + i+ "] \"" + tmpid + "\"" );
		}
		if (tmpid.length() == 0 ) {
			tmpid = empty_string;
		}

		tmpdesc = tsptr.getDescription();
		if ( tmpdesc.length() == 0 ) {
			tmpdesc = empty_string;
		}

		tmptype = tsptr.getIdentifier().getType();
		if ( tmptype.length() == 0 ) {
			tmptype = empty_string;
		}

		tmpsource = tsptr.getIdentifier().getSource();
		if ( tmpsource.length() == 0 ) {
			tmpsource = empty_string;
		}

		tmpunits = tsptr.getDataUnits();
		if ( tmpunits.length() == 0) {
			tmpunits = empty_string;
		}

		tmplocation = tsptr.getIdentifier().getLocation();
		if (tmplocation.length() == 0) {
			tmplocation = empty_string;
		}

		v.clear();
		v.add ( cmnt );
		v.add ( new Integer ( i+1 ));
		v.add ( tmpid );
		v.add ( tmptype );
		v.add ( tmpsource );
		v.add ( tmpunits );
		v.add ( TimeUtil.monthAbbreviation(	tsptr.getDate1().getMonth()));
		v.add ( new Integer ( tsptr.getDate1().getYear()));
		v.add ( TimeUtil.monthAbbreviation(	tsptr.getDate2().getMonth()));
		v.add ( new Integer ( tsptr.getDate2().getYear()));
		v.add ( tmplocation );
		v.add ( tmpdesc );

		iline = StringUtil.formatString ( v, format );
		out.println ( iline );

		// Print the genesis information if requested...

		if ( print_genesis ) {
			genesis = tsptr.getGenesis();
			if ( genesis != null ) {
				int size = genesis.size();
				if ( size > 0 ) {
					out.println ( cmnt + "      Time series creation history:" );
					for(int igen = 0; igen < size; igen++){
						out.println( cmnt + "      " + (String)genesis.get(igen) );
					}
				}
			}
		}
	}
	out.println ( cmnt );

	// Ready to write to file.  Check for no data, which was not checked
	// before because the comments should be printed even if there is no time series data to print.

	if ( nseries == 0 ) {
		return;
	}

	// Switch to non-permanent comments...

	cmnt = NONPERMANENT_COMMENT;
	out.println( cmnt + "EndHeader" );
	out.println( cmnt );
	if ( req_interval_base == TimeInterval.MONTH ) {
		if ( outputYearType == YearType.WATER ) {
			out.println( cmnt + " Yr ID            Oct     Nov     Dec     Jan" +
			"     Feb     Mar     Apr     May     Jun     Jul" +
			"     Aug     Sep     " + year_title );
		}
		else if ( outputYearType == YearType.NOV_TO_OCT ) {
			out.println( cmnt + " Yr ID            Nov     Dec     Jan     Feb" +
			"     Mar     Apr     May     Jun     Jul     Aug" +
			"     Sep     Oct     " + year_title );
		}
		else {
			out.println( cmnt + " Yr ID            Jan" +
			"     Feb     Mar     Apr     May     Jun     Jul" +
			"     Aug     Sep     Oct     Nov     Dec     " +
			year_title );
		}

		out.println ( cmnt + "-e-b----------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------e" +
		"b------eb------eb--------e" );
	}
	else {
		// Daily output...
		out.println( cmnt + "Yr  Mo ID            d(x,1)  d(x,2)  d(x,3)  " +
		"d(x,4)  d(x,5)  d(x,6)  d(x,7)  d(x,8)  d(x,9) " +
		"d(x,10) d(x,11) d(x,12) d(x,13) d(x,14) d(x,15) " +
		"d(x,16) d(x,17) d(x,18) d(x,19) d(x,20) d(x,21) " +
		"d(x,22) d(x,23) d(x,24) d(x,25) d(x,26) d(x,27) " +
		"d(x,28) d(x,29) d(x,30) d(x,31)   " + year_title );

		out.println ( cmnt + "--xx--xb----------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb------e" +
		"b------eb------eb------eb------eb------eb------eb--------e" );
	}

	// Calculate period of record using months since that is the block of
	// time that StateMod operates with...

	int month1 = 0, year1 = 0, month2 = 0, year2 = 0;
	if ( date1 != null ) {
		month1 = date1.getMonth();
		year1 = date1.getYear();
	}
	if ( date2 != null ) {
		month2 = date2.getMonth();
		year2 = date2.getYear();
	}
	// Initialize...
	DateTime req_date1 = new DateTime (DateTime.PRECISION_MONTH);
	DateTime req_date2 = new DateTime (DateTime.PRECISION_MONTH);
	req_date1.setMonth ( month1 );
	req_date1.setYear ( year1 );
	req_date2.setMonth ( month2 );
	req_date2.setYear ( year2 );

	// Define string variables for output;
	// initial_format contains the initial part of each output line
	//	For monthly, either
	//	"year ID3456789012" (monthly) or
	//	"     ID3456789012" (average monthly).
	//	For daily,
	//	"year  mo ID3456789012"
	// iline_format_buffer is created on the fly (it depends on precision and initial_format)
	// These are set in the following section
	String initial_format;
	String year_format = "%4d"; // Only used when formatting total to printed
	String month_format = "%4d"; // Only used when formatting total to printed
	String id_format = " %-12.12s"; // Only used when formatting total to printed

	// If period of record of interest was not requested, find
	// period of record that covers all time series...
	String yeartype = "WYR";
	if ( standard_ts ) {
		if ( (date1 == null) || (date2 == null) ) {
			try {
				TSLimits valid_dates = TSUtil.getPeriodFromTS ( tslist, TSUtil.MAX_POR );
				if ( (month1==0) || (year1==0) ) {
					req_date1.setMonth ( valid_dates.getDate1().getMonth());
					req_date1.setYear ( valid_dates.getDate1().getYear());
				}
				if ( (month2==0) || (year2==0) ) {
					req_date2.setMonth ( valid_dates.getDate2().getMonth());
					req_date2.setYear ( valid_dates.getDate2().getYear());
				}
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, rtn, "Unable to determine output period." );
				throw new Exception ( "Unable to determine output period." );
			}
		}

		// Set req_date* to the appropriate month if req_date* doesn't
		// agree with output_format (e.g., if "WYR" requested but req_date1 != 10)
		if ( outputYearType == YearType.WATER ) {
			while ( req_date1.getMonth() != 10 ) {
				req_date1.addMonth ( -1 );
			}
			while ( req_date2.getMonth() != 9 ) {
				req_date2.addMonth ( 1 );
			}
			year = req_date1.getYear() + 1;
			yeartype = "WYR";
		}
		else if ( outputYearType == YearType.NOV_TO_OCT ) {
			while ( req_date1.getMonth() != 11 ) {
				req_date1.addMonth ( -1 );
			}
			while ( req_date2.getMonth() != 10 ) {
				req_date2.addMonth ( 1 );
			}
			year = req_date1.getYear() + 1;
			yeartype = "IYR";
		}
		else {
			// if ( output_format.equalsIgnoreCase ( "CYR" ))
			while ( req_date1.getMonth() != 1 ) {
				req_date1.addMonth ( -1 );
			}
			while ( req_date2.getMonth() != 12 ) {
				req_date2.addMonth ( 1 );
			}
			year = req_date1.getYear();
			yeartype = "CYR";
		}
		format = "   %2d/%4d  -     %2d/%4d%5.5s" + StringUtil.formatString ( yeartype,"%5.5s");
		// Format for start of line...
		if ( req_interval_base == TimeInterval.MONTH ) {
			initial_format = "%4d %-12.12s";
		}
		else {
			// daily...
			initial_format = "%4d%4d %-12.12s";
		}
	}
	else {
		// Average monthly...
		if ( outputYearType == YearType.WATER ) {
			req_date1.setMonth ( 10 );
			req_date1.setYear ( 0 );
			req_date2.setMonth ( 9 );
			req_date2.setYear ( 1 );
			yeartype = "WYR";
		}
		else if ( outputYearType == YearType.NOV_TO_OCT ) {
			req_date1.setMonth ( 11 );
			req_date1.setYear ( 0 );
			req_date2.setMonth ( 10 );
			req_date2.setYear ( 1 );
			yeartype = "IYR";
		}
		else {
			// if ( output_format.equalsIgnoreCase ( "CYR" ))
			req_date1.setMonth ( 1 );
			req_date1.setYear ( 0 );
			req_date2.setMonth ( 12 );
			req_date2.setYear ( 0 );
			yeartype = "CYR";
		}
		format = "   %2d/   0  -     %2d/   0%5.5s" + StringUtil.formatString(yeartype,"%5.5s");
		initial_format = "     %-12.12s";
	}

	// Write the header line with the period of record...

	v.clear();
	v.add ( new Integer ( req_date1.getMonth()));
	if ( standard_ts ) {
		v.add(new Integer (req_date1.getYear()));
	}
	v.add ( new Integer ( req_date2.getMonth()));
	if ( standard_ts ) {
		v.add(new Integer (req_date2.getYear()));
	}
	v.add ( output_units );
	iline = StringUtil.formatString ( v, format );
	out.println ( iline );

	// Write the data...

	// date is the starting date for each line and is incremented once
	// 	each station's time series has been written for that year
	// cdate is a counter for 12 months' worth of data for each station
	Message.printStatus ( 2, rtn, "Writing time series data for " + req_date1 + " to " + req_date2 );
	DateTime date = new DateTime ( DateTime.PRECISION_MONTH );
	DateTime cdate = new DateTime ( DateTime.PRECISION_MONTH );
	date.setMonth ( req_date1.getMonth());
	date.setYear ( req_date1.getYear());
	int precision = PRECISION_DEFAULT;
	List iline_v = null; // Vector for output lines (objects to be formatted).
	List iline_format_v = null; // Vector for formats for objects.
	int	ndays; // Number of days in a month.
	int	mon, day, j; // counters
	Double DoubleMissingDV = new Double ( MissingDV );

	if ( req_interval_base == TimeInterval.MONTH ) {
		iline_v = new Vector(15,1);
		iline_format_v = new Vector(15,1);
	}
	else {
		// Daily...
		iline_v = new Vector(36,1);
		iline_format_v = new Vector(36,1);
	}

	// Buffer that is used to format each line...

	StringBuffer iline_format_buffer = new StringBuffer();
	
	boolean doSumToPrinted = true; // Current default is for total to sum to printed values, not in-memory

	if ( req_interval_base == TimeInterval.MONTH ) {
		// Monthly data file.  Need to output in the calendar for the
		// file, which results in a little juggling of data...
		// Print one year at a time for each time series

		year--;	// Decrement because the loop increments it
		String units = "";
		// The basic format for data generally includes a . regardless.
		// However, implementation of the .ifm file for the RGDSS has
		// some huge negative numbers where we don't want the period.
		// Check here for the requested format and set accordingly...
		String data_format8 = "%#8.";
		String data_format10 = "%#10.";
		if ( (req_precision > PRECISION_SPECIAL_OFFSET) || (req_precision*-1 > PRECISION_SPECIAL_OFFSET) ) {
			int remainder = req_precision%PRECISION_SPECIAL_OFFSET;
			if ( remainder < 0 ) {
				remainder *= -1;
			}
			if ((remainder & PRECISION_NO_DECIMAL_FOR_LARGE) != 0 ){
				data_format8 = "%8.";
				data_format10 = "%10.";
			}
		}
		// Put together the formats that could be used.  This is faster
		// than reformatting for each number to be written.  Although
		// some will never use, set up the array so that a precision of
		// "i" will be found in array position "i" - this will
		// optimize performance.  These formats are slightly different
		// than the monthly formats - trailing "." is enforced - not sure why?
		String [] format10_for_precision = new String[11];
		String [] format8_for_precision = new String[9];
		for ( int i = 0; i < 11; i++ ) {
			format10_for_precision[i] = data_format10 + i + "f";
		}
		for ( int i = 0; i < 9; i++ ) {
			format8_for_precision[i] = data_format8 + i + "f";
		}
		for ( ; date.lessThanOrEqualTo(req_date2); date.addMonth(12)) {
			year++;
			for ( j = 0; j < nseries; j++ ) {
				cdate.setMonth ( date.getMonth());
				cdate.setYear ( date.getYear());
				// First, clear this string out, then append to
				// it until ready to println to the output file...
				if ( !include_ts[j] ) {
					continue;
				}
				tsptr = (TS)tslist.get(j);
				if ( tsptr.getDataIntervalBase() != req_interval_base ) {
					// We've already warned user above.
					continue;
				}
				if ( req_precision == PRECISION_USE_UNITS ) {
					// Only get the units if we are going to use them...
					units = tsptr.getDataUnits();
				}
				annual_sum = 0;
				annual_count = 0;
				iline_v.clear();
				iline_format_v.clear();
				iline_format_buffer.setLength(0);
				iline_format_buffer.append ( initial_format );
				if ( standard_ts ) {
					iline_v.add( new Integer (year));
					iline_format_v.add (year_format);
				}
				iline_v.add(tsptr.getIdentifier().getLocation());
				iline_format_v.add(id_format);
				
				for (mon=0; mon <12; mon++) {
					value = tsptr.getDataValue (cdate);
					if ( req_precision == PRECISION_USE_UNITS ) {
						precision = getPrecision ( req_precision, 8, value, units);
					}
					else {
						precision = getPrecision ( req_precision, 8,value);
					}
					iline_format_buffer.append ( format8_for_precision[precision] );
					iline_format_v.add(format8_for_precision[precision]);
	
					if (tsptr.isDataMissing (value)) {
						// Missing data so don't add to the annual value.  Print
						// using the same format as for other data...
						iline_v.add ( DoubleMissingDV );
						if ( Message.isDebugOn ) {
							// Wrap to increase performance...
							Message.printWarning ( 20, rtn, "Missing Data Found in TS at "+
							cdate.toString(DateTime.FORMAT_YYYY_MM) + ", printing " + MissingDV );
						}
					}
					else {
						annual_sum += value;
						++annual_count;
						iline_v.add (new Double(value));
					}
					cdate.addMonth(1);
				}
	
				// Add total to output, format and print output line
				if ( req_precision == PRECISION_USE_UNITS ) {
					precision = getPrecision ( req_precision, 10, annual_sum, units );
				}
				else {
					precision = getPrecision ( req_precision, 10, annual_sum );
				}
				iline_format_buffer.append ( format10_for_precision[precision] );
				// Total value at the end of the line...
				iline_v.add ( getLineTotal(tsptr,standard_ts,12,iline_v,iline_format_v,req_interval_base,do_total,
					annual_sum,annual_count,doSumToPrinted));
				iline = StringUtil.formatString (iline_v, iline_format_buffer.toString());
				out.println ( iline );
			}
		}
	}
	else if ( req_interval_base == TimeInterval.DAY ) {
		// Daily format files.  Because the output is always in calendar
		// date and because counts are slightly different, include separate code,
		// rather than trying to merge with monthly output.
		// The outer loop iterates on months...
		int monthly_count = 0;
		double monthly_sum = 0.0;
		// Put together the formats that could be used.  This is faster
		// than reformatting for each number to be written.  Although
		// some will never use, set up the array so that a precision of
		// "i" will be found in array position "i" - this will
		// optimize performance.  These formats are slightly different
		// than the monthly formats - trailing "." is enforced - not sure why?
		String [] format10_for_precision = new String[11];
		String [] format8_for_precision = new String[9];
		for ( int i = 0; i < 11; i++ ) {
			format10_for_precision[i] = "%#10." + i + "f";
		}
		for ( int i = 0; i < 9; i++ ) {
			format8_for_precision[i] = "%#8." + i + "f";
		}
		for ( ; date.lessThanOrEqualTo(req_date2); date.addMonth(1)) {
			for ( j = 0; j < nseries; j++ ) {
				// Set the calendar date for daily data...
				cdate.setMonth ( date.getMonth());
				cdate.setYear ( date.getYear());
				// First, clear this string out, then append to
				// it until ready to println to the output file...
				if ( !include_ts[j] ) {
					continue;
				}
				tsptr = (TS)tslist.get(j);
				if ( tsptr.getDataIntervalBase() != req_interval_base ) {
					// Only output the requested, matching interval.
					continue;
				}
				monthly_sum = 0;
				monthly_count = 0;
				iline_v.clear();
				iline_format_v.clear();
				iline_format_buffer.setLength(0);
				iline_format_buffer.append ( initial_format );
				iline_v.add ( new Integer (cdate.getYear()));
				iline_format_v.add(year_format);
				iline_v.add( new Integer(cdate.getMonth()));
				iline_format_v.add(month_format);
				iline_v.add(tsptr.getIdentifier().getLocation());
				iline_format_v.add(id_format);
	
				// StateMod daily time series contain 31 values for every month (months containing
				// fewer than 31 days use 0s as fillers).
				ndays = TimeUtil.numDaysInMonth ( cdate.getMonth(), cdate.getYear());
				for ( day=1; day <=31; day++) {
					if ( day <= ndays ) {
						cdate.setDay ( day );
						value = tsptr.getDataValue (cdate);
					}
					else {
						// Extra non-existent days up to 31 days...
					    // TODO SAM 2010-02-25 Should this be set to missing?  How does StateMod use it?
						value = 0.0;
					}
					precision = getPrecision ( req_precision,8,value );
					iline_format_buffer.append(format8_for_precision[precision]);
					iline_format_v.add(format8_for_precision[precision]);
					if (tsptr.isDataMissing (value)) {
						// Missing data so don't add to the annual value.  Print
						// using the same format as for other data...
						iline_v.add (DoubleMissingDV );
						if ( Message.isDebugOn ) {
							// Wrap to increase performance...
							Message.printWarning ( 20, rtn, "Missing Data Found in TS at "+
							cdate.toString(DateTime.FORMAT_YYYY_MM_DD) + ", printing " + MissingDV );
						}
					}
					else {
						monthly_sum += value;
						if ( day <= ndays ) {
						    // Don't add to the count for days outside actual days.
						    ++monthly_count;
						}
						iline_v.add (new Double(value));
					}
				}
	
				// Add total onto format line, format, and print
				precision = getPrecision ( req_precision, 10, monthly_sum );
				iline_format_buffer.append (format10_for_precision[precision] );
				// Total value at the end of the line...
				iline_v.add ( getLineTotal(tsptr,standard_ts,ndays,iline_v,iline_format_v,
					req_interval_base,do_total,monthly_sum,monthly_count,doSumToPrinted));
				iline = StringUtil.formatString ( iline_v,iline_format_buffer.toString());
				out.println ( iline );
			}
		}
	}
	// Do not close the files.  They are closed in the calling routine.
}

/**
This routine calls the overloaded writeTimeSeriesList, but in addition,
writes the headers which trace the history of the creation of this file.  
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param infile File (can include path) from which to retrieve history.
@param outfile File (can include path) to write to, including new comments.
@param newcomments Comments, in addition to default comments (which explain the file format), to add to history.
@param tslist A vector of time series (MonthTS objects).
@param date1 Start of period to write.
@param date2 End of period to write.
@param outputYearType output year type
@param MissingDV Value to be printed when missing values are encountered.
@param precision Requested precision of output (number of digits after the decimal
point).  The default is is 2.  This should be set according to the datatype
in calling routines and is not automatically set here.  The full width for time series is 8 characters.
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList ( String infile, String outfile, List<String> newcomments, List<TS> tslist, 
	DateTime date1, DateTime date2, YearType outputYearType, double MissingDV, int precision )
throws Exception
{	List<String> commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String rtn = "StateMod_TS.writeTimeSeriesList";

	Message.printStatus ( 2, rtn, 
		"Writing time series to file \"" + outfile + "\" using \"" + infile + "\" header..." );

	// Process the header from the old file...

	PrintWriter out = null;
	try {
		out = IOUtil.processFileHeaders (
			IOUtil.getPathUsingWorkingDir(infile), IOUtil.getPathUsingWorkingDir(outfile),
				newcomments, commentIndicators, ignoredCommentIndicators, 0 );
		if ( out == null ) {
			Message.printWarning ( 3, rtn, "Error writing time series to \"" + outfile + "\"" );
			throw new Exception ( "Error writing time series to \"" + outfile + "\"" );
		}
	
		//
		// Now write the new data...
		//
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, rtn, "Calling writeTimeSeriesList" );
		}
		writeTimeSeriesList (out, tslist, date1, date2, outputYearType, MissingDV, precision, false );
	}
	finally {
		if ( out != null ) {
			out.flush();
			out.close();
		}
	}
}

/**
Write time series to a StateMod format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param tslist List of time series to write, which must be of the same interval.
@param props Properties of the output, as described in the following table:

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>CalendarType</b></td>
<td>The type of calendar, either "Water" (Oct through Sep);
"NovToOct", or "Calendar" (Jan through Dec), consistent with the YearType enumeration.
</td>
<td>CalenderYear (but may be made sensitive to the data type or units in the future).</td>
</tr>

<tr>
<td><b>InputFile</b></td>
<td>Name of input file (or null if no input file).  If specified, the
file headers of the input file will be transferred to the output file.</td>
</td>
<td>null</td>
</tr>

<tr>
<td><b>MissingDataValue</b></td>
<td>The missing data value to use for output.
</td>
<td>-999</td>
</tr>

<tr>
<td><b>NewComments</b></td>
<td>New comments to add to the header at output.  Set the object in the
PropList to a String[] (the String value part of the property is ignored).
</td>
<td>None</td>
</tr>

<tr>
<td><b>OutputEnd (previously End)</b></td>
<td>Ending date output (YYYY-MM).
</td>
<td>Write all data.</td>
</tr>

<tr>
<td><b>OutputFile</b></td>
<td>Name of output file to write.  The name can be the same as the input
file.  This property must be specified.</td>
</td>
<td>None - must be specified.</td>
</tr>

<tr>
<td><b>OutputPrecision</b></td>
<td>Number of digits after the decimal point on output for data values.  If
positive, the precision is always used.  If negative, then the number of
digits will be <= the absolute value of the requested precision, with less
digits to accommodate large numbers.  This allows some flexibility to fit
large numbers into the standard field widths of StateMod files.
If PRECISION_FROM_UNITS, then the data units are checked to determine
the precision, using the negative precision convention.  For example, if the
units "AF" are to be shown to one digit of precision after the decimal point,
then -1 would be used for the precision.
</td>
<td>2</td>
</tr>

<tr>
<td><b>OutputStart (previously Start)</b></td>
<td>Starting date for output (YYYY-MM).
</td>
<td>Write all data.</td>
</tr>

<tr>
<td><b>PrintGenesis</b></td>
<td>Indicates whether to print time series creation history (true) or not (false).
</td>
<td>false</td>
</tr>

</table>
@throws Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList ( List<TS> tslist, PropList props )
throws Exception
{	String prop_value = null;
	String routine = "StateMod_TS.writeTimeSeriesList";

	// Get the calendar type...

	prop_value = props.getValue ( "CalendarType" );
	if ( prop_value == null ) {
		prop_value = "" + YearType.CALENDAR;
	}
	YearType yearType = YearType.valueOfIgnoreCase(prop_value);
	if ( yearType == null ) {
		yearType = YearType.CALENDAR;
	}

	// Get the input file...

	prop_value = props.getValue ( "InputFile" );
	String infile = null;	// Default
	if ( prop_value != null ) {
		infile = prop_value;
	}

	// Get the missing data value...

	double MissingDV = -999.0;	// Default
	prop_value = props.getValue ( "MissingDataValue" );
	if ( prop_value != null ) {
		MissingDV = StringUtil.atod(prop_value);
	}

	// Get the start of the period...

	DateTime date1 = null;	// Default
	prop_value = props.getValue ( "OutputStart" );
	if ( prop_value == null ) {
		prop_value = props.getValue ( "Start" );
		if ( prop_value != null ) {
			Message.printWarning ( 3, routine, "StateMod write property Start is obsolete.  Use OutputStart.");
		}
	}
	if ( prop_value != null ) {
		try {
			date1 = DateTime.parse ( prop_value.trim() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine,
			"Error parsing starting date \"" + prop_value + "\"." + "  Using null." );
			date1 = null;
		}
	}

	// Get the end of the period...

	DateTime date2 = null;	// Default
	prop_value = props.getValue ( "OutputEnd" );
	if ( prop_value == null ) {
		prop_value = props.getValue ( "End" );
		if ( prop_value != null ) {
			Message.printWarning ( 3, routine, "StateMod write property End is obsolete.  Use OutputEnd.");
		}
	}
	if ( prop_value != null ) {
		try {
			date2 = DateTime.parse ( prop_value.trim() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, "Error parsing ending date \"" + prop_value + "\".  Using null." );
			date2 = null;
		}
	}

	// Get new comments for the file...

	List<String> newComments = null;	// Default
	Object prop_contents = props.getContents ( "NewComments" );
	if ( prop_contents != null ) {
	    if ( prop_contents instanceof List ) {
	        newComments = (List<String>)prop_contents;
	    }
	    else if ( prop_contents instanceof String[] ) {
	        newComments = StringUtil.toList((String [])prop_contents);
	    }
	}

	// Get the output file...

	String outfile = null;	// Default
	prop_value = props.getValue ( "OutputFile" );
	if ( prop_value != null ) {
		outfile = prop_value;
	}

	// Get the output precision...

	prop_value = props.getValue ( "OutputPrecision" );
	int precision = PRECISION_DEFAULT;	// Default
	if ( prop_value != null ) {
		precision = StringUtil.atoi(prop_value);
	}

	// Check to see if we should print genesis information...

	prop_value = props.getValue ( "PrintGenesis" );
	String print_genesis = "false";	// Default
	if ( prop_value != null ) {
		print_genesis = prop_value;
	}
	boolean print_genesis_flag = true;
	if ( print_genesis.equalsIgnoreCase("true")) {
		print_genesis_flag = true;
	}
	else {
		print_genesis_flag = false;
	}

	// Process the header from the old file...

	Message.printStatus ( 2, routine, "Writing new time series to file \"" + outfile + 
		"\" using \"" + infile + "\" header..." );

	PrintWriter out = null;
	try {
		List<String> commentIndicators = new Vector(1);
		commentIndicators.add ( "#" );
		List<String> ignoredCommentIndicators = new Vector(1);
		ignoredCommentIndicators.add ( "#>");
		out = IOUtil.processFileHeaders (
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile),
			newComments, commentIndicators, ignoredCommentIndicators, 0 );
		if ( out == null ) {
			Message.printWarning ( 3, routine, "Error writing time series to \"" + 
			IOUtil.getPathUsingWorkingDir(outfile) + "\"" );
			throw new Exception ( 
			"Error writing time series to \"" + IOUtil.getPathUsingWorkingDir(outfile) + "\"" );
		}
	
		// Now write the new data...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Calling writeTimeSeriesList");
		}
		writeTimeSeriesList ( out, tslist, date1, date2, yearType,
			MissingDV, precision, print_genesis_flag );
	}
	finally {
		if ( out != null ) {
			out.flush();
			out.close();
		}
	}
}

}