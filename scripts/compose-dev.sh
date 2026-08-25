#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/secrets/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: secrets/.env not found." >&2
  echo "Expected: $ENV_FILE" >&2
  echo "Create the file before running Docker Compose." >&2
  exit 1
fi

cd "$PROJECT_ROOT"

docker compose --env-file "$ENV_FILE" "$@"
