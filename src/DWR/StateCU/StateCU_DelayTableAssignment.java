// StateCU_DelayTableAssignment - class to hold StateCU delay table assignment data for StateCU,
// compatible with the StateCU DLA file.

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

package DWR.StateCU;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Class to hold StateCU delay table assignment data for StateCU, compatible with the StateCU DLA file.
*/
public class StateCU_DelayTableAssignment extends StateCU_Data
implements StateCU_ComponentValidator
{

/**
Number of stations.
*/
private String[] __delay_table_ids = null;
private double[] __delay_table_percents = null;

/**
Construct a StateCU_DelayTableAssignment instance and set to missing and empty data.
*/
public StateCU_DelayTableAssignment()
{	super();
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_DelayTableAssignment)_original)._isClone = false;
	_isClone = true;

	int num = ((StateCU_DelayTableAssignment)_original).getNumDelayTables();

	StateCU_DelayTableAssignment dta = (StateCU_DelayTableAssignment)_original;

	if (dta.__delay_table_ids != null) {
		__delay_table_ids = new String[num];
		for (int i = 0; i < num; i++) {
			__delay_table_ids[i] = dta.getDelayTableID(i);
		}
	}

	if (dta.__delay_table_percents != null) {
		__delay_table_percents = new double[num];
		for (int i = 0; i < num; i++) {
			__delay_table_percents[i] = dta.getDelayTablePercent(i);
		}
	}
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__delay_table_ids = null;
	__delay_table_percents = null;
	super.finalize();
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-12 
	// When specific checks are added to checkComponentData return the header for that data here
	return new String[] {};
}

/**
Return the delay table identifier.
@return the delay table identifier or an empty string if the position is invalid.
@param pos Delay table index (relative to zero).
*/
public String getDelayTableID ( int pos )
{	if ( __delay_table_ids == null ) {
		return "";
	}
	if ( (pos >= 0) && (pos < __delay_table_ids.length) ) {
		return __delay_table_ids[pos];
	}
	else {
		return "";
	}
}

/**
Return the delay table percent.
@return the delay table percent or 0.0 if the position is invalid.
@param pos Delay table index (relative to zero).
*/
public double getDelayTablePercent ( int pos )
{	if ( __delay_table_percents == null ) {
		return 0.0;
	}
	if ( (pos >= 0) && (pos < __delay_table_percents.length) ) {
		return __delay_table_percents[pos];
	}
	else {
		return 0.0;
	}
}

/**
Return the number of delay tables that are used.
@return the number of delay tables that are used.
*/
public int getNumDelayTables ()
{	if ( __delay_table_ids == null ) {
		return 0;
	}
	else {
		return __delay_table_ids.length;
	}
}

/**
Read the StateCU delay table assignment file and return as a list of StateCU_DelayTableAssignment.
@param filename filename containing delay table assignment records.
*/
public static List<StateCU_DelayTableAssignment> readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_DelayTableAssignment.readStateCUFile";
	String iline = null;
	List<Object> v = new Vector<Object> ( 8 );
	List<StateCU_DelayTableAssignment> data_Vector = new Vector<StateCU_DelayTableAssignment> ( 100 );	// Data to return.
	int i;
	int format_0[] = {
		StringUtil.TYPE_STRING,	// CU Location
		StringUtil.TYPE_STRING};// Num delays
	int format_0w[] = {
		12,	// CU Location
		2 };	// Num delays
	// The following used to iteratively read the end of each record.
	int format_1[] = {
		StringUtil.TYPE_STRING,	// Percent
		StringUtil.TYPE_STRING};// Table ID
	int format_1w[] = {
		8,	// Percent
		8 }; // Table ID

	StateCU_DelayTableAssignment data = null;
	BufferedReader in = null;
	Message.printStatus ( 1, rtn, "Reading StateCU delay table assignment file: " + filename );

	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	String num_delay_tables, percent;
	int ndt = 0;
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}

		// Allocate new DelayTableAssignment instance...
		data = new StateCU_DelayTableAssignment();

		StringUtil.fixedRead ( iline, format_0, format_0w, v );
		data.setID ( ((String)v.get(0)).trim() ); 
		num_delay_tables = ((String)v.get(1)).trim();
		if ( (num_delay_tables.length() != 0) && StringUtil.isInteger(num_delay_tables)) {
			data.setNumDelayTables ( StringUtil.atoi(num_delay_tables) );
		}
		ndt = data.getNumDelayTables();
		for ( i = 0; i < ndt; i++ ) {
			StringUtil.fixedRead ( iline.substring(14 + i*16), format_1, format_1w, v );
			percent = ((String)v.get(0)).trim();
			if ( (percent.length() != 0) && StringUtil.isDouble(percent)) {
				data.setDelayTablePercent ( StringUtil.atod(percent), i );
			}
			data.setDelayTableID ( ((String)v.get(1)).trim(), i ); 
		}

		// Add the StateCU_DelayTableAssignment to the vector...
		data_Vector.add ( data );
	}
	if ( in != null ) {
		in.close();
	}
	return data_Vector;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_DelayTableAssignment dta = (StateCU_DelayTableAssignment)_original;
	super.restoreOriginal();

	__delay_table_percents = dta.__delay_table_percents;
	__delay_table_ids = dta.__delay_table_ids;

	_isClone = false;
	_original = null;
}

/**
Set the delay table identifier.
@param id Delay table identifier.
@param pos Delay table index (relative to zero).
*/
public void setDelayTableID ( String id, int pos )
{	__delay_table_ids[pos] = id;
}

/**
Set the number of delay tables.  The data arrays are reallocated.
@param num_delay_tables Number of delay tables.
*/
public void setNumDelayTables ( int num_delay_tables )
{	__delay_table_ids = new String[num_delay_tables];
	__delay_table_percents = new double[num_delay_tables];
}

/**
Set the delay table percentage.
@param percent delay table percentage.
@param pos Station index (relative to zero).
*/
public void setDelayTablePercent ( double percent, int pos )
{	__delay_table_percents[pos] = percent;
}

/**
Performs specific data checks and returns a list of data that failed the data checks.
@param dataset StateCU dataset currently in memory.
@return validation results.
*/
public StateCU_ComponentValidation validateComponent( StateCU_DataSet dataset ) {
	// TODO KAT 2007-04-12 Add specific checks here ...
	return null;
}

/**
Write a list of StateCU_DelayTableAssignment to a file.  The filename is
adjusted to the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A Vector of StateCU_DelayTableAssignment to write.
@param newComments Comments to add to the top of the file.  Specify as null if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename,
	List<StateCU_DelayTableAssignment> data_Vector, List<String> newComments )
throws IOException
{	List<String> commentStr = new Vector<String>(1);
	commentStr.add ( "#" );
	List<String> ignoreCommentStr = new Vector<String>(1);
	ignoreCommentStr.add ( "#>" );
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir ( filename_prev );
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
		newComments, commentStr, ignoreCommentStr, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" + full_filename + "\"" );
	}
	writeVector ( data_Vector, out );
	out.flush();
	out.close();
	out = null;
}

/**
Write a list of StateCU_DelayTableAssignment to an opened file.
@param data_Vector A list of StateCU_DelayTableAssignment to write.
@param out output PrintWriter.
@exception IOException if an error occurs.
*/
private static void writeVector ( List<StateCU_DelayTableAssignment> data_Vector, PrintWriter out )
throws IOException
{	int i,j;
	String cmnt = "#>";
	// Missing data handled by formatting as a string...
	StateCU_DelayTableAssignment data = null;

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Delay Table Assignment (DLA) File" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record format (a12,i2,20(f8.2,i8))");
	out.println ( cmnt );
	out.println ( cmnt + "  ID              :  CU Location identifier" );
	out.println ( cmnt + "  ND              :  Number of delay tables" );
	out.println ( cmnt + "  Pct             :  Percent of flow that uses the delay table" );
	out.println ( cmnt + "  DTID            :  Delay table identifier" );
	out.println ( cmnt );
	out.println ( cmnt + "    ID    ND   Pct   DTID" );
	out.println ( cmnt + "---------ebeb------eb------eb------eb------eb------e..." );
	out.println ( cmnt + "EndHeader" );

	int num = 0;
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	int ndt = 0;
	StringBuffer b = new StringBuffer();
	for ( i=0; i<num; i++ ) {
		data = data_Vector.get(i);
		if ( data == null ) {
			continue;
		}

		b.setLength(0);
		b.append( StringUtil.formatString(data.getID(),"%-12.12s"));
		ndt = data.getNumDelayTables();
		b.append( StringUtil.formatString(ndt,"%2d"));
		for ( j = 0; j < ndt; j++ ) {
			b.append ( StringUtil.formatString( data.getDelayTablePercent(j), "%8.2f" ) );
			b.append ( StringUtil.formatString( StringUtil.atoi( data.getDelayTableID(j)),"%8d"));
		}
		out.println ( b.toString() );
	}
}

/**
Writes a list of StateCU_DelayTableAssignment objects to a list file.  A 
header is printed to the top of the file, containing the commands used to 
generate the file.  Any strings in the body of the file that contain the field 
delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the Vector of objects to write.
@param newComments comments to add to the top of the file (e.g., command file and HydroBase version).
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List<StateCU_DelayTableAssignment> data, List<String> newComments) 
throws Exception {
	String routine = "StateCU_DelayTableAssignment.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("ID");
	fields.add("DelayTableID");
	fields.add("Percent");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY;
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
	
	int j = 0;
	int k = 0;
	int num = 0;
	PrintWriter out = null;
	StateCU_DelayTableAssignment dly = null;
	List<String> commentString = new Vector<String>(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new Vector<String>(1);
	ignoreCommentString.add ("#>");
	String[] line = new String[fieldCount];
	String id = null;
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector<String>();
		}
		else {
			newComments2 = new Vector<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU location delay table assignment information as a delimited list file.");
		newComments2.add(2,"See also the associated CU location file.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			dly = (StateCU_DelayTableAssignment)data.get(i);
			
			num = dly.getNumDelayTables();
			id = dly.getID();

			for (j = 0; j < num; j++) {
				line[0] = StringUtil.formatString(id,formats[0]).trim();
				line[1] = StringUtil.formatString(dly.getDelayTableID(j),formats[1]).trim();
				line[2] = StringUtil.formatString(dly.getDelayTablePercent(j),formats[2]).trim();

				buffer = new StringBuffer();	
				for (k = 0; k < fieldCount; k++) {
					if (line[k].indexOf(delimiter) > -1) {
						line[k] = "\"" + line[k] + "\"";
					}
					if (k > 0) {
						buffer.append(delimiter);
					}
					buffer.append(line[k]);
				}
	
				out.println(buffer.toString());
			}
		}
		out.flush();
		out.close();
		out = null;
	}
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

}
