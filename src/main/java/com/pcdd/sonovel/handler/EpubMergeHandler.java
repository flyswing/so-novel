package com.pcdd.sonovel.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.model.Rule.Book;
import com.pcdd.sonovel.utils.EpubCompatUtils;
import com.pcdd.sonovel.utils.FileUtils;
import io.documentnode.epub4j.domain.*;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
public class EpubMergeHandler implements PostProcessingHandler {

    public static final String COVER_NAME = "cover.html";
    public static final String COVER_ID = "cover";
    public static final String COVER_IMAGE_ID = "cover-image";

    @SneakyThrows
    @Override
    public void handle(Book b, File saveDir) {
        if (FileUtil.isDirEmpty(saveDir)) {
            Console.error(render("<== 《{}》（{}）下载章节数为 0，取消生成 EPUB", "red"), b.getBookName(), b.getAuthor());
            return;
        }

        io.documentnode.epub4j.domain.Book book = new io.documentnode.epub4j.domain.Book();
        // content.opf > metadata
        Metadata meta = book.getMetadata();
        meta.addTitle(b.getBookName());
        meta.setAuthors(List.of(new Author(b.getAuthor())));
        meta.addDescription(b.getIntro());
        Resource coverPage = new Resource(COVER_ID, ResourceUtil.readBytes("templates/chapter_cover.html"), COVER_NAME, MediaTypes.XHTML);
        // 下载封面失败会导致生成 epub 中断
        try {
            byte[] bytes = HttpUtil.downloadBytes(b.getCoverUrl());
            book.setCoverImage(new Resource(COVER_IMAGE_ID, bytes, "cover.jpg", MediaTypes.JPG));
            // 添加封面页（必须设置 id，否则 spine 的 idref 为 null 会导致生成失败）
            book.addSection("封面", coverPage);
        } catch (Exception e) {
            Console.error(render("EPUB 最新封面 {} 下载失败：{}", "red"), b.getCoverUrl(), e.getMessage());
        }
        // 不设置会导致 Apple Books 无法使用苹方字体
        meta.setLanguage("zh");
        meta.setDates(List.of(new io.documentnode.epub4j.domain.Date(new Date())));
        meta.addPublisher("so-novel");
        meta.setRights(List.of("本电子书由 so-novel(https://github.com/freeok/so-novel) 制作生成。仅供交流使用，不得用于商业用途。"));

        // content.opf > manifest
        List<File> files = FileUtils.sortFilesByName(saveDir);
        int len = String.valueOf(files.size()).length();
        // 添加正文页
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            // 截取第一个 _ 后的字符串，即章节名
            String title = StrUtil.subAfter(FileUtil.mainName(file), "_", false);
            String id = StrUtil.padPre(String.valueOf(i + 1), len, '0');
            book.addSection(title, new Resource(id, FileUtil.readBytes(file), id + ".html", MediaTypes.XHTML));
        }

        // 设置 guide，用于指定封面（type 必须为 cover，中文值会导致部分阅读器无法识别）
        book.getGuide().addReference(new GuideReference(coverPage, GuideReference.COVER, "封面"));
        File epubFile = new File(StrUtil.format("{}/{}({}).epub", saveDir.getParent(), b.getBookName(), b.getAuthor()));
        EpubWriter epubWriter = new EpubWriter();
        try (FileOutputStream out = new FileOutputStream(epubFile)) {
            epubWriter.write(book, out);
        }
        // 去掉 content.opf 的 opf: 元素前缀，兼容 BookOrbit / WPS / 掌阅等阅读器
        EpubCompatUtils.fixForStrictReaders(epubFile);
    }

}