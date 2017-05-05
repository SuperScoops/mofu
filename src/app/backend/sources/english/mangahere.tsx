import MangaSource from '../manga-source'
import get from '../../utils/accessor'
import Dictionary from '../../utils/dictionary'
import SearchResult from '../../search/search-result'
import MangaInfo from '../../abstracts/manga-info'
import MangaChapter from '../../abstracts/manga-chapter'

const cheerio: CheerioAPI = require('cheerio')

export class MangaHere extends MangaSource {

    searchPre = "http://www.mangahere.co/search.php?name_method=cw&author_method=cw&artist_method=cw&advopts=1&name="

    async getInfo(url: string): Promise<MangaInfo> {
        let s: string = await get(url, function (s) {
            return s.indexOf('manga_detail_top') > -1;
        });
        let $ = cheerio.load(s)
        let image = cheerio('.manga_detail_top', s).first().children('img.img').first().attr("src")
        let title = $('meta[property="og:title"]').attr('content')
        // let title = $('.detail_topText').first().text()
        // let authors = $("a[href=http://www.mangahere.co/author/]").first().text();
        let authors = $('.manga_detail_top').find("a").filter(function (index, element) {
            return $(element).attr("href").indexOf("/author/") > -1
        }).first().text();
        let artists = $('.manga_detail_top').find("a").filter(function (index, element) {
            return $(element).attr("href").indexOf("/artist/") > -1
        }).first().text();
        let desc = $('.manga_detail_top').find('#show').first().text()
        let source = "MangaHere"
        let chapters: MangaChapter[] = []
        $('.detail_list').find("a").each(function (index, element) {
            let href = $(element).attr("href")
            if (href.indexOf("mangahere") > -1) {
                let next: MangaChapter = {
                    name: $(element).text().trim(),
                    url: href,
                    originalURL: url,
                    source: this
                }
                chapters.push(next)
            }
        })
        for (let k = 1; k < chapters.length; k++) {
            chapters[k].prevChapter = chapters[k - 1]
            if (k < chapters.length - 1) {
                chapters[k].nextChapter = chapters[k + 1]
            }
        }
        let chapterCount = chapters.length;
        return {
            image: image,
            title: title,
            author: authors,
            description: desc,
            sourceName: source,
            source: this,
            chapterCount: chapterCount,
            chapters: chapters,
            artist: artists
        };
    }

    async loadChapter(chapter: MangaChapter): Promise<MangaChapter> {
        return undefined
    }

    async search(phrase: string) {
        let url = this.searchPre + phrase;
        let s: string = await get(url, function (s) {
            return s.indexOf("result_search") >= 0
        });
        let $ = cheerio.load(s);
        let mainLinks = $('.result_search > dl > dt > a.name_one')
        let res: SearchResult[] = []
        mainLinks.toArray().forEach(link => {
            let names: string[] = []
            let alt = $(link.parentNode.parentNode).children('dd')
            let altText = alt.text()
            altText = altText.substr(altText.indexOf(':') + 1).trim()
            names.push($(link).attr('rel'));
            altText.split(";").forEach(element => {
                element = element.trim()
                if (element.length > 0 && element.indexOf("...") == -1) {
                    names.push(element)
                } else if (element.indexOf("...") > -1) {
                    element.split("...").forEach(e => {
                        if (e.length > 0)
                            names.push(e.trim())
                    });
                }
            });
            let href: string = $(link).attr("href")
            res.push({
                title: names[0],
                altNames: names.length > 1 ? names.slice(1) : undefined,
                url: href
            })
        });
        return res
    }
}

let instance = new MangaHere()
export default instance