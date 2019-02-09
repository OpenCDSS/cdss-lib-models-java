// StateMod_OperationalRight_Metadata_SourceOrDestinationType - enumeration to store values
// for allowed operational right source and destination types

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

// TODO SAM 2010-12-12 Evaluate how these compare to dataset components - seems like sources can in some cases
// be a subtype of a component (e.g., different types of plans).
/**
This enumeration stores values for allowed operational right source and destination types,
which can be used to perform checks and visualization.
*/
public enum StateMod_OperationalRight_Metadata_SourceOrDestinationType
{
    /**
     * Carrier (diversion that is a carrier).
     */
    CARRIER("Carrier"),
    /**
     * Diversion station.
     */
    DIVERSION("Diversion"),
    /**
     * Diversion right.
     */
    DIVERSION_RIGHT("Diversion Right"),
    /**
     * Downstream call.
     * TODO SAM 2010-12-11 Should this just be a list of node types (other values in enum)?
     */
    DOWNSTREAM_CALL("Downstream Call"),
    /**
     * Instream flow station.
     */
    INSTREAM_FLOW("Instream Flow"),
    /**
     * Instream flow right.
     */
    INSTREAM_FLOW_RIGHT("Instream Flow Right"),
    /**
     * Operational right.
     */
    OPERATIONAL_RIGHT("Operational Right"),
    /**
     * Other node - in network but not any specific station type.
     */
    OTHER("Other"),
    /**
     * Plans as per StateMod documentation (shorten names because show up in UI choices with limited space)
     */
    PLAN_ACCOUNTING("Plan (Accounting)"), // Type 11
    PLAN_OUT_OF_PRIORITY("Plan (OutOfPriority Div, Strg)"), // Type 9
    PLAN_RECHARGE("Plan (Recharge)"), // Type 8
    PLAN_RELEASE_LIMIT("Plan (Release Limit)"), // Type 12
    PLAN_REUSE_TO_RESERVOIR("Plan (Reuse to Res)"), // Type 3
    PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN("Plan (Reuse to Res Transmtn)"), // Type 5
    PLAN_REUSE_TO_DIVERSION("Plan (Reuse to Div)"), // Type 4
    PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN("Plan (Reuse to Div Transmtn)"), // Type 6
    PLAN_SPECIAL_WELL_AUGMENTATION("Plan (Special Well Aug)"), // Type 10
    PLAN_TC("Plan (T&C)"), // Type 1
    PLAN_TRANSMOUNTAIN_IMPORT("Plan (Transmtn Import)"), // Type 7
    PLAN_WELL_AUGMENTATION("Plan (Well Aug)"), // Type 2
    /**
     * Reservoir station.
     */
    RESERVOIR("Reservoir"),
    /**
     * Reservoir right.
     */
    RESERVOIR_RIGHT("Reservoir Right"),
    /**
     * River node.
     */
    RIVER_NODE("River Node"),
    /**
     * Stream gage.
     */
    STREAM_GAGE("Stream Gage"),
    /**
     * Well station.
     */
    WELL("Well"),
    /**
     * Well right.
     */
    WELL_RIGHT("Well Right"),
    /**
     * Not applicable.
     */
    NA("NA");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_OperationalRight_Metadata_SourceOrDestinationType(String displayName) {
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
public static StateMod_OperationalRight_Metadata_SourceOrDestinationType valueOfIgnoreCase(String name)
{
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] values = values();
    // Currently supported values
    for ( StateMod_OperationalRight_Metadata_SourceOrDestinationType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
