package function.tracker;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import image.roi.PointList;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import jex.statics.JEXStatics;

public class FindMaxima  {	
	// clas variables
	private float[][] imageMatrix;
	private Rectangle roi;
	private PointList pList ;
	private ImagePlus image;
	public int threshold = 0;
	public int cellNB = 0;
	public boolean fixedCellNb = false;
	public int cellRadius = 10;
	public int mincellRadius = 0;
	
	public FindMaxima(){}

	public PointList findMaximum(ImagePlus image, Rectangle roi){
		// pass the variables
		this.image       = image;
		this.roi         = roi;
//		this.fixedCellNb = false;
		this.pList       = new PointList() ;
		
		analyze();
		
		return pList;
	}
	
	// ------------------------------------
	// ------ CALCULATION UTILITIES -------
	// ------------------------------------
	
	public void analyze(){
		ImageProcessor ip = this.image.getProcessor();
		imageMatrix = ip.getFloatArray();
		float[][] matrix = ip.duplicate().getFloatArray();
		float[][] matrix2 = ip.duplicate().getFloatArray();
//		JEXStatics.logManager.log("Analyzing image of width "+matrix.length+" by "+matrix[0].length,1,this);
		
		if (fixedCellNb){
			pList = findMaxima(matrix, matrix2, cellNB);
		}
		else {
			pList = findMaxima(matrix, matrix2, (float)threshold);
		}
	}

	// find n maxima
	private PointList findMaxima(float[][] fimage, float[][] fimage2, int n){
		PointList result = new PointList();

		// for n find a new maximum and save
		for (int k=0; k<n; k++){
			Point currentPoint = findAdditionalMaxima(fimage, result);
			JEXStatics.logManager.log("Found new point "+currentPoint.x+"."+currentPoint.y,1,this);
			result.add(currentPoint);
		}
		return result;
	}

	private PointList findMaxima(float[][] fimage, float[][] fimage2, float thresh){
		PointList result = new PointList();

		float currentMax = thresh;
		while ((currentMax >= thresh)&&(currentMax > 0)){
			Point newPoint = findAdditionalMaxima(fimage, result);
			currentMax = getValueOfPoint(newPoint,imageMatrix);
			JEXStatics.logManager.log("currentMax "+currentMax+" currentTresh "+thresh,2,this);
			if (currentMax >= thresh){
				result.add(newPoint);
			}
		}
		PointList results = despecle(fimage2, mincellRadius, result, threshold);
		return results;
	}
	
	private PointList despecle(float[][] fimage, int mincellRadius, PointList result, float thresh){
		PointList results = new PointList();
		int n = result.size();
		
		for (int k=0; k<n; k++){
			Point p = result.get(k);
			int minX = Math.max(p.x - mincellRadius, 0);
			int maxX = Math.min(p.x + mincellRadius, fimage.length-1);
			int minY = Math.max(p.y - mincellRadius, 0);
			int maxY = Math.min(p.y + mincellRadius, fimage[0].length-1);
//			int dumb = (int)(fimage[minX][minY])+(int)(fimage[minX][maxY])+(int)(fimage[maxX][minY])+(int)(fimage[maxX][maxY]);
//			int one = (int)(fimage[minX][minY]);
//			int two = (int)(fimage[minX][maxY]);
			if( ((int)(fimage[minX][minY])+(int)(fimage[minX][maxY])+(int)(fimage[maxX][minY])+(int)(fimage[maxX][maxY])) < thresh)
				fimage[p.x][p.y] = 0;
			if (fimage[p.x][p.y] != 0) results.add(p);
		}
//		int dummy = results.size();
		return results;
	}

	// find an additional maxima
	private Point findAdditionalMaxima(float[][] fimage, PointList currentPoints){
		Point result = new Point(0,0);
		float currentMax = 0;

		// find the n maximum features of the image
		int Xstart = 0 ;
		int Ystart = 0 ;
		int w = fimage.length;
		int h = fimage[0].length;
		
		if (roi != null){
			Xstart = (int) roi.getX();
			Ystart = (int) roi.getY();
			w = Xstart + (int) roi.getWidth();
			h = Ystart + (int) roi.getHeight();
		}
		
//		System.out.println("   FindMaxima ---> searching in region "+Xstart+", "+Ystart+", "+w+", "+h);

		int index = 0;
		for (int i=Ystart; i<h; i++){
			for (int j=Xstart; j<w; j++){
				int x = j;
				int y = i;
				
				if ((fimage[j][i] > currentMax)&&(isInROI(x,y))){
//					JEXStatics.logManager.log(""+index+" - Current:"+currentMax+" Point: ("+x+"."+y+") New value:"+fimage[j][i],1,this);
					index++;
					
					currentMax = fimage[j][i];
					result = new Point(x,y);
//					JEXStatics.logManager.log("### Current point pushed the maximum!: "+fimage[j][i],1,this);
				}
			}
		}
		if (result != null) {
			JEXStatics.logManager.log("filling array at point "+result.x+"."+result.y,2,this);
			fillArrayWithZeros(fimage,result);
//			if (fixedCellNb){
//				fillArrayAroundPoint(fimage,result,cellNB);
//			}
			if (!fixedCellNb) {
				fillArrayAroundPoint(fimage,result,threshold);
			}
			
		}
		
		return result;
	}
	
	private void fillArrayWithZeros(float[][] fimage, Point p){
		int minX = Math.max(p.x - cellRadius/2, 0);
		int maxX = Math.min(p.x + cellRadius/2, fimage.length);
		int minY = Math.max(p.y - cellRadius/2, 0);
		int maxY = Math.min(p.y + cellRadius/2, fimage[0].length);
		for (int i=minX; i<maxX; i++){
			for (int j=minY; j<maxY; j++){
				Point currentPoint = new Point(i,j);
				if (distance(p,currentPoint) <= (cellRadius)) fimage[i][j] = 0;
			}
		}
	}
	
	/**
	 * Fill the array around the point p, above threshold thresh
	 * @param fimage
	 * @param p
	 * @param thresh
	 */
	private void fillArrayAroundPoint(float[][] fimage, Point p, double thresh){
		Point currentPoint = new Point(p.x,p.y);
		int w = fimage.length;
		int h = fimage[0].length;
		List<Point> pointsToDo = new ArrayList<Point>(0);
		List<Point> pointsDone = new ArrayList<Point>(0);
		pointsToDo.add(currentPoint);
		
		while (pointsToDo.size() > 0){
			// set next point to do
			Point doing = pointsToDo.remove(0);
			pointsDone.add(doing);
			fimage[doing.x][doing.y] = 0 ;
			float pixelValue;
			
			// add the neighbours of point doing to the list of points to do if they fit some requirements
			int topx = doing.x ;
			int topy = doing.y -1 ;
			if (topy >= 0){
				pixelValue = fimage[topx][topy];
				if ( topy > 0 && pixelValue > thresh) 
					pointsToDo.add(new Point(topx,topy));
			}
			
			int leftx = doing.x - 1 ;
			int lefty = doing.y ;
			if (leftx >= 0){
				pixelValue = fimage[leftx][lefty];
				if ( leftx > 0 && pixelValue > thresh) 
					pointsToDo.add(new Point(leftx,lefty));
			}

			int rightx = doing.x + 1 ;
			int righty = doing.y ;
			if (rightx < w){
				pixelValue = fimage[rightx][righty];
				if ( rightx < w && pixelValue > thresh) 
					pointsToDo.add(new Point(rightx,righty));
			}
			
			int bottomx = doing.x ;
			int bottomy = doing.y + 1 ;
			if (bottomy < h){
				pixelValue = fimage[bottomx][bottomy];
				if ( bottomy  < h && pixelValue > thresh) 
					pointsToDo.add(new Point(bottomx,bottomy));
			}
		}
		
	}
	
	// get the values of the point extracted
	public float getValueOfPoint(Point p, float[][] fimage){
//		int x = Math.max(0, p.x - (int)(CellRadius));
//		int y = Math.max(0, p.y - (int)(CellRadius));
		int x = Math.max(0, p.x);
		int y = Math.max(0, p.y);
		float result = fimage[x][y];
		return result;
	}

	// distance of a point to other saved points
	@SuppressWarnings("unused")
	private int distanceToOtherMaxima(int x, int y, List<Point> otherMax){
		int distanceMax = image.getWidth();
		for (int k=0, len=otherMax.size(); k<len; k++){
			Point currentPoint = otherMax.get(k);
			double currentDistance = Math.sqrt(((currentPoint.x - x)*(currentPoint.x - x) + (currentPoint.y - y)*(currentPoint.y - y)));
			distanceMax = ((int)currentDistance < distanceMax) ? (int)currentDistance : distanceMax;
		}
		return distanceMax;
	}
	
	public double distance(Point p1, Point p2){
		double currentDistance = Math.sqrt(((p1.x - p2.x)*(p1.x - p2.x) + (p2.y - p2.y)*(p2.y - p2.y)));
		return currentDistance;
	}

	// find if a point is in the roi
	private boolean isInROI(int x, int y){
		if (roi == null){
			return true;
		}
		if ((roi.getWidth() == 0) || (roi.getHeight() == 0)) {return true;}
		if ( (x > roi.getX()) && (x < (roi.getX()+roi.getWidth())) && (y > roi.getY()) && (y < (roi.getY()+roi.getHeight())) ){return true;}
		return false;
	}

	// Find the saved point close to the given point, null if none
	@SuppressWarnings("unused")
	private int isCloseToPoint(Point p, List<Point> savedPoints){
		for (int k=0, len=savedPoints.size(); k<len; k++){
			Point currentPoint = savedPoints.get(k);
			// distance to currentpoint
			double currentDistance = Math.sqrt(((currentPoint.x - p.x)*(currentPoint.x - p.x) + (currentPoint.y - p.y)*(currentPoint.y - p.y)));
			// if the distance is small enough select this point and return
			if (currentDistance < (cellRadius)) {return k;}
		}
		return -1;			
	}

	/**
	* Return the first index of vector v that matches with object o, -1 if none match
	*/
	public static boolean isMember(Point p, List<Point> v){
		for (int k=0, len = v.size(); (k<len); k++) {
			if (p.equals(v.get(k))) return true;
		}
		return false;
	}
		
	public PointList getPointList(){
		PointList result = pList.clone();
		return result;
	}

}