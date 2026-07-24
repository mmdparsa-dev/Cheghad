# Contributing to Cheghad

Thank you for your interest in contributing to Cheghad! We welcome contributions from everyone. This document provides guidelines and instructions for contributing to our project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Reporting Issues](#reporting-issues)
- [Feature Requests](#feature-requests)

## Code of Conduct

We are committed to providing a welcoming and inspiring community for all. Please be respectful, inclusive, and constructive in all interactions.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** to your local machine
3. **Create a new branch** for your work
4. **Make your changes** and test thoroughly
5. **Submit a pull request** to the main repository

## Development Setup

### Prerequisites

- Android Studio (latest stable version)
- JDK 11 or higher
- Git
- Gradle

### Setup Instructions

1. Clone the repository:
```bash
git clone https://github.com/mmdparsa-dev/Cheghad.git
cd Cheghad
```

2. Open the project in Android Studio:
   - File → Open → Select the Cheghad directory
   - Let Gradle sync and build the project

3. Install dependencies:
```bash
./gradlew dependencies
```

4. Build the project:
```bash
./gradlew build
```

## How to Contribute

### Types of Contributions

We welcome:
- **Bug fixes**: Help us identify and resolve issues
- **Features**: Propose and implement new functionality
- **Documentation**: Improve README, guides, and code comments
- **Tests**: Add unit tests and UI tests
- **Translations**: Help localize the app
- **Design improvements**: Suggest UI/UX enhancements

### Before You Start

1. Check if your issue/feature hasn't already been reported
2. Discuss major changes in an issue first
3. Keep changes focused and atomic
4. Write clear commit messages

## Pull Request Process

1. **Update your branch** with the latest main:
```bash
git fetch upstream
git rebase upstream/main
```

2. **Push to your fork**:
```bash
git push origin your-feature-branch
```

3. **Create a Pull Request** on GitHub with:
   - Clear title describing the change
   - Description of what and why the change was made
   - Reference to related issues (#issue-number)
   - Screenshots/videos for UI changes

4. **Respond to feedback** and make requested changes

5. **Wait for approval** from maintainers

## Coding Standards

### Kotlin Style Guide

- Follow [Kotlin Official Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names

### Android Best Practices

- Follow [Android Architecture Components](https://developer.android.com/topic/architecture)
- Use MVVM or MVI architecture patterns
- Implement proper error handling
- Use Material 3 design components

### Code Quality

- Write clean, readable code
- Add comments for complex logic
- Avoid code duplication
- Use Android Lint to check for issues

## Commit Guidelines

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring without feature changes
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **chore**: Build, dependency, or tooling changes

### Examples

```
feat(expense): add expense filtering by category

- Implement category filtering in expense list
- Add filter UI components
- Add unit tests for filtering logic

Closes #123
```

```
fix(dashboard): fix currency conversion calculation

The exchange rate was being applied twice,
causing incorrect calculations.

Fixes #456
```

## Reporting Issues

### Bug Report Template

When reporting a bug, please include:

1. **Description**: Clear description of the issue
2. **Steps to Reproduce**: Step-by-step instructions
3. **Expected Behavior**: What should happen
4. **Actual Behavior**: What actually happens
5. **Screenshots**: Visual evidence if applicable
6. **Environment**: 
   - Device model and Android version
   - App version
   - Any relevant settings

### Create an Issue

Visit the [Issues](https://github.com/mmdparsa-dev/Cheghad/issues) page and click "New Issue"

## Feature Requests

We love hearing about ideas to improve Cheghad!

**Before submitting:**
- Check if the feature has already been requested
- Consider if it aligns with the project's goals
- Think about implementation approach

**Include in your request:**
- Clear description of the feature
- Use cases and benefits
- Proposed UI/UX (if applicable)
- Any alternative approaches you've considered

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests com.example.cheghad.ExampleTest

# Run instrumentation tests
./gradlew connectedAndroidTest
```

### Test Coverage

- Aim for high coverage on business logic
- Use JUnit for unit tests
- Use Espresso for UI tests
- Mock external dependencies

## Documentation

- Update README.md for user-facing changes
- Add inline code comments for complex logic
- Update CHANGELOG.md
- Create/update API documentation if needed

## Questions?

- Open a discussion on GitHub
- Check existing issues and PRs
- Contact the maintainers

---

Thank you for contributing to Cheghad! Your efforts help make this project better for everyone. 🙌

### فارسی

# مشارکت در پروژه Cheghad

از علاقه شما به مشارکت در پروژه Cheghad سپاسگزاریم! ما از مشارکت همه افراد استقبال می‌کنیم. این سند راهنمایی برای مشارکت در پروژه ما فراهم می‌کند.

## فهرست مطالب

- [قوانین رفتاری](#قوانین-رفتاری)
- [شروع کار](#شروع-کار)
- [تنظیم محیط توسعه](#تنظیم-محیط-توسعه)
- [روش‌های مشارکت](#روش‌های-مشارکت)
- [فرآیند Pull Request](#فرآیند-pull-request)
- [استانداردهای کدنویسی](#استانداردهای-کدنویسی)
- [دستورالعمل Commit](#دستورالعمل-commit)
- [گزارش مشکلات](#گزارش-مشکلات)
- [درخواست ویژگی‌های جدید](#درخواست-ویژگی‌های-جدید)

## قوانین رفتاری

ما متعهد به ایجاد یک جامعه خوش‌آمد و الهام‌بخش برای همه هستیم. لطفاً در تمام تعاملات احترام‌گذار، شامل‌کننده و سازنده باشید.

## شروع کار

1. **Fork کردن مخزن** در GitHub
2. **Clone کردن Fork شما** به دستگاه محلی
3. **ایجاد یک شاخه جدید** برای کار شما
4. **ایجاد تغییرات** و تست جامع
5. **ارسال Pull Request** به مخزن اصلی

## تنظیم محیط توسعه

### پیش‌نیازها

- Android Studio (آخرین نسخه پایدار)
- JDK 11 یا بالاتر
- Git
- Gradle

### دستورالعمل تنظیم

1. Clone کردن مخزن:
```bash
git clone https://github.com/mmdparsa-dev/Cheghad.git
cd Cheghad
```

2. باز کردن پروژه در Android Studio:
   - File → Open → انتخاب دایرکتوری Cheghad
   - اجازه دهید Gradle sync و build را انجام دهد

3. نصب Dependencies:
```bash
./gradlew dependencies
```

4. Build کردن پروژه:
```bash
./gradlew build
```

## روش‌های مشارکت

### انواع مشارکت

ما از موارد زیر استقبال می‌کنیم:
- **رفع باگ**: کمک به شناسایی و حل مشکلات
- **ویژگی‌های جدید**: پیشنهاد و پیاده‌سازی عملکردهای جدید
- **مستندات**: بهبود README، راهنماها و نظرات کد
- **تست‌ها**: افزودن Unit Test و UI Test
- **ترجمه**: کمک به محلی‌سازی برنامه
- **بهبودهای طراحی**: پیشنهاد بهبودهای UI/UX

### قبل از شروع

1. بررسی کنید که مشکل/ویژگی شما قبلاً گزارش نشده است
2. تغییرات بزرگ را ابتدا در یک issue بحث کنید
3. تغییرات را متمرکز و اتمی نگه دارید
4. پیام‌های Commit واضح بنویسید

## فرآیند Pull Request

1. **بروزرسانی شاخه شما** با آخرین Main:
```bash
git fetch upstream
git rebase upstream/main
```

2. **Push کردن به Fork شما**:
```bash
git push origin your-feature-branch
```

3. **ایجاد Pull Request** در GitHub با:
   - عنوان واضح توصیف‌کننده تغییر
   - توضیح در مورد چه و چرا تغییر انجام شد
   - ارجاع به issue های مرتبط (#issue-number)
   - Screenshot/Video برای تغییرات UI

4. **پاسخ به بازخورد** و اعمال تغییرات درخواستی

5. **انتظار برای تایید** از نگهداران

## استانداردهای کدنویسی

### راهنمای سبک Kotlin

- پیروی از [راهنمای رسمی Kotlin](https://kotlinlang.org/docs/coding-conventions.html)
- استفاده از 4 فاصله برای Indentation
- حداکثر طول خط: 120 کاراکتر
- استفاده از نام‌های معنادار برای متغیرها و توابع

### بهترین عملکردهای Android

- پیروی از [Android Architecture Components](https://developer.android.com/topic/architecture)
- استفاده از الگوهای معماری MVVM یا MVI
- پیاده‌سازی مدیریت خطا مناسب
- استفاده از مؤلفه‌های طراحی Material 3

### کیفیت کد

- نوشتن کد تمیز و قابل‌فهم
- افزودن نظرات برای منطق پیچیده
- جلوگیری از تکرار کد
- استفاده از Android Lint برای بررسی مشکلات

## دستورالعمل Commit

### فرمت پیام Commit

```
<type>(<scope>): <subject>

<body>

<footer>
```

### انواع

- **feat**: یک ویژگی جدید
- **fix**: رفع یک باگ
- **docs**: تغییرات مستندات
- **style**: تغییرات سبک کد (قالب‌بندی و غیره)
- **refactor**: بازسازی کد بدون تغییر ویژگی
- **perf**: بهبودهای عملکرد
- **test**: افزودن یا بروزرسانی تست‌ها
- **chore**: تغییرات Build، Dependency یا Tooling

### نمونه‌ها

```
feat(expense): افزودن فیلتر هزینه بر اساس دسته‌بندی

- پیاده‌سازی فیلتر دسته‌بندی در لیست هزینه
- افزودن مؤلفه‌های UI فیلتر
- افزودن Unit Test برای منطق فیلتر

بسته می‌کند #123
```

```
fix(dashboard): رفع محاسبه تبدیل ارز

نرخ تبادل دو بار اعمال می‌شد،
که باعث محاسبات نادرست می‌شد.

رفع می‌کند #456
```

## گزارش مشکلات

### الگوی گزارش Bug

هنگام گزارش یک باگ، لطفاً شامل کنید:

1. **توضیح**: توضیح واضح مشکل
2. **مراحل تکرار**: دستورالعمل‌های گام‌به‌گام
3. **رفتار مورد انتظار**: چه باید اتفاق بیفتد
4. **رفتار واقعی**: چه اتفاقی واقعاً می‌افتد
5. **Screenshot**: مدارک بصری در صورت لزوم
6. **محیط**:
   - مدل دستگاه و نسخه Android
   - نسخه برنامه
   - تنظیمات مرتبط

### ایجاد Issue

از [Issues](https://github.com/mmdparsa-dev/Cheghad/issues) صفحه بازدید کنید و روی "New Issue" کلیک کنید

## درخواست ویژگی‌های جدید

ما دوست داریم از ایده‌هایی برای بهبود Cheghad بشنویم!

**قبل از ارسال:**
- بررسی کنید که ویژگی قبلاً درخواست نشده است
- در نظر بگیرید آیا با اهداف پروژه سازگار است
- راه پیاده‌سازی را فکر کنید

**شامل موارد زیر در درخواست:**
- توضیح واضح ویژگی
- موارد استفاده و منافع
- UI/UX پیشنهادی (در صورت لزوم)
- هر راهکار جایگزینی که در نظر گرفته‌اید

## تست

### اجرای تست‌ها

```bash
# اجرای تمام تست‌ها
./gradlew test

# اجرای کلاس تست خاص
./gradlew test --tests com.example.cheghad.ExampleTest

# اجرای تست‌های Instrumentation
./gradlew connectedAndroidTest
```

### پوشش تست

- هدف پوشش بالا در منطق تجاری
- استفاده از JUnit برای Unit Test
- استفاده از Espresso برای UI Test
- Mock کردن وابستگی‌های خارجی

## مستندات

- بروزرسانی README.md برای تغییرات مواجه‌شونده با کاربر
- افزودن نظرات کد برای منطق پیچیده
- بروزرسانی CHANGELOG.md
- ایجاد/بروزرسانی مستندات API در صورت نیاز

## سؤالات؟

- باز کردن یک بحث در GitHub
- بررسی Issue و PR موجود
- تماس با نگهداران

---

از مشارکت شما در Cheghad سپاسگزاریم! تلاش شما این پروژه را برای همه بهتر می‌کند. 🙌
