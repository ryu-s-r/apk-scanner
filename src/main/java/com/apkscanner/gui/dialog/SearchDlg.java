package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import com.apkscanner.gui.tabpanels.ResContentFocusChanger;
import com.apkscanner.resource.RImg;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.WindowSizeMemorizer;
import com.apkspectrum.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.ZipFileUtil;

public class SearchDlg extends JDialog {
	private static final long serialVersionUID = 6109952065388761951L;

	public String sName;
	private static ApkInfo apkinfo;
	private ArrayList<TableData> data = new ArrayList<TableData>();
	static JTextField name;
	AllTableModel allTableModel;
	JTable allTable;
	ImageIcon Loading;
	JLabel label;
	static String hoverFilePath;

	private ResContentFocusChanger changer;

	public SearchDlg(ResContentFocusChanger changer) {
		setBounds(100, 100, 500, 500);
		setTitle("Input Dialog");
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);

		WindowSizeMemorizer.apply(this, new Dimension(500, 500));

		setLayout(new BorderLayout());
		// Create Input
		name = new JTextField();

		name.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sName = name.getText();
				searchString();
			}
		});

		//name.setBounds(57, 36, 175, 20);
		getContentPane().add(name,BorderLayout.NORTH);
		getContentPane().add(makeTable(),BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		// Button OK
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sName = name.getText();
				searchString();
				//dispose();
			}
		});
		//btnOK.setBounds(70, 93, 78, 23);
		buttonPanel.add(btnOK);

		// Button Cancel
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sName = "";
				dispose();
			}
		});
		//btnCancel.setBounds(158, 93, 74, 23);
		buttonPanel.add(btnCancel);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		label = new JLabel(RImg.WAIT_BAR.getImageIcon());
		label.setVisible(false);
		buttonPanel.add(label);

		this.changer = changer;
	}

	public void setJTableColumnsWidth(JTable table, int tablePreferredWidth, double... percentages) {
		double total = 0;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
		}

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth((int) (tablePreferredWidth * (percentages[i] / total)));
		}
	}

	private JPanel makeTable() {
		allTableModel = new AllTableModel(data);
		JPanel panel = new JPanel(new BorderLayout());
		allTable = new CustomTable(allTableModel);

		allTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				JTable table =(JTable) me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2) {
					// your valueChanged overridden method
					Log.d("click : " + row + "  file : " +(data.get(row)).path + "   line : " + (data.get(row)).line);
					//ImageResource.setTreeFocus((data.get(row)).path, (data.get(row)).line);
					if(changer!=null) {
						changer.setResContentFocus((data.get(row)).path, (data.get(row)).line, name.getText());
					} else {
						Log.d("TreeFocusChanger = null");
					}

				}
			}
		});

		JScrollPane scroll = new JScrollPane(allTable);
		setJTableColumnsWidth(allTable, allTable.getWidth(), 10, 30, 10, 50);

		panel.add(scroll);

		allTable.setRowHeight(20);

		return panel;
	}

	private static class CustomTable extends JTable {
		private static final long serialVersionUID = 7146431942417244851L;

		private CustomTooltip m_tooltip;

		public CustomTable(AllTableModel allTableModel) {
			super(allTableModel);
		}

		@Override
		public JToolTip createToolTip() {
			if (m_tooltip == null) {
				m_tooltip = new CustomTooltip();
				m_tooltip.setComponent(this);

				Log.d("null");
			} else {
				Log.d("not null");
			}
			return m_tooltip;
		}

		public void setLabelText(String str) {
			m_tooltip.setTextPath(str);
		}

		public void setMarkString(String str) {
			m_tooltip.setMarkString(name.getText());
		}

		public String getToolTipText(MouseEvent e) {
			String toolTipText = null;
			Point p = e.getPoint(); // MouseEvent
			int col = columnAtPoint(p);
			int row = rowAtPoint(p);
			Component comp = prepareRenderer(getCellRenderer(row, col), row, col);

			Rectangle bounds = getCellRect(row, col, false);

			try {
				//comment row, exclude heading
				if (comp.getPreferredSize().width > bounds.width) {
					hoverFilePath = getValueAt(row, 1).toString();

					//Log.d(hoverFilePath);

					String[] xmlbuffer = AaptNativeWrapper.Dump.getXmltree(apkinfo.filePath,
							new String[] { hoverFilePath });
					// StringBuilder sb = new StringBuilder();
					// for(String s: xmlbuffer) sb.append(s+"\n");

					// this.setLabelText(getValueAt(row, 1).toString());

					toolTipText = apkinfo.a2xConvert.convertToText(xmlbuffer);
					if(m_tooltip !=null) {
						setMarkString(name.getText());
						setLabelText(hoverFilePath);
					} else {

					}
					// toolTipText = getValueAt(row, 1).toString();

				}
			} catch (RuntimeException e1) {
				//catch null pointer exception if mouse is over an empty line
			}
			return toolTipText;
		}
	}

	private static class CustomTooltip extends JToolTip {
		private static final long serialVersionUID = 1732882292311005730L;
		private JPanel m_panel;
		private RSyntaxTextArea textView;
		private JLabel label;
		public CustomTooltip() {
			super();

			m_panel = new JPanel(new BorderLayout());
			textView = new RSyntaxTextArea(20,60);
			RTextScrollPane sp = new RTextScrollPane(textView);

			label = new JLabel();
			textView.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
			textView.setCodeFoldingEnabled(true);
			textView.setEditable(false);

			m_panel.add(BorderLayout.CENTER, sp);
			m_panel.add(BorderLayout.NORTH, label);

			setLayout(new BorderLayout());
			add(m_panel);
		}

		public void setMarkString(String str) {
			SearchContext context = new SearchContext();
			context.setMatchCase(false);
			context.setMarkAll(true);
			context.setSearchFor(str);
			context.setWholeWord(false);
			//org.fife.ui.rtextarea.SearchResult result =
			SearchEngine.find(textView, context);

		}

		public void setTextPath(String str) {
			label.setText(str);
		}

		@Override
		public Dimension getPreferredSize() {
			return m_panel.getPreferredSize();
		}

		@Override
		public void setTipText(String tipText) {
			if (tipText != null && !tipText.isEmpty()) {
				textView.setText(tipText);
				textView.setCaretPosition(0);

			} else {
				super.setTipText(tipText);
			}
		}
	}

	private void searchString() {
		label.setVisible(true);

		new Thread(new Runnable() {
			public void run()
			{
				String findStr =name.getText();

				String[] filelist = apkinfo.resources;
				String temp = new String();

				data.clear();

				for(int i=0; i<filelist.length; i++) {
					if(filelist[i].endsWith(".png")) continue;

					if((filelist[i].startsWith("res/") && filelist[i].endsWith(".xml")) || filelist[i].equals("AndroidManifest.xml")) {
						//						String[] xmlbuffer = AaptWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {filelist[i]});
						//						StringBuilder sb = new StringBuilder();
						//						for(String s: xmlbuffer) sb.append(s+"\n");

						String[] xmlbuffer = AaptNativeWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {filelist[i]});
						//StringBuilder sb = new StringBuilder();
						//for(String s: xmlbuffer) sb.append(s+"\n");
						temp = apkinfo.a2xConvert.convertToText(xmlbuffer);


						//temp = sb.toString();
					} else if(filelist[i].endsWith(".txt") || filelist[i].endsWith(".mk")
							|| filelist[i].endsWith(".html") || filelist[i].endsWith(".js") || filelist[i].endsWith(".css") || filelist[i].endsWith(".json")
							|| filelist[i].endsWith(".props") || filelist[i].endsWith(".properties")) {
						byte[] buffer = ZipFileUtil.readData(apkinfo.filePath, filelist[i]);
						if(buffer != null) {
							temp = new String(buffer);
						}
					} else if("resources.arsc".equals(filelist[i])) {
						String[] lines = apkinfo.resourcesWithValue;
						if(lines == null) {
							Log.w("no ready resources.arsc");
							continue;
						}

						for(int n = 0; n < lines.length; n++) {
							if (lines[n].contains(findStr)) {
								data.add(new TableData(data.size(),filelist[i],n,lines[n]));
							}
						}
						allTableModel.fireTableDataChanged();
						continue;
					} else {
						continue;
					}


					int lineNumber = 1;       // 행 번호
					try {

						////////////////////////////////////////////////////////////////
						//Scanner scanner = new Scanner(temp);
						//System.out.println(filelist[i]);
						if(temp ==null) continue;
						BufferedReader reader = new BufferedReader(new StringReader(temp));

						String line;
						try {
							while ((line = reader.readLine()) != null) {
								//String line = scanner.nextLine();
								// process the line
								if (line.contains(findStr))  {
									//System.out.format("%3d: %s%n", lineNumber, line);
									data.add(new TableData(data.size(),filelist[i],lineNumber,line));
								}

								lineNumber++;
							}
							allTableModel.fireTableDataChanged();
						} catch (IOException e) {
							e.printStackTrace();
						}

						////////////////////////////////////////////////////////////////
					} catch (PatternSyntaxException e) { // 정규식에 에러가 있다면
						System.err.println(e);
						System.exit(1);
					}
				}
				label.setVisible(false);
				setJTableColumnsWidth(allTable, allTable.getWidth(), 10, 30, 10, 50);
			}
		}).start();
	}

	public void setApkInfo(ApkInfo apkinfo) {
		SearchDlg.apkinfo = apkinfo;
		Log.d(""+SearchDlg.apkinfo);
	}

	class TableData {
		private int Index;
		private String path;
		private int line;
		private String findstring;

		public TableData(int Index, String path, int line, String findstring) {
			super();
			this.Index = Index;
			this.path = path;
			this.line = line;
			this.findstring = findstring;
		}

		public int getCount() {
			return Index;
		}

		public String getPath() {
			return path;
		}
		public int getLine() {
			return line;
		}
		public String getfindString() {
			return findstring;
		}
	}

	class AllTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -4035177205338096822L;

		ArrayList<TableData> tableData;

		Object[] columnNames = {"Index", "path", "line", ""};

		public AllTableModel(ArrayList<TableData> data) {

			tableData = data;
		}

		public ArrayList<TableData> getTableData() {
			return tableData;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column].toString();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return tableData.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TableData data = tableData.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return data.getCount();
			case 1:
				return data.getPath();
			case 2:
				return data.getLine();
			case 3:
				return data.getfindString();
			default:
				return null;
			}
		}
	}
}

