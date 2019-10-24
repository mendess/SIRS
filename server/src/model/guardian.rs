use serde::{Deserialize, Serialize};
use std::{
    fmt::{self, Display},
    sync::atomic::{AtomicU64, Ordering},
};

#[derive(Debug, Copy, Clone, Eq, PartialEq, PartialOrd, Ord, Hash, Serialize, Deserialize)]
#[serde(transparent)]
pub struct GuardianId(u64);

impl From<u64> for GuardianId {
    fn from(u: u64) -> Self {
        Self(u)
    }
}

impl Display for GuardianId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

static LAST_ID: AtomicU64 = AtomicU64::new(0);

#[derive(Debug, Clone, Eq, PartialEq, PartialOrd, Ord, Hash, Serialize, Deserialize)]
pub struct Guardian {
    id: GuardianId,
    username: String,
    password: Vec<u8>,
}

impl Guardian {
    pub fn new(username: String, password: Vec<u8>) -> Self {
        Self {
            id: LAST_ID.fetch_add(1, Ordering::SeqCst).into(),
            username,
            password,
        }
    }

    pub fn id(&self) -> GuardianId {
        self.id
    }
}
