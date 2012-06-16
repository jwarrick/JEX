package jex;

import guiObject.DialogGlassPane;
import guiObject.SignalMenuButton;
import icons.IconRepository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import jex.jexTabPanel.JEXTabPanelController;
import jex.jexTabPanel.creationPanel.JEXCreationPanelController;
import jex.jexTabPanel.jexDistributionPanel.JEXDistributionPanelController;
import jex.jexTabPanel.jexFunctionPanel.JEXFunctionPanelController;
import jex.jexTabPanel.jexLabelPanel.JEXLabelPanelController;
import jex.jexTabPanel.jexNotesPanel.JEXNotesPanelController;
import jex.jexTabPanel.jexPluginPanel.JEXPluginPanelController;
import jex.jexTabPanel.jexStatisticsPanel.JEXStatisticsPanelController;
import jex.jexTabPanel.jexViewPanel.JEXViewPanelController;
import jex.objectAndEntryPanels.EntryAndObjectPanel;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import jex.statics.KeyStatics;
import jex.statics.LogManager;
import jex.statics.PrefsUtility;
import jex.statics.R;
import plugins.labelManager.DatabaseLabelManager;
import preferences.XPreferencePanelController;
import preferences.XPreferences;
import signals.SSCenter;
import cruncher.Cruncher;


public class JEXperiment extends JFrame implements ActionListener, WindowListener, WindowFocusListener, KeyEventDispatcher {
	
	private static final long serialVersionUID = 1L;
	
	// Peripheral GUI elements
	protected AboutBox           aboutBox;
	protected JDialog           prefsDialog;
	private   JEXLogOnPanel      logOnFrame;
	private   JEXDatabaseChooser dbChooser;
	
	public JEXperiment() {
		// Start the statics class
		JEXStatics.main               = this            ;
		
		// Load the manager
		JEXManager jexManager         = new JEXManager();
		JEXStatics.jexManager         = jexManager      ;
		
		// Load the database manager
		JEXDatabaseManager jexDBManager = new JEXDatabaseManager();
		JEXStatics.jexDBManager         = jexDBManager      ;
		
		// Load the icon repository
		IconRepository iconRepository = new IconRepository();
		JEXStatics.iconRepository     = iconRepository  ;
		
		// Load the logManager
		LogManager logManager         = new LogManager();
		JEXStatics.logManager         = logManager      ;
		
		// Load the status bar
		StatusBar statusBar           = new StatusBar();
		JEXStatics.statusBar          = statusBar       ;
		
		// Load temporary preferences that does not come from a user file (will be overwritten upon login)
		PrefsUtility.preferences = new XPreferences();
		PrefsUtility.initializeAnyNewPrefs();
		
		// Load the function cruncher
		Cruncher cruncher              = new Cruncher();
		JEXStatics.cruncher            = cruncher;
//		DatabaseEmulatorPool emulatorPool = new DatabaseEmulatorPool();
//		JEXStatics.emulatorPool           = emulatorPool;
		
		// Load the label color code
		JEXLabelColorCode labelColorCode = new JEXLabelColorCode();
		JEXStatics.labelColorCode        = labelColorCode;
		JEXStatics.labelManager 		 = new DatabaseLabelManager();
		
		// Set the current panel to null
		JEXStatics.currentPane		= null			 ;
		
		// Properties of this window
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(false);
		setTitle("Je'Xperiment - Databasing made for the biologist");
		
		// initialize all the windows
		initialize();
		
		// End of loading display main JEX window
		showLogOnFrame(true);
	}
	
	/**
	 * Show a frame with user file loading options
	 * @param display
	 */
	public void showLogOnFrame(boolean display)
	{
		if (display)
		{
			logOnFrame = new JEXLogOnPanel();

			logOnFrame.setUndecorated(false);
			logOnFrame.setBounds(300, 150, 700, 500);
			logOnFrame.setVisible(true);
		}
		else
		{
			logOnFrame.dispose();
		}
	}
	
	/**
	 * Show a panel with all the available databases
	 * @param display
	 */
	public void showDatabaseChooserFrame(boolean display)
	{
		if (display)
		{
			dbChooser = new JEXDatabaseChooser();

			dbChooser.rebuild();
			dbChooser.setUndecorated(false);
			dbChooser.setBounds(300, 150, 700, 500);
			dbChooser.setVisible(true);
		}
		else
		{
			dbChooser.dispose();
		}
	}
	
	/**
	 * Displays a waiting cursor on the main jex window
	 * @param waiting
	 */
	public void setWaitingCursor(boolean waiting){
		if (waiting){
			JEXStatics.logManager.log("setting cursor to waiting mode",1,this);
			Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
			this.setCursor(hourglassCursor);
		}
		else {
//			Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
			JEXStatics.logManager.log("setting cursor to normal mode",1,this);
			this.setCursor(null);
		}
	}
	
	/**
	 * Enable the display of the main jex window
	 * @param display
	 */
	public void showMainJEXWindow(boolean display)
	{
		if (display)
		{
			this.setVisible(true);
		}
		else
		{
			this.setVisible(false);
		}
	}
	
	// ----------------------------------------------------
	// --------- PLOT THE WINDOWS AND DRAW GUI ------------
	// ----------------------------------------------------
	
	// JEX views
	public JPanel menuPane = new JPanel();
	public JPanel centerPane = new JPanel();
	public JSplitPane  menuSplitPane   = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	public JSplitPane  centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	
	public EntryAndObjectPanel leftPanel ; // LEFT PANEL
	
	// Panel size
	public int topLeftX     = 60  ;
	public int topLeftY     = 20  ;
	public int windowWidth  = 1150;
	public int windowHeight = 700 ;
	
	/**
	 * Initialize the GUI
	 */
	public void initialize(){
		// Setup panel
		this.setBounds(topLeftX, topLeftY, windowWidth, windowHeight);
		this.setResizable(true);
		this.setTitle("Je'Xperiment - Databasing made for the biologist");
		
		// Get the content panel
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(DisplayStatics.background);
		
		// Add the menu bar
		createMenuBar();
		contentPane.add(menuPane,BorderLayout.PAGE_START);
		
		// Prepare the panels in the main split pane
		centerPane.setBackground(DisplayStatics.background);
		centerPane.setLayout(new BorderLayout());
		centerPane.add(Box.createRigidArea(new Dimension(50,50)));
		
		// Prepare the left split pane
		leftPanel = new EntryAndObjectPanel();
		leftPanel.setPreferredSize(new Dimension(300,300));
		
		// Prepare the right split panel
		centerSplitPane.setBackground(DisplayStatics.background);
		centerSplitPane.setBorder(null);
		centerSplitPane.setDividerLocation(300);
		centerSplitPane.setDividerSize(6);
		centerSplitPane.setResizeWeight(1.0);
		centerSplitPane.setLeftComponent(centerPane);
		centerSplitPane.setRightComponent(new JPanel());
		
		// Add the right split panel
		menuSplitPane.setBackground(DisplayStatics.background);
		menuSplitPane.setBorder(null);
		menuSplitPane.setDividerLocation(200);
		menuSplitPane.setDividerSize(6);
		menuSplitPane.setResizeWeight(0.0);
		menuSplitPane.setLeftComponent(leftPanel);
		menuSplitPane.setRightComponent(centerSplitPane);
		contentPane.add(menuSplitPane,BorderLayout.CENTER);
		
		// Add the status bar
		new Thread(JEXStatics.statusBar).start();
		contentPane.add(JEXStatics.statusBar,BorderLayout.PAGE_END);
		
		// Set the key listener
		setQuickKeys();
	}
	
	// menu items
	public SignalMenuButton save                    = new SignalMenuButton();
	public SignalMenuButton arrayCreationButton     = new SignalMenuButton();
	public SignalMenuButton fileDistributionButton  = new SignalMenuButton();
	public SignalMenuButton labelDistributionButton = new SignalMenuButton();
	public SignalMenuButton viewView                = new SignalMenuButton();
	public SignalMenuButton notesView               = new SignalMenuButton();
	public SignalMenuButton batchFunctionButton     = new SignalMenuButton();
	public SignalMenuButton statAnalysis            = new SignalMenuButton();
	public SignalMenuButton pluginsButton           = new SignalMenuButton();
	public SignalMenuButton prefsButton             = new SignalMenuButton();
	public SignalMenuButton bookmark                = new SignalMenuButton();
	
	/**
	 * Create the top menu bar
	 */
	public void createMenuBar(){
		int spacing = 8;
		menuPane.setBackground(DisplayStatics.menuBackground);
		menuPane.setPreferredSize(new Dimension(300,60));
		menuPane.setLayout(new BoxLayout(menuPane,BoxLayout.LINE_AXIS));
		
		Image greyBox        = JEXStatics.iconRepository.boxImage(30, 30,DisplayStatics.dividerColor);
		
		Image iconSave        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_SAVE, 30, 30);
		Image iconOverSave    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_SAVE, 30, 30);
		Image iconPressedSave = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_SAVE, 30, 30);
		save.setBackgroundColor(DisplayStatics.menuBackground);
		save.setForegroundColor(DisplayStatics.lightBackground);
		save.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		save.setSize(new Dimension(100,50));
		save.setImage(iconSave);
		save.setMouseOverImage(iconOverSave);
		save.setMousePressedImage(iconPressedSave);
		save.setDisabledImage(greyBox);
		save.setText("Save");
		SSCenter.defaultCenter().connect(save, SignalMenuButton.SIG_ButtonClicked_NULL, this, "save", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(save);
		
		menuPane.add(Box.createHorizontalGlue());

		Image iconCreation        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_CREATE_UNSELECTED, 30, 30);
		Image iconOverCreation    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_CREATE_UNSELECTED, 30, 30);
		Image iconPressedCreation = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_CREATE_UNSELECTED, 30, 30);
		arrayCreationButton.setBackgroundColor(DisplayStatics.menuBackground);
		arrayCreationButton.setForegroundColor(DisplayStatics.lightBackground);
		arrayCreationButton.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		arrayCreationButton.setSize(new Dimension(100,50));
		arrayCreationButton.setImage(iconCreation);
		arrayCreationButton.setMouseOverImage(iconOverCreation);
		arrayCreationButton.setMousePressedImage(iconPressedCreation);
		arrayCreationButton.setDisabledImage(greyBox);
		arrayCreationButton.setText("Create");
		SSCenter.defaultCenter().connect(arrayCreationButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayCreationPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(arrayCreationButton);
		
		Image iconDistribution        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_DISTRIBUTE_UNSELECTED, 30, 30);
		Image iconOverDistribution    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_DISTRIBUTE_UNSELECTED, 30, 30);
		Image iconPressedDistribution = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_DISTRIBUTE_UNSELECTED, 30, 30);
		fileDistributionButton.setBackgroundColor(DisplayStatics.menuBackground);
		fileDistributionButton.setForegroundColor(DisplayStatics.lightBackground);
		fileDistributionButton.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		fileDistributionButton.setSize(new Dimension(100,50));
		fileDistributionButton.setImage(iconDistribution);
		fileDistributionButton.setMouseOverImage(iconOverDistribution);
		fileDistributionButton.setMousePressedImage(iconPressedDistribution);
		fileDistributionButton.setDisabledImage(greyBox);
		fileDistributionButton.setText("Import");
		SSCenter.defaultCenter().connect(fileDistributionButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayDistributionPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(fileDistributionButton);

		Image iconLabel        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_LABEL_UNSELECTED, 30, 30);
		Image iconOverLabel    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_LABEL_UNSELECTED, 30, 30);
		Image iconPressedLabel = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_LABEL_UNSELECTED, 30, 30);
		labelDistributionButton.setBackgroundColor(DisplayStatics.menuBackground);
		labelDistributionButton.setForegroundColor(DisplayStatics.lightBackground);
		labelDistributionButton.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		labelDistributionButton.setSize(new Dimension(100,50));
		labelDistributionButton.setImage(iconLabel);
		labelDistributionButton.setMouseOverImage(iconOverLabel);
		labelDistributionButton.setMousePressedImage(iconPressedLabel);
		labelDistributionButton.setDisabledImage(greyBox);
		labelDistributionButton.setText("Label");
		SSCenter.defaultCenter().connect(labelDistributionButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayLabelPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(labelDistributionButton);

		Image iconNotes        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_NOTES_UNSELECTED, 30, 30);
		Image iconOverNotes    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_NOTES_UNSELECTED, 30, 30);
		Image iconPressedNotes = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_NOTES_UNSELECTED, 30, 30);
		notesView.setBackgroundColor(DisplayStatics.menuBackground);
		notesView.setForegroundColor(DisplayStatics.lightBackground);
		notesView.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		notesView.setSize(new Dimension(100,50));
		notesView.setImage(iconNotes);
		notesView.setMouseOverImage(iconOverNotes);
		notesView.setMousePressedImage(iconPressedNotes);
		notesView.setText("Notes");
		SSCenter.defaultCenter().connect(notesView, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayNotesPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(notesView);

		Image iconFunction        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_FUNCTION_UNSELECTED, 30, 30);
		Image iconOverFunction    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_FUNCTION_UNSELECTED, 30, 30);
		Image iconPressedFunction = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_FUNCTION_UNSELECTED, 30, 30);
		batchFunctionButton.setBackgroundColor(DisplayStatics.menuBackground);
		batchFunctionButton.setForegroundColor(DisplayStatics.lightBackground);
		batchFunctionButton.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		batchFunctionButton.setSize(new Dimension(100,50));
		batchFunctionButton.setImage(iconFunction);
		batchFunctionButton.setMouseOverImage(iconOverFunction);
		batchFunctionButton.setMousePressedImage(iconPressedFunction);
		batchFunctionButton.setDisabledImage(greyBox);
		batchFunctionButton.setText("Process");
		SSCenter.defaultCenter().connect(batchFunctionButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayFunctionPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(batchFunctionButton);

		Image iconStats        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_STATS_UNSELECTED, 30, 30);
		Image iconOverStats    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_STATS_UNSELECTED, 30, 30);
		Image iconPressedStats = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_STATS_UNSELECTED, 30, 30);
		statAnalysis.setBackgroundColor(DisplayStatics.menuBackground);
		statAnalysis.setForegroundColor(DisplayStatics.lightBackground);
		statAnalysis.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		statAnalysis.setSize(new Dimension(100,50));
		statAnalysis.setImage(iconStats);
		statAnalysis.setMouseOverImage(iconOverStats);
		statAnalysis.setMousePressedImage(iconPressedStats);
		statAnalysis.setDisabledImage(greyBox);
		statAnalysis.setText("Statistics");
		SSCenter.defaultCenter().connect(statAnalysis, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayStatisticsPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(statAnalysis);

		Image iconPlugins        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PLUGIN_UNSELECTED, 30, 30);
		Image iconOverPlugins    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PLUGIN_UNSELECTED, 30, 30);
		Image iconPressedPlugins = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PLUGIN_UNSELECTED, 30, 30);
		pluginsButton.setBackgroundColor(DisplayStatics.menuBackground);
		pluginsButton.setForegroundColor(DisplayStatics.lightBackground);
		pluginsButton.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		pluginsButton.setSize(new Dimension(100,50));
		pluginsButton.setImage(iconPlugins);
		pluginsButton.setMouseOverImage(iconOverPlugins);
		pluginsButton.setMousePressedImage(iconPressedPlugins);
		pluginsButton.setDisabledImage(greyBox);
		pluginsButton.setText("Plugins");
		SSCenter.defaultCenter().connect(pluginsButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayPluginsPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(pluginsButton);

		Image iconView        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_VIEW_UNSELECTED, 30, 30);
		Image iconOverView    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_VIEW_UNSELECTED, 30, 30);
		Image iconPressedView = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_VIEW_UNSELECTED, 30, 30);
		viewView.setBackgroundColor(DisplayStatics.menuBackground);
		viewView.setForegroundColor(DisplayStatics.lightBackground);
		viewView.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		viewView.setSize(new Dimension(100,50));
		viewView.setImage(iconView);
		viewView.setMouseOverImage(iconOverView);
		viewView.setMousePressedImage(iconPressedView);
		viewView.setText("View");
		SSCenter.defaultCenter().connect(viewView, SignalMenuButton.SIG_ButtonClicked_NULL, this, "displayViewPane", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(viewView);

		menuPane.add(Box.createHorizontalGlue());

		Image iconPreferences        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PREFS, 30, 30);
		Image iconOverPreferences    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PREFS_OVER, 30, 30);
		Image iconPressedPreferences = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PREFS_CLICK, 30, 30);
		prefsButton.setBackgroundColor(DisplayStatics.menuBackground);
		prefsButton.setForegroundColor(DisplayStatics.lightBackground);
		prefsButton.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		prefsButton.setSize(new Dimension(100,50));
		prefsButton.setImage(iconPreferences);
		prefsButton.setMouseOverImage(iconOverPreferences);
		prefsButton.setMousePressedImage(iconPressedPreferences);
		prefsButton.setText("Preferences");
		SSCenter.defaultCenter().connect(prefsButton, SignalMenuButton.SIG_ButtonClicked_NULL, this, "openPreferences", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(prefsButton);

		Image iconBookmark        = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_BOOKMARK, 30, 30);
		Image iconOverBookmark    = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_BOOKMARK, 30, 30);
		Image iconPressedBookmark = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_BOOKMARK, 30, 30);
		bookmark.setBackgroundColor(DisplayStatics.menuBackground);
		bookmark.setForegroundColor(DisplayStatics.lightBackground);
		bookmark.setClickedColor(DisplayStatics.menuBackground, DisplayStatics.lightBackground);
		bookmark.setSize(new Dimension(100,50));
		bookmark.setImage(iconBookmark);
		bookmark.setMouseOverImage(iconOverBookmark);
		bookmark.setMousePressedImage(iconPressedBookmark);
		bookmark.setText("Bookmark");
		SSCenter.defaultCenter().connect(bookmark, SignalMenuButton.SIG_ButtonClicked_NULL, this, "bookmark", (Class[])null);
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(bookmark);
		
		menuPane.add(Box.createHorizontalStrut(spacing));
		menuPane.add(Box.createHorizontalStrut(spacing));
	}
	
	public void unselectedAllIcons()
	{
		Image iconCreation = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_CREATE_UNSELECTED, 30, 30);
		arrayCreationButton.setImage(iconCreation);
		arrayCreationButton.setMouseOverImage(iconCreation);
		arrayCreationButton.setMousePressedImage(iconCreation);
		
		Image iconDistribution = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_DISTRIBUTE_UNSELECTED, 30, 30);
		fileDistributionButton.setImage(iconDistribution);
		fileDistributionButton.setMouseOverImage(iconDistribution);
		fileDistributionButton.setMousePressedImage(iconDistribution);
		
		Image iconLabel = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_LABEL_UNSELECTED, 30, 30);
		labelDistributionButton.setImage(iconLabel);
		labelDistributionButton.setMouseOverImage(iconLabel);
		labelDistributionButton.setMousePressedImage(iconLabel);
		
		Image iconNotes = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_NOTES_UNSELECTED, 30, 30);
		notesView.setImage(iconNotes);
		notesView.setMouseOverImage(iconNotes);
		notesView.setMousePressedImage(iconNotes);
		
		Image iconFunction = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_FUNCTION_UNSELECTED, 30, 30);
		batchFunctionButton.setImage(iconFunction);
		batchFunctionButton.setMouseOverImage(iconFunction);
		batchFunctionButton.setMousePressedImage(iconFunction);
		
		Image iconStats = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_STATS_UNSELECTED, 30, 30);
		statAnalysis.setImage(iconStats);
		statAnalysis.setMouseOverImage(iconStats);
		statAnalysis.setMousePressedImage(iconStats);
		
		Image iconPlugins = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PLUGIN_UNSELECTED, 30, 30);
		pluginsButton.setImage(iconPlugins);
		pluginsButton.setMouseOverImage(iconPlugins);
		pluginsButton.setMousePressedImage(iconPlugins);
		
		Image iconView = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_VIEW_UNSELECTED, 30, 30);
		viewView.setImage(iconView);
		viewView.setMouseOverImage(iconView);
		viewView.setMousePressedImage(iconView);
	}
	
	public void displayCreationPane()
	{
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXCreationPanelController creationPane = new JEXCreationPanelController();
		JEXStatics.creationPane = creationPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.creationPane;
		
		// Set the new panel size
		JEXStatics.creationPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
//		// Change the icons
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.creationPane;
//		JEXStatics.creationPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconCreation = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_CREATE_SELECTED, 30, 30);
		arrayCreationButton.setImage(iconCreation);
		arrayCreationButton.setMouseOverImage(iconCreation);
		arrayCreationButton.setMousePressedImage(iconCreation);

		JEXStatics.logManager.log("Displaying the creation view", 0, this);
		changeMainView(JEXStatics.creationPane.getMainPanel());
		changeLeftView(JEXStatics.creationPane.getLeftPanel());
		changeRightView(JEXStatics.creationPane.getRightPanel());
		
	}
	
	public void displayDistributionPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.distribPane;
//		JEXStatics.distribPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXDistributionPanelController distribPane  = new JEXDistributionPanelController();
		JEXStatics.distribPane = distribPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.distribPane;
		
		// Set the new panel size
		JEXStatics.distribPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconDistribution = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_DISTRIBUTE_SELECTED, 30, 30);
		fileDistributionButton.setImage(iconDistribution);
		fileDistributionButton.setMouseOverImage(iconDistribution);
		fileDistributionButton.setMousePressedImage(iconDistribution);

		JEXStatics.logManager.log("Displaying the distribution panel", 0, this);
		changeMainView(JEXStatics.distribPane.getMainPanel());
		changeLeftView(JEXStatics.distribPane.getLeftPanel());
		changeRightView(JEXStatics.distribPane.getRightPanel());
	}
	
	public void displayLabelPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.labelPane;
//		JEXStatics.labelPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXLabelPanelController        labelPane    = new JEXLabelPanelController();
		JEXStatics.labelPane = labelPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.labelPane;
		
		// Set the new panel size
		JEXStatics.labelPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconLabel = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_LABEL_SELECTED, 30, 30);
		labelDistributionButton.setImage(iconLabel);
		labelDistributionButton.setMouseOverImage(iconLabel);
		labelDistributionButton.setMousePressedImage(iconLabel);

		JEXStatics.logManager.log("Displaying the label panel", 0, this);
		changeMainView(JEXStatics.labelPane.getMainPanel());
		changeLeftView(JEXStatics.labelPane.getLeftPanel());
		changeRightView(JEXStatics.labelPane.getRightPanel());
	}
	
	public void displayNotesPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.notesPane;
//		JEXStatics.viewPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXNotesPanelController        notesPane    = new JEXNotesPanelController();
		JEXStatics.notesPane = notesPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.notesPane;
		
		// Set the new panel size
		JEXStatics.notesPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconNotes = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_NOTES_SELECTED, 30, 30);
		notesView.setImage(iconNotes);
		notesView.setMouseOverImage(iconNotes);
		notesView.setMousePressedImage(iconNotes);

		JEXStatics.logManager.log("Displaying the notes panel", 0, this);
		changeMainView(JEXStatics.notesPane.getMainPanel());
		changeLeftView(JEXStatics.notesPane.getLeftPanel());
		changeRightView(JEXStatics.notesPane.getRightPanel());
		
	}
	
	public void displayViewPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.viewPane;
//		JEXStatics.viewPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXViewPanelController         viewPane     = new JEXViewPanelController();
		JEXStatics.viewPane = viewPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.viewPane;
		
		// Set the new panel size
		JEXStatics.viewPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconNotes = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_VIEW_SELECTED, 30, 30);
		viewView.setImage(iconNotes);
		viewView.setMouseOverImage(iconNotes);
		viewView.setMousePressedImage(iconNotes);

		JEXStatics.logManager.log("Displaying the view panel", 0, this);
		changeMainView(JEXStatics.viewPane.getMainPanel());
		changeLeftView(JEXStatics.viewPane.getLeftPanel());
		changeRightView(JEXStatics.viewPane.getRightPanel());
		
	}
	
	public void displayFunctionPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.functionPane;
//		JEXStatics.functionPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXFunctionPanelController     functionPane = new JEXFunctionPanelController();
		JEXStatics.functionPane = functionPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.functionPane;
		
		// Set the new panel size
		JEXStatics.functionPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconFunction = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_FUNCTION_SELECTED, 30, 30);
		batchFunctionButton.setImage(iconFunction);
		batchFunctionButton.setMouseOverImage(iconFunction);
		batchFunctionButton.setMousePressedImage(iconFunction);

		JEXStatics.logManager.log("Displaying the processing panel", 0, this);
		changeMainView(JEXStatics.functionPane.getMainPanel());
		changeLeftView(JEXStatics.functionPane.getLeftPanel());
		changeRightView(JEXStatics.functionPane.getRightPanel());
	}
	
	public void displayPluginsPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
//		JEXStatics.currentPane = JEXStatics.pluginPane;
//		JEXStatics.pluginPane.imposeSplitPaneOptions(centerSplitPane);
//		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXPluginPanelController       pluginPane   = new JEXPluginPanelController();
		JEXStatics.pluginPane = pluginPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.pluginPane;
		
		// Set the new panel size
		JEXStatics.pluginPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconPlugins = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_PLUGIN_SELECTED, 30, 30);
		pluginsButton.setImage(iconPlugins);
		pluginsButton.setMouseOverImage(iconPlugins);
		pluginsButton.setMousePressedImage(iconPlugins);

		JEXStatics.logManager.log("Displaying the plugin panel", 0, this);
		changeMainView(JEXStatics.pluginPane.getMainPanel());
		changeLeftView(JEXStatics.pluginPane.getLeftPanel());
		changeRightView(JEXStatics.pluginPane.getRightPanel());
	}

	/**
	 * Switch to the statistics display
	 */
	public void displayStatisticsPane()
	{
//		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
		
		// Reset views
		resetViews();
		
		// Create a contoller
		JEXStatisticsPanelController statPane = new JEXStatisticsPanelController();
		JEXStatics.statPane = statPane;
		
		// set current pane
		JEXStatics.currentPane = JEXStatics.statPane;
		
		// Set the new panel size
		JEXStatics.statPane.imposeSplitPaneOptions(centerSplitPane);
		JEXTabPanelController.setLeftPanelWidth(menuSplitPane.getDividerLocation());
		
		// Change the icons
		unselectedAllIcons();
		Image iconStats = JEXStatics.iconRepository.getImageWithName(IconRepository.MAIN_STATS_SELECTED, 30, 30);
		statAnalysis.setImage(iconStats);
		statAnalysis.setMouseOverImage(iconStats);
		statAnalysis.setMousePressedImage(iconStats);
		
		// Display the panels
		JEXStatics.logManager.log("Displaying the statistics builder", 0, this);
		changeMainView(JEXStatics.statPane.getMainPanel());
		changeLeftView(JEXStatics.statPane.getLeftPanel());
		changeRightView(JEXStatics.statPane.getRightPanel());
	}
	
	public void resetViews()
	{
		// Send close signal to current panel
		if (JEXStatics.currentPane != null) JEXStatics.currentPane.closeTab();
		
		// Save the current size settings
		if(JEXStatics.currentPane != null) JEXStatics.currentPane.saveSplitPaneOptions(centerSplitPane);
		
		// Set to null every controller 
		JEXStatics.creationPane     = null       ;
		JEXStatics.distribPane      = null       ;
		JEXStatics.labelPane        = null       ;
		JEXStatics.notesPane        = null       ;
		JEXStatics.functionPane     = null       ;
		JEXStatics.statPane         = null       ;
		JEXStatics.viewPane         = null       ;
		JEXStatics.pluginPane       = null       ;
	}
	
	/**
	 * Replace center view by the chosen view
	 * @param newView
	 */
	private void changeMainView(JPanel newView){
		JEXStatics.logManager.log("Changing view", 0, this);
		
		centerPane.removeAll();
		centerPane.add(newView,BorderLayout.CENTER);
		centerPane.revalidate();
		centerPane.repaint();
	}
	
	/**
	 * Change the panel in the left side of the display
	 * @param leftView
	 */
	private void changeLeftView(JPanel leftView)
	{
		JEXStatics.logManager.log("Changing left view", 0, this);
		if (leftView == null)
		{
			menuSplitPane.setLeftComponent(leftPanel);
			menuSplitPane.setDividerLocation(JEXViewPanelController.getLeftPanelWidth());
		}
		else
		{
			menuSplitPane.setLeftComponent(leftView);
		}
		
		this.repaint();
	}
	
	/**
	 * Change the panel in the left side of the display
	 * @param leftView
	 */
	private void changeRightView(JPanel rightView)
	{
		JEXStatics.logManager.log("Changing right view", 0, this);
		if (rightView == null)
		{
			centerSplitPane.setRightComponent(new JPanel());
		}
		else
		{
			centerSplitPane.setRightComponent(rightView);
		}
		// This is to refresh views to the appropriate navigation depth
		// We do it here because we always load the RightView last
		SSCenter.defaultCenter().emit(JEXStatics.jexManager, JEXManager.NAVIGATION, (Object[])null);
		this.repaint();
	}
	
	/**
	 * Display a panel on the glass pane
	 * @param pane
	 * @return
	 */
	public boolean displayGlassPane(JPanel pane, boolean on){
		
		if (pane == null){
			Component c = this.getGlassPane();
			c.setVisible(false);
			return false;
		}
		
		if (on){
			pane.setOpaque(true); 
			setGlassPane(pane);
			pane.setVisible(true);
			return true;
		}
		else {
			pane.setOpaque(false); 
			setGlassPane(pane);
			pane.setVisible(false);
			return true;
		}
		
	}
				
	/**
	 * Open the user file at location FILE
	 * @param file
	 */
	public void openUser(File file)
	{
		JEXStatics.logManager.log("Opening user file "+file.getName(), 0, this);
        
        // Load the user file
        boolean done = JEXStatics.jexManager.logOn(file);
        if (!done) return;
        
        // Change the panel
		JEXStatics.main.displayViewPane();
	}
	
	// ----------------------------------------------------
	// --------- GENERAL METHODS --------------------------
	// ----------------------------------------------------

	/**
	 * Quit the program
	 */
	public void quit(){
		System.exit(0);
	}
	
	/**
	 * Save the opened database
	 */
	public void save(){
		JEXStatics.logManager.log("Saving requested", 1, this);
//		String consolidateStr = JEXStatics.userPreferences.get("Consolidate Database", "false");
//		Boolean consolidate   = Boolean.parseBoolean(consolidateStr);
		JEXStatics.jexManager.saveCurrentDatabase();
	}
	
	/**
	 * Make a bookmark
	 */
	public void bookmark(){
		if (!JEXStatics.jexManager.isLoggedOn()) return;
		if (JEXStatics.jexManager.getCurrentDatabase() == null) return;
		
		DialogGlassPane diagPanel = new DialogGlassPane("Warning");
		diagPanel.setSize(400, 200);

		ErrorMessagePane errorPane = new ErrorMessagePane("Bookmarks are not implemented yet... ");
		diagPanel.setCentralPanel(errorPane);

		JEXStatics.main.displayGlassPane(diagPanel,true);
	}
	
	/**
	 * Open preference panel
	 */
	public void openPreferences()
	{
		// Reload from file to get rid of any unsaved changes
		PrefsUtility.reloadPrefs();
		XPreferencePanelController prefs = new XPreferencePanelController(PrefsUtility.getUserPrefs(), true);
		prefsDialog = new JDialog(this,"JEX Preferences",Dialog.ModalityType.APPLICATION_MODAL);
		prefsDialog.addWindowListener(this);
		prefsDialog.getContentPane().setLayout(new BorderLayout());
		prefsDialog.getContentPane().add(prefs.getPanel(), BorderLayout.CENTER);
		SSCenter.defaultCenter().connect(prefs, XPreferencePanelController.SIG_Save_NULL, this, "savePrefs", (Class[])null);
		SSCenter.defaultCenter().connect(prefs, XPreferencePanelController.SIG_Cancel_NULL, this, "cancelPrefs", (Class[])null);
		prefsDialog.setSize(500, 500);
		prefsDialog.setVisible(true);
		
	}
		
	// ----------------------------------------------------
	// --------- EVENT HANDLING FUNCTIONS -----------------
	// ----------------------------------------------------
	
    public void actionPerformed (ActionEvent e) { }
    
    public void savePrefs()
    {
    	PrefsUtility.savePrefs();
    }
    
    public void cancelPrefs()
    {
    	prefsDialog.setVisible(false);
    	PrefsUtility.reloadPrefs();
    	prefsDialog.dispatchEvent(new WindowEvent(prefsDialog, WindowEvent.WINDOW_CLOSING));
    }
	
	private void setQuickKeys()
	{		
		// Allow JEX to prelisten to KeyEvents to capture modifier key states before processing by components in focus
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		
		// Always add the actions to the action map of the menuPane because that is the one that never changes
		// If it does ever change, call setQuickKeys again to reconnect quick keys
		KeyStroke stroke;
		
		// Save action
	    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK, false);
	    this.menuPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "save");
	    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, false);
	    this.menuPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "save");
	    this.menuPane.getActionMap().put("save", new ActionSave());
	    
	    // Other actions
	    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_DOWN_MASK, false);
	    this.menuPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "abortGuiTask");
	    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false);
	    this.menuPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "abortGuiTask");
	    this.menuPane.getActionMap().put("abortGuiTask", new ActionAbortGuiTask());
	    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK, false);
	    this.menuPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "abortCrunch");
	    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, false);
	    this.menuPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "abortCrunch");
	    this.menuPane.getActionMap().put("abortCrunch", new ActionAbortCrunch());
	}
	
	public void keyTyped(KeyEvent e){}
    public void keyReleased(KeyEvent e) {}

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e)
	{
		if(e.getSource() == this)
		{
			JEXStatics.logManager.log("Dispose the window and exit", 0, this);
			if(R.isConnected())
			{
				R.close();
			}
			this.dispose();
			quit();
		}
		else if(e.getSource() == this.prefsDialog)
		{
			PrefsUtility.reloadPrefs();
		}
		
	}
	public void windowClosing(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}	
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
    
    // -------------------------------------
    // --------- Main functions ---------
    // -------------------------------------
    public static void main(final String args[]) 
    {    	
    	Runnable runner = new Runnable()
    	{
    		public void run()
    		{
    			// set default font
    			Font font = new Font("sans serif", Font.PLAIN, 11);
    			UIManager.put("Button.font", font);
    			UIManager.put("ToggleButton.font", font);
    			UIManager.put("RadioButton.font", font);
    			UIManager.put("CheckBox.font", font);
    			UIManager.put("ColorChooser.font", font);
    			UIManager.put("ComboBox.font", font);
    			UIManager.put("Label.font", font);
    			UIManager.put("List.font", font);
    			UIManager.put("MenuBar.font", font);
    			UIManager.put("MenuItem.font", font);
    			UIManager.put("RadioButtonMenuItem.font", font);
    			UIManager.put("CheckBoxMenuItem.font", font);
    			UIManager.put("Menu.font", font);
    			UIManager.put("PopupMenu.font", font);
    			UIManager.put("OptionPane.font", font);
    			UIManager.put("Panel.font", font);
    			UIManager.put("ProgressBar.font", font);
    			UIManager.put("ScrollPane.font", font);
    			UIManager.put("Viewport.font", font);
    			UIManager.put("TabbedPane.font", font);
    			UIManager.put("Table.font", font);
    			UIManager.put("TableHeader.font", font);
    			UIManager.put("TextField.font", font);
    			UIManager.put("PasswordField.font", font);
    			UIManager.put("TextArea.font", font);
    			UIManager.put("TextPane.font", font);
    			UIManager.put("EditorPane.font", font);
    			UIManager.put("TitledBorder.font", font);
    			UIManager.put("ToolBar.font", font);
    			UIManager.put("ToolTip.font", font);
    			UIManager.put("Tree.font", font);
    			UIManager.put("Label.font", font);
    			JEXperiment jex = new jex.JEXperiment();

    			if (args != null && args.length > 0)
    			{
    				String arg1 = args[0];
    				File file   = new File(arg1);
    				if (file.exists())
    				{
    					jex.openUser(file);
    				}
    			}
    		}
    	};
    	SwingUtilities.invokeLater(runner);
    }

	public void windowGainedFocus(WindowEvent arg0) {
		this.getRootPane().requestFocusInWindow();
	}

	public void windowLostFocus(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("serial")
	public class ActionSave extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
	    {
			JEXStatics.main.save();
	    	JEXStatics.logManager.log("Saving", 0, this);
	    }
	}
	
	@SuppressWarnings("serial")
	public class ActionAbortGuiTask extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
	    {
			JEXStatics.cruncher.stopGuiTask = true;
	    	JEXStatics.logManager.log("Aborting Gui Task!", 0, this);
	    }
	}
	
	@SuppressWarnings("serial")
	public class ActionAbortCrunch extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
	    {
			JEXStatics.cruncher.stopCrunch = true;
	    	JEXStatics.logManager.log("Aborting Crunch!", 0, this);
	    }
	}

	public boolean dispatchKeyEvent(KeyEvent e)
	{
		KeyStatics.captureModifiers(e);
		// KeyStatics.printModifiers();
		return false; // Allow others to respond to the keyEvent
	}

}
