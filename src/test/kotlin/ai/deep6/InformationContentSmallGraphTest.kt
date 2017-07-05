package ai.deep6

import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.neo4j.driver.v1.Config
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session
import org.neo4j.harness.junit.Neo4jRule
import scala.util.matching.Regex

/**
 * Created by newton on 6/29/17.
 */
class InformationContentSmallGraphTest {
    lateinit private var driver: Driver
    lateinit private var session: Session

    @Rule @JvmField
    val neo4j = Neo4jRule()
            .withProcedure(SimilarityMeasureService::class.java)
            .withFunction(SimilarityMeasureService::class.java)
            .withFixture(
                """
                CREATE
                (root:Concept{str:'root'}),
                (animal:Concept{str:'animal'}),
                (vertebrate:Concept{str:'vertebrate'}),
                (cat:Concept{str:'cat'}),
                (dog:Concept{str:'dog'}),
                (bacteria:Concept{str:'bacteria'}),
                (ecoli:Concept{str:'ecoli'}),

                (root)-[:PARENT_OF]->(animal)-[:PARENT_OF]->(vertebrate),
                (vertebrate)-[:PARENT_OF]->(cat),
                (vertebrate)-[:PARENT_OF]->(dog),
                (root)-[:PARENT_OF]->(bacteria)-[:PARENT_OF]->(ecoli)
                """
            )

    @Before
    fun setup() {
        driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig())
    }

    @Test
    fun helloWorldCalInformationContent() {
        driver.session().use {
            it.run("MATCH (n:Concept{str:'root'}) CALL ai.deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'animal'}) CALL ai.deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'vertebrate'}) CALL ai.deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'cat'}) CALL ai.deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'dog'}) CALL ai.deep6.calculateInfoContent(n) RETURN *")
        }
    }

    @Test
    fun calculateInfoContentFromRootTest() {
        driver.session().use {
            it.run("MATCH (n:Concept{str:'root'}) CALL ai.deep6.calculateInfoContentFromRoot(n) RETURN *")

            val results = it.run("MATCH (n:Concept) RETURN n")
            results.forEach{ it.values().forEach { println(it.asNode().values().toList().joinToString()) } }
        }
    }

    @Ignore
    @Test
    fun calculateSimilarlityPathIcMeasure() {
        driver.session().use {
            val results = it.run("""
                MATCH (c1:Concept{str:'dog'}), (c2:Concept{str:'ecoli'})
                CALL ai.deep6.similarityPathIc(c1,c2) YIELD result
                RETURN result
            """)

            println(results.list().size)
        }
    }

    @Test
    fun calculateSimilarityPathIcMeasureFunc() {
        driver.session().use {
            it.run("""
                MATCH (n:Concept{str:'root'})
                CALL ai.deep6.calculateInfoContentFromRoot(n)
                RETURN *
                """)

            val results = it.run("""
                MATCH (c1:Concept{str:'dog'}), (c2:Concept{str:'ecoli'})
                RETURN ai.deep6.similarityPathIc(c1,c2)
                """)

            println(results.single().values().toList().joinToString())
        }
    }
}