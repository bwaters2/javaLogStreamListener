package com.rs.logstream;

import com.rs.logstream.websocket.WebSocketClient;

import java.util.Scanner;

public class Application {

    public static String masherySocketURL = "SEND THE MASHERY URL AS A COMMAND LINE ARGUMENT OR REPLACE THIS WITH YOUR URL";

    public static void main(String[] args) {

        masherySocketURL = args.length > 0 ? args[0] : masherySocketURL;
        WebSocketClient ws = new WebSocketClient();
        ws.connect(masherySocketURL);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

    }
}

