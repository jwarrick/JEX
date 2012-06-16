package jex.jexTabPanel.creationPanel;

import icons.IconRepository;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import utilities.FontUtility;
import Database.DBObjects.JEXEntry;
import Database.Definition.HierarchyLevel;
import Database.Definition.Tray;

public class CreationTrayController {
	
	private CreationExperimentController parentController;
	private CreationTrayPanel      panel;
	private Tray                   tray;
	private ArrayPreview           arrayPreview;
	
	// GUI
	private Color background    = CreationExperimentalTreeController.defaultBackground;
	private JLabel viewbutton   = new JLabel("");
	private JLabel traybutton   = new JLabel("");
	private JLabel expandbutton = new JLabel("+");
	public boolean expanded     = false;

	public CreationTrayController(CreationExperimentController parentController)
	{
		// Pass variables
		this.parentController = parentController;
		
		// GUI
		expandbutton = new JLabel("");
		expandbutton.setText(null);
		expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PLUS, 30, 30));
		
		viewbutton = new JLabel("");
		viewbutton.setText(null);
		viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 20, 20));
		
		traybutton = new JLabel("");
		traybutton.setText(null);
		traybutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.TRAY_ICON, 20, 20));
		
		// Connect signals
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTION, this, "selectionChanged", (Class[])null);
		SSCenter.defaultCenter().connect(parentController.parentController, CreationExperimentalTreeController.VIEWED_LEVEL_CHANGE, this, "viewedLevelChange", new Class[]{HierarchyLevel.class});
	}
	
	///
	/// GETTERS AND SETTERS 
	///
	
	public void setTray(Tray tray)
	{
		this.tray = tray;
		rebuildModel();
	}

	public void setSelected(boolean selected)
	{
		// Get the entries
		Set<JEXEntry> entries = tray.getEntries();
		
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
		Set<JEXEntry> entries = tray.getEntries();
		
		// Are all of the entries selected?
		boolean result = JEXStatics.jexManager.isAllSelected(entries);
		
		return result;
	}
	
	public void setViewedInInspector(boolean viewed)
	{
		if (viewed) parentController.setViewedInInspector(tray);
		else parentController.setViewedInInspector(null);
	}
	
	public boolean isViewedInInspector()
	{
		return parentController.isViewedInInspector(tray);
	}
	
	public CreationTrayPanel panel()
	{
		if (this.panel == null)
		{
			panel = new CreationTrayPanel();
		}
		return panel;
	}
	
	///
	/// SINGALS 
	///
	
	public void rebuildModel()
	{
		if (expanded && tray != null)
		{
			int rows = tray.size();
			int cols = tray.get(0).size();
			arrayPreview = new ArrayPreview();
			arrayPreview.setRowsAndCols(cols, rows);
		}
		else
		{
			arrayPreview = null;
		}
		
		panel().rebuild();
	}
	
	public void selectionChanged()
	{
		panel().rebuild();
	}
	
	public void viewedLevelChange(HierarchyLevel hLevel)
	{
		if (tray == hLevel)
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.ENTRY_NOEYE, 20, 20));	
		}
		else
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 20, 20));
		}
		panel.repaint();
	}
	
	class CreationTrayPanel extends JPanel implements MouseListener
	{
		private static final long serialVersionUID = 1L;
		
		CreationTrayPanel()
		{
			this.addMouseListener(this);
			viewbutton.addMouseListener(this);
			expandbutton.addMouseListener(this);
			
			rebuild();
		}
		
		public void rebuild()
		{
			// Remove all
			this.removeAll();
			
			// Create the gui componenets
			JLabel trayLabel = new JLabel(tray.getName());
			trayLabel.setFont(FontUtility.boldFont);
			JLabel insideLabel = new JLabel("Array of "+tray.size()+" by "+tray.get(0).size());
			insideLabel.setFont(FontUtility.italicFont);
			
			// Place the gui
			this.setLayout(new MigLayout("ins 0 0 0 0","10[25]5[25]10[fill,grow]10[20]10",""));
			this.add(expandbutton,"Cell 0 0, width 25, span 1 2");
			this.add(traybutton,"Cell 1 0, width 25, span 1 2");
			this.add(trayLabel,"Cell 2 0, height 10");
			this.add(insideLabel,"Cell 2 1, height 10");
			this.add(viewbutton,"Cell 3 0, span 1 2, width 20");
			
			// Set the expanded state
			if (expanded)
			{
				expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_MINUS, 20, 20));
				
				// If the panel is expanded then add a preview of the array
				if (arrayPreview != null)
				{
					int cellSizeX = 30;
					int cellSizey = 25;
					int width   = cellSizeX * tray.size() + cellSizeX;
					int height  = cellSizey * tray.get(0).size() + cellSizey;
					JPanel encapsulatingPane = new JPanel();
					encapsulatingPane.setBackground(DisplayStatics.lightBackground);
					encapsulatingPane.setLayout(new MigLayout("ins 5, center","",""));
					encapsulatingPane.add(arrayPreview.panel(),"height "+height+", width "+width);
					this.add(encapsulatingPane, "Dock south, growx");
				}
			}
			else 
			{
				expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PLUS, 20, 20));
			}
			
			if (isSelected()) this.setBorder(BorderFactory.createLineBorder(DisplayStatics.redoutline, 2));
			else this.setBorder(BorderFactory.createLineBorder(DisplayStatics.dividerColor, 2));
			
			this.revalidate();
			this.repaint();
		}
		
		public void mouseClicked(MouseEvent e)  {}
		public void mouseEntered(MouseEvent e) 
		{
			if (e.getSource() == this){
				background = CreationExperimentalTreeController.selectedBackground;
				this.setBackground(background);
				this.repaint();
			}
		}
		public void mouseExited(MouseEvent e) 
		{
			if (e.getSource() == this){
				background = CreationExperimentalTreeController.defaultBackground;
				this.setBackground(background);
				this.repaint();
			}
		}
		public void mousePressed(MouseEvent e)  {}
		public void mouseReleased(MouseEvent e) 
		{
			if (e.getSource() == this){
				setSelected(!isSelected());
			}
			else if (e.getSource() == viewbutton)
			{
				setViewedInInspector(!isViewedInInspector());
			}
			else if (e.getSource() == expandbutton)
			{
				expanded = !expanded;
//				rebuild();
				rebuildModel();
			}
		}
	}
}
