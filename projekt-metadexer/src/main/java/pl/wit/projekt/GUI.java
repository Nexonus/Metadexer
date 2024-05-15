package pl.wit.projekt;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Jan Konarski - ID06PD1 - 20356
 * @category GUI
 * Implementacja interfejsu graficznego - Metadexer
 */
public class GUI extends JFrame implements ActionListener, WindowListener {
	
	private Metadata metadata;
	private static final long serialVersionUID = -5574138120807033215L;
	/// Create TextFields
	private JTextField tbInputPath = new JTextField("",60);
	private JTextField tbOutputPath = new JTextField("",60);
	private JTextField tbThreadCount = new JTextField("",1);
	
	/// Create fileChoosers
	private JFileChooser fcInputChooser = new JFileChooser(new File("..\\"));	// default start directory
	private JFileChooser fcOutputChooser = new JFileChooser(new File("..\\"));
	// Create Buttons
	private JButton btnInputFolder = new JButton("Choose Input Folder");
	private JButton btnOutputFolder = new JButton("Choose Output Folder");
	private JButton btnStart = new JButton("Start");
	private JButton btnCleanup = new JButton("Cleanup");
	
	// Create Panels
	private JPanel pnContentPane = new JPanel(new BorderLayout());
	private	JPanel pnFolderPane = new JPanel(new BorderLayout());
	private JPanel pnInputPane = new JPanel(new GridLayout(1,3));
	private JPanel pnOutputPane = new JPanel(new GridLayout(1,2));
	private JPanel pnStartPane = new JPanel(new BorderLayout());
	private JPanel pnStartPaneSplit = new JPanel(new GridLayout(1,4));
	private JPanel pnStartPaneBottom = new JPanel(new BorderLayout());
	//private JPanel pnProgressBarPane = new JPanel(new GridLayout(2,1));
	
	private JPanel pnThreadNumberPane = new JPanel(new GridLayout(1,2));
	
	/// Output log components
	private JTextPane tpScrollPane = new JTextPane();
	private JScrollPane spScrollPane = new JScrollPane(tpScrollPane);
	
	private JLabel lbOutputLogLabel = new JLabel("Output log:");
	private JLabel lbThreadUsageLabel = new JLabel();
	
	//private JProgressBar pbProgressBar = new JProgressBar();
	
	private String outputPathDirectory;
	private String inputPathDirectory;
	private Integer threadCount;
	private Integer progressValue;
	private Integer maxProcessors = Runtime.getRuntime().availableProcessors();
	private Integer confirm=0;
	private long startTime;
	private long finishTime;
	
	private Map<Integer, Long> measurementMap = new HashMap<Integer, Long>();
	
	// Initialize GUI window in a main function
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new GUI();
	}
	/**
	 * 
	 * @throws IOException
	 */
	public GUI() throws IOException {
		super("Metadexer");	// Window title

		lbThreadUsageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		tpScrollPane.setBackground(Color.BLACK);
		spScrollPane.setBorder(new EmptyBorder(5,5,5,5));
		
		/// Create a new 750 x 450 px window for GUI, default pane is pnContentPane:
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100,100,750,450);
		
		pnContentPane.setBorder(new EmptyBorder(5,5,5,5));
		pnStartPane.setBorder(new EmptyBorder(2,2,2,2));
		pnFolderPane.setBorder(new EmptyBorder(2,2,2,2));
		
		pnStartPaneBottom.setBorder(new EmptyBorder(1,4,1,4));
		lbOutputLogLabel.setBorder(new EmptyBorder(5,5,5,5));
		
		
		btnCleanup.setBackground(Color.RED);
		btnCleanup.setForeground(Color.WHITE);
		
		lbThreadUsageLabel.setText("0 < Threads < "+(maxProcessors+1));
		
		//pbProgressBar.setStringPainted(true);
		
		//pnContentPane.setLayout(new FlowLayout(FlowLayout.CENTER,15,5)); // create flowLayout (flexBox)
		setContentPane(pnContentPane);
				
		/// Set debug colors for each panel
		pnContentPane.setBackground(Color.WHITE); 
		pnFolderPane.setBackground(Color.WHITE); 
		pnStartPane.setBackground(Color.LIGHT_GRAY);
		pnStartPaneSplit.setBackground(Color.WHITE);
		pnStartPaneBottom.setBackground(Color.WHITE);
		
		/// Add Icons

		ArrayList<Image>iconList = new ArrayList<Image>();
		URL iconPath = GUI.class.getResource("/icon32.png");
		Image icon = ImageIO.read(iconPath);
		iconList.add(icon);
		
		
		iconPath = GUI.class.getResource("/icon64.png");
		icon = ImageIO.read(iconPath);
		iconList.add(icon);
		
		this.setIconImages(iconList);
		
		/// Add ActionListeners to buttons
		btnInputFolder.addActionListener(this);
		btnOutputFolder.addActionListener(this);
		btnStart.addActionListener(this);
		btnCleanup.addActionListener(this);
		
		tbInputPath.addActionListener(this);
		
		/// Choose Directories with fileChoosers
		fcInputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fcOutputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		/// Add components to the panels
		pnInputPane.add(tbInputPath, BorderLayout.EAST);
		pnInputPane.add(btnInputFolder,BorderLayout.CENTER);
		pnFolderPane.add(pnInputPane,BorderLayout.NORTH);
		
		pnOutputPane.add(tbOutputPath,BorderLayout.EAST);
		pnOutputPane.add(btnOutputFolder,BorderLayout.CENTER);
		pnFolderPane.add(pnOutputPane,BorderLayout.SOUTH);
		
		//pnThreadNumberPane.add(lbThreadUsageLabel);
		pnThreadNumberPane.add(tbThreadCount);
		
		// Add panels to the ContentPane, mostly visual stuff
		//pnStartPaneSplit.add(lbOutputLogLabel,BorderLayout.WEST); // borderlayout.west property non-necessary for any GridLayout if set as so
		pnStartPaneSplit.add(lbThreadUsageLabel);
		
		pnStartPaneSplit.add(pnThreadNumberPane);
		pnStartPaneSplit.add(btnStart,BorderLayout.EAST);		  // mandatory for BorderLayout
		pnStartPaneSplit.add(btnCleanup);
		
		//pnProgressBarPane.add(lbProgress, BorderLayout.NORTH);
		
		//pnProgressBarPane.add(pbProgressBar, BorderLayout.CENTER);
		
		//pnStartPane.add(pnProgressBarPane, BorderLayout.EAST);
		
		pnStartPaneBottom.add(lbOutputLogLabel);
		
		pnStartPane.add(pnStartPaneSplit,BorderLayout.NORTH);
		pnStartPane.add(pnStartPaneBottom, BorderLayout.SOUTH);
		
		//pnStartPane.add(pnProgressBarPane, BorderLayout.SOUTH);
		
		pnContentPane.add(pnStartPane,BorderLayout.CENTER);
		
		pnContentPane.add(pnFolderPane,BorderLayout.NORTH);
		
		
		// Set TextArea from OutputLog to non-editable
		//tpScrollPane.setEditable(false);
		tpScrollPane.setEditable(false);
		
		// Add ScrollPane to the GUI components.
		pnStartPane.add(spScrollPane, BorderLayout.CENTER);
		
		/** Important! **/
		/// Make sure app is visible, off by default! Super important!
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) { // Refactor this method to reduce its Cognitive Complexity from 29 to the 15 allowed. [+18 locations]
		Object source = ae.getSource();
		String strInputPath = tbInputPath.getText();
		String strOutputPath = tbOutputPath.getText();
		
		boolean inputValid = false;
		boolean outputValid = false;
		boolean cleanupStart = false;
		
		int result;
		if (source == btnCleanup) {
			tpScrollPane.setText("");
			final Path path = Paths.get(strOutputPath);
			if (Files.exists(path))
			{
				String dir = System.getProperty("user.dir");
				if (getConfirm() <= 3) {
					setConfirm(getConfirm()+1);
				}
				if (strOutputPath.contains(dir) && !strOutputPath.equals(dir)) {
					try {
						cleanupStart = true;
						this.appendToPane(tpScrollPane, "⚠ You're about to remove "+Metadata.countImageFiles(strOutputPath)+" files from: ".concat(strOutputPath).concat("\nPlease write 'delete' in the Input Box to confirm, then press this button three more times. Confirm: "+getConfirm()+"/"+"3\n\n"), Color.ORANGE, Color.DARK_GRAY);
						if (tbInputPath.getText().equals("delete") && getConfirm() > 3) {
							tpScrollPane.setText("");
							measurementMap.clear();
							setConfirm(0);
							File directory = new File(strOutputPath);
							this.appendToPane(tpScrollPane, "⚠ Deleted: "+Metadata.countImageFiles(strOutputPath)+" files.", Color.ORANGE, Color.DARK_GRAY);
							FileUtils.cleanDirectory(directory);
						}
					} catch (IOException io) {
						io.printStackTrace();
					}
				}
				else {
					this.appendToPane(tpScrollPane, "⚠ Access denied. Make sure the target path is a subdirectory of the path the runnable was launched in.", Color.ORANGE, Color.DARK_GRAY);
				}
			}
			else {
				this.appendToPane(tpScrollPane, "⚠ Directory doesn't exist.", Color.ORANGE, Color.DARK_GRAY);
			}
		}else if (cleanupStart){
			this.appendToPane(tpScrollPane, "⚠ Cleanup cancelled.", Color.ORANGE, Color.DARK_GRAY);
			setConfirm(0);
			cleanupStart = false;
		}
		
		if (source == tbInputPath) {
			try {
				tpScrollPane.setText("");
				measurementMap.clear();
				if (Files.exists(Paths.get(strInputPath))) {
					this.appendToPane(tpScrollPane, "- You're about to copy "+Metadata.countImageFiles(strInputPath)+" images. Press Start to begin.\n", Color.WHITE, Color.DARK_GRAY);
				}else {
					appendToPane(tpScrollPane,"@ Invalid input path: ".concat(strInputPath).concat("\n"), Color.RED, Color.BLACK);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (source == btnInputFolder) {

			result = fcInputChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				if (strInputPath != tbInputPath.getText()) {
					tpScrollPane.setText("");
					measurementMap.clear();
				}
				strInputPath = fcInputChooser.getSelectedFile().getAbsolutePath(); // "getAbsolutePath" returns a string, there's no need to call "toString()"
				tbInputPath.setText(strInputPath);

				try {
					tpScrollPane.setText("");
					this.appendToPane(tpScrollPane, "- You're about to copy "+Metadata.countImageFiles(strInputPath)+" images. Press Start to begin.\n", Color.WHITE, Color.DARK_GRAY);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}else {
				tbInputPath.setText(strInputPath);
			}
		}else if (source == btnOutputFolder) {
			result = fcOutputChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) { // Change this instance-reference to a static reference.
				strOutputPath = fcOutputChooser.getSelectedFile().getAbsolutePath();
				tbOutputPath.setText(strOutputPath);
			}else {
				tbOutputPath.setText(strOutputPath);
			}
		}else if (source == btnStart) {
			
			//pbProgressBar.setValue(0);
			
			tpScrollPane.setText(""); // 
			if (checkValidFolderPath(tbInputPath.getText()) && tbInputPath.getText() != null && !tbInputPath.getText().isEmpty()) {
				inputValid = true;
			}else {
				
				appendToPane(tpScrollPane,"@ Invalid input path: ".concat(strInputPath).concat("\n"), Color.RED, Color.BLACK);
			}
			if (checkValidFolderPath(tbOutputPath.getText()) && tbOutputPath.getText() != null && !tbOutputPath.getText().isEmpty()) {
				outputValid = true;
			}else {
				
				appendToPane(tpScrollPane,"@ Invalid output path: ".concat(strOutputPath).concat("\n"), Color.RED, Color.BLACK);
			}
			if (checkIfNumericValid(tbThreadCount.getText())) {
				threadCount = Integer.parseInt(tbThreadCount.getText());
			}else {
				appendToPane(tpScrollPane,"@ Invalid thread count: ".concat(tbThreadCount.getText()).concat("\n"), Color.RED, Color.BLACK);
				threadCount = 0;
			}
				
			if (inputValid && outputValid && threadCount > 0) {

				setOutputPathDirectory(tbOutputPath.getText());
				setInputPathDirectory(tbInputPath.getText());
				
				try {
					startTime = System.currentTimeMillis();
					// Searching method
					this.metadata = new Metadata(this);
					//pbProgressBar.setMaximum(metadata.countRegularFiles(strInputPath));
					metadata.setRootOutputDirectory(strOutputPath.concat("\\"));
					metadata.discoverImages(strInputPath, threadCount);
					
				} catch (IOException io) {
					io.printStackTrace();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				} catch (NoSuchAlgorithmException nsa) {
					nsa.printStackTrace();
				}
			}
		}
	}
	// Check if file exists at selected path
	/**
	 * 
	 * @param path
	 * @return
	 */
	private boolean checkValidFolderPath(String path) {
		return Files.exists(Paths.get(path));
	}
	
	// Check if the thread number is valid
	/**
	 * 
	 * @param numeric
	 * @return
	 */
	private boolean checkIfNumericValid(String numeric) {
		if (numeric == null) {
			return false;
		}
		try {
			Integer num = Integer.parseInt(numeric);
			if (num <= 0 || num > maxProcessors) {
				return false;
			}
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	// Add colored text to the Output Log
	/**
	 * 
	 * @param tpScrollPane
	 * @param message
	 * @param c
	 * @param bg
	 */
	protected void appendToPane(JTextPane tpScrollPane, String message, Color c, Color bg) {
		tpScrollPane.setEditable(true);
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet asFg = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		AttributeSet asBg = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, bg);
		
		int length = tpScrollPane.getDocument().getLength();
		tpScrollPane.setCaretPosition(length);
		tpScrollPane.setCharacterAttributes(asFg, false);
		tpScrollPane.setCharacterAttributes(asBg, false);
		tpScrollPane.replaceSelection(message);
		tpScrollPane.setEditable(false);
	}
	/**
	 * 
	 * @param pbProgressBar
	 */
	protected void notifyProgressBar(JProgressBar pbProgressBar) {
		pbProgressBar.setValue(getProgressValue());
	}
	/// Getters and Setters
	/**
	 * 
	 * @return
	 */
	public String getOutputPathDirectory() {
		return outputPathDirectory;
	}
	/**
	 * 
	 * @param outputPathDirectory
	 */
	public void setOutputPathDirectory(String outputPathDirectory) {
		this.outputPathDirectory = outputPathDirectory;
	}
	/**
	 * 
	 * @return
	 */
	public String getInputPathDirectory() {
		return inputPathDirectory;
	}
	/**
	 * 
	 * @param inputPathDirectory
	 */
	public void setInputPathDirectory(String inputPathDirectory) {
		this.inputPathDirectory = inputPathDirectory;
	}
	/**
	 * 
	 * @param imagePath
	 */
	public void notifyCopied(String imagePath) {
		String msg = "> Copied file: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.GREEN, Color.BLACK);
	}
	/**
	 * 
	 * @param imagePath
	 */
	public void notifyDuplicate(String imagePath) {
		String msg = "⚠ Skipped duplicate file: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.ORANGE, Color.BLACK);
	}
	/**
	 * 
	 * @param imagePath
	 */
	public void notifyError(String imagePath) {
		String msg = "# Error - Not an Image: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.RED, Color.BLACK);
	}
	/**
	 * 
	 * @param imagePath
	 */
	public void notifyNoMetadata(String imagePath) {
		String msg = "% File has no EXIF metadata, skipping: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.ORANGE, Color.BLACK);
	}
	/***
	 * 
	 * @param copiedFiles
	 * @throws IOException
	 */
	public void notifyCopiedFiles(Integer copiedFiles) throws IOException {
		finishTime = System.currentTimeMillis();
		String msg = "\n- Number of copied files: "+copiedFiles+(" in time: ")+(finishTime-startTime)+(" ms \n\n");
		measurementMap.put(threadCount, finishTime-startTime);
		this.appendToPane(tpScrollPane, msg, Color.WHITE, Color.DARK_GRAY);
		
		this.appendToPane(tpScrollPane, "Time measurements: \n", Color.WHITE, Color.BLACK);
		for (Integer key : measurementMap.keySet()) {
			this.appendToPane(tpScrollPane, key+"-Thread's : "+measurementMap.get(key)+" miliseconds \n", Color.WHITE, Color.BLACK);
		}
	}
	/*
	public void setProgressValue(Integer progressValue){
		this.progressValue = progressValue;
		this.notifyProgressBar(pbProgressBar);
	}*/
	/**
	 * 
	 * @return
	 */
	public Integer getProgressValue() {
		return progressValue;
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		//metadata.shutdown();
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 
	 * @return
	 */
	public Integer getConfirm() {
		return confirm;
	}
	/**
	 * 
	 * @param confirm
	 */
	public void setConfirm(Integer confirm) {
		this.confirm = confirm;
	}
}


