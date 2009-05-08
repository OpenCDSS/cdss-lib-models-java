package DWR.StateCU;

import java.util.List;
import java.util.Vector;

/**
Simple class to hold validation data for a StateCU component object.  An instance of this class is returned
by the StateCU_Component.validateComponent() method.
*/
public class StateCU_ComponentValidation
{

/**
List of component data validation items - specific problems.
*/
private List<StateCU_ComponentValidationProblem> __validationProblems = new Vector();

/**
Add a validation result.
@param validationItem a validation problem.
*/
public void add ( StateCU_ComponentValidationProblem item ) {
	__validationProblems.add ( item );
}

/**
Get a validation problem.
*/
public StateCU_ComponentValidationProblem get ( int i ) {
	return __validationProblems.get ( i );
}

/**
Get all validation problems.
*/
public List<StateCU_ComponentValidationProblem> getAll () {
	return __validationProblems;
}

/**
Return the number of validation problems for the object.
@return the number of validation problems for the object.
*/
public int size ()
{
	return __validationProblems.size ();
}

}