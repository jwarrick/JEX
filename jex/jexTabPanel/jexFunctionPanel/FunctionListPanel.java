package jex.jexTabPanel.jexFunctionPanel;

import function.CrunchFactory;
import function.ExperimentalDataCrunch;
import function.experimentalDataProcessing.MatlabFunctionClass;
import guiObject.DialogGlassCenterPanel;
import guiObject.DialogGlassPane;
import guiObject.JFormPanel;
import guiObject.JParameterPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cruncher.JEXFunction;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import jex.statics.PrefsUtility;
import net.miginfocom.swing.MigLayout;
import plugins.viewer.ScrollPainter;
import signals.SSCenter;
import utilities.FontUtility;
import Database.Definition.ParameterSet;

public class FunctionListPanel {
	private static final long serialVersionUID = 1L;
	
	// GUI variables
	private JPanel 				 panel;
	private JEXFunctionPanel     parent;
	private JPanel               blocksPanel;
	private FunctionParameterPanel paramPanel;
	private FunctionLoadSaveAddRunPanel    editPanel;
	private ScrollPainter		 scroll;
	private FunctionBlockPanel   selectedFunction;
	
	public int currentFunctionIndex = 0;
	public boolean autoSave = false;
	

	public FunctionListPanel(JEXFunctionPanel parent)
	{
		this.parent = parent;
		setUp();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	private void setUp()
	{
		this.panel = new JPanel();
		this.panel.setBackground(DisplayStatics.background);
		this.panel.setLayout(new MigLayout("flowx,ins 0","[0:0,fill,grow]0[0:0,fill,200]","[0:0,fill,grow]"));
		
		// Create the add button
		editPanel = new FunctionLoadSaveAddRunPanel(this);

		// Create the panel to hold the FunctionBlockPanels
		blocksPanel = new JPanel();
		blocksPanel.setBackground(DisplayStatics.background);
		blocksPanel.setLayout(new MigLayout("center,flowx,ins 0","[center,0:0,fill,grow 50]3[center,0:0,fill,grow 50]","[center,0:0,fill,grow]"));
		
		// Create the parameters panel
		this.paramPanel = new FunctionParameterPanel();
		
		// Create the scroll bar
		this.scroll = new ScrollPainter(15,50,false);
		this.scroll.listenToAllWheelEvents = true;
		SSCenter.defaultCenter().connect(this.scroll, ScrollPainter.SIG_IndexChanged_NULL, this, "scrollChanged", (Class[])null);
		
		// Build the panel
		this.panel.add(editPanel.panel(),"growx,south");
		this.panel.add(blocksPanel,"grow, height 100:150:,gap 0 3 3 3");
		this.panel.add(paramPanel.panel(),"grow, height 100:150:,gap 0 0 3 3");
		
		rebuildList();
	}
	
	public void rebuildList()
	{
		blocksPanel.removeAll();
		List<FunctionBlockPanel> functionPanelList = JEXFunctionPanel.functionList; 
		if(this.currentFunctionIndex > functionPanelList.size())
		{
			this.currentFunctionIndex = functionPanelList.size() - 1;
		}
		if(functionPanelList.size() > 2)
		{
			this.scroll.setVerticalOffset(1);
			this.scroll.setHeight(18);
			blocksPanel.add(scroll.panel(),"growx,south");
			
			if(functionPanelList.size() > 0 && this.currentFunctionIndex < (functionPanelList.size()))
			{
				blocksPanel.add(functionPanelList.get(this.currentFunctionIndex).panel(),"grow, width 150::");
			}
			if(functionPanelList.size() > 1 && this.currentFunctionIndex < (functionPanelList.size()-1))
			{
				blocksPanel.add(functionPanelList.get(this.currentFunctionIndex+1).panel(),"grow, width 150::");
			}
		}
		else
		{
			if(functionPanelList.size() > 0)
			{
				blocksPanel.add(functionPanelList.get(0).panel(),"grow, width 150::");
			}
			if(functionPanelList.size() > 1)
			{
				blocksPanel.add(functionPanelList.get(1).panel(),"grow, width 150::");
			}
		}
		
		this.scroll.setNumPositions(this.parent.getFunctionPanels().size()-1);
		this.scroll.setIndex(this.currentFunctionIndex);
		this.scroll.repaint();

		blocksPanel.revalidate();
		blocksPanel.repaint();
	}
	
	public void scrollChanged()
	{
		this.currentFunctionIndex = this.scroll.index();
		this.rebuildList();
	}
	
	public void addFunction()
	{
		JEXStatics.logManager.log("Opening functionlist selection panel", 1, this);

		DialogGlassPane diagPanel = new DialogGlassPane("Choose function");
		diagPanel.setSize(600, 400);

		FunctionChooserPanel flistpane = new FunctionChooserPanel(null);
		diagPanel.setCentralPanel(flistpane);

		JEXStatics.main.displayGlassPane(diagPanel,true);
	}
	
	public void setFunctionIndex(int index)
	{
		if(index < this.parent.getFunctionPanels().size())
		{
			this.currentFunctionIndex = index;
			this.scroll.setIndex(index);
		}
		this.rebuildList();
	}
	
	public void runAllFunctions(boolean autoSave)
	{
		parent.runAllFunctions(true, autoSave);
	}
	
	public void loadFunctionList()
	{
		// Creating file chooser to open user preferences
		JFileChooser fc = new JFileChooser();
		
		// Set the current directory
		String lastPath = PrefsUtility.getLastPath();
		File filepath = new File(lastPath);
		if (filepath.isDirectory()) fc.setCurrentDirectory(filepath);
		else {
			File filefolder = filepath.getParentFile();
			fc.setCurrentDirectory(filefolder);
		}
		
		// Open dialog box
		int returnVal = fc.showOpenDialog(this.panel);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	// Get the file
            File file = fc.getSelectedFile();
            JEXStatics.logManager.log("Loading function list at location "+file.getPath(), 0, this);
            
			// Set the last path opened to the path selected
			PrefsUtility.setLastPath(file.getPath());
                		
    		// Save the function list
			parent.loadFunctionList(file);
			
        } else {
        	JEXStatics.logManager.log("Cannot load the file",0,this);
        }
	}
	
	public void saveFunctionList()
	{
		// Creating file chooser to open user preferences
		JFileChooser fc = new JFileChooser();
		
		// Set the current directory
		String lastPath = PrefsUtility.getLastPath();
		File filepath = new File(lastPath);
		if (filepath.isDirectory()) fc.setCurrentDirectory(filepath);
		else {
			File filefolder = filepath.getParentFile();
			fc.setCurrentDirectory(filefolder);
		}
		
		// Open dialog box
		int returnVal = fc.showSaveDialog(this.panel);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	// Get the file
            File file = fc.getSelectedFile();
            JEXStatics.logManager.log("Saving function list at location "+file.getPath(), 0, this);
            
			// Set the last path opened to the path selected
            PrefsUtility.setLastPath(file.getPath());
                		
    		// Save the function list
			parent.saveFunctionList(file);
			
        } else {
        	JEXStatics.logManager.log("Cannot save the file",0,this);
        }
	}

	public void selectFunction(FunctionBlockPanel fb)
	{
		this.selectedFunction = fb;
		changeSelectedFunction();
	}
	
	public FunctionBlockPanel getSelectedFunction()
	{
		return this.selectedFunction;
	}
	
	private void changeSelectedFunction()
	{
		if(this.selectedFunction ==  null)
		{
			paramPanel.selectFunction(null);
		}
		else
		{
			paramPanel.selectFunction(this.selectedFunction.getFunction());
		}
	}
	
	// ----------------------------------------------------
	// --------- FUNCTION LIST PANEL ----------------------
	// ----------------------------------------------------
	/**
	 * FunctionChooserPanel class
	 */
	class FunctionChooserPanel extends DialogGlassCenterPanel implements ActionListener{
		private static final long serialVersionUID = 1L;

		// Main GUI
		JPanel mainPane = new JPanel();
		
		// GUI function list
		JPanel functionCreationPane = new JPanel();
		JScrollPane treeScrollPane = new JScrollPane(); 
		FunctionTree functionTree ;
		JButton addButton = new JButton("Choose");
		
		// GUI parameter set
		JPanel parameterPane = new JPanel();
		JLabel pTitle = new JLabel();
		JFormPanel pPane = new JFormPanel();
		
		// variables
		private JEXFunction newfunction;
		private JParameterPanel paramPanel;
		
		FunctionChooserPanel(){
			initialize();
		}
		
		FunctionChooserPanel(JEXFunction function){
			initialize();
			
			if (function != null){
				selectFunction(function);
			}
		}
		
		private void initialize(){
			// Main panel
			mainPane.setBackground(DisplayStatics.lightBackground);
			mainPane.setLayout(new BoxLayout(mainPane,BoxLayout.LINE_AXIS));
			
			// Function tree panel
			functionTree = new FunctionTree();
			functionTree.fillTree();
			JPanel treePanel = new JPanel();
			treePanel.setBackground(DisplayStatics.lightBackground);
			treePanel.setLayout(new BorderLayout());
			treePanel.add(functionTree);
			treeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			treeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			treeScrollPane.setViewportView(treePanel);
			addButton.setMaximumSize(new Dimension(250,20));
			addButton.setPreferredSize(new Dimension(250,20));
			addButton.addActionListener(this);
			
			// add mouse listener to function tree
			MouseAdapter ml = new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					int selRow = functionTree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = functionTree.getPathForLocation(e.getX(), e.getY());
					if(selRow != -1) {
						if(e.getClickCount() == 2) {
							JEXStatics.logManager.log("Double click on tree", 1, this);
							ExperimentalDataCrunch cr = functionTree.getFunctionForPath(selPath);
							
							if (cr==null) return;
							
							// make the function
							JEXFunction result = new JEXFunction(cr.getName());
							selectFunction(result);
						}
					}
				}
			};
			functionTree.addMouseListener(ml);
			
			functionCreationPane.setBackground(DisplayStatics.lightBackground);
			functionCreationPane.setLayout(new BoxLayout(functionCreationPane,BoxLayout.PAGE_AXIS));
			functionCreationPane.setAlignmentY(TOP_ALIGNMENT);
			functionCreationPane.setMaximumSize(new Dimension(250,400));
			functionCreationPane.setPreferredSize(new Dimension(250,200));
			functionCreationPane.add(treeScrollPane);
			functionCreationPane.add(addButton);
			
			// separator
			JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			sep.setMaximumSize(new Dimension(10,500));
			sep.setPreferredSize(new Dimension(10,200));
			sep.setBackground(DisplayStatics.lightBackground);
			sep.setForeground(Color.black);
			
			// Parameter chooser
			pTitle.setText("Select function");
			pTitle.setFont(FontUtility.boldFont);
			pTitle.setAlignmentY(TOP_ALIGNMENT);
			pTitle.setAlignmentX(LEFT_ALIGNMENT);
			pTitle.setMaximumSize(new Dimension(250,20));
			pTitle.setPreferredSize(new Dimension(250,20));
			parameterPane.setBackground(DisplayStatics.lightBackground);
			parameterPane.setLayout(new BoxLayout(parameterPane,BoxLayout.PAGE_AXIS));
			parameterPane.setAlignmentY(TOP_ALIGNMENT);
			parameterPane.add(pTitle);
			parameterPane.add(Box.createVerticalGlue());
			
			mainPane.add(functionCreationPane);
			mainPane.add(sep);
			mainPane.add(parameterPane);
			this.add(mainPane);
		}

		/**
		 * Called when clicked yes on the dialog panel
		 */
		public void yes(){
			JEXStatics.logManager.log("Function creation validated", 1, this);
			if (paramPanel != null) {
				paramPanel.saveParameters();
				parent.addFunction(newfunction);
			}
		}
		
		/**
		 * Called when clicked cancel on the dialog panel
		 */
		public void cancel(){
			JEXStatics.logManager.log("Function creation canceled", 1, this);
			
		}

		public void selectFunction(JEXFunction function){
			pTitle.setText("Function: "+function.getFunctionName());
			pTitle.repaint();
			
			ExperimentalDataCrunch crunch = function.getCrunch();
			if (crunch == null) return;
			
			ParameterSet parameters = function.getParameters();
			paramPanel = new JParameterPanel(parameters);
			paramPanel.setMaximumSize(new Dimension(250,600));
			paramPanel.setPreferredSize(new Dimension(250,200));

			pTitle.setAlignmentX(LEFT_ALIGNMENT);
			paramPanel.setAlignmentX(LEFT_ALIGNMENT);
			
			parameterPane.removeAll();
			parameterPane.add(pTitle);
			parameterPane.add(paramPanel);
			
			newfunction = function;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addButton){
				ExperimentalDataCrunch cr = functionTree.getSelectedFunction();
				if (cr==null) return;
				
				// make the function
				JEXFunction result = new JEXFunction(cr.getName());
				selectFunction(result);
			}
		}
		
	}
	

	// ----------------------------------------------------
	// --------- FUNCTION TREE PANEL ----------------------
	// ----------------------------------------------------
	class FunctionTree extends JTree implements TreeSelectionListener{
		private static final long serialVersionUID = 1L;
		public DefaultMutableTreeNode     top ;
		public ExperimentalDataCrunch     cr ;

		public FunctionTree(){
			top = new DefaultMutableTreeNode("Root");
			DefaultTreeModel treeModel = new DefaultTreeModel(top);
			this.setModel(treeModel);
			this.setRootVisible(false);
			this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.addTreeSelectionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
			this.setCellRenderer(new FunctionNodeRenderer());
		}
		
		public ExperimentalDataCrunch getSelectedFunction(){
			return cr;
		}
		
		public void fillTree(){
			// clear the current tree
			top.removeAllChildren();
			DefaultTreeModel treeModel = new DefaultTreeModel(top);

			// get all IDs
			JEXStatics.logManager.log("Filling tree ",1,this);
			
			LinkedHashSet<String> toolboxes = CrunchFactory.getToolboxes();
			for (String tb: toolboxes){
				ToolBoxNode dbNode = new ToolBoxNode(tb);
				top.add(dbNode);
			}
			
			MatlabToolboxNode dbNode = new MatlabToolboxNode();
			top.add(dbNode);
			
			this.setModel(treeModel);
//			this.expandAll();
		}

		/**
		 * Expand each node of the tree until level determined by internal variable defaultdepth
		 */
		public void expandAll() {
			for (int i = 0; i < this.getRowCount(); i++) {
				this.expandRow(i);
			}

		}

		public void valueChanged(TreeSelectionEvent e) {
			// get all selected nodes of the browsing tree
			TreePath[] selectedPaths = this.getSelectionPaths();
			// if the user clicked elsewhere than on a node tag
			if (selectedPaths == null) {return;}
			
			// Get the path selected
			Object[] path = selectedPaths[0].getPath();
			Object o = path[path.length -1] ;
			
			if (o instanceof FunctionNode) {
				this.cr = ((FunctionNode)o).function;
				JEXStatics.logManager.log("Selecting function "+cr.getName(),1,this);
			}
			if (o instanceof MatlabNode) {
				this.cr = ((MatlabNode)o).makeFunction();
				JEXStatics.logManager.log("Selecting matlab function "+cr.getName(),1,this);
			}
			if (o instanceof CustomMatlabNode) {
				this.cr = ((CustomMatlabNode)o).makeFunction();
				JEXStatics.logManager.log("Selecting custom matlab function "+cr.getName(),1,this);
			}
		}
		
		public ExperimentalDataCrunch getFunctionForPath(TreePath selectedpath)
		{
			// Get the path selected
			Object[] path = selectedpath.getPath();
			Object o = path[path.length -1] ;
			
			ExperimentalDataCrunch crunch = null;
			if (o instanceof FunctionNode) {
				crunch = ((FunctionNode)o).function;
			}
			else if (o instanceof MatlabNode) {
				crunch = ((MatlabNode)o).makeFunction();
			}
			else if (o instanceof CustomMatlabNode) {
				crunch = ((CustomMatlabNode)o).makeFunction();
			}
			return crunch;
		}
		
		class ToolBoxNode extends DefaultMutableTreeNode{
			private static final long serialVersionUID = 1L;
			String toolbox;
			
			public ToolBoxNode(String toolbox){
				super(toolbox);
				this.toolbox = toolbox;
				fill();
			}
			
			public void fill(){
				this.removeAllChildren();
				
				TreeMap<String,ExperimentalDataCrunch> availableFunctions = CrunchFactory.getFunctionsFromToolbox(toolbox);
				for (ExperimentalDataCrunch c: availableFunctions.values()){
					FunctionNode dbNode = new FunctionNode(c);
					this.add(dbNode);
				}
			}
		}
		
		class FunctionNode extends DefaultMutableTreeNode{
			private static final long serialVersionUID = 1L;
			ExperimentalDataCrunch function;
			
			public FunctionNode(ExperimentalDataCrunch function){
				super(function.getName());
				this.function = function;
			}
		}

		class MatlabToolboxNode extends DefaultMutableTreeNode{
			private static final long serialVersionUID = 1L;

			public MatlabToolboxNode(){
				super("Matlab/Octave");
				fill();
			}

			public void fill(){
				this.removeAllChildren();

				List<MatlabNode> availableFunctions = getAvailableFunctions();
				for (int i=0, len=availableFunctions.size(); i<len; i++){
					MatlabNode dbNode = availableFunctions.get(i);
					this.add(dbNode);
				}
				
				CustomMatlabNode lastNode = new CustomMatlabNode();
				this.add(lastNode);
			}
			
			public List<MatlabNode> getAvailableFunctions()
			{
				File root = new File(CrunchFactory.matlabPath); //Need to remember to include windows folks and work with classes instead of 
				File[] l = root.listFiles();
				
				List<MatlabNode> ret = new ArrayList<MatlabNode>(0);
				
				if (l == null) return ret;
				for(int i = 0; i < l.length; i++)
				{
					String name = l[i].getName();
					if(name.substring(name.length()-2, name.length()).equals(".m"))
					{
						MatlabNode mFunction = new MatlabNode(l[i]);
						ret.add(mFunction);
					}
				}
				
				return ret;
			}
		}
		
		class MatlabNode extends DefaultMutableTreeNode{
			private static final long serialVersionUID = 1L;
			String path;
			
			public MatlabNode(File file){
				super(file.getName());
				this.path = file.toString();
			}
			
			public ExperimentalDataCrunch makeFunction(){
				ExperimentalDataCrunch ret = new MatlabFunctionClass(path);
				return ret;
			}
		}
		
		class CustomMatlabNode extends DefaultMutableTreeNode{
			private static final long serialVersionUID = 1L;

			public CustomMatlabNode(){
				super("Choose custom .m");
			}

			public ExperimentalDataCrunch makeFunction(){
				String path = selectPath();
				ExperimentalDataCrunch ret = new MatlabFunctionClass(path);
				return ret;
			}
			
			public String selectPath(){
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(JEXStatics.main);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
					File saveFile = fc.getSelectedFile();
					String savepath = saveFile.getAbsolutePath();
					return savepath;
		        } else { 
					System.out.println("Not possible to choose file as a Matlab function...");
		        }
		        return null;
			}
		}
	
		class FunctionNodeRenderer extends DefaultTreeCellRenderer {
			private static final long serialVersionUID = 1L;
			Icon functionIcon;

		    public FunctionNodeRenderer() {
//		    	functionIcon = icon;
		    }

		    public Component getTreeCellRendererComponent(
		                        JTree tree,
		                        Object value,
		                        boolean sel,
		                        boolean expanded,
		                        boolean leaf,
		                        int row,
		                        boolean hasFocus) {

		        super.getTreeCellRendererComponent(
		                        tree, value, sel,
		                        expanded, leaf, row,
		                        hasFocus);
		        
		        if (value instanceof FunctionNode){
		        	FunctionNode fn = (FunctionNode)value;
		        	setToolTipText(fn.function.getInfo());
		        }
		        else if (value instanceof MatlabNode){
		        	setToolTipText("This is a matlab function");
		        }
		        else if (value instanceof CustomMatlabNode){
		        	setToolTipText("This is any matlab function");
		        }
		        else {
		            setToolTipText(null); //no tool tip
		        } 

		        return this;
		    }

		}

	}

}
