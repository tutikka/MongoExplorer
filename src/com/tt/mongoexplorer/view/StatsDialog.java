package com.tt.mongoexplorer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.domain.Database;
import com.tt.mongoexplorer.utils.MongoUtils;
import com.tt.mongoexplorer.utils.UIUtils;

@SuppressWarnings("serial")
public class StatsDialog extends JDialog implements ActionListener {

	private static final int DIALOG_WIDTH = 640;
	
	private static final int DIALOG_HEIGHT = 480;
	
	private JTree tree;
	
	private DefaultTreeModel treeModel;
	
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(new CustomNode("", ""));
	
	private CustomTreeCellRenderer customTreeCellRenderer = new CustomTreeCellRenderer();
	
	private JButton close;
	
	private Object source;
	
	public StatsDialog(JFrame parent, Object source) {
		super(parent);
		this.source = source;
		setTitle("View Statistics");
		
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		int x = parent.getX() + parent.getWidth() / 2 - DIALOG_WIDTH / 2;
		int y = parent.getY() + parent.getHeight() / 2 - DIALOG_HEIGHT / 2;
		setLocation(x, y);
		
		setModal(true);
	
		setLayout(new BorderLayout());
		
		add(createInfoPanel(), BorderLayout.NORTH);
		add(createStatsPanel(), BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
		
		handleStats();
		
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("close".equals(e.getActionCommand())) {
			dispose();
		}
	}
	
	// *********
	// private
	// *********
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		String hostLabel = "-";
		String databaseLabel = "-";
		String collectionLabel = "-";
		if (source instanceof Database) {
			Database database = (Database) source;
			hostLabel = database.getHost().toString();
			databaseLabel = database.toString();
		}
		if (source instanceof Collection) {
			Collection collection = (Collection) source;
			hostLabel = collection.getDatabase().getHost().toString();
			databaseLabel = collection.getDatabase().toString();
			collectionLabel = collection.toString();
		}
		panel.setLayout(new GridLayout(1, 3));
		panel.add(new JLabel(hostLabel, UIUtils.icon("resources/small/host.png"), SwingConstants.LEFT));
		panel.add(new JLabel(databaseLabel, UIUtils.icon("resources/small/database.png"), SwingConstants.CENTER));
		panel.add(new JLabel(collectionLabel, UIUtils.icon("resources/small/collection.png"), SwingConstants.RIGHT));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		return (panel);
	}
	
	private JPanel createStatsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.setCellRenderer(customTreeCellRenderer);
		tree.setBackground(Color.DARK_GRAY);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JScrollPane jsp = new JScrollPane(tree);
		jsp.setBackground(Color.DARK_GRAY);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(jsp);
		return (panel);
	}
	
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		close = new JButton("Close");
		close.setActionCommand("close");
		close.addActionListener(this);
		panel.add(close);
		return (panel);
	}
	
	private void walk(DBObject dbo, DefaultMutableTreeNode parent) {
		for (String key : dbo.keySet()) {
			Object object = dbo.get(key);
			if (object instanceof DBObject) {
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, object));
				treeModel.insertNodeInto(dmtn, parent, parent.getChildCount());
				walk((DBObject) object, dmtn);
			} else {
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, object));
				parent.add(dmtn);
			}
		}
		
	}
	
	private void handleStats() {
		CommandResult commandResult = null;
		MongoClient client = null;
		try {
			if (source instanceof Database) {
				Database database = (Database) source;
				client = MongoUtils.getMongoClient(database.getHost());
				commandResult = client.getDB(database.getName()).getStats();
			}
			if (source instanceof Collection) {
				Collection collection = (Collection) source;
				client = MongoUtils.getMongoClient(collection.getDatabase().getHost());
				commandResult = client.getDB(collection.getDatabase().getName()).getCollection(collection.getName()).getStats();
			}
		} catch (Exception e) {
			new ErrorDialog(this, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		for (String key : commandResult.keySet()) {
			Object object = commandResult.get(key);
			DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, object));
			root.add(dmtn);
			if (object instanceof DBObject) {
				walk((DBObject) object, dmtn);
			}
		}
		treeModel.nodeStructureChanged(root);
	}
	
	private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
			Object userObject = dmtn.getUserObject();
			CustomNode customNode = (CustomNode) userObject;

			setIcon(null);
			setBackground(Color.DARK_GRAY);
			setBackgroundNonSelectionColor(Color.DARK_GRAY);
			setBackgroundSelectionColor(Color.LIGHT_GRAY);
			setBorderSelectionColor(Color.LIGHT_GRAY);
			
			if (customNode.object instanceof DBObject) {
				setText("<html><font color='gray'>{ </font><font color='orange'>" + customNode.key + " </font><font color='gray'> }</font></html>");
			} else {
				setText("<html><font color='white'>" + customNode.key + "</font> <font color='yellow'>" + customNode.object.toString() + "</font> <font color='gray'>" + customNode.object.getClass().getSimpleName() + "</font></html>");
			}
			
			return (this);
		}
		
	}
	
	private class CustomNode {
		
		public CustomNode(String key, Object object) {
			this.key = key;
			this.object = object;
		}
		
		private String key;
		
		private Object object;
		
	}
	
}
