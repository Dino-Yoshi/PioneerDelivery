# Pioneer Delivery Coding Style Guide

This guide remains the supporting implementation standard for the accepted SRS/SDS v1.0 baseline.

## Functions

- Use descriptive `camelCase` names with no underscores.
- Give arguments meaningful names, using `camelCase` where appropriate and no underscores.
- Keep conditions on a single line; do not wrap them across lines.

## Variables

- Use descriptive `camelCase` names with no underscores.
- Keep each variable declaration on one line.

## Comments

- Place comments on their own lines directly above the code they describe. Never put comments beside code.
- Multiline block comments may appear above function declarations.
- Inside functions, generally use brief `//` comments of one or two lines.
- Larger blocks of code, such as loops with nested instructions, are an exception: use a multiline block comment above the block when it explains the logic more clearly than separate comments on individual instructions. Use judgment to choose between a short single-line comment and a multiline summary, keeping either concise.
