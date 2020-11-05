package BattleEye.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BattlEyeLogger implements BLogger {

    private final String[] directories = {"./BattlEyeLogger", "./BattlEyeLogger/ErrorReports", "./BattlEyeLogger/Logs"};
    private Path currentLogPath;
    private Path currentErrorLogPath;

    private static BLogger logger = new BattlEyeLogger();

    public BattlEyeLogger() {
        for (String path : directories) {
            if (!Files.exists(Paths.get(path))) {
                try {
                    Files.createDirectory(Paths.get(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        currentLogPath = createLogFile();
    }

    @Override
    public Path createLogFile() {
        Path logPath = GetDatedPath(2);

        if (!Files.exists(logPath)) {
            try {
                Files.createFile(logPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return logPath;
    }

    @Override
    public Path createErrorFile() {
        Path errorLogPath = GetDatedPath(1);

        if (!Files.exists(errorLogPath)) {
            try {
                Files.createFile(errorLogPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return errorLogPath;
    }

    @Override
    public void log(String msg) {
        if (currentLogPath == null || !Files.exists(currentLogPath)) {
            currentLogPath = createLogFile();
        }

        String messageString = "[BattlEye]:: " + msg;
        System.out.println(messageString);
        messageString += "\n";
        byte[] logMessageBytes = messageString.getBytes();

        try {
            Files.write(currentLogPath, logMessageBytes, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void error(String msg) {
        if (currentErrorLogPath == null || !Files.exists(currentLogPath)) {
            currentErrorLogPath = createErrorFile();
        }

        String messageString = "[BattlEye]:: " + msg;
        System.err.println(messageString);
        messageString += "\n";
        byte[] errorLogMessageBytes = messageString.getBytes();

        try {
            Files.write(currentErrorLogPath, errorLogMessageBytes,StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Path GetDatedPath(int type) {
        String fileTypePrefix = "";

        if (type == 1)
            fileTypePrefix = directories[type] + "/BLogger-Error_";

        if (type == 2)
            fileTypePrefix = directories[type] + "/BLogger-Log_";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();

        String dateString = dtf.format(now);
        Path path = Paths.get(fileTypePrefix + dateString + ".log");

        return path;
    }

    public static BLogger GetLogger() {
        return logger;
    }
}
