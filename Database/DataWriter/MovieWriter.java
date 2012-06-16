package Database.DataWriter;

import Database.DBObjects.JEXData;
import Database.DBObjects.JEXDataSingle;
import Database.DBObjects.dimension.DimensionMap;

public class MovieWriter {

	/**
	 * Make a movie data object with a single movie inside
	 * @param objectName
	 * @param filePath
	 * @return data
	 */
	public static JEXData makeMovieObject(String objectName, String filePath){
		JEXData data = new JEXData(JEXData.MOVIE,objectName);
		JEXDataSingle ds = FileWriter.saveFileDataSingle(filePath);
		data.addData(new DimensionMap(), ds);
		return data;
	}
	
}
