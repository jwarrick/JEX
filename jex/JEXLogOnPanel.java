package jex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import jex.statics.PrefsUtility;
import net.miginfocom.swing.MigLayout;
import utilities.FontUtility;

public class JEXLogOnPanel extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	// GUI variables
	private CreateUserPanel   createUserPane ;
	private OpenUserPanel     openUserPane ;
	private RecentlyOpenPanel openrecentPane ;

	public JEXLogOnPanel()
	{
		initialize();
	}
	
	private void initialize()
	{
		Container c = this.getContentPane();
		c.setBackground(DisplayStatics.background);
		c.setLayout(new MigLayout("flowy,ins 0","5[fill,grow]5","5[fill,grow]5[fill,grow]5[fill,grow]5"));
		
		createUserPanel();
		openUserPanel();
		openRecentPanel();
		
		c.add(createUserPane,"height 33%");
		c.add(openUserPane,"height 33%");
		c.add(openrecentPane,"height 34%");
	}
	
	private void createUserPanel()
	{
		createUserPane = new CreateUserPanel();
	}
	
	private void openUserPanel()
	{
		openUserPane = new OpenUserPanel();
	}
	
	private void openRecentPanel()
	{
		openrecentPane = new RecentlyOpenPanel();
	}

	public void openUser(File file)
	{
		// open the user file
		JEXStatics.logManager.log("Opening userfile "+file, 1, this);
		boolean done = JEXStatics.jexManager.logOn(file);

		if (!done) {
			// Do nothing... there was an error
			return;
		}
		else
		{
			// Switch the display view
			JEXStatics.main.showLogOnFrame(false);
			JEXStatics.main.showDatabaseChooserFrame(true);
		}
	}

	public void actionPerformed(ActionEvent arg0) {}


	// Create the recently opened panel
	class RecentlyOpenPanel extends JPanel implements MouseListener{
		private static final long serialVersionUID = 1L;
		Color backColor = DisplayStatics.lightBackground;

		// Two panels
		private JPanel contentPanel ;
		private JPanel prettyPanel  ;

		JLabel[] labels ;
		String[] files  ;

		public RecentlyOpenPanel(){
			initialize();
			this.addMouseListener(this);
		}

		private void initialize(){
			// Make the content panel
			makeContentPanel();
			
			// Make the pretty panel
			makePrettyPanel();
			
			// Make this panel
			this.setBackground(this.backColor);
			this.setLayout(new BorderLayout());
			this.add(prettyPanel);
		}

		private void makePrettyPanel()
		{
			JLabel title1 = new JLabel("Option 3.");
			JLabel title2 = new JLabel("Select Recently Opened User");
			title1.setFont(FontUtility.boldFont);
			title2.setFont(FontUtility.boldFont);
			title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			title2.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			prettyPanel = new JPanel(); 
			prettyPanel.setBackground(this.backColor);
			prettyPanel.setLayout(new MigLayout("flowy,ins 0","10[fill,grow]","[grow,fill][30]3[30][grow,fill]"));
			prettyPanel.add(Box.createRigidArea(new Dimension(10,10)));
			prettyPanel.add(title1,"");
			prettyPanel.add(title2,"");
		}
		
		private void makeContentPanel()
		{
			contentPanel = new JPanel();
			contentPanel.setBackground(this.backColor);
			contentPanel.setLayout(new MigLayout("flowy,ins 0","10[fill,grow]",""));

			// Set the welcome label
			JLabel title1 = new JLabel("Option 3.");
			JLabel title2 = new JLabel("Select Recently Opened User");
			title1.setFont(FontUtility.boldFont);
			title2.setFont(FontUtility.boldFont);
			title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			title2.setAlignmentX(Component.LEFT_ALIGNMENT);

			// Display the welcome label
			contentPanel.add(Box.createVerticalGlue(),"height 5");
			contentPanel.add(title1,"height 15");
			contentPanel.add(title2,"height 15");
			contentPanel.add(Box.createVerticalGlue(),"");

			// Display the recentrly opened files
			files  = JEXStatics.jexManager.getRecentelyOpenedUserFiles();
			labels = new JLabel[files.length];
			for (int i=0; i<files.length; i++)
			{
				String userFile = files[i];
				File f = new File(userFile);
				String fName = f.getName();

				JLabel userLabel = new JLabel(fName);
				userLabel.setFont(FontUtility.defaultFontl);
				userLabel.addMouseListener(this);
				labels[i] = userLabel;

				userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

				contentPanel.add(userLabel,"height 15");
			}
			contentPanel.add(Box.createVerticalGlue(),"");
		}

		private void display(int panelToDisplay)
		{
			this.removeAll();
			
			if (panelToDisplay == 0)
			{
				this.add(contentPanel,BorderLayout.CENTER);
			}
			else
			{
				this.add(prettyPanel,BorderLayout.CENTER);
			}
			
			this.invalidate();
			this.revalidate();
			this.repaint();
		}
		
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) 
		{
			Point p       = arg0.getPoint();
			Point tl      = this.getLocation();
			Dimension dim = this.getSize();
			if (p.x > tl.x && p.x < tl.x + dim.getWidth() && p.y > tl.y && p.y < tl.y + dim.getHeight())
			{
				display(0);
			}
			else
			{
				display(0);
			}
		}
		public void mouseExited(MouseEvent arg0) 
		{
			Point p       = arg0.getPoint();
			Dimension dim = this.getSize();
			if (p.x > 0 && p.x < dim.getWidth() && p.y > 0 && p.y < dim.getHeight())
			{
//				JEXStatics.logManager.log("Mouse exited but still inside "+p.x+"-"+p.y+" vs. "+tl.x+"-"+tl.y, 1, this);
				display(0);
			}
			else
			{
//				JEXStatics.logManager.log("Mouse exited to outside "+p.x+"-"+p.y+" vs. "+tl.x+"-"+tl, 1, this);
				display(1);
			}
		}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) 
		{
			JEXStatics.logManager.log("Clicked to open user file", 1, this);
			for (int i=0; i<files.length; i++)
			{
				JLabel label = labels[i];
				if (arg0.getSource() == label)
				{
					// Get the file
					String file = files[i];
					File user = new File(file);
					openUser(user);
				}
			}
		}
	}

	// Create the user creation panel
	class CreateUserPanel extends JPanel implements ActionListener, MouseListener
	{
		private static final long serialVersionUID = 1L;
		Color backColor = DisplayStatics.lightBackground;

		// GUI variables
		private JTextField userNameField ;
		private JTextField pathField ;
		private JButton           browseButton;
		private JButton           doItButton;

		// Two panels
		private JPanel contentPanel ;
		private JPanel prettyPanel  ;

		CreateUserPanel()
		{
			initialize();
			this.addMouseListener(this);
		}

		private void initialize()
		{
			// Make the content panel
			makeContentPanel();
			
			// Make the pretty panel
			makePrettyPanel();
			
			// Make this panel
			this.setBackground(this.backColor);
			this.setLayout(new BorderLayout());
			this.add(prettyPanel);
		}
		
		private void makePrettyPanel()
		{
			JLabel title1 = new JLabel("Option 1.");
			JLabel title2 = new JLabel("Create a new user");
			title1.setFont(FontUtility.boldFont);
			title2.setFont(FontUtility.boldFont);
			title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			title2.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			prettyPanel = new JPanel(); 
			prettyPanel.setBackground(this.backColor);
			prettyPanel.setLayout(new MigLayout("flowy,ins 0","10[fill,grow]","[grow,fill][30]3[30][grow,fill]"));
			prettyPanel.add(Box.createRigidArea(new Dimension(10,10)));
			prettyPanel.add(title1,"");
			prettyPanel.add(title2,"");
		}
		
		private void makeContentPanel()
		{
			contentPanel = new JPanel();
			contentPanel.setBackground(this.backColor);
			contentPanel.setLayout(new MigLayout("ins 0","10[grow,fill]5[400]5[150]10",""));

			// Create the components
			JLabel title1 = new JLabel("Option 1.");
			JLabel title2 = new JLabel("Create a new user");
			title1.setFont(FontUtility.boldFont);
			title2.setFont(FontUtility.boldFont);
			title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			title2.setAlignmentX(Component.LEFT_ALIGNMENT);

			JLabel label1 = new JLabel("User Name");			
			JLabel label2 = new JLabel("Path");

			userNameField = new JTextField("Jim");
			pathField = new JTextField("");
			userNameField.setBackground(this.backColor);
			pathField.setBackground(this.backColor);
			
			browseButton = new JButton("Browse");
			doItButton = new JButton("YES");
			browseButton.addActionListener(this);
			doItButton.addActionListener(this);
			
			// Place the components
			contentPanel.add(Box.createVerticalStrut(10),"height 10,wrap");
			contentPanel.add(title1,"height 15,wrap");
			contentPanel.add(title2,"height 15,wrap");
			contentPanel.add(Box.createVerticalStrut(5),"height 5,wrap");
			contentPanel.add(label1,"height 25");
			contentPanel.add(userNameField,"height 25,growx,span 2,wrap");
			contentPanel.add(label2,"height 25");
			contentPanel.add(pathField,"height 25,growx");
			contentPanel.add(browseButton,"height 25,growx,wrap");
			contentPanel.add(Box.createVerticalGlue());
			contentPanel.add(doItButton,"height 25,growx,span 3,wrap");
		}

		private void display(int panelToDisplay)
		{
			this.removeAll();
			
			if (panelToDisplay == 0)
			{
				this.add(contentPanel,BorderLayout.CENTER);
			}
			else
			{
				this.add(prettyPanel,BorderLayout.CENTER);
			}
			
			this.invalidate();
			this.revalidate();
			this.repaint();
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			if (e.getSource() == browseButton)
			{
				// Creating file chooser to open user preferences
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				// Set the current directory
				String lastPath = PrefsUtility.getLastPath();
				File filepath = new File(lastPath);
				if (filepath.isDirectory()) fc.setCurrentDirectory(filepath);
				else {
					File filefolder = filepath.getParentFile();
					fc.setCurrentDirectory(filefolder);
				}

				// Open dialog box
				int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
					// Get the file
					File file = fc.getSelectedFile();
					if (! file.isDirectory())
					{
						file = file.getParentFile();
					}
					JEXStatics.logManager.log("Creating user file in folder "+file.getPath(), 0, this);

					// Set the last path opened to the path selected
					PrefsUtility.setLastPath(file.getPath());

					// Set the path field
					pathField.setText(file.getPath());
					pathField.repaint();
				}
			}
			else if (e.getSource() == doItButton)
			{
				// Make the user file
				String folder = pathField.getText();
				String name   = userNameField.getText();
				String fName  = folder + File.separator + name + ".jex";
				File   file   = new File(fName);

				// Create the user
				JEXStatics.jexManager.createUser(file);
				openUser(file);
			}
		}

		public void mouseClicked(MouseEvent arg0) {}

		public void mouseEntered(MouseEvent arg0) {
			Point p       = arg0.getPoint();
			Point tl      = this.getLocation();
			Dimension dim = this.getSize();
			if (p.x > tl.x && p.x < tl.x + dim.getWidth() && p.y > tl.y && p.y < tl.y + dim.getHeight())
			{
				display(0);
			}
			else
			{
				display(0);
			}
		}

		public void mouseExited(MouseEvent arg0) {
			Point p       = arg0.getPoint();
			Dimension dim = this.getSize();
			if (p.x > 0 && p.x < dim.getWidth() && p.y > 0 && p.y < dim.getHeight())
			{
//				JEXStatics.logManager.log("Mouse exited but still inside "+p.x+"-"+p.y+" vs. "+tl.x+"-"+tl.y, 1, this);
				display(0);
			}
			else
			{
//				JEXStatics.logManager.log("Mouse exited to outside "+p.x+"-"+p.y+" vs. "+tl.x+"-"+tl, 1, this);
				display(1);
			}
		}

		public void mousePressed(MouseEvent arg0) {}

		public void mouseReleased(MouseEvent arg0) {}
	}

	// Create the user creation panel
	class OpenUserPanel extends JPanel implements ActionListener, MouseListener
	{
		private static final long serialVersionUID = 1L;
		
		// GUI variables
		Color backColor = DisplayStatics.lightLightBackground;
		private JTextField userNameField ;
		private JTextField pathField ;
		private JButton    browseButton;
		private JButton    doItButton;
		
		// Two panels
		private JPanel contentPanel ;
		private JPanel prettyPanel  ;

		OpenUserPanel()
		{
			initialize();
			this.addMouseListener(this);
		}
		
		private void initialize()
		{
			// Make the content panel
			makeContentPanel();
			
			// Make the pretty panel
			makePrettyPanel();
			
			// Make this panel
			this.setBackground(this.backColor);
			this.setLayout(new BorderLayout());
			this.add(prettyPanel);
		}
		
		private void makePrettyPanel()
		{
			JLabel title1 = new JLabel("Option 2.");
			JLabel title2 = new JLabel("Open an existing user file");
			title1.setFont(FontUtility.boldFont);
			title2.setFont(FontUtility.boldFont);
			title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			title2.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			prettyPanel = new JPanel(); 
			prettyPanel.setBackground(this.backColor);
			prettyPanel.setLayout(new MigLayout("flowy,ins 0","10[fill,grow]","[grow,fill][30]3[30][grow,fill]"));
			prettyPanel.add(Box.createRigidArea(new Dimension(10,10)));
			prettyPanel.add(title1,"");
			prettyPanel.add(title2,"");
		}
		
		private void makeContentPanel()
		{
			contentPanel = new JPanel();
			contentPanel.setBackground(this.backColor);
			contentPanel.setLayout(new MigLayout("ins 0","10[grow,fill]5[400]5[150]10",""));
			
			// Create the components
			JLabel title1 = new JLabel("Option 2.");
			JLabel title2 = new JLabel("Open an existing user file");
			title1.setFont(FontUtility.boldFont);
			title2.setFont(FontUtility.boldFont);
			title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			title2.setAlignmentX(Component.LEFT_ALIGNMENT);

			JLabel label1 = new JLabel("User Name");			
			JLabel label2 = new JLabel("Path");
			
			userNameField = new JTextField("Jim");
			pathField = new JTextField("");
			userNameField.setBackground(this.backColor);
			pathField.setBackground(this.backColor);
			
			browseButton = new JButton("Browse");
			doItButton = new JButton("YES");
			browseButton.addActionListener(this);
			doItButton.addActionListener(this);
			
			// Place the components
			contentPanel.add(Box.createVerticalStrut(10),"height 10,wrap");
			contentPanel.add(title1,"height 15,span 2,wrap");
			contentPanel.add(title2,"height 15,span 2,wrap");
			contentPanel.add(Box.createVerticalStrut(5),"height 5,wrap");
			contentPanel.add(label1,"height 25");
			contentPanel.add(userNameField,"height 25,growx,span 2,wrap");
			contentPanel.add(label2,"height 25");
			contentPanel.add(pathField,"height 25,growx");
			contentPanel.add(browseButton,"height 25,growx,wrap");
			contentPanel.add(Box.createVerticalGlue());
			contentPanel.add(doItButton,"height 25,growx,span 3,wrap");
		}
		
		private void display(int panelToDisplay)
		{
			this.removeAll();
			
			if (panelToDisplay == 0)
			{
				this.add(contentPanel,BorderLayout.CENTER);
			}
			else
			{
				this.add(prettyPanel,BorderLayout.CENTER);
			}
			
			this.invalidate();
			this.revalidate();
			this.repaint();
		}

		public void actionPerformed(ActionEvent e) 
		{
			if (e.getSource() == browseButton)
			{
				// Creating file chooser to open user preferences
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				// Set the current directory
				String lastPath = PrefsUtility.getLastPath();
				File filepath = new File(lastPath);
				if (filepath.isDirectory()) 
				{
					fc.setCurrentDirectory(filepath);
				}
				else {
					File filefolder = filepath.getParentFile();
					fc.setCurrentDirectory(filefolder);
				}

				// Open dialog box
				int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
					// Get the file
					File file = fc.getSelectedFile();
					if (! file.isDirectory())
					{
						// Get the user name
						String nameWithExt = file.getName();
						String[] split = nameWithExt.split(".jex");
						if (split.length<1) return;
						String name = split[0];
						userNameField.setText(name);

						// Make the file a directory
						file = file.getParentFile();
					}
					JEXStatics.logManager.log("Creating user file in folder "+file.getPath(), 0, this);

					// Set the last path opened to the path selected
					PrefsUtility.setLastPath(file.getPath());

					// Set the path field
					pathField.setText(file.getPath());
					pathField.repaint();
				}
			}
			else if (e.getSource() == doItButton)
			{
				// Make the user file
				String folder = pathField.getText();
				String name   = userNameField.getText();
				String fName  = folder + File.separator + name + ".jex";
				File   file   = new File(fName);
				openUser(file);
			}
		}

		public void mouseClicked(MouseEvent arg0) {}

		public void mouseEntered(MouseEvent arg0) {
			Point p       = arg0.getPoint();
			Point tl      = this.getLocation();
			Dimension dim = this.getSize();
			if (p.x > tl.x && p.x < tl.x + dim.getWidth() && p.y > tl.y && p.y < tl.y + dim.getHeight())
			{
				display(0);
			}
			else
			{
				display(0);
			}
		}

		public void mouseExited(MouseEvent arg0) {
			Point p       = arg0.getPoint();
			Dimension dim = this.getSize();
			if (p.x > 0 && p.x < dim.getWidth() && p.y > 0 && p.y < dim.getHeight())
			{
//				JEXStatics.logManager.log("Mouse exited but still inside "+p.x+"-"+p.y+" vs. "+tl.x+"-"+tl.y, 1, this);
				display(0);
			}
			else
			{
//				JEXStatics.logManager.log("Mouse exited to outside "+p.x+"-"+p.y+" vs. "+tl.x+"-"+tl, 1, this);
				display(1);
			}
		}

		public void mousePressed(MouseEvent arg0) {}

		public void mouseReleased(MouseEvent arg0) {}
	}

}
