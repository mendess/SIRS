use crate::schema::guardians;
use diesel::{Identifiable, Queryable};
use serde::{Deserialize, Serialize};
use std::fmt::{self, Display};

#[derive(Identifiable, Queryable)]
pub struct Guardian {
    pub id: i32,
    pub username: String,
    pub password: String,
}

#[derive(Debug, Copy, Clone, Eq, PartialEq, PartialOrd, Ord, Hash, Deserialize, Serialize)]
#[serde(transparent)]
pub struct GuardianId(pub i32);

impl Display for GuardianId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

impl From<i32> for GuardianId {
    fn from(i: i32) -> GuardianId {
        GuardianId(i)
    }
}
