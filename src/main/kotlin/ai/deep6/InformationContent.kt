package ai.deep6

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import org.neo4j.procedure.*

/**
 * Created by newton on 6/29/17.
 */
class InformationContent {
    @Context
    lateinit var db: GraphDatabaseAPI

    @Context
    lateinit var log: Log

    private var taxonomyWalker: TraversalDescription

    init {
        taxonomyWalker = db.traversalDescription()
    }

    @Procedure(value = "deep6.semantic.calulateInfoContent", mode = Mode.WRITE)
    @Description("Calculate the information content given a root node")
    fun calculateInfoContent(@Name("root") root: Node) {
        log.info("hello world")
    }
}