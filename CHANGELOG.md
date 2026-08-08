## [v1.0.0 - Stable] - 2026-08-08

# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:
## 🎉 این اولین ورژن پایدار هست
- اضافه کردن مجوز VIBRATE به AndroidManifest
- ایجاد کلاس ابزار HapticUtils با پشتیبانی از ارتعاشات سبک، متوسط، موفقیت، خطا
- ادغام بازخورد لمسی در تمام کلیک‌ها، دکمه‌های تغییر وضعیت، دیالوگ‌ها و اقدامات کشیدن رابط کاربری
- پیاده‌سازی مرتب‌سازی سفارشی کشیدن و رها کردن برای اقلام بازار با پایداری
- معرفی LocalAdaptiveDpScale و LocalAdaptiveSpScale برای طراحی واکنش‌گرا در دستگاه‌ها
- بازسازی انیمیشن‌های انتقال صفحه با استفاده از CubicBezierEasing
- بهبود فیزیک کشیدن BentoGrid با انیمیشن‌های فنری و ارتفاع پویا
- اضافه کردن منابع رشته‌ای محلی برای سفارشی‌سازی ردیف Bento Grid
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
-----

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes: 
## 🎉 This is First Version Stable


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

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

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
# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

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
مدل داده و ViewModel ها اصلاح شده
- رفع اشکالات



## [v0.9.4 - Beta] - 2026-07-31

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

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
# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

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

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

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
# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

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
# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

- Correct the App Version in Settings
- Fix Loop Update
- Fix Bugs
# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

- درست کردن ورژن اپ در تنظیمات
- حل مشکل اپدیت پشت سرهم
- فیکس کردن باگ ها

# 0.7.8
# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

- Fix Update Problem
# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

- حل مشکل آپدیت


# 0.7.7

# NEEEWWW VEERRRSSSIIIIOOONNNNNNNN🚀
## The Changes:

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

# 🚀 نسسسسسخه جدیییییدددددد!
## تغییرات:

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

### v0.7.0

### The First Release Of Cheghad App🚀

If app has Bug Tell me from Issues

⚠️Warning:This Verison is Beta & Can has bug

-----

### اولین نسخه از اپ چقد🚀

اگر اپ باگی داشت از طریق Issue بهم اطلاع بدید

⚠️هشدار:این نسخه بتا هست و ممکنه باگ داشته باشه

-----

Developed By **mmdparsa**
