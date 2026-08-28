package com.pcdd.sonovel.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class EpubCompatUtilsTest {

    @Test
    @DisplayName("rewriteOpf 去掉元素前缀并保留属性命名空间")
    void rewriteOpf() {
        String input = """
                <?xml version='1.0' encoding='UTF-8'?>
                <opf:package version="2.0" unique-identifier="BookId" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <opf:metadata>
                    <dc:identifier id="BookId" opf:scheme="UUID">abc</dc:identifier>
                    <dc:creator opf:role="aut">作者</dc:creator>
                    <opf:meta name="cover" content="image_1" />
                  </opf:metadata>
                  <opf:manifest>
                    <opf:item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml" />
                  </opf:manifest>
                  <opf:spine toc="ncx">
                    <opf:itemref idref="item_1" />
                  </opf:spine>
                </opf:package>
                """;

        String fixed = EpubCompatUtils.rewriteOpf(input);

        assertFalse(fixed.contains("<opf:"));
        assertFalse(fixed.contains("</opf:"));
        assertTrue(fixed.contains("<package "));
        assertTrue(fixed.contains("xmlns=\"http://www.idpf.org/2007/opf\""));
        assertTrue(fixed.contains("<metadata xmlns:opf=\"http://www.idpf.org/2007/opf\">"));
        assertTrue(fixed.contains("opf:scheme=\"UUID\""));
        assertTrue(fixed.contains("opf:role=\"aut\""));
        assertTrue(fixed.contains("<meta name=\"cover\""));
    }

    @Test
    @DisplayName("fixForStrictReaders 改写 EPUB 内 content.opf")
    void fixForStrictReaders() throws Exception {
        File epub = Files.createTempFile("compat-", ".epub").toFile();
        epub.deleteOnExit();

        String opf = """
                <?xml version='1.0' encoding='UTF-8'?>
                <opf:package version="2.0" unique-identifier="BookId" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <opf:metadata>
                    <dc:title>测试</dc:title>
                    <dc:identifier id="BookId" opf:scheme="UUID">id-1</dc:identifier>
                  </opf:metadata>
                  <opf:manifest>
                    <opf:item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml" />
                  </opf:manifest>
                  <opf:spine toc="ncx"/>
                </opf:package>
                """;

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(epub.toPath()))) {
            byte[] mime = "application/epub+zip".getBytes(StandardCharsets.US_ASCII);
            ZipEntry mimeEntry = new ZipEntry("mimetype");
            mimeEntry.setMethod(ZipEntry.STORED);
            mimeEntry.setSize(mime.length);
            mimeEntry.setCompressedSize(mime.length);
            CRC32 crc = new CRC32();
            crc.update(mime);
            mimeEntry.setCrc(crc.getValue());
            zos.putNextEntry(mimeEntry);
            zos.write(mime);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("META-INF/container.xml"));
            zos.write("""
                    <?xml version="1.0"?>
                    <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                      <rootfiles>
                        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                      </rootfiles>
                    </container>
                    """.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("OEBPS/content.opf"));
            zos.write(opf.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        assertTrue(EpubCompatUtils.fixForStrictReaders(epub));
        assertFalse(EpubCompatUtils.fixForStrictReaders(epub));

        try (ZipFile zip = new ZipFile(epub)) {
            String fixedOpf = new String(zip.getInputStream(zip.getEntry("OEBPS/content.opf")).readAllBytes(), StandardCharsets.UTF_8);
            assertFalse(fixedOpf.contains("<opf:"));
            assertTrue(fixedOpf.contains("<package "));
            assertTrue(fixedOpf.contains("opf:scheme=\"UUID\""));

            var entries = zip.entries();
            assertEquals("mimetype", entries.nextElement().getName());
        }
    }

}
