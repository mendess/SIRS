use super::{guardian::GuardianId, Location};
use serde::{Deserialize, Serialize};
use std::{
    fmt::{self, Display},
    sync::atomic::{AtomicU64, Ordering},
};

#[derive(Debug, Copy, Clone, Eq, PartialEq, PartialOrd, Ord, Hash, Serialize, Deserialize)]
#[serde(transparent)]
pub struct ChildId(u64);

impl From<u64> for ChildId {
    fn from(u: u64) -> Self {
        Self(u)
    }
}

impl Display for ChildId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

static LAST_CHILD_ID: AtomicU64 = AtomicU64::new(0);

pub struct Child {
    id: ChildId,
    guardians: Vec<GuardianId>,
    location: Option<Location>,
}

impl Child {
    pub fn new(guardian: GuardianId) -> Self {
        Self {
            id: LAST_CHILD_ID.fetch_add(1, Ordering::SeqCst).into(),
            guardians: vec![guardian],
            location: None,
        }
    }

    pub fn id(&self) -> ChildId {
        self.id
    }

    pub fn update_location(&mut self, location: Location) {
        self.location = Some(location);
    }

    fn is_guarded_by(&self, guardian: GuardianId) -> bool {
        self.guardians.contains(&guardian)
    }

    pub fn location(&self, guardian: GuardianId) -> Option<Location> {
        self.location.filter(|_| self.is_guarded_by(guardian))
    }

    pub fn add_guardian(&mut self, guardian: GuardianId) {
        self.guardians.push(guardian);
    }
}
