package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.myapplication.R
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        val btn = view.findViewById<Button>(R.id.btnEmpezar)

        btn.setOnClickListener {
            findNavController().navigate(R.id.btnCliente)
        }

        return view
    }
}