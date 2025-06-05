package br.edu.utfpr.annycosta.controledecontas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import br.edu.utfpr.annycosta.controledecontas.modelo.Categoria;
import br.edu.utfpr.annycosta.controledecontas.persistencia.AppDatabase;
import br.edu.utfpr.annycosta.controledecontas.persistencia.CategoriaDao;

public class CategoriasActivity extends AppCompatActivity {
    private List<Categoria> categoriasList = new ArrayList<>();
    private ArrayAdapter<Categoria> adapter;
    private CategoriaDao categoriaDao;

    private final ActivityResultLauncher<Intent> cadastroCategoriaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    carregarCategorias();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        Toolbar toolbar = findViewById(R.id.toolbar_categorias);
        setSupportActionBar(toolbar);

        AppDatabase db = AppDatabase.getDatabase(this);
        categoriaDao = db.categoriaDao();

        ListView listView = findViewById(R.id.listViewCategorias);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoriasList);
        listView.setAdapter(adapter);

        carregarCategorias();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Categoria categoria = categoriasList.get(position);
            abrirCadastroCategoria(categoria);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Categoria categoria = categoriasList.get(position);
            mostrarDialogoExclusao(categoria);
            return true;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.categorias));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarCategorias();
    }

    private void carregarCategorias() {
        categoriaDao.getAllCategorias().observe(this, categorias -> {
            categoriasList.clear();
            if (categorias != null) {
                categoriasList.addAll(categorias);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void mostrarDialogoExclusao(Categoria categoria) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_exclusao_categoria)
                .setMessage(getString(R.string.mensagem_confirmar_exclusao_categoria) + " (" + categoria.getNome() + ")?")
                .setPositiveButton(R.string.sim, (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        categoriaDao.delete(categoria);
                        runOnUiThread(() -> {
                            Toast.makeText(this, R.string.categoria_excluida, Toast.LENGTH_SHORT).show();
                            carregarCategorias();
                        });
                    });
                })
                .setNegativeButton(R.string.nao, null)
                .show();
    }

    private void abrirCadastroCategoria(Categoria categoria) {
        Intent intent = new Intent(this, CategoriaActivity.class);
        if (categoria != null) {
            intent.putExtra("categoria_id", categoria.getId());
        }
        cadastroCategoriaLauncher.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_categoria, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_categoria) {
            abrirCadastroCategoria(null);
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}