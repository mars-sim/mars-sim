/**
 * Mars Simulation Project
 * CoreMind.kt
 * @version 3.1.0 2019-08-22
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai;

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.random.Random
import com.beust.nnk.NeuralNetwork
import com.beust.nnk.NetworkData

class CoreMind {
	
	var LOG_LEVEL = 2
	
	var nn = NeuralNetwork(inputSize = 5, hiddenSize = 5, outputSize = 1)
	
	fun log(level: Int, s: String) {
	    if (LOG_LEVEL >= level) println(s)
	}
	
//	val dec = 4
//	var dec = 0
//	var inputVector : List<Int> = emptyList()
	var skillList : List<Any> = emptyList()
//	var skills : List<List<Int>> = skillList.toMutableList()	
	
	class Args {
        @Parameter(names = arrayOf("--log"), description = "Define the log level (1-3)")
        var log: Int = 2
    }

    val args = Args()
	
	fun create(careerString : String) {
		val dec = careerString.toInt()
//	    JCommander(args).parse(*argv)
    	LOG_LEVEL = args.log

	    testSkill(dec)
	}
	
	fun createTrainingSet(dec : Int) : List<Int>{
		
		val inputVector : List<Int> = createInputVector(dec) 
			
	    with(nn) {
	        val trainingValues = createNetworkData(dec)
	
	        train(trainingValues, 1)
	    }
		
		return inputVector
	}

	fun createInputVector(dec : Int) : List<Int> {
		var list1 : List<Int>
//		var list1 : List<Int> = inputList.toMutableList()

		var bin : String = Integer.toBinaryString(dec)
		var size = bin.length
		var padding = "0"
		while (size < 5) {
			bin = padding + bin
			size = size + 1
		}
		val list0 = bin.toCollection(ArrayList()).toList()
		list1 = list0.map { it.toString().toInt()}
				
		skillList += list1
					
		println("$dec in decimal -- > $bin in binary --> input vector : $list1 ")

		return list1
	}
	
	fun createNetworkData(dec : Int) : List<NetworkData> {
		var data: List<NetworkData> = emptyList()
//		var data  = objects.toMutableList()
		
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
//				println("$dec in decimal -- > $bin in binary --> input vector : $list1 ")
				val d : NetworkData = NetworkData.create(list1, listOf(1))
				data += d
			}
			else {
				val d : NetworkData = NetworkData.create(list1, listOf(0))
				data += d
			}
		}
		return data

	}
		
	
	fun Testing(inputVector : List<Int>) {
	    with(nn) {	
	        val testValues = listOf(
				NetworkData.create(inputVector, listOf(1))		
	        )
			
	        test(testValues)
			println()
			// print input and output weights
	        dump()
			println()
	    }
	}
	
	fun testSkill(dec : Int) {
		
		var cycle = 200
		
		for (i in 1..cycle) {
			log(1, "Training $i :")
			val inputVector : List<Int> = createTrainingSet(dec)
			Testing(inputVector)
		}
		
	}

}