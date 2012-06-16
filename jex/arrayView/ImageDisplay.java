package jex.arrayView;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import image.roi.PointList;
import image.roi.ROIPlus;
import image.roi.Trajectory;
import image.roi.Vect;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;

public class ImageDisplay extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// statics
	public static double zoomFactor = 1.5;
	
	// Interacting classes
	private ImageDisplayController highLevelController;
	
	// Options of class
	private boolean canZoomAndPan = true;
	
	// Info to display with image
	private String title  = "" ;
	
	// display variables
	private Color background  = DisplayStatics.background;
	private JLabel titleLabel = new JLabel();
	private JPanel displayPanel;

	public ImageDisplay(ImageDisplayController controller, String title){
		this.highLevelController = controller;
		this.title      = title;
		
		this.initialize();
	}
	
	/**
	 * Initialize panel
	 */
	private void initialize(){
		this.setBackground(background);
		this.setLayout(new BorderLayout());
		
		displayPanel = new JPanel();
		displayPanel.setBackground(background);
		displayPanel.setLayout(new BorderLayout());
		displayPanel.addMouseListener(this);
		displayPanel.addMouseMotionListener(this);
		displayPanel.addMouseWheelListener(this);
		
		titleLabel.setText(title);
		titleLabel.setPreferredSize(new Dimension(200,20));
		titleLabel.setMaximumSize(new Dimension(200,20));
		titleLabel.setForeground(Color.WHITE);

		this.add(titleLabel,BorderLayout.PAGE_START);
		this.add(displayPanel,BorderLayout.CENTER);
	}
	
	/**
	 * Allows the image panel to zoom and pan
	 * @param canZoomAndPan
	 */
	public void setCanZoomAndPan(boolean canZoomAndPan){
		this.canZoomAndPan = canZoomAndPan;
	}

	/**
	 * Set a high level roi controller, responds to point and rectangle creations
	 * @param highLevelController
	 */
	public void setHighLevelController(ImageDisplayController highLevelController){
		this.highLevelController = highLevelController;
	}
	
	// -------------------------------------------------
	// -------------- Setters and actions --------------
	// -------------------------------------------------
	// Source image variables
	private ImageProcessor sourceIJ        = null ; // for looking up individual pixels
	private Image	       adjustedSource  = null ;
	private int            minIntensity    = -1, maxIntensity = -1;
	private Rectangle      imageRect       = new Rectangle(0,0,0,0);
	private Rectangle      srcRect         = new Rectangle(0,0,0,0);
	private Point          srcCenter       = new Point(-1,-1);
	private Rectangle      dstRect         = new Rectangle(0,0,0,0);
	private double         zoom            = 1    ; // current zoom scale
	
	@Override
	public void setBackground(Color background)
	{
		this.background = background;
		this.repaint();
	}
	
	/**
	 * Set the displayed image
	 * @param image
	 */
	public void setImage(ImagePlus image){
		if(image != null) this.setImage(image.getProcessor());
	}
	
	/**
	 * Set the displayed image
	 * @param image
	 */
	public void setImage(ImageProcessor image){
		if(image == null) return;
		
		JEXStatics.logManager.log("Setting image in panel", 2, this);
		this.sourceIJ = image;
		if(this.maxIntensity == -1 || this.minIntensity == -1)
		{
			this.minIntensity = (int)this.sourceIJ.getMin();
			this.maxIntensity = (int)this.sourceIJ.getMax();
		}
		this.setLimits(this.minIntensity, this.maxIntensity);
		if(this.sourceIJ != null && imageRect.width == this.sourceIJ.getWidth() && imageRect.height == this.sourceIJ.getHeight()) {}
		else
		{
			if (sourceIJ == null){
				imageRect.width		= 0;
				imageRect.height	= 0;
				srcCenter.x			= imageRect.width / 2;
				srcCenter.y			= imageRect.height / 2;
				zoom				= 1;
			}
			else {
				imageRect.width		= sourceIJ.getWidth();
				imageRect.height	= sourceIJ.getHeight();
				srcCenter.x     	= imageRect.width / 2;
				srcCenter.y			= imageRect.height / 2;
				zoom				= 1;
			}
		}
		extractImage();
		this.repaint();
	}

	/**
	 * Set the displayed image
	 * @param image
	 */
	public void setImage(ImagePlus image, boolean noRescaling){
		if (!noRescaling) setImage(image);
		else {
			JEXStatics.logManager.log("Setting image in panel", 1, this);
			this.sourceIJ = image.getProcessor();
			if(this.maxIntensity == -1 || this.minIntensity == -1)
			{
				this.minIntensity = (int)this.sourceIJ.getMin();
				this.maxIntensity = (int)this.sourceIJ.getMax();
			}
			this.setLimits(this.minIntensity, this.maxIntensity);
			if (sourceIJ == null){
				imageRect.width		= 0;
				imageRect.height	= 0;
				srcCenter.x			= imageRect.width / 2;
				srcCenter.y			= imageRect.height / 2;
				zoom				= 1;
			}
			else {
				imageRect.width      = sourceIJ.getWidth();
				imageRect.height     = sourceIJ.getHeight();
			}
			extractImage();
			this.repaint();
		}
	}
	
	public void setLimits(int min, int max)
	{
		this.minIntensity = min;
		this.maxIntensity = max;
		this.sourceIJ.setMinAndMax(min, max);
		this.adjustedSource = this.sourceIJ.createImage();
		this.repaint();
	}
	
	public int minIntensity()
	{
		return this.minIntensity;
	}
	
	public int maxIntensity()
	{
		return this.maxIntensity;
	}

	/**
	 * Set the displayed image
	 * @param image
	 */
	public void setTitle(String title){
		JEXStatics.logManager.log("Setting title of image to "+title, 1, this);
		this.title = title;
		extractImage();
		this.repaint();
	}
	
	/**
	 * Zoom image around point (srcCenter.x,srcCenter.y)
	 * @param units number of increments
	 * @param centerP center point around which zoom is done
	 */
	public void zoom(int units, Point centerP){
		if (units >= 0) zoom = zoom * units * ImageDisplay.zoomFactor ;
		else zoom = Math.abs( zoom / ( units * ImageDisplay.zoomFactor ) );
		
		if (zoom < 1) zoom = 1;
		if (zoom > 401) zoom = 400;
		
		this.srcCenter.x     = (int) centerP.getX();
		this.srcCenter.y     = (int) centerP.getY();
		JEXStatics.logManager.log("Zooming image, new zoom factor is "+zoom, 2, this);
		extractImage();
		this.repaint();
	}
	
	/**
	 * Translate image by a vector (dx,dy)
	 * @param dx x displacement
	 * @param dy y displacement
	 */
	public void translate(Vect v){
		this.srcCenter.x     = (int) Math.round(srcCenter.x + v.dX);
		this.srcCenter.y     = (int) Math.round(srcCenter.y + v.dY);
		JEXStatics.logManager.log("Translating image by "+v.dX+","+v.dY, 2, this);
		extractImage();
		this.repaint();
	}
	
	/**
	 * Translate image by a vector (dx,dy)
	 * @param dx x displacement
	 * @param dy y displacement
	 */
	public void dragToPoint(Point mouseDragLocation){
		Vect dp = new Vect(firstPointMouseDisplay.x-mouseDragLocation.x, firstPointMouseDisplay.y-mouseDragLocation.y);
		dp = this.displayToImage(dp);
		Point newLocation = new Point((int)(firstPointSrcCenter.x + dp.dX), (int)(firstPointSrcCenter.y + dp.dY));
		this.srcCenter.x     = newLocation.x;
		this.srcCenter.y     = newLocation.y;
		JEXStatics.logManager.log("Dragging image to "+newLocation.x+","+newLocation.y, 2, this);
		extractImage();
		this.repaint();
	}
	
	// -------------------------------------------------
	// -------------- ROI action -----------------------
	// -------------------------------------------------
	private ROIPlus          activeROI ;
	private HashSet<ROIPlus> rois ;
	private HashMap<ROIPlus,Color> colors = new HashMap<ROIPlus,Color>() ;
	private Trajectory       trajectory;
	private Point            clickedPoint;
	private int              radius;
	
	/**
	 * Set the active roi... default color is yellow
	 * @param roi
	 */
	public void setActiveRoi(ROIPlus roi){
		this.activeROI = roi;
//		extractImage();
		this.repaint();
	}
	
	/**
	 * Add a roi to the list of non active rois... default color is cyan
	 * @param roi
	 */
	public void addRoi(ROIPlus roi){
		if (rois == null) rois = new HashSet<ROIPlus>();
		if (roi == null) return;
		rois.add(roi);
//		extractImage();
		this.repaint();
	}

	/**
	 * Add a roi to the list of non active rois... default color is cyan
	 * @param roi
	 */
	public void addRoi(ROIPlus roi, Color color){
		if (rois == null) rois = new HashSet<ROIPlus>();
		if (roi == null) return;
		rois.add(roi);
		colors.put(roi, color);
//		extractImage();
		this.repaint();
	}
	
	/**
	 * Remove a roi from the list of non active rois
	 * 
	 * @param roi
	 */
	public void removeRoi(ROIPlus roi){
		if (rois == null) rois = new HashSet<ROIPlus>();
		rois.remove(roi);
//		extractImage();
		this.repaint();
	}
	
	/**
	 * Set the whole list of rois... default color is cyan
	 * @param rois
	 */
	public void setRois(HashSet<ROIPlus> rois){
		this.rois = rois;
//		extractImage();
		this.repaint();
	}
	
	/**
	 * Returns the current active roi
	 * @return ROIPlus
	 */
	public ROIPlus getActiveRoi(){
		return activeROI;
	}
	
	public void displayTrajectory(Trajectory trajectory){
		this.trajectory = trajectory;
	}
	
	public void setRadius(int radius){
		this.radius = radius;
	}
	
	public void setClickedPoint(Point p){
		this.clickedPoint = p;
	}
	
	// -----------------------------------------------------------
	// -------------- Image preparation and display --------------
	// -----------------------------------------------------------
	// Variables for extracted image display
	private double scale = 1 ; // scale of displayed image
//	private int dstRect.x     = 0 ; // x location of image in frame
//	private int dstRect.y     = 0 ; // y location of image in frame
	private int wpane    = 0 ; // width of image canvas
	private int hpane    = 0 ; // height of image canvas
	private boolean scanLeft  = false;
	private boolean scanRight = false;
	private boolean scanUp    = false;
	private boolean scanDown  = false;
	
	/**
	 * Extract image to display from source image
	 */
	private void extractImage(){
		
		if (adjustedSource == null)
		{
			srcRect = new Rectangle(0,0,0,0);
			return;
		}
		
		srcRect.width  = (int)( imageRect.width / zoom);
		if (srcRect.width >= imageRect.width) {
			srcCenter.x     = srcRect.width/2;
			srcRect.width = imageRect.width;
		}
		
		srcRect.height = (int)( imageRect.height / zoom);
		if (srcRect.height >= imageRect.height){
			srcCenter.y      = srcRect.height/2;
			srcRect.height = imageRect.height;
		}
		
		// change here
		
		srcRect.x = srcCenter.x - srcRect.width/2;
		if (srcRect.x+srcRect.width > imageRect.width) {
			srcRect.x    = imageRect.width-srcRect.width-1;
			srcCenter.x = imageRect.width - srcRect.width/2;
		}
		if (srcRect.x < 0) {
			srcRect.x = 0;
			srcCenter.x = srcRect.width/2;
		}
		
		srcRect.y = srcCenter.y - srcRect.height/2;
		if (srcRect.y+srcRect.height > imageRect.height) {
			srcRect.y = imageRect.height-srcRect.height-1;
			srcCenter.y = imageRect.height - srcRect.height/2;
		}
		if (srcRect.y < 0) {
			srcRect.y = 0;
			srcCenter.y = srcRect.height/2;
		}
		
		if (srcRect.x > 1) scanLeft = true;
		else scanLeft = false;
		if (srcRect.y > 1) scanUp = true;
		else scanUp = false;
		if (srcRect.x+srcRect.width < imageRect.width-1) scanRight = true;
		else scanRight = false;
		if (srcRect.y+srcRect.height < imageRect.height-1) scanDown = true;
		else scanDown = false;
		
//		JEXStatics.logManager.log("Extracting image srcRect.x = "+srcRect.x+", srcRect.y = "+srcRect.y, 1, this);
//		JEXStatics.logManager.log("Extracting image srcRect.width = "+srcRect.width+", srcRect.height = "+srcRect.height, 1, this);
		
		if (srcRect.width==0 || srcRect.height==0) 
			srcRect = new Rectangle(0,0,0,0);
		else srcRect = new Rectangle(srcRect.x, srcRect.y, srcRect.width, srcRect.height);
		
	}
	
	/**
	 * Paint this componement with cool colors
	 */
	@Override
	public void paint(Graphics g) {
//		JEXStatics.logManager.log("Painting", 2, this);
		
		// get graphics
		Graphics2D g2 = (Graphics2D)g;
		super.paintComponent(g2);
		
		// paint a background rectangle to erase the modified image
		wpane     = this.getWidth();
		hpane     = this.getHeight();
		g2.setColor(background);
		g2.fillRect(0, 0, wpane, hpane);
		
//		// Scale image to take as much area of frame as possible
//		if (toDisplay == null) return;
		
		// Find the new scale of the image
		double scaleX = ((double)wpane)/((double)srcRect.width);
		double scaleY = ((double)hpane)/((double)srcRect.height);
		scale         = Math.min(scaleX, scaleY);
		int newW      = (int) (scale * srcRect.width);
		int newH      = (int) (scale * srcRect.height);
		
		dstRect.x      = wpane/2 - newW/2;
		dstRect.y      = hpane/2 - newH/2;
		
		dstRect = new Rectangle(dstRect.x,dstRect.y,newW,newH);
		displayPanel.setBounds(dstRect);

//		// resize the image
//		if (image == null || imw!=newW || imh!=newH){
//			imw   = newW;
//			imh   = newH;
//			image = toDisplay.getScaledInstance(newW, newH, Image.SCALE_DEFAULT);
//		}

		// draw the image
		g2.setColor(background);
		g2.fillRect(0, 0, wpane, hpane);
		g2.drawImage(adjustedSource, dstRect.x, dstRect.y, dstRect.x+dstRect.width, dstRect.y+dstRect.height, srcRect.x, srcRect.y, srcRect.x+srcRect.width, srcRect.y+srcRect.height, null);
//		g2.drawImage(toDisplay, dstRect.x, dstRect.y,this);
		
		// draw the title
		g2.setColor(Color.CYAN);
		g2.drawString(title, dstRect.x+10, dstRect.y+20);
		
		// draw zoom level
		int zoomInt = (int) (zoom * 100.0);
		g2.setColor(Color.CYAN);
		g2.drawString("Zoom = "+zoomInt+"%", dstRect.x+newW-100, dstRect.y+newH-10);
		
		// draw triangles to signify panning possibility
		g2.setColor(Color.CYAN);
		int tHeight   = 15 ;
		int tHalfWidth = 6;
		int tSpacing   = 5;
		if (scanLeft) {
			int x1 = dstRect.x+tSpacing;
			int y1 = dstRect.y+newH/2;
			int x2 = x1 + tHeight;
			int y2 = y1 - tHalfWidth;
			int x3 = x1 + tHeight;
			int y3 = y1 + tHalfWidth;
			int[] xPoints = new int[] {x1,x2,x3};
			int[] yPoints = new int[] {y1,y2,y3};
			g2.fillPolygon(xPoints, yPoints, 3);
		}
		if (scanRight) {
			int x1 = dstRect.x+newW-tSpacing;
			int y1 = dstRect.y+newH/2;
			int x2 = x1 - tHeight;
			int y2 = y1 - tHalfWidth;
			int x3 = x1 - tHeight;
			int y3 = y1 + tHalfWidth;
			int[] xPoints = new int[] {x1,x2,x3};
			int[] yPoints = new int[] {y1,y2,y3};
			g2.fillPolygon(xPoints, yPoints, 3);
		}
		if (scanUp) {
			int x1 = dstRect.x+newW/2;
			int y1 = dstRect.y+tSpacing;
			int x2 = x1 - tHalfWidth;
			int y2 = y1 + tHeight;
			int x3 = x1 + tHalfWidth;
			int y3 = y1 + tHeight;
			int[] xPoints = new int[] {x1,x2,x3};
			int[] yPoints = new int[] {y1,y2,y3};
			g2.fillPolygon(xPoints, yPoints, 3);
		}
		if (scanDown) {
			int x1 = dstRect.x+newW/2;
			int y1 = dstRect.y+newH-tSpacing;
			int x2 = x1 - tHalfWidth;
			int y2 = y1 - tHeight;
			int x3 = x1 + tHalfWidth;
			int y3 = y1 - tHeight;
			int[] xPoints = new int[] {x1,x2,x3};
			int[] yPoints = new int[] {y1,y2,y3};
			g2.fillPolygon(xPoints, yPoints, 3);
		}
		
		// Paint the Rois
		paintRois(g2);
		paintActiveRoi(g2);
		paintTrajectory(g2);
		paintClickedPoint(g2);
	}	
	
	/**
	 * Paint the active Roi a certain color
	 * @param g2
	 */
	public void paintActiveRoi(Graphics2D g2){
		if (activeROI!=null) paintRoi(g2,activeROI,Color.YELLOW);
	}
	
	/**
	 * Paint the inactive Roi a certain color
	 * @param g2
	 */
	public void paintRois(Graphics2D g2){
		if (rois == null) return;
		for (ROIPlus roip: rois){
			paintRoi(g2,roip,Color.CYAN);
		}
	}
	
	/**
	 * Paint single non-active roi
	 * @param g2 graphics2D
	 * @param roip RoiPlus
	 */
	private void paintRoi(Graphics2D g2, ROIPlus roip, Color color){
//		String type = roip.type;
		Color colorToPaint = (colors.get(roip) == null)? color : colors.get(roip);
		g2.setColor(colorToPaint);
		PointList points = roip.getPointList();
		for (Point p: points) paintPoint(g2,colorToPaint,p);
		HashSet<Point[]> lines = roip.getListOfLines();
		for (Point[] l: lines) paintLine(g2,colorToPaint,l);
	}
	
	/**
	 * Paint the trajectory on the image
	 * @param g2
	 */
	private void paintTrajectory(Graphics2D g2){
		if (trajectory == null) return;
		g2.setColor(Color.YELLOW);
		int frame1   = trajectory.initial();
		Point f2     = trajectory.getPoint(frame1);
		Point s2     = f2;
		
		while (s2 != null){
			paintPoint(g2,Color.yellow,f2);
			paintLine(g2,Color.yellow,new Point[] {f2,s2});
//			g2.fillOval(f2.x,f2.y,3,3);
//			g2.drawLine(f2.x, f2.y, s2.x, s2.y);
			f2     = s2;
			frame1 = trajectory.next(frame1);
			s2     = trajectory.getPoint(frame1);
		}
	}

	/**
	 * Paint a rectangle around the clicked point
	 */
	private void paintClickedPoint(Graphics2D g2){
		if (clickedPoint == null) return;
		int dispRadius = (int) (radius * scale);
		paintRectangle(g2,Color.red,clickedPoint,dispRadius);
	}
	
	/**
	 * Paint point of roi in the display 
	 * skip point if out of the field of view
	 * @param p
	 */
	private void paintPoint(Graphics2D g2, Color color, Point p){
		Point newP = imageToDisplay(p);
		if (isInDisplay(newP)){
			g2.setColor(color);
			g2.fillRect(newP.x-1, newP.y-1, 3, 3);
		}
	}
	
	/**
	 * Paint line in the display, 
	 * crop the line if it goes out of the field of view
	 * @param p1
	 * @param p2
	 */
	private void paintLine(Graphics2D g2, Color color, Point[] line){
		Point p1 = imageToDisplay(line[0]);
		Point p2 = imageToDisplay(line[1]);
		
		boolean isP1 = isInDisplay(p1);
		boolean isP2 = isInDisplay(p2);
		
		if (isP1 && isP2){
			g2.setColor(color);
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			return;
		}
		
		double slope = ((double)(p2.y - p1.y))/((double)(p2.x - p1.x));
		
		if (!isP1 && isP2){
			Point p11 = findFirstPointOfLineInDisplay(p1,slope);
			if (p11==null) return;
			g2.setColor(color);
			g2.drawLine(p11.x, p11.y, p2.x, p2.y);
		}
		else if (isP1 && !isP2){
			Point p21 = findFirstPointOfLineInDisplay(p2,slope);
			if (p21==null) return;
			g2.setColor(color);
			g2.drawLine(p1.x, p1.y, p21.x, p21.y);
		}
		else {
			Point p11 = findFirstPointOfLineInDisplay(p1,slope);
			Point p21 = findFirstPointOfLineInDisplay(p2,slope);
			if (p11==null || p21==null) return;
			g2.setColor(color);
			g2.drawLine(p11.x, p11.y, p21.x, p21.y);
		}
	}

	/**
	 * Paint line in the display, 
	 * crop the line if it goes out of the field of view
	 * @param p1
	 * @param p2
	 */
	private void paintRectangle(Graphics2D g2, Color color, Point p, int radius){
		Point newP = imageToDisplay(p);
		if (isInDisplay(newP)){
			g2.setColor(color);
			g2.drawRect(newP.x-radius, newP.y-radius, 2*radius+1, 2*radius+1);
		}
	}
	
	// ---------------------------------------------------------------------------------
	// -------------- Utilities to pass point from display to image world --------------
	// ---------------------------------------------------------------------------------

	/**
	 * Return a point in the image space from a point on the screen
	 * @param p
	 * @return point
	 */
	private Point displayToImage(Point p){
		// Values are truncated to the upperleft corner on purpose
		// Find distance of point from top left corner of dstRect image
		int x1 = (int) (((p.x - dstRect.x)) / scale);
		int y1 = (int) (((p.y - dstRect.y)) / scale);
		
//		// Find distance of point from top left corner of real image
//		int x2 = (int) ((double) x1 / zoom);
//		int y2 = (int) ((double) y1 / zoom);
		
		// Find coordinates of point in image
		int x3 = x1 + srcRect.x;
		int y3 = y1 + srcRect.y;
		
		Point result = new Point(x3,y3);
		return result;
	}
	
	/**
	 * Return a point in the display space from a point on the image
	 * @param p
	 * @return
	 */
	private Point imageToDisplay(Point p){
		// Find coordinate of point relative to top left displayed corner
		int x3 = p.x - srcRect.x ;
		int y3 = p.y - srcRect.y ;
		
		// Find coordinate of point in displayed (extracted) image
//		int x2 = (int) ((double) x3 * zoom);
//		int y2 = (int) ((double) y3 * zoom);
		
		// Find coordinate of point in displayed (scaled) image
		int x1 = (int) ((x3) * scale + dstRect.x);
		int y1 = (int) ((y3) * scale + dstRect.y);
		
		Point result = new Point(x1,y1);
		return result;
	}

	/**
	 * Return a vector in the image space from the display space
	 * @param v
	 * @return
	 */
	private Vect displayToImage(Vect v){
		Vect result = v.duplicate();
		result.multiply(1/scale);
		return result;
	}
	
	/**
	 * Return a vector in display space from the image space
	 * @param v
	 * @return
	 */
	@SuppressWarnings("unused")
	private Vect imageToDisplay(Vect v){
		Vect result = v.duplicate();
		result.multiply(scale);
		return result;
	}
	
	/**
	 * Returns true if point p should be displayed on the canvas
	 * @param p
	 * @return boolean
	 */
	private boolean isInDisplay(Point p){
		if (p.x>dstRect.x && p.y>dstRect.y && p.x<wpane-dstRect.x && p.y<hpane-dstRect.y) return true;
		return false;
	}
	
	/**
	 * Return the first point in the line passing by point P with slope SLOPE
	 * in the display range, return null if no overlap
	 * @param p
	 * @param slope
	 * @return Point
	 */
	private Point findFirstPointOfLineInDisplay(Point p, double slope){
		if (p.x < dstRect.x){
			int y = p.y + (int) (slope * (dstRect.x - p.x));
			Point result = new Point(dstRect.x+1,y);
			if (isInDisplay(result)) return result;
		}
		else if (p.x > wpane - dstRect.x){
			int y = p.y - (int) (slope * (p.x - wpane + dstRect.x));
			Point result = new Point(wpane-dstRect.x-1,y);
			if (isInDisplay(result)) return result;
		}
		else if (p.y < dstRect.y){
			int x = p.x + (int) ((1/slope) * (dstRect.y - p.y));
			Point result = new Point(x,dstRect.y+1);
			if (isInDisplay(result)) return result;
		}
		else if (p.y > hpane - dstRect.y){
			int x = p.x - (int) ((1/slope) * ((p.y - hpane + dstRect.y)));
			Point result = new Point(x,hpane-dstRect.y-1);
			if (isInDisplay(result)) return result;
		}
		
		return null;
	}
	
	// --------------------------------------------------
	// -------------- Mouse and key events --------------
	// --------------------------------------------------
//	private Point firstPoint = null; // To be replaced by second point to find difference between updates
	private Point firstPointMouseDisplay  = null ;  // Point saved for initiation of dragging of image
	private Point firstPointSrcCenter = null; // Point saved to keep track of original position;
//	private Point secondPoint = null ;  // Point saved for dragging of image
//	private boolean dragging  = false;  // Is in dragging mode
//	private Rectangle rectTmp = null ;  // temporary rectangle roi
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() > 1) return;
		if (e.getButton() == MouseEvent.BUTTON3){
			//if (lowLevelController != null) lowLevelController.clickedPoint(displayToImage(e.getPoint()));
			if (highLevelController != null) highLevelController.rightClickedPoint(displayToImage(e.getPoint()));
			//JEXStatics.logManager.log("Mouse right clicked", 0, this);
		}
//		if (e.getButton() == MouseEvent.BUTTON1){
//			Point p = e.getPoint();
//			Point p2 = new Point(p.x + (int)dstRect.getX(), p.y + (int)dstRect.getY());
//			
//			//if (lowLevelController != null) lowLevelController.clickedPoint(displayToImage(scaledP));
//			if (highLevelController != null) highLevelController.clickedPoint(displayToImage(p2));
//			//JEXStatics.logManager.log("Mouse left clicked", 0, this);
//		}
	}
	public void mouseEntered(MouseEvent e) {
		
	}
	public void mouseExited(MouseEvent e) {
		
	}
	public void mousePressed(MouseEvent e) {
		firstPointSrcCenter = new Point(srcCenter.x, srcCenter.y);
		firstPointMouseDisplay = e.getPoint();
	}
	public void mouseReleased(MouseEvent e) {
		firstPointMouseDisplay = null;
		firstPointSrcCenter = null;
		
		if (e.getButton() == MouseEvent.BUTTON1){
			Point p = e.getPoint();
			Point p2 = new Point(p.x + (int)dstRect.getX(), p.y + (int)dstRect.getY());
			if (highLevelController != null) highLevelController.clickedPoint(displayToImage(p2));
		}
	}
	public void mouseDragged(MouseEvent e) {
		Point scaledP = e.getPoint();
		
		if (e.isAltDown() && canZoomAndPan){
			this.dragToPoint(scaledP);
		}
//		if (!e.isAltDown() && lowLevelController != null) lowLevelController.mouseMoved(displayToImage(scaledP));
		if (!e.isAltDown() && highLevelController != null) {
//			int x = Math.min(firstPointMouseDisplay.x, scaledP.x);
//			int y = Math.min(firstPointMouseDisplay.y, scaledP.y);
//			int w = Math.abs(scaledP.x-firstPointMouseDisplay.x);
//			int h = Math.abs(scaledP.y-firstPointMouseDisplay.y);
			int x = firstPointMouseDisplay.x;
			int y = firstPointMouseDisplay.y;
			int w = scaledP.x-firstPointMouseDisplay.x;
			int h = scaledP.y-firstPointMouseDisplay.y;
			Rectangle rectTmp = new Rectangle(x,y,w,h);
			highLevelController.extendedRectangle(rectTmp);
		}

		return;
	}
	public void mouseMoved(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (canZoomAndPan){
			// Zoom around mouse pointer
			int units = e.getWheelRotation();
			Point scaledP = e.getPoint();
			zoom(-units,displayToImage(scaledP));
		}
	}
	
}
