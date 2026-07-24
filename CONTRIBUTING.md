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

