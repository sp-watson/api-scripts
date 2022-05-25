package rpmigration

import fileaccess.PerOffenderFileOutput

data class SingleTestMigrationRequestEvent (
    val offenderNo: String,
    val hospitalNomsId: String,
)

class SingleTestMigration (
    private val progressStream: PerOffenderFileOutput,
    private val migrateOffenderCommand: MigrateOffender,
) {
    fun handle(command: SingleTestMigrationRequestEvent) {
        val progressStreamRef = progressStream.migrationStarted(command.offenderNo)!!
        println("Moving ${command.offenderNo}")

        try {
            val migrationData = MigrateOffenderRequestEvent(command.offenderNo, command.hospitalNomsId)
            migrateOffenderCommand.handle(migrationData)
            println("Moved")
            return
        } catch (th: Throwable) {
            progressStream.migrationFailed(progressStreamRef, th)
        }

        println("Move failed")
    }
}