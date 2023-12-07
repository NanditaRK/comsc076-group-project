import java.io.*;
import java.util.Map;

public class CompressFile {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("type java CompressFile sourceFile.txt compressedFile.txt");
            return;
        }

        String sourceFileName = args[0];
        String compressedFileName = args[1];

        try {
            //read text from the source file
                String text = readFile(sourceFileName);

            
            //aharacter frequencies
                int[] counts = HuffmanCode.getCharacterFrequency(text);

            //buuild Huffman Tree
        HuffmanCode.Tree huffmanTree = HuffmanCode.getHuffmanTree(counts);

            //get the tree
            HuffmanCode.Tree.Node root = huffmanTree.root;

            
            HuffmanCode.Tree.Codes codeTable = new HuffmanCode.Tree.Codes(root);
            Map<Character, String> codes = codeTable.getCodeTable();

            //write codes
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(compressedFileName));

            
            objectOutputStream.writeObject(codes);
            
            objectOutputStream.close();

            //compress and send
            BitOutputStream bitOutputStream = new BitOutputStream(new File(compressedFileName));
            compressFile(text, codes, bitOutputStream);
           
            bitOutputStream.close();

            System.out.println("done.");

        } catch (IOException e) {
            System.out.println("Something went wrong.");
        }
    }

    private static String readFile(String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            
            int fileSize = fis.available();
            byte[] buffer = new byte[fileSize];
            fis.read(buffer);
            return new String(buffer);
        }
    }

    private static void compressFile(String text, Map<Character, String> codes, BitOutputStream bitOutputStream) throws IOException {
        for (char character : text.toCharArray()) {
            String code = codes.get(character);
            writeCodeToStream(code, bitOutputStream);
        }
    }

    private static void writeCodeToStream(String code, BitOutputStream bitOutputStream) throws IOException {
        for (char bit : code.toCharArray()) {
            bitOutputStream.writeBit(bit);
        }
    }
}
