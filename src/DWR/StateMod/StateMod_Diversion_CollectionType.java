// StateMod_Diversion_CollectionType - collection types for diversion

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.StateMod;

/**
This enumeration defines collection types for StateMod diversions.
*/
public enum StateMod_Diversion_CollectionType
{
    /**
    Aggregate is similar to System but means that water rights for a group of diversions are aggregated into classes.
    */
    AGGREGATE("Aggregate"),
    /**
	MultiStruct - should be used to represent two or more structures
	that divert from DIFFERENT TRIBUTARIES to serve the same demand
	(irrigated acreage or M&I demand).  In the Historic model used to
	estimate Baseflows, the historic diversions need to be represented on
	the correct tributary, so all structures are in the network.  Average
	efficiencies need to be set for these structures, since IWR has been
	assigned to only one structure.  In Baseline and Calculated mode, the
	MultiStruct collection will assign all demand to the primary structure
	and zero out the demand for the secondary structures.  Water rights will
	continue to be assigned to each individual structure, and operating
	rules need to be included to allow the model to divert from the
	secondary structure location (under their water right) to meet the
	primary structure demand.
    */
    MULTISTRUCT("MultiStruct"),
    /**
    System means that water rights for a group of diversions retain individual values.
	Should be used to represents two or more structures with
	intermingled lands and/or diversions that divert from the SAME
	TRIBUTARY.  Only the primary structure should be included in the
	network.  The System will combine historic diversions,
	capacities, and acreages for use in the Historic model and to create
	Baseflows.  Water rights for all structures will be assigned explicitly
	to the primary structure.  No operating rules or set efficiency commands are required.
    */
    SYSTEM("System");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_Diversion_CollectionType(String displayName) {
        this.displayName = displayName;
    }

/**
 * Return the display name for the running average type.  This is usually similar to the
 * value but using appropriate mixed case.
 * @return the display name.
 */
@Override
public String toString() {
    return displayName;
}

/**
 * Return the enumeration value given a string name (case-independent).
 * @return the enumeration value given a string name (case-independent), or null if not matched.
 */
public static StateMod_Diversion_CollectionType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    StateMod_Diversion_CollectionType [] values = values();
    for ( StateMod_Diversion_CollectionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}