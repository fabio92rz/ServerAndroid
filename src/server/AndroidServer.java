package server;

/**
 * Created by Fabio on 08/07/2016.
 */
import com.mysql.jdbc.ResultSet;
import database.ConnessioneDatabase;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AndroidServer extends Thread {

    public static final int SERVERPORT = 8080;
    private boolean running = false;
    private PrintWriter bufferSender;
    private OnMessageReceived messageListener;
    private ServerSocket serverSocket;
    private Socket client;
    ArrayList<String> user = new ArrayList<>();

    public AndroidServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void run() {
        super.run();

        runServer();

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

    public void runServer() {
        running = true;
        boolean login = false;

        try {
            System.out.println("S: Connecting...");

            //create a server socket. A server socket waits for requests to come in over the network.
            serverSocket = new ServerSocket(SERVERPORT);

            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
            client = serverSocket.accept();

            System.out.println("S: Receiving...");

            try {
                bufferSender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (running) {

                    String message = null;

                    try {
                        message = in.readLine();
                    } catch (IOException e) {
                        System.out.println("Error reading message: " + e.getMessage());
                    }



                    if (message != null && messageListener != null) {
                        //call the method messageReceived from ServerBoard class
                        messageListener.messageReceived(message);
                    }

                    String username = in.readLine();
                    String password = in.readLine();

                    int dbId = 0;
                    String dbEmail = "";
                    String dbPassword = "";
                    String dbName = "";
                    String dbSurname = "";
                    String dbProfPic = "";

                    if (username != null && password != null){

                        ConnessioneDatabase.Connetti();
                        String query = "SELECT * FROM users WHERE email = " + username + " AND password = " + password;
                        ConnessioneDatabase.cmd.executeQuery(query);

                        ResultSet rs = (ResultSet) ConnessioneDatabase.cmd.getResultSet();

                        while (rs.next()) {

                            dbId = rs.getInt("id");
                            dbEmail = rs.getString("email");
                            dbPassword = rs.getString("password");
                            dbName = rs.getString("name");
                            dbSurname = rs.getString("surname");
                            dbProfPic = rs.getString("profile_picture");

                            if (dbPassword.equals(password)) {
                                login = true;

                                user.add(dbName);
                                user.add(dbEmail);
                                user.add(dbSurname);
                                user.add(dbProfPic);
                                user.add(String.valueOf(dbId));
                            }
                        }
                    }

                    ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
                    //outputStream.writeObject(user);
                    outputStream.writeObject(login);
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

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public static void main(String[] args) {

    }

}

