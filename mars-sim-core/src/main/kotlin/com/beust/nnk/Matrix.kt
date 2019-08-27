package com.beust.nnk

//import java.util.*
import java.io.Serializable

open class Matrix(val rows: Int, val columns: Int, defaultValue: () -> Float = { -> 0.0f }) : Serializable {
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

//    fun Float.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

	fun Float.format(digits: Int) : String {
		if (digits < 0)
			return java.lang.String.format("%.${digits}f", this)
		else
			return java.lang.String.format("%1${digits}f", this)
    }
	
    operator fun get(i: Int) = content[i]

	    fun dump() : String {
        val result = StringBuilder()
        repeat(rows) { i ->
            repeat(columns) { j ->
					result.append(content[i][j].format(2)) //.append(" ")
            }
            result.append("\n")
        }
        return result.toString()
    }

    override fun toString() = dump()
}
