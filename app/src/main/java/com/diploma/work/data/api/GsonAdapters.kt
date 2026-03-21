package com.diploma.work.data.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.Instant

class LongTypeAdapter : TypeAdapter<Long>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Long? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.STRING -> {
                val str = reader.nextString()
                if (str.isNullOrEmpty()) 0L else str.toLongOrNull() ?: 0L
            }
            JsonToken.NUMBER -> reader.nextLong()
            else -> {
                reader.skipValue()
                0L
            }
        }
    }
}

class IntTypeAdapter : TypeAdapter<Int>() {
    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.STRING -> {
                val str = reader.nextString()
                if (str.isNullOrEmpty()) 0 else str.toIntOrNull() ?: 0
            }
            JsonToken.NUMBER -> reader.nextInt()
            else -> {
                reader.skipValue()
                0
            }
        }
    }
}

class InstantTypeAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())
        }
    }

    override fun read(reader: JsonReader): Instant? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.STRING -> {
                val str = reader.nextString()
                if (str.isNullOrEmpty()) null
                else try {
                    Instant.parse(str)
                } catch (_: Exception) {
                    null
                }
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
