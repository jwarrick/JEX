package jex.jexTabPanel.jexDistributionPanel;

import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class FileListCellRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
	{
		String s = "";
		if (value instanceof File){
			s = ((File)value).getName();
		}
		else {
			s = value.toString();
		}
		setText(s);

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}