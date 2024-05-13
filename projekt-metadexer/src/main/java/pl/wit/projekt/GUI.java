package pl.wit.projekt;

/***
 * @author Jan Konarski
 * GUI - Metadexer
 * Implementacja interfejsu graficznego programu do indeksowania i sortowania plików JPG na bazie metadanych EXIF
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.imaging.ImagingException;

public class GUI extends JFrame implements ActionListener {
	
	/// Create TextFields
	private JTextField tbInputPath = new JTextField("",60);
	private JTextField tbOutputPath = new JTextField("",60);
	private JTextField tbThreadCount = new JTextField("");
	
	/// Create fileChoosers
	private JFileChooser fcInputChooser = new JFileChooser(new File("..\\"));	// default start directory
	private JFileChooser fcOutputChooser = new JFileChooser(new File("..\\"));
	// Create Buttons
	private JButton btnInputFolder = new JButton("Choose Input Folder");
	private JButton btnOutputFolder = new JButton("Choose Output Folder");
	private JButton btnStart = new JButton("Start");
	
	// Create Panels
	private JPanel pnContentPane = new JPanel(new BorderLayout());
	private	JPanel pnFolderPane = new JPanel(new BorderLayout());
	private JPanel pnInputPane = new JPanel(new GridLayout(1,3));
	private JPanel pnOutputPane = new JPanel(new GridLayout(1,2));
	private JPanel pnStartPane = new JPanel(new BorderLayout());
	private JPanel pnStartPaneSplit = new JPanel(new GridLayout(1,3));
	
	private JPanel pnThreadNumberPane = new JPanel(new GridLayout(1,2));
	
	/// Output log components
	private JTextPane tpScrollPane = new JTextPane();
	private JScrollPane spScrollPane = new JScrollPane(tpScrollPane);
	
	private JLabel lbOutputLogLabel = new JLabel("Output log:");
	private JLabel lbThreadUsageLabel = new JLabel("Thread count:");
	
	private String outputPathDirectory;
	private String inputPathDirectory;
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	// Initialize GUI window
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
		
		lbOutputLogLabel.setBorder(new EmptyBorder(5,5,5,5));
		
		//pnContentPane.setLayout(new FlowLayout(FlowLayout.CENTER,15,5)); // create flowLayout (flexBox)
		setContentPane(pnContentPane);
				
		/// Set debug colors for each panel
		pnContentPane.setBackground(Color.WHITE); 
		pnFolderPane.setBackground(Color.WHITE); 
		pnStartPane.setBackground(Color.WHITE);
		pnStartPaneSplit.setBackground(Color.WHITE);
		
		/// Add ActionListeners to buttons
		btnInputFolder.addActionListener(this);
		btnOutputFolder.addActionListener(this);
		btnStart.addActionListener(this);
			
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
		
		pnThreadNumberPane.add(lbThreadUsageLabel);
		pnThreadNumberPane.add(tbThreadCount);
		
		// Add panels to the ContentPane, mostly visual stuff
		pnStartPaneSplit.add(lbOutputLogLabel,BorderLayout.WEST); // borderlayout.west property non-necessary for any GridLayout if set as so
		pnStartPaneSplit.add(pnThreadNumberPane);
		pnStartPaneSplit.add(btnStart,BorderLayout.EAST);		  // mandatory for BorderLayout
		
		
		pnStartPane.add(pnStartPaneSplit,BorderLayout.NORTH);
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
		
		int result;
		
		if (source == btnInputFolder) {

			result = fcInputChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				strInputPath = fcInputChooser.getSelectedFile().getAbsolutePath(); // "getAbsolutePath" returns a string, there's no need to call "toString()"
				tbInputPath.setText(strInputPath);
			}else {
				strInputPath = "";
				tbInputPath.setText(strInputPath);
			}
		}else if (source == btnOutputFolder) {
			result = fcOutputChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) { // Change this instance-reference to a static reference.
				strOutputPath = fcOutputChooser.getSelectedFile().getAbsolutePath();
				tbOutputPath.setText(strOutputPath);
			}else {
				strOutputPath = "";
				tbOutputPath.setText(strOutputPath);
			}
		}else if (source == btnStart) {
			
			boolean inputValid = false;
			boolean outputValid = false;
			
			tpScrollPane.setText(""); // 
			if (checkValidFolderPath(tbInputPath.getText()) && tbInputPath.getText() != null && !tbInputPath.getText().isEmpty()) {
				inputValid = true;
			}else {
				tbInputPath.setText("");
				appendToPane(tpScrollPane,"[ERROR] - Invalid input path: ".concat(strInputPath).concat("\n"), Color.RED);
			}
			if (checkValidFolderPath(tbOutputPath.getText()) && tbOutputPath.getText() != null && !tbOutputPath.getText().isEmpty()) {
				outputValid = true;
			}else {
				tbOutputPath.setText("");
				appendToPane(tpScrollPane,"[ERROR] - Invalid output path: ".concat(strOutputPath).concat("\n"), Color.RED);
			}
			if (inputValid && outputValid) {
				Integer threadCount = Integer.parseInt(tbThreadCount.getText());
				setOutputPathDirectory(tbOutputPath.getText());
				setInputPathDirectory(tbInputPath.getText());
				Metadata metadata = new Metadata();
				metadata.setStrOutputDirectoryPath(strOutputPath.concat("\\"));
				try {
					// Searching method
					metadata.discoverImages(strInputPath, threadCount);
					/*
					if (!Metadata.getIndexedImageList().isEmpty()) {
						appendToPane(tpScrollPane,"[SUCCESS] - Copied Files: ".concat("\n").concat("\n"), Color.GREEN);
					}
					
					/*else {
						appendToPane(tpScrollPane,"[WARNING] - No unique files to copy found.".concat("\n").concat("\n"), Color.ORANGE);
					}
					
					for (String imageName : Metadata.getIndexedImageList()) {
						appendToPane(tpScrollPane,imageName.concat("\n"), Color.WHITE);
					}
					*/
					
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
	public boolean checkValidFolderPath(String path) {
		return Files.exists(Paths.get(path));
		
		/*old version 
		if (Files.exists(Paths.get(path))) {
			return true;
		}
		return false;
		*/
	}
	private void appendToPane(JTextPane tpScrollPane, String message, Color c) {
		tpScrollPane.setEditable(true);
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		
		int length = tpScrollPane.getDocument().getLength();
		tpScrollPane.setCaretPosition(length);
		tpScrollPane.setCharacterAttributes(as, false);
		tpScrollPane.replaceSelection(message);
		tpScrollPane.setEditable(false);
	}
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
}


