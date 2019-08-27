 package com.beust.nnk
 
import java.io.Serializable
 
data class NonLinearity (
        val activate: ((Float) -> Float),
        val activateDerivative: ((Float) -> Float)): Serializable
 
enum class NonLinearities(val value: NonLinearity) {
    TANH(NonLinearity(
            { x -> Math.tanh(x.toDouble()).toFloat() },
            { x -> (1.0f - x * x) }
    )),
    RELU(NonLinearity(
            { x -> if (x > 0) x else 0f },
            { x -> if (x > 0) 1f else 0f }
    )),
    LEAKYRELU(NonLinearity(
            { x -> if (x > 0) x else 0.01f * x },
            { x -> if (x > 0) 1f else 0.01f }
    )),
    SOFTPLUS(NonLinearity(
            { x -> Math.log10(1 + Math.exp(x.toDouble())).toFloat() },
            { x -> 1 / (1 + Math.exp(-x.toDouble())).toFloat() }
    )),
    SIGMOID(NonLinearity(
            { x -> 1 / (1 + Math.exp(-x.toDouble())).toFloat() },
            { x -> Math.exp(x.toDouble() / Math.pow(1 + Math.exp(-x.toDouble()), 2.0)).toFloat() }
    )),
    SOFTSIGN(NonLinearity(
            { x ->  x / (1 + Math.abs(x)) },
            { x -> 1 / ((1+Math.abs(x))*(1+Math.abs(x)))}
    )),

}
