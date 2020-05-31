package com.ignacio.simpleperceptronkotlin

import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * A Perceptron is a machine that can organize things in classes or groups depending on some criteria.
 * This case is the simplest one, and our Perceptron is uni-dimensional. This is to say, we are going to organize
 * inputs based only in one property. In the case of points, it could be for example whether they are over or under
 * a given line. Here when we say "uni-dimensional we refer to the criteria. To be over or under a line is just
 * one dimension we are organizing our inputs against.
 *
 * The lenght of a Perceptron input is all the inputs + a bias. We include the bias as one more input in
 * the inpus array.
 */
class Perceptron(size: Int, val learningConstant: Float = 0.01f) {

    val weights: FloatArray = FloatArray(size).apply {
        for(i in indices) {
            this[i] = Random.nextInt(-100, 100)/100f
        }
    }

    /**
     * Feed forward: Generate an output from our array of inputs (x,y coordinates of our points).
     */
    fun feedForward(inputs: FloatArray): Int {
        var sum = 0f
        for(i in weights.indices) {
            sum += inputs[i] * weights[i]
        }
        return activate(sum)
    }

    /**
     * Activation function: Gives our Perceptron's output. This is our Perceptron's guess. Is the
     */
    private fun activate(sum: Float): Int {
        return if(sum > 0) 1 else -1
    }

    /**
     * What it means to train a Perceptron?
     * 1. Get our guess on an input with feedForward(input)
     * 2. Get the error, namely, difference from our guess to desired result.
     * 3. Adjust the Perceptron's weights with the error.
     * We use a learningConstant, this is something {0,1} that determines the speed of our correction.
     * When it's too small, learning will be slow, when it's too big we could skip the right solution.
     */
    fun train(inputs: FloatArray, desiredOutput: Int) {
        val guess: Int = feedForward(inputs)
        val error = desiredOutput - guess.toFloat()
        for (i in weights.indices) {
            weights[i] += learningConstant * error * inputs[i]
        }
    }
}

class PerceptronEvaluator() {
    fun evaluate(inputs: FloatArray) {
        val perceptron = Perceptron(3)
        val point: FloatArray = inputs + 1f
        println("point: $point")
        val result = perceptron.feedForward(point)
        println("result: $result")
    }
}

data class TrainingCase(
    val inputs: FloatArray,
    val answer: Int
)

data class GraphicSize(
    val width: Int,
    val height: Int
)

class PerceptronTrainer(
    val graphicSize: GraphicSize,
    val drawer: GuessDrawer,
    val bias: Float = 1f
) {
    val perceptronJob = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.Default + perceptronJob + CoroutineName("perceptronCoroutine"))

    lateinit var trainingSet: Array<TrainingCase>
    lateinit var perceptron: Perceptron
    lateinit var line: (Float) -> Float

    fun initialize(trainingSize: Int, line: (Float) -> Float) {
        this.line = line
        trainingSet = Array(trainingSize) {
            val x = randomFloatInInterval(-graphicSize.width/2f, graphicSize.width/2f)
            val y = randomFloatInInterval(-graphicSize.height/2f, graphicSize.height/2f)
            val answer = if(y < line(x)) -1 else 1
            TrainingCase(floatArrayOf(x, y, bias), answer)
        }
        this.perceptron = Perceptron(3)
    }

    fun train() {
        coroutineScope.launch {
            for(i in trainingSet.indices) {
                val case = trainingSet[i]
                println("training case is $i: $case")
                perceptron.train(case.inputs, case.answer)
                drawPerceptronGuessesAfterTrainingIteration(i)
                delay(10)
            }
        }
    }

    private suspend fun drawPerceptronGuessesAfterTrainingIteration(iterationNumber: Int) {
        for (j in 0..iterationNumber) {
            val drawingInputs: FloatArray = getDrawingInput(trainingSet[j].inputs)
            val guess = perceptron.feedForward(drawingInputs + bias)
            drawTheGuess(drawingInputs, guess)
        }
    }

    fun getDrawingInput(inputs: FloatArray): FloatArray {
        // move our inputs so that they fit in the view
        return floatArrayOf(
            inputs[0] + graphicSize.width/2f,
            inputs[1] + graphicSize.height/2f
        )
    }

    fun getOriginalPoints(): FloatArray {
        val out = (-graphicSize.width..graphicSize.width).map {
            floatArrayOf(it.toFloat(), line(it.toFloat()))
        }.reduce { acc, floats -> acc + floats }
        println("original points: ${out.map { it }}")
        return out
    }

    private suspend fun drawTheGuess(inputs: FloatArray, guess: Int) {
        withContext(Dispatchers.Main) {
            drawer.draw(inputs, guess)
        }
    }

    interface GuessDrawer {
        fun draw(inputs: FloatArray, guess: Int)
    }

    private fun randomFloatInInterval(a: Float, b: Float): Float {
        return if(b > a) {
            Random.nextFloat()*(b - a)+a
        }
        else {
            Random.nextFloat()*(a - b)+b
        }
    }

    fun close() {
        coroutineScope.cancel()
    }
}