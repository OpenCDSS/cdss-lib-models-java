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
