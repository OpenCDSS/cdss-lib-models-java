// StateMod_Diversion_Node - diversion node for the network

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

//------------------------------------------------------------------------------
// StateMod_Diversion_Node - diversion node for the network
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2003-07-30	Steven A. Malers, RTi	Initial version.
//------------------------------------------------------------------------------

package DWR.StateMod;

import RTi.Util.IO.Node;

/**
Network object for StateMod diversion node.
*/
public class StateMod_Diversion_Node extends Node
{

/**
Constructor.
*/
public StateMod_Diversion_Node ( String id, String name )
{	super ( id, name, "Diversion" );
}

} // End StateMod_Diversion_Node
