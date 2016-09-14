package automata

import java.io.Serializable

class Transition(var symbol: String, var source: State, var target: State): Serializable{
    var top: Char = '\u0000'

    var toPush: List<Char> = listOf()
    var  replacement: Char = 'B'
    var  direction: TuringMachineDirection=TuringMachineDirection.Right

    constructor(symbol: String, source: State, target: State, top:Char, toPush:List<Char>) : this(symbol,source,target) {
        this.top=top
        this.toPush=toPush
    }




    constructor(symbol: String, source: State, target: State, replacement: Char, direction: TuringMachineDirection=TuringMachineDirection.Right) : this(symbol,source,target) {
        this.replacement=replacement
        this.direction=direction
    }
}

