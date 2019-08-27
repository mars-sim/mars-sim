/**
 * Mars Simulation Project
 * CoreMind.kt
 * @version 3.1.0 2019-08-22
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mind;

import java.io.Serializable
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.random.Random
import com.beust.nnk.NeuralNetwork
import com.beust.nnk.NetworkData
import org.mars_sim.msp.core.person.ai.SkillType

public class CoreMind : Serializable {
	
	var LOG_LEVEL = 2
	
	var INPUT_SIZE = 5
	
	var nn = NeuralNetwork(inputSize = INPUT_SIZE, hiddenSize = INPUT_SIZE, outputSize = 1)
	
	var data: List<NetworkData> = setupNetworkData()
	
	var idList : List<Int> = emptyList()
		
	fun setupNetworkData() : List<NetworkData> {
		var data: List<NetworkData> = emptyList()
		
		for (i in 0..31) {
			var bin : String = Integer.toBinaryString(i)
			var size = bin.length
			var padding = "0"
			while (size < INPUT_SIZE) {
				bin = padding + bin
				size = size + 1
			}
			val list0 = bin.toCollection(ArrayList()).toList() 
			val list1 = list0.map { it.toString().toInt() } 
				
			val d : NetworkData = NetworkData.create(list1, listOf(0))
			data += d

		}
		return data

	}
	
	fun log(level: Int, s: String) {
	    if (LOG_LEVEL >= level) print(s)
	}
	

	class Args : Serializable {
        @Parameter(names = arrayOf("--log"), description = "Define the log level (1-3)")
        var log: Int = 2
    }

    val args = Args()
	
	
	fun create(careerString : String) {
		val dec = careerString.toInt()
//	    JCommander(args).parse(*argv)
    	LOG_LEVEL = args.log

		if (!idList.contains(dec))
			// Add the new skill to the idList
			idList += dec
		
	    repeatTraining(dec)
	}
	
	fun createTrainingSet(){
			
	    with(nn) {
	        val trainingValues = createNetworkData()
	
	        train(trainingValues, 1)
	    }
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
					
//		val skill : String = SkillType.lookup(dec).getName()
//		println("Skill : $skill id $dec (in decimal) -- > $bin (in binary) --> input vector : $list1 ")

		return list1
	}
	
	fun createNetworkData() : List<NetworkData> {
		var newList: List<NetworkData> = emptyList()
		
		for (i in 0..31) {
			if (idList.contains(i)) {
				val list = createInputVector(i)
				val d : NetworkData = NetworkData.create(list, listOf(1))
				newList += d
			}
			else {
				newList += data.get(i)
			}
		}
		
		return newList
	}
		
	
	fun Testing(inputVector : List<Int>) {
	    with(nn) {	
	        val testValues = listOf(
				NetworkData.create(inputVector, listOf(1))		
	        )
			
	        test(testValues)
//			println()
			// print input and output weights
//	        dump()
//			println()
	    }
	}
	
	fun repeatTraining(dec : Int) {
		
		var cycle = 50
		
//		val skill : String = SkillType.lookup(dec).getName()
		
//		println("--- $skill (id $dec) skill ---")
		
		for (i in 1..cycle) {
//			log(1, "Training $i :" )
			val inputVector : List<Int> = createInputVector(dec) 
			createTrainingSet()
			Testing(inputVector)
		}
		
	}

}