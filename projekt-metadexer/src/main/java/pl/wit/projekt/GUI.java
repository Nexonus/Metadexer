package pl.wit.projekt;

/***
 * @author Jan Konarski
 * GUI - Metadexer
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.apache.commons.imaging.ImagingException;

public class GUI extends JFrame implements ActionListener {
	private JPanel contentPane;
	private JFileChooser chooseFile = new JFileChooser(new File("..\\samples"));
	private JTextField tbInputPath = new JTextField("",35);
	private String folderPath = new String("");
	
	/// Create interface
	public GUI() throws ImagingException, IOException {
		super("Metadexer");
				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100,100,650,300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10,10,10,10));
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		
		setContentPane(contentPane);
	
		JButton btnChooseInputFolder = new JButton("Open");
		btnChooseInputFolder.addActionListener(this);
		
		JButton btnStart = new JButton("Start");
		Metadata metadata = new Metadata();
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					metadata.DiscoverImages("..\\samples");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		JPanel filePane = new JPanel();
		JPanel startPane = new JPanel();
		
		contentPane.setBackground(Color.WHITE);
		filePane.setBackground(Color.LIGHT_GRAY);
		
		startPane.setBackground(Color.RED);
		
		JLabel lbInputLabel = new JLabel();
		lbInputLabel.setText("Path to input images folder");
		lbInputLabel.setForeground(Color.WHITE);
		
		tbInputPath.setEditable(false);
		chooseFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		filePane.add(lbInputLabel);
		filePane.add(tbInputPath);
		filePane.add(btnChooseInputFolder);
		
		startPane.add(btnStart);
		
		contentPane.add(filePane);
		contentPane.add(startPane);
		
		
		setVisible(true);	/// By default a window is hidden!
	}
	public void actionPerformed(ActionEvent e) {
		int result = chooseFile.showOpenDialog(this);
		if(result == chooseFile.APPROVE_OPTION) {
			folderPath = chooseFile.getSelectedFile().getAbsolutePath();
			tbInputPath.setText(folderPath);
		}else if (result == chooseFile.CANCEL_OPTION){
			folderPath = "";
			tbInputPath.setText(folderPath);
		}
	}
}

