// StateCU_BlaneyCriddle - class to hold StateCU Blaney-Criddle crop data, compatible with StateCU KBC file

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Class to hold StateCU Blaney-Criddle crop data for StateCU/StateDMI, compatible with the StateCU KBC file.
*/
public class StateCU_BlaneyCriddle extends StateCU_Data
implements StateCU_ComponentValidator
{

// List data in the same order as in the StateCU documentation...

// Cropn (Crop name) is stored in the base class name.
// kcey (crop number) is stored in the base class ID, if necessary (currently not used).

/**
Growth curve type (Day=perennial crop, Percent=annual crop).
*/
private String __flag = StateCU_Util.MISSING_STRING;

/**
Day of year for annual crop types, null if perennial crop.
*/
private int [] __nckca = null;

/**
Crop coefficient for annual crop types, null if annual crop.
*/
private double [] __ckca = null;

/**
Percent of growing season for perennial crop types.
*/
private int [] __nckcp = null;

/**
Crop coefficient for perennial crop types.
*/
private double [] __ckcp = null;

/**
Flag for modified/original Blaney-Criddle
*/
private int __ktsw = StateCU_Util.MISSING_INT;

/**
Construct a StateCU_BlaneyCriddle instance and set to missing and empty data.
Number of coefficients (as per StateCU this is currently always 25 for perennial
crops and 21 for annual crops, but in the future the number may be made variable).
@param curve_type "Day" for perennial crop (25 coefficients are assumed) or
"Percent" for annual crop (21 coefficients are assumed).
*/
public StateCU_BlaneyCriddle ( String curve_type )
{	super();
	setFlag ( curve_type );
	if ( __flag.equalsIgnoreCase("Percent") ) {
		// Annual crop
		__nckca = new int[21];
		__ckca = new double[21];
		// Default these to simplify setting in DMI and other code...
		for ( int i = 0; i < 21; i++ ) {
			__nckca[i] = i*5;
		}
	}
	else {	// Assume "Day" (perennial crop)
		__nckcp = new int[25];
		__ckcp = new double[25];
		// Default these to simplify setting in DMI and other code...
		__nckcp[0] = 1;
		__nckcp[1] = 15;
		__nckcp[2] = 32;
		__nckcp[3] = 46;
		__nckcp[4] = 60;
		__nckcp[5] = 74;
		__nckcp[6] = 91;
		__nckcp[7] = 105;
		__nckcp[8] = 121;
		__nckcp[9] = 135;
		__nckcp[10] = 152;
		__nckcp[11] = 166;
		__nckcp[12] = 182;
		__nckcp[13] = 196;
		__nckcp[14] = 213;
		__nckcp[15] = 227;
		__nckcp[16] = 244;
		__nckcp[17] = 258;
		__nckcp[18] = 274;
		__nckcp[19] = 288;
		__nckcp[20] = 305;
		__nckcp[21] = 319;
		__nckcp[22] = 335;
		__nckcp[23] = 349;
		__nckcp[24] = 366;
	}
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_BlaneyCriddle)_original)._isClone = false;
	_isClone = true;

	StateCU_BlaneyCriddle bc = (StateCU_BlaneyCriddle)_original;

	if (bc.__ckca != null) {
		__ckca = new double[21];
		__nckca = new int[21];
		for (int i = 0; i < 21; i++) {
			__ckca[i] = bc.__ckca[i];
			__nckca[i] = bc.__nckca[i];
		}
	}
	else {
		__ckcp = new double[25];
		__nckcp = new int[25];
		for (int i = 0; i < 25; i++) {
			__ckcp[i] = bc.__ckcp[i];
			__nckcp[i] = bc.__nckcp[i];
		}
	}	
}

/**
Return the crop coefficients for an annual crop.
@return the crop coefficients for an annual crop.
*/
public double [] getCkca ()
{	return __ckca;
}

public double getCkca(int index) {
	return __ckca[index];
}

/**
Return the crop coefficients for a perennial crop.
@return the crop coefficients for a perennial crop.
*/
public double [] getCkcp ()
{	return __ckcp;
}

public double getCkcp(int index) {
	return __ckcp[index];
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-12 When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Return the growth curve type flag.
@return the growth curve type flag.
*/
public String getFlag ()
{	return __flag;
}

/**
Return the Blaney-Criddle modified/original switch
@return the Blaney-Criddle modified/original switch
*/
public int getKtsw ()
{
	return __ktsw;
}

/**
Return the day of year for an annual crop.
@return the day of year for an annual crop.
*/
public int [] getNckca ()
{	return __nckca;
}

public int getNckca(int index) {
	return __nckca[index];
}

/**
Return the percent of growing season for a perennial crop.
@return the percent of growing season for a perennial crop.
*/
public int [] getNckcp ()
{	return __nckcp;
}

public int getNckcp(int index) {
	return __nckcp[index];
}

/**
Indicate whether the crop is an annual crop (has coefficients specified for percent of growing season) or
perennial crop (has coefficients specified for day of year).
@return the day of year for an annual crop.
*/
public boolean isAnnualCrop ()
{	if ( __nckca != null ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Checks for version 10 by reading the file
and checking the number of fields.  Version 10 has 3 values in record 2.
Version 11+ has 4 values in record 3.  It is assumed that values are separated by whitespace.
@param filename Path to Blaney-Criddle file to check.
@return true if the file is for version 10, false if not.
@throws IOException
 */
private static boolean isVersion_10( String filename ) throws IOException
{
	boolean rVal = false;  // Not version 10
	String fname = filename;
	String line = "";
	BufferedReader input = null;
	int count = 0;  // Record count - interested in third record
	
	// Read the StateCU file.  Only read the first line 
	// This is enough to know if it is version 10
	input = new BufferedReader ( new FileReader (fname));
	while ( (line = input.readLine()) != null ) {
		// check for comments
		if (line.startsWith("#") || line.trim().length()==0 ){
			continue;
		}
		
		// Version 10 has 3 columns in the third row - the newest version has 4 columns
		if( count == 2) {
			List<String> tmp = StringUtil.breakStringList(line, " \t", StringUtil.DELIM_SKIP_BLANKS);
			if(tmp.size() == 3) {
				rVal = true;
			}
			break;
		}
		count++;
	}
	input.close();
	return rVal;
}


/**
Read the StateCU KBC file and return as a Vector of StateCU_BlaneyCriddle.
@param filename filename containing KBC records.
*/
public static List<StateCU_BlaneyCriddle> readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_BlaneyCriddle.readKBCFile";
	String iline = null;
	StateCU_BlaneyCriddle kbc = null;
	List<StateCU_BlaneyCriddle> kbc_Vector = new Vector<StateCU_BlaneyCriddle>(25);
	BufferedReader in = null;
	boolean version10 = isVersion_10( filename );	// Is version 10 (old) format?
	
	Message.printStatus ( 1, rtn, "Reading StateCU KBC file: " + filename );
	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	int nc = -1;
	String title = null; // The title is currently read but not stored since it is never really used for anything.
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}
		if ( title == null ) {
			title = iline;
		}
		else if ( nc < 0 ) {
			// Assume that the line contains the number of crops
			nc = StringUtil.atoi ( iline.trim() );
			break;
		}
	}

	// Now loop through the number of curves...

	// TODO SAM 2007-02-18 Evaluate if needed
	//String id;
	String cropn, flag;
	int npts;
	int [] nckca = null;
	int [] nckcp = null;
	double [] ckc = null;
	String ktsw = null;
	List<String> tokens;
	int j = 0;
	for ( int i = 0; i < nc; i++ ) {
		nckca = null;	// use to check whether annual or perennial below.
		// Read a free format line...

		iline = in.readLine();

		tokens = StringUtil.breakStringList ( iline.trim(), " \t", StringUtil.DELIM_SKIP_BLANKS );
		// TODO SAM 2007-02-18 Evaluate if needed
		//id = (String)tokens.elementAt(0);
		cropn = (String)tokens.get(1);
		flag = (String)tokens.get(2);
		
		if ( version10 ) {
			ktsw = "";
		}
		else {
			ktsw = (String)tokens.get(3);
		}
		// Allocate new StateCU_BlaneyCriddle instance...

		kbc = new StateCU_BlaneyCriddle ( flag );
		kbc.setName ( cropn );
		// TODO SAM 2005-05-22 Ignore the old ID and use the crop name - this facilitates
		// sorting and other standard StateCU_Data features.
		//kbc.setID ( id );
		kbc.setID ( cropn );
		
		if( StringUtil.isInteger(ktsw) ) {
			kbc.setKtsw( StringUtil.atoi(ktsw) );
		}
		
		// Read the coefficients...
		if ( flag.equalsIgnoreCase("Day") ) {
			ckc = kbc.getCkcp();
			nckcp = kbc.getNckcp();
		}
		else {
			ckc = kbc.getCkca();
			nckca = kbc.getNckca();
		}
		npts = ckc.length;

		for ( j = 0; j < npts; j++ ) {
			iline = in.readLine();

			tokens = StringUtil.breakStringList ( iline.trim(), " \t", StringUtil.DELIM_SKIP_BLANKS );
			if ( nckca == null ) {
				// Processing perennial crop...
				nckcp[j] = StringUtil.atoi((String)tokens.get(0) );
				ckc[j] = StringUtil.atod((String)tokens.get(1) );
			}
			else {
				// Processing annual crop...
				nckca[j] = StringUtil.atoi((String)tokens.get(0) );
				ckc[j] = StringUtil.atod((String)tokens.get(1) );
			}
		}

		// add the StateCU_BlaneyCriddle to the vector...
		kbc_Vector.add ( kbc );
	}
	if ( in != null ) {
		in.close();
	}
	return kbc_Vector;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_BlaneyCriddle bc = (StateCU_BlaneyCriddle)_original;
	super.restoreOriginal();

	__flag = bc.__flag;
	__ckca = bc.__ckca;
	__nckca = bc.__nckca;
	__ckcp = bc.__ckcp;
	__nckcp = bc.__nckcp;
	
	_isClone = false;
	_original = null;
}

/**
Set the curve value for the crop coefficient curve (coefficient only).
@param i Index in the curve (zero-index).
For example 1 corresponds to 5% for a percent curve or day 15 for a day curve.
@param coeff Value at the day/percent position.
*/
public void setCurveValue ( int i, double coeff )
{	if ( __flag.equalsIgnoreCase("Percent") ) {
		// Percent of growing season - Annual..
		__ckca[i] = coeff;
	}
	else {
		// Day of year - Perennial...
		__ckcp[i] = coeff;
	}
}

/**
Set the values for the crop coefficient curve (curve position and coefficient).
@param i Index in the curve (zero-index).
For example 1 corresponds to 5% for a percent curve or day 15 for a day curve.
@param pos Position in the curve (day or percent depending on the curve type).
@param coeff Value at the position.
*/
public void setCurveValues ( int i, int pos, double coeff )
{	if ( __flag.equalsIgnoreCase("Percent") ) {
		// Annual..
		__nckca[i] = pos;
		__ckca[i] = coeff;
	}
	else {
		// Perennial...
		__nckcp[i] = pos;
		__ckcp[i] = coeff;
	}
}

/**
Sets the day or percent for a curve value.
@param i the index in the curve (zero-index).
For example 1 corresponds to 5% for a percent curve or day 15 for a day curve.
@param pos the new day or percent to change the index position to.
*/
public void setCurvePosition(int i, int pos) {
	if (__flag.equalsIgnoreCase("Percent")) {
		// Percent of growing season - Annual..
		__nckca[i] = pos;
	}
	else {	
		// Day of year - Perennial...
		__nckcp[i] = pos;
	}
}

/**
Set the growth curve type ("Day" for perennial crop or "Percent" for annual
crop).  This should normally only be called by the constructor.
@param flag Growth curve type.
*/
public void setFlag ( String flag )
{	__flag = flag;
}

/**
Sets the original/modified flag for Blaney-Criddle
@param flag for ktsw 
(0 = SCS Modified, 1 = Original, 2 = Modified with elevation, 
3 = Original with elevation, 4 = Estimating potential ET) 
*/
public void setKtsw( int ktsw )
{
	__ktsw = ktsw;
}

// TODO SAM 2009-05-08 Evaluate whether to allow passing in max coefficient value for check
/**
Performs specific data checks and returns a list of data that failed the data checks.
@param count Index of the data vector currently being checked.
@param dataset StateCU dataset currently in memory.
@param props Extra properties to perform checks with.
@return List of invalid data.
*/
public StateCU_ComponentValidation validateComponent ( StateCU_DataSet dataset ) {
	StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
	String id = getName(); // Name is used for ID because ID used to be numeric
	int [] nckca = getNckca();
	int [] nckcp = getNckcp();
	if ( ((nckca == null) || (nckca.length == 0)) && ((nckcp == null) || (nckcp.length == 0)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id +
			"\" data are neither specified as day of year or percent of season.",
			"Specify coefficients for day of year OR percent of season.") );
	}
	else if ( ((nckca != null) && (nckca.length > 0)) && ((nckcp != null) && (nckcp.length > 0)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id +
			"\" data are specified as day of year and percent of season.",
			"Specify coefficients for day of year OR percent of season.") );
	}
	else if ( isAnnualCrop() ) {
		// Annual - percent of season
		double [] ckca = getCkca();
		for ( int i = 0; i < nckca.length; i++ ) {
			if ( (nckca[i] < 0) || (nckca[i] > 100)) {
				validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" percent of season (" +
					nckca[i] + ") is invalid.", "Specify as 0 to 100.") );
			}
			if ( (ckca[i] < 0) || (ckca[i] > 3.0)) {
				validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" coefficient (" +
					ckca[i] + ") is invalid.", "Specify as 0 to 3.0 (upper limit may vary by location).") );
			}
		}
	}
	else {
		// Perennial - day of year
		double [] ckcp = getCkcp();
		for ( int i = 0; i < nckcp.length; i++ ) {
			if ( (nckcp[i] < 1) || (nckcp[i] > 366)) {
				validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" day of year (" +
					nckcp[i] + ") is invalid.", "Specify as 1 to 366.") );
			}
			if ( (ckcp[i] < 0) || (ckcp[i] > 3.0)) {
				validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" coefficient (" +
					ckcp[i] + ") is invalid.", "Specify as 0 to 3.0 (upper limit may vary by location).") );
			}
		}
	}
	// Check method
	int ktsw = getKtsw();
	if ( (ktsw < 0) || (ktsw > 4)) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" Blaney-Criddle method (" +
			ktsw + ") is invalid.", "Specify as 0 to 4 (refer to StateCU documentation).") );
	}

	return validation;
}

/**
Write a list of StateCU_BlaneyCriddle to a file.  The filename is adjusted to
the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A list of StateCU_BlaneyCriddle to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename, List<StateCU_BlaneyCriddle> data_Vector,
	List<String> new_comments )
throws IOException
{	writeStateCUFile ( filename_prev, filename, data_Vector, new_comments, null );
}

/**
Write a list of StateCU_BlaneyCriddle to a file.  The filename is adjusted to
the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A list of StateCU_BlaneyCriddle to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@param props Properties to control the output.  Currently only the
optional Precision property can be set, indicating how many digits after the
decimal should be printed (default is 3).
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename, List<StateCU_BlaneyCriddle> data_Vector,
	List<String> new_comments, PropList props )
throws IOException
{	List<String> comment_str = new Vector<String>(1);
	comment_str.add ( "#" );
	List<String> ignore_comment_str = new Vector<String>(1);
	ignore_comment_str.add ( "#>" );
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir ( filename_prev );
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
		new_comments, comment_str, ignore_comment_str, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" + full_filename + "\"" );
	}
	writeVector ( data_Vector, out, props );
	out.flush();
	out.close();
	out = null;
}

/**
Write a list of StateCU_BlaneyCriddle to an opened file.
@param data_Vector A Vector of StateCU_BlaneyCriddle to write.
@param out output PrintWriter.
@param props Properties to control the output.  Currently only the
optional Precision property can be set, indicating how many digits after the
decimal should be printed (default is 3).
@exception IOException if an error occurs.
*/
private static void writeVector ( List<StateCU_BlaneyCriddle> data_Vector, PrintWriter out, PropList props )
throws IOException
{	int i,j;
	String cmnt = "#>";
	// Missing data are handled by formatting all as strings (blank if necessary).
	boolean version10 = false;	// Indicate if old Version 10 format is written
	
	if ( props == null ) {
		props = new PropList ( "StateCU_BlaneyCriddle" );
	}
	String Precision = props.getValue ( "Precision" );
	String Version = props.getValue ( "Version" );
	if( Version != null && Version.equals("10") ) {
		// Version 10 is an older version.
		version10 = true;
	}
	
	int Precision_int = 3;
	if ( (Precision != null) && StringUtil.isInteger(Precision) ) {
		Precision_int = StringUtil.atoi ( Precision );
	}

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Blaney-Criddle Crop Coefficient (KBC) File" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record 1 format (a80)" );
	out.println ( cmnt );
	out.println ( cmnt + "  Title     remark:  Title" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record 2 format (free format)" );
	out.println ( cmnt );
	out.println ( cmnt + "  NumCurves     nc:  Number of crop coefficient curves" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record 3 format (free format)" );
	out.println ( cmnt );
	out.println ( cmnt + "  ID            id:  Crop number (not used by StateCU)" );
	out.println ( cmnt + "  CropName   cropn:  Crop name (e.g., ALFALFA)" );
	out.println ( cmnt + "  CurveType   flag:  Growth curve type" );
	out.println ( cmnt + "                     Day = perennial; specify 25 values" );
	out.println ( cmnt + "                           for start, middle, end of month");
	out.println ( cmnt + "                     Percent = annual; specify 21 values" );
	out.println ( cmnt + "                           for 0, 5, ..., 100% of season" );
	out.println ( cmnt );
	if ( !version10 ) {
		// Include newer format information...
		out.println ( cmnt + "  BCMethod    ktsw:  Blaney-Criddle Method" );
		out.println ( cmnt + "                     0 = SCS Modified Blaney-Criddle" );
		out.println ( cmnt + "                     1 = Original Blaney-Criddle" );
		out.println ( cmnt + "                     2 = Modifed Blaney-Criddle w/ Elev. Adj.");
		out.println ( cmnt + "                     3 = Original Blaney-Criddle w/ Elev. Adj.");
		out.println ( cmnt + "                     4 = Pochop");
		out.println ( cmnt );
	}
	out.println ( cmnt + "  Record 4 format (free format)" );
	out.println ( cmnt );
	out.println ( cmnt + "Position     nckca:  Percent (0 to 100) of growing season for annual crop");
	out.println ( cmnt + "             nckcp:  Day of year (1 to 366) for perennial crop" );
	out.println ( cmnt + "Coeff         ckca:  Crop coefficient for annual crop" );
	out.println ( cmnt + "         OR   ckcp:  Crop coefficient for perennial crop");
	out.println ( cmnt );
	out.println ( cmnt + "Title..." );
	out.println ( cmnt + "NumCurves" );
	out.println ( cmnt + "ID CropName CurveType" );
	out.println ( cmnt + "Position Coeff" );
	out.println ( cmnt + "----------------------------" );
	out.println ( cmnt + "EndHeader" );
	out.println ( "Crop Coefficient Curves for Blaney-Criddle" );

	int num = 0;
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	out.println ( num );
	StateCU_BlaneyCriddle kbc = null;
	int [] nckca = null;
	int [] nckcp = null;
	double [] ckca = null;
	double [] ckcp = null;
	int size = 0;
	String value_format = "%9." + Precision_int + "f";
	for ( i=0; i<num; i++ ) {
		kbc = (StateCU_BlaneyCriddle)data_Vector.get(i);
		if ( kbc == null ) {
			continue;
		}

		// Just get all the data.  Null arrays are used as a check
		// below to know what data to output...
		nckca = kbc.getNckca();
		nckcp = kbc.getNckcp();
		ckca = kbc.getCkca();
		ckcp = kbc.getCkcp();
		
		// Do not truncate the name to 20 characters if version 10 because
		// doing so may result in arbitrary cut of the current crop names and
		// result in different output from old anyhow.
		String name = kbc.getName();
		// Since free format, the ID must always have something.  If
		// we don't know, put -999...
		String id = "" + (i + 1);  // Default to sequential number
		if ( version10 ) {
			// Previously used -999
			id = "-999";
		}
		if ( !StateCU_Util.isMissing(kbc.getID()) ) {
			// Changes elsewhere impact this so also use -999 unless it is a number
			if ( StringUtil.isInteger(kbc.getID())) {
				id = "" + kbc.getID();
			}
			else {
				id = "-999";
			}
			// Can't use the crop name because StateCU expects a number (?)
			//id = kbc.getID();
		}
		// Output based on the version because file comparisons may be done when verifying files.
		if ( version10 ) {
			// No ktsw...
		    out.println ( id + " " + name + " " + kbc.getFlag() );
		}
		else {
			// With ktsw, but OK if blank.
			out.println ( id + " " + name + " " + kbc.getFlag() + " " + kbc.getKtsw() );
		}
		
		if ( nckca != null ) {
			size = nckca.length;
		}
		else {
			size = nckcp.length;
		}
		for ( j = 0; j < size; j++ ) {
			if ( nckca != null ) {
				// Print annual curve (Percent)...
				out.println (
				StringUtil.formatString(nckca[j],"%-3d") + StringUtil.formatString(ckca[j],value_format) );
			}
			else {
				// Print perennial curve (Day)...
				out.println ( StringUtil.formatString((int)nckcp[j],"%-3d") +
				StringUtil.formatString(ckcp[j],value_format) );
			}
		}
	}
}

/**
Writes a list of StateCU_BlaneyCriddle objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the Vector of objects to write.  
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List<StateCU_BlaneyCriddle> data,
	List<String> outputComments ) 
throws Exception
{	String routine = "StateCU_BlaneyCriddle.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("Name");
	fields.add("CurveType");
	fields.add("DayPercent");
	fields.add("Coefficient");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_BLANEY_CRIDDLE;
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
	PrintWriter out = null;
	StateCU_BlaneyCriddle bc = null;
	List<String> commentString = new Vector<String>(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new Vector<String>(1);
	ignoreCommentString.add ( "#>" );
	String[] line = new String[fieldCount];
	String flag = null;
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( outputComments == null ) {
			newComments2 = new Vector<String>();
		}
		else {
			newComments2 = new Vector<String>(outputComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU Blaney-Criddle crop coefficients as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			bc = (StateCU_BlaneyCriddle)data.get(i);
			flag = bc.getFlag();
			if (flag.equalsIgnoreCase("Percent")) {
				for (j = 0; j < 21; j++) {
					line[0] = StringUtil.formatString(bc.getName(), formats[0]).trim();
					line[1] = StringUtil.formatString(bc.getFlag(), formats[1]).trim();
					line[2] = StringUtil.formatString(bc.getNckca(j), formats[2]).trim();
					line[3] = StringUtil.formatString(bc.getCkca(j), formats[3]).trim();

					buffer = new StringBuffer();	
					for (k = 0; k < fieldCount; k++) {
						if (k > 0) {
							buffer.append(delimiter);
						}
						if (line[k].indexOf(delimiter) > -1) {
							line[k] = "\"" + line[k] + "\"";
						}
						buffer.append(line[k]);
					}	
					out.println(buffer.toString());
				}
			}
			else {
				for (j = 0; j < 25; j++) {
					line[0] = StringUtil.formatString(bc.getName(), formats[0]).trim();
					line[1] = StringUtil.formatString(bc.getFlag(), formats[1]).trim();
					line[2] = StringUtil.formatString(bc.getNckcp(j), formats[2]).trim();
					line[3] = StringUtil.formatString(bc.getCkcp(j),formats[3]).trim();

					buffer = new StringBuffer();	
					for (k = 0; k < fieldCount; k++) {
						if (k > 0) {
							buffer.append(delimiter);
						}
						if (line[k].indexOf(delimiter) > -1) {
							line[k] = "\"" + line[k] + "\"";
						}
						buffer.append(line[k]);
					}	
					out.println(buffer.toString());
				}			
			}
		}
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
		out = null;
	}
}

}
