package syntax.tree

import interpreter.BasicBlock
import semantic.types.StructType
import semantic.types.StructTypeLater
import syntax.lexer.Token
import java.util.*

class TranslationUnit(val externalDeclarations: List<Node>) : Node() {
    val functions = externalDeclarations.filterIsInstance<FunctionDefinition>()
    val declarations: List<Declaration>

    init {
        declarations = ArrayList<Declaration>()
        walkChildren({}) {
            if (it is Declaration) {
                declarations.add(it)
            }
        }
    }

    override fun forEachChild(action: (Node) -> Unit) {
        externalDeclarations.forEach(action)
    }

    override fun root(): Token = externalDeclarations.first().root()

    override fun toString(): String = "translation unit"
}

class FunctionDefinition(val specifiers: DeclarationSpecifiers, val namedDeclarator: NamedDeclarator, val body: List<Statement>, val closingBrace: Token) : Node() {
    fun name(): String = namedDeclarator.name.text

    val parameters: List<NamedDeclarator> = (namedDeclarator.declarator as Declarator.Function).parameters.map { it.namedDeclarator }

    var stackFrameType: StructType = StructTypeLater

    lateinit var controlFlowGraph: LinkedHashMap<String, BasicBlock>

    override fun forEachChild(action: (Node) -> Unit) {
        // action(declarator)
        body.forEach(action)
    }

    override fun root(): Token = namedDeclarator.name

    override fun toString(): String = namedDeclarator.toString()
}
