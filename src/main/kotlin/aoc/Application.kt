package aoc

import io.reactivex.Single
import aoc.profiling.Profiler
import aoc.services.ChallengeInputService
import aoc.services.RequestService
import aoc.services.SessionService
import aoc.solutions.day1.Day1Solution

open class Application {
    companion object {
        private val requestService = RequestService()
        private val sessionService = SessionService(requestService)
        private val challengeInputService = ChallengeInputService(requestService)
        private val profiler = Profiler()
        private val solutions = arrayOf(
                Day1Solution())

        @JvmStatic
        fun main(args: Array<String>) {

            println("Advent of Code solutions by Ken Van Hoeylandt")
            println()

            try {
                val applicationParameters = applicationParameters(args)

                sessionService.setSessionToken(applicationParameters.sessionToken)

                val dayIndex = applicationParameters.day - 1

                if (dayIndex < 0 || dayIndex > solutions.lastIndex) {
                    println("ERROR: Failed to get solution")
                    return
                }

                val solution = solutions[dayIndex]

                solve(solution).blockingGet()

            } catch (caught: Exception) {
                println("Usage:")
                println("\tjava -jar AdventOfCode2016.jar [sessionToken] [day]")
            }
        }

        private fun solve(solution: Solution): Single<String> {
            println("Retrieving assignment data...")

            return challengeInputService.request(solution.day)
                    .map {
                        println("Solving assignment...")
                        profiler.start("Solution Part 1")
                        val resultPartOne = solution.solvePartOne(it)
                        profiler.stop("Solution Part 1")

                        profiler.start("Solution Part 2")
                        val resultPartTwo = solution.solvePartTwo(it)
                        profiler.stop("Solution Part 2")

                        " - part 1: $resultPartOne\n - part 2: $resultPartTwo"
                    }
                    .doOnError {
                        println("error when solving assignment")

                        val message = it.message

                        if (message != null) {
                            println("Solution failed: $message")
                        }

                        it.printStackTrace()
                    }
                    .doOnSuccess {
                        println("Solution for day ${solution.day}:\n$it")
                    }
        }
    }
}