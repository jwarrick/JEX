package jex.jexTabPanel.jexPluginPanel;

import guiObject.DialogGlassPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import plugin.rscripter.RScripter;
import plugins.imageAligner2.ImageAligner;
import plugins.selector.SelectorPlugInTester;
import plugins.viewer.ImageBrowser;
import signals.SSCenter;
import utilities.FontUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.Definition.Experiment;
import Database.Definition.TypeName;
import Exporter.Exporter;
import Exporter.ExporterOptionPanel;


public class PluginPanel implements ActionListener{
	
	public JPanel panel;
	
	private static final long serialVersionUID = 1L;
	private Color foregroundColor = DisplayStatics.lightBackground;
	private JButton exportCSV    = new JButton("Export to CSV");
	private JButton exportImage  = new JButton("Export to Image");
	private JButton roiMaker     = new JButton("Make ROIs");
	private JButton imageAligner = new JButton("Get Image Alignments");
	private JButton selectorPanel = new JButton("Selector Panel");
	private JButton rScripterPanel = new JButton("R Scripter");

	public PluginPanel(){
		this.panel = new JPanel();
		initialize();
	}
	
	/**
	 * Detach the signals
	 */
	public void deInitialize()
	{
		SSCenter.defaultCenter().disconnect(this);
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	private void initialize(){
		this.panel.setBackground(foregroundColor);
		this.panel.setLayout(new MigLayout("flowy, ins 10, gapy 3, alignx center","[]","[]"));

		JLabel label1 = new JLabel("Exporting tools:");
		label1.setFont(FontUtility.boldFont);
		exportCSV.setMaximumSize(new Dimension(150,20));
		exportCSV.setPreferredSize(new Dimension(150,20));
		exportCSV.addActionListener(this);
		exportImage.setMaximumSize(new Dimension(150,20));
		exportImage.setPreferredSize(new Dimension(150,20));
		exportImage.addActionListener(this);
		JLabel label2 = new JLabel("Object creation tools:");
		label2.setFont(FontUtility.boldFont);
		roiMaker.setMaximumSize(new Dimension(150,20));
		roiMaker.setPreferredSize(new Dimension(150,20));
		roiMaker.addActionListener(this);
		imageAligner.setMaximumSize(new Dimension(150,20));
		imageAligner.setPreferredSize(new Dimension(150,20));
		imageAligner.addActionListener(this);
		selectorPanel.setMaximumSize(new Dimension(150,20));
		selectorPanel.setPreferredSize(new Dimension(150,20));
		selectorPanel.addActionListener(this);
		rScripterPanel.setMaximumSize(new Dimension(150,20));
		rScripterPanel.setPreferredSize(new Dimension(150,20));
		rScripterPanel.addActionListener(this);
		
		this.panel.add(label1,"left");
		//this.panel.add(Box.createVerticalStrut(3));
		this.panel.add(exportCSV);
		//this.panel.add(Box.createVerticalStrut(3));
		this.panel.add(exportImage);
		//this.panel.add(Box.createVerticalStrut(3));
		this.panel.add(label2,"left");
		//this.panel.add(Box.createVerticalStrut(3));
		this.panel.add(roiMaker);
		//this.panel.add(Box.createVerticalStrut(3));
		this.panel.add(imageAligner);
		//this.panel.add(Box.createVerticalStrut(3));
		this.panel.add(selectorPanel);
		//this.panel.add(Box.createVerticalGlue());
		this.panel.add(rScripterPanel);
	}
	

	// ----------------------------------------------------
	// --------- OTHER FUNCTIONS -----------------
	// ----------------------------------------------------

	public void diplayPanel() {}

	public void stopDisplayingPanel() {}

	public JPanel getHeader(){
		return null;
	}


	// ----------------------------------------------------
	// --------- EVENT FUNCTIONS -----------------
	// ----------------------------------------------------
	
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == exportCSV)
		{
			DialogGlassPane diagPanel = new DialogGlassPane("Info");
			diagPanel.setSize(400, 200);
			
			ExporterOptionPanel exportPane= new ExporterOptionPanel(Exporter.EXPORT_AS_CSV);
			diagPanel.setCentralPanel(exportPane);

			JEXStatics.main.displayGlassPane(diagPanel,true);
		}
		if (e.getSource() == exportImage)
		{
			DialogGlassPane diagPanel = new DialogGlassPane("Info");
			diagPanel.setSize(400, 300);
			
			ExporterOptionPanel exportPane= new ExporterOptionPanel(Exporter.EXPORT_AS_IMAGE);
			diagPanel.setCentralPanel(exportPane);

			JEXStatics.main.displayGlassPane(diagPanel,true);
		}
		if (e.getSource() == roiMaker)
		{
			TreeSet<JEXEntry> entries = JEXStatics.jexManager.getSelectedEntries();
			TypeName tn = JEXStatics.jexManager.getSelectedObject();
			if(entries.size() > 0 && tn != null && tn.getType().equals(JEXData.IMAGE))
			{
				new ImageBrowser(entries,tn);
			}
		}
		if (e.getSource() == imageAligner)
		{
			TreeSet<JEXEntry> entries = JEXStatics.jexManager.getSelectedEntries();
			TypeName tn = JEXStatics.jexManager.getSelectedObject();
			if(entries.size() > 0 && tn != null && tn.getType().equals(JEXData.IMAGE))
			{
				ImageAligner aligner = new ImageAligner();
				aligner.setDBSelection(entries, tn);
				
			}
		}
		if (e.getSource() == selectorPanel)
		{
			TreeMap<String,Experiment> experiments = JEXStatics.jexManager.getExperimentTree();
			new SelectorPlugInTester(experiments);			
		}
		if (e.getSource() == rScripterPanel)
		{
			@SuppressWarnings("unused")
			RScripter scripter = new RScripter();
		}
	}
}
