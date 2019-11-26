use super::child::{Child, ChildId};
use crate::schema::locations;
use diesel::{Associations, Identifiable, Insertable, Queryable};
use serde::{Deserialize, Serialize};

#[derive(Associations, Identifiable, Queryable)]
#[table_name = "locations"]
#[belongs_to(parent = "Child", foreign_key = "child_id")]
pub struct LocationInternal {
    id: i32,
    child_id: i32,
    latitude: f64,
    longitude: f64,
}

#[derive(Deserialize, Serialize)]
pub struct Location {
    child_id: ChildId,
    latitude: f64,
    longitude: f64,
}

impl From<LocationInternal> for Location {
    fn from(l: LocationInternal) -> Self {
        Self {
            child_id: ChildId(l.child_id),
            latitude: l.latitude,
            longitude: l.longitude,
        }
    }
}

#[derive(Insertable, Deserialize, Serialize)]
#[table_name = "locations"]
pub struct NewLocation {
    child_id: i32,
    latitude: f64,
    longitude: f64,
}

impl NewLocation {
    pub fn new(child_id: ChildId, latitude: f64, longitude: f64) -> Self {
        Self {
            child_id: child_id.0,
            latitude,
            longitude,
        }
    }
}
