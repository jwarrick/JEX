package Database.SingleUserDatabase;


public class JEXReader {

//	/**
//	 * Finds the list of database contained in a repository
//	 * @param rep
//	 * @return
//	 */
//	public static JEXDatabaseInfo[] findDatabasesInRepository(Repository rep)
//	{
//		List<JEXDatabaseInfo> resultList = new ArrayList<JEXDatabaseInfo>(0);
//
//		// Test the path return null if it's not a possible path
//		File toLoad = new File(rep.getPath());
//		if (!toLoad.exists()){return new JEXDatabaseInfo[0];}
//		if (!toLoad.isDirectory()){return new JEXDatabaseInfo[0];}
//
//		// Create the result list and loop through the folders
//		File[] subFiles = toLoad.listFiles();
//		if (subFiles == null) return new JEXDatabaseInfo[0];
//
//		for (int i=0; i<subFiles.length; i++) {
//			File f = subFiles[i];
//
//			// If the file is not a folder then continue
//			if (!f.isDirectory() || f.getName().startsWith(".")) continue;
//
//			// Loop through the files in that folder
//			File[] subsubFiles = f.listFiles();
//			
//			// Set a flag to see if a repository was found
//			boolean repositoryFound = false;
//			
//			// Loop thorugh the files a first time to find all up-to-date databases
//			for (int j=0; j<subsubFiles.length; j++) {
//				File f2 = subsubFiles[j];
//
//				// If the sub-folder contains a file named Database.xml it's likely a database
//				// So load it into the list
//				if (f2.getName().equals(JEXDatabaseInfo.LOCAL_DBINFO_CURRENTVERSION))
//				{
//					// Create a new database wrap
//					JEXDatabaseInfo dbInfo = new JEXDatabaseInfo(f2.getAbsolutePath());
//					resultList.add(dbInfo);
//
//					// move on to the next folder
//					repositoryFound = true;
//				}
//			}
//
//			// If none were found, loop through to look for old ones
//			if (repositoryFound) continue;
//			for (int j=0; j<subsubFiles.length; j++) {
//				File f2 = subsubFiles[j];
//				
//				if (JEXDatabaseInfo.LOCAL_DBINFO_OLDVERSIONS.contains(f2.getName()))
//				{	
//					// Load the old info file into a new XPreferences
//					String infoPath   = rep.getPath() + File.separator + f.getName() + File.separator + "JEX3Database_info.txt";
//					XPreferences infoPrefs = XPreferences_Utilities.updateFromVersion3(infoPath);
//
//					// Save into the new info file path
//					infoPath = rep.getPath() + File.separator + f.getName() + File.separator + JEXDatabaseInfo.LOCAL_DBINFO_CURRENTVERSION;
//					infoPrefs.setPath(infoPath);
//					infoPrefs.save();
//
//					// Create a new database wrap
//					JEXDatabaseInfo dbInfo = new JEXDatabaseInfo(infoPath);
//					resultList.add(dbInfo);
//
//					// move on to the next folder
//					break;
//				}
//			}
//		}
//		JEXStatics.logManager.log("Scanned "+subFiles.length+" files and found "+resultList.size()+" databases", 1, null);
//
//		JEXDatabaseInfo[] result = new JEXDatabaseInfo[resultList.size()];
//		for (int index=0; index<resultList.size(); index++){
//			result[index] = resultList.get(index);
//		}
//		return result;
//	}
	
//	/**
//	 * Load the database and return it
//	 * @param dbInfo
//	 * @return
//	 */
//	public static JEXLocalDatabase load(JEXDatabaseInfo dbInfo)
//	{
//		// Get the database type
//		String databaseType = dbInfo.getType();
//		
//		// Match the type with the possible types
//		if (databaseType.equals(JEXLocalDatabase.LOCAL_DATABASE))
//		{
//			JEXStatics.logManager.log("Loading the local database "+dbInfo.getName(), 0, null);
//			
//			// Create a core
//			JEXLocalDatabaseCore dbIO = new JEXLocalDatabaseCore(dbInfo);
//			
//			// Load it
//			int done = dbIO.loadCurrentDatabase();
//			
//			// Test the loading condition
//			if (done == JEXLocalDatabaseCore.LOAD_FAIL) 
//			{
//				JEXStatics.logManager.log("!!! Database loading failed !!!", 0, null);
//				return null;
//			}
//			
//			// Make a databse interface
//			JEXLocalDatabase db = new JEXLocalDatabase(dbIO);
//			
//			// return the database
//			return db;
//		}
//		else if (databaseType.equals(JEXLocalDatabase.REMOTE_DATABASE))
//		{
//	
//		}
//		else if (databaseType.equals(JEXLocalDatabase.REMOTE_WITH_IP_DATABASE))
//		{
//			
//		}
//		
//		return null;
//	}


}
