# SpyKid

## Setup

### Backend (Linux)

#### Requirements
- Postgres
- The Rust programming language

#### Install
1. Change the [.env](server/.env) file to point to your database
2. Run the [setup.sh](./server/setup.sh)
3. Run `cargo run --release`

### Frontend

#### Requirements
- Android studio
- Android debug bridge (adb)
- Android phone

#### Install (Any)
1. Connect the phone through adb
2. Press `Run` with the application you want (child or guardian)
