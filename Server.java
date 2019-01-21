package WangpanFinal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Server extends JFrame implements Runnable {
	JTextArea jTextArea = new JTextArea();
	File root = new File("D:/����/Java/WangPan/ServerDocument/");
	File temproot = null;
	InputStream iStream = null;
	OutputStream oStream = null;
	PrintStream pStream = null;
	BufferedReader bReader = null;
	String msg = null;
	String[] msgs = null;
	ServerSocket serverSocket = null;
	Socket socket = null;
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

	public Server() throws Exception {
		this.add(jTextArea);
		this.setTitle("������");
		this.setSize(600, 400);
		this.setVisible(true);
		this.setLocation(500, 500);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(9900);
			socket = serverSocket.accept(); // ����ͻ���
			jTextArea.append("�ͻ��˽���" + "\n");
			pStream = new PrintStream(socket.getOutputStream());
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String newClient = bReader.readLine(); // �����û��ǳ�
			newClient = newClient.split("#")[1];
			temproot = new File(root.getPath() + "/" + newClient + "/"); // ���������޸ĸ�Ŀ¼
			root = new File(temproot.getPath());
			jTextArea.append(temproot.getPath() + "\n");
			if (!temproot.exists()) {
				temproot.mkdirs(); // �½�Ŀ¼
			}
			while (true) {
				msg = bReader.readLine(); // �ӿͻ��˶���������Ϣ
				msgs = msg.split("#"); // ��������ǰ׺�ж�������Ϣ
				jTextArea.append(msg + "\n");

				if (msgs[0].equals(DownloadMsg)) { // ���ͻ���Ҫ�������ļ�
					DownloadFileAction();
				} else if (msgs[0].equals(UploadMsg)) {
					UploadFileAction();
				} else if (msgs[0].equals(VisitMsg)) {
					VisitDirAction();
				} else if (msgs[0].equals(UpdateMsg)) {
					UpdateList();
				} else if (msgs[0].equals(CheckTypeMsg)) {
					CheckType();
				} else if (msgs[0].equals(NewDirMsg)) {
					NewDirAction();
				} else if (msgs[0].equals(RenaDirMsg)) {
					RenameDirAction();
				} else if (msgs[0].equals(DelDirMsg)) {
					DeleteDirAction();
				} else if (msgs[0].equals(RenaFileMsg)) {
					RenameFileAction();
				} else if (msgs[0].equals(DelFileMsg)) {
					DeleteFileAction();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// ***
	// �����ļ�
	void DownloadFileAction() throws Exception { // �ͻ����½�����
		msg = msgs[1]; // ��ȡ���·��
		File file = new File(temproot, msg);

		if (!file.exists()) {
			jTextArea.append(msg + NotExistMsg + "\n");
			pStream.println(NotExistMsg);
			return;
		}

		jTextArea.append(msg + ExistMsg + "\n");
		pStream.println(ExistMsg);
		iStream = new FileInputStream(file);
		oStream = socket.getOutputStream();
		// pStream = new PrintStream(oStream);
		// pStream.println(msg);

		byte[] data = new byte[2048]; // �������
		int len = 0;
		while ((len = iStream.read(data)) != -1) {
			oStream.write(data, 0, len);
			oStream.flush();
		}
		jTextArea.append("�ļ��������" + "\n");
		iStream.close();
		oStream.close(); // �ر����������
		socket.close();
		socket = serverSocket.accept();
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		jTextArea.append("�ͻ������½���" + "\n");
	}

	// ***
	// �ϴ��ļ�
	void UploadFileAction() throws Exception { // �ͻ����½��ϴ�
		msg = msgs[1];

		File file = new File(temproot, msg);
		if (!file.exists()) {
			file.createNewFile();
		}
		iStream = socket.getInputStream();
		oStream = new FileOutputStream(file);
		byte[] data = new byte[2048];
		int len = 0;
		while ((len = iStream.read(data)) != -1) {
			oStream.write(data, 0, len);
			oStream.flush();
		}
		jTextArea.append("�ļ��ϴ����\n");
		iStream.close();
		oStream.close(); // �ر����������
		socket.close();
		socket = serverSocket.accept();
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		jTextArea.append("�ͻ�����������\n");
	}

	// ***
	// ������Ŀ¼
	void VisitDirAction() throws Exception {
		msg = msgs[1];

		if (msg.equals(ReturnMsg)) {
			if (temproot.getPath().equals(root.getPath())) {
				// ���ѵ����Ŀ¼����ֱ�ӷ���
				return;
			} else {
				// ��������һ��
				temproot = temproot.getParentFile();
			}
		} else {
			temproot = new File(temproot, msg);
			if (!temproot.isDirectory()) {
				// ��ѡ����Ϊ�ļ������Է�����˫��Ŀ¼
				temproot = temproot.getParentFile();
				return;
			}
		}
	}

	// ***
	// �����б�
	void UpdateList() throws Exception {
		String[] lists = temproot.list();
		String string = null;
		if (temproot.getPath().equals(root.getPath())) {
			string = "...";
		} else {
			string = "������һ��";
		}
		for (String list : lists) {
			string = string + "@" + list;
		}
		pStream.println(string);
	}

	// ***
	// �ж��ļ�����
	void CheckType() throws Exception {
		msg = msgs[1];
		File file = new File(temproot, msg);
		if (file.isDirectory()) {
			// ��ǰѡ�е�Ϊ�ļ��л�nullʱ�����ļ��в˵�
			pStream.println(IsDirMsg);
		} else {
			pStream.println(IsFileMsg);
		}
	}

	// ***
	// �ڵ�ǰĿ¼���½��ļ���
	void NewDirAction() throws Exception {
		String newDirName = msgs[1];
		File newDir = new File(temproot, newDirName);
		if (newDir.exists()) {
			pStream.println(ExistMsg);
		} else {
			pStream.println(NotExistMsg);
			newDir.mkdirs();
		}
	}

	// ***
	// ������ѡ�е��ļ���
	void RenameDirAction() throws Exception {
		String preDirName = msgs[1];
		String newDirName = msgs[2];
		if (preDirName.equals(newDirName)) {
			pStream.println(NotExistMsg);
			return;
		}
		File newfile = new File(temproot, newDirName);
		File prefile = new File(temproot, preDirName);
		if (newfile.exists() || !prefile.exists() || !prefile.isDirectory() || !prefile.renameTo(newfile)) {
			pStream.println(ExistMsg);
		} else {
			pStream.println(NotExistMsg);
		}
	}

	// ***
	// ɾ��ѡ���ļ���
	void DeleteDirAction() throws Exception {
		String DirName = msgs[1];
		File file = new File(temproot, DirName);
		if (!file.exists() || file.listFiles().length > 0 || !file.delete()) {
			pStream.println(NotExistMsg);
		} else {
			pStream.println(ExistMsg);
		}
	}

	// ***
	// ������ѡ�е��ļ�
	void RenameFileAction() throws Exception {
		String preFileName = msgs[1];
		String newFileName = msgs[2];
		String[] name = preFileName.split("\\."); // ����split����ͨ��������ʽ�ָ����������ʹ��"\\."�ָ�
		if (name[0].equals(newFileName)) {
			pStream.println(NotExistMsg);
			return;
		}
		String postfix = name[1];
		File newfile = new File(temproot, newFileName + "." + postfix);
		File prefile = new File(temproot, preFileName);
		if (newfile.exists() || !prefile.exists() || !prefile.isFile() || !prefile.renameTo(newfile)) {
			pStream.println(ExistMsg);
		} else {
			pStream.println(NotExistMsg);
		}
	}

	//***
	// ɾ��ѡ���ļ�
	void DeleteFileAction() throws Exception {
		String FileName = msgs[1];
		File file = new File(temproot, FileName);
		if (!file.exists() || !file.delete()) {
			pStream.println(ExistMsg);
		} else {
			pStream.println(NotExistMsg);
		}
	}

	public static void main(String[] args) throws Exception {
		new Server();
	}
}
