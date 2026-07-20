# Security Policy

## Secret handling policy
- Never commit API keys, tokens, passwords, signing keys, or other credentials.
- Use environment variables, local untracked config, or secure CI/CD secrets.
- Treat screenshots/logs/docs as sensitive if they can expose secrets.

## Incident response (do this first)
1. Revoke/disable exposed secret immediately.
2. Rotate/regenerate replacement credential.
3. Update dependent systems/apps with new secret.
4. Remove leaked material from repository files.
5. If already committed, rewrite history and force-push with coordination.
6. Document incident scope and remediation.

## History rewrite reminder
- Deleting a file in a new commit does not remove secret exposure from history.
- Use history rewrite tooling when needed and notify collaborators to rebase/reset.

## Disclosure/reporting
- Privately report vulnerabilities to maintainers (do not open a public issue with exploit details).
- Include impact, reproduction steps, affected versions/areas, and suggested fix if known.
