# Kotlin Static Analysis (Detekt)

We use **Detekt** to maintain consistent code quality across the project.
Detekt performs static analysis on Kotlin code and helps identify potential bugs, code smells, and style issues that are not covered by ktlint.

## Commands

### 1. Run Static Analysis
Use this command to check for Detekt rule violations:
```bash
./gradlew detekt
```
This will analyze the entire codebase and report any issues such as unused parameters, long parameter lists, complexity problems, or potential bugs.

### 2. Generate Default Configuration
If you need to regenerate the Detekt configuration file:
```bash
./gradlew detektGenerateConfig
```
This creates a detekt.yml file with all available rules.
(You normally don’t need this unless you are updating or resetting the config.)

### 3. Run Detekt with Custom Config
Our project uses a custom detekt.yml.
To run Detekt explicitly with this configuration:
```bash
./gradlew detekt --config detekt.yml
```

## Integration
* **CI/CD:** These checks are automatically run in our GitHub Actions pipeline for every pull request to the `main` branch.
* **Pre-commit:** It is highly recommended to run `./gradlew detekt` before committing your changes.

## Configuration
Detekt is configured through the detekt.yml file in the project root.

## Recommended Workflow
#### 1. Write or update your code
#### 2. Run Detekt locally
```bash
./gradlew detekt
```
#### 3. Review the HTML report at
```bash
app/build/reports/detekt/detekt.html
```
#### 4. Fix any violations
#### 5. Commit your changes
#### 6. Open a pull request
#### 7. CI runs Detekt automatically
#### 8. If CI passes, your PR is considered ready to merge.  
#### Even if CI fails, the merge will still succeed, but you should fix the issues.



