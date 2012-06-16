package plugin.entryViewer;

import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import net.miginfocom.swing.MigLayout;
import Database.DBObjects.JEXEntry;

public class EntryInfoPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	public JEXEntry entry;
	public EntryInArrayPreviewController arrayPreviewController;

	public EntryInfoPanel(JEXEntry entry)
	{
		this.entry = entry;
		initialize();
	}
	
	private void initialize()
	{
		this.setBackground(DisplayStatics.background);
		this.setLayout(new MigLayout());
	}
}
