package guiObject;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import net.miginfocom.swing.MigLayout;
import utilities.FontUtility;

public class FlatRoundedButton implements PaintComponentDelegate, MouseListener {
	private static final long serialVersionUID = 1L;
	
	// INTERFACE VARIABLES
	private String    name ;
	private String    toolTipText ;
	private Icon      icon ;
	private List<ActionListener> listeners;

	// GUI VARIABLES
	private PixelComponentDisplay panel;
	public Color background      = DisplayStatics.lightBackground;
	public Color normalBack      = DisplayStatics.lightBackground;
	public Color mouseOverBack   = DisplayStatics.lightMouseOverBackground;
	public Color selectedBack    = DisplayStatics.background;
	public Color temporaryGround = normalBack;
	
	private JLabel nameLabel ;

	public FlatRoundedButton(String name){
		this.name = name;
		this.initialize();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	public void initialize(){
		this.panel = new PixelComponentDisplay(this);
		this.panel.setLayout(new MigLayout("left, ins 2 6 2 6","[center]","[center]"));
		this.panel.setBackground(DisplayStatics.background);
		this.panel.addMouseListener(this);
		
		this.nameLabel = new JLabel();
		this.nameLabel.setBackground(DisplayStatics.background);
		if (name != null)
		{
			this.nameLabel.setText(this.name);
			this.nameLabel.setFont(FontUtility.defaultFonts);
		}
		if (icon != null)
		{
			this.nameLabel.setIcon(icon);
		}
		
		this.panel.add(nameLabel);
		listeners = new ArrayList<ActionListener>(0);
	}
	
	public void setText(String name){
		this.name = name;
		nameLabel.setText(name);
		this.panel.repaint();
	}
	
	public void setIcon(Icon icon)
	{
		this.icon = icon;
		nameLabel.setIcon(icon);
		this.panel.repaint();
	}
	
	public void setFont(Font f)
	{
		this.nameLabel.setFont(f);
	}
	
	public String getText(){
		return this.name;
	}
	
	public Icon getIcon()
	{
		return this.icon;
	}
	
	public void setToolTipText(String toolTipText)
	{
		this.toolTipText = toolTipText ;
		panel.setToolTipText(this.toolTipText);
	}
	
	/**
	 * Add an action listener to this button
	 * @param id
	 */
	public void addActionListener(ActionListener id){
		listeners.add(id);
	}
	
	public void refresh(){
		temporaryGround = normalBack;
		this.panel.repaint();
	}
	
	/**
	 * Paint this componement with cool colors
	 */
	public void paintComponent(Graphics2D g2) { 
		
//	    int x = 2;
//	    int y = 2;
//	    int w = this.panel.getWidth() - 3;
//	    int h = this.panel.getHeight() - 3;
	    int arc = 15;
	    
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    Rectangle r = this.panel.getBoundsLocal();
	    g2.setColor(background);
	    g2.fill(this.panel.getBoundsLocal());
	    
	    g2.setColor(temporaryGround);
	    g2.fillRoundRect(0, 0, r.width, r.height, arc, arc);
	    
	    g2.setColor(Color.BLACK);
	    
	    //g2.drawString(this.nameLabel.getText(), (float)(labelX), (float)(labelY-1));
	    //g2.dispose();
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {
		temporaryGround = mouseOverBack;
		this.panel.repaint();
	}
	public void mouseExited(MouseEvent e) {
		temporaryGround = normalBack;
		this.panel.repaint();
	}
	public void mousePressed(MouseEvent e) {
		temporaryGround = selectedBack;
		this.panel.repaint();
	}
	public void mouseReleased(MouseEvent e) {
		ActionEvent event = new ActionEvent(this,0,null);
		for (ActionListener id: listeners){
			id.actionPerformed(event);
		}
		refresh();
	}
	
}
