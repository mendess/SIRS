#![feature(proc_macro_hygiene, decl_macro)]

mod model;

#[macro_use]
extern crate rocket;

use model::{ChildId, Db, GuardianId, Location};
use rocket::State;
use rocket_contrib::json::Json;
use serde::{Deserialize, Serialize};

#[get("/child")]
fn im_here() {}

#[derive(Debug, Serialize, Deserialize)]
struct GetChildLocation {
    guardian: GuardianId,
    child: ChildId,
}

#[post("/guardian", data = "<request>")]
fn where_is_my_child(db: State<Db>, request: Json<GetChildLocation>) -> Option<Json<Location>> {
    db.child_location(request.child, request.guardian)
        .map(|l| Json(l))
}

#[derive(Debug, Serialize, Deserialize)]
struct RegisterGuardianRequest {
    username: String,
    password: Vec<u8>,
}

#[post("/guardian/create", data = "<request>")]
fn register_guardian(db: State<Db>, request: Json<RegisterGuardianRequest>) -> String {
    let request = request.into_inner();
    db.register_new_guardian(request.username, request.password)
        .to_string()
}

#[derive(Debug, Serialize, Deserialize)]
struct RegisterChild {
    guardian: GuardianId,
}

#[post("/child/create", data = "<request>")]
fn register_child(db: State<Db>, request: Json<RegisterChild>) -> String {
    db.register_new_child(request.guardian).to_string()
}

#[derive(Debug, Serialize, Deserialize)]
struct ChildLocation {
    location: Location,
    child: ChildId,
}

#[post("/child", data = "<request>")]
fn update_child_location(db: State<Db>, request: Json<ChildLocation>) {
    db.update_child_location(request.child, request.location)
}

fn main() {
    rocket::ignite()
        .mount(
            "/",
            routes![
                where_is_my_child,
                register_guardian,
                register_child,
                update_child_location
            ],
        )
        .manage(Db::default())
        .launch();
}
