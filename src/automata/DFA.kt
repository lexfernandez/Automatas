package automata

import java.io.Serializable
import java.util.*

/**
 * Created by Alex Fernandez on 07/25/2016.
 */

open class DFA(): IAutomata, Serializable {
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()


    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        var s = getState(source)
        var t = getState(target)
        var transition: Transition = Transition(symbol, s, t)
        if(s.getTransition(symbol)!=null)
            throw Exception("transition alredy exist")
        else
            return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        val result: State = deltaExtended(initial!!,alphabet)
        return finals.contains(result)
    }

    private fun deltaExtended(q: State, alphabet: String): State {
        if(alphabet.isEmpty()){
            return q
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        println("x: $x a:$a")
        return delta(deltaExtended(q,x),a)
    }

    private fun delta(q: State, symbol: Char): State {
        if (q.getTransition(symbol)!=null)
            return q.getTransition(symbol)!!.target
        else
            throw Exception("Transition dont exits")
    }
}

