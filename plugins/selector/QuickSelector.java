package plugins.selector;

import image.roi.PointList;

import java.awt.Point;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JPanel;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import Database.DBObjects.JEXEntry;
import Database.Definition.Experiment;
import Database.Definition.Tray;

public class QuickSelector {
	
	private final String EXPT = "Expt: ";
	private final String TRAY = "Array: ";
	
	private JPanel panel;
	private QuickSelectorHeaders headers;
	private QuickSelectorList list;
	private QuickSelectorArray array;
	private TreeMap<String,Experiment> experiments;
	public Experiment curExpt;
	public Tray curTray;
	public List<String> listNames;
	
	public QuickSelector()
	{
		this.panel = new JPanel(new MigLayout("left,flowy,ins 5","[fill,grow]","[]0[fill,grow]"));
		this.panel.setBackground(DisplayStatics.lightBackground);
		//this.panel.setBackground(Color.RED);
		this.headers = new QuickSelectorHeaders();
		//this.headers.panel().setBackground(Color.BLUE);
		this.list = new QuickSelectorList();
		//this.list.panel().setBackground(Color.YELLOW);
		this.array = new QuickSelectorArray();
		this.experiments = new TreeMap<String,Experiment>();
		this.curExpt = null;
		this.curTray = null;
		this.listNames = new Vector<String>();
		this.connect();
		this.organize();
		this.reset();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	private void connect()
	{
		SSCenter.defaultCenter().connect(this.headers, QuickSelectorHeaders.SIG_ExptHeaderClicked_NULL, this, "clickedOnExperimentHeader", (Class[])null);
		SSCenter.defaultCenter().connect(this.headers, QuickSelectorHeaders.SIG_TrayHeaderClicked_NULL, this, "clickedOnTrayHeader", (Class[])null);
		
		SSCenter.defaultCenter().connect(this.list, QuickSelectorList.SIG_ListSelectionChanged_NULL, this, "chooseSelection",  (Class[])null);
		SSCenter.defaultCenter().connect(this.array, QuickSelectorArray.SIG_SelectionChanged_NULL, this, "chooseSelection", (Class[])null);
		
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "navigationChanged", (Class[])null);
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTION, this, "showEntries", (Class[])null);
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.EXPERIMENTTREE_CHANGE, this, "reset", (Class[])null);
	}
	
	private void organize()
	{
		this.panel.add(this.headers.panel(), "grow");
		this.panel.add(this.list.panel(), "grow");
	}
	
	public void chooseSelection()
	{
		// If choosing Expt, change header and show list of trays
		if(this.curExpt == null)
		{
			String selectedExptName = this.list.getSelection();
			if(selectedExptName == null)
			{
				return;
			}
			
			// Send command to the jexmanager to select the experiment
			JEXStatics.jexManager.setExperimentAndArrayViewed(selectedExptName, null);
		}
		
		// If choosing Tray, change header and show array
		else if(this.curExpt != null && this.curTray == null)
		{
			String selectedTrayName = this.list.getSelection();
			if(selectedTrayName == null)
			{
				return;
			}

			// Send command to the jexmanager to select the experiment
			JEXStatics.jexManager.setExperimentAndArrayViewed(this.curExpt.expName, selectedTrayName);
		}
		
		// If choosing Entry, send entry selection signal
		else if(this.curExpt != null && this.curTray != null)
		{
			PointList pl = this.array.getSelected();
			TreeSet<JEXEntry> selectedEntries = new TreeSet<JEXEntry>();
			for(Point p : pl)
			{
				selectedEntries.add(this.curTray.get(p.x).get(p.y));
			}
			TreeSet<JEXEntry> currentSelection = JEXStatics.jexManager.getSelectedEntries();
			for(JEXEntry e : currentSelection)
			{
				String tempExpName = e.getEntryExperiment();
				String tempTrayName = e.getEntryTrayName();
				if(!tempExpName.equals(this.curExpt.expName) || (tempExpName.equals(this.curExpt.expName) && !tempTrayName.equals(this.curTray.arrayName)))
				{
					selectedEntries.add(e);
				}
			}
			JEXStatics.jexManager.setSelectedEntries(selectedEntries);
			
		}
	}
	
	public void showExperiments()
	{
		if(this.experiments != null)
		{
		this.panel.remove(this.array.panel());
		this.panel.remove(this.list.panel());
		this.panel.add(this.list.panel(),"grow");
		this.headers.setExperimentHeader(EXPT);
		this.headers.setTrayHeader(TRAY);
		this.headers.panel().repaint();
		this.curExpt = null;
		this.curTray = null;
		this.listNames.clear();
		for(String exptName : this.experiments.keySet())
		{
			this.listNames.add(exptName);
		}
		this.list.setList(this.listNames);
		this.headers.setListHeader("Choose an experiment");
		}
		this.repaint();
	}
	
	public void showTrays()
	{
		if(this.curExpt != null)
		{
			this.panel.remove(this.array.panel());
			this.panel.remove(this.list.panel());
			this.panel.add(this.list.panel(),"grow");
			this.headers.setExperimentHeader(EXPT + this.curExpt.expName);
			this.headers.setTrayHeader(TRAY);
			this.curTray = null;

			this.listNames.clear();
			for(String name : this.curExpt.keySet())
			{
				this.listNames.add(name);
			}
			this.list.setList(this.listNames);
			this.headers.setListHeader("Choose an array");
		}
		this.repaint();
	}
	
	public void showEntries()
	{
		if(this.curExpt != null && this.curTray != null)
		{
			this.panel.remove(this.array.panel());
			this.panel.remove(this.list.panel());
			this.panel.add(this.array.panel(),"grow");
			this.headers.setExperimentHeader(EXPT + this.curExpt.expName);
			this.headers.setTrayHeader(TRAY + this.curTray.arrayName);
		
			int width = 0, height = 0;
			for(Integer x : this.curTray.keySet())
			{
				if((x+1) > width)
				{
					width = x+1;
				}
				for(Integer y : this.curTray.get(x).keySet())
				{
					if((y+1) > height)
					{
						height = y+1;
					}
				}
			}
			if(width == 0 || height == 0)
			{
				return;
			}
			this.array.setRowsAndCols(height, width);
			this.array.deselectAll();
			for(JEXEntry e : this.curTray.entries)
			{
				if(JEXStatics.jexManager.isSelected(e))
				{
					this.array._select(new Point(e.getTrayX(), e.getTrayY()));
				}
			}
			this.headers.setListHeader("Choose entries");
		}
		this.repaint();
	}
	
	public void clickedOnExperimentHeader()
	{
		JEXStatics.logManager.log("Clicked on the experient header, sending command", 1, this);
		JEXStatics.jexManager.setExperimentAndArrayViewed(null, null);
	}
	
	public void clickedOnTrayHeader()
	{
		JEXStatics.logManager.log("Clicked on the tray header, sending command", 1, this);
		JEXStatics.jexManager.setExperimentAndArrayViewed(this.curExpt.expName, null);
	}
	
	public void navigationChanged()
	{
		String viewedExp = JEXStatics.jexManager.getExperimentViewed();
		String viewedArray = JEXStatics.jexManager.getArrayViewed();
		this.experiments = JEXStatics.jexManager.getExperimentTree();
		
		if (viewedExp == null)
		{
			showExperiments();
		}
		else
		{
			Experiment exp = this.experiments.get(viewedExp);
			this.curExpt   = exp;
			
			if (exp == null || viewedArray == null)
			{
				showTrays();
			}
			else 
			{
				Tray tray = exp.get(viewedArray);
				this.curTray = tray;
				showEntries();
			}
		}
	}
	
	public void reset()
	{
		this.experiments = JEXStatics.jexManager.getExperimentTree();
		this.showExperiments();
		this.repaint();
	}
	
	public void repaint()
	{
		this.panel.revalidate();
		this.panel.repaint();
	}

}
