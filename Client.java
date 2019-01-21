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
	private File root = new File("D:/����/Java/WangPan/ClientDocument");
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
		NickName = JOptionPane.showInputDialog("�������ǳ�");
		if(NickName == null) {
			NickName = "default";
		}

		socket = new Socket("127.0.0.1", 9900);
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		pStream.println(LoginMsg + "#" + NickName);

		UpdateList();
		dirlist.setFont(new Font("����", Font.BOLD, 15));
		this.add(jScrollPane);
		jPanel.setSize(getMaximumSize());

		dirlist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList theList = (JList) e.getSource();
				if (e.getButton() == e.BUTTON1 && e.getClickCount() == 2) {
					// ���˫��ѡ�е��ļ���Ŀ¼
					int index = theList.locationToIndex(e.getPoint());
					// System.out.println(index);
					// ˫���հ״�ʱindex����Ϊ���һ��ѡ��
					// getCellBounds(index, index).contains(e.getPoint())�ж���굥����λ���Ƿ������һ��ѡ����
					// �����ڸ�ѡ������ȡ��ѡ��״̬
					if (index != -1 && !theList.getCellBounds(index, index).contains(e.getPoint())) {
						dirlist.clearSelection();
						index = -1;
					}

					MouseDoubleClick(index);
				} else if (e.getButton() == e.BUTTON1 && e.getClickCount() == 1) {
					// ��굥���հ�����ʱȡ��ѡ��
					int index = theList.locationToIndex(e.getPoint());
					// System.out.println(index);
					// �����հ״�ʱindex����Ϊ���һ��ѡ��
					// getCellBounds(index, index).contains(e.getPoint())�ж���굥����λ���Ƿ������һ��ѡ����
					// �����ڸ�ѡ������ȡ��ѡ��״̬
					if (index != -1 && !theList.getCellBounds(index, index).contains(e.getPoint())) {
						dirlist.clearSelection();
					}
				} else if (e.getButton() == e.BUTTON3) {
					// ����һ�ѡ�е��ļ���Ŀ¼
					int index = theList.locationToIndex(e.getPoint());
					if (index != -1 && !theList.getCellBounds(index, index).contains(e.getPoint())) {
						index = -1;
					}
					// System.out.println("�һ�"+index);
					if (index == -1) {
						dirlist.clearSelection();
					} else {
						dirlist.setSelectedIndex(index);
					}
					MouseRightClick(index, e.getX(), e.getY());
				}
			}
		});
		/* ��һ�β�Ҫ��ǰ�ţ���ǰ�žͲ�����ʾ���棬��Ҳ��֪��Ϊɶ */
		this.setTitle(NickName);
		this.setSize(600, 400);
		this.setVisible(true);
		this.setLocation(400, 200);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	// ***
	// �������˫��ʱ���¼�
	public void MouseDoubleClick(int index) {
		if (index != -1) {
			VisitDirAction(index);
			// �����б�
			UpdateList();
		}
	}

	// ***
	// �����б�
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
	// ��������һ�ʱ���¼�
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
					// ��ǰѡ�е�Ϊ�ļ��л�nullʱ�����ļ��в˵�
					dirMenu.show(this, x, y);
				} else {
					fileMenu.show(this, x, y);
				}
			}
		} catch (Exception e) {

		}
	}

	// ***
	// ��ʾ�ļ��в˵�
	class DirMenu extends JPopupMenu {
		private JMenuItem newDir = new JMenuItem("�½��ļ���");
		private JMenuItem renameDir = new JMenuItem("������");
		private JMenuItem deleteDir = new JMenuItem("ɾ��");
		private JMenuItem uploadFile = new JMenuItem("�ϴ��ļ�");

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
	// ��ʾ�ļ��˵�
	class FileMenu extends JPopupMenu {
		private JMenuItem newDir = new JMenuItem("�½��ļ���");
		private JMenuItem renameFile = new JMenuItem("������");
		private JMenuItem deleteFile = new JMenuItem("ɾ��");
		private JMenuItem downloadFile = new JMenuItem("�����ļ�");

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

	// ��ʾ�ϴ��ļ�ѡ���
	class FileChose{
		private JFileChooser  jFileChooser = null;
		
		public FileChose() throws Exception{
			msg = null;
			jFileChooser = new JFileChooser("D:/����/Java/WangPan/ClientDocument");
			jFileChooser.setFileSelectionMode(jFileChooser.FILES_AND_DIRECTORIES);
			jFileChooser.showDialog(new JLabel(), "ѡ��");
			File file = jFileChooser.getSelectedFile();
			if(file.isFile()){
				msg = file.getPath();
				UploadFileAction();
			}else {
				JOptionPane.showMessageDialog(null, "ֻ���ϴ����ļ�");
				msg = null;
			}
		}
	}


	// ***
	// ������Ŀ¼
	void VisitDirAction(int index) {
		// ѡ���б�Χ��
		String s = dirsvect.elementAt(index);
		// System.out.println(s);
		if (index == 0) {
			// ��ѡ�С�...���򡰷�����һ����
			pStream.println(VisitMsg + "#" + ReturnMsg);
		} else {
			// ����ѡ����
			pStream.println(VisitMsg + "#" + s);
		}
	}

	// ***
	// �ڵ�ǰĿ¼���½��ļ���
	void NewDirAction() throws Exception {
		String newDirName = JOptionPane.showInputDialog("�������ļ�������", "�½��ļ���");
		if(newDirName == null) {
			return;
		}
		pStream.println(NewDirMsg + "#" + newDirName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "��ǰ�ļ����Ѵ���");
		}
		UpdateList();
	}

	// ***
	// ������ѡ�е��ļ���
	void RenameDirAction() throws Exception {
		String preDirName = dirsvect.elementAt(dirlist.getSelectedIndex());
		String newDirName = JOptionPane.showInputDialog("�������µ�����");
		pStream.println(RenaDirMsg + "#" + preDirName + "#" + newDirName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "�޷��޸ĵ�ǰ�ļ�����");
		}
		UpdateList();
	}

	// ***
	// ɾ��ѡ���ļ���
	void DeleteDirAction() throws Exception {
		String DirName = dirsvect.elementAt(dirlist.getSelectedIndex());
		pStream.println(DelDirMsg + "#" + DirName);
		if (bReader.readLine().equals(NotExistMsg)) {
			JOptionPane.showMessageDialog(null, "�޷�ɾ����ǰ�ļ���");
		}
		UpdateList();
	}

	// ***
	// ȱ��ȡ������
	// �ϴ��ļ�����ǰĿ¼��
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
		JOptionPane.showMessageDialog(null, "�ļ��ϴ����");
		UpdateList();
	}

	// ***
	// ������ѡ�е��ļ�
	void RenameFileAction() throws Exception {
		String preFileName = dirsvect.elementAt(dirlist.getSelectedIndex());
		String newFileName = JOptionPane.showInputDialog("�������µ�����");
		newFileName = newFileName.split("\\.")[0]; // ��������Ч������
		pStream.println(RenaFileMsg + "#" + preFileName + "#" + newFileName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "�޷��޸ĵ�ǰ�ļ���");
		}
		UpdateList();
	}

	// ***
	// ɾ��ѡ���ļ�
	void DeleteFileAction() throws Exception {
		String FileName = dirsvect.elementAt(dirlist.getSelectedIndex());
		pStream.println(DelFileMsg + "#" + FileName);
		if (bReader.readLine().equals(ExistMsg)) {
			JOptionPane.showMessageDialog(null, "�޷�ɾ����ǰ�ļ�");
		}
		UpdateList();
	}

	// ***
	// �����ļ�
	void DownloadFileAction() throws Exception {
		msg = dirsvect.elementAt(dirlist.getSelectedIndex());
		pStream.println(DownloadMsg + "#" + msg);
		if (bReader.readLine().equals(NotExistMsg)) {
			return;
		}

		File file = new File(root, msg);
		file.createNewFile(); // �½��ļ�
		// System.out.println(file.getPath());
		iStream = socket.getInputStream();
		oStream = new FileOutputStream(file);
		byte[] data = new byte[2048]; // �����ļ�
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
		JOptionPane.showMessageDialog(null, "�ļ��������");
	}

	public static void main(String[] args) throws Exception {
		try{
			new Client();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
}

