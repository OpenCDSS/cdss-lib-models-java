package DWR.StateMod;

import RTi.GR.GRLimits;

/**
This class provides for data management of StateMod_Network_AnnotationRenderer instances and associated data so
that the information can be used to provide a list of annotations in the network editor and provide
data back to the renderers when the annotations need to be rendered.
*/
public class StateMod_Network_AnnotationData
{

/**
Renderer for the data object.
*/
private StateMod_Network_AnnotationRenderer __annotationRenderer = null;

/**
Object that will be rendered.
*/
private Object __object = null;

/**
Label for the object (displayed in the network editor).
*/
protected String __label = null;

/**
Data limits for the rendered object (network coordinate system data units).
*/
private GRLimits __limits = null;

/**
Construct an instance from primitive data.
@param annotationRenderer the object that will render the data object
@param object the data object to be rendered
@param label the label to display on the rendered object
@param limits the data limits for the object, to simplify zooming
*/
public StateMod_Network_AnnotationData ( StateMod_Network_AnnotationRenderer annotationRenderer,
	Object object, String label, GRLimits limits )
{	__annotationRenderer = annotationRenderer;
	__object = object;
	__label = label;
	setLimits(limits);
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{
	super.finalize();
}

/**
Return the label for the object.
@return the label for the object
*/
public String getLabel ()
{	return __label;
}

/**
Return the limits for the object.
@return the limits for the object
*/
public GRLimits getLimits ()
{	return __limits;
}

/**
Return the object to be rendered.
@return the object to be rendered
*/
public Object getObject ()
{	return __object;
}


/**
Return the StateMod_Network_AnnotationRenderer for the data.
@return the StateMod_Network_AnnotationRenderer for the data
*/
public StateMod_Network_AnnotationRenderer getStateModNetworkAnnotationRenderer ()
{	return __annotationRenderer;
}

/**
Set the limits for the object.  These may need to be set after the initial construction because
the renderer has more information about the extent of the annotation.
@param the limits for the object
*/
private void setLimits ( GRLimits limits )
{	__limits = limits;
}

}