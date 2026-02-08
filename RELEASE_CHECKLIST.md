# Release Checklist

## Pre-Release

- [ ] All tests passing (`./gradlew test`)
- [ ] Version updated in `gradle.properties`
- [ ] CHANGELOG.md updated (if applicable)
- [ ] Documentation reviewed
- [ ] Examples tested locally

## Publishing

- [ ] Create Git tag (`git tag -a v1.0.0 -m "Release 1.0.0"`)
- [ ] Push tag to GitHub (`git push origin v1.0.0`)
- [ ] Create GitHub Release (or let workflow publish on tag push)
- [ ] Verify GitHub Actions workflow succeeded
- [ ] Verify package appears in GitHub Packages

## Post-Release

- [ ] Test installation from GitHub Packages in a fresh project
- [ ] Update README with new version (if needed)
- [ ] Announce release (if applicable)
- [ ] Increment version to next SNAPSHOT in `gradle.properties`
