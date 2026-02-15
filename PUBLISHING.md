# Publishing Guide

## Maven ga yuklash — qisqacha

| Maqsad | Qilish kerak |
|--------|----------------|
| **Lokal Maven** (~/.m2) | `./gradlew publishToMavenLocal` — hech qanday credential kerak emas |
| **GitHub Packages** | `ACTOR` (GitHub username) va `TOK` (PAT) o‘rnating, keyin `./gradlew publish -Pversion=1.0.0` |
| **Maven Central** | Sonatype (OSSRH) account + GPG signing; `OSSRH_USERNAME`, `OSSRH_TOKEN`, `SIGNING_KEY`, `SIGNING_PASSWORD` o‘rnating |

Batafsil: quyidagi bo‘limlar va [.github/workflows/publish.yml](.github/workflows/publish.yml).

---

## Repositories

- **GitHub Packages** – Maven format, works for both Maven and Gradle
- **Maven Central** – Optional, requires Sonatype account and GPG signing

## Prerequisites

1. GitHub Personal Access Token with `write:packages` scope
2. GitHub repository: `feruzlabs/entity-variable-storage`
3. (Maven Central) Sonatype OSSRH account and GPG key

## Publishing Process

### 1. Update Version

Edit **gradle.properties**:

```properties
version=1.0.0
```

### 2. Create Git Tag

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 3. Manual Publish (Local)

```bash
# Ensure Gradle wrapper exists
gradle wrapper --gradle-version 8.5

# GitHub Packages uchun credentials (build.gradle ACTOR va TOK ishlatadi)
export ACTOR=YOUR_GITHUB_USERNAME
export TOK=YOUR_PERSONAL_ACCESS_TOKEN

# Publish (GitHub Packages ga; Sonatype faqat OSSRH_* o'rnatilganda)
./gradlew publish -Pversion=1.0.0
```

### 4. Automated Publish (GitHub Release)

1. Open your repository on GitHub
2. Go to **Releases** → **Create a new release**
3. Choose tag: `v1.0.0`
4. Click **Publish release**
5. GitHub Actions will run and publish to GitHub Packages

### 5. Publish to Maven Central (Optional)

1. Add repository variable: `PUBLISH_MAVEN_CENTRAL=true`
2. Add secrets: `OSSRH_USERNAME`, `OSSRH_TOKEN`, `SIGNING_KEY`, `SIGNING_PASSWORD`
3. GitHub Actions will run `publishToSonatype closeAndReleaseSonatypeStagingRepository` when credentials are set

Local Maven Central publish:

```bash
export OSSRH_USERNAME=your-sonatype-username
export OSSRH_TOKEN=your-sonatype-token
export SIGNING_KEY=your-base64-gpg-private-key
export SIGNING_PASSWORD=your-gpg-passphrase
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository -Pversion=1.0.0
```

### 6. Verify Publication

View published packages:

- GitHub Packages: https://github.com/feruzlabs?tab=packages
- Maven Central: https://central.sonatype.com/

## Version Strategy

- **Releases**: `1.0.0`, `1.1.0`, `2.0.0`
- **Snapshots**: `1.0.0-SNAPSHOT` (auto-published on main/develop branch)
- **Release candidates**: `1.0.0-RC1`, `1.0.0-RC2`

## Workflow Triggers

| Trigger            | Version source              |
|--------------------|-----------------------------|
| GitHub Release     | Tag name (e.g., v1.0.0)     |
| Push to main       | `gradle.properties`         |
| Push to develop    | `gradle.properties` + SNAPSHOT |
| Manual dispatch    | Workflow input or SNAPSHOT  |

## Troubleshooting

### Authentication Failed

Check that your token has the required scopes:

- `read:packages`
- `write:packages`
- `delete:packages` (optional)

### Package Already Exists

Existing versions cannot be overwritten. Increment the version number and publish again.

### Gradle Wrapper Missing

If `./gradlew` does not exist:

```bash
gradle wrapper --gradle-version 8.5
```

Then commit the generated `gradlew`, `gradlew.bat`, and `gradle/wrapper/` files.
