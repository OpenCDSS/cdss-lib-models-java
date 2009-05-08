package DWR.StateMod;

/**
Simple class to hold validation problem for a StateMod component object, including the object reference,
the problem description, and the recommendation to fix.
*/
public class StateMod_ComponentValidationProblem
{
	
/**
Object that has the problem.
*/
private StateMod_ComponentValidator __data;

/**
Validation problem.
*/
private String __problem = "";

/**
Validation recommendation.
*/
private String __recommendation = "";

/**
Constructor.  Null parameters will be converted to empty strings.  Simple validation issues may not
have a recommendation.  More complex problems, for example when cross-checking dataset components, may have
a correspondingly more complex recommendation.
@param data object that has the problem.
@param problem description of validation problem.
@param recommendation recommendation of how to correct the problem.
*/
public StateMod_ComponentValidationProblem ( StateMod_ComponentValidator data, String problem, String recommendation )
{
	__problem = problem;
	if ( __problem == null ) {
		__problem = "";
	}
	__data = data;
	__recommendation = recommendation;
	if ( __recommendation == null ) {
		__recommendation = "";
	}
}

/**
Return the data object that has the problem.
*/
public StateMod_ComponentValidator getData()
{
	return __data;
}

/**
Return the problem description.
*/
public String getProblem()
{
	return __problem;
}

/**
Return the recommendation description.
*/
public String getRecommendation()
{
	return __recommendation;
}

}