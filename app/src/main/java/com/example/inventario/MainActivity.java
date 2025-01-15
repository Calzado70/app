package com.example.inventario;

import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Variables de la interfaz
    private Spinner parejaSpinner, zonaSpinner, conteoSpinner;
    private EditText codigoEditText;
    private ListView registrosListView;

    // Datos en memoria
    private HashMap<String, Integer> codigoCantidadMap; // Mapa para códigos y cantidades
    private ArrayList<String> registros; // Lista para mostrar datos en formato texto
    private ArrayAdapter<String> registrosAdapter;

    // Opciones de Pareja, Zona y Conteo
    private final String[] PAREJAS = {"pareja1", "pareja2", "pareja3", "pareja4", "pareja5", "pareja6", "pareja7", "pareja8"};
    private final String[] ZONAS = {"Laboratorio", "Terceros", "Oficina", "MezaniA", "MezaniB", "Estanteria_lado_A", "Estanteria_lado_B", "Estanteria_lado_C"};
    private final String[] CONTEOS = {"conteo1", "conteo2", "conteo3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar los elementos de la interfaz
        parejaSpinner = findViewById(R.id.spinner_pareja);
        zonaSpinner = findViewById(R.id.spinner_zona);
        conteoSpinner = findViewById(R.id.spinner_conteo);
        codigoEditText = findViewById(R.id.edittext_codigo);
        registrosListView = findViewById(R.id.listview_registros);
        Button addButton = findViewById(R.id.button_add);
        Button exportButton = findViewById(R.id.button_exportar);

        // Configurar Spinners
        parejaSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PAREJAS));
        zonaSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ZONAS));
        conteoSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CONTEOS));

        // Inicializar datos en memoria
        codigoCantidadMap = new HashMap<>();
        registros = new ArrayList<>();
        registrosAdapter = new ArrayAdapter<>(this, R.layout.list_item_layout, registros);
        registrosListView.setAdapter(registrosAdapter);

        // Listener para procesar lecturas automáticas con la pistola lectora
        codigoEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                procesarLectura();
                return true;
            }
            return false;
        });

        // Botón Añadir Lectura
        addButton.setOnClickListener(view -> procesarLectura());

        // Botón Exportar a Excel
        exportButton.setOnClickListener(view -> exportToExcel());
    }

    private void procesarLectura() {
        // Obtener el código del campo de texto
        String codigo = codigoEditText.getText().toString().trim();

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Código vacío, intente de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener valores de los spinners
        String pareja = parejaSpinner.getSelectedItem().toString();
        String zona = zonaSpinner.getSelectedItem().toString();
        String conteo = conteoSpinner.getSelectedItem().toString(); // Obtener conteo seleccionado

        // Incrementar cantidad si el código ya existe, o añadirlo si es nuevo
        int nuevaCantidad = codigoCantidadMap.getOrDefault(codigo, 0) + 1;
        codigoCantidadMap.put(codigo, nuevaCantidad);

        // Calcular fecha y talla
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String talla = codigo.length() >= 2 ? codigo.substring(codigo.length() - 2) : "NA";

        // Actualizar la lista de registros
        actualizarListaRegistros(pareja, zona, conteo, fecha, talla);

        // Limpiar el campo de código
        codigoEditText.setText("");
        Toast.makeText(this, "Lectura procesada.", Toast.LENGTH_SHORT).show();
    }

    private void actualizarListaRegistros(String pareja, String zona, String conteo, String fecha, String talla) {
        registros.clear();
        for (String codigo : codigoCantidadMap.keySet()) {
            int cantidad = codigoCantidadMap.get(codigo);
            String registro = "Pareja: " + pareja + ", Zona: " + zona + ", Conteo: " + conteo +
                    ", Código: " + codigo + ", Fecha: " + fecha + ", Talla: " + talla + ", Cantidad: " + cantidad;
            registros.add(registro);
        }
        registrosAdapter.notifyDataSetChanged();
    }

    private void exportToExcel() {
        if (codigoCantidadMap.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Crear un archivo Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Inventario");

            // Agregar encabezados
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Cantidad");

            // Agregar datos de códigos y cantidades
            int rowIndex = 1;
            for (String codigo : codigoCantidadMap.keySet()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(codigo);
                row.createCell(1).setCellValue(codigoCantidadMap.get(codigo));
            }

            // Guardar el archivo en la carpeta Descargas
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadDir, "inventario.xlsx");

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, "Datos exportados a: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al exportar.", Toast.LENGTH_SHORT).show();
        }
    }
}
