package demo

object S120_Business_goals:
  def mainGoal: String = "Rapid development of maintainable web services"

  def subGoals: List[String] = List(
    "Verify properties of services at compile-time",
    "Provide guidance for the IDE",
    "Generate OpenAPI/Swagger documentation",
    "Reuse code when possible",
    "Integrate with a stack chosen by the developer",
    "Increase developer experience",
    "Provide observability hooks"
  )
