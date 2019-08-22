package com.beust.nnk

//import java.util.*

open class Matrix(val rows: Int, val columns: Int, defaultValue: () -> Float = { -> 0.0f }) {
    val content = ArrayList<ArrayList<Float>>(rows)

    init {
        repeat(rows) { _ ->
            val nl = ArrayList<Float>()
            content.add(nl)
            repeat(columns) {
                nl.add(defaultValue())
            }
        }
    }

    fun Float.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

    operator fun get(i: Int) = content[i]

	    fun dump() : String {
        val result = StringBuilder()
        repeat(rows) { i ->
            repeat(columns) { j ->
					result.append(" ").append(content[i][j].format(2)).append(" ")
            }
            result.append("\n")
        }
        return result.toString()
    }
	
//    fun dump() : String {
//        val result = StringBuilder()
//        repeat(rows) { i ->
//            repeat(columns) { j -> {
//				
//					if (content[i][j] <0)
//						result.append(" ").append(content[i][j].format(2)).append(" ")
//                	else
//                		result.append(content[i][j].format(2)).append(" ")
//				}
//            }
//            result.append("\n")
//        }
//        return result.toString()
//    }

    override fun toString() = dump()
}
