package com.tt.mongoexplorer.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import com.tt.mongoexplorer.callback.NavigationCallback;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.utils.Constants;
import com.tt.mongoexplorer.utils.UIUtils;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener, NavigationCallback {

	private NavigationPanel navigationPanel = new NavigationPanel(this);
	
	private JTabbedPane contentPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	
	public MainFrame() {
		setTitle(Constants.NAME + " - " + Constants.VERSION);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT);
		setMinimumSize(new Dimension(Constants.MIMIMUM_WINDOW_WIDTH, Constants.MINIMUM_WINDOW_HEIGHT));
		
		setJMenuBar(createMenuBar());
		
		contentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		jsp.setLeftComponent(navigationPanel);
		jsp.setRightComponent(contentPanel);
		jsp.setResizeWeight(0.2d);
		
		setLayout(new GridLayout(1, 1));
		add(jsp);
		
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dimension.width / 2 - Constants.DEFAULT_WINDOW_WIDTH / 2, dimension.height / 2 - Constants.DEFAULT_WINDOW_HEIGHT / 2);
		
		navigationPanel.addNavigationCallback(this);
		
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
		if ("about".equals(e.getActionCommand())) {
			UIUtils.about(this);
		}
	}

	@Override
	public void onOpenQueryWindowRequested(Collection collection) {
		QueryPanel queryPanel = new QueryPanel(this, collection);
		queryPanel.setOpaque(false);
		contentPanel.addTab(collection.toString(), UIUtils.icon("resources/small/collection.png"), queryPanel, createToolTip(collection));
		contentPanel.setSelectedComponent(queryPanel);
		queryPanel.openQueryWindow();
	}

	@Override
	public void onFindAllDocumentsRequested(Collection collection) {
		QueryPanel queryPanel = new QueryPanel(this, collection);
		queryPanel.setOpaque(false);
		contentPanel.addTab(collection.toString(), UIUtils.icon("resources/small/collection.png"), queryPanel, createToolTip(collection));
		contentPanel.setSelectedComponent(queryPanel);
		queryPanel.findAllDocuments();
	}
	
	// *********
	// private
	// *********
	
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
	
	private String createToolTip(Collection collection) {
		StringBuilder sb = new StringBuilder();
		sb.append(collection.getDatabase().getHost().toString());
		sb.append(" / ");
		sb.append(collection.getDatabase().toString());
		return (sb.toString());
	}
	
}
