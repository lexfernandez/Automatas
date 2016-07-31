package automata

import java.io.Serializable

class Transition(val symbol: Char, var source: State, var target: State): Serializable