package Database.DataWriter;

import image.roi.ROIPlus;

import java.util.HashMap;
import java.util.Map;

import Database.DBObjects.JEXData;
import Database.DBObjects.JEXDataSingle;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.DimensionMap;

public class RoiWriter {

	/**
	 * Make a simple, zero dimensional ROI data object
	 */
	public static JEXData makeRoiObject(String objectName, ROIPlus roi){
		JEXData result = new JEXData(JEXData.ROI,objectName);
		result.put(JEXEntry.INFO, "No info inputed");
		result.put(JEXEntry.DATE, utilities.DateUtility.getDate());
		result.put(JEXEntry.MODIFDATE, utilities.DateUtility.getDate());

		String value = roi.getPointList().pointListString();
		String pattern = roi.getPattern().pointListString();
		JEXDataSingle ds = new JEXDataSingle();
		ds.put(JEXDataSingle.ROITYPE,""+roi.type);
		ds.put(JEXDataSingle.POINTLIST,value);
		ds.put(JEXDataSingle.PATTERN,pattern);
		
		DimensionMap map = new DimensionMap();
		result.addData(map, ds);
		result.put("roiType",""+roi.type);
		
		return result;
	}
	
	/**
	 * Make a one dimensional ROI object with a list of Rois at indexes given by 
	 * the string array INDEXES for a dimension name DIMENSIONNAME
	 * @param objectName
	 * @param rois
	 * @param indexes
	 * @param dimensionName
	 * @return
	 */
	public static JEXData makeRoiObject(String objectName, ROIPlus[] rois, String[] indexes, String dimensionName){
		HashMap<DimensionMap,ROIPlus> datamap = new HashMap<DimensionMap,ROIPlus>();
		for (int index=0; index<rois.length; index++){
			ROIPlus roi     = rois[index];
			String indValue = indexes[index];
			DimensionMap map = new DimensionMap();
			map.put(dimensionName, indValue);
			datamap.put(map, roi);
		}
		
		JEXData result = makeRoiObject(objectName,datamap);
		return result;
	}
	
	/**
	 * Make a one dimensional ROI object with a list of Rois at integer indexes 
	 * for a dimension name DIMENSIONNAME
	 * @param objectName
	 * @param rois
	 * @param dimensionName
	 * @return
	 */
	public static JEXData makeRoiObject(String objectName, ROIPlus[] rois, String dimensionName){
		HashMap<DimensionMap,ROIPlus> datamap = new HashMap<DimensionMap,ROIPlus>();
		for (int index=0; index<rois.length; index++){
			ROIPlus roi = rois[index];
			DimensionMap map = new DimensionMap();
			map.put(dimensionName, ""+index);
			datamap.put(map, roi);
		}
		
		JEXData result = makeRoiObject(objectName,datamap);
		return result;
	}

	/**
	 * Make a one dimensional ROI object with a list of Rois at integer indexes 
	 * for a dimension name DIMENSIONNAME
	 * @param objectName
	 * @param roi
	 * @param dimensionName
	 * @return
	 */
	public static JEXData makeRoiObject(String objectName, Map<DimensionMap,ROIPlus> rois){
		JEXData result = new JEXData(JEXData.ROI,objectName);
		result.put(JEXEntry.INFO, "No info inputed");
		result.put(JEXEntry.DATE, utilities.DateUtility.getDate());
		result.put(JEXEntry.MODIFDATE, utilities.DateUtility.getDate());
		
		for (DimensionMap map: rois.keySet()){
			ROIPlus roi  = rois.get(map);
			String value = roi.getPointList().pointListString();
			String pattern = roi.getPattern().pointListString();
			JEXDataSingle ds = new JEXDataSingle();
			ds.put(JEXDataSingle.ROITYPE,""+roi.type);
			ds.put(JEXDataSingle.POINTLIST,value);
			ds.put(JEXDataSingle.PATTERN,pattern);
			
			result.addData(map, ds);
			result.put("roiType",""+roi.type);
		}
		return result;
	}
	
}
