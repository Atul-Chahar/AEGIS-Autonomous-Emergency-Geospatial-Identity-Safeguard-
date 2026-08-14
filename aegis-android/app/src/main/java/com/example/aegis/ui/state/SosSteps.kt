package com.example.aegis.ui.state

import com.example.aegis.domain.model.SosDispatchResult

/**
 * Maps the real emergency dispatch state to the user-facing SOS progress steps.
 *
 * Honesty rules (per DESIGN.md / UI spec):
 *  - A step only shows a checkmark (SUCCEEDED) when the real state model says so.
 *  - PendingSmsFallback means the packet is stored locally with an SMS handoff
 *    payload ready — it is NOT a delivered checkmark, so SMS fallback and relay
 *    search stay IN_PROGRESS until connectivity confirms delivery.
 *  - "Authority received" only becomes SUCCEEDED when the backend ack arrives (Sent).
 */
fun buildSosSteps(
  dispatching: Boolean,
  dispatchResult: SosDispatchResult?,
  hasLocationFix: Boolean,
  blackBoxAttached: Boolean,
): List<SosProgressStep> {
  val recorded =
    when {
      dispatchResult != null -> SosStepStatus.SUCCEEDED
      dispatching -> SosStepStatus.IN_PROGRESS
      else -> SosStepStatus.PENDING
    }

  val locationLocked =
    when {
      hasLocationFix -> SosStepStatus.SUCCEEDED
      dispatching -> SosStepStatus.IN_PROGRESS
      else -> SosStepStatus.PENDING
    }

  val blackBox =
    if (blackBoxAttached) SosStepStatus.SUCCEEDED else SosStepStatus.PENDING

  val sendingInternet =
    when {
      dispatchResult is SosDispatchResult.Sent &&
        dispatchResult.transport.contains("Internet", ignoreCase = true) ->
        SosStepStatus.SUCCEEDED
      dispatchResult is SosDispatchResult.PendingSmsFallback ||
        dispatchResult is SosDispatchResult.Failed ||
        dispatchResult is SosDispatchResult.NotAvailable ->
        SosStepStatus.FAILED
      dispatching -> SosStepStatus.IN_PROGRESS
      else -> SosStepStatus.PENDING
    }

  val smsFallback =
    if (dispatchResult is SosDispatchResult.PendingSmsFallback) {
      SosStepStatus.IN_PROGRESS
    } else {
      SosStepStatus.PENDING
    }

  val relaySearch =
    if (dispatchResult is SosDispatchResult.PendingSmsFallback) {
      SosStepStatus.IN_PROGRESS
    } else {
      SosStepStatus.PENDING
    }

  val authorityReceived =
    if (dispatchResult is SosDispatchResult.Sent) SosStepStatus.SUCCEEDED else SosStepStatus.PENDING

  return listOf(
    SosProgressStep("Emergency recorded", recorded),
    SosProgressStep("Location locked", locationLocked),
    SosProgressStep("Journey BlackBox attached", blackBox),
    SosProgressStep("Sending via internet", sendingInternet),
    SosProgressStep("SMS fallback", smsFallback),
    SosProgressStep("Searching for offline relay", relaySearch),
    SosProgressStep("Authority received", authorityReceived),
  )
}

/** Offline guidance shown when the emergency was stored locally but could not reach the network. */
fun offlineMessageFor(dispatchResult: SosDispatchResult?): String? =
  if (dispatchResult is SosDispatchResult.PendingSmsFallback) {
    "No internet. Your emergency has been safely stored. Searching for a nearby relay."
  } else {
    null
  }
