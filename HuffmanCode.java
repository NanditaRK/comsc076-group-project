import java.util.*;

/**
 * This class represents a Huffman coding tree.
 */
public final class HuffmanTree {

    public final HuffmanInternalNode root;
    private List<List<Integer>> codes;

    public HuffmanTree(HuffmanInternalNode root, int num) {
        this.root = Objects.requireNonNull(root);
        codes = new ArrayList<List<Integer>>();
        for (int i = 0; i < num; i++)
            codes.add(null);
        huff_list(root, new ArrayList<Integer>());
    }

    private void huff_list(HuffmanNode node, List<Integer> prefix) {
        if (node instanceof HuffmanInternalNode internalNode) {
            prefix.add(0);
            huff_list(internalNode.leftChild(), prefix);
            prefix.remove(prefix.size() - 1);

            prefix.add(1);
            huff_list(internalNode.rightChild(), prefix);
            prefix.remove(prefix.size() - 1);

        } else if (node instanceof HuffmanLeaf leaf) {
            codes.set(leaf.symbol(), new ArrayList<Integer>(prefix));

        } else {
            throw new AssertionError("invalid node");
        }
    }

    // Gets the Huffman code for the specified symbol.
    public List<Integer> getHuffmanCode(int symbol) {
        return codes.get(symbol);
    }
}

// This interface represents a node in the Huffman coding tree.
sealed interface HuffmanNode permits HuffmanInternalNode, HuffmanLeaf {}

// This record represents an internal node in the Huffman coding tree.
record HuffmanInternalNode(HuffmanNode leftChild, HuffmanNode rightChild) implements HuffmanNode {

    // Constructs an InternalNode with the specified left and right children.
    public HuffmanInternalNode {
        leftChild = Objects.requireNonNull(leftChild);
        rightChild = Objects.requireNonNull(rightChild);
    }

    public HuffmanNode leftChild() {
        return leftChild;
    }

    public HuffmanNode rightChild() {
        return rightChild;
    }
}

// This record represents a leaf node in the Huffman coding tree.
record HuffmanLeaf(int symbol) implements HuffmanNode {
    public int symbol() {
        return symbol;
    }
}

// This class represents a frequency table for symbols.
class SymbolFrequencyTable {

    private int[] frequencies;

    // Constructs a FrequencyTable with the given array of frequencies.
    public SymbolFrequencyTable(int[] freqs) {
        Objects.requireNonNull(freqs);
        frequencies = freqs.clone(); 
    }

    // Gets the number of unique symbols in the frequency table.
    public int num_chars() {
        return frequencies.length;
    }

    // Increments the frequency of the specified symbol.
    public void step(int symbol) {
        frequencies[symbol]++;
    }

    // Builds a HuffmanTree based on the frequencies in the frequency table.
    public HuffmanTree buildHuffmanTree() {
        Queue<node_freq> pqueue = new PriorityQueue<node_freq>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0)
                pqueue.add(new node_freq(new HuffmanLeaf(i), i, frequencies[i]));
        }

        for (int i = 0; i < frequencies.length && pqueue.size() < 2; i++) {
            if (frequencies[i] == 0)
                pqueue.add(new node_freq(new HuffmanLeaf(i), i, 0));
        }
        if (pqueue.size() < 2)
            throw new AssertionError();

        while (pqueue.size() > 1) {
            node_freq x = pqueue.remove();
            node_freq y = pqueue.remove();
            pqueue.add(new node_freq(
                    new HuffmanInternalNode(x.node, y.node),
                    Math.min(x.lowestSymbol, y.lowestSymbol),
                    x.frequency + y.frequency));
        }

        return new HuffmanTree((HuffmanInternalNode) pqueue.remove().node, frequencies.length);
    }

    // This class represents a node with its frequency for building the HuffmanTree.
    private static class node_freq implements Comparable<node_freq> {

        public final HuffmanNode node;
        public final int lowestSymbol;
        public final long frequency;

        // Constructs a node with the specified node, lowest symbol, and frequency.
        public node_freq(HuffmanNode nd, int lowSym, long freq) {
            node = nd;
            lowestSymbol = lowSym;
            frequency = freq;
        }

        // Compares this node with another based on frequency and lowest symbol.
        public int compareTo(node_freq other) {
            if (frequency < other.frequency)
                return -1;
            else if (frequency > other.frequency)
                return 1;
            else if (lowestSymbol < other.lowestSymbol)
                return -1;
            else if (lowestSymbol > other.lowestSymbol)
                return 1;
            else
                return 0;
        }
    }
}

// Creates a Huffman Tree from frequency information
class HuffmanTreeBuilder {
		
    private int[] chars;

    // Constructor for creating HuffmanTreeBuilder from an array of code lengths.
    public HuffmanTreeBuilder(int[] treelens) {
        Objects.requireNonNull(treelens);
        
        // Sort and initialize tree lengths
        chars = treelens.clone();
        Arrays.sort(chars);
        
        // Check if the tree is valid
        int currentLevel = chars[chars.length - 1];
        int numNodesAtLevel = 0;
        for (int i = chars.length - 1; i >= 0 && chars[i] > 0; i--) {
            int cl = chars[i];
            while (cl < currentLevel) {
                if (numNodesAtLevel % 2 != 0)
                    throw new IllegalArgumentException("invalid tree structure");
                numNodesAtLevel /= 2;
                currentLevel--;
            }
            numNodesAtLevel++;
        }
        
        System.arraycopy(treelens, 0, chars, 0, treelens.length);
    }
    
    // Constructor for creating HuffmanTreeBuilder from an existing HuffmanTree.
    public HuffmanTreeBuilder(HuffmanTree tree, int symbolLimit) {
        Objects.requireNonNull(tree);
        
        // Validate input parameters
        if (symbolLimit < 2)
            throw new IllegalArgumentException("At least 2 symbols needed");
        
        chars = new int[symbolLimit];
        update_tree(tree.root, 0);
    }
    
    // Recursively update the HuffmanTree.
    private void update_tree(HuffmanNode node, int depth) {
        if (node instanceof HuffmanInternalNode internalNode) {
            update_tree(internalNode.leftChild (), depth + 1);
            update_tree(internalNode.rightChild(), depth + 1);
        } else if (node instanceof HuffmanLeaf leaf) {
            // Handle leaf node (symbol)
            int symbol = leaf.symbol();
            if (symbol >= chars.length)
                throw new IllegalArgumentException("Symbol exceeds symbol limit");
            if (chars[symbol] != 0)
                throw new AssertionError("Symbol has more than one code");
            chars[symbol] = depth;
        } else {
            throw new AssertionError("Illegal node type");
        }
    }
    
    // Get the symbol limit of the HuffmanTreeBuilder.
    public int num_chars() {
        return chars.length;
    }
    
    // Get the code length of a specific symbol.
    public int getCodeLength(int symbol) {
        if (symbol < 0 || symbol >= chars.length)
            throw new IllegalArgumentException("Symbol out of range");
        return chars[symbol];
    }
    
    // Convert HuffmanTreeBuilder to a HuffmanTree.
    public HuffmanTree toHuffmanTree() {
        List<HuffmanNode> nodes = new ArrayList<HuffmanNode>();
        
        // Iterate through code lengths to build HuffmanTree
        for (int i = max(chars); i >= 0; i--) {  
            if (nodes.size() % 2 != 0)
                throw new AssertionError("invalid tree structure");
            
            List<HuffmanNode> newNodes = new ArrayList<HuffmanNode>();
            
            // Add leaf nodes
            if (i > 0) {
                for (int j = 0; j < chars.length; j++) {
                    if (chars[j] == i)
                        newNodes.add(new HuffmanLeaf(j));
                }
            }
            
            // Add internal nodes
            for (int j = 0; j < nodes.size(); j += 2)
                newNodes.add(new HuffmanInternalNode(nodes.get(j), nodes.get(j + 1)));
            
            nodes = newNodes;
        }
        
        return new HuffmanTree((HuffmanInternalNode)nodes.get(0), chars.length);
    }
    
    // Find the maximum value in an array.
    private static int max(int[] array) {
        int max = Arrays.stream(array).max().orElseThrow();
        return max;
    }
}