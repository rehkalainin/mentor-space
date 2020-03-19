package com.andersenlab.mentorspace

import java.util.UUID
import Employee._

case class Employee (id:UUID, name:Name, lastName:LastName, department:DepartmentType, category:CategoryType)
object Employee {
  case class Name(value:String) extends AnyVal
  case class LastName(value:String) extends AnyVal

  sealed trait DepartmentType
  object DepartmentType {
    case object Scala extends DepartmentType
    case object Java extends DepartmentType
    case object PHP extends DepartmentType
    case object Python extends DepartmentType
    case object CSharp extends DepartmentType
  }

  sealed trait CategoryType
  object CategoryType{
    case object Trainee extends CategoryType
    case object J1 extends CategoryType
    case object J2 extends CategoryType
    case object J3 extends CategoryType
    case object M1 extends CategoryType
    case object M2 extends CategoryType
    case object S extends CategoryType
  }

  def apply(name: Name, lastName: LastName, department: DepartmentType, category: CategoryType): Employee =
    Employee(UUID.randomUUID(), name, lastName, department, category)
}


