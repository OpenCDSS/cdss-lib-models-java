package DWR.StateMod;

/**
This interface should be implemented by StateMod data object classes and provides validation within a
dataset.
 */
public interface StateMod_ComponentValidator {
    // TODO SAM 2009-05-03 Evaluate how to turn off some dataset checks when dealing with circular and/or
    // partial dataset relationships.  Hopefully can decide on a convention and implement good default
    // validation.
    /**
     * Validate a StateMod data object given the context of the data set.  For example,
     * to fully check one component may require checking other components.
     * @param dataset the StateMod_DataSet that is managing the individual object.  If null, do not
     * perform dataset checks.
     * @return validation results
     */
    StateMod_ComponentValidation validateComponent ( StateMod_DataSet dataset );
}