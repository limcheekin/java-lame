/** 
 * PCM to MP3 conversion using https://github.com/nwaldispuehl/java-lame
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;

import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;

public class PcmToMp3 {
	// REF: https://introcs.cs.princeton.edu/java/stdlib/StdAudio.java.html
	public static final float SAMPLE_RATE = 8000;
	private static final int BITS_PER_SAMPLE = 16;

	private static final int MONO = 1;
	private static final boolean LITTLE_ENDIAN = false;
	private static final boolean SIGNED = true;

	public static void main(String[] args) throws Exception {
		writeBytesToFile(new File("audio.mp3"), encodePcmToMp3(readBytesFromFile(new File("audio.raw"))));
	}

	// REF:
	// http://www.java2s.com/Code/Java/File-Input-Output/Readfiletobytearrayandsavebytearraytofile.htm
	/**
	 * Read bytes from a File into a byte[].
	 * 
	 * @param file The File to read.
	 * @return A byte[] containing the contents of the File.
	 * @throws IOException Thrown if the File is too long to read or couldn't be
	 *                     read fully.
	 */
	public static byte[] readBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length
					+ " bytes, max supported " + Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	/**
	 * Writes the specified byte[] to the specified File path.
	 * 
	 * @param theFile File Object representing the path to write to.
	 * @param bytes   The byte[] of data to write to the File.
	 * @throws IOException Thrown if there is problem creating or writing the File.
	 */
	public static void writeBytesToFile(File theFile, byte[] bytes) throws IOException {
		BufferedOutputStream bos = null;

		try {
			FileOutputStream fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos);
			bos.write(bytes);
		} finally {
			if (bos != null) {
				try {
					// flush and close the BufferedOutputStream
					bos.flush();
					bos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static byte[] encodePcmToMp3(byte[] pcm) {
		AudioFormat inputFormat = new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, MONO, SIGNED, LITTLE_ENDIAN);
		LameEncoder encoder = new LameEncoder(inputFormat, BITS_PER_SAMPLE, MPEGMode.STEREO, Lame.EXTREME_FAST, false);

		ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
		byte[] buffer = new byte[encoder.getPCMBufferSize()];

		int bytesToTransfer = Math.min(buffer.length, pcm.length);
		int bytesWritten;
		int currentPcmPosition = 0;
		while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
			currentPcmPosition += bytesToTransfer;
			bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);

			mp3.write(buffer, 0, bytesWritten);
		}

		encoder.close();
		return mp3.toByteArray();
	}
}
