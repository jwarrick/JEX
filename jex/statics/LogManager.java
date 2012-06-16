package jex.statics;

public class LogManager {
	// Statics
	public static int NODETAIL     = 0  ;
	public static int NORMALDETAIL = 1  ;
	public static int HIGHDETAIL   = 2  ;
	public static int ERROR        = -1 ;
	
	public static int OUTPUT2SCREEN = 0 ;
	public static int OUTPUT2FILE   = 1 ;
	
	// Variables
	int detailLevel = 1;
	int output = 0;
	
	public LogManager(){
		
	}

	public void log(String str, int priority, Object source){
		if (detailLevel >= priority){
			String sourceClass = (source == null) ? "###" : source.getClass().getSimpleName();
			if(source instanceof String)
			{
				sourceClass = source.toString();
			}
			
			String toOutput = "   " + sourceClass + " ---> "+str;
			output(toOutput);
		}
		else if (detailLevel == ERROR)
		{
			String sourceClass = (source == null) ? "###" : source.getClass().getSimpleName();
			if(source instanceof String)
			{
				sourceClass = source.toString();
			}
			String toOutput = "!! ERROR !! " + sourceClass + " ---> "+str;
			output(toOutput);
		}
	}
	
	private void output(String str){
		if (output == OUTPUT2SCREEN){
			System.out.println(str);
		}
		else if (output == OUTPUT2FILE){
			
		}
	}
}
