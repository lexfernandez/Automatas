package automata

/**
 * Created by Alex Fernandez on 09/05/2016.
 */
class PDA():IAutomata{
    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()
    var stack = Stack<Char>(mutableListOf())
    var stackLanguage: MutableList<Char> = mutableListOf()
    val START_CHARACTER: Char = 'Z'
    private val  epsilon: Char = 'E'

    fun addTransition(symbol: Char, source: String, target: String,top:Char,toPush:List<Char>): Boolean {
        val s = getState(source)
        val f = getState(target)
        val ts = s.getTransitions(symbol)
//        for (t in ts){
//            if(t.source.value==source && t.target.value==target){
//                throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
//            }
//        }

        val transition: Transition = Transition(symbol.toString(), s, f,top,toPush)
        addLanguageSymbol(symbol)
        f.addTransitionPointingToMe(transition)
        return s.addTransition(transition)
    }



    override fun evaluate(alphabet: String): Boolean {
        println("NFAE Evaluation")
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        stack.items.clear()
        stack.push(START_CHARACTER)

        val result: State? = deltaExtended(init,alphabet,stack)
        return  getFinalStates().contains(result)
    }




    private fun deltaExtended(state: State, alphabet: String, stack: Stack<Char>): State? {
        var top: Char = if(stack.isEmpty()) epsilon else stack.pop()!!

        if(alphabet.isEmpty()){
            for (transition in delta(state,epsilon,top)){
                var newStack = Stack<Char>(stack.items.map { it }.toMutableList())
                for (element in transition.second.reversed()){
                    if(!element.equals(epsilon)){
                        newStack.push(element)
                    }
                }
                var result = deltaExtended(transition.first,alphabet,newStack)
                if(result!=null){
                    println("1 - $result")
                    return result
                }
            }
            println("current - $state")
            return state
        }

        val a = alphabet.first()
        val x = alphabet.subSequence(1,alphabet.length).toString()

        for (transition in delta(state,a,top)){
            var newStack = Stack<Char>(stack.items.map { it }.toMutableList())
            for (element in transition.second.reversed()){
                if(!element.equals(epsilon)){
                    newStack.push(element)
                }
            }
            var result = deltaExtended(transition.first,x,newStack)
            if(result!=null){
                println("2 - $result")
                return result
            }
        }


        for (transition in delta(state,epsilon,top)){
            var newStack = Stack<Char>(stack.items.map { it }.toMutableList())
            for (element in transition.second.reversed()){
                if(!element.equals(epsilon)){
                    newStack.push(element)
                }
            }
            var result = deltaExtended(transition.first,alphabet,newStack)
            if(result!=null){
                println("3 - $result")
                return result
            }
        }

        println("null")
        return null
    }

    private fun delta(q: State, symbol: Char, top: Char?): List<Pair<State, List<Char>>> {
        if (q.getTransition(symbol)!=null){
            val qs = q.getTransitions(symbol).filter { it.top==top }.map{ Pair(it.target,it.toPush) }
            return qs
        }
        else{
            return listOf()
        }

    }

    private fun getTargetsName(states: String, symbol: Char): String?{
        var deltas: MutableList<State> = mutableListOf()

        for (value in states.split(",")){
            if(hasState(value.trim())){
                val state = getState(value.trim())
               // deltas = deltas.union(delta(state, symbol, top)).toMutableList()
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

    override fun toDFA(): DFA {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toRegex(): NFAE {
        return NFAE()
    }

    override fun toMinimizedDFA(): DFA {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}