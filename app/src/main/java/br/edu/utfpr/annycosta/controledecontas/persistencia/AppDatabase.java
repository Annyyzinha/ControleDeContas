package br.edu.utfpr.annycosta.controledecontas.persistencia;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.utfpr.annycosta.controledecontas.modelo.Categoria;
import br.edu.utfpr.annycosta.controledecontas.modelo.Conta;

@Database(entities = {Conta.class, Categoria.class}, version = 5, exportSchema = false)
@TypeConverters({LocalDateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContaDao contaDao();
    public abstract CategoriaDao categoriaDao();

    private static volatile AppDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "contas_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}