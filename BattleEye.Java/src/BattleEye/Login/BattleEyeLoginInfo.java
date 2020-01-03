package BattleEye.Login;

import java.net.InetSocketAddress;

public class BattleEyeLoginInfo extends InetSocketAddress {
    private String password;

    public BattleEyeLoginInfo(String hostname, int port, String pass) {
        super(hostname, port);
        password = pass;
    }

    public String getPassword()
    {
        return password;
    }
}
