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