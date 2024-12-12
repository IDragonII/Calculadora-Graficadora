package com.example.calculadora2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class GraphActivity : AppCompatActivity() {
    private lateinit var graphView: GraphView
    private lateinit var editTextFunction: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        editTextFunction = findViewById(R.id.editTextFunction)
        graphView = findViewById(R.id.graph_view)

        val btnDrawGraph: Button = findViewById(R.id.btnDrawGraph)
        btnDrawGraph.setOnClickListener {
            val function = editTextFunction.text.toString()
            graphView.setFunction(function) // Pasar la función a GraphView
            graphView.invalidate() // Redibujar el gráfico
        }
    }
}
