package net.sf.jooreports.opendocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import net.sf.jooreports.opendocument.OpenDocumentArchive;
import net.sf.jooreports.opendocument.OpenDocumentIO;

import org.apache.commons.io.IOUtils;

public class OpenDocumentIOTest extends TestCase {

	public void testReadZip() throws IOException {
		InputStream input = new FileInputStream("src/test/resources/empty.odt");
		OpenDocumentArchive archive = OpenDocumentIO.readZip(input);
		
		assertNotNull("null archive", archive);
		Set entryNames = archive.getEntryNames();
		assertFalse("no entries", entryNames.isEmpty());
		
		String mimetypeString = IOUtils.toString(archive.getEntryReader(OpenDocumentArchive.ENTRY_MIMETYPE));
		assertEquals("application/vnd.oasis.opendocument.text", mimetypeString);
	}

	public void testReadDirectory() throws IOException {
		File directory = new File("src/test/resources/empty");
		OpenDocumentArchive documentData = OpenDocumentIO.readDirectory(directory);
		
		assertNotNull("null data", documentData);
		Set entryNames = documentData.getEntryNames();
		assertFalse("no entries", entryNames.isEmpty());

		String mimetypeString = IOUtils.toString(documentData.getEntryReader(OpenDocumentArchive.ENTRY_MIMETYPE));
		assertEquals("application/vnd.oasis.opendocument.text", mimetypeString);
	}

	public void testReadDirectoryNotFound() throws IOException {
		File directory = new File("no-such-directory");
		try {
			OpenDocumentIO.readDirectory(directory);
			fail("should have thrown exception");
		} catch (IllegalArgumentException illegalArgumentException) {
			// expected
		}
	}

	public void testWriteZip() throws IOException {
		OpenDocumentArchive archive = new OpenDocumentArchive();
		
		Writer mimetypeWriter = archive.getEntryWriter(OpenDocumentArchive.ENTRY_MIMETYPE);
		mimetypeWriter.write("application/x-test");
		mimetypeWriter.close();
		
		Writer contentWriter = archive.getEntryWriter(OpenDocumentArchive.ENTRY_CONTENT);
		contentWriter.write("test content");
		contentWriter.close();
		
		File documentFile = File.createTempFile("document", ".zip");
		OpenDocumentIO.writeZip(archive, new FileOutputStream(documentFile));
		assertTrue("file not created", documentFile.exists() && documentFile.length() > 0);
		
		ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(documentFile));
		ZipEntry firstEntry = zipInputStream.getNextEntry();
		assertNotNull("zip file has no entries", firstEntry);
		assertEquals("first entry not 'mimetype' as required by OpenDocument", OpenDocumentArchive.ENTRY_MIMETYPE, firstEntry.getName());
		assertEquals("first entry compressed", ZipEntry.STORED, firstEntry.getMethod());

		// Added by NetForce1 to verify crc-correctness
		String mimetype = IOUtils.toString(zipInputStream);
		assertEquals("mimetype content", "application/x-test", mimetype);
		
		zipInputStream.close();
		
		documentFile.delete();
	}
}
