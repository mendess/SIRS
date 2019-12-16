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

<div class="notes">
Este nice notes para eu saber que dizer
</div>

# Implementation

![](./sirs_architecture.png)

## Secure channels {data-background-image="./sirs_architecture.png" data-background-opacity=0.2}

- Client ⇔ Server
- Guardian ⇔ Child

## Secure Protocols

- AES in CBC mode for session keys and the child/guardian shared secret
- RSA for server authentication with a private/public key pair

# Results

![](./app_use.jpeg)

# Demo

## Demo

<video data-autoplay src="sirs_demo.mp4"></video>
