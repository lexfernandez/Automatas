package automata

import java.io.Serializable

class Transition(val symbol: Char, var source: String, var target: String): Serializable