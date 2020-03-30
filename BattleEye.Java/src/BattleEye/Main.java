package BattleEye;

import BattleEye.Client.JConClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Main {
    private static JConClient client;

    public static void main(String[] args){
        try {
            client = new JConClient("127.0.0.1", 2302, "VPPTest", true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line;

        try {
            while ((line = reader.readLine()) != null)
            {
                client.sendCommand(line);
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}