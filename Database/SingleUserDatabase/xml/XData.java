package Database.SingleUserDatabase.xml;

import java.util.ArrayList;
import java.util.List;

import Database.DBObjects.JEXData;

public class XData extends XElement{
	
	private static final long serialVersionUID = 1L;
	
	public static final String ELEMENTNAME = "XData";

	public XData(){
		super(ELEMENTNAME);
	}

	public XData(String type){
		super(ELEMENTNAME);
		this.setAttribute(JEXData.TYPE, type);
	}
	
	public void addMeta(String key, String value)
	{
		this.setAtt(key,value,"");
	}
	
	public void addMetaWithCategory(String key, String category, String value)
	{
		this.setAtt(key,value,category);
	}
	
	public void addDataSingle(XDataSingle data)
	{
		this.addXElement(data);
	}
	
	public String getTypeField()
	{
		return this.getAttributeValue(JEXData.TYPE);
	}
	
	public XDataSingle getFirstDataElement()
	{
		XDataSingle result = (XDataSingle) collection.getContent(0);
		return result;
	}
	
	public List<XDataSingle> getSingleDataElements()
	{
		List<XDataSingle> result = new ArrayList<XDataSingle>(0);
		List<XElement> children = this.getXElements();
		for (XElement child: children){
			result.add((XDataSingle)child);
		}
		return result;
	}

}






