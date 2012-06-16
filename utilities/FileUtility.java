package utilities;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import jex.statics.JEXStatics;
import jex.statics.OsVersion;

import org.apache.commons.io.FileUtils;

import Database.SingleUserDatabase.JEXWriter;

public class FileUtility implements Comparator<File>
{
	
	public static void openFileDefaultApplication(String name) throws Exception {
        if (OsVersion.IS_OSX) { 
        	Desktop.getDesktop().open(new File(name));
            System.out.println("Executed ! ");
        }
        else if (OsVersion.IS_WINDOWS) { 
        	Desktop.getDesktop().open(new File(name));
            System.out.println("Executed ! ");
        }
    }
	
	public static void openFileDefaultApplication(File path) throws Exception {
		openFileDefaultApplication(path.getPath());
	}
	
	public static int runMultipleStringCommands(String[] separateCommands) throws Exception {
		
		if (OsVersion.IS_OSX) {
			StringBuilder finalCommand = new StringBuilder();
			int count = 0;
			for(String command : separateCommands)
			{
				if(count == 0)
				{
					finalCommand.append(command);
				}
				else
				{
					finalCommand.append(" ; ").append(command);
				}
				count++;
			}
			String str = finalCommand.toString();
			JEXStatics.logManager.log("Execute: " + "/bin/bash -c " + str, 0, "FileUtility");
			Process p = Runtime.getRuntime().exec(new String[]{"/bin/bash","-c",str});
			int result = p.waitFor();
			System.out.println("Executed ! ");
			return result;
		}
		else if (OsVersion.IS_WINDOWS) { 
			return -1;
		}
		return -1;
	}
	
	/**
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
	 * If a deletion fails, the method stops attempting to delete and returns false.
	 * @param dir
	 * @return
	 */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    public static boolean copy(File src, File dst)
    {
    	return copy(src, dst, false);
    }

    /**
     * Copy file SRC into DST
     * @param src
     * @param dst
     * @throws IOException
     */
    public static boolean copy(File src, File dst, boolean forceCopyWithinTempFolder)
    {
    	// test if destination or source files are null
    	if (src == null || dst == null) 
    	{
    		JEXStatics.logManager.log("File copy impossible, source or destination is null", 1, "FileUtility");
    		return false;
    	}
    	
    	if (src.equals(dst))
    	{
    		JEXStatics.logManager.log("No copying necessary (src == dst)", 1, "FileUtility");
    		return true;
    	}
    	
    	// Make destination folder if it doesn't exist
    	if (!dst.getParentFile().exists())
    	{
    		JEXStatics.logManager.log("Making destination folder "+dst.getParent(), 1, "FileUtility");
    		dst.getParentFile().mkdirs();
    	}
    	
    	if (src.getParentFile().equals(dst.getParentFile()) && !FileUtility.isFileInDirectory(src, new File(JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getTempFolderName())))
    	{
    		src.renameTo(dst);
    		JEXStatics.logManager.log("(src folder == dst folder). Renamed file " + src.getPath() + " to " + dst.getPath(), 1, "FileUtility");
    		return true;
    	}
    	
    	if(FileUtility.isFileInDirectory(src, new File(JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getTempFolderName())) && !forceCopyWithinTempFolder)
    	{
    		// Then move the file
    		src.renameTo(dst);
    		JEXStatics.logManager.log("Moving file from " + src.getPath() + " to " + dst.getPath(), 0, "FileUtility");
    		return true;
    	}
    	else
    	{
    		return _copy(src, dst);
    	}
    }
    
    public static boolean moveFolderContents(File srcFolder, File dstFolder)
    {
    	try {
			FileUtils.moveDirectory(srcFolder, dstFolder);
			return true;
		} catch (IOException e) {
			JEXStatics.logManager.log("Error occurred copying " + srcFolder.getPath() + " to " + dstFolder.getPath(), 0, "FileUtility");
    		e.printStackTrace();
    		return false;
		}
    }
    
    private static boolean _copy(File src, File dst)
    {
    	try
    	{
			FileUtils.copyFile(src, dst);
			return true;
		} 
    	catch (IOException e)
    	{
			JEXStatics.logManager.log("Error occurred copying " + src.getPath() + " to " + dst.getPath(), 0, "FileUtility");
    		e.printStackTrace();
    		return false;
		}
//    	// Then copy the file
//		InputStream in = null;
//	    OutputStream out = null;
//	    boolean success = false;
//		try
//		{
//    		in = new FileInputStream(src);
//            out = new FileOutputStream(dst);
//            
//            JEXStatics.logManager.log("Copying " + src.getPath() + " to " + dst.getPath(), 0, "FileUtility");
//        
//            // Transfer bytes from in to out
//            byte[] buf = new byte[32*1024];
//            int len;
//            while ((len = in.read(buf)) > 0)
//            {
//                out.write(buf, 0, len);
//            }
//            success = true;
//		}
//		catch (Exception ex)
//		{
//			JEXStatics.logManager.log("Error occurred copying " + src.getPath() + " to " + dst.getPath(), 0, "FileUtility");
//    		ex.printStackTrace();
//    		success = false;
//		}
//		finally
//		{
//            if (in != null) in.close();
//            if (out != null) out.close();
//		}
//    	return success;
    }

    public static String getFileNameWithoutExtension(String pathOrFilename)
    {
    	String fileName = getFileNameWithExtension(pathOrFilename);
    	if(fileName == null) return null;
    	
    	int index = fileName.lastIndexOf('.');
    	if (index == -1 || index > fileName.length())
    	{
    		return pathOrFilename;
    	}
    	String result = fileName.substring(0, index);
    	
    	return result;
    }
    
    public static String removeWhiteSpaceOnEnds(String s)
    {
    	String temp = s;
    	while(temp.startsWith(" ") || temp.startsWith("\t"))
    	{
    		temp = temp.substring(1);
    	}
    	while(temp.endsWith(" ") || temp.endsWith("\t"))
    	{
    		temp = temp.substring(0, temp.length()-1);
    	}
    	return temp;
    }
    
    public static String getFileNameSuffixDigits(String pathOrFilename)
    {
    	String filename = FileUtility.getFileNameWithoutExtension(pathOrFilename);
    	String suffix = "";
    	for(int j = filename.length()-1; j >= 0; j--)
    	{
    		if(Character.isDigit(filename.charAt(j)))
    		{
    			suffix = filename.substring(j, filename.length());
    		}
    		else
    		{
    			break;
    		}
    	}
    	return suffix;
    }
    
    public static String getFileNameWithExtension(String pathOrFilename)
    {
    	if (pathOrFilename == null) return null;

    	File temp = new File(pathOrFilename);
    	temp = temp.getAbsoluteFile();
    	String fileName = temp.getName();
    	return fileName;
    }
    
    public static String getFileNameExtension(String pathOrFilename)
    {
    	if (pathOrFilename == null) return null;
    	
    	int index = pathOrFilename.lastIndexOf('.');
    	if (index == -1 || index > pathOrFilename.length())
    	{
    		return null;
    	}
    	String result = pathOrFilename.substring(index+1);
    	
    	return result;
    }
    
    public static String[] splitFilePathOnSeparator(String path)
    {
    	return path.split(Pattern.quote(File.separator));
    }
    
    public static String getFileParent(String path)
    {
    	if(path == null) return null;
    	
    	File temp = new File(path);
    	temp = temp.getAbsoluteFile();
    	return temp.getParent();
    }

    public static boolean isFileInDirectory(File f, File directory)
    {
    	File folder = f.getParentFile();
    	if (folder.equals(directory))
    	{
    		return true;
    	}
    	return false;
    }
    
    public static List<File> getSortedFileList(File[] files)
    {
    	List<File> fileList = new Vector<File>();
    	if(files == null || files.length == 0) return fileList;
    	for(File f : files)
    	{
    		fileList.add(f);
    	}
    	sortFileList(fileList);
    	return fileList;
    }
    
    public static void sortFileList(List<File> files)
    {
    	Collections.sort(files, new FileUtility());
    }

	/**
	 * Path comparator method for sorting fileLists
	 */
	public int compare(File thisFile, File thatFile)
	{
		try
		{
			int ret = StringUtility.compareString(thisFile.getCanonicalPath(), thatFile.getCanonicalPath());
			return ret;
		} catch (IOException e) {
			JEXStatics.logManager.log("Couldn't resolve one of the following path strings to a canonical path: " + thisFile.getPath() + " and " + thatFile.getPath(), 0, this);
			e.printStackTrace();
			return -1;
		}
	}
	
	public static String openPath(String path, boolean directories, Component comp){
		JFileChooser chooser = new JFileChooser();
		if (directories) chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		// Set the folder
		File filepath = new File(path);
		if (filepath.isDirectory()) chooser.setCurrentDirectory(filepath);
		else {
			File filefolder = filepath.getParentFile();
			chooser.setCurrentDirectory(filefolder);
		}
		
		String savepath = "";
		File saveFile = null;
		int returnVal = chooser.showOpenDialog(comp);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			saveFile = chooser.getSelectedFile();
			savepath = saveFile.getAbsolutePath();
		} else { 
			JEXStatics.logManager.log("Not possible to choose that file...",1,null);
		}
		
		if (saveFile == null) return null;
		
		File folder = saveFile.getParentFile();
		if (!folder.exists()) {folder.mkdirs();}
		
		return savepath;
	}

	public static String savePath(String path, boolean directories, Component comp){
		JFileChooser chooser = new JFileChooser();
		if (directories) chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		// Set the folder
		File filepath = new File(path);
		if (filepath.isDirectory()) chooser.setCurrentDirectory(filepath);
		else {
			File filefolder = filepath.getParentFile();
			chooser.setCurrentDirectory(filefolder);
		}
		
		String savepath = "";
		File saveFile = null;
		int returnVal = chooser.showSaveDialog(comp);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			saveFile = chooser.getSelectedFile();
			savepath = saveFile.getAbsolutePath();
		} else { 
			JEXStatics.logManager.log("Not possible to save in that file...",1,null);
		}
		
		if (saveFile == null) return null;
		
		File folder = saveFile.getParentFile();
		if (!folder.exists()) {folder.mkdirs();}
		
		return savepath;
	}

	public static String getNextName(String path, String currentName, String desiredPrefix)
	{
		int count = 0;
		File test = new File(path + File.separator + desiredPrefix + count + "_" + currentName);
		while(test.exists())
		{
			count++;
			test = new File(path + File.separator + desiredPrefix + count + "_" + currentName);
		}
		return (desiredPrefix + count + "_" + currentName);
	}
}
