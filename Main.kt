package minesweeper
import kotlin.random.Random

enum class Marks(val mark: Char) {
    MINE('X'),
    MARKED('*'),
    UNMARKED('.'),
    EXPLORED('/')
}

class MineSweeper(private val rows: Int = 9, private val cols: Int = 9) {
    
    private val playingField: Array<CharArray> = Array(rows) { CharArray(cols) { Marks.UNMARKED.mark } }
    private val hiddenPlayingField: Array<CharArray> = Array(rows) { CharArray(cols) { Marks.UNMARKED.mark} }
    private val mineOnField = 'X'
    private val minePrompt = "How many mines do you want on the field?"
    private var youAreDead = false

    fun run() {
        this.promptForMines()
        this.printTheField()

        do {
            val corInput = promptForInput()
            if (corInput) printTheField()

            if (youAreDead) {
                printTheField(revealField = true)
                break
            }

        } while (!checkIfAllMarked())

        if (youAreDead) {
            println("You stepped on a mine and failed!")
        } else {
            println("Congratulations! You found all the mines!")
        }
    }

    /**
     *  function to copy playingField and only hide mines - not used in final program
     */
    private fun fillHiddenField() {
        for (r in playingField.indices) {
            for (c in playingField[r].indices) {
                if (playingField[r][c] == Marks.MINE.mark) {
                    hiddenPlayingField[r][c] = Marks.UNMARKED.mark
                } else {
                    hiddenPlayingField[r][c] = playingField[r][c]
                }
            }
        }
    }
    
    /**
        Print the playingField
    */
    private fun printTheField(revealField: Boolean = false) {
        if (revealField) {
            printField(playingField)
        } else printField(hiddenPlayingField)
    }

    /**
     *  get input from user how many mines to have on the field
     * */
    private fun promptForMines() {
        // take input from player
        println(minePrompt)
        val mines = readLine()!!.toInt()
        this.fillFieldWithMines(mines)
        // this.fillHiddenField() <-- used in earlier build
    }

    /**
     *  get player input in the game
     * */
    private fun promptForInput(): Boolean {

        println("Set/delete mine marks or claim a cell as free:")

        val coordinates = readLine()!!.split(" ")

        // get the coordinates minus 1 (array starts at 0)
        val x = coordinates[0].toInt() - 1 //xCo - 1
        val y = coordinates[1].toInt() - 1 //yCo - 1

        // get what type of input
        return if (coordinates[2] == "mine") {
            markCell(x, y)
        } else exploreCell(x, y)
    }

    /**
     *  Mark a cell
     * */
    private fun markCell(x: Int, y: Int): Boolean {
        val notValid = fieldIsNumber(x, y, hiddenField = true)
        if (notValid) {
            println("There is a number here!")
            return false
        }

        if (hiddenPlayingField[y][x] == Marks.UNMARKED.mark) {
            hiddenPlayingField[y][x] = Marks.MARKED.mark
        } else {
            hiddenPlayingField[y][x] = Marks.UNMARKED.mark
        }
        return true
    }

    /**
     *  Explore a cell
     * */
    private fun exploreCell(x: Int, y: Int): Boolean {
        if (playingField[y][x] == Marks.MINE.mark) {
            youAreDead = true
            return false
        }
        else {
            revealField(x, y)
        }
        return true
    }

    /**
     *  reveals a cell. if number it stops, else checks neighbours
     * */
    private fun revealField(x: Int, y: Int) {
        if (fieldIsNumber(x, y)) {
            hiddenPlayingField[y][x] = playingField[y][x]
        } else if (hiddenPlayingField[y][x] != Marks.EXPLORED.mark) {
            hiddenPlayingField[y][x] = Marks.EXPLORED.mark
            playingField[y][x] = Marks.EXPLORED.mark
            // check cells around
            when {
                // top row
                y == 0 ->
                    // left corner
                    when (x) {
                        0 -> {
                            revealField(x + 1, y)
                            revealField(x + 1, y + 1)
                            revealField(x, y + 1)
                        }
                        cols - 1 -> { // right corner
                            revealField(x - 1, y)
                            revealField(x - 1, y + 1)
                            revealField(x, y + 1)
                        }
                        else -> {  // rest of top row
                            revealField(x - 1, y)
                            revealField(x - 1, y + 1)
                            revealField(x, y + 1)
                            revealField(x + 1, y + 1)
                            revealField(x + 1, y)
                        }
                    }
                // bottom row
                y == rows - 1 ->
                    // left corner
                    when (x) {
                        0 -> {
                            revealField(x, y - 1)
                            revealField(x + 1, y - 1)
                            revealField(x + 1, y)
                        }
                        cols - 1 -> {  // right corner
                            revealField(x - 1, y)
                            revealField(x - 1, y - 1)
                            revealField(x, y - 1)
                        }
                        else -> {  // rest of bottom row
                            revealField(x - 1, y)
                            revealField(x - 1, y - 1)
                            revealField(x, y - 1)
                            revealField(x + 1, y - 1)
                            revealField(x + 1, y)
                        }
                    }
                // left side
                x == 0 -> {
                    revealField(x, y - 1)
                    revealField(x + 1, y - 1)
                    revealField(x + 1, y)
                    revealField(x + 1, y + 1)
                    revealField(x, y + 1)
                }
                // right side
                x == cols - 1 -> {
                    revealField(x, y - 1)
                    revealField(x - 1, y - 1)
                    revealField(x - 1, y)
                    revealField(x - 1, y + 1)
                    revealField(x, y + 1)
                }
                else -> {
                    revealField(x, y - 1)
                    revealField(x - 1, y - 1)
                    revealField(x - 1, y)
                    revealField(x - 1, y + 1)
                    revealField(x, y + 1)
                    revealField(x + 1, y + 1)
                    revealField(x + 1, y)
                    revealField(x + 1, y - 1)
                }
            }
        }

    }

    /**
     * Check if the game is won - True if done, otherwise false
     * */
    private fun checkIfAllMarked(): Boolean {

        var isDone = false
        for (r in playingField.indices) {
            for (c in playingField[r].indices) {
                val pValue = playingField[r][c]
                val hValue = hiddenPlayingField[r][c]

                if (pValue != hValue) {
                    isDone = pValue == Marks.MINE.mark && hValue == Marks.MARKED.mark
                    if (!isDone) return false
                }
            }
        }
        return isDone
    }

    /**
     *  Checks if the player wants to mark a number
     * */
    private fun fieldIsNumber(x: Int, y: Int, hiddenField: Boolean = false): Boolean {
        val numbers = "12345678".toCharArray()
        val fieldContent = if (hiddenField) hiddenPlayingField[y][x] else playingField[y][x]
        return fieldContent in numbers
    }

    /**
        update the playingField with hints about how many mines around the empty cell
    */
    private fun updateWithNumbers() {
        
        for (r in playingField.indices) {
            for (c in playingField[r].indices) {
                
                if (playingField[r][c] != mineOnField) {
                    var minesAround = 0
                    
                    if (r == 0) {
                        if (c == 0) {
                            if (playingField[r][c + 1] == mineOnField) minesAround++
                            if (playingField[r + 1][c + 1] == mineOnField) minesAround++
                            if (playingField[r + 1][c] == mineOnField) minesAround++
                                          
                        } else if (c == cols - 1) {
                            if (playingField[r][c - 1] == mineOnField) minesAround++
                            if (playingField[r + 1][c - 1] == mineOnField) minesAround++
                            if (playingField[r + 1][c] == mineOnField) minesAround++
                            
                        } else {
                            if (playingField[r][c + 1] == mineOnField) minesAround++
                            if (playingField[r + 1][c + 1] == mineOnField) minesAround++
                            if (playingField[r + 1][c] == mineOnField) minesAround++
                            if (playingField[r + 1][c - 1] == mineOnField) minesAround++
                            if (playingField[r][c - 1] == mineOnField) minesAround++
                            
                        }
                    } else if (r == rows - 1) {
                        if (c == 0) {
                                if (playingField[r][c + 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c + 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c] == mineOnField) minesAround++
                                          
                        } else if (c == cols - 1) {
                                if (playingField[r][c - 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c - 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c] == mineOnField) minesAround++
                            
                        } else {
                                if (playingField[r][c + 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c + 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c] == mineOnField) minesAround++
                                if (playingField[r - 1][c - 1] == mineOnField) minesAround++
                                if (playingField[r][c - 1] == mineOnField) minesAround++
                            
                        }
                    } else {
                        if (c == 0) {
                                if (playingField[r][c + 1] == mineOnField) minesAround++
                                if (playingField[r + 1][c + 1] == mineOnField) minesAround++
                                if (playingField[r + 1][c] == mineOnField) minesAround++
                                if (playingField[r - 1][c] == mineOnField) minesAround++
                                if (playingField[r - 1][c + 1] == mineOnField) minesAround++
                                
                        } else if (c == cols - 1) {
                                if (playingField[r - 1][c] == mineOnField) minesAround++
                                if (playingField[r - 1][c - 1] == mineOnField) minesAround++
                                if (playingField[r][c - 1] == mineOnField) minesAround++
                                if (playingField[r + 1][c - 1] == mineOnField) minesAround++
                                if (playingField[r + 1][c] == mineOnField) minesAround++
                            
                        } else {
                                if (playingField[r - 1][c - 1] == mineOnField) minesAround++
                                if (playingField[r][c - 1] == mineOnField) minesAround++
                                if (playingField[r + 1][c - 1] == mineOnField) minesAround++
                                if (playingField[r - 1][c] == mineOnField) minesAround++
                                if (playingField[r + 1][c] == mineOnField) minesAround++
                                if (playingField[r - 1][c + 1] == mineOnField) minesAround++
                                if (playingField[r][c + 1] == mineOnField) minesAround++
                                if (playingField[r + 1][c + 1] == mineOnField) minesAround++
                            
                        }
                    }
                    
                    if (minesAround != 0) {
                        playingField[r][c] = when(minesAround) {
                            1 -> '1'
                            2 -> '2'
                            3 -> '3'
                            4 -> '4'
                            5 -> '5'
                            6 -> '6'
                            7 -> '7'
                            else -> '8'
                        }
                    }
                }
                
            }
        }
        
    }
    
    /**
        Fill the playingField with desired number of mines
    */
    private fun fillFieldWithMines(numberOfMines: Int) {
        if (numberOfMines <= cols * rows) {
            
            repeat (numberOfMines) {
                var occupied = true
            
                while (occupied) {
                    val rowPlacement = Random.nextInt(playingField.size )
                    val colPlacement = Random.nextInt(playingField[0].size)
                    if (playingField[rowPlacement][colPlacement] != mineOnField) {
                        playingField[rowPlacement][colPlacement] = mineOnField
                        occupied = false
                    }
                }
            }
            updateWithNumbers()
        }
    }
}

/**
    general function for printing an Array<CharArray>
*/
fun printField(a: Array<CharArray>) {
    println(" |123456789|")
    println("-|---------|")
    for (ar in a.indices) {
        print(ar + 1)
        print("|")
        for (cell in a[ar]) {
            print(cell)
        }
        print("|")
        println()
    }
    println("-|---------|")
}



fun main() {
    val game = MineSweeper()
    game.run()
}
