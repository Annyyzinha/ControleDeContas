package br.edu.utfpr.annycosta.controledecontas.persistencia;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import br.edu.utfpr.annycosta.controledecontas.modelo.Categoria;

@Dao
public interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Categoria categoria);

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    LiveData<List<Categoria>> getAllCategorias();

    @Query("SELECT * FROM categorias WHERE id = :id")
    Categoria getCategoriaByIdSync(int id);

    @Query("SELECT * FROM categorias WHERE nome = :nomeCategoria COLLATE NOCASE LIMIT 1")
    Categoria getCategoriaByNomeSync(String nomeCategoria);
}