
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/**
 * This program enables you to connect to sshd server and get the shell prompt.
 *   $ CLASSPATH=.:../build javac Shell.java 
 *   $ CLASSPATH=.:../build java Shell
 * You will be asked username, hostname and passwd. 
 * If everything works fine, you will get the shell prompt. Output may
 * be ugly because of lacks of terminal-emulation, but you can issue commands.
 *
 */
import com.jcraft.jsch.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.*;

public class main {

	private static final int PORT_NUMER = 2222;
	private static final String DEFAULT_USER = "test";

	public static void main(String[] arg) {

		try {

			Session session = startConnection();

			// comando a ejecutar
			String command1 = "pwd";
			runCommand(command1, session);
			
			String command2 = "ls -a";
			runCommand(command2, session);
			
			session.disconnect();
			System.out.println("DONE");

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Inicia la conexion al servidor ssh con username, password
	 * 
	 * @return
	 * @throws JSchException
	 */
	private static Session startConnection() throws JSchException {
		JSch jsch = new JSch();

		// jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

		String host = JOptionPane.showInputDialog("Enter username@hostname", DEFAULT_USER + "@localhost");
		String user = host.substring(0, host.indexOf('@'));
		host = host.substring(host.indexOf('@') + 1);

		Session session = jsch.getSession(user, host, PORT_NUMER);

		String passwd = JOptionPane.showInputDialog("Enter password");
		session.setPassword(passwd);

		UserInfo ui = new MyUserInfo() {
			public void showMessage(String message) {
				JOptionPane.showMessageDialog(null, message);
			}

			public boolean promptYesNo(String message) {
				Object[] options = { "yes", "no" };
				int foo = JOptionPane.showOptionDialog(null, message, "Warning", JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				return foo == 0;
			}

			// If password is not given before the invocation of Session#connect(),
			// implement also following methods,
			// * UserInfo#getPassword(),
			// * UserInfo#promptPassword(String message) and
			// * UIKeyboardInteractive#promptKeyboardInteractive()

		};

		session.setUserInfo(ui);

		// It must not be recommended, but if you want to skip host-key check,
		// invoke following,
		// session.setConfig("StrictHostKeyChecking", "no");

		// session.connect();
		session.connect(30000); // making a connection with timeout.

		return session;
	}

	private static void printServerResponse(InputStream inServerResponse, Channel channel) throws IOException {
		byte[] tmp = new byte[1024];
		while (true) {
			while (inServerResponse.available() > 0) {
				int i = inServerResponse.read(tmp, 0, 1024);
				if (i < 0)
					break;
				System.out.print(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				System.out.println("exit-status: " + channel.getExitStatus());
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
	}

	private static void runCommand(String command, Session session) throws JSchException, IOException {
		// inicio un channel de ejecucion
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);

		// leer los datos que vienen de la consola
		InputStream inServerResponse = channel.getInputStream();
		channel.connect();

		printServerResponse(inServerResponse, channel);

		channel.disconnect();
	}

	public static abstract class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		public String getPassword() {
			return null;
		}

		public boolean promptYesNo(String str) {
			return false;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return false;
		}

		public boolean promptPassword(String message) {
			return false;
		}

		public void showMessage(String message) {
		}

		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
				boolean[] echo) {
			return null;
		}
	}
}