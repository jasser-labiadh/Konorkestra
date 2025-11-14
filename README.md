![Konorkestra](docs/images/konor.png)
# Konorkestra

Konorkestra is a **configuration orchestration system** designed to make **configuration management in distributed environments straightforward, safe, and declarative**.

> **Note:** The project is under active development. Features and implementations may change over time. Design decisions and documentation are available in the `/docs` folder.

---

## Core Concepts

Konorkestra revolves around three key concepts: **ConfigSet**, **Group**, and **Policy**.

- **ConfigSet**: A collection of configuration key-value pairs with identifying metadata.  
- **Group**: One or more `ConfigSet`s combined with a `Policy`. Nodes subscribe to groups to receive configuration updates.  
- **Policy**: Defines how configuration changes are applied (e.g., gradual rollout, quorum-based updates, constrained changes).

---

## How it Works

1. **Node Subscription**  
   Nodes (physical or virtual machines) subscribe to a `Group` using a pre-generated token. Once subscribed, they receive the current configuration and stay updated in real-time.

2. **Lightweight Agent**  
   Each node runs a lightweight agent acting as a local server for all processes on that node (containerized or not). Communication with processes happens via a local socket.

3. **Process Integration**  
   Processes can register logic to execute when a configuration change occurs (e.g., reload a value, restart a service, run custom logic) via a **language-level SDK**. MVP targets **Java**, with plans for other languages.

4. **Controlled Configuration Access**  
   The agent uses a custom lightweight storage engine. Processes only access configuration through the agent—nothing is scattered or directly exposed. Failover, synchronization, and consistency are handled internally.

5. **Declarative Approach**  
   All configurations and updates are defined declaratively. Konorkestra Central ensures **atomicity, traceability, and safe application** of configuration changes.

---

## Core Architecture

Konorkestra treats **any operation beyond bootstrapping the central system as a "job"**.  

- **Job Dispatcher**: Validates jobs and persists them. Clients see a job as “accepted” once this step is complete.  
- **Job Scheduler**: Schedules jobs for execution, handles concurrency, and locks resources if necessary.  
- **Execution Engine**: Converts a job into a dynamic **execution plan** composed of **redo-safe operations**. A “compiler” generates deterministic, recoverable plans.  
- **Storage Engine**: A custom engine underlies the system. Optimized for **read-heavy workloads** and **low write frequency**. UDP handles heartbeat/liveness; TCP handles critical updates. Scaling Konorkestra requires swapping in a storage engine with **consensus and leader election** support.  
- **Transaction Manager**: Tracks all job steps to guarantee smooth recovery in case of crashes.

---

## Editions

- **OSS Edition (Open Source)**  
  - Single-node optimized.  
  - Lightweight UDP for node liveness and aggregated metrics.  
  - Minimal external dependencies.  
  - Focused on configuration reliability, atomic updates, and developer-friendly integrations.  
  - **Note:** Constraints for canary-style updates are **not included** in OSS.

- **Ultimate Edition (Enterprise / SaaS)**  
  - Multi-node support with a distributed storage engine.  
  - Centralized heartbeat and metrics collection.  
  - High availability, load balancing, and enterprise-grade features.  
  - Supports **canary updates with constraints**, enabling staged rollouts and advanced policies.  
  - **Note:** Ultimate Edition will be developed based on OSS feedback and adoption; it is **not planned for immediate release**.

---

## Goals

- Provide a **reliable and predictable configuration system** for distributed nodes.  
- Ensure **atomic updates** and **runtime consistency** across nodes and processes.  
- Enable **developer-friendly integrations** via SDKs.  
- Allow teams to **define policies and rules** for safe configuration changes.  

---

## Documentation

For detailed architecture, design decisions, and usage examples, see the `/docs` folder in this repository.

---

## Contributing

Contributions are welcome! Reach out at **jasser@jasser.dev** with ideas, questions, or pull requests.
