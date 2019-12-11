mod child;
mod guardian;
mod location;

pub use child::{Child, ChildId, ChildView};
pub use guardian::GuardianId;
pub use location::Location;

use super::schema::{children, guardian_has_children, guardians, locations};
use crate::error::{Error, Result};
use diesel::{pg::PgConnection, prelude::*, Associations, Identifiable, Insertable, Queryable};
use guardian::Guardian;
use location::LocationInternal;
use std::sync::Mutex;

pub struct Db(Mutex<PgConnection>);

impl Default for Db {
    fn default() -> Self {
        dotenv::dotenv().ok();
        let db_url = std::env::var("DATABASE_URL").expect("DATABASE_URL must be set");
        PgConnection::establish(&db_url)
            .map(|c| Db(Mutex::new(c)))
            .unwrap_or_else(|e| {
                panic!(format!(
                    "Error connecting to {} with error: {:?}",
                    db_url, e
                ))
            })
    }
}

impl Db {
    pub fn register_new_guardian(&self, username: String, password: String) -> Result<GuardianId> {
        #[derive(Insertable)]
        #[table_name = "guardians"]
        struct NewGuardian<'a> {
            pub username: &'a str,
            pub password: &'a str,
        }

        let new_guardian = NewGuardian {
            username: &username,
            password: &password,
        };
        diesel::insert_into(guardians::table)
            .values(&new_guardian)
            .get_result(&*self.0.lock().unwrap())
            .map(|g: Guardian| GuardianId(g.id))
            .map_err(|e| {
                eprintln!("Error in Db::register_new_guardian: {}", e);
                use diesel::result::DatabaseErrorKind;
                use diesel::result::Error as DBError;
                match e {
                    DBError::DatabaseError(DatabaseErrorKind::UniqueViolation, _) => {
                        Error::UsernameTaken
                    }
                    _ => Error::Other,
                }
            })
    }

    pub fn login_guardian(&self, username: String, password: String) -> Result<GuardianId> {
        guardians::table
            .filter(guardians::username.eq(username))
            .filter(guardians::password.eq(password))
            .select(guardians::id)
            .first::<i32>(&*self.0.lock().unwrap())
            .map(GuardianId::from)
            .map_err(|e| {
                eprintln!("Error in Db::login_guardian: {:?}", e);
                Error::InvalidUsernameOrPassword
            })
    }

    pub fn register_new_child(
        &self,
        username: String,
        password: String,
        guardian_id: GuardianId,
    ) -> Result<ChildId> {
        #[derive(Insertable)]
        #[table_name = "children"]
        struct NewChild<'a> {
            pub username: &'a str,
            pub password: &'a str,
        }
        let guardian_id = guardian_id.0;
        let conn = self.0.lock().unwrap();
        conn.transaction(|| {
            let child_id = diesel::insert_into(children::table)
                .values(&NewChild {
                    username: &username,
                    password: &password,
                })
                .get_result::<Child>(&*conn)?
                .id;
            diesel::insert_into(guardian_has_children::table)
                .values(&GuardianHasChildren {
                    child_id: child_id.0,
                    guardian_id,
                })
                .execute(&*conn)?;
            Ok(child_id)
        })
        .map_err(|e| {
            eprintln!("Error in Db::register_new_child: {}", e);
            use diesel::result::DatabaseErrorKind;
            use diesel::result::Error as DBError;
            match e {
                DBError::DatabaseError(DatabaseErrorKind::ForeignKeyViolation, _) => {
                    Error::InvalidGuardian
                }
                DBError::DatabaseError(DatabaseErrorKind::UniqueViolation, _) => {
                    Error::UsernameTaken
                }
                _ => Error::Other,
            }
        })
    }

    pub fn login_child(&self, username: String, password: String) -> Result<ChildId> {
        children::table
            .filter(children::username.eq(username))
            .filter(children::password.eq(password))
            .select(children::id)
            .first::<i32>(&*self.0.lock().unwrap())
            .map(ChildId::from)
            .map_err(|e| {
                eprintln!("Error in Db::login_child: {:?}", e);
                Error::InvalidUsernameOrPassword
            })
    }

    pub fn update_child_location(&self, location: Location) -> Result<()> {
        diesel::insert_into(locations::table)
            .values(&location)
            .execute(&*self.0.lock().unwrap())
            .map(|_| ())
            .map_err(|e| {
                eprintln!("Error in Db::update_child_location: {:?}", e);
                // use diesel::result::DatabaseErrorKind;
                // use diesel::result::Error as DBError;
                match e {
                    _ => Error::Other,
                }
            })
    }

    pub fn child_location(&self, child: ChildId, guardian: GuardianId) -> Result<Vec<Location>> {
        guardian_has_children::table
            .filter(guardian_has_children::guardian_id.eq(guardian.0))
            .filter(guardian_has_children::child_id.eq(child.0))
            .select(guardian_has_children::child_id)
            .inner_join(
                locations::table.on(guardian_has_children::child_id.eq(locations::child_id)),
            )
            .select((locations::id, locations::child_id, locations::location))
            .load::<LocationInternal>(&*self.0.lock().unwrap())
            .map(|v| v.into_iter().map(Location::from).collect())
            .map_err(|e| {
                eprintln!("Error in Db::child_location-1: {:?}", e);
                use diesel::result::Error as DBError;
                match e {
                    DBError::NotFound => Error::InvalidChildOrGuardian,
                    _ => Error::Other,
                }
            })
    }

    pub fn guard_child(&self, child: ChildId, guardian: GuardianId) -> Result<()> {
        diesel::insert_into(guardian_has_children::table)
            .values(&GuardianHasChildren {
                child_id: child.0,
                guardian_id: guardian.0,
            })
            .execute(&*self.0.lock().unwrap())
            .map(|_| ())
            .map_err(|e| {
                eprintln!("Error in Db::guard_child: {:?}", e);
                use diesel::result::DatabaseErrorKind;
                use diesel::result::Error as DBError;
                match e {
                    DBError::DatabaseError(DatabaseErrorKind::ForeignKeyViolation, _) => {
                        Error::InvalidGuardian
                    }
                    DBError::DatabaseError(DatabaseErrorKind::UniqueViolation, _) => {
                        Error::AlreadyGuarding
                    }
                    _ => Error::Other,
                }
            })
    }

    pub fn list_children(&self, guardian: GuardianId) -> Result<Vec<ChildView>> {
        guardian_has_children::table
            .filter(guardian_has_children::guardian_id.eq(guardian.0))
            .inner_join(children::table)
            .select((children::id, children::username))
            .load::<ChildView>(&*self.0.lock().unwrap())
            .map_err(|e| {
                eprintln!("Error in Db::list_children: {:?}", e);
                Error::Other
            })
    }
}

#[derive(Insertable, Identifiable, Queryable, Associations)]
#[table_name = "guardian_has_children"]
#[belongs_to(Child)]
#[belongs_to(Guardian)]
#[primary_key(guardian_id, child_id)]
struct GuardianHasChildren {
    pub guardian_id: i32,
    pub child_id: i32,
}
