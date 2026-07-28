package com.jaidensiu.orbit.queue

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json

internal class FileQueueStorage(
    private val path: Path,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : QueueStorage {
    private val mutex = Mutex()

    override suspend fun append(event: QueuedEvent) {
        return mutex.withLock { writeAllLocked(events = readAllLocked() + event) }
    }

    override suspend fun readAll(): List<QueuedEvent> {
        return mutex.withLock { readAllLocked() }
    }

    override suspend fun removeIds(ids: Set<String>) {
        return mutex.withLock { writeAllLocked(events = readAllLocked().filterNot { it.id in ids }) }
    }

    override suspend fun replaceAll(events: List<QueuedEvent>) {
        return mutex.withLock { writeAllLocked(events) }
    }

    private fun readAllLocked(): List<QueuedEvent> {
        if (!SystemFileSystem.exists(path)) {
            return emptyList()
        }
        val content = SystemFileSystem.source(path).buffered().use { it.readString() }
        if (content.isBlank()) {
            return emptyList()
        }
        return content.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> runCatching { json.decodeFromString<QueuedEvent>(line) }.getOrNull() }
            .toList()
    }

    private fun writeAllLocked(events: List<QueuedEvent>) {
        val parent = path.parent
        if (parent != null && !SystemFileSystem.exists(path = parent)) {
            SystemFileSystem.createDirectories(path = parent)
        }
        val tempPath = Path(base = parent ?: Path("."), parts = arrayOf("${path.name}.tmp"))
        SystemFileSystem.sink(tempPath).buffered().use { sink ->
            events.forEach { event ->
                sink.writeString(json.encodeToString(value = event) + "\n")
            }
        }
        if (SystemFileSystem.exists(path)) {
            SystemFileSystem.delete(path)
        }
        SystemFileSystem.atomicMove(source = tempPath, destination = path)
    }
}
