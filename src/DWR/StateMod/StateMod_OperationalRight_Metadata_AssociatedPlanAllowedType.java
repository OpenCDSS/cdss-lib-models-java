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
    PLAN_OUT_OF_PRIORITY("Plan (Out of Priority Diversion or Storage)"),
    PLAN_RECHARGE("Plan (Recharge)"),
    PLAN_REUSE_TO_RESERVOIR("Plan (Reuse to Reservoir)"),
    PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN("Plan (Reuse to Diversion from Transmountain)"),
    PLAN_REUSE_TO_DIVERSION("Plan (Reuse to Diversion)"),
    PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN("Plan (Reuse to Diversion from Transmountain)"),
    PLAN_TC("Plan (Terms & Conditions)"),
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