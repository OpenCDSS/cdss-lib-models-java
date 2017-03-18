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
Class to hold StateCU Penman-Monteith crop data for StateCU/StateDMI, compatible
with the StateCU KPM file.
*/
public class StateCU_PenmanMonteith extends StateCU_Data
implements StateCU_ComponentValidator
{

// List data in the same order as in the StateCU documentation...

// Cropn (Crop name) is stored in the base class name.
// id (crop number) is stored in the base class ID, if necessary (currently not used).

/**
Number of growth stages (number of 0, 10, ..., 90, 100 percent sequences in coefficients).
This is implied during read from the crop name.
*/
private int __nGrowthStages = StateCU_Util.MISSING_INT;

/**
Time from start of growth to effective cover (%) or % of growth stage after effective cover.
*/
private double [][] __kcday = null;

/**
Crop coefficient for each stage.
*/
private double [][] __kcb = null;

/**
Construct a StateCU_PenmanMonteith instance and set to missing and empty data.
@param nGrowthStage the number of growth stages.
*/
public StateCU_PenmanMonteith ( int nGrowthStage )
{	super();
	__nGrowthStages = nGrowthStage;
	int ncpgs = getNCoefficientsPerGrowthStage();
	// Allocate the arrays based on the number of growth stages
	__kcday = new double[nGrowthStage][];
	__kcb = new double[nGrowthStage][];
	for ( int igs = 0; igs < nGrowthStage; igs++ ) {
		__kcday[igs] = new double[ncpgs];
		__kcb[igs] = new double[ncpgs];
		// Default these to simplify setting in DMI and other code...
		for ( int i = 0; i < ncpgs; i++ ) {
			__kcday[igs][i] = i*100.0/(ncpgs - 1);
			__kcb[igs][i] = StateCU_Util.MISSING_DOUBLE;
		}
	}
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_PenmanMonteith)_original)._isClone = false;
	_isClone = true;

	StateCU_PenmanMonteith pm = (StateCU_PenmanMonteith)_original;
	
	__nGrowthStages = pm.__nGrowthStages;
	int ncpgs = getNCoefficientsPerGrowthStage();
	__kcday = new double[__nGrowthStages][];
	__kcb = new double[__nGrowthStages][];
	for ( int igs = 0; igs < __nGrowthStages; igs++ ) {
		__kcday[igs] = new double[ncpgs];
		__kcb[igs] = new double[ncpgs];
		// Default these to simplify setting in DMI and other code...
		for ( int i = 0; i < ncpgs; i++ ) {
			__kcday[igs][i] = pm.__kcday[igs][i];
			__kcb[igs][i] = pm.__kcb[igs][i];
		}
	}
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__kcb = null;
	__kcday = null;
	super.finalize();
}

/**
Return the crop coefficients.
@return the crop coefficients.
*/
public double [][] getKcb ()
{	return __kcb;
}

/**
Return the crop coefficient for the growth stage and index.
@param igs growth stage (0+ index)
@param index index in growth stage (0+)
@return the crop coefficient for the growth stage and index.
*/
public double getKcb(int igs, int index)
{
	return __kcb[igs][index];
}

/**
Return the crop coefficients time percent.
@return the crop coefficients time percent.
*/
public double [][] getKcday ()
{	return __kcday;
}

/**
Return the crop coefficient time percent for the growth stage and index.
@param igs growth stage (0+ index)
@param index index in growth stage (0+)
@return the crop coefficient time percent for the growth stage and index.
*/
public double getKcday(int igs, int index)
{
	return __kcday[igs][index];
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
Return the number of coefficients per growth stages - currently always 11.
@return the number of coefficients per growth stages.
*/
public static int getNCoefficientsPerGrowthStage ()
{	return 11;
}

/**
Return the number of growth stages.
@return the number of growth stages.
*/
public int getNGrowthStages ()
{	return __nGrowthStages;
}

/**
Return the number of growth stages (3 if the crop name contains ALFALFA, 1 if it
contains GRASS and PASTURE, and 2 otherwise).
@param cropName the crop name.
@return the number of growth stages for the crop.
*/
public static int getNGrowthStagesFromCropName ( String cropName )
{	cropName = cropName.toUpperCase();
	if ( cropName.indexOf("ALFALFA") >= 0 ) {
		return 3;
	}
	else if ( (cropName.indexOf("GRASS") >= 0) && (cropName.indexOf("PASTURE") >= 0) ) {
		return 1;
	}
	else {
		return 2;
	}
}

/**
Read the StateCU KPM file and return as a list of StateCU_PenmanMonteith.
@param filename filename containing KPM records.
*/
public static List<StateCU_PenmanMonteith> readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_PenmanMonteith.readStateCUFile";
	String iline = null;
	StateCU_PenmanMonteith kpm = null;
	List<StateCU_PenmanMonteith> kpmList = new Vector<StateCU_PenmanMonteith>(25);
	BufferedReader in = null;
	int lineNum = 0;
	
	Message.printStatus ( 1, rtn, "Reading StateCU KPM file: " + filename );
	try {
		// The following throws an IOException if the file cannot be opened...
		in = new BufferedReader ( new FileReader (filename));
		int nc = -1;
		String title = null; // The title is currently read but not stored since it is never really used for anything.
		while ( (iline = in.readLine()) != null ) {
			++lineNum;
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0 ){
				continue;
			}
			if ( title == null ) {
				title = iline;
			}
			else if ( nc < 0 ) {
				// Assume that the line contains the number of crops
				nc = Integer.parseInt( iline.trim() );
				break;
			}
		}
	
		// Now loop through the number of curves...
	
		// TODO SAM 2010-03-30 Evaluate if needed
		//String id;
		String cropn;
		List<String> tokens;
		int j = 0;
		// Read the number of curves (crops)
		for ( int i = 0; i < nc; i++ ) {
			// Read a free format line...
			iline = in.readLine();
			++lineNum;
	
			tokens = StringUtil.breakStringList ( iline.trim(), " \t", StringUtil.DELIM_SKIP_BLANKS );
			// TODO SAM 2007-02-18 Evaluate if needed
			//id = tokens.elementAt(0);
			cropn = tokens.get(1);
			
			// Allocate new StateCU_PenmanMonteith instance...
	
			int ngs = getNGrowthStagesFromCropName(cropn);
			kpm = new StateCU_PenmanMonteith ( ngs );
			// Number of coefficients per growth stage...
			int ncpgs = StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
			kpm.setName ( cropn );
			// TODO SAM 2005-05-22 Ignore the old ID and use the crop name - this facilitates
			// sorting and other standard StateCU_Data features.
			//kbc.setID ( id );
			kpm.setID ( cropn );
			
			// Read the coefficients...
			for ( int igs = 0; igs < ngs; igs++ ) {
				for ( j = 0; j < ncpgs; j++ ) {
					iline = in.readLine();
					++lineNum;
					tokens = StringUtil.breakStringList ( iline.trim(), " \t", StringUtil.DELIM_SKIP_BLANKS );
					kpm.setCurvePosition(igs,j,Double.parseDouble(tokens.get(0)) );
					kpm.setCurveValue(igs,j,Double.parseDouble(tokens.get(1)) );
				}
			}
	
			// add the StateCU_PenmanMonteith to the list...
			kpmList.add ( kpm );
		}
	}
	catch ( Exception e ) {
		Message.printWarning( 3, rtn, "Error reading file (" + e + ")." );
		Message.printWarning(3, rtn, e);
		throw new IOException ( "Error reading file \"" + filename + "\" near line " + lineNum );
	}
	finally {
		if ( in != null ) {
			in.close();
		}
	}
	return kpmList;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_PenmanMonteith bc = (StateCU_PenmanMonteith)_original;
	super.restoreOriginal();

	__kcb = bc.__kcb;
	__kcday = bc.__kcday;
	__nGrowthStages = bc.__nGrowthStages;
	_isClone = false;
	_original = null;
}

/**
Set the curve value for the crop coefficient curve (coefficient only).
@param igs growth stage index (zero-index).
@param i Index in the curve (zero-index).  For example 1 corresponds to 10%.
*/
public void setCurveValue ( int igs, int i, double coeff )
{	__kcb[igs][i] = coeff;
}

/**
Set the values for the crop coefficient curve (curve position and coefficient).
@param igs growth stage index (zero-index).
@param i Index in the curve (zero-index).  For example 0 corresponds to 0% and 1 to 10%.
@param pos Position in the curve (percent).
@param coeff Value at the position.
*/
public void setCurveValues ( int igs, int i, double pos, double coeff )
{	__kcday[igs][i] = pos;
	__kcb[igs][i] = coeff;
}

/**
Sets the percent for a curve value.
@param i the index in the curve (zero-index).
For example 1 corresponds to 5% for a percent curve or day 15 for a day curve.
@param pos the new percent to change the index position to.
*/
public void setCurvePosition(int igs, int i, double pos)
{
	__kcday[igs][i] = pos;
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
	double [][] kcday = getKcday();
	double [][] kcb = getKcb();
	// Percent of growth stage
	for ( int igs = 0; igs < kcday.length; igs++ ) {
		for ( int ipos = 0; ipos < kcday[igs].length; ipos++ ) {
			if ( (kcday[igs][ipos] < 0.0) || (kcday[igs][ipos] > 100.0)) {
				validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id +
					"\" percent of growth stage (" +
					kcday[igs][ipos] + ") is invalid.", "Specify as 0 to 100.") );
			}
			if ( (kcb[igs][ipos] < 0) || (kcb[igs][ipos] > 3.0)) {
				validation.add(new StateCU_ComponentValidationProblem(this,"Crop \"" + id + "\" coefficient (" +
					kcb[igs][ipos] + ") is invalid.", "Specify as 0 to 3.0 (upper limit may vary by location).") );
			}
		}
	}

	return validation;
}

/**
Write a list of StateCU_PenmanMonteith to a file.  The filename is adjusted to
the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A list of StateCU_PenmanMonteith to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename,
	List<StateCU_PenmanMonteith> data_Vector, List<String> new_comments )
throws IOException
{	writeStateCUFile ( filename_prev, filename, data_Vector, new_comments, null );
}

/**
Write a list of StateCU_PenmanMonteith to a file.  The filename is adjusted to
the working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A list of StateCU_PenmanMonteith to write.
@param new_comments Comments to add to the top of the file.  Specify as null 
if no comments are available.
@param props Properties to control the output.  Currently only the
optional Precision property can be set, indicating how many digits after the
decimal should be printed (default is 3).
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filename_prev, String filename,
	List<StateCU_PenmanMonteith> data_Vector, List<String> new_comments, Integer precision )
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
	writeVector ( data_Vector, out, precision );
	out.flush();
	out.close();
	out = null;
}

/**
Write a list of StateCU_PenmanMonteith to an opened file.
@param data_Vector A list of StateCU_PenmanMonteith to write.
@param out output PrintWriter.
@param props Properties to control the output.  Currently only the
optional Precision property can be set, indicating how many digits after the
decimal should be printed (default is 3).
@exception IOException if an error occurs.
*/
private static void writeVector ( List<StateCU_PenmanMonteith> data_Vector, PrintWriter out, Integer precision )
throws IOException
{	String cmnt = "#>";
	// Missing data are handled by formatting all as strings (blank if necessary).
	if ( precision == null ) {
		precision = new Integer(3); // Make this agree with the Blaney-Criddle default
	}
	int precision2 = precision.intValue();

	out.println ( cmnt );
	out.println ( cmnt + " StateCU Penman-Monteith Crop Coefficient (KPM) File" );
	out.println ( cmnt );
	out.println ( cmnt + " Record 1 format (a80)" );
	out.println ( cmnt );
	out.println ( cmnt + "  Title     remark:  Title" );
	out.println ( cmnt );
	out.println ( cmnt + " Record 2 format (free format)" );
	out.println ( cmnt );
	out.println ( cmnt + "  NumCurves     nc:  Number of crop coefficient curves" );
	out.println ( cmnt );
	out.println ( cmnt + " Record 3 format (free format)" );
	out.println ( cmnt );
	out.println ( cmnt + "  ID            id:  Crop number (not used by StateCU)" );
	out.println ( cmnt + "  CropName   cropn:  Crop name (e.g., ALFALFA)" );
	out.println ( cmnt );
	out.println ( cmnt + " Record 4 format (free format)" );
	out.println ( cmnt );
	out.println ( cmnt + "  Percent    kcday:  Time from start of growth to effective cover (%)");
	out.println ( cmnt + "                     or % of next growth stage." );
	out.println ( cmnt + "  Coeff        kcb:  Crop coefficient for alfalfa-based ET" );
	out.println ( cmnt );
	out.println ( cmnt + "  33 day/crop coefficient pairs for alfalfa.");
	out.println ( cmnt + "  11 day/crop coefficient pairs for grass pasture.");
	out.println ( cmnt + "  22 day/crop coefficient pairs for all other crop types.");
	out.println ( cmnt );
	out.println ( cmnt + "Title" );
	out.println ( cmnt + "NumCurves" );
	out.println ( cmnt + "ID CropName" );
	out.println ( cmnt + "Percent Coeff" );
	out.println ( cmnt + "----------------------------" );
	out.println ( cmnt + "EndHeader" );
	// Default title
	out.println ( "Crop Coefficient Curves for Penman-Monteith" );

	int num = 0;
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	out.println ( num );
	// Width allows precision to be increased some...
	String value_format = "%8." + precision2 + "f";
	int i = 0;
	for ( StateCU_PenmanMonteith kpm : data_Vector ) {
		++i;
		if ( kpm == null ) {
			continue;
		}
		// Crop number (not used by StateCU) and name
		String name = kpm.getName();
		// Since free format, the ID must always have something.  If
		// we don't know, put -999...
		String id = "" + i; // Default to sequential number
		out.println ( id + " " + name );
		
		// Loop through the number of growth stages per crop
		int nGrowthStages = kpm.getNGrowthStages();
		int ncpgs = StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
		for ( int igs = 0; igs < nGrowthStages; igs++ ) {
			for ( int j = 0; j < ncpgs; j++ ) {
				out.println (
				StringUtil.formatString(kpm.getKcday(igs,j),"%3.0f") +
					StringUtil.formatString(kpm.getKcb(igs,j),value_format) );
			}
		}
	}
}

/**
Writes a list of StateCU_PenmanMonteith objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the file.
Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.  
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update,
	List<StateCU_PenmanMonteith> data, List<String> outputComments ) 
throws Exception
{	String routine = "StateCU_PenmanMonteith.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("Name");
	fields.add("GrowthStage");
	fields.add("Percent");
	fields.add("Coefficient");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_PENMAN_MONTEITH;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
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
	List<String> commentString = new Vector<String>(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new Vector<String>(1);
	ignoreCommentString.add ( "#>" );
	String[] line = new String[fieldCount];
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
		newComments2.add(1,"StateCU Penman-Monteith crop coefficients as a delimited list file.");
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
		StateCU_PenmanMonteith kpm;
		for (int i = 0; i < size; i++) {
			kpm = data.get(i);
			int ngs = kpm.getNGrowthStages();
			int ncpgs = StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
			for ( int igs = 0; igs < ngs; igs++ ) {
				for (j = 0; j < ncpgs; j++) {
					line[0] = StringUtil.formatString(kpm.getName(), formats[0]).trim();
					line[1] = StringUtil.formatString((igs + 1), formats[1]).trim();
					line[2] = StringUtil.formatString(kpm.getKcday(igs,j), formats[2]).trim();
					line[3] = StringUtil.formatString(kpm.getKcb(igs,j), formats[3]).trim();
	
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