# JCON
BattlEye Rcon Library written in Java.

This is a work in progress, contains bugs and missing some features at the moment.

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
            client = new JConClient("127.0.0.1", 2302, "VPPTest", true);
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
