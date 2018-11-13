package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.SystemUtil;

public class EasyGuiEmptyPanel extends FlatPanel {
	static private Color sdkverPanelcolor = new Color(242,242,242);
	
	public EasyGuiEmptyPanel() {
		setLayout(new BorderLayout());
		
		setshadowlen(3);
		setBorder(BorderFactory.createEmptyBorder(10, 10,10, 10));
		JLabel appicon = new JLabel();
		
		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<html><div id=\"about\">");
		strTabInfo.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		//strTabInfo.append("  <H3>Using following tools</H3></div>");
		strTabInfo.append("  <br/><br/>");
		strTabInfo.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		strTabInfo.append("  It is open source project on <a href=\"https://github.sec.samsung.net/sunggyu-kam/apk-scanner\" title=\"APK Scanner Site\">SEC Github</a></html>");		
		appicon.setText(strTabInfo.toString());
		add(appicon, BorderLayout.CENTER);
	}    
}
