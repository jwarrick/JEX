package jex.experimentalTreeAndViewer;

import icons.IconRepository;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import utilities.FontUtility;
import Database.DBObjects.JEXEntry;
import Database.Definition.Experiment;
import Database.Definition.HierarchyLevel;
import Database.Definition.Tray;

public class ExperimentalTreeExperimentController 
{	
	// Model
	public Experiment                             exp;
	public ExperimentalTreeExperimentPanel        panel;
	private ExperimentalTreeController            parentController;
	private List<ExperimentalTreeArrayController> trayPanelControllers;

	// GUI
	private JLabel expandbutton = new JLabel("+");
	private JLabel viewbutton   = new JLabel("");
	private JLabel expLabel     = new JLabel("");
	private JLabel title        = new JLabel("");
	private JLabel info         = new JLabel("");
	private JLabel more         = new JLabel("");
	public boolean expanded     = false;
	public boolean viewed       = false;
	private Color background    = ExperimentalTreeController.defaultBackground;
	
	public ExperimentalTreeExperimentController(ExperimentalTreeController parentController) 
	{
		this.parentController = parentController;
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTION, this, "selectionChanged", (Class[])null);
		
		// initialize
		expandbutton = new JLabel("");
		expandbutton.setText(null);
		expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PLUS, 30, 30));
		
		viewbutton = new JLabel("");
		viewbutton.setText(null);
		viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 30, 30));
		
		expLabel = new JLabel("");
		expLabel.setText(null);
		expLabel.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.EXPERIMENT_ICON, 30, 30));
	}
	
	///
	/// GETTERS AND SETTERS 
	///
	
	public void setExperiment(Experiment exp)
	{
		this.exp = exp;
		rebuildModel();
	}
	
	public void setSelected(boolean selected)
	{
		// Get the entries
		Set<JEXEntry> entries = exp.getEntries();
		
		// Add or remove from selection
		if (selected)
		{
			JEXStatics.jexManager.addEntriesToSelection(entries);
		}
		else
		{
			JEXStatics.jexManager.removeEntriesFromSelection(entries);
		}
	}
	
	public boolean isSelected()
	{
		// Get the entries
		Set<JEXEntry> entries = exp.getEntries();
		
		// Are all of the entries selected?
		boolean result = JEXStatics.jexManager.isAllSelected(entries);
		
		return result;
	}
	
	public boolean isExpanded()
	{
		return this.expanded;
	}
	
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
		
		rebuildModel();
	}
	
	public void rebuildModel()
	{
		JEXStatics.logManager.log("Rebuilding Experiment tree node model - "+exp.getName(), 1, this);
		
		title.setText(exp.getName());
		title.setFont(FontUtility.boldFont);
		info.setText(exp.expInfo);
		info.setFont(FontUtility.italicFont);
		more.setText("Contains "+exp.size()+" arrays");
		more.setFont(FontUtility.italicFont);
		
		trayPanelControllers = new ArrayList<ExperimentalTreeArrayController>();
		if (expanded)
		{
			for (String trayName: exp.keySet())
			{
				// Get the tray
				Tray tray = exp.get(trayName);
				
				// Make a tray display controller
				ExperimentalTreeArrayController trayPanelController = new ExperimentalTreeArrayController(this.parentController);
				trayPanelController.setTray(tray);
				
				// Hook it up to a signaling center
				SSCenter.defaultCenter().connect(trayPanelController, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, this, "viewedLevelChangeUp", new Class[]{HierarchyLevel.class});
				SSCenter.defaultCenter().connect(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_DOWN, trayPanelController, "viewedLevelChangeDown", new Class[]{HierarchyLevel.class});
				
				// Add it to the list
				trayPanelControllers.add(trayPanelController);
			}
		}

		panel().rebuild();
	}

	public ExperimentalTreeExperimentPanel panel()
	{
		if (panel == null)
		{
			panel = new ExperimentalTreeExperimentPanel();
			panel.rebuild();
		}
		return panel;
	}

	
	///
	/// SIGNALS
	///
	
	public void viewedLevelChangeUp(HierarchyLevel hLevel)
	{
		SSCenter.defaultCenter().emit(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, new Object[]{hLevel});
	}
	
	public void viewedLevelChangeDown(HierarchyLevel hLevel)
	{
		if (exp == hLevel)
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.ENTRY_NOEYE, 30, 30));
			viewed = true;
		}
		else
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 30, 30));
			viewed = false;
		}
		panel().repaint();
		
		// Re-emit the signal
		SSCenter.defaultCenter().emit(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_DOWN, new Object[]{hLevel});
	}

	public void selectionChanged()
	{
		panel().setPanelSelected(isSelected());
	}

	class ExperimentalTreeExperimentPanel extends JPanel implements MouseListener
	{
		private static final long serialVersionUID = 1L;
		private JPanel topPanel;

		ExperimentalTreeExperimentPanel()
		{			
			// make the holding panel
			topPanel = new JPanel();

			// Make the listeners
			topPanel.addMouseListener(this);
			expandbutton.addMouseListener(this);
			viewbutton.addMouseListener(this);
		}

		public void rebuild()
		{
			JEXStatics.logManager.log("Rebuilding Experiment tree node panel - "+exp.getName(), 1, this);
			
			// remove all
			this.removeAll();

			// Set the mig layout
			this.setLayout(new MigLayout("flowy, ins 0","10[fill,grow]10",""));
			this.setBackground(DisplayStatics.background);

			// Add all the components
			topPanel.setLayout(new MigLayout("flowy, ins 0","10[fill,grow]10",""));
			topPanel.add(title,"gapy 2, height 15");
			topPanel.add(info,"gapy 2, height 10");
			topPanel.add(more,"gapy 0, height 10");
			this.add(topPanel,"gapy 0");
			
			// Place the cropping button
			topPanel.add(expandbutton,"width 35,gapx 10, Dock west");
			topPanel.add(expLabel,"width 35,gapx 10, Dock west");
			if (parentController.treeMode() == ExperimentalTreeController.TREE_MODE_CREATION)
			{
				topPanel.add(viewbutton,"width 35, Dock east");
			}

			// If the cell is expanded display the arrays
			if (expanded)
			{
				expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_MINUS, 20, 20));
				
				for (ExperimentalTreeArrayController trayPanelController: trayPanelControllers)
				{
					this.add(trayPanelController.panel(),"gapx 20, gapy 2");
				}
				
				this.add(Box.createRigidArea(new Dimension(2,2)),"gapy 5");
			}
			else 
			{
				expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PLUS, 20, 20));
			}
			
			// If the cell is selected display the background
			if (isSelected()) topPanel.setBorder(BorderFactory.createLineBorder(DisplayStatics.redoutline, 2));
			else topPanel.setBorder(BorderFactory.createLineBorder(DisplayStatics.dividerColor, 2));

			// refresh display
			panel().revalidate();
			panel().repaint();
		}

		public void setPanelSelected(boolean selected)
		{
			// If the cell is selected display the background
			if (selected) topPanel.setBorder(BorderFactory.createLineBorder(DisplayStatics.redoutline, 2));
			else topPanel.setBorder(BorderFactory.createLineBorder(DisplayStatics.dividerColor, 2));
		}
		
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) 
		{
			if (e.getSource() == topPanel || e.getSource() == expandbutton){
				background = ExperimentalTreeController.selectedBackground;
				topPanel.setBackground(background);
				for (ExperimentalTreeArrayController trayPanelController: trayPanelControllers)
				{
					JPanel trayPanel = trayPanelController.panel();
					trayPanel.setBackground(background);
				}
				this.repaint();
			}
		}
		public void mouseExited(MouseEvent e) 
		{
			if (e.getSource() == topPanel || e.getSource() == expandbutton){
				background = ExperimentalTreeController.defaultBackground;
				topPanel.setBackground(background);
				for (ExperimentalTreeArrayController trayPanelController: trayPanelControllers)
				{
					JPanel trayPanel = trayPanelController.panel();
					trayPanel.setBackground(background);
				}
				this.repaint();
			}
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) 
		{
			if (e.getSource() == topPanel){
				setSelected(!isSelected());
			}
			else if (e.getSource() == expandbutton)
			{
				if (!isExpanded())
				{
					parentController.openExperiment(exp);
				}
				else
				{
					parentController.openExperiment(null);
				}
			}
			else if (e.getSource() == viewbutton)
			{
				viewedLevelChangeUp(exp);
			}
		}

	}
	
	
}