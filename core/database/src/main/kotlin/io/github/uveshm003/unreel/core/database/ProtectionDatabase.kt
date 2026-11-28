package io.github.uveshm003.unreel.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_protection")
data class AppProtectionRow(
    @PrimaryKey val packageName: String,
    val level: String,
    val dailyBudgetMinutes: Int,
    val allowOneBlockScroll: Boolean,
)

@Dao
abstract class ProtectionDao {
    @Query("SELECT * FROM app_protection")
    abstract fun observe(): Flow<List<AppProtectionRow>>

    @Query("SELECT * FROM app_protection WHERE packageName = :packageName")
    protected abstract suspend fun get(packageName: String): AppProtectionRow?

    @Upsert
    protected abstract suspend fun put(row: AppProtectionRow)

    @Transaction
    open suspend fun edit(packageName: String, transform: (AppProtectionRow?) -> AppProtectionRow) {
        put(transform(get(packageName)))
    }
}

@Database(entities = [AppProtectionRow::class], version = 1, exportSchema = true)
abstract class ProtectionDatabase : RoomDatabase() {
    abstract fun protection(): ProtectionDao

    companion object {
        fun open(context: Context, name: String = "unreel.db"): ProtectionDatabase =
            Room.databaseBuilder(context, ProtectionDatabase::class.java, name)
                .openHelperFactory(PreservePolicyDatabaseFactory())
                .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ProtectionDatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ProtectionDatabase = ProtectionDatabase.open(context)

    @Provides
    fun protectionDao(database: ProtectionDatabase): ProtectionDao = database.protection()
}
