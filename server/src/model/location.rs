use super::child::{Child, ChildId};
use crate::schema::locations;
use diesel::{Associations, Identifiable, Insertable, Queryable};
use serde::{Deserialize, Serialize};

// TODO: Clean this to one location type

#[derive(Associations, Identifiable, Queryable)]
#[table_name = "locations"]
#[belongs_to(parent = "Child", foreign_key = "child_id")]
pub struct LocationInternal {
    id: i32,
    child_id: i32,
    location: String,
}

#[derive(Insertable, Deserialize, Serialize)]
#[table_name = "locations"]
pub struct Location {
    child_id: i32,
    location: String,
}

impl Location {
    pub fn new(child: ChildId, location: String) -> Self {
        Self {
            child_id: child.0,
            location,
        }
    }

    pub fn into_blob(self) -> String {
        self.location
    }
}

impl From<LocationInternal> for Location {
    fn from(l: LocationInternal) -> Self {
        Self {
            child_id: l.child_id,
            location: l.location,
        }
    }
}
