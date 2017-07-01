package ai.deep6

import ai.deep6.constants.REL
import com.sun.corba.se.impl.orbutil.graph.Graph
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.internal.GraphDatabaseAPI

/**
 * Created by newton on 6/29/17.
 */
class InformationContent {
    val nodeToRoot: TraversalDescription

    constructor(db: GraphDatabaseAPI) {
        nodeToRoot = db.traversalDescription()
                .relationships(REL.PARENT_OF, Direction.INCOMING)
    }

    fun calculate(startNode: Node) {
        for(path in nodeToRoot.traverse(startNode)) {
            println(path.endNode().getProperty("str"))
        }
    }
}