package com.beust.nnk

import kotlin.random.Random

import java.io.Serializable
//import java.util.Random
//import java.util.*

/**
 * A simple neural network with one hidden layer. Learning rate, momemntum and activation function are
 * all hardcoded in this example but should ideally be configurable.
 *
 * @author Cédric Beust <cedric@beust.com>
 * @since 5/02/2016
 */

class NeuralNetwork(val inputSize: Int, val hiddenSize: Int, val outputSize: Int,
					val hiddenNonLinearity: NonLinearity = NonLinearities.TANH.value,
					val outputNonLinearity: NonLinearity = NonLinearities.LEAKYRELU.value)
					: Serializable {

    val actualInputSize = inputSize + 1 // Add one for the bias node

    // Activations for nodes
    val activationInput = Vector(actualInputSize, { -> 1.0f })
    val activationHidden = Vector(hiddenSize, { -> 1.0f })
    val activationOutput = Vector(outputSize, { -> 1.0f })

    // Weights
    // Make sure our initial weights are not going to send our values into the saturated area of the
    // non linearity function, so spread them gently around 0. In theory, this should be a ratio function
    // of the fan-in (previous layer size) and fan-out (next layer size) but let's just hardcode for now
    val weightInput = Matrix(actualInputSize, hiddenSize, { -> rand(-0.2f, 0.2f) })
    val weightOutput = Matrix(hiddenSize, outputSize, { -> rand(-0.2f, 0.2f) })

    // Weights for momentum
    val momentumInput = Matrix(actualInputSize, hiddenSize)
    val momentumOutput = Matrix(hiddenSize, outputSize)

    /** Fix the random seed for reproducible numbers while debugging */
    companion object {
        val random = Random(1)
    }
    fun rand(min: Float, max: Float) = random.nextFloat() * (max - min) + min


    /**
     * Run the graph with the given inputs.
     *
     * @return the outputs as a vector.
     */
    fun runGraph(inputs: List<Float>, logLevel: Int) : Vector {
        if (inputs.size != actualInputSize -1) {
            throw RuntimeException("Expected ${actualInputSize - 1} inputs but got ${inputs.size}")
        }

        // Input activations (note: -1 since we don't count the bias node)
        repeat(actualInputSize - 1) {
            activationInput[it] = inputs[it]
        }

        // Hidden activations
        repeat(hiddenSize) { j ->
            var sum = 0.0f
            repeat(actualInputSize) { i ->
//                val w: List<Float> = weightInput[i]
                log(logLevel, "    sum += ai[$i] ${activationInput[i]} * wi[i][j] ${weightInput[i][j]}")
                sum += activationInput[i] * weightInput[i][j]
            }
            activationHidden[j] = hiddenNonLinearity.activate(sum)
            log(logLevel, "    final sum going into ah[$j]: " + activationHidden[j])
        }

        // Output activations
        repeat(outputSize) { k ->
            var sum = 0.0f
            repeat(hiddenSize) { j ->
                log(logLevel, "    sum += ah[$j] ${activationHidden[j]} * wo[$j][$k] ${weightOutput[j][k]}")
                log(logLevel, "         = " + activationHidden[j] * weightOutput[j][k])
                sum += activationHidden[j] * weightOutput[j][k]
            }
            activationOutput[k] = outputNonLinearity.activate(sum)
            log(logLevel, "  activate(sum $sum) = " + activationOutput)
            
        }

        return activationOutput
    }

    /**
     * Use the targets to backpropagate through the graph, starting with the output, then the hidden
     * layer and then the input.
     *
     * @return the error
     */
    fun backPropagate(targets: List<Float>, learningRate: Float, momentum: Float) : Float {
        if (targets.size != outputSize) {
            throw RuntimeException("Expected $outputSize targets but got ${targets.size}")
        }

        // Calculate error terms for output
        val outputDeltas = Vector(outputSize)
        repeat(outputSize) { k ->
            val error = targets[k] - activationOutput[k]
            outputDeltas[k] = outputNonLinearity.activateDerivative(activationOutput[k]) * error
        }

        // Calculate error terms for hidden layers
        val hiddenDeltas = Vector(hiddenSize)
        repeat(hiddenSize) { j ->
            var error = 0.0f
            repeat(outputSize) { k ->
                error += outputDeltas[k] * weightOutput[j][k]
            }
            hiddenDeltas[j] = hiddenNonLinearity.activateDerivative(activationHidden[j]) * error
        }

        // Update output weights
        repeat(hiddenSize) { j ->
            repeat(outputSize) { k ->
                val change = outputDeltas[k] * activationHidden[j]
                log(3, "      weightOutput[$j][$k] changing from " + weightOutput[j][k]
                        + " to " + (weightOutput[j][k] + learningRate * change + momentum * momentumOutput[j][k]))
                weightOutput[j][k] = weightOutput[j][k] + learningRate * change + momentum * momentumOutput[j][k]
                momentumOutput[j][k] = change
            }
        }

        // Update input weights
        repeat(actualInputSize) { i ->
            repeat(hiddenSize) { j ->
                val change = hiddenDeltas[j] * activationInput[i]
                log(3, "      weightInput[$i][$j] changing from " + weightInput[i][j]
                        + " to " + (weightInput[i][j] + learningRate * change + momentum * momentumInput[i][j]))
                weightInput[i][j] = weightInput[i][j] + learningRate * change + momentum * momentumInput[i][j]
                momentumInput[i][j] = change
            }
        }

        // Calculate error
        var error = 0.0
        repeat(targets.size) { k ->
            val diff = targets[k] - activationOutput[k]
            error += 0.5 * diff * diff
        }

        log(3, "      new error: " + error)
        return error.toFloat()
    }

    /**
     * Train the graph with the given NetworkData, which contains pairs of inputs and expected outputs.
     */
    fun train(networkDatas: List<NetworkData>, iterations: Int, learningRate: Float = 0.5f,
              momentum: Float = 0.1f) {
        repeat(iterations) { iteration ->
            var error = 0.0f
            networkDatas.forEach { pattern ->
                log(3, "  Current input: " + pattern.inputs)
                runGraph(pattern.inputs, logLevel = 3)
                val bp = backPropagate(pattern.expectedOutputs, learningRate, momentum)
                error += bp
            }
            if (iteration % 100 == 0) {
                log(3, "  Iteration $iteration Error: $error")
            }
        }
    }

    fun test(networkDatas: List<NetworkData>) {
        networkDatas.forEach {
//            log(1, it.inputs.toString() + " -> " +
					runGraph(it.inputs, logLevel = 3)
//			)
        }
    }

    fun dump() {
        log(2, "Input weights:\n" + weightInput.dump())
        log(2, "Output weights:\n" + weightOutput.dump())
    }
}

//    class Vector2(val size: Int, val defaultValue: () -> Float = { -> 0.0f }) : Matrix(size, 1, defaultValue) {
//        operator fun set(it: Int, value: Float) {
//            this[it][0] = value
//        }
//        override operator fun get(i: Int) = this[i][0]
//    }


