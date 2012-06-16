package jex.objectAndEntryPanels;

import guiObject.DialogGlassPane;
import guiObject.FlatRoundedButton;
import guiObject.FormGlassPane;
import icons.IconRepository;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import utilities.FontUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.tnvi;

public class JEXDataPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	// Model variables
	private tnvi objectList;
	private List<JEXDataPanelLine> panelList;

	// Main gui components
	private JScrollPane        scroll;
	private JPanel             objectPane;
	private JPanel             panel;
	
	// Buttons
	private JPanel             buttonPane;
	private FlatRoundedButton  editButton ;
	private FlatRoundedButton  deleteButton ;
	private FlatRoundedButton  duplicateButton ;
	
	// Where to get the objects
	Object objectSource ;
	String objectSourceMethod ;

	// Where to delete the objects
	Object objectDeletor ;
	String objectDeletorMethod ;

	// Where to edit the objects
	Object objectEditor ;
	String objectEditorMethod ;

	// Where to duplicate the objects
	Object objectDuplicator ;
	String objectDuplicatorMethod ;
	
	/**
	 * Create a new JEXData list
	 * @param parent
	 */
	public JEXDataPanel(tnvi objectList){
		super();
		
		// Initialize variables
		this.objectList = objectList;
		this.panelList = new ArrayList<JEXDataPanelLine>(0);

		objectPane = new JPanel();
		objectPane.setBackground(DisplayStatics.lightBackground);
		objectPane.setLayout(new MigLayout("flowy,ins 3 3 3 3, gap 0","[fill,grow]","[]"));
		
		scroll = new JScrollPane(objectPane);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		// Setup updating links
		SSCenter.defaultCenter().connect(JEXStatics.jexDBManager, JEXManager.EXPERIMENTTREE_UPDATE, this, "rebuild", (Class[])null);
		
		rebuild();
	}
	
	public JEXDataPanel(){
		super();
		
		// Initialize variables
		this.objectList = null;
		this.panelList = new ArrayList<JEXDataPanelLine>(0);
		
		// Make the button panel
		initializeButtonPanel();

		objectPane = new JPanel();
		objectPane.setBackground(DisplayStatics.lightBackground);
		objectPane.setLayout(new MigLayout("flowy,ins 3 3 3 3, gap 0","[fill,grow]1","[]"));
		
		scroll = new JScrollPane(objectPane);
		scroll.setBorder(BorderFactory.createEmptyBorder());

		// Setup updating links
		SSCenter.defaultCenter().connect(JEXStatics.jexDBManager, JEXManager.EXPERIMENTTREE_UPDATE, this, "rebuild", (Class[])null);
		
		rebuild();
	}
	
	private void initializeButtonPanel()
	{
		buttonPane = new JPanel();
		buttonPane.setBackground(DisplayStatics.lightBackground);
		buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.LINE_AXIS));
		buttonPane.setLayout(new MigLayout("flowx, ins 0","[fill,grow][25][25][25]","25"));
		
		editButton               = new FlatRoundedButton(null);
		editButton.background    = DisplayStatics.lightBackground;
		editButton.mouseOverBack = DisplayStatics.menuBackground;
		editButton.normalBack    = DisplayStatics.lightBackground;
		editButton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PREFERENCES, 20, 20));
		editButton.addActionListener(this);
		editButton.setToolTipText("Edits the selected data from the selected entries");
		editButton.refresh();
		
		duplicateButton = new FlatRoundedButton(null);
		duplicateButton.background    = DisplayStatics.lightBackground;
		duplicateButton.mouseOverBack = DisplayStatics.menuBackground;
		duplicateButton.normalBack    = DisplayStatics.lightBackground;
		duplicateButton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_PLUS_GREY, 20, 20));
		duplicateButton.addActionListener(this);
		duplicateButton.setToolTipText("Duplicates the selected data from the selected entries");
		duplicateButton.refresh();
		
		deleteButton    = new FlatRoundedButton(null);
		deleteButton.background    = DisplayStatics.lightBackground;
		deleteButton.mouseOverBack = DisplayStatics.menuBackground;
		deleteButton.normalBack    = DisplayStatics.lightBackground;
		deleteButton.setIcon(JEXStatics.iconRepository.getIconWithName(IconRepository.MISC_DELETE, 20, 20));
		deleteButton.addActionListener(this);
		deleteButton.setToolTipText("Removes the selected data from the selected entries");
		deleteButton.refresh();
		
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.add(editButton.panel());
		buttonPane.add(duplicateButton.panel());
		buttonPane.add(deleteButton.panel());
	}
	
	public void whereToGetObjects(Object source, String methodToCall)
	{
		this.objectSource       = source ;
		this.objectSourceMethod = methodToCall ;
	}
	
	public void whenToGetObjects(Object source, String signalName, String methodToCall)
	{
		// Setup updating links
		SSCenter.defaultCenter().connect(source, signalName, this, methodToCall, (Class[])null);
	}
	
	public void whereToRemoveObjects(Object objectDeletor, String objectDeletorMethod)
	{
		this.objectDeletor       = objectDeletor ;
		this.objectDeletorMethod = objectDeletorMethod ;
	}
	
	public void whereToEditObjects(Object objectEditor, String objectEditorMethod)
	{
		this.objectEditor       = objectEditor ;
		this.objectEditorMethod = objectEditorMethod ;
	}
	
	public void whereToDuplicateObjects(Object objectDuplicator, String objectDuplicatorMethod)
	{
		this.objectDuplicator       = objectDuplicator ;
		this.objectDuplicatorMethod = objectDuplicatorMethod ;
	}
	
	public JScrollPane scroll()
	{
		return this.scroll;
	}
	
	public JPanel panel()
	{
		panel = new JPanel();
		panel.setBackground(DisplayStatics.background);
		panel.setLayout(new BorderLayout());

		panel.add(scroll,BorderLayout.CENTER);
		panel.add(buttonPane,BorderLayout.PAGE_END);
		
		return panel;
	}
	
	/**
	 * Rebuild the objectpanel list in case the array has changed
	 */
	public void rebuild(){
		JEXStatics.logManager.log("Rebuilding the object Panel", 2, this);
		objectPane.removeAll();

		// If the object list is empty or null
		// then add a message signaling it to the user
		if (objectList == null || objectList.size() == 0)
		{
			// create a label
			JLabel label = new JLabel("No objects");
			label.setFont(FontUtility.italicFonts);
			
			// Create a panel
			JPanel temp = new JPanel();
			temp.setLayout(new BorderLayout());
			temp.add(label);
			temp.setPreferredSize(new Dimension(20,20));
			
			// add it to the main panel
			objectPane.add(temp);
		}
		else
		{
			// create the list of panels
			panelList = new ArrayList<JEXDataPanelLine>(0);

			// loop through the list of objects
			for (String type: objectList.keySet()){
				
				// if the object is a hierarchy object, do not display it... 
				if (type.equals(JEXData.HIERARCHY)) continue;
				
				// Create a label indicating a new type of object in the list
				JLabel typeLabel = new JLabel(type);
				typeLabel.setFont(FontUtility.italicFonts);
				
				// Create a panel with the label inside it
				JPanel spacerkey = new JPanel();
				spacerkey.setBackground(DisplayStatics.lightBackground);
				spacerkey.setLayout(new BoxLayout(spacerkey,BoxLayout.LINE_AXIS));
				spacerkey.add(typeLabel);
				spacerkey.add(Box.createHorizontalGlue());
				
				// add it to the main panel
				objectPane.add(spacerkey);
				
				// Loop through the object names
				for (String name: objectList.get(type).keySet()){
					
					// if there are no objects of that type name, then pass
					TreeMap<String,Set<JEXEntry>> dataSet = objectList.get(type).get(name);
					if (dataSet.size() == 0) continue;

					// Create a object panel line to display the object type name
					JEXDataPanelLine newobjectPanel = new JEXDataPanelLine(new TypeName(type,name), this);
					
					// add it to the main panel and the object panel list
					panelList.add(newobjectPanel);
					objectPane.add(newobjectPanel,"growx");
				}
			}
		}

		this.objectPane.revalidate();
		this.objectPane.repaint();
	}
	
	////// ACTIONS
	
	public void duplicateObject(FormGlassPane formPane)
	{
		if (formPane == null) return;
		
		// Get the form
		JEXStatics.logManager.log("Duplicating the object", 1, this);
		LinkedHashMap<String,String> form = (LinkedHashMap<String,String>) formPane.form();
		
		// get the new name and info
		String objectName = form.get("Object name");
		String objectInfo = form.get("Object info");
		
		// make the objects
		Set<JEXEntry> selectedEntries = JEXStatics.jexManager.getSelectedEntries();
		TypeName      selectedTN      = JEXStatics.jexManager.getSelectedObject();
		HashMap<JEXEntry,JEXData> dataArray = new HashMap<JEXEntry,JEXData>();
		for (JEXEntry entry: selectedEntries)
		{
			JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(selectedTN, entry);
			JEXData newData = new JEXData(data);
			newData.setDataObjectName(objectName);
			newData.setDataObjectInfo(objectInfo);
			newData.setDataObjectDate(utilities.DateUtility.getDate());
			newData.setDataObjectModifDate(utilities.DateUtility.getDate());
			
			dataArray.put(entry, newData);
		}
		
		// Call the duplicate method from the object that controls duplications
		try {
			Method method = objectDuplicator.getClass().getMethod(objectDuplicatorMethod, dataArray.getClass());
			method.invoke(objectDuplicator, dataArray);
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			JEXStatics.logManager.log("Couldn't find method to create the desired connection", 0, this);
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
	}
	
	public void editObject(FormGlassPane formPane)
	{
		if (formPane == null) return;
		
		// Get the form
		JEXStatics.logManager.log("Editing the object", 1, this);
		LinkedHashMap<String,String> form = (LinkedHashMap<String,String>) formPane.form();
		
		// get the new name and info
		String objectName = form.get("New name");
		String objectInfo = form.get("New info");
		
		// make the objects
		Set<JEXEntry> selectedEntries = JEXStatics.jexManager.getSelectedEntries();
		TypeName      selectedTN      = JEXStatics.jexManager.getSelectedObject();
		TreeMap<JEXEntry,JEXData> newDataArray = new TreeMap<JEXEntry,JEXData>();
		TreeMap<JEXEntry,Set<JEXData>> oldDataArray = new TreeMap<JEXEntry,Set<JEXData>>();
		
		for (JEXEntry entry: selectedEntries)
		{
			// Fill the old data array
			JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(selectedTN, entry);
			if(data == null)
			{
				continue;
			}
			HashSet<JEXData> datas = new HashSet<JEXData>();
			datas.add(data);
			oldDataArray.put(entry, datas);
			
			Object[] options = {"OK", "Cancel"};
			JEXData dataToOverwrite = JEXStatics.jexManager.getDataOfTypeNameInEntry(new TypeName(data.getTypeName().getType(), objectName), entry);
			if(dataToOverwrite != null)
			{
				int n = JOptionPane.showOptionDialog(JEXStatics.main,"Overwrite the data with existing name: " + objectName + "?",
						"Export Images",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);
				if(n == 1)
				{
					JEXStatics.logManager.log("Canceling edit to prevent overwriting existing data.", 0, this);
					JEXStatics.statusBar.setStatusText("Canceling object edit to prevent overwriting existing data.");
					return;
				}
			}
			
			// Make the new data array
			JEXData newData = new JEXData(data);
			newData.setDataObjectName(objectName);
			newData.setDataObjectInfo(objectInfo);
			newData.setDataObjectDate(utilities.DateUtility.getDate());
			newData.setDataObjectModifDate(utilities.DateUtility.getDate());
			newDataArray.put(entry, newData);
		}
		
		try {
			// Remove the old data
			Method method = objectDeletor.getClass().getMethod(objectDeletorMethod, oldDataArray.getClass());
			method.invoke(objectDeletor, oldDataArray);
			
			// Add the new data
			Method method2 = objectDuplicator.getClass().getMethod(objectDuplicatorMethod, newDataArray.getClass());
			method2.invoke(objectDuplicator, newDataArray);
			
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			JEXStatics.logManager.log("Couldn't find method to create the desired connection", 0, this);
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
	}
	
	public void doAction(String actionString){}
	
	/**
	 * Call when object list has changed
	 */
	public void objectsChanged(){
		
		try {
			Method method = objectSource.getClass().getMethod(objectSourceMethod, (Class<?>[]) null);
			Object result = method.invoke(objectSource);
			
			if (result instanceof tnvi)
			{
				objectList = (tnvi) result;
				rebuild();
			}
			else
			{
				return;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			JEXStatics.logManager.log("Couldn't find method to create the desired connection", 0, this);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == deleteButton)
		{
			// Get the object that is selected and return if there are none
			TypeName selectedTN = JEXStatics.jexManager.getSelectedObject();
			if (selectedTN == null) return;
			
			// Compile the list of data to remove
			JEXStatics.logManager.log("Removing the selected object", 1, this);
			Set<JEXEntry> selectedEntries = JEXStatics.jexManager.getSelectedEntries();
			TreeMap<JEXEntry,Set<JEXData>> dataArray = new TreeMap<JEXEntry,Set<JEXData>>();
			for (JEXEntry entry: selectedEntries){
				JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(selectedTN, entry);
				HashSet<JEXData> datas = new HashSet<JEXData>();
				datas.add(data);
				dataArray.put(entry, datas);
			}
			
			// Call the deletion method from the object that controls delections
			try {
				Method method = objectDeletor.getClass().getMethod(objectDeletorMethod, dataArray.getClass());
				method.invoke(objectDeletor, dataArray);
			} catch (SecurityException ex) {
				ex.printStackTrace();
			} catch (NoSuchMethodException ex) {
				JEXStatics.logManager.log("Couldn't find method to create the desired connection", 0, this);
				ex.printStackTrace();
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			}
		}
		else if (e.getSource() == duplicateButton)
		{
			JEXStatics.logManager.log("Opening an object duplication query box", 1, this);

			// get info on the currently selected object
			Set<JEXEntry> selectedEntries = JEXStatics.jexManager.getSelectedEntries();
			TypeName      selectedTN      = JEXStatics.jexManager.getSelectedObject();
			if (selectedTN == null){
				JEXStatics.logManager.log("No object selected", 1, this);
				JEXStatics.statusBar.setStatusText("No object selected");
				return;
			}
			else if (selectedEntries.size() == 0){
				JEXStatics.logManager.log("No entry selected", 1, this);
				JEXStatics.statusBar.setStatusText("No entry selected");
				return;
			}

			// create a form for getting info on the new object
			String newObjectName = JEXStatics.jexDBManager.getUniqueObjectName(selectedEntries, selectedTN.getType(), selectedTN.getName());
			JEXData data         = JEXStatics.jexManager.getDataOfTypeNameInEntry(selectedTN, selectedEntries.iterator().next());
			LinkedHashMap<String,String> form = new LinkedHashMap<String,String>();
			form.put("Object name", newObjectName);
			form.put("Object info", data.getDataObjectInfo());

			// create a form panel
			FormGlassPane formPane = new FormGlassPane(form);
			formPane.callMethodOfClassOnAcceptance(this, "duplicateObject",new Class[]{FormGlassPane.class});

			// create a glass panel
			DialogGlassPane diagPanel = new DialogGlassPane("Duplicate selected object");
			diagPanel.setSize(400, 200);
			diagPanel.setCentralPanel(formPane);

			JEXStatics.main.displayGlassPane(diagPanel,true);
		}
		else if (e.getSource() == editButton)
		{
			JEXStatics.logManager.log("Opening an object Edit query box", 1, this);

			// get info on the currently selected object
			Set<JEXEntry> selectedEntries = JEXStatics.jexManager.getSelectedEntries();
			TypeName      selectedTN      = JEXStatics.jexManager.getSelectedObject();
			if (selectedTN == null){
				JEXStatics.logManager.log("No object selected", 1, this);
				JEXStatics.statusBar.setStatusText("No object selected");
				return;
			}
			else if (selectedEntries.size() == 0){
				JEXStatics.logManager.log("No entry selected", 1, this);
				JEXStatics.statusBar.setStatusText("No entry selected");
				return;
			}

			// create a form for getting info on the new object
			String newObjectName = JEXStatics.jexDBManager.getUniqueObjectName(selectedEntries, selectedTN.getType(), selectedTN.getName());
			JEXData data         = JEXStatics.jexManager.getExampleOfDataWithTypeNameInEntries(selectedTN, selectedEntries);
			LinkedHashMap<String,String> form = new LinkedHashMap<String,String>();
			form.put("New name", newObjectName);
			form.put("New info", data.getDataObjectInfo());

			// create a form panel
			FormGlassPane formPane = new FormGlassPane(form);
			formPane.callMethodOfClassOnAcceptance(this, "editObject",new Class[]{FormGlassPane.class});

			// create a glass panel
			DialogGlassPane diagPanel = new DialogGlassPane("Edit selected object");
			diagPanel.setSize(400, 200);
			diagPanel.setCentralPanel(formPane);

			JEXStatics.main.displayGlassPane(diagPanel,true);
		}
	}


}