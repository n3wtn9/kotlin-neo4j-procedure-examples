package ai.deep6

import ai.deep6.constants.REL
import com.sun.corba.se.impl.orbutil.graph.Graph
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.internal.GraphDatabaseAPI
import kotlin.streams.toList

/**
 * Created by newton on 6/29/17.
 */
class InformationContent {
    val nodeToRoot: TraversalDescription
    val nodeToLeaf: TraversalDescription

    constructor(db: GraphDatabaseAPI) {
        nodeToRoot = db.traversalDescription()
                .relationships(REL.PARENT_OF, Direction.INCOMING)
        nodeToLeaf = db.traversalDescription()
                .relationships(REL.PARENT_OF, Direction.OUTGOING)
    }

    fun calculate(startNode: Node): Double {
        val rootPath = nodeToRoot.traverse(startNode).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.INCOMING) }
                .toList()

        if (rootPath.size > 1) println("more than one root?!!?")

        val path = rootPath.get(0)
        val root = path.endNode()
        val subsumerCount = path.length().toDouble() + 1.0
        println("str: ${path.endNode().getProperty("str")}, subsumer count: ${path.length() + 1}")

        val leaves = nodeToLeaf.traverse(startNode).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }
                .toList()

        println("has x leaves: ${leaves.size}")
        leaves.forEach { println("str: ${it.endNode().getProperty("str")}") }
        val leafCount = leaves.size.toDouble()

        val maxLeaves = nodeToLeaf.traverse(root).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }
                .count()
                .toDouble()

        val log2adjust = 1.0 / Math.log(2.0)
        val denom = log2adjust * Math.log(leafCount / subsumerCount + 1.0)
        val num = log2adjust * Math.log(maxLeaves + 1.0)
        return num - denom
    }
}