package guiObject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;

public class JParameterPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	// Results
	public boolean isValidated = false;
	
	// Model variables
	ParameterSet parameters;
	
	// GUI objects
	private List<JParameterEntryPanel> formEntryPanels;
	private Color backgroundColor = DisplayStatics.lightBackground;
	private JPanel centerPanel ;
	private JScrollPane scroll ;
	private JButton createButton ;
	private JButton cancelButton ;
	private JPanel buttonPanel ;
	private List<ActionListener> listeners;

	/**
	 * Create a new empty JParameterPanel
	 */
	public JParameterPanel() {
		parameters = new ParameterSet();
		listeners = new ArrayList<ActionListener>(0);
		initialize();
	}
	
	public JParameterPanel(ParameterSet parameters){
		this.parameters = parameters;
		listeners = new ArrayList<ActionListener>(0);
		initialize();
	}
	
	/**
	 * Initialize the form
	 */
	public void initialize(){
		// layout of this form
		this.setLayout(new BorderLayout());

		createButton = new JButton("Apply");
		createButton.setMaximumSize(new Dimension(100,20));
		createButton.setPreferredSize(new Dimension(100,20));
		createButton.addActionListener(this);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new Dimension(100,20));
		cancelButton.setPreferredSize(new Dimension(100,20));
		cancelButton.addActionListener(this);
		
		buttonPanel = new JPanel();
		buttonPanel.setBackground(backgroundColor);
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
		buttonPanel.add(createButton);
		buttonPanel.add(cancelButton);
		
		centerPanel = new JPanel();
		centerPanel.setBackground(backgroundColor);
		centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.PAGE_AXIS));
		scroll = new JScrollPane(centerPanel);
		this.scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		this.add(scroll);
		this.makeForm();
	}
	
	/**
	 * Add an entry line to the form
	 * @param title
	 * @param note
	 * @param type
	 * @param options
	 */
	public void addParameter(String title, String note, String type, String[] options){
		Parameter p = new Parameter(title,note,type,options,0);
		this.addParameter(p);
	}
	
	/**
	 * Add an entry for parameter P
	 * @param p
	 */
	public void addParameter(Parameter p){
//		JParameterEntryPanel panel = new JParameterEntryPanel(p);
		parameters.addParameter(p);
//		this.formEntryPanels.add(panel);
	}
	
	/**
	 * Set the background color
	 * @param backgroundColor
	 */
	public void setBackgroundColor(Color backgroundColor){
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * Make the form
	 */
	public void makeForm(){
		centerPanel.removeAll();
		centerPanel.setBackground(backgroundColor);
		
		Collection<Parameter> params = parameters.getParameters();
		formEntryPanels = new ArrayList<JParameterEntryPanel>(0);
		for (Parameter fe: params){
			JParameterEntryPanel fep = new JParameterEntryPanel(fe);
			fep.setBackground(backgroundColor);
			formEntryPanels.add(fep);
			fep.setAlignmentX(LEFT_ALIGNMENT);
			centerPanel.add(fep);
		}
//		centerPanel.add(Box.createVerticalStrut(10));
//		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
//		centerPanel.add(buttonPanel);
		this.revalidate();
		
//		this.add(new JSeparator(JSeparator.HORIZONTAL));
//		this.add(Box.createVerticalGlue());
	}
	
	/**
	 * Return the value of entry line titled NAME
	 * @param name
	 * @return the value for key NAME
	 */
	public String getValue(String name){
		for (JParameterEntryPanel fep: formEntryPanels){
			if (fep.p.title.equals(name)){
				String result = fep.getValue();
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Add an action listener to this form panel
	 * @param listener
	 */
	public void addActionListener(ActionListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Save the parameters
	 */
	public void saveParameters(){
		JEXStatics.logManager.log("Saving the parameter set...",1,this);
		for (JParameterEntryPanel fep: formEntryPanels){
			String name = fep.p.title;
			Parameter p = parameters.getParameter(name);
			String result = fep.getValue();
			p.setValue(result);
		}
	}
	
	/**
	 * Clicked ok or cancel
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == createButton){
			saveParameters();
		}
		if (e.getSource() == cancelButton){
			JEXStatics.logManager.log("Cancel modifications...",1,this);
			makeForm();
		}
		ActionEvent event = new ActionEvent(this,0,null);
		for (ActionListener id: listeners){
			id.actionPerformed(event);
		}
	}
	
}
