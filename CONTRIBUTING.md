## Security checklist before commit
- [ ] No secrets in source code, config, docs, or test data.
- [ ] No tokens/keys in logs, stack traces, screenshots, or screen recordings.
- [ ] Secrets are injected only via BuildConfig, environment variables, or user-level ~/.gradle/gradle.properties.
- [ ] If anything was exposed, revoke and rotate immediately before pushing further changes.
