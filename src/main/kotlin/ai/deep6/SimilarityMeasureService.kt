package ai.deep6

import org.neo4j.graphdb.Label
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.RelationshipType
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import org.neo4j.procedure.*
import java.util.stream.Stream
import kotlin.streams.asSequence
import kotlin.streams.toList

/**
 * Created by newton on 6/29/17.
 */
class SimilarityMeasureService {
    @Context
    lateinit var db: GraphDatabaseAPI

    @Context
    lateinit var log: Log

    @Procedure(value = "ai.deep6.calculateInfoContent", mode = Mode.WRITE)
    @Description("Calculate the information content from node")
    fun calculateInfoContent(@Name("startNode") startNode: Node) {
        val ic = InformationContent(log,db)
        println("node: ${startNode.getProperty("str")}, info content ${ic.calculate(startNode)}")
    }

    @Procedure(value = "ai.deep6.calculateInfoContentFromRoot", mode = Mode.WRITE)
    @Description("Calculate the information content given a root node")
    fun calculateInfoContentFromRoot(@Name("rootNode") rootNode: Node) {
        val ic = InformationContent(log,db)
        ic.calculateRootToNodeIC(rootNode)
    }

//    class PathRes(@JvmField val result: Path)
//
//    @Procedure(value = "ai.deep6.similarityPathIc")
//    @Description("Calculate the similarity based on path and information content")
//    fun similarityPathIc(@Name("concept1") concept1: Node, @Name("concept2") concept2: Node): Stream<PathRes> {
//        val sm = SimilarityMeasure(db)
//        return sm.calcualteSimilarityPathIc(concept1,concept2).map(::PathRes)
//    }

    @UserFunction(value = "ai.deep6.similarityPathIc")
    @Description("Function to calculate similarity based on path and information content")
    fun similarityPathIcFunc(@Name("concept1") concept1: Node, @Name("concept2") concept2: Node): Double {
        val sm = SimilarityMeasure(db)
        return sm.calcualteSimilarityPathIc(concept1, concept2)
    }
}