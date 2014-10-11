package com.tt.mongoexplorer.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.tt.mongoexplorer.utils.Constants;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener {

	private NavigationPanel navigationPanel = new NavigationPanel(this);
	
	private QueryPanel queryPanel = new QueryPanel(this);
	
	public MainFrame() {
		setTitle(Constants.NAME + " - " + Constants.VERSION);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT);
		setMinimumSize(new Dimension(Constants.MIMIMUM_WINDOW_WIDTH, Constants.MINIMUM_WINDOW_HEIGHT));
		
		setJMenuBar(createMenuBar());
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		jsp.setLeftComponent(navigationPanel);
		jsp.setRightComponent(queryPanel);
		jsp.setResizeWeight(0.2d);
		
		setLayout(new GridLayout(1, 1));
		add(jsp);
		
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dimension.width / 2 - Constants.DEFAULT_WINDOW_WIDTH / 2, dimension.height / 2 - Constants.DEFAULT_WINDOW_HEIGHT / 2);
		
		// query panel listens for navigation callbacks
		navigationPanel.addNavigationCallback(queryPanel);
		
		setVisible(true);
		
		// open connect dialog by default
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ConnectDialog connectDialog = new ConnectDialog(MainFrame.this);
				connectDialog.addConnectCallback(navigationPanel);
				connectDialog.setVisible(true);
			}
		});
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu file = new JMenu("File");
		JMenuItem connect = new JMenuItem("Connect...");
		connect.setActionCommand("connect");
		connect.addActionListener(this);
		file.add(connect);
		file.addSeparator();
		JMenuItem exit = new JMenuItem("Exit");
		exit.setActionCommand("exit");
		exit.addActionListener(this);
		file.add(exit);
		menuBar.add(file);
		
		JMenu help = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		about.setActionCommand("about");
		about.addActionListener(this);
		help.add(about);
		menuBar.add(help);
		
		return (menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("connect".equals(e.getActionCommand())) {
			ConnectDialog connectDialog = new ConnectDialog(this);
			connectDialog.addConnectCallback(navigationPanel);
			connectDialog.setVisible(true);
		}
		if ("exit".equals(e.getActionCommand())) {
			int result = JOptionPane.showConfirmDialog(this, "Are you sure?", "Exit", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				dispose();
			}
		}
	}
	
}
