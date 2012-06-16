package function.imageUtility;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import image.roi.PointList;
import image.roi.ROIPlus;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;

public class ParticleAnalyzer extends ij.plugin.filter.ParticleAnalyzer{
	
	HashMap<ROIPlus,ImageStatistics> results = new HashMap<ROIPlus,ImageStatistics>();
	ResultsTable rt = new ResultsTable();
	ImageProcessor redirectIp;
	
	int measurements;
	
	public static final int DEFAULT_MEASUREMENTS = ImageStatistics.CENTER_OF_MASS | ImageStatistics.AREA | ImageStatistics.MEAN;
	
	public ParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize, double minCirc, double maxCirc)
	{
		super(options, measurements, rt, minSize, maxSize, minCirc, maxCirc);
		this.measurements = measurements;
	}
	
	public HashMap<ROIPlus,ImageStatistics> getMeasurements()
	{
		return results;
	}
	
	public ImageProcessor getOutlineImage()
	{
		ImageProcessor ret = redirectIp.duplicate();
		ret.convertToRGB();
		ret.setColor(Color.YELLOW);
		int i = 1;
		String label;
		Point center;
		for(ROIPlus roip : results.keySet())
		{
			label = ""+i;
			PointList points = roip.getPointList();
			points.remove(points.size()-1);
			ret.drawPolygon(points.toPolygon());
			center = PointList.getCenter(points.getBounds());
			ret.drawString(label, center.x, center.y);
			i++;
		}
		return ret;
	}
	
//	private void setResultsTable(ResultsTable rt)
//	{
//		this.rt = rt;
//	}
	
//	public ResultsTable getResultsTable()
//	{
//		rt.show("Particle Analysis");
//		return this.rt;
//	}
	
	public static ParticleAnalyzer analyzeParticles(ByteProcessor blackAndWhite, boolean particlesAreWhite, boolean excludeOnEdges, boolean fillHoles, double minSize, double maxSize, double minCirc, double maxCirc, int measurements)
	{
		return analyzeParticles(null, blackAndWhite, particlesAreWhite, excludeOnEdges, fillHoles, minSize, maxSize, minCirc, maxCirc, measurements);
	}
	
	public static ParticleAnalyzer analyzeParticles(ImageProcessor grayScale, ByteProcessor blackAndWhite, boolean particlesAreWhite, boolean excludeOnEdges, boolean fillHoles, double minSize, double maxSize, double minCirc, double maxCirc, int measurements)
	{
		
		int options = 0;
		ByteProcessor bw2 = (ByteProcessor)blackAndWhite.duplicate();
		if(excludeOnEdges)
		{
			options |= EXCLUDE_EDGE_PARTICLES;
		}
		if(!fillHoles)
		{
			options |= INCLUDE_HOLES;
		}
		if(particlesAreWhite)
		{
			bw2.invert();
		}
		
		ResultsTable rt = new ResultsTable();
		function.imageUtility.ParticleAnalyzer ret = new function.imageUtility.ParticleAnalyzer(options, measurements, rt, minSize, maxSize, minCirc, maxCirc);
		if(grayScale == null)
		{
			ret.redirectIp = bw2;
		}
		else
		{
			ret.redirectIp = grayScale;
		}
		if(!ret.analyze(new ImagePlus("Dummy",bw2), bw2))
		{
			return null;
		}
		return ret;
	}
	
	
	
	@Override
	public void saveResults(ImageStatistics stats, Roi roi)
	{
		ROIPlus roip = new ROIPlus(roi);
		redirectIp.setRoi(roi);
		stats = ImageStatistics.getStatistics(redirectIp, this.measurements, null);
		this.results.put(roip, stats);
		//super.saveResults(stats, roi);
	}

}
