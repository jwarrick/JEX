package plugins.viewer;

import guiObject.PixelComponentDisplay;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import image.roi.PointList;
import image.roi.Vect;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import jex.statics.JEXStatics;
import signals.SSCenter;

public class ImageDelegate {
	
	public static final String SIG_ImageUpdated_Null = "SIG_ImageUpdated_Null";
	
	// Source image variables
	private ImagePlus image = null;
	private String imagePath = null;
	private ImageProcessor sourceIJ   = null ; // for looking up individual pixels
	private Image	adjustedSource = null;
	private int bitDepth = 0;
	private int minDisplayIntensity = -1, maxDisplayIntensity = -1;
	private double minImageIntensity = -1, maxImageIntensity = -1;
	private Rectangle imageRect = new Rectangle(0,0,0,0);
	private Rectangle srcRect = new Rectangle(0,0,0,0);
	private Point srcCenter = new Point(-1,-1);
	private double zoom      = 1    ; // current zoom scale
	private boolean fitToDestination = true;
	private int maxZoom = 25;
	private double minZoom = 0.1;
	private double zoomFactor = 1.5;
	private boolean forceFit = true;
	
	
	public void setImage(String path)
	{
		if(path == null)
		{
			this.setImage((ImagePlus)null);
			return;
		}
		if(path.equals(this.imagePath))
		{
			return;
		}
		this.imagePath = path;
		JEXStatics.logManager.log("Opening image at path "+this.imagePath, 1, this);
		this.setImage(new ImagePlus(path));
	}
	
	/**
	 * Set the displayed image
	 * @param image
	 */
	public void setImage(ImagePlus image)
	{
		if(image == null)
		{
			this.image = null;
			this.adjustedSource = null;
			this.sourceIJ = null;
			return;
		}

		this.image = image;
		ImageProcessor proc = image.getProcessor();
		ImageStatistics stats = (proc == null) ? null : ImageStatistics.getStatistics(proc, ImageStatistics.MIN_MAX, null);
		this.minImageIntensity = (stats == null) ? 0 : stats.min;
		this.maxImageIntensity = (stats == null) ? 255 : stats.max;
		if(image != null)
		{
			// JEXStatics.logManager.log("Getting bitDepth", 0, this);
			this.bitDepth = image.getBitDepth();
			// JEXStatics.logManager.log("Getting Processor", 0, this);
			this.sourceIJ = image.getProcessor();
			if(this.maxDisplayIntensity == -1 || this.minDisplayIntensity == -1)
			{
				JEXStatics.logManager.log("Getting intensities", 0, this);
				this.minDisplayIntensity = (int)this.minImageIntensity;
				this.maxDisplayIntensity = (int)this.maxImageIntensity;
			}
			
			// setLimits sets the adjustedSource image.
			// JEXStatics.logManager.log("Setting intensities", 0, this);
			if(this.image.getType() == ImagePlus.COLOR_RGB)
			{
				this.sourceIJ.snapshot();
			}
			this.setDisplayLimits(this.minDisplayIntensity, this.maxDisplayIntensity);
			if(this.sourceIJ != null && imageRect.width == this.sourceIJ.getWidth() && imageRect.height == this.sourceIJ.getHeight())
			{
				imageUpdated();
			}
			else
			{
				if (sourceIJ == null)
				{
					imageRect.width		= 0;
					imageRect.height	= 0;
					srcCenter.x			= imageRect.width / 2;
					srcCenter.y			= imageRect.height / 2;
					zoom				= 1;
				}
				else
				{
					imageRect.width		= sourceIJ.getWidth();
					imageRect.height	= sourceIJ.getHeight();
					srcCenter.x     	= imageRect.width / 2;
					srcCenter.y			= imageRect.height / 2;
					zoom				= 1;
				}
				this.setToFitToDestination();
			}
		}
	}
	
	public Image getImage()
	{
		return this.adjustedSource;
	}
	
	public void setDisplayLimits(int min, int max)
	{
		this.minDisplayIntensity = min;
		this.maxDisplayIntensity = max;
		if(this.image == null) return;
		// JEXStatics.logManager.log("Setting limits", 0, this);
		if(this.image.getType() == ImagePlus.COLOR_RGB)
		{
			this.sourceIJ.reset();
			this.sourceIJ.setMinAndMax(min, max);
			this.adjustedSource = this.sourceIJ.createImage();
		}
		else if (sourceIJ != null)
		{
			this.sourceIJ.setMinAndMax((double) min, (double) max);
			this.adjustedSource = this.sourceIJ.createImage();
		}
		else
		{
			this.sourceIJ = null;
			this.adjustedSource = null;
		}
		// JEXStatics.logManager.log("Creating image", 0, this);
		
		// JEXStatics.logManager.log("Updating image after setting limits", 0, this);
		imageUpdated();
	}
	
	public int minDisplayIntensity()
	{
		return this.minDisplayIntensity;
	}
	
	public int maxDisplayIntensity()
	{
		return this.maxDisplayIntensity;
	}
	
	public double minImageIntensity()
	{
		return this.minImageIntensity;
	}
	
	public double maxImageIntensity()
	{
		return this.maxImageIntensity;
	}
	
	public int bitDepth()
	{
		return this.bitDepth;
	}
	
	// ImageDelegate methods called by ModelDelegates
	
	/**
	 * Zoom image around point (srcCenter.x,srcCenter.y)
	 * @param units number of increments
	 * @param centerP center point around which zoom is done in the actual image
	 */
	public void setZoom(int units, Point centerP)
	{
		this.fitToDestination = false;
		
		double newZoom = maxZoom;
		if(units > 0) // which means to zoom in.
		{
			newZoom = this.getNextHigherZoomLevel(zoom);
		}
		else
		{
			newZoom = this.getNextLowerZoomLevel(zoom);
		}
		if(this.zoom != newZoom)
		{
			this.zoom = newZoom;
			this.srcCenter.x     = (int) centerP.x;
			this.srcCenter.y     = (int) centerP.y;
			// JEXStatics.logManager.log("Zooming to image point: "+ this.srcCenter.x + "," + this.srcCenter.y, 0, this);
			imageUpdated(); // sets fitToDestination back to true if necessary via getSrcRect
		}
	}
	
	/**
	 * Zoom image around point (srcCenter.x,srcCenter.y)
	 * @param units number of increments
	 * @param centerP center point around which zoom is done in the actual image
	 */
	public void setZoom(double zoom, Point centerP)
	{
		this.fitToDestination = false;
		
		double newZoom = zoom;
		if(newZoom > maxZoom) // which means to zoom in.
		{
			newZoom = maxZoom;
		}
		if(newZoom < minZoom)
		{
			newZoom = minZoom;
		}
		if(this.zoom != newZoom || this.srcCenter.x != (int) centerP.x || this.srcCenter.y != (int) centerP.y)
		{
			this.zoom = newZoom;
			this.srcCenter.x     = (int) centerP.x;
			this.srcCenter.y     = (int) centerP.y;
			// JEXStatics.logManager.log("Zooming to image point: "+ this.srcCenter.x + "," + this.srcCenter.y, 0, this);
			imageUpdated(); // sets fitToDestination back to true if necessary via getSrcRect
		}
	}
	
	private double getNextHigherZoomLevel(double currentZoom)
	{
		if(currentZoom >= maxZoom/zoomFactor) return maxZoom;
		double newZoom = maxZoom;
		while(newZoom > currentZoom)
		{
			newZoom = newZoom/zoomFactor;
		}
		return newZoom*zoomFactor;
	}
	
	private double getNextLowerZoomLevel(double currentZoom)
	{
		double newZoom = maxZoom;
		while(newZoom >= currentZoom)
		{
			newZoom = newZoom/zoomFactor;
		}
		return newZoom;
	}
	
	public double getZoom()
	{
		return this.zoom;
	}
	
	public void setToFitToDestination()
	{
		this.fitToDestination = true;
		imageUpdated();
	}
	
	public void imageDragged(Rectangle r)
	{
		this.srcCenter.x = r.x - r.width;
		this.srcCenter.y = r.y - r.height;
		imageUpdated();
	}
	
	public void translateSrcRect(Vect v)
	{
		this.srcCenter.x = (int)(this.srcCenter.x + v.dX);
		this.srcCenter.y = (int)(this.srcCenter.y + v.dY);
		imageUpdated();
	}
	
	public void imageUpdated()
	{
		// JEXStatics.logManager.log("paint(), srcCenter: " + srcCenter, 0, this);
		// JEXStatics.logManager.log("paint(), min: " + minIntensity, 0, this);
		// JEXStatics.logManager.log("paint(), max: " + maxIntensity, 0, this);
		// JEXStatics.logManager.log("paint(), zoom: " + zoom, 0, this);
		// JEXStatics.logManager.log("Sending ImageUpdated signal", 0, this);
		SSCenter.defaultCenter().emit(this, SIG_ImageUpdated_Null, (Object[])null);
	}
	
	public Rectangle getImageRect()
	{
		return (Rectangle)this.imageRect.clone();
	}
	
	public Point getSrcCenter()
	{
		return (Point)this.srcCenter.clone();
	}
	
	/**
	 * srcCenter and zoom determine everything. srcRect is what is represented in the
	 * PixelDisplay and is calculated from srcCenter and zoom. This method is called
	 * by those who will paint this image onto a display (dstRect).
	 */
	public Rectangle getSrcRect(Rectangle dstRect){
		
		// JEXStatics.logManager.log("getSrcRect start: srcCenter = " + this.srcCenter.x + "," + this.srcCenter.y, 0, this);
		
		if(fitToDestination)
		{
			double imageAspect = ((double) imageRect.height)/((double) imageRect.width);
			double dstAspect = ((double) dstRect.height)/((double) dstRect.width);
			
			boolean heightLimited = false;
			if(dstAspect <= imageAspect)
			{
				heightLimited = true;
			}
			
			if(heightLimited)
			{
				srcRect.height = (int)((double)imageRect.height);
				srcRect.width = (int)(((double)srcRect.height) / dstAspect);
				zoom = ((double) dstRect.width)/((double) srcRect.width);
			}
			else
			{
				srcRect.width  = (int)((double)imageRect.width);
				srcRect.height = (int)(dstAspect * ((double)srcRect.width));
				zoom = ((double) dstRect.height)/((double) srcRect.height);
			}
		}
		else
		{
			srcRect.width  = (int)((double)dstRect.width / zoom);
			srcRect.height = (int)((double)dstRect.height / zoom);
		}
		
		boolean srcIsWiderThanImage = false;
		if(srcRect.width > imageRect.width) srcIsWiderThanImage = true;
		boolean srcIsTallerThanImage = false;
		if(srcRect.height > imageRect.height) srcIsTallerThanImage = true;
		
		if(forceFit && srcIsWiderThanImage && srcIsTallerThanImage)
		{
			this.fitToDestination = true;
			return this.getSrcRect(dstRect);
		}
		
		if(forceFit && srcIsWiderThanImage)
		{
			srcCenter.x = imageRect.width/2;
			srcRect.x = srcCenter.x-srcRect.width/2;
		}
		else
		{
			srcRect.x = (int) srcCenter.x - srcRect.width/2;
			if(forceFit)
			{
				// Correct if off to the right
				if (srcRect.x+srcRect.width > imageRect.width) {
					srcCenter.x = imageRect.width - srcRect.width/2;
					srcRect.x    = imageRect.width-srcRect.width;
					
				}
				// Correct if off to the left
				if (srcRect.x < 0) {
					srcRect.x = 0;
					srcCenter.x = srcRect.width/2;
				}
			}
		}
		
		if(forceFit && srcIsTallerThanImage)
		{
			srcCenter.y = imageRect.height/2;
			srcRect.y = srcCenter.y-srcRect.height/2;
		}
		else
		{
			srcRect.y = (int) srcCenter.y - srcRect.height/2;
			if(forceFit)
			{
				// Correct if off to the bottom
				if (srcRect.y+srcRect.height > imageRect.height) {
					srcCenter.y = imageRect.height - srcRect.height/2;
					srcRect.y    = imageRect.height-srcRect.height;
					
				}
				// Correct if off to the left
				if (srcRect.y < 0) {
					srcRect.y = 0;
					srcCenter.y = srcRect.height/2;
				}
			}
		}
		// JEXStatics.logManager.log("getSrcRect end: srcCenter = " + this.srcCenter.x + "," + this.srcCenter.y, 0, this);
		
		// JEXStatics.logManager.log("getSrcRect(), dstRect: " + dstRect, 0, this);
		// JEXStatics.logManager.log("getSrcRect(), srcRect: " + srcRect, 0, this);
		return (Rectangle)srcRect.clone();
	}
	
	public boolean getFitsInDestination()
	{
		return this.fitToDestination;
	}
	
	public void setForceFit(boolean forceFit)
	{
		this.forceFit = forceFit;
	}
	
	public int getPixelIntensity(Point p)
	{
		return (int) this.sourceIJ.getPixelValue(p.x, p.y);
	}
	
	public void show()
	{
		ImagePlus im = new ImagePlus("whatever", this.getImage());
		im.show();
	}
	
/////////////////////////////////////////////////////////////////////////
	//////    Utilities to pass point from display to image world     ///////
	/////////////////////////////////////////////////////////////////////////
	
	/**
	 * Return a point in the image space from a point on the screen
	 * @param p
	 * @return point
	 */
	public Point displayToImage(PixelComponentDisplay display, Point p)
	{
		Rectangle srcRect = this.getSrcRect(display.getBounds()); // 0,0 in srcRect is the upper left corner of the image
		Point ret = new Point((int) ((((double)p.x) / this.getZoom()) + srcRect.x), (int) ((((double)p.y) / this.getZoom()) + srcRect.y));
		return ret;
	}
	
	public Rectangle displayToImage(PixelComponentDisplay display, Rectangle r)
	{
		Point loc = new Point(r.x, r.y);
		Vect w_h = new Vect(r.width, r.height);
		loc = this.displayToImage(display, loc);
		w_h = this.displayToImage(display, w_h);
		Rectangle ret = new Rectangle(loc.x, loc.y, (int)w_h.dX, (int)w_h.dY);
		return ret;
	}
	
	/**
	 * Return a point in the display space from a point on the image
	 * @param p
	 * @return
	 */
	public PointList displayToImage(PixelComponentDisplay display, PointList pl){
		PointList ret = new PointList();
		for(Point p : pl)
		{
			ret.add(this.displayToImage(display, p));
		}
		return ret;
	}
	
	/**
	 * Return a vector in the image space from the display space
	 * @param v
	 * @return
	 */
	public Vect displayToImage(PixelComponentDisplay display, Vect v){
		Vect result = v.duplicate();
		result.multiply(1/this.getZoom());
		return result;
	}
	
	/**
	 * Return a point in the display space from a point on the image
	 * @param p
	 * @return
	 */
	public Point imageToDisplay(PixelComponentDisplay display, Point p)
	{
		Rectangle srcRect = this.getSrcRect(display.getBounds()); // 0,0 in srcRect is the upper left corner of the image
		return new Point((int) (((double)p.x - srcRect.x) * this.getZoom()), (int) (((double)p.y - srcRect.y) * this.getZoom()));
	}
	
	public Rectangle imageToDisplay(PixelComponentDisplay display, Rectangle r)
	{
		Point loc = new Point(r.x, r.y);
		Vect w_h = new Vect(r.width, r.height);
		loc = this.imageToDisplay(display, loc);
		w_h = this.imageToDisplay(display, w_h);
		return new Rectangle(loc.x, loc.y, (int)w_h.dX, (int)w_h.dY);
	}
	
	/**
	 * Return a vector in display space from the image space
	 * @param v
	 * @return
	 */
	public Vect imageToDisplay(PixelComponentDisplay display, Vect v){
		Vect result = v.duplicate();
		result.multiply(this.getZoom());
		return result;
	}
	
	/**
	 * Return a point in the display space from a point on the image
	 * @param p
	 * @return
	 */
	public PointList imageToDisplay(PixelComponentDisplay display, PointList pl){
		PointList ret = new PointList();
		for(Point p : pl)
		{
			ret.add(this.imageToDisplay(display, p));
		}
		return ret;
	}
	
	public static Rectangle intersection(Rectangle r1, Rectangle r2)
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
	
}
