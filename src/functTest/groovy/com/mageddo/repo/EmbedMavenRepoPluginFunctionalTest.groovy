package com.mageddo.repo

import groovy.io.FileType
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class UrlVerifierPluginFunctionalTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'java'
                id 'com.runafter.gradle-embed-maven-repo'
            }

        """
    }

    def "Should retrieve and create pom files to custom maven repo"() {
        buildFile << """
            repositories {
                maven {
                    url "file://\${projectDir}/maven"
                }
                mavenCentral()
            }
            dependencies{
                compile group: 'com.google.code.gson', name: 'gson', version: '2.6.2'
                compile group: 'junit', name: 'junit', version: '4.12'
            }
            task createMirror(type: RepoBuilder){
                mavenRepoFolder = file("file://\${projectDir}/maven")
            }
        """

        when:
        println(testProjectDir)
        println(testProjectDir.root)
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments("createMirror", "--info")
            .build()

        then:
        result.task(":createMirror").outcome == SUCCESS
        def pomFiles = []
        testProjectDir.root.eachFileRecurse(FileType.FILES) { file ->
            if (file.name.endsWith("pom"))
                pomFiles << file
        }
        pomFiles.empty == false
    }
}