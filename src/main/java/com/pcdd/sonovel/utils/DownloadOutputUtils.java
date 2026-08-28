package com.pcdd.sonovel.utils;

import com.pcdd.sonovel.model.Rule.Book;
import lombok.experimental.UtilityClass;

/**
 * 根据书籍信息与导出格式，推断合并后的输出文件名（位于 download-path 根目录）。
 */
@UtilityClass
public class DownloadOutputUtils {

    public String resolveOutputFileName(Book book, String extName, String bookDir) {
        if (book == null || extName == null) {
            return null;
        }
        return switch (extName.toLowerCase()) {
            case "epub" -> "%s(%s).epub".formatted(book.getBookName(), book.getAuthor());
            case "txt" -> "%s(%s).txt".formatted(book.getBookName(), book.getAuthor());
            case "pdf" -> "%s(%s).pdf".formatted(book.getBookName(), book.getAuthor());
            case "html" -> bookDir + ".zip";
            default -> null;
        };
    }

}
