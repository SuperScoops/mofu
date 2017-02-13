package com.edasaki.misakachan.manga;

import java.util.ArrayList;
import java.util.List;

import com.edasaki.misakachan.source.AbstractSource;

public class Chapter {
    private final String chapterName, mangaTitle;
    private final int pageCount, chapterNumber;
    private final List<MangaPage> pages;
    public AbstractSource source;

    public Chapter(String mangaTitle, String chapterTitle, int chapterNumber, List<MangaPage> pages) {
        this.mangaTitle = mangaTitle;
        this.chapterName = chapterTitle;
        this.chapterNumber = chapterNumber;
        this.pageCount = pages.size();
        this.pages = new ArrayList<MangaPage>();
        this.pages.addAll(pages);
    }

    public String getChapterName() {
        return this.chapterName;
    }

    public int getPageCount() {
        return this.pageCount;
    }

    public List<MangaPage> getPages() {
        return pages;
    }

    public String getMangaTitle() {
        return this.mangaTitle;
    }

    public int getChapterNumber() {
        return this.chapterNumber;
    }

}
