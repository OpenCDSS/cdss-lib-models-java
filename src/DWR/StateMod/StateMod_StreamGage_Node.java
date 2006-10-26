//------------------------------------------------------------------------------
// StateMod_RiverStation_Node - RiverStation node for the network
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2003-07-30	Steven A. Malers, RTi	Initial version.
// 2003-09-11	SAM, RTi		Rename class from
//					StateMod_RiverStation_Node to
//					StateMod_StreamGage_Node.
//------------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.IO.Node;

/**
Network object for StateMod StreamGage node.
*/
public class StateMod_StreamGage_Node extends Node
{

/**
Constructor.
*/
public StateMod_StreamGage_Node ( String id, String name )
{	super ( id, name, "StreamGage" );
}

} // End StateMod_StreamGage_Node
