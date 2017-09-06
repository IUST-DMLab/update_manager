package ir.ac.iust.dml.kg.search.feedback.logic

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.search.feedback.access.entities.UpdateTask
import ir.ac.iust.dml.kg.search.feedback.access.repositories.UpdateTaskRepository
import ir.ac.iust.dml.kg.services.client.runner.ApiClient
import ir.ac.iust.dml.kg.services.client.runner.swagger.V1definitionsApi
import ir.ac.iust.dml.kg.services.client.runner.swagger.V1runApi
import ir.ac.iust.dml.kg.services.client.runner.swagger.model.CommandLineData
import ir.ac.iust.dml.kg.services.client.runner.swagger.model.DefinitionData
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class UpdateLogic {

  val runApi: V1runApi
  val runDefinitionApi: V1definitionsApi
  val mapperAddress = ConfigReader.getPath("executable.mapper", "~/pkg/codes/mapper/runner/target")

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.runner.url", "http://localhost:8102/rs")
    client.connectTimeout = 4800000
    runApi = V1runApi(client)
    runDefinitionApi = V1definitionsApi(client)
  }

  fun addRun(definition: DefinitionData, title: String, vararg commands: String) {
    definition.title = title
    definition.maxTryCount = 5
    definition.maxTryDuration = 24 * 3600 * 1000L
    val commandListData = CommandLineData()
    commandListData.command = "java"
    commandListData.arguments.addAll(commands)
    commandListData.workingDirectory = mapperAddress.toAbsolutePath().toString()
    definition.commands = mutableListOf(commandListData)
    runDefinitionApi.insert1(definition)
  }

  @PostConstruct
  fun createRuns() {
    val map = mutableMapOf<String, DefinitionData>()
    runDefinitionApi.all1().forEach { map[it.title] = it }
    val wikiDefinition = map.getOrDefault("wiki", DefinitionData())
    addRun(wikiDefinition, "wiki", "-jar", "mapper.jar", "completeDump", "knowledgeStore")
    val tableDefinition = map.getOrDefault("tables", DefinitionData())
    addRun(tableDefinition, "wiki", "-jar", "mapper.jar", "tables", "knowledgeStore")
    val rawDefinition = map.getOrDefault("raw", DefinitionData())
    addRun(rawDefinition, "wiki", "-jar", "mapper.jar", "raw", "knowledgeStore")
  }

  private val LOGGER = Logger.getLogger(this.javaClass)!!
  @Autowired lateinit var repository: UpdateTaskRepository

  fun search(page: Int, pageSize: Int, module: String?, path: String?,
             minStartDate: Long?, maxStartDate: Long?,
             minEndDate: Long?, maxEndDate: Long?): Page<UpdateTask> {
    return repository.search(page, pageSize, module, path,
        minStartDate, maxStartDate, minEndDate, maxEndDate)
  }
}