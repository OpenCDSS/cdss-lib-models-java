package DWR.StateMod;

/**
A simple class to hold undo data for StateMod_Network editing, basically to store the previous positions of
nodes in case they need to be undone.
*/
public class StateMod_Network_UndoData
{
	public int nodeNum;
	public double oldX;
	public double oldY;
	public double newX;
	public double newY;

	public int[] otherNodes = null;
	public double[] oldXs = null;
	public double[] oldYs = null;
	public double[] newXs = null;
	public double[] newYs = null;
}