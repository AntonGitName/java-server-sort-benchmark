package ru.mit.spbau.antonpp.benchmark.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * @author antonpp
 * @since 20/12/2016
 */
@Slf4j
public class Application {
    private static final String CMD_HELP = "help";
    private static final String CMD_EXIT = "exit";
    private static final String CMD_START = "start";
    private static final String CMD_STOP = "stop";

    private final JCommander jc;

    @Parameter(names = {"-p", "--port"})
    private int port = 31001;

    @Parameter(names = {"-h", "--help"}, description = "Print this help message and exit", help = true)
    private boolean help;

    @Nullable
    private Server server;

    private Application(String[] args) throws IOException {
        jc = new JCommander(this);

        try {
            jc.parse(args);
        } catch (Exception e) {
            log.warn("Failed to parse input", e);
            System.out.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (help) {
            jc.usage();
        }
    }

    private static void printCommands() {
        final String fmt = "\t%-15s-\t%s%n";
        String cmd = CMD_HELP;
        System.out.printf(fmt, cmd, "Print list of available commands");
        System.out.println();
        cmd = CMD_EXIT;
        System.out.printf(fmt, cmd, "Exit the application and saves tracker state");
        System.out.println();
        cmd = String.format("%s <mode>", CMD_START);
        System.out.printf(fmt, cmd, "Starts server with specified mode which can be one of the above:");
        for (ServerMode serverMode : ServerMode.values()) {
            System.out.printf("\t\t%d:\t%s%n", serverMode.ordinal(), serverMode);
        }
        System.out.println();
        cmd = CMD_STOP;
        System.out.printf(fmt, cmd, "Stops server");
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        new Application(args).readlineLoop();
    }

    private static void printToLogAndSout(String msg, Throwable e) {
        log.error(msg, e);
        System.out.println("ERROR");
        System.out.println(e);
    }

    private void readlineLoop() {
        val user = System.getProperty("user.name");
        val path = Paths.get(System.getProperty("user.home")).relativize(Paths.get(System.getProperty("user.dir")));

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.printf("%s@~/%s$ ", user, path);

                val line = scanner.nextLine();
                log.debug("User typed: {}", line);
                val split = line.split("\\s+");

                switch (split[0]) {

                    case CMD_STOP:
                        handleStop();
                        break;
                    case CMD_START:
                        handleStart(split);
                        break;
                    case CMD_EXIT:
                        handleExit();
                        return;
                    case CMD_HELP:
                    default:
                        printCommands();
                        break;
                }
            }
        }
    }

    private void handleExit() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                printToLogAndSout("Failed to stop server", e);
            }
        }
        System.out.println("Bye!");
    }

    private void handleStop() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                printToLogAndSout("Failed to stop server", e);
            }
        } else {
            System.out.println("You must start server first");
        }
    }

    private void handleStart(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong arguments count");
        }
        final int x;
        try {
            x = Integer.valueOf(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Mode must be a number");
            return;
        }
        if (x < 0 || x >= ServerMode.values().length) {
            System.out.println("Invalid server mode.");
        }
        try {
            server = ServerFactory.create(port, ServerMode.values()[x]);
            server.start();
        } catch (IOException e) {
            printToLogAndSout("Could not start server", e);
        }
    }
}
