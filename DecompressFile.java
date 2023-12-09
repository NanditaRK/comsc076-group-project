import java.io.*;
import java.util.*;

/**
 * This class provides functionality to decompress a file using Huffman coding.
 */
public final class DecompressFile {

    // Main method to decompress a file using Huffman coding.
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java decompressFile InputFile OutputFile");
            System.exit(1);
            return;
        }
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        try (BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                HuffmanTreeBuilder huffmanTreeBuilder = readCodeLengthTable(in);
                HuffmanTree huffmanTree = huffmanTreeBuilder.toHuffmanTree();
                decompress(huffmanTree, in, out);
            }
        }
    }

    // Reads the code length table from the input stream and constructs a HuffmanTreeBuilder.
    static HuffmanTreeBuilder readCodeLengthTable(BitInputStream in) throws IOException {
        int[] len = new int[257];
        for (int i = 0; i < len.length; i++) {
            int val = 0;
            for (int j = 0; j < 8; j++)
                val = (val << 1) | in.hasnext();
            len[i] = val;
        }
        return new HuffmanTreeBuilder(len);
    }

    // Decompresses the input stream using the provided HuffmanTree and writes the result to the output stream.
    static void decompress(HuffmanTree huffmanTree, BitInputStream in, OutputStream out) throws IOException {
        HuffmanInternalNode currentNode = (HuffmanInternalNode) huffmanTree.root;
        while (true) {
            int temp = in.hasnext();
            HuffmanNode nextNode;
            if (temp == 0) nextNode = currentNode.leftChild();
            else if (temp == 1) nextNode = currentNode.rightChild();
            else throw new AssertionError("Invalid value from hasnext()");

            if (nextNode instanceof HuffmanLeaf leaf) {
                int symbol = leaf.symbol();
                if (symbol == 256)  // EOF symbol
                    break;
                out.write(symbol);
                currentNode = (HuffmanInternalNode) huffmanTree.root;  // Reset to the root for the next symbol
            } else if (nextNode instanceof HuffmanInternalNode internalNode) {
                currentNode = internalNode;
            } else {
                throw new AssertionError("invalid node type");
            }
        }
    }
}

// This class represents a BitInputStream for reading individual bits from an underlying InputStream.
final class BitInputStream implements Closeable {

    private InputStream input;
    private int currentByte;
    private int numBits;

    // Constructs a BitInputStream with the specified InputStream.
    public BitInputStream(InputStream in) {
        input = Objects.requireNonNull(in);
        currentByte = 0;
        numBits = 0;
    }

    // Reads the next bit from the input stream.
    public int read() throws IOException {
        if (numBits == 0) {
            currentByte = input.read();
            if (currentByte == -1) {
                return -1;
            }
            numBits = 8;
        }

        if (numBits <= 0) {
            throw new AssertionError();
        }

        numBits--;
        return (currentByte >>> numBits) & 1;
    }

    // Reads the next bit from the input stream, throwing an EOFException if the end of the stream is reached.
    public int hasnext() throws IOException {
        int result = read();
        if (result != -1)
            return result;
        else
            throw new EOFException();
    }

    // Closes the underlying InputStream.
    public void close() throws IOException {
        input.close();
        currentByte = -1;
        numBits = 0;
    }
}
