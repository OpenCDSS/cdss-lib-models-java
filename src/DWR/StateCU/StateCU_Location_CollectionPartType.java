// StateCU_Location_CollectionPartType - collection part types for StateCU location

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

package DWR.StateCU;

/**
This enumeration defines collection part types for StateCU locations.
*/
public enum StateCU_Location_CollectionPartType
{
    /**
    Ditch means that diversions/wells are put in a collection using a ditch ID (wells are determined by association with ditch).
    */
    DITCH("Ditch"),
    /**
    Parcel means that wells are put in a collection using a parcel ID (wells are determined by association with parcel).
    This is only used with the legacy Rio Grande approach.
    */
    PARCEL("Parcel"),
    /**
    Well means that wells are put in a collection using a well ID (can be a WDID or a permit receipt).
    See also StateCU_Location_CollectionPartIdType.
    */
    WELL("Well");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateCU_Location_CollectionPartType(String displayName) {
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
public static StateCU_Location_CollectionPartType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    StateCU_Location_CollectionPartType [] values = values();
    for ( StateCU_Location_CollectionPartType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}