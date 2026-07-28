package com.jaidensiu.orbit.queue

internal interface QueueStorage {
    suspend fun append(event: QueuedEvent)
    suspend fun readAll(): List<QueuedEvent>
    suspend fun removeIds(ids: Set<String>)
    suspend fun replaceAll(events: List<QueuedEvent>)
}
