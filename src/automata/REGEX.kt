package automata

import org.unitec.regularexpresion.RegularExpressionParser
import org.unitec.regularexpresion.tree.*

/**
 * Created by lex on 09-19-16.
 */
class REGEX private  constructor(): NFAE() {

    var  expression: String = ""

    constructor(expression: String=""):this(){
        fromRegex(expression)
    }

    fun fromRegex(regex: String) {
        if(regex.isEmpty()) return
        this.states.clear()
        this.finals.clear()
        this.initial = null
        this.language.clear()
        var rootNode: Node = RegularExpressionParser().Parse(sanitizeRegex(regex))
        var nfae = regexToNFAE(rootNode)
        for (state in nfae.states){
            this.states.add(state)
        }
        this.initial=nfae.initial
        this.finals.addAll(nfae.finals)
        this.language = nfae.language
        this.expression = regex
    }

    private fun  sanitizeRegex(regex: String): String {
        var regexp = regex.replace(" ","").replace(".","").replace("[","(").replace("]",")")
        var ret: String =""
        var c:Char
        var c2:Char
        for (i in 0..regexp.length-1){
            c=regexp[i];
            if(i+1<regexp.length){
                c2=regexp[i+1]
                ret+=c;
                if(c!='('&&c2!=')'&&c!='+'&&c2!='+'&&c2!='*'){
                    ret+='.'
                }
            }
        }
        ret+=regexp[regexp.length-1]
        return ret
    }

    private fun regexToNFAE(node: Node):NFAE{

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

        S.language.forEach { symbol -> R.addLanguageSymbol(symbol) }
        R.language = R.language.distinct().toMutableList()
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
        R.language = R.language.distinct().toMutableList()
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

}