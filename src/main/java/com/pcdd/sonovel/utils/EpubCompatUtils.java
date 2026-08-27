package com.pcdd.sonovel.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 提升 EPUB 与 BookOrbit / WPS / 掌阅等阅读器的兼容性。
 * <p>
 * epub4j 生成的 content.opf 使用 {@code opf:} 元素前缀（符合标准），
 * 但部分阅读器的解析器不支持该写法。修复规则参考：
 * <a href="https://github.com/freeok/so-novel/issues/54">#54</a>
 * <a href="https://github.com/freeok/so-novel/discussions/199">#199</a>
 */
@UtilityClass
public class EpubCompatUtils {

    private static final String OPF_PATH = "OEBPS/content.opf";
    private static final String MIMETYPE = "mimetype";
    private static final String EPUB_MIME = "application/epub+zip";
    private static final String OPF_NS = "http://www.idpf.org/2007/opf";

    /**
     * 就地修复 EPUB：去掉 content.opf 中的 {@code opf:} 元素前缀，保留属性前缀所需命名空间。
     *
     * @return true 表示已改写；false 表示无需处理或未找到 OPF
     */
    @SneakyThrows
    public boolean fixForStrictReaders(File epubFile) {
        if (epubFile == null || !epubFile.isFile()) {
            return false;
        }

        File tempFile = FileUtil.createTempFile("sonovel-epub-", ".epub", true);
        boolean changed = false;

        try (ZipFile zipFile = new ZipFile(epubFile);
             ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempFile.toPath()))) {

            // EPUB 规范要求 mimetype 为首个未压缩条目
            writeStoredEntry(zos, MIMETYPE, EPUB_MIME.getBytes(StandardCharsets.US_ASCII));

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (MIMETYPE.equals(name)) {
                    continue;
                }

                byte[] data;
                try (InputStream in = zipFile.getInputStream(entry)) {
                    data = in.readAllBytes();
                }

                if (OPF_PATH.equals(name) || name.endsWith("/content.opf")) {
                    String original = new String(data, StandardCharsets.UTF_8);
                    String fixed = rewriteOpf(original);
                    if (!original.equals(fixed)) {
                        changed = true;
                        data = fixed.getBytes(StandardCharsets.UTF_8);
                    }
                }

                ZipEntry outEntry = new ZipEntry(name);
                zos.putNextEntry(outEntry);
                zos.write(data);
                zos.closeEntry();
            }
        }

        if (changed) {
            FileUtil.move(tempFile, epubFile, true);
        } else {
            FileUtil.del(tempFile);
        }
        return changed;
    }

    /**
     * 将 epub4j 风格的 OPF 转为无元素前缀写法，兼容严格阅读器。
     */
    public String rewriteOpf(String opfXml) {
        if (StrUtil.isBlank(opfXml) || !opfXml.contains("<opf:")) {
            return opfXml;
        }
        return opfXml
                .replace("<opf:", "<")
                .replace("</opf:", "</")
                .replace("xmlns:opf", "xmlns")
                .replace("<metadata>", "<metadata xmlns:opf=\"" + OPF_NS + "\">")
                // 历史版本曾把 guide type 写成中文「封面」，部分阅读器只认 cover
                .replace("type=\"封面\"", "type=\"cover\"")
                .replace("type='封面'", "type=\"cover\"");
    }

    private void writeStoredEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

}
