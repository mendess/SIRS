use crate::schema::children;
use diesel::{backend::Backend, Identifiable, Insertable, Queryable};
use serde::{Deserialize, Serialize};
use std::fmt::{self, Display};

#[derive(
    Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Serialize, Deserialize, Identifiable, Queryable,
)]
#[table_name = "children"]
pub struct Child {
    pub id: ChildId,
    pub username: String,
    pub password: String,
}

#[derive(Debug, Copy, Clone, Eq, PartialEq, PartialOrd, Ord, Hash, Deserialize, Serialize)]
#[serde(transparent)]
pub struct ChildId(pub i32);

impl Display for ChildId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

impl From<i32> for ChildId {
    fn from(i: i32) -> Self {
        Self(i)
    }
}

impl<DB, ST> Queryable<ST, DB> for ChildId
where
    DB: Backend,
    i32: diesel::deserialize::FromSql<ST, DB>,
{
    type Row = <i32 as Queryable<ST, DB>>::Row;

    fn build(row: Self::Row) -> Self {
        row.into()
    }
}

impl<Tab> Insertable<Tab> for ChildId
where
    i32: Insertable<Tab>,
{
    type Values = <i32 as Insertable<Tab>>::Values;

    fn values(self) -> Self::Values {
        self.0.values()
    }
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Serialize, Deserialize, Queryable)]
#[serde(rename(serialize = "Child", deserialize = "Child"))]
pub struct ChildView {
    pub id: ChildId,
    pub username: String,
}
