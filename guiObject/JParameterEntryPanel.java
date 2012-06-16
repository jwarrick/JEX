package guiObject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jex.statics.JEXStatics;
import Database.Definition.Parameter;


public class JParameterEntryPanel extends JPanel implements DocumentListener, ChangeListener, ActionListener{
	private static final long serialVersionUID = 1L;

		// Model variable
		Parameter p;
		
		// GUI variables
		JLabel titleField ;
		JComponent resultField ;
		Dimension size = new Dimension(100,20) ;
		
		public JParameterEntryPanel(Parameter p){
			this.p = p;
			initialize();
		}
	
		/**
		 * Set the text label dimension
		 * @param size
		 */
		public void setLabelSize(Dimension size){
			this.size = size;
		}
		
		/**
		 * initialize the entry
		 */
		private void initialize(){
			// This
			this.setBackground(Color.white);
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			this.setMaximumSize(new Dimension(1000,20));
			
			// GUI objects
			titleField = new JLabel(p.title);
			if (p.type.equals(Parameter.DROPDOWN) && p.options != null){
				resultField = new JComboBox(p.options);
				
				for (int i=0, len=p.options.length; i<len; i++){
					if (p.result.equals(p.options[i])) ((JComboBox)resultField).setSelectedIndex(i);
				}
				((JComboBox)resultField).addActionListener(this);
			}
			else if (p.type.equals(Parameter.CHECKBOX) && p.options != null){
				resultField = new JCheckBox();
				if (p.result.equals("true")) ((JCheckBox)resultField).setSelected(true);
				((JCheckBox)resultField).addChangeListener(this);
			}
			else { 
				resultField = new JTextField("");
				((JTextField)resultField).setText(p.result);
				((JTextField)resultField).getDocument().addDocumentListener(this);
//				if (p.options != null && p.options.length > 0){ ((JTextField)resultField).setText(p.options[0]); }
			}
			
			titleField.setBackground(Color.white);
			titleField.setMaximumSize(size);
			titleField.setPreferredSize(size);
			resultField.setBackground(Color.white);
			resultField.setMaximumSize(new Dimension(1000,20));
			
			if (p.note !=  null) resultField.setToolTipText(p.note);
			
			this.add(Box.createHorizontalStrut(5));
			this.add(titleField);
			this.add(resultField);
			this.add(Box.createHorizontalStrut(5));
		}
	
		/**
		 * Return the set value in the result Field
		 * @return String value of the component
		 */
		public String getValue(){
			String result;
			if (p.type.equals(FormLine.DROPDOWN) && p.options != null){  
				result = ((JComboBox)resultField).getSelectedItem().toString();}
			else if (p.type.equals(FormLine.CHECKBOX) && p.options != null){  
				result = ""+((JCheckBox)resultField).isSelected();}
			else { 
				result = ((JTextField)resultField).getText();}
			
			return result;
		}
		
		/**
		 * Set the displayed value of the parameter
		 * @param paramValue
		 */
		public void setValue(String paramValue)
		{
			p.setValue(paramValue);
			((JTextField)resultField).setText(paramValue);
		}
		
		/**
		 * Return the parameter displayed in this panel
		 * @return
		 */
		public Parameter getParameter()
		{
			return this.p;
		}
		
		@Override
		public void validate(){
			p.setValue(this.getValue());
		}

		public void changedUpdate(DocumentEvent arg0) {
			String result = this.getValue();
			String old = p.getValue();
			p.setValue(result);
			JEXStatics.logManager.log("Changed parameter from " + old + " to " + p.getValue(), 0, this);
		}

		public void insertUpdate(DocumentEvent arg0) {
			String result = this.getValue();
			String old = p.getValue();
			p.setValue(result);
			JEXStatics.logManager.log("Changed parameter from " + old + " to " + p.getValue(), 0, this);
		}

		public void removeUpdate(DocumentEvent arg0) {
			String result = this.getValue();
			String old = p.getValue();
			p.setValue(result);
			JEXStatics.logManager.log("Changed parameter from " + old + " to " + p.getValue(), 0, this);
		}

		public void actionPerformed(ActionEvent arg0) {
			String result = this.getValue();
			String old = p.getValue();
			p.setValue(result);
			JEXStatics.logManager.log("Changed parameter from " + old + " to " + p.getValue(), 0, this);
		}

		public void stateChanged(ChangeEvent arg0) {			
			String result = this.getValue();
			String old = p.getValue();
			p.setValue(result);
			JEXStatics.logManager.log("Changed parameter from " + old + " to " + p.getValue(), 0, this);
		}
	}