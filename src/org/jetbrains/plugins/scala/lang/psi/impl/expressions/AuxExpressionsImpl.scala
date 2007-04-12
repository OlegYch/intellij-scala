package org.jetbrains.plugins.scala.lang.psi.impl.expressions
/**
* @author Ilya Sergey
* PSI implementation for auxiliary expressions
*/
import com.intellij.lang.ASTNode
import com.intellij.psi.tree._
import com.intellij.psi._
import com.intellij.psi.scope._

import org.jetbrains.plugins.scala.lang.psi._
import org.jetbrains.plugins.scala.lang.resolve.processors._

import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.formatting.patterns.indent._
import org.jetbrains.plugins.scala.lang.psi.containers._
import org.jetbrains.plugins.scala.lang.psi.impl.top.templateStatements._

class ScExpression(node: ASTNode) extends ScExprImpl(node) {
  override def toString: String = "expression"
}

case class ScArgumentExprsImpl(node: ASTNode) extends ScExpr1Impl(node) with ContiniousIndent{
  override def toString: String = "Argument expressions"
}

case class ScBlockExprImpl(node: ASTNode) extends ScExpr1Impl(node) with BlockedIndent{
  override def toString: String = "Block expression"

  //    def isExpr = (elementType : IElementType) => (ScalaElementTypes.EXPRESSION_BIT_SET.contains(elementType))
  //
  //    def getExpression : ScExpr1Impl = childSatisfyPredicate(isExpr).asInstanceOf[ScExpr1Impl]
}

case class ScResExprImpl(node: ASTNode) extends ScExprImpl (node) with ScBlock {
  override def toString: String = "Result expression"
  def getType(): PsiType = null
}

case class ScBlockStatImpl(node: ASTNode) extends ScExpr1Impl(node) {
  override def toString: String = "Common block statement"
}

case class ScErrorStatImpl(node: ASTNode) extends ScExpr1Impl(node) {
  override def toString: String = "Error statement"
}

case class ScBindingImpl(node: ASTNode) extends ScalaPsiElementImpl (node) with Referenced {
  override def toString: String = "Binding"

  def getNames = {
    val children = allChildrenOfType[ScReference](REFERENCE_SET)
    if (children != null) {
      children.toList
    } else {
      Nil: List[ScReference]
    }
  }

}

class ScEnumeratorImpl(node: ASTNode) extends ScEnumeratorsImpl(node) {
  override def toString: String = "Enumerator"
}

case class ScEnumeratorsImpl(node: ASTNode) extends ScExpr1Impl(node) with ContiniousIndent{
  override def toString: String = "Enumerators"
}

case class ScAnFunImpl(node: ASTNode) extends ScExpr1Impl(node) with IfElseIndent with LocalContainer{

  import com.intellij.psi.scope._
  override def processDeclarations(processor: PsiScopeProcessor,
          substitutor: PsiSubstitutor,
          lastParent: PsiElement,
          place: PsiElement): Boolean = {
    import org.jetbrains.plugins.scala.lang.resolve.processors._

    if (processor.isInstanceOf[ScalaLocalVariableResolveProcessor]){
        this.varOffset = processor.asInstanceOf[ScalaLocalVariableResolveProcessor].offset
      getBindingReferences(processor, substitutor)
    } else true
  }

  def getBindings = childrenOfType[ScBindingImpl](TokenSet.create(Array(ScalaElementTypes.BINDING)))

  def getBindingReferences(processor: PsiScopeProcessor,
          substitutor: PsiSubstitutor): Boolean = {

    // Scan for parameters
    for (val binding <- getBindings; binding.getTextOffset <= varOffset) {
      if (binding != null && ! processor.execute(binding, substitutor)) {
        return false
      }
    }
    return true
  }

  override def toString: String = "Anonymous function"

  def getType(): PsiType = null
}


/**
*   Main entity of code of block
*
*/
case class ScBlockImpl(node: ASTNode) extends ScalaPsiElementImpl (node)
with ScBlock with Importable with LocalContainer{
  override def toString: String = "Block"

  def getType(): PsiType = null

  def getTmplDefs = childrenOfType[ScalaPsiElementImpl](ScalaElementTypes.TMPL_DEF_BIT_SET)

  override def processDeclarations(processor: PsiScopeProcessor,
          substitutor: PsiSubstitutor,
          lastParent: PsiElement,
          place: PsiElement): Boolean = {
    import org.jetbrains.plugins.scala.lang.resolve.processors._

    if (processor.isInstanceOf[ScalaClassResolveProcessor]) { // Get Class types
        this.canBeObject = processor.asInstanceOf[ScalaClassResolveProcessor].canBeObject
        this.offset = processor.asInstanceOf[ScalaClassResolveProcessor].offset
      getClazz(getTmplDefs, processor, substitutor)
    } else if (processor.isInstanceOf[ScalaLocalVariableResolveProcessor]){
      // Get Local variables
        this.varOffset = processor.asInstanceOf[ScalaLocalVariableResolveProcessor].offset
      getVariable(processor, substitutor)
    } else true
  }
}

case class ScTupleImpl(node: ASTNode) extends ScalaPsiElementImpl (node){
  override def toString: String = "Tuple"
  def getType(): PsiType = null
}

