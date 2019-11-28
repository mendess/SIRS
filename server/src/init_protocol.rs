use rand::{rngs::StdRng, SeedableRng};
use std::io::{self, Write};
use x25519_dalek::{EphemeralSecret, PublicKey, SharedSecret};

pub fn exchange_dh<W: Write>(
    client_public: PublicKey,
    output: &mut W,
) -> Result<SharedSecret, io::Error> {
    let mut rng = StdRng::from_seed(Default::default());
    let server_secret = EphemeralSecret::new(&mut rng);
    let server_public = PublicKey::from(&server_secret);
    output.write_all(server_public.as_bytes())?;
    Ok(server_secret.diffie_hellman(&client_public))
}
