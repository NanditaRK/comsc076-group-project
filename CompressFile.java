import java.io.*;
import java.util.*;

/**
 * This class provides functionality to compress a file using Huffman coding.
 */
public final class CompressFile {

    //allows user to run program from command line
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java CompressFile InputFile OutputFile");
            System.exit(1);
            return;
        }
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        SymbolFrequencyTable freqs = getFrequencies(inputFile);

        //frequency of 1 for end of file symbol

        freqs.step(256);

        //instantiates huffman tree
        HuffmanTree huffmanTree = freqs.buildHuffmanTree();

        //creates huffman tree with frequencies
        HuffmanTreeBuilder huffmanTreeBuilder = new HuffmanTreeBuilder(huffmanTree, freqs.num_chars());

        
        huffmanTree = huffmanTreeBuilder.toHuffmanTree();

        //compressses the input file and writes to the output file
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
            try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
                writeCodeLengthTable(out, huffmanTreeBuilder);
                compress(huffmanTree, in, out);
            }
        }
    }

    //reads the frequencies of symbols from the file and constructs a FrequencyTable which is just 
    public static SymbolFrequencyTable getFrequencies(File file) throws IOException {
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

    //writes the code length table to the output stream.
    public static void writeCodeLengthTable(BitOutputStream out, HuffmanTreeBuilder huffmanTreeBuilder) throws IOException {
        for (int i = 0; i < huffmanTreeBuilder.num_chars(); i++) {
            int val = huffmanTreeBuilder.getCodeLength(i);
            if (val >= 256)
                throw new RuntimeException("Taking too long");

            for (int j = 7; j >= 0; j--)
                out.write((val >>> j) & 1);
        }
    }

    //compresses the input stream using the provided HuffmanTree and writes the result to the output stream
    public static void compress(HuffmanTree huffmanTree, InputStream in, BitOutputStream out) throws IOException {
        while (true) {
            int b = in.read();
            if (b == -1)
                break;
            writeCode(huffmanTree, b, out);
        }
        //adds end of file
        writeCode(huffmanTree, 256, out);
    }

    //writes the output to the file using the tree
    public static void writeCode(HuffmanTree huffmanTree, int symbol, BitOutputStream out) throws IOException {
        List<Integer> bits = huffmanTree.getHuffmanCode(symbol);
        for (int bit : bits)
            out.write(bit);
    }
}


