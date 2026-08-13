package com.example.aegis.safety

class SensorRingBuffer(private val maxAgeMillis: Long = 120_000L) {

  private val samples = mutableListOf<SensorSample>()

  @Synchronized
  fun addSample(sample: SensorSample) {
    samples.add(sample)
    pruneOldSamples(sample.timestamp)
  }

  @Synchronized
  fun pruneOldSamples(currentTimestamp: Long) {
    val cutoff = currentTimestamp - maxAgeMillis
    samples.removeAll { it.timestamp < cutoff }
  }

  @Synchronized
  fun getBeforeWindow(eventTimestamp: Long, windowMillis: Long = 30_000L): List<SensorSample> {
    val start = eventTimestamp - windowMillis
    return samples.filter { it.timestamp in start until eventTimestamp }
  }

  @Synchronized
  fun getAfterWindow(eventTimestamp: Long, windowMillis: Long = 60_000L): List<SensorSample> {
    val end = eventTimestamp + windowMillis
    return samples.filter { it.timestamp in (eventTimestamp + 1)..end }
  }

  @Synchronized
  fun getSurroundingWindow(
    eventTimestamp: Long,
    beforeMillis: Long = 30_000L,
    afterMillis: Long = 60_000L,
  ): List<SensorSample> {
    val start = eventTimestamp - beforeMillis
    val end = eventTimestamp + afterMillis
    return samples.filter { it.timestamp in start..end }
  }

  @Synchronized
  fun getAllSamples(): List<SensorSample> {
    return samples.toList()
  }

  @Synchronized
  fun clear() {
    samples.clear()
  }
}
