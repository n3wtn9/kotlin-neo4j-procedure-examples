package ai.deep6

import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
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
            .withProcedure(SimilarityMeasureService::class.java)
            .withFunction(SimilarityMeasureService::class.java)
            .withFixture(
                """
                CREATE
                (root:Concept{str:'root', sab:'LIFE'}),
                (animal:Concept{str:'animal', sab:'LIFE'}),
                (vertebrate:Concept{str:'vertebrate', sab:'LIFE'}),
                (cat:Concept{str:'cat', sab:'LIFE'}),
                (dog:Concept{str:'dog', sab:'LIFE'}),
                (bacteria:Concept{str:'bacteria', sab:'LIFE'}),
                (ecoli:Concept{str:'ecoli', sab:'LIFE'}),

                (root)-[:PARENT_OF]->(animal)-[:PARENT_OF]->(vertebrate),
                (vertebrate)-[:PARENT_OF]->(cat),
                (vertebrate)-[:PARENT_OF]->(dog),
                (root)-[:PARENT_OF]->(bacteria)-[:PARENT_OF]->(ecoli),

                (r2:Comm{str:'root', sab:'TELECOM'}),
                (wired:Comm{str:'wired', sab:'TELECOM'}),
                (wireless:Comm{str:'wireless', sab:'TELECOM'}),
                (copper:Comm{str:'copper', sab:'TELECOM'}),
                (fiber:Comm{str:'fiber', sab:'TELECOM'}),
                (wifi:Comm{str:'wifi', sab:'TELECOM'}),
                (cellular:Comm{str:'cellular', sab:'TELECOM'}),

                (r2)-[:PARENT_OF]->(wired),
                (r2)-[:PARENT_OF]->(wireless),
                (wired)-[:PARENT_OF]->(copper),
                (wired)-[:PARENT_OF]->(fiber),
                (wireless)-[:PARENT_OF]->(wifi),
                (wireless)-[:PARENT_OF]->(cellular)
                """
            )

    @Before
    fun setup() {
        driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig())
    }

    @Test
    fun calculateInfoContentFromRootTest() {
        driver.session().use {
            it.run("MATCH (n:Concept{str:'root'}) CALL ai.deep6.calculateInfoContentFromRoot(n,'sab','LIFE') RETURN *")

            val results = it.run("MATCH (n:Concept) RETURN n")
            results.forEach{ it.values().forEach { println(it.asNode().values().toList().joinToString()) } }
            results.forEach {
                it.values().forEach {
                    when(it.asNode().get("str").asString()) {
                        "root" -> assertEquals(0.0, it.get("infoContent").asDouble(), 0.0)
                        "animal" -> assertEquals(1.0, it.get("infoContent").asDouble(), 0.0)
                        "vertibrate" -> assertEquals(1.263034405833794, it.get("infoContent").asDouble(), 0.0)
                        "cat" -> assertEquals(1.6780719051126376, it.get("infoContent").asDouble(), 0.0)
                        "dog" -> assertEquals(1.6780719051126376, it.get("infoContent").asDouble(), 0.0)
                        "bacteria" -> assertEquals(1.4150374992788437, it.get("infoContent").asDouble(), 0.0)
                        "ecoli" -> assertEquals(1.5849625007211563, it.get("infoContent").asDouble(), 0.0)
                    }
                }
            }
        }
    }

    @Test
    fun calculateSimilarityPathIcMeasure() {
        driver.session().use {
            it.run("""
                MATCH (n:Concept{str:'root'})
                CALL ai.deep6.calculateInfoContentFromRoot(n,'sab','LIFE')
                RETURN *
                """)

            val results = it.run("""
                MATCH (c1:Concept{str:'dog'}), (c2:Concept{str:'ecoli'})
                RETURN ai.deep6.similarityPathIc('sab','LIFE',c1,c2)
                """)

            assertEquals(0.2345746960501983, results.single().values().get(0).asDouble(), 0.0)
        }
    }

    @Test
    fun calculateSimScoreTest() {
        driver.session().use {
            it.run("""
                MATCH (n:Concept{str:'root'})
                CALL ai.deep6.calculateInfoContentFromRoot(n,'sab','LIFE')
                RETURN *
                """)

            val results = it.run("""
                MATCH (c1:Concept{str:'dog'}), (c2:Concept{str:'cat'})
                RETURN ai.deep6.similarityPathIc('sab','LIFE',c1,c2)
                """)

            assertEquals(0.5464256933667291, results.single().values().get(0).asDouble() ,0.0)
        }
    }
}