# Server API

## The wrapping packet

Every request sent to the server needs to follow this format
```Json
{
  "iv": "00000",
  "payload": "base64"
}
```
Where
 - `iv` is a base64 encoded string of 16 bytes.
 - `payload` is a base64 encoded string encrypted with the shared secret

## API

### Register a new Guardian
#### Expected json
```Json
{
  "RegisterGuardian": {
    "username": "user",
    "password": "pass"
  }
}
```
#### Response
```Json
{
  "Ok": {
    "RegisterGuardian": {
      "id": 1
    }
  }
}
```

### Register a new Child
#### Expected json
```Json
{
  "RegisterChild": {
    "guardian": 1,
    "username": "child"
  }
}
```
#### Response
```Json
{
  "Ok": {
    "RegisterChild": {
      "id": 1
    }
  }
}
```
### List child locations
#### Request
```Json
{
  "ChildLocation": {
    "guardian": 1,
    "child": 1
  }
}
```
#### Response
```Json
{
  "Ok": {
    "ChildLocation": {
      "locations": [
        "some encryted blob",
        "gibberish"
      ]
    }
  }
}
```
### List children
#### Request
```Json
{
  "ListChildren": {
    "guardian": 1
  }
}
```
#### Response
```Json
{
  "Ok": {
    "ListChildren": {
      "children": [
        {
          "id": 1,
          "username": "child1"
        },
        {
          "id": 2,
          "username": "child2"
        }
      ]
    }
  }
}
```
### Update child location
#### Request
```Json
{
  "UpdateChildLocation": {
    "child": 1,
    "location": "some encryted blob"
  }
}
```
#### Response
```Json
{
"Ok": "UpdateChildLocation"
}
```

## Errors
All responses can respond with
```Json
{
  "Err": "ErrorName"
}
```
where `ErrorName` can be any name in [error.rs::Error](./src/error.rs)
