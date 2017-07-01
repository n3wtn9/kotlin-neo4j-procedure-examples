package ai.deep6

import org.neo4j.graphdb.Label
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import org.neo4j.procedure.*

/**
 * Created by newton on 6/29/17.
 */
class InformationContentService {
    @Context
    lateinit var db: GraphDatabaseAPI

    @Context
    lateinit var log: Log

    @Procedure(value = "deep6.calculateInfoContent", mode = Mode.WRITE)
    @Description("Calculate the information content given a root node")
    fun calculateInfoContent(@Name("startNode") startNode: Node) {
        val ic = InformationContent(db)
        ic.calculate(startNode)
    }
}