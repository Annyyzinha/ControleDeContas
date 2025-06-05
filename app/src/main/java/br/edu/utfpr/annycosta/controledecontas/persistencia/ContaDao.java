package br.edu.utfpr.annycosta.controledecontas.persistencia;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import br.edu.utfpr.annycosta.controledecontas.modelo.Conta;

@Dao
public interface ContaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Conta conta);

    @Update
    void update(Conta conta);

    @Delete
    void delete(Conta conta);

    @Query("SELECT * FROM contas ORDER BY vencimento ASC")
    LiveData<List<Conta>> getAllContas();

    @Query("SELECT * FROM contas WHERE id = :id")
    Conta getContaByIdSync(int id);
}