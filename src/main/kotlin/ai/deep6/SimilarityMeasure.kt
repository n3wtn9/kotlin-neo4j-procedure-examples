package ai.deep6

import ai.deep6.constants.REL
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator
import org.neo4j.graphdb.traversal.Evaluators
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.internal.GraphDatabaseAPI
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Created by newton on 7/3/17.
 */
class SimilarityMeasure {

    val conceptsPath: TraversalDescription

    constructor(db: GraphDatabaseAPI) {
        conceptsPath = db.traversalDescription()
                .relationships(REL.PARENT_OF)
    }

    fun calcualteSimilarityPathIc(concept1: Node, concept2: Node): Double {
        val pathResults = conceptsPath
                .evaluator(Evaluators.endNodeIs<Any>(Evaluation.INCLUDE_AND_CONTINUE, Evaluation.EXCLUDE_AND_CONTINUE, concept2))
                .traverse(concept1)
                .stream()
                .toList()

        return 0.0
    }
}