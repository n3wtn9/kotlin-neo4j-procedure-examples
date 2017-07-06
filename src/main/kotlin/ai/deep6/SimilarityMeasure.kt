package ai.deep6

import ai.deep6.constants.REL
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator
import org.neo4j.graphdb.traversal.Evaluators
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.builtinprocs.SchemaProcedure
import org.neo4j.kernel.internal.GraphDatabaseAPI
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

    constructor(db: GraphDatabaseAPI) {
        conceptsPath = db.traversalDescription()
                .depthFirst()
                .relationships(REL.PARENT_OF)
    }

    fun calcualteSimilarityPathIc(concept1: Node, concept2: Node): Double {
        // Find paths between two concepts in ontology using traversal description
        val pathResults = conceptsPath
                .evaluator(Evaluators.endNodeIs<Any>(Evaluation.INCLUDE_AND_CONTINUE, Evaluation.EXCLUDE_AND_CONTINUE, concept2))
                .traverse(concept1)
                .stream()
                .toList()

        // Deal with multiple results
        val path = when (pathResults.size) {
            0 -> return 0.0
            1 -> pathResults.first()
            else -> {
                println("Ahh multiple paths found between 2 concepts...choosing shortest one.")
                pathResults.sortedBy { it.length() }.first()
            }
        }

        // Find the least common subsumer (common parent)
        var lcsNode: Node = path.startNode()
        for(rel in path.relationships()) {
            lcsNode = if (rel.startNode.equals(lcsNode)) break else rel.startNode
        }

        val c1Ic = concept1.getProperty("infoContent") as Double
        val c2Ic = concept2.getProperty("infoContent") as Double
        val lcsIc = lcsNode.getProperty("infoContent") as Double
        val distance = c1Ic + c2Ic - 2 * lcsIc

        val similarityScore = 1 / (distance + 1)

        return similarityScore
    }

    fun similarityPathDebug(concept1: Node, concept2: Node): Stream<SchemaProcedure.GraphResult> {
        val pathResults = conceptsPath
                .evaluator(Evaluators.endNodeIs<Any>(Evaluation.INCLUDE_AND_CONTINUE, Evaluation.EXCLUDE_AND_CONTINUE, concept2))
                .traverse(concept1)
                .stream()
                .toList()

        // Deal with multiple results
        val path = when (pathResults.size) {
            0 -> return listOf(SchemaProcedure.GraphResult(listOf(), listOf())).stream()
            1 -> pathResults.first()
            else -> {
                println("Ahh multiple paths found between 2 concepts...choosing shortest one.")
                pathResults.sortedBy { it.length() }.first()
            }
        }

        return listOf(SchemaProcedure.GraphResult(path.nodes().toList(), path.relationships().toList())).stream()
    }
}