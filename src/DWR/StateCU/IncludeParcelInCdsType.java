// IncludeParcelInCdsType - whether to include the parcel in CDS file

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
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.StateCU;

/**
This enumeration defines location types for StateCU locations,
based on whether surface or groundwater supply are available.
*/
public enum IncludeParcelInCdsType
{
    /**
     * Error because of input data problem - needs to be resolved.
     */
    ERROR("ERR"),
    /**
     * Parcel should not be included in CDS - should not generally be the case.
     */
    NO("NO"),
    /**
     * Parcel should be included in the CDS.
     */
    YES("YES"),
    /**
     * Unknown - typically because not yet processed.
     */
    UNKNOWN("UNK");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private IncludeParcelInCdsType(String displayName) {
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
public static IncludeParcelInCdsType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    IncludeParcelInCdsType [] values = values();
    for ( IncludeParcelInCdsType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}