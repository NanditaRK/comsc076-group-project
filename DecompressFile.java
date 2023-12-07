import java.io.*;
import java.util.Map;

public class Decompressfile {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("type java DecompressFile compressedfile.txt decompressedFile.txt");
			return;
		}
		String compressedFileName = args[0];
		String decompressedFileName = args[1];

		try {
			// Read Huffman codes from compressed file
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(compressedFileName));
			Map<Character, String> codes = (Map<Character, String>) objectInputStream.readObject();
			objectInputStream.close();

			// Build Huffman Tree from the codes
			HuffmanCode.Tree huffmanTree = HuffmanCode.getHuffmanTree(codes);

			// Decompress the file using huffman tree
			BitInputStream bitInputStream = new BitInputStream(new File(compressedFileName));
			String decompressedText = decompressFile(bitInputStream, huffmanTree);
			bitInputStream.close();

			// Write decompressed text to new file
			writeFile(decompressedFileName, decompressedText);

			System.out.println("done");

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Something went wrong:(");
			e.printStackTrace();
		}

	}

	private static String decompressFile(BitInputStream bitInputStream, HuffmanCode.Tree huffmanTree)
			throws IOException {
		StringBuilder decompressedText = new StringBuilder();

		HuffmanCode.Tree.Node current = huffmanTree.root;

		while (bitInputStream.hasNextBit()) {

			char bit = bitInputStream.readBit();
			if (bit == '0') {
				current = current.left;
			} else {
				current = current.right;
			}

			if (current.left == null && current.right == null) {
				// Leaf node, add character to decompressed text
				decompressedText.append(current.element);
				current = huffmanTree.root;
			}
		}
		return decompressedText.toString();
	}

	private static void writeFile(String fileName, String content) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
			writer.print(content);
		}
	}

}
