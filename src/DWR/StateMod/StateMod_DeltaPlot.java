// StateMod_DeltaPlot - store the information generated by StateMod DelPlt utility

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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class StateMod_DeltaPlot extends StateMod_Data {

protected String	_type;	// "Difference", "Single", etc.
protected List<String>	_columnTitles;	// String vector, column titles
protected List<StateMod_DeltaPlotNode>	_nodes;	// StateMod_DeltaPlotNode vector

/**
Constructor.
*/
public StateMod_DeltaPlot ()
{	super ( );
	initialize ();
}

public void addColumnTitle(String s)
{	if ( s != null ) {
		_columnTitles.add (s);
	}
}

/**
Add big picture nodes.
*/
public void addNode ( StateMod_DeltaPlotNode node )
{	if ( node != null ) {
		_nodes.add ( node );	
	}
}

/**
Get the column titles.
*/
public String getColumnTitle(int index) {
	return (String)_columnTitles.get(index);
}

public int getColumnTitleSize() {
	return _columnTitles.size();
}

public StateMod_DeltaPlotNode getNode ( int index )
{	return (StateMod_DeltaPlotNode)_nodes.get(index);
}

/**
Return the type.
*/
public String getType() {
	return _type;
}

private void initialize ()
{	_type = "";
	_columnTitles = new Vector<String>();
	_nodes = new Vector<StateMod_DeltaPlotNode>();
}

public int nodesSize () {
	return _nodes.size();
}

/**
Set the type.
*/
public void setType ( String s )
{	if ( s != null ) {
		_type = s;
	}
}

public int writeArcViewFile ( PrintWriter out )
throws IOException
{	String rtn = "StateMod_DeltaPlot.writeArcViewFile";
	String iline = null;
	String type = null;
	String yrOrAve = null;
	String colTitle = null;
	List<String> list = null;
	StateMod_DeltaPlotNode node = null;
	String id = null;
	String name = null;

	try {	type = getType();
		boolean include_type = false;
		if ( 	type.equalsIgnoreCase("Difference") ||
			type.equalsIgnoreCase("Merge") ||
			type.equalsIgnoreCase("Diffx")) {
			include_type = true;
		}

		// print header
		yrOrAve = "";
		colTitle = "";
		iline = "Identifier, Name";
		if ( include_type ) {
			iline += ", " + type;
		}
		int size = _columnTitles.size();
		for ( int i=0; i<size; i++ ) {
			list = StringUtil.breakStringList (
				_columnTitles.get(i), " ", 
				StringUtil.DELIM_SKIP_BLANKS );
			yrOrAve = list.get(0);
			colTitle = list.get(1).replace(',','_');
			if ( !include_type ) {
				iline += ",";
			}
			iline += " " + yrOrAve + " " + colTitle;
		}
		out.println ( iline );

		// print data
		int num = _nodes.size();
		for ( int i=0; i<num; i++ ) {
			node = (StateMod_DeltaPlotNode)_nodes.get(i);
			id = node.getID();
			name = node.getName();
			if ( id == null || id.length() == 0 ) {
				id = " ";
			}
			if ( name == null || name.length() == 0 ) {
				name = " ";
			}
			else {	name = name.replace(',','_');
			}

			iline = "\"" + id + "\"," + name;
			int numz = node.getZsize();
			for ( int j=0; j<numz; j++ ) {
				iline += "," + node.getZ(j);
			}

			out.println ( iline );

		}
	} catch (Exception e) {
		rtn = null;
		iline = null;
		type = null;
		yrOrAve = null;
		colTitle = null;
		list = null;
		node = null;
		id = null;
		name = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
	
	rtn = null;
	iline = null;
	type = null;
	yrOrAve = null;
	colTitle = null;
	list = null;
	node = null;
	id = null;
	name = null;
	return 0;
}

/**
Read delplt output information and store.
*/
public int readStateModDeltaOutputFile ( String filename )
throws IOException
{	String rtn = "StateMod_DeltaPlot.readStateModDeltaOutputFile";
	String iline = null;
	BufferedReader in = null;
	List<String> list1 = null;
	String a = null;
	Integer b = null;
	String format ="s12s1s24s1d10s1d10s1d10s1d10s1d10s1d10s1d10" +
			"s1d10s1d10s1d10s1d10s1d10s1d10s1d10s1d10s1d10s1d10" +
			"s1d10s1d10s1d10";
	List<Object> v = null;

	Message.printStatus ( 1, rtn, "Reading delplt output file: " 
		+ filename );
	try {	in = new BufferedReader ( new FileReader (filename));

		iline = in.readLine().trim();
		list1 = StringUtil.breakStringList (
			iline, " ", StringUtil.DELIM_SKIP_BLANKS );
		if ( list1.size() < 2 ) {
			Message.printWarning ( 1, rtn, 
			"Unknown format for first line in delplt output file: "
			+ iline );
			in.close();
			return 1;
		}
		
		// first line: nz, nbigtitles
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, rtn, iline );
		}

		a = list1.get(0);
		//Message.printDebug ( 10, rtn, a );
		b = Integer.valueOf ( a );
		int nz = b.intValue();
		//Message.printDebug ( 10, rtn, "" + nz );
		int nbigtitles = (Integer.valueOf ((String)list1.get(1))).
			intValue();

		// second line: type
		iline = in.readLine();
		setType ( iline.trim() );

		// line 3 through nbigtitles:  
		for ( int i=0; i<nbigtitles; i++ ) {
			iline = in.readLine();
			addColumnTitle ( iline.trim() );	
		}

		// CAT !!!  Don't forget to come back to this ...
		// check here for props.BP_GET_COLUMN_TITLES_ONLY
		// CAT !!!
		
		// now read data
		while (( iline = in.readLine()) != null ) {
			try {
			StateMod_DeltaPlotNode node = new StateMod_DeltaPlotNode();
			v = StringUtil.fixedRead ( iline, format );
			/* some names may contain commas; use fixed format read
			Vector list2 = StringUtil.breakStringList (
				iline, ",", StringUtil.DELIM_SKIP_BLANKS );
			*/
			if ( v.size() < 2+nz ) {
				Message.printWarning ( 1, rtn, 
				"Unknown format for input line in " +
				"delplt output file: "
				+ iline );
				return 1;
			}
			node.setID(((String)v.get(0)).trim());
			node.setName(((String)v.get(2)).trim());
			for ( int j=2; j<2+nz; j++ )
				node.addZ((Double)v.get(2*j));

			_nodes.add ( node );
			} catch ( Exception e ) {
				Message.printWarning ( 2, rtn, 
					"Trouble parsing xgr line \"" + 
					iline + "\"");
				Message.printWarning ( 5, rtn, e );
			}
		}
	} catch (Exception e) {
		rtn = null;
		iline = null;
		if ( in != null ) {
			in.close();
		}
		in = null;
		list1 = null;
		a = null;
		b = null;
		format = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}

	rtn = null;
	iline = null;
	if ( in != null ) {
		in.close();
	}
	in = null;
	list1 = null;
	a = null;
	b = null;
	format = null;
	return 0;
}

public void writeArcViewFile ( String instrfile, String outstrfile,
	String[] new_comments )
throws IOException
{	String rtn = "StateMod_DeltaPlot.writeArcViewFile";
	PrintWriter out = null;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, rtn, 
		"in writeArcViewFile printing file: " 
		+ outstrfile );
	}
	try {	out = new PrintWriter ( new FileOutputStream ( outstrfile ));
		this.writeArcViewFile ( out );
		out.flush();
		out.close();
		out = null;
		rtn = null;
	} catch ( Exception e ) {
		if ( out != null ) {
			out.close();
		}
		out = null;
		rtn = null;
		Message.printWarning ( 2, rtn, e );
		throw new IOException ( e.getMessage());
	}
}

}
