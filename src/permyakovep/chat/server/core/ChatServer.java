package permyakovep.chat.server.core;
//package lesson_four.server.core;

import permyakovep.chat.network.ServerSocketThread;

public class ChatServer {

    private ServerSocketThread serverSocketThread;

    public void start(int port) {
        System.out.println("Start button pressed");
        if(serverSocketThread != null && serverSocketThread.isAlive()) {
            System.out.println("Server is already running");
            return;
        }
            serverSocketThread = new ServerSocketThread("Server", port);
    }

    public void stop() {
        System.out.println("Stop button pressed");
        if(serverSocketThread == null || !serverSocketThread.isAlive()) {
            System.out.println("Server is not running");
            return;
        }
        serverSocketThread.interrupt();
    }
}
