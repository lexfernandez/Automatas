package automata

import java.io.Serializable

class Transition(var symbol: String, var source: State, var target: State): Serializable
