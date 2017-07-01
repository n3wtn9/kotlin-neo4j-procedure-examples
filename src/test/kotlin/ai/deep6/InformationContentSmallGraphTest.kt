package ai.deep6

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.neo4j.driver.v1.Config
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session
import org.neo4j.harness.junit.Neo4jRule

/**
 * Created by newton on 6/29/17.
 */
class InformationContentSmallGraphTest {
    lateinit private var driver: Driver
    lateinit private var session: Session

    @Rule @JvmField
    val neo4j = Neo4jRule()
            .withProcedure(InformationContentService::class.java)
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
            it.run("MATCH (n:Concept{str:'root'}) CALL deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'animal'}) CALL deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'vertebrate'}) CALL deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'cat'}) CALL deep6.calculateInfoContent(n) RETURN *")
            it.run("MATCH (n:Concept{str:'dog'}) CALL deep6.calculateInfoContent(n) RETURN *")
        }
    }
}