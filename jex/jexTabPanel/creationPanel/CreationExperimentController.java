package jex.jexTabPanel.creationPanel;

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

public class CreationExperimentController 
{	
	// Model
	public Experiment                    exp;
	public CreationCellPanel             panel;
	public CreationExperimentalTreeController       parentController;
	private List<CreationTrayController> trayPanelControllers;

	// GUI
	private JLabel expandbutton = new JLabel("+");
	private JLabel viewbutton   = new JLabel("");
	private JLabel expLabel     = new JLabel("");
	private JLabel title        = new JLabel("");
	private JLabel info         = new JLabel("");
	private JLabel more         = new JLabel("");
	public boolean expanded     = false;
	private Color background    = CreationExperimentalTreeController.defaultBackground;
	
	public CreationExperimentController(CreationExperimentalTreeController parentController) 
	{
		this.parentController = parentController;
		
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTION, this, "selectionChanged", (Class[])null);
		SSCenter.defaultCenter().connect(parentController, CreationExperimentalTreeController.VIEWED_LEVEL_CHANGE, this, "viewedLevelChange", new Class[]{HierarchyLevel.class});
		
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
	
	public void setViewedInInspector(HierarchyLevel level)
	{
		this.parentController.setViewedInInspector(level);
	}
	
	public boolean isViewedInInspector(HierarchyLevel level)
	{
		return this.parentController.isViewedInInspector(level);
	}
		
	public void rebuildModel()
	{
		title.setText(exp.getName());
		title.setFont(FontUtility.boldFont);
		info.setText(exp.expInfo);
		info.setFont(FontUtility.italicFont);
		more.setText("Contains "+exp.size()+" arrays");
		more.setFont(FontUtility.italicFont);
		
		trayPanelControllers = new ArrayList<CreationTrayController>();
		if (expanded)
		{
			for (String trayName: exp.keySet())
			{
				Tray tray = exp.get(trayName);
				CreationTrayController trayPanelController = new CreationTrayController(this);
				trayPanelController.setTray(tray);
				trayPanelControllers.add(trayPanelController);
			}
		}

		panel().rebuild();
	}

	public CreationCellPanel panel()
	{
		if (panel == null)
		{
			panel = new CreationCellPanel();
		}
		return panel;
	}

	
	///
	/// SIGNALS
	///
	
	public void viewedLevelChange(HierarchyLevel hLevel)
	{
		if (exp == hLevel)
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.ENTRY_NOEYE, 30, 30));	
		}
		else
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 30, 30));
		}
		panel.repaint();
	}

	public void selectionChanged()
	{
		panel().rebuild();
	}

	class CreationCellPanel extends JPanel implements MouseListener
	{
		private static final long serialVersionUID = 1L;
		private JPanel topPanel;

 		CreationCellPanel()
		{			
			// make the holding panel
			topPanel = new JPanel();

			// Make the listeners
			topPanel.addMouseListener(this);
			expandbutton.addMouseListener(this);
			viewbutton.addMouseListener(this);
			
			// Rebuild
			rebuild();
		}

		public void rebuild()
		{
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
			topPanel.add(viewbutton,"width 35, Dock east");
			
			// If the cell is expanded display the arrays
			if (expanded)
			{
				expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_MINUS, 20, 20));
				
				for (CreationTrayController trayPanelController: trayPanelControllers)
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
			parentController.panel().revalidate();
			parentController.panel().repaint();
		}

		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) 
		{
			if (e.getSource() == topPanel || e.getSource() == expandbutton){
				background = CreationExperimentalTreeController.selectedBackground;
				topPanel.setBackground(background);
				for (CreationTrayController trayPanelController: trayPanelControllers)
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
				background = CreationExperimentalTreeController.defaultBackground;
				topPanel.setBackground(background);
				for (CreationTrayController trayPanelController: trayPanelControllers)
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
				expanded = !expanded;
//				rebuild();
				rebuildModel();
			}
			else if (e.getSource() == viewbutton)
			{
				setViewedInInspector(exp);
			}
		}

	}
	
	
}