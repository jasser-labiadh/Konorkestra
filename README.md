# KONORKESTRA

**KONORKESTRA** is a distributed configuration orchestration and heartbeat monitoring system, designed to manage and observe large-scale node deployments across cloud, hybrid, and on-premise environments.

It consists of two primary subsystems:

1. [Node-side Components](#1-node-side-components)
2. [Central System Components](#2-central-system-components)

---

## ⚙️ Design Goals

- 🔁 **High Observability**: Real-time visibility into node activity and health.
- 🧠 **Dynamic Configuration**: Push-based configuration updates with flexible quorum models.
- 🔐 **Security-first**: Secret-key based authentication and scoped access control.
- 🧩 **Pluggable Clients**: Modular design allows embedding the configuration client into other native applications (e.g., C++).
- 🧵 **Concurrency-safe**: Thread-safe I/O and configuration updates through internal wrappers and managers.

---

## 1. Node-side Components

### 🔁 Heartbeat Client

- **Function**: Periodically sends a "heartbeat" signal to indicate that the node is active.
- **Node Identity**:
    - Automatically generates a **globally unique node ID**.
    - Includes a **cloud-specific prefix** if the node runs in cloud infrastructure.
- **Transport Layer**:
    - Communicates with the central system via a **load-balanced HTTP/gRPC** channel.
- **Fault Handling**:
    - Retries with exponential backoff on network failure.
    - Detects if the node is misconfigured and exits with error reporting.

---

### ⚙️ Configuration Client

- **Function**: Connects to the central configuration service and listens for real-time updates.
- **Authentication**:
    - Uses an admin-generated **secret key** passed via the `-x` flag to verify identity and group membership.
- **Persistence Mechanism**:
    - Uses three types of files to persist configuration state:
        - `metadata`: stores version, timestamp, and consistency info.
        - `data`: contains the raw configuration payload.
        - `index`: maintains offset information for fast access.
    - Designed for **resilience** and **fast recovery** on node restart.
- **Subscription API (Local IPC)**:
    - Provides a **Unix Domain Socket (UDS)** server to enable **local processes** to subscribe to configuration updates.
    - C++ clients can link against the provided native library to:
        - Register callbacks
        - Subscribe/unsubscribe to specific config namespaces
- **Submodules**:
    - `WriterManager`: Controls atomic write operations.
    - `LogManager`: Tracks update history and rollback points.
    - `KeyManager`: Manages secret keys and access control.
    - `ThreadPoolWrapper`: Executes update callbacks and manages parallelism.

---

## 2. Central System Components

### 🛠️ Admin CLI & REST API

- **Admin Operations**:
    - Account sign up / login
    - Create / delete **node groups**
    - Define initial **key-value configs** for groups
    - Generate **onboarding keys** for nodes to join groups securely
    - Modify existing configurations or rotate keys
    - Choose **update policy**:
        - **Quorum**: Change is accepted when `X%` of nodes acknowledge.
        - **Strict**: All nodes must acknowledge the update.

- **Dynamic Quorum**:
    - The required percentage `X` is dynamically calculated based on **heartbeat data** (live node count per group).
    - Ensures fault tolerance in elastic or failing environments.

- **Audit Logging**:
    - All administrative operations are logged for traceability.

---

## 3. Central System Workflows

### 📡 Heartbeat Pipeline

**Flow**:
#### Key Components:

- **Receiver Node**:
    - Updates **Redis** with a TTL to mark the node as "alive."
    - Forwards the heartbeat to the Producer Service.

- **Producer Service**:
    - Publishes structured heartbeat messages to a **Kafka** topic.
    - Listens for **Redis key expiry events** to detect dead nodes and emits timeout signals.

- **Analytics Service**:
    - Consumes heartbeats from Kafka.
    - Generates:
        - Health reports
        - Cloud/on-prem analytics
        - Group-wise uptime/downtime trends

---

### 🔧 Configuration Update Pipeline

**Flow**:
#### Process:

1. Admin issues a config update request via CLI/API.
2. Gateway routes it through **authentication** and **authorization**.
3. Update Component sends the change to all nodes in the group.
4. Waits for acknowledgment:
    - **Quorum** mode
    - **Strict** mode
5. Upon success:
    - Commits update to the central database (e.g., **Oracle**).
    - Ensures any new node joining the group receives the **latest config snapshot**.

---

## 🧰 Technologies Used

| Component              | Tech Stack                 |
|------------------------|----------------------------|
| Heartbeat & Config     | gRPC, HTTP/2, UDS          |
| Message Queue          | Apache Kafka               |
| Data Store (Liveness)  | Redis (TTL-based key-value store) |
| Persistent DB          | Oracle (Relational DB for metadata) |
| Analytics              | Custom service (Kafka consumer) |
| CLI                    | Python                     |
| Native Client Binding  | C++ with JNI/UDS           |

---
## 🏁 Version

This is **Version 1.1 (V1.1)** of KONORKESTRA.

## 📬 Contact

Maintained by: Jasser Labiadh

Email: jasser@jasser.dev 