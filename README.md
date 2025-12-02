![Konorkestra](docs/images/konor.png)
# Konorkestra

Konorkestra is a **declarative, DAG-driven, QUIC-powered configuration orchestration system** for distributed environments.

It focuses on **safe, traceable configuration changes** and **runtime reactivity**, while keeping the agent footprint and network overhead as small as possible.

> **Status:** Under active development (MVP).  
> Design docs live under [`/docs`](./docs). Expect changes as the project evolves.

---

## Core Concepts

Konorkestra revolves around four core primitives:

- **ConfigSet**  
  A versioned, immutable bundle of configuration key–value pairs.  
  - No in-place mutations  
  - Every change creates a new version (`ConfigSet vN`)  
  - Enables clean rollback and atomic resync

- **Group**  
  A logical grouping of nodes that share one or more `ConfigSet`s.  
  Nodes subscribe to groups to receive configuration.

- **Node**  
  A machine/VM/container running the **Konor Agent**, which:
  - Maintains a control-plane connection to Konor Center  
  - Stores config locally  
  - Serves it to local processes via a socket

- **Strategy** (inline rollout strategy)  
  Defines *how* a change rolls out across nodes:
  - `ALL` – strict consistency across all live nodes  
  - `QUORUM` – success once a threshold of live nodes is updated  
  - `BATCH` – rollout in waves on top of ALL/QUORUM  
  - `EVENTUAL` – fire-and-forget; nodes converge over time

There is **no separate Policy object** in the MVP. Strategies are defined inline in jobs, which simplifies the compiler and execution engine.

---

## How It Works

### 1. Job-Based Model

Anything beyond bootstrapping Konor Center is a **job**:

- `UPDATE_CONFIGSET`
- `CREATE_CONFIGSET`
- `CREATE_GROUP`
- `UPDATE_GROUP`
- `ISSUE_JOIN_TOKEN`
- `NODE_JOIN`
- etc.

Jobs are submitted via CLI or API, validated, and then compiled into a **Directed Acyclic Graph (DAG)** of redo-safe steps.

### 2. DAG Execution Engine

Each job’s DAG typically includes:

1. Acquiring locks on relevant primitives (group/configset)
2. Resolving target nodes, groups, and latest versions
3. Building a rollout plan based on the selected strategy
4. Applying the new config version to nodes (possibly in batches)
5. Waiting for ACKs and checking ALL/QUORUM thresholds
6. Committing job status and writing audit logs
7. Cleanup and unlock

This model provides:

- Idempotency (safe retries on crash)
- Deterministic orchestration
- Clear observability into each step of a change

### 3. Konor Agent on Each Node

Each node runs a **lightweight agent** that:

- Maintains a **QUIC** connection to Konor Center
- Stores ConfigSets in a small local storage engine
- Exposes a local socket API for processes (UNIX domain socket or loopback)
- Applies config changes atomically per version
- Sends **heartbeats** to report liveness and current config versions
- Handles **resync** when it falls behind

Processes don’t read scattered config files; they call the agent (via a language SDK) and can register callbacks to react to config changes (e.g. reload, soft restart, custom logic).  
MVP SDK target: **Java**, with more languages to come.

### 4. Networking & Protocol

Konorkestra uses:

- **QUIC** for the control plane:
  - Low idle overhead
  - Connection migration (good for dynamic IPs/containers)
  - Multiplexed streams with reliability and encryption

- **Konor Frame** as the wire protocol:
  - Minimal binary framing:
    - `version (1 byte) | msg_type (1 byte) | flags (1 byte) | payload_len (4 bytes) | payload`
  - Payload is a fixed-format binary struct per operation (no JSON/YAML on the control plane)
  - Example message types:
    - `CONFIG_APPLY`
    - `APPLY_ACK`
    - `NODE_JOIN`
    - `NODE_HEARTBEAT`
    - `NODE_RESYNC`

This keeps parsing cost tiny and predictable, even with tens of thousands of connected agents.

---

## Liveness, Rollout, and Resync

### Heartbeats & Live Set

- Agents periodically send small heartbeats with:
  - `node_id`
  - last applied `ConfigSet` version(s)
  - timestamp / basic health info
- If heartbeats are missed for a configurable window, Konor Center marks the node as **DEAD**/**OFFLINE**.

### Rollout Semantics

Rollouts operate over the set of **live nodes**:

- `ALL` means “all *live* nodes”
- `QUORUM` thresholds are computed over live nodes
- `BATCH` applies to subsets of live nodes

If nodes die mid-rollout:

- They are removed from the live set
- Quorum calculations adjust dynamically
- Rollout does not block forever on a dead node

### Resync & Convergence

When a node comes back (or joins):

1. The heartbeat or join request reveals its current config version(s).
2. If the node is behind, Konor Center sends a **resync frame** (`NODE_RESYNC` / `CONFIG_APPLY` with the latest version).
3. The agent atomically applies the newer version and ACKs.

Because ConfigSets are **versioned and immutable**, resync is simple and safe.  
This ensures the cluster **eventually converges** even if some nodes were down during a rollout.

---

## Failure Handling & Webhooks

Konorkestra can emit **webhooks** for key events:

- rollout failures or partial failures
- agent unreachable / node offline
- resync failures
- constraint violations (in future editions)

These webhooks can be integrated with:

- Slack / email / PagerDuty
- Kubernetes operators or controllers
- CI/CD automation and observability systems

This turns stale or inconsistent nodes into **visible, actionable events**, instead of silent failures.

---

## Storage Engine

MVP uses:

- A single-node storage engine (based on RocksDB) for:
  - jobs
  - DAG state
  - configset versions
  - groups
  - node metadata

It supports transactions and redo logs to ensure recovery after crashes.

Future enterprise edition will swap this for a consensus-backed engine (e.g. Raft-based) to provide high availability and scaling for Konor Center.

---

## Editions

### OSS Edition (MVP)

- Single-node Konor Center
- QUIC + Konor Frame for communication
- DAG-based rollouts with strategies:
  - ALL, QUORUM, BATCH, EVENTUAL
- Versioned ConfigSets and resync
- Lightweight agent and local storage
- CLI + basic integrations

### Future Enterprise / “Ultimate” Edition

Planned (not implemented yet):

- Multi-node, HA Konor Center  
- Consensus-based storage & leader election  
- Metrics-aware rollout constraints and canaries  
- Advanced observability and integration  
- Multi-cluster or multi-region support  

The enterprise design will be driven by OSS adoption and feedback.

---

## Documentation

Detailed architecture, design decisions, and diagrams are in the [`/docs`](./docs) folder.

Planned docs include:

- Architecture & Design (MVP)
- Konor Frame wire protocol specification
- DAG execution model
- Agent SDK usage
- Deployment examples (Docker/Kubernetes, bare metal)

---

## Contributing

Contributions, feedback, and design critiques are welcome.

- Email: **jasser@jasser.dev**

You can help by:

- Reviewing architecture docs
- Suggesting improvements to the protocol/DAG design
- Implementing parts of the MVP (agent, center, CLI, SDK)
- Trying Konorkestra in small test environments and reporting back
