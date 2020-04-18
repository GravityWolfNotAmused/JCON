# JCON
BattlEye Rcon Library written in Java.

This is a work in progress, contains bugs and missing some features at the moment.

## Current Bugs:
After 2 and a half hours the library crashes that BE protected server.

## Sending Command Sample
```java
import BattleEye.Client.JConClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    private static JConClient client;

    public static void main(String[] args) {
        try {
            //Change 127.0.0.1 to IP address of the server which you are trying to connect to.
            client = new JConClient("127.0.0.1", 2302, "JCONTest", false);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        client.addPacketListener((type, sequence, data) -> {
            //Do your stuff on packet received.
        });

        client.addQueueListener((type, sequence, data) -> {
            //do your stuff on packet sent.
        });


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                client.sendCommand(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
