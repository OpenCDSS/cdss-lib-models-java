package DWR.StateMod;

import java.util.List;

import cdss.domain.hydrology.network.UpstreamFlowNodeI;

/**
 * Class to store gage data for StateDMI SetStreamEstimateCoefficientsPFGage() command
 * used with the network.  This implementation decouples the StateMod and CDSS node network packages.
 * @author sam
 *
 */
public class UpstreamFlowNodeA implements UpstreamFlowNodeI
{

	/**
	 * List of StateMod_PrfGageData to indicate "neighboring" gages that should be used as if
	 * they are upstream gages.
	 */
	List<StateMod_PrfGageData> __prfGageData;
	
	/**
	 * Constructor (immutable).
	 */
	public UpstreamFlowNodeA ( List<StateMod_PrfGageData>prfGageData )
	{
		__prfGageData = prfGageData;
	}
	
	/**
	Checks to see if the node is one where a "neighboring gage" is to be used for the upstream
	flow node.  See the StateDMI SetStreamEstimateCoefficientsPFGage() command.
	@param commonID the id of the node to check
	@return the number of the node if it is to receive information, or -1 if
	the node wasn't found or is not to receive information.
	*/
	public int isSetprfTarget ( String commonID )
	{
		return StateMod_PrfGageData.isSetprfTarget(commonID, __prfGageData);
	}
}