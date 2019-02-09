// StateMod_Network_AnnotationDataListJPanel - panel to hold a list of StateMod_Network_AnnotationData

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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import RTi.Util.GUI.JGUIUtil;

/**
A panel to hold a list of StateMod_Network_AnnotationData, to allow interaction such as clearing the list
of annotations.
*/
public class StateMod_Network_AnnotationDataListJPanel extends JPanel implements ActionListener
{

/**
The JList that manages the list of annotations (labels).
*/
private JList __annotationJList = null;

/**
Data for the list.
*/
private DefaultListModel __annotationJListModel = new DefaultListModel();

/**
Indicate whether the component should be set invisible when the list is empty.
*/
private boolean __hideIfEmpty = false;

/**
The list of annotations maintained in the GeoView.
*/
private List<StateMod_Network_AnnotationData> __annotationDataList = null;

/**
The component that actually renders the annotations - need this if the popup menu changes the
list of displayed annotations (such as clearing the list).
*/
private StateMod_Network_JComponent __networkJComponent = null;

/**
Menu items.
*/
private String __RemoveAllAnnotationsString = "Remove All Annotations";

/**
Constructor.
@param annotationDataList list of annotation data, if available (can pass null and reset the list
later by calling setAnnotationData()).
@param hideIfEmpty if true, set the panel to not visible if the list is empty - this may be appropriate
if UI real estate is in short supply and annotations should only be shown if used
*/
public StateMod_Network_AnnotationDataListJPanel ( List<StateMod_Network_AnnotationData> annotationDataList,
	StateMod_Network_JComponent networkJComponent, boolean hideIfEmpty )
{	super();
	// Set up the layout manager
	this.setLayout(new GridBagLayout());
	this.setBorder(BorderFactory.createTitledBorder("Annotations"));
	int y = 0;
	Insets insetsTLBR = new Insets ( 0, 0, 0, 0 );
	__annotationJList = new JList();
	if ( annotationDataList != null ) {
		setAnnotationData ( annotationDataList );
	}
	JGUIUtil.addComponent ( this, new JScrollPane(__annotationJList),
		0, y, 1, 1, 1.0, 1.0,
		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );
	__hideIfEmpty = hideIfEmpty;
	__networkJComponent = networkJComponent;
	
	// Add popup for actions on annotations
	
	final JPopupMenu popupMenu = new JPopupMenu();
	JMenuItem removeAllAnnotationsJMenuItem = new JMenuItem(__RemoveAllAnnotationsString);
	removeAllAnnotationsJMenuItem.addActionListener(this);
	popupMenu.add(removeAllAnnotationsJMenuItem);
	__annotationJList.addMouseListener(new MouseAdapter() {
	     public void mouseClicked(MouseEvent me) {
	         // if right mouse button clicked (or me.isPopupTrigger())
	         if ( SwingUtilities.isRightMouseButton(me)
	             //&& !__annotationJList.isSelectionEmpty()
	             //&& __annotationJList.locationToIndex(me.getPoint())
	             //== __annotationJList.getSelectedIndex()
	             	) {
	                 popupMenu.show(__annotationJList, me.getX(), me.getY());
	             }
	         }
	     }
	);

	checkVisibility();
}

/**
Handle action events.
*/
public void actionPerformed ( ActionEvent event )
{
	String action = event.getActionCommand();
	if ( action.equals(__RemoveAllAnnotationsString) ) {
		// Remove from the list and the original data that was passed in
		__annotationJListModel.clear();
		if ( __annotationDataList != null ) {
			if ( __networkJComponent != null ) {
				// TODO SAM 2010-12-28 Enable
				__networkJComponent.clearAnnotations(); // This will modify __annotationDataList
			}
		}
		checkVisibility();
	}
}

/**
Add an annotation to the list.
*/
public void addAnnotation ( StateMod_Network_AnnotationData annotationData )
{
	// For now just add at the end...
	if ( annotationData != null ) {
		__annotationJListModel.addElement ( annotationData.getLabel() );
	}
	checkVisibility();
}

/**
Check the annotation list visibility.  If hideIfEmpty=true, then set to not visible if the list is empty.
*/
private void checkVisibility ()
{
	if ( __hideIfEmpty && __annotationJListModel.size() == 0 ) {
		setVisible(false);
	}
	else {
		setVisible(true);
	}
}

/**
Set the annotation data and repopulate the list.
*/
public void setAnnotationData ( List<StateMod_Network_AnnotationData> annotationDataList )
{
	__annotationDataList = annotationDataList;
	List<String> annotationLabelList = new Vector<String>(annotationDataList.size());
	for ( StateMod_Network_AnnotationData annotationData : annotationDataList ) {
		annotationLabelList.add(annotationData.getLabel());
	}
	// Sort the array before adding
	Collections.sort(annotationLabelList);
	__annotationJListModel = new DefaultListModel();
	for ( String annotationLabel: annotationLabelList ) {
		__annotationJListModel.addElement(annotationLabel);
	}
	__annotationJList.setModel(__annotationJListModel);
	checkVisibility();
}

/**
Set the GeoView that is rendering the map.
*/
public void setNetworkJComponent ( StateMod_Network_JComponent networkJComponent )
{
	__networkJComponent = networkJComponent;
}

}
