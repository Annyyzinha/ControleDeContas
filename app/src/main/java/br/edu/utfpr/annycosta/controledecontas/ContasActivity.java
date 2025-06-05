package br.edu.utfpr.annycosta.controledecontas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import br.edu.utfpr.annycosta.controledecontas.modelo.Conta;
import br.edu.utfpr.annycosta.controledecontas.persistencia.AppDatabase;
import br.edu.utfpr.annycosta.controledecontas.persistencia.ContaDao;

public class ContasActivity extends AppCompatActivity {
    private List<Conta> contasList = new ArrayList<>();
    private ContaAdapter adapter;
    private ContaDao contaDao;
    private TextView txtListaVazia;
    private ListView listViewContas;

    private boolean isInContextualMode = false;
    private Conta contaSelecionadaParaAcao;

    private final ActivityResultLauncher<Intent> cadastroContaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    carregarContasObservaveis();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contas);

        Toolbar toolbar = findViewById(R.id.toolbar_contas);
        setSupportActionBar(toolbar);

        AppDatabase db = AppDatabase.getDatabase(this);
        contaDao = db.contaDao();

        txtListaVazia = findViewById(R.id.txtListaVazia);
        listViewContas = findViewById(R.id.listViewContas);

        adapter = new ContaAdapter(this, contasList);
        listViewContas.setAdapter(adapter);

        carregarContasObservaveis();

        listViewContas.setOnItemLongClickListener((parent, view, position, id) -> {
            if (isInContextualMode) {
                return false;
            }
            if (position >= 0 && position < contasList.size()) {
                contaSelecionadaParaAcao = contasList.get(position);
                isInContextualMode = true;
                invalidateOptionsMenu();
                view.setSelected(true);
            }
            return true;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void exitContextualMode() {
        isInContextualMode = false;
        contaSelecionadaParaAcao = null;
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contas, menu);
        getMenuInflater().inflate(R.menu.menu_contextual, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuAdicionar = menu.findItem(R.id.menu_adicionar);
        MenuItem menuListarCategorias = menu.findItem(R.id.menu_listar_categorias);
        MenuItem menuSettings = menu.findItem(R.id.menu_settings);
        MenuItem menuSobre = menu.findItem(R.id.menu_sobre);

        MenuItem menuEditarCab = menu.findItem(R.id.menu_editar);
        MenuItem menuExcluirCab = menu.findItem(R.id.menu_excluir);

        if (isInContextualMode) {
            if (menuAdicionar != null) menuAdicionar.setVisible(false);
            if (menuListarCategorias != null) menuListarCategorias.setVisible(false);
            if (menuSettings != null) menuSettings.setVisible(false);
            if (menuSobre != null) menuSobre.setVisible(false);

            if (menuEditarCab != null) menuEditarCab.setVisible(true);
            if (menuExcluirCab != null) menuExcluirCab.setVisible(true);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(contaSelecionadaParaAcao != null ? contaSelecionadaParaAcao.getNome() : getString(R.string.menu_acoes_conta));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        } else {
            if (menuAdicionar != null) menuAdicionar.setVisible(true);
            if (menuListarCategorias != null) menuListarCategorias.setVisible(true);
            if (menuSettings != null) menuSettings.setVisible(true);
            if (menuSobre != null) menuSobre.setVisible(true);

            if (menuEditarCab != null) menuEditarCab.setVisible(false);
            if (menuExcluirCab != null) menuExcluirCab.setVisible(false);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.app_name));
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            if (isInContextualMode) {
                exitContextualMode();
                return true;
            }
        } else if (itemId == R.id.menu_adicionar) {
            abrirCadastroConta(null);
            return true;
        } else if (itemId == R.id.menu_listar_categorias) {
            startActivity(new Intent(this, CategoriasActivity.class));
            return true;
        } else if (itemId == R.id.menu_settings) {
            startActivity(new Intent(this, ConfiguracaoActivity.class));
            return true;
        } else if (itemId == R.id.menu_sobre) {
            startActivity(new Intent(this, SobreActivity.class));
            return true;
        } else if (itemId == R.id.menu_editar) {
            if (contaSelecionadaParaAcao != null) {
                abrirCadastroConta(contaSelecionadaParaAcao);
            }
            exitContextualMode();
            return true;
        } else if (itemId == R.id.menu_excluir) {
            if (contaSelecionadaParaAcao != null) {
                confirmarExclusaoConta(contaSelecionadaParaAcao);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isInContextualMode) {
            exitContextualMode();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        carregarContasObservaveis();
        if (isInContextualMode) {
            exitContextualMode();
        }
    }

    private void carregarContasObservaveis() {
        contaDao.getAllContas().observe(this, contas -> {
            contasList.clear();
            if (contas != null) {
                contasList.addAll(contas);
            }
            adapter.notifyDataSetChanged();
            atualizarListaVazia();
        });
    }

    private void confirmarExclusaoConta(final Conta conta) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_exclusao)
                .setMessage(getString(R.string.mensagem_confirmar_exclusao) + " (" + conta.getNome() + ")?")
                .setPositiveButton(R.string.sim, (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        contaDao.delete(conta);
                        runOnUiThread(() -> {
                            Toast.makeText(this,R.string.msg_conta_excluida, Toast.LENGTH_SHORT).show();
                        });
                    });
                    exitContextualMode();
                })
                .setNegativeButton(R.string.nao, (dialog, which) -> {
                    exitContextualMode();
                })
                .setOnCancelListener(dialog -> {
                    exitContextualMode();
                })
                .show();
    }


    private void abrirCadastroConta(Conta conta) {
        Intent intent = new Intent(this, ContaActivity.class);
        if (conta != null) {
            intent.putExtra("conta_id", conta.getId());
        }
        cadastroContaLauncher.launch(intent);
    }

    private void atualizarListaVazia() {
        if (contasList.isEmpty()) {
            txtListaVazia.setVisibility(View.VISIBLE);
            listViewContas.setVisibility(View.GONE);
        } else {
            txtListaVazia.setVisibility(View.GONE);
            listViewContas.setVisibility(View.VISIBLE);
        }
    }

    private static class ViewHolder {
        TextView textViewConta;
        int corOriginalTexto;
    }

    static class ContaAdapter extends ArrayAdapter<Conta> {

        ContaAdapter(AppCompatActivity context, List<Conta> contas) {
            super(context, 0, contas);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            ViewHolder viewHolder;

            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_conta, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textViewConta = listItemView.findViewById(R.id.textViewConta);
                viewHolder.corOriginalTexto = viewHolder.textViewConta.getCurrentTextColor();
                listItemView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) listItemView.getTag();
            }

            Conta currentConta = getItem(position);

            if (currentConta != null) {
                String status = currentConta.isPaga() ? getContext().getString(R.string.status_paga) : getContext().getString(R.string.status_em_aberto);
                String lembreteStatus = currentConta.isLembreteAtivo() ? getContext().getString(R.string.lembrete_ativo) : getContext().getString(R.string.lembrete_inativo);
                String vencimentoFormatado = currentConta.getVencimentoFormatado();

                viewHolder.textViewConta.setText(getContext().getString(R.string.conta_formatada,
                        currentConta.getNome(),
                        currentConta.getValorFormatado(),
                        vencimentoFormatado,
                        status,
                        lembreteStatus));

                if (currentConta.isAtrasada()) {
                    viewHolder.textViewConta.setTextColor(ContextCompat.getColor(getContext(), R.color.vermelho_atraso));
                } else {
                    viewHolder.textViewConta.setTextColor(viewHolder.corOriginalTexto);
                }
            }
            return listItemView;
        }
    }
}