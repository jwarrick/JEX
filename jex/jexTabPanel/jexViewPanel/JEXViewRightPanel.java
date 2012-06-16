package jex.jexTabPanel.jexViewPanel;

import java.awt.BorderLayout;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jex.JEXManager;
import jex.infoPanels.DatabaseInfoPanelController;
import jex.infoPanels.InfoPanelController;
import jex.infoPanels.LabelInfoPanelController;
import jex.infoPanels.SelectedEntriesInfoPanelController;
import jex.infoPanels.SelectedObjectInfoPanelController;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;

public class JEXViewRightPanel extends JPanel{
	private static final long serialVersionUID = 1L;
//	private InfoPanelList  infoPane;
	
	// GUI
	JScrollPane          scroll ;
	JPanel               center      ;
//	ExperimentalViewMode expViewMode ;

	JEXViewRightPanel()
	{
		// Do the gui
		initialize();
		
		// Listen to infopanel change
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.INFOPANELS, this, "infoPanelsChanged", (Class[])null);
		JEXStatics.logManager.log("Connected to infopanels change signal", 0, this);
		
		// rebuild
		rebuild();
	}
	
	/**
	 * Detach the signals
	 */
	public void deInitialize()
	{
		SSCenter.defaultCenter().disconnect(this);
	}
	
	/**
	 * Update the infopanel list
	 */
	public void infoPanelsChanged(){
//		this.setPanels(JEXStatics.jexManager.getInfoPanels());
		this.rebuild();
	}
	
	private void initialize()
	{
		// Set some infopanels
		JEXStatics.jexManager.setInfoPanelController("Database", new DatabaseInfoPanelController());
		JEXStatics.jexManager.setInfoPanelController("Selection", new SelectedEntriesInfoPanelController());
		JEXStatics.jexManager.setInfoPanelController("SelectedLabel", new LabelInfoPanelController());
		JEXStatics.jexManager.setInfoPanelController("SelectedObject", new SelectedObjectInfoPanelController());
		
//		// Create the experimental view mode panel
//		expViewMode = new ExperimentalViewMode();
		
		// Create the center panel
		center = new JPanel();
		center.setLayout(new MigLayout("flowy, ins 3","[grow,fill]","[]3[]3[]3[]3[]3[]3[]"));
		center.setBackground(DisplayStatics.background);
		
//		// Place the scroll panel
//		scroll = new JScrollPane(center);
//		scroll.setBackground(DisplayStatics.background);
//		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		// Place them in the gui
		this.setLayout(new BorderLayout());
		this.setBackground(DisplayStatics.background);
		this.add(center,BorderLayout.CENTER);
//		this.add(expViewMode,BorderLayout.PAGE_END);
	}
	
	public void rebuild()
	{
		// Remove all the panels
		center.removeAll();
		//center.setLayout(new MigLayout("ins 4","[fill]",""));

		// Get the panels controllers
		TreeMap<String,InfoPanelController> controllers = JEXStatics.jexManager.getInfoPanelControllers();
		for (InfoPanelController c: controllers.values())
		{
			if (c == null) continue;
			JPanel p = c.panel();
			center.add(p,"growx");
		}
		//center.add(Box.createRigidArea(new Dimension(2,2)),"growy");

		center.invalidate();
		center.validate();
		center.repaint();
		this.repaint();
	}
}
