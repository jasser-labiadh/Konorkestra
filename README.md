KONORKESTRA - Dynamic Configuration & Health Monitoring System

Overview:
KONORKESTRA is an ongoing project designed to manage dynamic configurations and node health monitoring for distributed systems. The system enables nodes to join a group of configurations, synchronize their settings, and handle configuration updates efficiently.

Key Components:

1. Configuration Management:
	•	Admin Setup:
The admin signs up using credentials and creates a “GROUP” of settings for nodes. This group consists of a list of key-value pairs. The system generates a unique secret key for each group.
	•	Node Initialization:
Nodes join a group using the secret key. For example, a node runs a command like java config_client.java AX324JR9 to join the group and fetch all the configurations.
	•	Configuration Updates:
The system maintains a list of transactions for each group, such as setting a configuration value. It reads from the bottom up and only takes the most up-to-date values for each configuration. The system is optimized using a daemon compaction process to remove redundant data.
	•	Consistency Models:
The system offers two consistency models:
	•	Eventual Consistency: Updates are propagated using quorum-based parameters.
	•	Strong Consistency: All nodes acknowledge updates before committing.
	•	File System Design:
	•	Meta Data File: Stores information about the configuration, such as its name, offset, and size.
	•	Data File: Stores the actual configuration data. Configurations are appended to this file, rather than being overwritten.
	•	Serialization: Configurations are serialized using Protobuf for cross-language compatibility.

2. Heartbeat & Node Health Monitoring:
	•	Heartbeat Client:
Each node runs a heartbeat client that generates a unique ID (e.g., AWS instance ID or MAC address for on-prem nodes). Heartbeats are sent at fixed intervals to monitor node liveness.
	•	Heartbeat Workflow:
Heartbeats are sent to a reverse proxy, which forwards them to the Receiver Service, where the node’s liveness is stored in a Redis database. The Receiver then passes the data to a Message Queue Producer Service. If a heartbeat expiry event occurs (i.e., the node fails to send a heartbeat), the producer service publishes this data to a message queue for further analysis.
	•	Analytics Service:
The heartbeat data is consumed by the Analytics Service, which stores it in TimescaleDB, since the data is time-series in nature. This service analyzes node health and liveness.

3. Go Library:
	•	Library Design:
A Go library is built for managing configurations. The library communicates with the configuration client and exposes a getConfig function for retrieving configuration data. It also manages offset updates and can trigger custom actions when configurations change.
	•	Subscription Mechanism:
The getConfig function subscribes processes to configuration changes. When an update occurs, processes are notified via a signal and can execute custom logic, such as restarting the application or reloading the configuration.
	•	Inter-process Communication (IPC):
The library uses Unix Domain Sockets (UDS) for communication between the library and the configuration client. This ensures efficient local communication between processes.
	•	Custom Logic:
The developer is responsible for implementing custom logic (e.g., restarting an app, reloading configurations) when a configuration change is detected.

4. Component Overview:
	•	Receiver:
Receives heartbeats and updates the Redis key-value store to refresh the TTL (time-to-live) of nodes.
	•	Producer Service:
Receives heartbeats from the Receiver and Redis. It listens for Redis key expiry events (indicating that a node hasn’t sent a heartbeat within the expected time) and sends heartbeat information to Kafka for further processing by the Analytics Service.
	•	Update and Grouping Services:
Handle configuration updates and group node configurations. They ensure that the correct configurations are applied to each node and manage the process of updating configurations.
	•	Compaction Service:
This service optimizes the configuration data by removing redundant entries, improving system efficiency.

Summary:

KONORKESTRA is an ongoing project that manages node configurations, ensuring they are updated and synchronized across all nodes in a group. It handles heartbeat monitoring for node health, provides multiple consistency models for configuration updates, and enables custom logic for processes reacting to configuration changes. The Go library simplifies the integration of configuration management across different programming languages while ensuring efficient and reliable communication between the components.
