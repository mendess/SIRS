# Spykid

- Eva Verboom
- Felipe Gorostiaga
- Pedro Mendes

# The problem

As a guardian you might want to track your child's location to make sure they
don't stray too far from where they should be.

## Requirements

Among others it's important that the child's location be kept in absolute secret
as it is a very sensitive piece of information.

## Trust Assumptions

There are 3 actors in the system:

- Server
- Guardians
- Children

# Implementation

![](./sirs_architecture.png)

<div class="notes">
This is our architecture, as you can see there are 2 types of clients and a
server.

The client server communication is always done using a session key while the
indirect child guardian communication is encrypted with a shared secret.
</div>

## Secure channels {data-background-image="./sirs_architecture.png" data-background-opacity=0.2}

- Client ⇔ Server
- Guardian ⇔ Child

<div class="notes">
There are 2 kinds of secure channels, as mentioned before, the client server
communication is done with a session key, but all the sensitive information sent
to the server is encrypted with a secret shared between the guardian and the
child. This way, although these two clients never directly message each other
they can still communicate with confidentiality.
</div>

## Secure Protocols

<div style="flex">
<div>
- AES in CBC mode for session keys and the child/guardian shared secret
- RSA for server authentication with a private/public key pair
</div>
![](protocol.jpeg)
</div>

<div class="notes">
For the session keys and the shared secrets, since both are symmetric keys, we
opted for the AES algorithm in CBC mode as is recommended by the Android
Developer Community.

For authentication we use 2 distinct methods, one for the server which is a
public private key pair where the public key of the server is vendored with the
application to make sure it is the correct key.

For the authentication of the client we use a user password pair, sending the
password hashed and salted.
</div>

# Demo

![](./app_use.jpeg)

## Demo

<video data-autoplay src="sirs_demo.mp4"></video>
