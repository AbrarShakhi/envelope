#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"

ROOT_ENV_FILE="$PROJECT_ROOT/.env"
ENGINE_ENV_FILE="$PROJECT_ROOT/envelope-engine/.env"


log() {
    echo "==> $*"
}


create_root_env() {
    log "Checking root .env..."

    if [[ -f "$ROOT_ENV_FILE" ]]; then
        echo "    Root .env already exists. Skipping creation."
        return
    fi

    echo "    Creating root .env..."

    cat > "$ROOT_ENV_FILE" <<'EOF'
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_PORT=5432
DATABASE_URL=jdbc:postgresql://localhost:{POSTGRES_PORT}/{POSTGRES_DB}
JWT_SECRET_KEY=
PRELOGIN_PSEUDO_SALT_SECRET=
ACCOUNT_LOCKOUT_MAX_ATTEMPTS=
ACCOUNT_LOCKOUT_DURATION_SECONDS=
EOF

    echo "    Created: $ROOT_ENV_FILE"
}


create_symlink() {
    local source="$1"
    local target="$2"

    echo "    Configuring symlink..."
    echo "    Source: $source"
    echo "    Target: $target"

    if [[ -L "$target" ]]; then
        local current_target
        current_target="$(readlink "$target")"

        if [[ "$current_target" == "$source" ]]; then
            echo "    Symlink already points to $source. Skipping."
        else
            echo "    Existing symlink points to: $current_target"
            echo "    Updating symlink..."

            ln -sfn "$source" "$target"

            echo "    Symlink updated."
        fi

        return
    fi

    if [[ -e "$target" ]]; then
        echo "    Target already exists as a regular file."
        echo "    Removing: $target"

        rm "$target"
    fi

    echo "    Creating symlink..."

    ln -s "$source" "$target"

    echo "    Symlink created."
}


show_summary() {
    echo
    log "Development environment initialized successfully."
    echo
    echo "    Root:   $ROOT_ENV_FILE"
    echo "    Engine: $ENGINE_ENV_FILE -> ../.env"
    echo
    echo "    Both paths use the same .env file."
}


main() {
    log "Initializing development environment..."
    echo
    echo "    Project root: $PROJECT_ROOT"
    echo "    Root .env:    $ROOT_ENV_FILE"
    echo "    Engine .env:  $ENGINE_ENV_FILE"
    echo

    create_root_env
    echo

    create_symlink "../.env" "$ENGINE_ENV_FILE"

    show_summary
}


main "$@"
