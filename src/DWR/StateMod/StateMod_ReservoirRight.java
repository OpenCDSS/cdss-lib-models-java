// StateMod_ReservoirRight - class to store reservoir rights

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

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_ReservoirRight extends StateMod_Data 
implements Cloneable, Comparable<StateMod_Data>, StateMod_ComponentValidator, StateMod_Right {
/**
Administration number
*/
protected String _rtem;
/**
Decreed amount
*/
protected double _dcrres;
/**
Filling ratio
*/
protected int _iresco;
/**
Reservoir type
*/
protected int _ityrstr;
/**
Reservoir right type
*/
protected int _n2fill;
/**
out of priority associated op right
*/
protected String _copid;
	
/**
Constructor.
*/
public StateMod_ReservoirRight() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_ReservoirRight right = (StateMod_ReservoirRight)super.clone();
	right._isClone = true;
	return right;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by rtem, dcrres, iresco,
ityrstr, n2fill and copid, in that order.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_ReservoirRight right = (StateMod_ReservoirRight)data;

	res = _rtem.compareTo(right.getRtem());
	if (res != 0) {
		return res;
	}

	if (_dcrres < right._dcrres) {
		return -1;
	}
	else if (_dcrres > right._dcrres) {
		return 1;
	}

	if (_iresco < right._iresco) {
		return -1;
	}
	else if (_iresco > right._iresco) {
		return 1;
	}

	if (_ityrstr < right._ityrstr) {
		return -1;
	}
	else if (_ityrstr > right._ityrstr) {
		return 1;
	}

	if (_n2fill < right._n2fill) {
		return -1;
	}
	else if (_n2fill > right._n2fill) {
		return 1;
	}

	res = _copid.compareTo(right._copid);
	return res;	
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = (StateMod_ReservoirRight)clone();
	((StateMod_ReservoirRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights lists and see if they are the same.
@param v1 the first list of StateMod_ReservoirRight s to check.  Cannot be null.
@param v2 the second list of StateMod_ReservoirRight s to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List<StateMod_ReservoirRight> v1, List<StateMod_ReservoirRight> v2)
{
	StateMod_ReservoirRight r1;	
	StateMod_ReservoirRight r2;	
	if (v1.size() != v2.size()) {
		//Message.printStatus(2, routine, "Lists are different sizes");
		return false;
	}
	else {
		// sort the lists and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		//Message.printStatus(2, routine, "Lists are of size: " + size);
		List<StateMod_ReservoirRight> v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List<StateMod_ReservoirRight> v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		//Message.printStatus(2, routine, "Lists have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = v1Sort.get(i);	
			r2 = v2Sort.get(i);	
			//Message.printStatus(2, routine, r1.toString());
			//Message.printStatus(2, routine, r2.toString());
			//Message.printStatus(2, routine, "Element " + i + " comparison: " + r1.compareTo(r2));
			if (r1.compareTo(r2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two reservoir rights are equal.  Strings are compared with case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_ReservoirRight right) {
	 if (!super.equals(right)) {
	 	return false;
	}
	
	if ( right._rtem.equals(_rtem) && right._dcrres == _dcrres && right._iresco == _iresco
		&& right._ityrstr == _ityrstr && right._n2fill == _n2fill && right._copid.equals(_copid)) {
		return true;
	}
	return false;
}

/**
Return the administration number, as per the generic interface.
@return the administration number, as a String to protect from roundoff.
*/
public String getAdministrationNumber ()
{	return getRtem();
}

/**
Return the out-of-priority associated op right.
*/
public String getCopid() {
	return _copid;
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-16 When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Return the decreed amount.
*/
public double getDcrres() {
	return _dcrres;
}

/**
Return the decree, as per the generic interface.
@return the decree, in the units of the data.
*/
public double getDecree()
{	return getDcrres();
}

// TODO SAM 2007-05-15 Need to evaluate whether should be hard-coded.
/**
Return the decree units.
@return the decree units.
*/
public String getDecreeUnits()
{	return "ACFT";
}

/**
Return the right identifier, as per the generic interface.
@return the right identifier.
*/
public String getIdentifier()
{	return getID();
}

/**
Return the filling ratio.
*/
public int getIresco() {
	return _iresco;
}

/**
Return a list of account distribution switch option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Account to be served by right", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getIrescoChoices ( boolean include_notes )
{	List<String> v = new Vector<String>(102);	// Allow for one blank in StateDMI
	for ( int i = 1; i <= 50; i++ ) {
		v.add ( "" + i + " - Account served by right" );
	}
	for ( int i = -1; i >= -50; i-- ) {
		v.add ( "" + i + " - Fill first " + (-i) + " accounts" );
	}
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i,StringUtil.getToken(v.get(i), " ", 0, 0));
		}
	}
	return v;
}

/**
Return the default account distribution switch choice.  This can be used by GUI
code to pick a default for a new reservoir.
@return the default reservoir account distribution.
*/
public static String getIrescoDefault ( boolean include_notes )
{	// Make this aggree with the above method...
	if ( include_notes ) {
		return ( "1 - Account served by right" );
	}
	else {
		return "1";
	}
}

/**
Return a list of on/off switch option strings, for use in GUIs.
The options are of the form "0" if include_notes is false and "0 - Off", if include_notes is true.
@return a list of on/off switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getIrsrswChoices ( boolean include_notes )
{	return StateMod_DiversionRight.getIdvrswChoices ( include_notes );
}

/**
Return the default on/off switch choice.  This can be used by GUI code
to pick a default for a new reservoir.
@return the default reservoir on/off choice.
*/
public static String getIrsrswDefault ( boolean include_notes )
{	return StateMod_DiversionRight.getIdvrswDefault ( include_notes );	
}

/**
Return the reservoir type.
*/
public int getItyrstr() {
	return _ityrstr;
}

/**
Return a list of right type option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - Standard", if include_notes is true.
@return a list of fill switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getItyrsrChoices ( boolean include_notes )
{	List<String> v = new Vector<String>(2);
	v.add ( "1 - Standard" ); // Possible options are listed here.
	v.add ( "-1 - Out of priority water right" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i,StringUtil.getToken(v.get(i), " ", 0, 0) );
		}
	}
	return v;
}

/**
Return the default right type choice.  This can be used by GUI code
to pick a default for a new reservoir.
@return the default right type choice.
*/
public static String getItyrsrDefault ( boolean include_notes )
{	// Make this agree with the above method...
	if ( include_notes ) {
		return ( "1 - Standard" );
	}
	else {
		return "1";
	}
}

/**
Return the right location identifier, as per the generic interface.
@return the right location identifier (location where right applies).
*/
public String getLocationIdentifier()
{	return getCgoto();
}

/**
Retrieve the reservoir right type.
*/
public int getN2fill() {
	return _n2fill;
}

/**
Return a list of fill switch option strings, for use in GUIs.
The options are of the form "1" if include_notes is false and
"1 - First fill", if include_notes is true.
@return a list of fill switch option strings, for use in GUIs.
@param include_notes Indicate whether notes should be added after the parameter values.
*/
public static List<String> getN2fillChoices ( boolean include_notes )
{	List<String> v = new Vector<String>(2);
	v.add ( "1 - First fill" ); // Possible options are listed here.
	v.add ( "2 - Second fill" );
	if ( !include_notes ) {
		// Remove the trailing notes...
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.set(i,StringUtil.getToken(v.get(i), " ", 0, 0) );
		}
	}
	return v;
}

/**
Return the default fill type choice.  This can be used by GUI code to pick a default for a new reservoir.
@return the default fill type choice.
*/
public static String getN2fillDefault ( boolean include_notes )
{	// Make this agree with the above method...
	if ( include_notes ) {
		return ( "1 - First fill" );
	}
	else {
		return "1";
	}
}

/**
Return the administration number.
*/
public String getRtem() {
	return _rtem;
}

/**
INitialize data members
*/
private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_RESERVOIR_RIGHTS;
	_rtem = "99999";	// Default as per old SMGUI.
	_copid = "";
	_dcrres	= 0;
	_iresco	= 1;	// Server first account, as per old SMGUI default
	_ityrstr= 1;
	_n2fill	= 1;
}


/**
Determine whether a file is a reservoir right file.  Currently true is returned
if the file extension is ".rer".
@param filename Name of the file being checked.
@return true if the file is a StateMod reservoir right file.
*/
public static boolean isReservoirRightFile ( String filename )
{	if ( filename.toUpperCase().endsWith(".RER")) {
		return true;
	}
	return false;
}

/**
Read reservoir right information in and store in a list.
@param filename Name of file to read.
@return list of reservoir right data
@exception Exception if there is an error reading the file.
*/
public static List<StateMod_ReservoirRight> readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_ReservoirRight.readStateModFile";
	List<StateMod_ReservoirRight> theRights = new Vector<StateMod_ReservoirRight>();
	int [] format_0 = {
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_STRING,
		StringUtil.TYPE_DOUBLE,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_INTEGER,
		StringUtil.TYPE_STRING };
	int [] format_0w = {
		12,
		24,
		12,
		16,
		8,
		8,
		8,
		8,
		8,
		12 };
	String iline = null;
	List<Object> v = new Vector<Object>(10);
	BufferedReader in = null;
	StateMod_ReservoirRight aRight = null;

	Message.printStatus(2, routine, "Reading reservoir rights file: " + filename);
	
	try {
		in = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(filename)));
		while ((iline = in.readLine())!= null) {
			if (iline.startsWith("#")|| iline.trim().length()==0) {
				continue;
			}

			aRight = new StateMod_ReservoirRight();

			StringUtil.fixedRead(iline, format_0, format_0w, v);
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "iline: " + iline);
			}
			aRight.setID(((String)v.get(0)).trim());
			aRight.setName(((String)v.get(1)).trim());
			aRight.setCgoto(((String)v.get(2)).trim());
			aRight.setRtem(((String)v.get(3)).trim());
			aRight.setDcrres((Double)v.get(4));
			aRight.setSwitch((Integer)v.get(5));
			aRight.setIresco((Integer)v.get(6));
			aRight.setItyrstr((Integer)v.get(7));
			aRight.setN2fill((Integer)v.get(8));
			aRight.setCopid(((String)v.get(9)).trim());
			theRights.add(aRight);
		}
	} 
	catch (Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
	}
	return theRights;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_ReservoirRight r = (StateMod_ReservoirRight)_original;
	super.restoreOriginal();
	_rtem = r._rtem;
	_dcrres = r._dcrres;
	_iresco = r._iresco;
	_ityrstr = r._ityrstr;
	_n2fill = r._n2fill;
	_copid = r._copid;
	_isClone = false;
	_original = null;
}

/**
Set the out-of-priority associated op right.
*/
public void setCopid(String copid) {
	if ( (copid != null) && !copid.equals(_copid) ) {
		_copid = copid;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount.
*/
public void setDcrres(double dcrres) {
	if (dcrres != _dcrres) {
		_dcrres = dcrres;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount.
*/
public void setDcrres(Double dcrres) {
	setDcrres(dcrres.doubleValue());
}

/**
Set the decreed amount.
*/
public void setDcrres(String dcrres) {
	if (dcrres != null) {
		setDcrres(StringUtil.atod(dcrres.trim()));
	}
}

/**
Set the decree, as per the generic interface.
@param decree decree, in the units of the data.
*/
public void setDecree( double decree )
{	setDcrres(decree);
}

/**
Set the filling ratio.
*/
public void setIresco(int iresco) {
	if (iresco != _iresco) {
		_iresco = iresco;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the filling ratio.
*/
public void setIresco(Integer iresco) {
	setIresco(iresco.intValue());
}

/**
Set the filling ratio.
*/
public void setIresco(String iresco) {
	if (iresco != null) {
		setIresco(StringUtil.atoi(iresco.trim()));
	}
}

/**
Set the reservoir type.
*/
public void setItyrstr(int ityrstr) {
	if (ityrstr != _ityrstr) {
		_ityrstr = ityrstr;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the reservoir type.
*/
public void setItyrstr(Integer ityrstr) {
	setItyrstr(ityrstr.intValue());
}

/**
Set the reservoir type.
*/
public void setItyrstr(String ityrstr) {
	if (ityrstr != null) {
		setItyrstr(StringUtil.atoi(ityrstr.trim()));
	}
}

/**
Set the reservoir right type.
*/
public void setN2fill(int n2fill) {
	if (n2fill != _n2fill) {
		_n2fill = n2fill;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
Set the reservoir right type.
*/
public void setN2fill(Integer n2fill) {
	setN2fill(n2fill.intValue());
}

/**
Set the reservoir right type.
*/
public void setN2fill(String n2fill) {
	if (n2fill != null) {
		setN2fill(StringUtil.atoi(n2fill.trim()));
	}
}

/**
Set the administration number.
*/
public void setRtem(String rtem) {
	if ( (rtem != null) && !rtem.equals(_rtem) ) {
		_rtem = rtem;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_RESERVOIR_RIGHTS, true);
		}
	}
}

/**
@param dataset StateMod dataset object.
@return validation results.
 */
public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String cgoto = getCgoto();
	String irtem = getRtem();
	double dcrres = getDcrres();
	int iresco = getIresco();
	int ityrsr = getItyrstr();
	int n2fill = getN2fill();
	String copid = getCopid();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right identifier is blank.",
			"Specify a reservoir right identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id + "\" name is blank.",
			"Specify a reservoir right name to clarify data.") );
	}
	if ( StateMod_Util.isMissing(cgoto) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id +
			"\" reservoir station ID is blank.",
			"Specify a reservoir station to associate with the reservoir right.") );
	}
	else {
		// Verify that the reservoir station is in the data set, if the network is available
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_RESERVOIR_STATIONS);
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> resList = (List<StateMod_Reservoir>)comp2.getData();
		if ( (resList != null) && (resList.size() > 0) ) {
			if ( StateMod_Util.indexOf(resList, cgoto) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id +
					"\" associated reservoir (" + cgoto + ") is not found in the list of reservoir stations.",
					"Specify a valid reservoir station ID to associate with the reservoir right.") );
			}
		}
	}
	if ( StateMod_Util.isMissing(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id +
			"\" administration number is blank.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else if ( !StringUtil.isDouble(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id +
			"\" administration number (" + irtem + ") is invalid.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else {
		double irtemd = Double.parseDouble(irtem);
		if ( irtemd < 0 ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id +
				"\" administration number (" + irtem + ") is invalid.",
				"Specify an administration number NNNNN.NNNNN.") );
		}
	}
	if ( !(dcrres >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir right \"" + id + "\" decree (" +
			StringUtil.formatString(dcrres,"%.2f") + ") is invalid.",
			"Specify the decree as a number >= 0.") );
	}
	List<String> choices = getIrescoChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + iresco) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" ownership code (" + iresco + ") is invalid.",
			"Specify the ownership code as one of " + choices) );
	}
	choices = getItyrsrChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + ityrsr) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" right type (" + ityrsr + ") is invalid.",
			"Specify the right type as one of " + choices) );
	}
	choices = getN2fillChoices(false);
	if ( StringUtil.indexOfIgnoreCase(choices,"" + n2fill) < 0 ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Reservoir \"" + id +
			"\" first fill type (" + n2fill + ") is invalid.",
			"Specify the first fill type as one of " + choices) );
	}
	if ( ityrsr == -1 ) {
		if ( StateMod_Util.isMissing(copid) ) {
			validation.add(new StateMod_ComponentValidationProblem(this,
				"Reservoir right out-of-priority associated operational right identifier is blank.",
				"Specify an operational right identifier.") );
		}
	}
	return validation;
}

/**
Write reservoir right information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theRights vector of reservoir right to print
@param newComments addition comments which should be included in history
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile, List<StateMod_ReservoirRight> theRights, List<String> newComments)
throws Exception {
	writeStateModFile(infile, outfile, theRights, newComments, false);
}

/**
Write reservoir right information to output.  History header information 
is also maintained by calling this routine.
@param infile input file from which previous history should be taken
@param outfile output file to which to write
@param theRights list of reservoir right to print
@param newComments addition comments which should be included in history
@param oldAdminNumFormat whether to use the old admin num format or not
@exception Exception if an error occurs.
*/
public static void writeStateModFile(String infile, String outfile,
		List<StateMod_ReservoirRight> theRights, List<String> newComments, boolean oldAdminNumFormat)
throws Exception
{
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");	
	String routine = "StateMod_ReservoirRight.writeStateModFile";
	PrintWriter out = null;

	if (Message.isDebugOn) {
		Message.printDebug(2, routine, "Writing reservoir rights to file: " + outfile);
	}
	try {	
		out = IOUtil.processFileHeaders(
			IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		String iline = null;
		String cmnt = "#>";
		StateMod_ReservoirRight right = null;
		List<Object> v = new Vector<Object>(10);
		String format_0 = null;
		String format_1 = null;
		if (oldAdminNumFormat) {
			// Left justify...
			format_0 = "%-12.12s%-24.24s%-12.12s    %-12.12s%8.0f%8d%8d%8d%8d%-12.12s";
			format_1 = "%-12.12s%-24.24s%-12.12s    %-12.12s%8.2f%8d%8d%8d%8d%-12.12s";
		}
		else {	
			// Right justify...
			format_0 = "%-12.12s%-24.24s%-12.12s    %12.12s%8.0f%8d%8d%8d%8d%-12.12s";
			format_1 = "%-12.12s%-24.24s%-12.12s    %12.12s%8.2f%8d%8d%8d%8d%-12.12s";
		}
	
		out.println(cmnt);
		out.println(cmnt + " *******************************************************");
		out.println(cmnt + "  StateMod Reservoir Right File");
		out.println(cmnt);
		out.println(cmnt + "  format:  (a12, a24, a12, F16.5, f8.0, 4i8, a12)");
		out.println(cmnt);
		out.println(cmnt + "  ID       cirsid:  Reservoir right ID");
		out.println(cmnt + "  Name      namer:  Reservoir name");
		out.println(cmnt + "  Res ID    cgoto:  Reservoir ID tied to this right");
		out.println(cmnt + "  Admin #    rtem:  Administration number");
		out.println(cmnt + "                    (small is senior).");
		out.println(cmnt + "  Decree   dcrres:  Decreed amount (af)");
		out.println(cmnt + "  On/Off   irsrsw:  Switch 0 = off,1 = on");
		out.println(cmnt + "                    YYYY = on for years >= YYYY" );
		out.println(cmnt + "                    -YYYY = off for years > YYYY" );
		out.println(cmnt + "  Owner    iresco:  Ownership code");
		out.println(cmnt + "                      >0, account to be filled");
		out.println(cmnt + "                      <0, ownership go to 1st (n) accounts");
		out.println(cmnt + "  Type     ityrsr:  Reservoir type");
		out.println(cmnt + "                      1=Standard");
		out.println(cmnt + "                      2=Out of priority water right");
		out.println(cmnt + "  Fill #   n2fill:  Right type 1=1st fill, 2=2nd fill");
		out.println(cmnt + "  Out ID    copid:  Out of priority associated operational ");
		out.println(cmnt + "                      right  (when ityrsr=-1)");
		out.println(cmnt);
		out.println(cmnt + "    ID     Name                    Res ID            Admin #   Decree  On/Off  Owner   Type    Fill #  Out ID     ");
		out.println(cmnt + "---------eb----------------------eb----------eb--------------eb------eb------eb------eb------eb------eb----------e");
		out.println(cmnt);
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		int num = 0;
		if (theRights != null) {
			num = theRights.size();
		}
		for (int i = 0; i < num; i++) {
			right =(StateMod_ReservoirRight)theRights.get(i);
			if (right == null) {
				continue;
			}
			v.clear();
			v.add(right.getID());
			v.add(right.getName());
			v.add(right.getCgoto());
			v.add(right.getRtem());
			v.add(Double.valueOf(right.getDcrres()));
			v.add(Integer.valueOf(right.getSwitch()));
			v.add(Integer.valueOf(right.getIresco()));
			v.add(Integer.valueOf(right.getItyrstr()));
			v.add(Integer.valueOf(right.getN2fill()));
			v.add(right.getCopid());
			if (right.getDcrres()< 1.0) {
				// Use the format for a small right(8.2)...
				iline = StringUtil.formatString(v, format_1);
			}
			else {	
				// Default format 8.0...
				iline = StringUtil.formatString(v, format_0);
			}
			out.println(iline);
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
Writes a list of StateMod_ReservoirRight objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments comments to write to the top of the file.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update,
	List<StateMod_ReservoirRight> data, List<String> newComments ) 
throws Exception
{	String routine = "StateMod_ReservoirRight.writeListFile";
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("ID");
	fields.add("Name");
	fields.add("StructureID");
	fields.add("AdministrationNumber");
	fields.add("DecreedAmount");
	fields.add("OnOff");
	fields.add("AccountDistribution");
	fields.add("Type");
	fields.add("FillType");
	fields.add("OopRight");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_RESERVOIR_RIGHTS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	StateMod_ReservoirRight right = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	PrintWriter out = null;

	try {
		// Add some basic comments at the top of the file.  Do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector<String>();
		}
		else {
			newComments2 = new Vector<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod reservoir rights as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			right = (StateMod_ReservoirRight)data.get(i);
			
			line[0] = StringUtil.formatString(right.getID(),formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(),formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(),formats[2]).trim();
			line[3] = StringUtil.formatString(right.getRtem(),formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrres(),formats[4]).trim();
			line[5] = StringUtil.formatString(right.getSwitch(),formats[5]).trim();
			line[6] = StringUtil.formatString(right.getIresco(),formats[6]).trim();
			line[7] = StringUtil.formatString(right.getItyrstr(),formats[7]).trim();
			line[8] = StringUtil.formatString(right.getN2fill(),formats[8]).trim();
			line[9] = StringUtil.formatString(right.getCopid(),formats[9]).trim();

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
