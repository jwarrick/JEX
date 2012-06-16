package image.roi;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jex.statics.JEXStatics;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import utilities.StopWatch;
import utilities.DateUtility;
import Database.SingleUserDatabase.xml.ObjectFactory;
import Database.SingleUserDatabase.xml.XData;
import Database.SingleUserDatabase.xml.XDataSingle;


public class XTrajectorySet extends XData{
	private static final long serialVersionUID = 1L;
	private List<Trajectory> trajectories;

	public XTrajectorySet(){
		super("XTrajectorySet");

		this.addMeta("Name", "Trajectories");
		this.addMeta("Type", "XTrajectorySet");
		this.addMeta("Info", "None yet");
		this.addMeta("date", DateUtility.getDate());
		this.addMeta("Dimensions", "");
		trajectories = new java.util.ArrayList<Trajectory>(0);
	}

	public XTrajectorySet(List<Trajectory> trajectories){
		this();
		this.addTrajectories(trajectories);
	}
	
	public void addTrajectory(Trajectory trajectory){
		Trajectory result = new Trajectory();
		result.addMeta("Number of Points", ""+trajectory.pmapList().length());
		result.addMeta("CellRadius", ""+trajectory.getCellRadius());
		result.addMeta("InterpolationWindow", ""+trajectory.getInterpolationWindow());
		result.addMeta("MaxDispTime", ""+trajectory.getMaxDissapearanceTime());
		result.addMeta("Points", trajectory.pmapList());
		result.load();
		trajectories.add(result);
		this.addDataSingle(result);
		
//		trajectories.add(trajectory);
//		trajectory.update();
//		this.addDataSingle(trajectory);
	}

	public void addTrajectories(List<Trajectory> trajectories){
		this.trajectories = trajectories;
		for (Trajectory traj: trajectories){
			traj.update();
			this.addDataSingle(traj);
		}
	}
	
	public List<Trajectory> getTrajectories(){
		return trajectories;
	}
	
	public static XTrajectorySet getTrajectoriesFromPath(String path){
		JEXStatics.logManager.log("Parsing the trajectory xml string "+path, 0, null);
		StopWatch stopwatch = new StopWatch();
	    stopwatch.start();
	    
		SAXBuilder sb = new SAXBuilder();
		sb.setFactory(new ObjectFactory());
		Document resturnDoc = null;
		try {
			resturnDoc = sb.build(new File(path));
		}
		catch (JDOMException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		stopwatch.stop();
		JEXStatics.logManager.log("--------------------------------------",1,null);
		JEXStatics.logManager.log("Factory TIME: "+stopwatch.toString(),1,null);
		JEXStatics.logManager.log("--------------------------------------",1,null);
		
		if (!(resturnDoc.getRootElement() instanceof XData)){
			return new XTrajectorySet();
		}
		XData traj = (XData) resturnDoc.getRootElement();
		
//		String info = traj.getMetaValue("Info");
//		String date = traj.getMetaValue("date");
		List<XDataSingle> trajList = traj.getSingleDataElements();
		
		XTrajectorySet result = new XTrajectorySet();
		for (XDataSingle trajds: trajList){
			Trajectory trajectory = Trajectory.fromXDataSingle(trajds);
			result.trajectories.add(trajectory);
			result.addDataSingle(trajectory);
//			result.addTrajectory(trajectory);
		}

		
		return result;
	}
	
	
	
	
}
