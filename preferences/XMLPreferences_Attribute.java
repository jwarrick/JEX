package preferences;

import java.beans.PropertyChangeListener;

import org.jdom.Element;

public class XMLPreferences_Attribute extends Element implements Property {
	private static final long serialVersionUID = 1L;
	
	// Common Values
	public static String DEFAULTCATEGORY = "DefaultCat";
	public static final String ELEMENTNAME = "Att";

	// Standard Fields
	public static String NAME = "Name";
	public static String VALUE = "Value";
	public static String CATEGORY = "Category";	
	
	public XMLPreferences_Attribute(){
		super(ELEMENTNAME);
	}

	public XMLPreferences_Attribute(String attName, Object attValue, String category)
	{
		this();
		
		this.setAttName(attName);
		this.setAttValue(attValue);
		this.setAttCategory(category);
	}

	public XMLPreferences_Attribute(String attName, Object attValue)
	{
		this();

		this.setAttName(attName);
		this.setAttValue(attValue);
		this.setAttCategory(XMLPreferences_Attribute.DEFAULTCATEGORY);
	}
	
	public String getAttName()
	{
		String value = this.getAttributeValue(XMLPreferences_Attribute.NAME);
		return value;
	}
	
	public void setAttName(String attName)
	{
		this.setAttribute(XMLPreferences_Attribute.NAME, attName);
	}
	
	public String getAttValue()
	{
		String value = this.getAttributeValue(XMLPreferences_Attribute.VALUE);
		return value;
	}
	
	public void setAttValue(Object o)
	{
		this.setAttribute(XMLPreferences_Attribute.VALUE, o.toString());
	}
	
	public String getAttCategory()
	{
		String categoryStr = this.getAttributeValue(XMLPreferences_Attribute.CATEGORY);
		if (categoryStr != null) return categoryStr;
		return XMLPreferences_Attribute.DEFAULTCATEGORY;
	}
	
	public void setAttCategory(String category){
		this.setAttribute(XMLPreferences_Attribute.CATEGORY, category);
	}

	public String getDisplayName() {
		return this.getAttName();
	}

	public String getShortDescription() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class getType() {
		return String.class;
	}

	public void setValue(Object value) {
		this.setAttValue(value);
	}
	
	public String getValue() {
		return this.getAttValue();
	}

	public boolean isEditable() {
		return true;
	}

	public String getCategory() {
		return null;
	}

	public void readFromObject(Object object) {
		// TODO Auto-generated method stub
		
	}

	public void writeToObject(Object object) {
		// TODO Auto-generated method stub
		
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public Property getParentProperty() {
		return (Property) this.getParentElement().getParentElement();
	}

	public Property[] getSubProperties() {
		return null;
	}
	
}

