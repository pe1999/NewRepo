/*
============================================================================
Name        : Lesson 6 Homework
Author      : Пермяков Евгений
============================================================================
*/

/**
 *  - Полностью разобраться с кодом урока
 *  - Готовим вопросы по коду
 *  - * Реализовать массовую рассылку сообщений с помощью Vector (он как ArrayList)
 *  - * Автоматическое убирание верхней панели и появление нижней (и наоборот)
 *  - * Реализовать кнопку Disconnect
 *  И, чтобы не терять на уроке время, скачиваем SQLite и JDBC
 *  https://sqlitestudio.pl/index.rvt
 *  https://bitbucket.org/xerial/sqlite-jdbc/downloads/
 *  На уроке будем устанавливать и настраивать этот инструментарий.
 */

package ru.geekbrains.java.chat.client;

import ru.geekbrains.java.chat.library.Messages;
import ru.geekbrains.java.network.SocketThread;
import ru.geekbrains.java.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final String WIN_TITLE = "Chat client";
    private static final String[] EMPTY_LIST = new String[0];
    //private static final String CHAT_LOG_FILENAME = "./chat.log";

    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField(/*"95.84.209.91"*/ "127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);

    private final JTextField tfLogin = new JTextField("pe1999" /*"ivan_igorevich"*/);
    private final JPasswordField tfPassword = new JPasswordField("111111");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private boolean shownIoErrors = false;
    private SocketThread socketThread;

    JScrollPane scrollLog;

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //посреди экрана
        setSize(WIDTH, HEIGHT);
        setTitle(WIN_TITLE);
        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);
        panelBottom.setVisible(false);

        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);

        log.setEditable(false);

        /*JScrollPane */scrollLog = new JScrollPane(log);

        JScrollPane scrollUsers = new JScrollPane(userList);

/*        String[] users = { "user1_)_", "user2", "user3", "user4", "user5", "user6",
                "user7", "user8", "user9", "user10", "user12", "user13_123_123", };
*/
        userList.setListData(EMPTY_LIST);
        //scrollUsers.setPreferredSize(new Dimension(100, 0));

        JSplitPane mySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scrollLog, scrollUsers);



        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);
        //add(scrollUsers, BorderLayout.EAST);
        //add(scrollLog, BorderLayout.CENTER);
        add(mySplitPane, BorderLayout.CENTER);
        mySplitPane.setDividerSize(1);
        mySplitPane.setDividerLocation(280);
        setAlwaysOnTop(true);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            disconnect();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void disconnect() {
        socketThread.close();
    }

    void sendMessage() {
        String msg = tfMessage.getText();
        //String username = tfLogin.getText();
        if ("".equals(msg)) return;
        //log.append(username + ": " + msg + "\n");
        tfMessage.setText(null);
        tfMessage.requestFocusInWindow();
        socketThread.sendMessage(Messages.getTypeBroadcastShort(msg));
//        try (FileWriter out = new FileWriter("log.txt", true)) {
//            out.write(username + ": " + msg + "\n");
//            out.flush();
//        } catch (IOException e) {
//            if (!shownIoErrors) {
//                shownIoErrors = true;
//                log.append("System: File write error\n");
//                JOptionPane.showMessageDialog(this, "File write error", "Exception", JOptionPane.ERROR_MESSAGE);
//            }
//        }
    }

    void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void handleMessage(String value) {
        String[] arr = value.split(Messages.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Messages.AUTH_ACCEPT:
                setTitle(WIN_TITLE + " nickname: " + arr[1]);
                break;
            case Messages.AUTH_DENIED:
                putLog(value);
                break;
            case Messages.USER_LIST:
                String users = value.substring(Messages.USER_LIST.length() + Messages.DELIMITER.length());
                String[] usersArr= users.split(Messages.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Messages.MSG_FORMAT_ERROR:
                //
                socketThread.close();
                break;
            case Messages.TYPE_BROADCAST:
                putLog(dateFormat.format(Long.parseLong(arr[1])) + arr[2] + ": " + arr[3]);
                break;
            default:
                throw new RuntimeException("Unknown message from server" + value);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] ste = e.getStackTrace();
        String msg;
        if (ste.length == 0)
            msg = "Empty stacktrace";
        else
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + "\t at " + ste[0];

        JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);

    }

    void connect() {
        Socket socket = null;
        try {
            socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
        } catch (IOException e) {
            log.append("Exception: " + e.getMessage());
        }
        socketThread = new SocketThread(this, "Client thread", socket);
    }

    /**
     * SocketThread listener methods
     */

    @Override
    public void onSocketThreadStart(SocketThread thread, Socket socket) {
        putLog("Connection established");
    }

    @Override
    public void onSocketThreadStop(SocketThread thread) {
        setTitle(WIN_TITLE);
        userList.setListData(EMPTY_LIST);
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
    }

    @Override
    public void onSocketIsReady(SocketThread thread, Socket socket) {
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        thread.sendMessage(Messages.getAuthRequest(login, password));
        panelTop.setVisible(false);
        panelBottom.setVisible(true);
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMessage(msg);
/*
                String[] arr = msg.split(Messages.DELIMITER);
                switch (arr[0]) {
                    case Messages.TYPE_BROADCAST:
                        putLog(arr[3], dateFormat.format(Long.parseLong(arr[1])) + arr[2]);
                        break;
                    case Messages.AUTH_ACCEPT:
                        putLog("Authentication accept", "system");
                        break;
                    case Messages.AUTH_DENIED:
                        putLog("Authentication denied", "system");
                        break;
                    case Messages.MSG_FORMAT_ERROR:
                        putLog("Message format error", "system");
                        break;
                    default:
                }
*/
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Exception e) {
        putLog("SocketException");
    }
}
