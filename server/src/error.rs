use rocket::{
    http::{ContentType, Status},
    response::{self, Responder},
    Request, Response,
};
use std::error::Error as ErrorT;
use std::fmt::{self, Display};
use std::io::Cursor;

pub type Result<T> = std::result::Result<T, Error>;

#[derive(Debug)]
pub enum Error {
    InvalidChild,
    InvalidGuardian,
    NotGuarding,
    AlreadyGuarding,
    InvalidChildOrGuardian,
    Other,
}

impl ErrorT for Error {}

impl Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{:?}", self)
    }
}

impl<'r> Responder<'r> for Error {
    fn respond_to(self, _: &Request) -> response::Result<'r> {
        Response::build()
            .header(ContentType::Plain)
            .sized_body(Cursor::new(format!("{}", self)))
            .status(Status::BadRequest)
            .ok()
    }
}
