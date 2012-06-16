package image.roi;

import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import jex.statics.JEXStatics;
import utilities.Pair;

public class HashedPointList implements Comparator<Pair<IdPoint,IdPoint>>{
	
	TreeMap<Integer,PointList> xHash = new TreeMap<Integer,PointList>();
	TreeMap<Integer,PointList> yHash = new TreeMap<Integer,PointList>();
	PointList points;
	
	public HashedPointList()
	{
		this.points = new PointList();
	}
	
	public HashedPointList(PointList points)
	{
		this.points = points;
		PointList hashedPoints;
		for(IdPoint p : points)
		{
			// Add to xHash
			hashedPoints = this.xHash.get(p.x);
			if(hashedPoints == null)
			{
				hashedPoints = new PointList();
			}
			hashedPoints.add(p);
			this.xHash.put(p.x, hashedPoints);
			
			// Add to yHash
			hashedPoints = this.yHash.get(p.y);
			if(hashedPoints == null)
			{
				hashedPoints = new PointList();
			}
			hashedPoints.add(p);
			this.yHash.put(p.y, hashedPoints);
		}
	}
	
	/**
	 * radius is actually a square region extending radius pixels
	 * around the specified point. Returns the nearest point
	 * within that radius.
	 * @param p (point around which to search)
	 * @param radius (radius of region to search)
	 * @param squareRegion (search in a square-shaped region or a circular region)
	 * @return nearest point in that range (ties are broken by which is found first)
	 */
	public IdPoint getNearestInRange(Point p, double radius, boolean squareRegion)
	{
		PointList xMatches = new PointList();
		PointList yMatches = new PointList();
		PointList matches;
		
		// Get the points with the x point in the correct range
		for(int i = 0; i <= radius; i++)
		{
			//System.out.println(i);
			matches = this.xHash.get(p.x+i);
			if(matches != null) xMatches.addAll(matches);
			if(i > 0)
			{
				matches = this.xHash.get(p.x-i);
				if(matches != null) xMatches.addAll(matches);
			}
		}
		if(xMatches.size() == 0)
		{
			return null;
		}
		
		// Get the points with the y point in the correct range
		for(int i = 0; i <= radius; i++)
		{
			matches = this.yHash.get(p.y+i);
			if(matches != null) yMatches.addAll(matches);
			if(i > 0)
			{
				matches = this.yHash.get(p.y-i);
				if(matches != null) yMatches.addAll(matches);
			}
		}
		if(yMatches.size() == 0)
		{
			return null;
		}
		
		// Perform the intersection of the match lists
		xMatches.retainAll(yMatches);
		if(xMatches.size() == 0)
		{
			return null;
		}
		if(xMatches.size() == 1)
		{
			return xMatches.get(0);
		}
		
		// Calculate the 'distance measure' from p for each remaining point
		double[] distance = new double[xMatches.size()];
		for(int i = 0; i < distance.length; i++)
		{
			distance[i] = Math.pow((xMatches.get(i).x-p.x),2) + Math.pow((xMatches.get(i).y-p.y),2);
		}
		
		// Find the min distance and return a new point with those coordinates
		// Set the minimum radius to be that of the corners of the searched square region
		// or the circle that fits within that square
		double min=radius*radius; 
		if(squareRegion)
		{
			min = min * Math.sqrt(2);
		}
		for(int i = 0; i < distance.length; i++)
		{
			min = Math.min(min,distance[i]);
		}
		for(int i = 0; i < distance.length; i++)
		{
			if(distance[i] == min)
			{
				return xMatches.get(i);
			}
		}
		//JEXStatics.logManager.log("No nearest point found", 0, this);
		return null;
	}
	
	public static List<Pair<IdPoint,IdPoint>> getNearestNeighbors(PointList l1, PointList l2, double radius, boolean squareRegion)
	{
		List<Pair<IdPoint,IdPoint>> ret = new Vector<Pair<IdPoint,IdPoint>>();
		
		HashedPointList sl2 = new HashedPointList(l2);
		for(IdPoint p : l1)
		{
			IdPoint nearest = sl2.getNearestInRange(p, radius, squareRegion);
			ret.add(new Pair<IdPoint,IdPoint>(p, nearest));
		}
		JEXStatics.logManager.log("getNearestNeighbors returned " + ret.size() + " pairs for a list of " + l1.size() + " points, which is a difference of " + (ret.size() - l1.size()), 0, HashedPointList.class);
		return ret;
	}
	
	public static List<Pair<IdPoint,IdPoint>> filterConflicts(List<Pair<IdPoint,IdPoint>> pairs)
	{
		// Sort the pairs list into ascending order of distance between the points within the pair
		Collections.sort(pairs, new HashedPointList());
		
		// Take the first occurrence of each 
		List<Pair<IdPoint,IdPoint>> resolved = new Vector<Pair<IdPoint,IdPoint>>();
		for(Pair<IdPoint,IdPoint> pair : pairs)
		{
			// If this pair connects a point p1 with a point p2 that is already connected to a different point p1
			// (i.e., already exists in the resolved list), eliminate the connection from p1 to p2
			if(hasConflict(resolved, pair))
			{
				pair.p2 = null; // Eliminate the conflicting connection with p2 that already exists in a different pair
				resolved.add(pair);
			}
			else
			{
				resolved.add(pair);
			}
		}
		
		// Resort resolved list before returning
		Collections.sort(resolved, new HashedPointList());
		
		return resolved;
	}
	
	private static boolean hasConflict(List<Pair<IdPoint,IdPoint>> pairs, Pair<IdPoint,IdPoint> p)
	{
		if(p.p2 == null)
		{
			return false;
		}
		for(Pair<IdPoint,IdPoint> pair : pairs)
		{
			if(pair.p2 == p.p2)
			{
				return true;
			}
		}
		return false;
	}
	
	public static double distance(Pair<IdPoint,IdPoint> p)
	{
		if(p.size() < 2 || p.p1 == null || p.p2 == null) return Double.MAX_VALUE;
		double ret = ((Point) p.p1).distance((Point) p.p2);
		return ret;
	}
	
	public boolean remove(Point p)
	{
		PointList xMatches = this.xHash.get(p.x);
		PointList yMatches = this.yHash.get(p.y);
		return (this.points.remove(p) && xMatches != null && xMatches.remove(p) && yMatches != null && yMatches.remove(p));
	}

	public int compare(Pair<IdPoint,IdPoint> pair1, Pair<IdPoint,IdPoint> pair2)
	{
		return (int) Math.signum(distance(pair1) - distance(pair2));
	}

}
