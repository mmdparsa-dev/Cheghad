## [v1.1.5 - Stable] - 2026-11-27

#  یه اپدیت کوچیک
## تغییرات:

- رفع مشکل کرش برنامه در هنگام افقی کردن نمودار
- اضافه شدن نسخه store برای استور ها 
- رفع بعضی از مشکلات
-----

# a little update 
## The Changes: 

- Fixed an app crash that occurred when rotating the chart to landscape mode
- Added a version for app stores
- Fixed various issues


## [v1.1.4 - Stable] - 2026-11-27

# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

- جایگزینی اسکرپینگ HTML سایت TGJU با وب‌سرویس واقعی call.tgju.org/ajax.json همراه با مدیریت صحیح بستن ریسپانس‌ها و برگرداندن کد 503 در صورت عدم دسترسی به نرخ لحظه‌ای
- حذف کامل داده‌های فیک و تصادفی (fluctuate، درصدهای تصادفی، تولیدکننده نمودار آزمایشی timeRandom و نرخ ثابت دلار)
- حذف کامل سرویس‌ها و مدل‌های بلااستفاده KifpoolApiService، KifpoolApi، KifpoolOkHttpClient، KifpoolRetrofit و KifpoolModels
- دریافت کندل‌ها و تاریخچه واقعی قیمت از dashboard-api.tgju.org/v1/tv2/history برای نمودار تکنیکال و نمودار ساده (CurrencyRepository.fetchHistory و مدیریت هم‌زمانی با historyFetchJob)
- دریافت هم‌زمان USOON از تاریخچه TV2 و تبدیل ارزهای دلاری (BTC، ETH، USOON) به تومان بر اساس نرخ لحظه‌ای دلار
- استفاده از kotlinx.serialization برای ساخت ایمن خروجی JSON اندپوینت‌ها
- افزودن تایم‌فریم‌های ساعتی/دقیقه‌ای 1m (MIN1) و 5m (MIN5) در چارت تکنیکال با مپینگ به رزولوشن‌های "1" و "5" در TGJU
- تنظیم خودکار زوم اولیه نمودار تکنیکال روی کل تاریخچه بارگذاری‌شده هنگام باز شدن و افزودن دکمه تغییر حالت افقی/عمودی (Landscape / Portrait)
- پشتیبانی هم‌زمان از چند اندیکاتور میانگین متحرک نمایی (EMA 25، EMA 50، EMA 100 و EMA 200) با رنگ‌ها و استروک‌های متمایز، پیش‌تنظیم‌های سریع و خروجی PNG/PDF
- نمایش تاریخ و زمان دقیق همراه با مقادیر OHLC هنگام بررسی کندل با کراس‌هر (formatCandleDateTime)
- تکمیل ترجمه فارسی و انگلیسی تمام بخش‌های نمودار تکنیکال، ابزارهای رسم و گزارش PDF/PNG
- جایگزینی فونت وزیر با فونت یکپارچه google_sans_vazirmatn_rounded.ttf در رابط کاربری و ویجت صفحه خانه
- پشتیبانی از رندر Markdown در یادداشت‌های انتشار (MarkdownContent در UpdateDialog) و بهبود فرآیند بررسی آپدیت
- افزودن متن منبع "تمام اطلاعات از tgju.com گرفته شده" در انتهای لیست صفحه بازار
- جایگزینی نمایش بینهایت با متن "Overflow" در صفحه ماشین‌حساب ارز
- اصلاح برچسب "ردیف سوم" (third_row_customize)، ساده شدن رنگ پیش‌فرض ردیف سوم کارت‌های خانه (row3Colored = false) و بهبود کارت‌های سوییچ و انیمیشن نرم مرتب‌سازی بازه‌های زمانی در تنظیمات
- غیرفعال شدن پیش‌فرض خبرها، دسته‌بندی‌ها و خبرگزاری‌ها (newsEnabled و فیلترهای پیش‌فرض)
- یکپارچه‌سازی تشخیص تلویزیون (isTvDevice) با بررسی UiModeManager و Configuration.uiMode و افزودن هلپرهای مشترک فوکوس TV (rememberTvFocusState و TvFocusableIconButton)
- بهینه‌سازی کامل نمودار تکنیکال و کندل‌استیک برای Android TV شامل دکمه‌های کندل قبلی/بعدی و زوم، میانبرهای ریموت/کیبورد و ابعاد خواناتر در نمایشگر تلویزیون
- بهینه‌سازی تمام صفحات (خانه، بازار، تنظیمات، ماشین‌حساب، اخبار با گرید ۲ ستونه، دیالوگ جزئیات با کنترل D-pad نمودار ساده، و دیالوگ آپدیت) برای ناوبری یک‌کلیکی با ریموت تلویزیون
- به‌روزرسانی پیکربندی بیلد و رپر گرادل و افزودن تست‌های واحد، Robolectric حالت تلویزیون و تست‌های Compose دستگاهی (ApiClientParsingTest، MarkdownParserTest، PriceFormattingTest، TechnicalChartEnhancementsTest، TvOptimizationRobolectricTest، CheghadDeviceInstrumentedTest)

-----

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

- Replaced TGJU HTML scraping with the real call.tgju.org/ajax.json endpoint with safe response closing and HTTP 503 responses when live rates are unavailable
- Removed all synthetic/random fallbacks (fluctuate(), seeded percentages, timeRandom mock chart generator, and hardcoded USD fallback prices)
- Removed unused KifpoolApiService, KifpoolApi, KifpoolOkHttpClient, KifpoolRetrofit, and KifpoolModels
- Connected both Technical Chart and Simple Chart (CurrencyRepository.fetchHistory with cancellation-safe historyFetchJob) to real candle history from dashboard-api.tgju.org/v1/tv2/history
- Added concurrent USOON fetching via TV2 history and converted USD-denominated crypto assets (BTC, ETH, USOON) to Toman using the live USD rate
- Used kotlinx.serialization for safe API JSON response building
- Added 1m (MIN1) and 5m (MIN5) intraday timeframes mapped to TGJU resolutions "1" and "5"
- Initialized the Technical Chart viewport to fit full loaded history on open and added a quick landscape/portrait toggle button
- Implemented simultaneous Multi-EMA indicators (EMA 25, EMA 50, EMA 100, EMA 200 via EmaPeriod) with distinct colors/strokes, quick presets, and PNG/PDF export support
- Added full date and time display alongside OHLC values on crosshair candle inspection (formatCandleDateTime)
- Completed full English and Persian localization across Technical Chart controls, drawing tools, indicators, and PDF/PNG exports
- Replaced Vazir font files with unified google_sans_vazirmatn_rounded.ttf across the app theme and Glance widget
- Added Markdown release notes rendering in UpdateDialog (MarkdownContent) and streamlined update checking
- Added "All data is sourced from tgju.com" footer at the bottom of the Market list
- Replaced infinity output with "Overflow" in CurrencyCalculatorScreen
- Fixed the "Third row" label (third_row_customize), set Row 3 default card style to simple (row3Colored = false), updated settings switch cards, and smoothed TimeRangeReorderSection spring animations
- Disabled news feed, news categories, and news agencies by default
- Unified isTvDevice() (UiModeManager + Configuration.uiMode) and added shared TV focus utilities (rememberTvFocusState, TvFocusableIconButton) plus ExpressiveConnectedButtonGroup D-pad focus highlights
- Optimized Technical Chart and Candlestick view for Android TV with larger 10-foot labels/legend, Previous/Next candle and zoom quick controls, remote key shortcuts, and TV-sized export bottom sheets
- Optimized Home, Market, Settings, Calculator, News (2-column TV grid), Asset Detail Dialog (with D-pad scrubbing on InteractiveAssetChart), and Update Dialog for widescreen TV and single-click D-pad focus without Switch focus traps
- Updated Gradle wrapper/build config and added unit, TV Robolectric Compose, and on-device Compose instrumented tests (ApiClientParsingTest, MarkdownParserTest, PriceFormattingTest, TechnicalChartEnhancementsTest, TvOptimizationRobolectricTest, CheghadDeviceInstrumentedTest)


## [v1.1.1 - Stable] - 2026-09-08

#  یه اپدیت کوچیک
## تغییرات:

- اضافه شدن قابلیت تغییر رنگ کندل ها
- اضافه شدن قابلیت تغییر عملکرد لمس کارت های ارز
- تغییر مکان انتقال کانتینر اطلاعات کندل
- رفع باگ نمایش nav هنگام کار کردن با نمودار تکنیکال در اندروید تی وی
-----

# a little update 
## The Changes: 

- Added the ability to change the color of candles
- Added the ability to change the touch function of currency cards
- Changed the location of the candle data container
- Fixed the nav display bug when working with technical charts on Android TV


## [v1.1.0 - Stable] - 2026-09-07
# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

- اضافه شدن keystore.properties به .gitignore 
- پیاده‌سازی لودینگ داینامیک keystore.properties در Gradle
- قابلیت خواندن پسوردها و مسیر استور از Environment Variables یا فایل لوکال
- اضافه شدن دیپندنسی‌های تست androidx.junit و runner 
- پیاده‌سازی متغیر استیت selectedTechnicalAsset در MainActivity 
- اضافه شدن BackHandler اختصاصی برای صفحه technical_chart 
- اضافه شدن technical_chart به ترتیب انیمیشن‌های صفحه در AnimatedContent 
- رندر کامپوننت TechnicalChartScreen به همراه دیتای فال‌بک دلاری
- اکشن باز کردن چارت تکنیکال در متد onOpenTechnicalChart صفحه هشدارها
- غیرفعال شدن نوار نویگیشن ( NavigationSuiteType.None ) در نمای چارت تکنیکال
- دکمه دسترسی به چارت تکنیکال در دیالوگ جزییات دارایی ( AssetDetailDialog )
- کارت دسترسی به چارت تکنیکال در صفحه هشدارها ( AlarmsScreen )
- تعریف کلاینت اشتراکی ScraperOkHttpClient با مهلت زمانی ۱۲ ثانیه
- افزایش تایم‌اوت وب‌اسکرپینگ TGJU به ۱۲ ثانیه
- افزایش تایم‌اوت ریکوئست مارکت بیت‌پین به ۱۲ ثانیه
- افزایش زمان انتظار CompletableFuture.allOf به ۱۲ ثانیه
- انتقال کوئری‌های getCachedCurrencies و saveCurrenciesToCache به کانتکست Dispatchers.IO 
- مدیریت اکسپشن با بلوک‌های try-catch در کش دیتابیس Room
- فیلتر کردن اخبار فال‌بک بر اساس خبرگزاری‌های فعال ( activeAgencyIds )
- پیاده‌سازی کامپوننت رفرش ExpressivePullToRefreshBox در صفحه اخبار
- حذف ارور سراسری در CoroutineExceptionHandler و جایگزینی با لاگ در ViewModel
- همگام‌سازی دیتای کش SharedPreferences به دیتابیس Room در راه‌اندازی اولیه
- افزودن دیلی ۱ ثانیه‌ای پیش از اولین دریافت زمان‌بندی‌شده نرخ‌ها
- عدم پاک شدن ارور در زمان رفرش‌های اتوماتیک دوره‌ای
- امن‌سازی فراخوانی به‌روزرسانی ویجت‌ها با بلوک try-catch 
- امن‌سازی بررسی هشدارهای قیمت با بلوک try-catch 
- اضافه شدن ریسورس‌های رشته‌ای چارت تکنیکال به زبان انگلیسی
- اضافه شدن ریسورس‌های رشته‌ای چارت تکنیکال به زبان فارسی

-----

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes: 

- Added keystore.properties to .gitignore
- Implemented dynamic loading of keystore.properties in Gradle
- Ability to read passwords and store path from Environment Variables or local file
- Added androidx.junit and runner test dependencies
- Implemented selectedTechnicalAsset state variable in MainActivity
- Added dedicated BackHandler for technical_chart page
- Added technical_chart in order of page animations in AnimatedContent
- Rendered TechnicalChartScreen component with dollar fallback data
- Action to open technical chart in onOpenTechnicalChart method of alerts page
- Disabling navigation bar (NavigationSuiteType.None) in technical chart view
- Technical chart access button in asset detail dialog (AssetDetailDialog)
- Technical chart access card in alerts page (AlarmsScreen)
- Definition of ScraperOkHttpClient shared client with 12 second timeout
- Increased TGJU web scraping timeout to 12 seconds
- Increase BitPin Market request timeout to 12 seconds
- Increase CompletableFuture.allOf wait time to 12 seconds
- Move getCachedCurrencies and saveCurrenciesToCache queries to Dispatchers.IO context
- Handle exceptions with try-catch blocks in Room database cache
- Filter fallback news based on active agencies ( activeAgencyIds )
- Implement ExpressivePullToRefreshBox refresh component on news page
- Remove global error in CoroutineExceptionHandler and replace with log in ViewModel
- Synchronize SharedPreferences cache data to Room database on initial startup
- Add 1 second delay before first scheduled fetch of rates
- Error not cleared during periodic automatic refreshes
- Secure widget update call with try-catch block
- Secure price alert checking with try-catch block
- Added technical chart string resources in English
- Added technical chart string resources in Persian


## [v1.0.6.5 - Stable] - 2026-08-23

#  یه اپدیت کوچیک
## تغییرات:

- حل مشکل نصف شدن Navbar در اندروید TV و اندروید
- ریسپانسیو تر شدن Navbar
-----

# a little update 
## The Changes: 

- Fixed bottom navigation bar cutoff issue on Android and Android TV
- Improved navigation bar responsiveness


# Cheghad Changelog

## [v1.0.0 - Stable] - 2026-08-08

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
### 🎉 این اولین ورژن پایدار هست
- اضافه کردن مجوز VIBRATE به AndroidManifest
- ایجاد کلاس ابزار HapticUtils با پشتیبانی از ارتعاشات سبک، متوسط، موفقیت، خطا
- ادغام بازخورد لمسی در تمام کلیک‌ها، دکمه‌های تغییر وضعیت، دیالوگ‌ها و اقدامات کشیدن رابط کاربری
- پیاده‌سازی مرتب‌سازی سفارشی کشیدن و رها کردن برای اقلام بازار با پایداری
- معرفی LocalAdaptiveDpScale و LocalAdaptiveSpScale برای طراحی واکنش‌گرا در دستگاه‌ها
- بازسازی انیمیشن‌های انتقال صفحه با استفاده از CubicBezierEasing
- بهبود فیزیک کشیدن BentoGrid با انیمیشن‌های فنری و ارتفاع پویا
- اضافه کردن منابع رشته���ای محلی برای سفارشی‌سازی ردیف Bento Grid
- اضافه کردن کلید اصلی در تنظیمات DataStore برای فعال/غیرفعال کردن کامل اخبار
- اضافه کردن دکمه‌های اقدام جمعی 'فعال کردن همه' و 'غیرفعال کردن همه' برای خبرگزاری‌ها
- طراحی صفحه نمایش وضعیت خالی سفارشی با دکمه اقدام مستقیم برای فید خبری غیرفعال
- بهینه‌سازی NewsRepository برای اجرای همزمان دریافت‌های RSS از طریق کوروتین‌های async/awaitAll
- انتقال‌های انیمیشن روان بین حالت‌های محاسبه
- پیاده‌سازی تأخیر بارگذاری اولیه ۱۵۰۰ میلی‌ثانیه برای افزایش پاسخگویی رابط کاربری هنگام راه‌اندازی
- اضافه کردن اولویت انتخاب به‌روزرسانی بتا/آلفا در تنظیمات
- بهبود UpdateManager برای تمایز دقیق نسخه‌های پایدار از پیش‌نسخه‌ها
- فعال کردن کوچک‌سازی R8 و کاهش منابع برای نوع ساخت نسخه
- پیکربندی قوانین ProGuard برای حذف تمام فراخوانی‌های متد android.util.Log
- پاکسازی کدبیس با حذف فراخوانی‌های HttpLoggingInterceptor و printStackTrace
- ارتقاء Gradle Wrapper به ۹.۷.۰، KSP به ۲.۳.۱۱ و افزایش حافظه هیپ JVM به -Xmx4g

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes: 
### 🎉 This is First Version Stable
- Add VIBRATE permission to AndroidManifest
- Create HapticUtils utility class supporting Light, Medium, Success, Error vibrations
- Integrate haptic feedback into all UI clicks, toggles, dialogs, and drag actions
- Implement custom Drag & Drop sorting for market items with persistence
- Introduce LocalAdaptiveDpScale & LocalAdaptiveSpScale for responsive design across devices
- Refactor screen transition animations using CubicBezierEasing
- Enhance BentoGrid drag physics with spring animations & dynamic elevation
- Add localized string resources for Bento Grid row customization
- Add master toggle in Settings DataStore to completely enable/disable news
- Add 'Enable All' and 'Disable All' mass action buttons for news agencies
- Design custom Empty State screen with direct action button for disabled news feed
- Optimize NewsRepository to execute concurrent RSS fetches via async/awaitAll coroutines
- Smooth animation transitions between calculation modes
- Implement 1500ms initial load delay to boost launch UI responsiveness
- Add Beta/Alpha update opt-in preference in settings
- Enhance UpdateManager to strictly distinguish stable releases from prereleases
- Enable R8 minification and resource shrinking for release build type
- Configure ProGuard rules to strip all android.util.Log method calls
- Clean up codebase by removing HttpLoggingInterceptor and printStackTrace calls
- Upgrade Gradle Wrapper to 9.7.0, KSP to 2.3.11, and bump JVM heap memory to -Xmx4g

## [v0.9.9 - RC] - 2026-08-04

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes:
- Add Antimation in Calculator 
- Add History to Calculator
- Sync Widget with Lastest Update App (Every 15m in background)
- Optimized Lockscreen Widget for Google Pixels
- Now for Change Apparace Only lockscreen widget go to settings app
- Add animation to some sections
- Faster Update Currency
- Fix Update Loop
- Translate Updateing
- Translate some Sections Calculator
- Translate News Agencies
- Translate other Texts
- Refrector Datamodel & Viewmodels
- Fix Bugs

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
- اضافه کردن انیمیشن در ماشین حساب
- اضافه کردن تاریخچه به ماشین حساب
- همگام سازی ویجت با آخرین بروزرسانی برنامه (هر 15 دقیقه در پس زمینه)
- بهینه سازی ویجت صفحه قفل برای گوگل پیکسل
- اکنون برای تغییر ویجت صفحه قفل فقط برای برنامه، به تنظیمات برنامه بروید
- اضافه کردن انیمیشن به برخی بخش ها
- بروزرسانی سریعتر ارز
- رفع حلقه بروزرسانی
- ترجمه بروزرسانی
- ترجمه برخی از بخش ها در ماشین حساب
- ترجمه خبرگزاری ها
- ترجمه سایر متون
- مدل داده و ViewModel ها اصلاح شده
- رفع اشکالات

## [v0.9.4 - Beta] - 2026-07-31

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes:
- Fix Slow app Boot
- Re-writed some section by Material Design 3 Experessive
- Add Color app
- Add 2 Widget for unlock screen
- Add Preview Widget 
- Translated Calculator section
- Update Switches
- Update Progress inductor
- Fix not show resume in Welcome
- Button "Select Asset / Currency" Re-writed by Material Design 3 Experessive
- Respansived Widgets
- Fix Bugs

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
- رفع مشکل کندی در اجرای اولیه برنامه
- بازنویسی بخش‌هایی از برنامه با استفاده از استانداردهای Material Design 3 Expressive
- افزودن قابلیت انتخاب رنگ برای برنامه
- افزودن ۲ ویجت برای صفحه قفل
- افزودن ویجت پیش‌نمایش
- ترجمه بخش ماشین‌حساب
- به‌روزرسانی سوئیچ‌ها
- به‌روزرسانی نشانگر پیشرفت
- رفع مشکل عدم نمایش گزینه ادامه در صفحه خوش‌آمدگویی
- بازنویسی دکمه «انتخاب دارایی/ارز» بر اساس Material Design 3 Expressive
- بهینه‌سازی ویجت‌ها برای نمایش واکنش‌گرا
- برطرف کردن باگ ها

## [v0.8.6 - Beta] - 2026-07-27

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes:
- Fix problem Update
- Updates Component
- Add Edit in widgets
- You can change Apparace widget
- You can select Currency widget
- Add status update
- Fix Database problem
- Re-writed Animations by Material 3 Experrssive
- Add Gold Ouns
- Fix Bugs

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
- رفع مشکل به‌روزرسانی
- به‌روزرسانی کامپوننت
- افزودن قابلیت ویرایش به ویجت‌ها
- امکان تغییر ظاهر ویجت
- امکان انتخاب ویجت ارز
- افزودن قابلیت به‌روزرسانی وضعیت
- رفع مشکل پایگاه داده
- بازنویسی انیمیشن‌ها با استفاده از Material 3 Expressive
- افزودن قیمت انس طلا
- فیکس باگ‌ها

## [v0.7.9 - Beta] - 2026-07-25

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes:
- Correct the App Version in Settings
- Fix Loop Update
- Fix Bugs

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
- درست کردن ورژن اپ در تنظیمات
- حل مشکل اپدیت پشت سرهم
- فیکس کردن باگ ها

## [v0.7.8]

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes:
- Fix Update Problem

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
- حل مشکل آپدیت

## [v0.7.7]

### 🚀 NEEEWWW VEERRRSSSIIIIOOONNNNNNNN
### The Changes:
- Add USOON
- Rewrited by Material 3 Expressive
- Smoother Animations
- Add Update Application
- Add Check For In Application
- Alerts Now has Notifications
- Add Animation to Time Chart
- New Appearance Update
- You can Separation and Merge Boxes in Customiztion mode
- You can Colorful and Uncolorful Boxes in Customiztion mode
- Add Animations
- Add Wigets
- Delete Not Used Files
- Fix Bugs

### 🚀 نسسسسسخه جدیییییدددددد!
### تغییرات:
- افزودن USOON
- بازنویسی بر اساس Material 3 Expressive
- انیمیشن‌های روان‌تر
- افزودن به‌روزرسانی برنامه
- افزودن بررسی به‌روزرسانی در برنامه
- هشدارها اکنون دارای ناتیفیکیشن هستند
- افزودن انیمیشن به نمودار زمانی
- به‌روزرسانی ظاهر جدید
- می‌توانید باکس‌ها را در حالت سفارشی‌سازی تفکیک و ادغام کنید
- می‌توانید باکس‌ها را در حالت سفارشی‌سازی رنگی و بدون رنگ کنید
- افزودن انیمیشن‌ها
- افزودن ویجت‌ها
- حذف فایل‌های استفاده نشده
- رفع باگ‌ها

## [v0.7.0]

### The First Release Of Cheghad App🚀

If app has Bug Tell me from Issues

⚠️ Warning: This Version is Beta & Can has bug

---

### اولین نسخه از اپ چقد🚀

اگر اپ باگی داشت از طریق Issue بهم اطلاع بدید

⚠️ هشدار: این نسخه بتا هست و ممکنه باگ داشته باشه

---

Developed By **mmdparsa**
