package plugins.viewer;

import guiObject.PaintComponentDelegate;
import guiObject.PixelComponentDisplay;
import image.roi.PointList;
import image.roi.ROIPlus;
import image.roi.Vect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Vector;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import signals.SSCenter;

public class ImageDisplayController implements PaintComponentDelegate, MouseListener, MouseMotionListener, MouseWheelListener {
	
	public static final String SIG_StatusUpdated_NULL = "SIG_StatusUpdated_NULL";
	public static final String SIG_RoiEdited_NULL = "SIG_RoiEdited_NULL";
	public static final String SIG_RoiFinished_NULL = "SIG_RoiFinished_NULL";

	private PixelComponentDisplay display;
	private ImageDelegate imageDelegate;
	private RoiDelegate roiDelegate;
	
	public static final int MODE_VIEW = RoiDelegate.MODE_VIEW, MODE_MOVE = RoiDelegate.MODE_MOVE, MODE_CREATE = RoiDelegate.MODE_CREATE;
	private int mode;
	
	public ImageDisplayController()
	{
		this.display = new PixelComponentDisplay(this);
		//this.display.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
		this.display.addMouseListener((MouseListener) this);
		this.display.addMouseMotionListener((MouseMotionListener) this);
		this.display.addMouseWheelListener((MouseWheelListener) this);
		this.imageDelegate = new ImageDelegate();
		this.roiDelegate = null;
		this.mode = MODE_VIEW;
		this.currentLocation = new Point(0,0);
		SSCenter.defaultCenter().connect(this.imageDelegate, ImageDelegate.SIG_ImageUpdated_Null, this, "imageUpdated", (Class[])null);
		SSCenter.defaultCenter().connect(this.roiDelegate, RoiDelegate.SIG_RoiUpdated_Null, this, "roiUpdated", (Class[])null);
		SSCenter.defaultCenter().connect(this.roiDelegate, RoiDelegate.SIG_RoiFinished_Null, this, "roiFinished", (Class[])null);
	}
	
	public PixelComponentDisplay panel()
	{
		return this.display;
	}

	///////////////////////////////////////////////////////////
	////////////////////  MouseEvents  ////////////////////////
	///////////////////////////////////////////////////////////
	private static final int REG_Handle = 0, REG_Roi = 1, REG_Other = 2;
	private Point firstPointInDisplay  = null, firstPointSrcCenter = null, firstPointInImage = null;  // Point saved for initiation of dragging
	private Rectangle srcCenterDragRect = null, imageDragRect = null; // Rect saved for sending dragging info
	private int region = REG_Other;
	private Point xp = null;
	private Point currentLocation = null;
	private boolean dragged = false, roiOrHandleDragged = false;
	private Point initialHandleLocationInImage = null, initialRoiLocationInImage = null, currentRoiLocationInImage;
	
	///////////////////////////////////////////////////////////
	//////////////// handle MouseEvents ///////////////////////
	///////////////////////////////////////////////////////////
	public void mouseClicked(MouseEvent e)
	{
		if(this.mode == MODE_VIEW) return;
//		if(this.mode == MODE_CREATE && e.getClickCount() == 2)
//		{
//			this.roiDelegate.finishRoi();
//			return;
//		}
		if(e.getClickCount() > 1) return;
		if(e.getButton() == MouseEvent.BUTTON3)
		{
			if(region == REG_Handle)
			{
				this.roiDelegate.handleRightClicked(xp);
			}
			else if(region == REG_Other && mode == MODE_CREATE)
			{
//				if(this.roiDelegate.size() == 0)
//					SSCenter.defaultCenter().emit(this, SIG_SaveRoi_NULL, (Object[])null);
//				else
//					this.roiDelegate.rightClicked(this.imageDelegate.displayToImage(this.display, e.getPoint()));
				this.roiDelegate.rightClicked(this.imageDelegate.displayToImage(this.display, e.getPoint()));
			}
			else if(region == REG_Roi && mode == MODE_MOVE)
			{
				//SSCenter.defaultCenter().emit(this, SIG_SaveRoi_NULL, (Object[])null);
			}
		}
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			if(region == REG_Handle)
			{
				this.roiDelegate.handleLeftClicked(xp);
			}
			else if(region == REG_Other && mode == MODE_CREATE)
			{
				this.roiDelegate.leftClicked(this.imageDelegate.displayToImage(this.display, e.getPoint()));
			}
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		
		if(!(e.getButton() == MouseEvent.BUTTON1)) return;
		
		firstPointInDisplay = e.getPoint();
		firstPointSrcCenter = this.imageDelegate.getSrcCenter();
		firstPointInImage = this.imageDelegate.displayToImage(this.display, firstPointInDisplay);
		imageDragRect = new Rectangle(firstPointInImage.x, firstPointInImage.y, 0, 0);
		srcCenterDragRect = new Rectangle(firstPointSrcCenter.x, firstPointSrcCenter.y, 0, 0);
		determineAndSetRegionOfPoint(e.getPoint());
		if(xp != null) initialHandleLocationInImage = new Point(xp.x, xp.y);
		if(this.roiDelegate.size() > 0)
		{
			currentRoiLocationInImage = this.roiDelegate.editablePoints.get(0);
			initialRoiLocationInImage = (Point)currentRoiLocationInImage.clone();
		}
	}
	public void mouseReleased(MouseEvent e) {
		if(!(e.getButton() == MouseEvent.BUTTON1)) return;
		if(dragged)
		{
			this.roiDelegate.dragFinished(imageDragRect);
			dragged = false;
		}
		firstPointInDisplay = null;
		firstPointInImage = null;
		firstPointSrcCenter = null;
		srcCenterDragRect = null;
		imageDragRect = null;
		region = REG_Other;
		initialHandleLocationInImage = null;
		initialRoiLocationInImage = null;
		xp = null;
		if(roiOrHandleDragged)
		{
			 SSCenter.defaultCenter().emit(this, SIG_RoiEdited_NULL, (Object[])null);
			 roiOrHandleDragged = false;
		}
		
	}
	public void mouseDragged(MouseEvent e){
		
		if(!(e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK)) return;
		
		// Set the currentLocation;
		currentLocation = e.getPoint();
		
		// Determine what is being dragged and send current drag rectangle
		Vect shift = new Vect(currentLocation.x-firstPointInDisplay.x, currentLocation.y-firstPointInDisplay.y);
		shift = this.imageDelegate.displayToImage(this.display, shift);
		
		// dragRect.x and y already hold the firstPoints, just update the width and height
		srcCenterDragRect.width = (int)shift.dX;
		srcCenterDragRect.height = (int)shift.dY;
		imageDragRect.width = (int)shift.dX;
		imageDragRect.height = (int)shift.dY;
		
		if(region == REG_Handle && mode == MODE_MOVE)
		{
			roiOrHandleDragged = true; // for knowing when to signal a roiEdited signal
			if(xp == null)
				JEXStatics.logManager.log("This shouldn't happen but handle was clicked and no xp was returned", 0, this);
			xp.setLocation(initialHandleLocationInImage.x + imageDragRect.width, initialHandleLocationInImage.y + imageDragRect.height);
			this.display.repaint();
		}
		else if(region == REG_Handle && mode == MODE_CREATE)
		{
			roiOrHandleDragged = false; // for knowing when to signal a roiEdited signal
			if(xp == null)
				JEXStatics.logManager.log("This shouldn't happen but handle was clicked and no xp was returned", 0, this);
			xp.setLocation(initialHandleLocationInImage.x + imageDragRect.width, initialHandleLocationInImage.y + imageDragRect.height);
			this.display.repaint();
		}
		else if(region == REG_Roi && mode == MODE_MOVE)
		{
			roiOrHandleDragged = true;  // for knowing when to signal a roiEdited signal
			int oldDragRectWidth = currentRoiLocationInImage.x - initialRoiLocationInImage.x;
			int oldDragRectHeight = currentRoiLocationInImage.y - initialRoiLocationInImage.y;
			this.roiDelegate.editablePoints.translate(imageDragRect.width - oldDragRectWidth, imageDragRect.height - oldDragRectHeight);
//			JEXStatics.logManager.log("currentRoiLocation: " + currentRoiLocationInImage, 0, this);
//			JEXStatics.logManager.log("oldDragRectWidth: " + oldDragRectWidth, 0, this);
//			JEXStatics.logManager.log("oldDragRectHeight: " + oldDragRectHeight, 0, this);
//			JEXStatics.logManager.log("imageDragRect: " + imageDragRect, 0, this);
			this.display.repaint();			
		}
		else if(mode == MODE_CREATE)
		{
			dragged = true;
			this.roiDelegate.dragging(imageDragRect);
		}
		else if(!this.imageDelegate.getFitsInDestination())
		{
			this.imageDelegate.imageDragged(srcCenterDragRect);
		}
		SSCenter.defaultCenter().emit(this, SIG_StatusUpdated_NULL, (Object[])null);
	}
	public void mouseMoved(MouseEvent e)
	{
		currentLocation = e.getPoint();
		if(mode == MODE_CREATE)
		{
			if(this.roiDelegate.size() > 0)
			{
				this.display.repaint();
			}
		}
		SSCenter.defaultCenter().emit(this, SIG_StatusUpdated_NULL, (Object[])null);
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		// Zoom around mouse pointer
		int wheelUnits = e.getWheelRotation();
		boolean alt_option_command_pressed = (e.isAltDown() || e.isAltGraphDown() || e.isMetaDown());
		boolean isVertical = !e.isShiftDown();
		if(isVertical && alt_option_command_pressed) // then alt/option/command is down and moving in vertical direction
		{
			Point center = getDisplayPanelCenter();
			// JEXStatics.loManager.log("Zooming to imagePt: " + imageCenter.x + "," + imageCenter.y, 0, this);
			this.imageDelegate.setZoom(-1*wheelUnits,this.imageDelegate.displayToImage(this.display, center));
		}
		else if(!isVertical && !alt_option_command_pressed && !this.imageDelegate.getFitsInDestination()) // then horizontal and not zooming
		{
			if(wheelUnits > 0) this.nudgeRight();
			else this.nudgeLeft();
		}
		else if(isVertical && !alt_option_command_pressed && !this.imageDelegate.getFitsInDestination()) // then vertical and not zooming
		{
			if(wheelUnits < 0) this.nudgeUp();
			else this.nudgeDown();
		}
	}
	
	public void determineAndSetRegionOfPoint(Point p)
	{
		if(mode == MODE_VIEW)
		{
			this.region = REG_Other;
			return;
		}
		
		PointList handles = this.imageDelegate.imageToDisplay(this.display, this.roiDelegate.getEditablePoints());
		
		if(this.roiDelegate.isLineType())
		{
			// Lines don't have a region. A two point line is special though
			// the two point line converts a handle drag to a roi drag if a
			// special handle is dragged.
			
			// Adjust click by a half an image pixel to simulate adjusting all
			// line handles to the center of each pixel
			Vect v = new Vect(-0.5,-0.5);
			v = this.imageDelegate.imageToDisplay(this.display, v);
			Point adjustedPoint = new Point((int) (p.x + v.dX), (int) (p.y + v.dY));
			
			Point handle = handles.nearestPointInRectRange(adjustedPoint, this.handleRadius);
			if(handle != null)
			{
				int index = handles.indexOf(handle);
				Point imageHandle = this.roiDelegate.getEditablePoints().get(index);
				xp = imageHandle;//this.roiDelegate.getReferenceOfPointAt(imageHandle);
				this.region = REG_Handle;
			}
			else if(mode != MODE_CREATE && this.roiDelegate.pointIsInRoi(this.imageDelegate.displayToImage(this.display, p)))
			{
				this.region = REG_Roi;
			}
			else
			{
				this.region = REG_Other;
			}
			
		}
		else
		{
			Point handle = handles.nearestPointInRectRange(p, this.handleRadius);
			if(handle != null)
			{
				int index = handles.indexOf(handle);
				Point imageHandle = this.roiDelegate.getEditablePoints().get(index);
				xp = imageHandle;//this.roiDelegate.getReferenceOfPointAt(imageHandle);
				this.region = REG_Handle;
			}
			else if(mode != MODE_CREATE && this.roiDelegate.pointIsInRoi(this.imageDelegate.displayToImage(this.display, p)))
			{
				this.region = REG_Roi;
			}
			else
			{
				this.region = REG_Other;
			}
		}
	}	
	
	///////////////////////////////////////////////////////////
	////////////////  signal responses  ///////////////////////
	///////////////////////////////////////////////////////////
	
	public void imageUpdated()
	{
		this.display.repaint();
		SSCenter.defaultCenter().emit(this, SIG_StatusUpdated_NULL, (Object[])null);
	}
	
	public void roiUpdated()
	{
		this.display.repaint();
		SSCenter.defaultCenter().emit(this, SIG_StatusUpdated_NULL, (Object[])null);
	}
	
	public void roiFinished()
	{
		this.display.repaint();
		SSCenter.defaultCenter().emit(this, SIG_RoiEdited_NULL, (Object[])null);
		SSCenter.defaultCenter().emit(this, SIG_RoiFinished_NULL, (Object[])null);
		SSCenter.defaultCenter().emit(this, SIG_StatusUpdated_NULL, (Object[])null);
	}
	
	/////////////////////////////////////////////////////////////////////////
	///////////////////////// painting methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////
	private int handleRadius = 5;
	private Color zoomIndicatorColor = new Color(128, 128, 255), lineColor = Color.YELLOW;
	private Stroke zoomIndicatorStroke = new BasicStroke(1);
	private Rectangle dstRect, srcRect, displaySrcRect, imageRect, displayImageRect, intersectionRect;

	public void paintComponent(Graphics2D g2)
	{
		// Don't need to start with call to super.paintComponent(g2) because we will draw the background ourselves.
		
		// Gather the rectangles that represent important positions 
		dstRect = this.display.getBoundsLocal(); // panel region
		srcRect = this.imageDelegate.getSrcRect(dstRect); // region of original image being drawn
		displaySrcRect = this.imageDelegate.imageToDisplay(this.display, srcRect); // Actual drawn region including padding but not panel background
		imageRect = this.imageDelegate.getImageRect(); // Size of original image
		displayImageRect = this.imageDelegate.imageToDisplay(this.display, imageRect); // Actual drawn region without padding but including panel background
		intersectionRect = intersection(displaySrcRect,displayImageRect); // Actual drawn region without padding and without panel background
		
		// Paint the image Background
		g2.setColor(DisplayStatics.background);
		g2.fill(this.display.getBoundsLocal());
		
		// Paint the image
		if(this.imageDelegate.getImage() != null)
			g2.drawImage(this.imageDelegate.getImage(), displaySrcRect.x, displaySrcRect.y, displaySrcRect.x+displaySrcRect.width, displaySrcRect.y+displaySrcRect.height, srcRect.x, srcRect.y, srcRect.x+srcRect.width, srcRect.y+srcRect.height, null);
		else return;
		
		// Paint any smashed roi lines
		this.paintSmashedLines(g2);

		// Paint any live rois only if it has any points to draw
		if(this.roiDelegate.size() > 0)
		{
			g2.setClip(intersectionRect);
			this.paintSmashedLines(g2);
			if(mode == MODE_VIEW)
			{
				lineColor = Color.RED;
				g2.setColor(lineColor);
				this.paintLines(g2);
			}
			else if(mode == MODE_MOVE)
			{
				lineColor = Color.GREEN;
				g2.setColor(lineColor);
				this.paintLines(g2);
				this.paintHandles(g2);
			}
			else
			{
				lineColor = Color.YELLOW;
				g2.setColor(lineColor);
				this.paintLines(g2);
				this.paintHandles(g2);
			}
		}
		
		// Paint the zoom indicator if necessary
		if(!this.imageDelegate.getFitsInDestination())
		{
			this.paintZoomIndicator(g2, srcRect);
		}
		
		// dispose because we know we don't have any children components that need to paint.
		g2.dispose();
	}
	
	private void paintZoomIndicator(Graphics2D g2, Rectangle srcRect) 
	{
		Point zoomIndicatorLocation = new Point(10,10);
        
		int x1 = zoomIndicatorLocation.x;
        int y1 = zoomIndicatorLocation.y;
        Rectangle imageRect = this.imageDelegate.getImageRect();
        double aspectRatio = (double)imageRect.height/imageRect.width;
        double indicatorScale = 64.0/imageRect.width;
        int w1 = 64;
        if (aspectRatio>1.0)
        {
            w1 = (int)(w1/aspectRatio);
            indicatorScale = w1/imageRect.width;
        }
        int h1 = (int)(w1*aspectRatio);
        if (w1<4) w1 = 4;
        if (h1<4) h1 = 4;
        // Rectangle imageIndicator = new Rectangle(x1, y1, w1, h1);
        
        // scale the srcRect Location
        int x2 = (int)Math.round(srcRect.x*indicatorScale);// + zoomIndicatorLocation.x);
        int y2 = (int)Math.round(srcRect.y*indicatorScale);// + zoomIndicatorLocation.y);
        int w2 = (int)Math.round(srcRect.width*indicatorScale);
        int h2 = (int)Math.round(srcRect.height*indicatorScale);
        if (w2<1) w2 = 1;
        if (h2<1) h2 = 1;
        // Rectangle viewedIndicator = new Rectangle(x2, y2, w2, h2);
        
        g2.setColor(zoomIndicatorColor);
        (g2).setStroke(zoomIndicatorStroke);
        g2.drawRect(x1, y1, w1, h1);
        //RepaintManager.currentManager(this.display).addDirtyRegion(this.display, x1, y1, w1, h1);
        if (w2*h2<=200 || w2<10 || h2<10)
            g2.fillRect(x1+x2, y1+y2, w2, h2);
        else
            g2.drawRect(x1+x2, y1+y2, w2, h2);
    }
	
	private static void paintHandle(Graphics2D g2, Color[] handleBorderAndFillColor, Point p, int radius)
	{
        g2.setColor(handleBorderAndFillColor[0]);
        g2.fillRect(p.x-radius,p.y-radius,radius*2+1,radius*2+1);
        g2.setColor(handleBorderAndFillColor[1]);
        g2.fillRect(p.x-radius+1,p.y-radius+1,radius*2-1,radius*2-1);
    }
	
	public void paintHandles(Graphics2D g2)
	{
		//JEXStatics.logManager.log("PAINTHANDLES", 0, this);
		// paint handles for every point in linehandles and regionhandles
		PointList handles = this.imageDelegate.imageToDisplay(this.display, this.roiDelegate.getEditablePoints());
		if(handles.size() == 0) return;
		if(this.roiDelegate.isLineType() || this.roiDelegate.type() == ROIPlus.ROI_POINT)
		{
			Vect v = new Vect(0.5,0.5);
			v = this.imageDelegate.imageToDisplay(this.display, v);
			handles.translate(v.dX, v.dY);
		}
		for(int i = 0; i < handles.size(); i++)
		{
			Color[] colors = this.roiDelegate.getHandleBorderAndFillColor(i);
			paintHandle(g2, colors, handles.get(i), handleRadius);
		}
		//handles.clear();
	}
	
	public void paintLines(Graphics2D g2)
	{
		//JEXStatics.logManager.log("PAINTLINES", 0, this);
		PointList transformedPoints = this.imageDelegate.imageToDisplay(this.display, this.roiDelegate.editablePoints);
		Point growthPoint = this.imageDelegate.imageToDisplay(this.display, this.imageDelegate.displayToImage(this.display, currentLocation));
		if(this.roiDelegate.isLineType() || this.roiDelegate.type() == ROIPlus.ROI_POINT)
		{
			Vect v = new Vect(0.5,0.5);
			v = this.imageDelegate.imageToDisplay(this.display, v);
			growthPoint.x = growthPoint.x + (int)v.dX;
			growthPoint.y = growthPoint.y + (int)v.dY;
			transformedPoints.translate(v.dX, v.dY);
		}
		this.roiDelegate.paintLinesForPointList(g2, transformedPoints, mode, growthPoint);
		//transformedPoints.clear();
	}
	
	public void paintSmashedLines(Graphics2D g2)
	{
		List<PointList> transformedSmashedPoints = new Vector<PointList>();
		Vect v = new Vect(0.5,0.5);
		v = this.imageDelegate.imageToDisplay(this.display, v);
		PointList transformedPl;
		for(PointList pl : this.roiDelegate.getSmashedPoints())
		{
			transformedPl = this.imageDelegate.imageToDisplay(this.display, pl);
			if(this.roiDelegate.isLineType() || this.roiDelegate.type() == ROIPlus.ROI_POINT);
			{
				transformedPl.translate(v.dX, v.dY);
			}
			transformedSmashedPoints.add(transformedPl);
		}
		this.roiDelegate.paintSmashedRois(g2, transformedSmashedPoints);
	}
	
	/**
	 * The first rectangle will contain the 
	 * @return
	 */
	@SuppressWarnings("unused")
	private Rectangle getRoiClipRect()
	{
		Rectangle ret = this.roiDelegate.getBounds();
		
		int x2 = 0, y2 = 0;
		
		ret = this.imageDelegate.imageToDisplay(this.display, ret);
		if(this.roiDelegate.size() == 0)
		{
			ret.x = currentLocation.x;
			ret.y = currentLocation.y;
		}
		
		if(this.roiDelegate.isLineType())
		{
			Vect v = new Vect(0.5,0.5);
			v = this.imageDelegate.imageToDisplay(this.display, v);
			ret.x = (int)(ret.x + v.dX);
			ret.y = (int)(ret.y + v.dY);
		}
		
		if(mode == MODE_CREATE)
		{
			x2 = (int) Math.min(Math.max(ret.x+ret.width+handleRadius+3, currentLocation.x+1), intersectionRect.x+intersectionRect.width);
			y2 = (int) Math.min(Math.max(ret.y+ret.height+handleRadius+3, currentLocation.y+1), intersectionRect.y+intersectionRect.height);		
			ret.x = (int) Math.max(Math.min(ret.x-handleRadius, currentLocation.x-1), intersectionRect.x+1);
			ret.y = (int) Math.max(Math.min(ret.y-handleRadius, currentLocation.y-1), intersectionRect.y+1);
		}
		else
		{
			x2 = (int) Math.min(ret.x+ret.width+handleRadius+3, intersectionRect.x+intersectionRect.width);
			y2 = (int) Math.min(ret.y+ret.height+handleRadius+3, intersectionRect.y+intersectionRect.height);		
			ret.x = (int) Math.max(ret.x-handleRadius, intersectionRect.x+1);
			ret.y = (int) Math.max(ret.y-handleRadius, intersectionRect.y+1);
		}
		
		ret.width = x2-ret.x;
		ret.height = y2-ret.y;
		
		return ret;
	}
	
	private static Rectangle intersection(Rectangle r1, Rectangle r2)
	{
		Rectangle ret = new Rectangle();
		
		int x2 = Math.min(r1.x + r1.width, r2.x + r2.width);
		int y2 = Math.min(r1.y + r1.height, r2.y + r2.height);
		ret.x = Math.max(r1.x, r2.x);
		ret.y = Math.max(r1.y, r2.y);
		ret.width = x2 - ret.x;
		ret.height = y2 - ret.y;
		
		return ret;
		
	}
	
	private Point getDisplayPanelCenter()
	{
		Rectangle center = this.display.getBounds();
		return new Point(center.x+center.width/2, center.y + center.height/2);
	}
	
	@SuppressWarnings("unused")
	private static Rectangle getComboBounds(Rectangle r1, Rectangle r2)
	{
		Rectangle ret = new Rectangle();
		ret.x = Math.min(r1.x, r2.x);
		ret.y = Math.min(r1.y, r2.y);
		int x2 = Math.max(r1.x+r1.width, r2.x+r2.width);
		int y2 = Math.max(r1.y+r1.height, r2.y+r2.height);
		ret.width = x2-ret.x;
		ret.height = y2-ret.y;
		return ret;
	}
	
	/////////////////////////////////////////////////////////////////////////
	///////////////////      status information         /////////////////////
	/////////////////////////////////////////////////////////////////////////
	
	public int minImageIntensity()
	{
		return (int) this.imageDelegate.minImageIntensity();
	}
	
	public int maxImageIntensity()
	{
		return (int) this.imageDelegate.maxImageIntensity();
	}
	
	public int minDisplayIntensity()
	{
		return this.imageDelegate.minDisplayIntensity();
	}
	
	public int maxDisplayIntensity()
	{
		return this.imageDelegate.maxDisplayIntensity();
	}
	
	public int bitDepth()
	{
		return this.imageDelegate.bitDepth();
	}
	
	public double getZoom()
	{
		return this.imageDelegate.getZoom();
	}
	
	public double getPixelIntensity()
	{
		return this.imageDelegate.getPixelIntensity(this.imageDelegate.displayToImage(this.display, currentLocation));
	}
	
	public Point currentImageLocation()
	{
		return this.imageDelegate.displayToImage(this.display, this.currentLocation);
	}
	
	public Rectangle imageRect()
	{
		return this.imageDelegate.getImageRect();
	}
	
	public Rectangle roiRect()
	{
		if(this.roiDelegate == null) return null;
		return this.roiDelegate.getEditablePoints().getBounds();
	}
	
	public boolean hasImage()
	{
		return this.imageDelegate.getImage() != null;
	}
	
	/////////////////////////////////////////////////////////////////////////
	//////////////////////////////     actions     //////////////////////////
	/////////////////////////////////////////////////////////////////////////
	
	public void setImage(String path)
	{
		this.imageDelegate.setImage(path);
	}
	
	public void setLimits(int min, int max)
	{
		this.imageDelegate.setDisplayLimits(min, max);
	}
	
	public void setRoiDisplay(int type, PointList editable, List<PointList> smashed)
	{
		if(this.roiDelegate != null) 
		{
			this.roiDelegate.clearAllPoints();
			SSCenter.defaultCenter().disconnect(this.roiDelegate, RoiDelegate.SIG_RoiUpdated_Null, this);
			SSCenter.defaultCenter().disconnect(this.roiDelegate, RoiDelegate.SIG_RoiFinished_Null, this);
		}
		this.roiDelegate = this.getDelegateOfType(type);
		SSCenter.defaultCenter().connect(this.roiDelegate, RoiDelegate.SIG_RoiUpdated_Null, this, "roiUpdated", (Class[])null);
		SSCenter.defaultCenter().connect(this.roiDelegate, RoiDelegate.SIG_RoiFinished_Null, this, "roiFinished", (Class[])null);
		if(editable == null) editable = new PointList();
		this.roiDelegate.setEditablePoints(editable.clone());
		if(smashed == null) smashed = new Vector<PointList>();
		this.roiDelegate.setSmashedPoints(smashed);
		
		// paint new roi
		this.display.repaint();
		SSCenter.defaultCenter().emit(this, SIG_StatusUpdated_NULL, (Object[])null);
	}
	
//	public void setSmashedPoints(List<PointList> smashed)
//	{
//		this.roiDelegate.setSmashedPoints(smashed);
//	}
	
	/**
	 * returns a new copy of the points for saving that will not be altered
	 * behind the back of the RoiDelegate
	 * @return
	 */
	public PointList getEditablePoints()
	{
		return this.roiDelegate.getEditablePoints().clone();
	}
	
	public int getRoiType()
	{
		return this.roiDelegate.type();
	}
	
	private RoiDelegate getDelegateOfType(int type)
	{
		if(type == ROIPlus.ROI_RECT)
		{
			return new RectDelegate(null);		
		}
		else if(type == ROIPlus.ROI_ELLIPSE)
		{
			return new EllipseDelegate(null);			
		}
		else if(type == ROIPlus.ROI_LINE)
		{
			return new LineDelegate(null);		
		}
		else if(type == ROIPlus.ROI_POLYGON)
		{
			return new PGonDelegate(null);		
		}
		else if(type == ROIPlus.ROI_POLYLINE)
		{
			return new PLineDelegate(null);			
		}
		else if(type == ROIPlus.ROI_POINT)
		{
			return new PointDelegate(null);			
		}
		else // type == ROIPlus.ROI_UNDEFINED
		{
			return new RoiDelegate(null);
		}
			
	}
	
//	private void paintImage()
//	{
//		this.paintImageFlag = true;
//		this.paintRoiFlag = false;
//		this.display.repaint();
//		this.paintImageFlag = true;
//		this.paintRoiFlag = true;
//	}
	
	public int mode()
	{
		return this.mode;
	}
	
	public void setMode(int mode)
	{
		if(mode != this.mode)
		{
			this.mode = mode;
			this.display.repaint();			
		}
	}
	
	public void setHandleRadius(int radius)
	{
		this.handleRadius = radius;
		this.display.repaint();
	}
	
	private double nudgePercent = 0.1;
	
	public void nudgeRight()
	{
		double dX = this.display.getBoundsLocal().width*nudgePercent;
		this.imageDelegate.translateSrcRect(this.imageDelegate.displayToImage(this.display, new Vect(dX, 0))); // results in update signal
	}
	
	public void nudgeLeft()
	{
		double dX = this.display.getBoundsLocal().width*nudgePercent;
		this.imageDelegate.translateSrcRect(this.imageDelegate.displayToImage(this.display, new Vect(-dX, 0))); // results in update signal
	}
	
	public void nudgeUp()
	{
		double dY = this.display.getBoundsLocal().height*nudgePercent;
		this.imageDelegate.translateSrcRect(this.imageDelegate.displayToImage(this.display, new Vect(0, -dY))); // results in update signal
		// JEXStatics.loManager.log("nudge up " + (-1*dY), 0, this);
	}
	
	public void nudgeDown()
	{
		double dY = this.display.getBoundsLocal().height*nudgePercent;
		this.imageDelegate.translateSrcRect(this.imageDelegate.displayToImage(this.display, new Vect(0, dY))); // results in update signal
		// JEXStatics.loManager.log("nudge down " + (-1*dY), 0, this);
	}
	
	public void finishRoi()
	{
		if(mode == MODE_CREATE) this.roiDelegate.finishRoi();
	}
	
	public void deleteRoi()
	{
		this.roiDelegate.editablePoints.clear();
		SSCenter.defaultCenter().emit(this, SIG_RoiEdited_NULL, (Object[])null);
	}
	
	public void zoomIn()
	{
		Point center = getDisplayPanelCenter();
		this.imageDelegate.setZoom(1,this.imageDelegate.displayToImage(this.display, center)); // results in update signal
	}
	
	public void zoomOut()
	{
		Point center = getDisplayPanelCenter();
		this.imageDelegate.setZoom(-1,this.imageDelegate.displayToImage(this.display, center)); // results in update signal
	}

//	public void keyPressed(KeyEvent e) {
//		if(e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE)
//		{
//			if(mode == MODE_CREATE) this.roiDelegate.finishRoi();
//		}
//		else if(KeyStatics.ctrlOrCmdIsDown() && e.getKeyCode() == KeyEvent.VK_EQUALS) 
//		{
//			Point center = getDisplayPanelCenter();
//			this.imageDelegate.setZoom(1,this.imageDelegate.displayToImage(this.display, center)); // results in update signal
//			// JEXStatics.loManager.log("Zoom in", 0, this);
//		}
//		else if(KeyStatics.ctrlOrCmdIsDown() && e.getKeyCode() == KeyEvent.VK_MINUS)
//		{
//			Point center = getDisplayPanelCenter();
//			this.imageDelegate.setZoom(-1,this.imageDelegate.displayToImage(this.display, center));
//			// JEXStatics.loManager.log("Zoom out", 0, this);
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
//		{
//			this.nudgeRight();
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_LEFT)
//		{
//			this.nudgeLeft();
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_UP)
//		{
//			this.nudgeUp();
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_DOWN)
//		{
//			this.nudgeDown();
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
//		{
//			this.roiDelegate.editablePoints.clear();
//			SSCenter.defaultCenter().emit(this, SIG_RoiEdited_NULL, (Object[])null);
//		}
//		else
//		{
//			
//		}
//	}
//
//	public void keyReleased(KeyEvent e) 
//	{
//		
//	}
//
//	public void keyTyped(KeyEvent e)
//	{
//		
//	}
	
	
}
