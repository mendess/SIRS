#![feature(proc_macro_hygiene, decl_macro)]

mod error;
mod model;
mod schema;

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate diesel;

use error::Error;
use model::{Child, ChildId, Db, GuardianId, Location, NewLocation};
use rocket::{
    http::RawStr,
    request::FromParam,
    State,
};
use rocket_contrib::json::Json;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
struct RegisterGuardianRequest {
    username: String,
    password: String,
}

#[post("/guardian/create", data = "<request>")]
fn register_guardian(
    db: State<Db>,
    request: Json<RegisterGuardianRequest>,
) -> Result<String, Error> {
    let request = request.into_inner();
    db.register_new_guardian(request.username, request.password)
        .map(|i| i.to_string())
}

#[derive(Debug, Serialize, Deserialize)]
struct GetChildLocation {
    guardian: GuardianId,
    child: ChildId,
}

#[post("/guardian", data = "<request>")]
fn where_is_my_child(
    db: State<Db>,
    request: Json<GetChildLocation>,
) -> Result<Option<Json<Location>>, Error> {
    db.child_location(request.child, request.guardian)
        .map(|mut l| l.pop().map(|i| Json(i)))
}

#[get("/guardian/<guardian>")]
fn list_children(db: State<Db>, guardian: GuardianId) -> Result<Json<Vec<Child>>, Error> {
    db.list_children(guardian).map(|c| Json(c))
}

#[derive(Debug, Serialize, Deserialize)]
struct RegisterChild {
    guardian: GuardianId,
    username: String,
}

#[post("/child/create", data = "<request>")]
fn register_child(db: State<Db>, request: Json<RegisterChild>) -> Result<String, Error> {
    let request = request.into_inner();
    db.register_new_child(request.username, request.guardian)
        .map(|i| i.to_string())
}

#[post("/child", data = "<request>")]
fn update_child_location(db: State<Db>, request: Json<NewLocation>) -> Result<(), Error> {
    db.update_child_location(request.into_inner())
}

fn main() {
    rocket::ignite()
        .mount(
            "/",
            routes![
                where_is_my_child,
                register_guardian,
                register_child,
                update_child_location,
                list_children,
            ],
        )
        .manage(Db::default())
        .launch();
}

impl<'r> FromParam<'r> for GuardianId {
    type Error = &'r RawStr;

    fn from_param(param: &'r RawStr) -> Result<Self, Self::Error> {
        i32::from_param(param).map(GuardianId::from)
    }
}
