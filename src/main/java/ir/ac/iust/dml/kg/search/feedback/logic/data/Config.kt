package ir.ac.iust.dml.kg.search.feedback.logic.data

import java.nio.file.Path

data class ScanAddress(var address: String? = null, var depth: Int? = null, var path: Path? = null)
data class Config(var scanAddresses: MutableList<ScanAddress> = mutableListOf())