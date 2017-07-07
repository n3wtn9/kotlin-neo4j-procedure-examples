package ai.deep6

import ai.deep6.constants.REL
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import kotlin.streams.toList

/**
 * Created by newton on 6/29/17.
 */
class InformationContent {
    val nodeToLeaf: TraversalDescription
    val rootToNode: TraversalDescription

    val log: Log

    val leafCountProperty = "leafCount"
    val informationContentProperty = "infoContent"

    constructor(log: Log, db: GraphDatabaseAPI, sourcePropKey: String, sourcePropValue: String) {

        this.log = log

        nodeToLeaf = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF, Direction.OUTGOING)
                .evaluator(SourceOntologyEvaluator(sourcePropKey, sourcePropValue))

        rootToNode = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF, Direction.OUTGOING)
                .evaluator(SourceOntologyEvaluator(sourcePropKey, sourcePropValue))
    }

    fun calculateRootToNodeIC(rootNode: Node) {
        val maxLeaves = rootToNode.traverse(rootNode).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }
                .count()
                .toDouble()

        val log2adjust = 1.0 / Math.log(2.0)
        val num = Math.log(maxLeaves + 1.0)

        rootToNode.traverse(rootNode).stream().forEach {
            val subsumerCount = it.length() + 1.0

            var leafCount =
            if(it.endNode().hasProperty(leafCountProperty))
                it.endNode().getProperty(leafCountProperty) as Double
            else countLeaves(it.endNode())

            val denom = Math.log(leafCount / subsumerCount + 1.0)
            val infoContent = log2adjust * ( num - denom )

            it.endNode().setProperty(informationContentProperty, infoContent)
        }
    }

    fun countLeaves(child: Node): Double {
        val leafCount = nodeToLeaf.traverse(child).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }
                .count()
                .toDouble()

        child.setProperty(leafCountProperty, leafCount)

        return leafCount
    }
}