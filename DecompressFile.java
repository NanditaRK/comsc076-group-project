import java.io.*;
import java.util.*;

/**
 * This class provides functionality to decompress a file using Huffman coding.
 */
public final class DecompressFile {

    //allows user to ruun file from command line
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java DecompressFile InputFile OutputFile");
            System.exit(1);
            return;
        }
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        //decompresses file by reading the compressed file and converting using huffman tree
        try (BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                HuffmanTreeBuilder huffmanTreeBuilder = readCodeLengthTable(in);
                HuffmanTree huffmanTree = huffmanTreeBuilder.toHuffmanTree();
                decompress(huffmanTree, in, out);
            }
        }
    }

    //reads the code length table from the input stream and constructs a HuffmanTreeBuilder
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

    //decompresses the input stream using the provided HuffmanTree and writes the result to the output stream
    static void decompress(HuffmanTree huffmanTree, BitInputStream in, OutputStream out) throws IOException {
        HuffmanInternalNode currentNode = (HuffmanInternalNode) huffmanTree.root;
        while (true) {
            int temp = in.hasnext();
            HuffmanNode nextNode;
            if (temp == 0) nextNode = currentNode.leftChild();
            else if (temp == 1) nextNode = currentNode.rightChild();
            else throw new RuntimeException("Invalid value from hasnext()");

            if (nextNode instanceof HuffmanLeaf leaf) {
                int symbol = leaf.symbol();
                if (symbol == 256)
                    break;
                out.write(symbol);
                //reset to the root for the next symbol
                currentNode = (HuffmanInternalNode) huffmanTree.root;
            } else if (nextNode instanceof HuffmanInternalNode internalNode) {
                currentNode = internalNode;
            } else {
                throw new RuntimeException("invalid node type");
            }
        }
    }
}

