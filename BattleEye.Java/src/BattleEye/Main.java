package BattleEye;

import BattleEye.Socket.BattlEyeSocket;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    private static BattlEyeSocket socket;

    public static void main(String[] args) {
        try {
            socket = new BattlEyeSocket("127.0.0.1", 2302, "VPPTest", true);

            socket.connect();
            socket.login();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread thread1 = new Thread(()->{
            while(socket.isConnected())
            {
                try {
                    socket.receiveCallback();
                    Thread.sleep(800);
                }catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread thread2 = new Thread(()->{
           while(socket.isConnected())
           {
               try{
                    socket.sendNextPacket();
                    Thread.sleep(800);
               }catch (IOException | InterruptedException e) {
                    e.printStackTrace();
               }
           }
        });

        thread1.start();
        thread2.start();

        while(true) {
            try {
                Thread.sleep(10000);
                //socket.sendCommand("say -1 hello");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } /*catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }
}
