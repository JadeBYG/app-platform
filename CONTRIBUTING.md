# Contributing

## Commit Convention

Use the following prefixes for commit messages:

- `feat:` new feature or behavior change
- `infra:` deployment, CI/CD, Docker, AWS, environment configuration
- `docs:` documentation updates

Examples:

- `feat: add application status metrics for prometheus`
- `infra: add github actions workflow for ecs deployment`
- `docs: add day8 deployment verification steps`

## CI/CD Secrets (GitHub Repository Settings)

Add these repository secrets before running the workflow:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

The workflow deploys to:

- Region: `us-east-2`
- ECR repository: `app-platform`
- ECS cluster: `app-platform-cluster`
- ECS service: `app-platform-svc`
