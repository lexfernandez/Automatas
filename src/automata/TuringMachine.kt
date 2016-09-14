package automata

import java.io.Serializable

/**
 * Created by lex on 09-13-16.
 */

class TuringMachine(): IAutomata, Serializable,Cloneable {
    override fun toRegex(): NFAE {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toMinimizedDFA(): DFA {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toDFA(): DFA {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()

    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        if(symbol.equals('E'))
            throw Exception("Symbol E is not valid for a ${this.javaClass.simpleName}")
        val s = getState(source)
        val t = getState(target)
        val transition: Transition = Transition(symbol.toString(), s, t)
        if(s.getTransition(symbol)!=null)
            throw Exception("Transition from ${s.value} with symbol $symbol already exist!")
        else{
            addLanguageSymbol(symbol)
            t.addTransitionPointingToMe(transition)
            return s.addTransition(transition)
        }
    }

    override fun evaluate(alphabet: String): Boolean {
        if(this.states.count()==0) return false
        println("DFA Evaluation")
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        val result: State = deltaExtended(init,alphabet)
        return getFinalStates().contains(result)
    }

    private fun deltaExtended(q: State, alphabet: String): State {
        if(alphabet.isEmpty()){
            return q
        }

        val a = alphabet.last()
        val x = alphabet.subSequence(0,alphabet.length-1).toString()

        //println("x: $x a:$a")
        return delta(deltaExtended(q,x),a)
    }

    private fun delta(q: State, symbol: Char): State {
        if (q.getTransition(symbol)!=null)
            return q.getTransition(symbol)!!.target
        else
            throw Exception("Transition dont exits")
    }
}