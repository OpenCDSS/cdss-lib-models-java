//------------------------------------------------------------------------------
// StateCU_DelayTable - Contains delay table I/O.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2004-03-17	Steven A. Malers, RTi	Copy StateMod_DelayTable and update to
//					have only the static readStateCUFile()
//					and writeCUFile() methods.
// 2004-03-31	SAM, RTi		Fix bug where delay values were written
//					on one line instead of 12 per line.
// 2005-04-19	J. Thomas Sapienza, RTi	Enabled readStateCUFile().
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------

package DWR.StateCU;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_DelayTable;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class reads and writes StateCU delay table files.  The StateCU and StateMod
file formats are similar.  Consequently, rather than redefining data members
here, the StateMod delay tables should be used.  This class simply provides
read and write methods for StateCU file formats.
*/
public abstract class StateCU_DelayTable
{

/** 
Read delay information from a StateCU file and store in a java vector.  The new
delay entries are added to the end of the previously stored delays.  Returns the delay table information.
@return a Vector of StateMod_DelayTable
@param filename the filename to read from.
@param interv The control file interv parameter.  +n indicates the number of
values in each delay pattern.  -1 indicates variable number of values with
values as percent (0-100).  -100 indicates variable number of values with values as fraction (0-1).
*/
public static List readStateCUFile ( String filename, int interv )
throws Exception
{
	boolean is_monthly = true;
	String routine = "StateCU_DelayTable.readStateCUFile";
	String iline;
	List theDelays = new Vector(1);
	StateMod_DelayTable aDelay = new StateMod_DelayTable ( is_monthly );
	int num_read=0, total_num_to_read=0;
	boolean reading=false; 
	BufferedReader in = null;
	StringTokenizer split = null;

	if (Message.isDebugOn) {
		Message.printDebug(10, routine, "in readStateModFile reading file: " + filename);
	}
	try {	
		in = new BufferedReader(new FileReader(filename));
		while ((iline = in.readLine()) != null) {
			// check for comments
			iline = iline.trim();
			if (iline.startsWith("#") || iline.length()==0) {
				continue;
			}

			split = new StringTokenizer(iline);
			if ((split == null) || (split.countTokens()== 0)) {
				continue;
			}

			if (!reading) {
				// allocate new delay node
				aDelay = new StateMod_DelayTable ( is_monthly );
				num_read = 0;
				reading = true;
				theDelays.add(aDelay);
				aDelay.setTableID(split.nextToken());

				if (interv < 0) {
					aDelay.setNdly(split.nextToken());
				}
				else {	
					aDelay.setNdly(interv);
				}
				total_num_to_read = aDelay.getNdly();
				// Set the delay table units(default is percent)...
				aDelay.setUnits("PCT");
				if (interv == -100) {
					aDelay.setUnits("FRACTION");
				} 
			}

			while (split.hasMoreTokens()) {
				aDelay.addRet_val(split.nextToken());
				num_read++;
			}
			if (num_read >= total_num_to_read) {
				reading = false;
			}
		}
	} catch (Exception e) {
		Message.printWarning(2, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	return theDelays;
}

/**
Write the new (updated) delay table file.  This routine writes the new delay
table file.  If an original file is specified, then the original header is
carried into the new file.  The writing of data is done by the dumpDelayFile
routine which now does not mess with headers.
@param inputFile old file (used as input)
@param outputFile new file to create
@param dly list of delays
@param newcomments new comments to save with the header of the file
@throws Exception if an error occurs
*/
public static void writeStateCUFile(String inputFile, String outputFile, List dly, List newcomments )
throws Exception
{	PrintWriter	out = null;
	List commentStr = new Vector(1);
	commentStr.add ( "#" );
	List ignoreCommentStr = new Vector(1);
	ignoreCommentStr.add ( "#>" );
	String routine = "StateMod_DelayTable.writeStateCUFile";

	Message.printStatus(2, routine, 
		"Writing new delay table to file \"" + outputFile + "\" using \"" + inputFile + "\" header...");

	try {	
		// Process the header from the old file...
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(inputFile),
			IOUtil.getPathUsingWorkingDir(outputFile),
			newcomments, commentStr, ignoreCommentStr, 0);
	
		// Now write the new data...
		String cmnt="#>";
		String m_format = "%8.2f";
		StateMod_DelayTable delay = null;
	
		out.println(cmnt);
		out.println(cmnt + " *******************************************************");
		out.println(cmnt + " StateCU Delay (Return flow) Table");
		out.println(cmnt);
		out.println(cmnt + "     Format (a8, i4, (12f8.2)");
		out.println(cmnt);
		out.println(cmnt + "   ID       idly: Delay table id");
		out.println(cmnt + "   Ndly     ndly: Number of entries in delay table idly.");
		out.println(cmnt + "   Ret  dlyrat(1-n,idl): Return for month n, station idl");
		out.println(cmnt);
		out.println(cmnt + " ID   Ndly  Ret1    Ret2    Ret3    Ret4    Ret5    Ret6  " +
			"  Ret7    Ret8    Ret9    Ret10   Ret11   Ret12...");
		out.println(cmnt + "-----eb--eb------eb------eb------eb------eb------eb------e" +
			"b------eb------eb------eb------eb------eb------e..." );
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int ndly = 0;
		if (dly != null) {
			ndly = dly.size();
		}
		if (Message.isDebugOn) {
			Message.printDebug(3, routine, "Printing " + ndly + " delay table entries.");
		}
	
		StringBuffer b = new StringBuffer();
		int j = 0;	// Index for returns in a table
		int nvals = 0; // Number of returns in a table
		boolean printed; // Indicates if a line of output was printed, to help handle 12 values or less per line
		for (int i = 0; i < ndly; i++) {
			delay = (StateMod_DelayTable)dly.get(i);
			b.setLength(0);
			b.append ( StringUtil.formatString(delay.getTableID(), "%8d"));
			b.append ( StringUtil.formatString(delay.getNdly(), "%4d") );
			nvals = delay.getNdly();
			printed = false;
			for ( j = 0; j < nvals; j++ ) {
				b.append ( StringUtil.formatString( delay.getRet_val(j), m_format) );
				printed = false;
				if ( ((j + 1)%12) == 0 ) {
					// Print the output and initialize a new line...
					out.println(b.toString());
					b.setLength(0);
					b.append ( "            " );
					printed = true;
				}
			}
			if ( !printed ) {
				// Print the last line of output...
				out.println(b.toString());
			}
		}
	} 
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		throw e;
	}
	finally {
		if (out != null) {
			out.close();
		}
	}
}

}