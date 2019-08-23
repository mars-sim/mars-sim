package com.beust.nnk

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.random.Random

var LOG_LEVEL = 2

var nn = NeuralNetwork(inputSize = 5, hiddenSize = 5, outputSize = 1)

fun log(level: Int, s: String) {
    if (LOG_LEVEL >= level) println(s)
}

val dec = 4

// Astronomy :
// Pri : 6 
val astronomy0 = listOf(0, 0, 1, 1, 0)
// Sec : 7 
val astronomy1 = listOf(0, 0, 1, 1, 1)

// Piloting :
// Pri : 5 
val piloting0 = listOf(0, 0, 1, 0, 1)
// Sec : 4 
val piloting1 = listOf(0, 0, 1, 0, 0)

fun Pilot() {
	   with(nn) {
        val trainingValues = listOf(	
			// Piloting secondary
			NetworkData.create(piloting1, listOf(1)),
			
			// Piloting primary
			NetworkData.create(piloting0, listOf(1))

        )
		
        train(trainingValues, 1)
    }
}

fun Astro() {
    with(nn) {
        val trainingValues = listOf(

			// Astronomy Primary 
			NetworkData.create(astronomy0, listOf(1)),
	
			// Astronomy Secondary
			NetworkData.create(astronomy1, listOf(1))
		
        )
		
        train(trainingValues, 1)
    }
}

fun Training0() {
    with(nn) {
        val trainingValues = listOf(
	
            NetworkData.create(listOf(0, 0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 1, 0), listOf(0)),
		
			// Astronomy Primary 
//			NetworkData.create(astronomy0, listOf(1)),
		
            NetworkData.create(listOf(0, 0, 0, 1, 1), listOf(0)),
	
			// Astronomy Secondary
//			NetworkData.create(astronomy1, listOf(1)),
            NetworkData.create(listOf(0, 0, 1, 0, 0), listOf(0)),
			NetworkData.create(listOf(0, 0, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 1), listOf(0)),		
//		    NetworkData.create(listOf(0, 1, 0, 0, 0), listOf(0)),
			
			// Piloting secondary
			NetworkData.create(piloting1, listOf(1)),
			
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
			
			// Piloting primary
//			NetworkData.create(piloting0, listOf(1)),
							
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
		
        train(trainingValues, 1)
    }
}

fun Training1() {
    with(nn) {
        val trainingValues = listOf(
	
            NetworkData.create(listOf(0, 0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 0, 1), listOf(0)),
//            NetworkData.create(listOf(0, 0, 0, 1, 0), listOf(0)),
		
			// Astronomy Primary 
			NetworkData.create(astronomy0, listOf(1)),
			NetworkData.create(astronomy0, listOf(1)),
					
            NetworkData.create(listOf(0, 0, 0, 1, 1), listOf(0)),
	
			// Astronomy Secondary
//			NetworkData.create(astronomy1, listOf(1)),
            NetworkData.create(listOf(0, 0, 1, 0, 0), listOf(0)),
			NetworkData.create(listOf(0, 0, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 1), listOf(0)),		
//		    NetworkData.create(listOf(0, 1, 0, 0, 0), listOf(0)),
			
			// Piloting secondary
			NetworkData.create(piloting1, listOf(1)),
			
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
			
			// Piloting primary
//			NetworkData.create(piloting0, listOf(1)),
								
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
		
        train(trainingValues, 1)
    }
}

fun Training2() {
    with(nn) {
        val trainingValues = listOf(
	
            NetworkData.create(listOf(0, 0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 0, 1), listOf(0)),
//            NetworkData.create(listOf(0, 0, 0, 1, 0), listOf(0)),
		
			// Astronomy Primary 
			NetworkData.create(astronomy0, listOf(1)),
			NetworkData.create(astronomy0, listOf(1)),
			
            NetworkData.create(listOf(0, 0, 0, 1, 1), listOf(0)),
	
			// Astronomy Secondary
//			NetworkData.create(astronomy1, listOf(1)),
            NetworkData.create(listOf(0, 0, 1, 0, 0), listOf(0)),
			NetworkData.create(listOf(0, 0, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1, 1), listOf(0)),		
//		    NetworkData.create(listOf(0, 1, 0, 0, 0), listOf(0)),
			
			// Piloting secondary
			NetworkData.create(piloting1, listOf(1)),
			
			NetworkData.create(listOf(0, 1, 0, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 1, 0, 1, 0), listOf(0)),
			NetworkData.create(listOf(0, 1, 0, 1, 1), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 0, 1), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 1, 1), listOf(0)),
			
			NetworkData.create(listOf(1, 0, 0, 0, 0), listOf(0)),
			NetworkData.create(listOf(1, 0, 0, 0, 1), listOf(0)),
//            NetworkData.create(listOf(1, 0, 0, 1, 0), listOf(0)),
			
			// Piloting primary
			NetworkData.create(piloting0, listOf(1)),
//			NetworkData.create(piloting0, listOf(1)),
								
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
		
        train(trainingValues, 1)
    }
}

fun TrainingSet() {
    with(nn) {
		
        val trainingValues = ListOfData(dec)

        train(trainingValues, 1)
    }
}

fun ListOfData(dec : Int) : List<NetworkData> {

//	val data: List<NetworkData> = emptyList()
	val objects: List<NetworkData> = emptyList()
	var data  = objects.toMutableList()
	
//	val array = 0..31
//	array.drop(dec)

	for (i in 0..31) {
		var bin : String = Integer.toBinaryString(i)
		var size = bin.length
		var padding = "0"
		while (size < 5) {
			bin = padding + bin
			size = size + 1
		}
		val list0 = bin.toCollection(ArrayList()).toList() 
		val list1 = list0.map { it.toString().toInt() } 
			
		if (i == dec) {
			println("$dec in decimal -- > $bin in binary --> input vector : $list1 ")
			val d : NetworkData = NetworkData.create(list1, listOf(1))
			data.add(d)
		}
		else {
			val d : NetworkData = NetworkData.create(list1, listOf(0))
			data.add(d)
		}
	}

	return data
}


fun Testing() {
    with(nn) {

//		val r0 = (0..1).random()
//		val r1 = (0..1).random()
//		val r2 = (0..1).random()
//		val r3 = (0..1).random()
//		val r4 = (0..1).random()
					
        val testValues = listOf(
//			NetworkData.create(listOf(0, 0, 0, 0, 0), listOf(0)),
//            NetworkData.create(listOf(0, r1, r2, r3, r4), listOf(0)),
//            NetworkData.create(listOf(r2, 0, r3, r0, r1), listOf(0)),
//            NetworkData.create(listOf(r3, r4, 0, r1, r2), listOf(0)),
//			
//            NetworkData.create(listOf(r4, r3, r2, 0, 1), listOf(0)),
//            NetworkData.create(listOf(r4, r3, r2, 1, 0), listOf(0)),
//            NetworkData.create(listOf(r3, r2, r1, 0, 1), listOf(0)),
//            NetworkData.create(listOf(r3, r2, r1, 1, 0), listOf(0)),
			
			NetworkData.create(astronomy0, listOf(1)),			
            NetworkData.create(astronomy1, listOf(1)),
		
			NetworkData.create(piloting0, listOf(1)),
			NetworkData.create(piloting1, listOf(1))
        )
		
        test(testValues)
		println()
		// print input and output weights
        dump()
		println()
    }
}

fun TestSkill() {
	
	var cycle = 200
	
	for (i in 1..cycle) {
		log(1, "Training $i :")
		TrainingSet()
		Testing()
	}
	
//	for (i in 1..cycle) {
//		log(1, "Pilot Training 0-$i:")
//		Training0()
//		Testing()
//	}
//	
//	cycle += 50
//	 
//	for (i in 1..cycle) {
//		log(1, "Pilot Training 1-$i:")
//		Training1()
//		Testing()
//	}
//
//	cycle += 50
//	
//	for (i in 1..cycle) {
//		log(1, "Astro Training 0-$i:")
//		Training2()
//		Testing()
//	}
//	
//	cycle += 50
//	
//	for (i in 1..cycle) {
//		log(1, "Astro Training 1-$i:")
//		Training2()
//		Testing()
//	}
}

fun main(argv: Array<String>) {
    class Args {
        @Parameter(names = arrayOf("--log"), description = "Define the log level (1-3)")
        var log: Int = 2
    }

    val args = Args()
    JCommander(args).parse(*argv)
    LOG_LEVEL = args.log

//    conversion()
	
	TestSkill()
	
}

fun conversion() {
//	val t_list = 0..31
	val dec = 3
	var bin : String = Integer.toBinaryString(dec)
	var size = bin.length
	var padding = "0"
	while (size < 5) {
		bin = padding + bin
		size = size + 1
	}
	val list = bin.toCollection(ArrayList()).toList() // map { it.toInt() } //

	println("$dec in decimal -- > $bin in binary --> $list")
}
