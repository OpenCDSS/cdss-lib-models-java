// StateMod_DeltaPlotNode - Stores information about a station to be graphed.

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

import java.util.List;
import java.util.Vector;

import RTi.Util.String.StringUtil;

public class StateMod_DeltaPlotNode extends StateMod_Data {

protected String	_id;
protected String	_name;
protected double 	_x;	// 
protected double	_y;	// 
protected List<Double>	_z;

/**
Constructor.
*/
public StateMod_DeltaPlotNode ()
{	super ( );
	initialize ();
}

public void addZ ( double d ) {
	addZ ( Double.valueOf (d));
}

public void addZ ( Double d ) {
	_z.add (d);
}

public void addZ ( String str )
{	if ( str == null ) {
		return;
	}
	addZ ( StringUtil.atod(str.trim()));
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
	Double d = (Double)_z.get(i);
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
	_z = new Vector<Double>(10,10);
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
