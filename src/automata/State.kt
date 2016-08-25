package automata

import java.io.Serializable

class State(var value: String): Serializable {
    private var transitions: MutableList<Transition> = mutableListOf()
    private var transitionsPointingToMe: MutableList<Transition> = mutableListOf()

    fun addTransition(transition: Transition): Boolean {
        return transitions.add(transition)
    }

    fun getTransition(id: Char): Transition? {
        return transitions.find { it.symbol==id }
    }

    fun getTransitions(): MutableList<Transition> {
        return transitions
    }

    fun getTransitionsPointingToMe(): MutableList<Transition> {
        return transitionsPointingToMe
    }

    fun getTransitions(id: Char): List<Transition> {
        return transitions.filter { it.symbol==id }
    }

    fun  addTransitionPointingToMe(transition: Transition) : Boolean {
        return transitionsPointingToMe.add(transition)
    }
}