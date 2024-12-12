package com.example.calculadora2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import android.content.Intent
import java.util.*
import android.widget.PopupMenu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private var primerNumero: Double = Double.NaN
    private var segundoNumero: Double = Double.NaN
    private lateinit var tvTemp: TextView
    private lateinit var tvResult: TextView
    private var operacionActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTemp = findViewById(R.id.tvTemp)
        tvResult = findViewById(R.id.tvResult)

        val fabMenu = findViewById<FloatingActionButton>(R.id.fab_menu)
        fabMenu.setOnClickListener {
            showPopupMenu(fabMenu)
        }
    }

    private fun showPopupMenu(view: FloatingActionButton) {
        // Crear el PopupMenu
        val popupMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.dropdown_menu, popupMenu.menu)

        // Manejar la selección de ítems del menú
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.item1 -> {
                    val intent = Intent(this, GraphActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item2 -> {
                    // Acción para la opción 2
                    true
                }
                R.id.item3 -> {
                    // Acción para la opción 3
                    true
                }
                else -> false
            }
        }

        // Mostrar el menú
        popupMenu.show()
    }

    fun seleccionarNumero(view: View) {
        val button = view as Button
        val numero = button.text.toString()

        // Evitar ceros a la izquierda al ingresar números
        if (numero == "π") {
            // Agregar π formateado a 2 decimales
            if (tvTemp.text.toString() == "0") {
                tvTemp.text = String.format("%.2f", Math.PI) // Muestra pi como 3.14
            } else {
                tvTemp.append(String.format("%.2f", Math.PI)) // Agregar pi formateado
            }
        } else {
            // Evitar ceros a la izquierda para otros números
            if (tvTemp.text.toString() == "0" || tvTemp.text.toString() == "") {
                tvTemp.text = numero
            } else {
                when (numero) {
                    "ln" -> tvTemp.append("ln(") // Iniciar la función logaritmo natural
                    else -> tvTemp.append(numero) // Para los otros números
                }
            }
        }
    }



    fun cambiarOperador(view: View) {
        val button = view as Button
        val operador = button.text.toString()

        // Si el operador es "-", maneja el caso de signo negativo
        if (operador == "-" && (tvTemp.text.isEmpty() || tvTemp.text.last() in setOf('+', '-', '*', '/', '^', '('))) {
            tvTemp.append("-") // Agregar signo negativo
        } else {
            // Usar el resultado anterior como primerNumero si hay uno
            if (tvTemp.text.isEmpty() && primerNumero.isFinite()) {
                tvTemp.append(formatoDecimal(primerNumero))
            }

            if (tvTemp.text.isNotEmpty()) {
                val expresion = tvTemp.text.toString().trim()
                operacionActual = when (operador) {
                    "X" -> "*"
                    "^" -> "^"
                    "÷" -> "/"
                    else -> operador
                }
                tvTemp.text = "$expresion $operacionActual "
            }
        }
    }
    fun agregarParentesis(view: View) {
        val button = view as Button
        val paren = button.text.toString() // Obtén el texto del botón presionado

        // Contar paréntesis abiertos y cerrados en la expresión actual
        val textoActual = tvTemp.text.toString()
        val countOpen = textoActual.count { it == '(' }
        val countClose = textoActual.count { it == ')' }

        // Agregar paréntesis de apertura o cierre según el botón presionado
        if (paren == "(") {
            // Solo agregar paréntesis de apertura si el último carácter no es un operador
            // y el número de paréntesis abiertos es mayor que los cerrados
            if (textoActual.isEmpty() || !textoActual.split(" ").last().isOperatorOrFunction() || countOpen > countClose) {
                tvTemp.append("(")
            }
        } else if (paren == ")") {
            // Solo agregar un paréntesis de cierre si hay un paréntesis de apertura sin cerrar
            if (countOpen > countClose) {
                tvTemp.append(")")
            }
        }
    }

    // Función de extensión para verificar si un carácter es un operador o función
    private fun String.isOperatorOrFunction(): Boolean {
        return this in setOf("+", "-", "*", "/", "^", "(", ")") || this in setOf("sin", "cos", "tan", "ln", "sqrt")
    }

    fun borrar(b: View) {
        val boton: Button = b as Button
        when (boton.text.toString().trim()) {
            "C" -> {
                // Eliminar el último carácter en tvTemp
                if (tvTemp.text.toString().isNotEmpty()) {
                    tvTemp.text = tvTemp.text.toString().dropLast(1)
                }
            }
            "CA" -> {
                // Limpiar todo
                primerNumero = Double.NaN
                segundoNumero = Double.NaN
                tvTemp.text = ""
                tvResult.text = ""
            }
        }
    }


    fun igual(view: View) {
        try {
            val expresion = tvTemp.text.toString()
            val resultado = eval(expresion)
            tvResult.text = formatoDecimal(resultado)

            // Guardar el resultado como primerNumero
            primerNumero = resultado

            // Limpiar tvTemp para nuevas operaciones
            tvTemp.text = ""

        } catch (e: Exception) {
            tvResult.text = "Error"
        }
    }

    fun seleccionarFuncion(view: View) {
        val button = view as Button
        val funcion = button.text.toString()

        when (funcion) {
            "sin" -> tvTemp.append("sin(") // Agregar función seno
            "cos" -> tvTemp.append("cos(") // Agregar función coseno
            "tan" -> tvTemp.append("tan(") // Agregar función tangente
            "raiz" -> tvTemp.append("sqrt(") // Agregar función raíz cuadrada
        }
    }


    private fun eval(expr: String): Double {
        val output = LinkedList<String>()
        val operators = Stack<String>()

        val precedence = mapOf(
            "+" to 1,
            "-" to 1,
            "*" to 2,
            "/" to 2,
            "^" to 3,
            "(" to 0
        )

        val tokens = expr.replace(" ", "").split("(?<=[-+*/()^])|(?=[-+*/()^])".toRegex()).filter { it.isNotEmpty() }

        val processedTokens = mutableListOf<String>()
        for (i in tokens.indices) {
            if (tokens[i] == "-" && (i == 0 || tokens[i - 1] in precedence.keys || tokens[i - 1] == "(")) {
                processedTokens.add("-1")
                processedTokens.add("*")
            } else {
                processedTokens.add(tokens[i])
            }
        }

        for (token in processedTokens) {
            when {
                token.isNumber() -> output.add(token)
                token == "ln" || token == "sin" || token == "cos" || token == "tan" || token == "sqrt" -> operators.push(token)
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    operators.pop() // Quitar el "("

                    // Evaluar la función si hay una
                    if (operators.isNotEmpty() && operators.peek().let { it == "sin" || it == "cos" || it == "tan" || it == "sqrt" }) {
                        output.add(operators.pop())
                    }
                }
                else -> {
                    while (operators.isNotEmpty() && precedence[operators.peek()]!! >= precedence[token]!!) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.pop())
        }

        return evaluateRPN(output)
    }
    private fun evaluateRPN(tokens: List<String>): Double {
        val stack = Stack<Double>()

        for (token in tokens) {
            if (token.isNumber()) {
                stack.push(token.toDouble())
            } else {
                when (token) {
                    "ln" -> {
                        val value = stack.pop()
                        stack.push(Math.log(value))  // Logaritmo natural
                    }
                    "sin" -> {
                        val value = stack.pop()
                        stack.push(Math.sin(Math.toRadians(value))) // Seno
                    }
                    "cos" -> {
                        val value = stack.pop()
                        stack.push(Math.cos(Math.toRadians(value))) // Coseno
                    }
                    "tan" -> {
                        val value = stack.pop()
                        stack.push(Math.tan(Math.toRadians(value))) // Tangente
                    }
                    "sqrt" -> {
                        val value = stack.pop()
                        stack.push(Math.sqrt(value))  // Raíz cuadrada
                    }
                    else -> {
                        val b = stack.pop()
                        val a = stack.pop()
                        stack.push(
                            when (token) {
                                "+" -> a + b
                                "-" -> a - b
                                "*" -> a * b
                                "/" -> a / b
                                "^" -> Math.pow(a, b)
                                else -> throw IllegalArgumentException("Invalid operator: $token")
                            }
                        )
                    }
                }
            }
        }

        return stack.pop()
    }




    // Función de extensión para verificar si una cadena es un número
    private fun String.isNumber(): Boolean {
        return this.toDoubleOrNull() != null
    }

    // Función para formatear el resultado
    private fun formatoDecimal(numero: Double): String {
        val df = DecimalFormat("#.##") // Cambia el patrón según tus necesidades
        return df.format(numero)
    }
}
