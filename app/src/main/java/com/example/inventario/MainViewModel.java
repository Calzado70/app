package com.example.inventario;

import androidx.lifecycle.ViewModel;

import com.example.inventario.Registro;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    public ArrayList<Registro> registros = new ArrayList<>();
}
