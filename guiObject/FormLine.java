package guiObject;

public class FormLine {
	public static String TEXTFIELD = "TextField";
	public static String DROPDOWN = "DropDown";
	public static String FILECHOOSER = "FileChooser";
	public static String CHECKBOX = "CheckBox";
	
	String title ;
	String note ;
	String type ;
	String[] options;
	String result = "";
	int defaultOption = 0;
	
	/**
	 * Create a form line 
	 * @param title
	 */
	public FormLine(String title) {
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
	public FormLine(String title, String note, String defaultValue) {
		this.title = title;
		this.note = note;
		this.type = TEXTFIELD;
		this.options = new String[] {defaultValue};
	}
	
	/**
	 * Create a form line 
	 * @param title
	 * @param note
	 * @param type
	 * @param options
	 */
	public FormLine(String title, String note, String type, String[] options) {
		this.title = title;
		this.note = note;
		this.type = type;
		this.options = options;
	}
	
	/**
	 * Create a form line 
	 * @param title
	 * @param note
	 * @param type
	 * @param options
	 */
	public FormLine(String title, String note, String type, String[] options, int defaultOption) {
		this.title = title;
		this.note = note;
		this.type = type;
		this.options = options;
		this.defaultOption = defaultOption;
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
}
