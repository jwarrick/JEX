package jex.jexTabPanel.jexFunctionPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import signals.SSCenter;
import utilities.FontUtility;
import utilities.StringUtility;
import Database.DBObjects.JEXEntry;

public class JEXEntryPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	// Model variables
	private TreeSet<JEXEntry> entryList;
	private List<JEXEntryPanelLine> panelList;

	// Main gui components
	private JScrollPane        scroll;
	private JPanel             entryPane;
	
	public JEXEntryPanel()
	{
		this.panelList = new ArrayList<JEXEntryPanelLine>(0);

		// Setup updating links
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.SELECTION, this, "rebuild", (Class[])null);

		// Create the entry panel
		entryPane = new JPanel();
		entryPane.setBackground(DisplayStatics.lightBackground);
		entryPane.setLayout(new BoxLayout(entryPane,BoxLayout.PAGE_AXIS));

		// Create the scroll panel
		scroll = new JScrollPane(entryPane);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		this.setBackground(DisplayStatics.lightBackground);
//		this.setLayout(new MigLayout("center,flowy,ins 2","[center,grow]","[]1[0:0,fill,grow 33]1[]1[0:0,grow 67]"));
		this.setLayout(new BorderLayout());
		this.add(scroll);

		rebuild();
	}
	
	/**
	 * Rebuild the objectpanel list in case the array has changed
	 */
	public void rebuild(){		
		// Get the itnv fromt the database
		entryList = JEXStatics.jexManager.getSelectedEntries();
		entryPane.removeAll();
		entryPane.setAlignmentX(LEFT_ALIGNMENT);
		entryPane.setBackground(DisplayStatics.lightBackground);

		// If the object list is empty or null
		// then add a message signaling it to the user
		if (entryList == null || entryList.size() == 0)
		{
			JEXStatics.logManager.log("Rebuilding the entry panel: found 0 entries", 2, this);
			
			// create a label
			JLabel label = new JLabel("No Entries");
			label.setFont(FontUtility.italicFonts);
			
			// Create a panel
			JPanel temp = new JPanel();
			temp.setLayout(new BorderLayout());
			temp.add(label);
			temp.setPreferredSize(new Dimension(40,20));
			
			// add it to the main panel
			entryPane.add(temp);
		}
		else
		{
			JEXStatics.logManager.log("Rebuilding the entry panel: found "+entryList.size()+" entries", 2, this);
			
			// create the list of panels
			panelList = new ArrayList<JEXEntryPanelLine>(0);

			// Create the entry map
			TreeMap<String,TreeMap<String,TreeSet<JEXEntry>>> entryMap = sortEntries(entryList);
			List<String> expNames = new Vector<String>();
			expNames.addAll(entryMap.keySet());
			StringUtility.sortStringList(expNames);
			
			// Loop through the entryMap
			for (String expName: expNames)
			{
				// Create the experiment label
				JLabel expLabel = new JLabel(expName);
				expLabel.setFont(FontUtility.boldFont);
				expLabel.setAlignmentX(LEFT_ALIGNMENT);
				entryPane.add(expLabel);
				
				// Loop through the trays
				TreeMap<String,TreeSet<JEXEntry>> trayMap = entryMap.get(expName);
				for (String trayName: trayMap.keySet())
				{
					// Create the tray label
					JLabel arrayLabel = new JLabel("     "+trayName);
					arrayLabel.setFont(FontUtility.boldFont);
					arrayLabel.setAlignmentX(LEFT_ALIGNMENT);
					entryPane.add(arrayLabel);
					
					// Loop through the entries
					TreeSet<JEXEntry> theEntries = trayMap.get(trayName);
					for (JEXEntry e: theEntries){
						
						// Create a object panel line to display the object type name
						JEXEntryPanelLine newobjectPanel = new JEXEntryPanelLine(e);
						newobjectPanel.setAlignmentX(LEFT_ALIGNMENT);
						
						// add it to the main panel and the object panel list
						panelList.add(newobjectPanel);
						entryPane.add(newobjectPanel);
					}
					
					// add a spacer for visual purposes
					entryPane.add(Box.createVerticalStrut(5));
				}
				
				// add a spacer for visual purposes
				entryPane.add(Box.createVerticalStrut(10));
			}
		}
		entryPane.add(Box.createVerticalGlue());

		// repaint the main panel
		refresh();
		entryPane.invalidate();
		entryPane.validate();
		entryPane.repaint();
	}

	/**
	 * Refresh the objectpanel list in case the selection has changed
	 */
	public void refresh(){
		JEXStatics.logManager.log("Refreshing object Panel", 2, this);
		for (JEXEntryPanelLine o: panelList){
			o.refresh();
		}
	}
	
	/**
	 * Create a entry map sorting entries with exp Name and Tray Name
	 * @param entries
	 * @return
	 */
	private TreeMap<String,TreeMap<String,TreeSet<JEXEntry>>> sortEntries(TreeSet<JEXEntry> entries)
	{
		// Create the tree map
		TreeMap<String,TreeMap<String,TreeSet<JEXEntry>>> entryMap = new TreeMap<String,TreeMap<String,TreeSet<JEXEntry>>>();
		
		// Loop through the entries and add the entries in the map
		for (JEXEntry entry: entries)
		{
			// Get the keys
			String expName   = entry.getEntryExperiment();
			String arrayname = entry.getEntryTrayName();
			
			// Place the entry in the map and create the objects if necessary
			TreeMap<String,TreeSet<JEXEntry>> trayMap = entryMap.get(expName);
			if (trayMap == null)
			{
				trayMap = new TreeMap<String,TreeSet<JEXEntry>>();
				entryMap.put(expName, trayMap);
			}
			
			TreeSet<JEXEntry> theEntries = trayMap.get(arrayname);
			if (theEntries == null)
			{
				theEntries = new TreeSet<JEXEntry>();
				trayMap.put(arrayname, theEntries);
			}
			
			theEntries.add(entry);
		}
		
		return entryMap;
	}

	////// ACTIONS
	
	public void doAction(String actionString){}
	
	/**
	 * Call when object list has changed
	 */
	public void entriesChanged(){
		rebuild();
	}
	
	public void actionPerformed(ActionEvent e){}

	public void diplayPanel() {}

	public void stopDisplayingPanel() {}



}
