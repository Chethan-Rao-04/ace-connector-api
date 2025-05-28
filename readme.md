# ACE Connector API

A Spring Boot application that provides a REST API for interacting with the ACE pseudonymization service.

## Overview

The ACE Connector API serves as a client-side interface to the ACE pseudonymization service, providing endpoints for:

- Domain management (create, read, update, delete)
- Pseudonym operations (creation, retrieval, update, deletion)
- Health check endpoints

## Requirements

- Java 17 or later
- Maven 3.6+
- Keycloak authentication server
- ACE pseudonymization service

## Getting Started

### Configuration

Configure the application in `application.properties`:

```properties
# Server configuration
server.port=8083

# Pseudonymization service configuration
pseudonymization.service-url=http://localhost:8080
pseudonymization.keycloak-url=http://localhost:8081
pseudonymization.realm=development
pseudonymization.client-id=ace
pseudonymization.client-secret=your-client-secret
pseudonymization.username=test
pseudonymization.password=your-password
```

Run the application using Maven:

```bash
mvn spring-boot:run
```

## API Endpoints

### Health Check Endpoints

- `GET /api/domains/domain-health-check` - Check domain API status
- `GET /api/pseudonyms/pseudo-health-check` - Check pseudonymization API status

### Domain Management

- `GET /api/domains` - Get all domains
- `GET /api/domains/{domainName}` - Get a specific domain
- `POST /api/domains` - Create a new domain
- `PUT /api/domains/{domainName}` - Update a domain
- `DELETE /api/domains/{domainName}` - Delete a domain
- `GET /api/domains/hierarchy` - Get domain hierarchy

### Pseudonym Operations

- `POST /api/pseudonyms/{domainName}` - Create a pseudonym
- `GET /api/pseudonyms/{domainName}/by-id` - Get pseudonym by identifier
- `GET /api/pseudonyms/{domainName}/by-psn` - Get pseudonym by pseudonym value
- `PUT /api/pseudonyms/{domainName}/by-id` - Update pseudonym by identifier
- `PUT /api/pseudonyms/{domainName}/by-psn` - Update pseudonym by pseudonym value
- `DELETE /api/pseudonyms/{domainName}` - Delete pseudonym

## Testing the API

### Health check

Use `curl` to test the health check endpoints:

```bash
curl -X GET localhost:8083/api/domains/domain-health-check
curl -X GET localhost:8083/api/pseudonyms/pseudo-health-check
```

### Test the Domain endpoints

```bash
### Get all domains
curl -X GET localhost:8083/api/domains
```

```bash
### Get all domains with detailed information
curl -X GET "localhost:8083/api/domains?detailed=true"
```

```bash
### Get a specific domain
curl -X GET localhost:8083/api/domains/example-domain
```

```bash
### Get a specific domain with detailed information
curl -X GET "localhost:8083/api/domains/example-domain?detailed=true"
```

```bash
### Create a new domain
curl -X POST localhost:8083/api/domains \
-H "Content-Type: application/json" \
-d '{
"name": "new-domain",
"displayName": "New Domain",
"description": "My new domain for testing",
"validFrom": "2023-01-01T00:00:00.000Z",
"validTo": "2025-12-31T23:59:59.999Z"
}'
```

```bash
### Update a domain
curl -X PUT localhost:8083/api/domains/example-domain \
-H "Content-Type: application/json" \
-d '{
"name": "example-domain",
"displayName": "Updated Domain",
"description": "Updated domain description",
"validFrom": "2023-01-01T00:00:00.000Z",
"validTo": "2025-12-31T23:59:59.999Z"
}'
```

```bash
### Delete a domain
curl -X DELETE localhost:8083/api/domains/example-domain
```bash
### Delete a domain with force option
curl -X DELETE "localhost:8083/api/domains/example-domain?force=true"
```

```bash
### Get domain hierarchy
curl -X GET localhost:8083/api/domains/hierarchy
```

## Architecture

The application consists of:

- **Controllers**: Handle HTTP requests and responses
- **DTOs**: Transfer data between client and server
- **Service Connectors**: Communicate with the ACE pseudonymization service
- **Configuration**: Manage application properties and beans