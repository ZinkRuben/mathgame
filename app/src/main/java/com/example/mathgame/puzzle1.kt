package com.example.mathgame

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.Validators.not
import kotlinx.android.synthetic.main.activity_puzzle1.*
import java.util.*



class puzzle1 : AppCompatActivity() {
    private val equalsTo = intent.getStringExtra("equalsTo")
    private val puzzle = intent.getStringExtra("puzzle")
    private val originalText = "$puzzle=$equalsTo"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle1)
        equationET.setText(originalText)
        equationET.showSoftInputOnFocus = false
        equationET.isLongClickable = false

        CButton.setOnClickListener { delete() }
        ACButton.setOnClickListener { clearAll() }
        SubmitButton.setOnClickListener { checkIfValid() }

        rightArrowButton.setOnClickListener { movePosition(false) }
        leftArrowButton.setOnClickListener { movePosition(true) }

        plusButton.setOnClickListener { writeSymbols("+") }
        minusButton.setOnClickListener { writeSymbols("-") }
        exponentialButton.setOnClickListener { writeSymbols("^") }
        mulitplicationButton.setOnClickListener { writeSymbols("*") }
        divisonButton.setOnClickListener { writeSymbols("/") }
        sqrtButton.setOnClickListener { writeSymbols("√") }
        openParenthesisButton.setOnClickListener { writeSymbols("(") }
        closeParenthesisButton.setOnClickListener { writeSymbols(")") }
        factorialButton.setOnClickListener { writeSymbols("!") }

    }


    private fun writeSymbols(symbol: String) {
        if (equationET.selectionStart < (equationET.text.length-1)) {
            equationET.text.insert(equationET.selectionStart, symbol)
        }
    }

    @SuppressLint("SetTextI18n")
    //todo cursor is a little glitchy when deleting text, jumps to the end or beginning

    fun delete() {
        //checks that we're not trying to delete the -1th character (crash) or a part of the original text
        if ((equationET.selectionStart == 0) or (equationET.selectionStart == (equationET.text.length-1)) or (equationET.text.toString()[equationET.selectionStart-1].isDigit()) ) {
            return
        }
            val remember = equationET.selectionStart
            equationET.setText(equationET.text.substring(0, equationET.selectionStart-1) + equationET.text.substring(equationET.selectionStart, equationET.text.length))
            equationET.setSelection(remember-1)

    }
    private fun clearAll() {
        equationET.setText(originalText)
    }

    private fun movePosition(DirectionIsLeft: Boolean){
        if (DirectionIsLeft and (equationET.selectionStart != 0)) {
            equationET.setSelection(equationET.selectionStart-1)
        }
        else if(!(DirectionIsLeft) and (equationET.selectionStart < (equationET.text.length-2))){
            equationET.setSelection(equationET.selectionStart+1)
        }
    }
    private fun checkIfValid() {
        //todo add expetions if not evaluateable

        val equation = equationET.text

        val list = equation.split("=")
        var leftSide = list[0]
        val rightSide = list[1]
        leftSide = leftSide.replace("√", "sqrt")
        var result = eval(leftSide)
        val inInt = result.toInt()
        equationET.setText("$inInt=$rightSide")

        }

    fun factorial(number: Double): Double {
        val numberk = number.toInt()
        var result = 1
        println(numberk)
        for (n in 2..numberk) {
            result *= n
            println(result)
        }
        return result.toDouble()
    }
    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm() // addition
                    else if (eat('-'.toInt())) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt())) x *= parseFactor() // multiplication
                    else if (eat('/'.toInt())) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // unary plus
                if (eat('-'.toInt())) return -parseFactor() // unary minus
                var x: Double
                val startPos = pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()

                    x = if (func == "sqrt") Math.sqrt(x) else if (func == "sin") Math.sin(Math.toRadians(x)) else if (func == "cos") Math.cos(Math.toRadians(x)) else if (func == "tan") Math.tan(Math.toRadians(x)) else throw RuntimeException("Unknown function: $func")
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation

                if (eat('!'.toInt())) x = factorial(x)
                return x
            }
        }.parse()
    }

}