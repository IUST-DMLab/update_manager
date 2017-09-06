package ir.ac.iust.dml.kg.search.feedback.logic

import com.google.gson.Gson
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.search.feedback.access.entities.UpdateTask
import ir.ac.iust.dml.kg.search.feedback.access.repositories.UpdateTaskRepository
import ir.ac.iust.dml.kg.search.feedback.logic.data.Config
import ir.ac.iust.dml.kg.search.feedback.logic.data.UpdateInfo
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Paths
import javax.annotation.PostConstruct

@Service
class DirectoryScanner {
  private val LOGGER = Logger.getLogger(this.javaClass)!!
  private val semaphore = "Semaphore"
  private var working = false
  private var gson = Gson()
  private lateinit var config: Config
  private val scanRegex = Regex("info\\.json")
  @Autowired lateinit var repository: UpdateTaskRepository

  @PostConstruct
  fun loadConfig() {
    config = ConfigReader.readConfigObject("scan_config.json", Config::class.java)
    config.scanAddresses.forEach { it.path = Paths.get(it.address!!) }
  }

  @Scheduled(fixedRate = 5000)
  fun reportCurrentTime() {
    synchronized(semaphore) {
      if (working) return
      working = true
    }
    LOGGER.trace("i am alive! $config")
    config.scanAddresses.forEach {
      pathScan@ PathWalker.getPath(it.path!!, scanRegex, it.depth!!).forEach {
        val absolutePath = it.toAbsolutePath().toString()
        var update = repository.findByPath(absolutePath)
//        if (update != null) return@pathScan
        LOGGER.info("I have found a new update at $absolutePath")
        try {
          val info = ConfigReader.readJson(it, UpdateInfo::class)
          LOGGER.info("content is $info")
        } catch (th: Throwable) {
          LOGGER.error(th)
        }
        update = UpdateTask()
      }
    }
    synchronized(semaphore) {
      working = false
    }
  }
}