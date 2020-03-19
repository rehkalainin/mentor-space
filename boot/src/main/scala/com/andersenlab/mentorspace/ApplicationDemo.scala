package com.andersenlab.mentorspace

import java.util.UUID

import Employee._


object ApplicationDemo extends App {

  val andrey = Employee(UUID.randomUUID(), Name("Andrey"), LastName("Ivanov"), DepartmentType.Scala, CategoryType.J1)
  val jim = Employee(Name("Jim"), LastName("Beam"), DepartmentType.Java, CategoryType.J2)
  val kos = Employee(Name("Kos"), LastName("Petrov"), DepartmentType.Scala, CategoryType.M2 )

  println(andrey)
  println(jim)
  println(kos)
}
