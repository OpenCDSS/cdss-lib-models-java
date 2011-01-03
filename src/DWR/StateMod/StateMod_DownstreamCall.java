package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;

import RTi.TS.DayTS;
import RTi.TS.TSUtil;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
This class stores downstream call data.  Currently the class provides a read method because a single daily
time series is read from the file.  This is experimental data for operating rule type 23 and more enhancements
may be needed in the future.
*/
public class StateMod_DownstreamCall //extends StateMod_Data 
//implements Cloneable, Comparable
{

/**
Read return information in and store in a list.
@param filename filename for data file to read
@throws Exception if an error occurs
*/
public static DayTS readStateModFile(String filename )
throws Exception
{	String routine = "StateMod_DownstreamCall.readStateModFile";
	String iline = null;
	List<String> vData = new Vector(4);
	int linecount = 0;
	
	BufferedReader in = null;

	Message.printStatus(2, routine, "Reading downstream call file: " + filename);
	int size = 0;
	int errorCount = 0;
	int year, month, day;
	double adminNumber;
	DateTime date = new DateTime(DateTime.PRECISION_DAY);
	DayTS ts = null;
	try {	
		in = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(filename)));
		boolean headerRead = false;
		while ((iline = in.readLine()) != null) {
			++linecount;
			// check for comments
			if (iline.startsWith("#") || (iline.trim().length()== 0) ) {
				// Special dynamic header comments written by software and blank lines - no need to keep
				continue;
			}
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line: " + iline);
			}
			if ( !headerRead ) {
				// Read the header line and create the time series.
				// This code copied from StateMod_TS.readTimeSeriesList()
				String format = "i5x1i4x5i5x1i4s5s5";
				List<Object>v = StringUtil.fixedRead ( iline, format );
				int m1 = ((Integer)v.get(0)).intValue();
				int y1 = ((Integer)v.get(1)).intValue();
				int m2 = ((Integer)v.get(2)).intValue();
				int y2 = ((Integer)v.get(3)).intValue();
				DateTime date1Header;
				date1Header = new DateTime ( DateTime.PRECISION_DAY );
				date1Header.setYear ( y1 );
				date1Header.setMonth ( m1 );
				date1Header.setDay ( 1 );
				DateTime date2Header;
				date2Header = new DateTime ( DateTime.PRECISION_DAY );
				date2Header.setYear ( y2 );
				date2Header.setMonth ( m2 );
				date2Header.setDay ( TimeUtil.numDaysInMonth(m2,y2) );
				String units = ((String)v.get(4)).trim();
				String yeartypes = ((String)v.get(5)).trim();
				// TODO SAM 2011-01-02 Year type is not used since year month and day are all calendar
				/*
				int yeartype = StateMod_DataSet.SM_CYR;
				// Year type is used in one place to initialize the year when
				// transferring data.  However, it is assumed that m1 is always correct for the year type.
				if ( yeartypes.equalsIgnoreCase("WYR") ) {
					yeartype = StateMod_DataSet.SM_WYR;
				}
				else if ( yeartypes.equalsIgnoreCase("IYR") ) {
					yeartype = StateMod_DataSet.SM_IYR;
				}
				*/
				// year that are specified are used to set the period.
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, "Parsed m1=" + m1 + " y1=" +
					y1 + " m2=" + m2 + " y2=" + y2 + " units=\"" + units + "\" yeartype=\"" + yeartypes + "\"" );
				}
				// Now create the time series
				String tsidentString = "OprType23..DownstreamCall.Day";
				ts = (DayTS)TSUtil.newTimeSeries(tsidentString, true);
				ts.setDate1(date1Header);
				ts.setDate1Original(date1Header);
				ts.setDate2(date2Header);
				ts.setDate2Original(date2Header);
				ts.setDataUnits(units);
				ts.setDataUnitsOriginal(units);
				ts.allocateDataSpace();
			}
			else {
				// A time series data line
				// Break the line using whitespace, while allowing for quoted strings...
				try {
					vData = StringUtil.breakStringList (
						iline, " \t", StringUtil.DELIM_ALLOW_STRINGS|StringUtil.DELIM_SKIP_BLANKS );
					size = 0;
					if ( vData != null ) {
						size = vData.size();
					}
					if ( size < 3 ) {
						Message.printStatus ( 2, routine, "Ignoring line " + linecount +
						" not enough data values.  Have " + size + " expecting 3" );
						++errorCount;
						continue;
					}
					// Uncomment if testing...
					//Message.printStatus ( 2, routine, "" + v );
		
					// Allocate new plan node and set the values
					year = Integer.parseInt(vData.get(0).trim());
					month = Integer.parseInt(vData.get(1).trim());
					day = Integer.parseInt(vData.get(2).trim());
					adminNumber = Double.parseDouble(vData.get(3).trim());
					date.setYear(year);
					date.setMonth(month);
					date.setDay(day);
					ts.setDataValue(date, adminNumber);
				}
				catch ( Exception e2 ) {
					Message.printWarning(3, routine, "Error reading line " + linecount + " \"" + iline +
						"\" (" + e2 + ") - skipping line." );
					Message.printWarning(3, routine, e2);
				}
			}

			// Set the data to not dirty because it was just initialized...

			ts.setDirty ( false );
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, "Error reading line " + linecount + " \"" + iline + "\" (" + e + ")." );
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	if ( errorCount > 0 ) {
		throw new Exception ( "There were " + errorCount + " errors processing the data - refer to log file." );
	}
	// Return the single time series.
	return ts;
}

}