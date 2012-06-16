package plugins.viewer;

import image.roi.PointList;
import image.roi.ROIPlus;

import java.awt.Graphics2D;
import java.awt.Point;

public class PointDelegate extends PGonDelegate {

	public PointDelegate(PointList pl)
	{
		super(pl);
		this.type = ROIPlus.ROI_POINT;
	}

	@Override
	public void paintLinesForPointList(Graphics2D g2, PointList transformedList, int mode, Point cursorLocation)
	{
		if(mode == MODE_SMASHED)
		{
			int n = transformedList.size();
			int count = 0;
			java.awt.Rectangle temp = new java.awt.Rectangle();
			for(Point p : transformedList)
			{
				g2.setColor(RoiDelegate.getColorForIntInRange(count, n));
				temp.x = p.x - 2;
				temp.y = p.y - 2;
				temp.width = 4;
				temp.height = 4;
				g2.fill(temp);
				count = count + 1;
			}
		}
		if(mode == MODE_VIEW)
		{
			java.awt.Rectangle temp = new java.awt.Rectangle();
			for(Point p : transformedList)
			{
				temp.x = p.x - 2;
				temp.y = p.y - 2;
				temp.width = 4;
				temp.height = 4;
				g2.fill(temp);
			}
		}
	}
}
