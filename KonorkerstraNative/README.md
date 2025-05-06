# **Go Dynamic Configuration Synchronization**

This Go library provides **dynamic synchronization** of configurations across processes in a distributed system. It acts as a bridge between client processes and a centralized **configuration daemon**, enabling real-time configuration updates with minimal overhead.

## **Features**
- **Centralized Configuration Sync**: Processes register to receive updates tailored to their needs from the central configuration system.
- **IPC-Based Notification**: Utilizes **named pipes (FIFOs)** for inter-process communication (IPC) to notify processes of configuration changes.
- **Cross-Language Integration**: The library communicates through **Unix Domain Sockets (UDS)**, making it compatible with processes in languages like **Java**, **C**, and **Python**.
- **Developer-Friendly**:
  - **Subscribe to Updates**: Easily register for configuration changes.
  - **Real-Time Updates**: Processes are notified when configuration changes occur, with no need for polling or callbacks.

---

## **Architecture Overview**

### **Key Components**
- **KonorkestraConfigDaemon (Java)**:
  - A central configuration daemon that listens for updates from a centralized system and notifies registered processes by writing to their pipes.

- **Go Library**:
  - Acts as a **shared library** for client processes to subscribe to configuration changes.
  - Sends registration requests to the daemon and returns a **unique named pipe** for receiving updates.

- **Client Processes**:
  - Any process (C, Python, Java) can use the Go library to subscribe to configuration changes.
  - Processes receive real-time updates through their pipes and react accordingly.

---