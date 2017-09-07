package ir.ac.iust.dml.kg.search.feedback.logic

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.search.feedback.access.entities.UpdateTask
import ir.ac.iust.dml.kg.search.feedback.access.repositories.UpdateTaskRepository
import ir.ac.iust.dml.kg.search.feedback.logic.data.UpdateInfo
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DirectoryScanner {
  private val LOGGER = Logger.getLogger(this.javaClass)!!
  private val semaphore = "Semaphore"
  private var working = false
  private val scanRegex = Regex("info\\.json")
  @Autowired lateinit var repository: UpdateTaskRepository
  @Autowired lateinit var settings: UpdateConfigurator
  lateinit var updateRunner: UpdateRunner

  @Scheduled(fixedRate = 5000)
  fun reportCurrentTime() {
    synchronized(semaphore) {
      if (working) return
      working = true
    }
    for (@Suppress("Destructure") address in settings.scanAddresses) {
      val scanned = PathWalker.getPath(address.path!!, scanRegex, address.depth!!)
      scanned.forEach {
        try {
          val absolutePath: String = it.toAbsolutePath().toString()
          var update = repository.findByPath(absolutePath)
          val info = ConfigReader.readJson(it, UpdateInfo::class.java)
          if (info.extractionStart == null || info.extractionEnd == null || info.module == null)
            return@forEach
          if (update != null && update.startTime == info.extractionStart && update.endTime == info.extractionEnd)
            return@forEach
          LOGGER.info("I have found a new update at $absolutePath")
          LOGGER.info("content is $info")
          if (update == null) update = UpdateTask()
          update.startTime = info.extractionStart
          update.endTime = info.extractionEnd
          update.module = info.module
          update.path = it.toAbsolutePath().toString()
          update.runnerId = updateRunner.run(update.module!!)
          if (update.runnerId != null)
            repository.save(update)
        } catch (th: Throwable) {
          LOGGER.error(th)
        }
      }
    }
    synchronized(semaphore) {
      working = false
    }
  }
}