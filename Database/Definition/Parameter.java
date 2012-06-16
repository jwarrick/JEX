package Database.Definition;

import utilities.ArrayUtility;

public class Parameter {
	public static String TEXTFIELD = "TextField";
	public static String DROPDOWN = "DropDown";
	public static String FILECHOOSER = "FileChooser";
	public static String CHECKBOX = "CheckBox";
	
	public String title ;
	public String note ;
	public String type ;
	public String[] options;
	public String result = "";
	int defaultOption = 0;
	
	/**
	 * Create a form line 
	 * @param title
	 */
	public Parameter(String title) {
		this.title = title;
		this.note = "";
		this.type = TEXTFIELD;
		this.options = new String[] {"value"};
	}
	
	/**
	 * Create a form line 
	 * @param title
	 * @param note
	 * @param defaultValue
	 */
	public Parameter(String title, String note, String defaultValue) {
		this.title = title;
		this.note = note;
		this.type = TEXTFIELD;
		this.result = defaultValue;
		this.options = new String[] {defaultValue};
	}
	
	/**
	 * Create a form line 
	 * @param title
	 * @param note
	 * @param type
	 * @param options
	 */
	public Parameter(String title, String note, String type, String[] options) {
		this.title = title;
		this.note = note;
		this.type = type;
		this.options = options;
		this.result = options[0];
	}
	
	/**
	 * Create a form line 
	 * @param title
	 * @param note
	 * @param type
	 * @param options
	 */
	public Parameter(String title, String note, String type, String[] options, int defaultOption) {
		this.title = title;
		this.note = note;
		this.type = type;
		this.options = options;
		this.defaultOption = defaultOption;
		if (options.length >= defaultOption) this.result = options[defaultOption];
		else {
			this.defaultOption = 0;
			this.result = options[0];
		}
	}
	
	/**
	 * Set the current value of the formline
	 * @param value
	 */
	public void setValue(String value){
		this.result = value;
	}
	
	/**
	 * Return the current value of the formline
	 * @return value of the formline
	 */
	public String getValue(){
		return result;
	}
	
	/**
	 * Returns the name of the parameter
	 * @return
	 */
	public String getTitle()
	{
		return this.title;
	}
	
	public Parameter duplicate()
	{
		Parameter ret = new Parameter(title,note,type,ArrayUtility.duplicateStringArray(options),defaultOption);
		ret.setValue(this.result);
		return ret;
	}


}
