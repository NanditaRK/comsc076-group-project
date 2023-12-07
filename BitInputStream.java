/*
 * This class reads individual bits from a file. It has a method
 * to calculate the availible bits, read the bits, and close the 
 * stream.
 * 
 * @author Citlally Chavez
 * @version 1.0
 * @since 09-14-23
 */


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;


//This class has methods that read bits from an input stream
public class BitInputStream {

	//Initialize variables
	private BufferedInputStream input;
	private int currentByte;
	private int bitsRemaining;

	/*
	 * This constructor creates a BitInputStream from a file
	 * 
	 * @param file Input file
	 * @throws IOException Throws exception in case of IO error
	 */
	public BitInputStream(File file) throws IOException {
		input = new BufferedInputStream(new FileInputStream(file));
		currentByte = 0;
		bitsRemaining = 0;
	}

	/*
	 * This method returns the number of bits available in the file
	 * 
	 * @throws IOException Throws exception in case of IO error
	 * @return Number of bits available
	 */
	public int available() throws IOException {
		
		//Calculate and return bits available
		return (8 - bitsRemaining) + 8 * (input.available());
	}

	/*
	 * This method reads the next bit from the input stream.
	 * 
	 * @throws IOException Throws exception in case of IO error
	 */
	public char readBit() throws IOException {
		
		//If there are no remaining bits in current byte, read new one
		if (bitsRemaining == 0) {
			currentByte = input.read();
			if (currentByte == -1) {
				
				//return -1 if end of file is reached
				return (char) -1;
			}
			
			//Set bitsRemaining to 8 for new byte
			bitsRemaining = 8;
		}
		
		//Read bit in current byte and minus bitsRemaining
		char bit = (char) ((currentByte >> (bitsRemaining - 1)) & 1);
		bitsRemaining--;
		
		//Return as '0' or '1'
		return bit == 0 ? '0' : '1';
	}

	/*
	 * Reads the remainder of the file and returns the bit string
	 * 
	 * @return String with read bits 
	 * @throws IOException Throws exception in case of IO error
	 */
	public String readBits() throws IOException {
		
		//Use Stringbuilder to store bit string
		StringBuilder bitString = new StringBuilder();
		char bit;
		
		//Read each bit and append to bitString
		while ((bit = readBit()) != (char) -1) {
			bitString.append(bit);
		}
		return bitString.toString();
	}

	/*
	 * Close input stream
	 * @throws IOException Throws exception in case of IO error
	 */
	public void close() throws IOException {
		input.close();
	}
}
