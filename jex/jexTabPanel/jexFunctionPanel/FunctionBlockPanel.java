package jex.jexTabPanel.jexFunctionPanel;

import function.ExperimentalDataCrunch;
import guiObject.FlatRoundedButton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import transferables.TransferableTypeName;
import utilities.FontUtility;
import Database.DBObjects.JEXEntry;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.tnvi;
import cruncher.JEXFunction;

public class FunctionBlockPanel implements ActionListener, MouseListener{
	private static final long serialVersionUID = 1L;
	
	// GUI
	protected Color foregroundColor = DisplayStatics.lightBackground;
	JPanel  panel		   = new JPanel();
	JScrollPane scroll	   = new JScrollPane();
	JPanel  centerPane     = new JPanel()  ;
	JPanel  inputList      = new JPanel()  ;
	JPanel  outputList     = new JPanel()  ;
	JPanel  titlePane      = new JPanel()  ;
	FlatRoundedButton upOneButton    = new FlatRoundedButton("<-") ;
	FlatRoundedButton downOneButton  = new FlatRoundedButton("->") ;
	FlatRoundedButton deleteButton   = new FlatRoundedButton("X") ;
	JLabel  functionName   = new JLabel()  ;
	JPanel  runPane        = new JPanel()  ;
	JButton runButton   = new JButton() ;
	JButton testButton   = new JButton() ;
	
	// variables
	protected JEXFunction  function;
	protected ParameterSet parameters;
	private   JEXFunctionPanel parent;
	
	// Function variables
	TreeMap<String,FunctionInputDrop> inputPanes;
	TreeMap<Integer,FunctionOutputDrag> outputPanes ;
	ExperimentalDataCrunch crunch;


	public FunctionBlockPanel(JEXFunctionPanel parent){
		this.parent = parent;
		
		makeTitlePane();
		initializeSingleFunctionPanel();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	private void makeTitlePane()
	{
		// Prepare the name label
		String funName = (function == null) ? "NONE" : function.getFunctionName();
		funName = funName.substring(0, Math.min(funName.length(),14));
		functionName.setText(funName);
		functionName.setMinimumSize(new Dimension(10,25));
		functionName.setFont(FontUtility.boldFont);
		functionName.setMaximumSize(new Dimension(1000,25));
		functionName.addMouseListener(this);
		
		// Prepare the left button
//		upOneButton.setText("<");
		upOneButton.addActionListener(this);
//		upOneButton.setPreferredSize(new Dimension(25,25));
//		upOneButton.setMaximumSize(new Dimension(25,25));

		// Prepare the right button
//		downOneButton.setText(">");
		downOneButton.addActionListener(this);
//		downOneButton.setPreferredSize(new Dimension(25,25));
//		downOneButton.setMaximumSize(new Dimension(25,25));
		
		// Prepare the right button
//		deleteButton.setText("x");
		deleteButton.addActionListener(this);
//		deleteButton.setPreferredSize(new Dimension(25,25));
//		deleteButton.setMaximumSize(new Dimension(25,25));
		
		// Prepare the title pane container
		titlePane.removeAll();
		titlePane.setBackground(foregroundColor);
		titlePane.setLayout(new MigLayout("center,flowx, ins 0","[]2[center,fill,grow]0[]0[]","[]"));
		
		// Add the objects inside
		titlePane.add(upOneButton.panel());
		titlePane.add(functionName,"growx");
		titlePane.add(deleteButton.panel());
		titlePane.add(downOneButton.panel());
		
		// Make the run buttons
		testButton.setText("TEST");
		testButton.addActionListener(this);
		runButton.setText("RUN");
		runButton.addActionListener(this);
		
		// Make the run panel
		runPane.setBackground(foregroundColor);
		runPane.setLayout(new MigLayout("flowx,ins 0","[fill,grow]","[]"));
		runPane.add(testButton,"growx, width 25:60:");
		runPane.add(runButton,"growx, width 25:60:");
	}
		
	private void initializeSingleFunctionPanel()
	{
		
		inputList.setBackground(foregroundColor);
		inputList.setLayout(new BoxLayout(inputList,BoxLayout.PAGE_AXIS));
		inputList.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		outputList.setBackground(foregroundColor);
		outputList.setLayout(new BoxLayout(outputList,BoxLayout.PAGE_AXIS));
		outputList.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		
		centerPane.setBackground(foregroundColor);
		centerPane.setLayout(new MigLayout("flowy, ins 0, gapy 3","[left,fill,grow]","[]"));
		centerPane.add(inputList,"growx,width 50:100:");
		centerPane.add(outputList,"growx,width 50:100:");

		scroll = new JScrollPane(centerPane);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		this.panel = new JPanel();
		this.panel.setLayout(new MigLayout("flowy,ins 3","[fill,grow]","[]0[fill,grow,]0[]"));
		this.panel.add(titlePane,"growx,width 50:100:");
		this.panel.add(scroll);
		this.panel.add(runPane,"growx,width 50:100:");
	}

	public void setFunction(JEXFunction function){
		this.function = function;
		
		ExperimentalDataCrunch crunch = null;
		if (function != null) crunch = function.getCrunch();
		inputPanes = new TreeMap<String,FunctionInputDrop>();
		outputPanes = new TreeMap<Integer,FunctionOutputDrag>() ;
		inputList.removeAll();
		outputList.removeAll();

		// Creating the input drop panels
		int nbInput = 0;
		TypeName[] inNames = null ;
		if (crunch != null) {
			nbInput = crunch.getInputNames().length;
			inNames = crunch.getInputNames();
		}
		JLabel inputLabel = new JLabel("Inputs:");
		inputLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		inputList.add(inputLabel);
		inputList.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		for (int i=0; i<nbInput; i++){
			FunctionInputDrop ind = new FunctionInputDrop(this,i,inNames[i]);
			ind.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			inputList.add(ind);
			inputList.add(Box.createVerticalStrut(3));
			inputPanes.put(inNames[i].getName(), ind);
		}
		JEXStatics.logManager.log("Created "+nbInput+" input drop panels",1,this);

		// Create the output drag panels
		int nbOutput = 0;
		if (crunch != null) nbOutput = crunch.getOutputs().length;
		JLabel outputLabel = new JLabel("Outputs:");
		outputLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		outputList.add(outputLabel);
		outputList.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		for (int i=0; i<nbOutput; i++){
			FunctionOutputDrag oud = new FunctionOutputDrag(i,function.getExpectedOutputTN(i));
			oud.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			outputList.add(oud);
			outputList.add(Box.createVerticalStrut(3));
			outputPanes.put(new Integer(i), oud);
		}
		JEXStatics.logManager.log("Created "+nbOutput+" output drag panels",1,this);

		// Prepare the name label
		functionName.setText(function.getFunctionName());
		functionName.setFont(FontUtility.boldFont);

		centerPane.revalidate();
		centerPane.repaint();
	}

	/**
	 * An input has been droped... check the new set of inputs for validity
	 */
	public void inputsChanged(){
		JEXStatics.logManager.log("Inputs changed... testing function",1,this);

		// Get output names before test function
		Set<Integer> intKeys = outputPanes.keySet();
		for (Integer index: intKeys){
			FunctionOutputDrag oud = outputPanes.get(index);
			String outputName = oud.getOutputName();
			function.setExpectedOutputName(index, outputName);
		}
	}

	/**
	 * Set input of name NAME with typename INPUTTN
	 * @param name
	 * @param inputTN
	 */
	public void setInput(String inputName, TypeName inputTN){
		function.setInput(inputName, inputTN);
		inputsChanged();
	}

	/**
	 * Set the list of given output names for this function
	 */
	public void setListOfOutputNames()
	{
		Set<Integer> outputIndeces = outputPanes.keySet();
		for (Integer index: outputIndeces){
			FunctionOutputDrag outPane = outputPanes.get(index);
			function.setExpectedOutputName(index, outPane.getOutputName());
		}
		return;
	}

	/**
	 * Test the function and set the status to green or red
	 */
	public void testFunction(){
		// Set the cruncher
		crunch = function.getCrunch();
		crunch.setInputs(function.getInputs());
		
		// Loop through the inputs to see if they exist in the database or if another function
		// has an output with that typeName
		for (String inputName: function.getInputs().keySet())
		{
			// Get the inputTN
			TypeName inputTN = function.getInputs().get(inputName);
			
			// Check if the object is in the database
			if (isDataInDatabase(inputTN))
			{
				FunctionInputDrop in = this.inputPanes.get(inputName);
				in.setInputTN(inputTN);
			}
			
			// Else check if the object is the output of a previous object
			if (isDataOutputOfFunction(inputTN))
			{
				FunctionInputDrop in = this.inputPanes.get(inputName);
				in.setInputTN(inputTN);
			}
		}
		
		// Set the outputs
		boolean canRun = (crunch.checkInputs() == ExperimentalDataCrunch.INPUTSOK);
		for (FunctionOutputDrag outPane: outputPanes.values()){
			outPane.setCanRun(canRun);
		}
	}
	
	/**
	 * Return true if the object exists in the database
	 * @param tn
	 * @return
	 */
	private boolean isDataInDatabase(TypeName tn)
	{
		if(tn == null)
		{
			return false;
		}
		
		tnvi TNVI = JEXStatics.jexManager.getTNVI();
		
		TreeMap<String, TreeMap<String, Set<JEXEntry>>> nvi = TNVI.get(tn.getType());
		if (nvi == null) return false;
		
		TreeMap<String, Set<JEXEntry>> vi = nvi.get(tn.getName());
		if (vi == null) return false;
		
		return true;
	}
	
	/**
	 * Return true if the object is the output of another function of the function list
	 * @param tn
	 * @return
	 */
	private boolean isDataOutputOfFunction(TypeName tn)
	{
		return parent.isDataOutputOfFunction(tn);
	}
	
	/**
	 * Return the function in this functionblockpanel
	 * @return
	 */
	public JEXFunction getFunction()
	{
		this.setListOfOutputNames();
		return this.function;
	}
	
	// ----------------------------------------------------
	// --------- INPUT DROP BOX ---------------------------
	// ----------------------------------------------------
	class FunctionInputDrop extends JPanel {
		private static final long serialVersionUID = 1L;
		
		TypeName inputTN ;
		int index ;
		TypeName inname ;
		FunctionBlockPanel parent ;
		
		JPanel dropArea = new JPanel();
		JLabel inputTNLabel = new JLabel();
		JPanel box ;
		
		FunctionInputDrop(FunctionBlockPanel parent, int index, TypeName inname){
			this.parent = parent;
			this.index = index ;
			this.inname = inname;
			new InputButtonListener(this);
			initialize();
		}
		
		private void initialize(){
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			this.setBackground(foregroundColor);
			this.setMinimumSize(new Dimension(400,20));
			this.setMaximumSize(new Dimension(200,20));

			inputTNLabel.setFont(FontUtility.italicFonts);
			box = new JPanel() {
				private static final long serialVersionUID = 1L;

				protected void paintComponent(Graphics g) {
					int x = 0;
				    int y = 0;
				    int w = getWidth() - 1;
				    int h = getHeight() - 1;

				    Graphics2D g2 = (Graphics2D) g.create();
				    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				    
				    Color c ;
				    if (inputTN != null) c = Color.green;
				    else c = Color.red;
				    g2.setColor(c);
				    g2.fillRect(x, y, w, h);

				    g2.setStroke(new BasicStroke(1f));
				    g2.setColor(Color.black);
				    g2.drawRect(x, y, w, h); 
				    
				    g2.dispose();
				}
			};
			box.setPreferredSize(new Dimension(20,20));
			box.setMaximumSize(new Dimension(20,20));
			box.setMinimumSize(new Dimension(20,20));
			box.setBorder(BorderFactory.createLineBorder(Color.black));
			box.setBackground(Color.RED);
			box.setToolTipText(inname.getName());
			rebuild();
		}
		
		private void rebuild(){
			if (inputTN != null) {
				box.setBackground(Color.GREEN);
				inputTNLabel.setText(inputTN.getType()+": "+inputTN.getName());
			}
			else {
				box.setBackground(Color.RED);
				inputTNLabel.setText("Set input: "+inname);
			}
			
			this.removeAll();
			this.add(box);
			this.add(Box.createHorizontalStrut(5));
			this.add(inputTNLabel);
			this.repaint();
		}
		
		public void setInputTN(TypeName inputTN){
			JEXStatics.logManager.log("Set typename of the input drop...",1,this);
			this.inputTN = inputTN;
			parent.setInput(inname.getName(),inputTN);
			
			rebuild();
		}
		
		public void setIsCrunchable(boolean b){
			rebuild();
		}
	}
	
	// ----------------------------------------------------
	// --------- INPUT LISTENER ---------------------------
	// ----------------------------------------------------
	class InputButtonListener extends DropTargetAdapter {

		private DropTarget dropTarget;
		private FunctionInputDrop button;

		public InputButtonListener(FunctionInputDrop button) {
			this.button = button;

			dropTarget = new DropTarget(button, DnDConstants.ACTION_COPY, this, true, null);
			JEXStatics.logManager.log("Drop target constructed ..."+ dropTarget,1,this);
		}

		public void drop(DropTargetDropEvent event) {
			
			try {
				if (event.isDataFlavorSupported(TransferableTypeName.jexDataFlavor)) {
					Transferable tr = event.getTransferable();
					
					if(tr.isDataFlavorSupported(TransferableTypeName.jexDataFlavor))
					{
						TypeName name = (TypeName) tr.getTransferData(TransferableTypeName.jexDataFlavor);
						JEXStatics.logManager.log("Passing a typeName...",1,this);
						button.setInputTN(name);
					}			
					
					event.acceptDrop(DnDConstants.ACTION_COPY);
					event.dropComplete(true);
					JEXStatics.logManager.log("Drop completed...",1,this);
					
					return;
				}
				event.rejectDrop();
			} catch (Exception e) {
				e.printStackTrace();
				event.rejectDrop();
			}
		}
	}
	
	// ----------------------------------------------------
	// --------- OUTPUT BOX -------------------------------
	// ----------------------------------------------------
	class FunctionOutputDrag extends JPanel implements DragGestureListener, DocumentListener{
		private static final long serialVersionUID = 1L;
		
		TypeName tn;
		int index ;
		boolean canRun = false;

		JPanel dragArea = new JPanel();
		JTextField outputTNLabel = new JTextField();
		JCheckBox saveOutput = new JCheckBox();
		JPanel box ;

		FunctionOutputDrag(int index, TypeName tn){
			this.tn = tn;
			this.index = index ;
			initialize();
			rebuild();
		}

		private void initialize(){
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			this.setBackground(foregroundColor);
			this.setMinimumSize(new Dimension(400,20));
			this.setMaximumSize(new Dimension(200,20));
			
			outputTNLabel.setFont(FontUtility.italicFonts);
			outputTNLabel.getDocument().addDocumentListener(this);
			box = new JPanel() {
				private static final long serialVersionUID = 1L;

				protected void paintComponent(Graphics g) {
					int x = 0;
					int y = 0;
					int w = getWidth() - 1;
					int h = getHeight() - 1;

					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					Color c ;
					if (canRun) c = Color.green;
					else c = Color.red;
					g2.setColor(c);
					g2.fillRect(x, y, w, h);

					g2.setStroke(new BasicStroke(1f));
					g2.setColor(Color.black);
					g2.drawRect(x, y, w, h); 

					g2.dispose();
				}
			};
			box.setPreferredSize(new Dimension(20,20));
			box.setMaximumSize(new Dimension(20,20));
			box.setMinimumSize(new Dimension(20,20));
			box.setBorder(BorderFactory.createLineBorder(Color.black));
			box.setBackground(Color.RED);
			
			saveOutput.setToolTipText("Check this box to set the output to be saved in the database");

			// Create the drag source
			createDragSource();
		}

		private void rebuild(){
			if (tn == null) outputTNLabel.setText("Set name");
			else outputTNLabel.setText(tn.getName());
			
			this.removeAll();
			this.add(box);
			this.add(Box.createHorizontalStrut(5));
			this.add(saveOutput);
			this.add(outputTNLabel);
			this.repaint();
		}

		public void setCanRun(boolean canRun){
			this.canRun = canRun;
			this.repaint();
		}
		
		public String getOutputName(){
			return outputTNLabel.getText();
		}
	
		/**
		 * Set up a drag source on this object
		 */
		private void createDragSource(){
			// create the drag source for the button
			DragSource ds = new DragSource();
			ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
		}
		
		public void dragGestureRecognized(DragGestureEvent event)
		{
			Cursor cursor = null;
			if (event.getDragAction() == DnDConstants.ACTION_COPY) {
				cursor = DragSource.DefaultCopyDrop;
			}
			event.startDrag(cursor, new TransferableTypeName(this.tn.duplicate()));
		}

		public void changedUpdate(DocumentEvent arg0)
		{
			this.tn.setName(this.outputTNLabel.getText());
		}

		public void insertUpdate(DocumentEvent arg0)
		{
			this.tn.setName(this.outputTNLabel.getText());
		}

		public void removeUpdate(DocumentEvent arg0)
		{
			this.tn.setName(this.outputTNLabel.getText());
		}
	}
		

	// ----------------------------------------------------
	// --------- EVENT HANDLING FUNCTIONS -----------------
	// ----------------------------------------------------
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.upOneButton)
		{
			parent.upOne(this);
		}
		else if (e.getSource() == this.downOneButton)
		{
			parent.downOne(this);
		}
		else if (e.getSource() == this.deleteButton)
		{
			if (this == parent.getSelectedFunction())
			{
				parent.selectFunction(null);
			}
			parent.delete(this);
		}
		else if (e.getSource() == this.testButton)
		{
			this.setListOfOutputNames();
			parent.runOneFunction(function, true);
		}
		else if (e.getSource() == this.runButton)
		{
			this.setListOfOutputNames();
			parent.runOneFunction(function, false);
		}
	}

	public void mouseClicked(MouseEvent arg0) {}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) 
	{
		parent.selectFunction(this);
	}
}
