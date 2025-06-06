# Environment variables
ENV ?= dev
COMPOSE=docker-compose -f compose.$(ENV).yml

# Default target
.DEFAULT_GOAL := help

# Help command
help:
	@echo "Available commands:"
	@echo "  make up          - Start the containers"
	@echo "  make down        - Stop the containers"
	@echo "  make logs        - View all container logs"
	@echo "  make logs SERVICE=<service> - View logs for specific service"
	@echo "  make ps          - List running containers"
	@echo "  make build       - Build the containers"
	@echo "  make rebuild     - Rebuild and restart containers"
	@echo "  make clean       - Stop containers and remove volumes"
	@echo "  make volume-prune - Remove all unused volumes"
	@echo "  make restart     - Restart all containers"
	@echo "  make status      - Show detailed container status"
	@echo "  make shell       - Open shell in backend container"
	@echo "  make mongo-shell - Open MongoDB shell"
	@echo "  make redis-cli   - Open Redis CLI"
	@echo "  make test        - Run tests"
	@echo "  make lint        - Run code linting"
	@echo ""
	@echo "Environment options:"
	@echo "  ENV=dev  - Use development environment (default)"
	@echo "  ENV=prod - Use production environment"
	@echo ""
	@echo "Example: make up ENV=prod"
	@echo ""
	@echo "Available services for logs:"
	@echo "  - spring-boot-backend"
	@echo "  - mongodb"
	@echo "  - redis-master"
	@echo "  - redis-slave-1"
	@echo "  - redis-slave-2"
	@echo "  - redis-sentinel-1"
	@echo "  - redis-sentinel-2"
	@echo "  - redis-sentinel-3"
	@echo ""
	@echo "Example: make logs SERVICE=spring-boot-backend ENV=prod"

# Container management
up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down

logs:
	@if [ "$(SERVICE)" = "" ]; then \
		$(COMPOSE) logs -f; \
	else \
		$(COMPOSE) logs -f $(SERVICE); \
	fi

ps:
	$(COMPOSE) ps

build:
	$(COMPOSE) build

rebuild: down build up

clean:
	$(COMPOSE) down -v --remove-orphans

volume-prune:
	docker volume prune -f

restart:
	$(COMPOSE) restart

status:
	$(COMPOSE) ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

# Development tools
shell:
	$(COMPOSE) exec spring-boot-backend /bin/sh

mongo-shell:
	$(COMPOSE) exec mongodb mongosh

redis-cli:
	$(COMPOSE) exec redis-master redis-cli

# Testing and quality
test:
	mvn test

lint:
	mvn checkstyle:check

# Environment setup
setup-dev:
	cp src/main/resources/dev-env.properties.example src/main/resources/dev-env.properties
	$(COMPOSE) build

setup-prod:
	cp src/main/resources/prod-env.properties.example src/main/resources/prod-env.properties
	$(COMPOSE) build

# Cleanup
cleanup: down volume-prune
	@echo "Cleaned up all containers and volumes"
