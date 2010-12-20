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
	 * @param objectToRender the object to render as an annotation on the map
	 * @param label the string that is used to label the annotation on the map
	 */
	public void renderStateModNetworkAnnotation ( StateMod_Network_JComponent geoviewJComponent,
		Object objectToRender, String label );
	
	/**
	 * Return the object to render.
	 */
	//public Object getAnnotationObject ();
	
	/**
	 * Return the label for the object to render.  This will be listed in the GeoViewPanel.
	 */
	//public String getAnnotationLabel ();
}