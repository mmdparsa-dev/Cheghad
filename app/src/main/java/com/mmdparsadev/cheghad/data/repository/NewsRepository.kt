package com.mmdparsadev.cheghad.data.repository

import android.util.Xml
import com.mmdparsadev.cheghad.data.models.NewsAgency
import com.mmdparsadev.cheghad.data.models.NewsArticle
import com.mmdparsadev.cheghad.data.models.NewsCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.concurrent.TimeUnit

class NewsRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    companion object {
        val AGENCIES = listOf(
            NewsAgency("fars", "خبرگزاری فارس", "Fars News", "https://www.farsnews.ir/rss/economy", "https://www.farsnews.ir", 0xFF16A085),
            NewsAgency("irna", "خبرگزاری ایرنا", "IRNA", "https://www.irna.ir/rss/tp/20", "https://www.irna.ir", 0xFF2A5298),
            NewsAgency("yjc", "باشگاه خبرنگاران جوان", "YJC", "https://www.yjc.ir/fa/rss/3", "https://www.yjc.ir", 0xFFE67E22),
            NewsAgency("isna", "خبرگزاری ایسنا", "ISNA", "https://www.isna.ir/rss/tp/14", "https://www.isna.ir", 0xFF0072BC),
            NewsAgency("tasnim", "خبرگزاری تسنیم", "Tasnim", "https://www.tasnimnews.ir/fa/rss/feed/0/7/0/", "https://www.tasnimnews.ir", 0xFF0088CC),
            NewsAgency("bbc", "بی‌بی‌سی فارسی", "BBC Persian", "https://feeds.bbci.co.uk/persian/rss.xml", "https://www.bbc.com/persian", 0xFFBB1919),
            NewsAgency("mehr", "خبرگزاری مهر", "Mehr News", "https://www.mehrnews.com/rss/tp/25", "https://www.mehrnews.com", 0xFFC0392B),
            NewsAgency("ilna", "خبرگزاری ایلنا", "ILNA", "https://www.ilna.ir/fa/rss/3", "https://www.ilna.ir", 0xFF8E44AD),
            NewsAgency("moj", "خبرگزاری موج", "Moj News", "https://www.mojnews.com/fa/rss/allnews", "https://www.mojnews.com", 0xFF2980B9),
            NewsAgency("tabnak", "تابناک", "Tabnak", "https://www.tabnak.ir/fa/rss/allnews", "https://www.tabnak.ir", 0xFFD35400),
            NewsAgency("khabaronline", "خبرآنلاین", "Khabar Online", "https://www.khabaronline.ir/rss/tp/11", "https://www.khabaronline.ir", 0xFF27AE60),
            NewsAgency("borna", "خبرگزاری برنا", "Borna News", "https://www.borna.news/fa/rss/allnews", "https://www.borna.news", 0xFFF39C12),
            NewsAgency("ana", "خبرگزاری آنا", "ANA News", "https://ana.ir/fa/rss/allnews", "https://ana.ir", 0xFF16A085),
            NewsAgency("alef", "سایت خبری الف", "Alef", "https://www.alef.ir/rss/allnews", "https://www.alef.ir", 0xFF34495E),
            NewsAgency("irib", "خبرگزاری صداوسیما", "IRIB News", "https://www.iribnews.ir/fa/rss/allnews", "https://www.iribnews.ir", 0xFF005A9C),
            NewsAgency("sputnik", "اسپوتنیک فارسی", "Sputnik", "https://spnfa.ir/export/rss2/archive/index.xml", "https://spnfa.ir", 0xFFE74C3C),
            NewsAgency("independent", "ایندیپندنت فارسی", "Independent Persian", "https://www.independentpersian.com/rss.xml", "https://www.independentpersian.com", 0xFF2C3E50),
            NewsAgency("voa", "صدای آمریکا", "VOA Persian", "https://ir.voanews.com/api/z\$g_pve-v_", "https://ir.voanews.com", 0xFF1A5276),
            NewsAgency("donya", "دنیای اقتصاد", "Donya-e-Eqtesad", "https://donya-e-eqtesad.com/rss/all", "https://donya-e-eqtesad.com", 0xFF00A859),
            NewsAgency("tgju", "شبکه طلا و ارز", "TGJU", "https://www.tgju.org/news/rss", "https://www.tgju.org", 0xFFD4AF37)
        )
    }

    suspend fun fetchLiveNews(): List<NewsArticle> = withContext(Dispatchers.IO) {
        val fetchedList = mutableListOf<NewsArticle>()
        
        for (agency in AGENCIES) {
            try {
                val request = Request.Builder()
                    .url(agency.rssUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val xmlData = response.body?.string() ?: ""
                    if (xmlData.isNotEmpty()) {
                        val parsedArticles = parseRssFeed(xmlData, agency)
                        fetchedList.addAll(parsedArticles)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val initialFallback = getInitialNewsArticles()
        if (fetchedList.isEmpty()) {
            initialFallback.sortedByDescending { it.pubTimestamp }
        } else {
            // Combine fetched live news with fallbacks for agencies that had network errors
            val existingAgencies = fetchedList.map { it.agency.id }.toSet()
            val missingAgencyFallbacks = initialFallback.filter { it.agency.id !in existingAgencies }
            
            (fetchedList + missingAgencyFallbacks)
                .distinctBy { it.title.trim() }
                .sortedByDescending { it.pubTimestamp }
        }
    }

    private fun parseDateToMillis(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val formats = listOf(
            java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US),
            java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        )
        for (sdf in formats) {
            try {
                val date = sdf.parse(dateStr.trim())
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        if (diff <= 0) return "لحظاتی پیش"
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 2 -> "لحظاتی پیش"
            minutes < 60 -> "$minutes دقیقه پیش"
            hours < 24 -> "$hours ساعت پیش"
            else -> "$days روز پیش"
        }
    }

    private fun parseRssFeed(xmlData: String, agency: NewsAgency): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var eventType = parser.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentDesc = ""
            var currentPubDate = ""
            var currentImgUrl: String? = null
            var insideItem = false

            val imgRegex = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name ?: ""
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                            insideItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentDesc = ""
                            currentPubDate = ""
                            currentImgUrl = null
                        } else if (insideItem) {
                            when (name.lowercase()) {
                                "title" -> currentTitle = readElementText(parser)
                                "link" -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    if (!href.isNullOrEmpty()) {
                                        currentLink = href
                                    } else {
                                        currentLink = readElementText(parser)
                                    }
                                }
                                "description", "summary", "content" -> {
                                    val rawDesc = readElementText(parser)
                                    if (currentDesc.isEmpty()) {
                                        currentDesc = rawDesc
                                    }
                                    if (currentImgUrl == null) {
                                        val match = imgRegex.find(rawDesc)
                                        if (match != null) {
                                            currentImgUrl = match.groupValues[1]
                                        }
                                    }
                                }
                                "pubdate", "published", "updated" -> currentPubDate = readElementText(parser)
                                "enclosure" -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (!url.isNullOrEmpty()) {
                                        currentImgUrl = url
                                    }
                                }
                                "media:content", "media:thumbnail" -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (!url.isNullOrEmpty()) {
                                        currentImgUrl = url
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if ((name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) && insideItem) {
                            insideItem = false
                            val cleanTitle = cleanHtml(currentTitle)
                            val cleanDesc = cleanHtml(currentDesc)
                            if (cleanTitle.length > 5) {
                                val cat = categorizeNews(cleanTitle, cleanDesc)
                                val parsedTs = parseDateToMillis(currentPubDate)
                                val formattedTime = if (currentPubDate.isNotEmpty()) formatTimeAgo(parsedTs) else "تازه‌ترین خبر"
                                val article = NewsArticle(
                                    id = "${agency.id}_${cleanTitle.hashCode()}",
                                    title = cleanTitle,
                                    summary = if (cleanDesc.length > 180) cleanDesc.take(180) + "..." else cleanDesc,
                                    agency = agency,
                                    pubDate = currentPubDate.ifEmpty { "امروز" },
                                    timeAgo = formattedTime,
                                    category = cat,
                                    link = currentLink.ifEmpty { agency.websiteUrl },
                                    imageUrl = currentImgUrl,
                                    pubTimestamp = parsedTs
                                )
                                articles.add(article)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return articles
    }

    private fun readElementText(parser: XmlPullParser): String {
        val result = StringBuilder()
        var depth = 1
        while (depth > 0) {
            when (val event = parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    result.append(parser.text)
                }
                XmlPullParser.END_DOCUMENT -> break
            }
        }
        return result.toString()
    }

    private fun cleanHtml(htmlStr: String): String {
        return htmlStr
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    private fun categorizeNews(title: String, desc: String): NewsCategory {
        val text = "$title $desc".lowercase()
        return when {
            text.contains("طلا") || text.contains("سکه") || text.contains("دلار") || text.contains("ارز") || text.contains("یورو") -> NewsCategory.CurrencyGold
            text.contains("بورس") || text.contains("سهام") || text.contains("شاخص") || text.contains("فرابورس") -> NewsCategory.Bourse
            text.contains("بیت کوین") || text.contains("کریپتو") || text.contains("اتریوم") || text.contains("بلاکچین") -> NewsCategory.Crypto
            text.contains("تحریم") || text.contains("آمریکا") || text.contains("سازمان ملل") || text.contains("مذاکرات") || text.contains("بین‌الملل") -> NewsCategory.World
            else -> NewsCategory.Economic
        }
    }

    fun getInitialNewsArticles(): List<NewsArticle> {
        val fars = AGENCIES.find { it.id == "fars" } ?: AGENCIES[0]
        val irna = AGENCIES.find { it.id == "irna" } ?: AGENCIES[0]
        val yjc = AGENCIES.find { it.id == "yjc" } ?: AGENCIES[0]
        val isna = AGENCIES.find { it.id == "isna" } ?: AGENCIES[0]
        val tasnim = AGENCIES.find { it.id == "tasnim" } ?: AGENCIES[0]
        val bbc = AGENCIES.find { it.id == "bbc" } ?: AGENCIES[0]
        val mehr = AGENCIES.find { it.id == "mehr" } ?: AGENCIES[0]
        val ilna = AGENCIES.find { it.id == "ilna" } ?: AGENCIES[0]
        val moj = AGENCIES.find { it.id == "moj" } ?: AGENCIES[0]
        val tabnak = AGENCIES.find { it.id == "tabnak" } ?: AGENCIES[0]
        val khabaronline = AGENCIES.find { it.id == "khabaronline" } ?: AGENCIES[0]
        val borna = AGENCIES.find { it.id == "borna" } ?: AGENCIES[0]
        val ana = AGENCIES.find { it.id == "ana" } ?: AGENCIES[0]
        val alef = AGENCIES.find { it.id == "alef" } ?: AGENCIES[0]
        val irib = AGENCIES.find { it.id == "irib" } ?: AGENCIES[0]
        val sputnik = AGENCIES.find { it.id == "sputnik" } ?: AGENCIES[0]
        val independent = AGENCIES.find { it.id == "independent" } ?: AGENCIES[0]
        val voa = AGENCIES.find { it.id == "voa" } ?: AGENCIES[0]
        val donya = AGENCIES.find { it.id == "donya" } ?: AGENCIES[0]
        val tgju = AGENCIES.find { it.id == "tgju" } ?: AGENCIES[0]

        val now = System.currentTimeMillis()
        return listOf(
            NewsArticle(
                id = "news_1",
                title = "نرخ دلار و طلا در بازار امروز؛ بررسی آخرین نوسانات بازار آزاد",
                summary = "بازار طلا و ارز کشور امروز نوسانات ملایمی را تجربه کرد. گزارش میدانی نشان می‌دهد قیمت سکه امامی و دلار با تغییرات اندک به ثبات نسبی رسیده‌اند.",
                agency = tgju,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۱۰ دقیقه پیش",
                category = NewsCategory.CurrencyGold,
                link = "https://www.tgju.org",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=600&q=80",
                pubTimestamp = now - (10 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_2",
                title = "گزارش بازار بورس؛ صعود شاخص کل و ورود نقدینگی جدید به بازار سهام",
                summary = "شاخص کل بورس تهران در جریان معاملات امروز با رشد همراه شد. گروه‌های خودرویی و فلزات اساسی بیشترین تأثیر مثبت را بر شاخص کل برجای گذاشتند.",
                agency = donya,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۲۵ دقیقه پیش",
                category = NewsCategory.Bourse,
                link = "https://donya-e-eqtesad.com",
                imageUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=600&q=80",
                pubTimestamp = now - (25 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_3",
                title = "اطلاعیه بانک مرکزی درباره سیاست‌های جدید تخصیص ارز واردات",
                summary = "بانک مرکزی در راستای مدیریت بازار ارز، رویکردهای جدیدی برای تامین ارز کالاهای اساسی و واسطه‌ای تولید اعلان نمود.",
                agency = tasnim,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۴۰ دقیقه پیش",
                category = NewsCategory.Economic,
                link = "https://www.tasnimnews.ir",
                imageUrl = null,
                pubTimestamp = now - (40 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_4",
                title = "تثبیت قیمت بیت‌کوین در کانال ۶۵ هزار دلار؛ تحلیل وضعیت بازار کریپتو",
                summary = "رمزارز بیت‌کوین پس از اصلاح اخیر مجددا به بالای ۶۵ هزار دلار بازگشت. تحلیلگران معتقدند کاهش نرخ بهره کلید صعود بعدی بازار است.",
                agency = isna,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۱ ساعت پیش",
                category = NewsCategory.Crypto,
                link = "https://www.isna.ir",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80",
                pubTimestamp = now - (60 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_5",
                title = "گزارش بانک جهانی درباره رشد اقتصادی کشورهای منطقه در سال جاری",
                summary = "بانک جهانی در تازه‌ترین گزارش اقتصادی خود، چشم‌انداز رشد اقتصادی کشورهای خاورمیانه و بازار انرژی را تحلیل کرده است.",
                agency = irna,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۲ ساعت پیش",
                category = NewsCategory.World,
                link = "https://www.irna.ir",
                imageUrl = null,
                pubTimestamp = now - (120 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_6",
                title = "تحلیل روند قیمت طلا و حباب سکه در بازار تهران",
                summary = "کارشناسان بازار طلا با اشاره به روند اونس جهانی و نرخ دلار، بازه صعودی یا نزولی حباب سکه در روزهای آینده را ارزیابی نمودند.",
                agency = mehr,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۳ ساعت پیش",
                category = NewsCategory.CurrencyGold,
                link = "https://www.mehrnews.com",
                imageUrl = null,
                pubTimestamp = now - (180 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_7",
                title = "جدیدترین تصمیمات شورای پول و اعتبار پیرامون تسهیلات مسکن و بخش تولید",
                summary = "شورای پول و اعتبار ضوابط جدید اعطای تسهیلات به واحدهای تولیدی و تولید مسکن را تصویب و ابلاغ کرد.",
                agency = fars,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۳.۵ ساعت پیش",
                category = NewsCategory.Economic,
                link = "https://www.farsnews.ir",
                imageUrl = null,
                pubTimestamp = now - (210 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_8",
                title = "تحلیل عملکرد بازار سرمایه و پیش‌بینی روند شاخص در هفته آینده",
                summary = "بررسی جریان نقدینگی و ارزش معاملات خرد در بورس تهران نشان از گرایش خریداران به سمت نمادهای ریالی دارد.",
                agency = yjc,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۴ ساعت پیش",
                category = NewsCategory.Bourse,
                link = "https://www.yjc.ir",
                imageUrl = null,
                pubTimestamp = now - (240 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_9",
                title = "آخرین تحولات اقتصادی خاورمیانه و نوسانات بازار نفت در بازار بین‌المللی",
                summary = "تحلیلگران بین‌المللی تحولات عرضه و تقاضای انرژی را در تقابل با چشم‌انداز تورمی بررسی می‌کنند.",
                agency = bbc,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۴.۵ ساعت پیش",
                category = NewsCategory.World,
                link = "https://www.bbc.com/persian",
                imageUrl = null,
                pubTimestamp = now - (270 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_10",
                title = "جزئیات حقوق و دستمزد کارگران و تصمیمات جدید شورای عالی کار",
                summary = "نشست تخصصی بررسی موازنه معیشت و تورم با حضور نمایندگان کارگری و کارفرمایی برگزار شد.",
                agency = ilna,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۵ ساعت پیش",
                category = NewsCategory.Economic,
                link = "https://www.ilna.ir",
                imageUrl = null,
                pubTimestamp = now - (300 * 60 * 1000)
            ),
            NewsArticle(
                id = "news_11",
                title = "تحلیل آخرین وضعیت تورم و رشد نقدینگی بر اساس آمارهای کلان اقتصادی",
                summary = "بررسی آمارهای رسمی نشان از کند شدن نرخ رشد پایه پولی در ماه‌های اخیر دارد.",
                agency = khabaronline,
                pubDate = "۱۴۰۳/۰۵/۰۱",
                timeAgo = "۵.۵ ساعت پیش",
                category = NewsCategory.Economic,
                link = "https://www.khabaronline.ir",
                imageUrl = null,
                pubTimestamp = now - (330 * 60 * 1000)
            )
        )
    }
}
