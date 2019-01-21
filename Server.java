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
	File root = new File("D:/程序/Java/WangPan/ServerDocument/");
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
		this.setTitle("服务器");
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
			socket = serverSocket.accept(); // 接入客户端
			jTextArea.append("客户端接入" + "\n");
			pStream = new PrintStream(socket.getOutputStream());
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String newClient = bReader.readLine(); // 读入用户昵称
			newClient = newClient.split("#")[1];
			temproot = new File(root.getPath() + "/" + newClient + "/"); // 根据名称修改根目录
			root = new File(temproot.getPath());
			jTextArea.append(temproot.getPath() + "\n");
			if (!temproot.exists()) {
				temproot.mkdirs(); // 新建目录
			}
			while (true) {
				msg = bReader.readLine(); // 从客户端读入命令信息
				msgs = msg.split("#"); // 根据命令前缀判断命令信息
				jTextArea.append(msg + "\n");

				if (msgs[0].equals(DownloadMsg)) { // 当客户端要求下载文件
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
	// 下载文件
	void DownloadFileAction() throws Exception { // 客户端新建下载
		msg = msgs[1]; // 获取相对路径
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

		byte[] data = new byte[2048]; // 输出数据
		int len = 0;
		while ((len = iStream.read(data)) != -1) {
			oStream.write(data, 0, len);
			oStream.flush();
		}
		jTextArea.append("文件下载完毕" + "\n");
		iStream.close();
		oStream.close(); // 关闭输入输出流
		socket.close();
		socket = serverSocket.accept();
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		jTextArea.append("客户端重新接入" + "\n");
	}

	// ***
	// 上传文件
	void UploadFileAction() throws Exception { // 客户端新建上传
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
		jTextArea.append("文件上传完成\n");
		iStream.close();
		oStream.close(); // 关闭输入输出流
		socket.close();
		socket = serverSocket.accept();
		pStream = new PrintStream(socket.getOutputStream());
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		jTextArea.append("客户端重新连接\n");
	}

	// ***
	// 访问子目录
	void VisitDirAction() throws Exception {
		msg = msgs[1];

		if (msg.equals(ReturnMsg)) {
			if (temproot.getPath().equals(root.getPath())) {
				// 若已到达根目录，则直接返回
				return;
			} else {
				// 否则向上一级
				temproot = temproot.getParentFile();
			}
		} else {
			temproot = new File(temproot, msg);
			if (!temproot.isDirectory()) {
				// 若选中项为文件，则仍访问其双亲目录
				temproot = temproot.getParentFile();
				return;
			}
		}
	}

	// ***
	// 更新列表
	void UpdateList() throws Exception {
		String[] lists = temproot.list();
		String string = null;
		if (temproot.getPath().equals(root.getPath())) {
			string = "...";
		} else {
			string = "返回上一级";
		}
		for (String list : lists) {
			string = string + "@" + list;
		}
		pStream.println(string);
	}

	// ***
	// 判断文件类型
	void CheckType() throws Exception {
		msg = msgs[1];
		File file = new File(temproot, msg);
		if (file.isDirectory()) {
			// 当前选中的为文件夹或null时弹出文件夹菜单
			pStream.println(IsDirMsg);
		} else {
			pStream.println(IsFileMsg);
		}
	}

	// ***
	// 在当前目录下新建文件夹
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
	// 重命名选中的文件夹
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
	// 删除选中文件夹
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
	// 重命名选中的文件
	void RenameFileAction() throws Exception {
		String preFileName = msgs[1];
		String newFileName = msgs[2];
		String[] name = preFileName.split("\\."); // 由于split函数通过正则表达式分隔，因此这里使用"\\."分隔
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
	// 删除选中文件
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
