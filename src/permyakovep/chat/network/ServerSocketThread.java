package permyakovep.chat.network;

public class ServerSocketThread extends Thread {

    private int port;

    public ServerSocketThread(String name, int port) {
        super(name);
        this.port = port;
        start();
    }

    @Override
    public void run() {
        System.out.println("SST started");
        while (!isInterrupted()) {
            System.out.println("SST is working!");
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                interrupt();
            }
        }
        System.out.println("SST stopped");
    }
}
