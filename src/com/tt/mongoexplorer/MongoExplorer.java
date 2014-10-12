package com.tt.mongoexplorer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.tt.mongoexplorer.view.MainFrame;

public class MongoExplorer {

	public static void main(String[] args) {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Runnable ui = new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				new MainFrame();
			}
		};
		SwingUtilities.invokeLater(ui);
	}
	
}
