package pl.wit.projekt;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame implements ActionListener {
	private JPanel contentPane;
	private JFileChooser chooseFile = new JFileChooser(new File("C:\\Documents"));
	private JTextField tbInputPath = new JTextField("",35);
	private String folderPath = new String("");
	
	public GUI() {
		super("Metadexer");
				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100,100,600,300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10,10,10,10));
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		
		setContentPane(contentPane);
	
		JButton btnChooseInputFolder = new JButton("Open");
		btnChooseInputFolder.addActionListener(this);
		
		JPanel filePane = new JPanel();
		JPanel testPane = new JPanel();
		
		contentPane.setBackground(Color.WHITE);
		filePane.setBackground(Color.LIGHT_GRAY);
		
		testPane.setBackground(Color.RED);
		
		JLabel lbInputLabel = new JLabel();
		lbInputLabel.setText("Path to input images folder");
		lbInputLabel.setForeground(Color.WHITE);
		
		tbInputPath.setEditable(false);
		chooseFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		filePane.add(lbInputLabel);
		filePane.add(tbInputPath);
		filePane.add(btnChooseInputFolder);
		
		contentPane.add(filePane);
		contentPane.add(testPane);
		
		
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

