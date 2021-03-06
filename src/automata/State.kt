package automata

import java.io.Serializable

class State(var value: String): Serializable {
    private var transitions: MutableList<Transition> = mutableListOf()
    private var transitionsPointingToMe: MutableList<Transition> = mutableListOf()

    fun addTransition(transition: Transition): Boolean {
        return transitions.add(transition)
    }

    fun getTransition(id: Char): Transition? {
        return transitions.find { it.symbol.first()==id }
    }
    fun getTransition(id: String): Transition? {
        return transitions.find { it.symbol==id }
    }

    fun getTransitions(): MutableList<Transition> {
        return transitions
    }

    fun getTransitionsPointingToMe(): MutableList<Transition> {
        return transitionsPointingToMe
    }

    fun getTransitions(id: Char): List<Transition> {
        return transitions.filter { it.symbol.first()==id }
    }

    fun  addTransitionPointingToMe(transition: Transition) : Boolean {
        return transitionsPointingToMe.add(transition)
    }

    fun  removeTransition(e: Transition):Boolean {
        //println("removing transition ${e.source.value} -> ${e.symbol} -> ${e.target.value}")
        //e.target.removeTransitionPointingToMe(e)
        return this.transitions.remove(e)
    }

    fun  removeTransitionPointingToMe(e: Transition):Boolean {
        //println("removing transition PM ${e.source.value} -> ${e.symbol} -> ${e.target.value}")
        return this.transitionsPointingToMe.remove(e)
    }

    var  index: Int = -1
    var  lowlink: Int = -1
}