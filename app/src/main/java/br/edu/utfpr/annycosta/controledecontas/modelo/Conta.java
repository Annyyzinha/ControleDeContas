package br.edu.utfpr.annycosta.controledecontas.modelo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import br.edu.utfpr.annycosta.controledecontas.persistencia.LocalDateConverter;

@Entity(tableName = "contas",
        indices = {@Index(value = {"nome"}, unique = true)},
        foreignKeys = @ForeignKey(entity = Categoria.class,
                parentColumns = "id",
                childColumns = "categoria_id",
                onDelete = ForeignKey.SET_NULL))
@TypeConverters(LocalDateConverter.class)
public class Conta implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "valor")
    private double valor;

    @ColumnInfo(name = "vencimento")
    private LocalDate vencimento;

    @ColumnInfo(name = "categoria_id", index = true)
    private Integer categoriaId;

    @ColumnInfo(name = "paga")
    private boolean paga;

    @ColumnInfo(name = "lembrete_ativo")
    private boolean lembreteAtivo;

    public Conta() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public LocalDate getVencimento() { return vencimento; }
    public void setVencimento(LocalDate vencimento) { this.vencimento = vencimento; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public boolean isPaga() { return paga; }
    public void setPaga(boolean paga) { this.paga = paga; }
    public boolean isLembreteAtivo() { return lembreteAtivo; }
    public void setLembreteAtivo(boolean lembreteAtivo) { this.lembreteAtivo = lembreteAtivo; }

    public String getValorFormatado() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    public String getVencimentoFormatado() {
        if (vencimento == null) return "";
        Locale currentLocale = Locale.getDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(currentLocale);
        return vencimento.format(formatter);
    }

    @Ignore
    public boolean isAtrasada() {
        return !isPaga() && getVencimento() != null && getVencimento().isBefore(LocalDate.now());
    }

    public static class Builder {
        private String nome;
        private double valor;
        private LocalDate vencimento;
        private Integer categoriaId;
        private boolean paga = false;
        private boolean lembreteAtivo = false;
    }
}