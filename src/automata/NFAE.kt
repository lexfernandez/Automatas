package automata

/**
 * Created by lex on 08-02-16.
 */

import java.io.Serializable
import org.unitec.regularexpresion.RegularExpressionParser
import org.unitec.regularexpresion.tree.*

class NFAE(): IAutomata, Serializable {
    override fun toMinimizedDFA(): DFA {
        return this.toDFA().toMinimizedDFA()
    }

    fun fromRegex(regex: String): NFAE {
        var rootNode: Node = RegularExpressionParser().Parse(regex)

        return regexToNFAE(rootNode)
    }

    private fun regexToNFAE(node:Node):NFAE{

        when(node){
            is CharNode -> {
                return getNFAeFromSymbol(node)
            }
            is ANDNode -> {
               return getNFAeFromAnd(node)
            }
            is ORNode -> {
                return  getNFAeFromOr(node)
            }
            is RepeatNode -> {
                return getNFAeFromStar(node)
            }
            else -> {
                throw Exception("not implemented")
            }
        }
    }

    private fun  getNFAeFromStar(node: RepeatNode): NFAE {
        var R = regexToNFAE(node.node).renameStates('R')

        var Rinitial=R.getInitialState()!!
        var Rfinals=R.getFinalStates()

        R.setInitialState(null)
        for (state in Rfinals){
            R.removeFinalState(state.value)
        }

        val initial: State = State("q0")
        val final: State = State("qf")

        R.addState(initial)
        R.addState(final)
        R.addTransition('E',initial.value,Rinitial.value)

        R.setInitialState(initial.value)

        for (state in Rfinals){
            R.addTransition('E',state.value,Rinitial.value)
            R.addTransition('E',state.value,final.value)
        }

        R.addTransition('E',initial.value,final.value)

        R.setFinalState(final.value)

        return R as NFAE
    }

    private fun  getNFAeFromOr(node: ORNode): NFAE {
        var R = regexToNFAE(node.leftNode).renameStates('R')
        var S = regexToNFAE(node.rightNode).renameStates('S')

        var Rinitial=R.getInitialState()!!
        var Sinitial=S.getInitialState()!!
        var Rfinals=R.getFinalStates()
        var Sfinals=S.getFinalStates()

        R.setInitialState(null)
        S.setInitialState(null)
        for (state in Rfinals){
            R.removeFinalState(state.value)
        }
        for (state in S.states){
            R.addState(state)
        }
//        for (state in Sfinals){
//            S.removeFinalState(state.value)
//        }
        val initial: State = State("q0")
        val final: State = State("qf")

        R.addState(initial)
        R.addState(final)
        R.addTransition('E',initial.value,Rinitial.value)
        R.addTransition('E',initial.value,Sinitial.value)
        R.setInitialState(initial.value)

        for (state in Rfinals){
            R.addTransition('E',state.value,final.value)
        }
        for (state in Sfinals){
            R.addTransition('E',state.value,final.value)
        }
        R.setFinalState(final.value)
        return R as NFAE
    }

    private fun  getNFAeFromAnd(node: ANDNode): NFAE {
        var R = regexToNFAE(node.leftNode).renameStates('R')
        var S = regexToNFAE(node.rightNode).renameStates('S')

        var Rfinals = R.getFinalStates()
        var Sinitial = S.getInitialState()!!

        R.finals.clear()

        S.states.forEach { state -> R.addState(state) }

        Rfinals.forEach { state -> R.addTransition('E',state.value,Sinitial.value) }

        for (state in S.finals){
            R.setFinalState(state.value)
        }

        S.language.forEach { symbol -> R.addLanguageSymbol(symbol) }

        return R as NFAE
    }

    private fun  getNFAeFromSymbol(symbol: CharNode): NFAE {
        val nfae=NFAE()
        val initial: State = State("q0")
        val final: State = State("qf")
        nfae.addState(initial)
        nfae.addState(final)
        nfae.addTransition(symbol.value.first(),initial.value,final.value)
        nfae.setInitialState(initial.value)
        nfae.setFinalState(final.value)
        return nfae
    }

    override fun toRegex(): DFA {
        return this.toDFA().toRegex()
    }

    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()

    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        val s = getState(source)
        val f = getState(target)
        val ts = s.getTransitions(symbol)
        for (t in ts){
            if(t.source.value==source && t.target.value==target){
                throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
            }
        }

        val transition: Transition = Transition(symbol.toString(), s, f)
        addLanguageSymbol(symbol)
        f.addTransitionPointingToMe(transition)
        return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        println("NFAE Evaluation")
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        val result: List<State> = deltaExtended(eClosure(init),alphabet)
        return !getFinalStates().intersect(result).isEmpty()
    }

    private fun deltaExtended(eclosures: List<State>, alphabet: String): List<State> {
        if(alphabet.isEmpty()){
            return eclosures
        }

        val a = alphabet.first()
        val x = alphabet.subSequence(1,alphabet.length).toString()

        //print("s: $a ")
        //println("c:${eclosures.map { it.value }}")
        var deltas : MutableList<State> = mutableListOf()

        for(cstate in eclosures){
            val delta = delta(cstate,a)
            deltas = deltas.union(delta).toMutableList()
        }

        var result : MutableList<State> = mutableListOf()
        for (d in deltas){
            result = result.union(eClosure(d)).toMutableList()
        }

        return deltaExtended(result,x)
    }

    private fun delta(q: State, symbol: Char): List<State> {
        if (q.getTransition(symbol)!=null){
            //println("Delta ${q.value},$symbol:")
            val qs = q.getTransitions(symbol).map{ it.target }

            return qs
        }
        else{
            //println("Delta $symbol: {}")
            return listOf()
        }

    }

//    fun printTable(){
//        var table: List<Pair<String,String>>
//        for (state in states){
//            print("${state.value}|")
//            for (symbol in language){
//                var transitions = state.getTransitions(symbol).map{ it.target.value }.sorted()
//                if(transitions.count()==0) continue
//                var conjunto = transitions.toString().replace("[","").replace("]","").trim()
//                print("$conjunto|")
//            }
//            print("\n")
//        }
//    }

    private fun getTargetsName(states: String, symbol: Char): String?{
        var deltas: MutableList<State> = mutableListOf()

        for (value in states.split(",")){
            if(hasState(value.trim())){
                val state = getState(value.trim())
                deltas = deltas.union(delta(state,symbol)).toMutableList()
            }
        }
        print("$symbol: e-Closure(${deltas.map { it.value }})")

        var closures: MutableList<State> = mutableListOf()
        for(delta in deltas){
            closures = closures.union(eClosure(delta)).toMutableList()
        }
        //println(closures.map { it.value }.sorted())
        return closures.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
    }

    private fun eClosure(state: State): List<State> {

        var closure = state.getTransitions('E').map { it.target }
        closure = closure.union(listOf(state)).toMutableList()

        var toVisit:Queue<State> = Queue()
        toVisit.Queue(closure.toMutableList())

        while (toVisit.isNotEmpty()){
            var current = toVisit.dequeue()
            if(current!=null){
                var currentClosure = current.getTransitions('E').map { it.target }.subtract(closure)
                toVisit.items=toVisit.items.union(currentClosure).toMutableList()
                closure=closure.union(currentClosure).toMutableList()

            }
        }

//        for(s in c){
//            c=c.union(eClosure(s)).toList()
//        }
//        c=c.union(listOf(state)).toList()
        println("eC(${state.value})=${closure.map { it.value }}")
        return closure.sortedBy { it.value }
    }

//    fun printClosure(){
//        for (state in states){
//            println(eClosure(state).map { it.value })
//        }
//    }

    override fun toDFA(): DFA{
        val dfa = DFA()
        val queue: Queue<State> = Queue()
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        val closure = eClosure(init)
        val initial = State(closure.map { it.value }.toString().replace("[","").replace("]","").trim())
        queue.enqueue(initial)
        dfa.addState(initial)
        dfa.setInitialState(initial.value)
        for (c in closure){
            if (this.isFinal(c.value)){
                dfa.setFinalState(initial.value)
                break
            }
        }

        while (queue.isNotEmpty()){
            val q = queue.dequeue()
            if(q!=null) {
                //println("State: ${q.value}| ")
                for (symbol in language){
                    val newStateName = getTargetsName(q.value,symbol)
                    //if(newStateName!=null)
                        //println("$symbol : $newStateName| ")
                    if(newStateName!=null && newStateName.isNotEmpty()){
                        if(!dfa.hasState(newStateName)){
                            val newState = State(newStateName)
                            dfa.addState(newState)
                            queue.enqueue(newState)
                        }

                        if(!dfa.hasTransition(symbol,q.value,newStateName)){
                            dfa.addTransition(symbol,q.value,newStateName)
                        }

                        for (name  in newStateName.split(",")){
                            if (this.isFinal(name.trim())){
                                dfa.setFinalState(newStateName)
                                break
                            }
                        }
                    }
                }
                print("\n")
            }
        }
        return dfa
    }

}