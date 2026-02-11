# Publishing Guide

## Prerequisites

1. GitHub Personal Access Token with `write:packages` scope
2. GitHub repository: `feruzlabs/entity-variable-storage`

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

# Set credentials
export GITHUB_ACTOR=YOUR_GITHUB_USERNAME
export GITHUB_TOKEN=YOUR_PERSONAL_ACCESS_TOKEN

# Publish
./gradlew publish -Pversion=1.0.0
```

### 4. Automated Publish (GitHub Release)

1. Open your repository on GitHub
2. Go to **Releases** → **Create a new release**
3. Choose tag: `v1.0.0`
4. Click **Publish release**
5. GitHub Actions will run and publish to GitHub Packages

### 5. Verify Publication

View published packages:

```
https://github.com/feruzlabs?tab=packages
```

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
