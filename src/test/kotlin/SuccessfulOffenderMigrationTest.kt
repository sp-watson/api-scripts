import fileaccess.SuccessfulOffenderMigrations
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class SuccessfulOffenderMigrationTest {
    private val EXAMPLE_MIGRATIONS_FILE = "migrations.txt"
    private lateinit var baseDirectory: String

    @BeforeEach
    fun setUp() {
        // Make empty file
        baseDirectory = getDirectoryName("successfulOffenderMigrationTest")
        val tempFilename = Path.of(baseDirectory, EXAMPLE_MIGRATIONS_FILE).absolutePathString()
        File(tempFilename).writeText("")
    }

    @Test
    fun `should read back all added offenders when done in parallel`() {
        val migrationFile = SuccessfulOffenderMigrations("", "example.txt")
        val exampleOffenders = listOf("A1234AA", "B2345BB", "C3456CC")
        runBlocking {
            exampleOffenders.forEach {
                launch {
                    println("Adding offender: $it")
                    migrationFile.offenderMigrated(it)
                    println("Added offender: $it")
                }
            }
        }
        Assertions.assertThat(migrationFile.getMigratedOffenders()).containsAll(exampleOffenders)
    }

    @AfterEach
    fun tearDown() {
        // Remove file
        val tempFilename = Path.of(baseDirectory, EXAMPLE_MIGRATIONS_FILE).absolutePathString()
        if (!File(tempFilename).delete()) {
            throw RuntimeException("Deletion failed - you will have to delete manually")
        }
    }

    private fun getDirectoryName(resourceDirectoryName: String): String {
        return javaClass.getResource("/$resourceDirectoryName").file
    }
}