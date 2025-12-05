# Konor Frame – Wire Protocol Specification

Konor Frame is the **binary wire protocol** used between Konorkestra Center and Konor Agents.  
It operates on top of QUIC and provides **message framing, versioning, and routing metadata**, while all semantic content lives inside **Protobuf payloads**.

Konor Frame is intentionally minimal:
- QUIC provides reliability and ordering.
- Konor Frame provides structure, operation typing, and correlation.

---

## 1. Frame Overview

Every message sent between Center and Agent is a **Konor Frame**:
[ HEADER ][ PAYLOAD ]
- **Header** is fixed-size (20 bytes)
- **Payload** is a Protobuf message whose type is determined by `OP_CODE`

---

## 2. Binary Layout

All multi-byte fields are **big-endian**.
+——————+————+———–+—————–+––––––––––+—————––+
| MAGIC (4 bytes)  | VERSION(1) | OP_CODE(1)| RESERVED (2)    | JOB_ID (8 bytes)   | PAYLOAD_LEN (4)   |
+——————+————+———–+—————–+––––––––––+—————––+
|                                 PAYLOAD (PAYLOAD_LEN bytes)                                          |
+––––––––––––––––––––––––––––––––––––––––––––––––––––+
### Field Definitions

#### **MAGIC (u32, 4 bytes)**
Constant used to validate that the stream is speaking Konor Frame.  
ASCII `"KONR"` → 0x4B 0x4F 0x4E 0x52.

#### **VERSION (u8, 1 byte)**
Protocol version.
- Start at `1`
- Increment only when header format or semantics change in incompatible ways

#### **OP_CODE (u8, 1 byte)**
Identifies which Protobuf message the payload represents.

#### **RESERVED (u16, 2 bytes)**
Always `0` in version 1.  
Reserved for future flags or extensions.

#### **JOB_ID (u64, 8 bytes)**
Correlation identifier for Konorkestra job execution.
- `0` means “not part of a job”
- Used for fast routing in the DAG execution engine and logging

#### **PAYLOAD_LEN (u32, 4 bytes)**
Length of the Protobuf payload (in bytes).  
Receiver reads exactly `PAYLOAD_LEN` bytes after the header.

---

## 3. Operation Codes (OP_CODE)

Each OP_CODE maps to a specific Protobuf message type.  
This ensures clean routing and extensibility.

| OP_CODE | Direction          | Meaning                   | Protobuf Type         |
|---------|--------------------|---------------------------|------------------------|
| `0x01`  | Center → Agent     | Apply new config version  | `ConfigApply`          |
| `0x02`  | Agent → Center     | ACK config application    | `ApplyAck`             |
| `0x03`  | Agent → Center     | Heartbeat/liveness        | `NodeHeartbeat`        |
| `0x04`  | Agent → Center     | Node joining system       | `NodeJoin`             |
| `0x05`  | Center → Agent     | Response to join          | `NodeJoinResponse`     |
| `0x06`  | Center → Agent     | Force resync              | `NodeResync` (optional)|
| `0x7F`  | Both directions    | Error message             | `ErrorFrame`           |

You may extend OP_CODE values in the future without modifying header layout.

---

## 4. Payloads (Protobuf Messages)

The payload is always a single Protobuf message.  
The receiver selects the appropriate Protobuf type based on `OP_CODE`.

### Example: `ConfigApply` (OP 0x01)

```protobuf
message ConfigApply {
  string node_id = 1;
  uint64 configset_version = 2;
  string group_id = 3;
  bytes  config_payload = 4;  // serialized kvs or blob reference
}
Example: ApplyAck (OP 0x02)
message ApplyAck {
  string node_id = 1;
  uint64 configset_version = 2;

  enum Status {
    OK = 0;
    FAILED = 1;
    VALIDATION_ERROR = 2;
  }

  Status status = 3;
  string message = 4;
}
Konor Frame exists because Konorkestra needs a lightweight, predictable, binary protocol for control-plane communication. 
HTTP adds unnecessary overhead—text headers, complex framing, and semantics we do not use—while Konor Frame gives us a fixed, 
minimal header, fast parsing, clean message boundaries, and simple mapping from operation codes to protobuf messages. 
This makes communication cheaper, more deterministic, and easier to reason about at scale, 
especially with thousands of agents sending frequent heartbeats and configuration updates.