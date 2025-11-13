![Konorkestra](docs/images/konor.png)
# Konorkestra

Konorkestra is a configuration orchestration system designed to make **configuration management in distributed environments straightforward, safe, and declarative**.

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
   Nodes (physical or virtual machines) subscribe to a `Group` using a pre-generated token. Once subscribed, they receive the current configuration and stay updated with changes in real-time.

2. **Lightweight Agent**  
   Each node runs a lightweight agent that acts as a local server for all processes on that node (containerized or not). Communication with processes happens via a local socket.

3. **Process Integration**  
   Processes can register callbacks or logic to execute when a configuration change occurs (e.g., reload a value, restart a service, run custom logic). This is done through a **language-level SDK**. The MVP targets **Java**, with plans for additional languages in the future.

4. **Controlled Configuration Access**  
   The agent uses a custom lightweight storage engine. Processes only access configuration through the agent—nothing is scattered or exposed directly. Failover, synchronization, and consistency are handled by the agent.

5. **Declarative Approach**  
   All configurations and updates are defined declaratively. Konorkestra Central ensures atomicity, traceability, and safe application of configuration changes.

---

## Core Architecture and Components

Konorkestra is designed around the concept of **jobs**, where any operation beyond bootstrapping the central system is treated as a job.

- **Job Dispatcher**: Validates incoming jobs and persists them. Clients see a job as “accepted” once this step is complete.  
- **Job Scheduler**: Schedules jobs for execution while handling concurrency and locking resources when necessary.  
- **Execution Engine**: Converts a job object into a dynamic execution plan composed of **redo-safe operations**. A “compiler” generates these plans to ensure deterministic, recoverable execution.  
- **Storage Engine**: A custom storage engine underlies the system. Scaling Konorkestra requires swapping in a storage engine that supports **consensus and leader election**, enabling efficient multi-node deployments. Optimized for read-heavy workloads with low write frequency. UDP handles heartbeat/liveness checks, while TCP is used only for critical updates.  
- **Transaction Manager**: Tracks all steps of every job to guarantee **smooth recovery** in case of crashes.

**Deployment Vision**:  
- MVP: Single-node, highly efficient, fully functional.  
- Ultimate Edition: Enterprise-grade features, distributed support, and high-availability capabilities.

---

## Goals

- Provide a **reliable and predictable configuration system** for distributed nodes.
- Ensure **atomic updates** and **runtime consistency** across nodes and processes.
- Enable **developer-friendly integrations** via language SDKs.
- Allow teams to **define policies and rules** for safe configuration changes.

---

## Documentation

For detailed architecture, design decisions, and usage examples, see the `/docs` folder in this repository.

---

## Contributing

Contributions are welcome! Feel free to reach out at **jasser@jasser.dev** with ideas, questions, or pull requests.
