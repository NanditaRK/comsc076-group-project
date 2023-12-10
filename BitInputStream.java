import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides functionality to BitInputStream which reads bits from files.
 * 
 * @author Gunit Pabla
 * @author Nandita Raj Kumar
 * @author Citlally Chavez-Ramos
 * @author Daniiar Gadeev
 * 
 * @since Dec 9 2023
 */

public class BitInputStream implements Closeable {

    private InputStream input;
    private int currentByte;
    private int numBits;

    // Constructs a BitInputStream with the specified InputStream.
    public BitInputStream(InputStream in) {
        if(in == null){
            throw new RuntimeException("Object is null.");
        }
        input = in;
        currentByte = 0;
        numBits = 0;
    }

    //reads the next bit from the input stream.
    public int read() throws IOException {
        if (numBits == 0) {
            currentByte = input.read();
            if (currentByte == -1) {
                return -1;
            }
            numBits = 8;
        }

        if (numBits <= 0) {
            throw new RuntimeException("The number of bits is equal to 0.");
        }

        numBits--;
        //shifts and reqrites byte
        return (currentByte >>> numBits) & 1;
    }

    //reads the next bit from the input stream, throwing an EOFException if the end of the stream is reached.
    public int hasnext() throws IOException {
        int result = read();
        if (result != -1)
            return result;
        else
            throw new EOFException("End of file has been reached.");
    }

    //closes the input stream to avoid errors
    public void close() throws IOException {
        input.close();
        currentByte = -1;
        numBits = 0;
    }
}
