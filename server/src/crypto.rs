use crate::error;
use once_cell::sync::OnceCell;
use openssl::{
    pkey::Private,
    rsa::{Padding, Rsa},
    symm::{self, Cipher},
};
use rand::{rngs::StdRng, RngCore, SeedableRng};
use serde::{Deserialize, Serialize};
use std::{
    cell::RefCell,
    io::{self, Read, Write},
};

pub fn decrypt_with_private_k(data: [u8; 256]) -> [u8; 32] {
    static PRIVATE_KEY: OnceCell<Rsa<Private>> = OnceCell::new();
    let pkey = PRIVATE_KEY.get_or_init(|| {
        static PRIVATE_KEY_BYTES: &[u8] = include_bytes!("../resources/private_key.pem");
        Rsa::private_key_from_pem(PRIVATE_KEY_BYTES).unwrap()
    });
    let mut cryptogram = [0_u8; 256];
    pkey.private_decrypt(&data, &mut cryptogram, Padding::NONE)
        .expect("Why did you fail me, my son");
    std::convert::TryFrom::try_from(&cryptogram[(256 - 32)..]).unwrap()
}

fn generate_challenge(rng: &mut StdRng) -> [u8; 32] {
    let mut challenge = [0_u8; 32];
    rng.fill_bytes(&mut challenge);
    challenge
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Packet {
    iv: String,
    payload: String,
}

pub struct CryptoConfig {
    cipher: Cipher,
    session_key: [u8; 32],
    challenge: [u8; 32],
    rng: RefCell<StdRng>,
}

impl CryptoConfig {
    pub fn new<S>(mut stream: S) -> Result<Self, io::Error>
    where
        S: Read + Write,
    {
        let mut session_key = [0_u8; 256];
        stream.read_exact(&mut session_key)?;
        let session_key = decrypt_with_private_k(session_key);
        eprintln!("Session key: {:?}", session_key);
        let mut rng = StdRng::from_seed(Default::default());
        let challenge = generate_challenge(&mut rng);
        eprintln!("Challenge: {:?}", challenge);
        stream.write_all(&challenge)?;
        Ok(Self {
            cipher: Cipher::aes_256_cbc(),
            session_key,
            challenge,
            rng: RefCell::new(rng),
        })
    }
}

impl Packet {
    pub fn decrypt(self, config: &CryptoConfig) -> error::Result<Vec<u8>> {
        let mut payload = symm::decrypt(
            config.cipher,
            &config.session_key,
            Some(&base64::decode(&self.iv)?),
            &base64::decode(&self.payload)?,
        )?;
        if payload.len() < 32 {
            Err(error::Error::DecryptionFailed)
        } else if &payload[(payload.len() - 32)..] != config.challenge {
            eprintln!("Received challenge: {:?}", &payload[(payload.len() - 32)..]);
            Err(error::Error::InvalidChallenge)
        } else {
            payload.truncate(payload.len() - 32);
            Ok(payload)
        }
    }

    pub fn new(payload: Vec<u8>, config: &CryptoConfig) -> Packet {
        let mut iv = [0_u8; 16];
        config.rng.borrow_mut().fill_bytes(&mut iv);

        let payload = symm::encrypt(config.cipher, &config.session_key, Some(&iv), &payload)
            .expect("Couldn't encrypt message");

        Packet {
            payload: base64::encode(&payload),
            iv: base64::encode(&iv),
        }
    }
}
