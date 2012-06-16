package jex.experimentalTreeAndViewer;

import icons.IconRepository;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

public class ExperimentalTreeArrayController 
{
	private ExperimentalTreeArrayPanel panel;
	private Tray                       tray;
	private ArrayPreviewController     arrayPreview;
	private ExperimentalTreeController parentController;
	
	// GUI
	private Color  background   = ExperimentalTreeController.defaultBackground;
	private JLabel viewbutton   = new JLabel("");
	private JLabel openArrayButton = new JLabel("Open");
	private JLabel traybutton   = new JLabel("");
	private JLabel expandbutton = new JLabel("+");
	public boolean expanded     = false;
	public boolean viewed       = false;
	
	// GUI statics
	private static ImageIcon expandIcon = JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PLUS, 30, 30);
	private static ImageIcon trayIcon   = JEXStatics.iconRepository.getIconWithName(IconRepository.TRAY_ICON, 20, 20);
	private static ImageIcon miscIcon   = JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 20, 20);

	public ExperimentalTreeArrayController(ExperimentalTreeController parentController)
	{
		this.parentController = parentController;
		
		// GUI
		expandbutton = new JLabel("");
		expandbutton.setText(null);
		expandbutton.setIcon(expandIcon);
		
		viewbutton = new JLabel("");
		viewbutton.setText(null);
		viewbutton.setIcon(miscIcon);
		
		traybutton = new JLabel("");
		traybutton.setText(null);
		traybutton.setIcon(trayIcon);
		
		openArrayButton = new JLabel("Open Array");
		openArrayButton.setIcon(trayIcon);
		
		// Connect signals
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTION, this, "selectionChanged", (Class[])null);
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTEDOBJ, this, "selectedObjectChanged", (Class[])null);
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
		
	public ExperimentalTreeArrayPanel panel()
	{
		if (this.panel == null)
		{
			panel = new ExperimentalTreeArrayPanel();
		}
		return panel;
	}
	
	///
	/// SINGALS 
	///
	
	public void rebuildModel()
	{
		//String trayName = (tray == null)? "NULL" : tray.getName();
		//JEXStatics.logManager.log("Rebuilding model for array "+trayName, 1, this);
		
//		if (expanded && tray != null)
//		{
//			//int rows = tray.size();
//			//int cols = tray.get(0).size();
//			arrayPreview = new ArrayPreviewController();
//			//arrayPreview.setRowsAndCols(cols, rows);
//			arrayPreview.setTray(tray);
//		}
//		else
//		{
//			arrayPreview = null;
//		}
		
		panel().rebuild();
	}
	
	public void selectionChanged()
	{
		panel().rebuild();
		
//		if (isSelected()) panel().setBorder(BorderFactory.createLineBorder(DisplayStatics.redoutline, 2));
//		else panel().setBorder(BorderFactory.createLineBorder(DisplayStatics.dividerColor, 2));
//		
//		panel().repaint();
	}
	
	public void selectedObjectChanged()
	{
		panel().rebuild();
	}
	
	public void viewedLevelChangeUp(HierarchyLevel hLevel)
	{
		SSCenter.defaultCenter().emit(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, new Object[]{hLevel});
	}
	
	public void viewedLevelChangeDown(HierarchyLevel hLevel)
	{
		if (tray == hLevel)
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.ENTRY_NOEYE, 20, 20));	
			viewed = true;
		}
		else
		{
			viewbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 20, 20));
			viewed = false;
		}
		panel.repaint();
	}
	
	class ExperimentalTreeArrayPanel extends JPanel implements MouseListener
	{
		private static final long serialVersionUID = 1L;
		
		ExperimentalTreeArrayPanel()
		{
			this.addMouseListener(this);
			viewbutton.addMouseListener(this);
			expandbutton.addMouseListener(this);
			openArrayButton.addMouseListener(this);
			
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
			this.setLayout(new MigLayout("ins 0 0 0 0","10[25]10[fill,grow]10[20]10",""));
			this.add(expandbutton,"Cell 0 0, width 25, span 1 2");
			//this.add(traybutton,"Cell 1 0, width 25, span 1 2");
			this.add(trayLabel,"Cell 1 0, height 10");
			this.add(insideLabel,"Cell 1 1, height 10");
			if (parentController.treeMode() == ExperimentalTreeController.TREE_MODE_VIEW)
			{
				this.add(openArrayButton,"Cell 2 0, span 1 2, width 20");
			}
			else if (parentController.treeMode() == ExperimentalTreeController.TREE_MODE_CREATION)
			{
				this.add(viewbutton,"Cell 2 0, span 1 2, width 20");
			}
			
			// Set the expanded state
			if (expanded)
			{
				expandbutton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_MINUS, 20, 20));
				
				// Make the array preview
				arrayPreview = new ArrayPreviewController();
				arrayPreview.setTray(tray);
				arrayPreview.rebuildModel();
				int cellSizeX = 30;
				int cellSizey = 25;
				int width   = cellSizeX * tray.size() + cellSizeX;
				int height  = cellSizey * tray.get(0).size() + cellSizey;
				this.add(arrayPreview.panel(), "Dock south, growx, height "+height+", width "+width);
				
//				// If the panel is expanded then add a preview of the array
//				if (arrayPreview != null)
//				{
//					arrayPreview.rebuildModel();
//					int cellSizeX = 30;
//					int cellSizey = 25;
//					int width   = cellSizeX * tray.size() + cellSizeX;
//					int height  = cellSizey * tray.get(0).size() + cellSizey;
//					this.add(arrayPreview.panel(), "Dock south, growx, height "+height+", width "+width);
//				}
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
				background = ExperimentalTreeController.selectedBackground;
				this.setBackground(background);
				this.repaint();
			}
		}
		public void mouseExited(MouseEvent e) 
		{
			if (e.getSource() == this){
				background = ExperimentalTreeController.defaultBackground;
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
				viewedLevelChangeUp(tray);
			}
			else if (e.getSource() == expandbutton)
			{
				expanded = !expanded;
				rebuildModel();
			}
			else if (e.getSource() == openArrayButton)
			{
				parentController.openArray(tray);
			}
		}
	}
}
