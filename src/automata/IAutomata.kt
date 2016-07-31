package automata

import java.util.*

/**
 * Created by lex on 07-26-16.
 */

interface IAutomata {
    var states: MutableList<State>
    var initial: State?
    var finals: MutableList<State>

    fun addState(state: State): Boolean {
        if(states.find { it.value==state.value }!=null)
            throw Exception("El estado ya existe")
        return states.add(state)
    }

    fun getState(value: String): State {
        return states.find { it.value==value }?: throw Exception("State $value doesn't exist!")
    }

    fun printStates(){
        for (state in states){
            println("state: ${state.value}")
            for(transition in state.getTransitions()){
                println("\ttransition: ${transition.source.value} -> ${transition.symbol} -> ${transition.target.value}")
            }
        }
        println("initial state: ${initial!!.value}")
        for (final in finals){
            println("final state: ${final.value}")
        }
    }

    fun setInitialState(value: String?) {
        initial = states.find { it.value==value }
    }

    fun getInitialState(): State? {
        return initial
    }

    fun setFinalState(value: String):Boolean {
        if(finals.find { it.value == value }!=null)
            return true
        else
            return finals.add(getState(value))
    }

    fun unsetFinalState(value: String): Boolean {
        var state = finals.find { it.value == value }
        if(state!=null)
            return finals.remove(state)
        return false
    }

    fun isFinal(value: String): Boolean{
        try {
            var state= getState(value)
            return finals.contains(state)
        }catch(e: Exception){
            return false
        }
    }

    fun getFinalStates(): MutableList<State> {
        return finals
    }

    fun removeState(value: String): Boolean {
        var state = states.find { it.value==value }
        if(state!= null)
            return states.remove(state)
        return false
    }



    fun addTransition( symbol: Char,source: String, target: String): Boolean
    fun evaluate(alphabet: String): Boolean
}