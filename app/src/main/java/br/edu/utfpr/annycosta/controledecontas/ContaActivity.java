package br.edu.utfpr.annycosta.controledecontas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import br.edu.utfpr.annycosta.controledecontas.modelo.Categoria;
import br.edu.utfpr.annycosta.controledecontas.modelo.Conta;
import br.edu.utfpr.annycosta.controledecontas.persistencia.AppDatabase;
import br.edu.utfpr.annycosta.controledecontas.persistencia.CategoriaDao;
import br.edu.utfpr.annycosta.controledecontas.persistencia.ContaDao;

public class ContaActivity extends AppCompatActivity {
    private EditText editTextNomeConta, editTextValor, editTextVencimento;
    private RadioGroup radioGroupStatus;
    private RadioButton radioPaga, radioEmAberto;
    private Spinner spinnerCategoria;
    private CheckBox checkBoxLembrete;

    private ContaDao contaDao;
    private CategoriaDao categoriaDao;
    private Conta contaEmEdicao;
    private int contaIdEmEdicao = -1;

    private ArrayAdapter<Categoria> categoriasSpinnerAdapter;
    private List<Categoria> listaCategoriasSpinner = new ArrayList<>();
    private LocalDate dataVencimentoSelecionada;
    private Integer categoriaIdParaSelecionarAposCarregamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conta);

        Toolbar toolbar = findViewById(R.id.toolbar_conta);
        setSupportActionBar(toolbar);

        AppDatabase db = AppDatabase.getDatabase(this.getApplicationContext());
        contaDao = db.contaDao();
        categoriaDao = db.categoriaDao();

        editTextNomeConta = findViewById(R.id.editTextNomeConta);
        editTextValor = findViewById(R.id.editTextValor);
        editTextVencimento = findViewById(R.id.editTextVencimento);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        radioPaga = findViewById(R.id.radioPaga);
        radioEmAberto = findViewById(R.id.radioEmAberto);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        checkBoxLembrete = findViewById(R.id.checkBoxLembrete);

        setupDatePicker();
        setupCategoriaSpinner();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.cadastro_conta));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("conta_id")) {
            contaIdEmEdicao = intent.getIntExtra("conta_id", -1);
            if (contaIdEmEdicao != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    Conta c = contaDao.getContaByIdSync(contaIdEmEdicao);
                    runOnUiThread(() -> {
                        if (c != null) {
                            contaEmEdicao = c;
                            carregarDadosConta(contaEmEdicao);
                        }
                    });
                });
            }
        } else {
            carregarDefaultLembrete();
            radioEmAberto.setChecked(true);
        }
    }

    private void setupDatePicker() {
        editTextVencimento.setOnClickListener(v -> mostrarDatePicker());
        editTextVencimento.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarDatePicker();
        });
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        if (dataVencimentoSelecionada != null) {
            calendario.set(dataVencimentoSelecionada.getYear(), dataVencimentoSelecionada.getMonthValue() -1, dataVencimentoSelecionada.getDayOfMonth());
        }

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    dataVencimentoSelecionada = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
                    editTextVencimento.setText(dataVencimentoSelecionada.format(formatter));
                    editTextVencimento.setError(null);
                }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void setupCategoriaSpinner() {
        categoriasSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                listaCategoriasSpinner);
        categoriasSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriasSpinnerAdapter);

        categoriaDao.getAllCategorias().observe(this, categorias -> {
            listaCategoriasSpinner.clear();
            Categoria prompt = new Categoria(getString(R.string.selecione));
            prompt.setId(0);
            listaCategoriasSpinner.add(prompt);

            if (categorias != null) {
                listaCategoriasSpinner.addAll(categorias);
            }
            categoriasSpinnerAdapter.notifyDataSetChanged();

            if (listaCategoriasSpinner.size() == 1 && categorias.isEmpty()) {
                Toast.makeText(this, R.string.aviso_nenhuma_categoria_cadastrada, Toast.LENGTH_LONG).show();
            }

            if (categoriaIdParaSelecionarAposCarregamento != null) {
                selecionarCategoriaNoSpinnerPorId(categoriaIdParaSelecionarAposCarregamento);
                categoriaIdParaSelecionarAposCarregamento = null;
            } else if (contaEmEdicao == null) {
                spinnerCategoria.setSelection(0);
            }
        });
    }

    private void selecionarCategoriaNoSpinnerPorId(Integer categoriaId) {
        if (categoriaId == null) {
            spinnerCategoria.setSelection(0);
            return;
        }
        for (int i = 0; i < listaCategoriasSpinner.size(); i++) {
            if (listaCategoriasSpinner.get(i).getId() == categoriaId) {
                spinnerCategoria.setSelection(i);
                return;
            }
        }
        spinnerCategoria.setSelection(0);
    }

    private void carregarDadosConta(Conta conta) {
        editTextNomeConta.setText(conta.getNome());
        editTextValor.setText(String.format(Locale.US, "%.2f", conta.getValor()));

        if (conta.getVencimento() != null) {
            dataVencimentoSelecionada = conta.getVencimento();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            editTextVencimento.setText(dataVencimentoSelecionada.format(formatter));
        } else {
            editTextVencimento.setText("");
        }

        if (conta.isPaga()) radioPaga.setChecked(true);
        else radioEmAberto.setChecked(true);

        checkBoxLembrete.setChecked(conta.isLembreteAtivo());

        if (listaCategoriasSpinner.isEmpty() || listaCategoriasSpinner.size() <=1 && listaCategoriasSpinner.get(0).getId() == 0) {
            categoriaIdParaSelecionarAposCarregamento = conta.getCategoriaId();
        } else {
            selecionarCategoriaNoSpinnerPorId(conta.getCategoriaId());
        }
    }

    private void carregarDefaultLembrete() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean defaultReminder = preferences.getBoolean(getString(R.string.pref_default_reminder_key), false);
        checkBoxLembrete.setChecked(defaultReminder);
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
            salvarConta();
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

    private void salvarConta() {
        String nome = editTextNomeConta.getText().toString().trim();
        String valorStr = editTextValor.getText().toString().replace(",", ".");

        editTextNomeConta.setError(null);
        editTextValor.setError(null);
        editTextVencimento.setError(null);
        View spinnerSelectedView = spinnerCategoria.getSelectedView();
        if (spinnerSelectedView instanceof TextView) {
            ((TextView) spinnerSelectedView).setError(null);
        }

        boolean eValido = true;
        View primeiroCampoInvalido = null;

        if (TextUtils.isEmpty(nome)) {
            editTextNomeConta.setError(getString(R.string.error_informe_nome));
            if (primeiroCampoInvalido == null) primeiroCampoInvalido = editTextNomeConta;
            eValido = false;
        }

        if (TextUtils.isEmpty(valorStr)) {
            editTextValor.setError(getString(R.string.error_informe_valor));
            if (primeiroCampoInvalido == null) primeiroCampoInvalido = editTextValor;
            eValido = false;
        } else {
            try {
                double valor = Double.parseDouble(valorStr);
                if (valor <= 0) {
                    editTextValor.setError(getString(R.string.error_valor_positivo));
                    if (primeiroCampoInvalido == null) primeiroCampoInvalido = editTextValor;
                    eValido = false;
                }
            } catch (NumberFormatException e) {
                editTextValor.setError(getString(R.string.error_valor_invalido));
                if (primeiroCampoInvalido == null) primeiroCampoInvalido = editTextValor;
                eValido = false;
            }
        }

        if (dataVencimentoSelecionada == null) {
            editTextVencimento.setError(getString(R.string.error_informe_data));
            if (primeiroCampoInvalido == null) primeiroCampoInvalido = editTextVencimento;
            eValido = false;
        }

        int selectedCategoriaPos = spinnerCategoria.getSelectedItemPosition();
        if (selectedCategoriaPos == Spinner.INVALID_POSITION || selectedCategoriaPos == 0 ) {
            if (listaCategoriasSpinner.size() <=1 ) {
                Toast.makeText(this, R.string.aviso_cadastre_uma_categoria_primeiro, Toast.LENGTH_LONG).show();
            } else {
                if (spinnerSelectedView instanceof TextView) {
                    ((TextView) spinnerSelectedView).setError(getString(R.string.error_selecione_categoria));
                } else {
                    Toast.makeText(this, R.string.error_selecione_categoria, Toast.LENGTH_SHORT).show();
                }
            }
            if (primeiroCampoInvalido == null && spinnerSelectedView !=null) primeiroCampoInvalido = spinnerCategoria;
            eValido = false;
        }


        if (radioGroupStatus.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.error_selecione_status, Toast.LENGTH_SHORT).show();
            eValido = false;
        }

        if (!eValido) {
            if (primeiroCampoInvalido != null && !(primeiroCampoInvalido instanceof Spinner)) {
                primeiroCampoInvalido.requestFocus();
            }
            return;
        }

        double valor = Double.parseDouble(valorStr);
        boolean paga = radioPaga.isChecked();
        boolean lembreteAtivo = checkBoxLembrete.isChecked();
        Categoria categoriaSelecionada = listaCategoriasSpinner.get(selectedCategoriaPos);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Conta contaParaSalvar;
            if (contaEmEdicao != null) {
                contaParaSalvar = contaEmEdicao;
            } else {
                contaParaSalvar = new Conta();
            }

            contaParaSalvar.setNome(nome);
            contaParaSalvar.setValor(valor);
            contaParaSalvar.setVencimento(dataVencimentoSelecionada);
            contaParaSalvar.setCategoriaId(categoriaSelecionada.getId());
            contaParaSalvar.setPaga(paga);
            contaParaSalvar.setLembreteAtivo(lembreteAtivo);

            if (contaIdEmEdicao != -1) {
                contaDao.update(contaParaSalvar);
            } else {
                contaDao.insert(contaParaSalvar);
            }
            runOnUiThread(() -> {
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void limparFormulario() {
        editTextNomeConta.setText("");
        editTextNomeConta.setError(null);
        editTextValor.setText("");
        editTextValor.setError(null);
        editTextVencimento.setText("");
        editTextVencimento.setError(null);
        dataVencimentoSelecionada = null;
        radioGroupStatus.clearCheck();
        radioEmAberto.setChecked(true);
        spinnerCategoria.setSelection(0);
        View spinnerSelectedView = spinnerCategoria.getSelectedView();
        if (spinnerSelectedView instanceof TextView) {
            ((TextView) spinnerSelectedView).setError(null);
        }
        carregarDefaultLembrete();
        contaEmEdicao = null;
        contaIdEmEdicao = -1;
        categoriaIdParaSelecionarAposCarregamento = null;
        editTextNomeConta.requestFocus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}