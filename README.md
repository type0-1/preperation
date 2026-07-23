# Technical Design: Real-Time Team Task Board

## 1. Overview

The Real-Time Team Task Board is a small web application that allows users to create tasks, assign them to team members, move them through workflow stages, and see updates without refreshing the page.

The project is designed to refresh and practise:

* Java backend development
* Spring Boot fundamentals
* Angular
* Node.js
* REST API design
* WebSockets
* SQL and persistence
* testing and debugging
* basic system design

The application is deliberately small. It should feel complete without requiring microservices, cloud infrastructure, payments, or complicated authentication.

## 2. Goals

The system should allow a user to:

* create and view a board
* create tasks
* edit task details
* assign tasks to users
* move tasks between workflow columns
* see task updates in real time
* view a small activity feed
* filter tasks by assignee or priority

The main goal is to build a polished end-to-end application using Java, Node.js, and Angular.

## 3. Non-goals

The first version will not include:

* multiple organizations
* fine-grained permissions
* file attachments
* comments
* email notifications
* production-grade authentication
* mobile applications
* offline support
* microservices
* Kubernetes
* Kafka
* distributed databases

## 4. Technology Stack

### Frontend

Angular will provide the user interface.

Responsibilities:

* board view
* task cards
* drag-and-drop movement
* task creation and editing forms
* filtering
* activity feed
* real-time update handling
* loading and error states

Suggested Angular features:

* Angular Router
* Reactive Forms
* HttpClient
* RxJS
* Angular CDK drag-and-drop

### Core backend

Java with Spring Boot will own the application’s data and business rules.

Responsibilities:

* REST APIs
* task validation
* task state transitions
* persistence
* transaction handling
* activity recording
* user and board management
* authoritative task state

Suggested Java technologies:

* Spring Boot
* Spring Web
* Spring Data JPA
* Bean Validation
* PostgreSQL or H2
* JUnit
* Mockito

### Real-time gateway

Node.js will provide WebSocket connections between the backend and Angular clients.

Responsibilities:

* maintain WebSocket connections
* group users by board
* receive task-change events from the Java backend
* broadcast updates to connected Angular clients
* handle reconnects and disconnected clients

Suggested Node.js technologies:

* TypeScript
* Express
* Socket.IO

The Java backend remains the source of truth. Node.js only distributes notifications.

## 5. High-Level Architecture

```text
┌─────────────────┐
│ Angular Client  │
└────────┬────────┘
         │ REST
         ▼
┌─────────────────┐
│ Java Backend    │
│ Spring Boot     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ PostgreSQL      │
└─────────────────┘

Java Backend
      │
      │ internal event request
      ▼
┌─────────────────┐
│ Node.js Gateway │
│ Socket.IO       │
└────────┬────────┘
         │ WebSocket
         ▼
 Angular Clients
```

All writes go through the Java backend.

The Node.js gateway must never modify task data directly.

## 6. Core Domain Model

### User

```text
User
- id: UUID
- displayName: string
- email: string
- createdAt: timestamp
```

Authentication can be simplified for the first version. The frontend may allow the user to select one of several seeded users.

### Board

```text
Board
- id: UUID
- name: string
- description: string
- createdAt: timestamp
- updatedAt: timestamp
```

The first version may contain only one board.

### Task

```text
Task
- id: UUID
- boardId: UUID
- title: string
- description: string
- status: TaskStatus
- priority: TaskPriority
- assigneeId: UUID nullable
- version: long
- createdAt: timestamp
- updatedAt: timestamp
```

### TaskStatus

```text
TODO
IN_PROGRESS
DONE
```

### TaskPriority

```text
LOW
MEDIUM
HIGH
```

### Activity

```text
Activity
- id: UUID
- boardId: UUID
- taskId: UUID nullable
- actorId: UUID
- type: ActivityType
- description: string
- createdAt: timestamp
```

### ActivityType

```text
TASK_CREATED
TASK_UPDATED
TASK_MOVED
TASK_ASSIGNED
TASK_DELETED
```

## 7. Functional Requirements

### 7.1 View board

The user can open the main board and see three columns:

* To Do
* In Progress
* Done

Each column displays its tasks.

Each task card displays:

* title
* priority
* assignee
* last-updated time

### 7.2 Create task

The user can create a task with:

* title
* description
* priority
* optional assignee

New tasks start in `TODO`.

Validation rules:

* title is required
* title must contain between 3 and 100 characters
* description must not exceed 1,000 characters
* priority is required

### 7.3 Edit task

The user can update:

* title
* description
* priority
* assignee

The user cannot directly edit:

* id
* board id
* creation time
* version

### 7.4 Move task

The user can move a task between workflow columns using drag and drop.

Supported transitions:

```text
TODO → IN_PROGRESS
IN_PROGRESS → TODO
IN_PROGRESS → DONE
DONE → IN_PROGRESS
```

The first version should reject direct movement from `TODO` to `DONE`.

### 7.5 Delete task

The user can delete a task after confirming the action.

Deletion should create an activity entry.

Hard deletion is acceptable for this project.

### 7.6 Filter tasks

The user can filter visible tasks by:

* assignee
* priority

Filtering may happen entirely in Angular after the board data is loaded.

### 7.7 Activity feed

The board displays the 20 most recent activity records.

Example entries:

```text
Alex created "Implement login form"
Jamie moved "Fix validation bug" to Done
Alex assigned "Add error handling" to Morgan
```

### 7.8 Real-time updates

When one user changes a task, other users viewing the same board should receive the update.

Supported event types:

```text
task.created
task.updated
task.moved
task.deleted
activity.created
```

Clients should update their local board state after receiving an event.

## 8. API Design

Base path:

```text
/api
```

### Boards

#### Get board

```http
GET /api/boards/{boardId}
```

Response:

```json
{
  "id": "0ad3c184-43fc-43ad-bd31-956204d0b390",
  "name": "Graduate Prep Board",
  "description": "Tasks for the next two weeks"
}
```

### Tasks

#### List board tasks

```http
GET /api/boards/{boardId}/tasks
```

Response:

```json
[
  {
    "id": "cbe380f9-85ab-4300-a5ed-d86af8686d81",
    "title": "Build Angular task form",
    "description": "Create a reactive form with validation",
    "status": "TODO",
    "priority": "HIGH",
    "assigneeId": "ed389422-d555-4421-bd5a-b715008a995f",
    "version": 1,
    "createdAt": "2026-07-23T10:00:00Z",
    "updatedAt": "2026-07-23T10:00:00Z"
  }
]
```

#### Get task

```http
GET /api/tasks/{taskId}
```

#### Create task

```http
POST /api/boards/{boardId}/tasks
```

Request:

```json
{
  "title": "Build Angular task form",
  "description": "Create a reactive form with validation",
  "priority": "HIGH",
  "assigneeId": "ed389422-d555-4421-bd5a-b715008a995f"
}
```

Response:

```http
201 Created
```

#### Update task

```http
PUT /api/tasks/{taskId}
```

Request:

```json
{
  "title": "Build and test Angular task form",
  "description": "Create a reactive form with validation",
  "priority": "HIGH",
  "assigneeId": "ed389422-d555-4421-bd5a-b715008a995f",
  "version": 1
}
```

The request includes the version so that concurrent updates can be detected.

#### Move task

```http
PATCH /api/tasks/{taskId}/status
```

Request:

```json
{
  "status": "IN_PROGRESS",
  "version": 1
}
```

#### Delete task

```http
DELETE /api/tasks/{taskId}
```

Response:

```http
204 No Content
```

### Users

#### List users

```http
GET /api/users
```

### Activity

#### List recent board activity

```http
GET /api/boards/{boardId}/activities?limit=20
```

## 9. Error Handling

The backend should use a consistent error response.

```json
{
  "code": "INVALID_TASK_TRANSITION",
  "message": "A task cannot move directly from TODO to DONE",
  "timestamp": "2026-07-23T10:15:00Z"
}
```

Suggested HTTP status codes:

```text
400 Bad Request
Invalid input or invalid state transition

404 Not Found
Board, task, or user does not exist

409 Conflict
Task was modified by another request

500 Internal Server Error
Unexpected failure
```

Angular should display readable errors rather than raw backend responses.

## 10. Concurrency

The application should use optimistic locking for task updates.

The `Task` entity should contain a version field.

Example:

```java
@Version
private Long version;
```

When two users update the same task concurrently:

1. Both clients read task version 3.
2. User A submits an update using version 3.
3. The backend saves the task as version 4.
4. User B submits an update using version 3.
5. The backend rejects the second update.
6. The API returns `409 Conflict`.
7. Angular reloads the current task and informs the user.

This is enough concurrency handling for the project.

## 11. Real-Time Event Flow

When a task is changed:

1. Angular sends a REST request to Java.
2. Java validates and saves the change.
3. Java commits the database transaction.
4. Java sends an event to the Node.js gateway.
5. Node.js broadcasts the event to clients subscribed to the board.
6. Angular updates its local state.

Example event:

```json
{
  "type": "task.moved",
  "boardId": "0ad3c184-43fc-43ad-bd31-956204d0b390",
  "task": {
    "id": "cbe380f9-85ab-4300-a5ed-d86af8686d81",
    "status": "IN_PROGRESS",
    "version": 2,
    "updatedAt": "2026-07-23T10:20:00Z"
  }
}
```

For the first version, Java may send events to Node.js using a simple internal HTTP endpoint.

```http
POST /internal/events
```

This is not perfectly reliable, but it is acceptable for a learning project.

If event delivery fails, the database remains correct. Users can refresh the board to recover the latest state.

## 12. Java Backend Structure

Suggested package structure:

```text
com.example.taskboard
├── board
│   ├── BoardController
│   ├── BoardService
│   ├── BoardRepository
│   └── Board
├── task
│   ├── TaskController
│   ├── TaskService
│   ├── TaskRepository
│   ├── Task
│   ├── TaskStatus
│   └── TaskPriority
├── activity
│   ├── ActivityController
│   ├── ActivityService
│   ├── ActivityRepository
│   └── Activity
├── user
│   ├── UserController
│   ├── UserService
│   ├── UserRepository
│   └── User
├── realtime
│   └── RealtimeEventPublisher
├── common
│   ├── ApiExceptionHandler
│   ├── ApiError
│   └── NotFoundException
└── config
```

Controllers should contain HTTP-specific logic.

Services should contain business logic.

Repositories should contain persistence logic.

Entities should not depend on controllers or API request classes.

## 13. Angular Structure

Suggested structure:

```text
src/app
├── core
│   ├── api
│   ├── models
│   └── websocket
├── board
│   ├── board-page
│   ├── board-column
│   ├── task-card
│   ├── task-form
│   ├── activity-feed
│   └── board.service.ts
├── shared
│   ├── loading-spinner
│   ├── error-message
│   └── confirm-dialog
└── app.routes.ts
```

Suggested components:

```text
BoardPageComponent
BoardColumnComponent
TaskCardComponent
TaskFormComponent
ActivityFeedComponent
TaskFilterComponent
```

The `BoardService` should:

* load tasks
* create tasks
* update tasks
* move tasks
* delete tasks
* expose board state using RxJS

The WebSocket service should:

* connect to Node.js
* subscribe to a board
* reconnect after disconnection
* expose incoming events as an observable

## 14. Node.js Structure

Suggested structure:

```text
src
├── server.ts
├── socket
│   ├── connection-handler.ts
│   └── board-rooms.ts
├── events
│   ├── event-controller.ts
│   └── event-types.ts
└── middleware
    └── error-handler.ts
```

Node.js should expose:

```http
POST /internal/events
```

The endpoint should validate that the event contains:

* event type
* board ID
* event payload

It should then broadcast the event to the appropriate Socket.IO room.

Clients join a room using:

```text
board:{boardId}
```

## 15. Database Schema

Example tables:

```sql
users
- id UUID primary key
- display_name varchar(100) not null
- email varchar(255) not null unique
- created_at timestamp not null

boards
- id UUID primary key
- name varchar(100) not null
- description varchar(500)
- created_at timestamp not null
- updated_at timestamp not null

tasks
- id UUID primary key
- board_id UUID not null
- title varchar(100) not null
- description varchar(1000)
- status varchar(30) not null
- priority varchar(20) not null
- assignee_id UUID null
- version bigint not null
- created_at timestamp not null
- updated_at timestamp not null

activities
- id UUID primary key
- board_id UUID not null
- task_id UUID null
- actor_id UUID not null
- type varchar(30) not null
- description varchar(500) not null
- created_at timestamp not null
```

Foreign keys should be used between related records.

## 16. Testing Strategy

### Java unit tests

Test:

* task creation validation
* valid state transitions
* invalid state transitions
* assignment rules
* activity creation
* handling missing tasks

### Java integration tests

Test:

* REST request and response behaviour
* persistence
* optimistic locking
* transaction rollback
* error responses

Use Spring Boot test support and an in-memory database or test container.

### Angular tests

Test:

* form validation
* task rendering
* filters
* service HTTP calls
* incoming WebSocket events
* error messages

### Node.js tests

Test:

* clients joining board rooms
* valid event broadcasting
* invalid event rejection
* disconnected client handling

### Manual end-to-end test

Open the application in two browser windows.

1. Open the same board in both windows.
2. Create a task in window A.
3. Confirm it appears in window B.
4. Move the task in window B.
5. Confirm it moves in window A.
6. Edit the same task from both windows.
7. Confirm one stale update receives a conflict.

## 17. Security

For the first version, use simplified identity.

The frontend can include a user selector and send the selected user ID in a request header.

```http
X-User-Id: ed389422-d555-4421-bd5a-b715008a995f
```

The backend should verify that the user exists.

This is not secure enough for production, but it avoids spending several days implementing authentication.

JWT authentication can be added only after the core application is complete.

## 18. Logging

The Java backend should log:

* incoming task-changing operations
* task IDs and board IDs
* rejected state transitions
* optimistic locking conflicts
* failures publishing real-time events

The Node.js gateway should log:

* client connections
* client disconnections
* board subscriptions
* event broadcasts
* invalid event requests

Do not log complete request bodies or sensitive information.

## 19. Local Development

Suggested ports:

```text
Angular:      4200
Java backend: 8080
Node gateway: 3000
PostgreSQL:   5432
```

A later version may use Docker Compose.

```text
docker-compose.yml
- postgres
- java-backend
- node-gateway
```

Run Angular separately during development for fast reloads.

## 20. Implementation Plan

### Days 1–2: Java foundations

* create Spring Boot project
* create entities
* configure database
* create repositories
* seed one board and several users

### Days 3–4: Java API

* implement task APIs
* add validation
* add exception handling
* add activity recording
* write service unit tests

### Days 5–7: Angular foundations

* create Angular project
* create board layout
* load tasks from the backend
* render columns and cards
* add task form
* add editing and deletion

### Days 8–9: Task movement

* add drag-and-drop
* call status API
* handle invalid transitions
* add filters
* improve loading and error states

### Days 10–11: Node.js gateway

* create Express and Socket.IO server
* implement board rooms
* implement internal event endpoint
* publish Java events to Node.js
* process events in Angular

### Days 12–13: Testing and cleanup

* add integration tests
* add Angular tests
* test concurrent task updates
* fix bugs
* refactor confusing code

### Day 14: Finish

* write README
* add setup instructions
* add screenshots
* check that another person could run the project
* remove unfinished features
* make one final end-to-end pass

## 21. Minimum Viable Version

The project is complete when:

* the board loads successfully
* users can create, edit, move, and delete tasks
* tasks persist in the database
* invalid input produces clear errors
* task movements follow defined rules
* two browser windows receive real-time updates
* stale task updates produce a conflict
* the core Java business logic has tests
* the README contains working setup instructions

Everything else is optional.

## 22. Optional Extensions

Only add these after the minimum version is complete:

* task comments
* due dates
* JWT authentication
* Docker Compose
* multiple boards
* board membership
* pagination for activity records
* retrying failed real-time events
* dark mode
* deployment to a cloud provider

## 23. Final Design Decisions

The system uses Java as the authoritative backend because it owns persistence and business rules.

Node.js is used only for real-time communication because WebSocket connection handling is a natural responsibility for it.

Angular is responsible for presentation and temporary client-side state.

PostgreSQL is the source of truth.

REST is used for all state-changing operations.

WebSockets are used only to notify clients that state has changed.

Optimistic locking prevents users from silently overwriting each other’s changes.

The design deliberately prioritizes clarity, completion, and learning over production-scale architecture.
