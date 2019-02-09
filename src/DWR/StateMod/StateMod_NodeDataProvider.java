// StateMod_NodeDataProvider - This interface provides data for a node, for example by querying a database.

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

package DWR.StateMod;

/**
 * This interface provides data for a node, for example by querying a database.
 *
 */
public interface StateMod_NodeDataProvider
{
	/**
	 * Set the descriptions for the nodes, beyond what is already set.  For example,
	 * this will cause HydroBase to be queried to fill descriptions given the IDs.
	 * @param network StateMod_NodeNetwork containing the nodes.
	 * @param createFancyDescription legacy flag indicating how to create description.
	 * @param createOutputFiles legacy flag indicating whether output files should be created.
	 */
	public void setNodeDescriptions ( StateMod_NodeNetwork network, boolean createFancyDescription,
			boolean createOutputFiles );
	
	/**
	Looks up the location of a structure in the database.
	@param identifier the identifier of the structure (WDID).
	@return a two-element array, the first element of which is the X location
	and the second of which is the Y location.  If none can be found, both values
	will be -1.
	*/
	public double[] lookupNodeLocation( String identifier);
}
