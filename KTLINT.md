# Kotlin Linting (ktlint)

We use **ktlint** to ensure a consistent coding style across the project. It automatically checks and formats Kotlin code to follow the standard Android style guide.

## Commands

### 1. Check for Style Violations
Run this command to see if there are any linting errors in your code:
```bash
./gradlew ktlintCheck
```

### 2. Auto-Fix Style Issues
Run this command to automatically format your code and fix most style violations:
```bash
./gradlew ktlintFormat
```

## Integration
* **CI/CD:** These checks are automatically run in our GitHub Actions pipeline for every pull request to the `main` branch.
* **Pre-commit:** It is highly recommended to run `./gradlew ktlintFormat` before committing your changes.
