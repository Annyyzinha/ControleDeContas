package br.edu.utfpr.annycosta.controledecontas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import br.edu.utfpr.annycosta.controledecontas.modelo.Categoria;
import br.edu.utfpr.annycosta.controledecontas.persistencia.AppDatabase;
import br.edu.utfpr.annycosta.controledecontas.persistencia.CategoriaDao;

public class CategoriaActivity extends AppCompatActivity {
    private EditText editTextNomeCategoria;
    private Categoria categoriaEmEdicao;
    private CategoriaDao categoriaDao;
    private int categoriaIdEmEdicao = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria);

        Toolbar toolbar = findViewById(R.id.toolbar_categoria);
        setSupportActionBar(toolbar);

        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoria);
        AppDatabase db = AppDatabase.getDatabase(this);
        categoriaDao = db.categoriaDao();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.cadastro_categoria));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("categoria_id")) {
            categoriaIdEmEdicao = intent.getIntExtra("categoria_id", -1);
            if (categoriaIdEmEdicao != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    Categoria cat = categoriaDao.getCategoriaByIdSync(categoriaIdEmEdicao);
                    runOnUiThread(() -> {
                        if (cat != null) {
                            categoriaEmEdicao = cat;
                            editTextNomeCategoria.setText(categoriaEmEdicao.getNome());
                        }
                    });
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_salvar) {
            salvarCategoria();
            return true;
        } else if (itemId == R.id.menu_limpar) {
            limparFormulario();
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void salvarCategoria() {
        String nome = editTextNomeCategoria.getText().toString().trim();
        editTextNomeCategoria.setError(null);

        if (nome.isEmpty()) {
            editTextNomeCategoria.setError(getString(R.string.error_nome_obrigatorio));
            editTextNomeCategoria.requestFocus();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Categoria categoriaExistenteComMesmoNome = categoriaDao.getCategoriaByNomeSync(nome);
            if (categoriaExistenteComMesmoNome != null && (categoriaEmEdicao == null || categoriaExistenteComMesmoNome.getId() != categoriaEmEdicao.getId())) {
                runOnUiThread(() -> {
                    editTextNomeCategoria.setError(getString(R.string.error_categoria_ja_existe));
                    editTextNomeCategoria.requestFocus();
                });
                return;
            }

            Categoria categoriaParaSalvar;
            if (categoriaEmEdicao != null) {
                categoriaParaSalvar = categoriaEmEdicao;
                categoriaParaSalvar.setNome(nome);
                categoriaDao.update(categoriaParaSalvar);
            } else {
                categoriaParaSalvar = new Categoria();
                categoriaParaSalvar.setNome(nome);
                categoriaDao.insert(categoriaParaSalvar);
            }
            runOnUiThread(() -> {
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void limparFormulario() {
        editTextNomeCategoria.setText("");
        editTextNomeCategoria.setError(null);
        categoriaEmEdicao = null;
        categoriaIdEmEdicao = -1;
        editTextNomeCategoria.requestFocus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}