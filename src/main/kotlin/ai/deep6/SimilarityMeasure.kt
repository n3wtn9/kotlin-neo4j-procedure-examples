package ai.deep6

import ai.deep6.constants.REL
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.*
import org.neo4j.kernel.builtinprocs.SchemaProcedure
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Created by newton on 7/3/17.
 *
 * REF:
 * Semantic similarity in the biomedical domain: an evaluation across knowledge sources
 * Vijay N GarlaEmail author and Cynthia Brandt
 */
class SimilarityMeasure {

    val conceptsPath: TraversalDescription
    val biDiNodePath: BidirectionalTraversalDescription
    val log: Log

    constructor(log: Log, db: GraphDatabaseAPI, sourcePropKey: String, sourcePropValue: String) {
        this.log = log
        conceptsPath = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF, Direction.INCOMING)
                .evaluator(SourceOntologyEvaluator(sourcePropKey, sourcePropValue))
                .uniqueness(Uniqueness.NODE_PATH)

        biDiNodePath = db.bidirectionalTraversalDescription()
                .startSide(conceptsPath)
                .endSide(conceptsPath)
    }

    fun calculateSimilarityPathIcBiDi(node1: Node, node2: Node): Double {
        val pathResults = biDiNodePath
                .traverse(node1, node2)
                .stream()
                .toList()

        // Deal with multiple results
        val path = when (pathResults.size) {
            0 -> return 0.0
            1 -> pathResults.first()
            else -> {
                log.info("Ahh multiple paths found between 2 concepts...choosing shortest one.")
                pathResults.sortedBy { it.length() }.first()
            }
        }

        // Find the least common subsumer (common parent)
        var lcsNode: Node = path.startNode()
        for(rel in path.relationships()) {
            lcsNode = if (rel.startNode.equals(lcsNode)) break else rel.startNode
        }

        val c1Ic = node1.getProperty("infoContent") as Double
        val c2Ic = node2.getProperty("infoContent") as Double
        val lcsIc = lcsNode.getProperty("infoContent") as Double

        return similarityPathIcEq(c1Ic, c2Ic, lcsIc)
    }

    fun similarityPathIcEq(c1IC: Double, c2IC: Double, lcsIC: Double): Double {
        val distance = c1IC + c2IC - 2 * lcsIC

        val similarityScore = 1 / (distance + 1)

        return similarityScore
    }
}