package automata

import automata.AutomataOperation.*
import java.io.Serializable

/**
 * Created by Alex Fernandez on 07/25/2016.
 */

open class DFA(): IAutomata, Serializable,Cloneable {

    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()

    var index=0
    var S:MutableList<State> = mutableListOf()

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

    override fun toDFA(): DFA {
        return this
    }

    override fun toRegex(): NFAE {
        var nfae=NFAE()
        for (state in states){
            nfae.addState(State(state.value))
        }

        for (state in states){
            for (transition in state.getTransitions()){
                nfae.addTransition(transition.symbol.first(),transition.source.value,transition.target.value)
            }
        }

        var newInitial = State("Initial")
        nfae.addState(newInitial)
        nfae.setInitialState(newInitial.value)
        nfae.addTransition('E',newInitial.value,getInitialState()!!.value)

        var newFinal = State("Final")
        nfae.addState(newFinal)
        nfae.setFinalState(newFinal.value)

        for(final in finals){
            nfae.addTransition('E',final.value,newFinal.value)
        }


        while (nfae.states.count()>2){
            //Get first state to remove
            var st=nfae.states.filter { !(nfae.isFinal(it.value)) && (nfae.getInitialState()?.value!=it.value) }.sortedBy{ it.getTransitionsPointingToMe().count() }
            println("${st.map { it.value + " - " + it.getTransitionsPointingToMe().count() }}")
            var state = st.first()

            //Get transitions pointing to self state
            var edgesToUnified = state.getTransitions().groupBy { it.target }.filter { it.value.count()>1 }

            for (group in edgesToUnified){

                var edge = group.value.first()
                var toRemove = group.value.minus(edge)
                var newTransitionSymbol= if(edge.symbol.equals("E")) "" else edge.symbol
                for (e in toRemove){
                    newTransitionSymbol += if(e.symbol.equals("E")) "" else (if(e.symbol.count()>1) "+"+e.symbol else e.symbol)
                    state.removeTransition(e)
                }
                state.removeTransition(edge)
                if(newTransitionSymbol.isNotEmpty()){
                    if(newTransitionSymbol.count()>1 && !newTransitionSymbol.startsWith("("))
                        newTransitionSymbol = "($newTransitionSymbol)"
                    var nt=Transition(newTransitionSymbol,edge.source,edge.target)
                    edge.source.addTransition(nt)
                    edge.target.addTransitionPointingToMe(nt)
                }
            }

            //get multiple transitions pointing from one state to another state
            edgesToUnified = state.getTransitionsPointingToMe().groupBy { it.source }.filter { it.value.count()>1 }
            for (group in edgesToUnified){
                var edge = group.value.first()
                var toRemove = group.value.minus(edge)
                var newTransitionSymbol= if(edge.symbol.equals("E")) "" else edge.symbol
                for (e in toRemove){
                    newTransitionSymbol += if(e.symbol.equals("E")) "" else (if(e.symbol.count()>1) "+"+e.symbol else e.symbol)
                    state.removeTransitionPointingToMe(e)
                }
                state.removeTransitionPointingToMe(edge)
                if(newTransitionSymbol.isNotEmpty()){
                    if(newTransitionSymbol.count()>1 && !newTransitionSymbol.startsWith("("))
                        newTransitionSymbol = "($newTransitionSymbol)"
                    var nt=Transition(newTransitionSymbol,edge.source,edge.target)
                    edge.source.addTransition(nt)
                    edge.target.addTransitionPointingToMe(nt)
                }
            }

            //create new edges and remove old ones
            if(!(nfae.isFinal(state.value)) && (nfae.getInitialState()?.value!=state.value)){
                var transition =  state.getTransitions().filter { it.source==it.target }.firstOrNull()
                var star=""
                if(transition!=null){
                    star = if (transition.symbol.count()>1 && !transition.symbol.startsWith("(")) "(${transition.symbol})*" else "${transition.symbol}*"
                    transition.target.removeTransitionPointingToMe(transition)
                    state.removeTransition(transition)
                }

                var transitionsIGointTo = state.getTransitions()
                //println("transitionsIGointTo: ${transitionsIGointTo.map { it.source.value + "->"+it.symbol+"->"+it.target.value }}")
                var transitionsPointingToMe = state.getTransitionsPointingToMe()
                //println("transitionsPointingToMe: ${transitionsPointingToMe.map { it.source.value + "->"+it.symbol+"->"+it.target.value }}")

                var transitions:MutableList<Transition> =  mutableListOf()
                for (p in transitionsPointingToMe){
                    //println("0 - ${p.source.value} -> [${p.symbol}] -> ${p.target.value}")
                    if(p==transition) continue
                    for (t in transitionsIGointTo){
                        if(t==transition) continue
                        var symbol = (if(p.symbol.equals("E")) "" else p.symbol) + star + (if(t.symbol.equals("E")) "" else t.symbol)
                        var source = p.source
                        var target = t.target
                        println("1 - ${source.value} -> [$symbol] -> ${target.value}")
                        transitions.add(Transition(symbol,source,target))
                    }
                }

                while (transitionsPointingToMe.any()){
                    var t = transitionsPointingToMe.first()
                    //println("2 - ${t.source.value} -> [${t.symbol}] -> ${t.target.value}")
                    t.source.removeTransition(t)
                    t.target.removeTransition(t)
                    transitionsPointingToMe.remove(t)
                }

                while (transitionsIGointTo.any()){
                    var t = transitionsIGointTo.first()
                    //println("3 - ${t.source.value} -> [${t.symbol}] -> ${t.target.value}")
                    t.source.removeTransition(t)
                    t.target.removeTransitionPointingToMe(t)
                    transitionsIGointTo.remove(t)
                }

                nfae.removeState(state.value)

                for(newTransition in transitions){
                    newTransition.source.addTransition(newTransition)
                    newTransition.target.addTransitionPointingToMe(newTransition)
                }
            }
        }

        return nfae
    }

    override fun clone(): DFA {
        val clone = DFA()
        for(state in this.states){
            clone.addState(State(state.value))
        }

        for(state in this.states){
            for(transition in state.getTransitions()){
                clone.addTransition(transition.symbol.first(),transition.source.value,transition.target.value)
            }
        }

        var initial=getInitialState()
        if(initial!=null)
            clone.setInitialState(initial.value)

        for(state in this.getFinalStates()){
            clone.setFinalState(state.value)
        }

        return clone
    }

    override fun toMinimizedDFA(): DFA {
        val pairsTable:MutableList<Pair<State,State>> = mutableListOf()
        val markedPairs:MutableList<Pair<State,State>> = mutableListOf()
        val unmarkedPairs:MutableList<Pair<State,State>> = mutableListOf()


        //Step 1
        val visitedStates:MutableList<State> = mutableListOf()
        for (qi in this.states){
            for (qj in this.states.filter { !visitedStates.contains(it) }){
                if(qi!=qj){
                    pairsTable.add(Pair(qi,qj))
                }
            }
            visitedStates.add(qi)
        }

        //Step 2
        for (pair in pairsTable){

            if(this.isFinal(pair.first.value) and !this.isFinal(pair.second.value)){
                markedPairs.add(pair)
            }else if(this.isFinal(pair.second.value) and !this.isFinal(pair.first.value)){
                markedPairs.add(pair)
            }else{
                unmarkedPairs.add(pair)
            }
        }

        //Step 3
        var up:MutableList<Pair<State,State>> = mutableListOf()
        up=up.union(unmarkedPairs).toMutableList()
        val iterate = up.listIterator()
        while (iterate.hasNext()){
            val pair=iterate.next()
            for (a in this.language){
                try {
                    val A=delta(pair.first,a)
                    val B=delta(pair.second,a)
                    val list = markedPairs.filter { (it.first.value == A.value && it.second.value == B.value) }
                    val result=list.firstOrNull()
                    println("(${pair.first.value},${pair.second.value}):$a ==>(${A.value}.${B.value}):${result!=null}")
                    if(result!=null){
                        markedPairs.add(pair)
                        unmarkedPairs.remove(pair)
                    }
                }catch (e:Exception){
                    markedPairs.add(pair)
                    unmarkedPairs.remove(pair)
                }

            }
            iterate.remove()
        }

        //Step 4
        val newStates: MutableList<MutableList<State>> = mutableListOf()

        for (pair in unmarkedPairs){
            newStates.add(pair.toList().toMutableList())
        }

        val toAddLater:MutableList<MutableList<State>> = mutableListOf()
        for (state in this.states){
            val toCombine = newStates.filter { it.contains(state) }
            if(toCombine.count()>1){
                //Combine
                newStates.removeAll(toCombine)
                val states: MutableList<State> = toCombine.flatten().toMutableList()
                newStates.add(states)
            }else if(toCombine.count()==0){
                toAddLater.add(mutableListOf(state))
            }
        }

        newStates.addAll(toAddLater)
        //Create minimized DFA
        val minimizedDfa:DFA = DFA()
        for (states in newStates){
            minimizedDfa.addState(State(states.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()))
        }

        for (states in newStates){
            for (source in states){
                if(states.contains(this.getInitialState())){
                    minimizedDfa.setInitialState(states.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim())
                }
                if(this.isFinal(source.value)){
                    minimizedDfa.setFinalState(states.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim())
                }

                for (symbol in this.language){
                    try {
                        val destiny = delta(source, symbol)
                        val msource = states.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim()
                        val targets = newStates.filter { it.contains(destiny) }

                        for (target in targets){
                            val mdestiny = target.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim()
                            minimizedDfa.addTransition(symbol,msource,mdestiny)
                        }
                    } catch(e: Exception) {

                    }
                }
            }
        }

        return minimizedDfa
    }

    private fun unify(A:DFA,B:DFA,op: AutomataOperation): DFA {
        A.renameStates('A')
        B.renameStates('B')
        val states: MutableList<MutableList<State>> = mutableListOf()
        val ai = A.getInitialState()
        val bi = B.getInitialState()
        if(ai==null || bi==null)  throw Exception("Initial state is not set")
        var initial = mutableListOf(ai,bi)
        states.add(initial)

        var automata = DFA()
        var initialStateName = initial.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
        automata.addState(State(initialStateName))
        automata.setInitialState(initialStateName)

        if(A.isFinal(ai.value) or B.isFinal(bi.value)){
            automata.setFinalState(initialStateName)
        }

        var acceptedLanguage = A.language.union(B.language)

        var iterate = states.listIterator()
        while (iterate.hasNext()){
            var group = iterate.next()
            for (symbol in acceptedLanguage){
                var targetGroup:MutableList<State> = mutableListOf()
                for (state in group){
                    try {
                        targetGroup.add(state.getTransition(symbol)!!.target)
                    }catch (e:Exception){

                    }
                }

                if(targetGroup.count()>0){
                    states.remove(group)

                    var sourceName = group.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
                    var targetName = targetGroup.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
                    if(!automata.hasState(targetName)){
                        automata.addState(State(targetName))
                        states.add(targetGroup)
                    }
                    when(op){
                        Union -> {
                            if((A.finals.intersect(targetGroup).count()>0) or (B.finals.intersect(targetGroup).count()>0)){
                                automata.setFinalState(targetName)
                            }
                        }
                        Intersect -> {
                            if((A.finals.intersect(targetGroup).count()>0) and (B.finals.intersect(targetGroup).count()>0)){
                                automata.setFinalState(targetName)
                            }
                        }
                        Subtract -> {
                            if((A.finals.intersect(targetGroup).count()>0)){
                                if(B.finals.intersect(targetGroup).count()==0){
                                    automata.setFinalState(targetName)
                                }
                            }
                        }

                    }

                    if(!automata.hasTransition(symbol,sourceName,targetName))
                        automata.addTransition(symbol,sourceName,targetName)
                    iterate = states.listIterator()
                }

            }
        }

        return automata
    }

    fun union(B:DFA): DFA{
        return unify(this,B, Union)
    }

    fun intersect(B:DFA): DFA{
        return unify(this,B, Intersect)
    }

    fun subtract(B:DFA): DFA{
        return unify(this,B, Subtract)
    }

    fun complement(): DFA{
        var complement = this.clone()

        for (state in complement.states){
            if(this.isFinal(state.value))
                complement.removeFinalState(state.value)
            else
                complement.setFinalState(state.value)
        }

        var sumidero: State? = null

        for (state in this.states){
            for (symbol in complement.language){
                if(state.getTransition(symbol)==null){
                    if(sumidero==null){
                        sumidero= State("drain")
                        complement.addState(sumidero)
                        complement.setFinalState(sumidero.value)
                        for (a in complement.language){
                            complement.addTransition(a,sumidero.value,sumidero.value)
                        }
                        complement.addTransition(symbol,state.value,sumidero.value)
                    }else{
                        complement.addTransition(symbol,state.value,sumidero.value)
                    }
                }
            }
        }

        return complement
    }
}

