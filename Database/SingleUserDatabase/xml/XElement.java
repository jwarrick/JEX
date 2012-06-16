package Database.SingleUserDatabase.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;

import utilities.XMLUtility;

public class XElement extends Element {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ELEMENTNAME = "XElement";
	
	protected AttributeList meta;
	protected Collection collection;

	public XElement()
	{
		super(ELEMENTNAME);
	}
	
	public XElement(String species)
	{
		super(species);
		
		if (this.getChild(AttributeList.ELEMENTNAME) != null)
		{
			this.removeContent(this.getChild(AttributeList.ELEMENTNAME));
			//this.removeChildren(AttributeList_20110212.ELEMENTNAME);
		}
		meta = new AttributeList();
		this.addContent(meta);
		
		if (this.getChild(Collection.ELEMENTNAME) != null)
		{
			this.removeContent(this.getChild(Collection.ELEMENTNAME));
			//this.removeChildren(Collection_20110212.ELEMENTNAME);
		}
		collection = new Collection();
		this.addContent(collection);
	}
	
	private void initializeElement()
	{
		meta = (AttributeList) this.getChild(AttributeList.ELEMENTNAME);
		if (meta == null)
		{
			meta = new AttributeList();
			this.addContent(meta);
		}
		
		collection = (Collection) this.getChild(Collection.ELEMENTNAME);
		if (collection == null)
		{
			collection = new Collection();
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
			if (toAddName.equals(AttributeList.ELEMENTNAME))
			{
				if (this.getChild(AttributeList.ELEMENTNAME) != null)
				{
					this.removeContent(this.getChild(AttributeList.ELEMENTNAME));
				}
				Element e = super.addContent(child);
				meta = (AttributeList) toAdd;
				return e;
			}
			
			// If the name is an collection then check if one already exists
			if (toAddName.equals(Collection.ELEMENTNAME))
			{
				if (this.getChild(Collection.ELEMENTNAME) != null)
				{
					this.removeContent(this.getChild(Collection.ELEMENTNAME));
				}
				Element e = super.addContent(child);
				collection = (Collection) toAdd;
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
	
	public void setAtt(String key, String value)
	{
		this.setAtt(key, value, Attribute.DEFAULTCATEGORY);
	}
	
	public void setAtt(String key, String value, String category)
	{
		if (meta == null) initializeElement();
		
		Attribute att = null;
		if(this.hasAtt(key))
		{
			att = meta.getAttWithName(key);
			att.setAttValue(value);
			att.setAttCategory(category);
		}
		else // Make and add attribute
		{
			att = new Attribute(key,value,category);
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
	
	public void addXElement(XElement child)
	{
		collection.addBud(child);
	}
	
	public void removeXElement(XElement child)
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
	
	public List<XElement> getXElements()
	{
		if (collection == null) initializeElement();
		
		@SuppressWarnings("unchecked")
		List<Element> children = collection.getChildren();
		List<XElement> result  = new ArrayList<XElement>();
		for (Element elem: children)
		{
			if (elem instanceof XElement){
				result.add((XElement) elem);
			}
		}
		
		return result;
	}

	public XElement getXElementParent()
	{
		Element parent = this.getParentElement();
		if (parent != null)
		{
			Element grandparent = parent.getParentElement();
			if (grandparent instanceof XElement){
				return (XElement)grandparent;
			}
		}
		return null;
	}
	
	public Iterator<XElement> iterator()
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
