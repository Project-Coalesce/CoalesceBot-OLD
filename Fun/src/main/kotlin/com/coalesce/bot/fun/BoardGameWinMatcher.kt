package com.coalesce.bot.`fun`

import com.coalesce.bot.utilities.listOf

fun winDetection(board: List<List<String?>>, rows: Int, columns: Int, size: Int): String? {
    val count = size - 1
    fun attemptDetect(line: List<String?>): String? {
        var current = "" to 0
        line.forEach {
            if (current.first == it) {
                current = current.first to current.second + 1
                if (current.second >= count) return current.first
            } else current = (it ?: "") to 0
        }
        return null
    }

    listOf<List<String?>> {
        addAll(board) // Horizontal

        val verticalLines = mutableListOf<MutableList<String?>>()
        board.forEach {
            it.forEachIndexed { index, s ->
                if (index >= verticalLines.size) verticalLines.add(mutableListOf())
                verticalLines[index].add(s)
            }
        }
        addAll(verticalLines)

        // Top-left to bottom-right
        for (rowStr in 0..rows - count) {
            add(listOf {
                var col = 0; var row = rowStr
                while (row <= rows && col <= columns) {
                    add(board[col][row])
                    row ++; col ++
                }
            })
        }
        if (columns > count)
            for (colStr in 1..columns - count) {
                add(listOf {
                    var col = colStr; var row = 0
                    while (row <= rows && col <= columns) {
                        add(board[col][row])
                        row ++; col ++
                    }
                })
            }
        // Top-right to bottom-left
        for (rowStr in count..rows) {
            add(listOf {
                var col = 0; var row = rowStr
                while (row >= 0 && col <= columns) {
                    add(board[col][row])
                    row --; col ++
                }
            })
        }
        if (columns > count)
            for (colStr in 1..columns - count) {
                add(listOf {
                    var col = colStr; var row = 0
                    while (row <= rows && col >= 0) {
                        add(board[col][row])
                        row ++; col --
                    }
                })
            }
    }.forEach {
        val detectResult = attemptDetect(it)
        if (detectResult != null) return detectResult
    }
    return null
}