# Issue tracker: GitHub

The SRS and SDS in `docs/` are the authoritative specifications. Implementation tickets live in GitHub Issues for Dino-Yoshi/PioneerDelivery. Use `gh` from this repository.

## Conventions

- Create issues with `gh issue create --title ... --body-file ...`.
- Read issues with `gh issue view <number> --json number,title,body,labels,comments`.
- List issues with `gh issue list`, using state and label filters as needed.
- Apply labels with `gh issue edit <number> --add-label ...`.
- Label implementation-ready tickets `ready-for-agent`; create that label if it is missing.
- Link related implementation tickets with issue references or sub-issues where useful.
- When a skill says to publish or fetch a ticket, use the corresponding GitHub issue.
- PRs as a request surface: no.
