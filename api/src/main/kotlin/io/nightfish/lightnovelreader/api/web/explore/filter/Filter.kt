package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.util.LocalString

abstract class Filter<T>(default: T) {
    private data class Listener<T> (
        val listener: (T) -> Unit,
        val weight: Int = 0
    )

    var value: T = default
        set(value) {
            field = value
            onChange(value)
        }
    private var listeners = mutableListOf<Listener<T>>()

    abstract fun getTitle(): LocalString

    private fun onChange(value: T) {
        listeners.forEach {
            it.listener.invoke(value)
        }
    }

    fun addOnChangeListener(onChange: (T) -> Unit) {
        listeners.add(Listener(onChange))
        listeners = listeners.distinct().sortedBy { it.weight }.reversed().toMutableList()
    }

    fun addOnChangeListener(weight: Int, onChange: (T) -> Unit) {
        listeners.add(Listener(onChange, weight))
        listeners = listeners.distinct().sortedBy { it.weight }.reversed().toMutableList()
    }
}