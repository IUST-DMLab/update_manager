package ir.ac.iust.dml.kg.search.feedback.web.commons.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class WelcomeController {
  @RequestMapping(path = arrayOf("/", ""))
  fun index() = """
    Greetings from Spring Boot!
    <br/>
    Select one of these options:
    <br/>
    <a href='/swagger-ui.html'>All public services</a>
    """
}