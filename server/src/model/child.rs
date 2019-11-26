use crate::schema::children;
use diesel::{Identifiable, Queryable};
use serde::{Deserialize, Serialize};
use std::fmt::{self, Display};

#[derive(
    Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Serialize, Deserialize, Identifiable, Queryable,
)]
#[table_name = "children"]
pub struct Child {
    pub id: i32,
    pub username: String,
}

#[derive(Debug, Copy, Clone, Eq, PartialEq, PartialOrd, Ord, Hash, Deserialize, Serialize)]
#[serde(transparent)]
pub struct ChildId(pub i32);

impl Display for ChildId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}
