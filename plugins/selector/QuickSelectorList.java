package plugins.selector;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import signals.SSCenter;
import utilities.StringUtility;

public class QuickSelectorList implements ListSelectionListener {
	
	public static final String SIG_ListSelectionChanged_NULL = "SIG_ListSelectionChanged_NULL";
	
	private JPanel panel;
	private JList listDisplay;
//	private int count = 0;
	
	public QuickSelectorList()
	{
		this.panel = new JPanel(new BorderLayout());
		this.listDisplay = new JList();
		JScrollPane scroll = new JScrollPane(this.listDisplay);
		this.panel.add(scroll, BorderLayout.CENTER);
		this.listDisplay.addListSelectionListener(this);
		this.panel.repaint();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	public void setList(List<String> names)
	{
		StringUtility.sortStringList(names);
		this.listDisplay.setListData(names.toArray());
	}
	
	public String getSelection()
	{
		Object selectedItem = this.listDisplay.getSelectedValue();
		if(selectedItem == null)
		{
			return null;
		}
		return this.listDisplay.getSelectedValue().toString();
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
		if(e.getSource() == this.listDisplay && e.getValueIsAdjusting() == false)
		{
			SSCenter.defaultCenter().emit(this, SIG_ListSelectionChanged_NULL, (Object[])null);
		}
	}
}
