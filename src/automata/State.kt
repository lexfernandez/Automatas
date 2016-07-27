package automata

import java.util.*

class State(val value: String) {
    private var transitions: HashMap<Char, Transition> = HashMap()

    fun addTransition(transition: Transition): Transition? {
        return transitions.put(transition.symbol,transition)
    }

    fun getTransition(id: Char): Transition? {
        return transitions[id]?: null
    }

    fun getTransitions(): HashMap<Char, Transition> {
        return transitions
    }
}