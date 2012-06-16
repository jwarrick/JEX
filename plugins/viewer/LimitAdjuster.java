package plugins.viewer;

import java.awt.Color;

import javax.swing.JPanel;

import Database.DBObjects.dimension.Dim;

import net.miginfocom.swing.MigLayout;
import signals.SSCenter;

public class LimitAdjuster {
	
	private DimScrollBar minBar = null, maxBar = null;
	private JPanel panel;
	
	public static final String SIG_limitsChanged_NULL = "SIG_limitsChanged_NULL";
	
	public LimitAdjuster()
	{
		this.panel = new JPanel();
		this.panel.setBackground(Color.RED);
		this.panel.setLayout(new MigLayout("flowy,ins 0","[fill,grow]","[]0[]"));		
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	public void setIntensityBounds(int max)
	{
		int tempmin = this.min();
		int tempmax = this.max();
		
		if(this.maxBar == null)
		{
			this.maxBar = new DimScrollBar(new Dim("Max", 0, max), false);
			this.panel.add(this.maxBar.panel(),"growx");
			SSCenter.defaultCenter().connect(this.maxBar, DimScrollBar.SIG_ValueChanged_Null, this, "maxChanged", (Class[])null);
		}
		else
		{
			this.maxBar.setDim(new Dim("Max", 0, max));
		}
		if(this.minBar == null)
		{
			this.minBar = new DimScrollBar(new Dim("Min", 0, max), false);
			this.panel.add(this.minBar.panel(),"growx");
			SSCenter.defaultCenter().connect(this.minBar, DimScrollBar.SIG_ValueChanged_Null, this, "minChanged", (Class[])null);
		}
		else
		{
			this.minBar.setDim(new Dim("Min", 0, max));
		}
		
				
		this.setLimits(tempmin, tempmax);
		
	}
	
	public int min()
	{
		if(this.minBar == null) return -1;
		return this.minBar.indexInt();
	}
	
	public int max()
	{
		if(this.maxBar == null) return -1;
		return this.maxBar.indexInt();
	}
	
	public void minChanged()
	{
		if(minBar.indexInt() > maxBar.indexInt())
		{
			minBar.setIndex(maxBar.indexInt());
		}
		SSCenter.defaultCenter().emit(this, SIG_limitsChanged_NULL, (Object[])null);
	}
	
	public void maxChanged()
	{
		if(minBar.indexInt() > maxBar.indexInt())
		{
			maxBar.setIndex(minBar.indexInt());
		}
		SSCenter.defaultCenter().emit(this, SIG_limitsChanged_NULL, (Object[])null);
	}
	
	public void setLimits(int min, int max)
	{
		if(min < 0 || min > this.minBar.dim().size()) min = 0;
		if(min < 0 || min > this.maxBar.dim().size()) max = this.maxBar.dim().size();
		this.minBar.setIndex(min);
		this.maxBar.setIndex(max);
	}

}
