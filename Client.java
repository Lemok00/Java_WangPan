package WangpanFinal;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;

public class Client extends JFrame {
	private String NickName = null;
	private File root = new File("D:/程序/Java/WangPan/ClientDocument");
	File temproot = new File(root.getPath());
	Socket socket = null;
	InputStream iStream = null;
	OutputStream oStream = null;
	PrintStream pStream = null;
	BufferedReader bReader = null;
	String msg = null;
	String[] msgs = null;

	private JPanel jPanel = new JPanel();
	private DirMenu dirMenu = new DirMenu();
	private FileMenu fileMenu = new FileMenu();
	Vector<String> dirsvect = new Vector<>();
	private JList dirlist = new JList<>(dirsvect);
	private JScrollPane jScrollPane = new JScrollPane(dirlist);

	static String ExistMsg = "EXIST";
	static String NotExistMsg = "NEXIST";
	static String LoginMsg = "LOGIN";
	static String DownloadMsg = "DOWNLOAD";
	static String UploadMsg = "UPLOAD";
	static String VisitMsg = "VISIT";
	static String ReturnMsg = "RETURN";
	static String NewDirMsg = "NEWDIR";
	static String RenaDirMsg = "REDIR";
	static String DelDirMsg = "DELDIR";
	static String NewFileMsg = "NEWFILE";
	static String RenaFileMsg = "REFILE";
	static String DelFileMsg = "DELFILE";
	static String CheckTypeMsg = "CHECKTYPE";
	static String IsDirMsg = "ISDIR";
	static String IsFileMsg = "ISFILE";
	static String UpdateMsg = "UPDATE";

	public Client() throws Exception {
		NickName = JOptionPane.showInputDialog("请输入昵称");
		if(NickName == null) {
			NickName = "default";
		}

		socket = new Socket("127.0.0.1", 9900);
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		pStream.println(LoginMsg + "#" + NickName);

		UpdateList();
		dirlist.setFont(new Font("宋体", Font.BOLD, 15));
		this.add(jScrollPane);
		jPanel.setSize(getMaximumSize());

		dirlist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList theList = (JList) e.getSource();
				if (e.getButton() == e.BUTTON1 && e.getClickCount() == 2) {
					// 鼠标双击选中的文件或目录
					int index = theList.locationToIndex(e.getPoint());
					// System.out.println(index);
					// 双击空白处时index会置为最后一个选项
					// getCellBounds(index, index).contains(e.getPoint())判断鼠标单击的位置是否在最后一个选项中
					// 若不在该选项中则取消选择状态
					if (index != -1 && !theList.getCellBounds(index, index).contains(e.getPoint())) {
						dirlist.clearSelection();
						index = -1;
					}

					MouseDoubleClick(index);
				} else if (e.getButton() == e.BUTTON1 && e.getClickCount() == 1) {
					// 鼠标单击空白区域时取消选中
					int index = theList.locationToIndex(e.getPoint());
					// System.out.println(index);
					// 单击空白处时index会置为最后一个选项
					// getCellBounds(index, index).contains(e.getPoint())判断鼠标单击的位置是否在最后一个选项中
					// 若不在该选项中则取消选择状态
					if (index != -1 && !theList.getCellBounds(index, index).contains(e.getPoint())) {
						dirlist.clearSelection();
					}
				} else if (e.getButton() == e.BUTTON3) {
					// 鼠标右击选中的文件或目录
					int index = theList.locationToIndex(e.getPoint());
					if (index != -1 && !theList.getCellBounds(index, index).contains(e.getPoint())) {
						index = -1;
					}
					// System.out.println("右击"+index);
					if (index == -1) {
						dirlist.clearSelection();
					} else {
						dirlist.setSelectedIndex(index);
					}
					MouseRightClick(index, e.getX(), e.getY());
				}
			}
		});
		/* 这一段不要往前放，往前放就不会显示界面，我也不知道为啥 */
		this.setTitle(NickName);
		this.setSize(600, 400);
		this.setVisible(true);
		this.setLocation(400, 200);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	// ***
	// 处理鼠标双击时的事件
	public void MouseDoubleClick(int index) {
		if (index != -1) {
			VisitDirAction(index);
			// 更新列表
			UpdateList();
		}
	}

	// ***
	// 更新列表
	void UpdateList() {
		try {
			pStream.println(UpdateMsg + "#");
			String Msg = bReader.readLine();
			String[] dirs = Msg.split("@");
			dirsvect.clear();
			for (String dir : dirs) {
				dirsvect.add(dir);
			}
			dirlist.setListData(dirsvect);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// ***
	// 处理鼠标右击时的事件
	public void MouseRightClick(int index, int x, int y) {
		try {
			if (index == -1) {
				dirMenu.show(this, x, y);
			} else {
				String fileName = dirsvect.elementAt(dirlist.getSelectedIndex());
				File file = new File(temproot, fileName);
				pStream.println(CheckTypeMsg + "#" + fileName);
				String string = bReader.readLine();
				if (string.equals(IsDirMsg)) {
					// 当前选中的为文件夹或null时弹出文件夹菜单
					dirMenu.show(this, x, y);
				} else {
					fileMenu.show(this, x, y);
				}
			}
		} catch (Exception e) {

		}
	}

	// ***
	// 显示文件夹菜单
	class DirMenu extends JPopupMenu {
		private JMenuItem newDir = new JMenuItem("新建文件夹");
		private JMenuItem renameDir = new JMenuItem("重命名");
		private JMenuItem deleteDir = new JMenuItem("删除");
		private JMenuItem uploadFile = new JMenuItem("上传文件");

		public DirMenu() {
			this.add(newDir);
			this.add(renameDir);
			this.add(deleteDir);
			this.add(uploadFile);

			newDir.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						NewDirAction();
					} catch (Exception e1) {

					}
				}
			});

			renameDir.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						RenameDirAction();
					} catch (Exception e1) {

					}
				}
			});

			deleteDir.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						DeleteDirAction();
					} catch (Exception e1) {

					}
				}
			});

			uploadFile.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						new FileChose();
					} catch (Exception e1) {

					}
				}
			});
		}
	}

	// ***
	// 显示文件菜单
	class FileMenu extends JPopupMenu {
		private JMenuItem newDir = new JMenuItem("新建文件夹");
		private JMenuItem renameFile = new JMenuItem("重命名");
		private JMenuItem deleteFile = new JMenuItem("删除");
		private JMenuItem downloadFile = new JMenuItem("下载文件");

		public FileMenu() {
			this.add(newDir);
			this.add(renameFile);
			this.add(deleteFile);
			this.add(downloadFile);

			newDir.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						NewDirAction();
					} catch (Exception e1) {

					}
				}
			});

			renameFile.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						RenameFileAction();
					} catch (Exception e1) {

					}
				}
			});

			deleteFile.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						DeleteFileAction();
					} catch (Exception e1) {

					}
				}
			});

			downloadFile.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						DownloadFileAction();
					} catch (Exception e1) {

					}
				}
			});
		}
	}

	// 显示上传文件选择框
	class FileChose{
		private JFileChooser  jFileChooser = null;
		
		public FileChose() throws Exception{
			msg = null;
			jFileChooser = new JFileChooser("D:/程序/Java/WangPan/ClientDocument");
			jFileChooser.setFileSelectionMode(jFileChooser.FILES_AND_DIRECTORIES);
			jFileChooser.showDialog(new JLabel(), "选择");
			File file = jFileChooser.getSelectedFile();
			if(file.isFile()){
				msg = file.getPath();
				UploadFileAction();
			}else {
				JOptionPane.showMessageDialog(null, "只能上传单文件");
				msg = null;
			}
		}
	}


	// ***
	// 访问子目录
	void VisitDirAction(int index) {
		// 选中列表范围内
		String s = dirsvect.elementAt(index);
		// System.out.println(s);
		if (index == 0) {
			// 若选中“...”或“返回上一级”
			pStream.println(VisitMsg + "#" + ReturnMsg);
		} else {
			// 访问选中项
			pStream.println(VisitMsg + "#" + s);
		}
	}

	// ***
	// 在当前目录下新建文件夹
	void NewDirAction() throws Exception {
		String newDirName = JOptionPane.showInputDialog("请输入文件夹名称", "新建文件夹");
		if(newDirName == null) {
			return;
		}
		pStream.println(NewDirMsg + "#" + newDirName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "当前文件夹已存在");
		}
		UpdateList();
	}

	// ***
	// 重命名选中的文件夹
	void RenameDirAction() throws Exception {
		String preDirName = dirsvect.elementAt(dirlist.getSelectedIndex());
		String newDirName = JOptionPane.showInputDialog("请输入新的名称");
		pStream.println(RenaDirMsg + "#" + preDirName + "#" + newDirName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "无法修改当前文件夹名");
		}
		UpdateList();
	}

	// ***
	// 删除选中文件夹
	void DeleteDirAction() throws Exception {
		String DirName = dirsvect.elementAt(dirlist.getSelectedIndex());
		pStream.println(DelDirMsg + "#" + DirName);
		if (bReader.readLine().equals(NotExistMsg)) {
			JOptionPane.showMessageDialog(null, "无法删除当前文件夹");
		}
		UpdateList();
	}

	// ***
	// 缺少取消操作
	// 上传文件至当前目录下
	void UploadFileAction() throws Exception {
		if(msg == null) {
			return;
		}
		File file = new File(msg);
		pStream.println(UploadMsg + "#" + file.getName());
		// System.out.println(file.getPath());
		iStream = new FileInputStream(file);
		oStream = socket.getOutputStream();
		byte[] data = new byte[2048];
		int len = 0;
		while ((len = iStream.read(data)) != -1) {
			oStream.write(data, 0, len);
			oStream.flush();
		}
		oStream.close();
		iStream.close();
		socket.close();
		socket = new Socket("127.0.0.1", 9900);
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		JOptionPane.showMessageDialog(null, "文件上传完成");
		UpdateList();
	}

	// ***
	// 重命名选中的文件
	void RenameFileAction() throws Exception {
		String preFileName = dirsvect.elementAt(dirlist.getSelectedIndex());
		String newFileName = JOptionPane.showInputDialog("请输入新的名称");
		newFileName = newFileName.split("\\.")[0]; // 仅保留有效的输入
		pStream.println(RenaFileMsg + "#" + preFileName + "#" + newFileName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "无法修改当前文件名");
		}
		UpdateList();
	}

	// ***
	// 删除选中文件
	void DeleteFileAction() throws Exception {
		String FileName = dirsvect.elementAt(dirlist.getSelectedIndex());
		pStream.println(DelFileMsg + "#" + FileName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "无法删除当前文件");
		}
		UpdateList();
	}

	// ***
	// 下载文件
	void DownloadFileAction() throws Exception {
		msg = dirsvect.elementAt(dirlist.getSelectedIndex());
		pStream.println(DownloadMsg + "#" + msg);
		if (bReader.readLine().equals(NotExistMsg)) {
			return;
		}

		File file = new File(root, msg);
		file.createNewFile(); // 新建文件
		// System.out.println(file.getPath());
		iStream = socket.getInputStream();
		oStream = new FileOutputStream(file);
		byte[] data = new byte[2048]; // 传输文件
		int len = 0;
		while ((len = iStream.read(data)) != -1) {
			oStream.write(data, 0, len);
			oStream.flush();
		}
		oStream.close();
		iStream.close();
		socket.close();
		socket = new Socket("127.0.0.1", 9900);
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		JOptionPane.showMessageDialog(null, "文件下载完成");
	}

	public static void main(String[] args) throws Exception {
		try{
			new Client();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
}

