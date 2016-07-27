package automata

import java.util.*

/**
 * Created by lex on 07-26-16.
 */

interface Automata {
    var states: HashMap<String, State>
    var initial: String
    var finals: MutableList<String>

    fun addState(state: State): State? {
        if(states[state.value]!=null)
            throw Exception("El estado ya existe")
        return states.put(state.value,state)
    }

    fun getState(value: String): State? {
        return states[value]?: null
    }

    fun printStates(){
        for ((key,state) in states){
            println("state: ${state.value}")
            for((key,transition) in state.getTransitions()){
                println("\ttransition: ${transition.source} -> ${transition.symbol} -> ${transition.target}")
            }
        }
        println("initial state: $initial")
        for (f in finals){
            println("final state: $f")
        }
    }

    fun setInitialState(i: String) {
        initial = i
    }

    fun getInitialState(): String {
        return initial
    }

    fun setFinalState(i: String) {
        if(!finals.contains(i))
            finals.add(i)
    }

    fun getFinalStates(): MutableList<String> {
        return finals
    }

    fun addTransition( symbol: Char,source: String, target: String): Transition?
    fun evaluate(alphabet: String): Boolean
}