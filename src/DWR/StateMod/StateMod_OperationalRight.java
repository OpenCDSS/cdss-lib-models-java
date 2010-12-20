//------------------------------------------------------------------------------
// StateMod_OperationalRight - class derived from StateMod_Data.  Contains 
//	information read from the operational rights file.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 07 Jan 1998	Catherine E.		Created initial version of class
//		Nutting-Lane, RTi
// 23 Feb 1998	CEN, RTi		Added Write routines
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 23 Nov 1999	CEN, RTi		Added comments for each 
//					StateMod_OperationalRight
//					instantiation.
// 07 Mar 2000	CEN, RTi		Modified read/write methods logic to
//					work off dumx variable to determine
//					additional lines for a rule rather than
//					using rule type.  Also, added rule types
//					15 and 16.
// 19 Feb 2001	Steven A. Malers, RTi	Code review.  Clean up javadoc.  Handle
//					nulls and set unused variables to null.
//					Add finalize.  Alphabetize methods.
//					Change IO to IOUtil.  Change some status
//					messages to debug and remove some debug
//					messages.
// 2001-12-27	SAM, RTi		Update to use new fixedRead() to
//					improve performance (are not using full
//					optimization here).
// 2002-09-19	SAM, RTi		Use isDirty() instead of setDirty() to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTi	Renamed from SMOprits to 
//					StateMod_OperationalRight
// 2003-06-10	JTS, RTi		* Folded dumpOperationalRightsFile()
//					  into writeOperationalRightsFile()
//					* Renamed parseOperationalRightsFile()
//					  into readOperationalRightsFile()
// 2003-06-23	JTS, RTi		Renamed writeOperationalRightsFile()
//					to writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readOperationalRightsFile()
//					to readStateModFile()
// 2003-07-15	JTS, RTi		Changed to use new dataset design.
// 2003-08-03	SAM, RTi		Changed isDirty() back to setDirty().
// 2003-08-25	SAM, RTi		Changed public oprightsOptions to
//					TYPES, consistent with other programming
//					standards.
// 2003-08-28	SAM, RTi		* Call setDirty() for each object and
//					  the data component.
//					* Clean up parameter names and javadoc.
// 2003-09-15	SAM, RTi		* Update to handle all new operations,
//					  up through number 23.
//					* Change some data types from numbers to
//					  String because of changes in how they
//					  are used in the FORTRAM (must be doing
//					  internal type casting in FORTRAN).
//					* Change StringTokenizer to
//					  breakStringList() - easier to check
//					  count of tokens.
// 2003-09-22	J. Thomas Sapienza, RTi	* Added hasImonsw().
//					* Added setupImonsw().
//					* Added getQdebt().
//					* Added getQdebtx().
//					* Added getSjmina().
//					* Added getSjrela().
// 2003-10-19	SAM, RTi		Change description of types 2 and 3 as
//					per Ray Bennett 2003-10-18 email.
// 2004-07-14	JTS, RTi		* Added acceptChanges().
//					* Added changed().
//					* Added clone().
//					* Added compareTo().
//					* Added createBackup().
//					* Added restoreOriginal().
//					* Now implements Cloneable.
//					* Now implements Comparable.
//					* Clone status is checked via _isClone
//					  when the component is marked as dirty.
// 2004-08-25	JTS, RTi		Revised the clone() code because of
//					null pointers being thrown if the data
//					arrays were null.
// 2004-08-26	JTS, RTi		The array values (_intern and _imonsw)
//					were not being handled in 
//					restoreOriginal() or compareTo(), so
//					they were added.
// 2006-08-16	SAM, RTi		* Add names of operational rights 24 to
//					  35.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_OperationalRight 
extends StateMod_Data
implements Cloneable, Comparable {

/**
Maximum handled operational right type (those that the software has been coded to handle).
Ideally this should be the same as those listed in the operational right metadata, but some metadata may
be added at runtime to account for rights added to the model but not the Java code.
*/
public static int MAX_HANDLED_TYPE = 23;
/**
Administration number.
*/
protected String _rtem;
/**
Typically the number of intervening structures or the number of monthly
switches, depending on the right number
*/
protected int _dumx;
/**
Typically the destination ID.
*/
protected String _ciopde;
/**
Typically the destination account.
*/
protected String _iopdes;
/**
Typically the supply ID.
*/
protected String _ciopso1;
/**
Typically the supply account.
*/
protected String _iopsou1;
/**
Definition varies by right type.
*/
protected String _ciopso2;
/**
Definition varies by right type.
*/
protected String _iopsou2;
/**
Definition varies by right type.
*/
protected String _ciopso3;
/**
Definition varies by right type.
*/
protected String _iopsou3;
/**
Used with type 17, 18.
*/
protected String _ciopso4;
/**
Used with type 17, 18.
*/
protected String _iopsou4;
/**
Used with type 17, 18.
*/
protected String _ciopso5;
/**
Used with type 17, 18.
*/
protected String _iopsou5;
/**
Operational right type > 1.
*/
protected int _ityopr;
/**
Intervening structure IDs (up to 10 in StateMod doc but no limit here) - used by some rights, null if not used.
*/
protected String _intern[] = null;
/**
Monthly switch, for some rights, null if not used.
*/
protected int _imonsw[] = null;
/**
Comments provided by user - # comments before each right.  An empty (non-null) list is guaranteed.
TODO SAM 2010-12-14 Evaluate whether this can be in StateMod_Data or will it bloat memory.
*/
private List<String> __commentsBeforeData = new Vector();
/**
Used with operational right 17, 18.
*/
protected double _qdebt;
/**
used with operational right 17, 18.
*/
protected double _qdebtx;
/**
Used with operational right 20.
*/
protected double _sjmina;
/**
Used with operational right 20.
*/
protected double _sjrela;
/**
Plan ID.
*/
private String __creuse;
/**
Diversion type.
*/
private String __cdivtyp;
/**
Conveyance loss.
*/
private double __oprLoss;
/**
Miscellaneous limits.
*/
private double __oprLimit;
/**
Beginning year of operation.
*/
private int __ioBeg;
/**
Ending year of operation.
*/
private int __ioEnd;

/**
The operational right as a list of strings (lines after right comments and prior to the comments for
the next right.
*/
private List<String> __rightStringsVector = new Vector();

/**
Used with monthly and annual limitation.
*/
private String __cx = "";

// cidvri = ID is in base class identifier
// nameo = Name is in base class name
// ioprsw = on/off is in base class switch

/**
Constructor.
*/
public StateMod_OperationalRight() {
	super();
	initialize();
}

/**
Accepts any changes made inside of a GUI to this object.
*/
public void acceptChanges() {
	_isClone = false;
	_original = null;
}

/**
Compares this object with its original value (generated by createBackup() upon
entering a GUI) to see if it has changed.
*/
public boolean changed() {
	if (_original == null) {
		return true;
	}
	if (compareTo(_original) == 0) {
		return false;
	}
	return true;
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_OperationalRight op = (StateMod_OperationalRight)super.clone();
	op._isClone = true;

	if (_intern != null) {
		op._intern = (String[])_intern.clone();
	}
	else {
		op._intern = null;
	}

	if (_imonsw != null) {
		op._imonsw = (int[])_imonsw.clone();
	}
	else {
		_imonsw = null;
	}

	return op;
}

/**
Compares this object to another StateMod_OperationalRight object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_OperationalRight op = (StateMod_OperationalRight)o;

	res = _rtem.compareTo(op._rtem);
	if (res != 0) {
		return res;
	}

	if (_dumx < op._dumx) {
		return -1;
	}
	else if (_dumx > op._dumx) {
		return 1;
	}
	
	res = _ciopde.compareTo(op._ciopde);
	if (res != 0) {
		return res;
	}

	res = _iopdes.compareTo(op._iopdes);
	if (res != 0) {
		return res;
	}

	res = _ciopso1.compareTo(op._ciopso1);
	if (res != 0) {
		return res;
	}

	res = _iopsou1.compareTo(op._iopsou1);
	if (res != 0) {
		return res;
	}

	res = _ciopso2.compareTo(op._ciopso2);
	if (res != 0) {
		return res;
	}

	res = _iopsou2.compareTo(op._iopsou2);
	if (res != 0) {
		return res;
	}

	res = _ciopso3.compareTo(op._ciopso3);
	if (res != 0) {
		return res;
	}

	res = _iopsou3.compareTo(op._iopsou3);
	if (res != 0) {
		return res;
	}

	res = _ciopso4.compareTo(op._ciopso4);
	if (res != 0) {
		return res;
	}

	res = _iopsou4.compareTo(op._iopsou4);
	if (res != 0) {
		return res;
	}

	res = _ciopso5.compareTo(op._ciopso5);
	if (res != 0) {
		return res;
	}

	res = _iopsou5.compareTo(op._iopsou5);
	if (res != 0) {
		return res;
	}

	if (_intern == null && op._intern == null) {
		// ok
	}
	else if (_intern == null) {
		return -1;
	}
	else if (op._intern == null) {
		return 1;
	}
	else {
		int size1 = _intern.length;
		int size2 = op._intern.length;
		if (size1 < size2) {
			return -1;
		}
		else if (size1 > size2) {
			return 1;
		}

		for (int i = 0; i < size1; i++) {
			res = _intern[i].compareTo(op._intern[i]);
			if (res != 0) {
				return res;
			}
		}
	}

	if (_imonsw == null && op._imonsw == null) {
		// ok
	}
	else if (_imonsw == null) {
		return -1;
	}
	else if (op._imonsw == null) {
		return 1;
	}
	else {
		int size1 = _imonsw.length;
		int size2 = op._imonsw.length;
		if (size1 < size2) {
			return -1;
		}
		else if (size1 > size2) {
			return 1;
		}

		for (int i = 0; i < size1; i++) {
			if (_imonsw[i] < op._imonsw[i]) {
				return -1;
			}
			else if (_imonsw[i] > op._imonsw[i]) {
				return 1;
			}
		}
	}

	if (_ityopr < op._ityopr) {
		return -1;
	}
	else if (_ityopr > op._ityopr) {
		return 1;
	}
	
	if (_qdebt < op._qdebt) {
		return -1;
	}
	else if (_qdebt > op._qdebt) {
		return 1;
	}
	
	if (_qdebtx < op._qdebtx) {
		return -1;
	}
	else if (_qdebtx > op._qdebtx) {
		return 1;
	}
	
	if (_sjmina < op._sjmina) {
		return -1;
	}
	else if (_sjmina > op._sjmina) {
		return 1;
	}
	
	if (_sjrela < op._sjrela) {
		return -1;
	}
	else if (_sjrela > op._sjrela) {
		return 1;
	}
	
	res = __creuse.compareTo(op.__creuse);
	if (res != 0) {
		return res;
	}
	
	res = __cdivtyp.compareTo(op.__cdivtyp);
	if (res != 0) {
		return res;
	}
	
	if (__oprLoss < op.__oprLoss) {
		return -1;
	}
	else if (__oprLoss > op.__oprLoss) {
		return 1;
	}
	
	if (__oprLimit < op.__oprLimit) {
		return -1;
	}
	else if (__oprLimit > op.__oprLimit) {
		return 1;
	}
	
	if (__ioBeg < op.__ioBeg) {
		return -1;
	}
	else if (__ioBeg > op.__ioBeg) {
		return 1;
	}
	
	if (__ioEnd < op.__ioEnd) {
		return -1;
	}
	else if (__ioEnd > op.__ioEnd) {
		return 1;
	}

	return 0;
}

/**
Creates a copy of the object for later use in checking to see if it was changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_OperationalRight)_original)._isClone = false;
	_isClone = true;
}

// TODO SAM 2010-12-11 Should the default if not specified be version 2?
/**
Determine the StateMod operational right file version.  Version 1 is old and Version 2 was introduced
in Version 12.0.  The version is determined by checking for the string "FileFormatVersion 2" in a comment
line.
@return 1 for the old version and 2 for version 2.
*/
private static int determineFileVersion ( String filename )
{	BufferedReader in = null;
	int version = 1;
	try {
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( filename )) );
	    try {
    		// Read lines and check for common strings that indicate a DateValue file.
    		String string = null;
    		while( (string = in.readLine()) != null ) {
    			string = string.trim().toUpperCase();
    			if ( string.startsWith("#") && (string.indexOf("FILEFORMATVERSION 2") > 0) ) {
    				version = 2;
    				break;
    			}
    		}
	    }
	    finally {
	        if ( in != null ) {
	            in.close();
	        }
	    }
		in = null;
		return version;
	}
	catch ( Exception e ) {
		return version;
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_ciopde = null;
	_iopdes = null;
	_ciopso1 = null;
	_iopsou1 = null;
	_ciopso2 = null;
	_iopsou2 = null;
	_ciopso3 = null;
	_iopsou3 = null;
	_ciopso4 = null;
	_iopsou4 = null;
	_ciopso5 = null;
	_iopsou5 = null;
	_imonsw = null;
	_intern = null;
	__commentsBeforeData = null;
	__creuse = null;
	__cdivtyp = null;
	super.finalize();
}

/**
Return the cdivtyp (diversion type).
*/
public String getCdivtyp() {
	return __cdivtyp;
}

/**
Return the ciopde.
*/
public String getCiopde() {
	return _ciopde;
}

/**
Return the ciopso1.
*/
public String getCiopso1() {
	return _ciopso1;
}

/**
Return the ciopso2.
*/
public String getCiopso2() {
	return _ciopso2;
}

/**
Return the ciopso3.
*/
public String getCiopso3() {
	return _ciopso3;
}

/**
Return the ciopso4.
*/
public String getCiopso4() {
	return _ciopso4;
}

/**
Return the ciopso5.
*/
public String getCiopso5() {
	return _ciopso5;
}

/**
Return the creuse (plan identifier).
*/
public String getCreuse() {
	return __creuse;
}

/**
Return the comments from the input file that immediate precede the data.
@return the comments from the input file that immediate precede the data.
*/
public List<String> getCommentsBeforeData() {
	return __commentsBeforeData;
}

/**
Return the cx (used with monthly and annual limitation).
*/
public String getCx() {
	return __cx;
}

/**
Retrieve dumx.
*/
public int getDumx() {
	return _dumx;
}

/**
Return the array of monthly switch.
*/
public int [] getImonsw() {
	return _imonsw;
}

/**
Return a monthly switch at an index.
@param index month to get switch for (0-11), where the index is a position, not
a month (actual month is controlled by the year type for the data set).
*/
public int getImonsw(int index) {
	return _imonsw[index];
}

/**
Return the array of "intern".
*/
public String [] getIntern()
{
	return _intern;
}

/**
Return the "intern" at an index.
*/
public String getIntern(int index) {
	return _intern[index];
}

/**
Retrieve the ioBeg.
*/
public int getIoBeg() {
	return __ioBeg;
}

/**
Retrieve the ioEnd.
*/
public int getIoEnd() {
	return __ioEnd;
}

/**
Get the interns as a vector.
@return the intervening structure identifiers or an empty Vector.
*/
public List getInternsVector() {
	List v = new Vector();
	if ( _intern != null ) {
		for ( int i = 0; i < _intern.length; i++) {
			v.add(getIntern(i));
		}
	}
	return v;
}

/**
Return the iopdes.
*/
public String getIopdes() {
	return _iopdes;
}

/**
Return the iopsou.
*/
public String getIopsou1() {
	return _iopsou1;
}

/**
Return the iopsou2.
*/
public String getIopsou2() {
	return _iopsou2;
}

/**
Return the iopsou3.
*/
public String getIopsou3() {
	return _iopsou3;
}

/**
Return the iopsou4.
*/
public String getIopsou4() {
	return _iopsou4;
}

/**
Return the iopsou5.
*/
public String getIopsou5() {
	return _iopsou5;
}

/**
Retrieve the ityopr.
*/
public int getItyopr() {
	return _ityopr;
}

/**
Return OprLimit.
@return OprLimit.
*/
public double getOprLimit() {
	return __oprLimit;
}

/**
Return OprLoss.
@return OprLoss.
*/
public double getOprLoss() {
	return __oprLoss;
}

/**
Return rtem.
@return rtem.
*/
public String getRtem() {
	return _rtem;
}

public double getQdebt() {
	return _qdebt;
}

public double getQdebtx() {
	return _qdebtx;
}

/**
 * @return the list of strings that containing the operating rule data when the
 * right is not understood.
 */
public List<String> getRightStrings()
{
	return __rightStringsVector;
}

public double getSjrela() {
	return _sjrela;
}

public double getSjmina() {
	return _sjmina;
}

/**
Initializes member variables.
*/
private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_OPERATION_RIGHTS;
	_rtem = "0";
	_dumx = 0;
	_ciopde = "";
	_iopdes = "0";
	_ciopso1 = "";
	_iopsou1 = "0";
	_ciopso2 = "";
	_iopsou2 = "0";
	_ciopso3 = "";
	_iopsou3 = "0";
	_ciopso4 = "";
	_iopsou4 = "0";
	_ciopso5 = "";
	_iopsou5 = "0";
	_ityopr = 0;	// Unknown
	_imonsw = null;	// Define in constructor or when reading
	_intern = null;	// Define in constructor or when reading
	__commentsBeforeData = new Vector(1);
	_qdebt = 0.0;
	_qdebtx = 0.0;
	_sjmina = 0.0;
	_sjrela = 0.0;
	// Newer data
	__creuse = "";
	__cdivtyp = "";
	__oprLoss = 0.0;
	__oprLimit = 0.0;
	__ioBeg = 0;
	__ioEnd = 0;
}

public boolean hasImonsw() {
	if (_imonsw != null) {
		return true;
	}
	return false;
}

/**
Indicate whether an operational right is known to the software.  If true, then the internal code should
handle.  If false, the right should be treated as strings on read.
*/
public static boolean isRightUnderstoodByCode( int rightType )
{
	if ( rightType <= MAX_HANDLED_TYPE ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Read operational right information in and store in a list.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List<StateMod_OperationalRight> readStateModFile(String filename)
throws Exception
{
	int version = determineFileVersion(filename);
	if ( version == 1 ) {
		return readStateModFileVersion1(filename);
	}
	else if ( version == 2 ) {
		return readStateModFileVersion2(filename);
	}
	else {
		throw new Exception ( "Unable to determine StateMod file version to read operational rights." );
	}
}

/**
 * Read the StateMod operational rights file intervening structures.
 * @param ninterv Intervening structures switch.
 * @param routine to use for logging.
 * @param linecount Line count (1+) before reading in this method.
 * @param in BufferedReader to read.
 * @param anOprit Operational right for which to read data.
 * @return the number of errors.
 * @exception IOException if there is an error reading the file
 */
private static int readStateModFile_InterveningStructures ( int ninterv, String routine, int linecount,
		BufferedReader in, StateMod_OperationalRight anOprit )
throws IOException
{	// One line has up to 10 intervening structure identifiers
	String iline = in.readLine().trim();
	int errorCount = 0;
	try {
		Message.printStatus ( 2, routine, "Processing operating rule line " + (linecount + 1) + ": " + iline );
		List<String> tokens = StringUtil.breakStringList( iline, " \t", StringUtil.DELIM_SKIP_BLANKS );
		int ntokens = 0;
		if ( tokens != null ) {
			ntokens = tokens.size();
		}
		if ( ntokens > 0 ) {
			anOprit._intern = new String[ninterv];
		}
		for ( int i=0; i<ntokens; i++) {
			anOprit.setIntern(i, tokens.get(i).trim(), false);
		}
	}
	catch ( Exception e ) {
		// TODO SAM 2010-12-13 Need to handle errors and provide feedback
		Message.printWarning(3, routine, "Error reading intervening structures at line " + (linecount + 1) +
			": " + iline + " (" + e + ")" );
	}
	return errorCount;
}

/**
Read the StateMod operational rights file intervening structures.
@param routine to use for logging.
@param linecount Line count (1+) before reading in this method.
@param in BufferedReader to read.
@param anOprit Operational right for which to read data.
@return the number of errors.
@exception IOException if there is an error reading the file
*/
private static int readStateModFile_MonthlyAndAnnualLimitationData ( String routine, int linecount,
		BufferedReader in, StateMod_OperationalRight anOprit )
throws IOException
{	// One line has up to 10 intervening structure identifiers
	String iline = in.readLine().trim();
	int errorCount = 0;
	try {
		Message.printStatus ( 2, routine, "Processing operating rule monthly and annual limitation line " + (linecount + 1) + ": " + iline );
		List<String> tokens = StringUtil.breakStringList( iline, " \t", StringUtil.DELIM_SKIP_BLANKS );
		int ntokens = 0;
		if ( tokens != null ) {
			ntokens = tokens.size();
		}
		// Only one identifier
		if ( ntokens > 0 ) {
			anOprit.setCx ( tokens.get(0).trim() );
		}
	}
	catch ( Exception e ) {
		// TODO SAM 2010-12-13 Need to handle errors and provide feedback
		Message.printWarning(3, routine, "Error reading monthly and annual limitation at line " + (linecount + 1) +
			": " + iline + " (" + e + ")" );
	}
	return errorCount;
}

/**
 * Read the StateMod operational rights file monthly switches.
 * @param nmonsw Monthly switch
 * @param routine to use for logging.
 * @param linecount Line count (1+) before reading in this method.
 * @param in BufferedReader to read.
 * @param anOprit Operational right for which to read data.
 * @return the number of errors.
 * @exception IOException if there is an error reading the file
 */
private static int readStateModFile_MonthlySwitches ( int nmonsw, String routine, int linecount,
		BufferedReader in, StateMod_OperationalRight anOprit )
throws IOException
{
	String iline = in.readLine().trim();
	try {
		Message.printStatus ( 2, routine, "Processing operating rule line " + (linecount + 1) + ": " + iline );
		// Switches are free format
		List<String> tokens = StringUtil.breakStringList( iline, " \t", StringUtil.DELIM_SKIP_BLANKS );
		int ntokens = 0;
		if ( tokens != null ) {
			ntokens = tokens.size();
		}
		if ( nmonsw > 0 ) {
			anOprit._imonsw = new int[nmonsw];
		}
		for ( int i=0; i<ntokens; i++) {
			anOprit.setImonsw(i, tokens.get(i).trim());
		}
	}
	catch ( Exception e ) {
		// TODO SAM 2010-12-13 Need to handle errors and provide feedback
		Message.printWarning(3, routine, "Error reading monthly switches at line " + (linecount + 1) + ": " + iline +
		    " (" + e + ")" );
		return 1;
	}
	return 0;
}

/**
Read operational right information in and store in a list.
@param filename Name of file to read - the file should be the older "version 1" format.
@exception Exception if there is an error reading the file.
*/
private static List<StateMod_OperationalRight> readStateModFileVersion1(String filename)
throws Exception {
	String routine = "StateMod_OperationalRight.readStateModFileVersion1";
	String iline = null;
	List v = null;
	List<StateMod_OperationalRight> theOprits = new Vector();
	List<String> comment_vector = new Vector(1);	// Will be used prior to finding an operational right
	// Formats use strings for many variables because files may have extra
	// whitespace or be used for numeric and character data...
	// Consistent among all operational rights...
	// Before adding creuse, etc... (12 values)
	//String format_0 = "s12s24x16s12s8i8x1s12s8x1s12s8x1s12s8s8";
	// After adding creuse, etc.... (18 values)
	String format_0 = "s12s24x16s12s8i8x1s12s8x1s12s8x1s12s8s8x1a12x1s12x1s8s8s8s8";
	// Format for intervening structures...
	// TODO SAM 2007-03-01 Evaluate use
	//String format_interv = "x36s12s12s12s12s12s12s12s12s12s12";
	// Rio Grande additional data...
	String format_rg = "x64s8s8x1s12s8x1s12s8x1s12s8";
	String format_sj = "x64s8s8";
	BufferedReader in = null;
	StateMod_OperationalRight anOprit = null;
	int linecount = 0;

	int dumx, ninterv, nmonsw;
	int type = 0;
	int errorCount = 0;

	Message.printStatus(2, routine, "Reading operational rights file \"" + filename + "\"");
	try {
		boolean reading_unknown_right = false;
		List<String> right_strings_Vector = null;	// Operating rule as a list of strings
		in = new BufferedReader(new FileReader(filename));
		while ((iline = in.readLine()) != null) {
			++linecount;
			Message.printStatus ( 2, routine, "Processing operating rule line " + linecount + ": " + iline );
			// If was reading an unknown rule, turn off flag if done reading.
			if ( reading_unknown_right ) {
				if ( (iline.length() > 0) && (iline.charAt(0) != ' ') ) {
					// Done reading the unknown right.  Next are either comments before or data for
					// the next right.
					reading_unknown_right = false;
					// Add to the end of the list
					Message.printStatus ( 2, routine, "Adding unrecognized operational right \"" +
							anOprit.getID() + "\" as text");
					theOprits.add(anOprit);
					// Don't continue because the line that was just read needs to be handled.
				}
				else {
					// Blank at front of line so assume still reading the unknown right.
					// Add a string to the unknown right
					right_strings_Vector.add ( iline );
					continue;
				}
			}
			// check for comments
			// if a temporary comment line
			if ( iline.startsWith("#>") ) {
				continue;
			}
			/* TODO SAM 2008-03-10 Evaluate whether needed
			else if ((iline.startsWith("#") && !readingTmpComments)	|| iline.trim().length()==0) {
				// A general comment line not associated with an operational right...
				continue;
			}
			*/
			else if (iline.startsWith("#")) { 
				// A comment line specific to an individual operational right...
				if (Message.isDebugOn) {
					Message.printDebug(10, routine, "Opright comments: " + iline);
				}
				comment_vector.add(iline.substring(1).trim());
				continue;
			}

			// Allocate new operational rights object
			anOprit = new StateMod_OperationalRight();
			if (Message.isDebugOn) {
				Message.printDebug(10, routine,	"Number of Opright comments: " + comment_vector.size());
			}
			if (comment_vector.size()> 0) {
				// Set comments that have been read previous to this line.
				anOprit.setCommentsBeforeData(comment_vector);
			}
			// Always clear out for next object...
			comment_vector = new Vector(1);

			// line 1
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line 1: " + iline);
			}
			v = StringUtil.fixedRead(iline, format_0);
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, v.toString() );
			}
			anOprit.setID(((String)v.get(0)).trim()); 
			anOprit.setName(((String)v.get(1)).trim()); 
			anOprit.setRtem(((String)v.get(2)).trim()); 
			anOprit.setDumx(((String)v.get(3)).trim());
			dumx = anOprit.getDumx();
			anOprit.setSwitch( (Integer)v.get(4) );
			// Should always be in file but may be zero...
			anOprit.setCiopde(((String)v.get(5)).trim());
			anOprit.setIopdes(((String)v.get(6)).trim());
			// Should always be in file but may be zero...
			anOprit.setCiopso1(((String)v.get(7)).trim());
			anOprit.setIopsou1(((String)v.get(8)).trim());
			// Should always be in file but may be zero...
			anOprit.setCiopso2(((String)v.get(9)).trim());
			anOprit.setIopsou2(((String)v.get(10)).trim());
			// Type is used to make additional decisions below...
			type = StringUtil.atoi(((String)v.get(11)).trim());
			anOprit.setItyopr(type);
			// Plan ID
			anOprit.setCreuse(((String)v.get(12)).trim());
			// Diversion type
			anOprit.setCdivtyp(((String)v.get(13)).trim());
			// Conveyance loss...
			double oprLoss = StringUtil.atod(((String)v.get(14)).trim());
			anOprit.setOprLoss ( oprLoss );
			// Miscellaneous limits...
			double oprLimit = StringUtil.atod(((String)v.get(15)).trim());
			anOprit.setOprLimit ( oprLimit );
			// Beginning year...
			int ioBeg = StringUtil.atoi(((String)v.get(16)).trim());
			anOprit.setIoBeg ( ioBeg );
			// Ending year...
			int ioEnd = StringUtil.atoi(((String)v.get(17)).trim());
			anOprit.setIoEnd ( ioEnd );
			Message.printStatus( 2, routine, "Reading operating rule type " + type +
					" starting at line " + linecount );
			
			if ( type > MAX_HANDLED_TYPE ) {
				// The type is not known so read in as strings and set the type to negative.
				// Most of the reading will occur at the top of the loop.
				reading_unknown_right = true;
				right_strings_Vector = new Vector();
				right_strings_Vector.add ( iline );
				// Add list and continue to add if more lines are read.  Since using a reference
				// this will ensure that all lines are set for the right.
				anOprit.setRightStrings ( right_strings_Vector );
				Message.printWarning ( 2, routine, "Unknown right type " + type + " at line " + linecount +
						".  Reading as text to continue reading file." );
				continue;
			}

			// Now read the additional lines of data.  Just do the
			// logic brute force since the order of data is not
			// a pattern that is common between many rights...

			nmonsw = 0;
			ninterv = 0;
			
			// May have monthly switch and intervening structures.  For now check the value.
			// FIXME SAM 2008-03-17 Will read in a file that indicates what is allowed so it is
			// easier to dynamically check.
			
			if ( dumx == 12 ) {
				// Only have monthly switches
				nmonsw = 12;
			}
			else if ( dumx >= 0 ) {
				// Only have intervening structures...
				ninterv = dumx;
			}
			else if ( dumx < 0 ){
				// Have monthly switches and intervening structures.
				// -12 of the total count toward the monthly switch and the remainder is
				// the number of intervening structures
				// Check the value because some rules like 17 - Rio Grande Compact use -8
				if ( dumx < -12 ) {
					ninterv = -1*(dumx + 12);
					nmonsw = 12;
				}
				else {
					ninterv = -1*dumx;
				}
			}
			// FIXME SAM 2008-03-17 Need some more checks for things like invalid -11 and + 13
			
			// Start reading additional information before monthly and intervening data)...

			if ( type == 17 ) {
				// Rio Grande compact data...
				iline = in.readLine().trim();
				++linecount;
				Message.printStatus ( 2, routine, "Processing operating rule line " + linecount + ": " + iline );
				v = StringUtil.fixedRead(iline, format_rg);
				anOprit.setQdebt( ((String)v.get(0)).trim() );
				anOprit.setQdebtx( ((String)v.get(1)).trim() );
				anOprit.setCiopso3(	((String)v.get(2)).trim());
				anOprit.setIopsou3(	((String)v.get(3)).trim());
				anOprit.setCiopso4(	((String)v.get(4)).trim());
				anOprit.setIopsou4(	((String)v.get(5)).trim());
				anOprit.setCiopso5(	((String)v.get(6)).trim());
				anOprit.setIopsou5(	((String)v.get(7)).trim());
			}
			else if ( type == 18 ) {
				// Rio Grande Compact - Conejos River
				iline = in.readLine().trim();
				++linecount;
				Message.printStatus ( 2, routine, "Processing operating rule line " + linecount + ": " + iline );
				v = StringUtil.fixedRead(iline, format_rg);
				anOprit.setQdebt( ((String)v.get(0)).trim() );
				anOprit.setQdebtx( ((String)v.get(1)).trim() );
				anOprit.setCiopso3(	((String)v.get(2)).trim());
				anOprit.setIopsou3(	((String)v.get(3)).trim());
				anOprit.setCiopso4(	((String)v.get(4)).trim());
				anOprit.setIopsou4(	((String)v.get(5)).trim());
				anOprit.setCiopso5(	((String)v.get(6)).trim());
				anOprit.setIopsou5(	((String)v.get(7)).trim());
			}
			else if ( type == 20 ) {
				// San Juan RIP...
				v = StringUtil.fixedRead(iline, format_sj);
				anOprit.setSjmina( ((String)v.get(0)).trim());
				anOprit.setSjrela( ((String)v.get(1)).trim());
			}
			
			// ...end reading additional data before monthly and intervening structure data
			
			// Start reading the monthly and intervening structure data...
			
			Message.printStatus ( 2, routine, "Number of intervening structures = " + ninterv +
					" month switch = " + nmonsw );
			if ( nmonsw > 0 ) {
				errorCount += readStateModFile_MonthlySwitches ( nmonsw, routine, linecount, in, anOprit );
				++linecount;
			}
			// Don't read for the Rio Grande types
			if ( (ninterv > 0) && (type != 17) && (type != 18) ) {
				errorCount += readStateModFile_InterveningStructures (
					ninterv, routine, linecount, in, anOprit );
				++linecount;
			}
			
			// ...end reading monthly and intervening structure data.
			
			// Start reading additional data after monthly and intervening structure data...
			
			// ...end reading additional data after monthly and intervening structure data

			// add the operational right to the vector of oprits
			Message.printStatus ( 2, routine, "Adding recognized operational right \"" +
					anOprit.getID() + "\" from full read.");
			theOprits.add(anOprit);
		}
		// All lines have been read.
		if ( reading_unknown_right ) {
			// Last line was part of the unknown right so need to add what there was.
			Message.printStatus ( 2, routine, "Adding unrecognized operational right \"" +
					anOprit.getID() + "\" as text.");
			theOprits.add(anOprit);
		}
	}
	catch (Exception e) {
		Message.printWarning(3, routine, "Error reading near line " + linecount + ": " + iline);
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	return theOprits;
}

/**
Read operational right information in and store in a list.
@param filename Name of file to read - the file should be the older "version 2" format.
@exception Exception if there is an error reading the file.
*/
private static List<StateMod_OperationalRight> readStateModFileVersion2(String filename)
throws Exception {
	String routine = "StateMod_OperationalRight.readStateModFileVersion2";
	String iline = null;
	List v = null;
	List<StateMod_OperationalRight> theOprits = new Vector();
	List<String> commentsBeforeData = new Vector(1);	// Will be used prior to finding an operational right
	// Formats use strings for many variables because files may have extra
	// whitespace or be used for numeric and character data...
	// Consistent among all operational rights...
	// Before adding creuse, etc... (12 values)
	//   String format_0 = "s12s24x16s12s8i8x1s12s8x1s12s8x1s12s8s8";
	// After adding creuse, etc.... (18 values)
	// Format to read line 1.  The following differ from the StateMod documentation (as of Nov 2008 doc):
	// - administration number is read as a string (not float) to prevent roundoff since this
	//   is an important number
	// - iopdes (destination account) is treated as a string (not integer) for flexibility
	// 
	String formatLine1 = "a12a24x12x4a12f8i8x1a12a8x1a12a8x1a12a8i8x1a12x1a12x1f8f8i8i8";
	// Rio Grande additional data...
	// StateMod doc treats last part as numbers but treat as strings here consistent with source ID/account
	//String format_17 = "x64f8f8x1a12i8x1a12i8x1a12i8";
	String format_17 = "x64f8f8x1a12a8x1a12a8x1a12a8";
	// San Juan additional data...
	String format_20 = "x64f8f8";
	StateMod_OperationalRight anOprit = null;
	BufferedReader in = null;
	int linecount = 0;

	int dumxInt, ninterv, nmonsw;
	float dumxFloat;
	double oprLimit; // Internal value
	int rightType = 0;
	int errorCount = 0;

	Message.printStatus(2, routine, "Reading operational rights file \"" + filename + "\"");
	try {
		boolean readingUnknownRight = false;
		List<String> rightStringsList = null;	// Operating rule as a list of strings
		in = new BufferedReader(new FileReader(filename));
		while ((iline = in.readLine()) != null) {
			++linecount;
			Message.printStatus ( 2, routine, "Processing operating rule line " + linecount + ": " + iline );
			// If was reading an unknown rule, turn off flag if done reading.
			if ( readingUnknownRight ) {
				if ( (iline.length() > 0) && (iline.charAt(0) != ' ') ) {
					// Done reading the unknown right.  Next are either comments before or data for
					// the next right.
					readingUnknownRight = false;
					// Add to the end of the list
					Message.printStatus ( 2, routine, "Adding unrecognized operational right \"" +
							anOprit.getID() + "\" as text from previous lines");
					theOprits.add(anOprit);
					// Don't continue because the line that was just read needs to be handled.
				}
				else {
					// Blank at front of line so assume still reading the unknown right.
					// Add a string to the unknown right
					rightStringsList.add ( iline );
					continue;
				}
			}
			// check for comments
			// if a temporary comment line
			if ( iline.startsWith("#>") ) {
				continue;
			}
			/* TODO SAM 2008-03-10 Evaluate whether needed
			else if ((iline.startsWith("#") && !readingTmpComments)	|| iline.trim().length()==0) {
				// A general comment line not associated with an operational right...
				continue;
			}
			*/
			else if (iline.startsWith("#")) { 
				// A comment line specific to an individual operational right...
				if (Message.isDebugOn) {
					Message.printDebug(10, routine, "Opright comments: " + iline);
				}
				// Don't trim because may want to compare output during testing
				// Do trim the initial #, which will get added on output.
				commentsBeforeData.add(iline.substring(1));
				continue;
			}

			// Allocate new operational rights object
			anOprit = new StateMod_OperationalRight();
			if (Message.isDebugOn) {
				Message.printDebug(10, routine,	"Number of Opright comments: " + commentsBeforeData.size());
			}
			if (commentsBeforeData.size()> 0) {
				// Set comments that have been read previous to this line.  First, attempt to discard
				// comments that do not below with the operational right.  For now, search backward for
				// "FileFormatVersion", "EndHeader", and "--e".  If found, discard the comments prior
				// to this because they are assumed to be file header comments, not comments for a specific right.
				String comment;
				for ( int iComment = commentsBeforeData.size() - 1; iComment >= 0; --iComment ) {
					comment = commentsBeforeData.get(iComment).toUpperCase();
					if ( (comment.indexOf("FILEFORMATVERSION") >= 0) ||
						(comment.indexOf("ENDHEADER") >= 0) || (comment.indexOf("--E") >= 0) ) {
						// Remove the comments above the position.
						while ( iComment >= 0 ) {
							commentsBeforeData.remove(iComment--);
						}
						break;
					}
				}
				anOprit.setCommentsBeforeData(commentsBeforeData);
			}
			// Always clear out for next right...
			commentsBeforeData = new Vector(1);

			// line 1
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "line 1: " + iline);
			}
			v = StringUtil.fixedRead(iline, formatLine1);
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, v.toString() );
			}
			anOprit.setID(((String)v.get(0)).trim()); 
			anOprit.setName(((String)v.get(1)).trim()); 
			anOprit.setRtem(((String)v.get(2)).trim());
			dumxFloat = (Float)v.get(3);
			if ( dumxFloat >= 0.0 ) {
				anOprit.setDumx((int)(dumxFloat + .1));  // Add .1 to make sure 11.9999 ends up as 12, etc.
			}
			else {
				anOprit.setDumx((int)(dumxFloat - .1));  // Subtract .1 to make sure 11.9999 ends up as 12, etc.
			}
			dumxInt = anOprit.getDumx();
			anOprit.setIoprsw( (Integer)v.get(4) );
			// Destination data - should always be in file but may be zero...
			anOprit.setCiopde(((String)v.get(5)).trim());
			anOprit.setIopdes(((String)v.get(6)).trim());
			// Supply data - should always be in file but may be zero...
			anOprit.setCiopso1(((String)v.get(7)).trim());
			anOprit.setIopsou1(((String)v.get(8)).trim());
			// Should always be in file but may be zero...
			anOprit.setCiopso2(((String)v.get(9)).trim());
			anOprit.setIopsou2(((String)v.get(10)).trim());
			// Type is used to make additional decisions below...
			anOprit.setItyopr((Integer)v.get(11));
			rightType = anOprit.getItyopr();
			Message.printStatus ( 2, routine, "rightType=" + rightType + " DumxF=" + dumxFloat + " DumxI=" + dumxInt );
			// Plan ID
			anOprit.setCreuse(((String)v.get(12)).trim());
			// Diversion type
			anOprit.setCdivtyp(((String)v.get(13)).trim());
			// Conveyance loss...
			anOprit.setOprLoss ( (Float)v.get(14) );
			// Miscellaneous limits...
			anOprit.setOprLimit ( (Float)v.get(15) );
			oprLimit = anOprit.getOprLimit();
			// Beginning year...
			anOprit.setIoBeg ( (Integer)v.get(16) );
			// Ending year...
			anOprit.setIoEnd ( (Integer)v.get(17) );
			Message.printStatus( 2, routine, "Reading operating rule type " + rightType +
					" starting at line " + linecount );
			
			boolean rightUnderstoodByCode = isRightUnderstoodByCode(rightType);
			
			if ( !rightUnderstoodByCode ) {
				// FIXME SAM 2010-12-11 Need to use config data to know characteristics of right -
				// number of lines?
				// The type is not known so read in as strings and set the type to negative.
				// Most of the reading will occur at the top of the loop.
				readingUnknownRight = true;
				rightStringsList = new Vector();
				rightStringsList.add ( iline );
				// Add list and continue to add if more lines are read.  Since using a reference
				// this will ensure that all lines are set for the right.
				anOprit.setRightStrings ( rightStringsList );
				Message.printWarning ( 2, routine, "Unknown right type " + rightType + " at line " + linecount +
						".  Reading as text to continue reading file." );
				continue;
			}

			// If here the operational right is understood and additional lines of data may be provided.

			nmonsw = 0;
			ninterv = 0;
			
			// May have monthly switch and intervening structures.  For now check the value.
			// FIXME SAM 2008-03-17 Will read in a file that indicates what is allowed so it is
			// easier to dynamically check.
			
			if ( dumxInt == 12 ) {
				// Only have monthly switches
				nmonsw = 12;
			}
			else if ( dumxInt >= 0 ) {
				// Only have intervening structures...
				ninterv = dumxInt;
			}
			else if ( dumxInt < 0 ){
				// Have monthly switches and intervening structures.
				// -12 of the total count toward the monthly switch and the remainder is
				// the number of intervening structures
				// Check the value because some rules like 17 - Rio Grande Compact use -8
				if ( dumxInt < -12 ) {
					ninterv = -1*(dumxInt + 12);
					nmonsw = 12;
				}
				else {
					ninterv = -1*dumxInt;
				}
			}
			
			// FIXME SAM 2008-03-17 Need some more checks for things like invalid -11 and + 13
			
			// Start reading additional information before monthly and intervening structure data)...

			if ( rightType == 17 ) {
				// Rio Grande compact data...
				iline = in.readLine();
				++linecount;
				Message.printStatus ( 2, routine, "Processing operating rule 17 line " + linecount + ": " + iline );
				v = StringUtil.fixedRead(iline, format_17);
				anOprit.setQdebt( (Float)v.get(0) );
				anOprit.setQdebtx( (Float)v.get(1) );
				anOprit.setCiopso3(	((String)v.get(2)).trim());
				anOprit.setIopsou3(	((String)v.get(3)).trim());
				anOprit.setCiopso4(	((String)v.get(4)).trim());
				anOprit.setIopsou4(	((String)v.get(5)).trim());
				anOprit.setCiopso5(	((String)v.get(6)).trim());
				anOprit.setIopsou5(	((String)v.get(7)).trim());
			}
			else if ( rightType == 18 ) {
				// Rio Grande Compact - Conejos River
				iline = in.readLine();
				++linecount;
				Message.printStatus ( 2, routine, "Processing operating rule 18 line " + linecount + ": " + iline );
				v = StringUtil.fixedRead(iline, format_17);
				anOprit.setQdebt( (Float)v.get(0) );
				anOprit.setQdebtx( (Float)v.get(1) );
				anOprit.setCiopso3(	((String)v.get(2)).trim());
				anOprit.setIopsou3(	((String)v.get(3)).trim());
				anOprit.setCiopso4(	((String)v.get(4)).trim());
				anOprit.setIopsou4(	((String)v.get(5)).trim());
				anOprit.setCiopso5(	((String)v.get(6)).trim());
				anOprit.setIopsou5(	((String)v.get(7)).trim());
			}
			else if ( rightType == 20 ) {
				// San Juan RIP...
				iline = in.readLine();
				++linecount;
				Message.printStatus ( 2, routine, "Processing operating rule 20 line " + linecount + ": " + iline );
				v = StringUtil.fixedRead(iline, format_20);
				anOprit.setSjmina( (Float)v.get(0));
				anOprit.setSjrela( (Float)v.get(1));
			}
			
			// ...end reading additional data before monthly and intervening structure data
			
			// Start reading the monthly and intervening structure data...
			
			Message.printStatus ( 2, routine, "Dumx=" + dumxInt + ", number of intervening structures = " +
				ninterv + " month switch = " + nmonsw );
			if ( nmonsw > 0 ) {
				errorCount += readStateModFile_MonthlySwitches ( nmonsw, routine, linecount, in, anOprit );
				++linecount;
			}
			// Only read intervening structures if allowed (otherwise assume user error in input)
			// TODO SAM 2010-12-13 can metadata carriers allowed be checked here (same as intervening structures)?
			if ( (ninterv > 0) && (rightType != 17) && (rightType != 18) ) {
				errorCount += readStateModFile_InterveningStructures (
					ninterv, routine, linecount, in, anOprit );
				++linecount;
			}
			
			// ...end reading monthly and intervening structure data.
			
			// Start reading additional data after monthly and intervening structure data...
			
			if ( rightType == 10 ) {
				// Monthly and annual limitation data...
				if ( (oprLimit > 0) ) {
					errorCount += readStateModFile_MonthlyAndAnnualLimitationData (
						routine, linecount, in, anOprit );
					++linecount;
				}
			}
			
			// ...end reading additional data after monthly and intervening structure data

			// add the operational right to the vector of rights
			Message.printStatus ( 2, routine, "Adding recognized operational right type " + rightType +
			    " \"" + anOprit.getID() + "\" from full read.");
			theOprits.add(anOprit);
		}
		// All lines have been read.
		if ( readingUnknownRight ) {
			// Last line was part of the unknown right so need to add what there was.
			Message.printStatus ( 2, routine, "Adding unrecognized operational right type " + rightType +
				" \"" + anOprit.getID() + "\" as text.");
			theOprits.add(anOprit);
		}
	}
	catch (Exception e) {
		Message.printWarning(3, routine, "Error reading near line " + linecount + ": " + iline);
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	// If there were any errors, generate an exception
	if ( errorCount > 0 ) {
		throw new Exception ( "There were " + errorCount + " errors reading the operational rights." );
	}
	return theOprits;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_OperationalRight op = (StateMod_OperationalRight)_original;
	super.restoreOriginal();
	_rtem = op._rtem;
	_dumx = op._dumx;
	_ciopde = op._ciopde;
	_iopdes = op._iopdes;
	_ciopso1 = op._ciopso1;
	_iopsou1 = op._iopsou1;
	_ciopso2 = op._ciopso2;
	_iopsou2 = op._iopsou2;
	_ciopso3 = op._ciopso3;
	_iopsou3 = op._iopsou3;
	_ciopso4 = op._ciopso4;
	_iopsou4 = op._iopsou4;
	_ciopso5 = op._ciopso5;
	_iopsou5 = op._iopsou5;
	_ityopr = op._ityopr;
	_qdebt = op._qdebt;
	_qdebtx = op._qdebtx;
	_sjmina = op._sjmina;
	_sjrela = op._sjrela;
	_imonsw = op._imonsw;
	_intern = op._intern;
	// Newer data..
	__creuse = op.__creuse;
	__cdivtyp = op.__cdivtyp;
	__oprLoss = op.__oprLoss;
	__oprLimit = op.__oprLimit;
	__ioBeg = op.__ioBeg;
	__ioEnd = op.__ioEnd;
	_isClone = false;
	_original = null;
}

/**
Set the cdivtyp.
*/
public void setCdivtyp(String cdivtyp) {
	if ( (cdivtyp != null) && !cdivtyp.equals(__cdivtyp) ) {
		__cdivtyp = cdivtyp;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS,true);
		}
	}
}

/**
Set the user ciopde.
*/
public void setCiopde(String ciopde) {
	if ( (ciopde != null) && !ciopde.equals(_ciopde) ) {
		_ciopde = ciopde;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS,true);
		}
	}
}

/**
Set the user ciopso.
*/
public void setCiopso1(String ciopso1) {
	if ( (ciopso1 != null) && !ciopso1.equals(_ciopso1)) {
		_ciopso1 = ciopso1;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the user ciopso2.
*/
public void setCiopso2(String ciopso2) {
	if ( (ciopso2 != null) && !ciopso2.equals(_ciopso2)) {
		_ciopso2 = ciopso2;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the user ciopso3.
*/
public void setCiopso3(String ciopso3) {
	if ( (ciopso3 != null) && !ciopso3.equals(_ciopso3)) {
		_ciopso3 = ciopso3;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the user ciopso4.
*/
public void setCiopso4(String ciopso4) {
	if ( (ciopso4 != null) && !ciopso4.equals(_ciopso4)) {
		_ciopso4 = ciopso4;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the user ciopso5.
*/
public void setCiopso5(String ciopso5) {
	if ( (ciopso5 != null) && !ciopso5.equals(_ciopso5)) {
		_ciopso5 = ciopso5;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the comments before the data in the input file.
@param commentsBeforeData comments before the data in the input file.
*/
public void setCommentsBeforeData(List<String> commentsBeforeData)
{	boolean dirty = false;
	int size = commentsBeforeData.size();
	List<String> commentsBeforeData0 = getCommentsBeforeData();
	if ( size != commentsBeforeData0.size() ) {
		dirty = true;
	}
	else {
		// Lists are the same size and there may not have been any changes
		// Need to check each string in the comments
		for ( int i = 0; i < size; i++ ) {
			if ( !commentsBeforeData.get(i).equals(commentsBeforeData0.get(i))) {
				dirty = true;
				break;
			}
		}
	}
	if ( dirty ) {
		// Something was different so set the comments and change the dirty flag
		__commentsBeforeData = commentsBeforeData;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS,true);
		}
	}
}

/**
Set the creuse.
*/
public void setCreuse(String creuse) {
	if ( (creuse != null) && !creuse.equals(__creuse) ) {
		__creuse = creuse;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS,true);
		}
	}
}

/**
Set the cx.
*/
public void setCx(String cx) {
	if ( (cx != null) && !cx.equals(__cx) ) {
		__cx = cx;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS,true);
		}
	}
}

/**
Set dumx
*/
public void setDumx(int dumx) {
	if (dumx != _dumx) {
		_dumx = dumx;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set dumx
*/
public void setDumx(Integer dumx) {
	setDumx(dumx.intValue());
}

/**
Set dumx.  Note that sometimes the integer has a . at the end.  To resolve this,
convert to a double and then cast as an integer.
*/
public void setDumx(String dumx)
{	if (dumx != null) {
		Double d = (Double.parseDouble(dumx.trim()));
		setDumx((int)d.doubleValue());
	}
}

/**
Set ioBeg
*/
public void setIoBeg(int ioBeg) {
	if (ioBeg != __ioBeg) {
		__ioBeg = ioBeg;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set ioBeg
*/
public void setIoBeg(Integer ioBeg) {
	setIoBeg(ioBeg.intValue());
}

/**
Set ioBeg.
*/
public void setIoBeg(String ioBeg)
{	if (ioBeg != null) {
		setIoBeg((int)(StringUtil.atoi(ioBeg.trim())));
	}
}

/**
Set ioEnd
*/
public void setIoEnd(int ioEnd) {
	if (ioEnd != __ioEnd) {
		__ioEnd = ioEnd;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set ioEnd
*/
public void setIoEnd(Integer ioEnd) {
	setIoEnd(ioEnd.intValue());
}

/**
Set ioEnd.
*/
public void setIoEnd(String ioEnd)
{	if (ioEnd != null) {
		setIoEnd((int)(StringUtil.atoi(ioEnd.trim())));
	}
}

/**
Set a monthly switch.
*/
public void setImonsw(int index, int imonsw) {
	if (imonsw != _imonsw[index]) {
		_imonsw[index] = imonsw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set a monthly switch.
*/
public void setImonsw(int index, Integer imonsw) {
	setImonsw(index, imonsw.intValue());
}

/**
Set a monthly switch.
*/
public void setImonsw(int index, String imonsw) {
	if (imonsw != null) {
		setImonsw(index, Integer.parseInt(imonsw.trim()));
	}
}

/**
Set an "intern".
@param adjustDumx if true, adjust the dumx by increasing the count of intern.  If false, just set the
intervening ID but do not change dumX.
*/
public void setIntern(int index, String intern, boolean adjustDumx) {
	if (intern == null) {
		return;
	}
	if (!intern.equals(_intern[index])) {
		// Only set if not already set - otherwise will trigger dirty flag
		_intern[index] = intern;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
		if (Message.isDebugOn) {
			Message.printDebug(30, 
				"StateMod_OperationalRight.setIntern", "Old Dumx: " + getDumx()+ ", New Dumx: " + index+1);
		}
		if ( adjustDumx ) {
			if (index+1 > getDumx()) {
				// Need to increment dumX
				// FIXME SAM 2010-12-13 Is this code needed and deal with negatives
				setDumx(index+1);
			}
		}
		if (Message.isDebugOn) {
			Message.printDebug(30, "StateMod_OperationalRight.setInter", "Dumx: " + getDumx());
		}
	}
}

/**
Sets the interns from a list.
*/
public void setInterns(List<String> v) {
	for (int i = 0; i < 10; i++) {
		setIntern(i, v.get(i), false);
	}
}

/**
Set the iopdes.
*/
public void setIopdes(String iopdes) {
	if ( (iopdes != null) && !iopdes.equals(_iopdes) ) {
		_iopdes = iopdes;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the ioprsw - this calls setSwitch() in the base class.
*/
public void setIoprsw(Integer ioprsw) {
	setSwitch(ioprsw.intValue());
}

/**
Set the iopsou1.
*/
public void setIopsou1(String iopsou1) {
	if ( (iopsou1 != null) && !iopsou1.equals(_iopsou1) ) {
		_iopsou1 = iopsou1;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the iopsou2.
*/
public void setIopsou2(String iopsou2) {
	if ( (iopsou2 != null) && !iopsou2.equals(_iopsou2) ) {
		_iopsou2 = iopsou2;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the iopsou3.
*/
public void setIopsou3(String iopsou3) {
	if ( (iopsou3 != null) && !iopsou3.equals(_iopsou3) ) {
		_iopsou3 = iopsou3;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the iopsou4.
*/
public void setIopsou4(String iopsou4) {
	if ( (iopsou4 != null) && !iopsou4.equals(_iopsou4) ) {
		_iopsou4 = iopsou4;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the iopsou5.
*/
public void setIopsou5(String iopsou5) {
	if ( (iopsou5 != null) && !iopsou5.equals(_iopsou5) ) {
		_iopsou5 = iopsou5;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the ityopr
*/
public void setItyopr(int ityopr) {
	if (ityopr != _ityopr) {
		_ityopr = ityopr;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set the ityopr
*/
public void setItyopr(Integer ityopr) {
	setItyopr(ityopr.intValue());
}

/**
Set the ityopr
*/
public void setItyopr(String ityopr) {
	if (ityopr != null) {
		setItyopr(Integer.parseInt(ityopr.trim()));
	}
}

/**
Set oprLimit
*/
public void setOprLimit(double oprLimit) {
	if (oprLimit != __oprLimit) {
		__oprLimit = oprLimit;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set oprLoss
*/
public void setOprLoss(double oprLoss) {
	if (oprLoss != __oprLoss) {
		__oprLoss = oprLoss;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set qdebt
*/
public void setQdebt(double qdebt) {
	if (qdebt != _qdebt) {
		_qdebt = qdebt;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set qdebt
*/
public void setQdebt(Double qdebt) {
	setQdebt(qdebt.doubleValue());
}

/**
Set qdebt
*/
public void setQdebt(String qdebt) {
	if (qdebt != null) {
		setQdebt(Double.parseDouble(qdebt.trim()));
	}
}

/**
Set qdebtx
*/
public void setQdebtx(double qdebtx) {
	if (qdebtx != _qdebtx) {
		_qdebtx = qdebtx;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set qdebtx
*/
public void setQdebtx(Double qdebtx) {
	setQdebtx(qdebtx.doubleValue());
}

/**
Set qdebtx
*/
public void setQdebtx(String qdebtx) {
	if (qdebtx != null) {
		setQdebtx(Double.parseDouble(qdebtx.trim()));
	}
}

/**
Set the operating rule strings, when read as text because an unknown right type.
*/
private void setRightStrings ( List<String> right_strings_Vector )
{
	__rightStringsVector = right_strings_Vector;
}

/**
Set rtem
*/
public void setRtem(String rtem) {
	if (!_rtem.equals(rtem)) {
		_rtem = rtem;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set sjmina
*/
public void setSjmina(double sjmina) {
	if (sjmina != _sjmina) {
		_sjmina = sjmina;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set sjmina
*/
public void setSjmina(Double sjmina) {
	setSjmina(sjmina.doubleValue());
}

/**
Set sjmina
*/
public void setSjmina(String sjmina) {
	if (sjmina != null) {
		setSjmina(Double.parseDouble(sjmina.trim()));
	}
}

/**
Set sjrela
*/
public void setSjrela(double sjrela) {
	if (sjrela != _sjrela) {
		_sjrela = sjrela;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
	}
}

/**
Set sjrela
*/
public void setSjrela(Double sjrela) {
	setSjrela(sjrela.doubleValue());
}

/**
Set sjrela
*/
public void setSjrela(String sjrela) {
	if (sjrela != null) {
		setSjrela(Double.parseDouble(sjrela.trim()));
	}
}


public void setupImonsw() {	
	_imonsw = new int[12];
	for (int i = 0; i < 12; i++) {
		_imonsw[i] = 0;
	}
}

/**
Write operational right information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param formatVersion the StateMod operational rights format version (1 or 2)
@param theOpr list of operational right to write
@param newComments addition comments that should be included at the top of the file
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile, int formatVersion,
	List<StateMod_OperationalRight> theOpr, List<String> newComments)
throws Exception {
	if ( formatVersion == 1 ) {
		writeStateModFileVersion1( infile,  outfile, theOpr, newComments);
	}
	else if ( formatVersion == 2 ) {
		writeStateModFileVersion2( infile,  outfile, theOpr, newComments);
	}
}

/**
Write operational right information to output for version 1 format file.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theOpr vector of operational right to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFileVersion1(String infile, String outfile,
	List<StateMod_OperationalRight> theOpr, List<String> newComments)
throws Exception {
	PrintWriter	out = null;
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String routine = "StateMod_OperationalRight.writeStateModFileVersion1";

	Message.printStatus(1, routine, "Writing new operational rights to file \""
		+ outfile + "\" using \"" + infile + "\" header...");

	out = IOUtil.processFileHeaders(infile, outfile, newComments, commentIndicators, ignoredCommentIndicators, 0);
	try {
		String cmnt = "#>";
		String iline = null;
		String format = "%-12.12s%-36.36s%16.16s%7d.%8d %-12.12s%8d %-12.12s%8d %-12.12s%8d%8d";
		String formatS = "         %d %d %d %d %d %d %d %d %d %d %d %d";
		String formatsp = "%36.36s";
		String formatI = "%-12.12s";
		StateMod_OperationalRight opr = null;
		List v = new Vector(12);
		List vS = new Vector(12);
		List vsp = new Vector(1);
		List vI = new Vector(1);
		List comments_vector = null;
	
		out.println(cmnt);
		out.println(cmnt + " *******************************************************");
 		out.println(cmnt + " Operational Right File");
 		out.println(cmnt);
		out.println(cmnt + "     Card 1   Control");
		out.println(cmnt + "     format:  (a12, a24, 12x, 2i4, i8, f8.0, i8, 3 (i8,a12), 20i8)");
 		out.println(cmnt);
		out.println(cmnt + "     ID       cidvri:          Operational Right ID");
		out.println(cmnt + "     Name     nameo:           Operational Right name");
		out.println(cmnt + "     AdminDat iodat (1-2,k):   Effective priority date");
		out.println(cmnt + "     Admin #  irtem:           Priority Number (smaller is most senior)");
		out.println(cmnt + "     # Str    dumx:            Number of intervenging structures ");
		out.println(cmnt + "     On/Off   ioprsw (k):      Switch 0 = off,1 = on");
		out.println(cmnt + "     Dest ID  ciopde:          Destination reservoir or structure ID");
		out.println(cmnt + "     Dest Ac  iopdes (2,k):    Destination reservoir or structure account # (1 for a diversion)");
		out.println(cmnt + "     Sou1 ID  ciopso (1)       Supply reservoir #1 or structure ID");
		out.println(cmnt + "     Sou1 Ac  iopsou (2,k):    Supply reservoir #1 or structure account # (1 for a diversion)");
		out.println(cmnt + "     Sou2 ID  ciopso (2):      Supply reservoir #2 ID");
		out.println(cmnt + "     Sou1 Ac  iopsou (4,k):    Supply reservoir #2 account");
		out.println(cmnt + "     Type     ityopr (k):      Switch");
		out.println(cmnt + "              1 = Reservoir Release to an instream demand");
		out.println(cmnt + "              2 = Reservoir Release to a direct diversion demand");
		out.println(cmnt + "              3 = Reservoir Release to a direct diversion demand by a carrier");
		out.println(cmnt + "              4 = Reservoir Release to a direct diversion demand by exchange");
		out.println(cmnt + "              5 = Reservoir Release to a reservoir by exchange");
		out.println(cmnt + "              6 = Reservoir to reservoir bookover");
		out.println(cmnt + "              7 = Reservoir Release to a carrier exchange");
		out.println(cmnt + "              8 = Out-of-Priority Reservoir Storage");
		out.println(cmnt + "              9 = Reservoir Release for target contents");
		out.println(cmnt + "              10 = General Replacement Reservoir");
		out.println(cmnt + "              11 = Direct flow demand thru intervening structures");
		out.println(cmnt + "              12 = Reoperate");
		out.println(cmnt + "              13 = Index Flow");
		out.println(cmnt + "              14 = Similar to 11 but diversions are constrained by demand at carrier structure");
		out.println(cmnt + "              15 = Interruptible Supply");
		out.println(cmnt + "              16 = Direct Flow Storage");

		out.println(cmnt);
		out.println(cmnt + " *************************************************************************");
		out.println(cmnt + "     Card 2   Carrier Ditch data (include only if dumx > 0)");
		out.println(cmnt + "     format:  (free)");
		out.println(cmnt);
		out.println(cmnt + "     Inter    itern (1,j)     intervening direct diversion structure id's");
		out.println(cmnt + "                              Enter # Str values");
		out.println(cmnt);
		out.println(cmnt + " ID        Name                    "
			+ "NA          AdminDat  Admin#   # Str  On/Off Dest "
			+ "Id     Dest Ac  Sou1 Id     Sou1 Ac  Sou2 Id     Sou2 Ac     Type");
		out.println(cmnt + "---------eb----------------------e"
			+ "b----------eb------eb------eb------eb------e-b----------eb------e-b----------eb------e-b------"
			+ "----eb------eb------e");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);

		int num = 0;
		if (theOpr != null) {
			num = theOpr.size();
		}
		int num_intern;
		int num_comments;
		int dumx;
		for (int i = 0; i < num ; i++) {
			opr = (StateMod_OperationalRight)theOpr.get(i);
			if (opr == null) {
				continue;
			}

			comments_vector = opr.getCommentsBeforeData();
			num_comments = comments_vector.size();
			// Print the comments in front of the operational right
			for (int j = 0; j < num_comments; j++) {
				out.println("# " + (String)comments_vector.get(j));
			}
			// If the operational right was not understood at read, print the original contents
			// and go to the next right.
			List rightStringsList = opr.getRightStrings();
			if ( rightStringsList != null ) {
				for ( int j = 0; j < rightStringsList.size(); j++ ) {
					out.println ( rightStringsList.get(j) );
				}
				continue;
			}

			v.clear();
			v.add(opr.getID());
			v.add(opr.getName());
			v.add(opr.getCgoto());
			v.add(new Integer(opr.getDumx()));
			v.add(new Integer(opr.getSwitch()));
			v.add(opr.getCiopde());
			v.add(new Integer(opr.getIopdes()));
			v.add(opr.getCiopso1());
			v.add(new Integer(opr.getIopsou1()));
			v.add(opr.getCiopso2());
			v.add(new Integer(opr.getIopsou2()));
			v.add(new Integer(opr.getItyopr()));
			iline = StringUtil.formatString(v, format);
			out.println(iline);
			dumx = opr.getDumx();

			if ((dumx == 12) || (dumx < -12)) {
				if (Message.isDebugOn) {
					Message.printDebug(50, routine,
						"in area 1: getDumx = " + opr.getDumx()+ "getItyopr = " + opr.getItyopr());
				}
				vS.clear();
				for (int j = 0; j < 12; j++) {
					vS.add(new Integer( opr.getImonsw(j)));
				}
				iline = StringUtil.formatString(vS, formatS);
				out.println(iline);
			}

			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "in area 3 (" + opr.getID() + "): getDumx = " + opr.getDumx()
					+ ", getItyopr = " + opr.getItyopr());
			}
			if ((dumx > 0 && dumx <= 10)|| dumx < -12) {
				if (Message.isDebugOn) {
					Message.printDebug(50, routine,
						"in area 2: getDumx = " + opr.getDumx() + "getItyopr = " + opr.getItyopr());
				}
				vsp.clear();
				vsp.add(" ");
				iline = StringUtil.formatString(vsp, formatsp);
				out.print(iline);

				num_intern = opr.getDumx();
				if (opr.getDumx() < -12) {
					num_intern = -12 - opr.getDumx();
				}
				for (int j = 0; j < num_intern; j++) {
					if (Message.isDebugOn) {
						Message.printDebug(50, routine, "in area 3: " + num_intern);
					}
					vI.clear();
					vI.add(opr.getIntern(j));
					iline = StringUtil.formatString( vI, formatI);
					out.print(iline);
				}
				out.println();
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
	}
}

/**
Write operational right information to output for version 2 format file.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theOpr vector of operational right to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFileVersion2(String infile, String outfile,
	List<StateMod_OperationalRight> theOpr, List<String> newComments)
throws Exception {
	PrintWriter	out = null;
	List commentIndicators = new Vector(1);
	commentIndicators.add ( "#" );
	List ignoredCommentIndicators = new Vector(1);
	ignoredCommentIndicators.add ( "#>");
	String routine = "StateMod_OperationalRight.writeStateModFileVersion2";

	Message.printStatus(1, routine, "Writing new operational rights to file \""
		+ outfile + "\" using \"" + infile + "\" header...");

	out = IOUtil.processFileHeaders(infile, outfile, newComments, commentIndicators, ignoredCommentIndicators, 0);
	try {
		String cmnt = "#>";
		String iline = null;
		// Note that dumx is output as a string to force the trailing period
		String formatLine1 = "%-12.12s%-24.24s                %12.12s%8.8s%8d %-12.12s%8d %-12.12s%8d %-12.12s%8d" +
			"%8d %-12.12s %-12.12s %8.0f%8.0f%8d%8d";
		// The spaces in the following follow traditional file formatting and allow input and output to be compared
		String formatMonthlySwitches = "                                    %d %d %d %d %d %d %d %d %d %d %d %d";
		String formatsp = "%36.36s";
		String formatI = "%-12.12s";
		StateMod_OperationalRight opr = null;
		List v = new Vector(18);
		List vMonthlySwitches = new Vector(12);
		List vsp = new Vector(1);
		List vI = new Vector(1);
		List<String> commentsBeforeData = null;
	
		out.println(cmnt);
		out.println(cmnt + " *******************************************************");
 		out.println(cmnt + " Operational Right (Operating Rule) File");
 		out.println(cmnt + "" );
		/*
 		out.println(cmnt);
		out.println(cmnt + "     Card 1   Control");
		out.println(cmnt + "     format:  (a12, a24, 12x, 2i4, i8, f8.0, i8, 3 (i8,a12), 20i8)");
 		out.println(cmnt);
		out.println(cmnt + "     ID       cidvri:          Operational Right ID");
		out.println(cmnt + "     Name     nameo:           Operational Right name");
		out.println(cmnt + "     AdminDat iodat (1-2,k):   Effective priority date");
		out.println(cmnt + "     Admin #  irtem:           Priority Number (smaller is most senior)");
		out.println(cmnt + "     # Str    dumx:            Number of intervenging structures ");
		out.println(cmnt + "     On/Off   ioprsw (k):      Switch 0 = off,1 = on");
		out.println(cmnt + "     Dest ID  ciopde:          Destination reservoir or structure ID");
		out.println(cmnt + "     Dest Ac  iopdes (2,k):    Destination reservoir or structure account # (1 for a diversion)");
		out.println(cmnt + "     Sou1 ID  ciopso (1)       Supply reservoir #1 or structure ID");
		out.println(cmnt + "     Sou1 Ac  iopsou (2,k):    Supply reservoir #1 or structure account # (1 for a diversion)");
		out.println(cmnt + "     Sou2 ID  ciopso (2):      Supply reservoir #2 ID");
		out.println(cmnt + "     Sou1 Ac  iopsou (4,k):    Supply reservoir #2 account");
		out.println(cmnt + "     Type     ityopr (k):      Switch");

		out.println(cmnt);
		out.println(cmnt + " *************************************************************************");
		out.println(cmnt + "     Card 2   Carrier Ditch data (include only if dumx > 0)");
		out.println(cmnt + "     format:  (free)");
		out.println(cmnt);
		out.println(cmnt + "     Inter    itern (1,j)     intervening direct diversion structure id's");
		out.println(cmnt + "                              Enter # Str values");
		out.println(cmnt);
		out.println(cmnt + " ID        Name                    "
			+ "NA          AdminDat  Admin#   # Str  On/Off Dest "
			+ "Id     Dest Ac  Sou1 Id     Sou1 Ac  Sou2 Id     Sou2 Ac     Type");
		out.println(cmnt + "---------eb----------------------e"
			+ "b----------eb------eb------eb------eb------e-b----------eb------e-b----------eb------e-b------"
			+ "----eb------eb------e");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);*/
		
		out.println(cmnt );
		out.println(cmnt + "           OPERATING RULE TYPES (types 1 through " + MAX_HANDLED_TYPE +
			" are understood by the software that wrote this file)" );
		out.println(cmnt + "  =======================================================================================================================" );
		for ( StateMod_OperationalRight_Metadata metadata: StateMod_OperationalRight_Metadata.getAllMetadata() ) {
			out.println(cmnt + "	" + StringUtil.formatString(metadata.getRightTypeNumber(),"%2d") +
				"   " + metadata.getRightTypeName() );
		}
		out.println(cmnt + "");
		out.println(cmnt + "            GUIDE TO COLUMN ENTRIES (see StateMod documentation for details, using variable names)" );
		out.println(cmnt + "  =======================================================================================================================" );
		out.println(cmnt + "   ID         cidvri     Unique identifier for the operating rule, used in output in the *.xop output file" );
		out.println(cmnt + "   Name       nameo      Name of operating rule - used for descriptive purposes only" );
		out.println(cmnt + "   Admin #    irtem      Administration number used to determine priority of operational water rights relative to other" );
		out.println(cmnt + "                         operations and direct diversion, reservoir, instream flow, and well rights" );
		out.println(cmnt + "                         (see tabulation in *.xwr output file)" );
		out.println(cmnt + "   # Str      dumx       Number of carrier structures, monthly on/off switches, or monthly volumetrics" );
		out.println(cmnt + "                         (flag telling StateMod program the number of entries on next lines)" );
		out.println(cmnt + "   On/Off     ioprsw     1 for ON and 0 for OFF" );
		out.println(cmnt + "   Dest ID    ciopde     Destination of operating rule whose demand is to be met by simulating the operating rule" );
		out.println(cmnt + "   Dest Ac    iopdes     Account at destination to be met by operating rule - typically 1 for a diversion structure" );
		out.println(cmnt + "                         and account number for reservoir destination" );
		out.println(cmnt + "   Sou1 ID    ciopso(1)  ID number of primary source of water under which water right is being diverted in operating rule - " );
		out.println(cmnt + "                         typically a water right, reservoir, or Plan structure ID" );
		out.println(cmnt + "   Sou1 Ac    iopsou(1)  Account of Sou1 - typically 1 for a diversion structure and account number for reservoir source" );
		out.println(cmnt + "   Sou2 ID    ciopso(2)  ID of Plan where reusable storage water or reusable ditch credits is accounted" );
		out.println(cmnt + "   Sou2 Ac    iopsou(2)  Percentage of Plan supplies available for operation" );
		out.println(cmnt + "   Type       ityopr     Rule type corresponding with definitions in StateMod documentation (see list above" );	
		out.println(cmnt + "                         Note that this data processing software explicitly understands up to type " + MAX_HANDLED_TYPE );
		out.println(cmnt + "                         Other rule types are read as text assuming that each operating rule has comments above the data." );
		out.println(cmnt + "   ReusePlan  creuse     ID of Plan where reusable return flows or diversions to storage are accounted" );
		out.println(cmnt + "   Div Type   cdivtyp    'Diversion' indicates pro-rata diversion of source water right priority or exchange of reusable credits to Dest1" );
		out.println(cmnt + "                         'Depletion' indicates pro-rata diversion of source water right priority consumptive use or augmentation of upstream diversions at Dest1" );
		out.println(cmnt + "   OprLoss    OprLoss    Percentage of simulated diversion lost in carrier ditch (only applies to certain rules - see StateMod documentation, Section 4.13)" );
		out.println(cmnt + "   Limit      OprLimit   Capacity limit for carrier structures different from capacity in .dds file (used to represent constricted conveyance capacity for winter deliveries to reservoirs)" );
		out.println(cmnt + "   Year1      IoBeg      First year the operating rule is on." );
		out.println(cmnt + "   Year2      IoEnd      Last year the operating rule is on." );
		out.println(cmnt + "" );
		out.println(cmnt + " Note - StateMod supports several *.opr file format versions.  The following string indicates the version for this file:" );
		out.println(cmnt + "" );
		out.println(cmnt + " FileFormatVersion 2" );
		out.println(cmnt + "" );
		out.println(cmnt + " If the format version indicator is not provided StateMod will try to determine the version." );
		out.println(cmnt + "" );
		out.println(cmnt + " Card 1 format:  a12,a24,12x,4x,f12.5,f8.0,i8,3(1x,a12,i8),i8,1x,a12,1x,a12,1x,2f8.0,2i8" );
		out.println(cmnt + "" );
		out.println(cmnt + " ID        Name                    NA                    Admin#   # Str  On/Off Dest Id     Dest Ac  Sou1 Id     Sou1 Ac  Sou2 Id     Sou2 Ac     Type ReusePlan    Div Type      OprLoss   Limit   Year1   Year 2" );
		out.println(cmnt + " ---------eb----------------------eb----------exxxxb----------eb------eb------e-b----------eb------e-b----------eb------e-b----------eb------eb------exb----------exb----------exb------eb------exb------eb--------e" );
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
		
		int num = 0;
		if (theOpr != null) {
			num = theOpr.size();
		}
		int num_intern;
		int numComments;
		int dumx;
		int ruleType;
		double oprLimit;
		for (int i = 0; i < num ; i++) {
			opr = theOpr.get(i);
			if (opr == null) {
				continue;
			}
			ruleType = opr.getItyopr();

			commentsBeforeData = opr.getCommentsBeforeData();
			numComments = commentsBeforeData.size();
			// Print the comments in front of the operational right
			// The original comments were stripped of the leading # but otherwise are padded with whitespace
			// as per the original - when written they should exactly match the original
			for (int j = 0; j < numComments; j++) {
				out.println("#" + commentsBeforeData.get(j));
			}
			if ( !isRightUnderstoodByCode(opr.getItyopr()) ) {
				// The operational right is not explicitly understood so print the original contents
				// and go to the next right
				List<String> rightStringsList = opr.getRightStrings();
				if ( rightStringsList != null ) {
					for ( int j = 0; j < rightStringsList.size(); j++ ) {
						out.println ( rightStringsList.get(j) );
					}
				}
			}
			else {
				// Print the rights details using the in-memory information
				v.clear();
				v.add(opr.getID());
				v.add(opr.getName());
				v.add(opr.getRtem());
				// Dumx is handled as a float even though it is documented as having integer values
				// Traditionally it has a period in the files (e.g., "12.").  Therefore, force the period here
				String dumxString = "" + opr.getDumx() + ".";
				v.add(dumxString);
				v.add(new Integer(opr.getSwitch()));
				v.add(opr.getCiopde());
				v.add(opr.getIopdes());
				v.add(opr.getCiopso1());
				v.add(opr.getIopsou1());
				v.add(opr.getCiopso2());
				v.add(opr.getIopsou2());
				v.add(new Integer(ruleType));
				v.add(opr.getCreuse());
				v.add(opr.getCdivtyp());
				v.add(new Double(opr.getOprLoss()));
				oprLimit = opr.getOprLimit();
				v.add(new Double(oprLimit));
				v.add(new Integer(opr.getIoBeg()));
				v.add(new Integer(opr.getIoEnd()));
				iline = StringUtil.formatString(v, formatLine1);
				out.println(iline);
				dumx = opr.getDumx();
				
				// Write records before monthly switches and intervening structures
				
				if ( (ruleType == 17) || (ruleType == 18) ) {
					out.println ( "                                                                " +
						StringUtil.formatString(opr.getQdebt(), "%7.0f.") +
						StringUtil.formatString(opr.getQdebtx(), "%7.0f.") + " " +
						StringUtil.formatString(opr.getCiopso3(), "%-12.12s") +
						StringUtil.formatString(opr.getIopsou3(), "%8.8s") + " " +
						StringUtil.formatString(opr.getCiopso4(), "%-12.12s") +
						StringUtil.formatString(opr.getIopsou4(), "%8.8s") + " " +
						StringUtil.formatString(opr.getCiopso5(), "%-12.12s") +
						StringUtil.formatString(opr.getIopsou5(), "%8.8s") );
				}
				else if ( ruleType == 20 ) {
					out.println ( "                                                                " +
							StringUtil.formatString(opr.getSjmina(), "%8.0f") +
							StringUtil.formatString(opr.getSjrela(), "%8.0f") );
				}
				
				// Write the monthly switches if used
	
				if ((dumx == 12) || (dumx < -12)) {
					if (Message.isDebugOn) {
						Message.printDebug(50, routine,
							"in area 1: getDumx = " + opr.getDumx()+ "getItyopr = " + opr.getItyopr());
					}
					vMonthlySwitches.clear();
					for (int j = 0; j < 12; j++) {
						vMonthlySwitches.add(new Integer( opr.getImonsw(j)));
					}
					iline = StringUtil.formatString(vMonthlySwitches, formatMonthlySwitches);
					out.println(iline);
				}
				
				// Write the intervening structures if used
	
				if (Message.isDebugOn) {
					Message.printDebug(50, routine, "in area 3 (" + opr.getID() + "): getDumx = " + opr.getDumx()
						+ ", getItyopr = " + opr.getItyopr());
				}
				if ( (ruleType != 17) && (ruleType != 18) ) {
					if ((dumx > 0 && dumx <= 10)|| dumx < -12) {
						if (Message.isDebugOn) {
							Message.printDebug(50, routine,
								"in area 2: getDumx = " + opr.getDumx() + "getItyopr = " + opr.getItyopr());
						}
						vsp.clear();
						vsp.add(" ");
						iline = StringUtil.formatString(vsp, formatsp);
						out.print(iline);
		
						num_intern = opr.getDumx();
						if (opr.getDumx() < -12) {
							num_intern = -12 - opr.getDumx();
						}
						for (int j = 0; j < num_intern; j++) {
							if (Message.isDebugOn) {
								Message.printDebug(50, routine, "in area 3: " + num_intern);
							}
							vI.clear();
							vI.add(opr.getIntern(j));
							iline = StringUtil.formatString( vI, formatI);
							out.print(iline);
						}
						out.println();
					}
				}
				
				// Write more records for specific operating rules
				
				if ( ruleType == 10 ) {
					if ( oprLimit > 0.0 ) {
						writeStateModFile_MonthlyAndAnnualLimitationData ( routine, out, opr );
					}
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
	}
}

/**
WRite the StateMod operational rights file monthly and annual limitation data.
@param routine to use for logging.
@param linecount Line count (1+) before writing in this method.
@param out BufferedWriter to write.
@param anOprit Operational right for which to write data.
@return the number of errors.
@exception IOException if there is an error writing the file
*/
private static int writeStateModFile_MonthlyAndAnnualLimitationData ( String routine,
		PrintWriter out, StateMod_OperationalRight anOprit )
throws IOException
{	// Single identifier
	int errorCount = 0;
	try {
		out.println ( "                                    " + anOprit.getCx().trim() );
	}
	catch ( Exception e ) {
		// TODO SAM 2010-12-13 Need to handle errors and provide feedback
		Message.printWarning(3, routine, "Error writing monthly and annual limitation for right \"" +
			anOprit.getID() + "\"" );
	}
	return errorCount;
}

}