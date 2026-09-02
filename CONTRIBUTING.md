Contributing Guide

Thank you for contributing to this project.

Please follow the workflow below when making changes to the project.

1. Do not work directly on main

The main branch is the stable branch.

Do not make changes directly on main.

Always create a new branch for your work.

Examples:

git checkout main
git pull origin main

git checkout -b feature/user-login


Use these branch naming conventions:

feature/<name> — new functionality
fix/<name> — bug fixes
refactor/<name> — code restructuring
docs/<name> — documentation changes

Examples:

feature/user-login
feature/payment-system
fix/login-error
refactor/auth-service
docs/api-guide

2. Keep your branch focused

One branch should generally contain one feature, bug fix, or related change.

Avoid combining unrelated changes in the same branch.

Good:

feature/user-login


Not recommended:

feature/login-payment-dashboard-everything

3. Keep your branch up to date

Before starting work, make sure your local main is up to date:

git checkout main
git pull origin main


Then create your feature branch.

If main changes while you are working, update your branch before opening or merging your Pull Request.

4. Write meaningful commits

Write commit messages that clearly describe what changed.

Good:

Add user login endpoint
Fix password validation
Add loading state to login form


Avoid:

update
changes
fix
asdf
final
final2

5. Test your changes

Before opening a Pull Request:

Run the project's tests.
Run the linter, if available.
Make sure the application builds successfully.
Test the feature you changed.
Make sure you did not introduce unrelated changes.

Do not open a Pull Request with known failing tests unless you clearly explain why.

6. Open a Pull Request

When your work is ready:

your-branch → main


Open a Pull Request on GitHub.

The Pull Request should explain:

What changed?

Describe the changes you made.

Why?

Explain the reason for the change.

How was it tested?

Explain how you tested the changes.

Example:

## What changed

- Added user login endpoint
- Added email/password validation
- Added login error handling

## Why

Users need to be able to log into their accounts.

## Testing

- Tested valid login
- Tested invalid password
- Tested non-existent user
- Ran all existing tests

7. Pull Request review

Do not merge your own Pull Request unless the project owner explicitly asks you to do so.

The project owner will review the Pull Request.

The owner may:

Approve the Pull Request
Request changes
Ask questions
Request additional tests
Merge the Pull Request

Please address review comments before the Pull Request is merged.

8. After your Pull Request is merged

After the Pull Request has been merged, delete your feature branch if it is no longer needed.

Then update your local main:

git checkout main
git pull origin main


Create a new branch for your next task.

9. Do not commit secrets

Never commit:

Passwords
API keys
Access tokens
Private keys
Production credentials
.env files containing secrets

Use .env.example for documenting required environment variables.

Example:

.env.example

DATABASE_URL=
API_KEY=
JWT_SECRET=


The actual .env file should remain local and should normally be included in .gitignore.

10. Keep Pull Requests small

Smaller Pull Requests are easier to review and less likely to introduce problems.

Prefer:

PR #1 — Add user model
PR #2 — Add authentication API
PR #3 — Add login UI


over one very large Pull Request containing everything.

11. Communication

For large changes, discuss the approach before starting implementation.

If you are unsure about something, open an issue or discuss it with the project owner before making a large architectural change.

12. Golden rule

The standard workflow is:

Issue / Task
     ↓
Create branch
     ↓
Make changes
     ↓
Test
     ↓
Commit
     ↓
Push branch
     ↓
Open Pull Request
     ↓
Code review
     ↓
Address feedback
     ↓
Approval
     ↓
Merge into main
     ↓
Delete branch

Important

main is the stable branch.

Do not push directly to main.

All normal changes should go through a Pull Request and review.
