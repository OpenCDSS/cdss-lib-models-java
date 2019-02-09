// StateMod_Network_UndoData - simple class to hold undo data for StateMod_Network editing

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
