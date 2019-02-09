// StateMod_ComponentValidator - this interface should be implemented by StateMod data object
// classes and provides validation within a dataset.

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
