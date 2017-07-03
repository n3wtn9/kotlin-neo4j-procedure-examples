package ai.deep6

import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.neo4j.driver.v1.Config
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session
import org.neo4j.harness.junit.Neo4jRule

import org.junit.Assert.*
import org.junit.Ignore

/**
 * Created by newton on 6/29/17.
 */

class ExampleTest {
    lateinit private var driver: Driver
    lateinit private var session: Session

    @Rule @JvmField
    val neo4j = Neo4jRule()
            .withProcedure(Example::class.java)
            .withFunction(Example::class.java)

    @Before
    fun setup() {
        driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig())
        session = driver.session()
    }

    @Test
    fun shouldConcatStringCorrectly() {
        val result = session.run("RETURN ai.deep6.concat(['name','surname'],';') AS result")
        assertThat  (result.single().get("result").asString(), IsEqual.equalTo("name;surname"))
    }

    @Test
    fun shouldConnectNodesCorrectly() {
        session.run("CREATE (p:From)")
        session.run("CREATE (p:To)")
        session.run("""
                       MATCH (f:From), (t:To)
                       CALL ai.deep6.connect(f,'KNOWS',t)
                       RETURN *
                    """)
        val result = session.run("MATCH p=(f:From)-[:KNOWS]->(t:To) RETURN p")
        assertThat(result.single().size(), IsEqual.equalTo(1))
    }
}