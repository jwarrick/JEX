package jex.infoPanels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import net.miginfocom.swing.MigLayout;
import utilities.FontUtility;

public class InfoPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	// GUI
	protected JPanel centerPane;
	protected JLabel titleLabel;
	
	// Variables
	protected String title    = "";
	
	// Color
	protected Color transparent     = new Color(150,150,150,0);
	protected Color transparentDark = new Color(50,50,50,150);

	public InfoPanel(){
		initialize();
	}

	private void initialize(){
		this.setLayout(new MigLayout("flowy,ins 5","[fill,grow]",""));
		this.setBackground(DisplayStatics.background);
		
		// Make a dummy central panel
		centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane,BoxLayout.PAGE_AXIS));
		centerPane.setBackground(transparent);
		
		// Make a dummy title label
		titleLabel = new JLabel();
		titleLabel.setText(title);
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(FontUtility.boldFont);
		
		// Fill the panel
		this.add(titleLabel,"height 15!,growx");
		this.add(centerPane,"growx");
	}
	
	/**
	 * Update the title of the info panel
	 * @param title
	 */
	public void setTitle(String title){
		this.title = title;
		titleLabel.setText(title);
		titleLabel.repaint();
	}
	
	/**
	 * Returns the title of the infopanel
	 * @return
	 */
	public String getTitle(){
		return this.title;
	}
	
	/**
	 * Set the inner panel
	 * @param pane
	 */
	public void setCenterPanel(JPanel pane){
		// set the central panel to the panel PANE
		centerPane = pane;
		
		// Reset the gui
		this.removeAll();
		this.add(titleLabel,"height 15!,growx");
		this.add(centerPane,"growx");
		
		// refresh
		this.invalidate();
		this.validate();
		this.repaint();
	}
	
	// ----------------------------------------------------
	// --------- DRAWING AND GRAPHICS ---------------------
	// ----------------------------------------------------
	
	/**
	 * Paint this componement with cool colors
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int x = 0;
	    int y = 0;
	    int w = getWidth()-3;
	    int h = getHeight();
	    int arc = 10;

	    Graphics2D g2 = (Graphics2D) g.create();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    g2.setColor(transparentDark);
	    g2.fillRoundRect(x, y, w, h, arc, arc);

	    g2.dispose();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
	
}
