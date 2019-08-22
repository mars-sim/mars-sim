package com.beust.nnk

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.random.Random

var LOG_LEVEL = 2

var nn = NeuralNetwork(inputSize = 5, hiddenSize = 5, outputSize = 1)

fun log(level: Int, s: String) {
    if (LOG_LEVEL >= level) println(s)
}

fun main(argv: Array<String>) {
    class Args {
        @Parameter(names = arrayOf("--log"), description = "Define the log level (1-3)")
        var log: Int = 2
    }

    val args = Args()
    JCommander(args).parse(*argv)
    LOG_LEVEL = args.log

	log(1, "Training 1:")
	feed()

	log(1, "Training 2:")	
	feed()

	log(1, "Training 3:")		
	feed()
	
	log(1, "Training 4:")	
	feed()
	
	log(1, "Training 5:")		
	feed()
	
	log(1, "Training 6:")
	feed()

	log(1, "Training 7:")	
	feed()

	log(1, "Training 8:")		
	feed()
	
	log(1, "Training 9:")	
	feed()
	
	log(1, "Training 10:")		
	feed()
	

}

// Astronomy :
// Pri : 2 --> 0, 0, 0, 1, 0 
val astronomy0 = listOf(0, 0, 0, 1, 0)
// Sec : 3 --> 0, 0, 0, 1, 1
val astronomy1 = listOf(0, 0, 0, 1, 1)

fun feed() {
    with(nn) {
        val trainingValues = listOf(

			
            NetworkData.create(listOf(0, 0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 0, 1), listOf(0)),
			// Primary stimulus
			NetworkData.create(astronomy0, listOf(1)),
			NetworkData.create(astronomy0, listOf(1)),
//			NetworkData.create(astronomy0, listOf(1)),
			// Secondary stimulus
			NetworkData.create(astronomy1, listOf(1)),
//			NetworkData.create(astronomy1, listOf(1)),
			
            NetworkData.create(listOf(0, 0, 1, 0, 0), listOf(0)),
			NetworkData.create(listOf(0, 0, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 1), listOf(0)),
					
		    NetworkData.create(listOf(0, 1, 0, 0, 0), listOf(0)),
			NetworkData.create(listOf(0, 1, 0, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 1, 0, 1, 0), listOf(0)),
			NetworkData.create(listOf(0, 1, 0, 1, 1), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 1, 1), listOf(0)),
			
			NetworkData.create(listOf(1, 0, 0, 0, 0), listOf(0)),
			NetworkData.create(listOf(1, 0, 0, 0, 1), listOf(0)),
            NetworkData.create(listOf(1, 0, 0, 1, 0), listOf(0)),
			NetworkData.create(listOf(1, 0, 0, 1, 1), listOf(0)),
            NetworkData.create(listOf(1, 0, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(1, 0, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(1, 0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(1, 0, 1, 1, 1), listOf(0)),
			
		    NetworkData.create(listOf(1, 1, 0, 0, 0), listOf(0)),
			NetworkData.create(listOf(1, 1, 0, 0, 1), listOf(0)),
            NetworkData.create(listOf(1, 1, 0, 1, 0), listOf(0)),
			NetworkData.create(listOf(1, 1, 0, 1, 1), listOf(0)),
            NetworkData.create(listOf(1, 1, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(1, 1, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(1, 1, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(1, 1, 1, 1, 1), listOf(0))
		
        )
		
        train(trainingValues, 10)

		val r0 = (0..1).random()
		val r1 = (0..1).random()
		val r2 = (0..1).random()
		val r3 = (0..1).random()
		val r4 = (0..1).random()
					
        val testValues = listOf(
			NetworkData.create(listOf(0, 0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, r1, r2, r3, r4), listOf(0)),
            NetworkData.create(listOf(r2, 0, r3, r0, r1), listOf(0)),
            NetworkData.create(listOf(r3, r4, 0, r1, r2), listOf(0)),
			
            NetworkData.create(listOf(r4, r3, r2, 0, 1), listOf(0)),
            NetworkData.create(listOf(r4, r3, r2, 1, 0), listOf(0)),
            NetworkData.create(listOf(r3, r2, r1, 0, 1), listOf(0)),
            NetworkData.create(listOf(r3, r2, r1, 1, 0), listOf(0)),
			
			NetworkData.create(astronomy0, listOf(1)),			
            NetworkData.create(astronomy1, listOf(1))
        )
		
        test(testValues)
		
		println()
		
        dump()
		
		println()
		
    }
}
