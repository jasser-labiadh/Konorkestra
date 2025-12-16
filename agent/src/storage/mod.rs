use std::sync::Arc;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum Namespace {
    ConfigSet, // after joining a group we get these
    Runtime,  // every configuration and data related to the operations ( node metadata, job status, etc.)
}
//MVP implementation would be based on sled, maybe later on customized storage engine.
pub trait StorageEngine: Send + Sync + 'static {
    type Error: std::error::Error + Send + Sync + 'static;

    /// Get value for (namespace, key).
    fn get(&self, ns: Namespace, key: &[u8]) -> Result<Option<Vec<u8>>, Self::Error>;

    /// Insert/update value.
    fn put(&self, ns: Namespace, key: &[u8], value: &[u8]) -> Result<(), Self::Error>;

    /// Delete a key if it exists.
    fn delete(&self, ns: Namespace, key: &[u8]) -> Result<(), Self::Error>;

    /// List keys/values with a given prefix. Used for listing configsets, etc.
    fn scan_prefix(
        &self,
        ns: Namespace,
        prefix: &[u8],
    ) -> Result<Vec<(Vec<u8>, Vec<u8>)>, Self::Error>;

    /// Best-effort flush to disk. For purely in-memory engines this can be a no-op.
    fn flush(&self) -> Result<(), Self::Error>;
}

// Type alias you can use everywhere in the agent:
pub type DynStorage = Arc<dyn StorageEngine<Error = StorageError>>;

// A concrete error type you can share across impls.
#[derive(thiserror::Error, Debug)]
pub enum StorageError {
    #[error("backend error: {0}")]
    Backend(String),

    #[error("serialization error: {0}")]
    Codec(String),

    // Add more as needed
}

// Re-export implementations so `crate::storage::InMemoryStorage` works:
mod memory;
pub use memory::InMemoryStorage;

// later:
// mod sled;
// pub use sled::SledStorage;