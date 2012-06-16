package Database.SingleUserDatabase.xml;

import Database.DBObjects.JEXData;

public class XDataSingle extends XElement{

	private static final long serialVersionUID = 1L;
	
	public static final String ELEMENTNAME = "XDataSingle";

	public XDataSingle(){
		super(ELEMENTNAME);
	}
	
	public XDataSingle(String type){
		super(ELEMENTNAME);
		this.setAttribute(JEXData.TYPE, type);
	}
	
	public void addMeta(String key, String value)
	{
		this.setAtt(key, value);
	}
	
	public void addMeta(String key, String value, String category)
	{
		this.setAtt(key, value, category);
	}
	
}
