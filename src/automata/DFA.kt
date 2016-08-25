package automata

import java.io.Serializable

/**
 * Created by Alex Fernandez on 07/25/2016.
 */

open class DFA(): IAutomata, Serializable,Cloneable {


    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()


    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        if(symbol.equals('E'))
            throw Exception("Symbol E is not valid for a ${this.javaClass.simpleName}")
        var s = getState(source)
        var t = getState(target)
        var transition: Transition = Transition(symbol, s, t)
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
        var init = getInitialState()
        val result: State = deltaExtended(init,alphabet)
        return getFinalStates().contains(result)
    }

    private fun deltaExtended(q: State, alphabet: String): State {
        if(alphabet.isEmpty()){
            return q
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        //println("x: $x a:$a")
        return delta(deltaExtended(q,x),a)
    }

    private fun delta(q: State, symbol: Char): State {
        if (q.getTransition(symbol)!=null)
            return q.getTransition(symbol)!!.target
        else
            throw Exception("Transition dont exits")
    }



    override fun toDFA(): DFA {
        return this.clone()
    }

    override fun toRegex(): String {
        println(this)
        println(this.finals.map { it.value })
        var regex=""
        for(final in getFinalStates()){
            var clone = this.clone()
            println(clone)
            for(cfinal in clone.getFinalStates()){
                if(final.value!=cfinal.value)
                    clone.removeFinalState(cfinal.value)
            }

            for (state in clone.states){
                //si el estado no es inicial y no final
//                if(!clone.isFinal(state.value) && clone.getInitialState().value!=state.value ){
//                    //removerlo
//                    var transitionsPointingToMe = this.getState(state.value).getTransitionsPointingToMe()
//                    var transitionsIPointTo = this.getState(state.value).getTransitions()
//                    transitionsIPointTo.filter { it.target.value= }
//                    if(clone.removeState(state.value)){
//                        for(fromT in transitionsPointingToMe){
//                            for (toT in transitionsIPointTo){
//                                //fromT.
//                            }
//                        }
//                    }
//                }

            }
            println(clone.initial?.value)
            println(clone.finals.map { it.value })
        }

        return regex
    }

    override fun clone(): DFA {
        var clone = DFA()
        for(state in this.states){
            clone.addState(State(state.value))
        }

        for(state in this.states){
            for(transition in state.getTransitions()){
                clone.addTransition(transition.symbol,transition.source.value,transition.target.value)
            }
        }

        clone.setInitialState(this.getInitialState().value)

        for(state in this.getFinalStates()){
            clone.setFinalState(state.value)
        }

        return clone
    }

    override fun toMinimizedDFA(): DFA {
        hopcroftMinimization(this)
        return this
    }

    private fun hopcroftMinimization(dfa: DFA): MutableList<MutableList<State>> {
        println(this.states.map { it.value+"*" })
        var partitions: MutableList<MutableList<State>> = mutableListOf()
        var L: Queue<MutableList<State>> = Queue()

        var F = dfa.getFinalStates().toMutableList()
        var FdifQ = dfa.states.subtract(dfa.getFinalStates()).toMutableList()

        var C0:MutableList<State>
        var C1:MutableList<State>
        if(F.count()<FdifQ.count()){
            C0 = FdifQ
            C1 = F
            L.enqueue(C1)
        }else{
            C1 = FdifQ
            C0 = F
            L.enqueue(C1)
        }

        partitions.add(C0)
        partitions.add(C1)

        while (L.isNotEmpty()){
            var S = L.dequeue().orEmpty().toMutableList()
            for (a in dfa.language) {
                var P: MutableList<MutableList<State>> = mutableListOf()
                P = P.union(partitions).toMutableList()
                var iterate= P.listIterator()
                while (iterate.hasNext()) {
                    var B = iterate.next()
                    var tuple = split(B,S,a)
                    if(tuple.first.isNotEmpty() and tuple.second.isNotEmpty()){
                        partitions.remove(B)
                        partitions.add(tuple.first)
                        partitions.add(tuple.second)
                        if(L.items.contains(B)){
                            L.items.remove(B)
                            L.enqueue(tuple.first)
                            L.enqueue(tuple.second)
                        }else{
                            if(tuple.first.count()<tuple.second.count())
                                L.enqueue(tuple.first)
                            else
                                L.enqueue(tuple.second)
                        }
                    }
                    iterate.remove()
                }
            }
        }

        println(L.items.map { it.map { it.value+"*" } })
        println(partitions.map { it.map { it.value+"*" } })
        return partitions
    }

    private fun split(B:MutableList<State>,S:MutableList<State>,a:Char):Pair<MutableList<State>,MutableList<State>> {
        var G1:MutableList<State> = B.intersect(S.map { it.getTransitions(a).map { it.target } }.flatten()).distinct().toMutableList()
        var G2:MutableList<State> = B.subtract(G1).distinct().toMutableList()
        return Pair(G1,G2)
    }
}

