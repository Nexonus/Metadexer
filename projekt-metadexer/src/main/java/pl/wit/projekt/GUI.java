package pl.wit.projekt;

/***
 * @author Jan Konarski - Graphic Interface setup
 * Implementacja interfejsu graficznego programu do indeksowania i sortowania plików JPG na bazie metadanych EXIF
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.io.FileUtils;

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
	private long startTime;
	private long finishTime;
	
	private Map<Integer, Long> measurementMap = new HashMap<Integer, Long>();
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	// Initialize GUI window in a main function
	public static void main(String[] args) throws IOException {
		new GUI();
	}
	/***
	 * 
	 * @throws ImagingException
	 * @throws IOException
	 */
	public GUI() throws IOException {
		super("METADEXER");

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
	/***
	 * Action Performed : Check for which button is pressed and read DialogResult to perform an action.
	 */
	@Override
	public void actionPerformed(ActionEvent ae) { // Refactor this method to reduce its Cognitive Complexity from 29 to the 15 allowed. [+18 locations]
		Object source = ae.getSource();
		String strInputPath = tbInputPath.getText();
		String strOutputPath = tbOutputPath.getText();
		
		boolean inputValid = false;
		boolean outputValid = false;
		
		int result;
		if (source == btnCleanup) {
			tpScrollPane.setText("");
			if(!strOutputPath.contains("\\Metadexer\\Metadexer\\"))
				this.appendToPane(tpScrollPane, "⚠ Access denied.", Color.ORANGE, Color.DARK_GRAY);
			else {
				this.appendToPane(tpScrollPane, "⚠ You're about to remove ALL files from: ".concat(strOutputPath).concat("\nPlease write 'delete' in the Input Box to confirm, then press this button again.\n\n"), Color.ORANGE, Color.DARK_GRAY);
				
				if (tbInputPath.getText().equals("delete")) {
					try {
						if (Files.exists(Paths.get(strOutputPath)))
						{
							tpScrollPane.setText("");
							measurementMap.clear();
							File directory = new File(strOutputPath);
							this.appendToPane(tpScrollPane, "⚠ Deleted: "+Metadata.countImageFiles(strOutputPath)+" files.", Color.ORANGE, Color.DARK_GRAY);
							FileUtils.cleanDirectory(directory);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
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
	/**
	 * Check if Path leads to a valid existing file (Directory)
	 * @param path
	 * @return
	 */
	private boolean checkValidFolderPath(String path) {
		return Files.exists(Paths.get(path));
	}
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
	protected void notifyProgressBar(JProgressBar pbProgressBar) {
		pbProgressBar.setValue(getProgressValue());
	}
	/// Getters and Setters
	public String getOutputPathDirectory() {
		return outputPathDirectory;
	}
	public void setOutputPathDirectory(String outputPathDirectory) {
		this.outputPathDirectory = outputPathDirectory;
	}
	public String getInputPathDirectory() {
		return inputPathDirectory;
	}
	public void setInputPathDirectory(String inputPathDirectory) {
		this.inputPathDirectory = inputPathDirectory;
	}
	public void notifyCopied(String imagePath) {
		String msg = "> Copied file: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.GREEN, Color.BLACK);
	}
	public void notifyDuplicate(String imagePath) {
		String msg = "⚠ Skipped duplicate file: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.ORANGE, Color.BLACK);
	}
	public void notifyError(String imagePath) {
		String msg = "# Error invalid file: ".concat(imagePath).concat("\n");
		this.appendToPane(tpScrollPane, msg, Color.RED, Color.BLACK);
	}
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
}


