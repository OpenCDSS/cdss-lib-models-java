// StateMod_ComponentValidation - simple class to hold validation data for a StateMod component object

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

import java.util.List;
import java.util.Vector;

/**
Simple class to hold validation data for a StateMod component object.  An instance of this class is returned
by the StateMod_Component.validateComponent() method.
*/
public class StateMod_ComponentValidation
{

/**
List of component data validation items - specific problems.
*/
private List<StateMod_ComponentValidationProblem> __validationProblems = new Vector<StateMod_ComponentValidationProblem>();

/**
Add a validation result.
@param validationItem a validation problem.
*/
public void add ( StateMod_ComponentValidationProblem item ) {
	__validationProblems.add ( item );
}

/**
Get a validation problem.
*/
public StateMod_ComponentValidationProblem get ( int i ) {
	return __validationProblems.get ( i );
}

/**
Get all validation problems.
*/
public List<StateMod_ComponentValidationProblem> getAll () {
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