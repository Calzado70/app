package com.example.inventario;

import androidx.annotation.NonNull;

public class Registro {
    String pareja;
    String zona;
    String conteo;
    String codigo;
    String fecha;
    String talla;
    int cantidad;

    public Registro(String pareja, String zona, String conteo, String codigo, String fecha, String talla, int cantidad) {
        this.pareja = pareja;
        this.zona = zona;
        this.conteo = conteo;
        this.codigo = codigo;
        this.fecha = fecha;
        this.talla = talla;
        this.cantidad = cantidad;
    }

    @NonNull
    @Override
    public String toString() {
        return "Pareja: " + pareja + ", Zona: " + zona + ", Conteo: " + conteo +
                ", Código: " + codigo + ", Fecha: " + fecha + ", Talla: " + talla + ", Cantidad: " + cantidad;
    }
}
