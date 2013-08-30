package cs.man.ac.uk.tavernamobile.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class WorkflowFileLoader {

	public byte[] loadResource(String path) {
		InputStream is = null;

		try {
			is = getResourceStream(path);
			return IOUtils.toByteArray(is);
		} catch (Exception e) {
			try {
				throw new IOException("Could not open resource at: " + path);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			IOUtils.closeQuietly(is);
		}

		return null;
	}

	private InputStream getResourceStream(String filename) {
		InputStream is = getClass().getResourceAsStream(filename);

		if (is == null) {
			try {
				throw new IOException("Could not open resource: " + filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return is;
	}

	// Returns the contents of the file in a byte array.
	public static byte[] getBytesFromFile(File file) throws Exception 
	{
		InputStream inputStream = new FileInputStream(file);

		// Get the size of the file
		long fileSize = file.length();
		// check whether the file is larger than Integer.MAX_VALUE.
		if (fileSize > Integer.MAX_VALUE) {
			inputStream.close();
			throw new Exception("The file: " + file.getName() + " is too large.");
		}
		
		byte[] bytes = new byte[(int)fileSize];
		// Read in the bytes
		int offset = 0; 
		int numOfBytesRead = 0;
		while (offset < bytes.length && numOfBytesRead >= 0) 
		{
			numOfBytesRead = inputStream.read(bytes, offset, bytes.length - offset);
			offset += numOfBytesRead;
		}

		// Check whether all bytes have been read
		if (offset < bytes.length) {
			inputStream.close();
			throw new IOException("Could not complete reading file: " + file.getName());
		}
		
		inputStream.close();
		return bytes;
	}
}
