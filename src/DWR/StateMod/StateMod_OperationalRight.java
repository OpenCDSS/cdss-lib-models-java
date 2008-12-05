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
Operational right types using reasonably short phrases,
*/
public static final String[] NAMES = {
	"Unknown",	// 0, Use when the right number is outside the supported range
	"Reservoir Release to an Instream Flow",		// 1
	"Reservoir Release to a Diversion by the River",	// 2
	"Reservoir Release to a Diversion or Reservoir by a Carrier",	// 3
	"Reservoir Release to a Div. by Exchange with River",	// 4
	"Reservoir Storage by Exchange",			// 5
	"Reservoir to Reservoir Transfer (Bookover)",		// 6
	"Diversion by a Carrier by Exchange",			// 7
	"Out of Priority Reservoir Storage",			// 8
	"Reservoir Target",					// 9
	"Replacement Res. to a Div. by Release or Exchange",	// 10
	"Direct Flow Div. to a Demand or Res. through carriers",// 11
	"Reoperation",						// 12
	"Index Flow Constraint on an Instream Flow",		// 13
	"Direct Flow Div. limited by Demand at Dest. and Carrier",// 14
	"Interruptible Supply",					// 15
	"Direct Flow Storage",					// 16
	"Rio Grande Compact - Rio Grande River",		// 17
	"Rio Grande Compact - Conejos River",			// 18
	"Split Channel",					// 19
	"San Juan RIP Reservoir",				// 20
	"Wells with Sprinkler Use",				// 21
	"Soil Moisture Use",					// 22
	"Downstream Call",					// 23
	"Direct Flow Exchange",					// 24
	"Direct Flow Bypass",					// 25
	"Reservoir or ReUse Plan to a T&C Plan",		// 26
	"ReUse Plan to a Div. or Res. Direct w/ or w/out dest. reuse", // 27
	"ReUse Plan to a Div. or Res by Exch. w/ or w/out dest reuse", // 28
	"ReUse Plan Spill",					// 29
	"Reservoir Re Diversion",				// 30
	"Carrier to a ditch or res. w/ Reusable return flows",	// 31
	"Res. and Plan to a Dir. Flow or res. or car. Dir. w/ or w/out dest. reuse", // 32
	"Res. and Plan to a Dir. Flow or res. or car. by Exch. w/ or w/out dest. reuse", // 33
	"Paper Exchange",					// 34
	"Import to a Div., Res., or Carrier w/ or w/out Reuse.",	// 35
	"Seasonal (Daily) Water Right", // 36
	"Augmentation Well", // 37
	"Out of Priority Diversion with Plan", // 38
	"Alternate Point", // 39
	"South Platte Compact", // 40
	"Reservoir Storage with Special Limits", // 41
	"Plan Demand Reset", // 42
	"In Priority Supply", // 43
	"Recharge Well", // 44
	"Carrier with Loss", // 45
	"Multiple Plan Ownership", // 46
	"Administrative Plan Limit", // 47
	"Plan or Reservoir Reuse to a T&C or Augmentation Plan Direct", // 48
	"Plan or Reservoir Reuse to a T&C or Augmentation Plan Exchange" // 49
};

/**
 * Maximum handled operational right type (those that the software has been
 * coded to handle).
 */
public static int MAX_HANDLED_TYPE = 23;
/**
 * Maximum known operational right type (those that are know from documentation but
 * whose syntax is not specifically handled.
 */
public static int MAX_KNOWN_TYPE = 49;

protected String	_rtem;		// Administration number.
protected int		_dumx;		// Typically the number of intervening
					// structures or the number of monthly
					// switches, depending on the right
					// number
protected String 	_ciopde;	// Typically the destination ID
protected String	_iopdes;	// Typically the destination account
protected String 	_ciopso1;	// Typically the supply ID
protected String 	_iopsou1;	// Typically the supply account
protected String 	_ciopso2;	// Definition varies by right type.
protected String 	_iopsou2;	// Definition varies by right type.
protected String 	_ciopso3;	// Definition varies by right type.
protected String 	_iopsou3;	// Definition varies by right type.
protected String 	_ciopso4;	// Used with type 17, 18
protected String 	_iopsou4;	// Used with type 17, 18
protected String 	_ciopso5;	// Used with type 17, 18
protected String 	_iopsou5;	// Used with type 17, 18
protected int 		_ityopr;	// Operational right type > 1.
protected String 	_intern[];	// Intervening structure IDs (up to 10
					// in StateMod doc but no limit here) -
					// used by some rights.
protected int		_imonsw[];	// Monthly switch, for some rights
protected List	_comments;	// Comments provided by user - # comments before each right

protected double	_qdebt;		// used with operational right 17, 18
protected double	_qdebtx;	// used with operational right 17, 18

protected double	_sjmina;	// used with operational right 20
protected double	_sjrela;	// used with operational right 20
/**
 * Plan ID.
 */
private String __creuse;
/**
 * Diversion type.
 */
private String __cdivtyp;
/**
 * Convenyance loss.
 */
private double __oprLoss;
/**
 * Miscellaneous limits.
 */
private double __oprLimit;
/**
 * Beginning year of operation.
 */
private int __ioBeg;
/**
 * Ending year of operation.
 */
private int __ioEnd;

private List __rightStringsVector = null;

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
Creates a copy of the object for later use in checking to see if it was 
changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_OperationalRight)_original)._isClone = false;
	_isClone = true;
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
	_comments = null;
	__creuse = null;
	__cdivtyp = null;
	super.finalize();
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

public List getComments() {
	return _comments;
}

/**
Retrieve dumx.
*/
public int getDumx() {
	return _dumx;
}

/**
Return a monthly switch.
@param index month to get switch for (0-11), where the index is a position, not
a month (actual month is controlled by the year type for the data set).
*/
public int getImonsw(int index) {
	return _imonsw[index];
}

/**
Retrieve an "intern".
*/
public String getIntern(int index) {
	return _intern[index];
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
public List getRightStrings()
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
	_comments = new Vector(1);
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

public void setupImonsw() {	
	_imonsw = new int[12];
	for (int i = 0; i < 12; i++) {
		_imonsw[i] = 0;
	}
}

/**
Read operational right information in and store in a Vector.
@param filename Name of file to read.
@exception Exception if there is an error reading the file.
*/
public static List readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_OperationalRight.readStateModFile";
	String iline = null;
	List v = null;
	List theOprits = new Vector();
	List comment_vector = new Vector(1);	// Will be used prior to finding an operational right
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

	Message.printStatus(2, routine, "Reading operational rights file : " + filename);
	try {
		boolean reading_unknown_right = false;
		List right_strings_Vector = null;	// Operating rule as a list of strings
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
				anOprit.setComments(comment_vector);
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
				// Add Vector and continue to add if more lines are read.  Since using a reference
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
				linecount += readStateModFile_MonthlySwitches ( nmonsw, routine, linecount, in, anOprit );
			}
			// Don't read for the Rio Grande types
			if ( (ninterv > 0) && (type != 17) && (type != 18) ) {
				linecount += readStateModFile_InterveningStructures (
						ninterv, routine, linecount, in, anOprit );
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
		routine = null;
		v = null;
		comment_vector = null;
		format_0 = null;
		if (in != null) {
			in.close();
		}
		in = null;
		anOprit = null;
		Message.printWarning(2, routine, "Error reading near line " + linecount + ": " + iline);
		iline = null;
		Message.printWarning(2, routine, e);
		throw e;
	}

	routine = null;
	iline = null;
	v = null;
	comment_vector = null;
	format_0 = null;
	if (in != null) {
		in.close();
	}
	in = null;
	anOprit = null;
	return theOprits;
}

/**
 * Read the StateMod operational rights file intervening structures.
 * @param ninterv Intervening structures switch.
 * @param routine to use for logging.
 * @param linecount Line count (1+) before reading in this method.
 * @param in BufferedReader to read.
 * @param anOprit Operational right for which to read data.
 * @return the number of lines read (add to line count in calling code).
 * @exception IOException if there is an error reading the file
 */
private static int readStateModFile_InterveningStructures ( int ninterv, String routine, int linecount,
		BufferedReader in, StateMod_OperationalRight anOprit )
throws IOException
{
	String iline = in.readLine().trim();
	Message.printStatus ( 2, routine, "Processing operating rule line " + (linecount + 1) + ": " + iline );
	List tokens = StringUtil.breakStringList( iline, " \t", StringUtil.DELIM_SKIP_BLANKS );
	int ntokens = 0;
	if ( tokens != null ) {
		ntokens = tokens.size();
	}
	if ( ntokens > 0 ) {
		anOprit._intern = new String[ninterv];
	}
	for ( int i=0; i<ntokens; i++) {
		anOprit.setIntern(i, ((String)tokens.get(i)).trim());
	}
	return 1;
}

/**
 * Read the statemod operational rights file monthly switches.
 * @param nmonsw Monthly switch
 * @param routine to use for logging.
 * @param linecount Line count (1+) before reading in this method.
 * @param in BufferedReader to read.
 * @param anOprit Operational right for which to read data.
 * @return the number of lines read (add to line count in calling code).
 * @exception IOException if there is an error reading the file
 */
private static int readStateModFile_MonthlySwitches ( int nmonsw, String routine, int linecount,
		BufferedReader in, StateMod_OperationalRight anOprit )
throws IOException
{
	String iline = in.readLine().trim();
	Message.printStatus ( 2, routine, "Processing operating rule line " + (linecount + 1) + ": " + iline );
	List tokens = StringUtil.breakStringList( iline, " \t", StringUtil.DELIM_SKIP_BLANKS );
	int ntokens = 0;
	if ( tokens != null ) {
		ntokens = tokens.size();
	}
	if ( nmonsw > 0 ) {
		anOprit._imonsw = new int[nmonsw];
	}
	for ( int i=0; i<ntokens; i++) {
		anOprit.setImonsw(i, ((String)tokens.get(i)).trim());
	}
	return 1;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was caled and sets _original to null.
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

// TODO SAM 2008-03-16 - need to check for dirty
/**
Set the comments
*/
public void setComments(List comments) {
	_comments = comments;
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
convert to a double and then case as an integer.
*/
public void setDumx(String dumx)
{	if (dumx != null) {
		setDumx((int)(StringUtil.atod(dumx.trim())));
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
	setDumx(ioBeg.intValue());
}

/**
Set ioBeg.
*/
public void setIoBeg(String ioBeg)
{	if (ioBeg != null) {
		setIoBeg((int)(StringUtil.atod(ioBeg.trim())));
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
	setDumx(ioEnd.intValue());
}

/**
Set ioEnd.
*/
public void setIoEnd(String ioEnd)
{	if (ioEnd != null) {
		setIoEnd((int)(StringUtil.atod(ioEnd.trim())));
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
		setImonsw(index, StringUtil.atoi(imonsw.trim()));
	}
}

/**
Set an "intern".
*/
public void setIntern(int index, String intern) {
	if (intern == null) {
		return;
	}
	if (!intern.equals(_intern[index])) {
		_intern[index] = intern;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_OPERATION_RIGHTS, true);
		}
		if (Message.isDebugOn)
			Message.printDebug(30, 
				"StateMod_OperationalRight.setIntern",
				"Old Dumx: " + getDumx()+ ", New Dumx: " 
				+ index+1);
		if (index+1 > getDumx()) {
			setDumx(index+1);
		}
		if (Message.isDebugOn) {
			Message.printDebug(30, 
				"StateMod_OperationalRight.setInter",
				"Dumx: " + getDumx());
		}
	}
}

/**
Sets the interns from a vector.
*/
public void setInterns(List v) {
	for (int i = 0; i < 10; i++) {
		setIntern(i, (String)v.get(i));
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
		setItyopr(StringUtil.atoi(ityopr.trim()));
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
		setQdebt(StringUtil.atod(qdebt.trim()));
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
		setQdebtx(StringUtil.atod(qdebtx.trim()));
	}
}

/**
Set the operating rule strings, when read as text because an unknown right type.
*/
private void setRightStrings ( List right_strings_Vector )
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
		setSjmina(StringUtil.atod(sjmina.trim()));
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
		setSjrela(StringUtil.atod(sjrela.trim()));
	}
}

/**
Write operational right information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theOpr vector of operational right to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile, List theOpr, String[] newComments)
throws Exception {
	PrintWriter	out = null;
	String [] comment_str = { "#" };
	String [] ignore_str = { "#>" };
	String routine = "StateMod_OperationalRight.writeStateModFile";

	Message.printStatus(1, routine, 
		"Writing new operational rights to file \""
		+ outfile + "\" using \"" + infile + "\" header...");

	out = IOUtil.processFileHeaders(infile, outfile,
		newComments, comment_str, ignore_str, 0);
	try {
	String cmnt = "#>";
	String iline = null;
	String format = "%-12.12s%-36.36s%16.16s%7d.%8d "
			+ "%-12.12s%8d %-12.12s%8d %-12.12s%8d%8d";
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
		out.println(cmnt + " *************************************"
			+ "******************");
 		out.println(cmnt + " Operational Right File");
 		out.println(cmnt);
		out.println(cmnt + "     Card 1   Control");
		out.println(cmnt + "     format:  (a12, a24, 12x, 2i4, i8, "
			+ "f8.0, i8, 3 (i8,a12), 20i8)");
 		out.println(cmnt);
		out.println(cmnt + "     ID       cidvri:          "
			+ "Operational Right ID");
		out.println(cmnt + "     Name     nameo:           "
			+ "Operational Right name");
		out.println(cmnt + "     AdminDat iodat (1-2,k):    "
			+ "Effective priority date");
		out.println(cmnt + "     Admin #  irtem:           "
			+ "Priority Number (smaller is most senior)");
		out.println(cmnt + "     # Str    dumx:             "
			+ "Number of intervenging structures ");
		out.println(cmnt + "     On/Off   ioprsw (k):       "
			+ "Switch 0 = off,1 = on");
		out.println(cmnt + "     Dest ID  ciopde:          "
			+ "Destination reservoir or structure ID");
		out.println(cmnt + "     Dest Ac  iopdes (2,k):     "
			+ "Destination reservoir or structure account # "
			+ "(enter 1 for a diversion)");
		out.println(cmnt + "     Sou1 ID  ciopso (1)       "
			+ "Supply reservoir #1 or structure ID");
		out.println(cmnt + "     Sou1 Ac  iopsou (2,k):     "
			+ "Supply reservoir #1 or structure account # "
			+ "(enter 1 for a diversion)");
		out.println(cmnt + "     Sou2 ID  ciopso (2):       "
			+ "Supply reservoir #2 ID");
		out.println(cmnt + "     Sou1 Ac  iopsou (4,k):     "
			+ "Supply reservoir #2 account");
		out.println(cmnt + "     Type     ityopr (k):       Switch");
		out.println(cmnt + "                        1 = Reservoir "
			+ "Release to an instream demand");
		out.println(cmnt + "                        2 = Reservoir "
			+ "Release to a direct diversion demand");
		out.println(cmnt + "                        3 = Reservoir "
			+ "Release to a direct diversion demand by a carrier");
		out.println(cmnt + "                        4 = Reservoir "
			+ "Release to a direct diversion demand by exchange");
		out.println(cmnt + "                        5 = Reservoir "
			+ "Release to a reservoir by exchange");
		out.println(cmnt + "                        6 = Reservoir "
			+ "to reservoir bookover");
		out.println(cmnt + "                        7 = Reservoir "
			+ "Release to a carrier exchange");
		out.println(cmnt + "                        8 = Out-of-"
			+ "Priority Reservoir Storage");
		out.println(cmnt + "                        9 = Reservoir "
			+ "Release for target contents");
		out.println(cmnt + "                        10 = General "
			+ "Replacement Reservoir");
		out.println(cmnt + "                        11 = Direct "
			+ "flow demand thru intervening structures");
		out.println(cmnt + "                        12 = Reoperate");
		out.println(cmnt + "                        13 = Index Flow");
		out.println(cmnt + "                        14 = "
			+ "Similar to 11 but diversions are constrained by "
			+ "demand at carrier structure");
		out.println(cmnt + "                        15 = "
			+ "Interruptible Supply");
		out.println(cmnt + "                        16 = Direct "
			+ "Flow Storage");

		out.println(cmnt);
		out.println(cmnt + " *************************"
			+ "************************************************");
		out.println(cmnt + "     Card 2   Carrier Ditch data "
			+ "(include only if dumx > 0)");
		out.println(cmnt + "     format:  (free)");
		out.println(cmnt);
		out.println(cmnt + "     Inter    itern (1,j)     "
			+ "intervening direct diversion structure id's");
		out.println(cmnt + "                              "
			+ "Enter # Str values");
		out.println(cmnt);
		out.println(cmnt + " ID        Name                    "
			+ "NA          AdminDat  Admin#   # Str  On/Off Dest "
			+ "Id     Dest Ac  Sou1 Id     Sou1 Ac  Sou2 Id     "
			+ "Sou2 Ac     Type");
		out.println(cmnt + "---------eb----------------------e"
			+ "b----------eb------eb------eb------eb------e-b-"
			+ "---------eb------e-b----------eb------e-b------"
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

			comments_vector = opr.getComments();
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
				Message.printDebug(50, routine, 
					"in area 3 (" + opr.getID() + "): getDumx = " + opr.getDumx()
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

	out.flush();
	out.close();
	out = null;
	comment_str = null;
	ignore_str = null;
	routine = null;
	} 
	catch (Exception e) {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
		comment_str = null;
		ignore_str = null;
		routine = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
}

} // End StateMod_OperationalRight
