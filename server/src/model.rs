mod child;
mod guardian;

use child::Child;
use guardian::Guardian;
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, sync::Mutex};

pub use child::ChildId;
pub use guardian::GuardianId;

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Location {
    x: i64,
    y: i64,
}

#[derive(Default)]
pub struct Db {
    children: Mutex<HashMap<ChildId, Child>>,
    guardians: Mutex<HashMap<GuardianId, Guardian>>,
}

impl Db {
    pub fn register_new_guardian(&self, username: String, password: Vec<u8>) -> GuardianId {
        let new = Guardian::new(username, password);
        let id = new.id();
        self.guardians.lock().unwrap().insert(id, new);
        id
    }

    pub fn register_new_child(&self, guardian: GuardianId) -> ChildId {
        let new = Child::new(guardian);
        let id = new.id();
        self.children.lock().unwrap().insert(id, new);
        id
    }

    pub fn child_location(&self, child: ChildId, guardian: GuardianId) -> Option<Location> {
        self.children
            .lock()
            .unwrap()
            .get(&child)
            .and_then(|c| c.location(guardian))
    }

    pub fn update_child_location(&self, child: ChildId, location: Location) {
        self.children
            .lock()
            .unwrap()
            .get_mut(&child)
            .map(|c| c.update_location(location));
    }

    /// This is an angle of attack
    /// If an attacker gets a hold of the child's phone they can easily add
    /// themselves as a guardian.
    ///
    /// The easy way to fix this is to allow only one guardian per child.
    pub fn guard_child(&self, child: ChildId, guardian: GuardianId) -> bool {
        self.children
            .lock()
            .unwrap()
            .get_mut(&child)
            .map(|c| c.add_guardian(guardian))
            .is_some()
    }
}
