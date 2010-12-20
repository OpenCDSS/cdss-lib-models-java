package DWR.StateMod;

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
Construct an instance from primitive data.
*/
public StateMod_Network_AnnotationData ( StateMod_Network_AnnotationRenderer annotationRenderer,
	Object object, String label )
{	__annotationRenderer = annotationRenderer;
	__object = object;
	__label = label;
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
Return the StateMod_Network_AnnotationRenderer for the data.
@return the StateMod_Network_AnnotationRenderer for the data
*/
public StateMod_Network_AnnotationRenderer getStateModNetworkAnnotationRenderer ()
{	return __annotationRenderer;
}

/**
Return the label for the object.
@return the label for the object
*/
public String getLabel ()
{	return __label;
}

/**
Return the object to be rendered.
@return the object to be rendered
*/
public Object getObject ()
{	return __object;
}

}