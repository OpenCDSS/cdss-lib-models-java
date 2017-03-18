//-----------------------------------------------------------------------------
// StateCU_DataSet_JFrame - an object to display a StateCU data set.
//-----------------------------------------------------------------------------
// History:
//
// 2003-06-08	Steven A. Malers, RTi	Created class.
// 2003-07-07	SAM, RTi		Support component groups.
// 2003-07-13	SAM, RTi		Update to handle
//					RTi.Util.IO.DataSetComponent being used
//					for data set components now.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package DWR.StateCU;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.DataSetComponent;

/**
This StateCU_DataSet_JFrame class displays a StateCU_DataSet and its components.
Only one instance of this interface should be created.  The setVisible() method
can be called to hide/show the interface.
*/
@SuppressWarnings("serial")
public class StateCU_DataSet_JFrame extends JFrame
//implements ChangeListener
{

private StateCU_DataSet __dataset = null;

/**
Construct a StateCU_DataSet_JFrame and optionally set visible.
@param parent JFrame from which this instance is constructed.
@param dataset StateCU_DataSet that is being displayed/managed.
@param title Title to be displayed.
@param is_visible Indicates whether the display should be made visible at
creation.
*/
public StateCU_DataSet_JFrame ( JFrame parent, StateCU_DataSet dataset,
				String title, boolean is_visible )
{	__dataset = dataset;
	initialize ( title, is_visible );
}

/**
Initialize the interface.
@param title Title to be displayed.
@param is_visible Indicates whether the display should be made visible at
creation.
*/
private void initialize ( String title, boolean is_visible )
{
	GridBagLayout gbl = new GridBagLayout();
	Insets insetsTLBR = new Insets ( 2, 2, 2, 2 );	// space around text
							// area

	// Add a panel to hold the main components...

	JPanel display_JPanel = new JPanel ();
	display_JPanel.setLayout ( gbl );
	getContentPane().add ( display_JPanel );

	JTabbedPane dataset_JTabbedPane = new JTabbedPane ();
	//dataset_JTabbedPane.addChangeListener ( this );
	JGUIUtil.addComponent ( display_JPanel, dataset_JTabbedPane,
			0, 0, 10, 1, 0, 0,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	int y = 0;	// Used for vertical positioning.

	//
	// Data Set Components...
	//
	// 1 row per component, each column width of 1
	//

	JPanel components_JPanel = new JPanel();
	components_JPanel.setLayout ( gbl );
	dataset_JTabbedPane.addTab ( "Components", null,
		components_JPanel, "Components" );
	// Add the headers...
	int x = 0;
	JGUIUtil.addComponent ( components_JPanel,
			new JLabel ("Component" ),
			x, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( components_JPanel,
			new JLabel ("Created How" ),
			++x, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( components_JPanel,
			new JLabel ("Input File" ),
			++x, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( components_JPanel,
			new JLabel ("Count" ),
			++x, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( components_JPanel,
			new JLabel ("Incomplete" ),
			++x, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( components_JPanel,
			new JLabel ("Data File" ),
			++x, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Now add the contents for each component...
	List<DataSetComponent> components_Vector = __dataset.getComponents();
	int size = components_Vector.size();
	DataSetComponent component;
	y = 0;	// Incremented below.  True row 0 is used for headers above.
	List<DataSetComponent> data = null;
	int data_size = 0;
	for ( int i = 0; i < size; i++ ) {
		x = 0;
		component = components_Vector.get(i);
		JTextField component_JTextField = new JTextField(
			component.getComponentName(), 20 );
		component_JTextField.setEditable ( false );
		JGUIUtil.addComponent ( components_JPanel,
			component_JTextField,
			x, ++y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

		if ( component.isGroup() ) {
			// Get each of the sub-component's information...
			if ( component.getData() == null ) {
				continue;
			}
			@SuppressWarnings("unchecked")
			List<DataSetComponent> data0 = (List<DataSetComponent>)component.getData();
			data = data0;
			data_size = 0;
			if ( data != null ) {
				data_size = data.size();
			}
			for ( int j = 0; j < data_size; j++ ) {
				x = 0;
				component = (DataSetComponent)data.get(j);
				component_JTextField =
					new JTextField( "    " +
					component.getComponentName(), 20 );
				component_JTextField.setEditable ( false );
				JGUIUtil.addComponent ( components_JPanel,
					component_JTextField,
					x, ++y, 1, 1, 0.0, 0.0,
					insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

				// REVISIT - need to track create method
				JTextField from_JTextField = new JTextField(
					"?", 10 );
					//from.getName(), 10 );
				from_JTextField.setEditable ( false );
				JGUIUtil.addComponent ( components_JPanel,
					from_JTextField,
					++x, y, 1, 1, 0.0, 0.0,
					insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

				// REVISIT - need to indicate list or commands
				// file
				JTextField inputfile_JTextField =
					new JTextField( "?", 10 );
					//from.getName(), 10 );
				inputfile_JTextField.setEditable ( false );
				JGUIUtil.addComponent ( components_JPanel,
					inputfile_JTextField,
					++x, y, 1, 1, 0.0, 0.0,
					insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

				int count = 0;
				try {
					count = ((List)component.getData()).size();
				}
				catch ( Exception e ) {
					// REVISIT
					// Probably because not a Vector (GIS
					// and control) - need a more graceful
					// way to handle.
					count = -1;
				}
				JTextField object_JTextField = null;
				if ( count >= 0 ) {
					object_JTextField =
					new JTextField( "" + count, 5 );
				}
				else {	object_JTextField =
					new JTextField( "-", 5 );
				}
				object_JTextField.setEditable ( false );
				JGUIUtil.addComponent ( components_JPanel,
					object_JTextField,
					++x, y, 1, 1, 0.0, 0.0,
					insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

				// REVISIT - need to indicate how many are
				// incomplete

				JTextField incomplete_JTextField =
					new JTextField( "?", 5 );
					//from.getName(), 10 );
				incomplete_JTextField.setEditable ( false );
				JGUIUtil.addComponent ( components_JPanel,
					incomplete_JTextField,
					++x, y, 1, 1, 0.0, 0.0,
					insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

				JTextField datafile_JTextField = new JTextField(
					component.getDataFileName(), 15 );
				datafile_JTextField.setEditable ( false );
				JGUIUtil.addComponent ( components_JPanel,
					datafile_JTextField,
					++x, y, 1, 1, 0.0, 0.0,
					insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
			}
		}
	}

	//
	// Data Set Properties...
	//
	// 1 grid for labels, 6 for text fields (resizable).
	//

	y = 0;
	JPanel properties_JPanel = new JPanel();
	properties_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent ( properties_JPanel,
			new JLabel ("Data Set Type:" ),
			0, y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField dataset_type_JTextField = new JTextField (
			__dataset.getDataSetName(), 20 );
	dataset_type_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( properties_JPanel,
			dataset_type_JTextField,
			1, y, 2, 1, 0.0, 1.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( properties_JPanel,
			new JLabel ("Data Set Base Name:" ),
			0, ++y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField dataset_basename_JTextField = new JTextField (
			__dataset.getBaseName(), 20 );
	dataset_basename_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( properties_JPanel,
			dataset_basename_JTextField,
			1, y, 2, 1, 0.0, 1.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( properties_JPanel,
			new JLabel ("Data Set Directory:" ),
			0, ++y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField dataset_dir_JTextField = new JTextField (
			__dataset.getDataSetDirectory(), 40 );
	dataset_dir_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( properties_JPanel,
			dataset_dir_JTextField,
			1, y, 6, 1, 0.0, 1.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( properties_JPanel,
			new JLabel ("Data Set File:" ),
			0, ++y, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField dataset_file_JTextField = new JTextField (
			__dataset.getDataSetFileName(), 20 );
	dataset_file_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( properties_JPanel,
			dataset_file_JTextField,
			1, y, 2, 1, 0.0, 1.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	dataset_JTabbedPane.addTab ( "Properties", null,
		properties_JPanel, "Properties" );

	// Show the interface...

	if ( (title == null) || (title.length() == 0) ) {
		setTitle ( "Data Set Manager" );
	}
	else {	setTitle ( title );
	}
	pack();
	JGUIUtil.center(this);
	setResizable(true);
	setVisible ( is_visible );
}

} // End StateCU_DataSet_JFrame
