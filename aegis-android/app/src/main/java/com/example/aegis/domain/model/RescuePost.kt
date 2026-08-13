package com.example.aegis.domain.model

/** Nearest rescue / checkpoint post for a zone. */
data class RescuePost(
  val name: String,
  val location: String,
  val distance: String,
  val eta: String,
  val rating: String,
)
