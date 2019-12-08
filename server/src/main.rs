mod crypto;
mod error;
mod model;
mod schema;

#[macro_use]
extern crate diesel;

use crypto::{CryptoConfig, Packet};
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
    RegisterGuardian {
        username: String,
        password: String,
    },
    LoginGuardian {
        username: String,
        password: String,
    },
    RegisterChild {
        guardian: GuardianId,
        username: String,
        password: String,
    },
    LoginChild {
        username: String,
        password: String,
    },
    ChildLocation {
        guardian: GuardianId,
        child: ChildId,
    },
    ListChildren {
        guardian: GuardianId,
    },
    UpdateChildLocation {
        child: ChildId,
        location: String,
    },
}

#[derive(Serialize, Deserialize)]
enum Response {
    RegisterGuardian { id: GuardianId },
    LoginGuardian { id: GuardianId },
    RegisterChild { id: ChildId },
    LoginChild { id: ChildId },
    ChildLocation { locations: Vec<String> },
    ListChildren { children: Vec<ChildView> },
    UpdateChildLocation,
}

impl Request {
    #[allow(unused_variables)]
    fn serve(self, db: &Db) -> Result<Response, Error> {
        use Request::*;
        use Response as R;
        match self {
            RegisterGuardian {
                username: u,
                password: p,
            } => db
                .register_new_guardian(u, p)
                .map(|id| R::RegisterGuardian { id }),
            LoginGuardian {
                username: u,
                password: p,
            } => db.login_guardian(u, p).map(|id| R::LoginGuardian { id }),
            RegisterChild {
                guardian: g,
                username: u,
                password: p,
            } => db
                .register_new_child(u, p, g)
                .map(|id| R::RegisterChild { id }),
            LoginChild {
                username: u,
                password: p,
            } => db.login_child(u, p).map(|id| R::LoginChild { id }),
            ChildLocation {
                guardian: g,
                child: c,
            } => db.child_location(c, g).map(|v| R::ChildLocation {
                locations: v.into_iter().map(|l| l.into_blob()).collect(),
            }),
            ListChildren { guardian: g } => {
                db.list_children(g).map(|l| R::ListChildren { children: l })
            }
            UpdateChildLocation {
                child: c,
                location: l,
            } => db
                .update_child_location(Location::new(c, l))
                .map(|_| R::UpdateChildLocation),
        }
    }
}

struct Session {
    db: Arc<Db>,
    stream: TcpStream,
    crypto_config: CryptoConfig,
}

impl Session {
    fn start(mut stream: TcpStream, db: Arc<Db>) -> Result<(), io::Error> {
        let mut session = Self {
            db,
            crypto_config: CryptoConfig::new(&mut stream)?,
            stream,
        };
        for packet in Deserializer::from_reader(session.stream.try_clone()?).into_iter() {
            let response = packet
                .map_err(Error::from)
                .and_then(|p| session.read_request(p))
                .and_then(|r| r.serve(&session.db));
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
        guardian: 1.into(),
        username: "child".into(),
        password: "passchild".into(),
    };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::RegisterChild { id: 1.into() };
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());

    let r = Request::ChildLocation {
        guardian: 1.into(),
        child: 1.into(),
    };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::ChildLocation {
        locations: vec!["some encryted blob".into(), "gibberish".into()],
    };
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());

    let r = Request::ListChildren { guardian: 1.into() };
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
        child: 1.into(),
        location: "some encryted blob".into(),
    };
    println!("Request: {}", serde_json::to_string_pretty(&r).unwrap());
    let r = Response::UpdateChildLocation;
    println!("Response: {}", serde_json::to_string_pretty(&r).unwrap());
}
