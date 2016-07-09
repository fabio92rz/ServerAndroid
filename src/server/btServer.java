package server;

/**
 * Created by Fabio on 08/07/2016.
 */

import com.mysql.jdbc.ResultSet;
import database.ConnessioneDatabase;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class btServer extends Thread {

    public static final int SERVERPORT = 8080;
    private boolean running = false;
    private PrintWriter bufferSender;
    private OnMessageReceived messageListener;
    private ServerSocket serverSocket;
    private Socket client;
    private ArrayList<String> user = new ArrayList<>();
    private boolean login = false;

    public btServer(OnMessageReceived messageListener) {

        this.messageListener = messageListener;
    }

    public static void main(String[] args) {

        btServer mprova = new btServer(new OnMessageReceived() {
            @Override
            public void messageReceived(String message) {


            }
        });

        mprova.start();
    }

    public void close() {

        running = false;

        if (bufferSender != null) {
            bufferSender.flush();
            bufferSender.close();
            bufferSender = null;
        }

        try {
            client.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("S: Done.");
        serverSocket = null;
        client = null;
    }

    public void sendMessage(String message) {
        if (bufferSender != null && !bufferSender.checkError()) {
            bufferSender.println(message);
            bufferSender.flush();
        }
    }

    private void runServer() {
        running = true;

        try {
            System.out.println("S: Connecting...");

            serverSocket = new ServerSocket(SERVERPORT);
            client = serverSocket.accept();
            System.out.println("S: Receiving...");

            try {

                bufferSender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (running) {

                    String username = in.readLine();
                    String password = in.readLine();

                    if (username != null && password != null) {

                        ConnessioneDatabase.Connetti();
                        String query = "SELECT * FROM users WHERE email = " + username + " AND password = " + password;
                        ConnessioneDatabase.cmd.executeQuery(query);

                        ResultSet rs = (ResultSet) ConnessioneDatabase.cmd.getResultSet();

                        int dbId = 0;
                        String dbEmail = "";
                        String dbPassword = "";
                        String dbName = "";
                        String dbSurname = "";
                        String dbProfPic = "";

                        while (rs.next()) {

                            dbId = rs.getInt("id");
                            dbEmail = rs.getString("email");
                            dbPassword = rs.getString("password");
                            dbName = rs.getString("name");
                            dbSurname = rs.getString("surname");
                            dbProfPic = rs.getString("profile_picture");

                            if (dbEmail.equals(username) && dbPassword.equals(password)) {
                                login = true;
                            }
                        }

                        user.add(dbName);
                        user.add(dbEmail);
                        user.add(dbSurname);
                        user.add(dbProfPic);
                        user.add(dbPassword);
                        user.add(String.valueOf(dbId));
                    }

                    ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
                    outputStream.write(login ? 1:0);

                    String message = null;
                    try {
                        message = in.readLine();
                    } catch (IOException e) {
                        System.out.println("Error reading message: " + e.getMessage());
                    }

                    if (message != null && messageListener != null) {
                        messageListener.messageReceived(message);
                    }
                }

            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        runServer();
    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}