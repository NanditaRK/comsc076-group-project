import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * This class provides functionality to BitOutputStream which writes to files and streams.
 * 
 * @author Gunit Pabla
 * @author Nandita Raj Kumar
 * @author Citlally Chavez-Ramos
 * @author Daniiar Gadeev
 * 
 * @since Dec 9 2023
 */

 public class BitOutputStream implements Closeable {

    private OutputStream output;
    private int currentByte;
    private int numBits;

    // Constructs a BitOutputStream with the specified OutputStream.
    public BitOutputStream(OutputStream out) {
        output = Objects.requireNonNull(out);
        currentByte = 0;
        numBits = 0;
    }

    // Writes the specified bit to the output stream.
    public void write(int b) throws IOException {
        if (b != 0 && b != 1)
            throw new IllegalArgumentException("Argument must be 0 or 1");
        currentByte = (currentByte << 1) | b;
        numBits++;
        if (numBits == 8) {
            output.write(currentByte);
            currentByte = 0;
            numBits = 0;
        }
    }

    // Closes the BitOutputStream and writes any remaining bits.
    public void close() throws IOException {
        while (numBits != 0)
            write(0);
        output.close();
    }
}