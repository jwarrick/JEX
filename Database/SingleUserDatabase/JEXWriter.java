package Database.SingleUserDatabase;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import jex.statics.JEXStatics;

import org.apache.commons.io.FileUtils;

import preferences.XPreferences;
import utilities.DateUtility;
import utilities.FileUtility;
import utilities.StringUtility;
import utilities.XMLUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;

public class JEXWriter {
	
	// File system statics
	public static String ATTACHEDFILES = "Attached Files";
	public static String NOTES         = "Note.rtf"; 
	public static String TEMP_FOLDER_PATH = "temp";
	public static String CORE_TEMP_NAME   = "JEXData";
	private static int fileCounter = 0;
	

	// ---------------------------------------------
	// Saving methods
	// ---------------------------------------------
	
	public static void saveBDInfo(JEXDBInfo dbInfo)
	{
		// Get the file location to save the db info file
		String path = dbInfo.getAbsolutePath();
		File   f    = new File(path);
		
		// Create an archive path
		String dbFileName  = FileUtility.getNextName(f.getParent(), JEXDBInfo.LOCAL_DBINFO_CURRENTVERSION, "Archive");
		File   newf        = new File(f.getParent() + File.separator + dbFileName);
		
		// If an old one exists, create an archive
		if (f.exists())
		{
			FileUtility.copy(f, newf);
		}
		
		// Save the info file	
		dbInfo.set(JEXDBInfo.DB_MDATE, DateUtility.getDate());
		dbInfo.getXML().saveToPath(dbInfo.getAbsolutePath());
	}
	
	public static void editDBInfo(JEXDBInfo dbInfo, String name, String info, String password)
	{
		if(name == null || name.equals(""))
		{
			return;
		}
		
		// Archive the current dbInfo
		JEXWriter.saveBDInfo(dbInfo);
		
		File currentDBDirectory = new File(dbInfo.getDirectory());	
		String currentDBDirectoryParent = currentDBDirectory.getParent();
		try
		{
			// Check the new name and change the directory name if necessary
			name = FileUtility.removeWhiteSpaceOnEnds(name);
			File newDBDirectory = new File(currentDBDirectoryParent + File.separator + name);
			if(!name.equals(dbInfo.getName()))
			{
				// If the name changed then the folder of the database requires a name change
				// This method renames the folder.
				// We can do this and not affect DB behavior as long as we change the database name and
				// classy path (jexpath) in the dbInfo object held by JEXManager
				// because all file paths are relative to dbInfo.getDirectory(), which references jexPath
				FileUtils.moveDirectory(currentDBDirectory, newDBDirectory);
			}
			
			// Change the information in dbInfo
			dbInfo.set(JEXDBInfo.DB_NAME,name);
			dbInfo.set(JEXDBInfo.DB_INFO, info);
			dbInfo.set(JEXDBInfo.DB_PWD, password);
			
			// Save the new one
			dbInfo.getXML().saveToPath(newDBDirectory.getAbsolutePath());
			
			// Reload dbInfo from new path
			dbInfo.setPath(newDBDirectory.getAbsolutePath());
		} 
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
	}
	
	public static void cleanDB(JEXDBInfo dbInfo)
	{
		
	}
	
	
	// ---------------------------------------------
	// Temporary saving methods
	// ---------------------------------------------
	
	/**
	 * Save the image in the temporary database folder
	 */
	public static String saveImage(ImagePlus im)
	{
		// Create the file path
		String fullPath   = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath("tif");
		
		// Save the image
		FileSaver imFS = new FileSaver(im);
		imFS.saveAsTiff(fullPath);
		JEXStatics.logManager.log("Saving image to: " + fullPath, 1, "JEXWriter");
		return fullPath;
	}

	/**
	 * Save the image in the temporary database folder
	 */
	public static String saveImage(ImageProcessor imp)
	{
		return saveImage(new ImagePlus("",imp));
	}
	
	/**
	 * Save figure in using type defined by extension (jpg, tif, or png)
	 * @param bImage
	 * @param extension
	 */
	public static String saveFigure(BufferedImage bImage, String extension)
	{
		return saveFigure(new ImagePlus("figure",bImage), extension);
	}
	
	/**
	 * Save figure in using type defined by extension (jpg, tif, or png)
	 * @param im
	 * @param extension
	 */
	public static String saveFigure(ImagePlus im, String extension)
	{
		String fullPath   = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(extension);
		
		JEXStatics.logManager.log("Saving figure to: " + fullPath, 1, null);
		
		FileSaver fs = new FileSaver(im);
		if (extension.equals("jpg"))
		{
			fs.saveAsJpeg(fullPath);
		}
		else if (extension.equals("tif")) 
		{
			fs.saveAsTiff(fullPath);
		}
		else if (extension.equals("png")) 
		{
			fs.saveAsPng(fullPath);
		}
		else
		{
			JEXStatics.logManager.log("Extension not supported: " + extension + ", must be jpg, tif, or png.", 0, null);
			return null;
		}
			
		return fullPath;
	}

	/**
	 * Save the image in the temporary database folder
	 */
	public static String saveText(String text, String extension)
	{
		// Create the file path
		String fullPath   = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(extension);
		
		// Save the text
		printToFile(text, fullPath);
		
		return fullPath;
	}
	
	/**
	 * Copy a file to the temp folder
	 */
	public static String saveFile(File f)
	{
		// Create the file path
		String extension;
		try
		{
			extension = utilities.FileUtility.getFileNameExtension(f.getCanonicalPath());
			String fullPath = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(extension);
			
			// Save the image
			File dst = new File(fullPath);
			utilities.FileUtility.copy(f, dst);
			return fullPath;
		} 
		catch (IOException e)
		{
			JEXStatics.logManager.log("Couldn't copy file! Returning null.", 0, "JEXWriter");
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	// ---------------------------------------------
	// DB creation methods
	// ---------------------------------------------
	
	/**
	 * Create a new local database
	 * @param name
	 * @param info
	 * @param password
	 * @return
	 */
	public static JEXDBInfo createDBInfo(Repository rep, String name, String info, String password){
		// Make database folder
		File folder = new File(rep.getPath() + File.separator + name) ;
		if (folder.exists()) {
			JEXStatics.statusBar.setStatusText("DB creation impossible, folder exists...");
			return null;
		}
		else {
			folder.mkdirs();
		}
		
		XPreferences dbInfo = new XPreferences();	
	
		dbInfo.put(JEXDBInfo.DB_NAME, name);
		dbInfo.put(JEXDBInfo.DB_INFO, info);
		dbInfo.put(JEXDBInfo.DB_AUTHOR, JEXStatics.jexManager.getUserName());
		dbInfo.put(JEXDBInfo.DB_DATE, utilities.DateUtility.getDate());
		dbInfo.put(JEXDBInfo.DB_MDATE, utilities.DateUtility.getDate());
		dbInfo.put(JEXDBInfo.DB_VERSION, JEXDBIO.VERSION);
		dbInfo.put(JEXDBInfo.DB_PWD, password);
		dbInfo.put(JEXDBInfo.DB_PWDREQUIRED, "false");
		dbInfo.put(JEXDBInfo.DB_TYPE, JEXDB.LOCAL_DATABASE);
		
		String path = folder.getAbsolutePath() + File.separator + JEXDBInfo.LOCAL_DBINFO_CURRENTVERSION;
		dbInfo.saveToPath(path);
		
		JEXDBInfo ret = new JEXDBInfo(path);
		return ret;
	}
	
	// ---------------------------------------------
	// Path methods
	// ---------------------------------------------
	
	/**
	 * Returns the database folder, ie the root of the file structure
	 * @return
	 */
	public static String getDatabaseFolder()
	{
		JEXDBInfo dbInfo = JEXStatics.jexManager.getDatabaseInfo();
		return getDatabaseFolder(dbInfo);
	}
		
	/**
	 * Returns the database folder, ie the root of the file structure
	 * @return
	 */
	public static String getDatabaseFolder(JEXDBInfo dbInfo)
	{
		String f = dbInfo.getDirectory();
		return f;
	}
	
	/**
	 * Returns the folder path relative to the database containing all the data and attached files to the experiment of entry
	 * @param entry
	 * @return
	 */
	public static String getExperimentFolder(JEXEntry entry, boolean loadPath, boolean relative)
	{
		String expName;
		if(loadPath && entry.loadTimeExperimentName != null)
		{
			expName = FileUtility.removeWhiteSpaceOnEnds(entry.loadTimeExperimentName);
		}
		else
		{
			expName = FileUtility.removeWhiteSpaceOnEnds(entry.getEntryExperiment());
		}
		// Create it if the folder doesn't exist
		File f = new File(JEXWriter.getDatabaseFolder() + File.separator + expName);
		if (!f.exists()) f.mkdirs();
		
		if(relative)
		{
			return expName;
		}
		else
		{
			return f.getAbsolutePath();
		}
	}
	
	/**
	 * Returns the folder path relative to the database containing all the data and attached files to the array of entry
	 * @param entry
	 * @return
	 */
	public static String getArrayFolder(JEXEntry entry, boolean loadPath, boolean relative)
	{
		String arrayName;
		if(loadPath && entry.loadTimeTrayName != null)
		{
			arrayName = FileUtility.removeWhiteSpaceOnEnds(entry.loadTimeTrayName);
		}
		else
		{
			arrayName = FileUtility.removeWhiteSpaceOnEnds(entry.getEntryTrayName());
		}
		// Create it if the folder doesn't exist
		File f        = new File(JEXWriter.getExperimentFolder(entry, loadPath, false) + File.separator + arrayName);
		if (!f.exists()) f.mkdirs();
		
		return JEXWriter.getExperimentFolder(entry, loadPath, relative) + File.separator + arrayName;
	}
	
	/**
	 * Returns the folder path relative to the database containing all the data and attached files to the entry of entry
	 * @param entry
	 * @return
	 */
	public static String getEntryFolder(JEXEntry entry, boolean loadPath, boolean relative)
	{
		String entryFolderName = "Cell_x" + entry.getTrayX() + "_y" + entry.getTrayY();
		
		// Create it if the folder doesn't exist
		File f = new File(JEXWriter.getArrayFolder(entry, loadPath, false) + File.separator + entryFolderName);
		if (!f.exists()) f.mkdirs();
		
		return JEXWriter.getArrayFolder(entry, loadPath, relative) + File.separator + entryFolderName;
	}
	
	/**
	 * Returns the folder path relative to the database containing all the data and attached files to the entry of entry
	 * @param entry
	 * @return
	 */
	public static String getDataFolder(JEXData data, boolean relative)
	{
		String dataFolderName = FileUtility.removeWhiteSpaceOnEnds(data.getTypeName().toString());
		
		// Create it if the folder doesn't exist
		File f = new File(JEXWriter.getEntryFolder(data.getParent(), false, false) + File.separator + dataFolderName);
		if (!f.exists()) f.mkdirs();
		
		return JEXWriter.getEntryFolder(data.getParent(), false, relative) + File.separator + dataFolderName;
	}

	/**
	 * Return the path to the attached files for an entry
	 * The hierarchylevel sets the scope of the folder returned
	 * it can take the values of Experiment, Tray or nothing for hte whole DB
	 */
	public static String getAttachedFolderPath(JEXEntry entry, String hierarchyLevel, boolean loadPath, boolean relative)
	{
		String result = null;
		
		if (hierarchyLevel.equals(""))
		{
			if(relative)
			{
				result = ATTACHEDFILES;
			}
			else
			{
				result = getDatabaseFolder() + File.separator + ATTACHEDFILES;
			}
		}
		else if (hierarchyLevel.equals(JEXEntry.EXPERIMENT)){
			result = getExperimentFolder(entry, loadPath, relative) + File.separator + ATTACHEDFILES;
		}
		else if (hierarchyLevel.equals(JEXEntry.TRAY)){
			result = getArrayFolder(entry, loadPath, relative) + File.separator + ATTACHEDFILES;
		}
		
		// Create it if the folder doesn't exist
		File f;
		if(relative)
		{
			f = new File(JEXWriter.getDatabaseFolder() + File.separator + result);
		}
		else
		{
			f = new File(result);
		}
		
		if (!f.exists()) f.mkdirs();
		
		return result;
	}
	
	/**
	 * Return the path to the attached notes file for a specific entry
	 * at a specific hierarchy level
	 */
	public static String getAttachedNotesPath(JEXEntry entry, String hierarchyLevel, boolean loadPath, boolean relative)
	{
		String result = getAttachedFolderPath(entry,hierarchyLevel,loadPath,relative);
		
		if (result == null)
		{
			return null;
		}
		
		result = result + File.separator + NOTES;
		
		// Create it if the folder doesn't exist
		File f;
		if(relative)
		{
			f = new File(JEXWriter.getDatabaseFolder() + File.separator + result);
		}
		else
		{
			f = new File(result);
		}
		if (!f.exists()) XMLUtility.XMLsave(result, "");
		
		return result;
	}
	
	/**
	 * Get the path of the temporary folder to save data that is not yet attached to the database
	 * @return
	 */
	public static String getTempFolderName()
	{	
		return TEMP_FOLDER_PATH;
	}
	
	/**
	 * Return a unique name in the temporary folder
	 * @return
	 */
	public synchronized static String getUniqueRelativeTempPath(String extension)
	{
		// Create the file path
		String tempFolder = JEXWriter.getTempFolderName();		
		String tempName = getAvailableTempName(CORE_TEMP_NAME, 10, extension);
		String relativePath   = tempFolder + File.separator + tempName;
		return relativePath;
	}
	
	/**
	 * Copy the fragmented file to a new temp location in the temp/defrag folder and return the fullpath.
	 * Things are put in temp/defrag in case the src is from the temp folder.
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static String defrag(String srcPath) throws IOException
    {
    	File dst = new File(JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(FileUtility.getFileNameExtension(srcPath)));
    	File src = new File(srcPath);
    	if(FileUtility.copy(src, dst, true))
    	{
    		src.delete();
    		return dst.getPath();
    	}
    	JEXStatics.logManager.log("Couldn't defrag the file " + srcPath, 0, "JEXWriter");
    	return srcPath;
    }
	
	/**
	 * Get the next free file name based on the core name, a suffix and a selected extension
	 * @param path
	 * @param coreName
	 * @param suffix
	 * @param extension
	 * @return
	 */
	private synchronized static String getAvailableTempName(String coreName, int suffixNumberLength, String extension)
	{
		String fileName = coreName + StringUtility.fillLeft(""+fileCounter, suffixNumberLength, "0")+"."+extension;
		fileCounter = fileCounter + 1;
		return fileName;
	}
	
	/**
	 * Print string STR in file at path PATH
	 * @param str
	 * @param path
	 */
	private static void printToFile(String str, String path)
	{
		try
		{
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8")); 
			writer.write(str);
			writer.close();
		}
		catch (IOException e)
		{
			System.out.println("ERROR creatingfile");
		}
	}
	
}
