package com.tt.mongoexplorer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.tt.mongoexplorer.callback.NavigationCallback;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.domain.Database;
import com.tt.mongoexplorer.domain.Host;
import com.tt.mongoexplorer.utils.MongoUtils;

@SuppressWarnings("serial")
public class QueryPanel extends JPanel implements ActionListener, NavigationCallback, TreeSelectionListener {

	private MainFrame parent;
	
	private JTextField query;
	
	private JButton find;
	
	private JTree tree;
	
	private DefaultTreeModel treeModel;
	
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(new CustomNode("", ""));
	
	private CustomTreeCellRenderer customTreeCellRenderer = new CustomTreeCellRenderer();
	
	private JTextArea area;
	
	private JLabel host;
	
	private JLabel database;
	
	private JLabel collection;
	
	private JLabel time;
	
	private Host selectedHost;
	
	private Database selectedDatabase;
	
	private Collection selectedCollection;
	
	public QueryPanel(MainFrame parent) {
		this.parent = parent;
	
		setLayout(new BorderLayout());
		
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		add(createQueryPanel(), BorderLayout.NORTH);
		add(createResultsPanel(), BorderLayout.CENTER);
		add(createInfoPanel(), BorderLayout.SOUTH);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("find".equals(e.getActionCommand())) {
			find();
		}
	}

	@Override
	public void onHostSelected(Host host) {
		this.selectedHost = host;
		updateInfo();
	}

	@Override
	public void onDatabaseSelected(Database database) {
		if (database == null) {
			this.selectedDatabase = null;
		} else {
			this.selectedHost = database.getHost();
			this.selectedDatabase = database;
		}
		updateInfo();
	}

	@Override
	public void onCollectionSelected(Collection collection) {
		if (collection == null) {
			this.selectedCollection = null;
		} else {
			this.selectedHost = collection.getDatabase().getHost();
			this.selectedDatabase = collection.getDatabase();
			this.selectedCollection = collection;
		}
		updateInfo();
	}
	
	@Override
	public void onQueryRequested(String query) {
		this.query.setText(query);
		find();
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
			area.setText(customNode.object.toString());
		}
	}
	
	// *********
	// private
	// *********
	
	private JPanel createQueryPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		panel.add(new JLabel("Query:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		query = new JTextField("");
		panel.add(query, c);
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.0;
		find = new JButton("Find");
		find.setActionCommand("find");
		find.addActionListener(this);
		panel.add(find, c);
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
		
		JScrollPane jspTree = new JScrollPane(tree);
		jspTree.setBackground(Color.DARK_GRAY);
		JScrollPane jspArea = new JScrollPane(area);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspTree, jspArea);
		jsp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		jsp.setResizeWeight(0.9d);

		return (jsp);
	}
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 4));
		host = new JLabel("[Not selected]", new ImageIcon("resources/small/host.png"), SwingConstants.LEFT);
		panel.add(host);
		database = new JLabel("[Not selected]", new ImageIcon("resources/small/database.png"), SwingConstants.CENTER);
		panel.add(database);
		collection = new JLabel("[Not selected]", new ImageIcon("resources/small/collection.png"), SwingConstants.CENTER);
		panel.add(collection);
		time = new JLabel("[Not run]", new ImageIcon("resources/small/time.png"), SwingConstants.RIGHT);
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
	
	private void find() {
		
		if (selectedHost == null) {
			JOptionPane.showMessageDialog(parent, "Please select a host first");
			return;
		}
		
		if (selectedDatabase == null) {
			JOptionPane.showMessageDialog(parent, "Please select a database first");
			return;
		}
		
		if (selectedCollection == null) {
			JOptionPane.showMessageDialog(parent, "Please select a collection first");
			return;
		}
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				
				// clear tree
				root.removeAllChildren();
				treeModel.nodeStructureChanged(root);
				
				// clear json
				area.setText("");
				
				find.setEnabled(false);
				MongoClient client = null;
				try {
					client = MongoUtils.getMongoClient(selectedHost);
					long start = System.currentTimeMillis();
					DBCursor cursor = null;
					if (query.getText() != null && query.getText().length() > 0) {
						DBObject ref = (DBObject) JSON.parse(query.getText());
						cursor = client.getDB(selectedDatabase.getName()).getCollection(selectedCollection.getName()).find(ref);
					} else {
						cursor = client.getDB(selectedDatabase.getName()).getCollection(selectedCollection.getName()).find();
					}
					long end = System.currentTimeMillis();
					while (cursor.hasNext()) {
						DBObject dbo = cursor.next();
						String key = dbo.get("_id") == null ? "" : dbo.get("_id").toString();
						DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, dbo));
						root.add(dmtn);
						walk(dbo, dmtn);
						treeModel.nodeStructureChanged(root);
					}
					time.setText((end - start) + " ms | " + cursor.count() + " result(s)");
				} catch (Exception e) {
					e.printStackTrace(System.err);
				} finally {
					if (client != null) {
						client.close();
					}
				}
				find.setEnabled(true);
			}
		};
		SwingUtilities.invokeLater(runnable);
	}
	
	private void updateInfo() {
		if (selectedHost != null) {
			host.setText(selectedHost.toString());
		} else {
			host.setText("[Not selected]");
		}
		if (selectedDatabase != null) {
			database.setText(selectedDatabase.toString());
		} else {
			database.setText("[Not selected]");
		}
		if (selectedCollection != null) {
			collection.setText(selectedCollection.toString());
		} else {
			collection.setText("[Not selected]");
		}
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
				setIcon(null);
				setText("<html><font color='white'>" + customNode.key + "</font> <font color='yellow'>" + customNode.object.toString() + "</font> <font color='gray'>" + customNode.object.getClass().getName() + "</font></html>");
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
