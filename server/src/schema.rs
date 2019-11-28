table! {
    children (id) {
        id -> Int4,
        username -> Varchar,
        password -> Varchar,
    }
}

table! {
    guardian_has_children (child_id, guardian_id) {
        child_id -> Int4,
        guardian_id -> Int4,
    }
}

table! {
    guardians (id) {
        id -> Int4,
        username -> Varchar,
        password -> Varchar,
    }
}

table! {
    locations (id) {
        id -> Int4,
        child_id -> Int4,
        location -> Varchar,
    }
}

joinable!(guardian_has_children -> children (child_id));
joinable!(guardian_has_children -> guardians (guardian_id));
joinable!(locations -> children (child_id));

allow_tables_to_appear_in_same_query!(
    children,
    guardian_has_children,
    guardians,
    locations,
);
