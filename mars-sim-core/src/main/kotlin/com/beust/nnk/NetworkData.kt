package com.beust.nnk

/**
 * Defines the inputs and expected outputs for a graph.
 */
class NetworkData(val inputs: List<Float>, val expectedOutputs: List<Float>) {
    companion object {
        fun create(inputs: List<Int>, targets: List<Int>)
            = NetworkData(inputs.map { it.toFloat() }, targets.map { it.toFloat() })
    }
    override fun toString() = inputs.toString() + " -> " + expectedOutputs
}