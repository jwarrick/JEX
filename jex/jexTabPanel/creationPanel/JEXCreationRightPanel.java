package jex.jexTabPanel.creationPanel;

import guiObject.DialogGlassPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import jex.ErrorMessagePane;
import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import utilities.DateUtility;
import utilities.FileUtility;
import utilities.FontUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.JEXLabel;
import Database.Definition.HierarchyLevel;
import Database.Definition.Tray;

public class JEXCreationRightPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	// Model
	private HierarchyLevel hlevel;
	
	// GUI description
	private Color foregroundColor = DisplayStatics.lightBackground;
	
	// GUI Elements
	private JLabel title1, title2;
	private JPanel headerPane1, headerPane2;
	private JPanel createObjectPanel, editObjectPanel;
	
	// NEW ARRAY FIELDS
	JTextArea expName    = new JTextArea("New experiment");
	JTextArea expInfo    = new JTextArea("No Info");
	JTextArea expDate    = new JTextArea(DateUtility.getDate());
	JTextArea trayName   = new JTextArea("New Array");
	JTextArea trayWidth  = new JTextArea("1");
	JTextArea trayHeight = new JTextArea("1");
	private JButton createButton;
	
	// EDITING ARRAY FIELDS
	JTextArea viewedExpName    = new JTextArea("New experiment");
	JTextArea viewedExpInfo    = new JTextArea("No Info");
	JTextArea viewedExpDate    = new JTextArea(DateUtility.getDate());
	JTextArea viewedTrayName   = new JTextArea("New Array");
	JTextArea viewedTrayWidth  = new JTextArea("1");
	JTextArea viewedTrayHeight = new JTextArea("1");
	private JButton editButton;
	private JButton deleteButton;
	private JButton consolidateButton;
	
	// DULPICATING ARRAY FIELDS
	List<JCheckBox> labelsToDuplicate = new ArrayList<JCheckBox>();
	private JButton duplicateButton;
	

	JEXCreationRightPanel()
	{
		initialize();
			
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "navigationChanged", (Class[])null);
		
	}
	
	/**
	 * Detach the signals
	 */
	public void deInitialize()
	{
		SSCenter.defaultCenter().disconnect(this);
	}
	
	private void initialize(){
		// Initialize buttons
		createButton = new JButton("Create Array");
		createButton.addActionListener(this);
		editButton = new JButton("Edit");
		editButton.addActionListener(this);
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(this);
		consolidateButton = new JButton("Clean up files");
		consolidateButton.addActionListener(this);
		duplicateButton = new JButton("Duplicate");
		duplicateButton.addActionListener(this);
		
		this.setBackground(foregroundColor);
		this.setLayout(new BorderLayout());
		rebuild();
	}

	public void rebuild(){
		// Build the selector header
		title1 = new JLabel("CREATE NEW ARRAYS");
		headerPane1 = new JPanel(new MigLayout("flowy,center,ins 1","[center]","[center]"));
		headerPane1.setBackground(DisplayStatics.menuBackground);
		title1.setFont(FontUtility.boldFont);
		headerPane1.add(title1);
		
		// Build the quickSelector
		createObjectPanel = makeCreateObjectPanel();
		
		// Build the selector header
		title2 = new JLabel("EDIT / MANAGE ARRAYS");
		title2.setFont(FontUtility.boldFont);
		headerPane2 = new JPanel(new MigLayout("flowy,center,ins 1","[center]","[center]"));
		headerPane2.setBackground(DisplayStatics.menuBackground);
		headerPane2.add(title2);
		
		// Build the objects panel
		editObjectPanel = makeEditObjectPanel();
		
		// Remove all
		this.removeAll();
		
		// Place the objects 
		this.setBackground(DisplayStatics.lightBackground);
		this.setLayout(new MigLayout("center,flowy,ins 2","[center,fill,grow]","[]1[0:0,fill,grow 60]1[]1[0:0,grow 40]"));
		this.add(headerPane1,"growx");
		this.add(createObjectPanel,"grow");
		this.add(headerPane2,"growx");
		this.add(editObjectPanel,"grow");

		// revalidate
		this.revalidate();
		this.repaint();
	}

	public JPanel makeCreateObjectPanel()
	{
		JPanel result = new JPanel();
		
		// make the labels
		JLabel expNameLabel = new JLabel("Exp. Name");
		JLabel expInfoLabel = new JLabel("Exp. Info");
		JLabel expDateLabel = new JLabel("Date");
		JLabel trayNameLabel = new JLabel("Array Name");
		JLabel trayWidthLabel = new JLabel("Array Width");
		JLabel trayHeightLabel = new JLabel("Array Height");
		
		// Make the string to preset in the fields
		String expNameStr    = (hlevel != null) ? hlevel.getRepresentativeEntry().getEntryExperiment() : "New Experiment";
		String expInfoStr    = (hlevel != null) ? hlevel.getRepresentativeEntry().getEntryExperimentInfo() : "No Info";
		String expDateStr    = (hlevel != null) ? hlevel.getRepresentativeEntry().getDate() : DateUtility.getDate();
		String trayNameStr   = (hlevel instanceof Tray) ? hlevel.getRepresentativeEntry().getEntryTrayName() : "New Array";
		String trayWidthStr  = (hlevel instanceof Tray) ? ""+((Tray)hlevel).getSublevelArray().size() : "1";
		String trayHeightStr = (hlevel instanceof Tray) ? ""+((Tray)hlevel).getSublevelArray().get("0").size() : "1";
				
		// Make the text fields
		expName    = new JTextArea(expNameStr);
		expInfo    = new JTextArea(expInfoStr);
		expDate    = new JTextArea(expDateStr);
		trayName   = new JTextArea(trayNameStr);
		trayWidth  = new JTextArea(trayWidthStr);
		trayHeight = new JTextArea(trayHeightStr);
		
		// Set the layout
		result.setLayout(new MigLayout("ins 0","2[]2[]2",""));
		result.add(expNameLabel,"gapy5, width 50%");
		result.add(expName,"width 50%,wrap, width 50:200:500");
		result.add(expInfoLabel,"width 50%");
		result.add(expInfo,"width 50%,wrap, width 50:200:500");
		result.add(expDateLabel,"width 50%");
		result.add(expDate,"width 50%,wrap, width 50:200:500");
		result.add(trayNameLabel,"width 50%");
		result.add(trayName,"width 50%,wrap, width 50:200:500");
		result.add(trayWidthLabel,"width 50%");
		result.add(trayWidth,"width 50%,wrap, width 50:200:500");
		result.add(trayHeightLabel,"width 50%");
		result.add(trayHeight,"width 50%,wrap, width 50:200:500");
		result.add(createButton,"width 100%, span 2, wrap, width 50:400:500");
		
		// Duplication
		if (hlevel != null && hlevel instanceof Tray)
		{
			JLabel duplicateLabel = new JLabel("Duplicate the selected array:");
			duplicateLabel.setFont(FontUtility.italicFonts);
			result.add(duplicateLabel,"width 100%, gapy 10, span 2,wrap, width 50:200:500");
			
			// Add the labels?
			Set<JEXEntry> entries = hlevel.getEntries();
			TreeMap<String,TreeSet<String>> labels = JEXStatics.jexManager.getAvailableLabels(entries);

			// Make the checkBoxes
			labelsToDuplicate = new ArrayList<JCheckBox>();
			result.add(new JLabel("Selected labels to duplicate"),"width 100%, span 2,wrap, width 50:200:500");

			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new MigLayout("ins 5, flowy","[]",""));
			labelPanel.setBackground(DisplayStatics.lightBackground);
			for (String labelName: labels.keySet())
			{
				JCheckBox labelBox = new JCheckBox(labelName);
				labelsToDuplicate.add(labelBox);
				labelPanel.add(labelBox,"width 100%, width 50:200:500");
			}

			javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(labelPanel);
			result.add(scroll,"width 100%, growy, span 2,wrap, width 50:400:500");
			result.add(duplicateButton,"width 100%, span 2, width 50:400:500");
		}

		return result;
	}

	public JPanel makeEditObjectPanel()
	{
		JPanel result = new JPanel();

		// make the labels
		JLabel viewedExpNameLabel = new JLabel("Exp. Name");
		JLabel viewedExpInfoLabel = new JLabel("Exp. Info");
		JLabel viewedExpDateLabel = new JLabel("Date");
		JLabel viewedTrayNameLabel = new JLabel("Array Name");
		JLabel viewedTrayWidthLabel = new JLabel("Array Width");
		JLabel viewedTrayHeightLabel = new JLabel("Array Height");
		
		// Make the string to preset in the fields
		String expNameStr    = (hlevel != null) ? hlevel.getRepresentativeEntry().getEntryExperiment() : "New Experiment";
		String expInfoStr    = (hlevel != null) ? hlevel.getRepresentativeEntry().getEntryExperimentInfo() : "No Info";
		String expDateStr    = (hlevel != null) ? hlevel.getRepresentativeEntry().getDate() : DateUtility.getDate();
		String trayNameStr   = (hlevel instanceof Tray) ? hlevel.getRepresentativeEntry().getEntryTrayName() : "New Array";
		String trayWidthStr  = (hlevel instanceof Tray) ? ""+((Tray)hlevel).getSublevelArray().size() : "1";
		String trayHeightStr = (hlevel instanceof Tray) ? ""+((Tray)hlevel).getSublevelArray().get("0").size() : "1";

		// Make the text fields
		viewedExpName    = new JTextArea(expNameStr);
		viewedExpInfo    = new JTextArea(expInfoStr);
		viewedExpDate    = new JTextArea(expDateStr);
		viewedTrayName   = new JTextArea(trayNameStr);
		viewedTrayWidth  = new JTextArea(trayWidthStr);
		viewedTrayHeight = new JTextArea(trayHeightStr);
		
		// Set the layout
		if (hlevel != null)
		{
			result.setLayout(new MigLayout("ins 0","2[]2[]2",""));
			result.add(viewedExpNameLabel,"gapy5, width 50%");
			result.add(viewedExpName,"width 50%,wrap, width 50:200:500");
			result.add(viewedExpInfoLabel,"width 50%");
			result.add(viewedExpInfo,"width 50%,wrap, width 50:200:500");
			result.add(viewedExpDateLabel,"width 50%");
			result.add(viewedExpDate,"width 50%,wrap, width 50:200:500");
			if (hlevel instanceof Tray)
			{
				result.add(viewedTrayNameLabel,"width 50%");
				result.add(viewedTrayName,"width 50%,wrap, width 50:200:500");
				result.add(viewedTrayWidthLabel,"width 50%");
				result.add(viewedTrayWidth,"width 50%,wrap, width 50:200:500");
				result.add(viewedTrayHeightLabel,"width 50%");
				result.add(viewedTrayHeight,"width 50%,wrap, width 50:200:500");
			}
			result.add(editButton,"width 50%");
			result.add(deleteButton,"width 50%, wrap, width 50:200:500");

			JLabel consolidateLabel = new JLabel("Clean up non-linked files in the finder:");
			consolidateLabel.setFont(FontUtility.italicFonts);
			result.add(consolidateLabel,"width 100%, gapy 10, span 2,wrap, width 50:200:500");
			result.add(consolidateButton,"width 100%, span 2, width 50:400:500");
			
		}

		return result;
	}
	

	// ----------------------------------------------------
	// --------- OPERATIONS FUNCTIONS -----------------
	// ----------------------------------------------------
	
	public void createArray()
	{
		JEXStatics.logManager.log("Creating new data sets", 1, this);
		
		String expNameStr = expName.getText();
		String expInfoStr = expInfo.getText();
		String dateStr = expDate.getText();
		String trayNameStr = trayName.getText();
		String arrayWidthStr = trayWidth.getText();
		String arrayHeightStr = trayHeight.getText();
		int w = Integer.parseInt(arrayWidthStr);
		int h = Integer.parseInt(arrayHeightStr);
		
		JEXStatics.jexManager.createEntryArray(expNameStr, trayNameStr, dateStr, expInfoStr, w, h);
	}
	
	public void editArray()
	{
		JEXStatics.logManager.log("Editing data sets", 1, this);
		
		// Get the new values requested
		String expNameStr = FileUtility.removeWhiteSpaceOnEnds(viewedExpName.getText());
		String expInfoStr = FileUtility.removeWhiteSpaceOnEnds(viewedExpInfo.getText());
		String dateStr = FileUtility.removeWhiteSpaceOnEnds(viewedExpDate.getText());
		String trayNameStr = FileUtility.removeWhiteSpaceOnEnds(viewedTrayName.getText());
		
		// Get the entries
		if (hlevel == null) return;
		TreeSet<JEXEntry> entries = hlevel.getEntries();
		
		// Do the changes
		JEXStatics.jexDBManager.editHeirarchyForEntries(entries, expNameStr, trayNameStr, expInfoStr, dateStr);
				
	}

	public void deleteArray()
	{
		// Get the entries
		if (hlevel == null) return;
		Set<JEXEntry> entries = hlevel.getEntries();
		
		// If the pre-existence flag is true, then issue a warning message
		JEXStatics.logManager.log("Are you sure you want to remove the entries", 1, this);

		DialogGlassPane diagPanel = new DialogGlassPane("Warning");
		diagPanel.setSize(400, 200);

		ErrorMessagePane errorPane = new ErrorMessagePane("Are you sure you want to delete the entries... ");
		diagPanel.setCentralPanel(errorPane);

		JEXStatics.main.displayGlassPane(diagPanel,true);
		
		// remove the entries
		JEXStatics.jexDBManager.removeEntries(entries);
	}
	
	public void duplicateArray()
	{
		if (!(hlevel instanceof Tray)) return;
		
		// Create a new array
		String expNameStr     = expName.getText();
		String expInfoStr     = expInfo.getText();
		String dateStr        = expDate.getText();
		String trayNameStr    = trayName.getText();
		String arrayWidthStr  = trayWidth.getText();
		String arrayHeightStr = trayHeight.getText();
		int w = Integer.parseInt(arrayWidthStr);
		int h = Integer.parseInt(arrayHeightStr);
		JEXEntry[][] array = JEXStatics.jexManager.createEntryArray(expNameStr, trayNameStr, dateStr, expInfoStr, w, h);
		
		
		//////////////////////////
		// Gather the labels and prepare copied versions for addition to database
		
		// Fill it with the labels requested
		Set<String> labelToDuplicate = new HashSet<String>();
		for (JCheckBox jcb: labelsToDuplicate)
		{
			if (jcb.isSelected())
			{
				String labelName = jcb.getText();
				labelToDuplicate.add(labelName);
			}
		}
		
		// Label map
		TreeMap<JEXEntry,Set<JEXData>> dataArray = new TreeMap<JEXEntry,Set<JEXData>>();
		
		// Loop through the arrays and add the  labels
		Set<JEXEntry> entries = hlevel.getEntries();
		for (JEXEntry entry: entries)
		{
			// Get the x y location of the entry
			int x = entry.getTrayX();
			int y = entry.getTrayY();
			
			// Get the labels
			TreeMap<String,String> labels = JEXStatics.jexManager.getAvailableLabels(entry);
			
			// Place in the new array
			JEXEntry newEntry = array[x][y];
			
			// Make the label map
			Set<JEXData> labelMap = new HashSet<JEXData>();
			
			// Make the labels
			for (String labelName: labels.keySet())
			{
				JEXLabel label = new JEXLabel(labelName, labels.get(labelName), "");
				labelMap.add(label);
			}
			
			// Put in saving list
			dataArray.put(newEntry, labelMap);
		}
		
		// Add the compiled objects to the database
		JEXStatics.jexDBManager.saveDataListInEntries(dataArray, true);
	}
	
	public void consolidateArray()
	{
		// Get the entries
		if (hlevel == null) return;
		
		// Consolidating entries of the selected hierarchy level
		JEXStatics.logManager.log("Not Implemented Yet", 1, this);
	}
	
	// ----------------------------------------------------
	// --------- EVENT FUNCTIONS -----------------
	// ----------------------------------------------------
	
	public void viewedHierarchyLevelChange(HierarchyLevel hlevel)
	{
		JEXStatics.logManager.log("Signal change for viewing a different hierarchy level received", 1, this);
		this.hlevel = hlevel;
		rebuild();
	}
	
	public void navigationChanged() {}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == createButton){
			createArray();
		}
		else if (e.getSource() == editButton){
			editArray();
		}
		else if (e.getSource() == deleteButton)
		{
			deleteArray();
		}
		else if (e.getSource() == duplicateButton)
		{
			duplicateArray();
		}
		else if (e.getSource() == consolidateButton)
		{
			consolidateArray();
		}
	}
	
	
}