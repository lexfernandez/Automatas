package automata

import java.util.*

/**
 * Created by Alex Fernandez on 07/25/2016.
 */

class DFA(){
    private var states: HashMap<Int,State> = HashMap()
    private var initial: Int = 0
    private var finals: MutableList<Int> = mutableListOf()

    fun addState(state: State): State? {
        return states.put(state.id,state)
    }

    fun getState(id: Int): State? {
        return states[id]?: null
    }

    fun getStates() : HashMap<Int,State> {
        return states
    }

    fun printStates(){
        for ((key,state) in states){
            println("state: ${state.id}")
            for((key,transition) in state.getTransitions()){
                println("\ttransition: ${transition.source} -> ${transition.symbol} -> ${transition.target}")
            }
        }
        println("initial state: $initial")
        for (f in finals){
            println("final state: $f")
        }
    }

    fun addTransition( symbol: Char,source: Int, target: Int): Transition? {
        var s = getState(source) ?: throw IllegalArgumentException("source not exist")
        getState(target) ?: throw IllegalArgumentException("target not exist")
        var transition: Transition = Transition(symbol,source,target)
        if(s.getTransition(symbol)!=null)
            throw Exception("transition alredy exist")
        else
            return s.addTransition(transition)
    }

    fun setInitialState(i: Int) {
        initial = i
    }

    fun getInitialState(): Int {
        return initial
    }

    fun setFinalState(i: Int) {
        if(!finals.contains(i))
            finals.add(i)
    }

    fun getFinalStates(): MutableList<Int> {
        return finals
    }

    fun evaluate(alphabet: String): Boolean {
        println(initial)
        println(alphabet)
        printStates()
        val result: State = deltaExtended(states[initial]!!,alphabet)
        return finals.contains(result.id)
    }

    private fun deltaExtended(q: State,alphabet: String): State {
        if(alphabet.isEmpty()){
            return q
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        println("x: $x a:$a")
        return delta(deltaExtended(q,x),a)
    }

    private fun delta(q: State,symbol: Char): State {
        if (q.getTransition(symbol)!=null)
            return getState(q.getTransition(symbol)!!.target)!!
        else
            throw Exception("Transition dont exits")
    }
}

class State(val id: Int, val value: String) {
    private var transitions: HashMap<Char,Transition> = HashMap()

    fun addTransition(transition: Transition): Transition? {
        return transitions.put(transition.symbol,transition)
    }

    fun getTransition(id: Char): Transition? {
        return transitions[id]?: null
    }

    fun getTransitions(): HashMap<Char,Transition>{
        return transitions
    }
}

class Transition(val symbol: Char, var source: Int, var target: Int)