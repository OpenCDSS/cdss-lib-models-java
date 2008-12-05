/******************************************************************************
* File: StateCU_Component.java
* Author: KAT
* Date: 2007-04-11
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-04-11	Kurt Tometich	Initial version.
******************************************************************************/
package DWR.StateCU;

import RTi.Util.IO.PropList;

/**
Interface for StateCU components to implement.  
 */
public interface StateCU_Component {
	// This method provides for a more abstract coding when running
	// data checks on a specific component.
	// Each component should implement this method.
    String[] checkComponentData( int count, StateCU_DataSet dataset, PropList props );
}
