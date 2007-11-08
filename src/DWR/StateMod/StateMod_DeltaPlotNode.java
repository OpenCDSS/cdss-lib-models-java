//------------------------------------------------------------------------------
// StateMod_DeltaPlotNode - Stores information about a station to be graphed.  
//	Used in conjunction with StateMod_DeltaPlot.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 30 Nov 1998	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi
// 17 Feb 2001	Steven A. Malers, RTi	Review code.  Clean up javadoc.  Add
//					finalize().  Handle nulls.  Alphabetize
//					methods.  Set unused variables to null.
//					Remove unneeded imports.
//------------------------------------------------------------------------------
// 2003-08-26	J. Thomas Sapienza, RTi	Initial StateMod version from 
//					SMBigPictNode.
//------------------------------------------------------------------------------

package DWR.StateMod;

import java.util.Vector;

import RTi.Util.String.StringUtil;

public class StateMod_DeltaPlotNode extends StateMod_Data {

protected String	_id;
protected String	_name;
protected double 	_x;	// 
protected double	_y;	// 
protected Vector	_z;	// double vector, z components

/**
Constructor.
*/
public StateMod_DeltaPlotNode ()
{	super ( );
	initialize ();
}

public void addZ ( double d ) {
	addZ ( new Double (d));
}

public void addZ ( Double d ) {
	_z.addElement (d);
}

public void addZ ( String str )
{	if ( str == null ) {
		return;
	}
	addZ ( StringUtil.atod(str.trim()));
}

/**
Clean up before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_id = null;
	_name = null;
	_x = -999;
	_y = -999;
	_z = null;
	super.finalize();
}

/**
Get id.
*/
public String getID() {
	return _id;
}

/**
Get name.
*/
public String getName() {
	return _name;
}

/**
Get x coordinate.
*/
public double getX() {
	return _x;
}

/**
Get y coordinate.
*/
public double getY() {
	return _y;
}

public double getZ ( int i ) {
	Double d = (Double)_z.elementAt(i);
	return d.doubleValue();
}

public int getZsize ( ) {
	return _z.size();
}

private void initialize ()
{	_id = "";
	_name = "";
	_x = -999;
	_y = -999;
	_z = new Vector (10,10);
}

public void setID ( String s )
{	if ( s != null ) {
		_id = s;
	}
}

public void setName ( String s )
{	if ( s != null ) {
		_name = s;
	}
}

public void setX ( double d ) {
	_x = d;
}

public void setX ( String str )
{	if ( str != null ) {
		setX ( StringUtil.atod(str.trim()));
	}
}

public void setY ( double d ) {
	_y = d;
}

public void setY ( String str )
{	if ( str == null ) {
		return;
	}
	setY ( StringUtil.atod(str.trim()));
}

}
