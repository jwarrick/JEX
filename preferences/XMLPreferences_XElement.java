package preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;

import Database.SingleUserDatabase.xml.Attribute;

import utilities.XMLUtility;

public class XMLPreferences_XElement extends Element {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ELEMENTNAME = "XElement";
	
	protected XMLPreferences_AttributeList meta;
	protected XMLPreferences_Collection collection;

	public XMLPreferences_XElement()
	{
		super(ELEMENTNAME);
	}
	
	public XMLPreferences_XElement(String species)
	{
		super(ELEMENTNAME);
		
		if (this.getChild(XMLPreferences_AttributeList.ELEMENTNAME) != null)
		{
			this.removeContent(this.getChild(XMLPreferences_AttributeList.ELEMENTNAME));
			//this.removeChildren(AttributeList_20110212.ELEMENTNAME);
		}
		meta = new XMLPreferences_AttributeList();
		this.addContent(meta);
		
		if (this.getChild(XMLPreferences_Collection.ELEMENTNAME) != null)
		{
			this.removeContent(this.getChild(XMLPreferences_Collection.ELEMENTNAME));
			//this.removeChildren(Collection_20110212.ELEMENTNAME);
		}
		collection = new XMLPreferences_Collection();
		this.addContent(collection);
	}
	
	private void initializeElement()
	{
		meta = (XMLPreferences_AttributeList) this.getChild(XMLPreferences_AttributeList.ELEMENTNAME);
		if (meta == null)
		{
			meta = new XMLPreferences_AttributeList();
			this.addContent(meta);
		}
		
		collection = (XMLPreferences_Collection) this.getChild(XMLPreferences_Collection.ELEMENTNAME);
		if (collection == null)
		{
			collection = new XMLPreferences_Collection();
			this.addContent(collection);
		}
	}
	
	// ---------------------------------------------
	// Meta bud convenience methods
	// ---------------------------------------------
	
	@Override
	public Element addContent(Content child)
	{
		if (child instanceof Element)
		{
			// Cast to element
			Element toAdd = (Element) child;
			
			// Get the name
			String toAddName = toAdd.getName();
			
			// If the name is an attlist then check if one already exists
			if (toAddName.equals(XMLPreferences_AttributeList.ELEMENTNAME))
			{
				if (this.getChild(XMLPreferences_AttributeList.ELEMENTNAME) != null)
				{
					this.removeContent(this.getChild(XMLPreferences_AttributeList.ELEMENTNAME));
				}
				Element e = super.addContent(child);
				meta = (XMLPreferences_AttributeList) toAdd;
				return e;
			}
			
			// If the name is an collection then check if one already exists
			if (toAddName.equals(XMLPreferences_Collection.ELEMENTNAME))
			{
				if (this.getChild(XMLPreferences_Collection.ELEMENTNAME) != null)
				{
					this.removeContent(this.getChild(XMLPreferences_Collection.ELEMENTNAME));
				}
				Element e = super.addContent(child);
				collection = (XMLPreferences_Collection) toAdd;
				return e;
			}
		}
		
		Element e = super.addContent(child);
		return e;
	}
	
	public boolean hasAtt(String key)
	{
		if(meta == null) initializeElement();
		Element e = meta.getAttWithName(key);
		return e != null;
	}
	
	public String getAtt(String key)
	{
		if (meta == null) initializeElement();
		return meta.getValueOfAttWithName(key);
	}
	
	public XMLPreferences_Attribute getFullAttribute(String key)
	{
		if (meta == null) initializeElement();
		return meta.getAttWithName(key);
	}
	
	public void setAtt(String key, String value)
	{
		this.setAtt(key, value, Attribute.DEFAULTCATEGORY);
	}
	
	public void setAtt(String key, String value, String category)
	{
		if (meta == null) initializeElement();
		
		XMLPreferences_Attribute att = null;
		if(this.hasAtt(key))
		{
			att = meta.getAttWithName(key);
			att.setAttValue(value);
			att.setAttCategory(category);
		}
		else // Make and add attribute
		{
			att = new XMLPreferences_Attribute(key,value,category);
			meta.addAtt(att);
		}		
	}
	
	public void removeAtt(String key)
	{
		if (meta == null) return;
		
		meta.removeAttWithName(key);
	}

	public List<String> getAttNames()
	{
		if (meta == null) initializeElement();
		
		List<String> result = meta.getAttNames();
		return result;
	}
//	
//	public BudAttribute getAttributeWithName(String name)
//	{
//		if (meta == null) initializeElement();
//		
//		BudAttribute result = meta.getAttWithName(name);
//		return result;
//	}
	
	// ---------------------------------------------
	// Collection bud convenience methods
	// ---------------------------------------------
	
	public void addXElement(XMLPreferences_XElement child)
	{
		collection.addBud(child);
	}
	
	public void removeXElement(XMLPreferences_XElement child)
	{
		collection.removeBud(child);
	}
	
	@SuppressWarnings("unchecked")
	public List<Element> getCollectionChildren()
	{
		if (collection == null) initializeElement();
		List<Element> result = collection.getChildren();
		return result;
	}
	
	public List<XMLPreferences_XElement> getXElements()
	{
		if (collection == null) initializeElement();
		
		@SuppressWarnings("unchecked")
		List<Element> children = collection.getChildren();
		List<XMLPreferences_XElement> result  = new ArrayList<XMLPreferences_XElement>();
		for (Element elem: children)
		{
			if (elem instanceof XMLPreferences_XElement){
				result.add((XMLPreferences_XElement) elem);
			}
		}
		
		return result;
	}

	public XMLPreferences_XElement getXElementParent()
	{
		Element parent = this.getParentElement();
		if (parent != null)
		{
			Element grandparent = parent.getParentElement();
			if (grandparent instanceof XMLPreferences_XElement){
				return (XMLPreferences_XElement)grandparent;
			}
		}
		return null;
	}
	
	public Iterator<XMLPreferences_XElement> iterator()
	{
		return this.getXElements().iterator();
	}
	
	@Override
	public String toString()
	{
		String result = XMLUtility.toXML(this);
		return result;
	}
}
