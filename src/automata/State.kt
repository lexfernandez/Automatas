package automata

import java.io.Serializable
import java.util.*

class State(val value: String): Serializable {
    private var transitions: MutableList<Transition> = mutableListOf()

    fun addTransition(transition: Transition): Boolean {
        return transitions.add(transition)
    }

    fun getTransition(id: Char): Transition? {
        return transitions.find { it.symbol==id }
    }

    fun getTransitions(): MutableList<Transition> {
        return transitions
    }

    fun getTransitions(id: Char): List<Transition> {
        return transitions.filter { it.symbol==id }
    }
}