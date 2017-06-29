package ai.deep6

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.neo4j.logging.Log
import org.neo4j.procedure.*

/**
 * Created by newton on 6/29/17.
 */
class Example {
    @Context
    lateinit var log: Log

    @Procedure(value = "example.connect", mode = Mode.WRITE)
    @Description("Conncet 2 nodes with given relationship")
    fun connect(@Name("from") from: Node, @Name("type") type: String, @Name("to") to: Node) {
        from.createRelationshipTo(to, RelationshipType.withName(type))
        log.info("Created a new relationship using connect.")
    }

    @UserFunction(value = "example.concat")
    @Description("example.concat(['s1','s2',...], delimiter) - join the given strings with the given delimiter.")
    fun concat(@Name("strings") strings: List<String>?, @Name("delimiter") delimiter: String?): String {
        val _strings = strings ?: listOf()
        val _delimiter = delimiter ?: ","
        return _strings.joinToString(_delimiter)
    }
}
