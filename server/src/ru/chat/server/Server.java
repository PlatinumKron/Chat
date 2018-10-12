package ru.chat.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.chat.network.TCP;
import ru.chat.network.TCPObserver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server implements TCPObserver {

    private  static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        new Server();
    }

    private final ArrayList<TCP> connections = new ArrayList<>();

    private Server() {
        System.out.println("Server Running...");
        try (ServerSocket serverSocket = new ServerSocket(8228)) {
            while (true) {
                try {
                    new TCP(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCP Connection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private FileReader file;

    {
        try {
            file = new FileReader("validNames.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    nameWrapper names = GSON.fromJson(file, nameWrapper.class);

    //Переопределение методов интерфейса
    @Override
    public synchronized void OnConnectionReady(TCP tcp) {
        connections.add(tcp);
        sendAll("Client connected: " + tcp);
    }

    @Override
    public synchronized void OnReceiveString(TCP tcp, String value) {
        for (String name:names.names) {
            if (value.split(":")[0].equals(name)) {
                sendAll(value);
            }
        }
    }

    @Override
    public synchronized void onDisconnect(TCP tcp) {
        connections.remove(tcp);
        sendAll("Client disconnected: " + tcp);
    }

    @Override
    public synchronized void onException(TCP tcp, Exception e) {
        System.out.println("TCP Connection exception:" + e);
    }

    private void sendAll(String value) {
        System.out.println(value);
        final  int cnt = connections.size();
        for (int i=0; i < cnt; i++) {
            connections.get(i).sendString(value);
        }
    }
}

class nameWrapper {

    public ArrayList<String> names;
}
