# ===================================
# Maven Build Commands
# ===================================

.PHONY: help build clean package test test-integration run start stop restart logs status swagger setup start-all stop-all

# Default target
.DEFAULT_GOAL := help

# Color output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[0;33m
NC := \033[0m

help: ## Show this help message
	@echo "$(BLUE)Learning Platform - Available Commands:$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

build: ## Build the project (compile)
	@echo "$(BLUE)Building project...$(NC)"
	mvn clean compile

clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning project...$(NC)"
	mvn clean

package: ## Package application as JAR
	@echo "$(BLUE)Packaging application...$(NC)"
	mvn clean package -DskipTests
	@echo "$(GREEN)JAR created in target/ directory$(NC)"

test: ## Run all tests
	@echo "$(BLUE)Running tests...$(NC)"
	mvn test

test-integration: ## Run integration tests only
	@echo "$(BLUE)Running integration tests...$(NC)"
	mvn test -Dtest=**/*IntegrationTest

run: ## Run application (foreground)
	@echo "$(BLUE)Starting application on port 8081...$(NC)"
	@echo "$(YELLOW)Swagger UI: http://localhost:8081/swagger-ui.html$(NC)"
	@echo "$(YELLOW)OpenAPI Docs: http://localhost:8081/api-docs$(NC)"
	mvn spring-boot:run

start: setup ## Start application in background
	@echo "$(BLUE)Starting application in background...$(NC)"
	@nohup mvn spring-boot:run > logs/application.log 2>&1 & echo $$! > .app.pid
	@sleep 3
	@echo "$(GREEN)Application started. PID: $$(cat .app.pid)$(NC)"
	@echo "$(YELLOW)Swagger UI: http://localhost:8081/swagger-ui.html$(NC)"
	@echo "$(YELLOW)Use 'make logs' to view logs or 'make stop' to stop$(NC)"

stop: ## Stop background application
	@if [ -f .app.pid ]; then \
		PID=$$(cat .app.pid); \
		echo "$(BLUE)Stopping application (PID: $$PID)...$(NC)"; \
		kill $$PID 2>/dev/null || true; \
		rm -f .app.pid; \
		pkill -f 'spring-boot:run' || true; \
		sleep 2; \
		echo "$(GREEN)Application stopped$(NC)"; \
	else \
		echo "$(YELLOW)No PID file. Killing any Spring Boot processes...$(NC)"; \
		pkill -f 'spring-boot:run' || echo "$(YELLOW)No processes found$(NC)"; \
	fi

restart: stop start ## Restart application

logs: ## Show application logs (tail -f)
	@if [ -f logs/application.log ]; then \
		tail -f logs/application.log; \
	else \
		echo "$(YELLOW)No log file found. Is app running in background?$(NC)"; \
	fi

status: ## Check application status
	@echo "$(BLUE)Checking application status...$(NC)"
	@if [ -f .app.pid ]; then \
		PID=$$(cat .app.pid); \
		if ps -p $$PID > /dev/null 2>&1; then \
			echo "$(GREEN)Application is running (PID: $$PID)$(NC)"; \
		else \
			echo "$(YELLOW)PID file exists but process not running$(NC)"; \
		fi \
	else \
		echo "$(YELLOW)Application not running (no PID file)$(NC)"; \
	fi
	@echo ""
	@echo "$(BLUE)Port status:$(NC)"
	@netstat -tuln 2>/dev/null | grep -E ':(8080|8081)' || echo "$(YELLOW)Ports 8080/8081 are free$(NC)"

swagger: ## Open Swagger UI in browser
	@echo "$(BLUE)Opening Swagger UI...$(NC)"
	@xdg-open http://localhost:8081/swagger-ui.html 2>/dev/null || \
	 sensible-browser http://localhost:8081/swagger-ui.html 2>/dev/null || \
	 echo "$(YELLOW)Open http://localhost:8081/swagger-ui.html in browser$(NC)"

setup: ## Create necessary directories
	@mkdir -p logs

# ===================================
# Combined Commands
# ===================================

start-all: ## Start database and application (one command)
	@echo "$(BLUE)===================================$(NC)"
	@echo "$(BLUE)Starting Learning Platform$(NC)"
	@echo "$(BLUE)===================================$(NC)"
	@echo ""
	@echo "$(BLUE)[1/3] Starting PostgreSQL database...$(NC)"
	@docker-compose up -d db
	@echo "$(YELLOW)Waiting for database to be healthy...$(NC)"
	@sleep 8
	@echo "$(GREEN)Database started$(NC)"
	@echo ""
	@echo "$(BLUE)[2/3] Starting application...$(NC)"
	@mkdir -p logs
	@nohup mvn spring-boot:run > logs/application.log 2>&1 & echo $$! > .app.pid
	@sleep 12
	@if ps -p $$(cat .app.pid) > /dev/null 2>&1; then \
		echo "$(GREEN)Application started (PID: $$(cat .app.pid))$(NC)"; \
	else \
		echo "$(YELLOW)Warning: Application may be still starting, check logs$(NC)"; \
	fi
	@echo ""
	@echo "$(BLUE)[3/3] All services ready!$(NC)"
	@echo ""
	@echo "$(GREEN)===================================$(NC)"
	@echo "$(GREEN)Learning Platform is running!$(NC)"
	@echo "$(GREEN)===================================$(NC)"
	@echo ""
	@echo "$(YELLOW)Available URLs:$(NC)"
	@echo "  • Swagger UI:    $(BLUE)http://localhost:8081/swagger-ui.html$(NC)"
	@echo "  • OpenAPI Docs:  $(BLUE)http://localhost:8081/api-docs$(NC)"
	@echo "  • Database:      $(BLUE)localhost:5432$(NC)"
	@echo ""
	@echo "$(YELLOW)Useful commands:$(NC)"
	@echo "  • make logs      - View application logs"
	@echo "  • make status    - Check application status"
	@echo "  • make stop-all  - Stop everything"
	@echo "  • make help      - Show all commands"
	@echo ""

stop-all: ## Stop application and database
	@echo "$(BLUE)Stopping all services...$(NC)"
	@echo ""
	@echo "$(BLUE)[1/2] Stopping application...$(NC)"
	-@if [ -f .app.pid ]; then kill $$(cat .app.pid) 2>/dev/null; rm -f .app.pid; fi
	-@pkill -f 'spring-boot:run' 2>/dev/null
	@sleep 2
	@echo "$(GREEN)Application stopped$(NC)"
	@echo ""
	@echo "$(BLUE)[2/2] Stopping database...$(NC)"
	-@docker-compose stop db
	@echo "$(GREEN)Database stopped$(NC)"
	@echo ""
	@echo "$(GREEN)All services stopped!$(NC)"

# ===================================
# Database Commands
# ===================================

db-start: ## Start PostgreSQL database
	@echo "$(BLUE)Starting PostgreSQL database...$(NC)"
	docker-compose up -d db
	@echo "$(GREEN)Database started$(NC)"
	@echo "$(YELLOW)Waiting for database to be ready...$(NC)"
	@sleep 5

db-stop: ## Stop PostgreSQL database
	@echo "$(BLUE)Stopping PostgreSQL database...$(NC)"
	docker-compose stop db
	@echo "$(GREEN)Database stopped$(NC)"

db-logs: ## Show database logs
	docker-compose logs -f db

db-status: ## Check database status
	@docker-compose ps db

# ===================================
# Docker Commands
# ===================================

# Build Docker images
docker-build:
	docker-compose build

# Start containers in detached mode
docker-up:
	docker-compose up -d

# Stop containers
docker-down:
	docker-compose down

# View logs (follow mode)
docker-logs:
	docker-compose logs -f

# View logs for specific service
docker-logs-app:
	docker-compose logs -f app

docker-logs-db:
	docker-compose logs -f db

# Restart containers
docker-restart:
	docker-compose restart

# Stop and remove containers, networks, and volumes
docker-clean:
	docker-compose down -v

# Show running containers
docker-ps:
	docker-compose ps

# Rebuild and restart containers
docker-rebuild:
	docker-compose down
	docker-compose build --no-cache
	docker-compose up -d

# Execute bash in app container
docker-exec-app:
	docker exec -it learning-platform-app sh

# Execute psql in database container
docker-exec-db:
	docker exec -it learning-platform-db psql -U postgres -d learning_platform

# ===================================
# Combined Commands
# ===================================

# Build and start everything
docker-start: docker-build docker-up

# Full cleanup and fresh start
docker-fresh: docker-clean docker-build docker-up

.PHONY: build package test run docker-build docker-up docker-down docker-logs docker-logs-app docker-logs-db docker-restart docker-clean docker-ps docker-rebuild docker-exec-app docker-exec-db docker-start docker-fresh