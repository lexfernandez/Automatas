package automata

import java.io.Serializable

class Transition(var symbol: String, var source: State, var target: State): Serializable{
    var top: Char = '\u0000'

    var toPush: List<Char> = listOf()

    constructor(symbol: String, source: State, target: State, top:Char, toPush:List<Char>) : this(symbol,source,target) {
        this.top=top
        this.toPush=toPush
    }

    constructor(symbol: String, source: State, target: State, replacement: Char, direction: TuringMachineDirection ) : this(symbol,source,target) {
        this.top=top
        this.toPush=toPush
    }
}

enum class TuringMachineDirection(c: Char='>') {
    Left('<'),
    Rgit igth('>'),
}
