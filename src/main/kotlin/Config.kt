interface Config {
    val removingExistingRestrictedPatient: Boolean
    val prisonApiRootUrl: String
    val restrictedPatientsApiRootUrl: String
    val token: String
    val spreadsheetFileName: String
    val resultsBaseDirectory: String
}
