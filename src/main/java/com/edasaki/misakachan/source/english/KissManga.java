package com.edasaki.misakachan.source.english;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.edasaki.misakachan.manga.Chapter;
import com.edasaki.misakachan.manga.Series;
import com.edasaki.misakachan.source.AbstractSource;
import com.edasaki.misakachan.source.SearchAction;
import com.edasaki.misakachan.utils.MCacheUtils;
import com.edasaki.misakachan.utils.logging.M;

public class KissManga extends AbstractSource {

    @Override
    public boolean match(String url) {
        return url.contains("kissmanga.com");
    }

    @Override
    public boolean matchInfo(String url) {
        return url.contains("kissmanga.com/Manga/");
    }

    @Override
    public Series getSeries(String url) {
        Document doc = MCacheUtils.getDocument(url);
        if (doc == null)
            return null;
        Series series = new Series();
        series.source = this.getSourceName();
        Element detail = doc.select(".detail_topText").first();
        series.imageURL = doc.select(".manga_detail_top").first().select("img.img").first().absUrl("src");
        series.title = doc.select("meta[property=og:title").first().attr("content");
        series.description = detail.select("#show").first().ownText();
        series.authors = selectFirst(detail, "a[href^=http://www.mangahere.co/author/]");
        series.artists = selectFirst(detail, "a[href^=http://www.mangahere.co/artist/]");
        Elements labels = detail.select("label");
        for (Element e : labels) {
            if (e.text().contains("Genre")) {
                series.genres = e.parent().ownText();
            } else if (e.text().contains("Alternative")) {
                series.altNames = e.parent().ownText();
            }
        }
        Elements chapters = doc.select(".detail_list > ul:not([class]) > li");
        for (Element chapter : chapters) {
            String cURL = chapter.select("a").first().absUrl("href");
            String cName = chapter.select("a").first().text();
            series.addChapter(cName, cURL);
        }
        return series;
    }

    @Override
    public Chapter getChapterFromSite(String url) {
        return null;
    }

    @Override
    public String getSourceName() {
        return "MangaHere";
    }

    private static long lastSearch = 0;

    @Override
    public SearchAction getSearch() {
        return (searchTerm) -> {
            if (System.currentTimeMillis() - lastSearch < 5000) {
                try {
                    long sleep = 5000 - (System.currentTimeMillis() - lastSearch) + 100;
                    M.debug("sleeping " + sleep);
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String prefix = "http://www.mangahere.co/search.php?name_method=cw&author_method=cw&artist_method=cw&advopts=1&name=";
            String url = prefix + searchTerm;
            Document doc = MCacheUtils.getDocument(url);
            if (doc == null)
                return null;
            // update last search time after site has been accessed
            lastSearch = System.currentTimeMillis();
            Elements entries = doc.select(".result_search > dl");
            Elements mainLinks = entries.select("dl > dt > a.name_one");
            Map<Element, List<String>> linkMap = new HashMap<Element, List<String>>();
            for (Element link : mainLinks) {
                Elements alt = link.parent().parent().select("dd");
                String altText = alt.text();
                altText = altText.substring(altText.indexOf(':') + 1).trim();
                List<String> associatedNames = new ArrayList<String>();
                associatedNames.add(link.attr("rel"));
                if (altText.contains("...")) {
                    Document detailed = MCacheUtils.getDocument(link.absUrl("href"));
                    Elements altNameLabel = detailed.select("label:contains(Alternative Name)");
                    for (Element altNameLabelEle : altNameLabel) {
                        try {
                            altText = altNameLabelEle.parent().text();
                            altText = altText.substring(altText.indexOf(':') + 1);
                            for (String s : altText.split(";")) {
                                s = s.trim();
                                if (s.length() > 0)
                                    associatedNames.add(s);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (String s : altText.split(";")) {
                        s = s.trim();
                        if (s.length() > 0)
                            associatedNames.add(s);
                    }
                }
                linkMap.put(link, associatedNames);
            }
            return createResultSet(linkMap);
        };
    }

    @Override
    public String getImageURL(Document doc) {
        return doc.select(".manga_detail_top").first().select("img.img").first().absUrl("src");
    }

}