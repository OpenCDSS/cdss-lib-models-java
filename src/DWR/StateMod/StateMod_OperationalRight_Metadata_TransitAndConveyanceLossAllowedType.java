// StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType - enumeration to stores values
// for operational right diversion type

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
This enumeration stores values for operational right diversion type,
which can be used to perform checks and visualization.
*/
public enum StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType
{
    /**
     * Not applicable.
     */
    NA("NA"),
    /**
     * Yes.
     */
    YES("Yes");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType(String displayName) {
        this.displayName = displayName;
    }

/**
 * Return the display name for the statistic.  This is usually the same as the
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
public static StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType valueOfIgnoreCase(String name)
{
	StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType [] values = values();
    // Currently supported values
    for ( StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
