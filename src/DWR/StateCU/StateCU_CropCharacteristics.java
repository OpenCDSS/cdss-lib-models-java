//------------------------------------------------------------------------------
// StateCU_CropCharacteristics - class to hold CU crop characteristics data,
//			compatible with StateCU CCH file
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2002-11-14	Steven A. Malers, RTi	Copy CULocation class and update for
//					the CCH file contents.
// 2003-02-19	SAM, RTi		Change so any missing data are printed
//					as blanks.
// 2003-06-04	SAM, RTi		Rename class from CUCropCharacteristics
//					to StateCU_CropCharacteristics.
//					Change read/write methods to not use
//					file extension in method name.
// 2003-07-02	SAM, RTi		Change so that the identifier and name
//					are the same.  The old crop number is
//					no longer used but there is no name
//					field.
// 2005-01-17	J. Thomas Sapienza, RTi	* Added createBackup().
//					* Added restoreOriginal().
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2007-01-05   KAT, RTi		updates to the format
// 							Old Format ver. 10 ("x" is a space): 
//  						(a20,2(1x,i2,2x,i2),2x,i2,2x,2(i4,1x),3(f3,0,1x),
//  						4(f4.1,1x),2(i2,1x),i3,1x,i2)
// 							New Format (spaces are included):
//  						(a30,10(i6),4(f6.1),4(i5))
// 2007-01-29	SAM, RTi		Review KAT's code.  Clean code based on Eclipse
//					information.
// 2007-03-04	SAM, RTi		Some final code cleanup - formats were not quite
//							in agreement with StateCU documentation.
// 2007-03-19	SAM, RTi		Write the crop number as a sequential integer.
// 2007-04-22	SAM, RTi		Minor change to fix extra EndHeader comment in
//					output.  Add AutoAdjust to write method properties.
//------------------------------------------------------------------------------

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
import RTi.Util.Time.TimeUtil;

/**
Class to hold StateCU crop characteristics data for StateCU/StateDMI, compatible
with the StateCU CCH file.  The method names correspond exactly to CCH variable
names as of StateCU Version 10 documentation.
*/
public class StateCU_CropCharacteristics extends StateCU_Data implements StateCU_ComponentValidator
{

// List data in the same order as in the StateCU documentation...

// Cropn (Crop name) is stored in the base class name.
// kcey (crop number) is stored in the base class ID, if necessary (currently not used).

/**
Planting month.
*/
private int __gdate1 = StateCU_Util.MISSING_INT;

/**
Planting day.
*/
private int __gdate2 = StateCU_Util.MISSING_INT;

/**
Harvest month.
*/
private int __gdate3 = StateCU_Util.MISSING_INT;

/**
Harvest day.
*/
private int __gdate4 = StateCU_Util.MISSING_INT;

/**
Days to full cover.
*/
private int __gdate5 = StateCU_Util.MISSING_INT;

/**
Length of season.
*/
private int __gdates = StateCU_Util.MISSING_INT;

/**
Temperature early moisture (F).
*/
private double __tmois1 = StateCU_Util.MISSING_DOUBLE;

/**
Temperature late moisture (F).
*/
private double __tmois2 = StateCU_Util.MISSING_DOUBLE;

/**
Management allowable deficit level.
*/
private double __mad = StateCU_Util.MISSING_DOUBLE;

/**
Initial root zone depth (IN).
*/
private double __irx = StateCU_Util.MISSING_DOUBLE;

/**
Maximum root zone depth (IN).
*/
private double __frx = StateCU_Util.MISSING_DOUBLE;

/**
Available water holding capacity (IN?, IN/IN?).
*/
private double __awc = StateCU_Util.MISSING_DOUBLE;

/**
Maximum application depth (IN).
*/
private double __apd = StateCU_Util.MISSING_DOUBLE;

/**
Spring frost date flag (0=mean, 1=28F, 2=32F).
*/
private int __tflg1 = StateCU_Util.MISSING_INT;

/**
Fall frost date flag (0=mean, 1=28F, 2=32F).
*/
private int __tflg2 = StateCU_Util.MISSING_INT;

/**
Days between 1st and 2nd cut.
*/
private int __cut2 = StateCU_Util.MISSING_INT;

/**
Days between 2nd and 3rd cut.
*/
private int __cut3 = StateCU_Util.MISSING_INT;

/**
Construct a StateCU_CropCharacteristics instance and set to missing and empty data.
*/
public StateCU_CropCharacteristics()
{	super();
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_CropCharacteristics)_original)._isClone = false;
	_isClone = true;
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	super.finalize();
}

/**
Return the maximum application depth (IN).
@return the maximum application depth (IN).
*/
public double getApd()
{	return __apd;
}

/**
Return the available water holding capacity (AWC).
@return the available water holding capacity (AWC).
*/
public double getAwc()
{	return __awc;
}

/**
Return the days between 1st and 2nd cuts for *ALFALFA*.
@return the days between 1st and 2nd cuts for *ALFALFA*.
*/
public int getCut2()
{	return __cut2;
}

/**
Return the days between 2nd and 3rd cuts for *ALFALFA*.
@return the days between 2nd and 3rd cuts for *ALFALFA*.
*/
public int getCut3()
{	return __cut3;
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
Return the maximum root zone depth (FRX).
@return the maximum root zone depth (FRX).
*/
public double getFrx()
{	return __frx;
}

/**
Return the planting month.
@return the planting month.
*/
public int getGdate1()
{	return __gdate1;
}

/**
Return the planting day.
@return the planting day.
*/
public int getGdate2()
{	return __gdate2;
}

/**
Return the harvest month.
@return the harvest month.
*/
public int getGdate3()
{	return __gdate3;
}

/**
Return the harvest day.
@return the harvest day.
*/
public int getGdate4()
{	return __gdate4;
}

/**
Return the days to full cover.
@return the days to full cover.
*/
public int getGdate5()
{	return __gdate5;
}

/**
Return the length of the season.
@return the length of the season.
*/
public int getGdates()
{	return __gdates;
}

/**
Return the initial root zone depth (IRX).
@return the initial root zone depth (IRX).
*/
public double getIrx()
{	return __irx;
}

/**
Return the management allowable deficit (MAD).
@return the management allowable deficit (MAD).
*/
public double getMad()
{	return __mad;
}

/**
Return the spring frost date flag.
@return the spring frost date flag.
*/
public int getTflg1()
{	return __tflg1;
}

/**
Return the fall frost date flag.
@return the fall frost date flag.
*/
public int getTflg2()
{	return __tflg2;
}

/**
Return the temperature early moisture (F).
@return the temperature early moisture (F).
*/
public double getTmois1()
{	return __tmois1;
}

/**
Return the temperature late moisture (F).
@return the temperature late moisture (F).
*/
public double getTmois2()
{	return __tmois2;
}

/**
Checks for version 10 by reading the file and checking the record length. 
Version 10 records are < 103 characters and version 11+ are >= 103.  This
is actually a compromise given the old format was 94 and the new is 134,
according to the StateCU documentation.
@param filename File to check.
@return true if version 10 or false if version 11+.
@throws IOException 
*/
private static boolean isVersion_10( String filename ) throws IOException
{
	boolean rVal = false;
	String fname = filename;
	String line = "";
	BufferedReader input = null;
	
	// Read the StateCU file.  Only read the first line 
	// This is enough to know if it is version 10
	input = new BufferedReader ( new FileReader (fname));
	while ( (line = input.readLine()) != null ) {
		// check for comments
		if (line.startsWith("#") || line.trim().length()==0 ){
			continue;
		}
		
		if(line.length() < 103) {
			rVal = true;
			break;
		}
	}
	input.close();
	return rVal;
}


/**
Read the StateCU CCH file and return as a Vector of StateCU_CropCharacteristics.
@param filename filename containing CCH records.
*/
public static List readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_CropCharacteristics.readStateCUFile";
	String iline = null;
	List v = new Vector ( 18 );
	List cch_Vector = new Vector ( 100 );	// Data to return.
	// Don't read the crop number...
	int v10_format_0[] = {
				StringUtil.TYPE_STRING,		// cname
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// gdate1
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// gdate2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// gdate3
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// gdate4
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// gdate5
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// gdates
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// tmois1
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// tmois2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// mad
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// irx
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// frx
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// awc
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// apd
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// tflg1
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// tflg2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,		// cut2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING };	// cut3

	// New format for version 11+
	int format_0[] = {
			StringUtil.TYPE_STRING,		// cname
			StringUtil.TYPE_SPACE,		// skip over ckey
			StringUtil.TYPE_STRING,		// gdate1
			StringUtil.TYPE_STRING,		// gdate2
			StringUtil.TYPE_STRING,		// gdate3
			StringUtil.TYPE_STRING,		// gdate4
			StringUtil.TYPE_STRING,		// gdate5
			StringUtil.TYPE_STRING,		// gdates
			StringUtil.TYPE_STRING,		// tmois1
			StringUtil.TYPE_STRING,		// tmois2
			StringUtil.TYPE_STRING,		// mad
			StringUtil.TYPE_STRING,		// irx
			StringUtil.TYPE_STRING,		// frx
			StringUtil.TYPE_STRING,		// awc
			StringUtil.TYPE_STRING,		// apd
			StringUtil.TYPE_STRING,		// tflg1
			StringUtil.TYPE_STRING,		// tflg2
			StringUtil.TYPE_STRING,		// cut2
			StringUtil.TYPE_STRING };	// cut3
	
	int v10_format_Ow[] = {
			20,	// cname
			5,
			2,	// gdate1
			1,
			2,	// gdate2
			2,
			2,	// gdate3
			2,
			2,	// gdate4
			2,
			4,	// gdate5
			1,
			4,	// gdates
			1,
			3,	// tmois1
			1,
			3,	// tmois2
			1,
			3,	// mad
			1,
			4,	// irx
			1,
			4,	// frx
			1,
			4,	// awc
			1,
			4,	// apd
			1,
			2,	// tflg1
			1,
			2,	// tflg2
			1,
			3,	// cut2
			1,
			2	// cut3
			};
	
	// New format for version 11+
	int format_0w[] = {
			30,	// cname
			6,	// ckey - crop number, not used
			6,	// gdate1 
			6,	// gdate2
			6,	// gdate3
			6,	// gdate4
			6,	// gdate5
			6,	// gdates
			6,	// tmois1
			6,	// tmois2
			6,	// mad
			6,	// irx
			6,	// frx
			6,	// awc
			6,	// apd
			6,	// tflg1
			6,	// tflg2
			6,	// cut2
			6	// cut3
			};
	
	// set the version based on the length of this field
	// Version 10 uses 20 characters and newer versions use 30
	if( isVersion_10( filename ) )
	{
		Message.printStatus(2, rtn, "Format of file was found to be" +
				" version 10.  Will use old format for reading.");
		// Reset formats to old...
		format_0w = v10_format_Ow;
		format_0 = v10_format_0;
	}
	StateCU_CropCharacteristics cch = null;
	BufferedReader in = null;
	
	Message.printStatus ( 2, rtn, "Reading StateCU CCH file: " + filename );
	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	String string;
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}

		// allocate new StateCU_CropCharacteristics instance...
		cch = new StateCU_CropCharacteristics();
		StringUtil.fixedRead ( iline, format_0, format_0w, v );
		cch.setName ( ((String)v.get(0)).trim() ); 
		// Set the ID the same as the name for data management purposes.
		// Maybe later a true name field will be added.
		cch.setID ( ((String)v.get(0)).trim() );
		// Crop key is not even read so continue with index 1...
		string = ((String)v.get(1)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setGdate1 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(2)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setGdate2 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(3)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setGdate3 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(4)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setGdate4 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(5)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setGdate5 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(6)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setGdates ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(7)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setTmois1 ( StringUtil.atod(string) );
		}
		string = ((String)v.get(8)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setTmois2 ( StringUtil.atod(string) );
		}
		string = ((String)v.get(9)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setMad ( StringUtil.atod(string) );
		}
		string = ((String)v.get(10)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setIrx ( StringUtil.atod(string) );
		}
		string = ((String)v.get(11)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setFrx ( StringUtil.atod(string) );
		}
		string = ((String)v.get(12)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setAwc ( StringUtil.atod(string) );
		}
		string = ((String)v.get(13)).trim();
		if ( (string.length() != 0) && StringUtil.isDouble(string) ) {
			cch.setApd ( StringUtil.atod(string) );
		}
		string = ((String)v.get(14)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setTflg1 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(15)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setTflg2 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(16)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setCut2 ( StringUtil.atoi(string) );
		}
		string = ((String)v.get(17)).trim();
		if ( (string.length() != 0) && StringUtil.isInteger(string) ) {
			cch.setCut3 ( StringUtil.atoi(string) );
		}

		// add the StateCU_CropCharacteristics to the vector...
		cch_Vector.add ( cch );
	}
	if ( in != null ) {
		in.close();
	}
	return cch_Vector;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_CropCharacteristics chars = (StateCU_CropCharacteristics)_original;
	super.restoreOriginal();

	__apd = chars.__apd;
	__awc = chars.__awc;
	__cut2 = chars.__cut2;
	__cut3 = chars.__cut3;
	__frx = chars.__frx;
	__gdate1 = chars.__gdate1;
	__gdate2 = chars.__gdate2;
	__gdate3 = chars.__gdate3;
	__gdate4 = chars.__gdate4;
	__gdate5 = chars.__gdate5;
	__gdates = chars.__gdates;
	__irx = chars.__irx;
	__mad = chars.__mad;
	__tflg1 = chars.__tflg1;
	__tflg2 = chars.__tflg2;
	__tmois1 = chars.__tmois1;
	__tmois2 = chars.__tmois2;
	
	_isClone = false;
	_original = null;
}

/**
Set the maximum application depth (IN).
@param apd maximum application depth (IN).
*/
public void setApd ( double apd )
{	__apd = apd;
}

/**
Set the available water holding capacity.
@param awc available water holding capacity.
*/
public void setAwc ( double awc )
{	__awc = awc;
}

/**
Set the number of days between 1st and 2nd cut (*ALFALFA*).
@param cut2 number of days between 1st and 2nd cut (*ALFALFA*).
*/
public void setCut2 ( int cut2 )
{	__cut2 = cut2;
}

/**
Set the number of days between 2nd and 3rd cut (*ALFALFA*).
@param cut3 number of days between 2nd and 3rd cut (*ALFALFA*).
*/
public void setCut3 ( int cut3 )
{	__cut3 = cut3;
}

/**
Set the maximum root zone depth (IN).
@param frx maximum root zone depth (IN).
*/
public void setFrx ( double frx )
{	__frx = frx;
}

/**
Set the planting month.
@param gdate1 Planting month (1-12).
*/
public void setGdate1 ( int gdate1 )
{	__gdate1 = gdate1;
}

/**
Set the planting day.
@param gdate2 Planting day (1-12).
*/
public void setGdate2 ( int gdate2 )
{	__gdate2 = gdate2;
}

/**
Set the harvest month.
@param gdate3 Harvest month (1-12).
*/
public void setGdate3 ( int gdate3 )
{	__gdate3 = gdate3;
}

/**
Set the harvest day.
@param gdate4 Harvest day (1-12).
*/
public void setGdate4 ( int gdate4 )
{	__gdate4 = gdate4;
}

/**
Set the days to full cover.
@param gdate5 Days to full cover.
*/
public void setGdate5 ( int gdate5 )
{	__gdate5 = gdate5;
}

/**
Set the length of season.
@param gdates Length of season (days).
*/
public void setGdates ( int gdates )
{	__gdates = gdates;
}

/**
Set the initial root zone depth (IN).
@param irx initial root zone depth (IN).
*/
public void setIrx ( double irx )
{	__irx = irx;
}

/**
Set the management allowable deficit (MAD).
@param mad management allowable deficit (MAD).
*/
public void setMad ( double mad )
{	__mad = mad;
}

/**
Set the spring frost date flag (0=mean, 1=28F, 2=32F).
@param tflg1 spring frost date flag (0=mean, 1=28F, 2=32F).
*/
public void setTflg1 ( int tflg1 )
{	__tflg1 = tflg1;
}

/**
Set the fall frost date flag (0=mean, 1=28F, 2=32F).
@param tflg2 fall frost date flag (0=mean, 1=28F, 2=32F).
*/
public void setTflg2 ( int tflg2 )
{	__tflg2 = tflg2;
}

/**
Set the temperature early moisture.
@param tmois1 Temperature early moisture (F).
*/
public void setTmois1 ( double tmois1 )
{	__tmois1 = tmois1;
}

/**
Set the temperature late moisture.
@param tmois2 Temperature late moisture (F).
*/
public void setTmois2 ( double tmois2 )
{	__tmois2 = tmois2;
}

/**
Performs specific data checks and returns a list of data that failed the data checks.
For now don't check for missing data individually - just check for invalid data.
@param dataset StateCU dataset currently in memory.
@return Validation results.
 */
public StateCU_ComponentValidation validateComponent ( StateCU_DataSet dataset )
{
	StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
	String id = getName(); // Name is used for ID because ID used to be numeric
	// Crop number (not used by StateCU - not checked)
	int gdate1 = getGdate1();
	int gdate2 = getGdate2();
	int gdate3 = getGdate3();
	int gdate4 = getGdate4();
	int gdate5 = getGdate5();
	int gdates = getGdates();
	// TODO SAM 2009-05-05 Evaluate whether day check should use month
	if ( !TimeUtil.isValidMonth(gdate1) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" planting month (" +
			gdate1 + ") is invalid.", "Specify a month 1-12.") );
	}
	if ( !TimeUtil.isValidDay(gdate2) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" planting day (" +
			gdate2 + ") is invalid.", "Specify a day 1-31.") );
	}
	if ( !TimeUtil.isValidMonth(gdate3) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" harvest month (" +
			gdate3 + ") is invalid.", "Specify a month 1-12.") );
	}
	if ( !TimeUtil.isValidDay(gdate4) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" planting day (" +
			gdate4 + ") is invalid.", "Specify a day 1-31.") );
	}
	if ( (gdate5 < 0) || (gdate5 > 365) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" days to full cover (" +
			gdate5 + ") is invalid.", "Specify a day 0 - 365.") );
	}
	if ( (gdates <= 0) || (gdate5 > 365) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" days in season (" +
			gdates + ") is invalid.", "Specify days 1 - 365.") );
	}
	double tmois1 = getTmois1();
	double tmois2 = getTmois2();
	// Somewhat arbitrary
	if ( (tmois1 < 0) || (tmois1 > 100) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" temperature early moisture (" +
			tmois1 + ") is invalid.", "Specify degrees F.") );
	}
	if ( (tmois2 < 0) || (tmois2 > 100) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" temperature late moisture (" +
			tmois2 + ") is invalid.", "Specify degrees F.") );
	}
	// Management allowable deficit (not used by StateCU - not checked)
	// Initial root zone depth (not used by StateCU - not checked)
	double frx = getFrx();
	if ( frx <= 0 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" maximum root zone depth (" +
			frx + ") is invalid.", "Specify inches > 0.") );
	}
	// AWC often missing so don't check
	// Application depth
	double apd = getApd();
	// Somewhat arbitrary
	if ( (apd < 0) || (apd > 100) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" maximum application depth (" +
			apd + ") is invalid.", "Specify inches > 0.") );
	}
	int tflg1 = getTflg1();
	int tflg2 = getTflg2();
	if ( (tflg1 < 0) || (tflg1 > 2) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" spring frost flag (" +
			tflg1 + ") is invalid.", "Specify 0, 1, 2.") );
	}
	if ( (tflg2 < 0) || (tflg2 > 2) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" fall frost flag (" +
			tflg2 + ") is invalid.", "Specify 0, 1, 2.") );
	}
	int cut2 = getCut2();
	int cut3 = getCut3();
	if ( cut2 > 365 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" days to 2nd cut (" +
			cut2 + ") is invalid.", "Specify days < 365.") );
	}
	if ( cut3 > 365 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" days to 3rd cut (" +
			cut3 + ") is invalid.", "Specify days < 365.") );
	}
	return validation;
}

/**
Write a list of StateCU_CropCharacteristics to a file.  The filename is
adjusted to the working directory if necessary using IOUtil.getPathUsingWorkingDir()
@param filename_prev
@param filename
@param data_Vector
@param new_comments
@throws IOException
 */
public static void writeStateCUFile ( String filename_prev, String filename,
	List data_Vector, List new_comments )
throws IOException
{	writeStateCUFile ( filename_prev, filename, data_Vector, new_comments, null );
}

/**
Write a list of StateCU_CropCharacteristics to a file.  The filename is adjusted to
the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A list of StateCU_CropCharacteristics to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@param props Properties to control output.
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>        <td><b>Description</b></td>     <td><b>Default</
b></td>
</tr>

<tr>
<td><b>AutoAdjust</b></td>
<td><b>If "true", then if Version 10 format, strip off "." and trailing text from crop names.</b>
<td>None - write current format.</td>
</tr>

<tr>
<td><b>Version</b></td>
<td><b>If "10", write StateCU Version 10 format file.  Otherwise, write the current format.</b>
<td>None - write current format.</td>
</tr>
</table>
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename,
		List data_Vector, List new_comments, PropList props )
throws IOException
{	List comment_str = new Vector(1);
	comment_str.add ( "#" );
	List ignore_comment_str = new Vector(1);
	ignore_comment_str.add ( "#>" );
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir (filename_prev );
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
Write a list of StateCU_CropCharacteristics to an opened file.
@param data_Vector A list of StateCU_CropCharacteristics to write.
@param out output PrintWriter.
@param props Properties to control output (see writeStateCUFile).
@exception IOException if an error occurs.
*/
private static void writeVector ( List data_Vector, PrintWriter out, PropList props )
throws IOException
{	int i;
	String iline;
	String rtn = "StateCU_CropCharacterstics.writeVector";
	String cmnt = "#>";
	// For header comments...
	String rec_format = "  Record format (a30,10(i6),4(f6.1),4(i5))";
	// format used to write the file
	String format = "%-30.30s%6.6s%6.6s%6.6s%6.6s%6.6s" +
		"%6.6s%6.6s%6.6s%6.6s%6.6s" +
		"%6.6s%6.6s%6.6s%6.6s%5.5s%5.5s%5.5s%5.5s";
	String crop_num_format = "%6d";
	String date_format  =  "%6d";
	String date_format2 =  "%6d";
	String temp_format  =  "%6d";
	String float_format =  "%6.1f";
	String last_format  =  "%5d";
	
	// check proplist for version
	boolean version_10 = false;  // Default is to use current 11+ version format
	if ( props == null ) {
		props = new PropList ( "StateCU_CropCharacteristics" );
	}
	String Version = props.getValue ( "Version" );
	// set the format to version 10 format
	if ( (Version != null) && Version.equalsIgnoreCase("10") ) {
		version_10 = true;
	}
	String AutoAdjust = props.getValue ( "AutoAdjust" );
	// AutoAdjust output based on version
	boolean AutoAdjust_boolean = false;
	if ( (AutoAdjust != null) && AutoAdjust.equalsIgnoreCase("true") ) {
		AutoAdjust_boolean = true;
	}
	if ( version_10 ) {
		// format written in the header (record format)
		rec_format = "  Record format " +
		"(a20,2(1x,i2,2x,i2),2x,i2,2x,2(i4,1x)," +
		"3(f3.0,1x),4(f4.1,1x),2(i2,1x),i3,1x,i2)";
		// format for writing the file
		format = "%-20.20s %2.2s  %2.2s %2.2s  %2.2s  %2.2s  " +
		"%4.4s %4.4s %3.3s %3.3s %3.3s " +
		"%4.4s %4.4s %4.4s %4.4s %2.2s %2.2s %3.3s %2.2s";
		date_format = "%2d";
		date_format2 = "%4d";
		temp_format = "%3.0f";
		float_format = "%4.1f";
		last_format = "%2d";
		Message.printStatus(2, rtn, "Writing file in Version 10 format.");
	}
	
	List v = new Vector(19);	// Reuse for all output lines.
	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Crop Characteristics (CCH) File" );
	out.println ( cmnt );
	out.println ( cmnt + rec_format);
	out.println ( cmnt );
	out.println ( cmnt + "  CropName   cropn:  Crop name (e.g., ALFALFA)" );
	out.println ( cmnt + "  CropNum     ckey:  Crop number (not used in StateCU - written as sequential number)" );
	out.println ( cmnt + "Plant MM    gdate1:  Planting month (1-12)" );
	out.println ( cmnt + "Plant DD    gdate2:  Planting day (e.g., 1-31)");
	out.println ( cmnt + "Harvest MM  gdate3:  Harvest month (1-12)" );
	out.println ( cmnt + "Harvest DD  gdate4:  Harvest day (1-31)" );
	out.println ( cmnt + "To Full     gdate5:  Days to full cover" );
	out.println ( cmnt + "Days Seas   gdates:  Length of season (days)" );
	out.println ( cmnt + "Ear Tem     tmois1:  Temperature early moisture (F)" );
	out.println ( cmnt + "Lat Tem     tmois2:  Temperature late moisture (F)" );
	out.println ( cmnt + "MAD            mad:  Mananagement allowable deficit (not used)" );
	out.println ( cmnt + "Init Dep       irx:  Initial root zone depth (in, not used)" );
	out.println ( cmnt + "Max Dep        frx:  Maximum root zone depth (in)" );
	out.println ( cmnt + "AWC            awc:  Available water holding capacity (in)" );
	out.println ( cmnt + "                     OVERWRITTEN IF .par IS SUPPLIED" );
	out.println ( cmnt + "App Dep        apd:  Maximum application depth (in)" );
	out.println ( cmnt + "Frost Sp     tflg1:  Spring frost date flag" );
	out.println ( cmnt + "                     0 = mean, 1 = 28F, 2 = 32F" );
	out.println ( cmnt + "Frost Fa     tflg2:  Fall frost date flag" );
	out.println ( cmnt + "                     0 = mean, 1 = 28F, 2 = 32F" );
	out.println ( cmnt + "Days to 2nd   cut2:  Days between 1st and 2nd cuttings" );
	out.println ( cmnt + "                     cropn = *ALFALFA* only" );
	out.println ( cmnt + "Days to 3rd   cut3:  Days between 2nd and 3rd cuttings" );
	out.println ( cmnt + "                     cropn = *ALFALFA* only" );
	out.println ( cmnt );
	
	if ( version_10 ) {
		out.println ( cmnt + "                       Plant  Harvest To   Days Ear Lat     Init Max       App  Frost Days to" );
		out.println ( cmnt + "     CropName          MM DD  MM  DD  Full Seas Tem Tem MAD Dep  Dep  AWC  Dep  Sp Fa 2nd 3rd" );
		out.println ( cmnt + "-----------------exbexxbexbexxbexxbexxb--exb--exb-exb-exb-exb--exb--exb--exb--exbexbexb-exbe" );
	}
	else {
		out.println ( cmnt + "                             Crop     Plant      Harvest    To   Days   Ear   Lat         Init  Max         App    Frost   Days to" );
		out.println ( cmnt + "            CropName         Num    MM    DD    MM    DD   Full  Seas   Tem   Tem   MAD   Dep   Dep   AWC   Dep   Sp   Fa  2nd  3rd" );
		out.println ( cmnt + "---------------------------eb----eb----eb----eb----eb----eb----eb----eb----eb----eb----eb----eb----eb----eb----eb---eb---eb---eb---e" );
	}
	out.println ( cmnt + "EndHeader" );
	
	int num = 0;
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	StateCU_CropCharacteristics cch = null;
	String name = null;	// Crop name
	for ( i=0; i<num; i++ ) {
		cch = (StateCU_CropCharacteristics)data_Vector.get(i);
		if ( cch == null ) {
			continue;
		}

		v.clear();
		int pos = 0;	// Position of object
		name = cch.getName();
		if ( version_10 && AutoAdjust_boolean ) {
			pos = name.indexOf(".");
			if ( pos > 0 ) {
			    name = name.substring(0,pos);
			}
		}
		v.add(name);
		// Crop number is not used by model
		// Write as sequential integer to facilitate free-format reading.
		v.add(StringUtil.formatString((i + 1), crop_num_format));

		// Planting...
		if ( StateCU_Util.isMissing(cch.__gdate1) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__gdate1, date_format));
		}
		if ( StateCU_Util.isMissing(cch.__gdate2) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__gdate2, date_format));
		}
		// Harvest...
		if ( StateCU_Util.isMissing(cch.__gdate3) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__gdate3, date_format));
		}
		if ( StateCU_Util.isMissing(cch.__gdate4) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__gdate4, date_format));
		}
		// Days to cover...
		if ( StateCU_Util.isMissing(cch.__gdate5) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__gdate5, date_format2));
		}
		// Length of season...
		if ( StateCU_Util.isMissing(cch.__gdates) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__gdates, date_format2));
		}
		// Moisture...
		if ( StateCU_Util.isMissing(cch.__tmois1) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__tmois1, temp_format));
		}
		if ( StateCU_Util.isMissing(cch.__tmois2) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__tmois2, temp_format));
		}
		// MAD...
		if ( StateCU_Util.isMissing(cch.__mad) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__mad, temp_format));
		}
		// Root depths...
		if ( StateCU_Util.isMissing(cch.__irx) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__irx, float_format));
		}
		if ( StateCU_Util.isMissing(cch.__frx) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__frx, float_format));
		}
		if ( StateCU_Util.isMissing(cch.__awc) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__awc, float_format));
		}
		if ( StateCU_Util.isMissing(cch.__apd) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__apd, float_format));
		}
		// Frost date flags (check for -90 because the original data
		// may be read in as -99 because it does not strictly comply with formatting)...
		if ( StateCU_Util.isMissing(cch.__tflg1) || (cch.__tflg1 < -90.0) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__tflg1, last_format));
		}
		if ( StateCU_Util.isMissing(cch.__tflg2) || (cch.__tflg2 < -90.0) ) {
			v.add("-999");
		}
		else {
			v.add(StringUtil.formatString(cch.__tflg2, last_format));
		}
		// Additional cuttings (ALFALFA only)...
		if ( StateCU_Util.isMissing(cch.__cut2) || (StringUtil.indexOfIgnoreCase(cch._name,"ALFALFA",0) < 0) ) {
			v.add("");
		}
		else {
			v.add(StringUtil.formatString(cch.__cut2, last_format));
		}
		if ( StateCU_Util.isMissing(cch.__cut3) || (StringUtil.indexOfIgnoreCase(cch._name,"ALFALFA",0) < 0) ) {
			v.add("");
		}
		else {
			v.add(StringUtil.formatString(cch.__cut3, last_format));
		}	
		iline = StringUtil.formatString ( v, format);
		out.println ( iline );
	}
}

/**
Writes a list of StateCU_CropCharacteristics objects to a list file.  A header
is printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.  
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List data,
	List outputComments ) 
throws Exception {
	String routine = "StateCU_CropCharacteristics.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List fields = new Vector();
	fields.add("Name");
	fields.add("PlantingMonth");
	fields.add("PlantingDay");
	fields.add("HarvestMonth");
	fields.add("HarvestDay");
	fields.add("DaysToCover");
	fields.add("SeasonLength");
	fields.add("EarlyMoisture");
	fields.add("LateMoisture");
	fields.add("DeficitLevel");
	fields.add("InitialRootZone");
	fields.add("MaxRootZone");
	fields.add("AWC");
	fields.add("MAD");
	fields.add("SpringFrost");
	fields.add("FallFrost");
	fields.add("DaysBetween1And2");
	fields.add("DaysBetween2And3");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_CROP_CHARACTERISTICS;
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
	PrintWriter out = null;
	StateCU_CropCharacteristics cc = null;
	List commentString = new Vector(1);
	commentString.add ( "#" );
	List ignoreCommentString = new Vector(1);
	ignoreCommentString.add ( "#>" );
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List newComments2 = null;
		if ( outputComments == null ) {
			newComments2 = new Vector();
		}
		else {
			newComments2 = new Vector(outputComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU crop characteristics as a delimited list file.");
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
			cc = (StateCU_CropCharacteristics)data.get(i);
			
			line[0] = StringUtil.formatString(cc.getName(),formats[0]).trim();
			line[1] = StringUtil.formatString(cc.getGdate1(),formats[1]).trim();
			line[2] = StringUtil.formatString(cc.getGdate2(),formats[2]).trim();
			line[3] = StringUtil.formatString(cc.getGdate3(),formats[3]).trim();
			line[4] = StringUtil.formatString(cc.getGdate4(),formats[4]).trim();
			line[5] = StringUtil.formatString(cc.getGdate5(),formats[5]).trim();
			line[6] = StringUtil.formatString(cc.getGdates(),formats[6]).trim();
			line[7] = StringUtil.formatString(cc.getTmois1(),formats[7]).trim();
			line[8] = StringUtil.formatString(cc.getTmois2(),formats[8]).trim();
			line[9] = StringUtil.formatString(cc.getMad(),formats[9]).trim();
			line[10] = StringUtil.formatString(cc.getIrx(),formats[10]).trim();
			line[11] = StringUtil.formatString(cc.getFrx(),formats[11]).trim();
			line[12] = StringUtil.formatString(cc.getAwc(),formats[12]).trim();
			line[13] = StringUtil.formatString(cc.getApd(),formats[13]).trim();
			line[14] = StringUtil.formatString(cc.getTflg1(),formats[14]).trim();
			line[15] = StringUtil.formatString(cc.getTflg2(),formats[15]).trim();
			line[16] = StringUtil.formatString(cc.getCut2(),formats[16]).trim();
			line[17] = StringUtil.formatString(cc.getCut3(),formats[17]).trim();

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if (j > 0) {
					buffer.append(delimiter);
				}
				if (line[j].indexOf(delimiter) > -1) {
					line[j] = "\"" + line[j] + "\"";
				}
				buffer.append(line[j]);
			}

			out.println(buffer.toString());
		}
		out.flush();
		out.close();
		out = null;
	}
	catch (Exception e) {
		Message.printWarning ( 3, routine, e );
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