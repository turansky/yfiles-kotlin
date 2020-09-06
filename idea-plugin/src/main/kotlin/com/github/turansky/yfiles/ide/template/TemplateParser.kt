package com.github.turansky.yfiles.ide.template

import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.Ref
import com.intellij.psi.impl.source.parsing.xml.XmlParsing
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ThreeState
import com.intellij.util.TripleFunction
import com.intellij.util.diff.FlyweightCapableTreeStructure

/**
 * Copy of [XmlParser][com.intellij.psi.impl.source.parsing.xml.XmlParser].
 * Difference - [TemplateParsing] instead of [XmlParsing][com.intellij.psi.impl.source.parsing.xml.XmlParsing].
 *
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/master/xml/xml-psi-impl/src/com/intellij/psi/impl/source/parsing/xml/XmlParser.java">XmlParser</a>
 */
class TemplateParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        builder.enforceCommentTokens(TokenSet.EMPTY)
        builder.putUserData(PsiBuilderImpl.CUSTOM_COMPARATOR, REPARSE_XML_TAG_BY_NAME)
        val file = builder.mark()
        TemplateParsing(builder).parseDocument()
        file.done(root)
        return builder.treeBuilt
    }

    private companion object {
        val REPARSE_XML_TAG_BY_NAME = TripleFunction(::reparseXmlByTagName)

        // tries to match an old and new XmlTag by name
        fun reparseXmlByTagName(
            oldNode: ASTNode,
            newNode: LighterASTNode,
            structure: FlyweightCapableTreeStructure<LighterASTNode>
        ): ThreeState {
            if (oldNode is XmlTag && newNode.tokenType === XmlElementType.XML_TAG) {
                val oldName = (oldNode as XmlTag).name
                val childrenRef: Ref<Array<LighterASTNode>> = Ref.create(null)
                val count = structure.getChildren(newNode, childrenRef)
                if (count < 3) return ThreeState.UNSURE
                val children: Array<LighterASTNode> = childrenRef.get()
                if (children[0].tokenType !== XmlTokenType.XML_START_TAG_START) return ThreeState.UNSURE
                if (children[1].tokenType !== XmlTokenType.XML_NAME) return ThreeState.UNSURE
                if (children[2].tokenType !== XmlTokenType.XML_TAG_END) return ThreeState.UNSURE
                val name = children[1] as LighterASTTokenNode
                val newName = name.text
                if (!Comparing.equal(oldName, newName)) return ThreeState.NO
            }

            return ThreeState.UNSURE
        }
    }
}

class TemplateParsing(builder: PsiBuilder) : XmlParsing(builder) {
    override fun parseTag(multipleRootTagError: Boolean) {
        super.parseTag(false)
    }
}
