//------------------------------------------------------------------------------
// StateMod_StreamEstimate_Coefficients - class to store stream estimate
//				station coefficient information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 11 Feb 1998	CEN, RTi		Added _dataset.setDirty
//					to all set
//					routines.
// 11 Mar 1998	CEN, RTi		Changed class to extend from Object
//					(default)to StateMod_Data.  Removed utm
//					abilities(use StateMod_Data's).
// 04 Aug 1998	Steven A. Malers, RTi	Overload some of the routines for use in
//					DMIs.  Add SMLocateBaseNode as per
//					legacy code.
// 21 Dec 1998	CEN, RTi		Added throws IOException to read/write
//					routines.
// 18 Feb 2001	SAM, RTi		Code review.  Clean up javadoc.  Handle
//					nulls.  Set unused variables to null.
//					Add finalize.  Alphabetize methods.
//					Change IO to IOUtil.  Change so Vectors
//					are initialize sized to 5 rather than
//					15.  Update SMLocateBaseNode to use
//					SMUtil for search.
// 02 May 2001	SAM, RTi		Track down problem with warning reading
//					baseflow.  Second line of read was
//					trimming first, which caused the fixed
//					read to have problems.  Not sure how
//					the problem got introduced.
// 2001-12-27	SAM, RTi		Update to use new fixedRead()to
//					improve performance.
// 2002-09-12	SAM, RTi		Remove baseflow time series code from
//					here and move to the SMRiverInfo class,
//					to handle the time series associated
//					with .ris nodes.
// 2002-09-19	SAM, RTi		Use isDirty() instead of setDirty() to
//					indicate edits.
//------------------------------------------------------------------------------
// 2003-06-04	J. Thomas Sapienza, RTI	Renamed from SMRivBaseflows
// 2003-06-10	JTS, RTi		* Folded dumpRiverBaseFlowsFile() into
//					  writeRiverBaseFlowsFile()
//					* Renamed parseRiverBaseFlowsFile() to
//					  readRiverBaseFlowsFile()
// 2003-06-23	JTS, RTi		Renamed writeRiverBaseFlowsFile() to
//					writeStateModFile()
// 2003-06-26	JTS, RTi		Renamed readRiverBaseFlowsFile() to
//					readStateModFile()
// 2003-07-15	JTS, RTi		Changed to use new dataset design.
// 2003-08-03	SAM, RTi		* Rename class from StateMod_BaseFlows
//					  to StateMod_BaseFlowCoefficients.
//					* Changed isDirty() back to setDirty().
//					* locateIndexFromID() is now in
//					  StateMod_Util.
// 2003-09-11	SAM, RTi		Rename class from
//					StateMod_BaseFlowCoefficents to
//					StateMod_StreamEstimate_Coefficients.
// 2003-10-16	SAM, RTi		Remove description since it is not
//					used in the file and the base class
//					name could be used if needed.
// 2004-03-15	JTS, RTi		Put description back in as it is now
//					used by the makenet code in 
//					HydroBase_NodeNetwork.
// 2004-07-12	JTS, RTi		Removed description one more time.
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
// 2004-08-13	SAM, RTi		* When writing, adjust file names for
//					  working directory.
//					* Handle null _dataset, for use with
//					  StateDMI.
//					* For setM() and setN(), if the size is
//					  set to zero, clear out the data
//					  vectors - this is used with the
//					  StateDMI set command.
// 2005-04-18	JTS, RTi		Added writeListFile().
// 2007-04-12	Kurt Tometich, RTi		Added checkComponentData() and
//									getDataHeader() methods for check
//									file and data check support.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_StreamEstimate_Coefficients 
extends StateMod_Data 
implements Cloneable, StateMod_Component {

// REVISIT - why is this needed?
public static final int MAX_BASEFLOWS = 15;

protected String 	_flowX;	// node where flow is to be estimated
protected int		_N;	// number of stations upstream X
protected Vector	_coefn;	// double - factors to weight the gaged flow
protected Vector	_upper;	// String - station id upstream X
protected double	_proratnf;	// factor to distribute the gain
protected int		_M;	// number of stations used to calc the gain
protected Vector	_coefm;	// double - factors to weight the flow for gain
protected Vector	_flowm;	// String - station id upstream X
	
/**
Constructor.
*/
public StateMod_StreamEstimate_Coefficients() {
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

public void addCoefm(double d) {
	addCoefm(new Double(d));
}

public void addCoefm(Double d) {
	_coefm.addElement(d);
	int size = _coefm.size();
	if (size > _M) {
		_M = size;
	}
	if (!_isClone) {
		if ( _dataset != null ) {
			_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS, true);
		}
	}
}

public void addCoefn(double d) {
	addCoefn(new Double(d));
}

public void addCoefn(Double d) {
	_coefn.addElement(d);
	int size = _coefn.size();
	if (size > _N) {
		_N = size;
	}
	if (!_isClone) {
		if ( _dataset != null) {
			_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS, true);
		}
	}
}

public void addUpper(String s) {
	if (s != null) {
		_upper.addElement(s.trim());
		int size = _upper.size();
		if (size > _N) {
			_N = size;
		}
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
			}
		}
	}
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
@param count Number of components checked.
@param dataset StateMod dataset object.
@param props Extra properties for specific data checks.
@return List of data that failed specific checks.
 */
public String[] checkComponentData( int count, 
StateMod_DataSet dataset, PropList props ) 
{
	// TODO KAT 2007-04-16
	// add specific checks here
	return null;
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_StreamEstimate_Coefficients c 
		= (StateMod_StreamEstimate_Coefficients)super.clone();
	c._isClone = true;

	c._coefn = (Vector)_coefn.clone();
	c._upper = (Vector)_upper.clone();
	c._coefm = (Vector)_coefm.clone();
	c._flowm = (Vector)_flowm.clone();
	return c;
}

/**
Compares this object to another StateMod_StreamEstimate_Coefficients object.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateMod_StreamEstimate_Coefficients c 
		= (StateMod_StreamEstimate_Coefficients)o;

	res = _flowX.compareTo(c._flowX);
	if (res != 0) {
		return res;
	}

	if (_N < c._N) {
		return -1;
	}
	else if (_N > c._N) {
		return 1;
	}

	if (_proratnf < c._proratnf) {
		return -1;
	}
	else if (_proratnf > c._proratnf) {
		return 1;
	}

	if (_M < c._M) {
		return -1;
	}
	else if (_M > c._M) {
		return 1;
	}

	int size1 = 0;
	int size2 = 0;
	double d1 = 0;
	double d2 = 0;
	String s1 = null;
	String s2 = null;

	if (_coefn == null && c._coefn != null) {
		return -1;
	}
	else if (_coefn != null && c._coefn == null) {
		return 1;
	}

	size1 = _coefn.size();
	size2 = c._coefn.size();
	
	if (size1 < size2) {
		return -1;
	}
	else if (size2 > size1) {
		return 1;
	}
	else {
		for (int i = 0; i < size1; i++) {
			d1 = ((Double)_coefn.elementAt(i)).doubleValue();
			d2 = ((Double)c._coefn.elementAt(i)).doubleValue();
			if (d1 < d2) {
				return -1;
			}
			else if (d1 > d2) {
				return 1;
			}
		}
	}
		
	if (_upper == null && c._upper != null) {
		return -1;
	}
	else if (_upper != null && c._upper == null) {
		return 1;
	}

	size1 = _upper.size();
	size2 = c._upper.size();
	
	if (size1 < size2) {
		return -1;
	}
	else if (size2 > size1) {
		return 1;
	}
	else {
		for (int i = 0; i < size1; i++) {
			s1 = (String)_upper.elementAt(i);
			s2 = (String)c._upper.elementAt(i);
			res = s1.compareTo(s2);
			if (res != 0) {
				return res;
			}
		}
	}		

	if (_coefm == null && c._coefm != null) {
		return -1;
	}
	else if (_coefm != null && c._coefm == null) {
		return 1;
	}

	size1 = _coefm.size();
	size2 = c._coefm.size();
	
	if (size1 < size2) {
		return -1;
	}
	else if (size2 > size1) {
		return 1;
	}
	else {
		for (int i = 0; i < size1; i++) {
			d1 = ((Double)_coefm.elementAt(i)).doubleValue();
			d2 = ((Double)c._coefm.elementAt(i)).doubleValue();
			if (d1 < d2) {
				return -1;
			}
			else if (d1 > d2) {
				return 1;
			}
		}
	}


	if (_flowm == null && c._flowm != null) {
		return -1;
	}
	else if (_flowm != null && c._flowm == null) {
		return 1;
	}

	size1 = _flowm.size();
	size2 = c._flowm.size();
	
	if (size1 < size2) {
		return -1;
	}
	else if (size2 > size1) {
		return 1;
	}
	else {
		for (int i = 0; i < size1; i++) {
			s1 = (String)_flowm.elementAt(i);
			s2 = (String)c._flowm.elementAt(i);
			res = s1.compareTo(s2);
			if (res != 0) {
				return res;
			}
		}
	}		

	return 0;
}

/**
Creates a copy of the object for later use in checking to see if it was 
changed in a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateMod_StreamEstimate_Coefficients)_original)._isClone = false;
	_isClone = true;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	_flowX = null;
	_coefn = null;
	_upper = null;
	_coefm = null;
	_flowm = null;
	super.finalize();
}

/**
Retrieve the coefm corresponding to a particular index.
*/
public double getCoefm(int index) {
	return ((Double)_coefm.elementAt(index)).doubleValue();
}

/**
Retrieve the coefm corresponding to a particular index.
*/
public Vector getCoefm() {
	return _coefm;
}

/**
Return the coefn corresponding to a particular index.
*/
public double getCoefn(int index) {
	return ((Double)_coefn.elementAt(index)).doubleValue();
}

/**
Return the coefn corresponding to a particular index.
*/
public Vector getCoefn() {
	return _coefn;
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-16 
	// When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Return the upper id corresponding to a particular index.
*/
public String getFlowm(int index) {
	return (String)_flowm.elementAt(index);
}

/**
Return the upper id corresponding to a particular index.
*/
public Vector getFlowm() {
	return _flowm;
}

/**
Return the node where flow is to be estimated.
*/
public String getFlowX() {
	return _flowX;
}

/**
Return the number of stations used to calc the gain.
*/
public int getM() {
	return _M;
}

/**
Retrieve the number of stations upstream X.
*/
public int getN() {
	return _N;
}

/**
Return the factor to distribute the gain.
*/
public double getProratnf() {
	return _proratnf;
}

/**
Retrun the upper id corresponding to a particular index.
*/
public String getUpper(int index) {
	return (String)_upper.elementAt(index);
}

/**
Retrun the upper id corresponding to a particular index.
*/
public Vector getUpper() {
	return _upper;
}

/**
Initialize member variables.
*/
private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS;
	_flowX = "";
	_N = 0;
	_M = 0;
	_coefn = new Vector(5,5);
	_upper = new Vector(5,5);
	_coefm = new Vector(5,5);
	_flowm = new Vector(5,5);
	_proratnf = 0;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_StreamEstimate_Coefficients c 
		= (StateMod_StreamEstimate_Coefficients)_original;
	super.restoreOriginal();
	_flowX = c._flowX;
	_N = c._N;
	_proratnf = c._proratnf;
	_M = c._M;
	_isClone = false;
	_original = null;
}

/**
Add factor to vector of factors to weight the flow for gain.
*/
public void setCoefm(int index, String str) {
	if (str != null) {
		setCoefm(index, StringUtil.atod(str.trim()));
	}
}

/**
Add factor to vector of factors to weight the flow for gain.
*/
public void setCoefm(int index, double str) {
	if (_coefm.size()< index+1) {
		addCoefm(new Double(str));
	}
	else {	
		_coefm.setElementAt(new Double(str), index);
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
			}
		}
	}
}

/**
Add factor to vector of factors to weight the gaged flow to vector (coefn).
*/
public void setCoefn(int index, String str) {
	if (str != null) {
		setCoefn(index, StringUtil.atod(str.trim()));
	}
}

/**
Main version.
*/
public void setCoefn(int index, double str) {
	if (_coefn.size()< index+1) {
		addCoefn(new Double(str));
	}
	else {	
		_coefn.setElementAt(new Double(str), index);
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
			}
		}
	}
}

/**
Set the node where flow is to be estimated.
*/
public void setFlowX(String s) {
	if (s != null) {
		if (!s.equals(_flowX)) {
			if (!_isClone) {
				if ( _dataset != null ) {
					_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
				}
			}
			_flowX = s;
			setID(s);
		}
	}
}

/**
Add id to vector of station ids upstream X.
*/
public void setFlowm(int index, String str) {
	if (str != null) {
		if (_flowm.size()< index+1) {
			addFlowm(str.trim());
		}
		else {	
			_flowm.setElementAt(str.trim(), index);
			if (!_isClone) {
				if ( _dataset != null ) {
					_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
				}
			}
		}
	}
}

/**
Add id to vector of station ids upstream X.
*/
public void addFlowm(String s) {
	if (s != null) {
		_flowm.addElement(s.trim());
		int size = _flowm.size();
		if (size > _M) {
			_M = size;
		}
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS, true);
			}
		}
	}
}

/**
Set the number of stations used to calc the gain.
*/
public void setM(int i) {
	if (i != _M) {
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty(StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS, true);
			}
		}
		_M = i;
		if ( i == 0 ) {
			// Clear vector...
			_coefm.removeAllElements();
			_flowm.removeAllElements();
		}
	}
}

/**
Set the number of stations used to calc the gain.
*/
public void setM(Integer i) {
	setM(i.intValue());
}

/**
Set the number of stations used to calc the gain.
*/
public void setM(String str) {
	if (str != null) {
		setM(StringUtil.atoi(str.trim()));
	}
}

/**
Set the number of stations upstream X.
*/
public void setN(int i) {
	if (i != _N) {
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,
				true);
			}
		}
		_N = i;
		if ( i == 0 ) {
			// Clear vector...
			_coefn.removeAllElements();
			_upper.removeAllElements();
		}
	}
}

/**
Set the number of stations upstream X.
*/
public void setN(Integer i) {
	setN(i.intValue());
}

/**
Set the number of stations upstream X.
*/
public void setN(String str) {
	if (str != null) {
		setN(StringUtil.atoi(str.trim()));
	}
}

/**
Set the initial storage of owner.
*/
public void setProratnf(double d) {
	if (d != _proratnf) {
		if (!_isClone) {
			if ( _dataset != null ) {
				_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS,true);
			}
		}
		_proratnf = d;
	}
}

/**
Set the initial storage of owner.
*/
public void setProratnf(Double d) {
	setProratnf(d.doubleValue());
}

/**
Set the initial storage of owner.
*/
public void setProratnf(String str) {
	if (str != null) {
		setProratnf(StringUtil.atod(str.trim()));
	}
}

/**
Add id to vector of station ids upstream X.
*/
public void setUpper(int index, String str) {
	if (str != null) {
		if (_upper.size()< index+1) {
			addUpper(str.trim());
		}
		else {	
			_upper.setElementAt(str.trim(), index);
			if (!_isClone) {
				if ( _dataset != null ) {
					_dataset.setDirty( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS, true);
				}
			}
		}
	}
}

/**
@return A in instance of StateMod_StreamEstimate_Coefficients from a vector of
the same, or null if not found.
@param baseflow Vector of StateMod_BaseFlowCoefficients data.
@param id Baseflow node identifier to locate.
*/
public static StateMod_StreamEstimate_Coefficients
		locateBaseNode(Vector baseflow, 
String id) {
	int index = StateMod_Util.locateIndexFromID(id, baseflow);
	if (index < 0) {
		return null;
	}
	return (StateMod_StreamEstimate_Coefficients)baseflow.elementAt(index);
}

/**
Read stream estimate coefficients and store in a Vector.
@param filename Name of file to read.
@return Vector of baseflow data
@exception Exception if there is an error reading the file.
*/
public static Vector readStateModFile(String filename)
throws Exception {
	String routine ="StateMod_StreamEstimate_Coefficients.readStateModFile";
	Vector theBaseflows = new Vector();
	
	String iline = null;
	Vector v = new Vector(2);	// used to retrieve from fixedRead
	String adnl = null;
	int [] format_0 = {	StringUtil.TYPE_STRING,
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_INTEGER };
	int [] format_0w = {	12,
				8,
				8 };
	int [] format_1 = {	StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING };
	int [] format_1w = {	8,
				1,
				12 };
	int [] format_2 = {	StringUtil.TYPE_SPACE,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER };
	int [] format_2w = {	12,
				8,
				8 };
	int [] format_3 = {	StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING };
	int [] format_3w = {	8,
				1,
				12 };
	BufferedReader in = null;
	StateMod_StreamEstimate_Coefficients aBaseflow = null;
	int i = 0;
	int num_adnl;
	int begin_pos;
	int end_pos;
	int linecount = 0;

	if (Message.isDebugOn) {
		Message.printDebug(30, routine, "Reading file :" + filename);
	}

	try {	
		in = new BufferedReader(new FileReader(filename));
		while ((iline = in.readLine()) != null) {
			++linecount;
			// check for comments
			if (iline.startsWith("#") || iline.trim().length()==0) {
				continue;
			}
			// allocate a new baseflow
			aBaseflow = new StateMod_StreamEstimate_Coefficients();

			// read in first of two lines for each baseflow
			StringUtil.fixedRead(iline, format_0, format_0w, v);
			aBaseflow.setFlowX(((String)v.elementAt(0)).trim());
			aBaseflow.setN((Integer)v.elementAt(1));

			num_adnl = aBaseflow.getN();
			for (i = 0; i < num_adnl; i++) {
				// calculate begin_pos and end_pos
				//	 8(factor to weight the gaged flow)
				// +	 1(space)
				// +	12(station id upstream X)
				// --------
				//	21(for each set of num_adnl
				// +	28(array index after initial info)
				// +	 1(we should start on next position)
				// -	 1(our string starts with index 0)
				// 	
				begin_pos = 28 +(i * 21);
				end_pos = begin_pos + 21;
				adnl = iline.substring(begin_pos, end_pos);
				StringUtil.fixedRead(adnl, format_1,
					format_1w, v);
				aBaseflow.addCoefn((Double)v.elementAt(0));
				aBaseflow.addUpper((String)v.elementAt(1));
			}

			// read in second of two lines for each baseflow
			iline = in.readLine();
			StringUtil.fixedRead(iline, format_2, format_2w, v);
			aBaseflow.setProratnf((Double)v.elementAt(0));
			aBaseflow.setM((Integer)v.elementAt(1));

			num_adnl = aBaseflow.getM();
			for (i = 0; i < num_adnl; i++) {
				begin_pos = 28 +(i * 21);
				end_pos = begin_pos + 21;
				adnl = iline.substring(begin_pos, end_pos);
				StringUtil.fixedRead(adnl, format_3,
					format_3w, v);
				aBaseflow.addCoefm((Double)v.elementAt(0));
				aBaseflow.addFlowm((String)v.elementAt(1));
			}

			// add the baseflow to the vector of baseflows
			theBaseflows.addElement(aBaseflow);
		}
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading near line "
			+ linecount);
		routine = null;
		iline = null;
		v = null;	// used to retrieve from fixedRead
		adnl = null;
		format_0 = null;
		format_0w = null;
		format_1 = null;
		format_1w = null;
		format_2 = null;
		format_2w = null;
		format_3 = null;
		format_3w = null;
		if (in != null) {
			in.close();
		}
		in = null;
		aBaseflow = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
	routine = null;
	iline = null;
	v = null;	// used to retrieve from fixedRead
	adnl = null;
	format_0 = null;
	format_0w = null;
	format_1 = null;
	format_1w = null;
	format_2 = null;
	format_2w = null;
	format_3 = null;
	format_3w = null;
	if (in != null) {
		in.close();
	}
	in = null;
	aBaseflow = null;
	return theBaseflows;
}

/**
Write stream estimate station coefficients to output.  History header
information is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theBaseflows vector of baseflow to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
Vector theBaseflows, String[] newComments)
throws Exception {
	String routine =
		"StateMod_StreamStation_Coefficients.writeStateModFile";
	String [] comment_str = { "#" };
	String [] ignore_str = { "#>" };
	PrintWriter out = null;

	Message.printStatus(1, routine, 
		"Writing new stream estimate station coefficients to file \"" +
			outfile + "\" using \"" + infile + "\" header...");

	try {	
	out = IOUtil.processFileHeaders(
		IOUtil.getPathUsingWorkingDir(infile),
		IOUtil.getPathUsingWorkingDir(outfile), 
		newComments, comment_str, ignore_str, 0);

	String cmnt = "#>";
	String iline = null;
	StateMod_StreamEstimate_Coefficients bf = null;
	String format_1 = "%-12.12s        %8d";
	String format_2 = "%8.3f %-12.12s";
	String format_3 = "            %8.3f%8d";
	Vector v = new Vector(2);

		out.println(cmnt 
			+ "---------------------------------------------" 
			+ "------------------------------");
		out.println(cmnt +"  Stream Estimate Station Coefficient Data");
		out.println(cmnt);
		out.println(cmnt 
			+ "  FlowX = (FlowB(1)*coefB(1) + FlowG(2)*coefB(2) "
			+ "+ ...)+");
		out.println(cmnt 
			+ "          pf * (FlowG(1)*coefG(1) + FlowG(2)*"
			+ "coefG(2) + ...)+");
		out.println(cmnt);
		out.println(cmnt + "  where:");
		out.println(cmnt);
		out.println(cmnt 
			+ "  FlowX = Flow at intermediate node to be "
			+ "estimated.");
		out.println(cmnt 
			+ "  FlowB =   Estimate flow station(s).");
		out.println(cmnt 
			+ "  FlowG =   Gain flow station(s).");
		out.println(cmnt);
		out.println(cmnt 
			+ "     pf = Proration factor for gain term.");
		out.println(cmnt 
			+ "  coefB =   Estimate flow coefficient.");
		out.println(cmnt 
			+ "  coefG =   Gain flow coefficient.");
		out.println(cmnt);
		out.println(cmnt 
			+ "  Card 1 format (a12, 8x, i8, 10(f8.3,1x,a12)");
		out.println(cmnt);
		out.println(cmnt 
			+ "       FlowX:  Node where flow is to be estimated");
		out.println(cmnt 
			+ "       Mbase:  Number of base stations to follow");
		out.println(cmnt 
			+ "       coefB:  Estimate flow coefficient");
		out.println(cmnt 
			+ "       FlowB:  Estimate station ID");
		out.println(cmnt);
		out.println(cmnt 
			+ "  Card 2 format (12x, f8.2, i8, 10(f8.3,1x,a12)");
		out.println(cmnt);
		out.println(cmnt 
			+ "          pf:  Proration factor for gain term.");
		out.println(cmnt 
			+ "       nbase:  Number of gain stations to follow");
		out.println(cmnt 
			+ "       coefG:  Gain flow coefficient.");
		out.println(cmnt 
			+ "       FlowG:  Gaged flow stations used " 
			+ "to calculate gain");
		out.println(cmnt);
		out.println(cmnt 
			+ " FlowX              mbase   coefB1    FlowB1   " 
			+ " coefB2    FlowB2    coefB3   FlowB3     " 
			+ " coefB3    FlowB4     ...");
		out.println(cmnt 
			+ "---------exxxxxxxxb------eb------exb----------e" 
			+ "b------exb----------eb------exb----------e" 
			+ "b------exb----------e ...");
		out.println(cmnt 
			+ "             pf     nbase   coefG1   FlowG1    " 
			+ " coefG2    FlowG2     coefG3    FlowG3    " 
			+ " coefG4    FlowG4     ...");
		out.println(cmnt 
			+ "xxxxxxxxxxb------eb------eb------exb----------e" 
			+ "b------exb----------eb------exb----------e" 
			+ "b------exb----------e ...");
		out.println(cmnt);
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int num = 0;
		if (theBaseflows != null) {
			num = theBaseflows.size();
		}
		for (int i = 0; i < num; i++) {
			bf = (StateMod_StreamEstimate_Coefficients)
				theBaseflows.elementAt(i);
			if (bf == null) {
				continue;
			}

			// 1st line
			v.removeAllElements();
			v.addElement(bf.getFlowX());
			v.addElement(new Integer(bf.getN()));
			iline = StringUtil.formatString(v, format_1);
			out.print(iline);

			for (int j = 0; j < bf.getN(); j++) {
				v.removeAllElements();
				v.addElement(new Double(bf.getCoefn(j)));
				v.addElement(bf.getUpper(j));
				iline = StringUtil.formatString(v, format_2);
				out.print(iline);
			}
			out.println();
	
			// 2nd line
			v.removeAllElements();
			v.addElement(new Double(bf.getProratnf()));
			v.addElement(new Integer(bf.getM()));
			iline = StringUtil.formatString(v, format_3);
			out.print(iline);
			for (int j = 0; j < bf.getM(); j++) {
				v.removeAllElements();
				v.addElement(new Double(bf.getCoefm(j)));
				v.addElement(bf.getFlowm(j));
				iline = StringUtil.formatString(v, format_2);
				out.print(iline);
			}
			out.println();
		}

	out.flush();
	out.close();
	out = null;
	routine = null;
	comment_str = null;
	ignore_str = null;
	} 
	catch (Exception e) {
		if (out != null) {
			out.close();
		}
		out = null;
		routine = null;
		comment_str = null;
		ignore_str = null;
		Message.printWarning(2, routine, e);
		throw e;
	}
}

/**
Writes a Vector of StateMod_StreamEstimate_Coefficients objects to a list file. 
A header is printed to the top of the file, containing the commands used to 
generate the file.  Any strings in the body of the file that contain the field 
delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the Vector of objects to write.  
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter,
boolean update, Vector data) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	Vector fields = new Vector();
	fields.add("ID");
	fields.add("Name");
	fields.add("UpstreamGage");
	fields.add("ProrationFactor");
	fields.add("Weight");
	fields.add("GageID");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.elementAt(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	int k = 0;
	int num = 0;
	int M = 0;
	int N = 0;
	PrintWriter out = null;
	StateMod_StreamEstimate_Coefficients coeff = null;
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
	String[] line = new String[fieldCount];
	String[] newComments = null;
	String id = null;
	StringBuffer buffer = new StringBuffer();
	
	try {	
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names[i] + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			coeff = (StateMod_StreamEstimate_Coefficients)
				data.elementAt(i);

			id = coeff.getID();

			M = coeff.getM();
			N = coeff.getN();

			num = M < N ? N : M;
			
			for (j = 0; j < num; j++) {
				line[0] = StringUtil.formatString(id,
					formats[0]).trim();	

				if (j < N) {
					line[1] = StringUtil.formatString(
						coeff.getCoefn(j),
						formats[1]).trim();
					line[2] = StringUtil.formatString(
						coeff.getUpper(j), 
						formats[2]).trim();
				}
				else {
					line[1] = "";
					line[2] = "";
				}

				if (j < M) {
					line[3] = StringUtil.formatString(
						coeff.getProratnf(), 
						formats[3]).trim();
					line[4] = StringUtil.formatString(
						coeff.getCoefm(j), 
						formats[4]).trim();
					line[5] = StringUtil.formatString(
						coeff.getFlowm(j), 
						formats[5]).trim();
				}
				else {
					line[3] = "";
					line[4] = "";
					line[5] = "";
				}

				buffer = new StringBuffer();	
				for (k = 0; k < fieldCount; k++) {
					if (line[k].indexOf(delimiter) > -1) {
						line[k] = "\"" + line[k] + "\"";
					}
					buffer.append(line[k]);
					if (k < (fieldCount - 1)) {
						buffer.append(delimiter);
					}
				}

				out.println(buffer.toString());
			}
		}
		out.flush();
		out.close();
		out = null;
	}
	catch (Exception e) {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
		throw e;
	}
}

}
