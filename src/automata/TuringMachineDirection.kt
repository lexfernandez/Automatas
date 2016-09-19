package automata

import java.io.Serializable

enum class TuringMachineDirection(c: Char='>'): Serializable {
    Left('<'),
    Right('>')
}