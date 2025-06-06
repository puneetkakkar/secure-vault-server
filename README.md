<p align="center">
  <img src="./src/main/resources/static/images/secure-vault-logo-3x.png" alt="secure vault brand" width="400"/>
</p>

<h1 align="center">SECURE VAULT - THE ONLY KEY IS YOU</h1>

<br>

**Secure Vault** is a privacy-focused application designed to safeguard your passwords, documents, and other confidential information. It's built with end-to-end encryption, ensuring that only you have access to your data. We never have access to your passwords or sensitive information.

## ⚗️ Key Features:

- **End-to-End Encryption:** Your data is encrypted on your device and remains encrypted on our servers, ensuring only you can access it.
- **Master Password Security:** Your master password is the only key to unlocking your vault.
- **Secure Storage:** Keep all your valuable data safe and organized in one place.
- **Secure Sharing:** Share your information securely with trusted individuals.

## Why Choose Secure Vault?

- **Ultimate Privacy:** Your data is your business. We don't have access to it.
- **Peace of Mind:** Securely store and manage your most important information.
- **Easy to Use:** A user-friendly interface makes it simple to manage your vault.
- **Open Source:** We welcome community contributions to make Secure Vault even better.

## Getting Started:

### Prerequisites:

- **Java Development Kit (JDK):** Ensure you have a compatible JDK installed (version 11 or higher).
- **Maven:** Make sure you have Apache Maven installed.
- **Docker and Docker Compose:** Required for running the application in containers.
- **IDE:** You'll need an IDE of your choice (e.g., IntelliJ IDEA, Eclipse, VS Code) to work with the project.

### Development Setup:

#### Option 1: Local Development

1. **Clone the repository:**

   ```bash
   git clone https://github.com/puneetkakkar/secure-vault-server
   ```

2. **Navigate to the project directory:**

   ```bash
   cd secure-vault-server
   ```

3. **Install dependencies:**

   ```bash
   mvn clean install
   ```

4. **Build the application:**

   ```bash
   mvn package
   ```

5. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

The Secure Vault application will be accessible at http://localhost:8080 by default.

#### Option 2: Docker Development Setup

1. **Setup development environment:**

   ```bash
   make setup-dev
   ```

2. **Start the development containers:**

   ```bash
   make up
   ```

3. **Verify the services:**
   ```bash
   make ps
   ```

The development stack includes:

- Spring Boot Backend (port 8080)
- MongoDB (port 27017)
- Redis (port 6379)

### Production Setup with Docker:

1. **Setup production environment:**

   ```bash
   make setup-prod
   ```

2. **Start the production containers:**

   ```bash
   make up ENV=prod
   ```

3. **Verify the services:**
   ```bash
   make ps ENV=prod
   ```

The production stack includes:

- Spring Boot Backend (port 8080)
- MongoDB (port 27018)
- Redis Master (port 6382)
- Redis Slaves (ports 6383, 6384)
- Redis Sentinels (ports 26382, 26383, 26384)

### Using Make Commands

The project includes a Makefile with various helpful commands. To see all available commands:

```bash
make help
```

Common commands:

- `make up` - Start containers
- `make down` - Stop containers
- `make logs` - View container logs
- `make ps` - List running containers
- `make shell` - Open shell in backend container
- `make mongo-shell` - Open MongoDB shell
- `make redis-cli` - Open Redis CLI
- `make test` - Run tests
- `make lint` - Run code linting
- `make clean` - Stop containers and remove volumes

For production environment, add `ENV=prod` to any command:

```bash
make up ENV=prod
make logs ENV=prod
```

### Architecture:

The application uses a microservices architecture with:

#### Development Environment:

- **Spring Boot Backend:** Main application server
- **MongoDB:** Primary database for data storage
- **Redis:** Single instance for caching and session management

#### Production Environment:

- **Spring Boot Backend:** Main application server
- **MongoDB:** Primary database for data storage
- **Redis Cluster:**
  - Master-Slave replication (1 master, 2 slaves)
  - Sentinel-based high availability (3 sentinels)
  - Automatic failover support

### Environment Configuration:

Both development and production environments require proper configuration through environment variables. The main differences are:

1. **Development (`compose.dev.yml`):**

   - Single Redis instance
   - Standard MongoDB port
   - Development-specific Spring profiles
   - Hot-reload enabled for development

2. **Production (`compose.prod.yml`):**
   - Redis cluster with master-slave replication
   - Redis sentinel for high availability
   - Custom MongoDB port
   - Production-specific Spring profiles
   - Optimized for performance and security

## Contributing

We encourage you to contribute to Secure Vault's development!

- **Report issues:** Find a bug or have a suggestion? Please submit an issue on GitHub.
- **Submit pull requests:** Want to add a feature or fix a bug? Submit a pull request to our repository.
- **Join the community:** Connect with other developers.

**Before contributing:**

- Please read our [Contributing Guidelines](CONTRIBUTING.md) for a detailed guide.
- Ensure you have signed our Contributor License Agreement (CLA).

## License

This project is licensed under the [License Name] License - see the [LICENSE](LICENSE) file for details.

**Together, let's make Secure Vault the ultimate solution for protecting your digital secrets!**
