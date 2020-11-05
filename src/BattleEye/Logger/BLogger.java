package BattleEye.Logger;

import java.nio.file.Path;

public interface BLogger {
    Path createLogFile();
    Path createErrorFile();

    void log(String msg);
    void error(String msg);
}
