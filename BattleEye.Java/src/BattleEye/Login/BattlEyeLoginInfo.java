package BattleEye.Login;

import java.net.InetSocketAddress;

public class BattlEyeLoginInfo extends InetSocketAddress {
    private String password;

    public BattlEyeLoginInfo(String hostname, int port, String pass) {
        super(hostname, port);
        password = pass;
    }

    public String getPassword()
    {
        return password;
    }
}
