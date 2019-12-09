use base64::DecodeError;
use openssl::error::ErrorStack;
use serde::{Deserialize, Serialize};
use serde_json::error::Error as SerdeError;
use std::error::Error as ErrorT;
use std::fmt::{self, Display};

pub type Result<T> = std::result::Result<T, Error>;

#[derive(Debug, Deserialize, Serialize)]
pub enum Error {
    AlreadyGuarding,
    CouldntDecodeB64,
    DecryptionFailed,
    InvalidChallenge,
    InvalidChild,
    InvalidChildOrGuardian,
    InvalidGuardian,
    InvalidPacketFormat,
    InvalidUsernameOrPassword,
    NotGuarding,
    NotLoggedIn,
    Other,
}

impl ErrorT for Error {}

impl Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{:?}", self)
    }
}

impl From<ErrorStack> for Error {
    fn from(_: ErrorStack) -> Self {
        Self::DecryptionFailed
    }
}

impl From<DecodeError> for Error {
    fn from(_: DecodeError) -> Self {
        Self::CouldntDecodeB64
    }
}

impl From<SerdeError> for Error {
    fn from(e: SerdeError) -> Self {
        eprintln!("Serde: {:?}", e);
        Self::InvalidPacketFormat
    }
}
