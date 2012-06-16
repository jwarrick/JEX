package jex;

import guiObject.SignalButton;
import guiObject.SignalMenuButton;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import jex.statics.DisplayStatics;
import jex.statics.PrefsUtility;
import preferences.XPreferencePanelController;
import signals.SSCenter;

public class StatusBar extends JPanel implements Runnable, ActionListener {
	private static final long serialVersionUID = 1L;
	public JLabel       labelBar = new JLabel("");
	public JProgressBar bar      = new JProgressBar(JProgressBar.HORIZONTAL,0,100);
	public SignalButton aboutButton    = new SignalButton("About");
	public SignalButton testButton     = new SignalButton("Test");
	public static int   percent  = 0;

	public StatusBar(){}
	
	private void initialize(){
		this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		this.setBackground(DisplayStatics.menuBackground);
		this.setPreferredSize(new Dimension(200,30));
		
		aboutButton.setMaximumSize(new Dimension(50,20));
		testButton.setMaximumSize(new Dimension(50,20));
		SSCenter.defaultCenter().connect(aboutButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "showAboutBox", (Class[])null);
		SSCenter.defaultCenter().connect(testButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "testButtonPressed", (Class[])null);
		
		labelBar.setMaximumSize(new Dimension(200,20));
		labelBar.setMaximumSize(new Dimension(200,20));
		labelBar.setPreferredSize(new Dimension(200,20));
		
		// display status bar
		this.add(Box.createHorizontalStrut(5));
		this.add(bar);
		this.add(Box.createHorizontalStrut(5));
		this.add(labelBar);
		this.add(Box.createHorizontalGlue());
		this.add(testButton);
		this.add(aboutButton);
	}

	// ---------------------------------------------
	// Getters and setters
	// ---------------------------------------------
	
	/**
	 * Open about box
	 */
	public void showAboutBox(){
		AboutBox aboutBox = new AboutBox();
		aboutBox.setResizable(false);
		aboutBox.setVisible(true);
	}
	
	/**
	 * Test button was pressed
	 */
	public void testButtonPressed()
	{
		// Get the preferences
		XPreferencePanelController pPanel = new XPreferencePanelController(PrefsUtility.getRootPrefs(), true);
		JPanel pane = pPanel.getPanel();
		
		// Show the panel in a popup frame
		JDialog diag = new JDialog();
		diag.setResizable(true);
		diag.setBounds(100, 50, 200, 400);
		diag.setMinimumSize(new Dimension(75,300));
		diag.setTitle("Je'Xperiment - Preferences");
		diag.setContentPane(pane);
		
		diag.setVisible(true);
	}
	
	/**
	 * Reset the progress bar
	 */
	public void resetBar(){
		setProgressPercentage(0);
	}
	
	/**
	 * Set the advancement of the progress bar
	 * @param progress
	 */
	public void setProgressPercentage(int progress){
		bar.setValue(progress);
		bar.setStringPainted(true);
		
		java.awt.Rectangle progressRect = bar.getBounds();
		progressRect.x = 0;
		progressRect.y = 0;
		bar.paintImmediately( progressRect );
	}
	
	/**
	 * Set the label to be displayed in the status bar
	 * @param str
	 */
	public void setStatusText(String str){
		labelBar.setText(str);
		labelBar.repaint();
	}

	public void run() {
		// TODO Auto-generated method stub
		initialize();
		
	}

	// ----------------------------------------------------
	// --------- EVENT HANDLING FUNCTIONS -----------------
	// ----------------------------------------------------
	
	public void actionPerformed(ActionEvent e){}

}
