package support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Restores the process streams even when an assertion or the program fails. */
public final class ConsoleSession implements AutoCloseable {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStream redirectedOut = new PrintStream(output, true, StandardCharsets.UTF_8);

    public ConsoleSession(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        System.setOut(redirectedOut);
    }

    public String output() {
        redirectedOut.flush();
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Override
    public void close() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        redirectedOut.close();
    }
}
