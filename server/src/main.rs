mod crypto;
mod error;
mod model;
mod schema;

#[macro_use]
extern crate diesel;

use crypto::{CryptoConfig, Packet};
use either::Either::{self, Left, Right};
use error::Error;
use model::{ChildId, ChildView, Db, GuardianId, Location};
use serde::{Deserialize, Serialize};
use serde_json::Deserializer;
use std::{
    io::{self, Write},
    net::{TcpListener, TcpStream},
    sync::Arc,
    thread,
};

#[derive(Serialize, Deserialize)]
enum Request {
    RegisterGuardian { username: String, password: String },
    LoginGuardian { username: String, password: String },
    RegisterChild { username: String, password: String },
    LoginChild { username: String, password: String },
    ChildLocation { child: ChildId },
    ListChildren,
    UpdateChildLocation { location: String },
}

#[derive(Serialize, Deserialize)]
enum Response {
    RegisterGuardian { id: GuardianId },
    LoginGuardian { id: GuardianId },
    RegisterChild { id: ChildId },
    LoginChild { id: ChildId },
    ChildLocation { locations: Vec<String> },
    ListChildren { children: Vec<ChildView> },
    Success,
}

impl Request {
    fn serve(
        self,
        db: &Db,
        logged_in: Option<Either<GuardianId, ChildId>>,
    ) -> Result<Response, Error> {
        use Request as R;
        use Response::*;
        match (self, logged_in) {
            (R::RegisterGuardian { username, password }, _) => db
                .register_new_guardian(username, password)
                .map(|id| RegisterGuardian { id }),
            (R::LoginGuardian { username, password }, _) => db
                .login_guardian(username, password)
                .map(|id| LoginGuardian { id }),
            (R::RegisterChild { username, password }, Some(Left(g))) => db
                .register_new_child(username, password, g)
                .map(|id| RegisterChild { id }),
            (R::LoginChild { username, password }, _) => db
                .login_child(username, password)
                .map(|id| LoginChild { id }),
            (R::ChildLocation { child }, Some(Left(g))) => {
                db.child_location(child, g).map(|v| ChildLocation {
                    locations: v.into_iter().map(|l| l.into_blob()).collect(),
                })
            }
            (R::ListChildren, Some(Left(g))) => {
                db.list_children(g).map(|l| ListChildren { children: l })
            }
            (R::UpdateChildLocation { location }, Some(Right(c))) => db
                .update_child_location(Location::new(c, location))
                .map(|_| Success),
            _ => Err(Error::NotLoggedIn),
        }
    }
}

struct Session {
    db: Arc<Db>,
    stream: TcpStream,
    crypto_config: CryptoConfig,
    logged_in: Option<Either<GuardianId, ChildId>>,
}

impl Session {
    fn start(mut stream: TcpStream, db: Arc<Db>) -> Result<(), io::Error> {
        let mut session = Self {
            db,
            crypto_config: CryptoConfig::new(&mut stream)?,
            stream,
            logged_in: None,
        };
        for packet in Deserializer::from_reader(session.stream.try_clone()?).into_iter() {
            let mut response = packet
                .map_err(Error::from)
                .and_then(|p| session.read_request(p))
                .and_then(|r| r.serve(&session.db, session.logged_in));
            use Response::*;
            session.logged_in = match response {
                Ok(RegisterGuardian { id }) | Ok(LoginGuardian { id }) => {
                    response = Ok(Success);
                    Some(Left(id))
                }
                Ok(LoginChild { id }) => {
                    response = Ok(Success);
                    Some(Right(id))
                }
                _ => session.logged_in,
            };
            session.write_response(response)?;
        }
        Ok(())
    }

    fn read_request(&mut self, packet: Packet) -> Result<Request, Error> {
        let thing = packet.decrypt(&self.crypto_config)?;
        eprintln!("payload: {}", std::str::from_utf8(&thing).unwrap());
        Ok(serde_json::from_slice(&thing)?)
    }

    fn write_response(&mut self, response: Result<Response, Error>) -> Result<(), io::Error> {
        let payload = serde_json::to_vec(&response).expect("Couldn't serialize response");
        let packet = Packet::new(payload, &self.crypto_config);
        serde_json::to_writer(&self.stream, &packet)?;
        self.stream.write_all(&[b'\n'])?;
        Ok(())
    }
}

fn main() {
    let database = Arc::new(Db::default());
    let listener = TcpListener::bind("0.0.0.0:6894").unwrap();
    eprintln!("Server started, listening on port 6894");
    for stream in listener.incoming() {
        let db_clone = Arc::clone(&database);
        thread::spawn(|| {
            let _ = stream
                .and_then(|s: TcpStream| {
                    eprintln!("Received connection from {:?}", s.peer_addr());
                    Session::start(s, db_clone)
                })
                .map_err(|e| eprintln!("Connection ended with: {:?}", e));
        });
    }
}

#[allow(dead_code)]
fn show_api() {
    let r = Request::RegisterGuardian {
        username: "user".into(),
        password: "pass".into(),
    };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::RegisterGuardian { id: 1.into() };
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());

    let r = Request::RegisterChild {
        username: "child".into(),
        password: "passchild".into(),
    };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::RegisterChild { id: 1.into() };
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());

    let r = Request::ChildLocation { child: 1.into() };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::ChildLocation {
        locations: vec!["some encryted blob".into(), "gibberish".into()],
    };
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());

    let r = Request::ListChildren;
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::ListChildren {
        children: vec![
            ChildView {
                id: 1.into(),
                username: "child1".into(),
            },
            ChildView {
                id: 2.into(),
                username: "child2".into(),
            },
        ],
    };
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());

    let r = Request::UpdateChildLocation {
        location: "some encryted blob".into(),
    };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::Success;
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());
}
