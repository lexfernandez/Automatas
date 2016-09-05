package automata

/**
 * Created by Alex Fernandez on 09/05/2016.
 */
class PDA():IAutomata {
    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()
    var stack = Stack<Char>(mutableListOf())

    fun addTransition(symbol: Char, source: String, target: String,top:Char,toPush:List<Char>): Boolean {
        val s = getState(source)
        val f = getState(target)
        val ts = s.getTransitions(symbol)
        for (t in ts){
            if(t.source.value==source && t.target.value==target){
                throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
            }
        }

        val transition: Transition = Transition(symbol.toString(), s, f,top,toPush)
        addLanguageSymbol(symbol)
        f.addTransitionPointingToMe(transition)
        return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        println("NFAE Evaluation")
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        val result: List<State> = deltaExtended(eClosure(init),alphabet)
        return (finals.isEmpty() and stack.isEmpty()) or (!getFinalStates().intersect(result).isEmpty())
    }

    private fun deltaExtended(eclosures: List<State>, alphabet: String): List<State> {
        if(alphabet.isEmpty()){
            return eclosures
        }

        val a = alphabet.first()
        val x = alphabet.subSequence(1,alphabet.length).toString()
        val top=stack.pop()



        //print("s: $a ")
        //println("c:${eclosures.map { it.value }}")
//        var deltas : MutableList<State> = mutableListOf()
//
//        for(cstate in eclosures){
//            val delta = delta(cstate,a,top?:'E')
//            deltas = deltas.union(delta).toMutableList()
//        }


        //q.getTransitions(a).filter { it.top==top }.map{ it.target }


//        var result : MutableList<State> = mutableListOf()
//        for (d in deltas){
//            result = result.union(eClosure(d)).toMutableList()
//        }

        return deltaExtended(result,x)
    }



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
        var c = state.getTransitions('E').map { it.target }
        for(s in c){
            c=c.union(eClosure(s)).toList()
        }
        c=c.union(listOf(state)).toList()
        return c.sortedBy { it.value }
    }

    override fun toDFA(): DFA {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toRegex(): String {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toMinimizedDFA(): DFA {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}