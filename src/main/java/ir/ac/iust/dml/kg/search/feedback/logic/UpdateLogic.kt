package ir.ac.iust.dml.kg.search.feedback.logic

import ir.ac.iust.dml.kg.search.feedback.access.entities.UpdateTask
import ir.ac.iust.dml.kg.search.feedback.access.repositories.UpdateTaskRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class UpdateLogic {

  private val LOGGER = Logger.getLogger(this.javaClass)!!
  @Autowired lateinit var repository: UpdateTaskRepository

  fun search(page: Int, pageSize: Int, module: String?, path: String?,
             minStartDate: Long?, maxStartDate: Long?,
             minEndDate: Long?, maxEndDate: Long?): Page<UpdateTask> {
    return repository.search(page, pageSize, module, path,
        minStartDate, maxStartDate, minEndDate, maxEndDate)
  }
}