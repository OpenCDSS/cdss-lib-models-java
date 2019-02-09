// StateMod_Network_AnnotationRenderer - Objects that implement this interface
// can be added to the StateMod_Network_JComponent to annotate the
// network by drawing additional objects on top of the network.

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
Objects that implement this interface can be added to the StateMod_Network_JComponent to annotate the
network by drawing additional objects on top of the network.  This is useful, for example, to highlight
information beyond a normal selection.  For example, the annotation might show the relationships between
nodes.
*/
public interface StateMod_Network_AnnotationRenderer {

	/**
	 * This method will be called by the StateMod_Network_JComponent when rendering the map,
	 * passing back the object from getAnnotationObject().
	 * @param geoviewJComponent the map object
	 * @param annotationData the annotation data to be used during rendering
	 */
	public void renderStateModNetworkAnnotation ( StateMod_Network_JComponent geoviewJComponent,
		StateMod_Network_AnnotationData annotationData );
}
