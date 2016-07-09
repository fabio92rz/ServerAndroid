package server;

import com.mysql.jdbc.ResultSet;
import database.ConnessioneDatabase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.ArrayList;

/**
 * Created by Fabio on 07/07/2016.
 */
public class TCPServer extends Thread {

    public static final int ServerPort = 8080;
    private boolean running = false;
    ArrayList<String> user = new ArrayList<>();

    public static void main(String[] args) {

    }

    @Override
    public void run() {
        super.run();

        running = true;
        boolean login = false;

        try {
            System.out.println("S: Connecting....");

            ServerSocket serverSocket = new ServerSocket(ServerPort);
            Socket client = serverSocket.accept();
            System.out.println("S: Receiving...");

            try {

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (running) {

                    String username = in.readLine();
                    String password = in.readLine();

                    if (username != null && password != null){

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

                            if (dbPassword.equals(password)) {
                                login = true;
                            }
                        }

                        user.add(dbName);
                        user.add(dbEmail);
                        user.add(dbSurname);
                        user.add(dbProfPic);
                        user.add(String.valueOf(dbId));
                    }

                    ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
                    outputStream.writeObject(user);
                }

            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            } finally {
                client.close();
                System.out.println("S: Done");
            }
        } catch (Exception e) {
            System.out.println("S: Error");
        }
    }
}
