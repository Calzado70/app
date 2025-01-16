package com.example.inventario;

import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Variables de la interfaz
    private Spinner parejaSpinner, zonaSpinner, conteoSpinner;
    private EditText codigoEditText;

    // Datos en memoria
    private ArrayList<Registro> registros; // Lista para almacenar los registros con todos los detalles
    private ArrayAdapter<String> registrosAdapter;

    // Opciones de Pareja, Zona y Conteo
    private final String[] PAREJAS = {"pareja1", "pareja2", "pareja3", "pareja4", "pareja5", "pareja6", "pareja7", "pareja8"};
    private final String[] ZONAS = {"Laboratorio", "Terceros", "Oficina", "MezaniA", "MezaniB", "Estanteria_lado_A", "Estanteria_lado_B", "Estanteria_lado_C"};
    private final String[] CONTEOS = {"conteo1", "conteo2", "conteo3"};

    // Contraseña para eliminar
    private static final String PASSWORD = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar los elementos de la interfaz
        parejaSpinner = findViewById(R.id.spinner_pareja);
        zonaSpinner = findViewById(R.id.spinner_zona);
        conteoSpinner = findViewById(R.id.spinner_conteo);
        codigoEditText = findViewById(R.id.edittext_codigo);
        ListView registrosListView = findViewById(R.id.listview_registros);
        Button addButton = findViewById(R.id.button_add);
        Button exportButton = findViewById(R.id.button_exportar);

        // Configurar Spinners
        parejaSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PAREJAS));
        zonaSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ZONAS));
        conteoSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CONTEOS));

        // Inicializar datos en memoria
        registros = new ArrayList<>();
        registrosAdapter = new ArrayAdapter<>(this, R.layout.list_item_layout, new ArrayList<>());
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

        // Listener para eliminar un registro con clic largo
        registrosListView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (registros.isEmpty()) {
                Toast.makeText(this, "No hay registros para eliminar.", Toast.LENGTH_SHORT).show();
            } else {
                // Solicitar contraseña para eliminar el último registro
                promptPasswordForDeletion(registros.size() - 1); // Posición del último registro
            }
            return true;
        });
    }

    private void procesarLectura() {
        String codigo = codigoEditText.getText().toString().trim();

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Código vacío, intente de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        String pareja = parejaSpinner.getSelectedItem().toString();
        String zona = zonaSpinner.getSelectedItem().toString();
        String conteo = conteoSpinner.getSelectedItem().toString();
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String talla = codigo.length() >= 2 ? codigo.substring(codigo.length() - 2) : "NA";

        boolean encontrado = false;
        for (Registro registro : registros) {
            if (registro.codigo.equals(codigo)) {
                registro.cantidad++;
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            registros.add(new Registro(pareja, zona, conteo, codigo, fecha, talla, 1));
        }

        actualizarListaRegistros();
        codigoEditText.setText("");
        Toast.makeText(this, "Lectura procesada.", Toast.LENGTH_SHORT).show();
    }

    private void actualizarListaRegistros() {
        registrosAdapter.clear();
        for (Registro registro : registros) {
            registrosAdapter.add(registro.toString());
        }
        registrosAdapter.notifyDataSetChanged();
    }

    private void eliminarRegistro(int position) {
        registros.remove(position);
        actualizarListaRegistros();
        Toast.makeText(this, "Registro eliminado.", Toast.LENGTH_SHORT).show();
    }

    private void exportToExcel() {
        if (registros.isEmpty()) {
            Toast.makeText(this, "No hay registros para exportar.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Crear un libro de Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Registros");

            // Crear encabezado
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Pareja");
            headerRow.createCell(1).setCellValue("Zona");
            headerRow.createCell(2).setCellValue("Conteo");
            headerRow.createCell(3).setCellValue("Código");
            headerRow.createCell(4).setCellValue("Fecha");
            headerRow.createCell(5).setCellValue("Talla");
            headerRow.createCell(6).setCellValue("Cantidad");

            // Agregar registros a la hoja
            for (int i = 0; i < registros.size(); i++) {
                Registro registro = registros.get(i);
                Row row = sheet.createRow(i + 1); // La fila 0 es para el encabezado
                row.createCell(0).setCellValue(registro.pareja);
                row.createCell(1).setCellValue(registro.zona);
                row.createCell(2).setCellValue(registro.conteo);
                row.createCell(3).setCellValue(registro.codigo);
                row.createCell(4).setCellValue(registro.fecha);
                row.createCell(5).setCellValue(registro.talla);
                row.createCell(6).setCellValue(registro.cantidad);
            }

            // Obtener valores para el nombre del archivo
            String zona = zonaSpinner.getSelectedItem().toString();
            String conteo = conteoSpinner.getSelectedItem().toString();
            String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = zona + "_" + conteo + "_" + fecha + ".xlsx";

            // Guardar archivo en la carpeta de Descargas
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            // Notificar éxito
            Toast.makeText(this, "Datos exportados correctamente: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Limpiar registros y actualizar lista
            registros.clear();
            actualizarListaRegistros();

        } catch (Exception e) {
            // Manejar errores
            Toast.makeText(this, "Error al exportar los datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void promptPasswordForDeletion(int position) {
        EditText passwordInput = new EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("Introduce la contraseña para eliminar este producto:")
                .setView(passwordInput)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String enteredPassword = passwordInput.getText().toString();
                    if (PASSWORD.equals(enteredPassword)) {
                        eliminarRegistro(position);
                    } else {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

}
