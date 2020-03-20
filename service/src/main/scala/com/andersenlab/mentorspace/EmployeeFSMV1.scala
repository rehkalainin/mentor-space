package com.andersenlab.mentorspace

import akka.actor.{ActorLogging, FSM}
import Employee._

object EmployeeFSMV1 {
  sealed trait State
  object State{
    case object Idle extends State
    case object Recruitment extends State
    case object EmployeeHired extends State
    case object EmployeeStudyStarted extends State
    case object EmployeeLabStarted extends State
    case object EmployeeGraduated extends State
    case object EmployeeWorkStarted extends State
    case object EmployeeMentorAvailable extends State
    case object EmployeeMentorFilled extends State
    case object EmployeeQuit extends State
  }

  sealed trait Data
  object Data{
    case object Uninitialized extends Data
    case class Initialized(staff:Set[Employee],mentorStore: Map[Employee,Set[Employee]]) extends Data
    case class EmployeeData(employee: Employee, staff: Set[Employee],mentorStore: Map[Employee,Set[Employee]])extends Data
    case class EmployeeDataMentoring(padavan: Employee, mentor:Employee, staff: Set[Employee], mentorStore:Map[Employee,Set[Employee]]) extends Data

  }
  object Command{
    case class Initialize(staff:Set[Employee],mentorStore: Map[Employee,Set[Employee]])
    case class HireEmployee(employee: Employee)
    case class Welcome(employee: Employee)
    case object Study
    case object IncreaseJ1
    case object Work
    case object IncreaseMentor
    case object InitMentor
    case class ReceiveMentor(mentor:Employee)
    case class RemoveMentor(mentor:Employee)
    case class ReceivePadavan(padavan:Employee)
    case class RemovePadavan(padavan:Employee)
    case object FireEmployee
    case object Quit
    case object ExamFail
    case object ExamPass
    case class Comments(comments : String)
    case class EmployeeError(reason:String)
  }

}
class EmployeeFSMV1 extends FSM[EmployeeFSMV1.State, EmployeeFSMV1.Data] with ActorLogging {

  import EmployeeFSMV1._

  startWith(State.Idle, Data.Uninitialized)

  when(State.Idle) {
    case Event(Command.Initialize(staff,mentorStore), Data.Uninitialized) =>
      sender() ! Command.Comments("FSM has initialized")
      goto(State.Recruitment) using Data.Initialized(staff,mentorStore)
    case _ =>
      stay() replying Command.EmployeeError("EmployeeFSM hasn't initialized")
  }
  when(State.Recruitment) {
    case Event(Command.HireEmployee(employee), Data.Initialized(staff,mentorStore)) => {
      if (!staff.contains(employee)) {
        val newStaff = staff + employee
        sender() ! Command.Comments("Staff updated and employee waiting Welcome")
        stay() using Data.Initialized(newStaff,mentorStore)
      } else {
        stay() replying Command.EmployeeError("Can't hired one employee twice")
      }
    }
    case Event(Command.Welcome(employee), Data.Initialized(staff,mentorStore)) =>
      if (staff.contains(employee)) {
        goto(State.EmployeeHired) using Data.EmployeeData(employee, staff,mentorStore)
      } else {
        stay() replying Command.EmployeeError("Impossible Welcome absent employee")
      }
  }
  when(State.EmployeeHired) {
    case Event(Command.Work, Data.EmployeeData(employee, staff, mentorStore)) if employee.category != CategoryType.Trainee =>
      sender() ! Command.Comments("Employee goto Work")
      goto(State.EmployeeWorkStarted)

    case Event(Command.Study, Data.EmployeeData(employee, staff, mentorStore)) if employee.category == CategoryType.Trainee =>
      sender() ! Command.Comments("Employee goto Study")
      goto(State.EmployeeStudyStarted)
  }

      when(State.EmployeeStudyStarted) {
        case Event(Command.ReceiveMentor(mentor), Data.EmployeeData(employee, staff, mentorStore)) =>
          if (mentorStore.contains(mentor)) {
            val padavans = mentorStore(mentor)
            if (padavans.size < 2) {
              sender() ! Command.Comments(s"Employee $employee receive mentor $mentor")
              val newMentorStore = mentorStore + (mentor -> (padavans + employee))
              stay() using Data.EmployeeDataMentoring(employee, mentor, staff, newMentorStore)
            } else {
              sender() ! Command.Comments(s"Mentor filled")
              stay() using Data.EmployeeData(employee, staff, mentorStore)
            }
          } else {
            sender() ! Command.Comments("Mentor Invalid try other mentor")
            stay() using Data.EmployeeData(employee, staff, mentorStore)
          }

        case Event(Command.ExamPass, Data.EmployeeDataMentoring(employee, mentor, staff, mentorStore)) =>
          sender() ! Command.Comments("Trainee pass theory exam and goto Lab")
          goto(State.EmployeeLabStarted) using Data.EmployeeDataMentoring(employee, mentor, staff, mentorStore)
      }

      when(State.EmployeeLabStarted) {
        case Event(Command.ExamPass, Data.EmployeeDataMentoring(employee, mentor, staff, mentorStore)) =>
          sender() ! Command.Comments("Trainee pass Lab exam and goto Graduated")
          goto(State.EmployeeGraduated) using Data.EmployeeDataMentoring(employee, mentor, staff, mentorStore)
      }

      when(State.EmployeeGraduated) {
        case Event(Command.IncreaseJ1, Data.EmployeeDataMentoring(employee, mentor, staff, mentorStore)) =>
          val j1 = employee.copy(id = employee.id, name = employee.name, lastName = employee.lastName, department = employee.department,
            category = CategoryType.J1)
          val newStaff = staff - employee + j1
          if (mentorStore.contains(mentor)) {
            val padavans = mentorStore(mentor)
            sender() ! Command.Comments("Googbue mentor!")
            val newPadavans = padavans - employee
            val newMentorStore = mentorStore + (mentor -> newPadavans)
            goto(State.EmployeeHired) using Data.EmployeeData(j1, newStaff, newMentorStore)
          } else {
            sender() ! Command.Comments("Mentor quit")
            goto(State.EmployeeHired) using Data.EmployeeData(j1, newStaff, mentorStore)
          }
      }

      when(State.EmployeeWorkStarted) {
        case Event(Command.IncreaseMentor, Data.EmployeeData(employee, staff, mentorStore)) if !employee.category.toString.startsWith("J") =>
          goto(State.EmployeeMentorAvailable)
        case Event(Command.FireEmployee, Data.EmployeeData(employee, staff, mentorStore)) =>
          sender() ! Command.Comments(s"Fire employee $employee")
          goto(State.EmployeeQuit) using Data.EmployeeData(employee, staff, mentorStore)
      }

      when(State.EmployeeMentorAvailable) {
        case Event(Command.InitMentor, Data.EmployeeData(employee, staff, mentorStore)) =>
          if (!mentorStore.contains(employee)) {
            val newMentorStore = mentorStore + (employee -> Set[Employee]())
            sender() ! Command.Comments(s"New mentor $employee init")
            stay() using Data.EmployeeData(employee, staff, newMentorStore)
          } else {
            sender() ! Command.Comments(s"Mentor $employee already was inited")
            stay()
          }
        case Event(Command.ReceivePadavan(padavan), Data.EmployeeData(employee, staff, mentorStore)) =>
          if (mentorStore.contains(employee)) {
            val padavans = mentorStore(employee)
            if (padavans.size < 2) {
              sender() ! Command.Comments(s" $employee added padavan $padavan")
              val newMentorStore = mentorStore + (employee -> (padavans + padavan))
              stay() using Data.EmployeeData(employee, staff, newMentorStore)
            } else {
              sender() ! Command.Comments(s" Mentor is filled")
              goto(State.EmployeeMentorFilled) using Data.EmployeeData(employee, staff, mentorStore)
            }
          } else {
            sender() ! Command.Comments("Mentor not Init")
            stay()
          }

        case Event(Command.RemovePadavan(padavan), Data.EmployeeData(employee, staff, mentorStore)) =>
          if (mentorStore.contains(employee)) {
            val padavans = mentorStore(employee)
            padavans.find(_.id == padavan.id) match {
              case Some(padavan) =>
                val newPadavans = padavans - padavan
                val newMentorStore = mentorStore + (employee -> newPadavans)
                goto(State.EmployeeMentorAvailable) using Data.EmployeeData(employee, staff, newMentorStore)
              case None =>
                sender() ! Command.Comments(s"$employee not mentoring padavan $padavan")
                stay()
            }
          } else {
            sender() ! Command.Comments("Mentor not Init")
            stay()
          }

        case Event(Command.FireEmployee, Data.EmployeeData(employee, staff, mentorStore)) =>
          sender() ! Command.Comments(s"Fire employee $employee")
          goto(State.EmployeeQuit)
      }

      when(State.EmployeeMentorFilled) {
        case Event(Command.RemovePadavan(padavan), Data.EmployeeData(employee, staff, mentorStore)) =>
          val padavans = mentorStore(employee)
          padavans.find(_.id == padavan.id) match {
            case Some(padavan) =>
              val newPadavans = padavans - padavan
              val newMentorStore = mentorStore + (employee -> newPadavans)
              goto(State.EmployeeMentorAvailable) using Data.EmployeeData(employee, staff, newMentorStore)
            case None =>
              sender() ! Command.Comments(s"$employee not mentoring padavan $padavan")
              stay() using Data.EmployeeData(employee, staff, mentorStore)
          }
        case Event(Command.FireEmployee, Data.EmployeeData(employee, staff, mentorStore)) =>
          sender() ! Command.Comments(s"Fire employee $employee")
          goto(State.EmployeeQuit) using Data.EmployeeData(employee, staff, mentorStore)
      }
      when(State.EmployeeQuit) {
        case Event(Command.Quit, Data.EmployeeData(employee, staff, mentorStore)) =>
          val newStaff = staff - employee
          if (mentorStore.contains(employee)) {
            val newMentorStore = mentorStore - employee
            sender() ! Command.Comments(s"Employee $employee has fired")
            goto(State.Recruitment) using Data.Initialized(newStaff, newMentorStore)
          } else {
            goto(State.Recruitment) using Data.Initialized(newStaff, mentorStore)
          }
      }
  }



