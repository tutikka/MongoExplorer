package com.tt.mongoexplorer.view;

import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.tt.mongoexplorer.domain.Database;
import com.tt.mongoexplorer.utils.MongoUtils;
import com.tt.mongoexplorer.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class CommandDialog extends JDialog implements ActionListener, TreeSelectionListener {

	private static final int DIALOG_WIDTH = 640;
	
	private static final int DIALOG_HEIGHT = 480;
	
	private JTextField command;

	private JButton execute;

	private JTree tree;

	private DefaultTreeModel treeModel;

	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(new CustomNode("", ""));

	private CustomTreeCellRenderer customTreeCellRenderer = new CustomTreeCellRenderer();

	private JTextArea area;

	private JLabel host;

	private JLabel database;

	private JLabel time;

	private Database selectedDatabase;

	public CommandDialog(JFrame parent, Database database) {
		super(parent);
		this.selectedDatabase = database;
		setTitle("Execute Command");
		
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		int x = parent.getX() + parent.getWidth() / 2 - DIALOG_WIDTH / 2;
		int y = parent.getY() + parent.getHeight() / 2 - DIALOG_HEIGHT / 2;
		setLocation(x, y);
		
		setModal(true);
	
		setLayout(new BorderLayout());
		
		add(createCommandPanel(), BorderLayout.NORTH);
		add(createResultsPanel(), BorderLayout.CENTER);
		add(createInfoPanel(), BorderLayout.SOUTH);
		
		updateInfo();
		
		command.setCaretPosition(2);
		command.requestFocus();
		
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("execute".equals(e.getActionCommand())) {
			if (selectedDatabase == null) {
				UIUtils.error(this, "Please select the database");
				return;
			}
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					execute.setEnabled(false);
					handleQuery();
					execute.setEnabled(true);
				}
			};
			SwingUtilities.invokeLater(runnable);
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (dmtn == null) {
			return;
		}
		Object object = dmtn.getUserObject();
		if (object == null) {
			return;
		}
		if (object instanceof CustomNode) {
			CustomNode customNode = (CustomNode) object;
			if (customNode.object instanceof DBObject) {
				area.setText(UIUtils.prettyPrint((DBObject) customNode.object));
			} else {
				area.setText(customNode.object.toString());
			}
			area.setCaretPosition(0);
		}
	}
	
	// *********
	// private
	// *********
	
	private JPanel createCommandPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		panel.add(new JLabel("Command:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		command = new JTextField("{  }");
		panel.add(command, c);
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.0;
		execute = new JButton("Execute");
		execute.setActionCommand("execute");
		execute.addActionListener(this);
		panel.add(execute, c);
		panel.setBorder(new EmptyBorder(10, 10, 0, 10));
		return (panel);
	}
	
	private JSplitPane createResultsPanel() {
		
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.setCellRenderer(customTreeCellRenderer);
		tree.addTreeSelectionListener(this);
		tree.setBackground(Color.DARK_GRAY);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		area = new JTextArea();
		area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JScrollPane jspTree = new JScrollPane(tree);
		jspTree.setBackground(Color.DARK_GRAY);
		JScrollPane jspArea = new JScrollPane(area);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspTree, jspArea);
		jsp.setOpaque(false);
		jsp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		jsp.setResizeWeight(0.9d);

		return (jsp);
	}
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridLayout(1, 3));
		host = new JLabel("-", UIUtils.icon("resources/small/host.png"), SwingConstants.LEFT);
		panel.add(host);
		database = new JLabel("-", UIUtils.icon("resources/small/database.png"), SwingConstants.CENTER);
		panel.add(database);
		time = new JLabel("-", UIUtils.icon("resources/small/time.png"), SwingConstants.RIGHT);
		panel.add(time);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
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
	
	private void handleQuery() {
		root.removeAllChildren();
		treeModel.nodeStructureChanged(root);
		area.setText("");
		MongoClient client = null;
		try {
			client = MongoUtils.getMongoClient(selectedDatabase.getHost());
			long s = System.currentTimeMillis();
			CommandResult commandResult = client.getDB(selectedDatabase.getName()).command((DBObject) JSON.parse(command.getText()));
			long e = System.currentTimeMillis();
			for (String key : commandResult.keySet()) {
				Object object = commandResult.get(key);
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, object));
				root.add(dmtn);
				if (object instanceof DBObject) {
					walk((DBObject) object, dmtn);
				}
			}
			treeModel.nodeStructureChanged(root);
			time.setText((e - s) + " ms");
		} catch (Exception e) {
			new ErrorDialog(this, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
	private void updateInfo() {
		host.setText(selectedDatabase.getHost().toString());
		database.setText(selectedDatabase.toString());
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
