package com.tt.mongoexplorer.utils;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class UIUtils {

	public static void info(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Heads Up!", JOptionPane.INFORMATION_MESSAGE, UIUtils.icon("resources/large/info.png"));
	}
	
	public static void error(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Oops!", JOptionPane.ERROR_MESSAGE, UIUtils.icon("resources/large/error.png"));
	}
	
	public static Icon icon(String path) {
		File file = new File(path);
		if (file.exists()) {
			return (new ImageIcon(path));
		} else {
			return (new ImageIcon(UIUtils.class.getResource("/" + path)));
		}
	}
	
}
