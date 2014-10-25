/**
 * @author Emmanuel Nhan
 */

import models.{Assignment, AssignmentGenerator}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
@RunWith(classOf[JUnitRunner])
class AssignmentGeneratorSpec extends Specification{

  "Pre processing with assignment generator" should {
    "generate a list" in {
      val str = "One\nTwo\nThree"
      new AssignmentGenerator(1L, 3).preProcess(str) must beEqualTo(List("One", "Two", "Three"))
    }
  }

  "Song splitting with assignment generator" should {
    "generate a simple list when no other thing than one part" in {
      val str = "One\nTwo\nThree"
      new AssignmentGenerator(1L, 3).songSplit(str) must beEqualTo(List(List("One", "Two", "Three")))
    }

    "generate a two element list when two parts separated with double spaces" in {
      val str = "One\nTwo\n\nThree"
      new AssignmentGenerator(1L, 3).songSplit(str) must beEqualTo(List(List("One", "Two"), List("Three")))
    }

    "generate a two element list when two parts separated with multiple spaces" in {
      val str = "One\nTwo\n\n\nThree"
      new AssignmentGenerator(1L, 3).songSplit(str) must beEqualTo(List(List("One", "Two"), List("Three")))
    }
  }


  "An assignment generator" should {
    "generate simple assignment from a two line song " in {
      val songId = 1L
      val spots = 3
      val songPart = List(
        "This is the first line",
        "This is the second line")

      new AssignmentGenerator(1L, spots).parse(songPart) must beEqualTo(List(
        Assignment(None, songId,0, "This is the first line\nThis is the second line", "", "", spots, 0 )
      ))
    }


    "generate two assignments from a 4 line song " in {
      val songId = 1L
      val spots = 3
      val songPart = List(
        "This is the first line",
        "This is the second line",
      "This is the third one",
      "This is the last"
      )

      new AssignmentGenerator(1L, spots).parse(songPart) must beEqualTo(List(
        Assignment(None, songId, 1, "This is the third one\nThis is the last", "This is the second line", "", spots, 0),
        Assignment(None, songId, 0, "This is the first line\nThis is the second line", "", "This is the third one", spots, 0)
      ))
    }

    "generate 3 assignments from a 5 line song" in {
      val songId = 1L
      val spots = 3
      val songPart = List(
        "This is the first line",
        "This is the second line",
        "This is the third one",
        "This is the last",
        "This IS !"
      )

      new AssignmentGenerator(1L, spots).parse(songPart) must beEqualTo(List(
        Assignment(None, songId, 2, "This IS !", "This is the last", "", spots, 0),
        Assignment(None, songId, 1, "This is the third one\nThis is the last", "This is the second line", "This IS !", spots, 0),
        Assignment(None, songId, 0, "This is the first line\nThis is the second line", "", "This is the third one", spots, 0)
      ))
    }

    "generate 3 assignments from a 5 line song with rank offset" in {
      val songId = 1L
      val spots = 3
      val songPart = List(
        "This is the first line",
        "This is the second line",
        "This is the third one",
        "This is the last",
        "This IS !"
      )

      new AssignmentGenerator(1L, spots).parse(songPart, 3) must beEqualTo(List(
        Assignment(None, songId, 5, "This IS !", "This is the last", "", spots, 0),
        Assignment(None, songId, 4, "This is the third one\nThis is the last", "This is the second line", "This IS !", spots, 0),
        Assignment(None, songId, 3, "This is the first line\nThis is the second line", "", "This is the third one", spots, 0)
      ))
    }

  }

  "An assignment generator" should {
    "generate a correct song with simple case" in {
      val song =
        """This is the first verse
          |This is the first one
          |
          |
          |This is the refrain
          |Refain suite""".stripMargin

      val songId = 1L
      val spots = 3
      new AssignmentGenerator(songId, spots).parseSong(song) must beEqualTo(List(
        Assignment(None, songId, 0, "This is the first verse\nThis is the first one", "", "", spots, 0),
        Assignment(None, songId, 1, "This is the refrain\nRefain suite", "", "", spots, 0)
      ))
    }

    "generate a correct song with complex case" in {
      val song =
        """This is the first verse
          |This is the first one
          |This another thing
          |With some other content
          |
          |
          |This is the refrain
          |Refain suite""".stripMargin

      val songId = 1L
      val spots = 3
      new AssignmentGenerator(songId, spots).parseSong(song) must beEqualTo(List(
        Assignment(None, songId, 1, "This another thing\nWith some other content", "This is the first one", "", spots, 0),
        Assignment(None, songId, 0, "This is the first verse\nThis is the first one", "", "This another thing", spots, 0),
        Assignment(None, songId, 2, "This is the refrain\nRefain suite", "", "", spots, 0)
      ))
    }
  }

}
