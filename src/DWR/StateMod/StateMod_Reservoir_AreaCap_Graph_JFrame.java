//------------------------------------------------------------------------------
// StateMod_Reservoir_AreaCap_Graph_JFrame - frame to graph a reservoir's 
//	content/area/seepage curve
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2006-02-28	Steven A. Malers, RTi	Create class to use the JFreeChart
//					package.  Initial prototyping was
//					created by JTS.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.util.Vector;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.axis.NumberAxis;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import org.jfree.chart.title.TextTitle;

import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.Message.Message;

public class StateMod_Reservoir_AreaCap_Graph_JFrame extends JFrame
{

private StateMod_Reservoir __res = null;
private String __type = "Area";

/**
Display a graph of reservoir content versus area or content versus seepage.
@param dataset The dataset including the reservoir.
@param res StateMod_Reservoir with data to graph.
@param type "Area" or "Seepage", indicating the data to graph.
@param editable Indicate whether the data are editable or not (currently
ignored and treated as not editable through the graph).
*/
public StateMod_Reservoir_AreaCap_Graph_JFrame (
	StateMod_DataSet dataset,
	StateMod_Reservoir res, String type, boolean editable )
{	StateMod_GUIUtil.setTitle(this, dataset, res.getName() 
		+ " - Reservoir Content/" + type + " Curve", null);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());	

	__res = res;
	__type = type;

	DefaultTableXYDataset graph_dataset = createDataset();
	JFreeChart chart = createChart(graph_dataset);
	ChartPanel chartPanel = new ChartPanel(chart);
	chartPanel.setPreferredSize(new Dimension(500, 500));
	getContentPane().add("Center", chartPanel);
	pack();
	JGUIUtil.center ( this );
	setVisible ( true );
}

/**
Create a dataset to display in the chart.  Extract the values from the
StateMod_Reservoir curve data.
*/
private DefaultTableXYDataset createDataset()
{	String routine ="StateMod_Reservoir_AreaCap_Graph_JFrame.createDataSet";
	DefaultTableXYDataset dataset = new DefaultTableXYDataset();

	Vector v = __res.getAreaCaps();
	int size = 0;
	if ( v != null ) {
		size = v.size();
	}
	XYSeries series = new XYSeries("Reservoir " + __res.getID() +
		" (" + __res.getName() + ") Content/" + __type + " Curve",
		false, false);
	StateMod_ReservoirAreaCap ac = null;
	// Speed up checks in loop...
	boolean do_area = true;
	if ( __type.equalsIgnoreCase("Seepage") ) {
		do_area = false;
	}
	double value_prev = -10000000.0, value = 0.0, value2 = 0.0;
	double content = 0.0;
	int match_count = 0;
	for ( int i = 0; i < size; i++ ) {
		ac = (StateMod_ReservoirAreaCap)v.elementAt(i);
		// Curves will often have a very large content to protect
		// against out of bounds for interpolation.  However, if this
		// point is graphed, it causes the other values to appear
		// miniscule.  Therefore, omit the last point if it is much
		// larger than the previous value.
		if (	(size > 4) && (i == (size - 1)) &&
			ac.getConten() > 9000000.0 ) {
			Message.printStatus ( 2, routine,
			"Skipping last point.  Seems to be very large bounding"+
			" value and might skew the graph." );
			continue;
		}
		// Add X first, then Y...
		if ( do_area ) {
			value = ac.getSurarea();
		}
		else {	value = ac.getSeepage();
		}
		if ( value == value_prev ) {
			// This is needed because JFreeChart will not allow
			// adjacent X values to be the same.
			++match_count;
			value2 = value + match_count*.00001;
		}
		else {	value2 = value;
		}
		content = ac.getConten();
		// REVISIT SAM 2006-08-20
		// Not sure if content needs to be checked the same way for
		// duplicates.
		Message.printStatus (
			2, routine, "X="+value2+" y="+content);
		series.add(value2,content);
		value_prev = value;
	}
	dataset.addSeries(series);

	return dataset;
}

/**
Create the chart using the data.
*/
private JFreeChart createChart(DefaultTableXYDataset dataset)
{
	String xlabel = "Surface Area (ACRE)";
	if ( __type.equals("Seepage") ) {
		xlabel = "Seepage (AF/M)";
	}
	JFreeChart chart = ChartFactory.createXYLineChart(
		"Reservoir Content/" + __type + " Curve",
		xlabel,
		"Content (ACFT)",
		dataset,
		PlotOrientation.VERTICAL,
		false,
		true,
		false
	);

	chart.addSubtitle(new TextTitle(
		__res.getID() + " (" + __res.getName() + ")" ));
	//TextTitle source = new TextTitle("Source: rgtTW.res");
	//source.setFont(new Font("SansSerif", Font.PLAIN, 10));
	//source.setPosition(RectangleEdge.BOTTOM);
	//source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
	//chart.addSubtitle(source);

	chart.setBackgroundPaint(Color.WHITE);

	XYPlot plot = (XYPlot)chart.getPlot();
	plot.setBackgroundPaint(Color.white);
	plot.setRangeGridlinePaint(Color.lightGray);

	NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

	return chart;
}

/**
Create the panel to hold the chart.
*/
public JPanel createDemoPanel() {
	JFreeChart chart = createChart(createDataset());
	return new ChartPanel(chart);
}

}
