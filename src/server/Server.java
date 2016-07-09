package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.mysql.jdbc.ResultSet;
import comunicazione.*;
import database.*;

class ServerThread extends Thread {
    public boolean Attivo = true;

    List<String> utenteLoggato = new ArrayList<>();


    Socket socket;
    ObjectInputStream dalClient;
    ObjectOutputStream versoClient;

    ServerThread(Socket socket) {
        this.socket = socket;
    }

    void gestisciLogin(List<String>Utente) {
        Risposta risposta;

        String username = Utente.get(0);
        String password = Utente.get(1);

        boolean login = false;
        String dbUsername = "", dbPassword = "", dbName = "", dbSurname = "", dbProfPic = "";

        int dbId = 0;
        try {
            System.out.println("provo la connessione");
            ConnessioneDatabase.Connetti();
            String query = "SELECT * FROM users WHERE email = " + username + " AND password = " + password;
            ConnessioneDatabase.cmd.executeQuery(query);

            ResultSet rs = (ResultSet) ConnessioneDatabase.cmd.getResultSet();

            while (rs.next()) {

                dbId = rs.getInt("id");
                dbUsername = rs.getString("email");
                dbPassword = rs.getString("password");
                dbName = rs.getString("name");
                dbSurname = rs.getString("surname");
                dbProfPic = rs.getString("profile_picture");

                if (dbPassword.equals(password)) {
                    login = true;
                }
            }

            utenteLoggato.add(String.valueOf(dbId));
            utenteLoggato.add(dbUsername);
            utenteLoggato.add(dbPassword);
            utenteLoggato.add(dbName);
            utenteLoggato.add(dbSurname);
            utenteLoggato.add(dbProfPic);

            if (login)
                risposta = new Risposta(TipiRisposte.Successo, utenteLoggato);
            else
                risposta = new Risposta(TipiRisposte.Fallimento, null);
            versoClient.writeObject(risposta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**void gestisciRegistrazione(MUser r) {
        Risposta risposta;
        String messaggio = "";
        String Nome = r.getNome();
        String Cognome = r.getCognome();
        String Username = r.getUsername();
        String Password = r.getPassword();
        String Nascita = r.getNascita();
        int Matricola = 0;
        boolean ok = true;

        try {
            ConnessioneDatabase.Connetti();
            ConnessioneDatabase.cmd.executeUpdate(Query.Registrazione(Nome, Cognome, Username, Password, Nascita));

            String query = "SELECT * FROM `persona`";
            try {
                ConnessioneDatabase.Connetti();
                ConnessioneDatabase.cmd.executeQuery(query);
                ResultSet rs = (ResultSet) ConnessioneDatabase.cmd.getResultSet();
                while (rs.next()) {
                    Matricola = rs.getInt("matricola");
                }

                ConnessioneDatabase.cmd.executeUpdate(Query.RegistraStudente(Matricola));
            } catch (Exception e) {
                e.printStackTrace();
            }


            utenteLoggato = new MUser(Matricola, Cognome, Nome, Nascita, Username, Password);
            gestisciUtenteCrea(utenteLoggato);

            if (ok)
                risposta = new Risposta(TipiRisposte.Successo, utenteLoggato.getNome());
            else
                risposta = new Risposta(TipiRisposte.Fallimento, utenteLoggato.getNome());
            versoClient.writeObject(risposta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }**/

    public void run() {
        try {
            dalClient = new ObjectInputStream(socket.getInputStream());
            versoClient = new ObjectOutputStream(socket.getOutputStream());

            while (Attivo) {
                Richiesta richiesta;

                richiesta = (Richiesta) dalClient.readObject();

                System.out.println("tipo richiesta: " + richiesta.getTipo());

                switch (richiesta.getTipo()) {
                    case TipiRichieste.Login:
                        gestisciLogin((List<String>) richiesta.Oggetto);
                        break;

                    /**case TipiRichieste.Registrazione:
                        gestisciRegistrazione((MUser) richiesta.Oggetto);
                        break;**/

                    default:
                        System.out.println("Errore Richiesta");
                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

public class Server {

    static Socket sock;
    static ServerSocket ssock;

    public static void main(String[] args) throws Exception {
        ssock = new ServerSocket(4000);

        while (true) {
            System.out.println("START SERVER: SERVER IN ASCOLTO!!");
            sock = ssock.accept();

            ServerThread clientThread = new ServerThread(sock);
            clientThread.start();
        }
    }
}
