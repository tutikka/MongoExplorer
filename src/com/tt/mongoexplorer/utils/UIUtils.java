package com.tt.mongoexplorer.utils;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class UIUtils {

	public static void info(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Heads Up!", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("resources/large/info.png"));
	}
	
	public static void error(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Oops!", JOptionPane.ERROR_MESSAGE, new ImageIcon("resources/large/error.png"));
	}
	
}
