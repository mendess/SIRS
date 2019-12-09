use openssl::rsa::Rsa;

use std::fs::File;
use std::io::Write;

fn main() {
    let rsa = Rsa::generate(2048).unwrap();
    let public_key = rsa.public_key_to_pem().unwrap();
    let private_key = rsa.private_key_to_pem().unwrap();
    File::create("public_key.pem").unwrap().write_all(&public_key).unwrap();
    File::create("private_key.pem").unwrap().write_all(&private_key).unwrap();
}
