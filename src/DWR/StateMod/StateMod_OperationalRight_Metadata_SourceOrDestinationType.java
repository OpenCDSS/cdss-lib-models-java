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
     * Plans as per StateMod documentation.
     */
    PLAN_ACCOUNTING("Plan (Accounting Plan)"),
    PLAN_OUT_OF_PRIORITY("Plan (Out of Priority Diversion or Storage)"),
    PLAN_RECHARGE("Plan (Recharge)"),
    PLAN_RELEASE_LIMIT("Plan (Release Limit Plan)"),
    PLAN_REUSE_TO_RESERVOIR("Plan (Reuse to Reservoir)"),
    PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN("Plan (Reuse to Diversion from Transmountain)"),
    PLAN_REUSE_TO_DIVERSION("Plan (Reuse to Diversion)"),
    PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN("Plan (Reuse to Diversion from Transmountain)"),
    PLAN_SPECIAL_WELL_AUGMENTATION("Plan (Special Well Augmentation)"),
    PLAN_TC("Plan (Terms & Conditions)"),
    PLAN_TRANSMOUNTAIN_IMPORT("Plan (Transmountain Import)"),
    PLAN_WELL_AUGMENTATION("Plan (Well Augmentation)"),
    /**
     * Reservoir station.
     */
    RESERVOIR("Reservoir"),
    /**
     * Reservoir right.
     */
    RESERVOIR_RIGHT("Reservoir Right"),
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