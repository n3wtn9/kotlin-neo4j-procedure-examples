package ai.deep6

import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator

/**
 * Created by newton on 7/6/17.
 */
class SourceOntologyEvaluator(val sourcePropKey: String, val sourcePropValue: String) : Evaluator {
        override fun evaluate(path: Path?): Evaluation {
                if (path!!.endNode().hasProperty(sourcePropKey)
                        && path.startNode().hasProperty(sourcePropKey)
                        && path.endNode().getProperty(sourcePropKey).equals(sourcePropValue)
                        && path.startNode().getProperty(sourcePropKey).equals(sourcePropValue))
                        return Evaluation.INCLUDE_AND_CONTINUE
                else
                        return Evaluation.EXCLUDE_AND_PRUNE
        }
}