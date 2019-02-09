// StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType - enumeration that stores values
// for whether an operational right allows an associated plan,

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
This enumeration stores values for whether an operational right allows an associated plan,
which can be used to perform checks and visualization.
*/
public enum StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType
{
    /**
     * Not applicable (associated plan not allowed).
     */
    NA("NA"),
    /**
     * Reuse plans
     */
    PLAN_ACCOUNTING("Plan (Accounting)"),
    PLAN_OUT_OF_PRIORITY("Plan (Out of Priority Diversion or Storage)"),
    PLAN_RECHARGE("Plan (Recharge)"),
    PLAN_RELEASE_LIMIT("Plan (Release Limit)"),
    PLAN_REUSE_TO_RESERVOIR("Plan (Reuse to Reservoir)"),
    PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN("Plan (Reuse to Diversion from Transmountain)"),
    PLAN_REUSE_TO_DIVERSION("Plan (Reuse to Diversion)"),
    PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN("Plan (Reuse to Diversion from Transmountain)"),
    PLAN_SPECIAL_WELL_AUGMENTATION("Plan (Special Well Augmentation)"),
    PLAN_TC("Plan (Terms & Conditions)"),
    PLAN_TRANSMOUNTAIN_IMPORT("Plan (Transmountain Import)"),
    PLAN_WELL_AUGMENTATION("Plan (Well Augmentation)");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType(String displayName) {
        this.displayName = displayName;
    }
    
/**
Lookup the source or destination type from a plan type.
@return the matching source or destination type, or null if not matched.
*/
public StateMod_OperationalRight_Metadata_SourceOrDestinationType getMatchingSourceOrDestinationType ()
{
	switch ( this ) {
		// List in StateMod documentation order
		case PLAN_TC:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC;
		case PLAN_WELL_AUGMENTATION:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION;
		case PLAN_REUSE_TO_RESERVOIR:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR;
		case PLAN_REUSE_TO_DIVERSION:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION;
		case PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN;
		case PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN;
		case PLAN_TRANSMOUNTAIN_IMPORT:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TRANSMOUNTAIN_IMPORT;
		case PLAN_RECHARGE:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_RECHARGE;
		case PLAN_OUT_OF_PRIORITY:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_OUT_OF_PRIORITY;
		case PLAN_SPECIAL_WELL_AUGMENTATION:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_SPECIAL_WELL_AUGMENTATION;
		case PLAN_ACCOUNTING:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING;
		case PLAN_RELEASE_LIMIT:
			return StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_RELEASE_LIMIT;
	}
	// No match
	return null;
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
public static StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType valueOfIgnoreCase(String name)
{
	StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] values = values();
    // Currently supported values
    for ( StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
