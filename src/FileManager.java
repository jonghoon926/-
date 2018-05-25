import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FileManager implements ActionListener, ClipboardOwner {
    private JPanel pn = new JPanel(new BorderLayout());
    private JTextField tf = new JTextField("C:\\");
    private JTable jt = new JTable();
    private JList<String> list = new JList<>();
    private JScrollPane sp1 = new JScrollPane();
    private JScrollPane sp2 = new JScrollPane();
    private JLabel jl = new JLabel("File Manager");
    private JPanel jc = new JPanel(new BorderLayout());
    private JPanel ju = new JPanel(new BorderLayout());
    private File name;
    private File getBack;
    private String path = "C:\\";
    private String[][] data;
    private JMenuItem[] PopItem_English = new JMenuItem[6];
    private DefaultTableModel model;
    private int[] copy;
    private String[] English_columnNames = {"Name", "Size", "Modified"};
    private Vector<String> copyFile;
    private String[] directoryName_List;

    public FileManager() {
        JFrame f = new JFrame("File Manager");
        f.setLayout(new BorderLayout());
        PopItem_English[0] = new JMenuItem("Show Item in the Folder");
        PopItem_English[1] = new JMenuItem("Copy");
        PopItem_English[2] = new JMenuItem("Paste");
        PopItem_English[3] = new JMenuItem("Delete");
        jt.getTableHeader().setReorderingAllowed(false);
        sp1.setPreferredSize(new Dimension(200, -1));
        tf.setText("C:\\");
        PopItem_English[2].setEnabled(false);
        
        getJList();

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    String clicked;
                    getBack = new File(path, "..");
                    clicked = list.getSelectedValue();
                    if (clicked.equals("..")) {
                        try {
                            path = getBack.getCanonicalPath();
                        } catch (Exception ignored) {

                        }
                        tf.setText(path);
                        getJList();
                    } else {
                        path = name.getPath() + File.separator + clicked;
                        if (path.contains("C:\\\\"))
                            path = name.getPath() + clicked;
                        tf.setText(path);
                        getJList();
                    }
                } catch (NullPointerException aee) {
                }
            }
        });
        sp2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu PopMenu = new JPopupMenu();
                        for (int i = 0; i < 4; i++) {
                            if (i == 1) PopMenu.addSeparator();
                            if (i == 1 || i == 3) continue;
                            PopMenu.add(PopItem_English[i]);
                        }
                    
                    PopMenu.show(jt, e.getX(), e.getY());
                    PopMenu.setVisible(true);
                }
            }
        });

        jt.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu PopMenu = new JPopupMenu();
                        for (int i = 0; i < 4; i++) {
                            if (i == 1 || i == 3) PopMenu.addSeparator();
                            PopMenu.add(PopItem_English[i]);
                        }
                    
                    PopMenu.show(jt, e.getX(), e.getY());
                    PopMenu.setVisible(true);
                }
            }
        });
        for (int i = 0; i < 4; i++) {
            PopItem_English[i].addActionListener(this);
        }
        sp1.setViewportView(list);;
        sp2.setViewportView(jt);
        jc.add(jl,BorderLayout.WEST);
        ju.add(sp2, BorderLayout.CENTER);
        ju.add(sp1,BorderLayout.WEST);
        pn.add(tf, BorderLayout.NORTH);
        pn.add(jc, BorderLayout.SOUTH);
        pn.add(ju, BorderLayout.CENTER);
       
        f.setSize(1300, 700);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(pn);
        f.setVisible(true);
    }

    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
    	
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == PopItem_English[3]) {
            int[] columns = jt.getSelectedRows();
            for (int column : columns) {
                System.out.println(column);
                System.out.println(path + File.separator + jt.getValueAt(column, 0));
            }
            for (int column : columns) {
                File delete = new File(path + File.separator + jt.getValueAt(column, 0));
                delete.delete();
            }
            for (int i = 0; i < columns.length; i++)
                model.removeRow(columns[i] - i);
            model.fireTableDataChanged();
            jt.updateUI();
        }

        if (e.getSource() == PopItem_English[0]) {
            File open_Directory = new File(path);
            try {
                Desktop.getDesktop().open(open_Directory);
            } catch (IOException ignored) {

            }
        }

        if (e.getSource() == PopItem_English[1]) {
            PopItem_English[2].setEnabled(true);
            copy = jt.getSelectedRows();
            copyFile = new Vector<>(jt.getRowCount());
            for (int i = 0; i < copy.length; i++) {
                copyFile.add(i, (path + "\\" + jt.getValueAt(copy[i], 0)));
            }
        }

        if (e.getSource() == PopItem_English[2]) {
            String tmp = path;
            int count = 0;
            for (int i = 0; i < copy.length; i++) {
                String command = "cmd /c copy \"" + copyFile.get(i) + "\"" + " \"" + tmp + "\" /y";
                try {
                    Process child = Runtime.getRuntime().exec(command);
                    InputStreamReader in = new InputStreamReader(child.getInputStream(), "MS949");
                    int c;
                    StringBuilder result;
                    result = new StringBuilder();
                    while ((c = in.read()) != -1) {
                        result.append((char) c);
                    }
                    if (result.toString().contains("0개 파일이 복사되었습니다.")) {
                            JOptionPane.showMessageDialog(null, "액세스 권한이 없습니다.", "에러", JOptionPane.ERROR_MESSAGE);
                        in.close();
                        break;
                    }
                    in.close();
                    count++;
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
            getJList();
            if (count == copy.length) {
                tf.setText(path);
                getJList();
            }
        }
    }

    private void getJList() {
    	
        File[] directory_list;
        File[] file_list;
        name = new File(path);

        directory_list = name.listFiles(File::isDirectory);
        file_list = name.listFiles(File::isFile);
        
        int count = 1;
        directoryName_List = new String[0];
        if (directory_list != null) {
            directoryName_List = new String[directory_list.length + 1];
            for (int i = -1; i < directory_list.length; i++) {
                String back = "..";
                if (i == -1) directoryName_List[0] = back;
                else {
                    if (directory_list[i].getName().contains("$") ||
                            directory_list[i].getName().contains("Recovery") ||
                            directory_list[i].getName().contains("System") ||
                            directory_list[i].getName().contains("Temp") ||
                            directory_list[i].getName().contains("PerfLogs") ||
                            directory_list[i].getName().contains("Documents and Settings") ||
                            !directory_list[i].canRead()) continue;

                    directoryName_List[count] = directory_list[i].getName();
                    count++;
                }
            }
        }

        list.setListData(directoryName_List);
        if (list.getVisibleRowCount() != 0)
            data = new String[0][3];
        if (file_list != null) {
            {
                data = new String[file_list.length][3];
                for (int i = 0; i < file_list.length; i++) {
                    data[i][0] = file_list[i].getName();
                    String file_size;
                    long size = file_list[i].length();
                    if (size < 1024) {
                        file_size = String.format("%d B", size);
                    } else if (size < 1024 * 1024) {
                        file_size = String.format("%.2f KB", size / 1024.0);
                    } else if (size < 1024 * 1024 * 1024) {
                        file_size = String.format("%.2f MB", size / 1048576.0);
                    } else {
                        file_size = String.format("%.2f GB", size / 1073741824.0);
                    }
                    data[i][1] = file_size;
                    Date dt = new Date(file_list[i].lastModified());
                    SimpleDateFormat formatter = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
                    String date = formatter.format(dt);
                    data[i][2] = String.valueOf(date);
                }
            }

            setTable();
        } else {
                JOptionPane.showMessageDialog(null, "액세스 권한이 없습니다.", "에러", JOptionPane.ERROR_MESSAGE);
            getBack = new File(path, "..");
            try {
                path = getBack.getCanonicalPath();
            } catch (Exception ignored) {

            }
            tf.setText(path);
            getJList();

        }
    }

    private void setTable() {
            model = new DefaultTableModel(data, English_columnNames);
            jt.setModel(model);
            jl.setText("File Explorer");        
    }

    public static void main(String[] args) {
        new FileManager();
    }
}
