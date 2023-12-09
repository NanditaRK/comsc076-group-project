import java.io.*;
import java.util.*;

/**
 * This class provides functionality to compress a file using Huffman coding.
 */
public final class CompressFile {

    // Main method to compress a file using Huffman coding.
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java compressFile InputFile OutputFile");
            System.exit(1);
            return;
        }
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        SymbolFrequencyTable freqs = getFrequencies(inputFile);
        freqs.step(256);  // EOF symbol gets a frequency of 1
        HuffmanTree huffmanTree = freqs.buildHuffmanTree();
        HuffmanTreeBuilder huffmanTreeBuilder = new HuffmanTreeBuilder(huffmanTree, freqs.num_chars());

        huffmanTree = huffmanTreeBuilder.toHuffmanTree();

        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
            try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
                writeCodeLengthTable(out, huffmanTreeBuilder);
                compress(huffmanTree, in, out);
            }
        }
    }

    // Reads the frequencies of symbols from the file and constructs a FrequencyTable.
    private static SymbolFrequencyTable getFrequencies(File file) throws IOException {
        SymbolFrequencyTable freqs = new SymbolFrequencyTable(new int[257]);
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            while (true) {
                int b = input.read();
                if (b == -1)
                    break;
                freqs.step(b);
            }
        }
        return freqs;
    }

    // Writes the code length table to the output stream.
    static void writeCodeLengthTable(BitOutputStream out, HuffmanTreeBuilder huffmanTreeBuilder) throws IOException {
        for (int i = 0; i < huffmanTreeBuilder.num_chars(); i++) {
            int val = huffmanTreeBuilder.getCodeLength(i);
            if (val >= 256)
                throw new RuntimeException("Taking too long");

            for (int j = 7; j >= 0; j--)
                out.write((val >>> j) & 1);
        }
    }

    // Compresses the input stream using the provided HuffmanTree and writes the result to the output stream.
    static void compress(HuffmanTree huffmanTree, InputStream in, BitOutputStream out) throws IOException {
        while (true) {
            int b = in.read();
            if (b == -1)
                break;
            writeCode(huffmanTree, b, out);
        }
        writeCode(huffmanTree, 256, out);  // EOF
    }

    // Writes the Huffman code for the given symbol to the output stream.
    static void writeCode(HuffmanTree huffmanTree, int symbol, BitOutputStream out) throws IOException {
        List<Integer> bits = huffmanTree.getHuffmanCode(symbol);
        for (int bit : bits)
            out.write(bit);
    }
}


// This class represents a BitOutputStream for writing individual bits to an underlying OutputStream.
final class BitOutputStream implements Closeable {

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
