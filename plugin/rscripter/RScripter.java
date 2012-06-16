package plugin.rscripter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jex.statics.JEXStatics;
import jex.statics.R;
import net.miginfocom.swing.MigLayout;
import plugins.plugin.PlugIn;
import plugins.plugin.PlugInController;
import utilities.LSVList;
import utilities.StringUtility;
import weka.core.converters.JEXTableWriter2;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DBObjects.dimension.Table;
import Database.DataReader.FileReader;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;

public class RScripter implements PlugInController, ActionListener, ClipboardOwner {

	public PlugIn dialog;
	public TreeSet<JEXEntry> entries;
	public TypeName tn;
	
	public JTextArea code;
	public JPanel main;
	public JScrollPane scroll;
	public JPanel buttons;
	public JButton getFileTable;
	public JButton tableFromText;
	public JLabel buttonHeader;
	
	public RScripter()
	{
		this.dialog = new PlugIn(this);
		this.dialog.setBounds(100, 100, 500, 400);
		initialize();
		this.dialog.setVisible(true);
	}
	
	private void initialize()
	{
		this.main = new JPanel();
		this.main.setLayout(new MigLayout("flowx, ins 3","[fill,grow]3[50]","[fill,grow]"));
		this.code = new JTextArea();
		this.scroll = new JScrollPane(this.code);
		this.code.setLineWrap(true);
		this.main.add(this.scroll,"grow");
		this.buttons = new JPanel();
		this.buttons.setLayout(new MigLayout("flowy, ins 0","[]","[]3[]"));
		this.main.add(this.buttons,"grow");
		
		this.buttonHeader = new JLabel("Script Macros");
		this.getFileTable = new JButton("Get File Table");
		this.tableFromText = new JButton("Table From Text");
		
		this.buttons.add(buttonHeader,"growx");
		this.buttons.add(getFileTable,"growx");
		this.buttons.add(tableFromText,"growx");
		
		this.getFileTable.addActionListener(this);
		this.tableFromText.addActionListener(this);
		
		Container contents = this.dialog.getContentPane();
		contents.setLayout(new BorderLayout());
		contents.add(this.main, BorderLayout.CENTER);
		
		
		
	}
	
	public void setDBSelection(TreeSet<JEXEntry> entries, TypeName tn)
	{
		this.entries = entries;
		this.tn = tn;		
	}
	
	public void finalizePlugIn()
	{
		
	}

	public PlugIn plugIn()
	{
		return this.dialog;
	}

	public void actionPerformed(ActionEvent e)
	{
		this.setDBSelection(JEXStatics.jexManager.getSelectedEntries(), JEXStatics.jexManager.getSelectedObject());
		if(e.getSource() == this.getFileTable)
		{
			if(this.entries != null && this.entries.size() > 0 && this.tn != null)
			{
				String command = getRScript_FileTable(this.entries, this.tn);
				if(command != null)
				{
					this.code.setText(command);
					this.code.selectAll();
					StringSelection stringSelection = new StringSelection( command );
				    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				    clipboard.setContents( stringSelection, this );
				}
			}
		}
		if(e.getSource() == this.tableFromText)
		{
			String tableText = this.code.getText();
			String path = JEXWriter.saveText(tableText, "txt");
			String command = "";
			if(StringUtility.isNumeric(tableText.substring(0,1)))
			{
				command = "read.table(" + R.quotedPath(path) + ", header = FALSE, sep = '\\t', quote = \"\\\"'\", dec = '.', comment.char = '#', stringsAsFactors = default.stringsAsFactors(), na.strings = 'NA', strip.white = FALSE, blank.lines.skip = TRUE)";
			}
			else
			{
				command = "read.table(" + R.quotedPath(path) + ", header = TRUE, sep = '\\t', quote = \"\\\"'\", dec = '.', comment.char = '#', stringsAsFactors = default.stringsAsFactors(), na.strings = 'NA', strip.white = FALSE, blank.lines.skip = TRUE)";
			}
			this.code.setText( command );
			this.code.selectAll();
			StringSelection stringSelection = new StringSelection( command );
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents( stringSelection, this );
		}
	}
	
	public static String getRScript_FileTable(TreeSet<JEXEntry> entries, TypeName tn)
	{
		
		TreeMap<DimensionMap,String> files = new TreeMap<DimensionMap,String>();
		DimTable totalDimTable = new DimTable();
		TreeSet<String> expts = new TreeSet<String>();
		TreeSet<String> trays = new TreeSet<String>();
		TreeSet<String> xs = new TreeSet<String>();
		TreeSet<String> ys = new TreeSet<String>();
		if(tn.getType().equals(JEXData.FILE))
		{
			for(JEXEntry e : entries)
			{
				JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(tn, e);
				if(data != null)
				{
					DimTable tempDimTable = data.getDimTable();
					totalDimTable = DimTable.union(totalDimTable, tempDimTable);
					TreeMap<DimensionMap,String> temp = FileReader.readObjectToFilePathTable(data);
					for(DimensionMap map : temp.keySet())
					{
						DimensionMap newMap = map.copy();
						String expt = e.getEntryExperiment();
						String tray = e.getEntryTrayName();
						int x = e.getTrayX();
						int y = e.getTrayY();
						newMap.put("Experiment", expt);
						newMap.put("Tray", tray);
						newMap.put("X", ""+x);
						newMap.put("Y", ""+y);
						expts.add(expt);
						trays.add(tray);
						xs.add(""+x);
						ys.add(""+y);
						files.put(newMap, temp.get(map));
					}
				}
			}
		}
		Dim exptDim = new Dim("Experiment");
		exptDim.dimValues.addAll(expts);
		Dim trayDim = new Dim("Tray");
		trayDim.dimValues.addAll(trays);
		Dim xDim = new Dim("X");
		xDim.dimValues.addAll(xs);
		Dim yDim = new Dim("Y");
		yDim.dimValues.addAll(ys);
		DimTable newTable = new DimTable();
		newTable.add(exptDim);
		newTable.add(trayDim);
		newTable.add(xDim);
		newTable.add(yDim);
		newTable.addAll(totalDimTable);
		
		String path = JEXTableWriter2.writeTable("tempFileTable", new Table<String>(newTable,files));
		LSVList commands = new LSVList();
		commands.add("library(RWeka)");
		commands.add("fileTable <- read.arff(" + R.quotedPath(path) + ")");
		
		return commands.toString();
	}
	
	public void lostOwnership(Clipboard arg0, Transferable arg1)
	{
		// DO NOTHING
	}

}
