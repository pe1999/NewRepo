/*
============================================================================
Name        : Lesson 4 Homework
Author      : Пермяков Евгений
============================================================================
*/

package permyakovep.chat.client;
//package lesson_four.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

/*
	Отправлять сообщения в лог по нажатию кнопки или по нажатию клавиши Enter.
	Создать лог в файле (записи должны делаться при отправке сообщений).
*/

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final String CHAT_LOG_FILENAME = "./chat.log";

    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);

    private final JTextField tfLogin = new JTextField("ivan_igorevich");
    private final JPasswordField tfPassword = new JPasswordField("123456");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI());
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //посреди экрана
        setSize(WIDTH, HEIGHT);
        setTitle("Chat Client");
        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);

        log.setEditable(false);

        JScrollPane scrollLog = new JScrollPane(log);

        JScrollPane scrollUsers = new JScrollPane(userList);

        String[] users = { "user1_)_", "user2", "user3", "user4", "user5", "user6",
                "user7", "user8", "user9", "user10", "user12", "user13_123_123", };
        userList.setListData(users);
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
    public void actionPerformed(ActionEvent e)  {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            if(!tfMessage.getText().isEmpty()) writeMessage();
        } else {
            throw new RuntimeException("Unknown source: " + src);
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
            msg = e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n" + "\t at " + ste[0];
        JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private void writeMessage() {

        String toUser = userList.getSelectedValue() == null ? "" : " to " + userList.getSelectedValue();
        String message = "[" + tfLogin.getText() + toUser + "]: " + tfMessage.getText();

        log.append(message + "\r\n");

        FileWriter chatlog;
        try {
            chatlog = new FileWriter(CHAT_LOG_FILENAME, true);
            chatlog.write((message + "\r\n"));
            chatlog.close();
        }  catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "File I/O error", "Exception", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        tfMessage.setText("");
        tfMessage.requestFocusInWindow();
    }

}
