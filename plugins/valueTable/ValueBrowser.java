package plugins.valueTable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JPanel;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import plugins.plugin.PlugIn;
import plugins.plugin.PlugInController;
import plugins.viewer.EntryMenu;
import signals.SSCenter;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXDataSingle;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.JEXLabel;
import Database.Definition.TypeName;

public class ValueBrowser implements PlugInController
{
	
	// Variables
	private Vector<JEXEntry> 					entries ;

	// Gui
	private PlugIn dialog ;
	private EntryMenu entryMenu;
	
	private ValueTable valueTable;
	
////////////////////////////////////////
//////////// Constructors //////////////
////////////////////////////////////////
	
	public ValueBrowser()
	{
		initizalizeDialog();
		initialize();
		
		this.setVisible(true);

		// Make appropriate connections
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.AVAILABLEOBJ, this, "viewedEntryChanged", (Class[])null);
	}
	
	public ValueBrowser(TreeSet<JEXEntry> entries, TypeName tn)
	{	
		this();
		this.setDBSelection(entries, tn);
	}
	
////////////////////////////////////////
////////////Initializers  //////////////
////////////////////////////////////////
	
	private void initialize()
	{
		
	}

	private void initizalizeDialog()
	{
		this.dialog = new PlugIn(this);
		this.dialog.setBounds(100, 100, 800, 600);
		this.dialog.setDefaultCloseOperation(PlugIn.DISPOSE_ON_CLOSE);
		this.dialog.getContentPane().setBackground(DisplayStatics.background);
		this.dialog.getContentPane().setLayout(new BorderLayout());
		this.dialog.getContentPane().add(new JPanel(), BorderLayout.CENTER);
	}
	
	private void drawNull()
	{
		this.dialog.getContentPane().removeAll();
		this.dialog.getContentPane().add(new JPanel(), BorderLayout.CENTER);
		this.dialog.getContentPane().invalidate();
		this.dialog.getContentPane().validate();
		this.dialog.getContentPane().repaint();
	}

	private void drawTable()
	{
		this.dialog.getContentPane().removeAll();
		
		javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(this.valueTable);
		scroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		this.dialog.getContentPane().add(scroll, BorderLayout.CENTER);
		this.dialog.getContentPane().invalidate();
		this.dialog.getContentPane().validate();
		this.dialog.getContentPane().repaint();
	}
	
////////////////////////////////////////
////////////   Actions    //////////////
////////////////////////////////////////
	
	public void setVisible(boolean visible)
	{
		this.dialog.setVisible(visible);
	}
	
	public void setDBSelection(TreeSet<JEXEntry> entries, TypeName tn)
	{
		this.entries = new Vector<JEXEntry>(entries);
		
		viewedEntryChanged();

		this.dialog.validate();
		this.dialog.repaint();
	}
	
	public JEXEntry currentEntry()
	{
//		int entryIndex = this.dataBrowser.currentEntry();
		int entryIndex = 0;
		if(this.entries == null) return null;
		return this.entries.get(entryIndex);
	}
	
	/**
	 * Display a panel on the glass pane
	 * @param pane
	 * @return
	 */
	public boolean displayGlassPane(JPanel pane, boolean on){
		
		if (pane == null){
			Component c = this.dialog.getGlassPane();
			c.setVisible(false);
			return false;
		}
		
		if (on){
			pane.setOpaque(true); 
			this.dialog.setGlassPane(pane);
			pane.setVisible(true);
			return true;
		}
		else {
			pane.setOpaque(false); 
			this.dialog.setGlassPane(pane);
			pane.setVisible(false);
			return true;
		}
		
	}
	
	public void toggleEntryValid()
	{
		JEXData validLabel = JEXStatics.jexManager.getDataOfTypeNameInEntry(new TypeName(JEXData.LABEL,JEXEntry.VALID), this.currentEntry());
		JEXLabel newValidLabel = new JEXLabel(validLabel.getTypeName().getName(), validLabel.getFirstSingle().get(JEXDataSingle.VALUE), validLabel.getFirstSingle().get(JEXDataSingle.UNIT));
		Boolean isValid = new Boolean(newValidLabel.getLabelValue());
		if(isValid)
		{
			newValidLabel.setLabelValue("false");
			this.entryMenu.setValid(false);
			JEXStatics.jexManager.setEntryValid(this.currentEntry(), false);
		}
		else
		{
			newValidLabel.setLabelValue("true");
			this.entryMenu.setValid(true);
			JEXStatics.jexManager.setEntryValid(this.currentEntry(), true);
		}
	}

////////////////////////////////////////
////////////   Reactions  //////////////
////////////////////////////////////////
	
	public void viewedEntryChanged()
	{
		// get the entry
		JEXEntry entry = this.currentEntry();
		if (entry == null) drawNull();
		
		// get the type name of the value to display
		TypeName tn = JEXStatics.jexManager.getSelectedObject();
		if (tn == null || !tn.getType().equals(JEXData.VALUE)) drawNull();
		
		// Get the jexdata
		JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(tn, entry);
		if (data == null) drawNull();
		
		// Make the value table
		valueTable = new ValueTable();
		valueTable.setDataToView(data);
		valueTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		valueTable.setCellSelectionEnabled(true);
		drawTable();
	}
	
	public void entryChanged()
	{
		
	}
	
////////////////////////////////////////
//////////// PlugIn Stuff //////////////
////////////////////////////////////////
	
	/**
	 * Called upon closing of the window
	 */
	public void finalizePlugIn()
	{
		
	}
	
	public PlugIn plugIn()
	{
		return this.dialog;
	}
}
