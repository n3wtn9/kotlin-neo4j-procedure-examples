package ai.deep6

import ai.deep6.constants.REL
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import kotlin.streams.toList

/**
 * Created by newton on 6/29/17.
 */
class InformationContent {
    val nodeToRoot: TraversalDescription
    val nodeToLeaf: TraversalDescription
    val rootToNode: TraversalDescription

    val log: Log
    val log2adjust : Double

    val leafCountProperty = "leafCount"
    val informationContentProperty = "infoContent"

    constructor(log: Log, db: GraphDatabaseAPI, sourceProperty: String, sourceOnthology: String) {
        log2adjust = 1.0 / Math.log(2.0)
        this.log = log
        nodeToRoot = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF, Direction.INCOMING)
                .evaluator(SourceOntologyEvaluator(sourceProperty, sourceOnthology))

        nodeToLeaf = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF, Direction.OUTGOING)
                .evaluator(SourceOntologyEvaluator(sourceProperty, sourceOnthology))

        rootToNode = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF, Direction.OUTGOING)
                .evaluator(SourceOntologyEvaluator(sourceProperty, sourceOnthology))
    }

    @Deprecated("old code")
    fun calculate(startNode: Node): Double {
        val rootPath = nodeToRoot.traverse(startNode).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.INCOMING) }
                .toList()

        if (rootPath.size > 1) log.info("more than one root?!!?")

        val path = rootPath.get(0)
        val root = path.endNode()
        val subsumerCount = path.length().toDouble() + 1.0
//        log.info("str: ${path.endNode().getProperty("str")}, subsumer count: ${path.length() + 1}")

        val leaves = nodeToLeaf.traverse(startNode).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }
                .toList()

//        println("has x leaves: ${leaves.size}")
//        leaves.forEach { println("str: ${it.endNode().getProperty("str")}") }
        val leafCount = leaves.size.toDouble()

        val maxLeaves = nodeToLeaf.traverse(root).stream()
                .filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }
                .count()
                .toDouble()

        val denom = Math.log(leafCount / subsumerCount + 1.0)
        val num = Math.log(maxLeaves + 1.0)
        val ic = log2adjust * ( num - denom )
        return ic
    }

    fun calculateRootToNodeIC(rootNode: Node) {
        val maxLeaves = rootToNode.traverse(rootNode).stream().filter { !it.endNode().hasRelationship(REL.PARENT_OF, Direction.OUTGOING) }.count().toDouble()
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

    class SourceOntologyEvaluator(val sourceProperty: String, val sourceOnthology: String): Evaluator {
        override fun evaluate(path: Path?): Evaluation {
            if (path!!.endNode().hasProperty(sourceProperty) && path.endNode().getProperty(sourceProperty).equals(sourceOnthology))
                return Evaluation.INCLUDE_AND_CONTINUE
            else
                return Evaluation.EXCLUDE_AND_PRUNE
        }
    }
}