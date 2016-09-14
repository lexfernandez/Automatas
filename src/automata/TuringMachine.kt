package automata

import java.io.Serializable

/**
 * Created by lex on 09-13-16.
 */

class TuringMachine(): IAutomata, Serializable,Cloneable {
    override fun toRegex(): DFA {
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

    fun addTransition( symbol: Char, source: String, target: String, replacement: Char, direction: TuringMachineDirection): Boolean {
        val s = getState(source)
        val f = getState(target)
        val ts = s.getTransitions(symbol)
        for (t in ts){
            if(t.source.value==source && t.target.value==target && t.replacement==replacement && t.direction==direction){
                throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
            }
        }

        val transition: Transition = Transition(symbol.toString(), s, f,replacement,direction)
        addLanguageSymbol(symbol)
        f.addTransitionPointingToMe(transition)
        return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        if(this.states.count()==0) return false
        println("Turing Machine Evaluation")

        var ribbon = "B"+alphabet+"B"
        var _ribbon = ribbon.toMutableList()

        var currentState = getInitialState()

        var index = 1

        if (currentState != null) {
            while (!isFinal(currentState!!.value)) {
                if (currentState != null) {
                    var transitionsSize = currentState.getTransitions().size
                    var transitionCount = 0
                    for (transition in currentState!!.getTransitions()) {

                        if (transition.symbol==_ribbon[index].toString()) {
                            if (transition.direction==TuringMachineDirection.Left) {
                                index -= 1
                                if (index >= 0) {
                                    _ribbon[index+1] = transition.replacement
                                    currentState = transition.target
                                    break
                                } else {
                                    return false
                                }
                            } else if (transition.direction==TuringMachineDirection.Right) {
                                index += 1

                                if (index <= _ribbon.size - 1) {
                                    _ribbon[index-1] = transition.replacement
                                    currentState = transition.target
                                    break
                                } else {
                                    return false
                                }
                            }
                        }
                        transitionCount++
                    }
                    if (transitionCount == transitionsSize) {
                        return false
                    }
                }
            }
        }
        println(_ribbon.toString())
        return true
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